package org.apache.commons.csv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Test;

public class CSVPrinterTest {

    @Test
    // Tests print(null) when nullString is null -> prints empty
    public void testPrint_nullValue_nullStringNull_printsEmpty() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVPrinter printer = new CSVPrinter(sb, CSVFormat.DEFAULT);
        printer.print((Object) null);
        assertEquals("", sb.toString());
    }

    @Test
    // Tests print(null) when nullString is set -> prints nullString
    public void testPrint_nullValue_nullStringSet_printsNullString() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        CSVPrinter printer = new CSVPrinter(sb, format);
        printer.print((Object) null);
        assertEquals("NULL", sb.toString());
    }

    @Test
    // Tests print(empty string) as first field (newRecord true) -> quotes empty token
    public void testPrint_emptyString_newRecord_quotes() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVPrinter printer = new CSVPrinter(sb, CSVFormat.DEFAULT);
        printer.print("");
        assertEquals("\"\"", sb.toString());
    }

    @Test
    // Tests print(empty string) as second field (newRecord false) -> does not quote
    public void testPrint_emptyString_notNewRecord_noQuotes() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVPrinter printer = new CSVPrinter(sb, CSVFormat.DEFAULT);
        printer.print("a");
        printer.print("");
        assertEquals("a,", sb.toString());
    }

    @Test
    // Tests print(value containing delimiter) -> quotes
    public void testPrint_valueWithDelimiter_quotes() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVPrinter printer = new CSVPrinter(sb, CSVFormat.DEFAULT);
        printer.print("a,b");
        assertEquals("\"a,b\"", sb.toString());
    }

    @Test
    // Tests print(value containing quote char) -> quotes and doubles quote
    public void testPrint_valueWithQuote_quotesAndDoublesQuote() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVPrinter printer = new CSVPrinter(sb, CSVFormat.DEFAULT);
        printer.print("a\"b");
        assertEquals("\"a\"\"b\"", sb.toString());
    }

    @Test
    // Tests print(value containing LF) -> quotes
    public void testPrint_valueWithLF_quotes() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVPrinter printer = new CSVPrinter(sb, CSVFormat.DEFAULT);
        printer.print("a\nb");
        assertEquals("\"a\nb\"", sb.toString());
    }

    @Test
    // Tests print(value containing CR) -> quotes
    public void testPrint_valueWithCR_quotes() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVPrinter printer = new CSVPrinter(sb, CSVFormat.DEFAULT);
        printer.print("a\rb");
        assertEquals("\"a\rb\"", sb.toString());
    }

    @Test
    // Tests printAndQuote with ALL policy -> always quotes
    public void testPrint_quotePolicyAll_alwaysQuotes() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVFormat format = CSVFormat.DEFAULT.withQuotePolicy(Quote.ALL);
        CSVPrinter printer = new CSVPrinter(sb, format);
        printer.print("abc");
        assertEquals("\"abc\"", sb.toString());
    }

    @Test
    // Tests printAndQuote with NON_NUMERIC policy: number not quoted, string quoted
    public void testPrint_quotePolicyNonNumeric_numberAndString() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVFormat format = CSVFormat.DEFAULT.withQuotePolicy(Quote.NON_NUMERIC);
        CSVPrinter printer = new CSVPrinter(sb, format);
        printer.print(123);
        printer.print("abc");
        assertEquals("123,\"abc\"", sb.toString());
    }

    @Test
    // Tests printAndQuote with NONE policy (quoting disabled, escaping enabled)
    public void testPrint_quotePolicyNone_escapesSpecialChars() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVFormat format = CSVFormat.DEFAULT.withQuotePolicy(Quote.NONE).withEscape('\\');
        CSVPrinter printer = new CSVPrinter(sb, format);
        printer.print("a,b");
        assertEquals("a\\,b", sb.toString());
    }

    @Test
    // Tests printAndEscape when quoting is disabled and escaping enabled (delimiter, LF, CR, escape)
    public void testPrint_escapeModeBasic_escapesSpecialChars() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVFormat format = CSVFormat.DEFAULT.withQuote(null).withEscape('\\');
        CSVPrinter printer = new CSVPrinter(sb, format);
        printer.print("a,b\nc\rd\\e");
        assertEquals("a\\,b\\nc\\rd\\\\e", sb.toString());
    }

    @Test
    // Tests printComment when commenting is enabled
    public void testPrintComment_enabled_printsCommentWithHash() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        CSVPrinter printer = new CSVPrinter(sb, format);
        printer.printComment("test comment");
        assertTrue(sb.toString().contains("# test comment"));
    }

    @Test
    // Tests println -> outputs record separator and sets newRecord
    public void testPrintln_newRecordSet() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVPrinter printer = new CSVPrinter(sb, CSVFormat.DEFAULT);
        printer.print("a");
        printer.println();
        assertEquals("a\r\n", sb.toString());
    }

    @Test
    // Tests printRecord with varargs
    public void testPrintRecord_varargs_printsValuesAndRecordSeparator() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVPrinter printer = new CSVPrinter(sb, CSVFormat.DEFAULT);
        printer.printRecord("a", "b", "c");
        assertEquals("a,b,c\r\n", sb.toString());
    }

    @Test
    // Tests printRecord with Iterable
    public void testPrintRecord_iterable_printsValuesAndRecordSeparator() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVPrinter printer = new CSVPrinter(sb, CSVFormat.DEFAULT);
        printer.printRecord(Arrays.asList("x", "y"));
        assertEquals("x,y\r\n", sb.toString());
    }

    @Test
    // Tests printRecords with array containing various types
    public void testPrintRecords_array_printsMultipleRecords() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVPrinter printer = new CSVPrinter(sb, CSVFormat.DEFAULT);
        Object[] records = new Object[] {
            new String[] {"1", "2"},
            "single",
            Arrays.asList("a", "b")
        };
        printer.printRecords(records);
        assertEquals("1,2\r\nsingle\r\na,b\r\n", sb.toString());
    }

    @Test
    // Tests MINIMAL quoting on value ending with space (c <= SP at end)
    public void testPrint_valueEndsWithSpace_quotes() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVPrinter printer = new CSVPrinter(sb, CSVFormat.DEFAULT);
        printer.print("abc ");
        assertEquals("\"abc \"", sb.toString());
    }

    @Test
    // Tests MINIMAL quoting on value starting with char <= COMMENT ('!')
    public void testPrint_valueStartsWithCharLessThanComment_quotes() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVPrinter printer = new CSVPrinter(sb, CSVFormat.DEFAULT);
        printer.print("!hello");
        assertEquals("\"!hello\"", sb.toString());
    }

    @Test
    // Tests flush on a non-Flushable Appendable (StringBuilder) -> does nothing
    public void testFlush_nonFlushable_doesNothing() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVPrinter printer = new CSVPrinter(sb, CSVFormat.DEFAULT);
        printer.flush();
    }

    // ========== New test cases for uncovered coverage ==========

    @Test
    // Tests print with quoted character when using non-standard delimiter
    public void testPrint_valueWithSemicolonDelimiter_usesCustomDelimiter() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(';');
        CSVPrinter printer = new CSVPrinter(sb, format);
        printer.print("a;b");
        assertEquals("\"a;b\"", sb.toString());
    }

    @Test
    // Tests MINIMAL quoting on value containing CRLF
    public void testPrint_valueWithCRLF_quotes() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVPrinter printer = new CSVPrinter(sb, CSVFormat.DEFAULT);
        printer.print("a\r\nb");
        assertEquals("\"a\r\nb\"", sb.toString());
    }

    @Test
    // Tests print with empty CSVPrinter and then printing to see newRecord behavior
    public void testPrint_firstFieldAfterNewRecord_quotesEmpty() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVPrinter printer = new CSVPrinter(sb, CSVFormat.DEFAULT);
        printer.println();
        printer.print("");
        assertEquals("\r\n\"\"", sb.toString());
    }

    @Test
    // Tests printComment with multi-line comment
    public void testPrintComment_multiline_commentFormatted() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        CSVPrinter printer = new CSVPrinter(sb, format);
        printer.printComment("line1\nline2");
        assertTrue(sb.toString().contains("# line1\n# line2"));
    }

    @Test
    // Tests printRecord with null element in array
    public void testPrintRecord_withNullElement_handlesNull() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVFormat format = CSVFormat.DEFAULT.withNullString("N/A");
        CSVPrinter printer = new CSVPrinter(sb, format);
        printer.printRecord("a", null, "b");
        assertEquals("a,N/A,b\r\n", sb.toString());
    }

    @Test
    // Tests printRecords with empty array
    public void testPrintRecords_emptyArray_noOutput() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVPrinter printer = new CSVPrinter(sb, CSVFormat.DEFAULT);
        printer.printRecords(new Object[0]);
        assertEquals("", sb.toString());
    }

    @Test
    // Tests close method behavior (via try-with-resources)
    public void testClose_closesResources() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVPrinter printer = new CSVPrinter(sb, CSVFormat.DEFAULT);
        printer.close();
        // Should not throw and should close underlying appendable if possible
    }

    @Test
    // Tests print with MINIMAL quoting and value containing only spaces
    public void testPrint_valueIsOnlySpaces_quotes() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVPrinter printer = new CSVPrinter(sb, CSVFormat.DEFAULT);
        printer.print("   ");
        assertEquals("\"   \"", sb.toString());
    }

    @Test
    // Tests print with quote char that equals delimiter
    public void testPrint_quoteEqualsDelimiter_handlesCorrectly() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter('"').withQuote('"');
        CSVPrinter printer = new CSVPrinter(sb, format);
        printer.print("test");
        assertEquals("\"test\"", sb.toString());
    }
}