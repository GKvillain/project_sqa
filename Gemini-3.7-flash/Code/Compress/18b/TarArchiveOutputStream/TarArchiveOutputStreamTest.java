package org.apache.commons.compress.archivers.tar;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TarArchiveOutputStreamTest {

    private ByteArrayOutputStream baos;
    private TarArchiveOutputStream tos;

    @Before
    public void setUp() {
        baos = new ByteArrayOutputStream();
        tos = new TarArchiveOutputStream(baos);
    }

    @After
    public void tearDown() throws IOException {
        if (tos != null) {
            try {
                tos.close();
            } catch (IOException ignored) {
            }
        }
    }

    // Tests writing a standard single entry and verifies bytes written and count
    @Test
    public void testPutArchiveEntry_normalFile_writesSuccessfully() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        byte[] content = "Hello World".getBytes("UTF-8");
        entry.setSize(content.length);

        tos.putArchiveEntry(entry);
        tos.write(content);
        tos.closeArchiveEntry();
        tos.finish();

        assertTrue(tos.getBytesWritten() > 0);
        assertEquals((int) tos.getBytesWritten(), tos.getCount());
        assertEquals(512, tos.getRecordSize());
    }

    // Tests writing data in chunks smaller and larger than record size to test buffer assembly
    @Test
    public void testWrite_chunkedAndAssembledData_writesSuccessfully() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("large_assembled.bin");
        byte[] content = new byte[1200];
        for (int i = 0; i < content.length; i++) {
            content[i] = (byte) (i % 256);
        }
        entry.setSize(content.length);

        tos.putArchiveEntry(entry);
        // Write in small chunks to exercise assembly buffer branch
        tos.write(content, 0, 100);
        tos.write(content, 100, 450); // crosses record boundary (assemLen > 0 and assemLen + numToWrite >= recordBuf.length)
        tos.write(content, 550, 650); // remaining bytes (>= recordBuf.length while loop)
        tos.closeArchiveEntry();
        tos.finish();

        assertTrue(tos.getBytesWritten() >= 1200);
    }

    // Tests writing a single byte using write(int)
    @Test
    public void testWrite_singleByte() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("single.txt");
        entry.setSize(1);
        tos.putArchiveEntry(entry);
        tos.write(65);
        tos.closeArchiveEntry();
        tos.finish();

        assertTrue(tos.getBytesWritten() > 0);
    }

    // Tests exception when writing more bytes than specified in entry size
    @Test(expected = IOException.class)
    public void testWrite_exceedsSpecifiedSize_throwsIOException() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("exceed.txt");
        entry.setSize(5);
        tos.putArchiveEntry(entry);
        tos.write(new byte[10]);
    }

    // Tests exception when closing an entry before all specified bytes are written
    @Test(expected = IOException.class)
    public void testCloseArchiveEntry_unwrittenBytesRemaining_throwsIOException() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("underflow.txt");
        entry.setSize(100);
        tos.putArchiveEntry(entry);
        tos.write(new byte[50]);
        tos.closeArchiveEntry();
    }

    // Tests exception when closing archive entry without an active entry
    @Test(expected = IOException.class)
    public void testCloseArchiveEntry_noCurrentEntry_throwsIOException() throws IOException {
        tos.closeArchiveEntry();
    }

    // Tests exception when finishing archive with unclosed entry
    @Test(expected = IOException.class)
    public void testFinish_withUnclosedEntry_throwsIOException() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("unclosed.txt");
        entry.setSize(0);
        tos.putArchiveEntry(entry);
        tos.finish();
    }

    // Tests exception when finishing an already finished archive
    @Test(expected = IOException.class)
    public void testFinish_alreadyFinished_throwsIOException() throws IOException {
        tos.finish();
        tos.finish();
    }

    // Tests exception when putting entry into finished archive
    @Test(expected = IOException.class)
    public void testPutArchiveEntry_streamFinished_throwsIOException() throws IOException {
        tos.finish();
        TarArchiveEntry entry = new TarArchiveEntry("afterFinish.txt");
        tos.putArchiveEntry(entry);
    }

    // Tests exception when long file name exceeds TarConstants.NAMELEN in default LONGFILE_ERROR mode
    @Test(expected = RuntimeException.class)
    public void testPutArchiveEntry_longNameErrorMode_throwsRuntimeException() throws IOException {
        String longName = "a/very/long/path/name/that/exceeds/the/tar/maximum/name/length/of/one/hundred/bytes/which/causes/an/error/by/default/file.txt";
        TarArchiveEntry entry = new TarArchiveEntry(longName);
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_ERROR);
        tos.putArchiveEntry(entry);
    }

    // Tests long file name using GNU long link mode
    @Test
    public void testPutArchiveEntry_longNameGnuMode_writesSuccessfully() throws IOException {
        String longName = "a/very/long/path/name/that/exceeds/the/tar/maximum/name/length/of/one/hundred/bytes/which/uses/gnu/extension/mode/file.txt";
        TarArchiveEntry entry = new TarArchiveEntry(longName);
        entry.setSize(0);
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.finish();

        assertTrue(tos.getBytesWritten() > 0);
    }

    // Tests long file name using POSIX PAX header mode
    @Test
    public void testPutArchiveEntry_longNamePosixMode_writesSuccessfully() throws IOException {
        String longName = "a/very/long/path/name/that/exceeds/the/tar/maximum/name/length/of/one/hundred/bytes/which/uses/posix/pax/header/mode/file.txt";
        TarArchiveEntry entry = new TarArchiveEntry(longName);
        entry.setSize(0);
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.finish();

        assertTrue(tos.getBytesWritten() > 0);
    }

    // Tests long directory name ending with slash in POSIX mode
    @Test
    public void testPutArchiveEntry_directoryWithPaxHeader_writesSuccessfully() throws IOException {
        String dirName = "a/very/long/path/name/that/exceeds/the/tar/maximum/name/length/of/one/hundred/bytes/which/is/a/directory/";
        TarArchiveEntry entry = new TarArchiveEntry(dirName);
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.finish();

        assertTrue(tos.getBytesWritten() > 0);
    }

    // Tests non-ASCII directory name with setAddPaxHeadersForNonAsciiNames
    @Test
    public void testPutArchiveEntry_nonAsciiDirectoryWithPaxHeader_writesSuccessfully() throws IOException {
        String nonAsciiDirName = "\u00e4\u00f6\u00fc\u00df/";
        TarArchiveEntry entry = new TarArchiveEntry(nonAsciiDirName);
        tos.setAddPaxHeadersForNonAsciiNames(true);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.finish();

        assertTrue(tos.getBytesWritten() > 0);
    }

    // Tests non-ASCII link name with setAddPaxHeadersForNonAsciiNames
    @Test
    public void testPutArchiveEntry_nonAsciiSymbolicLink_writesPaxHeader() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("symlink", TarConstants.LF_SYMLINK);
        entry.setLinkName("\u00e9\u00e8\u00e0");
        tos.setAddPaxHeadersForNonAsciiNames(true);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.finish();

        assertTrue(tos.getBytesWritten() > 0);
    }

    // Tests long file name with LONGFILE_TRUNCATE mode
    @Test
    public void testPutArchiveEntry_longNameTruncateMode_writesSuccessfully() throws IOException {
        String longName = "a/very/long/path/name/that/exceeds/the/tar/maximum/name/length/of/one/hundred/bytes/which/will/be/truncated/file.txt";
        TarArchiveEntry entry = new TarArchiveEntry(longName);
        entry.setSize(0);
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_TRUNCATE);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.finish();

        assertTrue(tos.getBytesWritten() > 0);
    }

    // Tests big number failure in BIGNUMBER_ERROR mode
    @Test(expected = RuntimeException.class)
    public void testPutArchiveEntry_bigNumberErrorMode_throwsRuntimeException() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("bignumber.txt");
        entry.setSize(TarConstants.MAXSIZE + 1L);
        tos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_ERROR);
        tos.putArchiveEntry(entry);
    }

    // Tests big number using POSIX PAX header mode
    @Test
    public void testPutArchiveEntry_bigNumberPosixMode_writesSuccessfully() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("bignumber_posix.txt");
        entry.setSize(TarConstants.MAXSIZE + 1L);
        entry.setUserId(TarConstants.MAXID + 1L);
        entry.setGroupId(TarConstants.MAXID + 1L);
        entry.setModTime(new Date((TarConstants.MAXSIZE + 1L) * 1000));

        tos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);
        tos.putArchiveEntry(entry);
        tos.write(0);
        tos.closeArchiveEntry();
        tos.finish();

        assertTrue(tos.getBytesWritten() > 0);
    }

    // Tests big number using STAR mode
    @Test
    public void testPutArchiveEntry_bigNumberStarMode_writesSuccessfully() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("bignumber_star.txt");
        entry.setSize(TarConstants.MAXSIZE + 1L);
        entry.setUserId(TarConstants.MAXID + 1L);
        entry.setGroupId(TarConstants.MAXID + 1L);

        tos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_STAR);
        tos.putArchiveEntry(entry);
        tos.write(0);
        tos.closeArchiveEntry();
        tos.finish();

        assertTrue(tos.getBytesWritten() > 0);
    }

    // Tests canWriteEntryData method
    @Test
    public void testCanWriteEntryData() {
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        assertTrue(tos.canWriteEntryData(entry));

        ArchiveEntry nonTarEntry = new ArchiveEntry() {
            public String getName() { return "dummy"; }
            public long getSize() { return 0; }
            public boolean isDirectory() { return false; }
            public Date getLastModifiedDate() { return new Date(); }
        };
        assertFalse(tos.canWriteEntryData(nonTarEntry));
    }

    // Tests createArchiveEntry from File object
    @Test
    public void testCreateArchiveEntry_validFile_returnsTarArchiveEntry() throws IOException {
        File tempFile = File.createTempFile("compress_test", ".tmp");
        try {
            ArchiveEntry entry = tos.createArchiveEntry(tempFile, "archive_entry.tmp");
            assertNotNull(entry);
            assertTrue(entry instanceof TarArchiveEntry);
            assertEquals("archive_entry.tmp", entry.getName());
        } finally {
            tempFile.delete();
        }
    }

    // Tests createArchiveEntry throws IOException when stream is finished
    @Test(expected = IOException.class)
    public void testCreateArchiveEntry_afterFinish_throwsIOException() throws IOException {
        File tempFile = File.createTempFile("compress_test", ".tmp");
        try {
            tos.finish();
            tos.createArchiveEntry(tempFile, "archive_entry.tmp");
        } finally {
            tempFile.delete();
        }
    }

    // Tests multiple close invocations
    @Test
    public void testClose_multipleTimes_noException() throws IOException {
        tos.close();
        tos.close();
    }

    // Tests constructors with different parameters and flush
    @Test
    public void testConstructorsAndFlush_validParameters_initializedProperly() throws IOException {
        TarArchiveOutputStream s1 = new TarArchiveOutputStream(new ByteArrayOutputStream(), "UTF-8");
        assertEquals(512, s1.getRecordSize());
        s1.close();

        TarArchiveOutputStream s2 = new TarArchiveOutputStream(new ByteArrayOutputStream(), 1024);
        assertEquals(512, s2.getRecordSize());
        s2.close();

        TarArchiveOutputStream s3 = new TarArchiveOutputStream(new ByteArrayOutputStream(), 1024, "UTF-8");
        assertEquals(512, s3.getRecordSize());
        s3.close();

        TarArchiveOutputStream s4 = new TarArchiveOutputStream(new ByteArrayOutputStream(), 1024, 1024);
        assertEquals(1024, s4.getRecordSize());
        s4.flush();
        s4.close();

        TarArchiveOutputStream s5 = new TarArchiveOutputStream(new ByteArrayOutputStream(), 1024, 512, "UTF-8");
        assertEquals(512, s5.getRecordSize());
        s5.close();
    }
}