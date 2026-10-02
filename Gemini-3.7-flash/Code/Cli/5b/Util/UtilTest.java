package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class UtilTest {

    // Tests defect where null input should return null
    @Test
    public void testStripLeadingHyphens_nullInput_returnsNull() {
        assertNull(Util.stripLeadingHyphens(null));
    }

    // Tests stripping double leading hyphens
    @Test
    public void testStripLeadingHyphens_doubleHyphen_returnsStrippedString() {
        assertEquals("foo", Util.stripLeadingHyphens("--foo"));
    }

    // Tests stripping single leading hyphen
    @Test
    public void testStripLeadingHyphens_singleHyphen_returnsStrippedString() {
        assertEquals("foo", Util.stripLeadingHyphens("-foo"));
    }

    // Tests string without leading hyphens
    @Test
    public void testStripLeadingHyphens_noHyphen_returnsOriginalString() {
        assertEquals("foo", Util.stripLeadingHyphens("foo"));
    }

    // Tests string with more than two leading hyphens
    @Test
    public void testStripLeadingHyphens_multipleHyphens_stripsFirstTwoHyphens() {
        assertEquals("-foo", Util.stripLeadingHyphens("---foo"));
    }

    // Tests string containing only single hyphen
    @Test
    public void testStripLeadingHyphens_onlySingleHyphen_returnsEmptyString() {
        assertEquals("", Util.stripLeadingHyphens("-"));
    }

    // Tests string containing only double hyphens
    @Test
    public void testStripLeadingHyphens_onlyDoubleHyphen_returnsEmptyString() {
        assertEquals("", Util.stripLeadingHyphens("--"));
    }

    // Tests empty string input for stripLeadingHyphens
    @Test
    public void testStripLeadingHyphens_emptyString_returnsEmptyString() {
        assertEquals("", Util.stripLeadingHyphens(""));
    }

    // Tests stripping leading and trailing quotes from a quoted string
    @Test
    public void testStripLeadingAndTrailingQuotes_quotedString_returnsUnquotedString() {
        assertEquals("foo", Util.stripLeadingAndTrailingQuotes("\"foo\""));
    }

    // Tests string without quotes
    @Test
    public void testStripLeadingAndTrailingQuotes_unquotedString_returnsOriginalString() {
        assertEquals("foo", Util.stripLeadingAndTrailingQuotes("foo"));
    }

    // Tests string with only leading quote
    @Test
    public void testStripLeadingAndTrailingQuotes_leadingQuoteOnly_returnsStrippedString() {
        assertEquals("foo", Util.stripLeadingAndTrailingQuotes("\"foo"));
    }

    // Tests string with only trailing quote
    @Test
    public void testStripLeadingAndTrailingQuotes_trailingQuoteOnly_returnsStrippedString() {
        assertEquals("foo", Util.stripLeadingAndTrailingQuotes("foo\""));
    }

    // Tests string containing exactly two quotes
    @Test
    public void testStripLeadingAndTrailingQuotes_twoQuotes_returnsEmptyString() {
        assertEquals("", Util.stripLeadingAndTrailingQuotes("\"\""));
    }

    // Tests empty string input for stripLeadingAndTrailingQuotes
    @Test
    public void testStripLeadingAndTrailingQuotes_emptyString_returnsEmptyString() {
        assertEquals("", Util.stripLeadingAndTrailingQuotes(""));
    }

    // Tests constructor coverage for Util class
    @Test
    public void testUtilConstructor() {
        Util util = new Util();
        assertNotNull(util);
    }

    // Tests string containing a single quote character
    @Test
    public void testStripLeadingAndTrailingQuotes_singleQuote_returnsEmptyString() {
        assertEquals("", Util.stripLeadingAndTrailingQuotes("\""));
    }

    // Tests string containing quotes in the middle
    @Test
    public void testStripLeadingAndTrailingQuotes_quotesInMiddle() {
        assertEquals("foo\"bar", Util.stripLeadingAndTrailingQuotes("\"foo\"bar\""));
    }
}