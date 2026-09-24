package org.apache.commons.lang3.math;

import org.junit.Test;
import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.*;

public class NumberUtilsTest {

    // Tests default constructor
    @Test
    public void testConstructor_default_instantiatesCorrectly() {
        assertNotNull(new NumberUtils());
    }

    // Tests toInt conversions with normal, null, and invalid inputs
    @Test
    public void testToInt_variousInputs_returnsExpected() {
        assertEquals(123, NumberUtils.toInt("123"));
        assertEquals(0, NumberUtils.toInt(null));
        assertEquals(0, NumberUtils.toInt(""));
        assertEquals(0, NumberUtils.toInt("abc"));
        assertEquals(5, NumberUtils.toInt("invalid", 5));
        assertEquals(5, NumberUtils.toInt(null, 5));
    }

    // Tests toLong conversions with normal, null, and invalid inputs
    @Test
    public void testToLong_variousInputs_returnsExpected() {
        assertEquals(123456789012L, NumberUtils.toLong("123456789012"));
        assertEquals(0L, NumberUtils.toLong(null));
        assertEquals(0L, NumberUtils.toLong(""));
        assertEquals(0L, NumberUtils.toLong("abc"));
        assertEquals(10L, NumberUtils.toLong("xyz", 10L));
        assertEquals(10L, NumberUtils.toLong(null, 10L));
    }

    // Tests toFloat conversions with normal, null, and invalid inputs
    @Test
    public void testToFloat_variousInputs_returnsExpected() {
        assertEquals(1.5f, NumberUtils.toFloat("1.5"), 0.0001f);
        assertEquals(0.0f, NumberUtils.toFloat(null), 0.0001f);
        assertEquals(0.0f, NumberUtils.toFloat(""), 0.0001f);
        assertEquals(0.0f, NumberUtils.toFloat("abc"), 0.0001f);
        assertEquals(2.5f, NumberUtils.toFloat("invalid", 2.5f), 0.0001f);
        assertEquals(2.5f, NumberUtils.toFloat(null, 2.5f), 0.0001f);
    }

    // Tests toDouble conversions with normal, null, and invalid inputs
    @Test
    public void testToDouble_variousInputs_returnsExpected() {
        assertEquals(1.5d, NumberUtils.toDouble("1.5"), 0.0001d);
        assertEquals(0.0d, NumberUtils.toDouble(null), 0.0001d);
        assertEquals(0.0d, NumberUtils.toDouble(""), 0.0001d);
        assertEquals(0.0d, NumberUtils.toDouble("abc"), 0.0001d);
        assertEquals(3.5d, NumberUtils.toDouble("invalid", 3.5d), 0.0001d);
        assertEquals(3.5d, NumberUtils.toDouble(null, 3.5d), 0.0001d);
    }

    // Tests toByte and toShort conversions
    @Test
    public void testToByteAndToShort_variousInputs_returnsExpected() {
        assertEquals((byte) 12, NumberUtils.toByte("12"));
        assertEquals((byte) 0, NumberUtils.toByte(null));
        assertEquals((byte) 5, NumberUtils.toByte("invalid", (byte) 5));

        assertEquals((short) 123, NumberUtils.toShort("123"));
        assertEquals((short) 0, NumberUtils.toShort(null));
        assertEquals((short) 5, NumberUtils.toShort("invalid", (short) 5));
    }

    // Tests createNumber with null, blank, and special prefix cases
    @Test
    public void testCreateNumber_nullAndSpecialCases_returnsExpected() {
        assertNull(NumberUtils.createNumber(null));
        assertNull(NumberUtils.createNumber("--123"));
        assertEquals(Integer.valueOf(255), NumberUtils.createNumber("0xFF"));
        assertEquals(Integer.valueOf(-255), NumberUtils.createNumber("-0xFF"));
    }

    // Tests createNumber blank string throws NumberFormatException
    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_blankString_throwsNumberFormatException() {
        NumberUtils.createNumber("   ");
    }

    // Tests createNumber type suffix parsing (f, F, d, D, l, L)
    @Test
    public void testCreateNumber_typeSuffixes_returnsCorrectType() {
        assertEquals(Float.valueOf(1.23f), NumberUtils.createNumber("1.23f"));
        assertEquals(Float.valueOf(1.23f), NumberUtils.createNumber("1.23F"));
        assertEquals(Double.valueOf(1.23d), NumberUtils.createNumber("1.23d"));
        assertEquals(Double.valueOf(1.23d), NumberUtils.createNumber("1.23D"));
        assertEquals(Long.valueOf(12345L), NumberUtils.createNumber("12345l"));
        assertEquals(Long.valueOf(12345L), NumberUtils.createNumber("12345L"));
        assertEquals(new BigInteger("999999999999999999999999999999"), NumberUtils.createNumber("999999999999999999999999999999L"));
    }

    // Tests createNumber without suffix for integers, longs, big integers, and decimals
    @Test
    public void testCreateNumber_withoutSuffix_returnsCorrectType() {
        assertEquals(Integer.valueOf(123), NumberUtils.createNumber("123"));
        assertEquals(Long.valueOf(3000000000L), NumberUtils.createNumber("3000000000"));
        assertEquals(new BigInteger("123456789012345678901234567890"), NumberUtils.createNumber("123456789012345678901234567890"));
        assertEquals(Float.valueOf(1.23f), NumberUtils.createNumber("1.23"));
        assertEquals(Double.valueOf(1.23456789012345e20d), NumberUtils.createNumber("1.23456789012345e20"));
        assertEquals(new BigDecimal("1.234567890123456789012345678901234567890"), NumberUtils.createNumber("1.234567890123456789012345678901234567890"));
    }

    // Tests specific create methods for primitive wrappers and big numbers
    @Test
    public void testCreateSpecificTypes_validAndNull_returnsExpected() {
        assertNull(NumberUtils.createFloat(null));
        assertEquals(Float.valueOf(1.5f), NumberUtils.createFloat("1.5"));

        assertNull(NumberUtils.createDouble(null));
        assertEquals(Double.valueOf(1.5d), NumberUtils.createDouble("1.5"));

        assertNull(NumberUtils.createInteger(null));
        assertEquals(Integer.valueOf(15), NumberUtils.createInteger("15"));

        assertNull(NumberUtils.createLong(null));
        assertEquals(Long.valueOf(15L), NumberUtils.createLong("15"));

        assertNull(NumberUtils.createBigInteger(null));
        assertEquals(new BigInteger("15"), NumberUtils.createBigInteger("15"));

        assertNull(NumberUtils.createBigDecimal(null));
        assertEquals(new BigDecimal("15.5"), NumberUtils.createBigDecimal("15.5"));
    }

    // Tests isDigits method
    @Test
    public void testIsDigits_variousInputs_returnsExpected() {
        assertFalse(NumberUtils.isDigits(null));
        assertFalse(NumberUtils.isDigits(""));
        assertTrue(NumberUtils.isDigits("12345"));
        assertFalse(NumberUtils.isDigits("123a45"));
        assertFalse(NumberUtils.isDigits("-123"));
    }

    // Tests isNumber for valid numbers
    @Test
    public void testIsNumber_validNumbers_returnsTrue() {
        assertTrue(NumberUtils.isNumber("123"));
        assertTrue(NumberUtils.isNumber("-123"));
        assertTrue(NumberUtils.isNumber("+123"));
        assertTrue(NumberUtils.isNumber("0x1A"));
        assertTrue(NumberUtils.isNumber("-0x1A"));
        assertTrue(NumberUtils.isNumber("1.23"));
        assertTrue(NumberUtils.isNumber(".23"));
        assertTrue(NumberUtils.isNumber("123."));
        assertTrue(NumberUtils.isNumber("1.23e-4"));
        assertTrue(NumberUtils.isNumber("1.23E4"));
        assertTrue(NumberUtils.isNumber("1234L"));
        assertTrue(NumberUtils.isNumber("12.34f"));
        assertTrue(NumberUtils.isNumber("12.34d"));
    }

    // Tests isNumber for invalid numbers and defect Lang-24 (decimals with L suffix)
    @Test
    public void testIsNumber_invalidNumbers_returnsFalse() {
        assertFalse(NumberUtils.isNumber(null));
        assertFalse(NumberUtils.isNumber(""));
        assertFalse(NumberUtils.isNumber("0x"));
        assertFalse(NumberUtils.isNumber("0x1G"));
        assertFalse(NumberUtils.isNumber("1.2.3"));
        assertFalse(NumberUtils.isNumber("1e2e3"));
        assertFalse(NumberUtils.isNumber("1e"));
        assertFalse(NumberUtils.isNumber("1e-"));
        assertFalse(NumberUtils.isNumber("."));
        assertFalse(NumberUtils.isNumber("1a"));
        assertFalse(NumberUtils.isNumber("1.1L"));
        assertFalse(NumberUtils.isNumber("1.L"));
        assertFalse(NumberUtils.isNumber(".1L"));
        assertFalse(NumberUtils.isNumber("1e1L"));
    }

    // Tests min and max on primitive arrays
    @Test
    public void testMinAndMax_primitiveArrays_returnsCorrectExtremes() {
        assertEquals(1L, NumberUtils.min(new long[]{3L, 1L, 2L}));
        assertEquals(3L, NumberUtils.max(new long[]{3L, 1L, 2L}));

        assertEquals(1, NumberUtils.min(new int[]{3, 1, 2}));
        assertEquals(3, NumberUtils.max(new int[]{3, 1, 2}));

        assertEquals((short) 1, NumberUtils.min(new short[]{3, 1, 2}));
        assertEquals((short) 3, NumberUtils.max(new short[]{3, 1, 2}));

        assertEquals((byte) 1, NumberUtils.min(new byte[]{3, 1, 2}));
        assertEquals((byte) 3, NumberUtils.max(new byte[]{3, 1, 2}));

        assertEquals(1.0d, NumberUtils.min(new double[]{3.0d, 1.0d, 2.0d}), 0.0001d);
        assertEquals(3.0d, NumberUtils.max(new double[]{3.0d, 1.0d, 2.0d}), 0.0001d);
        assertTrue(Double.isNaN(NumberUtils.min(new double[]{1.0d, Double.NaN, 2.0d})));
        assertTrue(Double.isNaN(NumberUtils.max(new double[]{1.0d, Double.NaN, 2.0d})));

        assertEquals(1.0f, NumberUtils.min(new float[]{3.0f, 1.0f, 2.0f}), 0.0001f);
        assertEquals(3.0f, NumberUtils.max(new float[]{3.0f, 1.0f, 2.0f}), 0.0001f);
        assertTrue(Float.isNaN(NumberUtils.min(new float[]{1.0f, Float.NaN, 2.0f})));
        assertTrue(Float.isNaN(NumberUtils.max(new float[]{1.0f, Float.NaN, 2.0f})));
    }

    // Tests min array with null input throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testMin_nullArray_throwsIllegalArgumentException() {
        NumberUtils.min((int[]) null);
    }

    // Tests max array with empty array input throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testMax_emptyArray_throwsIllegalArgumentException() {
        NumberUtils.max(new int[0]);
    }

    // Tests 3-parameter min and max methods
    @Test
    public void testMinAndMax_threeValues_returnsCorrectExtremes() {
        assertEquals(1L, NumberUtils.min(1L, 2L, 3L));
        assertEquals(1L, NumberUtils.min(3L, 1L, 2L));
        assertEquals(1L, NumberUtils.min(3L, 2L, 1L));
        assertEquals(3L, NumberUtils.max(1L, 2L, 3L));
        assertEquals(3L, NumberUtils.max(2L, 3L, 1L));
        assertEquals(3L, NumberUtils.max(3L, 2L, 1L));

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
}