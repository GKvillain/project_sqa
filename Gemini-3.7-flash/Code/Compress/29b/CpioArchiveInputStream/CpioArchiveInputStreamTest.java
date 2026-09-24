package org.apache.commons.compress.archivers.cpio;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.utils.CharsetNames;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;

import static org.junit.Assert.*;

public class CpioArchiveInputStreamTest {

    // Tests signature matching for all valid cpio formats
    @Test
    public void testMatches_validSignatures_returnsTrue() {
        byte[] oldBinary1 = new byte[] { 0x71, (byte) 0xc7, 0, 0, 0, 0 };
        byte[] oldBinary2 = new byte[] { (byte) 0xc7, 0x71, 0, 0, 0, 0 };
        byte[] magicNew = "070701".getBytes();
        byte[] magicNewCrc = "070702".getBytes();
        byte[] magicOldAscii = "070707".getBytes();

        assertTrue(CpioArchiveInputStream.matches(oldBinary1, 6));
        assertTrue(CpioArchiveInputStream.matches(oldBinary2, 6));
        assertTrue(CpioArchiveInputStream.matches(magicNew, 6));
        assertTrue(CpioArchiveInputStream.matches(magicNewCrc, 6));
        assertTrue(CpioArchiveInputStream.matches(magicOldAscii, 6));
    }

    // Tests signature matching for invalid signatures and short length
    @Test
    public void testMatches_invalidSignatures_returnsFalse() {
        byte[] shortSig = new byte[] { 0x71, (byte) 0xc7, 0, 0, 0 };
        byte[] invalidMagic = "070708".getBytes();
        byte[] randomBytes = new byte[] { 1, 2, 3, 4, 5, 6 };

        assertFalse(CpioArchiveInputStream.matches(shortSig, 5));
        assertFalse(CpioArchiveInputStream.matches(invalidMagic, 6));
        assertFalse(CpioArchiveInputStream.matches(randomBytes, 6));
    }

    // Tests constructors with different parameters
    @Test
    public void testConstructors_validInputs_createsInstance() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        CpioArchiveInputStream stream1 = new CpioArchiveInputStream(in);
        assertNotNull(stream1);
        stream1.close();

        ByteArrayInputStream in2 = new ByteArrayInputStream(new byte[0]);
        CpioArchiveInputStream stream2 = new CpioArchiveInputStream(in2, CharsetNames.UTF_8);
        assertNotNull(stream2);
        stream2.close();

        ByteArrayInputStream in3 = new ByteArrayInputStream(new byte[0]);
        CpioArchiveInputStream stream3 = new CpioArchiveInputStream(in3, 1024);
        assertNotNull(stream3);
        stream3.close();

        ByteArrayInputStream in4 = new ByteArrayInputStream(new byte[0]);
        CpioArchiveInputStream stream4 = new CpioArchiveInputStream(in4, 1024, CharsetNames.UTF_8);
        assertNotNull(stream4);
        stream4.close();
    }

    // Tests available on open stream before reading any entry
    @Test
    public void testAvailable_openStreamInitial_returnsOne() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        CpioArchiveInputStream stream = new CpioArchiveInputStream(in);
        assertEquals(1, stream.available());
        stream.close();
    }

    // Tests available after stream is closed throws IOException
    @Test(expected = IOException.class)
    public void testAvailable_closedStream_throwsIOException() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        CpioArchiveInputStream stream = new CpioArchiveInputStream(in);
        stream.close();
        stream.available();
    }

    // Tests close can be called multiple times without error
    @Test
    public void testClose_multipleCalls_noException() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        CpioArchiveInputStream stream = new CpioArchiveInputStream(in);
        stream.close();
        stream.close();
    }

    // Tests read with invalid bounds throws IndexOutOfBoundsException
    @Test(expected = IndexOutOfBoundsException.class)
    public void testRead_negativeOffset_throwsIndexOutOfBoundsException() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        CpioArchiveInputStream stream = new CpioArchiveInputStream(in);
        byte[] buf = new byte[10];
        try {
            stream.read(buf, -1, 5);
        } finally {
            stream.close();
        }
    }

    // Tests read with zero length returns 0
    @Test
    public void testRead_zeroLength_returnsZero() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        CpioArchiveInputStream stream = new CpioArchiveInputStream(in);
        byte[] buf = new byte[10];
        assertEquals(0, stream.read(buf, 0, 0));
        stream.close();
    }

    // Tests read when stream is closed throws IOException
    @Test(expected = IOException.class)
    public void testRead_closedStream_throwsIOException() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        CpioArchiveInputStream stream = new CpioArchiveInputStream(in);
        stream.close();
        byte[] buf = new byte[10];
        stream.read(buf, 0, buf.length);
    }

    // Tests read when no entry has been opened returns -1
    @Test
    public void testRead_noEntry_returnsMinusOne() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        CpioArchiveInputStream stream = new CpioArchiveInputStream(in);
        byte[] buf = new byte[10];
        assertEquals(-1, stream.read(buf, 0, buf.length));
        stream.close();
    }

    // Tests skip with negative value throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testSkip_negativeValue_throwsIllegalArgumentException() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        CpioArchiveInputStream stream = new CpioArchiveInputStream(in);
        try {
            stream.skip(-1);
        } finally {
            stream.close();
        }
    }

    // Tests skip when stream is closed throws IOException
    @Test(expected = IOException.class)
    public void testSkip_closedStream_throwsIOException() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        CpioArchiveInputStream stream = new CpioArchiveInputStream(in);
        stream.close();
        stream.skip(5);
    }

    // Tests reading from an empty stream throws EOFException when expecting magic
    @Test(expected = EOFException.class)
    public void testGetNextCPIOEntry_emptyStream_throwsEOFException() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        CpioArchiveInputStream stream = new CpioArchiveInputStream(in);
        try {
            stream.getNextCPIOEntry();
        } finally {
            stream.close();
        }
    }

    // Tests reading an archive with unknown magic header throws IOException
    @Test(expected = IOException.class)
    public void testGetNextCPIOEntry_unknownMagic_throwsIOException() throws IOException {
        byte[] invalidHeader = "99999900000000".getBytes();
        ByteArrayInputStream in = new ByteArrayInputStream(invalidHeader);
        CpioArchiveInputStream stream = new CpioArchiveInputStream(in);
        try {
            stream.getNextCPIOEntry();
        } finally {
            stream.close();
        }
    }

    // Tests reading a valid NEW ASCII formatted cpio archive entry and data
    @Test
    public void testReadNewEntry_validArchive_readsSuccessfully() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writeNewEntryHeader(baos, "070701", 0100644, 4, "test", 0);
        baos.write("data".getBytes());
        writeNewTrailer(baos, "070701");

        CpioArchiveInputStream stream = new CpioArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        CpioArchiveEntry entry = stream.getNextCPIOEntry();
        assertNotNull(entry);
        assertEquals("test", entry.getName());
        assertEquals(4, entry.getSize());
        assertEquals(CpioConstants.FORMAT_NEW, entry.getFormat());

        byte[] content = new byte[4];
        int read = stream.read(content, 0, 4);
        assertEquals(4, read);
        assertEquals("data", new String(content));
        assertEquals(-1, stream.read(content, 0, 4));
        assertEquals(0, stream.available());

        ArchiveEntry nextEntry = stream.getNextEntry();
        assertNull(nextEntry);
        stream.close();
    }

    // Tests reading NEW CRC format with mismatched checksum throws IOException
    @Test(expected = IOException.class)
    public void testReadNewCrcEntry_crcMismatch_throwsIOException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writeNewEntryHeader(baos, "070702", 0100644, 4, "test", 99999);
        baos.write("data".getBytes());
        writeNewTrailer(baos, "070702");

        CpioArchiveInputStream stream = new CpioArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        try {
            CpioArchiveEntry entry = stream.getNextCPIOEntry();
            assertNotNull(entry);
            byte[] content = new byte[4];
            stream.read(content, 0, 4);
            stream.read(content, 0, 4);
        } finally {
            stream.close();
        }
    }

    // Tests reading mode 0 on a non-trailer entry throws IOException
    @Test(expected = IOException.class)
    public void testReadNewEntry_modeZeroNotTrailer_throwsIOException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writeNewEntryHeader(baos, "070701", 0, 0, "nontrailer", 0);
        writeNewTrailer(baos, "070701");

        CpioArchiveInputStream stream = new CpioArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        try {
            stream.getNextCPIOEntry();
        } finally {
            stream.close();
        }
    }

    // Tests reading OLD ASCII formatted cpio archive entry
    @Test
    public void testReadOldAsciiEntry_validArchive_readsSuccessfully() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writeOldAsciiEntryHeader(baos, 0100644, 5, "file1");
        baos.write("hello".getBytes());
        writeOldAsciiTrailer(baos);

        CpioArchiveInputStream stream = new CpioArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        CpioArchiveEntry entry = stream.getNextCPIOEntry();
        assertNotNull(entry);
        assertEquals("file1", entry.getName());
        assertEquals(5, entry.getSize());
        assertEquals(CpioConstants.FORMAT_OLD_ASCII, entry.getFormat());

        byte[] content = new byte[5];
        int read = stream.read(content, 0, 5);
        assertEquals(5, read);
        assertEquals("hello", new String(content));

        assertNull(stream.getNextCPIOEntry());
        stream.close();
    }

    // Tests skipping bytes inside an entry
    @Test
    public void testSkip_withinEntry_skipsExpectedBytes() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writeNewEntryHeader(baos, "070701", 0100644, 8, "test", 0);
        baos.write("12345678".getBytes());
        writeNewTrailer(baos, "070701");

        CpioArchiveInputStream stream = new CpioArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        CpioArchiveEntry entry = stream.getNextCPIOEntry();
        assertNotNull(entry);

        long skipped = stream.skip(4);
        assertEquals(4, skipped);

        byte[] content = new byte[4];
        int read = stream.read(content, 0, 4);
        assertEquals(4, read);
        assertEquals("5678", new String(content));
        stream.close();
    }

    // Tests read with negative length throws IndexOutOfBoundsException
    @Test(expected = IndexOutOfBoundsException.class)
    public void testRead_negativeLength_throwsIndexOutOfBoundsException() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        CpioArchiveInputStream stream = new CpioArchiveInputStream(in);
        byte[] buf = new byte[10];
        try {
            stream.read(buf, 0, -1);
        } finally {
            stream.close();
        }
    }

    // Tests read with offset plus length greater than buffer length throws IndexOutOfBoundsException
    @Test(expected = IndexOutOfBoundsException.class)
    public void testRead_offsetPlusLengthExceedsBuffer_throwsIndexOutOfBoundsException() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        CpioArchiveInputStream stream = new CpioArchiveInputStream(in);
        byte[] buf = new byte[10];
        try {
            stream.read(buf, 5, 6);
        } finally {
            stream.close();
        }
    }

    // Tests skip with 0 returns 0
    @Test
    public void testSkip_zeroBytes_returnsZero() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writeNewEntryHeader(baos, "070701", 0100644, 4, "test", 0);
        baos.write("data".getBytes());
        writeNewTrailer(baos, "070701");

        CpioArchiveInputStream stream = new CpioArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        assertNotNull(stream.getNextCPIOEntry());
        assertEquals(0, stream.skip(0));
        stream.close();
    }

    // Tests skip with amount larger than remaining entry bytes is capped to entry size
    @Test
    public void testSkip_largerThanEntrySize_cappedToRemaining() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writeNewEntryHeader(baos, "070701", 0100644, 4, "test", 0);
        baos.write("data".getBytes());
        writeNewTrailer(baos, "070701");

        CpioArchiveInputStream stream = new CpioArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        assertNotNull(stream.getNextCPIOEntry());
        long skipped = stream.skip(100);
        assertEquals(4, skipped);
        assertEquals(0, stream.available());
        assertEquals(-1, stream.read(new byte[1], 0, 1));
        stream.close();
    }

    // Tests reading NEW CRC format with matching checksum succeeds
    @Test
    public void testReadNewCrcEntry_validChecksum_readsSuccessfully() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] data = "ABCD".getBytes();
        long checksum = 0;
        for (byte b : data) {
            checksum += (b & 0xFF);
        }
        writeNewEntryHeader(baos, "070702", 0100644, data.length, "validCrc", checksum);
        baos.write(data);
        writeNewTrailer(baos, "070702");

        CpioArchiveInputStream stream = new CpioArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        CpioArchiveEntry entry = stream.getNextCPIOEntry();
        assertNotNull(entry);
        assertEquals(CpioConstants.FORMAT_NEW_CRC, entry.getFormat());

        byte[] content = new byte[4];
        assertEquals(4, stream.read(content, 0, 4));
        assertEquals(-1, stream.read(content, 0, 4));
        assertNull(stream.getNextCPIOEntry());
        stream.close();
    }

    // Tests reading Old Binary format (standard byte order: 0x71, 0xc7)
    @Test
    public void testReadOldBinaryEntry_standardByteOrder_readsSuccessfully() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writeOldBinaryEntryHeader(baos, false, 0100644, 5, "fileB");
        baos.write("world".getBytes());
        if ((5 % 2) != 0) {
            baos.write(0); // binary data 2-byte alignment padding
        }
        writeOldBinaryTrailer(baos, false);

        CpioArchiveInputStream stream = new CpioArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        CpioArchiveEntry entry = stream.getNextCPIOEntry();
        assertNotNull(entry);
        assertEquals("fileB", entry.getName());
        assertEquals(5, entry.getSize());
        assertEquals(CpioConstants.FORMAT_OLD_BINARY, entry.getFormat());

        byte[] content = new byte[5];
        assertEquals(5, stream.read(content, 0, 5));
        assertEquals("world", new String(content));
        assertNull(stream.getNextCPIOEntry());
        stream.close();
    }

    // Tests reading Old Binary format (swapped half-word byte order: 0xc7, 0x71)
    @Test
    public void testReadOldBinaryEntry_swappedByteOrder_readsSuccessfully() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writeOldBinaryEntryHeader(baos, true, 0100644, 4, "swap");
        baos.write("swap".getBytes());
        writeOldBinaryTrailer(baos, true);

        CpioArchiveInputStream stream = new CpioArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        CpioArchiveEntry entry = stream.getNextCPIOEntry();
        assertNotNull(entry);
        assertEquals("swap", entry.getName());
        assertEquals(4, entry.getSize());
        assertEquals(CpioConstants.FORMAT_OLD_BINARY, entry.getFormat());

        byte[] content = new byte[4];
        assertEquals(4, stream.read(content, 0, 4));
        assertEquals("swap", new String(content));
        assertNull(stream.getNextCPIOEntry());
        stream.close();
    }

    // Tests reading multiple entries sequentially when first entry content is not read completely
    @Test
    public void testMultiEntry_skipToNextEntryAutomatically() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writeNewEntryHeader(baos, "070701", 0100644, 10, "entry1", 0);
        baos.write("0123456789".getBytes());
        int pad1 = (4 - (10 % 4)) % 4;
        for (int i = 0; i < pad1; i++) {
            baos.write(0);
        }

        writeNewEntryHeader(baos, "070701", 0100644, 5, "entry2", 0);
        baos.write("hello".getBytes());
        int pad2 = (4 - (5 % 4)) % 4;
        for (int i = 0; i < pad2; i++) {
            baos.write(0);
        }
        writeNewTrailer(baos, "070701");

        CpioArchiveInputStream stream = new CpioArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        CpioArchiveEntry e1 = stream.getNextCPIOEntry();
        assertNotNull(e1);
        assertEquals("entry1", e1.getName());

        // Without reading all bytes of e1, request next entry
        CpioArchiveEntry e2 = stream.getNextCPIOEntry();
        assertNotNull(e2);
        assertEquals("entry2", e2.getName());

        byte[] buf = new byte[5];
        assertEquals(5, stream.read(buf, 0, 5));
        assertEquals("hello", new String(buf));

        assertNull(stream.getNextCPIOEntry());
        assertNull(stream.getNextCPIOEntry()); // Calling after EOF should return null
        stream.close();
    }

    // Tests read when requesting more bytes than remaining in the current entry
    @Test
    public void testRead_requestMoreThanRemaining_readsOnlyRemaining() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writeNewEntryHeader(baos, "070701", 0100644, 3, "short", 0);
        baos.write("abc".getBytes());
        int pad = (4 - (3 % 4)) % 4;
        for (int i = 0; i < pad; i++) {
            baos.write(0);
        }
        writeNewTrailer(baos, "070701");

        CpioArchiveInputStream stream = new CpioArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        assertNotNull(stream.getNextCPIOEntry());

        byte[] buf = new byte[10];
        int read = stream.read(buf, 0, 10);
        assertEquals(3, read);
        assertEquals("abc", new String(buf, 0, 3));
        assertEquals(-1, stream.read(buf, 0, 10));
        stream.close();
    }

    // Helper method to write Old Binary Format Header
    private void writeOldBinaryEntryHeader(ByteArrayOutputStream out, boolean swapHalfWord, long mode, long size, String name) throws IOException {
        byte[] nameBytes = (name + "\0").getBytes();
        if (swapHalfWord) {
            out.write((byte) 0xc7);
            out.write(0x71);
            write16(out, 0, true);          // dev
            write16(out, 1, true);          // ino
            write16(out, (int) mode, true); // mode
            write16(out, 0, true);          // uid
            write16(out, 0, true);          // gid
            write16(out, 1, true);          // nlink
            write16(out, 0, true);          // rdev
            write32(out, 0, true);          // mtime
            write16(out, nameBytes.length, true); // namesize
            write32(out, size, true);       // filesize
        } else {
            out.write(0x71);
            out.write((byte) 0xc7);
            write16(out, 0, false);          // dev
            write16(out, 1, false);          // ino
            write16(out, (int) mode, false); // mode
            write16(out, 0, false);          // uid
            write16(out, 0, false);          // gid
            write16(out, 1, false);          // nlink
            write16(out, 0, false);          // rdev
            write32(out, 0, false);          // mtime
            write16(out, nameBytes.length, false); // namesize
            write32(out, size, false);       // filesize
        }
        out.write(nameBytes);
        int pad = (2 - ((26 + nameBytes.length) % 2)) % 2;
        for (int i = 0; i < pad; i++) {
            out.write(0);
        }
    }

    private void write16(ByteArrayOutputStream out, int val, boolean swap) {
        byte b0 = (byte) ((val >> 8) & 0xff);
        byte b1 = (byte) (val & 0xff);
        if (swap) {
            out.write(b1);
            out.write(b0);
        } else {
            out.write(b0);
            out.write(b1);
        }
    }

    private void write32(ByteArrayOutputStream out, long val, boolean swap) {
        byte b0 = (byte) ((val >> 24) & 0xff);
        byte b1 = (byte) ((val >> 16) & 0xff);
        byte b2 = (byte) ((val >> 8) & 0xff);
        byte b3 = (byte) (val & 0xff);
        if (swap) {
            out.write(b2);
            out.write(b3);
            out.write(b0);
            out.write(b1);
        } else {
            out.write(b0);
            out.write(b1);
            out.write(b2);
            out.write(b3);
        }
    }

    // Helper method to write Old Binary Trailer
    private void writeOldBinaryTrailer(ByteArrayOutputStream out, boolean swapHalfWord) throws IOException {
        writeOldBinaryEntryHeader(out, swapHalfWord, 0, 0, CpioConstants.CPIO_TRAILER);
        int totalWritten = out.size();
        int pad = (512 - (totalWritten % 512)) % 512;
        for (int i = 0; i < pad; i++) {
            out.write(0);
        }
    }

    // Helper method to write New Format CPIO Header
    private void writeNewEntryHeader(ByteArrayOutputStream out, String magic, long mode, long size, String name, long chksum) throws IOException {
        byte[] nameBytes = (name + "\0").getBytes();
        StringBuilder sb = new StringBuilder();
        sb.append(magic);
        sb.append(String.format("%08X", 1)); // inode
        sb.append(String.format("%08X", mode)); // mode
        sb.append(String.format("%08X", 0)); // uid
        sb.append(String.format("%08X", 0)); // gid
        sb.append(String.format("%08X", 1)); // nlink
        sb.append(String.format("%08X", 0)); // mtime
        sb.append(String.format("%08X", size)); // filesize
        sb.append(String.format("%08X", 0)); // maj
        sb.append(String.format("%08X", 0)); // min
        sb.append(String.format("%08X", 0)); // rmaj
        sb.append(String.format("%08X", 0)); // rmin
        sb.append(String.format("%08X", nameBytes.length)); // namesize
        sb.append(String.format("%08X", chksum)); // chksum
        out.write(sb.toString().getBytes());
        out.write(nameBytes);
        int pad = (4 - ((110 + nameBytes.length) % 4)) % 4;
        for (int i = 0; i < pad; i++) {
            out.write(0);
        }
    }

    // Helper method to write New Format Trailer
    private void writeNewTrailer(ByteArrayOutputStream out, String magic) throws IOException {
        writeNewEntryHeader(out, magic, 0, 0, CpioConstants.CPIO_TRAILER, 0);
        int totalWritten = out.size();
        int pad = (512 - (totalWritten % 512)) % 512;
        for (int i = 0; i < pad; i++) {
            out.write(0);
        }
    }

    // Helper method to write Old ASCII Header
    private void writeOldAsciiEntryHeader(ByteArrayOutputStream out, long mode, long size, String name) throws IOException {
        byte[] nameBytes = (name + "\0").getBytes();
        StringBuilder sb = new StringBuilder();
        sb.append("070707"); // magic
        sb.append(String.format("%06o", 0)); // dev
        sb.append(String.format("%06o", 1)); // ino
        sb.append(String.format("%06o", mode)); // mode
        sb.append(String.format("%06o", 0)); // uid
        sb.append(String.format("%06o", 0)); // gid
        sb.append(String.format("%06o", 1)); // nlink
        sb.append(String.format("%06o", 0)); // rdev
        sb.append(String.format("%011o", 0)); // mtime
        sb.append(String.format("%06o", nameBytes.length)); // namesize
        sb.append(String.format("%011o", size)); // filesize
        out.write(sb.toString().getBytes());
        out.write(nameBytes);
    }

    // Helper method to write Old ASCII Trailer
    private void writeOldAsciiTrailer(ByteArrayOutputStream out) throws IOException {
        writeOldAsciiEntryHeader(out, 0, 0, CpioConstants.CPIO_TRAILER);
        int totalWritten = out.size();
        int pad = (512 - (totalWritten % 512)) % 512;
        for (int i = 0; i < pad; i++) {
            out.write(0);
        }
    }
}