package org.apache.commons.collections.set;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

public class ListOrderedSetTest {

    private ListOrderedSet<String> orderedSet;

    @Before
    public void setUp() {
        orderedSet = new ListOrderedSet<String>();
    }

    // Tests normal add operation
    @Test
    public void testAdd_newElement_returnsTrue() {
        assertTrue(orderedSet.add("A"));
        assertEquals(1, orderedSet.size());
        assertEquals("A", orderedSet.get(0));
    }

    // Tests adding duplicate element
    @Test
    public void testAdd_duplicateElement_returnsFalse() {
        orderedSet.add("A");
        assertFalse(orderedSet.add("A"));
        assertEquals(1, orderedSet.size());
    }

    // Tests addAll with mixed unique and duplicate elements
    @Test
    public void testAddAll_mixedElements_returnsTrue() {
        orderedSet.add("A");
        List<String> list = Arrays.asList("A", "B", "C");
        assertTrue(orderedSet.addAll(list));
        assertEquals(3, orderedSet.size());
        assertEquals("A", orderedSet.get(0));
        assertEquals("B", orderedSet.get(1));
        assertEquals("C", orderedSet.get(2));
    }

    // Tests addAll with all duplicate elements
    @Test
    public void testAddAll_allDuplicates_returnsFalse() {
        orderedSet.add("A");
        List<String> list = Arrays.asList("A");
        assertFalse(orderedSet.addAll(list));
        assertEquals(1, orderedSet.size());
    }

    // Tests removal of existing element
    @Test
    public void testRemove_existingElement_returnsTrue() {
        orderedSet.add("A");
        orderedSet.add("B");
        assertTrue(orderedSet.remove("A"));
        assertEquals(1, orderedSet.size());
        assertEquals("B", orderedSet.get(0));
    }

    // Tests removal of non-existing element
    @Test
    public void testRemove_nonExistingElement_returnsFalse() {
        orderedSet.add("A");
        assertFalse(orderedSet.remove("B"));
        assertEquals(1, orderedSet.size());
    }

    // Tests retainAll when elements removed
    @Test
    public void testRetainAll_removesElements_returnsTrue() {
        orderedSet.add("A");
        orderedSet.add("B");
        orderedSet.add("C");
        List<String> retainList = Arrays.asList("A", "C");
        assertTrue(orderedSet.retainAll(retainList));
        assertEquals(2, orderedSet.size());
        assertEquals("A", orderedSet.get(0));
        assertEquals("C", orderedSet.get(1));
    }

    // Tests retainAll when no elements removed
    @Test
    public void testRetainAll_noChange_returnsFalse() {
        orderedSet.add("A");
        orderedSet.add("B");
        List<String> retainList = Arrays.asList("A", "B", "C");
        assertFalse(orderedSet.retainAll(retainList));
        assertEquals(2, orderedSet.size());
    }

    // Tests retainAll when set becomes empty
    @Test
    public void testRetainAll_emptySet_returnsTrue() {
        orderedSet.add("A");
        orderedSet.add("B");
        List<String> retainList = Arrays.asList();
        assertTrue(orderedSet.retainAll(retainList));
        assertTrue(orderedSet.isEmpty());
    }

    // Tests clear operation
    @Test
    public void testClear_clearsEverything() {
        orderedSet.add("A");
        orderedSet.add("B");
        orderedSet.clear();
        assertTrue(orderedSet.isEmpty());
        assertEquals(0, orderedSet.asList().size());
    }

    // Tests get at valid index
    @Test
    public void testGet_validIndex_returnsElement() {
        orderedSet.add("A");
        orderedSet.add("B");
        assertEquals("A", orderedSet.get(0));
        assertEquals("B", orderedSet.get(1));
    }

    // Tests indexOf for existing element
    @Test
    public void testIndexOf_existingElement_returnsIndex() {
        orderedSet.add("A");
        orderedSet.add("B");
        assertEquals(0, orderedSet.indexOf("A"));
        assertEquals(1, orderedSet.indexOf("B"));
    }

    // Tests indexOf for non-existing element
    @Test
    public void testIndexOf_nonExistingElement_returnsMinusOne() {
        orderedSet.add("A");
        assertEquals(-1, orderedSet.indexOf("B"));
    }

    // Tests add at specific index with new element
    @Test
    public void testAdd_indexAndNewElement_addedAtPosition() {
        orderedSet.add("A");
        orderedSet.add("C");
        orderedSet.add(1, "B");
        assertEquals(3, orderedSet.size());
        assertEquals("A", orderedSet.get(0));
        assertEquals("B", orderedSet.get(1));
        assertEquals("C", orderedSet.get(2));
    }

    // Tests add at specific index with duplicate element
    @Test
    public void testAdd_indexAndDuplicateElement_notAdded() {
        orderedSet.add("A");
        orderedSet.add("B");
        orderedSet.add(0, "A");
        assertEquals(2, orderedSet.size());
        assertEquals("A", orderedSet.get(0));
    }

    // Tests addAll at specific index with new elements
    @Test
    public void testAddAll_indexAndNewElements_addedAtPosition() {
        orderedSet.add("A");
        orderedSet.add("D");
        List<String> list = Arrays.asList("B", "C");
        assertTrue(orderedSet.addAll(1, list));
        assertEquals(4, orderedSet.size());
        assertEquals("A", orderedSet.get(0));
        assertEquals("B", orderedSet.get(1));
        assertEquals("C", orderedSet.get(2));
        assertEquals("D", orderedSet.get(3));
    }

    // Tests addAll at specific index with no new elements
    @Test
    public void testAddAll_indexAndNoNewElements_returnsFalse() {
        orderedSet.add("A");
        orderedSet.add("B");
        List<String> list = Arrays.asList("A");
        assertFalse(orderedSet.addAll(0, list));
        assertEquals(2, orderedSet.size());
    }

    // Tests remove at index
    @Test
    public void testRemove_index_removesElementAndReturnsIt() {
        orderedSet.add("A");
        orderedSet.add("B");
        assertEquals("A", orderedSet.remove(0));
        assertEquals(1, orderedSet.size());
        assertEquals("B", orderedSet.get(0));
    }

    // Tests asList returns unmodifiable list
    @Test
    public void testAsList_returnsUnmodifiableList() {
        orderedSet.add("A");
        List<String> list = orderedSet.asList();
        assertEquals(1, list.size());
        try {
            list.add("B");
            fail("Should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    // Tests toString uses underlying list
    @Test
    public void testToString_returnsListString() {
        orderedSet.add("A");
        orderedSet.add("B");
        assertEquals("[A, B]", orderedSet.toString());
    }

    // Tests iterator removes element correctly
    @Test
    public void testIterator_remove_removesFromBothListAndSet() {
        orderedSet.add("A");
        orderedSet.add("B");
        orderedSet.add("C");
        java.util.Iterator<String> it = orderedSet.iterator();
        it.next(); // A
        it.remove();
        assertEquals(2, orderedSet.size());
        assertEquals("B", orderedSet.get(0));
        assertFalse(orderedSet.contains("A"));
    }

    // Tests factory method with list containing duplicates
    @Test
    public void testListOrderedSet_listWithDuplicates_removesDuplicates() {
        List<String> list = new ArrayList<String>(Arrays.asList("A", "B", "A"));
        ListOrderedSet<String> set = ListOrderedSet.listOrderedSet(list);
        assertEquals(2, set.size());
        assertEquals("A", set.get(0));
        assertEquals("B", set.get(1));
    }

    // Tests factory method with null list throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testListOrderedSet_nullList_throwsException() {
        ListOrderedSet.listOrderedSet((List<String>) null);
    }

    // Tests factory method with null set throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testListOrderedSet_nullSet_throwsException() {
        ListOrderedSet.listOrderedSet((Set<String>) null);
    }
}