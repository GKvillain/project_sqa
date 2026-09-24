package com.fasterxml.jackson.core.io;

import java.math.BigDecimal;
import org.junit.Test;
import static org.junit.Assert.*;

public class NumberInputTest {

    // Tests parsing integer from char array with different lengths (1 to 9 digits)
    @Test
    public void testParseIntCharArray_validLengths_returnsExpectedInt() {
        char[] chars = "123456789".toCharArray();
        assertEquals(1, NumberInput.parseInt(chars, 0, 1));
        assertEquals(12, NumberInput.parseInt(chars, 0, 2));
        assertEquals(123, NumberInput.parseInt(chars, 0, 3));
        assertEquals(1234, NumberInput.parseInt(chars, 0, 4));
        assertEquals(12345, NumberInput.parseInt(chars, 0, 5));
        assertEquals(123456, NumberInput.parseInt(chars, 0, 6));
        assertEquals(1234567, NumberInput.parseInt(chars, 0, 7));
        assertEquals(12345678, NumberInput.parseInt(chars, 0, 8));
        assertEquals(123456789, NumberInput.parseInt(chars, 0, 9));
        assertEquals(456, NumberInput.parseInt(chars, 3, 3));
    }

    // Tests parseInt from string for positive, negative, and edge lengths
    @Test
    public void testParseIntString_validNumbers_returnsParsedInt() {
        assertEquals(0, NumberInput.parseInt("0"));
        assertEquals(7, NumberInput.parseInt("7"));
        assertEquals(123, NumberInput.parseInt("123"));
        assertEquals(123456789, NumberInput.parseInt("123456789"));
        assertEquals(-1, NumberInput.parseInt("-1"));
        assertEquals(-123456789, NumberInput.parseInt("-123456789"));
    }

    // Tests parseInt from string falling back to JDK Integer.parseInt
    @Test
    public void testParseIntString_fallbackAndEdgeCases_returnsParsedInt() {
        assertEquals(1000000000, NumberInput.parseInt("1000000000")); // 10 digits
        assertEquals(-1000000000, NumberInput.parseInt("-1000000000"));
        assertEquals(Integer.MAX_VALUE, NumberInput.parseInt(String.valueOf(Integer.MAX_VALUE)));
        assertEquals(Integer.MIN_VALUE, NumberInput.parseInt(String.valueOf(Integer.MIN_VALUE)));
    }

    // Tests parseInt from string with invalid character triggers NumberFormatException
    @Test(expected = NumberFormatException.class)
    public void testParseIntString_invalidChar_throwsNumberFormatException() {
        NumberInput.parseInt("12a34");
    }

    // Tests parseInt from string with only minus sign triggers NumberFormatException
    @Test(expected = NumberFormatException.class)
    public void testParseIntString_onlyMinusSign_throwsNumberFormatException() {
        NumberInput.parseInt("-");
    }

    // Tests parseLong from char array (10 to 18 digits)
    @Test
    public void testParseLongCharArray_validDigits_returnsLong() {
        char[] chars = "123456789012345678".toCharArray();
        assertEquals(1234567890L, NumberInput.parseLong(chars, 0, 10));
        assertEquals(123456789012345678L, NumberInput.parseLong(chars, 0, 18));
        assertEquals(4567890123456L, NumberInput.parseLong(chars, 3, 13));
    }

    // Tests parseLong from String for short and long numbers
    @Test
    public void testParseLongString_shortAndLongInputs_returnsLong() {
        assertEquals(0L, NumberInput.parseLong("0"));
        assertEquals(123456789L, NumberInput.parseLong("123456789")); // <= 9 digits
        assertEquals(1234567890123456789L, NumberInput.parseLong("1234567890123456789"));
        assertEquals(-1234567890123456789L, NumberInput.parseLong("-1234567890123456789"));
    }

    // Tests inLongRange with char array for in-range and out-of-range values
    @Test
    public void testInLongRangeCharArray_boundaryValues_returnsCorrectBoolean() {
        char[] maxLong = String.valueOf(Long.MAX_VALUE).toCharArray();
        assertTrue(NumberInput.inLongRange(maxLong, 0, maxLong.length, false));
        
        char[] minLongNoSign = String.valueOf(Long.MIN_VALUE).substring(1).toCharArray();
        assertTrue(NumberInput.inLongRange(minLongNoSign, 0, minLongNoSign.length, true));

        char[] shorter = "999999999999999999".toCharArray(); // 18 chars (< 19)
        assertTrue(NumberInput.inLongRange(shorter, 0, shorter.length, false));

        char[] longer = "10000000000000000000".toCharArray(); // 20 chars (> 19)
        assertFalse(NumberInput.inLongRange(longer, 0, longer.length, false));

        char[] aboveMax = "9223372036854775808".toCharArray();
        assertFalse(NumberInput.inLongRange(aboveMax, 0, aboveMax.length, false));

        char[] belowMax = "9223372036854775806".toCharArray();
        assertTrue(NumberInput.inLongRange(belowMax, 0, belowMax.length, false));
    }

    // Tests inLongRange with String for in-range and out-of-range values
    @Test
    public void testInLongRangeString_boundaryValues_returnsCorrectBoolean() {
        assertTrue(NumberInput.inLongRange(String.valueOf(Long.MAX_VALUE), false));
        assertTrue(NumberInput.inLongRange(String.valueOf(Long.MIN_VALUE).substring(1), true));
        assertTrue(NumberInput.inLongRange("12345", false));
        assertFalse(NumberInput.inLongRange("12345678901234567890", false));
        assertFalse(NumberInput.inLongRange("9223372036854775808", false));
        assertTrue(NumberInput.inLongRange("9223372036854775806", false));
    }

    // Tests parseAsInt with normal, leading sign, decimal, null, empty and invalid values
    @Test
    public void testParseAsInt_variousInputs_returnsExpectedOrDefValue() {
        assertEquals(0, NumberInput.parseAsInt(null, 0));
        assertEquals(5, NumberInput.parseAsInt("", 5));
        assertEquals(5, NumberInput.parseAsInt("   ", 5));
        assertEquals(123, NumberInput.parseAsInt("123", 0));
        assertEquals(123, NumberInput.parseAsInt("+123", 0));
        assertEquals(-123, NumberInput.parseAsInt("-123", 0));
        assertEquals(12, NumberInput.parseAsInt("12.75", 0));
        assertEquals(-12, NumberInput.parseAsInt("-12.75", 0));
        assertEquals(99, NumberInput.parseAsInt("not_a_number", 99));
        assertEquals(99, NumberInput.parseAsInt("12.34.56", 99));
    }

    // Tests parseAsLong with normal, leading sign, decimal, null, empty and invalid values
    @Test
    public void testParseAsLong_variousInputs_returnsExpectedOrDefValue() {
        assertEquals(0L, NumberInput.parseAsLong(null, 0L));
        assertEquals(5L, NumberInput.parseAsLong("", 5L));
        assertEquals(5L, NumberInput.parseAsLong("   ", 5L));
        assertEquals(123456789012L, NumberInput.parseAsLong("123456789012", 0L));
        assertEquals(123456789012L, NumberInput.parseAsLong("+123456789012", 0L));
        assertEquals(-123456789012L, NumberInput.parseAsLong("-123456789012", 0L));
        assertEquals(12L, NumberInput.parseAsLong("12.75", 0L));
        assertEquals(-12L, NumberInput.parseAsLong("-12.75", 0L));
        assertEquals(99L, NumberInput.parseAsLong("not_a_number", 99L));
        assertEquals(99L, NumberInput.parseAsLong("12.34.56", 99L));
    }

    // Tests parseAsDouble with valid numbers, null, empty and invalid strings
    @Test
    public void testParseAsDouble_variousInputs_returnsExpectedOrDefValue() {
        assertEquals(0.0, NumberInput.parseAsDouble(null, 0.0), 0.0001);
        assertEquals(5.5, NumberInput.parseAsDouble("", 5.5), 0.0001);
        assertEquals(5.5, NumberInput.parseAsDouble("   ", 5.5), 0.0001);
        assertEquals(123.456, NumberInput.parseAsDouble("123.456", 0.0), 0.0001);
        assertEquals(-123.456, NumberInput.parseAsDouble("-123.456", 0.0), 0.0001);
        assertEquals(9.9, NumberInput.parseAsDouble("not_a_number", 9.9), 0.0001);
    }

    // Tests parseDouble with the nasty small double constant and standard doubles
    @Test
    public void testParseDouble_nastySmallDoubleAndStandard_returnsCorrectDouble() {
        assertEquals(Double.MIN_VALUE, NumberInput.parseDouble(NumberInput.NASTY_SMALL_DOUBLE), 0.0);
        assertEquals(123.456, NumberInput.parseDouble("123.456"), 0.0001);
        assertEquals(-0.5, NumberInput.parseDouble("-0.5"), 0.0001);
    }

    // Tests parseBigDecimal from String and char arrays
    @Test
    public void testParseBigDecimal_validInputs_returnsBigDecimal() {
        assertEquals(new BigDecimal("123.456"), NumberInput.parseBigDecimal("123.456"));
        
        char[] chars = "prefix123.456suffix".toCharArray();
        assertEquals(new BigDecimal("123.456"), NumberInput.parseBigDecimal(chars, 6, 7));
        
        char[] fullChars = "987.654".toCharArray();
        assertEquals(new BigDecimal("987.654"), NumberInput.parseBigDecimal(fullChars));
    }

    // Tests parseBigDecimal throws exception on invalid input
    @Test(expected = NumberFormatException.class)
    public void testParseBigDecimal_invalidInput_throwsNumberFormatException() {
        NumberInput.parseBigDecimal("invalid");
    }

    // Tests constructor instantiation
    @Test
    public void testConstructor() {
        assertNotNull(new NumberInput());
    }

    // Tests parseAsBoolean with various boolean representations and fallback defaults
    @Test
    public void testParseAsBoolean_variousInputs_returnsExpectedOrDefValue() {
        assertTrue(NumberInput.parseAsBoolean("true", false));
        assertFalse(NumberInput.parseAsBoolean("false", true));
        assertTrue(NumberInput.parseAsBoolean("  true  ", false));
        assertFalse(NumberInput.parseAsBoolean("  false  ", true));
        assertTrue(NumberInput.parseAsBoolean(null, true));
        assertFalse(NumberInput.parseAsBoolean(null, false));
        assertTrue(NumberInput.parseAsBoolean("", true));
        assertFalse(NumberInput.parseAsBoolean("", false));
        assertTrue(NumberInput.parseAsBoolean("   ", true));
        assertFalse(NumberInput.parseAsBoolean("   ", false));
        assertFalse(NumberInput.parseAsBoolean("TRUE", false));
        assertTrue(NumberInput.parseAsBoolean("TRUE", true));
        assertFalse(NumberInput.parseAsBoolean("unknown", false));
        assertTrue(NumberInput.parseAsBoolean("unknown", true));
    }

    // Tests parseLong from char array with short length (<= 9 digits)
    @Test
    public void testParseLongCharArray_shortLengths_returnsLong() {
        char[] chars = "123456789".toCharArray();
        assertEquals(1L, NumberInput.parseLong(chars, 0, 1));
        assertEquals(12345L, NumberInput.parseLong(chars, 0, 5));
        assertEquals(123456789L, NumberInput.parseLong(chars, 0, 9));
    }

    // Tests parseInt with String covering lengths 2 through 8 for both positive and negative values
    @Test
    public void testParseIntString_intermediateLengths() {
        assertEquals(12, NumberInput.parseInt("12"));
        assertEquals(-12, NumberInput.parseInt("-12"));
        assertEquals(1234, NumberInput.parseInt("1234"));
        assertEquals(-1234, NumberInput.parseInt("-1234"));
        assertEquals(12345, NumberInput.parseInt("12345"));
        assertEquals(-12345, NumberInput.parseInt("-12345"));
        assertEquals(123456, NumberInput.parseInt("123456"));
        assertEquals(-123456, NumberInput.parseInt("-123456"));
        assertEquals(1234567, NumberInput.parseInt("1234567"));
        assertEquals(-1234567, NumberInput.parseInt("-1234567"));
        assertEquals(12345678, NumberInput.parseInt("12345678"));
        assertEquals(-12345678, NumberInput.parseInt("-12345678"));
    }

    // Tests parseInt with String having invalid chars at various positions
    @Test(expected = NumberFormatException.class)
    public void testParseIntString_singleNonDigit_throwsException() {
        NumberInput.parseInt("a");
    }

    @Test(expected = NumberFormatException.class)
    public void testParseIntString_invalidSecondChar_throwsException() {
        NumberInput.parseInt("1a");
    }

    @Test(expected = NumberFormatException.class)
    public void testParseIntString_invalidThirdChar_throwsException() {
        NumberInput.parseInt("12a");
    }

    @Test(expected = NumberFormatException.class)
    public void testParseIntString_invalidFourthChar_throwsException() {
        NumberInput.parseInt("123a");
    }

    @Test(expected = NumberFormatException.class)
    public void testParseIntString_invalidFifthChar_throwsException() {
        NumberInput.parseInt("1234a");
    }

    @Test(expected = NumberFormatException.class)
    public void testParseIntString_invalidSixthChar_throwsException() {
        NumberInput.parseInt("12345a");
    }

    @Test(expected = NumberFormatException.class)
    public void testParseIntString_invalidSeventhChar_throwsException() {
        NumberInput.parseInt("123456a");
    }

    @Test(expected = NumberFormatException.class)
    public void testParseIntString_invalidEighthChar_throwsException() {
        NumberInput.parseInt("1234567a");
    }

    @Test(expected = NumberFormatException.class)
    public void testParseIntString_invalidNinthChar_throwsException() {
        NumberInput.parseInt("12345678a");
    }

    @Test(expected = NumberFormatException.class)
    public void testParseIntString_negativeWithInvalidSecondChar_throwsException() {
        NumberInput.parseInt("-a");
    }

    @Test(expected = NumberFormatException.class)
    public void testParseIntString_negativeWithInvalidThirdChar_throwsException() {
        NumberInput.parseInt("-1a");
    }

    @Test(expected = NumberFormatException.class)
    public void testParseIntString_negativeWithInvalidFourthChar_throwsException() {
        NumberInput.parseInt("-12a");
    }

    @Test(expected = NumberFormatException.class)
    public void testParseIntString_negativeWithInvalidFifthChar_throwsException() {
        NumberInput.parseInt("-123a");
    }

    @Test(expected = NumberFormatException.class)
    public void testParseIntString_negativeWithInvalidSixthChar_throwsException() {
        NumberInput.parseInt("-1234a");
    }

    @Test(expected = NumberFormatException.class)
    public void testParseIntString_negativeWithInvalidSeventhChar_throwsException() {
        NumberInput.parseInt("-12345a");
    }

    @Test(expected = NumberFormatException.class)
    public void testParseIntString_negativeWithInvalidEighthChar_throwsException() {
        NumberInput.parseInt("-123456a");
    }

    @Test(expected = NumberFormatException.class)
    public void testParseIntString_negativeWithInvalidNinthChar_throwsException() {
        NumberInput.parseInt("-1234567a");
    }

    @Test(expected = NumberFormatException.class)
    public void testParseIntString_negativeWithInvalidTenthChar_throwsException() {
        NumberInput.parseInt("-12345678a");
    }

    // Tests inLongRange comparisons where prefix differs early
    @Test
    public void testInLongRange_earlyDiffPrefix() {
        char[] smallerFirstDigit = "1000000000000000000".toCharArray();
        assertTrue(NumberInput.inLongRange(smallerFirstDigit, 0, 19, false));

        char[] largerFirstDigit = "9999999999999999999".toCharArray();
        assertFalse(NumberInput.inLongRange(largerFirstDigit, 0, 19, false));

        char[] minLongPlusOne = "9223372036854775809".toCharArray();
        assertFalse(NumberInput.inLongRange(minLongPlusOne, 0, 19, true));

        assertTrue(NumberInput.inLongRange("1000000000000000000", false));
        assertFalse(NumberInput.inLongRange("9999999999999999999", false));
        assertFalse(NumberInput.inLongRange("9223372036854775809", true));
    }

    // Tests parseAsInt and parseAsLong single plus/minus sign and decimal without leading int
    @Test
    public void testParseAsIntAndLong_signOnlyAndDecimalLeading() {
        assertEquals(10, NumberInput.parseAsInt("+", 10));
        assertEquals(10, NumberInput.parseAsInt("-", 10));
        assertEquals(10L, NumberInput.parseAsLong("+", 10L));
        assertEquals(10L, NumberInput.parseAsLong("-", 10L));

        assertEquals(12, NumberInput.parseAsInt("+12.75", 0));
        assertEquals(12L, NumberInput.parseAsLong("+12.75", 0L));

        assertEquals(0, NumberInput.parseAsInt(".5", 10));
        assertEquals(0L, NumberInput.parseAsLong(".5", 10L));
        assertEquals(0, NumberInput.parseAsInt("+.5", 10));
        assertEquals(0L, NumberInput.parseAsLong("+.5", 10L));
        assertEquals(0, NumberInput.parseAsInt("-.5", 10));
        assertEquals(0L, NumberInput.parseAsLong("-.5", 10L));
    }

    // Tests parseAsDouble with leading plus sign
    @Test
    public void testParseAsDouble_leadingPlus() {
        assertEquals(123.456, NumberInput.parseAsDouble("+123.456", 0.0), 0.0001);
        assertEquals(5.0, NumberInput.parseAsDouble("+", 5.0), 0.0001);
        assertEquals(5.0, NumberInput.parseAsDouble("-", 5.0), 0.0001);
    }

    // Tests parseBigDecimal char array throws NumberFormatException on invalid input
    @Test(expected = NumberFormatException.class)
    public void testParseBigDecimalCharArray_invalidInput_throwsException() {
        char[] invalid = "not_a_number".toCharArray();
        NumberInput.parseBigDecimal(invalid, 0, invalid.length);
    }
}