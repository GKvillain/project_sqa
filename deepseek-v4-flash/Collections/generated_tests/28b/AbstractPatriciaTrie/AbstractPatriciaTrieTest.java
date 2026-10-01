package org.apache.commons.collections4.trie;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.collections4.OrderedMapIterator;
import org.junit.Test;

public class AbstractPatriciaTrieTest {

    private PatriciaTrie<String, String> createTrie() {
        return new PatriciaTrie<String, String>();
    }

    // ========== 原有测试用例 ==========

    @Test
    public void testPut_newKey_returnsNull() {
        PatriciaTrie<String, String> trie = createTrie();
        assertNull(trie.put("A", "1"));
        assertEquals("1", trie.get("A"));
    }

    @Test
    public void testPut_existingKey_returnsOldValue() {
        PatriciaTrie<String, String> trie = createTrie();
        trie.put("A", "1");
        assertEquals("1", trie.put("A", "2"));
        assertEquals("2", trie.get("A"));
    }

    @Test(expected = NullPointerException.class)
    public void testPut_nullKey_throwsNullPointerException() {
        createTrie().put(null, "value");
    }

    @Test
    public void testPut_emptyKey_returnsNull() {
        PatriciaTrie<String, String> trie = createTrie();
        assertNull(trie.put("", "root"));
        assertEquals("root", trie.get(""));
    }

    @Test
    public void testGet_existingKey_returnsValue() {
        PatriciaTrie<String, String> trie = createTrie();
        trie.put("key", "value");
        assertEquals("value", trie.get("key"));
    }

    @Test
    public void testGet_nonExistingKey_returnsNull() {
        PatriciaTrie<String, String> trie = createTrie();
        assertNull(trie.get("nonexistent"));
    }

    @Test
    public void testContainsKey_existingKey_returnsTrue() {
        PatriciaTrie<String, String> trie = createTrie();
        trie.put("key", "value");
        assertTrue(trie.containsKey("key"));
    }

    @Test
    public void testContainsKey_nonExistingKey_returnsFalse() {
        PatriciaTrie<String, String> trie = createTrie();
        assertFalse(trie.containsKey("nonexistent"));
    }

    @Test
    public void testRemove_existingKey_returnsValue() {
        PatriciaTrie<String, String> trie = createTrie();
        trie.put("key", "value");
        assertEquals("value", trie.remove("key"));
        assertNull(trie.get("key"));
    }

    @Test
    public void testRemove_nonExistingKey_returnsNull() {
        PatriciaTrie<String, String> trie = createTrie();
        assertNull(trie.remove("nonexistent"));
    }

    @Test
    public void testClear_afterPut_emptiesMap() {
        PatriciaTrie<String, String> trie = createTrie();
        trie.put("A", "1");
        trie.put("B", "2");
        trie.clear();
        assertEquals(0, trie.size());
        assertNull(trie.get("A"));
    }

    @Test
    public void testSize_empty_returnsZero() {
        assertEquals(0, createTrie().size());
    }

    @Test
    public void testSize_afterPut_returnsCorrect() {
        PatriciaTrie<String, String> trie = createTrie();
        trie.put("A", "1");
        trie.put("B", "2");
        assertEquals(2, trie.size());
    }

    @Test(expected = NoSuchElementException.class)
    public void testFirstKey_empty_throwsNoSuchElement() {
        createTrie().firstKey();
    }

    @Test
    public void testFirstKey_nonEmpty_returnsFirst() {
        PatriciaTrie<String, String> trie = createTrie();
        trie.put("B", "2");
        trie.put("A", "1");
        assertEquals("A", trie.firstKey());
    }

    @Test(expected = NoSuchElementException.class)
    public void testLastKey_empty_throwsNoSuchElement() {
        createTrie().lastKey();
    }

    @Test
    public void testLastKey_nonEmpty_returnsLast() {
        PatriciaTrie<String, String> trie = createTrie();
        trie.put("A", "1");
        trie.put("B", "2");
        assertEquals("B", trie.lastKey());
    }

    @Test
    public void testNextKey_existingKey_returnsNext() {
        PatriciaTrie<String, String> trie = createTrie();
        trie.put("A", "1");
        trie.put("B", "2");
        assertEquals("B", trie.nextKey("A"));
    }

    @Test
    public void testNextKey_lastKey_returnsNull() {
        PatriciaTrie<String, String> trie = createTrie();
        trie.put("A", "1");
        trie.put("B", "2");
        assertNull(trie.nextKey("B"));
    }

    @Test(expected = NullPointerException.class)
    public void testNextKey_null_throwsNullPointer() {
        createTrie().nextKey(null);
    }

    @Test
    public void testPreviousKey_existingKey_returnsPrevious() {
        PatriciaTrie<String, String> trie = createTrie();
        trie.put("A", "1");
        trie.put("B", "2");
        assertEquals("A", trie.previousKey("B"));
    }

    @Test
    public void testPreviousKey_firstKey_returnsNull() {
        PatriciaTrie<String, String> trie = createTrie();
        trie.put("A", "1");
        assertNull(trie.previousKey("A"));
    }

    @Test(expected = NullPointerException.class)
    public void testPreviousKey_null_throwsNullPointer() {
        createTrie().previousKey(null);
    }

    @Test
    public void testPrefixMap_prefixExists_returnsSubmap() {
        PatriciaTrie<String, String> trie = createTrie();
        trie.put("abc", "1");
        trie.put("abd", "2");
        trie.put("ab", "3");
        SortedMap<String, String> prefixMap = trie.prefixMap("ab");
        assertEquals(3, prefixMap.size());
        assertTrue(prefixMap.containsKey("abc"));
        assertTrue(prefixMap.containsKey("abd"));
        assertTrue(prefixMap.containsKey("ab"));
    }

    @Test
    public void testPrefixMap_noMatch_returnsEmpty() {
        PatriciaTrie<String, String> trie = createTrie();
        trie.put("abc", "1");
        SortedMap<String, String> prefixMap = trie.prefixMap("xyz");
        assertTrue(prefixMap.isEmpty());
    }

    @Test
    public void testSubMap_range_returnsCorrect() {
        PatriciaTrie<String, String> trie = createTrie();
        trie.put("A", "1");
        trie.put("B", "2");
        trie.put("C", "3");
        SortedMap<String, String> sub = trie.subMap("A", "C");
        assertEquals(2, sub.size());
        assertTrue(sub.containsKey("A"));
        assertTrue(sub.containsKey("B"));
        assertFalse(sub.containsKey("C"));
    }

    @Test
    public void testHeadMap_toKey_returnsCorrect() {
        PatriciaTrie<String, String> trie = createTrie();
        trie.put("A", "1");
        trie.put("B", "2");
        trie.put("C", "3");
        SortedMap<String, String> head = trie.headMap("C");
        assertEquals(2, head.size());
        assertTrue(head.containsKey("A"));
        assertTrue(head.containsKey("B"));
    }

    @Test
    public void testTailMap_fromKey_returnsCorrect() {
        PatriciaTrie<String, String> trie = createTrie();
        trie.put("A", "1");
        trie.put("B", "2");
        trie.put("C", "3");
        SortedMap<String, String> tail = trie.tailMap("B");
        assertEquals(2, tail.size());
        assertTrue(tail.containsKey("B"));
        assertTrue(tail.containsKey("C"));
    }

    @Test
    public void testMapIterator_iteratesAllEntries() {
        PatriciaTrie<String, String> trie = createTrie();
        trie.put("A", "1");
        trie.put("B", "2");
        OrderedMapIterator<String, String> it = trie.mapIterator();
        assertTrue(it.hasNext());
        assertEquals("A", it.next());
        assertEquals("1", it.getValue());
        assertTrue(it.hasNext());
        assertEquals("B", it.next());
        assertEquals("2", it.getValue());
        assertFalse(it.hasNext());
    }

    @Test
    public void testSelect_existingKey_returnsEntry() {
        PatriciaTrie<String, String> trie = createTrie();
        trie.put("A", "1");
        Map.Entry<String, String> entry = trie.select("A");
        assertNotNull(entry);
        assertEquals("A", entry.getKey());
        assertEquals("1", entry.getValue());
    }

    @Test
    public void testSelect_nonExistingKey_returnsNearest() {
        PatriciaTrie<String, String> trie = createTrie();
        trie.put("B", "2");
        trie.put("D", "4");
        Map.Entry<String, String> entry = trie.select("C");
        assertNotNull(entry);
        assertTrue(entry.getKey().equals("B") || entry.getKey().equals("D"));
    }

    @Test
    public void testSelectKey_existingKey_returnsKey() {
        PatriciaTrie<String, String> trie = createTrie();
        trie.put("A", "1");
        assertEquals("A", trie.selectKey("A"));
    }

    @Test
    public void testSelectValue_existingKey_returnsValue() {
        PatriciaTrie<String, String> trie = createTrie();
        trie.put("A", "1");
        assertEquals("1", trie.selectValue("A"));
    }

    @Test
    public void testEntrySet_containsEntries() {
        PatriciaTrie<String, String> trie = createTrie();
        trie.put("A", "1");
        trie.put("B", "2");
        Set<Map.Entry<String, String>> entrySet = trie.entrySet();
        assertEquals(2, entrySet.size());
    }

    @Test
    public void testValues_containsValues() {
        PatriciaTrie<String, String> trie = createTrie();
        trie.put("A", "1");
        trie.put("B", "2");
        Collection<String> values = trie.values();
        assertEquals(2, values.size());
        assertTrue(values.contains("1"));
        assertTrue(values.contains("2"));
    }

    @Test
    public void testKeySet_containsKeys() {
        PatriciaTrie<String, String> trie = createTrie();
        trie.put("A", "1");
        trie.put("B", "2");
        Set<String> keySet = trie.keySet();
        assertEquals(2, keySet.size());
        assertTrue(keySet.contains("A"));
        assertTrue(keySet.contains("B"));
    }

    @Test
    public void testPut_multipleKeys_sizeCorrect() {
        PatriciaTrie<Integer, String> trie = new PatriciaTrie<Integer, String>();
        trie.put(1, "one");
        trie.put(2, "two");
        trie.put(3, "three");
        assertEquals(3, trie.size());
        assertEquals("two", trie.get(2));
    }

    @Test
    public void testPut_replaceEmptyKey_returnsOldValue() {
        PatriciaTrie<String, String> trie = createTrie();
        trie.put("", "first");
        assertEquals("first", trie.put("", "second"));
        assertEquals("second", trie.get(""));
    }

    // ========== 新增测试用例 ==========

    @Test
    public void testIsEmpty_empty_returnsTrue() {
        assertTrue(createTrie().isEmpty());
    }

    @Test
    public void testIsEmpty_afterPut_returnsFalse() {
        PatriciaTrie<String, String> trie = createTrie();
        trie.put("A", "1");
        assertFalse(trie.isEmpty());
    }

    @Test
    public void testContainsValue_existingValue_returnsTrue() {
        PatriciaTrie<String, String> trie = createTrie();
        trie.put("A", "1");
        assertTrue(trie.containsValue("1"));
    }

    @Test
    public void testContainsValue_nonExistingValue_returnsFalse() {
        PatriciaTrie<String, String> trie = createTrie();
        trie.put("A", "1");
        assertFalse(trie.containsValue("2"));
    }

    @Test
    public void testPutAll_mergesEntries() {
        PatriciaTrie<String, String> trie = createTrie();
        Map<String, String> map = new HashMap<>();
        map.put("A", "1");
        map.put("B", "2");
        trie.putAll(map);
        assertEquals(2, trie.size());
        assertEquals("1", trie.get("A"));
        assertEquals("2", trie.get("B"));
    }

    @Test
    public void testEquals_sameContent_returnsTrue() {
        PatriciaTrie<String, String> trie1 = createTrie();
        PatriciaTrie<String, String> trie2 = createTrie();
        trie1.put("A", "1");
        trie2.put("A", "1");
        assertTrue(trie1.equals(trie2));
        assertEquals(trie1.hashCode(), trie2.hashCode());
    }

    @Test
    public void testHashCode_consistency() {
        PatriciaTrie<String, String> trie = createTrie();
        trie.put("A", "1");
        int hash1 = trie.hashCode();
        trie.put("B", "2");
        int hash2 = trie.hashCode();
        assertNotEquals(hash1, hash2);
        trie.clear();
        assertEquals(0, trie.hashCode());
    }

    @Test
    public void testMapIterator_remove_removesEntry() {
        PatriciaTrie<String, String> trie = createTrie();
        trie.put("A", "1");
        trie.put("B", "2");
        OrderedMapIterator<String, String> it = trie.mapIterator();
        it.next();
        it.remove();
        assertEquals(1, trie.size());
        assertFalse(trie.containsKey("A"));
    }

    @Test
    public void testKeySet_remove_removesFromMap() {
        PatriciaTrie<String, String> trie = createTrie();
        trie.put("A", "1");
        trie.put("B", "2");
        trie.keySet().remove("A");
        assertFalse(trie.containsKey("A"));
        assertEquals(1, trie.size());
    }

    @Test
    public void testSelectKey_nonExistingKey_returnsNull() {
        PatriciaTrie<String, String> trie = createTrie();
        trie.put("A", "1");
        assertNull(trie.selectKey("B"));
    }

    @Test
    public void testSelectValue_nonExistingKey_returnsNull() {
        PatriciaTrie<String, String> trie = createTrie();
        trie.put("A", "1");
        assertNull(trie.selectValue("B"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubMap_fromKeyGreaterThanToKey_throwsIllegalArgumentException() {
        PatriciaTrie<String, String> trie = createTrie();
        trie.put("A", "1");
        trie.subMap("C", "A");
    }

    @Test
    public void testPrefixMap_emptyPrefix_returnsAllEntries() {
        PatriciaTrie<String, String> trie = createTrie();
        trie.put("A", "1");
        trie.put("B", "2");
        SortedMap<String, String> prefixMap = trie.prefixMap("");
        assertEquals(2, prefixMap.size());
        assertTrue(prefixMap.containsKey("A"));
        assertTrue(prefixMap.containsKey("B"));
    }

    @Test
    public void testRemove_keyThatIsPrefix_doesNotAffectOtherKeys() {
        PatriciaTrie<String, String> trie = createTrie();
        trie.put("A", "1");
        trie.put("AB", "2");
        assertEquals("1", trie.remove("A"));
        assertNull(trie.get("A"));
        assertEquals("2", trie.get("AB"));
        assertEquals(1, trie.size());
    }

    @Test
    public void testNextKey_nonExistingKey_returnsNull() {
        PatriciaTrie<String, String> trie = createTrie();
        trie.put("A", "1");
        assertNull(trie.nextKey("B"));
    }

    @Test
    public void testPreviousKey_nonExistingKey_returnsNull() {
        PatriciaTrie<String, String> trie = createTrie();
        trie.put("B", "2");
        assertNull(trie.previousKey("A"));
    }

    @Test
    public void testMapIterator_previous_works() {
        PatriciaTrie<String, String> trie = createTrie();
        trie.put("A", "1");
        trie.put("B", "2");
        OrderedMapIterator<String, String> it = trie.mapIterator();
        assertTrue(it.hasNext());
        it.next(); // A
        assertTrue(it.hasNext());
        it.next(); // B
        assertFalse(it.hasNext());
        assertTrue(it.hasPrevious());
        assertEquals("A", it.previous());
        assertEquals("1", it.getValue());
    }

    @Test
    public void testValues_remove_removesEntry() {
        PatriciaTrie<String, String> trie = createTrie();
        trie.put("A", "1");
        trie.put("B", "2");
        trie.values().remove("1");
        assertFalse(trie.containsKey("A"));
        assertEquals(1, trie.size());
    }

    @Test
    public void testEntrySet_remove_removesEntry() {
        PatriciaTrie<String, String> trie = createTrie();
        trie.put("A", "1");
        trie.put("B", "2");
        Set<Map.Entry<String, String>> entrySet = trie.entrySet();
        Iterator<Map.Entry<String, String>> it = entrySet.iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> entry = it.next();
            if ("A".equals(entry.getKey())) {
                it.remove();
            }
        }
        assertFalse(trie.containsKey("A"));
        assertEquals(1, trie.size());
    }

    @Test
    public void testToString_nonEmpty_containsEntries() {
        PatriciaTrie<String, String> trie = createTrie();
        trie.put("A", "1");
        String str = trie.toString();
        assertTrue(str.contains("A"));
        assertTrue(str.contains("1"));
    }
}