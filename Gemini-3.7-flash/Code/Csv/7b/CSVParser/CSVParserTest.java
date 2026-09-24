package org.apache.commons.csv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.junit.Test;

public class CSVParserTest {

    // Tests parsing valid CSV string into records
    @Test
    public void testParseString_validCsv_returnsCorrectRecords() throws IOException {
        final String csv = "A,B,C\n1,2,3\n4,5,6";
        final CSVParser parser = CSVParser.parse(csv, CSVFormat.DEFAULT);
        final List<CSVRecord> records = parser.getRecords();

        assertEquals(3, records.size());
        assertEquals("A", records.get(0).get(0));
        assertEquals("B", records.get(0).get(1));
        assertEquals("C", records.get(0).get(2));
        assertEquals("1", records.get(1).get(0));
        assertEquals("6", records.get(2).get(2));
    }

    // Tests parsing empty string returns empty record list
    @Test
    public void testParseString_emptyInput_returnsNoRecords() throws IOException {
        final CSVParser parser = CSVParser.parse("", CSVFormat.DEFAULT);
        final List<CSVRecord> records = parser.getRecords();

        assertTrue(records.isEmpty());
        assertEquals(0, parser.getRecordNumber());
    }

    // Tests null string input throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testParseString_nullString_throwsException() throws IOException {
        CSVParser.parse((String) null, CSVFormat.DEFAULT);
    }

    // Tests null format input throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testParseString_nullFormat_throwsException() throws IOException {
        CSVParser.parse("a,b,c", null);
    }

    // Tests null file input throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testParseFile_nullFile_throwsException() throws IOException {
        CSVParser.parse((File) null, CSVFormat.DEFAULT);
    }

    // Tests null URL input throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testParseURL_nullUrl_throwsException() throws IOException {
        CSVParser.parse((URL) null, Charset.defaultCharset(), CSVFormat.DEFAULT);
    }

    // Tests null reader input in constructor throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullReader_throwsException() throws IOException {
        new CSVParser(null, CSVFormat.DEFAULT);
    }

    // Tests header auto-detection from first record
    @Test
    public void testInitializeHeader_autoDetection_populatesHeaderMap() throws IOException {
        final String csv = "header1,header2,header3\nval1,val2,val3";
        final CSVFormat format = CSVFormat.DEFAULT.withHeader();
        final CSVParser parser = CSVParser.parse(csv, format);

        final Map<String, Integer> headerMap = parser.getHeaderMap();
        assertNotNull(headerMap);
        assertEquals(3, headerMap.size());
        assertEquals(Integer.valueOf(0), headerMap.get("header1"));
        assertEquals(Integer.valueOf(1), headerMap.get("header2"));
        assertEquals(Integer.valueOf(2), headerMap.get("header3"));

        final List<CSVRecord> records = parser.getRecords();
        assertEquals(1, records.size());
        assertEquals("val1", records.get(0).get("header1"));
    }

    // Tests explicit header without skip header record
    @Test
    public void testInitializeHeader_explicitHeader_populatesHeaderMap() throws IOException {
        final String csv = "val1,val2\nval3,val4";
        final CSVFormat format = CSVFormat.DEFAULT.withHeader("col1", "col2");
        final CSVParser parser = CSVParser.parse(csv, format);

        final Map<String, Integer> headerMap = parser.getHeaderMap();
        assertNotNull(headerMap);
        assertEquals(2, headerMap.size());
        assertEquals(Integer.valueOf(0), headerMap.get("col1"));
        assertEquals(Integer.valueOf(1), headerMap.get("col2"));

        final List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals("val1", records.get(0).get("col1"));
    }

    // Tests explicit header with skipHeaderRecord option
    @Test
    public void testInitializeHeader_skipHeaderRecord_skipsFirstLine() throws IOException {
        final String csv = "colA,colB\n1,2\n3,4";
        final CSVFormat format = CSVFormat.DEFAULT.withHeader("colA", "colB").withSkipHeaderRecord(true);
        final CSVParser parser = CSVParser.parse(csv, format);

        final List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals("1", records.get(0).get("colA"));
        assertEquals("2", records.get(0).get("colB"));
        assertEquals("3", records.get(1).get("colA"));
    }

    // Tests getHeaderMap returns null when no header is configured
    @Test
    public void testGetHeaderMap_noHeader_returnsNull() throws IOException {
        final CSVParser parser = CSVParser.parse("a,b,c", CSVFormat.DEFAULT);
        assertNull(parser.getHeaderMap());
    }

    // Tests getHeaderMap returns a copy and cannot mutate parser state
    @Test
    public void testGetHeaderMap_mutation_doesNotAffectParser() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT.withHeader("A", "B");
        final CSVParser parser = CSVParser.parse("1,2", format);

        final Map<String, Integer> mapCopy = parser.getHeaderMap();
        mapCopy.put("C", 2);

        assertEquals(2, parser.getHeaderMap().size());
        assertFalse(parser.getHeaderMap().containsKey("C"));
    }

    // Tests nullString mapping to null value
    @Test
    public void testParse_withNullString_convertsNullValue() throws IOException {
        final String csv = "a,NULL,b";
        final CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        final CSVParser parser = CSVParser.parse(csv, format);

        final CSVRecord record = parser.nextRecord();
        assertNotNull(record);
        assertEquals("a", record.get(0));
        assertNull(record.get(1));
        assertEquals("b", record.get(2));
    }

    // Tests comments parsing and attachment to record
    @Test
    public void testParse_withComments_preservesCommentsInRecord() throws IOException {
        final String csv = "# comment line 1\n# comment line 2\na,b,c";
        final CSVFormat format = CSVFormat.DEFAULT.withCommentStart('#');
        final CSVParser parser = CSVParser.parse(csv, format);

        final CSVRecord record = parser.nextRecord();
        assertNotNull(record);
        assertEquals("a", record.get(0));
        assertEquals("comment line 1\ncomment line 2", record.getComment());
    }

    // Tests line number tracking across multiple records
    @Test
    public void testGetCurrentLineNumber_multiLineRecords_tracksCorrectLine() throws IOException {
        final String csv = "a,b\nc,d\ne,f";
        final CSVParser parser = CSVParser.parse(csv, CSVFormat.DEFAULT);

        assertEquals(0, parser.getCurrentLineNumber());
        parser.nextRecord();
        assertEquals(1, parser.getCurrentLineNumber());
        assertEquals(1, parser.getRecordNumber());

        parser.nextRecord();
        assertEquals(2, parser.getCurrentLineNumber());
        assertEquals(2, parser.getRecordNumber());
    }

    // Tests iterator methods hasNext and next
    @Test
    public void testIterator_standardIteration_returnsAllRecords() throws IOException {
        final String csv = "1,2\n3,4";
        final CSVParser parser = CSVParser.parse(csv, CSVFormat.DEFAULT);
        final Iterator<CSVRecord> iterator = parser.iterator();

        assertTrue(iterator.hasNext());
        final CSVRecord rec1 = iterator.next();
        assertEquals("1", rec1.get(0));

        assertTrue(iterator.hasNext());
        final CSVRecord rec2 = iterator.next();
        assertEquals("3", rec2.get(0));

        assertFalse(iterator.hasNext());
    }

    // Tests iterator next without hasNext throws NoSuchElementException at end of stream
    @Test(expected = NoSuchElementException.class)
    public void testIterator_exhaustedStream_throwsNoSuchElementException() throws IOException {
        final CSVParser parser = CSVParser.parse("a,b", CSVFormat.DEFAULT);
        final Iterator<CSVRecord> iterator = parser.iterator();

        iterator.next();
        iterator.next();
    }

    // Tests iterator remove throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testIterator_remove_throwsUnsupportedOperationException() throws IOException {
        final CSVParser parser = CSVParser.parse("a,b", CSVFormat.DEFAULT);
        final Iterator<CSVRecord> iterator = parser.iterator();
        iterator.remove();
    }

    // Tests close method and isClosed status
    @Test
    public void testClose_closedParser_isClosedTrue() throws IOException {
        final CSVParser parser = CSVParser.parse("a,b\nc,d", CSVFormat.DEFAULT);
        assertFalse(parser.isClosed());

        parser.close();
        assertTrue(parser.isClosed());
        assertNull(parser.nextRecord());
    }

    // Tests iterator hasNext and next on closed parser
    @Test(expected = NoSuchElementException.class)
    public void testIterator_afterClose_throwsNoSuchElementException() throws IOException {
        final CSVParser parser = CSVParser.parse("a,b\nc,d", CSVFormat.DEFAULT);
        final Iterator<CSVRecord> iterator = parser.iterator();
        parser.close();

        assertFalse(iterator.hasNext());
        iterator.next();
    }

    // Tests getRecords with custom collection
    @Test
    public void testGetRecords_customCollection_appendsRecords() throws IOException {
        final CSVParser parser = CSVParser.parse("a,b\nc,d", CSVFormat.DEFAULT);
        final List<CSVRecord> customList = new ArrayList<CSVRecord>();

        final List<CSVRecord> result = parser.getRecords(customList);
        assertEquals(2, result.size());
        assertEquals(customList, result);
    }
}