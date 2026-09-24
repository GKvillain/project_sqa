package org.apache.commons.collections.list;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SetUniqueListTest {

    private List list;
    private SetUniqueList uniqueList;

    @Before
    public void setUp() {
        list = new ArrayList();
        uniqueList = SetUniqueList.decorate(list);
    }

    // Tests factory method with null list throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testDecorate_nullList_throwsException() {
        SetUniqueList.decorate(null);
    }

    // Tests factory method with non-empty list containing duplicates
    @Test
    public void testDecorate_listWithDuplicates_removesDuplicatesRetainsOrder() {
        List initial = new ArrayList(Arrays.asList("A", "B", "A", "C", "B"));
        SetUniqueList decorated = SetUniqueList.decorate(initial);

        assertEquals(3, decorated.size());
        assertEquals("A", decorated.get(0));
        assertEquals("B", decorated.get(1));
        assertEquals("C", decorated.get(2));
    }

    // Tests constructor with null set throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullSet_throwsException() {
        new SetUniqueList(new ArrayList(), null);
    }

    // Tests add method with unique and duplicate elements
    @Test
    public void testAdd_uniqueAndDuplicateElements_maintainsUniqueness() {
        assertTrue(uniqueList.add("A"));
        assertTrue(uniqueList.add("B"));
        assertFalse(uniqueList.add("A"));

        assertEquals(2, uniqueList.size());
        assertTrue(uniqueList.contains("A"));
        assertTrue(uniqueList.contains("B"));
    }

    // Tests indexed add with unique and duplicate elements
    @Test
    public void testAdd_withIndex_insertsOnlyUniqueElements() {
        uniqueList.add(0, "A");
        uniqueList.add(1, "C");
        uniqueList.add(1, "B");
        uniqueList.add(0, "A"); // Duplicate, ignored

        assertEquals(3, uniqueList.size());
        assertEquals("A", uniqueList.get(0));
        assertEquals("B", uniqueList.get(1));
        assertEquals("C", uniqueList.get(2));
    }

    // Tests addAll methods maintaining uniqueness and order
    @Test
    public void testAddAll_withAndWithoutIndex_insertsUniqueElements() {
        uniqueList.addAll(Arrays.asList("A", "B", "A"));
        assertEquals(2, uniqueList.size());

        uniqueList.addAll(1, Arrays.asList("C", "B", "D"));
        assertEquals(4, uniqueList.size());
        assertEquals("A", uniqueList.get(0));
        assertEquals("C", uniqueList.get(1));
        assertEquals("D", uniqueList.get(2));
        assertEquals("B", uniqueList.get(3));
    }

    // Tests set method when setting a brand new element (pos == -1)
    @Test
    public void testSet_newElement_updatesListAndSet() {
        uniqueList.add("A");
        uniqueList.add("B");
        uniqueList.add("C");

        Object old = uniqueList.set(1, "D");
        assertEquals("B", old);
        assertEquals(3, uniqueList.size());
        assertEquals("D", uniqueList.get(1));
        assertTrue(uniqueList.contains("D"));
        assertFalse(uniqueList.contains("B"));
    }

    // Tests set method when setting element to its current index (pos == index)
    @Test
    public void testSet_sameElementAtSameIndex_returnsOldWithoutChange() {
        uniqueList.add("A");
        uniqueList.add("B");

        Object old = uniqueList.set(0, "A");
        assertEquals("A", old);
        assertEquals(2, uniqueList.size());
        assertEquals("A", uniqueList.get(0));
        assertTrue(uniqueList.contains("A"));
    }

    // Tests set method when setting element that exists elsewhere (pos != -1 && pos != index)
    @Test
    public void testSet_existingElementAtDifferentIndex_movesElement() {
        uniqueList.add("A");
        uniqueList.add("B");
        uniqueList.add("C");

        // Move "C" from index 2 to index 0 (pos > index)
        Object old0 = uniqueList.set(0, "C");
        assertEquals("A", old0);
        assertEquals(2, uniqueList.size());
        assertEquals("C", uniqueList.get(0));
        assertEquals("B", uniqueList.get(1));
        assertFalse(uniqueList.contains("A"));

        // Move "C" from index 0 to index 1 (pos < index)
        Object old1 = uniqueList.set(1, "C");
        assertEquals("B", old1);
        assertEquals(1, uniqueList.size());
        assertEquals("C", uniqueList.get(0));
        assertFalse(uniqueList.contains("B"));
    }

    // Tests remove by object and by index
    @Test
    public void testRemove_byObjectAndIndex_removesFromListAndSet() {
        uniqueList.addAll(Arrays.asList("A", "B", "C"));

        assertTrue(uniqueList.remove("B"));
        assertFalse(uniqueList.contains("B"));
        assertEquals(2, uniqueList.size());

        Object removed = uniqueList.remove(0);
        assertEquals("A", removed);
        assertFalse(uniqueList.contains("A"));
        assertEquals(1, uniqueList.size());
    }

    // Tests removeAll, retainAll, and clear
    @Test
    public void testRemoveAllAndRetainAllAndClear_modifiesListAndSet() {
        uniqueList.addAll(Arrays.asList("A", "B", "C", "D"));

        uniqueList.removeAll(Arrays.asList("A", "X"));
        assertEquals(3, uniqueList.size());
        assertFalse(uniqueList.contains("A"));

        uniqueList.retainAll(Arrays.asList("B", "C", "Y"));
        assertEquals(2, uniqueList.size());
        assertTrue(uniqueList.contains("B"));
        assertTrue(uniqueList.contains("C"));
        assertFalse(uniqueList.contains("D"));

        uniqueList.clear();
        assertEquals(0, uniqueList.size());
        assertFalse(uniqueList.contains("B"));
    }

    // Tests contains and containsAll
    @Test
    public void testContains_andContainsAll_returnsCorrectStatus() {
        uniqueList.addAll(Arrays.asList("A", "B", "C"));

        assertTrue(uniqueList.contains("A"));
        assertFalse(uniqueList.contains("Z"));
        assertTrue(uniqueList.containsAll(Arrays.asList("A", "C")));
        assertFalse(uniqueList.containsAll(Arrays.asList("A", "Z")));
    }

    // Tests asSet view
    @Test
    public void testAsSet_returnsUnmodifiableSetView() {
        uniqueList.addAll(Arrays.asList("A", "B"));
        Set setView = uniqueList.asSet();

        assertEquals(2, setView.size());
        assertTrue(setView.contains("A"));
        assertTrue(setView.contains("B"));
    }

    // Tests iterator and its remove operation
    @Test
    public void testIterator_traversalAndRemoval_updatesUnderlyingSet() {
        uniqueList.addAll(Arrays.asList("A", "B", "C"));
        Iterator it = uniqueList.iterator();

        assertTrue(it.hasNext());
        assertEquals("A", it.next());
        it.remove();

        assertFalse(uniqueList.contains("A"));
        assertEquals(2, uniqueList.size());
    }

    // Tests listIterator traversal, remove, and add operations
    @Test
    public void testListIterator_traversalAddAndRemove_maintainsUniqueness() {
        uniqueList.addAll(Arrays.asList("A", "B"));
        ListIterator lit = uniqueList.listIterator();

        assertTrue(lit.hasNext());
        assertEquals("A", lit.next());
        assertEquals("A", lit.previous());
        assertEquals("A", lit.next());

        lit.add("C");
        lit.add("B"); // Duplicate, should not be added

        assertEquals(3, uniqueList.size());
        assertEquals("A", uniqueList.get(0));
        assertEquals("C", uniqueList.get(1));
        assertEquals("B", uniqueList.get(2));

        assertEquals("B", lit.next());
        lit.remove();
        assertFalse(uniqueList.contains("B"));
    }

    // Tests listIterator set operation throwing UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testListIterator_set_throwsUnsupportedOperationException() {
        uniqueList.add("A");
        ListIterator lit = uniqueList.listIterator();
        lit.next();
        lit.set("B");
    }

    // Tests subList creation and behavior
    @Test
    public void testSubList_returnsDecoratedSubList() {
        uniqueList.addAll(Arrays.asList("A", "B", "C", "D"));
        List sub = uniqueList.subList(1, 3);

        assertEquals(2, sub.size());
        assertEquals("B", sub.get(0));
        assertEquals("C", sub.get(1));
        assertTrue(sub instanceof SetUniqueList);
    }
}