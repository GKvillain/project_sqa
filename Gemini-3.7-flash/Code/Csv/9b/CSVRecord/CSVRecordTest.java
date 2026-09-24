package org.apache.commons.csv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class CSVRecordTest {

    private enum Header {
        FIRST, SECOND, THIRD
    }

    private String[] values;
    private Map<String, Integer> headerMap;
    private CSVRecord record;

    @Before
    public void setUp() {
        values = new String[] {"A", "B", "C"};
        headerMap = new HashMap<String, Integer>();
        headerMap.put("FIRST", 0);
        headerMap.put("SECOND", 1);
        headerMap.put("THIRD", 2);
        record = new CSVRecord(values, headerMap, "test comment", 1L);
    }

    // Tests toMap when mapping is null (should return empty map without NPE)
    @Test
    public void testToMap_nullMapping_returnsEmptyMap() {
        final CSVRecord recordNoHeader = new CSVRecord(values, null, null, 0L);
        final Map<String, String> map = recordNoHeader.toMap();
        assertNotNull(map);
        assertTrue(map.isEmpty());
    }

    // Tests toMap with valid mapping
    @Test
    public void testToMap_validMapping_returnsPopulatedMap() {
        final Map<String, String> map = record.toMap();
        assertEquals(3, map.size());
        assertEquals("A", map.get("FIRST"));
        assertEquals("B", map.get("SECOND"));
        assertEquals("C", map.get("THIRD"));
    }

    // Tests toMap when header index exceeds values length
    @Test
    public void testToMap_headerIndexOutOfBounds_excludesMissingEntries() {
        final Map<String, Integer> shortHeaderMap = new HashMap<String, Integer>();
        shortHeaderMap.put("FIRST", 0);
        shortHeaderMap.put("FOURTH", 3);
        final CSVRecord shortRecord = new CSVRecord(values, shortHeaderMap, null, 1L);
        final Map<String, String> map = shortRecord.toMap();
        assertEquals(1, map.size());
        assertEquals("A", map.get("FIRST"));
        assertFalse(map.containsKey("FOURTH"));
    }

    // Tests get by string header name
    @Test
    public void testGetString_validName_returnsValue() {
        assertEquals("A", record.get("FIRST"));
        assertEquals("B", record.get("SECOND"));
        assertEquals("C", record.get("THIRD"));
    }

    // Tests get by string name when mapping is null
    @Test(expected = IllegalStateException.class)
    public void testGetString_nullMapping_throwsIllegalStateException() {
        final CSVRecord recordNoHeader = new CSVRecord(values, null, null, 0L);
        recordNoHeader.get("FIRST");
    }

    // Tests get by string name when header is not mapped
    @Test(expected = IllegalArgumentException.class)
    public void testGetString_unmappedName_throwsIllegalArgumentException() {
        record.get("UNKNOWN");
    }

    // Tests get by string name when index points outside values array
    @Test(expected = IllegalArgumentException.class)
    public void testGetString_indexOutOfBounds_throwsIllegalArgumentException() {
        final Map<String, Integer> invalidMap = new HashMap<String, Integer>();
        invalidMap.put("OUT_OF_BOUNDS", 10);
        final CSVRecord recordWithInvalidMap = new CSVRecord(values, invalidMap, null, 1L);
        recordWithInvalidMap.get("OUT_OF_BOUNDS");
    }

    // Tests get by enum
    @Test
    public void testGetEnum_validEnum_returnsValue() {
        assertEquals("A", record.get(Header.FIRST));
        assertEquals("B", record.get(Header.SECOND));
    }

    // Tests get by integer index
    @Test
    public void testGetInt_validIndex_returnsValue() {
        assertEquals("A", record.get(0));
        assertEquals("B", record.get(1));
        assertEquals("C", record.get(2));
    }

    // Tests get by invalid negative index
    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testGetInt_negativeIndex_throwsArrayIndexOutOfBoundsException() {
        record.get(-1);
    }

    // Tests isConsistent when mapping is null
    @Test
    public void testIsConsistent_nullMapping_returnsTrue() {
        final CSVRecord recordNoHeader = new CSVRecord(values, null, null, 0L);
        assertTrue(recordNoHeader.isConsistent());
    }

    // Tests isConsistent when mapping size matches values size
    @Test
    public void testIsConsistent_matchingSize_returnsTrue() {
        assertTrue(record.isConsistent());
    }

    // Tests isConsistent when mapping size differs from values size
    @Test
    public void testIsConsistent_differentSize_returnsFalse() {
        final Map<String, Integer> mismatchedMap = new HashMap<String, Integer>();
        mismatchedMap.put("FIRST", 0);
        final CSVRecord inconsistentRecord = new CSVRecord(values, mismatchedMap, null, 1L);
        assertFalse(inconsistentRecord.isConsistent());
    }

    // Tests isMapped with null and non-null mapping
    @Test
    public void testIsMapped_variousConditions_returnsCorrectBoolean() {
        assertTrue(record.isMapped("FIRST"));
        assertFalse(record.isMapped("NON_EXISTENT"));

        final CSVRecord recordNoHeader = new CSVRecord(values, null, null, 0L);
        assertFalse(recordNoHeader.isMapped("FIRST"));
    }

    // Tests isSet when mapped and index is within bounds
    @Test
    public void testIsSet_mappedAndValidIndex_returnsTrue() {
        assertTrue(record.isSet("FIRST"));
        assertTrue(record.isSet("SECOND"));
        assertTrue(record.isSet("THIRD"));
    }

    // Tests isSet when mapped but index is out of bounds
    @Test
    public void testIsSet_mappedAndOutOfBoundsIndex_returnsFalse() {
        final Map<String, Integer> extraMap = new HashMap<String, Integer>();
        extraMap.put("FOURTH", 3);
        final CSVRecord customRecord = new CSVRecord(values, extraMap, null, 1L);
        assertFalse(customRecord.isSet("FOURTH"));
    }

    // Tests isSet when column is not mapped
    @Test
    public void testIsSet_unmappedColumn_returnsFalse() {
        assertFalse(record.isSet("NON_EXISTENT"));
    }

    // Tests iterator over values
    @Test
    public void testIterator_iteratesAllValues_correctSequence() {
        final Iterator<String> iterator = record.iterator();
        assertTrue(iterator.hasNext());
        assertEquals("A", iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals("B", iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals("C", iterator.next());
        assertFalse(iterator.hasNext());
    }

    // Tests size, getComment, and getRecordNumber getters
    @Test
    public void testGetters_validRecord_returnsExpectedMetadata() {
        assertEquals(3, record.size());
        assertEquals("test comment", record.getComment());
        assertEquals(1L, record.getRecordNumber());
    }

    // Tests constructor with null values array
    @Test
    public void testConstructor_nullValues_initializesEmptyArray() {
        final CSVRecord emptyRecord = new CSVRecord(null, null, null, 0L);
        assertEquals(0, emptyRecord.size());
        assertNull(emptyRecord.getComment());
        assertEquals(0L, emptyRecord.getRecordNumber());
        assertEquals("[]", emptyRecord.toString());
    }

    // Tests toString method
    @Test
    public void testToString_validValues_returnsArrayStringRepresentation() {
        assertEquals("[A, B, C]", record.toString());
    }
}