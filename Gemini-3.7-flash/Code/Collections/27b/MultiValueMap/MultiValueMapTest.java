package org.apache.commons.collections4.map;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.Factory;
import org.apache.commons.collections4.FunctorException;
import org.apache.commons.collections4.MultiMap;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class MultiValueMapTest {

    private MultiValueMap<String, String> map;

    @Before
    public void setUp() {
        map = new MultiValueMap<String, String>();
    }

    // Tests default constructor and basic put/get operations
    @Test
    public void testPut_newKey_addsValueAndReturnsValue() {
        assertNull(map.getCollection("key1"));
        Object result = map.put("key1", "val1");
        assertEquals("val1", result);
        assertEquals(1, map.size("key1"));
        assertTrue(map.getCollection("key1").contains("val1"));
    }

    // Tests put with existing key appends value
    @Test
    public void testPut_existingKey_appendsValue() {
        map.put("key1", "val1");
        Object result = map.put("key1", "val2");
        assertEquals("val2", result);
        assertEquals(2, map.size("key1"));
        assertEquals(2, map.totalSize());
    }

    // Tests removeMapping when key does not exist
    @Test
    public void testRemoveMapping_nonExistentKey_returnsFalse() {
        assertFalse(map.removeMapping("nonExistent", "val1"));
    }

    // Tests removeMapping when value does not exist for key
    @Test
    public void testRemoveMapping_nonExistentValue_returnsFalse() {
        map.put("key1", "val1");
        assertFalse(map.removeMapping("key1", "val2"));
        assertEquals(1, map.size("key1"));
    }

    // Tests removeMapping when removing value leaves collection empty
    @Test
    public void testRemoveMapping_lastValue_removesKeyFromMap() {
        map.put("key1", "val1");
        boolean removed = map.removeMapping("key1", "val1");
        assertTrue(removed);
        assertNull(map.getCollection("key1"));
        assertFalse(map.containsKey("key1"));
        assertEquals(0, map.totalSize());
    }

    // Tests removeMapping when other values remain
    @Test
    public void testRemoveMapping_remainingValues_keepsKeyInMap() {
        map.put("key1", "val1");
        map.put("key1", "val2");
        boolean removed = map.removeMapping("key1", "val1");
        assertTrue(removed);
        assertEquals(1, map.size("key1"));
        assertTrue(map.containsKey("key1"));
        assertFalse(map.containsValue("key1", "val1"));
        assertTrue(map.containsValue("key1", "val2"));
    }

    // Tests containsValue by key and value
    @Test
    public void testContainsValue_byKeyAndValue_returnsCorrectStatus() {
        assertFalse(map.containsValue("key1", "val1"));
        map.put("key1", "val1");
        assertTrue(map.containsValue("key1", "val1"));
        assertFalse(map.containsValue("key1", "val2"));
    }

    // Tests containsValue across all keys
    @Test
    public void testContainsValue_allKeys_findsValue() {
        assertFalse(map.containsValue("val1"));
        map.put("key1", "val1");
        map.put("key2", "val2");
        assertTrue(map.containsValue("val1"));
        assertTrue(map.containsValue("val2"));
        assertFalse(map.containsValue("val3"));
    }

    // Tests putAll with collection of values
    @Test
    public void testPutAll_collectionValues_addsAll() {
        assertFalse(map.putAll("key1", null));
        assertFalse(map.putAll("key1", new ArrayList<String>()));

        boolean changed = map.putAll("key1", Arrays.asList("v1", "v2", "v3"));
        assertTrue(changed);
        assertEquals(3, map.size("key1"));

        boolean changedAgain = map.putAll("key1", Arrays.asList("v4"));
        assertTrue(changedAgain);
        assertEquals(4, map.size("key1"));
    }

    // Tests putAll with another Map (non-MultiMap and MultiMap)
    @Test
    public void testPutAll_mapAndMultiMap_copiesEntries() {
        Map<String, String> regularMap = new HashMap<String, String>();
        regularMap.put("k1", "v1");
        regularMap.put("k2", "v2");
        map.putAll(regularMap);
        assertEquals(2, map.totalSize());

        MultiValueMap<String, String> anotherMultiMap = new MultiValueMap<String, String>();
        anotherMultiMap.putAll("k1", Arrays.asList("v3", "v4"));
        anotherMultiMap.put("k3", "v5");

        map.putAll(anotherMultiMap);
        assertEquals(3, map.size("k1"));
        assertEquals(1, map.size("k2"));
        assertEquals(1, map.size("k3"));
        assertEquals(5, map.totalSize());
    }

    // Tests size(key) and totalSize()
    @Test
    public void testSizeAndTotalSize_variousStates_returnsCorrectCount() {
        assertEquals(0, map.size("k1"));
        assertEquals(0, map.totalSize());

        map.put("k1", "v1");
        map.put("k1", "v2");
        map.put("k2", "v3");

        assertEquals(2, map.size("k1"));
        assertEquals(1, map.size("k2"));
        assertEquals(3, map.totalSize());
    }

    // Tests iterator(key) behavior including remove
    @Test
    public void testIteratorByKey_traversalAndRemove_removesElementAndKeyWhenEmpty() {
        Iterator<String> emptyIt = map.iterator("nonExistent");
        assertFalse(emptyIt.hasNext());

        map.put("k1", "v1");
        map.put("k1", "v2");

        Iterator<String> it = map.iterator("k1");
        assertTrue(it.hasNext());
        assertEquals("v1", it.next());
        it.remove();
        assertEquals(1, map.size("k1"));

        assertTrue(it.hasNext());
        assertEquals("v2", it.next());
        it.remove();
        assertFalse(map.containsKey("k1"));
        assertEquals(0, map.totalSize());
    }

    // Tests iterator over all entries
    @Test
    public void testIterator_allEntries_iteratesEveryKeyValueMapping() {
        map.put("k1", "v1");
        map.put("k1", "v2");
        map.put("k2", "v3");

        Iterator<Map.Entry<String, String>> it = map.iterator();
        int count = 0;
        while (it.hasNext()) {
            Map.Entry<String, String> entry = it.next();
            assertNotNull(entry.getKey());
            assertNotNull(entry.getValue());
            count++;
        }
        assertEquals(3, count);
    }

    // Tests unsupported setValue on entry iterator
    @Test(expected = UnsupportedOperationException.class)
    public void testIterator_entrySetValue_throwsUnsupportedOperationException() {
        map.put("k1", "v1");
        Iterator<Map.Entry<String, String>> it = map.iterator();
        Map.Entry<String, String> entry = it.next();
        entry.setValue("newVal");
    }

    // Tests values() collection view
    @Test
    public void testValues_collectionOperations_reflectsMapContents() {
        map.put("k1", "v1");
        map.put("k1", "v2");
        map.put("k2", "v3");

        Collection<Object> values = map.values();
        assertEquals(3, values.size());

        Set<Object> set = new HashSet<Object>(values);
        assertTrue(set.contains("v1"));
        assertTrue(set.contains("v2"));
        assertTrue(set.contains("v3"));

        values.clear();
        assertEquals(0, map.totalSize());
        assertTrue(map.isEmpty());
    }

    // Tests factory creation with custom Collection class (HashSet)
    @Test
    public void testMultiValueMap_customCollectionClass_instantiatesCorrectType() {
        MultiValueMap<String, String> setMap = MultiValueMap.multiValueMap(
                new HashMap<String, HashSet<String>>(),
                HashSet.class
        );
        setMap.put("k1", "v1");
        setMap.put("k1", "v1"); // Duplicate should not increase size in HashSet

        assertEquals(1, setMap.size("k1"));
        assertTrue(setMap.getCollection("k1") instanceof HashSet);
    }

    // Tests factory creation with custom factory
    @Test
    public void testMultiValueMap_customFactory_usesFactory() {
        Factory<ArrayList<String>> factory = new Factory<ArrayList<String>>() {
            @Override
            public ArrayList<String> create() {
                return new ArrayList<String>();
            }
        };
        MultiValueMap<String, String> customMap = MultiValueMap.multiValueMap(
                new HashMap<String, ArrayList<String>>(),
                factory
        );
        customMap.put("k1", "v1");
        assertEquals(1, customMap.size("k1"));
    }

    // Tests constructor with null factory throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullFactory_throwsIllegalArgumentException() {
        new MultiValueMap<String, String>(new HashMap<String, ArrayList<String>>(), (Factory<ArrayList<String>>) null);
    }

    // Tests clear() empties the map
    @Test
    public void testClear_populatedMap_becomesEmpty() {
        map.put("k1", "v1");
        map.put("k2", "v2");
        assertEquals(2, map.totalSize());

        map.clear();
        assertEquals(0, map.totalSize());
        assertTrue(map.isEmpty());
        assertNull(map.getCollection("k1"));
    }

    // Tests serialization and deserialization
    @Test
    @SuppressWarnings("unchecked")
    public void testSerialization_roundTrip_restoresState() throws Exception {
        map.put("k1", "v1");
        map.put("k1", "v2");
        map.put("k2", "v3");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(map);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        MultiValueMap<String, String> deserialized = (MultiValueMap<String, String>) ois.readObject();
        ois.close();

        assertEquals(map.totalSize(), deserialized.totalSize());
        assertEquals(2, deserialized.size("k1"));
        assertEquals(1, deserialized.size("k2"));
        assertTrue(deserialized.containsValue("k1", "v1"));
        assertTrue(deserialized.containsValue("k1", "v2"));
        assertTrue(deserialized.containsValue("k2", "v3"));
    }

    // Tests invalid collection class in factory throws FunctorException
    @Test(expected = FunctorException.class)
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReflectionFactory_invalidClass_throwsFunctorException() {
        MultiValueMap invalidMap = MultiValueMap.multiValueMap(new HashMap(), (Class) AbstractCollection.class);
        invalidMap.put("k1", "v1");
    }

    // Tests factory creation with default collection class
    @Test
    public void testMultiValueMap_defaultCollectionClassFactory() {
        MultiValueMap<String, String> defaultMap = MultiValueMap.multiValueMap(new HashMap<String, ArrayList<String>>());
        assertNotNull(defaultMap);
        defaultMap.put("k1", "v1");
        assertEquals(1, defaultMap.size("k1"));
        assertTrue(defaultMap.getCollection("k1") instanceof ArrayList);
    }

    // Tests remove via entry iterator
    @Test
    public void testIterator_entryRemove_removesItemAndKeyWhenEmpty() {
        map.put("k1", "v1");
        map.put("k1", "v2");

        Iterator<Map.Entry<String, String>> it = map.iterator();
        assertTrue(it.hasNext());
        Map.Entry<String, String> entry1 = it.next();
        assertEquals("k1", entry1.getKey());
        it.remove();
        assertEquals(1, map.size("k1"));

        assertTrue(it.hasNext());
        Map.Entry<String, String> entry2 = it.next();
        assertEquals("k1", entry2.getKey());
        it.remove();
        assertFalse(map.containsKey("k1"));
        assertEquals(0, map.totalSize());
    }

    // Tests createCollection method
    @Test
    public void testCreateCollection_instantiatesEmptyCollection() {
        Collection<String> col = map.createCollection(10);
        assertNotNull(col);
        assertTrue(col.isEmpty());
    }

    // Tests containsValue for null key/value cases
    @Test
    public void testContainsValue_nullHandling() {
        assertFalse(map.containsValue(null));
        assertFalse(map.containsValue("k1", null));

        map.put("k1", null);
        assertTrue(map.containsValue(null));
        assertTrue(map.containsValue("k1", null));
        assertFalse(map.containsValue("nonExistent", null));
    }
}