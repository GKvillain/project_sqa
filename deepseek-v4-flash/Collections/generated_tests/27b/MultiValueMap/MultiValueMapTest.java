package org.apache.commons.collections4.map;

import static org.junit.Assert.*;
import java.util.*;
import org.junit.Test;

public class MultiValueMapTest {

    // Helper to create a fresh MultiValueMap with default ArrayList
    private MultiValueMap<String, String> createMap() {
        return new MultiValueMap<String, String>();
    }

    // Tests normal put with a new key
    @Test
    public void testPut_normalValue_shouldAddValue() {
        MultiValueMap<String, String> map = createMap();
        Object result = map.put("key1", "value1");
        assertEquals("value1", result);
        assertEquals(1, map.size());
        Collection<String> coll = map.getCollection("key1");
        assertNotNull(coll);
        assertEquals(1, coll.size());
        assertTrue(coll.contains("value1"));
    }

    // Tests put with an existing key should add to collection
    @Test
    public void testPut_existingKey_shouldAddSecondValue() {
        MultiValueMap<String, String> map = createMap();
        map.put("key1", "value1");
        Object result = map.put("key1", "value2");
        assertEquals("value2", result);
        assertEquals(1, map.size());
        Collection<String> coll = map.getCollection("key1");
        assertEquals(2, coll.size());
        assertTrue(coll.contains("value1"));
        assertTrue(coll.contains("value2"));
    }

    // Tests put with null value (should be allowed)
    @Test
    public void testPut_nullValue_shouldAddNull() {
        MultiValueMap<String, String> map = createMap();
        map.put("key1", null);
        assertEquals(1, map.size());
        Collection<String> coll = map.getCollection("key1");
        assertNotNull(coll);
        assertEquals(1, coll.size());
        assertTrue(coll.contains(null));
    }

    // Tests put with null key (HashMap supports null key)
    @Test
    public void testPut_nullKey_shouldWork() {
        MultiValueMap<String, String> map = createMap();
        map.put(null, "value");
        assertEquals(1, map.size());
        Collection<String> coll = map.getCollection(null);
        assertNotNull(coll);
        assertEquals(1, coll.size());
        assertTrue(coll.contains("value"));
    }

    // Tests removeMapping for an existing value
    @Test
    public void testRemoveMapping_existingValue_shouldSucceed() {
        MultiValueMap<String, String> map = createMap();
        map.put("key1", "value1");
        map.put("key1", "value2");
        assertTrue(map.removeMapping("key1", "value1"));
        Collection<String> coll = map.getCollection("key1");
        assertNotNull(coll);
        assertEquals(1, coll.size());
        assertTrue(coll.contains("value2"));
        assertEquals(1, map.size()); // key still present
    }

    // Tests removeMapping of the last value should remove the key
    @Test
    public void testRemoveMapping_lastValue_shouldRemoveKey() {
        MultiValueMap<String, String> map = createMap();
        map.put("key1", "value1");
        assertTrue(map.removeMapping("key1", "value1"));
        assertNull(map.getCollection("key1"));
        assertEquals(0, map.size());
    }

    // Tests removeMapping with non-existent key returns false
    @Test
    public void testRemoveMapping_nonExistentKey_shouldReturnFalse() {
        MultiValueMap<String, String> map = createMap();
        assertFalse(map.removeMapping("nonexistent", "value"));
    }

    // Tests removeMapping with non-existent value returns false
    @Test
    public void testRemoveMapping_nonExistentValue_shouldReturnFalse() {
        MultiValueMap<String, String> map = createMap();
        map.put("key1", "value1");
        assertFalse(map.removeMapping("key1", "otherValue"));
        assertEquals(1, map.size());
    }

    // Tests removeMapping with null value
    @Test
    public void testRemoveMapping_nullValue_shouldWorkIfPresent() {
        MultiValueMap<String, String> map = createMap();
        map.put("key1", null);
        map.put("key1", "value");
        assertTrue(map.removeMapping("key1", null));
        Collection<String> coll = map.getCollection("key1");
        assertEquals(1, coll.size());
        assertTrue(coll.contains("value"));
    }

    // Tests putAll with a normal Map (non-MultiMap)
    @Test
    public void testPutAll_normalMap_shouldAddAllEntries() {
        MultiValueMap<String, String> map = createMap();
        Map<String, String> normalMap = new HashMap<String, String>();
        normalMap.put("keyA", "valueA");
        normalMap.put("keyB", "valueB");
        map.putAll(normalMap);
        assertEquals(2, map.size());
        assertEquals(1, map.getCollection("keyA").size());
        assertEquals(1, map.getCollection("keyB").size());
    }

    // Tests putAll with a MultiValueMap (MultiMap branch)
    @Test
    public void testPutAll_multiValueMap_shouldMergeCorrectly() {
        MultiValueMap<String, String> source = createMap();
        source.put("key1", "v1");
        source.put("key1", "v2");
        source.put("key2", "v3");

        MultiValueMap<String, String> target = createMap();
        target.put("key1", "existing");
        target.putAll(source);

        assertEquals(2, target.size());
        Collection<String> coll1 = target.getCollection("key1");
        assertTrue(coll1.contains("existing"));
        assertTrue(coll1.contains("v1"));
        assertTrue(coll1.contains("v2"));
        assertEquals(3, coll1.size());

        Collection<String> coll2 = target.getCollection("key2");
        assertNotNull(coll2);
        assertEquals(1, coll2.size());
        assertTrue(coll2.contains("v3"));
    }

    // Tests putAll with key and empty collection returns false
    @Test
    public void testPutAll_keyCollection_emptyCollection_shouldReturnFalse() {
        MultiValueMap<String, String> map = createMap();
        assertFalse(map.putAll("key", new ArrayList<String>()));
        assertEquals(0, map.size());
    }

    // Tests putAll with key and non-empty collection
    @Test
    public void testPutAll_keyCollection_nonEmpty_shouldAddValues() {
        MultiValueMap<String, String> map = createMap();
        List<String> values = Arrays.asList("a", "b", "c");
        assertTrue(map.putAll("key", values));
        assertEquals(1, map.size());
        Collection<String> coll = map.getCollection("key");
        assertEquals(3, coll.size());
        assertTrue(coll.containsAll(values));
    }

    // Tests containsValue overall (value present)
    @Test
    public void testContainsValue_valuePresent_shouldReturnTrue() {
        MultiValueMap<String, String> map = createMap();
        map.put("key1", "value1");
        map.put("key1", "value2");
        assertTrue(map.containsValue("value1"));
        assertTrue(map.containsValue("value2"));
    }

    // Tests containsValue overall (value not present)
    @Test
    public void testContainsValue_valueNotPresent_shouldReturnFalse() {
        MultiValueMap<String, String> map = createMap();
        map.put("key1", "value1");
        assertFalse(map.containsValue("other"));
    }

    // Tests containsValue with key and value
    @Test
    public void testContainsValue_keyAndValue_shouldWork() {
        MultiValueMap<String, String> map = createMap();
        map.put("key1", "value1");
        map.put("key2", "value2");
        assertTrue(map.containsValue("key1", "value1"));
        assertFalse(map.containsValue("key1", "value2"));
        assertFalse(map.containsValue("nonexistent", "value1"));
    }

    // Tests totalSize
    @Test
    public void testTotalSize_afterMultiplePuts_shouldSum() {
        MultiValueMap<String, String> map = createMap();
        map.put("key1", "a");
        map.put("key1", "b");
        map.put("key2", "c");
        assertEquals(3, map.totalSize());
    }

    // Tests clear
    @Test
    public void testClear_shouldRemoveAllMappings() {
        MultiValueMap<String, String> map = createMap();
        map.put("key1", "v1");
        map.put("key2", "v2");
        map.clear();
        assertEquals(0, map.size());
        assertEquals(0, map.totalSize());
        assertNull(map.getCollection("key1"));
    }

    // Tests values() collection
    @Test
    public void testValues_shouldReturnAllValues() {
        MultiValueMap<String, String> map = createMap();
        map.put("key1", "v1");
        map.put("key1", "v2");
        map.put("key2", "v3");

        Collection<Object> allValues = map.values();
        assertEquals(3, allValues.size());
        assertTrue(allValues.contains("v1"));
        assertTrue(allValues.contains("v2"));
        assertTrue(allValues.contains("v3"));
    }

    // Additional test: removeMapping on key that was removed after last value
    @Test
    public void testRemoveMapping_afterKeyRemoved_shouldReturnFalse() {
        MultiValueMap<String, String> map = createMap();
        map.put("key1", "v1");
        map.removeMapping("key1", "v1"); // key removed
        assertFalse(map.removeMapping("key1", "v1"));
    }

    // ==============================
    // New tests for uncovered areas
    // ==============================

    // Test default constructor
    @Test
    public void testDefaultConstructor_shouldCreateEmptyMap() {
        MultiValueMap<String, String> map = createMap();
        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
    }

    // Test constructor with initial map
    @Test
    public void testConstructorWithInitialMap_shouldCopyEntries() {
        Map<String, String> initial = new HashMap<>();
        initial.put("k1", "v1");
        initial.put("k2", "v2");
        MultiValueMap<String, String> map = new MultiValueMap<>(initial);
        assertEquals(2, map.size());
        assertTrue(map.containsValue("k1", "v1"));
        assertTrue(map.containsValue("k2", "v2"));
    }

    // Test constructor with null initial map should throw
    @Test(expected = NullPointerException.class)
    public void testConstructorWithNullInitialMap_shouldThrow() {
        new MultiValueMap<String, String>(null);
    }

    // Test clone() method
    @Test
    public void testClone_shouldReturnShallowCopy() {
        MultiValueMap<String, String> map = createMap();
        map.put("key", "value");
        @SuppressWarnings("unchecked")
        MultiValueMap<String, String> cloned = (MultiValueMap<String, String>) map.clone();
        assertNotSame(map, cloned);
        assertEquals(map.size(), cloned.size());
        assertTrue(cloned.containsValue("key", "value"));
        // Modify original, cloned should not be affected
        map.put("key", "value2");
        assertEquals(1, cloned.size());
        assertFalse(cloned.containsValue("key", "value2"));
    }

    // Test equals() and hashCode()
    @Test
    public void testEqualsAndHashCode() {
        MultiValueMap<String, String> map1 = createMap();
        map1.put("key", "value");
        map1.put("key", "value2");

        MultiValueMap<String, String> map2 = createMap();
        map2.put("key", "value2");
        map2.put("key", "value");

        // order of values should not matter
        assertEquals(map1, map2);
        assertEquals(map1.hashCode(), map2.hashCode());

        // null key/value handling
        MultiValueMap<String, String> map3 = createMap();
        map3.put(null, null);
        assertFalse(map1.equals(map3));
        assertFalse(map1.hashCode() == map3.hashCode());

        // self equality
        assertEquals(map1, map1);

        // not equal to different type
        assertFalse(map1.equals("string"));
    }

    // Test toString()
    @Test
    public void testToString_shouldReturnStringRepresentation() {
        MultiValueMap<String, String> map = createMap();
        map.put("key1", "value1");
        String str = map.toString();
        assertNotNull(str);
        assertTrue(str.contains("key1"));
        assertTrue(str.contains("value1"));
    }

    // Test isEmpty()
    @Test
    public void testIsEmpty_shouldWorkCorrectly() {
        MultiValueMap<String, String> map = createMap();
        assertTrue(map.isEmpty());
        map.put("key", "value");
        assertFalse(map.isEmpty());
        map.removeMapping("key", "value");
        assertTrue(map.isEmpty());
    }

    // Test keySet()
    @Test
    public void testKeySet_shouldReturnAllKeys() {
        MultiValueMap<String, String> map = createMap();
        map.put("k1", "v1");
        map.put("k2", "v2");
        map.put("k1", "v3"); // duplicate key
        Set<String> keys = map.keySet();
        assertEquals(2, keys.size());
        assertTrue(keys.contains("k1"));
        assertTrue(keys.contains("k2"));
    }

    // Test entrySet()
    @Test
    public void testEntrySet_shouldReturnAllEntries() {
        MultiValueMap<String, String> map = createMap();
        map.put("k1", "v1");
        map.put("k1", "v2");
        map.put("k2", "v3");
        Set<Map.Entry<String, String>> entries = map.entrySet();
        assertEquals(3, entries.size());
    }

    // Test containsKey
    @Test
    public void testContainsKey_shouldWork() {
        MultiValueMap<String, String> map = createMap();
        map.put("key", "value");
        assertTrue(map.containsKey("key"));
        assertFalse(map.containsKey("nonexistent"));
        assertTrue(map.containsKey(null)); // null key support
    }

    // Test getCollection for existing key returns modifiable collection
    @Test
    public void testGetCollection_shouldReturnModifiableCollection() {
        MultiValueMap<String, String> map = createMap();
        map.put("key", "v1");
        Collection<String> coll = map.getCollection("key");
        assertNotNull(coll);
        int originalSize = coll.size();
        coll.add("v2"); // modify directly
        assertEquals(originalSize + 1, map.totalSize());
    }

    // Test remove(Object key)
    @Test
    public void testRemove_shouldRemoveAllValuesForThatKey() {
        MultiValueMap<String, String> map = createMap();
        map.put("k1", "v1");
        map.put("k1", "v2");
        map.put("k2", "v3");
        @SuppressWarnings("unchecked")
        Collection<String> removed = (Collection<String>) map.remove("k1");
        assertNotNull(removed);
        assertEquals(2, removed.size());
        assertEquals(1, map.size());
        assertNull(map.getCollection("k1"));
    }

    // Test remove with non-existent key returns null
    @Test
    public void testRemove_nonExistentKey_shouldReturnNull() {
        MultiValueMap<String, String> map = createMap();
        assertNull(map.remove("nonexistent"));
    }

    // Test putAll with null map throws
    @Test(expected = NullPointerException.class)
    public void testPutAll_nullMap_shouldThrow() {
        MultiValueMap<String, String> map = createMap();
        map.putAll((Map<String, String>) null);
    }

    // Test putAll with null key and collection throws
    @Test(expected = NullPointerException.class)
    public void testPutAll_nullKeyCollection_shouldThrow() {
        MultiValueMap<String, String> map = createMap();
        map.putAll(null, new ArrayList<String>());
    }
}