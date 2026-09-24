package org.apache.commons.lang;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.junit.Test;
import static org.junit.Assert.*;

public class NumberUtilsTest {

    // Tests constructor
    @Test
    public void testConstructor_default_instanceNotNull() {
        assertNotNull(new NumberUtils());
    }

    // Tests stringToInt with valid integer and default value fallback
    @Test
    public void testStringToInt_validAndInvalidInput_returnsExpectedInt() {
        assertEquals(123, NumberUtils.stringToInt("123"));
        assertEquals(0, NumberUtils.stringToInt("invalid"));
        assertEquals(0, NumberUtils.stringToInt(null));
        assertEquals(5, NumberUtils.stringToInt("invalid", 5));
        assertEquals(42, NumberUtils.stringToInt("42", 5));
    }

    // Tests createNumber with null, empty, and double negative prefix
    @Test
    public void testCreateNumber_nullAndSpecialPrefix_returnsExpected() {
        assertNull(NumberUtils.createNumber(null));
        assertNull(NumberUtils.createNumber("--123"));
    }

    // Tests createNumber with empty string throws NumberFormatException
    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_emptyString_throwsNumberFormatException() {
        NumberUtils.createNumber("");
    }

    // Tests createNumber with hexadecimal formats
    @Test
    public void testCreateNumber_hexadecimalString_returnsInteger() {
        assertEquals(Integer.valueOf(255), NumberUtils.createNumber("0xFF"));
        assertEquals(Integer.valueOf(-255), NumberUtils.createNumber("-0xFF"));
    }

    // Tests createNumber with standard Integer, Long, and BigInteger
    @Test
    public void testCreateNumber_validIntegers_returnsCorrectType() {
        assertEquals(Integer.valueOf(12345), NumberUtils.createNumber("12345"));
        assertEquals(Long.valueOf(1234567890123L), NumberUtils.createNumber("1234567890123"));
        assertEquals(new BigInteger("123456789012345678901234567890"), NumberUtils.createNumber("123456789012345678901234567890"));
    }

    // Tests createNumber with floating point representations
    @Test
    public void testCreateNumber_decimalAndExponent_returnsFloatOrDoubleOrBigDecimal() {
        assertEquals(Float.valueOf("1.23"), NumberUtils.createNumber("1.23"));
        assertEquals(Double.valueOf("1.2345678901234567"), NumberUtils.createNumber("1.2345678901234567"));
        assertEquals(new BigDecimal("1.23456789012345678901234567890"), NumberUtils.createNumber("1.23456789012345678901234567890"));
    }

    // Tests createNumber with type qualifiers ('l', 'L', 'f', 'F', 'd', 'D')
    @Test
    public void testCreateNumber_typeQualifiers_returnsCorrectType() {
        assertEquals(Long.valueOf(123), NumberUtils.createNumber("123L"));
        assertEquals(Long.valueOf(-123), NumberUtils.createNumber("-123l"));
        assertEquals(new BigInteger("123456789012345678901234567890"), NumberUtils.createNumber("123456789012345678901234567890L"));
        assertEquals(Float.valueOf(1.23f), NumberUtils.createNumber("1.23f"));
        assertEquals(Float.valueOf(1.23f), NumberUtils.createNumber("1.23F"));
        assertEquals(Double.valueOf(1.23d), NumberUtils.createNumber("1.23d"));
        assertEquals(Double.valueOf(1.23d), NumberUtils.createNumber("1.23D"));
    }

    // Tests createNumber with single qualifier character should throw NumberFormatException (Lang-44)
    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_onlyQualifierCharacter_throwsNumberFormatException() {
        NumberUtils.createNumber("L");
    }

    // Tests createNumber with invalid format throws NumberFormatException
    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_invalidFormat_throwsNumberFormatException() {
        NumberUtils.createNumber("1.2.3");
    }

    // Tests minimum for long and int
    @Test
    public void testMinimum_variousInputs_returnsMinimum() {
        assertEquals(1, NumberUtils.minimum(1, 2, 3));
        assertEquals(1, NumberUtils.minimum(2, 1, 3));
        assertEquals(1, NumberUtils.minimum(3, 2, 1));
        assertEquals(10L, NumberUtils.minimum(10L, 20L, 30L));
        assertEquals(10L, NumberUtils.minimum(20L, 10L, 30L));
        assertEquals(10L, NumberUtils.minimum(30L, 20L, 10L));
    }

    // Tests maximum for long and int
    @Test
    public void testMaximum_variousInputs_returnsMaximum() {
        assertEquals(3, NumberUtils.maximum(1, 2, 3));
        assertEquals(3, NumberUtils.maximum(1, 3, 2));
        assertEquals(3, NumberUtils.maximum(3, 1, 2));
        assertEquals(30L, NumberUtils.maximum(10L, 20L, 30L));
        assertEquals(30L, NumberUtils.maximum(10L, 30L, 20L));
        assertEquals(30L, NumberUtils.maximum(30L, 10L, 20L));
    }

    // Tests compare for double values including NaN and negative zero
    @Test
    public void testCompare_doubleValues_returnsExpectedOrder() {
        assertEquals(0, NumberUtils.compare(1.0d, 1.0d));
        assertEquals(-1, NumberUtils.compare(1.0d, 2.0d));
        assertEquals(1, NumberUtils.compare(2.0d, 1.0d));
        assertEquals(0, NumberUtils.compare(Double.NaN, Double.NaN));
        assertEquals(1, NumberUtils.compare(Double.NaN, 1.0d));
        assertEquals(-1, NumberUtils.compare(1.0d, Double.NaN));
        assertEquals(-1, NumberUtils.compare(-0.0d, 0.0d));
        assertEquals(1, NumberUtils.compare(0.0d, -0.0d));
    }

    // Tests compare for float values including NaN and negative zero
    @Test
    public void testCompare_floatValues_returnsExpectedOrder() {
        assertEquals(0, NumberUtils.compare(1.0f, 1.0f));
        assertEquals(-1, NumberUtils.compare(1.0f, 2.0f));
        assertEquals(1, NumberUtils.compare(2.0f, 1.0f));
        assertEquals(0, NumberUtils.compare(Float.NaN, Float.NaN));
        assertEquals(1, NumberUtils.compare(Float.NaN, 1.0f));
        assertEquals(-1, NumberUtils.compare(1.0f, Float.NaN));
        assertEquals(-1, NumberUtils.compare(-0.0f, 0.0f));
        assertEquals(1, NumberUtils.compare(0.0f, -0.0f));
    }

    // Tests isDigits with valid, invalid, and null inputs
    @Test
    public void testIsDigits_variousInputs_returnsExpectedBoolean() {
        assertTrue(NumberUtils.isDigits("12345"));
        assertFalse(NumberUtils.isDigits("123a45"));
        assertFalse(NumberUtils.isDigits(""));
        assertFalse(NumberUtils.isDigits(null));
        assertFalse(NumberUtils.isDigits("-123"));
    }

    // Tests isNumber with various valid and invalid numerical representations
    @Test
    public void testIsNumber_variousFormats_returnsExpectedBoolean() {
        assertTrue(NumberUtils.isNumber("123"));
        assertTrue(NumberUtils.isNumber("-123"));
        assertTrue(NumberUtils.isNumber("0x123"));
        assertTrue(NumberUtils.isNumber("-0x123"));
        assertTrue(NumberUtils.isNumber("1.23"));
        assertTrue(NumberUtils.isNumber("1.23e4"));
        assertTrue(NumberUtils.isNumber("1.23E-4"));
        assertTrue(NumberUtils.isNumber("123L"));
        assertTrue(NumberUtils.isNumber("1.23f"));
        assertTrue(NumberUtils.isNumber("1.23d"));
        
        assertFalse(NumberUtils.isNumber(null));
        assertFalse(NumberUtils.isNumber(""));
        assertFalse(NumberUtils.isNumber("0x"));
        assertFalse(NumberUtils.isNumber("0xAG"));
        assertFalse(NumberUtils.isNumber("1.2.3"));
        assertFalse(NumberUtils.isNumber("1.2e3e4"));
        assertFalse(NumberUtils.isNumber("1.2e"));
        assertFalse(NumberUtils.isNumber("1.2e+"));
        assertFalse(NumberUtils.isNumber("1.2e+3L"));
        assertFalse(NumberUtils.isNumber("abc"));
    }

    // Tests individual helper factory methods
    @Test
    public void testFactoryMethods_validStrings_returnsInstances() {
        assertEquals(Float.valueOf("1.5"), NumberUtils.createFloat("1.5"));
        assertEquals(Double.valueOf("2.5"), NumberUtils.createDouble("2.5"));
        assertEquals(Integer.valueOf(10), NumberUtils.createInteger("10"));
        assertEquals(Long.valueOf(20L), NumberUtils.createLong("20"));
        assertEquals(new BigInteger("100"), NumberUtils.createBigInteger("100"));
        assertEquals(new BigDecimal("200.5"), NumberUtils.createBigDecimal("200.5"));
    }
}