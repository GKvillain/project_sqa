package org.apache.commons.csv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CSVFormatTest {

    // Tests default format creation
    @Test
    public void testDefaultFormat_standardValues_returnsCorrectFormat() {
        CSVFormat format = CSVFormat.DEFAULT;
        assertEquals(',', format.getDelimiter());
        assertEquals(Character.valueOf('"'), format.getQuoteCharacter());
        assertEquals("\r\n", format.getRecordSeparator());
        assertTrue(format.getIgnoreEmptyLines());
        assertFalse(format.getSkipHeaderRecord());
        assertFalse(format.getIgnoreSurroundingSpaces());
        assertNull(format.getCommentMarker());
        assertNull(format.getEscapeCharacter());
        assertNull(format.getNullString());
        assertNull(format.getHeader());
        assertFalse(format.getAllowMissingColumnNames());
        assertFalse(format.getIgnoreHeaderCase());
    }

    // Tests RFC4180 format creation
    @Test
    public void testRfc4180Format_ignoreEmptyLinesDisabled_returnsCorrectFormat() {
        CSVFormat format = CSVFormat.RFC4180;
        assertEquals(',', format.getDelimiter());
        assertEquals(Character.valueOf('"'), format.getQuoteCharacter());
        assertEquals("\r\n", format.getRecordSeparator());
        assertFalse(format.getIgnoreEmptyLines());
    }

    // Tests EXCEL format creation
    @Test
    public void testExcelFormat_allowMissingColumnNames_returnsCorrectFormat() {
        CSVFormat format = CSVFormat.EXCEL;
        assertEquals(',', format.getDelimiter());
        assertEquals(Character.valueOf('"'), format.getQuoteCharacter());
        assertEquals("\r\n", format.getRecordSeparator());
        assertFalse(format.getIgnoreEmptyLines());
        assertTrue(format.getAllowMissingColumnNames());
    }

    // Tests TDF format creation
    @Test
    public void testTdfFormat_tabDelimiter_returnsCorrectFormat() {
        CSVFormat format = CSVFormat.TDF;
        assertEquals('\t', format.getDelimiter());
        assertEquals(Character.valueOf('"'), format.getQuoteCharacter());
        assertEquals("\r\n", format.getRecordSeparator());
        assertTrue(format.getIgnoreSurroundingSpaces());
    }

    // Tests MYSQL format creation
    @Test
    public void testMySqlFormat_escapeBackslashNullQuote_returnsCorrectFormat() {
        CSVFormat format = CSVFormat.MYSQL;
        assertEquals('\t', format.getDelimiter());
        assertNull(format.getQuoteCharacter());
        assertEquals("\n", format.getRecordSeparator());
        assertEquals(Character.valueOf('\\'), format.getEscapeCharacter());
        assertEquals("\\N", format.getNullString());
        assertFalse(format.getIgnoreEmptyLines());
    }

    // Tests newFormat with custom delimiter
    @Test
    public void testNewFormat_customDelimiter_returnsCorrectFormat() {
        CSVFormat format = CSVFormat.newFormat(';');
        assertEquals(';', format.getDelimiter());
        assertNull(format.getQuoteCharacter());
        assertNull(format.getRecordSeparator());
        assertFalse(format.getIgnoreEmptyLines());
    }

    // Tests withDelimiter with valid delimiter
    @Test
    public void testWithDelimiter_validDelimiter_returnsNewFormat() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(';');
        assertEquals(';', format.getDelimiter());
        assertEquals(',', CSVFormat.DEFAULT.getDelimiter());
    }

    // Tests withDelimiter throws exception for line break character
    @Test(expected = IllegalArgumentException.class)
    public void testWithDelimiter_lineBreakCharacter_throwsIllegalArgumentException() {
        CSVFormat.DEFAULT.withDelimiter('\n');
    }

    // Tests withQuote with valid character
    @Test
    public void testWithQuote_validCharacter_returnsNewFormat() {
        CSVFormat format = CSVFormat.DEFAULT.withQuote('\'');
        assertEquals(Character.valueOf('\''), format.getQuoteCharacter());
        assertEquals(Character.valueOf('"'), CSVFormat.DEFAULT.getQuoteCharacter());
    }

    // Tests withQuote with null disables quoting
    @Test
    public void testWithQuote_null_quoteDisabled() {
        CSVFormat format = CSVFormat.DEFAULT.withQuote(null);
        assertNull(format.getQuoteCharacter());
    }

    // Tests withEscape with valid character
    @Test
    public void testWithEscape_validCharacter_returnsNewFormat() {
        CSVFormat format = CSVFormat.DEFAULT.withEscape('\\');
        assertEquals(Character.valueOf('\\'), format.getEscapeCharacter());
        assertNull(CSVFormat.DEFAULT.getEscapeCharacter());
    }

    // Tests withIgnoreSurroundingSpaces enables trimming
    @Test
    public void testWithIgnoreSurroundingSpaces_enabled_true() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces();
        assertTrue(format.getIgnoreSurroundingSpaces());
        assertFalse(CSVFormat.DEFAULT.getIgnoreSurroundingSpaces());
    }

    // Tests withIgnoreEmptyLines enables ignoring
    @Test
    public void testWithIgnoreEmptyLines_enabled_true() {
        CSVFormat format = CSVFormat.RFC4180.withIgnoreEmptyLines();
        assertTrue(format.getIgnoreEmptyLines());
    }

    // Tests withHeader sets header array
    @Test
    public void testWithHeader_validHeader_returnsCopyOfHeader() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("A", "B", "C");
        String[] header = format.getHeader();
        assertNotNull(header);
        assertEquals(3, header.length);
        assertEquals("A", header[0]);
        assertEquals("B", header[1]);
        assertEquals("C", header[2]);
    }

    // Tests withHeader with duplicate entries throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testWithHeader_duplicateEntry_throwsIllegalArgumentException() {
        CSVFormat.DEFAULT.withHeader("A", "B", "A");
    }

    // Tests withHeaderComments setting
    @Test
    public void testWithHeaderComments_validComments_returnsHeaderComments() {
        CSVFormat format = CSVFormat.DEFAULT.withHeaderComments("Comment1", "Comment2");
        String[] comments = format.getHeaderComments();
        assertNotNull(comments);
        assertEquals(2, comments.length);
        assertEquals("Comment1", comments[0]);
        assertEquals("Comment2", comments[1]);
    }

    // Tests withSkipHeaderRecord enables skipping
    @Test
    public void testWithSkipHeaderRecord_enabled_true() {
        CSVFormat format = CSVFormat.DEFAULT.withSkipHeaderRecord();
        assertTrue(format.getSkipHeaderRecord());
        assertFalse(CSVFormat.DEFAULT.getSkipHeaderRecord());
    }

    // Tests withAllowMissingColumnNames enables allowance
    @Test
    public void testWithAllowMissingColumnNames_enabled_true() {
        CSVFormat format = CSVFormat.DEFAULT.withAllowMissingColumnNames();
        assertTrue(format.getAllowMissingColumnNames());
        assertFalse(CSVFormat.DEFAULT.getAllowMissingColumnNames());
    }

    // Tests withIgnoreHeaderCase enables ignoring case
    @Test
    public void testWithIgnoreHeaderCase_enabled_true() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreHeaderCase();
        assertTrue(format.getIgnoreHeaderCase());
        assertFalse(CSVFormat.DEFAULT.getIgnoreHeaderCase());
    }

    // Tests withNullString sets null string
    @Test
    public void testWithNullString_validString_returnsCorrectNullString() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("N/A");
        assertEquals("N/A", format.getNullString());
        assertNull(CSVFormat.DEFAULT.getNullString());
    }

    // Tests withRecordSeparator sets record separator
    @Test
    public void testWithRecordSeparator_validString_returnsCorrectSeparator() {
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator("\n");
        assertEquals("\n", format.getRecordSeparator());
        assertEquals("\r\n", CSVFormat.DEFAULT.getRecordSeparator());
    }

    // Tests withCommentMarker sets comment marker
    @Test
    public void testWithCommentMarker_validCharacter_returnsNewFormat() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        assertEquals(Character.valueOf('#'), format.getCommentMarker());
    }

    // Tests withQuoteMode sets quote mode
    @Test
    public void testWithQuoteMode_nonNullMode_returnsCorrectMode() {
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.NONE);
        assertEquals(QuoteMode.NONE, format.getQuoteMode());
    }

    // Tests valueOf with predefined format name
    @Test
    public void testValueOf_predefinedName_returnsCorrectFormat() {
        CSVFormat format = CSVFormat.valueOf("Default");
        assertEquals(CSVFormat.DEFAULT, format);
    }

    // Tests format method with values
    @Test
    public void testFormat_twoValues_returnsFormattedString() {
        String result = CSVFormat.DEFAULT.format("a", "b");
        assertEquals("a,b", result);
    }

    // Tests toString returns non-null string
    @Test
    public void testToString_defaultFormat_containsDelimiter() {
        String str = CSVFormat.DEFAULT.toString();
        assertTrue(str.contains("Delimiter=<,>"));
    }

    // Tests equals with same format returns true
    @Test
    public void testEquals_sameFormat_returnsTrue() {
        assertEquals(CSVFormat.DEFAULT, CSVFormat.DEFAULT);
    }

    // Tests equals with different format returns false
    @Test
    public void testEquals_differentFormat_returnsFalse() {
        assertFalse(CSVFormat.DEFAULT.equals(CSVFormat.RFC4180));
    }

    // Tests equals with null returns false
    @Test
    public void testEquals_null_returnsFalse() {
        assertFalse(CSVFormat.DEFAULT.equals(null));
    }

    // Tests equals with different type returns false
    @Test
    public void testEquals_differentType_returnsFalse() {
        assertFalse(CSVFormat.DEFAULT.equals("some string"));
    }

    // Tests isCommentMarkerSet returns false when not set
    @Test
    public void testIsCommentMarkerSet_notSet_returnsFalse() {
        assertFalse(CSVFormat.DEFAULT.isCommentMarkerSet());
    }

    // Tests isEscapeCharacterSet returns false when not set
    @Test
    public void testIsEscapeCharacterSet_notSet_returnsFalse() {
        assertFalse(CSVFormat.DEFAULT.isEscapeCharacterSet());
    }

    // Tests isNullStringSet returns false when not set
    @Test
    public void testIsNullStringSet_notSet_returnsFalse() {
        assertFalse(CSVFormat.DEFAULT.isNullStringSet());
    }

    // Tests isQuoteCharacterSet returns true when set
    @Test
    public void testIsQuoteCharacterSet_set_returnsTrue() {
        assertTrue(CSVFormat.DEFAULT.isQuoteCharacterSet());
    }

    // Tests validate throws when delimiter is line break
    @Test(expected = IllegalArgumentException.class)
    public void testValidate_delimiterIsLineBreak_throwsIllegalArgumentException() {
        CSVFormat.newFormat('\n');
    }

    // Tests validate throws when quote and delimiter are same
    @Test(expected = IllegalArgumentException.class)
    public void testValidate_quoteEqualsDelimiter_throwsIllegalArgumentException() {
        CSVFormat.newFormat(',').withQuote(',');
    }

    // Tests validate throws when escape and delimiter are same
    @Test(expected = IllegalArgumentException.class)
    public void testValidate_escapeEqualsDelimiter_throwsIllegalArgumentException() {
        CSVFormat.newFormat('.').withEscape('.');
    }

    // Tests validate throws when comment marker equals delimiter
    @Test(expected = IllegalArgumentException.class)
    public void testValidate_commentMarkerEqualsDelimiter_throwsIllegalArgumentException() {
        CSVFormat.newFormat('|').withCommentMarker('|');
    }

    // Tests validate throws when comment marker equals quote
    @Test(expected = IllegalArgumentException.class)
    public void testValidate_commentMarkerEqualsQuote_throwsIllegalArgumentException() {
        CSVFormat.DEFAULT.withCommentMarker('"');
    }

    // Tests validate throws when comment marker equals escape
    @Test(expected = IllegalArgumentException.class)
    public void testValidate_commentMarkerEqualsEscape_throwsIllegalArgumentException() {
        CSVFormat.DEFAULT.withEscape('\\').withCommentMarker('\\');
    }

    // Tests validate throws when escape is null and quote mode is NONE
    @Test(expected = IllegalArgumentException.class)
    public void testValidate_noEscapeQuoteModeNone_throwsIllegalArgumentException() {
        CSVFormat.newFormat(',').withQuoteMode(QuoteMode.NONE);
    }

    // Tests validate passes when escape is set and quote mode is NONE
    @Test
    public void testValidate_escapeSetQuoteModeNone_createsFormat() {
        CSVFormat format = CSVFormat.newFormat(',').withEscape('\\').withQuoteMode(QuoteMode.NONE);
        assertEquals(QuoteMode.NONE, format.getQuoteMode());
        assertEquals(Character.valueOf('\\'), format.getEscapeCharacter());
    }
}