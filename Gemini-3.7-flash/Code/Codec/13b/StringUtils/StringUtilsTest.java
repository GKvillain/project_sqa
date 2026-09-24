package org.apache.commons.codec.binary;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.commons.codec.CharEncoding;
import org.junit.Test;

public class StringUtilsTest {

    private static final String TEST_STRING = "Hello World!";

    // Tests default constructor instantiation
    @Test
    public void testConstructor_instanceCreation_shouldNotBeNull() {
        final StringUtils stringUtils = new StringUtils();
        assertNotNull(stringUtils);
    }

    // Tests getBytesIso8859_1 with valid string
    @Test
    public void testGetBytesIso8859_1_validString_returnsEncodedBytes() {
        final byte[] bytes = StringUtils.getBytesIso8859_1(TEST_STRING);
        assertNotNull(bytes);
        assertEquals(TEST_STRING, StringUtils.newStringIso8859_1(bytes));
    }

    // Tests getBytesIso8859_1 with null input
    @Test
    public void testGetBytesIso8859_1_nullString_returnsNull() {
        assertNull(StringUtils.getBytesIso8859_1(null));
    }

    // Tests getBytesUsAscii with valid string
    @Test
    public void testGetBytesUsAscii_validString_returnsEncodedBytes() {
        final byte[] bytes = StringUtils.getBytesUsAscii(TEST_STRING);
        assertNotNull(bytes);
        assertEquals(TEST_STRING, StringUtils.newStringUsAscii(bytes));
    }

    // Tests getBytesUsAscii with null input
    @Test
    public void testGetBytesUsAscii_nullString_returnsNull() {
        assertNull(StringUtils.getBytesUsAscii(null));
    }

    // Tests getBytesUtf8 with valid string
    @Test
    public void testGetBytesUtf8_validString_returnsEncodedBytes() {
        final byte[] bytes = StringUtils.getBytesUtf8(TEST_STRING);
        assertNotNull(bytes);
        assertEquals(TEST_STRING, StringUtils.newStringUtf8(bytes));
    }

    // Tests getBytesUtf8 with null input
    @Test
    public void testGetBytesUtf8_nullString_returnsNull() {
        assertNull(StringUtils.getBytesUtf8(null));
    }

    // Tests getBytesUtf16 with valid string
    @Test
    public void testGetBytesUtf16_validString_returnsEncodedBytes() {
        final byte[] bytes = StringUtils.getBytesUtf16(TEST_STRING);
        assertNotNull(bytes);
        assertEquals(TEST_STRING, StringUtils.newStringUtf16(bytes));
    }

    // Tests getBytesUtf16 with null input
    @Test
    public void testGetBytesUtf16_nullString_returnsNull() {
        assertNull(StringUtils.getBytesUtf16(null));
    }

    // Tests getBytesUtf16Be with valid string
    @Test
    public void testGetBytesUtf16Be_validString_returnsEncodedBytes() {
        final byte[] bytes = StringUtils.getBytesUtf16Be(TEST_STRING);
        assertNotNull(bytes);
        assertEquals(TEST_STRING, StringUtils.newStringUtf16Be(bytes));
    }

    // Tests getBytesUtf16Be with null input
    @Test
    public void testGetBytesUtf16Be_nullString_returnsNull() {
        assertNull(StringUtils.getBytesUtf16Be(null));
    }

    // Tests getBytesUtf16Le with valid string
    @Test
    public void testGetBytesUtf16Le_validString_returnsEncodedBytes() {
        final byte[] bytes = StringUtils.getBytesUtf16Le(TEST_STRING);
        assertNotNull(bytes);
        assertEquals(TEST_STRING, StringUtils.newStringUtf16Le(bytes));
    }

    // Tests getBytesUtf16Le with null input
    @Test
    public void testGetBytesUtf16Le_nullString_returnsNull() {
        assertNull(StringUtils.getBytesUtf16Le(null));
    }

    // Tests getBytesUnchecked with valid charset
    @Test
    public void testGetBytesUnchecked_validCharset_returnsEncodedBytes() {
        final byte[] bytes = StringUtils.getBytesUnchecked(TEST_STRING, CharEncoding.UTF_8);
        assertNotNull(bytes);
        assertEquals(TEST_STRING, StringUtils.newString(bytes, CharEncoding.UTF_8));
    }

    // Tests getBytesUnchecked with null string
    @Test
    public void testGetBytesUnchecked_nullString_returnsNull() {
        assertNull(StringUtils.getBytesUnchecked(null, CharEncoding.UTF_8));
    }

    // Tests getBytesUnchecked with unsupported charset throws IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testGetBytesUnchecked_invalidCharset_throwsIllegalStateException() {
        StringUtils.getBytesUnchecked(TEST_STRING, "INVALID_CHARSET_NAME");
    }

    // Tests newString with null bytes
    @Test
    public void testNewString_nullBytes_returnsNull() {
        assertNull(StringUtils.newString(null, CharEncoding.UTF_8));
    }

    // Tests newString with unsupported charset throws IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testNewString_invalidCharset_throwsIllegalStateException() {
        StringUtils.newString(new byte[] { 65, 66 }, "INVALID_CHARSET_NAME");
    }

    // Tests newStringIso8859_1 with null bytes
    @Test
    public void testNewStringIso8859_1_nullBytes_returnsNull() {
        assertNull(StringUtils.newStringIso8859_1(null));
    }

    // Tests newStringUsAscii with null bytes
    @Test
    public void testNewStringUsAscii_nullBytes_returnsNull() {
        assertNull(StringUtils.newStringUsAscii(null));
    }

    // Tests newStringUtf16 with null bytes
    @Test
    public void testNewStringUtf16_nullBytes_returnsNull() {
        assertNull(StringUtils.newStringUtf16(null));
    }

    // Tests newStringUtf16Be with null bytes
    @Test
    public void testNewStringUtf16Be_nullBytes_returnsNull() {
        assertNull(StringUtils.newStringUtf16Be(null));
    }

    // Tests newStringUtf16Le with null bytes
    @Test
    public void testNewStringUtf16Le_nullBytes_returnsNull() {
        assertNull(StringUtils.newStringUtf16Le(null));
    }

    // Tests newStringUtf8 with null bytes
    @Test
    public void testNewStringUtf8_nullBytes_returnsNull() {
        assertNull(StringUtils.newStringUtf8(null));
    }

    // Tests equals with both null
    @Test
    public void testEquals_bothNull_returnsTrue() {
        assertTrue(StringUtils.equals(null, null));
    }

    // Tests equals with first argument null
    @Test
    public void testEquals_firstNull_returnsFalse() {
        assertFalse(StringUtils.equals(null, "abc"));
    }

    // Tests equals with second argument null
    @Test
    public void testEquals_secondNull_returnsFalse() {
        assertFalse(StringUtils.equals("abc", null));
    }

    // Tests equals with same instance
    @Test
    public void testEquals_sameInstance_returnsTrue() {
        assertTrue(StringUtils.equals(TEST_STRING, TEST_STRING));
    }

    // Tests equals with equal String instances
    @Test
    public void testEquals_equalStrings_returnsTrue() {
        assertTrue(StringUtils.equals("abc", new String("abc")));
    }

    // Tests equals with different String instances of same length
    @Test
    public void testEquals_differentStringsSameLength_returnsFalse() {
        assertFalse(StringUtils.equals("abc", "def"));
    }

    // Tests equals with different length strings
    @Test
    public void testEquals_differentLengthStrings_returnsFalse() {
        assertFalse(StringUtils.equals("abc", "abcd"));
        assertFalse(StringUtils.equals("abcd", "abc"));
    }

    // Tests equals with non-String CharSequence equal contents
    @Test
    public void testEquals_charSequenceEqual_returnsTrue() {
        assertTrue(StringUtils.equals(new StringBuilder("abc"), new StringBuffer("abc")));
    }

    // Tests equals with non-String CharSequence different contents same length
    @Test
    public void testEquals_charSequenceDifferentSameLength_returnsFalse() {
        assertFalse(StringUtils.equals(new StringBuilder("abc"), new StringBuffer("abd")));
    }

    // Tests equals with non-String CharSequence different length
    @Test
    public void testEquals_charSequenceDifferentLength_returnsFalse() {
        assertFalse(StringUtils.equals(new StringBuilder("abc"), new StringBuffer("ab")));
        assertFalse(StringUtils.equals(new StringBuilder("ab"), new StringBuffer("abc")));
    }
}