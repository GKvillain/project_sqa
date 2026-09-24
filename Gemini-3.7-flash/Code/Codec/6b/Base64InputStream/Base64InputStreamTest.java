package org.apache.commons.codec.binary;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Base64InputStreamTest {

    // Tests that markSupported always returns false
    @Test
    public void testMarkSupported_default_returnsFalse() {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        Base64InputStream b64In = new Base64InputStream(in);
        assertFalse(b64In.markSupported());
    }

    // Tests null byte array parameter in read method throws NullPointerException
    @Test(expected = NullPointerException.class)
    public void testRead_nullByteArray_throwsNullPointerException() throws IOException {
        InputStream in = new ByteArrayInputStream("SGVsbG8=".getBytes("UTF-8"));
        Base64InputStream b64In = new Base64InputStream(in);
        b64In.read(null, 0, 1);
    }

    // Tests negative offset in read method throws IndexOutOfBoundsException
    @Test(expected = IndexOutOfBoundsException.class)
    public void testRead_negativeOffset_throwsIndexOutOfBoundsException() throws IOException {
        InputStream in = new ByteArrayInputStream("SGVsbG8=".getBytes("UTF-8"));
        Base64InputStream b64In = new Base64InputStream(in);
        b64In.read(new byte[10], -1, 1);
    }

    // Tests negative length in read method throws IndexOutOfBoundsException
    @Test(expected = IndexOutOfBoundsException.class)
    public void testRead_negativeLength_throwsIndexOutOfBoundsException() throws IOException {
        InputStream in = new ByteArrayInputStream("SGVsbG8=".getBytes("UTF-8"));
        Base64InputStream b64In = new Base64InputStream(in);
        b64In.read(new byte[10], 0, -1);
    }

    // Tests offset exceeding array length throws IndexOutOfBoundsException
    @Test(expected = IndexOutOfBoundsException.class)
    public void testRead_offsetGreaterThanArrayLength_throwsIndexOutOfBoundsException() throws IOException {
        InputStream in = new ByteArrayInputStream("SGVsbG8=".getBytes("UTF-8"));
        Base64InputStream b64In = new Base64InputStream(in);
        b64In.read(new byte[10], 11, 1);
    }

    // Tests offset + len exceeding array length throws IndexOutOfBoundsException
    @Test(expected = IndexOutOfBoundsException.class)
    public void testRead_offsetPlusLenGreaterThanArrayLength_throwsIndexOutOfBoundsException() throws IOException {
        InputStream in = new ByteArrayInputStream("SGVsbG8=".getBytes("UTF-8"));
        Base64InputStream b64In = new Base64InputStream(in);
        b64In.read(new byte[10], 5, 6);
    }

    // Tests reading zero length returns 0 immediately
    @Test
    public void testRead_zeroLength_returnsZero() throws IOException {
        InputStream in = new ByteArrayInputStream("SGVsbG8=".getBytes("UTF-8"));
        Base64InputStream b64In = new Base64InputStream(in);
        int bytesRead = b64In.read(new byte[10], 0, 0);
        assertEquals(0, bytesRead);
    }

    // Tests single-byte read decoding and EOF handling
    @Test
    public void testRead_singleByteDecode_returnsCorrectBytesAndEof() throws IOException {
        byte[] encoded = "SGVsbG8=".getBytes("UTF-8"); // "Hello"
        InputStream in = new ByteArrayInputStream(encoded);
        Base64InputStream b64In = new Base64InputStream(in);

        assertEquals('H', b64In.read());
        assertEquals('e', b64In.read());
        assertEquals('l', b64In.read());
        assertEquals('l', b64In.read());
        assertEquals('o', b64In.read());
        assertEquals(-1, b64In.read());
    }

    // Tests single-byte read when decoded byte has high-bit set (> 127)
    @Test
    public void testRead_singleByteHighBit_returnsUnsignedValue() throws IOException {
        byte[] input = new byte[]{(byte) 0xFF, (byte) 0xFE};
        byte[] encoded = Base64.encodeBase64(input);
        InputStream in = new ByteArrayInputStream(encoded);
        Base64InputStream b64In = new Base64InputStream(in);

        assertEquals(255, b64In.read());
        assertEquals(254, b64In.read());
        assertEquals(-1, b64In.read());
    }

    // Tests single-byte read encoding mode
    @Test
    public void testRead_singleByteEncode_returnsEncodedStream() throws IOException {
        byte[] plain = "Hello".getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(plain);
        Base64InputStream b64In = new Base64InputStream(in, true);

        StringBuilder sb = new StringBuilder();
        int c;
        while ((c = b64In.read()) != -1) {
            sb.append((char) c);
        }
        assertEquals("SGVsbG8=", sb.toString());
    }

    // Tests reading decoded byte array in one chunk
    @Test
    public void testRead_decodeByteArray_returnsDecodedContent() throws IOException {
        byte[] encoded = "SGVsbG8gV29ybGQh".getBytes("UTF-8"); // "Hello World!"
        InputStream in = new ByteArrayInputStream(encoded);
        Base64InputStream b64In = new Base64InputStream(in);

        byte[] dest = new byte[12];
        int bytesRead = b64In.read(dest, 0, dest.length);

        assertEquals(12, bytesRead);
        assertEquals("Hello World!", new String(dest, 0, bytesRead, "UTF-8"));
        assertEquals(-1, b64In.read(dest, 0, dest.length));
    }

    // Tests reading encoded byte array with custom line length and separator
    @Test
    public void testRead_encodeCustomLineLengthAndSeparator_returnsFormattedData() throws IOException {
        byte[] plain = "12345678901234567890".getBytes("UTF-8");
        byte[] separator = new byte[]{'\n'};
        InputStream in = new ByteArrayInputStream(plain);
        Base64InputStream b64In = new Base64InputStream(in, true, 4, separator);

        byte[] dest = new byte[100];
        int bytesRead = b64In.read(dest, 0, dest.length);
        assertTrue(bytesRead > 0);

        String result = new String(dest, 0, bytesRead, "UTF-8");
        String expected = new String(Base64.encodeBase64(plain, true), "UTF-8");
        // Verify output contains separator
        assertTrue(result.contains("\n"));
    }

    // Tests decoding input stream containing non-base64 whitespace characters
    @Test
    public void testRead_decodeWithWhitespace_returnsCorrectDecodedData() throws IOException {
        String base64WithSpaces = "  S G V s \r\n b G 8 = \n";
        InputStream in = new ByteArrayInputStream(base64WithSpaces.getBytes("UTF-8"));
        Base64InputStream b64In = new Base64InputStream(in);

        byte[] dest = new byte[10];
        int bytesRead = b64In.read(dest, 0, dest.length);

        assertEquals(5, bytesRead);
        assertEquals("Hello", new String(dest, 0, bytesRead, "UTF-8"));
    }

    // Tests read on empty stream returns EOF (-1)
    @Test
    public void testRead_emptyStream_returnsEof() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        Base64InputStream b64In = new Base64InputStream(in);

        byte[] dest = new byte[10];
        int bytesRead = b64In.read(dest, 0, dest.length);
        assertEquals(-1, bytesRead);
        assertEquals(-1, b64In.read());
    }

    // Tests partial reads with offset into destination array
    @Test
    public void testRead_partialArrayWithOffset_storesAtCorrectIndex() throws IOException {
        byte[] encoded = "SGVsbG8=".getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(encoded);
        Base64InputStream b64In = new Base64InputStream(in);

        byte[] dest = new byte[10];
        Arrays.fill(dest, (byte) 0);

        int bytesRead = b64In.read(dest, 2, 3);
        assertEquals(3, bytesRead);
        assertEquals(0, dest[0]);
        assertEquals(0, dest[1]);
        assertEquals('H', dest[2]);
        assertEquals('e', dest[3]);
        assertEquals('l', dest[4]);
        assertEquals(0, dest[5]);
    }
}