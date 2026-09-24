package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Unit tests for {@link Util}.
 */
public class UtilTest
{
    // Tests null input for stripLeadingHyphens
    @Test
    public void testStripLeadingHyphens_nullInput_returnsNull()
    {
        assertNull(Util.stripLeadingHyphens(null));
    }

    // Tests string starting with double hyphens
    @Test
    public void testStripLeadingHyphens_doubleHyphenPrefix_returnsStrippedString()
    {
        assertEquals("foo", Util.stripLeadingHyphens("--foo"));
    }

    // Tests string containing only double hyphens
    @Test
    public void testStripLeadingHyphens_doubleHyphenOnly_returnsEmptyString()
    {
        assertEquals("", Util.stripLeadingHyphens("--"));
    }

    // Tests string starting with a single hyphen
    @Test
    public void testStripLeadingHyphens_singleHyphenPrefix_returnsStrippedString()
    {
        assertEquals("bar", Util.stripLeadingHyphens("-bar"));
    }

    // Tests string containing only a single hyphen
    @Test
    public void testStripLeadingHyphens_singleHyphenOnly_returnsEmptyString()
    {
        assertEquals("", Util.stripLeadingHyphens("-"));
    }

    // Tests string with three hyphens
    @Test
    public void testStripLeadingHyphens_tripleHyphenPrefix_returnsSingleHyphenPrefixedString()
    {
        assertEquals("-baz", Util.stripLeadingHyphens("---baz"));
    }

    // Tests string with no leading hyphens
    @Test
    public void testStripLeadingHyphens_noLeadingHyphen_returnsOriginalString()
    {
        assertEquals("foo", Util.stripLeadingHyphens("foo"));
    }

    // Tests empty string for stripLeadingHyphens
    @Test
    public void testStripLeadingHyphens_emptyString_returnsEmptyString()
    {
        assertEquals("", Util.stripLeadingHyphens(""));
    }

    // Tests string enclosed in double quotes
    @Test
    public void testStripLeadingAndTrailingQuotes_enclosedInQuotes_returnsStrippedString()
    {
        assertEquals("one two", Util.stripLeadingAndTrailingQuotes("\"one two\""));
    }

    // Tests string with only leading double quote
    @Test
    public void testStripLeadingAndTrailingQuotes_leadingQuoteOnly_returnsStrippedString()
    {
        assertEquals("foo", Util.stripLeadingAndTrailingQuotes("\"foo"));
    }

    // Tests string with only trailing double quote
    @Test
    public void testStripLeadingAndTrailingQuotes_trailingQuoteOnly_returnsStrippedString()
    {
        assertEquals("bar", Util.stripLeadingAndTrailingQuotes("bar\""));
    }

    // Tests string without double quotes
    @Test
    public void testStripLeadingAndTrailingQuotes_noQuotes_returnsOriginalString()
    {
        assertEquals("baz", Util.stripLeadingAndTrailingQuotes("baz"));
    }

    // Tests empty string for stripLeadingAndTrailingQuotes
    @Test
    public void testStripLeadingAndTrailingQuotes_emptyString_returnsEmptyString()
    {
        assertEquals("", Util.stripLeadingAndTrailingQuotes(""));
    }

    // Tests string containing only a pair of double quotes
    @Test
    public void testStripLeadingAndTrailingQuotes_quotesOnly_returnsEmptyString()
    {
        assertEquals("", Util.stripLeadingAndTrailingQuotes("\"\""));
    }

    // Tests string containing only a single double quote
    @Test
    public void testStripLeadingAndTrailingQuotes_singleQuoteOnly_returnsEmptyString()
    {
        assertEquals("", Util.stripLeadingAndTrailingQuotes("\""));
    }

    // Tests string with quotes inside content
    @Test
    public void testStripLeadingAndTrailingQuotes_innerQuotes_retainsInnerQuotes()
    {
        assertEquals("foo\"bar", Util.stripLeadingAndTrailingQuotes("\"foo\"bar\""));
    }
}