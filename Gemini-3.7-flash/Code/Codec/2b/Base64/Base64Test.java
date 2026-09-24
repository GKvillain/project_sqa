package org.apache.commons.codec.binary;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Arrays;

import static org.junit.Assert.*;

public class Base64Test {

    // Tests encoding empty byte array returns empty array
    @Test
    public void testEncodeBase64_emptyInput_returnsEmptyArray() {
        byte[] input = new byte[0];
        byte[] encoded = Base64.encodeBase64(input);
        assertNotNull(encoded);
        assertEquals(0, encoded.length);
    }

    // Tests encoding null byte array returns null
    @Test
    public void testEncodeBase64_nullInput_returnsNull() {
        byte[] encoded = Base64.encodeBase64(null);
        assertNull(encoded);
    }

    // Tests basic 1-byte, 2-byte, and 3-byte standard encoding
    @Test
    public void testEncodeBase64_standardData_returnsCorrectBase64() {
        // "f" -> "Zg=="
        byte[] b1 = "f".getBytes();
        assertEquals("Zg==", new String(Base64.encodeBase64(b1)));

        // "fo" -> "Zm8="
        byte[] b2 = "fo".getBytes();
        assertEquals("Zm8=", new String(Base64.encodeBase64(b2)));

        // "foo" -> "Zm9v"
        byte[] b3 = "foo".getBytes();
        assertEquals("Zm9v", new String(Base64.encodeBase64(b3)));
    }

    // Tests basic decoding with and without padding
    @Test
    public void testDecodeBase64_standardData_returnsOriginalBytes() {
        byte[] decoded1 = Base64.decodeBase64("Zg==".getBytes());
        assertEquals("f", new String(decoded1));

        byte[] decoded2 = Base64.decodeBase64("Zm8=".getBytes());
        assertEquals("fo", new String(decoded2));

        byte[] decoded3 = Base64.decodeBase64("Zm9v".getBytes());
        assertEquals("foo", new String(decoded3));

        // Decoding without padding
        byte[] decodedNoPad = Base64.decodeBase64("Zg".getBytes());
        assertEquals("f", new String(decodedNoPad));
    }

    // Tests decoding empty or null data
    @Test
    public void testDecodeBase64_emptyAndNullInput_returnsEmptyOrNull() {
        assertNull(Base64.decodeBase64(null));
        assertEquals(0, Base64.decodeBase64(new byte[0]).length);
    }

    // Tests URL-safe encoding without padding characters
    @Test
    public void testEncodeBase64URLSafe_binaryWithPlusAndSlash_emitsDashAndUnderscore() {
        // Binary byte sequences that result in '+' and '/' in standard base64
        byte[] data = new byte[]{(byte) 0xfb, (byte) 0xff, (byte) 0xbf};
        byte[] standard = Base64.encodeBase64(data, false, false);
        assertEquals("----", new String(standard).replace('+', '-').replace('/', '_'));

        byte[] urlSafe = Base64.encodeBase64URLSafe(data);
        assertEquals("----", new String(urlSafe));
        assertFalse(new String(urlSafe).contains("+"));
        assertFalse(new String(urlSafe).contains("/"));
        assertFalse(new String(urlSafe).contains("="));
    }

    // Tests chunked encoding per RFC 2045 (76 character chunks followed by CRLF)
    @Test
    public void testEncodeBase64Chunked_longInput_splitsWithCrlf() {
        byte[] input = new byte[60]; // 60 bytes = 80 base64 chars -> 76 chars + CRLF + 4 chars + CRLF
        Arrays.fill(input, (byte) 'A');
        byte[] chunked = Base64.encodeBase64Chunked(input);
        String chunkedStr = new String(chunked);
        assertTrue(chunkedStr.contains("\r\n"));
        assertTrue(chunkedStr.endsWith("\r\n"));
        assertEquals(76 + 2 + 4 + 2, chunked.length);
    }

    // Tests streaming encode and decode methods directly
    @Test
    public void testStreamingEncodeAndDecode_multiStep_decodesCorrectly() {
        Base64 b64 = new Base64();
        byte[] input = "Hello, World!".getBytes();
        b64.encode(input, 0, input.length);
        b64.encode(input, 0, -1); // EOF

        assertTrue(b64.hasData());
        int avail = b64.avail();
        assertTrue(avail > 0);

        byte[] encoded = new byte[avail];
        int read = b64.readResults(encoded, 0, avail);
        assertEquals(avail, read);

        Base64 decoder = new Base64();
        decoder.decode(encoded, 0, encoded.length);
        decoder.decode(encoded, 0, -1); // EOF

        byte[] decoded = new byte[decoder.avail()];
        decoder.readResults(decoded, 0, decoded.length);
        assertEquals("Hello, World!", new String(decoded));
    }

    // Tests streaming behavior when EOF is sent with no data in chunked mode (Regression check for Codec-2)
    @Test
    public void testStreamingEncode_emptyInputWithChunking_noOutputWritten() {
        Base64 b64 = new Base64(76, Base64.CHUNK_SEPARATOR, false);
        b64.encode(new byte[0], 0, -1);
        byte[] out = new byte[10];
        int bytesRead = b64.readResults(out, 0, out.length);
        assertEquals(-1, bytesRead);
    }

    // Tests invalid lineSeparator containing Base64 characters in constructor throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_lineSeparatorContainsBase64Char_throwsException() {
        new Base64(76, new byte[]{'A', '\n'});
    }

    // Tests isBase64 and isArrayByteBase64 checks
    @Test
    public void testIsBase64_variousBytes_returnsExpectedBoolean() {
        assertTrue(Base64.isBase64((byte) 'A'));
        assertTrue(Base64.isBase64((byte) 'z'));
        assertTrue(Base64.isBase64((byte) '0'));
        assertTrue(Base64.isBase64((byte) '+'));
        assertTrue(Base64.isBase64((byte) '/'));
        assertTrue(Base64.isBase64((byte) '='));
        assertFalse(Base64.isBase64((byte) '$'));
        assertFalse(Base64.isBase64((byte) -5));

        assertTrue(Base64.isArrayByteBase64("Zm9v\r\n".getBytes()));
        assertFalse(Base64.isArrayByteBase64("Zm9v$bar".getBytes()));
    }

    // Tests discarding whitespace and non-base64 characters
    @Test
    public void testDiscardWhitespaceAndNonBase64_dirtyInput_removesUnwantedBytes() {
        byte[] withWhitespace = " Z m 9 v \r\n ".getBytes();
        byte[] cleanWhitespace = Base64.discardWhitespace(withWhitespace);
        assertEquals("Zm9v", new String(cleanWhitespace));

        byte[] withNonBase64 = "Z#m$9%v&".getBytes();
        byte[] cleanNonBase64 = Base64.discardNonBase64(withNonBase64);
        assertEquals("Zm9v", new String(cleanNonBase64));
    }

    // Tests Object encode/decode methods implementing Encoder/Decoder interface
    @Test
    public void testObjectEncodeDecode_validByteArray_success() throws Exception {
        Base64 b64 = new Base64();
        byte[] input = "Test Message".getBytes();
        Object encodedObj = b64.encode((Object) input);
        assertTrue(encodedObj instanceof byte[]);

        Object decodedObj = b64.decode(encodedObj);
        assertTrue(decodedObj instanceof byte[]);
        assertArrayEquals(input, (byte[]) decodedObj);
    }

    // Tests Object encode throws EncoderException for non-byte[] input
    @Test(expected = EncoderException.class)
    public void testEncodeObject_nonByteArray_throwsEncoderException() throws Exception {
        Base64 b64 = new Base64();
        b64.encode("String input not byte array");
    }

    // Tests Object decode throws DecoderException for non-byte[] input
    @Test(expected = DecoderException.class)
    public void testDecodeObject_nonByteArray_throwsDecoderException() throws Exception {
        Base64 b64 = new Base64();
        b64.decode("String input not byte array");
    }

    // Tests BigInteger encodeInteger and decodeInteger
    @Test
    public void testBigIntegerEncodeDecode_validBigInteger_roundTrips() {
        BigInteger original = new BigInteger("123456789012345678901234567890");
        byte[] encoded = Base64.encodeInteger(original);
        BigInteger decoded = Base64.decodeInteger(encoded);
        assertEquals(original, decoded);
    }

    // Tests encodeInteger with null throws NullPointerException
    @Test(expected = NullPointerException.class)
    public void testEncodeInteger_nullInput_throwsNullPointerException() {
        Base64.encodeInteger(null);
    }

    // Tests isUrlSafe and constructor with urlSafe parameter
    @Test
    public void testIsUrlSafe_variousConstructors_reflectsMode() {
        Base64 standard = new Base64();
        assertFalse(standard.isUrlSafe());

        Base64 urlSafe = new Base64(true);
        assertTrue(urlSafe.isUrlSafe());

        Base64 customUrlSafe = new Base64(64, new byte[]{';'}, true);
        assertTrue(customUrlSafe.isUrlSafe());
    }
}