package org.apache.commons.compress.archivers.zip;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ZipArchiveInputStreamTest {

    // Tests signature matching with valid Local File Header signature
    @Test
    public void testMatches_validLfhSignature_returnsTrue() {
        byte[] sig = ZipArchiveOutputStream.LFH_SIG;
        assertTrue(ZipArchiveInputStream.matches(sig, sig.length));
    }

    // Tests signature matching with valid End of Central Directory signature
    @Test
    public void testMatches_validEocdSignature_returnsTrue() {
        byte[] sig = ZipArchiveOutputStream.EOCD_SIG;
        assertTrue(ZipArchiveInputStream.matches(sig, sig.length));
    }

    // Tests signature matching with length shorter than signature length
    @Test
    public void testMatches_lengthTooShort_returnsFalse() {
        byte[] sig = ZipArchiveOutputStream.LFH_SIG;
        assertFalse(ZipArchiveInputStream.matches(sig, sig.length - 1));
    }

    // Tests signature matching with invalid signature bytes
    @Test
    public void testMatches_invalidSignature_returnsFalse() {
        byte[] invalidSig = new byte[]{0x00, 0x01, 0x02, 0x03};
        assertFalse(ZipArchiveInputStream.matches(invalidSig, invalidSig.length));
    }

    // Tests canReadEntryData for standard STORED and DEFLATED entries
    @Test
    public void testCanReadEntryData_supportedEntries_returnsTrue() {
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        
        ZipArchiveEntry storedEntry = new ZipArchiveEntry("stored.txt");
        storedEntry.setMethod(ZipEntry.STORED);
        assertTrue(zis.canReadEntryData(storedEntry));

        ZipArchiveEntry deflatedEntry = new ZipArchiveEntry("deflated.txt");
        deflatedEntry.setMethod(ZipEntry.DEFLATED);
        assertTrue(zis.canReadEntryData(deflatedEntry));
    }

    // Tests canReadEntryData with non-ZipArchiveEntry instance
    @Test
    public void testCanReadEntryData_nonZipArchiveEntry_returnsFalse() {
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        ArchiveEntry nonZipEntry = new ArchiveEntry() {
            public String getName() { return "test"; }
            public long getSize() { return 0; }
            public boolean flow() { return false; }
            public boolean isDirectory() { return false; }
            public java.util.Date getLastModifiedDate() { return new java.util.Date(); }
        };
        assertFalse(zis.canReadEntryData(nonZipEntry));
        assertFalse(zis.canReadEntryData(null));
    }

    // Tests read when stream is already closed throws IOException
    @Test(expected = IOException.class)
    public void testRead_closedStream_throwsIOException() throws IOException {
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        zis.close();
        zis.read(new byte[10], 0, 10);
    }

    // Tests read returns -1 when no entry is currently open
    @Test
    public void testRead_noCurrentEntry_returnsMinusOne() throws IOException {
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        assertEquals(-1, zis.read(new byte[10], 0, 10));
    }

    // Tests read with invalid offset and length arguments throws ArrayIndexOutOfBoundsException
    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testRead_invalidOffsetAndLength_throwsArrayIndexOutOfBoundsException() throws IOException {
        byte[] dummyZip = createZipWithStoredEntry("test.txt", "hello".getBytes("UTF-8"));
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(dummyZip));
        zis.getNextZipEntry();
        zis.read(new byte[10], -1, 5);
    }

    // Tests skip with negative value throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testSkip_negativeValue_throwsIllegalArgumentException() throws IOException {
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        zis.skip(-1);
    }

    // Tests reading a valid STORED entry correctly retrieves uncorrupted data
    @Test
    public void testGetNextZipEntry_storedEntry_readsCorrectData() throws IOException {
        byte[] expectedData = "Defects4J Compress STORED Entry Test Payload".getBytes("UTF-8");
        byte[] zipBytes = createZipWithStoredEntry("stored.txt", expectedData);

        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipBytes));
        ZipArchiveEntry entry = zis.getNextZipEntry();

        assertNotNull(entry);
        assertEquals("stored.txt", entry.getName());
        assertEquals(ZipEntry.STORED, entry.getMethod());

        byte[] actualData = new byte[expectedData.length];
        int readBytes = 0;
        int r;
        while (readBytes < actualData.length && (r = zis.read(actualData, readBytes, actualData.length - readBytes)) != -1) {
            readBytes += r;
        }

        assertEquals(expectedData.length, readBytes);
        assertArrayEquals(expectedData, actualData);
        assertEquals(-1, zis.read());
        assertNull(zis.getNextZipEntry());
        zis.close();
    }

    // Tests reading a DEFLATED entry correctly decompresses data
    @Test
    public void testGetNextZipEntry_deflatedEntry_readsCorrectData() throws IOException {
        byte[] expectedData = "Defects4J Compress DEFLATED Entry Test Payload".getBytes("UTF-8");
        byte[] zipBytes = createZipWithDeflatedEntry("deflated.txt", expectedData);

        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipBytes));
        ZipArchiveEntry entry = zis.getNextZipEntry();

        assertNotNull(entry);
        assertEquals("deflated.txt", entry.getName());
        assertEquals(ZipEntry.DEFLATED, entry.getMethod());

        byte[] actualData = new byte[expectedData.length];
        int readBytes = 0;
        int r;
        while (readBytes < actualData.length && (r = zis.read(actualData, readBytes, actualData.length - readBytes)) != -1) {
            readBytes += r;
        }

        assertEquals(expectedData.length, readBytes);
        assertArrayEquals(expectedData, actualData);
        assertEquals(-1, zis.read());
        zis.close();
    }

    // Tests reading multiple entries sequentially from archive
    @Test
    public void testGetNextZipEntry_multipleEntries_readsSequentially() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);

        byte[] data1 = "First Entry Data".getBytes("UTF-8");
        ZipArchiveEntry entry1 = new ZipArchiveEntry("entry1.txt");
        entry1.setMethod(ZipEntry.STORED);
        entry1.setSize(data1.length);
        entry1.setCompressedSize(data1.length);
        CRC32 crc1 = new CRC32();
        crc1.update(data1);
        entry1.setCrc(crc1.getValue());
        zaos.putArchiveEntry(entry1);
        zaos.write(data1);
        zaos.closeArchiveEntry();

        byte[] data2 = "Second Entry Data".getBytes("UTF-8");
        ZipArchiveEntry entry2 = new ZipArchiveEntry("entry2.txt");
        zaos.putArchiveEntry(entry2);
        zaos.write(data2);
        zaos.closeArchiveEntry();
        zaos.close();

        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));

        ZipArchiveEntry readEntry1 = zis.getNextZipEntry();
        assertNotNull(readEntry1);
        assertEquals("entry1.txt", readEntry1.getName());
        byte[] buf1 = new byte[data1.length];
        zis.read(buf1);
        assertArrayEquals(data1, buf1);

        ZipArchiveEntry readEntry2 = zis.getNextZipEntry();
        assertNotNull(readEntry2);
        assertEquals("entry2.txt", readEntry2.getName());
        byte[] buf2 = new byte[data2.length];
        zis.read(buf2);
        assertArrayEquals(data2, buf2);

        assertNull(zis.getNextZipEntry());
        zis.close();
    }

    // Tests skip functionality within an entry
    @Test
    public void testSkip_validBytes_skipsSuccessfully() throws IOException {
        byte[] data = "0123456789ABCDEF".getBytes("UTF-8");
        byte[] zipBytes = createZipWithStoredEntry("skip.txt", data);

        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipBytes));
        assertNotNull(zis.getNextZipEntry());

        long skipped = zis.skip(5);
        assertEquals(5, skipped);

        byte[] remaining = new byte[11];
        int read = zis.read(remaining);
        assertEquals(11, read);
        assertEquals("56789ABCDEF", new String(remaining, "UTF-8"));
        zis.close();
    }

    // Tests stream encountering split archive marker throws UnsupportedZipFeatureException
    @Test(expected = UnsupportedZipFeatureException.class)
    public void testReadFirstLocalFileHeader_splitArchiveSignature_throwsUnsupportedZipFeatureException() throws IOException {
        byte[] splitSig = ZipLong.DD_SIG.getBytes();
        byte[] data = new byte[30];
        System.arraycopy(splitSig, 0, data, 0, splitSig.length);

        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(data));
        zis.getNextZipEntry();
    }

    // Tests closing the stream multiple times does not throw exception
    @Test
    public void testClose_multipleCalls_noException() throws IOException {
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        zis.close();
        zis.close();
    }

    // Tests matches with APK signing block / archive extra data record signature
    @Test
    public void testMatches_archiveExtraDataSignature_returnsTrue() {
        byte[] sig = ZipArchiveOutputStream.AED_SIG;
        assertTrue(ZipArchiveInputStream.matches(sig, sig.length));
    }

    // Tests single byte read() on STORED entry
    @Test
    public void testRead_singleByte_readsAllBytesCorrectly() throws IOException {
        byte[] expected = "SingleByteRead".getBytes("UTF-8");
        byte[] zip = createZipWithStoredEntry("single.txt", expected);

        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zip));
        assertNotNull(zis.getNextZipEntry());

        for (int i = 0; i < expected.length; i++) {
            int b = zis.read();
            assertEquals(expected[i] & 0xFF, b);
        }
        assertEquals(-1, zis.read());
        zis.close();
    }

    // Tests getNextEntry delegate method returns same entry as getNextZipEntry
    @Test
    public void testGetNextEntry_delegatesToGetNextZipEntry() throws IOException {
        byte[] zip = createZipWithStoredEntry("delegate.txt", "data".getBytes("UTF-8"));
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zip));

        ArchiveEntry entry = zis.getNextEntry();
        assertNotNull(entry);
        assertEquals("delegate.txt", entry.getName());
        assertNull(zis.getNextEntry());
        zis.close();
    }

    // Tests reading an entry with unsupported compression method
    @Test
    public void testCanReadEntryData_unsupportedMethod_returnsFalse() {
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        ZipArchiveEntry entry = new ZipArchiveEntry("unsupported.txt");
        entry.setMethod(ZipMethod.BZIP2.getCode());
        assertFalse(zis.canReadEntryData(entry));
    }

    // Tests canReadEntryData when entry is encrypted
    @Test
    public void testCanReadEntryData_encryptedEntry_returnsFalse() {
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        ZipArchiveEntry entry = new ZipArchiveEntry("encrypted.txt");
        entry.setMethod(ZipEntry.DEFLATED);
        GeneralPurposeBit gpb = new GeneralPurposeBit();
        gpb.useEncryption(true);
        entry.setGeneralPurposeBit(gpb);
        assertFalse(zis.canReadEntryData(entry));
    }

    // Tests canReadEntryData with strong encryption flag
    @Test
    public void testCanReadEntryData_strongEncryption_returnsFalse() {
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        ZipArchiveEntry entry = new ZipArchiveEntry("strong_encrypted.txt");
        entry.setMethod(ZipEntry.DEFLATED);
        GeneralPurposeBit gpb = new GeneralPurposeBit();
        gpb.useStrongEncryption(true);
        entry.setGeneralPurposeBit(gpb);
        assertFalse(zis.canReadEntryData(entry));
    }

    // Tests skipping remaining entry bytes automatically when advancing to the next entry
    @Test
    public void testGetNextZipEntry_skipsUnreadBytesOfPreviousEntry() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);

        byte[] data1 = "Unread Content in Entry 1".getBytes("UTF-8");
        ZipArchiveEntry entry1 = new ZipArchiveEntry("first.txt");
        entry1.setMethod(ZipEntry.DEFLATED);
        zaos.putArchiveEntry(entry1);
        zaos.write(data1);
        zaos.closeArchiveEntry();

        byte[] data2 = "Content in Entry 2".getBytes("UTF-8");
        ZipArchiveEntry entry2 = new ZipArchiveEntry("second.txt");
        entry2.setMethod(ZipEntry.DEFLATED);
        zaos.putArchiveEntry(entry2);
        zaos.write(data2);
        zaos.closeArchiveEntry();
        zaos.close();

        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        ZipArchiveEntry read1 = zis.getNextZipEntry();
        assertNotNull(read1);
        assertEquals("first.txt", read1.getName());
        // Do not read any data, move directly to next entry
        ZipArchiveEntry read2 = zis.getNextZipEntry();
        assertNotNull(read2);
        assertEquals("second.txt", read2.getName());

        byte[] buf = new byte[data2.length];
        int read = zis.read(buf);
        assertEquals(data2.length, read);
        assertArrayEquals(data2, buf);
        assertNull(zis.getNextZipEntry());
        zis.close();
    }

    // Tests custom encoding in ZipArchiveInputStream constructor
    @Test
    public void testConstructor_withEncodingAndUseUnicodeExtraFields() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        zaos.setEncoding("UTF-8");

        ZipArchiveEntry entry = new ZipArchiveEntry("test-utf8.txt");
        zaos.putArchiveEntry(entry);
        zaos.write("hello".getBytes("UTF-8"));
        zaos.closeArchiveEntry();
        zaos.close();

        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()), "UTF-8", true);
        assertEquals("UTF-8", zis.getEncoding());
        ZipArchiveEntry readEntry = zis.getNextZipEntry();
        assertNotNull(readEntry);
        assertEquals("test-utf8.txt", readEntry.getName());
        zis.close();
    }

    // Tests reading zero length from read(buffer, 0, 0)
    @Test
    public void testRead_zeroLength_returnsZero() throws IOException {
        byte[] zip = createZipWithStoredEntry("zero.txt", "data".getBytes("UTF-8"));
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zip));
        assertNotNull(zis.getNextZipEntry());

        int bytesRead = zis.read(new byte[10], 0, 0);
        assertEquals(0, bytesRead);
        zis.close();
    }

    // Tests skipping zero bytes
    @Test
    public void testSkip_zeroBytes_returnsZero() throws IOException {
        byte[] zip = createZipWithStoredEntry("skipZero.txt", "data".getBytes("UTF-8"));
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zip));
        assertNotNull(zis.getNextZipEntry());

        assertEquals(0, zis.skip(0));
        zis.close();
    }

    // Tests reading an entry with Data Descriptor (streaming mode DEFLATE)
    @Test
    public void testGetNextZipEntry_dataDescriptorEntry_readsCorrectly() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);

        byte[] payload = "Data descriptor test stream content".getBytes("UTF-8");
        ZipArchiveEntry entry = new ZipArchiveEntry("stream_entry.txt");
        zaos.putArchiveEntry(entry);
        zaos.write(payload);
        zaos.closeArchiveEntry();
        zaos.close();

        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        ZipArchiveEntry readEntry = zis.getNextZipEntry();
        assertNotNull(readEntry);

        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[8];
        int count;
        while ((count = zis.read(buffer)) != -1) {
            result.write(buffer, 0, count);
        }

        assertArrayEquals(payload, result.toByteArray());
        assertNull(zis.getNextZipEntry());
        zis.close();
    }

    // Tests reading from an empty stream returns null entry
    @Test
    public void testGetNextZipEntry_emptyInputStream_returnsNull() throws IOException {
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        assertNull(zis.getNextZipEntry());
        zis.close();
    }

    // Helper method to create a ZIP containing a single STORED entry
    private byte[] createZipWithStoredEntry(String name, byte[] content) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        ZipArchiveEntry entry = new ZipArchiveEntry(name);
        entry.setMethod(ZipEntry.STORED);
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

    // Helper method to create a ZIP containing a single DEFLATED entry
    private byte[] createZipWithDeflatedEntry(String name, byte[] content) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        ZipArchiveEntry entry = new ZipArchiveEntry(name);
        entry.setMethod(ZipEntry.DEFLATED);
        zaos.putArchiveEntry(entry);
        zaos.write(content);
        zaos.closeArchiveEntry();
        zaos.close();
        return baos.toByteArray();
    }
}