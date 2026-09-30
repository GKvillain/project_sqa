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

    // Tests constructor
    @Test
    public void testConstructor_instanceCreation_succeeds() {
        assertNotNull(new NumberUtils());
    }

    // Tests toInt, toLong, toFloat, toDouble conversions with null, valid, and invalid inputs
    @Test
    public void testToPrimitiveConversions_variousInputs_returnsExpected() {
        assertEquals(0, NumberUtils.toInt(null));
        assertEquals(5, NumberUtils.toInt(null, 5));
        assertEquals(42, NumberUtils.toInt("42", 0));
        assertEquals(10, NumberUtils.toInt("invalid", 10));

        assertEquals(0L, NumberUtils.toLong(null));
        assertEquals(5L, NumberUtils.toLong(null, 5L));
        assertEquals(1234567890123L, NumberUtils.toLong("1234567890123", 0L));
        assertEquals(99L, NumberUtils.toLong("invalid", 99L));

        assertEquals(0.0f, NumberUtils.toFloat(null), 0.0001f);
        assertEquals(1.5f, NumberUtils.toFloat("1.5", 0.0f), 0.0001f);
        assertEquals(2.5f, NumberUtils.toFloat("invalid", 2.5f), 0.0001f);

        assertEquals(0.0d, NumberUtils.toDouble(null), 0.0001d);
        assertEquals(1.5d, NumberUtils.toDouble("1.5", 0.0d), 0.0001d);
        assertEquals(3.5d, NumberUtils.toDouble("invalid", 3.5d), 0.0001d);

        assertEquals((byte) 0, NumberUtils.toByte(null));
        assertEquals((byte) 12, NumberUtils.toByte("12", (byte) 0));
        assertEquals((byte) 7, NumberUtils.toByte("invalid", (byte) 7));

        assertEquals((short) 0, NumberUtils.toShort(null));
        assertEquals((short) 123, NumberUtils.toShort("123", (short) 0));
        assertEquals((short) 9, NumberUtils.toShort("invalid", (short) 9));
    }

    // Tests createNumber with null and double-negative prefix
    @Test
    public void testCreateNumber_nullAndNegativePrefix_returnsExpected() {
        assertNull(NumberUtils.createNumber(null));
        assertNull(NumberUtils.createNumber("--1234"));
    }

    // Tests createNumber with blank string throwing NumberFormatException
    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_blankString_throwsException() {
        NumberUtils.createNumber("   ");
    }

    // Tests createNumber with hex strings
    @Test
    public void testCreateNumber_hexStrings_returnsInteger() {
        assertEquals(Integer.valueOf(255), NumberUtils.createNumber("0xFF"));
        assertEquals(Integer.valueOf(-255), NumberUtils.createNumber("-0xFF"));
    }

    // Tests createNumber with integer, long, and BigInteger
    @Test
    public void testCreateNumber_integersAndLongs_returnsCorrectTypes() {
        assertEquals(Integer.valueOf(123), NumberUtils.createNumber("123"));
        assertEquals(Long.valueOf(123456789012345L), NumberUtils.createNumber("123456789012345"));
        assertEquals(new BigInteger("123456789012345678901234567890"), NumberUtils.createNumber("123456789012345678901234567890"));
    }

    // Tests createNumber with float, double, and BigDecimal numbers
    @Test
    public void testCreateNumber_floatingPointAndDecimals_returnsCorrectTypes() {
        assertEquals(Float.valueOf(1.23f), NumberUtils.createNumber("1.23f"));
        assertEquals(Float.valueOf(1.23F), NumberUtils.createNumber("1.23F"));
        assertEquals(Double.valueOf(1.23d), NumberUtils.createNumber("1.23d"));
        assertEquals(Double.valueOf(1.23D), NumberUtils.createNumber("1.23D"));
        assertEquals(Long.valueOf(12345L), NumberUtils.createNumber("12345L"));
        assertEquals(Long.valueOf(12345L), NumberUtils.createNumber("12345l"));
        assertEquals(Float.valueOf(1.23f), NumberUtils.createNumber("1.23"));
        assertEquals(new BigDecimal("1.23456789012345678901234567890"), NumberUtils.createNumber("1.23456789012345678901234567890"));
        assertEquals(Float.valueOf(0.0f), NumberUtils.createNumber("0.0"));
        assertEquals(Double.valueOf(0.0d), NumberUtils.createNumber("0.0d"));
    }

    // Tests createNumber with scientific exponential notation
    @Test
    public void testCreateNumber_scientificNotation_returnsNumber() {
        assertEquals(Float.valueOf(1.2e3f), NumberUtils.createNumber("1.2e3f"));
        assertEquals(Double.valueOf(1.2e3d), NumberUtils.createNumber("1.2e3d"));
        assertEquals(Float.valueOf(1.2e3f), NumberUtils.createNumber("1.2e3"));
        assertEquals(new BigDecimal("1.234567890123456789e30"), NumberUtils.createNumber("1.234567890123456789e30"));
    }

    // Tests createNumber with invalid formatted strings throwing NumberFormatException
    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_invalidFormat_throwsException() {
        NumberUtils.createNumber("1.2.3");
    }

    // Tests createNumber with invalid qualifier throwing NumberFormatException
    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_invalidQualifier_throwsException() {
        NumberUtils.createNumber("123z");
    }

    // Tests specific helper factory methods
    @Test
    public void testCreateTypedHelpers_validAndNullInputs_returnsExpected() {
        assertNull(NumberUtils.createFloat(null));
        assertEquals(Float.valueOf(3.14f), NumberUtils.createFloat("3.14"));

        assertNull(NumberUtils.createDouble(null));
        assertEquals(Double.valueOf(3.14159d), NumberUtils.createDouble("3.14159"));

        assertNull(NumberUtils.createInteger(null));
        assertEquals(Integer.valueOf(100), NumberUtils.createInteger("100"));

        assertNull(NumberUtils.createLong(null));
        assertEquals(Long.valueOf(1000L), NumberUtils.createLong("1000"));

        assertNull(NumberUtils.createBigInteger(null));
        assertEquals(new BigInteger("100000"), NumberUtils.createBigInteger("100000"));

        assertNull(NumberUtils.createBigDecimal(null));
        assertEquals(new BigDecimal("100.001"), NumberUtils.createBigDecimal("100.001"));
    }

    // Tests min and max for 3 primitive values
    @Test
    public void testMinMaxThreeParams_variousInputs_returnsExtremes() {
        assertEquals(1L, NumberUtils.min(3L, 1L, 2L));
        assertEquals(3L, NumberUtils.max(1L, 3L, 2L));

        assertEquals(1, NumberUtils.min(3, 1, 2));
        assertEquals(3, NumberUtils.max(1, 3, 2));

        assertEquals((short) 1, NumberUtils.min((short) 3, (short) 1, (short) 2));
        assertEquals((short) 3, NumberUtils.max((short) 1, (short) 3, (short) 2));

        assertEquals((byte) 1, NumberUtils.min((byte) 3, (byte) 1, (byte) 2));
        assertEquals((byte) 3, NumberUtils.max((byte) 1, (byte) 3, (byte) 2));

        assertEquals(1.0d, NumberUtils.min(3.0d, 1.0d, 2.0d), 0.0001d);
        assertEquals(3.0d, NumberUtils.max(1.0d, 3.0d, 2.0d), 0.0001d);

        assertEquals(1.0f, NumberUtils.min(3.0f, 1.0f, 2.0f), 0.0001f);
        assertEquals(3.0f, NumberUtils.max(1.0f, 3.0f, 2.0f), 0.0001f);
    }

    // Tests min and max for primitive arrays
    @Test
    public void testMinMaxArrays_validArrays_returnsMinAndMax() {
        assertEquals(1L, NumberUtils.min(new long[]{3L, 1L, 2L}));
        assertEquals(3L, NumberUtils.max(new long[]{1L, 3L, 2L}));

        assertEquals(1, NumberUtils.min(new int[]{3, 1, 2}));
        assertEquals(3, NumberUtils.max(new int[]{1, 3, 2}));

        assertEquals((short) 1, NumberUtils.min(new short[]{3, 1, 2}));
        assertEquals((short) 3, NumberUtils.max(new short[]{1, 3, 2}));

        assertEquals((byte) 1, NumberUtils.min(new byte[]{3, 1, 2}));
        assertEquals((byte) 3, NumberUtils.max(new byte[]{1, 3, 2}));

        assertEquals(1.0d, NumberUtils.min(new double[]{3.0d, 1.0d, 2.0d}), 0.0001d);
        assertEquals(3.0d, NumberUtils.max(new double[]{1.0d, 3.0d, 2.0d}), 0.0001d);

        assertEquals(1.0f, NumberUtils.min(new float[]{3.0f, 1.0f, 2.0f}), 0.0001f);
        assertEquals(3.0f, NumberUtils.max(new float[]{1.0f, 3.0f, 2.0f}), 0.0001f);
    }

    // Tests min array with NaN values
    @Test
    public void testMinArray_nanValues_returnsNaN() {
        assertTrue(Double.isNaN(NumberUtils.min(new double[]{1.0d, Double.NaN, 2.0d})));
        assertTrue(Float.isNaN(NumberUtils.min(new float[]{1.0f, Float.NaN, 2.0f})));
    }

    // Tests max array with NaN values
    @Test
    public void testMaxArray_nanValues_returnsNaN() {
        assertTrue(Double.isNaN(NumberUtils.max(new double[]{1.0d, Double.NaN, 2.0d})));
        assertTrue(Float.isNaN(NumberUtils.max(new float[]{1.0f, Float.NaN, 2.0f})));
    }

    // Tests min array with null input throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testMinArray_nullArray_throwsException() {
        NumberUtils.min((int[]) null);
    }

    // Tests min array with empty array throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testMinArray_emptyArray_throwsException() {
        NumberUtils.min(new int[0]);
    }

    // Tests isDigits method with various inputs
    @Test
    public void testIsDigits_variousInputs_returnsExpected() {
        assertFalse(NumberUtils.isDigits(null));
        assertFalse(NumberUtils.isDigits(""));
        assertTrue(NumberUtils.isDigits("12345"));
        assertFalse(NumberUtils.isDigits("123a45"));
        assertFalse(NumberUtils.isDigits("-123"));
    }

    // Tests isNumber method with valid numbers
    @Test
    public void testIsNumber_validNumbers_returnsTrue() {
        assertTrue(NumberUtils.isNumber("123"));
        assertTrue(NumberUtils.isNumber("-123"));
        assertTrue(NumberUtils.isNumber("123.45"));
        assertTrue(NumberUtils.isNumber(".45"));
        assertTrue(NumberUtils.isNumber("123."));
        assertTrue(NumberUtils.isNumber("1.23e4"));
        assertTrue(NumberUtils.isNumber("1.23E-4"));
        assertTrue(NumberUtils.isNumber("123L"));
        assertTrue(NumberUtils.isNumber("123.45f"));
        assertTrue(NumberUtils.isNumber("123.45d"));
        assertTrue(NumberUtils.isNumber("0x1A"));
        assertTrue(NumberUtils.isNumber("-0x1A"));
    }

    // Tests isNumber method with invalid numbers
    @Test
    public void testIsNumber_invalidNumbers_returnsFalse() {
        assertFalse(NumberUtils.isNumber(null));
        assertFalse(NumberUtils.isNumber(""));
        assertFalse(NumberUtils.isNumber("   "));
        assertFalse(NumberUtils.isNumber("0x"));
        assertFalse(NumberUtils.isNumber("0xG1"));
        assertFalse(NumberUtils.isNumber("1.2.3"));
        assertFalse(NumberUtils.isNumber("1e"));
        assertFalse(NumberUtils.isNumber("1e-"));
        assertFalse(NumberUtils.isNumber("1eE2"));
        assertFalse(NumberUtils.isNumber("1.2e+"));
        assertFalse(NumberUtils.isNumber("abc"));
        assertFalse(NumberUtils.isNumber("."));
        assertFalse(NumberUtils.isNumber("1.1L"));
        assertFalse(NumberUtils.isNumber("1.1l"));
    }
}