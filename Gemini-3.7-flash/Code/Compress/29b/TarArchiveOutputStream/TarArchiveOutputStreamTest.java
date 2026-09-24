package org.apache.commons.compress.archivers.tar;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

public class TarArchiveOutputStreamTest {

    // Tests normal entry lifecycle and byte counting
    @Test
    public void testPutArchiveEntry_normalEntry_writesSuccessfully() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        byte[] data = "Hello World".getBytes();
        entry.setSize(data.length);
        
        tos.putArchiveEntry(entry);
        tos.write(data, 0, data.length);
        tos.closeArchiveEntry();
        tos.finish();
        tos.close();

        assertTrue(tos.getBytesWritten() > 0);
        assertEquals(tos.getBytesWritten(), tos.getCount());
        assertEquals(TarConstants.DEFAULT_RCDSIZE, tos.getRecordSize());
    }

    // Tests custom constructors and encoding settings
    @Test
    public void testConstructor_withBlockAndRecordSizeAndEncoding_initializesCorrectly() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos, 1024, 512, "UTF-8");
        assertEquals(512, tos.getRecordSize());
        tos.close();
    }

    // Tests long file name throwing exception in LONGFILE_ERROR mode
    @Test(expected = RuntimeException.class)
    public void testPutArchiveEntry_longFileNameErrorMode_throwsException() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_ERROR);

        String longName = "a/very/long/path/name/that/exceeds/the/tar/maximum/name/length/limit/of/one/hundred/bytes/testfile1234567890.txt";
        TarArchiveEntry entry = new TarArchiveEntry(longName);
        entry.setSize(0);

        tos.putArchiveEntry(entry);
    }

    // Tests long file name using GNU extension
    @Test
    public void testPutArchiveEntry_longFileNameGnuMode_writesEntry() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);

        String longName = "a/very/long/path/name/that/exceeds/the/tar/maximum/name/length/limit/of/one/hundred/bytes/testfile1234567890.txt";
        TarArchiveEntry entry = new TarArchiveEntry(longName);
        entry.setSize(0);

        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.finish();
        tos.close();

        assertTrue(bos.size() > 0);
    }

    // Tests long file name using POSIX PAX header extension
    @Test
    public void testPutArchiveEntry_longFileNamePosixMode_writesEntry() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);

        String longName = "a/very/long/path/name/that/exceeds/the/tar/maximum/name/length/limit/of/one/hundred/bytes/testfile1234567890.txt";
        TarArchiveEntry entry = new TarArchiveEntry(longName);
        entry.setSize(0);

        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.finish();
        tos.close();

        assertTrue(bos.size() > 0);
    }

    // Tests long link name using GNU extension
    @Test
    public void testPutArchiveEntry_longLinkNameGnuMode_writesEntry() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);

        TarArchiveEntry entry = new TarArchiveEntry("symlink", TarConstants.LF_SYMLINK);
        String longTarget = "a/very/long/link/target/path/that/exceeds/the/tar/maximum/name/length/limit/of/one/hundred/bytes/target12345.txt";
        entry.setLinkName(longTarget);
        entry.setSize(0);

        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.finish();
        tos.close();

        assertTrue(bos.size() > 0);
    }

    // Tests big number error mode throws exception on oversized size
    @Test(expected = RuntimeException.class)
    public void testPutArchiveEntry_bigNumberErrorMode_throwsException() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_ERROR);

        TarArchiveEntry entry = new TarArchiveEntry("bigfile.dat");
        entry.setSize(TarConstants.MAXSIZE + 1);

        tos.putArchiveEntry(entry);
    }

    // Tests big number POSIX mode writes PAX header for large size
    @Test
    public void testPutArchiveEntry_bigNumberPosixMode_writesEntry() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);

        TarArchiveEntry entry = new TarArchiveEntry("bigfile.dat");
        entry.setSize(0);
        entry.setUserId(TarConstants.MAXID + 1);
        entry.setGroupId(TarConstants.MAXID + 1);
        entry.setDevMajor((int) TarConstants.MAXID + 1);
        entry.setDevMinor((int) TarConstants.MAXID + 1);

        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.finish();
        tos.close();

        assertTrue(bos.size() > 0);
    }

    // Tests big number STAR mode
    @Test
    public void testPutArchiveEntry_bigNumberStarMode_writesEntry() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_STAR);

        TarArchiveEntry entry = new TarArchiveEntry("starfile.dat");
        entry.setSize(0);

        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.finish();
        tos.close();

        assertTrue(bos.size() > 0);
    }

    // Tests non-ASCII name generating PAX headers
    @Test
    public void testPutArchiveEntry_nonAsciiNameWithPaxHeader_writesPaxHeader() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setAddPaxHeadersForNonAsciiNames(true);

        TarArchiveEntry entry = new TarArchiveEntry("non-ascii-\u00e4\u00f6\u00fc.txt");
        entry.setSize(0);

        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.finish();
        tos.close();

        assertTrue(bos.size() > 0);
    }

    // Tests directory entry handling (directory size forced to 0)
    @Test
    public void testPutArchiveEntry_directory_handledCorrectly() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        TarArchiveEntry entry = new TarArchiveEntry("mydir/");
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.finish();
        tos.close();

        assertTrue(bos.size() > 0);
    }

    // Tests write exceeding declared entry size throws exception
    @Test(expected = IOException.class)
    public void testWrite_exceedingSize_throwsException() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        TarArchiveEntry entry = new TarArchiveEntry("small.txt");
        entry.setSize(5);

        tos.putArchiveEntry(entry);
        byte[] data = new byte[10];
        tos.write(data, 0, data.length);
    }

    // Tests closeArchiveEntry called before writing all bytes throws exception
    @Test(expected = IOException.class)
    public void testCloseArchiveEntry_unwrittenBytes_throwsException() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        TarArchiveEntry entry = new TarArchiveEntry("file.txt");
        entry.setSize(100);

        tos.putArchiveEntry(entry);
        tos.write(new byte[10], 0, 10);
        tos.closeArchiveEntry();
    }

    // Tests write called without open entry throws IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testWrite_noOpenEntry_throwsException() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.write(new byte[10], 0, 10);
    }

    // Tests closeArchiveEntry called without open entry throws IOException
    @Test(expected = IOException.class)
    public void testCloseArchiveEntry_noCurrentEntry_throwsException() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.closeArchiveEntry();
    }

    // Tests finish called with unclosed entry throws IOException
    @Test(expected = IOException.class)
    public void testFinish_unclosedEntry_throwsException() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        TarArchiveEntry entry = new TarArchiveEntry("file.txt");
        entry.setSize(0);

        tos.putArchiveEntry(entry);
        tos.finish();
    }

    // Tests finish called twice throws IOException
    @Test(expected = IOException.class)
    public void testFinish_alreadyFinished_throwsException() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.finish();
        tos.finish();
    }

    // Tests chunked assembly buffer writing across multiple records
    @Test
    public void testWrite_chunkedBufferAssembly_writesCorrectly() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        byte[] payload = new byte[1500];
        for (int i = 0; i < payload.length; i++) {
            payload[i] = (byte) (i % 128);
        }

        TarArchiveEntry entry = new TarArchiveEntry("large.bin");
        entry.setSize(payload.length);

        tos.putArchiveEntry(entry);
        tos.write(payload, 0, 300);
        tos.write(payload, 300, 400);
        tos.write(payload, 700, 800);
        tos.closeArchiveEntry();
        tos.finish();
        tos.close();

        assertTrue(bos.size() > payload.length);
    }

    // Tests createArchiveEntry method
    @Test
    public void testCreateArchiveEntry_validFile_returnsEntry() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        File tempFile = File.createTempFile("tartest", ".tmp");
        try {
            TarArchiveEntry entry = (TarArchiveEntry) tos.createArchiveEntry(tempFile, "entryName.txt");
            assertNotNull(entry);
            assertEquals("entryName.txt", entry.getName());
        } finally {
            tempFile.delete();
            tos.close();
        }
    }

    // Tests writing explicit PAX headers directly
    @Test
    public void testWritePaxHeaders_customMap_writesSuccessfully() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.setModTime(new Date(1000000000000L));
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("path", "custom/path/test.txt");
        headers.put("comment", "This is a custom PAX header entry");

        tos.writePaxHeaders(entry, "test.txt", headers);
        tos.finish();
        tos.close();

        assertTrue(bos.size() > 0);
    }
}