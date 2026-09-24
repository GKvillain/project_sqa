package org.joda.time.field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.joda.time.DateTimeField;
import org.joda.time.DateTimeFieldType;
import org.joda.time.IllegalFieldValueException;
import org.junit.Test;

public class FieldUtilsTest {

    // Tests safeNegate with normal value
    @Test
    public void testSafeNegate_normalValue_returnsNegated() {
        assertEquals(-5, FieldUtils.safeNegate(5));
        assertEquals(5, FieldUtils.safeNegate(-5));
        assertEquals(0, FieldUtils.safeNegate(0));
    }

    // Tests safeNegate with Integer.MIN_VALUE throwing ArithmeticException
    @Test(expected = ArithmeticException.class)
    public void testSafeNegate_minIntValue_throwsArithmeticException() {
        FieldUtils.safeNegate(Integer.MIN_VALUE);
    }

    // Tests safeAdd(int, int) normal addition
    @Test
    public void testSafeAddInt_normalValues_returnsSum() {
        assertEquals(5, FieldUtils.safeAdd(2, 3));
        assertEquals(-1, FieldUtils.safeAdd(2, -3));
    }

    // Tests safeAdd(int, int) positive overflow
    @Test(expected = ArithmeticException.class)
    public void testSafeAddInt_overflow_throwsArithmeticException() {
        FieldUtils.safeAdd(Integer.MAX_VALUE, 1);
    }

    // Tests safeAdd(long, long) normal addition
    @Test
    public void testSafeAddLong_normalValues_returnsSum() {
        assertEquals(5L, FieldUtils.safeAdd(2L, 3L));
        assertEquals(-1L, FieldUtils.safeAdd(2L, -3L));
    }

    // Tests safeAdd(long, long) positive overflow
    @Test(expected = ArithmeticException.class)
    public void testSafeAddLong_overflow_throwsArithmeticException() {
        FieldUtils.safeAdd(Long.MAX_VALUE, 1L);
    }

    // Tests safeSubtract(long, long) normal subtraction
    @Test
    public void testSafeSubtract_normalValues_returnsDifference() {
        assertEquals(2L, FieldUtils.safeSubtract(5L, 3L));
        assertEquals(8L, FieldUtils.safeSubtract(5L, -3L));
    }

    // Tests safeSubtract(long, long) overflow
    @Test(expected = ArithmeticException.class)
    public void testSafeSubtract_underflow_throwsArithmeticException() {
        FieldUtils.safeSubtract(Long.MIN_VALUE, 1L);
    }

    // Tests safeMultiply(int, int) normal multiplication
    @Test
    public void testSafeMultiplyIntInt_normalValues_returnsProduct() {
        assertEquals(6, FieldUtils.safeMultiply(2, 3));
        assertEquals(-6, FieldUtils.safeMultiply(2, -3));
        assertEquals(0, FieldUtils.safeMultiply(0, 3));
    }

    // Tests safeMultiply(int, int) overflow
    @Test(expected = ArithmeticException.class)
    public void testSafeMultiplyIntInt_overflow_throwsArithmeticException() {
        FieldUtils.safeMultiply(Integer.MAX_VALUE, 2);
    }

    // Tests safeMultiply(long, int) normal values including switch cases (-1, 0, 1)
    @Test
    public void testSafeMultiplyLongInt_specialMultiplierValues_returnsProduct() {
        assertEquals(0L, FieldUtils.safeMultiply(100L, 0));
        assertEquals(100L, FieldUtils.safeMultiply(100L, 1));
        assertEquals(-100L, FieldUtils.safeMultiply(100L, -1));
        assertEquals(300L, FieldUtils.safeMultiply(100L, 3));
    }

    // Tests safeMultiply(long, int) with Long.MIN_VALUE and -1 (Defects4J Time-15 defect test)
    @Test(expected = ArithmeticException.class)
    public void testSafeMultiplyLongInt_minLongValueAndMinusOne_throwsArithmeticException() {
        FieldUtils.safeMultiply(Long.MIN_VALUE, -1);
    }

    // Tests safeMultiply(long, int) overflow with general multiplier
    @Test(expected = ArithmeticException.class)
    public void testSafeMultiplyLongInt_overflow_throwsArithmeticException() {
        FieldUtils.safeMultiply(Long.MAX_VALUE, 2);
    }

    // Tests safeMultiply(long, long) normal values and edge cases
    @Test
    public void testSafeMultiplyLongLong_normalValues_returnsProduct() {
        assertEquals(0L, FieldUtils.safeMultiply(0L, 5L));
        assertEquals(0L, FieldUtils.safeMultiply(5L, 0L));
        assertEquals(5L, FieldUtils.safeMultiply(1L, 5L));
        assertEquals(5L, FieldUtils.safeMultiply(5L, 1L));
        assertEquals(15L, FieldUtils.safeMultiply(3L, 5L));
    }

    // Tests safeMultiply(long, long) overflow with Long.MIN_VALUE and -1
    @Test(expected = ArithmeticException.class)
    public void testSafeMultiplyLongLong_minLongValueAndMinusOne_throwsArithmeticException() {
        FieldUtils.safeMultiply(Long.MIN_VALUE, -1L);
    }

    // Tests safeToInt(long) normal and overflow
    @Test
    public void testSafeToInt_withinRange_returnsInt() {
        assertEquals(12345, FieldUtils.safeToInt(12345L));
        assertEquals(Integer.MAX_VALUE, FieldUtils.safeToInt((long) Integer.MAX_VALUE));
        assertEquals(Integer.MIN_VALUE, FieldUtils.safeToInt((long) Integer.MIN_VALUE));
    }

    // Tests safeToInt(long) overflow
    @Test(expected = ArithmeticException.class)
    public void testSafeToInt_outOfRange_throwsArithmeticException() {
        FieldUtils.safeToInt((long) Integer.MAX_VALUE + 1L);
    }

    // Tests safeMultiplyToInt(long, long)
    @Test
    public void testSafeMultiplyToInt_validValues_returnsInt() {
        assertEquals(20, FieldUtils.safeMultiplyToInt(4L, 5L));
    }

    // Tests verifyValueBounds(DateTimeFieldType, int, int, int) valid and invalid bounds
    @Test
    public void testVerifyValueBounds_withinBounds_doesNotThrow() {
        FieldUtils.verifyValueBounds(DateTimeFieldType.hourOfDay(), 12, 0, 23);
        FieldUtils.verifyValueBounds("hourOfDay", 12, 0, 23);
    }

    // Tests verifyValueBounds(DateTimeFieldType, int, int, int) below lower bound
    @Test(expected = IllegalFieldValueException.class)
    public void testVerifyValueBounds_belowLowerBound_throwsIllegalFieldValueException() {
        FieldUtils.verifyValueBounds(DateTimeFieldType.hourOfDay(), -1, 0, 23);
    }

    // Tests getWrappedValue(int, int, int) with positive and negative inputs
    @Test
    public void testGetWrappedValue_validRanges_returnsWrappedValue() {
        assertEquals(5, FieldUtils.getWrappedValue(5, 1, 10));
        assertEquals(1, FieldUtils.getWrappedValue(11, 1, 10));
        assertEquals(10, FieldUtils.getWrappedValue(0, 1, 10));
        assertEquals(8, FieldUtils.getWrappedValue(5, 3, 1, 10));
        assertEquals(2, FieldUtils.getWrappedValue(5, 7, 1, 10));
    }

    // Tests getWrappedValue when minValue >= maxValue
    @Test(expected = IllegalArgumentException.class)
    public void testGetWrappedValue_minGreaterThanOrEqualToMax_throwsIllegalArgumentException() {
        FieldUtils.getWrappedValue(5, 10, 5);
    }

    // Tests equals(Object, Object) null and equality handling
    @Test
    public void testEquals_variousObjects_returnsExpectedResult() {
        assertTrue(FieldUtils.equals(null, null));
        assertFalse(FieldUtils.equals("test", null));
        assertFalse(FieldUtils.equals(null, "test"));
        assertTrue(FieldUtils.equals("test", "test"));
        assertFalse(FieldUtils.equals("test1", "test2"));
    }

    // Tests safeAdd(int, int) negative overflow
    @Test(expected = ArithmeticException.class)
    public void testSafeAddInt_negativeOverflow_throwsArithmeticException() {
        FieldUtils.safeAdd(Integer.MIN_VALUE, -1);
    }

    // Tests safeAdd(long, long) negative overflow
    @Test(expected = ArithmeticException.class)
    public void testSafeAddLong_negativeOverflow_throwsArithmeticException() {
        FieldUtils.safeAdd(Long.MIN_VALUE, -1L);
    }

    // Tests safeMultiply(long, long) positive overflow
    @Test(expected = ArithmeticException.class)
    public void testSafeMultiplyLongLong_overflow_throwsArithmeticException() {
        FieldUtils.safeMultiply(Long.MAX_VALUE, 2L);
    }

    // Tests safeMultiply(long, long) negative overflow
    @Test(expected = ArithmeticException.class)
    public void testSafeMultiplyLongLong_negativeOverflow_throwsArithmeticException() {
        FieldUtils.safeMultiply(Long.MIN_VALUE, 2L);
    }

    // Tests safeToInt(long) underflow
    @Test(expected = ArithmeticException.class)
    public void testSafeToInt_underflow_throwsArithmeticException() {
        FieldUtils.safeToInt((long) Integer.MIN_VALUE - 1L);
    }

    // Tests safeMultiplyToInt(long, long) overflow
    @Test(expected = ArithmeticException.class)
    public void testSafeMultiplyToInt_overflow_throwsArithmeticException() {
        FieldUtils.safeMultiplyToInt(Integer.MAX_VALUE, 2L);
    }

    // Tests safeDivide(long, long) normal division
    @Test
    public void testSafeDivide_normalValues_returnsQuotient() {
        assertEquals(3L, FieldUtils.safeDivide(6L, 2L));
        assertEquals(-3L, FieldUtils.safeDivide(6L, -2L));
        assertEquals(0L, FieldUtils.safeDivide(0L, 5L));
    }

    // Tests safeDivide(long, long) overflow with Long.MIN_VALUE and -1
    @Test(expected = ArithmeticException.class)
    public void testSafeDivide_minLongValueAndMinusOne_throwsArithmeticException() {
        FieldUtils.safeDivide(Long.MIN_VALUE, -1L);
    }

    // Tests verifyValueBounds(DateTimeFieldType, int, int, int) above upper bound
    @Test(expected = IllegalFieldValueException.class)
    public void testVerifyValueBounds_aboveUpperBound_throwsIllegalFieldValueException() {
        FieldUtils.verifyValueBounds(DateTimeFieldType.hourOfDay(), 24, 0, 23);
    }

    // Tests verifyValueBounds(String, int, int, int) below lower bound
    @Test(expected = IllegalFieldValueException.class)
    public void testVerifyValueBoundsString_belowLowerBound_throwsIllegalFieldValueException() {
        FieldUtils.verifyValueBounds("hourOfDay", -1, 0, 23);
    }

    // Tests verifyValueBounds(String, int, int, int) above upper bound
    @Test(expected = IllegalFieldValueException.class)
    public void testVerifyValueBoundsString_aboveUpperBound_throwsIllegalFieldValueException() {
        FieldUtils.verifyValueBounds("hourOfDay", 24, 0, 23);
    }

    // Tests verifyValueBounds(DateTimeField, int, int, int) valid bounds
    @Test
    public void testVerifyValueBoundsDateTimeField_withinBounds_doesNotThrow() {
        DateTimeField field = org.joda.time.chrono.ISOChronology.getInstanceUTC().hourOfDay();
        FieldUtils.verifyValueBounds(field, 12, 0, 23);
    }

    // Tests verifyValueBounds(DateTimeField, int, int, int) below lower bound
    @Test(expected = IllegalFieldValueException.class)
    public void testVerifyValueBoundsDateTimeField_belowLowerBound_throwsIllegalFieldValueException() {
        DateTimeField field = org.joda.time.chrono.ISOChronology.getInstanceUTC().hourOfDay();
        FieldUtils.verifyValueBounds(field, -1, 0, 23);
    }

    // Tests verifyValueBounds(DateTimeField, int, int, int) above upper bound
    @Test(expected = IllegalFieldValueException.class)
    public void testVerifyValueBoundsDateTimeField_aboveUpperBound_throwsIllegalFieldValueException() {
        DateTimeField field = org.joda.time.chrono.ISOChronology.getInstanceUTC().hourOfDay();
        FieldUtils.verifyValueBounds(field, 24, 0, 23);
    }

    // Tests getWrappedValue(int, int, int, int) with invalid min >= max bounds
    @Test(expected = IllegalArgumentException.class)
    public void testGetWrappedValueWithWrapValue_minGreaterThanOrEqualToMax_throwsIllegalArgumentException() {
        FieldUtils.getWrappedValue(5, 1, 10, 5);
    }
}