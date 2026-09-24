package org.apache.commons.csv;

import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class CSVParserTest {

    private enum TestHeaders {
        A, B, C
    }

    // Tests simple parsing of CSV string into records
    @Test
    public void testParse_simpleString_returnsRecords() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT;
        final CSVParser parser = CSVParser.parse("a,b,c\n1,2,3", format);
        final List<CSVRecord> records = parser.getRecords();

        assertEquals(2, records.size());
        assertEquals("a", records.get(0).get(0));
        assertEquals("b", records.get(0).get(1));
        assertEquals("c", records.get(0).get(2));
        assertEquals("1", records.get(1).get(0));
        assertEquals("2", records.get(1).get(1));
        assertEquals("3", records.get(1).get(2));
        assertEquals(2, parser.getRecordNumber());
        parser.close();
    }

    // Tests header initialization from first record
    @Test
    public void testParse_withHeaderFromFirstRecord_initializesHeaderMap() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT.withHeader();
        final CSVParser parser = CSVParser.parse("col1,col2,col3\nval1,val2,val3", format);
        final Map<String, Integer> headerMap = parser.getHeaderMap();

        assertNotNull(headerMap);
        assertEquals(3, headerMap.size());
        assertEquals(Integer.valueOf(0), headerMap.get("col1"));
        assertEquals(Integer.valueOf(1), headerMap.get("col2"));
        assertEquals(Integer.valueOf(2), headerMap.get("col3"));

        final List<CSVRecord> records = parser.getRecords();
        assertEquals(1, records.size());
        assertEquals("val1", records.get(0).get("col1"));
        assertEquals(1, records.get(0).getRecordNumber());
        parser.close();
    }

    // Tests predefined header with skipHeaderRecord option
    @Test
    public void testParse_withPredefinedHeaderAndSkipHeader_skipsFirstRecord() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT.withHeader("A", "B").withSkipHeaderRecord(true);
        final CSVParser parser = CSVParser.parse("h1,h2\nv1,v2\nv3,v4", format);
        final List<CSVRecord> records = parser.getRecords();

        assertEquals(2, records.size());
        assertEquals("v1", records.get(0).get("A"));
        assertEquals("v2", records.get(0).get("B"));
        assertEquals("v3", records.get(1).get("A"));
        assertEquals("v4", records.get(1).get("B"));
        parser.close();
    }

    // Tests header containing null or empty string when format defines header with null/empty elements
    @Test
    public void testInitializeHeader_headerWithNullElement_doesNotThrowNPE() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT.withHeader("A", null, "B");
        final CSVParser parser = CSVParser.parse("valA,valEmpty,valB", format);
        final Map<String, Integer> headerMap = parser.getHeaderMap();

        assertNotNull(headerMap);
        assertEquals(Integer.valueOf(0), headerMap.get("A"));
        assertEquals(Integer.valueOf(2), headerMap.get("B"));
        parser.close();
    }

    // Tests duplicate header names throw IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testInitializeHeader_duplicateHeader_throwsIllegalArgumentException() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT.withHeader("col1", "col1");
        CSVParser.parse("val1,val2", format);
    }

    // Tests duplicate empty headers with ignoreEmptyHeaders set to true
    @Test
    public void testInitializeHeader_duplicateEmptyHeadersWithIgnoreEmptyHeaders_succeeds() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT.withHeader("col1", "", "").withIgnoreEmptyHeaders(true);
        final CSVParser parser = CSVParser.parse("val1,val2,val3", format);
        final Map<String, Integer> headerMap = parser.getHeaderMap();

        assertNotNull(headerMap);
        assertEquals(Integer.valueOf(0), headerMap.get("col1"));
        parser.close();
    }

    // Tests null string conversion behavior
    @Test
    public void testParse_withNullString_convertsMatchingValuesToNull() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        final CSVParser parser = CSVParser.parse("a,NULL,c", format);
        final List<CSVRecord> records = parser.getRecords();

        assertEquals(1, records.size());
        assertEquals("a", records.get(0).get(0));
        assertNull(records.get(0).get(1));
        assertEquals("c", records.get(0).get(2));
        parser.close();
    }

    // Tests iterator traversal and line/record counters
    @Test
    public void testIterator_traversalAndHasNext_iteratesAllRecords() throws IOException {
        final CSVParser parser = new CSVParser(new StringReader("a,b\nc,d\ne,f"), CSVFormat.DEFAULT);
        final Iterator<CSVRecord> iterator = parser.iterator();

        assertTrue(iterator.hasNext());
        assertEquals("a", iterator.next().get(0));
        assertTrue(iterator.hasNext());
        assertEquals("c", iterator.next().get(0));
        assertTrue(iterator.hasNext());
        assertEquals("e", iterator.next().get(0));
        assertFalse(iterator.hasNext());
        assertEquals(3, parser.getRecordNumber());
        assertEquals(3, parser.getCurrentLineNumber());
        parser.close();
    }

    // Tests iterator next without hasNext and then throwing NoSuchElementException when exhausted
    @Test(expected = NoSuchElementException.class)
    public void testIterator_nextWhenExhausted_throwsNoSuchElementException() throws IOException {
        final CSVParser parser = CSVParser.parse("a", CSVFormat.DEFAULT);
        final Iterator<Iterator<CSVRecord>> itContainer = null;
        final Iterator<CSVRecord> it = parser.iterator();
        assertNotNull(it.next());
        it.next();
    }

    // Tests iterator remove operation is unsupported
    @Test(expected = UnsupportedOperationException.class)
    public void testIterator_remove_throwsUnsupportedOperationException() throws IOException {
        final CSVParser parser = CSVParser.parse("a,b", CSVFormat.DEFAULT);
        final Iterator<CSVRecord> it = parser.iterator();
        it.remove();
    }

    // Tests close and isClosed behavior
    @Test
    public void testClose_closesParser_isClosedReturnsTrue() throws IOException {
        final CSVParser parser = CSVParser.parse("a,b\nc,d", CSVFormat.DEFAULT);
        assertFalse(parser.isClosed());
        parser.close();
        assertTrue(parser.isClosed());

        final Iterator<CSVRecord> it = parser.iterator();
        assertFalse(it.hasNext());
    }

    // Tests calling next on closed parser throws NoSuchElementException
    @Test(expected = NoSuchElementException.class)
    public void testIterator_nextOnClosedParser_throwsNoSuchElementException() throws IOException {
        final CSVParser parser = CSVParser.parse("a,b", CSVFormat.DEFAULT);
        parser.close();
        final Iterator<CSVRecord> it = parser.iterator();
        it.next();
    }

    // Tests parsing with comments
    @Test
    public void testParse_withCommentMarker_capturesComment() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        final CSVParser parser = CSVParser.parse("# comment\na,b", format);
        final List<CSVRecord> records = parser.getRecords();

        assertEquals(1, records.size());
        assertEquals("comment", records.get(0).getComment());
        assertEquals("a", records.get(0).get(0));
        assertEquals("b", records.get(0).get(1));
        parser.close();
    }

    // Tests null Reader in constructor throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullReader_throwsIllegalArgumentException() throws IOException {
        new CSVParser(null, CSVFormat.DEFAULT);
    }

    // Tests null CSVFormat in constructor throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullFormat_throwsIllegalArgumentException() throws IOException {
        new CSVParser(new StringReader(""), null);
    }

    // Tests null String in parse static method throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testParseString_nullString_throwsIllegalArgumentException() throws IOException {
        CSVParser.parse((String) null, CSVFormat.DEFAULT);
    }

    // Tests null File in parse static method throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testParseFile_nullFile_throwsIllegalArgumentException() throws IOException {
        CSVParser.parse((File) null, Charset.defaultCharset(), CSVFormat.DEFAULT);
    }

    // Tests null URL in parse static method throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testParseURL_nullUrl_throwsIllegalArgumentException() throws IOException {
        CSVParser.parse((URL) null, Charset.defaultCharset(), CSVFormat.DEFAULT);
    }

    // Tests empty input string parsing
    @Test
    public void testParse_emptyInput_returnsEmptyRecordList() throws IOException {
        final CSVParser parser = CSVParser.parse("", CSVFormat.DEFAULT);
        final List<CSVRecord> records = parser.getRecords();

        assertTrue(records.isEmpty());
        assertNull(parser.getHeaderMap());
        assertEquals(0, parser.getRecordNumber());
        parser.close();
    }

    // Tests constructor with custom characterPosition and recordNumber offsets
    @Test
    public void testConstructor_withPositionAndRecordNumberOffsets() throws IOException {
        final CSVParser parser = new CSVParser(new StringReader("x,y\nz,w"), CSVFormat.DEFAULT, 10L, 5L);
        final CSVRecord record1 = parser.nextRecord();

        assertNotNull(record1);
        assertEquals(6L, record1.getRecordNumber());
        assertEquals(6L, parser.getRecordNumber());

        final CSVRecord record2 = parser.nextRecord();
        assertNotNull(record2);
        assertEquals(7L, record2.getRecordNumber());
        assertEquals(7L, parser.getRecordNumber());

        assertNull(parser.nextRecord());
        parser.close();
    }

    // Tests parse File with valid file and charset
    @Test
    public void testParseFile_validFile_parsesSuccessfully() throws IOException {
        final File tempFile = File.createTempFile("csv_test", ".csv");
        tempFile.deleteOnExit();

        final OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(tempFile), StandardCharsets.UTF_8);
        writer.write("c1,c2\nv1,v2");
        writer.close();

        final CSVParser parser = CSVParser.parse(tempFile, StandardCharsets.UTF_8, CSVFormat.DEFAULT.withHeader());
        final List<CSVRecord> records = parser.getRecords();

        assertEquals(1, records.size());
        assertEquals("v1", records.get(0).get("c1"));
        assertEquals("v2", records.get(0).get("c2"));
        parser.close();
    }

    // Tests parse File with null charset throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testParseFile_nullCharset_throwsIllegalArgumentException() throws IOException {
        final File tempFile = File.createTempFile("csv_test_null_charset", ".csv");
        tempFile.deleteOnExit();
        CSVParser.parse(tempFile, null, CSVFormat.DEFAULT);
    }

    // Tests parse File with null format throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testParseFile_nullFormat_throwsIllegalArgumentException() throws IOException {
        final File tempFile = File.createTempFile("csv_test_null_format", ".csv");
        tempFile.deleteOnExit();
        CSVParser.parse(tempFile, StandardCharsets.UTF_8, null);
    }

    // Tests parse URL with valid URL
    @Test
    public void testParseURL_validURL_parsesSuccessfully() throws IOException {
        final File tempFile = File.createTempFile("csv_url_test", ".csv");
        tempFile.deleteOnExit();

        final OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(tempFile), StandardCharsets.UTF_8);
        writer.write("h1,h2\nr1,r2");
        writer.close();

        final URL url = tempFile.toURI().toURL();
        final CSVParser parser = CSVParser.parse(url, StandardCharsets.UTF_8, CSVFormat.DEFAULT.withHeader());
        final List<CSVRecord> records = parser.getRecords();

        assertEquals(1, records.size());
        assertEquals("r1", records.get(0).get("h1"));
        assertEquals("r2", records.get(0).get("h2"));
        parser.close();
    }

    // Tests parse URL with null charset throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testParseURL_nullCharset_throwsIllegalArgumentException() throws IOException {
        final File tempFile = File.createTempFile("csv_url_test_null_charset", ".csv");
        tempFile.deleteOnExit();
        CSVParser.parse(tempFile.toURI().toURL(), null, CSVFormat.DEFAULT);
    }

    // Tests parse URL with null format throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testParseURL_nullFormat_throwsIllegalArgumentException() throws IOException {
        final File tempFile = File.createTempFile("csv_url_test_null_format", ".csv");
        tempFile.deleteOnExit();
        CSVParser.parse(tempFile.toURI().toURL(), StandardCharsets.UTF_8, null);
    }

    // Tests parse String with null format throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testParseString_nullFormat_throwsIllegalArgumentException() throws IOException {
        CSVParser.parse("a,b,c", null);
    }

    // Tests nextRecord returns null at EOF and remains null on repeated calls
    @Test
    public void testNextRecord_atEOF_returnsNullConsistently() throws IOException {
        final CSVParser parser = CSVParser.parse("1,2", CSVFormat.DEFAULT);
        assertNotNull(parser.nextRecord());
        assertNull(parser.nextRecord());
        assertNull(parser.nextRecord());
        parser.close();
    }

    // Tests header initialization when header from first record is requested on empty input
    @Test
    public void testInitializeHeader_emptyInputWithDynamicHeader_headerMapIsNull() throws IOException {
        final CSVParser parser = CSVParser.parse("", CSVFormat.DEFAULT.withHeader());
        assertNull(parser.getHeaderMap());
        assertTrue(parser.getRecords().isEmpty());
        parser.close();
    }

    // Tests header defined via Enum
    @Test
    public void testInitializeHeader_withEnum_mapsHeadersCorrectly() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT.withHeader(TestHeaders.class);
        final CSVParser parser = CSVParser.parse("1,2,3", format);
        final Map<String, Integer> headerMap = parser.getHeaderMap();

        assertNotNull(headerMap);
        assertEquals(3, headerMap.size());
        assertEquals(Integer.valueOf(0), headerMap.get("A"));
        assertEquals(Integer.valueOf(1), headerMap.get("B"));
        assertEquals(Integer.valueOf(2), headerMap.get("C"));

        final CSVRecord record = parser.nextRecord();
        assertEquals("1", record.get(TestHeaders.A));
        assertEquals("2", record.get(TestHeaders.B));
        assertEquals("3", record.get(TestHeaders.C));
        parser.close();
    }

    // Tests duplicate empty header strings when ignoreEmptyHeaders is false throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testInitializeHeader_duplicateEmptyHeadersWithoutIgnoreEmpty_throwsIllegalArgumentException() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT.withHeader("col1", "", "").withIgnoreEmptyHeaders(false);
        CSVParser.parse("val1,val2,val3", format);
    }

    // Tests multi-line records correctly update line and record numbers
    @Test
    public void testParse_multilineRecords_tracksLineNumbers() throws IOException {
        final CSVParser parser = CSVParser.parse("\"line1\nline2\",b\n\"line3\nline4\nline5\",c", CSVFormat.DEFAULT);
        final CSVRecord record1 = parser.nextRecord();
        assertNotNull(record1);
        assertEquals("line1\nline2", record1.get(0));
        assertEquals(1, record1.getRecordNumber());

        final CSVRecord record2 = parser.nextRecord();
        assertNotNull(record2);
        assertEquals("line3\nline4\nline5", record2.get(0));
        assertEquals(2, record2.getRecordNumber());
        assertEquals(5, parser.getCurrentLineNumber());
        parser.close();
    }
}