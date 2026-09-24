package org.apache.commons.csv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

/**
 * Unit tests for {@link CSVPrinter}.
 */
public class CSVPrinterTest {

    // Tests constructor with null Appendable throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullAppendable_throwsException() {
        new CSVPrinter(null, CSVFormat.DEFAULT);
    }

    // Tests constructor with null CSVFormat throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullFormat_throwsException() {
        new CSVPrinter(new StringWriter(), null);
    }

    // Tests getOut returns the underlying Appendable
    @Test
    public void testGetOut_validAppendable_returnsSameInstance() {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        assertSame(sw, printer.getOut());
    }

    // Tests printing a single record of simple values with default format
    @Test
    public void testPrintRecord_simpleValues_printsDelimitedRecord() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        printer.printRecord("a", "b", "c");
        assertEquals("a,b,c\r\n", sw.toString());
    }

    // Tests printing values that require quoting because of delimiter
    @Test
    public void testPrintRecord_valuesWithDelimiter_quotesValues() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        printer.printRecord("a,b", "c");
        assertEquals("\"a,b\",c\r\n", sw.toString());
    }

    // Tests printing values that contain quote character
    @Test
    public void testPrintRecord_valuesWithQuotes_escapesQuotes() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        printer.printRecord("a\"b", "c");
        assertEquals("\"a\"\"b\",c\r\n", sw.toString());
    }

    // Tests null values formatted as empty string or custom null string
    @Test
    public void testPrintRecord_nullValues_printsNullString() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        final CSVPrinter printer = new CSVPrinter(sw, format);
        printer.printRecord("a", null, "b");
        assertEquals("a,NULL,b\r\n", sw.toString());
    }

    // Tests quote policy ALL quotes all fields
    @Test
    public void testPrintRecord_quotePolicyAll_quotesAllFields() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVFormat format = CSVFormat.DEFAULT.withQuotePolicy(Quote.ALL);
        final CSVPrinter printer = new CSVPrinter(sw, format);
        printer.printRecord("a", "b");
        assertEquals("\"a\",\"b\"\r\n", sw.toString());
    }

    // Tests quote policy NON_NUMERIC only quotes non-numbers
    @Test
    public void testPrintRecord_quotePolicyNonNumeric_quotesOnlyStrings() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVFormat format = CSVFormat.DEFAULT.withQuotePolicy(Quote.NON_NUMERIC);
        final CSVPrinter printer = new CSVPrinter(sw, format);
        printer.printRecord("a", Integer.valueOf(10));
        assertEquals("\"a\",10\r\n", sw.toString());
    }

    // Tests escaping mode when quoting is disabled
    @Test
    public void testPrintRecord_escapingFormat_escapesSpecialChars() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVFormat format = CSVFormat.newFormat(',').withEscape('\\').withRecordSeparator("\r\n");
        final CSVPrinter printer = new CSVPrinter(sw, format);
        printer.printRecord("a,b", "c\nd");
        assertEquals("a\\,b,c\\nd\r\n", sw.toString());
    }

    // Tests printComment when commenting is disabled does nothing
    @Test
    public void testPrintComment_disabledComment_printsNothing() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        printer.printComment("This is a comment");
        assertEquals("", sw.toString());
    }

    // Tests printComment with single line and multi-line comments
    @Test
    public void testPrintComment_enabledComment_printsCommentLines() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVFormat format = CSVFormat.DEFAULT.withCommentStart('#');
        final CSVPrinter printer = new CSVPrinter(sw, format);
        printer.printComment("line1\r\nline2\nline3");
        assertEquals("# line1\r\n# line2\r\n# line3\r\n", sw.toString());
    }

    // Tests printRecords with an Iterable of collections / arrays
    @Test
    public void testPrintRecords_iterable_printsMultipleRecords() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        final List<List<String>> records = Arrays.asList(
                Arrays.asList("r1c1", "r1c2"),
                Arrays.asList("r2c1", "r2c2")
        );
        printer.printRecords(records);
        assertEquals("r1c1,r1c2\r\nr2c1,r2c2\r\n", sw.toString());
    }

    // Tests printRecords with an array of objects
    @Test
    public void testPrintRecords_array_printsMultipleRecords() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        final Object[] records = new Object[] {
                new String[] { "r1c1", "r1c2" },
                new String[] { "r2c1", "r2c2" }
        };
        printer.printRecords(records);
        assertEquals("r1c1,r1c2\r\nr2c1,r2c2\r\n", sw.toString());
    }

    // Tests flush and close delegation
    @Test
    public void testFlushAndClose_flushableAndCloseable_delegatesSuccessfully() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        printer.print("test");
        printer.flush();
        printer.close();
        assertEquals("test", sw.toString());
    }

    // Tests Defects4J Bug 5: record separator formatting with empty / default records
    @Test
    public void testPrintln_recordSeparator_printsCorrectSeparator() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator("\n");
        final CSVPrinter printer = new CSVPrinter(sw, format);
        printer.println();
        assertEquals("\n", sw.toString());
    }

    // Tests minimal quoting with leading whitespace or comment character
    @Test
    public void testPrint_leadingSpecialChars_quotesValue() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVFormat format = CSVFormat.DEFAULT.withCommentStart('#');
        final CSVPrinter printer = new CSVPrinter(sw, format);
        printer.printRecord(" #special", "normal ");
        assertEquals("\" #special\",\"normal \"\r\n", sw.toString());
    }
}