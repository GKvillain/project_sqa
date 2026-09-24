package org.apache.commons.csv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.junit.Test;

public class CSVParserTest {

    // Tests simple CSV parsing from String
    @Test
    public void testParse_simpleString_parsesRecords() throws IOException {
        final CSVParser parser = CSVParser.parse("a,b,c\n1,2,3", CSVFormat.DEFAULT);
        final List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals("a", records.get(0).get(0));
        assertEquals("b", records.get(0).get(1));
        assertEquals("c", records.get(0).get(2));
        assertEquals("1", records.get(1).get(0));
        assertEquals("2", records.get(1).get(1));
        assertEquals("3", records.get(1).get(2));
        parser.close();
    }

    // Tests getHeaderMap when no header format is defined (should return null in 4b defect case / fix)
    @Test
    public void testGetHeaderMap_noHeader_returnsNullOrEmpty() throws IOException {
        final CSVParser parser = CSVParser.parse("a,b,c\n1,2,3", CSVFormat.DEFAULT);
        final Map<String, Integer> headerMap = parser.getHeaderMap();
        // Defects4J Csv-4 fix: getHeaderMap() returns null if headerMap is null (or throws NPE in bug)
        // In unpatched 4b, headerMap is null and new LinkedHashMap(headerMap) throws NullPointerException
        if (headerMap != null) {
            assertTrue(headerMap.isEmpty());
        }
        parser.close();
    }

    // Tests header mapping with manual predefined headers
    @Test
    public void testGetHeaderMap_predefinedHeader_returnsCorrectMap() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT.withHeader("Col1", "Col2", "Col3");
        final CSVParser parser = CSVParser.parse("a,b,c", format);
        final Map<String, Integer> headerMap = parser.getHeaderMap();
        assertNotNull(headerMap);
        assertEquals(3, headerMap.size());
        assertEquals(Integer.valueOf(0), headerMap.get("Col1"));
        assertEquals(Integer.valueOf(1), headerMap.get("Col2"));
        assertEquals(Integer.valueOf(2), headerMap.get("Col3"));
        parser.close();
    }

    // Tests header parsed from the first line of CSV data
    @Test
    public void testGetHeaderMap_firstLineHeader_parsesHeaderCorrectly() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT.withHeader(new String[0]);
        final CSVParser parser = CSVParser.parse("header1,header2\nval1,val2", format);
        final Map<String, Integer> headerMap = parser.getHeaderMap();
        assertNotNull(headerMap);
        assertEquals(2, headerMap.size());
        assertEquals(Integer.valueOf(0), headerMap.get("header1"));
        assertEquals(Integer.valueOf(1), headerMap.get("header2"));

        final List<CSVRecord> records = parser.getRecords();
        assertEquals(1, records.size());
        assertEquals("val1", records.get(0).get("header1"));
        assertEquals("val2", records.get(0).get("header2"));
        parser.close();
    }

    // Tests skip header record option
    @Test
    public void testParse_skipHeaderRecord_skipsFirstRecord() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT.withHeader("h1", "h2").withSkipHeaderRecord(true);
        final CSVParser parser = CSVParser.parse("header1,header2\nval1,val2", format);
        final List<CSVRecord> records = parser.getRecords();
        assertEquals(1, records.size());
        assertEquals("val1", records.get(0).get(0));
        assertEquals("val2", records.get(0).get(1));
        parser.close();
    }

    // Tests null string conversion behavior
    @Test
    public void testParse_withNullString_convertsMatchingTokensToNull() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        final CSVParser parser = CSVParser.parse("NULL,value,null", format);
        final CSVRecord record = parser.nextRecord();
        assertNotNull(record);
        assertNull(record.get(0));
        assertEquals("value", record.get(1));
        assertNull(record.get(2)); // case-insensitive null comparison in addRecordValue
        parser.close();
    }

    // Tests parsing with comment marker
    @Test
    public void testParse_withComment_ignoresCommentLineAndPreservesComment() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        final CSVParser parser = CSVParser.parse("# comment\na,b", format);
        final CSVRecord record = parser.nextRecord();
        assertNotNull(record);
        assertEquals("a", record.get(0));
        assertEquals("b", record.get(1));
        assertEquals(" comment", record.getComment());
        parser.close();
    }

    // Tests parser iteration and record counting
    @Test
    public void testIterator_multipleRecords_iteratesCorrectly() throws IOException {
        final CSVParser parser = CSVParser.parse("1\n2\n3", CSVFormat.DEFAULT);
        final Iterator<CSVRecord> it = parser.iterator();
        assertTrue(it.hasNext());
        assertEquals("1", it.next().get(0));
        assertTrue(it.hasNext());
        assertEquals("2", it.next().get(0));
        assertTrue(it.hasNext());
        assertEquals("3", it.next().get(0));
        assertFalse(it.hasNext());
        assertEquals(3L, parser.getRecordNumber());
        parser.close();
    }

    // Tests NoSuchElementException on empty iterator
    @Test(expected = NoSuchElementException.class)
    public void testIterator_noMoreElements_throwsException() throws IOException {
        final CSVParser parser = CSVParser.parse("", CSVFormat.DEFAULT);
        final Iterator<CSVRecord> it = parser.iterator();
        it.next();
    }

    // Tests iterator behavior when parser is closed
    @Test(expected = NoSuchElementException.class)
    public void testIterator_afterClose_throwsException() throws IOException {
        final CSVParser parser = CSVParser.parse("a,b", CSVFormat.DEFAULT);
        parser.close();
        assertTrue(parser.isClosed());
        parser.iterator().next();
    }

    // Tests iterator remove unsupported operation
    @Test(expected = UnsupportedOperationException.class)
    public void testIterator_remove_throwsUnsupportedOperationException() throws IOException {
        final CSVParser parser = CSVParser.parse("a,b", CSVFormat.DEFAULT);
        final Iterator<CSVRecord> it = parser.iterator();
        it.remove();
    }

    // Tests parsing from File
    @Test
    public void testParse_fileSource_parsesSuccessfully() throws IOException {
        final File tempFile = File.createTempFile("csvparser_test", ".csv");
        tempFile.deleteOnExit();
        final FileWriter writer = new FileWriter(tempFile);
        writer.write("x,y\n1,2");
        writer.close();

        final CSVParser parser = CSVParser.parse(tempFile, CSVFormat.DEFAULT);
        final List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals("x", records.get(0).get(0));
        assertEquals("y", records.get(0).get(1));
        parser.close();
    }

    // Tests line number tracking across multi-line content
    @Test
    public void testGetCurrentLineNumber_multilineRecord_tracksLines() throws IOException {
        final CSVParser parser = CSVParser.parse("\"line1\nline2\",b\nc,d", CSVFormat.DEFAULT);
        assertEquals(0L, parser.getCurrentLineNumber());
        final CSVRecord rec1 = parser.nextRecord();
        assertNotNull(rec1);
        assertEquals("line1\nline2", rec1.get(0));
        assertEquals(2L, parser.getCurrentLineNumber());
        assertEquals(1L, parser.getRecordNumber());

        final CSVRecord rec2 = parser.nextRecord();
        assertNotNull(rec2);
        assertEquals("c", rec2.get(0));
        assertEquals(3L, parser.getCurrentLineNumber());
        assertEquals(2L, parser.getRecordNumber());
        parser.close();
    }

    // Tests parse static factory with null File throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testParse_nullFile_throwsIllegalArgumentException() throws IOException {
        CSVParser.parse((File) null, CSVFormat.DEFAULT);
    }

    // Tests parse static factory with null String throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testParse_nullString_throwsIllegalArgumentException() throws IOException {
        CSVParser.parse((String) null, CSVFormat.DEFAULT);
    }

    // Tests constructor with null Reader throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullReader_throwsIllegalArgumentException() throws IOException {
        new CSVParser(null, CSVFormat.DEFAULT);
    }

    // Tests constructor with null CSVFormat throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullFormat_throwsIllegalArgumentException() throws IOException {
        new CSVParser(new StringReader(""), null);
    }

    // Tests parse from URL
    @Test
    public void testParse_urlSource_parsesSuccessfully() throws IOException {
        final File tempFile = File.createTempFile("csvparser_url_test", ".csv");
        tempFile.deleteOnExit();
        final FileWriter writer = new FileWriter(tempFile);
        writer.write("u1,u2\n3,4");
        writer.close();

        final URL url = tempFile.toURI().toURL();
        final CSVParser parser = CSVParser.parse(url, Charset.forName("UTF-8"), CSVFormat.DEFAULT);
        final List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals("u1", records.get(0).get(0));
        assertEquals("u2", records.get(0).get(1));
        assertEquals("3", records.get(1).get(0));
        assertEquals("4", records.get(1).get(1));
        parser.close();
    }

    // Tests parse from URL with null URL throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testParse_nullUrl_throwsIllegalArgumentException() throws IOException {
        CSVParser.parse((URL) null, Charset.forName("UTF-8"), CSVFormat.DEFAULT);
    }

    // Tests parse from URL with null Charset throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testParse_nullCharset_throwsIllegalArgumentException() throws IOException {
        final File tempFile = File.createTempFile("csvparser_url_test", ".csv");
        tempFile.deleteOnExit();
        final URL url = tempFile.toURI().toURL();
        CSVParser.parse(url, (Charset) null, CSVFormat.DEFAULT);
    }

    // Tests parse from URL with null CSVFormat throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testParse_urlNullFormat_throwsIllegalArgumentException() throws IOException {
        final File tempFile = File.createTempFile("csvparser_url_test", ".csv");
        tempFile.deleteOnExit();
        final URL url = tempFile.toURI().toURL();
        CSVParser.parse(url, Charset.forName("UTF-8"), (CSVFormat) null);
    }

    // Tests parse static factory with null CSVFormat for String throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testParse_stringNullFormat_throwsIllegalArgumentException() throws IOException {
        CSVParser.parse("a,b,c", (CSVFormat) null);
    }

    // Tests parse static factory with null CSVFormat for File throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testParse_fileNullFormat_throwsIllegalArgumentException() throws IOException {
        final File tempFile = File.createTempFile("csvparser_test", ".csv");
        tempFile.deleteOnExit();
        CSVParser.parse(tempFile, (CSVFormat) null);
    }

    // Tests nextRecord returns null when closed
    @Test
    public void testNextRecord_afterClose_returnsNull() throws IOException {
        final CSVParser parser = CSVParser.parse("a,b\nc,d", CSVFormat.DEFAULT);
        assertFalse(parser.isClosed());
        parser.close();
        assertTrue(parser.isClosed());
        assertNull(parser.nextRecord());
    }

    // Tests first line header on empty string produces empty headerMap
    @Test
    public void testGetHeaderMap_emptyInputWithFirstLineHeader_producesEmptyMap() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT.withHeader(new String[0]);
        final CSVParser parser = CSVParser.parse("", format);
        final Map<String, Integer> headerMap = parser.getHeaderMap();
        assertNotNull(headerMap);
        assertTrue(headerMap.isEmpty());
        parser.close();
    }
}