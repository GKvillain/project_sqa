package org.apache.commons.collections4.list;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.junit.Test;

public class SetUniqueListTest {

    // Tests factory method with null list throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testSetUniqueList_nullList_throwsIllegalArgumentException() {
        SetUniqueList.setUniqueList(null);
    }

    // Tests factory method with duplicate elements removes duplicates
    @Test
    public void testSetUniqueList_listWithDuplicates_removesDuplicates() {
        List<String> input = new ArrayList<String>(Arrays.asList("a", "b", "a", "c"));
        SetUniqueList<String> list = SetUniqueList.setUniqueList(input);
        assertEquals(3, list.size());
        assertEquals(Arrays.asList("a", "b", "c"), list);
    }

    // Tests add(E) for a new element
    @Test
    public void testAdd_uniqueElement_returnsTrueAndAdds() {
        SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>());
        assertTrue(list.add("x"));
        assertEquals(1, list.size());
        assertTrue(list.contains("x"));
    }

    // Tests add(E) for a duplicate element
    @Test
    public void testAdd_duplicateElement_returnsFalseAndNotAdd() {
        SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>());
        list.add("x");
        assertFalse(list.add("x"));
        assertEquals(1, list.size());
    }

    // Tests add(index, E) with a duplicate at the target position
    @Test
    public void testAddAtIndex_duplicateElement_notAdded() {
        SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>());
        list.add("a");
        list.add("b");
        list.add(1, "a");
        assertEquals(2, list.size());
        assertEquals(Arrays.asList("a", "b"), list);
    }

    // Tests addAll(Collection) with duplicates in the incoming collection
    @Test
    public void testAddAll_collectionWithDuplicates_onlyUniqueAdded() {
        SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>());
        list.add("a");
        boolean changed = list.addAll(Arrays.asList("a", "b", "b", "c"));
        assertTrue(changed);
        assertEquals(3, list.size());
        assertTrue(list.contains("a"));
        assertTrue(list.contains("b"));
        assertTrue(list.contains("c"));
    }

    // Tests addAll(Collection) when all elements are already present
    @Test
    public void testAddAll_collectionAllDuplicates_returnsFalse() {
        SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>());
        list.add("a");
        boolean changed = list.addAll(Arrays.asList("a", "a"));
        assertFalse(changed);
        assertEquals(1, list.size());
    }

    // Tests addAll(int, Collection) with duplicates, verifies correct order
    @Test
    public void testAddAllAtIndex_duplicatesFiltered() {
        SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>());
        list.add("a");
        list.add("c");
        boolean changed = list.addAll(1, Arrays.asList("b", "a", "b"));
        assertTrue(changed);
        assertEquals(3, list.size());
        assertEquals(Arrays.asList("a", "b", "c"), list);
    }

    // Tests set(index, element) when the duplicate appears before the index
    @Test
    public void testSet_duplicateElementBeforeIndex_removesOriginalAndPreservesMiddleElement() {
        SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>());
        list.add("A");
        list.add("B");
        list.add("C");
        String removed = list.set(2, "A");
        assertEquals("C", removed);
        assertEquals(3, list.size());
        assertEquals(Arrays.asList("B", "C", "A"), list);
    }

    // Tests set(index, element) when the duplicate appears after the index
    @Test
    public void testSet_duplicateElementAfterIndex_removesDuplicate() {
        SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>());
        list.add("A");
        list.add("B");
        list.add("C");
        String removed = list.set(0, "C");
        assertEquals("A", removed);
        assertEquals(2, list.size());
        assertEquals(Arrays.asList("C", "B"), list);
    }

    // Tests set(index, element) with a completely new element
    @Test
    public void testSet_uniqueElement_replacesOld() {
        SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>());
        list.add("a");
        list.add("b");
        String removed = list.set(1, "c");
        assertEquals("b", removed);
        assertEquals(2, list.size());
        assertEquals(Arrays.asList("a", "c"), list);
    }

    // Tests remove(Object) for a present element
    @Test
    public void testRemove_objectPresent_returnsTrueAndRemoves() {
        SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>());
        list.add("a");
        list.add("b");
        assertTrue(list.remove("a"));
        assertEquals(1, list.size());
        assertFalse(list.contains("a"));
        assertTrue(list.contains("b"));
    }

    // Tests remove(Object) for an absent element
    @Test
    public void testRemove_objectAbsent_returnsFalse() {
        SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>());
        list.add("a");
        assertFalse(list.remove("z"));
        assertEquals(1, list.size());
    }

    // Tests remove(int) and verifies the set is updated
    @Test
    public void testRemove_index_removesAndUpdatesSet() {
        SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>());
        list.add("a");
        list.add("b");
        String removed = list.remove(0);
        assertEquals("a", removed);
        assertEquals(1, list.size());
        assertFalse(list.contains("a"));
        assertTrue(list.contains("b"));
    }

    // Tests removeAll(Collection) removes all matching elements
    @Test
    public void testRemoveAll_removesMatchingAndReturnsTrue() {
        SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>());
        list.add("a");
        list.add("b");
        list.add("c");
        assertTrue(list.removeAll(Arrays.asList("a", "c")));
        assertEquals(1, list.size());
        assertTrue(list.contains("b"));
    }

    // Tests retainAll(Collection) when only a subset should be kept
    @Test
    public void testRetainAll_keepMatching_removesOthers() {
        SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>());
        list.add("a");
        list.add("b");
        list.add("c");
        assertTrue(list.retainAll(Arrays.asList("b")));
        assertEquals(1, list.size());
        assertTrue(list.contains("b"));
    }

    // Tests retainAll(Collection) when all elements are retained
    @Test
    public void testRetainAll_allKept_returnsFalse() {
        SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>());
        list.add("a");
        list.add("b");
        assertFalse(list.retainAll(Arrays.asList("a", "b")));
        assertEquals(2, list.size());
    }

    // Tests clear() clears both the list and the set
    @Test
    public void testClear_clearsBothListAndSet() {
        SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>());
        list.add("a");
        list.add("b");
        list.clear();
        assertEquals(0, list.size());
        assertFalse(list.contains("a"));
        assertFalse(list.contains("b"));
    }

    // Tests iterator.remove() removes the element from both list and set
    @Test
    public void testIterator_removeRemovesFromSet() {
        SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>());
        list.add("a");
        list.add("b");
        Iterator<String> it = list.iterator();
        it.next();
        it.remove();
        assertEquals(1, list.size());
        assertFalse(list.contains("a"));
        assertTrue(list.contains("b"));
    }

    // Tests ListIterator.add() with a duplicate element (should be ignored)
    @Test
    public void testListIterator_addDuplicate_skips() {
        SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>());
        list.add("a");
        list.add("b");
        ListIterator<String> it = list.listIterator();
        it.next();
        it.add("a");
        assertEquals(2, list.size());
        assertTrue(list.contains("a"));
        assertTrue(list.contains("b"));
    }

    // Tests ListIterator.add() with a unique element (should be added)
    @Test
    public void testListIterator_addUnique_addsAndAdvances() {
        SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>());
        list.add("a");
        ListIterator<String> it = list.listIterator();
        it.next();
        it.add("b");
        assertEquals(2, list.size());
        assertTrue(list.contains("b"));
    }

    // Tests ListIterator.set() is unsupported
    @Test(expected = UnsupportedOperationException.class)
    public void testListIterator_set_throwsUnsupportedOperationException() {
        SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>());
        list.add("a");
        ListIterator<String> it = list.listIterator();
        it.next();
        it.set("b");
    }

    // Tests subList returns a working unique list
    @Test
    public void testSubList_returnsUniqueList() {
        SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>());
        list.add("a");
        list.add("b");
        list.add("c");
        List<String> sub = list.subList(0, 2);
        assertEquals(2, sub.size());
        assertEquals(Arrays.asList("a", "b"), sub);
    }

    // Tests asSet returns an unmodifiable set view
    @Test(expected = UnsupportedOperationException.class)
    public void testAsSet_unmodifiable_throwsOnAdd() {
        SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>());
        list.add("a");
        Set<String> setView = list.asSet();
        setView.add("b");
    }
}