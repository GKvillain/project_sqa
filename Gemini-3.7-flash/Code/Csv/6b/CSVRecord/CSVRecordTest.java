package org.apache.commons.csv;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class CSVRecordTest {

    private enum TestHeader {
        FIRST, SECOND, THIRD
    }

    private String[] values;
    private Map<String, Integer> headerMap;
    private CSVRecord record;

    @Before
    public void setUp() {
        values = new String[]{"first", "second", "third"};
        headerMap = new HashMap<String, Integer>();
        headerMap.put("FIRST", 0);
        headerMap.put("SECOND", 1);
        headerMap.put("THIRD", 2);
        record = new CSVRecord(values, headerMap, "test comment", 1L);
    }

    // Tests retrieving value by index
    @Test
    public void testGet_byIndex_returnsCorrectValue() {
        assertEquals("first", record.get(0));
        assertEquals("second", record.get(1));
        assertEquals("third", record.get(2));
    }

    // Tests retrieving value by column name
    @Test
    public void testGet_byName_returnsCorrectValue() {
        assertEquals("first", record.get("FIRST"));
        assertEquals("second", record.get("SECOND"));
        assertEquals("third", record.get("THIRD"));
    }

    // Tests retrieving value by enum constant
    @Test
    public void testGet_byEnum_returnsCorrectValue() {
        assertEquals("first", record.get(TestHeader.FIRST));
        assertEquals("second", record.get(TestHeader.SECOND));
        assertEquals("third", record.get(TestHeader.THIRD));
    }

    // Tests exception path when header mapping is null
    @Test(expected = IllegalStateException.class)
    public void testGet_byNameWithNullMapping_throwsIllegalStateException() {
        final CSVRecord recordWithoutHeader = new CSVRecord(values, null, null, 0L);
        recordWithoutHeader.get("FIRST");
    }

    // Tests exception path when column name is not present in mapping
    @Test(expected = IllegalArgumentException.class)
    public void testGet_unmappedName_throwsIllegalArgumentException() {
        record.get("UNKNOWN");
    }

    // Tests exception path when mapped index exceeds values array length
    @Test(expected = IllegalArgumentException.class)
    public void testGet_indexOutOfBoundsInMapping_throwsIllegalArgumentException() {
        final Map<String, Integer> invalidMapping = new HashMap<String, Integer>();
        invalidMapping.put("FOURTH", 3);
        final CSVRecord shortRecord = new CSVRecord(new String[]{"a"}, invalidMapping, null, 0L);
        shortRecord.get("FOURTH");
    }

    // Tests retrieving comment
    @Test
    public void testGetComment_returnsComment() {
        assertEquals("test comment", record.getComment());
        final CSVRecord recordNoComment = new CSVRecord(values, headerMap, null, 0L);
        assertNull(recordNoComment.getComment());
    }

    // Tests retrieving record number
    @Test
    public void testGetRecordNumber_returnsRecordNumber() {
        assertEquals(1L, record.getRecordNumber());
    }

    // Tests consistency when mapping matches values length
    @Test
    public void testIsConsistent_matchingSize_returnsTrue() {
        assertTrue(record.isConsistent());
    }

    // Tests consistency when mapping does not match values length
    @Test
    public void testIsConsistent_mismatchedSize_returnsFalse() {
        final CSVRecord shortRecord = new CSVRecord(new String[]{"a"}, headerMap, null, 0L);
        assertFalse(shortRecord.isConsistent());
    }

    // Tests consistency when mapping is null
    @Test
    public void testIsConsistent_nullMapping_returnsTrue() {
        final CSVRecord recordWithoutHeader = new CSVRecord(values, null, null, 0L);
        assertTrue(recordWithoutHeader.isConsistent());
    }

    // Tests isMapped with mapped and unmapped column names
    @Test
    public void testIsMapped_validAndInvalidNames() {
        assertTrue(record.isMapped("FIRST"));
        assertTrue(record.isMapped("SECOND"));
        assertFalse(record.isMapped("UNKNOWN"));

        final CSVRecord recordWithoutHeader = new CSVRecord(values, null, null, 0L);
        assertFalse(recordWithoutHeader.isMapped("FIRST"));
    }

    // Tests isSet when mapped and within bounds
    @Test
    public void testIsSet_validIndex_returnsTrue() {
        assertTrue(record.isSet("FIRST"));
        assertTrue(record.isSet("THIRD"));
    }

    // Tests isSet when column is mapped but index is out of bounds
    @Test
    public void testIsSet_indexOutOfBounds_returnsFalse() {
        final Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("A", 0);
        map.put("B", 1);
        map.put("C", 2);
        final CSVRecord shortRecord = new CSVRecord(new String[]{"valA"}, map, null, 0L);
        assertTrue(shortRecord.isSet("A"));
        assertFalse(shortRecord.isSet("B"));
        assertFalse(shortRecord.isSet("C"));
        assertFalse(shortRecord.isSet("UNKNOWN"));
    }

    // Tests isSet with null mapping
    @Test
    public void testIsSet_nullMapping_returnsFalse() {
        final CSVRecord recordWithoutHeader = new CSVRecord(values, null, null, 0L);
        assertFalse(recordWithoutHeader.isSet("FIRST"));
    }

    // Tests iterator over record values
    @Test
    public void testIterator_iteratesAllValues() {
        final Iterator<String> it = record.iterator();
        assertNotNull(it);
        assertTrue(it.hasNext());
        assertEquals("first", it.next());
        assertTrue(it.hasNext());
        assertEquals("second", it.next());
        assertTrue(it.hasNext());
        assertEquals("third", it.next());
        assertFalse(it.hasNext());
    }

    // Tests size of record
    @Test
    public void testSize_returnsCorrectSize() {
        assertEquals(3, record.size());
        final CSVRecord emptyRecord = new CSVRecord(null, null, null, 0L);
        assertEquals(0, emptyRecord.size());
    }

    // Tests toMap conversion
    @Test
    public void testToMap_populatesAllMappedEntries() {
        final Map<String, String> map = record.toMap();
        assertEquals(3, map.size());
        assertEquals("first", map.get("FIRST"));
        assertEquals("second", map.get("SECOND"));
        assertEquals("third", map.get("THIRD"));
    }

    // Tests putIn populates the given destination map
    @Test
    public void testPutIn_populatesTargetMap() {
        final Map<String, String> target = new HashMap<String, String>();
        final Map<String, String> result = record.putIn(target);
        assertEquals(target, result);
        assertEquals(3, result.size());
        assertEquals("first", result.get("FIRST"));
        assertEquals("second", result.get("SECOND"));
        assertEquals("third", result.get("THIRD"));
    }

    // Tests toString representation
    @Test
    public void testToString_returnsArrayString() {
        assertEquals("[first, second, third]", record.toString());
    }

    // Tests values accessor method
    @Test
    public void testValues_returnsUnderlyingArray() {
        final String[] actual = record.values();
        assertEquals(3, actual.length);
        assertEquals("first", actual[0]);
        assertEquals("second", actual[1]);
        assertEquals("third", actual[2]);
    }
}