package org.apache.commons.collections.map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.Factory;
import org.apache.commons.collections.FunctorException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for MultiValueMap.
 */
public class MultiValueMapTest {

    private MultiValueMap map;

    @Before
    public void setUp() {
        map = new MultiValueMap();
    }

    // Tests constructor with null factory throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullFactory_throwsIllegalArgumentException() {
        new MultiValueMap(new HashMap(), null);
    }

    // Tests decoration methods with various factories
    @Test
    public void testDecorate_validInputs_createsConfiguredMap() {
        MultiValueMap map1 = MultiValueMap.decorate(new HashMap());
        assertNotNull(map1);

        MultiValueMap map2 = MultiValueMap.decorate(new HashMap(), HashSet.class);
        map2.put("key", "val1");
        assertTrue(map2.getCollection("key") instanceof HashSet);

        Factory factory = new Factory() {
            public Object create() {
                return new ArrayList();
            }
        };
        MultiValueMap map3 = MultiValueMap.decorate(new HashMap(), factory);
        assertNotNull(map3);
    }

    // Tests reflection factory exception when instantiating abstract class or interface
    @Test(expected = FunctorException.class)
    public void testDecorate_uninstantiableClass_throwsFunctorException() {
        MultiValueMap uninstantiableMap = MultiValueMap.decorate(new HashMap(), List.class);
        uninstantiableMap.put("key", "value");
    }

    // Tests put method for new and existing keys
    @Test
    public void testPut_newAndExistingKey_addsValuesCorrectly() {
        map.put("A", "1");
        assertEquals(1, map.size("A"));
        assertTrue(map.containsValue("A", "1"));

        map.put("A", "2");
        assertEquals(2, map.size("A"));
        assertTrue(map.containsValue("A", "2"));
    }

    // Tests putAll with Collection input including null and empty
    @Test
    public void testPutAll_collectionInput_addsValuesCorrectly() {
        assertFalse(map.putAll("A", null));
        assertFalse(map.putAll("A", new ArrayList()));

        List values = Arrays.asList("1", "2");
        map.putAll("A", values);
        assertEquals(2, map.size("A"));

        List moreValues = Arrays.asList("3");
        boolean changed = map.putAll("A", moreValues);
        assertTrue(changed);
        assertEquals(3, map.size("A"));
    }

    // Tests putAll with standard Map
    @Test
    public void testPutAll_standardMap_copiesEntries() {
        Map normalMap = new HashMap();
        normalMap.put("A", "1");
        normalMap.put("B", "2");

        map.putAll(normalMap);
        assertEquals(1, map.size("A"));
        assertEquals(1, map.size("B"));
    }

    // Tests putAll with MultiMap
    @Test
    public void testPutAll_multiMap_copiesAllValues() {
        MultiValueMap source = new MultiValueMap();
        source.put("A", "1");
        source.put("A", "2");

        map.putAll(source);
        assertEquals(2, map.size("A"));
        assertTrue(map.containsValue("A", "1"));
        assertTrue(map.containsValue("A", "2"));
    }

    // Tests removeMapping for various conditions
    @Test
    public void testRemoveMapping_variousConditions_removesCorrectly() {
        assertNull(map.removeMapping("nonExistingKey", "val"));

        map.put("A", "1");
        map.put("A", "2");
        assertNull(map.removeMapping("A", "nonExistingVal"));

        assertEquals("1", map.removeMapping("A", "1"));
        assertEquals(1, map.size("A"));
        assertNotNull(map.get("A"));

        assertEquals("2", map.removeMapping("A", "2"));
        assertNull(map.get("A"));
        assertFalse(map.containsKey("A"));
    }

    // Tests containsValue searching whole map
    @Test
    public void testContainsValue_searchesWholeMap_returnsCorrectResult() {
        assertFalse(map.containsValue("1"));

        map.put("A", "1");
        map.put("B", "2");

        assertTrue(map.containsValue("1"));
        assertTrue(map.containsValue("2"));
        assertFalse(map.containsValue("3"));
    }

    // Tests containsValue for a specific key
    @Test
    public void testContainsValue_forKey_returnsCorrectResult() {
        assertFalse(map.containsValue("A", "1"));

        map.put("A", "1");
        assertTrue(map.containsValue("A", "1"));
        assertFalse(map.containsValue("A", "2"));
        assertFalse(map.containsValue("B", "1"));
    }

    // Tests getCollection and size for specific keys
    @Test
    public void testGetCollectionAndSize_returnsCorrectValues() {
        assertNull(map.getCollection("A"));
        assertEquals(0, map.size("A"));

        map.put("A", "1");
        map.put("A", "2");

        Collection coll = map.getCollection("A");
        assertNotNull(coll);
        assertEquals(2, coll.size());
        assertEquals(2, map.size("A"));
    }

    // Tests totalSize across all keys
    @Test
    public void testTotalSize_multipleKeys_returnsAccurateCount() {
        assertEquals(0, map.totalSize());

        map.put("A", "1");
        map.put("A", "2");
        map.put("B", "3");

        assertEquals(3, map.totalSize());
    }

    // Tests iterator on specific key
    @Test
    public void testIterator_onKey_iteratesAndRemoves() {
        Iterator emptyIt = map.iterator("nonExistent");
        assertFalse(emptyIt.hasNext());

        map.put("A", "1");
        map.put("A", "2");

        Iterator it = map.iterator("A");
        assertTrue(it.hasNext());
        assertEquals("1", it.next());
        it.remove();
        assertEquals(1, map.size("A"));

        assertTrue(it.hasNext());
        assertEquals("2", it.next());
        it.remove();
        assertNull(map.get("A"));
        assertFalse(map.containsKey("A"));
    }

    // Tests values collection view and operations
    @Test
    public void testValues_viewOperations_success() {
        map.put("A", "1");
        map.put("B", "2");

        Collection values = map.values();
        assertEquals(2, values.size());

        Iterator it = values.iterator();
        Set collected = new HashSet();
        while (it.hasNext()) {
            collected.add(it.next());
        }
        assertTrue(collected.contains("1"));
        assertTrue(collected.contains("2"));

        values.clear();
        assertEquals(0, map.totalSize());
        assertTrue(map.isEmpty());
    }

    // Tests clear method
    @Test
    public void testClear_clearsAllMappings() {
        map.put("A", "1");
        map.put("B", "2");

        map.clear();
        assertEquals(0, map.totalSize());
        assertNull(map.get("A"));
        assertNull(map.get("B"));
        assertTrue(map.isEmpty());
    }
}