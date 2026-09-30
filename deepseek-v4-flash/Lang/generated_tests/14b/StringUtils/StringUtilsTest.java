package org.apache.commons.lang3;

import org.junit.Test;
import static org.junit.Assert.*;

public class StringUtilsTest {

    // Tests null input for isEmpty
    @Test
    public void testIsEmpty_null_returnsTrue() {
        assertTrue(StringUtils.isEmpty(null));
    }

    // Tests whitespace only input for isBlank
    @Test
    public void testIsBlank_whitespace_returnsTrue() {
        assertTrue(StringUtils.isBlank("   "));
    }

    // Tests trimming of spaces
    @Test
    public void testTrim_spaces_returnsEmpty() {
        assertEquals("", StringUtils.trim("   "));
    }

    // Tests both null equals
    @Test
    public void testEquals_bothNull_returnsTrue() {
        assertTrue(StringUtils.equals(null, null));
    }

    // Tests case insensitive equals
    @Test
    public void testEqualsIgnoreCase_differentCase_returnsTrue() {
        assertTrue(StringUtils.equalsIgnoreCase("abc", "ABC"));
    }

    // Tests indexOf char not found
    @Test
    public void testIndexOf_charNotFound_returnsMinusOne() {
        assertEquals(-1, StringUtils.indexOf("abc", 'z'));
    }

    // Tests contains with null input
    @Test
    public void testContains_nullInput_returnsFalse() {
        assertFalse(StringUtils.contains(null, 'a'));
    }

    // Tests containsAny with char array found
    @Test
    public void testContainsAny_charArrayFound_returnsTrue() {
        assertTrue(StringUtils.containsAny("zzabyycdxx", 'z', 'a'));
    }

    // Tests substring with negative start
    @Test
    public void testSubstring_negativeStart_returnsSubstringFromEnd() {
        assertEquals("bc", StringUtils.substring("abc", -2));
    }

    // Tests substringBetween with both empty open/close
    @Test
    public void testSubstringBetween_bothEmpty_returnsEmptyString() {
        assertEquals("", StringUtils.substringBetween("abc", "", ""));
    }

    // Tests substringBetween with null open
    @Test
    public void testSubstringBetween_openNull_returnsNull() {
        assertNull(StringUtils.substringBetween("abc", null, "b"));
    }

    // Tests replace with max limit
    @Test
    public void testReplace_maxLimited_returnsPartialReplace() {
        assertEquals("zba", StringUtils.replace("aba", "a", "z", 1));
    }

    // Tests normal replaceEach
    @Test
    public void testReplaceEach_normal_returnsReplaced() {
        assertEquals("wcte", StringUtils.replaceEach("abcde", new String[]{"ab", "d"}, new String[]{"w", "t"}));
    }

    // Tests replaceEach with mismatched array lengths
    @Test(expected = IllegalArgumentException.class)
    public void testReplaceEach_mismatchedLength_throwsException() {
        StringUtils.replaceEach("abc", new String[]{"a"}, new String[]{"b","c"});
    }

    // Tests replaceEachRepeatedly with circular replacement
    @Test(expected = IllegalStateException.class)
    public void testReplaceEachRepeatedly_circular_throwsException() {
        StringUtils.replaceEachRepeatedly("abc", new String[]{"ab", "d"}, new String[]{"d", "ab"});
    }

    // Tests split with null input
    @Test
    public void testSplit_nullInput_returnsNull() {
        assertNull(StringUtils.split(null));
    }

    // Tests join with array and separator
    @Test
    public void testJoin_arrayWithSeparator_returnsJoined() {
        assertEquals("a;b;c", StringUtils.join(new String[]{"a","b","c"}, ';'));
    }

    // Tests capitalize on lowercase string
    @Test
    public void testCapitalize_lowercase_returnsCapitalized() {
        assertEquals("Cat", StringUtils.capitalize("cat"));
    }

    // Tests isAlpha with null input
    @Test
    public void testIsAlpha_nullInput_returnsFalse() {
        assertFalse(StringUtils.isAlpha(null));
    }

    // Tests isNumeric with digits only
    @Test
    public void testIsNumeric_digitsOnly_returnsTrue() {
        assertTrue(StringUtils.isNumeric("123"));
    }

    // Additional tests for uncovered methods

    @Test
    public void testIsNotEmpty_null_returnsFalse() {
        assertFalse(StringUtils.isNotEmpty(null));
    }

    @Test
    public void testIsNotEmpty_empty_returnsFalse() {
        assertFalse(StringUtils.isNotEmpty(""));
    }

    @Test
    public void testIsNotEmpty_nonEmpty_returnsTrue() {
        assertTrue(StringUtils.isNotEmpty("abc"));
    }

    @Test
    public void testIsNotBlank_null_returnsFalse() {
        assertFalse(StringUtils.isNotBlank(null));
    }

    @Test
    public void testIsNotBlank_empty_returnsFalse() {
        assertFalse(StringUtils.isNotBlank(""));
    }

    @Test
    public void testIsNotBlank_whitespace_returnsFalse() {
        assertFalse(StringUtils.isNotBlank("   "));
    }

    @Test
    public void testIsNotBlank_nonBlank_returnsTrue() {
        assertTrue(StringUtils.isNotBlank("abc"));
    }

    @Test
    public void testTrimToEmpty_null_returnsEmpty() {
        assertEquals("", StringUtils.trimToEmpty(null));
    }

    @Test
    public void testTrimToEmpty_spaces_returnsEmpty() {
        assertEquals("", StringUtils.trimToEmpty("   "));
    }

    @Test
    public void testTrimToEmpty_nonTrim_returnsSame() {
        assertEquals("abc", StringUtils.trimToEmpty("abc"));
    }

    @Test
    public void testStrip_null_returnsNull() {
        assertNull(StringUtils.strip(null));
    }

    @Test
    public void testStrip_spaces_returnsEmpty() {
        assertEquals("", StringUtils.strip("   "));
    }

    @Test
    public void testStrip_leadingTrailingSpaces_returnsTrimmed() {
        assertEquals("abc", StringUtils.strip("  abc  "));
    }

    @Test
    public void testStartsWith_normal_returnsTrue() {
        assertTrue(StringUtils.startsWith("abcde", "abc"));
    }

    @Test
    public void testStartsWith_notStart_returnsFalse() {
        assertFalse(StringUtils.startsWith("abcde", "bcd"));
    }

    @Test
    public void testStartsWith_nullPrefix_returnsTrue() {
        assertTrue(StringUtils.startsWith("abc", null));
    }

    @Test
    public void testEndsWith_normal_returnsTrue() {
        assertTrue(StringUtils.endsWith("abcde", "cde"));
    }

    @Test
    public void testEndsWith_notEnd_returnsFalse() {
        assertFalse(StringUtils.endsWith("abcde", "abc"));
    }

    @Test
    public void testEndsWith_nullSuffix_returnsTrue() {
        assertTrue(StringUtils.endsWith("abc", null));
    }

    @Test
    public void testSubstringBefore_normal_returnsBefore() {
        assertEquals("abc", StringUtils.substringBefore("abc-def", "-"));
    }

    @Test
    public void testSubstringBefore_noMatch_returnsSame() {
        assertEquals("abcdef", StringUtils.substringBefore("abcdef", "-"));
    }

    @Test
    public void testSubstringAfter_normal_returnsAfter() {
        assertEquals("def", StringUtils.substringAfter("abc-def", "-"));
    }

    @Test
    public void testSubstringAfter_noMatch_returnsEmpty() {
        assertEquals("", StringUtils.substringAfter("abcdef", "-"));
    }

    @Test
    public void testRemoveStart_normal_returnsRemoved() {
        assertEquals("def", StringUtils.removeStart("abcdef", "abc"));
    }

    @Test
    public void testRemoveStart_notStart_returnsSame() {
        assertEquals("abcdef", StringUtils.removeStart("abcdef", "bc"));
    }

    @Test
    public void testRemoveEnd_normal_returnsRemoved() {
        assertEquals("abc", StringUtils.removeEnd("abcdef", "def"));
    }

    @Test
    public void testRemoveEnd_notEnd_returnsSame() {
        assertEquals("abcdef", StringUtils.removeEnd("abcdef", "bc"));
    }

    @Test
    public void testRepeat_twice_returnsRepeated() {
        assertEquals("abab", StringUtils.repeat("ab", 2));
    }

    @Test
    public void testRepeat_zero_returnsEmpty() {
        assertEquals("", StringUtils.repeat("ab", 0));
    }

    @Test
    public void testCenter_normal_returnsCentered() {
        assertEquals(" abc ", StringUtils.center("abc", 5));
    }

    @Test
    public void testCenter_alreadyLonger_returnsSame() {
        assertEquals("abcdef", StringUtils.center("abcdef", 3));
    }

    @Test
    public void testLeftPad_normal_returnsPadded() {
        assertEquals("  abc", StringUtils.leftPad("abc", 5));
    }

    @Test
    public void testLeftPad_withChar_returnsPadded() {
        assertEquals("--abc", StringUtils.leftPad("abc", 5, '-'));
    }

    @Test
    public void testRightPad_normal_returnsPadded() {
        assertEquals("abc  ", StringUtils.rightPad("abc", 5));
    }

    @Test
    public void testRightPad_withChar_returnsPadded() {
        assertEquals("abc--", StringUtils.rightPad("abc", 5, '-'));
    }

    @Test
    public void testLowerCase_returnsLower() {
        assertEquals("abc", StringUtils.lowerCase("ABC"));
    }

    @Test
    public void testUpperCase_returnsUpper() {
        assertEquals("ABC", StringUtils.upperCase("abc"));
    }

    @Test
    public void testDefaultString_null_returnsEmpty() {
        assertEquals("", StringUtils.defaultString(null));
    }

    @Test
    public void testDefaultString_nonNull_returnsSame() {
        assertEquals("abc", StringUtils.defaultString("abc"));
    }

    @Test
    public void testDefaultString_nullWithDefault_returnsDefault() {
        assertEquals("default", StringUtils.defaultString(null, "default"));
    }

    @Test
    public void testAbbreviate_normal_returnsAbbreviated() {
        assertEquals("abc...", StringUtils.abbreviate("abcdefghij", 6));
    }

    @Test
    public void testAbbreviate_shorter_returnsSame() {
        assertEquals("abc", StringUtils.abbreviate("abc", 5));
    }

    @Test
    public void testDeleteWhitespace_returnsNoWhitespace() {
        assertEquals("abc", StringUtils.deleteWhitespace("a b c"));
    }

    @Test
    public void testDeleteWhitespace_null_returnsNull() {
        assertNull(StringUtils.deleteWhitespace(null));
    }

    @Test
    public void testIsAlphanumeric_alphaNum_returnsTrue() {
        assertTrue(StringUtils.isAlphanumeric("abc123"));
    }

    @Test
    public void testIsAlphanumeric_withSpace_returnsFalse() {
        assertFalse(StringUtils.isAlphanumeric("abc 123"));
    }
}