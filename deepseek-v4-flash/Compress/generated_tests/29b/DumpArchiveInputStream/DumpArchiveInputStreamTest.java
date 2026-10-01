package org.apache.commons.compress.archivers.dump;

import org.apache.commons.compress.archivers.ArchiveException;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class DumpArchiveInputStreamTest {

    private static final int TP_SIZE = DumpArchiveConstants.TP_SIZE;
    private static final int NFS_MAGIC = DumpArchiveConstants.NFS_MAGIC;
    private static final int NT_REC = 1;

    /*
     * Creates a 1024-byte dump header with a valid checksum.
     * The checksum is built so that the sum of all 32-bit words equals the
     * stored checksum (0), which satisfies DumpArchiveUtil#verify.
     */
    private static byte[] createValidHeader() {
        byte[] buffer = new byte[TP_SIZE];
        // ntrec at offset 20
        putInt(buffer, 20, NT_REC);
        // magic at offset 24
        putInt(buffer, 24, NFS_MAGIC);
        // checksum at offset 28
        putInt(buffer, 28, 0);
        // cancel magic and ntrec to keep the checksum consistent
        putInt(buffer, 0, -NFS_MAGIC - NT_REC);
        return buffer;
    }

    private static void putInt(byte[] buffer, int offset, int value) {
        ByteBuffer.wrap(buffer).order(ByteOrder.BIG_ENDIAN).putInt(offset, value);
    }

    // Tests: empty buffer is not a match
    @Test
    public void testMatches_emptyBuffer_returnsFalse() {
        assertFalse(DumpArchiveInputStream.matches(new byte[0], 0));
    }

    // Tests: short buffer with correct magic is accepted
    @Test
    public void testMatches_shortBufferWithMagic_returnsTrue() {
        byte[] buffer = new byte[32];
        putInt(buffer, 24, NFS_MAGIC);
        assertTrue(DumpArchiveInputStream.matches(buffer, 32));
    }

    // Tests: short buffer without magic is rejected
    @Test
    public void testMatches_shortBufferWithoutMagic_returnsFalse() {
        byte[] buffer = new byte[32];
        putInt(buffer, 24, 0);
        assertFalse(DumpArchiveInputStream.matches(buffer, 32));
    }

    // Tests: full buffer with valid header is accepted
    @Test
    public void testMatches_fullBufferWithValidHeader_returnsTrue() {
        byte[] buffer = createValidHeader();
        assertTrue(DumpArchiveInputStream.matches(buffer, buffer.length));
    }

    // Tests: full buffer with invalid header is rejected
    @Test
    public void testMatches_fullBufferWithInvalidHeader_returnsFalse() {
        byte[] buffer = new byte[TP_SIZE];
        assertFalse(DumpArchiveInputStream.matches(buffer, buffer.length));
    }

    // Tests: invalid header throws ArchiveException from constructor
    @Test(expected = ArchiveException.class)
    public void testConstructor_invalidHeader_throwsArchiveException() throws Exception {
        InputStream in = new ByteArrayInputStream(new byte[TP_SIZE]);
        new DumpArchiveInputStream(in);
    }

    // Tests: empty input throws ArchiveException from constructor
    @Test(expected = ArchiveException.class)
    public void testConstructor_emptyInput_throwsArchiveException() throws Exception {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        new DumpArchiveInputStream(in);
    }

    // Tests: null input causes NullPointerException
    @Test(expected = NullPointerException.class)
    public void testConstructor_nullInput_throwsNullPointerException() throws Exception {
        new DumpArchiveInputStream((InputStream) null);
    }

    // New test cases for uncovered code

    // Tests: constructor with valid header and null encoding (should not throw)
    @Test
    public void testConstructor_validHeader_nullEncoding() throws Exception {
        byte[] header = createValidHeader();
        // Extend header to have enough data for first INODE (beyond TP_SIZE)
        byte[] fullData = new byte[TP_SIZE * 2];
        System.arraycopy(header, 0, fullData, 0, TP_SIZE);
        // Set a dummy inode at offset TP_SIZE
        putInt(fullData, TP_SIZE, 0); // dummy data
        InputStream in = new ByteArrayInputStream(fullData);
        DumpArchiveInputStream dis = new DumpArchiveInputStream(in, null);
        assertNotNull(dis);
        dis.close();
    }

    // Tests: constructor with valid header and specific encoding
    @Test
    public void testConstructor_validHeader_withEncoding() throws Exception {
        byte[] header = createValidHeader();
        byte[] fullData = new byte[TP_SIZE * 2];
        System.arraycopy(header, 0, fullData, 0, TP_SIZE);
        putInt(fullData, TP_SIZE, 0);
        InputStream in = new ByteArrayInputStream(fullData);
        DumpArchiveInputStream dis = new DumpArchiveInputStream(in, "UTF-8");
        assertNotNull(dis);
        dis.close();
    }

    // Tests: constructor with valid header and empty encoding string
    @Test(expected = ArchiveException.class)
    public void testConstructor_validHeader_emptyEncoding() throws Exception {
        byte[] header = createValidHeader();
        byte[] fullData = new byte[TP_SIZE * 2];
        System.arraycopy(header, 0, fullData, 0, TP_SIZE);
        putInt(fullData, TP_SIZE, 0);
        InputStream in = new ByteArrayInputStream(fullData);
        new DumpArchiveInputStream(in, "");
    }

    // Tests: matches with buffer length exactly equal to TP_SIZE
    @Test
    public void testMatches_exactTPsize_validHeader() {
        byte[] buffer = createValidHeader();
        assertTrue(DumpArchiveInputStream.matches(buffer, TP_SIZE));
    }

    // Tests: matches with buffer length greater than TP_SIZE
    @Test
    public void testMatches_largerThanTPsize_validHeader() {
        byte[] buffer = new byte[TP_SIZE + 100];
        byte[] header = createValidHeader();
        System.arraycopy(header, 0, buffer, 0, TP_SIZE);
        assertTrue(DumpArchiveInputStream.matches(buffer, buffer.length));
    }

    // Tests: matches with buffer length less than 32 (minimum required for magic check)
    @Test
    public void testMatches_bufferLengthLessThan32_returnsFalse() {
        byte[] buffer = new byte[20];
        assertFalse(DumpArchiveInputStream.matches(buffer, 20));
    }

    // Tests: verify checksum calculation with DumpArchiveUtil.verify
    @Test
    public void testVerifyChecksum_validHeader() throws Exception {
        byte[] header = createValidHeader();
        assertTrue("Checksum should be valid", DumpArchiveUtil.verify(header));
    }

    // Tests: verify checksum with modified header (invalid)
    @Test
    public void testVerifyChecksum_invalidHeader() {
        byte[] buffer = new byte[TP_SIZE];
        putInt(buffer, 24, NFS_MAGIC);
        putInt(buffer, 20, 1);
        // Not setting proper checksum
        assertFalse("Checksum should be invalid", DumpArchiveUtil.verify(buffer));
    }
}