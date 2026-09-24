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

    private enum TestHeaderEnum {
        HEADER_ONE,
        HEADER_TWO,
        HEADER_THREE
    }

    // Tests predefined formats configuration
    @Test
    public void testPredefinedFormats() {
        assertEquals(',', CSVFormat.DEFAULT.getDelimiter());
        assertEquals(Character.valueOf('"'), CSVFormat.DEFAULT.getQuoteCharacter());
        assertTrue(CSVFormat.DEFAULT.getIgnoreEmptyLines());

        assertEquals(',', CSVFormat.RFC4180.getDelimiter());
        assertFalse(CSVFormat.RFC4180.getIgnoreEmptyLines());

        assertTrue(CSVFormat.EXCEL.getAllowMissingColumnNames());
        assertEquals(',', CSVFormat.EXCEL.getDelimiter());

        assertEquals('\t', CSVFormat.TDF.getDelimiter());
        assertTrue(CSVFormat.TDF.getIgnoreSurroundingSpaces());

        assertEquals('\t', CSVFormat.MYSQL.getDelimiter());
        assertNull(CSVFormat.MYSQL.getQuoteCharacter());
        assertEquals(Character.valueOf('\\'), CSVFormat.MYSQL.getEscapeCharacter());
        assertEquals("\\N", CSVFormat.MYSQL.getNullString());
    }

    // Tests valueOf with predefined format names
    @Test
    public void testValueOf() {
        assertEquals(CSVFormat.DEFAULT, CSVFormat.valueOf("Default"));
        assertEquals(CSVFormat.EXCEL, CSVFormat.valueOf("Excel"));
        assertEquals(CSVFormat.MYSQL, CSVFormat.valueOf("MySQL"));
        assertEquals(CSVFormat.RFC4180, CSVFormat.valueOf("RFC4180"));
        assertEquals(CSVFormat.TDF, CSVFormat.valueOf("TDF"));
    }

    // Tests newFormat factory method
    @Test
    public void testNewFormat() {
        final CSVFormat format = CSVFormat.newFormat('|');
        assertEquals('|', format.getDelimiter());
        assertNull(format.getQuoteCharacter());
        assertNull(format.getEscapeCharacter());
        assertNull(format.getCommentMarker());
        assertNull(format.getRecordSeparator());
        assertNull(format.getNullString());
        assertFalse(format.getIgnoreSurroundingSpaces());
        assertFalse(format.getIgnoreEmptyLines());
    }

    // Tests invalid delimiter (CR line break)
    @Test(expected = IllegalArgumentException.class)
    public void testWithDelimiter_lineBreakCR_throwsException() {
        CSVFormat.DEFAULT.withDelimiter('\r');
    }

    // Tests invalid delimiter (LF line break)
    @Test(expected = IllegalArgumentException.class)
    public void testWithDelimiter_lineBreakLF_throwsException() {
        CSVFormat.DEFAULT.withDelimiter('\n');
    }

    // Tests valid delimiter change
    @Test
    public void testWithDelimiter_validDelimiter_returnsUpdatedFormat() {
        final CSVFormat format = CSVFormat.DEFAULT.withDelimiter(';');
        assertEquals(';', format.getDelimiter());
    }

    // Tests setting quote character
    @Test
    public void testWithQuote_validAndLineBreak() {
        final CSVFormat format = CSVFormat.DEFAULT.withQuote('\'');
        assertEquals(Character.valueOf('\''), format.getQuoteCharacter());
        assertTrue(format.isQuoteCharacterSet());

        final CSVFormat noQuote = format.withQuote((Character) null);
        assertNull(noQuote.getQuoteCharacter());
        assertFalse(noQuote.isQuoteCharacterSet());
    }

    // Tests invalid quote character with line break
    @Test(expected = IllegalArgumentException.class)
    public void testWithQuote_lineBreak_throwsException() {
        CSVFormat.DEFAULT.withQuote('\n');
    }

    // Tests setting escape character
    @Test
    public void testWithEscape_validAndLineBreak() {
        final CSVFormat format = CSVFormat.DEFAULT.withEscape('\\');
        assertEquals(Character.valueOf('\\'), format.getEscapeCharacter());
        assertTrue(format.isEscapeCharacterSet());

        final CSVFormat noEscape = format.withEscape((Character) null);
        assertNull(noEscape.getEscapeCharacter());
        assertFalse(noEscape.isEscapeCharacterSet());
    }

    // Tests invalid escape character with line break
    @Test(expected = IllegalArgumentException.class)
    public void testWithEscape_lineBreak_throwsException() {
        CSVFormat.DEFAULT.withEscape('\r');
    }

    // Tests setting comment marker
    @Test
    public void testWithCommentMarker_validAndLineBreak() {
        final CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        assertEquals(Character.valueOf('#'), format.getCommentMarker());
        assertTrue(format.isCommentMarkerSet());

        final CSVFormat noComment = format.withCommentMarker((Character) null);
        assertNull(noComment.getCommentMarker());
        assertFalse(noComment.isCommentMarkerSet());
    }

    // Tests invalid comment marker with line break
    @Test(expected = IllegalArgumentException.class)
    public void testWithCommentMarker_lineBreak_throwsException() {
        CSVFormat.DEFAULT.withCommentMarker('\n');
    }

    // Tests validation when delimiter and quote character are the same
    @Test(expected = IllegalArgumentException.class)
    public void testValidate_delimiterSameAsQuote_throwsException() {
        CSVFormat.DEFAULT.withDelimiter('"');
    }

    // Tests validation when delimiter and escape character are the same
    @Test(expected = IllegalArgumentException.class)
    public void testValidate_delimiterSameAsEscape_throwsException() {
        CSVFormat.DEFAULT.withEscape(';').withDelimiter(';');
    }

    // Tests validation when delimiter and comment marker are the same
    @Test(expected = IllegalArgumentException.class)
    public void testValidate_delimiterSameAsCommentMarker_throwsException() {
        CSVFormat.DEFAULT.withCommentMarker(';').withDelimiter(';');
    }

    // Tests validation when quote character and comment marker are the same
    @Test(expected = IllegalArgumentException.class)
    public void testValidate_quoteSameAsCommentMarker_throwsException() {
        CSVFormat.DEFAULT.withCommentMarker('"');
    }

    // Tests validation when escape character and comment marker are the same
    @Test(expected = IllegalArgumentException.class)
    public void testValidate_escapeSameAsCommentMarker_throwsException() {
        CSVFormat.DEFAULT.withEscape('#').withCommentMarker('#');
    }

    // Tests validation when QuoteMode is NONE and escape character is not set
    @Test(expected = IllegalArgumentException.class)
    public void testValidate_quoteModeNoneWithoutEscape_throwsException() {
        CSVFormat.DEFAULT.withQuoteMode(QuoteMode.NONE);
    }

    // Tests setting headers and duplicate header validation
    @Test
    public void testWithHeader_validHeaders() {
        final CSVFormat format = CSVFormat.DEFAULT.withHeader("A", "B", "C");
        assertArrayEquals(new String[]{"A", "B", "C"}, format.getHeader());
    }

    // Tests duplicate header detection
    @Test(expected = IllegalArgumentException.class)
    public void testWithHeader_duplicateHeader_throwsException() {
        CSVFormat.DEFAULT.withHeader("Col1", "Col2", "Col1");
    }

    // Tests boolean flags and properties setters
    @Test
    public void testWithBooleanAndPropertyModifiers() {
        final CSVFormat format = CSVFormat.DEFAULT
                .withAllowMissingColumnNames(true)
                .withIgnoreEmptyLines(false)
                .withIgnoreSurroundingSpaces(true)
                .withIgnoreHeaderCase(true)
                .withSkipHeaderRecord(true)
                .withNullString("NULL")
                .withRecordSeparator("\n")
                .withHeaderComments("Header comment 1", "Header comment 2")
                .withQuoteMode(QuoteMode.ALL);

        assertTrue(format.getAllowMissingColumnNames());
        assertFalse(format.getIgnoreEmptyLines());
        assertTrue(format.getIgnoreSurroundingSpaces());
        assertTrue(format.getIgnoreHeaderCase());
        assertTrue(format.getSkipHeaderRecord());
        assertEquals("NULL", format.getNullString());
        assertTrue(format.isNullStringSet());
        assertEquals("\n", format.getRecordSeparator());
        assertArrayEquals(new String[]{"Header comment 1", "Header comment 2"}, format.getHeaderComments());
        assertEquals(QuoteMode.ALL, format.getQuoteMode());
    }

    // Tests format method and string output
    @Test
    public void testFormat_simpleValues_returnsFormattedString() {
        final String result = CSVFormat.DEFAULT.format("a", "b", "c");
        assertEquals("a,b,c", result);
    }

    // Tests parse and print creation
    @Test
    public void testParseAndPrint() throws IOException {
        final CSVParser parser = CSVFormat.DEFAULT.parse(new StringReader("a,b,c\n1,2,3"));
        assertNotNull(parser);
        final StringBuilder out = new StringBuilder();
        final CSVPrinter printer = CSVFormat.DEFAULT.print(out);
        assertNotNull(printer);
    }

    // Tests equals, hashCode, and toString consistency
    @Test
    public void testEqualsHashCodeAndToString() {
        final CSVFormat format1 = CSVFormat.DEFAULT.withNullString("N/A");
        final CSVFormat format2 = CSVFormat.DEFAULT.withNullString("N/A");
        final CSVFormat format3 = CSVFormat.DEFAULT.withNullString("NULL");

        assertEquals(format1, format1);
        assertEquals(format1, format2);
        assertEquals(format1.hashCode(), format2.hashCode());
        assertNotEquals(format1, format3);
        assertNotEquals(format1, null);
        assertNotEquals(format1, "someString");

        final String stringRepr = format1.toString();
        assertTrue(stringRepr.contains("Delimiter=<,>"));
        assertTrue(stringRepr.contains("NullString=<N/A>"));
    }

    // Additional tests for uncovered methods and branches

    @Test
    public void testWithHeader_fromEnum() {
        final CSVFormat format = CSVFormat.DEFAULT.withHeader(TestHeaderEnum.class);
        assertArrayEquals(new String[]{"HEADER_ONE", "HEADER_TWO", "HEADER_THREE"}, format.getHeader());

        final CSVFormat nullEnumFormat = CSVFormat.DEFAULT.withHeader((Class<? extends Enum<?>>) null);
        assertNull(nullEnumFormat.getHeader());
    }

    @Test
    public void testWithHeader_emptyAndNullArray() {
        final CSVFormat formatNull = CSVFormat.DEFAULT.withHeader((String[]) null);
        assertNull(formatNull.getHeader());

        final CSVFormat formatEmpty = CSVFormat.DEFAULT.withHeader();
        assertArrayEquals(new String[0], formatEmpty.getHeader());
    }

    @Test
    public void testWithFirstRecordAsHeader() {
        final CSVFormat format = CSVFormat.DEFAULT.withFirstRecordAsHeader();
        assertArrayEquals(new String[0], format.getHeader());
        assertTrue(format.getSkipHeaderRecord());
    }

    @Test
    public void testWithRecordSeparator_charAndSystem() {
        final CSVFormat formatChar = CSVFormat.DEFAULT.withRecordSeparator('\n');
        assertEquals("\n", formatChar.getRecordSeparator());

        final CSVFormat formatSystem = CSVFormat.DEFAULT.withSystemRecordSeparator();
        assertEquals(System.lineSeparator(), formatSystem.getRecordSeparator());
    }

    @Test
    public void testWithTrimAndTrailingDelimiter() {
        final CSVFormat format = CSVFormat.DEFAULT.withTrim(true).withTrailingDelimiter(true);
        assertTrue(format.getTrim());
        assertTrue(format.getTrailingDelimiter());

        final CSVFormat formatNoArgs = CSVFormat.DEFAULT.withTrim().withTrailingDelimiter();
        assertTrue(formatNoArgs.getTrim());
        assertTrue(formatNoArgs.getTrailingDelimiter());

        final CSVFormat formatDisabled = format.withTrim(false).withTrailingDelimiter(false);
        assertFalse(formatDisabled.getTrim());
        assertFalse(formatDisabled.getTrailingDelimiter());
    }

    @Test
    public void testWithAutoFlush() {
        final CSVFormat format = CSVFormat.DEFAULT.withAutoFlush(true);
        assertTrue(format.getAutoFlush());

        final CSVFormat formatDisabled = format.withAutoFlush(false);
        assertFalse(formatDisabled.getAutoFlush());
    }

    @Test
    public void testWithQuote_primitiveChar() {
        final CSVFormat format = CSVFormat.DEFAULT.withQuote('\'');
        assertEquals(Character.valueOf('\''), format.getQuoteCharacter());
    }

    @Test
    public void testWithEscape_primitiveChar() {
        final CSVFormat format = CSVFormat.DEFAULT.withEscape('!');
        assertEquals(Character.valueOf('!'), format.getEscapeCharacter());
    }

    @Test
    public void testWithCommentMarker_primitiveChar() {
        final CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('?');
        assertEquals(Character.valueOf('?'), format.getCommentMarker());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidate_quoteSameAsEscape_throwsException() {
        CSVFormat.DEFAULT.withEscape('"');
    }

    @Test
    public void testPrintlnAndPrintRecord() throws IOException {
        final StringWriter sw = new StringWriter();
        CSVFormat.DEFAULT.println(sw);
        assertEquals("\r\n", sw.toString());

        final StringWriter sw2 = new StringWriter();
        CSVFormat.DEFAULT.printRecord(sw2, "val1", "val2");
        assertEquals("val1,val2\r\n", sw2.toString());
    }

    @Test
    public void testPrinter() throws IOException {
        final CSVPrinter printer = CSVFormat.DEFAULT.printer();
        assertNotNull(printer);
    }
}