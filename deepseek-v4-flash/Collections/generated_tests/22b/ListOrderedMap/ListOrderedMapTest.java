package org.apache.commons.collections4.map;

import static org.junit.Assert.*;
import java.util.*;
import org.junit.Test;
import org.apache.commons.collections4.OrderedMapIterator;
import org.apache.commons.collections4.map.ListOrderedMap;

public class ListOrderedMapTest {

    // ================== existing tests ==================

    @Test
    public void testPut_newKey_addsToEnd() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("a", "1");
        assertEquals(1, map.size());
        assertEquals("a", map.get(0));
        assertEquals("1", map.get("a"));
    }

    @Test
    public void testPut_existingKey_doesNotChangeOrder() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("a", "1");
        map.put("b", "2");
        map.put("a", "3");
        assertEquals(2, map.size());
        assertEquals("a", map.get(0));
        assertEquals("b", map.get(1));
        assertEquals("3", map.get("a"));
    }

    @Test
    public void testPutAtIndex_newKey_insertsCorrectly() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("a", "1");
        map.put("c", "3");
        map.put(1, "b", "2");
        assertEquals(3, map.size());
        assertEquals("a", map.get(0));
        assertEquals("b", map.get(1));
        assertEquals("c", map.get(2));
    }

    @Test
    public void testPutAtIndex_existingKey_movesToIndex() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("a", "1");
        map.put("b", "2");
        map.put("c", "3");
        map.put(0, "b", "4");
        assertEquals("b", map.get(0));
        assertEquals("a", map.get(1));
        assertEquals("c", map.get(2));
        assertEquals("4", map.get("b"));
    }

    @Test
    public void testPutAtIndex_existingKey_indexGreaterThanPos() {
        // covers branch where pos < index
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("a", "1");
        map.put("b", "2");
        map.put("c", "3");
        map.put(2, "a", "5");
        assertEquals("b", map.get(0));
        assertEquals("a", map.get(1));
        assertEquals("c", map.get(2));
        assertEquals("5", map.get("a"));
    }

    @Test
    public void testPutAllAtIndex_newKeysOnly() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("a", "1");
        map.put("d", "4");
        Map<String, String> toAdd = new HashMap<String, String>();
        toAdd.put("b", "2");
        toAdd.put("c", "3");
        map.putAll(1, toAdd);
        assertEquals(4, map.size());
        assertEquals("a", map.get(0));
        assertEquals("b", map.get(1));
        assertEquals("c", map.get(2));
        assertEquals("d", map.get(3));
    }

    @Test
    public void testPutAllAtIndex_mixedKeys() {
        // Likely detects defect
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("a", "1");
        map.put("b", "2");
        map.put("c", "3");
        Map<String, String> toAdd = new LinkedHashMap<String, String>();
        toAdd.put("b", "4");
        toAdd.put("d", "5");
        toAdd.put("a", "6");
        map.putAll(1, toAdd);
        assertEquals(4, map.size());
        assertEquals("b", map.get(0));
        assertEquals("d", map.get(1));
        assertEquals("a", map.get(2));
        assertEquals("c", map.get(3));
        assertEquals("4", map.get("b"));
        assertEquals("5", map.get("d"));
        assertEquals("6", map.get("a"));
    }

    @Test
    public void testRemove_existingKey_returnsOldValue() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("a", "1");
        map.put("b", "2");
        assertEquals("1", map.remove("a"));
        assertEquals(1, map.size());
        assertEquals("b", map.get(0));
    }

    @Test
    public void testRemove_nonExistingKey_returnsNull() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("a", "1");
        assertNull(map.remove("b"));
        assertEquals(1, map.size());
    }

    @Test
    public void testRemoveAtIndex_removesCorrectElement() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("a", "1");
        map.put("b", "2");
        map.put("c", "3");
        assertEquals("2", map.remove(1));
        assertEquals(2, map.size());
        assertEquals("a", map.get(0));
        assertEquals("c", map.get(1));
    }

    @Test
    public void testClear_emptiesMap() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("a", "1");
        map.clear();
        assertEquals(0, map.size());
        assertTrue(map.isEmpty());
    }

    @Test(expected = NoSuchElementException.class)
    public void testFirstKey_emptyMap_throwsException() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.firstKey();
    }

    @Test
    public void testFirstLastKey_nonEmpty() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("x", "1");
        map.put("y", "2");
        assertEquals("x", map.firstKey());
        assertEquals("y", map.lastKey());
    }

    @Test
    public void testNextKey_variousCases() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("a", "1");
        map.put("b", "2");
        map.put("c", "3");
        assertEquals("b", map.nextKey("a"));
        assertNull(map.nextKey("c"));
        assertNull(map.nextKey("nonexistent"));
    }

    @Test
    public void testPreviousKey_variousCases() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("a", "1");
        map.put("b", "2");
        map.put("c", "3");
        assertEquals("b", map.previousKey("c"));
        assertNull(map.previousKey("a"));
        assertNull(map.previousKey("nonexistent"));
    }

    @Test
    public void testMapIterator_basic() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("a", "1");
        map.put("b", "2");
        OrderedMapIterator<String, String> it = map.mapIterator();
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
        it.remove();
        assertEquals(1, map.size());
        assertFalse(map.containsKey("b"));
    }

    @Test
    public void testMapIterator_illegalStateExceptions() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("a", "1");
        OrderedMapIterator<String, String> it = map.mapIterator();
        try {
            it.remove();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }
        try {
            it.getKey();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }
        try {
            it.getValue();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }
        try {
            it.setValue("x");
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    @Test
    public void testValuesView_set() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("a", "1");
        map.put("b", "2");
        List<String> values = map.valueList();
        assertEquals("1", values.get(0));
        assertEquals("1", values.set(0, "10"));
        assertEquals("10", map.get("a"));
    }

    @Test
    public void testNullKey_operations() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put(null, "nullVal");
        assertTrue(map.containsKey(null));
        assertEquals("nullVal", map.get(null));
        assertEquals(1, map.size());
        assertNull(map.get(0));
        assertEquals("nullVal", map.remove(null));
        assertTrue(map.isEmpty());
    }

    @Test
    public void testAsList_unmodifiable() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("a", "1");
        List<String> list = map.asList();
        assertEquals(1, list.size());
        assertTrue(list.contains("a"));
        try {
            list.add("b");
            fail("Should be unmodifiable");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    // ================ New tests covering uncovered parts ================

    @Test
    public void testPutAll_noIndex() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("a", "1");
        map.put("c", "3");
        Map<String, String> toAdd = new HashMap<String, String>();
        toAdd.put("b", "2");
        toAdd.put("d", "4");
        map.putAll(toAdd);
        assertEquals(4, map.size());
        // Original keys retain order, new keys appended
        assertEquals("a", map.get(0));
        assertEquals("c", map.get(1));
        assertEquals("b", map.get(2));
        assertEquals("d", map.get(3));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGet_invalidIndex() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("a", "1");
        map.get(1); // index out of bounds (size=1, valid indices 0)
    }

    @Test
    public void testContainsKey_and_ContainsValue() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("a", "1");
        map.put("b", "2");
        assertTrue(map.containsKey("a"));
        assertFalse(map.containsKey("c"));
        assertTrue(map.containsValue("1"));
        assertFalse(map.containsValue("3"));
    }

    @Test
    public void testKeyList_modifiable() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("a", "1");
        map.put("b", "2");
        List<String> keys = map.keyList();
        assertEquals(2, keys.size());
        assertEquals("a", keys.get(0));
        // remove through keyList
        keys.remove(0);
        assertEquals(1, map.size());
        assertFalse(map.containsKey("a"));
        assertEquals("b", map.get(0));
    }

    @Test
    public void testValueList_removeAndClear() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("a", "1");
        map.put("b", "2");
        List<String> values = map.valueList();
        // remove by object
        assertTrue(values.remove("1"));
        assertEquals(1, map.size());
        assertFalse(map.containsKey("a"));
        // clear
        values.clear();
        assertTrue(map.isEmpty());
    }

    @Test
    public void testEqualsAndHashCode() {
        ListOrderedMap<String, String> map1 = new ListOrderedMap<String, String>();
        map1.put("a", "1");
        map1.put("b", "2");
        ListOrderedMap<String, String> map2 = new ListOrderedMap<String, String>();
        map2.put("a", "1");
        map2.put("b", "2");
        assertTrue(map1.equals(map2));
        assertTrue(map1.hashCode() == map2.hashCode());
        map2.put("c", "3");
        assertFalse(map1.equals(map2));
    }

    @Test
    public void testToString() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("a", "1");
        String str = map.toString();
        assertTrue(str.contains("a=1"));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testPutAtIndex_invalidIndex() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("a", "1");
        map.put(-1, "b", "2"); // negative index
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testRemoveAtIndex_invalidIndex() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("a", "1");
        map.remove(1); // index > size
    }
}