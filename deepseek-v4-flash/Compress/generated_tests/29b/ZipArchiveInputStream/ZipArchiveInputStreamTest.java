package org.apache.commons.compress.archivers.zip;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.CRC32;

import org.junit.Test;

public class ZipArchiveInputStreamTest {

    @Test
    public void testRead_DeflatedEntry_returnsContent() throws Exception {
        byte[] content = "Hello World".getBytes("UTF-8");
        ZipArchiveInputStream zin = new ZipArchiveInputStream(
            new ByteArrayInputStream(createZip("deflated.txt", content, ZipArchiveOutputStream.DEFLATED, false)));
        ZipArchiveEntry entry = zin.getNextZipEntry();
        assertNotNull(entry);
        assertEquals("deflated.txt", entry.getName());
        assertArrayEquals(content, readAll(zin));
        zin.close();
    }

    @Test
    public void testRead_StoredEntry_returnsContent() throws Exception {
        byte[] content = "Stored content".getBytes("UTF-8");
        ZipArchiveInputStream zin = new ZipArchiveInputStream(
            new ByteArrayInputStream(createZip("stored.txt", content, ZipArchiveOutputStream.STORED, false)));
        ZipArchiveEntry entry = zin.getNextZipEntry();
        assertNotNull(entry);
        assertEquals("stored.txt", entry.getName());
        assertArrayEquals(content, readAll(zin));
        zin.close();
    }

    @Test
    public void testGetNextZipEntry_multipleEntries_returnsAllEntries() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(bos);
        addZipEntry(zos, "one.txt", "One".getBytes("UTF-8"), ZipArchiveOutputStream.STORED);
        addZipEntry(zos, "two.txt", "Two".getBytes("UTF-8"), ZipArchiveOutputStream.STORED);
        zos.close();

        ZipArchiveInputStream zin = new ZipArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
        ZipArchiveEntry e1 = zin.getNextZipEntry();
        ZipArchiveEntry e2 = zin.getNextZipEntry();
        assertNotNull(e1);
        assertEquals("one.txt", e1.getName());
        assertNotNull(e2);
        assertEquals("two.txt", e2.getName());
        assertNull(zin.getNextZipEntry());
        zin.close();
    }

    @Test
    public void testGetNextZipEntry_emptyArchive_returnsNull() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(bos);
        zos.close();

        ZipArchiveInputStream zin = new ZipArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
        assertNull(zin.getNextZipEntry());
        zin.close();
    }

    @Test
    public void testClose_closesStreamAndRead_throwsIOException() throws Exception {
        ZipArchiveInputStream zin = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        zin.close();
        try {
            zin.read(new byte[10]);
            fail("Expected IOException");
        } catch (IOException e) {
            // expected
        }
    }

    @Test
    public void testSkip_negativeValue_throwsIllegalArgumentException() throws Exception {
        ZipArchiveInputStream zin = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        try {
            zin.skip(-1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        zin.close();
    }

    @Test
    public void testMatches_validSignature_returnsTrue() {
        byte[] sig = new byte[] {0x50, 0x4b, 0x03, 0x04};
        assertTrue(ZipArchiveInputStream.matches(sig, 4));
    }

    @Test
    public void testMatches_invalidSignature_returnsFalse() {
        byte[] sig = new byte[] {0x50, 0x4b, 0x03, 0x05};
        assertFalse(ZipArchiveInputStream.matches(sig, 4));
        assertFalse(ZipArchiveInputStream.matches(new byte[] {0x50}, 1));
    }

    @Test
    public void testRead_invalidBufferArguments_throwsArrayIndexOutOfBoundsException() throws Exception {
        ZipArchiveInputStream zin = new ZipArchiveInputStream(
            new ByteArrayInputStream(createZip("x", "data".getBytes("UTF-8"), ZipArchiveOutputStream.STORED, false)));
        assertNotNull(zin.getNextZipEntry());
        try {
            zin.read(new byte[10], 5, 10);
            fail("Expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }
        zin.close();
    }

    @Test
    public void testRead_storedEntryWithDataDescriptor_allowed_readsContent() throws Exception {
        byte[] content = "Stored with DD".getBytes("UTF-8");
        byte[] zip = createStoredWithDataDescriptor(content);
        ZipArchiveInputStream zin = new ZipArchiveInputStream(
            new ByteArrayInputStream(zip), "UTF-8", true, true);
        ZipArchiveEntry entry = zin.getNextZipEntry();
        assertNotNull(entry);
        assertArrayEquals(content, readAll(zin));
        zin.close();
    }

    @Test
    public void testRead_storedEntryWithDataDescriptor_notAllowed_throwsUnsupportedZipFeatureException() throws Exception {
        byte[] content = "Stored with DD not allowed".getBytes("UTF-8");
        byte[] zip = createStoredWithDataDescriptor(content);
        ZipArchiveInputStream zin = new ZipArchiveInputStream(new ByteArrayInputStream(zip));
        assertNotNull(zin.getNextZipEntry());
        try {
            zin.read(new byte[1024]);
            fail("Expected UnsupportedZipFeatureException");
        } catch (UnsupportedZipFeatureException e) {
            // expected
        }
        zin.close();
    }

    // ========== New test cases to improve coverage ==========

    @Test
    public void testRead_DeflatedEntryWithDataDescriptor_notAllowed_stillReads() throws Exception {
        // Deflated entries always have data descriptors when sizes are unknown.
        // Even with default allowStoredEntriesWithDataDescriptor=false, deflated entries should be readable.
        byte[] content = "Deflated with DD".getBytes("UTF-8");
        byte[] zip = createZip("deflated.txt", content, ZipArchiveOutputStream.DEFLATED, false);
        ZipArchiveInputStream zin = new ZipArchiveInputStream(new ByteArrayInputStream(zip));
        ZipArchiveEntry entry = zin.getNextZipEntry();
        assertNotNull(entry);
        assertEquals(ZipArchiveOutputStream.DEFLATED, entry.getMethod());
        assertArrayEquals(content, readAll(zin));
        zin.close();
    }

    @Test
    public void testRead_MultipleEntriesMixedMethods() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(bos);
        addZipEntry(zos, "stored1.txt", "Stored one".getBytes("UTF-8"), ZipArchiveOutputStream.STORED);
        addZipEntry(zos, "deflated1.txt", "Deflated one".getBytes("UTF-8"), ZipArchiveOutputStream.DEFLATED);
        addZipEntry(zos, "stored2.txt", "Stored two".getBytes("UTF-8"), ZipArchiveOutputStream.STORED);
        zos.close();

        ZipArchiveInputStream zin = new ZipArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
        ZipArchiveEntry e1 = zin.getNextZipEntry();
        assertNotNull(e1);
        assertEquals("stored1.txt", e1.getName());
        assertArrayEquals("Stored one".getBytes("UTF-8"), readAll(zin));

        ZipArchiveEntry e2 = zin.getNextZipEntry();
        assertNotNull(e2);
        assertEquals("deflated1.txt", e2.getName());
        assertArrayEquals("Deflated one".getBytes("UTF-8"), readAll(zin));

        ZipArchiveEntry e3 = zin.getNextZipEntry();
        assertNotNull(e3);
        assertEquals("stored2.txt", e3.getName());
        assertArrayEquals("Stored two".getBytes("UTF-8"), readAll(zin));

        assertNull(zin.getNextZipEntry());
        zin.close();
    }

    @Test
    public void testRead_SingleByteRead() throws Exception {
        byte[] content = "SingleByte".getBytes("UTF-8");
        ZipArchiveInputStream zin = new ZipArchiveInputStream(
            new ByteArrayInputStream(createZip("single.txt", content, ZipArchiveOutputStream.STORED, false)));
        assertNotNull(zin.getNextZipEntry());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int b;
        while ((b = zin.read()) != -1) {
            out.write(b);
        }
        assertArrayEquals(content, out.toByteArray());
        zin.close();
    }

    @Test
    public void testSkip_PositiveValue() throws Exception {
        byte[] content = "Skip this part and read the rest".getBytes("UTF-8");
        ZipArchiveInputStream zin = new ZipArchiveInputStream(
            new ByteArrayInputStream(createZip("skip.txt", content, ZipArchiveOutputStream.STORED, false)));
        assertNotNull(zin.getNextZipEntry());
        long skipped = zin.skip(10); // skip first 10 bytes
        assertEquals(10, skipped);
        byte[] remaining = readAll(zin);
        byte[] expected = "part and read the rest".getBytes("UTF-8"); // after "Skip this "
        assertArrayEquals(expected, remaining);
        zin.close();
    }

    @Test
    public void testAvailable_AfterGetEntry() throws Exception {
        byte[] content = "Available content".getBytes("UTF-8");
        ZipArchiveInputStream zin = new ZipArchiveInputStream(
            new ByteArrayInputStream(createZip("avail.txt", content, ZipArchiveOutputStream.STORED, false)));
        assertNotNull(zin.getNextZipEntry());
        int avail = zin.available();
        assertTrue("available should be positive", avail > 0);
        zin.close();
    }

    @Test
    public void testClose_MultipleTimes() throws Exception {
        ZipArchiveInputStream zin = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        zin.close();
        // second close should not throw
        zin.close();
    }

    @Test
    public void testRead_SingleByteAfterClose_throwsIOException() throws Exception {
        ZipArchiveInputStream zin = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        zin.close();
        try {
            zin.read();
            fail("Expected IOException");
        } catch (IOException e) {
            // expected
        }
    }

    @Test
    public void testMatches_ShortBuffer() {
        // length less than signature length
        assertFalse(ZipArchiveInputStream.matches(new byte[] {0x50}, 0));
        assertFalse(ZipArchiveInputStream.matches(new byte[] {0x50, 0x4b}, 1));
        assertFalse(ZipArchiveInputStream.matches(new byte[] {0x50, 0x4b, 0x03}, 2));
        assertFalse(ZipArchiveInputStream.matches(new byte[] {0x50, 0x4b, 0x03, 0x04}, 3)); // length=3, not enough
    }

    @Test
    public void testConstructor_withEncodingAndAllowStoredDD() throws Exception {
        byte[] content = "Encoding test".getBytes("UTF-8");
        byte[] zip = createZip("enc.txt", content, ZipArchiveOutputStream.STORED, false);
        // default constructor uses platform encoding, but we can test explicit UTF-8
        ZipArchiveInputStream zin = new ZipArchiveInputStream(
            new ByteArrayInputStream(zip), "UTF-8");
        assertNotNull(zin.getNextZipEntry());
        assertArrayEquals(content, readAll(zin));
        zin.close();
    }

    @Test
    public void testGetNextZipEntry_AfterReadingAllEntries_returnsNull() throws Exception {
        byte[] content = "Only one".getBytes("UTF-8");
        ZipArchiveInputStream zin = new ZipArchiveInputStream(
            new ByteArrayInputStream(createZip("one.txt", content, ZipArchiveOutputStream.STORED, false)));
        assertNotNull(zin.getNextZipEntry());
        readAll(zin);
        assertNull(zin.getNextZipEntry());
        zin.close();
    }

    // ========== Helper methods (unchanged) ==========

    private byte[] createZip(String name, byte[] content, int method, boolean useDataDescriptor) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(bos);
        ZipArchiveEntry entry = new ZipArchiveEntry(name);
        entry.setMethod(method);
        if (method == ZipArchiveOutputStream.STORED && !useDataDescriptor) {
            CRC32 crc = new CRC32();
            crc.update(content);
            entry.setSize(content.length);
            entry.setCompressedSize(content.length);
            entry.setCrc(crc.getValue());
        }
        zos.putArchiveEntry(entry);
        zos.write(content);
        zos.closeArchiveEntry();
        zos.close();
        return bos.toByteArray();
    }

    private void addZipEntry(ZipArchiveOutputStream zos, String name, byte[] content, int method) throws IOException {
        ZipArchiveEntry entry = new ZipArchiveEntry(name);
        entry.setMethod(method);
        if (method == ZipArchiveOutputStream.STORED) {
            CRC32 crc = new CRC32();
            crc.update(content);
            entry.setSize(content.length);
            entry.setCompressedSize(content.length);
            entry.setCrc(crc.getValue());
        }
        zos.putArchiveEntry(entry);
        zos.write(content);
        zos.closeArchiveEntry();
    }

    private byte[] createStoredWithDataDescriptor(byte[] content) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(bos);
        ZipArchiveEntry entry = new ZipArchiveEntry("stored-dd.txt");
        entry.setMethod(ZipArchiveOutputStream.STORED);
        zos.putArchiveEntry(entry);
        zos.write(content);
        zos.closeArchiveEntry();
        zos.close();
        return bos.toByteArray();
    }

    private byte[] readAll(ZipArchiveInputStream zin) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int n;
        while ((n = zin.read(buffer)) != -1) {
            out.write(buffer, 0, n);
        }
        return out.toByteArray();
    }
}