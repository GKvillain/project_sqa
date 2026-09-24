package org.apache.commons.jxpath.ri.compiler;

import org.junit.Test;
import static org.junit.Assert.*;

public class CoreOperationLessThanOrEqualTest {

    // Tests getSymbol method returns correct operator symbol
    @Test
    public void testGetSymbol_standardCall_returnsCorrectSymbol() {
        Constant arg1 = new Constant(1);
        Constant arg2 = new Constant(2);
        CoreOperationLessThanOrEqual operation = new CoreOperationLessThanOrEqual(arg1, arg2);
        assertEquals("<=", operation.getSymbol());
    }

    // Tests computeValue when left operand is strictly less than right operand
    @Test
    public void testComputeValue_leftLessThanRight_returnsTrue() {
        Constant arg1 = new Constant(1.0);
        Constant arg2 = new Constant(2.0);
        CoreOperationLessThanOrEqual operation = new CoreOperationLessThanOrEqual(arg1, arg2);
        Object result = operation.computeValue(null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests computeValue when left operand is equal to right operand
    @Test
    public void testComputeValue_leftEqualsRight_returnsTrue() {
        Constant arg1 = new Constant(5.5);
        Constant arg2 = new Constant(5.5);
        CoreOperationLessThanOrEqual operation = new CoreOperationLessThanOrEqual(arg1, arg2);
        Object result = operation.computeValue(null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests computeValue when left operand is greater than right operand
    @Test
    public void testComputeValue_leftGreaterThanRight_returnsFalse() {
        Constant arg1 = new Constant(10.0);
        Constant arg2 = new Constant(5.0);
        CoreOperationLessThanOrEqual operation = new CoreOperationLessThanOrEqual(arg1, arg2);
        Object result = operation.computeValue(null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests computeValue with negative numbers where left is less than right
    @Test
    public void testComputeValue_negativeNumbersLeftLess_returnsTrue() {
        Constant arg1 = new Constant(-10.0);
        Constant arg2 = new Constant(-5.0);
        CoreOperationLessThanOrEqual operation = new CoreOperationLessThanOrEqual(arg1, arg2);
        Object result = operation.computeValue(null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests computeValue with negative numbers where left is greater than right
    @Test
    public void testComputeValue_negativeNumbersLeftGreater_returnsFalse() {
        Constant arg1 = new Constant(-1.0);
        Constant arg2 = new Constant(-3.0);
        CoreOperationLessThanOrEqual operation = new CoreOperationLessThanOrEqual(arg1, arg2);
        Object result = operation.computeValue(null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests computeValue with zero values on both sides
    @Test
    public void testComputeValue_zeros_returnsTrue() {
        Constant arg1 = new Constant(0);
        Constant arg2 = new Constant(0);
        CoreOperationLessThanOrEqual operation = new CoreOperationLessThanOrEqual(arg1, arg2);
        Object result = operation.computeValue(null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests computeValue with numeric string constants
    @Test
    public void testComputeValue_numericStringsLeftLess_returnsTrue() {
        Constant arg1 = new Constant("4.5");
        Constant arg2 = new Constant("5.0");
        CoreOperationLessThanOrEqual operation = new CoreOperationLessThanOrEqual(arg1, arg2);
        Object result = operation.computeValue(null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests computeValue with numeric string constants where left is greater
    @Test
    public void testComputeValue_numericStringsLeftGreater_returnsFalse() {
        Constant arg1 = new Constant("10.0");
        Constant arg2 = new Constant("2.0");
        CoreOperationLessThanOrEqual operation = new CoreOperationLessThanOrEqual(arg1, arg2);
        Object result = operation.computeValue(null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests computeValue with boundary value Double.MIN_VALUE and Double.MAX_VALUE
    @Test
    public void testComputeValue_minAndMaxDouble_returnsTrue() {
        Constant arg1 = new Constant(Double.MIN_VALUE);
        Constant arg2 = new Constant(Double.MAX_VALUE);
        CoreOperationLessThanOrEqual operation = new CoreOperationLessThanOrEqual(arg1, arg2);
        Object result = operation.computeValue(null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests computeValue with Double.POSITIVE_INFINITY
    @Test
    public void testComputeValue_infinityBoundary_returnsTrue() {
        Constant arg1 = new Constant(Double.NEGATIVE_INFINITY);
        Constant arg2 = new Constant(Double.POSITIVE_INFINITY);
        CoreOperationLessThanOrEqual operation = new CoreOperationLessThanOrEqual(arg1, arg2);
        Object result = operation.computeValue(null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests computeValue with NaN operand
    @Test
    public void testComputeValue_nanOperand_returnsFalse() {
        Constant arg1 = new Constant(Double.NaN);
        Constant arg2 = new Constant(1.0);
        CoreOperationLessThanOrEqual operation = new CoreOperationLessThanOrEqual(arg1, arg2);
        Object result = operation.computeValue(null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests evaluateCompare directly for negative, zero, and positive compare results
    @Test
    public void testEvaluateCompare_differentComparisonResults() {
        CoreOperationLessThanOrEqual operation = new CoreOperationLessThanOrEqual(new Constant(0), new Constant(0));
        assertTrue(operation.evaluateCompare(-1));
        assertTrue(operation.evaluateCompare(0));
        assertFalse(operation.evaluateCompare(1));
    }

    // Tests computeValue when operands are boolean expressions (evaluates true <= false and false <= true)
    @Test
    public void testComputeValue_booleanExpressions() {
        CoreOperationEqual trueExpr = new CoreOperationEqual(new Constant(1), new Constant(1));
        CoreOperationEqual falseExpr = new CoreOperationEqual(new Constant(1), new Constant(2));

        // false (0.0) <= true (1.0) -> true
        CoreOperationLessThanOrEqual op1 = new CoreOperationLessThanOrEqual(falseExpr, trueExpr);
        assertEquals(Boolean.TRUE, op1.computeValue(null));

        // true (1.0) <= false (0.0) -> false
        CoreOperationLessThanOrEqual op2 = new CoreOperationLessThanOrEqual(trueExpr, falseExpr);
        assertEquals(Boolean.FALSE, op2.computeValue(null));

        // true (1.0) <= true (1.0) -> true
        CoreOperationLessThanOrEqual op3 = new CoreOperationLessThanOrEqual(trueExpr, trueExpr);
        assertEquals(Boolean.TRUE, op3.computeValue(null));
    }
}