package org.apache.commons.cli;

import static org.junit.Assert.*;
import org.junit.Test;

public class UtilTest {

    // ===== Existing tests =====

    // Tests stripLeadingHyphens with no hyphens
    @Test
    public void testStripLeadingHyphens_noHyphens_returnsSameString() {
        assertEquals("test", Util.stripLeadingHyphens("test"));
    }

    // Tests stripLeadingHyphens with single hyphen
    @Test
    public void testStripLeadingHyphens_singleHyphen_returnsStringWithoutHyphen() {
        assertEquals("test", Util.stripLeadingHyphens("-test"));
    }

    // Tests stripLeadingHyphens with double hyphens
    @Test
    public void testStripLeadingHyphens_doubleHyphens_returnsStringWithoutDoubleHyphens() {
        assertEquals("test", Util.stripLeadingHyphens("--test"));
    }

    // Tests stripLeadingHyphens with empty string
    @Test
    public void testStripLeadingHyphens_emptyString_returnsEmptyString() {
        assertEquals("", Util.stripLeadingHyphens(""));
    }

    // Tests stripLeadingHyphens with only one hyphen
    @Test
    public void testStripLeadingHyphens_onlyHyphen_returnsEmptyString() {
        assertEquals("", Util.stripLeadingHyphens("-"));
    }

    // Tests stripLeadingHyphens with only two hyphens
    @Test
    public void testStripLeadingHyphens_onlyDoubleHyphens_returnsEmptyString() {
        assertEquals("", Util.stripLeadingHyphens("--"));
    }

    // Tests stripLeadingHyphens with double hyphens and content after
    @Test
    public void testStripLeadingHyphens_doubleHyphensWithContent_returnsCorrectString() {
        assertEquals("abc", Util.stripLeadingHyphens("--abc"));
    }

    // Tests stripLeadingHyphens with single hyphen and content after
    @Test
    public void testStripLeadingHyphens_singleHyphenWithContent_returnsCorrectString() {
        assertEquals("abc", Util.stripLeadingHyphens("-abc"));
    }

    // Tests stripLeadingAndTrailingQuotes with no quotes
    @Test
    public void testStripLeadingAndTrailingQuotes_noQuotes_returnsSameString() {
        assertEquals("test", Util.stripLeadingAndTrailingQuotes("test"));
    }

    // Tests stripLeadingAndTrailingQuotes with leading and trailing quotes
    @Test
    public void testStripLeadingAndTrailingQuotes_bothQuotes_returnsStringWithoutQuotes() {
        assertEquals("test", Util.stripLeadingAndTrailingQuotes("\"test\""));
    }

    // Tests stripLeadingAndTrailingQuotes with only leading quote
    @Test
    public void testStripLeadingAndTrailingQuotes_leadingQuoteOnly_returnsStringWithoutLeadingQuote() {
        assertEquals("test\"", Util.stripLeadingAndTrailingQuotes("\"test\""));
    }

    // Tests stripLeadingAndTrailingQuotes with only trailing quote
    @Test
    public void testStripLeadingAndTrailingQuotes_trailingQuoteOnly_returnsStringWithoutTrailingQuote() {
        assertEquals("\"test", Util.stripLeadingAndTrailingQuotes("\"test\""));
    }

    // Tests stripLeadingAndTrailingQuotes with empty string
    @Test
    public void testStripLeadingAndTrailingQuotes_emptyString_returnsEmptyString() {
        assertEquals("", Util.stripLeadingAndTrailingQuotes(""));
    }

    // Tests stripLeadingAndTrailingQuotes with only a double quote
    @Test
    public void testStripLeadingAndTrailingQuotes_onlyQuote_returnsEmptyString() {
        assertEquals("", Util.stripLeadingAndTrailingQuotes("\""));
    }

    // Tests stripLeadingAndTrailingQuotes with multiple quotes inside
    @Test
    public void testStripLeadingAndTrailingQuotes_multipleInnerQuotes_removesOnlyOuterQuotes() {
        assertEquals("test\"inner\"value", Util.stripLeadingAndTrailingQuotes("\"test\"inner\"value\""));
    }

    // ===== New tests added for uncovered coverage =====

    // ---- stripLeadingHyphens ----

    // Tests null input returns null (assuming implementation handles null)
    @Test
    public void testStripLeadingHyphens_null_returnsNull() {
        assertNull(Util.stripLeadingHyphens(null));
    }

    // Tests triple hyphen: only the first two hyphens should be removed
    @Test
    public void testStripLeadingHyphens_tripleHyphen_returnsStringWithSingleHyphen() {
        assertEquals("-test", Util.stripLeadingHyphens("---test"));
    }

    // Tests four hyphens: first two removed, leaving two hyphens
    @Test
    public void testStripLeadingHyphens_fourHyphens_returnsStringWithTwoHyphens() {
        assertEquals("--test", Util.stripLeadingHyphens("----test"));
    }

    // Tests string starting with hyphen but containing spaces
    @Test
    public void testStripLeadingHyphens_hyphenWithSpaces_returnsStringWithoutHyphen() {
        assertEquals(" test", Util.stripLeadingHyphens("- test"));
    }

    // Tests string with only a leading hyphen and nothing else (already covered "-" but with whitespace)
    @Test
    public void testStripLeadingHyphens_onlyHyphenWithSpace_returnsEmptyString() {
        assertEquals("", Util.stripLeadingHyphens("- "));
    }

    // ---- stripLeadingAndTrailingQuotes ----

    // Tests null input returns null (assuming implementation handles null)
    @Test
    public void testStripLeadingAndTrailingQuotes_null_returnsNull() {
        assertNull(Util.stripLeadingAndTrailingQuotes(null));
    }

    // Tests string with only a leading quote (no trailing quote)
    @Test
    public void testStripLeadingAndTrailingQuotes_leadingQuoteOnlyWithoutTrailing_returnsStringWithoutLeadingQuote() {
        assertEquals("test", Util.stripLeadingAndTrailingQuotes("\"test"));
    }

    // Tests string with only a trailing quote (no leading quote)
    @Test
    public void testStripLeadingAndTrailingQuotes_trailingQuoteOnlyWithoutLeading_returnsStringWithoutTrailingQuote() {
        assertEquals("test", Util.stripLeadingAndTrailingQuotes("test\""));
    }

    // Tests string with multiple leading quotes but only one trailing quote
    @Test
    public void testStripLeadingAndTrailingQuotes_multipleLeadingQuotes_removesOnlyOutermost() {
        assertEquals("\"test\"", Util.stripLeadingAndTrailingQuotes("\"\"test\"\""));
    }

    // Tests string with quotes inside and no outer quotes (should remain unchanged)
    @Test
    public void testStripLeadingAndTrailingQuotes_innerQuotesOnly_returnsSameString() {
        assertEquals("te\"st", Util.stripLeadingAndTrailingQuotes("te\"st"));
    }
}