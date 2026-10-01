package org.apache.commons.codec.binary;

import static org.junit.Assert.*;

import java.nio.ByteBuffer;

import org.apache.commons.codec.Charsets;
import org.junit.Test;

public class StringUtilsTest {

    // Tests for equals method

    @Test
    public void testEquals_bothNull_returnsTrue() {
        assertTrue(StringUtils.equals(null, null));
    }

    @Test
    public void testEquals_nullFirst_returnsFalse() {
        assertFalse(StringUtils.equals(null, "abc"));
    }

    @Test
    public void testEquals_sameString_returnsTrue() {
        assertTrue(StringUtils.equals("abc", "abc"));
    }

    @Test
    public void testEquals_differentString_returnsFalse() {
        assertFalse(StringUtils.equals("abc", "ABC"));
    }

    @Test
    public void testEquals_nonStringSameContent_returnsTrue() {
        assertTrue(StringUtils.equals(new StringBuilder("hello"), new StringBuilder("hello")));
    }

    // Defect-triggering test: non-String CharSequences with different lengths
    @Test
    public void testEquals_nonStringDifferentLength_returnsFalse() {
        assertFalse(StringUtils.equals(new StringBuilder("abc"), new StringBuilder("abcd")));
    }

    @Test
    public void testEquals_nonStringDifferentContent_returnsFalse() {
        assertFalse(StringUtils.equals(new StringBuilder("abc"), new StringBuilder("abd")));
    }

    // Tests for getBytesUtf8

    @Test
    public void testGetBytesUtf8_nullInput_returnsNull() {
        assertNull(StringUtils.getBytesUtf8(null));
    }

    @Test
    public void testGetBytesUtf8_validInput_returnsBytes() {
        String input = "test";
        byte[] expected = input.getBytes(Charsets.UTF_8);
        assertArrayEquals(expected, StringUtils.getBytesUtf8(input));
    }

    // Tests for getBytesUnchecked

    @Test
    public void testGetBytesUnchecked_nullInput_returnsNull() {
        assertNull(StringUtils.getBytesUnchecked(null, "UTF-8"));
    }

    @Test
    public void testGetBytesUnchecked_validInput_returnsBytes() {
        String input = "test";
        byte[] expected = input.getBytes(Charsets.UTF_8);
        assertArrayEquals(expected, StringUtils.getBytesUnchecked(input, "UTF-8"));
    }

    @Test(expected = IllegalStateException.class)
    public void testGetBytesUnchecked_invalidCharset_throwsIllegalStateException() {
        StringUtils.getBytesUnchecked("test", "invalid-charset");
    }

    // Tests for newString (with charset name)

    @Test
    public void testNewString_nullBytes_returnsNull() {
        assertNull(StringUtils.newString((byte[]) null, "UTF-8"));
    }

    @Test
    public void testNewString_validInput_returnsString() {
        byte[] bytes = "test".getBytes(Charsets.UTF_8);
        assertEquals("test", StringUtils.newString(bytes, "UTF-8"));
    }

    @Test(expected = IllegalStateException.class)
    public void testNewString_invalidCharset_throwsIllegalStateException() {
        byte[] bytes = new byte[] {65};
        StringUtils.newString(bytes, "invalid-charset");
    }

    // Tests for newStringUtf8

    @Test
    public void testNewStringUtf8_nullInput_returnsNull() {
        assertNull(StringUtils.newStringUtf8(null));
    }

    @Test
    public void testNewStringUtf8_validInput_returnsString() {
        byte[] bytes = "test".getBytes(Charsets.UTF_8);
        assertEquals("test", StringUtils.newStringUtf8(bytes));
    }

    // Tests for newStringIso8859_1

    @Test
    public void testNewStringIso8859_1_validInput_returnsString() {
        byte[] bytes = "test".getBytes(Charsets.ISO_8859_1);
        assertEquals("test", StringUtils.newStringIso8859_1(bytes));
    }

    // Tests for getByteBufferUtf8

    @Test
    public void testGetByteBufferUtf8_nullInput_returnsNull() {
        assertNull(StringUtils.getByteBufferUtf8(null));
    }

    @Test
    public void testGetByteBufferUtf8_validInput_returnsByteBuffer() {
        String input = "test";
        ByteBuffer result = StringUtils.getByteBufferUtf8(input);
        assertNotNull(result);
        byte[] resultBytes = new byte[result.remaining()];
        result.get(resultBytes);
        assertArrayEquals(input.getBytes(Charsets.UTF_8), resultBytes);
    }
}