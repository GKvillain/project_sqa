package org.apache.commons.codec.binary;

import org.junit.Test;
import static org.junit.Assert.*;

public class Base32Test {

    private static final byte[] BYTES_FIXTURE = new byte[]{'f', 'o', 'o', 'b', 'a', 'r'};

    // Tests RFC 4648 standard Base32 test vectors
    @Test
    public void testEncode_rfc4648Base32Vectors_returnsExpectedStrings() {
        final Base32 codec = new Base32();
        assertEquals("", codec.encodeAsString(new byte[0]));
        assertEquals("MY======", codec.encodeAsString(new byte[]{'f'}));
        assertEquals("MZXQ====", codec.encodeAsString(new byte[]{'f', 'o'}));
        assertEquals("MZXW6===", codec.encodeAsString(new byte[]{'f', 'o', 'o'}));
        assertEquals("MZXW6YQ=", codec.encodeAsString(new byte[]{'f', 'o', 'o', 'b'}));
        assertEquals("MZXW6YTB", codec.encodeAsString(new byte[]{'f', 'o', 'o', 'b', 'a'}));
        assertEquals("MZXW6YTBOI======", codec.encodeAsString(BYTES_FIXTURE));
    }

    // Tests RFC 4648 Base32hex test vectors
    @Test
    public void testEncode_rfc4648Base32HexVectors_returnsExpectedStrings() {
        final Base32 codec = new Base32(true);
        assertEquals("", codec.encodeAsString(new byte[0]));
        assertEquals("CO======", codec.encodeAsString(new byte[]{'f'}));
        assertEquals("CPNG====", codec.encodeAsString(new byte[]{'f', 'o'}));
        assertEquals("CPNMU===", codec.encodeAsString(new byte[]{'f', 'o', 'o'}));
        assertEquals("CPNMUOG=", codec.encodeAsString(new byte[]{'f', 'o', 'o', 'b'}));
        assertEquals("CPNMUOJ1", codec.encodeAsString(new byte[]{'f', 'o', 'o', 'b', 'a'}));
        assertEquals("CPNMUOJ1E8======", codec.encodeAsString(BYTES_FIXTURE));
    }

    // Tests decode of all modulus remainder sizes
    @Test
    public void testDecode_variousLengths_returnsOriginalBytes() {
        final Base32 codec = new Base32();
        assertArrayEquals(new byte[0], codec.decode(""));
        assertArrayEquals(new byte[]{'f'}, codec.decode("MY======"));
        assertArrayEquals(new byte[]{'f', 'o'}, codec.decode("MZXQ===="));
        assertArrayEquals(new byte[]{'f', 'o', 'o'}, codec.decode("MZXW6==="));
        assertArrayEquals(new byte[]{'f', 'o', 'o', 'b'}, codec.decode("MZXW6YQ="));
        assertArrayEquals(new byte[]{'f', 'o', 'o', 'b', 'a'}, codec.decode("MZXW6YTB"));
        assertArrayEquals(BYTES_FIXTURE, codec.decode("MZXW6YTBOI======"));
    }

    // Tests decode with unpadded inputs
    @Test
    public void testDecode_unpaddedInput_returnsDecodedBytes() {
        final Base32 codec = new Base32();
        assertArrayEquals(new byte[]{'f'}, codec.decode("MY"));
        assertArrayEquals(new byte[]{'f', 'o'}, codec.decode("MZXQ"));
        assertArrayEquals(new byte[]{'f', 'o', 'o'}, codec.decode("MZXW6"));
        assertArrayEquals(new byte[]{'f', 'o', 'o', 'b'}, codec.decode("MZXW6YQ"));
        assertArrayEquals(BYTES_FIXTURE, codec.decode("MZXW6YTBOI"));
    }

    // Tests decode Base32hex variant
    @Test
    public void testDecode_hexVariant_returnsDecodedBytes() {
        final Base32 codec = new Base32(true);
        assertArrayEquals(new byte[]{'f'}, codec.decode("CO======"));
        assertArrayEquals(BYTES_FIXTURE, codec.decode("CPNMUOJ1E8======"));
    }

    // Tests custom padding byte for encoding and decoding
    @Test
    public void testCustomPadding_validPadByte_encodesAndDecodesCorrectly() {
        final Base32 codec = new Base32((byte) '_');
        final String encoded = codec.encodeAsString(new byte[]{'f'});
        assertEquals("MY______", encoded);
        assertArrayEquals(new byte[]{'f'}, codec.decode(encoded));
    }

    // Tests custom padding byte with hex alphabet
    @Test
    public void testCustomPadding_hexVariant_encodesAndDecodesCorrectly() {
        final Base32 codec = new Base32(true, (byte) '$');
        final String encoded = codec.encodeAsString(new byte[]{'f'});
        assertEquals("CO$$$$$$", encoded);
        assertArrayEquals(new byte[]{'f'}, codec.decode(encoded));
    }

    // Tests line chunking with custom line length and separator
    @Test
    public void testChunkedEncoding_customSeparator_chunksCorrectly() {
        final byte[] separator = new byte[]{';'};
        final Base32 codec = new Base32(8, separator);
        final String encoded = codec.encodeAsString(BYTES_FIXTURE);
        assertEquals("MZXW6YTB;OI======;", encoded);
    }

    // Tests line chunking with default constructor taking lineLength
    @Test
    public void testChunkedEncoding_defaultSeparator_usesCrlf() {
        final Base32 codec = new Base32(8);
        final String encoded = codec.encodeAsString(BYTES_FIXTURE);
        assertEquals("MZXW6YTB\r\nOI======\r\n", encoded);
    }

    // Tests isInAlphabet with standard Base32 characters
    @Test
    public void testIsInAlphabet_standardAlphabet_returnsExpectedValues() {
        final Base32 codec = new Base32();
        assertTrue(codec.isInAlphabet((byte) 'A'));
        assertTrue(codec.isInAlphabet((byte) 'Z'));
        assertTrue(codec.isInAlphabet((byte) '2'));
        assertTrue(codec.isInAlphabet((byte) '7'));
        assertFalse(codec.isInAlphabet((byte) '0'));
        assertFalse(codec.isInAlphabet((byte) '1'));
        assertFalse(codec.isInAlphabet((byte) '8'));
        assertFalse(codec.isInAlphabet((byte) '9'));
        assertFalse(codec.isInAlphabet((byte) '='));
        assertFalse(codec.isInAlphabet((byte) -1));
        assertFalse(codec.isInAlphabet((byte) 127));
    }

    // Tests isInAlphabet with Base32hex characters
    @Test
    public void testIsInAlphabet_hexAlphabet_returnsExpectedValues() {
        final Base32 codec = new Base32(true);
        assertTrue(codec.isInAlphabet((byte) '0'));
        assertTrue(codec.isInAlphabet((byte) '9'));
        assertTrue(codec.isInAlphabet((byte) 'A'));
        assertTrue(codec.isInAlphabet((byte) 'V'));
        assertFalse(codec.isInAlphabet((byte) 'W'));
        assertFalse(codec.isInAlphabet((byte) 'Z'));
        assertFalse(codec.isInAlphabet((byte) '='));
        assertFalse(codec.isInAlphabet((byte) -1));
    }

    // Tests null line separator when lineLength > 0 throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullSeparatorWithPositiveLineLength_throwsException() {
        new Base32(10, null);
    }

    // Tests separator containing alphabet character throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_separatorContainsAlphabetChar_throwsException() {
        new Base32(8, new byte[]{'A'});
    }

    // Tests pad byte in standard alphabet throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_padInStandardAlphabet_throwsException() {
        new Base32((byte) 'A');
    }

    // Tests pad byte in hex alphabet throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_padInHexAlphabet_throwsException() {
        new Base32(true, (byte) '0');
    }

    // Tests pad byte being whitespace throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_padIsWhitespace_throwsException() {
        new Base32((byte) ' ');
    }

    // Tests decoding input containing ignored characters like whitespace/CRLF
    @Test
    public void testDecode_inputWithWhitespace_ignoresWhitespace() {
        final Base32 codec = new Base32();
        final byte[] decoded = codec.decode("MZXW6\r\nYTBOI======\n");
        assertArrayEquals(BYTES_FIXTURE, decoded);
    }
}