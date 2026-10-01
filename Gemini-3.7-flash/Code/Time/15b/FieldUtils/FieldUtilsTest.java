package org.joda.time.field;

import org.joda.time.DateTimeFieldType;
import org.joda.time.IllegalFieldValueException;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for FieldUtils.
 */
public class FieldUtilsTest {

    // Tests safeNegate with normal value
    @Test
    public void testSafeNegate_normalValue_returnsNegated() {
        assertEquals(-5, FieldUtils.safeNegate(5));
        assertEquals(5, FieldUtils.safeNegate(-5));
        assertEquals(0, FieldUtils.safeNegate(0));
    }

    // Tests safeNegate overflow exception on Integer.MIN_VALUE
    @Test(expected = ArithmeticException.class)
    public void testSafeNegate_minValue_throwsException() {
        FieldUtils.safeNegate(Integer.MIN_VALUE);
    }

    // Tests safeAdd for int with normal values and boundary
    @Test
    public void testSafeAddInt_validValues_returnsSum() {
        assertEquals(5, FieldUtils.safeAdd(2, 3));
        assertEquals(-1, FieldUtils.safeAdd(2, -3));
        assertEquals(Integer.MAX_VALUE, FieldUtils.safeAdd(Integer.MAX_VALUE - 1, 1));
        assertEquals(Integer.MIN_VALUE, FieldUtils.safeAdd(Integer.MIN_VALUE + 1, -1));
    }

    // Tests safeAdd for int overflow exception
    @Test(expected = ArithmeticException.class)
    public void testSafeAddInt_overflow_throwsException() {
        FieldUtils.safeAdd(Integer.MAX_VALUE, 1);
    }

    // Tests safeAdd for long with normal values and overflow
    @Test
    public void testSafeAddLong_validValues_returnsSum() {
        assertEquals(5L, FieldUtils.safeAdd(2L, 3L));
        assertEquals(-1L, FieldUtils.safeAdd(2L, -3L));
    }

    // Tests safeAdd for long overflow exception
    @Test(expected = ArithmeticException.class)
    public void testSafeAddLong_overflow_throwsException() {
        FieldUtils.safeAdd(Long.MAX_VALUE, 1L);
    }

    // Tests safeSubtract for long with normal values
    @Test
    public void testSafeSubtractLong_validValues_returnsDifference() {
        assertEquals(1L, FieldUtils.safeSubtract(3L, 2L));
        assertEquals(5L, FieldUtils.safeSubtract(2L, -3L));
    }

    // Tests safeSubtract for long overflow exception
    @Test(expected = ArithmeticException.class)
    public void testSafeSubtractLong_overflow_throwsException() {
        FieldUtils.safeSubtract(Long.MIN_VALUE, 1L);
    }

    // Tests safeMultiply for int with normal values and overflow
    @Test
    public void testSafeMultiplyInt_validValues_returnsProduct() {
        assertEquals(6, FieldUtils.safeMultiply(2, 3));
        assertEquals(-6, FieldUtils.safeMultiply(2, -3));
        assertEquals(0, FieldUtils.safeMultiply(0, 5));
    }

    // Tests safeMultiply for int overflow exception
    @Test(expected = ArithmeticException.class)
    public void testSafeMultiplyInt_overflow_throwsException() {
        FieldUtils.safeMultiply(Integer.MAX_VALUE, 2);
    }

    // Tests safeMultiply(long, int) normal values and edge cases (0, 1, -1)
    @Test
    public void testSafeMultiplyLongInt_specialValues_returnsProduct() {
        assertEquals(0L, FieldUtils.safeMultiply(100L, 0));
        assertEquals(100L, FieldUtils.safeMultiply(100L, 1));
        assertEquals(-100L, FieldUtils.safeMultiply(100L, -1));
        assertEquals(300L, FieldUtils.safeMultiply(100L, 3));
    }

    // Tests safeMultiply(long, int) overflow on Long.MIN_VALUE * -1
    @Test(expected = ArithmeticException.class)
    public void testSafeMultiplyLongInt_minValueMinusOne_throwsException() {
        FieldUtils.safeMultiply(Long.MIN_VALUE, -1);
    }

    // Tests safeMultiply(long, int) general overflow exception
    @Test(expected = ArithmeticException.class)
    public void testSafeMultiplyLongInt_overflow_throwsException() {
        FieldUtils.safeMultiply(Long.MAX_VALUE, 2);
    }

    // Tests safeMultiply(long, long) normal values and edge cases
    @Test
    public void testSafeMultiplyLongLong_validValues_returnsProduct() {
        assertEquals(0L, FieldUtils.safeMultiply(0L, 5L));
        assertEquals(0L, FieldUtils.safeMultiply(5L, 0L));
        assertEquals(5L, FieldUtils.safeMultiply(1L, 5L));
        assertEquals(5L, FieldUtils.safeMultiply(5L, 1L));
        assertEquals(6L, FieldUtils.safeMultiply(2L, 3L));
        assertEquals(-6L, FieldUtils.safeMultiply(2L, -3L));
    }

    // Tests safeMultiply(long, long) overflow with Long.MIN_VALUE and -1
    @Test(expected = ArithmeticException.class)
    public void testSafeMultiplyLongLong_minValueMinusOne_throwsException() {
        FieldUtils.safeMultiply(Long.MIN_VALUE, -1L);
    }

    // Tests safeMultiply(long, long) general overflow exception
    @Test(expected = ArithmeticException.class)
    public void testSafeMultiplyLongLong_overflow_throwsException() {
        FieldUtils.safeMultiply(Long.MAX_VALUE, 2L);
    }

    // Tests safeToInt with valid values
    @Test
    public void testSafeToInt_validValues_returnsInt() {
        assertEquals(100, FieldUtils.safeToInt(100L));
        assertEquals(Integer.MAX_VALUE, FieldUtils.safeToInt((long) Integer.MAX_VALUE));
        assertEquals(Integer.MIN_VALUE, FieldUtils.safeToInt((long) Integer.MIN_VALUE));
    }

    // Tests safeToInt overflow exception
    @Test(expected = ArithmeticException.class)
    public void testSafeToInt_overflow_throwsException() {
        FieldUtils.safeToInt((long) Integer.MAX_VALUE + 1L);
    }

    // Tests safeMultiplyToInt normal calculation and overflow
    @Test
    public void testSafeMultiplyToInt_validValues_returnsInt() {
        assertEquals(6, FieldUtils.safeMultiplyToInt(2L, 3L));
    }

    // Tests safeMultiplyToInt overflow exception
    @Test(expected = ArithmeticException.class)
    public void testSafeMultiplyToInt_overflow_throwsException() {
        FieldUtils.safeMultiplyToInt(Long.MAX_VALUE, 2L);
    }

    // Tests verifyValueBounds with valid bounds
    @Test
    public void testVerifyValueBounds_validBounds_noException() {
        FieldUtils.verifyValueBounds("testField", 5, 1, 10);
        FieldUtils.verifyValueBounds("testField", 1, 1, 10);
        FieldUtils.verifyValueBounds("testField", 10, 1, 10);
        FieldUtils.verifyValueBounds(DateTimeFieldType.dayOfMonth(), 15, 1, 31);
    }

    // Tests verifyValueBounds lower bound violation throws exception
    @Test(expected = IllegalFieldValueException.class)
    public void testVerifyValueBounds_belowLowerBound_throwsException() {
        FieldUtils.verifyValueBounds("testField", 0, 1, 10);
    }

    // Tests verifyValueBounds upper bound violation throws exception
    @Test(expected = IllegalFieldValueException.class)
    public void testVerifyValueBounds_aboveUpperBound_throwsException() {
        FieldUtils.verifyValueBounds(DateTimeFieldType.dayOfMonth(), 32, 1, 31);
    }

    // Tests getWrappedValue with wrap arithmetic
    @Test
    public void testGetWrappedValue_validInputs_returnsWrapped() {
        assertEquals(5, FieldUtils.getWrappedValue(5, 1, 10));
        assertEquals(1, FieldUtils.getWrappedValue(11, 1, 10));
        assertEquals(10, FieldUtils.getWrappedValue(0, 1, 10));
        assertEquals(6, FieldUtils.getWrappedValue(1, 5, 1, 10));
        assertEquals(10, FieldUtils.getWrappedValue(1, -1, 1, 10));
    }

    // Tests getWrappedValue invalid bounds exception
    @Test(expected = IllegalArgumentException.class)
    public void testGetWrappedValue_minGreaterEqualMax_throwsException() {
        FieldUtils.getWrappedValue(5, 10, 5);
    }

    // Tests equals handling equal, non-equal, and null objects
    @Test
    public void testEquals_variousInputs_returnsExpected() {
        assertTrue(FieldUtils.equals(null, null));
        assertFalse(FieldUtils.equals("a", null));
        assertFalse(FieldUtils.equals(null, "a"));
        assertTrue(FieldUtils.equals("a", "a"));
        assertFalse(FieldUtils.equals("a", "b"));
    }

    // --- Additional Tests for Uncovered Branches and Methods ---

    // Tests safeAdd for int underflow exception
    @Test(expected = ArithmeticException.class)
    public void testSafeAddInt_underflow_throwsException() {
        FieldUtils.safeAdd(Integer.MIN_VALUE, -1);
    }

    // Tests safeAdd for long underflow exception
    @Test(expected = ArithmeticException.class)
    public void testSafeAddLong_underflow_throwsException() {
        FieldUtils.safeAdd(Long.MIN_VALUE, -1L);
    }

    // Tests safeSubtract for long positive overflow exception
    @Test(expected = ArithmeticException.class)
    public void testSafeSubtractLong_positiveOverflow_throwsException() {
        FieldUtils.safeSubtract(Long.MAX_VALUE, -1L);
    }

    // Tests safeMultiply for int underflow exception
    @Test(expected = ArithmeticException.class)
    public void testSafeMultiplyInt_underflow_throwsException() {
        FieldUtils.safeMultiply(Integer.MIN_VALUE, 2);
    }

    // Tests safeMultiply(long, int) negative overflow exception
    @Test(expected = ArithmeticException.class)
    public void testSafeMultiplyLongInt_negativeOverflow_throwsException() {
        FieldUtils.safeMultiply(Long.MIN_VALUE, 2);
    }

    // Tests safeMultiply(long, long) negative overflow exception
    @Test(expected = ArithmeticException.class)
    public void testSafeMultiplyLongLong_negativeOverflow_throwsException() {
        FieldUtils.safeMultiply(Long.MIN_VALUE, 2L);
    }

    // Tests safeDivide(long, long) with valid values
    @Test
    public void testSafeDivideLong_validValues_returnsQuotient() {
        assertEquals(2L, FieldUtils.safeDivide(6L, 3L));
        assertEquals(-2L, FieldUtils.safeDivide(6L, -3L));
        assertEquals(0L, FieldUtils.safeDivide(0L, 5L));
        assertEquals(Long.MAX_VALUE, FieldUtils.safeDivide(Long.MAX_VALUE, 1L));
        assertEquals(Long.MIN_VALUE, FieldUtils.safeDivide(Long.MIN_VALUE, 1L));
    }

    // Tests safeDivide(long, long) overflow on Long.MIN_VALUE / -1
    @Test(expected = ArithmeticException.class)
    public void testSafeDivideLong_minValueMinusOne_throwsException() {
        FieldUtils.safeDivide(Long.MIN_VALUE, -1L);
    }

    // Tests safeDivide(long, long) division by zero
    @Test(expected = ArithmeticException.class)
    public void testSafeDivideLong_divideByZero_throwsException() {
        FieldUtils.safeDivide(10L, 0L);
    }

    // Tests safeToInt underflow exception
    @Test(expected = ArithmeticException.class)
    public void testSafeToInt_underflow_throwsException() {
        FieldUtils.safeToInt((long) Integer.MIN_VALUE - 1L);
    }

    // Tests safeMultiplyToInt underflow exception
    @Test(expected = ArithmeticException.class)
    public void testSafeMultiplyToInt_underflow_throwsException() {
        FieldUtils.safeMultiplyToInt(Long.MIN_VALUE, 2L);
    }

    // Tests verifyValueBounds with string type above upper bound
    @Test(expected = IllegalFieldValueException.class)
    public void testVerifyValueBounds_stringAboveUpperBound_throwsException() {
        FieldUtils.verifyValueBounds("testField", 11, 1, 10);
    }

    // Tests verifyValueBounds with DateTimeFieldType below lower bound
    @Test(expected = IllegalFieldValueException.class)
    public void testVerifyValueBounds_typeBelowLowerBound_throwsException() {
        FieldUtils.verifyValueBounds(DateTimeFieldType.dayOfMonth(), 0, 1, 31);
    }

    // Tests getWrappedValue 3-args version when min == max
    @Test(expected = IllegalArgumentException.class)
    public void testGetWrappedValue3Args_minEqualsMax_throwsException() {
        FieldUtils.getWrappedValue(5, 5, 5);
    }

    // Tests getWrappedValue 4-args version when min >= max
    @Test(expected = IllegalArgumentException.class)
    public void testGetWrappedValue4Args_minGreaterEqualMax_throwsException() {
        FieldUtils.getWrappedValue(1, 5, 10, 5);
    }

    // Tests getWrappedValue with large wrap values
    @Test
    public void testGetWrappedValue_largeWrapValues_returnsWrapped() {
        assertEquals(1, FieldUtils.getWrappedValue(1, 20, 1, 10));
        assertEquals(2, FieldUtils.getWrappedValue(1, -19, 1, 10));
    }
}