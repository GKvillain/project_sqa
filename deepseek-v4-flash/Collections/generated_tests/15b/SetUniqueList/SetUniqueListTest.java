package org.apache.commons.collections.list;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;

public class SetUniqueListTest {

    @Test(expected = IllegalArgumentException.class)
    public void testDecorate_nullList_throwsIllegalArgumentException() {
        SetUniqueList.decorate(null);
    }

    @Test
    public void testDecorate_emptyList_createsEmptyUniqueList() {
        List<String> list = new ArrayList<String>();
        SetUniqueList unique = SetUniqueList.decorate(list);
        assertTrue(unique.isEmpty());
        assertEquals(0, unique.size());
    }

    @Test
    public void testDecorate_listWithDuplicates_removesDuplicates() {
        List<String> list = new ArrayList<String>();
        list.add("A");
        list.add("B");
        list.add("A");
        SetUniqueList unique = SetUniqueList.decorate(list);
        assertEquals(2, unique.size());
        assertTrue(unique.contains("A"));
        assertTrue(unique.contains("B"));
        assertEquals("A", unique.get(0));
        assertEquals("B", unique.get(1));
    }

    @Test
    public void testAdd_newObject_returnsTrue() {
        List<String> list = new ArrayList<String>();
        SetUniqueList unique = SetUniqueList.decorate(list);
        assertTrue(unique.add("A"));
        assertEquals(1, unique.size());
    }

    @Test
    public void testAdd_duplicateObject_returnsFalse() {
        List<String> list = new ArrayList<String>();
        list.add("A");
        SetUniqueList unique = SetUniqueList.decorate(list);
        assertFalse(unique.add("A"));
        assertEquals(1, unique.size());
    }

    @Test
    public void testAddAtIndex_newObject_success() {
        List<String> list = new ArrayList<String>();
        list.add("A");
        list.add("C");
        SetUniqueList unique = SetUniqueList.decorate(list);
        unique.add(1, "B");
        assertEquals(3, unique.size());
        assertEquals("A", unique.get(0));
        assertEquals("B", unique.get(1));
        assertEquals("C", unique.get(2));
    }

    @Test
    public void testAddAtIndex_duplicateObject_noChange() {
        List<String> list = new ArrayList<String>();
        list.add("A");
        list.add("B");
        SetUniqueList unique = SetUniqueList.decorate(list);
        unique.add(1, "A");
        assertEquals(2, unique.size());
        assertEquals("A", unique.get(0));
        assertEquals("B", unique.get(1));
    }

    @Test
    public void testSet_newObjectNotInList_updatesSet() {
        List<String> list = new ArrayList<String>();
        list.add("A");
        list.add("B");
        list.add("C");
        SetUniqueList unique = SetUniqueList.decorate(list);
        Object removed = unique.set(1, "D");
        assertEquals("B", removed);
        assertEquals(3, unique.size());
        assertEquals("A", unique.get(0));
        assertEquals("D", unique.get(1));
        assertEquals("C", unique.get(2));
        assertTrue(unique.contains("D"));
        assertFalse(unique.contains("B"));
    }

    @Test
    public void testSet_existingObjectAtSameIndex_noChange() {
        List<String> list = new ArrayList<String>();
        list.add("A");
        list.add("B");
        SetUniqueList unique = SetUniqueList.decorate(list);
        Object removed = unique.set(0, "A");
        assertEquals("A", removed);
        assertEquals(2, unique.size());
        assertTrue(unique.contains("A"));
        assertTrue(unique.contains("B"));
    }

    @Test
    public void testSet_existingObjectAtDifferentIndex_removesDuplicate() {
        List<String> list = new ArrayList<String>();
        list.add("A");
        list.add("B");
        list.add("C");
        SetUniqueList unique = SetUniqueList.decorate(list);
        Object removed = unique.set(2, "A");
        assertEquals("C", removed);
        assertEquals(2, unique.size());
        assertEquals("B", unique.get(0));
        assertEquals("A", unique.get(1));
        assertTrue(unique.contains("A"));
        assertTrue(unique.contains("B"));
        assertFalse(unique.contains("C"));
    }

    @Test
    public void testAddAll_newCollection_addsOnlyUnique() {
        List<String> list = new ArrayList<String>();
        list.add("A");
        SetUniqueList unique = SetUniqueList.decorate(list);
        Collection<String> coll = Arrays.asList("A", "B", "C");
        boolean changed = unique.addAll(coll);
        assertTrue(changed);
        assertEquals(3, unique.size());
        assertTrue(unique.contains("B"));
        assertTrue(unique.contains("C"));
    }

    @Test
    public void testAddAll_duplicateCollection_noChange() {
        List<String> list = new ArrayList<String>();
        list.add("A");
        SetUniqueList unique = SetUniqueList.decorate(list);
        Collection<String> coll = Arrays.asList("A");
        boolean changed = unique.addAll(coll);
        assertFalse(changed);
        assertEquals(1, unique.size());
    }

    @Test
    public void testAddAllAtIndex_withDuplicates() {
        List<String> list = new ArrayList<String>();
        list.add("A");
        list.add("D");
        SetUniqueList unique = SetUniqueList.decorate(list);
        Collection<String> coll = Arrays.asList("B", "A", "C");
        boolean changed = unique.addAll(1, coll);
        assertTrue(changed);
        assertEquals(4, unique.size());
        assertEquals("A", unique.get(0));
        assertEquals("B", unique.get(1));
        assertEquals("C", unique.get(2));
        assertEquals("D", unique.get(3));
    }

    @Test
    public void testRemove_object_removesFromBoth() {
        List<String> list = new ArrayList<String>();
        list.add("A");
        list.add("B");
        SetUniqueList unique = SetUniqueList.decorate(list);
        assertTrue(unique.remove("A"));
        assertEquals(1, unique.size());
        assertFalse(unique.contains("A"));
        assertTrue(unique.contains("B"));
    }

    @Test
    public void testRemove_index_removesFromBoth() {
        List<String> list = new ArrayList<String>();
        list.add("A");
        list.add("B");
        SetUniqueList unique = SetUniqueList.decorate(list);
        Object removed = unique.remove(0);
        assertEquals("A", removed);
        assertEquals(1, unique.size());
        assertFalse(unique.contains("A"));
    }

    @Test
    public void testRemoveAll_collection_removesFromBoth() {
        List<String> list = new ArrayList<String>();
        list.add("A");
        list.add("B");
        list.add("C");
        SetUniqueList unique = SetUniqueList.decorate(list);
        Collection<String> coll = Arrays.asList("A", "C");
        boolean changed = unique.removeAll(coll);
        assertTrue(changed);
        assertEquals(1, unique.size());
        assertTrue(unique.contains("B"));
        assertFalse(unique.contains("A"));
        assertFalse(unique.contains("C"));
    }

    @Test
    public void testClear_listAndSetEmpty() {
        List<String> list = new ArrayList<String>();
        list.add("A");
        list.add("B");
        SetUniqueList unique = SetUniqueList.decorate(list);
        unique.clear();
        assertTrue(unique.isEmpty());
        assertEquals(0, unique.size());
        assertFalse(unique.contains("A"));
    }

    @Test
    public void testContains_usesSet() {
        List<String> list = new ArrayList<String>();
        list.add("A");
        SetUniqueList unique = SetUniqueList.decorate(list);
        assertTrue(unique.contains("A"));
        assertFalse(unique.contains("B"));
    }

    @Test
    public void testListIterator_add_duplicateIgnored() {
        List<String> list = new ArrayList<String>();
        list.add("A");
        SetUniqueList unique = SetUniqueList.decorate(list);
        ListIterator<String> it = unique.listIterator();
        it.next();
        it.add("A");
        assertEquals(1, unique.size());
        assertEquals("A", unique.get(0));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testListIterator_set_throwsUnsupportedOperationException() {
        List<String> list = new ArrayList<String>();
        list.add("A");
        SetUniqueList unique = SetUniqueList.decorate(list);
        ListIterator<String> it = unique.listIterator();
        it.next();
        it.set("B");
    }

    @Test
    public void testIterator_remove_updatesSet() {
        List<String> list = new ArrayList<String>();
        list.add("A");
        list.add("B");
        SetUniqueList unique = SetUniqueList.decorate(list);
        Iterator<String> it = unique.iterator();
        it.next();
        it.remove();
        assertEquals(1, unique.size());
        assertFalse(unique.contains("A"));
        assertTrue(unique.contains("B"));
    }

    @Test
    public void testSubList_returnsSetUniqueList() {
        List<String> list = new ArrayList<String>();
        list.add("A");
        list.add("B");
        list.add("C");
        SetUniqueList unique = SetUniqueList.decorate(list);
        List sub = unique.subList(1, 3);
        assertTrue(sub instanceof SetUniqueList);
        assertEquals(2, sub.size());
        assertEquals("B", sub.get(0));
        assertEquals("C", sub.get(1));
    }

    @Test
    public void testAsSet_returnsUnmodifiableSet() {
        List<String> list = new ArrayList<String>();
        list.add("A");
        SetUniqueList unique = SetUniqueList.decorate(list);
        Set<String> setView = unique.asSet();
        assertNotNull(setView);
        assertTrue(setView.contains("A"));
        try {
            setView.add("B");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
        }
    }
}