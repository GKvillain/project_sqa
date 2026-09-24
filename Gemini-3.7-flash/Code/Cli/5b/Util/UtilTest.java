package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Test cases for {@link Util}.
 */
public class UtilTest {

    // Tests constructor for class coverage
    @Test
    public void testConstructor() {
        assertNotNull(new Util());
    }

    // Tests stripLeadingHyphens with null input (Defects4J Cli-5 regression test)
    @Test
    public void testStripLeadingHyphens_nullInput_returnsNull() {
        assertNull(Util.stripLeadingHyphens(null));
    }

    // Tests double leading hyphens
    @Test
    public void testStripLeadingHyphens_doubleHyphenPrefix_returnsStrippedString() {
        assertEquals("foo", Util.stripLeadingHyphens("--foo"));
    }

    // Tests single leading hyphen
    @Test
    public void testStripLeadingHyphens_singleHyphenPrefix_returnsStrippedString() {
        assertEquals("f", Util.stripLeadingHyphens("-f"));
    }

    // Tests string with no leading hyphens
    @Test
    public void testStripLeadingHyphens_noHyphenPrefix_returnsOriginalString() {
        assertEquals("foo", Util.stripLeadingHyphens("foo"));
    }

    // Tests empty string input
    @Test
    public void testStripLeadingHyphens_emptyString_returnsEmptyString() {
        assertEquals("", Util.stripLeadingHyphens(""));
    }

    // Tests string containing only two hyphens
    @Test
    public void testStripLeadingHyphens_onlyTwoHyphens_returnsEmptyString() {
        assertEquals("", Util.stripLeadingHyphens("--"));
    }

    // Tests string containing only one hyphen
    @Test
    public void testStripLeadingHyphens_onlyOneHyphen_returnsEmptyString() {
        assertEquals("", Util.stripLeadingHyphens("-"));
    }

    // Tests string with more than two leading hyphens
    @Test
    public void testStripLeadingHyphens_tripleHyphenPrefix_returnsSingleHyphenPrefix() {
        assertEquals("-foo", Util.stripLeadingHyphens("---foo"));
    }

    // Tests string with hyphen in the middle
    @Test
    public void testStripLeadingHyphens_hyphenInMiddle_returnsOriginalString() {
        assertEquals("foo-bar", Util.stripLeadingHyphens("foo-bar"));
    }

    // Tests both leading and trailing quotes
    @Test
    public void testStripLeadingAndTrailingQuotes_enclosedInQuotes_returnsUnquotedString() {
        assertEquals("foo", Util.stripLeadingAndTrailingQuotes("\"foo\""));
    }

    // Tests leading quote only
    @Test
    public void testStripLeadingAndTrailingQuotes_leadingQuoteOnly_returnsStrippedString() {
        assertEquals("foo", Util.stripLeadingAndTrailingQuotes("\"foo"));
    }

    // Tests trailing quote only
    @Test
    public void testStripLeadingAndTrailingQuotes_trailingQuoteOnly_returnsStrippedString() {
        assertEquals("foo", Util.stripLeadingAndTrailingQuotes("foo\""));
    }

    // Tests string without quotes
    @Test
    public void testStripLeadingAndTrailingQuotes_noQuotes_returnsOriginalString() {
        assertEquals("foo", Util.stripLeadingAndTrailingQuotes("foo"));
    }

    // Tests empty string
    @Test
    public void testStripLeadingAndTrailingQuotes_emptyString_returnsEmptyString() {
        assertEquals("", Util.stripLeadingAndTrailingQuotes(""));
    }

    // Tests single quote string
    @Test
    public void testStripLeadingAndTrailingQuotes_singleQuote_returnsEmptyString() {
        assertEquals("", Util.stripLeadingAndTrailingQuotes("\""));
    }

    // Tests string containing only two quotes
    @Test
    public void testStripLeadingAndTrailingQuotes_twoQuotesOnly_returnsEmptyString() {
        assertEquals("", Util.stripLeadingAndTrailingQuotes("\"\""));
    }

    // Tests string with quotes in the middle
    @Test
    public void testStripLeadingAndTrailingQuotes_nestedQuotes_returnsStrippedOuterQuotes() {
        assertEquals("foo \"bar\"", Util.stripLeadingAndTrailingQuotes("\"foo \"bar\"\""));
    }

    // Tests string with quotes only in the middle
    @Test
    public void testStripLeadingAndTrailingQuotes_middleQuotesOnly_returnsOriginalString() {
        assertEquals("foo\"bar", Util.stripLeadingAndTrailingQuotes("foo\"bar"));
    }

    // Tests string containing only three quotes
    @Test
    public void testStripLeadingAndTrailingQuotes_threeQuotes_returnsSingleQuote() {
        assertEquals("\"", Util.stripLeadingAndTrailingQuotes("\"\"\""));
    }
}