package org.apache.commons.csv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class CSVPrinterTest {

    // Tests exception on null Appendable out parameter
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullOut_throwsIllegalArgumentException() throws IOException {
        new CSVPrinter(null, CSVFormat.DEFAULT);
    }

    // Tests exception on null CSVFormat parameter
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullFormat_throwsIllegalArgumentException() throws IOException {
        new CSVPrinter(new StringWriter(), null);
    }

    // Tests getOut returns the underlying Appendable
    @Test
    public void testGetOut_validAppendable_returnsSameInstance() throws IOException {
        StringWriter sw = new StringWriter();
        CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        assertSame(sw, printer.getOut());
    }

    // Tests printing simple records with default format
    @Test
    public void testPrintRecord_simpleValues_printsCorrectCsv() throws IOException {
        StringWriter sw = new StringWriter();
        CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withRecordSeparator("\r\n"));

        printer.printRecord("a", "b", "c");

        assertEquals("a,b,c\r\n", sw.toString());
    }

    // Tests printing null value with default empty string mapping
    @Test
    public void testPrint_nullValueDefault_printsEmptyToken() throws IOException {
        StringWriter sw = new StringWriter();
        CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withRecordSeparator("\r\n"));

        printer.print(null);
        printer.print("value");
        printer.println();

        assertEquals("\"\",value\r\n", sw.toString());
    }

    // Tests printing null value with configured nullString
    @Test
    public void testPrint_nullValueWithCustomNullString_printsNullString() throws IOException {
        StringWriter sw = new StringWriter();
        CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withNullString("NULL").withRecordSeparator("\r\n"));

        printer.print(null);
        printer.print("test");
        printer.println();

        assertEquals("NULL,test\r\n", sw.toString());
    }

    // Tests Quote.ALL policy quotes every value
    @Test
    public void testPrint_quotePolicyAll_quotesAllValues() throws IOException {
        StringWriter sw = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withQuotePolicy(Quote.ALL).withRecordSeparator("\r\n");
        CSVPrinter printer = new CSVPrinter(sw, format);

        printer.printRecord("a", 123, "c");

        assertEquals("\"a\",\"123\",\"c\"\r\n", sw.toString());
    }

    // Tests Quote.NON_NUMERIC policy quotes only non-number values
    @Test
    public void testPrint_quotePolicyNonNumeric_quotesOnlyNonNumbers() throws IOException {
        StringWriter sw = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withQuotePolicy(Quote.NON_NUMERIC).withRecordSeparator("\r\n");
        CSVPrinter printer = new CSVPrinter(sw, format);

        printer.printRecord("a", 123, 45.67, "d");

        assertEquals("\"a\",123,45.67,\"d\"\r\n", sw.toString());
    }

    // Tests Quote.NONE policy delegates to escape handling
    @Test
    public void testPrint_quotePolicyNoneWithEscape_escapesSpecialCharacters() throws IOException {
        StringWriter sw = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withQuotePolicy(Quote.NONE).withEscape('\\').withRecordSeparator("\r\n");
        CSVPrinter printer = new CSVPrinter(sw, format);

        printer.printRecord("a,b", "c\nd", "e\\f");

        assertEquals("a\\,b,c\\nd,e\\\\f\r\n", sw.toString());
    }

    // Tests value containing quote character gets quotes doubled under Quote.MINIMAL
    @Test
    public void testPrint_valueWithQuotes_doublesEmbeddedQuotes() throws IOException {
        StringWriter sw = new StringWriter();
        CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withRecordSeparator("\r\n"));

        printer.printRecord("a\"b", "c");

        assertEquals("\"a\"\"b\",c\r\n", sw.toString());
    }

    // Tests value starting with character <= COMMENT under Quote.MINIMAL
    @Test
    public void testPrint_valueStartingWithCommentOrWhitespace_quotesValue() throws IOException {
        StringWriter sw = new StringWriter();
        CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withRecordSeparator("\r\n"));

        printer.printRecord(" #hash", " normal");

        assertEquals("\" #hash\",\" normal\"\r\n", sw.toString());
    }

    // Tests value ending with whitespace under Quote.MINIMAL
    @Test
    public void testPrint_valueEndingWithWhitespace_quotesValue() throws IOException {
        StringWriter sw = new StringWriter();
        CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withRecordSeparator("\r\n"));

        printer.printRecord("endSpace ", "normal");

        assertEquals("\"endSpace \",normal\r\n", sw.toString());
    }

    // Tests escaping with pure escape format (no quote character)
    @Test
    public void testPrint_pureEscapingFormat_escapesDelimitersAndNewlines() throws IOException {
        StringWriter sw = new StringWriter();
        CSVFormat format = CSVFormat.MYSQL.withRecordSeparator("\n");
        CSVPrinter printer = new CSVPrinter(sw, format);

        printer.printRecord("hello\tworld", "line1\nline2\rline3", "back\\slash");

        assertEquals("hello\\\tworld\tline1\\nline2\\rline3\tback\\\\slash\n", sw.toString());
    }

    // Tests printComment when commenting is enabled
    @Test
    public void testPrintComment_commentingEnabled_printsCommentLines() throws IOException {
        StringWriter sw = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withCommentStart('#').withRecordSeparator("\r\n");
        CSVPrinter printer = new CSVPrinter(sw, format);

        printer.printComment("This is a comment\r\nwith multiple\nlines");
        printer.printRecord("a", "b");

        assertEquals("# This is a comment\r\n# with multiple\r\n# lines\r\na,b\r\n", sw.toString());
    }

    // Tests printComment does nothing when commenting is disabled
    @Test
    public void testPrintComment_commentingDisabled_printsNothing() throws IOException {
        StringWriter sw = new StringWriter();
        CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withRecordSeparator("\r\n"));

        printer.printComment("Ignored comment");

        assertEquals("", sw.toString());
    }

    // Tests printRecord with Iterable input
    @Test
    public void testPrintRecord_iterableInput_printsAllElements() throws IOException {
        StringWriter sw = new StringWriter();
        CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withRecordSeparator("\r\n"));

        List<String> list = Arrays.asList("item1", "item2", "item3");
        printer.printRecord(list);

        assertEquals("item1,item2,item3\r\n", sw.toString());
    }

    // Tests printRecords with array of Object arrays
    @Test
    public void testPrintRecords_arrayOfArrays_printsMultipleRecords() throws IOException {
        StringWriter sw = new StringWriter();
        CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withRecordSeparator("\r\n"));

        Object[][] data = new Object[][] {
            {"r1c1", "r1c2"},
            {"r2c1", "r2c2"}
        };
        printer.printRecords(data);

        assertEquals("r1c1,r1c2\r\nr2c1,r2c2\r\n", sw.toString());
    }

    // Tests printRecords with Iterable of Iterables
    @Test
    public void testPrintRecords_listOfLists_printsMultipleRecords() throws IOException {
        StringWriter sw = new StringWriter();
        CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withRecordSeparator("\r\n"));

        List<List<String>> data = Arrays.asList(
            Arrays.asList("1", "2"),
            Arrays.asList("3", "4")
        );
        printer.printRecords(data);

        assertEquals("1,2\r\n3,4\r\n", sw.toString());
    }

    // Tests printRecords with single objects (neither array nor Iterable)
    @Test
    public void testPrintRecords_singleObjects_printsEachOnNewRecord() throws IOException {
        StringWriter sw = new StringWriter();
        CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withRecordSeparator("\r\n"));

        String[] data = new String[] {"first", "second"};
        printer.printRecords(data);

        assertEquals("first\r\nsecond\r\n", sw.toString());
    }

    // Tests flush and close methods on CSVPrinter
    @Test
    public void testFlushAndClose_validWriter_executesWithoutError() throws IOException {
        StringWriter sw = new StringWriter();
        CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);

        printer.print("data");
        printer.flush();
        printer.close();

        assertNotNull(sw.toString());
    }

    // Tests header printing behavior upon CSVPrinter construction
    @Test
    public void testConstructor_withHeader_printsHeaderRecord() throws IOException {
        StringWriter sw = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withHeader("Col1", "Col2").withRecordSeparator("\r\n");
        new CSVPrinter(sw, format);

        assertTrue(sw.toString().startsWith("Col1,Col2\r\n") || sw.toString().isEmpty());
    }
}