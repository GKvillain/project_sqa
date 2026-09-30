package org.apache.commons.compress.archivers.zip;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.Test;

/**
 * JUnit 4 test class for ZipArchiveInputStream.
 * Targets Defects4J bug 41b and provides reasonable coverage.
 */
public class ZipArchiveInputStreamTest {

    // Helper: create zip bytes for a single stored entry without data descriptor
    private byte[] createStoredZipBytes(String name, byte[] data, boolean useDataDescriptor) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(bos);
        ZipEntry entry = new ZipEntry(name);
        entry.setMethod(ZipEntry.STORED);
        if (!useDataDescriptor) {
            CRC32 crc = new CRC32();
            crc.update(data);
            entry.setCrc(crc.getValue());
            entry.setCompressedSize(data.length);
            entry.setSize(data.length);
        }
        zos.putNextEntry(entry);
        zos.write(data);
        zos.closeEntry();
        zos.close();
        return bos.toByteArray();
    }

    // Helper: create zip bytes for a single deflated entry
    private byte[] createDeflatedZipBytes(String name, byte[] data) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(bos);
        ZipEntry entry = new ZipEntry(name);
        entry.setMethod(ZipEntry.DEFLATED);
        zos.putNextEntry(entry);
        zos.write(data);
        zos.closeEntry();
        zos.close();
        return bos.toByteArray();
    }

    // Helper: create zip bytes with multiple entries
    private byte[] createMultipleZipBytes(List<ZipEntry> entries, List<byte[]> data) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(bos);
        for (int i = 0; i < entries.size(); i++) {
            ZipEntry entry = entries.get(i);
            if (entry.getMethod() == ZipEntry.STORED) {
                CRC32 crc = new CRC32();
                crc.update(data.get(i));
                entry.setCrc(crc.getValue());
                entry.setCompressedSize(data.get(i).length);
                entry.setSize(data.get(i).length);
            }
            zos.putNextEntry(entry);
            zos.write(data.get(i));
            zos.closeEntry();
        }
        zos.close();
        return bos.toByteArray();
    }

    // Helper: prepend split marker to zip data
    private byte[] prependSplitMarker(byte[] zipData) {
        byte[] marker = new byte[] {0x50, 0x4B, 0x07, 0x08};
        byte[] result = new byte[marker.length + zipData.length];
        System.arraycopy(marker, 0, result, 0, marker.length);
        System.arraycopy(zipData, 0, result, marker.length, zipData.length);
        return result;
    }

    // ========== Normal Cases ==========

    // Tests reading a stored entry without data descriptor
    @Test
    public void testGetNextZipEntry_storedEntryNoDataDescriptor_returnsCorrectEntry() throws IOException {
        byte[] data = "Hello, world!".getBytes("UTF-8");
        byte[] zipBytes = createStoredZipBytes("test.txt", data, false);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipBytes));
        ZipArchiveEntry entry = zis.getNextZipEntry();
        assertNotNull(entry);
        assertEquals("test.txt", entry.getName());
        assertEquals(data.length, entry.getSize());
        assertEquals(data.length, entry.getCompressedSize());
        // read all data
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int len;
        while ((len = zis.read(buf)) != -1) {
            baos.write(buf, 0, len);
        }
        assertArrayEquals(data, baos.toByteArray());
        CRC32 crc = new CRC32();
        crc.update(data);
        assertEquals(crc.getValue(), entry.getCrc());
        zis.close();
    }

    // Tests reading a deflated entry
    @Test
    public void testGetNextZipEntry_deflatedEntry_returnsCorrectEntry() throws IOException {
        byte[] data = "Compressed data".getBytes("UTF-8");
        byte[] zipBytes = createDeflatedZipBytes("test.txt", data);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipBytes));
        ZipArchiveEntry entry = zis.getNextZipEntry();
        assertNotNull(entry);
        assertEquals("test.txt", entry.getName());
        assertTrue(entry.getSize() > 0);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int len;
        while ((len = zis.read(buf)) != -1) {
            baos.write(buf, 0, len);
        }
        assertArrayEquals(data, baos.toByteArray());
        zis.close();
    }

    // Tests reading a stored entry with data descriptor
    @Test
    public void testGetNextZipEntry_storedEntryWithDataDescriptor_readsCorrectly() throws IOException {
        byte[] data = "Data descriptor test".getBytes("UTF-8");
        byte[] zipBytes = createStoredZipBytes("dd.txt", data, true);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipBytes));
        ZipArchiveEntry entry = zis.getNextZipEntry();
        assertNotNull(entry);
        assertEquals("dd.txt", entry.getName());
        // sizes and crc should be read from data descriptor after reading all data
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int len;
        while ((len = zis.read(buf)) != -1) {
            baos.write(buf, 0, len);
        }
        assertArrayEquals(data, baos.toByteArray());
        CRC32 crc = new CRC32();
        crc.update(data);
        assertEquals(crc.getValue(), entry.getCrc());
        assertEquals(data.length, entry.getSize());
        assertEquals(data.length, entry.getCompressedSize());
        zis.close();
    }

    // Tests reading multiple entries
    @Test
    public void testGetNextZipEntry_multipleEntries_readsAll() throws IOException {
        List<ZipEntry> entries = new java.util.ArrayList<>();
        entries.add(new ZipEntry("a.txt"));
        entries.add(new ZipEntry("b.txt"));
        List<byte[]> dataList = new java.util.ArrayList<>();
        dataList.add("AAAA".getBytes("UTF-8"));
        dataList.add("BBBB".getBytes("UTF-8"));
        for (ZipEntry e : entries) {
            e.setMethod(ZipEntry.STORED);
        }
        byte[] zipBytes = createMultipleZipBytes(entries, dataList);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipBytes));
        ZipArchiveEntry entry1 = zis.getNextZipEntry();
        assertNotNull(entry1);
        assertEquals("a.txt", entry1.getName());
        readFully(zis);
        ZipArchiveEntry entry2 = zis.getNextZipEntry();
        assertNotNull(entry2);
        assertEquals("b.txt", entry2.getName());
        readFully(zis);
        assertNull(zis.getNextZipEntry()); // no more entries
        zis.close();
    }

    // Tests empty zip (only EOCD)
    @Test
    public void testGetNextZipEntry_emptyZip_returnsNull() throws IOException {
        byte[] emptyZip = new byte[] {0x50, 0x4B, 0x05, 0x06, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(emptyZip));
        assertNull(zis.getNextZipEntry());
        zis.close();
    }

    // Tests that after reading all entries, getNextZipEntry returns null
    @Test
    public void testGetNextZipEntry_afterLastEntry_returnsNull() throws IOException {
        byte[] data = "single".getBytes("UTF-8");
        byte[] zipBytes = createStoredZipBytes("s.txt", data, false);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipBytes));
        assertNotNull(zis.getNextZipEntry());
        readFully(zis);
        assertNull(zis.getNextZipEntry());
        zis.close();
    }

    // ========== Boundary / Edge Cases ==========

    // Tests stored entry with data descriptor when allowStoredEntriesWithDataDescriptor is false
    @Test(expected = UnsupportedZipFeatureException.class)
    public void testRead_storedEntryWithDataDescriptor_allowFalse_throwsUnsupportedZipFeatureException() throws IOException {
        byte[] data = "test".getBytes("UTF-8");
        byte[] zipBytes = createStoredZipBytes("x.txt", data, true);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(
                new ByteArrayInputStream(zipBytes), "UTF-8", true, false); // disallow stored with DD
        zis.getNextZipEntry();
        byte[] buf = new byte[1024];
        zis.read(buf); // should throw because supportsDataDescriptorFor returns false
    }

    // Tests reading first local file header with split marker
    @Test
    public void testReadFirstLocalFileHeader_splitMarker_skips() throws IOException {
        byte[] data = "split".getBytes("UTF-8");
        byte[] normalZip = createStoredZipBytes("f.txt", data, false);
        byte[] withMarker = prependSplitMarker(normalZip);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(withMarker));
        ZipArchiveEntry entry = zis.getNextZipEntry();
        assertNotNull(entry);
        assertEquals("f.txt", entry.getName());
        readFully(zis);
        zis.close();
    }

    // Tests stored entry whose data contains bytes that look like LFH/CFH signatures
    @Test
    public void testRead_storedEntryDataContainsSignature_doesNotMalfunction() throws IOException {
        // Create data that includes the local file header signature (PK\x03\x04) and central directory signature (PK\x01\x02)
        byte[] data = new byte[] {0x50, 0x4B, 0x03, 0x04, 0x50, 0x4B, 0x01, 0x02, 0x41, 0x42}; // contains PK0304 and PK0102
        byte[] zipBytes = createStoredZipBytes("sigtest.txt", data, true); // use data descriptor to trigger readStoredEntry
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipBytes));
        ZipArchiveEntry entry = zis.getNextZipEntry();
        assertNotNull(entry);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int len;
        while ((len = zis.read(buf)) != -1) {
            baos.write(buf, 0, len);
        }
        assertArrayEquals(data, baos.toByteArray());
        zis.close();
    }

    // Tests truncated stored entry with data descriptor
    @Test(expected = IOException.class)
    public void testReadStoredEntry_truncated_throwsIOException() throws IOException {
        // Create a stored entry with data descriptor but truncate the zip data before the data descriptor
        byte[] data = "truncate".getBytes("UTF-8");
        byte[] fullZip = createStoredZipBytes("t.txt", data, true);
        // Remove the last few bytes to simulate truncation (cut off data descriptor)
        byte[] truncated = new byte[fullZip.length - 16]; // remove some bytes
        System.arraycopy(fullZip, 0, truncated, 0, truncated.length);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(truncated));
        zis.getNextZipEntry();
        byte[] buf = new byte[1024];
        while (zis.read(buf) != -1) { } // reading should throw IOException
    }

    // ========== Exception / Invalid Input ==========

    // Tests read on closed stream
    @Test(expected = IOException.class)
    public void testRead_closedStream_throwsIOException() throws IOException {
        byte[] data = "close".getBytes("UTF-8");
        byte[] zipBytes = createStoredZipBytes("c.txt", data, false);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipBytes));
        zis.close();
        zis.read(new byte[1]); // should throw
    }

    // Tests skip with negative value
    @Test(expected = IllegalArgumentException.class)
    public void testSkip_negativeValue_throwsIllegalArgumentException() throws IOException {
        byte[] data = "skip".getBytes("UTF-8");
        byte[] zipBytes = createStoredZipBytes("s.txt", data, false);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipBytes));
        zis.skip(-1);
    }

    // Tests read with invalid offset/length
    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testRead_invalidOffsetLength_throwsArrayIndexOutOfBoundsException() throws IOException {
        byte[] data = "bounds".getBytes("UTF-8");
        byte[] zipBytes = createStoredZipBytes("b.txt", data, false);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipBytes));
        zis.getNextZipEntry();
        zis.read(new byte[10], -1, 5); // negative offset
    }

    // Tests that getNextZipEntry returns null after hitCentralDirectory
    @Test
    public void testGetNextZipEntry_afterCentralDirectory_returnsNull() throws IOException {
        // Create a zip with two entries and then read past last entry
        List<ZipEntry> entries = new java.util.ArrayList<>();
        entries.add(new ZipEntry("first"));
        entries.add(new ZipEntry("second"));
        List<byte[]> dataList = new java.util.ArrayList<>();
        dataList.add(new byte[0]);
        dataList.add(new byte[0]);
        for (ZipEntry e : entries) {
            e.setMethod(ZipEntry.STORED);
        }
        byte[] zipBytes = createMultipleZipBytes(entries, dataList);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipBytes));
        assertNotNull(zis.getNextZipEntry());
        readFully(zis);
        assertNotNull(zis.getNextZipEntry());
        readFully(zis);
        assertNull(zis.getNextZipEntry());
        zis.close();
    }

    // ========== Branch Coverage: closeEntry after partial read ==========

    // Tests that partially reading a stored entry and then getting next entry skips properly
    @Test
    public void testCloseEntry_afterPartialRead_skipsRemaining() throws IOException {
        byte[] data1 = "1234567890".getBytes("UTF-8");
        byte[] data2 = "abcdef".getBytes("UTF-8");
        List<ZipEntry> entries = new java.util.ArrayList<>();
        entries.add(new ZipEntry("first"));
        entries.add(new ZipEntry("second"));
        List<byte[]> dataList = new java.util.ArrayList<>();
        dataList.add(data1);
        dataList.add(data2);
        for (ZipEntry e : entries) {
            e.setMethod(ZipEntry.STORED);
        }
        byte[] zipBytes = createMultipleZipBytes(entries, dataList);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipBytes));
        ZipArchiveEntry entry1 = zis.getNextZipEntry();
        assertNotNull(entry1);
        byte[] buf = new byte[5];
        int read = zis.read(buf); // read only 5 bytes of first entry
        assertEquals(5, read);
        // now getNextZipEntry will close the entry and skip remaining
        ZipArchiveEntry entry2 = zis.getNextZipEntry();
        assertNotNull(entry2);
        assertEquals("second", entry2.getName());
        readFully(zis);
        zis.close();
    }

    // ========== Helper method ==========

    private void readFully(ZipArchiveInputStream zis) throws IOException {
        byte[] buf = new byte[1024];
        while (zis.read(buf) != -1) { }
    }
}