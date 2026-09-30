package org.apache.commons.lang3;

import org.junit.Test;

import java.util.Arrays;
import java.util.Locale;

import static org.junit.Assert.*;

public class StringUtilsTest {

    // Tests defect in equals when comparing CharSequence objects like StringBuilder/StringBuffer
    @Test
    public void testEquals_charSequenceImplementations_returnsTrueForMatchingContent() {
        assertTrue(StringUtils.equals(null, null));
        assertFalse(StringUtils.equals(null, "abc"));
        assertFalse(StringUtils.equals("abc", null));
        assertTrue(StringUtils.equals("abc", "abc"));
        assertFalse(StringUtils.equals("abc", "ABC"));

        // Tests CharSequence comparisons across different implementations
        CharSequence cs1 = new StringBuilder("abc");
        CharSequence cs2 = new StringBuilder("abc");
        CharSequence cs3 = new StringBuffer("abc");
        assertTrue(StringUtils.equals(cs1, cs2));
        assertTrue(StringUtils.equals(cs1, cs3));
        assertTrue(StringUtils.equals("abc", cs1));
        assertTrue(StringUtils.equals(cs1, "abc"));
    }

    // Tests equalsIgnoreCase with various inputs including null and different cases
    @Test
    public void testEqualsIgnoreCase_variousInputs_matchesCorrectly() {
        assertTrue(StringUtils.equalsIgnoreCase(null, null));
        assertFalse(StringUtils.equalsIgnoreCase(null, "abc"));
        assertFalse(StringUtils.equalsIgnoreCase("abc", null));
        assertTrue(StringUtils.equalsIgnoreCase("abc", "abc"));
        assertTrue(StringUtils.equalsIgnoreCase("abc", "ABC"));
        assertTrue(StringUtils.equalsIgnoreCase("abc", new StringBuilder("ABC")));
        assertFalse(StringUtils.equalsIgnoreCase("abcd", "abc"));
    }

    // Tests isEmpty, isNotEmpty, isBlank, and isNotBlank
    @Test
    public void testEmptyAndBlankChecks_variousInputs() {
        assertTrue(StringUtils.isEmpty(null));
        assertTrue(StringUtils.isEmpty(""));
        assertFalse(StringUtils.isEmpty(" "));
        assertFalse(StringUtils.isEmpty("abc"));

        assertFalse(StringUtils.isNotEmpty(null));
        assertFalse(StringUtils.isNotEmpty(""));
        assertTrue(StringUtils.isNotEmpty(" "));
        assertTrue(StringUtils.isNotEmpty("abc"));

        assertTrue(StringUtils.isBlank(null));
        assertTrue(StringUtils.isBlank(""));
        assertTrue(StringUtils.isBlank("   \t\r\n"));
        assertFalse(StringUtils.isBlank("  a  "));

        assertFalse(StringUtils.isNotBlank(null));
        assertFalse(StringUtils.isNotBlank(""));
        assertFalse(StringUtils.isNotBlank("   "));
        assertTrue(StringUtils.isNotBlank("  a  "));
    }

    // Tests trim, trimToNull, trimToEmpty, strip, stripToNull, stripToEmpty
    @Test
    public void testTrimAndStrip_variousInputs() {
        assertNull(StringUtils.trim(null));
        assertEquals("", StringUtils.trim(""));
        assertEquals("abc", StringUtils.trim("  abc  "));

        assertNull(StringUtils.trimToNull(null));
        assertNull(StringUtils.trimToNull("   "));
        assertEquals("abc", StringUtils.trimToNull("  abc  "));

        assertEquals("", StringUtils.trimToEmpty(null));
        assertEquals("", StringUtils.trimToEmpty("   "));
        assertEquals("abc", StringUtils.trimToEmpty("  abc  "));

        assertNull(StringUtils.strip(null));
        assertEquals("", StringUtils.strip(""));
        assertEquals("abc", StringUtils.strip("  abc  "));
        assertEquals("abc", StringUtils.strip("yxabcyx", "xyz"));

        assertNull(StringUtils.stripToNull(null));
        assertNull(StringUtils.stripToNull("   "));
        assertEquals("abc", StringUtils.stripToNull("  abc  "));

        assertEquals("", StringUtils.stripToEmpty(null));
        assertEquals("abc", StringUtils.stripToEmpty("  abc  "));

        assertEquals("abc", StringUtils.stripStart("  abc", null));
        assertEquals("abc", StringUtils.stripEnd("abc  ", null));
        assertArrayEquals(new String[]{"a", "b"}, StringUtils.stripAll(new String[]{"  a ", " b "}));
    }

    // Tests stripAccents with normal and null input
    @Test
    public void testStripAccents_validAndNullInputs() {
        assertNull(StringUtils.stripAccents(null));
        assertEquals("", StringUtils.stripAccents(""));
        assertEquals("control", StringUtils.stripAccents("control"));
        assertEquals("eclair", StringUtils.stripAccents("\u00e9clair"));
    }

    // Tests indexOf, lastIndexOf, ordinalIndexOf and their case-insensitive variants
    @Test
    public void testIndexOfAndLastIndexOf_variousInputs() {
        assertEquals(-1, StringUtils.indexOf(null, 'a'));
        assertEquals(-1, StringUtils.indexOf("", 'a'));
        assertEquals(0, StringUtils.indexOf("aabaabaa", 'a'));
        assertEquals(2, StringUtils.indexOf("aabaabaa", 'b'));
        assertEquals(5, StringUtils.indexOf("aabaabaa", 'b', 3));
        assertEquals(-1, StringUtils.indexOf("aabaabaa", 'b', 9));

        assertEquals(-1, StringUtils.indexOf(null, "a"));
        assertEquals(-1, StringUtils.indexOf("aabaabaa", null));
        assertEquals(0, StringUtils.indexOf("aabaabaa", ""));
        assertEquals(1, StringUtils.indexOf("aabaabaa", "ab"));
        assertEquals(4, StringUtils.indexOf("aabaabaa", "ab", 2));

        assertEquals(2, StringUtils.indexOfIgnoreCase("aabaabaa", "B"));
        assertEquals(1, StringUtils.indexOfIgnoreCase("aabaabaa", "AB", -1));
        assertEquals(-1, StringUtils.indexOfIgnoreCase("aabaabaa", "B", 9));

        assertEquals(7, StringUtils.lastIndexOf("aabaabaa", 'a'));
        assertEquals(5, StringUtils.lastIndexOf("aabaabaa", 'b'));
        assertEquals(2, StringUtils.lastIndexOf("aabaabaa", 'b', 4));

        assertEquals(7, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "A"));
        assertEquals(5, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "B", 8));

        assertEquals(0, StringUtils.ordinalIndexOf("aabaabaa", "a", 1));
        assertEquals(1, StringUtils.ordinalIndexOf("aabaabaa", "a", 2));
        assertEquals(7, StringUtils.lastOrdinalIndexOf("aabaabaa", "a", 1));
        assertEquals(6, StringUtils.lastOrdinalIndexOf("aabaabaa", "a", 2));
    }

    // Tests contains, containsIgnoreCase, containsWhitespace
    @Test
    public void testContains_variousInputs() {
        assertFalse(StringUtils.contains(null, 'a'));
        assertFalse(StringUtils.contains("", 'a'));
        assertTrue(StringUtils.contains("abc", 'a'));
        assertFalse(StringUtils.contains("abc", 'z'));

        assertFalse(StringUtils.contains(null, "a"));
        assertFalse(StringUtils.contains("abc", null));
        assertTrue(StringUtils.contains("abc", ""));
        assertTrue(StringUtils.contains("abc", "bc"));
        assertFalse(StringUtils.contains("abc", "d"));

        assertTrue(StringUtils.containsIgnoreCase("abc", "A"));
        assertFalse(StringUtils.containsIgnoreCase("abc", "Z"));
        assertFalse(StringUtils.containsIgnoreCase(null, "A"));

        assertTrue(StringUtils.containsWhitespace("a b"));
        assertFalse(StringUtils.containsWhitespace("abc"));
        assertFalse(StringUtils.containsWhitespace(null));
        assertFalse(StringUtils.containsWhitespace(""));
    }

    // Tests containsAny, containsOnly, containsNone, indexOfAny, indexOfAnyBut
    @Test
    public void testCharSetSearchOperations_variousInputs() {
        assertTrue(StringUtils.containsAny("zzabyycdxx", 'z', 'a'));
        assertTrue(StringUtils.containsAny("zzabyycdxx", "za"));
        assertFalse(StringUtils.containsAny("aba", 'z'));
        assertFalse(StringUtils.containsAny(null, 'a'));

        assertTrue(StringUtils.containsOnly("abab", 'a', 'b', 'c'));
        assertFalse(StringUtils.containsOnly("ab1", 'a', 'b', 'c'));
        assertTrue(StringUtils.containsOnly("", 'a'));
        assertFalse(StringUtils.containsOnly("ab", (char[]) null));

        assertTrue(StringUtils.containsNone("ab1", 'x', 'y', 'z'));
        assertFalse(StringUtils.containsNone("abz", 'x', 'y', 'z'));
        assertTrue(StringUtils.containsNone(null, 'a'));

        assertEquals(0, StringUtils.indexOfAny("zzabyycdxx", 'z', 'a'));
        assertEquals(3, StringUtils.indexOfAny("zzabyycdxx", "by"));
        assertEquals(-1, StringUtils.indexOfAny("aba", 'z'));

        assertEquals(3, StringUtils.indexOfAnyBut("zzabyycdxx", 'z', 'a'));
        assertEquals(3, StringUtils.indexOfAnyBut("zzabyycdxx", "za"));
        assertEquals(-1, StringUtils.indexOfAnyBut("aba", "ab"));

        assertEquals(2, StringUtils.indexOfAny("zzabyycdxx", "cd", "ab"));
        assertEquals(6, StringUtils.lastIndexOfAny("zzabyycdxx", "ab", "cd"));
    }

    // Tests substring, substringBefore, substringAfter, substringBetween
    @Test
    public void testSubstringOperations_variousInputs() {
        assertNull(StringUtils.substring(null, 0));
        assertEquals("", StringUtils.substring("", 0));
        assertEquals("c", StringUtils.substring("abc", 2));
        assertEquals("bc", StringUtils.substring("abc", -2));
        assertEquals("ab", StringUtils.substring("abc", 0, 2));
        assertEquals("", StringUtils.substring("abc", 2, 0));
        assertEquals("b", StringUtils.substring("abc", -2, -1));

        assertEquals("ab", StringUtils.left("abc", 2));
        assertNull(StringUtils.left(null, 2));
        assertEquals("bc", StringUtils.right("abc", 2));
        assertEquals("b", StringUtils.mid("abc", 1, 1));

        assertEquals("a", StringUtils.substringBefore("abcba", "b"));
        assertEquals("abc", StringUtils.substringBeforeLast("abcba", "b"));
        assertEquals("cba", StringUtils.substringAfter("abcba", "b"));
        assertEquals("a", StringUtils.substringAfterLast("abcba", "b"));

        assertEquals("bc", StringUtils.substringBetween("tagbctag", "tag"));
        assertEquals("b", StringUtils.substringBetween("wx[b]yz", "[", "]"));
        assertArrayEquals(new String[]{"a", "b", "c"}, StringUtils.substringsBetween("[a][b][c]", "[", "]"));
    }

    // Tests split variants and join
    @Test
    public void testSplitAndJoin_variousInputs() {
        assertNull(StringUtils.split(null));
        assertArrayEquals(new String[0], StringUtils.split(""));
        assertArrayEquals(new String[]{"abc", "def"}, StringUtils.split("abc  def"));
        assertArrayEquals(new String[]{"a", "b", "c"}, StringUtils.split("a:b:c", ':'));
        assertArrayEquals(new String[]{"a", "b:c"}, StringUtils.split("a:b:c", ":", 2));

        assertArrayEquals(new String[]{"ab", "cd", "ef"}, StringUtils.splitByWholeSeparator("ab-!-cd-!-ef", "-!-"));
        assertArrayEquals(new String[]{"ab", "", "def"}, StringUtils.splitPreserveAllTokens("ab  def"));
        assertArrayEquals(new String[]{"foo", "Bar"}, StringUtils.splitByCharacterTypeCamelCase("fooBar"));

        assertNull(StringUtils.join((Object[]) null, ","));
        assertEquals("", StringUtils.join(new Object[0], ","));
        assertEquals("a,b,c", StringUtils.join(new String[]{"a", "b", "c"}, ","));
        assertEquals("a;b;c", StringUtils.join(new String[]{"a", "b", "c"}, ';'));
        assertEquals("a--b--c", StringUtils.join(new String[]{"a", "b", "c"}, "--"));
        assertEquals("a,b", StringUtils.join(Arrays.asList("a", "b"), ","));
    }

    // Tests replace, replaceOnce, replaceChars, replaceEach, replaceEachRepeatedly
    @Test
    public void testReplaceOperations_variousInputs() {
        assertNull(StringUtils.replace(null, "a", "b"));
        assertEquals("aba", StringUtils.replace("aba", null, "b"));
        assertEquals("b", StringUtils.replace("aba", "a", ""));
        assertEquals("zbz", StringUtils.replace("aba", "a", "z"));
        assertEquals("zba", StringUtils.replaceOnce("aba", "a", "z"));

        assertEquals("aycya", StringUtils.replaceChars("abcba", 'b', 'y'));
        assertEquals("ayzya", StringUtils.replaceChars("abcba", "bc", "yz"));
        assertEquals("ac", StringUtils.replaceChars("abc", "b", ""));

        assertEquals("wcte", StringUtils.replaceEach("abcde", new String[]{"ab", "d"}, new String[]{"w", "t"}));
        assertEquals("tcte", StringUtils.replaceEachRepeatedly("abcde", new String[]{"ab", "d"}, new String[]{"d", "t"}));

        assertEquals("abzzzzef", StringUtils.overlay("abcdef", "zzzz", 2, 4));
    }

    // Tests circular replaceEachRepeatedly throws IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testReplaceEachRepeatedly_circularReference_throwsIllegalStateException() {
        StringUtils.replaceEachRepeatedly("abcde", new String[]{"ab", "d"}, new String[]{"d", "ab"});
    }

    // Tests replaceEach with mismatched array lengths throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testReplaceEach_mismatchedArrays_throwsIllegalArgumentException() {
        StringUtils.replaceEach("abc", new String[]{"a", "b"}, new String[]{"c"});
    }

    // Tests repeat, rightPad, leftPad, center
    @Test
    public void testPaddingAndRepeat_variousInputs() {
        assertNull(StringUtils.repeat(null, 2));
        assertEquals("", StringUtils.repeat("a", 0));
        assertEquals("aaa", StringUtils.repeat("a", 3));
        assertEquals("abab", StringUtils.repeat("ab", 2));
        assertEquals("?, ?, ?", StringUtils.repeat("?", ", ", 3));
        assertEquals("eee", StringUtils.repeat('e', 3));

        assertEquals("bat  ", StringUtils.rightPad("bat", 5));
        assertEquals("batzz", StringUtils.rightPad("bat", 5, 'z'));
        assertEquals("batyz", StringUtils.rightPad("bat", 5, "yz"));
        assertEquals("bat", StringUtils.rightPad("bat", 2));

        assertEquals("  bat", StringUtils.leftPad("bat", 5));
        assertEquals("zzbat", StringUtils.leftPad("bat", 5, 'z'));
        assertEquals("yzbat", StringUtils.leftPad("bat", 5, "yz"));
        assertEquals("bat", StringUtils.leftPad("bat", 2));

        assertEquals(" ab ", StringUtils.center("ab", 4));
        assertEquals("yayy", StringUtils.center("a", 4, 'y'));
        assertEquals("yayz", StringUtils.center("a", 4, "yz"));
        assertEquals("abcd", StringUtils.center("abcd", 2));
    }

    // Tests abbreviate and abbreviateMiddle
    @Test
    public void testAbbreviate_variousInputs() {
        assertNull(StringUtils.abbreviate(null, 4));
        assertEquals("", StringUtils.abbreviate("", 4));
        assertEquals("abcdefg", StringUtils.abbreviate("abcdefg", 7));
        assertEquals("abc...", StringUtils.abbreviate("abcdefg", 6));
        assertEquals("...fghi...", StringUtils.abbreviate("abcdefghijklmno", 5, 10));

        assertEquals("ab.f", StringUtils.abbreviateMiddle("abcdef", ".", 4));
        assertEquals("abc", StringUtils.abbreviateMiddle("abc", ".", 3));
    }

    // Tests abbreviate width too small throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testAbbreviate_invalidMaxWidth_throwsIllegalArgumentException() {
        StringUtils.abbreviate("abcdefg", 3);
    }

    // Tests Levenshtein distance calculations
    @Test
    public void testLevenshteinDistance_variousInputs() {
        assertEquals(0, StringUtils.getLevenshteinDistance("", ""));
        assertEquals(1, StringUtils.getLevenshteinDistance("", "a"));
        assertEquals(7, StringUtils.getLevenshteinDistance("aaapppp", ""));
        assertEquals(1, StringUtils.getLevenshteinDistance("frog", "fog"));
        assertEquals(3, StringUtils.getLevenshteinDistance("fly", "ant"));
        assertEquals(7, StringUtils.getLevenshteinDistance("elephant", "hippo"));

        assertEquals(7, StringUtils.getLevenshteinDistance("elephant", "hippo", 7));
        assertEquals(-1, StringUtils.getLevenshteinDistance("elephant", "hippo", 6));
        assertEquals(0, StringUtils.getLevenshteinDistance("", "", 0));
    }

    // Tests Levenshtein distance null argument throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testLevenshteinDistance_nullInput_throwsIllegalArgumentException() {
        StringUtils.getLevenshteinDistance(null, "abc");
    }

    // Tests startsWith, endsWith, startsWithIgnoreCase, endsWithIgnoreCase, startsWithAny, endsWithAny
    @Test
    public void testStartsWithAndEndsWith_variousInputs() {
        assertTrue(StringUtils.startsWith(null, null));
        assertFalse(StringUtils.startsWith(null, "abc"));
        assertFalse(StringUtils.startsWith("abcdef", null));
        assertTrue(StringUtils.startsWith("abcdef", "abc"));
        assertFalse(StringUtils.startsWith("ABCDEF", "abc"));

        assertTrue(StringUtils.startsWithIgnoreCase("ABCDEF", "abc"));
        assertFalse(StringUtils.startsWithIgnoreCase("ABCDEF", "xyz"));

        assertTrue(StringUtils.startsWithAny("abcxyz", "abc", "def"));
        assertFalse(StringUtils.startsWithAny("abcxyz", "xyz", "def"));

        assertTrue(StringUtils.endsWith(null, null));
        assertFalse(StringUtils.endsWith(null, "def"));
        assertFalse(StringUtils.endsWith("abcdef", null));
        assertTrue(StringUtils.endsWith("abcdef", "def"));
        assertFalse(StringUtils.endsWith("ABCDEF", "def"));

        assertTrue(StringUtils.endsWithIgnoreCase("ABCDEF", "def"));
        assertFalse(StringUtils.endsWithIgnoreCase("ABCDEF", "xyz"));

        assertTrue(StringUtils.endsWithAny("abcxyz", "xyz", "def"));
        assertFalse(StringUtils.endsWithAny("abcxyz", "abc", "def"));
    }

    // Tests character classification predicates: isAlpha, isNumeric, isWhitespace, isAllLowerCase, isAllUpperCase
    @Test
    public void testCharacterTypeChecks_variousInputs() {
        assertFalse(StringUtils.isAlpha(null));
        assertFalse(StringUtils.isAlpha(""));
        assertTrue(StringUtils.isAlpha("abc"));
        assertFalse(StringUtils.isAlpha("ab2c"));

        assertTrue(StringUtils.isAlphaSpace("ab c"));
        assertFalse(StringUtils.isAlphaSpace("ab2c"));

        assertTrue(StringUtils.isAlphanumeric("ab2c"));
        assertFalse(StringUtils.isAlphanumeric("ab-c"));

        assertTrue(StringUtils.isAlphanumericSpace("ab 2c"));
        assertFalse(StringUtils.isAlphanumericSpace("ab-2c"));

        assertTrue(StringUtils.isAsciiPrintable("abc !~"));
        assertFalse(StringUtils.isAsciiPrintable("abc\u007f"));

        assertTrue(StringUtils.isNumeric("123"));
        assertFalse(StringUtils.isNumeric("12.3"));

        assertTrue(StringUtils.isNumericSpace("12 3"));
        assertFalse(StringUtils.isNumericSpace("12.3"));

        assertTrue(StringUtils.isWhitespace("   \t\n"));
        assertFalse(StringUtils.isWhitespace("  a  "));

        assertTrue(StringUtils.isAllLowerCase("abc"));
        assertFalse(StringUtils.isAllLowerCase("abC"));

        assertTrue(StringUtils.isAllUpperCase("ABC"));
        assertFalse(StringUtils.isAllUpperCase("aBC"));
    }

    // Tests case modification: upperCase, lowerCase, capitalize, uncapitalize, swapCase
    @Test
    public void testCaseConversions_variousInputs() {
        assertEquals("ABC", StringUtils.upperCase("abc"));
        assertEquals("ABC", StringUtils.upperCase("abc", Locale.ENGLISH));
        assertNull(StringUtils.upperCase(null));

        assertEquals("abc", StringUtils.lowerCase("ABC"));
        assertEquals("abc", StringUtils.lowerCase("ABC", Locale.ENGLISH));
        assertNull(StringUtils.lowerCase(null));

        assertEquals("Cat", StringUtils.capitalize("cat"));
        assertEquals("cat", StringUtils.uncapitalize("Cat"));
        assertEquals("tHE DOG HAS A bone", StringUtils.swapCase("The dog has a BONE"));
    }

    // Tests countMatches, difference, indexOfDifference, getCommonPrefix, normalizeSpace, chomp, chop
    @Test
    public void testMiscStringOperations() {
        assertEquals(2, StringUtils.countMatches("abba", "a"));
        assertEquals(0, StringUtils.countMatches(null, "a"));
        assertEquals(0, StringUtils.countMatches("abba", ""));

        assertEquals("robot", StringUtils.difference("i am a machine", "i am a robot"));
        assertEquals(7, StringUtils.indexOfDifference("i am a machine", "i am a robot"));
        assertEquals("i am a ", StringUtils.getCommonPrefix("i am a machine", "i am a robot"));

        assertEquals("a b c", StringUtils.normalizeSpace("  a   b \t\r\n c  "));
        assertNull(StringUtils.normalizeSpace(null));

        assertEquals("abc", StringUtils.chomp("abc\r\n"));
        assertEquals("abc", StringUtils.chomp("abc\n"));
        assertEquals("abc", StringUtils.chop("abcd"));
        assertEquals("abc", StringUtils.chop("abc\r\n"));

        assertEquals("abc", StringUtils.deleteWhitespace(" a b c "));
        assertEquals("domain.com", StringUtils.removeStart("www.domain.com", "www."));
        assertEquals("domain.com", StringUtils.removeStartIgnoreCase("WWW.domain.com", "www."));
        assertEquals("www.domain", StringUtils.removeEnd("www.domain.com", ".com"));
        assertEquals("www.domain", StringUtils.removeEndIgnoreCase("www.domain.COM", ".com"));
        assertEquals("qd", StringUtils.remove("queued", "ue"));
        assertEquals("qeed", StringUtils.remove("queued", 'u'));
        assertEquals("tab", StringUtils.reverse("bat"));
        assertEquals("c.b.a", StringUtils.reverseDelimited("a.b.c", '.'));

        assertEquals("", StringUtils.defaultString(null));
        assertEquals("default", StringUtils.defaultString(null, "default"));
        assertEquals("default", StringUtils.defaultIfBlank("   ", "default"));
        assertEquals("default", StringUtils.defaultIfEmpty("", "default"));
    }

    // Tests public constructor instantiation
    @Test
    public void testConstructor_instanceCreation() {
        StringUtils instance = new StringUtils();
        assertNotNull(instance);
    }
}