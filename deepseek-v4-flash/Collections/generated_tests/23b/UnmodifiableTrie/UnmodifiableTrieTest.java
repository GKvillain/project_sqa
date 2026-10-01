package org.apache.commons.collections4.trie;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;

import org.apache.commons.collections4.Trie;
import org.junit.Before;
import org.junit.Test;

public class UnmodifiableTrieTest {

    private Trie<String, String> delegate;
    private UnmodifiableTrie<String, String> trie;

    @Before
    public void setUp() {
        delegate = new PatriciaTrie<String, String>();
        delegate.put("key1", "value1");
        delegate.put("key2", "value2");
        delegate.put("key3", "value3");
        trie = UnmodifiableTrie.unmodifiableTrie(delegate);
    }

    // Tests that null delegate throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testUnmodifiableTrie_nullTrie_throwsIllegalArgumentException() {
        UnmodifiableTrie.unmodifiableTrie(null);
    }

    // Tests factory method returns non-null unmodifiable trie
    @Test
    public void testUnmodifiableTrie_validTrie_returnsUnmodifiableTrie() {
        UnmodifiableTrie<String, String> result = UnmodifiableTrie.unmodifiableTrie(delegate);
        assertNotNull(result);
    }

    // Tests that get returns correct value from delegate
    @Test
    public void testGet_existingKey_returnsValue() {
        assertEquals("value1", trie.get("key1"));
    }

    // Tests get for non-existing key returns null
    @Test
    public void testGet_nonExistingKey_returnsNull() {
        assertNull(trie.get("nonexistent"));
    }

    // Tests containsKey returns true for existing key
    @Test
    public void testContainsKey_existingKey_returnsTrue() {
        assertTrue(trie.containsKey("key1"));
    }

    // Tests containsKey returns false for non-existing key
    @Test
    public void testContainsKey_nonExistingKey_returnsFalse() {
        assertFalse(trie.containsKey("nonexistent"));
    }

    // Tests containsValue returns true for existing value
    @Test
    public void testContainsValue_existingValue_returnsTrue() {
        assertTrue(trie.containsValue("value1"));
    }

    // Tests containsValue returns false for non-existing value
    @Test
    public void testContainsValue_nonExistingValue_returnsFalse() {
        assertFalse(trie.containsValue("nonexistent"));
    }

    // Tests isEmpty returns false when delegate not empty
    @Test
    public void testIsEmpty_nonEmptyTrie_returnsFalse() {
        assertFalse(trie.isEmpty());
    }

    // Tests size returns delegate size
    @Test
    public void testSize_delegateHasThreeEntries_returnsThree() {
        assertEquals(3, trie.size());
    }

    // Tests clear throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testClear_anyState_throwsUnsupportedOperationException() {
        trie.clear();
    }

    // Tests put throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testPut_anyKeyValue_throwsUnsupportedOperationException() {
        trie.put("newKey", "newValue");
    }

    // Tests putAll throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testPutAll_anyMap_throwsUnsupportedOperationException() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("a", "b");
        trie.putAll(map);
    }

    // Tests remove throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testRemove_anyKey_throwsUnsupportedOperationException() {
        trie.remove("key1");
    }

    // Tests firstKey returns delegate's first key
    @Test
    public void testFirstKey_delegateHasKeys_returnsFirstKey() {
        assertEquals("key1", trie.firstKey());
    }

    // Tests lastKey returns delegate's last key
    @Test
    public void testLastKey_delegateHasKeys_returnsLastKey() {
        assertEquals("key3", trie.lastKey());
    }

    // Tests headMap returns unmodifiable sorted map
    @Test
    public void testHeadMap_validToKey_returnsUnmodifiableSortedMap() {
        SortedMap<String, String> head = trie.headMap("key2");
        assertNotNull(head);
        // Attempt modification should throw
        try {
            head.put("x", "y");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    // Tests subMap returns unmodifiable sorted map
    @Test
    public void testSubMap_validRange_returnsUnmodifiableSortedMap() {
        SortedMap<String, String> sub = trie.subMap("key1", "key3");
        assertNotNull(sub);
        assertEquals(2, sub.size());
    }

    // Tests tailMap returns unmodifiable sorted map
    @Test
    public void testTailMap_validFromKey_returnsUnmodifiableSortedMap() {
        SortedMap<String, String> tail = trie.tailMap("key2");
        assertNotNull(tail);
        assertEquals(2, tail.size());
    }

    // Tests prefixMap returns unmodifiable sorted map
    @Test
    public void testPrefixMap_validPrefix_returnsUnmodifiableSortedMap() {
        SortedMap<String, String> prefix = trie.prefixMap("key");
        assertNotNull(prefix);
        assertEquals(3, prefix.size());
    }

    // Tests entrySet returns unmodifiable set
    @Test
    public void testEntrySet_returnsUnmodifiableSet() {
        assertNotNull(trie.entrySet());
        assertEquals(3, trie.entrySet().size());
    }

    // Tests keySet returns unmodifiable set
    @Test
    public void testKeySet_returnsUnmodifiableSet() {
        assertNotNull(trie.keySet());
        assertEquals(3, trie.keySet().size());
    }

    // Tests values returns unmodifiable collection
    @Test
    public void testValues_returnsUnmodifiableCollection() {
        assertNotNull(trie.values());
        assertEquals(3, trie.values().size());
    }

    // Tests that changes to delegate after creation are reflected (delegation)
    @Test
    public void testDelegation_delegateModifiedAfterCreation_reflectsChanges() {
        delegate.put("newKey", "newValue");
        assertEquals("newValue", trie.get("newKey"));
        assertEquals(4, trie.size());
    }

    // Tests comparator returns delegate comparator (null for PatriciaTrie)
    @Test
    public void testComparator_defaultTrie_returnsNull() {
        assertNull(trie.comparator());
    }

    // Tests equals and hashCode delegate to underlying trie
    @Test
    public void testEquals_sameContent_returnsTrue() {
        Trie<String, String> otherDelegate = new PatriciaTrie<String, String>();
        otherDelegate.put("key1", "value1");
        otherDelegate.put("key2", "value2");
        otherDelegate.put("key3", "value3");
        UnmodifiableTrie<String, String> otherTrie = UnmodifiableTrie.unmodifiableTrie(otherDelegate);
        assertTrue(trie.equals(otherTrie));
    }

    @Test
    public void testHashCode_sameContent_returnsSameHashCode() {
        Trie<String, String> otherDelegate = new PatriciaTrie<String, String>();
        otherDelegate.put("key1", "value1");
        otherDelegate.put("key2", "value2");
        otherDelegate.put("key3", "value3");
        UnmodifiableTrie<String, String> otherTrie = UnmodifiableTrie.unmodifiableTrie(otherDelegate);
        assertEquals(trie.hashCode(), otherTrie.hashCode());
    }

    // Tests mapIterator returns unmodifiable iterator
    @Test
    public void testMapIterator_returnsUnmodifiableOrderedMapIterator() {
        assertNotNull(trie.mapIterator());
    }

    // Tests toString delegates to underlying trie
    @Test
    public void testToString_delegatesToUnderlying() {
        assertEquals(delegate.toString(), trie.toString());
    }

    // ===== New additional tests for uncovered scenarios =====

    // Tests isEmpty returns true for an empty trie
    @Test
    public void testIsEmpty_emptyTrie_returnsTrue() {
        UnmodifiableTrie<String, String> empty = UnmodifiableTrie.unmodifiableTrie(new PatriciaTrie<String, String>());
        assertTrue(empty.isEmpty());
    }

    // Tests size returns 0 for an empty trie
    @Test
    public void testSize_emptyTrie_returnsZero() {
        UnmodifiableTrie<String, String> empty = UnmodifiableTrie.unmodifiableTrie(new PatriciaTrie<String, String>());
        assertEquals(0, empty.size());
    }

    // Tests headMap with a key smaller than all existing keys returns empty map
    @Test
    public void testHeadMap_emptyKeyRange_returnsEmptyMap() {
        SortedMap<String, String> head = trie.headMap("key0");
        assertNotNull(head);
        assertTrue(head.isEmpty());
    }

    // Tests headMap modifications throw UnsupportedOperationException
    @Test
    public void testHeadMap_modification_throwsUnsupportedOperationException() {
        SortedMap<String, String> head = trie.headMap("key2");
        try {
            head.clear();
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    // Tests subMap with invalid range throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testSubMap_invalidRange_throwsIllegalArgumentException() {
        trie.subMap("key3", "key1");
    }

    // Tests subMap modifications throw UnsupportedOperationException
    @Test
    public void testSubMap_modification_throwsUnsupportedOperationException() {
        SortedMap<String, String> sub = trie.subMap("key1", "key3");
        try {
            sub.remove("key2");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    // Tests tailMap with a key larger than all existing keys returns empty map
    @Test
    public void testTailMap_fromKeyBeyondLast_returnsEmptyMap() {
        SortedMap<String, String> tail = trie.tailMap("key4");
        assertNotNull(tail);
        assertTrue(tail.isEmpty());
    }

    // Tests tailMap modifications throw UnsupportedOperationException
    @Test
    public void testTailMap_modification_throwsUnsupportedOperationException() {
        SortedMap<String, String> tail = trie.tailMap("key2");
        try {
            tail.put("newKey", "value");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    // Tests prefixMap with no matching prefix returns empty map
    @Test
    public void testPrefixMap_noMatch_returnsEmptyMap() {
        SortedMap<String, String> prefix = trie.prefixMap("xyz");
        assertNotNull(prefix);
        assertTrue(prefix.isEmpty());
    }

    // Tests prefixMap modifications throw UnsupportedOperationException
    @Test
    public void testPrefixMap_modification_throwsUnsupportedOperationException() {
        SortedMap<String, String> prefix = trie.prefixMap("key");
        try {
            prefix.remove("key1");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    // Tests entrySet.remove throws UnsupportedOperationException
    @Test
    public void testEntrySet_remove_throwsUnsupportedOperationException() {
        try {
            trie.entrySet().remove(trie.entrySet().iterator().next());
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    // Tests keySet.remove throws UnsupportedOperationException
    @Test
    public void testKeySet_remove_throwsUnsupportedOperationException() {
        try {
            trie.keySet().remove("key1");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    // Tests values.remove throws UnsupportedOperationException
    @Test
    public void testValues_remove_throwsUnsupportedOperationException() {
        try {
            trie.values().remove("value1");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    // Tests mapIterator.remove throws UnsupportedOperationException
    @Test
    public void testMapIterator_remove_throwsUnsupportedOperationException() {
        try {
            trie.mapIterator().remove();
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    // Tests mapIterator has next element when trie is not empty
    @Test
    public void testMapIterator_hasNext_returnsTrue() {
        assertTrue(trie.mapIterator().hasNext());
    }

    // Tests equals with null returns false
    @Test
    public void testEquals_null_returnsFalse() {
        assertFalse(trie.equals(null));
    }

    // Tests equals with a different content returns false
    @Test
    public void testEquals_differentObject_returnsFalse() {
        Trie<String, String> other = new PatriciaTrie<String, String>();
        other.put("different", "value");
        UnmodifiableTrie<String, String> otherTrie = UnmodifiableTrie.unmodifiableTrie(other);
        assertFalse(trie.equals(otherTrie));
    }
}