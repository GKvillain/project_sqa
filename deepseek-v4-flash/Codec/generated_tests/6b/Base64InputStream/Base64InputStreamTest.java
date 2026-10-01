package org.apache.commons.codec.binary;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

public class Base64InputStreamTest {

    // Helper to read fully from the stream
    private byte[] readFully(Base64InputStream b64is, int expectedLength) throws IOException {
        byte[] buf = new byte[expectedLength];
        int total = 0;
        while (total < expectedLength) {
            int r = b64is.read(buf, total, expectedLength - total);
            if (r == -1) throw new IOException("Unexpected EOF");
            total += r;
        }
        return buf;
    }

    // Tests decoding of a simple base64 string
    @Test
    public void testReadDecode_normalInput_returnsDecodedBytes() throws Exception {
        byte[] encoded = "SGVsbG8=".getBytes("UTF-8");
        InputStream bis = new ByteArrayInputStream(encoded);
        Base64InputStream b64is = new Base64InputStream(bis, false);
        byte[] expected = "Hello".getBytes("UTF-8");
        byte[] actual = readFully(b64is, expected.length);
        assertArrayEquals(expected, actual);
        assertEquals(-1, b64is.read());
    }

    // Tests encoding of plain bytes
    @Test
    public void testReadEncode_normalInput_returnsEncodedBytes() throws Exception {
        byte[] plain = "Hello".getBytes("UTF-8");
        InputStream bis = new ByteArrayInputStream(plain);
        Base64InputStream b64is = new Base64InputStream(bis, true);
        byte[] expected = "SGVsbG8=".getBytes("UTF-8");
        byte[] actual = readFully(b64is, expected.length);
        assertArrayEquals(expected, actual);
    }

    // Tests reading single byte via read()
    @Test
    public void testReadSingleByte_decodeOneByte_returnsCorrect() throws Exception {
        byte[] encoded = "QQ==".getBytes("UTF-8");
        InputStream bis = new ByteArrayInputStream(encoded);
        Base64InputStream b64is = new Base64InputStream(bis, false);
        assertEquals('A', b64is.read());
        assertEquals(-1, b64is.read());
    }

    // Tests that empty decoding stream returns -1 (detects defect CODEC-101)
    @Test(timeout = 2000)
    public void testRead_emptyStreamDecode_returnsMinusOne() throws Exception {
        InputStream bis = new ByteArrayInputStream(new byte[0]);
        Base64InputStream b64is = new Base64InputStream(bis, false);
        assertEquals(-1, b64is.read());
    }

    // Tests that read(byte[]) on empty decoding stream returns -1
    @Test
    public void testReadBytes_emptyStreamDecode_returnsMinusOne() throws Exception {
        InputStream bis = new ByteArrayInputStream(new byte[0]);
        Base64InputStream b64is = new Base64InputStream(bis, false);
        byte[] buf = new byte[10];
        assertEquals(-1, b64is.read(buf, 0, 10));
    }

    // Tests that null array throws NullPointerException
    @Test(expected = NullPointerException.class)
    public void testRead_nullArray_throwsNullPointerException() throws Exception {
        InputStream bis = new ByteArrayInputStream(new byte[10]);
        Base64InputStream b64is = new Base64InputStream(bis);
        b64is.read(null, 0, 1);
    }

    // Tests negative offset throws IndexOutOfBoundsException
    @Test(expected = IndexOutOfBoundsException.class)
    public void testRead_negativeOffset_throwsIndexOutOfBoundsException() throws Exception {
        InputStream bis = new ByteArrayInputStream(new byte[10]);
        Base64InputStream b64is = new Base64InputStream(bis);
        b64is.read(new byte[10], -1, 1);
    }

    // Tests offset beyond array length throws IndexOutOfBoundsException
    @Test(expected = IndexOutOfBoundsException.class)
    public void testRead_offsetExceedsLength_throwsIndexOutOfBoundsException() throws Exception {
        InputStream bis = new ByteArrayInputStream(new byte[10]);
        Base64InputStream b64is = new Base64InputStream(bis);
        b64is.read(new byte[10], 10, 1);
    }

    // Tests negative len throws IndexOutOfBoundsException
    @Test(expected = IndexOutOfBoundsException.class)
    public void testRead_negativeLen_throwsIndexOutOfBoundsException() throws Exception {
        InputStream bis = new ByteArrayInputStream(new byte[10]);
        Base64InputStream b64is = new Base64InputStream(bis);
        b64is.read(new byte[10], 0, -1);
    }

    // Tests zero len returns 0
    @Test
    public void testRead_zeroLen_returnsZero() throws Exception {
        InputStream bis = new ByteArrayInputStream(new byte[10]);
        Base64InputStream b64is = new Base64InputStream(bis);
        assertEquals(0, b64is.read(new byte[10], 0, 0));
    }

    // Tests offset at end with zero len (valid)
    @Test
    public void testRead_offsetAtEndWithZeroLen_returnsZero() throws Exception {
        InputStream bis = new ByteArrayInputStream(new byte[10]);
        Base64InputStream b64is = new Base64InputStream(bis);
        byte[] buf = new byte[10];
        assertEquals(0, b64is.read(buf, 10, 0));
    }

    // Tests optimization when b.length == len
    @Test
    public void testReadDecode_largeBuffer_optimizationUsed() throws Exception {
        byte[] encoded = "SGVsbG8=".getBytes("UTF-8");
        InputStream bis = new ByteArrayInputStream(encoded);
        Base64InputStream b64is = new Base64InputStream(bis, false);
        byte[] actual = new byte[5];
        int n = b64is.read(actual, 0, 5);
        assertEquals(5, n);
        assertArrayEquals("Hello".getBytes("UTF-8"), actual);
    }

    // Tests when buffer is larger than len (no optimization)
    @Test
    public void testReadDecode_bufferLargerThanLen_works() throws Exception {
        byte[] encoded = "SGVsbG8=".getBytes("UTF-8");
        InputStream bis = new ByteArrayInputStream(encoded);
        Base64InputStream b64is = new Base64InputStream(bis, false);
        byte[] actual = new byte[100];
        int n = b64is.read(actual, 0, 5);
        assertEquals(5, n);
        byte[] expected = "Hello".getBytes("UTF-8");
        assertArrayEquals(expected, Arrays.copyOf(actual, 5));
    }

    // Tests multiple reads to exercise hasData() true/false paths
    @Test
    public void testReadDecode_multipleChunks_works() throws Exception {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb.append("Hello");
        }
        byte[] plain = sb.toString().getBytes("UTF-8");
        byte[] encoded = Base64.encodeBase64(plain);
        InputStream bis = new ByteArrayInputStream(encoded);
        Base64InputStream b64is = new Base64InputStream(bis, false);
        byte[] actual = new byte[plain.length];
        int totalRead = 0;
        while (totalRead < plain.length) {
            int r = b64is.read(actual, totalRead, Math.min(100, plain.length - totalRead));
            if (r == -1) break;
            totalRead += r;
        }
        assertArrayEquals(plain, actual);
    }

    // Tests encoding with line length and line separator
    @Test
    public void testReadEncode_withLineLength_returnsEncodedWithLineBreaks() throws Exception {
        byte[] plain = "HelloWorld".getBytes("UTF-8");
        byte[] lineSep = "\n".getBytes("UTF-8");
        InputStream bis = new ByteArrayInputStream(plain);
        Base64InputStream b64is = new Base64InputStream(bis, true, 4, lineSep);
        String expectedEncoded = "SGVs\nbG9X\nb3Js\nZA==";
        byte[] expected = expectedEncoded.getBytes("UTF-8");
        byte[] actual = readFully(b64is, expected.length);
        assertArrayEquals(expected, actual);
    }

    // Tests that whitespace in base64 input is ignored (read() may loop internally)
    @Test
    public void testReadDecode_ignoresWhitespace_returnsDecoded() throws Exception {
        String input = "  SGVs bG8=  \n";
        byte[] encoded = input.getBytes("UTF-8");
        InputStream bis = new ByteArrayInputStream(encoded);
        Base64InputStream b64is = new Base64InputStream(bis, false);
        byte[] expected = "Hello".getBytes("UTF-8");
        byte[] actual = readFully(b64is, expected.length);
        assertArrayEquals(expected, actual);
        assertEquals(-1, b64is.read());
    }

    // Tests markSupported returns false
    @Test
    public void testMarkSupported_returnsFalse() {
        InputStream bis = new ByteArrayInputStream(new byte[10]);
        Base64InputStream b64is = new Base64InputStream(bis);
        assertFalse(b64is.markSupported());
    }
}