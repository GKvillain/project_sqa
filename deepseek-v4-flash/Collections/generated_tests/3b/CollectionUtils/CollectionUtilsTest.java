package org.apache.commons.collections;

import static org.junit.Assert.*;

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

import org.apache.commons.collections.bag.HashBag;
import org.junit.Test;

public class CollectionUtilsTest {

    // --- union ---
    @Test
    public void testUnion_normal_returnsUnion() {
        Collection<Integer> a = Arrays.asList(1, 2, 2, 3);
        Collection<Integer> b = Arrays.asList(2, 3, 4);
        Collection<Integer> result = CollectionUtils.union(a, b);
        // Expected: [1,2,2,3,4] order not guaranteed
        assertTrue(result.containsAll(Arrays.asList(1,2,2,3,4)));
        assertEquals(5, result.size());
    }

    @Test
    public void testUnion_emptyA_returnsB() {
        Collection<Integer> a = new ArrayList<>();
        Collection<Integer> b = Arrays.asList(1,2);
        Collection<Integer> result = CollectionUtils.union(a, b);
        assertTrue(result.containsAll(b));
        assertEquals(2, result.size());
    }

    // --- intersection ---
    @Test
    public void testIntersection_normal_returnsIntersection() {
        Collection<Integer> a = Arrays.asList(1, 2, 2, 3);
        Collection<Integer> b = Arrays.asList(2, 3, 4);
        Collection<Integer> result = CollectionUtils.intersection(a, b);
        // Expected: [2,3] (cardinality 1 each)
        assertTrue(result.containsAll(Arrays.asList(2,3)));
        assertEquals(2, result.size());
    }

    // --- disjunction ---
    @Test
    public void testDisjunction_normal_returnsSymmetricDifference() {
        Collection<Integer> a = Arrays.asList(1, 2, 2, 3);
        Collection<Integer> b = Arrays.asList(2, 3, 4);
        Collection<Integer> result = CollectionUtils.disjunction(a, b);
        // Expected: [1,2,4] (2 appears once in a, not in b -> 1; 1 only in a; 4 only in b)
        assertTrue(result.containsAll(Arrays.asList(1,2,4)));
        assertEquals(3, result.size());
    }

    // --- subtract ---
    @Test
    public void testSubtract_normal_returnsDifference() {
        Collection<Integer> a = Arrays.asList(1, 2, 2, 3);
        Collection<Integer> b = Arrays.asList(2, 3);
        Collection<Integer> result = CollectionUtils.subtract(a, b);
        // Expected: [1,2]
        assertTrue(result.containsAll(Arrays.asList(1,2)));
        assertEquals(2, result.size());
    }

    // --- containsAny ---
    @Test
    public void testContainsAny_noCommon_returnsFalse() {
        assertFalse(CollectionUtils.containsAny(Arrays.asList(1,2), Arrays.asList(3,4)));
    }

    @Test
    public void testContainsAny_hasCommon_returnsTrue() {
        assertTrue(CollectionUtils.containsAny(Arrays.asList(1,2), Arrays.asList(2,3)));
    }

    @Test
    public void testContainsAny_firstSmaller_returnsTrue() {
        // first smaller size branch
        assertTrue(CollectionUtils.containsAny(Arrays.asList(1), Arrays.asList(1,2,3)));
    }

    // --- getCardinalityMap ---
    @Test
    public void testGetCardinalityMap_normal_returnsMap() {
        Map<Object, Integer> map = CollectionUtils.getCardinalityMap(Arrays.asList("a","b","a","c"));
        assertEquals(3, map.size());
        assertEquals(2, (int) map.get("a"));
        assertEquals(1, (int) map.get("b"));
        assertEquals(1, (int) map.get("c"));
    }

    // --- cardinality ---
    @Test
    public void testCardinality_set_returnsZeroOrOne() {
        Set<String> set = new HashSet<>(Arrays.asList("x","y"));
        assertEquals(1, CollectionUtils.cardinality("x", set));
        assertEquals(0, CollectionUtils.cardinality("z", set));
    }

    @Test
    public void testCardinality_bag_returnsCount() {
        Bag bag = new HashBag();
        bag.add("x", 3);
        assertEquals(3, CollectionUtils.cardinality("x", bag));
    }

    @Test
    public void testCardinality_nullObjectInList_countsNull() {
        List<String> list = Arrays.asList("a", null, "b", null);
        assertEquals(2, CollectionUtils.cardinality(null, list));
    }

    @Test
    public void testCardinality_normalList_countsUsingEquals() {
        List<Integer> list = Arrays.asList(1,2,1,3);
        assertEquals(2, CollectionUtils.cardinality(1, list));
    }

    // --- isSubCollection ---
    @Test
    public void testIsSubCollection_true() {
        assertTrue(CollectionUtils.isSubCollection(Arrays.asList(1,2), Arrays.asList(1,2,3)));
    }

    @Test
    public void testIsSubCollection_false() {
        assertFalse(CollectionUtils.isSubCollection(Arrays.asList(1,2,2), Arrays.asList(1,2,3)));
    }

    // --- isProperSubCollection ---
    @Test
    public void testIsProperSubCollection_true() {
        assertTrue(CollectionUtils.isProperSubCollection(Arrays.asList(1), Arrays.asList(1,2)));
    }

    @Test
    public void testIsProperSubCollection_equalSize_false() {
        assertFalse(CollectionUtils.isProperSubCollection(Arrays.asList(1,2), Arrays.asList(1,2)));
    }

    // --- isEqualCollection ---
    @Test
    public void testIsEqualCollection_equal_returnsTrue() {
        assertTrue(CollectionUtils.isEqualCollection(Arrays.asList(1,2,1), Arrays.asList(1,1,2)));
    }

    @Test
    public void testIsEqualCollection_differentSize_returnsFalse() {
        assertFalse(CollectionUtils.isEqualCollection(Arrays.asList(1), Arrays.asList(1,2)));
    }

    @Test
    public void testIsEqualCollection_differentCardinality_returnsFalse() {
        assertFalse(CollectionUtils.isEqualCollection(Arrays.asList(1,1), Arrays.asList(1,2)));
    }

    // --- find ---
    @Test
    public void testFind_nullCollection_returnsNull() {
        assertNull(CollectionUtils.find(null, new Predicate() {
            public boolean evaluate(Object obj) { return true; }
        }));
    }

    @Test
    public void testFind_found_returnsItem() {
        Object found = CollectionUtils.find(Arrays.asList("a","b","c"), new Predicate() {
            public boolean evaluate(Object obj) { return "b".equals(obj); }
        });
        assertEquals("b", found);
    }

    // --- forAllDo ---
    @Test
    public void testForAllDo_nullCollection_noChange() {
        // should not throw
        CollectionUtils.forAllDo(null, new Closure() {
            public void execute(Object obj) { /* noop */ }
        });
    }

    // --- filter ---
    @Test
    public void testFilter_normal_removesNotMatching() {
        List<Integer> list = new ArrayList<>(Arrays.asList(1,2,3,4));
        CollectionUtils.filter(list, new Predicate() {
            public boolean evaluate(Object obj) { return ((Integer)obj) % 2 == 0; }
        });
        assertEquals(Arrays.asList(2,4), list);
    }

    // --- transform (List) ---
    @Test
    public void testTransform_list_transformsInPlace() {
        List<String> list = new ArrayList<>(Arrays.asList("a","b"));
        CollectionUtils.transform(list, new Transformer() {
            public Object transform(Object obj) { return ((String)obj).toUpperCase(); }
        });
        assertEquals(Arrays.asList("A","B"), list);
    }

    // --- transform (non-List) ---
    @Test
    public void testTransform_set_collectsAndClears() {
        Set<String> set = new HashSet<>(Arrays.asList("a","b"));
        CollectionUtils.transform(set, new Transformer() {
            public Object transform(Object obj) { return ((String)obj).toUpperCase(); }
        });
        assertTrue(set.containsAll(Arrays.asList("A","B")));
        assertEquals(2, set.size());
    }

    // --- countMatches ---
    @Test
    public void testCountMatches_nullCollection_returnsZero() {
        assertEquals(0, CollectionUtils.countMatches(null, new Predicate() {
            public boolean evaluate(Object obj) { return true; }
        }));
    }

    @Test
    public void testCountMatches_normal_returnsCount() {
        int count = CollectionUtils.countMatches(Arrays.asList(1,2,3,4), new Predicate() {
            public boolean evaluate(Object obj) { return ((Integer)obj) % 2 == 0; }
        });
        assertEquals(2, count);
    }

    // --- exists ---
    @Test
    public void testExists_true_returnsTrue() {
        assertTrue(CollectionUtils.exists(Arrays.asList(1,2,3), new Predicate() {
            public boolean evaluate(Object obj) { return (Integer)obj > 2; }
        }));
    }

    @Test
    public void testExists_false_returnsFalse() {
        assertFalse(CollectionUtils.exists(Arrays.asList(1,2), new Predicate() {
            public boolean evaluate(Object obj) { return (Integer)obj > 5; }
        }));
    }

    // --- select ---
    @Test
    public void testSelect_nullPredicate_returnsEmptyList() {
        Collection result = CollectionUtils.select(Arrays.asList(1,2), null);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testSelect_normal_returnsFiltered() {
        Collection result = CollectionUtils.select(Arrays.asList(1,2,3,4), new Predicate() {
            public boolean evaluate(Object obj) { return ((Integer)obj) % 2 == 0; }
        });
        assertEquals(Arrays.asList(2,4), result);
    }

    // --- selectRejected ---
    @Test
    public void testSelectRejected_nullPredicate_returnsEmptyList() {
        Collection result = CollectionUtils.selectRejected(Arrays.asList(1,2), null);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testSelectRejected_normal_returnsComplement() {
        Collection result = CollectionUtils.selectRejected(Arrays.asList(1,2,3,4), new Predicate() {
            public boolean evaluate(Object obj) { return ((Integer)obj) % 2 == 0; }
        });
        assertEquals(Arrays.asList(1,3), result);
    }

    // --- collect (Collection + Transformer) ---
    @Test
    public void testCollect_nullTransformer_returnsEmptyList() {
        Collection result = CollectionUtils.collect(Arrays.asList(1,2), null);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testCollect_normal_transforms() {
        Collection result = CollectionUtils.collect(Arrays.asList(1,2), new Transformer() {
            public Object transform(Object obj) { return ((Integer)obj) * 2; }
        });
        assertEquals(Arrays.asList(2,4), result);
    }

    // --- collect (Iterator + Transformer) ---
    // Fixed: changed method name to avoid duplicate
    @Test
    public void testCollect_nullTransformerFromIterator_returnsEmptyList() {
        Collection result = CollectionUtils.collect(Arrays.asList(1,2).iterator(), null);
        assertTrue(result.isEmpty());
    }

    // --- addIgnoreNull ---
    @Test
    public void testAddIgnoreNull_nullObject_returnsFalse() {
        List<String> list = new ArrayList<>();
        assertFalse(CollectionUtils.addIgnoreNull(list, null));
        assertTrue(list.isEmpty());
    }

    @Test
    public void testAddIgnoreNull_nonNull_returnsTrue() {
        List<String> list = new ArrayList<>();
        assertTrue(CollectionUtils.addIgnoreNull(list, "x"));
    }

    // --- addAll with Iterator ---
    @Test
    public void testAddAll_iterator_addsAll() {
        List<Integer> list = new ArrayList<>();
        CollectionUtils.addAll(list, Arrays.asList(1,2,3).iterator());
        assertEquals(Arrays.asList(1,2,3), list);
    }

    // --- addAll with Enumeration ---
    @Test
    public void testAddAll_enumeration_addsAll() {
        List<Integer> list = new ArrayList<>();
        Enumeration<Integer> enumeration = Collections.enumeration(Arrays.asList(1,2));
        CollectionUtils.addAll(list, enumeration);
        assertEquals(Arrays.asList(1,2), list);
    }

    // --- addAll with array ---
    @Test
    public void testAddAll_array_addsAll() {
        List<Integer> list = new ArrayList<>();
        CollectionUtils.addAll(list, new Integer[]{1,2});
        assertEquals(Arrays.asList(1,2), list);
    }

    // --- get ---
    @Test
    public void testGet_list_returnsElement() {
        assertEquals("b", CollectionUtils.get(Arrays.asList("a","b","c"), 1));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGet_negativeIndex_throwsException() {
        CollectionUtils.get(new ArrayList<>(), -1);
    }

    @Test
    public void testGet_map_returnsEntry() {
        Map<String,Integer> map = new HashMap<>();
        map.put("x",1);
        Object entry = CollectionUtils.get(map, 0);
        assertTrue(entry instanceof Map.Entry);
    }

    @Test
    public void testGet_array_returnsElement() {
        assertEquals(2, CollectionUtils.get(new Integer[]{1,2,3}, 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGet_null_throwsException() {
        CollectionUtils.get(null, 0);
    }

    // --- size ---
    @Test
    public void testSize_list_returnsSize() {
        assertEquals(3, CollectionUtils.size(Arrays.asList(1,2,3)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSize_null_throwsException() {
        CollectionUtils.size(null);
    }

    @Test
    public void testSize_iterator_returnsRemaining() {
        Iterator<Integer> it = Arrays.asList(1,2,3).iterator();
        it.next(); // consume one
        assertEquals(2, CollectionUtils.size(it));
    }

    // --- sizeIsEmpty ---
    @Test
    public void testSizeIsEmpty_null_throwsException() {
        try {
            CollectionUtils.sizeIsEmpty(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testSizeIsEmpty_emptyList_returnsTrue() {
        assertTrue(CollectionUtils.sizeIsEmpty(new ArrayList<>()));
    }

    @Test
    public void testSizeIsEmpty_nonEmptyList_returnsFalse() {
        assertFalse(CollectionUtils.sizeIsEmpty(Arrays.asList(1)));
    }

    // --- isEmpty ---
    @Test
    public void testIsEmpty_null_returnsTrue() {
        assertTrue(CollectionUtils.isEmpty(null));
    }

    @Test
    public void testIsEmpty_emptyCollection_returnsTrue() {
        assertTrue(CollectionUtils.isEmpty(new ArrayList<>()));
    }

    @Test
    public void testIsNotEmpty_nonEmpty_returnsTrue() {
        assertTrue(CollectionUtils.isNotEmpty(Arrays.asList(1)));
    }

    @Test
    public void testIsNotEmpty_null_returnsFalse() {
        assertFalse(CollectionUtils.isNotEmpty(null));
    }

    // --- reverseArray ---
    @Test
    public void testReverseArray_normal_reverses() {
        Integer[] array = {1,2,3};
        CollectionUtils.reverseArray(array);
        assertArrayEquals(new Integer[]{3,2,1}, array);
    }

    // --- isFull ---
    @Test(expected = NullPointerException.class)
    public void testIsFull_null_throwsException() {
        CollectionUtils.isFull(null);
    }

    @Test
    public void testIsFull_nonBounded_returnsFalse() {
        assertFalse(CollectionUtils.isFull(new ArrayList<>()));
    }

    // --- maxSize ---
    @Test(expected = NullPointerException.class)
    public void testMaxSize_null_throwsException() {
        CollectionUtils.maxSize(null);
    }

    @Test
    public void testMaxSize_nonBounded_returnsNegativeOne() {
        assertEquals(-1, CollectionUtils.maxSize(new ArrayList<>()));
    }

    // --- retainAll ---
    @Test
    public void testRetainAll_normal_retainsOnlyCommon() {
        Collection<Integer> result = CollectionUtils.retainAll(
                Arrays.asList(1,2,2,3),
                Arrays.asList(2,3,3));
        // Expected: [2,2,3] (cardinality min)
        assertTrue(result.containsAll(Arrays.asList(2,2,3)));
        assertEquals(3, result.size());
    }

    // --- removeAll (BUG DETECTION) ---
    @Test
    public void testRemoveAll_normal_removesElements() {
        Collection<String> result = CollectionUtils.removeAll(
                Arrays.asList("A","B","C"),
                Arrays.asList("B","C"));
        // Correct behavior: remove B and C, keep A
        // Buggy implementation (using retainAll) would keep only B and C.
        assertTrue("removeAll should not contain removed element", !result.contains("B"));
        assertTrue("removeAll should not contain removed element", !result.contains("C"));
        assertTrue("removeAll should contain kept element", result.contains("A"));
        assertEquals(1, result.size());
    }

    // ================ NEW TEST CASES FOR UNCOVERED METHODS ================

    // --- emptyCollection ---
    @Test
    public void testEmptyCollection_returnsEmptyAndUnmodifiable() {
        Collection<Object> empty = CollectionUtils.emptyCollection();
        assertTrue(empty.isEmpty());
        assertEquals(0, empty.size());
        // verify it is unmodifiable
        try {
            empty.add("x");
            fail("Should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    // --- emptyIfNull ---
    @Test
    public void testEmptyIfNull_withNull_returnsEmptyCollection() {
        Collection<Object> result = CollectionUtils.emptyIfNull(null);
        assertTrue(result.isEmpty());
        // returned collection should be unmodifiable
        try {
            result.add("x");
            fail("Should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void testEmptyIfNull_withNonNull_returnsSameCollection() {
        List<String> original = new ArrayList<>(Arrays.asList("a","b"));
        Collection<String> result = CollectionUtils.emptyIfNull(original);
        assertSame(original, result);
        assertEquals(2, result.size());
    }

    // --- synchronizedCollection ---
    @Test
    public void testSynchronizedCollection_returnsSynchronizedWrapper() {
        List<String> list = new ArrayList<>(Arrays.asList("x"));
        Collection<String> sync = CollectionUtils.synchronizedCollection(list);
        assertNotNull(sync);
        // basic operation
        assertEquals(1, sync.size());
        assertTrue(sync.contains("x"));
    }

    // --- unmodifiableCollection ---
    @Test
    public void testUnmodifiableCollection_returnsUnmodifiableWrapper() {
        List<String> list = new ArrayList<>(Arrays.asList("a","b"));
        Collection<String> unmod = CollectionUtils.unmodifiableCollection(list);
        assertEquals(2, unmod.size());
        try {
            unmod.add("c");
            fail("Should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    // --- predicatedCollection ---
    @Test
    public void testPredicatedCollection_validAdd_succeeds() {
        Predicate onlyStrings = new Predicate() {
            public boolean evaluate(Object obj) {
                return obj instanceof String;
            }
        };
        Collection<String> pred = CollectionUtils.predicatedCollection(new ArrayList<>(), onlyStrings);
        pred.add("hello");
        assertEquals(1, pred.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPredicatedCollection_invalidAdd_throwsException() {
        Predicate onlyStrings = new Predicate() {
            public boolean evaluate(Object obj) {
                return obj instanceof String;
            }
        };
        Collection<Integer> pred = CollectionUtils.predicatedCollection(new ArrayList<>(), onlyStrings);
        pred.add(123); // should throw
    }

    // --- transformedCollection ---
    @Test
    public void testTransformedCollection_addTransforms() {
        Transformer doubler = new Transformer() {
            public Object transform(Object obj) {
                return ((Integer)obj) * 2;
            }
        };
        Collection<Integer> trans = CollectionUtils.transformedCollection(new ArrayList<>(), doubler);
        trans.add(5);
        assertEquals(1, trans.size());
        assertTrue(trans.contains(10));
    }

    // --- extractSingleton ---
    @Test
    public void testExtractSingleton_success_returnsElement() {
        Collection<String> singleton = Arrays.asList("only");
        assertEquals("only", CollectionUtils.extractSingleton(singleton));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExtractSingleton_empty_throwsException() {
        CollectionUtils.extractSingleton(new ArrayList<>());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExtractSingleton_multiple_throwsException() {
        CollectionUtils.extractSingleton(Arrays.asList("a","b"));
    }

    // --- containsAll (CollectionUtils method) ---
    @Test
    public void testContainsAll_true_returnsTrue() {
        assertTrue(CollectionUtils.containsAll(
                Arrays.asList(1,2,2,3),
                Arrays.asList(2,3)));
    }

    @Test
    public void testContainsAll_false_returnsFalse() {
        // cardinality insufficient
        assertFalse(CollectionUtils.containsAll(
                Arrays.asList(1,2,3),
                Arrays.asList(1,2,2)));
    }

    // --- index ---
    @Test
    public void testIndex_found_returnsPosition() {
        int idx = CollectionUtils.index(Arrays.asList(10,20,30), new Predicate() {
            public boolean evaluate(Object obj) { return ((Integer)obj) == 20; }
        });
        assertEquals(1, idx);
    }

    @Test
    public void testIndex_notFound_returnsMinusOne() {
        int idx = CollectionUtils.index(Arrays.asList(10,20,30), new Predicate() {
            public boolean evaluate(Object obj) { return ((Integer)obj) == 99; }
        });
        assertEquals(-1, idx);
    }

    // --- filterInverse ---
    @Test
    public void testFilterInverse_removesMatchingElements() {
        List<Integer> list = new ArrayList<>(Arrays.asList(1,2,3,4,5));
        CollectionUtils.filterInverse(list, new Predicate() {
            public boolean evaluate(Object obj) { return ((Integer)obj) % 2 == 0; }
        });
        // keep elements that do NOT match predicate -> odd numbers
        assertEquals(Arrays.asList(1,3,5), list);
    }

    // --- collect with output collection (overloaded) ---
    @Test
    public void testCollect_withOutputCollection_addsToExisting() {
        Transformer doubler = new Transformer() {
            public Object transform(Object obj) { return ((Integer)obj) * 2; }
        };
        List<Integer> output = new ArrayList<>(Arrays.asList(100));
        CollectionUtils.collect(Arrays.asList(1,2,3), doubler, output);
        assertEquals(Arrays.asList(100, 2, 4, 6), output);
    }

    // --- select with output collection (overloaded) ---
    @Test
    public void testSelect_withOutputCollection_addsToExisting() {
        Predicate even = new Predicate() {
            public boolean evaluate(Object obj) { return ((Integer)obj) % 2 == 0; }
        };
        List<Integer> output = new ArrayList<>(Arrays.asList(100));
        CollectionUtils.select(Arrays.asList(1,2,3,4), even, output);
        assertEquals(Arrays.asList(100, 2, 4), output);
    }

    // --- selectRejected with output collection (overloaded) ---
    @Test
    public void testSelectRejected_withOutputCollection_addsToExisting() {
        Predicate even = new Predicate() {
            public boolean evaluate(Object obj) { return ((Integer)obj) % 2 == 0; }
        };
        List<Integer> output = new ArrayList<>(Arrays.asList(100));
        CollectionUtils.selectRejected(Arrays.asList(1,2,3,4), even, output);
        assertEquals(Arrays.asList(100, 1, 3), output);
    }

    // --- forAllDo with Iterator (overloaded) ---
    @Test
    public void testForAllDo_iterator_appliesClosure() {
        final StringBuilder sb = new StringBuilder();
        Closure appender = new Closure() {
            public void execute(Object obj) {
                sb.append(obj);
            }
        };
        CollectionUtils.forAllDo(Arrays.asList("a","b","c").iterator(), appender);
        assertEquals("abc", sb.toString());
    }

    // --- countMatches with Iterator (overloaded) ---
    @Test
    public void testCountMatches_iterator_returnsCount() {
        int count = CollectionUtils.countMatches(
                Arrays.asList(1,2,3,4,5).iterator(),
                new Predicate() {
                    public boolean evaluate(Object obj) { return ((Integer)obj) % 2 == 0; }
                });
        assertEquals(2, count);
    }

    // --- exists with Iterator (overloaded) ---
    @Test
    public void testExists_iterator_true_returnsTrue() {
        assertTrue(CollectionUtils.exists(
                Arrays.asList(1,2,3).iterator(),
                new Predicate() {
                    public boolean evaluate(Object obj) { return (Integer)obj > 2; }
                }));
    }

    @Test
    public void testExists_iterator_false_returnsFalse() {
        assertFalse(CollectionUtils.exists(
                Arrays.asList(1,2).iterator(),
                new Predicate() {
                    public boolean evaluate(Object obj) { return (Integer)obj > 5; }
                }));
    }
}