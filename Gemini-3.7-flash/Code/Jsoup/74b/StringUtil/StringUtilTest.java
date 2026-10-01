package org.jsoup.helper;

import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StringUtilTest {

    // Tests join with Collection input
    @Test
    public void testJoin_collectionWithMultipleElements_returnsJoinedString() {
        assertEquals("a, b, c", StringUtil.join(Arrays.asList("a", "b", "c"), ", "));
    }

    // Tests join with empty collection
    @Test
    public void testJoin_emptyCollection_returnsEmptyString() {
        assertEquals("", StringUtil.join(Collections.emptyList(), ", "));
    }

    // Tests join with single element collection (avoids StringBuilder branch)
    @Test
    public void testJoin_singleElementCollection_returnsSingleString() {
        assertEquals("one", StringUtil.join(Collections.singletonList("one"), ", "));
    }

    // Tests join with array input
    @Test
    public void testJoin_stringArray_returnsJoinedString() {
        assertEquals("foo-bar", StringUtil.join(new String[]{"foo", "bar"}, "-"));
    }

    // Tests padding with memoised boundary width (< 21)
    @Test
    public void testPadding_memoisedWidth_returnsCachedPadding() {
        assertEquals("", StringUtil.padding(0));
        assertEquals("   ", StringUtil.padding(3));
        assertEquals("                    ", StringUtil.padding(20));
    }

    // Tests padding with non-memoised width (>= 21)
    @Test
    public void testPadding_largeWidth_returnsGeneratedPadding() {
        assertEquals("                     ", StringUtil.padding(21));
        assertEquals("                      ", StringUtil.padding(22));
    }

    // Tests padding with negative width throwing exception
    @Test(expected = IllegalArgumentException.class)
    public void testPadding_negativeWidth_throwsIllegalArgumentException() {
        StringUtil.padding(-1);
    }

    // Tests isBlank with null and empty string
    @Test
    public void testIsBlank_nullOrEmptyString_returnsTrue() {
        assertTrue(StringUtil.isBlank(null));
        assertTrue(StringUtil.isBlank(""));
    }

    // Tests isBlank with only whitespace characters
    @Test
    public void testIsBlank_onlyWhitespace_returnsTrue() {
        assertTrue(StringUtil.isBlank("   \t \r \n \f "));
    }

    // Tests isBlank with non-whitespace characters
    @Test
    public void testIsBlank_nonWhitespace_returnsFalse() {
        assertFalse(StringUtil.isBlank("   a   "));
    }

    // Tests isNumeric with null and empty string
    @Test
    public void testIsNumeric_nullOrEmpty_returnsFalse() {
        assertFalse(StringUtil.isNumeric(null));
        assertFalse(StringUtil.isNumeric(""));
    }

    // Tests isNumeric with valid digits
    @Test
    public void testIsNumeric_onlyDigits_returnsTrue() {
        assertTrue(StringUtil.isNumeric("1234567890"));
    }

    // Tests isNumeric with non-digit characters
    @Test
    public void testIsNumeric_containsNonDigits_returnsFalse() {
        assertFalse(StringUtil.isNumeric("123a45"));
        assertFalse(StringUtil.isNumeric(" 123"));
    }

    // Tests isWhitespace HTML spec definitions
    @Test
    public void testIsWhitespace_variousCharacters_identifiesCorrectly() {
        assertTrue(StringUtil.isWhitespace(' '));
        assertTrue(StringUtil.isWhitespace('\t'));
        assertTrue(StringUtil.isWhitespace('\n'));
        assertTrue(StringUtil.isWhitespace('\f'));
        assertTrue(StringUtil.isWhitespace('\r'));
        assertFalse(StringUtil.isWhitespace(160));
        assertFalse(StringUtil.isWhitespace('a'));
    }

    // Tests isActuallyWhitespace including non-breaking space (160)
    @Test
    public void testIsActuallyWhitespace_nbspAndOtherWhitespace_returnsTrue() {
        assertTrue(StringUtil.isActuallyWhitespace(160));
        assertTrue(StringUtil.isActuallyWhitespace(' '));
        assertFalse(StringUtil.isActuallyWhitespace('x'));
    }

    // Tests normaliseWhitespace collapsing multiple whitespaces and stripping leading/trailing correctly
    @Test
    public void testNormaliseWhitespace_multipleWhitespaces_collapsesToSingleSpace() {
        assertEquals("a b c", StringUtil.normaliseWhitespace("  a  \t\n  b   c  "));
        assertEquals("a b", StringUtil.normaliseWhitespace("a\u00A0b"));
    }

    // Tests appendNormalisedWhitespace with stripLeading true and false
    @Test
    public void testAppendNormalisedWhitespace_stripLeadingOption_handlesLeadingWhitespace() {
        StringBuilder sb1 = new StringBuilder();
        StringUtil.appendNormalisedWhitespace(sb1, "   hello world", true);
        assertEquals("hello world", sb1.toString());

        StringBuilder sb2 = new StringBuilder();
        StringUtil.appendNormalisedWhitespace(sb2, "   hello world", false);
        assertEquals(" hello world", sb2.toString());
    }

    // Tests in method
    @Test
    public void testIn_needleInHaystack_returnsTrueOrFalse() {
        assertTrue(StringUtil.in("apple", "banana", "apple", "cherry"));
        assertFalse(StringUtil.in("orange", "banana", "apple", "cherry"));
    }

    // Tests inSorted method
    @Test
    public void testInSorted_sortedHaystack_returnsCorrectResult() {
        String[] sorted = new String[]{"apple", "banana", "cherry"};
        assertTrue(StringUtil.inSorted("banana", sorted));
        assertFalse(StringUtil.inSorted("orange", sorted));
    }

    // Tests resolve with base URL and relative URL query string and relative path
    @Test
    public void testResolve_baseUrlAndRelativePath_resolvesCorrectly() throws MalformedURLException {
        URL base = new URL("http://example.com/dir/file.html");
        assertEquals("http://example.com/dir/file.html?query=1", StringUtil.resolve(base, "?query=1").toExternalForm());
        assertEquals("http://example.com/dir/other.html", StringUtil.resolve(base, "other.html").toExternalForm());
    }

    // Tests resolve string method with normal and malformed cases
    @Test
    public void testResolve_stringUrls_handlesRelativeAndMalformed() {
        assertEquals("http://example.com/dir/target.html", StringUtil.resolve("http://example.com/dir/file.html", "target.html"));
        assertEquals("http://example.com/abs.html", StringUtil.resolve("malformed-base", "http://example.com/abs.html"));
        assertEquals("", StringUtil.resolve("malformed-base", "relative.html"));
    }

    // Tests stringBuilder cached reuse and cleanup
    @Test
    public void testStringBuilder_reusedBuilder_returnsEmptyInstance() {
        StringBuilder sb1 = StringUtil.stringBuilder();
        sb1.append("test-data");
        StringBuilder sb2 = StringUtil.stringBuilder();
        assertEquals(0, sb2.length());
    }
}