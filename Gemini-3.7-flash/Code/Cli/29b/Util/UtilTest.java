package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class UtilTest
{
    // Tests instantiation of Util class for constructor coverage
    @Test
    public void testUtilConstructor()
    {
        assertNotNull(new Util());
    }

    // Tests null input for stripLeadingHyphens
    @Test
    public void testStripLeadingHyphens_nullInput_returnsNull()
    {
        assertNull(Util.stripLeadingHyphens(null));
    }

    // Tests double hyphen prefix branch
    @Test
    public void testStripLeadingHyphens_doubleHyphen_returnsStrippedString()
    {
        assertEquals("foo", Util.stripLeadingHyphens("--foo"));
    }

    // Tests single hyphen prefix branch
    @Test
    public void testStripLeadingHyphens_singleHyphen_returnsStrippedString()
    {
        assertEquals("foo", Util.stripLeadingHyphens("-foo"));
    }

    // Tests string with no leading hyphens
    @Test
    public void testStripLeadingHyphens_noHyphens_returnsOriginalString()
    {
        assertEquals("foo", Util.stripLeadingHyphens("foo"));
    }

    // Tests string with three leading hyphens
    @Test
    public void testStripLeadingHyphens_tripleHyphen_returnsRemainingHyphen()
    {
        assertEquals("-foo", Util.stripLeadingHyphens("---foo"));
    }

    // Tests single hyphen boundary case
    @Test
    public void testStripLeadingHyphens_singleHyphenOnly_returnsEmptyString()
    {
        assertEquals("", Util.stripLeadingHyphens("-"));
    }

    // Tests double hyphen boundary case
    @Test
    public void testStripLeadingHyphens_doubleHyphenOnly_returnsEmptyString()
    {
        assertEquals("", Util.stripLeadingHyphens("--"));
    }

    // Tests empty string input for stripLeadingHyphens
    @Test
    public void testStripLeadingHyphens_emptyString_returnsEmptyString()
    {
        assertEquals("", Util.stripLeadingHyphens(""));
    }

    // Tests standard string with leading and trailing quotes
    @Test
    public void testStripLeadingAndTrailingQuotes_enclosedQuotes_returnsStrippedString()
    {
        assertEquals("foo", Util.stripLeadingAndTrailingQuotes("\"foo\""));
    }

    // Tests empty pair of quotes
    @Test
    public void testStripLeadingAndTrailingQuotes_emptyQuotes_returnsEmptyString()
    {
        assertEquals("", Util.stripLeadingAndTrailingQuotes("\"\""));
    }

    // Tests string without quotes
    @Test
    public void testStripLeadingAndTrailingQuotes_noQuotes_returnsOriginalString()
    {
        assertEquals("foo", Util.stripLeadingAndTrailingQuotes("foo"));
    }

    // Tests empty string input for stripLeadingAndTrailingQuotes
    @Test
    public void testStripLeadingAndTrailingQuotes_emptyString_returnsEmptyString()
    {
        assertEquals("", Util.stripLeadingAndTrailingQuotes(""));
    }

    // Tests string with only a leading quote (defect detection: should not strip if not paired)
    @Test
    public void testStripLeadingAndTrailingQuotes_onlyLeadingQuote_returnsOriginalString()
    {
        assertEquals("\"foo", Util.stripLeadingAndTrailingQuotes("\"foo"));
    }

    // Tests string with only a trailing quote (defect detection: should not strip if not paired)
    @Test
    public void testStripLeadingAndTrailingQuotes_onlyTrailingQuote_returnsOriginalString()
    {
        assertEquals("foo\"", Util.stripLeadingAndTrailingQuotes("foo\""));
    }

    // Tests single quote character boundary case
    @Test
    public void testStripLeadingAndTrailingQuotes_singleQuoteOnly_returnsOriginalString()
    {
        assertEquals("\"", Util.stripLeadingAndTrailingQuotes("\""));
    }

    // Tests multiple words enclosed in quotes
    @Test
    public void testStripLeadingAndTrailingQuotes_quotedSentence_returnsStrippedSentence()
    {
        assertEquals("one two", Util.stripLeadingAndTrailingQuotes("\"one two\""));
    }
}