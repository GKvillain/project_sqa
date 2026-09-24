package org.apache.commons.compress.archivers.dump;

import org.apache.commons.compress.archivers.ArchiveException;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class DumpArchiveInputStreamTest {

    // Helper to write a 32-bit integer in little-endian format
    private static void write32(byte[] buffer, int offset, int value) {
        buffer[offset] = (byte) (value & 0xFF);
        buffer[offset + 1] = (byte) ((value >> 8) & 0xFF);
        buffer[offset + 2] = (byte) ((value >> 16) & 0xFF);
        buffer[offset + 3] = (byte) ((value >> 24) & 0xFF);
    }

    // Helper to write a 64-bit integer in little-endian format
    private static void write64(byte[] buffer, int offset, long value) {
        for (int i = 0; i < 8; i++) {
            buffer[offset + i] = (byte) ((value >> (i * 8)) & 0xFF);
        }
    }

    // Helper to calculate checksum according to dump archive format
    private static int calculateChecksum(byte[] buffer) {
        int calc = DumpArchiveConstants.CHECKSUM;
        for (int i = 0; i < 256; i++) {
            calc += DumpArchiveUtil.convert32(buffer, i * 4);
        }
        calc -= DumpArchiveUtil.convert32(buffer, 28);
        return calc;
    }

    // Helper to create a valid dump record buffer with proper checksum
    private static byte[] createRecord(int segmentType, int volumeNumber, int count, int ino) {
        byte[] record = new byte[DumpArchiveConstants.TP_SIZE];
        write32(record, 0, segmentType);
        write32(record, 12, volumeNumber);
        write32(record, 20, ino);
        write32(record, 24, DumpArchiveConstants.NFS_MAGIC);
        write32(record, 160, count);

        int checksum = calculateChecksum(record);
        write32(record, 28, checksum);
        return record;
    }

    // Helper to create a valid dump inode record
    private static byte[] createInodeRecord(int ino, int mode, long size, int count, byte[] cAddr) {
        byte[] record = new byte[DumpArchiveConstants.TP_SIZE];
        write32(record, 0, DumpArchiveConstants.SEGMENT_TYPE.INODE.code);
        write32(record, 12, 1);
        write32(record, 20, ino);
        write32(record, 24, DumpArchiveConstants.NFS_MAGIC);
        write32(record, 32, mode);
        write64(record, 48, size);
        write32(record, 160, count);
        if (cAddr != null) {
            System.arraycopy(cAddr, 0, record, 164, Math.min(cAddr.length, 512));
        }

        int checksum = calculateChecksum(record);
        write32(record, 28, checksum);
        return record;
    }

    // Tests matches with buffer length less than 32 bytes
    @Test
    public void testMatches_lengthLessThan32_returnsFalse() {
        byte[] buffer = new byte[31];
        assertFalse(DumpArchiveInputStream.matches(buffer, 31));
    }

    // Tests matches with 32 <= length < TP_SIZE and invalid magic
    @Test
    public void testMatches_shortLengthInvalidMagic_returnsFalse() {
        byte[] buffer = new byte[64];
        write32(buffer, 24, 12345);
        assertFalse(DumpArchiveInputStream.matches(buffer, 64));
    }

    // Tests matches with 32 <= length < TP_SIZE and valid NFS_MAGIC
    @Test
    public void testMatches_shortLengthValidMagic_returnsTrue() {
        byte[] buffer = new byte[64];
        write32(buffer, 24, DumpArchiveConstants.NFS_MAGIC);
        assertTrue(DumpArchiveInputStream.matches(buffer, 64));
    }

    // Tests matches with full TP_SIZE buffer and invalid checksum
    @Test
    public void testMatches_fullRecordInvalidChecksum_returnsFalse() {
        byte[] buffer = new byte[DumpArchiveConstants.TP_SIZE];
        write32(buffer, 24, DumpArchiveConstants.NFS_MAGIC);
        write32(buffer, 28, 0);
        assertFalse(DumpArchiveInputStream.matches(buffer, DumpArchiveConstants.TP_SIZE));
    }

    // Tests matches with full TP_SIZE buffer and valid checksum
    @Test
    public void testMatches_fullRecordValidChecksum_returnsTrue() {
        byte[] record = createRecord(DumpArchiveConstants.SEGMENT_TYPE.HEADER.code, 1, 0, 0);
        assertTrue(DumpArchiveInputStream.matches(record, record.length));
    }

    // Tests constructor with invalid header throws UnrecognizedFormatException
    @Test(expected = UnrecognizedFormatException.class)
    public void testConstructor_invalidHeader_throwsUnrecognizedFormatException() throws Exception {
        byte[] invalidHeader = new byte[DumpArchiveConstants.TP_SIZE];
        ByteArrayInputStream bais = new ByteArrayInputStream(invalidHeader);
        new DumpArchiveInputStream(bais);
    }

    // Tests constructor with empty input stream throws ArchiveException
    @Test(expected = ArchiveException.class)
    public void testConstructor_emptyStream_throwsArchiveException() throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
        new DumpArchiveInputStream(bais);
    }

    // Tests constructor with custom encoding parameter and invalid stream
    @Test(expected = ArchiveException.class)
    public void testConstructor_withEncoding_throwsArchiveExceptionOnEmpty() throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
        new DumpArchiveInputStream(bais, "UTF-8");
    }

    // Tests constructor when CLRI segment has invalid type
    @Test(expected = ArchiveException.class)
    public void testConstructor_invalidClriType_throwsArchiveException() throws Exception {
        byte[] header = createRecord(DumpArchiveConstants.SEGMENT_TYPE.HEADER.code, 1, 0, 0);
        byte[] notClri = createRecord(DumpArchiveConstants.SEGMENT_TYPE.BITS.code, 1, 0, 0);

        byte[] all = new byte[header.length + notClri.length];
        System.arraycopy(header, 0, all, 0, header.length);
        System.arraycopy(notClri, 0, all, header.length, notClri.length);

        ByteArrayInputStream bais = new ByteArrayInputStream(all);
        new DumpArchiveInputStream(bais);
    }

    // Tests constructor when BITS segment has invalid type
    @Test(expected = ArchiveException.class)
    public void testConstructor_invalidBitsType_throwsArchiveException() throws Exception {
        byte[] header = createRecord(DumpArchiveConstants.SEGMENT_TYPE.HEADER.code, 1, 0, 0);
        byte[] clri = createRecord(DumpArchiveConstants.SEGMENT_TYPE.CLRI.code, 1, 0, 0);
        byte[] notBits = createRecord(DumpArchiveConstants.SEGMENT_TYPE.HEADER.code, 1, 0, 0);

        byte[] all = new byte[DumpArchiveConstants.TP_SIZE * 3];
        System.arraycopy(header, 0, all, 0, DumpArchiveConstants.TP_SIZE);
        System.arraycopy(clri, 0, all, DumpArchiveConstants.TP_SIZE, DumpArchiveConstants.TP_SIZE);
        System.arraycopy(notBits, 0, all, DumpArchiveConstants.TP_SIZE * 2, DumpArchiveConstants.TP_SIZE);

        ByteArrayInputStream bais = new ByteArrayInputStream(all);
        new DumpArchiveInputStream(bais);
    }

    // Tests constructor with multiple CLRI and BITS count records
    @Test
    public void testConstructor_multipleClriAndBitsBlocks() throws Exception {
        byte[] header = createRecord(DumpArchiveConstants.SEGMENT_TYPE.HEADER.code, 1, 0, 0);
        byte[] clri1 = createRecord(DumpArchiveConstants.SEGMENT_TYPE.CLRI.code, 1, 2, 0);
        byte[] clri2 = createRecord(DumpArchiveConstants.SEGMENT_TYPE.CLRI.code, 1, 0, 0);
        byte[] bits1 = createRecord(DumpArchiveConstants.SEGMENT_TYPE.BITS.code, 1, 2, 0);
        byte[] bits2 = createRecord(DumpArchiveConstants.SEGMENT_TYPE.BITS.code, 1, 0, 0);

        byte[] all = new byte[DumpArchiveConstants.TP_SIZE * 5];
        System.arraycopy(header, 0, all, 0, DumpArchiveConstants.TP_SIZE);
        System.arraycopy(clri1, 0, all, DumpArchiveConstants.TP_SIZE, DumpArchiveConstants.TP_SIZE);
        System.arraycopy(clri2, 0, all, DumpArchiveConstants.TP_SIZE * 2, DumpArchiveConstants.TP_SIZE);
        System.arraycopy(bits1, 0, all, DumpArchiveConstants.TP_SIZE * 3, DumpArchiveConstants.TP_SIZE);
        System.arraycopy(bits2, 0, all, DumpArchiveConstants.TP_SIZE * 4, DumpArchiveConstants.TP_SIZE);

        ByteArrayInputStream bais = new ByteArrayInputStream(all);
        DumpArchiveInputStream in = new DumpArchiveInputStream(bais);
        assertNotNull(in.getSummary());
        in.close();
    }

    // Tests successful initialization and summary retrieval
    @Test
    public void testConstructor_validHeaderAndClriAndBits_success() throws Exception {
        byte[] header = createRecord(DumpArchiveConstants.SEGMENT_TYPE.HEADER.code, 1, 0, 0);
        byte[] clri = createRecord(DumpArchiveConstants.SEGMENT_TYPE.CLRI.code, 1, 0, 0);
        byte[] bits = createRecord(DumpArchiveConstants.SEGMENT_TYPE.BITS.code, 1, 0, 0);

        byte[] all = new byte[DumpArchiveConstants.TP_SIZE * 3];
        System.arraycopy(header, 0, all, 0, DumpArchiveConstants.TP_SIZE);
        System.arraycopy(clri, 0, all, DumpArchiveConstants.TP_SIZE, DumpArchiveConstants.TP_SIZE);
        System.arraycopy(bits, 0, all, DumpArchiveConstants.TP_SIZE * 2, DumpArchiveConstants.TP_SIZE);

        ByteArrayInputStream bais = new ByteArrayInputStream(all);
        DumpArchiveInputStream in = new DumpArchiveInputStream(bais);

        assertNotNull(in.getSummary());
        assertEquals(DumpArchiveConstants.TP_SIZE * 3, in.getBytesRead());
        assertEquals(DumpArchiveConstants.TP_SIZE * 3, in.getCount());

        in.close();
    }

    // Tests getNextEntry encountering END segment type returning null
    @Test
    public void testGetNextEntry_endSegment_returnsNull() throws Exception {
        byte[] header = createRecord(DumpArchiveConstants.SEGMENT_TYPE.HEADER.code, 1, 0, 0);
        byte[] clri = createRecord(DumpArchiveConstants.SEGMENT_TYPE.CLRI.code, 1, 0, 0);
        byte[] bits = createRecord(DumpArchiveConstants.SEGMENT_TYPE.BITS.code, 1, 0, 0);
        byte[] end = createRecord(DumpArchiveConstants.SEGMENT_TYPE.END.code, 1, 0, 0);

        byte[] all = new byte[DumpArchiveConstants.TP_SIZE * 4];
        System.arraycopy(header, 0, all, 0, DumpArchiveConstants.TP_SIZE);
        System.arraycopy(clri, 0, all, DumpArchiveConstants.TP_SIZE, DumpArchiveConstants.TP_SIZE);
        System.arraycopy(bits, 0, all, DumpArchiveConstants.TP_SIZE * 2, DumpArchiveConstants.TP_SIZE);
        System.arraycopy(end, 0, all, DumpArchiveConstants.TP_SIZE * 3, DumpArchiveConstants.TP_SIZE);

        ByteArrayInputStream bais = new ByteArrayInputStream(all);
        DumpArchiveInputStream in = new DumpArchiveInputStream(bais);

        assertNull(in.getNextDumpEntry());
        assertNull(in.getNextEntry());

        in.close();
    }

    // Tests read returns -1 after close or when no active entry is available
    @Test
    public void testRead_whenClosed_returnsNegativeOne() throws Exception {
        byte[] header = createRecord(DumpArchiveConstants.SEGMENT_TYPE.HEADER.code, 1, 0, 0);
        byte[] clri = createRecord(DumpArchiveConstants.SEGMENT_TYPE.CLRI.code, 1, 0, 0);
        byte[] bits = createRecord(DumpArchiveConstants.SEGMENT_TYPE.BITS.code, 1, 0, 0);

        byte[] all = new byte[DumpArchiveConstants.TP_SIZE * 3];
        System.arraycopy(header, 0, all, 0, DumpArchiveConstants.TP_SIZE);
        System.arraycopy(clri, 0, all, DumpArchiveConstants.TP_SIZE, DumpArchiveConstants.TP_SIZE);
        System.arraycopy(bits, 0, all, DumpArchiveConstants.TP_SIZE * 2, DumpArchiveConstants.TP_SIZE);

        ByteArrayInputStream bais = new ByteArrayInputStream(all);
        DumpArchiveInputStream in = new DumpArchiveInputStream(bais);

        in.close();
        byte[] buf = new byte[16];
        assertEquals(-1, in.read(buf, 0, buf.length));
    }

    // Tests read throws IllegalStateException when active entry is not initialized
    @Test(expected = IllegalStateException.class)
    public void testRead_noActiveEntry_throwsIllegalStateException() throws Exception {
        byte[] header = createRecord(DumpArchiveConstants.SEGMENT_TYPE.HEADER.code, 1, 0, 0);
        byte[] clri = createRecord(DumpArchiveConstants.SEGMENT_TYPE.CLRI.code, 1, 0, 0);
        byte[] bits = createRecord(DumpArchiveConstants.SEGMENT_TYPE.BITS.code, 1, 0, 0);

        byte[] all = new byte[DumpArchiveConstants.TP_SIZE * 3];
        System.arraycopy(header, 0, all, 0, DumpArchiveConstants.TP_SIZE);
        System.arraycopy(clri, 0, all, DumpArchiveConstants.TP_SIZE, DumpArchiveConstants.TP_SIZE);
        System.arraycopy(bits, 0, all, DumpArchiveConstants.TP_SIZE * 2, DumpArchiveConstants.TP_SIZE);

        ByteArrayInputStream bais = new ByteArrayInputStream(all);
        DumpArchiveInputStream in = new DumpArchiveInputStream(bais);

        byte[] buf = new byte[16];
        in.read(buf, 0, buf.length);
    }

    // Tests reading a regular file entry and its contents
    @Test
    public void testRead_regularFileEntry_readsData() throws Exception {
        byte[] header = createRecord(DumpArchiveConstants.SEGMENT_TYPE.HEADER.code, 1, 0, 0);
        byte[] clri = createRecord(DumpArchiveConstants.SEGMENT_TYPE.CLRI.code, 1, 0, 0);
        byte[] bits = createRecord(DumpArchiveConstants.SEGMENT_TYPE.BITS.code, 1, 0, 0);

        byte[] cAddr = new byte[512];
        cAddr[0] = 1; // 1 data block
        byte[] inode = createInodeRecord(5, 0100644, 10, 1, cAddr);

        byte[] dataBlock = new byte[DumpArchiveConstants.TP_SIZE];
        for (int i = 0; i < 10; i++) {
            dataBlock[i] = (byte) ('a' + i);
        }

        byte[] end = createRecord(DumpArchiveConstants.SEGMENT_TYPE.END.code, 1, 0, 0);

        byte[] all = new byte[DumpArchiveConstants.TP_SIZE * 5];
        System.arraycopy(header, 0, all, 0, DumpArchiveConstants.TP_SIZE);
        System.arraycopy(clri, 0, all, DumpArchiveConstants.TP_SIZE, DumpArchiveConstants.TP_SIZE);
        System.arraycopy(bits, 0, all, DumpArchiveConstants.TP_SIZE * 2, DumpArchiveConstants.TP_SIZE);
        System.arraycopy(inode, 0, all, DumpArchiveConstants.TP_SIZE * 3, DumpArchiveConstants.TP_SIZE);
        System.arraycopy(dataBlock, 0, all, DumpArchiveConstants.TP_SIZE * 4, DumpArchiveConstants.TP_SIZE);

        ByteArrayInputStream bais = new ByteArrayInputStream(all);
        DumpArchiveInputStream in = new DumpArchiveInputStream(bais);

        DumpArchiveEntry entry = in.getNextDumpEntry();
        assertNotNull(entry);
        assertEquals(5, entry.getIno());
        assertEquals(10, entry.getSize());

        // Test read with len == 0
        byte[] readBuf = new byte[16];
        assertEquals(0, in.read(readBuf, 0, 0));

        // Read all 10 bytes
        int readBytes = in.read(readBuf, 0, 10);
        assertEquals(10, readBytes);
        for (int i = 0; i < 10; i++) {
            assertEquals((byte) ('a' + i), readBuf[i]);
        }

        // Subsequent read returns -1 (EOF)
        assertEquals(-1, in.read(readBuf, 0, 10));

        in.close();
    }

    // Tests reading a sparse file (with hole)
    @Test
    public void testRead_sparseFileEntry_readsHoleAsZeros() throws Exception {
        byte[] header = createRecord(DumpArchiveConstants.SEGMENT_TYPE.HEADER.code, 1, 0, 0);
        byte[] clri = createRecord(DumpArchiveConstants.SEGMENT_TYPE.CLRI.code, 1, 0, 0);
        byte[] bits = createRecord(DumpArchiveConstants.SEGMENT_TYPE.BITS.code, 1, 0, 0);

        byte[] cAddr = new byte[512];
        cAddr[0] = 0; // hole block (no data block follows on tape)
        byte[] inode = createInodeRecord(6, 0100644, 5, 1, cAddr);

        byte[] all = new byte[DumpArchiveConstants.TP_SIZE * 4];
        System.arraycopy(header, 0, all, 0, DumpArchiveConstants.TP_SIZE);
        System.arraycopy(clri, 0, all, DumpArchiveConstants.TP_SIZE, DumpArchiveConstants.TP_SIZE);
        System.arraycopy(bits, 0, all, DumpArchiveConstants.TP_SIZE * 2, DumpArchiveConstants.TP_SIZE);
        System.arraycopy(inode, 0, all, DumpArchiveConstants.TP_SIZE * 3, DumpArchiveConstants.TP_SIZE);

        ByteArrayInputStream bais = new ByteArrayInputStream(all);
        DumpArchiveInputStream in = new DumpArchiveInputStream(bais);

        DumpArchiveEntry entry = in.getNextDumpEntry();
        assertNotNull(entry);

        byte[] readBuf = new byte[5];
        int readBytes = in.read(readBuf, 0, 5);
        assertEquals(5, readBytes);
        for (int i = 0; i < 5; i++) {
            assertEquals(0, readBuf[i]);
        }

        in.close();
    }

    // Tests read with invalid offset and length arguments
    @Test(expected = IllegalArgumentException.class)
    public void testRead_negativeLength_throwsIllegalArgumentException() throws Exception {
        byte[] header = createRecord(DumpArchiveConstants.SEGMENT_TYPE.HEADER.code, 1, 0, 0);
        byte[] clri = createRecord(DumpArchiveConstants.SEGMENT_TYPE.CLRI.code, 1, 0, 0);
        byte[] bits = createRecord(DumpArchiveConstants.SEGMENT_TYPE.BITS.code, 1, 0, 0);
        byte[] cAddr = new byte[512];
        cAddr[0] = 1;
        byte[] inode = createInodeRecord(7, 0100644, 10, 1, cAddr);
        byte[] dataBlock = new byte[DumpArchiveConstants.TP_SIZE];

        byte[] all = new byte[DumpArchiveConstants.TP_SIZE * 5];
        System.arraycopy(header, 0, all, 0, DumpArchiveConstants.TP_SIZE);
        System.arraycopy(clri, 0, all, DumpArchiveConstants.TP_SIZE, DumpArchiveConstants.TP_SIZE);
        System.arraycopy(bits, 0, all, DumpArchiveConstants.TP_SIZE * 2, DumpArchiveConstants.TP_SIZE);
        System.arraycopy(inode, 0, all, DumpArchiveConstants.TP_SIZE * 3, DumpArchiveConstants.TP_SIZE);
        System.arraycopy(dataBlock, 0, all, DumpArchiveConstants.TP_SIZE * 4, DumpArchiveConstants.TP_SIZE);

        ByteArrayInputStream bais = new ByteArrayInputStream(all);
        DumpArchiveInputStream in = new DumpArchiveInputStream(bais);
        in.getNextDumpEntry();

        byte[] buf = new byte[10];
        in.read(buf, 0, -1);
    }

    // Tests close is idempotent and can be called multiple times
    @Test
    public void testClose_multipleCalls_noError() throws Exception {
        byte[] header = createRecord(DumpArchiveConstants.SEGMENT_TYPE.HEADER.code, 1, 0, 0);
        byte[] clri = createRecord(DumpArchiveConstants.SEGMENT_TYPE.CLRI.code, 1, 0, 0);
        byte[] bits = createRecord(DumpArchiveConstants.SEGMENT_TYPE.BITS.code, 1, 0, 0);

        byte[] all = new byte[DumpArchiveConstants.TP_SIZE * 3];
        System.arraycopy(header, 0, all, 0, DumpArchiveConstants.TP_SIZE);
        System.arraycopy(clri, 0, all, DumpArchiveConstants.TP_SIZE, DumpArchiveConstants.TP_SIZE);
        System.arraycopy(bits, 0, all, DumpArchiveConstants.TP_SIZE * 2, DumpArchiveConstants.TP_SIZE);

        ByteArrayInputStream bais = new ByteArrayInputStream(all);
        DumpArchiveInputStream in = new DumpArchiveInputStream(bais);

        in.close();
        in.close();
    }
}