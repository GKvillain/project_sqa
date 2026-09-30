package org.jsoup.helper;

import org.junit.Test;
import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * JUnit 4 test class for StringUtil from jsoup (Defects4J bug 74b).
 * Designed to detect defects, cover important branches, and achieve reasonable coverage.
 */
public class StringUtilTest {

    // --- join(Collection) ---

    @Test
    public void testJoin_CollectionMultipleStrings_returnsJoinedWithSeparator() {
        Collection<String> coll = Arrays.asList("a", "b", "c");
        assertEquals("a,b,c", StringUtil.join(coll, ","));
    }

    @Test
    public void testJoin_CollectionEmpty_returnsEmptyString() {
        Collection<String> coll = new ArrayList<>();
        assertEquals("", StringUtil.join(coll, ","));
    }

    // --- join(Iterator) ---

    @Test
    public void testJoin_IteratorSingleElement_returnsElementString() {
        List<String> list = Arrays.asList("only");
        assertEquals("only", StringUtil.join(list.iterator(), ","));
    }

    @Test
    public void testJoin_IteratorEmpty_returnsEmptyString() {
        List<String> list = new ArrayList<>();
        assertEquals("", StringUtil.join(list.iterator(), ","));
    }

    // --- padding(int) ---

    @Test
    public void testPadding_widthZero_returnsEmptyString() {
        assertEquals("", StringUtil.padding(0));
    }

    @Test
    public void testPadding_widthOne_returnsSingleSpace() {
        assertEquals(" ", StringUtil.padding(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPadding_widthNegative_throwsIllegalArgumentException() {
        StringUtil.padding(-1);
    }

    @Test
    public void testPadding_widthLarge_returnsCorrectSpaces() {
        // width larger than memoised padding length (21)
        int width = 25;
        String expected = "                         "; // 25 spaces
        assertEquals(expected, StringUtil.padding(width));
    }

    // --- isBlank(String) ---

    @Test
    public void testIsBlank_null_returnsTrue() {
        assertTrue(StringUtil.isBlank(null));
    }

    @Test
    public void testIsBlank_empty_returnsTrue() {
        assertTrue(StringUtil.isBlank(""));
    }

    @Test
    public void testIsBlank_onlyWhitespace_returnsTrue() {
        assertTrue(StringUtil.isBlank(" \t\n\r\f"));
    }

    @Test
    public void testIsBlank_nonWhitespace_returnsFalse() {
        assertFalse(StringUtil.isBlank(" a "));
    }

    @Test
    public void testIsBlank_whitespaceThenNonWhitespace_returnsFalse() {
        assertFalse(StringUtil.isBlank("  x"));
    }

    // --- isNumeric(String) ---

    @Test
    public void testIsNumeric_null_returnsFalse() {
        assertFalse(StringUtil.isNumeric(null));
    }

    @Test
    public void testIsNumeric_empty_returnsFalse() {
        assertFalse(StringUtil.isNumeric(""));
    }

    @Test
    public void testIsNumeric_digits_returnsTrue() {
        assertTrue(StringUtil.isNumeric("12345"));
    }

    @Test
    public void testIsNumeric_mixed_returnsFalse() {
        assertFalse(StringUtil.isNumeric("123a45"));
    }

    @Test
    public void testIsNumeric_negative_returnsFalse() {
        assertFalse(StringUtil.isNumeric("-123"));
    }

    // --- normaliseWhitespace(String) ---

    @Test
    public void testNormaliseWhitespace_multipleWhitespaceCharacters_collapsesToSingleSpace() {
        assertEquals("hello world", StringUtil.normaliseWhitespace("hello   \t\n\r\f world"));
    }

    @Test
    public void testNormaliseWhitespace_leadingAndTrailingWhitespace_keepsSingleSpaceEdge() {
        // stripLeading is false in normaliseWhitespace so leading space remains
        assertEquals(" hello world ", StringUtil.normaliseWhitespace("  hello world  "));
    }

    @Test
    public void testNormaliseWhitespace_stringWithNonBreakingSpace_replacesWithSpace() {
        String input = "foo\u00A0bar"; // &nbsp;
        assertEquals("foo bar", StringUtil.normaliseWhitespace(input));
    }

    // --- appendNormalisedWhitespace(StringBuilder, String, boolean) ---

    @Test
    public void testAppendNormalisedWhitespace_stripLeadingTrue_removesLeadingWhitespace() {
        StringBuilder sb = new StringBuilder();
        StringUtil.appendNormalisedWhitespace(sb, "   hello", true);
        assertEquals("hello", sb.toString());
    }

    @Test
    public void testAppendNormalisedWhitespace_stripLeadingFalse_keepsLeadingSpace() {
        StringBuilder sb = new StringBuilder();
        StringUtil.appendNormalisedWhitespace(sb, "   hello", false);
        assertEquals(" hello", sb.toString());
    }

    // --- in(String, String...) ---

    @Test
    public void testIn_needlePresentAmongHaystack_returnsTrue() {
        assertTrue(StringUtil.in("b", "a", "b", "c"));
    }

    @Test
    public void testIn_needleAbsent_returnsFalse() {
        assertFalse(StringUtil.in("d", "a", "b", "c"));
    }

    // --- inSorted(String, String[]) ---

    @Test
    public void testInSorted_needlePresent_returnsTrue() {
        String[] sorted = {"a", "b", "c"};
        assertTrue(StringUtil.inSorted("b", sorted));
    }

    @Test
    public void testInSorted_needleAbsent_returnsFalse() {
        String[] sorted = {"a", "b", "c"};
        assertFalse(StringUtil.inSorted("d", sorted));
    }

    // --- resolve(String, String) ---

    @Test
    public void testResolve_absoluteBaseAndRelativePath_returnsAbsoluteUrl() throws Exception {
        String base = "http://example.com/path/file.html";
        String rel = "subdir/other.html";
        String result = StringUtil.resolve(base, rel);
        assertEquals("http://example.com/path/subdir/other.html", result);
    }

    @Test
    public void testResolve_relativeStartsWithQuestionMark_usesBasePath() throws Exception {
        String base = "http://example.com/path/file.html";
        String rel = "?query=1";
        String result = StringUtil.resolve(base, rel);
        assertEquals("http://example.com/path/file.html?query=1", result);
    }

    @Test
    public void testResolve_invalidBase_returnsEmptyString() {
        String base = "not a url";
        String rel = "anything";
        String result = StringUtil.resolve(base, rel);
        assertEquals("", result);
    }

    // --- stringBuilder() ---

    @Test
    public void testStringBuilder_returnsEmptyBuilder() {
        StringBuilder sb = StringUtil.stringBuilder();
        assertNotNull(sb);
        assertEquals(0, sb.length());
    }

    @Test
    public void testStringBuilder_afterLargeAppend_resetsToEmpty() {
        // fill with content exceeding max cached size (8*1024)
        StringBuilder sb = StringUtil.stringBuilder();
        for (int i = 0; i < 9000; i++) {
            sb.append('x');
        }
        // get another builder – should be new or reset
        StringBuilder sb2 = StringUtil.stringBuilder();
        assertEquals(0, sb2.length());
    }
}