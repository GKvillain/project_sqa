package org.apache.commons.csv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.junit.Test;

public class CSVParserTest {

    // Tests normal case: parse a simple CSV string with header
    @Test
    public void testParse_simpleString_returnsRecords() throws IOException {
        final String csv = "a,b,c\n1,2,3\n4,5,6";
        final CSVParser parser = CSVParser.parse(csv, CSVFormat.DEFAULT.withHeader("a", "b", "c"));
        final List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals("1", records.get(0).get("a"));
        assertEquals("5", records.get(1).get("b"));
        parser.close();
    }

    // Tests normal case: parse CSV with no header
    @Test
    public void testParse_noHeader_returnsRecords() throws IOException {
        final String csv = "1,2,3\n4,5,6";
        final CSVParser parser = CSVParser.parse(csv, CSVFormat.DEFAULT);
        final List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals("1", records.get(0).get(0));
        assertEquals("6", records.get(1).get(2));
        parser.close();
    }

    // Tests normal case: parse single record
    @Test
    public void testParse_singleRecord_returnsOneRecord() throws IOException {
        final String csv = "hello,world";
        final CSVParser parser = CSVParser.parse(csv, CSVFormat.DEFAULT);
        final List<CSVRecord> records = parser.getRecords();
        assertEquals(1, records.size());
        assertEquals("hello", records.get(0).get(0));
        assertEquals("world", records.get(0).get(1));
        parser.close();
    }

    // Tests empty string input
    @Test(expected = IOException.class)
    public void testParse_emptyString_throwsIOException() throws IOException {
        final CSVParser parser = CSVParser.parse("", CSVFormat.DEFAULT);
        parser.getRecords();
    }

    // Tests null string input
    @Test(expected = IllegalArgumentException.class)
    public void testParse_nullString_throwsIllegalArgumentException() throws IOException {
        CSVParser.parse((String) null, CSVFormat.DEFAULT);
    }

    // Tests null format input
    @Test(expected = IllegalArgumentException.class)
    public void testParse_nullFormat_throwsIllegalArgumentException() throws IOException {
        CSVParser.parse("a,b,c", null);
    }

    // Tests null reader input
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullReader_throwsIllegalArgumentException() throws IOException {
        new CSVParser(null, CSVFormat.DEFAULT);
    }

    // Tests null reader input with header
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullReaderWithFormat_throwsIllegalArgumentException() throws IOException {
        new CSVParser(null, CSVFormat.EXCEL);
    }

    // Tests boundary case: single character input
    @Test(expected = IOException.class)
    public void testParse_singleChar_throwsIOException() throws IOException {
        final CSVParser parser = CSVParser.parse("a", CSVFormat.DEFAULT);
        parser.getRecords();
    }

    // Tests boundary case: CSV with only newline
    @Test(expected = IOException.class)
    public void testParse_onlyNewline_throwsIOException() throws IOException {
        final CSVParser parser = CSVParser.parse("\n", CSVFormat.DEFAULT);
        parser.getRecords();
    }

    // Tests reading header map with explicit header
    @Test
    public void testGetHeaderMap_explicitHeader_returnsMap() throws IOException {
        final String csv = "1,2,3";
        final CSVParser parser = CSVParser.parse(csv, CSVFormat.DEFAULT.withHeader("x", "y", "z"));
        final Map<String, Integer> headerMap = parser.getHeaderMap();
        assertNotNull(headerMap);
        assertEquals(3, headerMap.size());
        assertEquals(Integer.valueOf(0), headerMap.get("x"));
        assertEquals(Integer.valueOf(1), headerMap.get("y"));
        assertEquals(Integer.valueOf(2), headerMap.get("z"));
        parser.close();
    }

    // Tests header map with header read from first record
    @Test
    public void testGetHeaderMap_headerFromFirstRecord_returnsMap() throws IOException {
        final String csv = "col1,col2\nval1,val2";
        final CSVParser parser = CSVParser.parse(csv, CSVFormat.DEFAULT.withHeader());
        final Map<String, Integer> headerMap = parser.getHeaderMap();
        assertNotNull(headerMap);
        assertEquals(2, headerMap.size());
        assertEquals(Integer.valueOf(0), headerMap.get("col1"));
        assertEquals(Integer.valueOf(1), headerMap.get("col2"));
        parser.close();
    }

    // Tests header map with no header
    @Test
    public void testGetHeaderMap_noHeader_returnsNull() throws IOException {
        final String csv = "a,b";
        final CSVParser parser = CSVParser.parse(csv, CSVFormat.DEFAULT);
        assertNull(parser.getHeaderMap());
        parser.close();
    }

    // Tests record number after parsing
    @Test
    public void testGetRecordNumber_afterParsing_returnsCorrectNumber() throws IOException {
        final String csv = "a,b\nc,d\ne,f";
        final CSVParser parser = CSVParser.parse(csv, CSVFormat.DEFAULT);
        parser.getRecords();
        assertEquals(2L, parser.getRecordNumber());
        parser.close();
    }

    // Tests iterator next() without hasNext() call
    @Test
    public void testIterator_nextWithoutHasNext_returnsCorrectRecord() throws IOException {
        final String csv = "a,b\n1,2";
        final CSVParser parser = CSVParser.parse(csv, CSVFormat.DEFAULT);
        final Iterator<CSVRecord> it = parser.iterator();
        final CSVRecord record = it.next();
        assertNotNull(record);
        assertEquals("1", record.get(0));
        assertEquals("2", record.get(1));
        parser.close();
    }

    // Tests iterator hasNext() returns false when no more records
    @Test
    public void testIterator_hasNextAfterLastRecord_returnsFalse() throws IOException {
        final String csv = "a,b";
        final CSVParser parser = CSVParser.parse(csv, CSVFormat.DEFAULT);
        final Iterator<CSVRecord> it = parser.iterator();
        assertTrue(it.hasNext());
        it.next();
        assertFalse(it.hasNext());
        parser.close();
    }

    // Tests iteration with multiple records
    @Test
    public void testIterator_multipleRecords_iteratesCorrectly() throws IOException {
        final String csv = "1\n2\n3";
        final CSVParser parser = CSVParser.parse(csv, CSVFormat.DEFAULT);
        final Iterator<CSVRecord> it = parser.iterator();
        int count = 0;
        while (it.hasNext()) {
            it.next();
            count++;
        }
        assertEquals(3, count);
        parser.close();
    }

    // Tests closed parser: iterator should return false for hasNext
    @Test
    public void testIterator_afterClose_hasNextReturnsFalse() throws IOException {
        final String csv = "a,b";
        final CSVParser parser = CSVParser.parse(csv, CSVFormat.DEFAULT);
        parser.close();
        final Iterator<CSVRecord> it = parser.iterator();
        assertFalse(it.hasNext());
    }

    // Tests closed parser: iterator next() throws NoSuchElementException
    @Test(expected = NoSuchElementException.class)
    public void testIterator_afterClose_nextThrowsNoSuchElementException() throws IOException {
        final String csv = "a,b";
        final CSVParser parser = CSVParser.parse(csv, CSVFormat.DEFAULT);
        parser.close();
        parser.iterator().next();
    }

    // Tests duplicate header throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testInitializeHeader_duplicateHeader_throwsIllegalArgumentException() throws IOException {
        final String csv = "a,a\n1,2";
        final CSVParser parser = CSVParser.parse(csv, CSVFormat.DEFAULT.withHeader());
        parser.getRecords();
    }

    // Tests null string replacement via format
    @Test
    public void testParse_withNullString_returnsNullValues() throws IOException {
        final String csv = "NULL,value";
        final CSVParser parser = CSVParser.parse(csv, CSVFormat.DEFAULT.withNullString("NULL"));
        final List<CSVRecord> records = parser.getRecords();
        assertNull(records.get(0).get(0));
        assertEquals("value", records.get(0).get(1));
        parser.close();
    }
}