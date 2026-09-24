package org.apache.commons.collections4;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Vector;

import org.apache.commons.collections4.functors.EqualPredicate;
import org.apache.commons.collections4.functors.NotNullPredicate;
import org.apache.commons.collections4.functors.TruePredicate;
import org.apache.commons.collections4.iterators.BoundedIterator;
import org.apache.commons.collections4.iterators.CollatedIterator;
import org.apache.commons.collections4.iterators.FilterIterator;
import org.apache.commons.collections4.iterators.FilterListIterator;
import org.apache.commons.collections4.iterators.LoopingIterator;
import org.apache.commons.collections4.iterators.LoopingListIterator;
import org.apache.commons.collections4.iterators.ObjectArrayIterator;
import org.apache.commons.collections4.iterators.ObjectArrayListIterator;
import org.apache.commons.collections4.iterators.ObjectGraphIterator;
import org.apache.commons.collections4.iterators.PeekingIterator;
import org.apache.commons.collections4.iterators.PushbackIterator;
import org.apache.commons.collections4.iterators.SkippingIterator;
import org.apache.commons.collections4.iterators.TransformIterator;
import org.apache.commons.collections4.iterators.ZippingIterator;
import org.junit.Test;

public class IteratorUtilsTest {

    // Tests emptyIterator returns an empty iterator
    @Test
    public void testEmptyIterator_noElements_hasNextReturnsFalse() {
        final ResettableIterator<Object> it = IteratorUtils.emptyIterator();
        assertNotNull(it);
        assertFalse(it.hasNext());
    }

    // Tests emptyListIterator returns an empty list iterator
    @Test
    public void testEmptyListIterator_noElements_hasPreviousAndNextReturnFalse() {
        final ResettableListIterator<Object> it = IteratorUtils.emptyListIterator();
        assertNotNull(it);
        assertFalse(it.hasNext());
        assertFalse(it.hasPrevious());
    }

    // Tests singletonIterator with a single element
    @Test
    public void testSingletonIterator_singleElement_iteratesOnce() {
        final ResettableIterator<String> it = IteratorUtils.singletonIterator("test");
        assertTrue(it.hasNext());
        assertEquals("test", it.next());
        assertFalse(it.hasNext());
    }

    // Tests arrayIterator with standard array
    @Test
    public void testArrayIterator_validArray_iteratesAllElements() {
        final String[] array = new String[] { "a", "b", "c" };
        final ResettableIterator<String> it = IteratorUtils.arrayIterator(array);
        assertEquals("a", it.next());
        assertEquals("b", it.next());
        assertEquals("c", it.next());
        assertFalse(it.hasNext());
    }

    // Tests arrayIterator with subset bounds
    @Test
    public void testArrayIterator_withStartAndEnd_iteratesSubArray() {
        final String[] array = new String[] { "a", "b", "c", "d" };
        final ResettableIterator<String> it = IteratorUtils.arrayIterator(array, 1, 3);
        assertEquals("b", it.next());
        assertEquals("c", it.next());
        assertFalse(it.hasNext());
    }

    // Tests boundedIterator limits number of elements returned
    @Test
    public void testBoundedIterator_validLimit_returnsSpecifiedMax() {
        final List<String> list = Arrays.asList("a", "b", "c", "d");
        final BoundedIterator<String> it = IteratorUtils.boundedIterator(list.iterator(), 2);
        assertEquals("a", it.next());
        assertEquals("b", it.next());
        assertFalse(it.hasNext());
    }

    // Tests skippingIterator skips the initial elements
    @Test
    public void testSkippingIterator_validOffset_skipsElements() {
        final List<String> list = Arrays.asList("a", "b", "c", "d");
        final SkippingIterator<String> it = IteratorUtils.skippingIterator(list.iterator(), 2);
        assertEquals("c", it.next());
        assertEquals("d", indulgence(it.next()));
        assertFalse(it.hasNext());
    }

    private Object indulgence(Object next) {
        return next;
    }

    // Tests zippingIterator interleaves elements from multiple iterators
    @Test
    public void testZippingIterator_twoIterators_interleavesElements() {
        final Iterator<String> it1 = Arrays.asList("a", "c").iterator();
        final Iterator<String> it2 = Arrays.asList("b", "d").iterator();
        final ZippingIterator<String> zipping = IteratorUtils.zippingIterator(it1, it2);
        assertEquals("a", zipping.next());
        assertEquals("b", zipping.next());
        assertEquals("c", zipping.next());
        assertEquals("d", zipping.next());
        assertFalse(zipping.hasNext());
    }

    // Tests toList converts iterator elements to a List
    @Test
    public void testToList_validIterator_returnsPopulatedList() {
        final List<String> original = Arrays.asList("x", "y", "z");
        final List<String> result = IteratorUtils.toList(original.iterator());
        assertEquals(original, result);
    }

    // Tests toArray converts iterator elements to an Object array
    @Test
    public void testToArray_validIterator_returnsObjectArray() {
        final List<String> list = Arrays.asList("one", "two");
        final Object[] array = IteratorUtils.toArray(list.iterator());
        assertArrayEquals(new Object[] { "one", "two" }, array);
    }

    // Tests toArray with type converts iterator elements to typed array
    @Test
    public void testToArray_withClassType_returnsTypedArray() {
        final List<String> list = Arrays.asList("one", "two");
        final String[] array = IteratorUtils.toArray(list.iterator(), String.class);
        assertArrayEquals(new String[] { "one", "two" }, array);
    }

    // Tests getIterator handles different input types (null, Collection, Map, Array)
    @Test
    public void testGetIterator_variousTypes_returnsAppropriateIterator() {
        assertTrue(IteratorUtils.isEmpty(IteratorUtils.getIterator(null)));

        final List<String> list = Arrays.asList("a", "b");
        assertEquals("a", IteratorUtils.getIterator(list).next());

        final Map<String, String> map = new HashMap<String, String>();
        map.put("key", "val");
        assertEquals("val", IteratorUtils.getIterator(map).next());

        final String[] array = new String[] { "elem" };
        assertEquals("elem", IteratorUtils.getIterator(array).next());
    }

    // Tests find returns the first matching element
    @Test
    public void testFind_matchingElementExists_returnsFirstMatch() {
        final List<String> list = Arrays.asList("apple", "banana", "cherry");
        final String result = IteratorUtils.find(list.iterator(), EqualPredicate.equalPredicate("banana"));
        assertEquals("banana", result);

        final String notFound = IteratorUtils.find(list.iterator(), EqualPredicate.equalPredicate("orange"));
        assertNull(notFound);
    }

    // Tests matchesAny returns true when at least one element matches
    @Test
    public void testMatchesAny_matchingElementExists_returnsTrue() {
        final List<Integer> list = Arrays.asList(1, 2, 3);
        assertTrue(IteratorUtils.matchesAny(list.iterator(), EqualPredicate.equalPredicate(2)));
        assertFalse(IteratorUtils.matchesAny(list.iterator(), EqualPredicate.equalPredicate(5)));
        assertFalse(IteratorUtils.matchesAny(null, EqualPredicate.equalPredicate(1)));
    }

    // Tests matchesAll returns true only when all elements match
    @Test
    public void testMatchesAll_allMatch_returnsTrue() {
        final List<String> list = Arrays.asList("a", "b", "c");
        assertTrue(IteratorUtils.matchesAll(list.iterator(), NotNullPredicate.notNullPredicate()));

        final List<String> withNull = Arrays.asList("a", null, "c");
        assertFalse(IteratorUtils.matchesAll(withNull.iterator(), NotNullPredicate.notNullPredicate()));
    }

    // Tests isEmpty for null, empty and non-empty iterators
    @Test
    public void testIsEmpty_nullAndEmpty_returnsCorrectBoolean() {
        assertTrue(IteratorUtils.isEmpty(null));
        assertTrue(IteratorUtils.isEmpty(Collections.emptyList().iterator()));
        assertFalse(IteratorUtils.isEmpty(Arrays.asList("a").iterator()));
    }

    // Tests contains checks presence of object in iterator
    @Test
    public void testContains_elementInIterator_returnsCorrectResult() {
        final List<String> list = Arrays.asList("a", "b", "c");
        assertTrue(IteratorUtils.contains(list.iterator(), "b"));
        assertFalse(IteratorUtils.contains(list.iterator(), "z"));
        assertFalse(IteratorUtils.contains(null, "a"));
    }

    // Tests get retrieves the element at specified index
    @Test
    public void testGet_validIndex_returnsElement() {
        final List<String> list = Arrays.asList("first", "second", "third");
        assertEquals("first", IteratorUtils.get(list.iterator(), 0));
        assertEquals("second", IteratorUtils.get(list.iterator(), 1));
        assertEquals("third", IteratorUtils.get(list.iterator(), 2));
    }

    // Tests get throws IndexOutOfBoundsException when index is out of bounds
    @Test(expected = IndexOutOfBoundsException.class)
    public void testGet_indexOutOfBounds_throwsException() {
        final List<String> list = Arrays.asList("first");
        IteratorUtils.get(list.iterator(), 5);
    }

    // Tests size counts elements accurately
    @Test
    public void testSize_validIterator_returnsCount() {
        assertEquals(0, IteratorUtils.size(null));
        assertEquals(0, IteratorUtils.size(Collections.emptyList().iterator()));
        assertEquals(3, IteratorUtils.size(Arrays.asList("1", "2", "3").iterator()));
    }

    // Tests toString formats iterator contents correctly
    @Test
    public void testToString_validIterator_formatsToString() {
        final List<String> list = Arrays.asList("a", "b", "c");
        assertEquals("[a, b, c]", IteratorUtils.toString(list.iterator()));
        assertEquals("[]", IteratorUtils.toString(Collections.emptyList().iterator()));
        assertEquals("[]", IteratorUtils.toString(null));
    }

    // Tests asIterator and asEnumeration bridge utilities
    @Test
    public void testAsIteratorAndAsEnumeration_bridgeConversion_iteratesCorrectly() {
        final Vector<String> vector = new Vector<String>(Arrays.asList("1", "2"));
        final Iterator<String> it = IteratorUtils.asIterator(vector.elements());
        assertTrue(it.hasNext());
        assertEquals("1", it.next());

        final Enumeration<String> en = IteratorUtils.asEnumeration(Arrays.asList("x", "y").iterator());
        assertTrue(en.hasMoreElements());
        assertEquals("x", en.nextElement());
    }

    // === Additional Tests for Uncovered IteratorUtils Methods ===

    @Test
    public void testEmptyMapAndOrderedIterators() {
        final MapIterator<Object, Object> mapIt = IteratorUtils.emptyMapIterator();
        assertNotNull(mapIt);
        assertFalse(mapIt.hasNext());

        final OrderedIterator<Object> orderedIt = IteratorUtils.emptyOrderedIterator();
        assertNotNull(orderedIt);
        assertFalse(orderedIt.hasNext());
        assertFalse(orderedIt.hasPrevious());

        final OrderedMapIterator<Object, Object> orderedMapIt = IteratorUtils.emptyOrderedMapIterator();
        assertNotNull(orderedMapIt);
        assertFalse(orderedMapIt.hasNext());
        assertFalse(orderedMapIt.hasPrevious());
    }

    @Test
    public void testSingletonListIterator() {
        final ResettableListIterator<String> listIt = IteratorUtils.singletonListIterator("single");
        assertTrue(listIt.hasNext());
        assertFalse(listIt.hasPrevious());
        assertEquals("single", listIt.next());
        assertFalse(listIt.hasNext());
        assertTrue(listIt.hasPrevious());
        assertEquals("single", listIt.previous());
    }

    @Test
    public void testArrayListIterator() {
        final String[] array = new String[] { "a", "b", "c", "d" };
        final ResettableListIterator<String> it = IteratorUtils.arrayListIterator(array);
        assertEquals("a", it.next());
        assertEquals("b", it.next());

        final ResettableListIterator<String> itStart = IteratorUtils.arrayListIterator(array, 2);
        assertEquals("c", itStart.next());

        final ResettableListIterator<String> itBound = IteratorUtils.arrayListIterator(array, 1, 3);
        assertEquals("b", itBound.next());
        assertEquals("c", itBound.next());
        assertFalse(itBound.hasNext());
    }

    @Test
    public void testArrayIteratorAndArrayListIteratorObjectOverloads() {
        final int[] primitiveArray = new int[] { 10, 20, 30, 40 };

        final ResettableIterator<Object> it = IteratorUtils.arrayIterator((Object) primitiveArray);
        assertEquals(10, it.next());
        assertEquals(20, it.next());

        final ResettableIterator<Object> itStart = IteratorUtils.arrayIterator((Object) primitiveArray, 2);
        assertEquals(30, itStart.next());

        final ResettableIterator<Object> itRange = IteratorUtils.arrayIterator((Object) primitiveArray, 1, 3);
        assertEquals(20, itRange.next());
        assertEquals(30, itRange.next());
        assertFalse(itRange.hasNext());

        final ResettableListIterator<Object> listIt = IteratorUtils.arrayListIterator((Object) primitiveArray);
        assertEquals(10, listIt.next());

        final ResettableListIterator<Object> listItStart = IteratorUtils.arrayListIterator((Object) primitiveArray, 2);
        assertEquals(30, listItStart.next());

        final ResettableListIterator<Object> listItRange = IteratorUtils.arrayListIterator((Object) primitiveArray, 1, 3);
        assertEquals(20, listItRange.next());
        assertEquals(30, listItRange.next());
        assertFalse(listItRange.hasNext());
    }

    @Test
    public void testArrayIteratorWithSingleStart() {
        final String[] array = new String[] { "a", "b", "c" };
        final ResettableIterator<String> it = IteratorUtils.arrayIterator(array, 1);
        assertEquals("b", it.next());
        assertEquals("c", it.next());
        assertFalse(it.hasNext());
    }

    @Test
    public void testChainedIterator() {
        final Iterator<String> it1 = Arrays.asList("1", "2").iterator();
        final Iterator<String> it2 = Arrays.asList("3", "4").iterator();
        final Iterator<String> chained = IteratorUtils.chainedIterator(it1, it2);

        assertEquals("1", chained.next());
        assertEquals("2", chained.next());
        assertEquals("3", chained.next());
        assertEquals("4", chained.next());
        assertFalse(chained.hasNext());

        final Collection<Iterator<? extends String>> itColl = new ArrayList<Iterator<? extends String>>();
        itColl.add(Arrays.asList("a").iterator());
        itColl.add(Arrays.asList("b").iterator());
        final Iterator<String> chainedColl = IteratorUtils.chainedIterator(itColl);
        assertEquals("a", chainedColl.next());
        assertEquals("b", chainedColl.next());
        assertFalse(chainedColl.hasNext());
    }

    @Test
    public void testCollatedIterator() {
        final Comparator<Integer> comp = Comparator.naturalOrder();
        final Iterator<Integer> it1 = Arrays.asList(1, 3, 5).iterator();
        final Iterator<Integer> it2 = Arrays.asList(2, 4, 6).iterator();
        final Iterator<Integer> collated = IteratorUtils.collatedIterator(comp, it1, it2);

        assertEquals(Integer.valueOf(1), collated.next());
        assertEquals(Integer.valueOf(2), collated.next());
        assertEquals(Integer.valueOf(3), collated.next());
        assertEquals(Integer.valueOf(4), collated.next());
        assertEquals(Integer.valueOf(5), collated.next());
        assertEquals(Integer.valueOf(6), collated.next());
        assertFalse(collated.hasNext());

        final Collection<Iterator<? extends Integer>> itColl = new ArrayList<Iterator<? extends Integer>>();
        itColl.add(Arrays.asList(1, 4).iterator());
        itColl.add(Arrays.asList(2, 3).iterator());
        final Iterator<Integer> collatedColl = IteratorUtils.collatedIterator(comp, itColl);
        assertEquals(Integer.valueOf(1), collatedColl.next());
        assertEquals(Integer.valueOf(2), collatedColl.next());
        assertEquals(Integer.valueOf(3), collatedColl.next());
        assertEquals(Integer.valueOf(4), collatedColl.next());
        assertFalse(collatedColl.hasNext());
    }

    @Test
    public void testFilteredAndTransformedIterator() {
        final List<Integer> numbers = Arrays.asList(1, 2, 3, 4);
        final Iterator<Integer> filtered = IteratorUtils.filteredIterator(numbers.iterator(), new Predicate<Integer>() {
            @Override
            public boolean evaluate(final Integer object) {
                return object % 2 == 0;
            }
        });
        assertEquals(Integer.valueOf(2), filtered.next());
        assertEquals(Integer.valueOf(4), filtered.next());
        assertFalse(filtered.hasNext());

        final ListIterator<Integer> listIt = numbers.listIterator();
        final ListIterator<Integer> filteredList = IteratorUtils.filteredListIterator(listIt, new Predicate<Integer>() {
            @Override
            public boolean evaluate(final Integer object) {
                return object > 2;
            }
        });
        assertEquals(Integer.valueOf(3), filteredList.next());
        assertEquals(Integer.valueOf(4), filteredList.next());

        final Iterator<String> transformed = IteratorUtils.transformedIterator(numbers.iterator(), new Transformer<Integer, String>() {
            @Override
            public String transform(final Integer input) {
                return "num:" + input;
            }
        });
        assertEquals("num:1", transformed.next());
        assertEquals("num:2", transformed.next());
    }

    @Test
    public void testUnmodifiableIterators() {
        final List<String> list = new ArrayList<String>(Arrays.asList("a", "b"));
        final Iterator<String> unmodifiable = IteratorUtils.unmodifiableIterator(list.iterator());
        assertEquals("a", unmodifiable.next());
        try {
            unmodifiable.remove();
            fail("Expected UnsupportedOperationException");
        } catch (final UnsupportedOperationException expected) {
            // expected
        }

        final ListIterator<String> unmodifiableList = IteratorUtils.unmodifiableListIterator(list.listIterator());
        assertEquals("a", unmodifiableList.next());
        try {
            unmodifiableList.remove();
            fail("Expected UnsupportedOperationException");
        } catch (final UnsupportedOperationException expected) {
            // expected
        }

        final MapIterator<Object, Object> emptyMapIt = IteratorUtils.emptyMapIterator();
        final MapIterator<Object, Object> unmodifiableMap = IteratorUtils.unmodifiableMapIterator(emptyMapIt);
        assertNotNull(unmodifiableMap);
    }

    @Test
    public void testLoopingIteratorAndListIterator() {
        final List<String> list = Arrays.asList("a", "b");
        final ResettableIterator<String> looping = IteratorUtils.loopingIterator(list);
        assertEquals("a", looping.next());
        assertEquals("b", looping.next());
        assertEquals("a", looping.next());

        final ResettableListIterator<String> loopingList = IteratorUtils.loopingListIterator(list);
        assertEquals("a", loopingList.next());
        assertEquals("b", loopingList.next());
        assertEquals("a", loopingList.next());
    }

    @Test
    public void testPeekingAndPushbackIterator() {
        final List<String> list = Arrays.asList("a", "b", "c");
        final PeekingIterator<String> peeking = IteratorUtils.peekingIterator(list.iterator());
        assertEquals("a", peeking.peek());
        assertEquals("a", peeking.next());
        assertEquals("b", peeking.peek());

        final PushbackIterator<String> pushback = IteratorUtils.pushbackIterator(list.iterator());
        assertEquals("a", pushback.next());
        pushback.pushback("x");
        assertEquals("x", pushback.next());
        assertEquals("b", pushback.next());
    }

    @Test
    public void testObjectGraphIterator() {
        final Iterator<Object> it = IteratorUtils.objectGraphIterator("root", new Transformer<Object, Object>() {
            @Override
            public Object transform(final Object input) {
                if ("root".equals(input)) {
                    return Arrays.asList("leaf1", "leaf2").iterator();
                }
                return null;
            }
        });
        assertTrue(it.hasNext());
        assertEquals("leaf1", it.next());
        assertEquals("leaf2", it.next());
        assertFalse(it.hasNext());
    }

    @Test
    public void testAsIterableAndAsMultipleUseIterable() {
        final List<String> list = Arrays.asList("a", "b");
        final Iterable<String> iterable = IteratorUtils.asIterable(list.iterator());
        final List<String> result = new ArrayList<String>();
        for (final String s : iterable) {
            result.add(s);
        }
        assertEquals(list, result);

        final Iterable<String> multiUse = IteratorUtils.asMultipleUseIterable(list.iterator());
        final List<String> pass1 = new ArrayList<String>();
        for (final String s : multiUse) {
            pass1.add(s);
        }
        final List<String> pass2 = new ArrayList<String>();
        for (final String s : multiUse) {
            pass2.add(s);
        }
        assertEquals(list, pass1);
        assertEquals(list, pass2);
    }

    @Test
    public void testAsIteratorWithCollection() {
        final Vector<String> vector = new Vector<String>(Arrays.asList("1", "2"));
        final List<String> removeList = new ArrayList<String>(Arrays.asList("1", "2"));
        final Iterator<String> it = IteratorUtils.asIterator(vector.elements(), removeList);
        assertEquals("1", it.next());
        it.remove();
        assertEquals(1, removeList.size());
        assertEquals("2", removeList.get(0));
    }

    @Test
    public void testIndexOfAndFirst() {
        final List<String> list = Arrays.asList("apple", "banana", "cherry");
        assertEquals(1, IteratorUtils.indexOf(list.iterator(), EqualPredicate.equalPredicate("banana")));
        assertEquals(-1, IteratorUtils.indexOf(list.iterator(), EqualPredicate.equalPredicate("pear")));
        assertEquals(-1, IteratorUtils.indexOf(null, EqualPredicate.equalPredicate("pear")));

        assertEquals("apple", IteratorUtils.first(list.iterator()));
        assertNull(IteratorUtils.first(Collections.emptyList().iterator()));
        assertNull(IteratorUtils.first(null));
    }

    @Test
    public void testForEachAndForEachButLast() {
        final List<String> list = Arrays.asList("a", "b", "c");
        final List<String> collector = new ArrayList<String>();
        IteratorUtils.forEach(list.iterator(), new Closure<String>() {
            @Override
            public void execute(final String input) {
                collector.add(input);
            }
        });
        assertEquals(list, collector);

        final List<String> collectorButLast = new ArrayList<String>();
        final String last = IteratorUtils.forEachButLast(list.iterator(), new Closure<String>() {
            @Override
            public void execute(final String input) {
                collectorButLast.add(input);
            }
        });
        assertEquals(Arrays.asList("a", "b"), collectorButLast);
        assertEquals("c", last);

        assertNull(IteratorUtils.forEachButLast(null, null));
        assertNull(IteratorUtils.forEachButLast(Collections.emptyList().iterator(), null));
    }

    @Test
    public void testToListWithEstimatedSize() {
        final List<String> original = Arrays.asList("x", "y", "z");
        final List<String> result = IteratorUtils.toList(original.iterator(), 10);
        assertEquals(original, result);
    }

    @Test
    public void testToStringCustom() {
        final List<Integer> list = Arrays.asList(1, 2, 3);
        final Transformer<Integer, String> trans = new Transformer<Integer, String>() {
            @Override
            public String transform(final Integer input) {
                return "v" + input;
            }
        };
        assertEquals("[v1, v2, v3]", IteratorUtils.toString(list.iterator(), trans));
        assertEquals("<v1;v2;v3>", IteratorUtils.toString(list.iterator(), trans, ";", "<", ">"));
        assertEquals("<>", IteratorUtils.toString(null, trans, ";", "<", ">"));
    }

    @Test
    public void testGetIteratorAdditionalTypes() {
        final Hashtable<String, String> dict = new Hashtable<String, String>();
        dict.put("k", "v");
        final Iterator<?> dictIt = IteratorUtils.getIterator(dict);
        assertTrue(dictIt.hasNext());

        final Vector<String> vec = new Vector<String>(Arrays.asList("val"));
        final Iterator<?> enumIt = IteratorUtils.getIterator(vec.elements());
        assertTrue(enumIt.hasNext());
        assertEquals("val", enumIt.next());

        final Iterator<String> rawIt = Arrays.asList("raw").iterator();
        assertSame(rawIt, IteratorUtils.getIterator(rawIt));

        final Iterator<?> singleObjIt = IteratorUtils.getIterator("singleObject");
        assertTrue(singleObjIt.hasNext());
        assertEquals("singleObject", singleObjIt.next());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetNegativeIndex() {
        final List<String> list = Arrays.asList("a", "b");
        IteratorUtils.get(list.iterator(), -1);
    }

    @Test(expected = NullPointerException.class)
    public void testGetNullIterator() {
        IteratorUtils.get(null, 0);
    }

    @Test
    public void testZippingIteratorWithThreeIterators() {
        final Iterator<String> it1 = Arrays.asList("a", "d").iterator();
        final Iterator<String> it2 = Arrays.asList("b", "e").iterator();
        final Iterator<String> it3 = Arrays.asList("c", "f").iterator();
        final ZippingIterator<String> zipping = IteratorUtils.zippingIterator(it1, it2, it3);
        assertEquals("a", zipping.next());
        assertEquals("b", zipping.next());
        assertEquals("c", zipping.next());
        assertEquals("d", zipping.next());
        assertEquals("e", zipping.next());
        assertEquals("f", zipping.next());
        assertFalse(zipping.hasNext());
    }
}