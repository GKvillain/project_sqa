package org.apache.commons.jxpath.ri.compiler;

import org.apache.commons.jxpath.ri.EvalContext;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CoreOperationGreaterThanTest {

    // Tests getSymbol method returns correct operator symbol
    @Test
    public void testGetSymbol_noCondition_returnsGreaterThanSymbol() {
        Constant arg1 = new Constant(Double.valueOf(1.0));
        Constant arg2 = new Constant(Double.valueOf(2.0));
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(arg1, arg2);

        assertEquals(">", op.getSymbol());
    }

    // Tests computeValue with left operand strictly greater than right operand
    @Test
    public void testComputeValue_leftGreaterThanRight_returnsTrue() {
        Constant arg1 = new Constant(Double.valueOf(5.0));
        Constant arg2 = new Constant(Double.valueOf(3.0));
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(arg1, arg2);

        Object result = op.computeValue((EvalContext) null);

        assertEquals(Boolean.TRUE, result);
    }

    // Tests computeValue with left operand strictly less than right operand
    @Test
    public void testComputeValue_leftLessThanRight_returnsFalse() {
        Constant arg1 = new Constant(Double.valueOf(2.0));
        Constant arg2 = new Constant(Double.valueOf(4.0));
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(arg1, arg2);

        Object result = op.computeValue((EvalContext) null);

        assertEquals(Boolean.FALSE, result);
    }

    // Tests computeValue with equal operand values
    @Test
    public void testComputeValue_equalOperands_returnsFalse() {
        Constant arg1 = new Constant(Double.valueOf(3.0));
        Constant arg2 = new Constant(Double.valueOf(3.0));
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(arg1, arg2);

        Object result = op.computeValue((EvalContext) null);

        assertEquals(Boolean.FALSE, result);
    }

    // Tests computeValue with zero values
    @Test
    public void testComputeValue_zeroVersusZero_returnsFalse() {
        Constant arg1 = new Constant(Double.valueOf(0.0));
        Constant arg2 = new Constant(Double.valueOf(0.0));
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(arg1, arg2);

        Object result = op.computeValue((EvalContext) null);

        assertEquals(Boolean.FALSE, result);
    }

    // Tests computeValue with zero and negative value
    @Test
    public void testComputeValue_zeroGreaterThanNegative_returnsTrue() {
        Constant arg1 = new Constant(Double.valueOf(0.0));
        Constant arg2 = new Constant(Double.valueOf(-1.0));
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(arg1, arg2);

        Object result = op.computeValue((EvalContext) null);

        assertEquals(Boolean.TRUE, result);
    }

    // Tests computeValue with negative numbers
    @Test
    public void testComputeValue_negativeNumbersGreater_returnsTrue() {
        Constant arg1 = new Constant(Double.valueOf(-2.0));
        Constant arg2 = new Constant(Double.valueOf(-5.0));
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(arg1, arg2);

        Object result = op.computeValue((EvalContext) null);

        assertEquals(Boolean.TRUE, result);
    }

    // Tests computeValue with negative numbers less than
    @Test
    public void testComputeValue_negativeNumbersLess_returnsFalse() {
        Constant arg1 = new Constant(Double.valueOf(-5.0));
        Constant arg2 = new Constant(Double.valueOf(-2.0));
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(arg1, arg2);

        Object result = op.computeValue((EvalContext) null);

        assertEquals(Boolean.FALSE, result);
    }

    // Tests computeValue with integer number constants
    @Test
    public void testComputeValue_integerConstants_returnsCorrectResult() {
        Constant arg1 = new Constant(Integer.valueOf(10));
        Constant arg2 = new Constant(Integer.valueOf(5));
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(arg1, arg2);

        Object result = op.computeValue((EvalContext) null);

        assertEquals(Boolean.TRUE, result);
    }

    // Tests computeValue with numeric string constants
    @Test
    public void testComputeValue_stringOperands_parsedAsDouble() {
        Constant arg1 = new Constant("15.5");
        Constant arg2 = new Constant("10.2");
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(arg1, arg2);

        Object result = op.computeValue((EvalContext) null);

        assertEquals(Boolean.TRUE, result);
    }

    // Tests computeValue with string comparisons where alphabetical order differs from numeric
    @Test
    public void testComputeValue_stringNumericDifference_returnsTrue() {
        Constant arg1 = new Constant("10");
        Constant arg2 = new Constant("2");
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(arg1, arg2);

        Object result = op.computeValue((EvalContext) null);

        assertEquals(Boolean.TRUE, result);
    }

    // Tests computeValue with boundary double infinity values
    @Test
    public void testComputeValue_positiveInfinityVersusMaxDouble_returnsTrue() {
        Constant arg1 = new Constant(Double.valueOf(Double.POSITIVE_INFINITY));
        Constant arg2 = new Constant(Double.valueOf(Double.MAX_VALUE));
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(arg1, arg2);

        Object result = op.computeValue((EvalContext) null);

        assertEquals(Boolean.TRUE, result);
    }

    // Tests computeValue with negative infinity boundary
    @Test
    public void testComputeValue_negativeInfinityVersusMinValue_returnsFalse() {
        Constant arg1 = new Constant(Double.valueOf(Double.NEGATIVE_INFINITY));
        Constant arg2 = new Constant(Double.valueOf(-Double.MAX_VALUE));
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(arg1, arg2);

        Object result = op.computeValue((EvalContext) null);

        assertEquals(Boolean.FALSE, result);
    }

    // Tests computeValue with NaN operands
    @Test
    public void testComputeValue_nanOperand_returnsFalse() {
        Constant arg1 = new Constant(Double.valueOf(Double.NaN));
        Constant arg2 = new Constant(Double.valueOf(1.0));
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(arg1, arg2);

        Object result = op.computeValue((EvalContext) null);

        assertEquals(Boolean.FALSE, result);
    }

    // Tests evaluateCompare directly with positive compare values
    @Test
    public void testEvaluateCompare_positiveCompareValue_returnsTrue() {
        Constant arg1 = new Constant(Double.valueOf(1.0));
        Constant arg2 = new Constant(Double.valueOf(2.0));
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(arg1, arg2);

        assertTrue(op.evaluateCompare(1));
        assertTrue(op.evaluateCompare(100));
    }

    // Tests evaluateCompare directly with zero compare value
    @Test
    public void testEvaluateCompare_zeroCompareValue_returnsFalse() {
        Constant arg1 = new Constant(Double.valueOf(1.0));
        Constant arg2 = new Constant(Double.valueOf(2.0));
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(arg1, arg2);

        assertFalse(op.evaluateCompare(0));
    }

    // Tests evaluateCompare directly with negative compare values
    @Test
    public void testEvaluateCompare_negativeCompareValue_returnsFalse() {
        Constant arg1 = new Constant(Double.valueOf(1.0));
        Constant arg2 = new Constant(Double.valueOf(2.0));
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(arg1, arg2);

        assertFalse(op.evaluateCompare(-1));
        assertFalse(op.evaluateCompare(-100));
    }

    // Tests computeValue with right operand being NaN
    @Test
    public void testComputeValue_rightNanOperand_returnsFalse() {
        Constant arg1 = new Constant(Double.valueOf(1.0));
        Constant arg2 = new Constant(Double.valueOf(Double.NaN));
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(arg1, arg2);

        Object result = op.computeValue((EvalContext) null);

        assertEquals(Boolean.FALSE, result);
    }
}