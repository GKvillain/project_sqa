package org.apache.commons.compress.archivers.tar;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class TarArchiveOutputStreamTest {

    private ByteArrayOutputStream byteArrayOutputStream;

    @Before
    public void setUp() {
        byteArrayOutputStream = new ByteArrayOutputStream();
    }

    // Tests writing a normal entry and verifying bytes written count
    @Test
    public void testCount_writeEntry_updatesBytesWritten() throws Exception {
        TarArchiveOutputStream taos = new TarArchiveOutputStream(byteArrayOutputStream);
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        byte[] contents = "Hello, World!".getBytes("UTF-8");
        entry.setSize(contents.length);
        taos.putArchiveEntry(entry);
        taos.write(contents);
        taos.closeArchiveEntry();
        taos.finish();
        taos.close();

        assertEquals(contents.length, taos.getBytesWritten());
    }

    // Tests getRecordSize returns default record size
    @Test
    public void testGetRecordSize_defaultConstructor_returnsDefaultRecordSize() throws Exception {
        TarArchiveOutputStream taos = new TarArchiveOutputStream(byteArrayOutputStream);
        assertEquals(TarBuffer.DEFAULT_RCDSIZE, taos.getRecordSize());
        taos.close();
    }

    // Tests custom block size and record size constructor
    @Test
    public void testConstructor_customBlockAndRecordSize_setsRecordSizeCorrectly() throws Exception {
        int customBlockSize = 1024;
        int customRecordSize = 512;
        TarArchiveOutputStream taos = new TarArchiveOutputStream(byteArrayOutputStream, customBlockSize, customRecordSize);
        assertEquals(customRecordSize, taos.getRecordSize());
        taos.close();
    }

    // Tests writing past the specified entry size throws IOException
    @Test(expected = IOException.class)
    public void testWrite_exceedsEntrySize_throwsException() throws Exception {
        TarArchiveOutputStream taos = new TarArchiveOutputStream(byteArrayOutputStream);
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.setSize(5);
        taos.putArchiveEntry(entry);
        byte[] data = new byte[10];
        taos.write(data, 0, data.length);
    }

    // Tests closing an entry before all specified bytes are written throws IOException
    @Test(expected = IOException.class)
    public void testCloseArchiveEntry_beforeAllBytesWritten_throwsException() throws Exception {
        TarArchiveOutputStream taos = new TarArchiveOutputStream(byteArrayOutputStream);
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.setSize(10);
        taos.putArchiveEntry(entry);
        taos.write(new byte[5], 0, 5);
        taos.closeArchiveEntry();
    }

    // Tests calling closeArchiveEntry without an active entry throws IOException
    @Test(expected = IOException.class)
    public void testCloseArchiveEntry_noActiveEntry_throwsException() throws Exception {
        TarArchiveOutputStream taos = new TarArchiveOutputStream(byteArrayOutputStream);
        taos.closeArchiveEntry();
    }

    // Tests finish with unclosed entry throws IOException
    @Test(expected = IOException.class)
    public void testFinish_withUnclosedEntry_throwsException() throws Exception {
        TarArchiveOutputStream taos = new TarArchiveOutputStream(byteArrayOutputStream);
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.setSize(0);
        taos.putArchiveEntry(entry);
        taos.finish();
    }

    // Tests calling finish twice throws IOException
    @Test(expected = IOException.class)
    public void testFinish_alreadyFinished_throwsException() throws Exception {
        TarArchiveOutputStream taos = new TarArchiveOutputStream(byteArrayOutputStream);
        taos.finish();
        taos.finish();
    }

    // Tests putArchiveEntry after finish throws IOException
    @Test(expected = IOException.class)
    public void testPutArchiveEntry_afterFinished_throwsException() throws Exception {
        TarArchiveOutputStream taos = new TarArchiveOutputStream(byteArrayOutputStream);
        taos.finish();
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.setSize(0);
        taos.putArchiveEntry(entry);
    }

    // Tests createArchiveEntry after finish throws IOException
    @Test(expected = IOException.class)
    public void testCreateArchiveEntry_afterFinished_throwsException() throws Exception {
        TarArchiveOutputStream taos = new TarArchiveOutputStream(byteArrayOutputStream);
        taos.finish();
        File tempFile = File.createTempFile("compress", "test");
        tempFile.deleteOnExit();
        taos.createArchiveEntry(tempFile, "temp.txt");
    }

    // Tests long file name throws RuntimeException in default LONGFILE_ERROR mode
    @Test(expected = RuntimeException.class)
    public void testPutArchiveEntry_longFileNameErrorMode_throwsException() throws Exception {
        TarArchiveOutputStream taos = new TarArchiveOutputStream(byteArrayOutputStream);
        StringBuilder longName = new StringBuilder();
        for (int i = 0; i < TarConstants.NAMELEN + 10; i++) {
            longName.append("a");
        }
        TarArchiveEntry entry = new TarArchiveEntry(longName.toString());
        entry.setSize(0);
        taos.putArchiveEntry(entry);
    }

    // Tests long file name in LONGFILE_TRUNCATE mode succeeds
    @Test
    public void testPutArchiveEntry_longFileNameTruncateMode_success() throws Exception {
        TarArchiveOutputStream taos = new TarArchiveOutputStream(byteArrayOutputStream);
        taos.setLongFileMode(TarArchiveOutputStream.LONGFILE_TRUNCATE);
        StringBuilder longName = new StringBuilder();
        for (int i = 0; i < TarConstants.NAMELEN + 10; i++) {
            longName.append("a");
        }
        TarArchiveEntry entry = new TarArchiveEntry(longName.toString());
        entry.setSize(0);
        taos.putArchiveEntry(entry);
        taos.closeArchiveEntry();
        taos.finish();
        taos.close();
    }

    // Tests long file name in LONGFILE_GNU mode creates long link entry
    @Test
    public void testPutArchiveEntry_longFileNameGnuMode_success() throws Exception {
        TarArchiveOutputStream taos = new TarArchiveOutputStream(byteArrayOutputStream);
        taos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
        StringBuilder longName = new StringBuilder();
        for (int i = 0; i < TarConstants.NAMELEN + 10; i++) {
            longName.append("a");
        }
        TarArchiveEntry entry = new TarArchiveEntry(longName.toString());
        entry.setSize(0);
        taos.putArchiveEntry(entry);
        taos.closeArchiveEntry();
        taos.finish();
        taos.close();

        assertTrue(byteArrayOutputStream.toByteArray().length > 0);
    }

    // Tests directory entry handling
    @Test
    public void testPutArchiveEntry_directoryEntry_success() throws Exception {
        TarArchiveOutputStream taos = new TarArchiveOutputStream(byteArrayOutputStream);
        TarArchiveEntry entry = new TarArchiveEntry("testdir/");
        taos.putArchiveEntry(entry);
        taos.closeArchiveEntry();
        taos.finish();
        taos.close();

        assertEquals(0, entry.getSize());
    }

    // Tests writing multiple records and small assembled buffers
    @Test
    public void testWrite_multipleRecordsAndAssemblyBuffer_success() throws Exception {
        TarArchiveOutputStream taos = new TarArchiveOutputStream(byteArrayOutputStream);
        int recordSize = taos.getRecordSize();
        int totalSize = recordSize * 2 + 100;
        byte[] data = new byte[totalSize];
        for (int i = 0; i < totalSize; i++) {
            data[i] = (byte) (i % 128);
        }

        TarArchiveEntry entry = new TarArchiveEntry("largefile.dat");
        entry.setSize(totalSize);
        taos.putArchiveEntry(entry);

        // Write in chunks to trigger assemble buffer logic
        int chunkSize = 300;
        int written = 0;
        while (written < totalSize) {
            int toWrite = Math.min(chunkSize, totalSize - written);
            taos.write(data, written, toWrite);
            written += toWrite;
        }

        taos.closeArchiveEntry();
        taos.finish();
        taos.close();

        assertEquals(totalSize, taos.getBytesWritten());
    }

    // Tests flush and close multiple calls
    @Test
    public void testFlushAndClose_multipleCalls_noException() throws Exception {
        TarArchiveOutputStream taos = new TarArchiveOutputStream(byteArrayOutputStream);
        taos.flush();
        taos.close();
        taos.close(); // Second close should be safe
    }
}