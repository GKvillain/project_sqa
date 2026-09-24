package org.apache.commons.collections4.list;

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
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SetUniqueListTest {

    private List<String> decoratedList;
    private SetUniqueList<String> setUniqueList;

    @Before
    public void setUp() {
        decoratedList = new ArrayList<String>();
        decoratedList.add("A");
        decoratedList.add("B");
        decoratedList.add("C");
        setUniqueList = SetUniqueList.setUniqueList(decoratedList);
    }

    // Tests factory method with null list
    @Test(expected = IllegalArgumentException.class)
    public void testSetUniqueList_nullList_throwsException() {
        SetUniqueList.setUniqueList(null);
    }

    // Tests factory method with empty list and duplicates
    @Test
    public void testSetUniqueList_listWithDuplicates_removesDuplicates() {
        final List<String> list = new ArrayList<String>(Arrays.asList("A", "B", "A", "C", "B"));
        final SetUniqueList<String> uniqueList = SetUniqueList.setUniqueList(list);
        assertEquals(3, uniqueList.size());
        assertEquals("A", uniqueList.get(0));
        assertEquals("B", uniqueList.get(1));
        assertEquals("C", uniqueList.get(2));

        final SetUniqueList<String> emptyList = SetUniqueList.setUniqueList(new ArrayList<String>());
        assertTrue(emptyList.isEmpty());
    }

    // Tests constructor with null set
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullSet_throwsException() {
        new SetUniqueList<String>(new ArrayList<String>(), null);
    }

    // Tests asSet returns an unmodifiable view of the internal set
    @Test
    public void testAsSet_returnsSetView() {
        final Set<String> setView = setUniqueList.asSet();
        assertNotNull(setView);
        assertEquals(3, setView.size());
        assertTrue(setView.contains("A"));
        assertTrue(setView.contains("B"));
        assertTrue(setView.contains("C"));
    }

    // Tests add method for unique and duplicate elements
    @Test
    public void testAdd_uniqueAndDuplicateElements_maintainsUniqueness() {
        assertTrue(setUniqueList.add("D"));
        assertEquals(4, setUniqueList.size());
        assertEquals("D", setUniqueList.get(3));

        assertFalse(setUniqueList.add("A"));
        assertEquals(4, setUniqueList.size());
    }

    // Tests add at specific index for unique and duplicate elements
    @Test
    public void testAddIndexed_uniqueAndDuplicateElements_insertsCorrectly() {
        setUniqueList.add(1, "D");
        assertEquals(4, setUniqueList.size());
        assertEquals("D", setUniqueList.get(1));

        // Duplicate add at index should be ignored
        setUniqueList.add(0, "D");
        assertEquals(4, setUniqueList.size());
        assertEquals("A", setUniqueList.get(0));
    }

    // Tests addAll at the end and at specific index
    @Test
    public void testAddAll_withDuplicates_addsOnlyUniqueElements() {
        final Collection<String> toAdd = Arrays.asList("C", "D", "E", "D");
        assertTrue(setUniqueList.addAll(toAdd));
        assertEquals(5, setUniqueList.size());
        assertEquals("D", setUniqueList.get(3));
        assertEquals("E", setUniqueList.get(4));

        assertFalse(setUniqueList.addAll(Arrays.asList("A", "B", "C")));

        final Collection<String> toAddIndexed = Arrays.asList("F", "B", "G");
        assertTrue(setUniqueList.addAll(1, toAddIndexed));
        assertEquals(7, setUniqueList.size());
        assertEquals("F", setUniqueList.get(1));
        assertEquals("G", setUniqueList.get(2));
    }

    // Tests set method replacing element with a new unique element and with existing element
    @Test
    public void testSet_replacingElements_maintainsUniqueness() {
        // Replace with new element
        final String old1 = setUniqueList.set(1, "Z");
        assertEquals("B", old1);
        assertEquals(3, setUniqueList.size());
        assertEquals("Z", setUniqueList.get(1));
        assertFalse(setUniqueList.contains("B"));
        assertTrue(setUniqueList.contains("Z"));

        // Replace with same element at same index
        final String old2 = setUniqueList.set(1, "Z");
        assertEquals("Z", old2);
        assertEquals(3, setUniqueList.size());
        assertTrue(setUniqueList.contains("Z"));

        // Replace at index 0 with element already existing at index 2 ("C")
        final String old3 = setUniqueList.set(0, "C");
        assertEquals("A", old3);
        assertEquals(2, setUniqueList.size());
        assertEquals("C", setUniqueList.get(0));
        assertEquals("Z", setUniqueList.get(1));
    }

    // Tests remove by object and by index
    @Test
    public void testRemove_byObjectAndIndex_removesProperly() {
        assertTrue(setUniqueList.remove("B"));
        assertFalse(setUniqueList.contains("B"));
        assertEquals(2, setUniqueList.size());

        assertFalse(setUniqueList.remove("NonExistent"));

        final String removed = setUniqueList.remove(0);
        assertEquals("A", removed);
        assertFalse(setUniqueList.contains("A"));
        assertEquals(1, setUniqueList.size());
        assertEquals("C", setUniqueList.get(0));
    }

    // Tests removeAll
    @Test
    public void testRemoveAll_matchingElements_removesAllMatching() {
        assertTrue(setUniqueList.removeAll(Arrays.asList("A", "C", "Z")));
        assertEquals(1, setUniqueList.size());
        assertEquals("B", setUniqueList.get(0));

        assertFalse(setUniqueList.removeAll(Arrays.asList("X", "Y")));
    }

    // Tests retainAll with all, partial, and empty intersection
    @Test
    public void testRetainAll_variousCollections_updatesListCorrectly() {
        // When all elements match, returns false
        assertFalse(setUniqueList.retainAll(Arrays.asList("A", "B", "C", "D")));

        // Partial match
        assertTrue(setUniqueList.retainAll(Arrays.asList("A", "C", "Z")));
        assertEquals(2, setUniqueList.size());
        assertEquals("A", setUniqueList.get(0));
        assertEquals("C", setUniqueList.get(1));
        assertFalse(setUniqueList.contains("B"));

        // No match clears list
        assertTrue(setUniqueList.retainAll(Arrays.asList("X", "Y")));
        assertTrue(setUniqueList.isEmpty());
    }

    // Tests clear and containsAll
    @Test
    public void testClearAndContainsAll() {
        assertTrue(setUniqueList.containsAll(Arrays.asList("A", "B")));
        assertFalse(setUniqueList.containsAll(Arrays.asList("A", "Z")));

        setUniqueList.clear();
        assertEquals(0, setUniqueList.size());
        assertFalse(setUniqueList.contains("A"));
    }

    // Tests iterator traversal and remove
    @Test
    public void testIterator_traversalAndRemove_worksCorrectly() {
        final Iterator<String> it = setUniqueList.iterator();
        assertTrue(it.hasNext());
        assertEquals("A", it.next());
        it.remove();
        assertFalse(setUniqueList.contains("A"));
        assertEquals(2, setUniqueList.size());

        assertEquals("B", it.next());
        assertEquals("C", it.next());
        assertFalse(it.hasNext());
    }

    // Tests listIterator traversal, add, remove, and unsupported set
    @Test
    public void testListIterator_operations() {
        final ListIterator<String> listIt = setUniqueList.listIterator();
        assertTrue(listIt.hasNext());
        assertEquals("A", listIt.next());
        assertTrue(listIt.hasPrevious());
        assertEquals("A", listIt.previous());
        assertEquals("A", listIt.next());

        listIt.remove();
        assertFalse(setUniqueList.contains("A"));

        // Add unique item through iterator
        listIt.add("X");
        assertTrue(setUniqueList.contains("X"));

        // Add duplicate item through iterator (should be ignored)
        listIt.add("B");
        assertEquals(3, setUniqueList.size());

        final ListIterator<String> indexedIt = setUniqueList.listIterator(1);
        assertEquals("B", indexedIt.next());
    }

    // Tests listIterator set throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testListIterator_set_throwsUnsupportedOperationException() {
        final ListIterator<String> listIt = setUniqueList.listIterator();
        listIt.next();
        listIt.set("Z");
    }

    // Tests subList creation and createSetBasedOnList fallback
    @Test
    public void testSubList_andCreateSetBasedOnList() {
        final List<String> sub = setUniqueList.subList(1, 3);
        assertEquals(2, sub.size());
        assertEquals("B", sub.get(0));
        assertEquals("C", sub.get(1));

        // Test with custom set implementation to exercise createSetBasedOnList
        final SetUniqueList<String> customSetList = new SetUniqueList<String>(
                new ArrayList<String>(Arrays.asList("1", "2")),
                new TreeSet<String>(Arrays.asList("1", "2"))
        );
        final List<String> customSub = customSetList.subList(0, 1);
        assertEquals(1, customSub.size());
        assertEquals("1", customSub.get(0));
    }
}