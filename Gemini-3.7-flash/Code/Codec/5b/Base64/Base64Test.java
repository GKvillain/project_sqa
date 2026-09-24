package org.apache.commons.codec.binary;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.junit.Test;

public class Base64Test {

    // Tests standard encode with simple ASCII string
    @Test
    public void testEncode_standardString_returnsBase64() {
        byte[] input = "Hello World".getBytes();
        byte[] encoded = Base64.encodeBase64(input);
        assertEquals("SGVsbG8gV29ybGQ=", StringUtils.newStringUtf8(encoded));
    }

    // Tests standard decode with simple Base64 string
    @Test
    public void testDecode_standardBase64_returnsOriginalBytes() {
        byte[] encoded = "SGVsbG8gV29ybGQ=".getBytes();
        byte[] decoded = Base64.decodeBase64(encoded);
        assertEquals("Hello World", StringUtils.newStringUtf8(decoded));
    }

    // Tests decode with string input
    @Test
    public void testDecodeBase64_stringInput_returnsOriginalBytes() {
        byte[] decoded = Base64.decodeBase64("SGVsbG8gV29ybGQ=");
        assertEquals("Hello World", StringUtils.newStringUtf8(decoded));
    }

    // Tests URL-safe encode and decode
    @Test
    public void testEncodeBase64URLSafe_binaryData_returnsUrlSafeWithoutPadding() {
        byte[] input = new byte[]{(byte) 0xfb, (byte) 0xff, (byte) 0xbf};
        byte[] encoded = Base64.encodeBase64URLSafe(input);
        String encodedStr = StringUtils.newStringUtf8(encoded);
        assertEquals("-_-/", encodedStr.replace('/', '_').replace('+', '-'));
        assertFalse(encodedStr.contains("+"));
        assertFalse(encodedStr.contains("/"));
        assertFalse(encodedStr.contains("="));
        
        byte[] decoded = Base64.decodeBase64(encoded);
        assertArrayEquals(input, decoded);
    }

    // Tests chunked encode with line separator
    @Test
    public void testEncodeBase64Chunked_largeData_chunksOutput() {
        byte[] input = new byte[100];
        for (int i = 0; i < input.length; i++) {
            input[i] = (byte) i;
        }
        byte[] chunked = Base64.encodeBase64Chunked(input);
        String chunkedStr = StringUtils.newStringUtf8(chunked);
        assertTrue(chunkedStr.contains("\r\n"));
        assertTrue(chunkedStr.endsWith("\r\n"));

        byte[] decoded = Base64.decodeBase64(chunked);
        assertArrayEquals(input, decoded);
    }

    // Tests streaming decode when buffer is initially null and EOF has modulus != 0 (Codec-5 defect target)
    @Test
    public void testDecode_streamingWithModulus2AndNullBuffer_decodesWithoutNullPointer() {
        Base64 base64 = new Base64();
        byte[] input = "TQ==".getBytes();
        // Feed partial bytes without using decode(byte[]) so initialBuffer is not set
        base64.decode(input, 0, 2);
        base64.decode(input, 0, -1); // triggers EOF with modulus 2
        assertTrue(base64.hasData());
        byte[] result = new byte[base64.avail()];
        int read = base64.readResults(result, 0, result.length);
        assertEquals(1, read);
        assertEquals("M", StringUtils.newStringUtf8(result));
    }

    // Tests streaming decode with modulus 3 and EOF
    @Test
    public void testDecode_streamingWithModulus3AndNullBuffer_decodesWithoutNullPointer() {
        Base64 base64 = new Base64();
        byte[] input = "TWE=".getBytes();
        base64.decode(input, 0, 3);
        base64.decode(input, 0, -1); // triggers EOF with modulus 3
        assertTrue(base64.hasData());
        byte[] result = new byte[base64.avail()];
        int read = base64.readResults(result, 0, result.length);
        assertEquals(2, read);
        assertEquals("Ma", StringUtils.newStringUtf8(result));
    }

    // Tests null and empty input for encode and decode
    @Test
    public void testEncodeDecode_nullAndEmpty_returnsSame() {
        assertNull(Base64.encodeBase64(null));
        assertNull(Base64.decodeBase64((byte[]) null));
        assertNull(Base64.decodeBase64((String) null));

        assertArrayEquals(new byte[0], Base64.encodeBase64(new byte[0]));
        assertArrayEquals(new byte[0], Base64.decodeBase64(new byte[0]));
    }

    // Tests constructor validation when lineSeparator contains base64 characters
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_lineSeparatorContainsBase64_throwsIllegalArgumentException() {
        byte[] invalidSeparator = new byte[]{'A', '\n'};
        new Base64(76, invalidSeparator);
    }

    // Tests encodeBase64 with maxResultSize exceeded
    @Test(expected = IllegalArgumentException.class)
    public void testEncodeBase64_exceedsMaxResultSize_throwsIllegalArgumentException() {
        byte[] input = "Input data to exceed size limit".getBytes();
        Base64.encodeBase64(input, false, false, 5);
    }

    // Tests Object decode and encode for valid and invalid types
    @Test
    public void testObjectEncodeDecode_validAndInvalidTypes() throws Exception {
        Base64 base64 = new Base64();
        
        Object encodedObj = base64.encode((Object) "Test".getBytes());
        assertTrue(encodedObj instanceof byte[]);
        
        Object decodedObjBytes = base64.decode(encodedObj);
        assertArrayEquals("Test".getBytes(), (byte[]) decodedObjBytes);

        Object decodedObjStr = base64.decode((Object) "VGVzdA==");
        assertArrayEquals("Test".getBytes(), (byte[]) decodedObjStr);
    }

    // Tests Object encode with invalid type throws EncoderException
    @Test(expected = EncoderException.class)
    public void testEncode_invalidObjectType_throwsEncoderException() throws Exception {
        Base64 base64 = new Base64();
        base64.encode("StringObjectNotByteArray");
    }

    // Tests Object decode with invalid type throws DecoderException
    @Test(expected = DecoderException.class)
    public void testDecode_invalidObjectType_throwsDecoderException() throws Exception {
        Base64 base64 = new Base64();
        base64.decode(Integer.valueOf(123));
    }

    // Tests isBase64 and isArrayByteBase64 methods
    @Test
    public void testIsBase64AndIsArrayByteBase64_variousInputs() {
        assertTrue(Base64.isBase64((byte) 'A'));
        assertTrue(Base64.isBase64((byte) 'z'));
        assertTrue(Base64.isBase64((byte) '0'));
        assertTrue(Base64.isBase64((byte) '+'));
        assertTrue(Base64.isBase64((byte) '/'));
        assertTrue(Base64.isBase64((byte) '-'));
        assertTrue(Base64.isBase64((byte) '_'));
        assertTrue(Base64.isBase64((byte) '='));
        assertFalse(Base64.isBase64((byte) '$'));
        assertFalse(Base64.isBase64((byte) -1));

        assertTrue(Base64.isArrayByteBase64(new byte[]{'S', 'G', 'V', 's', 'b', 'G', '8', '='}));
        assertTrue(Base64.isArrayByteBase64(new byte[]{'S', ' ', '\r', '\n', '\t', 'G'}));
        assertFalse(Base64.isArrayByteBase64(new byte[]{'S', 'G', '!', '8'}));
    }

    // Tests BigInteger encode and decode
    @Test
    public void testBigIntegerEncodeDecode_validInteger_reconstructsBigInteger() {
        BigInteger bigInt = new BigInteger("12345678901234567890");
        byte[] encoded = Base64.encodeInteger(bigInt);
        assertNotNull(encoded);
        BigInteger decoded = Base64.decodeInteger(encoded);
        assertEquals(bigInt, decoded);
    }

    // Tests encodeInteger with null parameter throws NullPointerException
    @Test(expected = NullPointerException.class)
    public void testEncodeInteger_nullParam_throwsNullPointerException() {
        Base64.encodeInteger(null);
    }

    // Tests encodeToString and encodeBase64String helper methods
    @Test
    public void testEncodeToString_validData_returnsExpectedBase64String() {
        Base64 base64 = new Base64();
        String result = base64.encodeToString("Hello".getBytes());
        assertEquals("SGVsbG8=", result);

        String chunkedStr = Base64.encodeBase64String("Hello".getBytes());
        assertEquals("SGVsbG8=\r\n", chunkedStr);

        String urlSafeStr = Base64.encodeBase64URLSafeString(new byte[]{(byte) 0xfb, (byte) 0xf0});
        assertEquals("-_A", urlSafeStr);
    }

    // Tests discardWhitespace deprecated method
    @Test
    public void testDiscardWhitespace_withSpacesAndNewlines_removesWhitespace() {
        byte[] input = "S G\r\nV\tsb G 8=".getBytes();
        byte[] groomed = Base64.discardWhitespace(input);
        assertEquals("SGVsbG8=", StringUtils.newStringUtf8(groomed));
    }
}