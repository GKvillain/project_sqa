package org.apache.commons.jxpath.ri.compiler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.jxpath.ri.EvalContext;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CoreOperationRelationalExpressionTest {

    private static class ConcreteRelationalExpression extends CoreOperationRelationalExpression {
        private final int expectedComparison;

        ConcreteRelationalExpression(Expression left, Expression right, int expectedComparison) {
            super(new Expression[]{left, right});
            this.expectedComparison = expectedComparison;
        }

        @Override
        public String getSymbol() {
            return ">";
        }

        @Override
        protected boolean evaluateCompare(int compare) {
            return compare == expectedComparison;
        }
    }

    private static class ValueExpression extends Expression {
        private final Object val;

        ValueExpression(Object val) {
            this.val = val;
        }

        @Override
        public Object computeValue(EvalContext context) {
            return val;
        }

        @Override
        public Object compute(EvalContext context) {
            return val;
        }

        @Override
        public boolean computeContextDependent() {
            return false;
        }
    }

    // Tests simple comparison where left is greater than right
    @Test
    public void testComputeValue_leftGreaterThanRight_returnsTrue() {
        Expression left = new ValueExpression(Double.valueOf(10.0));
        Expression right = new ValueExpression(Double.valueOf(5.0));
        CoreOperationRelationalExpression expr = new ConcreteRelationalExpression(left, right, 1);

        Object result = expr.computeValue(null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests comparison where values are equal
    @Test
    public void testComputeValue_equalValues_returnsTrueForZeroComparison() {
        Expression left = new ValueExpression(Double.valueOf(5.0));
        Expression right = new ValueExpression(Double.valueOf(5.0));
        CoreOperationRelationalExpression expr = new ConcreteRelationalExpression(left, right, 0);

        Object result = expr.computeValue(null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests comparison where left is less than right
    @Test
    public void testComputeValue_leftLessThanRight_returnsTrueForNegativeComparison() {
        Expression left = new ValueExpression(Double.valueOf(3.0));
        Expression right = new ValueExpression(Double.valueOf(7.0));
        CoreOperationRelationalExpression expr = new ConcreteRelationalExpression(left, right, -1);

        Object result = expr.computeValue(null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests NaN on left operand returns false
    @Test
    public void testComputeValue_leftIsNaN_returnsFalse() {
        Expression left = new ValueExpression(Double.valueOf(Double.NaN));
        Expression right = new ValueExpression(Double.valueOf(5.0));
        CoreOperationRelationalExpression expr = new ConcreteRelationalExpression(left, right, 1);

        Object result = expr.computeValue(null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests NaN on right operand returns false
    @Test
    public void testComputeValue_rightIsNaN_returnsFalse() {
        Expression left = new ValueExpression(Double.valueOf(5.0));
        Expression right = new ValueExpression(Double.valueOf(Double.NaN));
        CoreOperationRelationalExpression expr = new ConcreteRelationalExpression(left, right, 1);

        Object result = expr.computeValue(null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests non-numeric string converted to NaN returns false
    @Test
    public void testComputeValue_nonNumericString_returnsFalse() {
        Expression left = new ValueExpression("invalid_number");
        Expression right = new ValueExpression(Double.valueOf(5.0));
        CoreOperationRelationalExpression expr = new ConcreteRelationalExpression(left, right, 1);

        Object result = expr.computeValue(null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests left operand as Collection containing a matching element
    @Test
    public void testComputeValue_leftCollection_containsMatch() {
        List<Double> list = Arrays.asList(Double.valueOf(1.0), Double.valueOf(10.0));
        Expression left = new ValueExpression(list);
        Expression right = new ValueExpression(Double.valueOf(5.0));
        CoreOperationRelationalExpression expr = new ConcreteRelationalExpression(left, right, 1);

        Object result = expr.computeValue(null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests left operand as Collection containing no matching elements
    @Test
    public void testComputeValue_leftCollection_noMatch() {
        List<Double> list = Arrays.asList(Double.valueOf(1.0), Double.valueOf(2.0));
        Expression left = new ValueExpression(list);
        Expression right = new ValueExpression(Double.valueOf(5.0));
        CoreOperationRelationalExpression expr = new ConcreteRelationalExpression(left, right, 1);

        Object result = expr.computeValue(null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests right operand as Collection containing a matching element
    @Test
    public void testComputeValue_rightCollection_containsMatch() {
        Expression left = new ValueExpression(Double.valueOf(10.0));
        List<Double> list = Arrays.asList(Double.valueOf(5.0), Double.valueOf(20.0));
        Expression right = new ValueExpression(list);
        CoreOperationRelationalExpression expr = new ConcreteRelationalExpression(left, right, 1);

        Object result = expr.computeValue(null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests both operands as Collections with at least one matching pair
    @Test
    public void testComputeValue_bothCollections_findsMatch() {
        List<Double> leftList = Arrays.asList(Double.valueOf(2.0), Double.valueOf(8.0));
        List<Double> rightList = Arrays.asList(Double.valueOf(5.0), Double.valueOf(10.0));
        Expression left = new ValueExpression(leftList);
        Expression right = new ValueExpression(rightList);
        CoreOperationRelationalExpression expr = new ConcreteRelationalExpression(left, right, 1);

        Object result = expr.computeValue(null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests both operands as Collections with no matching pairs
    @Test
    public void testComputeValue_bothCollections_noMatch() {
        List<Double> leftList = Arrays.asList(Double.valueOf(1.0), Double.valueOf(2.0));
        List<Double> rightList = Arrays.asList(Double.valueOf(5.0), Double.valueOf(10.0));
        Expression left = new ValueExpression(leftList);
        Expression right = new ValueExpression(rightList);
        CoreOperationRelationalExpression expr = new ConcreteRelationalExpression(left, right, 1);

        Object result = expr.computeValue(null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests empty Collection on left operand returns false
    @Test
    public void testComputeValue_emptyCollectionLeft_returnsFalse() {
        Expression left = new ValueExpression(Collections.emptyList());
        Expression right = new ValueExpression(Double.valueOf(5.0));
        CoreOperationRelationalExpression expr = new ConcreteRelationalExpression(left, right, 1);

        Object result = expr.computeValue(null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests empty Collection on right operand returns false
    @Test
    public void testComputeValue_emptyCollectionRight_returnsFalse() {
        Expression left = new ValueExpression(Double.valueOf(5.0));
        Expression right = new ValueExpression(Collections.emptyList());
        CoreOperationRelationalExpression expr = new ConcreteRelationalExpression(left, right, 1);

        Object result = expr.computeValue(null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests left operand as Iterator
    @Test
    public void testComputeValue_leftIterator_containsMatch() {
        List<Double> list = new ArrayList<Double>();
        list.add(Double.valueOf(10.0));
        Expression left = new ValueExpression(list.iterator());
        Expression right = new ValueExpression(Double.valueOf(5.0));
        CoreOperationRelationalExpression expr = new ConcreteRelationalExpression(left, right, 1);

        Object result = expr.computeValue(null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests right operand as Iterator
    @Test
    public void testComputeValue_rightIterator_containsMatch() {
        List<Double> list = new ArrayList<Double>();
        list.add(Double.valueOf(5.0));
        Expression left = new ValueExpression(Double.valueOf(10.0));
        Expression right = new ValueExpression(list.iterator());
        CoreOperationRelationalExpression expr = new ConcreteRelationalExpression(left, right, 1);

        Object result = expr.computeValue(null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests getPrecedence returns expected value 3
    @Test
    public void testGetPrecedence_returnsThree() {
        Expression left = new ValueExpression(Double.valueOf(1.0));
        Expression right = new ValueExpression(Double.valueOf(2.0));
        CoreOperationRelationalExpression expr = new ConcreteRelationalExpression(left, right, 0);

        assertEquals(3, expr.getPrecedence());
    }

    // Tests isSymmetric returns false
    @Test
    public void testIsSymmetric_returnsFalse() {
        Expression left = new ValueExpression(Double.valueOf(1.0));
        Expression right = new ValueExpression(Double.valueOf(2.0));
        CoreOperationRelationalExpression expr = new ConcreteRelationalExpression(left, right, 0);

        assertFalse(expr.isSymmetric());
    }
}