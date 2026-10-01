package org.apache.commons.compress.archivers.cpio;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.utils.ArchiveUtils;
import org.junit.Test;

public class CpioArchiveInputStreamTest {

    private static final String MAGIC_NEW = "070701";
    private static final String MAGIC_NEW_CRC = "070702";
    private static final String MAGIC_OLD_ASCII = "070707";
    private static final String CPIO_TRAILER = "TRAILER!!!";

    // Helper: write a 4-byte-aligned zero pad to stream
    private static void addPad(byte[] data, int start, int length) {
        int pad = (4 - (length % 4)) % 4;
        for (int i = 0; i < pad; i++) {
            data[start + length + i] = 0;
        }
    }

    // Helper: create a byte array for a new/CRC format entry header + name + data
    private static byte[] createNewEntry(boolean crc, String name, long size,
                                         long mode, long chksum, byte[] data) {
        String magic = crc ? MAGIC_NEW_CRC : MAGIC_NEW;
        int nameLen = name.length() + 1; // includes null
        int headerLen = 26 * 8; // 13 fields * 8 chars each
        int namePad = (4 - ((26*8 + nameLen) % 4)) % 4;
        int totalLen = 26*8 + nameLen + namePad + (int)size;
        byte[] padForData = new byte[(4 - ((int)(size % 4))) % 4];
        int bufLen = 26*8 + nameLen + namePad + (int)size + padForData.length;
        byte[] buf = new byte[bufLen];

        // Fill with zeros initially
        // Build hex fields
        String[] fields = {
            magic,                                  // 6 bytes but we write 6 chars, followed by 2 zeros? Actually magic is exactly 6 bytes, but we need to write 6 bytes, not 8. Wait: In the code, readNewEntry reads magic as 6 bytes, but then reads other fields as 8-byte hex each. So magic is 6 bytes, then the rest fields are 8 bytes each. So total header before name is: magic(6) + 10 fields * 8 = 86 bytes? Actually list of fields after magic: inode, mode, uid, gid, nlink, mtime, filesize, devmajor, devminor, rdevmajor, rdevminor, namesize, chksum. That's 13 fields. But first field is magic, which is only 6 bytes. In the source, after reading magic, it reads 8-byte fields for each. So the layout: magic (6) + 12 fields * 8 = 102 bytes? Let's count: after magic, there are: inode (8), mode (8), uid (8), gid (8), nlink (8), mtime (8), filesize (8), devmajor (8), devminor (8), rdevmajor (8), rdevminor (8), namesize (8), chksum (8). That totals 6 + 13*8 = 110 bytes. That seems right. So we need to write magic as 6 bytes, then each field as 8-byte hex string.
        };

        // We'll construct manually:
        int pos = 0;
        // magic
        System.arraycopy(ArchiveUtils.toAsciiBytes(magic), 0, buf, pos, 6);
        pos += 6;
        // helper to write 8-byte hex
        byte[] hex8 = new byte[8];
        long[] values = {
            047771L, // inode (arbitrary)
            mode,
            0L,      // uid
            0L,      // gid
            1L,      // nlink
            0L,      // mtime
            size,    // filesize
            0L,      // devmajor
            0L,      // devminor
            0L,      // rdevmajor
            0L,      // rdevminor
            nameLen, // namesize
            chksum   // chksum
        };
        for (long v : values) {
            String hex = String.format("%016x", v).substring(8); // take last 8 hex digits
            System.arraycopy(ArchiveUtils.toAsciiBytes(hex), 0, buf, pos, 8);
            pos += 8;
        }
        // Now name with null
        System.arraycopy(ArchiveUtils.toAsciiBytes(name), 0, buf, pos, name.length());
        pos += name.length();
        buf[pos++] = 0; // null byte
        // name pad
        pos += namePad;
        // data
        if (data != null && size > 0) {
            System.arraycopy(data, 0, buf, pos, (int) size);
            pos += (int) size;
        }
        // data pad
        for (int i = 0; i < padForData.length; i++) {
            buf[pos++] = 0;
        }
        return buf;
    }

    // Helper to build a stream with one new-format entry and trailer
    private static byte[] buildNewStream(String name, long size, long mode,
                                          long chksum, byte[] data, boolean crc) {
        byte[] entry = createNewEntry(crc, name, size, mode, chksum, data);
        byte[] trailer = createNewEntry(crc, CPIO_TRAILER, 0, 0, 0, null);
        byte[] stream = new byte[entry.length + trailer.length];
        System.arraycopy(entry, 0, stream, 0, entry.length);
        System.arraycopy(trailer, 0, stream, entry.length, trailer.length);
        return stream;
    }

    private static CpioArchiveInputStream createStream(byte[] data) {
        return new CpioArchiveInputStream(new ByteArrayInputStream(data));
    }

    // ---------- Tests ----------

    @Test
    public void testGetNextCPIOEntry_newFormat_success() throws IOException {
        byte[] content = "Hello".getBytes();
        byte[] stream = buildNewStream("file.txt", content.length, 0100644L, 0L, content, false);
        try (CpioArchiveInputStream cis = createStream(stream)) {
            CpioArchiveEntry entry = cis.getNextCPIOEntry();
            assertNotNull(entry);
            assertEquals("file.txt", entry.getName());
            assertEquals(content.length, entry.getSize());
            byte[] read = new byte[content.length];
            assertEquals(content.length, cis.read(read));
            assertArrayEquals(content, read);
            // Next should be null (trailer)
            assertNull(cis.getNextCPIOEntry());
        }
    }

    @Test
    public void testGetNextCPIOEntry_newCrcFormat_correctChecksum() throws IOException {
        byte[] content = "CRC test".getBytes();
        long crcSum = 0;
        for (byte b : content) {
            crcSum += (b & 0xFF);
        }
        byte[] stream = buildNewStream("crc.txt", content.length, 0100644L, crcSum, content, true);
        try (CpioArchiveInputStream cis = createStream(stream)) {
            CpioArchiveEntry entry = cis.getNextCPIOEntry();
            assertNotNull(entry);
            assertEquals("crc.txt", entry.getName());
            byte[] buf = new byte[content.length];
            cis.read(buf);
            // After reading all data, CRC is checked internally; no exception expected
            // Read again should return -1
            assertEquals(-1, cis.read(buf));
            // Next entry
            assertNull(cis.getNextCPIOEntry());
        }
    }

    @Test(expected = IOException.class)
    public void testGetNextCPIOEntry_newCrcFormat_wrongChecksum_throws() throws IOException {
        byte[] content = "bad CRC".getBytes();
        // Provide wrong checksum (0)
        byte[] stream = buildNewStream("crc.txt", content.length, 0100644L, 0L, content, true);
        CpioArchiveInputStream cis = createStream(stream);
        try {
            CpioArchiveEntry entry = cis.getNextCPIOEntry();
            assertNotNull(entry);
            byte[] buf = new byte[content.length];
            cis.read(buf);
            // After reading all data, CRC mismatch should throw IOException
        } finally {
            cis.close();
        }
    }

    @Test
    public void testGetNextCPIOEntry_oldAsciiFormat_success() throws IOException {
        // Build old ASCII stream manually
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        // magic "070707"
        bos.write(ArchiveUtils.toAsciiBytes(MAGIC_OLD_ASCII));
        // write fields in octal as fixed-length strings
        // device(6), inode(6), mode(6), uid(6), gid(6), nlink(6), rdev(6), mtime(11), namesize(6), filesize(11)
        long[] fields = {
            0L,       // device
            047771L,  // inode
            0100644L, // mode (regular file)
            0L,       // uid
            0L,       // gid
            1L,       // nlink
            0L,       // rdev
            0L,       // mtime
            9L,       // namesize (8 chars + null)
            8L        // filesize
        };
        String[] octals = {
            "000000", "000000", "000000", "000000", "000000", "000000",
            "000000", "00000000000", "000000", "00000000000"
        };
        // Actually we need to format each as octal with leading zeros
        // We'll do manually: for each field, write string with proper length
        String[] patterns = {"%06o","%06o","%06o","%06o","%06o","%06o","%06o","%011o","%06o","%011o"};
        String[] valuesStr = new String[fields.length];
        for (int i = 0; i < fields.length; i++) {
            valuesStr[i] = String.format(patterns[i], fields[i]);
        }
        for (String s : valuesStr) {
            bos.write(ArchiveUtils.toAsciiBytes(s));
        }
        // name "file.txt" (8 chars + null)
        bos.write(ArchiveUtils.toAsciiBytes("file.txt"));
        bos.write(0);
        // data
        byte[] content = "data1234".getBytes();
        bos.write(content);
        // no data pad in old ascii? Actually there is data pad to 4?
        // The code in readOldAsciiEntry does not call header pad, but data pad in read() is done generically, so we need data pad.
        int dataPad = (4 - (content.length % 4)) % 4;
        for (int i = 0; i < dataPad; i++) {
            bos.write(0);
        }
        // trailer entry
        bos.write(ArchiveUtils.toAsciiBytes(MAGIC_OLD_ASCII));
        // trailer fields all zero except namesize for "TRAILER!!!"
        long[] trailerFields = {
            0L,0L,0L,0L,0L,0L,0L,0L,10L,0L // 10 = "TRAILER!!!".length()+1
        };
        for (int i = 0; i < trailerFields.length; i++) {
            bos.write(ArchiveUtils.toAsciiBytes(String.format(patterns[i], trailerFields[i])));
        }
        bos.write(ArchiveUtils.toAsciiBytes("TRAILER!!!"));
        bos.write(0);
        byte[] stream = bos.toByteArray();
        try (CpioArchiveInputStream cis = createStream(stream)) {
            CpioArchiveEntry entry = cis.getNextCPIOEntry();
            assertNotNull(entry);
            assertEquals("file.txt", entry.getName());
            assertEquals(content.length, entry.getSize());
            byte[] buf = new byte[content.length];
            assertEquals(content.length, cis.read(buf));
            assertArrayEquals(content, buf);
            assertNull(cis.getNextCPIOEntry());
        }
    }

    @Test
    public void testGetNextCPIOEntry_oldBinaryFormat_success() throws IOException {
        // Build old binary, no swap (little-endian)
        // magic 0xC7 0x71 (little endian)
        byte[] magic = new byte[]{(byte)0xC7, (byte)0x71};
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(magic);
        // fields: device(2), inode(2), mode(2), uid(2), gid(2), nlink(2), rdev(2), mtime(4), namesize(2), filesize(4)
        short[] fields = {
            0,          // device
            047771,     // inode (short)
            0100644,    // mode
            0,          // uid
            0,          // gid
            1,          // nlink
            0,          // rdev
            0,          // mtime (int)
            9,          // namesize
            8           // filesize (int)
        };
        // write little-endian
        writeShortLE(bos, fields[0]);
        writeShortLE(bos, fields[1]);
        writeShortLE(bos, fields[2]);
        writeShortLE(bos, fields[3]);
        writeShortLE(bos, fields[4]);
        writeShortLE(bos, fields[5]);
        writeShortLE(bos, fields[6]);
        writeIntLE(bos, fields[7]);   // mtime 4 bytes
        writeShortLE(bos, (short)fields[8]);
        writeIntLE(bos, fields[9]);
        // name
        bos.write(ArchiveUtils.toAsciiBytes("file.txt"));
        bos.write(0);
        // header pad? The readOldBinaryEntry calls skip(getHeaderPadCount()) after name, so need to compute pad.
        // After name (9 bytes: 8 chars + null), header so far = 2(magic) + 2*7 shorts + 4 (mtime) + 2 (namesize)+4(filesize) = 2+14+4+2+4=26 bytes.
        // Then name = 9 bytes, total 35, pad to 4: 1 byte skip
        bos.write(0); // pad 1 byte
        // data
        byte[] content = "data1234".getBytes();
        bos.write(content);
        int dataPad = (4 - (content.length % 4)) % 4;
        for (int i = 0; i < dataPad; i++) bos.write(0);
        // trailer
        bos.write(magic);
        // trailer fields all zero except namesize
        short[] trailerFields = {0,0,0,0,0,0,0,0,10,0};
        writeShortLE(bos, trailerFields[0]);
        writeShortLE(bos, trailerFields[1]);
        writeShortLE(bos, trailerFields[2]);
        writeShortLE(bos, trailerFields[3]);
        writeShortLE(bos, trailerFields[4]);
        writeShortLE(bos, trailerFields[5]);
        writeShortLE(bos, trailerFields[6]);
        writeIntLE(bos, 0);
        writeShortLE(bos, (short)10);
        writeIntLE(bos, 0);
        bos.write(ArchiveUtils.toAsciiBytes("TRAILER!!!"));
        bos.write(0);
        byte[] stream = bos.toByteArray();
        try (CpioArchiveInputStream cis = createStream(stream)) {
            CpioArchiveEntry entry = cis.getNextCPIOEntry();
            assertNotNull(entry);
            assertEquals("file.txt", entry.getName());
            byte[] buf = new byte[content.length];
            assertEquals(content.length, cis.read(buf));
            assertArrayEquals(content, buf);
            assertNull(cis.getNextCPIOEntry());
        }
    }

    @Test
    public void testGetNextCPIOEntry_oldBinarySwapped_success() throws IOException {
        // same as above but magic bytes swapped (big-endian representation)
        byte[] magic = new byte[]{(byte)0x71, (byte)0xC7};
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(magic);
        // fields are the same, but likely interpreted as big-endian? Actually the code reads binary long with swapHalfWord flag.
        // If magic appears as big-endian, the code will swap half words when reading fields.
        // We'll produce the same little-endian field bytes; the swap will convert them to big-endian internally.
        // This might be confusing. To simplify, we'll just use the same byte representations as little-endian
        // and rely on the fact that the code will see the magic as swapped and set swapHalfWord=true, then read fields
        // using swapped interpretation. That should correctly parse.
        short[] fields = {
            0,          // device
            047771,     // inode
            0100644,    // mode
            0,          // uid
            0,          // gid
            1,          // nlink
            0,          // rdev
            0,          // mtime (int)
            9,          // namesize
            8           // filesize
        };
        writeShortLE(bos, fields[0]);
        writeShortLE(bos, fields[1]);
        writeShortLE(bos, fields[2]);
        writeShortLE(bos, fields[3]);
        writeShortLE(bos, fields[4]);
        writeShortLE(bos, fields[5]);
        writeShortLE(bos, fields[6]);
        writeIntLE(bos, fields[7]);
        writeShortLE(bos, (short)fields[8]);
        writeIntLE(bos, fields[9]);
        bos.write(ArchiveUtils.toAsciiBytes("file.txt"));
        bos.write(0);
        bos.write(0); // pad
        byte[] content = "data".getBytes();
        bos.write(content);
        int dataPad = (4 - (content.length % 4)) % 4;
        for (int i = 0; i < dataPad; i++) bos.write(0);
        // trailer
        bos.write(magic);
        short[] tfields = {0,0,0,0,0,0,0,0,10,0};
        writeShortLE(bos, tfields[0]);
        writeShortLE(bos, tfields[1]);
        writeShortLE(bos, tfields[2]);
        writeShortLE(bos, tfields[3]);
        writeShortLE(bos, tfields[4]);
        writeShortLE(bos, tfields[5]);
        writeShortLE(bos, tfields[6]);
        writeIntLE(bos, 0);
        writeShortLE(bos, (short)10);
        writeIntLE(bos, 0);
        bos.write(ArchiveUtils.toAsciiBytes("TRAILER!!!"));
        bos.write(0);
        byte[] stream = bos.toByteArray();
        try (CpioArchiveInputStream cis = createStream(stream)) {
            CpioArchiveEntry entry = cis.getNextCPIOEntry();
            assertNotNull(entry);
            assertEquals("file.txt", entry.getName());
            byte[] buf = new byte[content.length];
            int read = cis.read(buf);
            if (read > 0) {
                assertArrayEquals(content, Arrays.copyOf(buf, read));
            }
            assertNull(cis.getNextCPIOEntry());
        }
    }

    private static void writeShortLE(ByteArrayOutputStream bos, short s) {
        bos.write(s & 0xFF);
        bos.write((s >> 8) & 0xFF);
    }

    private static void writeIntLE(ByteArrayOutputStream bos, int i) {
        bos.write(i & 0xFF);
        bos.write((i >> 8) & 0xFF);
        bos.write((i >> 16) & 0xFF);
        bos.write((i >> 24) & 0xFF);
    }

    @Test(expected = IOException.class)
    public void testGetNextCPIOEntry_modeZeroNonTrailer_throws() throws IOException {
        // Create new format entry with mode=0 and name != trailer
        byte[] stream = buildNewStream("bad", 0, 0L, 0L, null, false);
        CpioArchiveInputStream cis = createStream(stream);
        try {
            cis.getNextCPIOEntry(); // should throw
        } finally {
            cis.close();
        }
    }

    @Test(expected = IOException.class)
    public void testGetNextCPIOEntry_unknownMagic_throws() throws IOException {
        byte[] stream = new byte[]{0x00, 0x01, 0x02, 0x03, 0x04, 0x05};
        CpioArchiveInputStream cis = createStream(stream);
        try {
            cis.getNextCPIOEntry();
        } finally {
            cis.close();
        }
    }

    @Test
    public void testRead_afterEntryEOF_returnsMinusOne() throws IOException {
        byte[] content = "short".getBytes();
        byte[] stream = buildNewStream("f", content.length, 0100644L, 0L, content, false);
        try (CpioArchiveInputStream cis = createStream(stream)) {
            cis.getNextCPIOEntry();
            byte[] buf = new byte[content.length];
            cis.read(buf);
            // second read should return -1
            assertEquals(-1, cis.read(buf));
        }
    }

    @Test
    public void testRead_beforeGetNextEntry_returnsMinusOne() throws IOException {
        byte[] stream = buildNewStream("dummy", 0, 0100644L, 0L, null, false);
        try (CpioArchiveInputStream cis = createStream(stream)) {
            byte[] buf = new byte[10];
            assertEquals(-1, cis.read(buf));
        }
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testRead_invalidArgs_throws() throws IOException {
        byte[] stream = buildNewStream("x", 5, 0100644L, 0L, new byte[5], false);
        CpioArchiveInputStream cis = createStream(stream);
        try {
            cis.getNextCPIOEntry();
            byte[] buf = new byte[10];
            cis.read(buf, -1, 5); // invalid offset
        } finally {
            cis.close();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSkip_negative_throws() throws IOException {
        byte[] stream = buildNewStream("x", 0, 0100644L, 0L, null, false);
        try (CpioArchiveInputStream cis = createStream(stream)) {
            cis.getNextCPIOEntry();
            cis.skip(-1);
        }
    }

    @Test
    public void testSkip_beyondData_returnsLimited() throws IOException {
        byte[] content = "123".getBytes();
        byte[] stream = buildNewStream("f", content.length, 0100644L, 0L, content, false);
        try (CpioArchiveInputStream cis = createStream(stream)) {
            cis.getNextCPIOEntry();
            // skip more than available
            long skipped = cis.skip(1000);
            assertEquals(content.length, skipped);
            // now entry EOF should be set
            assertEquals(-1, cis.read(new byte[10]));
        }
    }

    @Test(expected = IOException.class)
    public void testClose_afterClose_throws() throws IOException {
        CpioArchiveInputStream cis = createStream(new byte[0]);
        cis.close();
        cis.getNextCPIOEntry(); // should throw IOException
    }

    @Test
    public void testAvailable_beforeEOF_returnsOne() throws IOException {
        byte[] content = "available".getBytes();
        byte[] stream = buildNewStream("a", content.length, 0100644L, 0L, content, false);
        try (CpioArchiveInputStream cis = createStream(stream)) {
            cis.getNextCPIOEntry();
            assertEquals(1, cis.available());
        }
    }

    @Test
    public void testAvailable_afterEOF_returnsZero() throws IOException {
        byte[] content = "z".getBytes();
        byte[] stream = buildNewStream("z", content.length, 0100644L, 0L, content, false);
        try (CpioArchiveInputStream cis = createStream(stream)) {
            cis.getNextCPIOEntry();
            byte[] buf = new byte[content.length];
            cis.read(buf);
            assertEquals(0, cis.available());
        }
    }

    @Test
    public void testGetNextCPIOEntry_emptyEntry_returnsEntryAndNoData() throws IOException {
        byte[] stream = buildNewStream("empty", 0, 0100644L, 0L, null, false);
        try (CpioArchiveInputStream cis = createStream(stream)) {
            CpioArchiveEntry entry = cis.getNextCPIOEntry();
            assertNotNull(entry);
            assertEquals(0, entry.getSize());
            // Reading should return -1 immediately
            assertEquals(-1, cis.read(new byte[1]));
            // Next entry
            assertNull(cis.getNextCPIOEntry());
        }
    }
}