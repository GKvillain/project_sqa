package org.apache.commons.compress.archivers.tar;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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

    // Tests default constructor and getRecordSize
    @Test
    public void testGetRecordSize_defaultConstructor_returnsDefaultRecordSize() {
        assertEquals(TarBuffer.DEFAULT_RCDSIZE, tos.getRecordSize());
    }

    // Tests custom block and record size constructor
    @Test
    public void testConstructor_customBlockAndRecordSize_setsCorrectRecordSize() {
        TarArchiveOutputStream customTos = new TarArchiveOutputStream(baos, 1024, 512);
        assertEquals(512, customTos.getRecordSize());
    }

    // Tests writing a normal file entry with matching content size
    @Test
    public void testPutArchiveEntry_normalFile_writesSuccessfully() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        byte[] content = "Hello World".getBytes();
        entry.setSize(content.length);

        tos.putArchiveEntry(entry);
        tos.write(content);
        tos.closeArchiveEntry();
        tos.finish();

        assertTrue(baos.size() > 0);
    }

    // Tests writing a directory entry where size should be treated as zero
    @Test
    public void testPutArchiveEntry_directory_writesSuccessfully() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("testdir/");
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.finish();

        assertTrue(baos.size() > 0);
    }

    // Tests long file name when mode is LONGFILE_ERROR (default) throws exception
    @Test(expected = RuntimeException.class)
    public void testPutArchiveEntry_longFileNameErrorMode_throwsRuntimeException() throws IOException {
        String longName = "1234567890/1234567890/1234567890/1234567890/1234567890/1234567890/1234567890/1234567890/1234567890/1234567890/longFileName.txt";
        TarArchiveEntry entry = new TarArchiveEntry(longName);
        tos.putArchiveEntry(entry);
    }

    // Tests long file name when mode is LONGFILE_TRUNCATE
    @Test
    public void testPutArchiveEntry_longFileNameTruncateMode_succeeds() throws IOException {
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_TRUNCATE);
        String longName = "1234567890/1234567890/1234567890/1234567890/1234567890/1234567890/1234567890/1234567890/1234567890/1234567890/longFileName.txt";
        TarArchiveEntry entry = new TarArchiveEntry(longName);
        entry.setSize(0);

        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.finish();

        assertTrue(baos.size() > 0);
    }

    // Tests long file name when mode is LONGFILE_GNU
    @Test
    public void testPutArchiveEntry_longFileNameGnuMode_succeeds() throws IOException {
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
        String longName = "1234567890/1234567890/1234567890/1234567890/1234567890/1234567890/1234567890/1234567890/1234567890/1234567890/longFileName.txt";
        TarArchiveEntry entry = new TarArchiveEntry(longName);
        byte[] content = "GNU long name content".getBytes();
        entry.setSize(content.length);

        tos.putArchiveEntry(entry);
        tos.write(content);
        tos.closeArchiveEntry();
        tos.finish();

        assertTrue(baos.size() > 0);
    }

    // Tests writing more bytes than specified in entry header throws IOException
    @Test(expected = IOException.class)
    public void testWrite_exceedsSpecifiedSize_throwsIOException() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.setSize(5);
        tos.putArchiveEntry(entry);
        tos.write(new byte[10], 0, 10);
    }

    // Tests closing an archive entry before writing all specified bytes throws IOException
    @Test(expected = IOException.class)
    public void testCloseArchiveEntry_unwrittenBytesRemain_throwsIOException() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.setSize(100);
        tos.putArchiveEntry(entry);
        tos.write(new byte[50], 0, 50);
        tos.closeArchiveEntry();
    }

    // Tests calling finish when there is an unclosed entry throws IOException
    @Test(expected = IOException.class)
    public void testFinish_withUnclosedEntry_throwsIOException() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.setSize(10);
        tos.putArchiveEntry(entry);
        tos.finish();
    }

    // Tests writing small chunks assembling into full record buffers
    @Test
    public void testWrite_smallChunksAssembleCorrectly_succeeds() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        int totalSize = 1000;
        entry.setSize(totalSize);
        tos.putArchiveEntry(entry);

        byte[] chunk = new byte[32];
        int written = 0;
        while (written < totalSize) {
            int toWrite = Math.min(chunk.length, totalSize - written);
            tos.write(chunk, 0, toWrite);
            written += toWrite;
        }

        tos.closeArchiveEntry();
        tos.finish();

        assertTrue(baos.size() > 0);
    }

    // Tests writing chunk larger than record buffer size
    @Test
    public void testWrite_chunkLargerThanRecordSize_succeeds() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        int totalSize = 1500;
        entry.setSize(totalSize);
        tos.putArchiveEntry(entry);

        byte[] bigChunk = new byte[1500];
        tos.write(bigChunk, 0, bigChunk.length);
        tos.closeArchiveEntry();
        tos.finish();

        assertTrue(baos.size() > 0);
    }

    // Tests closing the stream multiple times
    @Test
    public void testClose_multipleCalls_closesWithoutException() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.setSize(0);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.close();
        tos.close();
    }

    // Tests flush method
    @Test
    public void testFlush_invoked_flushesUnderlyingStream() throws IOException {
        tos.flush();
    }

    // Tests createArchiveEntry helper
    @Test
    public void testCreateArchiveEntry_validInput_returnsEntry() throws IOException {
        File tempFile = File.createTempFile("tar_test", ".tmp");
        try {
            ArchiveEntry entry = tos.createArchiveEntry(tempFile, "entryName.txt");
            assertNotNull(entry);
            assertEquals("entryName.txt", entry.getName());
        } finally {
            tempFile.delete();
        }
    }

    // --- New Tests ---

    // Tests constructor with block size only
    @Test
    public void testConstructor_blockSizeOnly() {
        TarArchiveOutputStream customTos = new TarArchiveOutputStream(baos, 1024);
        assertEquals(TarBuffer.DEFAULT_RCDSIZE, customTos.getRecordSize());
    }

    // Tests constructor with encoding
    @Test
    public void testConstructor_encoding() throws IOException {
        TarArchiveOutputStream customTos = new TarArchiveOutputStream(baos, "UTF-8");
        assertEquals(TarBuffer.DEFAULT_RCDSIZE, customTos.getRecordSize());
        customTos.close();
    }

    // Tests constructor with block size and encoding
    @Test
    public void testConstructor_blockSizeAndEncoding() throws IOException {
        TarArchiveOutputStream customTos = new TarArchiveOutputStream(baos, 1024, "UTF-8");
        assertEquals(TarBuffer.DEFAULT_RCDSIZE, customTos.getRecordSize());
        customTos.close();
    }

    // Tests constructor with block size, record size and encoding
    @Test
    public void testConstructor_blockSizeRecordSizeAndEncoding() throws IOException {
        TarArchiveOutputStream customTos = new TarArchiveOutputStream(baos, 1024, 512, "UTF-8");
        assertEquals(512, customTos.getRecordSize());
        customTos.close();
    }

    // Tests single byte write method write(int)
    @Test
    public void testWrite_singleByte_writesSuccessfully() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("singleByte.txt");
        entry.setSize(1);
        tos.putArchiveEntry(entry);
        tos.write(65);
        tos.closeArchiveEntry();
        tos.finish();

        assertTrue(baos.size() > 0);
    }

    // Tests getBytesWritten and getCount tracking
    @Test
    public void testGetBytesWritten_and_getCount() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        byte[] data = "test data".getBytes();
        entry.setSize(data.length);
        tos.putArchiveEntry(entry);
        tos.write(data);
        tos.closeArchiveEntry();
        tos.finish();

        assertTrue(tos.getBytesWritten() > 0);
        assertEquals(tos.getBytesWritten(), tos.getCount());
    }

    // Tests long file name when mode is LONGFILE_POSIX
    @Test
    public void testPutArchiveEntry_longFileNamePosixMode_succeeds() throws IOException {
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
        String longName = "1234567890/1234567890/1234567890/1234567890/1234567890/1234567890/1234567890/1234567890/1234567890/1234567890/longFileName.txt";
        TarArchiveEntry entry = new TarArchiveEntry(longName);
        byte[] content = "POSIX long name content".getBytes();
        entry.setSize(content.length);

        tos.putArchiveEntry(entry);
        tos.write(content);
        tos.closeArchiveEntry();
        tos.finish();

        assertTrue(baos.size() > 0);
    }

    // Tests long link name with GNU mode
    @Test
    public void testPutArchiveEntry_longLinkNameGnuMode_succeeds() throws IOException {
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
        TarArchiveEntry entry = new TarArchiveEntry("symlink");
        entry.setLinkName("1234567890/1234567890/1234567890/1234567890/1234567890/1234567890/1234567890/1234567890/1234567890/1234567890/target.txt");
        entry.setSize(0);

        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.finish();

        assertTrue(baos.size() > 0);
    }

    // Tests long link name with POSIX mode
    @Test
    public void testPutArchiveEntry_longLinkNamePosixMode_succeeds() throws IOException {
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
        TarArchiveEntry entry = new TarArchiveEntry("symlink");
        entry.setLinkName("1234567890/1234567890/1234567890/1234567890/1234567890/1234567890/1234567890/1234567890/1234567890/1234567890/target.txt");
        entry.setSize(0);

        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.finish();

        assertTrue(baos.size() > 0);
    }

    // Tests non-TarArchiveEntry passing to putArchiveEntry throws ClassCastException
    @Test(expected = ClassCastException.class)
    public void testPutArchiveEntry_nonTarEntry_throwsClassCastException() throws IOException {
        ArchiveEntry nonTarEntry = new ArchiveEntry() {
            @Override
            public String getName() {
                return "test";
            }

            @Override
            public long getSize() {
                return 0;
            }

            @Override
            public boolean isDirectory() {
                return false;
            }

            @Override
            public Date getLastModifiedDate() {
                return new Date();
            }
        };
        tos.putArchiveEntry(nonTarEntry);
    }

    // Tests addPaxHeadersForNonAsciiNames
    @Test
    public void testPutArchiveEntry_addPaxHeadersForNonAsciiNames() throws IOException {
        tos.setAddPaxHeadersForNonAsciiNames(true);
        TarArchiveEntry entry = new TarArchiveEntry("non_ascii_\u00e4\u00f6\u00fc.txt");
        entry.setSize(0);

        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.finish();

        assertTrue(baos.size() > 0);
    }

    // Tests big number mode with BIGNUMBER_POSIX
    @Test
    public void testPutArchiveEntry_bigNumberPosixMode_succeeds() throws IOException {
        tos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);
        TarArchiveEntry entry = new TarArchiveEntry("big_file.txt");
        entry.setSize(0100000000000L);

        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.finish();

        assertTrue(baos.size() > 0);
    }

    // Tests big number mode with BIGNUMBER_STAR
    @Test
    public void testPutArchiveEntry_bigNumberStarMode_succeeds() throws IOException {
        tos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_STAR);
        TarArchiveEntry entry = new TarArchiveEntry("big_file.txt");
        entry.setSize(0100000000000L);

        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.finish();

        assertTrue(baos.size() > 0);
    }

    // Tests big number mode with BIGNUMBER_ERROR
    @Test(expected = RuntimeException.class)
    public void testPutArchiveEntry_bigNumberErrorMode_throwsException() throws IOException {
        tos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_ERROR);
        TarArchiveEntry entry = new TarArchiveEntry("big_file.txt");
        entry.setSize(0100000000000L);

        tos.putArchiveEntry(entry);
    }

    // Tests putArchiveEntry after finish throws IOException
    @Test(expected = IOException.class)
    public void testPutArchiveEntry_afterFinish_throwsIOException() throws IOException {
        tos.finish();
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        tos.putArchiveEntry(entry);
    }

    // Tests closeArchiveEntry without putArchiveEntry throws IOException
    @Test(expected = IOException.class)
    public void testCloseArchiveEntry_withoutPutArchiveEntry_throwsIOException() throws IOException {
        tos.closeArchiveEntry();
    }

    // Tests write after finish throws IOException
    @Test(expected = IOException.class)
    public void testWrite_afterFinish_throwsIOException() throws IOException {
        tos.finish();
        tos.write(new byte[]{1, 2, 3});
    }
}