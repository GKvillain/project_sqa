package org.apache.commons.compress.compressors.deflate;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Deflater;

import org.junit.Test;

/**
 * JUnit 4 test class for DeflateCompressorInputStream.
 * Targets defect in count handling for read(byte[],int,int) when -1 is returned.
 */
public class DeflateCompressorInputStreamTest {

    // Helper: compress data with or without zlib header
    private static byte[] compress(byte[] input, boolean zlibHeader) {
        Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION, !zlibHeader);
        deflater.setInput(input);
        deflater.finish();
        ByteArrayOutputStream bos = new ByteArrayOutputStream(input.length);
        byte[] buf = new byte[1024];
        while (!deflater.finished()) {
            int len = deflater.deflate(buf);
            bos.write(buf, 0, len);
        }
        deflater.end();
        return bos.toByteArray();
    }

    // === existing tests ===

    @Test
    public void testRead_singleByteFromCompressedData_returnsCorrectByte() throws IOException {
        byte[] uncompressed = "hello".getBytes();
        byte[] compressed = compress(uncompressed, true);
        DeflateCompressorInputStream cis = new DeflateCompressorInputStream(new ByteArrayInputStream(compressed));
        int b = cis.read();
        assertTrue(b != -1);
        assertEquals(uncompressed[0], (byte) b);
        cis.close();
    }

    @Test
    public void testRead_singleByteAtEndOfStream_returnsMinusOne() throws IOException {
        byte[] uncompressed = "x".getBytes();
        byte[] compressed = compress(uncompressed, true);
        DeflateCompressorInputStream cis = new DeflateCompressorInputStream(new ByteArrayInputStream(compressed));
        cis.read();
        int b = cis.read();
        assertEquals(-1, b);
        cis.close();
    }

    @Test
    public void testRead_byteArrayFromCompressedData_returnsCorrectData() throws IOException {
        byte[] uncompressed = "hello world".getBytes();
        byte[] compressed = compress(uncompressed, true);
        DeflateCompressorInputStream cis = new DeflateCompressorInputStream(new ByteArrayInputStream(compressed));
        byte[] buf = new byte[20];
        int ret = cis.read(buf, 0, buf.length);
        assertEquals(uncompressed.length, ret);
        assertArrayEquals(uncompressed, java.util.Arrays.copyOf(buf, ret));
        cis.close();
    }

    @Test
    public void testRead_byteArrayAtEndOfStream_returnsMinusOneAndCountUnchanged() throws IOException {
        byte[] uncompressed = "abc".getBytes();
        byte[] compressed = compress(uncompressed, true);
        DeflateCompressorInputStream cis = new DeflateCompressorInputStream(new ByteArrayInputStream(compressed));
        while (cis.read() != -1) { }
        long beforeCount = cis.getBytesRead();
        assertEquals(3, beforeCount);
        byte[] buf = new byte[10];
        int ret = cis.read(buf, 0, buf.length);
        assertEquals(-1, ret);
        assertEquals(3, cis.getBytesRead());
        cis.close();
    }

    @Test
    public void testRead_byteArrayPartialRead_returnsCorrectCount() throws IOException {
        byte[] uncompressed = "testing partial read".getBytes();
        byte[] compressed = compress(uncompressed, true);
        DeflateCompressorInputStream cis = new DeflateCompressorInputStream(new ByteArrayInputStream(compressed));
        byte[] smallBuf = new byte[5];
        int total = 0;
        int ret;
        while ((ret = cis.read(smallBuf, 0, smallBuf.length)) != -1) {
            total += ret;
        }
        assertEquals(uncompressed.length, total);
        assertEquals(total, cis.getBytesRead());
        cis.close();
    }

    @Test
    public void testSkip_positiveAmount_skipsCorrectly() throws IOException {
        byte[] uncompressed = "skip this text".getBytes();
        byte[] compressed = compress(uncompressed, true);
        DeflateCompressorInputStream cis = new DeflateCompressorInputStream(new ByteArrayInputStream(compressed));
        long skipped = cis.skip(5);
        assertEquals(5, skipped);
        byte[] buf = new byte[100];
        int ret = cis.read(buf, 0, buf.length);
        assertEquals(uncompressed.length - 5, ret);
        assertEquals(uncompressed[5], buf[0]);
        cis.close();
    }

    @Test
    public void testSkip_zeroAmount_returnsZero() throws IOException {
        byte[] compressed = compress("test".getBytes(), true);
        DeflateCompressorInputStream cis = new DeflateCompressorInputStream(new ByteArrayInputStream(compressed));
        long skipped = cis.skip(0);
        assertEquals(0, skipped);
        cis.close();
    }

    @Test
    public void testSkip_negativeAmount_returnsZero() throws IOException {
        byte[] compressed = compress("test".getBytes(), true);
        DeflateCompressorInputStream cis = new DeflateCompressorInputStream(new ByteArrayInputStream(compressed));
        long skipped = cis.skip(-1);
        assertEquals(0, skipped);
        cis.close();
    }

    @Test
    public void testAvailable_beforeReading_returnsPositive() throws IOException {
        byte[] compressed = compress("available check".getBytes(), true);
        DeflateCompressorInputStream cis = new DeflateCompressorInputStream(new ByteArrayInputStream(compressed));
        int av = cis.available();
        assertTrue(av > 0);
        cis.close();
    }

    @Test
    public void testAvailable_afterReadingAll_returnsZero() throws IOException {
        byte[] compressed = compress("some".getBytes(), true);
        DeflateCompressorInputStream cis = new DeflateCompressorInputStream(new ByteArrayInputStream(compressed));
        while (cis.read() != -1) { }
        assertEquals(0, cis.available());
        cis.close();
    }

    @Test(expected = IOException.class)
    public void testRead_afterClose_throwsIOException() throws IOException {
        byte[] compressed = compress("close".getBytes(), true);
        DeflateCompressorInputStream cis = new DeflateCompressorInputStream(new ByteArrayInputStream(compressed));
        cis.close();
        cis.read();
    }

    @Test
    public void testConstructor_withDeflateParameters_noException() throws IOException {
        byte[] compressed = compress("constructor".getBytes(), true);
        DeflateParameters params = new DeflateParameters();
        DeflateCompressorInputStream cis = new DeflateCompressorInputStream(new ByteArrayInputStream(compressed), params);
        assertNotNull(cis);
        int b = cis.read();
        assertNotEquals(-1, b);
        cis.close();
    }

    @Test(expected = IOException.class)
    public void testRead_dataWithoutZlibHeader_throwsIOException() throws IOException {
        byte[] uncompressed = "no header".getBytes();
        byte[] compressedNoHeader = compress(uncompressed, false);
        DeflateCompressorInputStream cis = new DeflateCompressorInputStream(new ByteArrayInputStream(compressedNoHeader));
        cis.read();
        cis.close();
    }

    // ============ NEW TESTS ============

    // Test read(byte[],int,int) with len == 0 should return 0
    @Test
    public void testRead_byteArrayZeroLength_returnsZero() throws IOException {
        byte[] compressed = compress("test".getBytes(), true);
        DeflateCompressorInputStream cis = new DeflateCompressorInputStream(new ByteArrayInputStream(compressed));
        byte[] buf = new byte[10];
        long beforeCount = cis.getBytesRead();
        int ret = cis.read(buf, 0, 0);
        assertEquals(0, ret);
        assertEquals(beforeCount, cis.getBytesRead()); // count should not change
        cis.close();
    }

    // Test read(byte[],int,int) after close should throw IOException
    @Test(expected = IOException.class)
    public void testRead_byteArrayAfterClose_throwsIOException() throws IOException {
        byte[] compressed = compress("close".getBytes(), true);
        DeflateCompressorInputStream cis = new DeflateCompressorInputStream(new ByteArrayInputStream(compressed));
        cis.close();
        cis.read(new byte[10], 0, 10);
    }

    // Test read(byte[],int,int) with negative offset should throw IndexOutOfBoundsException
    @Test(expected = IndexOutOfBoundsException.class)
    public void testRead_byteArrayWithNegativeOffset_throwsIndexOutOfBounds() throws IOException {
        byte[] compressed = compress("test".getBytes(), true);
        DeflateCompressorInputStream cis = new DeflateCompressorInputStream(new ByteArrayInputStream(compressed));
        byte[] buf = new byte[10];
        cis.read(buf, -1, 5);
        cis.close();
    }

    // Test read(byte[],int,int) with negative length should throw IndexOutOfBoundsException
    @Test(expected = IndexOutOfBoundsException.class)
    public void testRead_byteArrayWithNegativeLength_throwsIndexOutOfBounds() throws IOException {
        byte[] compressed = compress("test".getBytes(), true);
        DeflateCompressorInputStream cis = new DeflateCompressorInputStream(new ByteArrayInputStream(compressed));
        byte[] buf = new byte[10];
        cis.read(buf, 0, -1);
        cis.close();
    }

    // Test read(byte[],int,int) with off+len > buf.length should throw IndexOutOfBoundsException
    @Test(expected = IndexOutOfBoundsException.class)
    public void testRead_byteArrayWithLengthTooLong_throwsIndexOutOfBounds() throws IOException {
        byte[] compressed = compress("test".getBytes(), true);
        DeflateCompressorInputStream cis = new DeflateCompressorInputStream(new ByteArrayInputStream(compressed));
        byte[] buf = new byte[5];
        cis.read(buf, 2, 4); // off+len = 6 > 5
        cis.close();
    }

    // Test skip with amount more than available data skips only available data
    @Test
    public void testSkip_moreThanAvailable_skipsOnlyAvailable() throws IOException {
        byte[] uncompressed = "abc".getBytes();
        byte[] compressed = compress(uncompressed, true);
        DeflateCompressorInputStream cis = new DeflateCompressorInputStream(new ByteArrayInputStream(compressed));
        long skipped = cis.skip(10);
        assertEquals(3, skipped); // only 3 bytes available
        assertEquals(3, cis.getBytesRead());
        int b = cis.read();
        assertEquals(-1, b);
        cis.close();
    }
}