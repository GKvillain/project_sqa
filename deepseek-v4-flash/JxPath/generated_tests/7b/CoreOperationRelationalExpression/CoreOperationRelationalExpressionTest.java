package org.apache.commons.jxpath.ri.compiler;

import static org.junit.Assert.*;
import org.junit.Test;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.EvalContext;

/**
 * JUnit 4 test class for CoreOperationRelationalExpression.
 * Tests both the base class methods (getPrecedence, isSymmetric) and the
 * behavior of its concrete subclasses via XPath evaluation.
 */
public class CoreOperationRelationalExpressionTest {

    // ---------- Tests for base class methods ----------

    // Tests getPrecedence returns 3
    @Test
    public void testGetPrecedence_returns3() {
        CoreOperationLessThan less = new CoreOperationLessThan(
                new Constant("1"), new Constant("2"));
        assertEquals(3, less.getPrecedence());
    }

    // Tests isSymmetric returns false
    @Test
    public void testIsSymmetric_returnsFalse() {
        CoreOperationLessThan less = new CoreOperationLessThan(
                new Constant("1"), new Constant("2"));
        assertFalse(less.isSymmetric());
    }

    // ---------- Normal case tests for relational operators ----------

    // Tests 1 < 2 yields true
    @Test
    public void testLessThan_true() {
        JXPathContext ctx = JXPathContext.newContext(null);
        assertTrue((Boolean) JXPathContext.compile("1 < 2").getValue(ctx));
    }

    // Tests 2 < 1 yields false
    @Test
    public void testLessThan_false() {
        JXPathContext ctx = JXPathContext.newContext(null);
        assertFalse((Boolean) JXPathContext.compile("2 < 1").getValue(ctx));
    }

    // Tests 1 <= 1 yields true
    @Test
    public void testLessThanOrEqual_true() {
        JXPathContext ctx = JXPathContext.newContext(null);
        assertTrue((Boolean) JXPathContext.compile("1 <= 1").getValue(ctx));
    }

    // Tests 2 <= 1 yields false
    @Test
    public void testLessThanOrEqual_false() {
        JXPathContext ctx = JXPathContext.newContext(null);
        assertFalse((Boolean) JXPathContext.compile("2 <= 1").getValue(ctx));
    }

    // Tests 2 > 1 yields true
    @Test
    public void testGreaterThan_true() {
        JXPathContext ctx = JXPathContext.newContext(null);
        assertTrue((Boolean) JXPathContext.compile("2 > 1").getValue(ctx));
    }

    // Tests 1 > 2 yields false
    @Test
    public void testGreaterThan_false() {
        JXPathContext ctx = JXPathContext.newContext(null);
        assertFalse((Boolean) JXPathContext.compile("1 > 2").getValue(ctx));
    }

    // Tests 1 >= 1 yields true
    @Test
    public void testGreaterThanOrEqual_true() {
        JXPathContext ctx = JXPathContext.newContext(null);
        assertTrue((Boolean) JXPathContext.compile("1 >= 1").getValue(ctx));
    }

    // Tests 1 >= 2 yields false
    @Test
    public void testGreaterThanOrEqual_false() {
        JXPathContext ctx = JXPathContext.newContext(null);
        assertFalse((Boolean) JXPathContext.compile("1 >= 2").getValue(ctx));
    }

    // ---------- Boundary / Edge cases ----------

    // Tests comparison with NaN (should be false for all operators)
    @Test
    public void testLessThan_withNaN_returnsFalse() {
        JXPathContext ctx = JXPathContext.newContext(null);
        ctx.getVariables().declareVariable("nan", Double.NaN);
        assertFalse((Boolean) JXPathContext.compile("$nan < 1").getValue(ctx));
    }

    @Test
    public void testGreaterThan_withNaN_returnsFalse() {
        JXPathContext ctx = JXPathContext.newContext(null);
        ctx.getVariables().declareVariable("nan", Double.NaN);
        assertFalse((Boolean) JXPathContext.compile("$nan > 1").getValue(ctx));
    }

    @Test
    public void testLessThanOrEqual_withNaN_returnsFalse() {
        JXPathContext ctx = JXPathContext.newContext(null);
        ctx.getVariables().declareVariable("nan", Double.NaN);
        assertFalse((Boolean) JXPathContext.compile("$nan <= 1").getValue(ctx));
    }

    @Test
    public void testGreaterThanOrEqual_withNaN_returnsFalse() {
        JXPathContext ctx = JXPathContext.newContext(null);
        ctx.getVariables().declareVariable("nan", Double.NaN);
        assertFalse((Boolean) JXPathContext.compile("$nan >= 1").getValue(ctx));
    }

    // Tests comparison with Infinity (positive and negative)
    @Test
    public void testLessThan_withPositiveInfinity_returnsFalse() {
        JXPathContext ctx = JXPathContext.newContext(null);
        ctx.getVariables().declareVariable("inf", Double.POSITIVE_INFINITY);
        assertFalse((Boolean) JXPathContext.compile("$inf < 1").getValue(ctx));
    }

    @Test
    public void testGreaterThan_withPositiveInfinity_returnsTrue() {
        JXPathContext ctx = JXPathContext.newContext(null);
        ctx.getVariables().declareVariable("inf", Double.POSITIVE_INFINITY);
        assertTrue((Boolean) JXPathContext.compile("$inf > 1").getValue(ctx));
    }

    @Test
    public void testLessThan_withNegativeInfinity_returnsTrue() {
        JXPathContext ctx = JXPathContext.newContext(null);
        ctx.getVariables().declareVariable("ninf", Double.NEGATIVE_INFINITY);
        assertTrue((Boolean) JXPathContext.compile("$ninf < 1").getValue(ctx));
    }

    @Test
    public void testGreaterThan_withNegativeInfinity_returnsFalse() {
        JXPathContext ctx = JXPathContext.newContext(null);
        ctx.getVariables().declareVariable("ninf", Double.NEGATIVE_INFINITY);
        assertFalse((Boolean) JXPathContext.compile("$ninf > 1").getValue(ctx));
    }

    // Tests comparison with null (should return false without exception)
    @Test
    public void testLessThan_withNull_returnsFalse() {
        JXPathContext ctx = JXPathContext.newContext(null);
        ctx.getVariables().declareVariable("nul", null);
        assertFalse((Boolean) JXPathContext.compile("$nul < 1").getValue(ctx));
    }

    @Test
    public void testGreaterThan_withNull_returnsFalse() {
        JXPathContext ctx = JXPathContext.newContext(null);
        ctx.getVariables().declareVariable("nul", null);
        assertFalse((Boolean) JXPathContext.compile("$nul > 1").getValue(ctx));
    }

    // Tests mixed types: string "5" compared to number 6
    @Test
    public void testLessThan_stringAndNumber_returnsTrue() {
        JXPathContext ctx = JXPathContext.newContext(null);
        assertTrue((Boolean) JXPathContext.compile("'5' < 6").getValue(ctx));
    }

    // Tests non-numeric string compared to number
    @Test
    public void testLessThan_nonNumericString_returnsFalse() {
        JXPathContext ctx = JXPathContext.newContext(null);
        assertFalse((Boolean) JXPathContext.compile("'abc' < 5").getValue(ctx));
    }

    // Tests zero boundary: 0 < 0 false, 0 <= 0 true
    @Test
    public void testLessThan_zeroEqual_returnsFalse() {
        JXPathContext ctx = JXPathContext.newContext(null);
        assertFalse((Boolean) JXPathContext.compile("0 < 0").getValue(ctx));
    }

    @Test
    public void testLessThanOrEqual_zeroEqual_returnsTrue() {
        JXPathContext ctx = JXPathContext.newContext(null);
        assertTrue((Boolean) JXPathContext.compile("0 <= 0").getValue(ctx));
    }
}