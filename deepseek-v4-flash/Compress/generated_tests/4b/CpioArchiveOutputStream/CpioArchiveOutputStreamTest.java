package org.apache.commons.compress.archivers.cpio;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;

public class CpioArchiveOutputStreamTest {

    // Tests constructor with valid FORMAT_NEW
    @Test
    public void testConstructor_validFormatNew_createsOutputStream() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos, CpioConstants.FORMAT_NEW);
        assertNotNull(out);
        out.close();
    }

    // Tests constructor with valid FORMAT_NEW_CRC
    @Test
    public void testConstructor_validFormatNewCrc_createsOutputStream() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos, CpioConstants.FORMAT_NEW_CRC);
        assertNotNull(out);
        out.close();
    }

    // Tests constructor with valid FORMAT_OLD_ASCII
    @Test
    public void testConstructor_validFormatOldAscii_createsOutputStream() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos, CpioConstants.FORMAT_OLD_ASCII);
        assertNotNull(out);
        out.close();
    }

    // Tests constructor with invalid format throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_invalidFormat_throwsIllegalArgumentException() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        new CpioArchiveOutputStream(bos, (short) 999);
    }

    // Tests putArchiveEntry with valid entry for new format
    @Test
    public void testPutArchiveEntry_validEntry_newFormat() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos, CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW);
        entry.setName("test");
        entry.setSize(0);
        out.putNextEntry(entry);
        out.closeArchiveEntry();
        out.close();
        assertTrue(bos.size() > 0);
    }

    // Tests putArchiveEntry with duplicate name throws IOException
    @Test(expected = IOException.class)
    public void testPutArchiveEntry_duplicateName_throwsIOException() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos, CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW);
        entry.setName("test");
        entry.setSize(0);
        out.putNextEntry(entry);
        out.closeArchiveEntry();
        CpioArchiveEntry entry2 = new CpioArchiveEntry(CpioConstants.FORMAT_NEW);
        entry2.setName("test");
        entry2.setSize(0);
        out.putNextEntry(entry2);
    }

    // Tests putArchiveEntry with mismatched format throws IOException
    @Test(expected = IOException.class)
    public void testPutArchiveEntry_mismatchedFormat_throwsIOException() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos, CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_OLD_ASCII);
        entry.setName("test");
        entry.setSize(0);
        out.putNextEntry(entry);
    }

    // Tests closeArchiveEntry with correct size does not throw
    @Test
    public void testCloseArchiveEntry_correctSize_success() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos, CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW);
        entry.setName("test");
        entry.setSize(4);
        out.putNextEntry(entry);
        out.write(new byte[] {1, 2, 3, 4});
        out.closeArchiveEntry();
        out.close();
    }

    // Tests closeArchiveEntry with incorrect size throws IOException
    @Test(expected = IOException.class)
    public void testCloseArchiveEntry_incorrectSize_throwsIOException() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos, CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW);
        entry.setName("test");
        entry.setSize(4);
        out.putNextEntry(entry);
        out.write(new byte[] {1, 2, 3});
        out.closeArchiveEntry();
    }

    // Tests write with no current entry throws IOException
    @Test(expected = IOException.class)
    public void testWrite_noCurrentEntry_throwsIOException() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos, CpioConstants.FORMAT_NEW);
        out.write(new byte[] {1}, 0, 1);
    }

    // Tests write past end of entry throws IOException
    @Test(expected = IOException.class)
    public void testWrite_exceedsEntrySize_throwsIOException() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos, CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW);
        entry.setName("test");
        entry.setSize(1);
        out.putNextEntry(entry);
        out.write(new byte[] {1, 2}, 0, 2);
    }

    // Tests finish with unclosed entry throws IOException
    @Test(expected = IOException.class)
    public void testFinish_unclosedEntry_throwsIOException() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos, CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW);
        entry.setName("test");
        entry.setSize(0);
        out.putNextEntry(entry);
        out.finish();
    }

    // Tests finish with no entries succeeds (trailer written)
    @Test
    public void testFinish_noEntries_success() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos, CpioConstants.FORMAT_NEW);
        out.finish();
        out.close();
        assertTrue(bos.size() > 0);
    }

    // Tests close calls finish and closes stream
    @Test
    public void testClose_closesStream() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos, CpioConstants.FORMAT_NEW);
        out.close();
        assertTrue(bos.size() > 0);
    }

    // Tests writing to closed stream throws IOException
    @Test(expected = IOException.class)
    public void testWrite_closedStream_throwsIOException() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos, CpioConstants.FORMAT_NEW);
        out.close();
        out.write(new byte[] {1}, 0, 1);
    }

    // Tests write with invalid offset/len throws IndexOutOfBoundsException
    @Test(expected = IndexOutOfBoundsException.class)
    public void testWrite_invalidOffset_throwsIndexOutOfBoundsException() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos, CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW);
        entry.setName("test");
        entry.setSize(5);
        out.putNextEntry(entry);
        out.write(new byte[] {1, 2, 3}, -1, 2);
    }

    // Tests write with zero length does nothing
    @Test
    public void testWrite_zeroLength_doesNothing() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos, CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW);
        entry.setName("test");
        entry.setSize(5);
        out.putNextEntry(entry);
        out.write(new byte[] {1, 2, 3}, 0, 0);
        out.closeArchiveEntry();
        out.close();
    }

    // Tests CRC check in closeArchiveEntry for FORMAT_NEW_CRC (defect trigger)
    @Test(expected = IOException.class)
    public void testCloseArchiveEntry_crcMismatch_throwsIOException() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos, CpioConstants.FORMAT_NEW_CRC);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW_CRC);
        entry.setName("test");
        entry.setSize(4);
        entry.setChksum(12345L); // Set wrong chksum to trigger CRC error
        out.putNextEntry(entry);
        out.write(new byte[] {1, 2, 3, 4});
        out.closeArchiveEntry(); // Should throw because stored chksum does not match computed CRC
    }

    // ========== New test cases for uncovered parts ==========

    @Test(expected = IOException.class)
    public void testPutNextEntryAfterClose() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos, CpioConstants.FORMAT_NEW);
        out.close();
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW);
        entry.setName("test");
        entry.setSize(0);
        out.putNextEntry(entry);
    }

    @Test(expected = IOException.class)
    public void testCloseArchiveEntryWithoutEntry() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos, CpioConstants.FORMAT_NEW);
        out.closeArchiveEntry();
    }

    @Test(expected = IOException.class)
    public void testPutNextEntryAfterFinish() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos, CpioConstants.FORMAT_NEW);
        out.finish();
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW);
        entry.setName("test");
        entry.setSize(0);
        out.putNextEntry(entry);
    }

    @Test(expected = IOException.class)
    public void testWriteAfterFinish() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos, CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW);
        entry.setName("test");
        entry.setSize(1);
        out.putNextEntry(entry);
        out.closeArchiveEntry();
        out.finish();
        out.write(new byte[] {1}, 0, 1);
    }

    @Test
    public void testMultipleEntries() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos, CpioConstants.FORMAT_NEW);
        for (int i = 0; i < 3; i++) {
            CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW);
            entry.setName("test" + i);
            entry.setSize(1);
            out.putNextEntry(entry);
            out.write(new byte[] {(byte) i});
            out.closeArchiveEntry();
        }
        out.close();
        assertTrue(bos.size() > 0);
    }
}