package org.apache.commons.compress.archivers.cpio;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CpioArchiveOutputStreamTest {

    private ByteArrayOutputStream baos;

    @Before
    public void setUp() {
        baos = new ByteArrayOutputStream();
    }

    // Tests constructor with invalid format throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_invalidFormat_throwsIllegalArgumentException() {
        new CpioArchiveOutputStream(baos, (short) 999);
    }

    // Tests normal entry writing with FORMAT_NEW
    @Test
    public void testPutArchiveEntry_formatNew_writesSuccessfully() throws IOException {
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "test.txt", 4);
        out.putArchiveEntry(entry);
        out.write(new byte[]{1, 2, 3, 4}, 0, 4);
        out.closeArchiveEntry();
        out.finish();
        out.close();

        assertTrue(baos.size() > 0);
    }

    // Tests default constructor uses FORMAT_NEW
    @Test
    public void testConstructor_defaultFormat_usesFormatNew() throws IOException {
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos);
        CpioArchiveEntry entry = new CpioArchiveEntry("default.txt", 0);
        out.putArchiveEntry(entry);
        out.closeArchiveEntry();
        out.close();

        assertTrue(baos.size() > 0);
    }

    // Tests normal entry writing with FORMAT_OLD_ASCII
    @Test
    public void testPutArchiveEntry_formatOldAscii_writesSuccessfully() throws IOException {
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_OLD_ASCII);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_OLD_ASCII, "old_ascii.txt", 3);
        out.putArchiveEntry(entry);
        out.write(new byte[]{'a', 'b', 'c'});
        out.closeArchiveEntry();
        out.close();

        assertTrue(baos.size() > 0);
    }

    // Tests normal entry writing with FORMAT_OLD_BINARY
    @Test
    public void testPutArchiveEntry_formatOldBinary_writesSuccessfully() throws IOException {
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_OLD_BINARY);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_OLD_BINARY, "old_bin.txt", 2);
        out.putArchiveEntry(entry);
        out.write(new byte[]{10, 20});
        out.closeArchiveEntry();
        out.close();

        assertTrue(baos.size() > 0);
    }

    // Tests FORMAT_NEW_CRC with correct checksum validation
    @Test
    public void testPutArchiveEntry_formatNewCrc_validChecksumSucceeds() throws IOException {
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW_CRC);
        byte[] data = new byte[]{10, 20, 30};
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW_CRC, "crc.txt", data.length);
        entry.setChksum(60); // 10 + 20 + 30 = 60
        out.putArchiveEntry(entry);
        out.write(data);
        out.closeArchiveEntry();
        out.close();

        assertTrue(baos.size() > 0);
    }

    // Tests FORMAT_NEW_CRC with incorrect checksum throws IOException
    @Test(expected = IOException.class)
    public void testCloseArchiveEntry_formatNewCrcMismatch_throwsIOException() throws IOException {
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW_CRC);
        byte[] data = new byte[]{10, 20, 30};
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW_CRC, "crc_fail.txt", data.length);
        entry.setChksum(999); // Wrong checksum
        out.putArchiveEntry(entry);
        out.write(data);
        out.closeArchiveEntry();
    }

    // Tests format mismatch between entry and stream throws IOException
    @Test(expected = IOException.class)
    public void testPutArchiveEntry_formatMismatch_throwsIOException() throws IOException {
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_OLD_ASCII, "mismatch.txt", 0);
        out.putArchiveEntry(entry);
    }

    // Tests duplicate entry name throws IOException
    @Test(expected = IOException.class)
    public void testPutArchiveEntry_duplicateEntryName_throwsIOException() throws IOException {
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry1 = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "dup.txt", 0);
        CpioArchiveEntry entry2 = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "dup.txt", 0);
        out.putArchiveEntry(entry1);
        out.closeArchiveEntry();
        out.putArchiveEntry(entry2);
    }

    // Tests putting a new entry automatically closes previous entry
    @Test
    public void testPutArchiveEntry_autoClosesPreviousEntry_succeeds() throws IOException {
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry1 = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "file1.txt", 0);
        CpioArchiveEntry entry2 = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "file2.txt", 0);
        out.putArchiveEntry(entry1);
        out.putArchiveEntry(entry2);
        out.closeArchiveEntry();
        out.close();

        assertTrue(baos.size() > 0);
    }

    // Tests write without open entry throws IOException
    @Test(expected = IOException.class)
    public void testWrite_noCurrentEntry_throwsIOException() throws IOException {
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        out.write(new byte[]{1, 2, 3});
    }

    // Tests write with negative offset throws IndexOutOfBoundsException
    @Test(expected = IndexOutOfBoundsException.class)
    public void testWrite_negativeOffset_throwsIndexOutOfBoundsException() throws IOException {
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "test.txt", 5);
        out.putArchiveEntry(entry);
        out.write(new byte[5], -1, 3);
    }

    // Tests write with negative length throws IndexOutOfBoundsException
    @Test(expected = IndexOutOfBoundsException.class)
    public void testWrite_negativeLength_throwsIndexOutOfBoundsException() throws IOException {
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "test.txt", 5);
        out.putArchiveEntry(entry);
        out.write(new byte[5], 0, -1);
    }

    // Tests write with offset + length exceeding buffer length throws IndexOutOfBoundsException
    @Test(expected = IndexOutOfBoundsException.class)
    public void testWrite_offsetLengthExceedsBuffer_throwsIndexOutOfBoundsException() throws IOException {
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "test.txt", 5);
        out.putArchiveEntry(entry);
        out.write(new byte[5], 3, 3);
    }

    // Tests writing 0 length does nothing and succeeds
    @Test
    public void testWrite_zeroLength_doesNothing() throws IOException {
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "test.txt", 0);
        out.putArchiveEntry(entry);
        out.write(new byte[5], 0, 0);
        out.closeArchiveEntry();
        out.close();

        assertTrue(baos.size() > 0);
    }

    // Tests write past specified entry size throws IOException
    @Test(expected = IOException.class)
    public void testWrite_pastEndOfEntry_throwsIOException() throws IOException {
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "test.txt", 2);
        out.putArchiveEntry(entry);
        out.write(new byte[]{1, 2, 3});
    }

    // Tests closeArchiveEntry with size mismatch throws IOException
    @Test(expected = IOException.class)
    public void testCloseArchiveEntry_sizeMismatch_throwsIOException() throws IOException {
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "test.txt", 5);
        out.putArchiveEntry(entry);
        out.write(new byte[]{1, 2});
        out.closeArchiveEntry();
    }

    // Tests finish with unclosed entry throws IOException
    @Test(expected = IOException.class)
    public void testFinish_unclosedEntry_throwsIOException() throws IOException {
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "test.txt", 2);
        out.putArchiveEntry(entry);
        out.finish();
    }

    // Tests multiple finish calls do not throw exception
    @Test
    public void testFinish_multipleCalls_noOp() throws IOException {
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        out.finish();
        out.finish();
        out.close();
    }

    // Tests operations on closed stream throw IOException
    @Test(expected = IOException.class)
    public void testPutArchiveEntry_closedStream_throwsIOException() throws IOException {
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        out.close();
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "test.txt", 0);
        out.putArchiveEntry(entry);
    }

    // Tests createArchiveEntry returns CpioArchiveEntry instance with expected name
    @Test
    public void testCreateArchiveEntry_validInput_returnsCpioArchiveEntry() throws IOException {
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        File dummyFile = new File("someFile.txt");
        ArchiveEntry entry = out.createArchiveEntry(dummyFile, "customName.txt");
        assertNotNull(entry);
        assertTrue(entry instanceof CpioArchiveEntry);
        assertEquals("customName.txt", entry.getName());
    }

    // Tests constructor with custom block size and encoding
    @Test
    public void testConstructor_customBlockSizeAndEncoding_writesSuccessfully() throws IOException {
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW, 1024, "UTF-8");
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "custom_block.txt", 4);
        out.putArchiveEntry(entry);
        out.write(new byte[]{1, 2, 3, 4});
        out.closeArchiveEntry();
        out.finish();
        out.close();

        assertEquals(0, baos.size() % 1024);
    }

    // Tests constructor with custom block size
    @Test
    public void testConstructor_customBlockSize_writesSuccessfully() throws IOException {
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW, 512);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "block512.txt", 0);
        out.putArchiveEntry(entry);
        out.closeArchiveEntry();
        out.finish();
        out.close();

        assertEquals(0, baos.size() % 512);
    }

    // Tests closeArchiveEntry when no entry is open throws IOException
    @Test(expected = IOException.class)
    public void testCloseArchiveEntry_noCurrentEntry_throwsIOException() throws IOException {
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        out.closeArchiveEntry();
    }

    // Tests write on closed stream throws IOException
    @Test(expected = IOException.class)
    public void testWrite_closedStream_throwsIOException() throws IOException {
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        out.close();
        out.write(new byte[]{1, 2, 3}, 0, 3);
    }

    // Tests createArchiveEntry on closed stream throws IOException
    @Test(expected = IOException.class)
    public void testCreateArchiveEntry_closedStream_throwsIOException() throws IOException {
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        out.close();
        out.createArchiveEntry(new File("someFile.txt"), "someFile.txt");
    }

    // Tests close called multiple times does not throw exception
    @Test
    public void testClose_multipleCalls_noOp() throws IOException {
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        out.close();
        out.close();
    }
}