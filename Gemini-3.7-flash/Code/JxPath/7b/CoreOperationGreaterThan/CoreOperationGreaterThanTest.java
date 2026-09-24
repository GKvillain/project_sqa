package org.apache.commons.jxpath.ri.compiler;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CoreOperationGreaterThanTest {

    // Tests that getSymbol returns the correct operator symbol
    @Test
    public void testGetSymbol_returnsGreaterThanSymbol() {
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(new Constant(2), new Constant(1));
        assertEquals(">", op.getSymbol());
    }

    // Tests greater than with integer constants where left > right (true branch)
    @Test
    public void testComputeValue_leftGreaterThanRight_returnsTrue() {
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(new Constant(5), new Constant(3));
        Object result = op.computeValue(null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests greater than with integer constants where left < right (false branch)
    @Test
    public void testComputeValue_leftLessThanRight_returnsFalse() {
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(new Constant(2), new Constant(7));
        Object result = op.computeValue(null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests boundary condition where left == right (false branch)
    @Test
    public void testComputeValue_leftEqualsRight_returnsFalse() {
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(new Constant(4), new Constant(4));
        Object result = op.computeValue(null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests greater than with negative numbers where left > right
    @Test
    public void testComputeValue_negativeNumbersLeftGreater_returnsTrue() {
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(new Constant(-1), new Constant(-5));
        Object result = op.computeValue(null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests greater than with negative numbers where left < right
    @Test
    public void testComputeValue_negativeNumbersLeftLess_returnsFalse() {
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(new Constant(-10), new Constant(-2));
        Object result = op.computeValue(null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests zero against negative value
    @Test
    public void testComputeValue_zeroGreaterThanNegative_returnsTrue() {
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(new Constant(0), new Constant(-1));
        Object result = op.computeValue(null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests zero against positive value
    @Test
    public void testComputeValue_zeroLessThanPositive_returnsFalse() {
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(new Constant(0), new Constant(1));
        Object result = op.computeValue(null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests decimal / double values where left > right
    @Test
    public void testComputeValue_doubleValuesLeftGreater_returnsTrue() {
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(new Constant(5.5), new Constant(5.4));
        Object result = op.computeValue(null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests decimal / double values where left < right
    @Test
    public void testComputeValue_doubleValuesLeftLess_returnsFalse() {
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(new Constant(3.14), new Constant(3.15));
        Object result = op.computeValue(null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests string representations of numbers where left > right
    @Test
    public void testComputeValue_stringNumbersLeftGreater_returnsTrue() {
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(new Constant("10"), new Constant("2"));
        Object result = op.computeValue(null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests string representations of numbers where left < right
    @Test
    public void testComputeValue_stringNumbersLeftLess_returnsFalse() {
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(new Constant("2"), new Constant("10"));
        Object result = op.computeValue(null);
        assertEquals(Boolean.FALSE, result);
    }

    // Directly tests evaluateCompare with positive integer
    @Test
    public void testEvaluateCompare_positive_returnsTrue() {
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(new Constant(1), new Constant(2));
        assertTrue(op.evaluateCompare(1));
    }

    // Directly tests evaluateCompare with zero
    @Test
    public void testEvaluateCompare_zero_returnsFalse() {
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(new Constant(1), new Constant(2));
        assertFalse(op.evaluateCompare(0));
    }

    // Directly tests evaluateCompare with negative integer
    @Test
    public void testEvaluateCompare_negative_returnsFalse() {
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(new Constant(1), new Constant(2));
        assertFalse(op.evaluateCompare(-1));
    }

    // Tests NaN comparison
    @Test
    public void testComputeValue_withNaN_returnsFalse() {
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(new Constant(Double.NaN), new Constant(1.0));
        Object result = op.computeValue(null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests infinity comparison
    @Test
    public void testComputeValue_withInfinity_returnsTrue() {
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(new Constant(Double.POSITIVE_INFINITY), new Constant(Double.MAX_VALUE));
        Object result = op.computeValue(null);
        assertEquals(Boolean.TRUE, result);
    }
}