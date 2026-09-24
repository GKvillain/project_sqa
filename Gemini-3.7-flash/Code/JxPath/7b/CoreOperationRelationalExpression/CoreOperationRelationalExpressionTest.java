package org.apache.commons.jxpath.ri.compiler;

import org.apache.commons.jxpath.ri.EvalContext;
import org.junit.Test;
import static org.junit.Assert.*;

public class CoreOperationRelationalExpressionTest {

    private static class StubRelationalExpression extends CoreOperationRelationalExpression {
        public StubRelationalExpression(Expression[] args) {
            super(args);
        }

        public Object computeValue(EvalContext context) {
            return Boolean.TRUE;
        }

        public String getSymbol() {
            return "~";
        }
    }

    // Tests getPrecedence returns correct value 3
    @Test
    public void testGetPrecedence_validInstance_returnsThree() {
        Expression[] args = new Expression[] { new Constant("a"), new Constant("b") };
        StubRelationalExpression expr = new StubRelationalExpression(args);
        assertEquals(3, expr.getPrecedence());
    }

    // Tests isSymmetric returns false
    @Test
    public void testIsSymmetric_validInstance_returnsFalse() {
        Expression[] args = new Expression[] { new Constant(1), new Constant(2) };
        StubRelationalExpression expr = new StubRelationalExpression(args);
        assertFalse(expr.isSymmetric());
    }

    // Tests constructor with null args
    @Test
    public void testConstructor_nullArgs_initializesSuccessfully() {
        StubRelationalExpression expr = new StubRelationalExpression(null);
        assertNotNull(expr);
        assertEquals(3, expr.getPrecedence());
        assertFalse(expr.isSymmetric());
    }

    // Tests constructor with empty args array
    @Test
    public void testConstructor_emptyArgs_initializesSuccessfully() {
        StubRelationalExpression expr = new StubRelationalExpression(new Expression[0]);
        assertNotNull(expr);
        assertEquals(3, expr.getPrecedence());
        assertFalse(expr.isSymmetric());
    }

    // Tests constructor with multiple arguments
    @Test
    public void testConstructor_multipleArgs_storesArguments() {
        Expression[] args = new Expression[] {
            new Constant("left"),
            new Constant("right")
        };
        StubRelationalExpression expr = new StubRelationalExpression(args);
        assertEquals(2, expr.getArguments().length);
        assertSame(args[0], expr.getArguments()[0]);
        assertSame(args[1], expr.getArguments()[1]);
    }

    // Tests CoreOperationGreaterThan inheritance of getPrecedence and isSymmetric
    @Test
    public void testCoreOperationGreaterThan_precedenceAndSymmetry() {
        CoreOperationGreaterThan expr = new CoreOperationGreaterThan(new Constant(1), new Constant(2));
        assertEquals(3, expr.getPrecedence());
        assertFalse(expr.isSymmetric());
    }

    // Tests CoreOperationGreaterThanOrEqual inheritance of getPrecedence and isSymmetric
    @Test
    public void testCoreOperationGreaterThanOrEqual_precedenceAndSymmetry() {
        CoreOperationGreaterThanOrEqual expr = new CoreOperationGreaterThanOrEqual(new Constant(1), new Constant(2));
        assertEquals(3, expr.getPrecedence());
        assertFalse(expr.isSymmetric());
    }

    // Tests CoreOperationLessThan inheritance of getPrecedence and isSymmetric
    @Test
    public void testCoreOperationLessThan_precedenceAndSymmetry() {
        CoreOperationLessThan expr = new CoreOperationLessThan(new Constant(1), new Constant(2));
        assertEquals(3, expr.getPrecedence());
        assertFalse(expr.isSymmetric());
    }

    // Tests CoreOperationLessThanOrEqual inheritance of getPrecedence and isSymmetric
    @Test
    public void testCoreOperationLessThanOrEqual_precedenceAndSymmetry() {
        CoreOperationLessThanOrEqual expr = new CoreOperationLessThanOrEqual(new Constant(1), new Constant(2));
        assertEquals(3, expr.getPrecedence());
        assertFalse(expr.isSymmetric());
    }

    // Tests computeValue for CoreOperationLessThan
    @Test
    public void testComputeValue_lessThan() {
        CoreOperationLessThan exprTrue = new CoreOperationLessThan(new Constant(1), new Constant(2));
        assertEquals(Boolean.TRUE, exprTrue.computeValue(null));

        CoreOperationLessThan exprFalse = new CoreOperationLessThan(new Constant(2), new Constant(1));
        assertEquals(Boolean.FALSE, exprFalse.computeValue(null));

        CoreOperationLessThan exprEqual = new CoreOperationLessThan(new Constant(2), new Constant(2));
        assertEquals(Boolean.FALSE, exprEqual.computeValue(null));
    }

    // Tests computeValue for CoreOperationLessThanOrEqual
    @Test
    public void testComputeValue_lessThanOrEqual() {
        CoreOperationLessThanOrEqual exprTrue = new CoreOperationLessThanOrEqual(new Constant(1), new Constant(2));
        assertEquals(Boolean.TRUE, exprTrue.computeValue(null));

        CoreOperationLessThanOrEqual exprEqual = new CoreOperationLessThanOrEqual(new Constant(2), new Constant(2));
        assertEquals(Boolean.TRUE, exprEqual.computeValue(null));

        CoreOperationLessThanOrEqual exprFalse = new CoreOperationLessThanOrEqual(new Constant(3), new Constant(2));
        assertEquals(Boolean.FALSE, exprFalse.computeValue(null));
    }

    // Tests computeValue for CoreOperationGreaterThan
    @Test
    public void testComputeValue_greaterThan() {
        CoreOperationGreaterThan exprTrue = new CoreOperationGreaterThan(new Constant(2), new Constant(1));
        assertEquals(Boolean.TRUE, exprTrue.computeValue(null));

        CoreOperationGreaterThan exprFalse = new CoreOperationGreaterThan(new Constant(1), new Constant(2));
        assertEquals(Boolean.FALSE, exprFalse.computeValue(null));

        CoreOperationGreaterThan exprEqual = new CoreOperationGreaterThan(new Constant(2), new Constant(2));
        assertEquals(Boolean.FALSE, exprEqual.computeValue(null));
    }

    // Tests computeValue for CoreOperationGreaterThanOrEqual
    @Test
    public void testComputeValue_greaterThanOrEqual() {
        CoreOperationGreaterThanOrEqual exprTrue = new CoreOperationGreaterThanOrEqual(new Constant(2), new Constant(1));
        assertEquals(Boolean.TRUE, exprTrue.computeValue(null));

        CoreOperationGreaterThanOrEqual exprEqual = new CoreOperationGreaterThanOrEqual(new Constant(2), new Constant(2));
        assertEquals(Boolean.TRUE, exprEqual.computeValue(null));

        CoreOperationGreaterThanOrEqual exprFalse = new CoreOperationGreaterThanOrEqual(new Constant(1), new Constant(2));
        assertEquals(Boolean.FALSE, exprFalse.computeValue(null));
    }

    // Tests computeValue with string representations of numbers
    @Test
    public void testComputeValue_stringOperands() {
        CoreOperationLessThan exprLessThan = new CoreOperationLessThan(new Constant("2"), new Constant("10"));
        assertEquals(Boolean.TRUE, exprLessThan.computeValue(null));

        CoreOperationGreaterThan exprGreaterThan = new CoreOperationGreaterThan(new Constant("10"), new Constant("2"));
        assertEquals(Boolean.TRUE, exprGreaterThan.computeValue(null));
    }

    // Tests computeValue with NaN operands
    @Test
    public void testComputeValue_nanOperands() {
        CoreOperationLessThan expr = new CoreOperationLessThan(new Constant("invalid"), new Constant(1));
        assertEquals(Boolean.FALSE, expr.computeValue(null));
    }

    // Tests getSymbol methods
    @Test
    public void testGetSymbol_allRelationalOperations() {
        assertEquals("<", new CoreOperationLessThan(new Constant(1), new Constant(2)).getSymbol());
        assertEquals("<=", new CoreOperationLessThanOrEqual(new Constant(1), new Constant(2)).getSymbol());
        assertEquals(">", new CoreOperationGreaterThan(new Constant(1), new Constant(2)).getSymbol());
        assertEquals(">=", new CoreOperationGreaterThanOrEqual(new Constant(1), new Constant(2)).getSymbol());
    }
}