package org.apache.commons.csv;

import static org.junit.Assert.*;

import org.junit.Test;

public class CSVFormatTest {

    // Tests that newFormat throws IllegalArgumentException for line break delimiter
    @Test(expected = IllegalArgumentException.class)
    public void testNewFormat_lineBreak_throwsIllegalArgumentException() {
        CSVFormat.newFormat('\n');
    }

    // Tests that withDelimiter throws IllegalArgumentException for line break
    @Test(expected = IllegalArgumentException.class)
    public void testWithDelimiter_lineBreak_throwsIllegalArgumentException() {
        CSVFormat.DEFAULT.withDelimiter('\r');
    }

    // Tests that withQuoteChar throws IllegalArgumentException for line break
    @Test(expected = IllegalArgumentException.class)
    public void testWithQuoteChar_lineBreak_throwsIllegalArgumentException() {
        CSVFormat.DEFAULT.withQuoteChar('\n');
    }

    // Tests that withEscape throws IllegalArgumentException for line break
    @Test(expected = IllegalArgumentException.class)
    public void testWithEscape_lineBreak_throwsIllegalArgumentException() {
        CSVFormat.DEFAULT.withEscape('\r');
    }

    // Tests that withCommentStart throws IllegalArgumentException for line break
    @Test(expected = IllegalArgumentException.class)
    public void testWithCommentStart_lineBreak_throwsIllegalArgumentException() {
        CSVFormat.DEFAULT.withCommentStart('\n');
    }

    // Tests validate() throws IllegalStateException when quoteChar equals delimiter
    @Test(expected = IllegalStateException.class)
    public void testValidate_quoteCharEqualsDelimiter_throwsIllegalStateException() {
        CSVFormat format = CSVFormat.newFormat(',').withQuoteChar(',');
        format.validate();
    }

    // Tests validate() throws IllegalStateException when escape equals delimiter
    @Test(expected = IllegalStateException.class)
    public void testValidate_escapeEqualsDelimiter_throwsIllegalStateException() {
        CSVFormat format = CSVFormat.newFormat(',').withEscape(',');
        format.validate();
    }

    // Tests validate() throws IllegalStateException when commentStart equals delimiter
    @Test(expected = IllegalStateException.class)
    public void testValidate_commentStartEqualsDelimiter_throwsIllegalStateException() {
        CSVFormat format = CSVFormat.newFormat(',').withCommentStart(',');
        format.validate();
    }

    // Tests validate() throws IllegalStateException when quoteChar equals commentStart
    @Test(expected = IllegalStateException.class)
    public void testValidate_quoteCharEqualsCommentStart_throwsIllegalStateException() {
        CSVFormat format = CSVFormat.DEFAULT.withQuoteChar('#').withCommentStart('#');
        format.validate();
    }

    // Tests validate() throws IllegalStateException when escape equals commentStart
    @Test(expected = IllegalStateException.class)
    public void testValidate_escapeEqualsCommentStart_throwsIllegalStateException() {
        CSVFormat format = CSVFormat.DEFAULT.withEscape('#').withCommentStart('#');
        format.validate();
    }

    // Tests validate() throws IllegalStateException when escape is null and quotePolicy is NONE
    @Test(expected = IllegalStateException.class)
    public void testValidate_escapeNullAndQuotePolicyNone_throwsIllegalStateException() {
        CSVFormat format = CSVFormat.DEFAULT.withEscape((Character) null).withQuotePolicy(Quote.NONE);
        format.validate();
    }

    // Tests validate() throws IllegalStateException when header contains duplicates
    @Test(expected = IllegalStateException.class)
    public void testValidate_headerDuplicate_throwsIllegalStateException() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("a", "b", "a");
        format.validate();
    }

    // Tests validate() does not throw when format is valid
    @Test
    public void testValidate_validFormat_noException() {
        CSVFormat.DEFAULT.validate();
        CSVFormat.RFC4180.validate();
        CSVFormat.EXCEL.validate();
        CSVFormat.TDF.validate();
        CSVFormat.MYSQL.validate();
    }

    // Tests equals returns true for same object
    @Test
    public void testEquals_sameObject_true() {
        CSVFormat format = CSVFormat.DEFAULT;
        assertTrue(format.equals(format));
    }

    // Tests equals returns true for equal formats
    @Test
    public void testEquals_equalFormats_true() {
        CSVFormat f1 = CSVFormat.DEFAULT;
        CSVFormat f2 = CSVFormat.DEFAULT;
        assertTrue(f1.equals(f2));
        assertTrue(f2.equals(f1));
    }

    // Tests equals returns false for formats with different delimiter
    @Test
    public void testEquals_differentDelimiter_false() {
        CSVFormat f1 = CSVFormat.DEFAULT;
        CSVFormat f2 = CSVFormat.DEFAULT.withDelimiter(';');
        assertFalse(f1.equals(f2));
    }

    // Tests hashCode consistency with equals
    @Test
    public void testHashCode_consistentWithEquals() {
        CSVFormat f1 = CSVFormat.DEFAULT;
        CSVFormat f2 = CSVFormat.DEFAULT;
        assertEquals(f1.hashCode(), f2.hashCode());
    }

    // Tests format() produces correct output
    @Test
    public void testFormat_simpleValues_formattedCorrectly() {
        assertEquals("hello,world", CSVFormat.DEFAULT.format("hello", "world"));
        assertEquals("a,,c", CSVFormat.DEFAULT.format("a", null, "c"));
    }

    // Tests withHeader(null) produces null header
    @Test
    public void testWithHeader_null_getHeaderReturnsNull() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader((String[]) null);
        assertNull(format.getHeader());
    }

    // Tests withHeader creates defensive copy
    @Test
    public void testWithHeader_array_returnsCopy() {
        String[] original = {"a", "b"};
        CSVFormat format = CSVFormat.DEFAULT.withHeader(original);
        original[0] = "changed";
        String[] headerFromFormat = format.getHeader();
        assertEquals("a", headerFromFormat[0]);
        assertEquals("b", headerFromFormat[1]);
        assertNotSame(original, headerFromFormat);
    }

    // ========== New test cases for uncovered parts ==========

    // Tests withDelimiter does not throw when delimiter is valid
    @Test
    public void testWithDelimiter_valid_noException() {
        assertNotNull(CSVFormat.DEFAULT.withDelimiter(';'));
    }

    // Tests withQuoteChar does not throw when quote char is valid
    @Test
    public void testWithQuoteChar_valid_noException() {
        assertNotNull(CSVFormat.DEFAULT.withQuoteChar('"'));
    }

    // Tests withEscape does not throw when escape is valid
    @Test
    public void testWithEscape_valid_noException() {
        assertNotNull(CSVFormat.DEFAULT.withEscape('\\'));
    }

    // Tests withCommentStart does not throw when comment start is valid
    @Test
    public void testWithCommentStart_valid_noException() {
        assertNotNull(CSVFormat.DEFAULT.withCommentStart('#'));
    }

    // Tests withDelimiter throws IllegalArgumentException for delimiter that is a letter
    @Test(expected = IllegalArgumentException.class)
    public void testWithDelimiter_letter_throwsIllegalArgumentException() {
        CSVFormat.DEFAULT.withDelimiter('a');
    }

    // Tests withQuoteChar throws IllegalArgumentException for delimiter character
    @Test(expected = IllegalArgumentException.class)
    public void testWithQuoteChar_delimiter_throwsIllegalArgumentException() {
        CSVFormat.DEFAULT.withQuoteChar(',');
    }

    // Tests withEscape throws IllegalArgumentException for delimiter character
    @Test(expected = IllegalArgumentException.class)
    public void testWithEscape_delimiter_throwsIllegalArgumentException() {
        CSVFormat.DEFAULT.withEscape(',');
    }

    // Tests withCommentStart throws IllegalArgumentException for delimiter character
    @Test(expected = IllegalArgumentException.class)
    public void testWithCommentStart_delimiter_throwsIllegalArgumentException() {
        CSVFormat.DEFAULT.withCommentStart(',');
    }

    // Tests getDelimiter returns correct value
    @Test
    public void testGetDelimiter() {
        CSVFormat format = CSVFormat.newFormat(';');
        assertEquals(';', format.getDelimiter());
    }

    // Tests withHeader with prepend and append values
    @Test
    public void testWithHeader_prependAppend() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("a").withHeader("b");
        assertArrayEquals(new String[] {"b"}, format.getHeader());
    }

    // Tests withHeader with boolean parameters (false)
    @Test
    public void testWithHeader_booleanFalse() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader(false);
        assertNull(format.getHeader());
    }

    // Tests withHeader with boolean parameters (true)
    @Test
    public void testWithHeader_booleanTrue() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader(true);
        assertNotNull(format.getHeader());
    }

    // Tests toString returns non-null and contains delimiter info
    @Test
    public void testToString_containsDelimiter() {
        String str = CSVFormat.DEFAULT.toString();
        assertTrue(str.contains("Delimiter=<,>"));
    }

    // Tests equals returns false when comparing with null or different type
    @Test
    public void testEquals_nullOrDifferentType_false() {
        CSVFormat format = CSVFormat.DEFAULT;
        assertFalse(format.equals(null));
        assertFalse(format.equals("some string"));
    }

    // Tests equals returns false when ignoreSurroundingSpaces differs
    @Test
    public void testEquals_differentIgnoreSurroundingSpaces_false() {
        CSVFormat f1 = CSVFormat.DEFAULT;
        CSVFormat f2 = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(true);
        assertFalse(f1.equals(f2));
    }

    // Tests withIgnoreSurroundingSpaces works
    @Test
    public void testWithIgnoreSurroundingSpaces() {
        assertTrue(CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(true).getIgnoreSurroundingSpaces());
    }

    // Tests withIgnoreEmptyLines works
    @Test
    public void testWithIgnoreEmptyLines() {
        assertFalse(CSVFormat.DEFAULT.withIgnoreEmptyLines(false).getIgnoreEmptyLines());
    }

    // Tests withNullString works
    @Test
    public void testWithNullString() {
        assertEquals("NULL", CSVFormat.DEFAULT.withNullString("NULL").getNullString());
    }

    // Tests withQuotePolicy works
    @Test
    public void testWithQuotePolicy() {
        assertEquals(Quote.ALL, CSVFormat.DEFAULT.withQuotePolicy(Quote.ALL).getQuotePolicy());
    }

    // Tests withRecordSeparator works
    @Test
    public void testWithRecordSeparator() {
        assertEquals("\r\n", CSVFormat.DEFAULT.withRecordSeparator("\r\n").getRecordSeparator());
    }

    // Tests withSkipHeaderRecord works
    @Test
    public void testWithSkipHeaderRecord() {
        assertTrue(CSVFormat.DEFAULT.withSkipHeaderRecord(true).getSkipHeaderRecord());
    }

    // Tests withIgnoreHeaderCase works (if applicable)
    @Test
    public void testWithIgnoreHeaderCase() {
        assertTrue(CSVFormat.DEFAULT.withIgnoreHeaderCase(true).getIgnoreHeaderCase());
    }

    // Tests withTrailingDelimiter works
    @Test
    public void testWithTrailingDelimiter() {
        assertTrue(CSVFormat.DEFAULT.withTrailingDelimiter(true).getTrailingDelimiter());
    }

    // Tests withTrim works
    @Test
    public void testWithTrim() {
        assertTrue(CSVFormat.DEFAULT.withTrim(true).getTrim());
    }

    // Tests withAllowMissingColumnNames works
    @Test
    public void testWithAllowMissingColumnNames() {
        assertTrue(CSVFormat.DEFAULT.withAllowMissingColumnNames(true).getAllowMissingColumnNames());
    }

    // Tests withAutoFlush works (if applicable)
    @Test
    public void testWithAutoFlush() {
        assertTrue(CSVFormat.DEFAULT.withAutoFlush(true).getAutoFlush());
    }

    // Tests withQuoteMode works
    @Test
    public void testWithQuoteMode() {
        assertEquals(QuoteMode.ALL, CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL).getQuoteMode());
    }

    // Tests withCommentMarker works
    @Test
    public void testWithCommentMarker() {
        assertEquals('#', (char) CSVFormat.DEFAULT.withCommentMarker('#').getCommentMarker().charValue());
    }

    // Tests withEscapeCharacter works
    @Test
    public void testWithEscapeCharacter() {
        assertEquals('\\', (char) CSVFormat.DEFAULT.withEscapeCharacter('\\').getEscapeCharacter().charValue());
    }
}