package org.apache.commons.jxpath.ri.compiler;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.jxpath.ri.EvalContext;
import org.junit.Test;

import static org.junit.Assert.*;

public class CoreOperationRelationalExpressionTest {

    private static class MockExpression extends Expression {
        private final Object value;

        MockExpression(Object value) {
            this.value = value;
        }

        public Object compute(EvalContext context) {
            return value;
        }

        public Object computeValue(EvalContext context) {
            return value;
        }

        public boolean computeContextDependent() {
            return false;
        }
    }

    private static class TestLessThanOperation extends CoreOperationRelationalExpression {
        protected TestLessThanOperation(Expression[] args) {
            super(args);
        }

        protected boolean evaluateCompare(int compare) {
            return compare < 0;
        }

        public String getSymbol() {
            return "<";
        }
    }

    private static class TestGreaterThanOperation extends CoreOperationRelationalExpression {
        protected TestGreaterThanOperation(Expression[] args) {
            super(args);
        }

        protected boolean evaluateCompare(int compare) {
            return compare > 0;
        }

        public String getSymbol() {
            return ">";
        }
    }

    private static class TestLessThanOrEqualOperation extends CoreOperationRelationalExpression {
        protected TestLessThanOrEqualOperation(Expression[] args) {
            super(args);
        }

        protected boolean evaluateCompare(int compare) {
            return compare <= 0;
        }

        public String getSymbol() {
            return "<=";
        }
    }

    private static class TestGreaterThanOrEqualOperation extends CoreOperationRelationalExpression {
        protected TestGreaterThanOrEqualOperation(Expression[] args) {
            super(args);
        }

        protected boolean evaluateCompare(int compare) {
            return compare >= 0;
        }

        public String getSymbol() {
            return ">=";
        }
    }

    // Tests getPrecedence returns RELATIONAL_EXPR_PRECEDENCE
    @Test
    public void testGetPrecedence_returnsRelationalExprPrecedence() {
        TestLessThanOperation op = new TestLessThanOperation(new Expression[]{
            new Constant(Integer.valueOf(1)),
            new Constant(Integer.valueOf(2))
        });
        assertEquals(CoreOperation.RELATIONAL_EXPR_PRECEDENCE, op.getPrecedence());
    }

    // Tests isSymmetric returns false
    @Test
    public void testIsSymmetric_returnsFalse() {
        TestLessThanOperation op = new TestLessThanOperation(new Expression[]{
            new Constant(Integer.valueOf(1)),
            new Constant(Integer.valueOf(2))
        });
        assertFalse(op.isSymmetric());
    }

    // Tests simple less-than comparison with numbers
    @Test
    public void testComputeValue_lessThan_returnsTrue() {
        TestLessThanOperation op = new TestLessThanOperation(new Expression[]{
            new Constant(Integer.valueOf(1)),
            new Constant(Integer.valueOf(2))
        });
        Object result = op.computeValue(null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests simple less-than comparison when left is equal to right
    @Test
    public void testComputeValue_lessThanEqualValues_returnsFalse() {
        TestLessThanOperation op = new TestLessThanOperation(new Expression[]{
            new Constant(Integer.valueOf(2)),
            new Constant(Integer.valueOf(2))
        });
        Object result = op.computeValue(null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests simple greater-than comparison
    @Test
    public void testComputeValue_greaterThan_returnsTrue() {
        TestGreaterThanOperation op = new TestGreaterThanOperation(new Expression[]{
            new Constant(Integer.valueOf(5)),
            new Constant(Integer.valueOf(3))
        });
        Object result = op.computeValue(null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests less than or equal comparison on boundary equal
    @Test
    public void testComputeValue_lessThanOrEqual_boundaryEqual_returnsTrue() {
        TestLessThanOrEqualOperation op = new TestLessThanOrEqualOperation(new Expression[]{
            new Constant(Double.valueOf(2.5)),
            new Constant(Double.valueOf(2.5))
        });
        Object result = op.computeValue(null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests greater than or equal comparison on boundary equal
    @Test
    public void testComputeValue_greaterThanOrEqual_boundaryEqual_returnsTrue() {
        TestGreaterThanOrEqualOperation op = new TestGreaterThanOrEqualOperation(new Expression[]{
            new Constant(Double.valueOf(2.5)),
            new Constant(Double.valueOf(2.5))
        });
        Object result = op.computeValue(null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests NaN on left operand returns false
    @Test
    public void testComputeValue_leftIsNaN_returnsFalse() {
        TestLessThanOperation op = new TestLessThanOperation(new Expression[]{
            new Constant("not-a-number"),
            new Constant(Integer.valueOf(1))
        });
        Object result = op.computeValue(null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests NaN on right operand returns false
    @Test
    public void testComputeValue_rightIsNaN_returnsFalse() {
        TestLessThanOperation op = new TestLessThanOperation(new Expression[]{
            new Constant(Integer.valueOf(1)),
            new Constant("invalid-number")
        });
        Object result = op.computeValue(null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests left operand as Collection (Iterator) vs single value
    @Test
    public void testComputeValue_leftIsCollection_matchesTrue() {
        final List list = Arrays.asList(Integer.valueOf(5), Integer.valueOf(1));
        Expression leftExpr = new MockExpression(list);

        TestLessThanOperation op = new TestLessThanOperation(new Expression[]{
            leftExpr,
            new Constant(Integer.valueOf(3))
        });
        Object result = op.computeValue(null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests left operand as Collection (Iterator) with no match
    @Test
    public void testComputeValue_leftIsCollection_noMatch_returnsFalse() {
        final List list = Arrays.asList(Integer.valueOf(5), Integer.valueOf(6));
        Expression leftExpr = new MockExpression(list);

        TestLessThanOperation op = new TestLessThanOperation(new Expression[]{
            leftExpr,
            new Constant(Integer.valueOf(3))
        });
        Object result = op.computeValue(null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests right operand as Collection (Iterator) vs single value (Defects4J 20 regression)
    @Test
    public void testComputeValue_rightIsCollection_evaluatesOrderCorrectly() {
        final List list = Arrays.asList(Integer.valueOf(0), Integer.valueOf(5));
        Expression rightExpr = new MockExpression(list);

        TestLessThanOperation op = new TestLessThanOperation(new Expression[]{
            new Constant(Integer.valueOf(3)),
            rightExpr
        });
        Object result = op.computeValue(null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests right operand as Collection (Iterator) with no match
    @Test
    public void testComputeValue_rightIsCollection_noMatch_returnsFalse() {
        final List list = Arrays.asList(Integer.valueOf(1), Integer.valueOf(2));
        Expression rightExpr = new MockExpression(list);

        TestLessThanOperation op = new TestLessThanOperation(new Expression[]{
            new Constant(Integer.valueOf(3)),
            rightExpr
        });
        Object result = op.computeValue(null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests both operands as Collections (Iterators) with match
    @Test
    public void testComputeValue_bothAreCollections_matchesTrue() {
        final List leftList = Arrays.asList(Integer.valueOf(10), Integer.valueOf(2));
        final List rightList = Arrays.asList(Integer.valueOf(5), Integer.valueOf(1));

        Expression leftExpr = new MockExpression(leftList);
        Expression rightExpr = new MockExpression(rightList);

        TestLessThanOperation op = new TestLessThanOperation(new Expression[]{
            leftExpr,
            rightExpr
        });
        Object result = op.computeValue(null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests both operands as Collections (Iterators) with empty left collection
    @Test
    public void testComputeValue_bothAreCollections_emptyLeft_returnsFalse() {
        final List leftList = Collections.emptyList();
        final List rightList = Arrays.asList(Integer.valueOf(1), Integer.valueOf(2));

        Expression leftExpr = new MockExpression(leftList);
        Expression rightExpr = new MockExpression(rightList);

        TestLessThanOperation op = new TestLessThanOperation(new Expression[]{
            leftExpr,
            rightExpr
        });
        Object result = op.computeValue(null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests both operands as Collections (Iterators) with empty right collection
    @Test
    public void testComputeValue_bothAreCollections_emptyRight_returnsFalse() {
        final List leftList = Arrays.asList(Integer.valueOf(1), Integer.valueOf(2));
        final List rightList = Collections.emptyList();

        Expression leftExpr = new MockExpression(leftList);
        Expression rightExpr = new MockExpression(rightList);

        TestLessThanOperation op = new TestLessThanOperation(new Expression[]{
            leftExpr,
            rightExpr
        });
        Object result = op.computeValue(null);
        assertEquals(Boolean.FALSE, result);
    }
}