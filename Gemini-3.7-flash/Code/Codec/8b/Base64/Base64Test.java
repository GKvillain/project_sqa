package org.apache.commons.codec.binary;

import java.math.BigInteger;
import java.util.Arrays;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.junit.Test;
import static org.junit.Assert.*;

public class Base64Test {

    // Tests encoding basic ASCII string with standard Base64
    @Test
    public void testEncodeBase64_standardString_returnsEncodedBytes() {
        byte[] input = StringUtils.getBytesUtf8("Hello World");
        byte[] encoded = Base64.encodeBase64(input);
        assertEquals("SGVsbG8gV29ybGQ=", StringUtils.newStringUtf8(encoded));
    }

    // Tests decoding basic Base64 string back to original bytes
    @Test
    public void testDecodeBase64_standardEncodedString_returnsDecodedBytes() {
        byte[] encoded = StringUtils.getBytesUtf8("SGVsbG8gV29ybGQ=");
        byte[] decoded = Base64.decodeBase64(encoded);
        assertEquals("Hello World", StringUtils.newStringUtf8(decoded));
    }

    // Tests decoding using String parameter overload
    @Test
    public void testDecodeBase64_stringInput_returnsDecodedBytes() {
        byte[] decoded = Base64.decodeBase64("SGVsbG8gV29ybGQ=");
        assertEquals("Hello World", StringUtils.newStringUtf8(decoded));
    }

    // Tests encoding and decoding with various padding lengths (1, 2, and 3 bytes)
    @Test
    public void testEncodeDecode_variousLengths_handlesPaddingCorrectly() {
        Base64 base64 = new Base64();
        
        byte[] oneByte = new byte[]{'f'};
        byte[] twoBytes = new byte[]{'f', 'o'};
        byte[] threeBytes = new byte[]{'f', 'o', 'o'};

        assertEquals("Zg==", base64.encodeToString(oneByte));
        assertEquals("Zm8=", base64.encodeToString(twoBytes));
        assertEquals("Zm9v", base64.encodeToString(threeBytes));

        assertArrayEquals(oneByte, base64.decode("Zg=="));
        assertArrayEquals(twoBytes, base64.decode("Zm8="));
        assertArrayEquals(threeBytes, base64.decode("Zm9v"));
    }

    // Tests null and empty byte array handling
    @Test
    public void testEncodeDecode_nullAndEmpty_returnsSame() {
        Base64 base64 = new Base64();
        assertNull(base64.encode((byte[]) null));
        assertNull(base64.decode((byte[]) null));
        assertArrayEquals(new byte[0], base64.encode(new byte[0]));
        assertArrayEquals(new byte[0], base64.decode(new byte[0]));
        assertNull(Base64.encodeBase64((byte[]) null));
        assertNull(Base64.decodeBase64((byte[]) null));
    }

    // Tests URL-safe encoding and decoding with '-' and '_'
    @Test
    public void testEncodeBase64URLSafe_binaryWithSpecialChars_usesUrlSafeAlphabetWithoutPadding() {
        byte[] binaryData = new byte[]{(byte) 0xfb, (byte) 0xff, (byte) 0xbf}; // produces + and / in standard
        byte[] standardEncoded = Base64.encodeBase64(binaryData);
        byte[] urlSafeEncoded = Base64.encodeBase64URLSafe(binaryData);

        assertEquals("+/+/ ", StringUtils.newStringUtf8(standardEncoded).trim());
        assertEquals("-_-_", StringUtils.newStringUtf8(urlSafeEncoded));
        assertTrue(Base64.encodeBase64URLSafeString(binaryData).contains("-_-_"));

        // Decoder should decode both standard and URL-safe seamlessly
        assertArrayEquals(binaryData, Base64.decodeBase64(standardEncoded));
        assertArrayEquals(binaryData, Base64.decodeBase64(urlSafeEncoded));
    }

    // Tests chunked encoding per RFC 2045 (76 character chunks + CRLF)
    @Test
    public void testEncodeBase64Chunked_longInput_chunksOutputWithCRLF() {
        byte[] input = new byte[60]; // 60 * 4/3 = 80 characters -> 76 chars + CRLF + 4 chars + CRLF
        Arrays.fill(input, (byte) 'A');
        byte[] chunked = Base64.encodeBase64Chunked(input);
        String chunkedStr = StringUtils.newStringUtf8(chunked);

        assertTrue(chunkedStr.contains("\r\n"));
        assertEquals(80 + 4, chunked.length); // 80 chars + 2 CRLFs (4 bytes)
        assertArrayEquals(input, Base64.decodeBase64(chunked));
    }

    // Tests custom line length and custom line separator constructor
    @Test
    public void testCustomChunking_customLengthAndSeparator_chunksCorrectly() {
        byte[] input = "1234567890".getBytes();
        byte[] customSeparator = new byte[]{';'};
        Base64 base64 = new Base64(4, customSeparator);

        byte[] encoded = base64.encode(input);
        String encodedStr = StringUtils.newStringUtf8(encoded);
        assertTrue(encodedStr.contains(";"));
        assertArrayEquals(input, base64.decode(encoded));
    }

    // Tests constructor exception when line separator contains Base64 character
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_separatorContainsBase64Character_throwsIllegalArgumentException() {
        byte[] invalidSeparator = new byte[]{'A', '\n'};
        new Base64(76, invalidSeparator);
    }

    // Tests maxResultSize check throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testEncodeBase64_exceedsMaxResultSize_throwsIllegalArgumentException() {
        byte[] input = new byte[100];
        Base64.encodeBase64(input, false, false, 10);
    }

    // Tests Object encode interface method with valid and invalid types
    @Test
    public void testEncodeObject_validAndInvalidTypes_encodesOrThrowsException() throws Exception {
        Base64 base64 = new Base64();
        byte[] input = "test".getBytes();
        Object result = base64.encode((Object) input);
        assertTrue(result instanceof byte[]);
        assertArrayEquals(base64.encode(input), (byte[]) result);

        try {
            base64.encode("Invalid Object Type");
            fail("Expected EncoderException for non-byte[] input");
        } catch (EncoderException expected) {
            // Success
        }
    }

    // Tests Object decode interface method with byte[], String, and invalid types
    @Test
    public void testDecodeObject_validAndInvalidTypes_decodesOrThrowsException() throws Exception {
        Base64 base64 = new Base64();
        byte[] encodedBytes = base64.encode("test".getBytes());
        String encodedString = base64.encodeToString("test".getBytes());

        Object resultFromBytes = base64.decode((Object) encodedBytes);
        assertTrue(resultFromBytes instanceof byte[]);
        assertEquals("test", new String((byte[]) resultFromBytes));

        Object resultFromString = base64.decode((Object) encodedString);
        assertTrue(resultFromString instanceof byte[]);
        assertEquals("test", new String((byte[]) resultFromString));

        try {
            base64.decode(Integer.valueOf(12345));
            fail("Expected DecoderException for non-byte[] and non-String input");
        } catch (DecoderException expected) {
            // Success
        }
    }

    // Tests isBase64 and isArrayByteBase64 validation methods
    @Test
    public void testIsBase64_variousInputs_returnsExpectedBoolean() {
        assertTrue(Base64.isBase64((byte) 'A'));
        assertTrue(Base64.isBase64((byte) 'z'));
        assertTrue(Base64.isBase64((byte) '0'));
        assertTrue(Base64.isBase64((byte) '+'));
        assertTrue(Base64.isBase64((byte) '/'));
        assertTrue(Base64.isBase64((byte) '-'));
        assertTrue(Base64.isBase64((byte) '_'));
        assertTrue(Base64.isBase64((byte) '=')); // PAD

        assertFalse(Base64.isBase64((byte) '$'));
        assertFalse(Base64.isBase64((byte) -5));

        assertTrue(Base64.isArrayByteBase64(StringUtils.getBytesUtf8("SGVsbG8gV29ybGQ=\r\n ")));
        assertFalse(Base64.isArrayByteBase64(new byte[]{'A', 'B', '$', 'D'}));
    }

    // Tests BigInteger encode and decode
    @Test
    public void testBigInteger_encodeAndDecode_matchesOriginal() {
        BigInteger original = new BigInteger("123456789012345678901234567890");
        byte[] encoded = Base64.encodeInteger(original);
        BigInteger decoded = Base64.decodeInteger(encoded);
        assertEquals(original, decoded);
    }

    // Tests BigInteger null input exception
    @Test(expected = NullPointerException.class)
    public void testEncodeInteger_nullInput_throwsNullPointerException() {
        Base64.encodeInteger(null);
    }

    // Tests streaming methods: hasData, avail, readResults, setInitialBuffer
    @Test
    public void testStreamingOperations_readResultsAndBufferHandling_executesCorrectly() {
        Base64 base64 = new Base64();
        assertFalse(base64.hasData());
        assertEquals(0, base64.avail());

        byte[] input = "Streaming data test".getBytes();
        base64.encode(input, 0, input.length);
        assertTrue(base64.hasData());
        assertTrue(base64.avail() > 0);

        byte[] out = new byte[base64.avail()];
        int read = base64.readResults(out, 0, out.length);
        assertEquals(out.length, read);
        assertFalse(base64.hasData());

        // Test setInitialBuffer
        byte[] buffer = new byte[100];
        base64.setInitialBuffer(buffer, 10, 100);
        assertTrue(base64.hasData());
    }

    // Tests discardWhitespace static utility method
    @Test
    public void testDiscardWhitespace_inputWithSpacesAndTabs_removesWhitespace() {
        byte[] withWhitespace = new byte[]{'A', ' ', '\t', 'B', '\r', '\n', 'C'};
        byte[] clean = Base64.discardWhitespace(withWhitespace);
        assertArrayEquals(new byte[]{'A', 'B', 'C'}, clean);
    }

    // Tests decode method ignoring invalid characters / garbage-in garbage-out
    @Test
    public void testDecode_withNonBase64Characters_ignoresNonBase64Characters() {
        byte[] valid = Base64.encodeBase64("Hello World".getBytes());
        byte[] withGarbage = new byte[valid.length + 4];
        withGarbage[0] = '%';
        System.arraycopy(valid, 0, withGarbage, 1, valid.length);
        withGarbage[withGarbage.length - 1] = '*';

        byte[] decoded = Base64.decodeBase64(withGarbage);
        assertEquals("Hello World", StringUtils.newStringUtf8(decoded));
    }
}