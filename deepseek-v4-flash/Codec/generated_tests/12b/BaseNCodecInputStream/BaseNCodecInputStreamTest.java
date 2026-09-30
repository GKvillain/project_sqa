package org.apache.commons.codec.binary;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

public class BaseNCodecInputStreamTest {

    private InputStream createDecodeStream(String base64Data) {
        byte[] data = base64Data.getBytes();
        return new Base64InputStream(new ByteArrayInputStream(data));
    }

    private InputStream createEncodeStream(byte[] rawData) {
        return new Base64InputStream(new ByteArrayInputStream(rawData), true);
    }

    @Test(expected = NullPointerException.class)
    public void testRead_nullBuffer_throwsNullPointerException() throws IOException {
        InputStream in = createDecodeStream("SGVsbG8=");
        in.read(null, 0, 1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testRead_negativeOffset_throwsIndexOutOfBoundsException() throws IOException {
        InputStream in = createDecodeStream("SGVsbG8=");
        byte[] buf = new byte[10];
        in.read(buf, -1, 1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testRead_negativeLength_throwsIndexOutOfBoundsException() throws IOException {
        InputStream in = createDecodeStream("SGVsbG8=");
        byte[] buf = new byte[10];
        in.read(buf, 0, -1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testRead_offsetPlusLengthExceedsBuffer_throwsIndexOutOfBoundsException() throws IOException {
        InputStream in = createDecodeStream("SGVsbG8=");
        byte[] buf = new byte[10];
        in.read(buf, 8, 5);
    }

    @Test
    public void testRead_zeroLength_returnsZero() throws IOException {
        InputStream in = createDecodeStream("SGVsbG8=");
        byte[] buf = new byte[10];
        int result = in.read(buf, 0, 0);
        assertEquals(0, result);
    }

    @Test
    public void testRead_normalDecode_returnsCorrectBytes() throws IOException {
        InputStream in = createDecodeStream("SGVsbG8=");
        byte[] buf = new byte[10];
        int len = in.read(buf);
        byte[] expected = "Hello".getBytes();
        assertEquals(5, len);
        assertArrayEquals(expected, java.util.Arrays.copyOf(buf, len));
    }

    @Test
    public void testRead_normalEncode_returnsCorrectBytes() throws IOException {
        InputStream in = createEncodeStream("Hello".getBytes());
        byte[] buf = new byte[20];
        int len = in.read(buf);
        String expectedEncoded = "SGVsbG8=";
        byte[] expected = expectedEncoded.getBytes();
        assertEquals(expected.length, len);
        assertArrayEquals(expected, java.util.Arrays.copyOf(buf, len));
    }

    @Test
    public void testRead_emptyDecodeStream_returnsEOF() throws IOException {
        InputStream in = new Base64InputStream(new ByteArrayInputStream(new byte[0]));
        int result = in.read(new byte[10]);
        assertEquals(-1, result);
    }

    @Test
    public void testRead_emptyEncodeStream_returnsEOF() throws IOException {
        InputStream in = new Base64InputStream(new ByteArrayInputStream(new byte[0]), true);
        int result = in.read(new byte[10]);
        assertEquals(-1, result);
    }

    @Test
    public void testRead_singleByteRead_normalDecode_returnsCorrectValue() throws IOException {
        InputStream in = createDecodeStream("QQ==");
        int b = in.read();
        assertEquals('A', b);
        int b2 = in.read();
        assertEquals(-1, b2);
    }

    @Test
    public void testRead_singleByteRead_emptyDecodeStream_returnsEOF() throws IOException {
        InputStream in = new Base64InputStream(new ByteArrayInputStream(new byte[0]));
        int b = in.read();
        assertEquals(-1, b);
    }

    @Test
    public void testRead_singleByteRead_emptyEncodeStream_returnsEOF() throws IOException {
        InputStream in = new Base64InputStream(new ByteArrayInputStream(new byte[0]), true);
        int b = in.read();
        assertEquals(-1, b);
    }

    @Test
    public void testRead_afterEOF_returnsEOF() throws IOException {
        InputStream in = createDecodeStream("QQ==");
        in.read(); // consume 'A'
        int b = in.read(); // EOF
        assertEquals(-1, b);
        b = in.read(); // again
        assertEquals(-1, b);
    }

    @Test(timeout = 2000)
    public void testRead_emptyStreamNoInfiniteLoop() throws IOException {
        InputStream in = new Base64InputStream(new ByteArrayInputStream(new byte[0]));
        byte[] buf = new byte[10];
        int result = in.read(buf);
        assertEquals(-1, result);
    }

    @Test
    public void testRead_multipleReadsWithSmallBuffer_returnsCorrectData() throws IOException {
        InputStream in = createDecodeStream("SGVsbG8=");
        byte[] buf = new byte[2];
        int total = 0;
        int r;
        while ((r = in.read(buf)) != -1) {
            total += r;
        }
        assertEquals(5, total);
    }

    @Test
    public void testRead_encodeLargeData_uses4096Buffer() throws IOException {
        byte[] raw = new byte[5000];
        for (int i = 0; i < raw.length; i++) {
            raw[i] = (byte) (i % 256);
        }
        InputStream in = createEncodeStream(raw);
        byte[] buf = new byte[8192];
        int total = 0;
        int r;
        while ((r = in.read(buf)) != -1) {
            total += r;
        }
        int expectedEncodedLen = (int) Math.ceil(raw.length * 4.0 / 3.0);
        expectedEncodedLen = (expectedEncodedLen + 3) / 4 * 4;
        assertEquals(expectedEncodedLen, total);
    }

    @Test
    public void testMarkSupported_returnsFalse() {
        InputStream in = createDecodeStream("QQ==");
        assertFalse(in.markSupported());
    }
}