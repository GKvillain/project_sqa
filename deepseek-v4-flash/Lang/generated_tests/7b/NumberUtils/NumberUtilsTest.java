package org.apache.commons.lang3.math;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * JUnit 4 Test Class for NumberUtils (Lang 7b bug).
 * Focuses on createNumber, isNumber, and edge cases.
 */
public class NumberUtilsTest {

    // Tests createNumber with null input
    @Test
    public void testCreateNumber_nullInput_returnsNull() {
        assertNull("createNumber(null) should return null", NumberUtils.createNumber(null));
    }

    // Tests createNumber with blank string
    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_blankString_throwsNumberFormatException() {
        NumberUtils.createNumber(" ");
    }

    // Tests createNumber with empty string
    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_emptyString_throwsNumberFormatException() {
        NumberUtils.createNumber("");
    }

    // Tests createNumber with a simple integer string
    @Test
    public void testCreateNumber_simpleInteger_returnsInteger() {
        Number result = NumberUtils.createNumber("123");
        assertNotNull(result);
        assertTrue("Should be an Integer", result instanceof Integer);
        assertEquals(123, result.intValue());
    }

    // Tests createNumber with a hex string
    @Test
    public void testCreateNumber_hexString_returnsInteger() {
        Number result = NumberUtils.createNumber("0xFF");
        assertNotNull(result);
        assertTrue("Should be an Integer", result instanceof Integer);
        assertEquals(255, result.intValue());
    }

    // Tests createNumber with long suffix
    @Test
    public void testCreateNumber_longSuffix_returnsLong() {
        Number result = NumberUtils.createNumber("123L");
        assertNotNull(result);
        assertTrue("Should be a Long", result instanceof Long);
        assertEquals(123L, result.longValue());
    }

    // Tests createNumber with float suffix
    @Test
    public void testCreateNumber_floatSuffix_returnsFloat() {
        Number result = NumberUtils.createNumber("1.5f");
        assertNotNull(result);
        assertTrue("Should be a Float", result instanceof Float);
        assertEquals(1.5f, result.floatValue(), 0.0001);
    }

    // Tests createNumber with double suffix
    @Test
    public void testCreateNumber_doubleSuffix_returnsDouble() {
        Number result = NumberUtils.createNumber("2.5d");
        assertNotNull(result);
        assertTrue("Should be a Double", result instanceof Double);
        assertEquals(2.5d, result.doubleValue(), 0.0001);
    }

    // Tests createNumber with a leading zero (not octal)
    @Test
    public void testCreateNumber_leadingZeros_returnsInteger() {
        Number result = NumberUtils.createNumber("007");
        assertNotNull(result);
        assertTrue("Should be an Integer", result instanceof Integer);
        assertEquals(7, result.intValue());
    }

    // Tests createNumber with negative hex
    @Test
    public void testCreateNumber_negativeHex_returnsInteger() {
        Number result = NumberUtils.createNumber("-0x10");
        assertNotNull(result);
        assertTrue("Should be an Integer", result instanceof Integer);
        assertEquals(-16, result.intValue());
    }

    // Tests createNumber with scientific notation
    @Test
    public void testCreateNumber_scientificNotation_returnsDouble() {
        Number result = NumberUtils.createNumber("1e2");
        assertNotNull(result);
        assertTrue("Should be a Double", result instanceof Double);
        assertEquals(100.0, result.doubleValue(), 0.0001);
    }

    // Tests createNumber with double value
    @Test
    public void testCreateNumber_decimalString_returnsDouble() {
        Number result = NumberUtils.createNumber("3.14");
        assertNotNull(result);
        assertTrue("Should be a Double", result instanceof Double);
        assertEquals(3.14, result.doubleValue(), 0.0001);
    }

    // Tests isNumber with null string
    @Test
    public void testIsNumber_nullInput_returnsFalse() {
        assertFalse("isNumber(null) should return false", NumberUtils.isNumber(null));
    }

    // Tests isNumber with empty string
    @Test
    public void testIsNumber_emptyString_returnsFalse() {
        assertFalse("isNumber(\"\") should return false", NumberUtils.isNumber(""));
    }

    // Tests isNumber with valid integer
    @Test
    public void testIsNumber_validInteger_returnsTrue() {
        assertTrue("isNumber(\"123\") should return true", NumberUtils.isNumber("123"));
    }

    // Tests isNumber with valid decimal
    @Test
    public void testIsNumber_validDecimal_returnsTrue() {
        assertTrue("isNumber(\"3.14\") should return true", NumberUtils.isNumber("3.14"));
    }

    // Tests isNumber with hex string
    @Test
    public void testIsNumber_hexString_returnsTrue() {
        assertTrue("isNumber(\"0xFF\") should return true", NumberUtils.isNumber("0xFF"));
    }

    // Tests isNumber with invalid string
    @Test
    public void testIsNumber_invalidString_returnsFalse() {
        assertFalse("isNumber(\"abc\") should return false", NumberUtils.isNumber("abc"));
    }

    // Tests isNumber with string containing both E and decimal
    @Test
    public void testIsNumber_scientificWithDecimal_returnsTrue() {
        assertTrue("isNumber(\"1.5E2\") should return true", NumberUtils.isNumber("1.5E2"));
    }

    // Tests isNumber with long suffix
    @Test
    public void testIsNumber_longSuffix_returnsTrue() {
        assertTrue("isNumber(\"123L\") should return true", NumberUtils.isNumber("123L"));
    }

    // Tests isNumber with double suffix
    @Test
    public void testIsNumber_doubleSuffix_returnsTrue() {
        assertTrue("isNumber(\"1.5d\") should return true", NumberUtils.isNumber("1.5d"));
    }

    // Tests isNumber with trailing dot
    @Test
    public void testIsNumber_trailingDot_returnsTrue() {
        assertTrue("isNumber(\"123.\") should return true", NumberUtils.isNumber("123."));
    }

    // Tests isNumber with hex but no digits after 0x
    @Test
    public void testIsNumber_hexOnlyPrefix_returnsFalse() {
        assertFalse("isNumber(\"0x\") should return false", NumberUtils.isNumber("0x"));
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

    // Tests toInt with valid string
    @Test
    public void testToInt_validString_returnsInt() {
        assertEquals(5, NumberUtils.toInt("5"));
    }

    // Tests toLong with null input
    @Test
    public void testToLong_nullInput_returnsZero() {
        assertEquals(0L, NumberUtils.toLong(null));
    }

    // Tests toFloat with null input
    @Test
    public void testToFloat_nullInput_returnsZeroFloat() {
        assertEquals(0.0f, NumberUtils.toFloat(null), 0.0001);
    }

    // Tests toDouble with null input
    @Test
    public void testToDouble_nullInput_returnsZeroDouble() {
        assertEquals(0.0d, NumberUtils.toDouble(null), 0.0001);
    }

    // Tests min(long[]) with valid array
    @Test
    public void testMin_longArray_returnsMin() {
        long[] array = {5L, 2L, 8L, 1L, 9L};
        assertEquals(1L, NumberUtils.min(array));
    }

    // Tests max(int[]) with valid array
    @Test
    public void testMax_intArray_returnsMax() {
        int[] array = {5, 2, 8, 1, 9};
        assertEquals(9, NumberUtils.max(array));
    }

    // Tests min with three ints
    @Test
    public void testMin_threeInts_returnsMin() {
        assertEquals(1, NumberUtils.min(5, 1, 3));
    }

    // Tests max with three ints
    @Test
    public void testMax_threeInts_returnsMax() {
        assertEquals(5, NumberUtils.max(5, 1, 3));
    }

    // Tests isDigits with null
    @Test
    public void testIsDigits_nullInput_returnsFalse() {
        assertFalse(NumberUtils.isDigits(null));
    }

    // Tests isDigits with empty string
    @Test
    public void testIsDigits_emptyString_returnsFalse() {
        assertFalse(NumberUtils.isDigits(""));
    }

    // Tests isDigits with valid digit string
    @Test
    public void testIsDigits_validDigits_returnsTrue() {
        assertTrue(NumberUtils.isDigits("12345"));
    }

    // Tests isDigits with non-digit string
    @Test
    public void testIsDigits_nonDigitString_returnsFalse() {
        assertFalse(NumberUtils.isDigits("12a45"));
    }

    // New tests added for better coverage

    // Test createNumber with explicit + prefix
    @Test
    public void testCreateNumber_plusPrefix_returnsInteger() {
        Number result = NumberUtils.createNumber("+123");
        assertNotNull(result);
        assertTrue("Should be an Integer", result instanceof Integer);
        assertEquals(123, result.intValue());
    }

    // Test createNumber with negative integer
    @Test
    public void testCreateNumber_negativeInteger_returnsInteger() {
        Number result = NumberUtils.createNumber("-123");
        assertNotNull(result);
        assertTrue("Should be an Integer", result instanceof Integer);
        assertEquals(-123, result.intValue());
    }

    // Test createNumber with hex float suffix
    @Test
    public void testCreateNumber_hexFloatSuffix_returnsFloat() {
        Number result = NumberUtils.createNumber("0x1.0p2f");
        assertNotNull(result);
        assertTrue("Should be a Float", result instanceof Float);
        assertEquals(4.0f, result.floatValue(), 0.0001);
    }

    // Test createNumber with hex double suffix
    @Test
    public void testCreateNumber_hexDoubleSuffix_returnsDouble() {
        Number result = NumberUtils.createNumber("0x1.0p2d");
        assertNotNull(result);
        assertTrue("Should be a Double", result instanceof Double);
        assertEquals(4.0d, result.doubleValue(), 0.0001);
    }

    // Test createNumber with octal-like string (leading zero)
    @Test
    public void testCreateNumber_octalString_returnsInteger() {
        Number result = NumberUtils.createNumber("010");
        assertNotNull(result);
        assertTrue("Should be an Integer", result instanceof Integer);
        assertEquals(8, result.intValue());
    }

    // Test createNumber with invalid hex string
    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_invalidHex_throwsNumberFormatException() {
        NumberUtils.createNumber("0x");
    }

    // Test createNumber with string containing both L and decimal
    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_illegalLongFormat_throwsNumberFormatException() {
        NumberUtils.createNumber("1.2L");
    }

    // Test createNumber with string containing both f and decimal
    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_illegalFloatFormat_throwsNumberFormatException() {
        NumberUtils.createNumber("1.2fL");
    }

    // Test createNumber with string containing both d and integer
    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_illegalDoubleFormat_throwsNumberFormatException() {
        NumberUtils.createNumber("1d");
    }

    // Test createNumber with string containing both e and L suffix
    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_illegalExponentFormat_throwsNumberFormatException() {
        NumberUtils.createNumber("1e2L");
    }

    // Test createNumber with string ending with period
    @Test
    public void testCreateNumber_trailingPeriod_returnsDouble() {
        Number result = NumberUtils.createNumber("123.");
        assertNotNull(result);
        assertTrue("Should be a Double", result instanceof Double);
        assertEquals(123.0, result.doubleValue(), 0.0001);
    }

    // Test createNumber with string starting with period
    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_startingPeriod_throwsNumberFormatException() {
        NumberUtils.createNumber(".123");
    }

    // Test createNumber with negative exponent notation
    @Test
    public void testCreateNumber_negativeExponent_returnsDouble() {
        Number result = NumberUtils.createNumber("1e-2");
        assertNotNull(result);
        assertTrue("Should be a Double", result instanceof Double);
        assertEquals(0.01, result.doubleValue(), 0.0001);
    }

    // Test createNumber with uppercase F suffix
    @Test
    public void testCreateNumber_uppercaseFloatSuffix_returnsFloat() {
        Number result = NumberUtils.createNumber("1.5F");
        assertNotNull(result);
        assertTrue("Should be a Float", result instanceof Float);
        assertEquals(1.5f, result.floatValue(), 0.0001);
    }

    // Test createNumber with uppercase D suffix
    @Test
    public void testCreateNumber_uppercaseDoubleSuffix_returnsDouble() {
        Number result = NumberUtils.createNumber("1.5D");
        assertNotNull(result);
        assertTrue("Should be a Double", result instanceof Double);
        assertEquals(1.5d, result.doubleValue(), 0.0001);
    }

    // Test createNumber with uppercase L suffix
    @Test
    public void testCreateNumber_uppercaseLongSuffix_returnsLong() {
        Number result = NumberUtils.createNumber("123L");
        assertNotNull(result);
        assertTrue("Should be a Long", result instanceof Long);
        assertEquals(123L, result.longValue());
    }

    // Test createNumber with zero value
    @Test
    public void testCreateNumber_zero_returnsInteger() {
        Number result = NumberUtils.createNumber("0");
        assertNotNull(result);
        assertTrue("Should be an Integer", result instanceof Integer);
        assertEquals(0, result.intValue());
    }

    // Test createNumber with negative zero
    @Test
    public void testCreateNumber_negativeZero_returnsInteger() {
        Number result = NumberUtils.createNumber("-0");
        assertNotNull(result);
        assertTrue("Should be an Integer", result instanceof Integer);
        assertEquals(0, result.intValue());
    }

    // Test isNumber with plus sign
    @Test
    public void testIsNumber_plusSign_returnsTrue() {
        assertTrue("isNumber(\"+123\") should return true", NumberUtils.isNumber("+123"));
    }

    // Test isNumber with negative sign
    @Test
    public void testIsNumber_negativeSign_returnsTrue() {
        assertTrue("isNumber(\"-123\") should return true", NumberUtils.isNumber("-123"));
    }

    // Test isNumber with leading zero
    @Test
    public void testIsNumber_leadingZero_returnsTrue() {
        assertTrue("isNumber(\"0123\") should return true", NumberUtils.isNumber("0123"));
    }

    // Test isNumber with octal prefix
    @Test
    public void testIsNumber_octalPrefix_returnsTrue() {
        assertTrue("isNumber(\"010\") should return true", NumberUtils.isNumber("010"));
    }

    // Test isNumber with invalid octal prefix
    @Test
    public void testIsNumber_invalidOctalPrefix_returnsFalse() {
        assertFalse("isNumber(\"018\") should return false", NumberUtils.isNumber("018"));
    }

    // Test isNumber with float suffix and decimal
    @Test
    public void testIsNumber_floatSuffixDecimal_returnsTrue() {
        assertTrue("isNumber(\"1.5f\") should return true", NumberUtils.isNumber("1.5f"));
    }

    // Test isNumber with float suffix but no decimal
    @Test
    public void testIsNumber_floatSuffixNoDecimal_returnsTrue() {
        assertTrue("isNumber(\"1f\") should return true", NumberUtils.isNumber("1f"));
    }

    // Test isNumber with double exponent
    @Test
    public void testIsNumber_doubleExponent_returnsTrue() {
        assertTrue("isNumber(\"1.5e2\") should return true", NumberUtils.isNumber("1.5e2"));
    }

    // Test isNumber with invalid exponent format
    @Test
    public void testIsNumber_invalidExponent_returnsFalse() {
        assertFalse("isNumber(\"1e\") should return false", NumberUtils.isNumber("1e"));
    }

    // Test isNumber with invalid double suffix
    @Test
    public void testIsNumber_invalidDoubleSuffix_returnsFalse() {
        assertFalse("isNumber(\"1.2dL\") should return false", NumberUtils.isNumber("1.2dL"));
    }

    // Test isNumber with negative hex
    @Test
    public void testIsNumber_negativeHex_returnsTrue() {
        assertTrue("isNumber(\"-0x10\") should return true", NumberUtils.isNumber("-0x10"));
    }

    // Test isNumber with positive hex
    @Test
    public void testIsNumber_positiveHex_returnsTrue() {
        assertTrue("isNumber(\"+0x10\") should return true", NumberUtils.isNumber("+0x10"));
    }

    // Test isNumber with hex prefix but invalid
    @Test
    public void testIsNumber_validHexOnly_returnsFalse() {
        assertFalse("isNumber(\"0xG\") should return false", NumberUtils.isNumber("0xG"));
    }

    // Test isNumber with hex notation but no prefix
    @Test
    public void testIsNumber_hexNoPrefix_returnsFalse() {
        assertFalse("isNumber(\"FF\") should return false", NumberUtils.isNumber("FF"));
    }

    // Test toInt with invalid string returns default
    @Test
    public void testToInt_invalidString_returnsDefault() {
        assertEquals(0, NumberUtils.toInt("abc", 0));
    }

    // Test toInt with default value
    @Test
    public void testToInt_invalidStringWithDefault_returnsDefault() {
        assertEquals(42, NumberUtils.toInt("abc", 42));
    }

    // Test toInt with valid string and default
    @Test
    public void testToInt_validStringWithDefault_returnsParsedValue() {
        assertEquals(7, NumberUtils.toInt("7", 42));
    }

    // Test toLong with default value
    @Test
    public void testToLong_invalidStringWithDefault_returnsDefault() {
        assertEquals(99L, NumberUtils.toLong("abc", 99L));
    }

    // Test toFloat with default value
    @Test
    public void testToFloat_invalidStringWithDefault_returnsDefault() {
        assertEquals(3.14f, NumberUtils.toFloat("abc", 3.14f), 0.001);
    }

    // Test toDouble with default value
    @Test
    public void testToDouble_invalidStringWithDefault_returnsDefault() {
        assertEquals(2.71d, NumberUtils.toDouble("abc", 2.71d), 0.001);
    }

    // Test min with null array
    @Test(expected = NullPointerException.class)
    public void testMin_nullArray_throwsNullPointerException() {
        NumberUtils.min((long[]) null);
    }

    // Test min with empty array
    @Test(expected = IllegalArgumentException.class)
    public void testMin_emptyArray_throwsIllegalArgumentException() {
        NumberUtils.min(new long[0]);
    }

    // Test max with empty array
    @Test(expected = IllegalArgumentException.class)
    public void testMax_emptyArray_throwsIllegalArgumentException() {
        NumberUtils.max(new int[0]);
    }

    // Test min with single element
    @Test
    public void testMin_singleElement_returnsElement() {
        assertEquals(7L, NumberUtils.min(new long[]{7L}));
    }

    // Test max with single element
    @Test
    public void testMax_singleElement_returnsElement() {
        assertEquals(7, NumberUtils.max(new int[]{7}));
    }

    // Test min short
    @Test
    public void testMin_shortArray_returnsMin() {
        short[] array = {5, 2, 8, 1, 9};
        assertEquals(1, NumberUtils.min(array));
    }

    // Test max short
    @Test
    public void testMax_shortArray_returnsMax() {
        short[] array = {5, 2, 8, 1, 9};
        assertEquals(9, NumberUtils.max(array));
    }

    // Test min byte
    @Test
    public void testMin_byteArray_returnsMin() {
        byte[] array = {5, 2, 8, 1, 9};
        assertEquals(1, NumberUtils.min(array));
    }

    // Test max byte
    @Test
    public void testMax_byteArray_returnsMax() {
        byte[] array = {5, 2, 8, 1, 9};
        assertEquals(9, NumberUtils.max(array));
    }

    // Test min double
    @Test
    public void testMin_doubleArray_returnsMin() {
        double[] array = {5.5, 2.2, 8.8, 1.1, 9.9};
        assertEquals(1.1, NumberUtils.min(array), 0.001);
    }

    // Test max double
    @Test
    public void testMax_doubleArray_returnsMax() {
        double[] array = {5.5, 2.2, 8.8, 1.1, 9.9};
        assertEquals(9.9, NumberUtils.max(array), 0.001);
    }

    // Test min float
    @Test
    public void testMin_floatArray_returnsMin() {
        float[] array = {5.5f, 2.2f, 8.8f, 1.1f, 9.9f};
        assertEquals(1.1f, NumberUtils.min(array), 0.001);
    }

    // Test max float
    @Test
    public void testMax_floatArray_returnsMax() {
        float[] array = {5.5f, 2.2f, 8.8f, 1.1f, 9.9f};
        assertEquals(9.9f, NumberUtils.max(array), 0.001);
    }

    // Test min with three shorts
    @Test
    public void testMin_threeShorts_returnsMin() {
        assertEquals(1, NumberUtils.min((short)5, (short)1, (short)3));
    }

    // Test max with three shorts
    @Test
    public void testMax_threeShorts_returnsMax() {
        assertEquals(5, NumberUtils.max((short)5, (short)1, (short)3));
    }

    // Test min with three bytes
    @Test
    public void testMin_threeBytes_returnsMin() {
        assertEquals(1, NumberUtils.min((byte)5, (byte)1, (byte)3));
    }

    // Test max with three bytes
    @Test
    public void testMax_threeBytes_returnsMax() {
        assertEquals(5, NumberUtils.max((byte)5, (byte)1, (byte)3));
    }

    // Test min with three longs
    @Test
    public void testMin_threeLongs_returnsMin() {
        assertEquals(1L, NumberUtils.min(5L, 1L, 3L));
    }

    // Test max with three longs
    @Test
    public void testMax_threeLongs_returnsMax() {
        assertEquals(5L, NumberUtils.max(5L, 1L, 3L));
    }

    // Test min with three doubles
    @Test
    public void testMin_threeDoubles_returnsMin() {
        assertEquals(1.1, NumberUtils.min(5.5, 1.1, 3.3), 0.001);
    }

    // Test max with three doubles
    @Test
    public void testMax_threeDoubles_returnsMax() {
        assertEquals(5.5, NumberUtils.max(5.5, 1.1, 3.3), 0.001);
    }

    // Test min with three floats
    @Test
    public void testMin_threeFloats_returnsMin() {
        assertEquals(1.1f, NumberUtils.min(5.5f, 1.1f, 3.3f), 0.001);
    }

    // Test max with three floats
    @Test
    public void testMax_threeFloats_returnsMax() {
        assertEquals(5.5f, NumberUtils.max(5.5f, 1.1f, 3.3f), 0.001);
    }

    // Test isDigits with empty string
    @Test
    public void testIsDigits_digitsWithLeadingZero_returnsTrue() {
        assertTrue(NumberUtils.isDigits("0123"));
    }

    // Test isDigits with whitespace
    @Test
    public void testIsDigits_withWhitespace_returnsFalse() {
        assertFalse(NumberUtils.isDigits(" 123"));
    }

    // Test isDigits with decimal point
    @Test
    public void testIsDigits_decimalPoint_returnsFalse() {
        assertFalse(NumberUtils.isDigits("12.3"));
    }
}