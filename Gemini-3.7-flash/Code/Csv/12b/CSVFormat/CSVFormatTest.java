package org.apache.commons.csv;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import org.junit.Test;

public class CSVFormatTest {

    // Tests predefined DEFAULT format properties
    @Test
    public void testDefaultFormat_predefinedConstants_haveCorrectValues() {
        assertEquals(',', CSVFormat.DEFAULT.getDelimiter());
        assertEquals(Character.valueOf('"'), CSVFormat.DEFAULT.getQuoteCharacter());
        assertNull(CSVFormat.DEFAULT.getCommentMarker());
        assertNull(CSVFormat.DEFAULT.getEscapeCharacter());
        assertFalse(CSVFormat.DEFAULT.getIgnoreSurroundingSpaces());
        assertTrue(CSVFormat.DEFAULT.getIgnoreEmptyLines());
        assertEquals("\r\n", CSVFormat.DEFAULT.getRecordSeparator());
        assertNull(CSVFormat.DEFAULT.getNullString());
        assertNull(CSVFormat.DEFAULT.getHeader());
        assertFalse(CSVFormat.DEFAULT.getSkipHeaderRecord());
        assertFalse(CSVFormat.DEFAULT.getAllowMissingColumnNames());
    }

    // Tests predefined RFC4180, EXCEL, TDF, and MYSQL formats
    @Test
    public void testPredefinedFormats_standardConfigurations_matchSpecs() {
        assertFalse(CSVFormat.RFC4180.getIgnoreEmptyLines());
        assertEquals(',', CSVFormat.RFC4180.getDelimiter());

        assertEquals('\t', CSVFormat.TDF.getDelimiter());
        assertTrue(CSVFormat.TDF.getIgnoreSurroundingSpaces());

        assertEquals('\t', CSVFormat.MYSQL.getDelimiter());
        assertEquals(Character.valueOf('\\'), CSVFormat.MYSQL.getEscapeCharacter());
        assertNull(CSVFormat.MYSQL.getQuoteCharacter());
        assertEquals("\n", CSVFormat.MYSQL.getRecordSeparator());
        assertFalse(CSVFormat.MYSQL.getIgnoreEmptyLines());
    }

    // Tests creating format with newFormat
    @Test
    public void testNewFormat_customDelimiter_createsFormat() {
        CSVFormat format = CSVFormat.newFormat(';');
        assertEquals(';', format.getDelimiter());
        assertNull(format.getQuoteCharacter());
        assertNull(format.getQuoteMode());
        assertNull(format.getCommentMarker());
        assertNull(format.getEscapeCharacter());
        assertFalse(format.getIgnoreSurroundingSpaces());
        assertFalse(format.getIgnoreEmptyLines());
        assertNull(format.getRecordSeparator());
        assertNull(format.getNullString());
        assertNull(format.getHeader());
        assertFalse(format.getSkipHeaderRecord());
        assertFalse(format.getAllowMissingColumnNames());
    }

    // Tests withDelimiter with valid char
    @Test
    public void testWithDelimiter_validChar_returnsUpdatedFormat() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter('|');
        assertEquals('|', format.getDelimiter());
    }

    // Tests withDelimiter with line break character throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testWithDelimiter_lineBreakLF_throwsException() {
        CSVFormat.DEFAULT.withDelimiter('\n');
    }

    // Tests withDelimiter with CR throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testWithDelimiter_lineBreakCR_throwsException() {
        CSVFormat.DEFAULT.withDelimiter('\r');
    }

    // Tests withQuote and isQuoteCharacterSet
    @Test
    public void testWithQuote_charAndNull_updatesQuoteChar() {
        CSVFormat format = CSVFormat.DEFAULT.withQuote('\'');
        assertEquals(Character.valueOf('\''), format.getQuoteCharacter());
        assertTrue(format.isQuoteCharacterSet());

        CSVFormat noQuote = format.withQuote((Character) null);
        assertNull(noQuote.getQuoteCharacter());
        assertFalse(noQuote.isQuoteCharacterSet());
    }

    // Tests withQuote with line break throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testWithQuote_lineBreakChar_throwsException() {
        CSVFormat.DEFAULT.withQuote('\n');
    }

    // Tests withQuoteMode
    @Test
    public void testWithQuoteMode_allModes_setsQuoteMode() {
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL);
        assertEquals(QuoteMode.ALL, format.getQuoteMode());
    }

    // Tests withCommentMarker and isCommentMarkerSet
    @Test
    public void testWithCommentMarker_validCharAndNull_updatesCommentMarker() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        assertEquals(Character.valueOf('#'), format.getCommentMarker());
        assertTrue(format.isCommentMarkerSet());

        CSVFormat noComment = format.withCommentMarker((Character) null);
        assertNull(noComment.getCommentMarker());
        assertFalse(noComment.isCommentMarkerSet());
    }

    // Tests withCommentMarker with line break throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testWithCommentMarker_lineBreakChar_throwsException() {
        CSVFormat.DEFAULT.withCommentMarker('\r');
    }

    // Tests withEscape and isEscapeCharacterSet
    @Test
    public void testWithEscape_charAndNull_updatesEscapeChar() {
        CSVFormat format = CSVFormat.DEFAULT.withEscape('?');
        assertEquals(Character.valueOf('?'), format.getEscapeCharacter());
        assertTrue(format.isEscapeCharacterSet());

        CSVFormat noEscape = format.withEscape((Character) null);
        assertNull(noEscape.getEscapeCharacter());
        assertFalse(noEscape.isEscapeCharacterSet());
    }

    // Tests withEscape with line break throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testWithEscape_lineBreakChar_throwsException() {
        CSVFormat.DEFAULT.withEscape('\n');
    }

    // Tests withNullString and isNullStringSet
    @Test
    public void testWithNullString_validStringAndNull_updatesNullString() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        assertEquals("NULL", format.getNullString());
        assertTrue(format.isNullStringSet());

        CSVFormat noNullStr = format.withNullString(null);
        assertNull(noNullStr.getNullString());
        assertFalse(noNullStr.isNullStringSet());
    }

    // Tests withHeader and getHeader
    @Test
    public void testWithHeader_validHeaders_returnsClonedArray() {
        String[] header = new String[]{"A", "B", "C"};
        CSVFormat format = CSVFormat.DEFAULT.withHeader(header);
        assertArrayEquals(header, format.getHeader());
        
        // Ensure immutability via clone
        header[0] = "Z";
        assertEquals("A", format.getHeader()[0]);
    }

    // Tests withHeader with null resets header
    @Test
    public void testWithHeader_null_resetsHeader() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("A", "B").withHeader((String[]) null);
        assertNull(format.getHeader());
    }

    // Tests withHeader duplicate names throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testWithHeader_duplicateHeaderEntries_throwsException() {
        CSVFormat.DEFAULT.withHeader("Col1", "Col2", "Col1");
    }

    // Tests boolean flag with-methods
    @Test
    public void testBooleanWithMethods_toggleFlags_updatesCorrectly() {
        CSVFormat format = CSVFormat.DEFAULT
                .withIgnoreSurroundingSpaces(true)
                .withIgnoreEmptyLines(false)
                .withSkipHeaderRecord(true)
                .withAllowMissingColumnNames(true);

        assertTrue(format.getIgnoreSurroundingSpaces());
        assertFalse(format.getIgnoreEmptyLines());
        assertTrue(format.getSkipHeaderRecord());
        assertTrue(format.getAllowMissingColumnNames());
    }

    // Tests withRecordSeparator char and String
    @Test
    public void testWithRecordSeparator_charAndString_updatesSeparator() {
        CSVFormat format1 = CSVFormat.DEFAULT.withRecordSeparator('\n');
        assertEquals("\n", format1.getRecordSeparator());

        CSVFormat format2 = CSVFormat.DEFAULT.withRecordSeparator("\r\n");
        assertEquals("\r\n", format2.getRecordSeparator());
    }

    // Tests validation when delimiter equals quoteCharacter
    @Test(expected = IllegalArgumentException.class)
    public void testValidate_delimiterEqualsQuote_throwsException() {
        CSVFormat.DEFAULT.withDelimiter('"');
    }

    // Tests validation when delimiter equals escapeCharacter
    @Test(expected = IllegalArgumentException.class)
    public void testValidate_delimiterEqualsEscape_throwsException() {
        CSVFormat.DEFAULT.withEscape('!').withDelimiter('!');
    }

    // Tests validation when delimiter equals commentMarker
    @Test(expected = IllegalArgumentException.class)
    public void testValidate_delimiterEqualsComment_throwsException() {
        CSVFormat.DEFAULT.withCommentMarker('#').withDelimiter('#');
    }

    // Tests validation when quoteCharacter equals commentMarker
    @Test(expected = IllegalArgumentException.class)
    public void testValidate_quoteEqualsComment_throwsException() {
        CSVFormat.DEFAULT.withCommentMarker('"');
    }

    // Tests validation when escapeCharacter equals commentMarker
    @Test(expected = IllegalArgumentException.class)
    public void testValidate_escapeEqualsComment_throwsException() {
        CSVFormat.DEFAULT.withEscape('#').withCommentMarker('#');
    }

    // Tests validation when QuoteMode.NONE is set without escape character
    @Test(expected = IllegalArgumentException.class)
    public void testValidate_quoteModeNoneWithoutEscape_throwsException() {
        CSVFormat.DEFAULT.withQuoteMode(QuoteMode.NONE);
    }

    // Tests equals and hashCode consistency
    @Test
    public void testEqualsAndHashCode_sameAndDifferentObjects_worksCorrectly() {
        CSVFormat format1 = CSVFormat.DEFAULT
                .withCommentMarker('#')
                .withEscape('\\')
                .withNullString("N/A")
                .withHeader("H1", "H2");

        CSVFormat format2 = CSVFormat.DEFAULT
                .withCommentMarker('#')
                .withEscape('\\')
                .withNullString("N/A")
                .withHeader("H1", "H2");

        assertEquals(format1, format1);
        assertEquals(format1, format2);
        assertEquals(format1.hashCode(), format2.hashCode());
        assertFalse(format1.equals(null));
        assertFalse(format1.equals("not a CSVFormat"));

        assertNotEquals(format1, format1.withDelimiter(';'));
        assertNotEquals(format1, format1.withQuote('\''));
        assertNotEquals(format1, format1.withCommentMarker('/'));
        assertNotEquals(format1, format1.withEscape('?'));
        assertNotEquals(format1, format1.withNullString("NULL"));
        assertNotEquals(format1, format1.withIgnoreSurroundingSpaces(true));
        assertNotEquals(format1, format1.withIgnoreEmptyLines(false));
        assertNotEquals(format1, format1.withSkipHeaderRecord(true));
        assertNotEquals(format1, format1.withRecordSeparator("\n"));
        assertNotEquals(format1, format1.withHeader("X", "Y"));
    }

    // Tests toString formatting
    @Test
    public void testToString_variousSettings_containsConfigDetails() {
        CSVFormat format = CSVFormat.DEFAULT
                .withEscape('\\')
                .withCommentMarker('#')
                .withNullString("N/A")
                .withHeader("Col1", "Col2");

        String str = format.toString();
        assertTrue(str.contains("Delimiter=<,>"));
        assertTrue(str.contains("Escape=<\\>"));
        assertTrue(str.contains("QuoteChar=<\">"));
        assertTrue(str.contains("CommentStart=<#>"));
        assertTrue(str.contains("NullString=<N/A>"));
        assertTrue(str.contains("RecordSeparator=<\r\n>"));
        assertTrue(str.contains("Header:[Col1, Col2]"));
    }

    // Tests format method with values
    @Test
    public void testFormat_objectArray_formatsCorrectly() {
        String formatted = CSVFormat.DEFAULT.format("a", "b", "c");
        assertEquals("a,b,c", formatted);
    }

    // Tests parse method returns parser
    @Test
    public void testParse_stringReader_returnsParser() throws Exception {
        CSVParser parser = CSVFormat.DEFAULT.parse(new StringReader("a,b,c"));
        assertNotNull(parser);
        parser.close();
    }

    // Tests print method returns printer
    @Test
    public void testPrint_appendable_returnsPrinter() throws Exception {
        StringBuilder sb = new StringBuilder();
        CSVPrinter printer = CSVFormat.DEFAULT.print(sb);
        assertNotNull(printer);
        printer.printRecord("1", "2");
        assertTrue(sb.length() > 0);
    }
}