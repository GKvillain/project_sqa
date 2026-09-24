package org.apache.commons.collections.map;

import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.ResettableIterator;
import org.junit.Test;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class Flat3MapTest {

    // Tests defect Collections 1b: remove key2 when size is 3 must return value2, not value3
    @Test
    public void testRemove_sizeThreeRemoveKey2_returnsCorrectValue() {
        Flat3Map map = new Flat3Map();
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

    // Tests defect Collections 1b: remove key1 when size is 3 must return value1, not value3
    @Test
    public void testRemove_sizeThreeRemoveKey1_returnsCorrectValue() {
        Flat3Map map = new Flat3Map();
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

    // Tests defect Collections 1b: remove null key at pos2/pos1 when size is 3
    @Test
    public void testRemove_sizeThreeWithNullKey_returnsCorrectValue() {
        Flat3Map map = new Flat3Map();
        map.put("key1", "val1");
        map.put(null, "val2");
        map.put("key3", "val3");

        Object removed = map.remove(null);
        assertEquals("val2", removed);
        assertEquals(2, map.size());
        assertFalse(map.containsKey(null));
        assertEquals("val1", map.get("key1"));
        assertEquals("val3", map.get("key3"));
    }

    // Tests basic put and get operations for size 0 to 3 (flat mode)
    @Test
    public void testPutAndGet_flatMode_successfulMappings() {
        Flat3Map map = new Flat3Map();
        assertTrue(map.isEmpty());
        assertEquals(0, map.size());

        assertNull(map.put("A", "1"));
        assertEquals(1, map.size());
        assertEquals("1", map.get("A"));

        assertNull(map.put("B", "2"));
        assertEquals(2, map.size());
        assertEquals("2", map.get("B"));

        assertNull(map.put("C", "3"));
        assertEquals(3, map.size());
        assertEquals("3", map.get("C"));

        // Update existing key
        assertEquals("2", map.put("B", "22"));
        assertEquals(3, map.size());
        assertEquals("22", map.get("B"));

        assertNull(map.get("NonExistent"));
    }

    // Tests map growth beyond 3 switching to delegate mode
    @Test
    public void testPut_exceedingThreeItems_switchesToDelegateMap() {
        Flat3Map map = new Flat3Map();
        map.put("k1", "v1");
        map.put("k2", "v2");
        map.put("k3", "v3");
        map.put("k4", "v4"); // triggers convertToMap()

        assertEquals(4, map.size());
        assertEquals("v1", map.get("k1"));
        assertEquals("v2", map.get("k2"));
        assertEquals("v3", map.get("k3"));
        assertEquals("v4", map.get("k4"));

        // Operations in delegate mode
        assertTrue(map.containsKey("k4"));
        assertTrue(map.containsValue("v4"));
        assertEquals("v4", map.remove("k4"));
        assertEquals(3, map.size());
    }

    // Tests remove at size 1 and size 2 (including shift logic)
    @Test
    public void testRemove_sizeOneAndTwo_removesAndShiftsCorrectly() {
        Flat3Map map = new Flat3Map();
        map.put("k1", "v1");
        map.put("k2", "v2");

        // Remove key1 from size 2 (should shift key2 into slot 1)
        assertEquals("v1", map.remove("k1"));
        assertEquals(1, map.size());
        assertNull(map.get("k1"));
        assertEquals("v2", map.get("k2"));

        // Remove key2 from size 1
        assertEquals("v2", map.remove("k2"));
        assertEquals(0, map.size());
        assertTrue(map.isEmpty());

        // Remove from empty map returns null
        assertNull(map.remove("k2"));
    }

    // Tests null key handling in put, get, containsKey, and remove
    @Test
    public void testNullKeyHandling_flatMode() {
        Flat3Map map = new Flat3Map();
        map.put(null, "nullVal");
        assertEquals(1, map.size());
        assertTrue(map.containsKey(null));
        assertEquals("nullVal", map.get(null));

        // Update null key
        assertEquals("nullVal", map.put(null, "newNullVal"));
        assertEquals("newNullVal", map.get(null));

        assertEquals("newNullVal", map.remove(null));
        assertFalse(map.containsKey(null));
        assertNull(map.get(null));
    }

    // Tests containsValue for flat mode and null values
    @Test
    public void testContainsValue_flatMode() {
        Flat3Map map = new Flat3Map();
        map.put("k1", "v1");
        map.put("k2", null);

        assertTrue(map.containsValue("v1"));
        assertTrue(map.containsValue(null));
        assertFalse(map.containsValue("v2"));
    }

    // Tests copy constructor and putAll
    @Test
    public void testPutAllAndConstructor_validMaps() {
        Map source = new HashMap();
        source.put("a", "1");
        source.put("b", "2");

        Flat3Map map = new Flat3Map(source);
        assertEquals(2, map.size());
        assertEquals("1", map.get("a"));
        assertEquals("2", map.get("b"));

        Map largeSource = new HashMap();
        largeSource.put("c", "3");
        largeSource.put("d", "4");
        largeSource.put("e", "5");
        largeSource.put("f", "6");

        map.putAll(largeSource);
        assertEquals(6, map.size());
        assertEquals("6", map.get("f"));
    }

    // Tests clear method switching back from delegate mode to flat mode
    @Test
    public void testClear_delegateMode_resetsToFlatMode() {
        Flat3Map map = new Flat3Map();
        for (int i = 0; i < 5; i++) {
            map.put("key" + i, "val" + i);
        }
        assertEquals(5, map.size());

        map.clear();
        assertEquals(0, map.size());
        assertTrue(map.isEmpty());

        // Ensure subsequent put goes to flat mode
        map.put("newKey", "newVal");
        assertEquals(1, map.size());
        assertEquals("newVal", map.get("newKey"));
    }

    // Tests MapIterator forward iteration, setValue, remove, and reset
    @Test
    public void testMapIterator_flatMode_traverseAndMutate() {
        Flat3Map map = new Flat3Map();
        map.put("k1", "v1");
        map.put("k2", "v2");
        map.put("k3", "v3");

        MapIterator it = map.mapIterator();
        assertTrue(it.hasNext());

        assertEquals("k1", it.next());
        assertEquals("v1", it.getValue());
        it.setValue("v1_mod");
        assertEquals("v1_mod", map.get("k1"));

        assertEquals("k2", it.next());
        it.remove(); // removes k2
        assertEquals(2, map.size());
        assertNull(map.get("k2"));

        ((ResettableIterator) it).reset();
        assertTrue(it.hasNext());
        assertEquals("k1", it.next());
    }

    // Tests MapIterator exception cases (calling get/set before next, calling next when exhausted)
    @Test(expected = IllegalStateException.class)
    public void testMapIterator_getKeyWithoutNext_throwsException() {
        Flat3Map map = new Flat3Map();
        map.put("k1", "v1");
        MapIterator it = map.mapIterator();
        it.getKey();
    }

    // Tests MapIterator next past end throws NoSuchElementException
    @Test(expected = NoSuchElementException.class)
    public void testMapIterator_nextPastEnd_throwsException() {
        Flat3Map map = new Flat3Map();
        map.put("k1", "v1");
        MapIterator it = map.mapIterator();
        it.next();
        it.next();
    }

    // Tests EntrySet, KeySet, and Values views
    @Test
    public void testViews_flatMode_containsAndRemove() {
        Flat3Map map = new Flat3Map();
        map.put("k1", "v1");
        map.put("k2", "v2");

        Set entrySet = map.entrySet();
        Set keySet = map.keySet();
        Collection values = map.values();

        assertEquals(2, entrySet.size());
        assertEquals(2, keySet.size());
        assertEquals(2, values.size());

        assertTrue(keySet.contains("k1"));
        assertTrue(values.contains("v2"));

        keySet.remove("k1");
        assertEquals(1, map.size());
        assertFalse(map.containsKey("k1"));

        entrySet.clear();
        assertEquals(0, map.size());
    }

    // Tests EntrySet iterator equals, hashCode, and toString
    @Test
    public void testEntrySetIterator_entryMethods() {
        Flat3Map map = new Flat3Map();
        map.put("k1", "v1");

        Iterator it = map.entrySet().iterator();
        assertTrue(it.hasNext());
        Map.Entry entry = (Map.Entry) it.next();

        assertEquals("k1", entry.getKey());
        assertEquals("v1", entry.getValue());
        assertEquals("k1=v1", entry.toString());

        Map other = new HashMap();
        other.put("k1", "v1");
        Map.Entry otherEntry = (Map.Entry) other.entrySet().iterator().next();

        assertTrue(entry.equals(otherEntry));
        assertEquals(otherEntry.hashCode(), entry.hashCode());
    }

    // Tests equals and hashCode between Flat3Map and other Maps
    @Test
    public void testEqualsAndHashCode_equalMaps_match() {
        Flat3Map map1 = new Flat3Map();
        map1.put("k1", "v1");
        map1.put("k2", "v2");

        Flat3Map map2 = new Flat3Map();
        map2.put("k1", "v1");
        map2.put("k2", "v2");

        Map hashMap = new HashMap();
        hashMap.put("k1", "v1");
        hashMap.put("k2", "v2");

        assertTrue(map1.equals(map1));
        assertTrue(map1.equals(map2));
        assertTrue(map1.equals(hashMap));
        assertEquals(map1.hashCode(), map2.hashCode());
        assertEquals(map1.hashCode(), hashMap.hashCode());

        map2.put("k3", "v3");
        assertFalse(map1.equals(map2));
        assertFalse(map1.equals("NotAMap"));
    }

    // Tests clone in both flat mode and delegate mode
    @Test
    public void testClone_flatAndDelegateModes() {
        Flat3Map flatMap = new Flat3Map();
        flatMap.put("k1", "v1");
        Flat3Map clonedFlat = (Flat3Map) flatMap.clone();
        assertEquals(1, clonedFlat.size());
        assertEquals("v1", clonedFlat.get("k1"));

        for (int i = 0; i < 5; i++) {
            flatMap.put("key" + i, "val" + i);
        }
        Flat3Map clonedDelegate = (Flat3Map) flatMap.clone();
        assertEquals(5, clonedDelegate.size());
        assertEquals("val0", clonedDelegate.get("key0"));
    }

    // Tests serialization and deserialization
    @Test
    public void testSerialization_roundTrip_reconstructsMap() throws Exception {
        Flat3Map map = new Flat3Map();
        map.put("k1", "v1");
        map.put("k2", "v2");
        map.put("k3", "v3");
        map.put("k4", "v4"); // serialized in delegate mode

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(map);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Flat3Map deserialized = (Flat3Map) ois.readObject();
        ois.close();

        assertEquals(4, deserialized.size());
        assertEquals("v1", deserialized.get("k1"));
        assertEquals("v4", deserialized.get("k4"));
    }

    // Tests toString in empty and populated states
    @Test
    public void testToString_formatsCorrectly() {
        Flat3Map map = new Flat3Map();
        assertEquals("{}", map.toString());

        map.put("k1", "v1");
        assertEquals("{k1=v1}", map.toString());
    }

    // --- New Tests for Full Coverage ---

    @Test
    public void testSerialization_flatMode_roundTrip() throws Exception {
        Flat3Map map = new Flat3Map();
        map.put("k1", "v1");
        map.put("k2", "v2");
        map.put("k3", "v3");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(map);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Flat3Map deserialized = (Flat3Map) ois.readObject();
        ois.close();

        assertEquals(3, deserialized.size());
        assertEquals("v1", deserialized.get("k1"));
        assertEquals("v2", deserialized.get("k2"));
        assertEquals("v3", deserialized.get("k3"));
    }

    @Test
    public void testRemove_sizeThreeRemoveKey3() {
        Flat3Map map = new Flat3Map();
        map.put("k1", "v1");
        map.put("k2", "v2");
        map.put("k3", "v3");

        assertEquals("v3", map.remove("k3"));
        assertEquals(2, map.size());
        assertEquals("v1", map.get("k1"));
        assertEquals("v2", map.get("k2"));
        assertNull(map.get("k3"));

        // Remove non-existent key from size 2
        assertNull(map.remove("k4"));
        assertEquals(2, map.size());
    }

    @Test
    public void testRemove_nullKeyAtPosition3() {
        Flat3Map map = new Flat3Map();
        map.put("k1", "v1");
        map.put("k2", "v2");
        map.put(null, "v3");

        assertTrue(map.containsKey(null));
        assertEquals("v3", map.remove(null));
        assertEquals(2, map.size());
        assertFalse(map.containsKey(null));
    }

    @Test
    public void testRemove_nullKeyAtPosition1() {
        Flat3Map map = new Flat3Map();
        map.put(null, "v1");
        map.put("k2", "v2");
        map.put("k3", "v3");

        assertEquals("v1", map.remove(null));
        assertEquals(2, map.size());
        assertFalse(map.containsKey(null));
        assertEquals("v2", map.get("k2"));
        assertEquals("v3", map.get("k3"));
    }

    @Test
    public void testRemove_nullKeySizeOneAndTwo() {
        Flat3Map map = new Flat3Map();
        map.put(null, "v1");
        map.put("k2", "v2");

        assertEquals("v1", map.remove(null));
        assertEquals(1, map.size());
        assertEquals("v2", map.get("k2"));

        map.put(null, "v1_new");
        assertEquals("v2", map.remove("k2"));
        assertEquals(1, map.size());
        assertEquals("v1_new", map.remove(null));
        assertEquals(0, map.size());
    }

    @Test
    public void testPut_overwriteAtEachPosition() {
        Flat3Map map = new Flat3Map();
        map.put("k1", "v1");
        assertEquals("v1", map.put("k1", "v1_updated"));
        assertEquals("v1_updated", map.get("k1"));

        map.put("k2", "v2");
        assertEquals("v1_updated", map.put("k1", "v1_updated2"));
        assertEquals("v2", map.put("k2", "v2_updated"));

        map.put("k3", "v3");
        assertEquals("v1_updated2", map.put("k1", "v1_final"));
        assertEquals("v2_updated", map.put("k2", "v2_final"));
        assertEquals("v3", map.put("k3", "v3_final"));
        assertEquals(3, map.size());
    }

    @Test
    public void testPut_nullKeyOverwritesAtPosition123() {
        Flat3Map map = new Flat3Map();
        map.put(null, "v0");
        map.put("k1", "v1");
        map.put("k2", "v2");
        assertEquals("v0", map.put(null, "v0_new"));

        Flat3Map map2 = new Flat3Map();
        map2.put("k1", "v1");
        map2.put(null, "v0");
        map2.put("k2", "v2");
        assertEquals("v0", map2.put(null, "v0_new"));

        Flat3Map map3 = new Flat3Map();
        map3.put("k1", "v1");
        map3.put("k2", "v2");
        map3.put(null, "v0");
        assertEquals("v0", map3.put(null, "v0_new"));
    }

    @Test
    public void testContainsKeyAndGet_variations() {
        Flat3Map map = new Flat3Map();
        assertFalse(map.containsKey("k1"));
        assertNull(map.get("k1"));

        map.put("k1", "v1");
        assertTrue(map.containsKey("k1"));
        assertFalse(map.containsKey("k2"));
        assertFalse(map.containsKey(null));
        assertNull(map.get(null));

        map.put("k2", "v2");
        assertTrue(map.containsKey("k2"));
        assertFalse(map.containsKey("k3"));
        assertEquals("v2", map.get("k2"));

        map.put("k3", "v3");
        assertTrue(map.containsKey("k3"));
        assertFalse(map.containsKey("k4"));
        assertEquals("v3", map.get("k3"));
    }

    @Test
    public void testContainsKey_nullKeyVariations() {
        Flat3Map map = new Flat3Map();
        map.put("k1", "v1");
        map.put("k2", "v2");
        map.put(null, "v3");
        assertTrue(map.containsKey(null));

        Flat3Map map2 = new Flat3Map();
        map2.put(null, "v1");
        map2.put("k2", "v2");
        assertTrue(map2.containsKey(null));
        assertEquals("v1", map2.get(null));
    }

    @Test
    public void testContainsValue_allPositions() {
        Flat3Map map = new Flat3Map();
        assertFalse(map.containsValue("v1"));
        assertFalse(map.containsValue(null));

        map.put("k1", "v1");
        assertTrue(map.containsValue("v1"));
        assertFalse(map.containsValue("v2"));

        map.put("k2", "v2");
        assertTrue(map.containsValue("v2"));

        map.put("k3", "v3");
        assertTrue(map.containsValue("v3"));
        assertFalse(map.containsValue("v4"));

        // With null value at position 3
        map.put("k3", null);
        assertTrue(map.containsValue(null));

        // With null value at position 1
        map.put("k1", null);
        assertTrue(map.containsValue(null));
    }

    @Test
    public void testToString_multipleElementsAndDelegate() {
        Flat3Map map = new Flat3Map();
        map.put("k1", "v1");
        map.put("k2", "v2");
        assertTrue(map.toString().contains("k1=v1"));
        assertTrue(map.toString().contains("k2=v2"));

        map.put("k3", "v3");
        assertTrue(map.toString().contains("k3=v3"));

        map.put("k4", "v4");
        assertTrue(map.toString().contains("k4=v4"));
    }

    @Test
    public void testMapIterator_delegateMode() {
        Flat3Map map = new Flat3Map();
        for (int i = 0; i < 5; i++) {
            map.put("k" + i, "v" + i);
        }
        MapIterator it = map.mapIterator();
        int count = 0;
        while (it.hasNext()) {
            it.next();
            assertNotNull(it.getKey());
            assertNotNull(it.getValue());
            count++;
        }
        assertEquals(5, count);
    }

    @Test(expected = IllegalStateException.class)
    public void testMapIterator_getValueWithoutNext_throwsException() {
        Flat3Map map = new Flat3Map();
        map.put("k1", "v1");
        MapIterator it = map.mapIterator();
        it.getValue();
    }

    @Test(expected = IllegalStateException.class)
    public void testMapIterator_setValueWithoutNext_throwsException() {
        Flat3Map map = new Flat3Map();
        map.put("k1", "v1");
        MapIterator it = map.mapIterator();
        it.setValue("v");
    }

    @Test(expected = IllegalStateException.class)
    public void testMapIterator_removeWithoutNext_throwsException() {
        Flat3Map map = new Flat3Map();
        map.put("k1", "v1");
        MapIterator it = map.mapIterator();
        it.remove();
    }

    @Test(expected = IllegalStateException.class)
    public void testMapIterator_removeTwice_throwsException() {
        Flat3Map map = new Flat3Map();
        map.put("k1", "v1");
        MapIterator it = map.mapIterator();
        it.next();
        it.remove();
        it.remove();
    }

    @Test
    public void testMapIterator_removeAtPos1AndPos3() {
        Flat3Map map = new Flat3Map();
        map.put("k1", "v1");
        map.put("k2", "v2");
        map.put("k3", "v3");

        MapIterator it = map.mapIterator();
        assertEquals("k1", it.next());
        it.remove(); // removes pos 1
        assertEquals(2, map.size());
        assertNull(map.get("k1"));

        assertEquals("k2", it.next());
        assertEquals("k3", it.next());
        it.remove(); // removes pos 3 (which is now in pos 2)
        assertEquals(1, map.size());
        assertNull(map.get("k3"));
        assertEquals("v2", map.get("k2"));
    }

    @Test
    public void testKeySetIterator_fullLifecycle() {
        Flat3Map map = new Flat3Map();
        map.put("k1", "v1");
        map.put("k2", "v2");

        Set keys = map.keySet();
        Iterator it = keys.iterator();
        assertTrue(it.hasNext());
        assertEquals("k1", it.next());
        it.remove();
        assertEquals(1, map.size());
        assertFalse(map.containsKey("k1"));

        assertEquals("k2", it.next());
        assertFalse(it.hasNext());
    }

    @Test
    public void testValuesIterator_fullLifecycle() {
        Flat3Map map = new Flat3Map();
        map.put("k1", "v1");
        map.put("k2", "v2");

        Collection vals = map.values();
        Iterator it = vals.iterator();
        assertTrue(it.hasNext());
        assertEquals("v1", it.next());
        it.remove();
        assertEquals(1, map.size());
        assertFalse(map.containsValue("v1"));

        assertEquals("v2", it.next());
        assertFalse(it.hasNext());
    }

    @Test
    public void testEntrySet_containsAndRemoveEntries() {
        Flat3Map map = new Flat3Map();
        map.put("k1", "v1");
        map.put("k2", "v2");

        Set entrySet = map.entrySet();
        Iterator it = entrySet.iterator();
        Map.Entry entry = (Map.Entry) it.next();

        assertTrue(entrySet.contains(entry));
        assertFalse(entrySet.contains("notAnEntry"));

        Map otherMap = new HashMap();
        otherMap.put("k1", "differentVal");
        Map.Entry otherEntry = (Map.Entry) otherMap.entrySet().iterator().next();
        assertFalse(entrySet.contains(otherEntry));

        assertFalse(entrySet.remove("notAnEntry"));
        assertFalse(entrySet.remove(otherEntry));
        assertTrue(entrySet.remove(entry));
        assertEquals(1, map.size());
    }

    @Test
    public void testFlatMapEntry_setValueAndEquals() {
        Flat3Map map = new Flat3Map();
        map.put("k1", "v1");

        Iterator it = map.entrySet().iterator();
        Map.Entry entry = (Map.Entry) it.next();

        assertEquals("v1", entry.setValue("v1_new"));
        assertEquals("v1_new", map.get("k1"));

        assertFalse(entry.equals("notAnEntry"));

        Map mapWithNull = new Flat3Map();
        mapWithNull.put(null, null);
        Map.Entry nullEntry = (Map.Entry) mapWithNull.entrySet().iterator().next();
        assertEquals(0, nullEntry.hashCode());
        assertTrue(nullEntry.equals(nullEntry));
    }

    @Test
    public void testDelegateMode_viewsAndOperations() {
        Flat3Map map = new Flat3Map();
        for (int i = 0; i < 5; i++) {
            map.put("k" + i, "v" + i);
        }

        assertTrue(map.keySet().contains("k0"));
        assertTrue(map.values().contains("v0"));
        assertEquals(5, map.entrySet().size());
        assertEquals(5, map.keySet().size());
        assertEquals(5, map.values().size());

        map.keySet().remove("k0");
        assertEquals(4, map.size());
        assertFalse(map.containsKey("k0"));

        map.values().remove("v1");
        assertEquals(3, map.size());
        assertFalse(map.containsValue("v1"));
    }

    @Test
    public void testEqualsAndHashCode_cornerCases() {
        Flat3Map map1 = new Flat3Map();
        map1.put("k1", null);

        Flat3Map map2 = new Flat3Map();
        map2.put("k1", "notNull");

        assertFalse(map1.equals(map2));

        Flat3Map map3 = new Flat3Map();
        map3.put("differentKey", null);
        assertFalse(map1.equals(map3));

        Flat3Map empty1 = new Flat3Map();
        Flat3Map empty2 = new Flat3Map();
        assertTrue(empty1.equals(empty2));
        assertEquals(empty1.hashCode(), empty2.hashCode());
    }
}