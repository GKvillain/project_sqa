package org.apache.commons.codec.binary;

import static org.junit.Assert.*;

import org.junit.Test;

import java.math.BigInteger;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;

public class Base64Test {

    // Test encodeBase64 with null input returns null
    @Test
    public void testEncodeBase64_nullInput_returnsNull() {
        assertNull(Base64.encodeBase64(null));
    }

    // Test encodeBase64 with empty array returns empty
    @Test
    public void testEncodeBase64_emptyInput_returnsEmpty() {
        byte[] input = new byte[0];
        byte[] result = Base64.encodeBase64(input);
        assertEquals(0, result.length);
    }

    // Test encodeBase64 with normal input returns correct output
    @Test
    public void testEncodeBase64_normalInput_returnsEncoded() {
        byte[] input = "Hello".getBytes();
        byte[] expected = "SGVsbG8=".getBytes();
        assertArrayEquals(expected, Base64.encodeBase64(input));
    }

    // Test encodeBase64 with 2 bytes input returns correct padding
    @Test
    public void testEncodeBase64_twoBytesInput_returnsEncodedWithPadding() {
        byte[] input = new byte[]{(byte) 0x00, (byte) 0x01};
        byte[] expected = "AAE=".getBytes();
        assertArrayEquals(expected, Base64.encodeBase64(input));
    }

    // Test encodeBase64Chunked with long input produces chunked output
    @Test
    public void testEncodeBase64Chunked_longInput_containsLineSeparator() {
        // Create input that produces output longer than 76 characters
        byte[] input = new byte[100];
        for (int i = 0; i < 100; i++) {
            input[i] = (byte) i;
        }
        byte[] result = Base64.encodeBase64Chunked(input);
        String resultStr = new String(result);
        assertTrue(resultStr.contains("\r\n"));
    }

    // Test encodeBase64URLSafe with normal input returns URL-safe encoded output
    @Test
    public void testEncodeBase64URLSafe_normalInput_returnsURLSafeEncoded() {
        byte[] input = new byte[]{(byte) 0x3e, (byte) 0xbf};
        // Standard: Pj8= ; URL-safe: Pj8=
        // Use bytes that produce + or / to see difference
        byte[] input2 = new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff};
        byte[] result = Base64.encodeBase64URLSafe(input2);
        String resultStr = new String(result);
        assertFalse(resultStr.contains("+"));
        assertFalse(resultStr.contains("/"));
        assertFalse(resultStr.contains("="));
    }

    // Test decodeBase64 with null input returns null
    @Test
    public void testDecodeBase64_nullInput_returnsNull() {
        assertNull(Base64.decodeBase64(null));
    }

    // Test decodeBase64 with empty array returns empty
    @Test
    public void testDecodeBase64_emptyInput_returnsEmpty() {
        byte[] input = new byte[0];
        byte[] result = Base64.decodeBase64(input);
        assertEquals(0, result.length);
    }

    // Test decodeBase64 with normal input returns correct decoded bytes
    @Test
    public void testDecodeBase64_normalInput_returnsDecoded() {
        byte[] input = "SGVsbG8=".getBytes();
        byte[] expected = "Hello".getBytes();
        assertArrayEquals(expected, Base64.decodeBase64(input));
    }

    // Test decodeBase64 with non-padded input returns correct decoded bytes
    @Test
    public void testDecodeBase64_nonPaddedInput_returnsDecoded() {
        byte[] input = "SGVsbG8".getBytes();
        byte[] expected = "Hello".getBytes();
        assertArrayEquals(expected, Base64.decodeBase64(input));
    }

    // Test decodeBase64 with input containing line separators ignores them
    @Test
    public void testDecodeBase64_chunkedInput_ignoresLineSeparators() {
        byte[] input = "SGVs\r\nbG8=".getBytes();
        byte[] expected = "Hello".getBytes();
        assertArrayEquals(expected, Base64.decodeBase64(input));
    }

    // Test encodeInteger with null input throws NullPointerException
    @Test(expected = NullPointerException.class)
    public void testEncodeInteger_nullInput_throwsNullPointerException() {
        Base64.encodeInteger(null);
    }

    // Test encodeInteger with non-null input returns encoded bytes
    @Test
    public void testEncodeInteger_validInput_returnsEncoded() {
        BigInteger bigInt = BigInteger.valueOf(12345);
        byte[] result = Base64.encodeInteger(bigInt);
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    // Test decodeInteger with valid input returns correct BigInteger
    @Test
    public void testDecodeInteger_validInput_returnsBigInteger() {
        BigInteger original = BigInteger.valueOf(12345);
        byte[] encoded = Base64.encodeInteger(original);
        BigInteger decoded = Base64.decodeInteger(encoded);
        assertEquals(original, decoded);
    }

    // Test isBase64 with valid base64 character returns true
    @Test
    public void testIsBase64_validChar_returnsTrue() {
        assertTrue(Base64.isBase64((byte) 'A'));
        assertTrue(Base64.isBase64((byte) 'z'));
        assertTrue(Base64.isBase64((byte) '0'));
        assertTrue(Base64.isBase64((byte) '+'));
        assertTrue(Base64.isBase64((byte) '/'));
        assertTrue(Base64.isBase64((byte) '='));
    }

    // Test isBase64 with invalid base64 character returns false
    @Test
    public void testIsBase64_invalidChar_returnsFalse() {
        assertFalse(Base64.isBase64((byte) '!'));
        assertFalse(Base64.isBase64((byte) ' '));
        assertFalse(Base64.isBase64((byte) '\n'));
    }

    // Test isArrayByteBase64 with all valid base64 characters returns true
    @Test
    public void testIsArrayByteBase64_validArray_returnsTrue() {
        byte[] valid = "SGVsbG8=".getBytes();
        assertTrue(Base64.isArrayByteBase64(valid));
    }

    // Test isArrayByteBase64 with whitespace characters returns true (treated as valid)
    @Test
    public void testIsArrayByteBase64_arrayWithWhitespace_returnsTrue() {
        byte[] valid = "SGVs bG8=".getBytes();
        assertTrue(Base64.isArrayByteBase64(valid));
    }

    // Test isArrayByteBase64 with invalid characters returns false
    @Test
    public void testIsArrayByteBase64_arrayWithInvalidChar_returnsFalse() {
        byte[] invalid = "SGVs!bG8=".getBytes();
        assertFalse(Base64.isArrayByteBase64(invalid));
    }

    // Test encode with Object parameter throws EncoderException for non-byte[] input
    @Test(expected = EncoderException.class)
    public void testEncodeObject_nonByteArray_throwsEncoderException() throws EncoderException {
        Base64 b64 = new Base64();
        b64.encode("Not a byte array");
    }

    // Test decode with Object parameter throws DecoderException for non-byte[] input
    @Test(expected = DecoderException.class)
    public void testDecodeObject_nonByteArray_throwsDecoderException() throws DecoderException {
        Base64 b64 = new Base64();
        b64.decode("Not a byte array");
    }

    // Test isUrlSafe returns correct value based on constructor
    @Test
    public void testIsUrlSafe_defaultConstructor_returnsFalse() {
        Base64 b64 = new Base64();
        assertFalse(b64.isUrlSafe());
    }

    // Test isUrlSafe returns true when constructed with urlSafe=true
    @Test
    public void testIsUrlSafe_urlSafeConstructor_returnsTrue() {
        Base64 b64 = new Base64(true);
        assertTrue(b64.isUrlSafe());
    }

    // Test constructor with line separator containing base64 character throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_lineSeparatorWithBase64Char_throwsIllegalArgumentException() {
        byte[] badSeparator = new byte[]{'A', '\n'};
        new Base64(76, badSeparator);
    }

    // === NEW TEST CASES ===

    // Test encodeBase64String returns correct String
    @Test
    public void testEncodeBase64String_normalInput_returnsEncodedString() {
        byte[] input = "Hello".getBytes();
        String expected = "SGVsbG8=";
        assertEquals(expected, Base64.encodeBase64String(input));
    }

    // Test decodeBase64 with String input returns correct decoded bytes
    @Test
    public void testDecodeBase64String_normalInput_returnsDecoded() {
        String input = "SGVsbG8=";
        byte[] expected = "Hello".getBytes();
        assertArrayEquals(expected, Base64.decodeBase64(input));
    }

    // Test static encodeBase64 with isChunked=true produces chunked output
    @Test
    public void testEncodeBase64_chunkedTrue_producesChunkedOutput() {
        byte[] input = new byte[100];
        for (int i = 0; i < 100; i++) {
            input[i] = (byte) i;
        }
        byte[] result = Base64.encodeBase64(input, true);
        String resultStr = new String(result);
        assertTrue(resultStr.contains("\r\n"));
    }

    // Test static encodeBase64 with urlSafe=true produces URL-safe output (no +, /, =)
    @Test
    public void testEncodeBase64_urlSafeTrue_producesUrlSafeOutput() {
        byte[] input = new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff};
        byte[] result = Base64.encodeBase64(input, false, true);
        String resultStr = new String(result);
        assertFalse(resultStr.contains("+"));
        assertFalse(resultStr.contains("/"));
        assertFalse(resultStr.contains("="));
    }

    // Test instance encode(byte[]) and decode(byte[]) roundtrip
    @Test
    public void testInstanceEncodeDecode_roundtrip() {
        Base64 b64 = new Base64();
        byte[] original = "Hello World!".getBytes();
        byte[] encoded = b64.encode(original);
        byte[] decoded = b64.decode(encoded);
        assertArrayEquals(original, decoded);
    }

    // Test instance with line length 76 produces chunked output
    @Test
    public void testInstanceEncodeChunked_containsLineSeparator() {
        Base64 b64 = new Base64(76); // line length 76
        byte[] input = new byte[100];
        for (int i = 0; i < 100; i++) {
            input[i] = (byte) i;
        }
        byte[] encoded = b64.encode(input);
        String encodedStr = new String(encoded);
        assertTrue(encodedStr.contains("\r\n"));
    }

    // Test constructor with valid line length and line separator does not throw
    @Test
    public void testConstructor_lineLengthAndSeparator_valid() {
        // Should not throw
        new Base64(76, new byte[]{'\r', '\n'});
        new Base64(64, new byte[]{'\n'});
    }

    // Test decodeInteger with null input throws NullPointerException
    @Test(expected = NullPointerException.class)
    public void testDecodeInteger_nullInput_throwsNullPointerException() {
        Base64.decodeInteger(null);
    }
}