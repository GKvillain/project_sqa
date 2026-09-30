package org.apache.commons.jxpath.ri.compiler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.commons.jxpath.ri.EvalContext;
import org.junit.Test;
import static org.junit.Assert.*;

public class CoreOperationRelationalExpressionTest {

    private static class TestRelationalExpression extends CoreOperationRelationalExpression {
        private final int expectedComparison;

        TestRelationalExpression(Expression left, Expression right, int expectedComparison) {
            super(new Expression[] { left, right });
            this.expectedComparison = expectedComparison;
        }

        @Override
        protected boolean evaluateCompare(int compare) {
            return compare == expectedComparison;
        }
    }

    private static class TestLessThanExpression extends CoreOperationRelationalExpression {
        TestLessThanExpression(Expression left, Expression right) {
            super(new Expression[] { left, right });
        }

        @Override
        protected boolean evaluateCompare(int compare) {
            return compare < 0;
        }
    }

    private static class TestGreaterThanOrEqualExpression extends CoreOperationRelationalExpression {
        TestGreaterThanOrEqualExpression(Expression left, Expression right) {
            super(new Expression[] { left, right });
        }

        @Override
        protected boolean evaluateCompare(int compare) {
            return compare >= 0;
        }
    }

    private static class MockExpression extends Expression {
        private final Object value;

        MockExpression(Object value) {
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
    }

    // Tests that getPrecedence returns 3
    @Test
    public void testGetPrecedence_default_returnsThree() {
        TestLessThanExpression expr = new TestLessThanExpression(new MockExpression(1), new MockExpression(2));
        assertEquals(3, expr.getPrecedence());
    }

    // Tests that isSymmetric returns false
    @Test
    public void testIsSymmetric_default_returnsFalse() {
        TestLessThanExpression expr = new TestLessThanExpression(new MockExpression(1), new MockExpression(2));
        assertFalse(expr.isSymmetric());
    }

    // Tests normal comparison where left is less than right (< 0)
    @Test
    public void testComputeValue_leftLessThanRight_returnsTrue() {
        TestLessThanExpression expr = new TestLessThanExpression(
                new MockExpression(Double.valueOf(1.5)),
                new MockExpression(Double.valueOf(2.5))
        );
        Object result = expr.computeValue(null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests normal comparison where left is greater than right
    @Test
    public void testComputeValue_leftGreaterThanRight_returnsFalse() {
        TestLessThanExpression expr = new TestLessThanExpression(
                new MockExpression(Double.valueOf(5.0)),
                new MockExpression(Double.valueOf(2.0))
        );
        Object result = expr.computeValue(null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests comparison where values are equal
    @Test
    public void testComputeValue_equalValues_returnsExpectedResult() {
        TestRelationalExpression expr = new TestRelationalExpression(
                new MockExpression(Double.valueOf(3.0)),
                new MockExpression(Double.valueOf(3.0)),
                0
        );
        Object result = expr.computeValue(null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests comparison with greater than or equal operator on equal values
    @Test
    public void testComputeValue_greaterThanOrEqualEqualValues_returnsTrue() {
        TestGreaterThanOrEqualExpression expr = new TestGreaterThanOrEqualExpression(
                new MockExpression("4.0"),
                new MockExpression(Double.valueOf(4.0))
        );
        Object result = expr.computeValue(null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests NaN on the left operand
    @Test
    public void testComputeValue_leftIsNaN_returnsFalse() {
        TestLessThanExpression expr = new TestLessThanExpression(
                new MockExpression(Double.valueOf(Double.NaN)),
                new MockExpression(Double.valueOf(1.0))
        );
        Object result = expr.computeValue(null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests NaN on the right operand
    @Test
    public void testComputeValue_rightIsNaN_returnsFalse() {
        TestLessThanExpression expr = new TestLessThanExpression(
                new MockExpression(Double.valueOf(1.0)),
                new MockExpression(Double.valueOf(Double.NaN))
        );
        Object result = expr.computeValue(null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests string conversion to double
    @Test
    public void testComputeValue_stringOperands_parsedAndCompared() {
        TestLessThanExpression expr = new TestLessThanExpression(
                new MockExpression("10"),
                new MockExpression("20")
        );
        Object result = expr.computeValue(null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests invalid string operand resulting in NaN
    @Test
    public void testComputeValue_invalidStringOperand_returnsFalse() {
        TestLessThanExpression expr = new TestLessThanExpression(
                new MockExpression("invalidNumber"),
                new MockExpression(Double.valueOf(10.0))
        );
        Object result = expr.computeValue(null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests left operand as a Collection with matching element
    @Test
    public void testComputeValue_leftCollectionWithMatch_returnsTrue() {
        List<Object> leftList = Arrays.<Object>asList(Double.valueOf(10.0), Double.valueOf(1.0));
        TestLessThanExpression expr = new TestLessThanExpression(
                new MockExpression(leftList),
                new MockExpression(Double.valueOf(5.0))
        );
        Object result = expr.computeValue(null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests left operand as a Collection with no matching element
    @Test
    public void testComputeValue_leftCollectionNoMatch_returnsFalse() {
        List<Object> leftList = Arrays.<Object>asList(Double.valueOf(10.0), Double.valueOf(20.0));
        TestLessThanExpression expr = new TestLessThanExpression(
                new MockExpression(leftList),
                new MockExpression(Double.valueOf(5.0))
        );
        Object result = expr.computeValue(null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests right operand as a Collection with matching element
    @Test
    public void testComputeValue_rightCollectionWithMatch_returnsTrue() {
        List<Object> rightList = Arrays.<Object>asList(Double.valueOf(10.0), Double.valueOf(1.0));
        TestLessThanExpression expr = new TestLessThanExpression(
                new MockExpression(Double.valueOf(5.0)),
                new MockExpression(rightList)
        );
        Object result = expr.computeValue(null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests both operands as Collections with match
    @Test
    public void testComputeValue_bothCollectionsWithMatch_returnsTrue() {
        List<Object> leftList = Arrays.<Object>asList(Double.valueOf(10.0), Double.valueOf(2.0));
        List<Object> rightList = Arrays.<Object>asList(Double.valueOf(1.0), Double.valueOf(5.0));
        TestLessThanExpression expr = new TestLessThanExpression(
                new MockExpression(leftList),
                new MockExpression(rightList)
        );
        Object result = expr.computeValue(null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests both operands as Collections with no match
    @Test
    public void testComputeValue_bothCollectionsNoMatch_returnsFalse() {
        List<Object> leftList = Arrays.<Object>asList(Double.valueOf(10.0), Double.valueOf(20.0));
        List<Object> rightList = Arrays.<Object>asList(Double.valueOf(1.0), Double.valueOf(2.0));
        TestLessThanExpression expr = new TestLessThanExpression(
                new MockExpression(leftList),
                new MockExpression(rightList)
        );
        Object result = expr.computeValue(null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests empty collection on the left side
    @Test
    public void testComputeValue_emptyCollectionLeft_returnsFalse() {
        TestLessThanExpression expr = new TestLessThanExpression(
                new MockExpression(Collections.emptyList()),
                new MockExpression(Double.valueOf(5.0))
        );
        Object result = expr.computeValue(null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests empty collection on the right side
    @Test
    public void testComputeValue_emptyCollectionRight_returnsFalse() {
        TestLessThanExpression expr = new TestLessThanExpression(
                new MockExpression(Double.valueOf(5.0)),
                new MockExpression(Collections.emptyList())
        );
        Object result = expr.computeValue(null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests iterator operand
    @Test
    public void testComputeValue_iteratorOperands_evaluatesCorrectly() {
        List<Object> list = new ArrayList<Object>();
        list.add(Double.valueOf(3.0));
        TestLessThanExpression expr = new TestLessThanExpression(
                new MockExpression(list.iterator()),
                new MockExpression(Double.valueOf(5.0))
        );
        Object result = expr.computeValue(null);
        assertEquals(Boolean.TRUE, result);
    }
}