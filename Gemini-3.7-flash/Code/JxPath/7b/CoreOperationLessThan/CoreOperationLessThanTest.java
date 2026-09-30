package org.apache.commons.jxpath.ri.compiler;

import org.junit.Test;
import static org.junit.Assert.*;

public class CoreOperationLessThanTest {

    // Tests getSymbol returns correct operator symbol
    @Test
    public void testGetSymbol_returnsCorrectSymbol() {
        CoreOperationLessThan op = new CoreOperationLessThan(new Constant(1), new Constant(2));
        assertEquals("<", op.getSymbol());
    }

    // Tests left value strictly less than right value returns Boolean.TRUE
    @Test
    public void testComputeValue_leftLessThanRight_returnsTrue() {
        CoreOperationLessThan op = new CoreOperationLessThan(new Constant(1), new Constant(2));
        Object result = op.computeValue(null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests left value strictly greater than right value returns Boolean.FALSE
    @Test
    public void testComputeValue_leftGreaterThanRight_returnsFalse() {
        CoreOperationLessThan op = new CoreOperationLessThan(new Constant(5), new Constant(3));
        Object result = op.computeValue(null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests equal values return Boolean.FALSE (boundary case for strict inequality)
    @Test
    public void testComputeValue_equalValues_returnsFalse() {
        CoreOperationLessThan op = new CoreOperationLessThan(new Constant(4), new Constant(4));
        Object result = op.computeValue(null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests negative values comparison
    @Test
    public void testComputeValue_negativeValues_returnsCorrectResult() {
        CoreOperationLessThan op1 = new CoreOperationLessThan(new Constant(-10), new Constant(-5));
        assertEquals(Boolean.TRUE, op1.computeValue(null));

        CoreOperationLessThan op2 = new CoreOperationLessThan(new Constant(-2), new Constant(-8));
        assertEquals(Boolean.FALSE, op2.computeValue(null));
    }

    // Tests comparisons around zero boundary
    @Test
    public void testComputeValue_zeroBoundary_returnsCorrectResult() {
        CoreOperationLessThan op1 = new CoreOperationLessThan(new Constant(-0.001), new Constant(0));
        assertEquals(Boolean.TRUE, op1.computeValue(null));

        CoreOperationLessThan op2 = new CoreOperationLessThan(new Constant(0), new Constant(0.001));
        assertEquals(Boolean.TRUE, op2.computeValue(null));

        CoreOperationLessThan op3 = new CoreOperationLessThan(new Constant(0), new Constant(0));
        assertEquals(Boolean.FALSE, op3.computeValue(null));
    }

    // Tests numeric string values are coerced to double correctly
    @Test
    public void testComputeValue_numericStrings_returnsCorrectResult() {
        CoreOperationLessThan op1 = new CoreOperationLessThan(new Constant("2.5"), new Constant("3.1"));
        assertEquals(Boolean.TRUE, op1.computeValue(null));

        CoreOperationLessThan op2 = new CoreOperationLessThan(new Constant("10"), new Constant("2"));
        assertEquals(Boolean.FALSE, op2.computeValue(null));
    }

    // Tests non-numeric string converts to NaN and comparisons return Boolean.FALSE
    @Test
    public void testComputeValue_nonNumericStringNaN_returnsFalse() {
        CoreOperationLessThan op1 = new CoreOperationLessThan(new Constant("invalid"), new Constant(10));
        assertEquals(Boolean.FALSE, op1.computeValue(null));

        CoreOperationLessThan op2 = new CoreOperationLessThan(new Constant(10), new Constant("invalid"));
        assertEquals(Boolean.FALSE, op2.computeValue(null));

        CoreOperationLessThan op3 = new CoreOperationLessThan(new Constant("abc"), new Constant("xyz"));
        assertEquals(Boolean.FALSE, op3.computeValue(null));
    }

    // Tests double extremes: Infinity and -Infinity
    @Test
    public void testComputeValue_infinities_returnsCorrectResult() {
        CoreOperationLessThan op1 = new CoreOperationLessThan(
                new Constant(Double.valueOf(Double.NEGATIVE_INFINITY)),
                new Constant(Double.valueOf(Double.POSITIVE_INFINITY))
        );
        assertEquals(Boolean.TRUE, op1.computeValue(null));

        CoreOperationLessThan op2 = new CoreOperationLessThan(
                new Constant(Double.valueOf(Double.POSITIVE_INFINITY)),
                new Constant(Double.valueOf(Double.MAX_VALUE))
        );
        assertEquals(Boolean.FALSE, op2.computeValue(null));
    }

    // Tests double boundary values (Double.MAX_VALUE and Double.MIN_VALUE)
    @Test
    public void testComputeValue_doubleBoundaries_returnsCorrectResult() {
        CoreOperationLessThan op1 = new CoreOperationLessThan(
                new Constant(Double.valueOf(Double.MIN_VALUE)),
                new Constant(Double.valueOf(Double.MAX_VALUE))
        );
        assertEquals(Boolean.TRUE, op1.computeValue(null));

        CoreOperationLessThan op2 = new CoreOperationLessThan(
                new Constant(Double.valueOf(Double.MAX_VALUE)),
                new Constant(Double.valueOf(Double.MIN_VALUE))
        );
        assertEquals(Boolean.FALSE, op2.computeValue(null));
    }

    // Tests floating point precision comparison
    @Test
    public void testComputeValue_decimalPrecision_returnsCorrectResult() {
        CoreOperationLessThan op = new CoreOperationLessThan(new Constant(1.0000001), new Constant(1.0000002));
        assertEquals(Boolean.TRUE, op.computeValue(null));
    }

    // Tests evaluateCompare protected method directly with negative, zero, and positive compare results
    @Test
    public void testEvaluateCompare_variousCompareResults() {
        CoreOperationLessThan op = new CoreOperationLessThan(new Constant(1), new Constant(2));

        assertTrue(op.evaluateCompare(-1));
        assertTrue(op.evaluateCompare(-100));
        assertTrue(op.evaluateCompare(Integer.MIN_VALUE));

        assertFalse(op.evaluateCompare(0));
        assertFalse(op.evaluateCompare(1));
        assertFalse(op.evaluateCompare(100));
        assertFalse(op.evaluateCompare(Integer.MAX_VALUE));
    }

    // Tests boolean constants evaluation
    @Test
    public void testComputeValue_booleanConstants() {
        CoreOperationLessThan op1 = new CoreOperationLessThan(new Constant(Boolean.FALSE), new Constant(Boolean.TRUE));
        assertEquals(Boolean.TRUE, op1.computeValue(null));

        CoreOperationLessThan op2 = new CoreOperationLessThan(new Constant(Boolean.TRUE), new Constant(Boolean.FALSE));
        assertEquals(Boolean.FALSE, op2.computeValue(null));

        CoreOperationLessThan op3 = new CoreOperationLessThan(new Constant(Boolean.TRUE), new Constant(Boolean.TRUE));
        assertEquals(Boolean.FALSE, op3.computeValue(null));
    }
}