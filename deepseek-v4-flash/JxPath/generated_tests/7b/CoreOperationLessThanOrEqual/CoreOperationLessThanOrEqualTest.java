package org.apache.commons.jxpath.ri.compiler;

import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.InfoSetUtil;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * JUnit 4 test class for CoreOperationLessThanOrEqual.
 * Tests the computeValue method of the CoreOperationLessThanOrEqual class.
 */
public class CoreOperationLessThanOrEqualTest {

    // Helper method to create a constant expression with a double value
    private Expression createConstantExpression(final double value) {
        return new Expression() {
            @Override
            public Object computeValue(EvalContext context) {
                return new Double(value);
            }

            @Override
            public Object compute(EvalContext context) {
                return computeValue(context);
            }

            @Override
            public boolean isContextDependent() {
                return false;
            }
        };
    }

    // Helper method to create a constant expression with a null value
    private Expression createNullExpression() {
        return new Expression() {
            @Override
            public Object computeValue(EvalContext context) {
                return null;
            }

            @Override
            public Object compute(EvalContext context) {
                return computeValue(context);
            }

            @Override
            public boolean isContextDependent() {
                return false;
            }
        };
    }

    // Helper method to create a constant expression with an integer value
    private Expression createIntegerExpression(final int value) {
        return new Expression() {
            @Override
            public Object computeValue(EvalContext context) {
                return new Integer(value);
            }

            @Override
            public Object compute(EvalContext context) {
                return computeValue(context);
            }

            @Override
            public boolean isContextDependent() {
                return false;
            }
        };
    }

    // Helper method to evaluate the comparison
    private boolean evaluate(Expression left, Expression right) {
        CoreOperationLessThanOrEqual op = new CoreOperationLessThanOrEqual(left, right);
        Object result = op.computeValue(null);
        return ((Boolean) result).booleanValue();
    }

    // Tests normal case: left < right, should return true
    @Test
    public void testComputeValue_lessThan_returnsTrue() {
        Expression left = createConstantExpression(1.0);
        Expression right = createConstantExpression(5.0);
        assertTrue("1.0 <= 5.0 should be true", evaluate(left, right));
    }

    // Tests normal case: left == right, should return true
    @Test
    public void testComputeValue_equalValues_returnsTrue() {
        Expression left = createConstantExpression(3.0);
        Expression right = createConstantExpression(3.0);
        assertTrue("3.0 <= 3.0 should be true", evaluate(left, right));
    }

    // Tests normal case: left > right, should return false
    @Test
    public void testComputeValue_greaterThan_returnsFalse() {
        Expression left = createConstantExpression(10.0);
        Expression right = createConstantExpression(2.0);
        assertFalse("10.0 <= 2.0 should be false", evaluate(left, right));
    }

    // Tests boundary case: left is double max value
    @Test
    public void testComputeValue_maxDouble_returnsCorrectResult() {
        Expression left = createConstantExpression(Double.MAX_VALUE);
        Expression right = createConstantExpression(Double.MAX_VALUE);
        assertTrue("Double.MAX_VALUE <= Double.MAX_VALUE should be true", evaluate(left, right));
    }

    // Tests boundary case: left is double min value
    @Test
    public void testComputeValue_minDouble_returnsCorrectResult() {
        Expression left = createConstantExpression(Double.MIN_VALUE);
        Expression right = createConstantExpression(Double.MIN_VALUE);
        assertTrue("Double.MIN_VALUE <= Double.MIN_VALUE should be true", evaluate(left, right));
    }

    // Tests boundary case: left is very small negative number
    @Test
    public void testComputeValue_negativeBoundary_returnsCorrectResult() {
        Expression left = createConstantExpression(-1.0);
        Expression right = createConstantExpression(-0.5);
        assertTrue("-1.0 <= -0.5 should be true", evaluate(left, right));
    }

    // Tests boundary case: left is zero
    @Test
    public void testComputeValue_zero_returnsCorrectResult() {
        Expression left = createConstantExpression(0.0);
        Expression right = createConstantExpression(0.0);
        assertTrue("0.0 <= 0.0 should be true", evaluate(left, right));
    }

    // Tests edge case: left is negative infinity
    @Test
    public void testComputeValue_negativeInfinity_returnsCorrectResult() {
        Expression left = createConstantExpression(Double.NEGATIVE_INFINITY);
        Expression right = createConstantExpression(Double.POSITIVE_INFINITY);
        assertTrue("-Infinity <= +Infinity should be true", evaluate(left, right));
    }

    // Tests edge case: left is positive infinity
    @Test
    public void testComputeValue_positiveInfinity_returnsCorrectResult() {
        Expression left = createConstantExpression(Double.POSITIVE_INFINITY);
        Expression right = createConstantExpression(Double.NEGATIVE_INFINITY);
        assertFalse("+Infinity <= -Infinity should be false", evaluate(left, right));
    }

    // Tests edge case: left is NaN - NaN comparison should always return false
    @Test
    public void testComputeValue_nan_returnsFalse() {
        Expression left = createConstantExpression(Double.NaN);
        Expression right = createConstantExpression(Double.NaN);
        assertFalse("NaN <= NaN should be false", evaluate(left, right));
    }

    // Tests edge case: left is NaN, right is a number - should return false
    @Test
    public void testComputeValue_nanVsNumber_returnsFalse() {
        Expression left = createConstantExpression(Double.NaN);
        Expression right = createConstantExpression(5.0);
        assertFalse("NaN <= 5.0 should be false", evaluate(left, right));
    }

    // Tests edge case: left is a number, right is NaN - should return false
    @Test
    public void testComputeValue_numberVsNaN_returnsFalse() {
        Expression left = createConstantExpression(5.0);
        Expression right = createConstantExpression(Double.NaN);
        assertFalse("5.0 <= NaN should be false", evaluate(left, right));
    }

    // Tests edge case: left is null - InfoSetUtil.doubleValue(null) returns 0.0
    @Test
    public void testComputeValue_nullLeft_returnsCorrectResult() {
        Expression left = createNullExpression();
        Expression right = createConstantExpression(0.0);
        assertTrue("null <= 0.0 should be true (null becomes 0.0)", evaluate(left, right));
    }

    // Tests edge case: right is null - InfoSetUtil.doubleValue(null) returns 0.0
    @Test
    public void testComputeValue_nullRight_returnsCorrectResult() {
        Expression left = createConstantExpression(-1.0);
        Expression right = createNullExpression();
        assertTrue("-1.0 <= null should be true (null becomes 0.0)", evaluate(left, right));
    }

    // Tests edge case: both are null - both become 0.0, so 0.0 <= 0.0 should be true
    @Test
    public void testComputeValue_bothNull_returnsTrue() {
        Expression left = createNullExpression();
        Expression right = createNullExpression();
        assertTrue("null <= null should be true (both become 0.0)", evaluate(left, right));
    }

    // Tests with integer values: left is negative integer
    @Test
    public void testComputeValue_negativeInteger_returnsCorrectResult() {
        Expression left = createIntegerExpression(-5);
        Expression right = createIntegerExpression(0);
        assertTrue("-5 <= 0 should be true", evaluate(left, right));
    }

    // Tests with integer values: left is positive integer
    @Test
    public void testComputeValue_positiveInteger_returnsCorrectResult() {
        Expression left = createIntegerExpression(5);
        Expression right = createIntegerExpression(3);
        assertFalse("5 <= 3 should be false", evaluate(left, right));
    }

    // Tests getSymbol method - should return "<="
    @Test
    public void testGetSymbol_returnsCorrectSymbol() {
        Expression left = createConstantExpression(1.0);
        Expression right = createConstantExpression(2.0);
        CoreOperationLessThanOrEqual op = new CoreOperationLessThanOrEqual(left, right);
        assertEquals("Symbol should be <=", "<=", op.getSymbol());
    }
}