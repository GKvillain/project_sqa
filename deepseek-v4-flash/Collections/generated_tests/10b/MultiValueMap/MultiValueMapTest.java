package org.apache.commons.collections.map;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.collections.Factory;
import org.junit.Test;

/**
 * JUnit 4 test class for MultiValueMap, targeting Defects4J bug 10b.
 */
public class MultiValueMapTest {

    // Helper to create a decorated map with default ArrayList
    private MultiValueMap createDefaultMultiMap() {
        return (MultiValueMap) MultiValueMap.decorate(new HashMap());
    }

    // -------------------- Normal cases --------------------

    @Test
    public void testPut_singleValue_returnsValueAndStoresInCollection() {
        MultiValueMap mvm = createDefaultMultiMap();
        Object key = "key1";
        Object value = "val1";
        Object returned = mvm.put(key, value);
        assertEquals(value, returned);
        Collection coll = mvm.getCollection(key);
        assertNotNull(coll);
        assertEquals(1, coll.size());
        assertTrue(coll.contains(value));
    }

    @Test
    public void testPut_multipleValuesForSameKey_storesAllInCollection() {
        MultiValueMap mvm = createDefaultMultiMap();
        Object key = "key";
        mvm.put(key, "a");
        mvm.put(key, "b");
        Collection coll = mvm.getCollection(key);
        assertEquals(2, coll.size());
        assertTrue(coll.contains("a"));
        assertTrue(coll.contains("b"));
    }

    @Test
    public void testPutAll_collectionOfValues_addsAll() {
        MultiValueMap mvm = createDefaultMultiMap();
        Object key = "key";
        Collection values = new ArrayList();
        values.add("x");
        values.add("y");
        boolean changed = mvm.putAll(key, values);
        assertTrue(changed);
        Collection coll = mvm.getCollection(key);
        assertEquals(2, coll.size());
        assertTrue(coll.containsAll(values));
    }

    @Test
    public void testPutAll_nullCollection_returnsFalse() {
        MultiValueMap mvm = createDefaultMultiMap();
        assertFalse(mvm.putAll("key", null));
    }

    @Test
    public void testPutAll_emptyCollection_returnsFalse() {
        MultiValueMap mvm = createDefaultMultiMap();
        assertFalse(mvm.putAll("key", new ArrayList()));
    }

    @Test
    public void testRemoveMapping_removesValue_returnsValue() {
        MultiValueMap mvm = createDefaultMultiMap();
        mvm.put("k", "v1");
        mvm.put("k", "v2");
        Object removed = mvm.removeMapping("k", "v1");
        assertEquals("v1", removed);
        Collection coll = mvm.getCollection("k");
        assertTrue(coll.contains("v2"));
        assertFalse(coll.contains("v1"));
    }

    @Test
    public void testRemoveMapping_lastValueRemovesKey_returnsValue() {
        MultiValueMap mvm = createDefaultMultiMap();
        mvm.put("k", "v");
        Object removed = mvm.removeMapping("k", "v");
        assertEquals("v", removed);
        assertNull(mvm.getCollection("k"));
        assertFalse(mvm.containsKey("k"));
    }

    @Test
    public void testRemoveMapping_nonexistentValue_returnsNull() {
        MultiValueMap mvm = createDefaultMultiMap();
        mvm.put("k", "v");
        assertNull(mvm.removeMapping("k", "nonexistent"));
        // map unchanged
        assertNotNull(mvm.getCollection("k"));
    }

    @Test
    public void testRemoveMapping_keyNotPresent_returnsNull() {
        MultiValueMap mvm = createDefaultMultiMap();
        assertNull(mvm.removeMapping("absent", "v"));
    }

    @Test
    public void testContainsValue_valuePresent_returnsTrue() {
        MultiValueMap mvm = createDefaultMultiMap();
        mvm.put("k1", "a");
        mvm.put("k2", "b");
        assertTrue(mvm.containsValue("a"));
        assertTrue(mvm.containsValue("b"));
    }

    @Test
    public void testContainsValue_valueAbsent_returnsFalse() {
        MultiValueMap mvm = createDefaultMultiMap();
        mvm.put("k", "val");
        assertFalse(mvm.containsValue("other"));
    }

    @Test
    public void testContainsValue_keyAndValue_present_returnsTrue() {
        MultiValueMap mvm = createDefaultMultiMap();
        mvm.put("k", "v");
        assertTrue(mvm.containsValue("k", "v"));
    }

    @Test
    public void testContainsValue_keyAndValue_absent_returnsFalse() {
        MultiValueMap mvm = createDefaultMultiMap();
        mvm.put("k", "v");
        assertFalse(mvm.containsValue("k", "nonexistent"));
        assertFalse(mvm.containsValue("other", "v"));
    }

    @Test
    public void testTotalSize_multipleKeysAndValues_countsAll() {
        MultiValueMap mvm = createDefaultMultiMap();
        mvm.put("k1", "a");
        mvm.put("k1", "b");
        mvm.put("k2", "c");
        assertEquals(3, mvm.totalSize());
    }

    @Test
    public void testSize_objectKey_existent_returnsCollectionSize() {
        MultiValueMap mvm = createDefaultMultiMap();
        mvm.put("k", "a");
        mvm.put("k", "b");
        assertEquals(2, mvm.size("k"));
    }

    @Test
    public void testSize_objectKey_nonexistent_returnsZero() {
        MultiValueMap mvm = createDefaultMultiMap();
        assertEquals(0, mvm.size("nonexistent"));
    }

    @Test
    public void testIterator_keyNotPresent_returnsEmptyIterator() {
        MultiValueMap mvm = createDefaultMultiMap();
        Iterator it = mvm.iterator("absent");
        assertNotNull(it);
        assertFalse(it.hasNext());
    }

    @Test
    public void testIterator_keyPresent_iteratesAllValues() {
        MultiValueMap mvm = createDefaultMultiMap();
        mvm.put("k", "a");
        mvm.put("k", "b");
        Iterator it = mvm.iterator("k");
        assertTrue(it.hasNext());
        assertEquals("a", it.next());
        assertTrue(it.hasNext());
        assertEquals("b", it.next());
        assertFalse(it.hasNext());
    }

    @Test
    public void testValuesIterator_remove_removeValueAndKeyIfEmpty() {
        MultiValueMap mvm = createDefaultMultiMap();
        mvm.put("k", "v");
        Iterator values = mvm.iterator("k");
        values.next();
        values.remove();
        assertNull(mvm.getCollection("k"));
        assertFalse(mvm.containsKey("k"));
    }

    @Test
    public void testClear_clearsUnderlyingMap() {
        MultiValueMap mvm = createDefaultMultiMap();
        mvm.put("k", "v");
        mvm.clear();
        assertTrue(mvm.isEmpty());
        assertEquals(0, mvm.totalSize());
    }

    @Test
    public void testPutAll_Map_normalMap_behavesLikeSeparatePuts() {
        MultiValueMap mvm = createDefaultMultiMap();
        Map normal = new HashMap();
        normal.put("k1", "v1");
        normal.put("k2", "v2");
        mvm.putAll(normal);
        assertTrue(mvm.containsValue("v1"));
        assertTrue(mvm.containsValue("v2"));
        // single values
        assertEquals(1, mvm.getCollection("k1").size());
    }

    @Test
    public void testPutAll_Map_multiMap_addsAllCollections() {
        MultiValueMap mvm = createDefaultMultiMap();
        MultiValueMap other = createDefaultMultiMap();
        other.put("k", "a");
        other.put("k", "b");
        mvm.putAll(other);
        Collection coll = mvm.getCollection("k");
        assertEquals(2, coll.size());
        assertTrue(coll.contains("a"));
        assertTrue(coll.contains("b"));
    }

    @Test
    public void testValues_view_containsAllValues() {
        MultiValueMap mvm = createDefaultMultiMap();
        mvm.put("k1", "a");
        mvm.put("k1", "b");
        mvm.put("k2", "c");
        Collection values = mvm.values();
        assertEquals(3, values.size());
        assertTrue(values.contains("a"));
        assertTrue(values.contains("b"));
        assertTrue(values.contains("c"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullFactory_throwsIllegalArgumentException() {
        Map base = new HashMap();
        new MultiValueMap(base, null);
    }
}