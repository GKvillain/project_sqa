package org.apache.commons.lang.math;

import static org.junit.Assert.*;
import org.junit.Test;

public class NumberUtilsTest {

    // Tests null input to toInt, should return default 0
    @Test
    public void testToInt_null_returnsDefaultValue() {
        assertEquals(0, NumberUtils.toInt(null));
        assertEquals(5, NumberUtils.toInt(null, 5));
    }

    // Tests empty string to toInt, should return default
    @Test
    public void testToInt_emptyString_returnsDefaultValue() {
        assertEquals(0, NumberUtils.toInt(""));
        assertEquals(3, NumberUtils.toInt("", 3));
    }

    // Tests invalid string to toInt, should return default
    @Test
    public void testToInt_invalidString_returnsDefaultValue() {
        assertEquals(0, NumberUtils.toInt("abc"));
        assertEquals(7, NumberUtils.toInt("12.3", 7));
    }

    // Tests valid string to toInt
    @Test
    public void testToInt_validString_returnsCorrectInt() {
        assertEquals(42, NumberUtils.toInt("42"));
        assertEquals(-17, NumberUtils.toInt("-17"));
    }

    // Tests valid string to toLong
    @Test
    public void testToLong_validString_returnsCorrectLong() {
        assertEquals(123L, NumberUtils.toLong("123"));
        assertEquals(-45L, NumberUtils.toLong("-45"));
    }

    // Tests createNumber with null input, should return null
    @Test
    public void testCreateNumber_null_returnsNull() {
        assertNull(NumberUtils.createNumber(null));
    }

    // Tests createNumber with blank string, should throw NumberFormatException
    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_blank_throwsNumberFormatException() {
        NumberUtils.createNumber("");
    }

    // Tests createNumber with hex prefix, should return Integer
    @Test
    public void testCreateNumber_hex_returnsInteger() {
        Number num = NumberUtils.createNumber("0x1F");
        assertTrue(num instanceof Integer);
        assertEquals(31, num.intValue());
    }

    // Tests createNumber with negative hex prefix, should return Integer
    @Test
    public void testCreateNumber_negativeHex_returnsInteger() {
        Number num = NumberUtils.createNumber("-0x10");
        assertTrue(num instanceof Integer);
        assertEquals(-16, num.intValue());
    }

    // Tests createNumber with long suffix, should return Long
    @Test
    public void testCreateNumber_longSuffix_returnsLong() {
        Number num = NumberUtils.createNumber("1234567890L");
        assertTrue(num instanceof Long);
        assertEquals(1234567890L, num.longValue());
    }

    // Tests createNumber with float suffix, should return Float
    @Test
    public void testCreateNumber_floatSuffix_returnsFloat() {
        Number num = NumberUtils.createNumber("1.5f");
        assertTrue(num instanceof Float);
        assertEquals(1.5f, num.floatValue(), 0.0001);
    }

    // Tests createNumber with double suffix, should return Double
    @Test
    public void testCreateNumber_doubleSuffix_returnsDouble() {
        Number num = NumberUtils.createNumber("1.5d");
        assertTrue(num instanceof Double);
        assertEquals(1.5d, num.doubleValue(), 0.0001);
    }

    // Tests createNumber with decimal point, should return Float first
    @Test
    public void testCreateNumber_decimal_returnsFloat() {
        Number num = NumberUtils.createNumber("3.14");
        assertTrue(num instanceof Float);
        assertEquals(3.14f, num.floatValue(), 0.0001);
    }

    // Tests createNumber with scientific notation, should return Float or Double
    @Test
    public void testCreateNumber_scientific_returnsNumber() {
        Number num = NumberUtils.createNumber("1e2");
        assertTrue(num instanceof Float);
        assertEquals(100.0f, num.floatValue(), 0.0001);
    }

    // Tests createNumber with invalid suffix, should throw NumberFormatException
    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_invalidSuffix_throwsNumberFormatException() {
        NumberUtils.createNumber("12L");
    }

    // Tests isNumber with valid integer string, should return true
    @Test
    public void testIsNumber_validInteger_returnsTrue() {
        assertTrue(NumberUtils.isNumber("123"));
        assertTrue(NumberUtils.isNumber("-456"));
    }

    // Tests isNumber with valid decimal string, should return true
    @Test
    public void testIsNumber_validDecimal_returnsTrue() {
        assertTrue(NumberUtils.isNumber("3.14"));
    }

    // Tests isNumber with valid hex string, should return true
    @Test
    public void testIsNumber_hex_returnsTrue() {
        assertTrue(NumberUtils.isNumber("0x1A"));
        assertTrue(NumberUtils.isNumber("-0x10"));
    }

    // Tests isNumber with invalid string (two decimals), should return false
    @Test
    public void testIsNumber_invalidDoubleDecimal_returnsFalse() {
        assertFalse(NumberUtils.isNumber("1.2.3"));
    }

    // Tests isNumber with invalid string (blank), should return false
    @Test
    public void testIsNumber_blank_returnsFalse() {
        assertFalse(NumberUtils.isNumber(""));
        assertFalse(NumberUtils.isNumber(null));
    }

    // Tests isDigits with valid digits, should return true
    @Test
    public void testIsDigits_valid_returnsTrue() {
        assertTrue(NumberUtils.isDigits("12345"));
    }

    // Tests isDigits with null/empty/negative, should return false
    @Test
    public void testIsDigits_invalid_returnsFalse() {
        assertFalse(NumberUtils.isDigits(null));
        assertFalse(NumberUtils.isDigits(""));
        assertFalse(NumberUtils.isDigits("-123"));
    }

    // Tests min with int array
    @Test
    public void testMin_intArray_returnsMinimum() {
        assertEquals(1, NumberUtils.min(new int[]{5, 1, 3}));
        assertEquals(-9, NumberUtils.min(new int[]{-9, 2, 0}));
    }

    // Tests max with int array
    @Test
    public void testMax_intArray_returnsMaximum() {
        assertEquals(9, NumberUtils.max(new int[]{5, 9, 3}));
        assertEquals(7, NumberUtils.max(new int[]{-2, 7, 0}));
    }

    // Tests min with empty array throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testMin_emptyArray_throwsIllegalArgumentException() {
        NumberUtils.min(new int[0]);
    }

    // Tests compare doubles with -0.0 vs 0.0
    @Test
    public void testCompare_doubleZeroes_returnsNegative() {
        assertEquals(-1, NumberUtils.compare(-0.0, 0.0));
        assertEquals(1, NumberUtils.compare(0.0, -0.0));
        assertEquals(0, NumberUtils.compare(0.0, 0.0));
    }

    // ========== NEW TEST CASES FOR UNCOVERED CODE ==========

    // createNumber with uppercase hex prefix 0X
    @Test
    public void testCreateNumber_hexUppercase_returnsInteger() {
        Number num = NumberUtils.createNumber("0XFF");
        assertTrue(num instanceof Integer);
        assertEquals(255, num.intValue());
    }

    // createNumber with negative long suffix
    @Test
    public void testCreateNumber_negativeLongSuffix_returnsLong() {
        Number num = NumberUtils.createNumber("-123L");
        assertTrue(num instanceof Long);
        assertEquals(-123L, num.longValue());
    }

    // createNumber with double value (no suffix, with decimal and exponent)
    @Test
    public void testCreateNumber_decimalWithExponent_returnsDouble() {
        Number num = NumberUtils.createNumber("1.5e2");
        assertTrue(num instanceof Double);
        assertEquals(150.0, num.doubleValue(), 0.0001);
    }

    // createNumber with negative decimal
    @Test
    public void testCreateNumber_negativeDecimal_returnsFloat() {
        Number num = NumberUtils.createNumber("-3.14");
        assertTrue(num instanceof Float);
        assertEquals(-3.14f, num.floatValue(), 0.0001);
    }

    // createNumber with BigInteger (very large number without suffix)
    @Test
    public void testCreateNumber_largeNumber_returnsBigInteger() {
        Number num = NumberUtils.createNumber("99999999999999999999");
        assertTrue(num instanceof java.math.BigInteger);
        assertEquals(new java.math.BigInteger("99999999999999999999"), num);
    }

    // createNumber with BigDecimal (large decimal without suffix)
    @Test
    public void testCreateNumber_largeDecimal_returnsBigDecimal() {
        Number num = NumberUtils.createNumber("1.9999999999999999999");
        assertTrue(num instanceof java.math.BigDecimal);
        assertEquals(new java.math.BigDecimal("1.9999999999999999999"), num);
    }

    // createNumber with invalid format (two exponents)
    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_invalidExponent_throwsNumberFormatException() {
        NumberUtils.createNumber("1e2e3");
    }

    // createNumber with invalid format (trailing 'L' after decimal)
    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_decimalWithL_throwsNumberFormatException() {
        NumberUtils.createNumber("1.5L");
    }

    // createNumber with string that has leading zeros
    @Test
    public void testCreateNumber_leadingZeros_returnsInteger() {
        Number num = NumberUtils.createNumber("007");
        assertTrue(num instanceof Integer);
        assertEquals(7, num.intValue());
    }

    // createFloat with null
    @Test
    public void testCreateFloat_null_returnsNull() {
        assertNull(NumberUtils.createFloat(null));
    }

    // createFloat with valid string
    @Test
    public void testCreateFloat_valid_returnsFloat() {
        assertEquals(2.5f, NumberUtils.createFloat("2.5"), 0.0001);
    }

    // createFloat with invalid string
    @Test(expected = NumberFormatException.class)
    public void testCreateFloat_invalid_throwsNumberFormatException() {
        NumberUtils.createFloat("abc");
    }

    // createDouble with null
    @Test
    public void testCreateDouble_null_returnsNull() {
        assertNull(NumberUtils.createDouble(null));
    }

    // createDouble with valid string
    @Test
    public void testCreateDouble_valid_returnsDouble() {
        assertEquals(3.14, NumberUtils.createDouble("3.14"), 0.0001);
    }

    // createLong with null
    @Test
    public void testCreateLong_null_returnsNull() {
        assertNull(NumberUtils.createLong(null));
    }

    // createLong with valid string
    @Test
    public void testCreateLong_valid_returnsLong() {
        assertEquals(1234567890L, NumberUtils.createLong("1234567890").longValue());
    }

    // createLong with invalid string
    @Test(expected = NumberFormatException.class)
    public void testCreateLong_invalid_throwsNumberFormatException() {
        NumberUtils.createLong("12.3");
    }

    // createInteger with null
    @Test
    public void testCreateInteger_null_returnsNull() {
        assertNull(NumberUtils.createInteger(null));
    }

    // createInteger with valid string
    @Test
    public void testCreateInteger_valid_returnsInteger() {
        assertEquals(123, NumberUtils.createInteger("123").intValue());
    }

    // createInteger with invalid string
    @Test(expected = NumberFormatException.class)
    public void testCreateInteger_invalid_throwsNumberFormatException() {
        NumberUtils.createInteger("12.3");
    }

    // createBigInteger with null
    @Test
    public void testCreateBigInteger_null_returnsNull() {
        assertNull(NumberUtils.createBigInteger(null));
    }

    // createBigInteger with valid string
    @Test
    public void testCreateBigInteger_valid_returnsBigInteger() {
        assertEquals(new java.math.BigInteger("12345678901234567890"),
                     NumberUtils.createBigInteger("12345678901234567890"));
    }

    // createBigInteger with hex prefix
    @Test
    public void testCreateBigInteger_hex_returnsBigInteger() {
        assertEquals(new java.math.BigInteger("1F", 16),
                     NumberUtils.createBigInteger("#1F"));
    }

    // createBigDecimal with null
    @Test
    public void testCreateBigDecimal_null_returnsNull() {
        assertNull(NumberUtils.createBigDecimal(null));
    }

    // createBigDecimal with valid string
    @Test
    public void testCreateBigDecimal_valid_returnsBigDecimal() {
        assertEquals(new java.math.BigDecimal("3.14159"),
                     NumberUtils.createBigDecimal("3.14159"));
    }

    // createBigDecimal with invalid string
    @Test(expected = NumberFormatException.class)
    public void testCreateBigDecimal_invalid_throwsNumberFormatException() {
        NumberUtils.createBigDecimal("abc");
    }

    // isNumber with valid long suffix
    @Test
    public void testIsNumber_longSuffix_returnsTrue() {
        assertTrue(NumberUtils.isNumber("123L"));
        assertTrue(NumberUtils.isNumber("-456L"));
    }

    // isNumber with valid float suffix
    @Test
    public void testIsNumber_floatSuffix_returnsTrue() {
        assertTrue(NumberUtils.isNumber("1.5f"));
        assertTrue(NumberUtils.isNumber("2.0F"));
    }

    // isNumber with valid double suffix
    @Test
    public void testIsNumber_doubleSuffix_returnsTrue() {
        assertTrue(NumberUtils.isNumber("3.14d"));
        assertTrue(NumberUtils.isNumber("1.5D"));
    }

    // isNumber with valid scientific notation (e/E)
    @Test
    public void testIsNumber_scientific_returnsTrue() {
        assertTrue(NumberUtils.isNumber("1e10"));
        assertTrue(NumberUtils.isNumber("2.5E-3"));
    }

    // isNumber with negative decimal
    @Test
    public void testIsNumber_negativeDecimal_returnsTrue() {
        assertTrue(NumberUtils.isNumber("-3.14"));
    }

    // isNumber with string that has leading sign but no digits
    @Test
    public void testIsNumber_onlySign_returnsFalse() {
        assertFalse(NumberUtils.isNumber("+"));
        assertFalse(NumberUtils.isNumber("-"));
    }

    // isNumber with string containing letters
    @Test
    public void testIsNumber_letters_returnsFalse() {
        assertFalse(NumberUtils.isNumber("12a34"));
    }

    // isNumber with string containing 'g' suffix (invalid)
    @Test
    public void testIsNumber_invalidSuffix_returnsFalse() {
        assertFalse(NumberUtils.isNumber("123g"));
    }

    // isNumber with string containing multiple signs
    @Test
    public void testIsNumber_multipleSigns_returnsFalse() {
        assertFalse(NumberUtils.isNumber("--123"));
        assertFalse(NumberUtils.isNumber("+-123"));
    }

    // toLong with null
    @Test
    public void testToLong_null_returnsDefault() {
        assertEquals(0L, NumberUtils.toLong(null));
        assertEquals(10L, NumberUtils.toLong(null, 10L));
    }

    // toLong with empty string
    @Test
    public void testToLong_empty_returnsDefault() {
        assertEquals(0L, NumberUtils.toLong(""));
        assertEquals(5L, NumberUtils.toLong("", 5L));
    }

    // toLong with invalid string
    @Test
    public void testToLong_invalid_returnsDefault() {
        assertEquals(0L, NumberUtils.toLong("abc"));
        assertEquals(7L, NumberUtils.toLong("12.3", 7L));
    }

    // toFloat with null
    @Test
    public void testToFloat_null_returnsDefault() {
        assertEquals(0.0f, NumberUtils.toFloat(null), 0.0001);
        assertEquals(2.5f, NumberUtils.toFloat(null, 2.5f), 0.0001);
    }

    // toFloat with valid string
    @Test
    public void testToFloat_valid_returnsFloat() {
        assertEquals(3.14f, NumberUtils.toFloat("3.14"), 0.0001);
    }

    // toFloat with invalid string
    @Test
    public void testToFloat_invalid_returnsDefault() {
        assertEquals(0.0f, NumberUtils.toFloat("abc"), 0.0001);
        assertEquals(1.5f, NumberUtils.toFloat("abc", 1.5f), 0.0001);
    }

    // toDouble with null
    @Test
    public void testToDouble_null_returnsDefault() {
        assertEquals(0.0, NumberUtils.toDouble(null), 0.0001);
        assertEquals(2.5, NumberUtils.toDouble(null, 2.5), 0.0001);
    }

    // toDouble with valid string
    @Test
    public void testToDouble_valid_returnsDouble() {
        assertEquals(3.14159, NumberUtils.toDouble("3.14159"), 0.0001);
    }

    // toDouble with invalid string
    @Test
    public void testToDouble_invalid_returnsDefault() {
        assertEquals(0.0, NumberUtils.toDouble("abc"), 0.0001);
        assertEquals(1.5, NumberUtils.toDouble("abc", 1.5), 0.0001);
    }

    // toByte with valid string
    @Test
    public void testToByte_valid_returnsByte() {
        assertEquals((byte)127, NumberUtils.toByte("127"));
    }

    // toByte with null
    @Test
    public void testToByte_null_returnsDefault() {
        assertEquals((byte)0, NumberUtils.toByte(null));
        assertEquals((byte)5, NumberUtils.toByte(null, (byte)5));
    }

    // toShort with valid string
    @Test
    public void testToShort_valid_returnsShort() {
        assertEquals((short)32767, NumberUtils.toShort("32767"));
    }

    // min with long array
    @Test
    public void testMin_longArray_returnsMinimum() {
        assertEquals(1L, NumberUtils.min(new long[]{5L, 1L, 3L}));
        assertEquals(-9L, NumberUtils.min(new long[]{-9L, 2L, 0L}));
    }

    // min with float array
    @Test
    public void testMin_floatArray_returnsMinimum() {
        assertEquals(1.5f, NumberUtils.min(new float[]{5.5f, 1.5f, 3.0f}), 0.0001);
        assertEquals(-9.0f, NumberUtils.min(new float[]{-9.0f, 2.0f, 0.0f}), 0.0001);
    }

    // min with double array
    @Test
    public void testMin_doubleArray_returnsMinimum() {
        assertEquals(1.5, NumberUtils.min(new double[]{5.5, 1.5, 3.0}), 0.0001);
        assertEquals(-9.0, NumberUtils.min(new double[]{-9.0, 2.0, 0.0}), 0.0001);
    }

    // max with long array
    @Test
    public void testMax_longArray_returnsMaximum() {
        assertEquals(9L, NumberUtils.max(new long[]{5L, 9L, 3L}));
        assertEquals(7L, NumberUtils.max(new long[]{-2L, 7L, 0L}));
    }

    // max with float array
    @Test
    public void testMax_floatArray_returnsMaximum() {
        assertEquals(5.5f, NumberUtils.max(new float[]{5.5f, 1.5f, 3.0f}), 0.0001);
        assertEquals(7.0f, NumberUtils.max(new float[]{-2.0f, 7.0f, 0.0f}), 0.0001);
    }

    // max with double array
    @Test
    public void testMax_doubleArray_returnsMaximum() {
        assertEquals(5.5, NumberUtils.max(new double[]{5.5, 1.5, 3.0}), 0.0001);
        assertEquals(7.0, NumberUtils.max(new double[]{-2.0, 7.0, 0.0}), 0.0001);
    }

    // min with empty long array
    @Test(expected = IllegalArgumentException.class)
    public void testMin_emptyLongArray_throwsIllegalArgumentException() {
        NumberUtils.min(new long[0]);
    }

    // min with empty float array
    @Test(expected = IllegalArgumentException.class)
    public void testMin_emptyFloatArray_throwsIllegalArgumentException() {
        NumberUtils.min(new float[0]);
    }

    // min with empty double array
    @Test(expected = IllegalArgumentException.class)
    public void testMin_emptyDoubleArray_throwsIllegalArgumentException() {
        NumberUtils.min(new double[0]);
    }

    // max with empty int array
    @Test(expected = IllegalArgumentException.class)
    public void testMax_emptyIntArray_throwsIllegalArgumentException() {
        NumberUtils.max(new int[0]);
    }

    // max with empty long array
    @Test(expected = IllegalArgumentException.class)
    public void testMax_emptyLongArray_throwsIllegalArgumentException() {
        NumberUtils.max(new long[0]);
    }

    // max with empty float array
    @Test(expected = IllegalArgumentException.class)
    public void testMax_emptyFloatArray_throwsIllegalArgumentException() {
        NumberUtils.max(new float[0]);
    }

    // max with empty double array
    @Test(expected = IllegalArgumentException.class)
    public void testMax_emptyDoubleArray_throwsIllegalArgumentException() {
        NumberUtils.max(new double[0]);
    }

    // min with three ints (non-array version)
    @Test
    public void testMin_threeInts_returnsMinimum() {
        assertEquals(1, NumberUtils.min(5, 1, 3));
        assertEquals(-9, NumberUtils.min(-9, 2, 0));
    }

    // max with three ints (non-array version)
    @Test
    public void testMax_threeInts_returnsMaximum() {
        assertEquals(9, NumberUtils.max(5, 9, 3));
        assertEquals(7, NumberUtils.max(-2, 7, 0));
    }

    // min with three longs (non-array version)
    @Test
    public void testMin_threeLongs_returnsMinimum() {
        assertEquals(1L, NumberUtils.min(5L, 1L, 3L));
        assertEquals(-9L, NumberUtils.min(-9L, 2L, 0L));
    }

    // max with three longs (non-array version)
    @Test
    public void testMax_threeLongs_returnsMaximum() {
        assertEquals(9L, NumberUtils.max(5L, 9L, 3L));
        assertEquals(7L, NumberUtils.max(-2L, 7L, 0L));
    }

    // min with three floats (non-array version)
    @Test
    public void testMin_threeFloats_returnsMinimum() {
        assertEquals(1.5f, NumberUtils.min(5.5f, 1.5f, 3.0f), 0.0001);
        assertEquals(-9.0f, NumberUtils.min(-9.0f, 2.0f, 0.0f), 0.0001);
    }

    // max with three floats (non-array version)
    @Test
    public void testMax_threeFloats_returnsMaximum() {
        assertEquals(5.5f, NumberUtils.max(5.5f, 1.5f, 3.0f), 0.0001);
        assertEquals(7.0f, NumberUtils.max(-2.0f, 7.0f, 0.0f), 0.0001);
    }

    // min with three doubles (non-array version)
    @Test
    public void testMin_threeDoubles_returnsMinimum() {
        assertEquals(1.5, NumberUtils.min(5.5, 1.5, 3.0), 0.0001);
        assertEquals(-9.0, NumberUtils.min(-9.0, 2.0, 0.0), 0.0001);
    }

    // max with three doubles (non-array version)
    @Test
    public void testMax_threeDoubles_returnsMaximum() {
        assertEquals(5.5, NumberUtils.max(5.5, 1.5, 3.0), 0.0001);
        assertEquals(7.0, NumberUtils.max(-2.0, 7.0, 0.0), 0.0001);
    }

    // compare with NaN values
    @Test
    public void testCompare_doubleNaN_returnsCorrect() {
        assertEquals(1, NumberUtils.compare(Double.NaN, 1.0));
        assertEquals(-1, NumberUtils.compare(1.0, Double.NaN));
        assertEquals(0, NumberUtils.compare(Double.NaN, Double.NaN));
    }

    // compare with normal double values
    @Test
    public void testCompare_doubleNormal_returnsCorrect() {
        assertEquals(-1, NumberUtils.compare(1.0, 2.0));
        assertEquals(1, NumberUtils.compare(2.0, 1.0));
        assertEquals(0, NumberUtils.compare(1.0, 1.0));
    }

    // compare with negative values
    @Test
    public void testCompare_doubleNegative_returnsCorrect() {
        assertEquals(-1, NumberUtils.compare(-5.0, -3.0));
        assertEquals(1, NumberUtils.compare(-3.0, -5.0));
    }

    // isNumber with negative hex
    @Test
    public void testIsNumber_negativeHexWithOx_returnsTrue() {
        assertTrue(NumberUtils.isNumber("-0xFF"));
    }

    // isNumber with hex prefix only
    @Test
    public void testIsNumber_hexPrefixOnly_returnsFalse() {
        assertFalse(NumberUtils.isNumber("0x"));
        assertFalse(NumberUtils.isNumber("-0x"));
    }

    // isNumber with string that has 'L' suffix lowercase
    @Test
    public void testIsNumber_lowercaseL_returnsFalse() {
        assertFalse(NumberUtils.isNumber("123l"));
    }

    // isNumber with string that has leading '.' 
    @Test
    public void testIsNumber_leadingDecimal_returnsFalse() {
        assertFalse(NumberUtils.isNumber(".123"));
    }

    // isNumber with string that has trailing '.'
    @Test
    public void testIsNumber_trailingDecimal_returnsFalse() {
        assertFalse(NumberUtils.isNumber("123."));
    }

    // isNumber with string that has 'e' but no digits after
    @Test
    public void testIsNumber_exponentWithoutDigits_returnsFalse() {
        assertFalse(NumberUtils.isNumber("1e"));
        assertFalse(NumberUtils.isNumber("1E"));
    }

    // isNumber with string that has sign after 'e' but no digits
    @Test
    public void testIsNumber_exponentSignWithoutDigits_returnsFalse() {
        assertFalse(NumberUtils.isNumber("1e+"));
        assertFalse(NumberUtils.isNumber("1E-"));
    }

    // isNumber with string that has 'd' suffix on non-decimal
    @Test
    public void testIsNumber_dSuffixOnInteger_returnsTrue() {
        assertTrue(NumberUtils.isNumber("123d"));
        assertTrue(NumberUtils.isNumber("123D"));
    }

    // isNumber with string that has 'f' suffix on non-decimal
    @Test
    public void testIsNumber_fSuffixOnInteger_returnsTrue() {
        assertTrue(NumberUtils.isNumber("123f"));
        assertTrue(NumberUtils.isNumber("123F"));
    }

    // isNumber with string containing multiple 'e'
    @Test
    public void testIsNumber_multipleE_returnsFalse() {
        assertFalse(NumberUtils.isNumber("1e2e3"));
    }

    // isNumber with string containing invalid hex characters
    @Test
    public void testIsNumber_invalidHexChars_returnsFalse() {
        assertFalse(NumberUtils.isNumber("0x1G"));
    }

    // isDigits with string containing letters
    @Test
    public void testIsDigits_withLetters_returnsFalse() {
        assertFalse(NumberUtils.isDigits("123a"));
    }

    // isDigits with string containing signs
    @Test
    public void testIsDigits_withSigns_returnsFalse() {
        assertFalse(NumberUtils.isDigits("+123"));
    }

    // isDigits with string containing decimal point
    @Test
    public void testIsDigits_withDecimal_returnsFalse() {
        assertFalse(NumberUtils.isDigits("12.3"));
    }
}