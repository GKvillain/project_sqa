package org.apache.commons.collections.list;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

public class SetUniqueListTest {

    private SetUniqueList list;

    @Before
    public void setUp() {
        list = SetUniqueList.decorate(new ArrayList());
    }

    // Tests add method with unique element
    @Test
    public void testAdd_uniqueElement_returnsTrue() {
        assertTrue(list.add("A"));
        assertEquals(1, list.size());
        assertTrue(list.contains("A"));
    }

    // Tests add method with duplicate element
    @Test
    public void testAdd_duplicateElement_returnsFalse() {
        list.add("A");
        assertFalse(list.add("A"));
        assertEquals(1, list.size());
    }

    // Tests add at index with unique element
    @Test
    public void testAddAtIndex_uniqueElement_addsToList() {
        list.add("A");
        list.add("C");
        list.add(1, "B");
        assertEquals(Arrays.asList("A", "B", "C"), list);
        assertTrue(list.contains("B"));
    }

    // Tests add at index with duplicate element
    @Test
    public void testAddAtIndex_duplicateElement_doesNotAdd() {
        list.add("A");
        list.add("C");
        list.add(1, "A");
        assertEquals(Arrays.asList("A", "C"), list);
        assertFalse(list.contains("A") && list.size() == 3);
    }

    // Tests addAll with no duplicates
    @Test
    public void testAddAll_noDuplicates_addsAll() {
        list.add("A");
        List toAdd = Arrays.asList("B", "C");
        assertTrue(list.addAll(toAdd));
        assertEquals(Arrays.asList("A", "B", "C"), list);
    }

    // Tests addAll with duplicates
    @Test
    public void testAddAll_withDuplicates_addsOnlyUnique() {
        list.add("A");
        List toAdd = Arrays.asList("A", "B", "C");
        assertTrue(list.addAll(toAdd));
        assertEquals(Arrays.asList("A", "B", "C"), list);
        assertEquals(3, list.size());
    }

    // Tests addAll at index with duplicates
    @Test
    public void testAddAllAtIndex_withDuplicates_addsOnlyUnique() {
        list.add("A");
        list.add("D");
        List toAdd = Arrays.asList("A", "B", "C");
        assertTrue(list.addAll(1, toAdd));
        assertEquals(Arrays.asList("A", "B", "C", "D"), list);
        assertEquals(4, list.size());
    }

    // Tests addAll with empty collection
    @Test
    public void testAddAll_emptyCollection_returnsFalse() {
        list.add("A");
        assertFalse(list.addAll(new ArrayList()));
        assertEquals(Arrays.asList("A"), list);
    }

    // Tests set method with new object not in list
    @Test
    public void testSet_newObject_returnsOldValue() {
        list.add("A");
        list.add("B");
        Object old = list.set(0, "C");
        assertEquals("A", old);
        assertEquals(Arrays.asList("C", "B"), list);
        assertTrue(list.contains("C"));
    }

    // Tests set method with existing object at different index
    @Test
    public void testSet_existingObjectAtDifferentIndex_removesDuplicate() {
        list.add("A");
        list.add("B");
        list.add("C");
        Object old = list.set(0, "C");
        assertEquals("A", old);
        assertEquals(Arrays.asList("C", "B"), list);
        assertEquals(2, list.size());
    }

    // Tests set method with same object at same index
    @Test
    public void testSet_sameObjectSameIndex_keepsSingleElement() {
        list.add("A");
        list.add("B");
        Object old = list.set(0, "A");
        assertEquals("A", old);
        assertEquals(Arrays.asList("A", "B"), list);
        assertEquals(2, list.size());
    }

    // Tests remove by object
    @Test
    public void testRemoveByObject_existingObject_removesFromListAndSet() {
        list.add("A");
        list.add("B");
        assertTrue(list.remove("A"));
        assertEquals(Arrays.asList("B"), list);
        assertFalse(list.contains("A"));
    }

    // Tests remove by object not present
    @Test
    public void testRemoveByObject_notPresent_returnsFalse() {
        list.add("A");
        assertFalse(list.remove("B"));
        assertEquals(Arrays.asList("A"), list);
    }

    // Tests remove by index
    @Test
    public void testRemoveByIndex_existingIndex_removesElement() {
        list.add("A");
        list.add("B");
        Object removed = list.remove(0);
        assertEquals("A", removed);
        assertEquals(Arrays.asList("B"), list);
        assertFalse(list.contains("A"));
    }

    // Tests removeAll
    @Test
    public void testRemoveAll_removesMatchingElements() {
        list.add("A");
        list.add("B");
        list.add("C");
        assertTrue(list.removeAll(Arrays.asList("A", "C")));
        assertEquals(Arrays.asList("B"), list);
        assertFalse(list.contains("A"));
        assertFalse(list.contains("C"));
    }

    // Tests retainAll
    @Test
    public void testRetainAll_keepsOnlyMatchingElements() {
        list.add("A");
        list.add("B");
        list.add("C");
        assertTrue(list.retainAll(Arrays.asList("A", "C")));
        assertEquals(Arrays.asList("A", "C"), list);
        assertFalse(list.contains("B"));
    }

    // Tests clear method
    @Test
    public void testClear_removesAllElements() {
        list.add("A");
        list.add("B");
        list.clear();
        assertTrue(list.isEmpty());
        assertFalse(list.contains("A"));
    }

    // Tests contains and containsAll
    @Test
    public void testContainsAndContainsAll_checksSetMembership() {
        list.add("A");
        list.add("B");
        assertTrue(list.contains("A"));
        assertFalse(list.contains("C"));
        assertTrue(list.containsAll(Arrays.asList("A", "B")));
        assertFalse(list.containsAll(Arrays.asList("A", "C")));
    }

    // Tests subList returns SetUniqueList
    @Test
    public void testSubList_returnsSetUniqueList() {
        list.add("A");
        list.add("B");
        list.add("C");
        List sub = list.subList(1, 3);
        assertTrue(sub instanceof SetUniqueList);
        assertEquals(Arrays.asList("B", "C"), sub);
    }

    // Tests decorate with null list throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testDecorate_nullList_throwsException() {
        SetUniqueList.decorate(null);
    }

    // Tests decorate with list containing duplicates
    @Test
    public void testDecorate_listWithDuplicates_removesDuplicates() {
        List dupList = Arrays.asList("A", "B", "A", "C");
        SetUniqueList unique = SetUniqueList.decorate(new ArrayList(dupList));
        assertEquals(Arrays.asList("A", "B", "C"), unique);
    }

    // Tests iterator remove
    @Test
    public void testIterator_remove_removesFromSet() {
        list.add("A");
        list.add("B");
        java.util.Iterator it = list.iterator();
        it.next();
        it.remove();
        assertEquals(Arrays.asList("B"), list);
        assertFalse(list.contains("A"));
    }

    // Tests listIterator remove
    @Test
    public void testListIterator_remove_removesFromSet() {
        list.add("A");
        list.add("B");
        java.util.ListIterator it = list.listIterator();
        it.next();
        it.remove();
        assertEquals(Arrays.asList("B"), list);
        assertFalse(list.contains("A"));
    }
}