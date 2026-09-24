package org.apache.commons.collections.map;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.collections.Factory;
import org.apache.commons.collections.FunctorException;
import org.apache.commons.collections.MultiMap;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class MultiValueMapTest {

    private MultiValueMap map;

    @Before
    public void setUp() {
        map = new MultiValueMap();
    }

    // Tests adding multiple values for the same key and retrieving collection
    @Test
    public void testPut_multipleValuesSameKey_storesInCollection() {
        Object res1 = map.put("key1", "val1");
        Object res2 = map.put("key1", "val2");

        assertEquals("val1", res1);
        assertEquals("val2", res2);
        assertEquals(2, map.size("key1"));

        Collection coll = map.getCollection("key1");
        assertNotNull(coll);
        assertEquals(2, coll.size());
        assertTrue(coll.contains("val1"));
        assertTrue(coll.contains("val2"));
    }

    // Tests putAll with collection of values
    @Test
    public void testPutAll_collectionOfValues_addsAllSuccessfully() {
        Collection values = Arrays.asList("A", "B", "C");
        boolean changed = map.putAll("key1", values);

        assertTrue(changed);
        assertEquals(3, map.size("key1"));
        assertTrue(map.containsValue("key1", "B"));
        assertFalse(map.containsValue("key1", "D"));

        // Adding more to existing key
        boolean changedAgain = map.putAll("key1", Arrays.asList("D", "E"));
        assertTrue(changedAgain);
        assertEquals(5, map.size("key1"));
    }

    // Tests putAll with null or empty collection
    @Test
    public void testPutAll_nullOrEmptyCollection_returnsFalse() {
        assertFalse(map.putAll("key1", null));
        assertFalse(map.putAll("key1", new ArrayList()));
        assertEquals(0, map.size("key1"));
    }

    // Tests putAll with another MultiMap vs standard Map
    @Test
    public void testPutAll_mapParameter_copiesCorrectly() {
        MultiValueMap multiSource = new MultiValueMap();
        multiSource.put("key1", "val1");
        multiSource.put("key1", "val2");
        multiSource.put("key2", "val3");

        map.putAll(multiSource);
        assertEquals(2, map.size("key1"));
        assertEquals(1, map.size("key2"));

        MultiValueMap normalTarget = new MultiValueMap();
        Map normalMap = new HashMap();
        normalMap.put("keyA", "valA");
        normalMap.put("keyB", "valB");

        normalTarget.putAll(normalMap);
        assertEquals(1, normalTarget.size("keyA"));
        assertEquals(1, normalTarget.size("keyB"));
        assertTrue(normalTarget.containsValue("keyA", "valA"));
    }

    // Tests removeMapping removes specific item and cleans up empty collection
    @Test
    public void testRemoveMapping_existingKeyAndValue_removesAndCleansUp() {
        map.put("key1", "val1");
        map.put("key1", "val2");

        Object removed = map.removeMapping("key1", "val1");
        assertEquals("val1", removed);
        assertEquals(1, map.size("key1"));
        assertFalse(map.containsValue("key1", "val1"));
        assertTrue(map.containsValue("key1", "val2"));

        // Remove the last value for the key
        Object removedLast = map.removeMapping("key1", "val2");
        assertEquals("val2", removedLast);
        assertNull(map.getCollection("key1"));
        assertFalse(map.containsKey("key1"));
    }

    // Tests removeMapping for non-existing key or non-existing value
    @Test
    public void testRemoveMapping_nonExistentKeyOrValue_returnsNull() {
        map.put("key1", "val1");

        assertNull(map.removeMapping("key1", "nonExistentVal"));
        assertNull(map.removeMapping("nonExistentKey", "val1"));
    }

    // Tests containsValue searching across all collections
    @Test
    public void testContainsValue_acrossAllKeys_findsCorrectly() {
        map.put("key1", "val1");
        map.put("key2", "val2");

        assertTrue(map.containsValue("val1"));
        assertTrue(map.containsValue("val2"));
        assertFalse(map.containsValue("val3"));
    }

    // Tests containsValue with null or empty map
    @Test
    public void testContainsValue_emptyMap_returnsFalse() {
        assertFalse(map.containsValue("anything"));
        assertFalse(map.containsValue("key1", "anything"));
    }

    // Tests totalSize across all mappings
    @Test
    public void testTotalSize_multipleKeysAndValues_calculatesSum() {
        assertEquals(0, map.totalSize());

        map.put("key1", "val1");
        map.put("key1", "val2");
        map.put("key2", "val3");

        assertEquals(3, map.totalSize());
    }

    // Tests clear method
    @Test
    public void testClear_populatedMap_resetsMap() {
        map.put("key1", "val1");
        map.put("key2", "val2");
        assertEquals(2, map.totalSize());

        map.clear();
        assertEquals(0, map.totalSize());
        assertNull(map.getCollection("key1"));
    }

    // Tests size(key) for non-existing key
    @Test
    public void testSize_nonExistentKey_returnsZero() {
        assertEquals(0, map.size("nonExistent"));
    }

    // Tests values() view collection and iterator
    @Test
    public void testValues_viewCollection_iteratesAllValues() {
        map.put("key1", "val1");
        map.put("key1", "val2");
        map.put("key2", "val3");

        Collection vals = map.values();
        assertEquals(3, vals.size());

        Set collected = new HashSet();
        for (Iterator it = vals.iterator(); it.hasNext();) {
            collected.add(it.next());
        }

        assertTrue(collected.contains("val1"));
        assertTrue(collected.contains("val2"));
        assertTrue(collected.contains("val3"));

        vals.clear();
        assertEquals(0, map.totalSize());
    }

    // Tests iterator(key) behavior including removing elements
    @Test
    public void testIterator_existingKey_removesElementAndEntryWhenEmpty() {
        map.put("key1", "val1");
        map.put("key1", "val2");

        Iterator it = map.iterator("key1");
        assertTrue(it.hasNext());
        assertEquals("val1", it.next());
        it.remove();

        assertEquals(1, map.size("key1"));
        assertTrue(it.hasNext());
        assertEquals("val2", it.next());
        it.remove();

        assertFalse(map.containsKey("key1"));
    }

    // Tests iterator(key) for non-existent key returns EmptyIterator
    @Test
    public void testIterator_nonExistentKey_returnsEmptyIterator() {
        Iterator it = map.iterator("nonExistent");
        assertNotNull(it);
        assertFalse(it.hasNext());
    }

    // Tests decorate factory with Class type (TreeSet)
    @Test
    public void testDecorate_withCollectionClass_createsSpecifiedCollectionType() {
        MultiValueMap treeSetMap = MultiValueMap.decorate(new HashMap(), TreeSet.class);
        treeSetMap.put("key1", "B");
        treeSetMap.put("key1", "A");

        Collection coll = treeSetMap.getCollection("key1");
        assertTrue(coll instanceof TreeSet);
        Iterator it = coll.iterator();
        assertEquals("A", it.next());
        assertEquals("B", it.next());
    }

    // Tests decorate with custom Factory instance
    @Test
    public void testDecorate_withCustomFactory_usesFactory() {
        Factory customFactory = new Factory() {
            public Object create() {
                return new HashSet();
            }
        };

        MultiValueMap customMap = MultiValueMap.decorate(new HashMap(), customFactory);
        customMap.put("key1", "val1");
        customMap.put("key1", "val1"); // Duplicate for Set

        assertEquals(1, customMap.size("key1"));
        assertTrue(customMap.getCollection("key1") instanceof HashSet);
    }

    // Tests constructor with null Factory throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullFactory_throwsIllegalArgumentException() {
        new MultiValueMap(new HashMap(), null);
    }

    // Tests reflection factory instantiation failure throws FunctorException
    @Test(expected = FunctorException.class)
    public void testDecorate_uninstantiableClass_throwsFunctorException() {
        MultiValueMap uninstantiableMap = MultiValueMap.decorate(new HashMap(), Collection.class);
        uninstantiableMap.put("key1", "val1");
    }

    // Tests serialization and deserialization
    @Test
    public void testSerialization_roundTrip_preservesContent() throws Exception {
        map.put("key1", "val1");
        map.put("key1", "val2");
        map.put("key2", "val3");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(map);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        MultiValueMap deserialized = (MultiValueMap) ois.readObject();
        ois.close();

        assertEquals(3, deserialized.totalSize());
        assertEquals(2, deserialized.size("key1"));
        assertEquals(1, deserialized.size("key2"));
        assertTrue(deserialized.containsValue("key1", "val1"));
        assertTrue(deserialized.containsValue("key1", "val2"));
        assertTrue(deserialized.containsValue("key2", "val3"));
    }
}