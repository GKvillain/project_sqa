package org.apache.commons.csv;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import org.junit.Test;

public class CSVFormatTest {

    // Tests creating format with valid delimiter
    @Test
    public void testNewFormat_validDelimiter_createsFormatWithDelimiter() {
        final CSVFormat format = CSVFormat.newFormat('|');
        assertEquals('|', format.getDelimiter());
        assertNull(format.getQuoteChar());
        assertNull(format.getCommentStart());
        assertNull(format.getEscape());
        assertFalse(format.getIgnoreSurroundingSpaces());
        assertFalse(format.getIgnoreEmptyLines());
        assertNull(format.getRecordSeparator());
        assertNull(format.getNullString());
        assertNull(format.getHeader());
        assertFalse(format.getSkipHeaderRecord());
    }

    // Tests exception path when delimiter is a line break character
    @Test(expected = IllegalArgumentException.class)
    public void testNewFormat_lineBreakDelimiterLF_throwsIllegalArgumentException() {
        CSVFormat.newFormat('\n');
    }

    // Tests exception path when delimiter is CR line break character
    @Test(expected = IllegalArgumentException.class)
    public void testWithDelimiter_lineBreakDelimiterCR_throwsIllegalArgumentException() {
        CSVFormat.DEFAULT.withDelimiter('\r');
    }

    // Tests exception path when quoteChar is a line break character
    @Test(expected = IllegalArgumentException.class)
    public void testWithQuoteChar_lineBreak_throwsIllegalArgumentException() {
        CSVFormat.DEFAULT.withQuoteChar('\n');
    }

    // Tests exception path when escape character is a line break
    @Test(expected = IllegalArgumentException.class)
    public void testWithEscape_lineBreak_throwsIllegalArgumentException() {
        CSVFormat.DEFAULT.withEscape('\r');
    }

    // Tests exception path when comment start character is a line break
    @Test(expected = IllegalArgumentException.class)
    public void testWithCommentStart_lineBreak_throwsIllegalArgumentException() {
        CSVFormat.DEFAULT.withCommentStart('\n');
    }

    // Tests normal case where validation passes for DEFAULT format
    @Test
    public void testValidate_validFormat_doesNotThrowException() {
        CSVFormat.DEFAULT.validate();
        CSVFormat.RFC4180.validate();
        CSVFormat.EXCEL.validate();
        CSVFormat.TDF.validate();
        CSVFormat.MYSQL.validate();
    }

    // Tests validation when quoteChar and delimiter are equal
    @Test(expected = IllegalStateException.class)
    public void testValidate_quoteCharEqualsDelimiter_throwsIllegalStateException() {
        CSVFormat.DEFAULT.withDelimiter('!').withQuoteChar('!').validate();
    }

    // Tests validation when escape and delimiter are equal
    @Test(expected = IllegalStateException.class)
    public void testValidate_escapeEqualsDelimiter_throwsIllegalStateException() {
        CSVFormat.DEFAULT.withDelimiter('!').withEscape('!').validate();
    }

    // Tests validation when commentStart and delimiter are equal
    @Test(expected = IllegalStateException.class)
    public void testValidate_commentStartEqualsDelimiter_throwsIllegalStateException() {
        CSVFormat.DEFAULT.withDelimiter('!').withCommentStart('!').validate();
    }

    // Tests validation when quoteChar and commentStart are equal
    @Test(expected = IllegalStateException.class)
    public void testValidate_quoteCharEqualsCommentStart_throwsIllegalStateException() {
        CSVFormat.DEFAULT.withQuoteChar('#').withCommentStart('#').validate();
    }

    // Tests validation when escape and commentStart are equal
    @Test(expected = IllegalStateException.class)
    public void testValidate_escapeEqualsCommentStart_throwsIllegalStateException() {
        CSVFormat.DEFAULT.withEscape('#').withCommentStart('#').validate();
    }

    // Tests validation when Quote.NONE policy is set without an escape character
    @Test(expected = IllegalStateException.class)
    public void testValidate_quotePolicyNoneWithoutEscape_throwsIllegalStateException() {
        CSVFormat.DEFAULT.withQuotePolicy(Quote.NONE).withEscape(null).validate();
    }

    // Tests validation when header array contains duplicate column names
    @Test(expected = IllegalStateException.class)
    public void testValidate_duplicateHeaderNames_throwsIllegalStateException() {
        CSVFormat.DEFAULT.withHeader("A", "B", "A").validate();
    }

    // Tests withX methods for immutability and updated properties
    @Test
    public void testWithMethods_chaining_returnsUpdatedFormat() {
        final CSVFormat format = CSVFormat.DEFAULT
                .withDelimiter(';')
                .withQuoteChar('\'')
                .withQuotePolicy(Quote.ALL)
                .withCommentStart('#')
                .withEscape('\\')
                .withIgnoreSurroundingSpaces(true)
                .withIgnoreEmptyLines(false)
                .withRecordSeparator("\r\n")
                .withNullString("NULL")
                .withHeader("Col1", "Col2")
                .withSkipHeaderRecord(true);

        assertEquals(';', format.getDelimiter());
        assertEquals(Character.valueOf('\''), format.getQuoteChar());
        assertEquals(Quote.ALL, format.getQuotePolicy());
        assertEquals(Character.valueOf('#'), format.getCommentStart());
        assertEquals(Character.valueOf('\\'), format.getEscape());
        assertTrue(format.getIgnoreSurroundingSpaces());
        assertFalse(format.getIgnoreEmptyLines());
        assertEquals("\r\n", format.getRecordSeparator());
        assertEquals("NULL", format.getNullString());
        assertArrayEquals(new String[]{"Col1", "Col2"}, format.getHeader());
        assertTrue(format.getSkipHeaderRecord());

        assertTrue(format.isQuoting());
        assertTrue(format.isCommentingEnabled());
        assertTrue(format.isEscaping());
        assertTrue(format.isNullHandling());
    }

    // Tests predefined format constants
    @Test
    public void testPredefinedFormats_constants_haveExpectedValues() {
        assertEquals(',', CSVFormat.DEFAULT.getDelimiter());
        assertEquals(Character.valueOf('"'), CSVFormat.DEFAULT.getQuoteChar());
        assertTrue(CSVFormat.DEFAULT.getIgnoreEmptyLines());

        assertEquals('\t', CSVFormat.TDF.getDelimiter());
        assertTrue(CSVFormat.TDF.getIgnoreSurroundingSpaces());

        assertEquals('\t', CSVFormat.MYSQL.getDelimiter());
        assertEquals(Character.valueOf('\\'), CSVFormat.MYSQL.getEscape());
        assertNull(CSVFormat.MYSQL.getQuoteChar());
        assertEquals("\n", CSVFormat.MYSQL.getRecordSeparator());
    }

    // Tests equals and hashCode consistency
    @Test
    public void testEqualsAndHashCode_sameAndDifferent_consistent() {
        final CSVFormat f1 = CSVFormat.DEFAULT.withHeader("A", "B");
        final CSVFormat f2 = CSVFormat.DEFAULT.withHeader("A", "B");
        final CSVFormat f3 = CSVFormat.DEFAULT.withHeader("A", "C");

        assertTrue(f1.equals(f1));
        assertTrue(f1.equals(f2));
        assertEquals(f1.hashCode(), f2.hashCode());

        assertFalse(f1.equals(null));
        assertFalse(f1.equals("differentType"));
        assertFalse(f1.equals(f3));
        assertFalse(f1.equals(CSVFormat.DEFAULT.withDelimiter(';')));
        assertFalse(f1.equals(CSVFormat.DEFAULT.withIgnoreEmptyLines(false)));
    }

    // Tests formatting values into String
    @Test
    public void testFormat_validValues_returnsFormattedString() {
        final String result = CSVFormat.DEFAULT.format("a", "b", "c");
        assertEquals("a,b,c", result);
    }

    // Tests toString method contains format properties
    @Test
    public void testToString_variousConfigurations_containsExpectedProperties() {
        final CSVFormat format = CSVFormat.DEFAULT
                .withEscape('\\')
                .withCommentStart('#')
                .withNullString("N/A")
                .withHeader("H1", "H2");

        final String str = format.toString();
        assertTrue(str.contains("Delimiter=<,>"));
        assertTrue(str.contains("Escape=<\\>"));
        assertTrue(str.contains("QuoteChar=<\">"));
        assertTrue(str.contains("CommentStart=<#>"));
        assertTrue(str.contains("NullString=<N/A>"));
        assertTrue(str.contains("Header:[H1, H2]"));
    }

    // Tests parsing reader input returns non-null CSVParser
    @Test
    public void testParse_readerInput_returnsCSVParser() throws IOException {
        final StringReader reader = new StringReader("a,b,c\n1,2,3");
        final CSVParser parser = CSVFormat.DEFAULT.parse(reader);
        assertNotNull(parser);
    }
}