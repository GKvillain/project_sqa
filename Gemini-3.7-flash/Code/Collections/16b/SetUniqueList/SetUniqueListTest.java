package org.apache.commons.collections.list;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class SetUniqueListTest {

    private List decoratedList;
    private SetUniqueList uniqueList;

    @Before
    public void setUp() {
        decoratedList = new ArrayList();
        uniqueList = SetUniqueList.decorate(decoratedList);
    }

    // Tests factory decorate with null input throwing exception
    @Test(expected = IllegalArgumentException.class)
    public void testDecorate_nullList_throwsException() {
        SetUniqueList.decorate(null);
    }

    // Tests factory decorate with empty list
    @Test
    public void testDecorate_emptyList_createsEmptySetUniqueList() {
        List empty = new ArrayList();
        SetUniqueList list = SetUniqueList.decorate(empty);
        assertTrue(list.isEmpty());
        assertEquals(0, list.size());
    }

    // Tests factory decorate with duplicates retains first occurrences
    @Test
    public void testDecorate_listWithDuplicates_removesDuplicates() {
        List input = new ArrayList(Arrays.asList("A", "B", "A", "C", "B"));
        SetUniqueList list = SetUniqueList.decorate(input);
        assertEquals(3, list.size());
        assertEquals("A", list.get(0));
        assertEquals("B", list.get(1));
        assertEquals("C", list.get(2));
    }

    // Tests constructor with null set throwing exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullSet_throwsException() {
        new SetUniqueList(new ArrayList(), null);
    }

    // Tests asSet returns unmodifiable set view
    @Test(expected = UnsupportedOperationException.class)
    public void testAsSet_returnsUnmodifiableSet_modificationThrowsException() {
        uniqueList.add("A");
        Set set = uniqueList.asSet();
        assertEquals(1, set.size());
        assertTrue(set.contains("A"));
        set.add("B");
    }

    // Tests add method with unique and duplicate items
    @Test
    public void testAdd_uniqueAndDuplicateElements_returnsExpectedBoolean() {
        assertTrue(uniqueList.add("A"));
        assertTrue(uniqueList.add("B"));
        assertFalse(uniqueList.add("A"));
        assertEquals(2, uniqueList.size());
        assertTrue(uniqueList.contains("A"));
        assertTrue(uniqueList.contains("B"));
    }

    // Tests add at index with unique and duplicate elements
    @Test
    public void testAddInt_uniqueAndDuplicateElements_insertsCorrectly() {
        uniqueList.add("A");
        uniqueList.add("C");
        uniqueList.add(1, "B");
        assertEquals(3, uniqueList.size());
        assertEquals("B", uniqueList.get(1));

        // Attempting to add duplicate at index should be ignored
        uniqueList.add(0, "C");
        assertEquals(3, uniqueList.size());
        assertEquals("A", uniqueList.get(0));
    }

    // Tests addAll at the end of the list
    @Test
    public void testAddAll_collectionWithDuplicates_addsOnlyUnique() {
        uniqueList.add("A");
        List toAdd = Arrays.asList("B", "A", "C", "B");
        assertTrue(uniqueList.addAll(toAdd));
        assertEquals(3, uniqueList.size());
        assertEquals("A", uniqueList.get(0));
        assertEquals("B", uniqueList.get(1));
        assertEquals("C", uniqueList.get(2));
        assertFalse(uniqueList.addAll(Arrays.asList("A", "B")));
    }

    // Tests addAll at a specific index
    @Test
    public void testAddAllInt_collectionWithDuplicates_insertsInOrder() {
        uniqueList.add("A");
        uniqueList.add("D");
        List toAdd = Arrays.asList("B", "A", "C");
        assertTrue(uniqueList.addAll(1, toAdd));
        assertEquals(4, uniqueList.size());
        assertEquals("A", uniqueList.get(0));
        assertEquals("B", uniqueList.get(1));
        assertEquals("C", uniqueList.get(2));
        assertEquals("D", uniqueList.get(3));
    }

    // Tests set method replacing element with new unique element
    @Test
    public void testSet_newElement_replacesAndReturnsPrevious() {
        uniqueList.add("A");
        uniqueList.add("B");
        Object old = uniqueList.set(1, "C");
        assertEquals("B", old);
        assertEquals(2, uniqueList.size());
        assertEquals("C", uniqueList.get(1));
        assertTrue(uniqueList.contains("C"));
        assertFalse(uniqueList.contains("B"));
    }

    // Tests set method when setting element to itself (Defects4J bug 16 regression test)
    @Test
    public void testSet_sameElement_retainsElementInSetAndList() {
        uniqueList.add("A");
        uniqueList.add("B");
        Object old = uniqueList.set(0, "A");
        assertEquals("A", old);
        assertEquals(2, uniqueList.size());
        assertTrue(uniqueList.contains("A"));
        // Ensuring set still maintains "A", so adding "A" again should return false
        assertFalse(uniqueList.add("A"));
        assertEquals(2, uniqueList.size());
    }

    // Tests set method when target element is already at another position
    @Test
    public void testSet_elementAlreadyPresentAtDifferentIndex_movesElement() {
        uniqueList.add("A");
        uniqueList.add("B");
        uniqueList.add("C");
        Object old = uniqueList.set(0, "C");
        assertEquals("A", old);
        assertEquals(2, uniqueList.size());
        assertEquals("C", uniqueList.get(0));
        assertEquals("B", uniqueList.get(1));
        assertTrue(uniqueList.contains("C"));
        assertFalse(uniqueList.contains("A"));
    }

    // Tests remove by object and remove by index
    @Test
    public void testRemove_byObjectAndIndex_removesFromListAndSet() {
        uniqueList.add("A");
        uniqueList.add("B");
        uniqueList.add("C");

        assertTrue(uniqueList.remove("B"));
        assertFalse(uniqueList.contains("B"));
        assertEquals(2, uniqueList.size());

        Object removed = uniqueList.remove(0);
        assertEquals("A", removed);
        assertFalse(uniqueList.contains("A"));
        assertEquals(1, uniqueList.size());
        assertEquals("C", uniqueList.get(0));
    }

    // Tests removeAll and retainAll methods
    @Test
    public void testRemoveAllAndRetainAll_validCollections_updatesListAndSet() {
        uniqueList.add("A");
        uniqueList.add("B");
        uniqueList.add("C");
        uniqueList.add("D");

        assertTrue(uniqueList.removeAll(Arrays.asList("A", "C")));
        assertEquals(2, uniqueList.size());
        assertFalse(uniqueList.contains("A"));
        assertFalse(uniqueList.contains("C"));

        assertTrue(uniqueList.retainAll(Arrays.asList("B", "Z")));
        assertEquals(1, uniqueList.size());
        assertTrue(uniqueList.contains("B"));
        assertFalse(uniqueList.contains("D"));
    }

    // Tests clear method
    @Test
    public void testClear_nonEmptyList_clearsListAndSet() {
        uniqueList.add("A");
        uniqueList.add("B");
        uniqueList.clear();
        assertEquals(0, uniqueList.size());
        assertTrue(uniqueList.isEmpty());
        assertFalse(uniqueList.contains("A"));
        assertFalse(uniqueList.contains("B"));
    }

    // Tests containsAll method
    @Test
    public void testContainsAll_subsets_returnsExpectedBoolean() {
        uniqueList.add("A");
        uniqueList.add("B");
        assertTrue(uniqueList.containsAll(Arrays.asList("A", "B")));
        assertTrue(uniqueList.containsAll(Arrays.asList("A")));
        assertFalse(uniqueList.containsAll(Arrays.asList("A", "C")));
    }

    // Tests iterator traversal and removal
    @Test
    public void testIterator_traversalAndRemove_updatesListAndSet() {
        uniqueList.add("A");
        uniqueList.add("B");
        Iterator it = uniqueList.iterator();
        assertTrue(it.hasNext());
        assertEquals("A", it.next());
        it.remove();
        assertEquals(1, uniqueList.size());
        assertFalse(uniqueList.contains("A"));
        assertTrue(it.hasNext());
        assertEquals("B", it.next());
        assertFalse(it.hasNext());
    }

    // Tests listIterator add, previous, and unsupported set
    @Test
    public void testListIterator_operations_behaveCorrectly() {
        uniqueList.add("A");
        uniqueList.add("C");
        ListIterator lit = uniqueList.listIterator();
        assertEquals("A", lit.next());
        lit.add("B");
        assertEquals("C", lit.next());
        assertEquals("C", lit.previous());

        try {
            lit.set("X");
            fail("ListIterator.set should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {
            // expected
        }

        // Duplicate add via listIterator should be ignored
        lit.add("A");
        assertEquals(3, uniqueList.size());
    }

    // Tests listIterator starting from a specific index
    @Test
    public void testListIteratorInt_validIndex_startsAtPosition() {
        uniqueList.add("A");
        uniqueList.add("B");
        uniqueList.add("C");
        ListIterator lit = uniqueList.listIterator(1);
        assertEquals("B", lit.next());
        assertEquals("B", lit.previous());
    }

    // Tests subList returns decorated SetUniqueList
    @Test
    public void testSubList_validRange_returnsSetUniqueList() {
        uniqueList.add("A");
        uniqueList.add("B");
        uniqueList.add("C");
        List sub = uniqueList.subList(1, 3);
        assertNotNull(sub);
        assertEquals(2, sub.size());
        assertEquals("B", sub.get(0));
        assertEquals("C", sub.get(1));
        assertTrue(sub instanceof SetUniqueList);
    }
}