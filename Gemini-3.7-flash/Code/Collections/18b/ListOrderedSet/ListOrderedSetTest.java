package org.apache.commons.collections.set;

import org.apache.commons.collections.OrderedIterator;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class ListOrderedSetTest {

    private ListOrderedSet<String> set;

    @Before
    public void setUp() {
        set = new ListOrderedSet<String>();
    }

    // Tests factory method with valid empty Set and List
    @Test
    public void testListOrderedSet_emptySetAndList_createsInstance() {
        Set<String> s = new HashSet<String>();
        List<String> l = new ArrayList<String>();
        ListOrderedSet<String> orderedSet = ListOrderedSet.listOrderedSet(s, l);
        assertNotNull(orderedSet);
        assertTrue(orderedSet.isEmpty());
    }

    // Tests factory method throws exception when Set is null
    @Test(expected = IllegalArgumentException.class)
    public void testListOrderedSet_nullSetWithList_throwsException() {
        ListOrderedSet.listOrderedSet(null, new ArrayList<String>());
    }

    // Tests factory method throws exception when List is null
    @Test(expected = IllegalArgumentException.class)
    public void testListOrderedSet_nullListWithSet_throwsException() {
        ListOrderedSet.listOrderedSet(new HashSet<String>(), null);
    }

    // Tests factory method throws exception when Set is non-empty
    @Test(expected = IllegalArgumentException.class)
    public void testListOrderedSet_nonEmptySet_throwsException() {
        Set<String> s = new HashSet<String>();
        s.add("item");
        ListOrderedSet.listOrderedSet(s, new ArrayList<String>());
    }

    // Tests factory method throws exception when List is non-empty
    @Test(expected = IllegalArgumentException.class)
    public void testListOrderedSet_nonEmptyList_throwsException() {
        List<String> l = new ArrayList<String>();
        l.add("item");
        ListOrderedSet.listOrderedSet(new HashSet<String>(), l);
    }

    // Tests factory method with Set only and null check
    @Test(expected = IllegalArgumentException.class)
    public void testListOrderedSet_nullSet_throwsException() {
        ListOrderedSet.listOrderedSet((Set<String>) null);
    }

    // Tests factory method with List containing duplicates
    @Test
    public void testListOrderedSet_listWithDuplicates_removesDuplicatesAndPreservesOrder() {
        List<String> list = new ArrayList<String>(Arrays.asList("A", "B", "A", "C", "B"));
        ListOrderedSet<String> orderedSet = ListOrderedSet.listOrderedSet(list);
        assertEquals(3, orderedSet.size());
        assertEquals("A", orderedSet.get(0));
        assertEquals("B", orderedSet.get(1));
        assertEquals("C", orderedSet.get(2));
    }

    // Tests factory method with null List throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testListOrderedSet_nullList_throwsException() {
        ListOrderedSet.listOrderedSet((List<String>) null);
    }

    // Tests adding elements and maintaining insertion order
    @Test
    public void testAdd_newAndDuplicateElements_maintainsInsertionOrder() {
        assertTrue(set.add("first"));
        assertTrue(set.add("second"));
        assertFalse(set.add("first"));
        assertEquals(2, set.size());
        assertEquals("first", set.get(0));
        assertEquals("second", set.get(1));
    }

    // Tests adding element at specified index
    @Test
    public void testAddAtIndex_newAndDuplicateElements_insertsCorrectly() {
        set.add("A");
        set.add("C");
        set.add(1, "B");
        assertEquals(3, set.size());
        assertEquals("A", set.get(0));
        assertEquals("B", set.get(1));
        assertEquals("C", set.get(2));

        // Adding already existing element at index should be ignored
        set.add(0, "B");
        assertEquals(3, set.size());
        assertEquals("A", set.get(0));
    }

    // Tests addAll preserving order and skipping duplicates
    @Test
    public void testAddAll_collectionWithDuplicates_addsUniqueElements() {
        set.add("A");
        boolean changed = set.addAll(Arrays.asList("B", "A", "C"));
        assertTrue(changed);
        assertEquals(3, set.size());
        assertEquals("A", set.get(0));
        assertEquals("B", set.get(1));
        assertEquals("C", set.get(2));

        boolean changedAgain = set.addAll(Arrays.asList("A", "B"));
        assertFalse(changedAgain);
    }

    // Tests addAll at specified index
    @Test
    public void testAddAllAtIndex_collectionWithElements_insertsAtCorrectIndex() {
        set.add("A");
        set.add("D");
        boolean changed = set.addAll(1, Arrays.asList("B", "C", "A"));
        assertTrue(changed);
        assertEquals(4, set.size());
        assertEquals("A", set.get(0));
        assertEquals("B", set.get(1));
        assertEquals("C", set.get(2));
        assertEquals("D", set.get(3));

        boolean noChange = set.addAll(0, Arrays.asList("A", "D"));
        assertFalse(noChange);
    }

    // Tests get and indexOf methods
    @Test
    public void testGetAndIndexOf_validElements_returnsExpectedValues() {
        set.add("zero");
        set.add("one");
        set.add("two");

        assertEquals("zero", set.get(0));
        assertEquals("two", set.get(2));
        assertEquals(0, set.indexOf("zero"));
        assertEquals(1, set.indexOf("one"));
        assertEquals(-1, set.indexOf("nonexistent"));
    }

    // Tests remove by object
    @Test
    public void testRemove_byObject_removesElementAndUpdatesOrder() {
        set.add("A");
        set.add("B");
        set.add("C");

        assertTrue(set.remove("B"));
        assertEquals(2, set.size());
        assertEquals("A", set.get(0));
        assertEquals("C", set.get(1));
        assertFalse(set.remove("nonexistent"));
    }

    // Tests remove by index
    @Test
    public void testRemove_byIndex_removesAndReturnsElement() {
        set.add("A");
        set.add("B");
        set.add("C");

        Object removed = set.remove(1);
        assertEquals("B", removed);
        assertEquals(2, set.size());
        assertEquals("A", set.get(0));
        assertEquals("C", set.get(1));
        assertFalse(set.contains("B"));
    }

    // Tests removeAll
    @Test
    public void testRemoveAll_matchingElements_removesAllSpecified() {
        set.add("A");
        set.add("B");
        set.add("C");

        boolean changed = set.removeAll(Arrays.asList("A", "C", "D"));
        assertTrue(changed);
        assertEquals(1, set.size());
        assertEquals("B", set.get(0));

        assertFalse(set.removeAll(Arrays.asList("X", "Y")));
    }

    // Tests retainAll with partial match
    @Test
    public void testRetainAll_subset_retainsMatchingElementsInOrder() {
        set.add("A");
        set.add("B");
        set.add("C");
        set.add("D");

        boolean changed = set.retainAll(Arrays.asList("C", "A"));
        assertTrue(changed);
        assertEquals(2, set.size());
        assertEquals("A", set.get(0));
        assertEquals("C", set.get(1));

        assertFalse(set.retainAll(Arrays.asList("A", "C")));
    }

    // Tests retainAll with empty collection
    @Test
    public void testRetainAll_emptyCollection_clearsSet() {
        set.add("A");
        set.add("B");

        boolean changed = set.retainAll(new ArrayList<String>());
        assertTrue(changed);
        assertEquals(0, set.size());
        assertTrue(set.isEmpty());
    }

    // Tests clear method
    @Test
    public void testClear_populatedSet_resetsSet() {
        set.add("A");
        set.add("B");
        set.clear();

        assertEquals(0, set.size());
        assertTrue(set.isEmpty());
        assertEquals(-1, set.indexOf("A"));
    }

    // Tests asList view
    @Test(expected = UnsupportedOperationException.class)
    public void testAsList_unmodifiableView_throwsExceptionOnModification() {
        set.add("A");
        set.add("B");
        List<String> list = set.asList();
        assertEquals(2, list.size());
        assertEquals("A", list.get(0));
        assertEquals("B", list.get(1));

        list.add("C");
    }

    // Tests toArray methods
    @Test
    public void testToArray_validSet_returnsElementsInOrder() {
        set.add("A");
        set.add("B");

        Object[] objArray = set.toArray();
        assertArrayEquals(new Object[]{"A", "B"}, objArray);

        String[] strArray = set.toArray(new String[2]);
        assertArrayEquals(new String[]{"A", "B"}, strArray);
    }

    // Tests OrderedIterator traversal and remove
    @Test
    public void testIterator_bidirectionalAndRemove_functionsCorrectly() {
        set.add("A");
        set.add("B");
        set.add("C");

        OrderedIterator<String> it = set.iterator();
        assertTrue(it.hasNext());
        assertEquals("A", it.next());
        assertEquals("B", it.next());

        assertTrue(it.hasPrevious());
        assertEquals("B", it.previous());
        assertEquals("B", it.next());

        it.remove();
        assertEquals(2, set.size());
        assertFalse(set.contains("B"));
        assertEquals("C", it.next());
    }

    // Tests toString method
    @Test
    public void testToString_populatedSet_returnsListFormatString() {
        set.add("A");
        set.add("B");
        assertEquals("[A, B]", set.toString());
    }
}