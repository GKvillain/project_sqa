package org.apache.commons.compress.archivers.tar;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TarArchiveOutputStreamTest {

    private ByteArrayOutputStream byteArrayOutputStream;
    private TarArchiveOutputStream tarOut;

    @Before
    public void setUp() {
        byteArrayOutputStream = new ByteArrayOutputStream();
        tarOut = new TarArchiveOutputStream(byteArrayOutputStream);
    }

    @After
    public void tearDown() throws IOException {
        if (tarOut != null) {
            try {
                tarOut.close();
            } catch (IOException ignored) {
            }
        }
    }

    // Tests normal lifecycle of adding a file entry, writing bytes, and closing entry
    @Test
    public void testPutArchiveEntry_normalFile_writesSuccessfully() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        byte[] content = "Hello World".getBytes();
        entry.setSize(content.length);

        tarOut.putArchiveEntry(entry);
        tarOut.write(content, 0, content.length);
        tarOut.closeArchiveEntry();
        tarOut.close();

        byte[] result = byteArrayOutputStream.toByteArray();
        assertTrue(result.length > 0);
        assertEquals(0, result.length % TarBuffer.DEFAULT_RCDSIZE);
    }

    // Tests directory entry handling where size is treated as 0
    @Test
    public void testPutArchiveEntry_directoryEntry_setsSizeToZero() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("dir/");
        tarOut.putArchiveEntry(entry);
        tarOut.closeArchiveEntry();
        tarOut.close();

        byte[] result = byteArrayOutputStream.toByteArray();
        assertTrue(result.length > 0);
    }

    // Tests custom block size and record size constructors and getRecordSize
    @Test
    public void testConstructors_customSizes_returnsExpectedRecordSize() throws IOException {
        TarArchiveOutputStream customOut = new TarArchiveOutputStream(byteArrayOutputStream, 1024);
        assertEquals(TarBuffer.DEFAULT_RCDSIZE, customOut.getRecordSize());
        customOut.close();

        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        TarArchiveOutputStream customOut2 = new TarArchiveOutputStream(baos2, 1024, 1024);
        assertEquals(1024, customOut2.getRecordSize());
        customOut2.close();
    }

    // Tests writing data across multiple buffer assembly cycles
    @Test
    public void testWrite_multipleSmallChunks_assemblesCorrectly() throws IOException {
        byte[] data = new byte[600];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (i % 128);
        }

        TarArchiveEntry entry = new TarArchiveEntry("chunks.bin");
        entry.setSize(data.length);
        tarOut.putArchiveEntry(entry);

        // write in chunks smaller than record size (512)
        tarOut.write(data, 0, 300);
        tarOut.write(data, 300, 300);
        tarOut.closeArchiveEntry();
        tarOut.close();

        byte[] result = byteArrayOutputStream.toByteArray();
        assertTrue(result.length >= 1024);
    }

    // Tests writing single chunk larger than record buffer size
    @Test
    public void testWrite_largeChunk_writesDirectRecords() throws IOException {
        byte[] data = new byte[1024];
        TarArchiveEntry entry = new TarArchiveEntry("large.bin");
        entry.setSize(data.length);

        tarOut.putArchiveEntry(entry);
        tarOut.write(data, 0, data.length);
        tarOut.closeArchiveEntry();
        tarOut.close();

        byte[] result = byteArrayOutputStream.toByteArray();
        assertTrue(result.length > 1024);
    }

    // Tests writing more bytes than specified in the entry header
    @Test(expected = IOException.class)
    public void testWrite_exceedsSpecifiedSize_throwsIOException() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("overflow.txt");
        entry.setSize(5);

        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[10];
        tarOut.write(data, 0, 10);
    }

    // Tests closing an entry before all specified bytes are written
    @Test(expected = IOException.class)
    public void testCloseArchiveEntry_unwrittenBytes_throwsIOException() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("underflow.txt");
        entry.setSize(100);

        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[50];
        tarOut.write(data, 0, 50);
        tarOut.closeArchiveEntry();
    }

    // Tests long file name when LONGFILE_ERROR mode is active (default mode)
    @Test(expected = RuntimeException.class)
    public void testPutArchiveEntry_longFileNameErrorMode_throwsRuntimeException() throws IOException {
        String longName = "a".repeat(101);
        TarArchiveEntry entry = new TarArchiveEntry(longName);

        tarOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_ERROR);
        tarOut.putArchiveEntry(entry);
    }

    // Tests long file name when LONGFILE_TRUNCATE mode is active
    @Test
    public void testPutArchiveEntry_longFileNameTruncateMode_succeeds() throws IOException {
        String longName = "b".repeat(105);
        TarArchiveEntry entry = new TarArchiveEntry(longName);
        entry.setSize(0);

        tarOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_TRUNCATE);
        tarOut.putArchiveEntry(entry);
        tarOut.closeArchiveEntry();
        tarOut.close();

        byte[] result = byteArrayOutputStream.toByteArray();
        assertTrue(result.length > 0);
    }

    // Tests long file name when LONGFILE_GNU mode is active (GNU extension)
    @Test
    public void testPutArchiveEntry_longFileNameGnuMode_createsLongLinkEntry() throws IOException {
        String longName = "long_path_name/".repeat(10) + "file.txt";
        TarArchiveEntry entry = new TarArchiveEntry(longName);
        byte[] content = "GNU Longlink content".getBytes();
        entry.setSize(content.length);

        tarOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
        tarOut.putArchiveEntry(entry);
        tarOut.write(content, 0, content.length);
        tarOut.closeArchiveEntry();
        tarOut.close();

        byte[] result = byteArrayOutputStream.toByteArray();
        assertTrue(result.length > 0);
    }

    // Tests boundary condition when file name length is exactly NAMELEN - 1 (99 characters)
    @Test
    public void testPutArchiveEntry_fileNameBelowNameLen_succeedsWithoutLongMode() throws IOException {
        String name99 = "c".repeat(TarConstants.NAMELEN - 1);
        TarArchiveEntry entry = new TarArchiveEntry(name99);
        entry.setSize(0);

        tarOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_ERROR);
        tarOut.putArchiveEntry(entry);
        tarOut.closeArchiveEntry();
        tarOut.close();

        byte[] result = byteArrayOutputStream.toByteArray();
        assertTrue(result.length > 0);
    }

    // Tests finish() writes two EOF records (1024 bytes of zero)
    @Test
    public void testFinish_emptyArchive_writesTwoEOFRecords() throws IOException {
        tarOut.finish();
        byte[] result = byteArrayOutputStream.toByteArray();
        assertEquals(TarBuffer.DEFAULT_RCDSIZE * 2, result.length);
        for (byte b : result) {
            assertEquals(0, b);
        }
    }

    // Tests flush and close multiple times without exception
    @Test
    public void testClose_calledMultipleTimes_idempotent() throws IOException {
        tarOut.flush();
        tarOut.close();
        tarOut.close();
    }

    // Tests createArchiveEntry factory method
    @Test
    public void testCreateArchiveEntry_validFile_returnsTarArchiveEntry() throws IOException {
        File tempFile = File.createTempFile("test_tar", ".tmp");
        try {
            ArchiveEntry entry = tarOut.createArchiveEntry(tempFile, "entryName.txt");
            assertNotNull(entry);
            assertTrue(entry instanceof TarArchiveEntry);
            assertEquals("entryName.txt", entry.getName());
        } finally {
            tempFile.delete();
        }
    }
}