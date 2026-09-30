package org.apache.commons.csv;

import org.junit.Test;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import static org.junit.Assert.*;

public class CSVRecordTest {

    private CSVRecord createRecord(final String[] values, final Map<String, Integer> mapping,
                                   final String comment, final long recordNumber) {
        return new CSVRecord(values, mapping, comment, recordNumber);
    }

    // Tests get(int) with valid index
    @Test
    public void testGet_byIndex_normal_returnsValue() {
        CSVRecord rec = createRecord(new String[]{"a", "b", "c"}, null, null, 1);
        assertEquals("a", rec.get(0));
        assertEquals("c", rec.get(2));
    }

    // Tests get(int) with negative index
    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testGet_byIndex_negative_throwsArrayIndexOutOfBounds() {
        CSVRecord rec = createRecord(new String[]{"a", "b"}, null, null, 1);
        rec.get(-1);
    }

    // Tests get(int) with out-of-bounds index
    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testGet_byIndex_outOfBounds_throwsArrayIndexOutOfBounds() {
        CSVRecord rec = createRecord(new String[]{"a", "b"}, null, null, 1);
        rec.get(2);
    }

    // Tests get(String) when mapping is null
    @Test(expected = IllegalStateException.class)
    public void testGet_byName_nullMapping_throwsIllegalStateException() {
        CSVRecord rec = createRecord(new String[]{"a"}, null, null, 1);
        rec.get("col");
    }

    // Tests get(String) with valid mapped index
    @Test
    public void testGet_byName_mappedValidIndex_returnsValue() {
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("col", 0);
        CSVRecord rec = createRecord(new String[]{"x", "y"}, mapping, null, 1);
        assertEquals("x", rec.get("col"));
    }

    // Tests get(String) with name that exists but mapped to index out of bounds (inconsistent record)
    // Expected: IllegalArgumentException per javadoc; buggy version throws ArrayIndexOutOfBoundsException
    @Test(expected = IllegalArgumentException.class)
    public void testGet_byName_mappedInvalidIndex_throwsIllegalArgumentException() {
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("col", 5);
        CSVRecord rec = createRecord(new String[]{"a", "b", "c"}, mapping, null, 1);
        rec.get("col");
    }

    // Tests get(String) when name is not in mapping
    @Test
    public void testGet_byName_unmappedName_returnsNull() {
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("col", 0);
        CSVRecord rec = createRecord(new String[]{"a"}, mapping, null, 1);
        assertNull(rec.get("other"));
    }

    // Tests isConsistent() when mapping is null
    @Test
    public void testIsConsistent_nullMapping_returnsTrue() {
        CSVRecord rec = createRecord(new String[]{"a"}, null, null, 1);
        assertTrue(rec.isConsistent());
    }

    // Tests isConsistent() when mapping size equals values length
    @Test
    public void testIsConsistent_mappingSizeEqualsValuesLength_returnsTrue() {
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("a", 0);
        mapping.put("b", 1);
        CSVRecord rec = createRecord(new String[]{"x", "y"}, mapping, null, 1);
        assertTrue(rec.isConsistent());
    }

    // Tests isConsistent() when mapping size does not equal values length
    @Test
    public void testIsConsistent_mappingSizeNotEqualsValuesLength_returnsFalse() {
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("a", 0);
        mapping.put("b", 1);
        CSVRecord rec = createRecord(new String[]{"x"}, mapping, null, 1);
        assertFalse(rec.isConsistent());
    }

    // Tests isMapped() when mapping is null
    @Test
    public void testIsMapped_nullMapping_returnsFalse() {
        CSVRecord rec = createRecord(new String[]{"a"}, null, null, 1);
        assertFalse(rec.isMapped("any"));
    }

    // Tests isMapped() when key is present
    @Test
    public void testIsMapped_keyPresent_returnsTrue() {
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("col", 0);
        CSVRecord rec = createRecord(new String[]{"a"}, mapping, null, 1);
        assertTrue(rec.isMapped("col"));
    }

    // Tests isMapped() when key is absent
    @Test
    public void testIsMapped_keyAbsent_returnsFalse() {
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("col", 0);
        CSVRecord rec = createRecord(new String[]{"a"}, mapping, null, 1);
        assertFalse(rec.isMapped("other"));
    }

    // Tests isSet() when mapping is null
    @Test
    public void testIsSet_nullMapping_returnsFalse() {
        CSVRecord rec = createRecord(new String[]{"a"}, null, null, 1);
        assertFalse(rec.isSet("any"));
    }

    // Tests isSet() when key exists and index is within bounds
    @Test
    public void testIsSet_mappedIndexLessThanLength_returnsTrue() {
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("col", 1);
        CSVRecord rec = createRecord(new String[]{"a", "b", "c"}, mapping, null, 1);
        assertTrue(rec.isSet("col"));
    }

    // Tests isSet() when key exists and index equals length
    @Test
    public void testIsSet_mappedIndexEqualsLength_returnsFalse() {
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("col", 3);
        CSVRecord rec = createRecord(new String[]{"a", "b", "c"}, mapping, null, 1);
        assertFalse(rec.isSet("col"));
    }

    // Tests isSet() when key exists and index exceeds length
    @Test
    public void testIsSet_mappedIndexGreaterThanLength_returnsFalse() {
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("col", 10);
        CSVRecord rec = createRecord(new String[]{"a", "b", "c"}, mapping, null, 1);
        assertFalse(rec.isSet("col"));
    }

    // Tests iterator() returns all values in order
    @Test
    public void testIterator_returnsAllValues() {
        CSVRecord rec = createRecord(new String[]{"a", "b", "c"}, null, null, 1);
        Iterator<String> it = rec.iterator();
        assertTrue(it.hasNext());
        assertEquals("a", it.next());
        assertEquals("b", it.next());
        assertEquals("c", it.next());
        assertFalse(it.hasNext());
    }

    // Tests getComment()
    @Test
    public void testGetComment_returnsComment() {
        CSVRecord rec = createRecord(new String[]{"a"}, null, "my comment", 1);
        assertEquals("my comment", rec.getComment());
    }

    // Tests getComment() when comment is null
    @Test
    public void testGetComment_nullComment_returnsNull() {
        CSVRecord rec = createRecord(new String[]{"a"}, null, null, 1);
        assertNull(rec.getComment());
    }

    // Tests getRecordNumber()
    @Test
    public void testGetRecordNumber_returnsRecordNumber() {
        CSVRecord rec = createRecord(new String[]{"a"}, null, null, 42);
        assertEquals(42L, rec.getRecordNumber());
    }

    // Tests size()
    @Test
    public void testSize_returnsLength() {
        CSVRecord rec = createRecord(new String[]{"a", "b"}, null, null, 1);
        assertEquals(2, rec.size());
    }

    // Tests toString() returns array representation
    @Test
    public void testToString_returnsArrayString() {
        CSVRecord rec = createRecord(new String[]{"a", "b", "c"}, null, null, 1);
        assertEquals("[a, b, c]", rec.toString());
    }

    // Tests constructor with null values gets empty array
    @Test
    public void testConstructor_nullValues_usesEmptyArray() {
        CSVRecord rec = createRecord(null, null, null, 1);
        assertEquals(0, rec.size());
    }
}