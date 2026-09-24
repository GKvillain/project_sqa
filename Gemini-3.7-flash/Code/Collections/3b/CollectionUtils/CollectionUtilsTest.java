package org.apache.commons.collections;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class CollectionUtilsTest {

    private List listA;
    private List listB;

    @Before
    public void setUp() {
        listA = new ArrayList();
        listA.add("A");
        listA.add("B");
        listA.add("C");

        listB = new ArrayList();
        listB.add("B");
        listB.add("C");
        listB.add("D");
    }

    // Tests defect in removeAll: elements in remove collection should be removed, not retained
    @Test
    public void testRemoveAll_validCollections_returnsElementsNotInRemove() {
        Collection result = CollectionUtils.removeAll(listA, listB);
        assertEquals(1, result.size());
        assertTrue(result.contains("A"));
        assertFalse(result.contains("B"));
        assertFalse(result.contains("C"));
    }

    // Tests retainAll normal case
    @Test
    public void testRetainAll_validCollections_returnsMatchingElements() {
        Collection result = CollectionUtils.retainAll(listA, listB);
        assertEquals(2, result.size());
        assertTrue(result.contains("B"));
        assertTrue(result.contains("C"));
        assertFalse(result.contains("A"));
    }

    // Tests union of two collections
    @Test
    public void testUnion_twoCollections_returnsMaxCardinalityUnion() {
        List a = Arrays.asList(new Object[]{"A", "A", "B"});
        List b = Arrays.asList(new Object[]{"A", "B", "B"});
        Collection result = CollectionUtils.union(a, b);
        assertEquals(4, result.size());
        assertEquals(2, CollectionUtils.cardinality("A", result));
        assertEquals(2, CollectionUtils.cardinality("B", result));
    }

    // Tests intersection of two collections
    @Test
    public void testIntersection_twoCollections_returnsMinCardinalityIntersection() {
        List a = Arrays.asList(new Object[]{"A", "A", "B"});
        List b = Arrays.asList(new Object[]{"A", "B", "B"});
        Collection result = CollectionUtils.intersection(a, b);
        assertEquals(2, result.size());
        assertEquals(1, CollectionUtils.cardinality("A", result));
        assertEquals(1, CollectionUtils.cardinality("B", result));
    }

    // Tests disjunction (symmetric difference) of two collections
    @Test
    public void testDisjunction_twoCollections_returnsSymmetricDifference() {
        List a = Arrays.asList(new Object[]{"A", "A", "B"});
        List b = Arrays.asList(new Object[]{"A", "B", "B"});
        Collection result = CollectionUtils.disjunction(a, b);
        assertEquals(2, result.size());
        assertEquals(1, CollectionUtils.cardinality("A", result));
        assertEquals(1, CollectionUtils.cardinality("B", result));
    }

    // Tests subtract method
    @Test
    public void testSubtract_twoCollections_returnsDifference() {
        List a = Arrays.asList(new Object[]{"A", "A", "B", "C"});
        List b = Arrays.asList(new Object[]{"A", "B"});
        Collection result = CollectionUtils.subtract(a, b);
        assertEquals(2, result.size());
        assertEquals(1, CollectionUtils.cardinality("A", result));
        assertEquals(1, CollectionUtils.cardinality("C", result));
    }

    // Tests containsAny for true and false branches
    @Test
    public void testContainsAny_overlappingAndDisjointCollections_returnsExpectedResult() {
        List disjoint = Arrays.asList(new Object[]{"X", "Y"});
        assertTrue(CollectionUtils.containsAny(listA, listB));
        assertFalse(CollectionUtils.containsAny(listA, disjoint));
    }

    // Tests isSubCollection and isProperSubCollection
    @Test
    public void testIsSubCollectionAndIsProperSubCollection_subsets_returnsExpectedResults() {
        List sub = Arrays.asList(new Object[]{"A", "B"});
        List exact = Arrays.asList(new Object[]{"A", "B", "C"});

        assertTrue(CollectionUtils.isSubCollection(sub, listA));
        assertTrue(CollectionUtils.isProperSubCollection(sub, listA));
        assertTrue(CollectionUtils.isSubCollection(exact, listA));
        assertFalse(CollectionUtils.isProperSubCollection(exact, listA));
        assertFalse(CollectionUtils.isSubCollection(listA, sub));
    }

    // Tests isEqualCollection with same and different cardinalities
    @Test
    public void testIsEqualCollection_equalAndDifferentCardinalities_returnsExpectedResults() {
        List a = Arrays.asList(new Object[]{"A", "B", "B"});
        List b = Arrays.asList(new Object[]{"B", "A", "B"});
        List c = Arrays.asList(new Object[]{"A", "A", "B"});

        assertTrue(CollectionUtils.isEqualCollection(a, b));
        assertFalse(CollectionUtils.isEqualCollection(a, c));
        assertFalse(CollectionUtils.isEqualCollection(a, listA));
    }

    // Tests cardinality for Set, Bag/List, and null elements
    @Test
    public void testCardinality_variousCollectionsAndNull_returnsAccurateCount() {
        List list = Arrays.asList(new Object[]{"A", "B", "A", null});
        Set set = new HashSet(list);

        assertEquals(2, CollectionUtils.cardinality("A", list));
        assertEquals(1, CollectionUtils.cardinality("B", list));
        assertEquals(1, CollectionUtils.cardinality(null, list));
        assertEquals(0, CollectionUtils.cardinality("Z", list));

        assertEquals(1, CollectionUtils.cardinality("A", set));
        assertEquals(0, CollectionUtils.cardinality("Z", set));
    }

    // Tests find, countMatches, and exists with predicate
    @Test
    public void testFindCountMatchesAndExists_predicates_returnsExpectedValues() {
        Predicate predicate = new Predicate() {
            public boolean evaluate(Object object) {
                return "B".equals(object);
            }
        };

        assertEquals("B", CollectionUtils.find(listA, predicate));
        assertEquals(1, CollectionUtils.countMatches(listA, predicate));
        assertTrue(CollectionUtils.exists(listA, predicate));

        Predicate falsePredicate = new Predicate() {
            public boolean evaluate(Object object) {
                return "Z".equals(object);
            }
        };

        assertNull(CollectionUtils.find(listA, falsePredicate));
        assertEquals(0, CollectionUtils.countMatches(listA, falsePredicate));
        assertFalse(CollectionUtils.exists(listA, falsePredicate));
    }

    // Tests filter method
    @Test
    public void testFilter_predicateFilter_removesUnmatchedElements() {
        List list = new ArrayList(Arrays.asList(new Object[]{"A", "B", "C"}));
        Predicate predicate = new Predicate() {
            public boolean evaluate(Object object) {
                return !"B".equals(object);
            }
        };

        CollectionUtils.filter(list, predicate);
        assertEquals(2, list.size());
        assertTrue(list.contains("A"));
        assertTrue(list.contains("C"));
        assertFalse(list.contains("B"));
    }

    // Tests transform and collect methods
    @Test
    public void testTransformAndCollect_transformer_transformsElementsCorrectly() {
        Transformer transformer = new Transformer() {
            public Object transform(Object input) {
                return input + "_transformed";
            }
        };

        Collection collected = CollectionUtils.collect(listA, transformer);
        assertEquals(3, collected.size());
        assertTrue(collected.contains("A_transformed"));

        List listToTransform = new ArrayList(listA);
        CollectionUtils.transform(listToTransform, transformer);
        assertEquals("A_transformed", listToTransform.get(0));
        assertEquals("B_transformed", listToTransform.get(1));
        assertEquals("C_transformed", listToTransform.get(2));
    }

    // Tests select and selectRejected
    @Test
    public void testSelectAndSelectRejected_predicate_filtersIntoMatchingAndNonMatching() {
        Predicate predicate = new Predicate() {
            public boolean evaluate(Object object) {
                return "A".equals(object);
            }
        };

        Collection selected = CollectionUtils.select(listA, predicate);
        Collection rejected = CollectionUtils.selectRejected(listA, predicate);

        assertEquals(1, selected.size());
        assertTrue(selected.contains("A"));

        assertEquals(2, rejected.size());
        assertTrue(rejected.contains("B"));
        assertTrue(rejected.contains("C"));
    }

    // Tests addIgnoreNull and addAll
    @Test
    public void testAddIgnoreNullAndAddAll_variousInputs_modifiesCollectionAsExpected() {
        List list = new ArrayList();
        assertFalse(CollectionUtils.addIgnoreNull(list, null));
        assertTrue(CollectionUtils.addIgnoreNull(list, "Item"));
        assertEquals(1, list.size());

        CollectionUtils.addAll(list, new Object[]{"Array1", "Array2"});
        assertEquals(3, list.size());

        Vector vector = new Vector();
        vector.add("Enum1");
        CollectionUtils.addAll(list, vector.elements());
        assertEquals(4, list.size());

        CollectionUtils.addAll(list, Arrays.asList(new Object[]{"Iter1"}).iterator());
        assertEquals(5, list.size());
    }

    // Tests get across various supported types: List, Array, Map, Iterator, Enumeration
    @Test
    public void testGet_supportedObjectTypes_returnsElementAtIndex() {
        assertEquals("B", CollectionUtils.get(listA, 1));
        assertEquals("two", CollectionUtils.get(new String[]{"one", "two", "three"}, 1));

        Map map = new HashMap();
        map.put("key1", "val1");
        Object mapEntry = CollectionUtils.get(map, 0);
        assertTrue(mapEntry instanceof Map.Entry);

        Vector vec = new Vector();
        vec.add("first");
        vec.add("second");
        assertEquals("second", CollectionUtils.get(vec.elements(), 1));
        assertEquals("second", CollectionUtils.get(vec.iterator(), 1));
    }

    // Tests get with negative index exception path
    @Test(expected = IndexOutOfBoundsException.class)
    public void testGet_negativeIndex_throwsIndexOutOfBoundsException() {
        CollectionUtils.get(listA, -1);
    }

    // Tests get with unsupported object type exception path
    @Test(expected = IllegalArgumentException.class)
    public void testGet_unsupportedObjectType_throwsIllegalArgumentException() {
        CollectionUtils.get(new Integer(123), 0);
    }

    // Tests size and sizeIsEmpty across different types
    @Test
    public void testSizeAndSizeIsEmpty_supportedTypes_returnsCorrectSizeAndState() {
        assertEquals(3, CollectionUtils.size(listA));
        assertFalse(CollectionUtils.sizeIsEmpty(listA));

        assertEquals(0, CollectionUtils.size(Collections.EMPTY_LIST));
        assertTrue(CollectionUtils.sizeIsEmpty(Collections.EMPTY_LIST));

        Map map = new HashMap();
        map.put("k", "v");
        assertEquals(1, CollectionUtils.size(map));
        assertFalse(CollectionUtils.sizeIsEmpty(map));

        assertEquals(2, CollectionUtils.size(new int[]{1, 2}));
        assertFalse(CollectionUtils.sizeIsEmpty(new int[]{1, 2}));
        assertTrue(CollectionUtils.sizeIsEmpty(new int[0]));
    }

    // Tests isEmpty and isNotEmpty null safety
    @Test
    public void testIsEmptyAndIsNotEmpty_nullEmptyAndPopulatedCollections_returnsAccurateStatus() {
        assertTrue(CollectionUtils.isEmpty(null));
        assertFalse(CollectionUtils.isNotEmpty(null));

        assertTrue(CollectionUtils.isEmpty(Collections.EMPTY_LIST));
        assertFalse(CollectionUtils.isNotEmpty(Collections.EMPTY_LIST));

        assertFalse(CollectionUtils.isEmpty(listA));
        assertTrue(CollectionUtils.isNotEmpty(listA));
    }

    // Tests reverseArray
    @Test
    public void testReverseArray_validArray_reversesInPlace() {
        Object[] array = new Object[]{"A", "B", "C"};
        CollectionUtils.reverseArray(array);
        assertEquals("C", array[0]);
        assertEquals("B", array[1]);
        assertEquals("A", array[2]);
    }

    // Additional coverage tests

    @Test
    public void testConstructor_instantiation_canBeInstantiated() {
        new CollectionUtils();
    }

    @Test
    public void testEmptyCollection_constant_isNotEmptyCheck() {
        assertNotNull(CollectionUtils.EMPTY_COLLECTION);
        assertTrue(CollectionUtils.EMPTY_COLLECTION.isEmpty());
    }

    @Test
    public void testGetCardinalityMap_collection_returnsAccurateCounts() {
        List list = Arrays.asList(new Object[]{"A", "A", "B", "C"});
        Map cardMap = CollectionUtils.getCardinalityMap(list);
        assertEquals(new Integer(2), cardMap.get("A"));
        assertEquals(new Integer(1), cardMap.get("B"));
        assertEquals(new Integer(1), cardMap.get("C"));
    }

    @Test
    public void testForAllDo_closure_executesOnAllElements() {
        final List result = new ArrayList();
        Closure closure = new Closure() {
            public void execute(Object input) {
                result.add(input);
            }
        };

        CollectionUtils.forAllDo(listA, closure);
        assertEquals(3, result.size());
        assertEquals("A", result.get(0));

        CollectionUtils.forAllDo((Collection) null, closure);
        CollectionUtils.forAllDo(listA, (Closure) null);
    }

    @Test
    public void testSelectAndSelectRejected_withOutputCollection_populatesOutputCollection() {
        Predicate predicate = new Predicate() {
            public boolean evaluate(Object object) {
                return "A".equals(object);
            }
        };

        List outputSelect = new ArrayList();
        Collection resSelect = CollectionUtils.select(listA, predicate, outputSelect);
        assertSame(outputSelect, resSelect);
        assertEquals(1, outputSelect.size());
        assertEquals("A", outputSelect.get(0));

        List outputReject = new ArrayList();
        Collection resReject = CollectionUtils.selectRejected(listA, predicate, outputReject);
        assertSame(outputReject, resReject);
        assertEquals(2, outputReject.size());
        assertEquals("B", outputReject.get(0));
        assertEquals("C", outputReject.get(1));
    }

    @Test
    public void testCollect_withIteratorAndOutputCollection_transformsElements() {
        Transformer transformer = new Transformer() {
            public Object transform(Object input) {
                return input + "!";
            }
        };

        Collection fromIter = CollectionUtils.collect(listA.iterator(), transformer);
        assertEquals(3, fromIter.size());

        List outList = new ArrayList();
        Collection resList = CollectionUtils.collect(listA, transformer, outList);
        assertSame(outList, resList);
        assertEquals(3, outList.size());

        List outIter = new ArrayList();
        Collection resIter = CollectionUtils.collect(listA.iterator(), transformer, outIter);
        assertSame(outIter, resIter);
        assertEquals(3, outIter.size());

        CollectionUtils.collect((Collection) null, transformer, new ArrayList());
        CollectionUtils.collect((Iterator) null, transformer, new ArrayList());
    }

    @Test
    public void testFilterAndTransform_nullArguments_handlesGracefully() {
        CollectionUtils.filter(null, null);
        CollectionUtils.filter(listA, null);
        assertEquals(3, listA.size());

        CollectionUtils.transform(null, null);
        CollectionUtils.transform(listA, null);
        assertEquals("A", listA.get(0));
    }

    @Test
    public void testIsFullAndMaxSize_variousCollections_returnsExpected() {
        assertFalse(CollectionUtils.isFull(listA));
        assertEquals(-1, CollectionUtils.maxSize(listA));

        try {
            CollectionUtils.isFull(null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            CollectionUtils.maxSize(null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testDecorators_variousDecorators_returnDecoratedCollections() {
        Predicate predicate = new Predicate() {
            public boolean evaluate(Object object) {
                return true;
            }
        };
        Transformer transformer = new Transformer() {
            public Object transform(Object input) {
                return input;
            }
        };

        assertNotNull(CollectionUtils.synchronizedCollection(listA));
        assertNotNull(CollectionUtils.unmodifiableCollection(listA));
        assertNotNull(CollectionUtils.predicatedCollection(listA, predicate));
        assertNotNull(CollectionUtils.typedCollection(listA, String.class));
        assertNotNull(CollectionUtils.transformedCollection(listA, transformer));
    }

    @Test
    public void testCollate_twoCollections_returnsSortedMergedCollection() {
        List a = Arrays.asList(new Object[]{"A", "C", "E"});
        List b = Arrays.asList(new Object[]{"B", "C", "D"});

        List collated = CollectionUtils.collate(a, b);
        assertEquals(6, collated.size());
        assertEquals(Arrays.asList(new Object[]{"A", "B", "C", "C", "D", "E"}), collated);

        List collatedNoDup = CollectionUtils.collate(a, b, false);
        assertEquals(5, collatedNoDup.size());
        assertEquals(Arrays.asList(new Object[]{"A", "B", "C", "D", "E"}), collatedNoDup);
    }

    @Test
    public void testGet_variousEdgeCasesAndOutBounds_handlesCorrectly() {
        Set set = new HashSet();
        set.add("onlyElement");
        assertEquals("onlyElement", CollectionUtils.get(set, 0));

        Vector vec = new Vector();
        vec.add("X");
        try {
            CollectionUtils.get(vec.elements(), 5);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }

        try {
            CollectionUtils.get(vec.iterator(), 5);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }

        try {
            CollectionUtils.get((Object) null, 0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testSizeAndSizeIsEmpty_nullIteratorAndEnumeration_handlesCorrectly() {
        assertEquals(0, CollectionUtils.size(null));
        assertTrue(CollectionUtils.sizeIsEmpty(null));

        Vector vec = new Vector();
        vec.add("A");
        vec.add("B");

        assertEquals(2, CollectionUtils.size(vec.iterator()));
        assertEquals(2, CollectionUtils.size(vec.elements()));

        assertTrue(CollectionUtils.sizeIsEmpty(new ArrayList().iterator()));
        assertFalse(CollectionUtils.sizeIsEmpty(vec.iterator()));

        assertTrue(CollectionUtils.sizeIsEmpty(new Vector().elements()));
        assertFalse(CollectionUtils.sizeIsEmpty(vec.elements()));
    }

    @Test
    public void testReverseArray_nullAndEmptyArray_handlesGracefully() {
        CollectionUtils.reverseArray(null);
        Object[] empty = new Object[0];
        CollectionUtils.reverseArray(empty);
        assertEquals(0, empty.length);
    }
}