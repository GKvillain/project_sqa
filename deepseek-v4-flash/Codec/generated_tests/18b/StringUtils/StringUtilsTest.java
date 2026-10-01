package org.apache.commons.codec.binary;

import static org.junit.Assert.*;

import java.nio.ByteBuffer;

import org.junit.Test;

public class StringUtilsTest {

    // Tests equals with both null inputs
    @Test
    public void testEquals_bothNull_returnsTrue() {
        assertTrue(StringUtils.equals(null, null));
    }

    // Tests equals with first null, second non-null
    @Test
    public void testEquals_firstNull_returnsFalse() {
        assertFalse(StringUtils.equals(null, "abc"));
    }

    // Tests equals with first non-null, second null
    @Test
    public void testEquals_secondNull_returnsFalse() {
        assertFalse(StringUtils.equals("abc", null));
    }

    // Tests equals with identical string objects (cs1 == cs2)
    @Test
    public void testEquals_sameObject_returnsTrue() {
        String s = "test";
        assertTrue(StringUtils.equals(s, s));
    }

    // Tests equals with equal strings (both instances of String)
    @Test
    public void testEquals_equalStrings_returnsTrue() {
        assertTrue(StringUtils.equals("abc", "abc"));
    }

    // Tests equals with different strings (case sensitive)
    @Test
    public void testEquals_differentCase_returnsFalse() {
        assertFalse(StringUtils.equals("abc", "ABC"));
    }

    // Tests equals with CharSequence that are not String (StringBuilder)
    @Test
    public void testEquals_CharSequenceNonString_equalContent_returnsTrue() {
        assertTrue(StringUtils.equals(new StringBuilder("abc"), new StringBuilder("abc")));
    }

    // Tests equals with CharSequence that are not String, different content
    @Test
    public void testEquals_CharSequenceNonString_differentContent_returnsFalse() {
        assertFalse(StringUtils.equals(new StringBuilder("abc"), new StringBuilder("def")));
    }

    // Tests getBytesUtf8 with null input
    @Test
    public void testGetBytesUtf8_nullInput_returnsNull() {
        assertNull(StringUtils.getBytesUtf8(null));
    }

    // Tests getBytesUtf8 with valid input
    @Test
    public void testGetBytesUtf8_validInput_returnsCorrectBytes() {
        byte[] expected = new byte[] { 0x48, 0x65, 0x6c, 0x6c, 0x6f };
        assertArrayEquals(expected, StringUtils.getBytesUtf8("Hello"));
    }

    // Tests getBytesIso8859_1 with null input
    @Test
    public void testGetBytesIso8859_1_nullInput_returnsNull() {
        assertNull(StringUtils.getBytesIso8859_1(null));
    }

    // Tests getBytesIso8859_1 with valid input
    @Test
    public void testGetBytesIso8859_1_validInput_returnsCorrectBytes() {
        byte[] expected = new byte[] { (byte) 0x48, (byte) 0x65, (byte) 0x6c, (byte) 0x6c, (byte) 0x6f };
        assertArrayEquals(expected, StringUtils.getBytesIso8859_1("Hello"));
    }

    // Tests getBytesUnchecked with null input
    @Test
    public void testGetBytesUnchecked_nullInput_returnsNull() {
        assertNull(StringUtils.getBytesUnchecked(null, "UTF-8"));
    }

    // Tests getBytesUnchecked with valid input
    @Test
    public void testGetBytesUnchecked_validInput_returnsCorrectBytes() {
        byte[] expected = new byte[] { 0x48, 0x65, 0x6c, 0x6c, 0x6f };
        assertArrayEquals(expected, StringUtils.getBytesUnchecked("Hello", "UTF-8"));
    }

    // Tests getBytesUnchecked with unsupported encoding, expects IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testGetBytesUnchecked_unsupportedEncoding_throwsException() {
        StringUtils.getBytesUnchecked("Hello", "unsupported-encoding");
    }

    // Tests newStringUtf8 with null input
    @Test
    public void testNewStringUtf8_nullInput_returnsNull() {
        assertNull(StringUtils.newStringUtf8(null));
    }

    // Tests newStringUtf8 with valid input
    @Test
    public void testNewStringUtf8_validInput_returnsCorrectString() {
        byte[] input = new byte[] { 0x48, 0x65, 0x6c, 0x6c, 0x6f };
        assertEquals("Hello", StringUtils.newStringUtf8(input));
    }

    // Tests newStringIso8859_1 with null input
    @Test
    public void testNewStringIso8859_1_nullInput_returnsNull() {
        assertNull(StringUtils.newStringIso8859_1(null));
    }

    // Tests newStringIso8859_1 with valid input
    @Test
    public void testNewStringIso8859_1_validInput_returnsCorrectString() {
        byte[] input = new byte[] { (byte) 0x48, (byte) 0x65, (byte) 0x6c, (byte) 0x6c, (byte) 0x6f };
        assertEquals("Hello", StringUtils.newStringIso8859_1(input));
    }

    // Tests newString with null input
    @Test
    public void testNewString_nullInput_returnsNull() {
        assertNull(StringUtils.newString(null, "UTF-8"));
    }

    // Tests newString with valid input
    @Test
    public void testNewString_validInput_returnsCorrectString() {
        byte[] input = new byte[] { 0x48, 0x65, 0x6c, 0x6c, 0x6f };
        assertEquals("Hello", StringUtils.newString(input, "UTF-8"));
    }

    // Tests newString with unsupported encoding, expects IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testNewString_unsupportedEncoding_throwsException() {
        byte[] input = new byte[] { 0x48, 0x65, 0x6c, 0x6c, 0x6f };
        StringUtils.newString(input, "unsupported-encoding");
    }

    // Tests getByteBufferUtf8 with null input
    @Test
    public void testGetByteBufferUtf8_nullInput_returnsNull() {
        assertNull(StringUtils.getByteBufferUtf8(null));
    }

    // Tests getByteBufferUtf8 with valid input
    @Test
    public void testGetByteBufferUtf8_validInput_returnsCorrectByteBuffer() {
        ByteBuffer result = StringUtils.getByteBufferUtf8("Hello");
        assertNotNull(result);
        byte[] expected = new byte[] { 0x48, 0x65, 0x6c, 0x6c, 0x6f };
        byte[] actual = new byte[result.remaining()];
        result.get(actual);
        assertArrayEquals(expected, actual);
    }

    // Tests getBytesUsAscii with null input
    @Test
    public void testGetBytesUsAscii_nullInput_returnsNull() {
        assertNull(StringUtils.getBytesUsAscii(null));
    }

    // Tests getBytesUsAscii with valid input
    @Test
    public void testGetBytesUsAscii_validInput_returnsCorrectBytes() {
        byte[] expected = new byte[] { 0x48, 0x65, 0x6c, 0x6c, 0x6f };
        assertArrayEquals(expected, StringUtils.getBytesUsAscii("Hello"));
    }

    // Tests getBytesUtf16 with null input
    @Test
    public void testGetBytesUtf16_nullInput_returnsNull() {
        assertNull(StringUtils.getBytesUtf16(null));
    }

    // Tests getBytesUtf16Be with null input
    @Test
    public void testGetBytesUtf16Be_nullInput_returnsNull() {
        assertNull(StringUtils.getBytesUtf16Be(null));
    }

    // Tests getBytesUtf16Le with null input
    @Test
    public void testGetBytesUtf16Le_nullInput_returnsNull() {
        assertNull(StringUtils.getBytesUtf16Le(null));
    }

    // Tests newStringUsAscii with null input
    @Test
    public void testNewStringUsAscii_nullInput_returnsNull() {
        assertNull(StringUtils.newStringUsAscii(null));
    }

    // Tests newStringUtf16 with null input
    @Test
    public void testNewStringUtf16_nullInput_returnsNull() {
        assertNull(StringUtils.newStringUtf16(null));
    }

    // Tests newStringUtf16Be with null input
    @Test
    public void testNewStringUtf16Be_nullInput_returnsNull() {
        assertNull(StringUtils.newStringUtf16Be(null));
    }

    // Tests newStringUtf16Le with null input
    @Test
    public void testNewStringUtf16Le_nullInput_returnsNull() {
        assertNull(StringUtils.newStringUtf16Le(null));
    }
}