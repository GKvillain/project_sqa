package org.apache.commons.compress.archivers.cpio;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.CRC32;

import org.junit.Test;

public class CpioArchiveInputStreamTest {

    // Helper method to create a valid CPIO entry data for testing
    private byte[] createNewEntryData(String name, String fileContent, boolean withCRC) throws IOException {
        int format = withCRC ? CpioConstants.FORMAT_NEW_CRC : CpioConstants.FORMAT_NEW;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream cpioOut = new CpioArchiveOutputStream(baos, format);
        CpioArchiveEntry entry = new CpioArchiveEntry(format);
        entry.setName(name);
        entry.setSize(fileContent.length());
        entry.setMode(0100644);
        entry.setUID(0);
        entry.setGID(0);
        entry.setNumberOfLinks(1);
        entry.setTime(System.currentTimeMillis() / 1000);
        entry.setDeviceMaj(0);
        entry.setDeviceMin(0);
        entry.setRemoteDeviceMaj(0);
        entry.setRemoteDeviceMin(0);
        if (withCRC) {
            CRC32 crc32 = new CRC32();
            crc32.update(fileContent.getBytes());
            entry.setChksum(crc32.getValue());
        }
        cpioOut.putArchiveEntry(entry);
        if (fileContent.length() > 0) {
            cpioOut.write(fileContent.getBytes());
        }
        cpioOut.close();
        return baos.toByteArray();
    }

    // Helper method to create an empty CPIO stream (only trailer)
    private byte[] createEmptyStream() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream cpioOut = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        cpioOut.close();
        return baos.toByteArray();
    }

    // Helper method to create a stream containing a single entry with mode 0 and non-trailer name
    private byte[] createEntryWithModeZero() throws IOException {
        int format = CpioConstants.FORMAT_NEW;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream cpioOut = new CpioArchiveOutputStream(baos, format);
        CpioArchiveEntry entry = new CpioArchiveEntry(format);
        entry.setName("not/trailer");
        entry.setSize(0);
        entry.setMode(0);
        entry.setUID(0);
        entry.setGID(0);
        entry.setNumberOfLinks(1);
        entry.setTime(0);
        entry.setDeviceMaj(0);
        entry.setDeviceMin(0);
        entry.setRemoteDeviceMaj(0);
        entry.setRemoteDeviceMin(0);
        cpioOut.putArchiveEntry(entry);
        cpioOut.close();
        return baos.toByteArray();
    }

    // Tests basic read operation with a valid new format entry
    @Test
    public void testRead_validNewEntry_returnsContent() throws IOException {
        String content = "test content";
        byte[] entryData = createNewEntryData("test.txt", content, false);
        ByteArrayInputStream bais = new ByteArrayInputStream(entryData);
        CpioArchiveInputStream cpioIn = new CpioArchiveInputStream(bais);

        CpioArchiveEntry entry = cpioIn.getNextCPIOEntry();
        assertNotNull(entry);
        assertEquals("test.txt", entry.getName());

        byte[] buffer = new byte[1024];
        int bytesRead = cpioIn.read(buffer, 0, buffer.length);
        assertEquals(content.length(), bytesRead);
        assertEquals(content, new String(buffer, 0, bytesRead));

        cpioIn.close();
    }

    // Tests reading a file with CRC checking
    @Test
    public void testRead_crcEntry_validContent() throws IOException {
        String content = "crc test data";
        byte[] entryData = createNewEntryData("crcfile.bin", content, true);
        ByteArrayInputStream bais = new ByteArrayInputStream(entryData);
        CpioArchiveInputStream cpioIn = new CpioArchiveInputStream(bais);

        CpioArchiveEntry entry = cpioIn.getNextCPIOEntry();
        assertNotNull(entry);
        assertEquals("crcfile.bin", entry.getName());

        byte[] buffer = new byte[1024];
        int bytesRead = cpioIn.read(buffer, 0, buffer.length);
        assertEquals(content.length(), bytesRead);
        assertEquals(content, new String(buffer, 0, bytesRead));

        // Reading again should return -1 (EOF) and trigger CRC validation
        int eofCheck = cpioIn.read(buffer, 0, 1);
        assertEquals(-1, eofCheck);

        cpioIn.close();
    }

    // Tests reading an empty file entry
    @Test
    public void testRead_emptyFile_returnsEof() throws IOException {
        byte[] entryData = createNewEntryData("empty.txt", "", false);
        ByteArrayInputStream bais = new ByteArrayInputStream(entryData);
        CpioArchiveInputStream cpioIn = new CpioArchiveInputStream(bais);

        CpioArchiveEntry e = cpioIn.getNextCPIOEntry();
        assertNotNull(e);
        assertEquals("empty.txt", e.getName());
        assertEquals(0, e.getSize());

        byte[] buffer = new byte[16];
        int bytesRead = cpioIn.read(buffer, 0, buffer.length);
        assertEquals(-1, bytesRead);

        cpioIn.close();
    }

    // Tests reading a trailer entry (should return null)
    @Test
    public void testRead_trailerEntry_returnsNull() throws IOException {
        byte[] streamData = createEmptyStream();
        ByteArrayInputStream bais = new ByteArrayInputStream(streamData);
        CpioArchiveInputStream cpioIn = new CpioArchiveInputStream(bais);

        CpioArchiveEntry entry = cpioIn.getNextCPIOEntry();
        assertNull(entry);

        cpioIn.close();
    }

    // Tests available() method before EOF
    @Test
    public void testAvailable_beforeEof_returnsOne() throws IOException {
        String content = "data";
        byte[] entryData = createNewEntryData("test.bin", content, false);
        ByteArrayInputStream bais = new ByteArrayInputStream(entryData);
        CpioArchiveInputStream cpioIn = new CpioArchiveInputStream(bais);

        cpioIn.getNextCPIOEntry();
        assertEquals(1, cpioIn.available());

        cpioIn.close();
    }

    // Tests available() method after EOF for the entry
    @Test
    public void testAvailable_afterEntryEof_returnsZero() throws IOException {
        String content = "data";
        byte[] entryData = createNewEntryData("test.bin", content, false);
        ByteArrayInputStream bais = new ByteArrayInputStream(entryData);
        CpioArchiveInputStream cpioIn = new CpioArchiveInputStream(bais);

        cpioIn.getNextCPIOEntry();
        byte[] buffer = new byte[1024];
        cpioIn.read(buffer, 0, buffer.length); // read all data
        int eofCheck = cpioIn.read(buffer, 0, 1);
        assertEquals(-1, eofCheck);
        assertEquals(0, cpioIn.available());

        cpioIn.close();
    }

    // Tests reading with negative offset throws exception
    @Test(expected = IndexOutOfBoundsException.class)
    public void testRead_negativeOffset_throwsException() throws IOException {
        String content = "test";
        byte[] entryData = createNewEntryData("file.txt", content, false);
        ByteArrayInputStream bais = new ByteArrayInputStream(entryData);
        CpioArchiveInputStream cpioIn = new CpioArchiveInputStream(bais);

        cpioIn.getNextCPIOEntry();
        byte[] buffer = new byte[10];
        cpioIn.read(buffer, -1, 5);

        cpioIn.close();
    }

    // Tests reading with negative length throws exception
    @Test(expected = IndexOutOfBoundsException.class)
    public void testRead_negativeLength_throwsException() throws IOException {
        String content = "test";
        byte[] entryData = createNewEntryData("file.txt", content, false);
        ByteArrayInputStream bais = new ByteArrayInputStream(entryData);
        CpioArchiveInputStream cpioIn = new CpioArchiveInputStream(bais);

        cpioIn.getNextCPIOEntry();
        byte[] buffer = new byte[10];
        cpioIn.read(buffer, 0, -5);

        cpioIn.close();
    }

    // Tests skip with zero value
    @Test
    public void testSkip_zero_returnsZero() throws IOException {
        String content = "data";
        byte[] entryData = createNewEntryData("file.txt", content, false);
        ByteArrayInputStream bais = new ByteArrayInputStream(entryData);
        CpioArchiveInputStream cpioIn = new CpioArchiveInputStream(bais);

        cpioIn.getNextCPIOEntry();
        long skipped = cpioIn.skip(0);
        assertEquals(0, skipped);

        cpioIn.close();
    }

    // Tests skip with valid value
    @Test
    public void testSkip_positiveValue_skipsBytes() throws IOException {
        String content = "1234567890";
        byte[] entryData = createNewEntryData("file.txt", content, false);
        ByteArrayInputStream bais = new ByteArrayInputStream(entryData);
        CpioArchiveInputStream cpioIn = new CpioArchiveInputStream(bais);

        cpioIn.getNextCPIOEntry();
        long skipped = cpioIn.skip(5);
        assertEquals(5, skipped);

        byte[] buffer = new byte[5];
        int bytesRead = cpioIn.read(buffer, 0, buffer.length);
        assertEquals(5, bytesRead);
        assertEquals("67890", new String(buffer));

        cpioIn.close();
    }

    // Tests skip with negative value throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testSkip_negativeValue_throwsException() throws IOException {
        String content = "data";
        byte[] entryData = createNewEntryData("file.txt", content, false);
        ByteArrayInputStream bais = new ByteArrayInputStream(entryData);
        CpioArchiveInputStream cpioIn = new CpioArchiveInputStream(bais);

        cpioIn.getNextCPIOEntry();
        cpioIn.skip(-1);

        cpioIn.close();
    }

    // Tests reading after close throws IOException
    @Test(expected = IOException.class)
    public void testRead_afterClose_throwsIOException() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
        CpioArchiveInputStream cpioIn = new CpioArchiveInputStream(bais);
        cpioIn.close();

        byte[] buffer = new byte[1];
        cpioIn.read(buffer, 0, 1);
    }

    // Tests getNextEntry after close throws IOException
    @Test(expected = IOException.class)
    public void testGetNextEntry_afterClose_throwsIOException() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
        CpioArchiveInputStream cpioIn = new CpioArchiveInputStream(bais);
        cpioIn.close();

        cpioIn.getNextEntry();
    }

    // Tests available after close throws IOException
    @Test(expected = IOException.class)
    public void testAvailable_afterClose_throwsIOException() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
        CpioArchiveInputStream cpioIn = new CpioArchiveInputStream(bais);
        cpioIn.close();

        cpioIn.available();
    }

    // Tests handling of unknown magic bytes
    @Test(expected = IOException.class)
    public void testGetNextEntry_unknownMagic_throwsException() throws IOException {
        byte[] invalidData = new byte[]{0x00, 0x01, 0x02, 0x03, 0x04, 0x05};
        ByteArrayInputStream bais = new ByteArrayInputStream(invalidData);
        CpioArchiveInputStream cpioIn = new CpioArchiveInputStream(bais);

        cpioIn.getNextCPIOEntry();
        cpioIn.close();
    }

    // Tests constructor with custom block size
    @Test
    public void testConstructor_customBlockSize_success() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
        CpioArchiveInputStream cpioIn = new CpioArchiveInputStream(bais, 512);
        assertNotNull(cpioIn);
        cpioIn.close();
    }

    // Tests reading entry with mode 0 (should still work for trailer)
    @Test
    public void testRead_entryWithModeZero_notTrailer_throwsException() throws IOException {
        byte[] entryData = createEntryWithModeZero();
        ByteArrayInputStream bais = new ByteArrayInputStream(entryData);
        CpioArchiveInputStream cpioIn = new CpioArchiveInputStream(bais);

        try {
            cpioIn.getNextCPIOEntry();
            fail("Expected IOException");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("Mode 0 only allowed in the trailer"));
        }

        cpioIn.close();
    }
}