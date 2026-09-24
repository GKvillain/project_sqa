package org.apache.commons.compress.archivers.tar;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.junit.Test;

import static org.junit.Assert.*;

public class TarArchiveInputStreamTest {

    // Tests matches method with valid POSIX magic and version
    @Test
    public void testMatches_posixMagic_returnsTrue() {
        byte[] header = new byte[512];
        System.arraycopy(TarConstants.MAGIC_POSIX.getBytes(), 0, header, TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN);
        System.arraycopy(TarConstants.VERSION_POSIX.getBytes(), 0, header, TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN);
        assertTrue(TarArchiveInputStream.matches(header, header.length));
    }

    // Tests matches method with valid GNU magic and version
    @Test
    public void testMatches_gnuMagic_returnsTrue() {
        byte[] header = new byte[512];
        System.arraycopy(TarConstants.MAGIC_GNU.getBytes(), 0, header, TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN);
        System.arraycopy(TarConstants.VERSION_GNU_SPACE.getBytes(), 0, header, TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN);
        assertTrue(TarArchiveInputStream.matches(header, header.length));

        byte[] headerZero = new byte[512];
        System.arraycopy(TarConstants.MAGIC_GNU.getBytes(), 0, headerZero, TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN);
        System.arraycopy(TarConstants.VERSION_GNU_ZERO.getBytes(), 0, headerZero, TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN);
        assertTrue(TarArchiveInputStream.matches(headerZero, headerZero.length));
    }

    // Tests matches method with valid Ant magic and version
    @Test
    public void testMatches_antMagic_returnsTrue() {
        byte[] header = new byte[512];
        System.arraycopy(TarConstants.MAGIC_ANT.getBytes(), 0, header, TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN);
        System.arraycopy(TarConstants.VERSION_ANT.getBytes(), 0, header, TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN);
        assertTrue(TarArchiveInputStream.matches(header, header.length));
    }

    // Tests matches method with short length
    @Test
    public void testMatches_shortLength_returnsFalse() {
        byte[] header = new byte[50];
        assertFalse(TarArchiveInputStream.matches(header, header.length));
    }

    // Tests matches method with invalid magic bytes
    @Test
    public void testMatches_invalidMagic_returnsFalse() {
        byte[] header = new byte[512];
        assertFalse(TarArchiveInputStream.matches(header, header.length));
    }

    // Tests constructors and getRecordSize
    @Test
    public void testConstructors_customParameters_correctRecordSize() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tais1 = new TarArchiveInputStream(bais);
        assertEquals(TarConstants.DEFAULT_RCDSIZE, tais1.getRecordSize());
        tais1.close();

        TarArchiveInputStream tais2 = new TarArchiveInputStream(bais, "UTF-8");
        assertEquals(TarConstants.DEFAULT_RCDSIZE, tais2.getRecordSize());
        tais2.close();

        TarArchiveInputStream tais3 = new TarArchiveInputStream(bais, 1024);
        assertEquals(TarConstants.DEFAULT_RCDSIZE, tais3.getRecordSize());
        tais3.close();

        TarArchiveInputStream tais4 = new TarArchiveInputStream(bais, 1024, "UTF-8");
        assertEquals(TarConstants.DEFAULT_RCDSIZE, tais4.getRecordSize());
        tais4.close();

        TarArchiveInputStream tais5 = new TarArchiveInputStream(bais, 1024, 512);
        assertEquals(512, tais5.getRecordSize());
        tais5.close();

        TarArchiveInputStream tais6 = new TarArchiveInputStream(bais, 1024, 512, "UTF-8");
        assertEquals(512, tais6.getRecordSize());
        tais6.close();
    }

    // Tests mark and reset unsupported behavior
    @Test
    public void testMarkSupported_andReset_doNothing() {
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        assertFalse(tais.markSupported());
        tais.mark(100);
        tais.reset();
    }

    // Tests skip behavior with zero or negative bytes
    @Test
    public void testSkip_zeroOrNegative_returnsZero() throws IOException {
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(new byte[100]));
        assertEquals(0, tais.skip(0));
        assertEquals(0, tais.skip(-10));
        tais.close();
    }

    // Tests read throwing IllegalStateException when no current entry is set
    @Test(expected = IllegalStateException.class)
    public void testRead_noCurrentEntry_throwsIllegalStateException() throws IOException {
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(new byte[512]));
        byte[] buf = new byte[10];
        tais.read(buf, 0, 10);
    }

    // Tests available returning zero when no entry is loaded
    @Test
    public void testAvailable_noEntry_returnsZero() throws IOException {
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        assertEquals(0, tais.available());
        tais.close();
    }

    // Tests parsePaxHeaders with valid input
    @Test
    public void testParsePaxHeaders_validHeaders_parsesCorrectly() throws IOException {
        String paxData = "25 ctime=1305284852.0\n30 gid=1000\n30 gname=test\n22 size=12345\n";
        ByteArrayInputStream bais = new ByteArrayInputStream(paxData.getBytes("UTF-8"));
        TarArchiveInputStream tais = new TarArchiveInputStream(bais);

        Map<String, String> headers = tais.parsePaxHeaders(bais);
        assertEquals("1305284852.0", headers.get("ctime"));
        assertEquals("1000", headers.get("gid"));
        assertEquals("test", headers.get("gname"));
        assertEquals("12345", headers.get("size"));
        tais.close();
    }

    // Tests parsePaxHeaders with empty stream
    @Test
    public void testParsePaxHeaders_emptyStream_returnsEmptyMap() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tais = new TarArchiveInputStream(bais);

        Map<String, String> headers = tais.parsePaxHeaders(bais);
        assertTrue(headers.isEmpty());
        tais.close();
    }

    // Tests parsePaxHeaders with truncated entry throwing IOException
    @Test(expected = IOException.class)
    public void testParsePaxHeaders_truncatedData_throwsIOException() throws IOException {
        String paxData = "50 size=12345\n";
        ByteArrayInputStream bais = new ByteArrayInputStream(paxData.getBytes("UTF-8"));
        TarArchiveInputStream tais = new TarArchiveInputStream(bais);
        tais.parsePaxHeaders(bais);
    }

    // Tests getNextTarEntry and getNextEntry on empty EOF records
    @Test
    public void testGetNextTarEntry_emptyRecord_returnsNull() throws IOException {
        byte[] emptyRecords = new byte[1024];
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(emptyRecords));

        TarArchiveEntry entry = tais.getNextTarEntry();
        assertNull(entry);
        assertTrue(tais.isAtEOF());

        ArchiveEntry archiveEntry = tais.getNextEntry();
        assertNull(archiveEntry);
        tais.close();
    }

    // Tests isEOFRecord method
    @Test
    public void testIsEOFRecord_nullOrZeroArray_returnsTrue() {
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        assertTrue(tais.isEOFRecord(null));
        assertTrue(tais.isEOFRecord(new byte[512]));

        byte[] nonZero = new byte[512];
        nonZero[0] = 'a';
        assertFalse(tais.isEOFRecord(nonZero));
    }

    // Tests canReadEntryData method
    @Test
    public void testCanReadEntryData_tarEntryVsOther_returnsExpected() {
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        TarArchiveEntry tarEntry = new TarArchiveEntry("test.txt");
        assertTrue(tais.canReadEntryData(tarEntry));

        ArchiveEntry nonTarEntry = new ArchiveEntry() {
            public String getName() { return "dummy"; }
            public long getSize() { return 0; }
            public boolean isDirectory() { return false; }
            public java.util.Date getLastModifiedDate() { return new java.util.Date(); }
        };
        assertFalse(tais.canReadEntryData(nonTarEntry));
        assertFalse(tais.canReadEntryData(null));
    }

    // Tests getter and setter for current entry and EOF flag
    @Test
    public void testGettersAndSetters_currentEntryAndEOF() {
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        assertNull(tais.getCurrentEntry());
        assertFalse(tais.isAtEOF());

        TarArchiveEntry entry = new TarArchiveEntry("file.txt");
        tais.setCurrentEntry(entry);
        assertSame(entry, tais.getCurrentEntry());

        tais.setAtEOF(true);
        assertTrue(tais.isAtEOF());
    }

    // Tests getNextTarEntry with invalid header bytes throwing IOException
    @Test(expected = IOException.class)
    public void testGetNextTarEntry_invalidHeader_throwsIOException() throws IOException {
        byte[] invalidHeader = new byte[512];
        for (int i = 0; i < 512; i++) {
            invalidHeader[i] = (byte) 0xFF;
        }
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(invalidHeader));
        tais.getNextTarEntry();
    }

    // Tests reading entries and content with TarArchiveOutputStream
    @Test
    public void testReadEntryContentAndSkip() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream taos = new TarArchiveOutputStream(baos);

        byte[] content1 = "Hello, TarArchiveInputStream!".getBytes("UTF-8");
        TarArchiveEntry entry1 = new TarArchiveEntry("file1.txt");
        entry1.setSize(content1.length);
        taos.putArchiveEntry(entry1);
        taos.write(content1);
        taos.closeArchiveEntry();

        byte[] content2 = "Second entry with more data to test skip and read fully.".getBytes("UTF-8");
        TarArchiveEntry entry2 = new TarArchiveEntry("dir/file2.txt");
        entry2.setSize(content2.length);
        taos.putArchiveEntry(entry2);
        taos.write(content2);
        taos.closeArchiveEntry();

        taos.close();

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));

        TarArchiveEntry readEntry1 = tais.getNextTarEntry();
        assertNotNull(readEntry1);
        assertEquals("file1.txt", readEntry1.getName());
        assertEquals(content1.length, readEntry1.getSize());
        assertTrue(tais.available() > 0);

        byte[] readBuf1 = new byte[content1.length];
        int numRead1 = tais.read(readBuf1, 0, readBuf1.length);
        assertEquals(content1.length, numRead1);
        assertArrayEquals(content1, readBuf1);
        assertEquals(-1, tais.read());
        assertEquals(0, tais.available());

        TarArchiveEntry readEntry2 = tais.getNextTarEntry();
        assertNotNull(readEntry2);
        assertEquals("dir/file2.txt", readEntry2.getName());

        long skipped = tais.skip(10);
        assertEquals(10, skipped);
        byte[] remainingBuf = new byte[content2.length - 10];
        int numRead2 = tais.read(remainingBuf);
        assertEquals(remainingBuf.length, numRead2);

        assertNull(tais.getNextTarEntry());
        tais.close();
    }

    // Tests GNU long name entry support
    @Test
    public void testGnuLongNameEntry() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream taos = new TarArchiveOutputStream(baos);
        taos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);

        String longFileName = "a/very/long/path/name/that/exceeds/the/standard/tar/one/hundred/characters/limit/test_long_file_name.txt";
        byte[] content = "GNU long name test".getBytes("UTF-8");
        TarArchiveEntry entry = new TarArchiveEntry(longFileName);
        entry.setSize(content.length);
        taos.putArchiveEntry(entry);
        taos.write(content);
        taos.closeArchiveEntry();
        taos.close();

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        TarArchiveEntry readEntry = tais.getNextTarEntry();
        assertNotNull(readEntry);
        assertEquals(longFileName, readEntry.getName());

        byte[] readContent = new byte[content.length];
        int bytesRead = tais.read(readContent, 0, readContent.length);
        assertEquals(content.length, bytesRead);
        assertArrayEquals(content, readContent);
        assertNull(tais.getNextTarEntry());
        tais.close();
    }

    // Tests GNU long link entry support
    @Test
    public void testGnuLongLinkEntry() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream taos = new TarArchiveOutputStream(baos);
        taos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);

        String longLinkName = "a/very/long/link/target/path/that/exceeds/the/standard/tar/one/hundred/characters/limit/target.txt";
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
        assertNull(tais.getNextTarEntry());
        tais.close();
    }

    // Tests PAX headers applied when reading archive
    @Test
    public void testPaxHeadersAppliedToEntry() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream taos = new TarArchiveOutputStream(baos);
        taos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);

        TarArchiveEntry entry = new TarArchiveEntry("pax_file.txt");
        entry.setSize(10);
        entry.setNames("user_test", "group_test");
        taos.putArchiveEntry(entry);
        taos.write(new byte[10]);
        taos.closeArchiveEntry();
        taos.close();

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        TarArchiveEntry readEntry = tais.getNextTarEntry();
        assertNotNull(readEntry);
        assertEquals("pax_file.txt", readEntry.getName());
        assertEquals("user_test", readEntry.getUserName());
        assertEquals("group_test", readEntry.getGroupName());
        tais.close();
    }

    // Tests skipping remaining content when advancing to next entry
    @Test
    public void testSkipRemainingContentOnNextEntry() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream taos = new TarArchiveOutputStream(baos);

        byte[] content1 = new byte[1000];
        TarArchiveEntry entry1 = new TarArchiveEntry("file1.bin");
        entry1.setSize(content1.length);
        taos.putArchiveEntry(entry1);
        taos.write(content1);
        taos.closeArchiveEntry();

        byte[] content2 = new byte[500];
        TarArchiveEntry entry2 = new TarArchiveEntry("file2.bin");
        entry2.setSize(content2.length);
        taos.putArchiveEntry(entry2);
        taos.write(content2);
        taos.closeArchiveEntry();
        taos.close();

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        TarArchiveEntry read1 = tais.getNextTarEntry();
        assertNotNull(read1);
        assertEquals("file1.bin", read1.getName());

        // Don't read file1.bin content, immediately get next entry
        TarArchiveEntry read2 = tais.getNextTarEntry();
        assertNotNull(read2);
        assertEquals("file2.bin", read2.getName());

        byte[] readBuf2 = new byte[500];
        int numRead = tais.read(readBuf2);
        assertEquals(500, numRead);
        tais.close();
    }
}