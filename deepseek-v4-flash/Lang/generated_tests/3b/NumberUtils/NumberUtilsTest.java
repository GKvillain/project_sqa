package org.apache.commons.lang3.math;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Test;

public class NumberUtilsTest {

    // Tests toInt with null input returns default value 0
    @Test
    public void testToInt_nullInput_returnsDefaultZero() {
        assertEquals(0, NumberUtils.toInt(null));
    }

    // Tests toInt with empty string returns default value 0
    @Test
    public void testToInt_emptyString_returnsDefaultZero() {
        assertEquals(0, NumberUtils.toInt(""));
    }

    // Tests toInt with valid positive string returns correct int
    @Test
    public void testToInt_validString_returnsCorrectValue() {
        assertEquals(123, NumberUtils.toInt("123"));
    }

    // Tests toLong with null input returns default value
    @Test
    public void testToLong_nullInput_returnsDefaultZero() {
        assertEquals(0L, NumberUtils.toLong(null));
    }

    // Tests toFloat with null input returns default value
    @Test
    public void testToFloat_nullInput_returnsDefaultZero() {
        assertEquals(0.0f, NumberUtils.toFloat(null), 0.0f);
    }

    // Tests toDouble with null input returns default value
    @Test
    public void testToDouble_nullInput_returnsDefaultZero() {
        assertEquals(0.0d, NumberUtils.toDouble(null), 0.0d);
    }

    // Tests createNumber with null input returns null
    @Test
    public void testCreateNumber_nullInput_returnsNull() {
        assertNull(NumberUtils.createNumber(null));
    }

    // Tests createNumber with blank string throws NumberFormatException
    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_blankInput_throwsNumberFormatException() {
        NumberUtils.createNumber("   ");
    }

    // Tests createNumber with hex prefix "0x" only (no digits) throws NumberFormatException
    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_hexPrefixOnly_throwsNumberFormatException() {
        NumberUtils.createNumber("0x");
    }

    // Tests createNumber with hex number that overflows int (8 hex digits, value > Integer.MAX_VALUE) should return Long
    @Test
    public void testCreateNumber_hexOverflowsInt_returnsLong() {
        // 0xFFFFFFFF = 4294967295L, fits in long but exceeds int range
        Long result = (Long) NumberUtils.createNumber("0xFFFFFFFF");
        assertEquals(4294967295L, result.longValue());
    }

    // Tests createNumber with hex number that fits in int returns Integer
    @Test
    public void testCreateNumber_hexFitsInt_returnsInteger() {
        Integer result = (Integer) NumberUtils.createNumber("0x7FFFFFFF");
        assertEquals(2147483647, result.intValue());
    }

    // Tests createNumber with hex number longer than 16 digits returns BigInteger
    @Test
    public void testCreateNumber_hexTooManyDigits_returnsBigInteger() {
        java.math.BigInteger result = (java.math.BigInteger) NumberUtils.createNumber("0x10000000000000001");
        assertEquals(new java.math.BigInteger("18446744073709551617"), result);
    }

    // Tests createNumber with octal lead zero returns Integer
    @Test
    public void testCreateNumber_octalNumber_returnsInteger() {
        // "077" = 63 decimal
        Integer result = (Integer) NumberUtils.createNumber("077");
        assertEquals(63, result.intValue());
    }

    // Tests createNumber with decimal number and exponent returns Double
    @Test
    public void testCreateNumber_decimalWithExponent_returnsDouble() {
        Double result = (Double) NumberUtils.createNumber("1.5e2");
        assertEquals(150.0, result.doubleValue(), 0.0);
    }

    // Tests createNumber with float qualifier 'f' returns Float
    @Test
    public void testCreateNumber_floatQualifier_returnsFloat() {
        Float result = (Float) NumberUtils.createNumber("3.14f");
        assertEquals(3.14f, result.floatValue(), 0.001f);
    }

    // Tests createNumber with long qualifier 'L' returns Long
    @Test
    public void testCreateNumber_longQualifier_returnsLong() {
        Long result = (Long) NumberUtils.createNumber("1234567890123456789L");
        assertEquals(1234567890123456789L, result.longValue());
    }

    // Tests createNumber with invalid string (trailing dot) throws NumberFormatException
    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_trailingDot_throwsNumberFormatException() {
        NumberUtils.createNumber("123.");
    }

    // Tests isNumber with valid decimal returns true
    @Test
    public void testIsNumber_validDecimal_returnsTrue() {
        assertTrue(NumberUtils.isNumber("3.14"));
    }

    // Tests isNumber with hex prefix "0x" only returns false
    @Test
    public void testIsNumber_hexPrefixOnly_returnsFalse() {
        assertFalse(NumberUtils.isNumber("0x"));
    }

    // Tests isNumber with valid hex string returns true
    @Test
    public void testIsNumber_validHex_returnsTrue() {
        assertTrue(NumberUtils.isNumber("0x1A2B"));
    }

    // Tests isNumber with trailing 'L' qualifier returns true for integer
    @Test
    public void testIsNumber_longQualifierInt_returnsTrue() {
        assertTrue(NumberUtils.isNumber("123L"));
    }

    // Tests isNumber with trailing 'L' and decimal point returns false
    @Test
    public void testIsNumber_longQualifierWithDecimal_returnsFalse() {
        assertFalse(NumberUtils.isNumber("1.23L"));
    }

    // Tests isNumber with exponential and no digit after 'E' returns false
    @Test
    public void testIsNumber_exponentialNoDigit_returnsFalse() {
        assertFalse(NumberUtils.isNumber("1.0E"));
    }

    // Tests isNumber with string containing only '.' returns false
    @Test
    public void testIsNumber_singleDot_returnsFalse() {
        assertFalse(NumberUtils.isNumber("."));
    }

    // Tests isDigits with null returns false
    @Test
    public void testIsDigits_null_returnsFalse() {
        assertFalse(NumberUtils.isDigits(null));
    }

    // Tests isDigits with valid digits returns true
    @Test
    public void testIsDigits_validDigits_returnsTrue() {
        assertTrue(NumberUtils.isDigits("12345"));
    }

    // Tests min with int array returns minimum value
    @Test
    public void testMin_intArray_returnsMinimum() {
        assertEquals(3, NumberUtils.min(new int[]{5, 3, 8}));
    }

    // Tests max with double array containing NaN returns NaN
    @Test
    public void testMax_doubleArrayWithNaN_returnsNaN() {
        assertTrue(Double.isNaN(NumberUtils.max(new double[]{1.0, Double.NaN, 3.0})));
    }

    // Tests min with three ints returns minimum
    @Test
    public void testMin_threeInts_returnsMinimum() {
        assertEquals(-5, NumberUtils.min(0, -5, 10));
    }

    // Tests max with three floats containing NaN returns NaN
    @Test
    public void testMax_threeFloatsWithNaN_returnsNaN() {
        assertTrue(Float.isNaN(NumberUtils.max(1.0f, Float.NaN, 3.0f)));
    }

    // ===== Additional tests for uncovered coverage =====

    @Test
    public void testToInt_nullInputWithDefault_returnsDefault() {
        assertEquals(42, NumberUtils.toInt(null, 42));
    }

    @Test
    public void testToInt_emptyStringWithDefault_returnsDefault() {
        assertEquals(7, NumberUtils.toInt("", 7));
    }

    @Test
    public void testToInt_invalidString_returnsDefaultZero() {
        assertEquals(0, NumberUtils.toInt("abc"));
        assertEquals(9, NumberUtils.toInt("abc", 9));
    }

    @Test
    public void testToInt_validStringWithDefault_returnsParsedValue() {
        assertEquals(10, NumberUtils.toInt("10", 1));
    }

    @Test
    public void testToLong_emptyString_returnsDefaultZero() {
        assertEquals(0L, NumberUtils.toLong(""));
    }

    @Test
    public void testToLong_invalidStringWithDefault_returnsDefault() {
        assertEquals(99L, NumberUtils.toLong("not-a-number", 99L));
    }

    @Test
    public void testToLong_validString_returnsValue() {
        assertEquals(42L, NumberUtils.toLong("42"));
    }

    @Test
    public void testToFloat_invalidString_returnsDefaultZero() {
        assertEquals(0.0f, NumberUtils.toFloat("abc"), 0.0f);
    }

    @Test
    public void testToFloat_validString_returnsValue() {
        assertEquals(2.5f, NumberUtils.toFloat("2.5"), 0.0f);
    }

    @Test
    public void testToFloat_invalidStringWithDefault_returnsDefault() {
        assertEquals(3.5f, NumberUtils.toFloat("abc", 3.5f), 0.0f);
    }

    @Test
    public void testToDouble_invalidString_returnsDefaultZero() {
        assertEquals(0.0d, NumberUtils.toDouble("abc"), 0.0d);
    }

    @Test
    public void testToDouble_validString_returnsValue() {
        assertEquals(2.5d, NumberUtils.toDouble("2.5"), 0.0d);
    }

    @Test
    public void testToDouble_invalidStringWithDefault_returnsDefault() {
        assertEquals(4.5d, NumberUtils.toDouble("abc", 4.5d), 0.0d);
    }

    @Test
    public void testToByte_nullInput_returnsDefaultZero() {
        assertEquals(0, NumberUtils.toByte(null));
    }

    @Test
    public void testToByte_invalidInput_returnsDefault() {
        assertEquals(0, NumberUtils.toByte("not-a-byte"));
        assertEquals(7, NumberUtils.toByte("not-a-byte", (byte) 7));
    }

    @Test
    public void testToByte_validInput_returnsValue() {
        assertEquals(127, NumberUtils.toByte("127"));
    }

    @Test
    public void testToShort_nullInput_returnsDefaultZero() {
        assertEquals(0, NumberUtils.toShort(null));
    }

    @Test
    public void testToShort_invalidInputWithDefault_returnsDefault() {
        assertEquals(3, NumberUtils.toShort("abc", (short) 3));
    }

    @Test
    public void testToShort_validInput_returnsValue() {
        assertEquals(32767, NumberUtils.toShort("32767"));
    }

    @Test
    public void testCreateNumber_integerWithoutSuffix_returnsInteger() {
        Integer result = (Integer) NumberUtils.createNumber("123");
        assertEquals(123, result.intValue());
    }

    @Test
    public void testCreateNumber_longWithoutSuffix_returnsLong() {
        Long result = (Long) NumberUtils.createNumber("123456789012");
        assertEquals(123456789012L, result.longValue());
    }

    @Test
    public void testCreateNumber_bigIntegerWithoutSuffix_returnsBigInteger() {
        BigInteger result = (BigInteger) NumberUtils.createNumber("123456789012345678901234567890");
        assertEquals(new BigInteger("123456789012345678901234567890"), result);
    }

    @Test
    public void testCreateNumber_decimalWithoutExponent_returnsDouble() {
        Double result = (Double) NumberUtils.createNumber("1.5");
        assertEquals(1.5d, result.doubleValue(), 0.0d);
    }

    @Test
    public void testCreateNumber_exponentWithoutDecimal_returnsDouble() {
        Double result = (Double) NumberUtils.createNumber("1e2");
        assertEquals(100.0d, result.doubleValue(), 0.0d);
    }

    @Test
    public void testCreateNumber_uppercaseDoubleQualifier_returnsDouble() {
        Double result = (Double) NumberUtils.createNumber("1.5D");
        assertEquals(1.5d, result.doubleValue(), 0.0d);
    }

    @Test
    public void testCreateNumber_uppercaseFloatQualifier_returnsFloat() {
        Float result = (Float) NumberUtils.createNumber("1.5F");
        assertEquals(1.5f, result.floatValue(), 0.001f);
    }

    @Test
    public void testCreateNumber_lowercaseLongQualifier_returnsLong() {
        Long result = (Long) NumberUtils.createNumber("123l");
        assertEquals(123L, result.longValue());
    }

    @Test
    public void testCreateNumber_negativeHex_returnsInteger() {
        Integer result = (Integer) NumberUtils.createNumber("-0x10");
        assertEquals(-16, result.intValue());
    }

    @Test
    public void testCreateNumber_uppercaseHexPrefix_returnsInteger() {
        Integer result = (Integer) NumberUtils.createNumber("0X1A");
        assertEquals(26, result.intValue());
    }

    @Test
    public void testCreateNumber_doubleDashPrefix_returnsNull() {
        assertNull(NumberUtils.createNumber("--1"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_decimalWithLongQualifier_throwsNumberFormatException() {
        NumberUtils.createNumber("1.2L");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_exponentWithoutDigits_throwsNumberFormatException() {
        NumberUtils.createNumber("1.2e");
    }

    @Test
    public void testCreateFloat_null_returnsNull() {
        assertNull(NumberUtils.createFloat(null));
    }

    @Test
    public void testCreateFloat_validString_returnsFloat() {
        Float result = NumberUtils.createFloat("1.5");
        assertEquals(1.5f, result.floatValue(), 0.001f);
    }

    @Test
    public void testCreateDouble_null_returnsNull() {
        assertNull(NumberUtils.createDouble(null));
    }

    @Test
    public void testCreateDouble_validString_returnsDouble() {
        Double result = NumberUtils.createDouble("1.5");
        assertEquals(1.5d, result.doubleValue(), 0.0d);
    }

    @Test
    public void testCreateInteger_null_returnsNull() {
        assertNull(NumberUtils.createInteger(null));
    }

    @Test
    public void testCreateInteger_validString_returnsInteger() {
        Integer result = NumberUtils.createInteger("42");
        assertEquals(42, result.intValue());
    }

    @Test
    public void testCreateLong_null_returnsNull() {
        assertNull(NumberUtils.createLong(null));
    }

    @Test
    public void testCreateLong_validString_returnsLong() {
        Long result = NumberUtils.createLong("42");
        assertEquals(42L, result.longValue());
    }

    @Test
    public void testCreateBigInteger_null_returnsNull() {
        assertNull(NumberUtils.createBigInteger(null));
    }

    @Test
    public void testCreateBigInteger_validString_returnsBigInteger() {
        BigInteger result = NumberUtils.createBigInteger("123456789012345678901234567890");
        assertEquals(new BigInteger("123456789012345678901234567890"), result);
    }

    @Test
    public void testCreateBigDecimal_null_returnsNull() {
        assertNull(NumberUtils.createBigDecimal(null));
    }

    @Test
    public void testCreateBigDecimal_validString_returnsBigDecimal() {
        BigDecimal result = NumberUtils.createBigDecimal("1.5");
        assertEquals(0, new BigDecimal("1.5").compareTo(result));
    }

    @Test
    public void testIsNumber_nullInput_returnsFalse() {
        assertFalse(NumberUtils.isNumber(null));
    }

    @Test
    public void testIsNumber_emptyInput_returnsFalse() {
        assertFalse(NumberUtils.isNumber(""));
    }

    @Test
    public void testIsNumber_negativeDecimal_returnsTrue() {
        assertTrue(NumberUtils.isNumber("-3.14"));
    }

    @Test
    public void testIsNumber_negativeHex_returnsTrue() {
        assertTrue(NumberUtils.isNumber("-0x1A"));
    }

    @Test
    public void testIsNumber_uppercaseHexPrefix_returnsTrue() {
        assertTrue(NumberUtils.isNumber("0X1A"));
    }

    @Test
    public void testIsNumber_invalidHexDigit_returnsFalse() {
        assertFalse(NumberUtils.isNumber("0x1G"));
    }

    @Test
    public void testIsNumber_validExponent_returnsTrue() {
        assertTrue(NumberUtils.isNumber("1.2e3"));
        assertTrue(NumberUtils.isNumber("1E-3"));
        assertTrue(NumberUtils.isNumber("1e+3"));
    }

    @Test
    public void testIsNumber_floatQualifier_returnsTrue() {
        assertTrue(NumberUtils.isNumber("1.5f"));
        assertTrue(NumberUtils.isNumber("1.5F"));
    }

    @Test
    public void testIsNumber_doubleQualifier_returnsTrue() {
        assertTrue(NumberUtils.isNumber("1.5d"));
        assertTrue(NumberUtils.isNumber("1.5D"));
    }

    @Test
    public void testIsNumber_lowercaseLongQualifier_returnsTrue() {
        assertTrue(NumberUtils.isNumber("123l"));
    }

    @Test
    public void testIsNumber_multipleDecimalPoints_returnsFalse() {
        assertFalse(NumberUtils.isNumber("1.2.3"));
    }

    @Test
    public void testIsNumber_hexWithDecimalPoint_returnsFalse() {
        assertFalse(NumberUtils.isNumber("0x1.2"));
    }

    @Test
    public void testIsDigits_emptyInput_returnsFalse() {
        assertFalse(NumberUtils.isDigits(""));
    }

    @Test
    public void testIsDigits_nonDigitInput_returnsFalse() {
        assertFalse(NumberUtils.isDigits("12a34"));
        assertFalse(NumberUtils.isDigits("12-34"));
    }

    @Test
    public void testIsDigits_singleDigit_returnsTrue() {
        assertTrue(NumberUtils.isDigits("7"));
    }

    @Test
    public void testMax_intArray_returnsMaximum() {
        assertEquals(5, NumberUtils.max(new int[]{5, -10, 3}));
    }

    @Test
    public void testMin_longArray_returnsMinimum() {
        assertEquals(-10L, NumberUtils.min(new long[]{5L, -10L, 3L}));
    }

    @Test
    public void testMax_longArray_returnsMaximum() {
        assertEquals(5L, NumberUtils.max(new long[]{5L, -10L, 3L}));
    }

    @Test
    public void testMin_shortArray_returnsMinimum() {
        assertEquals(-10, NumberUtils.min(new short[]{5, -10, 3}));
    }

    @Test
    public void testMax_shortArray_returnsMaximum() {
        assertEquals(5, NumberUtils.max(new short[]{5, -10, 3}));
    }

    @Test
    public void testMin_byteArray_returnsMinimum() {
        assertEquals(-10, NumberUtils.min(new byte[]{5, -10, 3}));
    }

    @Test
    public void testMax_byteArray_returnsMaximum() {
        assertEquals(5, NumberUtils.max(new byte[]{5, -10, 3}));
    }

    @Test
    public void testMin_floatArray_returnsMinimum() {
        assertEquals(-10.0f, NumberUtils.min(new float[]{5.0f, -10.0f, 3.0f}), 0.0f);
    }

    @Test
    public void testMax_floatArray_returnsMaximum() {
        assertEquals(5.0f, NumberUtils.max(new float[]{5.0f, -10.0f, 3.0f}), 0.0f);
    }

    @Test
    public void testMin_doubleArray_returnsMinimum() {
        assertEquals(-10.0d, NumberUtils.min(new double[]{5.0d, -10.0d, 3.0d}), 0.0d);
    }

    @Test
    public void testMax_doubleArray_returnsMaximum() {
        assertEquals(5.0d, NumberUtils.max(new double[]{5.0d, -10.0d, 3.0d}), 0.0d);
    }

    @Test
    public void testMin_doubleArrayWithNaN_returnsNaN() {
        assertTrue(Double.isNaN(NumberUtils.min(new double[]{1.0d, Double.NaN, 3.0d})));
    }

    @Test
    public void testMin_floatArrayWithNaN_returnsNaN() {
        assertTrue(Float.isNaN(NumberUtils.min(new float[]{1.0f, Float.NaN, 3.0f})));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMin_intArrayNull_throwsIllegalArgumentException() {
        NumberUtils.min((int[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMax_doubleArrayEmpty_throwsIllegalArgumentException() {
        NumberUtils.max(new double[0]);
    }

    @Test
    public void testMin_longValues_returnsMinimum() {
        assertEquals(-10L, NumberUtils.min(5L, -10L, 3L));
    }

    @Test
    public void testMax_longValues_returnsMaximum() {
        assertEquals(5L, NumberUtils.max(5L, -10L, 3L));
    }

    @Test
    public void testMin_shortValues_returnsMinimum() {
        assertEquals(-10, NumberUtils.min((short) 5, (short) -10, (short) 3));
    }

    @Test
    public void testMax_shortValues_returnsMaximum() {
        assertEquals(5, NumberUtils.max((short) 5, (short) -10, (short) 3));
    }

    @Test
    public void testMin_byteValues_returnsMinimum() {
        assertEquals(-10, NumberUtils.min((byte) 5, (byte) -10, (byte) 3));
    }

    @Test
    public void testMax_byteValues_returnsMaximum() {
        assertEquals(5, NumberUtils.max((byte) 5, (byte) -10, (byte) 3));
    }

    @Test
    public void testMin_floatValues_returnsMinimum() {
        assertEquals(-10.0f, NumberUtils.min(5.0f, -10.0f, 3.0f), 0.0f);
    }

    @Test
    public void testMax_floatValues_returnsMaximum() {
        assertEquals(5.0f, NumberUtils.max(5.0f, -10.0f, 3.0f), 0.0f);
    }

    @Test
    public void testMin_doubleValues_returnsMinimum() {
        assertEquals(-10.0d, NumberUtils.min(5.0d, -10.0d, 3.0d), 0.0d);
    }

    @Test
    public void testMax_doubleValues_returnsMaximum() {
        assertEquals(5.0d, NumberUtils.max(5.0d, -10.0d, 3.0d), 0.0d);
    }

    @Test
    public void testMin_threeDoubleValuesWithNaN_returnsNaN() {
        assertTrue(Double.isNaN(NumberUtils.min(1.0d, Double.NaN, 3.0d)));
    }
}