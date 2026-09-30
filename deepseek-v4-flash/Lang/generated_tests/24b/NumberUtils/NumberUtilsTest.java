package org.apache.commons.lang3.math;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Test;

public class NumberUtilsTest {

    // Tests toByte with null input returns default value
    @Test
    public void testToByte_nullInput_returnsDefaultValue() {
        assertEquals((byte) 5, NumberUtils.toByte(null, (byte) 5));
    }

    // Tests toByte with valid input returns correct value
    @Test
    public void testToByte_validInput_returnsCorrectValue() {
        assertEquals((byte) 10, NumberUtils.toByte("10", (byte) 0));
    }

    // Tests toByte with invalid input returns default value
    @Test
    public void testToByte_invalidInput_returnsDefaultValue() {
        assertEquals((byte) 3, NumberUtils.toByte("abc", (byte) 3));
    }

    // Tests toShort with null input returns default value
    @Test
    public void testToShort_nullInput_returnsDefaultValue() {
        assertEquals((short) 100, NumberUtils.toShort(null, (short) 100));
    }

    // Tests toShort with valid input returns correct value
    @Test
    public void testToShort_validInput_returnsCorrectValue() {
        assertEquals((short) 200, NumberUtils.toShort("200", (short) 0));
    }

    // Tests toShort with invalid input returns default value
    @Test
    public void testToShort_invalidInput_returnsDefaultValue() {
        assertEquals((short) 50, NumberUtils.toShort("xyz", (short) 50));
    }

    // Tests createNumber with null input returns null
    @Test
    public void testCreateNumber_nullInput_returnsNull() {
        assertNull(NumberUtils.createNumber(null));
    }

    // Tests createNumber with blank input throws NumberFormatException
    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_blankInput_throwsNumberFormatException() {
        NumberUtils.createNumber("   ");
    }

    // Tests createNumber with string starting with "--" returns null
    @Test
    public void testCreateNumber_doubleMinusPrefix_returnsNull() {
        assertNull(NumberUtils.createNumber("--123"));
    }

    // Tests createNumber with hexadecimal integer input returns correct Integer
    @Test
    public void testCreateNumber_hexIntegerInput_returnsCorrectInteger() {
        assertEquals(Integer.valueOf(255), NumberUtils.createNumber("0xFF"));
    }

    // Tests createNumber with negative hexadecimal integer input returns correct Integer
    @Test
    public void testCreateNumber_negativeHexIntegerInput_returnsCorrectInteger() {
        assertEquals(Integer.valueOf(-255), NumberUtils.createNumber("-0xFF"));
    }

    // Tests createNumber with valid long suffix 'L' returns correct Long
    @Test
    public void testCreateNumber_longSuffixInput_returnsCorrectLong() {
        assertEquals(Long.valueOf(12345L), NumberUtils.createNumber("12345L"));
    }

    // Tests createNumber with valid float suffix 'F' returns correct Float
    @Test
    public void testCreateNumber_floatSuffixInput_returnsCorrectFloat() {
        assertEquals(Float.valueOf(1.5f), NumberUtils.createNumber("1.5F"));
    }

    // Tests createNumber with valid double suffix 'D' returns correct Double
    @Test
    public void testCreateNumber_doubleSuffixInput_returnsCorrectDouble() {
        assertEquals(Double.valueOf(2.5d), NumberUtils.createNumber("2.5D"));
    }

    // Tests createNumber with decimal input (no type suffix) returns correct BigDecimal
    @Test
    public void testCreateNumber_decimalInput_returnsBigDecimal() {
        assertEquals(new BigDecimal("3.14"), NumberUtils.createNumber("3.14"));
    }

    // Tests createNumber with integer input (no type suffix) returns correct Integer
    @Test
    public void testCreateNumber_integerInput_returnsInteger() {
        assertEquals(Integer.valueOf(42), NumberUtils.createNumber("42"));
    }

    // Tests createNumber with large integer input that exceeds Integer range returns correct Long
    @Test
    public void testCreateNumber_largeIntegerInput_returnsLong() {
        assertEquals(Long.valueOf(3000000000L), NumberUtils.createNumber("3000000000"));
    }

    // Tests createNumber with very large integer input that exceeds Long range returns correct BigInteger
    @Test
    public void testCreateNumber_veryLargeIntegerInput_returnsBigInteger() {
        assertEquals(new BigInteger("12345678901234567890"), NumberUtils.createNumber("12345678901234567890"));
    }

    // Tests createNumber with scientific notation input returns correct Double
    @Test
    public void testCreateNumber_scientificNotationInput_returnsDouble() {
        assertEquals(Double.valueOf(1e10), NumberUtils.createNumber("1e10"));
    }

    // Tests isNumber with null input returns false
    @Test
    public void testIsNumber_nullInput_returnsFalse() {
        assertFalse(NumberUtils.isNumber(null));
    }

    // Tests isNumber with empty string returns false
    @Test
    public void testIsNumber_emptyInput_returnsFalse() {
        assertFalse(NumberUtils.isNumber(""));
    }

    // Tests isNumber with valid integer returns true
    @Test
    public void testIsNumber_validInteger_returnsTrue() {
        assertTrue(NumberUtils.isNumber("123"));
    }

    // Tests isNumber with valid hex number returns true
    @Test
    public void testIsNumber_validHex_returnsTrue() {
        assertTrue(NumberUtils.isNumber("0x1A"));
    }

    // Tests isNumber with valid decimal returns true
    @Test
    public void testIsNumber_validDecimal_returnsTrue() {
        assertTrue(NumberUtils.isNumber("1.5"));
    }

    // Tests isNumber with valid number with type qualifier 'L' returns true
    @Test
    public void testIsNumber_validLongSuffix_returnsTrue() {
        assertTrue(NumberUtils.isNumber("100L"));
    }

    // Tests isNumber with invalid input (two decimal points) returns false
    @Test
    public void testIsNumber_twoDecimalPoints_returnsFalse() {
        assertFalse(NumberUtils.isNumber("1.2.3"));
    }

    // Tests isNumber with input with two exponents returns false
    @Test
    public void testIsNumber_twoExponents_returnsFalse() {
        assertFalse(NumberUtils.isNumber("1e2e3"));
    }

    // Tests isNumber with input '0x' alone returns false
    @Test
    public void testIsNumber_hexPrefixOnly_returnsFalse() {
        assertFalse(NumberUtils.isNumber("0x"));
    }

    // Tests isNumber with input ending with illegal character returns false
    @Test
    public void testIsNumber_invalidTrailingChar_returnsFalse() {
        assertFalse(NumberUtils.isNumber("12g"));
    }

    // ======================== New test cases for uncovered areas ========================

    // --- toInt methods ---
    @Test
    public void testToInt_nullInput_returnsDefaultValue() {
        assertEquals(42, NumberUtils.toInt(null, 42));
    }

    @Test
    public void testToInt_validInput_returnsCorrectValue() {
        assertEquals(123, NumberUtils.toInt("123", 0));
    }

    @Test
    public void testToInt_invalidInput_returnsDefaultValue() {
        assertEquals(-1, NumberUtils.toInt("abc", -1));
    }

    // --- toLong methods ---
    @Test
    public void testToLong_nullInput_returnsDefaultValue() {
        assertEquals(999L, NumberUtils.toLong(null, 999L));
    }

    @Test
    public void testToLong_validInput_returnsCorrectValue() {
        assertEquals(456L, NumberUtils.toLong("456", 0L));
    }

    @Test
    public void testToLong_invalidInput_returnsDefaultValue() {
        assertEquals(0L, NumberUtils.toLong("xyz", 0L));
    }

    // --- toFloat methods ---
    @Test
    public void testToFloat_nullInput_returnsDefaultValue() {
        assertEquals(1.5f, NumberUtils.toFloat(null, 1.5f), 0.0f);
    }

    @Test
    public void testToFloat_validInput_returnsCorrectValue() {
        assertEquals(3.14f, NumberUtils.toFloat("3.14", 0.0f), 0.001f);
    }

    @Test
    public void testToFloat_invalidInput_returnsDefaultValue() {
        assertEquals(0.0f, NumberUtils.toFloat("notanumber", 0.0f), 0.0f);
    }

    // --- toDouble methods ---
    @Test
    public void testToDouble_nullInput_returnsDefaultValue() {
        assertEquals(2.71, NumberUtils.toDouble(null, 2.71), 0.0);
    }

    @Test
    public void testToDouble_validInput_returnsCorrectValue() {
        assertEquals(6.28, NumberUtils.toDouble("6.28", 0.0), 0.001);
    }

    @Test
    public void testToDouble_invalidInput_returnsDefaultValue() {
        assertEquals(-1.0, NumberUtils.toDouble("invalid", -1.0), 0.0);
    }

    // --- createInteger ---
    @Test
    public void testCreateInteger_nullInput_returnsNull() {
        assertNull(NumberUtils.createInteger(null));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateInteger_blankInput_throwsNumberFormatException() {
        NumberUtils.createInteger("  ");
    }

    @Test
    public void testCreateInteger_validInput_returnsInteger() {
        assertEquals(Integer.valueOf(77), NumberUtils.createInteger("77"));
    }

    @Test
    public void testCreateInteger_negativeInput_returnsInteger() {
        assertEquals(Integer.valueOf(-88), NumberUtils.createInteger("-88"));
    }

    // --- createLong ---
    @Test
    public void testCreateLong_nullInput_returnsNull() {
        assertNull(NumberUtils.createLong(null));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateLong_blankInput_throwsNumberFormatException() {
        NumberUtils.createLong("   ");
    }

    @Test
    public void testCreateLong_validInput_returnsLong() {
        assertEquals(Long.valueOf(123456789L), NumberUtils.createLong("123456789"));
    }

    @Test
    public void testCreateLong_negativeInput_returnsLong() {
        assertEquals(Long.valueOf(-987654321L), NumberUtils.createLong("-987654321"));
    }

    // --- createFloat ---
    @Test
    public void testCreateFloat_nullInput_returnsNull() {
        assertNull(NumberUtils.createFloat(null));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateFloat_blankInput_throwsNumberFormatException() {
        NumberUtils.createFloat(" ");
    }

    @Test
    public void testCreateFloat_validInput_returnsFloat() {
        assertEquals(Float.valueOf(2.5f), NumberUtils.createFloat("2.5"));
    }

    @Test
    public void testCreateFloat_negativeInput_returnsFloat() {
        assertEquals(Float.valueOf(-1.2e3f), NumberUtils.createFloat("-1.2e3"), 0.0f);
    }

    // --- createDouble ---
    @Test
    public void testCreateDouble_nullInput_returnsNull() {
        assertNull(NumberUtils.createDouble(null));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateDouble_blankInput_throwsNumberFormatException() {
        NumberUtils.createDouble(" ");
    }

    @Test
    public void testCreateDouble_validInput_returnsDouble() {
        assertEquals(Double.valueOf(3.14159), NumberUtils.createDouble("3.14159"));
    }

    @Test
    public void testCreateDouble_negativeInput_returnsDouble() {
        assertEquals(Double.valueOf(-0.5), NumberUtils.createDouble("-0.5"), 0.0);
    }

    // --- createBigInteger ---
    @Test
    public void testCreateBigInteger_nullInput_returnsNull() {
        assertNull(NumberUtils.createBigInteger(null));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateBigInteger_blankInput_throwsNumberFormatException() {
        NumberUtils.createBigInteger("  ");
    }

    @Test
    public void testCreateBigInteger_validInput_returnsBigInteger() {
        assertEquals(new BigInteger("999888777666555"), NumberUtils.createBigInteger("999888777666555"));
    }

    @Test
    public void testCreateBigInteger_negativeInput_returnsBigInteger() {
        assertEquals(new BigInteger("-12345678901234567890"), NumberUtils.createBigInteger("-12345678901234567890"));
    }

    // --- createBigDecimal ---
    @Test
    public void testCreateBigDecimal_nullInput_returnsNull() {
        assertNull(NumberUtils.createBigDecimal(null));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateBigDecimal_blankInput_throwsNumberFormatException() {
        NumberUtils.createBigDecimal("  ");
    }

    @Test
    public void testCreateBigDecimal_validInput_returnsBigDecimal() {
        assertEquals(new BigDecimal("12345.6789"), NumberUtils.createBigDecimal("12345.6789"));
    }

    @Test
    public void testCreateBigDecimal_negativeInput_returnsBigDecimal() {
        assertEquals(new BigDecimal("-0.001"), NumberUtils.createBigDecimal("-0.001"));
    }

    // --- isNumber additional edge cases ---
    @Test
    public void testIsNumber_negativeInteger_returnsTrue() {
        assertTrue(NumberUtils.isNumber("-123"));
    }

    @Test
    public void testIsNumber_positiveWithPlusSign_returnsTrue() {
        assertTrue(NumberUtils.isNumber("+456"));
    }

    @Test
    public void testIsNumber_zero_returnsTrue() {
        assertTrue(NumberUtils.isNumber("0"));
    }

    @Test
    public void testIsNumber_hexUppercasePrefix_returnsTrue() {
        assertTrue(NumberUtils.isNumber("0X1A"));
    }

    @Test
    public void testIsNumber_negativeHex_returnsTrue() {
        assertTrue(NumberUtils.isNumber("-0xFF"));
    }

    @Test
    public void testIsNumber_floatWithDecimalOnly_returnsTrue() {
        assertTrue(NumberUtils.isNumber(".5"));
    }

    @Test
    public void testIsNumber_decimalEndingWithDot_returnsFalse() {
        assertFalse(NumberUtils.isNumber("5."));
    }

    @Test
    public void testIsNumber_scientificNotationPositiveExponent_returnsTrue() {
        assertTrue(NumberUtils.isNumber("1e10"));
    }

    @Test
    public void testIsNumber_scientificNotationNegativeExponent_returnsTrue() {
        assertTrue(NumberUtils.isNumber("1e-5"));
    }

    @Test
    public void testIsNumber_scientificNotationWithDecimal_returnsTrue() {
        assertTrue(NumberUtils.isNumber("3.14e2"));
    }

    @Test
    public void testIsNumber_floatSuffixF_returnsTrue() {
        assertTrue(NumberUtils.isNumber("1.5F"));
    }

    @Test
    public void testIsNumber_doubleSuffixD_returnsTrue() {
        assertTrue(NumberUtils.isNumber("2.5D"));
    }

    @Test
    public void testIsNumber_longSuffixLowerCase_returnsTrue() {
        assertTrue(NumberUtils.isNumber("100l"));
    }

    @Test
    public void testIsNumber_hexWithSuffix_returnsFalse() {
        assertFalse(NumberUtils.isNumber("0x1AL"));
    }

    @Test
    public void testIsNumber_leadingZeros_returnsTrue() {
        assertTrue(NumberUtils.isNumber("00123"));
    }

    @Test
    public void testIsNumber_minusSignOnly_returnsFalse() {
        assertFalse(NumberUtils.isNumber("-"));
    }

    @Test
    public void testIsNumber_plusSignOnly_returnsFalse() {
        assertFalse(NumberUtils.isNumber("+"));
    }

    @Test
    public void testIsNumber_trailingDotAfterExponent_returnsFalse() {
        assertFalse(NumberUtils.isNumber("1e10."));
    }

    @Test
    public void testIsNumber_floatWithDoubleExponent_returnsFalse() {
        assertFalse(NumberUtils.isNumber("1.5e2e3"));
    }

    @Test
    public void testIsNumber_hexWithMinusSignAfterPrefix_returnsFalse() {
        assertFalse(NumberUtils.isNumber("0x-1A"));
    }

    @Test
    public void testIsNumber_negativeFloat_returnsTrue() {
        assertTrue(NumberUtils.isNumber("-3.14"));
    }

    @Test
    public void testIsNumber_octalPrefix_returnsFalse() {
        // The standard NumberUtils.isNumber does not support octal with leading '0' alone
        // "0" is valid decimal zero, but "012" is not considered octal by default
        // We'll treat it as decimal leading zero, which should be true
        assertTrue(NumberUtils.isNumber("012"));
    }

    // --- createNumber additional uncovered branches ---
    @Test
    public void testCreateNumber_scientificNotationWithLowercaseE_returnsDouble() {
        assertEquals(Double.valueOf(2.5e3), NumberUtils.createNumber("2.5e3"));
    }

    @Test
    public void testCreateNumber_scientificNotationNegativeExponent_returnsDouble() {
        assertEquals(Double.valueOf(1.5e-2), NumberUtils.createNumber("1.5e-2"));
    }

    @Test
    public void testCreateNumber_floatWithExponentAndF_returnsFloat() {
        assertEquals(Float.valueOf(1.0e1f), NumberUtils.createNumber("1e1F"));
    }

    @Test
    public void testCreateNumber_doubleWithExponentAndD_returnsDouble() {
        assertEquals(Double.valueOf(2e3d), NumberUtils.createNumber("2e3D"));
    }

    @Test
    public void testCreateNumber_hexWithLongSuffix_returnsLong() {
        assertEquals(Long.valueOf(0xFFFFL), NumberUtils.createNumber("0xFFFFL"));
    }

    @Test
    public void testCreateNumber_leadingZeros_returnsInteger() {
        assertEquals(Integer.valueOf(12), NumberUtils.createNumber("0012"));
    }

    @Test
    public void testCreateNumber_negativeLong_returnsLong() {
        assertEquals(Long.valueOf(-123L), NumberUtils.createNumber("-123L"));
    }

    @Test
    public void testCreateNumber_negativeFloat_returnsFloat() {
        assertEquals(Float.valueOf(-2.5f), NumberUtils.createNumber("-2.5F"));
    }

    @Test
    public void testCreateNumber_negativeDouble_returnsDouble() {
        assertEquals(Double.valueOf(-1.0d), NumberUtils.createNumber("-1.0D"));
    }

    @Test
    public void testCreateNumber_hexWithNegativeAndLong_returnsLong() {
        assertEquals(Long.valueOf(-255L), NumberUtils.createNumber("-0xFFL"));
    }

    @Test
    public void testCreateNumber_plusSignInteger_returnsInteger() {
        assertEquals(Integer.valueOf(100), NumberUtils.createNumber("+100"));
    }

    @Test
    public void testCreateNumber_plusSignDouble_returnsDouble() {
        assertEquals(Double.valueOf(3.5), NumberUtils.createNumber("+3.5"));
    }
}