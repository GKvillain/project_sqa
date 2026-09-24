package org.jsoup.helper;

import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StringUtilTest {

    // Tests join with empty collection
    @Test
    public void testJoin_emptyCollection_returnsEmptyString() {
        List<String> list = Collections.emptyList();
        assertEquals("", StringUtil.join(list, ","));
    }

    // Tests join with single element collection
    @Test
    public void testJoin_singleElementCollection_returnsSingleElement() {
        List<String> list = Collections.singletonList("one");
        assertEquals("one", StringUtil.join(list, ","));
    }

    // Tests join with multiple element array
    @Test
    public void testJoin_stringArray_returnsJoinedString() {
        String[] arr = new String[]{"one", "two", "three"};
        assertEquals("one, two, three", StringUtil.join(arr, ", "));
    }

    // Tests padding with memoised bounds (within cache)
    @Test
    public void testPadding_cachedWidth_returnsCorrectSpaces() {
        assertEquals("", StringUtil.padding(0));
        assertEquals(" ", StringUtil.padding(1));
        assertEquals("                    ", StringUtil.padding(20));
    }

    // Tests padding exceeding memoised array length
    @Test
    public void testPadding_exceedsCachedWidth_returnsCorrectSpaces() {
        assertEquals("                     ", StringUtil.padding(21));
        assertEquals("                          ", StringUtil.padding(26));
    }

    // Tests padding with negative width throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testPadding_negativeWidth_throwsException() {
        StringUtil.padding(-1);
    }

    // Tests isBlank with null, empty, whitespace, and non-blank strings
    @Test
    public void testIsBlank_variousInputs_returnsExpectedBoolean() {
        assertTrue(StringUtil.isBlank(null));
        assertTrue(StringUtil.isBlank(""));
        assertTrue(StringUtil.isBlank("   "));
        assertTrue(StringUtil.isBlank("\t\n\r \f"));
        assertFalse(StringUtil.isBlank("  a  "));
        assertFalse(StringUtil.isBlank("abc"));
    }

    // Tests isNumeric with valid digits, non-digits, null, and empty string
    @Test
    public void testIsNumeric_variousInputs_returnsExpectedBoolean() {
        assertFalse(StringUtil.isNumeric(null));
        assertFalse(StringUtil.isNumeric(""));
        assertTrue(StringUtil.isNumeric("1234567890"));
        assertFalse(StringUtil.isNumeric("123a45"));
        assertFalse(StringUtil.isNumeric(" 123 "));
    }

    // Tests isWhitespace and isActuallyWhitespace HTML character checks
    @Test
    public void testIsWhitespace_whitespaceCodePoints_returnsExpectedBoolean() {
        assertTrue(StringUtil.isWhitespace(' '));
        assertTrue(StringUtil.isWhitespace('\t'));
        assertTrue(StringUtil.isWhitespace('\n'));
        assertTrue(StringUtil.isWhitespace('\f'));
        assertTrue(StringUtil.isWhitespace('\r'));
        assertFalse(StringUtil.isWhitespace('a'));
        assertFalse(StringUtil.isWhitespace(160)); // &nbsp; is not in HTML whitespace spec

        assertTrue(StringUtil.isActuallyWhitespace(160)); // &nbsp; is actually whitespace
        assertTrue(StringUtil.isActuallyWhitespace(' '));
        assertFalse(StringUtil.isActuallyWhitespace('a'));
    }

    // Tests normaliseWhitespace collapse and replacement behavior
    @Test
    public void testNormaliseWhitespace_multipleSpacesAndTabs_collapsesSpaces() {
        String input = "  Hello \t \n world  ! \r\n";
        assertEquals(" Hello world ! ", StringUtil.normaliseWhitespace(input));
    }

    // Tests appendNormalisedWhitespace with stripLeading set to true and false
    @Test
    public void testAppendNormalisedWhitespace_stripLeadingFlag_stripsCorrectly() {
        StringBuilder sb1 = new StringBuilder();
        StringUtil.appendNormalisedWhitespace(sb1, "   hello   world   ", true);
        assertEquals("hello world ", sb1.toString());

        StringBuilder sb2 = new StringBuilder();
        StringUtil.appendNormalisedWhitespace(sb2, "   hello   world   ", false);
        assertEquals(" hello world ", sb2.toString());
    }

    // Tests in method with match and no match
    @Test
    public void testIn_needleInHaystack_returnsCorrectBoolean() {
        assertTrue(StringUtil.in("two", "one", "two", "three"));
        assertFalse(StringUtil.in("four", "one", "two", "three"));
    }

    // Tests inSorted method using binary search
    @Test
    public void testInSorted_sortedArray_returnsCorrectBoolean() {
        String[] sorted = new String[]{"apple", "banana", "cherry", "date"};
        assertTrue(StringUtil.inSorted("banana", sorted));
        assertFalse(StringUtil.inSorted("grape", sorted));
    }

    // Tests resolve with valid URL objects and query strings
    @Test
    public void testResolve_urlObjects_returnsResolvedUrl() throws MalformedURLException {
        URL base = new URL("http://example.com/path/file.html");
        URL resolvedRel = StringUtil.resolve(base, "other.html");
        assertEquals("http://example.com/path/other.html", resolvedRel.toExternalForm());

        URL resolvedQuery = StringUtil.resolve(base, "?foo=bar");
        assertEquals("http://example.com/path/file.html?foo=bar", resolvedQuery.toExternalForm());
    }

    // Tests resolve with string inputs including relative path and invalid URLs
    @Test
    public void testResolve_stringUrls_returnsExpectedResolvedString() {
        String base = "http://example.com/dir/page.html";
        assertEquals("http://example.com/dir/other.html", StringUtil.resolve(base, "other.html"));
        assertEquals("http://example.com/root.html", StringUtil.resolve(base, "/root.html"));
        assertEquals("http://other.com/page.html", StringUtil.resolve(base, "http://other.com/page.html"));
        assertEquals("http://example.com/abs.html", StringUtil.resolve("invalid-base-url", "http://example.com/abs.html"));
        assertEquals("", StringUtil.resolve("invalid-base-url", "invalid-rel-url"));
    }

    // Tests stringBuilder caching and re-initialization
    @Test
    public void testStringBuilder_reusedInstance_returnsEmptyStringBuilder() {
        StringBuilder sb1 = StringUtil.stringBuilder();
        sb1.append("test content");
        StringBuilder sb2 = StringUtil.stringBuilder();
        assertEquals(0, sb2.length());
    }
}