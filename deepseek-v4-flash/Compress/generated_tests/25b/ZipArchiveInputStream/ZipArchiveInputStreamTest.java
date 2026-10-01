package org.apache.commons.compress.archivers.zip;

import static org.junit.Assert.*;
import org.junit.Test;
import java.io.*;
import java.util.zip.*;

/**
 * JUnit 4 test class for ZipArchiveInputStream.
 * Designed to detect the Defects4J bug 25b, which relates to
 * incorrect handling of stored entries with data descriptors.
 */
public class ZipArchiveInputStreamTest {

    // ========== Helper methods to create zip data ==========

    /**
     * Creates a zip archive with a single stored entry.
     * The entry uses a data descriptor if useDataDescriptor is true.
     */
    private byte[] createZipWithStoredEntry(String name, byte[] content, boolean useDataDescriptor) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos)) {
            ZipArchiveEntry entry = new ZipArchiveEntry(name);
            entry.setMethod(ZipEntry.STORED);
            if (!useDataDescriptor) {
                // set size/crc to avoid data descriptor
                CRC32 crc = new CRC32();
                crc.update(content);
                entry.setSize(content.length);
                entry.setCompressedSize(content.length);
                entry.setCrc(crc.getValue());
            } else {
                // set method only; data descriptor will be written on close
                GeneralPurposeBit gp = new GeneralPurposeBit();
                gp.useDataDescriptor(true);
                entry.setGeneralPurposeBit(gp);
            }
            zos.putArchiveEntry(entry);
            if (content.length > 0) {
                zos.write(content);
            }
            zos.closeArchiveEntry();
        }
        return baos.toByteArray();
    }

    /**
     * Creates a zip archive with a single deflated entry.
     */
    private byte[] createZipWithDeflatedEntry(String name, byte[] content) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos)) {
            ZipArchiveEntry entry = new ZipArchiveEntry(name);
            entry.setMethod(ZipEntry.DEFLATED);
            zos.putArchiveEntry(entry);
            if (content.length > 0) {
                zos.write(content);
            }
            zos.closeArchiveEntry();
        }
        return baos.toByteArray();
    }

    /**
     * Creates an empty zip archive (only end of central directory).
     */
    private byte[] createEmptyZip() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos)) {
            // no entries
        }
        return baos.toByteArray();
    }

    /**
     * Creates a zip archive with a single stored entry, with data descriptor,
     * but also with known sizes set in the local file header.
     */
    private byte[] createZipWithStoredEntryAndKnownSizes(String name, byte[] content) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos)) {
            ZipArchiveEntry entry = new ZipArchiveEntry(name);
            entry.setMethod(ZipEntry.STORED);
            // set size/crc and also enable data descriptor
            CRC32 crc = new CRC32();
            crc.update(content);
            entry.setSize(content.length);
            entry.setCompressedSize(content.length);
            entry.setCrc(crc.getValue());
            GeneralPurposeBit gp = new GeneralPurposeBit();
            gp.useDataDescriptor(true);
            entry.setGeneralPurposeBit(gp);
            zos.putArchiveEntry(entry);
            if (content.length > 0) {
                zos.write(content);
            }
            zos.closeArchiveEntry();
        }
        return baos.toByteArray();
    }

    /**
     * Creates a zip archive with a single stored entry that has a Unicode name (UTF-8 flag set).
     */
    private byte[] createZipWithUnicodeName(String name, byte[] content) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos)) {
            ZipArchiveEntry entry = new ZipArchiveEntry(name);
            entry.setMethod(ZipEntry.STORED);
            CRC32 crc = new CRC32();
            crc.update(content);
            entry.setSize(content.length);
            entry.setCompressedSize(content.length);
            entry.setCrc(crc.getValue());
            // set UTF-8 flag
            GeneralPurposeBit gp = new GeneralPurposeBit();
            gp.useUTF8ForNames(true);
            entry.setGeneralPurposeBit(gp);
            zos.putArchiveEntry(entry);
            zos.write(content);
            zos.closeArchiveEntry();
        }
        return baos.toByteArray();
    }

    /**
     * Creates a zip archive with a single stored entry that has an extra field.
     */
    private byte[] createZipWithExtraField(String name, byte[] content) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos)) {
            ZipArchiveEntry entry = new ZipArchiveEntry(name);
            entry.setMethod(ZipEntry.STORED);
            CRC32 crc = new CRC32();
            crc.update(content);
            entry.setSize(content.length);
            entry.setCompressedSize(content.length);
            entry.setCrc(crc.getValue());
            // add a dummy extra field
            entry.addExtraField(new UnrecognizedExtraField() {{
                setHeaderId((short) 0x0017);
                setLocalFileDataData(new byte[] {0x01, 0x02});
                setCentralDirectoryData(new byte[] {0x01, 0x02});
            }});
            zos.putArchiveEntry(entry);
            zos.write(content);
            zos.closeArchiveEntry();
        }
        return baos.toByteArray();
    }

    /**
     * Creates a zip archive with multiple entries: stored, deflated, stored with data descriptor.
     */
    private byte[] createZipWithMultipleMixedEntries() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos)) {
            // first entry: stored, no DD
            byte[] content1 = "First".getBytes("UTF-8");
            ZipArchiveEntry e1 = new ZipArchiveEntry("first.txt");
            e1.setMethod(ZipEntry.STORED);
            CRC32 crc1 = new CRC32();
            crc1.update(content1);
            e1.setSize(content1.length);
            e1.setCompressedSize(content1.length);
            e1.setCrc(crc1.getValue());
            zos.putArchiveEntry(e1);
            zos.write(content1);
            zos.closeArchiveEntry();

            // second entry: deflated
            byte[] content2 = "Second".getBytes("UTF-8");
            ZipArchiveEntry e2 = new ZipArchiveEntry("second.txt");
            e2.setMethod(ZipEntry.DEFLATED);
            zos.putArchiveEntry(e2);
            zos.write(content2);
            zos.closeArchiveEntry();

            // third entry: stored with data descriptor
            byte[] content3 = "Third".getBytes("UTF-8");
            ZipArchiveEntry e3 = new ZipArchiveEntry("third.txt");
            e3.setMethod(ZipEntry.STORED);
            GeneralPurposeBit gp = new GeneralPurposeBit();
            gp.useDataDescriptor(true);
            e3.setGeneralPurposeBit(gp);
            zos.putArchiveEntry(e3);
            zos.write(content3);
            zos.closeArchiveEntry();
        }
        return baos.toByteArray();
    }

    /**
     * Creates a zip archive with a global comment.
     */
    private byte[] createZipWithComment(String commentStr) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos)) {
            zos.setComment(commentStr);
            // add one empty stored entry so zip is not empty
            ZipArchiveEntry entry = new ZipArchiveEntry("dummy.txt");
            entry.setMethod(ZipEntry.STORED);
            entry.setSize(0);
            entry.setCompressedSize(0);
            CRC32 crc = new CRC32();
            entry.setCrc(crc.getValue());
            zos.putArchiveEntry(entry);
            zos.closeArchiveEntry();
        }
        return baos.toByteArray();
    }

    // ========== Original test cases (unchanged) ==========

    // Test normal stored entry without data descriptor
    @Test
    public void testGetNextEntry_normalStoredEntry_returnsEntry() throws IOException {
        byte[] data = "Hello world".getBytes("UTF-8");
        byte[] zip = createZipWithStoredEntry("test.txt", data, false);
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zip))) {
            ZipArchiveEntry entry = zis.getNextZipEntry();
            assertNotNull(entry);
            assertEquals("test.txt", entry.getName());
            assertEquals(ZipEntry.STORED, entry.getMethod());
            assertEquals(data.length, entry.getSize());
            assertFalse(entry.getGeneralPurposeBit().usesDataDescriptor());
            // read content to verify
            byte[] readContent = new byte[data.length];
            int totalRead = 0;
            while (totalRead < data.length) {
                int n = zis.read(readContent, totalRead, data.length - totalRead);
                if (n == -1) break;
                totalRead += n;
            }
            assertEquals(data.length, totalRead);
            assertArrayEquals(data, readContent);
            // after reading, next entry should be null
            assertNull(zis.getNextZipEntry());
        }
    }

    // Test deflated entry
    @Test
    public void testGetNextEntry_deflatedEntry_returnsEntry() throws IOException {
        byte[] data = "Compressed content".getBytes("UTF-8");
        byte[] zip = createZipWithDeflatedEntry("def.txt", data);
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zip))) {
            ZipArchiveEntry entry = zis.getNextZipEntry();
            assertNotNull(entry);
            assertEquals("def.txt", entry.getName());
            assertEquals(ZipEntry.DEFLATED, entry.getMethod());
            // read content
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int n;
            while ((n = zis.read(buf, 0, buf.length)) != -1) {
                baos.write(buf, 0, n);
            }
            assertArrayEquals(data, baos.toByteArray());
            // verify CRC if available? Not needed.
        }
    }

    // Test stored entry with data descriptor (critical for bug 25b)
    @Test
    public void testGetNextEntry_storedEntryWithDataDescriptor_returnsEntry() throws IOException {
        byte[] data = "Data descriptor test".getBytes("UTF-8");
        byte[] zip = createZipWithStoredEntry("dd.txt", data, true);
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(
                new ByteArrayInputStream(zip),
                null,
                true,
                true)) { // allowStoredEntriesWithDataDescriptor = true
            ZipArchiveEntry entry = zis.getNextZipEntry();
            assertNotNull(entry);
            assertEquals("dd.txt", entry.getName());
            assertEquals(ZipEntry.STORED, entry.getMethod());
            // read content
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int n;
            while ((n = zis.read(buf, 0, buf.length)) != -1) {
                baos.write(buf, 0, n);
            }
            assertArrayEquals(data, baos.toByteArray());
            // next entry should be null
            assertNull(zis.getNextZipEntry());
        }
    }

    // Test read for stored entry without data descriptor
    @Test
    public void testRead_storedEntry_readContent() throws IOException {
        byte[] data = "Read test".getBytes("UTF-8");
        byte[] zip = createZipWithStoredEntry("read.txt", data, false);
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zip))) {
            ZipArchiveEntry entry = zis.getNextZipEntry();
            assertNotNull(entry);
            byte[] buffer = new byte[data.length];
            int bytesRead = zis.read(buffer, 0, buffer.length);
            assertEquals(data.length, bytesRead);
            assertArrayEquals(data, buffer);
        }
    }

    // Test read for deflated entry
    @Test
    public void testRead_deflatedEntry_readContent() throws IOException {
        byte[] data = "Read deflated".getBytes("UTF-8");
        byte[] zip = createZipWithDeflatedEntry("defread.txt", data);
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zip))) {
            ZipArchiveEntry entry = zis.getNextZipEntry();
            assertNotNull(entry);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int n;
            while ((n = zis.read(buf, 0, buf.length)) != -1) {
                baos.write(buf, 0, n);
            }
            assertArrayEquals(data, baos.toByteArray());
        }
    }

    // Test read for stored entry with data descriptor
    @Test
    public void testRead_storedEntryWithDataDescriptor_readContent() throws IOException {
        byte[] data = "Read DD".getBytes("UTF-8");
        byte[] zip = createZipWithStoredEntry("ddread.txt", data, true);
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(
                new ByteArrayInputStream(zip),
                null,
                true,
                true)) {
            ZipArchiveEntry entry = zis.getNextZipEntry();
            assertNotNull(entry);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int n;
            while ((n = zis.read(buf, 0, buf.length)) != -1) {
                baos.write(buf, 0, n);
            }
            assertArrayEquals(data, baos.toByteArray());
        }
    }

    // Test that reading a stored entry with data descriptor throws exception when flag is false
    @Test(expected = UnsupportedZipFeatureException.class)
    public void testRead_storedEntryWithDataDescriptorAndFlagFalse_throwsUnsupported() throws IOException {
        byte[] data = "DD not allowed".getBytes("UTF-8");
        byte[] zip = createZipWithStoredEntry("notallowed.txt", data, true);
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(
                new ByteArrayInputStream(zip),
                null,
                true,
                false)) { // allowStoredEntriesWithDataDescriptor = false
            ZipArchiveEntry entry = zis.getNextZipEntry();
            assertNotNull(entry);
            // reading should throw UnsupportedZipFeatureException
            byte[] buf = new byte[1024];
            zis.read(buf, 0, buf.length);
        }
    }

    // Test read on closed stream throws IOException
    @Test(expected = IOException.class)
    public void testRead_closedStream_throwsIOException() throws IOException {
        byte[] zip = createZipWithStoredEntry("close.txt", new byte[0], false);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zip));
        zis.close();
        zis.read(new byte[10], 0, 10);
    }

    // Test invalid offset/length in read throws ArrayIndexOutOfBoundsException
    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testRead_invalidOffset_throwsArrayIndexOutOfBoundsException() throws IOException {
        byte[] data = "valid".getBytes("UTF-8");
        byte[] zip = createZipWithStoredEntry("test.txt", data, false);
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zip))) {
            zis.getNextZipEntry();
            zis.read(new byte[10], -1, 5); // negative offset
        }
    }

    // Test that getNextZipEntry returns null after all entries read
    @Test
    public void testGetNextZipEntry_afterEntries_returnsNull() throws IOException {
        byte[] data = "entry".getBytes("UTF-8");
        // Create zip with two entries (stored)
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos)) {
            for (int i = 0; i < 2; i++) {
                ZipArchiveEntry entry = new ZipArchiveEntry("entry" + i + ".txt");
                entry.setMethod(ZipEntry.STORED);
                entry.setSize(data.length);
                entry.setCompressedSize(data.length);
                CRC32 crc = new CRC32();
                crc.update(data);
                entry.setCrc(crc.getValue());
                zos.putArchiveEntry(entry);
                zos.write(data);
                zos.closeArchiveEntry();
            }
        }
        byte[] zip = baos.toByteArray();
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zip))) {
            assertNotNull(zis.getNextZipEntry());
            // read all data to move to next entry? Actually getNextZipEntry handles close automatically
            assertNotNull(zis.getNextZipEntry());
            // consume second entry
            byte[] buf = new byte[1024];
            while (zis.read(buf, 0, buf.length) != -1) { }
            assertNull(zis.getNextZipEntry());
        }
    }

    // Test skip on stored entry
    @Test
    public void testSkip_storedEntry_skipsCorrectly() throws IOException {
        byte[] data = "1234567890".getBytes("UTF-8");
        byte[] zip = createZipWithStoredEntry("skip.txt", data, false);
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zip))) {
            ZipArchiveEntry entry = zis.getNextZipEntry();
            assertNotNull(entry);
            long skipped = zis.skip(5);
            assertEquals(5, skipped);
            byte[] remaining = new byte[5];
            int n = zis.read(remaining, 0, 5);
            assertEquals(5, n);
            assertArrayEquals("67890".getBytes("UTF-8"), remaining);
        }
    }

    // Test close twice does not throw
    @Test
    public void testClose_twice_noException() throws IOException {
        byte[] zip = createEmptyZip();
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zip));
        zis.close();
        zis.close(); // should not throw
    }

    // Test getNextZipEntry on empty zip returns null
    @Test
    public void testGetNextZipEntry_emptyZip_returnsNull() throws IOException {
        byte[] zip = createEmptyZip();
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zip))) {
            assertNull(zis.getNextZipEntry());
        }
    }

    // Test reading a zero-length stored entry
    @Test
    public void testRead_storedEntry_zeroLength_readMinusOne() throws IOException {
        byte[] zip = createZipWithStoredEntry("empty.txt", new byte[0], false);
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zip))) {
            ZipArchiveEntry entry = zis.getNextZipEntry();
            assertNotNull(entry);
            assertEquals(0, entry.getSize());
            int read = zis.read(new byte[10], 0, 10);
            assertEquals(-1, read);
        }
    }

    // Test reading a zero-length deflated entry (may have small compressed size)
    @Test
    public void testRead_deflatedEntry_zeroLength_readMinusOne() throws IOException {
        byte[] zip = createZipWithDeflatedEntry("emptydef.txt", new byte[0]);
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zip))) {
            ZipArchiveEntry entry = zis.getNextZipEntry();
            assertNotNull(entry);
            int read = zis.read(new byte[10], 0, 10);
            assertEquals(-1, read);
        }
    }

    // ========== New test cases to improve coverage ==========

    // Test stored entry with data descriptor AND known sizes in local header
    @Test
    public void testGetNextZipEntry_storedEntryWithDataDescriptorAndKnownSize_returnsEntry() throws IOException {
        byte[] data = "KnownSize".getBytes("UTF-8");
        byte[] zip = createZipWithStoredEntryAndKnownSizes("knowndd.txt", data);
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(
                new ByteArrayInputStream(zip),
                null,
                true,
                true)) {
            ZipArchiveEntry entry = zis.getNextZipEntry();
            assertNotNull(entry);
            assertEquals("knowndd.txt", entry.getName());
            assertEquals(ZipEntry.STORED, entry.getMethod());
            // read content
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int n;
            while ((n = zis.read(buf, 0, buf.length)) != -1) {
                baos.write(buf, 0, n);
            }
            assertArrayEquals(data, baos.toByteArray());
            assertNull(zis.getNextZipEntry());
        }
    }

    // Test reading a stored entry with unicode name (UTF-8 flag)
    @Test
    public void testGetNextZipEntry_unicodeName_returnsEntry() throws IOException {
        String unicodeName = "文件.txt"; // Chinese characters
        byte[] data = "Unicode".getBytes("UTF-8");
        byte[] zip = createZipWithUnicodeName(unicodeName, data);
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(
                new ByteArrayInputStream(zip),
                null,
                true,
                true)) {
            ZipArchiveEntry entry = zis.getNextZipEntry();
            assertNotNull(entry);
            assertEquals(unicodeName, entry.getName());
            // read and verify content
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int n;
            while ((n = zis.read(buf, 0, buf.length)) != -1) {
                baos.write(buf, 0, n);
            }
            assertArrayEquals(data, baos.toByteArray());
        }
    }

    // Test stored entry with extra fields present
    @Test
    public void testGetNextZipEntry_extraField_present() throws IOException {
        byte[] data = "ExtraFieldTest".getBytes("UTF-8");
        byte[] zip = createZipWithExtraField("extra.txt", data);
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(
                new ByteArrayInputStream(zip),
                null,
                true,
                true)) {
            ZipArchiveEntry entry = zis.getNextZipEntry();
            assertNotNull(entry);
            assertEquals("extra.txt", entry.getName());
            // verify that extra fields exist (we added one)
            assertTrue(entry.getExtraFields().length > 0);
            // consume data
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int n;
            while ((n = zis.read(buf, 0, buf.length)) != -1) {
                baos.write(buf, 0, n);
            }
            assertArrayEquals(data, baos.toByteArray());
        }
    }

    // Test getNextZipEntry when there are multiple entries with different methods
    @Test
    public void testGetNextZipEntry_multipleEntriesMixedMethods() throws IOException {
        byte[] zip = createZipWithMultipleMixedEntries();
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(
                new ByteArrayInputStream(zip),
                null,
                true,
                true)) {
            // first entry: stored, no DD
            ZipArchiveEntry e1 = zis.getNextZipEntry();
            assertNotNull(e1);
            assertEquals("first.txt", e1.getName());
            assertEquals(ZipEntry.STORED, e1.getMethod());
            // read all of first entry
            byte[] buf1 = new byte[1024];
            ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
            int n;
            while ((n = zis.read(buf1, 0, buf1.length)) != -1) {
                baos1.write(buf1, 0, n);
            }
            assertArrayEquals("First".getBytes("UTF-8"), baos1.toByteArray());

            // second entry: deflated
            ZipArchiveEntry e2 = zis.getNextZipEntry();
            assertNotNull(e2);
            assertEquals("second.txt", e2.getName());
            assertEquals(ZipEntry.DEFLATED, e2.getMethod());
            ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
            while ((n = zis.read(buf1, 0, buf1.length)) != -1) {
                baos2.write(buf1, 0, n);
            }
            assertArrayEquals("Second".getBytes("UTF-8"), baos2.toByteArray());

            // third entry: stored with data descriptor
            ZipArchiveEntry e3 = zis.getNextZipEntry();
            assertNotNull(e3);
            assertEquals("third.txt", e3.getName());
            assertEquals(ZipEntry.STORED, e3.getMethod());
            ByteArrayOutputStream baos3 = new ByteArrayOutputStream();
            while ((n = zis.read(buf1, 0, buf1.length)) != -1) {
                baos3.write(buf1, 0, n);
            }
            assertArrayEquals("Third".getBytes("UTF-8"), baos3.toByteArray());

            // no more entries
            assertNull(zis.getNextZipEntry());
        }
    }

    // Test skip beyond available data in a stored entry
    @Test
    public void testRead_storedEntry_skipMoreThanSize() throws IOException {
        byte[] data = "12345".getBytes("UTF-8");
        byte[] zip = createZipWithStoredEntry("skipmore.txt", data, false);
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zip))) {
            zis.getNextZipEntry();
            long skipped = zis.skip(100); // more than size
            assertEquals(data.length, skipped); // should skip only available bytes
            // subsequent read should return -1
            int read = zis.read(new byte[10]);
            assertEquals(-1, read);
        }
    }

    // Test read with zero-length buffer (should return 0, not -1, when entry has data)
    @Test
    public void testRead_zeroLengthBuffer_returnsZero() throws IOException {
        byte[] data = "nonempty".getBytes("UTF-8");
        byte[] zip = createZipWithStoredEntry("zerobuf.txt", data, false);
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zip))) {
            zis.getNextZipEntry();
            int result = zis.read(new byte[0], 0, 0);
            assertEquals(0, result);
        }
    }

    // Test getNextZipEntry without reading previous entry completely (should auto-close)
    @Test
    public void testGetNextZipEntry_skipToNextEntryWithoutReading() throws IOException {
        byte[] data = "entry1entry2".getBytes("UTF-8");
        // Create zip with two stored entries
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos)) {
            // first entry
            byte[] data1 = "first".getBytes("UTF-8");
            ZipArchiveEntry e1 = new ZipArchiveEntry("e1.txt");
            e1.setMethod(ZipEntry.STORED);
            e1.setSize(data1.length);
            e1.setCompressedSize(data1.length);
            CRC32 crc1 = new CRC32();
            crc1.update(data1);
            e1.setCrc(crc1.getValue());
            zos.putArchiveEntry(e1);
            zos.write(data1);
            zos.closeArchiveEntry();

            // second entry
            byte[] data2 = "second".getBytes("UTF-8");
            ZipArchiveEntry e2 = new ZipArchiveEntry("e2.txt");
            e2.setMethod(ZipEntry.STORED);
            e2.setSize(data2.length);
            e2.setCompressedSize(data2.length);
            CRC32 crc2 = new CRC32();
            crc2.update(data2);
            e2.setCrc(crc2.getValue());
            zos.putArchiveEntry(e2);
            zos.write(data2);
            zos.closeArchiveEntry();
        }
        byte[] zip = baos.toByteArray();
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zip))) {
            ZipArchiveEntry e1 = zis.getNextZipEntry();
            assertNotNull(e1);
            assertEquals("e1.txt", e1.getName());
            // do NOT read e1 completely; skip directly to next entry
            ZipArchiveEntry e2 = zis.getNextZipEntry();
            assertNotNull(e2);
            assertEquals("e2.txt", e2.getName());
            // now read e2 content
            byte[] readData = new byte["second".length()];
            int total = 0;
            while (total < readData.length) {
                int n = zis.read(readData, total, readData.length - total);
                if (n == -1) break;
                total += n;
            }
            assertArrayEquals("second".getBytes("UTF-8"), readData);
        }
    }

    // Test reading a truncated zip (should throw an exception)
    @Test(expected = IOException.class)
    public void testGetNextZipEntry_truncatedZip_throwsException() throws IOException {
        byte[] data = "content".getBytes("UTF-8");
        byte[] zip = createZipWithStoredEntry("trunc.txt", data, false);
        // Truncate the zip data: remove part of the entry data
        byte[] truncated = new byte[zip.length - 10];
        System.arraycopy(zip, 0, truncated, 0, truncated.length);
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(truncated))) {
            ZipArchiveEntry entry = zis.getNextZipEntry();
            assertNotNull(entry);
            // reading should throw because data is truncated
            byte[] buf = new byte[1024];
            while (zis.read(buf, 0, buf.length) != -1) { }
        }
    }

    // Test zip with a global comment
    @Test
    public void testGetNextZipEntry_zipWithComment_returnsEntries() throws IOException {
        String comment = "This is a test comment";
        byte[] zip = createZipWithComment(comment);
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zip))) {
            ZipArchiveEntry entry = zis.getNextZipEntry();
            assertNotNull(entry);
            assertEquals("dummy.txt", entry.getName());
            // read and verify content (empty)
            int read = zis.read(new byte[10]);
            assertEquals(-1, read);
            assertNull(zis.getNextZipEntry());
        }
    }

    // Test constructor with explicit encoding UTF-8
    @Test
    public void testConstructor_encoding_utf8() throws IOException {
        byte[] data = "UTF8".getBytes("UTF-8");
        byte[] zip = createZipWithStoredEntry("utf8.txt", data, false);
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(
                new ByteArrayInputStream(zip),
                "UTF-8",
                true,
                true)) {
            ZipArchiveEntry entry = zis.getNextZipEntry();
            assertNotNull(entry);
            assertEquals("utf8.txt", entry.getName());
            byte[] readData = new byte[data.length];
            int total = 0;
            while (total < data.length) {
                int n = zis.read(readData, total, data.length - total);
                if (n == -1) break;
                total += n;
            }
            assertArrayEquals(data, readData);
        }
    }

    // Test constructor with explicit encoding CP437
    @Test
    public void testConstructor_encoding_cp437() throws IOException {
        byte[] data = "CP437".getBytes("UTF-8");
        byte[] zip = createZipWithStoredEntry("cp437.txt", data, false);
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(
                new ByteArrayInputStream(zip),
                "CP437",
                true,
                true)) {
            ZipArchiveEntry entry = zis.getNextZipEntry();
            assertNotNull(entry);
            assertEquals("cp437.txt", entry.getName());
            byte[] readData = new byte[data.length];
            int total = 0;
            while (total < data.length) {
                int n = zis.read(readData, total, data.length - total);
                if (n == -1) break;
                total += n;
            }
            assertArrayEquals(data, readData);
        }
    }
}