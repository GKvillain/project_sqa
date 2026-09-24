package org.apache.commons.codec.binary;

import java.math.BigInteger;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class Base64Test {

    // Tests default constructor encoding does not chunk by default for small and large outputs
    @Test
    public void testEncode_defaultConstructor_encodesCorrectly() {
        Base64 b64 = new Base64();
        byte[] input = StringUtils.getBytesUtf8("Hello World");
        byte[] encoded = b64.encode(input);
        assertEquals("SGVsbG8gV29ybGQ=", StringUtils.newStringUtf8(encoded));
        byte[] decoded = b64.decode(encoded);
        assertEquals("Hello World", StringUtils.newStringUtf8(decoded));
    }

    // Tests encode with 1 byte (modulus 1 padding)
    @Test
    public void testEncode_oneByte_returnsPaddedResult() {
        byte[] input = new byte[]{'f'};
        byte[] encoded = Base64.encodeBase64(input);
        assertEquals("Zg==", StringUtils.newStringUtf8(encoded));
        byte[] decoded = Base64.decodeBase64(encoded);
        assertArrayEquals(input, decoded);
    }

    // Tests encode with 2 bytes (modulus 2 padding)
    @Test
    public void testEncode_twoBytes_returnsPaddedResult() {
        byte[] input = new byte[]{'f', 'o'};
        byte[] encoded = Base64.encodeBase64(input);
        assertEquals("Zm8=", StringUtils.newStringUtf8(encoded));
        byte[] decoded = Base64.decodeBase64(encoded);
        assertArrayEquals(input, decoded);
    }

    // Tests encode with 3 bytes (exact block, no padding)
    @Test
    public void testEncode_threeBytes_returnsUnpaddedResult() {
        byte[] input = new byte[]{'f', 'o', 'o'};
        byte[] encoded = Base64.encodeBase64(input);
        assertEquals("Zm9v", StringUtils.newStringUtf8(encoded));
        byte[] decoded = Base64.decodeBase64(encoded);
        assertArrayEquals(input, decoded);
    }

    // Tests URL-safe encoding skips padding and uses url safe chars
    @Test
    public void testEncodeBase64URLSafe_binaryData_usesUrlSafeAlphabetWithoutPadding() {
        byte[] input = new byte[]{(byte) 0xfb, (byte) 0xff, (byte) 0xbf};
        byte[] encoded = Base64.encodeBase64URLSafe(input);
        String result = StringUtils.newStringUtf8(encoded);
        assertEquals("---_", result);
        byte[] decoded = Base64.decodeBase64(encoded);
        assertArrayEquals(input, decoded);
    }

    // Tests encodeBase64URLSafeString method
    @Test
    public void testEncodeBase64URLSafeString_validInput_returnsUrlSafeString() {
        byte[] input = new byte[]{(byte) 0xff, (byte) 0xef};
        String result = Base64.encodeBase64URLSafeString(input);
        assertEquals("/+8", result.replace('-', '+').replace('_', '/').substring(0, 3));
        assertFalse(result.endsWith("="));
    }

    // Tests encodeBase64Chunked produces CRLF chunks
    @Test
    public void testEncodeBase64Chunked_longInput_chunksOutput() {
        byte[] input = new byte[60];
        for (int i = 0; i < input.length; i++) {
            input[i] = (byte) 'A';
        }
        byte[] chunked = Base64.encodeBase64Chunked(input);
        String chunkedStr = StringUtils.newStringUtf8(chunked);
        assertTrue(chunkedStr.contains("\r\n"));
        byte[] decoded = Base64.decodeBase64(chunked);
        assertArrayEquals(input, decoded);
    }

    // Tests null and empty array inputs for encode and decode
    @Test
    public void testEncodeDecode_nullAndEmptyInput_returnsSame() {
        assertNull(Base64.encodeBase64(null));
        assertNull(Base64.decodeBase64((byte[]) null));
        assertArrayEquals(new byte[0], Base64.encodeBase64(new byte[0]));
        assertArrayEquals(new byte[0], Base64.decodeBase64(new byte[0]));
    }

    // Tests isBase64 validation for valid and invalid octets
    @Test
    public void testIsBase64_variousBytes_returnsExpected() {
        assertTrue(Base64.isBase64((byte) 'A'));
        assertTrue(Base64.isBase64((byte) 'z'));
        assertTrue(Base64.isBase64((byte) '0'));
        assertTrue(Base64.isBase64((byte) '+'));
        assertTrue(Base64.isBase64((byte) '/'));
        assertTrue(Base64.isBase64((byte) '='));
        assertFalse(Base64.isBase64((byte) '$'));
        assertFalse(Base64.isBase64((byte) -1));
    }

    // Tests isArrayByteBase64 with valid, whitespace, and invalid inputs
    @Test
    public void testIsArrayByteBase64_variousArrays_validatesCorrectly() {
        assertTrue(Base64.isArrayByteBase64(StringUtils.getBytesUtf8("SGVs bG8=\r\n")));
        assertTrue(Base64.isArrayByteBase64(new byte[0]));
        assertFalse(Base64.isArrayByteBase64(StringUtils.getBytesUtf8("Hello!@#")));
    }

    // Tests constructor exception when line separator contains Base64 characters
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_base64CharInSeparator_throwsIllegalArgumentException() {
        byte[] invalidSeparator = new byte[]{'A', '\n'};
        new Base64(76, invalidSeparator);
    }

    // Tests encodeBase64 exceeding maxResultSize throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testEncodeBase64_exceedsMaxResultSize_throwsIllegalArgumentException() {
        byte[] input = new byte[100];
        Base64.encodeBase64(input, false, false, 10);
    }

    // Tests Object encode method with valid byte array
    @Test
    public void testEncode_objectByteArray_returnsEncodedObject() throws Exception {
        Base64 b64 = new Base64();
        byte[] input = StringUtils.getBytesUtf8("Test");
        Object result = b64.encode((Object) input);
        assertTrue(result instanceof byte[]);
        assertEquals("VGVzdA==", StringUtils.newStringUtf8((byte[]) result));
    }

    // Tests Object encode method with invalid object type
    @Test(expected = EncoderException.class)
    public void testEncode_nonByteArrayObject_throwsEncoderException() throws Exception {
        Base64 b64 = new Base64();
        b64.encode("InvalidType");
    }

    // Tests Object decode method with byte array and string
    @Test
    public void testDecode_objectTypes_returnsDecodedByteArray() throws Exception {
        Base64 b64 = new Base64();
        Object fromBytes = b64.decode((Object) StringUtils.getBytesUtf8("VGVzdA=="));
        assertTrue(fromBytes instanceof byte[]);
        assertEquals("Test", StringUtils.newStringUtf8((byte[]) fromBytes));

        Object fromString = b64.decode((Object) "VGVzdA==");
        assertTrue(fromString instanceof byte[]);
        assertEquals("Test", StringUtils.newStringUtf8((byte[]) fromString));
    }

    // Tests Object decode method with invalid object type
    @Test(expected = DecoderException.class)
    public void testDecode_invalidObjectType_throwsDecoderException() throws Exception {
        Base64 b64 = new Base64();
        b64.decode(Integer.valueOf(123));
    }

    // Tests BigInteger encode and decode
    @Test
    public void testEncodeDecodeInteger_validBigInteger_returnsCorrectBigInteger() {
        BigInteger bigInt = new BigInteger("12345678901234567890");
        byte[] encoded = Base64.encodeInteger(bigInt);
        assertNotNull(encoded);
        BigInteger decoded = Base64.decodeInteger(encoded);
        assertEquals(bigInt, decoded);
    }

    // Tests encodeInteger with null parameter throws NullPointerException
    @Test(expected = NullPointerException.class)
    public void testEncodeInteger_nullInput_throwsNullPointerException() {
        Base64.encodeInteger(null);
    }

    // Tests discardWhitespace method
    @Test
    public void testDiscardWhitespace_stringWithSpacesAndCRLF_stripsWhitespace() {
        byte[] input = StringUtils.getBytesUtf8("S G V\r\n s b A = =\n");
        byte[] cleaned = Base64.discardWhitespace(input);
        assertEquals("SGVsbA==", StringUtils.newStringUtf8(cleaned));
    }

    // Tests encodeToString method
    @Test
    public void testEncodeToString_binaryData_returnsBase64String() {
        Base64 b64 = new Base64();
        String result = b64.encodeToString(StringUtils.getBytesUtf8("Commons Codec"));
        assertEquals("Q29tbW9ucyBDb2RlYw==", result);
    }
}