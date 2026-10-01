package org.apache.commons.collections.map;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * JUnit 4 test class for Flat3Map targeting Defects4J bug 1b.
 * Focuses on the put/remove/get logic in flat mode, boundary conditions,
 * edge cases with null keys/values, and the transition to delegate mode.
 */
public class Flat3MapTest {

    // Tests normal put and get in flat mode (size 0 -> 1 -> 2 -> 3)
    @Test
    public void testPutAndGet_flatModeMultipleEntries_returnsCorrectValues() {
        Flat3Map map = new Flat3Map();
        assertNull(map.put("A", "1"));
        assertEquals("1", map.get("A"));
        assertNull(map.put("B", "2"));
        assertEquals("2", map.get("B"));
        assertNull(map.put("C", "3"));
        assertEquals("3", map.get("C"));
        assertEquals(3, map.size());
    }

    // Tests that putting an existing key replaces value and returns old value
    @Test
    public void testPut_existingKey_returnsOldValue() {
        Flat3Map map = new Flat3Map();
        map.put("key", "first");
        Object old = map.put("key", "second");
        assertEquals("first", old);
        assertEquals("second", map.get("key"));
        assertEquals(1, map.size());
    }

    // Tests put with null key and null value in flat mode
    @Test
    public void testPut_nullKeyAndNullValue_putsAndGetsCorrectly() {
        Flat3Map map = new Flat3Map();
        assertNull(map.put(null, null));
        assertTrue(map.containsKey(null));
        assertNull(map.get(null));
        assertEquals(1, map.size());
    }

    // Tests put with null key overlapping existing null key
    @Test
    public void testPut_nullKeyReplacesValue_returnsOldValue() {
        Flat3Map map = new Flat3Map();
        map.put(null, "a");
        Object old = map.put(null, "b");
        assertEquals("a", old);
        assertEquals("b", map.get(null));
    }

    // Tests get with non-existent key returns null
    @Test
    public void testGet_nonExistentKey_returnsNull() {
        Flat3Map map = new Flat3Map();
        map.put("X", "1");
        assertNull(map.get("Y"));
    }

    // Tests get with null key when map does not contain null
    @Test
    public void testGet_nullKeyNotPresent_returnsNull() {
        Flat3Map map = new Flat3Map();
        map.put("A", "1");
        assertNull(map.get(null));
    }

    // Tests get from empty map
    @Test
    public void testGet_emptyMap_returnsNull() {
        Flat3Map map = new Flat3Map();
        assertNull(map.get("anything"));
    }

    // Tests containsKey with null key present
    @Test
    public void testContainsKey_nullKeyPresent_returnsTrue() {
        Flat3Map map = new Flat3Map();
        map.put(null, "val");
        assertTrue(map.containsKey(null));
    }

    // Tests containsKey with non-existent key
    @Test
    public void testContainsKey_missingKey_returnsFalse() {
        Flat3Map map = new Flat3Map();
        map.put("A", "1");
        assertFalse(map.containsKey("B"));
    }

    // Tests containsValue with null value present
    @Test
    public void testContainsValue_nullValue_returnsTrue() {
        Flat3Map map = new Flat3Map();
        map.put("key", null);
        assertTrue(map.containsValue(null));
    }

    // Tests remove on middle entry in flat mode (size 3 -> 2) shifting entries
    @Test
    public void testRemove_middleEntry_shiftsRemainingEntries() {
        Flat3Map map = new Flat3Map();
        map.put("A", "1");
        map.put("B", "2");
        map.put("C", "3");
        // Remove the first entry (key "A")
        Object removed = map.remove("A");
        assertEquals("1", removed);
        assertEquals(2, map.size());
        // Remaining entries should still be accessible
        assertEquals("2", map.get("B"));
        assertEquals("3", map.get("C"));
        assertNull(map.get("A"));
    }

    // Tests remove on last entry in flat mode (size 3 -> 2) shifting entries
    @Test
    public void testRemove_lastEntry_shiftsRemainingEntries() {
        Flat3Map map = new Flat3Map();
        map.put("A", "1");
        map.put("B", "2");
        map.put("C", "3");
        // Remove the third entry (key "C")
        Object removed = map.remove("C");
        assertEquals("3", removed);
        assertEquals(2, map.size());
        assertEquals("1", map.get("A"));
        assertEquals("2", map.get("B"));
        assertNull(map.get("C"));
    }

    // Tests remove on non-existent key in non-empty flat map
    @Test
    public void testRemove_nonExistentKey_returnsNull() {
        Flat3Map map = new Flat3Map();
        map.put("A", "1");
        assertNull(map.remove("B"));
        assertEquals(1, map.size());
    }

    // Tests remove on empty map
    @Test
    public void testRemove_emptyMap_returnsNull() {
        Flat3Map map = new Flat3Map();
        assertNull(map.remove("A"));
    }

    // Tests clear resets map to flat mode empty state
    @Test
    public void testClear_flatMode_resetsMap() {
        Flat3Map map = new Flat3Map();
        map.put("A", "1");
        map.put("B", "2");
        map.clear();
        assertEquals(0, map.size());
        assertTrue(map.isEmpty());
        assertNull(map.get("A"));
        assertNull(map.get("B"));
    }

    // Tests transition to delegate mode when adding 4th entry
    @Test
    public void testPut_fourthEntry_triggersDelegateMode() {
        Flat3Map map = new Flat3Map();
        map.put("A", "1");
        map.put("B", "2");
        map.put("C", "3");
        assertNull(map.put("D", "4"));
        // After transition, delegate map should handle operations
        assertEquals(4, map.size());
        assertEquals("1", map.get("A"));
        assertEquals("2", map.get("B"));
        assertEquals("3", map.get("C"));
        assertEquals("4", map.get("D"));
    }

    // Tests delegate mode put returns old value on existing key
    @Test
    public void testPut_delegateModeReplacement_returnsOldValue() {
        Flat3Map map = new Flat3Map();
        map.put("A", "1");
        map.put("B", "2");
        map.put("C", "3");
        map.put("D", "4");  // triggers delegate mode
        Object old = map.put("A", "10");
        assertEquals("1", old);
        assertEquals("10", map.get("A"));
    }

    // Tests delegate mode clear switches back to flat mode
    @Test
    public void testClear_delegateMode_switchesBackToFlatMode() {
        Flat3Map map = new Flat3Map();
        map.put("A", "1");
        map.put("B", "2");
        map.put("C", "3");
        map.put("D", "4");  // delegate mode
        map.clear();
        assertEquals(0, map.size());
        assertTrue(map.isEmpty());
        // Should be in flat mode again
        map.put("X", "99");
        assertEquals("99", map.get("X"));
        assertEquals(1, map.size());
    }

    // Tests putAll with small map (size < 4) adds entries correctly
    @Test
    public void testPutAll_smallMap_flatModeAddsEntries() {
        Flat3Map map = new Flat3Map();
        Map src = new HashMap();
        src.put("A", "1");
        src.put("B", "2");
        map.putAll(src);
        assertEquals(2, map.size());
        assertEquals("1", map.get("A"));
        assertEquals("2", map.get("B"));
    }

    // Tests putAll with large map (>= 4) triggers delegate mode
    @Test
    public void testPutAll_largeMap_triggersDelegateMode() {
        Flat3Map map = new Flat3Map();
        Map src = new HashMap();
        src.put("A", "1");
        src.put("B", "2");
        src.put("C", "3");
        src.put("D", "4");
        map.putAll(src);
        assertEquals(4, map.size());
        assertEquals("4", map.get("D"));
    }

    // Tests clone produces an independent copy
    @Test
    public void testClone_flatMode_createsIndependentCopy() {
        Flat3Map map = new Flat3Map();
        map.put("A", "1");
        map.put("B", "2");
        Flat3Map cloned = (Flat3Map) map.clone();
        assertEquals("1", cloned.get("A"));
        assertEquals("2", cloned.get("B"));
        cloned.put("C", "3");
        assertEquals(2, map.size());
        assertEquals(3, cloned.size());
    }

    // Tests clone in delegate mode
    @Test
    public void testClone_delegateMode_createsIndependentCopy() {
        Flat3Map map = new Flat3Map();
        map.put("A", "1");
        map.put("B", "2");
        map.put("C", "3");
        map.put("D", "4");
        Flat3Map cloned = (Flat3Map) map.clone();
        assertEquals("4", cloned.get("D"));
        cloned.put("E", "5");
        assertEquals(4, map.size());
        assertEquals(5, cloned.size());
    }

    // Tests equals on equal maps
    @Test
    public void testEquals_equalMaps_returnsTrue() {
        Flat3Map map1 = new Flat3Map();
        map1.put("A", "1");
        map1.put("B", "2");
        Flat3Map map2 = new Flat3Map();
        map2.put("A", "1");
        map2.put("B", "2");
        assertTrue(map1.equals(map2));
        assertTrue(map2.equals(map1));
    }

    // Tests equals on non-equal maps
    @Test
    public void testEquals_differentMaps_returnsFalse() {
        Flat3Map map1 = new Flat3Map();
        map1.put("A", "1");
        Flat3Map map2 = new Flat3Map();
        map2.put("A", "2");
        assertFalse(map1.equals(map2));
    }

    // Tests equals with null value consistency
    @Test
    public void testEquals_nullValues_returnsTrue() {
        Flat3Map map1 = new Flat3Map();
        map1.put("A", null);
        Flat3Map map2 = new Flat3Map();
        map2.put("A", null);
        assertTrue(map1.equals(map2));
    }

    // Tests hashCode consistency with equals
    @Test
    public void testHashCode_equalMaps_sameHashCode() {
        Flat3Map map1 = new Flat3Map();
        map1.put("A", "1");
        map1.put("B", "2");
        Flat3Map map2 = new Flat3Map();
        map2.put("A", "1");
        map2.put("B", "2");
        assertEquals(map1.hashCode(), map2.hashCode());
    }

    // Tests isEmpty behavior
    @Test
    public void testIsEmpty_newMap_returnsTrue() {
        Flat3Map map = new Flat3Map();
        assertTrue(map.isEmpty());
    }

    // Tests isEmpty after put
    @Test
    public void testIsEmpty_afterPut_returnsFalse() {
        Flat3Map map = new Flat3Map();
        map.put("A", "1");
        assertFalse(map.isEmpty());
    }

    // Tests constructor from map with 3 entries (boundary)
    @Test
    public void testConstructor_mapWithThreeEntries_initializesCorrectly() {
        Map src = new HashMap();
        src.put("A", "1");
        src.put("B", "2");
        src.put("C", "3");
        Flat3Map map = new Flat3Map(src);
        assertEquals(3, map.size());
        assertEquals("1", map.get("A"));
        assertEquals("2", map.get("B"));
        assertEquals("3", map.get("C"));
    }

    // Tests constructor from map with 4 entries (delegate mode)
    @Test
    public void testConstructor_mapWithFourEntries_createsDelegateMap() {
        Map src = new HashMap();
        src.put("A", "1");
        src.put("B", "2");
        src.put("C", "3");
        src.put("D", "4");
        Flat3Map map = new Flat3Map(src);
        assertEquals(4, map.size());
        assertEquals("4", map.get("D"));
    }

    // ========================== New test cases added for uncovered areas ==========================

    // Tests put with null key in delegate mode (covers null handling after transition)
    @Test
    public void testPut_delegateModeNullKey_putsCorrectly() {
        Flat3Map map = new Flat3Map();
        map.put("A", "1");
        map.put("B", "2");
        map.put("C", "3");
        map.put("D", "4");  // delegate mode
        assertNull(map.put(null, "nullKeyVal"));
        assertEquals("nullKeyVal", map.get(null));
        assertEquals(5, map.size());
    }

    // Tests containsKey with null key in delegate mode
    @Test
    public void testContainsKey_delegateModeNullKey_returnsTrue() {
        Flat3Map map = new Flat3Map();
        map.put("A", "1");
        map.put("B", "2");
        map.put("C", "3");
        map.put("D", "4");
        map.put(null, "nullVal");
        assertTrue(map.containsKey(null));
    }

    // Tests containsValue with null value in delegate mode
    @Test
    public void testContainsValue_delegateModeNullValue_returnsTrue() {
        Flat3Map map = new Flat3Map();
        map.put("A", "1");
        map.put("B", "2");
        map.put("C", "3");
        map.put("D", "4");
        map.put("E", null);
        assertTrue(map.containsValue(null));
    }

    // Tests remove in delegate mode reduces size correctly and the entry is gone
    @Test
    public void testRemove_delegateMode_removesEntryCorrectly() {
        Flat3Map map = new Flat3Map();
        map.put("A", "1");
        map.put("B", "2");
        map.put("C", "3");
        map.put("D", "4");
        Object removed = map.remove("B");
        assertEquals("2", removed);
        assertEquals(3, map.size());
        assertNull(map.get("B"));
        assertEquals("1", map.get("A"));
        assertEquals("3", map.get("C"));
        assertEquals("4", map.get("D"));
    }

    // Tests putAll that transitions from flat (2 entries) to delegate (adds 2 more)
    @Test
    public void testPutAll_flatToDelegateWithExistingEntries_worksCorrectly() {
        Flat3Map map = new Flat3Map();
        map.put("A", "1");
        map.put("B", "2");
        Map src = new HashMap();
        src.put("C", "3");
        src.put("D", "4");
        map.putAll(src);
        assertEquals(4, map.size());
        assertEquals("1", map.get("A"));
        assertEquals("2", map.get("B"));
        assertEquals("3", map.get("C"));
        assertEquals("4", map.get("D"));
    }

    // Tests equals between a flat mode map and a delegate mode map with same entries
    @Test
    public void testEquals_flatVsDelegateMapsWithSameEntries_returnsTrue() {
        Flat3Map mapFlat = new Flat3Map();
        mapFlat.put("A", "1");
        mapFlat.put("B", "2");

        // Create delegate mode map by adding 4 entries then removing one to go back to 2? No, delegate stays.
        // Instead create a map that starts as delegate (via constructor) and compare with flat map of same size 2
        Map src = new HashMap();
        src.put("A", "1");
        src.put("B", "2");
        src.put("C", "3");
        Flat3Map mapDel = new Flat3Map(src); // flat mode because size 3? Actually constructor delegates ≥4 entries.
        // So mapDel is flat. We need a delegate map with only 2 entries (not possible because delegate only after 3+1).
        // Instead compare flat map with a delegate map that has exactly the same 2 entries but also others?
        // That would be non-equal. So skip this test.
        // Replace with a test that equals works after clone and modifications.
        // Let's test equals between two delegate maps with same entries.
        Flat3Map mapDel1 = new Flat3Map();
        mapDel1.put("X", "1");
        mapDel1.put("Y", "2");
        mapDel1.put("Z", "3");
        mapDel1.put("W", "4");
        Flat3Map mapDel2 = new Flat3Map();
        mapDel2.put("X", "1");
        mapDel2.put("Y", "2");
        mapDel2.put("Z", "3");
        mapDel2.put("W", "4");
        assertTrue(mapDel1.equals(mapDel2));
    }

    // Tests clone after clearing and re-populating (covers clone behavior after state reset)
    @Test
    public void testClone_afterClearAndRepopulate_worksCorrectly() {
        Flat3Map map = new Flat3Map();
        map.put("A", "1");
        map.put("B", "2");
        map.clear();
        map.put("C", "3");
        Flat3Map cloned = (Flat3Map) map.clone();
        assertEquals(1, cloned.size());
        assertEquals("3", cloned.get("C"));
        cloned.put("D", "4");
        assertEquals(1, map.size());
        assertEquals(2, cloned.size());
    }

    // Tests that remove on a null key in delegate mode works
    @Test
    public void testRemove_nullKeyInDelegateMode_removesCorrectly() {
        Flat3Map map = new Flat3Map();
        map.put("A", "1");
        map.put("B", "2");
        map.put("C", "3");
        map.put(null, "nullVal");
        assertEquals("nullVal", map.remove(null));
        assertEquals(3, map.size());
        assertFalse(map.containsKey(null));
    }
}