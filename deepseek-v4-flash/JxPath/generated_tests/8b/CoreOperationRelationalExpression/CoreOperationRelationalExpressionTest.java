package org.apache.commons.jxpath.ri.compiler;

import static org.junit.Assert.*;

import java.util.Iterator;

import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.InfoSetUtil;
import org.junit.Test;

public class CoreOperationRelationalExpressionTest {

    // Helper concrete class to instantiate the abstract class
    private static class TestCoreOperationRelationalExpression extends CoreOperationRelationalExpression {
        private final int compareResult;

        protected TestCoreOperationRelationalExpression(Expression[] args, int compareResult) {
            super(args);
            this.compareResult = compareResult;
        }

        @Override
        protected boolean evaluateCompare(int compare) {
            return compare == compareResult;
        }

        @Override
        public String getSymbol() {
            return "?";
        }
    }

    // Helper expression that returns a constant object
    private static class ConstantExpression extends Expression {
        private final Object value;

        ConstantExpression(Object value) {
            this.value = value;
        }

        @Override
        public Object compute(EvalContext context) {
            return computeValue(context);
        }

        @Override
        public Object computeValue(EvalContext context) {
            return value;
        }

        @Override
        public boolean computeContextDependent() {
            return false;
        }
    }

    // Tests compute method: null left
    @Test
    public void testCompute_nullLeft_usesDoubleValue() {
        ConstantExpression left = new ConstantExpression(null);
        ConstantExpression right = new ConstantExpression(Double.valueOf(1.0));
        TestCoreOperationRelationalExpression op = new TestCoreOperationRelationalExpression(
                new Expression[] { left, right }, -1);
        assertEquals(Boolean.TRUE, op.computeValue(new MockEvalContext()));
    }

    // Tests compute method: null right
    @Test
    public void testCompute_nullRight_usesDoubleValue() {
        ConstantExpression left = new ConstantExpression(Double.valueOf(5.0));
        ConstantExpression right = new ConstantExpression(null);
        TestCoreOperationRelationalExpression op = new TestCoreOperationRelationalExpression(
                new Expression[] { left, right }, 1);
        assertEquals(Boolean.TRUE, op.computeValue(new MockEvalContext()));
    }

    // Tests compute method: equal doubles
    @Test
    public void testCompute_equalDoubles_returnsTrue() {
        ConstantExpression left = new ConstantExpression(Double.valueOf(3.0));
        ConstantExpression right = new ConstantExpression(Double.valueOf(3.0));
        TestCoreOperationRelationalExpression op = new TestCoreOperationRelationalExpression(
                new Expression[] { left, right }, 0);
        assertEquals(Boolean.TRUE, op.computeValue(new MockEvalContext()));
    }

    // Tests compute method: left less than right, evaluateCompare returns true for -1
    @Test
    public void testCompute_leftLessThanRight_evaluateCompareTrue() {
        ConstantExpression left = new ConstantExpression(Double.valueOf(1.0));
        ConstantExpression right = new ConstantExpression(Double.valueOf(5.0));
        TestCoreOperationRelationalExpression op = new TestCoreOperationRelationalExpression(
                new Expression[] { left, right }, -1);
        assertEquals(Boolean.TRUE, op.computeValue(new MockEvalContext()));
    }

    // Tests compute method: left less than right, evaluateCompare returns false for -1
    @Test
    public void testCompute_leftLessThanRight_evaluateCompareFalse() {
        ConstantExpression left = new ConstantExpression(Double.valueOf(1.0));
        ConstantExpression right = new ConstantExpression(Double.valueOf(5.0));
        TestCoreOperationRelationalExpression op = new TestCoreOperationRelationalExpression(
                new Expression[] { left, right }, 1);
        assertEquals(Boolean.FALSE, op.computeValue(new MockEvalContext()));
    }

    // Tests compute method: left greater than right, evaluateCompare returns true for 1
    @Test
    public void testCompute_leftGreaterThanRight_evaluateCompareTrue() {
        ConstantExpression left = new ConstantExpression(Double.valueOf(10.0));
        ConstantExpression right = new ConstantExpression(Double.valueOf(3.0));
        TestCoreOperationRelationalExpression op = new TestCoreOperationRelationalExpression(
                new Expression[] { left, right }, 1);
        assertEquals(Boolean.TRUE, op.computeValue(new MockEvalContext()));
    }

    // Tests compute method: left greater than right, evaluateCompare returns false for 1
    @Test
    public void testCompute_leftGreaterThanRight_evaluateCompareFalse() {
        ConstantExpression left = new ConstantExpression(Double.valueOf(10.0));
        ConstantExpression right = new ConstantExpression(Double.valueOf(3.0));
        TestCoreOperationRelationalExpression op = new TestCoreOperationRelationalExpression(
                new Expression[] { left, right }, -1);
        assertEquals(Boolean.FALSE, op.computeValue(new MockEvalContext()));
    }

    // Tests compute method: left equals right, evaluateCompare returns true for 0
    @Test
    public void testCompute_leftEqualsRight_evaluateCompareTrue() {
        ConstantExpression left = new ConstantExpression(Double.valueOf(7.0));
        ConstantExpression right = new ConstantExpression(Double.valueOf(7.0));
        TestCoreOperationRelationalExpression op = new TestCoreOperationRelationalExpression(
                new Expression[] { left, right }, 0);
        assertEquals(Boolean.TRUE, op.computeValue(new MockEvalContext()));
    }

    // Tests compute method: left equals right, evaluateCompare returns false for 0
    @Test
    public void testCompute_leftEqualsRight_evaluateCompareFalse() {
        ConstantExpression left = new ConstantExpression(Double.valueOf(7.0));
        ConstantExpression right = new ConstantExpression(Double.valueOf(7.0));
        TestCoreOperationRelationalExpression op = new TestCoreOperationRelationalExpression(
                new Expression[] { left, right }, 1);
        assertEquals(Boolean.FALSE, op.computeValue(new MockEvalContext()));
    }

    // Tests compute method: left is an Iterator, right is not an Iterator containsMatch returns true
    @Test
    public void testCompute_leftIsIterator_containsMatchReturnsTrue() {
        ConstantExpression left = new ConstantExpression(java.util.Arrays.asList(1.0, 2.0).iterator());
        ConstantExpression right = new ConstantExpression(Double.valueOf(2.0));
        TestCoreOperationRelationalExpression op = new TestCoreOperationRelationalExpression(
                new Expression[] { left, right }, 0);
        assertEquals(Boolean.TRUE, op.computeValue(new MockEvalContext()));
    }

    // Tests compute method: left is Iterator, right is not an Iterator containsMatch returns false
    @Test
    public void testCompute_leftIsIterator_containsMatchReturnsFalse() {
        ConstantExpression left = new ConstantExpression(java.util.Arrays.asList(1.0, 3.0).iterator());
        ConstantExpression right = new ConstantExpression(Double.valueOf(2.0));
        TestCoreOperationRelationalExpression op = new TestCoreOperationRelationalExpression(
                new Expression[] { left, right }, 0);
        assertEquals(Boolean.FALSE, op.computeValue(new MockEvalContext()));
    }

    // Tests compute method: right is Iterator, left is not an Iterator containsMatch returns true
    @Test
    public void testCompute_rightIsIterator_containsMatchReturnsTrue() {
        ConstantExpression left = new ConstantExpression(Double.valueOf(3.0));
        ConstantExpression right = new ConstantExpression(java.util.Arrays.asList(3.0, 4.0).iterator());
        TestCoreOperationRelationalExpression op = new TestCoreOperationRelationalExpression(
                new Expression[] { left, right }, 0);
        assertEquals(Boolean.TRUE, op.computeValue(new MockEvalContext()));
    }

    // Tests compute method: both are Iterator, findMatch returns true
    @Test
    public void testCompute_bothIterators_findMatchReturnsTrue() {
        ConstantExpression left = new ConstantExpression(java.util.Arrays.asList(1.0, 5.0).iterator());
        ConstantExpression right = new ConstantExpression(java.util.Arrays.asList(5.0, 10.0).iterator());
        TestCoreOperationRelationalExpression op = new TestCoreOperationRelationalExpression(
                new Expression[] { left, right }, 0);
        assertEquals(Boolean.TRUE, op.computeValue(new MockEvalContext()));
    }

    // Tests compute method: both are Iterator, findMatch returns false
    @Test
    public void testCompute_bothIterators_findMatchReturnsFalse() {
        ConstantExpression left = new ConstantExpression(java.util.Arrays.asList(1.0, 2.0).iterator());
        ConstantExpression right = new ConstantExpression(java.util.Arrays.asList(3.0, 4.0).iterator());
        TestCoreOperationRelationalExpression op = new TestCoreOperationRelationalExpression(
                new Expression[] { left, right }, 0);
        assertEquals(Boolean.FALSE, op.computeValue(new MockEvalContext()));
    }

    // Tests compute method: string conversion to double
    @Test
    public void testCompute_stringToDouble_conversionWorks() {
        ConstantExpression left = new ConstantExpression("10");
        ConstantExpression right = new ConstantExpression(Double.valueOf(10.0));
        TestCoreOperationRelationalExpression op = new TestCoreOperationRelationalExpression(
                new Expression[] { left, right }, 0);
        assertEquals(Boolean.TRUE, op.computeValue(new MockEvalContext()));
    }

    // Mock EvalContext (simple, enough to satisfy null return)
    private static class MockEvalContext extends EvalContext {
        public MockEvalContext() {
            super(null);
        }

        @Override
        public Object getSingleNodePointer() {
            return null;
        }

        @Override
        public boolean nextNode() {
            return false;
        }

        @Override
        public int getCurrentPosition() {
            return 0;
        }

        @Override
        public boolean setPosition(int position) {
            return false;
        }

        @Override
        public Object getValue() {
            return null;
        }

        @Override
        public Object getContextNodePointer() {
            return null;
        }

        @Override
        public Object getJXPathContext() {
            return null;
        }

        @Override
        public Object getRootNode() {
            return null;
        }

        @Override
        public Object getNodeSet() {
            return null;
        }
    }
}