package org.apache.commons.jxpath.ri.compiler;

import org.apache.commons.jxpath.ri.EvalContext;
import org.junit.Test;
import static org.junit.Assert.*;

public class CoreOperationNotEqualTest {

    // Tests getSymbol returns correct not-equal symbol
    @Test
    public void testGetSymbol_returnsNotEqualSymbol() {
        Constant arg1 = new Constant("a");
        Constant arg2 = new Constant("b");
        CoreOperationNotEqual op = new CoreOperationNotEqual(arg1, arg2);
        assertEquals("!=", op.getSymbol());
    }

    // Tests computeValue with identical string constants
    @Test
    public void testComputeValue_equalStrings_returnsFalse() {
        Constant arg1 = new Constant("test");
        Constant arg2 = new Constant("test");
        CoreOperationNotEqual op = new CoreOperationNotEqual(arg1, arg2);
        Object result = op.computeValue((EvalContext) null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests computeValue with different string constants
    @Test
    public void testComputeValue_differentStrings_returnsTrue() {
        Constant arg1 = new Constant("test1");
        Constant arg2 = new Constant("test2");
        CoreOperationNotEqual op = new CoreOperationNotEqual(arg1, arg2);
        Object result = op.computeValue((EvalContext) null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests computeValue with identical numeric constants
    @Test
    public void testComputeValue_equalNumbers_returnsFalse() {
        Constant arg1 = new Constant(Double.valueOf(42.0));
        Constant arg2 = new Constant(Double.valueOf(42.0));
        CoreOperationNotEqual op = new CoreOperationNotEqual(arg1, arg2);
        Object result = op.computeValue((EvalContext) null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests computeValue with different numeric constants
    @Test
    public void testComputeValue_differentNumbers_returnsTrue() {
        Constant arg1 = new Constant(Double.valueOf(1.0));
        Constant arg2 = new Constant(Double.valueOf(2.0));
        CoreOperationNotEqual op = new CoreOperationNotEqual(arg1, arg2);
        Object result = op.computeValue((EvalContext) null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests computeValue with boolean true and number 1.0 (equal in XPath)
    @Test
    public void testComputeValue_booleanTrueAndNumberOne_returnsFalse() {
        Constant arg1 = new Constant(Double.valueOf(1.0));
        Constant arg2 = new Constant(Double.valueOf(1.0));
        CoreOperationNotEqual op = new CoreOperationNotEqual(arg1, arg2);
        Object result = op.computeValue((EvalContext) null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests computeValue with zero and non-zero numbers
    @Test
    public void testComputeValue_zeroAndNonZero_returnsTrue() {
        Constant arg1 = new Constant(Double.valueOf(0.0));
        Constant arg2 = new Constant(Double.valueOf(1.0));
        CoreOperationNotEqual op = new CoreOperationNotEqual(arg1, arg2);
        Object result = op.computeValue((EvalContext) null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests computeValue with empty string and non-empty string
    @Test
    public void testComputeValue_emptyStringAndNonEmptyString_returnsTrue() {
        Constant arg1 = new Constant("");
        Constant arg2 = new Constant("content");
        CoreOperationNotEqual op = new CoreOperationNotEqual(arg1, arg2);
        Object result = op.computeValue((EvalContext) null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests computeValue with two empty strings
    @Test
    public void testComputeValue_bothEmptyStrings_returnsFalse() {
        Constant arg1 = new Constant("");
        Constant arg2 = new Constant("");
        CoreOperationNotEqual op = new CoreOperationNotEqual(arg1, arg2);
        Object result = op.computeValue((EvalContext) null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests computeValue with negative numbers
    @Test
    public void testComputeValue_negativeNumbersEqual_returnsFalse() {
        Constant arg1 = new Constant(Double.valueOf(-5.5));
        Constant arg2 = new Constant(Double.valueOf(-5.5));
        CoreOperationNotEqual op = new CoreOperationNotEqual(arg1, arg2);
        Object result = op.computeValue((EvalContext) null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests computeValue with string and number representations
    @Test
    public void testComputeValue_stringAndNumberEqual_returnsFalse() {
        Constant arg1 = new Constant("42");
        Constant arg2 = new Constant(Double.valueOf(42.0));
        CoreOperationNotEqual op = new CoreOperationNotEqual(arg1, arg2);
        Object result = op.computeValue((EvalContext) null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests computeValue with string and number representations that differ
    @Test
    public void testComputeValue_stringAndNumberDifferent_returnsTrue() {
        Constant arg1 = new Constant("42");
        Constant arg2 = new Constant(Double.valueOf(43.0));
        CoreOperationNotEqual op = new CoreOperationNotEqual(arg1, arg2);
        Object result = op.computeValue((EvalContext) null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests computeValue with boolean expressions equal
    @Test
    public void testComputeValue_booleansEqual_returnsFalse() {
        CoreOperationNotEqual op1 = new CoreOperationNotEqual(new Constant("a"), new Constant("b")); // true
        CoreOperationNotEqual op2 = new CoreOperationNotEqual(new Constant("c"), new Constant("d")); // true
        CoreOperationNotEqual op = new CoreOperationNotEqual(op1, op2);
        Object result = op.computeValue((EvalContext) null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests computeValue with boolean expressions different
    @Test
    public void testComputeValue_booleansDifferent_returnsTrue() {
        CoreOperationNotEqual op1 = new CoreOperationNotEqual(new Constant("a"), new Constant("a")); // false
        CoreOperationNotEqual op2 = new CoreOperationNotEqual(new Constant("c"), new Constant("d")); // true
        CoreOperationNotEqual op = new CoreOperationNotEqual(op1, op2);
        Object result = op.computeValue((EvalContext) null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests isSymmetric returns true
    @Test
    public void testIsSymmetric_returnsTrue() {
        Constant arg1 = new Constant("a");
        Constant arg2 = new Constant("b");
        CoreOperationNotEqual op = new CoreOperationNotEqual(arg1, arg2);
        assertTrue(op.isSymmetric());
    }
}