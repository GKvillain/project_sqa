package org.apache.commons.collections4;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;

import org.junit.Test;

public class IteratorUtilsTest {

    // Tests emptyIterator() returns an iterator with no elements
    @Test
    public void testEmptyIterator_normalCase_returnsEmptyIterator() {
        Iterator<Object> it = IteratorUtils.emptyIterator();
        assertFalse(it.hasNext());
    }

    // Tests emptyListIterator() returns a list iterator with no elements
    @Test
    public void testEmptyListIterator_normalCase_returnsEmptyListIterator() {
        ListIterator<Object> it = IteratorUtils.emptyListIterator();
        assertFalse(it.hasNext());
        assertFalse(it.hasPrevious());
    }

    // Tests singletonIterator() returns an iterator with a single element
    @Test
    public void testSingletonIterator_normalCase_returnsIteratorWithSingleElement() {
        Iterator<String> it = IteratorUtils.singletonIterator("test");
        assertTrue(it.hasNext());
        assertEquals("test", it.next());
        assertFalse(it.hasNext());
    }

    // Tests arrayIterator() over an object array
    @Test
    public void testArrayIterator_objectArray_returnsIteratorOverArray() {
        String[] array = {"a", "b", "c"};
        Iterator<String> it = IteratorUtils.arrayIterator(array);
        assertEquals("a", it.next());
        assertEquals("b", it.next());
        assertEquals("c", it.next());
        assertFalse(it.hasNext());
    }

    // Tests arrayIterator() with a primitive array type
    @Test
    public void testArrayIterator_primitiveArray_returnsIteratorOverPrimitiveArray() {
        int[] array = {1, 2, 3};
        Iterator<Integer> it = IteratorUtils.arrayIterator(array);
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(1), it.next());
        assertEquals(Integer.valueOf(2), it.next());
        assertEquals(Integer.valueOf(3), it.next());
        assertFalse(it.hasNext());
    }

    // Tests arrayIterator() with start index
    @Test
    public void testArrayIterator_withStartIndex_returnsIteratorFromStart() {
        String[] array = {"a", "b", "c"};
        Iterator<String> it = IteratorUtils.arrayIterator(array, 1);
        assertEquals("b", it.next());
        assertEquals("c", it.next());
        assertFalse(it.hasNext());
    }

    // Tests boundedIterator() with max limit
    @Test
    public void testBoundedIterator_maxLimit_returnsLimitedElements() {
        List<String> list = Arrays.asList("a", "b", "c", "d");
        Iterator<String> it = IteratorUtils.boundedIterator(list.iterator(), 2);
        assertTrue(it.hasNext());
        assertEquals("a", it.next());
        assertTrue(it.hasNext());
        assertEquals("b", it.next());
        assertFalse(it.hasNext());
    }

    // Tests boundedIterator() with offset and max
    @Test
    public void testBoundedIterator_withOffset_skipsFirstElements() {
        List<String> list = Arrays.asList("a", "b", "c", "d");
        Iterator<String> it = IteratorUtils.boundedIterator(list.iterator(), 1, 2);
        assertEquals("b", it.next());
        assertEquals("c", it.next());
        assertFalse(it.hasNext());
    }

    // Tests boundedIterator() with null iterator throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testBoundedIterator_nullIterator_throwsException() {
        IteratorUtils.boundedIterator(null, 0, 1);
    }

    // Tests filteredIterator() returns only matching elements
    @Test
    public void testFilteredIterator_predicate_filtersElements() {
        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);
        Predicate<Integer> predicate = new Predicate<Integer>() {
            @Override
            public boolean evaluate(Integer object) {
                return object % 2 == 0;
            }
        };
        Iterator<Integer> it = IteratorUtils.filteredIterator(list.iterator(), predicate);
        assertEquals(Integer.valueOf(2), it.next());
        assertEquals(Integer.valueOf(4), it.next());
        assertFalse(it.hasNext());
    }

    // Tests filteredIterator() with null predicate throws exception
    @Test(expected = NullPointerException.class)
    public void testFilteredIterator_nullPredicate_throwsException() {
        IteratorUtils.filteredIterator(new ArrayList<String>().iterator(), null);
    }

    // Tests transformedIterator() applies transformation
    @Test
    public void testTransformedIterator_transformer_transformsElements() {
        List<Integer> list = Arrays.asList(1, 2, 3);
        Transformer<Integer, String> transformer = new Transformer<Integer, String>() {
            @Override
            public String transform(Integer input) {
                return "Number: " + input;
            }
        };
        Iterator<String> it = IteratorUtils.transformedIterator(list.iterator(), transformer);
        assertEquals("Number: 1", it.next());
        assertEquals("Number: 2", it.next());
        assertEquals("Number: 3", it.next());
        assertFalse(it.hasNext());
    }

    // Tests asIterable() allows foreach loop
    @Test
    public void testAsIterable_normalIterator_canBeUsedInForEach() {
        List<String> list = Arrays.asList("x", "y");
        Iterable<String> iterable = IteratorUtils.asIterable(list.iterator());
        List<String> result = new ArrayList<String>();
        for (String s : iterable) {
            result.add(s);
        }
        assertEquals(Arrays.asList("x", "y"), result);
    }

    // Tests toList() converts iterator to list
    @Test
    public void testToList_normalIterator_returnsList() {
        List<String> list = Arrays.asList("a", "b", "c");
        List<String> result = IteratorUtils.toList(list.iterator());
        assertEquals(3, result.size());
        assertEquals("a", result.get(0));
        assertEquals("b", result.get(1));
        assertEquals("c", result.get(2));
    }

    // Tests size() returns number of elements
    @Test
    public void testSize_normalIterator_returnsCorrectCount() {
        List<String> list = Arrays.asList("a", "b", "c");
        assertEquals(3, IteratorUtils.size(list.iterator()));
    }

    // Tests size() with null returns 0
    @Test
    public void testSize_nullIterator_returnsZero() {
        assertEquals(0, IteratorUtils.size(null));
    }

    // Tests isEmpty() with null returns true
    @Test
    public void testIsEmpty_nullIterator_returnsTrue() {
        assertTrue(IteratorUtils.isEmpty(null));
    }

    // Tests isEmpty() with non-empty iterator returns false
    @Test
    public void testIsEmpty_nonEmptyIterator_returnsFalse() {
        List<String> list = Arrays.asList("a");
        assertFalse(IteratorUtils.isEmpty(list.iterator()));
    }

    // Tests contains() returns true when object exists
    @Test
    public void testContains_objectExists_returnsTrue() {
        List<String> list = Arrays.asList("a", "b", "c");
        assertTrue(IteratorUtils.contains(list.iterator(), "b"));
    }

    // Tests contains() returns false when object does not exist
    @Test
    public void testContains_objectNotExists_returnsFalse() {
        List<String> list = Arrays.asList("a", "b", "c");
        assertFalse(IteratorUtils.contains(list.iterator(), "z"));
    }

    // Tests get() returns element at index
    @Test
    public void testGet_validIndex_returnsCorrectElement() {
        List<String> list = Arrays.asList("a", "b", "c");
        assertEquals("b", IteratorUtils.get(list.iterator(), 1));
    }

    // Tests get() throws IndexOutOfBoundsException for invalid index
    @Test(expected = IndexOutOfBoundsException.class)
    public void testGet_invalidIndex_throwsException() {
        List<String> list = Arrays.asList("a");
        IteratorUtils.get(list.iterator(), 5);
    }

    // Tests toString() returns default representation
    @Test
    public void testToString_normalIterator_returnsBracketedList() {
        List<String> list = Arrays.asList("a", "b", "c");
        assertEquals("[a, b, c]", IteratorUtils.toString(list.iterator()));
    }

    // Tests toString() with empty iterator returns empty brackets
    @Test
    public void testToString_emptyIterator_returnsEmptyBrackets() {
        Iterator<String> it = IteratorUtils.emptyIterator();
        assertEquals("[]", IteratorUtils.toString(it));
    }

    // Tests getIterator() with null returns empty iterator
    @Test
    public void testGetIterator_nullObject_returnsEmptyIterator() {
        Iterator<?> it = IteratorUtils.getIterator(null);
        assertFalse(it.hasNext());
    }

    // Tests getIterator() with Iterator returns same iterator
    @Test
    public void testGetIterator_iteratorObject_returnsSameIterator() {
        List<String> list = Arrays.asList("a", "b");
        Iterator<String> original = list.iterator();
        Iterator<?> result = IteratorUtils.getIterator(original);
        assertSame(original, result);
    }

    // Tests getIterator() with Map returns values iterator
    @Test
    public void testGetIterator_mapObject_returnsValuesIterator() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("k1", "v1");
        map.put("k2", "v2");
        Iterator<?> it = IteratorUtils.getIterator(map);
        assertTrue(it.hasNext());
        it.next();
        assertTrue(it.hasNext());
        it.next();
        assertFalse(it.hasNext());
    }

    // ========== New tests for uncovered coverage ==========

    // Tests singletonListIterator() returns a list iterator with single element
    @Test
    public void testSingletonListIterator_normalCase_returnsListIteratorWithSingleElement() {
        ListIterator<String> it = IteratorUtils.singletonListIterator("test");
        assertTrue(it.hasNext());
        assertEquals("test", it.next());
        assertFalse(it.hasNext());
        assertTrue(it.hasPrevious());
        assertEquals("test", it.previous());
        assertFalse(it.hasPrevious());
    }

    // Tests asEnumeration() converts iterator to enumeration
    @Test
    public void testAsEnumeration_normalIterator_returnsEnumeration() {
        List<String> list = Arrays.asList("a", "b", "c");
        Enumeration<String> enumeration = IteratorUtils.asEnumeration(list.iterator());
        assertTrue(enumeration.hasMoreElements());
        assertEquals("a", enumeration.nextElement());
        assertEquals("b", enumeration.nextElement());
        assertEquals("c", enumeration.nextElement());
        assertFalse(enumeration.hasMoreElements());
    }

    // Tests asIterator() converts enumeration to iterator
    @Test
    public void testAsIterator_normalEnumeration_returnsIterator() {
        List<String> list = Arrays.asList("x", "y", "z");
        Enumeration<String> enumeration = Collections.enumeration(list);
        Iterator<String> it = IteratorUtils.asIterator(enumeration);
        assertTrue(it.hasNext());
        assertEquals("x", it.next());
        assertEquals("y", it.next());
        assertEquals("z", it.next());
        assertFalse(it.hasNext());
    }

    // Tests filteredIterator() with null iterator throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testFilteredIterator_nullIterator_throwsException() {
        Predicate<String> predicate = new Predicate<String>() {
            @Override
            public boolean evaluate(String object) {
                return true;
            }
        };
        IteratorUtils.filteredIterator(null, predicate);
    }

    // Tests transformedIterator() with null transformer throws exception
    @Test(expected = NullPointerException.class)
    public void testTransformedIterator_nullTransformer_throwsException() {
        IteratorUtils.transformedIterator(new ArrayList<String>().iterator(), null);
    }

    // Tests emptyOrderedIterator() returns an ordered iterator with no elements
    @Test
    public void testEmptyOrderedIterator_normalCase_returnsEmptyOrderedIterator() {
        OrderedIterator<Object> it = IteratorUtils.emptyOrderedIterator();
        assertFalse(it.hasNext());
    }

    // Tests loopingIterator() loops over elements infinitely
    @Test
    public void testLoopingIterator_normalCase_loopsOverElements() {
        List<String> list = Arrays.asList("first", "second");
        Iterator<String> it = IteratorUtils.loopingIterator(list.iterator());
        assertEquals("first", it.next());
        assertEquals("second", it.next());
        assertEquals("first", it.next());
        assertEquals("second", it.next());
    }
}