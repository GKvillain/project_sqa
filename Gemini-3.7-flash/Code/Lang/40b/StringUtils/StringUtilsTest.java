package org.apache.commons.lang;

import org.junit.Test;
import java.util.Arrays;
import java.util.Locale;
import static org.junit.Assert.*;

public class StringUtilsTest {

    // Tests empty and blank checks with null, empty, whitespace and normal strings
    @Test
    public void testIsEmptyAndIsBlank_variousInputs_returnsExpectedResults() {
        assertTrue(StringUtils.isEmpty(null));
        assertTrue(StringUtils.isEmpty(""));
        assertFalse(StringUtils.isEmpty(" "));
        assertFalse(StringUtils.isEmpty("bob"));

        assertFalse(StringUtils.isNotEmpty(null));
        assertFalse(StringUtils.isNotEmpty(""));
        assertTrue(StringUtils.isNotEmpty(" "));
        assertTrue(StringUtils.isNotEmpty("bob"));

        assertTrue(StringUtils.isBlank(null));
        assertTrue(StringUtils.isBlank(""));
        assertTrue(StringUtils.isBlank(" \t\r\n "));
        assertFalse(StringUtils.isBlank(" bob "));

        assertFalse(StringUtils.isNotBlank(null));
        assertFalse(StringUtils.isNotBlank(""));
        assertFalse(StringUtils.isNotBlank("   "));
        assertTrue(StringUtils.isNotBlank(" bob "));
    }

    // Tests trim and strip operations with whitespace and specific characters
    @Test
    public void testTrimAndStrip_variousInputs_returnsExpectedResults() {
        assertNull(StringUtils.trim(null));
        assertEquals("", StringUtils.trim("  \t\r  "));
        assertEquals("abc", StringUtils.trim("  abc  "));

        assertNull(StringUtils.trimToNull(null));
        assertNull(StringUtils.trimToNull("   "));
        assertEquals("abc", StringUtils.trimToNull("  abc  "));

        assertEquals("", StringUtils.trimToEmpty(null));
        assertEquals("", StringUtils.trimToEmpty("   "));
        assertEquals("abc", StringUtils.trimToEmpty("  abc  "));

        assertNull(StringUtils.strip(null));
        assertEquals("abc", StringUtils.strip("  abc  "));
        assertEquals("abc", StringUtils.strip("..abc..", "."));
        assertEquals("abc..", StringUtils.stripStart("..abc..", "."));
        assertEquals("..abc", StringUtils.stripEnd("..abc..", "."));

        String[] strippedArray = StringUtils.stripAll(new String[]{"  ab  ", "  cd  ", null}, null);
        assertArrayEquals(new String[]{"ab", "cd", null}, strippedArray);
    }

    // Tests equals and equalsIgnoreCase null-safety and case behavior
    @Test
    public void testEqualsAndEqualsIgnoreCase_variousInputs_returnsExpectedResults() {
        assertTrue(StringUtils.equals(null, null));
        assertFalse(StringUtils.equals(null, "abc"));
        assertFalse(StringUtils.equals("abc", null));
        assertTrue(StringUtils.equals("abc", "abc"));
        assertFalse(StringUtils.equals("abc", "ABC"));

        assertTrue(StringUtils.equalsIgnoreCase(null, null));
        assertFalse(StringUtils.equalsIgnoreCase(null, "abc"));
        assertFalse(StringUtils.equalsIgnoreCase("abc", null));
        assertTrue(StringUtils.equalsIgnoreCase("abc", "abc"));
        assertTrue(StringUtils.equalsIgnoreCase("abc", "ABC"));
    }

    // Tests indexOf, lastIndexOf and ordinalIndexOf with chars and strings
    @Test
    public void testIndexOfAndLastIndexOf_variousInputs_returnsCorrectIndices() {
        assertEquals(-1, StringUtils.indexOf(null, 'a'));
        assertEquals(-1, StringUtils.indexOf("", 'a'));
        assertEquals(0, StringUtils.indexOf("aabaabaa", 'a'));
        assertEquals(2, StringUtils.indexOf("aabaabaa", 'b'));
        assertEquals(5, StringUtils.indexOf("aabaabaa", 'b', 3));

        assertEquals(-1, StringUtils.indexOf(null, "a"));
        assertEquals(0, StringUtils.indexOf("aabaabaa", ""));
        assertEquals(1, StringUtils.indexOf("aabaabaa", "ab"));
        assertEquals(2, StringUtils.indexOf("aabaabaa", "b", 0));

        assertEquals(2, StringUtils.ordinalIndexOf("aabaabaa", "b", 1));
        assertEquals(5, StringUtils.ordinalIndexOf("aabaabaa", "b", 2));
        assertEquals(-1, StringUtils.ordinalIndexOf("aabaabaa", "b", 3));
        assertEquals(-1, StringUtils.ordinalIndexOf(null, "a", 1));

        assertEquals(7, StringUtils.lastIndexOf("aabaabaa", 'a'));
        assertEquals(5, StringUtils.lastIndexOf("aabaabaa", 'b'));
        assertEquals(2, StringUtils.lastIndexOf("aabaabaa", 'b', 4));

        assertEquals(4, StringUtils.lastIndexOf("aabaabaa", "ab", 8));
        assertEquals(-1, StringUtils.lastIndexOf(null, "ab"));
    }

    // Tests contains and containsIgnoreCase including international/case edge cases
    @Test
    public void testContainsAndContainsIgnoreCase_variousInputs_returnsExpectedResults() {
        assertFalse(StringUtils.contains(null, 'a'));
        assertFalse(StringUtils.contains("", 'a'));
        assertTrue(StringUtils.contains("abc", 'a'));
        assertFalse(StringUtils.contains("abc", 'z'));

        assertFalse(StringUtils.contains(null, "a"));
        assertFalse(StringUtils.contains("abc", null));
        assertTrue(StringUtils.contains("abc", ""));
        assertTrue(StringUtils.contains("abc", "bc"));
        assertFalse(StringUtils.contains("abc", "d"));

        assertFalse(StringUtils.containsIgnoreCase(null, "a"));
        assertFalse(StringUtils.containsIgnoreCase("abc", null));
        assertTrue(StringUtils.containsIgnoreCase("", ""));
        assertTrue(StringUtils.containsIgnoreCase("abc", ""));
        assertTrue(StringUtils.containsIgnoreCase("abc", "A"));
        assertTrue(StringUtils.containsIgnoreCase("ABC", "a"));
        assertTrue(StringUtils.containsIgnoreCase("aBcDe", "bcd"));
        assertFalse(StringUtils.containsIgnoreCase("abc", "z"));

        // Case-insensitivity under special Locale conditions (e.g. Turkish dotted/dotless i)
        Locale defaultLocale = Locale.getDefault();
        try {
            Locale.setDefault(new Locale("tr", "TR"));
            assertTrue(StringUtils.containsIgnoreCase("TITLE", "title"));
            assertTrue(StringUtils.containsIgnoreCase("title", "TITLE"));
        } finally {
            Locale.setDefault(defaultLocale);
        }
    }

    // Tests character set searching methods: indexOfAny, containsAny, containsOnly, containsNone
    @Test
    public void testCharSetSearchMethods_variousInputs_returnsExpectedResults() {
        assertEquals(0, StringUtils.indexOfAny("zzabyycdxx", new char[]{'z', 'a'}));
        assertEquals(3, StringUtils.indexOfAny("zzabyycdxx", "by"));
        assertEquals(-1, StringUtils.indexOfAny("aba", "z"));
        assertEquals(-1, StringUtils.indexOfAny(null, "a"));

        assertTrue(StringUtils.containsAny("zzabyycdxx", new char[]{'z', 'a'}));
        assertTrue(StringUtils.containsAny("zzabyycdxx", "by"));
        assertFalse(StringUtils.containsAny("aba", "z"));
        assertFalse(StringUtils.containsAny(null, "z"));

        assertEquals(3, StringUtils.indexOfAnyBut("zzabyycdxx", new char[]{'z', 'a'}));
        assertEquals(3, StringUtils.indexOfAnyBut("zzabyycdxx", "za"));
        assertEquals(-1, StringUtils.indexOfAnyBut("aba", "ab"));

        assertTrue(StringUtils.containsOnly("abab", new char[]{'a', 'b', 'c'}));
        assertTrue(StringUtils.containsOnly("abab", "abc"));
        assertFalse(StringUtils.containsOnly("ab1", "abc"));
        assertTrue(StringUtils.containsOnly("", "abc"));
        assertFalse(StringUtils.containsOnly(null, "abc"));

        assertTrue(StringUtils.containsNone("abab", new char[]{'x', 'y', 'z'}));
        assertTrue(StringUtils.containsNone("abab", "xyz"));
        assertFalse(StringUtils.containsNone("abz", "xyz"));
        assertTrue(StringUtils.containsNone(null, "xyz"));
    }

    // Tests substring operations with positive and negative boundaries
    @Test
    public void testSubstringOperations_variousBoundaries_returnsCorrectSubstrings() {
        assertNull(StringUtils.substring(null, 0));
        assertEquals("", StringUtils.substring("", 0));
        assertEquals("bc", StringUtils.substring("abc", 1));
        assertEquals("c", StringUtils.substring("abc", 2));
        assertEquals("bc", StringUtils.substring("abc", -2));
        assertEquals("abc", StringUtils.substring("abc", -4));
        assertEquals("", StringUtils.substring("abc", 4));

        assertEquals("ab", StringUtils.substring("abc", 0, 2));
        assertEquals("", StringUtils.substring("abc", 2, 0));
        assertEquals("c", StringUtils.substring("abc", 2, 4));
        assertEquals("b", StringUtils.substring("abc", -2, -1));

        assertEquals("ab", StringUtils.left("abc", 2));
        assertEquals("abc", StringUtils.left("abc", 5));
        assertEquals("", StringUtils.left("abc", -1));
        assertNull(StringUtils.left(null, 2));

        assertEquals("bc", StringUtils.right("abc", 2));
        assertEquals("abc", StringUtils.right("abc", 5));
        assertEquals("", StringUtils.right("abc", -1));

        assertEquals("b", StringUtils.mid("abc", 1, 1));
        assertEquals("bc", StringUtils.mid("abc", 1, 5));
        assertEquals("", StringUtils.mid("abc", 5, 1));
        assertEquals("ab", StringUtils.mid("abc", -1, 2));
    }

    // Tests substring before/after/between extraction methods
    @Test
    public void testSubstringsBeforeAfterBetween_validAndEdgeInputs_returnsExpectedResults() {
        assertNull(StringUtils.substringBefore(null, "a"));
        assertEquals("a", StringUtils.substringBefore("abcba", "b"));
        assertEquals("abc", StringUtils.substringBefore("abc", "d"));
        assertEquals("", StringUtils.substringBefore("abc", ""));

        assertEquals("cba", StringUtils.substringAfter("abcba", "b"));
        assertEquals("", StringUtils.substringAfter("abc", "d"));
        assertEquals("abc", StringUtils.substringAfter("abc", ""));

        assertEquals("abc", StringUtils.substringBeforeLast("abcba", "b"));
        assertEquals("a", StringUtils.substringAfterLast("abcba", "b"));

        assertEquals("b", StringUtils.substringBetween("wx[b]yz", "[", "]"));
        assertNull(StringUtils.substringBetween("wxyz", "[", "]"));
        assertEquals("abc", StringUtils.substringBetween("tagabctag", "tag"));

        String[] tags = StringUtils.substringsBetween("[a][b][c]", "[", "]");
        assertArrayEquals(new String[]{"a", "b", "c"}, tags);
        assertNull(StringUtils.substringsBetween("abc", "[", "]"));
    }

    // Tests string splitting with various separators and whitespace handling
    @Test
    public void testSplitMethods_variousSeparators_splitsCorrectly() {
        assertNull(StringUtils.split(null));
        assertArrayEquals(new String[]{}, StringUtils.split(""));
        assertArrayEquals(new String[]{"abc", "def"}, StringUtils.split("abc def"));
        assertArrayEquals(new String[]{"abc", "def"}, StringUtils.split("abc  def"));
        assertArrayEquals(new String[]{"a", "b", "c"}, StringUtils.split("a:b:c", ':'));
        assertArrayEquals(new String[]{"a", "b:c"}, StringUtils.split("a:b:c", ":", 2));

        assertArrayEquals(new String[]{"ab", "cd", "ef"}, StringUtils.splitByWholeSeparator("ab-!-cd-!-ef", "-!-"));
        assertArrayEquals(new String[]{"ab", "cd-!-ef"}, StringUtils.splitByWholeSeparator("ab-!-cd-!-ef", "-!-", 2));

        assertArrayEquals(new String[]{"a", "", "b", "c"}, StringUtils.splitPreserveAllTokens("a..b.c", '.'));
        assertArrayEquals(new String[]{"", "a", "b", ""}, StringUtils.splitPreserveAllTokens(" a b ", ' '));

        assertArrayEquals(new String[]{"ab", " ", "de", " ", "fg"}, StringUtils.splitByCharacterType("ab de fg"));
        assertArrayEquals(new String[]{"foo", "Bar"}, StringUtils.splitByCharacterTypeCamelCase("fooBar"));
        assertArrayEquals(new String[]{"ASF", "Rules"}, StringUtils.splitByCharacterTypeCamelCase("ASFRules"));
    }

    // Tests join methods with arrays, iterators and collections
    @Test
    public void testJoinMethods_variousInputs_joinsCorrectly() {
        assertNull(StringUtils.join((Object[]) null));
        assertEquals("", StringUtils.join(new Object[]{}));
        assertEquals("abc", StringUtils.join(new Object[]{"a", "b", "c"}));
        assertEquals("a;b;c", StringUtils.join(new Object[]{"a", "b", "c"}, ';'));
        assertEquals("a--b--c", StringUtils.join(new Object[]{"a", "b", "c"}, "--"));
        assertEquals("b--c", StringUtils.join(new Object[]{"a", "b", "c"}, "--", 1, 3));

        assertEquals("a,b,c", StringUtils.join(Arrays.asList("a", "b", "c"), ","));
        assertEquals("a,b,c", StringUtils.join(Arrays.asList("a", "b", "c").iterator(), ','));
        assertNull(StringUtils.join((java.util.Collection<?>) null, ","));
    }

    // Tests replace, replaceOnce, replaceEach and replaceChars methods
    @Test
    public void testReplaceMethods_variousInputs_replacesCorrectly() {
        assertNull(StringUtils.replace(null, "a", "b"));
        assertEquals("any", StringUtils.replace("any", null, "b"));
        assertEquals("zbz", StringUtils.replace("aba", "a", "z"));
        assertEquals("zba", StringUtils.replaceOnce("aba", "a", "z"));
        assertEquals("zbza", StringUtils.replace("abaa", "a", "z", 2));

        assertEquals("wcte", StringUtils.replaceEach("abcde", new String[]{"ab", "d"}, new String[]{"w", "t"}));
        assertEquals("tcte", StringUtils.replaceEachRepeatedly("abcde", new String[]{"ab", "d"}, new String[]{"d", "t"}));

        assertNull(StringUtils.replaceChars(null, 'a', 'b'));
        assertEquals("aycya", StringUtils.replaceChars("abcba", 'b', 'y'));
        assertEquals("ayzya", StringUtils.replaceChars("abcba", "bc", "yz"));
        assertEquals("ac", StringUtils.replaceChars("abc", "b", null));
    }

    // Tests replaceEach with mismatched array lengths expecting exception
    @Test(expected = IllegalArgumentException.class)
    public void testReplaceEach_mismatchedArrayLengths_throwsIllegalArgumentException() {
        StringUtils.replaceEach("abc", new String[]{"a", "b"}, new String[]{"x"});
    }

    // Tests remove, deleteWhitespace and overlay operations
    @Test
    public void testRemoveAndDeleteAndOverlay_variousInputs_returnsExpectedResults() {
        assertNull(StringUtils.remove(null, "a"));
        assertEquals("qd", StringUtils.remove("queued", "ue"));
        assertEquals("qeed", StringUtils.remove("queued", 'u'));
        assertEquals("domain.com", StringUtils.removeStart("www.domain.com", "www."));
        assertEquals("domain.com", StringUtils.removeStartIgnoreCase("WWW.domain.com", "www."));
        assertEquals("www.domain", StringUtils.removeEnd("www.domain.com", ".com"));
        assertEquals("www.domain", StringUtils.removeEndIgnoreCase("www.domain.COM", ".com"));

        assertNull(StringUtils.deleteWhitespace(null));
        assertEquals("abc", StringUtils.deleteWhitespace("   a b   c  \t\r\n"));

        assertNull(StringUtils.overlay(null, "abc", 0, 0));
        assertEquals("abzzzzef", StringUtils.overlay("abcdef", "zzzz", 2, 4));
        assertEquals("abzzzzef", StringUtils.overlay("abcdef", "zzzz", 4, 2));
        assertEquals("zzzzabcdef", StringUtils.overlay("abcdef", "zzzz", -2, -3));
        assertEquals("abcdefzzzz", StringUtils.overlay("abcdef", "zzzz", 8, 10));
    }

    // Tests chomp and chop newline/character truncation methods
    @Test
    public void testChompAndChop_variousInputs_truncatesCorrectly() {
        assertNull(StringUtils.chomp(null));
        assertEquals("", StringUtils.chomp(""));
        assertEquals("abc", StringUtils.chomp("abc\r\n"));
        assertEquals("abc", StringUtils.chomp("abc\n"));
        assertEquals("abc", StringUtils.chomp("abc\r"));
        assertEquals("foo", StringUtils.chomp("foobar", "bar"));
        assertEquals("foobar", StringUtils.chomp("foobar", "baz"));

        assertNull(StringUtils.chop(null));
        assertEquals("", StringUtils.chop(""));
        assertEquals("", StringUtils.chop("a"));
        assertEquals("ab", StringUtils.chop("abc"));
        assertEquals("abc", StringUtils.chop("abc\r\n"));
        assertEquals("abc", StringUtils.chop("abc\n"));
    }

    // Tests padding, repeating and centering methods
    @Test
    public void testPadAndRepeatAndCenter_variousInputs_returnsPaddedStrings() {
        assertNull(StringUtils.repeat(null, 2));
        assertEquals("", StringUtils.repeat("a", 0));
        assertEquals("aaa", StringUtils.repeat("a", 3));
        assertEquals("abab", StringUtils.repeat("ab", 2));
        assertEquals("?, ?, ?", StringUtils.repeat("?", ", ", 3));

        assertNull(StringUtils.rightPad(null, 5));
        assertEquals("bat  ", StringUtils.rightPad("bat", 5));
        assertEquals("batzz", StringUtils.rightPad("bat", 5, 'z'));
        assertEquals("batyz", StringUtils.rightPad("bat", 5, "yz"));
        assertEquals("bat", StringUtils.rightPad("bat", 2));

        assertNull(StringUtils.leftPad(null, 5));
        assertEquals("  bat", StringUtils.leftPad("bat", 5));
        assertEquals("zzbat", StringUtils.leftPad("bat", 5, 'z'));
        assertEquals("yzbat", StringUtils.leftPad("bat", 5, "yz"));
        assertEquals("bat", StringUtils.leftPad("bat", 2));

        assertNull(StringUtils.center(null, 4));
        assertEquals(" ab ", StringUtils.center("ab", 4));
        assertEquals("yayy", StringUtils.center("a", 4, 'y'));
        assertEquals("yayz", StringUtils.center("a", 4, "yz"));
        assertEquals("abcd", StringUtils.center("abcd", 2));
    }

    // Tests case conversion and capitalization methods
    @Test
    public void testCaseConversions_variousInputs_changesCaseCorrectly() {
        assertNull(StringUtils.upperCase(null));
        assertEquals("ABC", StringUtils.upperCase("aBc"));
        assertEquals("ABC", StringUtils.upperCase("aBc", Locale.ENGLISH));

        assertNull(StringUtils.lowerCase(null));
        assertEquals("abc", StringUtils.lowerCase("aBc"));
        assertEquals("abc", StringUtils.lowerCase("aBc", Locale.ENGLISH));

        assertNull(StringUtils.capitalize(null));
        assertEquals("Cat", StringUtils.capitalize("cat"));
        assertEquals("CAt", StringUtils.capitalize("cAt"));

        assertNull(StringUtils.uncapitalize(null));
        assertEquals("cat", StringUtils.uncapitalize("Cat"));
        assertEquals("cAT", StringUtils.uncapitalize("CAT"));

        assertNull(StringUtils.swapCase(null));
        assertEquals("tHE DOG HAS A bone", StringUtils.swapCase("The dog has a BONE"));
    }

    // Tests character property checking methods (isAlpha, isNumeric, etc.)
    @Test
    public void testCharacterTypeChecks_variousInputs_identifiesCorrectly() {
        assertFalse(StringUtils.isAlpha(null));
        assertTrue(StringUtils.isAlpha(""));
        assertTrue(StringUtils.isAlpha("abc"));
        assertFalse(StringUtils.isAlpha("ab2c"));

        assertTrue(StringUtils.isAlphaSpace("ab c"));
        assertFalse(StringUtils.isAlphaSpace("ab2c"));

        assertTrue(StringUtils.isAlphanumeric("ab2c"));
        assertFalse(StringUtils.isAlphanumeric("ab-c"));

        assertTrue(StringUtils.isAlphanumericSpace("ab 2c"));
        assertFalse(StringUtils.isAlphanumericSpace("ab-2c"));

        assertTrue(StringUtils.isNumeric("123"));
        assertFalse(StringUtils.isNumeric("12.3"));

        assertTrue(StringUtils.isNumericSpace("12 3"));
        assertFalse(StringUtils.isNumericSpace("12.3"));

        assertTrue(StringUtils.isWhitespace("  \t\r\n"));
        assertFalse(StringUtils.isWhitespace(" a "));

        assertTrue(StringUtils.isAsciiPrintable("!ab-c~"));
        assertFalse(StringUtils.isAsciiPrintable("\u007f"));

        assertTrue(StringUtils.isAllLowerCase("abc"));
        assertFalse(StringUtils.isAllLowerCase("abC"));

        assertTrue(StringUtils.isAllUpperCase("ABC"));
        assertFalse(StringUtils.isAllUpperCase("aBC"));
    }

    // Tests difference, common prefix and Levenshtein distance methods
    @Test
    public void testDifferenceAndCommonPrefixAndLevenshtein_variousInputs_returnsExpectedResults() {
        assertEquals("xyz", StringUtils.difference("abcde", "abxyz"));
        assertEquals(-1, StringUtils.indexOfDifference("abc", "abc"));
        assertEquals(2, StringUtils.indexOfDifference("ab", "abxyz"));

        assertEquals("i am a ", StringUtils.getCommonPrefix(new String[]{"i am a machine", "i am a robot"}));
        assertEquals("", StringUtils.getCommonPrefix(new String[]{"abc", "xyz"}));

        assertEquals(0, StringUtils.getLevenshteinDistance("", ""));
        assertEquals(1, StringUtils.getLevenshteinDistance("", "a"));
        assertEquals(1, StringUtils.getLevenshteinDistance("frog", "fog"));
        assertEquals(3, StringUtils.getLevenshteinDistance("fly", "ant"));
        assertEquals(7, StringUtils.getLevenshteinDistance("elephant", "hippo"));
    }

    // Tests Levenshtein distance null input expecting exception
    @Test(expected = IllegalArgumentException.class)
    public void testGetLevenshteinDistance_nullInput_throwsIllegalArgumentException() {
        StringUtils.getLevenshteinDistance(null, "abc");
    }

    // Tests abbreviate method with normal cases and offset boundaries
    @Test
    public void testAbbreviate_variousWidthsAndOffsets_returnsAbbreviatedStrings() {
        assertNull(StringUtils.abbreviate(null, 4));
        assertEquals("", StringUtils.abbreviate("", 4));
        assertEquals("abc...", StringUtils.abbreviate("abcdefg", 6));
        assertEquals("abcdefg", StringUtils.abbreviate("abcdefg", 7));
        assertEquals("abcdefg", StringUtils.abbreviate("abcdefg", 8));
        assertEquals("a...", StringUtils.abbreviate("abcdefg", 4));

        assertEquals("abcdefg...", StringUtils.abbreviate("abcdefghijklmno", 0, 10));
        assertEquals("...fghi...", StringUtils.abbreviate("abcdefghijklmno", 5, 10));
        assertEquals("...ijklmno", StringUtils.abbreviate("abcdefghijklmno", 10, 10));
    }

    // Tests abbreviate with invalid width expecting exception
    @Test(expected = IllegalArgumentException.class)
    public void testAbbreviate_widthLessThanFour_throwsIllegalArgumentException() {
        StringUtils.abbreviate("abcdefg", 3);
    }

    // Tests startsWith and endsWith methods including IgnoreCase and Any variants
    @Test
    public void testStartsWithAndEndsWith_variousInputs_returnsExpectedResults() {
        assertTrue(StringUtils.startsWith(null, null));
        assertFalse(StringUtils.startsWith(null, "abc"));
        assertFalse(StringUtils.startsWith("abcdef", null));
        assertTrue(StringUtils.startsWith("abcdef", "abc"));
        assertFalse(StringUtils.startsWith("ABCDEF", "abc"));

        assertTrue(StringUtils.startsWithIgnoreCase(null, null));
        assertTrue(StringUtils.startsWithIgnoreCase("ABCDEF", "abc"));

        assertTrue(StringUtils.startsWithAny("abcxyz", new String[]{"xyz", "abc"}));
        assertFalse(StringUtils.startsWithAny("abcxyz", new String[]{"xyz", "def"}));
        assertFalse(StringUtils.startsWithAny(null, new String[]{"abc"}));

        assertTrue(StringUtils.endsWith(null, null));
        assertFalse(StringUtils.endsWith(null, "def"));
        assertFalse(StringUtils.endsWith("abcdef", null));
        assertTrue(StringUtils.endsWith("abcdef", "def"));
        assertFalse(StringUtils.endsWith("ABCDEF", "def"));

        assertTrue(StringUtils.endsWithIgnoreCase(null, null));
        assertTrue(StringUtils.endsWithIgnoreCase("ABCDEF", "def"));

        assertEquals(0, StringUtils.countMatches("abba", "xxx"));
        assertEquals(2, StringUtils.countMatches("abba", "a"));

        assertEquals("default", StringUtils.defaultString(null, "default"));
        assertEquals("abc", StringUtils.defaultString("abc", "default"));
        assertEquals("default", StringUtils.defaultIfEmpty("", "default"));
        assertEquals("abc", StringUtils.defaultIfEmpty("abc", "default"));

        assertEquals("tab", StringUtils.reverse("bat"));
        assertEquals("c.b.a", StringUtils.reverseDelimited("a.b.c", '.'));
        assertEquals(3, StringUtils.length("abc"));
        assertEquals(0, StringUtils.length(null));
    }
}