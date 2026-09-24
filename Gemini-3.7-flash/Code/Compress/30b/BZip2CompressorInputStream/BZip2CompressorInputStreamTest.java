package org.apache.commons.compress.compressors.bzip2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

public class BZip2CompressorInputStreamTest {

    // Minimal valid empty bzip2 stream (Header "BZh9" + EOS Magic + Combined CRC 0)
    private static final byte[] EMPTY_BZIP2_DATA = new byte[] {
        'B', 'Z', 'h', '9',
        0x17, 0x72, 0x45, 0x38, 0x50, (byte) 0x90,
        0x00, 0x00, 0x00, 0x00
    };

    private static byte[] compress(byte[] data) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BZip2CompressorOutputStream bzOut = new BZip2CompressorOutputStream(baos);
        bzOut.write(data);
        bzOut.close();
        return baos.toByteArray();
    }

    // Tests matches() with signature length less than 3
    @Test
    public void testMatches_tooShort_returnsFalse() {
        byte[] signature = new byte[] { 'B', 'Z' };
        assertFalse(BZip2CompressorInputStream.matches(signature, 2));
        assertFalse(BZip2CompressorInputStream.matches(signature, 0));
    }

    // Tests matches() with valid bzip2 header
    @Test
    public void testMatches_validHeader_returnsTrue() {
        byte[] signature = new byte[] { 'B', 'Z', 'h', '9' };
        assertTrue(BZip2CompressorInputStream.matches(signature, 4));
        assertTrue(BZip2CompressorInputStream.matches(signature, 3));
    }

    // Tests matches() with invalid magic bytes
    @Test
    public void testMatches_invalidHeader_returnsFalse() {
        byte[] sig1 = new byte[] { 'A', 'Z', 'h' };
        byte[] sig2 = new byte[] { 'B', 'A', 'h' };
        byte[] sig3 = new byte[] { 'B', 'Z', 'x' };
        assertFalse(BZip2CompressorInputStream.matches(sig1, 3));
        assertFalse(BZip2CompressorInputStream.matches(sig2, 3));
        assertFalse(BZip2CompressorInputStream.matches(sig3, 3));
    }

    // Tests constructor with null InputStream
    @Test(expected = IOException.class)
    public void testConstructor_nullInputStream_throwsException() throws IOException {
        new BZip2CompressorInputStream(null);
    }

    // Tests constructor with empty stream (not bzip2 format)
    @Test(expected = IOException.class)
    public void testConstructor_emptyStream_throwsIOException() throws IOException {
        new BZip2CompressorInputStream(new ByteArrayInputStream(new byte[0]));
    }

    // Tests constructor with invalid magic bytes in stream
    @Test(expected = IOException.class)
    public void testConstructor_invalidMagic_throwsIOException() throws IOException {
        byte[] invalidData = new byte[] { 'P', 'K', 0x03, 0x04 };
        new BZip2CompressorInputStream(new ByteArrayInputStream(invalidData));
    }

    // Tests constructor with invalid block size byte
    @Test(expected = IOException.class)
    public void testConstructor_invalidBlockSize_throwsIOException() throws IOException {
        byte[] invalidBlockSize = new byte[] { 'B', 'Z', 'h', '0' };
        new BZip2CompressorInputStream(new ByteArrayInputStream(invalidBlockSize));
    }

    // Tests constructor with bad block header
    @Test(expected = IOException.class)
    public void testConstructor_badBlockHeader_throwsIOException() throws IOException {
        byte[] badBlock = new byte[] { 'B', 'Z', 'h', '9', 0x01, 0x02, 0x03, 0x04, 0x05, 0x06 };
        new BZip2CompressorInputStream(new ByteArrayInputStream(badBlock));
    }

    // Tests constructor when combined CRC does not match
    @Test(expected = IOException.class)
    public void testConstructor_corruptedCrc_throwsIOException() throws IOException {
        byte[] corruptedCrcData = new byte[] {
            'B', 'Z', 'h', '9',
            0x17, 0x72, 0x45, 0x38, 0x50, (byte) 0x90,
            0x00, 0x00, 0x00, 0x01
        };
        new BZip2CompressorInputStream(new ByteArrayInputStream(corruptedCrcData));
    }

    // Tests read(byte[], int, int) with zero length should return 0 (Defects4J bug regression test)
    @Test
    public void testRead_zeroLength_returnsZero() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(EMPTY_BZIP2_DATA);
        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(bais);
        try {
            byte[] dest = new byte[10];
            assertEquals(0, bzIn.read(dest, 0, 0));
        } finally {
            bzIn.close();
        }
    }

    // Tests single-byte read on empty bzip2 stream returns EOF (-1)
    @Test
    public void testRead_emptyStream_returnsMinusOne() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(EMPTY_BZIP2_DATA);
        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(bais);
        try {
            assertEquals(-1, bzIn.read());
        } finally {
            bzIn.close();
        }
    }

    // Tests byte-array read on empty bzip2 stream returns EOF (-1)
    @Test
    public void testRead_emptyStreamBuffer_returnsMinusOne() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(EMPTY_BZIP2_DATA);
        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(bais);
        try {
            byte[] dest = new byte[10];
            assertEquals(-1, bzIn.read(dest, 0, dest.length));
        } finally {
            bzIn.close();
        }
    }

    // Tests read with negative offset throws IndexOutOfBoundsException
    @Test(expected = IndexOutOfBoundsException.class)
    public void testRead_negativeOffset_throwsIndexOutOfBoundsException() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(EMPTY_BZIP2_DATA);
        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(bais);
        try {
            byte[] dest = new byte[10];
            bzIn.read(dest, -1, 5);
        } finally {
            bzIn.close();
        }
    }

    // Tests read with negative length throws IndexOutOfBoundsException
    @Test(expected = IndexOutOfBoundsException.class)
    public void testRead_negativeLength_throwsIndexOutOfBoundsException() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(EMPTY_BZIP2_DATA);
        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(bais);
        try {
            byte[] dest = new byte[10];
            bzIn.read(dest, 0, -1);
        } finally {
            bzIn.close();
        }
    }

    // Tests read with offset + length exceeding buffer length throws IndexOutOfBoundsException
    @Test(expected = IndexOutOfBoundsException.class)
    public void testRead_offsetPlusLengthExceedsBuffer_throwsIndexOutOfBoundsException() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(EMPTY_BZIP2_DATA);
        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(bais);
        try {
            byte[] dest = new byte[10];
            bzIn.read(dest, 5, 6);
        } finally {
            bzIn.close();
        }
    }

    // Tests read after close throws IOException
    @Test(expected = IOException.class)
    public void testRead_afterClose_throwsIOException() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(EMPTY_BZIP2_DATA);
        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(bais);
        bzIn.close();
        bzIn.read();
    }

    // Tests read(byte[], int, int) after close throws IOException
    @Test(expected = IOException.class)
    public void testReadArray_afterClose_throwsIOException() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(EMPTY_BZIP2_DATA);
        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(bais);
        bzIn.close();
        byte[] dest = new byte[10];
        bzIn.read(dest, 0, 1);
    }

    // Tests decompressConcatenated flag with garbage following valid stream
    @Test(expected = IOException.class)
    public void testConstructor_concatenatedGarbage_throwsIOException() throws IOException {
        byte[] concatenatedData = new byte[EMPTY_BZIP2_DATA.length + 4];
        System.arraycopy(EMPTY_BZIP2_DATA, 0, concatenatedData, 0, EMPTY_BZIP2_DATA.length);
        concatenatedData[EMPTY_BZIP2_DATA.length] = 'J';
        concatenatedData[EMPTY_BZIP2_DATA.length + 1] = 'U';
        concatenatedData[EMPTY_BZIP2_DATA.length + 2] = 'N';
        concatenatedData[EMPTY_BZIP2_DATA.length + 3] = 'K';

        new BZip2CompressorInputStream(new ByteArrayInputStream(concatenatedData), true);
    }

    // Tests multiple close() calls execute safely
    @Test
    public void testClose_multipleCalls_succeeds() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(EMPTY_BZIP2_DATA);
        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(bais);
        bzIn.close();
        bzIn.close();
    }

    // Tests decompressing valid non-empty data using byte-by-byte read
    @Test
    public void testRead_singleBytes_decompressesCorrectly() throws IOException {
        byte[] expected = "Hello, Apache Commons Compress BZip2!".getBytes("UTF-8");
        byte[] compressed = compress(expected);

        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(new ByteArrayInputStream(compressed));
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int b;
            while ((b = bzIn.read()) != -1) {
                out.write(b);
            }
            assertArrayEquals(expected, out.toByteArray());
        } finally {
            bzIn.close();
        }
    }

    // Tests decompressing valid non-empty data using chunked buffer read
    @Test
    public void testRead_bufferedChunks_decompressesCorrectly() throws IOException {
        byte[] expected = "Chunked reading test payload for BZip2 decompression.".getBytes("UTF-8");
        byte[] compressed = compress(expected);

        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(new ByteArrayInputStream(compressed));
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[8];
            int numRead;
            while ((numRead = bzIn.read(buf, 0, buf.length)) != -1) {
                out.write(buf, 0, numRead);
            }
            assertArrayEquals(expected, out.toByteArray());
        } finally {
            bzIn.close();
        }
    }

    // Tests decompressing repeated data to exercise run-length encoding (RLE)
    @Test
    public void testRead_runLengthEncodedData_decompressesCorrectly() throws IOException {
        byte[] expected = new byte[10000];
        Arrays.fill(expected, 0, 5000, (byte) 'A');
        Arrays.fill(expected, 5000, 10000, (byte) 'B');
        byte[] compressed = compress(expected);

        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(new ByteArrayInputStream(compressed));
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[256];
            int numRead;
            while ((numRead = bzIn.read(buf, 0, buf.length)) != -1) {
                out.write(buf, 0, numRead);
            }
            assertArrayEquals(expected, out.toByteArray());
        } finally {
            bzIn.close();
        }
    }

    // Tests decompressConcatenated = true with multiple valid concatenated bzip2 streams
    @Test
    public void testRead_concatenatedStreams_readsAllStreams() throws IOException {
        byte[] part1 = "First stream data. ".getBytes("UTF-8");
        byte[] part2 = "Second stream data.".getBytes("UTF-8");

        byte[] compressed1 = compress(part1);
        byte[] compressed2 = compress(part2);

        byte[] concatenated = new byte[compressed1.length + compressed2.length];
        System.arraycopy(compressed1, 0, concatenated, 0, compressed1.length);
        System.arraycopy(compressed2, 0, concatenated, compressed1.length, compressed2.length);

        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(new ByteArrayInputStream(concatenated), true);
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[32];
            int numRead;
            while ((numRead = bzIn.read(buf, 0, buf.length)) != -1) {
                out.write(buf, 0, numRead);
            }
            byte[] expected = "First stream data. Second stream data.".getBytes("UTF-8");
            assertArrayEquals(expected, out.toByteArray());
        } finally {
            bzIn.close();
        }
    }

    // Tests decompressConcatenated = false stops after the first stream
    @Test
    public void testRead_notConcatenated_stopsAtFirstStream() throws IOException {
        byte[] part1 = "First stream data.".getBytes("UTF-8");
        byte[] part2 = "Second stream data.".getBytes("UTF-8");

        byte[] compressed1 = compress(part1);
        byte[] compressed2 = compress(part2);

        byte[] concatenated = new byte[compressed1.length + compressed2.length];
        System.arraycopy(compressed1, 0, concatenated, 0, compressed1.length);
        System.arraycopy(compressed2, 0, concatenated, compressed1.length, compressed2.length);

        ByteArrayInputStream bais = new ByteArrayInputStream(concatenated);
        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(bais, false);
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[32];
            int numRead;
            while ((numRead = bzIn.read(buf, 0, buf.length)) != -1) {
                out.write(buf, 0, numRead);
            }
            assertArrayEquals(part1, out.toByteArray());
            // Verifies the second stream remained unread in the underlying stream
            assertEquals(compressed2.length, bais.available());
        } finally {
            bzIn.close();
        }
    }
}