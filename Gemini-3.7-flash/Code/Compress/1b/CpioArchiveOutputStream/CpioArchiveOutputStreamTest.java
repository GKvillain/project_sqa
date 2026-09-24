package org.apache.commons.compress.archivers.cpio;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CpioArchiveOutputStreamTest implements CpioConstants {

    private ByteArrayOutputStream baos;
    private CpioArchiveOutputStream out;

    @Before
    public void setUp() {
        baos = new ByteArrayOutputStream();
    }

    @After
    public void tearDown() throws IOException {
        if (out != null) {
            try {
                out.close();
            } catch (IOException ignored) {
            }
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_invalidFormat_throwsIllegalArgumentException() {
        new CpioArchiveOutputStream(baos, (short) 999);
    }

    @Test
    public void testConstructor_withBlockSizeAndEncoding() throws IOException {
        out = new CpioArchiveOutputStream(baos, FORMAT_NEW, 512, "UTF-8");
        CpioArchiveEntry entry = new CpioArchiveEntry(FORMAT_NEW, "test.txt");
        entry.setSize(0);
        out.putArchiveEntry(entry);
        out.closeArchiveEntry();
        out.finish();
        assertTrue(baos.size() > 0);
    }

    @Test
    public void testConstructor_withEncodingOnly() throws IOException {
        out = new CpioArchiveOutputStream(baos, "UTF-8");
        CpioArchiveEntry entry = new CpioArchiveEntry(FORMAT_NEW, "test.txt");
        entry.setSize(0);
        out.putArchiveEntry(entry);
        out.closeArchiveEntry();
        out.finish();
        assertTrue(baos.size() > 0);
    }

    @Test
    public void testPutArchiveEntry_defaultFormatNew_writesExpectedMagic() throws IOException {
        out = new CpioArchiveOutputStream(baos);
        CpioArchiveEntry entry = new CpioArchiveEntry(FORMAT_NEW, "test.txt");
        entry.setSize(0);
        out.putArchiveEntry(entry);
        out.closeArchiveEntry();
        out.finish();

        byte[] result = baos.toByteArray();
        assertTrue(result.length > 0);
        String header = new String(result, 0, 6, StandardCharsets.US_ASCII);
        assertEquals(MAGIC_NEW, header);
    }

    @Test
    public void testPutArchiveEntry_oldAsciiFormat_writesExpectedMagic() throws IOException {
        out = new CpioArchiveOutputStream(baos, FORMAT_OLD_ASCII);
        CpioArchiveEntry entry = new CpioArchiveEntry(FORMAT_OLD_ASCII, "file_old_ascii.txt");
        byte[] content = "hello old ascii".getBytes(StandardCharsets.US_ASCII);
        entry.setSize(content.length);
        out.putArchiveEntry(entry);
        out.write(content);
        out.closeArchiveEntry();
        out.finish();

        byte[] result = baos.toByteArray();
        String header = new String(result, 0, 6, StandardCharsets.US_ASCII);
        assertEquals(MAGIC_OLD_ASCII, header);
    }

    @Test
    public void testPutArchiveEntry_oldBinaryFormat_writesSuccessfully() throws IOException {
        out = new CpioArchiveOutputStream(baos, FORMAT_OLD_BINARY);
        CpioArchiveEntry entry = new CpioArchiveEntry(FORMAT_OLD_BINARY, "binary.bin");
        byte[] content = new byte[] { 1, 2, 3, 4, 5 };
        entry.setSize(content.length);
        out.putArchiveEntry(entry);
        out.write(content);
        out.closeArchiveEntry();
        out.finish();

        byte[] result = baos.toByteArray();
        assertTrue(result.length > 0);
    }

    @Test
    public void testPutArchiveEntry_newCrcFormatWithCorrectChecksum_succeeds() throws IOException {
        out = new CpioArchiveOutputStream(baos, FORMAT_NEW_CRC);
        CpioArchiveEntry entry = new CpioArchiveEntry(FORMAT_NEW_CRC, "crc_test.txt");
        byte[] content = "checksum test".getBytes(StandardCharsets.US_ASCII);
        entry.setSize(content.length);

        long calculatedCrc = 0;
        for (byte b : content) {
            calculatedCrc += b & 0xFF;
        }
        entry.setChksum(calculatedCrc);

        out.putArchiveEntry(entry);
        out.write(content);
        out.closeArchiveEntry();
        out.finish();

        byte[] result = baos.toByteArray();
        String header = new String(result, 0, 6, StandardCharsets.US_ASCII);
        assertEquals(MAGIC_NEW_CRC, header);
    }

    @Test(expected = IOException.class)
    public void testCloseArchiveEntry_crcMismatch_throwsIOException() throws IOException {
        out = new CpioArchiveOutputStream(baos, FORMAT_NEW_CRC);
        CpioArchiveEntry entry = new CpioArchiveEntry(FORMAT_NEW_CRC, "crc_fail.txt");
        byte[] content = "content".getBytes(StandardCharsets.US_ASCII);
        entry.setSize(content.length);
        entry.setChksum(12345L);

        out.putArchiveEntry(entry);
        out.write(content);
        out.closeArchiveEntry();
    }

    @Test(expected = IOException.class)
    public void testPutArchiveEntry_duplicateEntryName_throwsIOException() throws IOException {
        out = new CpioArchiveOutputStream(baos);
        CpioArchiveEntry entry1 = new CpioArchiveEntry(FORMAT_NEW, "same_name.txt");
        entry1.setSize(0);
        out.putArchiveEntry(entry1);
        out.closeArchiveEntry();

        CpioArchiveEntry entry2 = new CpioArchiveEntry(FORMAT_NEW, "same_name.txt");
        entry2.setSize(0);
        out.putArchiveEntry(entry2);
    }

    @Test(expected = IOException.class)
    public void testCloseArchiveEntry_sizeMismatchLessThanDeclared_throwsIOException() throws IOException {
        out = new CpioArchiveOutputStream(baos);
        CpioArchiveEntry entry = new CpioArchiveEntry(FORMAT_NEW, "mismatch.txt");
        entry.setSize(10);
        out.putArchiveEntry(entry);
        out.write("123".getBytes(StandardCharsets.US_ASCII));
        out.closeArchiveEntry();
    }

    @Test(expected = IOException.class)
    public void testWrite_pastEndOfEntry_throwsIOException() throws IOException {
        out = new CpioArchiveOutputStream(baos);
        CpioArchiveEntry entry = new CpioArchiveEntry(FORMAT_NEW, "overflow.txt");
        entry.setSize(3);
        out.putArchiveEntry(entry);
        out.write("12345".getBytes(StandardCharsets.US_ASCII));
    }

    @Test(expected = IOException.class)
    public void testWrite_noCurrentEntry_throwsIOException() throws IOException {
        out = new CpioArchiveOutputStream(baos);
        out.write(new byte[] { 1, 2, 3 }, 0, 3);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testWrite_negativeOffset_throwsIndexOutOfBoundsException() throws IOException {
        out = new CpioArchiveOutputStream(baos);
        CpioArchiveEntry entry = new CpioArchiveEntry(FORMAT_NEW, "bounds.txt");
        entry.setSize(5);
        out.putArchiveEntry(entry);
        out.write(new byte[5], -1, 3);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testWrite_negativeLength_throwsIndexOutOfBoundsException() throws IOException {
        out = new CpioArchiveOutputStream(baos);
        CpioArchiveEntry entry = new CpioArchiveEntry(FORMAT_NEW, "bounds.txt");
        entry.setSize(5);
        out.putArchiveEntry(entry);
        out.write(new byte[5], 0, -1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testWrite_lengthExceedsBuffer_throwsIndexOutOfBoundsException() throws IOException {
        out = new CpioArchiveOutputStream(baos);
        CpioArchiveEntry entry = new CpioArchiveEntry(FORMAT_NEW, "bounds.txt");
        entry.setSize(5);
        out.putArchiveEntry(entry);
        out.write(new byte[5], 2, 4);
    }

    @Test
    public void testWrite_zeroLength_doesNothing() throws IOException {
        out = new CpioArchiveOutputStream(baos);
        CpioArchiveEntry entry = new CpioArchiveEntry(FORMAT_NEW, "zero.txt");
        entry.setSize(0);
        out.putArchiveEntry(entry);
        out.write(new byte[5], 0, 0);
        out.closeArchiveEntry();
    }

    @Test(expected = IOException.class)
    public void testPutArchiveEntry_streamClosed_throwsIOException() throws IOException {
        out = new CpioArchiveOutputStream(baos);
        out.close();
        CpioArchiveEntry entry = new CpioArchiveEntry(FORMAT_NEW, "closed.txt");
        entry.setSize(0);
        out.putArchiveEntry(entry);
    }

    @Test
    public void testFinish_calledTwice_succeedsSilently() throws IOException {
        out = new CpioArchiveOutputStream(baos);
        CpioArchiveEntry entry = new CpioArchiveEntry(FORMAT_NEW, "file.txt");
        entry.setSize(0);
        out.putArchiveEntry(entry);
        out.finish();
        int lengthAfterFirstFinish = baos.size();
        out.finish();
        assertEquals(lengthAfterFirstFinish, baos.size());
    }

    @Test
    public void testWrite_singleByteViaWriteArray() throws IOException {
        out = new CpioArchiveOutputStream(baos);
        CpioArchiveEntry entry = new CpioArchiveEntry(FORMAT_NEW, "single.txt");
        entry.setSize(1);
        out.putArchiveEntry(entry);
        out.write(new byte[] { 65 }, 0, 1);
        out.closeArchiveEntry();
        out.finish();
        assertTrue(baos.size() > 0);
    }

    @Test
    public void testPutArchiveEntry_previousEntryOpen_automaticallyClosesPrevious() throws IOException {
        out = new CpioArchiveOutputStream(baos);
        CpioArchiveEntry entry1 = new CpioArchiveEntry(FORMAT_NEW, "entry1.txt");
        entry1.setSize(2);
        out.putArchiveEntry(entry1);
        out.write("ab".getBytes(StandardCharsets.US_ASCII));

        CpioArchiveEntry entry2 = new CpioArchiveEntry(FORMAT_NEW, "entry2.txt");
        entry2.setSize(2);
        out.putArchiveEntry(entry2);
        out.write("cd".getBytes(StandardCharsets.US_ASCII));
        out.closeArchiveEntry();
        out.finish();

        assertTrue(baos.size() > 0);
    }

    @Test(expected = ClassCastException.class)
    public void testPutArchiveEntry_nonCpioArchiveEntry_throwsClassCastException() throws IOException {
        out = new CpioArchiveOutputStream(baos);
        ArchiveEntry nonCpioEntry = new ArchiveEntry() {
            @Override
            public String getName() {
                return "dummy";
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
            public java.util.Date getLastModifiedDate() {
                return new java.util.Date();
            }
        };
        out.putArchiveEntry(nonCpioEntry);
    }

    @Test
    public void testCreateArchiveEntry_fromFile() throws IOException {
        out = new CpioArchiveOutputStream(baos);
        File tempFile = File.createTempFile("cpio_test", ".tmp");
        try {
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write("hello world".getBytes(StandardCharsets.US_ASCII));
            }
            CpioArchiveEntry entry = (CpioArchiveEntry) out.createArchiveEntry(tempFile, "entry_from_file.txt");
            assertNotNull(entry);
            assertEquals("entry_from_file.txt", entry.getName());
            assertEquals(tempFile.length(), entry.getSize());
        } finally {
            tempFile.delete();
        }
    }

    @Test
    public void testCanWriteEntryData() {
        out = new CpioArchiveOutputStream(baos);
        CpioArchiveEntry regularEntry = new CpioArchiveEntry(FORMAT_NEW, "regular.txt");
        regularEntry.setType(CpioConstants.C_ISREG);
        assertTrue(out.canWriteEntryData(regularEntry));

        CpioArchiveEntry dirEntry = new CpioArchiveEntry(FORMAT_NEW, "dir/");
        dirEntry.setType(CpioConstants.C_ISDIR);
        assertFalse(out.canWriteEntryData(dirEntry));
    }
}