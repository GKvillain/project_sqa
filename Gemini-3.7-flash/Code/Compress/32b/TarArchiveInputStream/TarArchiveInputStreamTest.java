package org.apache.commons.compress.archivers.tar;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.utils.CharsetNames;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Map;

import static org.junit.Assert.*;

public class TarArchiveInputStreamTest {

    // Tests matches() with POSIX tar magic and version
    @Test
    public void testMatches_posixMagic_returnsTrue() {
        byte[] header = new byte[512];
        System.arraycopy(TarConstants.MAGIC_POSIX.getBytes(), 0, header, TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN);
        System.arraycopy(TarConstants.VERSION_POSIX.getBytes(), 0, header, TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN);
        assertTrue(TarArchiveInputStream.matches(header, 512));
    }

    // Tests matches() with GNU tar magic and version space
    @Test
    public void testMatches_gnuMagicSpace_returnsTrue() {
        byte[] header = new byte[512];
        System.arraycopy(TarConstants.MAGIC_GNU.getBytes(), 0, header, TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN);
        System.arraycopy(TarConstants.VERSION_GNU_SPACE.getBytes(), 0, header, TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN);
        assertTrue(TarArchiveInputStream.matches(header, 512));
    }

    // Tests matches() with GNU tar magic and version zero
    @Test
    public void testMatches_gnuMagicZero_returnsTrue() {
        byte[] header = new byte[512];
        System.arraycopy(TarConstants.MAGIC_GNU.getBytes(), 0, header, TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN);
        System.arraycopy(TarConstants.VERSION_GNU_ZERO.getBytes(), 0, header, TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN);
        assertTrue(TarArchiveInputStream.matches(header, 512));
    }

    // Tests matches() with Ant tar magic and version
    @Test
    public void testMatches_antMagic_returnsTrue() {
        byte[] header = new byte[512];
        System.arraycopy(TarConstants.MAGIC_ANT.getBytes(), 0, header, TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN);
        System.arraycopy(TarConstants.VERSION_ANT.getBytes(), 0, header, TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN);
        assertTrue(TarArchiveInputStream.matches(header, 512));
    }

    // Tests matches() with insufficient length
    @Test
    public void testMatches_shortLength_returnsFalse() {
        byte[] header = new byte[100];
        assertFalse(TarArchiveInputStream.matches(header, 100));
    }

    // Tests matches() with invalid magic
    @Test
    public void testMatches_invalidMagic_returnsFalse() {
        byte[] header = new byte[512];
        assertFalse(TarArchiveInputStream.matches(header, 512));
    }

    // Tests constructors and getRecordSize
    @Test
    public void testConstructors_customBlockAndRecordSize() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tais = new TarArchiveInputStream(bais, 1024, 512, "UTF-8");
        assertEquals(512, tais.getRecordSize());
        assertFalse(tais.markSupported());
        tais.mark(100);
        tais.reset();
        tais.close();
    }

    // Tests isEOFRecord with null and empty byte array
    @Test
    public void testIsEOFRecord_emptyAndNullRecord_returnsTrue() {
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        assertTrue(tais.isEOFRecord(null));
        assertTrue(tais.isEOFRecord(new byte[512]));
        byte[] nonEof = new byte[512];
        nonEof[0] = 1;
        assertFalse(tais.isEOFRecord(nonEof));
    }

    // Tests getNextTarEntry on empty stream
    @Test
    public void testGetNextTarEntry_emptyStream_returnsNull() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tais = new TarArchiveInputStream(bais);
        assertNull(tais.getNextTarEntry());
        assertTrue(tais.isAtEOF());
    }

    // Tests parsePaxHeaders with valid key-value pairs
    @Test
    public void testParsePaxHeaders_validHeaders_returnsParsedMap() throws IOException {
        String paxData = "25 path=test/long/name\n16 size=10240\n18 uid=4294967294\n";
        ByteArrayInputStream bais = new ByteArrayInputStream(paxData.getBytes(CharsetNames.UTF_8));
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        Map<String, String> headers = tais.parsePaxHeaders(bais);
        assertEquals("test/long/name", headers.get("path"));
        assertEquals("10240", headers.get("size"));
        assertEquals("4294967294", headers.get("uid"));
    }

    // Tests parsePaxHeaders with truncated stream
    @Test(expected = IOException.class)
    public void testParsePaxHeaders_truncatedData_throwsException() throws IOException {
        String paxData = "30 path=incomplete";
        ByteArrayInputStream bais = new ByteArrayInputStream(paxData.getBytes(CharsetNames.UTF_8));
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        tais.parsePaxHeaders(bais);
    }

    // Tests reading simple archive with single file entry
    @Test
    public void testRead_singleEntryArchive_readsContentSuccessfully() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream taos = new TarArchiveOutputStream(baos);
        TarArchiveEntry entry = new TarArchiveEntry("file.txt");
        byte[] content = "Hello, TarArchiveInputStream!".getBytes("UTF-8");
        entry.setSize(content.length);
        taos.putArchiveEntry(entry);
        taos.write(content);
        taos.closeArchiveEntry();
        taos.close();

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        TarArchiveEntry readEntry = tais.getNextTarEntry();
        assertNotNull(readEntry);
        assertEquals("file.txt", readEntry.getName());
        assertEquals(content.length, readEntry.getSize());
        assertEquals(content.length, tais.available());
        assertTrue(tais.canReadEntryData(readEntry));
        assertSame(readEntry, tais.getCurrentEntry());

        byte[] readBuffer = new byte[content.length];
        int bytesRead = tais.read(readBuffer, 0, readBuffer.length);
        assertEquals(content.length, bytesRead);
        assertArrayEquals(content, readBuffer);
        assertEquals(0, tais.available());

        assertEquals(-1, tais.read(readBuffer, 0, readBuffer.length));
        assertNull(tais.getNextTarEntry());
        tais.close();
    }

    // Tests skip functionality within an entry
    @Test
    public void testSkip_withinEntry_skipsExpectedBytes() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream taos = new TarArchiveOutputStream(baos);
        TarArchiveEntry entry = new TarArchiveEntry("skip.txt");
        byte[] content = "0123456789abcdefghij".getBytes("UTF-8");
        entry.setSize(content.length);
        taos.putArchiveEntry(entry);
        taos.write(content);
        taos.closeArchiveEntry();
        taos.close();

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        tais.getNextEntry();

        assertEquals(0, tais.skip(-5));
        assertEquals(0, tais.skip(0));
        long skipped = tais.skip(10);
        assertEquals(10, skipped);
        byte[] remaining = new byte[10];
        int read = tais.read(remaining, 0, 10);
        assertEquals(10, read);
        assertEquals("abcdefghij", new String(remaining, "UTF-8"));
        tais.close();
    }

    // Tests read when no current entry is open
    @Test(expected = IllegalStateException.class)
    public void testRead_noCurrentEntry_throwsIllegalStateException() throws IOException {
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        tais.read(new byte[10], 0, 10);
    }

    // Tests reading malformed header throws IOException with cause
    @Test(expected = IOException.class)
    public void testGetNextTarEntry_corruptedHeader_throwsIOException() throws IOException {
        byte[] corrupted = new byte[512];
        for (int i = 0; i < corrupted.length; i++) {
            corrupted[i] = (byte) 0xFF;
        }
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(corrupted));
        tais.getNextTarEntry();
    }

    // Tests handling of PAX headers with large numeric values such as big UID/GID
    @Test
    public void testGetNextTarEntry_paxHeaderWithBigNumbers_updatesEntryMetadata() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream taos = new TarArchiveOutputStream(baos);

        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.setSize(5);
        taos.putArchiveEntry(entry);
        taos.write("12345".getBytes("UTF-8"));
        taos.closeArchiveEntry();
        taos.close();

        // Construct tar with PAX header entry preceding the file entry
        String paxContent = "30 uid=4294967294\n30 gid=4294967295\n26 path=new_name.txt\n";
        byte[] paxBytes = paxContent.getBytes(CharsetNames.UTF_8);

        ByteArrayOutputStream paxTarBaos = new ByteArrayOutputStream();
        TarArchiveOutputStream paxTaos = new TarArchiveOutputStream(paxTarBaos);
        TarArchiveEntry paxEntry = new TarArchiveEntry("PaxHeader/test.txt", TarConstants.LF_PAX_EXTENDED_HEADER_LC);
        paxEntry.setSize(paxBytes.length);
        paxTaos.putArchiveEntry(paxEntry);
        paxTaos.write(paxBytes);
        paxTaos.closeArchiveEntry();

        TarArchiveEntry actualEntry = new TarArchiveEntry("test.txt");
        actualEntry.setSize(5);
        paxTaos.putArchiveEntry(actualEntry);
        paxTaos.write("12345".getBytes("UTF-8"));
        paxTaos.closeArchiveEntry();
        paxTaos.close();

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(paxTarBaos.toByteArray()));
        TarArchiveEntry resolvedEntry = tais.getNextTarEntry();
        assertNotNull(resolvedEntry);
        assertEquals("new_name.txt", resolvedEntry.getName());
        assertEquals(4294967294L, (long) resolvedEntry.getUserId());
        assertEquals(4294967295L, (long) resolvedEntry.getGroupId());
        tais.close();
    }

    // Tests GNU long name entry reading
    @Test
    public void testGetNextTarEntry_gnuLongNameEntry_setsLongName() throws IOException {
        String longFileName = "a/very/long/path/name/that/exceeds/the/standard/tar/one/hundred/bytes/limit/for/file/names/test_long_file_name.txt";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream taos = new TarArchiveOutputStream(baos);
        taos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);

        TarArchiveEntry entry = new TarArchiveEntry(longFileName);
        byte[] content = "gnu long name content".getBytes("UTF-8");
        entry.setSize(content.length);
        taos.putArchiveEntry(entry);
        taos.write(content);
        taos.closeArchiveEntry();
        taos.close();

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        TarArchiveEntry readEntry = tais.getNextTarEntry();
        assertNotNull(readEntry);
        assertEquals(longFileName, readEntry.getName());
        tais.close();
    }

    // Tests canReadEntryData with non-tar entry
    @Test
    public void testCanReadEntryData_nonTarEntry_returnsFalse() {
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        ArchiveEntry genericEntry = new ArchiveEntry() {
            public String getName() { return "entry"; }
            public long getSize() { return 0; }
            public boolean isDirectory() { return false; }
            public java.util.Date getLastModifiedDate() { return null; }
        };
        assertFalse(tais.canReadEntryData(genericEntry));
    }

    // Tests other constructor overloads
    @Test
    public void testConstructors_variousOverloads() throws IOException {
        ByteArrayInputStream bais1 = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tais1 = new TarArchiveInputStream(bais1, "UTF-8");
        assertEquals(TarConstants.DEFAULT_RCDSIZE, tais1.getRecordSize());
        tais1.close();

        ByteArrayInputStream bais2 = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tais2 = new TarArchiveInputStream(bais2, 1024);
        assertEquals(TarConstants.DEFAULT_RCDSIZE, tais2.getRecordSize());
        tais2.close();

        ByteArrayInputStream bais3 = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tais3 = new TarArchiveInputStream(bais3, 1024, "UTF-8");
        assertEquals(TarConstants.DEFAULT_RCDSIZE, tais3.getRecordSize());
        tais3.close();

        ByteArrayInputStream bais4 = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tais4 = new TarArchiveInputStream(bais4, 1024, 512);
        assertEquals(512, tais4.getRecordSize());
        tais4.close();
    }

    // Tests GNU long link entry reading
    @Test
    public void testGetNextTarEntry_gnuLongLinkEntry_setsLongLinkName() throws IOException {
        String longLinkName = "a/very/long/target/link/path/that/exceeds/the/standard/tar/one/hundred/bytes/limit/for/symbolic/links/target_file.txt";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream taos = new TarArchiveOutputStream(baos);
        taos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);

        TarArchiveEntry entry = new TarArchiveEntry("symlink.txt", TarConstants.LF_SYMLINK);
        entry.setLinkName(longLinkName);
        taos.putArchiveEntry(entry);
        taos.closeArchiveEntry();
        taos.close();

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        TarArchiveEntry readEntry = tais.getNextTarEntry();
        assertNotNull(readEntry);
        assertEquals("symlink.txt", readEntry.getName());
        assertEquals(longLinkName, readEntry.getLinkName());
        tais.close();
    }

    // Tests PAX header fields: uname, gname, mtime, linkpath, SCHILY.devmajor, SCHILY.devminor
    @Test
    public void testGetNextTarEntry_paxHeaderExtendedFields_appliesAllFields() throws IOException {
        ByteArrayOutputStream paxTarBaos = new ByteArrayOutputStream();
        TarArchiveOutputStream paxTaos = new TarArchiveOutputStream(paxTarBaos);

        String paxContent = "18 uname=alice\n"
                + "16 gname=staff\n"
                + "28 linkpath=target/link.txt\n"
                + "22 mtime=1234567890.5\n"
                + "20 SCHILY.devmajor=8\n"
                + "20 SCHILY.devminor=1\n";
        byte[] paxBytes = paxContent.getBytes(CharsetNames.UTF_8);

        TarArchiveEntry paxEntry = new TarArchiveEntry("PaxHeader/dev_entry", TarConstants.LF_PAX_EXTENDED_HEADER_LC);
        paxEntry.setSize(paxBytes.length);
        paxTaos.putArchiveEntry(paxEntry);
        paxTaos.write(paxBytes);
        paxTaos.closeArchiveEntry();

        TarArchiveEntry actualEntry = new TarArchiveEntry("dev_entry", TarConstants.LF_CHR);
        paxTaos.putArchiveEntry(actualEntry);
        paxTaos.closeArchiveEntry();
        paxTaos.close();

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(paxTarBaos.toByteArray()));
        TarArchiveEntry resolved = tais.getNextTarEntry();
        assertNotNull(resolved);
        assertEquals("alice", resolved.getUserName());
        assertEquals("staff", resolved.getGroupName());
        assertEquals("target/link.txt", resolved.getLinkName());
        assertEquals(1234567890500L, resolved.getModTime().getTime());
        assertEquals(8, resolved.getDevMajor());
        assertEquals(1, resolved.getDevMinor());
        tais.close();
    }

    // Tests Global PAX header entry reading
    @Test
    public void testGetNextTarEntry_globalPaxHeader_isIgnoredAndProceedsToNextEntry() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream taos = new TarArchiveOutputStream(baos);

        String globalPaxContent = "18 uname=global_user\n";
        byte[] globalPaxBytes = globalPaxContent.getBytes(CharsetNames.UTF_8);

        TarArchiveEntry globalPaxEntry = new TarArchiveEntry("GlobalHead.1", TarConstants.LF_PAX_GLOBAL_EXTENDED_HEADER_LC);
        globalPaxEntry.setSize(globalPaxBytes.length);
        taos.putArchiveEntry(globalPaxEntry);
        taos.write(globalPaxBytes);
        taos.closeArchiveEntry();

        TarArchiveEntry fileEntry = new TarArchiveEntry("normal.txt");
        byte[] content = "content".getBytes("UTF-8");
        fileEntry.setSize(content.length);
        taos.putArchiveEntry(fileEntry);
        taos.write(content);
        taos.closeArchiveEntry();
        taos.close();

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        TarArchiveEntry readEntry = tais.getNextTarEntry();
        assertNotNull(readEntry);
        assertEquals("normal.txt", readEntry.getName());
        tais.close();
    }

    // Tests canReadEntryData on GNU Sparse entry returns false
    @Test
    public void testCanReadEntryData_sparseEntry_returnsFalse() {
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        byte[] header = new byte[512];
        header[156] = TarConstants.LF_GNUTYPE_SPARSE;
        TarArchiveEntry sparseEntry = new TarArchiveEntry(header);
        assertFalse(tais.canReadEntryData(sparseEntry));
    }

    // Tests automatic skipping of unconsumed previous entry
    @Test
    public void testGetNextTarEntry_unconsumedPreviousEntry_skipsToNextEntry() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream taos = new TarArchiveOutputStream(baos);

        TarArchiveEntry entry1 = new TarArchiveEntry("entry1.txt");
        byte[] content1 = "first entry content with extra padding".getBytes("UTF-8");
        entry1.setSize(content1.length);
        taos.putArchiveEntry(entry1);
        taos.write(content1);
        taos.closeArchiveEntry();

        TarArchiveEntry entry2 = new TarArchiveEntry("entry2.txt");
        byte[] content2 = "second entry content".getBytes("UTF-8");
        entry2.setSize(content2.length);
        taos.putArchiveEntry(entry2);
        taos.write(content2);
        taos.closeArchiveEntry();
        taos.close();

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        TarArchiveEntry read1 = tais.getNextTarEntry();
        assertNotNull(read1);
        assertEquals("entry1.txt", read1.getName());

        // Don't read content of entry1, directly request entry2
        TarArchiveEntry read2 = tais.getNextTarEntry();
        assertNotNull(read2);
        assertEquals("entry2.txt", read2.getName());

        byte[] buf = new byte[content2.length];
        int readBytes = tais.read(buf, 0, buf.length);
        assertEquals(content2.length, readBytes);
        assertArrayEquals(content2, buf);
        tais.close();
    }

    // Tests skip beyond remaining bytes in entry
    @Test
    public void testSkip_moreThanAvailable_skipsOnlyAvailableBytes() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream taos = new TarArchiveOutputStream(baos);
        TarArchiveEntry entry = new TarArchiveEntry("file.txt");
        byte[] content = "12345".getBytes("UTF-8");
        entry.setSize(content.length);
        taos.putArchiveEntry(entry);
        taos.write(content);
        taos.closeArchiveEntry();
        taos.close();

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        tais.getNextTarEntry();

        long skipped = tais.skip(100);
        assertEquals(5, skipped);
        assertEquals(0, tais.available());
        assertEquals(-1, tais.read(new byte[5], 0, 5));
        tais.close();
    }

    // Tests reading zero bytes
    @Test
    public void testRead_zeroBytes_returnsZero() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream taos = new TarArchiveOutputStream(baos);
        TarArchiveEntry entry = new TarArchiveEntry("file.txt");
        byte[] content = "data".getBytes("UTF-8");
        entry.setSize(content.length);
        taos.putArchiveEntry(entry);
        taos.write(content);
        taos.closeArchiveEntry();
        taos.close();

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        tais.getNextTarEntry();

        assertEquals(0, tais.read(new byte[10], 0, 0));
        tais.close();
    }

    // Tests reading an archive that terminates after the first EOF record without a second record
    @Test
    public void testGetNextTarEntry_singleEofRecordThenStreamEnds() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream taos = new TarArchiveOutputStream(baos);
        TarArchiveEntry entry = new TarArchiveEntry("single.txt");
        entry.setSize(0);
        taos.putArchiveEntry(entry);
        taos.closeArchiveEntry();
        taos.close();

        // Extract bytes containing entry + single 512-byte zero block
        byte[] tarBytes = baos.toByteArray();
        byte[] singleEofTar = new byte[1024]; // 512 bytes header + 512 bytes single EOF block
        System.arraycopy(tarBytes, 0, singleEofTar, 0, 1024);

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(singleEofTar));
        assertNotNull(tais.getNextTarEntry());
        assertNull(tais.getNextTarEntry());
        assertTrue(tais.isAtEOF());
        tais.close();
    }

    // Tests reading after EOF returns -1
    @Test
    public void testRead_afterEof_returnsMinusOne() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream taos = new TarArchiveOutputStream(baos);
        taos.close();

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        assertNull(tais.getNextTarEntry());
        assertEquals(-1, tais.read(new byte[10], 0, 10));
        tais.close();
    }
}