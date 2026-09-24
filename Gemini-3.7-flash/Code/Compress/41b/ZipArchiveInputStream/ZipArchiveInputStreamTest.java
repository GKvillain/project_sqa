package org.apache.commons.compress.archivers.zip;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CRC32;
import java.util.zip.ZipException;

import static org.junit.Assert.*;

public class ZipArchiveInputStreamTest {

    // Tests matches method with valid and invalid zip signatures
    @Test
    public void testMatches_validAndInvalidSignatures_returnsExpected() {
        byte[] lfh = ZipArchiveOutputStream.LFH_SIG;
        assertTrue(ZipArchiveInputStream.matches(lfh, 4));

        byte[] eocd = ZipArchiveOutputStream.EOCD_SIG;
        assertTrue(ZipArchiveInputStream.matches(eocd, 4));

        byte[] dd = ZipArchiveOutputStream.DD_SIG;
        assertTrue(ZipArchiveInputStream.matches(dd, 4));

        byte[] split = ZipLong.SINGLE_SEGMENT_SPLIT_MARKER.getBytes();
        assertTrue(ZipArchiveInputStream.matches(split, 4));

        byte[] invalid = new byte[] {0x00, 0x01, 0x02, 0x03};
        assertFalse(ZipArchiveInputStream.matches(invalid, 4));

        assertFalse(ZipArchiveInputStream.matches(lfh, 3));
    }

    // Tests skip with negative value throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testSkip_negativeValue_throwsIllegalArgumentException() throws IOException {
        ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        try {
            in.skip(-1);
        } finally {
            in.close();
        }
    }

    // Tests read when stream is closed throws IOException
    @Test(expected = IOException.class)
    public void testRead_closedStream_throwsIOException() throws IOException {
        ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        in.close();
        byte[] buf = new byte[10];
        in.read(buf, 0, 10);
    }

    // Tests read when there is no current entry returns -1
    @Test
    public void testRead_noCurrentEntry_returnsMinusOne() throws IOException {
        ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        try {
            byte[] buf = new byte[10];
            assertEquals(-1, in.read(buf, 0, 10));
        } finally {
            in.close();
        }
    }

    // Tests read bounds check with invalid offset and length
    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testRead_invalidBufferBounds_throwsArrayIndexOutOfBoundsException() throws IOException {
        byte[] zipData = createStoredZipData("entry.txt", "data".getBytes("UTF-8"));
        ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        try {
            assertNotNull(in.getNextZipEntry());
            byte[] buf = new byte[10];
            in.read(buf, 5, 10);
        } finally {
            in.close();
        }
    }

    // Tests getNextZipEntry on empty stream returns null
    @Test
    public void testGetNextZipEntry_emptyStream_returnsNull() throws IOException {
        ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        try {
            assertNull(in.getNextZipEntry());
            assertNull(in.getNextEntry());
        } finally {
            in.close();
        }
    }

    // Tests reading STORED entries correctly
    @Test
    public void testGetNextZipEntry_storedEntry_readsContentSuccessfully() throws IOException {
        byte[] content = "Hello Stored Zip".getBytes("UTF-8");
        byte[] zipData = createStoredZipData("hello.txt", content);

        ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        try {
            ZipArchiveEntry entry = in.getNextZipEntry();
            assertNotNull(entry);
            assertEquals("hello.txt", entry.getName());
            assertEquals(content.length, entry.getSize());
            assertTrue(in.canReadEntryData(entry));

            byte[] readBuffer = new byte[content.length];
            int bytesRead = in.read(readBuffer, 0, readBuffer.length);
            assertEquals(content.length, bytesRead);
            assertArrayEquals(content, readBuffer);
            assertEquals(-1, in.read(readBuffer, 0, 1));

            assertNull(in.getNextZipEntry());
        } finally {
            in.close();
        }
    }

    // Tests reading DEFLATED entries correctly
    @Test
    public void testGetNextZipEntry_deflatedEntry_readsContentSuccessfully() throws IOException {
        byte[] content = "Hello Deflated World! This is compressed text content.".getBytes("UTF-8");
        byte[] zipData = createDeflatedZipData("deflated.txt", content);

        ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        try {
            ZipArchiveEntry entry = in.getNextZipEntry();
            assertNotNull(entry);
            assertEquals("deflated.txt", entry.getName());
            assertTrue(in.canReadEntryData(entry));

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[16];
            int read;
            while ((read = in.read(buffer, 0, buffer.length)) != -1) {
                out.write(buffer, 0, read);
            }
            assertArrayEquals(content, out.toByteArray());
            assertNull(in.getNextZipEntry());
        } finally {
            in.close();
        }
    }

    // Tests reading multiple sequential entries and skipping entry content
    @Test
    public void testGetNextZipEntry_multipleEntries_readsAllSequentially() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);

        ZipArchiveEntry entry1 = new ZipArchiveEntry("file1.txt");
        zaos.putArchiveEntry(entry1);
        zaos.write("content1".getBytes("UTF-8"));
        zaos.closeArchiveEntry();

        ZipArchiveEntry entry2 = new ZipArchiveEntry("file2.txt");
        zaos.putArchiveEntry(entry2);
        zaos.write("content2".getBytes("UTF-8"));
        zaos.closeArchiveEntry();
        zaos.close();

        ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        try {
            ZipArchiveEntry readEntry1 = in.getNextZipEntry();
            assertNotNull(readEntry1);
            assertEquals("file1.txt", readEntry1.getName());

            // Advance without reading content to test closeEntry / draining
            ZipArchiveEntry readEntry2 = in.getNextZipEntry();
            assertNotNull(readEntry2);
            assertEquals("file2.txt", readEntry2.getName());

            byte[] buf = new byte[32];
            int len = in.read(buf, 0, buf.length);
            assertEquals("content2", new String(buf, 0, len, "UTF-8"));

            assertNull(in.getNextZipEntry());
        } finally {
            in.close();
        }
    }

    // Tests split archive signature throws UnsupportedZipFeatureException
    @Test(expected = UnsupportedZipFeatureException.class)
    public void testGetNextZipEntry_splitArchiveSignature_throwsException() throws IOException {
        byte[] ddSig = new byte[30];
        System.arraycopy(ZipArchiveOutputStream.DD_SIG, 0, ddSig, 0, 4);
        ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(ddSig));
        try {
            in.getNextZipEntry();
        } finally {
            in.close();
        }
    }

    // Tests skip method skips requested amount of uncompressed bytes
    @Test
    public void testSkip_validPositiveValue_skipsBytes() throws IOException {
        byte[] content = "0123456789ABCDEF".getBytes("UTF-8");
        byte[] zipData = createStoredZipData("skip.txt", content);

        ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        try {
            assertNotNull(in.getNextZipEntry());
            long skipped = in.skip(5);
            assertEquals(5, skipped);
            byte[] buf = new byte[5];
            int read = in.read(buf, 0, 5);
            assertEquals(5, read);
            assertEquals("56789", new String(buf, 0, read, "UTF-8"));
        } finally {
            in.close();
        }
    }

    // Tests canReadEntryData with non-zip ArchiveEntry and unsupported methods
    @Test
    public void testCanReadEntryData_variousEntries_returnsCorrectBoolean() throws IOException {
        ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        try {
            assertFalse(in.canReadEntryData(null));

            ArchiveEntry dummyEntry = new ArchiveEntry() {
                public String getName() { return "dummy"; }
                public long getSize() { return 0; }
                public boolean isDirectory() { return false; }
                public java.util.Date getLastModifiedDate() { return null; }
            };
            assertFalse(in.canReadEntryData(dummyEntry));

            ZipArchiveEntry storedEntry = new ZipArchiveEntry("test");
            storedEntry.setMethod(ZipArchiveOutputStream.STORED);
            assertTrue(in.canReadEntryData(storedEntry));

            ZipArchiveEntry deflatedEntry = new ZipArchiveEntry("test");
            deflatedEntry.setMethod(ZipArchiveOutputStream.DEFLATED);
            assertTrue(in.canReadEntryData(deflatedEntry));

            ZipArchiveEntry unsupportedEntry = new ZipArchiveEntry("test");
            unsupportedEntry.setMethod(ZipMethod.TOKENIZATION.getCode());
            assertFalse(in.canReadEntryData(unsupportedEntry));
        } finally {
            in.close();
        }
    }

    // Tests closing the stream multiple times does not throw exception
    @Test
    public void testClose_multipleInvocations_idempotent() throws IOException {
        ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        in.close();
        in.close();
    }

    // Tests read with len == 0 returns 0
    @Test
    public void testRead_zeroLength_returnsZero() throws IOException {
        byte[] zipData = createStoredZipData("entry.txt", "data".getBytes("UTF-8"));
        ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        try {
            assertNotNull(in.getNextZipEntry());
            byte[] buf = new byte[10];
            assertEquals(0, in.read(buf, 0, 0));
        } finally {
            in.close();
        }
    }

    // Tests skip with toSkip == 0 returns 0
    @Test
    public void testSkip_zeroBytes_returnsZero() throws IOException {
        byte[] zipData = createStoredZipData("entry.txt", "data".getBytes("UTF-8"));
        ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        try {
            assertNotNull(in.getNextZipEntry());
            assertEquals(0, in.skip(0));
        } finally {
            in.close();
        }
    }

    // Tests constructors with encoding and configuration parameters
    @Test
    public void testConstructors_variousConfigurations_initializeSuccessfully() throws IOException {
        byte[] zipData = createStoredZipData("entry.txt", "data".getBytes("UTF-8"));

        ZipArchiveInputStream in1 = new ZipArchiveInputStream(new ByteArrayInputStream(zipData), "UTF-8");
        assertEquals("UTF-8", in1.getEncoding());
        assertNotNull(in1.getNextZipEntry());
        in1.close();

        ZipArchiveInputStream in2 = new ZipArchiveInputStream(new ByteArrayInputStream(zipData), "UTF-8", true);
        assertNotNull(in2.getNextZipEntry());
        in2.close();

        ZipArchiveInputStream in3 = new ZipArchiveInputStream(new ByteArrayInputStream(zipData), "UTF-8", true, true);
        assertNotNull(in3.getNextZipEntry());
        in3.close();
    }

    // Tests entry with encrypted flag set throws UnsupportedZipFeatureException on read
    @Test(expected = UnsupportedZipFeatureException.class)
    public void testRead_encryptedEntry_throwsUnsupportedZipFeatureException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        ZipArchiveEntry entry = new ZipArchiveEntry("encrypted.txt");
        zaos.putArchiveEntry(entry);
        zaos.write("secret".getBytes("UTF-8"));
        zaos.closeArchiveEntry();
        zaos.close();

        byte[] rawZip = baos.toByteArray();
        // General purpose bit flag is at offset 6 in LFH; set bit 0 (encrypted)
        rawZip[6] = (byte) (rawZip[6] | 0x01);

        ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(rawZip));
        try {
            ZipArchiveEntry readEntry = in.getNextZipEntry();
            assertNotNull(readEntry);
            assertFalse(in.canReadEntryData(readEntry));
            byte[] buf = new byte[16];
            in.read(buf, 0, buf.length);
        } finally {
            in.close();
        }
    }

    // Tests entry with unsupported compression method throws UnsupportedZipFeatureException on read
    @Test(expected = UnsupportedZipFeatureException.class)
    public void testRead_unsupportedMethod_throwsUnsupportedZipFeatureException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        ZipArchiveEntry entry = new ZipArchiveEntry("unsupported.txt");
        zaos.putArchiveEntry(entry);
        zaos.write("content".getBytes("UTF-8"));
        zaos.closeArchiveEntry();
        zaos.close();

        byte[] rawZip = baos.toByteArray();
        // Compression method is at offset 8 (2 bytes); set to method 99 (unsupported)
        rawZip[8] = 99;
        rawZip[9] = 0;

        ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(rawZip));
        try {
            ZipArchiveEntry readEntry = in.getNextZipEntry();
            assertNotNull(readEntry);
            assertFalse(in.canReadEntryData(readEntry));
            byte[] buf = new byte[16];
            in.read(buf, 0, buf.length);
        } finally {
            in.close();
        }
    }

    // Tests CRC verification failure for corrupted stored entry
    @Test(expected = ZipException.class)
    public void testRead_corruptedStoredEntryCrc_throwsZipException() throws IOException {
        byte[] content = "valid original data".getBytes("UTF-8");
        byte[] zipData = createStoredZipData("corrupt.txt", content);

        // Corrupt content payload in stored zip (LFH header length is 30 + name len 11 = 41)
        int payloadOffset = 30 + "corrupt.txt".length();
        zipData[payloadOffset] = (byte) (zipData[payloadOffset] ^ 0xFF);

        ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        try {
            assertNotNull(in.getNextZipEntry());
            byte[] buf = new byte[64];
            while (in.read(buf, 0, buf.length) != -1) {
                // Read until EOF to trigger CRC validation
            }
        } finally {
            in.close();
        }
    }

    // Tests corrupted deflated stream throws ZipException
    @Test(expected = ZipException.class)
    public void testRead_corruptedDeflatedStream_throwsZipException() throws IOException {
        byte[] content = "Deflated content to corrupt and fail decompression".getBytes("UTF-8");
        byte[] zipData = createDeflatedZipData("corrupt_deflate.txt", content);

        int payloadOffset = 30 + "corrupt_deflate.txt".length();
        // Corrupt deflated payload bytes
        for (int i = 0; i < 5 && (payloadOffset + i) < zipData.length; i++) {
            zipData[payloadOffset + i] = (byte) 0xFF;
        }

        ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        try {
            assertNotNull(in.getNextZipEntry());
            byte[] buf = new byte[64];
            while (in.read(buf, 0, buf.length) != -1) {
                // Consume to trigger decompress error
            }
        } finally {
            in.close();
        }
    }

    // Tests reading ZipArchiveEntry with unicode extra fields
    @Test
    public void testGetNextZipEntry_unicodeExtraFields_parsesCorrectly() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        zaos.setCreateUnicodeExtraFields(ZipArchiveOutputStream.UnicodeExtraFieldPolicy.ALWAYS);

        ZipArchiveEntry entry = new ZipArchiveEntry("unicode_test_äöü.txt");
        zaos.putArchiveEntry(entry);
        zaos.write("unicode data".getBytes("UTF-8"));
        zaos.closeArchiveEntry();
        zaos.close();

        ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()), "UTF-8", true);
        try {
            ZipArchiveEntry readEntry = in.getNextZipEntry();
            assertNotNull(readEntry);
            assertEquals("unicode_test_äöü.txt", readEntry.getName());
        } finally {
            in.close();
        }
    }

    // Helper method to create stored zip archive bytes
    private byte[] createStoredZipData(String name, byte[] content) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        ZipArchiveEntry entry = new ZipArchiveEntry(name);
        entry.setMethod(ZipArchiveOutputStream.STORED);
        entry.setSize(content.length);
        entry.setCompressedSize(content.length);
        CRC32 crc = new CRC32();
        crc.update(content);
        entry.setCrc(crc.getValue());
        zaos.putArchiveEntry(entry);
        zaos.write(content);
        zaos.closeArchiveEntry();
        zaos.close();
        return baos.toByteArray();
    }

    // Helper method to create deflated zip archive bytes
    private byte[] createDeflatedZipData(String name, byte[] content) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        ZipArchiveEntry entry = new ZipArchiveEntry(name);
        entry.setMethod(ZipArchiveOutputStream.DEFLATED);
        zaos.putArchiveEntry(entry);
        zaos.write(content);
        zaos.closeArchiveEntry();
        zaos.close();
        return baos.toByteArray();
    }
}