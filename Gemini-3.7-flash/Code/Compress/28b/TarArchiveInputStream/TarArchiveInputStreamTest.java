package org.apache.commons.compress.archivers.tar;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TarArchiveInputStreamTest {

    // Helper method to create a 512-byte tar header record for a standard file
    private byte[] createTarHeader(String name, long size) {
        return createTarHeader(name, size, (byte) '0');
    }

    // Helper method to create a 512-byte tar header record with custom typeflag
    private byte[] createTarHeader(String name, long size, byte typeFlag) {
        byte[] header = new byte[TarConstants.DEFAULT_RCDSIZE];
        byte[] nameBytes = name.getBytes();
        System.arraycopy(nameBytes, 0, header, 0, Math.min(nameBytes.length, 100));

        // Mode: 0644
        byte[] mode = "0000644\0".getBytes();
        System.arraycopy(mode, 0, header, 100, mode.length);

        // UID: 0000
        byte[] uid = "0000000\0".getBytes();
        System.arraycopy(uid, 0, header, 108, uid.length);

        // GID: 0000
        byte[] gid = "0000000\0".getBytes();
        System.arraycopy(gid, 0, header, 116, gid.length);

        // Size in octal
        String sizeStr = String.format("%011o", size);
        byte[] sizeBytes = sizeStr.getBytes();
        System.arraycopy(sizeBytes, 0, header, 124, sizeBytes.length);

        // Mod time
        byte[] mtime = "00000000000\0".getBytes();
        System.arraycopy(mtime, 0, header, 136, mtime.length);

        // Typeflag
        header[156] = typeFlag;

        // Magic and version (POSIX ustar)
        byte[] magic = TarConstants.MAGIC_POSIX.getBytes();
        System.arraycopy(magic, 0, header, TarConstants.MAGIC_OFFSET, magic.length);
        byte[] version = TarConstants.VERSION_POSIX.getBytes();
        System.arraycopy(version, 0, header, TarConstants.VERSION_OFFSET, version.length);

        // Checksum calculation
        for (int i = 0; i < 8; i++) {
            header[148 + i] = ' ';
        }
        long sum = 0;
        for (byte b : header) {
            sum += (b & 0xFF);
        }
        String chkStr = String.format("%06o\0 ", sum);
        byte[] chkBytes = chkStr.getBytes();
        System.arraycopy(chkBytes, 0, header, 148, Math.min(chkBytes.length, 8));

        return header;
    }

    // Helper method to write a record with 512-byte padding
    private void writePaddedRecord(ByteArrayOutputStream baos, byte[] data) throws IOException {
        baos.write(data);
        int pad = 512 - (data.length % 512);
        if (pad > 0 && pad < 512) {
            baos.write(new byte[pad]);
        }
    }

    // Tests matches() with POSIX, GNU, and ANT tar signatures
    @Test
    public void testMatches_validSignatures_returnsTrue() {
        byte[] header = new byte[512];

        // POSIX ustar
        System.arraycopy(TarConstants.MAGIC_POSIX.getBytes(), 0, header, TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN);
        System.arraycopy(TarConstants.VERSION_POSIX.getBytes(), 0, header, TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN);
        assertTrue(TarArchiveInputStream.matches(header, 512));

        // GNU tar (space version)
        System.arraycopy(TarConstants.MAGIC_GNU.getBytes(), 0, header, TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN);
        System.arraycopy(TarConstants.VERSION_GNU_SPACE.getBytes(), 0, header, TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN);
        assertTrue(TarArchiveInputStream.matches(header, 512));

        // GNU tar (zero version)
        System.arraycopy(TarConstants.VERSION_GNU_ZERO.getBytes(), 0, header, TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN);
        assertTrue(TarArchiveInputStream.matches(header, 512));

        // ANT tar
        System.arraycopy(TarConstants.MAGIC_ANT.getBytes(), 0, header, TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN);
        System.arraycopy(TarConstants.VERSION_ANT.getBytes(), 0, header, TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN);
        assertTrue(TarArchiveInputStream.matches(header, 512));
    }

    // Tests matches() with invalid length and signature
    @Test
    public void testMatches_invalidOrShortSignature_returnsFalse() {
        byte[] shortHeader = new byte[100];
        assertFalse(TarArchiveInputStream.matches(shortHeader, 100));

        byte[] invalidHeader = new byte[512];
        assertFalse(TarArchiveInputStream.matches(invalidHeader, 512));
    }

    // Tests constructor variations and getRecordSize
    @Test
    public void testConstructors_variousParameters_initializesCorrectly() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tais1 = new TarArchiveInputStream(bais);
        assertEquals(TarConstants.DEFAULT_RCDSIZE, tais1.getRecordSize());
        tais1.close();

        TarArchiveInputStream tais2 = new TarArchiveInputStream(bais, 1024);
        assertEquals(TarConstants.DEFAULT_RCDSIZE, tais2.getRecordSize());
        tais2.close();

        TarArchiveInputStream tais3 = new TarArchiveInputStream(bais, 1024, 512);
        assertEquals(512, tais3.getRecordSize());
        tais3.close();

        TarArchiveInputStream tais4 = new TarArchiveInputStream(bais, "UTF-8");
        assertEquals(TarConstants.DEFAULT_RCDSIZE, tais4.getRecordSize());
        tais4.close();

        TarArchiveInputStream tais5 = new TarArchiveInputStream(bais, 1024, "UTF-8");
        assertEquals(TarConstants.DEFAULT_RCDSIZE, tais5.getRecordSize());
        tais5.close();
    }

    // Tests read() before any entry is opened throws IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testRead_noCurrentEntry_throwsIllegalStateException() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[512]);
        TarArchiveInputStream tais = new TarArchiveInputStream(bais);
        try {
            tais.read(new byte[10], 0, 10);
        } finally {
            tais.close();
        }
    }

    // Tests getNextTarEntry on an empty stream returns null
    @Test
    public void testGetNextTarEntry_emptyStream_returnsNull() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tais = new TarArchiveInputStream(bais);
        assertNull(tais.getNextTarEntry());
        assertNull(tais.getNextEntry());
        tais.close();
    }

    // Tests getNextTarEntry on all-zero EOF record returns null
    @Test
    public void testGetNextTarEntry_allZeroRecord_returnsNull() throws IOException {
        byte[] data = new byte[1024]; // Two 512-byte zero records
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        TarArchiveInputStream tais = new TarArchiveInputStream(bais);
        assertNull(tais.getNextTarEntry());
        assertTrue(tais.isAtEOF());
        tais.close();
    }

    // Tests reading entry data and available count
    @Test
    public void testRead_validEntry_readsDataCorrectly() throws IOException {
        byte[] content = "Hello, Tar Archive!".getBytes();
        byte[] header = createTarHeader("test.txt", content.length);

        byte[] archive = new byte[header.length + 512 + 1024];
        System.arraycopy(header, 0, archive, 0, header.length);
        System.arraycopy(content, 0, archive, header.length, content.length);

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(archive));
        TarArchiveEntry entry = tais.getNextTarEntry();
        assertNotNull(entry);
        assertEquals("test.txt", entry.getName());
        assertEquals(content.length, tais.available());
        assertEquals(entry, tais.getCurrentEntry());

        byte[] readBuf = new byte[content.length];
        int bytesRead = tais.read(readBuf, 0, readBuf.length);
        assertEquals(content.length, bytesRead);
        assertEquals("Hello, Tar Archive!", new String(readBuf, 0, bytesRead));

        assertEquals(0, tais.available());
        assertEquals(-1, tais.read(readBuf, 0, readBuf.length));
        assertNull(tais.getNextTarEntry());
        tais.close();
    }

    // Tests skip() within entry boundaries
    @Test
    public void testSkip_withinEntry_skipsExpectedBytes() throws IOException {
        byte[] content = "0123456789ABCDEF".getBytes();
        byte[] header = createTarHeader("skip.txt", content.length);

        byte[] archive = new byte[header.length + 512 + 1024];
        System.arraycopy(header, 0, archive, 0, header.length);
        System.arraycopy(content, 0, archive, header.length, content.length);

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(archive));
        assertNotNull(tais.getNextTarEntry());

        long skipped = tais.skip(5);
        assertEquals(5, skipped);
        assertEquals(content.length - 5, tais.available());

        byte[] buf = new byte[5];
        int read = tais.read(buf, 0, 5);
        assertEquals(5, read);
        assertEquals("56789", new String(buf, 0, read));

        long skipRemaining = tais.skip(100);
        assertEquals(content.length - 10, skipRemaining);
        assertEquals(0, tais.available());

        tais.close();
    }

    // Tests canReadEntryData with TarArchiveEntry and non-TarArchiveEntry
    @Test
    public void testCanReadEntryData_variousEntries_returnsExpected() {
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        TarArchiveEntry regularEntry = new TarArchiveEntry("regular.txt");
        assertTrue(tais.canReadEntryData(regularEntry));

        ArchiveEntry anonymousEntry = new ArchiveEntry() {
            @Override
            public String getName() { return "anon"; }
            @Override
            public long getSize() { return 0; }
            @Override
            public boolean isDirectory() { return false; }
            @Override
            public java.util.Date getLastModifiedDate() { return null; }
        };
        assertFalse(tais.canReadEntryData(anonymousEntry));
    }

    // Tests isEOFRecord with zero buffer and non-zero buffer
    @Test
    public void testIsEOFRecord_zeroAndNonZero_returnsExpected() {
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        assertTrue(tais.isEOFRecord(null));
        assertTrue(tais.isEOFRecord(new byte[512]));

        byte[] nonZero = new byte[512];
        nonZero[0] = 'a';
        assertFalse(tais.isEOFRecord(nonZero));
    }

    // Tests parsePaxHeaders with valid PAX header entries
    @Test
    public void testParsePaxHeaders_validHeaders_parsesCorrectly() throws IOException {
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        String paxData = "25 path=long/file/name\n19 size=102400\n";
        ByteArrayInputStream in = new ByteArrayInputStream(paxData.getBytes("UTF-8"));

        Map<String, String> headers = tais.parsePaxHeaders(in);
        assertEquals(2, headers.size());
        assertEquals("long/file/name", headers.get("path"));
        assertEquals("102400", headers.get("size"));
    }

    // Tests parsePaxHeaders with truncated PAX header throws IOException
    @Test(expected = IOException.class)
    public void testParsePaxHeaders_truncatedHeader_throwsIOException() throws IOException {
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        String paxData = "30 path=truncated";
        ByteArrayInputStream in = new ByteArrayInputStream(paxData.getBytes("UTF-8"));
        tais.parsePaxHeaders(in);
    }

    // Tests reading a truncated entry where stream hits EOF prematurely
    @Test
    public void testRead_truncatedEntry_detectsEOF() throws IOException {
        byte[] content = "0123456789".getBytes();
        byte[] header = createTarHeader("truncated.txt", 100); // Declares size 100 but only provides 10 bytes

        byte[] archive = new byte[header.length + content.length];
        System.arraycopy(header, 0, archive, 0, header.length);
        System.arraycopy(content, 0, archive, header.length, content.length);

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(archive));
        assertNotNull(tais.getNextTarEntry());

        byte[] buf = new byte[50];
        int readFirst = tais.read(buf, 0, 50);
        assertEquals(10, readFirst);

        // Subsequent read reaches underlying EOF before full entrySize is read
        int readSecond = tais.read(buf, 0, 50);
        assertEquals(-1, readSecond);
        assertTrue(tais.isAtEOF());

        tais.close();
    }

    // Tests reset method does not fail
    @Test
    public void testReset_invocation_doesNotThrow() {
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        tais.reset();
    }

    // Tests setCurrentEntry and isAtEOF state helpers
    @Test
    public void testSetCurrentEntryAndSetAtEOF_updatesState() {
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        assertFalse(tais.isAtEOF());
        tais.setAtEOF(true);
        assertTrue(tais.isAtEOF());

        TarArchiveEntry entry = new TarArchiveEntry("test");
        tais.setCurrentEntry(entry);
        assertEquals(entry, tais.getCurrentEntry());
    }

    // Tests constructor with all 4 parameters (InputStream, blockSize, recordSize, encoding)
    @Test
    public void testConstructor_withFourParameters() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tais = new TarArchiveInputStream(bais, 1024, 512, "UTF-8");
        assertEquals(512, tais.getRecordSize());
        tais.close();
    }

    // Tests single-byte read() and zero-length read()
    @Test
    public void testRead_singleByteAndZeroLength() throws IOException {
        byte[] content = "ABC".getBytes();
        byte[] header = createTarHeader("testRead.txt", content.length);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(header);
        writePaddedRecord(baos, content);
        baos.write(new byte[1024]); // EOF blocks

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        assertNotNull(tais.getNextTarEntry());

        // Zero length read
        assertEquals(0, tais.read(new byte[10], 0, 0));

        // Single byte read
        assertEquals('A', tais.read());
        assertEquals('B', tais.read());
        assertEquals('C', tais.read());
        assertEquals(-1, tais.read());

        tais.close();
    }

    // Tests skip with non-positive values and mark/markSupported methods
    @Test
    public void testSkip_zeroOrNegativeAndMarkSupported() throws IOException {
        byte[] content = "DATA".getBytes();
        byte[] header = createTarHeader("skipTest.txt", content.length);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(header);
        writePaddedRecord(baos, content);
        baos.write(new byte[1024]);

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        assertFalse(tais.markSupported());
        tais.mark(10); // should not fail or affect anything

        assertNotNull(tais.getNextTarEntry());
        assertEquals(0, tais.skip(0));
        assertEquals(0, tais.skip(-10));

        // Skip remaining
        assertEquals(4, tais.skip(4));
        // Skipping when entry is fully consumed returns 0
        assertEquals(0, tais.skip(5));

        tais.close();
    }

    // Tests skipping over unread entry data when getting the next entry
    @Test
    public void testGetNextTarEntry_skipsRemainingDataOfPreviousEntry() throws IOException {
        byte[] content1 = "First entry content that will not be fully read.".getBytes();
        byte[] header1 = createTarHeader("first.txt", content1.length);

        byte[] content2 = "Second entry content.".getBytes();
        byte[] header2 = createTarHeader("second.txt", content2.length);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(header1);
        writePaddedRecord(baos, content1);
        baos.write(header2);
        writePaddedRecord(baos, content2);
        baos.write(new byte[1024]);

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));

        TarArchiveEntry e1 = tais.getNextTarEntry();
        assertNotNull(e1);
        assertEquals("first.txt", e1.getName());
        // Read only 5 bytes from first entry
        byte[] buf = new byte[5];
        assertEquals(5, tais.read(buf, 0, 5));

        // Moving to next entry automatically skips remaining data of e1
        TarArchiveEntry e2 = tais.getNextTarEntry();
        assertNotNull(e2);
        assertEquals("second.txt", e2.getName());

        byte[] contentBuf = new byte[content2.length];
        int read2 = tais.read(contentBuf, 0, contentBuf.length);
        assertEquals(content2.length, read2);
        assertEquals("Second entry content.", new String(contentBuf, 0, read2));

        assertNull(tais.getNextTarEntry());
        tais.close();
    }

    // Tests GNU LongName ('L') header handling
    @Test
    public void testGetNextTarEntry_gnuLongName() throws IOException {
        String longName = "very/long/path/name/that/exceeds/one/hundred/characters/and/must/be/stored/in/a/gnu/long/name/entry/test_file.txt";
        byte[] longNameBytes = (longName + "\0").getBytes("UTF-8");
        byte[] longNameHeader = createTarHeader("././@LongLink", longNameBytes.length, TarConstants.LF_GNUTYPE_LONGNAME);

        byte[] content = "gnu long name file content".getBytes();
        byte[] regularHeader = createTarHeader("short.txt", content.length);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(longNameHeader);
        writePaddedRecord(baos, longNameBytes);
        baos.write(regularHeader);
        writePaddedRecord(baos, content);
        baos.write(new byte[1024]);

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        TarArchiveEntry entry = tais.getNextTarEntry();
        assertNotNull(entry);
        assertEquals(longName, entry.getName());

        byte[] readBuf = new byte[content.length];
        int read = tais.read(readBuf, 0, readBuf.length);
        assertEquals(content.length, read);
        assertEquals("gnu long name file content", new String(readBuf, 0, read));

        assertNull(tais.getNextTarEntry());
        tais.close();
    }

    // Tests GNU LongLink ('K') header handling
    @Test
    public void testGetNextTarEntry_gnuLongLink() throws IOException {
        String longLinkName = "target/of/symbolic/link/with/a/very/long/destination/path/exceeding/standard/tar/header/limit/destination.txt";
        byte[] longLinkBytes = (longLinkName + "\0").getBytes("UTF-8");
        byte[] longLinkHeader = createTarHeader("././@LongLink", longLinkBytes.length, TarConstants.LF_GNUTYPE_LONGLINK);

        byte[] symlinkHeader = createTarHeader("symlink_entry", 0, TarConstants.LF_SYMLINK);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(longLinkHeader);
        writePaddedRecord(baos, longLinkBytes);
        baos.write(symlinkHeader);
        baos.write(new byte[1024]);

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        TarArchiveEntry entry = tais.getNextTarEntry();
        assertNotNull(entry);
        assertEquals("symlink_entry", entry.getName());
        assertEquals(longLinkName, entry.getLinkName());

        assertNull(tais.getNextTarEntry());
        tais.close();
    }

    // Tests PAX header ('x') parsing and applying extended attributes
    @Test
    public void testGetNextTarEntry_paxExtendedHeader() throws IOException {
        String paxData = "30 path=pax/custom/path.txt\n"
                       + "16 size=12\n"
                       + "12 uid=1001\n"
                       + "12 gid=2002\n"
                       + "16 uname=paxuser\n"
                       + "17 gname=paxgroup\n"
                       + "18 mtime=1234567890\n";
        byte[] paxBytes = paxData.getBytes("UTF-8");
        byte[] paxHeader = createTarHeader("PaxHeader/custom.txt", paxBytes.length, TarConstants.LF_PAX_EXTENDED_HEADER_LC);

        byte[] content = "Pax Content!".getBytes();
        byte[] fileHeader = createTarHeader("original_name.txt", content.length);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(paxHeader);
        writePaddedRecord(baos, paxBytes);
        baos.write(fileHeader);
        writePaddedRecord(baos, content);
        baos.write(new byte[1024]);

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        TarArchiveEntry entry = tais.getNextTarEntry();
        assertNotNull(entry);
        assertEquals("pax/custom/path.txt", entry.getName());
        assertEquals(12, entry.getSize());
        assertEquals(1001, entry.getUserId());
        assertEquals(2002, entry.getGroupId());
        assertEquals("paxuser", entry.getUserName());
        assertEquals("paxgroup", entry.getGroupName());
        assertEquals(1234567890L, entry.getModTime().getTime() / 1000);

        byte[] readBuf = new byte[content.length];
        int read = tais.read(readBuf, 0, readBuf.length);
        assertEquals(12, read);
        assertEquals("Pax Content!", new String(readBuf, 0, read));

        assertNull(tais.getNextTarEntry());
        tais.close();
    }

    // Tests Global PAX header ('g') affecting subsequent entries
    @Test
    public void testGetNextTarEntry_globalPaxHeader() throws IOException {
        String globalPax = "16 uname=globalU\n"
                         + "16 gname=globalG\n";
        byte[] globalPaxBytes = globalPax.getBytes("UTF-8");
        byte[] globalHeader = createTarHeader("pax_global_header", globalPaxBytes.length, TarConstants.LF_PAX_GLOBAL_EXTENDED_HEADER);

        byte[] content = "file data".getBytes();
        byte[] fileHeader = createTarHeader("file_under_global.txt", content.length);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(globalHeader);
        writePaddedRecord(baos, globalPaxBytes);
        baos.write(fileHeader);
        writePaddedRecord(baos, content);
        baos.write(new byte[1024]);

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        TarArchiveEntry entry = tais.getNextTarEntry();
        assertNotNull(entry);
        assertEquals("file_under_global.txt", entry.getName());
        assertEquals("globalU", entry.getUserName());
        assertEquals("globalG", entry.getGroupName());

        assertNull(tais.getNextTarEntry());
        tais.close();
    }

    // Tests parsePaxHeaders with UTF-8 multi-byte characters
    @Test
    public void testParsePaxHeaders_utf8Characters() throws IOException {
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        String paxData = "28 path=ทดสอบ/ไฟล์.txt\n";
        ByteArrayInputStream in = new ByteArrayInputStream(paxData.getBytes("UTF-8"));

        Map<String, String> headers = tais.parsePaxHeaders(in);
        assertEquals("ทดสอบ/ไฟล์.txt", headers.get("path"));
        tais.close();
    }
}