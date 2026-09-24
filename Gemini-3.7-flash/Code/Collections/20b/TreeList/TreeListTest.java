package org.apache.commons.collections.list;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TreeListTest {

    private TreeList<String> treeList;

    @Before
    public void setUp() {
        treeList = new TreeList<String>();
    }

    // Tests empty list initial state and size
    @Test
    public void testConstructor_emptyList_returnsSizeZero() {
        assertEquals(0, treeList.size());
        assertTrue(treeList.isEmpty());
        assertEquals(-1, treeList.indexOf("item"));
        assertFalse(treeList.contains("item"));
        assertArrayEquals(new Object[0], treeList.toArray());
    }

    // Tests collection constructor copying elements
    @Test
    public void testConstructor_withCollection_copiesAllElements() {
        List<String> source = new ArrayList<String>();
        source.add("A");
        source.add("B");
        source.add("C");
        TreeList<String> list = new TreeList<String>(source);

        assertEquals(3, list.size());
        assertEquals("A", list.get(0));
        assertEquals("B", list.get(1));
        assertEquals("C", list.get(2));
    }

    // Tests null input in collection constructor
    @Test(expected = NullPointerException.class)
    public void testConstructor_nullCollection_throwsNullPointerException() {
        new TreeList<String>(null);
    }

    // Tests adding elements at various positions and checking order
    @Test
    public void testAdd_insertAtVariousIndices_maintainsCorrectOrder() {
        treeList.add(0, "A");
        treeList.add(1, "C");
        treeList.add(1, "B"); // Insert in middle
        treeList.add(0, "First"); // Insert at beginning
        treeList.add(4, "Last"); // Insert at end

        assertEquals(5, treeList.size());
        assertEquals("First", treeList.get(0));
        assertEquals("A", treeList.get(1));
        assertEquals("B", treeList.get(2));
        assertEquals("C", treeList.get(3));
        assertEquals("Last", treeList.get(4));
    }

    // Tests AVL tree balancing via multiple sequential insertions
    @Test
    public void testAdd_multipleSequentialInserts_balancesTreeCorrectly() {
        for (int i = 0; i < 50; i++) {
            treeList.add(i, "Item" + i);
        }
        assertEquals(50, treeList.size());
        for (int i = 0; i < 50; i++) {
            assertEquals("Item" + i, treeList.get(i));
        }

        // Add in reverse to trigger opposite rotations
        TreeList<Integer> reverseList = new TreeList<Integer>();
        for (int i = 0; i < 50; i++) {
            reverseList.add(0, i);
        }
        assertEquals(50, reverseList.size());
        for (int i = 0; i < 50; i++) {
            assertEquals(Integer.valueOf(49 - i), reverseList.get(i));
        }
    }

    // Tests out of bounds index on add
    @Test(expected = IndexOutOfBoundsException.class)
    public void testAdd_negativeIndex_throwsIndexOutOfBoundsException() {
        treeList.add(-1, "Invalid");
    }

    // Tests out of bounds index above size on add
    @Test(expected = IndexOutOfBoundsException.class)
    public void testAdd_indexGreaterThanSize_throwsIndexOutOfBoundsException() {
        treeList.add(1, "Invalid");
    }

    // Tests get method with invalid indices
    @Test(expected = IndexOutOfBoundsException.class)
    public void testGet_emptyList_throwsIndexOutOfBoundsException() {
        treeList.get(0);
    }

    // Tests get method with index out of upper range
    @Test(expected = IndexOutOfBoundsException.class)
    public void testGet_indexEqualToSize_throwsIndexOutOfBoundsException() {
        treeList.add("A");
        treeList.get(1);
    }

    // Tests set method replacing elements and returning previous value
    @Test
    public void testSet_validIndex_updatesValueAndReturnsOldValue() {
        treeList.add("A");
        treeList.add("B");
        treeList.add("C");

        String oldVal = treeList.set(1, "Updated");
        assertEquals("B", oldVal);
        assertEquals("Updated", treeList.get(1));
        assertEquals(3, treeList.size());
    }

    // Tests set method with invalid index
    @Test(expected = IndexOutOfBoundsException.class)
    public void testSet_invalidIndex_throwsIndexOutOfBoundsException() {
        treeList.set(0, "A");
    }

    // Tests indexOf and contains with normal values and null
    @Test
    public void testIndexOfAndContains_variousElements_returnsCorrectIndices() {
        treeList.add("A");
        treeList.add(null);
        treeList.add("B");
        treeList.add("A");

        assertEquals(0, treeList.indexOf("A"));
        assertEquals(1, treeList.indexOf(null));
        assertEquals(2, treeList.indexOf("B"));
        assertEquals(-1, treeList.indexOf("NonExistent"));

        assertTrue(treeList.contains("A"));
        assertTrue(treeList.contains(null));
        assertFalse(treeList.contains("NonExistent"));
    }

    // Tests toArray method on non-empty tree
    @Test
    public void testToArray_populatedList_returnsCorrectArray() {
        treeList.add("A");
        treeList.add("B");
        treeList.add("C");
        Object[] arr = treeList.toArray();
        assertArrayEquals(new Object[]{"A", "B", "C"}, arr);
    }

    // Tests remove by index from different positions and AVL rebalancing
    @Test
    public void testRemove_variousPositions_maintainsIntegrity() {
        for (int i = 0; i < 10; i++) {
            treeList.add("Val" + i);
        }

        // Remove from beginning
        String removedFirst = treeList.remove(0);
        assertEquals("Val0", removedFirst);
        assertEquals("Val1", treeList.get(0));
        assertEquals(9, treeList.size());

        // Remove from end
        String removedLast = treeList.remove(treeList.size() - 1);
        assertEquals("Val9", removedLast);
        assertEquals(8, treeList.size());

        // Remove from middle
        String removedMiddle = treeList.remove(4);
        assertEquals("Val5", removedMiddle);
        assertEquals(7, treeList.size());

        // Drain the list
        while (!treeList.isEmpty()) {
            treeList.remove(0);
        }
        assertEquals(0, treeList.size());
        assertNull(treeList.indexOf("Val1") == -1 ? null : "");
    }

    // Tests remove method with invalid index
    @Test(expected = IndexOutOfBoundsException.class)
    public void testRemove_invalidIndex_throwsIndexOutOfBoundsException() {
        treeList.add("A");
        treeList.remove(1);
    }

    // Tests clear method
    @Test
    public void testClear_populatedList_resetsToEmpty() {
        treeList.add("A");
        treeList.add("B");
        treeList.clear();

        assertEquals(0, treeList.size());
        assertTrue(treeList.isEmpty());
        assertEquals(-1, treeList.indexOf("A"));
    }

    // Tests iterator traversal using standard iterator
    @Test
    public void testIterator_forwardTraversal_traversesAllElements() {
        treeList.add("A");
        treeList.add("B");
        treeList.add("C");

        Iterator<String> it = treeList.iterator();
        assertTrue(it.hasNext());
        assertEquals("A", it.next());
        assertTrue(it.hasNext());
        assertEquals("B", it.next());
        assertTrue(it.hasNext());
        assertEquals("C", it.next());
        assertFalse(it.hasNext());
    }

    // Tests ListIterator bidirectional traversal and index methods
    @Test
    public void testListIterator_bidirectionalTraversal_navigatesCorrectly() {
        treeList.add("A");
        treeList.add("B");
        treeList.add("C");

        ListIterator<String> it = treeList.listIterator(1);
        assertEquals(1, it.nextIndex());
        assertEquals(0, it.previousIndex());
        assertTrue(it.hasNext());
        assertTrue(it.hasPrevious());

        assertEquals("B", it.next());
        assertEquals("C", it.next());
        assertFalse(it.hasNext());

        assertEquals("C", it.previous());
        assertEquals("B", it.previous());
        assertEquals("A", it.previous());
        assertFalse(it.hasPrevious());
    }

    // Tests ListIterator next() when no elements remain
    @Test(expected = NoSuchElementException.class)
    public void testListIterator_nextBeyondEnd_throwsNoSuchElementException() {
        ListIterator<String> it = treeList.listIterator();
        it.next();
    }

    // Tests ListIterator previous() when at beginning
    @Test(expected = NoSuchElementException.class)
    public void testListIterator_previousAtStart_throwsNoSuchElementException() {
        ListIterator<String> it = treeList.listIterator();
        it.previous();
    }

    // Tests ListIterator add, set, and remove operations
    @Test
    public void testListIterator_addSetRemoveOperations_modifiesListCorrectly() {
        ListIterator<String> it = treeList.listIterator();
        it.add("A");
        it.add("B");
        it.add("D");

        assertEquals(3, treeList.size());
        assertEquals("D", treeList.get(2));

        // Move back to D
        assertEquals("D", it.previous());
        it.set("C");
        assertEquals("C", treeList.get(2));

        // Remove C
        it.remove();
        assertEquals(2, treeList.size());
        assertEquals("B", treeList.get(1));
    }

    // Tests ListIterator remove after previous traversal
    @Test
    public void testListIterator_removeAfterPrevious_removesCorrectElement() {
        treeList.add("A");
        treeList.add("B");
        treeList.add("C");

        ListIterator<String> it = treeList.listIterator(3);
        assertEquals("C", it.previous());
        it.remove();

        assertEquals(2, treeList.size());
        assertEquals("A", treeList.get(0));
        assertEquals("B", treeList.get(1));
        assertTrue(it.hasPrevious());
        assertEquals("B", it.previous());
    }

    // Tests ListIterator set without prior next/previous call
    @Test(expected = IllegalStateException.class)
    public void testListIterator_setWithoutNextOrPrevious_throwsIllegalStateException() {
        ListIterator<String> it = treeList.listIterator();
        it.set("Invalid");
    }

    // Tests ListIterator remove without prior next/previous call
    @Test(expected = IllegalStateException.class)
    public void testListIterator_removeWithoutNextOrPrevious_throwsIllegalStateException() {
        ListIterator<String> it = treeList.listIterator();
        it.remove();
    }

    // Tests fail-fast concurrent modification detection in ListIterator
    @Test(expected = ConcurrentModificationException.class)
    public void testListIterator_concurrentModification_throwsConcurrentModificationException() {
        ListIterator<String> it = treeList.listIterator();
        treeList.add("A");
        it.next();
    }
}