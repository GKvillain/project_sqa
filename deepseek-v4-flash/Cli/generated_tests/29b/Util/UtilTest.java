package org.apache.commons.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class UtilTest {

    // Tests null input for stripLeadingHyphens
    @Test
    public void testStripLeadingHyphens_nullInput_returnsNull() {
        assertNull(Util.stripLeadingHyphens(null));
    }

    // Tests empty string input
    @Test
    public void testStripLeadingHyphens_emptyString_returnsEmptyString() {
        assertEquals("", Util.stripLeadingHyphens(""));
    }

    // Tests string without leading hyphens
    @Test
    public void testStripLeadingHyphens_noHyphen_returnsSameString() {
        assertEquals("value", Util.stripLeadingHyphens("value"));
    }

    // Tests short option with one leading hyphen
    @Test
    public void testStripLeadingHyphens_singleHyphen_removesHyphen() {
        assertEquals("value", Util.stripLeadingHyphens("-value"));
    }

    // Tests long option with two leading hyphens
    @Test
    public void testStripLeadingHyphens_doubleHyphen_removesTwoHyphens() {
        assertEquals("value", Util.stripLeadingHyphens("--value"));
    }

    // Tests hyphen-only boundary case
    @Test
    public void testStripLeadingHyphens_hyphenOnly_returnsEmptyString() {
        assertEquals("", Util.stripLeadingHyphens("-"));
    }

    // Tests double hyphen-only boundary case
    @Test
    public void testStripLeadingHyphens_doubleHyphenOnly_returnsEmptyString() {
        assertEquals("", Util.stripLeadingHyphens("--"));
    }

    // Tests null input for stripLeadingAndTrailingQuotes
    @Test
    public void testStripLeadingAndTrailingQuotes_nullInput_returnsNull() {
        assertNull(Util.stripLeadingAndTrailingQuotes(null));
    }

    // Tests empty string input
    @Test
    public void testStripLeadingAndTrailingQuotes_emptyString_returnsEmptyString() {
        assertEquals("", Util.stripLeadingAndTrailingQuotes(""));
    }

    // Tests string without quotes
    @Test
    public void testStripLeadingAndTrailingQuotes_noQuotes_returnsSameString() {
        assertEquals("value", Util.stripLeadingAndTrailingQuotes("value"));
    }

    // Tests both leading and trailing quotes are removed
    @Test
    public void testStripLeadingAndTrailingQuotes_bothQuotes_removesBothQuotes() {
        assertEquals("value", Util.stripLeadingAndTrailingQuotes("\"value\""));
    }

    // Tests only leading quote is removed
    @Test
    public void testStripLeadingAndTrailingQuotes_leadingQuote_removesLeadingQuote() {
        assertEquals("value", Util.stripLeadingAndTrailingQuotes("\"value"));
    }

    // Tests only trailing quote is removed
    @Test
    public void testStripLeadingAndTrailingQuotes_trailingQuote_removesTrailingQuote() {
        assertEquals("value", Util.stripLeadingAndTrailingQuotes("value\""));
    }

    // Tests quoted empty string
    @Test
    public void testStripLeadingAndTrailingQuotes_quotedEmptyString_returnsEmptyString() {
        assertEquals("", Util.stripLeadingAndTrailingQuotes("\"\""));
    }

    // Tests quoted value with spaces preserves the spaces
    @Test
    public void testStripLeadingAndTrailingQuotes_valueWithSpaces_preservesSpaces() {
        assertEquals("hello world", Util.stripLeadingAndTrailingQuotes("\"hello world\""));
    }

    // Additional tests for uncovered branches

    @Test
    public void testStripLeadingHyphens_tripleHyphens_returnsStringWithSingleHyphen() {
        assertEquals("-value", Util.stripLeadingHyphens("---value"));
    }

    @Test
    public void testStripLeadingHyphens_quadrupleHyphens_returnsStringWithDoubleHyphen() {
        assertEquals("--value", Util.stripLeadingHyphens("----value"));
    }

    @Test
    public void testStripLeadingHyphens_tripleHyphenOnly_returnsSingleHyphen() {
        assertEquals("-", Util.stripLeadingHyphens("---"));
    }

    @Test
    public void testStripLeadingHyphens_quadrupleHyphenOnly_returnsDoubleHyphen() {
        assertEquals("--", Util.stripLeadingHyphens("----"));
    }

    @Test
    public void testStripLeadingAndTrailingQuotes_singleQuote_returnsEmptyString() {
        assertEquals("", Util.stripLeadingAndTrailingQuotes("\""));
    }

    @Test
    public void testStripLeadingAndTrailingQuotes_internalQuotesPreserved() {
        assertEquals("a\"b", Util.stripLeadingAndTrailingQuotes("\"a\"b\""));
    }

    @Test
    public void testStripLeadingAndTrailingQuotes_tripleQuotes_returnsSingleQuote() {
        assertEquals("\"", Util.stripLeadingAndTrailingQuotes("\"\"\""));
    }
}