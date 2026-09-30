package org.apache.commons.compress.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Adler32;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import org.junit.Test;

public class ChecksumCalculatingInputStreamTest {

    // Tests constructor and getValue() with normal input
    @Test
    public void testRead_singleByte_normalInput_returnsCorrectChecksum() throws IOException {
        byte[] data = {1, 2, 3, 4, 5};
        Checksum checksum = new CRC32();
        InputStream is = new ByteArrayInputStream(data);
        ChecksumCalculatingInputStream calc = new ChecksumCalculatingInputStream(checksum, is);
        while (calc.read() != -1) {
        }
        Checksum expectedChecksum = new CRC32();
        expectedChecksum.update(data, 0, data.length);
        assertEquals(expectedChecksum.getValue(), calc.getValue());
    }

    // Tests read(byte[]) normal behavior
    @Test
    public void testRead_byteArray_normalInput_returnsCorrectChecksum() throws IOException {
        byte[] data = {10, 20, 30, 40, 50};
        Checksum checksum = new Adler32();
        InputStream is = new ByteArrayInputStream(data);
        ChecksumCalculatingInputStream calc = new ChecksumCalculatingInputStream(checksum, is);
        byte[] buf = new byte[3];
        while (calc.read(buf) != -1) {
        }
        Checksum expectedChecksum = new Adler32();
        expectedChecksum.update(data, 0, data.length);
        assertEquals(expectedChecksum.getValue(), calc.getValue());
    }

    // Tests read(byte[], int, int) normal behavior
    @Test
    public void testRead_byteArrayOffsetLen_normalInput_returnsCorrectChecksum() throws IOException {
        byte[] data = {100, (byte)200, 0, 0, 0, 50};
        Checksum checksum = new CRC32();
        InputStream is = new ByteArrayInputStream(data);
        ChecksumCalculatingInputStream calc = new ChecksumCalculatingInputStream(checksum, is);
        byte[] buf = new byte[10];
        calc.read(buf, 2, 3);
        calc.read(buf, 5, 3);
        Checksum expectedChecksum = new CRC32();
        expectedChecksum.update(data, 0, data.length);
        assertEquals(expectedChecksum.getValue(), calc.getValue());
    }

    // Tests read() returns -1 when stream is exhausted
    @Test
    public void testRead_singleByte_emptyStream_returnsMinusOne() throws IOException {
        Checksum checksum = new CRC32();
        InputStream is = new ByteArrayInputStream(new byte[0]);
        ChecksumCalculatingInputStream calc = new ChecksumCalculatingInputStream(checksum, is);
        assertEquals(-1, calc.read());
        assertEquals(0, calc.getValue());
    }

    // Tests read(byte[]) returns -1 when stream is exhausted
    @Test
    public void testRead_byteArray_emptyStream_returnsMinusOne() throws IOException {
        Checksum checksum = new Adler32();
        InputStream is = new ByteArrayInputStream(new byte[0]);
        ChecksumCalculatingInputStream calc = new ChecksumCalculatingInputStream(checksum, is);
        assertEquals(-1, calc.read(new byte[5]));
        assertEquals(0, calc.getValue());
    }

    // Tests read(byte[], int, int) returns -1 when stream is exhausted
    @Test
    public void testRead_byteArrayOffsetLen_emptyStream_returnsMinusOne() throws IOException {
        Checksum checksum = new CRC32();
        InputStream is = new ByteArrayInputStream(new byte[0]);
        ChecksumCalculatingInputStream calc = new ChecksumCalculatingInputStream(checksum, is);
        assertEquals(-1, calc.read(new byte[5], 0, 3));
        assertEquals(0, calc.getValue());
    }

    // Tests skip() returns 1 when data available
    @Test
    public void testSkip_dataAvailable_returnsOne() throws IOException {
        byte[] data = {1};
        Checksum checksum = new CRC32();
        InputStream is = new ByteArrayInputStream(data);
        ChecksumCalculatingInputStream calc = new ChecksumCalculatingInputStream(checksum, is);
        assertEquals(1, calc.skip(10));
        Checksum expectedChecksum = new CRC32();
        expectedChecksum.update(data[0]);
        assertEquals(expectedChecksum.getValue(), calc.getValue());
    }

    // Tests skip() returns 0 when no data available
    @Test
    public void testSkip_noDataAvailable_returnsZero() throws IOException {
        Checksum checksum = new CRC32();
        InputStream is = new ByteArrayInputStream(new byte[0]);
        ChecksumCalculatingInputStream calc = new ChecksumCalculatingInputStream(checksum, is);
        assertEquals(0, calc.skip(10));
        assertEquals(0, calc.getValue());
    }

    // Tests getValue() after read with multiple calls
    @Test
    public void testGetValue_afterReadMultipleCalls_returnsCorrectChecksum() throws IOException {
        byte[] data = {1, 2, 3};
        Checksum checksum = new Adler32();
        InputStream is = new ByteArrayInputStream(data);
        ChecksumCalculatingInputStream calc = new ChecksumCalculatingInputStream(checksum, is);
        calc.read();
        calc.read(new byte[2]);
        Checksum expectedChecksum = new Adler32();
        expectedChecksum.update(data, 0, data.length);
        assertEquals(expectedChecksum.getValue(), calc.getValue());
    }

    // Tests that checksum is correct after reading all bytes using different read methods
    @Test
    public void testRead_mixedReads_returnsCorrectChecksum() throws IOException {
        byte[] data = {1, 2, 3, 4, 5, 6, 7};
        Checksum checksum = new CRC32();
        InputStream is = new ByteArrayInputStream(data);
        ChecksumCalculatingInputStream calc = new ChecksumCalculatingInputStream(checksum, is);
        calc.read();
        calc.read(new byte[3], 0, 2);
        calc.read(new byte[4]);
        byte[] buf = new byte[10];
        int ret = calc.read(buf, 0, 2);
        while (ret != -1) {
            ret = calc.read(buf, 0, 2);
        }
        Checksum expectedChecksum = new CRC32();
        expectedChecksum.update(data, 0, data.length);
        assertEquals(expectedChecksum.getValue(), calc.getValue());
    }

    // Tests boundary: read(byte[]) with zero-length array
    @Test
    public void testRead_byteArray_zeroLength_returnsZero() throws IOException {
        byte[] data = {1, 2, 3};
        Checksum checksum = new CRC32();
        InputStream is = new ByteArrayInputStream(data);
        ChecksumCalculatingInputStream calc = new ChecksumCalculatingInputStream(checksum, is);
        // reading zero-length array returns 0 from InputStream.read(byte[])
        int ret = calc.read(new byte[0]);
        assertEquals(0, ret);
        // Data not read, checksum should be 0 so far
        assertEquals(0, calc.getValue());
    }

    // Tests boundary: read(byte[], off, len) with len=0
    @Test
    public void testRead_byteArrayOffsetLen_zeroLen_returnsZero() throws IOException {
        byte[] data = {1, 2, 3};
        Checksum checksum = new Adler32();
        InputStream is = new ByteArrayInputStream(data);
        ChecksumCalculatingInputStream calc = new ChecksumCalculatingInputStream(checksum, is);
        int ret = calc.read(new byte[5], 0, 0);
        assertEquals(0, ret);
        assertEquals(0, calc.getValue());
    }

    // Tests normal case: multiple calls to read() exhaust stream
    @Test
    public void testRead_singleByte_exhaustedStream_returnsMinusOne() throws IOException {
        Checksum checksum = new CRC32();
        InputStream is = new ByteArrayInputStream(new byte[]{50});
        ChecksumCalculatingInputStream calc = new ChecksumCalculatingInputStream(checksum, is);
        calc.read();
        assertEquals(-1, calc.read());
        assertEquals(50, calc.getValue());
    }

    // Tests read(byte[]) with large array
    @Test
    public void testRead_byteArray_largeBuffer_returnsCorrectChecksum() throws IOException {
        byte[] data = new byte[1000];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (i % 256);
        }
        Checksum checksum = new CRC32();
        InputStream is = new ByteArrayInputStream(data);
        ChecksumCalculatingInputStream calc = new ChecksumCalculatingInputStream(checksum, is);
        byte[] buf = new byte[500];
        while (calc.read(buf) != -1) {
        }
        Checksum expectedChecksum = new CRC32();
        expectedChecksum.update(data, 0, data.length);
        assertEquals(expectedChecksum.getValue(), calc.getValue());
    }
}