package org.apache.commons.compress.archivers.tar;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Map;

import static org.junit.Assert.*;

public class TarArchiveInputStreamTest {

    // Helper to create a tar header block with the given name, size, and type flag
    private byte[] createTarHeader(String name, long size, byte linkFlag) {
        byte[] header = new byte[512];
        byte[] nameBytes = name.getBytes();
        System.arraycopy(nameBytes, 0, header, 0, Math.min(nameBytes.length, 100));

        // Mode: 0644
        String mode = "0000644";
        System.arraycopy(mode.getBytes(), 0, header, 100, mode.length());

        // UID: 0
        String uid = "0000000";
        System.arraycopy(uid.getBytes(), 0, header, 108, uid.length());

        // GID: 0
        String gid = "0000000";
        System.arraycopy(gid.getBytes(), 0, header, 116, gid.length());

        // Size in octal (11 digits + space/null)
        String sizeStr = String.format("%011o", size);
        System.arraycopy(sizeStr.getBytes(), 0, header, 124, sizeStr.length());

        // MTime
        String mtime = "00000000000";
        System.arraycopy(mtime.getBytes(), 0, header, 136, mtime.length());

        // Type flag
        header[156] = linkFlag;

        // Magic POSIX ustar\0
        System.arraycopy(TarConstants.MAGIC_POSIX.getBytes(), 0, header, TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN);
        // Version POSIX 00
        System.arraycopy(TarConstants.VERSION_POSIX.getBytes(), 0, header, TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN);

        // Checksum calculation (initially filled with spaces)
        for (int i = 148; i < 156; i++) {
            header[i] = ' ';
        }
        long sum = 0;
        for (int i = 0; i < 512; i++) {
            sum += header[i] & 0xFF;
        }
        String checksum = String.format("%06o", sum);
        System.arraycopy(checksum.getBytes(), 0, header, 148, checksum.length());
        header[154] = 0;
        header[155] = ' ';

        return header;
    }

    // Tests reading a standard single entry tar archive
    @Test
    public void testGetNextTarEntry_standardEntry_returnsEntryAndReadsContent() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] content = "Hello, Tar!".getBytes();
        byte[] header = createTarHeader("test.txt", content.length, TarConstants.LF_NORMAL);
        baos.write(header);
        baos.write(content);
        // Pad to record boundary (512 bytes)
        int pad = 512 - content.length;
        baos.write(new byte[pad]);
        // EOF records (two 512-byte zero records)
        baos.write(new byte[1024]);

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        TarArchiveEntry entry = tais.getNextTarEntry();
        assertNotNull(entry);
        assertEquals("test.txt", entry.getName());
        assertEquals(content.length, entry.getSize());
        assertEquals(content.length, tais.available());

        byte[] readBuffer = new byte[content.length];
        int bytesRead = tais.read(readBuffer, 0, readBuffer.length);
        assertEquals(content.length, bytesRead);
        assertArrayEquals(content, readBuffer);

        assertEquals(-1, tais.read(readBuffer, 0, readBuffer.length));
        assertNull(tais.getNextTarEntry());
        tais.close();
    }

    // Tests skipping data within an entry and advancing to the next entry
    @Test
    public void testSkip_withinEntry_skipsCorrectNumberOfBytes() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] content = "1234567890ABCDEF".getBytes();
        byte[] header = createTarHeader("skip.txt", content.length, TarConstants.LF_NORMAL);
        baos.write(header);
        baos.write(content);
        baos.write(new byte[512 - content.length]);
        baos.write(new byte[1024]);

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        TarArchiveEntry entry = tais.getNextTarEntry();
        assertNotNull(entry);

        long skipped = tais.skip(5);
        assertEquals(5, skipped);
        assertEquals(content.length - 5, tais.available());

        byte[] readBuffer = new byte[5];
        int bytesRead = tais.read(readBuffer, 0, 5);
        assertEquals(5, bytesRead);
        assertEquals("67890", new String(readBuffer));

        tais.close();
    }

    // Tests GNU LongLink / LongName entry parsing
    @Test
    public void testGetNextTarEntry_gnuLongNameEntry_setsLongNameCorrectly() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String longName = "a/very/long/path/name/that/exceeds/the/standard/tar/one/hundred/characters/limit/test_long_filename.txt";
        byte[] longNameBytes = longName.getBytes("UTF-8");

        // GNU long name header
        byte[] longNameHeader = createTarHeader(TarConstants.GNU_LONGLINK, longNameBytes.length + 1, TarConstants.LF_GNUTYPE_LONGNAME);
        baos.write(longNameHeader);
        baos.write(longNameBytes);
        baos.write(0); // null terminator
        int pad = 512 - ((longNameBytes.length + 1) % 512);
        if (pad < 512) {
            baos.write(new byte[pad]);
        }

        // Actual entry header and content
        byte[] content = "Long name content".getBytes();
        byte[] actualHeader = createTarHeader("short_dummy.txt", content.length, TarConstants.LF_NORMAL);
        baos.write(actualHeader);
        baos.write(content);
        int contentPad = 512 - content.length;
        baos.write(new byte[contentPad]);
        baos.write(new byte[1024]);

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        ArchiveEntry entry = tais.getNextEntry();
        assertNotNull(entry);
        assertEquals(longName, entry.getName());
        assertEquals(content.length, entry.getSize());
        assertNull(tais.getNextEntry());
        tais.close();
    }

    // Tests Pax headers parsing method directly
    @Test
    public void testParsePaxHeaders_validHeaders_parsesMap() throws IOException {
        String paxData = "25 path=extended_name.txt\n"
                       + "16 size=123456\n"
                       + "13 uid=1001\n"
                       + "13 gid=1002\n"
                       + "17 uname=testuser\n"
                       + "18 gname=testgroup\n"
                       + "20 linkpath=target.txt\n";

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        Map<String, String> headers = tais.parsePaxHeaders(new StringReader(paxData));

        assertEquals("extended_name.txt", headers.get("path"));
        assertEquals("123456", headers.get("size"));
        assertEquals("1001", headers.get("uid"));
        assertEquals("1002", headers.get("gid"));
        assertEquals("testuser", headers.get("uname"));
        assertEquals("testgroup", headers.get("gname"));
        assertEquals("target.txt", headers.get("linkpath"));
    }

    // Tests Pax headers exception on corrupted header length
    @Test(expected = IOException.class)
    public void testParsePaxHeaders_invalidLength_throwsIOException() throws IOException {
        String paxData = "50 path=short.txt\n";
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        tais.parsePaxHeaders(new StringReader(paxData));
    }

    // Tests Pax headers processing when reading archive entries
    @Test
    public void testGetNextTarEntry_paxHeaderEntry_appliesPaxHeaders() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String paxData = "25 path=override_name.txt\n"
                       + "13 size=5\n"
                       + "13 uid=1001\n"
                       + "13 gid=1002\n";
        byte[] paxBytes = paxData.getBytes("UTF-8");

        // Pax header entry (type 'x')
        byte[] paxHeader = createTarHeader("pax_hdr", paxBytes.length, TarConstants.LF_PAX_EXTENDED_HEADER_LC);
        baos.write(paxHeader);
        baos.write(paxBytes);
        int paxPad = 512 - (paxBytes.length % 512);
        if (paxPad < 512) {
            baos.write(new byte[paxPad]);
        }

        // Target file entry
        byte[] content = "Hello".getBytes();
        byte[] actualHeader = createTarHeader("orig_name.txt", content.length, TarConstants.LF_NORMAL);
        baos.write(actualHeader);
        baos.write(content);
        baos.write(new byte[512 - content.length]);
        baos.write(new byte[1024]);

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        TarArchiveEntry entry = tais.getNextTarEntry();
        assertNotNull(entry);
        assertEquals("override_name.txt", entry.getName());
        assertEquals(5, entry.getSize());
        assertEquals(1001, entry.getUserId());
        assertEquals(1002, entry.getGroupId());
        tais.close();
    }

    // Tests canReadEntryData for standard and GNU sparse entries
    @Test
    public void testCanReadEntryData_standardAndSparse_returnsExpected() {
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        byte[] normalHeader = createTarHeader("normal.txt", 100, TarConstants.LF_NORMAL);
        TarArchiveEntry normalEntry = new TarArchiveEntry(normalHeader);
        assertTrue(tais.canReadEntryData(normalEntry));

        byte[] sparseHeader = createTarHeader("sparse.txt", 100, TarConstants.LF_GNUTYPE_SPARSE);
        TarArchiveEntry sparseEntry = new TarArchiveEntry(sparseHeader);
        assertFalse(tais.canReadEntryData(sparseEntry));

        assertFalse(tais.canReadEntryData(null));
    }

    // Tests signature matching for POSIX, GNU, and Ant tar files
    @Test
    public void testMatches_validSignatures_returnsTrue() {
        byte[] posixSig = new byte[512];
        System.arraycopy(TarConstants.MAGIC_POSIX.getBytes(), 0, posixSig, TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN);
        System.arraycopy(TarConstants.VERSION_POSIX.getBytes(), 0, posixSig, TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN);
        assertTrue(TarArchiveInputStream.matches(posixSig, 512));

        byte[] gnuSigSpace = new byte[512];
        System.arraycopy(TarConstants.MAGIC_GNU.getBytes(), 0, gnuSigSpace, TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN);
        System.arraycopy(TarConstants.VERSION_GNU_SPACE.getBytes(), 0, gnuSigSpace, TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN);
        assertTrue(TarArchiveInputStream.matches(gnuSigSpace, 512));

        byte[] gnuSigZero = new byte[512];
        System.arraycopy(TarConstants.MAGIC_GNU.getBytes(), 0, gnuSigZero, TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN);
        System.arraycopy(TarConstants.VERSION_GNU_ZERO.getBytes(), 0, gnuSigZero, TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN);
        assertTrue(TarArchiveInputStream.matches(gnuSigZero, 512));

        byte[] antSig = new byte[512];
        System.arraycopy(TarConstants.MAGIC_ANT.getBytes(), 0, antSig, TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN);
        System.arraycopy(TarConstants.VERSION_ANT.getBytes(), 0, antSig, TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN);
        assertTrue(TarArchiveInputStream.matches(antSig, 512));
    }

    // Tests signature matching for short or invalid signatures
    @Test
    public void testMatches_invalidOrShortSignature_returnsFalse() {
        byte[] empty = new byte[100];
        assertFalse(TarArchiveInputStream.matches(empty, 100));

        byte[] invalid = new byte[512];
        assertFalse(TarArchiveInputStream.matches(invalid, 512));
    }

    // Tests constructor with block and record size and getRecordSize
    @Test
    public void testConstructorAndGetRecordSize_customParameters_returnsConfiguredSize() throws IOException {
        InputStream emptyStream = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tais = new TarArchiveInputStream(emptyStream, 1024, 512);
        assertEquals(512, tais.getRecordSize());
        tais.close();
    }

    // Tests available method with available data calculation and reset no-op
    @Test
    public void testAvailableAndReset_behaveCorrectly() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] content = "Some content".getBytes();
        byte[] header = createTarHeader("test.txt", content.length, TarConstants.LF_NORMAL);
        baos.write(header);
        baos.write(content);
        baos.write(new byte[512 - content.length]);
        baos.write(new byte[1024]);

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        tais.reset(); // no-op test
        assertEquals(0, tais.available());

        tais.getNextTarEntry();
        assertEquals(content.length, tais.available());
        tais.close();
    }

    // Tests reading past unexpected EOF throws IOException
    @Test(expected = IOException.class)
    public void testRead_unexpectedEOF_throwsIOException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] header = createTarHeader("truncated.txt", 1024, TarConstants.LF_NORMAL);
        baos.write(header);
        // Truncated stream without data or EOF records

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        tais.getNextTarEntry();
        byte[] buf = new byte[512];
        tais.read(buf, 0, 512);
    }
}