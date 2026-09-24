package org.apache.commons.jxpath.ri.compiler;

import org.apache.commons.jxpath.ri.EvalContext;
import org.junit.Test;
import static org.junit.Assert.*;

public class CoreOperationGreaterThanOrEqualTest {

    // Tests getSymbol method returns correct operator string
    @Test
    public void testGetSymbol_default_returnsCorrectSymbol() {
        Constant arg1 = new Constant(Integer.valueOf(1));
        Constant arg2 = new Constant(Integer.valueOf(2));
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(arg1, arg2);

        assertEquals(">=", op.getSymbol());
    }

    // Tests computeValue when left operand is greater than right operand
    @Test
    public void testComputeValue_leftGreaterThanRight_returnsTrue() {
        Constant arg1 = new Constant(Double.valueOf(5.0));
        Constant arg2 = new Constant(Double.valueOf(3.0));
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(arg1, arg2);

        Object result = op.computeValue((EvalContext) null);

        assertEquals(Boolean.TRUE, result);
    }

    // Tests computeValue when left operand is equal to right operand
    @Test
    public void testComputeValue_leftEqualsRight_returnsTrue() {
        Constant arg1 = new Constant(Double.valueOf(4.5));
        Constant arg2 = new Constant(Double.valueOf(4.5));
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(arg1, arg2);

        Object result = op.computeValue((EvalContext) null);

        assertEquals(Boolean.TRUE, result);
    }

    // Tests computeValue when left operand is less than right operand
    @Test
    public void testComputeValue_leftLessThanRight_returnsFalse() {
        Constant arg1 = new Constant(Double.valueOf(2.0));
        Constant arg2 = new Constant(Double.valueOf(7.0));
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(arg1, arg2);

        Object result = op.computeValue((EvalContext) null);

        assertEquals(Boolean.FALSE, result);
    }

    // Tests computeValue with negative numbers where left is greater
    @Test
    public void testComputeValue_negativeNumbersLeftGreater_returnsTrue() {
        Constant arg1 = new Constant(Double.valueOf(-1.0));
        Constant arg2 = new Constant(Double.valueOf(-5.0));
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(arg1, arg2);

        Object result = op.computeValue((EvalContext) null);

        assertEquals(Boolean.TRUE, result);
    }

    // Tests computeValue with negative numbers where left is smaller
    @Test
    public void testComputeValue_negativeNumbersLeftSmaller_returnsFalse() {
        Constant arg1 = new Constant(Double.valueOf(-10.0));
        Constant arg2 = new Constant(Double.valueOf(-2.0));
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(arg1, arg2);

        Object result = op.computeValue((EvalContext) null);

        assertEquals(Boolean.FALSE, result);
    }

    // Tests computeValue with zero and negative value boundary
    @Test
    public void testComputeValue_zeroAndNegative_returnsTrue() {
        Constant arg1 = new Constant(Double.valueOf(0.0));
        Constant arg2 = new Constant(Double.valueOf(-0.1));
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(arg1, arg2);

        Object result = op.computeValue((EvalContext) null);

        assertEquals(Boolean.TRUE, result);
    }

    // Tests computeValue with zero values comparison
    @Test
    public void testComputeValue_bothZeros_returnsTrue() {
        Constant arg1 = new Constant(Double.valueOf(0.0));
        Constant arg2 = new Constant(Double.valueOf(0.0));
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(arg1, arg2);

        Object result = op.computeValue((EvalContext) null);

        assertEquals(Boolean.TRUE, result);
    }

    // Tests computeValue with string representations of numbers where left is greater
    @Test
    public void testComputeValue_numericStringsLeftGreater_returnsTrue() {
        Constant arg1 = new Constant("10.5");
        Constant arg2 = new Constant("3.2");
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(arg1, arg2);

        Object result = op.computeValue((EvalContext) null);

        assertEquals(Boolean.TRUE, result);
    }

    // Tests computeValue with string representations of numbers where left is smaller
    @Test
    public void testComputeValue_numericStringsLeftSmaller_returnsFalse() {
        Constant arg1 = new Constant("1.5");
        Constant arg2 = new Constant("9.8");
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(arg1, arg2);

        Object result = op.computeValue((EvalContext) null);

        assertEquals(Boolean.FALSE, result);
    }

    // Tests computeValue with infinity values boundary
    @Test
    public void testComputeValue_positiveInfinityAndMaxDouble_returnsTrue() {
        Constant arg1 = new Constant(Double.valueOf(Double.POSITIVE_INFINITY));
        Constant arg2 = new Constant(Double.valueOf(Double.MAX_VALUE));
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(arg1, arg2);

        Object result = op.computeValue((EvalContext) null);

        assertEquals(Boolean.TRUE, result);
    }

    // Tests computeValue with NaN value returning false
    @Test
    public void testComputeValue_withNaN_returnsFalse() {
        Constant arg1 = new Constant(Double.valueOf(Double.NaN));
        Constant arg2 = new Constant(Double.valueOf(1.0));
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(arg1, arg2);

        Object result = op.computeValue((EvalContext) null);

        assertEquals(Boolean.FALSE, result);
    }
}