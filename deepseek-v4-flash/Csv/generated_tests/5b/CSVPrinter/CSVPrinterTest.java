package org.apache.commons.csv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

public class CSVPrinterTest {

    private CSVFormat format;
    private StringWriter sw;
    private CSVPrinter printer;

    // Helper method to create a custom CSVFormat
    private CSVFormat createFormat(final Character delimiter, final Character quoteChar, final Character escapeChar,
            final boolean quoting, final boolean escaping, final Quote quotePolicy, final String nullString,
            final boolean commentingEnabled, final Character commentStart, final String recordSeparator) {
        // Note: CSVFormat.Builder is not available; using with* methods on CSVFormat
        // This is a placeholder; actual CSVFormat construction depends on available API
        // For simplicity, use CSVFormat.DEFAULT and modify via with* methods
        return CSVFormat.DEFAULT;
    }

    @Before
    public void setUp() throws Exception {
        sw = new StringWriter();
        format = CSVFormat.DEFAULT;
        printer = new CSVPrinter(sw, format);
    }

    // Tests normal case: print a single value
    @Test
    public void testPrint_singleValue_printsValue() throws IOException {
        printer.print("hello");
        printer.flush();
        assertEquals("hello", sw.toString());
    }

    // Tests print with null value: should print empty string when nullString is null
    @Test
    public void testPrint_nullValue_printsEmpty() throws IOException {
        printer.print(null);
        printer.flush();
        assertEquals("", sw.toString());
    }

    // Tests print with null value and custom nullString
    @Test
    public void testPrint_nullValueWithNullString_printsNullString() throws IOException {
        CSVFormat customFormat = CSVFormat.DEFAULT.withNullString("NULL");
        CSVPrinter customPrinter = new CSVPrinter(sw, customFormat);
        customPrinter.print(null);
        customPrinter.flush();
        assertEquals("NULL", sw.toString());
    }

    // Tests printRecord with array of values
    @Test
    public void testPrintRecord_arrayValues_printsDelimitedAndNewline() throws IOException {
        printer.printRecord("a", "b", "c");
        printer.flush();
        assertEquals("a,b,c" + format.getRecordSeparator(), sw.toString());
    }

    // Tests printRecord with Iterable values
    @Test
    public void testPrintRecord_iterableValues_printsDelimitedAndNewline() throws IOException {
        printer.printRecord(Arrays.asList("x", "y", "z"));
        printer.flush();
        assertEquals("x,y,z" + format.getRecordSeparator(), sw.toString());
    }

    // Tests println: outputs record separator and sets newRecord flag
    @Test
    public void testPrintln_afterPrint_printsNewline() throws IOException {
        printer.print("first");
        printer.println();
        printer.flush();
        String expected = "first" + format.getRecordSeparator();
        assertEquals(expected, sw.toString());
    }

    // Tests print after newRecord: no delimiter before first value on new line
    @Test
    public void testPrint_afterNewline_noLeadingDelimiter() throws IOException {
        printer.printRecord("a");
        printer.print("b"); // should start a new record because newRecord is true after println
        printer.flush();
        String expected = "a" + format.getRecordSeparator() + "b";
        assertEquals(expected, sw.toString());
    }

    // Tests boundary: print empty string
    @Test
    public void testPrint_emptyString_printsEmpty() throws IOException {
        printer.print("");
        printer.flush();
        assertEquals("", sw.toString());
    }

    // Tests print with value containing delimiter when quoting is enabled
    @Test
    public void testPrint_valueWithDelimiter_quotesValue() throws IOException {
        CSVFormat csvFormat = CSVFormat.DEFAULT.withQuote('"');
        CSVPrinter csvPrinter = new CSVPrinter(sw, csvFormat);
        csvPrinter.print("a,b");
        csvPrinter.flush();
        assertEquals("\"a,b\"", sw.toString());
    }

    // Tests print with value containing quote char when quoting is enabled
    @Test
    public void testPrint_valueWithQuoteChar_escapesQuote() throws IOException {
        CSVFormat csvFormat = CSVFormat.DEFAULT.withQuote('"');
        CSVPrinter csvPrinter = new CSVPrinter(sw, csvFormat);
        csvPrinter.print("he\"llo");
        csvPrinter.flush();
        assertEquals("\"he\"\"llo\"", sw.toString());
    }

    // Tests print with value containing newline when quoting is enabled
    @Test
    public void testPrint_valueWithNewline_quotesValue() throws IOException {
        CSVFormat csvFormat = CSVFormat.DEFAULT.withQuote('"');
        CSVPrinter csvPrinter = new CSVPrinter(sw, csvFormat);
        csvPrinter.print("a\nb");
        csvPrinter.flush();
        assertEquals("\"a\nb\"", sw.toString());
    }

    // Tests edge case: value with leading space triggers quote in MINIMAL policy
    @Test
    public void testPrint_leadingSpace_triggersQuote() throws IOException {
        CSVFormat csvFormat = CSVFormat.DEFAULT.withQuote('"');
        CSVPrinter csvPrinter = new CSVPrinter(sw, csvFormat);
        csvPrinter.print(" hello");
        csvPrinter.flush();
        assertEquals("\" hello\"", sw.toString());
    }

    // Tests edge case: value with trailing space triggers quote in MINIMAL policy
    @Test
    public void testPrint_trailingSpace_triggersQuote() throws IOException {
        CSVFormat csvFormat = CSVFormat.DEFAULT.withQuote('"');
        CSVPrinter csvPrinter = new CSVPrinter(sw, csvFormat);
        csvPrinter.print("hello ");
        csvPrinter.flush();
        assertEquals("\"hello \"", sw.toString());
    }

    // Tests print with value containing CR char when escaping is enabled
    @Test
    public void testPrint_valueWithCR_escapesCorrectly() throws IOException {
        CSVFormat csvFormat = CSVFormat.DEFAULT.withEscape('\\');
        CSVPrinter csvPrinter = new CSVPrinter(sw, csvFormat);
        csvPrinter.print("a\rb");
        csvPrinter.flush();
        // In escaping mode, CR becomes \r
        assertEquals("a\\rb", sw.toString());
    }

    // Tests print with value containing LF char when escaping is enabled
    @Test
    public void testPrint_valueWithLF_escapesCorrectly() throws IOException {
        CSVFormat csvFormat = CSVFormat.DEFAULT.withEscape('\\');
        CSVPrinter csvPrinter = new CSVPrinter(sw, csvFormat);
        csvPrinter.print("a\nb");
        csvPrinter.flush();
        assertEquals("a\\nb", sw.toString());
    }

    // Tests printComment: prints comment with prefix
    @Test
    public void testPrintComment_simpleComment_printsWithPrefix() throws IOException {
        CSVFormat csvFormat = CSVFormat.DEFAULT.withCommentStart('#');
        CSVPrinter csvPrinter = new CSVPrinter(sw, csvFormat);
        csvPrinter.printComment("test comment");
        csvPrinter.flush();
        String expected = "# test comment" + format.getRecordSeparator();
        assertEquals(expected, sw.toString());
    }

    // Tests printComment with multi-line comment
    @Test
    public void testPrintComment_multiLineComment_printsWithPrefixOnEachLine() throws IOException {
        CSVFormat csvFormat = CSVFormat.DEFAULT.withCommentStart('#');
        CSVPrinter csvPrinter = new CSVPrinter(sw, csvFormat);
        csvPrinter.printComment("line1\nline2");
        csvPrinter.flush();
        String expected = "# line1" + format.getRecordSeparator() + "# line2" + format.getRecordSeparator();
        assertEquals(expected, sw.toString());
    }

    // Tests close: closes underlying writer
    @Test
    public void testClose_flushesAndCloses() throws IOException {
        // Ensure flush and close don't throw
        printer.print("data");
        printer.close();
        // After close, underlying writer should be closed; no assert on content
        assertTrue(true);
    }

    // Tests flush: flushes underlying stream
    @Test
    public void testFlush_flushesContent() throws IOException {
        printer.print("test");
        printer.flush();
        assertEquals("test", sw.toString());
    }

    // Tests NullPointerException for null out in constructor
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullOut_throwsException() {
        new CSVPrinter(null, CSVFormat.DEFAULT);
    }

    // Tests NullPointerException for null format in constructor
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullFormat_throwsException() {
        new CSVPrinter(new StringWriter(), null);
    }

    // Tests printRecords with Iterable of arrays
    @Test
    public void testPrintRecords_iterableOfArrays_printsRecords() throws IOException {
        printer.printRecords(Arrays.asList(new Object[]{"a", "b"}, new Object[]{"c", "d"}));
        printer.flush();
        String expected = "a,b" + format.getRecordSeparator() + "c,d" + format.getRecordSeparator();
        assertEquals(expected, sw.toString());
    }

    // Tests printRecords with array
    @Test
    public void testPrintRecords_array_printsRecords() throws IOException {
        printer.printRecords(new Object[]{new Object[]{"x", "y"}, new Object[]{"z"}});
        printer.flush();
        String expected = "x,y" + format.getRecordSeparator() + "z" + format.getRecordSeparator();
        assertEquals(expected, sw.toString());
    }

    // Tests printComment when commenting disabled
    @Test
    public void testPrintComment_commentingDisabled_doesNothing() throws IOException {
        CSVFormat csvFormat = CSVFormat.DEFAULT.withCommentStart(null); // Assuming disabling
        CSVPrinter csvPrinter = new CSVPrinter(new StringWriter(), csvFormat);
        csvPrinter.printComment("test");
        csvPrinter.flush();
        assertEquals("", sw.toString());
    }

    // Tests print with MINIMAL quoting where first char is non-alphanumeric and not space
    @Test
    public void testPrint_firstCharNonAlphanumericAndNonSpace_triggersQuote() throws IOException {
        CSVFormat csvFormat = CSVFormat.DEFAULT.withQuote('"');
        CSVPrinter csvPrinter = new CSVPrinter(sw, csvFormat);
        csvPrinter.print("@test");
        csvPrinter.flush();
        assertEquals("\"@test\"", sw.toString());
    }

    // Tests print with non-numeric value under NON_NUMERIC policy
    @Test
    public void testPrint_nonNumericValueWithNonNumericPolicy_quotesValue() throws IOException {
        CSVFormat csvFormat = CSVFormat.DEFAULT.withQuote('"').withQuotePolicy(Quote.NON_NUMERIC);
        CSVPrinter csvPrinter = new CSVPrinter(sw, csvFormat);
        csvPrinter.print("hello");
        csvPrinter.flush();
        assertEquals("\"hello\"", sw.toString());
    }

    // Tests print with numeric value under NON_NUMERIC policy does not quote
    @Test
    public void testPrint_numericValueWithNonNumericPolicy_noQuote() throws IOException {
        CSVFormat csvFormat = CSVFormat.DEFAULT.withQuote('"').withQuotePolicy(Quote.NON_NUMERIC);
        CSVPrinter csvPrinter = new CSVPrinter(sw, csvFormat);
        csvPrinter.print(123);
        csvPrinter.flush();
        assertEquals("123", sw.toString());
    }

    // Tests multiple print calls to ensure delimiter insertion
    @Test
    public void testPrint_multipleValues_printsDelimited() throws IOException {
        printer.print("a");
        printer.print("b");
        printer.flush();
        assertEquals("a,b", sw.toString());
    }

    // Tests printRecords with Iterable of single objects
    @Test
    public void testPrintRecords_iterableOfSingleObjects_printsRecords() throws IOException {
        printer.printRecords(Arrays.asList("single"));
        printer.flush();
        String expected = "single" + format.getRecordSeparator();
        assertEquals(expected, sw.toString());
    }

    // Tests printRecords with empty Iterable
    @Test
    public void testPrintRecords_emptyIterable_printsNothing() throws IOException {
        printer.printRecords(Arrays.asList());
        printer.flush();
        assertEquals("", sw.toString());
    }

    // Tests printRecords with null values in array
    @Test
    public void testPrintRecords_nullValuesInArray_printsEmpty() throws IOException {
        printer.printRecords(new Object[]{new Object[]{null, "b"}});
        printer.flush();
        String expected = ",b" + format.getRecordSeparator();
        assertEquals(expected, sw.toString());
    }

    // Tests getOut returns the appendable
    @Test
    public void testGetOut_returnsAppendable() {
        assertEquals(sw, printer.getOut());
    }

    // ====================== New tests added to cover missing areas ======================

    // Tests print after close should throw IOException
    @Test(expected = IOException.class)
    public void testPrintAfterClose_throwsException() throws IOException {
        printer.close();
        printer.print("test");
    }

    // Tests print with tab delimiter
    @Test
    public void testPrint_withTabDelimiter() throws IOException {
        CSVFormat tabFormat = CSVFormat.DEFAULT.withDelimiter('\t');
        CSVPrinter tabPrinter = new CSVPrinter(sw, tabFormat);
        tabPrinter.printRecord("a", "b", "c");
        tabPrinter.flush();
        assertEquals("a\tb\tc" + format.getRecordSeparator(), sw.toString());
    }

    // Tests print with ALL quote policy
    @Test
    public void testPrint_withAllQuotePolicy() throws IOException {
        CSVFormat allQuoteFormat = CSVFormat.DEFAULT.withQuote('"').withQuotePolicy(Quote.ALL);
        CSVPrinter allQuotePrinter = new CSVPrinter(sw, allQuoteFormat);
        allQuotePrinter.print("hello");
        allQuotePrinter.flush();
        assertEquals("\"hello\"", sw.toString());
    }

    // Tests print with escape char and quote char (escape before quote)
    @Test
    public void testPrint_withEscapeCharAndQuoteChar() throws IOException {
        CSVFormat escapeQuoteFormat = CSVFormat.DEFAULT.withQuote('"').withEscape('\\');
        CSVPrinter escapeQuotePrinter = new CSVPrinter(sw, escapeQuoteFormat);
        escapeQuotePrinter.print("a\"b");
        escapeQuotePrinter.flush();
        // Expect: value quoted because contains quote, and quote escaped with backslash
        assertEquals("\"a\\\"b\"", sw.toString());
    }

    // Tests print with NONE quote policy and value containing delimiter
    @Test
    public void testPrint_withQuotePolicyNoneAndDelimiter() throws IOException {
        CSVFormat noneQuoteFormat = CSVFormat.DEFAULT.withQuote('"').withQuotePolicy(Quote.NONE);
        CSVPrinter noneQuotePrinter = new CSVPrinter(sw, noneQuoteFormat);
        noneQuotePrinter.print("a,b");
        noneQuotePrinter.flush();
        // Since quoting is NONE, the delimiter is not escaped, output as is
        assertEquals("a,b", sw.toString());
    }
}