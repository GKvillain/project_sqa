package org.apache.commons.collections.map;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;

public class MultiValueMapTest {

    // Tests put with null key and non-null value
    @Test
    public void testPut_nullKeyNonCollectionValue_returnsValue() {
        MultiValueMap map = new MultiValueMap();
        Object result = map.put(null, "value1");
        assertNotNull("put(null, value) should return the value", result);
        assertEquals("value1", result);
    }

    // Tests put with non-null key and value (normal case)
    @Test
    public void testPut_normalKeyAndValue_returnsValue() {
        MultiValueMap map = new MultiValueMap();
        Object result = map.put("key1", "value1");
        assertEquals("value1", result);
    }

    // Tests put with same key and different value adds to collection
    @Test
    public void testPut_sameKeyDifferentValue_addsToCollection() {
        MultiValueMap map = new MultiValueMap();
        map.put("key", "value1");
        map.put("key", "value2");
        assertTrue(map.getCollection("key").contains("value1"));
        assertTrue(map.getCollection("key").contains("value2"));
        assertEquals(2, map.getCollection("key").size());
    }

    // Tests put with existing key and same value returns null (no change)
    @Test
    public void testPut_sameKeySameValue_returnsNull() {
        MultiValueMap map = new MultiValueMap();
        map.put("key", "value");
        Object result = map.put("key", "value");
        assertNull("put of same value should return null", result);
    }

    // Tests getCollection returns null for unmapped key
    @Test
    public void testGetCollection_unmappedKey_returnsNull() {
        MultiValueMap map = new MultiValueMap();
        assertNull(map.getCollection("nonexistent"));
    }

    // Tests getCollection returns correct collection after put
    @Test
    public void testGetCollection_mappedKey_returnsCollection() {
        MultiValueMap map = new MultiValueMap();
        map.put("key", "value");
        Collection coll = map.getCollection("key");
        assertNotNull(coll);
        assertEquals(1, coll.size());
        assertTrue(coll.contains("value"));
    }

    // Tests removeMapping removes value and key if collection becomes empty
    @Test
    public void testRemoveMapping_lastValue_removesKey() {
        MultiValueMap map = new MultiValueMap();
        map.put("key", "value");
        Object removed = map.removeMapping("key", "value");
        assertEquals("value", removed);
        assertFalse(map.containsKey("key"));
        assertNull(map.getCollection("key"));
    }

    // Tests removeMapping with non-existent key returns null
    @Test
    public void testRemoveMapping_nonExistentKey_returnsNull() {
        MultiValueMap map = new MultiValueMap();
        Object result = map.removeMapping("nonexistent", "value");
        assertNull(result);
    }

    // Tests removeMapping with existing key but non-existent value returns null
    @Test
    public void testRemoveMapping_existingKeyWrongValue_returnsNull() {
        MultiValueMap map = new MultiValueMap();
        map.put("key", "value1");
        Object result = map.removeMapping("key", "value2");
        assertNull(result);
    }

    // Tests removeMapping removes only one value when multiple exist
    @Test
    public void testRemoveMapping_oneOfMultipleValues_removesOnlyThatValue() {
        MultiValueMap map = new MultiValueMap();
        map.put("key", "value1");
        map.put("key", "value2");
        Object removed = map.removeMapping("key", "value1");
        assertEquals("value1", removed);
        assertTrue(map.containsKey("key"));
        Collection coll = map.getCollection("key");
        assertEquals(1, coll.size());
        assertTrue(coll.contains("value2"));
    }

    // Tests containsValue with value present in any key
    @Test
    public void testContainsValue_valuePresent_returnsTrue() {
        MultiValueMap map = new MultiValueMap();
        map.put("key1", "value1");
        map.put("key2", "value2");
        assertTrue(map.containsValue("value1"));
        assertTrue(map.containsValue("value2"));
    }

    // Tests containsValue with value not present
    @Test
    public void testContainsValue_valueNotPresent_returnsFalse() {
        MultiValueMap map = new MultiValueMap();
        map.put("key1", "value1");
        assertFalse(map.containsValue("nonexistent"));
    }

    // Tests clear removes all mappings
    @Test
    public void testClear_afterPut_removesAll() {
        MultiValueMap map = new MultiValueMap();
        map.put("key1", "value1");
        map.put("key2", "value2");
        map.clear();
        assertTrue(map.isEmpty());
    }

    // Tests size(Object) returns correct size for key
    @Test
    public void testSize_keyWithMultipleValues_returnsCorrectCount() {
        MultiValueMap map = new MultiValueMap();
        assertEquals(0, map.size("key"));
        map.put("key", "v1");
        assertEquals(1, map.size("key"));
        map.put("key", "v2");
        assertEquals(2, map.size("key"));
    }

    // Tests totalSize counts all values
    @Test
    public void testTotalSize_multipleKeys_returnsTotalCount() {
        MultiValueMap map = new MultiValueMap();
        assertEquals(0, map.totalSize());
        map.put("k1", "v1");
        map.put("k1", "v2");
        map.put("k2", "v3");
        assertEquals(3, map.totalSize());
    }

    // Tests putAll(Map) with MultiMap
    @Test
    public void testPutAll_multiMap_copyWorks() {
        MultiValueMap source = new MultiValueMap();
        source.put("k1", "v1");
        source.put("k1", "v2");
        source.put("k2", "v3");

        MultiValueMap target = new MultiValueMap();
        target.putAll(source);

        assertTrue(target.containsKey("k1"));
        assertTrue(target.containsKey("k2"));
        assertEquals(2, target.getCollection("k1").size());
        assertEquals(1, target.getCollection("k2").size());
    }

    // Tests putAll(Object, Collection) with null values
    @Test
    public void testPutAll_nullCollection_returnsFalse() {
        MultiValueMap map = new MultiValueMap();
        assertFalse(map.putAll("key", null));
    }

    // Tests putAll(Object, Collection) with empty collection
    @Test
    public void testPutAll_emptyCollection_returnsFalse() {
        MultiValueMap map = new MultiValueMap();
        assertFalse(map.putAll("key", new ArrayList()));
    }

    // Tests putAll(Object, Collection) adds to existing key
    @Test
    public void testPutAll_addValuesToExistingKey_succeeds() {
        MultiValueMap map = new MultiValueMap();
        map.put("key", "v1");
        List additional = new ArrayList();
        additional.add("v2");
        additional.add("v3");
        assertTrue(map.putAll("key", additional));
        assertEquals(3, map.getCollection("key").size());
    }

    // Tests iterator(Object) returns empty iterator for unmapped key
    @Test
    public void testIterator_unmappedKey_returnsEmptyIterator() {
        MultiValueMap map = new MultiValueMap();
        Iterator it = map.iterator("nonexistent");
        assertNotNull(it);
        assertFalse(it.hasNext());
    }

    // Tests iterator(Object) returns correct iterator for mapped key
    @Test
    public void testIterator_mappedKey_returnsIteratorWithValues() {
        MultiValueMap map = new MultiValueMap();
        map.put("key", "v1");
        map.put("key", "v2");
        Iterator it = map.iterator("key");
        assertTrue(it.hasNext());
        Set<String> resultSet = new HashSet<String>();
        while (it.hasNext()) {
            resultSet.add((String) it.next());
        }
        assertTrue(resultSet.contains("v1"));
        assertTrue(resultSet.contains("v2"));
    }

    // Tests removeMapping with null key
    @Test
    public void testRemoveMapping_nullKey_returnsNull() {
        MultiValueMap map = new MultiValueMap();
        Object result = map.removeMapping(null, "value");
        assertNull(result);
    }

    // Tests constructor with null factory throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullFactory_throwsException() {
        new MultiValueMap(new HashMap(), null);
    }
}