package org.apache.commons.collections.list;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

/**
 * Unit tests for {@link SetUniqueList}.
 */
public class SetUniqueListTest {

    private List list;
    private SetUniqueList uniqueList;

    @Before
    public void setUp() {
        list = new ArrayList();
        uniqueList = SetUniqueList.decorate(list);
    }

    // Tests exception when decorate is given null list
    @Test(expected = IllegalArgumentException.class)
    public void testDecorate_nullList_throwsException() {
        SetUniqueList.decorate(null);
    }

    // Tests decorate with an empty list
    @Test
    public void testDecorate_emptyList_returnsEmptySetUniqueList() {
        List emptyList = new ArrayList();
        SetUniqueList decorated = SetUniqueList.decorate(emptyList);
        assertTrue(decorated.isEmpty());
        assertEquals(0, decorated.size());
    }

    // Tests decorate with a list containing duplicates, keeping first occurrence
    @Test
    public void testDecorate_listWithDuplicates_removesDuplicates() {
        List initial = new ArrayList(Arrays.asList(new Object[]{"A", "B", "A", "C", "B"}));
        SetUniqueList decorated = SetUniqueList.decorate(initial);
        assertEquals(3, decorated.size());
        assertEquals("A", decorated.get(0));
        assertEquals("B", decorated.get(1));
        assertEquals("C", decorated.get(2));
    }

    // Tests protected constructor with null set throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullSet_throwsException() {
        new SetUniqueList(new ArrayList(), null);
    }

    // Tests adding a unique element
    @Test
    public void testAdd_uniqueElement_returnsTrueAndIncreasesSize() {
        assertTrue(uniqueList.add("A"));
        assertEquals(1, uniqueList.size());
        assertTrue(uniqueList.contains("A"));
    }

    // Tests adding a duplicate element returns false
    @Test
    public void testAdd_duplicateElement_returnsFalseAndDoesNotAdd() {
        uniqueList.add("A");
        assertFalse(uniqueList.add("A"));
        assertEquals(1, uniqueList.size());
    }

    // Tests inserting unique element at specific index
    @Test
    public void testAddAtIndex_uniqueElement_insertsAtIndex() {
        uniqueList.add("A");
        uniqueList.add("C");
        uniqueList.add(1, "B");
        assertEquals(3, uniqueList.size());
        assertEquals("B", uniqueList.get(1));
    }

    // Tests inserting duplicate element at specific index does not insert
    @Test
    public void testAddAtIndex_duplicateElement_doesNotInsert() {
        uniqueList.add("A");
        uniqueList.add("B");
        uniqueList.add(0, "B");
        assertEquals(2, uniqueList.size());
        assertEquals("A", uniqueList.get(0));
        assertEquals("B", uniqueList.get(1));
    }

    // Tests addAll with collection containing both unique and duplicate elements
    @Test
    public void testAddAll_collectionWithDuplicates_addsOnlyUnique() {
        uniqueList.add("A");
        boolean changed = uniqueList.addAll(Arrays.asList(new Object[]{"B", "A", "C"}));
        assertTrue(changed);
        assertEquals(3, uniqueList.size());
        assertEquals("B", uniqueList.get(1));
        assertEquals("C", uniqueList.get(2));
    }

    // Tests addAll with no new elements returns false
    @Test
    public void testAddAll_noNewElements_returnsFalse() {
        uniqueList.add("A");
        boolean changed = uniqueList.addAll(Arrays.asList(new Object[]{"A"}));
        assertFalse(changed);
        assertEquals(1, uniqueList.size());
    }

    // Tests setting an element that was not in the list (pos == -1)
    @Test
    public void testSet_newElement_replacesOldAndUpdatesSet() {
        uniqueList.add("A");
        uniqueList.add("B");
        Object old = uniqueList.set(0, "C");
        assertEquals("A", old);
        assertEquals(2, uniqueList.size());
        assertEquals("C", uniqueList.get(0));
        assertTrue(uniqueList.contains("C"));
        assertFalse(uniqueList.contains("A"));
    }

    // Tests setting the same element at the same index (pos == index)
    @Test
    public void testSet_sameElementAtSameIndex_returnsOldValue() {
        uniqueList.add("A");
        uniqueList.add("B");
        Object old = uniqueList.set(0, "A");
        assertEquals("A", old);
        assertEquals(2, uniqueList.size());
        assertEquals("A", uniqueList.get(0));
        assertTrue(uniqueList.contains("A"));
    }

    // Tests setting an element already present at a different index (pos != -1 && pos != index)
    @Test
    public void testSet_existingElementAtDifferentIndex_removesDuplicate() {
        uniqueList.add("A");
        uniqueList.add("B");
        uniqueList.add("C");
        Object old = uniqueList.set(0, "C");
        assertEquals("A", old);
        assertEquals(2, uniqueList.size());
        assertEquals("C", uniqueList.get(0));
        assertEquals("B", uniqueList.get(1));
        assertFalse(uniqueList.contains("A"));
        assertTrue(uniqueList.contains("B"));
        assertTrue(uniqueList.contains("C"));
    }

    // Tests remove by object and remove by index
    @Test
    public void testRemove_objectAndIndex_removesFromListAndSet() {
        uniqueList.add("A");
        uniqueList.add("B");
        uniqueList.add("C");

        boolean removedObj = uniqueList.remove("B");
        assertTrue(removedObj);
        assertFalse(uniqueList.contains("B"));
        assertEquals(2, uniqueList.size());

        Object removedIndex = uniqueList.remove(0);
        assertEquals("A", removedIndex);
        assertFalse(uniqueList.contains("A"));
        assertEquals(1, uniqueList.size());
        assertEquals("C", uniqueList.get(0));
    }

    // Tests removeAll and retainAll
    @Test
    public void testRemoveAllAndRetainAll_modifiesListAndSet() {
        uniqueList.add("A");
        uniqueList.add("B");
        uniqueList.add("C");

        uniqueList.removeAll(Arrays.asList(new Object[]{"A"}));
        assertEquals(2, uniqueList.size());
        assertFalse(uniqueList.contains("A"));

        uniqueList.retainAll(Arrays.asList(new Object[]{"B"}));
        assertEquals(1, uniqueList.size());
        assertTrue(uniqueList.contains("B"));
        assertFalse(uniqueList.contains("C"));
    }

    // Tests clear method
    @Test
    public void testClear_emptiesListAndSet() {
        uniqueList.add("A");
        uniqueList.add("B");
        uniqueList.clear();
        assertEquals(0, uniqueList.size());
        assertFalse(uniqueList.contains("A"));
        assertFalse(uniqueList.contains("B"));
    }

    // Tests contains and containsAll methods
    @Test
    public void testContainsAndContainsAll_membershipChecks() {
        uniqueList.add("A");
        uniqueList.add("B");
        assertTrue(uniqueList.contains("A"));
        assertFalse(uniqueList.contains("C"));
        assertTrue(uniqueList.containsAll(Arrays.asList(new Object[]{"A", "B"})));
        assertFalse(uniqueList.containsAll(Arrays.asList(new Object[]{"A", "C"})));
    }

    // Tests asSet returns unmodifiable set view
    @Test(expected = UnsupportedOperationException.class)
    public void testAsSet_returnsUnmodifiableSet_throwsOnModify() {
        uniqueList.add("A");
        Set setView = uniqueList.asSet();
        assertEquals(1, setView.size());
        assertTrue(setView.contains("A"));
        setView.add("B");
    }

    // Tests iterator traversal and remove
    @Test
    public void testIterator_remove_updatesListAndSet() {
        uniqueList.add("A");
        uniqueList.add("B");
        Iterator it = uniqueList.iterator();
        assertTrue(it.hasNext());
        assertEquals("A", it.next());
        it.remove();
        assertEquals(1, uniqueList.size());
        assertFalse(uniqueList.contains("A"));
        assertTrue(uniqueList.contains("B"));
    }

    // Tests listIterator operations: previous, add, and set exception
    @Test
    public void testListIterator_addAndPrevious_updatesSet() {
        uniqueList.add("A");
        ListIterator lit = uniqueList.listIterator();
        assertEquals("A", lit.next());
        assertEquals("A", lit.previous());
        lit.add("B");
        assertTrue(uniqueList.contains("B"));
        lit.add("B"); // duplicate via ListIterator
        assertEquals(2, uniqueList.size());
    }

    // Tests ListIterator set throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testListIterator_set_throwsUnsupportedOperationException() {
        uniqueList.add("A");
        ListIterator lit = uniqueList.listIterator();
        lit.next();
        lit.set("B");
    }

    // Tests subList creation and operations
    @Test
    public void testSubList_viewAndModifications() {
        uniqueList.add("A");
        uniqueList.add("B");
        uniqueList.add("C");
        List sub = uniqueList.subList(1, 3);
        assertEquals(2, sub.size());
        assertEquals("B", sub.get(0));
        assertEquals("C", sub.get(1));
    }
}