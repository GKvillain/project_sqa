package org.apache.commons.lang3;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * JUnit 4 test class for StringUtils, targeting Defects4J bug 39b.
 */
public class StringUtilsTest {

    // Tests for isEmpty
    @Test
    public void testIsEmpty_null_returnsTrue() {
        assertTrue(StringUtils.isEmpty(null));
    }

    @Test
    public void testIsEmpty_emptyString_returnsTrue() {
        assertTrue(StringUtils.isEmpty(""));
    }

    @Test
    public void testIsEmpty_nonEmptyString_returnsFalse() {
        assertFalse(StringUtils.isEmpty(" "));
        assertFalse(StringUtils.isEmpty("abc"));
    }

    // Tests for isBlank
    @Test
    public void testIsBlank_null_returnsTrue() {
        assertTrue(StringUtils.isBlank(null));
    }

    @Test
    public void testIsBlank_onlyWhitespace_returnsTrue() {
        assertTrue(StringUtils.isBlank(" "));
        assertTrue(StringUtils.isBlank("\t\n "));
    }

    @Test
    public void testIsBlank_nonBlank_returnsFalse() {
        assertFalse(StringUtils.isBlank("abc"));
        assertFalse(StringUtils.isBlank("  abc  "));
    }

    // Tests for trim
    @Test
    public void testTrim_null_returnsNull() {
        assertNull(StringUtils.trim(null));
    }

    @Test
    public void testTrim_emptyString_returnsEmpty() {
        assertEquals("", StringUtils.trim(""));
    }

    @Test
    public void testTrim_whitespace_returnsEmpty() {
        assertEquals("", StringUtils.trim("   "));
    }

    @Test
    public void testTrim_leadingTrailingSpaces_returnsTrimmed() {
        assertEquals("abc", StringUtils.trim("  abc  "));
    }

    // Tests for equals
    @Test
    public void testEquals_nullBoth_returnsTrue() {
        assertTrue(StringUtils.equals(null, null));
    }

    @Test
    public void testEquals_oneNull_returnsFalse() {
        assertFalse(StringUtils.equals(null, "abc"));
        assertFalse(StringUtils.equals("abc", null));
    }

    @Test
    public void testEquals_caseSensitive_returnsCorrect() {
        assertTrue(StringUtils.equals("abc", "abc"));
        assertFalse(StringUtils.equals("abc", "ABC"));
    }

    // Tests for indexOf (String, String)
    @Test
    public void testIndexOf_nullString_returnsMinusOne() {
        assertEquals(-1, StringUtils.indexOf(null, "abc"));
    }

    @Test
    public void testIndexOf_nullSearch_returnsMinusOne() {
        assertEquals(-1, StringUtils.indexOf("abc", null));
    }

    @Test
    public void testIndexOf_found_returnsIndex() {
        assertEquals(1, StringUtils.indexOf("abc", "bc"));
    }

    // Tests for contains
    @Test
    public void testContains_nullStr_returnsFalse() {
        assertFalse(StringUtils.contains(null, "a"));
    }

    @Test
    public void testContains_nullSearch_returnsFalse() {
        assertFalse(StringUtils.contains("abc", null));
    }

    @Test
    public void testContains_found_returnsTrue() {
        assertTrue(StringUtils.contains("abc", "a"));
    }

    // Tests for substring (int start)
    @Test
    public void testSubstring_negativeStart_returnsLastChars() {
        assertEquals("bc", StringUtils.substring("abc", -2));
    }

    @Test
    public void testSubstring_startBeyondLength_returnsEmpty() {
        assertEquals("", StringUtils.substring("abc", 10));
    }

    // Tests for replaceEach (core of bug 39b)
    @Test
    public void testReplaceEach_nullText_returnsNull() {
        assertNull(StringUtils.replaceEach(null, new String[]{"a"}, new String[]{"b"}));
    }

    @Test
    public void testReplaceEach_nullSearchList_returnsOriginalText() {
        assertEquals("abc", StringUtils.replaceEach("abc", null, new String[]{"x"}));
    }

    @Test
    public void testReplaceEach_withNullElementInSearch_ignoresIt() {
        // Bug: null in searchList caused NPE; fixed version should ignore and continue
        String result = StringUtils.replaceEach("ab", new String[]{"a", null}, new String[]{"x", "y"});
        assertEquals("xb", result);
    }

    @Test
    public void testReplaceEach_withNullElementInReplacement_ignoresIt() {
        // Bug: null replacement element should be ignored (treated as null)
        // The replacement for "a" is null, so it is skipped. Only "b" is replaced with "y", resulting in "ay".
        String result = StringUtils.replaceEach("ab", new String[]{"a", "b"}, new String[]{null, "y"});
        assertEquals("ay", result);
    }

    // Test for replaceEachRepeatedly with null replacement element
    @Test
    public void testReplaceEachRepeatedly_nullReplacement_ignores() {
        String result = StringUtils.replaceEachRepeatedly("ab", new String[]{"a", "b"}, new String[]{null, "y"});
        // Same logic: "a" ignored, "b" replaced with "y", no repeat because "y" not in search
        assertEquals("ay", result);
    }

    // Test for replaceEachRepeatedly with circular reference (a->aa) should throw IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testReplaceEachRepeatedly_circularReference_throwsIllegalState() {
        StringUtils.replaceEachRepeatedly("a", new String[]{"a"}, new String[]{"aa"});
    }

    // Tests for startsWithIgnoreCase
    @Test
    public void testStartsWithIgnoreCase_nullPrefix_returnsFalse() {
        assertFalse(StringUtils.startsWithIgnoreCase("abc", null));
    }

    @Test
    public void testStartsWithIgnoreCase_caseInsensitive_returnsTrue() {
        assertTrue(StringUtils.startsWithIgnoreCase("ABCDEF", "abc"));
    }

    // Tests for endsWith
    @Test
    public void testEndsWith_nullSuffix_returnsFalse() {
        assertFalse(StringUtils.endsWith("abc", null));
    }

    @Test
    public void testEndsWith_caseSensitive_returnsCorrect() {
        assertTrue(StringUtils.endsWith("abcdef", "def"));
        assertFalse(StringUtils.endsWith("abcdef", "DEF"));
    }

    // Tests for strip (null stripChars)
    @Test
    public void testStrip_nullInput_returnsNull() {
        assertNull(StringUtils.strip(null, "xy"));
    }

    @Test
    public void testStrip_stripCharsNull_usesWhitespace() {
        assertEquals("abc", StringUtils.strip("  abc  ", null));
    }

}