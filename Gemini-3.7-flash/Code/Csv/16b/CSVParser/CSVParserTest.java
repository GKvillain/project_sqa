package org.apache.commons.csv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.junit.Test;

public class CSVParserTest {

    // Tests simple CSV parsing with comma delimiter
    @Test
    public void testParseString_simpleCsv_returnsCorrectRecords() throws IOException {
        final String csv = "a,b,c\n1,2,3";
        final CSVParser parser = CSVParser.parse(csv, CSVFormat.DEFAULT);
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

    // Tests null string input in static parse method
    @Test(expected = IllegalArgumentException.class)
    public void testParseString_nullString_throwsException() throws IOException {
        CSVParser.parse((String) null, CSVFormat.DEFAULT);
    }

    // Tests null format input in static parse method
    @Test(expected = IllegalArgumentException.class)
    public void testParseString_nullFormat_throwsException() throws IOException {
        CSVParser.parse("a,b,c", null);
    }

    // Tests parsing from Reader
    @Test
    public void testParseReader_validReader_returnsParser() throws IOException {
        final CSVParser parser = CSVParser.parse(new StringReader("x,y\n4,5"), CSVFormat.DEFAULT);
        final List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals("x", records.get(0).get(0));
        assertEquals("y", records.get(0).get(1));
        parser.close();
    }

    // Tests parsing from File and Path
    @Test
    public void testParseFileAndPath_validFile_success() throws IOException {
        final File tempFile = File.createTempFile("csv_test", ".csv");
        tempFile.deleteOnExit();
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(tempFile), StandardCharsets.UTF_8)) {
            writer.write("h1,h2\nv1,v2\n");
        }

        try (CSVParser parser = CSVParser.parse(tempFile, StandardCharsets.UTF_8, CSVFormat.DEFAULT)) {
            final List<CSVRecord> records = parser.getRecords();
            assertEquals(2, records.size());
            assertEquals("h1", records.get(0).get(0));
        }

        final Path path = tempFile.toPath();
        try (CSVParser parser = CSVParser.parse(path, StandardCharsets.UTF_8, CSVFormat.DEFAULT)) {
            final List<CSVRecord> records = parser.getRecords();
            assertEquals(2, records.size());
            assertEquals("v1", records.get(1).get(0));
        }
    }

    // Tests parsing from InputStream
    @Test
    public void testParseInputStream_validStream_success() throws IOException {
        final InputStream in = new java.io.ByteArrayInputStream("col1,col2\nval1,val2".getBytes(StandardCharsets.UTF_8));
        try (CSVParser parser = CSVParser.parse(in, StandardCharsets.UTF_8, CSVFormat.DEFAULT)) {
            final List<CSVRecord> records = parser.getRecords();
            assertEquals(2, records.size());
            assertEquals("val2", records.get(1).get(1));
        }
    }

    // Tests constructor with offset and initial record number
    @Test
    public void testConstructor_withOffsetAndRecordNumber_maintainsState() throws IOException {
        final StringReader reader = new StringReader("A,B\nC,D");
        try (CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT, 10L, 5L)) {
            assertEquals(4L, parser.getRecordNumber());
            final CSVRecord record1 = parser.nextRecord();
            assertNotNull(record1);
            assertEquals(5L, parser.getRecordNumber());
            assertEquals(5L, record1.getRecordNumber());
            assertEquals("A", record1.get(0));
            assertEquals(10L, record1.getCharacterPosition());

            final CSVRecord record2 = parser.nextRecord();
            assertNotNull(record2);
            assertEquals(6L, parser.getRecordNumber());
            assertEquals(6L, record2.getRecordNumber());
        }
    }

    // Tests header initialization from the first line
    @Test
    public void testHeader_firstRecordAsHeader_parsesHeaderCorrectly() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT.withHeader();
        final String csv = "ColA,ColB\n1,2\n3,4";
        try (CSVParser parser = CSVParser.parse(csv, format)) {
            final Map<String, Integer> headerMap = parser.getHeaderMap();
            assertNotNull(headerMap);
            assertEquals(2, headerMap.size());
            assertEquals(Integer.valueOf(0), headerMap.get("ColA"));
            assertEquals(Integer.valueOf(1), headerMap.get("ColB"));

            final List<CSVRecord> records = parser.getRecords();
            assertEquals(2, records.size());
            assertEquals("1", records.get(0).get("ColA"));
            assertEquals("2", records.get(0).get("ColB"));
        }
    }

    // Tests header with ignoreHeaderCase format
    @Test
    public void testHeader_ignoreHeaderCase_caseInsensitiveLookup() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT.withHeader("HeaderOne", "HeaderTwo").withIgnoreHeaderCase();
        try (CSVParser parser = CSVParser.parse("1,2", format)) {
            final Map<String, Integer> headerMap = parser.getHeaderMap();
            assertNotNull(headerMap);
            assertEquals(Integer.valueOf(0), headerMap.get("headerone"));
            assertEquals(Integer.valueOf(1), headerMap.get("HEADERTWO"));
            final CSVRecord record = parser.nextRecord();
            assertEquals("1", record.get("headerone"));
            assertEquals("2", record.get("headertwo"));
        }
    }

    // Tests header with skipHeaderRecord option
    @Test
    public void testHeader_skipHeaderRecord_skipsFirstLine() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT.withHeader("H1", "H2").withSkipHeaderRecord();
        final String csv = "H1,H2\nval1,val2";
        try (CSVParser parser = CSVParser.parse(csv, format)) {
            final List<CSVRecord> records = parser.getRecords();
            assertEquals(1, records.size());
            assertEquals("val1", records.get(0).get("H1"));
            assertEquals("val2", records.get(0).get("H2"));
        }
    }

    // Tests duplicate headers throwing exception when allowMissingColumnNames is false
    @Test(expected = IllegalArgumentException.class)
    public void testHeader_duplicateHeaderNames_throwsException() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT.withHeader("Col", "Col");
        CSVParser.parse("1,2", format);
    }

    // Tests duplicate empty headers when allowMissingColumnNames is true
    @Test
    public void testHeader_duplicateEmptyHeadersWithAllowMissing_success() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT.withHeader("", "").withAllowMissingColumnNames();
        try (CSVParser parser = CSVParser.parse("1,2", format)) {
            assertNotNull(parser.getHeaderMap());
        }
    }

    // Tests header map is null when no header is configured
    @Test
    public void testGetHeaderMap_noHeader_returnsNull() throws IOException {
        try (CSVParser parser = CSVParser.parse("a,b,c", CSVFormat.DEFAULT)) {
            assertNull(parser.getHeaderMap());
        }
    }

    // Tests trim, trailing delimiter, and nullString configuration
    @Test
    public void testAddRecordValue_trimAndNullStringAndTrailingDelimiter() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT
                .withTrim()
                .withNullString("NULL")
                .withTrailingDelimiter();
        final String csv = " val1 , NULL , ";
        try (CSVParser parser = CSVParser.parse(csv, format)) {
            final CSVRecord record = parser.nextRecord();
            assertNotNull(record);
            assertEquals(2, record.size());
            assertEquals("val1", record.get(0));
            assertNull(record.get(1));
        }
    }

    // Tests comments in CSV records
    @Test
    public void testNextRecord_withComments_capturesComment() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        final String csv = "# Header comment\na,b\n# Next comment\nc,d";
        try (CSVParser parser = CSVParser.parse(csv, format)) {
            final CSVRecord record1 = parser.nextRecord();
            assertNotNull(record1);
            assertEquals("Header comment", record1.getComment());
            assertEquals("a", record1.get(0));

            final CSVRecord record2 = parser.nextRecord();
            assertNotNull(record2);
            assertEquals("Next comment", record2.getComment());
            assertEquals("c", record2.get(0));
        }
    }

    // Tests line number tracking and end-of-line detection
    @Test
    public void testLineNumberAndFirstEndOfLine() throws IOException {
        final String csv = "a,b\r\nc,d\r\ne,f";
        try (CSVParser parser = CSVParser.parse(csv, CSVFormat.DEFAULT)) {
            assertEquals(1L, parser.getCurrentLineNumber());
            parser.nextRecord();
            assertEquals("\r\n", parser.getFirstEndOfLine());
            assertEquals(2L, parser.getCurrentLineNumber());
            parser.nextRecord();
            assertEquals(3L, parser.getCurrentLineNumber());
        }
    }

    // Tests Iterator functionality including hasNext, next, and remove
    @Test
    public void testIterator_standardIteration_worksCorrectly() throws IOException {
        final String csv = "1,2\n3,4";
        try (CSVParser parser = CSVParser.parse(csv, CSVFormat.DEFAULT)) {
            final Iterator<CSVRecord> it = parser.iterator();
            assertTrue(it.hasNext());
            final CSVRecord r1 = it.next();
            assertEquals("1", r1.get(0));
            assertTrue(it.hasNext());
            final CSVRecord r2 = it.next();
            assertEquals("3", r2.get(0));
            assertFalse(it.hasNext());
        }
    }

    // Tests Iterator remove method throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testIterator_remove_throwsException() throws IOException {
        try (CSVParser parser = CSVParser.parse("1,2", CSVFormat.DEFAULT)) {
            final Iterator<CSVRecord> it = parser.iterator();
            it.next();
            it.remove();
        }
    }

    // Tests Iterator on closed parser
    @Test(expected = NoSuchElementException.class)
    public void testIterator_whenClosed_throwsNoSuchElementException() throws IOException {
        final CSVParser parser = CSVParser.parse("1,2", CSVFormat.DEFAULT);
        final Iterator<CSVRecord> it = parser.iterator();
        parser.close();
        assertTrue(parser.isClosed());
        assertFalse(it.hasNext());
        it.next();
    }

    // Tests Iterator calling next beyond available records
    @Test(expected = NoSuchElementException.class)
    public void testIterator_noMoreRecords_throwsNoSuchElementException() throws IOException {
        try (CSVParser parser = CSVParser.parse("1,2", CSVFormat.DEFAULT)) {
            final Iterator<CSVRecord> it = parser.iterator();
            it.next();
            it.next();
        }
    }

    // Tests invalid CSV sequence throwing IOException
    @Test(expected = IOException.class)
    public void testNextRecord_invalidSequence_throwsIOException() throws IOException {
        final String csv = "\"unclosed quote";
        try (CSVParser parser = CSVParser.parse(csv, CSVFormat.DEFAULT)) {
            parser.getRecords();
        }
    }
}