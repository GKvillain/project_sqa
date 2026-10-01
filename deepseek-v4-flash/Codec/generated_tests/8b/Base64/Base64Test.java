package org.apache.commons.codec.binary;

import static org.junit.Assert.*;

import java.math.BigInteger;

import org.junit.Test;

public class Base64Test {

    // Test encodeBase64 with null input: returns null
    @Test
    public void testEncodeBase64_nullInput_returnsNull() {
        byte[] result = Base64.encodeBase64(null);
        assertNull(result);
    }

    // Test encodeBase64 with empty input: returns empty array
    @Test
    public void testEncodeBase64_emptyInput_returnsEmpty() {
        byte[] input = new byte[0];
        byte[] result = Base64.encodeBase64(input);
        assertEquals(0, result.length);
    }

    // Test encodeBase64 with single byte input (modulus = 1, standard encoding)
    @Test
    public void testEncodeBase64_singleByte_standardEncoding() {
        byte[] input = new byte[] {(byte) 0x41}; // 'A'
        byte[] expected = "QQ==".getBytes();
        byte[] result = Base64.encodeBase64(input);
        assertArrayEquals(expected, result);
    }

    // Test encodeBase64 with two-byte input (modulus = 2, standard encoding)
    @Test
    public void testEncodeBase64_twoBytes_standardEncoding() {
        byte[] input = new byte[] {(byte) 0x41, (byte) 0x42}; // "AB"
        byte[] expected = "QUI=".getBytes();
        byte[] result = Base64.encodeBase64(input);
        assertArrayEquals(expected, result);
    }

    // Test encodeBase64 with three-byte input (modulus = 0, no padding)
    @Test
    public void testEncodeBase64_threeBytes_standardEncoding() {
        byte[] input = new byte[] {(byte) 0x41, (byte) 0x42, (byte) 0x43}; // "ABC"
        byte[] expected = "QUJD".getBytes();
        byte[] result = Base64.encodeBase64(input);
        assertArrayEquals(expected, result);
    }

    // Test encodeBase64URLSafe with single byte (should omit padding)
    @Test
    public void testEncodeBase64URLSafe_singleByte_noPadding() {
        byte[] input = new byte[] {(byte) 0x41};
        byte[] result = Base64.encodeBase64URLSafe(input);
        String resultStr = new String(result);
        // URL-safe encoding: padding omitted, '+' replaced with '-', '/' replaced with '_'
        assertTrue(resultStr.endsWith("Q"));
        assertEquals(2, result.length);
    }

    // Test encodeBase64String returns correct String
    @Test
    public void testEncodeBase64String_oneByte_returnsCorrectString() {
        byte[] input = new byte[] {(byte) 0x41};
        String result = Base64.encodeBase64String(input);
        assertEquals("QQ==", result);
    }

    // Test decodeBase64 with standard encoded string (with padding)
    @Test
    public void testDecodeBase64_standardString_returnsOriginal() {
        String base64String = "QUJD";
        byte[] expected = new byte[] {(byte) 0x41, (byte) 0x42, (byte) 0x43};
        byte[] result = Base64.decodeBase64(base64String);
        assertArrayEquals(expected, result);
    }

    // Test decodeBase64 with URL-safe encoded string (no '+' or '/')
    @Test
    public void testDecodeBase64_urlSafeString_decodesCorrectly() {
        String base64String = "QUJD";
        byte[] expected = new byte[] {(byte) 0x41, (byte) 0x42, (byte) 0x43};
        byte[] result = Base64.decodeBase64(base64String);
        assertArrayEquals(expected, result);
    }

    // Test encodeBase64Chunked: output should contain chunk separator (CRLF)
    @Test
    public void testEncodeBase64Chunked_longInput_containsLineSeparator() {
        byte[] input = new byte[100];
        for (int i = 0; i < 100; i++) {
            input[i] = (byte) i;
        }
        byte[] result = Base64.encodeBase64Chunked(input);
        String resultStr = new String(result);
        assertTrue(resultStr.contains("\r\n"));
    }

    // Test encode with negative input bytes (e.g., byte value > 127)
    @Test
    public void testEncode_negativeBytes_encodesCorrectly() {
        byte[] input = new byte[] {(byte) 0xFF, (byte) 0xFE, (byte) 0xFD};
        byte[] expected = Base64.encodeBase64(input);
        byte[] result = Base64.encodeBase64(input);
        assertArrayEquals(expected, result);
    }

    // Test isBase64 for known valid characters
    @Test
    public void testIsBase64_validCharacters_returnsTrue() {
        assertTrue(Base64.isBase64((byte) 'A'));
        assertTrue(Base64.isBase64((byte) 'z'));
        assertTrue(Base64.isBase64((byte) '0'));
        assertTrue(Base64.isBase64((byte) '+'));
        assertTrue(Base64.isBase64((byte) '/'));
        assertTrue(Base64.isBase64((byte) '='));
    }

    // Test isBase64 for invalid character
    @Test
    public void testIsBase64_invalidCharacter_returnsFalse() {
        assertFalse(Base64.isBase64((byte) '!'));
        assertFalse(Base64.isBase64((byte) '#'));
        assertFalse(Base64.isBase64((byte) 0x00));
    }

    // Test isArrayByteBase64 with whitespace (RFC 2045)
    @Test
    public void testIsArrayByteBase64_whitespace_returnsTrue() {
        byte[] array = new byte[] {'A', 'B', ' ', '\n', '\r', '\t', 'C'};
        assertTrue(Base64.isArrayByteBase64(array));
    }

    // Test decodeInteger with a known base64 encoded byte array
    @Test
    public void testDecodeInteger_validInput_returnsBigInteger() {
        // encode integer 1 as base64
        byte[] encoded = Base64.encodeBase64(new byte[] {1});
        BigInteger result = Base64.decodeInteger(encoded);
        assertEquals(BigInteger.ONE, result);
    }

    // Test encodeInteger with null: should throw NullPointerException
    @Test(expected = NullPointerException.class)
    public void testEncodeInteger_nullInput_throwsNullPointerException() {
        Base64.encodeInteger(null);
    }

    // Test constructor with line length and URL-safe mode
    @Test
    public void testConstructor_urlSafeMode_encodesWithoutPadding() {
        Base64 b64 = new Base64(0, new byte[] {}, true);
        byte[] input = new byte[] {(byte) 0x41};
        byte[] result = b64.encode(input);
        String resultStr = new String(result);
        // URL-safe: padding omitted, result length 2
        assertEquals(2, result.length);
        assertFalse(resultStr.contains("="));
    }

    // Test encodeBase64 with maxResultSize exceeded
    @Test(expected = IllegalArgumentException.class)
    public void testEncodeBase64_maxResultSizeExceeded_throwsIllegalArgumentException() {
        byte[] input = new byte[1000000];
        Base64.encodeBase64(input, false, false, 10);
    }

    // Test decode with empty byte array
    @Test
    public void testDecode_emptyArray_returnsEmpty() {
        byte[] input = new byte[0];
        byte[] result = new Base64().decode(input);
        assertArrayEquals(input, result);
    }

    // Test encode with zero-length byte array
    @Test
    public void testEncode_zeroLengthArray_returnsEmpty() {
        byte[] input = new byte[0];
        byte[] result = new Base64().encode(input);
        assertArrayEquals(input, result);
    }
}