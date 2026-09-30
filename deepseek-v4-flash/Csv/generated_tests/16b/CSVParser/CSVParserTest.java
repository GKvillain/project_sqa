package org.apache.commons.csv;

import static org.junit.Assert.assertArrayEquals;
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

    // Test simple record parsing
    @Test
    public void testParse_simpleRecord_returnsCorrectValues() throws IOException {
        String csv = "a,b,c";
        CSVFormat format = CSVFormat.DEFAULT;
        try (CSVParser parser = CSVParser.parse(csv, format)) {
            CSVRecord record = parser.nextRecord();
            assertNotNull(record);
            assertArrayEquals(new String[] {"a", "b", "c"}, record.values());
            assertNull(parser.nextRecord());
        }
    }

    // Test multiple records
    @Test
    public void testParse_multipleRecords_returnsCorrectRecords() throws IOException {
        String csv = "1,2\n3,4";
        CSVFormat format = CSVFormat.DEFAULT;
        try (CSVParser parser = CSVParser.parse(csv, format)) {
            List<CSVRecord> records = parser.getRecords();
            assertEquals(2, records.size());
            assertArrayEquals(new String[] {"1", "2"}, records.get(0).values());
            assertArrayEquals(new String[] {"3", "4"}, records.get(1).values());
        }
    }

    // Test empty input
    @Test
    public void testParse_emptyInput_returnsNoRecords() throws IOException {
        String csv = "";
        CSVFormat format = CSVFormat.DEFAULT;
        try (CSVParser parser = CSVParser.parse(csv, format)) {
            List<CSVRecord> records = parser.getRecords();
            assertTrue(records.isEmpty());
            assertNull(parser.nextRecord());
        }
    }

    // Test trailing delimiter with trailingDelimiter=true
    @Test
    public void testParse_trailingDelimiterTrue_skipsEmptyLastField() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withTrailingDelimiter(true);
        String csv = "a,b,";
        try (CSVParser parser = CSVParser.parse(csv, format)) {
            CSVRecord record = parser.nextRecord();
            assertNotNull(record);
            assertEquals(2, record.size());
            assertEquals("a", record.get(0));
            assertEquals("b", record.get(1));
            assertNull(parser.nextRecord());
        }
    }

    // Test trailing delimiter with trailingDelimiter=false (default)
    @Test
    public void testParse_trailingDelimiterFalse_includesEmptyLastField() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT; // trailingDelimiter false
        String csv = "a,b,";
        try (CSVParser parser = CSVParser.parse(csv, format)) {
            CSVRecord record = parser.nextRecord();
            assertNotNull(record);
            assertEquals(3, record.size());
            assertEquals("a", record.get(0));
            assertEquals("b", record.get(1));
            assertEquals("", record.get(2));
            assertNull(parser.nextRecord());
        }
    }

    // Test null string replacement
    @Test
    public void testParse_nullString_replacesWithNull() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        String csv = "a,NULL,c";
        try (CSVParser parser = CSVParser.parse(csv, format)) {
            CSVRecord record = parser.nextRecord();
            assertEquals("a", record.get(0));
            assertNull(record.get(1));
            assertEquals("c", record.get(2));
        }
    }

    // Test trim enabled
    @Test
    public void testParse_trimEnabled_trimsValues() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withTrim();
        String csv = " a , b , c ";
        try (CSVParser parser = CSVParser.parse(csv, format)) {
            CSVRecord record = parser.nextRecord();
            assertEquals("a", record.get(0));
            assertEquals("b", record.get(1));
            assertEquals("c", record.get(2));
        }
    }

    // Test header mapping from first line
    @Test
    public void testInitializeHeader_fromFirstLine_createsHeaderMap() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withHeader(); // read from first record
        String csv = "col1,col2\nval1,val2";
        try (CSVParser parser = CSVParser.parse(csv, format)) {
            Map<String, Integer> headerMap = parser.getHeaderMap();
            assertNotNull(headerMap);
            assertEquals(2, headerMap.size());
            assertEquals(Integer.valueOf(0), headerMap.get("col1"));
            assertEquals(Integer.valueOf(1), headerMap.get("col2"));
            CSVRecord record = parser.nextRecord();
            assertEquals("val1", record.get("col1"));
            assertEquals("val2", record.get("col2"));
        }
    }

    // Test header mapping with provided header names and skipHeaderRecord
    @Test
    public void testInitializeHeader_withFormatHeaderAndSkip_skipsFirstRecord() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("A", "B").withSkipHeaderRecord(true);
        String csv = "ignored,ignored\ndata1,data2";
        try (CSVParser parser = CSVParser.parse(csv, format)) {
            Map<String, Integer> headerMap = parser.getHeaderMap();
            assertEquals(2, headerMap.size());
            assertEquals(Integer.valueOf(0), headerMap.get("A"));
            CSVRecord record = parser.nextRecord();
            assertEquals("data1", record.get("A"));
            assertEquals("data2", record.get("B"));
        }
    }

    // Test duplicate header throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testInitializeHeader_duplicateHeader_throwsException() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("dup", "dup");
        String csv = "a,b";
        try (CSVParser parser = CSVParser.parse(csv, format)) {
            // Should throw during construction
        }
    }

    // Test empty header with allowMissingColumnNames = true
    @Test
    public void testInitializeHeader_emptyHeaderWithAllowMissing_works() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("A", "").withAllowMissingColumnNames(true);
        String csv = "val1,val2";
        try (CSVParser parser = CSVParser.parse(csv, format)) {
            Map<String, Integer> headerMap = parser.getHeaderMap();
            assertEquals(2, headerMap.size());
            assertTrue(headerMap.containsKey("A"));
            assertTrue(headerMap.containsKey(""));
        }
    }

    // Test case insensitive header map
    @Test
    public void testInitializeHeader_caseInsensitive_createsCaseInsensitiveMap() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("COL1", "COL2").withIgnoreHeaderCase(true);
        String csv = "val1,val2";
        try (CSVParser parser = CSVParser.parse(csv, format)) {
            Map<String, Integer> headerMap = parser.getHeaderMap();
            assertEquals(Integer.valueOf(0), headerMap.get("col1"));
            assertEquals(Integer.valueOf(1), headerMap.get("COL2"));
        }
    }

    // Test iterator behavior
    @Test
    public void testIterator_hasNextAndNext_worksCorrectly() throws IOException {
        String csv = "a,b\nc,d";
        CSVFormat format = CSVFormat.DEFAULT;
        try (CSVParser parser = CSVParser.parse(csv, format)) {
            Iterator<CSVRecord> iter = parser.iterator();
            assertTrue(iter.hasNext());
            CSVRecord first = iter.next();
            assertArrayEquals(new String[] {"a", "b"}, first.values());
            assertTrue(iter.hasNext());
            CSVRecord second = iter.next();
            assertArrayEquals(new String[] {"c", "d"}, second.values());
            assertFalse(iter.hasNext());
        }
    }

    // Test iterator on closed parser throws NoSuchElementException
    @Test(expected = NoSuchElementException.class)
    public void testIterator_closedParser_throwsNoSuchElement() throws IOException {
        String csv = "a,b";
        CSVFormat format = CSVFormat.DEFAULT;
        CSVParser parser = CSVParser.parse(csv, format);
        parser.close();
        parser.iterator().next();
    }

    // Test constructor null reader throws
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullReader_throwsIllegalArgumentException() throws IOException {
        new CSVParser(null, CSVFormat.DEFAULT);
    }

    // Test constructor null format throws
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullFormat_throwsIllegalArgumentException() throws IOException {
        new CSVParser(new StringReader(""), null);
    }

    // Test getCurrentLineNumber
    @Test
    public void testGetCurrentLineNumber_afterParsing_returnsCorrectLine() throws IOException {
        String csv = "a\nb\nc";
        CSVFormat format = CSVFormat.DEFAULT;
        try (CSVParser parser = CSVParser.parse(csv, format)) {
            parser.nextRecord();
            assertEquals(1, parser.getCurrentLineNumber());
            parser.nextRecord();
            assertEquals(2, parser.getCurrentLineNumber());
            parser.nextRecord();
            assertEquals(3, parser.getCurrentLineNumber());
        }
    }

    // Test getRecordNumber
    @Test
    public void testGetRecordNumber_afterParsing_returnsCorrectRecordNumber() throws IOException {
        String csv = "a\nb\nc";
        CSVFormat format = CSVFormat.DEFAULT;
        try (CSVParser parser = CSVParser.parse(csv, format)) {
            assertEquals(0, parser.getRecordNumber());
            parser.nextRecord();
            assertEquals(1, parser.getRecordNumber());
            parser.nextRecord();
            assertEquals(2, parser.getRecordNumber());
        }
    }

    // Test close and isClosed
    @Test
    public void testClose_closesParser_isClosedReturnsTrue() throws IOException {
        CSVParser parser = CSVParser.parse("", CSVFormat.DEFAULT);
        assertFalse(parser.isClosed());
        parser.close();
        assertTrue(parser.isClosed());
    }

    // Test comment lines are ignored
    @Test
    public void testParse_commentLines_ignored() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        String csv = "#comment\na,b\n#another\nc,d";
        try (CSVParser parser = CSVParser.parse(csv, format)) {
            List<CSVRecord> records = parser.getRecords();
            assertEquals(2, records.size());
            assertEquals("a", records.get(0).get(0));
            assertEquals("c", records.get(1).get(0));
        }
    }

    // Test invalid token (unclosed quote) throws IOException
    @Test(expected = IOException.class)
    public void testParse_invalidToken_throwsIOException() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT;
        String csv = "\"a"; // unclosed quote
        CSVParser parser = CSVParser.parse(csv, format);
        parser.nextRecord();
        parser.close();
    }

    // Test first end-of-line
    @Test
    public void testGetFirstEndOfLine_returnsCorrectEol() throws IOException {
        String csv = "a\nb\r\nc";
        CSVFormat format = CSVFormat.DEFAULT;
        try (CSVParser parser = CSVParser.parse(csv, format)) {
            parser.nextRecord(); // consume records to trigger detection
            assertEquals("\n", parser.getFirstEndOfLine());
        }
    }
}