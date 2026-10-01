package org.apache.commons.csv;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CSVFormatTest {

    // Tests normal creation of a format with a valid delimiter
    @Test
    public void testNewFormat_validDelimiter_createsFormat() {
        CSVFormat format = CSVFormat.newFormat(',');
        assertEquals(',', format.getDelimiter());
        assertNull(format.getQuoteCharacter());
        assertFalse(format.isQuoteCharacterSet());
    }

    // Tests that a line break delimiter throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testNewFormat_lineBreakDelimiter_throwsException() {
        CSVFormat.newFormat('\n');
    }

    // Tests that withDelimiter with a line break throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testWithDelimiter_lineBreak_throwsException() {
        CSVFormat.DEFAULT.withDelimiter('\r');
    }

    // Tests that withQuote with a line break throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testWithQuote_lineBreak_throwsException() {
        CSVFormat.DEFAULT.withQuote('\n');
    }

    // Tests that withCommentMarker with a line break throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testWithCommentMarker_lineBreak_throwsException() {
        CSVFormat.DEFAULT.withCommentMarker('\n');
    }

    // Tests that withEscape with a line break throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testWithEscape_lineBreak_throwsException() {
        CSVFormat.DEFAULT.withEscape('\n');
    }

    // Tests that delimiter equals quote character throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testDelimiterEqualsQuote_throwsException() {
        CSVFormat.newFormat(',').withQuote(',');
    }

    // Tests that delimiter equals escape character throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testDelimiterEqualsEscape_throwsException() {
        CSVFormat.newFormat(',').withEscape(',');
    }

    // Tests that delimiter equals comment marker throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testDelimiterEqualsCommentMarker_throwsException() {
        CSVFormat.newFormat(',').withCommentMarker(',');
    }

    // Tests that quote character equals comment marker throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testQuoteEqualsCommentMarker_throwsException() {
        CSVFormat.newFormat(',').withQuote('#').withCommentMarker('#');
    }

    // Tests that escape character equals comment marker throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testEscapeEqualsCommentMarker_throwsException() {
        CSVFormat.newFormat(',').withEscape('#').withCommentMarker('#');
    }

    // Tests that duplicate header entries throw IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testDuplicateHeader_throwsException() {
        CSVFormat.DEFAULT.withHeader("a", "a");
    }

    // Tests that default header is null
    @Test
    public void testHeader_null_default_returnsNull() {
        assertNull(CSVFormat.DEFAULT.getHeader());
    }

    // Tests that header with values returns a clone of the array
    @Test
    public void testHeader_withValues_returnsClone() {
        String[] header = {"Name", "Age"};
        CSVFormat format = CSVFormat.DEFAULT.withHeader(header);
        String[] result = format.getHeader();
        assertNotNull(result);
        assertNotSame(header, result);
        assertArrayEquals(header, result);
        // Verify that modifying the returned array does not affect internal state
        result[0] = "Changed";
        assertArrayEquals(header, format.getHeader());
    }

    // Tests default format properties
    @Test
    public void testDefaultProperties() {
        CSVFormat fmt = CSVFormat.DEFAULT;
        assertTrue(fmt.getIgnoreEmptyLines());
        assertFalse(fmt.getIgnoreSurroundingSpaces());
        assertFalse(fmt.getSkipHeaderRecord());
        assertFalse(fmt.getAllowMissingColumnNames());
        assertEquals(',', fmt.getDelimiter());
        assertEquals(Character.valueOf('"'), fmt.getQuoteCharacter());
        assertTrue(fmt.isQuoteCharacterSet());
        assertFalse(fmt.isEscapeCharacterSet());
        assertFalse(fmt.isCommentMarkerSet());
        assertFalse(fmt.isNullStringSet());
    }

    // Tests equals with same object
    @Test
    public void testEquals_sameObject_true() {
        CSVFormat fmt = CSVFormat.DEFAULT;
        assertTrue(fmt.equals(fmt));
    }

    // Tests equals with different delimiter
    @Test
    public void testEquals_differentDelimiter_false() {
        assertFalse(CSVFormat.DEFAULT.equals(CSVFormat.newFormat(';')));
    }

    // Tests format method produces expected output
    @Test
    public void testFormat_simpleValues() {
        String result = CSVFormat.DEFAULT.format("a", "b");
        assertEquals("a,b", result);
    }

    // Tests that withQuoteMode(NONE) and no escape character throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testWithQuoteModeNoneAndEscapeNull_throwsException() {
        CSVFormat.DEFAULT.withQuoteMode(QuoteMode.NONE);
    }

    // Tests that withQuoteMode(NONE) with an escape character works
    @Test
    public void testWithQuoteModeNoneAndEscapeSet_valid() {
        CSVFormat format = CSVFormat.DEFAULT.withEscape('\\').withQuoteMode(QuoteMode.NONE);
        assertEquals(QuoteMode.NONE, format.getQuoteMode());
        assertEquals(Character.valueOf('\\'), format.getEscapeCharacter());
    }

    // ================== New test cases for uncovered areas ==================

    // Tests withIgnoreEmptyLines
    @Test
    public void testWithIgnoreEmptyLines() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreEmptyLines(true);
        assertTrue(format.getIgnoreEmptyLines());
        format = CSVFormat.DEFAULT.withIgnoreEmptyLines(false);
        assertFalse(format.getIgnoreEmptyLines());
    }

    // Tests withIgnoreSurroundingSpaces
    @Test
    public void testWithIgnoreSurroundingSpaces() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(true);
        assertTrue(format.getIgnoreSurroundingSpaces());
        format = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(false);
        assertFalse(format.getIgnoreSurroundingSpaces());
    }

    // Tests withSkipHeaderRecord
    @Test
    public void testWithSkipHeaderRecord() {
        CSVFormat format = CSVFormat.DEFAULT.withSkipHeaderRecord(true);
        assertTrue(format.getSkipHeaderRecord());
        format = CSVFormat.DEFAULT.withSkipHeaderRecord(false);
        assertFalse(format.getSkipHeaderRecord());
    }

    // Tests withAllowMissingColumnNames
    @Test
    public void testWithAllowMissingColumnNames() {
        CSVFormat format = CSVFormat.DEFAULT.withAllowMissingColumnNames(true);
        assertTrue(format.getAllowMissingColumnNames());
        format = CSVFormat.DEFAULT.withAllowMissingColumnNames(false);
        assertFalse(format.getAllowMissingColumnNames());
    }

    // Tests withNullString
    @Test
    public void testWithNullString() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        assertEquals("NULL", format.getNullString());
        assertTrue(format.isNullStringSet());
    }

    // Tests withTrailingDelimiter
    @Test
    public void testWithTrailingDelimiter() {
        CSVFormat format = CSVFormat.DEFAULT.withTrailingDelimiter(true);
        assertTrue(format.getTrailingDelimiter());
        format = CSVFormat.DEFAULT.withTrailingDelimiter(false);
        assertFalse(format.getTrailingDelimiter());
    }

    // Tests withRecordSeparator (char)
    @Test
    public void testWithRecordSeparator_char() {
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator('\n');
        assertEquals("\n", format.getRecordSeparator());
    }

    // Tests withRecordSeparator (String)
    @Test
    public void testWithRecordSeparator_string() {
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator("\r\n");
        assertEquals("\r\n", format.getRecordSeparator());
    }

    // Tests format with null values
    @Test
    public void testFormatWithNullValues() {
        String result = CSVFormat.DEFAULT.format("a", null, "b");
        assertEquals("a,,b", result);
    }

    // Tests withQuoteMode ALL
    @Test
    public void testWithQuoteModeAll() {
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL);
        assertEquals(QuoteMode.ALL, format.getQuoteMode());
        String result = format.format("a", "b");
        assertEquals("\"a\",\"b\"", result);
    }

    // Tests withQuoteMode MINIMAL
    @Test
    public void testWithQuoteModeMinimal() {
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.MINIMAL);
        assertEquals(QuoteMode.MINIMAL, format.getQuoteMode());
        // Simple fields without special characters should not be quoted
        String result = format.format("a", "b");
        assertEquals("a,b", result);
        // Field containing delimiter should be quoted
        result = format.format("a,b", "c");
        assertEquals("\"a,b\",c", result);
    }

    // Tests withHeader with empty array
    @Test
    public void testWithHeader_emptyArray_returnsEmptyHeader() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader();
        assertNotNull(format.getHeader());
        assertEquals(0, format.getHeader().length);
    }

    // Tests hashCode consistency
    @Test
    public void testHashCode_consistent() {
        CSVFormat format1 = CSVFormat.DEFAULT;
        CSVFormat format2 = CSVFormat.DEFAULT;
        assertEquals(format1.hashCode(), format2.hashCode());
        // Changing a property should change hash code
        CSVFormat format3 = CSVFormat.DEFAULT.withDelimiter(';');
        assertNotEquals(format1.hashCode(), format3.hashCode());
    }

    // Tests withHeaderComments
    @Test
    public void testWithHeaderComments() {
        String[] comments = {"Comment1", "Comment2"};
        CSVFormat format = CSVFormat.DEFAULT.withHeaderComments(comments);
        assertArrayEquals(comments, format.getHeaderComments());
    }
}