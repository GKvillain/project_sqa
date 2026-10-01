package org.apache.commons.compress.archivers.tar;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.junit.Test;

public class TarArchiveOutputStreamTest {

    // Test constructor with default block and record sizes
    @Test
    public void testConstructor_defaultSizes_createsSuccessfully() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = new TarArchiveOutputStream(bos);
        assertNotNull(tarOut);
        assertEquals(TarBuffer.DEFAULT_RCDSIZE, tarOut.getRecordSize());
    }

    // Test constructor with custom block size
    @Test
    public void testConstructor_customBlockSize_createsSuccessfully() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = new TarArchiveOutputStream(bos, 2048);
        assertNotNull(tarOut);
    }

    // Test constructor with custom block and record sizes
    @Test
    public void testConstructor_customBlockAndRecordSizes_createsSuccessfully() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = new TarArchiveOutputStream(bos, 2048, 512);
        assertNotNull(tarOut);
    }

    // Test finish when already finished
    @Test(expected = IOException.class)
    public void testFinish_alreadyFinished_throwsIOException() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = new TarArchiveOutputStream(bos);
        tarOut.finish();
        tarOut.finish(); // should throw
    }

    // Test close without finish does finish first
    @Test
    public void testClose_withoutFinish_closesSuccessfully() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = new TarArchiveOutputStream(bos);
        tarOut.close();
        assertTrue(bos.size() > 0); // EOF records written
    }

    // Test close multiple times does not throw
    @Test
    public void testClose_multipleTimes_noException() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = new TarArchiveOutputStream(bos);
        tarOut.close();
        tarOut.close(); // should be safe
    }

    // Test putArchiveEntry with normal size entry
    @Test
    public void testPutArchiveEntry_normalEntry_success() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = new TarArchiveOutputStream(bos);
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.setSize(10);
        tarOut.putArchiveEntry(entry);
        tarOut.closeArchiveEntry();
        tarOut.close();
        assertTrue(bos.size() > 0);
    }

    // Test putArchiveEntry when finished
    @Test(expected = IOException.class)
    public void testPutArchiveEntry_afterFinish_throwsIOException() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = new TarArchiveOutputStream(bos);
        tarOut.finish();
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        tarOut.putArchiveEntry(entry);
    }

    // Test putArchiveEntry with long file name in GNU mode
    @Test
    public void testPutArchiveEntry_longFileNameGnu_success() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = new TarArchiveOutputStream(bos);
        tarOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 200; i++) {
            sb.append("x");
        }
        TarArchiveEntry entry = new TarArchiveEntry(sb.toString());
        entry.setSize(10);
        tarOut.putArchiveEntry(entry);
        tarOut.closeArchiveEntry();
        tarOut.close();
        assertTrue(bos.size() > 0);
    }

    // Test putArchiveEntry with long file name in TRUNCATE mode
    @Test
    public void testPutArchiveEntry_longFileNameTruncate_success() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = new TarArchiveOutputStream(bos);
        tarOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_TRUNCATE);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 200; i++) {
            sb.append("x");
        }
        TarArchiveEntry entry = new TarArchiveEntry(sb.toString());
        entry.setSize(10);
        tarOut.putArchiveEntry(entry);
        tarOut.closeArchiveEntry();
        tarOut.close();
        assertTrue(bos.size() > 0);
    }

    // Test putArchiveEntry with long file name in ERROR mode (default)
    @Test(expected = RuntimeException.class)
    public void testPutArchiveEntry_longFileNameError_throwsRuntimeException() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = new TarArchiveOutputStream(bos);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 200; i++) {
            sb.append("x");
        }
        TarArchiveEntry entry = new TarArchiveEntry(sb.toString());
        entry.setSize(10);
        tarOut.putArchiveEntry(entry);
    }

    // Test closeArchiveEntry without putArchiveEntry
    @Test(expected = IOException.class)
    public void testCloseArchiveEntry_noCurrentEntry_throwsIOException() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = new TarArchiveOutputStream(bos);
        tarOut.closeArchiveEntry();
    }

    // Test closeArchiveEntry when finished
    @Test(expected = IOException.class)
    public void testCloseArchiveEntry_afterFinish_throwsIOException() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = new TarArchiveOutputStream(bos);
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.setSize(10);
        tarOut.putArchiveEntry(entry);
        tarOut.finish(); // finishes without closing entry -> should throw
    }

    // Test write with valid data
    @Test
    public void testWrite_validData_writesCorrectly() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = new TarArchiveOutputStream(bos);
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.setSize(10);
        tarOut.putArchiveEntry(entry);
        byte[] data = "HelloWorld".getBytes();
        tarOut.write(data);
        tarOut.closeArchiveEntry();
        tarOut.close();
        assertTrue(bos.size() > 0);
    }

    // Test write exceeding entry size
    @Test(expected = IOException.class)
    public void testWrite_exceedsEntrySize_throwsIOException() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = new TarArchiveOutputStream(bos);
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.setSize(5);
        tarOut.putArchiveEntry(entry);
        byte[] data = "HelloWorld".getBytes();
        tarOut.write(data);
    }

    // Test flush
    @Test
    public void testFlush_afterWrite_noException() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = new TarArchiveOutputStream(bos);
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.setSize(10);
        tarOut.putArchiveEntry(entry);
        tarOut.flush();
        tarOut.closeArchiveEntry();
        tarOut.close();
    }

    // Test finish with haveUnclosedEntry
    @Test(expected = IOException.class)
    public void testFinish_withUnclosedEntry_throwsIOException() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = new TarArchiveOutputStream(bos);
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.setSize(10);
        tarOut.putArchiveEntry(entry);
        tarOut.finish(); // entry not closed -> should throw
    }

    // Test createArchiveEntry when finished
    @Test(expected = IOException.class)
    public void testCreateArchiveEntry_afterFinish_throwsIOException() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = new TarArchiveOutputStream(bos);
        tarOut.finish();
        tarOut.createArchiveEntry(new java.io.File("test.txt"), "test.txt");
    }

    // Test getRecordSize
    @Test
    public void testGetRecordSize_defaultRecordSize_returnsCorrectValue() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = new TarArchiveOutputStream(bos);
        assertEquals(TarBuffer.DEFAULT_RCDSIZE, tarOut.getRecordSize());
    }

    // Test setLongFileMode and getRecordSize
    @Test
    public void testSetLongFileMode_gnuMode_affectsLongFileNameHandling() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = new TarArchiveOutputStream(bos);
        tarOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
        // just ensure it doesn't throw; behavior tested in other tests
    }
}