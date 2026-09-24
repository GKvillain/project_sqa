package org.apache.commons.collections4.trie;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import org.apache.commons.collections4.OrderedMapIterator;
import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.Unmodifiable;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link UnmodifiableTrie}.
 */
public class UnmodifiableTrieTest {

    private Trie<String, String> backingTrie;
    private Trie<String, String> unmodifiableTrie;

    @Before
    public void setUp() {
        backingTrie = new PatriciaTrie<String>();
        backingTrie.put("alpha", "1");
        backingTrie.put("bravo", "2");
        backingTrie.put("charlie", "3");
        unmodifiableTrie = UnmodifiableTrie.unmodifiableTrie(backingTrie);
    }

    // Tests constructor with null trie throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullTrie_throwsException() {
        new UnmodifiableTrie<String, String>(null);
    }

    // Tests factory method with null trie throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testUnmodifiableTrie_nullTrie_throwsException() {
        UnmodifiableTrie.unmodifiableTrie(null);
    }

    // Tests unmodifiable interface marker implementation
    @Test
    public void testInstance_implementsUnmodifiable_returnsTrue() {
        assertTrue(unmodifiableTrie instanceof Unmodifiable);
    }

    // Tests query operations delegating to the underlying trie
    @Test
    public void testQueryMethods_validKeys_returnsExpectedResults() {
        assertEquals(3, unmodifiableTrie.size());
        assertFalse(unmodifiableTrie.isEmpty());
        assertTrue(unmodifiableTrie.containsKey("alpha"));
        assertFalse(unmodifiableTrie.containsKey("delta"));
        assertTrue(unmodifiableTrie.containsValue("1"));
        assertFalse(unmodifiableTrie.containsValue("99"));
        assertEquals("1", unmodifiableTrie.get("alpha"));
        assertNull(unmodifiableTrie.get("unknown"));
        assertEquals("alpha", unmodifiableTrie.firstKey());
        assertEquals("charlie", unmodifiableTrie.lastKey());
        assertEquals("bravo", unmodifiableTrie.nextKey("alpha"));
        assertEquals("alpha", unmodifiableTrie.previousKey("bravo"));
        assertNull(unmodifiableTrie.comparator());
    }

    // Tests clear throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testClear_invoked_throwsException() {
        unmodifiableTrie.clear();
    }

    // Tests put throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testPut_newEntry_throwsException() {
        unmodifiableTrie.put("delta", "4");
    }

    // Tests putAll throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testPutAll_mapInput_throwsException() {
        final Map<String, String> map = new HashMap<String, String>();
        map.put("delta", "4");
        unmodifiableTrie.putAll(map);
    }

    // Tests remove throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testRemove_existingKey_throwsException() {
        unmodifiableTrie.remove("alpha");
    }

    // Tests entrySet returns an unmodifiable set
    @Test(expected = UnsupportedOperationException.class)
    public void testEntrySet_modifySet_throwsException() {
        assertEquals(3, unmodifiableTrie.entrySet().size());
        unmodifiableTrie.entrySet().clear();
    }

    // Tests keySet returns an unmodifiable set
    @Test(expected = UnsupportedOperationException.class)
    public void testKeySet_modifySet_throwsException() {
        assertEquals(3, unmodifiableTrie.keySet().size());
        unmodifiableTrie.keySet().remove("alpha");
    }

    // Tests values returns an unmodifiable collection
    @Test(expected = UnsupportedOperationException.class)
    public void testValues_modifyCollection_throwsException() {
        assertEquals(3, unmodifiableTrie.values().size());
        unmodifiableTrie.values().clear();
    }

    // Tests prefixMap returns an unmodifiable submap
    @Test(expected = UnsupportedOperationException.class)
    public void testPrefixMap_modifyResult_throwsException() {
        final SortedMap<String, String> prefix = unmodifiableTrie.prefixMap("al");
        assertEquals(1, prefix.size());
        assertEquals("1", prefix.get("alpha"));
        prefix.clear();
    }

    // Tests headMap returns an unmodifiable submap
    @Test(expected = UnsupportedOperationException.class)
    public void testHeadMap_modifyResult_throwsException() {
        final SortedMap<String, String> head = unmodifiableTrie.headMap("bravo");
        assertEquals(1, head.size());
        assertTrue(head.containsKey("alpha"));
        head.clear();
    }

    // Tests tailMap returns an unmodifiable submap
    @Test(expected = UnsupportedOperationException.class)
    public void testTailMap_modifyResult_throwsException() {
        final SortedMap<String, String> tail = unmodifiableTrie.tailMap("bravo");
        assertEquals(2, tail.size());
        assertTrue(tail.containsKey("bravo"));
        assertTrue(tail.containsKey("charlie"));
        tail.clear();
    }

    // Tests subMap returns an unmodifiable submap
    @Test(expected = UnsupportedOperationException.class)
    public void testSubMap_modifyResult_throwsException() {
        final SortedMap<String, String> sub = unmodifiableTrie.subMap("alpha", "charlie");
        assertEquals(2, sub.size());
        sub.clear();
    }

    // Tests mapIterator is unmodifiable on setValue
    @Test(expected = UnsupportedOperationException.class)
    public void testMapIterator_setValue_throwsException() {
        final OrderedMapIterator<String, String> it = unmodifiableTrie.mapIterator();
        assertTrue(it.hasNext());
        it.next();
        it.setValue("modified");
    }

    // Tests mapIterator is unmodifiable on remove
    @Test(expected = UnsupportedOperationException.class)
    public void testMapIterator_remove_throwsException() {
        final OrderedMapIterator<String, String> it = unmodifiableTrie.mapIterator();
        assertTrue(it.hasNext());
        it.next();
        it.remove();
    }

    // Tests equals, hashCode and toString methods
    @Test
    public void testObjectMethods_validTrie_delegatesProperly() {
        assertEquals(backingTrie.hashCode(), unmodifiableTrie.hashCode());
        assertEquals(backingTrie.toString(), unmodifiableTrie.toString());
        assertEquals(backingTrie, unmodifiableTrie);
        assertEquals(unmodifiableTrie, backingTrie);
        assertNotEquals(unmodifiableTrie, Collections.emptyMap());
    }
}