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
import org.junit.Test;

public class CSVFormatTest {

    private enum HeaderEnum {
        NAME, EMAIL, PHONE
    }

    // Tests default constants and predefined instances
    @Test
    public void testPredefinedFormats_standardConfigurations_matchExpectedValues() {
        assertEquals(',', CSVFormat.DEFAULT.getDelimiter());
        assertEquals(Character.valueOf('"'), CSVFormat.DEFAULT.getQuoteCharacter());
        assertEquals("\r\n", CSVFormat.DEFAULT.getRecordSeparator());
        assertTrue(CSVFormat.DEFAULT.getIgnoreEmptyLines());

        assertEquals(',', CSVFormat.RFC4180.getDelimiter());
        assertFalse(CSVFormat.RFC4180.getIgnoreEmptyLines());

        assertEquals('\t', CSVFormat.TDF.getDelimiter());
        assertTrue(CSVFormat.TDF.getIgnoreSurroundingSpaces());

        assertEquals('\t', CSVFormat.MYSQL.getDelimiter());
        assertEquals(Character.valueOf('\\'), CSVFormat.MYSQL.getEscapeCharacter());
        assertNull(CSVFormat.MYSQL.getQuoteCharacter());

        assertEquals(CSVFormat.DEFAULT, CSVFormat.valueOf("Default"));
        assertEquals(CSVFormat.RFC4180, CSVFormat.valueOf("RFC4180"));
    }

    // Tests newFormat factory method
    @Test
    public void testNewFormat_customDelimiter_createsMinimalFormat() {
        final CSVFormat format = CSVFormat.newFormat('|');
        assertEquals('|', format.getDelimiter());
        assertNull(format.getQuoteCharacter());
        assertNull(format.getEscapeCharacter());
        assertNull(format.getCommentMarker());
        assertNull(format.getRecordSeparator());
        assertFalse(format.getIgnoreEmptyLines());
    }

    // Tests delimiter validation when set to line break
    @Test(expected = IllegalArgumentException.class)
    public void testValidate_delimiterIsLineBreakLF_throwsException() {
        CSVFormat.DEFAULT.withDelimiter('\n');
    }

    // Tests delimiter validation when set to CR
    @Test(expected = IllegalArgumentException.class)
    public void testValidate_delimiterIsLineBreakCR_throwsException() {
        CSVFormat.DEFAULT.withDelimiter('\r');
    }

    // Tests quote char equal to delimiter validation
    @Test(expected = IllegalArgumentException.class)
    public void testValidate_quoteCharEqualsDelimiter_throwsException() {
        CSVFormat.DEFAULT.withQuote(',').withDelimiter(',');
    }

    // Tests escape char equal to delimiter validation
    @Test(expected = IllegalArgumentException.class)
    public void testValidate_escapeCharEqualsDelimiter_throwsException() {
        CSVFormat.DEFAULT.withEscape(',').withDelimiter(',');
    }

    // Tests comment marker equal to delimiter validation
    @Test(expected = IllegalArgumentException.class)
    public void testValidate_commentMarkerEqualsDelimiter_throwsException() {
        CSVFormat.DEFAULT.withCommentMarker(',').withDelimiter(',');
    }

    // Tests quote char equal to comment marker validation
    @Test(expected = IllegalArgumentException.class)
    public void testValidate_quoteCharEqualsCommentMarker_throwsException() {
        CSVFormat.DEFAULT.withQuote('!').withCommentMarker('!');
    }

    // Tests quoteMode NONE without escape character validation
    @Test(expected = IllegalArgumentException.class)
    public void testValidate_quoteModeNoneWithoutEscape_throwsException() {
        CSVFormat.DEFAULT.withEscape(null).withQuoteMode(QuoteMode.NONE);
    }

    // Tests header duplicate validation
    @Test(expected = IllegalArgumentException.class)
    public void testValidate_duplicateHeaderNames_throwsException() {
        CSVFormat.DEFAULT.withHeader("col1", "col2", "col1");
    }

    // Tests comment marker validation when set to line break
    @Test(expected = IllegalArgumentException.class)
    public void testWithCommentMarker_lineBreak_throwsException() {
        CSVFormat.DEFAULT.withCommentMarker('\n');
    }

    // Tests escape character validation when set to line break
    @Test(expected = IllegalArgumentException.class)
    public void testWithEscape_lineBreak_throwsException() {
        CSVFormat.DEFAULT.withEscape('\r');
    }

    // Tests quote character validation when set to line break
    @Test(expected = IllegalArgumentException.class)
    public void testWithQuote_lineBreak_throwsException() {
        CSVFormat.DEFAULT.withQuote('\n');
    }

    // Tests withHeader from Enum class
    @Test
    public void testWithHeader_enumClass_setsHeaderNames() {
        final CSVFormat format = CSVFormat.DEFAULT.withHeader(HeaderEnum.class);
        assertArrayEquals(new String[]{"NAME", "EMAIL", "PHONE"}, format.getHeader());
    }

    // Tests withFirstRecordAsHeader configuration
    @Test
    public void testWithFirstRecordAsHeader_setsEmptyHeaderAndSkipFlag() {
        final CSVFormat format = CSVFormat.DEFAULT.withFirstRecordAsHeader();
        assertArrayEquals(new String[0], format.getHeader());
        assertTrue(format.getSkipHeaderRecord());
    }

    // Tests with... builder methods for various properties
    @Test
    public void testWithMethods_modifyPropertiesCorrectly() {
        final CSVFormat format = CSVFormat.DEFAULT
                .withAllowMissingColumnNames(true)
                .withAutoFlush(true)
                .withIgnoreHeaderCase(true)
                .withIgnoreSurroundingSpaces(true)
                .withNullString("NULL")
                .withTrailingDelimiter(true)
                .withTrim(true)
                .withHeaderComments("Header comment 1", "Header comment 2");

        assertTrue(format.getAllowMissingColumnNames());
        assertTrue(format.getAutoFlush());
        assertTrue(format.getIgnoreHeaderCase());
        assertTrue(format.getIgnoreSurroundingSpaces());
        assertEquals("NULL", format.getNullString());
        assertTrue(format.getTrailingDelimiter());
        assertTrue(format.getTrim());
        assertArrayEquals(new String[]{"Header comment 1", "Header comment 2"}, format.getHeaderComments());
        assertTrue(format.isNullStringSet());
    }

    // Tests equals and hashCode contracts
    @Test
    public void testEqualsAndHashCode_sameAndDifferentConfigurations() {
        final CSVFormat format1 = CSVFormat.DEFAULT.withDelimiter(';').withQuote('\'');
        final CSVFormat format2 = CSVFormat.DEFAULT.withDelimiter(';').withQuote('\'');
        final CSVFormat format3 = CSVFormat.DEFAULT.withDelimiter(',').withQuote('\'');

        assertEquals(format1, format1);
        assertEquals(format1, format2);
        assertEquals(format1.hashCode(), format2.hashCode());

        assertNotEquals(format1, format3);
        assertNotEquals(format1, null);
        assertNotEquals(format1, "otherType");
    }

    // Tests toString content
    @Test
    public void testToString_containsConfiguredProperties() {
        final CSVFormat format = CSVFormat.DEFAULT
                .withCommentMarker('#')
                .withEscape('\\')
                .withNullString("N/A");

        final String string = format.toString();
        assertTrue(string.contains("Delimiter=<,>"));
        assertTrue(string.contains("Escape=<\\>"));
        assertTrue(string.contains("CommentStart=<#>"));
        assertTrue(string.contains("NullString=<N/A>"));
    }

    // Tests format method with QuoteMode MINIMAL on basic strings
    @Test
    public void testFormat_quoteModeMinimal_quotesWhenNecessary() {
        assertEquals("a,b,c", CSVFormat.DEFAULT.format("a", "b", "c"));
        assertEquals("\"a,1\",b,c", CSVFormat.DEFAULT.format("a,1", "b", "c"));
        assertEquals("\"a\"\"1\",b,c", CSVFormat.DEFAULT.format("a\"1", "b", "c"));
        assertEquals("\"a\r\n1\",b,c", CSVFormat.DEFAULT.format("a\r\n1", "b", "c"));
        assertEquals("\"\",b", CSVFormat.DEFAULT.format("", "b"));
        assertEquals(",\"\"", CSVFormat.DEFAULT.format("a", "").substring(1));
    }

    // Tests format method with comment-like and special characters (Defects4J Csv-15 related)
    @Test
    public void testFormat_valuesWithCommentAndSpecialChars_formatsCorrectly() {
        final CSVFormat formatWithComment = CSVFormat.DEFAULT.withCommentMarker('#');
        assertEquals("\"#start\",b", formatWithComment.format("#start", "b"));
        assertEquals("a,#notComment", formatWithComment.format("a", "#notComment"));
        assertEquals("a,\\b", CSVFormat.DEFAULT.format("a", "\\b"));
    }

    // Tests format method with QuoteMode ALL and ALL_NON_NULL
    @Test
    public void testFormat_quoteModeAll_quotesAllFields() {
        final CSVFormat formatAll = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL);
        assertEquals("\"a\",\"b\",\"c\"", formatAll.format("a", "b", "c"));

        final CSVFormat formatAllNonNull = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL_NON_NULL).withNullString("NULL");
        assertEquals("\"a\",NULL,\"c\"", formatAllNonNull.format("a", null, "c"));
    }

    // Tests format method with QuoteMode NON_NUMERIC
    @Test
    public void testFormat_quoteModeNonNumeric_quotesStringsNotNumbers() {
        final CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.NON_NUMERIC);
        assertEquals("\"text\",123,\"456\"", format.format("text", Integer.valueOf(123), "456"));
    }

    // Tests print and escape with QuoteMode NONE
    @Test
    public void testPrint_quoteModeNone_escapesSpecialChars() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.NONE).withEscape('\\');
        final StringWriter out = new StringWriter();
        format.print("a,b", out, true);
        format.print("c\nd", out, false);
        assertEquals("a\\,b,c\\nd", out.toString());
    }

    // Tests printRecord with trailing delimiter
    @Test
    public void testPrintRecord_withTrailingDelimiter_appendsDelimiterBeforeNewline() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT.withTrailingDelimiter(true).withRecordSeparator("\n");
        final StringWriter out = new StringWriter();
        format.printRecord(out, "x", "y");
        assertEquals("x,y,\n", out.toString());
    }

    // Tests parse and print with Reader and Appendable
    @Test
    public void testParseAndPrint_validInputs_returnsParserAndPrinter() throws IOException {
        final StringReader reader = new StringReader("a,b\n1,2");
        final CSVParser parser = CSVFormat.DEFAULT.parse(reader);
        assertNotNull(parser);

        final StringWriter writer = new StringWriter();
        final CSVPrinter printer = CSVFormat.DEFAULT.print(writer);
        assertNotNull(printer);
        printer.printRecord("v1", "v2");
        assertEquals("v1,v2\r\n", writer.toString());
    }
}