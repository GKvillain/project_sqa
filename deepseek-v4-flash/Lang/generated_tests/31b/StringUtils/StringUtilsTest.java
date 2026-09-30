package org.apache.commons.lang3;

import static org.junit.Assert.*;
import org.junit.Test;

public class StringUtilsTest {

    @Test
    public void testIsEmpty() {
        assertTrue(StringUtils.isEmpty(null));
        assertTrue(StringUtils.isEmpty(""));
        assertFalse(StringUtils.isEmpty(" "));
        assertFalse(StringUtils.isEmpty("abc"));
    }

    @Test
    public void testIsBlank() {
        assertTrue(StringUtils.isBlank(null));
        assertTrue(StringUtils.isBlank(""));
        assertTrue(StringUtils.isBlank(" "));
        assertTrue(StringUtils.isBlank("\t\n"));
        assertFalse(StringUtils.isBlank("abc"));
    }

    @Test
    public void testStrip() {
        assertNull(StringUtils.strip(null));
        assertEquals("", StringUtils.strip("   "));
        assertEquals("abc", StringUtils.strip("  abc  "));
        assertEquals("abc", StringUtils.strip("xxabcxx", "x"));
    }

    @Test
    public void testEquals() {
        assertTrue(StringUtils.equals(null, null));
        assertTrue(StringUtils.equals("abc", "abc"));
        assertFalse(StringUtils.equals("abc", null));
        assertFalse(StringUtils.equals("abc", "ABC"));
    }

    @Test
    public void testSplit() {
        assertNull(StringUtils.split(null));
        assertArrayEquals(new String[]{}, StringUtils.split(""));
        assertArrayEquals(new String[]{"abc", "def"}, StringUtils.split("abc def"));
        assertArrayEquals(new String[]{"abc", "def"}, StringUtils.split("abc   def"));
        assertArrayEquals(new String[]{"a", "b", "c"}, StringUtils.split("a.b.c", '.'));
    }

    @Test
    public void testSplitPreserveAllTokens() {
        assertArrayEquals(new String[]{"abc", "", "def"}, StringUtils.splitPreserveAllTokens("abc  def", ' '));
        assertArrayEquals(new String[]{"", "abc", ""}, StringUtils.splitPreserveAllTokens(" abc ", ' '));
    }

    @Test
    public void testSplitByWholeSeparator() {
        // null separator -> whitespace
        assertArrayEquals(new String[]{"a","b"}, StringUtils.splitByWholeSeparator("a b", null));
        // empty separator -> whitespace (current design)
        assertArrayEquals(new String[]{"a","b"}, StringUtils.splitByWholeSeparator("a b", ""));
        // normal separator
        assertArrayEquals(new String[]{"a","b"}, StringUtils.splitByWholeSeparator("a--b", "--"));
        // adjacent separators treated as one
        assertArrayEquals(new String[]{"a","b"}, StringUtils.splitByWholeSeparator("a--b", "-"));
        // with max
        assertArrayEquals(new String[]{"a", "b-c"}, StringUtils.splitByWholeSeparator("a-b-c", "-", 2));
    }

    @Test(timeout = 1000)
    public void testSplitByWholeSeparatorPreserveAllTokens() {
        // preserve empty tokens
        assertArrayEquals(new String[]{"a", "", "b"}, StringUtils.splitByWholeSeparatorPreserveAllTokens("a--b", "-"));
        // empty separator should not hang and should split by whitespace
        String[] result = StringUtils.splitByWholeSeparatorPreserveAllTokens("a b", "");
        assertNotNull(result);
        assertArrayEquals(new String[]{"a", "b"}, result);
        // null separator
        assertArrayEquals(new String[]{"a","b"}, StringUtils.splitByWholeSeparatorPreserveAllTokens("a b", null));
    }

    @Test
    public void testReplace() {
        assertEquals("zbz", StringUtils.replace("aba", "a", "z"));
        assertEquals("zba", StringUtils.replace("aba", "a", "z", 1));
        assertEquals("aba", StringUtils.replace("aba", "x", "y"));
        assertEquals(null, StringUtils.replace(null, "a", "b"));
    }

    @Test
    public void testPad() {
        // leftPad
        assertEquals("  abc", StringUtils.leftPad("abc", 5));
        assertEquals("zzabc", StringUtils.leftPad("abc", 5, 'z'));
        // rightPad
        assertEquals("abc  ", StringUtils.rightPad("abc", 5));
        assertEquals("abczz", StringUtils.rightPad("abc", 5, 'z'));
        // center
        assertEquals(" abc ", StringUtils.center("abc", 5));
        assertEquals("yayz", StringUtils.center("a", 4, "yz"));
    }

    @Test
    public void testAbbreviate() {
        assertEquals("abc...", StringUtils.abbreviate("abcdefg", 6));
        assertEquals("abcdefg", StringUtils.abbreviate("abcdefg", 7));
        assertEquals("a...", StringUtils.abbreviate("abcdefg", 4));
        assertEquals("...fghi...", StringUtils.abbreviate("abcdefghijklmno", 5, 10));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAbbreviate_throwsWhenMaxLessThan4() {
        StringUtils.abbreviate("abc", 3);
    }

    @Test
    public void testStartsEndsWith() {
        assertTrue(StringUtils.startsWith("abcdef", "abc"));
        assertFalse(StringUtils.startsWith("abcdef", "ABC"));
        assertTrue(StringUtils.startsWith(null, null));
        assertTrue(StringUtils.startsWithAny("abcxyz", new String[]{"abc"}));
        assertFalse(StringUtils.startsWithAny("abcxyz", new String[]{"xyz"}));
        
        assertTrue(StringUtils.endsWith("abcdef", "def"));
        assertFalse(StringUtils.endsWith("abcdef", "DEF"));
        assertTrue(StringUtils.endsWith(null, null));
    }

    @Test
    public void testIsAlphaNumeric() {
        assertTrue(StringUtils.isAlpha("abc"));
        assertFalse(StringUtils.isAlpha("a1"));
        assertFalse(StringUtils.isAlpha(null));
        assertTrue(StringUtils.isNumeric("123"));
        assertFalse(StringUtils.isNumeric("12.3"));
        assertFalse(StringUtils.isNumeric(null));
    }

    @Test
    public void testDefaultString() {
        assertEquals("", StringUtils.defaultString(null));
        assertEquals("abc", StringUtils.defaultString("abc"));
    }

    @Test
    public void testCountMatches() {
        assertEquals(0, StringUtils.countMatches(null, "a"));
        assertEquals(2, StringUtils.countMatches("ababa", "a"));
    }

    @Test
    public void testSubstring() {
        assertNull(StringUtils.substring(null, 0));
        assertEquals("bc", StringUtils.substring("abc", -2));
        assertEquals("ab", StringUtils.substring("abc", 0, 2));
        assertEquals("", StringUtils.substring("abc", 2, 0));
    }

    @Test
    public void testJoin() {
        assertNull(StringUtils.join((Object[]) null));
        assertEquals("", StringUtils.join(new Object[]{}));
        assertEquals("a,b,c", StringUtils.join(new Object[]{"a","b","c"}, ','));
        assertEquals("abc", StringUtils.join(new Object[]{"a","b","c"}, null));
    }

    @Test
    public void testReplaceEach() {
        assertEquals("wcte", StringUtils.replaceEach("abcde", new String[]{"ab", "d"}, new String[]{"w", "t"}));
        assertEquals("dcte", StringUtils.replaceEach("abcde", new String[]{"ab", "d"}, new String[]{"d", "t"}));
        assertEquals("tcte", StringUtils.replaceEachRepeatedly("abcde", new String[]{"ab", "d"}, new String[]{"d", "t"}));
    }

    // ========== NEW TEST CASES FOR ADDITIONAL COVERAGE ==========

    @Test
    public void testSubstringBeforeAfter() {
        assertNull(StringUtils.substringBefore(null, "."));
        assertEquals("", StringUtils.substringBefore("abc", "."));
        assertEquals("abc", StringUtils.substringBefore("abc.def", "."));
        assertEquals("", StringUtils.substringBefore("abc", "abc"));
        
        assertNull(StringUtils.substringAfter(null, "."));
        assertEquals("", StringUtils.substringAfter("abc", "."));
        assertEquals("def", StringUtils.substringAfter("abc.def", "."));
        assertEquals("", StringUtils.substringAfter("abc", "abc"));
        
        assertNull(StringUtils.substringBetween(null, "'"));
        assertEquals("", StringUtils.substringBetween("abc", "'"));
        assertEquals("b", StringUtils.substringBetween("a'b'c", "'"));
        assertEquals("b", StringUtils.substringBetween("a'b'c", "'", "'"));
        assertEquals("", StringUtils.substringBetween("abc", "x", "y"));
    }

    @Test
    public void testTrim() {
        assertNull(StringUtils.trim(null));
        assertEquals("", StringUtils.trim(""));
        assertEquals("abc", StringUtils.trim("  abc  "));
        assertEquals("ab c", StringUtils.trim(" ab c "));
    }

    @Test
    public void testRemoveStartEnd() {
        assertEquals("ef", StringUtils.removeStart("abcdef", "abcd"));
        assertEquals("abcdef", StringUtils.removeStart("abcdef", "xyz"));
        assertNull(StringUtils.removeStart(null, "a"));
        assertEquals("", StringUtils.removeStart("", "a"));
        
        assertEquals("abcd", StringUtils.removeEnd("abcdef", "ef"));
        assertEquals("abcdef", StringUtils.removeEnd("abcdef", "xy"));
        assertNull(StringUtils.removeEnd(null, "a"));
        assertEquals("", StringUtils.removeEnd("", "a"));
        
        assertEquals("ef", StringUtils.removeStartIgnoreCase("ABCDef", "abcd"));
        assertEquals("abcd", StringUtils.removeEndIgnoreCase("abcdEF", "ef"));
    }

    @Test
    public void testCapitalizeUncapitalizeSwapCase() {
        assertNull(StringUtils.capitalize(null));
        assertEquals("", StringUtils.capitalize(""));
        assertEquals("Abc", StringUtils.capitalize("abc"));
        assertEquals("Abc", StringUtils.capitalize("Abc"));
        
        assertNull(StringUtils.uncapitalize(null));
        assertEquals("", StringUtils.uncapitalize(""));
        assertEquals("abc", StringUtils.uncapitalize("Abc"));
        assertEquals("aBC", StringUtils.uncapitalize("ABC"));
        
        assertNull(StringUtils.swapCase(null));
        assertEquals("", StringUtils.swapCase(""));
        assertEquals("hELLO wORLD", StringUtils.swapCase("Hello World"));
    }

    @Test
    public void testContains() {
        assertFalse(StringUtils.contains(null, 'a'));
        assertTrue(StringUtils.contains("abc", 'a'));
        assertFalse(StringUtils.contains("abc", 'z'));
        
        assertFalse(StringUtils.contains(null, "abc"));
        assertFalse(StringUtils.contains("abc", null));
        assertTrue(StringUtils.contains("abc", "bc"));
        assertTrue(StringUtils.contains("abc", ""));
        
        assertTrue(StringUtils.containsIgnoreCase("abc", "A"));
        assertFalse(StringUtils.containsIgnoreCase("abc", "z"));
        
        assertTrue(StringUtils.containsAny("abc", 'a', 'b'));
        assertFalse(StringUtils.containsAny("abc", 'x', 'y'));
        assertFalse(StringUtils.containsAny(null, 'a'));
        assertFalse(StringUtils.containsAny("", 'a'));
        
        assertTrue(StringUtils.containsNone("abc", 'x', 'y'));
        assertFalse(StringUtils.containsNone("abc", 'a', 'x'));
        assertTrue(StringUtils.containsNone(null, 'a'));
        
        assertTrue(StringUtils.containsOnly("abc", 'a', 'b', 'c'));
        assertFalse(StringUtils.containsOnly("abc", 'a', 'b'));
        assertFalse(StringUtils.containsOnly(null, 'a'));
    }

    @Test
    public void testDeleteWhitespaceAndRemove() {
        assertNull(StringUtils.deleteWhitespace(null));
        assertEquals("", StringUtils.deleteWhitespace(""));
        assertEquals("abc", StringUtils.deleteWhitespace(" a b c "));
        assertEquals("abc", StringUtils.deleteWhitespace("a\tb\nc"));
        
        assertNull(StringUtils.remove(null, "a"));
        assertEquals("", StringUtils.remove("", "a"));
        assertEquals("bc", StringUtils.remove("abc", "a"));
        assertEquals("abc", StringUtils.remove("abc", "x"));
        
        assertEquals("bc", StringUtils.removeAll("abc", "a"));
        assertEquals("b", StringUtils.removeFirst("aba", "a"));
    }

    @Test
    public void testRepeat() {
        assertNull(StringUtils.repeat(null, 3));
        assertEquals("", StringUtils.repeat("", 3));
        assertEquals("aaa", StringUtils.repeat("a", 3));
        assertEquals("", StringUtils.repeat("a", 0));
        assertEquals("ababab", StringUtils.repeat("ab", 3));
        
        assertEquals("a,a,a", StringUtils.repeat("a", ",", 3));
    }

    @Test
    public void testChompChop() {
        assertEquals("abc", StringUtils.chomp("abc\n"));
        assertEquals("abc", StringUtils.chomp("abc\r\n"));
        assertEquals("abc", StringUtils.chomp("abc"));
        assertNull(StringUtils.chomp(null));
        
        assertEquals("ab", StringUtils.chop("abc"));
        assertEquals("", StringUtils.chop("a"));
        assertNull(StringUtils.chop(null));
    }

    @Test
    public void testDifference() {
        assertEquals("cde", StringUtils.difference("abcde", "abc"));
        assertEquals("", StringUtils.difference("abc", "abc"));
        assertEquals("fgh", StringUtils.difference("abcde", "abcfgh"));
        assertNull(StringUtils.difference(null, "abc"));
        assertEquals("abc", StringUtils.difference("", "abc"));
        
        assertEquals(3, StringUtils.indexOfDifference("abcde", "abc"));
        assertEquals(-1, StringUtils.indexOfDifference("abc", "abc"));
        assertEquals(0, StringUtils.indexOfDifference("abc", "xyz"));
        assertEquals(-1, StringUtils.indexOfDifference(null, null));
    }

    @Test
    public void testIsCaseChecks() {
        assertTrue(StringUtils.isAllLowerCase("abc"));
        assertFalse(StringUtils.isAllLowerCase("abC"));
        assertFalse(StringUtils.isAllLowerCase(null));
        assertFalse(StringUtils.isAllLowerCase(""));
        
        assertTrue(StringUtils.isAllUpperCase("ABC"));
        assertFalse(StringUtils.isAllUpperCase("AbC"));
        assertFalse(StringUtils.isAllUpperCase(null));
        assertFalse(StringUtils.isAllUpperCase(""));
        
        assertTrue(StringUtils.isMixedCase("AbC"));
        assertFalse(StringUtils.isMixedCase("abc"));
        assertFalse(StringUtils.isMixedCase("ABC"));
    }
}