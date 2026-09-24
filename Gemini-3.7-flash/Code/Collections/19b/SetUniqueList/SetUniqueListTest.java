package org.apache.commons.collections.list;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link SetUniqueList}.
 */
public class SetUniqueListTest {

    private List<String> backingList;
    private SetUniqueList<String> uniqueList;

    @Before
    public void setUp() {
        backingList = new ArrayList<String>();
        uniqueList = new SetUniqueList<String>(backingList, new HashSet<String>());
    }

    // Tests factory method with null list
    @Test(expected = IllegalArgumentException.class)
    public void testSetUniqueList_nullList_throwsException() {
        SetUniqueList.setUniqueList(null);
    }

    // Tests factory method with empty list
    @Test
    public void testSetUniqueList_emptyList_createsEmptySetUniqueList() {
        List<String> list = new ArrayList<String>();
        SetUniqueList<String> result = SetUniqueList.setUniqueList(list);
        assertTrue(result.isEmpty());
        assertEquals(0, result.size());
    }

    // Tests factory method with duplicates in list
    @Test
    public void testSetUniqueList_listWithDuplicates_removesDuplicatesRetainingOrder() {
        List<String> list = new ArrayList<String>(Arrays.asList("A", "B", "A", "C", "B"));
        SetUniqueList<String> result = SetUniqueList.setUniqueList(list);
        assertEquals(3, result.size());
        assertEquals("A", result.get(0));
        assertEquals("B", result.get(1));
        assertEquals("C", result.get(2));
    }

    // Tests constructor with null set
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullSet_throwsException() {
        new SetUniqueList<String>(new ArrayList<String>(), null);
    }

    // Tests add method with unique element
    @Test
    public void testAdd_uniqueElement_returnsTrueAndAppends() {
        assertTrue(uniqueList.add("A"));
        assertTrue(uniqueList.add("B"));
        assertEquals(2, uniqueList.size());
        assertTrue(uniqueList.contains("A"));
        assertTrue(uniqueList.contains("B"));
    }

    // Tests add method with duplicate element
    @Test
    public void testAdd_duplicateElement_returnsFalseAndDoesNotAdd() {
        assertTrue(uniqueList.add("A"));
        assertFalse(uniqueList.add("A"));
        assertEquals(1, uniqueList.size());
    }

    // Tests add at index with unique and duplicate elements
    @Test
    public void testAddAtIndex_uniqueAndDuplicateElements_insertsOnlyUnique() {
        uniqueList.add(0, "A");
        uniqueList.add(1, "B");
        uniqueList.add(0, "C");
        assertEquals(3, uniqueList.size());
        assertEquals("C", uniqueList.get(0));
        assertEquals("A", uniqueList.get(1));

        // Attempt to insert duplicate at index 0
        uniqueList.add(0, "A");
        assertEquals(3, uniqueList.size());
        assertEquals("C", uniqueList.get(0));
    }

    // Tests addAll at specific index with collection containing duplicates
    @Test
    public void testAddAllAtIndex_collectionWithDuplicates_addsOnlyUniqueElements() {
        uniqueList.add("A");
        uniqueList.add("B");
        List<String> toAdd = Arrays.asList("B", "C", "A", "D");
        boolean changed = uniqueList.addAll(1, toAdd);
        assertTrue(changed);
        assertEquals(4, uniqueList.size());
        assertEquals("A", uniqueList.get(0));
        assertEquals("C", uniqueList.get(1));
        assertEquals("D", uniqueList.get(2));
        assertEquals("B", uniqueList.get(3));
    }

    // Tests set method replacing element with itself (regression for defect where set state is corrupted)
    @Test
    public void testSet_sameElementAtSameIndex_maintainsElementInSet() {
        uniqueList.add("A");
        uniqueList.add("B");
        String old = uniqueList.set(0, "A");
        assertEquals("A", old);
        assertEquals(2, uniqueList.size());
        assertTrue(uniqueList.contains("A"));
        assertFalse(uniqueList.add("A"));
    }

    // Tests set method when setting element that already exists at a different index
    @Test
    public void testSet_elementAlreadyPresentAtDifferentIndex_movesElementAndRemovesDuplicate() {
        uniqueList.add("A");
        uniqueList.add("B");
        uniqueList.add("C");
        // set index 0 to "C" (which is at index 2)
        String old = uniqueList.set(0, "C");
        assertEquals("A", old);
        assertEquals(2, uniqueList.size());
        assertEquals("C", uniqueList.get(0));
        assertEquals("B", uniqueList.get(1));
        assertFalse(uniqueList.contains("A"));
        assertTrue(uniqueList.contains("C"));
    }

    // Tests remove by object and by index
    @Test
    public void testRemove_byObjectAndIndex_removesSuccessfully() {
        uniqueList.add("A");
        uniqueList.add("B");
        uniqueList.add("C");

        assertTrue(uniqueList.remove("B"));
        assertFalse(uniqueList.contains("B"));
        assertFalse(uniqueList.remove("NonExistent"));

        String removed = uniqueList.remove(0);
        assertEquals("A", removed);
        assertFalse(uniqueList.contains("A"));
        assertEquals(1, uniqueList.size());
    }

    // Tests removeAll
    @Test
    public void testRemoveAll_collection_removesMatchingElements() {
        uniqueList.add("A");
        uniqueList.add("B");
        uniqueList.add("C");

        assertTrue(uniqueList.removeAll(Arrays.asList("A", "C", "D")));
        assertEquals(1, uniqueList.size());
        assertTrue(uniqueList.contains("B"));
        assertFalse(uniqueList.contains("A"));
    }

    // Tests retainAll with complete match, partial match, and no match
    @Test
    public void testRetainAll_variousCollections_retainsCorrectElements() {
        uniqueList.add("A");
        uniqueList.add("B");
        uniqueList.add("C");

        // Retain all existing elements -> returns false (no change)
        assertFalse(uniqueList.retainAll(Arrays.asList("A", "B", "C", "D")));

        // Retain partial elements
        assertTrue(uniqueList.retainAll(Arrays.asList("A", "C")));
        assertEquals(2, uniqueList.size());
        assertEquals("A", uniqueList.get(0));
        assertEquals("C", uniqueList.get(1));

        // Retain none
        assertTrue(uniqueList.retainAll(Arrays.asList("X", "Y")));
        assertTrue(uniqueList.isEmpty());
    }

    // Tests clear and containsAll
    @Test
    public void testClearAndContainsAll_validInputs_operatesCorrectly() {
        uniqueList.add("A");
        uniqueList.add("B");

        assertTrue(uniqueList.containsAll(Arrays.asList("A", "B")));
        assertFalse(uniqueList.containsAll(Arrays.asList("A", "C")));

        uniqueList.clear();
        assertEquals(0, uniqueList.size());
        assertFalse(uniqueList.contains("A"));
    }

    // Tests iterator and iterator remove
    @Test
    public void testIterator_iterateAndRemove_modifiesUnderlyingSet() {
        uniqueList.add("A");
        uniqueList.add("B");

        Iterator<String> it = uniqueList.iterator();
        assertTrue(it.hasNext());
        assertEquals("A", it.next());
        it.remove();

        assertEquals(1, uniqueList.size());
        assertFalse(uniqueList.contains("A"));
        assertTrue(uniqueList.contains("B"));
    }

    // Tests listIterator add, previous, and set
    @Test
    public void testListIterator_navigationAndModifications_behavesCorrectly() {
        uniqueList.add("A");
        uniqueList.add("B");

        ListIterator<String> lit = uniqueList.listIterator();
        assertEquals("A", lit.next());
        assertEquals("A", lit.previous());

        // Add unique element via listIterator
        lit.add("Z");
        assertTrue(uniqueList.contains("Z"));

        // Add duplicate element via listIterator (ignored)
        lit.add("B");
        assertEquals(3, uniqueList.size());
    }

    // Tests listIterator set throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testListIteratorSet_anyElement_throwsUnsupportedOperationException() {
        uniqueList.add("A");
        ListIterator<String> lit = uniqueList.listIterator();
        lit.next();
        lit.set("B");
    }

    // Tests subList creation and independent modification
    @Test
    public void testSubList_validRange_createsSubList() {
        uniqueList.add("A");
        uniqueList.add("B");
        uniqueList.add("C");
        uniqueList.add("D");

        List<String> sub = uniqueList.subList(1, 3);
        assertEquals(2, sub.size());
        assertEquals("B", sub.get(0));
        assertEquals("C", sub.get(1));
    }

    // Tests asSet returns unmodifiable view
    @Test(expected = UnsupportedOperationException.class)
    public void testAsSet_modifySet_throwsUnsupportedOperationException() {
        uniqueList.add("A");
        Set<String> setView = uniqueList.asSet();
        assertEquals(1, setView.size());
        assertTrue(setView.contains("A"));
        setView.add("B");
    }
}