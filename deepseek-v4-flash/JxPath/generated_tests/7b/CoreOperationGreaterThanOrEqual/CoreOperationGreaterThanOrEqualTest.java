package org.apache.commons.jxpath.ri.compiler;

import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.InfoSetUtil;

public class CoreOperationGreaterThanOrEqualTest {

    // Helper method: create a simple Expression that returns a constant value
    private Expression createConstant(final Object value) {
        return new Expression() {
            public Object computeValue(EvalContext context) {
                return value;
            }
            public Expression getExpression(int index) { return null; }
            public void setExpression(int index, Expression expr) {}
            public boolean isContextDependent() { return false; }
            public boolean computeContextDependent() { return false; }
            public Object compute(EvalContext context) { return computeValue(context); }
            public boolean isSimple() { return false; }
            public String asString() { return String.valueOf(value); }
            public int getLength() { return 1; }
            public Expression[] getArguments() { return new Expression[] { this }; }
            public void setArguments(Expression[] args) {}
        };
    }

    // Tests normal case: greater than
    @Test
    public void testComputeValue_greaterThan_returnsTrue() {
        Expression left = createConstant(5.0);
        Expression right = createConstant(3.0);
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(left, right);
        assertEquals(Boolean.TRUE, op.computeValue(null));
    }

    // Tests normal case: equal
    @Test
    public void testComputeValue_equal_returnsTrue() {
        Expression left = createConstant(4.0);
        Expression right = createConstant(4.0);
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(left, right);
        assertEquals(Boolean.TRUE, op.computeValue(null));
    }

    // Tests normal case: less than
    @Test
    public void testComputeValue_lessThan_returnsFalse() {
        Expression left = createConstant(2.0);
        Expression right = createConstant(7.0);
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(left, right);
        assertEquals(Boolean.FALSE, op.computeValue(null));
    }

    // Tests boundary: zero vs zero
    @Test
    public void testComputeValue_zeroAndZero_returnsTrue() {
        Expression left = createConstant(0.0);
        Expression right = createConstant(0.0);
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(left, right);
        assertEquals(Boolean.TRUE, op.computeValue(null));
    }

    // Tests boundary: negative values
    @Test
    public void testComputeValue_negativeGreaterThanNegative_returnsTrue() {
        Expression left = createConstant(-1.0);
        Expression right = createConstant(-3.0);
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(left, right);
        assertEquals(Boolean.TRUE, op.computeValue(null));
    }

    // Tests boundary: negative equal
    @Test
    public void testComputeValue_negativeEqual_returnsTrue() {
        Expression left = createConstant(-5.0);
        Expression right = createConstant(-5.0);
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(left, right);
        assertEquals(Boolean.TRUE, op.computeValue(null));
    }

    // Tests edge case: null input from left expression (InfoSetUtil.doubleValue returns NaN)
    @Test
    public void testComputeValue_nullLeft_returnsFalse() {
        Expression left = createConstant(null);
        Expression right = createConstant(1.0);
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(left, right);
        // NaN >= 1.0 is false
        assertEquals(Boolean.FALSE, op.computeValue(null));
    }

    // Tests edge case: null input from right expression
    @Test
    public void testComputeValue_nullRight_returnsFalse() {
        Expression left = createConstant(1.0);
        Expression right = createConstant(null);
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(left, right);
        // 1.0 >= NaN is false
        assertEquals(Boolean.FALSE, op.computeValue(null));
    }

    // Tests edge case: both null
    @Test
    public void testComputeValue_bothNull_returnsFalse() {
        Expression left = createConstant(null);
        Expression right = createConstant(null);
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(left, right);
        // NaN >= NaN is false
        assertEquals(Boolean.FALSE, op.computeValue(null));
    }

    // Tests getSymbol method
    @Test
    public void testGetSymbol_returnsCorrectString() {
        Expression left = createConstant(1.0);
        Expression right = createConstant(2.0);
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(left, right);
        assertEquals(">=", op.getSymbol());
    }

    // Tests edge case: large numbers
    @Test
    public void testComputeValue_largeNumbers_returnsTrue() {
        Expression left = createConstant(1e100);
        Expression right = createConstant(1e99);
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(left, right);
        assertEquals(Boolean.TRUE, op.computeValue(null));
    }

    // Tests edge case: negative large numbers
    @Test
    public void testComputeValue_negativeLargeNumbers_returnsFalse() {
        Expression left = createConstant(-1e100);
        Expression right = createConstant(-1e99);
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(left, right);
        assertEquals(Boolean.FALSE, op.computeValue(null));
    }

    // Additional coverage: Boolean conversion
    @Test
    public void testComputeValue_booleanGreaterThan_returnsTrue() {
        Expression left = createConstant(Boolean.TRUE);
        Expression right = createConstant(Boolean.FALSE);
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(left, right);
        assertEquals(Boolean.TRUE, op.computeValue(null));
    }

    @Test
    public void testComputeValue_booleanLessThan_returnsFalse() {
        Expression left = createConstant(Boolean.FALSE);
        Expression right = createConstant(Boolean.TRUE);
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(left, right);
        assertEquals(Boolean.FALSE, op.computeValue(null));
    }

    // Additional coverage: String conversion
    @Test
    public void testComputeValue_stringGreaterThan_returnsTrue() {
        Expression left = createConstant("5.5");
        Expression right = createConstant("3.5");
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(left, right);
        assertEquals(Boolean.TRUE, op.computeValue(null));
    }

    @Test
    public void testComputeValue_stringLessThan_returnsFalse() {
        Expression left = createConstant("2.5");
        Expression right = createConstant("10.0");
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(left, right);
        assertEquals(Boolean.FALSE, op.computeValue(null));
    }

    // Additional coverage: inherited CoreOperation methods
    @Test
    public void testGetArguments_returnsOperands() {
        Expression left = createConstant(1.0);
        Expression right = createConstant(2.0);
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(left, right);
        Expression[] args = op.getArguments();
        assertEquals(2, args.length);
        assertSame(left, args[0]);
        assertSame(right, args[1]);
    }

    @Test
    public void testGetLength_returnsTwo() {
        Expression left = createConstant(1.0);
        Expression right = createConstant(2.0);
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(left, right);
        assertEquals(2, op.getLength());
    }

    @Test
    public void testCompute_delegatesToComputeValue() {
        Expression left = createConstant(2.0);
        Expression right = createConstant(1.0);
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(left, right);
        assertEquals(Boolean.TRUE, op.compute(null));
    }

    @Test
    public void testAsString_containsSymbol() {
        Expression left = createConstant(1.0);
        Expression right = createConstant(2.0);
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(left, right);
        assertTrue(op.asString().contains(">="));
    }

    @Test
    public void testIsSimple_returnsFalse() {
        Expression left = createConstant(1.0);
        Expression right = createConstant(2.0);
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(left, right);
        assertFalse(op.isSimple());
    }

    @Test
    public void testIsContextDependent_withConstantOperands_returnsFalse() {
        Expression left = createConstant(1.0);
        Expression right = createConstant(2.0);
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(left, right);
        assertFalse(op.isContextDependent());
    }

    @Test
    public void testComputeContextDependent_withConstantOperands_returnsFalse() {
        Expression left = createConstant(1.0);
        Expression right = createConstant(2.0);
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(left, right);
        assertFalse(op.computeContextDependent());
    }

    // Additional coverage: precedence and symmetry
    @Test
    public void testGetPrecedence_returnsRelationalPrecedence() {
        Expression left = createConstant(1.0);
        Expression right = createConstant(2.0);
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(left, right);
        assertEquals(4, op.getPrecedence());
    }

    @Test
    public void testIsSymmetric_returnsFalse() {
        Expression left = createConstant(1.0);
        Expression right = createConstant(2.0);
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(left, right);
        assertFalse(op.isSymmetric());
    }
}