package org.apache.commons.jxpath.ri.compiler;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.JXPathContextReferenceImpl;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CoreOperationEqualTest {

    private EvalContext evalContext;

    @Before
    public void setUp() {
        JXPathContextReferenceImpl jxPathContext = (JXPathContextReferenceImpl) JXPathContext.newContext(new Object());
        evalContext = (EvalContext) jxPathContext.getAbsoluteRootContext();
    }

    // Tests getSymbol returns "="
    @Test
    public void testGetSymbol_returnsEqualSign() {
        CoreOperationEqual op = new CoreOperationEqual(new Constant("a"), new Constant("a"));
        assertEquals("=", op.getSymbol());
    }

    // Tests equality between identical string constants
    @Test
    public void testComputeValue_identicalStrings_returnsTrue() {
        CoreOperationEqual op = new CoreOperationEqual(new Constant("test"), new Constant("test"));
        Object result = op.computeValue(evalContext);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests equality between different string constants
    @Test
    public void testComputeValue_differentStrings_returnsFalse() {
        CoreOperationEqual op = new CoreOperationEqual(new Constant("test1"), new Constant("test2"));
        Object result = op.computeValue(evalContext);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests equality between identical integer/number constants
    @Test
    public void testComputeValue_identicalNumbers_returnsTrue() {
        CoreOperationEqual op = new CoreOperationEqual(new Constant(Double.valueOf(42.0)), new Constant(Double.valueOf(42.0)));
        Object result = op.computeValue(evalContext);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests equality between different number constants
    @Test
    public void testComputeValue_differentNumbers_returnsFalse() {
        CoreOperationEqual op = new CoreOperationEqual(new Constant(Double.valueOf(42.0)), new Constant(Double.valueOf(24.0)));
        Object result = op.computeValue(evalContext);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests equality between identical boolean constants
    @Test
    public void testComputeValue_identicalBooleans_returnsTrue() {
        CoreOperationEqual op = new CoreOperationEqual(new Constant("true"), new Constant("true"));
        Object result = op.computeValue(evalContext);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests equality between boolean true and boolean false
    @Test
    public void testComputeValue_differentBooleans_returnsFalse() {
        CoreOperationEqual trueExpr = new CoreOperationEqual(new Constant(Double.valueOf(1.0)), new Constant(Double.valueOf(1.0)));
        CoreOperationEqual falseExpr = new CoreOperationEqual(new Constant(Double.valueOf(1.0)), new Constant(Double.valueOf(2.0)));
        CoreOperationEqual op = new CoreOperationEqual(trueExpr, falseExpr);
        Object result = op.computeValue(evalContext);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests equality between number and equivalent string representation
    @Test
    public void testComputeValue_numberAndEquivalentString_returnsTrue() {
        CoreOperationEqual op = new CoreOperationEqual(new Constant(Double.valueOf(10.0)), new Constant("10"));
        Object result = op.computeValue(evalContext);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests equality between number and non-equivalent string representation
    @Test
    public void testComputeValue_numberAndDifferentString_returnsFalse() {
        CoreOperationEqual op = new CoreOperationEqual(new Constant(Double.valueOf(10.0)), new Constant("20"));
        Object result = op.computeValue(evalContext);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests equality between null evalContext and constant expressions
    @Test
    public void testComputeValue_nullContext_computesCorrectly() {
        CoreOperationEqual op = new CoreOperationEqual(new Constant("hello"), new Constant("hello"));
        Object result = op.computeValue(null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests equality with zero values
    @Test
    public void testComputeValue_zeroValues_returnsTrue() {
        CoreOperationEqual op = new CoreOperationEqual(new Constant(Double.valueOf(0.0)), new Constant(Double.valueOf(0.0)));
        Object result = op.computeValue(evalContext);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests equality with negative values
    @Test
    public void testComputeValue_negativeValues_returnsTrue() {
        CoreOperationEqual op = new CoreOperationEqual(new Constant(Double.valueOf(-5.0)), new Constant(Double.valueOf(-5.0)));
        Object result = op.computeValue(evalContext);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests equality with empty strings
    @Test
    public void testComputeValue_emptyStrings_returnsTrue() {
        CoreOperationEqual op = new CoreOperationEqual(new Constant(""), new Constant(""));
        Object result = op.computeValue(evalContext);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests equality between empty string and non-empty string
    @Test
    public void testComputeValue_emptyStringAndNonEmptyString_returnsFalse() {
        CoreOperationEqual op = new CoreOperationEqual(new Constant(""), new Constant("abc"));
        Object result = op.computeValue(evalContext);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests equality with null constant values
    @Test
    public void testComputeValue_nullConstants_returnsTrue() {
        CoreOperationEqual op = new CoreOperationEqual(new Constant((String) null), new Constant((String) null));
        Object result = op.computeValue(evalContext);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests equality between boolean and boolean evaluating to true
    @Test
    public void testComputeValue_identicalBooleanExpressions_returnsTrue() {
        CoreOperationEqual trueExpr1 = new CoreOperationEqual(new Constant(Double.valueOf(1.0)), new Constant(Double.valueOf(1.0)));
        CoreOperationEqual trueExpr2 = new CoreOperationEqual(new Constant(Double.valueOf(2.0)), new Constant(Double.valueOf(2.0)));
        CoreOperationEqual op = new CoreOperationEqual(trueExpr1, trueExpr2);
        Object result = op.computeValue(evalContext);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests equality between boolean and string
    @Test
    public void testComputeValue_booleanAndString_returnsCorrectResult() {
        CoreOperationEqual trueExpr = new CoreOperationEqual(new Constant(Double.valueOf(1.0)), new Constant(Double.valueOf(1.0)));
        CoreOperationEqual op = new CoreOperationEqual(trueExpr, new Constant("true"));
        Object result = op.computeValue(evalContext);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests equality between boolean and number
    @Test
    public void testComputeValue_booleanAndNumber_returnsCorrectResult() {
        CoreOperationEqual trueExpr = new CoreOperationEqual(new Constant(Double.valueOf(1.0)), new Constant(Double.valueOf(1.0)));
        CoreOperationEqual op = new CoreOperationEqual(trueExpr, new Constant(Double.valueOf(1.0)));
        Object result = op.computeValue(evalContext);
        assertEquals(Boolean.TRUE, result);
    }
}