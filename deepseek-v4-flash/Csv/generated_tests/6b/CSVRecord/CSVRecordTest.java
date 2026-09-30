package org.apache.commons.csv;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

public class CSVRecordTest {

    private enum TestEnum {
        col0, col1
    }

    @Test
    public void testGetByIndex_validIndex_returnsValue() {
        CSVRecord record = createRecord(new String[]{"a", "b"}, createMapping("col0", 0, "col1", 1), "comment", 1L);
        assertEquals("a", record.get(0));
        assertEquals("b", record.get(1));
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testGetByIndex_outOfBounds_throwsArrayIndexOutOfBoundsException() {
        CSVRecord record = createRecord(new String[]{"a"}, null, null, 1L);
        record.get(5);
    }

    @Test
    public void testGetByEnum_validEnum_returnsValue() {
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("col0", 0);
        mapping.put("col1", 1);
        CSVRecord record = createRecord(new String[]{"x", "y"}, mapping, null, 1L);
        assertEquals("x", record.get(TestEnum.col0));
        assertEquals("y", record.get(TestEnum.col1));
    }

    @Test(expected = IllegalStateException.class)
    public void testGetByName_mappingNull_throwsIllegalStateException() {
        CSVRecord record = createRecord(new String[]{"a"}, null, null, 1L);
        record.get("name");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetByName_nameNotFound_throwsIllegalArgumentException() {
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("col", 0);
        CSVRecord record = createRecord(new String[]{"a"}, mapping, null, 1L);
        record.get("unknown");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetByName_indexOutOfBounds_throwsIllegalArgumentException() {
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("col", 5);
        CSVRecord record = createRecord(new String[]{"a"}, mapping, null, 1L);
        record.get("col");
    }

    @Test
    public void testGetByName_normal_returnsValue() {
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("name", 0);
        CSVRecord record = createRecord(new String[]{"value"}, mapping, null, 1L);
        assertEquals("value", record.get("name"));
    }

    @Test
    public void testIsConsistent_mappingNull_returnsTrue() {
        CSVRecord record = createRecord(new String[]{"a", "b"}, null, null, 1L);
        assertTrue(record.isConsistent());
    }

    @Test
    public void testIsConsistent_mappingSizeEqualsValues_returnsTrue() {
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("c0", 0);
        mapping.put("c1", 1);
        CSVRecord record = createRecord(new String[]{"a", "b"}, mapping, null, 1L);
        assertTrue(record.isConsistent());
    }

    @Test
    public void testIsConsistent_mappingSizeNotEqualsValues_returnsFalse() {
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("c0", 0);
        CSVRecord record = createRecord(new String[]{"a", "b"}, mapping, null, 1L);
        assertFalse(record.isConsistent());
    }

    @Test
    public void testIsMapped_mappingNull_returnsFalse() {
        CSVRecord record = createRecord(new String[]{}, null, null, 1L);
        assertFalse(record.isMapped("any"));
    }

    @Test
    public void testIsMapped_nameInMapping_returnsTrue() {
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("col", 0);
        CSVRecord record = createRecord(new String[]{"a"}, mapping, null, 1L);
        assertTrue(record.isMapped("col"));
    }

    @Test
    public void testIsMapped_nameNotInMapping_returnsFalse() {
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("col", 0);
        CSVRecord record = createRecord(new String[]{"a"}, mapping, null, 1L);
        assertFalse(record.isMapped("other"));
    }

    @Test
    public void testIsSet_mappedAndIndexWithinBounds_returnsTrue() {
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("col", 0);
        CSVRecord record = createRecord(new String[]{"val"}, mapping, null, 1L);
        assertTrue(record.isSet("col"));
    }

    @Test
    public void testIsSet_mappedButIndexOutOfBounds_returnsFalse() {
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("col", 5);
        CSVRecord record = createRecord(new String[]{"val"}, mapping, null, 1L);
        assertFalse(record.isSet("col"));
    }

    @Test
    public void testIsSet_notMapped_returnsFalse() {
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("col", 0);
        CSVRecord record = createRecord(new String[]{"val"}, mapping, null, 1L);
        assertFalse(record.isSet("other"));
    }

    @Test
    public void testToMap_normal_returnsCorrectMap() {
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("col0", 0);
        mapping.put("col1", 1);
        CSVRecord record = createRecord(new String[]{"a", "b"}, mapping, null, 1L);
        Map<String, String> result = record.toMap();
        Map<String, String> expected = new HashMap<>();
        expected.put("col0", "a");
        expected.put("col1", "b");
        assertEquals(expected, result);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testToMap_indexOutOfBounds_throwsArrayIndexOutOfBoundsException() {
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("col", 10);
        CSVRecord record = createRecord(new String[]{"a"}, mapping, null, 1L);
        record.toMap();
    }

    @Test
    public void testSize_returnsValuesLength() {
        CSVRecord record = createRecord(new String[]{"a", "b", "c"}, null, "comment", 1L);
        assertEquals(3, record.size());
    }

    @Test
    public void testSize_emptyRecord_returnsZero() {
        CSVRecord record = createRecord(new String[]{}, null, null, 1L);
        assertEquals(0, record.size());
    }

    @Test
    public void testGetComment_returnsComment() {
        CSVRecord record = createRecord(new String[]{}, null, "my comment", 1L);
        assertEquals("my comment", record.getComment());
    }

    @Test
    public void testGetRecordNumber_returnsNumber() {
        CSVRecord record = createRecord(new String[]{}, null, null, 42L);
        assertEquals(42L, record.getRecordNumber());
    }

    @Test
    public void testIterator_returnsAllValues() {
        CSVRecord record = createRecord(new String[]{"x", "y", "z"}, null, null, 1L);
        Iterator<String> iter = record.iterator();
        assertTrue(iter.hasNext());
        assertEquals("x", iter.next());
        assertEquals("y", iter.next());
        assertEquals("z", iter.next());
        assertFalse(iter.hasNext());
    }

    @Test
    public void testToString_returnsArrayRepresentation() {
        CSVRecord record = createRecord(new String[]{"a", "b"}, null, null, 1L);
        assertEquals("[a, b]", record.toString());
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testGetByIndex_negativeIndex_throwsArrayIndexOutOfBoundsException() {
        CSVRecord record = createRecord(new String[]{"a"}, null, null, 1L);
        record.get(-1);
    }

    @Test(expected = IllegalStateException.class)
    public void testGetByEnum_mappingNull_throwsIllegalStateException() {
        CSVRecord record = createRecord(new String[]{"a"}, null, null, 1L);
        record.get(TestEnum.col0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetByEnum_nameNotFound_throwsIllegalArgumentException() {
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("col0", 0);
        CSVRecord record = createRecord(new String[]{"a"}, mapping, null, 1L);
        record.get(TestEnum.col1);
    }

    @Test
    public void testIsSet_mappingNull_returnsFalse() {
        CSVRecord record = createRecord(new String[]{"a"}, null, null, 1L);
        assertFalse(record.isSet("any"));
    }

    @Test
    public void testIsSet_indexNegative_returnsFalse() {
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("col", -1);
        CSVRecord record = createRecord(new String[]{"a"}, mapping, null, 1L);
        assertFalse(record.isSet("col"));
    }

    @Test
    public void testIterator_emptyRecord_hasNextFalse() {
        CSVRecord record = createRecord(new String[]{}, null, null, 1L);
        Iterator<String> iter = record.iterator();
        assertFalse(iter.hasNext());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testIterator_remove_throwsUnsupportedOperationException() {
        CSVRecord record = createRecord(new String[]{"a"}, null, null, 1L);
        Iterator<String> iter = record.iterator();
        iter.next();
        iter.remove();
    }

    @Test
    public void testToMap_emptyMapping_returnsEmptyMap() {
        Map<String, Integer> mapping = new HashMap<>();
        CSVRecord record = createRecord(new String[]{"a"}, mapping, null, 1L);
        assertTrue(record.toMap().isEmpty());
    }

    @Test
    public void testToString_emptyRecord_returnsEmptyBrackets() {
        CSVRecord record = createRecord(new String[]{}, null, null, 1L);
        assertEquals("[]", record.toString());
    }

    @Test
    public void testGetComment_null_returnsNull() {
        CSVRecord record = createRecord(new String[]{}, null, null, 1L);
        assertNull(record.getComment());
    }

    @Test
    public void testConstructor_nullValues_createsEmptyRecord() {
        CSVRecord record = new CSVRecord(null, null, null, 1L);
        assertEquals(0, record.size());
        assertFalse(record.iterator().hasNext());
    }

    private CSVRecord createRecord(String[] values, Map<String, Integer> mapping, String comment, long recordNumber) {
        return new CSVRecord(values, mapping, comment, recordNumber);
    }

    private Map<String, Integer> createMapping(Object... pairs) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            map.put((String) pairs[i], Integer.parseInt(pairs[i + 1].toString()));
        }
        return map;
    }
}