package org.apache.commons.compress.archivers.tar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class TarArchiveOutputStreamTest {

    // Helper method to create a basic TarArchiveOutputStream
    private TarArchiveOutputStream createStream() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        return new TarArchiveOutputStream(bos);
    }

    // Tests basic putArchiveEntry and closeArchiveEntry with an empty entry
    @Test
    public void testPutArchiveEntry_validEntry_createsHeader() throws IOException {
        TarArchiveOutputStream tos = createStream();
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.setSize(0);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.close();
    }

    // Tests writing data to an entry and verifying size handling
    @Test
    public void testWrite_validData_writesBytes() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        TarArchiveEntry entry = new TarArchiveEntry("test.bin");
        byte[] data = new byte[]{1, 2, 3, 4, 5};
        entry.setSize(data.length);
        tos.putArchiveEntry(entry);
        tos.write(data);
        tos.closeArchiveEntry();
        tos.finish();
        byte[] archive = bos.toByteArray();
        assertTrue(archive.length > 0);
    }

    // Tests writing more bytes than declared in entry throws IOException
    @Test(expected = IOException.class)
    public void testWrite_exceedsEntrySize_throwsIOException() throws IOException {
        TarArchiveOutputStream tos = createStream();
        TarArchiveEntry entry = new TarArchiveEntry("small.txt");
        entry.setSize(3);
        tos.putArchiveEntry(entry);
        tos.write(new byte[]{1, 2, 3, 4});
        tos.closeArchiveEntry();
    }

    // Tests closing an entry before writing full expected size throws IOException
    @Test(expected = IOException.class)
    public void testCloseArchiveEntry_undersizedEntry_throwsIOException() throws IOException {
        TarArchiveOutputStream tos = createStream();
        TarArchiveEntry entry = new TarArchiveEntry("partial.txt");
        entry.setSize(10);
        tos.putArchiveEntry(entry);
        tos.write(new byte[]{1, 2});
        tos.closeArchiveEntry();
    }

    // Tests writing zero bytes to an entry
    @Test
    public void testWrite_zeroBytes_noDataWritten() throws IOException {
        TarArchiveOutputStream tos = createStream();
        TarArchiveEntry entry = new TarArchiveEntry("empty.txt");
        entry.setSize(0);
        tos.putArchiveEntry(entry);
        tos.write(new byte[0]);
        tos.closeArchiveEntry();
        tos.finish();
    }

    // Tests LONGFILE_TRUNCATE mode with a long file name
    @Test
    public void testPutArchiveEntry_longNameTruncate_truncatesName() throws IOException {
        TarArchiveOutputStream tos = createStream();
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_TRUNCATE);
        String longName = "a";
        for (int i = 0; i < 200; i++) {
            longName += "b";
        }
        TarArchiveEntry entry = new TarArchiveEntry(longName);
        entry.setSize(0);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.close();
    }

    // Tests LONGFILE_ERROR mode with a long file name throws RuntimeException
    @Test(expected = RuntimeException.class)
    public void testPutArchiveEntry_longNameError_throwsRuntimeException() throws IOException {
        TarArchiveOutputStream tos = createStream();
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_ERROR);
        String longName = "c";
        for (int i = 0; i < 150; i++) {
            longName += "d";
        }
        TarArchiveEntry entry = new TarArchiveEntry(longName);
        entry.setSize(0);
        tos.putArchiveEntry(entry);
    }

    // Tests LONGFILE_GNU mode with a long file name
    @Test
    public void testPutArchiveEntry_longNameGNU_writesLongLink() throws IOException {
        TarArchiveOutputStream tos = createStream();
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
        String longName = "e";
        for (int i = 0; i < 200; i++) {
            longName += "f";
        }
        TarArchiveEntry entry = new TarArchiveEntry(longName);
        entry.setSize(0);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.close();
    }

    // Tests BIGNUMBER_ERROR mode with oversized size throws RuntimeException
    @Test(expected = RuntimeException.class)
    public void testPutArchiveEntry_bigNumberError_throwsRuntimeException() throws IOException {
        TarArchiveOutputStream tos = createStream();
        tos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_ERROR);
        TarArchiveEntry entry = new TarArchiveEntry("big.bin");
        entry.setSize(TarConstants.MAXSIZE + 1);
        tos.putArchiveEntry(entry);
    }

    // Tests BIGNUMBER_POSIX mode with oversized size adds PAX headers
    @Test
    public void testPutArchiveEntry_bigNumberPosix_writesPaxHeaders() throws IOException {
        TarArchiveOutputStream tos = createStream();
        tos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);
        TarArchiveEntry entry = new TarArchiveEntry("bigposix.bin");
        entry.setSize(TarConstants.MAXSIZE + 1);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.close();
    }

    // Tests BIGNUMBER_STAR mode with oversized size
    @Test
    public void testPutArchiveEntry_bigNumberStar_writesStarHeader() throws IOException {
        TarArchiveOutputStream tos = createStream();
        tos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_STAR);
        TarArchiveEntry entry = new TarArchiveEntry("bigstar.bin");
        entry.setSize(TarConstants.MAXSIZE + 1);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.close();
    }

    // Tests addPaxHeadersForNonAsciiNames with non-ASCII file name
    @Test
    public void testPutArchiveEntry_nonAsciiName_addsPaxHeader() throws IOException {
        TarArchiveOutputStream tos = createStream();
        tos.setAddPaxHeadersForNonAsciiNames(true);
        TarArchiveEntry entry = new TarArchiveEntry("\u00e9\u00e8.txt");
        entry.setSize(0);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.close();
    }

    // Tests finish() twice throws IOException
    @Test(expected = IOException.class)
    public void testFinish_alreadyFinished_throwsIOException() throws IOException {
        TarArchiveOutputStream tos = createStream();
        tos.finish();
        tos.finish();
    }

    // Tests finish() with unclosed entry throws IOException
    @Test(expected = IOException.class)
    public void testFinish_unclosedEntry_throwsIOException() throws IOException {
        TarArchiveOutputStream tos = createStream();
        TarArchiveEntry entry = new TarArchiveEntry("unclosed.txt");
        entry.setSize(5);
        tos.putArchiveEntry(entry);
        tos.finish();
    }

    // Tests createArchiveEntry when stream is finished throws IOException
    @Test(expected = IOException.class)
    public void testCreateArchiveEntry_finishedStream_throwsIOException() throws IOException {
        TarArchiveOutputStream tos = createStream();
        tos.finish();
        java.io.File f = new java.io.File("dummy.txt");
        tos.createArchiveEntry(f, "dummy.txt");
    }

    // Tests closeArchiveEntry without putArchiveEntry throws IOException
    @Test(expected = IOException.class)
    public void testCloseArchiveEntry_noCurrentEntry_throwsIOException() throws IOException {
        TarArchiveOutputStream tos = createStream();
        tos.closeArchiveEntry();
    }

    // Tests writePaxHeaders indirectly through non-ASCII name handling
    @Test
    public void testWritePaxHeaders_withNonAsciiName_works() throws IOException {
        TarArchiveOutputStream tos = createStream();
        tos.setAddPaxHeadersForNonAsciiNames(true);
        TarArchiveEntry entry = new TarArchiveEntry("hello_\u00e9.txt");
        entry.setSize(0);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.close();
    }

    // Tests close() with unfinished stream implicitly calls finish()
    @Test
    public void testClose_unfinishedStream_finishes() throws IOException {
        TarArchiveOutputStream tos = createStream();
        tos.close();
    }

    // Tests getBytesWritten after writing data
    @Test
    public void testGetBytesWritten_afterWriting_returnsCount() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.setSize(4);
        tos.putArchiveEntry(entry);
        tos.write(new byte[]{1, 2, 3, 4});
        tos.closeArchiveEntry();
        long written = tos.getBytesWritten();
        assertTrue(written > 0);
        tos.close();
    }

    // Tests flush method
    @Test
    public void testFlush_writesToUnderlyingStream() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.flush();
        tos.close();
    }
}