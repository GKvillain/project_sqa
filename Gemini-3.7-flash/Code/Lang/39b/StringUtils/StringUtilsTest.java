package org.apache.commons.lang3;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Locale;

public class StringUtilsTest {

    // Tests Defects4J Lang-39b: replaceEach with null replacement element in array
    @Test
    public void testReplaceEach_nullReplacementElement_ignoresNullReplacement() {
        assertEquals("aba", StringUtils.replaceEach("aba", new String[]{"a"}, new String[]{null}));
        assertEquals("b", StringUtils.replaceEach("aba", new String[]{"a", "b"}, new String[]{"c", null}));
        assertEquals("d", StringUtils.replaceEach("abc", new String[]{"a", "b", "c"}, new String[]{null, "d", null}));
    }

    // Tests normal case of replaceEach with multiple search and replace strings
    @Test
    public void testReplaceEach_normalCase_returnsReplacedString() {
        assertEquals("wcte", StringUtils.replaceEach("abcde", new String[]{"ab", "d"}, new String[]{"w", "t"}));
        assertNull(StringUtils.replaceEach(null, new String[]{"a"}, new String[]{"b"}));
        assertEquals("", StringUtils.replaceEach("", new String[]{"a"}, new String[]{"b"}));
        assertEquals("aba", StringUtils.replaceEach("aba", null, new String[]{"b"}));
        assertEquals("aba", StringUtils.replaceEach("aba", new String[0], new String[]{"b"}));
    }

    // Tests replaceEach with mismatched array lengths throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testReplaceEach_mismatchedArrayLengths_throwsIllegalArgumentException() {
        StringUtils.replaceEach("abc", new String[]{"a", "b"}, new String[]{"x"});
    }

    // Tests replaceEachRepeatedly with circular reference throwing IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testReplaceEachRepeatedly_circularReference_throwsIllegalStateException() {
        StringUtils.replaceEachRepeatedly("abcde", new String[]{"ab", "d"}, new String[]{"d", "ab"});
    }

    // Tests empty checks (isEmpty, isNotEmpty, isBlank, isNotBlank)
    @Test
    public void testEmptyAndBlankChecks_variousInputs_returnsExpected() {
        assertTrue(StringUtils.isEmpty(null));
        assertTrue(StringUtils.isEmpty(""));
        assertFalse(StringUtils.isEmpty(" "));
        assertFalse(StringUtils.isEmpty("bob"));

        assertFalse(StringUtils.isNotEmpty(null));
        assertTrue(StringUtils.isNotEmpty("bob"));

        assertTrue(StringUtils.isBlank(null));
        assertTrue(StringUtils.isBlank(""));
        assertTrue(StringUtils.isBlank("   "));
        assertFalse(StringUtils.isBlank(" bob "));

        assertFalse(StringUtils.isNotBlank("   "));
        assertTrue(StringUtils.isNotBlank("bob"));
    }

    // Tests strip and trim variations including null handling
    @Test
    public void testTrimAndStrip_variousInputs_returnsExpected() {
        assertNull(StringUtils.trim(null));
        assertEquals("", StringUtils.trim("   "));
        assertEquals("abc", StringUtils.trim("  abc  "));

        assertNull(StringUtils.trimToNull("   "));
        assertEquals("", StringUtils.trimToEmpty(null));

        assertNull(StringUtils.strip(null));
        assertEquals("abc", StringUtils.strip("  abc  "));
        assertEquals("abc", StringUtils.strip("  abcyx", "xyz "));
        assertEquals("abc", StringUtils.stripStart("yxabc", "xyz"));
        assertEquals("abc", StringUtils.stripEnd("abcyx", "xyz"));
    }

    // Tests indexOf, lastIndexOf, ordinalIndexOf, and contains methods
    @Test
    public void testIndexOfAndContains_variousInputs_returnsExpected() {
        assertEquals(-1, StringUtils.indexOf(null, 'a'));
        assertEquals(0, StringUtils.indexOf("aabaabaa", 'a'));
        assertEquals(2, StringUtils.indexOf("aabaabaa", 'b', 0));
        assertEquals(5, StringUtils.ordinalIndexOf("aabaabaa", "b", 2));
        assertEquals(-1, StringUtils.ordinalIndexOf("aabaabaa", "b", 0));

        assertEquals(5, StringUtils.lastIndexOf("aabaabaa", 'b'));
        assertEquals(2, StringUtils.lastIndexOf("aabaabaa", 'b', 4));

        assertTrue(StringUtils.contains("abc", 'a'));
        assertFalse(StringUtils.contains("abc", 'z'));
        assertTrue(StringUtils.containsIgnoreCase("abc", "A"));
        assertFalse(StringUtils.containsIgnoreCase("abc", "Z"));
    }

    // Tests substring operations (left, right, mid, substringBetween)
    @Test
    public void testSubstringOperations_variousInputs_returnsExpected() {
        assertEquals("ab", StringUtils.substring("abc", 0, 2));
        assertEquals("bc", StringUtils.substring("abc", -2));
        assertEquals("ab", StringUtils.left("abc", 2));
        assertEquals("bc", StringUtils.right("abc", 2));
        assertEquals("b", StringUtils.mid("abc", 1, 1));
        assertEquals("b", StringUtils.substringBetween("wx[b]yz", "[", "]"));
        assertArrayEquals(new String[]{"a", "b", "c"}, StringUtils.substringsBetween("[a][b][c]", "[", "]"));
    }

    // Tests split and join methods
    @Test
    public void testSplitAndJoin_variousInputs_returnsExpected() {
        assertArrayEquals(new String[]{"a", "b", "c"}, StringUtils.split("a.b.c", '.'));
        assertArrayEquals(new String[]{"a", "", "b", "c"}, StringUtils.splitPreserveAllTokens("a..b.c", '.'));
        assertArrayEquals(new String[]{"ab", "cd", "ef"}, StringUtils.splitByWholeSeparator("ab-!-cd-!-ef", "-!-"));
        assertArrayEquals(new String[]{"foo", "Bar"}, StringUtils.splitByCharacterTypeCamelCase("fooBar"));

        assertEquals("a;b;c", StringUtils.join(new String[]{"a", "b", "c"}, ';'));
        assertEquals("a--b--c", StringUtils.join(new String[]{"a", "b", "c"}, "--"));
        assertEquals("a, b, c", StringUtils.join(Arrays.asList("a", "b", "c"), ", "));
    }

    // Tests padding and centering functions
    @Test
    public void testPadAndCenter_variousInputs_returnsExpected() {
        assertEquals("bat  ", StringUtils.rightPad("bat", 5));
        assertEquals("batzz", StringUtils.rightPad("bat", 5, 'z'));
        assertEquals("batyz", StringUtils.rightPad("bat", 5, "yz"));

        assertEquals("  bat", StringUtils.leftPad("bat", 5));
        assertEquals("zzbat", StringUtils.leftPad("bat", 5, 'z'));
        assertEquals("yzbat", StringUtils.leftPad("bat", 5, "yz"));

        assertEquals(" ab ", StringUtils.center("ab", 4));
        assertEquals("yayz", StringUtils.center("a", 4, "yz"));
    }

    // Tests character classification methods
    @Test
    public void testCharacterTests_variousInputs_returnsExpected() {
        assertTrue(StringUtils.isAlpha("abc"));
        assertFalse(StringUtils.isAlpha("ab2c"));
        assertTrue(StringUtils.isAlphanumeric("ab2c"));
        assertFalse(StringUtils.isAlphanumeric("ab-c"));
        assertTrue(StringUtils.isNumeric("123"));
        assertFalse(StringUtils.isNumeric("12.3"));
        assertTrue(StringUtils.isWhitespace(" \t \n "));
        assertTrue(StringUtils.isAllLowerCase("abc"));
        assertFalse(StringUtils.isAllLowerCase("abC"));
        assertTrue(StringUtils.isAllUpperCase("ABC"));
        assertFalse(StringUtils.isAllUpperCase("Abc"));
    }

    // Tests case conversions and manipulations
    @Test
    public void testCaseConversions_variousInputs_returnsExpected() {
        assertEquals("ABC", StringUtils.upperCase("aBc"));
        assertEquals("abc", StringUtils.lowerCase("aBc", Locale.ENGLISH));
        assertEquals("Cat", StringUtils.capitalize("cat"));
        assertEquals("cat", StringUtils.uncapitalize("Cat"));
        assertEquals("tHE DOG HAS A bone", StringUtils.swapCase("The dog has a BONE"));
    }

    // Tests abbreviation and Levenshtein distance calculations
    @Test
    public void testAbbreviateAndDistance_variousInputs_returnsExpected() {
        assertEquals("abc...", StringUtils.abbreviate("abcdefg", 6));
        assertEquals("...fghi...", StringUtils.abbreviate("abcdefghijklmno", 5, 10));
        assertEquals(1, StringUtils.getLevenshteinDistance("frog", "fog"));
        assertEquals(3, StringUtils.getLevenshteinDistance("fly", "ant"));
        assertEquals(0, StringUtils.getLevenshteinDistance("", ""));
    }

    // Tests abbreviate with width less than minimum throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testAbbreviate_widthTooSmall_throwsIllegalArgumentException() {
        StringUtils.abbreviate("abcdefg", 3);
    }

    // Tests prefix and suffix matching
    @Test
    public void testStartsAndEndsWith_variousInputs_returnsExpected() {
        assertTrue(StringUtils.startsWith("abcdef", "abc"));
        assertTrue(StringUtils.startsWithIgnoreCase("ABCDEF", "abc"));
        assertTrue(StringUtils.startsWithAny("abcxyz", new String[]{"xyz", "abc"}));

        assertTrue(StringUtils.endsWith("abcdef", "def"));
        assertTrue(StringUtils.endsWithIgnoreCase("ABCDEF", "def"));
    }

    // Tests difference, common prefix, deleteWhitespace, and chomp/chop
    @Test
    public void testDifferenceAndChomp_variousInputs_returnsExpected() {
        assertEquals("robot", StringUtils.difference("i am a machine", "i am a robot"));
        assertEquals("i am a ", StringUtils.getCommonPrefix(new String[]{"i am a machine", "i am a robot"}));
        assertEquals("abc", StringUtils.deleteWhitespace("   ab  c  "));
        assertEquals("abc", StringUtils.chomp("abc\r\n"));
        assertEquals("ab", StringUtils.chop("abc"));
        assertEquals("abc", StringUtils.chop("abc\r\n"));
    }

    // Tests default string operations and null fallbacks
    @Test
    public void testDefaultStrings_variousInputs_returnsExpected() {
        assertEquals("", StringUtils.defaultString(null));
        assertEquals("abc", StringUtils.defaultString("abc"));
        assertEquals("NULL", StringUtils.defaultString(null, "NULL"));
        assertEquals("abc", StringUtils.defaultString("abc", "NULL"));

        assertEquals("def", StringUtils.defaultIfEmpty(null, "def"));
        assertEquals("def", StringUtils.defaultIfEmpty("", "def"));
        assertEquals("abc", StringUtils.defaultIfEmpty("abc", "def"));

        assertEquals("def", StringUtils.defaultIfBlank(null, "def"));
        assertEquals("def", StringUtils.defaultIfBlank("   ", "def"));
        assertEquals("abc", StringUtils.defaultIfBlank("abc", "def"));
    }

    // Tests containsAny, containsOnly, and containsNone
    @Test
    public void testContainsSets_variousInputs_returnsExpected() {
        assertTrue(StringUtils.containsAny("zzabyycdxx", 'z', 'a'));
        assertTrue(StringUtils.containsAny("zzabyycdxx", "za"));
        assertTrue(StringUtils.containsAny("aba", new String[]{"z", "a"}));
        assertFalse(StringUtils.containsAny("aba", new String[]{"z", "y"}));

        assertTrue(StringUtils.containsOnly("abab", 'a', 'b'));
        assertFalse(StringUtils.containsOnly("abac", 'a', 'b'));
        assertTrue(StringUtils.containsOnly("abab", "ab"));

        assertTrue(StringUtils.containsNone("ab", 'c', 'd'));
        assertFalse(StringUtils.containsNone("abab", 'a', 'c'));
        assertTrue(StringUtils.containsNone("ab", "cd"));
    }

    // Tests indexOfAny and indexOfAnyBut
    @Test
    public void testIndexOfAnyMethods_variousInputs_returnsExpected() {
        assertEquals(0, StringUtils.indexOfAny("zzabyycdxx", 'z', 'a'));
        assertEquals(2, StringUtils.indexOfAny("zzabyycdxx", "by"));
        assertEquals(1, StringUtils.indexOfAny("zzabyycdxx", new String[]{"za", "zab"}));
        assertEquals(-1, StringUtils.indexOfAny("zzabyycdxx", new String[]{"hello"}));

        assertEquals(3, StringUtils.indexOfAnyBut("zzabyycdxx", 'z', 'a', 'b'));
        assertEquals(2, StringUtils.indexOfAnyBut("zzabyycdxx", "z"));
        assertEquals(-1, StringUtils.indexOfAnyBut("aaaa", "a"));
    }

    // Tests indexOfIgnoreCase and lastIndexOfIgnoreCase
    @Test
    public void testIndexOfIgnoreCase_variousInputs_returnsExpected() {
        assertEquals(1, StringUtils.indexOfIgnoreCase("aBa", "B"));
        assertEquals(1, StringUtils.indexOfIgnoreCase("aBa", "B", 0));
        assertEquals(-1, StringUtils.indexOfIgnoreCase("aBa", "C"));

        assertEquals(3, StringUtils.lastIndexOfIgnoreCase("aBaBa", "B"));
        assertEquals(1, StringUtils.lastIndexOfIgnoreCase("aBaBa", "B", 2));
    }

    // Tests substringBefore, substringAfter, substringBeforeLast, substringAfterLast
    @Test
    public void testSubstringBeforeAfter_variousInputs_returnsExpected() {
        assertEquals("foo", StringUtils.substringBefore("fooXXbarXXbaz", "XX"));
        assertEquals("barXXbaz", StringUtils.substringAfter("fooXXbarXXbaz", "XX"));
        assertEquals("fooXXbar", StringUtils.substringBeforeLast("fooXXbarXXbaz", "XX"));
        assertEquals("baz", StringUtils.substringAfterLast("fooXXbarXXbaz", "XX"));

        assertNull(StringUtils.substringBefore(null, "a"));
        assertEquals("", StringUtils.substringBefore("", "a"));
        assertEquals("abc", StringUtils.substringBefore("abc", ""));
        assertEquals("abc", StringUtils.substringBefore("abc", "z"));
    }

    // Tests single replace, replaceChars, overlay, and repeat
    @Test
    public void testReplaceOverlayRepeat_variousInputs_returnsExpected() {
        assertEquals("aba", StringUtils.replace("aba", "b", "b"));
        assertEquals("aza", StringUtils.replace("aba", "b", "z"));
        assertEquals("azb", StringUtils.replace("abb", "b", "z", 1));

        assertEquals("ayz", StringUtils.replaceChars("abc", "bc", "yz"));
        assertEquals("a", StringUtils.replaceChars("abc", "bc", ""));

        assertEquals("abcdef", StringUtils.overlay("abcdef", "zz", 2, 2));
        assertEquals("abzzef", StringUtils.overlay("abcdef", "zz", 2, 4));

        assertEquals("abcabcabc", StringUtils.repeat("abc", 3));
        assertEquals("a, a, a", StringUtils.repeat("a", ", ", 3));
    }

    // Tests remove, removeStart, removeEnd, countMatches, and reverse operations
    @Test
    public void testRemoveCountAndReverse_variousInputs_returnsExpected() {
        assertEquals("que", StringUtils.remove("queued", "ed"));
        assertEquals("queued", StringUtils.removeStart("queued", "ab"));
        assertEquals("ed", StringUtils.removeStart("queued", "que"));
        assertEquals("que", StringUtils.removeEnd("queued", "ed"));
        assertEquals("queued", StringUtils.removeEnd("queued", "ab"));

        assertEquals(2, StringUtils.countMatches("abba", "b"));
        assertEquals(0, StringUtils.countMatches("abba", "z"));

        assertEquals("ba", StringUtils.reverse("ab"));
        assertEquals("baz.bar.foo", StringUtils.reverseDelimited("foo.bar.baz", '.'));
    }

    // Tests equals, equalsIgnoreCase, compare, and length utilities
    @Test
    public void testEqualsCompareAndLength_variousInputs_returnsExpected() {
        assertTrue(StringUtils.equals(null, null));
        assertFalse(StringUtils.equals(null, "abc"));
        assertTrue(StringUtils.equals("abc", "abc"));

        assertTrue(StringUtils.equalsIgnoreCase(null, null));
        assertTrue(StringUtils.equalsIgnoreCase("abc", "ABC"));
        assertFalse(StringUtils.equalsIgnoreCase("abc", "ABD"));

        assertEquals(0, StringUtils.compare(null, null));
        assertTrue(StringUtils.compare("a", "b") < 0);
        assertTrue(StringUtils.compareIgnoreCase("A", "b") < 0);

        assertEquals(0, StringUtils.length(null));
        assertEquals(3, StringUtils.length("abc"));
        assertTrue(StringUtils.isAsciiPrintable("abc~! "));
        assertFalse(StringUtils.isAsciiPrintable("abc\u00A0"));
    }

    // Tests constructor invocation for coverage
    @Test
    public void testConstructor() {
        assertNotNull(new StringUtils());
    }
}