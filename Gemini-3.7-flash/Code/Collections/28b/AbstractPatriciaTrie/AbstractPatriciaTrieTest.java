package org.apache.commons.collections4.trie;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;

import org.apache.commons.collections4.OrderedMapIterator;
import org.junit.Before;
import org.junit.Test;

public class AbstractPatriciaTrieTest {

    private PatriciaTrie<String> trie;

    @Before
    public void setUp() {
        trie = new PatriciaTrie<String>();
    }

    // Tests defect where prefixMap.clear() removes all elements or fails
    @Test
    public void testPrefixMap_clear_removesOnlyPrefixedEntries() {
        trie.put("Anna", "1");
        trie.put("Anael", "2");
        trie.put("Analu", "3");
        trie.put("Andreas", "4");
        trie.put("Bob", "5");

        final SortedMap<String, String> prefixMap = trie.prefixMap("Ana");
        assertEquals(2, prefixMap.size());

        prefixMap.clear();

        assertEquals(0, prefixMap.size());
        assertEquals(3, trie.size());
        assertFalse(trie.containsKey("Anael"));
        assertFalse(trie.containsKey("Analu"));
        assertTrue(trie.containsKey("Anna"));
        assertTrue(trie.containsKey("Andreas"));
        assertTrue(trie.containsKey("Bob"));
    }

    // Tests normal put and get operations
    @Test
    public void testPutAndGet_standardKeys_returnsCorrectValues() {
        assertNull(trie.put("key1", "val1"));
        assertNull(trie.put("key2", "val2"));
        assertEquals(2, trie.size());

        assertEquals("val1", trie.get("key1"));
        assertEquals("val2", trie.get("key2"));
        assertNull(trie.get("nonexistent"));

        // Overwrite existing key
        assertEquals("val1", trie.put("key1", "newVal1"));
        assertEquals("newVal1", trie.get("key1"));
        assertEquals(2, trie.size());
    }

    // Tests putting an empty string key stored at the root node
    @Test
    public void testPut_emptyStringKey_storedAtRoot() {
        assertNull(trie.put("", "emptyRoot"));
        assertEquals(1, trie.size());
        assertTrue(trie.containsKey(""));
        assertEquals("emptyRoot", trie.get(""));

        assertEquals("emptyRoot", trie.put("", "updatedRoot"));
        assertEquals("updatedRoot", trie.get(""));
        assertEquals(1, trie.size());
    }

    // Tests put with null key throwing NullPointerException
    @Test(expected = NullPointerException.class)
    public void testPut_nullKey_throwsNullPointerException() {
        trie.put(null, "value");
    }

    // Tests remove operation on existing and non-existing keys
    @Test
    public void testRemove_existingAndNonExistingKeys_returnsExpectedResults() {
        trie.put("dog", "canine");
        trie.put("cat", "feline");
        trie.put("cow", "bovine");

        assertEquals("feline", trie.remove("cat"));
        assertEquals(2, trie.size());
        assertFalse(trie.containsKey("cat"));
        assertNull(trie.remove("cat"));
        assertNull(trie.remove(null));
        assertNull(trie.remove("pig"));
    }

    // Tests removing the root key (empty string)
    @Test
    public void testRemove_rootKey_removesCorrectly() {
        trie.put("", "rootVal");
        trie.put("child", "childVal");

        assertEquals("rootVal", trie.remove(""));
        assertEquals(1, trie.size());
        assertFalse(trie.containsKey(""));
        assertTrue(trie.containsKey("child"));
    }

    // Tests clear resetting the trie state
    @Test
    public void testClear_populatedTrie_resetsSizeAndRoot() {
        trie.put("apple", "1");
        trie.put("banana", "2");
        trie.put("", "0");
        assertEquals(3, trie.size());

        trie.clear();

        assertEquals(0, trie.size());
        assertTrue(trie.isEmpty());
        assertNull(trie.get("apple"));
        assertNull(trie.get(""));
    }

    // Tests firstKey and lastKey on populated trie
    @Test
    public void testFirstAndLastKey_populatedTrie_returnsCorrectKeys() {
        trie.put("c", "3");
        trie.put("a", "1");
        trie.put("b", "2");

        assertEquals("a", trie.firstKey());
        assertEquals("c", trie.lastKey());
    }

    // Tests firstKey on empty trie throwing NoSuchElementException
    @Test(expected = NoSuchElementException.class)
    public void testFirstKey_emptyTrie_throwsNoSuchElementException() {
        trie.firstKey();
    }

    // Tests lastKey on empty trie throwing NoSuchElementException
    @Test(expected = NoSuchElementException.class)
    public void testLastKey_emptyTrie_throwsNoSuchElementException() {
        trie.lastKey();
    }

    // Tests nextKey and previousKey navigation
    @Test
    public void testNextAndPreviousKey_populatedTrie_returnsNextAndPreviousKeys() {
        trie.put("ant", "1");
        trie.put("bear", "2");
        trie.put("cat", "3");

        assertEquals("bear", trie.nextKey("ant"));
        assertEquals("cat", trie.nextKey("bear"));
        assertNull(trie.nextKey("cat"));

        assertEquals("bear", trie.previousKey("cat"));
        assertEquals("ant", trie.previousKey("bear"));
        assertNull(trie.previousKey("ant"));
    }

    // Tests nextKey with null argument throwing NullPointerException
    @Test(expected = NullPointerException.class)
    public void testNextKey_nullKey_throwsNullPointerException() {
        trie.nextKey(null);
    }

    // Tests previousKey with null argument throwing NullPointerException
    @Test(expected = NullPointerException.class)
    public void testPreviousKey_nullKey_throwsNullPointerException() {
        trie.previousKey(null);
    }

    // Tests mapIterator forward and backward iteration
    @Test
    public void testMapIterator_populatedTrie_iteratesForwardAndBackward() {
        trie.put("k1", "v1");
        trie.put("k2", "v2");

        final OrderedMapIterator<String, String> it = trie.mapIterator();
        assertTrue(it.hasNext());
        assertFalse(it.hasPrevious());

        assertEquals("k1", it.next());
        assertEquals("k1", it.getKey());
        assertEquals("v1", it.getValue());
        assertTrue(it.hasPrevious());

        assertEquals("k2", it.next());
        assertEquals("k2", it.getKey());
        assertEquals("v2", it.getValue());

        it.setValue("v2_updated");
        assertEquals("v2_updated", trie.get("k2"));

        assertEquals("k2", it.previous());
        assertEquals("k1", it.previous());
        assertFalse(it.hasPrevious());
    }

    // Tests entrySet, keySet and values collection views
    @Test
    public void testCollectionViews_populatedTrie_returnsCorrectViews() {
        trie.put("one", "1");
        trie.put("two", "2");

        final Set<Map.Entry<String, String>> entries = trie.entrySet();
        assertEquals(2, entries.size());

        final Set<String> keys = trie.keySet();
        assertEquals(2, keys.size());
        assertTrue(keys.contains("one"));
        assertTrue(keys.contains("two"));

        final Collection<String> values = trie.values();
        assertEquals(2, values.size());
        assertTrue(values.contains("1"));
        assertTrue(values.contains("2"));

        keys.remove("one");
        assertEquals(1, trie.size());
        assertFalse(trie.containsKey("one"));
    }

    // Tests select, selectKey and selectValue closest match
    @Test
    public void testSelect_matchingKey_returnsClosestMatch() {
        trie.put("Apple", "1");
        trie.put("Application", "2");

        final Map.Entry<String, String> entry = trie.select("Appl");
        assertNotNull(entry);
        assertTrue(entry.getKey().startsWith("Appl"));
        assertNotNull(trie.selectKey("Appl"));
        assertNotNull(trie.selectValue("Appl"));
    }

    // Tests prefixMap submap query methods
    @Test
    public void testPrefixMap_matchingPrefix_returnsCorrectSubset() {
        trie.put("car", "1");
        trie.put("cart", "2");
        trie.put("carpet", "3");
        trie.put("dog", "4");

        final SortedMap<String, String> prefixMap = trie.prefixMap("car");
        assertEquals(3, prefixMap.size());
        assertTrue(prefixMap.containsKey("car"));
        assertTrue(prefixMap.containsKey("cart"));
        assertTrue(prefixMap.containsKey("carpet"));
        assertFalse(prefixMap.containsKey("dog"));

        assertEquals("car", prefixMap.firstKey());
        assertEquals("cart", prefixMap.lastKey());
    }

    // Tests subMap, headMap and tailMap range views
    @Test
    public void testSubMapHeadMapTailMap_validRanges_returnsCorrectSubMaps() {
        trie.put("a", "1");
        trie.put("b", "2");
        trie.put("c", "3");
        trie.put("d", "4");

        final SortedMap<String, String> sub = trie.subMap("b", "d");
        assertEquals(2, sub.size());
        assertTrue(sub.containsKey("b"));
        assertTrue(sub.containsKey("c"));
        assertFalse(sub.containsKey("d"));

        final SortedMap<String, String> head = trie.headMap("c");
        assertEquals(2, head.size());
        assertTrue(head.containsKey("a"));
        assertTrue(head.containsKey("b"));

        final SortedMap<String, String> tail = trie.tailMap("c");
        assertEquals(2, tail.size());
        assertTrue(tail.containsKey("c"));
        assertTrue(tail.containsKey("d"));
    }

    // Tests iterator remove in prefixMap
    @Test
    public void testPrefixMap_iteratorRemove_removesExpectedElements() {
        trie.put("test1", "v1");
        trie.put("test2", "v2");
        trie.put("other", "v3");

        final SortedMap<String, String> prefixMap = trie.prefixMap("test");
        final Iterator<Map.Entry<String, String>> it = prefixMap.entrySet().iterator();
        while (it.hasNext()) {
            final Map.Entry<String, String> entry = it.next();
            if ("test1".equals(entry.getKey())) {
                it.remove();
            }
        }

        assertEquals(1, prefixMap.size());
        assertEquals(2, trie.size());
        assertFalse(trie.containsKey("test1"));
        assertTrue(trie.containsKey("test2"));
        assertTrue(trie.containsKey("other"));
    }

    // Additional tests for coverage

    @Test
    public void testConstructor_copyMap_initializesEntries() {
        final Map<String, String> source = new HashMap<String, String>();
        source.put("k1", "v1");
        source.put("k2", "v2");

        final PatriciaTrie<String> copy = new PatriciaTrie<String>(source);
        assertEquals(2, copy.size());
        assertEquals("v1", copy.get("k1"));
        assertEquals("v2", copy.get("k2"));
    }

    @Test
    public void testContainsValue_existingAndNonExisting_returnsExpected() {
        trie.put("k1", "v1");
        trie.put("k2", "v2");
        trie.put("k3", null);

        assertTrue(trie.containsValue("v1"));
        assertTrue(trie.containsValue("v2"));
        assertTrue(trie.containsValue(null));
        assertFalse(trie.containsValue("v3"));
    }

    @Test
    public void testPrefixMap_putWithinAndOutsidePrefix() {
        final SortedMap<String, String> prefixMap = trie.prefixMap("pre");
        prefixMap.put("prefix1", "val1");
        prefixMap.put("prefix2", "val2");

        assertEquals(2, prefixMap.size());
        assertEquals(2, trie.size());
        assertTrue(trie.containsKey("prefix1"));
        assertTrue(trie.containsKey("prefix2"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPrefixMap_putOutsidePrefix_throwsIllegalArgumentException() {
        final SortedMap<String, String> prefixMap = trie.prefixMap("pre");
        prefixMap.put("other", "val");
    }

    @Test(expected = NullPointerException.class)
    public void testPrefixMap_nullPrefix_throwsNullPointerException() {
        trie.prefixMap(null);
    }

    @Test
    public void testPrefixMap_emptyPrefix_returnsEntireTrie() {
        trie.put("a", "1");
        trie.put("b", "2");

        final SortedMap<String, String> prefixMap = trie.prefixMap("");
        assertEquals(2, prefixMap.size());
        assertTrue(prefixMap.containsKey("a"));
        assertTrue(prefixMap.containsKey("b"));
    }

    @Test(expected = NoSuchElementException.class)
    public void testPrefixMap_firstKey_emptyPrefixMap_throwsNoSuchElementException() {
        final SortedMap<String, String> prefixMap = trie.prefixMap("nonexistent");
        prefixMap.firstKey();
    }

    @Test(expected = NoSuchElementException.class)
    public void testPrefixMap_lastKey_emptyPrefixMap_throwsNoSuchElementException() {
        final SortedMap<String, String> prefixMap = trie.prefixMap("nonexistent");
        prefixMap.lastKey();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubMap_fromKeyGreaterThanToKey_throwsIllegalArgumentException() {
        trie.put("a", "1");
        trie.put("z", "2");
        trie.subMap("z", "a");
    }

    @Test
    public void testSubMap_putWithinAndOutsideRange() {
        final SortedMap<String, String> sub = trie.subMap("b", "d");
        sub.put("bc", "1");
        sub.put("c", "2");

        assertEquals(2, sub.size());
        assertEquals(2, trie.size());
        assertTrue(trie.containsKey("bc"));
        assertTrue(trie.containsKey("c"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubMap_putOutsideRange_throwsIllegalArgumentException() {
        final SortedMap<String, String> sub = trie.subMap("b", "d");
        sub.put("a", "1");
    }

    @Test
    public void testSelect_noMatch_returnsNull() {
        assertNull(trie.select("nonexistent"));
        assertNull(trie.selectKey("nonexistent"));
        assertNull(trie.selectValue("nonexistent"));
    }

    @Test
    public void testEqualsAndHashCode_twoEqualTries_returnsTrueAndSameHashCode() {
        final PatriciaTrie<String> trie2 = new PatriciaTrie<String>();
        trie.put("k1", "v1");
        trie.put("k2", "v2");

        trie2.put("k1", "v1");
        trie2.put("k2", "v2");

        assertEquals(trie, trie2);
        assertEquals(trie.hashCode(), trie2.hashCode());

        trie2.put("k3", "v3");
        assertNotEquals(trie, trie2);
    }

    @Test
    public void testToString_populatedTrie_containsEntries() {
        trie.put("key", "value");
        final String str = trie.toString();
        assertNotNull(str);
        assertTrue(str.contains("key"));
        assertTrue(str.contains("value"));
    }
}