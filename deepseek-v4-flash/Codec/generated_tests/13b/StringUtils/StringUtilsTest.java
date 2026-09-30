package org.apache.commons.codec.binary;

import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;

import org.junit.Test;

public class StringUtilsTest {

    // Tests getBytesIso8859_1 with valid string
    @Test
    public void testGetBytesIso8859_1_validInput_returnsBytes() {
        String input = "Hello";
        byte[] result = StringUtils.getBytesIso8859_1(input);
        assertNotNull(result);
        assertArrayEquals(new byte[] { 72, 101, 108, 108, 111 }, result);
    }

    // Tests getBytesIso8859_1 with null input
    @Test
    public void testGetBytesIso8859_1_nullInput_returnsNull() {
        assertNull(StringUtils.getBytesIso8859_1(null));
    }

    // Tests getBytesIso8859_1 with empty string
    @Test
    public void testGetBytesIso8859_1_emptyInput_returnsEmptyArray() {
        byte[] result = StringUtils.getBytesIso8859_1("");
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    // Tests getBytesUsAscii with valid string
    @Test
    public void testGetBytesUsAscii_validInput_returnsBytes() {
        String input = "Test";
        byte[] result = StringUtils.getBytesUsAscii(input);
        assertNotNull(result);
        assertArrayEquals(new byte[] { 84, 101, 115, 116 }, result);
    }

    // Tests getBytesUsAscii with null input
    @Test
    public void testGetBytesUsAscii_nullInput_returnsNull() {
        assertNull(StringUtils.getBytesUsAscii(null));
    }

    // Tests getBytesUtf8 with valid string
    @Test
    public void testGetBytesUtf8_validInput_returnsBytes() {
        String input = "UTF8";
        byte[] result = StringUtils.getBytesUtf8(input);
        assertNotNull(result);
        assertArrayEquals(new byte[] { 85, 84, 70, 56 }, result);
    }

    // Tests getBytesUtf8 with null input
    @Test
    public void testGetBytesUtf8_nullInput_returnsNull() {
        assertNull(StringUtils.getBytesUtf8(null));
    }

    // Tests getBytesUtf16 with valid string
    @Test
    public void testGetBytesUtf16_validInput_returnsBytes() {
        String input = "A";
        byte[] result = StringUtils.getBytesUtf16(input);
        assertNotNull(result);
        // UTF-16 includes BOM: FE FF 00 41 for big-endian, FF FE 41 00 for little-endian
        // Since Charsets.UTF_16 is used without specifying endianness, JVM default may vary; we just check non-null and length 4
        assertEquals(4, result.length);
    }

    // Tests getBytesUtf16Be with valid string
    @Test
    public void testGetBytesUtf16Be_validInput_returnsBytes() {
        String input = "A";
        byte[] result = StringUtils.getBytesUtf16Be(input);
        assertNotNull(result);
        // UTF-16BE: 00 41
        assertArrayEquals(new byte[] { 0, 65 }, result);
    }

    // Tests getBytesUtf16Le with valid string
    @Test
    public void testGetBytesUtf16Le_validInput_returnsBytes() {
        String input = "A";
        byte[] result = StringUtils.getBytesUtf16Le(input);
        assertNotNull(result);
        // UTF-16LE: 41 00
        assertArrayEquals(new byte[] { 65, 0 }, result);
    }

    // Tests getBytesUnchecked with valid charset
    @Test
    public void testGetBytesUnchecked_validInput_returnsBytes() {
        String input = "Hi";
        byte[] result = StringUtils.getBytesUnchecked(input, "UTF-8");
        assertNotNull(result);
        assertArrayEquals(new byte[] { 72, 105 }, result);
    }

    // Tests getBytesUnchecked with null input
    @Test
    public void testGetBytesUnchecked_nullInput_returnsNull() {
        assertNull(StringUtils.getBytesUnchecked(null, "UTF-8"));
    }

    // Tests getBytesUnchecked with unsupported charset
    @Test(expected = IllegalStateException.class)
    public void testGetBytesUnchecked_unsupportedCharset_throwsIllegalStateException() {
        StringUtils.getBytesUnchecked("test", "unsupported-charset");
    }

    // Tests newStringIso8859_1 with valid bytes
    @Test
    public void testNewStringIso8859_1_validBytes_returnsString() {
        byte[] input = new byte[] { 72, 101, 108, 108, 111 };
        String result = StringUtils.newStringIso8859_1(input);
        assertEquals("Hello", result);
    }

    // Tests newStringIso8859_1 with null bytes
    @Test
    public void testNewStringIso8859_1_nullInput_throwsNullPointerException() {
        // newStringIso8859_1 calls new String(bytes, Charsets.ISO_8859_1) which throws NullPointerException for null bytes
        try {
            StringUtils.newStringIso8859_1(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    // Tests newStringUtf8 with valid bytes
    @Test
    public void testNewStringUtf8_validBytes_returnsString() {
        byte[] input = new byte[] { 84, 101, 115, 116 };
        String result = StringUtils.newStringUtf8(input);
        assertEquals("Test", result);
    }

    // Tests newStringUtf8 with null bytes via private newString method
    @Test
    public void testNewStringUtf8_nullInput_returnsNull() {
        assertNull(StringUtils.newStringUtf8(null));
    }

    // Tests newString with valid bytes and charset name
    @Test
    public void testNewString_validBytes_returnsString() {
        byte[] input = new byte[] { 72, 105 };
        String result = StringUtils.newString(input, "UTF-8");
        assertEquals("Hi", result);
    }

    // Tests newString with null bytes
    @Test
    public void testNewString_nullInput_returnsNull() {
        assertNull(StringUtils.newString(null, "UTF-8"));
    }

    // Tests newString with unsupported charset
    @Test(expected = IllegalStateException.class)
    public void testNewString_unsupportedCharset_throwsIllegalStateException() {
        byte[] input = new byte[] { 65 };
        StringUtils.newString(input, "unsupported-charset");
    }

    // Tests newStringUsAscii with valid bytes
    @Test
    public void testNewStringUsAscii_validBytes_returnsString() {
        byte[] input = new byte[] { 65, 66 };
        String result = StringUtils.newStringUsAscii(input);
        assertEquals("AB", result);
    }

    // Tests newStringUtf16 with valid bytes
    @Test
    public void testNewStringUtf16_validBytes_returnsString() {
        byte[] input = new byte[] { -2, -1, 0, 65 }; // BOM + 'A' in UTF-16BE
        String result = StringUtils.newStringUtf16(input);
        assertEquals("A", result);
    }

    // ====== New tests to improve coverage ======

    // getBytesUsAscii with empty string
    @Test
    public void testGetBytesUsAscii_emptyInput_returnsEmptyArray() {
        byte[] result = StringUtils.getBytesUsAscii("");
        assertNotNull(result);
        assertArrayEquals(new byte[0], result);
    }

    // getBytesUtf8 with empty string
    @Test
    public void testGetBytesUtf8_emptyInput_returnsEmptyArray() {
        byte[] result = StringUtils.getBytesUtf8("");
        assertNotNull(result);
        assertArrayEquals(new byte[0], result);
    }

    // getBytesUtf16 with null input
    @Test
    public void testGetBytesUtf16_nullInput_returnsNull() {
        assertNull(StringUtils.getBytesUtf16(null));
    }

    // getBytesUtf16 with empty string (includes BOM)
    @Test
    public void testGetBytesUtf16_emptyInput_returnsBOM() {
        byte[] result = StringUtils.getBytesUtf16("");
        assertNotNull(result);
        // UTF-16 encoding of empty string gives just the BOM (FE FF)
        assertArrayEquals(new byte[] { (byte)0xFE, (byte)0xFF }, result);
    }

    // getBytesUtf16Be with null input
    @Test
    public void testGetBytesUtf16Be_nullInput_returnsNull() {
        assertNull(StringUtils.getBytesUtf16Be(null));
    }

    // getBytesUtf16Be with empty string
    @Test
    public void testGetBytesUtf16Be_emptyInput_returnsEmptyArray() {
        byte[] result = StringUtils.getBytesUtf16Be("");
        assertNotNull(result);
        assertArrayEquals(new byte[0], result);
    }

    // getBytesUtf16Le with null input
    @Test
    public void testGetBytesUtf16Le_nullInput_returnsNull() {
        assertNull(StringUtils.getBytesUtf16Le(null));
    }

    // getBytesUtf16Le with empty string
    @Test
    public void testGetBytesUtf16Le_emptyInput_returnsEmptyArray() {
        byte[] result = StringUtils.getBytesUtf16Le("");
        assertNotNull(result);
        assertArrayEquals(new byte[0], result);
    }

    // getBytesUnchecked with empty string
    @Test
    public void testGetBytesUnchecked_emptyInput_returnsEmptyArray() {
        byte[] result = StringUtils.getBytesUnchecked("", "UTF-8");
        assertNotNull(result);
        assertArrayEquals(new byte[0], result);
    }

    // newStringIso8859_1 with empty bytes
    @Test
    public void testNewStringIso8859_1_emptyInput_returnsEmptyString() {
        String result = StringUtils.newStringIso8859_1(new byte[0]);
        assertEquals("", result);
    }

    // newStringUtf8 with empty bytes
    @Test
    public void testNewStringUtf8_emptyInput_returnsEmptyString() {
        String result = StringUtils.newStringUtf8(new byte[0]);
        assertEquals("", result);
    }

    // newStringUsAscii with null input
    @Test
    public void testNewStringUsAscii_nullInput_returnsNull() {
        assertNull(StringUtils.newStringUsAscii(null));
    }

    // newStringUsAscii with empty bytes
    @Test
    public void testNewStringUsAscii_emptyInput_returnsEmptyString() {
        String result = StringUtils.newStringUsAscii(new byte[0]);
        assertEquals("", result);
    }

    // newStringUtf16 with null input
    @Test
    public void testNewStringUtf16_nullInput_returnsNull() {
        assertNull(StringUtils.newStringUtf16(null));
    }

    // newStringUtf16 with empty bytes
    @Test
    public void testNewStringUtf16_emptyInput_returnsEmptyString() {
        String result = StringUtils.newStringUtf16(new byte[0]);
        assertEquals("", result);
    }

    // newStringUtf16Be with valid bytes
    @Test
    public void testNewStringUtf16Be_validInput_returnsString() {
        byte[] input = new byte[] { 0, 65 }; // UTF-16BE encoding of "A"
        String result = StringUtils.newStringUtf16Be(input);
        assertEquals("A", result);
    }

    // newStringUtf16Be with null input
    @Test
    public void testNewStringUtf16Be_nullInput_returnsNull() {
        assertNull(StringUtils.newStringUtf16Be(null));
    }

    // newStringUtf16Be with empty bytes
    @Test
    public void testNewStringUtf16Be_emptyInput_returnsEmptyString() {
        String result = StringUtils.newStringUtf16Be(new byte[0]);
        assertEquals("", result);
    }

    // newStringUtf16Le with valid bytes
    @Test
    public void testNewStringUtf16Le_validInput_returnsString() {
        byte[] input = new byte[] { 65, 0 }; // UTF-16LE encoding of "A"
        String result = StringUtils.newStringUtf16Le(input);
        assertEquals("A", result);
    }

    // newStringUtf16Le with null input
    @Test
    public void testNewStringUtf16Le_nullInput_returnsNull() {
        assertNull(StringUtils.newStringUtf16Le(null));
    }

    // newStringUtf16Le with empty bytes
    @Test
    public void testNewStringUtf16Le_emptyInput_returnsEmptyString() {
        String result = StringUtils.newStringUtf16Le(new byte[0]);
        assertEquals("", result);
    }

    // newString (byte[], String) with empty bytes
    @Test
    public void testNewString_emptyInput_returnsEmptyString() {
        String result = StringUtils.newString(new byte[0], "UTF-8");
        assertEquals("", result);
    }
}