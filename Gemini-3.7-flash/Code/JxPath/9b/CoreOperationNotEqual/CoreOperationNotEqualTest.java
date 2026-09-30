package org.apache.commons.jxpath.ri.compiler;

import org.junit.Test;
import static org.junit.Assert.*;

public class CoreOperationNotEqualTest {

    // Tests that getSymbol returns the correct "!=" operator symbol
    @Test
    public void testGetSymbol_returnsCorrectSymbol() {
        Constant arg1 = new Constant("a");
        Constant arg2 = new Constant("b");
        CoreOperationNotEqual op = new CoreOperationNotEqual(arg1, arg2);
        assertEquals("!=", op.getSymbol());
    }

    // Tests computeValue with identical string literals returning Boolean.FALSE
    @Test
    public void testComputeValue_equalStrings_returnsFalse() {
        Constant arg1 = new Constant("hello");
        Constant arg2 = new Constant("hello");
        CoreOperationNotEqual op = new CoreOperationNotEqual(arg1, arg2);
        Object result = op.computeValue(null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests computeValue with different string literals returning Boolean.TRUE
    @Test
    public void testComputeValue_differentStrings_returnsTrue() {
        Constant arg1 = new Constant("hello");
        Constant arg2 = new Constant("world");
        CoreOperationNotEqual op = new CoreOperationNotEqual(arg1, arg2);
        Object result = op.computeValue(null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests computeValue with equal double numbers returning Boolean.FALSE
    @Test
    public void testComputeValue_equalNumbers_returnsFalse() {
        Constant arg1 = new Constant(new Double(42.0));
        Constant arg2 = new Constant(new Double(42.0));
        CoreOperationNotEqual op = new CoreOperationNotEqual(arg1, arg2);
        Object result = op.computeValue(null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests computeValue with different double numbers returning Boolean.TRUE
    @Test
    public void testComputeValue_differentNumbers_returnsTrue() {
        Constant arg1 = new Constant(new Double(42.0));
        Constant arg2 = new Constant(new Double(24.0));
        CoreOperationNotEqual op = new CoreOperationNotEqual(arg1, arg2);
        Object result = op.computeValue(null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests computeValue with equal string and number representation returning Boolean.FALSE
    @Test
    public void testComputeValue_equalStringAndNumber_returnsFalse() {
        Constant arg1 = new Constant("100");
        Constant arg2 = new Constant(new Double(100.0));
        CoreOperationNotEqual op = new CoreOperationNotEqual(arg1, arg2);
        Object result = op.computeValue(null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests computeValue with different string and number returning Boolean.TRUE
    @Test
    public void testComputeValue_differentStringAndNumber_returnsTrue() {
        Constant arg1 = new Constant("100");
        Constant arg2 = new Constant(new Double(200.0));
        CoreOperationNotEqual op = new CoreOperationNotEqual(arg1, arg2);
        Object result = op.computeValue(null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests computeValue with zero values returning Boolean.FALSE
    @Test
    public void testComputeValue_zeroAndZero_returnsFalse() {
        Constant arg1 = new Constant(new Double(0.0));
        Constant arg2 = new Constant(new Double(0.0));
        CoreOperationNotEqual op = new CoreOperationNotEqual(arg1, arg2);
        Object result = op.computeValue(null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests computeValue with zero and non-zero returning Boolean.TRUE
    @Test
    public void testComputeValue_zeroAndNonZero_returnsTrue() {
        Constant arg1 = new Constant(new Double(0.0));
        Constant arg2 = new Constant(new Double(1.0));
        CoreOperationNotEqual op = new CoreOperationNotEqual(arg1, arg2);
        Object result = op.computeValue(null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests computeValue with negative numbers that are equal returning Boolean.FALSE
    @Test
    public void testComputeValue_negativeNumbersEqual_returnsFalse() {
        Constant arg1 = new Constant(new Double(-15.5));
        Constant arg2 = new Constant(new Double(-15.5));
        CoreOperationNotEqual op = new CoreOperationNotEqual(arg1, arg2);
        Object result = op.computeValue(null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests computeValue with negative and positive numbers returning Boolean.TRUE
    @Test
    public void testComputeValue_negativeAndPositive_returnsTrue() {
        Constant arg1 = new Constant(new Double(-5.0));
        Constant arg2 = new Constant(new Double(5.0));
        CoreOperationNotEqual op = new CoreOperationNotEqual(arg1, arg2);
        Object result = op.computeValue(null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests computeValue with empty strings returning Boolean.FALSE
    @Test
    public void testComputeValue_emptyStringsEqual_returnsFalse() {
        Constant arg1 = new Constant("");
        Constant arg2 = new Constant("");
        CoreOperationNotEqual op = new CoreOperationNotEqual(arg1, arg2);
        Object result = op.computeValue(null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests computeValue with empty string and non-empty string returning Boolean.TRUE
    @Test
    public void testComputeValue_emptyStringAndNonEmptyString_returnsTrue() {
        Constant arg1 = new Constant("");
        Constant arg2 = new Constant("non-empty");
        CoreOperationNotEqual op = new CoreOperationNotEqual(arg1, arg2);
        Object result = op.computeValue(null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests computeValue with NaN comparison returning Boolean.TRUE
    @Test
    public void testComputeValue_nanValues_returnsTrue() {
        Constant arg1 = new Constant(new Double(Double.NaN));
        Constant arg2 = new Constant(new Double(Double.NaN));
        CoreOperationNotEqual op = new CoreOperationNotEqual(arg1, arg2);
        Object result = op.computeValue(null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests computeValue with boolean true and boolean true returning Boolean.FALSE
    @Test
    public void testComputeValue_booleanTrueAndTrue_returnsFalse() {
        CoreOperationEqual subOp1 = new CoreOperationEqual(new Constant("a"), new Constant("a"));
        CoreOperationEqual subOp2 = new CoreOperationEqual(new Constant("b"), new Constant("b"));
        CoreOperationNotEqual op = new CoreOperationNotEqual(subOp1, subOp2);
        Object result = op.computeValue(null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests computeValue with boolean true and boolean false returning Boolean.TRUE
    @Test
    public void testComputeValue_booleanTrueAndFalse_returnsTrue() {
        CoreOperationEqual subOp1 = new CoreOperationEqual(new Constant("a"), new Constant("a"));
        CoreOperationEqual subOp2 = new CoreOperationEqual(new Constant("a"), new Constant("b"));
        CoreOperationNotEqual op = new CoreOperationNotEqual(subOp1, subOp2);
        Object result = op.computeValue(null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests computeValue with boolean false and boolean false returning Boolean.FALSE
    @Test
    public void testComputeValue_booleanFalseAndFalse_returnsFalse() {
        CoreOperationEqual subOp1 = new CoreOperationEqual(new Constant("x"), new Constant("y"));
        CoreOperationEqual subOp2 = new CoreOperationEqual(new Constant("1"), new Constant("2"));
        CoreOperationNotEqual op = new CoreOperationNotEqual(subOp1, subOp2);
        Object result = op.computeValue(null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests computeValue with boolean true and non-zero number returning Boolean.FALSE
    @Test
    public void testComputeValue_booleanTrueAndNonZeroNumber_returnsFalse() {
        CoreOperationEqual subOp = new CoreOperationEqual(new Constant("a"), new Constant("a"));
        Constant num = new Constant(new Double(1.0));
        CoreOperationNotEqual op = new CoreOperationNotEqual(subOp, num);
        Object result = op.computeValue(null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests computeValue with boolean true and zero number returning Boolean.TRUE
    @Test
    public void testComputeValue_booleanTrueAndZeroNumber_returnsTrue() {
        CoreOperationEqual subOp = new CoreOperationEqual(new Constant("a"), new Constant("a"));
        Constant zero = new Constant(new Double(0.0));
        CoreOperationNotEqual op = new CoreOperationNotEqual(subOp, zero);
        Object result = op.computeValue(null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests computeValue with boolean false and zero number returning Boolean.FALSE
    @Test
    public void testComputeValue_booleanFalseAndZeroNumber_returnsFalse() {
        CoreOperationEqual subOp = new CoreOperationEqual(new Constant("a"), new Constant("b"));
        Constant zero = new Constant(new Double(0.0));
        CoreOperationNotEqual op = new CoreOperationNotEqual(subOp, zero);
        Object result = op.computeValue(null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests computeValue with boolean true and non-empty string returning Boolean.FALSE
    @Test
    public void testComputeValue_booleanTrueAndNonEmptyString_returnsFalse() {
        CoreOperationEqual subOp = new CoreOperationEqual(new Constant("a"), new Constant("a"));
        Constant str = new Constant("hello");
        CoreOperationNotEqual op = new CoreOperationNotEqual(subOp, str);
        Object result = op.computeValue(null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests computeValue with boolean true and empty string returning Boolean.TRUE
    @Test
    public void testComputeValue_booleanTrueAndEmptyString_returnsTrue() {
        CoreOperationEqual subOp = new CoreOperationEqual(new Constant("a"), new Constant("a"));
        Constant str = new Constant("");
        CoreOperationNotEqual op = new CoreOperationNotEqual(subOp, str);
        Object result = op.computeValue(null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests computeValue with boolean false and empty string returning Boolean.FALSE
    @Test
    public void testComputeValue_booleanFalseAndEmptyString_returnsFalse() {
        CoreOperationEqual subOp = new CoreOperationEqual(new Constant("a"), new Constant("b"));
        Constant str = new Constant("");
        CoreOperationNotEqual op = new CoreOperationNotEqual(subOp, str);
        Object result = op.computeValue(null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests toString representation of CoreOperationNotEqual
    @Test
    public void testToString() {
        Constant arg1 = new Constant("foo");
        Constant arg2 = new Constant("bar");
        CoreOperationNotEqual op = new CoreOperationNotEqual(arg1, arg2);
        assertEquals("'foo' != 'bar'", op.toString());
    }
}