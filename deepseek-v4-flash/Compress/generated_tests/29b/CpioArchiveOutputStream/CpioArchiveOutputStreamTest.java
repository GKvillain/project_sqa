package org.apache.commons.compress.archivers.cpio;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import static org.junit.Assert.*;
import org.junit.Test;

import org.apache.commons.compress.archivers.cpio.CpioArchiveEntry;
import org.apache.commons.compress.archivers.cpio.CpioArchiveOutputStream;
import org.apache.commons.compress.archivers.cpio.CpioConstants;
import org.apache.commons.compress.archivers.zip.ZipEncoding;
import org.apache.commons.compress.archivers.zip.ZipEncodingHelper;

public class CpioArchiveOutputStreamTest {

    // Helper: create a standard CpioArchiveEntry with given format, name and size
    private CpioArchiveEntry createEntry(short format, String name, long size) {
        CpioArchiveEntry entry = new CpioArchiveEntry(format);
        entry.setName(name);
        entry.setSize(size);
        entry.setMode(33188); // regular file, octal 100644
        entry.setTime(1234567890);
        entry.setNumberOfLinks(1);
        return entry;
    }

    // Helper: parse a hex string from a substring of a byte array
    private long parseHexString(String hex) {
        return Long.parseLong(hex, 16);
    }

    // Test constructor with unrecognised format
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_invalidFormat_throwsIllegalArgumentException() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        new CpioArchiveOutputStream(bos, (short) 99);
    }

    // Test duplicate entry name detection
    @Test(expected = IOException.class)
    public void testPutArchiveEntry_duplicateEntry_throwsIOException() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos, CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry1 = createEntry(CpioConstants.FORMAT_NEW, "file.txt", 0);
        CpioArchiveEntry entry2 = createEntry(CpioConstants.FORMAT_NEW, "file.txt", 0);
        out.putArchiveEntry(entry1);
        out.putArchiveEntry(entry2);
    }

    // Test format mismatch between entry and stream
    @Test(expected = IOException.class)
    public void testPutArchiveEntry_formatMismatch_throwsIOException() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos, CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry = createEntry(CpioConstants.FORMAT_OLD_ASCII, "file.txt", 0);
        out.putArchiveEntry(entry);
    }

    // Test putArchiveEntry after the stream has been finished
    @Test(expected = IOException.class)
    public void testPutArchiveEntry_finishedStream_throwsIOException() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos, CpioConstants.FORMAT_NEW);
        out.finish();
        out.putArchiveEntry(createEntry(CpioConstants.FORMAT_NEW, "file.txt", 0));
    }

    // Test that setting entry time to -1 causes it to be updated to current time
    @Test
    public void testPutArchiveEntry_negativeTime_setsCurrentTime() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos, CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry = createEntry(CpioConstants.FORMAT_NEW, "test.txt", 0);
        entry.setTime(-1);
        out.putArchiveEntry(entry);
        assertTrue("Entry time should be set to positive current time", entry.getTime() > 0);
        out.closeArchiveEntry();
        out.finish();
        out.close();
    }

    // Test write when no entry is active
    @Test(expected = IOException.class)
    public void testWrite_noEntry_throwsIOException() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos, CpioConstants.FORMAT_NEW);
        out.write(new byte[]{1,2,3}, 0, 3);
    }

    // Test write exceeding the entry size
    @Test(expected = IOException.class)
    public void testWrite_pastEndOfEntry_throwsIOException() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos, CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry = createEntry(CpioConstants.FORMAT_NEW, "file.txt", 5);
        out.putArchiveEntry(entry);
        out.write(new byte[]{1,2,3,4,5,6}, 0, 6);
    }

    // Test closeArchiveEntry when no entry exists
    @Test(expected = IOException.class)
    public void testCloseArchiveEntry_noEntry_throwsIOException() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos, CpioConstants.FORMAT_NEW);
        out.closeArchiveEntry();
    }

    // Test closeArchiveEntry when written size doesn't match entry size
    @Test(expected = IOException.class)
    public void testCloseArchiveEntry_sizeMismatch_throwsIOException() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos, CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry = createEntry(CpioConstants.FORMAT_NEW, "file.txt", 5);
        out.putArchiveEntry(entry);
        out.write(new byte[]{1,2,3}, 0, 3);
        out.closeArchiveEntry();
    }

    // Test closeArchiveEntry CRC mismatch
    @Test(expected = IOException.class)
    public void testCloseArchiveEntry_crcError_throwsIOException() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos, CpioConstants.FORMAT_NEW_CRC);
        CpioArchiveEntry entry = createEntry(CpioConstants.FORMAT_NEW_CRC, "file.txt", 4);
        entry.setChksum(0);
        out.putArchiveEntry(entry);
        out.write(new byte[]{1,2,3,4}, 0, 4);
        out.closeArchiveEntry();
    }

    // Test closeArchiveEntry with correct CRC
    @Test
    public void testCloseArchiveEntry_crcCorrect_succeeds() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos, CpioConstants.FORMAT_NEW_CRC);
        CpioArchiveEntry entry = createEntry(CpioConstants.FORMAT_NEW_CRC, "file.txt", 4);
        byte[] data = {1, 2, 3, 4};
        long crcExpected = 0;
        for (byte b : data) {
            crcExpected += (b & 0xFF);
        }
        entry.setChksum(crcExpected);
        out.putArchiveEntry(entry);
        out.write(data, 0, data.length);
        out.closeArchiveEntry();
        out.finish();
        out.close();
    }

    // Test finish when there is an unclosed entry
    @Test(expected = IOException.class)
    public void testFinish_unclosedEntry_throwsIOException() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos, CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry = createEntry(CpioConstants.FORMAT_NEW, "file.txt", 0);
        out.putArchiveEntry(entry);
        out.finish();
    }

    // Test finish on an already finished stream
    @Test(expected = IOException.class)
    public void testFinish_finishedStream_throwsIOException() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos, CpioConstants.FORMAT_NEW);
        out.finish();
        out.finish();
    }

    // Test finish pads the archive to the block size
    @Test
    public void testFinish_padsToBlockSize() throws IOException {
        final int blockSize = 512;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos, CpioConstants.FORMAT_NEW, blockSize);
        CpioArchiveEntry entry = createEntry(CpioConstants.FORMAT_NEW, "test.txt", 0);
        out.putArchiveEntry(entry);
        out.closeArchiveEntry();
        out.finish();
        out.close();
        assertTrue("Archive length must be a multiple of block size", bos.size() % blockSize == 0);
    }

    // Test close after finish (should not throw)
    @Test
    public void testClose_afterFinish_closesOnce() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos, CpioConstants.FORMAT_NEW);
        out.finish();
        out.close();
        out.close();
    }

    // Test closeArchiveEntry on a finished stream
    @Test(expected = IOException.class)
    public void testCloseArchiveEntry_finishedStream_throwsIOException() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos, CpioConstants.FORMAT_NEW);
        out.finish();
        out.closeArchiveEntry();
    }

    // Test write with invalid offsets/lengths
    @Test
    public void testWrite_invalidOffset_throwsIndexOutOfBoundsException() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos, CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry = createEntry(CpioConstants.FORMAT_NEW, "file.txt", 0);
        out.putArchiveEntry(entry);
        // negative offset
        try {
            out.write(new byte[5], -1, 2);
            fail("Should have thrown IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) { /* expected */ }
        // negative length
        try {
            out.write(new byte[5], 0, -1);
            fail("Should have thrown IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) { /* expected */ }
        // offset+length > array length
        try {
            out.write(new byte[5], 3, 3);
            fail("Should have thrown IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) { /* expected */ }
        out.closeArchiveEntry();
        out.finish();
        out.close();
    }

    // Test that non-ASCII entry name results in correct name length field in header
    @Test
    public void testNonAsciiName_correctNameLength() throws IOException {
        String nonAsciiName = "täst.txt"; // 9 chars, encoded in UTF-8 = 10 bytes
        // Compute expected encoded length using the same encoding as the stream
        ZipEncoding zipEncoding = ZipEncodingHelper.getZipEncoding("UTF-8");
        ByteBuffer buf = zipEncoding.encode(nonAsciiName);
        long expectedEncodedLength = buf.limit() - buf.position();
        long expectedNameLenField = expectedEncodedLength + 1; // +1 for null terminator

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos, CpioConstants.FORMAT_NEW, 512, "UTF-8");
        CpioArchiveEntry entry = createEntry(CpioConstants.FORMAT_NEW, nonAsciiName, 0);
        out.putArchiveEntry(entry);
        out.closeArchiveEntry();
        out.finish();
        out.close();

        byte[] archive = bos.toByteArray();
        // Name length field is at offset 6 (magic) + 12 * 8 (fields before name length)
        int nameLenOffset = 6 + 12 * 8;
        int fieldLen = 8;
        String hexField = new String(archive, nameLenOffset, fieldLen, "US-ASCII");
        long actualNameLen = parseHexString(hexField);
        assertEquals("Name length field must equal encoded length + 1", expectedNameLenField, actualNameLen);
    }

    // Test basic write with old ASCII and old binary formats
    @Test
    public void testWriteWithOldFormats_success() throws IOException {
        // Old ASCII
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos, CpioConstants.FORMAT_OLD_ASCII);
        CpioArchiveEntry entry = createEntry(CpioConstants.FORMAT_OLD_ASCII, "old.txt", 3);
        out.putArchiveEntry(entry);
        out.write(new byte[]{1,2,3}, 0, 3);
        out.closeArchiveEntry();
        out.finish();
        out.close();
        byte[] bytes = bos.toByteArray();
        assertEquals("070707", new String(bytes, 0, 6, "US-ASCII"));

        // Old Binary (only check that it does not throw and produces data)
        ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
        CpioArchiveOutputStream out2 = new CpioArchiveOutputStream(bos2, CpioConstants.FORMAT_OLD_BINARY);
        CpioArchiveEntry entry2 = createEntry(CpioConstants.FORMAT_OLD_BINARY, "oldbin.txt", 1);
        out2.putArchiveEntry(entry2);
        out2.write(new byte[]{0x42}, 0, 1);
        out2.closeArchiveEntry();
        out2.finish();
        out2.close();
        assertTrue("Binary archive should have content", bos2.size() > 0);
    }

    // Basic success case: write an entry, close, finish, and verify magic
    @Test
    public void testWriteBasic_success() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos, CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry = createEntry(CpioConstants.FORMAT_NEW, "basic.txt", 5);
        out.putArchiveEntry(entry);
        // Also test early return for zero-length write
        out.write(new byte[0], 0, 0);
        out.write(new byte[]{0,1,2,3,4}, 0, 5);
        out.closeArchiveEntry();
        out.finish();
        out.close();
        byte[] bytes = bos.toByteArray();
        assertEquals("070701", new String(bytes, 0, 6, "US-ASCII"));
    }
}