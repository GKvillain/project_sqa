package org.apache.commons.jxpath.ri.compiler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.apache.commons.jxpath.ri.EvalContext;
import org.junit.Test;
import static org.junit.Assert.*;

public class CoreOperationRelationalExpressionTest {

    private static class DummyExpression extends Expression {
        private final Object value;

        public DummyExpression(Object value) {
            this.value = value;
        }

        public Object compute(EvalContext context) {
            return value;
        }

        public Object computeValue(EvalContext context) {
            return value;
        }

        public boolean isContextDependent() {
            return false;
        }
    }

    private static class GreaterThanOperation extends CoreOperationRelationalExpression {
        public GreaterThanOperation(Expression left, Expression right) {
            super(new Expression[] { left, right });
        }

        protected boolean evaluateCompare(int compare) {
            return compare > 0;
        }
    }

    private static class LessThanOperation extends CoreOperationRelationalExpression {
        public LessThanOperation(Expression left, Expression right) {
            super(new Expression[] { left, right });
        }

        protected boolean evaluateCompare(int compare) {
            return compare < 0;
        }
    }

    private static class GreaterThanOrEqualOperation extends CoreOperationRelationalExpression {
        public GreaterThanOrEqualOperation(Expression left, Expression right) {
            super(new Expression[] { left, right });
        }

        protected boolean evaluateCompare(int compare) {
            return compare >= 0;
        }
    }

    private static class LessThanOrEqualOperation extends CoreOperationRelationalExpression {
        public LessThanOrEqualOperation(Expression left, Expression right) {
            super(new Expression[] { left, right });
        }

        protected boolean evaluateCompare(int compare) {
            return compare <= 0;
        }
    }

    // Tests getPrecedence method
    @Test
    public void testGetPrecedence_default_returnsRelationalPrecedence() {
        GreaterThanOperation op = new GreaterThanOperation(new DummyExpression(1), new DummyExpression(2));
        assertEquals(CoreOperation.RELATIONAL_EXPR_PRECEDENCE, op.getPrecedence());
    }

    // Tests isSymmetric method
    @Test
    public void testIsSymmetric_default_returnsFalse() {
        GreaterThanOperation op = new GreaterThanOperation(new DummyExpression(1), new DummyExpression(2));
        assertFalse(op.isSymmetric());
    }

    // Tests simple numbers comparison where left > right
    @Test
    public void testComputeValue_leftGreaterThanRight_returnsTrue() {
        GreaterThanOperation op = new GreaterThanOperation(new DummyExpression(5.0), new DummyExpression(3.0));
        assertEquals(Boolean.TRUE, op.computeValue(null));
    }

    // Tests simple numbers comparison where left == right
    @Test
    public void testComputeValue_leftEqualsRight_returnsFalseForGreaterThan() {
        GreaterThanOperation op = new GreaterThanOperation(new DummyExpression(5.0), new DummyExpression(5.0));
        assertEquals(Boolean.FALSE, op.computeValue(null));
    }

    // Tests simple numbers comparison where left == right for greater than or equal
    @Test
    public void testComputeValue_leftEqualsRight_returnsTrueForGreaterThanOrEqual() {
        GreaterThanOrEqualOperation op = new GreaterThanOrEqualOperation(new DummyExpression(5.0), new DummyExpression(5.0));
        assertEquals(Boolean.TRUE, op.computeValue(null));
    }

    // Tests simple numbers comparison where left < right
    @Test
    public void testComputeValue_leftLessThanRight_returnsTrueForLessThan() {
        LessThanOperation op = new LessThanOperation(new DummyExpression(3.0), new DummyExpression(5.0));
        assertEquals(Boolean.TRUE, op.computeValue(null));
    }

    // Tests NaN on left operand
    @Test
    public void testComputeValue_nanLeft_returnsFalse() {
        GreaterThanOperation op = new GreaterThanOperation(new DummyExpression(Double.NaN), new DummyExpression(5.0));
        assertEquals(Boolean.FALSE, op.computeValue(null));
    }

    // Tests NaN on right operand
    @Test
    public void testComputeValue_nanRight_returnsFalse() {
        GreaterThanOperation op = new GreaterThanOperation(new DummyExpression(5.0), new DummyExpression(Double.NaN));
        assertEquals(Boolean.FALSE, op.computeValue(null));
    }

    // Tests non-numeric strings converting to NaN
    @Test
    public void testComputeValue_invalidString_returnsFalse() {
        GreaterThanOperation op = new GreaterThanOperation(new DummyExpression("invalid"), new DummyExpression(5.0));
        assertEquals(Boolean.FALSE, op.computeValue(null));
    }

    // Tests collection on left operand matching condition
    @Test
    public void testComputeValue_collectionOnLeftMatches_returnsTrue() {
        List leftList = Arrays.asList(1.0, 6.0);
        GreaterThanOperation op = new GreaterThanOperation(new DummyExpression(leftList), new DummyExpression(5.0));
        assertEquals(Boolean.TRUE, op.computeValue(null));
    }

    // Tests collection on left operand not matching condition
    @Test
    public void testComputeValue_collectionOnLeftNoMatch_returnsFalse() {
        List leftList = Arrays.asList(1.0, 2.0);
        GreaterThanOperation op = new GreaterThanOperation(new DummyExpression(leftList), new DummyExpression(5.0));
        assertEquals(Boolean.FALSE, op.computeValue(null));
    }

    // Tests collection on right operand when left is greater than an element
    @Test
    public void testComputeValue_collectionOnRightLeftGreaterThan_returnsTrue() {
        List rightList = Arrays.asList(2.0, 8.0);
        GreaterThanOperation op = new GreaterThanOperation(new DummyExpression(5.0), new DummyExpression(rightList));
        assertEquals(Boolean.TRUE, op.computeValue(null));
    }

    // Tests collection on right operand when left is smaller than all elements
    @Test
    public void testComputeValue_collectionOnRightLeftSmallerThanAll_returnsFalse() {
        List rightList = Arrays.asList(6.0, 8.0);
        GreaterThanOperation op = new GreaterThanOperation(new DummyExpression(5.0), new DummyExpression(rightList));
        assertEquals(Boolean.FALSE, op.computeValue(null));
    }

    // Tests collection on right operand with less than operator
    @Test
    public void testComputeValue_collectionOnRightLessThan_returnsTrue() {
        List rightList = Arrays.asList(2.0, 8.0);
        LessThanOperation op = new LessThanOperation(new DummyExpression(5.0), new DummyExpression(rightList));
        assertEquals(Boolean.TRUE, op.computeValue(null));
    }

    // Tests iterator on left and right operands with intersection
    @Test
    public void testComputeValue_bothIteratorsMatch_returnsTrue() {
        List leftList = Arrays.asList(1.0, 10.0);
        List rightList = Arrays.asList(5.0, 20.0);
        GreaterThanOperation op = new GreaterThanOperation(new DummyExpression(leftList.iterator()), new DummyExpression(rightList.iterator()));
        assertEquals(Boolean.TRUE, op.computeValue(null));
    }

    // Tests iterator on left and right operands with no match
    @Test
    public void testComputeValue_bothIteratorsNoMatch_returnsFalse() {
        List leftList = Arrays.asList(1.0, 2.0);
        List rightList = Arrays.asList(5.0, 20.0);
        GreaterThanOperation op = new GreaterThanOperation(new DummyExpression(leftList.iterator()), new DummyExpression(rightList.iterator()));
        assertEquals(Boolean.FALSE, op.computeValue(null));
    }

    // Tests empty collection on left operand
    @Test
    public void testComputeValue_emptyCollectionLeft_returnsFalse() {
        GreaterThanOperation op = new GreaterThanOperation(new DummyExpression(Collections.emptyList()), new DummyExpression(5.0));
        assertEquals(Boolean.FALSE, op.computeValue(null));
    }

    // Tests empty collection on right operand
    @Test
    public void testComputeValue_emptyCollectionRight_returnsFalse() {
        GreaterThanOperation op = new GreaterThanOperation(new DummyExpression(5.0), new DummyExpression(Collections.emptyList()));
        assertEquals(Boolean.FALSE, op.computeValue(null));
    }

    // Tests both operands as collections matching
    @Test
    public void testComputeValue_bothCollectionsMatch_returnsTrue() {
        List leftList = Arrays.asList(1.0, 10.0);
        List rightList = Arrays.asList(5.0, 20.0);
        GreaterThanOperation op = new GreaterThanOperation(new DummyExpression(leftList), new DummyExpression(rightList));
        assertEquals(Boolean.TRUE, op.computeValue(null));
    }

    // Tests both operands as collections with no match
    @Test
    public void testComputeValue_bothCollectionsNoMatch_returnsFalse() {
        List leftList = Arrays.asList(1.0, 2.0);
        List rightList = Arrays.asList(5.0, 20.0);
        GreaterThanOperation op = new GreaterThanOperation(new DummyExpression(leftList), new DummyExpression(rightList));
        assertEquals(Boolean.FALSE, op.computeValue(null));
    }

    // Tests iterator on left operand with matching single value on right
    @Test
    public void testComputeValue_iteratorOnLeftMatches_returnsTrue() {
        List leftList = Arrays.asList(1.0, 8.0);
        GreaterThanOperation op = new GreaterThanOperation(new DummyExpression(leftList.iterator()), new DummyExpression(5.0));
        assertEquals(Boolean.TRUE, op.computeValue(null));
    }

    // Tests iterator on left operand with no matching single value on right
    @Test
    public void testComputeValue_iteratorOnLeftNoMatch_returnsFalse() {
        List leftList = Arrays.asList(1.0, 3.0);
        GreaterThanOperation op = new GreaterThanOperation(new DummyExpression(leftList.iterator()), new DummyExpression(5.0));
        assertEquals(Boolean.FALSE, op.computeValue(null));
    }

    // Tests iterator on right operand with matching single value on left
    @Test
    public void testComputeValue_iteratorOnRightMatches_returnsTrue() {
        List rightList = Arrays.asList(2.0, 8.0);
        GreaterThanOperation op = new GreaterThanOperation(new DummyExpression(5.0), new DummyExpression(rightList.iterator()));
        assertEquals(Boolean.TRUE, op.computeValue(null));
    }

    // Tests iterator on right operand with no matching single value on left
    @Test
    public void testComputeValue_iteratorOnRightNoMatch_returnsFalse() {
        List rightList = Arrays.asList(6.0, 8.0);
        GreaterThanOperation op = new GreaterThanOperation(new DummyExpression(5.0), new DummyExpression(rightList.iterator()));
        assertEquals(Boolean.FALSE, op.computeValue(null));
    }

    // Tests empty iterator on left operand
    @Test
    public void testComputeValue_emptyIteratorLeft_returnsFalse() {
        GreaterThanOperation op = new GreaterThanOperation(new DummyExpression(Collections.emptyList().iterator()), new DummyExpression(5.0));
        assertEquals(Boolean.FALSE, op.computeValue(null));
    }

    // Tests empty iterator on right operand
    @Test
    public void testComputeValue_emptyIteratorRight_returnsFalse() {
        GreaterThanOperation op = new GreaterThanOperation(new DummyExpression(5.0), new DummyExpression(Collections.emptyList().iterator()));
        assertEquals(Boolean.FALSE, op.computeValue(null));
    }

    // Tests null values on operands
    @Test
    public void testComputeValue_nullLeft_returnsFalse() {
        GreaterThanOperation op = new GreaterThanOperation(new DummyExpression(null), new DummyExpression(5.0));
        assertEquals(Boolean.FALSE, op.computeValue(null));
    }

    @Test
    public void testComputeValue_nullRight_returnsFalse() {
        GreaterThanOperation op = new GreaterThanOperation(new DummyExpression(5.0), new DummyExpression(null));
        assertEquals(Boolean.FALSE, op.computeValue(null));
    }

    // Tests numeric string operands
    @Test
    public void testComputeValue_numericStringsComparison_returnsTrue() {
        GreaterThanOperation op = new GreaterThanOperation(new DummyExpression("10.5"), new DummyExpression("5.2"));
        assertEquals(Boolean.TRUE, op.computeValue(null));
    }

    // Tests boolean operands
    @Test
    public void testComputeValue_booleanValuesComparison_returnsTrue() {
        GreaterThanOperation op = new GreaterThanOperation(new DummyExpression(Boolean.TRUE), new DummyExpression(Boolean.FALSE));
        assertEquals(Boolean.TRUE, op.computeValue(null));
    }

    // Tests less than or equal operation
    @Test
    public void testComputeValue_lessThanOrEqual_returnsCorrectValues() {
        LessThanOrEqualOperation op1 = new LessThanOrEqualOperation(new DummyExpression(3.0), new DummyExpression(5.0));
        assertEquals(Boolean.TRUE, op1.computeValue(null));

        LessThanOrEqualOperation op2 = new LessThanOrEqualOperation(new DummyExpression(5.0), new DummyExpression(5.0));
        assertEquals(Boolean.TRUE, op2.computeValue(null));

        LessThanOrEqualOperation op3 = new LessThanOrEqualOperation(new DummyExpression(7.0), new DummyExpression(5.0));
        assertEquals(Boolean.FALSE, op3.computeValue(null));
    }
}