package org.apache.commons.lang3.math;

import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class NumberUtilsTest {

    // Tests constructor instantiation
    @Test
    public void testConstructor_instantiation_returnsInstance() {
        assertNotNull(new NumberUtils());
    }

    // Tests basic conversion methods with default fallback
    @Test
    public void testToInt_validAndInvalidInput_returnsExpectedResult() {
        assertEquals(0, NumberUtils.toInt(null));
        assertEquals(0, NumberUtils.toInt(""));
        assertEquals(123, NumberUtils.toInt("123"));
        assertEquals(5, NumberUtils.toInt("invalid", 5));
        assertEquals(10, NumberUtils.toInt(null, 10));
    }

    // Tests toLong, toFloat, toDouble, toShort, toByte basic and default conversions
    @Test
    public void testToPrimitiveTypes_validAndInvalidInput_returnsExpectedResult() {
        assertEquals(0L, NumberUtils.toLong(null));
        assertEquals(123L, NumberUtils.toLong("123", 0L));
        assertEquals(5L, NumberUtils.toLong("abc", 5L));

        assertEquals(0.0f, NumberUtils.toFloat(null), 0.0f);
        assertEquals(1.5f, NumberUtils.toFloat("1.5"), 0.0f);
        assertEquals(2.5f, NumberUtils.toFloat("invalid", 2.5f), 0.0f);

        assertEquals(0.0d, NumberUtils.toDouble(null), 0.0d);
        assertEquals(1.5d, NumberUtils.toDouble("1.5"), 0.0d);
        assertEquals(2.5d, NumberUtils.toDouble("invalid", 2.5d), 0.0d);

        assertEquals((byte) 0, NumberUtils.toByte(null));
        assertEquals((byte) 8, NumberUtils.toByte("8", (byte) 0));
        assertEquals((byte) 3, NumberUtils.toByte("invalid", (byte) 3));

        assertEquals((short) 0, NumberUtils.toShort(null));
        assertEquals((short) 12, NumberUtils.toShort("12", (short) 0));
        assertEquals((short) 4, NumberUtils.toShort("invalid", (short) 4));
    }

    // Tests createNumber null and blank handling
    @Test
    public void testCreateNumber_nullAndBlank_returnsNullOrThrowsException() {
        assertNull(NumberUtils.createNumber(null));
    }

    // Tests createNumber blank input exception
    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_blankString_throwsNumberFormatException() {
        NumberUtils.createNumber("   ");
    }

    // Tests createNumber with hex prefixes including 8 and 16 digit boundaries
    @Test
    public void testCreateNumber_hexPrefixes_returnsCorrectNumber() {
        assertEquals(Integer.valueOf(255), NumberUtils.createNumber("0xff"));
        assertEquals(Integer.valueOf(255), NumberUtils.createNumber("0Xff"));
        assertEquals(Integer.valueOf(-255), NumberUtils.createNumber("-0xff"));
        assertEquals(Integer.valueOf(-255), NumberUtils.createNumber("-0Xff"));
        assertEquals(Integer.valueOf(255), NumberUtils.createNumber("#ff"));
        assertEquals(Integer.valueOf(-255), NumberUtils.createNumber("-#ff"));

        // Hex numbers near and above integer/long boundaries
        assertEquals(Long.valueOf(0x80000000L), NumberUtils.createNumber("0x80000000"));
        assertEquals(new BigInteger("8000000000000000", 16), NumberUtils.createNumber("0x8000000000000000"));
    }

    // Tests createNumber with type qualifiers
    @Test
    public void testCreateNumber_typeQualifiers_returnsCorrectType() {
        assertEquals(Long.valueOf(12345L), NumberUtils.createNumber("12345L"));
        assertEquals(Long.valueOf(12345L), NumberUtils.createNumber("12345l"));
        assertEquals(Float.valueOf(12.34f), NumberUtils.createNumber("12.34f"));
        assertEquals(Float.valueOf(12.34f), NumberUtils.createNumber("12.34F"));
        assertEquals(Double.valueOf(12.34d), NumberUtils.createNumber("12.34d"));
        assertEquals(Double.valueOf(12.34d), NumberUtils.createNumber("12.34D"));
        assertEquals(new BigInteger("9223372036854775808"), NumberUtils.createNumber("9223372036854775808L"));
    }

    // Tests createNumber with decimal, exponent, and plain numbers
    @Test
    public void testCreateNumber_plainAndDecimal_returnsExpectedNumber() {
        assertEquals(Integer.valueOf(123), NumberUtils.createNumber("123"));
        assertEquals(Long.valueOf(2147483648L), NumberUtils.createNumber("2147483648"));
        assertEquals(new BigInteger("9223372036854775808"), NumberUtils.createNumber("9223372036854775808"));

        assertEquals(Float.valueOf(1.23f), NumberUtils.createNumber("1.23"));
        assertEquals(Double.valueOf(1.23456789012345), NumberUtils.createNumber("1.23456789012345"));
        assertEquals(new BigDecimal("1.234567890123456789"), NumberUtils.createNumber("1.234567890123456789"));
        assertEquals(Float.valueOf(1.23e4f), NumberUtils.createNumber("1.23e4"));
    }

    // Tests createNumber invalid formats
    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_invalidFormat_throwsNumberFormatException() {
        NumberUtils.createNumber("1.2.3");
    }

    // Tests createBigDecimal invalid leading double negative
    @Test(expected = NumberFormatException.class)
    public void testCreateBigDecimal_doubleNegative_throwsNumberFormatException() {
        NumberUtils.createBigDecimal("--1.23");
    }

    // Tests createBigInteger with various prefixes
    @Test
    public void testCreateBigInteger_variousRadices_returnsCorrectBigInteger() {
        assertNull(NumberUtils.createBigInteger(null));
        assertEquals(new BigInteger("10"), NumberUtils.createBigInteger("10"));
        assertEquals(new BigInteger("-10"), NumberUtils.createBigInteger("-10"));
        assertEquals(new BigInteger("ff", 16), NumberUtils.createBigInteger("0xff"));
        assertEquals(new BigInteger("ff", 16), NumberUtils.createBigInteger("#ff"));
        assertEquals(new BigInteger("77", 8), NumberUtils.createBigInteger("077"));
        assertEquals(new BigInteger("-ff", 16), NumberUtils.createBigInteger("-0xff"));
    }

    // Tests min and max array methods with valid arrays
    @Test
    public void testMinAndMax_primitiveArrays_returnsMinAndMax() {
        assertEquals(1L, NumberUtils.min(new long[]{3L, 1L, 2L}));
        assertEquals(3L, NumberUtils.max(new long[]{1L, 3L, 2L}));

        assertEquals(1, NumberUtils.min(new int[]{3, 1, 2}));
        assertEquals(3, NumberUtils.max(new int[]{1, 3, 2}));

        assertEquals((short) 1, NumberUtils.min(new short[]{3, 1, 2}));
        assertEquals((short) 3, NumberUtils.max(new short[]{1, 3, 2}));

        assertEquals((byte) 1, NumberUtils.min(new byte[]{3, 1, 2}));
        assertEquals((byte) 3, NumberUtils.max(new byte[]{1, 3, 2}));

        assertEquals(1.0d, NumberUtils.min(new double[]{3.0d, 1.0d, 2.0d}), 0.0d);
        assertEquals(3.0d, NumberUtils.max(new double[]{1.0d, 3.0d, 2.0d}), 0.0d);

        assertEquals(1.0f, NumberUtils.min(new float[]{3.0f, 1.0f, 2.0f}), 0.0f);
        assertEquals(3.0f, NumberUtils.max(new float[]{1.0f, 3.0f, 2.0f}), 0.0f);
    }

    // Tests min and max array methods with NaN values
    @Test
    public void testMinAndMax_arraysWithNaN_returnsNaN() {
        assertTrue(Double.isNaN(NumberUtils.min(new double[]{1.0d, Double.NaN, 2.0d})));
        assertTrue(Double.isNaN(NumberUtils.max(new double[]{1.0d, Double.NaN, 2.0d})));
        assertTrue(Float.isNaN(NumberUtils.min(new float[]{1.0f, Float.NaN, 2.0f})));
        assertTrue(Float.isNaN(NumberUtils.max(new float[]{1.0f, Float.NaN, 2.0f})));
    }

    // Tests min array with null array throwing exception
    @Test(expected = IllegalArgumentException.class)
    public void testMin_nullArray_throwsIllegalArgumentException() {
        NumberUtils.min((int[]) null);
    }

    // Tests min array with empty array throwing exception
    @Test(expected = IllegalArgumentException.class)
    public void testMin_emptyArray_throwsIllegalArgumentException() {
        NumberUtils.min(new int[0]);
    }

    // Tests 3-parameter min and max methods
    @Test
    public void testMinAndMax_threeParameters_returnsCorrectValue() {
        assertEquals(1L, NumberUtils.min(3L, 1L, 2L));
        assertEquals(3L, NumberUtils.max(1L, 3L, 2L));

        assertEquals(1, NumberUtils.min(3, 1, 2));
        assertEquals(3, NumberUtils.max(1, 3, 2));

        assertEquals((short) 1, NumberUtils.min((short) 3, (short) 1, (short) 2));
        assertEquals((short) 3, NumberUtils.max((short) 1, (short) 3, (short) 2));

        assertEquals((byte) 1, NumberUtils.min((byte) 3, (byte) 1, (byte) 2));
        assertEquals((byte) 3, NumberUtils.max((byte) 1, (byte) 3, (byte) 2));

        assertEquals(1.0d, NumberUtils.min(3.0d, 1.0d, 2.0d), 0.0d);
        assertEquals(3.0d, NumberUtils.max(1.0d, 3.0d, 2.0d), 0.0d);

        assertEquals(1.0f, NumberUtils.min(3.0f, 1.0f, 2.0f), 0.0f);
        assertEquals(3.0f, NumberUtils.max(1.0f, 3.0f, 2.0f), 0.0f);
    }

    // Tests isDigits method
    @Test
    public void testIsDigits_validAndInvalidStrings_returnsCorrectBoolean() {
        assertFalse(NumberUtils.isDigits(null));
        assertFalse(NumberUtils.isDigits(""));
        assertFalse(NumberUtils.isDigits("12a34"));
        assertTrue(NumberUtils.isDigits("12345"));
    }

    // Tests isNumber method for various valid and invalid numeric strings
    @Test
    public void testIsNumber_variousFormats_returnsCorrectBoolean() {
        assertFalse(NumberUtils.isNumber(null));
        assertFalse(NumberUtils.isNumber(""));
        assertFalse(NumberUtils.isNumber("   "));
        assertFalse(NumberUtils.isNumber("abc"));
        assertFalse(NumberUtils.isNumber("0x"));
        assertFalse(NumberUtils.isNumber("0xxyz"));
        assertFalse(NumberUtils.isNumber("1.2.3"));
        assertFalse(NumberUtils.isNumber("1e2e3"));
        assertFalse(NumberUtils.isNumber("--1"));

        assertTrue(NumberUtils.isNumber("123"));
        assertTrue(NumberUtils.isNumber("-123"));
        assertTrue(NumberUtils.isNumber("123.45"));
        assertTrue(NumberUtils.isNumber(".45"));
        assertTrue(NumberUtils.isNumber("123."));
        assertTrue(NumberUtils.isNumber("0x1a"));
        assertTrue(NumberUtils.isNumber("-0x1A"));
        assertTrue(NumberUtils.isNumber("1.23e4"));
        assertTrue(NumberUtils.isNumber("1.23E-4"));
        assertTrue(NumberUtils.isNumber("12345L"));
        assertTrue(NumberUtils.isNumber("12.34f"));
        assertTrue(NumberUtils.isNumber("12.34d"));
    }
}