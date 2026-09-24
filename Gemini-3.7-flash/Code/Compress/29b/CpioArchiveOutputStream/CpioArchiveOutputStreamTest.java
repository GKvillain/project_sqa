package org.apache.commons.compress.archivers.cpio;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.junit.Test;
import static org.junit.Assert.*;

public class CpioArchiveOutputStreamTest {

    // Tests constructors with valid formats and invalid format
    @Test
    public void testConstructor_validAndInvalidFormats() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out1 = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        assertNotNull(out1);
        out1.close();

        CpioArchiveOutputStream out2 = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW_CRC, 512);
        assertNotNull(out2);
        out2.close();

        CpioArchiveOutputStream out3 = new CpioArchiveOutputStream(baos, "UTF-8");
        assertNotNull(out3);
        out3.close();

        CpioArchiveOutputStream outDefault = new CpioArchiveOutputStream(baos);
        assertNotNull(outDefault);
        outDefault.close();
    }

    // Tests constructor with unknown format throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_invalidFormat_throwsException() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new CpioArchiveOutputStream(baos, (short) 999);
    }

    // Tests writing and closing entry for FORMAT_NEW
    @Test
    public void testPutArchiveEntry_formatNew_writesSuccessfully() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "test1.txt");
        byte[] content = "Hello CPIO".getBytes();
        entry.setSize(content.length);
        entry.setMode(CpioConstants.C_ISREG);
        
        out.putArchiveEntry(entry);
        out.write(content);
        out.closeArchiveEntry();
        out.finish();
        out.close();

        assertTrue(baos.size() > 0);
    }

    // Tests writing and closing entry for FORMAT_NEW_CRC
    @Test
    public void testPutArchiveEntry_formatNewCrc_writesSuccessfully() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW_CRC);
        
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW_CRC, "crc.txt");
        byte[] content = "CRC Check".getBytes();
        entry.setSize(content.length);
        long crc = 0;
        for (byte b : content) {
            crc += b & 0xFF;
        }
        entry.setChksum(crc);

        out.putArchiveEntry(entry);
        out.write(content);
        out.closeArchiveEntry();
        out.close();

        assertTrue(baos.size() > 0);
    }

    // Tests CRC mismatch throwing IOException
    @Test(expected = IOException.class)
    public void testCloseArchiveEntry_crcMismatch_throwsException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW_CRC);

        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW_CRC, "crc_bad.txt");
        byte[] content = "CRC Bad".getBytes();
        entry.setSize(content.length);
        entry.setChksum(12345L); // invalid crc

        out.putArchiveEntry(entry);
        out.write(content);
        out.closeArchiveEntry();
    }

    // Tests writing and closing entry for FORMAT_OLD_ASCII
    @Test
    public void testPutArchiveEntry_formatOldAscii_writesSuccessfully() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_OLD_ASCII);
        
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_OLD_ASCII, "old_ascii.txt");
        byte[] content = "Old ASCII".getBytes();
        entry.setSize(content.length);

        out.putArchiveEntry(entry);
        out.write(content);
        out.closeArchiveEntry();
        out.close();

        assertTrue(baos.size() > 0);
    }

    // Tests writing and closing entry for FORMAT_OLD_BINARY
    @Test
    public void testPutArchiveEntry_formatOldBinary_writesSuccessfully() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_OLD_BINARY);
        
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_OLD_BINARY, "old_bin.txt");
        byte[] content = "Old Binary Content".getBytes();
        entry.setSize(content.length);

        out.putArchiveEntry(entry);
        out.write(content);
        out.closeArchiveEntry();
        out.close();

        assertTrue(baos.size() > 0);
    }

    // Tests explicit non-zero inode and device across formats
    @Test
    public void testPutArchiveEntry_withExplicitInodeAndDevice_writesSuccessfully() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);

        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "inode_test.txt");
        entry.setInode(100);
        entry.setDeviceMin(2);
        entry.setSize(0);

        out.putArchiveEntry(entry);
        out.closeArchiveEntry();
        out.close();

        assertTrue(baos.size() > 0);
    }

    // Tests format mismatch between entry and stream
    @Test(expected = IOException.class)
    public void testPutArchiveEntry_formatMismatch_throwsException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);

        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_OLD_ASCII, "mismatch.txt");
        out.putArchiveEntry(entry);
    }

    // Tests duplicate entry name throwing IOException
    @Test(expected = IOException.class)
    public void testPutArchiveEntry_duplicateEntryName_throwsException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);

        CpioArchiveEntry entry1 = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "dup.txt");
        entry1.setSize(0);
        out.putArchiveEntry(entry1);
        out.closeArchiveEntry();

        CpioArchiveEntry entry2 = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "dup.txt");
        entry2.setSize(0);
        out.putArchiveEntry(entry2);
    }

    // Tests closing non-existent entry
    @Test(expected = IOException.class)
    public void testCloseArchiveEntry_noCurrentEntry_throwsException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        out.closeArchiveEntry();
    }

    // Tests writing invalid size (under-written)
    @Test(expected = IOException.class)
    public void testCloseArchiveEntry_invalidSize_throwsException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);

        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "size.txt");
        entry.setSize(10);
        out.putArchiveEntry(entry);
        out.write(new byte[]{1, 2, 3});
        out.closeArchiveEntry();
    }

    // Tests writing past end of declared size
    @Test(expected = IOException.class)
    public void testWrite_pastEndOfEntry_throwsException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);

        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "overflow.txt");
        entry.setSize(2);
        out.putArchiveEntry(entry);
        out.write(new byte[]{1, 2, 3});
    }

    // Tests writing without putting an entry first
    @Test(expected = IOException.class)
    public void testWrite_withoutEntry_throwsException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        out.write(new byte[]{1, 2, 3});
    }

    // Tests writing with invalid offset/length bounds
    @Test(expected = IndexOutOfBoundsException.class)
    public void testWrite_outOfBounds_throwsException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);

        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "bounds.txt");
        entry.setSize(5);
        out.putArchiveEntry(entry);
        out.write(new byte[5], 2, 5);
    }

    // Tests calling finish when unclosed entries exist
    @Test(expected = IOException.class)
    public void testFinish_withUnclosedEntry_throwsException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);

        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "unclosed.txt");
        entry.setSize(0);
        out.putArchiveEntry(entry);
        out.finish();
    }

    // Tests operations on already closed stream
    @Test(expected = IOException.class)
    public void testEnsureOpen_closedStream_throwsException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        out.close();
        out.putArchiveEntry(new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "test.txt"));
    }

    // Tests createArchiveEntry helper method
    @Test
    public void testCreateArchiveEntry_validFile_returnsEntry() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);

        File tempFile = File.createTempFile("cpio_test", ".tmp");
        tempFile.deleteOnExit();

        org.apache.commons.compress.archivers.ArchiveEntry created = out.createArchiveEntry(tempFile, "entryName");
        assertNotNull(created);
        assertEquals("entryName", created.getName());
        out.close();
    }

    // Tests calling createArchiveEntry after finished throws IOException
    @Test(expected = IOException.class)
    public void testCreateArchiveEntry_afterFinished_throwsException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        out.finish();
        out.createArchiveEntry(new File("dummy"), "dummy");
    }

    // Tests calling finish twice throws IOException
    @Test(expected = IOException.class)
    public void testFinish_twice_throwsException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        out.finish();
        out.finish();
    }

    // Tests constructor with format, block size, and encoding
    @Test
    public void testConstructor_withEncodingAndBlockSize() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW, 512, "UTF-8");
        assertNotNull(out);
        out.close();
    }

    // Tests block size padding upon finish
    @Test
    public void testFinish_padsToBlockSize() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int blockSize = 512;
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW, blockSize);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "block.txt");
        entry.setSize(3);
        out.putArchiveEntry(entry);
        out.write(new byte[]{1, 2, 3});
        out.closeArchiveEntry();
        out.finish();
        assertEquals(0, baos.size() % blockSize);
        out.close();
    }

    // Tests writing single byte through write(int)
    @Test
    public void testWrite_singleByte() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "single_byte.txt");
        entry.setSize(1);
        out.putArchiveEntry(entry);
        out.write(42);
        out.closeArchiveEntry();
        out.finish();
        out.close();
        assertTrue(baos.size() > 0);
    }

    // Tests writing zero bytes
    @Test
    public void testWrite_zeroLength() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "zero_len.txt");
        entry.setSize(0);
        out.putArchiveEntry(entry);
        out.write(new byte[10], 0, 0);
        out.closeArchiveEntry();
        out.close();
    }

    // Tests closing stream without finish() and multiple close() calls
    @Test
    public void testClose_withoutCallingFinish_andIdempotentClose() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "close_test.txt");
        entry.setSize(0);
        out.putArchiveEntry(entry);
        out.closeArchiveEntry();
        out.close();
        out.close();
    }

    // Tests writing Old ASCII entry with detailed attributes
    @Test
    public void testPutArchiveEntry_oldAsciiWithDataAndDevices() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_OLD_ASCII);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_OLD_ASCII, "old_ascii_data.txt");
        entry.setDevice(1);
        entry.setInode(2);
        entry.setMode(0100644);
        entry.setUID(1000);
        entry.setGID(1000);
        entry.setNumberOfLinks(1);
        entry.setRemoteDevice(0);
        entry.setTime(System.currentTimeMillis() / 1000);
        byte[] content = "Data for ASCII".getBytes();
        entry.setSize(content.length);
        out.putArchiveEntry(entry);
        out.write(content);
        out.closeArchiveEntry();
        out.finish();
        out.close();
        assertTrue(baos.size() > 0);
    }

    // Tests writing Old Binary entry with detailed attributes
    @Test
    public void testPutArchiveEntry_oldBinaryWithDataAndDevices() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_OLD_BINARY);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_OLD_BINARY, "old_bin_data.txt");
        entry.setDevice(1);
        entry.setInode(2);
        entry.setMode(0100644);
        entry.setUID(1000);
        entry.setGID(1000);
        entry.setNumberOfLinks(1);
        entry.setRemoteDevice(0);
        entry.setTime(System.currentTimeMillis() / 1000);
        byte[] content = "Data for Binary".getBytes();
        entry.setSize(content.length);
        out.putArchiveEntry(entry);
        out.write(content);
        out.closeArchiveEntry();
        out.finish();
        out.close();
        assertTrue(baos.size() > 0);
    }

    // Tests createArchiveEntry on directory
    @Test
    public void testCreateArchiveEntry_directory() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        File dir = new File(System.getProperty("java.io.tmpdir"));
        ArchiveEntry entry = out.createArchiveEntry(dir, "tempDir");
        assertNotNull(entry);
        assertTrue(entry.isDirectory());
        out.close();
    }

    // Tests putArchiveEntry with non-CpioArchiveEntry throwing ClassCastException
    @Test(expected = ClassCastException.class)
    public void testPutArchiveEntry_nonCpioArchiveEntry_throwsException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        ArchiveEntry dummy = new ArchiveEntry() {
            @Override
            public String getName() { return "dummy"; }
            @Override
            public long getSize() { return 0; }
            @Override
            public boolean isDirectory() { return false; }
            @Override
            public Date getLastModifiedDate() { return new Date(); }
        };
        out.putArchiveEntry(dummy);
    }
}