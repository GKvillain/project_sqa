package org.apache.commons.compress.compressors.bzip2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

public class BZip2CompressorInputStreamTest {

    // An empty BZip2 stream (header + EOS magic + CRC)
    private static final byte[] EMPTY_BZIP2_STREAM = new byte[] {
        'B', 'Z', 'h', '9',
        0x17, 0x72, 0x45, 0x38, 0x50, (byte) 0x90,
        0x00, 0x00, 0x00, 0x00
    };

    // Helper to compress data using BZip2CompressorOutputStream
    private static byte[] compress(byte[] data) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BZip2CompressorOutputStream bzOut = new BZip2CompressorOutputStream(baos);
        bzOut.write(data);
        bzOut.close();
        return baos.toByteArray();
    }

    // Tests matches method with valid signatures
    @Test
    public void testMatches_validSignature_returnsTrue() {
        byte[] sig = new byte[] { 'B', 'Z', 'h', '9' };
        assertTrue(BZip2CompressorInputStream.matches(sig, 3));
        assertTrue(BZip2CompressorInputStream.matches(sig, 4));
    }

    // Tests matches method with short length
    @Test
    public void testMatches_shortLength_returnsFalse() {
        byte[] sig = new byte[] { 'B', 'Z', 'h' };
        assertFalse(BZip2CompressorInputStream.matches(sig, 2));
        assertFalse(BZip2CompressorInputStream.matches(sig, 0));
        assertFalse(BZip2CompressorInputStream.matches(sig, -1));
    }

    // Tests matches method with invalid magic bytes
    @Test
    public void testMatches_invalidSignature_returnsFalse() {
        assertFalse(BZip2CompressorInputStream.matches(new byte[] { 'A', 'Z', 'h' }, 3));
        assertFalse(BZip2CompressorInputStream.matches(new byte[] { 'B', 'A', 'h' }, 3));
        assertFalse(BZip2CompressorInputStream.matches(new byte[] { 'B', 'Z', 'a' }, 3));
    }

    // Tests constructor with null InputStream
    @Test(expected = IOException.class)
    public void testConstructor_nullInputStream_throwsIOException() throws IOException {
        new BZip2CompressorInputStream(null);
    }

    // Tests constructor with empty stream
    @Test(expected = IOException.class)
    public void testConstructor_emptyStream_throwsIOException() throws IOException {
        new BZip2CompressorInputStream(new ByteArrayInputStream(new byte[0]));
    }

    // Tests constructor with invalid header magic
    @Test(expected = IOException.class)
    public void testConstructor_invalidHeader_throwsIOException() throws IOException {
        byte[] invalidHeader = new byte[] { 'B', 'Z', 'x', '9' };
        new BZip2CompressorInputStream(new ByteArrayInputStream(invalidHeader));
    }

    // Tests constructor with invalid block size character
    @Test(expected = IOException.class)
    public void testConstructor_invalidBlockSize_throwsIOException() throws IOException {
        byte[] invalidBlockSize = new byte[] { 'B', 'Z', 'h', '0' };
        new BZip2CompressorInputStream(new ByteArrayInputStream(invalidBlockSize));
    }

    // Tests constructor with bad block header magic
    @Test(expected = IOException.class)
    public void testConstructor_badBlockHeader_throwsIOException() throws IOException {
        byte[] badBlock = new byte[] {
            'B', 'Z', 'h', '9',
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00
        };
        new BZip2CompressorInputStream(new ByteArrayInputStream(badBlock));
    }

    // Tests constructor with corrupted CRC in stream
    @Test(expected = IOException.class)
    public void testConstructor_badCrc_throwsIOException() throws IOException {
        byte[] badCrcStream = new byte[] {
            'B', 'Z', 'h', '9',
            0x17, 0x72, 0x45, 0x38, 0x50, (byte) 0x90,
            0x01, 0x02, 0x03, 0x04
        };
        new BZip2CompressorInputStream(new ByteArrayInputStream(badCrcStream));
    }

    // Tests reading from empty BZip2 stream returns EOF
    @Test
    public void testRead_emptyStream_returnsMinusOne() throws IOException {
        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(
            new ByteArrayInputStream(EMPTY_BZIP2_STREAM)
        );
        assertEquals(-1, bzIn.read());
        assertEquals(-1, bzIn.read());
        bzIn.close();
    }

    // Tests read byte array from empty BZip2 stream
    @Test
    public void testReadByteArray_emptyStream_returnsMinusOne() throws IOException {
        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(
            new ByteArrayInputStream(EMPTY_BZIP2_STREAM)
        );
        byte[] buf = new byte[10];
        assertEquals(-1, bzIn.read(buf, 0, buf.length));
        bzIn.close();
    }

    // Tests read with invalid offset
    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadByteArray_negativeOffset_throwsException() throws IOException {
        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(
            new ByteArrayInputStream(EMPTY_BZIP2_STREAM)
        );
        try {
            bzIn.read(new byte[10], -1, 5);
        } finally {
            bzIn.close();
        }
    }

    // Tests read with negative length
    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadByteArray_negativeLength_throwsException() throws IOException {
        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(
            new ByteArrayInputStream(EMPTY_BZIP2_STREAM)
        );
        try {
            bzIn.read(new byte[10], 0, -1);
        } finally {
            bzIn.close();
        }
    }

    // Tests read with offset plus length exceeding buffer length
    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadByteArray_bufferOverflow_throwsException() throws IOException {
        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(
            new ByteArrayInputStream(EMPTY_BZIP2_STREAM)
        );
        try {
            bzIn.read(new byte[10], 5, 6);
        } finally {
            bzIn.close();
        }
    }

    // Tests read after stream closed throws IOException
    @Test(expected = IOException.class)
    public void testRead_streamClosed_throwsIOException() throws IOException {
        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(
            new ByteArrayInputStream(EMPTY_BZIP2_STREAM)
        );
        bzIn.close();
        bzIn.read();
    }

    // Tests read byte array after stream closed throws IOException
    @Test(expected = IOException.class)
    public void testReadByteArray_streamClosed_throwsIOException() throws IOException {
        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(
            new ByteArrayInputStream(EMPTY_BZIP2_STREAM)
        );
        bzIn.close();
        byte[] buf = new byte[10];
        bzIn.read(buf, 0, buf.length);
    }

    // Tests close multiple times does not fail
    @Test
    public void testClose_multipleTimes_success() throws IOException {
        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(
            new ByteArrayInputStream(EMPTY_BZIP2_STREAM)
        );
        bzIn.close();
        bzIn.close();
    }

    // Tests concatenated empty streams with decompressConcatenated = true
    @Test
    public void testDecompressConcatenated_validStreams_success() throws IOException {
        byte[] concatStream = new byte[EMPTY_BZIP2_STREAM.length * 2];
        System.arraycopy(EMPTY_BZIP2_STREAM, 0, concatStream, 0, EMPTY_BZIP2_STREAM.length);
        System.arraycopy(EMPTY_BZIP2_STREAM, 0, concatStream, EMPTY_BZIP2_STREAM.length, EMPTY_BZIP2_STREAM.length);

        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(
            new ByteArrayInputStream(concatStream), true
        );
        assertEquals(-1, bzIn.read());
        bzIn.close();
    }

    // Tests concatenated streams with trailing garbage throws IOException
    @Test(expected = IOException.class)
    public void testDecompressConcatenated_trailingGarbage_throwsIOException() throws IOException {
        byte[] streamWithGarbage = new byte[EMPTY_BZIP2_STREAM.length + 4];
        System.arraycopy(EMPTY_BZIP2_STREAM, 0, streamWithGarbage, 0, EMPTY_BZIP2_STREAM.length);
        streamWithGarbage[EMPTY_BZIP2_STREAM.length] = 'G';
        streamWithGarbage[EMPTY_BZIP2_STREAM.length + 1] = 'A';
        streamWithGarbage[EMPTY_BZIP2_STREAM.length + 2] = 'R';
        streamWithGarbage[EMPTY_BZIP2_STREAM.length + 3] = 'B';

        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(
            new ByteArrayInputStream(streamWithGarbage), true
        );
        try {
            bzIn.read();
        } finally {
            bzIn.close();
        }
    }

    // Tests read with zero length returns zero
    @Test
    public void testReadByteArray_zeroLength_returnsZero() throws IOException {
        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(
            new ByteArrayInputStream(EMPTY_BZIP2_STREAM)
        );
        byte[] buf = new byte[10];
        assertEquals(0, bzIn.read(buf, 0, 0));
        bzIn.close();
    }

    // Tests decompressing a small byte array byte-by-byte and in chunks
    @Test
    public void testDecompress_smallData_matchesOriginal() throws IOException {
        byte[] original = "Hello World! Testing BZip2 decompression.".getBytes("UTF-8");
        byte[] compressed = compress(original);

        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(new ByteArrayInputStream(compressed));
        ByteArrayOutputStream decompressed = new ByteArrayOutputStream();
        int b;
        while ((b = bzIn.read()) != -1) {
            decompressed.write(b);
        }
        bzIn.close();

        assertArrayEquals(original, decompressed.toByteArray());
    }

    // Tests decompressing with read(byte[], int, int)
    @Test
    public void testDecompress_bufferRead_matchesOriginal() throws IOException {
        byte[] original = new byte[1024];
        new Random(42).nextBytes(original);
        byte[] compressed = compress(original);

        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(new ByteArrayInputStream(compressed));
        ByteArrayOutputStream decompressed = new ByteArrayOutputStream();
        byte[] buffer = new byte[128];
        int read;
        while ((read = bzIn.read(buffer, 0, buffer.length)) != -1) {
            decompressed.write(buffer, 0, read);
        }
        bzIn.close();

        assertArrayEquals(original, decompressed.toByteArray());
    }

    // Tests decompressing highly repetitive data to test RLE stage
    @Test
    public void testDecompress_rleData_matchesOriginal() throws IOException {
        byte[] original = new byte[5000];
        Arrays.fill(original, (byte) 'A');
        byte[] compressed = compress(original);

        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(new ByteArrayInputStream(compressed));
        ByteArrayOutputStream decompressed = new ByteArrayOutputStream();
        byte[] buffer = new byte[256];
        int read;
        while ((read = bzIn.read(buffer)) != -1) {
            decompressed.write(buffer, 0, read);
        }
        bzIn.close();

        assertArrayEquals(original, decompressed.toByteArray());
    }

    // Tests decompressing multiple concatenated streams containing actual data
    @Test
    public void testDecompressConcatenated_multipleDataStreams_matchesOriginal() throws IOException {
        byte[] data1 = "First stream data. ".getBytes("UTF-8");
        byte[] data2 = "Second stream data with different content.".getBytes("UTF-8");
        byte[] comp1 = compress(data1);
        byte[] comp2 = compress(data2);

        ByteArrayOutputStream combined = new ByteArrayOutputStream();
        combined.write(comp1);
        combined.write(comp2);

        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(
            new ByteArrayInputStream(combined.toByteArray()), true
        );
        ByteArrayOutputStream decompressed = new ByteArrayOutputStream();
        byte[] buffer = new byte[64];
        int read;
        while ((read = bzIn.read(buffer, 0, buffer.length)) != -1) {
            decompressed.write(buffer, 0, read);
        }
        bzIn.close();

        ByteArrayOutputStream expected = new ByteArrayOutputStream();
        expected.write(data1);
        expected.write(data2);
        assertArrayEquals(expected.toByteArray(), decompressed.toByteArray());
    }

    // Tests decompressConcatenated = false ignores concatenated second stream
    @Test
    public void testDecompressConcatenated_false_stopsAfterFirstStream() throws IOException {
        byte[] data1 = "First stream data.".getBytes("UTF-8");
        byte[] data2 = "Second stream data.".getBytes("UTF-8");
        byte[] comp1 = compress(data1);
        byte[] comp2 = compress(data2);

        ByteArrayOutputStream combined = new ByteArrayOutputStream();
        combined.write(comp1);
        combined.write(comp2);

        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(
            new ByteArrayInputStream(combined.toByteArray()), false
        );
        ByteArrayOutputStream decompressed = new ByteArrayOutputStream();
        byte[] buffer = new byte[64];
        int read;
        while ((read = bzIn.read(buffer, 0, buffer.length)) != -1) {
            decompressed.write(buffer, 0, read);
        }
        bzIn.close();

        assertArrayEquals(data1, decompressed.toByteArray());
    }

    // Tests truncated stream throws IOException during reading
    @Test(expected = IOException.class)
    public void testDecompress_truncatedStream_throwsIOException() throws IOException {
        byte[] original = "Some data that is long enough to have content to truncate.".getBytes("UTF-8");
        byte[] compressed = compress(original);
        // Truncate compressed data in the middle
        byte[] truncated = Arrays.copyOf(compressed, compressed.length / 2);

        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(new ByteArrayInputStream(truncated));
        try {
            byte[] buf = new byte[128];
            while (bzIn.read(buf, 0, buf.length) != -1) {
                // keep reading until EOF or error
            }
        } finally {
            bzIn.close();
        }
    }

    // Tests corrupted block CRC throws IOException during reading
    @Test(expected = IOException.class)
    public void testDecompress_corruptedDataCRC_throwsIOException() throws IOException {
        byte[] original = "CRC verification test data with some repetitive repetitive repetitive content.".getBytes("UTF-8");
        byte[] compressed = compress(original);
        // Corrupt a byte in the middle of compressed payload
        compressed[compressed.length - 12] ^= 0x55;

        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(new ByteArrayInputStream(compressed));
        try {
            byte[] buf = new byte[128];
            while (bzIn.read(buf, 0, buf.length) != -1) {
                // keep reading to trigger CRC check at block end
            }
        } finally {
            bzIn.close();
        }
    }
}