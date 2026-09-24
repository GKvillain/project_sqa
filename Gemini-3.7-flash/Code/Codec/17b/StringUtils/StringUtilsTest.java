package org.apache.commons.codec.binary;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;
import org.apache.commons.codec.CharEncoding;
import org.junit.Test;

/**
 * Unit tests for {@link StringUtils}.
 */
public class StringUtilsTest {

    // Tests constructor instantiation
    @Test
    public void testConstructor_instanceCreation_success() {
        assertNotNull(new StringUtils());
    }

    // Tests equals with both null arguments
    @Test
    public void testEquals_bothNull_returnsTrue() {
        assertTrue(StringUtils.equals(null, null));
    }

    // Tests equals with one null argument
    @Test
    public void testEquals_oneNull_returnsFalse() {
        assertFalse(StringUtils.equals(null, "abc"));
        assertFalse(StringUtils.equals("abc", null));
    }

    // Tests equals with identical and different String instances
    @Test
    public void testEquals_stringInstances_returnsCorrectComparison() {
        assertTrue(StringUtils.equals("abc", "abc"));
        assertFalse(StringUtils.equals("abc", "ABC"));
        assertFalse(StringUtils.equals("abc", "def"));
    }

    // Tests equals with non-String CharSequence implementations
    @Test
    public void testEquals_charSequenceImplementations_returnsCorrectComparison() {
        CharSequence cs1 = new StringBuilder("test");
        CharSequence cs2 = new StringBuilder("test");
        CharSequence cs3 = new StringBuilder("diff");

        assertTrue(StringUtils.equals(cs1, cs2));
        assertTrue(StringUtils.equals(cs1, "test"));
        assertFalse(StringUtils.equals(cs1, cs3));
    }

    // Tests getByteBufferUtf8 with normal string and null input
    @Test
    public void testGetByteBufferUtf8_stringAndNull_returnsExpected() {
        assertNull(StringUtils.getByteBufferUtf8(null));
        ByteBuffer buffer = StringUtils.getByteBufferUtf8("Hello");
        assertNotNull(buffer);
        assertEquals("Hello", new String(buffer.array(), 0, buffer.remaining()));
    }

    // Tests getBytesIso8859_1 with normal string and null input
    @Test
    public void testGetBytesIso8859_1_stringAndNull_returnsExpected() {
        assertNull(StringUtils.getBytesIso8859_1(null));
        byte[] bytes = StringUtils.getBytesIso8859_1("Hello");
        assertArrayEquals(new byte[]{'H', 'e', 'l', 'l', 'o'}, bytes);
    }

    // Tests getBytesUsAscii with normal string and null input
    @Test
    public void testGetBytesUsAscii_stringAndNull_returnsExpected() {
        assertNull(StringUtils.getBytesUsAscii(null));
        byte[] bytes = StringUtils.getBytesUsAscii("Hello");
        assertArrayEquals(new byte[]{'H', 'e', 'l', 'l', 'o'}, bytes);
    }

    // Tests getBytesUtf8 with normal string and null input
    @Test
    public void testGetBytesUtf8_stringAndNull_returnsExpected() {
        assertNull(StringUtils.getBytesUtf8(null));
        byte[] bytes = StringUtils.getBytesUtf8("Hello");
        assertArrayEquals(new byte[]{'H', 'e', 'l', 'l', 'o'}, bytes);
    }

    // Tests getBytesUtf16, getBytesUtf16Be, getBytesUtf16Le with normal strings and null input
    @Test
    public void testGetBytesUtf16Variants_stringAndNull_returnsExpected() {
        assertNull(StringUtils.getBytesUtf16(null));
        assertNull(StringUtils.getBytesUtf16Be(null));
        assertNull(StringUtils.getBytesUtf16Le(null));

        assertNotNull(StringUtils.getBytesUtf16("Hello"));
        assertNotNull(StringUtils.getBytesUtf16Be("Hello"));
        assertNotNull(StringUtils.getBytesUtf16Le("Hello"));
    }

    // Tests getBytesUnchecked with valid charset and null input
    @Test
    public void testGetBytesUnchecked_validCharsetAndNull_returnsExpected() {
        assertNull(StringUtils.getBytesUnchecked(null, CharEncoding.UTF_8));
        byte[] bytes = StringUtils.getBytesUnchecked("Hello", CharEncoding.UTF_8);
        assertArrayEquals(new byte[]{'H', 'e', 'l', 'l', 'o'}, bytes);
    }

    // Tests getBytesUnchecked with unsupported charset throws IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testGetBytesUnchecked_invalidCharset_throwsIllegalStateException() {
        StringUtils.getBytesUnchecked("Hello", "INVALID_CHARSET_NAME");
    }

    // Tests newString with valid charset and null input
    @Test
    public void testNewString_validCharsetAndNull_returnsExpected() {
        assertNull(StringUtils.newString(null, CharEncoding.UTF_8));
        byte[] bytes = new byte[]{'H', 'e', 'l', 'l', 'o'};
        assertEquals("Hello", StringUtils.newString(bytes, CharEncoding.UTF_8));
    }

    // Tests newString with unsupported charset throws IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testNewString_invalidCharset_throwsIllegalStateException() {
        byte[] bytes = new byte[]{'H', 'e', 'l', 'l', 'o'};
        StringUtils.newString(bytes, "INVALID_CHARSET_NAME");
    }

    // Tests newStringIso8859_1 with valid bytes and null input (Defects4J Codec-17)
    @Test
    public void testNewStringIso8859_1_bytesAndNull_returnsExpected() {
        assertNull(StringUtils.newStringIso8859_1(null));
        byte[] bytes = new byte[]{'H', 'e', 'l', 'l', 'o'};
        assertEquals("Hello", StringUtils.newStringIso8859_1(bytes));
    }

    // Tests newStringUsAscii with valid bytes and null input
    @Test
    public void testNewStringUsAscii_bytesAndNull_returnsExpected() {
        assertNull(StringUtils.newStringUsAscii(null));
        byte[] bytes = new byte[]{'H', 'e', 'l', 'l', 'o'};
        assertEquals("Hello", StringUtils.newStringUsAscii(bytes));
    }

    // Tests newStringUtf8 with valid bytes and null input
    @Test
    public void testNewStringUtf8_bytesAndNull_returnsExpected() {
        assertNull(StringUtils.newStringUtf8(null));
        byte[] bytes = new byte[]{'H', 'e', 'l', 'l', 'o'};
        assertEquals("Hello", StringUtils.newStringUtf8(bytes));
    }

    // Tests newStringUtf16, newStringUtf16Be, newStringUtf16Le with valid bytes and null input
    @Test
    public void testNewStringUtf16Variants_bytesAndNull_returnsExpected() {
        assertNull(StringUtils.newStringUtf16(null));
        assertNull(StringUtils.newStringUtf16Be(null));
        assertNull(StringUtils.newStringUtf16Le(null));

        byte[] utf16BeBytes = StringUtils.getBytesUtf16Be("Hello");
        byte[] utf16LeBytes = StringUtils.getBytesUtf16Le("Hello");
        byte[] utf16Bytes = StringUtils.getBytesUtf16("Hello");

        assertEquals("Hello", StringUtils.newStringUtf16Be(utf16BeBytes));
        assertEquals("Hello", StringUtils.newStringUtf16Le(utf16LeBytes));
        assertEquals("Hello", StringUtils.newStringUtf16(utf16Bytes));
    }
}