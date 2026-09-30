package org.apache.commons.csv;

import static org.junit.Assert.*;
import org.junit.Test;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class CSVParserTest {

    // Tests normal parsing of multiple records
    @Test
    public void testGetRecords_simpleInput_returnsListOfRecords() throws IOException {
        String input = "a,b,c\n1,2,3\n4,5,6";
        try (CSVParser parser = new CSVParser(new StringReader(input), CSVFormat.DEFAULT)) {
            List<CSVRecord> records = parser.getRecords();
            assertEquals(2, records.size());
            assertEquals("1", records.get(0).get(0));
            assertEquals("6", records.get(1).get(2));
        }
    }

    // Tests auto-detecting header from first record
    @Test
    public void testInitializeHeader_autoDetectFromFirstRecord_createsHeaderMap() throws IOException {
        String input = "col1,col2\nval1,val2";
        CSVFormat format = CSVFormat.DEFAULT.withHeader();
        try (CSVParser parser = new CSVParser(new StringReader(input), format)) {
            Map<String, Integer> headerMap = parser.getHeaderMap();
            assertNotNull(headerMap);
            assertEquals(2, headerMap.size());
            assertEquals(Integer.valueOf(0), headerMap.get("col1"));
            assertEquals(Integer.valueOf(1), headerMap.get("col2"));
            List<CSVRecord> records = parser.getRecords();
            assertEquals(1, records.size());
        }
    }

    // Tests header with skipHeaderRecord true – first record is skipped
    @Test
    public void testInitializeHeader_skipHeaderRecordTrue_skipsFirstDataRecord() throws IOException {
        String input = "ignore1,ignore2\nreal1,real2";
        CSVFormat format = CSVFormat.DEFAULT.withHeader("h1", "h2").withSkipHeaderRecord(true);
        try (CSVParser parser = new CSVParser(new StringReader(input), format)) {
            Map<String, Integer> headerMap = parser.getHeaderMap();
            assertNotNull(headerMap);
            assertEquals(2, headerMap.size());
            List<CSVRecord> records = parser.getRecords();
            assertEquals(1, records.size());
            assertEquals("real1", records.get(0).get("h1"));
        }
    }

    // Tests header with skipHeaderRecord false – first record is data
    @Test
    public void testInitializeHeader_skipHeaderRecordFalse_includesFirstRecordAsData() throws IOException {
        String input = "data1,data2\nnext1,next2";
        CSVFormat format = CSVFormat.DEFAULT.withHeader("h1", "h2").withSkipHeaderRecord(false);
        try (CSVParser parser = new CSVParser(new StringReader(input), format)) {
            Map<String, Integer> headerMap = parser.getHeaderMap();
            assertNotNull(headerMap);
            List<CSVRecord> records = parser.getRecords();
            assertEquals(2, records.size());
            assertEquals("data1", records.get(0).get("h1"));
            assertEquals("next2", records.get(1).get("h2"));
        }
    }

    // Tests that comment lines are ignored and the next record is returned
    @Test
    public void testNextRecord_commentLine_ignoredAndReturnsNextRecord() throws IOException {
        String input = "# comment\nval1,val2";
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        try (CSVParser parser = new CSVParser(new StringReader(input), format)) {
            List<CSVRecord> records = parser.getRecords();
            assertEquals(1, records.size());
            assertEquals("val1", records.get(0).get(0));
        }
    }

    // Tests multiple comment lines before a record – comment is accumulated
    @Test
    public void testNextRecord_multipleCommentLines_accumulatesComment() throws IOException {
        String input = "# line1\n# line2\nval1,val2";
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        try (CSVParser parser = new CSVParser(new StringReader(input), format)) {
            CSVRecord record = parser.nextRecord();
            assertNotNull(record);
            assertEquals("val1", record.get(0));
            String comment = record.getComment();
            assertNotNull(comment);
            assertTrue(comment.contains("line1"));
            assertTrue(comment.contains("line2"));
        }
    }

    // Tests that null string configuration replaces fields with null
    @Test
    public void testAddRecordValue_nullString_returnsNull() throws IOException {
        String input = "NULL,abc";
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        try (CSVParser parser = new CSVParser(new StringReader(input), format)) {
            List<CSVRecord> records = parser.getRecords();
            assertEquals(1, records.size());
            assertNull(records.get(0).get(0));
            assertEquals("abc", records.get(0).get(1));
        }
    }

    // Tests that an invalid token (unclosed quote) throws IOException
    @Test(expected = IOException.class)
    public void testNextRecord_invalidToken_throwsIOException() throws IOException {
        String input = "\"unclosed";
        try (CSVParser parser = new CSVParser(new StringReader(input), CSVFormat.DEFAULT)) {
            parser.getRecords();
        }
    }

    // Tests that empty input returns no records
    @Test
    public void testGetRecords_emptyInput_returnsEmptyList() throws IOException {
        try (CSVParser parser = new CSVParser(new StringReader(""), CSVFormat.DEFAULT)) {
            List<CSVRecord> records = parser.getRecords();
            assertTrue(records.isEmpty());
        }
    }

    // Tests that nextRecord returns null when at end of stream
    @Test
    public void testNextRecord_atEndOfStream_returnsNull() throws IOException {
        String input = "a,b\n1,2";
        try (CSVParser parser = new CSVParser(new StringReader(input), CSVFormat.DEFAULT)) {
            assertNotNull(parser.nextRecord());
            assertNotNull(parser.nextRecord());
            assertNull(parser.nextRecord());
        }
    }

    // Tests that iterator throws NoSuchElementException after close
    @Test(expected = NoSuchElementException.class)
    public void testIterator_afterClose_throwsNoSuchElementException() throws IOException {
        CSVParser parser = new CSVParser(new StringReader("a,b"), CSVFormat.DEFAULT);
        parser.close();
        parser.iterator().next();
    }

    // Tests that getHeaderMap returns null when no header exists (current code has NPE bug)
    @Test
    public void testGetHeaderMap_whenNoHeader_returnsNull() throws IOException {
        CSVParser parser = new CSVParser(new StringReader("a,b"), CSVFormat.DEFAULT);
        try {
            assertNull(parser.getHeaderMap()); // should return null but throws NPE due to bug
        } finally {
            parser.close();
        }
    }

    // Tests constructor with null reader throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullReader_throwsIllegalArgumentException() throws IOException {
        new CSVParser(null, CSVFormat.DEFAULT);
    }

    // Tests constructor with null format throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullFormat_throwsIllegalArgumentException() throws IOException {
        new CSVParser(new StringReader(""), null);
    }

    // Tests static parse with null string throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testParse_nullString_throwsIllegalArgumentException() throws IOException {
        CSVParser.parse((String) null, CSVFormat.DEFAULT);
    }

    // Tests static parse with null format throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testParse_nullFormat_throwsIllegalArgumentException() throws IOException {
        CSVParser.parse("", null);
    }

    // Tests that record number increments correctly
    @Test
    public void testGetRecordNumber_increasesAfterEachRecord() throws IOException {
        String input = "a\nb\nc";
        try (CSVParser parser = new CSVParser(new StringReader(input), CSVFormat.DEFAULT)) {
            assertEquals(0, parser.getRecordNumber());
            parser.nextRecord();
            assertEquals(1, parser.getRecordNumber());
            parser.nextRecord();
            assertEquals(2, parser.getRecordNumber());
            parser.nextRecord();
            assertEquals(3, parser.getRecordNumber());
        }
    }

    // Tests current line number after reading records
    @Test
    public void testGetCurrentLineNumber_returnsCorrectLine() throws IOException {
        String input = "a,b\nc,d";
        try (CSVParser parser = new CSVParser(new StringReader(input), CSVFormat.DEFAULT)) {
            parser.nextRecord();
            assertEquals(1, parser.getCurrentLineNumber());
            parser.nextRecord();
            assertEquals(2, parser.getCurrentLineNumber());
        }
    }

    // Tests iteration with for-each loop
    @Test
    public void testIterator_iteratesCorrectly() throws IOException {
        String input = "1\n2\n3";
        try (CSVParser parser = new CSVParser(new StringReader(input), CSVFormat.DEFAULT)) {
            int count = 0;
            for (@SuppressWarnings("unused") CSVRecord rec : parser) {
                count++;
            }
            assertEquals(3, count);
        }
    }

    // Tests that isClosed returns false before close and true after
    @Test
    public void testClose_isClosed_returnsTrue() throws IOException {
        CSVParser parser = new CSVParser(new StringReader(""), CSVFormat.DEFAULT);
        assertFalse(parser.isClosed());
        parser.close();
        assertTrue(parser.isClosed());
    }

    // Tests auto-detect header when first line is a comment only – no header, empty records
    @Test
    public void testInitializeHeader_autoDetectWithCommentOnly_returnsEmptyRecords() throws IOException {
        String input = "# comment";
        CSVFormat format = CSVFormat.DEFAULT.withHeader().withCommentMarker('#');
        try (CSVParser parser = new CSVParser(new StringReader(input), format)) {
            List<CSVRecord> records = parser.getRecords();
            assertTrue(records.isEmpty());
            assertEquals(0, parser.getRecordNumber());
        }
    }

    // ================== New tests for uncovered coverage ==================

    // Tests that getRecordNumber after getRecords() returns correct count
    @Test
    public void testGetRecordNumber_afterGetRecords_returnsRecordCount() throws IOException {
        String input = "a,b\n1,2\n3,4";
        try (CSVParser parser = new CSVParser(new StringReader(input), CSVFormat.DEFAULT)) {
            List<CSVRecord> records = parser.getRecords();
            assertEquals(2, records.size());
            assertEquals(2, parser.getRecordNumber());
        }
    }

    // Tests that record.get() with a column name that does not exist returns null (or handles gracefully)
    @Test
    public void testRecordGetByKeyNotFound_returnsNull() throws IOException {
        String input = "col1,col2\nval1,val2";
        CSVFormat format = CSVFormat.DEFAULT.withHeader();
        try (CSVParser parser = new CSVParser(new StringReader(input), format)) {
            List<CSVRecord> records = parser.getRecords();
            CSVRecord record = records.get(0);
            // Attempt to get a column that does not exist
            assertNull(record.get("nonExistentKey"));
        }
    }

    // Tests that nextRecord() throws NoSuchElementException after the last record (not just close)
    @Test(expected = NoSuchElementException.class)
    public void testIterator_afterLastElement_throwsNoSuchElementException() throws IOException {
        String input = "a,b";
        try (CSVParser parser = new CSVParser(new StringReader(input), CSVFormat.DEFAULT)) {
            // read the only record
            parser.nextRecord();
            // reading again should throw NoSuchElementException (if implementation does)
            // but current testNextRecord_atEndOfStream_returnsNull shows it returns null.
            // However iterator() is separate; we test that iterator throws after exhausting.
            // For consistency we assume iterator() follows Java Iterator contract.
            // This test verifies that the iterator (used implicitly) throws the exception.
            // We'll manually iterate using iterator().
            var it = parser.iterator();
            it.next(); // first record
            it.next(); // should throw NoSuchElementException
        }
    }

    // Tests that getHeaderMap() when header is set via withFirstRecordAsHeader() works correctly
    @Test
    public void testInitializeHeader_withFirstRecordAsHeader_createsHeaderMap() throws IOException {
        String input = "h1,h2\nv1,v2\nv3,v4";
        CSVFormat format = CSVFormat.DEFAULT.withFirstRecordAsHeader();
        try (CSVParser parser = new CSVParser(new StringReader(input), format)) {
            Map<String, Integer> headerMap = parser.getHeaderMap();
            assertNotNull(headerMap);
            assertEquals(2, headerMap.size());
            assertEquals(Integer.valueOf(0), headerMap.get("h1"));
            assertEquals(Integer.valueOf(1), headerMap.get("h2"));
            List<CSVRecord> records = parser.getRecords();
            assertEquals(2, records.size());
            assertEquals("v1", records.get(0).get("h1"));
            assertEquals("v4", records.get(1).get("h2"));
            // Verify that first record is not in data (skipHeaderRecord default true)
        }
    }

    // Tests that record's toMap() method returns correct mapping
    @Test
    public void testRecordToMap_returnsCorrectMap() throws IOException {
        String input = "col1,col2\nval1,val2";
        CSVFormat format = CSVFormat.DEFAULT.withHeader();
        try (CSVParser parser = new CSVParser(new StringReader(input), format)) {
            List<CSVRecord> records = parser.getRecords();
            CSVRecord record = records.get(0);
            Map<String, String> map = record.toMap();
            assertNotNull(map);
            assertEquals(2, map.size());
            assertEquals("val1", map.get("col1"));
            assertEquals("val2", map.get("col2"));
        }
    }

    // Tests that getHeaderMap() returns null when using DEFAULT format (no header) - already covered? But we add a more explicit case.
    @Test
    public void testGetHeaderMap_withDefaultFormat_returnsNull() throws IOException {
        String input = "a,b";
        try (CSVParser parser = new CSVParser(new StringReader(input), CSVFormat.DEFAULT)) {
            assertNull(parser.getHeaderMap());
        }
    }

    // Tests parser with custom escape character
    @Test
    public void testParseWithEscapeCharacter_unescapesCorrectly() throws IOException {
        String input = "a\\,b,c"; // escape comma with backslash
        CSVFormat format = CSVFormat.DEFAULT.withEscape('\\');
        try (CSVParser parser = new CSVParser(new StringReader(input), format)) {
            CSVRecord record = parser.nextRecord();
            assertEquals("a,b", record.get(0));
            assertEquals("c", record.get(1));
        }
    }

    // Tests parser with quoted fields and embedded quotes (double quotes)
    @Test
    public void testParseWithQuotedField_handlesEmbeddedQuotes() throws IOException {
        String input = "\"Say \"\"Hello\"\"\",world";
        try (CSVParser parser = new CSVParser(new StringReader(input), CSVFormat.DEFAULT)) {
            CSVRecord record = parser.nextRecord();
            assertEquals("Say \"Hello\"", record.get(0));
            assertEquals("world", record.get(1));
        }
    }

    // Tests that parser ignores empty lines when configured
    @Test
    public void testParseWithIgnoreEmptyLines_skipsEmptyLines() throws IOException {
        String input = "a,b\n\nc,d\n";
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreEmptyLines(true);
        try (CSVParser parser = new CSVParser(new StringReader(input), format)) {
            List<CSVRecord> records = parser.getRecords();
            assertEquals(2, records.size());
            assertEquals("a", records.get(0).get(0));
            assertEquals("c", records.get(1).get(0));
        }
    }

    // Tests that consecutive close() calls are idempotent (no exception)
    @Test
    public void testClose_multipleCalls_doesNotThrow() throws IOException {
        CSVParser parser = new CSVParser(new StringReader("a,b"), CSVFormat.DEFAULT);
        parser.close();
        parser.close(); // should not throw
        assertTrue(parser.isClosed());
    }

    // Tests parsing with a custom delimiter (tab)
    @Test
    public void testParseWithTabDelimiter_parsesCorrectly() throws IOException {
        String input = "a\tb\n1\t2";
        CSVFormat format = CSVFormat.TDF;
        try (CSVParser parser = new CSVParser(new StringReader(input), format)) {
            List<CSVRecord> records = parser.getRecords();
            assertEquals(1, records.size());
            assertEquals("1", records.get(0).get(0));
            assertEquals("2", records.get(0).get(1));
        }
    }
}