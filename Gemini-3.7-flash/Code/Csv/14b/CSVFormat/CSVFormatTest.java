package org.apache.commons.csv;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;

import org.junit.Test;

public class CSVFormatTest {

    // Tests predefined formats configuration
    @Test
    public void testPredefinedFormats_standardConfigurations_matchExpectedProperties() {
        assertEquals(',', CSVFormat.DEFAULT.getDelimiter());
        assertEquals(Character.valueOf('"'), CSVFormat.DEFAULT.getQuoteCharacter());
        assertEquals("\r\n", CSVFormat.DEFAULT.getRecordSeparator());
        assertTrue(CSVFormat.DEFAULT.getIgnoreEmptyLines());

        assertEquals('\t', CSVFormat.TDF.getDelimiter());
        assertTrue(CSVFormat.TDF.getIgnoreSurroundingSpaces());

        assertEquals('\t', CSVFormat.MYSQL.getDelimiter());
        assertEquals(Character.valueOf('\\'), CSVFormat.MYSQL.getEscapeCharacter());
        assertNull(CSVFormat.MYSQL.getQuoteCharacter());
        assertEquals("\n", CSVFormat.MYSQL.getRecordSeparator());
        assertEquals("\\N", CSVFormat.MYSQL.getNullString());

        assertEquals(CSVFormat.DEFAULT, CSVFormat.valueOf("Default"));
        assertEquals(CSVFormat.RFC4180, CSVFormat.valueOf("RFC4180"));
        assertEquals(CSVFormat.EXCEL, CSVFormat.valueOf("Excel"));
    }

    // Tests withDelimiter and newFormat
    @Test
    public void testWithDelimiter_validChar_createsNewFormat() {
        final CSVFormat format = CSVFormat.newFormat(';').withQuote('\'');
        assertEquals(';', format.getDelimiter());
        assertEquals(Character.valueOf('\''), format.getQuoteCharacter());
        assertFalse(format.getIgnoreEmptyLines());
    }

    // Tests validation exception when delimiter is a line break character
    @Test(expected = IllegalArgumentException.class)
    public void testWithDelimiter_lineBreakLF_throwsIllegalArgumentException() {
        CSVFormat.DEFAULT.withDelimiter('\n');
    }

    // Tests validation exception when delimiter equals quote character
    @Test(expected = IllegalArgumentException.class)
    public void testValidate_quoteCharEqualsDelimiter_throwsIllegalArgumentException() {
        CSVFormat.DEFAULT.withQuote(';').withDelimiter(';');
    }

    // Tests validation exception when delimiter equals escape character
    @Test(expected = IllegalArgumentException.class)
    public void testValidate_escapeEqualsDelimiter_throwsIllegalArgumentException() {
        CSVFormat.DEFAULT.withEscape(';').withDelimiter(';');
    }

    // Tests validation exception when delimiter equals comment marker
    @Test(expected = IllegalArgumentException.class)
    public void testValidate_commentMarkerEqualsDelimiter_throwsIllegalArgumentException() {
        CSVFormat.DEFAULT.withCommentMarker(';').withDelimiter(';');
    }

    // Tests validation exception when quote equals comment marker
    @Test(expected = IllegalArgumentException.class)
    public void testValidate_quoteCharEqualsCommentMarker_throwsIllegalArgumentException() {
        CSVFormat.DEFAULT.withQuote('!').withCommentMarker('!');
    }

    // Tests validation exception when QuoteMode is NONE without escape character
    @Test(expected = IllegalArgumentException.class)
    public void testValidate_quoteModeNoneWithoutEscape_throwsIllegalArgumentException() {
        CSVFormat.DEFAULT.withQuoteMode(QuoteMode.NONE).withEscape((Character) null);
    }

    // Tests validation exception when header array contains duplicate column names
    @Test(expected = IllegalArgumentException.class)
    public void testWithHeader_duplicateNames_throwsIllegalArgumentException() {
        CSVFormat.DEFAULT.withHeader("col1", "col2", "col1");
    }

    // Tests withHeader variations including enum and flags
    @Test
    public void testWithHeader_validInputs_setsHeadersCorrectly() {
        final CSVFormat format = CSVFormat.DEFAULT
                .withHeader("A", "B", "C")
                .withSkipHeaderRecord(true)
                .withAllowMissingColumnNames(true)
                .withIgnoreHeaderCase(true)
                .withHeaderComments("Comment1", "Comment2");

        assertArrayEquals(new String[]{"A", "B", "C"}, format.getHeader());
        assertTrue(format.getSkipHeaderRecord());
        assertTrue(format.getAllowMissingColumnNames());
        assertTrue(format.getIgnoreHeaderCase());
        assertArrayEquals(new String[]{"Comment1", "Comment2"}, format.getHeaderComments());
    }

    // Tests formatting values using format()
    @Test
    public void testFormat_simpleValues_returnsFormattedString() {
        final String result = CSVFormat.DEFAULT.format("a", "b", "c");
        assertEquals("a,b,c", result);
    }

    // Tests printing records with QuoteMode.ALL
    @Test
    public void testPrintRecord_quoteModeAll_quotesAllFields() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL).withRecordSeparator("\r\n");
        final StringWriter out = new StringWriter();
        format.printRecord(out, "hello", 123, "world");
        assertEquals("\"hello\",\"123\",\"world\"\r\n", out.toString());
    }

    // Tests printing records with QuoteMode.NON_NUMERIC
    @Test
    public void testPrintRecord_quoteModeNonNumeric_quotesStringsOnly() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.NON_NUMERIC).withRecordSeparator("\r\n");
        final StringWriter out = new StringWriter();
        format.printRecord(out, "text", Integer.valueOf(42), "more text");
        assertEquals("\"text\",42,\"more text\"\r\n", out.toString());
    }

    // Tests printing records with QuoteMode.NONE and escape character
    @Test
    public void testPrintRecord_quoteModeNoneWithEscape_escapesSpecialChars() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT
                .withQuoteMode(QuoteMode.NONE)
                .withEscape('\\')
                .withRecordSeparator("\n");
        final StringWriter out = new StringWriter();
        format.printRecord(out, "hello,world", "line1\nline2", "back\\slash");
        assertEquals("hello\\,world,line1\\nline2,back\\\\slash\n", out.toString());
    }

    // Tests printing records with null values and nullString
    @Test
    public void testPrintRecord_withNullString_outputsDefinedNullString() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL").withRecordSeparator("\r\n");
        final StringWriter out = new StringWriter();
        format.printRecord(out, "a", null, "b");
        assertEquals("a,NULL,b\r\n", out.toString());
    }

    // Tests printing records with trimming enabled
    @Test
    public void testPrintRecord_withTrim_trimsSurroundingSpaces() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT.withTrim().withRecordSeparator("\r\n");
        final StringWriter out = new StringWriter();
        format.printRecord(out, "  hello  ", "  world  ");
        assertEquals("hello,world\r\n", out.toString());
    }

    // Tests printing records with trailing delimiter enabled
    @Test
    public void testPrintln_withTrailingDelimiter_appendsDelimiterBeforeSeparator() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT.withTrailingDelimiter().withRecordSeparator("\r\n");
        final StringWriter out = new StringWriter();
        format.printRecord(out, "a", "b");
        assertEquals("a,b,\r\n", out.toString());
    }

    // Tests equals and hashCode consistency
    @Test
    public void testEqualsAndHashCode_sameAndDifferentObjects_behaveCorrectly() {
        final CSVFormat format1 = CSVFormat.DEFAULT.withIgnoreEmptyLines(true).withNullString("N/A");
        final CSVFormat format2 = CSVFormat.DEFAULT.withIgnoreEmptyLines(true).withNullString("N/A");
        final CSVFormat format3 = CSVFormat.DEFAULT.withIgnoreEmptyLines(false);

        assertEquals(format1, format1);
        assertEquals(format1, format2);
        assertEquals(format1.hashCode(), format2.hashCode());
        assertNotEquals(format1, format3);
        assertNotEquals(format1, null);
        assertNotEquals(format1, "someString");
    }

    // Tests toString method contains essential configuration attributes
    @Test
    public void testToString_configuredFormat_containsAttributes() {
        final CSVFormat format = CSVFormat.DEFAULT
                .withEscape('\\')
                .withCommentMarker('#')
                .withNullString("NULL")
                .withHeader("h1", "h2");

        final String str = format.toString();
        assertTrue(str.contains("Delimiter=<,>"));
        assertTrue(str.contains("Escape=<\\>"));
        assertTrue(str.contains("CommentStart=<#>"));
        assertTrue(str.contains("NullString=<NULL>"));
        assertTrue(str.contains("Header:[h1, h2]"));
    }

    // Tests parse method creates CSVParser correctly
    @Test
    public void testParse_stringReader_createsParser() throws IOException {
        final CSVParser parser = CSVFormat.DEFAULT.parse(new StringReader("a,b,c\n1,2,3"));
        assertNotNull(parser);
        parser.close();
    }
}