package org.apache.commons.jxpath.ri.compiler;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.InfoSetUtil;
import org.apache.commons.jxpath.ri.axes.InitialContext;
import org.apache.commons.jxpath.ri.axes.SelfContext;
import org.junit.Test;

public class CoreOperationRelationalExpressionTest {

    // Helper concrete subclass for testing
    private static class TestRelationalExpression extends CoreOperationRelationalExpression {
        private final int comparisonResult;

        TestRelationalExpression(Expression[] args, int comparisonResult) {
            super(args);
            this.comparisonResult = comparisonResult;
        }

        @Override
        protected boolean evaluateCompare(int compare) {
            return compare == comparisonResult;
        }

        @Override
        public String getSymbol() {
            return "test";
        }
    }

    private static class ConstantExpression extends Expression {
        private final Object value;

        ConstantExpression(Object value) {
            this.value = value;
        }

        @Override
        public String asString() {
            return String.valueOf(value);
        }

        @Override
        public Object compute(EvalContext context) {
            return value;
        }

        @Override
        public Object computeValue(EvalContext context) {
            return value;
        }

        @Override
        public boolean isContextDependent() {
            return false;
        }

        @Override
        public String toString() {
            return asString();
        }
    }

    // Tests compute with two numeric values, evaluates to true
    @Test
    public void testCompute_twoNumbersEqual_returnsTrue() {
        TestRelationalExpression expr = new TestRelationalExpression(new Expression[]{
                new ConstantExpression(5.0),
                new ConstantExpression(5.0)
        }, 0);
        assertTrue((Boolean) expr.computeValue(new MockEvalContext()));
    }

    // Tests compute with two numeric values, evaluates to false
    @Test
    public void testCompute_twoNumbersNotEqual_returnsFalse() {
        TestRelationalExpression expr = new TestRelationalExpression(new Expression[]{
                new ConstantExpression(3.0),
                new ConstantExpression(7.0)
        }, 0);
        assertFalse((Boolean) expr.computeValue(new MockEvalContext()));
    }

    // Tests compute with left value less than right, using evaluateCompare for -1
    @Test
    public void testCompute_leftLessThanRight_returnsTrue() {
        TestRelationalExpression expr = new TestRelationalExpression(new Expression[]{
                new ConstantExpression(2.0),
                new ConstantExpression(5.0)
        }, -1);
        assertTrue((Boolean) expr.computeValue(new MockEvalContext()));
    }

    // Tests compute with left value greater than right, using evaluateCompare for 1
    @Test
    public void testCompute_leftGreaterThanRight_returnsTrue() {
        TestRelationalExpression expr = new TestRelationalExpression(new Expression[]{
                new ConstantExpression(8.0),
                new ConstantExpression(3.0)
        }, 1);
        assertTrue((Boolean) expr.computeValue(new MockEvalContext()));
    }

    // Tests compute with NaN left value
    @Test
    public void testCompute_leftNaN_returnsFalse() {
        TestRelationalExpression expr = new TestRelationalExpression(new Expression[]{
                new ConstantExpression(Double.NaN),
                new ConstantExpression(5.0)
        }, 0);
        assertFalse((Boolean) expr.computeValue(new MockEvalContext()));
    }

    // Tests compute with NaN right value
    @Test
    public void testCompute_rightNaN_returnsFalse() {
        TestRelationalExpression expr = new TestRelationalExpression(new Expression[]{
                new ConstantExpression(5.0),
                new ConstantExpression(Double.NaN)
        }, 1);
        assertFalse((Boolean) expr.computeValue(new MockEvalContext()));
    }

    // Tests compute with self context (SelfContext) being reduced
    @Test
    public void testCompute_leftSelfContext_returnsCorrectResult() {
        TestRelationalExpression expr = new TestRelationalExpression(new Expression[]{
                new ConstantExpression(new SelfContext(null, null)),
                new ConstantExpression(new SelfContext(null, null))
        }, 0);
        // SelfContext with null pointer returns NaN so double comparison will be false
        assertFalse((Boolean) expr.computeValue(new MockEvalContext()));
    }

    // Tests compute with collection being reduced to iterator
    @Test
    public void testCompute_leftCollection_reducedToIterator() {
        Collection col = Arrays.asList(1.0, 2.0, 3.0);
        TestRelationalExpression expr = new TestRelationalExpression(new Expression[]{
                new ConstantExpression(col),
                new ConstantExpression(2.0)
        }, 0);
        assertTrue((Boolean) expr.computeValue(new MockEvalContext()));
    }

    // Tests compute with both sides as iterators (findMatch path)
    @Test
    public void testCompute_bothIterators_findsMatch_returnsTrue() {
        Collection leftCol = Arrays.asList(1.0, 2.0, 3.0);
        Collection rightCol = Arrays.asList(3.0, 4.0, 5.0);
        TestRelationalExpression expr = new TestRelationalExpression(new Expression[]{
                new ConstantExpression(leftCol),
                new ConstantExpression(rightCol)
        }, 0);
        assertTrue((Boolean) expr.computeValue(new MockEvalContext()));
    }

    // Tests compute with both sides as iterators (findMatch path) no match
    @Test
    public void testCompute_bothIterators_noMatch_returnsFalse() {
        Collection leftCol = Arrays.asList(1.0, 2.0);
        Collection rightCol = Arrays.asList(3.0, 4.0, 5.0);
        TestRelationalExpression expr = new TestRelationalExpression(new Expression[]{
                new ConstantExpression(leftCol),
                new ConstantExpression(rightCol)
        }, 0);
        assertFalse((Boolean) expr.computeValue(new MockEvalContext()));
    }

    // Tests compute with left iterator, right value (containsMatch path)
    @Test
    public void testCompute_leftIteratorRightValue_containsMatch_returnsTrue() {
        Collection col = Arrays.asList(1.0, 3.0, 5.0);
        TestRelationalExpression expr = new TestRelationalExpression(new Expression[]{
                new ConstantExpression(col),
                new ConstantExpression(3.0)
        }, 0);
        assertTrue((Boolean) expr.computeValue(new MockEvalContext()));
    }

    // Tests compute with left iterator, right value, no match
    @Test
    public void testCompute_leftIteratorRightValue_noMatch_returnsFalse() {
        Collection col = Arrays.asList(1.0, 2.0, 3.0);
        TestRelationalExpression expr = new TestRelationalExpression(new Expression[]{
                new ConstantExpression(col),
                new ConstantExpression(5.0)
        }, 0);
        assertFalse((Boolean) expr.computeValue(new MockEvalContext()));
    }

    // Tests compute with right iterator, left value (containsMatch path)
    @Test
    public void testCompute_rightIteratorLeftValue_containsMatch_returnsTrue() {
        Collection col = Arrays.asList(10.0, 20.0, 30.0);
        TestRelationalExpression expr = new TestRelationalExpression(new Expression[]{
                new ConstantExpression(20.0),
                new ConstantExpression(col)
        }, 0);
        assertTrue((Boolean) expr.computeValue(new MockEvalContext()));
    }

    // Tests compute with right iterator, left value, no match
    @Test
    public void testCompute_rightIteratorLeftValue_noMatch_returnsFalse() {
        Collection col = Arrays.asList(10.0, 20.0, 30.0);
        TestRelationalExpression expr = new TestRelationalExpression(new Expression[]{
                new ConstantExpression(25.0),
                new ConstantExpression(col)
        }, 0);
        assertFalse((Boolean) expr.computeValue(new MockEvalContext()));
    }

    // Tests getPrecedence returns 3
    @Test
    public void testGetPrecedence_returns3() {
        TestRelationalExpression expr = new TestRelationalExpression(new Expression[]{
                new ConstantExpression(1),
                new ConstantExpression(2)
        }, 0);
        assertEquals(3, expr.getPrecedence());
    }

    // Tests isSymmetric returns false
    @Test
    public void testIsSymmetric_returnsFalse() {
        TestRelationalExpression expr = new TestRelationalExpression(new Expression[]{
                new ConstantExpression(1),
                new ConstantExpression(2)
        }, 0);
        assertFalse(expr.isSymmetric());
    }

    // Mock EvalContext for testing computeValue
    private static class MockEvalContext extends EvalContext {
        public MockEvalContext() {
            super(null);
        }

        @Override
        public Pointer getCurrentPointer() {
            return null;
        }

        @Override
        public Pointer getCurrentNodePointer() {
            return null;
        }

        @Override
        public boolean nextNode() {
            return false;
        }

        @Override
        public boolean nextSet() {
            return false;
        }

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public Object next() {
            return null;
        }

        @Override
        public void remove() {}
    }
}