package org.apache.commons.compress.archivers.zip;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ZipArchiveInputStreamTest {

    private byte[] createStoredEntryZip(String entryName, byte[] data) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] nameBytes = entryName.getBytes("UTF-8");

        CRC32 crc = new CRC32();
        if (data != null && data.length > 0) {
            crc.update(data);
        }

        // Local file header signature
        baos.write(new byte[]{0x50, 0x4b, 0x03, 0x04});
        // Version needed to extract (10)
        baos.write(new byte[]{10, 0});
        // General purpose bit flag (0)
        baos.write(new byte[]{0, 0});
        // Compression method (0 = STORED)
        baos.write(new byte[]{0, 0});
        // Mod time and date
        baos.write(new byte[]{0, 0, 0, 0});
        // CRC-32
        long crcVal = crc.getValue();
        baos.write(new byte[]{(byte) crcVal, (byte) (crcVal >> 8), (byte) (crcVal >> 16), (byte) (crcVal >> 24)});
        // Compressed size
        int size = data == null ? 0 : data.length;
        baos.write(new byte[]{(byte) size, (byte) (size >> 8), (byte) (size >> 16), (byte) (size >> 24)});
        // Uncompressed size
        baos.write(new byte[]{(byte) size, (byte) (size >> 8), (byte) (size >> 16), (byte) (size >> 24)});
        // Filename length
        baos.write(new byte[]{(byte) nameBytes.length, (byte) (nameBytes.length >> 8)});
        // Extra field length
        baos.write(new byte[]{0, 0});
        // Filename
        baos.write(nameBytes);
        // Data
        if (data != null && data.length > 0) {
            baos.write(data);
        }

        return baos.toByteArray();
    }

    private byte[] createDeflatedEntryZip(String entryName, byte[] uncompressedData) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] nameBytes = entryName.getBytes("UTF-8");

        CRC32 crc = new CRC32();
        crc.update(uncompressedData);

        byte[] deflatedData = new byte[uncompressedData.length + 100];
        Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION, true);
        deflater.setInput(uncompressedData);
        deflater.finish();
        int deflatedSize = deflater.deflate(deflatedData);
        deflater.end();

        // Local file header signature
        baos.write(new byte[]{0x50, 0x4b, 0x03, 0x04});
        // Version needed to extract (20)
        baos.write(new byte[]{20, 0});
        // General purpose bit flag (0)
        baos.write(new byte[]{0, 0});
        // Compression method (8 = DEFLATED)
        baos.write(new byte[]{8, 0});
        // Mod time and date
        baos.write(new byte[]{0, 0, 0, 0});
        // CRC-32
        long crcVal = crc.getValue();
        baos.write(new byte[]{(byte) crcVal, (byte) (crcVal >> 8), (byte) (crcVal >> 16), (byte) (crcVal >> 24)});
        // Compressed size
        baos.write(new byte[]{(byte) deflatedSize, (byte) (deflatedSize >> 8), (byte) (deflatedSize >> 16), (byte) (deflatedSize >> 24)});
        // Uncompressed size
        int uSize = uncompressedData.length;
        baos.write(new byte[]{(byte) uSize, (byte) (uSize >> 8), (byte) (uSize >> 16), (byte) (uSize >> 24)});
        // Filename length
        baos.write(new byte[]{(byte) nameBytes.length, (byte) (nameBytes.length >> 8)});
        // Extra field length
        baos.write(new byte[]{0, 0});
        // Filename
        baos.write(nameBytes);
        // Compressed data
        baos.write(deflatedData, 0, deflatedSize);

        return baos.toByteArray();
    }

    // Tests matches with valid Local File Header signature
    @Test
    public void testMatches_validLfhSignature_returnsTrue() {
        byte[] sig = new byte[]{0x50, 0x4b, 0x03, 0x04};
        assertTrue(ZipArchiveInputStream.matches(sig, 4));
    }

    // Tests matches with valid End of Central Directory signature
    @Test
    public void testMatches_validEocdSignature_returnsTrue() {
        byte[] sig = new byte[]{0x50, 0x4b, 0x05, 0x06};
        assertTrue(ZipArchiveInputStream.matches(sig, 4));
    }

    // Tests matches with signature length shorter than required
    @Test
    public void testMatches_shortLength_returnsFalse() {
        byte[] sig = new byte[]{0x50, 0x4b, 0x03};
        assertFalse(ZipArchiveInputStream.matches(sig, 3));
    }

    // Tests matches with invalid signature bytes
    @Test
    public void testMatches_invalidSignature_returnsFalse() {
        byte[] sig = new byte[]{0x50, 0x4b, 0x01, 0x02};
        assertFalse(ZipArchiveInputStream.matches(sig, 4));
    }

    // Tests getNextZipEntry on empty stream
    @Test
    public void testGetNextZipEntry_emptyStream_returnsNull() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        ZipArchiveInputStream zipIn = new ZipArchiveInputStream(in);
        assertNull(zipIn.getNextZipEntry());
        zipIn.close();
    }

    // Tests getNextZipEntry when encountering Central Directory signature
    @Test
    public void testGetNextZipEntry_centralDirectoryHeader_returnsNull() throws IOException {
        byte[] cdh = new byte[]{0x50, 0x4b, 0x01, 0x02, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        ZipArchiveInputStream zipIn = new ZipArchiveInputStream(new ByteArrayInputStream(cdh));
        assertNull(zipIn.getNextZipEntry());
        // Second call should also return null due to hitCentralDirectory flag
        assertNull(zipIn.getNextZipEntry());
        zipIn.close();
    }

    // Tests getNextZipEntry reading a valid STORED entry and its content
    @Test
    public void testGetNextZipEntry_storedEntry_readsCorrectData() throws IOException {
        byte[] content = "Hello World!".getBytes("UTF-8");
        byte[] zipData = createStoredEntryZip("test.txt", content);

        ZipArchiveInputStream zipIn = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        ZipArchiveEntry entry = zipIn.getNextZipEntry();

        assertNotNull(entry);
        assertEquals("test.txt", entry.getName());
        assertEquals(ZipArchiveOutputStream.STORED, entry.getMethod());
        assertEquals(content.length, entry.getSize());

        byte[] readBuffer = new byte[content.length];
        int bytesRead = zipIn.read(readBuffer, 0, readBuffer.length);
        assertEquals(content.length, bytesRead);
        assertArrayEquals(content, readBuffer);

        // Subsequent read should return -1 at EOF of entry
        assertEquals(-1, zipIn.read(readBuffer, 0, readBuffer.length));
        zipIn.close();
    }

    // Tests getNextEntry alias delegates to getNextZipEntry
    @Test
    public void testGetNextEntry_delegatesToGetNextZipEntry_returnsEntry() throws IOException {
        byte[] content = "ArchiveEntry test".getBytes("UTF-8");
        byte[] zipData = createStoredEntryZip("alias.txt", content);

        ZipArchiveInputStream zipIn = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        assertEquals("alias.txt", zipIn.getNextEntry().getName());
        zipIn.close();
    }

    // Tests getNextZipEntry reading a valid DEFLATED entry and inflating content
    @Test
    public void testGetNextZipEntry_deflatedEntry_decompressesSuccessfully() throws IOException {
        byte[] content = "Compress deflated entry test string repeated multiple times to ensure compression works."
                .getBytes("UTF-8");
        byte[] zipData = createDeflatedEntryZip("deflated.txt", content);

        ZipArchiveInputStream zipIn = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        ZipArchiveEntry entry = zipIn.getNextZipEntry();

        assertNotNull(entry);
        assertEquals("deflated.txt", entry.getName());
        assertEquals(ZipArchiveOutputStream.DEFLATED, entry.getMethod());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[32];
        int read;
        while ((read = zipIn.read(buffer, 0, buffer.length)) != -1) {
            out.write(buffer, 0, read);
        }

        assertArrayEquals(content, out.toByteArray());
        zipIn.close();
    }

    // Tests reading multiple sequential entries
    @Test
    public void testGetNextZipEntry_multipleEntries_readsAllSequentially() throws IOException {
        byte[] file1 = createStoredEntryZip("file1.txt", "Content 1".getBytes("UTF-8"));
        byte[] file2 = createStoredEntryZip("file2.txt", "Content 2".getBytes("UTF-8"));

        ByteArrayOutputStream combined = new ByteArrayOutputStream();
        combined.write(file1);
        combined.write(file2);

        ZipArchiveInputStream zipIn = new ZipArchiveInputStream(new ByteArrayInputStream(combined.toByteArray()));

        ZipArchiveEntry entry1 = zipIn.getNextZipEntry();
        assertNotNull(entry1);
        assertEquals("file1.txt", entry1.getName());

        ZipArchiveEntry entry2 = zipIn.getNextZipEntry();
        assertNotNull(entry2);
        assertEquals("file2.txt", entry2.getName());

        assertNull(zipIn.getNextZipEntry());
        zipIn.close();
    }

    // Tests read on closed stream throws IOException
    @Test(expected = IOException.class)
    public void testRead_streamClosed_throwsIOException() throws IOException {
        byte[] zipData = createStoredEntryZip("test.txt", "data".getBytes("UTF-8"));
        ZipArchiveInputStream zipIn = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        zipIn.close();
        zipIn.read(new byte[10], 0, 10);
    }

    // Tests getNextZipEntry on closed stream returns null
    @Test
    public void testGetNextZipEntry_streamClosed_returnsNull() throws IOException {
        byte[] zipData = createStoredEntryZip("test.txt", "data".getBytes("UTF-8"));
        ZipArchiveInputStream zipIn = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        zipIn.close();
        assertNull(zipIn.getNextZipEntry());
    }

    // Tests read when no entry has been opened returns -1
    @Test
    public void testRead_noCurrentEntry_returnsMinusOne() throws IOException {
        ZipArchiveInputStream zipIn = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        assertEquals(-1, zipIn.read(new byte[10], 0, 10));
        zipIn.close();
    }

    // Tests read with out-of-bounds start and length parameters throws ArrayIndexOutOfBoundsException
    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testRead_invalidBounds_throwsArrayIndexOutOfBoundsException() throws IOException {
        byte[] zipData = createStoredEntryZip("test.txt", "data".getBytes("UTF-8"));
        ZipArchiveInputStream zipIn = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        zipIn.getNextZipEntry();
        try {
            zipIn.read(new byte[10], 5, 10);
        } finally {
            zipIn.close();
        }
    }

    // Tests skip method skips correct number of bytes
    @Test
    public void testSkip_validPositiveValue_skipsBytesCorrectly() throws IOException {
        byte[] content = "1234567890".getBytes("UTF-8");
        byte[] zipData = createStoredEntryZip("skip.txt", content);

        ZipArchiveInputStream zipIn = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        zipIn.getNextZipEntry();

        long skipped = zipIn.skip(5);
        assertEquals(5, skipped);

        byte[] remaining = new byte[5];
        int read = zipIn.read(remaining, 0, remaining.length);
        assertEquals(5, read);
        assertEquals("67890", new String(remaining, "UTF-8"));
        zipIn.close();
    }

    // Tests skip with negative value throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testSkip_negativeValue_throwsIllegalArgumentException() throws IOException {
        ZipArchiveInputStream zipIn = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        try {
            zipIn.skip(-1);
        } finally {
            zipIn.close();
        }
    }

    // Tests custom encoding constructor with UTF-8
    @Test
    public void testConstructor_customEncoding_initializesCorrectly() throws IOException {
        byte[] zipData = createStoredEntryZip("unicode_entry.txt", new byte[0]);
        ZipArchiveInputStream zipIn = new ZipArchiveInputStream(new ByteArrayInputStream(zipData), "UTF-8", true);
        ZipArchiveEntry entry = zipIn.getNextZipEntry();
        assertNotNull(entry);
        assertEquals("unicode_entry.txt", entry.getName());
        zipIn.close();
    }
}