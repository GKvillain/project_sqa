package org.apache.commons.jxpath.ri.compiler;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

public class CoreOperationCompareTest {

    // CoreOperationCompare is abstract; CoreOperationEqual is an existing concrete subclass.
    private CoreOperationCompare operation() {
        return new CoreOperationEqual(new Constant("left"), new Constant("right"));
    }

    // Tests equal(Object, Object) when both arguments are null
    @Test
    public void testEqual_nullNull_returnsTrue() {
        assertTrue(operation().equal((Object) null, (Object) null));
    }

    // Tests equal(Object, Object) when left is null and right is not
    @Test
    public void testEqual_nullObject_returnsFalse() {
        assertFalse(operation().equal(null, new Object()));
    }

    // Tests reference equality path and the final Object.equals path
    @Test
    public void testEqual_objects_returnsReferenceOrEqualsResult() {
        CoreOperationCompare op = operation();
        Object same = new Object();
        assertTrue(op.equal(same, same));
        assertFalse(op.equal(new Object(), new Object()));
    }

    // Tests equal(Object, Object) with equal booleans
    @Test
    public void testEqual_booleanSame_returnsTrue() {
        assertTrue(operation().equal(Boolean.TRUE, Boolean.TRUE));
    }

    // Tests equal(Object, Object) with different booleans
    @Test
    public void testEqual_booleanDifferent_returnsFalse() {
        assertFalse(operation().equal(Boolean.TRUE, Boolean.FALSE));
    }

    // Tests numeric equality across different Number types
    @Test
    public void testEqual_numberSameValueDifferentTypes_returnsTrue() {
        assertTrue(operation().equal(Integer.valueOf(1), Double.valueOf(1.0)));
    }

    // Tests numeric inequality
    @Test
    public void testEqual_numberNotEqual_returnsFalse() {
        assertFalse(operation().equal(Double.valueOf(1.5), Double.valueOf(2.5)));
    }

    // Tests equal(Object, Object) with equal strings
    @Test
    public void testEqual_stringEqual_returnsTrue() {
        assertTrue(operation().equal("value", "value"));
    }

    // Tests equal(Object, Object) with different strings
    @Test
    public void testEqual_stringDifferent_returnsFalse() {
        assertFalse(operation().equal("value", "other"));
    }

    // Tests numeric comparison when one operand is a string and the other is a number
    @Test
    public void testEqual_stringAndNumber_returnsTrue() {
        assertTrue(operation().equal("1", Integer.valueOf(1)));
    }

    // Tests boolean comparison when one operand is a string
    @Test
    public void testEqual_stringAndBoolean_returnsBooleanComparison() {
        CoreOperationCompare op = operation();
        assertTrue(op.equal("false", Boolean.TRUE));
        assertTrue(op.equal("", Boolean.FALSE));
    }

    // Tests contains() with an empty iterator
    @Test
    public void testContains_emptyIterator_returnsFalse() {
        assertFalse(operation().contains(Collections.<String>emptyList().iterator(), "value"));
    }

    // Tests contains() when a matching element exists
    @Test
    public void testContains_matchingElement_returnsTrue() {
        assertTrue(operation().contains(Arrays.asList("a", "b").iterator(), "b"));
    }

    // Tests contains() when no matching element exists
    @Test
    public void testContains_noMatchingElement_returnsFalse() {
        assertFalse(operation().contains(Arrays.asList("a", "b").iterator(), "c"));
    }

    // Tests findMatch() when both iterators are empty
    @Test
    public void testFindMatch_bothEmpty_returnsFalse() {
        CoreOperationCompare op = operation();
        assertFalse(op.findMatch(Collections.<String>emptyList().iterator(), Collections.<String>emptyList().iterator()));
    }

    // Tests findMatch() when one iterator is empty
    @Test
    public void testFindMatch_oneEmpty_returnsFalse() {
        CoreOperationCompare op = operation();
        assertFalse(op.findMatch(Arrays.asList("a").iterator(), Collections.<String>emptyList().iterator()));
        assertFalse(op.findMatch(Collections.<String>emptyList().iterator(), Arrays.asList("a").iterator()));
    }

    // Tests findMatch() when a matching pair exists
    @Test
    public void testFindMatch_matchingElement_returnsTrue() {
        CoreOperationCompare op = operation();
        assertTrue(op.findMatch(Arrays.asList("a", "b", "c").iterator(), Arrays.asList("x", "b").iterator()));
    }

    // Tests findMatch() when no matching pair exists
    @Test
    public void testFindMatch_noMatchingElement_returnsFalse() {
        CoreOperationCompare op = operation();
        assertFalse(op.findMatch(Arrays.asList("a", "b").iterator(), Arrays.asList("c", "d").iterator()));
    }

    // Tests the 3-argument equal() with a number and a numeric string
    @Test
    public void testEqualEvaluation_numberString_returnsTrue() {
        assertTrue(operation().equal(null, new Constant(Double.valueOf(1.0)), new Constant("1")));
    }

    // Regression test: a NaN value must not equal itself under XPath numeric comparison
    @Test
    public void testEqualEvaluation_nanSameObject_returnsFalse() {
        Constant nan = new Constant(Double.valueOf(Double.NaN));
        assertFalse(operation().equal(null, nan, nan));
    }
}