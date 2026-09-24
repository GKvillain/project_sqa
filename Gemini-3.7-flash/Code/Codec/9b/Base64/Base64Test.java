package org.apache.commons.codec.binary;

import java.math.BigInteger;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.junit.Test;

import static org.junit.Assert.*;

public class Base64Test {

    // Tests standard encoding without chunking
    @Test
    public void testEncodeBase64_standardString_returnsEncodedBytes() {
        byte[] input = "Hello World".getBytes();
        byte[] encoded = Base64.encodeBase64(input);
        String result = StringUtils.newStringUtf8(encoded);
        assertEquals("SGVsbG8gV29ybGQ=", result);
    }

    // Tests standard decoding of padded Base64 string
    @Test
    public void testDecodeBase64_validString_returnsDecodedBytes() {
        byte[] decoded = Base64.decodeBase64("SGVsbG8gV29ybGQ=");
        String result = StringUtils.newStringUtf8(decoded);
        assertEquals("Hello World", result);
    }

    // Tests URL-safe encoding replacing + and / with - and _ without padding
    @Test
    public void testEncodeBase64URLSafe_binaryData_returnsUrlSafeEncoding() {
        byte[] input = new byte[]{(byte) 0xfb, (byte) 0xff, (byte) 0xbf};
        String result = Base64.encodeBase64URLSafeString(input);
        assertEquals("----", result);
        assertFalse(result.contains("+"));
        assertFalse(result.contains("/"));
        assertFalse(result.contains("="));
    }

    // Tests chunked encoding with standard 76 char line limit and CRLF
    @Test
    public void testEncodeBase64Chunked_largeData_chunksWithCrlf() {
        byte[] input = new byte[60];
        for (int i = 0; i < input.length; i++) {
            input[i] = (byte) 'A';
        }
        byte[] chunked = Base64.encodeBase64Chunked(input);
        String chunkedStr = StringUtils.newStringUtf8(chunked);
        assertTrue(chunkedStr.contains("\r\n"));
        assertTrue(chunkedStr.endsWith("\r\n"));
    }

    // Tests defect in maxResultSize calculation for non-chunked encoding
    @Test
    public void testEncodeBase64_nonChunkedWithExactMaxSize_encodesSuccessfully() {
        byte[] input = new byte[]{'a', 'b', 'c', 'd'}; // 4 bytes -> 8 bytes base64 encoded
        // Non-chunked requires 8 bytes; chunked requires 10 bytes (8 + CRLF)
        byte[] encoded = Base64.encodeBase64(input, false, false, 8);
        assertNotNull(encoded);
        assertEquals("YWJjZA==", StringUtils.newStringUtf8(encoded));
    }

    // Tests exception thrown when output exceeds maxResultSize
    @Test(expected = IllegalArgumentException.class)
    public void testEncodeBase64_exceedsMaxSize_throwsIllegalArgumentException() {
        byte[] input = "TestDataExceedingSmallMaxSize".getBytes();
        Base64.encodeBase64(input, false, false, 4);
    }

    // Tests null and empty input handling for encode and decode
    @Test
    public void testEncodeDecode_nullAndEmptyInput_returnsNullAndEmpty() {
        assertNull(Base64.encodeBase64(null));
        assertNull(Base64.decodeBase64((byte[]) null));
        assertArrayEquals(new byte[0], Base64.encodeBase64(new byte[0]));
        assertArrayEquals(new byte[0], Base64.decodeBase64(new byte[0]));
    }

    // Tests isBase64 validator on individual bytes and byte arrays
    @Test
    public void testIsBase64_validAndInvalidOctets_returnsCorrectBoolean() {
        assertTrue(Base64.isBase64((byte) 'A'));
        assertTrue(Base64.isBase64((byte) '='));
        assertTrue(Base64.isBase64((byte) ' ')); // whitespace treated as valid in isBase64(byte[])
        assertFalse(Base64.isBase64((byte) '$'));
        assertTrue(Base64.isBase64("SGVsbG8="));
        assertFalse(Base64.isBase64("SGVsbG8=$"));
    }

    // Tests isArrayByteBase64 deprecated alias
    @Test
    public void testIsArrayByteBase64_validArray_returnsTrue() {
        byte[] valid = StringUtils.getBytesUtf8("SGVsbG8=");
        assertTrue(Base64.isArrayByteBase64(valid));
    }

    // Tests constructor validation when lineSeparator contains Base64 characters
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_base64CharInSeparator_throwsIllegalArgumentException() {
        new Base64(76, new byte[]{'A', '\n'});
    }

    // Tests encode and decode via Object interface
    @Test
    public void testEncodeDecodeObject_validTypes_succeeds() throws Exception {
        Base64 base64 = new Base64();
        Object encoded = base64.encode((Object) "Hello".getBytes());
        assertTrue(encoded instanceof byte[]);
        Object decoded = base64.decode(encoded);
        assertTrue(decoded instanceof byte[]);
        assertEquals("Hello", StringUtils.newStringUtf8((byte[]) decoded));
    }

    // Tests EncoderException on invalid object type
    @Test(expected = EncoderException.class)
    public void testEncode_invalidObjectType_throwsEncoderException() throws Exception {
        Base64 base64 = new Base64();
        base64.encode(Integer.valueOf(123));
    }

    // Tests DecoderException on invalid object type
    @Test(expected = DecoderException.class)
    public void testDecode_invalidObjectType_throwsDecoderException() throws Exception {
        Base64 base64 = new Base64();
        base64.decode(Integer.valueOf(123));
    }

    // Tests encodeInteger and decodeInteger for BigInteger support
    @Test
    public void testEncodeDecodeInteger_validBigInteger_returnsMatchingValue() {
        BigInteger original = new BigInteger("12345678901234567890");
        byte[] encoded = Base64.encodeInteger(original);
        BigInteger decoded = Base64.decodeInteger(encoded);
        assertEquals(original, decoded);
    }

    // Tests encodeInteger with null parameter throwing NullPointerException
    @Test(expected = NullPointerException.class)
    public void testEncodeInteger_nullInput_throwsNullPointerException() {
        Base64.encodeInteger(null);
    }

    // Tests discardWhitespace utility method
    @Test
    public void testDiscardWhitespace_stringWithWhitespace_removesWhitespace() {
        byte[] input = StringUtils.getBytesUtf8(" S G V s \r\n b G 8 = \t");
        byte[] expected = StringUtils.getBytesUtf8("SGVsbG8=");
        assertArrayEquals(expected, Base64.discardWhitespace(input));
    }

    // Tests isUrlSafe property for different constructors
    @Test
    public void testIsUrlSafe_variousConstructors_reflectsMode() {
        Base64 defaultB64 = new Base64();
        assertFalse(defaultB64.isUrlSafe());

        Base64 urlSafeB64 = new Base64(true);
        assertTrue(urlSafeB64.isUrlSafe());
    }

    // Tests decoding input with partial modulus (1, 2, or 3 remaining bytes without padding)
    @Test
    public void testDecode_unpaddedInputs_decodesCorrectly() {
        Base64 base64 = new Base64();
        // "YWJj" -> "abc" (modulus 0)
        assertEquals("abc", StringUtils.newStringUtf8(base64.decode("YWJj")));
        // "YWI" -> "ab" (modulus 3, 18 bits)
        assertEquals("ab", StringUtils.newStringUtf8(base64.decode("YWI")));
        // "YQ" -> "a" (modulus 2, 12 bits)
        assertEquals("a", StringUtils.newStringUtf8(base64.decode("YQ")));
    }
}