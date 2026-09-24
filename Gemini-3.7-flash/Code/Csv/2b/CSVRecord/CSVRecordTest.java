package org.apache.commons.csv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class CSVRecordTest {

    private String[] values;
    private Map<String, Integer> headerMap;
    private CSVRecord record;

    @Before
    public void setUp() {
        values = new String[]{"first", "second", "third"};
        headerMap = new HashMap<String, Integer>();
        headerMap.put("A", 0);
        headerMap.put("B", 1);
        headerMap.put("C", 2);
        record = new CSVRecord(values, headerMap, "test comment", 1L);
    }

    // Tests get by valid index
    @Test
    public void testGetInt_validIndex_returnsCorrectValue() {
        assertEquals("first", record.get(0));
        assertEquals("second", record.get(1));
        assertEquals("third", record.get(2));
    }

    // Tests get by mapped column name
    @Test
    public void testGetString_validName_returnsCorrectValue() {
        assertEquals("first", record.get("A"));
        assertEquals("second", record.get("B"));
        assertEquals("third", record.get("C"));
    }

    // Tests get by unmapped column name
    @Test
    public void testGetString_unmappedName_returnsNull() {
        assertNull(record.get("UNKNOWN"));
    }

    // Tests get by name when header mapping is null
    @Test(expected = IllegalStateException.class)
    public void testGetString_nullMapping_throwsIllegalStateException() {
        CSVRecord rec = new CSVRecord(values, null, null, 1L);
        rec.get("A");
    }

    // Tests get by name when record is inconsistent and column index is out of bounds
    @Test(expected = IllegalArgumentException.class)
    public void testGetString_inconsistentRecordOutOfBounds_throwsIllegalArgumentException() {
        Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("A", 0);
        map.put("B", 1);
        CSVRecord rec = new CSVRecord(new String[]{"only_one"}, map, null, 1L);
        rec.get("B");
    }

    // Tests isConsistent when header map size equals values length
    @Test
    public void testIsConsistent_matchingHeaderSize_returnsTrue() {
        assertTrue(record.isConsistent());
    }

    // Tests isConsistent when mapping is null
    @Test
    public void testIsConsistent_nullMapping_returnsTrue() {
        CSVRecord rec = new CSVRecord(values, null, null, 1L);
        assertTrue(rec.isConsistent());
    }

    // Tests isConsistent when header map size differs from values length
    @Test
    public void testIsConsistent_mismatchedHeaderSize_returnsFalse() {
        Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("A", 0);
        CSVRecord rec = new CSVRecord(values, map, null, 1L);
        assertFalse(rec.isConsistent());
    }

    // Tests isMapped with mapped and unmapped names
    @Test
    public void testIsMapped_mappedAndUnmappedNames_returnsExpectedBoolean() {
        assertTrue(record.isMapped("A"));
        assertFalse(record.isMapped("NON_EXISTENT"));
    }

    // Tests isMapped when mapping is null
    @Test
    public void testIsMapped_nullMapping_returnsFalse() {
        CSVRecord rec = new CSVRecord(values, null, null, 1L);
        assertFalse(rec.isMapped("A"));
    }

    // Tests isSet for mapped and populated column
    @Test
    public void testIsSet_mappedAndWithinBounds_returnsTrue() {
        assertTrue(record.isSet("A"));
        assertTrue(record.isSet("B"));
        assertTrue(record.isSet("C"));
    }

    // Tests isSet for unmapped column
    @Test
    public void testIsSet_unmappedColumn_returnsFalse() {
        assertFalse(record.isSet("NON_EXISTENT"));
    }

    // Tests isSet when column is mapped but index is out of bounds
    @Test
    public void testIsSet_mappedColumnOutOfBounds_returnsFalse() {
        Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("A", 0);
        map.put("B", 5);
        CSVRecord rec = new CSVRecord(new String[]{"value"}, map, null, 1L);
        assertTrue(rec.isSet("A"));
        assertFalse(rec.isSet("B"));
    }

    // Tests isSet when mapping is null
    @Test
    public void testIsSet_nullMapping_returnsFalse() {
        CSVRecord rec = new CSVRecord(values, null, null, 1L);
        assertFalse(rec.isSet("A"));
    }

    // Tests iterator across record values
    @Test
    public void testIterator_standardRecord_iteratesAllValues() {
        Iterator<String> iterator = record.iterator();
        assertNotNull(iterator);
        assertTrue(iterator.hasNext());
        assertEquals("first", iterator.next());
        assertEquals("second", iterator.next());
        assertEquals("third", iterator.next());
        assertFalse(iterator.hasNext());
    }

    // Tests getComment returns the comment string
    @Test
    public void testGetComment_withComment_returnsComment() {
        assertEquals("test comment", record.getComment());
    }

    // Tests getComment when comment is null
    @Test
    public void testGetComment_nullComment_returnsNull() {
        CSVRecord rec = new CSVRecord(values, headerMap, null, 1L);
        assertNull(rec.getComment());
    }

    // Tests getRecordNumber returns accurate number
    @Test
    public void testGetRecordNumber_validNumber_returnsCorrectNumber() {
        assertEquals(1L, record.getRecordNumber());
    }

    // Tests size of the record
    @Test
    public void testSize_standardRecord_returnsValuesLength() {
        assertEquals(3, record.size());
    }

    // Tests values() returns underlying array
    @Test
    public void testValues_standardRecord_returnsValuesArray() {
        String[] result = record.values();
        assertEquals(3, result.length);
        assertEquals("first", result[0]);
        assertEquals("second", result[1]);
        assertEquals("third", result[2]);
    }

    // Tests constructor with null values array
    @Test
    public void testConstructor_nullValues_createsEmptyRecord() {
        CSVRecord rec = new CSVRecord(null, Collections.<String, Integer>emptyMap(), null, 0L);
        assertEquals(0, rec.size());
        assertEquals(0, rec.values().length);
    }

    // Tests toString produces valid string representation
    @Test
    public void testToString_standardRecord_returnsFormattedString() {
        assertEquals("[first, second, third]", record.toString());
    }
}