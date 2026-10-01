package org.apache.commons.lang;

import static org.junit.Assert.*;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * JUnit 4 test class for NumberUtils.
 */
public class NumberUtilsTest {

    // Tests stringToInt with valid numeric string
    @Test
    public void testStringToInt_validInput_returnsCorrectInt() {
        assertEquals(123, NumberUtils.stringToInt("123"));
    }

    // Tests stringToInt with invalid input, expects default 0
    @Test
    public void testStringToInt_invalidInput_returnsDefaultZero() {
        assertEquals(0, NumberUtils.stringToInt("abc"));
    }

    // Tests stringToInt with custom default value
    @Test
    public void testStringToInt_invalidInputWithDefault_returnsDefault() {
        assertEquals(42, NumberUtils.stringToInt("xyz", 42));
    }

    // Tests createNumber with null input
    @Test
    public void testCreateNumber_nullInput_returnsNull() {
        assertNull(NumberUtils.createNumber(null));
    }

    // Tests createNumber with empty string
    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_emptyString_throwsNumberFormatException() {
        NumberUtils.createNumber("");
    }

    // Tests createNumber with double leading hyphens (defect trigger)
    @Test
    public void testCreateNumber_doubleLeadingHyphens_returnsNull() {
        assertNull(NumberUtils.createNumber("--1.2"));
    }

    // Tests createNumber with hex prefix
    @Test
    public void testCreateNumber_hexPrefix_returnsInteger() {
        assertEquals(Integer.valueOf(255), NumberUtils.createNumber("0xFF"));
    }

    // Tests createNumber with negative hex prefix
    @Test
    public void testCreateNumber_negativeHexPrefix_returnsInteger() {
        assertEquals(Integer.valueOf(-255), NumberUtils.createNumber("-0xFF"));
    }

    // Tests createNumber with valid long suffix L
    @Test
    public void testCreateNumber_longSuffix_returnsLong() {
        assertEquals(Long.valueOf(123L), NumberUtils.createNumber("123L"));
    }

    // Tests createNumber with valid float suffix f
    @Test
    public void testCreateNumber_floatSuffix_returnsFloat() {
        assertEquals(Float.valueOf(1.5f), NumberUtils.createNumber("1.5f"));
    }

    // Tests createNumber with valid double suffix d
    @Test
    public void testCreateNumber_doubleSuffix_returnsDouble() {
        assertEquals(Double.valueOf(2.5d), NumberUtils.createNumber("2.5d"));
    }

    // Tests createNumber with integer without type qualifier
    @Test
    public void testCreateNumber_integerWithoutQualifier_returnsInteger() {
        assertEquals(Integer.valueOf(42), NumberUtils.createNumber("42"));
    }

    // Tests createNumber with decimal without type qualifier
    @Test
    public void testCreateNumber_decimalWithoutQualifier_returnsDouble() {
        assertEquals(Double.valueOf(3.14), NumberUtils.createNumber("3.14"));
    }

    // Tests createNumber with large integer that forces BigInteger
    @Test
    public void testCreateNumber_largeInteger_returnsBigInteger() {
        String bigVal = "999999999999999999999999999999";
        assertEquals(new BigInteger(bigVal), NumberUtils.createNumber(bigVal));
    }

    // Tests createNumber with scientific notation
    @Test
    public void testCreateNumber_scientificNotation_returnsDouble() {
        assertEquals(Double.valueOf(1e10), NumberUtils.createNumber("1e10"));
    }

    // Tests createNumber with negative number
    @Test
    public void testCreateNumber_negativeNumber_returnsInteger() {
        assertEquals(Integer.valueOf(-7), NumberUtils.createNumber("-7"));
    }

    // Tests isDigits with null input
    @Test
    public void testIsDigits_nullInput_returnsFalse() {
        assertFalse(NumberUtils.isDigits(null));
    }

    // Tests isDigits with empty string
    @Test
    public void testIsDigits_emptyString_returnsFalse() {
        assertFalse(NumberUtils.isDigits(""));
    }

    // Tests isDigits with valid digits
    @Test
    public void testIsDigits_validDigits_returnsTrue() {
        assertTrue(NumberUtils.isDigits("12345"));
    }

    // Tests isNumber with valid integer
    @Test
    public void testIsNumber_validInteger_returnsTrue() {
        assertTrue(NumberUtils.isNumber("123"));
    }

    // Tests isNumber with invalid input
    @Test
    public void testIsNumber_invalidInput_returnsFalse() {
        assertFalse(NumberUtils.isNumber("12a3"));
    }

    // Tests minimum with int values
    @Test
    public void testMinimum_threeInts_returnsSmallest() {
        assertEquals(1, NumberUtils.minimum(3, 1, 2));
    }

    // Tests maximum with int values
    @Test
    public void testMaximum_threeInts_returnsLargest() {
        assertEquals(9, NumberUtils.maximum(5, 9, 3));
    }

    // Tests compare double with NaN
    @Test
    public void testCompare_doubleNaNWithNaN_returnsZero() {
        assertEquals(0, NumberUtils.compare(Double.NaN, Double.NaN));
    }

    // Tests compare double with positive and negative zero
    @Test
    public void testCompare_doublePosZeroNegZero_returnsPositive() {
        assertTrue(NumberUtils.compare(0.0, -0.0) > 0);
    }

    // Tests compare float with NaN
    @Test
    public void testCompare_floatNaNWithNaN_returnsZero() {
        assertEquals(0, NumberUtils.compare(Float.NaN, Float.NaN));
    }

    // Tests compare float with positive and negative zero
    @Test
    public void testCompare_floatPosZeroNegZero_returnsPositive() {
        assertTrue(NumberUtils.compare(0.0f, -0.0f) > 0);
    }

    // Tests createNumber with invalid trailing character
    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_invalidTrailingChar_throwsNumberFormatException() {
        NumberUtils.createNumber("123x");
    }

    // Tests createNumber with exponent but no digits after E
    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_exponentWithoutDigits_throwsNumberFormatException() {
        NumberUtils.createNumber("1e");
    }

    // Tests createBigInteger
    @Test
    public void testCreateBigInteger_validInput_returnsBigInteger() {
        assertEquals(new BigInteger("123"), NumberUtils.createBigInteger("123"));
    }

    // Tests createBigDecimal
    @Test
    public void testCreateBigDecimal_validInput_returnsBigDecimal() {
        assertEquals(new BigDecimal("123.45"), NumberUtils.createBigDecimal("123.45"));
    }

    // Tests createFloat
    @Test
    public void testCreateFloat_validInput_returnsFloat() {
        assertEquals(Float.valueOf(1.5f), NumberUtils.createFloat("1.5"));
    }

    // Tests createDouble
    @Test
    public void testCreateDouble_validInput_returnsDouble() {
        assertEquals(Double.valueOf(2.5), NumberUtils.createDouble("2.5"));
    }

    // Tests createInteger
    @Test
    public void testCreateInteger_validInput_returnsInteger() {
        assertEquals(Integer.valueOf(42), NumberUtils.createInteger("42"));
    }

    // Tests createLong
    @Test
    public void testCreateLong_validInput_returnsLong() {
        assertEquals(Long.valueOf(123L), NumberUtils.createLong("123"));
    }

    // ============== Additional tests to improve coverage ==============

    // isNumber tests
    @Test
    public void testIsNumber_nullInput_returnsFalse() {
        assertFalse(NumberUtils.isNumber(null));
    }

    @Test
    public void testIsNumber_emptyString_returnsFalse() {
        assertFalse(NumberUtils.isNumber(""));
    }

    @Test
    public void testIsNumber_hexPrefix_returnsTrue() {
        assertTrue(NumberUtils.isNumber("0x1A"));
    }

    @Test
    public void testIsNumber_negativeHex_returnsTrue() {
        assertTrue(NumberUtils.isNumber("-0x1A"));
    }

    @Test
    public void testIsNumber_hexOnlyPrefix_returnsFalse() {
        assertFalse(NumberUtils.isNumber("0x"));
    }

    @Test
    public void testIsNumber_positiveSign_returnsTrue() {
        assertTrue(NumberUtils.isNumber("+123"));
    }

    @Test
    public void testIsNumber_negativeSignOnly_returnsFalse() {
        assertFalse(NumberUtils.isNumber("-"));
    }

    @Test
    public void testIsNumber_decimalPoint_returnsTrue() {
        assertTrue(NumberUtils.isNumber("3.14"));
    }

    @Test
    public void testIsNumber_trailingDot_returnsTrue() {
        assertTrue(NumberUtils.isNumber("1."));
    }

    @Test
    public void testIsNumber_leadingDot_returnsTrue() {
        assertTrue(NumberUtils.isNumber(".5"));
    }

    @Test
    public void testIsNumber_scientificNotation_returnsTrue() {
        assertTrue(NumberUtils.isNumber("1e10"));
    }

    @Test
    public void testIsNumber_negativeExponent_returnsTrue() {
        assertTrue(NumberUtils.isNumber("1e-10"));
    }

    @Test
    public void testIsNumber_exponentWithoutDigits_returnsFalse() {
        assertFalse(NumberUtils.isNumber("1e"));
    }

    @Test
    public void testIsNumber_nanString_returnsTrue() {
        assertTrue(NumberUtils.isNumber("NaN"));
    }

    @Test
    public void testIsNumber_infinityString_returnsTrue() {
        assertTrue(NumberUtils.isNumber("Infinity"));
    }

    @Test
    public void testIsNumber_negativeInfinity_returnsTrue() {
        assertTrue(NumberUtils.isNumber("-Infinity"));
    }

    @Test
    public void testIsNumber_withWhitespace_returnsFalse() {
        assertFalse(NumberUtils.isNumber(" 123"));
    }

    // isDigits tests
    @Test
    public void testIsDigits_negativeNumber_returnsFalse() {
        assertFalse(NumberUtils.isDigits("-1"));
    }

    @Test
    public void testIsDigits_decimalNumber_returnsFalse() {
        assertFalse(NumberUtils.isDigits("1.5"));
    }

    @Test
    public void testIsDigits_singleZero_returnsTrue() {
        assertTrue(NumberUtils.isDigits("0"));
    }

    @Test
    public void testIsDigits_leadingZeros_returnsTrue() {
        assertTrue(NumberUtils.isDigits("007"));
    }

    @Test
    public void testIsDigits_withSpace_returnsFalse() {
        assertFalse(NumberUtils.isDigits("12 3"));
    }

    // createNumber additional tests
    @Test
    public void testCreateNumber_hexLowercasePrefix_returnsInteger() {
        assertEquals(Integer.valueOf(255), NumberUtils.createNumber("0xff"));
    }

    @Test
    public void testCreateNumber_hexUppercaseX_returnsInteger() {
        assertEquals(Integer.valueOf(255), NumberUtils.createNumber("0XFF"));
    }

    @Test
    public void testCreateNumber_hexWithLSuffix_returnsLong() {
        assertEquals(Long.valueOf(255L), NumberUtils.createNumber("0xFFL"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_hexWithNoDigits_throwsNumberFormatException() {
        NumberUtils.createNumber("0x");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_hexWithInvalidChar_throwsNumberFormatException() {
        NumberUtils.createNumber("0xG");
    }

    @Test
    public void testCreateNumber_plusSign_returnsInteger() {
        assertEquals(Integer.valueOf(123), NumberUtils.createNumber("+123"));
    }

    @Test
    public void testCreateNumber_negativeZero_returnsDouble() {
        assertEquals(Double.valueOf(-0.0), NumberUtils.createNumber("-0"));
    }

    @Test
    public void testCreateNumber_zero_returnsInteger() {
        assertEquals(Integer.valueOf(0), NumberUtils.createNumber("0"));
    }

    @Test
    public void testCreateNumber_leadingZeros_returnsInteger() {
        assertEquals(Integer.valueOf(7), NumberUtils.createNumber("007"));
    }

    @Test
    public void testCreateNumber_trailingDot_returnsDouble() {
        assertEquals(Double.valueOf(1.0), NumberUtils.createNumber("1."));
    }

    @Test
    public void testCreateNumber_leadingDot_returnsDouble() {
        assertEquals(Double.valueOf(0.5), NumberUtils.createNumber(".5"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_invalidDecimalWithLSuffix_throwsNumberFormatException() {
        NumberUtils.createNumber("1.2L");
    }

    @Test
    public void testCreateNumber_nanString_returnsDoubleNaN() {
        assertEquals(Double.valueOf(Double.NaN), NumberUtils.createNumber("NaN"));
    }

    @Test
    public void testCreateNumber_infinityString_returnsPositiveInfinity() {
        assertEquals(Double.valueOf(Double.POSITIVE_INFINITY), NumberUtils.createNumber("Infinity"));
    }

    @Test
    public void testCreateNumber_negativeInfinity_returnsNegativeInfinity() {
        assertEquals(Double.valueOf(Double.NEGATIVE_INFINITY), NumberUtils.createNumber("-Infinity"));
    }

    @Test
    public void testCreateNumber_negativeExponent_returnsDouble() {
        assertEquals(Double.valueOf(0.1), NumberUtils.createNumber("1e-1"));
    }

    @Test
    public void testCreateNumber_uppercaseExponent_returnsDouble() {
        assertEquals(Double.valueOf(1e10), NumberUtils.createNumber("1E10"));
    }

    @Test
    public void testCreateNumber_scientificWithDSuffix_returnsDouble() {
        assertEquals(Double.valueOf(1e10), NumberUtils.createNumber("1e10d"));
    }

    @Test
    public void testCreateNumber_scientificWithFSuffix_returnsFloat() {
        assertEquals(Float.valueOf(1e10f), NumberUtils.createNumber("1e10f"));
    }

    @Test
    public void testCreateNumber_longLowercaseSuffix_returnsLong() {
        assertEquals(Long.valueOf(123L), NumberUtils.createNumber("123l"));
    }

    @Test
    public void testCreateNumber_floatUppercaseSuffix_returnsFloat() {
        assertEquals(Float.valueOf(1.5f), NumberUtils.createNumber("1.5F"));
    }

    @Test
    public void testCreateNumber_doubleUppercaseSuffix_returnsDouble() {
        assertEquals(Double.valueOf(2.5), NumberUtils.createNumber("2.5D"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_onlySign_throwsNumberFormatException() {
        NumberUtils.createNumber("-");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_twoDecimalPoints_throwsNumberFormatException() {
        NumberUtils.createNumber("1.2.3");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_stringWithSpaces_throwsNumberFormatException() {
        NumberUtils.createNumber(" 123");
    }

    // createBigInteger tests
    @Test
    public void testCreateBigInteger_nullInput_returnsNull() {
        assertNull(NumberUtils.createBigInteger(null));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateBigInteger_emptyString_throwsNumberFormatException() {
        NumberUtils.createBigInteger("");
    }

    @Test
    public void testCreateBigInteger_hexString_returnsBigInteger() {
        assertEquals(new BigInteger("255"), NumberUtils.createBigInteger("0xFF"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateBigInteger_invalidString_throwsNumberFormatException() {
        NumberUtils.createBigInteger("abc");
    }

    // createBigDecimal tests
    @Test
    public void testCreateBigDecimal_nullInput_returnsNull() {
        assertNull(NumberUtils.createBigDecimal(null));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateBigDecimal_emptyString_throwsNumberFormatException() {
        NumberUtils.createBigDecimal("");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateBigDecimal_invalidString_throwsNumberFormatException() {
        NumberUtils.createBigDecimal("abc");
    }

    @Test
    public void testCreateBigDecimal_scientificNotation_returnsBigDecimal() {
        assertEquals(new BigDecimal("1e10"), NumberUtils.createBigDecimal("1e10"));
    }

    // createFloat tests
    @Test
    public void testCreateFloat_nullInput_returnsNull() {
        assertNull(NumberUtils.createFloat(null));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateFloat_emptyString_throwsNumberFormatException() {
        NumberUtils.createFloat("");
    }

    @Test
    public void testCreateFloat_nanString_returnsFloatNaN() {
        assertEquals(Float.NaN, NumberUtils.createFloat("NaN"), 0.0f);
    }

    @Test
    public void testCreateFloat_infinityString_returnsPositiveInfinity() {
        assertEquals(Float.POSITIVE_INFINITY, NumberUtils.createFloat("Infinity"), 0.0f);
    }

    @Test
    public void testCreateFloat_negativeInfinity_returnsNegativeInfinity() {
        assertEquals(Float.NEGATIVE_INFINITY, NumberUtils.createFloat("-Infinity"), 0.0f);
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateFloat_invalidString_throwsNumberFormatException() {
        NumberUtils.createFloat("abc");
    }

    // createDouble tests
    @Test
    public void testCreateDouble_nullInput_returnsNull() {
        assertNull(NumberUtils.createDouble(null));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateDouble_emptyString_throwsNumberFormatException() {
        NumberUtils.createDouble("");
    }

    @Test
    public void testCreateDouble_nanString_returnsDoubleNaN() {
        assertEquals(Double.NaN, NumberUtils.createDouble("NaN"), 0.0);
    }

    @Test
    public void testCreateDouble_infinityString_returnsPositiveInfinity() {
        assertEquals(Double.POSITIVE_INFINITY, NumberUtils.createDouble("Infinity"), 0.0);
    }

    @Test
    public void testCreateDouble_negativeInfinity_returnsNegativeInfinity() {
        assertEquals(Double.NEGATIVE_INFINITY, NumberUtils.createDouble("-Infinity"), 0.0);
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateDouble_invalidString_throwsNumberFormatException() {
        NumberUtils.createDouble("abc");
    }

    // createInteger tests
    @Test
    public void testCreateInteger_nullInput_returnsNull() {
        assertNull(NumberUtils.createInteger(null));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateInteger_emptyString_throwsNumberFormatException() {
        NumberUtils.createInteger("");
    }

    @Test
    public void testCreateInteger_hexString_returnsInteger() {
        assertEquals(Integer.valueOf(255), NumberUtils.createInteger("0xFF"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateInteger_invalidString_throwsNumberFormatException() {
        NumberUtils.createInteger("abc");
    }

    // createLong tests
    @Test
    public void testCreateLong_nullInput_returnsNull() {
        assertNull(NumberUtils.createLong(null));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateLong_emptyString_throwsNumberFormatException() {
        NumberUtils.createLong("");
    }

    @Test
    public void testCreateLong_hexString_returnsLong() {
        assertEquals(Long.valueOf(255L), NumberUtils.createLong("0xFF"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateLong_invalidString_throwsNumberFormatException() {
        NumberUtils.createLong("abc");
    }

    // compare double additional tests
    @Test
    public void testCompare_doubleNormalValues_returnsCorrectOrder() {
        assertTrue(NumberUtils.compare(1.0, 2.0) < 0);
        assertTrue(NumberUtils.compare(2.0, 1.0) > 0);
        assertEquals(0, NumberUtils.compare(1.0, 1.0));
    }

    @Test
    public void testCompare_doubleNaNAgainstNormal_returnsGreater() {
        assertTrue(NumberUtils.compare(Double.NaN, 1.0) > 0);
        assertTrue(NumberUtils.compare(1.0, Double.NaN) < 0);
    }

    @Test
    public void testCompare_doublePosZeroVsPosZero_returnsZero() {
        assertEquals(0, NumberUtils.compare(0.0, 0.0));
    }

    @Test
    public void testCompare_doubleNegZeroVsNegZero_returnsZero() {
        assertEquals(0, NumberUtils.compare(-0.0, -0.0));
    }

    // compare float additional tests
    @Test
    public void testCompare_floatNormalValues_returnsCorrectOrder() {
        assertTrue(NumberUtils.compare(1.0f, 2.0f) < 0);
        assertTrue(NumberUtils.compare(2.0f, 1.0f) > 0);
        assertEquals(0, NumberUtils.compare(1.0f, 1.0f));
    }

    @Test
    public void testCompare_floatNaNAgainstNormal_returnsGreater() {
        assertTrue(NumberUtils.compare(Float.NaN, 1.0f) > 0);
        assertTrue(NumberUtils.compare(1.0f, Float.NaN) < 0);
    }

    @Test
    public void testCompare_floatPosZeroVsPosZero_returnsZero() {
        assertEquals(0, NumberUtils.compare(0.0f, 0.0f));
    }

    @Test
    public void testCompare_floatNegZeroVsNegZero_returnsZero() {
        assertEquals(0, NumberUtils.compare(-0.0f, -0.0f));
    }

    // minimum/maximum additional tests
    @Test
    public void testMinimum_threeIntsAllEqual_returnsEqual() {
        assertEquals(5, NumberUtils.minimum(5, 5, 5));
    }

    @Test
    public void testMinimum_threeIntsNegative_returnsSmallest() {
        assertEquals(-10, NumberUtils.minimum(-10, 0, 5));
    }

    @Test
    public void testMaximum_threeIntsAllEqual_returnsEqual() {
        assertEquals(5, NumberUtils.maximum(5, 5, 5));
    }

    @Test
    public void testMaximum_threeIntsNegative_returnsLargest() {
        assertEquals(-1, NumberUtils.maximum(-10, -5, -1));
    }

    // stringToInt additional tests
    @Test
    public void testStringToInt_nullInput_returnsDefaultZero() {
        assertEquals(0, NumberUtils.stringToInt(null));
    }

    @Test
    public void testStringToInt_emptyString_returnsDefaultZero() {
        assertEquals(0, NumberUtils.stringToInt(""));
    }

    @Test
    public void testStringToInt_negativeNumber_returnsNegativeInt() {
        assertEquals(-5, NumberUtils.stringToInt("-5"));
    }

    @Test
    public void testStringToInt_withCustomDefaultOnNull_returnsDefault() {
        assertEquals(10, NumberUtils.stringToInt(null, 10));
    }

    @Test
    public void testStringToInt_withCustomDefaultOnInvalid_returnsDefault() {
        assertEquals(10, NumberUtils.stringToInt("abc", 10));
    }
}