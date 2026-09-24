package org.apache.commons.jxpath.ri.compiler;

import org.junit.Test;
import static org.junit.Assert.*;

public class CoreOperationLessThanTest {

    // Tests getSymbol method
    @Test
    public void testGetSymbol_returnsLessThanSign() {
        Constant arg1 = new Constant(Integer.valueOf(1));
        Constant arg2 = new Constant(Integer.valueOf(2));
        CoreOperationLessThan operation = new CoreOperationLessThan(arg1, arg2);
        assertEquals("<", operation.getSymbol());
    }

    // Tests true branch where left operand is strictly less than right operand
    @Test
    public void testComputeValue_leftLessThanRight_returnsTrue() {
        Constant arg1 = new Constant(Integer.valueOf(1));
        Constant arg2 = new Constant(Integer.valueOf(2));
        CoreOperationLessThan operation = new CoreOperationLessThan(arg1, arg2);
        Object result = operation.computeValue(null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests false branch where left operand is strictly greater than right operand
    @Test
    public void testComputeValue_leftGreaterThanRight_returnsFalse() {
        Constant arg1 = new Constant(Integer.valueOf(5));
        Constant arg2 = new Constant(Integer.valueOf(2));
        CoreOperationLessThan operation = new CoreOperationLessThan(arg1, arg2);
        Object result = operation.computeValue(null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests boundary case where left operand equals right operand
    @Test
    public void testComputeValue_leftEqualsRight_returnsFalse() {
        Constant arg1 = new Constant(Integer.valueOf(3));
        Constant arg2 = new Constant(Integer.valueOf(3));
        CoreOperationLessThan operation = new CoreOperationLessThan(arg1, arg2);
        Object result = operation.computeValue(null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests negative values comparison
    @Test
    public void testComputeValue_negativeValues_returnsTrue() {
        Constant arg1 = new Constant(Integer.valueOf(-10));
        Constant arg2 = new Constant(Integer.valueOf(-5));
        CoreOperationLessThan operation = new CoreOperationLessThan(arg1, arg2);
        Object result = operation.computeValue(null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests negative value greater than another negative value
    @Test
    public void testComputeValue_negativeValues_returnsFalse() {
        Constant arg1 = new Constant(Integer.valueOf(-2));
        Constant arg2 = new Constant(Integer.valueOf(-8));
        CoreOperationLessThan operation = new CoreOperationLessThan(arg1, arg2);
        Object result = operation.computeValue(null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests zero boundary comparison
    @Test
    public void testComputeValue_zeroBoundary_returnsCorrectBoolean() {
        Constant zero = new Constant(Integer.valueOf(0));
        Constant positive = new Constant(Integer.valueOf(1));
        Constant negative = new Constant(Integer.valueOf(-1));

        CoreOperationLessThan zeroLessThanPositive = new CoreOperationLessThan(zero, positive);
        assertEquals(Boolean.TRUE, zeroLessThanPositive.computeValue(null));

        CoreOperationLessThan negativeLessThanZero = new CoreOperationLessThan(negative, zero);
        assertEquals(Boolean.TRUE, negativeLessThanZero.computeValue(null));

        CoreOperationLessThan zeroLessThanZero = new CoreOperationLessThan(zero, zero);
        assertEquals(Boolean.FALSE, zeroLessThanZero.computeValue(null));
    }

    // Tests double floating-point numbers comparison
    @Test
    public void testComputeValue_doubleValues_returnsCorrectBoolean() {
        Constant arg1 = new Constant(Double.valueOf(1.123));
        Constant arg2 = new Constant(Double.valueOf(1.124));
        CoreOperationLessThan operation = new CoreOperationLessThan(arg1, arg2);
        assertEquals(Boolean.TRUE, operation.computeValue(null));

        CoreOperationLessThan reverseOperation = new CoreOperationLessThan(arg2, arg1);
        assertEquals(Boolean.FALSE, reverseOperation.computeValue(null));
    }

    // Tests string conversion to double for comparison
    @Test
    public void testComputeValue_stringNumbers_returnsCorrectBoolean() {
        Constant arg1 = new Constant("10.5");
        Constant arg2 = new Constant("20.1");
        CoreOperationLessThan operation = new CoreOperationLessThan(arg1, arg2);
        assertEquals(Boolean.TRUE, operation.computeValue(null));
    }

    // Tests NaN comparison resulting in false
    @Test
    public void testComputeValue_nanComparison_returnsFalse() {
        Constant arg1 = new Constant("non-number");
        Constant arg2 = new Constant(Integer.valueOf(10));
        CoreOperationLessThan operation = new CoreOperationLessThan(arg1, arg2);
        assertEquals(Boolean.FALSE, operation.computeValue(null));
    }

    // Tests boolean values comparison
    @Test
    public void testComputeValue_booleanValues_returnsCorrectBoolean() {
        Constant falseConst = new Constant(Boolean.FALSE);
        Constant trueConst = new Constant(Boolean.TRUE);

        CoreOperationLessThan falseLessThanTrue = new CoreOperationLessThan(falseConst, trueConst);
        assertEquals(Boolean.TRUE, falseLessThanTrue.computeValue(null));

        CoreOperationLessThan trueLessThanFalse = new CoreOperationLessThan(trueConst, falseConst);
        assertEquals(Boolean.FALSE, trueLessThanFalse.computeValue(null));

        CoreOperationLessThan trueLessThanTrue = new CoreOperationLessThan(trueConst, trueConst);
        assertEquals(Boolean.FALSE, trueLessThanTrue.computeValue(null));
    }

    // Tests infinite values comparison
    @Test
    public void testComputeValue_infinityValues_returnsCorrectBoolean() {
        Constant negInf = new Constant(Double.valueOf(Double.NEGATIVE_INFINITY));
        Constant posInf = new Constant(Double.valueOf(Double.POSITIVE_INFINITY));
        Constant zero = new Constant(Integer.valueOf(0));

        CoreOperationLessThan negLessThanPos = new CoreOperationLessThan(negInf, posInf);
        assertEquals(Boolean.TRUE, negLessThanPos.computeValue(null));

        CoreOperationLessThan posLessThanNeg = new CoreOperationLessThan(posInf, negInf);
        assertEquals(Boolean.FALSE, posLessThanNeg.computeValue(null));

        CoreOperationLessThan negLessThanZero = new CoreOperationLessThan(negInf, zero);
        assertEquals(Boolean.TRUE, negLessThanZero.computeValue(null));

        CoreOperationLessThan zeroLessThanPos = new CoreOperationLessThan(zero, posInf);
        assertEquals(Boolean.TRUE, zeroLessThanPos.computeValue(null));
    }

    // Tests isSymmetric method
    @Test
    public void testIsSymmetric_returnsFalse() {
        Constant arg1 = new Constant(Integer.valueOf(1));
        Constant arg2 = new Constant(Integer.valueOf(2));
        CoreOperationLessThan operation = new CoreOperationLessThan(arg1, arg2);
        assertFalse(operation.isSymmetric());
    }
}