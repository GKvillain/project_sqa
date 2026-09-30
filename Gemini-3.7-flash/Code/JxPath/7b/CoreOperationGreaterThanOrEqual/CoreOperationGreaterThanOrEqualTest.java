package org.apache.commons.jxpath.ri.compiler;

import org.apache.commons.jxpath.ri.EvalContext;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CoreOperationGreaterThanOrEqualTest {

    // Tests getSymbol returns correct operator string
    @Test
    public void testGetSymbol_returnsCorrectSymbol() {
        Constant arg1 = new Constant(Double.valueOf(1.0));
        Constant arg2 = new Constant(Double.valueOf(2.0));
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(arg1, arg2);
        assertEquals(">=", op.getSymbol());
    }

    // Tests greater value comparison returns true
    @Test
    public void testComputeValue_greaterThan_returnsTrue() {
        Constant arg1 = new Constant(Double.valueOf(5.0));
        Constant arg2 = new Constant(Double.valueOf(3.0));
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(arg1, arg2);
        Object result = op.computeValue((EvalContext) null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests equal value comparison returns true
    @Test
    public void testComputeValue_equalTo_returnsTrue() {
        Constant arg1 = new Constant(Double.valueOf(4.5));
        Constant arg2 = new Constant(Double.valueOf(4.5));
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(arg1, arg2);
        Object result = op.computeValue((EvalContext) null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests lesser value comparison returns false
    @Test
    public void testComputeValue_lessThan_returnsFalse() {
        Constant arg1 = new Constant(Double.valueOf(2.0));
        Constant arg2 = new Constant(Double.valueOf(5.0));
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(arg1, arg2);
        Object result = op.computeValue((EvalContext) null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests negative numbers comparison
    @Test
    public void testComputeValue_negativeNumbers_returnsCorrectResult() {
        Constant arg1 = new Constant(Double.valueOf(-2.0));
        Constant arg2 = new Constant(Double.valueOf(-5.0));
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(arg1, arg2);
        Object result = op.computeValue((EvalContext) null);
        assertEquals(Boolean.TRUE, result);

        Constant arg3 = new Constant(Double.valueOf(-10.0));
        Constant arg4 = new Constant(Double.valueOf(-5.0));
        CoreOperationGreaterThanOrEqual op2 = new CoreOperationGreaterThanOrEqual(arg3, arg4);
        Object result2 = op2.computeValue((EvalContext) null);
        assertEquals(Boolean.FALSE, result2);
    }

    // Tests zero boundary comparison
    @Test
    public void testComputeValue_zeroValues_returnsTrue() {
        Constant arg1 = new Constant(Double.valueOf(0.0));
        Constant arg2 = new Constant(Double.valueOf(-0.0));
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(arg1, arg2);
        Object result = op.computeValue((EvalContext) null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests string representations of numbers
    @Test
    public void testComputeValue_stringNumbers_returnsCorrectResult() {
        Constant arg1 = new Constant("10.5");
        Constant arg2 = new Constant("10.5");
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(arg1, arg2);
        Object result = op.computeValue((EvalContext) null);
        assertEquals(Boolean.TRUE, result);

        Constant arg3 = new Constant("15");
        Constant arg4 = new Constant("20");
        CoreOperationGreaterThanOrEqual op2 = new CoreOperationGreaterThanOrEqual(arg3, arg4);
        Object result2 = op2.computeValue((EvalContext) null);
        assertEquals(Boolean.FALSE, result2);
    }

    // Tests positive and negative infinity
    @Test
    public void testComputeValue_infinityValues_returnsCorrectResult() {
        Constant arg1 = new Constant(Double.valueOf(Double.POSITIVE_INFINITY));
        Constant arg2 = new Constant(Double.valueOf(Double.MAX_VALUE));
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(arg1, arg2);
        Object result = op.computeValue((EvalContext) null);
        assertEquals(Boolean.TRUE, result);

        Constant arg3 = new Constant(Double.valueOf(Double.NEGATIVE_INFINITY));
        Constant arg4 = new Constant(Double.valueOf(-Double.MAX_VALUE));
        CoreOperationGreaterThanOrEqual op2 = new CoreOperationGreaterThanOrEqual(arg3, arg4);
        Object result2 = op2.computeValue((EvalContext) null);
        assertEquals(Boolean.FALSE, result2);
    }

    // Tests NaN comparison returns false
    @Test
    public void testComputeValue_nanValue_returnsFalse() {
        Constant arg1 = new Constant(Double.valueOf(Double.NaN));
        Constant arg2 = new Constant(Double.valueOf(0.0));
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(arg1, arg2);
        Object result = op.computeValue((EvalContext) null);
        assertEquals(Boolean.FALSE, result);

        Constant arg3 = new Constant(Double.valueOf(Double.NaN));
        Constant arg4 = new Constant(Double.valueOf(Double.NaN));
        CoreOperationGreaterThanOrEqual op2 = new CoreOperationGreaterThanOrEqual(arg3, arg4);
        Object result2 = op2.computeValue((EvalContext) null);
        assertEquals(Boolean.FALSE, result2);
    }

    // Direct tests for evaluateCompare method
    @Test
    public void testEvaluateCompare() {
        Constant arg1 = new Constant(Double.valueOf(1.0));
        Constant arg2 = new Constant(Double.valueOf(2.0));
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(arg1, arg2);

        assertTrue("evaluateCompare(1) should return true", op.evaluateCompare(1));
        assertTrue("evaluateCompare(0) should return true", op.evaluateCompare(0));
        assertFalse("evaluateCompare(-1) should return false", op.evaluateCompare(-1));
    }
}