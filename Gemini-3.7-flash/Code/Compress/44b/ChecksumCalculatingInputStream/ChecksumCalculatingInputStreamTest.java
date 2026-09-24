package org.apache.commons.compress.utils;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.zip.CRC32;

public class ChecksumCalculatingInputStreamTest {

    // Tests reading a single byte from stream and updating checksum
    @Test
    public void testRead_singleByte_updatesChecksumAndReturnsByte() throws IOException {
        byte[] data = new byte[] { 1, 2, 3 };
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        CRC32 checksum = new CRC32();
        ChecksumCalculatingInputStream stream = new ChecksumCalculatingInputStream(checksum, bais);

        int firstByte = stream.read();
        assertEquals(1, firstByte);

        CRC32 expectedChecksum = new CRC32();
        expectedChecksum.update(1);
        assertEquals(expectedChecksum.getValue(), stream.getValue());
    }

    // Tests read single byte at EOF returns -1
    @Test
    public void testRead_endOfStream_returnsMinusOne() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
        CRC32 checksum = new CRC32();
        ChecksumCalculatingInputStream stream = new ChecksumCalculatingInputStream(checksum, bais);

        int result = stream.read();
        assertEquals(-1, result);
        assertEquals(0L, stream.getValue());
    }

    // Tests reading byte array buffer
    @Test
    public void testRead_byteArray_updatesChecksumAndReturnsCount() throws IOException {
        byte[] data = new byte[] { 10, 20, 30, 40 };
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        CRC32 checksum = new CRC32();
        ChecksumCalculatingInputStream stream = new ChecksumCalculatingInputStream(checksum, bais);

        byte[] buffer = new byte[4];
        int bytesRead = stream.read(buffer);

        assertEquals(4, bytesRead);
        assertArrayEquals(data, buffer);

        CRC32 expectedChecksum = new CRC32();
        expectedChecksum.update(data, 0, data.length);
        assertEquals(expectedChecksum.getValue(), stream.getValue());
    }

    // Tests reading byte array at EOF returns -1
    @Test
    public void testRead_byteArrayAtEof_returnsMinusOne() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
        CRC32 checksum = new CRC32();
        ChecksumCalculatingInputStream stream = new ChecksumCalculatingInputStream(checksum, bais);

        byte[] buffer = new byte[4];
        int bytesRead = stream.read(buffer);

        assertEquals(-1, bytesRead);
    }

    // Tests reading with offset and length
    @Test
    public void testRead_withOffsetAndLength_updatesChecksumAndReturnsCount() throws IOException {
        byte[] data = new byte[] { 'a', 'b', 'c', 'd', 'e' };
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        CRC32 checksum = new CRC32();
        ChecksumCalculatingInputStream stream = new ChecksumCalculatingInputStream(checksum, bais);

        byte[] buffer = new byte[10];
        int bytesRead = stream.read(buffer, 2, 3);

        assertEquals(3, bytesRead);
        assertEquals('a', buffer[2]);
        assertEquals('b', buffer[3]);
        assertEquals('c', buffer[4]);

        CRC32 expectedChecksum = new CRC32();
        expectedChecksum.update(data, 0, 3);
        assertEquals(expectedChecksum.getValue(), stream.getValue());
    }

    // Tests reading with offset and length at EOF returns -1
    @Test
    public void testRead_withOffsetAndLengthAtEof_returnsMinusOne() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
        CRC32 checksum = new CRC32();
        ChecksumCalculatingInputStream stream = new ChecksumCalculatingInputStream(checksum, bais);

        byte[] buffer = new byte[10];
        int bytesRead = stream.read(buffer, 0, 5);

        assertEquals(-1, bytesRead);
    }

    // Tests skip when stream has remaining data
    @Test
    public void testSkip_hasRemainingData_returnsOneAndCalculatesChecksum() throws IOException {
        byte[] data = new byte[] { 65, 66, 67 };
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        CRC32 checksum = new CRC32();
        ChecksumCalculatingInputStream stream = new ChecksumCalculatingInputStream(checksum, bais);

        long skipped = stream.skip(5);
        assertEquals(1L, skipped);

        CRC32 expectedChecksum = new CRC32();
        expectedChecksum.update(65);
        assertEquals(expectedChecksum.getValue(), stream.getValue());
    }

    // Tests skip when stream is exhausted
    @Test
    public void testSkip_atEndOfStream_returnsZero() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
        CRC32 checksum = new CRC32();
        ChecksumCalculatingInputStream stream = new ChecksumCalculatingInputStream(checksum, bais);

        long skipped = stream.skip(1);
        assertEquals(0L, skipped);
        assertEquals(0L, stream.getValue());
    }

    // Tests getValue matches expected checksum value
    @Test
    public void testGetValue_emptyStream_returnsInitialChecksumValue() {
        CRC32 checksum = new CRC32();
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
        ChecksumCalculatingInputStream stream = new ChecksumCalculatingInputStream(checksum, bais);

        assertEquals(0L, stream.getValue());
    }

    // Tests constructor with null checksum throws NullPointerException
    @Test(expected = NullPointerException.class)
    public void testConstructor_nullChecksum_throwsNullPointerException() {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
        new ChecksumCalculatingInputStream(null, bais);
    }

    // Tests constructor with null inputStream throws NullPointerException
    @Test(expected = NullPointerException.class)
    public void testConstructor_nullInputStream_throwsNullPointerException() {
        CRC32 checksum = new CRC32();
        new ChecksumCalculatingInputStream(checksum, null);
    }
}