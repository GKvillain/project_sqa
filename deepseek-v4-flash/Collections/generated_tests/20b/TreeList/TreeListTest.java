package org.apache.commons.collections.list;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * JUnit 4 test class for TreeList (Defects4J Bug 20b).
 */
public class TreeListTest {

    private TreeList<String> list;

    @Before
    public void setUp() {
        list = new TreeList<String>();
    }

    // Tests normal add and get operations
    @Test
    public void testAddAndGet_normal_returnsCorrectElement() {
        list.add("A");
        list.add("B");
        list.add("C");
        assertEquals("A", list.get(0));
        assertEquals("B", list.get(1));
        assertEquals("C", list.get(2));
        assertEquals(3, list.size());
    }

    // Tests adding at index 0 (beginning) – updates relative positions
    @Test
    public void testAddAtIndex_beginning_insertsCorrectly() {
        list.add("B");
        list.add("C");
        list.add(0, "A");
        assertEquals("A", list.get(0));
        assertEquals("B", list.get(1));
        assertEquals("C", list.get(2));
        assertEquals(3, list.size());
    }

    // Tests adding at index size (end) – appending
    @Test
    public void testAddAtIndex_end_insertsCorrectly() {
        list.add("A");
        list.add("B");
        list.add(2, "C");
        assertEquals("A", list.get(0));
        assertEquals("B", list.get(1));
        assertEquals("C", list.get(2));
        assertEquals(3, list.size());
    }

    // Tests adding in the middle – forces tree rebalancing
    @Test
    public void testAddAtIndex_middle_insertsCorrectly() {
        list.add("A");
        list.add("C");
        list.add(1, "B");
        assertEquals("A", list.get(0));
        assertEquals("B", list.get(1));
        assertEquals("C", list.get(2));
        assertEquals(3, list.size());
    }

    // Tests removing the first element (index 0)
    @Test
    public void testRemove_first_removesCorrectly() {
        list.add("A");
        list.add("B");
        list.add("C");
        String removed = list.remove(0);
        assertEquals("A", removed);
        assertEquals(2, list.size());
        assertEquals("B", list.get(0));
        assertEquals("C", list.get(1));
    }

    // Tests removing the last element (index size-1)
    @Test
    public void testRemove_last_removesCorrectly() {
        list.add("A");
        list.add("B");
        list.add("C");
        String removed = list.remove(2);
        assertEquals("C", removed);
        assertEquals(2, list.size());
        assertEquals("A", list.get(0));
        assertEquals("B", list.get(1));
    }

    // Tests removing an element from the middle – triggers AVL deletion logic
    @Test
    public void testRemove_middle_removesCorrectly() {
        list.add("A");
        list.add("B");
        list.add("C");
        list.add("D");
        list.add("E");
        String removed = list.remove(2); // remove "C"
        assertEquals("C", removed);
        assertEquals(4, list.size());
        assertEquals("A", list.get(0));
        assertEquals("B", list.get(1));
        assertEquals("D", list.get(2));
        assertEquals("E", list.get(3));
    }

    // Tests that removing with an out-of-bounds index throws IndexOutOfBoundsException
    @Test(expected = IndexOutOfBoundsException.class)
    public void testRemove_indexOutOfBounds_throwsException() {
        list.add("A");
        list.remove(1); // size is 1, so index 1 is invalid
    }

    // Tests that getting with an out-of-bounds index throws IndexOutOfBoundsException
    @Test(expected = IndexOutOfBoundsException.class)
    public void testGet_indexOutOfBounds_throwsException() {
        list.get(0); // empty list
    }

    // Tests set operation returns old value and updates element
    @Test
    public void testSet_replacesElement_returnsOldValue() {
        list.add("A");
        list.add("B");
        String old = list.set(1, "C");
        assertEquals("B", old);
        assertEquals("C", list.get(1));
    }

    // Tests clear empties the list and resets size
    @Test
    public void testClear_emptiesList() {
        list.add("A");
        list.add("B");
        list.clear();
        assertEquals(0, list.size());
        // verify that get throws exception after clear
        try {
            list.get(0);
            fail("Should have thrown IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    // Tests contains for existing and non-existing elements
    @Test
    public void testContains_existingElement_returnsTrue() {
        list.add("A");
        assertTrue(list.contains("A"));
        assertFalse(list.contains("B"));
    }

    // Tests indexOf for existing, duplicate, and non-existing elements
    @Test
    public void testIndexOf_existingElement_returnsIndex() {
        list.add("A");
        list.add("B");
        list.add("A");
        assertEquals(0, list.indexOf("A"));
        assertEquals(1, list.indexOf("B"));
        assertEquals(-1, list.indexOf("C"));
    }

    // Tests iterator traversal in forward direction
    @Test
    public void testIterator_hasNextNext_returnsElementsInOrder() {
        list.add("A");
        list.add("B");
        list.add("C");
        Iterator<String> it = list.iterator();
        assertTrue(it.hasNext());
        assertEquals("A", it.next());
        assertEquals("B", it.next());
        assertEquals("C", it.next());
        assertFalse(it.hasNext());
    }

    // Tests iterator remove after next() – removes the current element
    @Test
    public void testIterator_remove_removesElement() {
        list.add("A");
        list.add("B");
        list.add("C");
        Iterator<String> it = list.iterator();
        it.next(); // A
        it.next(); // B
        it.remove(); // remove B
        assertEquals(2, list.size());
        assertEquals("A", list.get(0));
        assertEquals("C", list.get(1));
    }

    // Tests ListIterator previous() functionality
    @Test
    public void testListIterator_previous_worksCorrectly() {
        list.add("A");
        list.add("B");
        list.add("C");
        ListIterator<String> lit = list.listIterator(2); // start before index 2 (C)
        assertTrue(lit.hasPrevious());
        assertEquals("B", lit.previous()); // goes to index 1
        assertEquals("A", lit.previous()); // goes to index 0
        assertFalse(lit.hasPrevious());
    }

    // Tests that structural modification outside iterator throws ConcurrentModificationException
    @Test(expected = ConcurrentModificationException.class)
    public void testIterator_concurrentModification_throwsException() {
        list.add("A");
        list.add("B");
        Iterator<String> it = list.iterator();
        list.add("C"); // modify list
        it.next(); // should throw
    }

    // Tests toArray produces the correct array
    @Test
    public void testToArray_returnsArrayWithElements() {
        list.add("X");
        list.add("Y");
        list.add("Z");
        Object[] expected = {"X", "Y", "Z"};
        assertArrayEquals(expected, list.toArray());
    }

    // Tests toArray on an empty list
    @Test
    public void testToArray_emptyList_returnsEmptyArray() {
        assertEquals(0, list.toArray().length);
    }

    // Tests adding at a negative index throws exception
    @Test(expected = IndexOutOfBoundsException.class)
    public void testAdd_negativeIndex_throwsException() {
        list.add(-1, "A");
    }

    // Tests removing at a negative index throws exception
    @Test(expected = IndexOutOfBoundsException.class)
    public void testRemove_negativeIndex_throwsException() {
        list.add("A");
        list.remove(-1);
    }

    // Stress test: add many elements then remove some to exercise tree balancing
    @Test
    public void testAddRemoveMany_keepsCorrectOrder() {
        // Insert elements that cause a skewed tree then remove from middle
        for (int i = 0; i < 10; i++) {
            list.add(String.valueOf(i));
        }
        assertEquals(10, list.size());
        // Remove elements from various positions
        list.remove(0); // remove "0"
        list.remove(5); // remove element at index 5 (original "6" after first removal)
        list.remove(7); // remove last
        assertEquals(7, list.size());
        // Verify remaining elements
        assertEquals("1", list.get(0));
        assertEquals("2", list.get(1));
        assertEquals("3", list.get(2));
        assertEquals("4", list.get(3));
        assertEquals("5", list.get(4));
        assertEquals("7", list.get(5));
        assertEquals("8", list.get(6));
    }

    // Tests that indexOf returns -1 for an empty list
    @Test
    public void testIndexOf_emptyList_returnsNegative() {
        assertEquals(-1, list.indexOf("anything"));
    }

    // Tests that contains returns false for an empty list
    @Test
    public void testContains_emptyList_returnsFalse() {
        assertFalse(list.contains("anything"));
    }
}