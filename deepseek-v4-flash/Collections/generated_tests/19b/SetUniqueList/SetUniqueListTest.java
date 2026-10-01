package org.apache.commons.collections.list;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.junit.Before;
import org.junit.Test;

public class SetUniqueListTest {

    private SetUniqueList<String> list;

    @Before
    public void setUp() {
        list = SetUniqueList.setUniqueList(new ArrayList<String>());
    }

    // Tests self-assignment (object already at same index) – should maintain set consistency
    @Test
    public void testSet_selfAssignment_elementRemovedFromSet() {
        list.add("A");
        list.add("B");
        list.set(0, "A");
        assertTrue("SetUniqueList should contain A after self-assignment", list.contains("A"));
        assertEquals(2, list.size());
    }

    // Tests set where object exists at different index – duplicate should be removed
    @Test
    public void testSet_duplicateAtDifferentIndex_removesDuplicateAndSet() {
        list.add("A");
        list.add("B");
        list.add("C");
        list.set(2, "A");
        assertEquals(2, list.size());
        assertEquals("B", list.get(0));
        assertEquals("A", list.get(1));
        assertTrue(list.contains("A"));
        assertTrue(list.contains("B"));
    }

    // Tests normal set with new element – replaces and syncs set
    @Test
    public void testSet_replaceWithNewElement_works() {
        list.add("A");
        list.add("B");
        String old = list.set(0, "C");
        assertEquals("A", old);
        assertEquals(2, list.size());
        assertEquals("C", list.get(0));
        assertEquals("B", list.get(1));
        assertFalse(list.contains("A"));
        assertTrue(list.contains("C"));
        assertTrue(list.contains("B"));
    }

    // Tests adding duplicate via add() returns false and does not change size
    @Test
    public void testAdd_duplicate_returnsFalse() {
        list.add("A");
        boolean result = list.add("A");
        assertFalse(result);
        assertEquals(1, list.size());
    }

    // Tests adding unique element via add() returns true
    @Test
    public void testAdd_normal_returnsTrue() {
        boolean result = list.add("A");
        assertTrue(result);
        assertEquals(1, list.size());
    }

    // Tests add(int, E) with duplicate – element not inserted
    @Test
    public void testAddAtIndex_duplicate_notAdded() {
        list.add("A");
        list.add("B");
        list.add(1, "A");
        assertEquals(2, list.size());
        assertEquals("A", list.get(0));
        assertEquals("B", list.get(1));
    }

    // Tests addAll with duplicates – only unique elements added
    @Test
    public void testAddAll_duplicateIgnored() {
        list.add("A");
        list.addAll(Arrays.asList("A", "B", "C"));
        assertEquals(3, list.size());
        assertTrue(list.contains("A"));
        assertTrue(list.contains("B"));
        assertTrue(list.contains("C"));
    }

    // Tests remove(Object) removes element from list and set
    @Test
    public void testRemove_object_removed() {
        list.add("A");
        list.add("B");
        boolean result = list.remove("A");
        assertTrue(result);
        assertEquals(1, list.size());
        assertFalse(list.contains("A"));
        assertTrue(list.contains("B"));
    }

    // Tests remove(int) returns removed element and updates set
    @Test
    public void testRemove_index_removed() {
        list.add("A");
        list.add("B");
        String removed = list.remove(0);
        assertEquals("A", removed);
        assertEquals(1, list.size());
        assertFalse(list.contains("A"));
        assertTrue(list.contains("B"));
    }

    // Tests removeAll with partial match
    @Test
    public void testRemoveAll_partialRemoval() {
        list.add("A");
        list.add("B");
        list.add("C");
        list.removeAll(Arrays.asList("A", "C"));
        assertEquals(1, list.size());
        assertEquals("B", list.get(0));
        assertFalse(list.contains("A"));
        assertTrue(list.contains("B"));
    }

    // Tests retainAll with subset – only retained elements remain
    @Test
    public void testRetainAll_subsetRetained() {
        list.add("A");
        list.add("B");
        list.add("C");
        boolean changed = list.retainAll(Arrays.asList("A", "C"));
        assertTrue(changed);
        assertEquals(2, list.size());
        assertTrue(list.contains("A"));
        assertTrue(list.contains("C"));
        assertFalse(list.contains("B"));
    }

    // Tests retainAll when all elements are already present – returns false
    @Test
    public void testRetainAll_noChange() {
        list.add("A");
        list.add("B");
        boolean changed = list.retainAll(Arrays.asList("A", "B"));
        assertFalse(changed);
        assertEquals(2, list.size());
    }

    // Tests retainAll with empty collection – clears list
    @Test
    public void testRetainAll_clearList() {
        list.add("A");
        list.add("B");
        boolean changed = list.retainAll(Collections.emptyList());
        assertTrue(changed);
        assertTrue(list.isEmpty());
    }

    // Tests clear removes all elements from list and set
    @Test
    public void testClear_clearsListAndSet() {
        list.add("A");
        list.add("B");
        list.clear();
        assertTrue(list.isEmpty());
        assertFalse(list.contains("A"));
        assertEquals(0, list.size());
    }

    // Tests contains works via set
    @Test
    public void testContains_elementFound() {
        list.add("A");
        assertTrue(list.contains("A"));
        assertFalse(list.contains("B"));
    }

    // Tests factory method removes duplicates from initial list
    @Test
    public void testSetUniqueList_factory_removesDuplicates() {
        List<String> input = new ArrayList<>(Arrays.asList("A", "B", "A", "C"));
        SetUniqueList<String> sul = SetUniqueList.setUniqueList(input);
        assertEquals(3, sul.size());
        assertEquals("A", sul.get(0));
        assertEquals("B", sul.get(1));
        assertEquals("C", sul.get(2));
    }

    // Tests factory method throws IllegalArgumentException on null list
    @Test(expected = IllegalArgumentException.class)
    public void testSetUniqueList_nullList_throwsIllegalArgumentException() {
        SetUniqueList.setUniqueList(null);
    }

    // Tests iterator remove updates set accordingly
    @Test
    public void testIterator_remove_syncsSet() {
        list.add("A");
        list.add("B");
        Iterator<String> it = list.iterator();
        it.next();
        it.remove();
        assertEquals(1, list.size());
        assertFalse(list.contains("A"));
        assertTrue(list.contains("B"));
    }

    // Tests listIterator add ignores duplicate
    @Test
    public void testListIterator_add_duplicate_ignored() {
        list.add("A");
        ListIterator<String> lit = list.listIterator();
        lit.add("A");
        assertEquals(1, list.size());
        assertEquals("A", list.get(0));
    }

    // Tests listIterator set throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testListIterator_set_throwsUnsupportedOperationException() {
        list.add("A");
        ListIterator<String> lit = list.listIterator();
        lit.next();
        lit.set("B");
    }

    // Tests subList returns a SetUniqueList containing correct elements
    @Test
    public void testSubList_containsCorrectElements() {
        list.add("A");
        list.add("B");
        list.add("C");
        List<String> sub = list.subList(1, 3);
        assertEquals(2, sub.size());
        assertEquals("B", sub.get(0));
        assertEquals("C", sub.get(1));
        assertTrue(sub instanceof SetUniqueList);
    }
}