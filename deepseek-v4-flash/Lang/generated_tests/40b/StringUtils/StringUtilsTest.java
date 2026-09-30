import org.junit.Test;
import static org.junit.Assert.*;

public class StringUtilsTest {

    // Tests null input for isEmpty returns true
    @Test
    public void testIsEmpty_nullInput_returnsTrue() {
        assertTrue(StringUtils.isEmpty(null));
    }

    // Tests empty string for isEmpty returns true
    @Test
    public void testIsEmpty_emptyString_returnsTrue() {
        assertTrue(StringUtils.isEmpty(""));
    }

    // Tests whitespace string for isBlank returns true
    @Test
    public void testIsBlank_whitespaceString_returnsTrue() {
        assertTrue(StringUtils.isBlank("   "));
    }

    // Tests null input for trim returns null
    @Test
    public void testTrim_nullInput_returnsNull() {
        assertNull(StringUtils.trim(null));
    }

    // Tests trim with spaces returns trimmed string
    @Test
    public void testTrim_withSpaces_returnsTrimmed() {
        assertEquals("abc", StringUtils.trim("  abc  "));
    }

    // Tests equals with both null returns true
    @Test
    public void testEquals_bothNull_returnsTrue() {
        assertTrue(StringUtils.equals(null, null));
    }

    // Tests indexOf with null string returns -1
    @Test
    public void testIndexOf_nullString_returnsMinusOne() {
        assertEquals(-1, StringUtils.indexOf(null, 'a'));
    }

    // Tests ordinalIndexOf with null input returns -1
    @Test
    public void testOrdinalIndexOf_nullInput_returnsMinusOne() {
        assertEquals(-1, StringUtils.ordinalIndexOf(null, "a", 1));
    }

    // Tests substring with negative start returns correct substring
    @Test
    public void testSubstring_negativeStart_returnsCorrect() {
        assertEquals("c", StringUtils.substring("abc", -1));
    }

    // Tests left with negative length returns empty string
    @Test
    public void testLeft_negativeLen_returnsEmpty() {
        assertEquals("", StringUtils.left("abc", -1));
    }

    // Tests mid with pos > length returns empty
    @Test
    public void testMid_posTooLarge_returnsEmpty() {
        assertEquals("", StringUtils.mid("abc", 5, 2));
    }

    // Tests substringBetween with null open returns null
    @Test
    public void testSubstringBetween_nullOpen_returnsNull() {
        assertNull(StringUtils.substringBetween("abc", null, "c"));
    }

    // Tests splitByWholeSeparatorPreserveAllTokens with adjacent separators preserves empty tokens
    @Test
    public void testSplitByWholeSeparatorPreserveAllTokens_adjacentSeparators_preservesEmptyTokens() {
        String[] result = StringUtils.splitByWholeSeparatorPreserveAllTokens("a--b", "-");
        assertArrayEquals(new String[]{"a", "", "b"}, result);
    }

    // Tests replaceEach with circular replacement throws IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testReplaceEachRepeatedly_circularReplacement_throwsIllegalStateException() {
        StringUtils.replaceEachRepeatedly("ab", new String[]{"a", "b"}, new String[]{"b", "a"});
    }

    // Tests abbreviate with maxWidth less than 4 throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testAbbreviate_maxWidthTooSmall_throwsIllegalArgumentException() {
        StringUtils.abbreviate("abc", 3);
    }

    // Tests startsWith with null prefix returns false (both null returns true)
    @Test
    public void testStartsWith_nullPrefix_returnsFalse() {
        assertFalse(StringUtils.startsWith("abc", null));
    }

    // Tests endsWith with null suffix returns false (both null returns true)
    @Test
    public void testEndsWith_nullSuffix_returnsFalse() {
        assertFalse(StringUtils.endsWith("abc", null));
    }

    // Tests getLevenshteinDistance with null string throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testGetLevenshteinDistance_nullString_throwsIllegalArgumentException() {
        StringUtils.getLevenshteinDistance(null, "abc");
    }

    // Tests replaceOnce replaces only first occurrence
    @Test
    public void testReplaceOnce_replacesOnlyFirst() {
        assertEquals("zbaa", StringUtils.replaceOnce("abaa", "a", "z"));
    }

    // Tests join with array separator returns string with separator
    @Test
    public void testJoin_arrayWithSeparator_returnsJoined() {
        assertEquals("a,b,c", StringUtils.join(new Object[]{"a", "b", "c"}, ','));
    }

    // Tests deleteWhitespace removes whitespace characters
    @Test
    public void testDeleteWhitespace_removesWhitespace() {
        assertEquals("abc", StringUtils.deleteWhitespace(" a b c "));
    }
}