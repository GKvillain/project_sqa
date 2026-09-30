package org.apache.commons.codec.binary;

import static org.junit.Assert.*;
import org.junit.Test;

import java.math.BigInteger;

/**
 * Test class for Base64 focusing on defect detection, branch coverage, and boundary conditions.
 */
public class Base64Test {

    // Tests encoding of null input returns null
    @Test
    public void testEncode_nullInput_returnsNull() {
        assertNull(new Base64().encode((byte[]) null));
    }

    // Tests encoding of empty byte array returns same array
    @Test
    public void testEncode_emptyInput_returnsSameArray() {
        byte[] input = new byte[0];
        assertSame(input, new Base64().encode(input));
    }

    // Tests decode of null input returns null
    @Test
    public void testDecode_nullInput_returnsNull() {
        assertNull(new Base64().decode((byte[]) null));
    }

    // Tests decode of empty byte array returns same array
    @Test
    public void testDecode_emptyInput_returnsSameArray() {
        byte[] input = new byte[0];
        assertSame(input, new Base64().decode(input));
    }

    // Tests standard encoding of simple input
    @Test
    public void testEncodeBase64_standardInput_returnsCorrectEncoding() {
        byte[] input = "Hello".getBytes();
        byte[] expected = "SGVsbG8=".getBytes();
        assertArrayEquals(expected, Base64.encodeBase64(input));
    }

    // Tests standard decoding of simple input
    @Test
    public void testDecodeBase64_standardInput_returnsCorrectDecoding() {
        String input = "SGVsbG8=";
        byte[] expected = "Hello".getBytes();
        assertArrayEquals(expected, Base64.decodeBase64(input));
    }

    // Tests URL-safe encoding
    @Test
    public void testEncodeBase64URLSafe_urlSafeInput_returnsCorrectEncoding() {
        byte[] input = new byte[]{(byte) 0xFF, (byte) 0xFB, (byte) 0xFC};
        byte[] expected = "/_v8".getBytes();
        assertArrayEquals(expected, Base64.encodeBase64URLSafe(input));
    }

    // Tests URL-safe encoding without padding
    @Test
    public void testEncodeBase64URLSafe_paddingNotNeeded_returnsNoPadding() {
        byte[] input = new byte[]{0x00, 0x00, 0x00};
        byte[] result = Base64.encodeBase64URLSafe(input);
        assertFalse(new String(result).contains("="));
    }

    // Tests encoding with chunked output
    @Test
    public void testEncodeBase64Chunked_inputLengthExceedsChunkSize_containsCRLF() {
        byte[] input = new byte[80];
        byte[] result = Base64.encodeBase64Chunked(input);
        String resultStr = new String(result);
        assertTrue(resultStr.contains("\r\n"));
    }

    // Tests isBase64 with valid character
    @Test
    public void testIsBase64_validChar_returnsTrue() {
        assertTrue(Base64.isBase64((byte) 'A'));
    }

    // Tests isBase64 with invalid character
    @Test
    public void testIsBase64_invalidChar_returnsFalse() {
        assertFalse(Base64.isBase64((byte) '!'));
    }

    // Tests isBase64 with pad character
    @Test
    public void testIsBase64_padChar_returnsTrue() {
        assertTrue(Base64.isBase64((byte) '='));
    }

    // Tests isArrayByteBase64 with valid array
    @Test
    public void testIsArrayByteBase64_validArray_returnsTrue() {
        byte[] array = "SGVsbG8=".getBytes();
        assertTrue(Base64.isArrayByteBase64(array));
    }

    // Tests isArrayByteBase64 with invalid array
    @Test
    public void testIsArrayByteBase64_invalidArray_returnsFalse() {
        byte[] array = new byte[]{'!', '@', '#'};
        assertFalse(Base64.isArrayByteBase64(array));
    }

    // Tests isArrayByteBase64 with whitespace
    @Test
    public void testIsArrayByteBase64_withWhitespace_returnsTrue() {
        byte[] array = "SGVs bG8=".getBytes();
        assertTrue(Base64.isArrayByteBase64(array));
    }

    // Tests encodeInteger with non-null input
    @Test
    public void testEncodeInteger_validInput_returnsEncodedBytes() {
        BigInteger bi = new BigInteger("123456789");
        byte[] result = Base64.encodeInteger(bi);
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    // Tests decodeInteger with valid input
    @Test
    public void testDecodeInteger_validInput_returnsCorrectBigInteger() {
        BigInteger original = new BigInteger("123456789");
        byte[] encoded = Base64.encodeInteger(original);
        BigInteger decoded = Base64.decodeInteger(encoded);
        assertEquals(original, decoded);
    }

    // Tests encodeInteger with null input throws NullPointerException
    @Test(expected = NullPointerException.class)
    public void testEncodeInteger_nullInput_throwsNullPointerException() {
        Base64.encodeInteger(null);
    }

    // Tests encoding with large input to trigger branch for line splitting (chunked)
    @Test
    public void testEncodeBase64Chunked_largeInput_containsMultipleLines() {
        byte[] input = new byte[300];
        byte[] result = Base64.encodeBase64Chunked(input);
        String resultStr = new String(result);
        // Count occurrences of CRLF
        int count = resultStr.split("\r\n", -1).length - 1;
        assertTrue(count >= 4);
    }

    // Tests decode of URL-safe encoded string
    @Test
    public void testDecodeBase64_urlSafeEncoded_returnsCorrectDecoding() {
        String input = "SGVsbG8";
        byte[] expected = "Hello".getBytes();
        assertArrayEquals(expected, Base64.decodeBase64(input));
    }

    // Tests that line separator containing base64 characters throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_lineSeparatorWithBase64Char_throwsIllegalArgumentException() {
        new Base64(76, new byte[]{'A', '\n'});
    }

    // Tests isUrlSafe returns false for default Base64
    @Test
    public void testIsUrlSafe_defaultConstructor_returnsFalse() {
        assertFalse(new Base64().isUrlSafe());
    }

    // Tests isUrlSafe returns true for URL-safe Base64
    @Test
    public void testIsUrlSafe_urlSafeConstructor_returnsTrue() {
        assertTrue(new Base64(true).isUrlSafe());
    }
}