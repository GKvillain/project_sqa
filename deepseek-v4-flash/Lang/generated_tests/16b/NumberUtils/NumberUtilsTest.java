package org.apache.commons.lang3.math;

import static org.junit.Assert.*;

import org.junit.Test;

public class NumberUtilsTest {

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

    // Tests toInt with valid string
    @Test
    public void testToInt_validString_returnsInt() {
        assertEquals(123, NumberUtils.toInt("123"));
    }

    // Tests toLong with valid string
    @Test
    public void testToLong_validString_returnsLong() {
        assertEquals(999999999L, NumberUtils.toLong("999999999"));
    }

    // Tests toFloat with valid string
    @Test
    public void testToFloat_validString_returnsFloat() {
        assertEquals(3.14f, NumberUtils.toFloat("3.14"), 0.0001f);
    }

    // Tests toDouble with valid string
    @Test
    public void testToDouble_validString_returnsDouble() {
        assertEquals(2.71828, NumberUtils.toDouble("2.71828"), 0.00001);
    }

    // Tests createNumber with null
    @Test
    public void testCreateNumber_nullInput_returnsNull() {
        assertNull(NumberUtils.createNumber(null));
    }

    // Tests createNumber with blank string
    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_blankString_throwsNumberFormatException() {
        NumberUtils.createNumber("  ");
    }

    // Tests createNumber with a valid integer
    @Test
    public void testCreateNumber_validInteger_returnsInteger() {
        Number result = NumberUtils.createNumber("42");
        assertTrue(result instanceof Integer);
        assertEquals(42, result.intValue());
    }

    // Tests createNumber with a valid long
    @Test
    public void testCreateNumber_validLong_returnsLong() {
        Number result = NumberUtils.createNumber("123456789");
        assertTrue(result instanceof Long);
        assertEquals(123456789L, result.longValue());
    }

    // Tests createNumber with a valid float
    @Test
    public void testCreateNumber_validFloat_returnsFloat() {
        Number result = NumberUtils.createNumber("1.5f");
        assertTrue(result instanceof Float);
        assertEquals(1.5f, result.floatValue(), 0.0001f);
    }

    // Tests createNumber with a valid double
    @Test
    public void testCreateNumber_validDouble_returnsDouble() {
        Number result = NumberUtils.createNumber("2.5d");
        assertTrue(result instanceof Double);
        assertEquals(2.5, result.doubleValue(), 0.0001);
    }

    // Tests createNumber with hex prefix
    @Test
    public void testCreateNumber_hexPrefix_returnsInteger() {
        Number result = NumberUtils.createNumber("0x1A");
        assertTrue(result instanceof Integer);
        assertEquals(26, result.intValue());
    }

    // Tests createNumber with 'L' suffix (valid long)
    @Test
    public void testCreateNumber_numberWithL_suffix_returnsLong() {
        Number result = NumberUtils.createNumber("1234L");
        assertTrue(result instanceof Long);
        assertEquals(1234L, result.longValue());
    }

    // Tests createNumber with single 'l' input – expects NumberFormatException (defect detection)
    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_singleL_throwsNumberFormatException() {
        NumberUtils.createNumber("l");
    }

    // Tests createNumber with single 'L' input – expects NumberFormatException (defect detection)
    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_singleL_uppercase_throwsNumberFormatException() {
        NumberUtils.createNumber("L");
    }

    // Tests createNumber with negative long and 'L' suffix
    @Test
    public void testCreateNumber_negativeLongL_returnsLong() {
        Number result = NumberUtils.createNumber("-123L");
        assertTrue(result instanceof Long);
        assertEquals(-123L, result.longValue());
    }

    // Tests createNumber with very large number returns BigInteger
    @Test
    public void testCreateNumber_veryLargeNumber_returnsBigInteger() {
        Number result = NumberUtils.createNumber("999999999999999999999999999999");
        assertTrue(result instanceof java.math.BigInteger);
    }

    // Tests createNumber with a BigDecimal string
    @Test
    public void testCreateNumber_bigDecimalString_returnsBigDecimal() {
        Number result = NumberUtils.createNumber("1.2345678901234567890");
        assertTrue(result instanceof java.math.BigDecimal);
        assertEquals(new java.math.BigDecimal("1.2345678901234567890"), result);
    }

    // Tests isNumber with valid hex string
    @Test
    public void testIsNumber_validHex_returnsTrue() {
        assertTrue(NumberUtils.isNumber("0xFF"));
    }

    // ========== New Test Cases for Better Coverage ==========

    // Test toInt with default value
    @Test
    public void testToInt_nullInputWithDefault_returnsDefault() {
        assertEquals(5, NumberUtils.toInt(null, 5));
    }

    @Test
    public void testToInt_emptyStringWithDefault_returnsDefault() {
        assertEquals(10, NumberUtils.toInt("", 10));
    }

    @Test
    public void testToInt_validStringWithDefault_returnsInt() {
        assertEquals(77, NumberUtils.toInt("77", 0));
    }

    // Test toLong with null/empty/default
    @Test
    public void testToLong_nullInput_returnsZero() {
        assertEquals(0L, NumberUtils.toLong(null));
    }

    @Test
    public void testToLong_nullInputWithDefault_returnsDefault() {
        assertEquals(100L, NumberUtils.toLong(null, 100L));
    }

    @Test
    public void testToLong_emptyString_returnsZero() {
        assertEquals(0L, NumberUtils.toLong(""));
    }

    @Test
    public void testToLong_emptyStringWithDefault_returnsDefault() {
        assertEquals(200L, NumberUtils.toLong("", 200L));
    }

    // Test toFloat with null/empty/default
    @Test
    public void testToFloat_nullInput_returnsZero() {
        assertEquals(0.0f, NumberUtils.toFloat(null), 0.0f);
    }

    @Test
    public void testToFloat_nullInputWithDefault_returnsDefault() {
        assertEquals(1.5f, NumberUtils.toFloat(null, 1.5f), 0.0001f);
    }

    @Test
    public void testToFloat_emptyString_returnsZero() {
        assertEquals(0.0f, NumberUtils.toFloat(""), 0.0f);
    }

    @Test
    public void testToFloat_emptyStringWithDefault_returnsDefault() {
        assertEquals(2.5f, NumberUtils.toFloat("", 2.5f), 0.0001f);
    }

    // Test toDouble with null/empty/default
    @Test
    public void testToDouble_nullInput_returnsZero() {
        assertEquals(0.0, NumberUtils.toDouble(null), 0.0);
    }

    @Test
    public void testToDouble_nullInputWithDefault_returnsDefault() {
        assertEquals(3.14, NumberUtils.toDouble(null, 3.14), 0.0001);
    }

    @Test
    public void testToDouble_emptyString_returnsZero() {
        assertEquals(0.0, NumberUtils.toDouble(""), 0.0);
    }

    @Test
    public void testToDouble_emptyStringWithDefault_returnsDefault() {
        assertEquals(2.71, NumberUtils.toDouble("", 2.71), 0.0001);
    }

    // Additional createNumber edge cases
    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_invalidString_throwsNumberFormatException() {
        NumberUtils.createNumber("abc123");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_plusMinusOnly_throwsNumberFormatException() {
        NumberUtils.createNumber("+-");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_trailingDot_throwsNumberFormatException() {
        NumberUtils.createNumber("123.");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_leadingDot_throwsNumberFormatException() {
        NumberUtils.createNumber(".456");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_hexWithDecimal_throwsNumberFormatException() {
        NumberUtils.createNumber("0x1A.5");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_doubleSuffixOnHex_throwsNumberFormatException() {
        NumberUtils.createNumber("0x1Ad");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_multiplePoints_throwsNumberFormatException() {
        NumberUtils.createNumber("12.34.56");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_negativeHex_throwsNumberFormatException() {
        NumberUtils.createNumber("-0x1A");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_justNegativeSign_throwsNumberFormatException() {
        NumberUtils.createNumber("-");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_justPositiveSign_throwsNumberFormatException() {
        NumberUtils.createNumber("+");
    }

    @Test
    public void testCreateNumber_positiveSignInteger_returnsInteger() {
        Number result = NumberUtils.createNumber("+42");
        assertTrue(result instanceof Integer);
        assertEquals(42, result.intValue());
    }

    @Test
    public void testCreateNumber_positiveSignLong_returnsLong() {
        Number result = NumberUtils.createNumber("+123L");
        assertTrue(result instanceof Long);
        assertEquals(123L, result.longValue());
    }

    @Test
    public void testCreateNumber_positiveSignFloat_returnsFloat() {
        Number result = NumberUtils.createNumber("+1.5f");
        assertTrue(result instanceof Float);
        assertEquals(1.5f, result.floatValue(), 0.0001f);
    }

    @Test
    public void testCreateNumber_positiveSignDouble_returnsDouble() {
        Number result = NumberUtils.createNumber("+2.5d");
        assertTrue(result instanceof Double);
        assertEquals(2.5, result.doubleValue(), 0.0001);
    }

    // isNumber additional tests
    @Test
    public void testIsNumber_invalidString_returnsFalse() {
        assertFalse(NumberUtils.isNumber("abc123"));
    }

    @Test
    public void testIsNumber_emptyString_returnsFalse() {
        assertFalse(NumberUtils.isNumber(""));
    }

    @Test
    public void testIsNumber_nullInput_returnsFalse() {
        assertFalse(NumberUtils.isNumber(null));
    }

    @Test
    public void testIsNumber_positiveInteger_returnsTrue() {
        assertTrue(NumberUtils.isNumber("123"));
    }

    @Test
    public void testIsNumber_negativeInteger_returnsTrue() {
        assertTrue(NumberUtils.isNumber("-123"));
    }

    @Test
    public void testIsNumber_positiveFloat_returnsTrue() {
        assertTrue(NumberUtils.isNumber("1.5"));
    }

    @Test
    public void testIsNumber_negativeFloat_returnsTrue() {
        assertTrue(NumberUtils.isNumber("-1.5"));
    }

    @Test
    public void testIsNumber_hexLowerCase_returnsTrue() {
        assertTrue(NumberUtils.isNumber("0xabcdef"));
    }

    @Test
    public void testIsNumber_hexUpperCase_returnsTrue() {
        assertTrue(NumberUtils.isNumber("0XABCDEF"));
    }

    @Test
    public void testIsNumber_hexWithMixedCase_returnsTrue() {
        assertTrue(NumberUtils.isNumber("0xAbCdEf"));
    }

    @Test
    public void testIsNumber_negativeHex_returnsFalse() {
        assertFalse(NumberUtils.isNumber("-0x1A"));
    }

    @Test
    public void testIsNumber_singleDot_returnsFalse() {
        assertFalse(NumberUtils.isNumber("."));
    }

    @Test
    public void testIsNumber_trailingD_returnsTrue() {
        assertTrue(NumberUtils.isNumber("2.5d"));
    }

    @Test
    public void testIsNumber_trailingF_returnsTrue() {
        assertTrue(NumberUtils.isNumber("2.5f"));
    }

    @Test
    public void testIsNumber_trailingL_returnsTrue() {
        assertTrue(NumberUtils.isNumber("123L"));
    }

    @Test
    public void testIsNumber_trailingDLowercase_returnsTrue() {
        assertTrue(NumberUtils.isNumber("2.5d"));
    }

    @Test
    public void testIsNumber_singleL_returnsFalse() {
        assertFalse(NumberUtils.isNumber("l"));
    }

    @Test
    public void testIsNumber_singleL_uppercase_returnsFalse() {
        assertFalse(NumberUtils.isNumber("L"));
    }

    @Test
    public void testIsNumber_justMinusSign_returnsFalse() {
        assertFalse(NumberUtils.isNumber("-"));
    }

    @Test
    public void testIsNumber_justPlusSign_returnsFalse() {
        assertFalse(NumberUtils.isNumber("+"));
    }

    @Test
    public void testIsNumber_stringWithLeadingZeros_returnsTrue() {
        assertTrue(NumberUtils.isNumber("00123"));
    }

    @Test
    public void testIsNumber_floatWithLeadingZeros_returnsTrue() {
        assertTrue(NumberUtils.isNumber("00.5"));
    }

    @Test
    public void testIsNumber_scientificNotation_returnsTrue() {
        assertTrue(NumberUtils.isNumber("1e10"));
    }

    @Test
    public void testIsNumber_scientificNotationNegativeExponent_returnsTrue() {
        assertTrue(NumberUtils.isNumber("1e-5"));
    }

    @Test
    public void testIsNumber_scientificNotationWithDecimal_returnsTrue() {
        assertTrue(NumberUtils.isNumber("1.23e4"));
    }

    @Test
    public void testIsNumber_invalidScientificNotation_returnsFalse() {
        assertFalse(NumberUtils.isNumber("1e"));
    }

    @Test
    public void testIsNumber_doubleSuffix_returnsTrue() {
        assertTrue(NumberUtils.isNumber("2.5d"));
    }

    @Test
    public void testIsNumber_floatSuffix_returnsTrue() {
        assertTrue(NumberUtils.isNumber("2.5f"));
    }

    // Additional createNumber tests for suffixes
    @Test
    public void testCreateNumber_floatSuffixF_returnsFloat() {
        Number result = NumberUtils.createNumber("3.14f");
        assertTrue(result instanceof Float);
        assertEquals(3.14f, result.floatValue(), 0.0001f);
    }

    @Test
    public void testCreateNumber_doubleSuffixD_returnsDouble() {
        Number result = NumberUtils.createNumber("2.71d");
        assertTrue(result instanceof Double);
        assertEquals(2.71, result.doubleValue(), 0.0001);
    }

    @Test
    public void testCreateNumber_scientificNotationDouble_returnsDouble() {
        Number result = NumberUtils.createNumber("1e10");
        assertTrue(result instanceof Double);
        assertEquals(1e10, result.doubleValue(), 0.0);
    }

    @Test
    public void testCreateNumber_scientificNotationNegativeExponent_returnsDouble() {
        Number result = NumberUtils.createNumber("1e-5");
        assertTrue(result instanceof Double);
        assertEquals(1e-5, result.doubleValue(), 1e-10);
    }

    @Test
    public void testCreateNumber_negativeDouble_returnsDouble() {
        Number result = NumberUtils.createNumber("-2.5");
        assertTrue(result instanceof Double);
        assertEquals(-2.5, result.doubleValue(), 0.0001);
    }

    @Test
    public void testCreateNumber_negativeFloatWithFSuffix_returnsFloat() {
        Number result = NumberUtils.createNumber("-1.5f");
        assertTrue(result instanceof Float);
        assertEquals(-1.5f, result.floatValue(), 0.0001f);
    }

    @Test
    public void testCreateNumber_hexBigInteger_returnsBigInteger() {
        Number result = NumberUtils.createNumber("0xFFFFFFFFFFFFFFFF");
        assertTrue(result instanceof java.math.BigInteger);
        assertEquals(new java.math.BigInteger("FFFFFFFFFFFFFFFF", 16), result);
    }

    @Test
    public void testCreateNumber_positiveHex_returnsInteger() {
        Number result = NumberUtils.createNumber("0xFF");
        assertTrue(result instanceof Integer);
        assertEquals(255, result.intValue());
    }
}