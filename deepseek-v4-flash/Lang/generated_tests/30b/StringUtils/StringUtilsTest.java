package org.apache.commons.lang3;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class StringUtilsTest {

    // Tests null and empty handling for isEmpty()
    @Test
    public void testIsEmpty_nullAndEmpty_returnsTrue() {
        assertTrue(StringUtils.isEmpty(null));
        assertTrue(StringUtils.isEmpty(""));
        assertFalse(StringUtils.isEmpty(" "));
        assertFalse(StringUtils.isEmpty("bob"));
    }

    // Tests null, empty, and whitespace handling for isBlank()
    @Test
    public void testIsBlank_nullEmptyWhitespace_returnsTrue() {
        assertTrue(StringUtils.isBlank(null));
        assertTrue(StringUtils.isBlank(""));
        assertTrue(StringUtils.isBlank(" \t\n"));
        assertFalse(StringUtils.isBlank("bob"));
        assertFalse(StringUtils.isBlank("  bob  "));
    }

    // Tests trim and strip methods with null, empty, and text inputs
    @Test
    public void testTrimAndStrip_variousInputs() {
        assertNull(StringUtils.trim(null));
        assertEquals("", StringUtils.trim("   "));
        assertEquals("abc", StringUtils.trim("  abc  "));
        assertNull(StringUtils.trimToNull("   "));
        assertEquals("abc", StringUtils.trimToNull(" abc "));
        assertEquals("", StringUtils.stripToEmpty(null));
        assertEquals("ab c", StringUtils.strip(" ab c "));
        assertEquals("abc  ", StringUtils.stripStart("yxabc  ", "xyz"));
        assertEquals("  abc", StringUtils.stripEnd("  abcyx", "xyz"));
    }

    // Tests equals and equalsIgnoreCase null-safe behavior
    @Test
    public void testEquals_nullSafeAndCaseInsensitive() {
        assertTrue(StringUtils.equals(null, null));
        assertFalse(StringUtils.equals(null, "abc"));
        assertTrue(StringUtils.equals("abc", "abc"));
        assertFalse(StringUtils.equals("abc", "ABC"));
        assertTrue(StringUtils.equalsIgnoreCase(null, null));
        assertTrue(StringUtils.equalsIgnoreCase("abc", "ABC"));
        assertFalse(StringUtils.equalsIgnoreCase("abc", null));
    }

    // Tests indexOf and contains with normal and boundary inputs
    @Test
    public void testIndexOfAndContains_variousInputs() {
        assertEquals(-1, StringUtils.indexOf(null, 'a'));
        assertEquals(2, StringUtils.indexOf("aabaabaa", 'b'));
        assertEquals(5, StringUtils.indexOf("aabaabaa", 'b', 3));
        assertEquals(-1, StringUtils.indexOf("aabaabaa", 'b', 9));
        assertTrue(StringUtils.contains("abc", 'b'));
        assertFalse(StringUtils.contains(null, 'a'));
        assertTrue(StringUtils.containsIgnoreCase("abc", "B"));
    }

    // Tests substring with negative indexes and out-of-range values
    @Test
    public void testSubstring_boundaryAndNegativeIndexes() {
        assertNull(StringUtils.substring(null, 0));
        assertEquals("bc", StringUtils.substring("abc", -2));
        assertEquals("", StringUtils.substring("abc", 4));
        assertEquals("ab", StringUtils.substring("abc", 0, 2));
        assertEquals("b", StringUtils.substring("abc", -2, -1));
        assertEquals("", StringUtils.substring("abc", 2, 0));
    }

    // Tests substringBefore/After/BeforeLast/AfterLast/Between
    @Test
    public void testSubstringBeforeAfterBetween_variousInputs() {
        assertEquals("abc", StringUtils.substringBefore("abc", "d"));
        assertEquals("cba", StringUtils.substringAfter("abcba", "b"));
        assertEquals("abc", StringUtils.substringBeforeLast("abcba", "b"));
        assertEquals("a", StringUtils.substringAfterLast("abcba", "b"));
        assertEquals("abc", StringUtils.substringBetween("tagabctag", "tag"));
        assertNull(StringUtils.substringBetween("abc", "[", "]"));
        assertArrayEquals(new String[] {"a", "b", "c"},
            StringUtils.substringsBetween("[a][b][c]", "[", "]"));
    }

    // Tests split, splitPreserveAllTokens, splitByCharacterType, and join
    @Test
    public void testSplitAndJoin_variousInputs() {
        assertArrayEquals(new String[] {"abc", "def"}, StringUtils.split("abc def"));
        assertArrayEquals(new String[] {"ab", "cd:ef"}, StringUtils.split("ab:cd:ef", ":", 2));
        assertArrayEquals(new String[] {"ab", "", "c"}, StringUtils.splitPreserveAllTokens("ab::c", ":"));
        assertArrayEquals(new String[] {"number", "5"}, StringUtils.splitByCharacterType("number5"));
        assertArrayEquals(new String[] {"foo", "Bar"}, StringUtils.splitByCharacterTypeCamelCase("fooBar"));
        assertEquals("a;b;c", StringUtils.join(new Object[] {"a", "b", "c"}, ';'));
        assertEquals("a--b", StringUtils.join(new Object[] {"a", "b"}, "--"));
        assertEquals("", StringUtils.join(new Object[0], ','));
    }

    // Tests remove, replace, replaceChars, and replaceEach
    @Test
    public void testRemoveAndReplace_variousInputs() {
        assertNull(StringUtils.remove(null, "a"));
        assertEquals("qd", StringUtils.remove("queued", "ue"));
        assertEquals("qeed", StringUtils.remove("queued", 'u'));
        assertEquals("zbz", StringUtils.replace("aba", "a", "z"));
        assertEquals("zbaa", StringUtils.replace("abaa", "a", "z", 1));
        assertEquals("ac", StringUtils.replaceChars("abc", "b", null));
        assertEquals("aycya", StringUtils.replaceChars("abcba", 'b', 'y'));
        assertEquals("wcte", StringUtils.replaceEach("abcde", new String[] {"ab", "d"}, new String[] {"w", "t"}));
    }

    // Tests repeat, including the Defects4J Lang-30 empty string edge case
    @Test
    public void testRepeat_variousInputs() {
        assertNull(StringUtils.repeat(null, 2));
        assertEquals("", StringUtils.repeat("", 3));
        assertEquals("aaa", StringUtils.repeat("a", 3));
        assertEquals("abab", StringUtils.repeat("ab", 2));
        assertEquals("", StringUtils.repeat("a", -1));
        assertEquals("?, ?, ?", StringUtils.repeat("?", ", ", 3));
        assertEquals("xxx", StringUtils.repeat("", "x", 3));
    }

    // Tests defaultString and case conversion methods
    @Test
    public void testDefaultStringAndCaseConversion_variousInputs() {
        assertEquals("", StringUtils.defaultString(null));
        assertEquals("bat", StringUtils.defaultString("bat"));
        assertEquals("NULL", StringUtils.defaultString(null, "NULL"));
        assertEquals("ABC", StringUtils.upperCase("aBc"));
        assertEquals("abc", StringUtils.lowerCase("aBc"));
        assertEquals("Cat", StringUtils.capitalize("cat"));
        assertEquals("cAT", StringUtils.uncapitalize("CAT"));
        assertEquals("tHE dOG", StringUtils.swapCase("The Dog"));
    }

    // Tests character classification methods
    @Test
    public void testCharacterTypeChecks_variousInputs() {
        assertFalse(StringUtils.isAlpha(null));
        assertTrue(StringUtils.isAlpha("abc"));
        assertFalse(StringUtils.isAlpha("ab2"));
        assertTrue(StringUtils.isAlphaSpace("ab c"));
        assertTrue(StringUtils.isAlphanumeric("ab2c"));
        assertTrue(StringUtils.isNumeric("123"));
        assertFalse(StringUtils.isNumeric("12.3"));
        assertTrue(StringUtils.isNumericSpace("12 3"));
        assertTrue(StringUtils.isWhitespace(" \t"));
        assertFalse(StringUtils.isAllLowerCase(""));
        assertTrue(StringUtils.isAllLowerCase("abc"));
        assertTrue(StringUtils.isAllUpperCase("ABC"));
    }

    // Tests startsWith, endsWith, and their case-insensitive variants
    @Test
    public void testStartsAndEndsWith_variousInputs() {
        assertTrue(StringUtils.startsWith(null, null));
        assertFalse(StringUtils.startsWith("abcdef", null));
        assertTrue(StringUtils.startsWith("abcdef", "abc"));
        assertTrue(StringUtils.startsWithIgnoreCase("ABCDEF", "abc"));
        assertTrue(StringUtils.endsWith("abcdef", "def"));
        assertTrue(StringUtils.endsWithIgnoreCase("ABCDEF", "def"));
        assertTrue(StringUtils.startsWithAny("abcxyz", new String[] {"abc", "xyz"}));
    }

    // Tests abbreviate and overlay methods
    @Test
    public void testAbbreviateAndOverlay_variousInputs() {
        assertNull(StringUtils.abbreviate(null, 10));
        assertEquals("", StringUtils.abbreviate("", 4));
        assertEquals("abc...", StringUtils.abbreviate("abcdefg", 6));
        assertEquals("a...", StringUtils.abbreviate("abcdefg", 4));
        assertEquals("...fghi...", StringUtils.abbreviate("abcdefghijklmno", 5, 10));
        assertEquals("abzzzzef", StringUtils.overlay("abcdef", "zzzz", 2, 4));
        assertEquals("abef", StringUtils.overlay("abcdef", null, 2, 4));
    }

    // Tests difference, common prefix, and Levenshtein distance
    @Test
    public void testDifferenceAndLevenshteinDistance_variousInputs() {
        assertEquals("robot", StringUtils.difference("i am a machine", "i am a robot"));
        assertEquals("", StringUtils.difference("abc", "abc"));
        assertEquals("ab", StringUtils.getCommonPrefix(new String[] {"abcde", "abxyz"}));
        assertEquals(0, StringUtils.getLevenshteinDistance("", ""));
        assertEquals(1, StringUtils.getLevenshteinDistance("frog", "fog"));
        assertEquals(3, StringUtils.getLevenshteinDistance("fly", "ant"));
    }

    // Tests containsAny, containsNone, and containsOnly
    @Test
    public void testContainsAnyContainsNoneContainsOnly_variousInputs() {
        assertTrue(StringUtils.containsAny("zzabyycdxx", new char[] {'z', 'a'}));
        assertFalse(StringUtils.containsAny("zzabyycdxx", new char[] {'1'}));
        assertTrue(StringUtils.containsAny("zzabyycdxx", "za"));
        assertFalse(StringUtils.containsAny(null, "za"));
        assertTrue(StringUtils.containsNone("ab", "xyz"));
        assertFalse(StringUtils.containsNone("abz", "xyz"));
        assertTrue(StringUtils.containsOnly("abab", "abc"));
        assertFalse(StringUtils.containsOnly("ab1", "abc"));
    }

    // Tests removeStart, removeEnd, chomp, and chop
    @Test
    public void testRemoveEndStartAndChompChop_variousInputs() {
        assertEquals("domain.com", StringUtils.removeStart("www.domain.com", "www."));
        assertEquals("www.domain", StringUtils.removeEnd("www.domain.com", ".com"));
        assertEquals("abc", StringUtils.chomp("abc\n"));
        assertEquals("ab", StringUtils.chop("abc"));
    }

    // Tests exception path for invalid abbreviation width
    @Test(expected = IllegalArgumentException.class)
    public void testAbbreviate_maxWidthTooSmall_throwsException() {
        StringUtils.abbreviate("abcdefg", 3);
    }

    // Tests exception path for null Levenshtein distance input
    @Test(expected = IllegalArgumentException.class)
    public void testGetLevenshteinDistance_nullInput_throwsException() {
        StringUtils.getLevenshteinDistance(null, "abc");
    }

    // ======================== Additional tests for uncovered methods and edge cases ========================

    @Test
    public void testIsNotEmpty() {
        assertFalse(StringUtils.isNotEmpty(null));
        assertFalse(StringUtils.isNotEmpty(""));
        assertTrue(StringUtils.isNotEmpty(" "));
        assertTrue(StringUtils.isNotEmpty("bob"));
    }

    @Test
    public void testIsNotBlank() {
        assertFalse(StringUtils.isNotBlank(null));
        assertFalse(StringUtils.isNotBlank(""));
        assertFalse(StringUtils.isNotBlank(" \t\n"));
        assertTrue(StringUtils.isNotBlank("bob"));
        assertTrue(StringUtils.isNotBlank("  bob  "));
    }

    @Test
    public void testDefaultIfEmpty() {
        assertEquals("default", StringUtils.defaultIfEmpty(null, "default"));
        assertEquals("default", StringUtils.defaultIfEmpty("", "default"));
        assertEquals("  ", StringUtils.defaultIfEmpty("  ", "default"));
        assertEquals("bob", StringUtils.defaultIfEmpty("bob", "default"));
    }

    @Test
    public void testDefaultIfBlank() {
        assertEquals("default", StringUtils.defaultIfBlank(null, "default"));
        assertEquals("default", StringUtils.defaultIfBlank("", "default"));
        assertEquals("default", StringUtils.defaultIfBlank(" \t\n", "default"));
        assertEquals("bob", StringUtils.defaultIfBlank("bob", "default"));
        assertEquals("  bob  ", StringUtils.defaultIfBlank("  bob  ", "default"));
    }

    @Test
    public void testDeleteWhitespace() {
        assertNull(StringUtils.deleteWhitespace(null));
        assertEquals("", StringUtils.deleteWhitespace(""));
        assertEquals("abc", StringUtils.deleteWhitespace("a b c"));
        assertEquals("abc", StringUtils.deleteWhitespace("  abc  "));
        assertEquals("", StringUtils.deleteWhitespace("   "));
    }

    @Test
    public void testReverse() {
        assertNull(StringUtils.reverse(null));
        assertEquals("", StringUtils.reverse(""));
        assertEquals("cba", StringUtils.reverse("abc"));
        assertEquals("ab", StringUtils.reverse("ba"));
    }

    @Test
    public void testRotate() {
        assertNull(StringUtils.rotate(null, 1));
        assertEquals("", StringUtils.rotate("", 2));
        assertEquals("cab", StringUtils.rotate("abc", 1));
        assertEquals("bca", StringUtils.rotate("abc", -1));
        assertEquals("abc", StringUtils.rotate("abc", 3));
    }

    @Test
    public void testWrapAndUnwrap() {
        assertNull(StringUtils.wrap(null, ""));
        assertEquals("{abc}", StringUtils.wrap("abc", "{"));
        assertEquals("\"abc\"", StringUtils.wrap("\"abc\"", "\""));
        assertNull(StringUtils.unwrap(null, "\""));
        assertEquals("abc", StringUtils.unwrap("\"abc\"", "\""));
        assertEquals("", StringUtils.unwrap("", "\""));
        assertEquals("abc", StringUtils.unwrap("{abc}", "{"));
        assertEquals("abc", StringUtils.unwrap("abc", "\""));
    }

    @Test
    public void testIndexOfDifference() {
        assertEquals(-1, StringUtils.indexOfDifference(null, null));
        assertEquals(0, StringUtils.indexOfDifference(null, "abc"));
        assertEquals(0, StringUtils.indexOfDifference("abc", null));
        assertEquals(-1, StringUtils.indexOfDifference("", ""));
        assertEquals(0, StringUtils.indexOfDifference("abc", ""));
        assertEquals(2, StringUtils.indexOfDifference("abc", "abx"));
        assertEquals(-1, StringUtils.indexOfDifference("abc", "abc"));
    }

    @Test
    public void testIndexOfStringWithNull() {
        assertEquals(-1, StringUtils.indexOf(null, "a"));
        assertEquals(0, StringUtils.indexOf("", ""));
        assertEquals(1, StringUtils.indexOf("abc", "bc"));
        assertEquals(-1, StringUtils.indexOf("abc", "bd"));
        assertEquals(1, StringUtils.indexOf("abc", "b", 0));
        assertEquals(-1, StringUtils.indexOf("abc", "b", 2));
    }

    @Test
    public void testLastIndexOfCharAndString() {
        assertEquals(-1, StringUtils.lastIndexOf(null, 'a'));
        assertEquals(5, StringUtils.lastIndexOf("aabaabaa", 'a'));
        assertEquals(5, StringUtils.lastIndexOf("aabaabaa", 'a', 6));
        assertEquals(-1, StringUtils.lastIndexOf("aabaabaa", 'b', 0));
        assertEquals(-1, StringUtils.lastIndexOf(null, "ab"));
        assertEquals(4, StringUtils.lastIndexOf("ababc", "ab"));
        assertEquals(1, StringUtils.lastIndexOf("ababc", "ab", 2));
    }

    @Test
    public void testContainsString() {
        assertFalse(StringUtils.contains(null, "a"));
        assertTrue(StringUtils.contains("abc", "b"));
        assertFalse(StringUtils.contains("abc", "d"));
        assertTrue(StringUtils.contains("abc", "abc"));
        assertTrue(StringUtils.contains("", ""));
    }

    @Test
    public void testReplaceOnce() {
        assertNull(StringUtils.replaceOnce(null, "a", "z"));
        assertEquals("zbc", StringUtils.replaceOnce("abc", "a", "z"));
        assertEquals("abz", StringUtils.replaceOnce("abc", "c", "z"));
        assertEquals("abc", StringUtils.replaceOnce("abc", "d", "z"));
        assertEquals("", StringUtils.replaceOnce("", "a", "z"));
    }

    @Test
    public void testLeftPadRightPadCenter() {
        assertNull(StringUtils.leftPad(null, 5));
        assertEquals("  abc", StringUtils.leftPad("abc", 5));
        assertEquals("abc", StringUtils.leftPad("abc", 2));
        assertEquals("xxabc", StringUtils.leftPad("abc", 5, 'x'));
        assertNull(StringUtils.rightPad(null, 5));
        assertEquals("abc  ", StringUtils.rightPad("abc", 5));
        assertEquals("abc", StringUtils.rightPad("abc", 2));
        assertEquals("abcxx", StringUtils.rightPad("abc", 5, 'x'));
        assertNull(StringUtils.center(null, 5));
        assertEquals(" abc ", StringUtils.center("abc", 5));
        assertEquals("abc", StringUtils.center("abc", 2));
        assertEquals("xxabcxx", StringUtils.center("abc", 7, 'x'));
    }

    @Test
    public void testCountMatches() {
        assertEquals(0, StringUtils.countMatches(null, 'a'));
        assertEquals(0, StringUtils.countMatches("", 'a'));
        assertEquals(2, StringUtils.countMatches("aba", 'a'));
        assertEquals(0, StringUtils.countMatches("abc", 'd'));
        assertEquals(0, StringUtils.countMatches(null, "ab"));
        assertEquals(1, StringUtils.countMatches("abab", "ab"));
        assertEquals(0, StringUtils.countMatches("abc", "abx"));
    }

    @Test
    public void testStripToNull() {
        assertNull(StringUtils.stripToNull(null));
        assertNull(StringUtils.stripToNull("   "));
        assertEquals("abc", StringUtils.stripToNull(" abc "));
        assertEquals("abc", StringUtils.stripToNull("abc"));
    }

    @Test
    public void testUpperLowerCapitalizeUncapitalizeSwapWithNull() {
        assertNull(StringUtils.upperCase(null));
        assertNull(StringUtils.lowerCase(null));
        assertNull(StringUtils.capitalize(null));
        assertNull(StringUtils.uncapitalize(null));
        assertNull(StringUtils.swapCase(null));
    }

    @Test
    public void testIsAllUpperCaseLowerCaseWithNull() {
        assertFalse(StringUtils.isAllUpperCase(null));
        assertFalse(StringUtils.isAllLowerCase(null));
    }

    @Test
    public void testIsNumericWithNull() {
        assertFalse(StringUtils.isNumeric(null));
        assertFalse(StringUtils.isNumericSpace(null));
    }

    @Test
    public void testContainsAnyNullArray() {
        assertFalse(StringUtils.containsAny(null, (char[]) null));
        assertFalse(StringUtils.containsAny("abc", (char[]) null));
        assertFalse(StringUtils.containsAny("abc", new char[0]));
        assertFalse(StringUtils.containsAny(null, "ab"));
    }

    @Test
    public void testContainsNoneNull() {
        assertTrue(StringUtils.containsNone(null, "a"));
        assertTrue(StringUtils.containsNone("abc", null));
        assertTrue(StringUtils.containsNone("abc", ""));
    }

    @Test
    public void testContainsOnlyNull() {
        assertFalse(StringUtils.containsOnly(null, "abc"));
        assertFalse(StringUtils.containsOnly("abc", null));
        assertFalse(StringUtils.containsOnly("abc", ""));
    }

    @Test
    public void testChopAndChompWithNull() {
        assertNull(StringUtils.chop(null));
        assertNull(StringUtils.chomp(null));
        assertEquals("", StringUtils.chop(""));
        assertEquals("", StringUtils.chomp(""));
    }

    @Test
    public void testRemoveStartEndWithNull() {
        assertNull(StringUtils.removeStart(null, "a"));
        assertNull(StringUtils.removeEnd(null, "a"));
        assertEquals("abc", StringUtils.removeStart("abc", null));
        assertEquals("abc", StringUtils.removeEnd("abc", null));
    }

    @Test
    public void testDifferenceWithNull() {
        assertNull(StringUtils.difference(null, null));
        assertNull(StringUtils.difference(null, "abc"));
        assertNull(StringUtils.difference("abc", null));
    }

    @Test
    public void testGetCommonPrefixWithNull() {
        assertEquals("", StringUtils.getCommonPrefix((String[]) null));
        assertEquals("", StringUtils.getCommonPrefix(new String[]{null, "abc"}));
        assertEquals("", StringUtils.getCommonPrefix(new String[]{"abc", null}));
        assertEquals("ab", StringUtils.getCommonPrefix(new String[]{"abc", "abx"}));
    }

    @Test
    public void testGetLevenshteinDistanceThreshold() {
        assertEquals(0, StringUtils.getLevenshteinDistance("", "", 0));
        assertEquals(1, StringUtils.getLevenshteinDistance("frog", "fog", 1));
        assertEquals(-1, StringUtils.getLevenshteinDistance("frog", "fog", 0)); // threshold too low
        assertEquals(3, StringUtils.getLevenshteinDistance("fly", "ant", 3));
        assertEquals(-1, StringUtils.getLevenshteinDistance("fly", "ant", 2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetLevenshteinDistanceThresholdNullInput() {
        StringUtils.getLevenshteinDistance(null, "abc", 1);
    }

    @Test
    public void testJoinWithNullElement() {
        assertEquals("a,,c", StringUtils.join(new Object[] {"a", null, "c"}, ','));
        assertEquals("a--c", StringUtils.join(new Object[] {"a", null, "c"}, "--"));
    }

    @Test
    public void testJoinWithIterator() {
        List<String> list = Arrays.asList("a", "b", "c");
        assertEquals("a,b,c", StringUtils.join(list.iterator(), ','));
        assertEquals("a,b,c", StringUtils.join(list, ','));
        assertEquals("", StringUtils.join(Collections.emptyIterator(), ','));
    }

    @Test
    public void testAbbreviateWithOffsetNegative() {
        assertNull(StringUtils.abbreviate(null, 4, 10));
        assertEquals("abc", StringUtils.abbreviate("abc", -1, 4));
        assertEquals("...", StringUtils.abbreviate("abcdefg", -2, 4));
    }

    @Test
    public void testSplitWithNullSeparator() {
        assertNull(StringUtils.split(null, null));
        assertArrayEquals(new String[]{"abc"}, StringUtils.split("abc", null));
    }

    @Test
    public void testReplaceCharsWithEmptyTarget() {
        assertEquals("abc", StringUtils.replaceChars("abc", "", "x"));
    }
}