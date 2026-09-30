package org.apache.commons.compress.archivers.cpio;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;

public class CpioArchiveOutputStreamTest {

    private CpioArchiveOutputStream cpioOut;
    private ByteArrayOutputStream baos;

    private CpioArchiveOutputStream createStream(short format) {
        baos = new ByteArrayOutputStream();
        return new CpioArchiveOutputStream(baos, format);
    }

    private CpioArchiveEntry createEntry(String name, long size, short format) {
        CpioArchiveEntry entry = new CpioArchiveEntry(format);
        entry.setName(name);
        entry.setFileSize(size);
        entry.setTime(0);
        return entry;
    }

    // Tests invalid format in constructor
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_invalidFormat_throwsIllegalArgumentException() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new CpioArchiveOutputStream(out, (short) 0x9999);
    }

    // Tests duplicate entry name
    @Test(expected = IOException.class)
    public void testPutNextEntry_duplicateName_throwsIOException() throws IOException {
        cpioOut = createStream(CpioConstants.FORMAT_NEW);
        cpioOut.putNextEntry(createEntry("dup", 0, CpioConstants.FORMAT_NEW));
        cpioOut.putNextEntry(createEntry("dup", 0, CpioConstants.FORMAT_NEW));
    }

    // Tests putNextEntry after stream closed
    @Test(expected = IOException.class)
    public void testPutNextEntry_closedStream_throwsIOException() throws IOException {
        cpioOut = createStream(CpioConstants.FORMAT_NEW);
        cpioOut.close();
        cpioOut.putNextEntry(createEntry("a", 0, CpioConstants.FORMAT_NEW));
    }

    // Tests writing without current entry
    @Test(expected = IOException.class)
    public void testWrite_withoutEntry_throwsIOException() throws IOException {
        cpioOut = createStream(CpioConstants.FORMAT_NEW);
        cpioOut.write(new byte[]{1}, 0, 1);
    }

    // Tests writing more bytes than entry size
    @Test(expected = IOException.class)
    public void testWrite_pastEndOfEntry_throwsIOException() throws IOException {
        cpioOut = createStream(CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry = createEntry("overflow", 3, CpioConstants.FORMAT_NEW);
        cpioOut.putNextEntry(entry);
        cpioOut.write(new byte[]{1,2,3,4}, 0, 4);
    }

    // Tests closeArchiveEntry with incorrect size
    @Test(expected = IOException.class)
    public void testCloseArchiveEntry_wrongSize_throwsIOException() throws IOException {
        cpioOut = createStream(CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry = createEntry("short", 5, CpioConstants.FORMAT_NEW);
        cpioOut.putNextEntry(entry);
        cpioOut.write(new byte[]{1}, 0, 1);
        cpioOut.closeArchiveEntry();
    }

    // Tests valid write in new format
    @Test
    public void testWrite_validData_newFormat_success() throws IOException {
        cpioOut = createStream(CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry = createEntry("hello.txt", 5, CpioConstants.FORMAT_NEW);
        cpioOut.putNextEntry(entry);
        cpioOut.write(new byte[]{'h','e','l','l','o'}, 0, 5);
        cpioOut.closeArchiveEntry();
        cpioOut.finish();
        assertTrue(baos.size() > 0);
    }

    // Tests CRC mismatch causes exception
    @Test(expected = IOException.class)
    public void testCloseArchiveEntry_crcMismatch_throwsIOException() throws IOException {
        cpioOut = createStream(CpioConstants.FORMAT_NEW_CRC);
        CpioArchiveEntry entry = createEntry("crc.txt", 3, CpioConstants.FORMAT_NEW_CRC);
        entry.setChksum(999);
        cpioOut.putNextEntry(entry);
        cpioOut.write(new byte[]{'a','b','c'}, 0, 3);
        cpioOut.closeArchiveEntry();
    }

    // Tests CRC matching passes
    @Test
    public void testCloseArchiveEntry_crcMatch_success() throws IOException {
        cpioOut = createStream(CpioConstants.FORMAT_NEW_CRC);
        CpioArchiveEntry entry = createEntry("crc.txt", 3, CpioConstants.FORMAT_NEW_CRC);
        int sum = 'a' + 'b' + 'c';
        entry.setChksum(sum);
        cpioOut.putNextEntry(entry);
        cpioOut.write(new byte[]{'a','b','c'}, 0, 3);
        cpioOut.closeArchiveEntry();
        cpioOut.finish();
        assertTrue(baos.size() > 0);
    }

    // Tests writing in old ASCII format
    @Test
    public void testOldAsciiFormat_write_success() throws IOException {
        cpioOut = createStream(CpioConstants.FORMAT_OLD_ASCII);
        CpioArchiveEntry entry = createEntry("old.txt", 2, CpioConstants.FORMAT_OLD_ASCII);
        cpioOut.putNextEntry(entry);
        cpioOut.write(new byte[]{'x','y'}, 0, 2);
        cpioOut.closeArchiveEntry();
        cpioOut.finish();
        assertTrue(baos.size() > 0);
    }

    // Tests writing in old binary format
    @Test
    public void testOldBinaryFormat_write_success() throws IOException {
        cpioOut = createStream(CpioConstants.FORMAT_OLD_BINARY);
        CpioArchiveEntry entry = createEntry("bin.dat", 1, CpioConstants.FORMAT_OLD_BINARY);
        cpioOut.putNextEntry(entry);
        cpioOut.write(new byte[]{'z'}, 0, 1);
        cpioOut.closeArchiveEntry();
        cpioOut.finish();
        assertTrue(baos.size() > 0);
    }

    // Tests writing a single int byte
    @Test
    public void testWriteInt_insideEntry_success() throws IOException {
        cpioOut = createStream(CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry = createEntry("one", 1, CpioConstants.FORMAT_NEW);
        cpioOut.putNextEntry(entry);
        cpioOut.write('A');
        cpioOut.closeArchiveEntry();
        cpioOut.finish();
        assertTrue(baos.size() > 0);
    }

    // Tests finish is idempotent
    @Test
    public void testFinish_calledTwice_noError() throws IOException {
        cpioOut = createStream(CpioConstants.FORMAT_NEW);
        cpioOut.finish();
        cpioOut.finish();
        assertTrue(true);
    }

    // Tests after close, operations throw IOException
    @Test(expected = IOException.class)
    public void testClose_thenWrite_throwsIOException() throws IOException {
        cpioOut = createStream(CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry = createEntry("a", 0, CpioConstants.FORMAT_NEW);
        cpioOut.putNextEntry(entry);
        cpioOut.closeArchiveEntry();
        cpioOut.close();
        cpioOut.finish();
    }

    // Tests putArchiveEntry casting
    @Test
    public void testPutArchiveEntry_worksWithCpioEntry() throws IOException {
        cpioOut = createStream(CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry = createEntry("archive", 0, CpioConstants.FORMAT_NEW);
        cpioOut.putArchiveEntry(entry);
        cpioOut.closeArchiveEntry();
        cpioOut.finish();
        assertTrue(baos.size() > 0);
    }

    // Tests zero-length entry
    @Test
    public void testZeroSizeEntry_writeNoData_success() throws IOException {
        cpioOut = createStream(CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry = createEntry("empty", 0, CpioConstants.FORMAT_NEW);
        cpioOut.putNextEntry(entry);
        cpioOut.closeArchiveEntry();
        cpioOut.finish();
        assertTrue(baos.size() > 0);
    }

    // Tests write with len=0 does nothing
    @Test
    public void testWrite_zeroLengthNoEffect() throws IOException {
        cpioOut = createStream(CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry = createEntry("zero", 1, CpioConstants.FORMAT_NEW);
        cpioOut.putNextEntry(entry);
        cpioOut.write(new byte[]{1,2,3}, 0, 0);
        cpioOut.write(new byte[]{'a'}, 0, 1);
        cpioOut.closeArchiveEntry();
        cpioOut.finish();
        assertTrue(baos.size() > 0);
    }

    // Tests negative offset
    @Test(expected = IndexOutOfBoundsException.class)
    public void testWrite_negativeOffset_throwsIndexOutOfBounds() throws IOException {
        cpioOut = createStream(CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry = createEntry("bad", 1, CpioConstants.FORMAT_NEW);
        cpioOut.putNextEntry(entry);
        cpioOut.write(new byte[]{1}, -1, 1);
    }

    // Tests negative length
    @Test(expected = IndexOutOfBoundsException.class)
    public void testWrite_negativeLength_throwsIndexOutOfBounds() throws IOException {
        cpioOut = createStream(CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry = createEntry("bad", 1, CpioConstants.FORMAT_NEW);
        cpioOut.putNextEntry(entry);
        cpioOut.write(new byte[]{1}, 0, -1);
    }

    // Tests offset beyond array bounds
    @Test(expected = IndexOutOfBoundsException.class)
    public void testWrite_offsetTooLarge_throwsIndexOutOfBounds() throws IOException {
        cpioOut = createStream(CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry = createEntry("bad", 1, CpioConstants.FORMAT_NEW);
        cpioOut.putNextEntry(entry);
        byte[] data = new byte[2];
        cpioOut.write(data, 3, 0);
    }

    // Tests multiple entries
    @Test
    public void testMultipleEntries_success() throws IOException {
        cpioOut = createStream(CpioConstants.FORMAT_NEW);
        CpioArchiveEntry e1 = createEntry("first", 2, CpioConstants.FORMAT_NEW);
        cpioOut.putNextEntry(e1);
        cpioOut.write(new byte[]{1,2}, 0, 2);
        cpioOut.closeArchiveEntry();

        CpioArchiveEntry e2 = createEntry("second", 3, CpioConstants.FORMAT_NEW);
        cpioOut.putNextEntry(e2);
        cpioOut.write(new byte[]{3,4,5}, 0, 3);
        cpioOut.closeArchiveEntry();
        cpioOut.finish();
        assertTrue(baos.size() > 0);
    }

    // Tests putNextEntry implicitly closes previous entry
    @Test
    public void testPutNextEntry_withoutClosingPrevious_closesPrevious() throws IOException {
        cpioOut = createStream(CpioConstants.FORMAT_NEW);
        CpioArchiveEntry e1 = createEntry("a", 2, CpioConstants.FORMAT_NEW);
        cpioOut.putNextEntry(e1);
        cpioOut.write(new byte[]{1,2}, 0, 2);

        CpioArchiveEntry e2 = createEntry("b", 0, CpioConstants.FORMAT_NEW);
        cpioOut.putNextEntry(e2); // should auto-close e1
        cpioOut.closeArchiveEntry();
        cpioOut.finish();
        assertTrue(baos.size() > 0);
    }

    // Additional tests for uncovered parts (AI test suite compile failed)
    // Tests constructor with null output stream
    @Test(expected = NullPointerException.class)
    public void testConstructor_nullOutputStream_throwsNullPointerException() {
        new CpioArchiveOutputStream(null, CpioConstants.FORMAT_NEW);
    }

    // Tests closeArchiveEntry before any entry has been put
    @Test(expected = IOException.class)
    public void testCloseArchiveEntry_noCurrentEntry_throwsIOException() throws IOException {
        cpioOut = createStream(CpioConstants.FORMAT_NEW);
        cpioOut.closeArchiveEntry();
    }

    // Tests finish without any entries written
    @Test
    public void testFinish_noEntries_success() throws IOException {
        cpioOut = createStream(CpioConstants.FORMAT_NEW);
        cpioOut.finish();
        assertTrue(baos.size() > 0);
    }

    // Tests multiple closeArchiveEntry calls in sequence without putting new entries
    @Test(expected = IOException.class)
    public void testCloseArchiveEntry_calledTwice_throwsIOException() throws IOException {
        cpioOut = createStream(CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry = createEntry("test", 0, CpioConstants.FORMAT_NEW);
        cpioOut.putNextEntry(entry);
        cpioOut.closeArchiveEntry();
        cpioOut.closeArchiveEntry(); // second call should fail
    }

    // Tests write with null byte array
    @Test(expected = NullPointerException.class)
    public void testWrite_nullByteArray_throwsNullPointerException() throws IOException {
        cpioOut = createStream(CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry = createEntry("null", 1, CpioConstants.FORMAT_NEW);
        cpioOut.putNextEntry(entry);
        cpioOut.write(null, 0, 1);
    }

    // Tests write with len larger than array length
    @Test(expected = IndexOutOfBoundsException.class)
    public void testWrite_lenTooLarge_throwsIndexOutOfBounds() throws IOException {
        cpioOut = createStream(CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry = createEntry("overflow", 5, CpioConstants.FORMAT_NEW);
        cpioOut.putNextEntry(entry);
        cpioOut.write(new byte[]{1, 2}, 0, 5);
    }

    // Tests finish after close
    @Test(expected = IOException.class)
    public void testFinish_afterClose_throwsIOException() throws IOException {
        cpioOut = createStream(CpioConstants.FORMAT_NEW);
        cpioOut.close();
        cpioOut.finish();
    }
}