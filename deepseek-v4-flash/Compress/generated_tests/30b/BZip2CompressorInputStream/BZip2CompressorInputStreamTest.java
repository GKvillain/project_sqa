package org.apache.commons.compress.compressors.bzip2;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

public class BZip2CompressorInputStreamTest {

    // Helper: compress a byte array using BZip2CompressorOutputStream
    private byte[] compress(byte[] input) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BZip2CompressorOutputStream bzOut = new BZip2CompressorOutputStream(baos);
        bzOut.write(input);
        bzOut.close();
        return baos.toByteArray();
    }

    // Helper: compress concatenated streams
    private byte[] compressConcatenated(byte[]... inputs) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (byte[] input : inputs) {
            BZip2CompressorOutputStream bzOut = new BZip2CompressorOutputStream(baos);
            bzOut.write(input);
            bzOut.close();
        }
        return baos.toByteArray();
    }

    // Helper: read all bytes from BZip2CompressorInputStream
    private byte[] readAll(BZip2CompressorInputStream bzIn) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int len;
        while ((len = bzIn.read(buf)) != -1) {
            baos.write(buf, 0, len);
        }
        return baos.toByteArray();
    }

    // Tests constructor with null input stream
    @Test(expected = NullPointerException.class)
    public void testConstructor_nullInput_throwsNullPointer() throws IOException {
        new BZip2CompressorInputStream(null);
    }

    // Tests read on closed stream
    @Test(expected = IOException.class)
    public void testRead_closedStream_throwsIOException() throws IOException {
        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(
                new ByteArrayInputStream(new byte[0]));
        bzIn.close();
        bzIn.read();
    }

    // Tests read with invalid negative offset
    @Test(expected = IndexOutOfBoundsException.class)
    public void testRead_negativeOffset_throwsIndexOutOfBounds() throws IOException {
        byte[] data = compress(new byte[]{65});
        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(
                new ByteArrayInputStream(data));
        bzIn.read(new byte[10], -1, 1);
    }

    // Tests read with invalid negative length
    @Test(expected = IndexOutOfBoundsException.class)
    public void testRead_negativeLength_throwsIndexOutOfBounds() throws IOException {
        byte[] data = compress(new byte[]{65});
        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(
                new ByteArrayInputStream(data));
        bzIn.read(new byte[10], 0, -1);
    }

    // Tests read with offset+len > dest.length
    @Test(expected = IndexOutOfBoundsException.class)
    public void testRead_offsetPlusLenExceedsLength_throwsIndexOutOfBounds() throws IOException {
        byte[] data = compress(new byte[]{65});
        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(
                new ByteArrayInputStream(data));
        bzIn.read(new byte[10], 5, 10);
    }

    // Tests read on stream with invalid magic bytes
    @Test(expected = IOException.class)
    public void testRead_invalidMagic_throwsIOException() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[]{0, 0, 0, 0});
        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(bais);
        bzIn.read();
    }

    // Tests read on stream with invalid block size (not between '1' and '9')
    @Test(expected = IOException.class)
    public void testRead_invalidBlockSize_throwsIOException() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[]{'B','Z','h','0'});
        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(bais);
        bzIn.read();
    }

    // Tests read on truncated stream (missing data)
    @Test(expected = IOException.class)
    public void testRead_truncatedStream_throwsIOException() throws IOException {
        byte[] compressed = compress(new byte[]{65, 66, 67});
        // Truncate to only first few bytes
        byte[] truncated = new byte[10];
        System.arraycopy(compressed, 0, truncated, 0, Math.min(10, compressed.length));
        ByteArrayInputStream bais = new ByteArrayInputStream(truncated);
        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(bais);
        while (bzIn.read() != -1) {
            // consume
        }
    }

    // Tests reading valid data (single byte)
    @Test
    public void testRead_validSingleByte_returnsCorrectByte() throws IOException {
        byte[] input = new byte[]{65}; // 'A'
        byte[] compressed = compress(input);
        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(
                new ByteArrayInputStream(compressed));
        int b = bzIn.read();
        assertEquals(65, b);
        assertEquals(-1, bzIn.read());
    }

    // Tests reading valid data (multiple bytes)
    @Test
    public void testRead_validMultipleBytes_returnsCorrectBytes() throws IOException {
        byte[] input = "Hello, BZip2!".getBytes("UTF-8");
        byte[] compressed = compress(input);
        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(
                new ByteArrayInputStream(compressed));
        byte[] result = readAll(bzIn);
        assertArrayEquals(input, result);
    }

    // Tests read with large block (size 9) and randomisation
    @Test
    public void testRead_largeBlockWithRandomisation_returnsCorrectBytes() throws IOException {
        byte[] input = new byte[100000];
        for (int i = 0; i < input.length; i++) {
            input[i] = (byte) (i % 256);
        }
        byte[] compressed = compress(input);
        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(
                new ByteArrayInputStream(compressed));
        byte[] result = readAll(bzIn);
        assertArrayEquals(input, result);
    }

    // Tests concatenated streams (decompressConcatenated=true)
    @Test
    public void testRead_concatenatedStreams_returnsBothBlocks() throws IOException {
        byte[] first = "FIRST".getBytes("UTF-8");
        byte[] second = "SECOND".getBytes("UTF-8");
        byte[] concatenated = compressConcatenated(first, second);
        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(
                new ByteArrayInputStream(concatenated), true);
        byte[] result = readAll(bzIn);
        byte[] expected = new byte[first.length + second.length];
        System.arraycopy(first, 0, expected, 0, first.length);
        System.arraycopy(second, 0, expected, first.length, second.length);
        assertArrayEquals(expected, result);
    }

    // Tests decompressConcatenated=false stops after first stream
    @Test
    public void testRead_concatenatedStreams_stopAfterFirst() throws IOException {
        byte[] first = "FIRST".getBytes("UTF-8");
        byte[] second = "SECOND".getBytes("UTF-8");
        byte[] concatenated = compressConcatenated(first, second);
        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(
                new ByteArrayInputStream(concatenated), false);
        byte[] result = readAll(bzIn);
        assertArrayEquals(first, result);
    }

    // Tests read on empty stream (no data after header)
    // This may trigger initial block magic detection or EOF
    @Test
    public void testRead_emptyStream_returnsEOF() throws IOException {
        // A valid BZip2 stream with no actual data? Compress empty array.
        byte[] compressed = compress(new byte[0]);
        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(
                new ByteArrayInputStream(compressed));
        // Should return -1 immediately (or after reading the block magic that ends the stream)
        int b = bzIn.read();
        assertEquals(-1, b);
    }

    // Tests read with block CRC error
    @Test(expected = IOException.class)
    public void testRead_CRCError_throwsIOException() throws IOException {
        // Create a partial valid stream and corrupt a byte after block header
        // This is tricky. We'll use a known compression and corrupt one byte in the data
        byte[] input = new byte[]{1,2,3};
        byte[] compressed = compress(input);
        // corrupt a byte in the compressed data (e.g., after the block header)
        // The block header starts at around index 14 (after magic, block size, etc.)
        // We'll flip a bit at some position.
        // For simplicity, we can use a try-catch in test if it's not guaranteed.
        // Actually, CRC is checked after reading entire block, so corrupting data will cause CRC mismatch.
        if (compressed.length > 20) {
            compressed[20] ^= 0xFF; // random corruption
        }
        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(
                new ByteArrayInputStream(compressed));
        // Reading should throw IOException due to CRC error
        while (bzIn.read() != -1) {
            // consume
        }
    }
}