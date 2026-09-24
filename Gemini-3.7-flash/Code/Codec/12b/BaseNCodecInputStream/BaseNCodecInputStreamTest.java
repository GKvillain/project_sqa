package org.apache.commons.codec.binary;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BaseNCodecInputStreamTest {

    // Tests that markSupported always returns false
    @Test
    public void testMarkSupported_always_returnsFalse() {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        BaseNCodecInputStream stream = new BaseNCodecInputStream(in, new Base64(), false);
        assertFalse(stream.markSupported());
    }

    // Tests null byte array throws NullPointerException
    @Test(expected = NullPointerException.class)
    public void testRead_nullByteArray_throwsNullPointerException() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[] { 'A', 'B' });
        BaseNCodecInputStream stream = new BaseNCodecInputStream(in, new Base64(), false);
        stream.read(null, 0, 1);
    }

    // Tests negative offset throws IndexOutOfBoundsException
    @Test(expected = IndexOutOfBoundsException.class)
    public void testRead_negativeOffset_throwsIndexOutOfBoundsException() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[] { 'A', 'B' });
        BaseNCodecInputStream stream = new BaseNCodecInputStream(in, new Base64(), false);
        byte[] b = new byte[10];
        stream.read(b, -1, 5);
    }

    // Tests negative length throws IndexOutOfBoundsException
    @Test(expected = IndexOutOfBoundsException.class)
    public void testRead_negativeLength_throwsIndexOutOfBoundsException() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[] { 'A', 'B' });
        BaseNCodecInputStream stream = new BaseNCodecInputStream(in, new Base64(), false);
        byte[] b = new byte[10];
        stream.read(b, 0, -1);
    }

    // Tests offset greater than array length throws IndexOutOfBoundsException
    @Test(expected = IndexOutOfBoundsException.class)
    public void testRead_offsetGreaterThanLength_throwsIndexOutOfBoundsException() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[] { 'A', 'B' });
        BaseNCodecInputStream stream = new BaseNCodecInputStream(in, new Base64(), false);
        byte[] b = new byte[10];
        stream.read(b, 11, 1);
    }

    // Tests offset plus length greater than array length throws IndexOutOfBoundsException
    @Test(expected = IndexOutOfBoundsException.class)
    public void testRead_offsetPlusLenGreaterThanLength_throwsIndexOutOfBoundsException() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[] { 'A', 'B' });
        BaseNCodecInputStream stream = new BaseNCodecInputStream(in, new Base64(), false);
        byte[] b = new byte[10];
        stream.read(b, 5, 6);
    }

    // Tests read with length zero returns zero without reading from stream
    @Test
    public void testRead_lengthZero_returnsZero() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[] { 'A', 'B' });
        BaseNCodecInputStream stream = new BaseNCodecInputStream(in, new Base64(), false);
        byte[] b = new byte[10];
        int result = stream.read(b, 0, 0);
        assertEquals(0, result);
    }

    // Tests single-byte read with encoding
    @Test
    public void testRead_singleByteEncode_returnsCorrectEncodedBytes() throws IOException {
        byte[] input = StringUtils.getBytesUtf8("Hello World");
        InputStream in = new ByteArrayInputStream(input);
        BaseNCodecInputStream stream = new BaseNCodecInputStream(in, new Base64(0), true);

        StringBuilder sb = new StringBuilder();
        int b;
        while ((b = stream.read()) != -1) {
            sb.append((char) b);
        }
        assertEquals(new String(Base64.encodeBase64(input)), sb.toString());
    }

    // Tests single-byte read with decoding
    @Test
    public void testRead_singleByteDecode_returnsCorrectDecodedBytes() throws IOException {
        byte[] encoded = Base64.encodeBase64(StringUtils.getBytesUtf8("Hello World"));
        InputStream in = new ByteArrayInputStream(encoded);
        BaseNCodecInputStream stream = new BaseNCodecInputStream(in, new Base64(), false);

        StringBuilder sb = new StringBuilder();
        int b;
        while ((b = stream.read()) != -1) {
            sb.append((char) b);
        }
        assertEquals("Hello World", sb.toString());
    }

    // Tests single-byte read on empty stream returns EOF (-1)
    @Test
    public void testRead_emptyStream_returnsEof() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        BaseNCodecInputStream stream = new BaseNCodecInputStream(in, new Base64(), false);
        assertEquals(-1, stream.read());
    }

    // Tests single-byte read returns unsigned integer for negative byte values
    @Test
    public void testRead_negativeByteValue_returnsUnsignedValue() throws IOException {
        byte[] original = new byte[] { (byte) 0x80, (byte) 0xFF };
        byte[] encoded = Base64.encodeBase64(original);
        InputStream in = new ByteArrayInputStream(encoded);
        BaseNCodecInputStream stream = new BaseNCodecInputStream(in, new Base64(), false);

        assertEquals(128, stream.read());
        assertEquals(255, stream.read());
        assertEquals(-1, stream.read());
    }

    // Tests byte array read with encoding
    @Test
    public void testRead_byteArrayEncode_returnsEncodedData() throws IOException {
        byte[] input = StringUtils.getBytesUtf8("Testing BaseNCodecInputStream encoding");
        InputStream in = new ByteArrayInputStream(input);
        BaseNCodecInputStream stream = new BaseNCodecInputStream(in, new Base64(0), true);

        byte[] buf = new byte[100];
        int bytesRead = stream.read(buf, 0, buf.length);
        assertTrue(bytesRead > 0);

        String result = new String(buf, 0, bytesRead);
        assertEquals(new String(Base64.encodeBase64(input)), result);
    }

    // Tests byte array read with decoding
    @Test
    public void testRead_byteArrayDecode_returnsDecodedData() throws IOException {
        byte[] input = StringUtils.getBytesUtf8("Testing BaseNCodecInputStream decoding");
        byte[] encoded = Base64.encodeBase64(input);
        InputStream in = new ByteArrayInputStream(encoded);
        BaseNCodecInputStream stream = new BaseNCodecInputStream(in, new Base64(), false);

        byte[] buf = new byte[100];
        int bytesRead = stream.read(buf, 0, buf.length);
        assertTrue(bytesRead > 0);

        String result = new String(buf, 0, bytesRead);
        assertEquals("Testing BaseNCodecInputStream decoding", result);
    }

    // Tests byte array read after EOF returns -1
    @Test
    public void testRead_byteArrayAfterEof_returnsMinusOne() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        BaseNCodecInputStream stream = new BaseNCodecInputStream(in, new Base64(), false);

        byte[] buf = new byte[10];
        int bytesRead = stream.read(buf, 0, buf.length);
        assertEquals(-1, bytesRead);
    }

    // Tests read with offset into destination buffer
    @Test
    public void testRead_byteArrayWithOffset_fillsCorrectBufferSegment() throws IOException {
        byte[] input = StringUtils.getBytesUtf8("Hello");
        byte[] encoded = Base64.encodeBase64(input);
        InputStream in = new ByteArrayInputStream(encoded);
        BaseNCodecInputStream stream = new BaseNCodecInputStream(in, new Base64(), false);

        byte[] buf = new byte[10];
        int bytesRead = stream.read(buf, 2, 5);
        assertEquals(5, bytesRead);
        assertEquals(0, buf[0]);
        assertEquals(0, buf[1]);
        assertEquals('H', buf[2]);
        assertEquals('e', buf[3]);
        assertEquals('l', buf[4]);
        assertEquals('l', buf[5]);
        assertEquals('o', buf[6]);
    }
}