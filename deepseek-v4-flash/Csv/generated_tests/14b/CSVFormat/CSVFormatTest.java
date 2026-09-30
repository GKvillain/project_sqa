package org.apache.commons.csv;

import static org.junit.Assert.*;

import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;

public class CSVFormatTest {

    // Test normal format with simple values
    @Test
    public void testFormat_NormalValues_ReturnsCSV() {
        CSVFormat format = CSVFormat.DEFAULT;
        String result = format.format("a", "b", "c");
        assertEquals("a,b,c", result);
    }

    // Test format with null value uses nullString when set
    @Test
    public void testFormat_NullWithNullString_UsesNullString() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        String result = format.format("a", null, "c");
        assertEquals("a,NULL,c", result);
    }

    // Test format with empty string
    @Test
    public void testFormat_EmptyValue_OutputsEmptyToken() {
        CSVFormat format = CSVFormat.DEFAULT;
        String result = format.format("", "b");
        assertEquals(",b", result); // empty token, then b
    }

    // Test print method with null value and no nullString
    @Test
    public void testPrint_NullValueNoNullString_WritesEmpty() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT;
        StringWriter sw = new StringWriter();
        format.print(null, sw, true);
        assertEquals("", sw.toString());
    }

    // Test print with null value and nullString
    @Test
    public void testPrint_NullValueWithNullString_WritesNullString() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("\\N");
        StringWriter sw = new StringWriter();
        format.print(null, sw, true);
        assertEquals("\\N", sw.toString());
    }

    // Test print with quoting needed (MINIMAL mode, start char <= COMMENT)
    @Test
    public void testPrintAndQuote_MINIMAL_StartWithCommentChar_Quotes() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withQuote('"');
        StringWriter sw = new StringWriter();
        format.print("#comment", sw, true);
        assertEquals("\"#comment\"", sw.toString());
    }

    // Test print with quoting needed (MINIMAL mode, contains delimiter)
    @Test
    public void testPrintAndQuote_MINIMAL_ContainsDelimiter_Quotes() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withQuote('"');
        StringWriter sw = new StringWriter();
        format.print("a,b", sw, true);
        assertEquals("\"a,b\"", sw.toString());
    }

    // Test print with quoting needed (MINIMAL mode, first token empty and newRecord)
    @Test
    public void testPrintAndQuote_MINIMAL_EmptyFirstToken_Quotes() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withQuote('"');
        StringWriter sw = new StringWriter();
        format.print("", sw, true);
        assertEquals("\"\"", sw.toString()); // empty token at start of record should be quoted
    }

    // Test print with no quoting needed (MINIMAL mode, normal alphanumeric)
    @Test
    public void testPrintAndQuote_MINIMAL_NormalValue_NoQuote() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withQuote('"');
        StringWriter sw = new StringWriter();
        format.print("hello", sw, true);
        assertEquals("hello", sw.toString());
    }

    // Test print with escape needed when quoting disabled
    @Test
    public void testPrintAndEscape_ContainsDelimiter_Escapes() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withQuote(null).withEscape('\\');
        StringWriter sw = new StringWriter();
        format.print("a,b", sw, true);
        assertEquals("a\\,b", sw.toString());
    }

    // Test print with quoteMode NONE and no escape throws during validation
    @Test(expected = IllegalArgumentException.class)
    public void testValidate_NoEscapeAndQuoteModeNone_Throws() {
        CSVFormat.DEFAULT.withQuoteMode(QuoteMode.NONE).withQuote(null).withEscape(null);
    }

    // Test delimiter cannot be line break
    @Test(expected = IllegalArgumentException.class)
    public void testWithDelimiter_LineBreak_Throws() {
        CSVFormat.DEFAULT.withDelimiter('\n');
    }

    // Test quote character cannot be line break
    @Test(expected = IllegalArgumentException.class)
    public void testWithQuote_LineBreak_Throws() {
        CSVFormat.DEFAULT.withQuote('\n');
    }

    // Test escape character cannot be line break
    @Test(expected = IllegalArgumentException.class)
    public void testWithEscape_LineBreak_Throws() {
        CSVFormat.DEFAULT.withEscape('\n');
    }

    // Test comment marker cannot be line break
    @Test(expected = IllegalArgumentException.class)
    public void testWithCommentMarker_LineBreak_Throws() {
        CSVFormat.DEFAULT.withCommentMarker('\n');
    }

    // Test duplicate header throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testValidate_DuplicateHeader_Throws() {
        CSVFormat.DEFAULT.withHeader("a", "b", "a");
    }

    // Test equals with identical formats
    @Test
    public void testEquals_SameFormat_ReturnsTrue() {
        CSVFormat f1 = CSVFormat.DEFAULT;
        CSVFormat f2 = CSVFormat.DEFAULT;
        assertTrue(f1.equals(f2));
    }

    // Test equals with different delimiter
    @Test
    public void testEquals_DifferentDelimiter_ReturnsFalse() {
        CSVFormat f1 = CSVFormat.DEFAULT;
        CSVFormat f2 = CSVFormat.DEFAULT.withDelimiter(';');
        assertFalse(f1.equals(f2));
    }

    // Test equals with null object
    @Test
    public void testEquals_NullObject_ReturnsFalse() {
        CSVFormat f = CSVFormat.DEFAULT;
        assertFalse(f.equals(null));
    }

    // Test equals with different class
    @Test
    public void testEquals_DifferentClass_ReturnsFalse() {
        CSVFormat f = CSVFormat.DEFAULT;
        assertFalse(f.equals("string"));
    }

    // Test hashCode consistency
    @Test
    public void testHashCode_ConsistentWithEquals() {
        CSVFormat f1 = CSVFormat.DEFAULT;
        CSVFormat f2 = CSVFormat.DEFAULT;
        assertEquals(f1.hashCode(), f2.hashCode());
    }

    // Test toString contains delimiter
    @Test
    public void testToString_ContainsDelimiter() {
        String str = CSVFormat.DEFAULT.toString();
        assertTrue(str.contains("Delimiter=<,>"));
    }

    // Test withFirstRecordAsHeader sets skipHeaderRecord and empty header
    @Test
    public void testWithFirstRecordAsHeader_SetsSkipHeaderAndEmptyHeader() {
        CSVFormat format = CSVFormat.DEFAULT.withFirstRecordAsHeader();
        assertTrue(format.getSkipHeaderRecord());
        assertArrayEquals(new String[0], format.getHeader()); // empty array for auto-read
    }

    // Test withHeader from enum
    @Test
    public void testWithHeader_Enum_CreatesHeaderArray() {
        enum TestHeader { Name, Age }
        CSVFormat format = CSVFormat.DEFAULT.withHeader(TestHeader.class);
        assertArrayEquals(new String[]{"Name", "Age"}, format.getHeader());
    }

    // Test withHeaderComments stores strings
    @Test
    public void testWithHeaderComments_StoresComments() {
        CSVFormat format = CSVFormat.DEFAULT.withHeaderComments("comment1", "comment2");
        assertArrayEquals(new String[]{"comment1", "comment2"}, format.getHeaderComments());
    }

    // Test getHeader returns a copy (modifying returned array does not affect format)
    @Test
    public void testGetHeader_ReturnsCopy() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("a", "b");
        String[] header = format.getHeader();
        header[0] = "modified";
        assertArrayEquals(new String[]{"a", "b"}, format.getHeader());
    }

    // Test print with trim=true trims spaces
    @Test
    public void testPrint_TrimEnabled_TrimsSpaces() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withTrim(true);
        StringWriter sw = new StringWriter();
        format.print("  hello  ", sw, true);
        assertEquals("hello", sw.toString());
    }

    // Test print with quoting end character <= SP (e.g., space)
    @Test
    public void testPrintAndQuote_MINIMAL_EndsWithSpace_Quotes() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withQuote('"');
        StringWriter sw = new StringWriter();
        format.print("hello ", sw, true);
        assertEquals("\"hello \"", sw.toString());
    }

    // Test print with quoting for newRecord empty token
    @Test
    public void testPrintAndQuote_MINIMAL_NewRecordEmptyToken_Quotes() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withQuote('"');
        StringWriter sw = new StringWriter();
        format.print("", sw, true);
        assertEquals("\"\"", sw.toString());
    }

    // ========== New tests for uncovered parts ==========

    // Test format with values that need escaping when quoting disabled
    @Test
    public void testFormat_WithEscapeAndQuoteDisabled_EscapesDelimiter() {
        CSVFormat format = CSVFormat.DEFAULT.withQuote(null).withEscape('\\');
        String result = format.format("a,b", "c");
        assertEquals("a\\,b,c", result);
    }

    // Test validate: null delimiter throws
    @Test(expected = IllegalArgumentException.class)
    public void testValidate_NullDelimiter_Throws() {
        CSVFormat.DEFAULT.withDelimiter('\0');
    }

    // Test validate: null quote and null escape with delimiter in value should throw
    @Test(expected = IllegalArgumentException.class)
    public void testValidate_NoQuoteNoEscapeWithDelimiterInValue_Throws() {
        CSVFormat format = CSVFormat.newFormat(',');
        format.validate();
    }
}