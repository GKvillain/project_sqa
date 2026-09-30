package org.apache.commons.codec.binary;

import org.junit.Test;
import static org.junit.Assert.*;

public class Base64Test {

    // Tests encode standard input "Man" -> "TWFu"
    @Test
    public void testEncode_standardInput_returnsCorrectBase64() {
        assertArrayEquals(new byte[]{'T','W','F','u'}, Base64.encodeBase64(new byte[]{'M','a','n'}));
    }

    // Tests decode standard input "TWFu" -> "Man"
    @Test
    public void testDecode_standardInput_returnsOriginal() {
        assertArrayEquals(new byte[]{'M','a','n'}, Base64.decodeBase64(new byte[]{'T','W','F','u'}));
    }

    // Tests encode and decode round trip for URL-safe mode
    @Test
    public void testEncodeDecode_urlSafe_roundTrip() {
        byte[][] inputs = { {0}, {0,0}, {0,0,0}, {1,2,3} };
        for (byte[] input : inputs) {
            byte[] encoded = Base64.encodeBase64URLSafe(input);
            byte[] decoded = Base64.decodeBase64(encoded);
            assertArrayEquals(input, decoded);
        }
    }

    // Tests encode multiple lengths (1,2,3 bytes of zero) returns correct base64 with padding
    @Test
    public void testEncode_multipleLengths_returnsCorrect() {
        assertArrayEquals(new byte[]{'A','A','=','='}, Base64.encodeBase64(new byte[]{0}));
        assertArrayEquals(new byte[]{'A','A','A','='}, Base64.encodeBase64(new byte[]{0,0}));
        assertArrayEquals(new byte[]{'A','A','A','A'}, Base64.encodeBase64(new byte[]{0,0,0}));
    }

    // Tests decode various lengths (1,2,3 chars) returns correct bytes
    @Test
    public void testDecode_variousLengths_returnsCorrect() {
        assertArrayEquals(new byte[0], Base64.decodeBase64(new byte[]{'A'}));
        assertArrayEquals(new byte[]{0}, Base64.decodeBase64(new byte[]{'A','A'}));
        assertArrayEquals(new byte[]{0,0}, Base64.decodeBase64(new byte[]{'A','A','A'}));
    }

    // Tests decode padded input "AA==" returns single zero byte
    @Test
    public void testDecode_paddedInput_returnsCorrect() {
        assertArrayEquals(new byte[]{0}, Base64.decodeBase64(new byte[]{'A','A','=','='}));
    }

    // Tests encode chunked with 57 zeros: first 76 chars 'A' then CRLF
    @Test
    public void testEncode_chunked_containsLineSeparator() {
        byte[] input = new byte[57]; // all zeros
        byte[] encoded = Base64.encodeBase64Chunked(input);
        int len = encoded.length;
        assertTrue("Encoded chunked should end with CRLF", len >= 2);
        assertEquals('\r', encoded[len-2]);
        assertEquals('\n', encoded[len-1]);
        for (int i = 0; i < 76; i++) {
            assertEquals('A', encoded[i]);
        }
    }

    // Tests constructor throws IllegalArgumentException when lineSeparator contains base64 char
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_invalidLineSeparator_throwsException() {
        new Base64(76, new byte[]{'A'});
    }

    // Tests isBase64 for valid, pad, and whitespace bytes
    @Test
    public void testIsBase64_variousBytes() {
        assertTrue(Base64.isBase64((byte)'A'));
        assertTrue(Base64.isBase64((byte)'='));
        assertFalse(Base64.isBase64((byte)' '));
    }

    // Tests encodeBase64 with maxResultSize exceeded throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testEncode_maxResultSizeExceeded_throwsException() {
        byte[] input = new byte[100000];
        Base64.encodeBase64(input, false, false, 100);
    }

    // Tests decode ignores whitespace characters
    @Test
    public void testDecode_ignoresWhitespace() {
        byte[] input = { 'T', ' ', 'W', ' ', 'F', ' ', 'u' };
        assertArrayEquals(new byte[]{'M','a','n'}, Base64.decodeBase64(input));
    }

    // Tests empty and null inputs for encode and decode
    @Test
    public void testEmptyAndNullInputs() {
        assertArrayEquals(new byte[0], Base64.encodeBase64(new byte[0]));
        assertNull(Base64.encodeBase64(null));
        assertArrayEquals(new byte[0], Base64.decodeBase64(new byte[0]));
        assertNull(Base64.decodeBase64((byte[])null));
    }
}