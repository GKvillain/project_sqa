package org.apache.commons.jxpath.ri.compiler;

import static org.junit.Assert.*;
import org.junit.Test;
import java.util.*;

public class CoreOperationCompareTest {

    private CoreOperationEqual createEqualOp() {
        return new CoreOperationEqual(new Constant("dummy"), new Constant("dummy"));
    }

    // Tests equal(Object, Object) when both references are the same
    @Test
    public void testEqual_bothSameReference_returnsTrue() {
        CoreOperationEqual op = createEqualOp();
        Object obj = new Object();
        assertTrue(op.equal(obj, obj));
    }

    // Tests equal(Object, Object) when both are null (l == r)
    @Test
    public void testEqual_bothNull_returnsTrue() {
        CoreOperationEqual op = createEqualOp();
        assertTrue(op.equal(null, null));
    }

    // Tests equal(Object, Object) with Boolean true and true
    @Test
    public void testEqual_booleanSameValue_returnsTrue() {
        CoreOperationEqual op = createEqualOp();
        assertTrue(op.equal(Boolean.TRUE, Boolean.TRUE));
    }

    // Tests equal(Object, Object) with Boolean true and false
    @Test
    public void testEqual_booleanDifferent_returnsFalse() {
        CoreOperationEqual op = createEqualOp();
        assertFalse(op.equal(Boolean.TRUE, Boolean.FALSE));
    }

    // Tests equal(Object, Object) with same int numbers
    @Test
    public void testEqual_numberSameInt_returnsTrue() {
        CoreOperationEqual op = createEqualOp();
        assertTrue(op.equal(5, 5));
    }

    // Tests equal(Object, Object) with different int numbers
    @Test
    public void testEqual_numberDifferentInt_returnsFalse() {
        CoreOperationEqual op = createEqualOp();
        assertFalse(op.equal(5, 10));
    }

    // Tests equal(Object, Object) with NaN values (NaN != NaN)
    @Test
    public void testEqual_numberNaN_returnsFalse() {
        CoreOperationEqual op = createEqualOp();
        assertFalse(op.equal(Double.NaN, Double.NaN));
    }

    // Tests equal(Object, Object) with positive infinity
    @Test
    public void testEqual_numberPositiveInfinitySame_returnsTrue() {
        CoreOperationEqual op = createEqualOp();
        assertTrue(op.equal(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY));
    }

    // Tests equal(Object, Object) with same strings
    @Test
    public void testEqual_stringSame_returnsTrue() {
        CoreOperationEqual op = createEqualOp();
        assertTrue(op.equal("hello", "hello"));
    }

    // Tests equal(Object, Object) with different strings
    @Test
    public void testEqual_stringDifferent_returnsFalse() {
        CoreOperationEqual op = createEqualOp();
        assertFalse(op.equal("hello", "world"));
    }

    // Tests equal(Object, Object) mixing Boolean and Number – uses booleanValue path
    @Test
    public void testEqual_mixedBooleanAndNumber_booleanTrueNumberNonZero_returnsTrue() {
        CoreOperationEqual op = createEqualOp();
        assertTrue(op.equal(Boolean.TRUE, 1));
    }

    // Tests equal(Object, Object) mixing Boolean and Number – false case
    @Test
    public void testEqual_mixedBooleanAndNumber_booleanTrueNumberZero_returnsFalse() {
        CoreOperationEqual op = createEqualOp();
        assertFalse(op.equal(Boolean.TRUE, 0));
    }

    // Tests equal(Object, Object) with objects that are equal via equals() (non-Boolean/Number/String)
    @Test
    public void testEqual_objectEquals_returnsTrue() {
        CoreOperationEqual op = createEqualOp();
        List<String> list1 = new ArrayList<>();
        List<String> list2 = new ArrayList<>();
        assertTrue(op.equal(list1, list2));
    }

    // Tests equal(Object, Object) with objects that are not equal
    @Test
    public void testEqual_objectNotEquals_returnsFalse() {
        CoreOperationEqual op = createEqualOp();
        List<String> list1 = new ArrayList<>();
        List<String> list2 = new ArrayList<>();
        list2.add("a");
        assertFalse(op.equal(list1, list2));
    }

    // Tests equal(Object, Object) when one is null and other is non-null
    @Test
    public void testEqual_oneNullOneNonNull_returnsFalse() {
        CoreOperationEqual op = createEqualOp();
        assertFalse(op.equal(null, "something"));
        assertFalse(op.equal("something", null));
    }

    // Tests contains(Iterator, Object) when iterator contains the value
    @Test
    public void testContains_iteratorContainsValue_returnsTrue() {
        CoreOperationEqual op = createEqualOp();
        List<Integer> list = Arrays.asList(1, 2, 3);
        assertTrue(op.contains(list.iterator(), 2));
    }

    // Tests contains(Iterator, Object) when iterator does not contain the value
    @Test
    public void testContains_iteratorNotContains_returnsFalse() {
        CoreOperationEqual op = createEqualOp();
        List<Integer> list = Arrays.asList(1, 2, 3);
        assertFalse(op.contains(list.iterator(), 5));
    }

    // Tests findMatch(Iterator, Iterator) when there is a matching element
    @Test
    public void testFindMatch_bothIteratorsMatch_returnsTrue() {
        CoreOperationEqual op = createEqualOp();
        List<Integer> list1 = Arrays.asList(1, 2, 3);
        List<Integer> list2 = Arrays.asList(3, 4, 5);
        assertTrue(op.findMatch(list1.iterator(), list2.iterator()));
    }

    // Tests findMatch(Iterator, Iterator) when no matching element
    @Test
    public void testFindMatch_noMatch_returnsFalse() {
        CoreOperationEqual op = createEqualOp();
        List<Integer> list1 = Arrays.asList(1, 2, 3);
        List<Integer> list2 = Arrays.asList(4, 5, 6);
        assertFalse(op.findMatch(list1.iterator(), list2.iterator()));
    }

    // Tests findMatch(Iterator, Iterator) when first iterator is empty
    @Test
    public void testFindMatch_firstEmpty_returnsFalse() {
        CoreOperationEqual op = createEqualOp();
        List<Integer> empty = Collections.emptyList();
        List<Integer> list = Arrays.asList(1, 2);
        assertFalse(op.findMatch(empty.iterator(), list.iterator()));
    }

    // Tests equal(EvalContext, Expression, Expression) with left expression returning Collection,
    // right expression returning non-Iterator -> should use contains(leftIr, rightValue)
    @Test
    public void testEqual_contextLeftIteratorRightNonIterator_usesContains() {
        CoreOperationEqual op = createEqualOp();
        Constant leftConstant = new Constant(Arrays.asList(1, 2, 3));
        Constant rightConstant = new Constant(2);
        assertTrue(op.equal(null, leftConstant, rightConstant));

        Constant rightConstant2 = new Constant(5);
        assertFalse(op.equal(null, leftConstant, rightConstant2));
    }

    // Tests equal(EvalContext, Expression, Expression) with both expressions returning Collection
    @Test
    public void testEqual_contextBothIterators_usesFindMatch() {
        CoreOperationEqual op = createEqualOp();
        Constant leftConstant = new Constant(Arrays.asList(1, 2, 3));
        Constant rightConstant = new Constant(Arrays.asList(3, 4, 5));
        assertTrue(op.equal(null, leftConstant, rightConstant));

        Constant rightConstant2 = new Constant(Arrays.asList(4, 5, 6));
        assertFalse(op.equal(null, leftConstant, rightConstant2));
    }

    // Tests equal(EvalContext, Expression, Expression) with left expression returning Collection
    // Collection is converted to iterator, then contains is used
    @Test
    public void testEqual_contextLeftCollectionRightNonCollection_usesContains() {
        CoreOperationEqual op = createEqualOp();
        Constant leftConstant = new Constant(Arrays.asList(1, 2, 3));
        Constant rightConstant = new Constant(2);
        assertTrue(op.equal(null, leftConstant, rightConstant));
    }

    // Tests equal(EvalContext, Expression, Expression) with both expressions returning Collection
    @Test
    public void testEqual_contextBothCollections_usesFindMatch() {
        CoreOperationEqual op = createEqualOp();
        Constant leftConstant = new Constant(Arrays.asList(1, 2, 3));
        Constant rightConstant = new Constant(Arrays.asList(3, 4, 5));
        assertTrue(op.equal(null, leftConstant, rightConstant));
    }

    // Tests equal(EvalContext, Expression, Expression) with right expression returning Collection,
    // left being simple value -> uses contains(rightIr, leftValue)
    @Test
    public void testEqual_contextRightIteratorLeftNonIterator_usesContains() {
        CoreOperationEqual op = createEqualOp();
        Constant leftConstant = new Constant(2);
        Constant rightConstant = new Constant(Arrays.asList(1, 2, 3));
        assertTrue(op.equal(null, leftConstant, rightConstant));
    }

    // Tests equal(EvalContext, Expression, Expression) with both simple values (no Iterator/Collection)
    @Test
    public void testEqual_contextBothSimpleValuesEqual_returnsTrue() {
        CoreOperationEqual op = createEqualOp();
        assertTrue(op.equal(null, new Constant(42), new Constant(42)));
    }
}