package org.apache.commons.jxpath.ri.compiler;

import java.util.Arrays;
import java.util.Collections;
import org.apache.commons.jxpath.ri.EvalContext;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CoreOperationRelationalExpressionTest {

    private static class ValueExpr extends Constant {
        private final Object val;

        public ValueExpr(Object val) {
            super("dummy");
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
    }

    private static class GreaterThanExpr extends CoreOperationRelationalExpression {
        public GreaterThanExpr(Expression left, Expression right) {
            super(new Expression[] { left, right });
        }

        @Override
        protected boolean evaluateCompare(int compare) {
            return compare > 0;
        }

        @Override
        public String getSymbol() {
            return ">";
        }
    }

    private static class LessThanExpr extends CoreOperationRelationalExpression {
        public LessThanExpr(Expression left, Expression right) {
            super(new Expression[] { left, right });
        }

        @Override
        protected boolean evaluateCompare(int compare) {
            return compare < 0;
        }

        @Override
        public String getSymbol() {
            return "<";
        }
    }

    private static class GreaterThanOrEqualExpr extends CoreOperationRelationalExpression {
        public GreaterThanOrEqualExpr(Expression left, Expression right) {
            super(new Expression[] { left, right });
        }

        @Override
        protected boolean evaluateCompare(int compare) {
            return compare >= 0;
        }

        @Override
        public String getSymbol() {
            return ">=";
        }
    }

    private static class LessThanOrEqualExpr extends CoreOperationRelationalExpression {
        public LessThanOrEqualExpr(Expression left, Expression right) {
            super(new Expression[] { left, right });
        }

        @Override
        protected boolean evaluateCompare(int compare) {
            return compare <= 0;
        }

        @Override
        public String getSymbol() {
            return "<=";
        }
    }

    // Tests operator precedence
    @Test
    public void testGetPrecedence_default_returnsThree() {
        GreaterThanExpr expr = new GreaterThanExpr(new Constant(Integer.valueOf(1)), new Constant(Integer.valueOf(2)));
        assertEquals(3, expr.getPrecedence());
    }

    // Tests symmetry property
    @Test
    public void testIsSymmetric_default_returnsFalse() {
        GreaterThanExpr expr = new GreaterThanExpr(new Constant(Integer.valueOf(1)), new Constant(Integer.valueOf(2)));
        assertFalse(expr.isSymmetric());
    }

    // Tests greater-than comparison with normal positive numbers
    @Test
    public void testComputeValue_greaterThan_returnsCorrectBoolean() {
        GreaterThanExpr exprTrue = new GreaterThanExpr(new Constant(Integer.valueOf(2)), new Constant(Integer.valueOf(1)));
        assertEquals(Boolean.TRUE, exprTrue.computeValue(null));

        GreaterThanExpr exprFalse = new GreaterThanExpr(new Constant(Integer.valueOf(1)), new Constant(Integer.valueOf(2)));
        assertEquals(Boolean.FALSE, exprFalse.computeValue(null));

        GreaterThanExpr exprEqual = new GreaterThanExpr(new Constant(Integer.valueOf(1)), new Constant(Integer.valueOf(1)));
        assertEquals(Boolean.FALSE, exprEqual.computeValue(null));
    }

    // Tests less-than comparison with normal numbers
    @Test
    public void testComputeValue_lessThan_returnsCorrectBoolean() {
        LessThanExpr exprTrue = new LessThanExpr(new Constant(Integer.valueOf(1)), new Constant(Integer.valueOf(2)));
        assertEquals(Boolean.TRUE, exprTrue.computeValue(null));

        LessThanExpr exprFalse = new LessThanExpr(new Constant(Integer.valueOf(2)), new Constant(Integer.valueOf(1)));
        assertEquals(Boolean.FALSE, exprFalse.computeValue(null));
    }

    // Tests greater-than-or-equal comparison boundary
    @Test
    public void testComputeValue_greaterThanOrEqual_returnsCorrectBoolean() {
        GreaterThanOrEqualExpr exprTrue = new GreaterThanOrEqualExpr(new Constant(Integer.valueOf(2)), new Constant(Integer.valueOf(1)));
        assertEquals(Boolean.TRUE, exprTrue.computeValue(null));

        GreaterThanOrEqualExpr exprEqual = new GreaterThanOrEqualExpr(new Constant(Integer.valueOf(1)), new Constant(Integer.valueOf(1)));
        assertEquals(Boolean.TRUE, exprEqual.computeValue(null));

        GreaterThanOrEqualExpr exprFalse = new GreaterThanOrEqualExpr(new Constant(Integer.valueOf(1)), new Constant(Integer.valueOf(2)));
        assertEquals(Boolean.FALSE, exprFalse.computeValue(null));
    }

    // Tests less-than-or-equal comparison boundary
    @Test
    public void testComputeValue_lessThanOrEqual_returnsCorrectBoolean() {
        LessThanOrEqualExpr exprTrue = new LessThanOrEqualExpr(new Constant(Integer.valueOf(1)), new Constant(Integer.valueOf(2)));
        assertEquals(Boolean.TRUE, exprTrue.computeValue(null));

        LessThanOrEqualExpr exprEqual = new LessThanOrEqualExpr(new Constant(Integer.valueOf(1)), new Constant(Integer.valueOf(1)));
        assertEquals(Boolean.TRUE, exprEqual.computeValue(null));

        LessThanOrEqualExpr exprFalse = new LessThanOrEqualExpr(new Constant(Integer.valueOf(2)), new Constant(Integer.valueOf(1)));
        assertEquals(Boolean.FALSE, exprFalse.computeValue(null));
    }

    // Tests NaN on left operand returns false
    @Test
    public void testComputeValue_nanLeft_returnsFalse() {
        GreaterThanExpr exprGT = new GreaterThanExpr(new Constant(Double.valueOf(Double.NaN)), new Constant(Integer.valueOf(1)));
        assertEquals(Boolean.FALSE, exprGT.computeValue(null));

        GreaterThanOrEqualExpr exprGTE = new GreaterThanOrEqualExpr(new Constant(Double.valueOf(Double.NaN)), new Constant(Integer.valueOf(1)));
        assertEquals(Boolean.FALSE, exprGTE.computeValue(null));

        LessThanExpr exprLT = new LessThanExpr(new Constant(Double.valueOf(Double.NaN)), new Constant(Integer.valueOf(1)));
        assertEquals(Boolean.FALSE, exprLT.computeValue(null));

        LessThanOrEqualExpr exprLTE = new LessThanOrEqualExpr(new Constant(Double.valueOf(Double.NaN)), new Constant(Integer.valueOf(1)));
        assertEquals(Boolean.FALSE, exprLTE.computeValue(null));
    }

    // Tests NaN on right operand returns false
    @Test
    public void testComputeValue_nanRight_returnsFalse() {
        GreaterThanExpr exprGT = new GreaterThanExpr(new Constant(Integer.valueOf(1)), new Constant(Double.valueOf(Double.NaN)));
        assertEquals(Boolean.FALSE, exprGT.computeValue(null));

        GreaterThanOrEqualExpr exprGTE = new GreaterThanOrEqualExpr(new Constant(Integer.valueOf(1)), new Constant(Double.valueOf(Double.NaN)));
        assertEquals(Boolean.FALSE, exprGTE.computeValue(null));

        LessThanExpr exprLT = new LessThanExpr(new Constant(Integer.valueOf(1)), new Constant(Double.valueOf(Double.NaN)));
        assertEquals(Boolean.FALSE, exprLT.computeValue(null));

        LessThanOrEqualExpr exprLTE = new LessThanOrEqualExpr(new Constant(Integer.valueOf(1)), new Constant(Double.valueOf(Double.NaN)));
        assertEquals(Boolean.FALSE, exprLTE.computeValue(null));
    }

    // Tests NaN on both operands returns false
    @Test
    public void testComputeValue_bothNaN_returnsFalse() {
        GreaterThanExpr expr = new GreaterThanExpr(
                new Constant(Double.valueOf(Double.NaN)),
                new Constant(Double.valueOf(Double.NaN))
        );
        assertEquals(Boolean.FALSE, expr.computeValue(null));
    }

    // Tests collections on both left and right operands with matching element
    @Test
    public void testComputeValue_bothCollectionsMatch_returnsTrue() {
        GreaterThanExpr expr = new GreaterThanExpr(
                new ValueExpr(Arrays.asList(Double.valueOf(1.0), Double.valueOf(5.0))),
                new ValueExpr(Arrays.asList(Double.valueOf(2.0), Double.valueOf(3.0)))
        );
        assertEquals(Boolean.TRUE, expr.computeValue(null));
    }

    // Tests collections on both left and right operands with no matching element
    @Test
    public void testComputeValue_bothCollectionsNoMatch_returnsFalse() {
        GreaterThanExpr expr = new GreaterThanExpr(
                new ValueExpr(Arrays.asList(Double.valueOf(1.0), Double.valueOf(2.0))),
                new ValueExpr(Arrays.asList(Double.valueOf(3.0), Double.valueOf(4.0)))
        );
        assertEquals(Boolean.FALSE, expr.computeValue(null));
    }

    // Tests left collection operand against single right value
    @Test
    public void testComputeValue_leftCollectionRightValue_returnsCorrectResult() {
        GreaterThanExpr exprTrue = new GreaterThanExpr(
                new ValueExpr(Arrays.asList(Double.valueOf(1.0), Double.valueOf(5.0))),
                new Constant(Double.valueOf(3.0))
        );
        assertEquals(Boolean.TRUE, exprTrue.computeValue(null));

        GreaterThanExpr exprFalse = new GreaterThanExpr(
                new ValueExpr(Arrays.asList(Double.valueOf(1.0), Double.valueOf(2.0))),
                new Constant(Double.valueOf(3.0))
        );
        assertEquals(Boolean.FALSE, exprFalse.computeValue(null));
    }

    // Tests single left value against right collection operand
    @Test
    public void testComputeValue_leftValueRightCollection_returnsCorrectResult() {
        LessThanExpr exprTrue = new LessThanExpr(
                new Constant(Double.valueOf(3.0)),
                new ValueExpr(Arrays.asList(Double.valueOf(1.0), Double.valueOf(5.0)))
        );
        assertEquals(Boolean.TRUE, exprTrue.computeValue(null));
    }

    // Tests empty collection operand
    @Test
    public void testComputeValue_emptyCollection_returnsFalse() {
        GreaterThanExpr expr = new GreaterThanExpr(
                new ValueExpr(Collections.emptyList()),
                new Constant(Double.valueOf(1.0))
        );
        assertEquals(Boolean.FALSE, expr.computeValue(null));
    }

    // Tests string representations converted to numeric values
    @Test
    public void testComputeValue_stringConversion_returnsCorrectResult() {
        GreaterThanExpr expr = new GreaterThanExpr(new Constant("10"), new Constant("2"));
        assertEquals(Boolean.TRUE, expr.computeValue(null));
    }

    // Tests standard subclasses symbols and evaluation
    @Test
    public void testStandardSubclasses_symbolsAndEvaluation() {
        CoreOperationGreaterThan gt = new CoreOperationGreaterThan(new Constant(Integer.valueOf(5)), new Constant(Integer.valueOf(3)));
        assertEquals(">", gt.getSymbol());
        assertEquals(Boolean.TRUE, gt.computeValue(null));

        CoreOperationLessThan lt = new CoreOperationLessThan(new Constant(Integer.valueOf(3)), new Constant(Integer.valueOf(5)));
        assertEquals("<", lt.getSymbol());
        assertEquals(Boolean.TRUE, lt.computeValue(null));

        CoreOperationGreaterThanOrEqual gte = new CoreOperationGreaterThanOrEqual(new Constant(Integer.valueOf(5)), new Constant(Integer.valueOf(5)));
        assertEquals(">=", gte.getSymbol());
        assertEquals(Boolean.TRUE, gte.computeValue(null));

        CoreOperationLessThanOrEqual lte = new CoreOperationLessThanOrEqual(new Constant(Integer.valueOf(5)), new Constant(Integer.valueOf(5)));
        assertEquals("<=", lte.getSymbol());
        assertEquals(Boolean.TRUE, lte.computeValue(null));
    }
}