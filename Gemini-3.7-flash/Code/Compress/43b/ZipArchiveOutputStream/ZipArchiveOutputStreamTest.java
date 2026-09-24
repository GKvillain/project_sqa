package org.apache.commons.compress.archivers.zip;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.ZipException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ZipArchiveOutputStreamTest {

    private File tempFile;

    @Before
    public void setUp() throws Exception {
        tempFile = File.createTempFile("zip-test-", ".zip");
    }

    @After
    public void tearDown() {
        if (tempFile != null && tempFile.exists()) {
            tempFile.delete();
        }
    }

    // Tests isSeekable for OutputStream constructor returns false
    @Test
    public void testIsSeekable_outputStream_returnsFalse() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        assertFalse(zaos.isSeekable());
    }

    // Tests isSeekable for File constructor returns true
    @Test
    public void testIsSeekable_file_returnsTrue() throws IOException {
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(tempFile);
        try {
            assertTrue(zaos.isSeekable());
        } finally {
            zaos.destroy();
        }
    }

    // Tests setting an invalid compression level throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testSetLevel_invalidLevel_throwsIllegalArgumentException() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        zaos.setLevel(15);
    }

    // Tests setting valid compression level and method
    @Test
    public void testSetLevelAndMethod_validParameters_success() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        zaos.setLevel(Deflater.BEST_COMPRESSION);
        zaos.setMethod(ZipArchiveOutputStream.DEFLATED);
        zaos.setComment("test archive comment");
    }

    // Tests getEncoding and setEncoding
    @Test
    public void testSetEncoding_customEncoding_returnsConfiguredEncoding() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        zaos.setEncoding("ISO-8859-1");
        assertEquals("ISO-8859-1", zaos.getEncoding());
    }

    // Tests writing when no entry is open throws IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testWrite_noOpenEntry_throwsIllegalStateException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        zaos.write(new byte[]{1, 2, 3}, 0, 3);
    }

    // Tests closing an archive entry when none is open throws IOException
    @Test(expected = IOException.class)
    public void testCloseArchiveEntry_noCurrentEntry_throwsIOException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        zaos.closeArchiveEntry();
    }

    // Tests finishing stream with unclosed entry throws IOException
    @Test(expected = IOException.class)
    public void testFinish_unclosedEntry_throwsIOException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        ZipArchiveEntry entry = new ZipArchiveEntry("entry.txt");
        zaos.putArchiveEntry(entry);
        zaos.finish();
    }

    // Tests calling finish twice throws IOException
    @Test(expected = IOException.class)
    public void testFinish_alreadyFinished_throwsIOException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        zaos.finish();
        zaos.finish();
    }

    // Tests STORED entry without size on non-seekable stream throws ZipException
    @Test(expected = ZipException.class)
    public void testPutArchiveEntry_storedWithoutSize_throwsZipException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        ZipArchiveEntry entry = new ZipArchiveEntry("stored.txt");
        entry.setMethod(ZipArchiveOutputStream.STORED);
        zaos.putArchiveEntry(entry);
    }

    // Tests STORED entry without CRC on non-seekable stream throws ZipException
    @Test(expected = ZipException.class)
    public void testPutArchiveEntry_storedWithoutCrc_throwsZipException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        ZipArchiveEntry entry = new ZipArchiveEntry("stored.txt");
        entry.setMethod(ZipArchiveOutputStream.STORED);
        entry.setSize(4);
        zaos.putArchiveEntry(entry);
    }

    // Tests STORED entry with bad CRC during write throws ZipException
    @Test(expected = ZipException.class)
    public void testCloseArchiveEntry_storedBadCrc_throwsZipException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        ZipArchiveEntry entry = new ZipArchiveEntry("stored.txt");
        entry.setMethod(ZipArchiveOutputStream.STORED);
        entry.setSize(4);
        entry.setCrc(12345L);
        zaos.putArchiveEntry(entry);
        zaos.write(new byte[]{1, 2, 3, 4}, 0, 4);
        zaos.closeArchiveEntry();
    }

    // Tests normal deflated entry workflow to ByteArrayOutputStream
    @Test
    public void testPutAndCloseArchiveEntry_deflated_success() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        zaos.putArchiveEntry(entry);
        byte[] data = "Hello, Compress!".getBytes("UTF-8");
        zaos.write(data, 0, data.length);
        zaos.closeArchiveEntry();
        zaos.close();

        assertTrue(baos.toByteArray().length > 0);
    }

    // Tests stored entry workflow with seekable channel (File)
    @Test
    public void testPutAndCloseArchiveEntry_storedWithSeekable_success() throws IOException {
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(tempFile);
        ZipArchiveEntry entry = new ZipArchiveEntry("stored-seekable.txt");
        entry.setMethod(ZipArchiveOutputStream.STORED);
        zaos.putArchiveEntry(entry);
        byte[] data = "Stored in file".getBytes("UTF-8");
        zaos.write(data, 0, data.length);
        zaos.closeArchiveEntry();
        zaos.close();

        assertTrue(tempFile.length() > 0);
    }

    // Tests createArchiveEntry from File
    @Test
    public void testCreateArchiveEntry_validFile_returnsCorrectEntry() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        ArchiveEntry entry = zaos.createArchiveEntry(tempFile, "tempEntry.txt");
        assertNotNull(entry);
        assertEquals("tempEntry.txt", entry.getName());
        zaos.close();
    }

    // Tests createArchiveEntry after finished throws IOException
    @Test(expected = IOException.class)
    public void testCreateArchiveEntry_afterFinished_throwsIOException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        zaos.finish();
        zaos.createArchiveEntry(tempFile, "tempEntry.txt");
    }

    // Tests canWriteEntryData for supported vs unsupported methods
    @Test
    public void testCanWriteEntryData_variousMethods() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);

        ZipArchiveEntry deflatedEntry = new ZipArchiveEntry("def.txt");
        deflatedEntry.setMethod(ZipArchiveOutputStream.DEFLATED);
        assertTrue(zaos.canWriteEntryData(deflatedEntry));

        ZipArchiveEntry storedEntry = new ZipArchiveEntry("stored.txt");
        storedEntry.setMethod(ZipArchiveOutputStream.STORED);
        assertTrue(zaos.canWriteEntryData(storedEntry));

        ZipArchiveEntry unshrinkEntry = new ZipArchiveEntry("unshrink.txt");
        unshrinkEntry.setMethod(ZipMethod.UNSHRINKING.getCode());
        assertFalse(zaos.canWriteEntryData(unshrinkEntry));
    }

    // Tests adding raw archive entry
    @Test
    public void testAddRawArchiveEntry_validRawStream_success() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);

        ZipArchiveEntry entry = new ZipArchiveEntry("raw.txt");
        byte[] content = "Raw Content".getBytes("UTF-8");
        CRC32 crc = new CRC32();
        crc.update(content);
        entry.setSize(content.length);
        entry.setCompressedSize(content.length);
        entry.setCrc(crc.getValue());
        entry.setMethod(ZipArchiveOutputStream.STORED);

        ByteArrayInputStream rawIn = new ByteArrayInputStream(content);
        zaos.addRawArchiveEntry(entry, rawIn);
        zaos.close();

        assertTrue(baos.toByteArray().length > 0);
    }

    // Tests Unicode extra field creation policy ALWAYS
    @Test
    public void testSetCreateUnicodeExtraFields_always_addsUnicodeExtra() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        zaos.setCreateUnicodeExtraFields(ZipArchiveOutputStream.UnicodeExtraFieldPolicy.ALWAYS);

        ZipArchiveEntry entry = new ZipArchiveEntry("test-unicode.txt");
        entry.setComment("test comment");
        zaos.putArchiveEntry(entry);
        zaos.write(new byte[]{1, 2, 3}, 0, 3);
        zaos.closeArchiveEntry();
        zaos.close();

        assertNotNull(entry.getExtraField(UnicodePathExtraField.UPATH_ID));
        assertNotNull(entry.getExtraField(UnicodeCommentExtraField.UCOM_ID));
    }

    // Tests Zip64Mode.Never throws exception when entry size exceeds limit
    @Test(expected = Zip64RequiredException.class)
    public void testSetUseZip64_neverModeTooBig_throwsZip64RequiredException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        zaos.setUseZip64(Zip64Mode.Never);

        ZipArchiveEntry entry = new ZipArchiveEntry("huge.txt");
        entry.setSize(ZipConstants.ZIP64_MAGIC + 1L);
        zaos.putArchiveEntry(entry);
    }

    // Tests ResourceAlignmentExtraField in ZipArchiveOutputStream
    @Test
    public void testPutArchiveEntry_withAlignment_addsAlignmentField() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);

        ZipArchiveEntry entry = new ZipArchiveEntry("aligned.txt");
        entry.setAlignment(4);
        zaos.putArchiveEntry(entry);
        zaos.write(new byte[]{1, 2}, 0, 2);
        zaos.closeArchiveEntry();
        zaos.close();

        assertNotNull(entry.getExtraField(ResourceAlignmentExtraField.ID));
    }

    // Tests language encoding flag configuration and fallback to UTF-8
    @Test
    public void testSetUseLanguageEncodingFlagAndFallbackToUTF8() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        zaos.setUseLanguageEncodingFlag(true);
        zaos.setFallbackToUTF8(true);
        zaos.setUseLanguageEncodingFlag(false);
        zaos.setFallbackToUTF8(false);
    }

    // Tests writing directory entry
    @Test
    public void testDirectoryEntry_success() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);

        ZipArchiveEntry dirEntry = new ZipArchiveEntry("directory/");
        zaos.putArchiveEntry(dirEntry);
        zaos.closeArchiveEntry();
        zaos.close();

        assertTrue(baos.toByteArray().length > 0);
    }

    // Tests Zip64Mode.Always forces Zip64 extra field creation even for small files
    @Test
    public void testZip64Mode_always_createsZip64ExtraField() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        zaos.setUseZip64(Zip64Mode.Always);

        ZipArchiveEntry entry = new ZipArchiveEntry("small-zip64.txt");
        zaos.putArchiveEntry(entry);
        byte[] data = "zip64 content".getBytes("UTF-8");
        zaos.write(data, 0, data.length);
        zaos.closeArchiveEntry();
        zaos.close();

        assertNotNull(entry.getExtraField(Zip64ExtendedInformationExtraField.HEADER_ID));
    }

    // Tests writePreamble before any entries
    @Test
    public void testWritePreamble_success() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        byte[] preamble = "PreAmbleData".getBytes("UTF-8");
        zaos.writePreamble(preamble);

        ZipArchiveEntry entry = new ZipArchiveEntry("afterPreamble.txt");
        zaos.putArchiveEntry(entry);
        zaos.write(new byte[]{1, 2, 3});
        zaos.closeArchiveEntry();
        zaos.close();

        byte[] result = baos.toByteArray();
        assertTrue(result.length > preamble.length);
    }

    // Tests writePreamble after an entry is put throws IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testWritePreamble_afterEntryPut_throwsIllegalStateException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        ZipArchiveEntry entry = new ZipArchiveEntry("entry.txt");
        zaos.putArchiveEntry(entry);
        zaos.writePreamble(new byte[]{1, 2, 3});
    }

    // Tests getBytesWritten method
    @Test
    public void testGetBytesWritten() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        assertEquals(0, zaos.getBytesWritten());

        ZipArchiveEntry entry = new ZipArchiveEntry("bytesWrittenTest.txt");
        zaos.putArchiveEntry(entry);
        zaos.write(new byte[]{10, 20, 30});
        zaos.closeArchiveEntry();
        zaos.close();

        assertTrue(zaos.getBytesWritten() > 0);
    }

    // Tests flush method
    @Test
    public void testFlush_success() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        ZipArchiveEntry entry = new ZipArchiveEntry("flushTest.txt");
        zaos.putArchiveEntry(entry);
        zaos.write(new byte[]{1, 2, 3});
        zaos.flush();
        zaos.closeArchiveEntry();
        zaos.close();
    }

    // Tests UnicodeExtraFieldPolicy.NOT_ENCODEABLE with ASCII name
    @Test
    public void testSetCreateUnicodeExtraFields_notEncodeable_ascii_noUnicodeExtra() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        zaos.setEncoding("US-ASCII");
        zaos.setCreateUnicodeExtraFields(ZipArchiveOutputStream.UnicodeExtraFieldPolicy.NOT_ENCODEABLE);

        ZipArchiveEntry entry = new ZipArchiveEntry("ascii.txt");
        zaos.putArchiveEntry(entry);
        zaos.write(new byte[]{1});
        zaos.closeArchiveEntry();
        zaos.close();

        assertNull(entry.getExtraField(UnicodePathExtraField.UPATH_ID));
    }

    // Tests UnicodeExtraFieldPolicy.NEVER
    @Test
    public void testSetCreateUnicodeExtraFields_never() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        zaos.setCreateUnicodeExtraFields(ZipArchiveOutputStream.UnicodeExtraFieldPolicy.NEVER);

        ZipArchiveEntry entry = new ZipArchiveEntry("test-never.txt");
        entry.setComment("comment");
        zaos.putArchiveEntry(entry);
        zaos.write(new byte[]{1});
        zaos.closeArchiveEntry();
        zaos.close();

        assertNull(entry.getExtraField(UnicodePathExtraField.UPATH_ID));
        assertNull(entry.getExtraField(UnicodeCommentExtraField.UCOM_ID));
    }

    // Tests STORED entry on non-seekable stream with all metadata pre-populated
    @Test
    public void testPutArchiveEntry_storedNonSeekable_allSpecified_success() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);

        byte[] data = new byte[]{1, 2, 3, 4, 5};
        CRC32 crc = new CRC32();
        crc.update(data);

        ZipArchiveEntry entry = new ZipArchiveEntry("stored-full.txt");
        entry.setMethod(ZipArchiveOutputStream.STORED);
        entry.setSize(data.length);
        entry.setCompressedSize(data.length);
        entry.setCrc(crc.getValue());

        zaos.putArchiveEntry(entry);
        zaos.write(data, 0, data.length);
        zaos.closeArchiveEntry();
        zaos.close();

        assertTrue(baos.toByteArray().length > 0);
    }

    // Tests addRawArchiveEntry with DEFLATED method
    @Test
    public void testAddRawArchiveEntry_deflated_success() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);

        byte[] rawDeflatedData = new byte[]{120, -100, 99, 100, 98, 6, 0, 0, 52, 0, 7};
        ZipArchiveEntry entry = new ZipArchiveEntry("rawDeflated.txt");
        entry.setMethod(ZipArchiveOutputStream.DEFLATED);
        entry.setSize(3);
        entry.setCompressedSize(rawDeflatedData.length);
        entry.setCrc(0x12345678L);

        ByteArrayInputStream rawIn = new ByteArrayInputStream(rawDeflatedData);
        zaos.addRawArchiveEntry(entry, rawIn);
        zaos.close();

        assertTrue(baos.toByteArray().length > 0);
    }

    // Tests closing stream multiple times does not throw exception
    @Test
    public void testClose_multipleTimes_noException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        zaos.close();
        zaos.close();
    }
}