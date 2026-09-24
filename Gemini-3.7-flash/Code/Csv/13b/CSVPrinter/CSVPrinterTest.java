package org.apache.commons.csv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

/**
 * Unit tests for {@link CSVPrinter}.
 */
public class CSVPrinterTest {

    // Tests constructor exception on null Appendable out
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullAppendable_throwsException() throws IOException {
        new CSVPrinter(null, CSVFormat.DEFAULT);
    }

    // Tests constructor exception on null CSVFormat
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullFormat_throwsException() throws IOException {
        new CSVPrinter(new StringWriter(), null);
    }

    // Tests constructor printing header comments and header record
    @Test
    public void testConstructor_headerAndComments_printsHeaderAndComments() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVFormat format = CSVFormat.DEFAULT
                .withCommentMarker('#')
                .withHeaderComments("Comment line 1", "Comment line 2")
                .withHeader("Col1", "Col2");
        new CSVPrinter(sw, format);
        final String expected = "# Comment line 1\r\n# Comment line 2\r\nCol1,Col2\r\n";
        assertEquals(expected, sw.toString());
    }

    // Tests constructor with skipHeaderRecord option
    @Test
    public void testConstructor_skipHeaderRecord_doesNotPrintHeader() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVFormat format = CSVFormat.DEFAULT
                .withHeader("Col1", "Col2")
                .withSkipHeaderRecord(true);
        new CSVPrinter(sw, format);
        assertEquals("", sw.toString());
    }

    // Tests getOut returns the underlying Appendable
    @Test
    public void testGetOut_returnsAppendable() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        assertSame(sw, printer.getOut());
        printer.close();
    }

    // Tests print with null value when nullString is not configured
    @Test
    public void testPrint_nullValueDefaultFormat_printsEmpty() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        printer.print(null);
        printer.print("a");
        assertEquals(",a", sw.toString());
        printer.close();
    }

    // Tests print with null value when nullString is configured
    @Test
    public void testPrint_nullValueWithNullString_printsConfiguredNullString() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        final CSVPrinter printer = new CSVPrinter(sw, format);
        printer.print(null);
        printer.print("value");
        assertEquals("NULL,value", sw.toString());
        printer.close();
    }

    // Tests QuoteMode.ALL encapsulates all printed values
    @Test
    public void testPrint_quoteModeAll_quotesAllValues() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL);
        final CSVPrinter printer = new CSVPrinter(sw, format);
        printer.print("hello");
        printer.print(123);
        assertEquals("\"hello\",\"123\"", sw.toString());
        printer.close();
    }

    // Tests QuoteMode.NON_NUMERIC quotes strings but not numbers
    @Test
    public void testPrint_quoteModeNonNumeric_quotesStringsOnly() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.NON_NUMERIC);
        final CSVPrinter printer = new CSVPrinter(sw, format);
        printer.print("text");
        printer.print(Integer.valueOf(42));
        printer.print(Double.valueOf(3.14));
        assertEquals("\"text\",42,3.14", sw.toString());
        printer.close();
    }

    // Tests QuoteMode.NONE uses escaping instead of quotes
    @Test
    public void testPrint_quoteModeNone_escapesSpecialCharacters() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.NONE).withEscape('\\');
        final CSVPrinter printer = new CSVPrinter(sw, format);
        printer.print("a,b");
        printer.print("c\nd");
        assertEquals("a\\,b,c\\nd", sw.toString());
        printer.close();
    }

    // Tests quoting values containing quotes, delimiters, and leading/trailing spaces
    @Test
    public void testPrint_specialCharacters_quotesAndDoublesQuotes() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        printer.print("a\"b");
        printer.print(" leading");
        printer.print("trailing ");
        assertEquals("\"a\"\"b\",\" leading\",\"trailing \"", sw.toString());
        printer.close();
    }

    // Tests pure escape format without quoting
    @Test
    public void testPrint_escapeFormatWithoutQuotes_escapesDelimiterAndCRLF() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVFormat format = CSVFormat.newFormat(',').withEscape('\\').withRecordSeparator("\r\n");
        final CSVPrinter printer = new CSVPrinter(sw, format);
        printer.print("a,b");
        printer.print("c\rd\ne");
        assertEquals("a\\,b,c\\rd\\ne", sw.toString());
        printer.close();
    }

    // Tests printComment when commentMarker is not configured
    @Test
    public void testPrintComment_disabledCommentMarker_doesNothing() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        printer.printComment("This comment should not be printed");
        assertEquals("", sw.toString());
        printer.close();
    }

    // Tests printComment handling multi-line comments with CR, LF, and CRLF
    @Test
    public void testPrintComment_multiLineComment_printsCommentPrefixEachLine() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        final CSVPrinter printer = new CSVPrinter(sw, format);
        printer.print("data");
        printer.printComment("Line1\nLine2\rLine3\r\nLine4");
        final String expected = "data\r\n# Line1\r\n# Line2\r\n# Line3\r\n# Line4\r\n";
        assertEquals(expected, sw.toString());
        printer.close();
    }

    // Tests printRecord with Object array
    @Test
    public void testPrintRecord_objectArray_printsRecord() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        printer.printRecord("v1", "v2", "v3");
        assertEquals("v1,v2,v3\r\n", sw.toString());
        printer.close();
    }

    // Tests printRecord with Iterable
    @Test
    public void testPrintRecord_iterable_printsRecord() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        final List<String> list = Arrays.asList("item1", "item2");
        printer.printRecord(list);
        assertEquals("item1,item2\r\n", sw.toString());
        printer.close();
    }

    // Tests printRecords with nested arrays and simple objects
    @Test
    public void testPrintRecords_nestedArrays_printsMultipleRecords() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        final Object[] records = new Object[] {
            new String[] { "r1c1", "r1c2" },
            new String[] { "r2c1", "r2c2" },
            "simple"
        };
        printer.printRecords(records);
        final String expected = "r1c1,r1c2\r\nr2c1,r2c2\r\nsimple\r\n";
        assertEquals(expected, sw.toString());
        printer.close();
    }

    // Tests printRecords with nested Iterable collections
    @Test
    public void testPrintRecords_nestedIterable_printsMultipleRecords() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        final List<Object> records = Arrays.<Object>asList(
            Arrays.asList("a", "b"),
            Arrays.asList("c", "d")
        );
        printer.printRecords(records);
        final String expected = "a,b\r\nc,d\r\n";
        assertEquals(expected, sw.toString());
        printer.close();
    }

    // Tests flush and close methods on underlying Appendable
    @Test
    public void testFlushAndClose_delegatesToAppendable() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        printer.print("test");
        printer.flush();
        printer.close();
        assertEquals("test", sw.toString());
    }

    // Tests println explicitly outputs record separator
    @Test
    public void testPrintln() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        printer.print("a");
        printer.println();
        printer.print("b");
        printer.println();
        assertEquals("a\r\nb\r\n", sw.toString());
        printer.close();
    }

    // Tests printRecord with no arguments prints an empty record line
    @Test
    public void testPrintRecord_empty() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        printer.printRecord();
        assertEquals("\r\n", sw.toString());
        printer.close();
    }

    // Tests printComment at the beginning of a record
    @Test
    public void testPrintComment_onNewRecord() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        final CSVPrinter printer = new CSVPrinter(sw, format);
        printer.printComment("Initial comment");
        assertEquals("# Initial comment\r\n", sw.toString());
        printer.close();
    }

    // Tests print with value starting with comment marker
    @Test
    public void testPrint_valueStartsWithCommentMarker() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        final CSVPrinter printer = new CSVPrinter(sw, format);
        printer.print("#commentLike");
        assertEquals("\"#commentLike\"", sw.toString());
        printer.close();
    }

    // Tests print with Reader value
    @Test
    public void testPrint_reader() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        final StringReader reader = new StringReader("fromReader");
        printer.print(reader);
        assertEquals("fromReader", sw.toString());
        printer.close();
    }

    // Tests print with Reader value containing special characters
    @Test
    public void testPrint_readerWithSpecialCharacters() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        final StringReader reader = new StringReader("a,b\r\nc\"d");
        printer.print(reader);
        assertEquals("\"a,b\r\nc\"\"d\"", sw.toString());
        printer.close();
    }

    // Tests QuoteMode.NONE without escape character throws IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testPrint_quoteModeNoneWithoutEscape_throwsException() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.NONE).withQuote(null);
        final CSVPrinter printer = new CSVPrinter(sw, format);
        printer.print("a,b");
    }

    // Tests printRecords with ResultSet
    @Test
    public void testPrintRecords_resultSet() throws SQLException, IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);

        final ResultSetMetaData metaData = (ResultSetMetaData) Proxy.newProxyInstance(
            CSVPrinterTest.class.getClassLoader(),
            new Class<?>[] { ResultSetMetaData.class },
            new InvocationHandler() {
                @Override
                public Object invoke(final Object proxy, final Method method, final Object[] args) {
                    if ("getColumnCount".equals(method.getName())) {
                        return 2;
                    }
                    if ("getColumnLabel".equals(method.getName()) || "getColumnName".equals(method.getName())) {
                        return "Col" + args[0];
                    }
                    return null;
                }
            }
        );

        final int[] row = new int[] { 0 };
        final ResultSet resultSet = (ResultSet) Proxy.newProxyInstance(
            CSVPrinterTest.class.getClassLoader(),
            new Class<?>[] { ResultSet.class },
            new InvocationHandler() {
                @Override
                public Object invoke(final Object proxy, final Method method, final Object[] args) {
                    if ("getMetaData".equals(method.getName())) {
                        return metaData;
                    }
                    if ("next".equals(method.getName())) {
                        row[0]++;
                        return row[0] <= 2;
                    }
                    if ("getString".equals(method.getName()) || "getObject".equals(method.getName())) {
                        return "r" + row[0] + "c" + args[0];
                    }
                    return null;
                }
            }
        );

        printer.printRecords(resultSet);
        assertEquals("r1c1,r1c2\r\nr2c1,r2c2\r\n", sw.toString());
        printer.close();
    }
}