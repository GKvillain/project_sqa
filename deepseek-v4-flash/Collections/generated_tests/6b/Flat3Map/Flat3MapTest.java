package org.apache.commons.collections.map;

import static org.junit.Assert.*;
import org.junit.Test;
import org.apache.commons.collections.MapIterator;

public class Flat3MapTest {

    // Tests remove for size=3, removing first (non-null) key returns correct value
    @Test
    public void testRemove_size3_removeFirstKey_returnsCorrectValue() {
        Flat3Map map = new Flat3Map();
        map.put("a", "1");
        map.put("b", "2");
        map.put("c", "3");
        Object removed = map.remove("a");
        assertEquals("1", removed);
        assertEquals(2, map.size());
        assertFalse(map.containsKey("a"));
        assertTrue(map.containsKey("b"));
        assertTrue(map.containsKey("c"));
    }

    // Tests remove for size=3, removing second (non-null) key returns correct value
    @Test
    public void testRemove_size3_removeSecondKey_returnsCorrectValue() {
        Flat3Map map = new Flat3Map();
        map.put("a", "1");
        map.put("b", "2");
        map.put("c", "3");
        Object removed = map.remove("b");
        assertEquals("2", removed);
        assertEquals(2, map.size());
        assertFalse(map.containsKey("b"));
        assertTrue(map.containsKey("a"));
        assertTrue(map.containsKey("c"));
    }

    // Tests remove for size=3, removing third (non-null) key returns correct value
    @Test
    public void testRemove_size3_removeThirdKey_returnsCorrectValue() {
        Flat3Map map = new Flat3Map();
        map.put("a", "1");
        map.put("b", "2");
        map.put("c", "3");
        Object removed = map.remove("c");
        assertEquals("3", removed);
        assertEquals(2, map.size());
        assertFalse(map.containsKey("c"));
        assertTrue(map.containsKey("a"));
        assertTrue(map.containsKey("b"));
    }

    // Tests remove for size=3, removing first null key returns correct value
    @Test
    public void testRemove_size3_removeFirstNullKey_returnsCorrectValue() {
        Flat3Map map = new Flat3Map();
        map.put(null, "null1");
        map.put("b", "2");
        map.put("c", "3");
        Object removed = map.remove(null);
        assertEquals("null1", removed);
        assertEquals(2, map.size());
        assertFalse(map.containsKey(null));
        assertTrue(map.containsKey("b"));
        assertTrue(map.containsKey("c"));
    }

    // Tests remove for size=3, removing second null key returns correct value
    @Test
    public void testRemove_size3_removeSecondNullKey_returnsCorrectValue() {
        Flat3Map map = new Flat3Map();
        map.put("a", "1");
        map.put(null, "null2");
        map.put("c", "3");
        Object removed = map.remove(null);
        assertEquals("null2", removed);
        assertEquals(2, map.size());
        assertFalse(map.containsKey(null));
        assertTrue(map.containsKey("a"));
        assertTrue(map.containsKey("c"));
    }

    // Tests remove for size=3, removing third null key returns correct value
    @Test
    public void testRemove_size3_removeThirdNullKey_returnsCorrectValue() {
        Flat3Map map = new Flat3Map();
        map.put("a", "1");
        map.put("b", "2");
        map.put(null, "null3");
        Object removed = map.remove(null);
        assertEquals("null3", removed);
        assertEquals(2, map.size());
        assertFalse(map.containsKey(null));
        assertTrue(map.containsKey("a"));
        assertTrue(map.containsKey("b"));
    }

    // Tests remove for size=2, removing first key returns correct value
    @Test
    public void testRemove_size2_removeFirstKey_returnsCorrectValue() {
        Flat3Map map = new Flat3Map();
        map.put("a", "1");
        map.put("b", "2");
        Object removed = map.remove("a");
        assertEquals("1", removed);
        assertEquals(1, map.size());
        assertFalse(map.containsKey("a"));
        assertTrue(map.containsKey("b"));
    }

    // Tests remove for size=2, removing second key returns correct value
    @Test
    public void testRemove_size2_removeSecondKey_returnsCorrectValue() {
        Flat3Map map = new Flat3Map();
        map.put("a", "1");
        map.put("b", "2");
        Object removed = map.remove("b");
        assertEquals("2", removed);
        assertEquals(1, map.size());
        assertFalse(map.containsKey("b"));
        assertTrue(map.containsKey("a"));
    }

    // Tests remove for size=1, removing only key returns correct value
    @Test
    public void testRemove_size1_removeOnlyKey_returnsCorrectValue() {
        Flat3Map map = new Flat3Map();
        map.put("a", "1");
        Object removed = map.remove("a");
        assertEquals("1", removed);
        assertEquals(0, map.size());
        assertTrue(map.isEmpty());
    }

    // Tests remove when key not present returns null
    @Test
    public void testRemove_keyNotFound_returnsNull() {
        Flat3Map map = new Flat3Map();
        map.put("a", "1");
        Object removed = map.remove("b");
        assertNull(removed);
        assertEquals(1, map.size());
    }

    // Tests remove in delegate mode (size > 3)
    @Test
    public void testRemove_delegateMode_removeWorks() {
        Flat3Map map = new Flat3Map();
        map.put("a", "1");
        map.put("b", "2");
        map.put("c", "3");
        map.put("d", "4");  // triggers conversion to delegate
        Object removed = map.remove("a");
        assertEquals("1", removed);
        assertEquals(3, map.size());
        assertFalse(map.containsKey("a"));
    }

    // Tests get with existing key
    @Test
    public void testGet_existingKey_returnsValue() {
        Flat3Map map = new Flat3Map();
        map.put("key", "value");
        assertEquals("value", map.get("key"));
    }

    // Tests get with null key
    @Test
    public void testGet_nullKey_returnsValue() {
        Flat3Map map = new Flat3Map();
        map.put(null, "nullval");
        assertEquals("nullval", map.get(null));
    }

    // Tests put that causes conversion to delegate mode
    @Test
    public void testPut_addOverSize_convertsToDelegate() {
        Flat3Map map = new Flat3Map();
        map.put("a", "1");
        map.put("b", "2");
        map.put("c", "3");
        map.put("d", "4");  // now delegate
        assertEquals(4, map.size());
        assertTrue(map.containsKey("a"));
        assertTrue(map.containsKey("b"));
        assertTrue(map.containsKey("c"));
        assertTrue(map.containsKey("d"));
        // verify delegate mode still works
        Object removed = map.remove("a");
        assertEquals("1", removed);
        assertEquals(3, map.size());
    }

    // Tests containsKey with existing key
    @Test
    public void testContainsKey_existingKey_returnsTrue() {
        Flat3Map map = new Flat3Map();
        map.put("a", "1");
        assertTrue(map.containsKey("a"));
    }

    // Tests containsKey with non-existing key
    @Test
    public void testContainsKey_nonExistingKey_returnsFalse() {
        Flat3Map map = new Flat3Map();
        map.put("a", "1");
        assertFalse(map.containsKey("b"));
    }

    // Tests clone of flat map equals original
    @Test
    public void testClone_flatMap_equalsOriginal() throws Exception {
        Flat3Map map = new Flat3Map();
        map.put("a", "1");
        map.put("b", "2");
        Flat3Map cloned = (Flat3Map) map.clone();
        assertEquals(map, cloned);
        assertEquals(map.size(), cloned.size());
        assertEquals("1", cloned.get("a"));
        assertEquals("2", cloned.get("b"));
    }

    // Tests equals between two equal flat maps
    @Test
    public void testEquals_equalMaps_returnsTrue() {
        Flat3Map map1 = new Flat3Map();
        map1.put("a", "1");
        map1.put("b", "2");
        Flat3Map map2 = new Flat3Map();
        map2.put("a", "1");
        map2.put("b", "2");
        assertTrue(map1.equals(map2));
    }

    // Tests equals between maps of different size
    @Test
    public void testEquals_differentSize_returnsFalse() {
        Flat3Map map1 = new Flat3Map();
        map1.put("a", "1");
        Flat3Map map2 = new Flat3Map();
        map2.put("a", "1");
        map2.put("b", "2");
        assertFalse(map1.equals(map2));
    }

    // Tests mapIterator on flat map
    @Test
    public void testMapIterator_flatMap_iteratesCorrectly() {
        Flat3Map map = new Flat3Map();
        map.put("a", "1");
        map.put("b", "2");
        MapIterator it = map.mapIterator();
        assertTrue(it.hasNext());
        it.next(); // first key
        assertEquals("1", it.getValue());
        it.setValue("11");
        it.next(); // second key
        assertEquals("2", it.getValue());
        it.setValue("22");
        assertFalse(it.hasNext());
        assertEquals("11", map.get("a"));
        assertEquals("22", map.get("b"));
    }

    // ========== NEW TEST CASES FOR ADDITIONAL COVERAGE ==========

    // Tests isEmpty on empty map
    @Test
    public void testIsEmpty_emptyMap_returnsTrue() {
        Flat3Map map = new Flat3Map();
        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
    }

    // Tests isEmpty on non-empty map
    @Test
    public void testIsEmpty_nonEmptyMap_returnsFalse() {
        Flat3Map map = new Flat3Map();
        map.put("a", "1");
        assertFalse(map.isEmpty());
    }

    // Tests putAll merges entries from another map
    @Test
    public void testPutAll_mergesEntriesCorrectly() {
        Flat3Map map = new Flat3Map();
        map.put("a", "1");
        java.util.Map<String, String> other = new java.util.HashMap<>();
        other.put("b", "2");
        other.put("c", "3");
        map.putAll(other);
        assertEquals(3, map.size());
        assertEquals("1", map.get("a"));
        assertEquals("2", map.get("b"));
        assertEquals("3", map.get("c"));
    }

    // Tests putAll with empty map does not change size
    @Test
    public void testPutAll_emptyMap_doesNothing() {
        Flat3Map map = new Flat3Map();
        map.put("a", "1");
        map.putAll(new java.util.HashMap<>());
        assertEquals(1, map.size());
    }

    // Tests clear on flat map empties it
    @Test
    public void testClear_flatMap_emptiesMap() {
        Flat3Map map = new Flat3Map();
        map.put("a", "1");
        map.put("b", "2");
        map.clear();
        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
        assertNull(map.get("a"));
    }

    // Tests clear on delegate map empties it
    @Test
    public void testClear_delegateMap_emptiesMap() {
        Flat3Map map = new Flat3Map();
        map.put("a", "1");
        map.put("b", "2");
        map.put("c", "3");
        map.put("d", "4");
        map.clear();
        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
    }

    // Tests toString returns correct format for flat map
    @Test
    public void testToString_flatMap_returnsCorrectFormat() {
        Flat3Map map = new Flat3Map();
        map.put("a", "1");
        String str = map.toString();
        assertTrue(str.contains("a") && str.contains("1"));
    }

    // Tests containsValue with existing value
    @Test
    public void testContainsValue_existingValue_returnsTrue() {
        Flat3Map map = new Flat3Map();
        map.put("a", "1");
        assertTrue(map.containsValue("1"));
    }

    // Tests containsValue with non-existing value
    @Test
    public void testContainsValue_nonExistingValue_returnsFalse() {
        Flat3Map map = new Flat3Map();
        map.put("a", "1");
        assertFalse(map.containsValue("2"));
    }

    // Tests containsValue with null value
    @Test
    public void testContainsValue_nullValue_returnsTrue() {
        Flat3Map map = new Flat3Map();
        map.put("a", null);
        assertTrue(map.containsValue(null));
    }

    // Tests equals with null returns false
    @Test
    public void testEquals_null_returnsFalse() {
        Flat3Map map = new Flat3Map();
        map.put("a", "1");
        assertFalse(map.equals(null));
    }

    // Tests hashCode is consistent between equal maps
    @Test
    public void testHashCode_equalMaps_equalHashCode() {
        Flat3Map map1 = new Flat3Map();
        map1.put("a", "1");
        map1.put("b", "2");
        Flat3Map map2 = new Flat3Map();
        map2.put("a", "1");
        map2.put("b", "2");
        assertEquals(map1.hashCode(), map2.hashCode());
    }

    // Tests keySet returns all keys
    @Test
    public void testKeySet_flatMap_returnsAllKeys() {
        Flat3Map map = new Flat3Map();
        map.put("a", "1");
        map.put("b", "2");
        java.util.Set<Object> keys = map.keySet();
        assertEquals(2, keys.size());
        assertTrue(keys.contains("a"));
        assertTrue(keys.contains("b"));
    }

    // Tests values returns all values
    @Test
    public void testValues_flatMap_returnsAllValues() {
        Flat3Map map = new Flat3Map();
        map.put("a", "1");
        map.put("b", "2");
        java.util.Collection<Object> values = map.values();
        assertEquals(2, values.size());
        assertTrue(values.contains("1"));
        assertTrue(values.contains("2"));
    }

    // Tests entrySet returns all entries
    @Test
    public void testEntrySet_flatMap_returnsAllEntries() {
        Flat3Map map = new Flat3Map();
        map.put("a", "1");
        map.put("b", "2");
        java.util.Set<java.util.Map.Entry<Object, Object>> entries = map.entrySet();
        assertEquals(2, entries.size());
    }

    // Tests remove on empty map returns null
    @Test
    public void testRemove_emptyMap_returnsNull() {
        Flat3Map map = new Flat3Map();
        assertNull(map.remove("a"));
        assertEquals(0, map.size());
    }

    // Tests remove with null key on empty map returns null
    @Test
    public void testRemove_nullKeyEmptyMap_returnsNull() {
        Flat3Map map = new Flat3Map();
        assertNull(map.remove(null));
        assertEquals(0, map.size());
    }
}