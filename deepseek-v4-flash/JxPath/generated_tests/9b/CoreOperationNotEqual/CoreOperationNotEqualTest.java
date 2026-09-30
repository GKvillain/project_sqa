package org.apache.commons.jxpath.ri.compiler;

import org.junit.Test;
import static org.junit.Assert.*;
import org.apache.commons.jxpath.JXPathContext;

public class CoreOperationNotEqualTest {

    // Tests getSymbol method
    @Test
    public void testGetSymbol_returnsNotEqualSymbol() {
        CoreOperationNotEqual op = new CoreOperationNotEqual(new Constant("a"), new Constant("b"));
        assertEquals("!=", op.getSymbol());
    }

    // Helper to evaluate XPath inequality expressions
    private boolean eval(String xpath) {
        JXPathContext context = JXPathContext.newContext(null);
        return (Boolean) context.getValue(xpath);
    }

    // Tests equal numbers -> false
    @Test
    public void testComputeValue_equalNumbers_returnsFalse() {
        assertFalse(eval("1 != 1"));
    }

    // Tests unequal numbers -> true
    @Test
    public void testComputeValue_notEqualNumbers_returnsTrue() {
        assertTrue(eval("1 != 2"));
    }

    // Tests equal strings -> false
    @Test
    public void testComputeValue_equalStrings_returnsFalse() {
        assertFalse(eval("'a' != 'a'"));
    }

    // Tests unequal strings -> true
    @Test
    public void testComputeValue_notEqualStrings_returnsTrue() {
        assertTrue(eval("'a' != 'b'"));
    }

    // Tests equal booleans -> false
    @Test
    public void testComputeValue_equalBooleans_returnsFalse() {
        assertFalse(eval("true() != true()"));
    }

    // Tests unequal booleans -> true
    @Test
    public void testComputeValue_notEqualBooleans_returnsTrue() {
        assertTrue(eval("true() != false()"));
    }

    // Tests NaN compared to NaN -> true (defect detection)
    @Test
    public void testComputeValue_NaNComparedToNaN_returnsTrue() {
        JXPathContext context = JXPathContext.newContext(null);
        context.getVariables().declareVariable("nan", Double.NaN);
        assertTrue((Boolean) context.getValue("$nan != $nan"));
    }

    // Tests NaN compared to a number -> true
    @Test
    public void testComputeValue_NaNComparedToNumber_returnsTrue() {
        JXPathContext context = JXPathContext.newContext(null);
        context.getVariables().declareVariable("nan", Double.NaN);
        assertTrue((Boolean) context.getValue("$nan != 0"));
    }

    // Tests null compared to non-null -> true
    @Test
    public void testComputeValue_nullComparedToNonNull_returnsTrue() {
        JXPathContext context = JXPathContext.newContext(null);
        context.getVariables().declareVariable("n", null);
        assertTrue((Boolean) context.getValue("$n != 'abc'"));
    }

    // Tests two nulls compared -> false (null != null)
    @Test
    public void testComputeValue_nullsCompared_returnsFalse() {
        JXPathContext context = JXPathContext.newContext(null);
        context.getVariables().declareVariable("n", null);
        assertFalse((Boolean) context.getValue("$n != $n"));
    }

    // Tests zero and negative zero -> false
    @Test
    public void testComputeValue_zeroAndNegativeZero_returnsFalse() {
        assertFalse(eval("0 != -0"));
    }

    // Tests null context throws NullPointerException
    @Test(expected = NullPointerException.class)
    public void testComputeValue_nullContext_throwsNullPointerException() {
        CoreOperationNotEqual op = new CoreOperationNotEqual(new Constant("a"), new Constant("b"));
        op.computeValue(null);
    }
}