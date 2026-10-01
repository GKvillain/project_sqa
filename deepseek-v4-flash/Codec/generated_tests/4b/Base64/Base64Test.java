package org.apache.commons.codec.binary;

import static org.junit.Assert.*;

import java.nio.charset.StandardCharsets;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.junit.Test;

public class Base64Test {

    // Tests URL-safe encoding/decoding round-trip for one byte (no padding)
    @Test
    public void testEncodeBase64URLSafeString_oneByte_roundtrip() {
        byte[] original = new byte[]{(byte) 0x00};
        String encoded = Base64.encodeBase64URLSafeString(original);
        byte[] decoded = Base64.decodeBase64(encoded);
        assertArrayEquals(original, decoded);
    }

    // Tests URL-safe encoding/decoding round-trip for two bytes (no padding)
    @Test
    public void testEncodeBase64URLSafeString_twoBytes_roundtrip() {
        byte[] original = new byte[]{(byte) 0x01, (byte) 0x02};
        String encoded = Base64.encodeBase64URLSafeString(original);
        byte[] decoded = Base64.decodeBase64(encoded);
        assertArrayEquals(original, decoded);
    }

    // Tests URL-safe encoding/decoding round-trip for three bytes (no padding)
    @Test
    public void testEncodeBase64URLSafeString_threeBytes_roundtrip() {
        byte[] original = new byte[]{(byte) 0x03, (byte) 0x04, (byte) 0x05};
        String encoded = Base64.encodeBase64URLSafeString(original);
        byte[] decoded = Base64.decodeBase64(encoded);
        assertArrayEquals(original, decoded);
    }

    // Tests URL-safe encoding/decoding round-trip for larger data (no padding)
    @Test
    public void testEncodeBase64URLSafe_largerData_roundtrip() {
        byte[] original = new byte[]{(byte) 0x10, (byte) 0x20, (byte) 0x30, (byte) 0x40, (byte) 0x50};
        byte[] encoded = Base64.encodeBase64URLSafe(original);
        byte[] decoded = Base64.decodeBase64(encoded);
        assertArrayEquals(original, decoded);
    }

    // Tests standard Base64 encoding/decoding round-trip (with padding when needed)
    @Test
    public void testEncodeBase64_standard_roundtrip() {
        byte[] original = new byte[]{(byte) 0x61, (byte) 0x62, (byte) 0x63}; // "abc"
        byte[] encoded = Base64.encodeBase64(original);
        byte[] decoded = Base64.decodeBase64(encoded);
        assertArrayEquals(original, decoded);
    }

    // Tests encodeBase64String / decodeBase64 round-trip
    @Test
    public void testEncodeBase64String_roundtrip() {
        byte[] original = new byte[]{(byte) 0x41}; // 'A'
        String encoded = Base64.encodeBase64String(original);
        byte[] decoded = Base64.decodeBase64(encoded);
        assertArrayEquals(original, decoded);
    }

    // Tests chunked encoding contains line separator (\r\n)
    @Test
    public void testEncodeBase64Chunked_containsLineSeparator() {
        // 57 bytes produce exactly 76 base64 characters (no extra padding) and one line separator after that
        byte[] data = new byte[57];
        byte[] encoded = Base64.encodeBase64Chunked(data);
        String result = new String(encoded, StandardCharsets.UTF_8);
        assertTrue("Chunked output should contain CRLF", result.contains("\r\n"));
    }

    // Tests decoding input with whitespace (ignored)
    @Test
    public void testDecode_withWhitespace_ignoresWhitespace() {
        byte[] expected = new byte[]{(byte) 0x41}; // 'A'
        String input = "A Q=="; // contains space between A and Q
        byte[] decoded = Base64.decodeBase64(input);
        assertArrayEquals(expected, decoded);
    }

    // Tests isBase64 with a valid base64 character
    @Test
    public void testIsBase64_validChar_returnsTrue() {
        assertTrue(Base64.isBase64((byte) 'A'));
    }

    // Tests isBase64 with the padding character
    @Test
    public void testIsBase64_pad_returnsTrue() {
        assertTrue(Base64.isBase64((byte) '='));
    }

    // Tests isBase64 with an invalid character
    @Test
    public void testIsBase64_invalidChar_returnsFalse() {
        assertFalse(Base64.isBase64((byte) '!'));
    }

    // Tests isArrayByteBase64 with whitespace included
    @Test
    public void testIsArrayByteBase64_withWhitespace_returnsTrue() {
        byte[] array = new byte[]{' ', 'A', '\n', '='};
        assertTrue(Base64.isArrayByteBase64(array));
    }

    // Tests encodeBase64 throws IllegalArgumentException when maxResultSize exceeded
    @Test(expected = IllegalArgumentException.class)
    public void testEncodeBase64_maxResultSizeExceeded_throwsIllegalArgumentException() {
        byte[] data = new byte[1000];
        Base64.encodeBase64(data, false, false, 100);
    }

    // Tests constructor throws when lineSeparator contains base64 character
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_lineSeparatorContainsBase64_throwsIllegalArgumentException() {
        new Base64(76, new byte[]{'A'});
    }

    // Tests encode(Object) throws EncoderException for non-byte[] input
    @Test(expected = EncoderException.class)
    public void testEncodeObject_notByteArray_throwsEncoderException() throws EncoderException {
        new Base64().encode("string");
    }

    // Tests decode(Object) throws DecoderException for non-byte[] and non-String input
    @Test(expected = DecoderException.class)
    public void testDecodeObject_notByteArrayOrString_throwsDecoderException() throws DecoderException {
        new Base64().decode(Integer.valueOf(123));
    }

    // Tests encode with empty byte array returns empty array
    @Test
    public void testEncode_emptyArray_returnsEmpty() {
        byte[] input = new byte[0];
        byte[] result = new Base64().encode(input);
        assertArrayEquals(input, result);
    }

    // Tests decode with empty byte array returns empty array
    @Test
    public void testDecode_emptyArray_returnsEmpty() {
        byte[] input = new byte[0];
        byte[] result = new Base64().decode(input);
        assertArrayEquals(input, result);
    }

    // Tests decodeBase64 with null byte array returns null
    @Test
    public void testDecode_nullArray_returnsNull() {
        assertNull(Base64.decodeBase64((byte[]) null));
    }

    // Tests decodeBase64 with empty String returns empty array
    @Test
    public void testDecode_emptyString_returnsEmptyArray() {
        byte[] result = Base64.decodeBase64("");
        assertNotNull(result);
        assertEquals(0, result.length);
    }
}