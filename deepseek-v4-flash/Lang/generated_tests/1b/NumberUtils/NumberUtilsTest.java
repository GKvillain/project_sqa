package org.apache.commons.lang3.math;

import static org.junit.Assert.*;

import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * JUnit 4 test class for NumberUtils (Defects4J Bug 1b).
 */
public class NumberUtilsTest {

    // Tests toInt with valid input
    @Test
    public void testToInt_validInput_returnsCorrectValue() {
        assertEquals(123, NumberUtils.toInt("123"));
    }

    // Tests toInt with null input
    @Test
    public void testToInt_nullInput_returnsZero() {
        assertEquals(0, NumberUtils.toInt(null));
    }

    // Tests toInt with empty string
    @Test
    public void testToInt_emptyString_returnsZero() {
        assertEquals(0, NumberUtils.toInt(""));
    }

    // Tests toLong with valid input
    @Test
    public void testToLong_validInput_returnsCorrectValue() {
        assertEquals(456L, NumberUtils.toLong("456"));
    }

    // Tests toLong with null input
    @Test
    public void testToLong_nullInput_returnsZero() {
        assertEquals(0L, NumberUtils.toLong(null));
    }

    // Tests toFloat with valid input
    @Test
    public void testToFloat_validInput_returnsCorrectValue() {
        assertEquals(1.5f, NumberUtils.toFloat("1.5"), 0.0001f);
    }

    // Tests toDouble with valid input
    @Test
    public void testToDouble_validInput_returnsCorrectValue() {
        assertEquals(2.5, NumberUtils.toDouble("2.5"), 0.0001);
    }

    // Tests createNumber with null input
    @Test
    public void testCreateNumber_nullInput_returnsNull() {
        assertNull(NumberUtils.createNumber(null));
    }

    // Tests createNumber with blank string throws exception
    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_blankInput_throwsException() {
        NumberUtils.createNumber("");
    }

    // Tests createNumber with a valid integer string
    @Test
    public void testCreateNumber_validInteger_returnsInteger() {
        assertTrue(NumberUtils.createNumber("123") instanceof Integer);
        assertEquals(123, NumberUtils.createNumber("123"));
    }

    // Tests createNumber with a valid long string
    @Test
    public void testCreateNumber_validLong_returnsLong() {
        assertTrue(NumberUtils.createNumber("123456789012") instanceof Long);
        assertEquals(123456789012L, NumberUtils.createNumber("123456789012"));
    }

    // Tests createNumber with a valid float string
    @Test
    public void testCreateNumber_validFloat_returnsFloat() {
        assertTrue(NumberUtils.createNumber("1.5f") instanceof Float);
        assertEquals(1.5f, NumberUtils.createNumber("1.5f"));
    }

    // Tests createNumber with a valid double string
    @Test
    public void testCreateNumber_validDouble_returnsDouble() {
        assertTrue(NumberUtils.createNumber("2.5") instanceof Double);
        assertEquals(2.5, NumberUtils.createNumber("2.5"));
    }

    // Tests createNumber with a string ending with 'L'
    @Test
    public void testCreateNumber_stringEndsWithL_returnsLong() {
        assertTrue(NumberUtils.createNumber("123L") instanceof Long);
        assertEquals(123L, NumberUtils.createNumber("123L"));
    }

    // Tests createNumber with a hex string
    @Test
    public void testCreateNumber_hexString_returnsInteger() {
        assertTrue(NumberUtils.createNumber("0x10") instanceof Integer);
        assertEquals(16, NumberUtils.createNumber("0x10"));
    }

    // Tests createNumber with a negative hex string
    @Test
    public void testCreateNumber_negativeHexString_returnsInteger() {
        assertTrue(NumberUtils.createNumber("-0x10") instanceof Integer);
        assertEquals(-16, NumberUtils.createNumber("-0x10"));
    }

    // Tests isNumber with a valid integer string
    @Test
    public void testIsNumber_validInteger_returnsTrue() {
        assertTrue(NumberUtils.isNumber("123"));
    }

    // Tests isNumber with null input
    @Test
    public void testIsNumber_nullInput_returnsFalse() {
        assertFalse(NumberUtils.isNumber(null));
    }

    // Tests isNumber with empty string
    @Test
    public void testIsNumber_emptyString_returnsFalse() {
        assertFalse(NumberUtils.isNumber(""));
    }

    // Tests isNumber with a hex string
    @Test
    public void testIsNumber_hexString_returnsTrue() {
        assertTrue(NumberUtils.isNumber("0x1A"));
    }

    // Tests isNumber with an invalid string containing letters
    @Test
    public void testIsNumber_invalidString_returnsFalse() {
        assertFalse(NumberUtils.isNumber("abc"));
    }

    // Tests min with int array
    @Test
    public void testMin_intArray_returnsMinValue() {
        assertEquals(1, NumberUtils.min(new int[]{3, 1, 2}));
    }

    // Tests max with int array
    @Test
    public void testMax_intArray_returnsMaxValue() {
        assertEquals(3, NumberUtils.max(new int[]{3, 1, 2}));
    }

    // Tests isDigits with null input
    @Test
    public void testIsDigits_nullInput_returnsFalse() {
        assertFalse(NumberUtils.isDigits(null));
    }

    // Tests isDigits with valid digit string
    @Test
    public void testIsDigits_validDigits_returnsTrue() {
        assertTrue(NumberUtils.isDigits("12345"));
    }

    // Tests createBigDecimal with null input
    @Test
    public void testCreateBigDecimal_nullInput_returnsNull() {
        assertNull(NumberUtils.createBigDecimal(null));
    }

    // Tests createBigDecimal with valid string
    @Test
    public void testCreateBigDecimal_validInput_returnsBigDecimal() {
        assertEquals(new BigDecimal("10.5"), NumberUtils.createBigDecimal("10.5"));
    }

    // =============== New test cases for uncovered coverage ===============

    // createNumber with lowercase 'l' suffix
    @Test
    public void testCreateNumber_lowercaseL_returnsLong() {
        assertTrue(NumberUtils.createNumber("123l") instanceof Long);
        assertEquals(123L, NumberUtils.createNumber("123l"));
    }

    // createNumber with uppercase 'F' suffix
    @Test
    public void testCreateNumber_uppercaseF_returnsFloat() {
        assertTrue(NumberUtils.createNumber("1.5F") instanceof Float);
        assertEquals(1.5f, NumberUtils.createNumber("1.5F"));
    }

    // createNumber with lowercase 'd' suffix
    @Test
    public void testCreateNumber_lowercaseD_returnsDouble() {
        assertTrue(NumberUtils.createNumber("2.5d") instanceof Double);
        assertEquals(2.5, NumberUtils.createNumber("2.5d"), 0.0001);
    }

    // createNumber with uppercase 'D' suffix
    @Test
    public void testCreateNumber_uppercaseD_returnsDouble() {
        assertTrue(NumberUtils.createNumber("2.5D") instanceof Double);
        assertEquals(2.5, NumberUtils.createNumber("2.5D"), 0.0001);
    }

    // createNumber with scientific notation (no suffix)
    @Test
    public void testCreateNumber_scientificNotation_returnsDouble() {
        assertTrue(NumberUtils.createNumber("1e10") instanceof Double);
        assertEquals(1e10, NumberUtils.createNumber("1e10"), 0.0001);
    }

    // createNumber with negative scientific notation
    @Test
    public void testCreateNumber_negativeScientificNotation_returnsDouble() {
        assertTrue(NumberUtils.createNumber("-1.5e-3") instanceof Double);
        assertEquals(-1.5e-3, NumberUtils.createNumber("-1.5e-3"), 1e-10);
    }

    // createNumber with leading '+'
    @Test
    public void testCreateNumber_leadingPlus_returnsInteger() {
        assertTrue(NumberUtils.createNumber("+123") instanceof Integer);
        assertEquals(123, NumberUtils.createNumber("+123"));
    }

    // createNumber with leading zeros
    @Test
    public void testCreateNumber_leadingZeros_returnsInteger() {
        assertTrue(NumberUtils.createNumber("00123") instanceof Integer);
        assertEquals(123, NumberUtils.createNumber("00123"));
    }

    // createNumber with "0"
    @Test
    public void testCreateNumber_zero_returnsInteger() {
        assertTrue(NumberUtils.createNumber("0") instanceof Integer);
        assertEquals(0, NumberUtils.createNumber("0"));
    }

    // createNumber with hex using uppercase 'X'
    @Test
    public void testCreateNumber_hexWithUpperCaseX_returnsInteger() {
        assertTrue(NumberUtils.createNumber("0X1A") instanceof Integer);
        assertEquals(26, NumberUtils.createNumber("0X1A"));
    }

    // createNumber with hex and long suffix
    @Test
    public void testCreateNumber_hexWithSuffixL_returnsLong() {
        assertTrue(NumberUtils.createNumber("0x10L") instanceof Long);
        assertEquals(16L, NumberUtils.createNumber("0x10L"));
    }

    // createNumber with leading decimal point
    @Test
    public void testCreateNumber_leadingDot_returnsDouble() {
        assertTrue(NumberUtils.createNumber(".5") instanceof Double);
        assertEquals(0.5, NumberUtils.createNumber(".5"), 0.0001);
    }

    // createNumber with invalid suffix throws exception
    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_endingWithInvalidSuffix_throwsException() {
        NumberUtils.createNumber("123g");
    }

    // createNumber with decimal and 'f' suffix
    @Test
    public void testCreateNumber_withDecimalAndSuffixF_returnsFloat() {
        assertTrue(NumberUtils.createNumber("1.5f") instanceof Float);
        assertEquals(1.5f, NumberUtils.createNumber("1.5f"));
    }

    // createNumber with decimal and 'd' suffix
    @Test
    public void testCreateNumber_withDecimalAndSuffixD_returnsDouble() {
        assertTrue(NumberUtils.createNumber("1.5d") instanceof Double);
        assertEquals(1.5, NumberUtils.createNumber("1.5d"), 0.0001);
    }

    // createNumber with exponent and 'd' suffix
    @Test
    public void testCreateNumber_withExponentAndSuffixD_returnsDouble() {
        assertTrue(NumberUtils.createNumber("1e5d") instanceof Double);
        assertEquals(1e5, NumberUtils.createNumber("1e5d"), 0.001);
    }

    // isNumber with scientific notation
    @Test
    public void testIsNumber_scientificNotation_returnsTrue() {
        assertTrue(NumberUtils.isNumber("1e10"));
    }

    // isNumber with leading zeros
    @Test
    public void testIsNumber_leadingZeros_returnsTrue() {
        assertTrue(NumberUtils.isNumber("00123"));
    }

    // isNumber with negative hex string
    @Test
    public void testIsNumber_negativeHexString_returnsTrue() {
        assertTrue(NumberUtils.isNumber("-0x1A"));
    }

    // isNumber with lowercase 'l' suffix
    @Test
    public void testIsNumber_lowercaseLSuffix_returnsTrue() {
        assertTrue(NumberUtils.isNumber("123l"));
    }

    // isNumber with invalid suffix returns false
    @Test
    public void testIsNumber_invalidSuffix_returnsFalse() {
        assertFalse(NumberUtils.isNumber("123g"));
    }

    // isNumber with leading '+'
    @Test
    public void testIsNumber_leadingPlus_returnsTrue() {
        assertTrue(NumberUtils.isNumber("+123"));
    }

    // isNumber with leading decimal point
    @Test
    public void testIsNumber_leadingDot_returnsTrue() {
        assertTrue(NumberUtils.isNumber(".5"));
    }

    // isNumber with trailing decimal point
    @Test
    public void testIsNumber_trailingDot_returnsTrue() {
        assertTrue(NumberUtils.isNumber("5."));
    }

    // isNumber with just "0x" prefix (no digits) returns false
    @Test
    public void testIsNumber_incompleteHex_returnsFalse() {
        assertFalse(NumberUtils.isNumber("0x"));
    }

    // toInt with invalid string returns zero
    @Test
    public void testToInt_invalidString_returnsZero() {
        assertEquals(0, NumberUtils.toInt("abc"));
    }

    // toLong with invalid string returns zero
    @Test
    public void testToLong_invalidString_returnsZero() {
        assertEquals(0L, NumberUtils.toLong("abc"));
    }

    // toFloat with invalid string returns zero
    @Test
    public void testToFloat_invalidString_returnsZero() {
        assertEquals(0.0f, NumberUtils.toFloat("abc"), 0.0001f);
    }

    // toDouble with invalid string returns zero
    @Test
    public void testToDouble_invalidString_returnsZero() {
        assertEquals(0.0, NumberUtils.toDouble("abc"), 0.0001);
    }

    // isDigits with negative sign returns false
    @Test
    public void testIsDigits_negative_returnsFalse() {
        assertFalse(NumberUtils.isDigits("-123"));
    }

    // isDigits with decimal point returns false
    @Test
    public void testIsDigits_decimal_returnsFalse() {
        assertFalse(NumberUtils.isDigits("12.3"));
    }

    // min with long array
    @Test
    public void testMin_longArray_returnsMinValue() {
        assertEquals(2L, NumberUtils.min(new long[]{5L, 2L, 8L}));
    }

    // max with long array
    @Test
    public void testMax_longArray_returnsMaxValue() {
        assertEquals(8L, NumberUtils.max(new long[]{5L, 2L, 8L}));
    }

    // min with float array
    @Test
    public void testMin_floatArray_returnsMinValue() {
        assertEquals(1.5f, NumberUtils.min(new float[]{3.0f, 1.5f, 2.0f}), 0.0001f);
    }

    // max with float array
    @Test
    public void testMax_floatArray_returnsMaxValue() {
        assertEquals(3.0f, NumberUtils.max(new float[]{3.0f, 1.5f, 2.0f}), 0.0001f);
    }

    // min with double array
    @Test
    public void testMin_doubleArray_returnsMinValue() {
        assertEquals(1.5, NumberUtils.min(new double[]{3.0, 1.5, 2.0}), 0.0001);
    }

    // max with double array
    @Test
    public void testMax_doubleArray_returnsMaxValue() {
        assertEquals(3.0, NumberUtils.max(new double[]{3.0, 1.5, 2.0}), 0.0001);
    }

    // min with empty array throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testMin_emptyArray_throwsException() {
        NumberUtils.min(new int[0]);
    }

    // max with empty array throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testMax_emptyArray_throwsException() {
        NumberUtils.max(new int[0]);
    }

    // min with null array throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testMin_nullArray_throwsException() {
        NumberUtils.min((int[]) null);
    }

    // max with null array throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testMax_nullArray_throwsException() {
        NumberUtils.max((int[]) null);
    }

    // createBigInteger with null
    @Test
    public void testCreateBigInteger_nullInput_returnsNull() {
        assertNull(NumberUtils.createBigInteger(null));
    }

    // createBigInteger with valid string
    @Test
    public void testCreateBigInteger_validInput_returnsBigInteger() {
        assertEquals(new BigInteger("12345678901234567890"), NumberUtils.createBigInteger("12345678901234567890"));
    }

    // createInteger with null
    @Test
    public void testCreateInteger_nullInput_returnsNull() {
        assertNull(NumberUtils.createInteger(null));
    }

    // createInteger with valid string
    @Test
    public void testCreateInteger_validInput_returnsInteger() {
        assertEquals(123, NumberUtils.createInteger("123").intValue());
        assertTrue(NumberUtils.createInteger("123") instanceof Integer);
    }

    // createLong with null
    @Test
    public void testCreateLong_nullInput_returnsNull() {
        assertNull(NumberUtils.createLong(null));
    }

    // createLong with valid string
    @Test
    public void testCreateLong_validInput_returnsLong() {
        assertEquals(123456789012L, NumberUtils.createLong("123456789012").longValue());
        assertTrue(NumberUtils.createLong("123456789012") instanceof Long);
    }

    // createFloat with null
    @Test
    public void testCreateFloat_nullInput_returnsNull() {
        assertNull(NumberUtils.createFloat(null));
    }

    // createFloat with valid string
    @Test
    public void testCreateFloat_validInput_returnsFloat() {
        assertEquals(1.5f, NumberUtils.createFloat("1.5f"), 0.0001f);
        assertTrue(NumberUtils.createFloat("1.5f") instanceof Float);
    }

    // createDouble with null
    @Test
    public void testCreateDouble_nullInput_returnsNull() {
        assertNull(NumberUtils.createDouble(null));
    }

    // createDouble with valid string
    @Test
    public void testCreateDouble_validInput_returnsDouble() {
        assertEquals(2.5, NumberUtils.createDouble("2.5"), 0.0001);
        assertTrue(NumberUtils.createDouble("2.5") instanceof Double);
    }
}