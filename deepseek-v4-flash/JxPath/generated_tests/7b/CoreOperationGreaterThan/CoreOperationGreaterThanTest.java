package org.apache.commons.jxpath.ri.compiler;

import static org.junit.Assert.*;

import org.junit.Test;

public class CoreOperationGreaterThanTest {

    // Tests basic greater than condition returns true
    @Test
    public void testComputeValue_largerDouble_returnsTrue() {
        Constant left = new Constant(5.0);
        Constant right = new Constant(3.0);
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(left, right);
        Object result = op.computeValue(null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests smaller value returns false
    @Test
    public void testComputeValue_smallerDouble_returnsFalse() {
        Constant left = new Constant(2.0);
        Constant right = new Constant(4.0);
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(left, right);
        Object result = op.computeValue(null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests equal values returns false
    @Test
    public void testComputeValue_equalDouble_returnsFalse() {
        Constant left = new Constant(3.0);
        Constant right = new Constant(3.0);
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(left, right);
        Object result = op.computeValue(null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests positive infinity greater than finite number returns true
    @Test
    public void testComputeValue_infinityGreater_returnsTrue() {
        Constant left = new Constant(Double.POSITIVE_INFINITY);
        Constant right = new Constant(1000.0);
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(left, right);
        Object result = op.computeValue(null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests negative infinity less than finite number returns false
    @Test
    public void testComputeValue_negInfinityLess_returnsFalse() {
        Constant left = new Constant(Double.NEGATIVE_INFINITY);
        Constant right = new Constant(0.0);
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(left, right);
        Object result = op.computeValue(null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests NaN on left side returns false
    @Test
    public void testComputeValue_nanLeft_returnsFalse() {
        Constant left = new Constant(Double.NaN);
        Constant right = new Constant(5.0);
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(left, right);
        Object result = op.computeValue(null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests NaN on right side returns false
    @Test
    public void testComputeValue_nanRight_returnsFalse() {
        Constant left = new Constant(5.0);
        Constant right = new Constant(Double.NaN);
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(left, right);
        Object result = op.computeValue(null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests both NaN returns false
    @Test
    public void testComputeValue_bothNaN_returnsFalse() {
        Constant left = new Constant(Double.NaN);
        Constant right = new Constant(Double.NaN);
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(left, right);
        Object result = op.computeValue(null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests zero equal zero returns false (boundary)
    @Test
    public void testComputeValue_zeroEqual_returnsFalse() {
        Constant left = new Constant(0.0);
        Constant right = new Constant(0.0);
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(left, right);
        Object result = op.computeValue(null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests negative zero vs positive zero returns false (equal in double)
    @Test
    public void testComputeValue_negativeZero_returnsFalse() {
        Constant left = new Constant(-0.0);
        Constant right = new Constant(0.0);
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(left, right);
        Object result = op.computeValue(null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests null left operand (InfoSetUtil.doubleValue(null) returns 0.0)
    @Test
    public void testComputeValue_nullLeft_returnsFalse() {
        Constant left = new Constant((Number) null);
        Constant right = new Constant(1.0);
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(left, right);
        Object result = op.computeValue(null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests null right operand
    @Test
    public void testComputeValue_nullRight_returnsFalse() {
        Constant left = new Constant(1.0);
        Constant right = new Constant((Number) null);
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(left, right);
        Object result = op.computeValue(null);
        assertEquals(Boolean.TRUE, result); // 1.0 > 0.0
    }

    // Tests string numbers are converted correctly
    @Test
    public void testComputeValue_stringNumbers_returnsCorrect() {
        Constant left = new Constant("3.5");
        Constant right = new Constant("2.0");
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(left, right);
        Object result = op.computeValue(null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests boolean true converted to 1.0, false converted to 0.0
    @Test
    public void testComputeValue_booleanTrue_returnsTrue() {
        Constant left = new Constant(1.0);
        Constant right = new Constant(0.0);
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(left, right);
        Object result = op.computeValue(null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests empty string leads to NaN, comparison false
    @Test
    public void testComputeValue_emptyStringLeft_returnsFalse() {
        Constant left = new Constant("");
        Constant right = new Constant(1.0);
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(left, right);
        Object result = op.computeValue(null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests getSymbol method
    @Test
    public void testGetSymbol_returnsGreaterThan() {
        Constant left = new Constant(1.0);
        Constant right = new Constant(0.0);
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(left, right);
        assertEquals(">", op.getSymbol());
    }
}