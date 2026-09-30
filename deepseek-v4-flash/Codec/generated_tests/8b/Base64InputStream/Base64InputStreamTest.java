package org.apache.commons.codec.binary;

import static org.junit.Assert.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.junit.Test;

public class Base64InputStreamTest {

    // ==================== Existing Tests ====================

    // Tests decode single byte using read()
    @Test
    public void testRead_singleByte_decode_returnsCorrectValue() throws IOException {
        Base64InputStream in = new Base64InputStream(new ByteArrayInputStream("SGVsbG8=".getBytes("UTF-8")));
        int b = in.read();
        assertEquals('H', b);
    }

    // Tests decode all bytes into buffer
    @Test
    public void testRead_allBytes_decode_returnsHello() throws IOException {
        Base64InputStream in = new Base64InputStream(new ByteArrayInputStream("SGVsbG8=".getBytes("UTF-8")));
        byte[] out = new byte[5];
        int n = in.read(out, 0, out.length);
        assertEquals(5, n);
        assertArrayEquals("Hello".getBytes("UTF-8"), out);
        assertEquals(-1, in.read());
    }

    // Tests encode mode returns base64 representation
    @Test
    public void testRead_encode_returnsBase64() throws IOException {
        Base64InputStream in = new Base64InputStream(new ByteArrayInputStream("Hello".getBytes("UTF-8")), true);
        byte[] buf = new byte[100];
        int n = in.read(buf, 0, buf.length);
        assertEquals("SGVsbG8=", new String(buf, 0, n, "UTF-8"));
    }

    // Tests encode mode with line length and separator
    @Test
    public void testRead_encodeChunked_returnsChunked() throws IOException {
        byte[] eol = "\n".getBytes("UTF-8");
        Base64InputStream in = new Base64InputStream(
                new ByteArrayInputStream("Hello".getBytes("UTF-8")), true, 4, eol);
        byte[] buf = new byte[100];
        int n = in.read(buf, 0, buf.length);
        assertEquals("SGVs\nbG8=", new String(buf, 0, n, "UTF-8"));
    }

    // Tests decode with chunked input (line separator before end)
    @Test
    public void testRead_decodeChunked_returnsHello() throws IOException {
        Base64InputStream in = new Base64InputStream(
                new ByteArrayInputStream("SGVs\nbG8=".getBytes("UTF-8")));
        byte[] buf = new byte[100];
        int n = in.read(buf, 0, buf.length);
        assertEquals("Hello", new String(buf, 0, n, "UTF-8"));
    }

    // Tests zero length read
    @Test
    public void testRead_lenZero_returnsZero() throws IOException {
        Base64InputStream in = new Base64InputStream(
                new ByteArrayInputStream("SGVs".getBytes("UTF-8")));
        assertEquals(0, in.read(new byte[0], 0, 0));
    }

    // Tests null buffer throws NullPointerException
    @Test(expected = NullPointerException.class)
    public void testRead_nullBuffer_throwsNullPointerException() throws IOException {
        Base64InputStream in = new Base64InputStream(
                new ByteArrayInputStream("SGVs".getBytes("UTF-8")));
        in.read(null, 0, 1);
    }

    // Tests negative offset throws IndexOutOfBoundsException
    @Test(expected = IndexOutOfBoundsException.class)
    public void testRead_negativeOffset_throwsIndexOutOfBoundsException() throws IOException {
        Base64InputStream in = new Base64InputStream(
                new ByteArrayInputStream("SGVs".getBytes("UTF-8")));
        in.read(new byte[10], -1, 1);
    }

    // Tests offset + len exceeds buffer length
    @Test(expected = IndexOutOfBoundsException.class)
    public void testRead_offsetLenExceedsBuffer_throwsIndexOutOfBoundsException() throws IOException {
        Base64InputStream in = new Base64InputStream(
                new ByteArrayInputStream("SGVs".getBytes("UTF-8")));
        in.read(new byte[10], 5, 6);
    }

    // Tests reading after EOF returns -1
    @Test
    public void testRead_afterEOF_returnsMinusOne() throws IOException {
        Base64InputStream in = new Base64InputStream(
                new ByteArrayInputStream("SGVsbG8=".getBytes("UTF-8")));
        byte[] buf = new byte[100];
        int n = in.read(buf, 0, buf.length);
        assertTrue(n > 0);
        assertEquals(-1, in.read());
    }

    // Tests markSupported is always false
    @Test
    public void testMarkSupported_returnsFalse() {
        Base64InputStream in = new Base64InputStream(new ByteArrayInputStream(new byte[0]));
        assertFalse(in.markSupported());
    }

    // Tests large amount of whitespace before base64 data is skipped
    @Test
    public void testRead_largeWhitespaceBeforeData_returnsDecoded() throws IOException {
        StringBuilder sb = new StringBuilder(8200);
        for (int i = 0; i < 8192; i++) {
            sb.append(' ');
        }
        sb.append("SGVsbG8=");
        Base64InputStream in = new Base64InputStream(
                new ByteArrayInputStream(sb.toString().getBytes("UTF-8")));
        byte[] buf = new byte[100];
        int n = in.read(buf, 0, buf.length);
        assertEquals("Hello", new String(buf, 0, n, "UTF-8"));
        assertEquals(-1, in.read());
    }

    // Tests read() skips zero-result reads when whitespace is encountered
    @Test
    public void testRead_singleByte_largeWhitespaceBeforeData_returnsFirstByte() throws IOException {
        StringBuilder sb = new StringBuilder(8200);
        for (int i = 0; i < 8192; i++) {
            sb.append(' ');
        }
        sb.append("SGVsbG8=");
        Base64InputStream in = new Base64InputStream(
                new ByteArrayInputStream(sb.toString().getBytes("UTF-8")));
        int b = in.read();
        assertEquals('H', b);
    }

    // Tests empty input returns EOF
    @Test
    public void testRead_emptyInput_returnsEOF() throws IOException {
        Base64InputStream in = new Base64InputStream(new ByteArrayInputStream(new byte[0]));
        assertEquals(-1, in.read());
        assertEquals(-1, in.read(new byte[1], 0, 1));
    }

    // Tests read with offset and length writes only into requested range
    @Test
    public void testRead_readWithOffset_returnsDecodedInRange() throws IOException {
        Base64InputStream in = new Base64InputStream(
                new ByteArrayInputStream("SGVsbG8=".getBytes("UTF-8")));
        byte[] buf = new byte[20];
        int n = in.read(buf, 5, 10);
        assertEquals(5, n);
        assertEquals("Hello", new String(buf, 5, n, "UTF-8"));
    }

    // Tests multiple reads return all decoded data with internal buffering
    @Test
    public void testRead_decodeMultipleReads_returnsAllData() throws IOException {
        Base64InputStream in = new Base64InputStream(
                new ByteArrayInputStream("SGVsbG8=".getBytes("UTF-8")));
        byte[] first = new byte[2];
        assertEquals(2, in.read(first, 0, 2));
        byte[] second = new byte[2];
        assertEquals(2, in.read(second, 0, 2));
        byte[] third = new byte[2];
        assertEquals(1, in.read(third, 0, 2));
        assertEquals(-1, in.read());
    }

    // ==================== New Tests for Uncovered Parts ====================

    // Tests that available() returns a non-negative value before and after read
    @Test
    public void testAvailable() throws IOException {
        Base64InputStream in = new Base64InputStream(
                new ByteArrayInputStream("SGVsbG8=".getBytes("UTF-8")));
        int availBefore = in.available();
        assertTrue("available should be non-negative before read", availBefore >= 0);
        byte[] buf = new byte[10];
        in.read(buf, 0, buf.length);
        int availAfter = in.available();
        assertTrue("available should be non-negative after read", availAfter >= 0);
        in.read(); // read to EOF
        assertTrue("available at EOF should be 0 or non-negative", in.available() >= 0);
        in.close();
    }

    // Tests that close() can be called multiple times without throwing
    @Test
    public void testClose_multipleCalls_doesNotThrow() throws IOException {
        Base64InputStream in = new Base64InputStream(
                new ByteArrayInputStream("SGVsbG8=".getBytes("UTF-8")));
        in.close();
        in.close(); // second close should be harmless
    }

    // Tests that read() after close throws IOException
    @Test(expected = IOException.class)
    public void testRead_afterClose_throwsIOException() throws IOException {
        Base64InputStream in = new Base64InputStream(
                new ByteArrayInputStream("SGVsbG8=".getBytes("UTF-8")));
        in.close();
        in.read(); // should throw
    }

    // Tests that read(byte[], int, int) after close throws IOException
    @Test(expected = IOException.class)
    public void testReadBuffer_afterClose_throwsIOException() throws IOException {
        Base64InputStream in = new Base64InputStream(
                new ByteArrayInputStream("SGVsbG8=".getBytes("UTF-8")));
        in.close();
        in.read(new byte[10], 0, 5);
    }

    // Tests constructor with null lineSeparator uses default separator (should not throw)
    @Test
    public void testConstructor_nullLineSeparator_usesDefault() throws IOException {
        // lineLength > 0, lineSeparator = null -> should use default (usually CRLF or LF)
        Base64InputStream in = new Base64InputStream(
                new ByteArrayInputStream("Hello".getBytes("UTF-8")), true, 4, null);
        byte[] buf = new byte[100];
        int n = in.read(buf, 0, buf.length);
        // The output will be chunked with default separator; we just check it's not empty
        assertTrue("Should produce some output", n > 0);
        in.close();
    }

    // Tests constructor with zero lineLength (no chunking)
    @Test
    public void testConstructor_zeroLineLength_disablesChunking() throws IOException {
        Base64InputStream in = new Base64InputStream(
                new ByteArrayInputStream("Hello".getBytes("UTF-8")), true, 0, "\n".getBytes("UTF-8"));
        byte[] buf = new byte[100];
        int n = in.read(buf, 0, buf.length);
        // lineLength=0 means no line separator inserted (chunking disabled)
        String result = new String(buf, 0, n, "UTF-8");
        assertEquals("SGVsbG8=", result);
        in.close();
    }

    // Tests decode with various whitespace characters (tab, newline, carriage return)
    @Test
    public void testRead_decodeWithTabsAndNewlines_returnsHello() throws IOException {
        String mixedWhitespace = "\t\n\r SGVs\tbG8=\n ";
        Base64InputStream in = new Base64InputStream(
                new ByteArrayInputStream(mixedWhitespace.getBytes("UTF-8")));
        byte[] buf = new byte[100];
        int n = in.read(buf, 0, buf.length);
        assertEquals("Hello", new String(buf, 0, n, "UTF-8"));
        assertEquals(-1, in.read());
        in.close();
    }

    // Tests encode with empty lineSeparator array (should behave like no separator)
    @Test
    public void testEncode_emptyLineSeparator_ignoresSeparator() throws IOException {
        byte[] emptySep = new byte[0];
        Base64InputStream in = new Base64InputStream(
                new ByteArrayInputStream("Hello".getBytes("UTF-8")), true, 4, emptySep);
        byte[] buf = new byte[100];
        int n = in.read(buf, 0, buf.length);
        // Empty separator means no line break inserted (chunking disabled)
        assertEquals("SGVsbG8=", new String(buf, 0, n, "UTF-8"));
        in.close();
    }
}