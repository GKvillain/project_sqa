package org.apache.commons.codec.binary;

import static org.junit.Assert.*;
import org.junit.Test;

public class Base32Test {

    private static final byte[] EMPTY = {};

    // Tests empty input encoding/decoding
    @Test
    public void testEncodeDecode_emptyInput_returnsEmpty() {
        Base32 base32 = new Base32();
        String encoded = base32.encodeToString(EMPTY);
        assertEquals("", encoded);
        byte[] decoded = base32.decode(encoded);
        assertArrayEquals(EMPTY, decoded);
    }

    // Tests single byte (modulus 1 in encode -> 2 chars + 6 pad; modulus 2 in decode)
    @Test
    public void testEncodeDecode_singleByte_roundtrip() {
        Base32 base32 = new Base32();
        byte[] input = { (byte) 0x12 };
        String encoded = base32.encodeToString(input);
        byte[] decoded = base32.decode(encoded);
        assertArrayEquals(input, decoded);
    }

    // Tests two bytes (modulus 2 in encode -> 4 chars + 4 pad; modulus 4 in decode)
    @Test
    public void testEncodeDecode_twoBytes_roundtrip() {
        Base32 base32 = new Base32();
        byte[] input = { (byte) 0x12, (byte) 0x34 };
        String encoded = base32.encodeToString(input);
        byte[] decoded = base32.decode(encoded);
        assertArrayEquals(input, decoded);
    }

    // Tests three bytes (modulus 3 in encode -> 5 chars + 3 pad; modulus 5 in decode)
    @Test
    public void testEncodeDecode_threeBytes_roundtrip() {
        Base32 base32 = new Base32();
        byte[] input = { (byte) 0x12, (byte) 0x34, (byte) 0x56 };
        String encoded = base32.encodeToString(input);
        byte[] decoded = base32.decode(encoded);
        assertArrayEquals(input, decoded);
    }

    // Tests four bytes (modulus 4 in encode -> 7 chars + 1 pad; modulus 7 in decode)
    @Test
    public void testEncodeDecode_fourBytes_roundtrip() {
        Base32 base32 = new Base32();
        byte[] input = { (byte) 0x12, (byte) 0x34, (byte) 0x56, (byte) 0x78 };
        String encoded = base32.encodeToString(input);
        byte[] decoded = base32.decode(encoded);
        assertArrayEquals(input, decoded);
    }

    // Tests five bytes (complete block, no padding; decode modulus 0 – no leftover processing)
    @Test
    public void testEncodeDecode_fiveBytes_roundtrip() {
        Base32 base32 = new Base32();
        byte[] input = { (byte) 0x12, (byte) 0x34, (byte) 0x56, (byte) 0x78, (byte) 0x9A };
        String encoded = base32.encodeToString(input);
        byte[] decoded = base32.decode(encoded);
        assertArrayEquals(input, decoded);
    }

    // Tests seven bytes (two blocks, one incomplete with 2 bytes left)
    @Test
    public void testEncodeDecode_sevenBytes_roundtrip() {
        Base32 base32 = new Base32();
        byte[] input = { (byte) 0x12, (byte) 0x34, (byte) 0x56, (byte) 0x78, (byte) 0x9A, (byte) 0xBC, (byte) 0xDE };
        String encoded = base32.encodeToString(input);
        byte[] decoded = base32.decode(encoded);
        assertArrayEquals(input, decoded);
    }

    // Tests encoding with line length > 0 (line separator is inserted)
    @Test
    public void testEncodeWithLineLength_usesSeparator() {
        Base32 base32 = new Base32(8); // line length 8, default CRLF
        byte[] input = new byte[10];
        for (int i = 0; i < input.length; i++) {
            input[i] = (byte) i;
        }
        String encoded = base32.encodeToString(input);
        assertTrue("Encoded string should contain line separator", encoded.contains("\r\n"));
        byte[] decoded = base32.decode(encoded);
        assertArrayEquals(input, decoded);
    }

    // Tests encoding/decoding with Base32 Hex alphabet
    @Test
    public void testEncodeDecode_useHex_roundtrip() {
        Base32 base32 = new Base32(true);
        byte[] input = { (byte) 0x12, (byte) 0x34, (byte) 0x56, (byte) 0x78, (byte) 0x9A };
        String encoded = base32.encodeToString(input);
        byte[] decoded = base32.decode(encoded);
        assertArrayEquals(input, decoded);
    }

    // Tests constructor: pad character that is part of alphabet throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_padIsAlphabet_throwsException() {
        new Base32((byte) 'A');
    }

    // Tests constructor: pad character that is whitespace throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_padIsWhitespace_throwsException() {
        new Base32((byte) ' ');
    }

    // Tests constructor: line separator containing Base32 characters throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_lineSeparatorContainsAlphabet_throwsException() {
        new Base32(76, new byte[] { 'A' });
    }

    // Tests constructor: lineLength > 0 with null lineSeparator throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_lineLengthPositiveButNullSeparator_throwsException() {
        new Base32(10, null);
    }

    // Tests isInAlphabet with valid Base32 characters (A-Z,2-7)
    @Test
    public void testIsInAlphabet_validChar_returnsTrue() {
        Base32 base32 = new Base32();
        assertTrue(base32.isInAlphabet((byte) 'A'));
        assertTrue(base32.isInAlphabet((byte) 'Z'));
        assertTrue(base32.isInAlphabet((byte) '2'));
        assertTrue(base32.isInAlphabet((byte) '7'));
    }

    // Tests isInAlphabet with characters outside valid alphabet
    @Test
    public void testIsInAlphabet_invalidChar_returnsFalse() {
        Base32 base32 = new Base32();
        assertFalse(base32.isInAlphabet((byte) '8'));
        assertFalse(base32.isInAlphabet((byte) 'a'));
        assertFalse(base32.isInAlphabet((byte) '*'));
    }

    // Tests decode with padding present (normal case)
    @Test
    public void testDecode_withPadding_ignoresPadding() {
        Base32 base32 = new Base32();
        byte[] input = { (byte) 0x12, (byte) 0x34, (byte) 0x56 };
        String encoded = base32.encodeToString(input);
        byte[] decoded = base32.decode(encoded);
        assertArrayEquals(input, decoded);
    }

    // Tests decode without padding (incomplete block)
    @Test
    public void testDecode_withoutPadding_acceptsIncompleteBlock() {
        Base32 base32 = new Base32();
        byte[] input = { (byte) 0x12 };
        String encodedWithPad = base32.encodeToString(input);
        String encodedNoPad = encodedWithPad.replace("=", "");
        byte[] decoded = base32.decode(encodedNoPad);
        assertArrayEquals(input, decoded);
    }
}