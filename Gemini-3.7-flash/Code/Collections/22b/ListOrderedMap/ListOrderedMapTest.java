package org.apache.commons.collections4.map;

import org.apache.commons.collections4.OrderedMapIterator;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ListOrderedMapTest {

    private ListOrderedMap<String, String> map;

    @Before
    public void setUp() {
        map = new ListOrderedMap<String, String>();
    }

    // Tests firstKey on empty map throws NoSuchElementException
    @Test(expected = NoSuchElementException.class)
    public void testFirstKey_emptyMap_throwsNoSuchElementException() {
        map.firstKey();
    }

    // Tests lastKey on empty map throws NoSuchElementException
    @Test(expected = NoSuchElementException.class)
    public void testLastKey_emptyMap_throwsNoSuchElementException() {
        map.lastKey();
    }

    // Tests firstKey and lastKey on non-empty map
    @Test
    public void testFirstKeyAndLastKey_nonEmptyMap_returnsCorrectKeys() {
        map.put("key1", "val1");
        map.put("key2", "val2");
        map.put("key3", "val3");

        assertEquals("key1", map.firstKey());
        assertEquals("key3", map.lastKey());
    }

    // Tests nextKey and previousKey navigation
    @Test
    public void testNextKeyAndPreviousKey_variousKeys_returnsExpected() {
        map.put("a", "1");
        map.put("b", "2");
        map.put("c", "3");

        assertEquals("b", map.nextKey("a"));
        assertEquals("c", map.nextKey("b"));
        assertNull(map.nextKey("c"));
        assertNull(map.nextKey("unknown"));

        assertNull(map.previousKey("a"));
        assertEquals("a", map.previousKey("b"));
        assertEquals("b", map.previousKey("c"));
        assertNull(map.previousKey("unknown"));
    }

    // Tests put with new and re-added keys
    @Test
    public void testPut_newAndExistingKeys_maintainsOriginalOrder() {
        map.put("a", "1");
        map.put("b", "2");
        map.put("c", "3");

        // Re-adding existing key should not change position
        final String oldVal = map.put("b", "updated");
        assertEquals("2", oldVal);
        assertEquals(3, map.size());
        assertEquals("a", map.get(0));
        assertEquals("b", map.get(1));
        assertEquals("c", map.get(2));
        assertEquals("updated", map.getValue(1));
    }

    // Tests put at specific index with a new key
    @Test
    public void testPutAtIndex_newKey_insertsAtPosition() {
        map.put("a", "1");
        map.put("c", "3");

        final String result = map.put(1, "b", "2");
        assertNull(result);
        assertEquals(3, map.size());
        assertEquals("a", map.get(0));
        assertEquals("b", map.get(1));
        assertEquals("c", map.get(2));
    }

    // Tests put at specific index with an existing key moving position
    @Test
    public void testPutAtIndex_existingKeyMoved_reordersProperly() {
        map.put("a", "1");
        map.put("b", "2");
        map.put("c", "3");

        // Move 'a' from index 0 to index 2
        final String old = map.put(2, "a", "1_new");
        assertEquals("1", old);
        assertEquals(3, map.size());
        assertEquals("b", map.get(0));
        assertEquals("a", map.get(1));
        assertEquals("c", map.get(2));

        // Move 'c' from index 2 to index 0
        final String oldC = map.put(0, "c", "3_new");
        assertEquals("3", oldC);
        assertEquals("c", map.get(0));
        assertEquals("b", map.get(1));
        assertEquals("a", map.get(2));
    }

    // Tests putAll into map
    @Test
    public void testPutAll_standardMap_preservesOrder() {
        final Map<String, String> other = new LinkedHashMap<String, String>();
        other.put("k1", "v1");
        other.put("k2", "v2");

        map.putAll(other);
        assertEquals(2, map.size());
        assertEquals("k1", map.get(0));
        assertEquals("k2", map.get(1));
    }

    // Tests putAll at specific index with new keys
    @Test
    public void testPutAllAtIndex_newKeys_insertsCorrectly() {
        map.put("k1", "v1");
        map.put("k4", "v4");

        final Map<String, String> insert = new LinkedHashMap<String, String>();
        insert.put("k2", "v2");
        insert.put("k3", "v3");

        map.putAll(1, insert);
        assertEquals(4, map.size());
        assertEquals("k1", map.get(0));
        assertEquals("k2", map.get(1));
        assertEquals("k3", map.get(2));
        assertEquals("k4", map.get(3));
    }

    // Tests putAll at index when replacing existing keys and null values
    @Test
    public void testPutAllAtIndex_existingKeysAndNullValues_ordersCorrectly() {
        map.put("k1", null);
        map.put("k2", "v2");
        map.put("k3", "v3");

        final Map<String, String> insert = new LinkedHashMap<String, String>();
        insert.put("k1", "v1_updated");
        insert.put("k4", "v4");

        map.putAll(1, insert);
        assertEquals(4, map.size());
        assertEquals("v1_updated", map.get("k1"));
        assertEquals("v4", map.get("k4"));
    }

    // Tests get, getValue, indexOf, and setValue by index
    @Test
    public void testGetAndSetValueByIndex_validIndices_returnsCorrectValues() {
        map.put("x", "10");
        map.put("y", "20");

        assertEquals("x", map.get(0));
        assertEquals("10", map.getValue(0));
        assertEquals(0, map.indexOf("x"));
        assertEquals(1, map.indexOf("y"));
        assertEquals(-1, map.indexOf("nonexistent"));

        final String old = map.setValue(1, "200");
        assertEquals("20", old);
        assertEquals("200", map.getValue(1));
    }

    // Tests remove by index and by key
    @Test
    public void testRemove_byKeyAndIndex_removesElementsCorrectly() {
        map.put("a", "1");
        map.put("b", "2");
        map.put("c", "3");

        final String removedVal = map.remove(1);
        assertEquals("2", removedVal);
        assertEquals(2, map.size());
        assertEquals("a", map.get(0));
        assertEquals("c", map.get(1));

        final String removedKeyVal = map.remove("a");
        assertEquals("1", removedKeyVal);
        assertNull(map.remove("nonexistent"));
        assertEquals(1, map.size());
        assertEquals("c", map.get(0));
    }

    // Tests clear method
    @Test
    public void testClear_nonEmptyMap_becomesEmpty() {
        map.put("a", "1");
        map.put("b", "2");
        map.clear();

        assertEquals(0, map.size());
        assertTrue(map.isEmpty());
        assertEquals(-1, map.indexOf("a"));
    }

    // Tests keyList and asList views
    @Test
    public void testKeyListAndAsList_viewIntegrity() {
        map.put("a", "1");
        map.put("b", "2");

        final List<String> keys = map.keyList();
        assertEquals(2, keys.size());
        assertEquals("a", keys.get(0));
        assertEquals("b", keys.get(1));
        assertEquals(keys, map.asList());
    }

    // Tests values and valueList views
    @Test
    public void testValuesAndValueList_operations() {
        map.put("a", "1");
        map.put("b", "2");
        map.put("c", "3");

        final List<String> valList = map.valueList();
        assertEquals(3, valList.size());
        assertEquals("1", valList.get(0));
        assertTrue(valList.contains("2"));
        assertFalse(valList.contains("99"));

        valList.set(1, "22");
        assertEquals("22", map.get("b"));

        valList.remove(1);
        assertEquals(2, map.size());
        assertFalse(map.containsKey("b"));

        valList.clear();
        assertEquals(0, map.size());
    }

    // Tests keySet view operations
    @Test
    public void testKeySetView_containsAndIteration() {
        map.put("a", "1");
        map.put("b", "2");

        final Set<String> keySet = map.keySet();
        assertEquals(2, keySet.size());
        assertTrue(keySet.contains("a"));
        assertFalse(keySet.contains("c"));

        final Iterator<String> it = keySet.iterator();
        assertTrue(it.hasNext());
        assertEquals("a", it.next());
        assertEquals("b", it.next());
        assertFalse(it.hasNext());

        keySet.clear();
        assertEquals(0, map.size());
    }

    // Tests entrySet view methods
    @Test
    public void testEntrySetView_operations() {
        map.put("a", "1");
        map.put("b", "2");

        final Set<Map.Entry<String, String>> entries = map.entrySet();
        assertEquals(2, entries.size());
        assertFalse(entries.isEmpty());

        final Iterator<Map.Entry<String, String>> it = entries.iterator();
        final Map.Entry<String, String> entry = it.next();
        assertEquals("a", entry.getKey());
        assertEquals("1", entry.getValue());
        entry.setValue("100");
        assertEquals("100", map.get("a"));

        it.remove();
        assertEquals(1, map.size());
        assertFalse(map.containsKey("a"));

        assertFalse(entries.remove("not an entry"));
    }

    // Tests mapIterator forward, backward, set, remove, and reset
    @Test
    public void testMapIterator_navigationAndModification() {
        map.put("a", "1");
        map.put("b", "2");

        final OrderedMapIterator<String, String> it = map.mapIterator();
        assertFalse(it.hasPrevious());
        assertTrue(it.hasNext());

        assertEquals("a", it.next());
        assertEquals("a", it.getKey());
        assertEquals("1", it.getValue());

        it.setValue("10");
        assertEquals("10", map.get("a"));

        assertTrue(it.hasNext());
        assertEquals("b", it.next());
        assertTrue(it.hasPrevious());
        assertEquals("b", it.previous());

        it.reset();
        assertFalse(it.hasPrevious());

        assertEquals("a", it.next());
        it.remove();
        assertEquals(1, map.size());
        assertFalse(map.containsKey("a"));
    }

    // Tests mapIterator invalid state exceptions
    @Test(expected = IllegalStateException.class)
    public void testMapIterator_getKeyWithoutNext_throwsIllegalStateException() {
        final OrderedMapIterator<String, String> it = map.mapIterator();
        it.getKey();
    }

    // Tests factory method listOrderedMap
    @Test
    public void testFactoryMethod_validMap_createsListOrderedMap() {
        final Map<String, String> base = new HashMap<String, String>();
        base.put("k", "v");
        final ListOrderedMap<String, String> created = ListOrderedMap.listOrderedMap(base);
        assertEquals(1, created.size());
        assertEquals("k", created.firstKey());
    }

    // Tests toString for empty and non-empty map
    @Test
    public void testToString_emptyAndPopulated() {
        assertEquals("{}", map.toString());
        map.put("k1", "v1");
        map.put("k2", "v2");
        assertEquals("{k1=v1, k2=v2}", map.toString());
    }

    // Tests factory method with null argument throws NullPointerException
    @Test(expected = NullPointerException.class)
    public void testFactoryMethod_nullMap_throwsNullPointerException() {
        ListOrderedMap.listOrderedMap(null);
    }

    // Tests put at invalid index throws IndexOutOfBoundsException
    @Test(expected = IndexOutOfBoundsException.class)
    public void testPutAtIndex_negativeIndex_throwsIndexOutOfBoundsException() {
        map.put(-1, "k", "v");
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testPutAtIndex_indexGreaterThanSize_throwsIndexOutOfBoundsException() {
        map.put(1, "k", "v");
    }

    // Tests putAll at invalid index throws IndexOutOfBoundsException
    @Test(expected = IndexOutOfBoundsException.class)
    public void testPutAllAtIndex_negativeIndex_throwsIndexOutOfBoundsException() {
        map.putAll(-1, new HashMap<String, String>());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testPutAllAtIndex_indexGreaterThanSize_throwsIndexOutOfBoundsException() {
        map.putAll(1, new HashMap<String, String>());
    }

    // Tests get, getValue, setValue, and remove by invalid index throws IndexOutOfBoundsException
    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetByIndex_outOfBounds_throwsIndexOutOfBoundsException() {
        map.get(0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValueByIndex_outOfBounds_throwsIndexOutOfBoundsException() {
        map.getValue(0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSetValueByIndex_outOfBounds_throwsIndexOutOfBoundsException() {
        map.setValue(0, "val");
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testRemoveByIndex_outOfBounds_throwsIndexOutOfBoundsException() {
        map.remove(0);
    }

    // Tests mapIterator exceptions for getValue, setValue, and remove before next
    @Test(expected = IllegalStateException.class)
    public void testMapIterator_getValueWithoutNext_throwsIllegalStateException() {
        final OrderedMapIterator<String, String> it = map.mapIterator();
        it.getValue();
    }

    @Test(expected = IllegalStateException.class)
    public void testMapIterator_setValueWithoutNext_throwsIllegalStateException() {
        final OrderedMapIterator<String, String> it = map.mapIterator();
        it.setValue("v");
    }

    @Test(expected = IllegalStateException.class)
    public void testMapIterator_removeWithoutNext_throwsIllegalStateException() {
        final OrderedMapIterator<String, String> it = map.mapIterator();
        it.remove();
    }

    @Test(expected = NoSuchElementException.class)
    public void testMapIterator_nextPastEnd_throwsNoSuchElementException() {
        final OrderedMapIterator<String, String> it = map.mapIterator();
        it.next();
    }

    @Test(expected = NoSuchElementException.class)
    public void testMapIterator_previousBeforeStart_throwsNoSuchElementException() {
        final OrderedMapIterator<String, String> it = map.mapIterator();
        it.previous();
    }

    // Tests mapIterator toString format
    @Test
    public void testMapIterator_toString() {
        map.put("a", "1");
        final OrderedMapIterator<String, String> it = map.mapIterator();
        assertEquals("Iterator[]", it.toString());
        it.next();
        assertEquals("Iterator[a=1]", it.toString());
    }

    // Tests values view methods
    @Test
    public void testValues_collectionOperations() {
        map.put("a", "1");
        map.put("b", "2");

        final Collection<String> vals = map.values();
        assertEquals(2, vals.size());
        assertTrue(vals.contains("1"));
        assertFalse(vals.contains("3"));

        final Iterator<String> it = vals.iterator();
        assertTrue(it.hasNext());
        assertEquals("1", it.next());
        it.remove();

        assertEquals(1, map.size());
        assertFalse(map.containsKey("a"));

        vals.clear();
        assertEquals(0, map.size());
    }

    // Tests entrySet view remove matching and non-matching entries
    @Test
    public void testEntrySet_removeEntry() {
        map.put("a", "1");
        map.put("b", "2");

        final Set<Map.Entry<String, String>> entrySet = map.entrySet();
        final Map.Entry<String, String> toRemove = entrySet.iterator().next();
        assertTrue(entrySet.remove(toRemove));
        assertEquals(1, map.size());
        assertFalse(map.containsKey("a"));
    }
}