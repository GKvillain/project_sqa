package org.apache.commons.csv;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class CSVRecordTest {

    // Helper to create a CSVRecord with given values and mapping
    private CSVRecord createRecord(String[] values, Map<String, Integer> mapping, String comment, long recordNumber) {
        return new CSVRecord(values, mapping, comment, recordNumber);
    }

    // Helper to create a mapping map from header names to indices
    private Map<String, Integer> createMapping(String... headers) {
        Map<String, Integer> mapping = new HashMap<String, Integer>();
        for (int i = 0; i < headers.length; i++) {
            mapping.put(headers[i], i);
        }
        return mapping;
    }

    // Tests get(int) with valid index
    @Test
    public void testGetInt_validIndex_returnsValue() {
        CSVRecord record = createRecord(new String[] {"a", "b", "c"}, null, null, 1);
        assertEquals("a", record.get(0));
        assertEquals("c", record.get(2));
    }

    // Tests get(int) with negative index throws exception
    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testGetInt_negativeIndex_throwsArrayIndexOutOfBounds() {
        CSVRecord record = createRecord(new String[] {"a"}, null, null, 1);
        record.get(-1);
    }

    // Tests get(int) with index equal to values length throws exception
    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testGetInt_indexEqualsLength_throwsArrayIndexOutOfBounds() {
        CSVRecord record = createRecord(new String[] {"a"}, null, null, 1);
        record.get(1);
    }

    // Tests get(Enum) delegates to get(String) with enum name
    @Test
    public void testGetEnum_validEnum_returnsValue() {
        Map<String, Integer> mapping = createMapping("COL1");
        CSVRecord record = createRecord(new String[] {"value1"}, mapping, null, 1);
        assertEquals("value1", record.get(MyEnum.COL1));
    }

    // Tests get(String) when mapping exists and index in bounds
    @Test
    public void testGetString_withMappingValidIndex_returnsValue() {
        Map<String, Integer> mapping = createMapping("name", "age");
        CSVRecord record = createRecord(new String[] {"Alice", "30"}, mapping, null, 1);
        assertEquals("Alice", record.get("name"));
        assertEquals("30", record.get("age"));
    }

    // Tests get(String) when mapping is null throws IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testGetString_nullMapping_throwsIllegalStateException() {
        CSVRecord record = createRecord(new String[] {"a"}, null, null, 1);
        record.get("x");
    }

    // Tests get(String) when name not in mapping throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testGetString_unmappedName_throwsIllegalArgumentException() {
        Map<String, Integer> mapping = createMapping("col0");
        CSVRecord record = createRecord(new String[] {"val"}, mapping, null, 1);
        record.get("unknown");
    }

    // Tests get(String) when mapped index is out of bounds throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testGetString_indexOutOfBounds_throwsIllegalArgumentException() {
        Map<String, Integer> mapping = new HashMap<String, Integer>();
        mapping.put("col", 5); // index 5 but values length = 2
        CSVRecord record = createRecord(new String[] {"a", "b"}, mapping, null, 1);
        record.get("col");
    }

    // Tests getComment with non-null comment
    @Test
    public void testGetComment_withComment_returnsComment() {
        CSVRecord record = createRecord(new String[] {"a"}, null, "this is a comment", 1);
        assertEquals("this is a comment", record.getComment());
    }

    // Tests getComment with null comment
    @Test
    public void testGetComment_nullComment_returnsNull() {
        CSVRecord record = createRecord(new String[] {"a"}, null, null, 1);
        assertNull(record.getComment());
    }

    // Tests getRecordNumber returns the record number passed to constructor
    @Test
    public void testGetRecordNumber_returnsRecordNumber() {
        CSVRecord record = createRecord(new String[] {"a"}, null, null, 42);
        assertEquals(42L, record.getRecordNumber());
    }

    // Tests isConsistent when mapping is null -> true
    @Test
    public void testIsConsistent_nullMapping_returnsTrue() {
        CSVRecord record = createRecord(new String[] {"a"}, null, null, 1);
        assertTrue(record.isConsistent());
    }

    // Tests isConsistent when mapping size equals values length -> true
    @Test
    public void testIsConsistent_mappingSizeEqualsValuesLength_returnsTrue() {
        Map<String, Integer> mapping = createMapping("h1", "h2");
        CSVRecord record = createRecord(new String[] {"v1", "v2"}, mapping, null, 1);
        assertTrue(record.isConsistent());
    }

    // Tests isConsistent when mapping size differs from values length -> false
    @Test
    public void testIsConsistent_mappingSizeNotEqualsValuesLength_returnsFalse() {
        Map<String, Integer> mapping = createMapping("h1");
        CSVRecord record = createRecord(new String[] {"v1", "v2"}, mapping, null, 1);
        assertFalse(record.isConsistent());
    }

    // Tests isMapped when mapping is null -> false
    @Test
    public void testIsMapped_nullMapping_returnsFalse() {
        CSVRecord record = createRecord(new String[] {"a"}, null, null, 1);
        assertFalse(record.isMapped("x"));
    }

    // Tests isMapped when name is in mapping -> true
    @Test
    public void testIsMapped_namePresent_returnsTrue() {
        Map<String, Integer> mapping = createMapping("col");
        CSVRecord record = createRecord(new String[] {"v"}, mapping, null, 1);
        assertTrue(record.isMapped("col"));
    }

    // Tests isMapped when name is not in mapping -> false
    @Test
    public void testIsMapped_nameNotPresent_returnsFalse() {
        Map<String, Integer> mapping = createMapping("col");
        CSVRecord record = createRecord(new String[] {"v"}, mapping, null, 1);
        assertFalse(record.isMapped("other"));
    }

    // Tests isSet when mapped and index within bounds -> true
    @Test
    public void testIsSet_mappedAndIndexInBounds_returnsTrue() {
        Map<String, Integer> mapping = createMapping("col");
        CSVRecord record = createRecord(new String[] {"v"}, mapping, null, 1);
        assertTrue(record.isSet("col"));
    }

    // Tests isSet when mapped but index out of bounds -> false
    @Test
    public void testIsSet_mappedIndexOutOfBounds_returnsFalse() {
        Map<String, Integer> mapping = new HashMap<String, Integer>();
        mapping.put("col", 5); // index 5 > values length 1
        CSVRecord record = createRecord(new String[] {"v"}, mapping, null, 1);
        assertFalse(record.isSet("col"));
    }

    // Tests isSet when not mapped -> false
    @Test
    public void testIsSet_notMapped_returnsFalse() {
        CSVRecord record = createRecord(new String[] {"v"}, null, null, 1);
        assertFalse(record.isSet("col"));
    }

    // Tests size returns the number of values
    @Test
    public void testSize_returnsValuesLength() {
        CSVRecord record = createRecord(new String[] {"a", "b"}, null, null, 1);
        assertEquals(2, record.size());
    }

    // Tests toMap returns map with only mapped columns that are in bounds
    @Test
    public void testToMap_returnsMapWithValidMappings() {
        Map<String, Integer> mapping = createMapping("h0", "h1", "h2");
        CSVRecord record = createRecord(new String[] {"v0", "v1", "v2"}, mapping, null, 1);
        Map<String, String> result = record.toMap();
        assertEquals(3, result.size());
        assertEquals("v0", result.get("h0"));
        assertEquals("v1", result.get("h1"));
        assertEquals("v2", result.get("h2"));
    }

    // Tests toMap skips columns with index out of bounds
    @Test
    public void testToMap_ignoresOutOfBoundIndex() {
        Map<String, Integer> mapping = new HashMap<String, Integer>();
        mapping.put("valid", 0);
        mapping.put("invalid", 5); // index 5 > values length 1
        CSVRecord record = createRecord(new String[] {"v0"}, mapping, null, 1);
        Map<String, String> result = record.toMap();
        assertEquals(1, result.size());
        assertEquals("v0", result.get("valid"));
        assertNull(result.get("invalid"));
    }

    // Tests that values() returns the internal array
    @Test
    public void testValues_returnsValuesArray() {
        String[] expected = {"a", "b"};
        CSVRecord record = createRecord(expected, null, null, 1);
        assertArrayEquals(expected, record.values());
    }

    // Tests toString returns Arrays.toString of values
    @Test
    public void testToString_usesArraysToString() {
        CSVRecord record = createRecord(new String[] {"x", "y"}, null, null, 1);
        assertEquals("[x, y]", record.toString());
    }

    // Tests iterator yields all values
    @Test
    public void testIterator_returnsAllValues() {
        CSVRecord record = createRecord(new String[] {"1", "2", "3"}, null, null, 1);
        int count = 0;
        for (String s : record) {
            assertNotNull(s);
            count++;
        }
        assertEquals(3, count);
    }

    // Enum used for get(Enum) test
    private enum MyEnum {
        COL1
    }
}