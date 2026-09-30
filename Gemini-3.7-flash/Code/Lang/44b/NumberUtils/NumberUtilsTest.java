package org.apache.commons.lang;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.junit.Test;
import static org.junit.Assert.*;

public class NumberUtilsTest {

    // Tests default constructor
    @Test
    public void testConstructor_default_instantiatesSuccessfully() {
        NumberUtils numUtils = new NumberUtils();
        assertNotNull(numUtils);
    }

    // Tests stringToInt with valid integer string
    @Test
    public void testStringToInt_validString_returnsInt() {
        assertEquals(123, NumberUtils.stringToInt("123"));
        assertEquals(-45, NumberUtils.stringToInt("-45"));
    }

    // Tests stringToInt with invalid string and default value fallback
    @Test
    public void testStringToInt_invalidString_returnsDefault() {
        assertEquals(0, NumberUtils.stringToInt("invalid"));
        assertEquals(10, NumberUtils.stringToInt(null, 10));
        assertEquals(5, NumberUtils.stringToInt("abc", 5));
    }

    // Tests createNumber with null input
    @Test
    public void testCreateNumber_nullInput_returnsNull() {
        assertNull(NumberUtils.createNumber(null));
    }

    // Tests createNumber with empty string
    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_emptyString_throwsException() {
        NumberUtils.createNumber("");
    }

    // Tests createNumber with invalid single character type qualifier (Lang-44 defect check)
    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_singleCharTypeQualifier_throwsException() {
        NumberUtils.createNumber("L");
    }

    // Tests createNumber with double negative prefix
    @Test
    public void testCreateNumber_doubleMinus_returnsNull() {
        assertNull(NumberUtils.createNumber("--123"));
    }

    // Tests createNumber with hexadecimal input
    @Test
    public void testCreateNumber_hexPrefix_returnsInteger() {
        assertEquals(Integer.valueOf(255), NumberUtils.createNumber("0xFF"));
        assertEquals(Integer.valueOf(-255), NumberUtils.createNumber("-0xFF"));
    }

    // Tests createNumber with various numeric formats and type suffixes
    @Test
    public void testCreateNumber_variousFormats_returnsCorrectNumberTypes() {
        assertEquals(Integer.valueOf(123), NumberUtils.createNumber("123"));
        assertEquals(Long.valueOf(1234567890123L), NumberUtils.createNumber("1234567890123"));
        assertEquals(new BigInteger("123456789012345678901234567890"), NumberUtils.createNumber("123456789012345678901234567890"));

        assertEquals(Long.valueOf(123L), NumberUtils.createNumber("123L"));
        assertEquals(Long.valueOf(-123L), NumberUtils.createNumber("-123l"));

        assertEquals(Float.valueOf(12.34f), NumberUtils.createNumber("12.34f"));
        assertEquals(Double.valueOf(12.34d), NumberUtils.createNumber("12.34d"));
        assertEquals(Float.valueOf(12.34f), NumberUtils.createNumber("12.34"));
        assertEquals(new BigDecimal("1.23456789012345678901234567890"), NumberUtils.createNumber("1.23456789012345678901234567890"));
    }

    // Tests createNumber with invalid format
    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_invalidFormat_throwsException() {
        NumberUtils.createNumber("12.34.56");
    }

    // Tests individual create methods
    @Test
    public void testCreateHelpers_validInput_returnsConvertedTypes() {
        assertEquals(Float.valueOf(3.14f), NumberUtils.createFloat("3.14"));
        assertEquals(Double.valueOf(3.14159), NumberUtils.createDouble("3.14159"));
        assertEquals(Integer.valueOf(42), NumberUtils.createInteger("42"));
        assertEquals(Long.valueOf(100L), NumberUtils.createLong("100"));
        assertEquals(new BigInteger("999999999999"), NumberUtils.createBigInteger("999999999999"));
        assertEquals(new BigDecimal("123.456"), NumberUtils.createBigDecimal("123.456"));
    }

    // Tests minimum and maximum methods for int and long
    @Test
    public void testMinMax_variousInputs_returnsExpectedExtremes() {
        assertEquals(1, NumberUtils.minimum(1, 2, 3));
        assertEquals(1, NumberUtils.minimum(3, 1, 2));
        assertEquals(1, NumberUtils.minimum(2, 3, 1));

        assertEquals(1L, NumberUtils.minimum(1L, 2L, 3L));
        assertEquals(1L, NumberUtils.minimum(3L, 1L, 2L));
        assertEquals(1L, NumberUtils.minimum(2L, 3L, 1L));

        assertEquals(3, NumberUtils.maximum(1, 2, 3));
        assertEquals(3, NumberUtils.maximum(3, 1, 2));
        assertEquals(3, NumberUtils.maximum(2, 3, 1));

        assertEquals(3L, NumberUtils.maximum(1L, 2L, 3L));
        assertEquals(3L, NumberUtils.maximum(3L, 1L, 2L));
        assertEquals(3L, NumberUtils.maximum(2L, 3L, 1L));
    }

    // Tests compare method for double values including special cases (-0.0, +0.0, NaN)
    @Test
    public void testCompare_doubleValues_returnsExpectedOrder() {
        assertTrue(NumberUtils.compare(1.0d, 2.0d) < 0);
        assertTrue(NumberUtils.compare(2.0d, 1.0d) > 0);
        assertEquals(0, NumberUtils.compare(1.5d, 1.5d));
        assertTrue(NumberUtils.compare(-0.0d, 0.0d) < 0);
        assertTrue(NumberUtils.compare(Double.NaN, Double.POSITIVE_INFINITY) > 0);
        assertEquals(0, NumberUtils.compare(Double.NaN, Double.NaN));
    }

    // Tests compare method for float values including special cases (-0.0, +0.0, NaN)
    @Test
    public void testCompare_floatValues_returnsExpectedOrder() {
        assertTrue(NumberUtils.compare(1.0f, 2.0f) < 0);
        assertTrue(NumberUtils.compare(2.0f, 1.0f) > 0);
        assertEquals(0, NumberUtils.compare(1.5f, 1.5f));
        assertTrue(NumberUtils.compare(-0.0f, 0.0f) < 0);
        assertTrue(NumberUtils.compare(Float.NaN, Float.POSITIVE_INFINITY) > 0);
        assertEquals(0, NumberUtils.compare(Float.NaN, Float.NaN));
    }

    // Tests isDigits method with various strings
    @Test
    public void testIsDigits_variousStrings_returnsCorrectBoolean() {
        assertFalse(NumberUtils.isDigits(null));
        assertFalse(NumberUtils.isDigits(""));
        assertFalse(NumberUtils.isDigits("12a34"));
        assertFalse(NumberUtils.isDigits("-123"));
        assertTrue(NumberUtils.isDigits("12345"));
    }

    // Tests isNumber method with valid and invalid inputs
    @Test
    public void testIsNumber_variousInputs_validatesCorrectly() {
        assertFalse(NumberUtils.isNumber(null));
        assertFalse(NumberUtils.isNumber(""));
        assertFalse(NumberUtils.isNumber("abc"));
        assertFalse(NumberUtils.isNumber("0x"));
        assertFalse(NumberUtils.isNumber("1.2.3"));
        assertFalse(NumberUtils.isNumber("1e2e3"));
        assertFalse(NumberUtils.isNumber("1e"));

        assertTrue(NumberUtils.isNumber("0x123"));
        assertTrue(NumberUtils.isNumber("-0xABCD"));
        assertTrue(NumberUtils.isNumber("12345"));
        assertTrue(NumberUtils.isNumber("-12345"));
        assertTrue(NumberUtils.isNumber("123.45"));
        assertTrue(NumberUtils.isNumber("-123.45"));
        assertTrue(NumberUtils.isNumber("1.23e-4"));
        assertTrue(NumberUtils.isNumber("12345L"));
        assertTrue(NumberUtils.isNumber("123.45f"));
        assertTrue(NumberUtils.isNumber("123.45d"));
    }
}