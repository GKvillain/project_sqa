package org.apache.commons.codec.binary;

import static org.junit.Assert.*;

import org.junit.Test;

public class Base64Test {

    // Tests encoding of a simple byte array using default constructor
    @Test
    public void testEncodeBase64_normalInput_returnsEncodedBytes() {
        byte[] input = "Hello".getBytes();
        byte[] expected = "SGVsbG8=".getBytes();
        byte[] result = Base64.encodeBase64(input);
        assertArrayEquals(expected, result);
    }

    // Tests decoding of a simple base64 string
    @Test
    public void testDecodeBase64_normalInput_returnsDecodedBytes() {
        String input = "SGVsbG8=";
        byte[] expected = "Hello".getBytes();
        byte[] result = Base64.decodeBase64(input);
        assertArrayEquals(expected, result);
    }

    // Tests encodeBase64URLSafe with normal input
    @Test
    public void testEncodeBase64URLSafe_normalInput_returnsEncodedBytesWithoutPadding() {
        byte[] input = {0x3e, 0x5f}; // + and / would be used in standard, but url safe uses - and _
        byte[] result = Base64.encodeBase64URLSafe(input);
        String resultStr = new String(result);
        assertFalse(resultStr.contains("+"));
        assertFalse(resultStr.contains("/"));
        assertFalse(resultStr.contains("="));
        assertNotNull(result);
    }

    // Tests decode from a URL-safe string
    @Test
    public void testDecodeBase64_urlSafeInput_returnsDecodedBytes() {
        String urlSafeBase64 = "Pj8_Pg=="; // standard base64 of some bytes
        byte[] expected = Base64.decodeBase64("Pj8/Pg=="); // standard decode of same data
        byte[] result = new Base64().decode(urlSafeBase64);
        assertArrayEquals(expected, result);
    }

    // Tests encoding empty array
    @Test
    public void testEncodeBase64_emptyInput_returnsEmptyArray() {
        byte[] input = new byte[0];
        byte[] result = Base64.encodeBase64(input);
        assertEquals(0, result.length);
    }

    // Tests decoding empty string
    @Test
    public void testDecodeBase64_emptyString_returnsEmptyArray() {
        byte[] result = Base64.decodeBase64("");
        assertEquals(0, result.length);
    }

    // Tests encoding null input returns null
    @Test
    public void testEncodeBase64_nullInput_returnsNull() {
        assertNull(Base64.encodeBase64(null));
    }

    // Tests decoding null byte array returns null
    @Test
    public void testDecodeBase64_nullByteArray_returnsNull() {
        assertNull(new Base64().decode((byte[]) null));
    }

    // Tests encode with chunking (isChunked=true), ensures CRLF exists
    @Test
    public void testEncodeBase64Chunked_longInput_containsLineSeparator() {
        byte[] input = new byte[200];
        for (int i = 0; i < 200; i++) {
            input[i] = (byte) (i % 256);
        }
        byte[] result = Base64.encodeBase64Chunked(input);
        String resultStr = new String(result);
        assertTrue(resultStr.contains("\r\n"));
    }

    // Tests encode with maxResultSize exceeded, expecting IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testEncodeBase64_maxResultSizeExceeded_throwsIllegalArgumentException() {
        byte[] input = new byte[1000000];
        Base64.encodeBase64(input, false, false, 100);
    }

    // Tests that isBase64 returns true for valid base64 characters
    @Test
    public void testIsBase64_validCharacters_returnsTrue() {
        assertTrue(Base64.isBase64((byte) 'A'));
        assertTrue(Base64.isBase64((byte) 'z'));
        assertTrue(Base64.isBase64((byte) '0'));
        assertTrue(Base64.isBase64((byte) '+'));
        assertTrue(Base64.isBase64((byte) '/'));
        assertTrue(Base64.isBase64((byte) '='));
    }

    // Tests that isBase64 returns false for invalid characters
    @Test
    public void testIsBase64_invalidCharacters_returnsFalse() {
        assertFalse(Base64.isBase64((byte) '!'));
        assertFalse(Base64.isBase64((byte) '@'));
        assertFalse(Base64.isBase64((byte) 0x00));
        assertFalse(Base64.isBase64((byte) 0x7f));
    }

    // Tests encode single byte to check padding and encoding
    @Test
    public void testEncode_singleByte_returnsCorrectPadding() {
        byte[] input = {0x41};
        byte[] result = Base64.encodeBase64(input);
        assertArrayEquals("QQ==".getBytes(), result);
    }

    // Tests decode with padding
    @Test
    public void testDecode_withPadding_returnsDecodedBytes() {
        byte[] input = "QQ==".getBytes();
        byte[] expected = {0x41};
        byte[] result = Base64.decodeBase64(input);
        assertArrayEquals(expected, result);
    }

    // Tests encode two bytes
    @Test
    public void testEncode_twoBytes_returnsCorrectPadding() {
        byte[] input = {0x41, 0x42};
        byte[] result = Base64.encodeBase64(input);
        assertArrayEquals("QUI=".getBytes(), result);
    }

    // Tests decoding without padding (URL-safe style)
    @Test
    public void testDecode_withoutPadding_returnsDecodedBytes() {
        String input = "SGVsbG8"; // no padding
        byte[] expected = "Hello".getBytes();
        byte[] result = Base64.decodeBase64(input);
        assertArrayEquals(expected, result);
    }

    // Tests encode to string
    @Test
    public void testEncodeBase64String_normalInput_returnsEncodedString() {
        String result = Base64.encodeBase64String("Test".getBytes());
        assertEquals("VGVzdA==", result);
    }

    // Tests encode URL-safe string
    @Test
    public void testEncodeBase64URLSafeString_normalInput_returnsEncodedString() {
        String result = Base64.encodeBase64URLSafeString("f".getBytes());
        assertEquals("Zg", result); // no padding, URL-safe
    }

    // Tests isArrayByteBase64 for valid and invalid arrays
    @Test
    public void testIsArrayByteBase64_mixedArray_returnsFalse() {
        byte[] valid = "SGVsbG8=".getBytes();
        assertTrue(Base64.isArrayByteBase64(valid));

        byte[] invalid = "SGVsbG8!".getBytes();
        assertFalse(Base64.isArrayByteBase64(invalid));
    }
}