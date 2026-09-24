package org.apache.commons.codec.binary;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;
import org.junit.Test;

public class StringUtilsTest {

    // Tests constructor instantiation
    @Test
    public void testConstructor_default_instanceCreated() {
        assertNotNull(new StringUtils());
    }

    // Tests equals with identical references and null inputs
    @Test
    public void testEquals_nullAndSameReferences_returnsExpected() {
        assertTrue(StringUtils.equals(null, null));
        assertFalse(StringUtils.equals(null, "abc"));
        assertFalse(StringUtils.equals("abc", null));
        CharSequence cs = new StringBuilder("abc");
        assertTrue(StringUtils.equals(cs, cs));
    }

    // Tests equals with String instances
    @Test
    public void testEquals_stringInstances_returnsCorrectComparison() {
        assertTrue(StringUtils.equals("abc", "abc"));
        assertFalse(StringUtils.equals("abc", "ABC"));
        assertFalse(StringUtils.equals("abc", "def"));
    }

    // Tests equals with CharSequence instances of equal content
    @Test
    public void testEquals_charSequenceEqualContent_returnsTrue() {
        assertTrue(StringUtils.equals("abc", new StringBuilder("abc")));
        assertTrue(StringUtils.equals(new StringBuilder("abc"), "abc"));
        assertTrue(StringUtils.equals(new StringBuilder("abc"), new StringBuilder("abc")));
    }

    // Tests equals with CharSequence instances of different lengths (bug detection)
    @Test
    public void testEquals_charSequenceDifferentLengths_returnsFalse() {
        assertFalse(StringUtils.equals("abc", new StringBuilder("abcd")));
        assertFalse(StringUtils.equals(new StringBuilder("abc"), "abcd"));
        assertFalse(StringUtils.equals(new StringBuilder("abcd"), "abc"));
        assertFalse(StringUtils.equals("abcd", new StringBuilder("abc")));
        assertFalse(StringUtils.equals(new StringBuilder("abc"), new StringBuilder("abcd")));
    }

    // Tests getByteBufferUtf8 method with valid and null input
    @Test
    public void testGetByteBufferUtf8_validAndNullInput_returnsCorrectBuffer() {
        assertNull(StringUtils.getByteBufferUtf8(null));
        ByteBuffer buffer = StringUtils.getByteBufferUtf8("Hello World");
        assertNotNull(buffer);
        assertEquals("Hello World", StringUtils.newStringUtf8(buffer.array()));
    }

    // Tests ISO-8859-1 getBytes and newString methods
    @Test
    public void testBytesAndString_iso8859_1_roundTripSuccess() {
        assertNull(StringUtils.getBytesIso8859_1(null));
        assertNull(StringUtils.newStringIso8859_1(null));

        byte[] bytes = StringUtils.getBytesIso8859_1("Hello");
        assertEquals("Hello", StringUtils.newStringIso8859_1(bytes));
    }

    // Tests US-ASCII getBytes and newString methods
    @Test
    public void testBytesAndString_usAscii_roundTripSuccess() {
        assertNull(StringUtils.getBytesUsAscii(null));
        assertNull(StringUtils.newStringUsAscii(null));

        byte[] bytes = StringUtils.getBytesUsAscii("Hello");
        assertEquals("Hello", StringUtils.newStringUsAscii(bytes));
    }

    // Tests UTF-8 getBytes and newString methods
    @Test
    public void testBytesAndString_utf8_roundTripSuccess() {
        assertNull(StringUtils.getBytesUtf8(null));
        assertNull(StringUtils.newStringUtf8(null));

        byte[] bytes = StringUtils.getBytesUtf8("Hello \u00e9");
        assertEquals("Hello \u00e9", StringUtils.newStringUtf8(bytes));
    }

    // Tests UTF-16 getBytes and newString methods
    @Test
    public void testBytesAndString_utf16_roundTripSuccess() {
        assertNull(StringUtils.getBytesUtf16(null));
        assertNull(StringUtils.newStringUtf16(null));

        byte[] bytes = StringUtils.getBytesUtf16("Hello");
        assertEquals("Hello", StringUtils.newStringUtf16(bytes));
    }

    // Tests UTF-16BE getBytes and newString methods
    @Test
    public void testBytesAndString_utf16Be_roundTripSuccess() {
        assertNull(StringUtils.getBytesUtf16Be(null));
        assertNull(StringUtils.newStringUtf16Be(null));

        byte[] bytes = StringUtils.getBytesUtf16Be("Hello");
        assertEquals("Hello", StringUtils.newStringUtf16Be(bytes));
    }

    // Tests UTF-16LE getBytes and newString methods
    @Test
    public void testBytesAndString_utf16Le_roundTripSuccess() {
        assertNull(StringUtils.getBytesUtf16Le(null));
        assertNull(StringUtils.newStringUtf16Le(null));

        byte[] bytes = StringUtils.getBytesUtf16Le("Hello");
        assertEquals("Hello", StringUtils.newStringUtf16Le(bytes));
    }

    // Tests getBytesUnchecked with valid and null input
    @Test
    public void testGetBytesUnchecked_validAndNullInput_returnsBytes() {
        assertNull(StringUtils.getBytesUnchecked(null, "UTF-8"));
        byte[] bytes = StringUtils.getBytesUnchecked("Hello", "UTF-8");
        assertArrayEquals(StringUtils.getBytesUtf8("Hello"), bytes);
    }

    // Tests getBytesUnchecked with invalid charset throwing IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testGetBytesUnchecked_invalidCharset_throwsIllegalStateException() {
        StringUtils.getBytesUnchecked("Hello", "INVALID_CHARSET_NAME");
    }

    // Tests newString with valid and null input
    @Test
    public void testNewString_validAndNullInput_returnsString() {
        assertNull(StringUtils.newString(null, "UTF-8"));
        byte[] bytes = StringUtils.getBytesUtf8("Hello");
        assertEquals("Hello", StringUtils.newString(bytes, "UTF-8"));
    }

    // Tests newString with invalid charset throwing IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testNewString_invalidCharset_throwsIllegalStateException() {
        StringUtils.newString(new byte[] { 0x48, 0x65 }, "INVALID_CHARSET_NAME");
    }
}