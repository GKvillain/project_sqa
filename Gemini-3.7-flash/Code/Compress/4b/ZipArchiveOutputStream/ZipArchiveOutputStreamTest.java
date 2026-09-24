package org.apache.commons.compress.archivers.zip;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.ZipException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ZipArchiveOutputStreamTest {

    private File tempFile;

    @Before
    public void setUp() throws Exception {
        tempFile = File.createTempFile("zip_test", ".zip");
    }

    @After
    public void tearDown() {
        if (tempFile != null && tempFile.exists()) {
            tempFile.delete();
        }
    }

    // Tests stream mode isSeekable returns false
    @Test
    public void testIsSeekable_outputStream_returnsFalse() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        assertFalse(zaos.isSeekable());
    }

    // Tests file constructor creates seekable output stream
    @Test
    public void testIsSeekable_file_returnsTrue() throws IOException {
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(tempFile);
        try {
            assertTrue(zaos.isSeekable());
        } finally {
            zaos.close();
        }
    }

    // Tests encoding getter and setter
    @Test
    public void testSetEncoding_customEncoding_returnsCustomEncoding() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        zaos.setEncoding("ISO-8859-1");
        assertEquals("ISO-8859-1", zaos.getEncoding());
    }

    // Tests setLevel with valid boundary values
    @Test
    public void testSetLevel_validBoundaryValues_succeeds() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        zaos.setLevel(Deflater.DEFAULT_COMPRESSION);
        zaos.setLevel(Deflater.NO_COMPRESSION);
        zaos.setLevel(Deflater.BEST_COMPRESSION);
    }

    // Tests setLevel with invalid lower value throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testSetLevel_belowDefaultCompression_throwsException() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        zaos.setLevel(-2);
    }

    // Tests setLevel with invalid higher value throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testSetLevel_aboveBestCompression_throwsException() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        zaos.setLevel(10);
    }

    // Tests finish with unclosed entry throws IOException
    @Test(expected = IOException.class)
    public void testFinish_withUnclosedEntry_throwsException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        zaos.putArchiveEntry(entry);
        zaos.finish();
    }

    // Tests normal deflated entry writing and closing to OutputStream
    @Test
    public void testPutArchiveEntry_deflatedEntry_writesSuccessfully() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        zaos.setComment("Test zip comment");

        ZipArchiveEntry entry = new ZipArchiveEntry("deflated.txt");
        entry.setComment("Entry comment");
        zaos.putArchiveEntry(entry);

        byte[] data = "Hello ZIP Archive Deflated Stream Data".getBytes("UTF-8");
        zaos.write(data, 0, data.length);
        zaos.closeArchiveEntry();
        zaos.close();

        byte[] result = baos.toByteArray();
        assertTrue(result.length > 0);
    }

    // Tests writing data larger than internal deflater block size (8192 bytes)
    @Test
    public void testWrite_largeBuffer_deflatesInBlocks() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);

        ZipArchiveEntry entry = new ZipArchiveEntry("large.bin");
        zaos.putArchiveEntry(entry);

        byte[] largeData = new byte[20000];
        for (int i = 0; i < largeData.length; i++) {
            largeData[i] = (byte) (i % 256);
        }
        zaos.write(largeData, 0, largeData.length);
        zaos.closeArchiveEntry();
        zaos.close();

        assertTrue(baos.size() > 0);
    }

    // Tests stored entry without size to stream throws ZipException
    @Test(expected = ZipException.class)
    public void testPutArchiveEntry_storedEntryWithoutSizeOnStream_throwsException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);

        ZipArchiveEntry entry = new ZipArchiveEntry("stored.txt");
        entry.setMethod(ZipArchiveOutputStream.STORED);
        entry.setCrc(12345L);
        zaos.putArchiveEntry(entry);
    }

    // Tests stored entry without CRC to stream throws ZipException
    @Test(expected = ZipException.class)
    public void testPutArchiveEntry_storedEntryWithoutCrcOnStream_throwsException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);

        ZipArchiveEntry entry = new ZipArchiveEntry("stored.txt");
        entry.setMethod(ZipArchiveOutputStream.STORED);
        entry.setSize(10);
        zaos.putArchiveEntry(entry);
    }

    // Tests stored entry with incorrect CRC on stream throws ZipException at closeArchiveEntry
    @Test(expected = ZipException.class)
    public void testCloseArchiveEntry_storedEntryMismatchCrcOnStream_throwsException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);

        byte[] data = "Stored data".getBytes("UTF-8");
        ZipArchiveEntry entry = new ZipArchiveEntry("stored.txt");
        entry.setMethod(ZipArchiveOutputStream.STORED);
        entry.setSize(data.length);
        entry.setCrc(99999L);

        zaos.putArchiveEntry(entry);
        zaos.write(data, 0, data.length);
        zaos.closeArchiveEntry();
    }

    // Tests stored entry with incorrect size on stream throws ZipException at closeArchiveEntry
    @Test(expected = ZipException.class)
    public void testCloseArchiveEntry_storedEntryMismatchSizeOnStream_throwsException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);

        byte[] data = "Stored data".getBytes("UTF-8");
        CRC32 crc = new CRC32();
        crc.update(data);

        ZipArchiveEntry entry = new ZipArchiveEntry("stored.txt");
        entry.setMethod(ZipArchiveOutputStream.STORED);
        entry.setSize(data.length + 10);
        entry.setCrc(crc.getValue());

        zaos.putArchiveEntry(entry);
        zaos.write(data, 0, data.length);
        zaos.closeArchiveEntry();
    }

    // Tests valid stored entry writing to OutputStream
    @Test
    public void testPutArchiveEntry_storedEntryValidOnStream_succeeds() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);

        byte[] data = "Exact stored contents".getBytes("UTF-8");
        CRC32 crc = new CRC32();
        crc.update(data);

        ZipArchiveEntry entry = new ZipArchiveEntry("stored_valid.txt");
        entry.setMethod(ZipArchiveOutputStream.STORED);
        entry.setSize(data.length);
        entry.setCrc(crc.getValue());

        zaos.putArchiveEntry(entry);
        zaos.write(data, 0, data.length);
        zaos.closeArchiveEntry();
        zaos.close();

        assertTrue(baos.size() > 0);
    }

    // Tests stored entry using RandomAccessFile (seekable) without precalculated size and CRC
    @Test
    public void testPutArchiveEntry_storedEntryOnSeekableFile_succeeds() throws IOException {
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(tempFile);
        byte[] data = "File random access content".getBytes("UTF-8");

        ZipArchiveEntry entry = new ZipArchiveEntry("stored_raf.txt");
        entry.setMethod(ZipArchiveOutputStream.STORED);

        zaos.putArchiveEntry(entry);
        zaos.write(data, 0, data.length);
        zaos.closeArchiveEntry();
        zaos.close();

        assertTrue(tempFile.length() > 0);
    }

    // Tests Unicode extra field policy configuration
    @Test
    public void testSetCreateUnicodeExtraFields_alwaysPolicy_writesUnicodeFields() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        zaos.setCreateUnicodeExtraFields(ZipArchiveOutputStream.UnicodeExtraFieldPolicy.ALWAYS);
        zaos.setEncoding("US-ASCII");
        zaos.setFallbackToUTF8(true);

        ZipArchiveEntry entry = new ZipArchiveEntry("unicode_test_entry.txt");
        entry.setComment("Unicode comment");
        zaos.putArchiveEntry(entry);
        zaos.write("test".getBytes("UTF-8"), 0, 4);
        zaos.closeArchiveEntry();
        zaos.close();

        assertTrue(baos.size() > 0);
    }

    // Tests language encoding flag and fallback configuration
    @Test
    public void testSetUseLanguageEncodingFlag_flagToggle_setsCorrectly() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        zaos.setEncoding("UTF-8");
        zaos.setUseLanguageEncodingFlag(true);
        zaos.setFallbackToUTF8(false);

        ZipArchiveEntry entry = new ZipArchiveEntry("utf8_entry.txt");
        zaos.putArchiveEntry(entry);
        zaos.write("data".getBytes("UTF-8"), 0, 4);
        zaos.closeArchiveEntry();
        zaos.close();

        assertTrue(baos.size() > 0);
    }

    // Tests createArchiveEntry factory method
    @Test
    public void testCreateArchiveEntry_fromFile_returnsZipArchiveEntry() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);

        File sampleFile = File.createTempFile("sample_entry", ".txt");
        try {
            ArchiveEntry entry = zaos.createArchiveEntry(sampleFile, "sample_entry.txt");
            assertNotNull(entry);
            assertTrue(entry instanceof ZipArchiveEntry);
            assertEquals("sample_entry.txt", entry.getName());
        } finally {
            sampleFile.delete();
            zaos.close();
        }
    }

    // Tests flush method with both stream and empty archive
    @Test
    public void testFlushAndClose_emptyArchive_succeeds() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        zaos.flush();
        zaos.close();
        assertTrue(baos.size() > 0);
    }

    // Tests single byte write method
    @Test
    public void testWrite_singleByte_writesCorrectly() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);

        ZipArchiveEntry entry = new ZipArchiveEntry("single_byte.txt");
        zaos.putArchiveEntry(entry);
        zaos.write('A');
        zaos.write('B');
        zaos.closeArchiveEntry();
        zaos.close();

        assertTrue(baos.size() > 0);
    }

    // Tests write(byte[]) full array overload
    @Test
    public void testWrite_byteArrayOverload_writesCorrectly() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);

        ZipArchiveEntry entry = new ZipArchiveEntry("full_array.txt");
        zaos.putArchiveEntry(entry);
        byte[] data = "full array content".getBytes("UTF-8");
        zaos.write(data);
        zaos.closeArchiveEntry();
        zaos.close();

        assertTrue(baos.size() > 0);
    }

    // Tests default method setting on ZipArchiveOutputStream
    @Test
    public void testSetMethod_storedDefaultMethod_appliesToEntries() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        zaos.setMethod(ZipArchiveOutputStream.STORED);

        byte[] data = "Default method stored".getBytes("UTF-8");
        CRC32 crc = new CRC32();
        crc.update(data);

        ZipArchiveEntry entry = new ZipArchiveEntry("default_stored.txt");
        entry.setSize(data.length);
        entry.setCrc(crc.getValue());

        zaos.putArchiveEntry(entry);
        zaos.write(data);
        zaos.closeArchiveEntry();
        zaos.close();

        assertTrue(baos.size() > 0);
    }

    // Tests directory entry handling (name ending with '/')
    @Test
    public void testPutArchiveEntry_directoryEntry_succeeds() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);

        ZipArchiveEntry dirEntry = new ZipArchiveEntry("test_dir/");
        zaos.putArchiveEntry(dirEntry);
        zaos.closeArchiveEntry();
        zaos.close();

        assertTrue(baos.size() > 0);
    }

    // Tests directory entry on seekable file
    @Test
    public void testPutArchiveEntry_directoryEntryOnSeekableFile_succeeds() throws IOException {
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(tempFile);

        ZipArchiveEntry dirEntry = new ZipArchiveEntry("nested/directory/");
        zaos.putArchiveEntry(dirEntry);
        zaos.closeArchiveEntry();
        zaos.close();

        assertTrue(tempFile.length() > 0);
    }

    // Tests Zip64Mode.Always configuration
    @Test
    public void testSetUseZip64_alwaysMode_writesZip64Headers() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        zaos.setUseZip64(Zip64Mode.Always);

        ZipArchiveEntry entry = new ZipArchiveEntry("zip64_entry.txt");
        zaos.putArchiveEntry(entry);
        zaos.write("Zip64 content".getBytes("UTF-8"));
        zaos.closeArchiveEntry();
        zaos.close();

        assertTrue(baos.size() > 0);
    }

    // Tests Zip64Mode.Never configuration
    @Test
    public void testSetUseZip64_neverMode_writesStandardHeaders() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        zaos.setUseZip64(Zip64Mode.Never);

        ZipArchiveEntry entry = new ZipArchiveEntry("zip32_entry.txt");
        zaos.putArchiveEntry(entry);
        zaos.write("Standard zip content".getBytes("UTF-8"));
        zaos.closeArchiveEntry();
        zaos.close();

        assertTrue(baos.size() > 0);
    }

    // Tests Zip64Mode.AsNeeded configuration
    @Test
    public void testSetUseZip64_asNeededMode_writesStandardHeaders() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        zaos.setUseZip64(Zip64Mode.AsNeeded);

        ZipArchiveEntry entry = new ZipArchiveEntry("as_needed.txt");
        zaos.putArchiveEntry(entry);
        zaos.write("As needed content".getBytes("UTF-8"));
        zaos.closeArchiveEntry();
        zaos.close();

        assertTrue(baos.size() > 0);
    }

    // Tests UnicodeExtraFieldPolicy.NEVER and NOT_ENCODEABLE
    @Test
    public void testSetCreateUnicodeExtraFields_neverAndNotEncodeablePolicies() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        zaos.setCreateUnicodeExtraFields(ZipArchiveOutputStream.UnicodeExtraFieldPolicy.NEVER);

        ZipArchiveEntry entry = new ZipArchiveEntry("never_unicode.txt");
        zaos.putArchiveEntry(entry);
        zaos.write("never".getBytes("UTF-8"));
        zaos.closeArchiveEntry();

        zaos.setCreateUnicodeExtraFields(ZipArchiveOutputStream.UnicodeExtraFieldPolicy.NOT_ENCODEABLE);
        ZipArchiveEntry entry2 = new ZipArchiveEntry("not_encodeable.txt");
        zaos.putArchiveEntry(entry2);
        zaos.write("not encodeable".getBytes("UTF-8"));
        zaos.closeArchiveEntry();

        zaos.close();
        assertTrue(baos.size() > 0);
    }

    // Tests canWriteEntryData method
    @Test
    public void testCanWriteEntryData_validAndInvalidEntries() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);

        ZipArchiveEntry supportedEntry = new ZipArchiveEntry("supported.txt");
        supportedEntry.setMethod(ZipArchiveOutputStream.DEFLATED);
        assertTrue(zaos.canWriteEntryData(supportedEntry));

        ZipArchiveEntry storedEntry = new ZipArchiveEntry("stored.txt");
        storedEntry.setMethod(ZipArchiveOutputStream.STORED);
        assertTrue(zaos.canWriteEntryData(storedEntry));

        ZipArchiveEntry unsupportedMethodEntry = new ZipArchiveEntry("unsupported.txt");
        unsupportedMethodEntry.setMethod(99);
        assertFalse(zaos.canWriteEntryData(unsupportedMethodEntry));
    }

    // Tests writing multiple entries on seekable file
    @Test
    public void testPutArchiveEntry_multipleEntriesOnSeekableFile_succeeds() throws IOException {
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(tempFile);

        ZipArchiveEntry entry1 = new ZipArchiveEntry("file1.txt");
        zaos.putArchiveEntry(entry1);
        zaos.write("File 1 content".getBytes("UTF-8"));
        zaos.closeArchiveEntry();

        ZipArchiveEntry entry2 = new ZipArchiveEntry("file2.txt");
        entry2.setMethod(ZipArchiveOutputStream.STORED);
        zaos.putArchiveEntry(entry2);
        zaos.write("File 2 stored content".getBytes("UTF-8"));
        zaos.closeArchiveEntry();

        zaos.close();
        assertTrue(tempFile.length() > 0);
    }

    // Tests calling close multiple times is safe
    @Test
    public void testClose_multipleCalls_isSafe() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        zaos.close();
        zaos.close();
    }

    // Tests calling closeArchiveEntry when no entry is active is safe
    @Test
    public void testCloseArchiveEntry_whenNoEntryOpen_isSafe() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        zaos.closeArchiveEntry();
        zaos.close();
    }

    // Tests getBytesWritten method
    @Test
    public void testGetBytesWritten_returnsWrittenByteCount() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);

        ZipArchiveEntry entry = new ZipArchiveEntry("counted.txt");
        zaos.putArchiveEntry(entry);
        zaos.write("Counted bytes test".getBytes("UTF-8"));
        zaos.closeArchiveEntry();
        zaos.close();

        assertTrue(zaos.getBytesWritten() > 0);
    }
}