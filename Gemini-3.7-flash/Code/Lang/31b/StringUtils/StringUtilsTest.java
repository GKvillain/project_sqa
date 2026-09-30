package org.apache.commons.lang3;

import org.junit.Test;
import java.util.Arrays;
import java.util.Locale;

import static org.junit.Assert.*;

public class StringUtilsTest {

    // Tests default constructor instantiation for code coverage
    @Test
    public void testConstructor_defaultInstantiation_notNull() {
        assertNotNull(new StringUtils());
    }

    // Tests defect Lang-31: Supplementary characters sharing surrogate halves should not match different code points
    @Test
    public void testContainsAny_supplementaryCharacters_returnsFalseWhenNoCodePointMatch() {
        // U+10401 (\uD801\uDC01) and U+10402 (\uD801\uDC02) share high surrogate \uD801 but are different code points
        String bChar = "\uD801\uDC01";
        String cChar = "\uD801\uDC02";

        assertFalse(StringUtils.containsAny(bChar, cChar));
        assertFalse(StringUtils.containsAny(bChar, new char[]{'\uD801', '\uDC02'}));
        assertFalse(StringUtils.containsAny(bChar, new char[]{'\uD801'}));
        assertTrue(StringUtils.containsAny(bChar, bChar));
        assertTrue(StringUtils.containsAny(bChar, new char[]{'\uD801', '\uDC01'}));
    }

    // Tests isEmpty and isNotEmpty with null, empty, blank, and non-empty inputs
    @Test
    public void testIsEmptyAndIsNotEmpty_variousInputs_returnsExpected() {
        assertTrue(StringUtils.isEmpty(null));
        assertTrue(StringUtils.isEmpty(""));
        assertFalse(StringUtils.isEmpty(" "));
        assertFalse(StringUtils.isEmpty("abc"));

        assertFalse(StringUtils.isNotEmpty(null));
        assertFalse(StringUtils.isNotEmpty(""));
        assertTrue(StringUtils.isNotEmpty(" "));
        assertTrue(StringUtils.isNotEmpty("abc"));
    }

    // Tests isBlank and isNotBlank with null, empty, whitespace, and non-blank inputs
    @Test
    public void testIsBlankAndIsNotBlank_variousInputs_returnsExpected() {
        assertTrue(StringUtils.isBlank(null));
        assertTrue(StringUtils.isBlank(""));
        assertTrue(StringUtils.isBlank(" \t\r\n "));
        assertFalse(StringUtils.isBlank("  a  "));

        assertFalse(StringUtils.isNotBlank(null));
        assertFalse(StringUtils.isNotBlank(""));
        assertFalse(StringUtils.isNotBlank(" \t\r\n "));
        assertTrue(StringUtils.isNotBlank("  a  "));
    }

    // Tests trim, trimToNull, and trimToEmpty with various inputs
    @Test
    public void testTrimVariants_variousInputs_returnsExpected() {
        assertNull(StringUtils.trim(null));
        assertEquals("", StringUtils.trim(""));
        assertEquals("", StringUtils.trim("   \t  "));
        assertEquals("abc", StringUtils.trim("  abc  "));

        assertNull(StringUtils.trimToNull(null));
        assertNull(StringUtils.trimToNull("   "));
        assertEquals("abc", StringUtils.trimToNull("  abc  "));

        assertEquals("", StringUtils.trimToEmpty(null));
        assertEquals("", StringUtils.trimToEmpty("   "));
        assertEquals("abc", StringUtils.trimToEmpty("  abc  "));
    }

    // Tests strip, stripStart, stripEnd, and stripAll
    @Test
    public void testStripVariants_variousInputs_returnsExpected() {
        assertNull(StringUtils.strip(null));
        assertEquals("", StringUtils.strip("   "));
        assertEquals("abc", StringUtils.strip("  abc  "));
        assertEquals("abc", StringUtils.strip("yyyabcyyy", "y"));

        assertEquals("abc  ", StringUtils.stripStart("  abc  ", null));
        assertEquals("abc", StringUtils.stripStart("xyzabc", "xyz"));

        assertEquals("  abc", StringUtils.stripEnd("  abc  ", null));
        assertEquals("abc", StringUtils.stripEnd("abcxyz", "xyz"));

        assertNull(StringUtils.stripAll(null));
        assertArrayEquals(new String[]{}, StringUtils.stripAll(new String[]{}));
        assertArrayEquals(new String[]{"a", "b", null}, StringUtils.stripAll(new String[]{" a ", "  b", null}));
    }

    // Tests equals and equalsIgnoreCase
    @Test
    public void testEqualsAndEqualsIgnoreCase_variousInputs_returnsExpected() {
        assertTrue(StringUtils.equals(null, null));
        assertFalse(StringUtils.equals(null, "abc"));
        assertFalse(StringUtils.equals("abc", null));
        assertTrue(StringUtils.equals("abc", "abc"));
        assertFalse(StringUtils.equals("abc", "ABC"));

        assertTrue(StringUtils.equalsIgnoreCase(null, null));
        assertFalse(StringUtils.equalsIgnoreCase(null, "abc"));
        assertTrue(StringUtils.equalsIgnoreCase("abc", "ABC"));
        assertFalse(StringUtils.equalsIgnoreCase("abc", "abd"));
    }

    // Tests indexOf, indexOfIgnoreCase, lastIndexOf, and ordinalIndexOf
    @Test
    public void testIndexOfAndLastIndexOf_variousInputs_returnsExpected() {
        assertEquals(-1, StringUtils.indexOf(null, 'a'));
        assertEquals(-1, StringUtils.indexOf("", 'a'));
        assertEquals(0, StringUtils.indexOf("aabaabaa", 'a'));
        assertEquals(2, StringUtils.indexOf("aabaabaa", 'b', 0));
        assertEquals(5, StringUtils.indexOf("aabaabaa", 'b', 3));
        assertEquals(-1, StringUtils.indexOf("aabaabaa", 'b', 9));

        assertEquals(2, StringUtils.indexOfIgnoreCase("aabaabaa", "B"));
        assertEquals(5, StringUtils.indexOfIgnoreCase("aabaabaa", "B", 3));

        assertEquals(7, StringUtils.lastIndexOf("aabaabaa", 'a'));
        assertEquals(5, StringUtils.lastIndexOf("aabaabaa", "b"));
        assertEquals(2, StringUtils.lastIndexOf("aabaabaa", "b", 4));

        assertEquals(0, StringUtils.ordinalIndexOf("aabaabaa", "a", 1));
        assertEquals(1, StringUtils.ordinalIndexOf("aabaabaa", "a", 2));
        assertEquals(7, StringUtils.lastOrdinalIndexOf("aabaabaa", "a", 1));
    }

    // Tests contains, containsIgnoreCase, containsOnly, and containsNone
    @Test
    public void testContainsVariants_variousInputs_returnsExpected() {
        assertFalse(StringUtils.contains(null, 'a'));
        assertFalse(StringUtils.contains("", "a"));
        assertTrue(StringUtils.contains("abc", "b"));
        assertFalse(StringUtils.contains("abc", "z"));

        assertTrue(StringUtils.containsIgnoreCase("abc", "B"));
        assertFalse(StringUtils.containsIgnoreCase("abc", "Z"));

        assertTrue(StringUtils.containsOnly("", "abc"));
        assertTrue(StringUtils.containsOnly("aba", "ab"));
        assertFalse(StringUtils.containsOnly("abac", "ab"));

        assertTrue(StringUtils.containsNone("abc", "xyz"));
        assertFalse(StringUtils.containsNone("abc", "cde"));
    }

    // Tests indexOfAny, containsAny, and indexOfAnyBut for basic inputs
    @Test
    public void testIndexOfAnyAndContainsAny_basicPlane_returnsExpected() {
        assertEquals(-1, StringUtils.indexOfAny((String) null, "abc"));
        assertEquals(0, StringUtils.indexOfAny("zzabyycdxx", "za"));
        assertEquals(3, StringUtils.indexOfAny("zzabyycdxx", "by"));
        assertEquals(-1, StringUtils.indexOfAny("aba", "z"));

        assertTrue(StringUtils.containsAny("zzabyycdxx", "za"));
        assertTrue(StringUtils.containsAny("zzabyycdxx", new char[]{'b', 'y'}));
        assertFalse(StringUtils.containsAny("aba", "z"));

        assertEquals(3, StringUtils.indexOfAnyBut("zzabyycdxx", "za"));
        assertEquals(-1, StringUtils.indexOfAnyBut("aba", "ab"));
    }

    // Tests substring, substringBefore, substringAfter, and substringBetween
    @Test
    public void testSubstringVariants_variousInputs_returnsExpected() {
        assertNull(StringUtils.substring(null, 0));
        assertEquals("bc", StringUtils.substring("abc", 1));
        assertEquals("ab", StringUtils.substring("abc", 0, 2));
        assertEquals("bc", StringUtils.substring("abc", -2));
        assertEquals("b", StringUtils.substring("abc", -2, -1));

        assertEquals("a", StringUtils.substringBefore("abcba", "b"));
        assertEquals("cba", StringUtils.substringAfter("abcba", "b"));
        assertEquals("abc", StringUtils.substringBeforeLast("abcba", "b"));
        assertEquals("a", StringUtils.substringAfterLast("abcba", "b"));

        assertEquals("b", StringUtils.substringBetween("wx[b]yz", "[", "]"));
        assertArrayEquals(new String[]{"a", "b"}, StringUtils.substringsBetween("[a][b]", "[", "]"));
    }

    // Tests split variants
    @Test
    public void testSplitVariants_variousInputs_returnsExpected() {
        assertNull(StringUtils.split(null));
        assertArrayEquals(new String[]{}, StringUtils.split(""));
        assertArrayEquals(new String[]{"abc", "def"}, StringUtils.split("abc def"));
        assertArrayEquals(new String[]{"a", "b", "c"}, StringUtils.split("a.b.c", '.'));
        assertArrayEquals(new String[]{"ab", "cd:ef"}, StringUtils.split("ab:cd:ef", ":", 2));

        assertArrayEquals(new String[]{"a", "", "b", "c"}, StringUtils.splitPreserveAllTokens("a..b.c", '.'));
        assertArrayEquals(new String[]{"ab", "cd", "ef"}, StringUtils.splitByWholeSeparator("ab-!-cd-!-ef", "-!-"));
        assertArrayEquals(new String[]{"ab", "", "cd"}, StringUtils.splitByWholeSeparatorPreserveAllTokens("ab-!--!-cd", "-!-"));
    }

    // Tests join variants with array, iterator, and iterable
    @Test
    public void testJoinVariants_variousInputs_returnsExpected() {
        assertNull(StringUtils.join((Object[]) null));
        assertEquals("", StringUtils.join(new Object[]{}));
        assertEquals("a;b;c", StringUtils.join(new Object[]{"a", "b", "c"}, ';'));
        assertEquals("a--b--c", StringUtils.join(new Object[]{"a", "b", "c"}, "--"));
        assertEquals("a,b,c", StringUtils.join(Arrays.asList("a", "b", "c"), ","));
        assertEquals("abc", StringUtils.join(Arrays.asList("a", "b", "c").iterator(), null));
    }

    // Tests replace, replaceOnce, and replaceChars
    @Test
    public void testReplaceVariants_variousInputs_returnsExpected() {
        assertNull(StringUtils.replace(null, "a", "z"));
        assertEquals("zbz", StringUtils.replace("aba", "a", "z"));
        assertEquals("zba", StringUtils.replaceOnce("aba", "a", "z"));
        assertEquals("zbza", StringUtils.replace("abaa", "a", "z", 2));

        assertEquals("aycya", StringUtils.replaceChars("abcba", 'b', 'y'));
        assertEquals("ayzya", StringUtils.replaceChars("abcba", "bc", "yz"));
        assertEquals("ac", StringUtils.replaceChars("abc", "b", ""));
    }

    // Tests replaceEach and replaceEachRepeatedly
    @Test
    public void testReplaceEach_variousInputs_returnsExpected() {
        assertNull(StringUtils.replaceEach(null, new String[]{"a"}, new String[]{"b"}));
        assertEquals("aba", StringUtils.replaceEach("aba", null, null));
        assertEquals("wcte", StringUtils.replaceEach("abcde", new String[]{"ab", "d"}, new String[]{"w", "t"}));
        assertEquals("dcte", StringUtils.replaceEach("abcde", new String[]{"ab", "d"}, new String[]{"d", "t"}));
        assertEquals("tcte", StringUtils.replaceEachRepeatedly("abcde", new String[]{"ab", "d"}, new String[]{"d", "t"}));
    }

    // Tests replaceEach with mismatched array lengths throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testReplaceEach_mismatchedArrays_throwsIllegalArgumentException() {
        StringUtils.replaceEach("abc", new String[]{"a", "b"}, new String[]{"z"});
    }

    // Tests remove, removeStart, and removeEnd
    @Test
    public void testRemoveVariants_variousInputs_returnsExpected() {
        assertEquals("qd", StringUtils.remove("queued", "ue"));
        assertEquals("qeed", StringUtils.remove("queued", 'u'));
        assertEquals("domain.com", StringUtils.removeStart("www.domain.com", "www."));
        assertEquals("www.domain", StringUtils.removeEnd("www.domain.com", ".com"));
        assertEquals("domain.com", StringUtils.removeStartIgnoreCase("WWW.domain.com", "www."));
        assertEquals("www.domain", StringUtils.removeEndIgnoreCase("www.domain.COM", ".com"));
    }

    // Tests leftPad, rightPad, center, and repeat
    @Test
    public void testPaddingAndRepeat_variousInputs_returnsExpected() {
        assertEquals("  bat", StringUtils.leftPad("bat", 5));
        assertEquals("zzbat", StringUtils.leftPad("bat", 5, 'z'));
        assertEquals("yzbat", StringUtils.leftPad("bat", 5, "yz"));

        assertEquals("bat  ", StringUtils.rightPad("bat", 5));
        assertEquals("batzz", StringUtils.rightPad("bat", 5, 'z'));
        assertEquals("batyz", StringUtils.rightPad("bat", 5, "yz"));

        assertEquals(" ab ", StringUtils.center("ab", 4));
        assertEquals("yayy", StringUtils.center("a", 4, 'y'));
        assertEquals("yayz", StringUtils.center("a", 4, "yz"));

        assertEquals("aaa", StringUtils.repeat("a", 3));
        assertEquals("abab", StringUtils.repeat("ab", 2));
        assertEquals("?, ?, ?", StringUtils.repeat("?", ", ", 3));
    }

    // Tests abbreviate, abbreviateMiddle, chomp, and chop
    @Test
    public void testAbbreviateAndChompVariants_variousInputs_returnsExpected() {
        assertEquals("abc...", StringUtils.abbreviate("abcdefg", 6));
        assertEquals("...fghi...", StringUtils.abbreviate("abcdefghijklmno", 5, 10));
        assertEquals("ab.f", StringUtils.abbreviateMiddle("abcdef", ".", 4));

        assertEquals("abc", StringUtils.chomp("abc\r\n"));
        assertEquals("abc", StringUtils.chomp("abc\n"));
        assertEquals("foo", StringUtils.chomp("foobar", "bar"));

        assertEquals("ab", StringUtils.chop("abc"));
        assertEquals("abc", StringUtils.chop("abc\r\n"));
    }

    // Tests abbreviate with invalid width throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testAbbreviate_widthLessThan4_throwsIllegalArgumentException() {
        StringUtils.abbreviate("abcdefg", 3);
    }

    // Tests difference, indexOfDifference, getCommonPrefix, and getLevenshteinDistance
    @Test
    public void testDifferenceAndLevenshtein_variousInputs_returnsExpected() {
        assertEquals("xyz", StringUtils.difference("abcde", "abxyz"));
        assertEquals(2, StringUtils.indexOfDifference("ab", "abxyz"));
        assertEquals(7, StringUtils.indexOfDifference(new String[]{"i am a machine", "i am a robot"}));
        assertEquals("i am a ", StringUtils.getCommonPrefix(new String[]{"i am a machine", "i am a robot"}));

        assertEquals(0, StringUtils.getLevenshteinDistance("", ""));
        assertEquals(1, StringUtils.getLevenshteinDistance("frog", "fog"));
        assertEquals(3, StringUtils.getLevenshteinDistance("fly", "ant"));
        assertEquals(7, StringUtils.getLevenshteinDistance("elephant", "hippo"));
    }

    // Tests character checks: isAlpha, isAlphanumeric, isNumeric, isWhitespace
    @Test
    public void testCharacterChecks_variousInputs_returnsExpected() {
        assertTrue(StringUtils.isAlpha("abc"));
        assertFalse(StringUtils.isAlpha("ab1c"));

        assertTrue(StringUtils.isAlphaSpace("ab c"));
        assertFalse(StringUtils.isAlphaSpace("ab1 c"));

        assertTrue(StringUtils.isAlphanumeric("ab1c"));
        assertFalse(StringUtils.isAlphanumeric("ab-1c"));

        assertTrue(StringUtils.isNumeric("123"));
        assertFalse(StringUtils.isNumeric("12.3"));

        assertTrue(StringUtils.isWhitespace(" \t\r\n "));
        assertFalse(StringUtils.isWhitespace("  a  "));

        assertTrue(StringUtils.isAllLowerCase("abc"));
        assertFalse(StringUtils.isAllLowerCase("abC"));

        assertTrue(StringUtils.isAllUpperCase("ABC"));
        assertFalse(StringUtils.isAllUpperCase("ABc"));
    }

    // Tests defaultString, defaultIfEmpty, and defaultIfBlank
    @Test
    public void testDefaultStringVariants_variousInputs_returnsExpected() {
        assertEquals("", StringUtils.defaultString(null));
        assertEquals("abc", StringUtils.defaultString("abc"));
        assertEquals("NULL", StringUtils.defaultString(null, "NULL"));
        assertEquals("abc", StringUtils.defaultString("abc", "NULL"));

        assertEquals("NULL", StringUtils.defaultIfEmpty(null, "NULL"));
        assertEquals("NULL", StringUtils.defaultIfEmpty("", "NULL"));
        assertEquals(" ", StringUtils.defaultIfEmpty(" ", "NULL"));
        assertEquals("abc", StringUtils.defaultIfEmpty("abc", "NULL"));

        assertEquals("NULL", StringUtils.defaultIfBlank(null, "NULL"));
        assertEquals("NULL", StringUtils.defaultIfBlank("", "NULL"));
        assertEquals("NULL", StringUtils.defaultIfBlank("   ", "NULL"));
        assertEquals("abc", StringUtils.defaultIfBlank("abc", "NULL"));
    }

    // Tests startsWith, startsWithIgnoreCase, and startsWithAny
    @Test
    public void testStartsWithVariants_variousInputs_returnsExpected() {
        assertTrue(StringUtils.startsWith(null, null));
        assertFalse(StringUtils.startsWith(null, "abc"));
        assertFalse(StringUtils.startsWith("abcdef", null));
        assertTrue(StringUtils.startsWith("abcdef", "abc"));
        assertFalse(StringUtils.startsWith("abcdef", "ABC"));

        assertTrue(StringUtils.startsWithIgnoreCase(null, null));
        assertFalse(StringUtils.startsWithIgnoreCase(null, "abc"));
        assertTrue(StringUtils.startsWithIgnoreCase("abcdef", "ABC"));
        assertFalse(StringUtils.startsWithIgnoreCase("abcdef", "xyz"));

        assertFalse(StringUtils.startsWithAny(null, "abc", "def"));
        assertFalse(StringUtils.startsWithAny("abcdef", (String[]) null));
        assertTrue(StringUtils.startsWithAny("abcdef", "abc", "xyz"));
        assertFalse(StringUtils.startsWithAny("abcdef", "def", "xyz"));
    }

    // Tests endsWith, endsWithIgnoreCase, and endsWithAny
    @Test
    public void testEndsWithVariants_variousInputs_returnsExpected() {
        assertTrue(StringUtils.endsWith(null, null));
        assertFalse(StringUtils.endsWith(null, "def"));
        assertFalse(StringUtils.endsWith("abcdef", null));
        assertTrue(StringUtils.endsWith("abcdef", "def"));
        assertFalse(StringUtils.endsWith("abcdef", "DEF"));

        assertTrue(StringUtils.endsWithIgnoreCase(null, null));
        assertFalse(StringUtils.endsWithIgnoreCase(null, "def"));
        assertTrue(StringUtils.endsWithIgnoreCase("abcdef", "DEF"));
        assertFalse(StringUtils.endsWithIgnoreCase("abcdef", "xyz"));

        assertFalse(StringUtils.endsWithAny(null, "abc", "def"));
        assertFalse(StringUtils.endsWithAny("abcdef", (String[]) null));
        assertTrue(StringUtils.endsWithAny("abcdef", "def", "xyz"));
        assertFalse(StringUtils.endsWithAny("abcdef", "abc", "xyz"));
    }

    // Tests case modification: capitalize, uncapitalize, swapCase, upperCase, lowerCase
    @Test
    public void testCaseModifications_variousInputs_returnsExpected() {
        assertNull(StringUtils.capitalize(null));
        assertEquals("Cat", StringUtils.capitalize("cat"));
        assertEquals("Cat", StringUtils.capitalize("Cat"));

        assertNull(StringUtils.uncapitalize(null));
        assertEquals("cat", StringUtils.uncapitalize("Cat"));
        assertEquals("cat", StringUtils.uncapitalize("cat"));

        assertNull(StringUtils.swapCase(null));
        assertEquals("", StringUtils.swapCase(""));
        assertEquals("tHe dOG", StringUtils.swapCase("The Dog"));

        assertNull(StringUtils.upperCase(null));
        assertEquals("ABC", StringUtils.upperCase("abc"));
        assertEquals("ABC", StringUtils.upperCase("abc", Locale.ENGLISH));

        assertNull(StringUtils.lowerCase(null));
        assertEquals("abc", StringUtils.lowerCase("ABC"));
        assertEquals("abc", StringUtils.lowerCase("ABC", Locale.ENGLISH));
    }

    // Tests countMatches and containsWhitespace
    @Test
    public void testCountMatchesAndContainsWhitespace_variousInputs_returnsExpected() {
        assertEquals(0, StringUtils.countMatches(null, "a"));
        assertEquals(0, StringUtils.countMatches("abba", null));
        assertEquals(0, StringUtils.countMatches("abba", ""));
        assertEquals(2, StringUtils.countMatches("abba", "a"));
        assertEquals(1, StringUtils.countMatches("abba", "bb"));

        assertFalse(StringUtils.containsWhitespace(null));
        assertFalse(StringUtils.containsWhitespace(""));
        assertFalse(StringUtils.containsWhitespace("abc"));
        assertTrue(StringUtils.containsWhitespace("a b c"));
        assertTrue(StringUtils.containsWhitespace("a\tb\nc"));
    }

    // Tests reverse and reverseDelimited
    @Test
    public void testReverseVariants_variousInputs_returnsExpected() {
        assertNull(StringUtils.reverse(null));
        assertEquals("", StringUtils.reverse(""));
        assertEquals("bat", StringUtils.reverse("tab"));

        assertNull(StringUtils.reverseDelimited(null, '.'));
        assertEquals("", StringUtils.reverseDelimited("", '.'));
        assertEquals("c.b.a", StringUtils.reverseDelimited("a.b.c", '.'));
    }

    // Tests left, right, mid, and overlay
    @Test
    public void testSubstrAndOverlay_variousInputs_returnsExpected() {
        assertNull(StringUtils.left(null, 2));
        assertEquals("", StringUtils.left("abc", -1));
        assertEquals("ab", StringUtils.left("abc", 2));
        assertEquals("abc", StringUtils.left("abc", 4));

        assertNull(StringUtils.right(null, 2));
        assertEquals("", StringUtils.right("abc", -1));
        assertEquals("bc", StringUtils.right("abc", 2));
        assertEquals("abc", StringUtils.right("abc", 4));

        assertNull(StringUtils.mid(null, 0, 2));
        assertEquals("", StringUtils.mid("abc", 0, -1));
        assertEquals("b", StringUtils.mid("abc", 1, 1));
        assertEquals("bc", StringUtils.mid("abc", 1, 5));
        assertEquals("", StringUtils.mid("abc", 5, 2));

        assertNull(StringUtils.overlay(null, "zz", 1, 2));
        assertEquals("abef", StringUtils.overlay("abcdef", "", 2, 4));
        assertEquals("abzzef", StringUtils.overlay("abcdef", "zz", 2, 4));
        assertEquals("abcdefzz", StringUtils.overlay("abcdef", "zz", 8, 10));
    }

    // Tests isAsciiPrintable and normalizeSpace
    @Test
    public void testAsciiPrintableAndNormalizeSpace_variousInputs_returnsExpected() {
        assertFalse(StringUtils.isAsciiPrintable(null));
        assertTrue(StringUtils.isAsciiPrintable(""));
        assertTrue(StringUtils.isAsciiPrintable(" !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~"));
        assertFalse(StringUtils.isAsciiPrintable("Caff\u00e9"));

        assertNull(StringUtils.normalizeSpace(null));
        assertEquals("", StringUtils.normalizeSpace(""));
        assertEquals("a b c", StringUtils.normalizeSpace("  a \t  b \r\n c  "));
    }

    // Tests splitByCharacterType and splitByCharacterTypeCamelCase
    @Test
    public void testSplitByCharacterTypeVariants_variousInputs_returnsExpected() {
        assertNull(StringUtils.splitByCharacterType(null));
        assertArrayEquals(new String[]{}, StringUtils.splitByCharacterType(""));
        assertArrayEquals(new String[]{"ab", " ", "cd"}, StringUtils.splitByCharacterType("ab cd"));
        assertArrayEquals(new String[]{"ab", "12", "cd"}, StringUtils.splitByCharacterType("ab12cd"));

        assertNull(StringUtils.splitByCharacterTypeCamelCase(null));
        assertArrayEquals(new String[]{"Foo", "Bar"}, StringUtils.splitByCharacterTypeCamelCase("FooBar"));
        assertArrayEquals(new String[]{"FOO", "Bar"}, StringUtils.splitByCharacterTypeCamelCase("FOOBar"));
    }

    // Tests join with primitive arrays and range
    @Test
    public void testJoinPrimitivesAndRange_variousInputs_returnsExpected() {
        assertNull(StringUtils.join((byte[]) null, ','));
        assertEquals("1,2,3", StringUtils.join(new byte[]{1, 2, 3}, ','));

        assertNull(StringUtils.join((short[]) null, ','));
        assertEquals("1,2,3", StringUtils.join(new short[]{1, 2, 3}, ','));

        assertNull(StringUtils.join((int[]) null, ','));
        assertEquals("1,2,3", StringUtils.join(new int[]{1, 2, 3}, ','));

        assertNull(StringUtils.join((long[]) null, ','));
        assertEquals("1,2,3", StringUtils.join(new long[]{1L, 2L, 3L}, ','));

        assertNull(StringUtils.join((float[]) null, ','));
        assertEquals("1.0,2.0", StringUtils.join(new float[]{1.0f, 2.0f}, ','));

        assertNull(StringUtils.join((double[]) null, ','));
        assertEquals("1.0,2.0", StringUtils.join(new double[]{1.0, 2.0}, ','));

        assertNull(StringUtils.join((char[]) null, ','));
        assertEquals("a,b,c", StringUtils.join(new char[]{'a', 'b', 'c'}, ','));

        assertEquals("b,c", StringUtils.join(new Object[]{"a", "b", "c", "d"}, ',', 1, 3));
        assertEquals("b--c", StringUtils.join(new Object[]{"a", "b", "c", "d"}, "--", 1, 3));
    }

    // Tests getLevenshteinDistance with threshold
    @Test
    public void testGetLevenshteinDistanceWithThreshold_variousInputs_returnsExpected() {
        assertEquals(0, StringUtils.getLevenshteinDistance("", "", 0));
        assertEquals(1, StringUtils.getLevenshteinDistance("frog", "fog", 1));
        assertEquals(-1, StringUtils.getLevenshteinDistance("frog", "fog", 0));
        assertEquals(2, StringUtils.getLevenshteinDistance("frog", "fogs", 2));
    }
}