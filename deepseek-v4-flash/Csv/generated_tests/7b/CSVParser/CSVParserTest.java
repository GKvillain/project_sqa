package org.apache.commons.csv;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.junit.Test;

public class CSVParserTest {

    @Test
    public void testParse_normalCsv_returnsCorrectRecords() throws IOException {
        CSVParser parser = CSVParser.parse("a,b\n1,2\n3,4", CSVFormat.DEFAULT);
        try {
            List<CSVRecord> records = parser.getRecords();
            assertEquals(2, records.size());
            assertEquals("1", records.get(0).get(0));
            assertEquals("2", records.get(0).get(1));
            assertEquals("3", records.get(1).get(0));
            assertEquals("4", records.get(1).get(1));
        } finally {
            parser.close();
        }
    }

    @Test
    public void testParse_emptyString_returnsEmptyList() throws IOException {
        CSVParser parser = CSVParser.parse("", CSVFormat.DEFAULT);
        try {
            List<CSVRecord> records = parser.getRecords();
            assertTrue(records.isEmpty());
        } finally {
            parser.close();
        }
    }

    @Test
    public void testParse_singleField_returnsRecordWithOneValue() throws IOException {
        CSVParser parser = CSVParser.parse("hello", CSVFormat.DEFAULT);
        try {
            List<CSVRecord> records = parser.getRecords();
            assertEquals(1, records.size());
            assertEquals("hello", records.get(0).get(0));
        } finally {
            parser.close();
        }
    }

    @Test
    public void testParse_headerFromFirstLine_buildsCorrectHeaderMap() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withHeader();
        CSVParser parser = CSVParser.parse("col1,col2\nval1,val2", format);
        try {
            Map<String, Integer> headerMap = parser.getHeaderMap();
            assertNotNull(headerMap);
            assertEquals(2, headerMap.size());
            assertEquals(Integer.valueOf(0), headerMap.get("col1"));
            assertEquals(Integer.valueOf(1), headerMap.get("col2"));
            List<CSVRecord> records = parser.getRecords();
            assertEquals(1, records.size());
            assertEquals("val1", records.get(0).get(0));
        } finally {
            parser.close();
        }
    }

    @Test
    public void testParse_headerProvided_skipsFirstLine() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("x", "y").withSkipHeaderRecord(true);
        CSVParser parser = CSVParser.parse("a,b\n1,2", format);
        try {
            Map<String, Integer> headerMap = parser.getHeaderMap();
            assertNotNull(headerMap);
            assertEquals(2, headerMap.size());
            assertEquals(Integer.valueOf(0), headerMap.get("x"));
            assertEquals(Integer.valueOf(1), headerMap.get("y"));
            List<CSVRecord> records = parser.getRecords();
            assertEquals(1, records.size());
            assertEquals("1", records.get(0).get(0));
            assertEquals("2", records.get(0).get(1));
        } finally {
            parser.close();
        }
    }

    @Test
    public void testParse_headerProvided_doesNotSkipFirstLine() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("x", "y").withSkipHeaderRecord(false);
        CSVParser parser = CSVParser.parse("a,b\n1,2", format);
        try {
            Map<String, Integer> headerMap = parser.getHeaderMap();
            assertNotNull(headerMap);
            assertEquals(2, headerMap.size());
            assertEquals(Integer.valueOf(0), headerMap.get("x"));
            assertEquals(Integer.valueOf(1), headerMap.get("y"));
            List<CSVRecord> records = parser.getRecords();
            assertEquals(2, records.size());
            assertEquals("a", records.get(0).get(0));
            assertEquals("b", records.get(0).get(1));
            assertEquals("1", records.get(1).get(0));
            assertEquals("2", records.get(1).get(1));
        } finally {
            parser.close();
        }
    }

    @Test
    public void testGetHeaderMap_whenNoHeader_returnsNull() throws IOException {
        CSVParser parser = CSVParser.parse("a,b", CSVFormat.DEFAULT);
        try {
            assertNull(parser.getHeaderMap());
        } finally {
            parser.close();
        }
    }

    @Test
    public void testParse_nullStringReplacement_replacesMatchingValues() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        CSVParser parser = CSVParser.parse("a,NULL\n1,2", format);
        try {
            List<CSVRecord> records = parser.getRecords();
            assertEquals(1, records.size());
            assertEquals("a", records.get(0).get(0));
            assertNull(records.get(0).get(1));
        } finally {
            parser.close();
        }
    }

    @Test
    public void testParse_nullStringIsNull_doesNotReplace() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT;
        CSVParser parser = CSVParser.parse("NULL,test", format);
        try {
            List<CSVRecord> records = parser.getRecords();
            assertEquals(1, records.size());
            assertEquals("NULL", records.get(0).get(0));
            assertEquals("test", records.get(0).get(1));
        } finally {
            parser.close();
        }
    }

    @Test
    public void testParse_nullStringIgnoreCase_replacesRegardlessOfCase() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("null");
        CSVParser parser = CSVParser.parse("NULL,Null,null", format);
        try {
            List<CSVRecord> records = parser.getRecords();
            assertEquals(1, records.size());
            assertNull(records.get(0).get(0));
            assertNull(records.get(0).get(1));
            assertNull(records.get(0).get(2));
        } finally {
            parser.close();
        }
    }

    @Test
    public void testParse_commentLine_ignored() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        CSVParser parser = CSVParser.parse("# comment\na,b\n", format);
        try {
            List<CSVRecord> records = parser.getRecords();
            assertEquals(1, records.size());
            assertEquals("a", records.get(0).get(0));
            assertEquals("b", records.get(0).get(1));
        } finally {
            parser.close();
        }
    }

    @Test
    public void testParse_trailingDelimiter_createsEmptyField() throws IOException {
        CSVParser parser = CSVParser.parse("a,", CSVFormat.DEFAULT);
        try {
            List<CSVRecord> records = parser.getRecords();
            assertEquals(1, records.size());
            assertEquals(2, records.get(0).size());
            assertEquals("a", records.get(0).get(0));
            assertEquals("", records.get(0).get(1));
        } finally {
            parser.close();
        }
    }

    @Test
    public void testParse_endsWithNewline_notExtraRecord() throws IOException {
        CSVParser parser = CSVParser.parse("a\n", CSVFormat.DEFAULT);
        try {
            List<CSVRecord> records = parser.getRecords();
            assertEquals(1, records.size());
        } finally {
            parser.close();
        }
    }

    @Test
    public void testParse_multilineQuotedField_parsesCorrectly() throws IOException {
        CSVParser parser = CSVParser.parse("\"hello\nworld\"\nnext", CSVFormat.DEFAULT);
        try {
            List<CSVRecord> records = parser.getRecords();
            assertEquals(2, records.size());
            assertEquals("hello\nworld", records.get(0).get(0));
            assertEquals("next", records.get(1).get(0));
        } finally {
            parser.close();
        }
    }

    @Test
    public void testParser_recordNumber_incrementsCorrectly() throws IOException {
        CSVParser parser = CSVParser.parse("a\nb\nc", CSVFormat.DEFAULT);
        try {
            CSVRecord rec1 = parser.nextRecord();
            assertNotNull(rec1);
            assertEquals(1, rec1.getRecordNumber());
            CSVRecord rec2 = parser.nextRecord();
            assertEquals(2, rec2.getRecordNumber());
            CSVRecord rec3 = parser.nextRecord();
            assertEquals(3, rec3.getRecordNumber());
            assertNull(parser.nextRecord());
        } finally {
            parser.close();
        }
    }

    @Test
    public void testGetCurrentLineNumber_returnsCurrentLine() throws IOException {
        CSVParser parser = CSVParser.parse("a\nb\nc", CSVFormat.DEFAULT);
        try {
            assertEquals(0, parser.getCurrentLineNumber());
            parser.nextRecord();
            assertEquals(1, parser.getCurrentLineNumber());
            parser.nextRecord();
            assertEquals(2, parser.getCurrentLineNumber());
        } finally {
            parser.close();
        }
    }

    @Test
    public void testGetRecords_afterPartialRead_returnsRemainingRecords() throws IOException {
        CSVParser parser = CSVParser.parse("1,2\n3,4\n5,6", CSVFormat.DEFAULT);
        try {
            parser.nextRecord();
            List<CSVRecord> remaining = parser.getRecords();
            assertEquals(2, remaining.size());
            assertEquals("3", remaining.get(0).get(0));
            assertEquals("5", remaining.get(1).get(0));
        } finally {
            parser.close();
        }
    }

    @Test
    public void testIterator_hasNextNext_returnsRecords() throws IOException {
        CSVParser parser = CSVParser.parse("x,y\n1,2", CSVFormat.DEFAULT);
        try {
            Iterator<CSVRecord> it = parser.iterator();
            assertTrue(it.hasNext());
            CSVRecord rec = it.next();
            assertEquals("1", rec.get(0));
            assertEquals("2", rec.get(1));
            assertFalse(it.hasNext());
        } finally {
            parser.close();
        }
    }

    @Test(expected = NoSuchElementException.class)
    public void testIterator_nextAfterClose_throwsNoSuchElementException() throws IOException {
        CSVParser parser = CSVParser.parse("a", CSVFormat.DEFAULT);
        parser.close();
        parser.iterator().next();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullReader_throwsIllegalArgumentException() throws IOException {
        new CSVParser(null, CSVFormat.DEFAULT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullFormat_throwsIllegalArgumentException() throws IOException {
        new CSVParser(new StringReader(""), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_invalidFormat_throwsIllegalArgumentException() throws IOException {
        CSVFormat invalidFormat = CSVFormat.newFormat(',').withQuote(',');
        new CSVParser(new StringReader("a,b"), invalidFormat);
    }

    @Test
    public void testIsClosed_returnsTrueAfterClose() throws IOException {
        CSVParser parser = CSVParser.parse("test", CSVFormat.DEFAULT);
        assertFalse(parser.isClosed());
        parser.close();
        assertTrue(parser.isClosed());
    }

    // ========== Test cases added for uncovered areas ==========

    @Test
    public void testParse_quotedFieldsWithEmbeddedQuotes_unescapesCorrectly() throws IOException {
        CSVParser parser = CSVParser.parse("\"she said \"\"hello\"\"\"", CSVFormat.DEFAULT);
        try {
            List<CSVRecord> records = parser.getRecords();
            assertEquals(1, records.size());
            assertEquals("she said \"hello\"", records.get(0).get(0));
        } finally {
            parser.close();
        }
    }

    @Test
    public void testParse_differentDelimiter_parsesCorrectly() throws IOException {
        CSVFormat format = CSVFormat.newFormat(';');
        CSVParser parser = CSVParser.parse("a;b\n1;2", format);
        try {
            List<CSVRecord> records = parser.getRecords();
            assertEquals(1, records.size());
            assertEquals("a", records.get(0).get(0));
            assertEquals("b", records.get(0).get(1));
        } finally {
            parser.close();
        }
    }

    @Test
    public void testParse_emptyFirstField_returnsEmptyString() throws IOException {
        CSVParser parser = CSVParser.parse(",b", CSVFormat.DEFAULT);
        try {
            List<CSVRecord> records = parser.getRecords();
            assertEquals(1, records.size());
            assertEquals("", records.get(0).get(0));
            assertEquals("b", records.get(0).get(1));
        } finally {
            parser.close();
        }
    }

    @Test
    public void testParse_multipleEmptyFields_returnsCorrectCount() throws IOException {
        CSVParser parser = CSVParser.parse("a,,,d", CSVFormat.DEFAULT);
        try {
            List<CSVRecord> records = parser.getRecords();
            assertEquals(1, records.size());
            assertEquals(4, records.get(0).size());
            assertEquals("a", records.get(0).get(0));
            assertEquals("", records.get(0).get(1));
            assertEquals("", records.get(0).get(2));
            assertEquals("d", records.get(0).get(3));
        } finally {
            parser.close();
        }
    }

    @Test
    public void testParse_ignoreEmptyLines_skipsEmptyLines() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreEmptyLines();
        CSVParser parser = CSVParser.parse("a\n\nb", format);
        try {
            List<CSVRecord> records = parser.getRecords();
            assertEquals(2, records.size());
            assertEquals("a", records.get(0).get(0));
            assertEquals("b", records.get(1).get(0));
        } finally {
            parser.close();
        }
    }

    @Test
    public void testParse_trimWhitespace_trimsFields() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withTrim();
        CSVParser parser = CSVParser.parse(" a , b ", format);
        try {
            List<CSVRecord> records = parser.getRecords();
            assertEquals(1, records.size());
            assertEquals("a", records.get(0).get(0));
            assertEquals("b", records.get(0).get(1));
        } finally {
            parser.close();
        }
    }

    @Test
    public void testParse_escapeCharacter_unescapesCorrectly() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withEscape('\\');
        CSVParser parser = CSVParser.parse("a\\,b", format);
        try {
            List<CSVRecord> records = parser.getRecords();
            assertEquals(1, records.size());
            assertEquals("a,b", records.get(0).get(0));
        } finally {
            parser.close();
        }
    }

    @Test
    public void testParse_quotedFieldWithEscape_processesInsideQuotes() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withEscape('\\');
        CSVParser parser = CSVParser.parse("\"a\\\"b\"", format);
        try {
            List<CSVRecord> records = parser.getRecords();
            assertEquals(1, records.size());
            assertEquals("a\"b", records.get(0).get(0));
        } finally {
            parser.close();
        }
    }

    @Test
    public void testParse_noTrim_preservesWhitespace() throws IOException {
        CSVParser parser = CSVParser.parse(" a , b ", CSVFormat.DEFAULT);
        try {
            List<CSVRecord> records = parser.getRecords();
            assertEquals(1, records.size());
            assertEquals(" a ", records.get(0).get(0));
            assertEquals(" b ", records.get(0).get(1));
        } finally {
            parser.close();
        }
    }

    @Test
    public void testIterator_remove_throwsUnsupportedOperationException() throws IOException {
        CSVParser parser = CSVParser.parse("a", CSVFormat.DEFAULT);
        try {
            Iterator<CSVRecord> it = parser.iterator();
            it.next();
            assertThrows(UnsupportedOperationException.class, () -> it.remove());
        } finally {
            parser.close();
        }
    }

    @Test
    public void testParse_customQuoteChar_handlesQuotes() throws IOException {
        CSVFormat format = CSVFormat.newFormat(',').withQuote('\'');
        CSVParser parser = CSVParser.parse("'hello,world',test", format);
        try {
            List<CSVRecord> records = parser.getRecords();
            assertEquals(1, records.size());
            assertEquals("hello,world", records.get(0).get(0));
            assertEquals("test", records.get(0).get(1));
        } finally {
            parser.close();
        }
    }

    @Test
    public void testParse_noHeader_getHeaderMap_returnsNull() throws IOException {
        CSVParser parser = CSVParser.parse("a,b", CSVFormat.DEFAULT);
        try {
            assertNull(parser.getHeaderMap());
        } finally {
            parser.close();
        }
    }

    @Test
    public void testParse_headerMapWithDuplicate_throwsException() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("x", "x");
        assertThrows(IllegalArgumentException.class, () -> {
            CSVParser parser = CSVParser.parse("a,b", format);
            parser.close();
        });
    }

    @Test
    public void testParse_allowMissingColumnNames_withHeader_succeeds() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withHeader().withAllowMissingColumnNames();
        CSVParser parser = CSVParser.parse("a,b\n1,2", format);
        try {
            Map<String, Integer> headerMap = parser.getHeaderMap();
            assertNotNull(headerMap);
            assertEquals(2, headerMap.size());
            assertEquals(Integer.valueOf(0), headerMap.get("a"));
            assertEquals(Integer.valueOf(1), headerMap.get("b"));
        } finally {
            parser.close();
        }
    }

    @Test
    public void testParse_ignoreSurroundingSpaces_withTrim_worksCorrectly() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withTrim().withIgnoreSurroundingSpaces();
        CSVParser parser = CSVParser.parse("  a  ,  b  ", format);
        try {
            List<CSVRecord> records = parser.getRecords();
            assertEquals(1, records.size());
            assertEquals("a", records.get(0).get(0));
            assertEquals("b", records.get(0).get(1));
        } finally {
            parser.close();
        }
    }
}