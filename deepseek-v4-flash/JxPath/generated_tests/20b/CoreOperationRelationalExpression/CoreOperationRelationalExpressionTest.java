package org.apache.commons.jxpath.ri.compiler;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.InfoSetUtil;
import org.apache.commons.jxpath.ri.axes.InitialContext;
import org.apache.commons.jxpath.ri.axes.SelfContext;
import org.junit.Test;

/**
 * Test class for CoreOperationRelationalExpression.
 */
public class CoreOperationRelationalExpressionTest {

    // Concrete subclass for testing - implements evaluateCompare
    private static class TestCoreOperationRelationalExpression extends CoreOperationRelationalExpression {
        private final int comparisonResult;

        TestCoreOperationRelationalExpression(Expression[] args, int comparisonResult) {
            super(args);
            this.comparisonResult = comparisonResult;
        }

        @Override
        protected boolean evaluateCompare(int compare) {
            return compare == comparisonResult;
        }

        @Override
        public String getOperationName() {
            return "test";
        }

        @Override
        public String getSymbol() {
            return "test";
        }
    }

    // Minimal Expression implementation for testing
    private static class TestExpression extends Expression {
        private final Object value;

        TestExpression(Object value) {
            this.value = value;
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
        public boolean isContextDependentForInfoStep() {
            return false;
        }

        @Override
        public String asString() {
            return String.valueOf(value);
        }
    }

    // Tests computeValue with basic numeric comparison
    @Test
    public void testComputeValue_basicNumericComparison_returnsCorrectBoolean() {
        Expression[] args = new Expression[]{new TestExpression(5.0), new TestExpression(3.0)};
        TestCoreOperationRelationalExpression expr = new TestCoreOperationRelationalExpression(args, 1);
        assertTrue((Boolean) expr.computeValue(null));
    }

    // Tests computeValue with equal numbers
    @Test
    public void testComputeValue_equalNumbers_returnsCorrectBoolean() {
        Expression[] args = new Expression[]{new TestExpression(5.0), new TestExpression(5.0)};
        TestCoreOperationRelationalExpression expr = new TestCoreOperationRelationalExpression(args, 0);
        assertTrue((Boolean) expr.computeValue(null));
    }

    // Tests computeValue with NaN left operand
    @Test
    public void testComputeValue_nanLeftOperand_returnsFalse() {
        Expression[] args = new Expression[]{new TestExpression(Double.NaN), new TestExpression(3.0)};
        TestCoreOperationRelationalExpression expr = new TestCoreOperationRelationalExpression(args, 1);
        assertFalse((Boolean) expr.computeValue(null));
    }

    // Tests computeValue with NaN right operand
    @Test
    public void testComputeValue_nanRightOperand_returnsFalse() {
        Expression[] args = new Expression[]{new TestExpression(5.0), new TestExpression(Double.NaN)};
        TestCoreOperationRelationalExpression expr = new TestCoreOperationRelationalExpression(args, 1);
        assertFalse((Boolean) expr.computeValue(null));
    }

    // Tests computeValue with both NaN
    @Test
    public void testComputeValue_bothNan_returnsFalse() {
        Expression[] args = new Expression[]{new TestExpression(Double.NaN), new TestExpression(Double.NaN)};
        TestCoreOperationRelationalExpression expr = new TestCoreOperationRelationalExpression(args, 0);
        assertFalse((Boolean) expr.computeValue(null));
    }

    // Tests computeValue with string representation of numbers
    @Test
    public void testComputeValue_stringNumbers_returnsCorrectBoolean() {
        Expression[] args = new Expression[]{new TestExpression("5"), new TestExpression("3")};
        TestCoreOperationRelationalExpression expr = new TestCoreOperationRelationalExpression(args, 1);
        assertTrue((Boolean) expr.computeValue(null));
    }

    // Tests computeValue with null operands
    @Test
    public void testComputeValue_nullOperands_returnsFalse() {
        Expression[] args = new Expression[]{new TestExpression(null), new TestExpression(null)};
        TestCoreOperationRelationalExpression expr = new TestCoreOperationRelationalExpression(args, 0);
        assertFalse((Boolean) expr.computeValue(null));
    }

    // Tests computeValue with iterator left operand - returns an iterator
    @Test
    public void testComputeValue_iteratorLeftOperand_returnsCorrectBoolean() {
        Collection<Object> col = new ArrayList<Object>();
        col.add(5.0);
        col.add(3.0);
        Expression[] args = new Expression[]{new TestExpression(col.iterator()), new TestExpression(5.0)};
        TestCoreOperationRelationalExpression expr = new TestCoreOperationRelationalExpression(args, 0);
        assertTrue((Boolean) expr.computeValue(null));
    }

    // Tests computeValue with iterator right operand
    @Test
    public void testComputeValue_iteratorRightOperand_returnsCorrectBoolean() {
        Expression[] args = new Expression[]{new TestExpression(5.0), new TestExpression(new TestIterator(5.0))};
        TestCoreOperationRelationalExpression expr = new TestCoreOperationRelationalExpression(args, 0);
        assertTrue((Boolean) expr.computeValue(null));
    }

    // Tests computeValue with both iterator operands - findMatch path
    @Test
    public void testComputeValue_bothIterators_returnsCorrectBoolean() {
        Collection<Object> leftCol = new ArrayList<Object>();
        leftCol.add(5.0);
        leftCol.add(3.0);
        Collection<Object> rightCol = new ArrayList<Object>();
        rightCol.add(7.0);
        rightCol.add(5.0);
        Expression[] args = new Expression[]{new TestExpression(leftCol.iterator()), new TestExpression(rightCol.iterator())};
        TestCoreOperationRelationalExpression expr = new TestCoreOperationRelationalExpression(args, 0);
        assertTrue((Boolean) expr.computeValue(null));
    }

    // Tests computeValue with both iterators no match
    @Test
    public void testComputeValue_bothIteratorsNoMatch_returnsFalse() {
        Collection<Object> leftCol = new ArrayList<Object>();
        leftCol.add(1.0);
        leftCol.add(2.0);
        Collection<Object> rightCol = new ArrayList<Object>();
        rightCol.add(3.0);
        rightCol.add(4.0);
        Expression[] args = new Expression[]{new TestExpression(leftCol.iterator()), new TestExpression(rightCol.iterator())};
        TestCoreOperationRelationalExpression expr = new TestCoreOperationRelationalExpression(args, 0);
        assertFalse((Boolean) expr.computeValue(null));
    }

    // Tests computeValue with SelfContext reduction
    @Test
    public void testComputeValue_selfContextReduction_returnsCorrectBoolean() {
        Expression[] args = new Expression[]{new TestExpression(new TestSelfContext()), new TestExpression(5.0)};
        TestCoreOperationRelationalExpression expr = new TestCoreOperationRelationalExpression(args, 1);
        assertTrue((Boolean) expr.computeValue(null));
    }

    // Tests computeValue with Collection operand
    @Test
    public void testComputeValue_collectionOperand_returnsCorrectBoolean() {
        Collection<Object> col = new ArrayList<Object>();
        col.add(5.0);
        Expression[] args = new Expression[]{new TestExpression(col), new TestExpression(5.0)};
        TestCoreOperationRelationalExpression expr = new TestCoreOperationRelationalExpression(args, 0);
        assertTrue((Boolean) expr.computeValue(null));
    }

    // Tests computeValue with boolean comparison
    @Test
    public void testComputeValue_booleanOperands_returnsCorrectBoolean() {
        Expression[] args = new Expression[]{new TestExpression(true), new TestExpression(false)};
        TestCoreOperationRelationalExpression expr = new TestCoreOperationRelationalExpression(args, 1);
        assertTrue((Boolean) expr.computeValue(null));
    }

    // Tests computeValue with integer comparison
    @Test
    public void testComputeValue_integerOperands_returnsCorrectBoolean() {
        Expression[] args = new Expression[]{new TestExpression(5), new TestExpression(3)};
        TestCoreOperationRelationalExpression expr = new TestCoreOperationRelationalExpression(args, 1);
        assertTrue((Boolean) expr.computeValue(null));
    }

    // Tests getPrecedence
    @Test
    public void testGetPrecedence_returnsRelationalExpressionPrecedence() {
        Expression[] args = new Expression[]{new TestExpression(1), new TestExpression(2)};
        TestCoreOperationRelationalExpression expr = new TestCoreOperationRelationalExpression(args, 1);
        assertEquals(CoreOperation.RELATIONAL_EXPR_PRECEDENCE, expr.getPrecedence());
    }

    // Tests isSymmetric
    @Test
    public void testIsSymmetric_returnsFalse() {
        Expression[] args = new Expression[]{new TestExpression(1), new TestExpression(2)};
        TestCoreOperationRelationalExpression expr = new TestCoreOperationRelationalExpression(args, 1);
        assertFalse(expr.isSymmetric());
    }

    // Helper Iterator implementation
    private static class TestIterator implements Iterator {
        private final Object value;
        private boolean hasNext = true;

        TestIterator(Object value) {
            this.value = value;
        }

        @Override
        public boolean hasNext() {
            return hasNext;
        }

        @Override
        public Object next() {
            hasNext = false;
            return value;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    // Helper SelfContext implementation for testing
    private static class TestSelfContext extends SelfContext {
        TestSelfContext() {
            super(null, null);
        }

        @Override
        public Object getSingleNodePointer() {
            return 5.0;
        }

        @Override
        public Object getCurrentNodePointer() {
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
    }
}