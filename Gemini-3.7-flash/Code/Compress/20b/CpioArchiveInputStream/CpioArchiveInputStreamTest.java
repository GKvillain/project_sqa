package org.apache.commons.compress.archivers.cpio;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;

import static org.junit.Assert.*;

public class CpioArchiveInputStreamTest {

    // Helper method to create a valid CPIO byte stream
    private byte[] createArchive(CpioArchiveEntry[] entries, byte[][] contents, short format) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, format);
        for (int i = 0; i < entries.length; i++) {
            out.putArchiveEntry(entries[i]);
            if (contents != null && contents[i] != null) {
                out.write(contents[i]);
            }
            out.closeArchiveEntry();
        }
        out.close();
        return baos.toByteArray();
    }

    // Tests matches() method with valid magic signatures
    @Test
    public void testMatches_validSignatures_returnsTrue() {
        byte[] magicNew = "070701".getBytes();
        assertTrue(CpioArchiveInputStream.matches(magicNew, 6));

        byte[] magicNewCrc = "070702".getBytes();
        assertTrue(CpioArchiveInputStream.matches(magicNewCrc, 6));

        byte[] magicOldAscii = "070707".getBytes();
        assertTrue(CpioArchiveInputStream.matches(magicOldAscii, 6));

        byte[] magicOldBinaryLe = new byte[]{(byte) 0x71, (byte) 0xc7, 0, 0, 0, 0};
        assertTrue(CpioArchiveInputStream.matches(magicOldBinaryLe, 6));

        byte[] magicOldBinaryBe = new byte[]{(byte) 0xc7, (byte) 0x71, 0, 0, 0, 0};
        assertTrue(CpioArchiveInputStream.matches(magicOldBinaryBe, 6));
    }

    // Tests matches() method with invalid signatures or short length
    @Test
    public void testMatches_invalidSignaturesAndLengths_returnsFalse() {
        assertFalse(CpioArchiveInputStream.matches(new byte[]{0x71, (byte) 0xc7}, 2));
        assertFalse(CpioArchiveInputStream.matches("070700".getBytes(), 6));
        assertFalse(CpioArchiveInputStream.matches("070708".getBytes(), 6));
        assertFalse(CpioArchiveInputStream.matches("170701".getBytes(), 6));
        assertFalse(CpioArchiveInputStream.matches("080701".getBytes(), 6));
        assertFalse(CpioArchiveInputStream.matches("071701".getBytes(), 6));
        assertFalse(CpioArchiveInputStream.matches("070801".getBytes(), 6));
        assertFalse(CpioArchiveInputStream.matches("070711".getBytes(), 6));
    }

    // Tests reading FORMAT_NEW archive entries
    @Test
    public void testGetNextEntry_formatNew_readsEntriesAndDataCorrectly() throws IOException {
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "test1.txt", 5);
        entry.setMode(0100644);
        byte[] content = "hello".getBytes();
        byte[] archive = createArchive(new CpioArchiveEntry[]{entry}, new byte[][]{content}, CpioConstants.FORMAT_NEW);

        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(archive));
        CpioArchiveEntry readEntry = in.getNextCPIOEntry();
        assertNotNull(readEntry);
        assertEquals("test1.txt", readEntry.getName());
        assertEquals(5, readEntry.getSize());
        assertEquals(CpioConstants.FORMAT_NEW, readEntry.getFormat());

        assertEquals(1, in.available());
        byte[] readBuf = new byte[10];
        int bytesRead = in.read(readBuf, 0, readBuf.length);
        assertEquals(5, bytesRead);
        assertEquals("hello", new String(readBuf, 0, bytesRead));

        assertEquals(0, in.available());
        assertEquals(-1, in.read(readBuf, 0, readBuf.length));
        assertNull(in.getNextEntry());
        in.close();
    }

    // Tests reading FORMAT_NEW_CRC archive entries with CRC calculation
    @Test
    public void testGetNextEntry_formatNewCrc_readsSuccessfully() throws IOException {
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW_CRC, "crc.txt", 4);
        entry.setMode(0100644);
        byte[] content = "data".getBytes();
        byte[] archive = createArchive(new CpioArchiveEntry[]{entry}, new byte[][]{content}, CpioConstants.FORMAT_NEW_CRC);

        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(archive));
        CpioArchiveEntry readEntry = in.getNextEntry();
        assertNotNull(readEntry);
        assertEquals("crc.txt", readEntry.getName());

        byte[] readBuf = new byte[4];
        assertEquals(4, in.read(readBuf, 0, 4));
        assertEquals("data", new String(readBuf));
        assertEquals(-1, in.read(readBuf, 0, 4));
        assertNull(in.getNextEntry());
        in.close();
    }

    // Tests reading FORMAT_OLD_ASCII archive entries
    @Test
    public void testGetNextEntry_formatOldAscii_readsEntriesCorrectly() throws IOException {
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_OLD_ASCII, "old_ascii.txt", 6);
        entry.setMode(0100644);
        byte[] content = "world!".getBytes();
        byte[] archive = createArchive(new CpioArchiveEntry[]{entry}, new byte[][]{content}, CpioConstants.FORMAT_OLD_ASCII);

        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(archive));
        CpioArchiveEntry readEntry = in.getNextCPIOEntry();
        assertNotNull(readEntry);
        assertEquals("old_ascii.txt", readEntry.getName());
        assertEquals(6, readEntry.getSize());
        assertEquals(CpioConstants.FORMAT_OLD_ASCII, readEntry.getFormat());

        byte[] readBuf = new byte[6];
        assertEquals(6, in.read(readBuf, 0, 6));
        assertEquals("world!", new String(readBuf));
        assertNull(in.getNextEntry());
        in.close();
    }

    // Tests reading FORMAT_OLD_BINARY archive entries
    @Test
    public void testGetNextEntry_formatOldBinary_readsEntriesCorrectly() throws IOException {
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_OLD_BINARY, "bin.txt", 3);
        entry.setMode(0100644);
        byte[] content = "abc".getBytes();
        byte[] archive = createArchive(new CpioArchiveEntry[]{entry}, new byte[][]{content}, CpioConstants.FORMAT_OLD_BINARY);

        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(archive));
        CpioArchiveEntry readEntry = in.getNextCPIOEntry();
        assertNotNull(readEntry);
        assertEquals("bin.txt", readEntry.getName());
        assertEquals(3, readEntry.getSize());
        assertEquals(CpioConstants.FORMAT_OLD_BINARY, readEntry.getFormat());

        byte[] readBuf = new byte[3];
        assertEquals(3, in.read(readBuf, 0, 3));
        assertEquals("abc", new String(readBuf));
        assertNull(in.getNextCPIOEntry());
        in.close();
    }

    // Tests skipping entry contents automatically when getting next entry
    @Test
    public void testCloseEntry_implicitSkipOnNextEntry_advancesStream() throws IOException {
        CpioArchiveEntry entry1 = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "file1.txt", 5);
        entry1.setMode(0100644);
        CpioArchiveEntry entry2 = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "file2.txt", 4);
        entry2.setMode(0100644);

        byte[] archive = createArchive(
                new CpioArchiveEntry[]{entry1, entry2},
                new byte[][]{"first".getBytes(), "next".getBytes()},
                CpioConstants.FORMAT_NEW
        );

        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(archive));
        CpioArchiveEntry e1 = in.getNextCPIOEntry();
        assertEquals("file1.txt", e1.getName());

        // Do not read e1 content, immediately call getNextEntry()
        CpioArchiveEntry e2 = in.getNextCPIOEntry();
        assertEquals("file2.txt", e2.getName());

        byte[] buf = new byte[4];
        assertEquals(4, in.read(buf, 0, 4));
        assertEquals("next", new String(buf));
        assertNull(in.getNextCPIOEntry());
        in.close();
    }

    // Tests skip() method within an entry
    @Test
    public void testSkip_positiveBytes_skipsDataCorrectly() throws IOException {
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "skip.txt", 10);
        entry.setMode(0100644);
        byte[] content = "0123456789".getBytes();
        byte[] archive = createArchive(new CpioArchiveEntry[]{entry}, new byte[][]{content}, CpioConstants.FORMAT_NEW);

        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(archive));
        assertNotNull(in.getNextEntry());

        long skipped = in.skip(5);
        assertEquals(5, skipped);

        byte[] buf = new byte[5];
        int read = in.read(buf, 0, 5);
        assertEquals(5, read);
        assertEquals("56789", new String(buf));
        assertEquals(-1, in.read(buf, 0, 5));
        in.close();
    }

    // Tests skip() method with negative argument throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testSkip_negativeValue_throwsException() throws IOException {
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        in.skip(-1);
    }

    // Tests read() with len == 0 returns 0
    @Test
    public void testRead_zeroLength_returnsZero() throws IOException {
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        assertEquals(0, in.read(new byte[10], 0, 0));
    }

    // Tests read() with invalid bounds throws IndexOutOfBoundsException
    @Test(expected = IndexOutOfBoundsException.class)
    public void testRead_invalidOffset_throwsException() throws IOException {
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(new byte[10]));
        in.read(new byte[5], -1, 2);
    }

    // Tests read() without entry returns -1
    @Test
    public void testRead_noEntryLoaded_returnsMinusOne() throws IOException {
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(new byte[10]));
        assertEquals(-1, in.read(new byte[5], 0, 5));
    }

    // Tests unknown magic header throws IOException
    @Test(expected = IOException.class)
    public void testGetNextEntry_unknownMagic_throwsIOException() throws IOException {
        byte[] invalidHeader = "INVALID_MAGIC_HEADER".getBytes();
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(invalidHeader));
        in.getNextCPIOEntry();
    }

    // Tests unexpected EOF in readFully throws EOFException
    @Test(expected = EOFException.class)
    public void testReadFully_unexpectedEof_throwsEOFException() throws IOException {
        byte[] truncated = new byte[]{0x30, 0x37};
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(truncated));
        in.getNextCPIOEntry();
    }

    // Tests operations on closed stream throw IOException
    @Test(expected = IOException.class)
    public void testEnsureOpen_afterClose_throwsIOException() throws IOException {
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        in.close();
        in.read(new byte[5], 0, 5);
    }

    // Tests available() method on closed stream throws IOException
    @Test(expected = IOException.class)
    public void testAvailable_afterClose_throwsIOException() throws IOException {
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        in.close();
        in.available();
    }

    // Tests custom block size constructor and skipRemainderOfLastBlock behavior
    @Test
    public void testConstructor_customBlockSize_skipsRemainder() throws IOException {
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "test.txt", 4);
        entry.setMode(0100644);
        byte[] archive = createArchive(new CpioArchiveEntry[]{entry}, new byte[][]{"test".getBytes()}, CpioConstants.FORMAT_NEW);

        // Pad archive to custom block size
        int customBlockSize = 512;
        byte[] padded = new byte[((archive.length + customBlockSize - 1) / customBlockSize) * customBlockSize];
        System.arraycopy(archive, 0, padded, 0, archive.length);

        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(padded), customBlockSize);
        assertNotNull(in.getNextEntry());
        byte[] buf = new byte[4];
        in.read(buf, 0, 4);
        assertNull(in.getNextEntry());
        in.close();
    }
}