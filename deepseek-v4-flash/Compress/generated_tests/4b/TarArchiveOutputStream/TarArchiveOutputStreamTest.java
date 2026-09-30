package org.apache.commons.compress.archivers.tar;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import org.junit.Test;

public class TarArchiveOutputStreamTest {

    // Helper to generate a name longer than NAMELEN (100)
    private String generateLongName() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < TarConstants.NAMELEN + 1; i++) {
            sb.append('a');
        }
        return sb.toString();
    }

    // Test default constructor
    @Test
    public void testConstructor_default_shouldCreateStream() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (TarArchiveOutputStream tos = new TarArchiveOutputStream(baos)) {
            assertNotNull(tos);
            assertEquals(512, tos.getRecordSize());
        }
    }

    // Test constructor with block size
    @Test
    public void testConstructor_withBlockSize_shouldCreateStream() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (TarArchiveOutputStream tos = new TarArchiveOutputStream(baos, 2048)) {
            assertNotNull(tos);
            assertEquals(512, tos.getRecordSize());
        }
    }

    // Test setLongFileMode
    @Test
    public void testSetLongFileMode_gnu_shouldSetMode() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (TarArchiveOutputStream tos = new TarArchiveOutputStream(baos)) {
            tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
            // no direct assertion, but subsequent behavior changes
        }
    }

    // Normal put and close entry
    @Test
    public void testPutAndCloseEntry_normalEntry_shouldSucceed() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (TarArchiveOutputStream tos = new TarArchiveOutputStream(baos)) {
            TarArchiveEntry entry = new TarArchiveEntry("file.txt");
            entry.setSize(10);
            tos.putArchiveEntry(entry);
            tos.write("1234567890".getBytes());
            tos.closeArchiveEntry();
        }
        assertTrue(baos.size() > 0);
    }

    // Write exceeding entry size -> exception
    @Test(expected = IOException.class)
    public void testWrite_exceedsEntrySize_throwsIOException() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (TarArchiveOutputStream tos = new TarArchiveOutputStream(baos)) {
            TarArchiveEntry entry = new TarArchiveEntry("file.txt");
            entry.setSize(5);
            tos.putArchiveEntry(entry);
            tos.write("1234567890".getBytes());
        }
    }

    // Close entry with less data than header size -> exception
    @Test(expected = IOException.class)
    public void testCloseArchiveEntry_underflow_throwsIOException() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (TarArchiveOutputStream tos = new TarArchiveOutputStream(baos)) {
            TarArchiveEntry entry = new TarArchiveEntry("file.txt");
            entry.setSize(10);
            tos.putArchiveEntry(entry);
            tos.write("12345".getBytes());
            tos.closeArchiveEntry();
        }
    }

    // Long file name with LONGFILE_ERROR (default) -> RuntimeException
    @Test(expected = RuntimeException.class)
    public void testPutArchiveEntry_longNameErrorMode_throwsRuntimeException() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (TarArchiveOutputStream tos = new TarArchiveOutputStream(baos)) {
            TarArchiveEntry entry = new TarArchiveEntry(generateLongName());
            entry.setSize(0);
            tos.putArchiveEntry(entry);
        }
    }

    // Long file name with LONGFILE_TRUNCATE -> succeeds
    @Test
    public void testPutArchiveEntry_longNameTruncateMode_shouldSucceed() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (TarArchiveOutputStream tos = new TarArchiveOutputStream(baos)) {
            tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_TRUNCATE);
            TarArchiveEntry entry = new TarArchiveEntry(generateLongName());
            entry.setSize(0);
            tos.putArchiveEntry(entry);
            tos.closeArchiveEntry();
        }
    }

    // Long file name with LONGFILE_GNU -> succeeds and writes long link entry
    @Test
    public void testPutArchiveEntry_longNameGnuMode_shouldSucceed() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (TarArchiveOutputStream tos = new TarArchiveOutputStream(baos)) {
            tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
            TarArchiveEntry entry = new TarArchiveEntry(generateLongName());
            entry.setSize(10);
            tos.putArchiveEntry(entry);
            tos.write("1234567890".getBytes());
            tos.closeArchiveEntry();
        }
        assertTrue(baos.size() > 0);
    }

    // Directory entry (isDirectory() == true) -> currSize set to 0
    @Test
    public void testPutArchiveEntry_directoryEntry_shouldSetCurrSizeZero() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (TarArchiveOutputStream tos = new TarArchiveOutputStream(baos)) {
            // name ending with "/" marks entry as directory
            TarArchiveEntry entry = new TarArchiveEntry("dirname/");
            assertTrue(entry.isDirectory());
            tos.putArchiveEntry(entry);
            tos.closeArchiveEntry();
        }
    }

    // Write zero bytes -> no exception
    @Test
    public void testWrite_zeroBytes_shouldNotThrow() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (TarArchiveOutputStream tos = new TarArchiveOutputStream(baos)) {
            TarArchiveEntry entry = new TarArchiveEntry("file.txt");
            entry.setSize(0);
            tos.putArchiveEntry(entry);
            tos.write(new byte[0]);
            tos.closeArchiveEntry();
        }
    }

    // Write multiple small chunks to exercise assembly buffer
    @Test
    public void testWrite_multipleSmallWrites_shouldAssembleRecords() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (TarArchiveOutputStream tos = new TarArchiveOutputStream(baos)) {
            TarArchiveEntry entry = new TarArchiveEntry("file.txt");
            int size = 1500;
            entry.setSize(size);
            tos.putArchiveEntry(entry);
            byte[] data = new byte[size];
            for (int i = 0; i < size; i++) data[i] = (byte) (i % 256);
            int offset = 0;
            while (offset < size) {
                int len = Math.min(100, size - offset);
                tos.write(data, offset, len);
                offset += len;
            }
            tos.closeArchiveEntry();
        }
    }

    // Write exactly one record size (512) -> full record written
    @Test
    public void testWrite_exactRecordSize_shouldWriteFullRecord() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (TarArchiveOutputStream tos = new TarArchiveOutputStream(baos)) {
            TarArchiveEntry entry = new TarArchiveEntry("file.txt");
            int size = 512;
            entry.setSize(size);
            tos.putArchiveEntry(entry);
            byte[] data = new byte[size];
            for (int i = 0; i < size; i++) data[i] = (byte) i;
            tos.write(data);
            tos.closeArchiveEntry();
        }
    }

    // Close entry with assembly buffer left (size < recordSize) -> flush and succeed
    @Test
    public void testCloseArchiveEntry_withAssemblyLeft_shouldFlushAndSucceed() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (TarArchiveOutputStream tos = new TarArchiveOutputStream(baos)) {
            TarArchiveEntry entry = new TarArchiveEntry("file.txt");
            int size = 300; // less than recordSize
            entry.setSize(size);
            tos.putArchiveEntry(entry);
            byte[] data = new byte[size];
            for (int i = 0; i < size; i++) data[i] = (byte) i;
            tos.write(data);
            // data stays in assembly buffer, will be flushed on closeArchiveEntry
            tos.closeArchiveEntry();
        }
    }

    // finish() with no unclosed entry -> writes EOF records
    @Test
    public void testFinish_noUnclosedEntry_shouldWriteEOFRecords() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (TarArchiveOutputStream tos = new TarArchiveOutputStream(baos)) {
            TarArchiveEntry entry = new TarArchiveEntry("file.txt");
            entry.setSize(0);
            tos.putArchiveEntry(entry);
            tos.closeArchiveEntry();
            tos.finish();
        }
    }

    // finish() with unclosed entry -> throws IOException
    @Test(expected = IOException.class)
    public void testFinish_withUnclosedEntry_throwsIOException() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (TarArchiveOutputStream tos = new TarArchiveOutputStream(baos)) {
            TarArchiveEntry entry = new TarArchiveEntry("file.txt");
            entry.setSize(0);
            tos.putArchiveEntry(entry);
            // entry not closed
            tos.finish();
        }
    }

    // flush() should not throw
    @Test
    public void testFlush_shouldSucceed() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (TarArchiveOutputStream tos = new TarArchiveOutputStream(baos)) {
            tos.flush();
        }
    }

    // createArchiveEntry returns a TarArchiveEntry
    @Test
    public void testCreateArchiveEntry_shouldReturnTarArchiveEntry() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (TarArchiveOutputStream tos = new TarArchiveOutputStream(baos)) {
            File tempFile = File.createTempFile("test", ".txt");
            tempFile.deleteOnExit();
            org.apache.commons.compress.archivers.ArchiveEntry ae = tos.createArchiveEntry(tempFile, "entryname.txt");
            assertNotNull(ae);
            assertTrue(ae instanceof TarArchiveEntry);
            assertEquals("entryname.txt", ae.getName());
        }
    }

    // ========== New test cases to cover missing coverage ==========

    // Test constructor with block size and record size
    @Test
    public void testConstructor_withBlockSizeAndRecordSize_shouldSetRecordSize() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (TarArchiveOutputStream tos = new TarArchiveOutputStream(baos, 1024, 512)) {
            assertNotNull(tos);
            assertEquals(512, tos.getRecordSize());
        }
    }

    // Test POSIX long file mode
    @Test
    public void testSetLongFileMode_posix_shouldSucceed() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (TarArchiveOutputStream tos = new TarArchiveOutputStream(baos)) {
            tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
            TarArchiveEntry entry = new TarArchiveEntry(generateLongName());
            entry.setSize(5);
            tos.putArchiveEntry(entry);
            tos.write("hello".getBytes());
            tos.closeArchiveEntry();
        }
        assertTrue(baos.size() > 0);
    }

    // Test STAR big number mode (no exception expected)
    @Test
    public void testSetBigNumberMode_star_shouldNotThrow() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (TarArchiveOutputStream tos = new TarArchiveOutputStream(baos)) {
            tos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_STAR);
            TarArchiveEntry entry = new TarArchiveEntry("file.txt");
            entry.setSize(100);
            tos.putArchiveEntry(entry);
            tos.write(new byte[100]);
            tos.closeArchiveEntry();
        }
        // no exception expected
    }

    // Test setAddPaxHeadersForBigNumbers (no exception expected)
    @Test
    public void testSetAddPaxHeadersForBigNumbers_shouldNotThrow() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (TarArchiveOutputStream tos = new TarArchiveOutputStream(baos)) {
            tos.setAddPaxHeadersForBigNumbers(true);
            TarArchiveEntry entry = new TarArchiveEntry("file.txt");
            entry.setSize(100);
            tos.putArchiveEntry(entry);
            tos.write(new byte[100]);
            tos.closeArchiveEntry();
        }
        // no exception expected
    }

    // Test getBytesWritten after finish
    @Test
    public void testGetBytesWritten_afterFinishing() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (TarArchiveOutputStream tos = new TarArchiveOutputStream(baos)) {
            TarArchiveEntry entry = new TarArchiveEntry("file.txt");
            entry.setSize(10);
            tos.putArchiveEntry(entry);
            tos.write("1234567890".getBytes());
            tos.closeArchiveEntry();
            tos.finish();
            long written = tos.getBytesWritten();
            assertTrue("Bytes written should be positive", written > 0);
        }
    }

    // Test write with non-zero offset and length
    @Test
    public void testWrite_withOffsetAndLength() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (TarArchiveOutputStream tos = new TarArchiveOutputStream(baos)) {
            TarArchiveEntry entry = new TarArchiveEntry("file.txt");
            int size = 100;
            entry.setSize(size);
            tos.putArchiveEntry(entry);
            byte[] data = new byte[150];
            for (int i = 0; i < 150; i++) data[i] = (byte) i;
            // write from offset 10, 100 bytes
            tos.write(data, 10, 100);
            tos.closeArchiveEntry();
        }
        // no exception expected
    }

    // Test close can be called multiple times without exception
    @Test
    public void testClose_multipleTimes_shouldNotThrow() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(baos);
        tos.close();
        tos.close(); // second close should be safe
    }
}