package org.apache.commons.compress.archivers.tar;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class TarArchiveOutputStreamTest {
    private ByteArrayOutputStream baos;
    private TarArchiveOutputStream tarOut;

    @Before
    public void setUp() {
        baos = new ByteArrayOutputStream();
        tarOut = new TarArchiveOutputStream(baos);
    }

    @After
    public void tearDown() throws IOException {
        tarOut.close();
    }

    private TarArchiveEntry createEntry(String name, long size) {
        TarArchiveEntry e = new TarArchiveEntry(name);
        e.setSize(size);
        return e;
    }

    private String createLongName(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append('a');
        }
        return sb.toString();
    }

    // Normal case: put entry, write data, close entry, finish
    @Test
    public void testPutArchiveEntry_writeData_closeEntry_shouldComplete() throws IOException {
        TarArchiveEntry entry = createEntry("file.txt", 10);
        tarOut.putArchiveEntry(entry);
        tarOut.write(new byte[]{1,2,3,4,5,6,7,8,9,10}, 0, 10);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        assertTrue(baos.size() > 0);
    }

    // closeArchiveEntry when bytes written < size -> IOException
    @Test(expected = IOException.class)
    public void testCloseArchiveEntry_lessBytesThanSize_throwsIOException() throws IOException {
        TarArchiveEntry entry = createEntry("file.txt", 100);
        tarOut.putArchiveEntry(entry);
        tarOut.write(new byte[]{1}, 0, 1);
        tarOut.closeArchiveEntry();
    }

    // write more bytes than entry size -> IOException
    @Test(expected = IOException.class)
    public void testWrite_exceedsEntrySize_throwsIOException() throws IOException {
        TarArchiveEntry entry = createEntry("file.txt", 5);
        tarOut.putArchiveEntry(entry);
        tarOut.write(new byte[10], 0, 10);
    }

    // write exactly full records (512 each)
    @Test
    public void testWrite_exactRecordSize_shouldWrite() throws IOException {
        TarArchiveEntry entry = createEntry("file.txt", 1024);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[1024];
        for (int i = 0; i < 1024; i++) data[i] = (byte)(i % 256);
        tarOut.write(data, 0, 1024);
        tarOut.closeArchiveEntry();
    }

    // write in small chunks to exercise assembly buffer
    @Test
    public void testWrite_smallChunks_shouldAssembleRecords() throws IOException {
        TarArchiveEntry entry = createEntry("file.txt", 600);
        tarOut.putArchiveEntry(entry);
        byte[] chunk = new byte[50];
        for (int i = 0; i < 12; i++) {
            tarOut.write(chunk, 0, 50);
        }
        tarOut.closeArchiveEntry();
    }

    // write that crosses record boundary (assembly flush)
    @Test
    public void testWrite_crossRecordBoundary_shouldWriteRecords() throws IOException {
        TarArchiveEntry entry = createEntry("file.txt", 600);
        tarOut.putArchiveEntry(entry);
        tarOut.write(new byte[200], 0, 200);
        tarOut.write(new byte[400], 0, 400);
        tarOut.closeArchiveEntry();
    }

    // closeArchiveEntry with assembly still pending and bytes < size
    @Test(expected = IOException.class)
    public void testCloseArchiveEntry_assemblyPendingAndBytesLess_throwsIOException() throws IOException {
        TarArchiveEntry entry = createEntry("file.txt", 600);
        tarOut.putArchiveEntry(entry);
        tarOut.write(new byte[500], 0, 500); // assembly still holds 500 bytes (size=600)
        tarOut.closeArchiveEntry(); // flushes 500, currBytes=500 < 600 => exception
    }

    // long file name with LONGFILE_ERROR (default) -> RuntimeException
    @Test(expected = RuntimeException.class)
    public void testPutArchiveEntry_longNameErrorMode_throwsRuntimeException() throws IOException {
        String longName = createLongName(150);
        TarArchiveEntry entry = createEntry(longName, 0);
        tarOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_ERROR);
        tarOut.putArchiveEntry(entry);
    }

    // long file name with LONGFILE_TRUNCATE -> no exception
    @Test
    public void testPutArchiveEntry_longNameTruncateMode_doesNotThrow() throws IOException {
        String longName = createLongName(150);
        TarArchiveEntry entry = createEntry(longName, 0);
        tarOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_TRUNCATE);
        tarOut.putArchiveEntry(entry);
        tarOut.closeArchiveEntry();
    }

    // long file name with LONGFILE_GNU -> no exception (writes long link entry)
    @Test
    public void testPutArchiveEntry_longNameGnuMode_createsLongLinkEntry() throws IOException {
        String longName = createLongName(150);
        TarArchiveEntry entry = createEntry(longName, 0);
        tarOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
        tarOut.putArchiveEntry(entry);
        tarOut.closeArchiveEntry();
    }

    // finish writes two EOF records
    @Test
    public void testFinish_writesTwoEOFRrecords() throws IOException {
        tarOut.finish();
        byte[] data = baos.toByteArray();
        assertEquals(1024, data.length);
        for (byte b : data) {
            assertEquals(0, b);
        }
    }

    // close calls finish and closes underlying stream
    @Test
    public void testClose_callsFinishAndClose() throws IOException {
        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut2 = new TarArchiveOutputStream(baos2);
        tarOut2.close();
        byte[] data = baos2.toByteArray();
        assertEquals(1024, data.length);
    }

    // flush delegates to underlying output stream
    @Test
    public void testFlush_delegatesToOut() throws IOException {
        tarOut.flush();
    }

    // createArchiveEntry returns a non-null TarArchiveEntry
    @Test
    public void testCreateArchiveEntry_returnsTarArchiveEntry() throws IOException {
        File tmp = File.createTempFile("test", ".txt");
        tmp.deleteOnExit();
        ArchiveEntry entry = tarOut.createArchiveEntry(tmp, "entryName");
        assertNotNull(entry);
        assertTrue(entry instanceof TarArchiveEntry);
    }

    // directory entry handled correctly (size = 0)
    @Test
    public void testPutArchiveEntry_directoryEntry_setsSizeZero() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("dir/");
        entry.setSize(0);
        tarOut.putArchiveEntry(entry);
        tarOut.closeArchiveEntry();
    }

    // multiple entries can be written sequentially
    @Test
    public void testMultipleEntries_shouldWriteMultipleHeaders() throws IOException {
        TarArchiveEntry entry1 = createEntry("f1.txt", 10);
        tarOut.putArchiveEntry(entry1);
        tarOut.write(new byte[10], 0, 10);
        tarOut.closeArchiveEntry();

        TarArchiveEntry entry2 = createEntry("f2.txt", 5);
        tarOut.putArchiveEntry(entry2);
        tarOut.write(new byte[5], 0, 5);
        tarOut.closeArchiveEntry();
    }

    // ========== New tests for uncovered areas ==========

    // test getBytesWritten() returns correct count after writing data
    @Test
    public void testGetBytesWritten_afterWrite_returnsCorrectCount() throws IOException {
        TarArchiveEntry entry = createEntry("test.txt", 100);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[100];
        tarOut.write(data, 0, 100);
        assertEquals(100, tarOut.getBytesWritten());
        tarOut.closeArchiveEntry();
        // after close, bytes written still includes padding? getBytesWritten returns bytes written before padding
        assertEquals(100, tarOut.getBytesWritten());
    }

    // test getCount() returns number of entries written
    @Test
    public void testGetCount_afterMultipleEntries_returnsCorrectCount() throws IOException {
        TarArchiveEntry entry1 = createEntry("a.txt", 10);
        tarOut.putArchiveEntry(entry1);
        tarOut.write(new byte[10], 0, 10);
        tarOut.closeArchiveEntry();
        assertEquals(1, tarOut.getCount());

        TarArchiveEntry entry2 = createEntry("b.txt", 5);
        tarOut.putArchiveEntry(entry2);
        tarOut.write(new byte[5], 0, 5);
        tarOut.closeArchiveEntry();
        assertEquals(2, tarOut.getCount());
    }

    // long file name with LONGFILE_POSIX mode (should use PAX extended header)
    @Test
    public void testPutArchiveEntry_longNamePosixMode_createsPaxEntry() throws IOException {
        String longName = createLongName(150);
        TarArchiveEntry entry = createEntry(longName, 0);
        tarOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
        tarOut.putArchiveEntry(entry);
        tarOut.closeArchiveEntry();
        // no exception expected
    }

    // setAddPaxHeadersForNonAsciiNames(true) and use non-ASCII name
    @Test
    public void testSetAddPaxHeadersForNonAsciiNames_withNonAsciiName_doesNotThrow() throws IOException {
        tarOut.setAddPaxHeadersForNonAsciiNames(true);
        TarArchiveEntry entry = createEntry("中文.txt", 50);
        entry.setSize(50);
        tarOut.putArchiveEntry(entry);
        tarOut.write(new byte[50], 0, 50);
        tarOut.closeArchiveEntry();
    }

    // setBigNumberMode(BIGNUMBER_POSIX) and write normal entry (no big number needed)
    @Test
    public void testSetBigNumberPosixMode_withNormalEntry_works() throws IOException {
        tarOut.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);
        TarArchiveEntry entry = createEntry("normal.txt", 256);
        tarOut.putArchiveEntry(entry);
        tarOut.write(new byte[256], 0, 256);
        tarOut.closeArchiveEntry();
    }

    // write after finish should throw IOException
    @Test(expected = IOException.class)
    public void testWrite_afterFinish_throwsIOException() throws IOException {
        tarOut.finish();
        tarOut.write(new byte[]{1}, 0, 1);
    }

    // close can be called multiple times without throwing
    @Test
    public void testClose_multipleTimes_doesNotThrow() throws IOException {
        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut2 = new TarArchiveOutputStream(baos2);
        tarOut2.close();
        tarOut2.close(); // second close should be safe
    }

    // constructor with custom blockSize and recordSize
    @Test
    public void testConstructorWithBlockSizeAndRecordSize() throws IOException {
        ByteArrayOutputStream baosCustom = new ByteArrayOutputStream();
        TarArchiveOutputStream customOut = new TarArchiveOutputStream(baosCustom, 1024, 2048);
        TarArchiveEntry entry = createEntry("custom.txt", 100);
        customOut.putArchiveEntry(entry);
        customOut.write(new byte[100], 0, 100);
        customOut.closeArchiveEntry();
        customOut.finish();
        byte[] data = baosCustom.toByteArray();
        assertTrue(data.length > 0);
        customOut.close();
    }

    // putArchiveEntry for a symbolic link entry (no data written)
    @Test
    public void testPutArchiveEntry_symbolicLink_doesNotRequireData() throws IOException {
        TarArchiveEntry linkEntry = new TarArchiveEntry("mylink");
        linkEntry.setLinkName("targetFile");
        linkEntry.setLinkFlag(TarConstants.LF_SYMLINK);
        linkEntry.setSize(0);
        tarOut.putArchiveEntry(linkEntry);
        tarOut.closeArchiveEntry(); // no write needed for symlink
    }
}