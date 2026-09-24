package org.apache.commons.codec.binary;

import java.math.BigInteger;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.junit.Test;
import static org.junit.Assert.*;

public class Base64Test {

    // Tests standard encode and decode roundtrip
    @Test
    public void testEncodeDecode_standardString_returnsOriginal() {
        String input = "Hello World";
        byte[] encoded = Base64.encodeBase64(StringUtils.getBytesUtf8(input));
        byte[] decoded = Base64.decodeBase64(encoded);
        assertEquals(input, StringUtils.newStringUtf8(decoded));
    }

    // Tests encode with no padding (length multiple of 3)
    @Test
    public void testEncode_lengthMultipleOf3_noPadding() {
        byte[] input = StringUtils.getBytesUtf8("123");
        byte[] encoded = Base64.encodeBase64(input);
        assertEquals("MTIz", StringUtils.newStringUtf8(encoded));
    }

    // Tests encode with 1 padding char (modulus 2)
    @Test
    public void testEncode_modulus2_onePadCharacter() {
        byte[] input = StringUtils.getBytesUtf8("12345");
        byte[] encoded = Base64.encodeBase64(input);
        assertEquals("MTIzNDU=", StringUtils.newStringUtf8(encoded));
    }

    // Tests encode with 2 padding chars (modulus 1)
    @Test
    public void testEncode_modulus1_twoPadCharacters() {
        byte[] input = StringUtils.getBytesUtf8("1234");
        byte[] encoded = Base64.encodeBase64(input);
        assertEquals("MTIzNA==", StringUtils.newStringUtf8(encoded));
    }

    // Tests URL-safe encode and decode
    @Test
    public void testEncodeBase64URLSafe_specialChars_usesUrlSafeAlphabetWithoutPadding() {
        byte[] input = new byte[]{(byte) 0xfb, (byte) 0xff, (byte) 0xbf};
        byte[] urlSafeEncoded = Base64.encodeBase64URLSafe(input);
        String result = StringUtils.newStringUtf8(urlSafeEncoded);
        assertFalse(result.contains("+"));
        assertFalse(result.contains("/"));
        assertFalse(result.contains("="));
        byte[] decoded = Base64.decodeBase64(urlSafeEncoded);
        assertArrayEquals(input, decoded);
    }

    // Tests encodeBase64String
    @Test
    public void testEncodeBase64String_validInput_returnsBase64String() {
        byte[] input = StringUtils.getBytesUtf8("Hello World!");
        String encoded = Base64.encodeBase64String(input);
        assertNotNull(encoded);
        byte[] decoded = Base64.decodeBase64(encoded);
        assertEquals("Hello World!", StringUtils.newStringUtf8(decoded));
    }

    // Tests encodeBase64URLSafeString
    @Test
    public void testEncodeBase64URLSafeString_validInput_returnsUrlSafeString() {
        byte[] input = new byte[]{(byte) 0xff, (byte) 0xee, (byte) 0xdd};
        String encoded = Base64.encodeBase64URLSafeString(input);
        assertFalse(encoded.contains("+"));
        assertFalse(encoded.contains("/"));
        assertArrayEquals(input, Base64.decodeBase64(encoded));
    }

    // Tests null and empty input handling for encode and decode
    @Test
    public void testEncodeDecode_nullAndEmpty_returnsSame() {
        assertNull(Base64.encodeBase64(null));
        assertNull(Base64.decodeBase64((byte[]) null));
        assertArrayEquals(new byte[0], Base64.encodeBase64(new byte[0]));
        assertArrayEquals(new byte[0], Base64.decodeBase64(new byte[0]));
    }

    // Tests chunked encoding with standard line length (76) and CRLF
    @Test
    public void testEncodeBase64Chunked_largeData_chunksWithCRLF() {
        byte[] input = new byte[100];
        for (int i = 0; i < input.length; i++) {
            input[i] = (byte) i;
        }
        byte[] chunked = Base64.encodeBase64Chunked(input);
        String chunkedStr = StringUtils.newStringUtf8(chunked);
        assertTrue(chunkedStr.contains("\r\n"));
        byte[] decoded = Base64.decodeBase64(chunked);
        assertArrayEquals(input, decoded);
    }

    // Tests custom lineLength and separator constructor
    @Test
    public void testConstructor_customLineLengthAndSeparator_formatsCorrectly() {
        byte[] separator = new byte[]{';'};
        Base64 b64 = new Base64(4, separator);
        byte[] input = StringUtils.getBytesUtf8("123456");
        byte[] encoded = b64.encode(input);
        String result = StringUtils.newStringUtf8(encoded);
        assertTrue(result.contains(";"));
        byte[] decoded = b64.decode(encoded);
        assertEquals("123456", StringUtils.newStringUtf8(decoded));
    }

    // Tests line separator containing base64 character throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_lineSeparatorContainsBase64_throwsException() {
        byte[] invalidSeparator = new byte[]{'A'};
        new Base64(76, invalidSeparator);
    }

    // Tests isBase64 method with valid and invalid octets
    @Test
    public void testIsBase64_variousOctets_returnsCorrectBoolean() {
        assertTrue(Base64.isBase64((byte) 'A'));
        assertTrue(Base64.isBase64((byte) 'z'));
        assertTrue(Base64.isBase64((byte) '0'));
        assertTrue(Base64.isBase64((byte) '+'));
        assertTrue(Base64.isBase64((byte) '/'));
        assertTrue(Base64.isBase64((byte) '='));
        assertFalse(Base64.isBase64((byte) '$'));
        assertFalse(Base64.isBase64((byte) -1));
    }

    // Tests isArrayByteBase64
    @Test
    public void testIsArrayByteBase64_validAndInvalidArrays_returnsCorrectBoolean() {
        assertTrue(Base64.isArrayByteBase64(StringUtils.getBytesUtf8("TWFu\r\n")));
        assertTrue(Base64.isArrayByteBase64(new byte[0]));
        assertFalse(Base64.isArrayByteBase64(StringUtils.getBytesUtf8("Invalid byte: @")));
    }

    // Tests Object encode with valid and invalid type
    @Test
    public void testEncodeObject_validAndInvalidType() throws Exception {
        Base64 b64 = new Base64();
        byte[] input = StringUtils.getBytesUtf8("test");
        Object result = b64.encode((Object) input);
        assertTrue(result instanceof byte[]);
        assertArrayEquals(Base64.encodeBase64(input), (byte[]) result);
    }

    // Tests Object encode with invalid type throws EncoderException
    @Test(expected = EncoderException.class)
    public void testEncodeObject_invalidType_throwsEncoderException() throws Exception {
        Base64 b64 = new Base64();
        b64.encode("StringNotAllowed");
    }

    // Tests Object decode with String, byte[], and invalid type
    @Test
    public void testDecodeObject_variousTypes() throws Exception {
        Base64 b64 = new Base64();
        byte[] expected = StringUtils.getBytesUtf8("Hello");
        byte[] encoded = Base64.encodeBase64(expected);
        
        assertArrayEquals(expected, (byte[]) b64.decode((Object) encoded));
        assertArrayEquals(expected, (byte[]) b64.decode((Object) StringUtils.newStringUtf8(encoded)));
    }

    // Tests Object decode with invalid type throws DecoderException
    @Test(expected = DecoderException.class)
    public void testDecodeObject_invalidType_throwsDecoderException() throws Exception {
        Base64 b64 = new Base64();
        b64.decode(Integer.valueOf(123));
    }

    // Tests encodeBase64 exceeding maxResultSize throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testEncodeBase64_exceedsMaxResultSize_throwsException() {
        byte[] input = StringUtils.getBytesUtf8("Test data to encode");
        Base64.encodeBase64(input, false, false, 4);
    }

    // Tests discardWhitespace method
    @Test
    public void testDiscardWhitespace_stringWithWhitespace_removesWhitespace() {
        byte[] input = StringUtils.getBytesUtf8(" T \n W \r F \t u ");
        byte[] groomed = Base64.discardWhitespace(input);
        assertEquals("TWFu", StringUtils.newStringUtf8(groomed));
    }

    // Tests BigInteger encode and decode
    @Test
    public void testEncodeDecodeInteger_validBigInteger_roundtripsCorrectly() {
        BigInteger bigInt = new BigInteger("12345678901234567890");
        byte[] encoded = Base64.encodeInteger(bigInt);
        BigInteger decoded = Base64.decodeInteger(encoded);
        assertEquals(bigInt, decoded);
    }

    // Tests encodeInteger with null throws NullPointerException
    @Test(expected = NullPointerException.class)
    public void testEncodeInteger_null_throwsNullPointerException() {
        Base64.encodeInteger(null);
    }

    // Tests instance encodeToString method
    @Test
    public void testEncodeToString_validInput_returnsString() {
        Base64 b64 = new Base64();
        byte[] input = StringUtils.getBytesUtf8("Sample");
        String encoded = b64.encodeToString(input);
        assertEquals("U2FtcGxl", encoded);
    }

    // Tests isUrlSafe method
    @Test
    public void testIsUrlSafe_variousConstructors_returnsCorrectState() {
        Base64 defaultB64 = new Base64();
        assertFalse(defaultB64.isUrlSafe());

        Base64 urlSafeB64 = new Base64(true);
        assertTrue(urlSafeB64.isUrlSafe());
    }
}