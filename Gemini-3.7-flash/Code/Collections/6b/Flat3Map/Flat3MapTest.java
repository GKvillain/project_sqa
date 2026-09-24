package org.apache.commons.collections.map;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.ResettableIterator;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class Flat3MapTest {

    private Flat3Map map;

    @Before
    public void setUp() {
        map = new Flat3Map();
    }

    // Tests basic put and get in flat mode for 1 to 3 entries
    @Test
    public void testPutAndGet_flatMode_successfulMappings() {
        assertTrue(map.isEmpty());
        assertEquals(0, map.size());

        assertNull(map.put("key1", "val1"));
        assertEquals(1, map.size());
        assertEquals("val1", map.get("key1"));
        assertNull(map.get("nonexistent"));

        assertNull(map.put("key2", "val2"));
        assertEquals(2, map.size());
        assertEquals("val1", map.get("key1"));
        assertEquals("val2", map.get("key2"));

        assertNull(map.put("key3", "val3"));
        assertEquals(3, map.size());
        assertEquals("val1", map.get("key1"));
        assertEquals("val2", map.get("key2"));
        assertEquals("val3", map.get("key3"));
    }

    // Tests removing key1 when size is 3 to verify correct old value returned
    @Test
    public void testRemove_size3RemoveKey1_returnsOldValue1() {
        map.put("key1", "val1");
        map.put("key2", "val2");
        map.put("key3", "val3");

        Object removed = map.remove("key1");
        assertEquals("val1", removed);
        assertEquals(2, map.size());
        assertNull(map.get("key1"));
        assertEquals("val2", map.get("key2"));
        assertEquals("val3", map.get("key3"));
    }

    // Tests removing key2 when size is 3 to verify correct old value returned
    @Test
    public void testRemove_size3RemoveKey2_returnsOldValue2() {
        map.put("key1", "val1");
        map.put("key2", "val2");
        map.put("key3", "val3");

        Object removed = map.remove("key2");
        assertEquals("val2", removed);
        assertEquals(2, map.size());
        assertEquals("val1", map.get("key1"));
        assertNull(map.get("key2"));
        assertEquals("val3", map.get("key3"));
    }

    // Tests removing key3 when size is 3 to verify correct old value returned
    @Test
    public void testRemove_size3RemoveKey3_returnsOldValue3() {
        map.put("key1", "val1");
        map.put("key2", "val2");
        map.put("key3", "val3");

        Object removed = map.remove("key3");
        assertEquals("val3", removed);
        assertEquals(2, map.size());
        assertEquals("val1", map.get("key1"));
        assertEquals("val2", map.get("key2"));
        assertNull(map.get("key3"));
    }

    // Tests removing key1 when size is 2 to verify correct old value returned
    @Test
    public void testRemove_size2RemoveKey1_returnsOldValue1() {
        map.put("key1", "val1");
        map.put("key2", "val2");

        Object removed = map.remove("key1");
        assertEquals("val1", removed);
        assertEquals(1, map.size());
        assertNull(map.get("key1"));
        assertEquals("val2", map.get("key2"));
    }

    // Tests removing key2 when size is 2 to verify correct old value returned
    @Test
    public void testRemove_size2RemoveKey2_returnsOldValue2() {
        map.put("key1", "val1");
        map.put("key2", "val2");

        Object removed = map.remove("key2");
        assertEquals("val2", removed);
        assertEquals(1, map.size());
        assertEquals("val1", map.get("key1"));
        assertNull(map.get("key2"));
    }

    // Tests removing null key across positions in flat mode
    @Test
    public void testRemove_nullKeyAtDifferentPositions_returnsCorrectValues() {
        map.put(null, "nullVal1");
        map.put("key2", "val2");
        map.put("key3", "val3");

        assertEquals("nullVal1", map.remove(null));
        assertEquals(2, map.size());
        assertNull(map.get(null));

        map.clear();
        map.put("key1", "val1");
        map.put(null, "nullVal2");
        map.put("key3", "val3");

        assertEquals("nullVal2", map.remove(null));
        assertEquals(2, map.size());
        assertNull(map.get(null));

        map.clear();
        map.put("key1", "val1");
        map.put("key2", "val2");
        map.put(null, "nullVal3");

        assertEquals("nullVal3", map.remove(null));
        assertEquals(2, map.size());
        assertNull(map.get(null));

        map.clear();
        map.put(null, "nullVal1");
        map.put("key2", "val2");

        assertEquals("nullVal1", map.remove(null));
        assertEquals(1, map.size());
        assertNull(map.get(null));

        map.clear();
        map.put(null, "onlyNull");
        assertEquals("onlyNull", map.remove(null));
        assertEquals(0, map.size());
    }

    // Tests putting existing keys and null keys to verify old value replacement
    @Test
    public void testPut_overwritingExistingKey_returnsPreviousValue() {
        map.put("key1", "val1");
        map.put("key2", "val2");
        map.put("key3", "val3");

        assertEquals("val1", map.put("key1", "newVal1"));
        assertEquals("newVal1", map.get("key1"));

        assertEquals("val2", map.put("key2", "newVal2"));
        assertEquals("newVal2", map.get("key2"));

        assertEquals("val3", map.put("key3", "newVal3"));
        assertEquals("newVal3", map.get("key3"));

        map.clear();
        map.put(null, "valA");
        map.put("k2", "valB");
        map.put("k3", "valC");
        assertEquals("valA", map.put(null, "newValA"));
        assertEquals("newValA", map.get(null));
    }

    // Tests transition from flat mode to delegate mode when size exceeds 3
    @Test
    public void testPut_exceedingThreeElements_switchesToDelegateMap() {
        map.put("k1", "v1");
        map.put("k2", "v2");
        map.put("k3", "v3");
        assertNull(map.put("k4", "v4"));

        assertEquals(4, map.size());
        assertEquals("v1", map.get("k1"));
        assertEquals("v2", map.get("k2"));
        assertEquals("v3", map.get("k3"));
        assertEquals("v4", map.get("k4"));

        assertTrue(map.containsKey("k4"));
        assertTrue(map.containsValue("v4"));
        assertEquals("v4", map.remove("k4"));
        assertEquals(3, map.size());
    }

    // Tests putAll with maps of various sizes
    @Test
    public void testPutAll_smallAndLargeMaps_handledProperly() {
        Map sourceSmall = new HashMap();
        sourceSmall.put("a", "1");
        sourceSmall.put("b", "2");
        map.putAll(sourceSmall);
        assertEquals(2, map.size());
        assertEquals("1", map.get("a"));

        Map sourceLarge = new HashMap();
        sourceLarge.put("c", "3");
        sourceLarge.put("d", "4");
        sourceLarge.put("e", "5");
        sourceLarge.put("f", "6");
        map.putAll(sourceLarge);

        assertEquals(6, map.size());
        assertEquals("3", map.get("c"));
        assertEquals("6", map.get("f"));

        Flat3Map copyConstructed = new Flat3Map(sourceSmall);
        assertEquals(2, copyConstructed.size());
    }

    // Tests containsKey and containsValue for both flat and delegate modes
    @Test
    public void testContainsKeyAndContainsValue_flatAndDelegate_returnsExpected() {
        map.put("k1", "v1");
        map.put("k2", null);
        map.put(null, "v3");

        assertTrue(map.containsKey("k1"));
        assertTrue(map.containsKey("k2"));
        assertTrue(map.containsKey(null));
        assertFalse(map.containsKey("k4"));

        assertTrue(map.containsValue("v1"));
        assertTrue(map.containsValue(null));
        assertTrue(map.containsValue("v3"));
        assertFalse(map.containsValue("unknown"));

        // Switch to delegate mode
        map.put("k4", "v4");
        assertTrue(map.containsKey("k4"));
        assertTrue(map.containsValue("v4"));
        assertFalse(map.containsKey("k5"));
        assertFalse(map.containsValue("v5"));
    }

    // Tests clear method resetting flat and delegate mode
    @Test
    public void testClear_resetsMapProperly() {
        map.put("k1", "v1");
        map.put("k2", "v2");
        map.clear();
        assertEquals(0, map.size());
        assertTrue(map.isEmpty());
        assertNull(map.get("k1"));

        map.put("k1", "v1");
        map.put("k2", "v2");
        map.put("k3", "v3");
        map.put("k4", "v4");
        map.clear();
        assertEquals(0, map.size());
        assertTrue(map.isEmpty());
    }

    // Tests MapIterator forward iteration, value setting, and removal
    @Test
    public void testMapIterator_iterationAndModification_worksCorrectly() {
        MapIterator emptyIt = map.mapIterator();
        assertFalse(emptyIt.hasNext());

        map.put("k1", "v1");
        map.put("k2", "v2");
        map.put("k3", "v3");

        MapIterator it = map.mapIterator();
        assertTrue(it.hasNext());
        assertEquals("k1", it.next());
        assertEquals("k1", it.getKey());
        assertEquals("v1", it.getValue());
        assertEquals("v1", it.setValue("newV1"));
        assertEquals("newV1", map.get("k1"));

        assertEquals("k2", it.next());
        it.remove();
        assertEquals(2, map.size());
        assertFalse(map.containsKey("k2"));

        if (it instanceof ResettableIterator) {
            ((ResettableIterator) it).reset();
            assertTrue(it.hasNext());
            assertEquals("k1", it.next());
        }

        // Delegate mode iterator
        map.put("k4", "v4");
        map.put("k5", "v5");
        MapIterator delIt = map.mapIterator();
        assertTrue(delIt.hasNext());
    }

    // Tests MapIterator illegal state exceptions
    @Test(expected = IllegalStateException.class)
    public void testMapIterator_getKeyBeforeNext_throwsException() {
        map.put("k1", "v1");
        MapIterator it = map.mapIterator();
        it.getKey();
    }

    // Tests MapIterator no such element exception
    @Test(expected = NoSuchElementException.class)
    public void testMapIterator_nextPastEnd_throwsException() {
        map.put("k1", "v1");
        MapIterator it = map.mapIterator();
        it.next();
        it.next();
    }

    // Tests entrySet, keySet, and values collections and their views
    @Test
    public void testViews_entrySetKeySetValues_consistentWithMap() {
        map.put("k1", "v1");
        map.put("k2", "v2");

        Set entrySet = map.entrySet();
        assertEquals(2, entrySet.size());
        Iterator entryIt = entrySet.iterator();
        assertTrue(entryIt.hasNext());
        Map.Entry entry = (Map.Entry) entryIt.next();
        assertNotNull(entry.getKey());
        assertNotNull(entry.getValue());
        assertEquals(entry, entry);
        assertFalse(entry.equals("not an entry"));

        Set keySet = map.keySet();
        assertEquals(2, keySet.size());
        assertTrue(keySet.contains("k1"));
        assertTrue(keySet.remove("k1"));
        assertEquals(1, map.size());

        Collection values = map.values();
        assertEquals(1, values.size());
        assertTrue(values.contains("v2"));
        values.clear();
        assertTrue(map.isEmpty());
    }

    // Tests equals and hashCode consistency for flat and delegate modes
    @Test
    public void testEqualsAndHashCode_flatAndDelegate_consistentWithMapContract() {
        Flat3Map other = new Flat3Map();
        assertTrue(map.equals(map));
        assertTrue(map.equals(other));
        assertEquals(map.hashCode(), other.hashCode());

        map.put("k1", "v1");
        map.put("k2", "v2");
        other.put("k1", "v1");
        other.put("k2", "v2");

        assertTrue(map.equals(other));
        assertEquals(map.hashCode(), other.hashCode());

        other.put("k2", "different");
        assertFalse(map.equals(other));

        assertFalse(map.equals(null));
        assertFalse(map.equals("string"));

        // Delegate equals
        map.put("k3", "v3");
        map.put("k4", "v4");
        other.put("k3", "v3");
        other.put("k4", "v4");
        other.put("k2", "v2");
        assertTrue(map.equals(other));
        assertEquals(map.hashCode(), other.hashCode());
    }

    // Tests clone creates independent shallow copy
    @Test
    public void testClone_createsShallowCopy() {
        map.put("k1", "v1");
        map.put("k2", "v2");

        Flat3Map cloned = (Flat3Map) map.clone();
        assertEquals(map.size(), cloned.size());
        assertEquals("v1", cloned.get("k1"));

        cloned.put("k1", "modified");
        assertEquals("v1", map.get("k1"));

        map.put("k3", "v3");
        map.put("k4", "v4");
        Flat3Map clonedDelegate = (Flat3Map) map.clone();
        assertEquals(4, clonedDelegate.size());
    }

    // Tests serialization and deserialization
    @Test
    public void testSerialization_readAndWriteObject_restoresState() throws Exception {
        map.put("k1", "v1");
        map.put("k2", "v2");
        map.put("k3", "v3");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(map);
        oos.close();

        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
        Flat3Map deserialized = (Flat3Map) ois.readObject();
        ois.close();

        assertEquals(map.size(), deserialized.size());
        assertEquals("v1", deserialized.get("k1"));
        assertEquals("v2", deserialized.get("k2"));
        assertEquals("v3", deserialized.get("k3"));
    }

    // Tests toString method formatting
    @Test
    public void testToString_formatsCorrectly() {
        assertEquals("{}", map.toString());

        map.put("k1", "v1");
        assertEquals("{k1=v1}", map.toString());

        map.put("k2", "v2");
        assertTrue(map.toString().contains("k1=v1"));
        assertTrue(map.toString().contains("k2=v2"));
    }

    // --- Additional targeted test cases for branch and condition coverage ---

    @Test
    public void testRemove_size1RemoveKey1_andNonExistent() {
        assertNull(map.remove("absent"));

        map.put("k1", "v1");
        assertNull(map.remove("absent"));
        assertEquals("v1", map.remove("k1"));
        assertEquals(0, map.size());

        map.put("k1", "v1");
        map.put("k2", "v2");
        assertNull(map.remove("absent"));

        map.put("k3", "v3");
        assertNull(map.remove("absent"));
    }

    @Test
    public void testGetAndContainsKey_variousPositionsAndNulls() {
        assertNull(map.get("none"));
        assertFalse(map.containsKey("none"));

        map.put("k1", "v1");
        assertEquals("v1", map.get("k1"));
        assertNull(map.get("none"));
        assertTrue(map.containsKey("k1"));
        assertFalse(map.containsKey("none"));

        map.put("k2", "v2");
        assertEquals("v2", map.get("k2"));
        assertNull(map.get("none"));
        assertTrue(map.containsKey("k2"));
        assertFalse(map.containsKey("none"));

        map.put("k3", "v3");
        assertEquals("v3", map.get("k3"));
        assertNull(map.get("none"));
        assertTrue(map.containsKey("k3"));
        assertFalse(map.containsKey("none"));
    }

    @Test
    public void testContainsValue_allPositionsAndDelegate() {
        assertFalse(map.containsValue("v1"));

        map.put("k1", "v1");
        assertTrue(map.containsValue("v1"));
        assertFalse(map.containsValue("v2"));

        map.put("k2", "v2");
        assertTrue(map.containsValue("v1"));
        assertTrue(map.containsValue("v2"));
        assertFalse(map.containsValue("v3"));

        map.put("k3", "v3");
        assertTrue(map.containsValue("v1"));
        assertTrue(map.containsValue("v2"));
        assertTrue(map.containsValue("v3"));
        assertFalse(map.containsValue("v4"));
    }

    @Test(expected = IllegalStateException.class)
    public void testMapIterator_getValueBeforeNext_throwsException() {
        map.put("k1", "v1");
        MapIterator it = map.mapIterator();
        it.getValue();
    }

    @Test(expected = IllegalStateException.class)
    public void testMapIterator_setValueBeforeNext_throwsException() {
        map.put("k1", "v1");
        MapIterator it = map.mapIterator();
        it.setValue("fail");
    }

    @Test(expected = IllegalStateException.class)
    public void testMapIterator_removeBeforeNext_throwsException() {
        map.put("k1", "v1");
        MapIterator it = map.mapIterator();
        it.remove();
    }

    @Test(expected = IllegalStateException.class)
    public void testMapIterator_doubleRemove_throwsException() {
        map.put("k1", "v1");
        MapIterator it = map.mapIterator();
        it.next();
        it.remove();
        it.remove();
    }

    @Test
    public void testEntrySetIterator_removePositionsAndMethods() {
        map.put("k1", "v1");
        map.put("k2", "v2");
        map.put("k3", "v3");

        Set entrySet = map.entrySet();
        Iterator it = entrySet.iterator();

        assertTrue(it.hasNext());
        Map.Entry e1 = (Map.Entry) it.next();
        assertEquals("k1", e1.getKey());
        assertEquals("v1", e1.getValue());
        e1.setValue("newV1");
        assertEquals("newV1", map.get("k1"));

        it.remove();
        assertEquals(2, map.size());
        assertFalse(map.containsKey("k1"));

        Map.Entry e2 = (Map.Entry) it.next();
        assertEquals("k2", e2.getKey());

        Map.Entry e3 = (Map.Entry) it.next();
        assertEquals("k3", e3.getKey());
        it.remove();
        assertEquals(1, map.size());
        assertFalse(map.containsKey("k3"));

        assertFalse(it.hasNext());
    }

    @Test
    public void testKeySetAndValues_methodsAndIterators() {
        map.put("k1", "v1");
        map.put("k2", "v2");
        map.put("k3", "v3");

        Set keySet = map.keySet();
        Iterator keyIt = keySet.iterator();
        assertTrue(keyIt.hasNext());
        assertEquals("k1", keyIt.next());
        keyIt.remove();
        assertEquals(2, map.size());
        assertFalse(map.containsKey("k1"));

        Collection values = map.values();
        Iterator valIt = values.iterator();
        assertTrue(valIt.hasNext());
        assertEquals("v2", valIt.next());
        valIt.remove();
        assertEquals(1, map.size());
        assertFalse(map.containsValue("v2"));
    }

    @Test
    public void testEntrySetContainsAndRemove() {
        map.put("k1", "v1");
        map.put("k2", "v2");

        Set entrySet = map.entrySet();
        Iterator it = entrySet.iterator();
        Map.Entry entry = (Map.Entry) it.next();

        assertTrue(entrySet.contains(entry));
        assertFalse(entrySet.contains("non-entry"));

        Map otherMap = new HashMap();
        otherMap.put("k1", "wrongVal");
        Map.Entry wrongEntry = (Map.Entry) otherMap.entrySet().iterator().next();
        assertFalse(entrySet.contains(wrongEntry));
        assertFalse(entrySet.remove(wrongEntry));

        assertTrue(entrySet.remove(entry));
        assertEquals(1, map.size());
    }

    @Test
    public void testSerialization_delegateMode() throws Exception {
        map.put("k1", "v1");
        map.put("k2", "v2");
        map.put("k3", "v3");
        map.put("k4", "v4");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(map);
        oos.close();

        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
        Flat3Map deserialized = (Flat3Map) ois.readObject();
        ois.close();

        assertEquals(map.size(), deserialized.size());
        assertEquals("v1", deserialized.get("k1"));
        assertEquals("v4", deserialized.get("k4"));
    }

    @Test
    public void testEqualsAndHashCode_withStandardMap() {
        Map stdMap = new HashMap();
        stdMap.put("k1", "v1");
        stdMap.put("k2", "v2");

        map.put("k1", "v1");
        map.put("k2", "v2");

        assertTrue(map.equals(stdMap));
        assertTrue(stdMap.equals(map));
        assertEquals(stdMap.hashCode(), map.hashCode());

        stdMap.put("k3", "v3");
        assertFalse(map.equals(stdMap));
    }
}