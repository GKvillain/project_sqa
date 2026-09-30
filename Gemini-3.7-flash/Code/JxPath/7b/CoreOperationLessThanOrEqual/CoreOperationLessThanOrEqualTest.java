package org.apache.commons.jxpath.ri.compiler;

import org.apache.commons.jxpath.ri.EvalContext;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CoreOperationLessThanOrEqualTest {

    // Tests symbol representation
    @Test
    public void testGetSymbol_returnsCorrectSymbol() {
        CoreOperationLessThanOrEqual op = new CoreOperationLessThanOrEqual(
                new Constant(Double.valueOf(1.0)),
                new Constant(Double.valueOf(2.0))
        );
        assertEquals("<=", op.getSymbol());
    }

    // Tests left less than right returns true
    @Test
    public void testComputeValue_leftLessThanRight_returnsTrue() {
        CoreOperationLessThanOrEqual op = new CoreOperationLessThanOrEqual(
                new Constant(Double.valueOf(1.0)),
                new Constant(Double.valueOf(2.0))
        );
        Object result = op.computeValue((EvalContext) null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests left equal to right returns true
    @Test
    public void testComputeValue_leftEqualToRight_returnsTrue() {
        CoreOperationLessThanOrEqual op = new CoreOperationLessThanOrEqual(
                new Constant(Double.valueOf(2.0)),
                new Constant(Double.valueOf(2.0))
        );
        Object result = op.computeValue((EvalContext) null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests left greater than right returns false
    @Test
    public void testComputeValue_leftGreaterThanRight_returnsFalse() {
        CoreOperationLessThanOrEqual op = new CoreOperationLessThanOrEqual(
                new Constant(Double.valueOf(3.0)),
                new Constant(Double.valueOf(2.0))
        );
        Object result = op.computeValue((EvalContext) null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests negative values comparison returning true
    @Test
    public void testComputeValue_negativeValuesLessThan_returnsTrue() {
        CoreOperationLessThanOrEqual op = new CoreOperationLessThanOrEqual(
                new Constant(Double.valueOf(-5.0)),
                new Constant(Double.valueOf(-2.0))
        );
        Object result = op.computeValue((EvalContext) null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests negative values comparison returning false
    @Test
    public void testComputeValue_negativeValuesGreaterThan_returnsFalse() {
        CoreOperationLessThanOrEqual op = new CoreOperationLessThanOrEqual(
                new Constant(Double.valueOf(-1.0)),
                new Constant(Double.valueOf(-3.0))
        );
        Object result = op.computeValue((EvalContext) null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests zero boundary comparison
    @Test
    public void testComputeValue_zeroBoundary_returnsTrue() {
        CoreOperationLessThanOrEqual op = new CoreOperationLessThanOrEqual(
                new Constant(Double.valueOf(0.0)),
                new Constant(Double.valueOf(0.0))
        );
        Object result = op.computeValue((EvalContext) null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests string numeric conversion and comparison
    @Test
    public void testComputeValue_stringOperands_returnsCorrectResult() {
        CoreOperationLessThanOrEqual op = new CoreOperationLessThanOrEqual(
                new Constant("10"),
                new Constant("20")
        );
        Object result = op.computeValue((EvalContext) null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests NaN comparison resulting in false
    @Test
    public void testComputeValue_nanOperand_returnsFalse() {
        CoreOperationLessThanOrEqual op = new CoreOperationLessThanOrEqual(
                new Constant("not_a_number"),
                new Constant(Double.valueOf(1.0))
        );
        Object result = op.computeValue((EvalContext) null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests infinite boundary comparison
    @Test
    public void testComputeValue_infinityOperands_returnsTrue() {
        CoreOperationLessThanOrEqual op = new CoreOperationLessThanOrEqual(
                new Constant(Double.valueOf(Double.NEGATIVE_INFINITY)),
                new Constant(Double.valueOf(Double.POSITIVE_INFINITY))
        );
        Object result = op.computeValue((EvalContext) null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests evaluateCompare directly
    @Test
    public void testEvaluateCompare_variousValues() {
        CoreOperationLessThanOrEqual op = new CoreOperationLessThanOrEqual(
                new Constant(Double.valueOf(1.0)),
                new Constant(Double.valueOf(2.0))
        );
        assertTrue(op.evaluateCompare(-1));
        assertTrue(op.evaluateCompare(-100));
        assertTrue(op.evaluateCompare(0));
        assertFalse(op.evaluateCompare(1));
        assertFalse(op.evaluateCompare(100));
    }
}