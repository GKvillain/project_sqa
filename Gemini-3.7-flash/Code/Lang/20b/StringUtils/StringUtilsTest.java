package org.apache.commons.lang3;

import org.junit.Test;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;

import static org.junit.Assert.*;

public class StringUtilsTest {

    // Tests empty, blank and whitespace checks
    @Test
    public void testIsEmptyAndIsBlank_variousInputs_returnsExpected() {
        assertTrue(StringUtils.isEmpty(null));
        assertTrue(StringUtils.isEmpty(""));
        assertFalse(StringUtils.isEmpty(" "));
        assertFalse(StringUtils.isEmpty("abc"));

        assertTrue(StringUtils.isNotEmpty(" "));
        assertFalse(StringUtils.isNotEmpty(""));
        assertFalse(StringUtils.isNotEmpty(null));

        assertTrue(StringUtils.isBlank(null));
        assertTrue(StringUtils.isBlank(""));
        assertTrue(StringUtils.isBlank("   \t\n"));
        assertFalse(StringUtils.isBlank("  a  "));

        assertTrue(StringUtils.isNotBlank("  a  "));
        assertFalse(StringUtils.isNotBlank("   "));
    }

    // Tests trim and strip operations including null handling
    @Test
    public void testTrimAndStrip_variousInputs_returnsExpected() {
        assertNull(StringUtils.trim(null));
        assertEquals("", StringUtils.trim("  "));
        assertEquals("abc", StringUtils.trim("  abc  "));

        assertNull(StringUtils.trimToNull(null));
        assertNull(StringUtils.trimToNull("   "));
        assertEquals("abc", StringUtils.trimToNull("  abc  "));

        assertEquals("", StringUtils.trimToEmpty(null));
        assertEquals("", StringUtils.trimToEmpty("   "));

        assertNull(StringUtils.strip(null));
        assertEquals("abc", StringUtils.strip("  abc  "));
        assertEquals("abc", StringUtils.strip("yyabczz", "yz"));
        assertEquals("abc", StringUtils.stripStart("yyabc", "y"));
        assertEquals("abc", StringUtils.stripEnd("abczz", "z"));

        assertArrayEquals(new String[]{"a", "b"}, StringUtils.stripAll(new String[]{"  a ", " b "}));
        assertNull(StringUtils.stripAll((String[]) null));
    }

    // Tests equals and equalsIgnoreCase
    @Test
    public void testEqualsAndEqualsIgnoreCase_nullAndNonNull_returnsExpected() {
        assertTrue(StringUtils.equals(null, null));
        assertFalse(StringUtils.equals(null, "abc"));
        assertFalse(StringUtils.equals("abc", null));
        assertTrue(StringUtils.equals("abc", "abc"));
        assertFalse(StringUtils.equals("abc", "ABC"));

        assertTrue(StringUtils.equalsIgnoreCase(null, null));
        assertFalse(StringUtils.equalsIgnoreCase(null, "abc"));
        assertTrue(StringUtils.equalsIgnoreCase("abc", "ABC"));
        assertFalse(StringUtils.equalsIgnoreCase("abc", "abcd"));
    }

    // Tests indexOf and lastIndexOf operations
    @Test
    public void testIndexOfAndLastIndexOf_variousCases_returnsCorrectIndices() {
        assertEquals(-1, StringUtils.indexOf((String) null, 'a'));
        assertEquals(-1, StringUtils.indexOf("", 'a'));
        assertEquals(0, StringUtils.indexOf("aabaabaa", 'a'));
        assertEquals(2, StringUtils.indexOf("aabaabaa", 'b'));
        assertEquals(5, StringUtils.indexOf("aabaabaa", 'b', 3));

        assertEquals(1, StringUtils.indexOf("aabaabaa", "ab"));
        assertEquals(0, StringUtils.indexOf("aabaabaa", ""));
        assertEquals(-1, StringUtils.indexOf("aabaabaa", "xyz"));

        assertEquals(7, StringUtils.lastIndexOf("aabaabaa", 'a'));
        assertEquals(5, StringUtils.lastIndexOf("aabaabaa", 'b'));
        assertEquals(4, StringUtils.lastIndexOf("aabaabaa", "ab"));
        assertEquals(-1, StringUtils.lastIndexOf(null, "ab"));

        assertEquals(0, StringUtils.indexOfIgnoreCase("aabaabaa", "A"));
        assertEquals(1, StringUtils.indexOfIgnoreCase("aabaabaa", "AB"));
        assertEquals(7, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "A"));
    }

    // Tests contains and containsAny / containsNone / containsOnly
    @Test
    public void testContainsMethods_variousCases_returnsExpected() {
        assertFalse(StringUtils.contains(null, 'a'));
        assertTrue(StringUtils.contains("abc", 'a'));
        assertFalse(StringUtils.contains("abc", 'z'));

        assertTrue(StringUtils.containsIgnoreCase("abc", "A"));
        assertFalse(StringUtils.containsIgnoreCase("abc", "Z"));

        assertTrue(StringUtils.containsWhitespace("a b"));
        assertFalse(StringUtils.containsWhitespace("abc"));

        assertTrue(StringUtils.containsAny("zzabyycdxx", 'z', 'a'));
        assertFalse(StringUtils.containsAny("aba", 'z'));

        assertTrue(StringUtils.containsOnly("abab", 'a', 'b', 'c'));
        assertFalse(StringUtils.containsOnly("ab1", 'a', 'b', 'c'));

        assertTrue(StringUtils.containsNone("abab", 'x', 'y', 'z'));
        assertFalse(StringUtils.containsNone("abz", 'x', 'y', 'z'));
    }

    // Tests substring operations including negative indices
    @Test
    public void testSubstringOperations_indicesAndSeparators_returnsCorrectSubstrings() {
        assertNull(StringUtils.substring(null, 0));
        assertEquals("", StringUtils.substring("", 0));
        assertEquals("bc", StringUtils.substring("abc", 1));
        assertEquals("bc", StringUtils.substring("abc", -2));
        assertEquals("ab", StringUtils.substring("abc", 0, 2));
        assertEquals("b", StringUtils.substring("abc", -2, -1));
        assertEquals("", StringUtils.substring("abc", 2, 0));

        assertEquals("ab", StringUtils.left("abc", 2));
        assertEquals("bc", StringUtils.right("abc", 2));
        assertEquals("b", StringUtils.mid("abc", 1, 1));

        assertEquals("a", StringUtils.substringBefore("abcba", "b"));
        assertEquals("cba", StringUtils.substringAfter("abcba", "b"));
        assertEquals("abc", StringUtils.substringBeforeLast("abcba", "b"));
        assertEquals("a", StringUtils.substringAfterLast("abcba", "b"));

        assertEquals("b", StringUtils.substringBetween("wx[b]yz", "[", "]"));
        assertArrayEquals(new String[]{"a", "b", "c"}, StringUtils.substringsBetween("[a][b][c]", "[", "]"));
    }

    // Tests split functionality with separator chars and whole separator
    @Test
    public void testSplit_variousSeparators_splitsCorrectly() {
        assertNull(StringUtils.split(null));
        assertArrayEquals(new String[0], StringUtils.split(""));
        assertArrayEquals(new String[]{"abc", "def"}, StringUtils.split("abc def"));
        assertArrayEquals(new String[]{"a", "b", "c"}, StringUtils.split("a.b.c", '.'));
        assertArrayEquals(new String[]{"a", "b:c"}, StringUtils.split("a:b:c", ":", 2));

        assertArrayEquals(new String[]{"ab", "cd", "ef"}, StringUtils.splitByWholeSeparator("ab-!-cd-!-ef", "-!-"));
        assertArrayEquals(new String[]{"ab", "", "de"}, StringUtils.splitPreserveAllTokens("ab::de", ":"));

        assertArrayEquals(new String[]{"foo", "Bar"}, StringUtils.splitByCharacterTypeCamelCase("fooBar"));
        assertArrayEquals(new String[]{"ASF", "Rules"}, StringUtils.splitByCharacterTypeCamelCase("ASFRules"));
    }

    // Tests join methods with arrays, iterables, and null elements (Defects4J regression)
    @Test
    public void testJoin_arraysAndCollections_joinsCorrectly() {
        assertNull(StringUtils.join((Object[]) null));
        assertEquals("", StringUtils.join(new Object[0]));
        assertEquals("abc", StringUtils.join(new String[]{"a", "b", "c"}));
        assertEquals("a;b;c", StringUtils.join(new String[]{"a", "b", "c"}, ';'));
        assertEquals("a--b--c", StringUtils.join(new String[]{"a", "b", "c"}, "--"));

        assertEquals(";;a", StringUtils.join(new Object[]{null, "", "a"}, ';'));
        assertEquals(",,a", StringUtils.join(new Object[]{null, "", "a"}, ","));
        assertEquals("", StringUtils.join(new Object[]{null}));

        assertEquals("a,b,c", StringUtils.join(Arrays.asList("a", "b", "c"), ','));
        assertEquals("a, b, c", StringUtils.join(Arrays.asList("a", "b", "c"), ", "));
        assertEquals("a", StringUtils.join(Collections.singletonList("a"), ','));
        assertNull(StringUtils.join((Iterable<?>) null, ','));
    }

    // Tests replace and remove methods
    @Test
    public void testReplaceAndRemove_variousInputs_replacesCorrectly() {
        assertNull(StringUtils.replace(null, "a", "b"));
        assertEquals("aba", StringUtils.replace("aba", null, "z"));
        assertEquals("zbz", StringUtils.replace("aba", "a", "z"));
        assertEquals("zba", StringUtils.replaceOnce("aba", "a", "z"));
        assertEquals("zbza", StringUtils.replace("abaa", "a", "z", 2));

        assertEquals("wcte", StringUtils.replaceEach("abcde", new String[]{"ab", "d"}, new String[]{"w", "t"}));
        assertEquals("aycya", StringUtils.replaceChars("abcba", 'b', 'y'));
        assertEquals("ayzya", StringUtils.replaceChars("abcba", "bc", "yz"));

        assertEquals("domain.com", StringUtils.removeStart("www.domain.com", "www."));
        assertEquals("www.domain", StringUtils.removeEnd("www.domain.com", ".com"));
        assertEquals("qd", StringUtils.remove("queued", "ue"));
        assertEquals("qeed", StringUtils.remove("queued", 'u'));
        assertEquals("abc", StringUtils.deleteWhitespace(" a b c "));
    }

    // Tests padding and centering functions
    @Test
    public void testPadAndCenter_variousLengths_padsCorrectly() {
        assertNull(StringUtils.leftPad(null, 5));
        assertEquals("  bat", StringUtils.leftPad("bat", 5));
        assertEquals("zzbat", StringUtils.leftPad("bat", 5, 'z'));
        assertEquals("yzbat", StringUtils.leftPad("bat", 5, "yz"));
        assertEquals("bat", StringUtils.leftPad("bat", 2));

        assertNull(StringUtils.rightPad(null, 5));
        assertEquals("bat  ", StringUtils.rightPad("bat", 5));
        assertEquals("batzz", StringUtils.rightPad("bat", 5, 'z'));
        assertEquals("batyz", StringUtils.rightPad("bat", 5, "yz"));

        assertNull(StringUtils.center(null, 5));
        assertEquals(" ab ", StringUtils.center("ab", 4));
        assertEquals("yaby", StringUtils.center("ab", 4, 'y'));
        assertEquals("yabz", StringUtils.center("ab", 4, "yz"));
    }

    // Tests repeat method
    @Test
    public void testRepeat_countAndSeparators_repeatsCorrectly() {
        assertNull(StringUtils.repeat(null, 2));
        assertEquals("", StringUtils.repeat("a", 0));
        assertEquals("", StringUtils.repeat("a", -1));
        assertEquals("aaa", StringUtils.repeat("a", 3));
        assertEquals("abab", StringUtils.repeat("ab", 2));
        assertEquals("?, ?, ?", StringUtils.repeat("?", ", ", 3));
        assertEquals("eee", StringUtils.repeat('e', 3));
    }

    // Tests case modification methods
    @Test
    public void testCaseConversions_upperLowerCapitalize_returnsExpected() {
        assertNull(StringUtils.upperCase(null));
        assertEquals("ABC", StringUtils.upperCase("aBc"));
        assertEquals("ABC", StringUtils.upperCase("aBc", Locale.ENGLISH));

        assertNull(StringUtils.lowerCase(null));
        assertEquals("abc", StringUtils.lowerCase("aBc"));
        assertEquals("abc", StringUtils.lowerCase("aBc", Locale.ENGLISH));

        assertNull(StringUtils.capitalize(null));
        assertEquals("Cat", StringUtils.capitalize("cat"));
        assertEquals("CAT", StringUtils.capitalize("cAT"));

        assertNull(StringUtils.uncapitalize(null));
        assertEquals("cat", StringUtils.uncapitalize("Cat"));

        assertNull(StringUtils.swapCase(null));
        assertEquals("tHE DOG", StringUtils.swapCase("The Dog"));
    }

    // Tests character classification methods
    @Test
    public void testCharacterTests_isAlphaNumericWhitespace_returnsExpected() {
        assertFalse(StringUtils.isAlpha(null));
        assertFalse(StringUtils.isAlpha(""));
        assertTrue(StringUtils.isAlpha("abc"));
        assertFalse(StringUtils.isAlpha("ab2c"));

        assertTrue(StringUtils.isAlphaSpace("ab c"));
        assertTrue(StringUtils.isAlphanumeric("ab2c"));
        assertFalse(StringUtils.isAlphanumeric("ab-c"));
        assertTrue(StringUtils.isAlphanumericSpace("ab 2 c"));

        assertTrue(StringUtils.isNumeric("123"));
        assertFalse(StringUtils.isNumeric("12.3"));
        assertTrue(StringUtils.isNumericSpace("12 3"));

        assertTrue(StringUtils.isWhitespace("  \t\n"));
        assertFalse(StringUtils.isWhitespace(" a "));

        assertTrue(StringUtils.isAllLowerCase("abc"));
        assertFalse(StringUtils.isAllLowerCase("abC"));

        assertTrue(StringUtils.isAllUpperCase("ABC"));
        assertFalse(StringUtils.isAllUpperCase("ABc"));

        assertTrue(StringUtils.isAsciiPrintable("abc !~"));
        assertFalse(StringUtils.isAsciiPrintable("abc\u007f"));
    }

    // Tests abbreviation methods
    @Test
    public void testAbbreviate_variousLengthsAndOffsets_abbreviatesCorrectly() {
        assertNull(StringUtils.abbreviate(null, 4));
        assertEquals("", StringUtils.abbreviate("", 4));
        assertEquals("abcdefg", StringUtils.abbreviate("abcdefg", 7));
        assertEquals("abcd...", StringUtils.abbreviate("abcdefghij", 7));
        assertEquals("...fghi...", StringUtils.abbreviate("abcdefghijklmno", 5, 10));
        assertEquals("ab.f", StringUtils.abbreviateMiddle("abcdef", ".", 4));
    }

    // Tests exception path for abbreviate when maxWidth is too small
    @Test(expected = IllegalArgumentException.class)
    public void testAbbreviate_widthTooSmall_throwsException() {
        StringUtils.abbreviate("abcdef", 3);
    }

    // Tests defaults, countMatches, reverse, difference and prefix
    @Test
    public void testMiscMethods_defaultsCountReverseDiff_returnsExpected() {
        assertEquals("", StringUtils.defaultString(null));
        assertEquals("default", StringUtils.defaultString(null, "default"));
        assertEquals("default", StringUtils.defaultIfBlank("  ", "default"));
        assertEquals("default", StringUtils.defaultIfEmpty("", "default"));

        assertEquals(2, StringUtils.countMatches("abba", "a"));
        assertEquals(0, StringUtils.countMatches("abba", "x"));

        assertEquals("tab", StringUtils.reverse("bat"));
        assertEquals("c.b.a", StringUtils.reverseDelimited("a.b.c", '.'));

        assertEquals("robot", StringUtils.difference("i am a machine", "i am a robot"));
        assertEquals(7, StringUtils.indexOfDifference("i am a machine", "i am a robot"));
        assertEquals("i am a ", StringUtils.getCommonPrefix("i am a machine", "i am a robot"));
    }

    // Tests startsWith and endsWith variants
    @Test
    public void testStartsAndEndsWith_caseSensitiveAndInsensitive_returnsExpected() {
        assertTrue(StringUtils.startsWith(null, null));
        assertFalse(StringUtils.startsWith(null, "abc"));
        assertTrue(StringUtils.startsWith("abcdef", "abc"));
        assertFalse(StringUtils.startsWith("ABCDEF", "abc"));
        assertTrue(StringUtils.startsWithIgnoreCase("ABCDEF", "abc"));
        assertTrue(StringUtils.startsWithAny("abcxyz", "def", "abc"));

        assertTrue(StringUtils.endsWith(null, null));
        assertTrue(StringUtils.endsWith("abcdef", "def"));
        assertFalse(StringUtils.endsWith("ABCDEF", "def"));
        assertTrue(StringUtils.endsWithIgnoreCase("ABCDEF", "def"));
        assertTrue(StringUtils.endsWithAny("abcxyz", "def", "xyz"));
    }

    // Tests Levenshtein distance calculation
    @Test
    public void testGetLevenshteinDistance_validInputs_returnsExpectedDistance() {
        assertEquals(0, StringUtils.getLevenshteinDistance("", ""));
        assertEquals(1, StringUtils.getLevenshteinDistance("", "a"));
        assertEquals(1, StringUtils.getLevenshteinDistance("frog", "fog"));
        assertEquals(3, StringUtils.getLevenshteinDistance("fly", "ant"));
        assertEquals(7, StringUtils.getLevenshteinDistance("elephant", "hippo"));

        assertEquals(7, StringUtils.getLevenshteinDistance("elephant", "hippo", 7));
        assertEquals(-1, StringUtils.getLevenshteinDistance("elephant", "hippo", 6));
    }

    // Tests exception path for Levenshtein distance on null input
    @Test(expected = IllegalArgumentException.class)
    public void testGetLevenshteinDistance_nullInput_throwsException() {
        StringUtils.getLevenshteinDistance(null, "abc");
    }

    // Tests chomp and chop operations
    @Test
    public void testChompAndChop_variousLineEndings_returnsChopped() {
        assertNull(StringUtils.chomp(null));
        assertEquals("", StringUtils.chomp(""));
        assertEquals("abc", StringUtils.chomp("abc\r\n"));
        assertEquals("abc", StringUtils.chomp("abc\n"));
        assertEquals("abc", StringUtils.chomp("abc\r"));
        assertEquals("foo", StringUtils.chomp("foobar", "bar"));

        assertNull(StringUtils.chop(null));
        assertEquals("", StringUtils.chop(""));
        assertEquals("", StringUtils.chop("a"));
        assertEquals("ab", StringUtils.chop("abc"));
        assertEquals("abc", StringUtils.chop("abc\r\n"));
    }

    // Tests normalizeSpace and constructor
    @Test
    public void testNormalizeSpaceAndConstructor_standardUsage_returnsExpected() {
        assertNotNull(new StringUtils());
        assertNull(StringUtils.normalizeSpace(null));
        assertEquals("a b c", StringUtils.normalizeSpace("  a   b \t\n c  "));
    }
}