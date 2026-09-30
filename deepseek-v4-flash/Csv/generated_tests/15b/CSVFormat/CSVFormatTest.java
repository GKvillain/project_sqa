package org.apache.commons.csv;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * JUnit 4 test class for CSVFormat, targeting Defects4J bug 15b.
 */
public class CSVFormatTest {

    // ========== validation tests ==========

    // Tests that delimiter cannot be a line break ('\n')
    @Test(expected = IllegalArgumentException.class)
    public void testValidate_delimiterIsLineBreak_throwsException() {
        CSVFormat.DEFAULT.withDelimiter('\n');
    }

    // Tests that delimiter cannot be a line break ('\r')
    @Test(expected = IllegalArgumentException.class)
    public void testValidate_delimiterIsCarriageReturn_throwsException() {
        CSVFormat.DEFAULT.withDelimiter('\r');
    }

    // Tests that quoteChar and delimiter cannot be the same
    @Test(expected = IllegalArgumentException.class)
    public void testValidate_quoteEqualsDelimiter_throwsException() {
        CSVFormat.DEFAULT.withQuote(',').withDelimiter(',');
    }

    // Tests that escape character and delimiter cannot be the same
    @Test(expected = IllegalArgumentException.class)
    public void testValidate_escapeEqualsDelimiter_throwsException() {
        CSVFormat.DEFAULT.withEscape(',').withDelimiter(',');
    }

    // Tests that comment marker and delimiter cannot be the same
    @Test(expected = IllegalArgumentException.class)
    public void testValidate_commentEqualsDelimiter_throwsException() {
        CSVFormat.DEFAULT.withCommentMarker(',').withDelimiter(',');
    }

    // Tests that comment marker and quoteChar cannot be the same
    @Test(expected = IllegalArgumentException.class)
    public void testValidate_commentEqualsQuote_throwsException() {
        CSVFormat.DEFAULT.withCommentMarker('"').withQuote('"');
    }

    // Tests that escape and comment marker cannot be the same
    @Test(expected = IllegalArgumentException.class)
    public void testValidate_escapeEqualsComment_throwsException() {
        CSVFormat.DEFAULT.withEscape('#').withCommentMarker('#');
    }

    // Tests that withQuoteMode(NONE) and no escape character throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testValidate_noEscapeAndQuoteModeNone_throwsException() {
        CSVFormat.DEFAULT.withQuote(null).withEscape(null).withQuoteMode(QuoteMode.NONE);
    }

    // Tests that duplicate header entries throw exception
    @Test(expected = IllegalArgumentException.class)
    public void testValidate_duplicateHeader_throwsException() {
        CSVFormat.DEFAULT.withHeader("a", "b", "a");
    }

    // ========== normal / boundary tests ==========

    // Tests default format properties
    @Test
    public void testDefaultFormat_hasExpectedValues() {
        CSVFormat format = CSVFormat.DEFAULT;
        assertEquals(',', format.getDelimiter());
        assertEquals(Character.valueOf('"'), format.getQuoteCharacter());
        assertEquals("\r\n", format.getRecordSeparator());
        assertTrue(format.getIgnoreEmptyLines());
        assertFalse(format.getSkipHeaderRecord());
    }

    // Tests withDelimiter returns new format with changed delimiter
    @Test
    public void testWithDelimiter_validChar_returnsCorrectFormat() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(';');
        assertEquals(';', format.getDelimiter());
    }

    // Tests withQuote set to null disables quoting
    @Test
    public void testWithQuote_null_disablesQuoting() {
        CSVFormat format = CSVFormat.DEFAULT.withQuote(null);
        assertNull(format.getQuoteCharacter());
        assertFalse(format.isQuoteCharacterSet());
    }

    // Tests withEscape sets escape character
    @Test
    public void testWithEscape_validChar_escapeSet() {
        CSVFormat format = CSVFormat.DEFAULT.withEscape('\\');
        assertEquals(Character.valueOf('\\'), format.getEscapeCharacter());
        assertTrue(format.isEscapeCharacterSet());
    }

    // Tests withCommentMarker sets comment marker
    @Test
    public void testWithCommentMarker_validChar_commentSet() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        assertEquals(Character.valueOf('#'), format.getCommentMarker());
        assertTrue(format.isCommentMarkerSet());
    }

    // Tests withNullString sets null string
    @Test
    public void testWithNullString_validString_nullStringSet() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        assertEquals("NULL", format.getNullString());
        assertTrue(format.isNullStringSet());
    }

    // Tests withRecordSeparator sets record separator (String version)
    @Test
    public void testWithRecordSeparator_string_returnsCorrect() {
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator("\n");
        assertEquals("\n", format.getRecordSeparator());
    }

    // Tests withRecordSeparator sets record separator (char version)
    @Test
    public void testWithRecordSeparator_char_returnsCorrect() {
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator('\n');
        assertEquals("\n", format.getRecordSeparator());
    }

    // Tests withIgnoreSurroundingSpaces toggles correctly
    @Test
    public void testWithIgnoreSurroundingSpaces_true_returnsFormat() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(true);
        assertTrue(format.getIgnoreSurroundingSpaces());
    }

    // Tests withAllowMissingColumnNames toggles correctly
    @Test
    public void testWithAllowMissingColumnNames_true_returnsFormat() {
        CSVFormat format = CSVFormat.DEFAULT.withAllowMissingColumnNames(true);
        assertTrue(format.getAllowMissingColumnNames());
    }

    // Tests withQuoteMode sets the quote mode
    @Test
    public void testWithQuoteMode_nonNull_setsMode() {
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL);
        assertEquals(QuoteMode.ALL, format.getQuoteMode());
    }

    // Tests withSystemRecordSeparator uses system line separator
    @Test
    public void testWithSystemRecordSeparator_usesSystemProperty() {
        CSVFormat format = CSVFormat.DEFAULT.withSystemRecordSeparator();
        assertEquals(System.getProperty("line.separator"), format.getRecordSeparator());
    }

    // Tests format method with simple values
    @Test
    public void testFormat_simpleValues_returnsFormattedString() {
        CSVFormat format = CSVFormat.DEFAULT;
        String result = format.format("a", "b", "c");
        assertEquals("a,b,c", result);
    }

    // Tests format with null value (should produce empty string)
    @Test
    public void testFormat_nullValue_returnsEmptyToken() {
        CSVFormat format = CSVFormat.DEFAULT;
        String result = format.format((Object) null);
        assertEquals("", result);
    }

    // Tests format with null string replacement
    @Test
    public void testFormat_nullStringReplacement_usesNullString() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("\\N");
        String result = format.format((Object) null);
        assertEquals("\\N", result);
    }

    // Tests format with value that contains delimiter (should be quoted)
    @Test
    public void testFormat_valueWithDelimiter_quotesValue() {
        CSVFormat format = CSVFormat.DEFAULT;
        String result = format.format("a,b", "c");
        assertEquals("\"a,b\",c", result);
    }

    // Tests format with value that contains quote (should be escaped)
    @Test
    public void testFormat_valueWithQuote_escapesQuote() {
        CSVFormat format = CSVFormat.DEFAULT;
        String result = format.format("a\"b", "c");
        assertEquals("\"a\"\"b\",c", result);
    }

    // Tests format with value that contains record separator (should be quoted)
    @Test
    public void testFormat_valueWithRecordSeparator_quotesValue() {
        CSVFormat format = CSVFormat.DEFAULT;
        String result = format.format("a\nb", "c");
        assertEquals("\"a\nb\",c", result);
    }

    // Tests equals when formats are identical
    @Test
    public void testEquals_sameFormat_returnsTrue() {
        CSVFormat format1 = CSVFormat.DEFAULT;
        CSVFormat format2 = CSVFormat.DEFAULT;
        assertTrue(format1.equals(format2));
    }

    // Tests equals when formats differ in a field
    @Test
    public void testEquals_differentDelimiter_returnsFalse() {
        CSVFormat format1 = CSVFormat.DEFAULT;
        CSVFormat format2 = CSVFormat.DEFAULT.withDelimiter(';');
        assertFalse(format1.equals(format2));
    }

    // Tests hashCode consistency with equals
    @Test
    public void testHashCode_equalFormats_sameHash() {
        CSVFormat format1 = CSVFormat.DEFAULT.withDelimiter(';');
        CSVFormat format2 = CSVFormat.DEFAULT.withDelimiter(';');
        assertEquals(format1.hashCode(), format2.hashCode());
    }

    // Tests toString contains key information
    @Test
    public void testToString_containsDelimiter() {
        String str = CSVFormat.DEFAULT.toString();
        assertTrue(str.contains("Delimiter=<,>"));
    }

    // Tests withIgnoreEmptyLines toggles correctly
    @Test
    public void testWithIgnoreEmptyLines_false_returnsFormat() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreEmptyLines(false);
        assertFalse(format.getIgnoreEmptyLines());
    }

    // Tests withSkipHeaderRecord toggles correctly
    @Test
    public void testWithSkipHeaderRecord_true_returnsFormat() {
        CSVFormat format = CSVFormat.DEFAULT.withSkipHeaderRecord(true);
        assertTrue(format.getSkipHeaderRecord());
    }

    // Tests withTrim toggles correctly
    @Test
    public void testWithTrim_true_returnsFormat() {
        CSVFormat format = CSVFormat.DEFAULT.withTrim(true);
        assertTrue(format.getTrim());
    }

    // Tests withTrailingDelimiter toggles correctly
    @Test
    public void testWithTrailingDelimiter_true_returnsFormat() {
        CSVFormat format = CSVFormat.DEFAULT.withTrailingDelimiter(true);
        assertTrue(format.getTrailingDelimiter());
    }

    // Tests withAutoFlush toggles correctly
    @Test
    public void testWithAutoFlush_true_returnsFormat() {
        CSVFormat format = CSVFormat.DEFAULT.withAutoFlush(true);
        assertTrue(format.getAutoFlush());
    }

    // Tests that TDF format uses tab delimiter
    @Test
    public void testTDF_delimiterIsTab() {
        assertEquals('\t', CSVFormat.TDF.getDelimiter());
        assertTrue(CSVFormat.TDF.getIgnoreSurroundingSpaces());
    }

    // Tests that EXCEL format allows missing column names
    @Test
    public void testEXCEL_allowMissingColumnNames() {
        assertTrue(CSVFormat.EXCEL.getAllowMissingColumnNames());
    }

    // Tests predefined formats via valueOf
    @Test
    public void testValueOf_validName_returnsCorrectFormat() {
        CSVFormat format = CSVFormat.valueOf("Default");
        assertSame(CSVFormat.DEFAULT, format);
    }

    // Tests newFormat creates a basic format
    @Test
    public void testNewFormat_delimiterOnly_basicFormat() {
        CSVFormat format = CSVFormat.newFormat('|');
        assertEquals('|', format.getDelimiter());
        assertNull(format.getQuoteCharacter());
        assertNull(format.getEscapeCharacter());
    }

    // Tests getHeader when headers are set
    @Test
    public void testGetHeader_withHeaders_returnsCopy() {
        String[] headers = {"a", "b", "c"};
        CSVFormat format = CSVFormat.DEFAULT.withHeader(headers);
        assertArrayEquals(headers, format.getHeader());
    }

    // Tests withHeader null clears the header
    @Test
    public void testWithHeader_null_clearsHeader() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("a").withHeader((String[]) null);
        assertNull(format.getHeader());
    }

    // Tests withRecordSeparator with empty string uses default
    @Test
    public void testWithRecordSeparator_emptyString_usesDefault() {
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator("");
        assertEquals("\r\n", format.getRecordSeparator());
    }
}