package org.apache.commons.compress.archivers.tar;

import static org.junit.Assert.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import org.junit.Test;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.utils.ArchiveUtils;

public class TarArchiveInputStreamTest {

    // Helper to create a tar file bytes from TarArchiveEntry array
    private byte[] createTarBytes(TarArchiveEntry... entries) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(baos);
        for (TarArchiveEntry entry : entries) {
            tos.putArchiveEntry(entry);
            if (entry.getSize() > 0) {
                byte[] data = new byte[(int) entry.getSize()];
                for (int i = 0; i < data.length; i++) {
                    data[i] = (byte) (i & 0xff);
                }
                tos.write(data);
            }
            tos.closeArchiveEntry();
        }
        tos.close();
        return baos.toByteArray();
    }

    // Tests empty input stream
    @Test
    public void testGetNextTarEntry_emptyInputStream_returnsNull() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tis = new TarArchiveInputStream(bais);
        assertNull(tis.getNextTarEntry());
        tis.close();
    }

    // Tests single regular file entry
    @Test
    public void testGetNextTarEntry_singleRegularFile_returnsEntry() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.setSize(10);
        byte[] tarBytes = createTarBytes(entry);
        ByteArrayInputStream bais = new ByteArrayInputStream(tarBytes);
        TarArchiveInputStream tis = new TarArchiveInputStream(bais);
        TarArchiveEntry result = tis.getNextTarEntry();
        assertNotNull(result);
        assertEquals("test.txt", result.getName());
        assertEquals(10, result.getSize());
        assertFalse(result.isDirectory());
        assertNull(tis.getNextTarEntry());
        tis.close();
    }

    // Tests directory entry
    @Test
    public void testGetNextTarEntry_directoryEntry_returnsDirectory() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("mydir/");
        entry.setSize(0);
        byte[] tarBytes = createTarBytes(entry);
        ByteArrayInputStream bais = new ByteArrayInputStream(tarBytes);
        TarArchiveInputStream tis = new TarArchiveInputStream(bais);
        TarArchiveEntry result = tis.getNextTarEntry();
        assertNotNull(result);
        assertEquals("mydir", result.getName());
        assertTrue(result.isDirectory());
        tis.close();
    }

    // Tests multiple entries in order
    @Test
    public void testGetNextTarEntry_multipleEntries_returnsInOrder() throws IOException {
        TarArchiveEntry entry1 = new TarArchiveEntry("a.txt");
        entry1.setSize(5);
        TarArchiveEntry entry2 = new TarArchiveEntry("b.txt");
        entry2.setSize(5);
        byte[] tarBytes = createTarBytes(entry1, entry2);
        ByteArrayInputStream bais = new ByteArrayInputStream(tarBytes);
        TarArchiveInputStream tis = new TarArchiveInputStream(bais);
        assertEquals("a.txt", tis.getNextTarEntry().getName());
        assertEquals("b.txt", tis.getNextTarEntry().getName());
        assertNull(tis.getNextTarEntry());
        tis.close();
    }

    // Tests GNU long name entry (type 'L')
    @Test
    public void testGetNextTarEntry_gnuLongName_returnsEntryWithLongName() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(baos);
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 150; i++) sb.append('a');
        String longName = sb.toString();
        TarArchiveEntry entry = new TarArchiveEntry(longName);
        entry.setSize(0);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.close();
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        TarArchiveInputStream tis = new TarArchiveInputStream(bais);
        TarArchiveEntry result = tis.getNextTarEntry();
        assertNotNull(result);
        assertEquals(longName, result.getName());
        tis.close();
    }

    // Tests GNU long link entry (type 'K')
    @Test
    public void testGetNextTarEntry_gnuLongLink_returnsEntryWithLinkName() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(baos);
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 150; i++) sb.append('b');
        String longLinkTarget = sb.toString();
        TarArchiveEntry linkEntry = new TarArchiveEntry("link", TarArchiveEntry.LF_SYMLINK);
        linkEntry.setLinkName(longLinkTarget);
        tos.putArchiveEntry(linkEntry);
        tos.closeArchiveEntry();
        tos.close();
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        TarArchiveInputStream tis = new TarArchiveInputStream(bais);
        TarArchiveEntry result = tis.getNextTarEntry();
        assertNotNull(result);
        assertEquals(longLinkTarget, result.getLinkName());
        tis.close();
    }

    // Tests PAX extended header (type 'x') for long file names
    @Test
    public void testGetNextTarEntry_paxHeader_returnsEntryWithLongName() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(baos, TarConstants.DEFAULT_BLKSIZE, TarConstants.DEFAULT_RCDSIZE, null);
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 200; i++) sb.append('c');
        String longName = sb.toString();
        TarArchiveEntry entry = new TarArchiveEntry(longName);
        entry.setSize(0);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.close();
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        TarArchiveInputStream tis = new TarArchiveInputStream(bais);
        TarArchiveEntry result = tis.getNextTarEntry();
        assertNotNull(result);
        assertEquals(longName, result.getName());
        tis.close();
    }

    // Tests reading data from a normal entry
    @Test
    public void testRead_validEntry_readsDataCorrectly() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("data.bin");
        entry.setSize(20);
        byte[] data = new byte[20];
        for (int i = 0; i < 20; i++) data[i] = (byte) (i + 1);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(baos);
        tos.putArchiveEntry(entry);
        tos.write(data);
        tos.closeArchiveEntry();
        tos.close();
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        TarArchiveInputStream tis = new TarArchiveInputStream(bais);
        tis.getNextTarEntry();
        byte[] buf = new byte[20];
        int total = 0;
        while (total < 20) {
            int r = tis.read(buf, total, 20 - total);
            if (r == -1) break;
            total += r;
        }
        assertEquals(20, total);
        assertArrayEquals(data, buf);
        assertEquals(-1, tis.read(buf, 0, 1)); // no more data
        tis.close();
    }

    // Tests that reading beyond entry size returns -1
    @Test
    public void testRead_afterEntryEOF_returnsMinusOne() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("empty.txt");
        entry.setSize(0);
        byte[] tarBytes = createTarBytes(entry);
        ByteArrayInputStream bais = new ByteArrayInputStream(tarBytes);
        TarArchiveInputStream tis = new TarArchiveInputStream(bais);
        tis.getNextTarEntry();
        byte[] buf = new byte[1];
        assertEquals(-1, tis.read(buf, 0, 1));
        tis.close();
    }

    // Tests that truncated TAR archive throws IOException when reading
    @Test(expected = IOException.class)
    public void testRead_truncatedArchive_throwsIOException() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("file.bin");
        entry.setSize(10);
        byte[] fullTar = createTarBytes(entry);
        // truncate to header + 5 bytes (header is 512 bytes)
        byte[] truncated = new byte[512 + 5];
        System.arraycopy(fullTar, 0, truncated, 0, truncated.length);
        ByteArrayInputStream bais = new ByteArrayInputStream(truncated);
        TarArchiveInputStream tis = new TarArchiveInputStream(bais);
        tis.getNextTarEntry();
        byte[] buf = new byte[10];
        tis.read(buf, 0, 10); // expects IOException
    }

    // Tests read before calling getNextTarEntry throws IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testRead_beforeGetNextEntry_throwsIllegalState() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.setSize(0);
        byte[] tarBytes = createTarBytes(entry);
        ByteArrayInputStream bais = new ByteArrayInputStream(tarBytes);
        TarArchiveInputStream tis = new TarArchiveInputStream(bais);
        byte[] buf = new byte[10];
        tis.read(buf, 0, 10); // currEntry == null, not eof yet
    }

    // Tests available() returns 0 for directory entry
    @Test
    public void testAvailable_directoryEntry_returnsZero() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("dir/");
        entry.setSize(0);
        byte[] tarBytes = createTarBytes(entry);
        ByteArrayInputStream bais = new ByteArrayInputStream(tarBytes);
        TarArchiveInputStream tis = new TarArchiveInputStream(bais);
        tis.getNextTarEntry();
        assertEquals(0, tis.available());
        tis.close();
    }

    // Tests available() for regular file entry
    @Test
    public void testAvailable_regularFile_returnsRemainingBytes() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("file.bin");
        entry.setSize(100);
        byte[] tarBytes = createTarBytes(entry);
        ByteArrayInputStream bais = new ByteArrayInputStream(tarBytes);
        TarArchiveInputStream tis = new TarArchiveInputStream(bais);
        tis.getNextTarEntry();
        assertEquals(100, tis.available());
        byte[] buf = new byte[10];
        tis.read(buf);
        assertEquals(90, tis.available());
        tis.close();
    }

    // Tests skip returns 0 for directory entry
    @Test
    public void testSkip_directoryEntry_returnsZero() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("dir/");
        entry.setSize(0);
        byte[] tarBytes = createTarBytes(entry);
        ByteArrayInputStream bais = new ByteArrayInputStream(tarBytes);
        TarArchiveInputStream tis = new TarArchiveInputStream(bais);
        tis.getNextTarEntry();
        assertEquals(0, tis.skip(10));
        tis.close();
    }

    // Tests skip on regular file
    @Test
    public void testSkip_regularFile_skipsBytes() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("file.bin");
        entry.setSize(50);
        byte[] data = new byte[50];
        for (int i = 0; i < 50; i++) data[i] = (byte) i;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(baos);
        tos.putArchiveEntry(entry);
        tos.write(data);
        tos.closeArchiveEntry();
        tos.close();
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        TarArchiveInputStream tis = new TarArchiveInputStream(bais);
        tis.getNextTarEntry();
        assertEquals(20, tis.skip(20));
        assertEquals(30, tis.available());
        byte[] buf = new byte[1];
        tis.read(buf);
        assertEquals((byte) 20, buf[0]); // first byte after skip
        tis.close();
    }

    // Tests canReadEntryData returns true for regular entry, false for sparse (conceptually)
    @Test
    public void testCanReadEntryData_regularEntry_returnsTrue() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tis = new TarArchiveInputStream(bais);
        TarArchiveEntry regular = new TarArchiveEntry("file.txt");
        assertTrue(tis.canReadEntryData(regular));
        tis.close();
    }

    // Tests static matches method with valid POSIX signature
    @Test
    public void testMatches_validPosixSignature_returnsTrue() {
        byte[] sig = new byte[TarConstants.VERSION_OFFSET + TarConstants.VERSIONLEN];
        System.arraycopy(ArchiveUtils.toAsciiBytes(TarConstants.MAGIC_POSIX), 0, sig, TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN);
        System.arraycopy(ArchiveUtils.toAsciiBytes(TarConstants.VERSION_POSIX), 0, sig, TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN);
        assertTrue(TarArchiveInputStream.matches(sig, sig.length));
    }

    // Tests static matches method with invalid signature
    @Test
    public void testMatches_invalidSignature_returnsFalse() {
        byte[] sig = new byte[10];
        assertFalse(TarArchiveInputStream.matches(sig, 10));
    }

    // Tests getRecordSize returns default record size
    @Test
    public void testGetRecordSize_defaultRecordSize() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tis = new TarArchiveInputStream(bais);
        assertEquals(TarConstants.DEFAULT_RCDSIZE, tis.getRecordSize());
        tis.close();
    }

    // Tests close closes underlying stream
    @Test
    public void testClose_closesUnderlyingStream() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tis = new TarArchiveInputStream(bais);
        tis.close();
        try {
            bais.read();
            fail("Expected IOException after close");
        } catch (IOException e) {
            // expected
        }
    }

    // Tests that two EOF records are consumed correctly
    @Test
    public void testGetNextTarEntry_twoEOFRecords_returnsNullAfterEntry() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("file.txt");
        entry.setSize(0);
        byte[] tarBytes = createTarBytes(entry);
        byte[] zeros = new byte[512];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(tarBytes);
        baos.write(zeros);
        baos.write(zeros);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        TarArchiveInputStream tis = new TarArchiveInputStream(bais);
        assertNotNull(tis.getNextTarEntry());
        assertNull(tis.getNextTarEntry());
        tis.close();
    }

    // Tests skipRecordPadding when entry size not multiple of record size
    @Test
    public void testGetNextTarEntry_withPadding_skipsPadding() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("file.bin");
        entry.setSize(1);
        byte[] data = { 0x42 };
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(baos);
        tos.putArchiveEntry(entry);
        tos.write(data);
        tos.closeArchiveEntry();
        tos.close();
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        TarArchiveInputStream tis = new TarArchiveInputStream(bais);
        TarArchiveEntry result = tis.getNextTarEntry();
        assertNotNull(result);
        byte[] buf = new byte[1];
        assertEquals(1, tis.read(buf));
        assertEquals(0x42, buf[0]);
        assertNull(tis.getNextTarEntry()); // next entry null after padding
        tis.close();
    }
}