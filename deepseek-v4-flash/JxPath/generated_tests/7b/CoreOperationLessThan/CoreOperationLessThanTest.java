package org.apache.commons.jxpath.ri.compiler;

import org.junit.Test;
import static org.junit.Assert.*;

public class CoreOperationLessThanTest {

    // Tests basic less than: 1 < 2 -> true
    @Test
    public void testComputeValue_lessThan_returnsTrue() {
        CoreOperationLessThan op = new CoreOperationLessThan(new Constant(1.0), new Constant(2.0));
        Boolean result = (Boolean) op.computeValue(null);
        assertTrue(result);
    }

    // Tests greater than: 2 < 1 -> false
    @Test
    public void testComputeValue_greater_returnsFalse() {
        CoreOperationLessThan op = new CoreOperationLessThan(new Constant(2.0), new Constant(1.0));
        Boolean result = (Boolean) op.computeValue(null);
        assertFalse(result);
    }

    // Tests equal values: 1 < 1 -> false
    @Test
    public void testComputeValue_equal_returnsFalse() {
        CoreOperationLessThan op = new CoreOperationLessThan(new Constant(1.0), new Constant(1.0));
        Boolean result = (Boolean) op.computeValue(null);
        assertFalse(result);
    }

    // Tests negative less than positive: -1 < 2 -> true
    @Test
    public void testComputeValue_negativeLessThanPositive_returnsTrue() {
        CoreOperationLessThan op = new CoreOperationLessThan(new Constant(-1.0), new Constant(2.0));
        Boolean result = (Boolean) op.computeValue(null);
        assertTrue(result);
    }

    // Tests negative less than negative: -3 < -2 -> true
    @Test
    public void testComputeValue_negativeLessThanNegative_returnsTrue() {
        CoreOperationLessThan op = new CoreOperationLessThan(new Constant(-3.0), new Constant(-2.0));
        Boolean result = (Boolean) op.computeValue(null);
        assertTrue(result);
    }

    // Tests zero less than positive
    @Test
    public void testComputeValue_zeroLessThanPositive_returnsTrue() {
        CoreOperationLessThan op = new CoreOperationLessThan(new Constant(0.0), new Constant(1.0));
        Boolean result = (Boolean) op.computeValue(null);
        assertTrue(result);
    }

    // Tests negative less than zero
    @Test
    public void testComputeValue_negativeLessThanZero_returnsTrue() {
        CoreOperationLessThan op = new CoreOperationLessThan(new Constant(-1.0), new Constant(0.0));
        Boolean result = (Boolean) op.computeValue(null);
        assertTrue(result);
    }

    // Tests positive less than negative: false branch
    @Test
    public void testComputeValue_positiveLessThanNegative_returnsFalse() {
        CoreOperationLessThan op = new CoreOperationLessThan(new Constant(1.0), new Constant(-1.0));
        Boolean result = (Boolean) op.computeValue(null);
        assertFalse(result);
    }

    // Tests positive infinity less than positive infinity: false
    @Test
    public void testComputeValue_infinityLessThanInfinity_returnsFalse() {
        CoreOperationLessThan op = new CoreOperationLessThan(
                new Constant(Double.POSITIVE_INFINITY),
                new Constant(Double.POSITIVE_INFINITY));
        Boolean result = (Boolean) op.computeValue(null);
        assertFalse(result);
    }

    // Tests negative infinity less than positive infinity: true
    @Test
    public void testComputeValue_negInfinityLessThanPosInfinity_returnsTrue() {
        CoreOperationLessThan op = new CoreOperationLessThan(
                new Constant(Double.NEGATIVE_INFINITY),
                new Constant(Double.POSITIVE_INFINITY));
        Boolean result = (Boolean) op.computeValue(null);
        assertTrue(result);
    }

    // Tests positive infinity less than negative infinity: false
    @Test
    public void testComputeValue_posInfinityLessThanNegInfinity_returnsFalse() {
        CoreOperationLessThan op = new CoreOperationLessThan(
                new Constant(Double.POSITIVE_INFINITY),
                new Constant(Double.NEGATIVE_INFINITY));
        Boolean result = (Boolean) op.computeValue(null);
        assertFalse(result);
    }

    // Tests NaN less than a number: false (any NaN comparison returns false)
    @Test
    public void testComputeValue_NaNLessThanValue_returnsFalse() {
        CoreOperationLessThan op = new CoreOperationLessThan(
                new Constant(Double.NaN),
                new Constant(1.0));
        Boolean result = (Boolean) op.computeValue(null);
        assertFalse(result);
    }

    // Tests number less than NaN: false
    @Test
    public void testComputeValue_valueLessThanNaN_returnsFalse() {
        CoreOperationLessThan op = new CoreOperationLessThan(
                new Constant(1.0),
                new Constant(Double.NaN));
        Boolean result = (Boolean) op.computeValue(null);
        assertFalse(result);
    }

    // Tests MAX_VALUE < MIN_VALUE (false)
    @Test
    public void testComputeValue_maxDoubleLessThanMinDouble_returnsFalse() {
        CoreOperationLessThan op = new CoreOperationLessThan(
                new Constant(Double.MAX_VALUE),
                new Constant(Double.MIN_VALUE));
        Boolean result = (Boolean) op.computeValue(null);
        assertFalse(result);
    }

    // Tests MIN_VALUE < MAX_VALUE (true)
    @Test
    public void testComputeValue_minDoubleLessThanMaxDouble_returnsTrue() {
        CoreOperationLessThan op = new CoreOperationLessThan(
                new Constant(Double.MIN_VALUE),
                new Constant(Double.MAX_VALUE));
        Boolean result = (Boolean) op.computeValue(null);
        assertTrue(result);
    }

    // Tests getSymbol returns "<"
    @Test
    public void testGetSymbol_returnsLessThan() {
        CoreOperationLessThan op = new CoreOperationLessThan(new Constant(0.0), new Constant(1.0));
        assertEquals("<", op.getSymbol());
    }

    // Tests with integer constants via autoboxing
    @Test
    public void testComputeValue_integerLessThan_returnsTrue() {
        CoreOperationLessThan op = new CoreOperationLessThan(new Constant(5), new Constant(10));
        Boolean result = (Boolean) op.computeValue(null);
        assertTrue(result);
    }

    // New tests to cover uncovered mixed-type and non-Double paths

    @Test
    public void testComputeValue_doubleLessThanStringNumber_returnsTrue() {
        CoreOperationLessThan op = new CoreOperationLessThan(
                new Constant(2.0),
                new Constant("5"));
        Boolean result = (Boolean) op.computeValue(null);
        assertTrue(result);
    }

    @Test
    public void testComputeValue_stringNumberLessThanDouble_returnsTrue() {
        CoreOperationLessThan op = new CoreOperationLessThan(
                new Constant("5"),
                new Constant(10.0));
        Boolean result = (Boolean) op.computeValue(null);
        assertTrue(result);
    }

    @Test
    public void testComputeValue_doubleGreaterThanStringNumber_returnsFalse() {
        CoreOperationLessThan op = new CoreOperationLessThan(
                new Constant(10.0),
                new Constant("5"));
        Boolean result = (Boolean) op.computeValue(null);
        assertFalse(result);
    }

    @Test
    public void testComputeValue_stringLessThanString_returnsTrue() {
        CoreOperationLessThan op = new CoreOperationLessThan(
                new Constant("apple"),
                new Constant("banana"));
        Boolean result = (Boolean) op.computeValue(null);
        assertTrue(result);
    }

    @Test
    public void testComputeValue_stringGreaterThanString_returnsFalse() {
        CoreOperationLessThan op = new CoreOperationLessThan(
                new Constant("banana"),
                new Constant("apple"));
        Boolean result = (Boolean) op.computeValue(null);
        assertFalse(result);
    }

    @Test
    public void testComputeValue_stringEqual_returnsFalse() {
        CoreOperationLessThan op = new CoreOperationLessThan(
                new Constant("abc"),
                new Constant("abc"));
        Boolean result = (Boolean) op.computeValue(null);
        assertFalse(result);
    }

    @Test
    public void testComputeValue_integerGreaterThanInteger_returnsFalse() {
        CoreOperationLessThan op = new CoreOperationLessThan(
                new Constant(10),
                new Constant(5));
        Boolean result = (Boolean) op.computeValue(null);
        assertFalse(result);
    }

    @Test
    public void testComputeValue_integerEqualInteger_returnsFalse() {
        CoreOperationLessThan op = new CoreOperationLessThan(
                new Constant(5),
                new Constant(5));
        Boolean result = (Boolean) op.computeValue(null);
        assertFalse(result);
    }

    @Test
    public void testComputeValue_integerLessThanDouble_returnsTrue() {
        CoreOperationLessThan op = new CoreOperationLessThan(
                new Constant(5),
                new Constant(10.5));
        Boolean result = (Boolean) op.computeValue(null);
        assertTrue(result);
    }

    @Test
    public void testComputeValue_doubleLessThanInteger_returnsTrue() {
        CoreOperationLessThan op = new CoreOperationLessThan(
                new Constant(1.5),
                new Constant(2));
        Boolean result = (Boolean) op.computeValue(null);
        assertTrue(result);
    }

    @Test
    public void testGetPrecedence_returnsFive() {
        CoreOperationLessThan op = new CoreOperationLessThan(new Constant(0.0), new Constant(1.0));
        assertEquals(5, op.getPrecedence());
    }
}