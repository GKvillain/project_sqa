package org.apache.commons.lang3.math;

import org.junit.Test;
import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.*;

public class NumberUtilsTest {

    // Tests constructor
    @Test
    public void testConstructor_default_instantiatesSuccessfully() {
        assertNotNull(new NumberUtils());
    }

    // Tests toInt conversions with default and fallback values
    @Test
    public void testToInt_variousInputs_returnsExpected() {
        assertEquals(0, NumberUtils.toInt(null));
        assertEquals(0, NumberUtils.toInt(""));
        assertEquals(123, NumberUtils.toInt("123"));
        assertEquals(5, NumberUtils.toInt("invalid", 5));
        assertEquals(10, NumberUtils.toInt(null, 10));
    }

    // Tests toLong conversions with default and fallback values
    @Test
    public void testToLong_variousInputs_returnsExpected() {
        assertEquals(0L, NumberUtils.toLong(null));
        assertEquals(0L, NumberUtils.toLong(""));
        assertEquals(12345678901L, NumberUtils.toLong("12345678901"));
        assertEquals(5L, NumberUtils.toLong("invalid", 5L));
        assertEquals(10L, NumberUtils.toLong(null, 10L));
    }

    // Tests toFloat conversions with default and fallback values
    @Test
    public void testToFloat_variousInputs_returnsExpected() {
        assertEquals(0.0f, NumberUtils.toFloat(null), 0.0001f);
        assertEquals(0.0f, NumberUtils.toFloat(""), 0.0001f);
        assertEquals(1.5f, NumberUtils.toFloat("1.5"), 0.0001f);
        assertEquals(2.5f, NumberUtils.toFloat("invalid", 2.5f), 0.0001f);
        assertEquals(3.5f, NumberUtils.toFloat(null, 3.5f), 0.0001f);
    }

    // Tests toDouble conversions with default and fallback values
    @Test
    public void testToDouble_variousInputs_returnsExpected() {
        assertEquals(0.0d, NumberUtils.toDouble(null), 0.0001d);
        assertEquals(0.0d, NumberUtils.toDouble(""), 0.0001d);
        assertEquals(1.5d, NumberUtils.toDouble("1.5"), 0.0001d);
        assertEquals(2.5d, NumberUtils.toDouble("invalid", 2.5d), 0.0001d);
        assertEquals(3.5d, NumberUtils.toDouble(null, 3.5d), 0.0001d);
    }

    // Tests toByte and toShort conversions
    @Test
    public void testToByteAndToShort_variousInputs_returnsExpected() {
        assertEquals((byte) 0, NumberUtils.toByte(null));
        assertEquals((byte) 12, NumberUtils.toByte("12"));
        assertEquals((byte) 5, NumberUtils.toByte("invalid", (byte) 5));

        assertEquals((short) 0, NumberUtils.toShort(null));
        assertEquals((short) 123, NumberUtils.toShort("123"));
        assertEquals((short) 5, NumberUtils.toShort("invalid", (short) 5));
    }

    // Tests createNumber with null and blank inputs
    @Test
    public void testCreateNumber_nullAndSpecialCases_returnsExpected() {
        assertNull(NumberUtils.createNumber(null));
        assertNull(NumberUtils.createNumber("--123"));
    }

    // Tests createNumber blank input exception
    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_blankString_throwsException() {
        NumberUtils.createNumber("   ");
    }

    // Tests createNumber with hexadecimal formats (Defects4J Lang-16 focus)
    @Test
    public void testCreateNumber_hexadecimalFormats_returnsInteger() {
        assertEquals(Integer.valueOf(255), NumberUtils.createNumber("0xff"));
        assertEquals(Integer.valueOf(255), NumberUtils.createNumber("0Xff"));
        assertEquals(Integer.valueOf(255), NumberUtils.createNumber("0XFF"));
        assertEquals(Integer.valueOf(-255), NumberUtils.createNumber("-0xFF"));
        assertEquals(Integer.valueOf(-255), NumberUtils.createNumber("-0xff"));
        assertEquals(Integer.valueOf(-255), NumberUtils.createNumber("-0Xff"));
        assertEquals(Integer.valueOf(-255), NumberUtils.createNumber("-0XFF"));
    }

    // Tests createNumber with integer, long, and big integer formats
    @Test
    public void testCreateNumber_integerTypes_returnsExpected() {
        assertEquals(Integer.valueOf(123), NumberUtils.createNumber("123"));
        assertEquals(Long.valueOf(2147483648L), NumberUtils.createNumber("2147483648"));
        assertEquals(new BigInteger("9223372036854775808"), NumberUtils.createNumber("9223372036854775808"));

        assertEquals(Long.valueOf(123L), NumberUtils.createNumber("123L"));
        assertEquals(Long.valueOf(123L), NumberUtils.createNumber("123l"));
        assertEquals(new BigInteger("9223372036854775808"), NumberUtils.createNumber("9223372036854775808L"));
    }

    // Tests createNumber with floating point and type qualifiers
    @Test
    public void testCreateNumber_floatingPointAndQualifiers_returnsExpected() {
        assertEquals(Float.valueOf("1.23f"), NumberUtils.createNumber("1.23f"));
        assertEquals(Float.valueOf("1.23F"), NumberUtils.createNumber("1.23F"));
        assertEquals(Double.valueOf("1.23d"), NumberUtils.createNumber("1.23d"));
        assertEquals(Double.valueOf("1.23D"), NumberUtils.createNumber("1.23D"));

        assertEquals(Float.valueOf("1.23"), NumberUtils.createNumber("1.23"));
        assertEquals(Double.valueOf("1.2345678901234567"), NumberUtils.createNumber("1.2345678901234567"));
        assertEquals(new BigDecimal("1.23456789012345678901234567890"), NumberUtils.createNumber("1.23456789012345678901234567890"));

        assertEquals(Double.valueOf("1.23e4"), NumberUtils.createNumber("1.23e4"));
        assertEquals(Float.valueOf("1.23e4f"), NumberUtils.createNumber("1.23e4f"));
        assertEquals(Double.valueOf("1.23e4d"), NumberUtils.createNumber("1.23e4d"));
    }

    // Tests individual create methods (Float, Double, Integer, Long, BigInteger, BigDecimal)
    @Test
    public void testCreateSpecificTypes_validAndNull_returnsExpected() {
        assertNull(NumberUtils.createFloat(null));
        assertEquals(Float.valueOf(1.5f), NumberUtils.createFloat("1.5"));

        assertNull(NumberUtils.createDouble(null));
        assertEquals(Double.valueOf(1.5d), NumberUtils.createDouble("1.5"));

        assertNull(NumberUtils.createInteger(null));
        assertEquals(Integer.valueOf(10), NumberUtils.createInteger("10"));

        assertNull(NumberUtils.createLong(null));
        assertEquals(Long.valueOf(10L), NumberUtils.createLong("10"));

        assertNull(NumberUtils.createBigInteger(null));
        assertEquals(new BigInteger("10"), NumberUtils.createBigInteger("10"));

        assertNull(NumberUtils.createBigDecimal(null));
        assertEquals(new BigDecimal("10.5"), NumberUtils.createBigDecimal("10.5"));
    }

    // Tests min and max for 3 parameters of primitive types
    @Test
    public void testMinAndMax_threePrimitives_returnsExtremeValues() {
        assertEquals(1L, NumberUtils.min(1L, 2L, 3L));
        assertEquals(1L, NumberUtils.min(3L, 1L, 2L));
        assertEquals(1L, NumberUtils.min(3L, 2L, 1L));
        assertEquals(3L, NumberUtils.max(1L, 2L, 3L));
        assertEquals(3L, NumberUtils.max(3L, 1L, 2L));
        assertEquals(3L, NumberUtils.max(2L, 3L, 1L));

        assertEquals(1, NumberUtils.min(1, 2, 3));
        assertEquals(1, NumberUtils.min(3, 1, 2));
        assertEquals(1, NumberUtils.min(3, 2, 1));
        assertEquals(3, NumberUtils.max(1, 2, 3));

        assertEquals((short) 1, NumberUtils.min((short) 1, (short) 2, (short) 3));
        assertEquals((short) 3, NumberUtils.max((short) 1, (short) 2, (short) 3));

        assertEquals((byte) 1, NumberUtils.min((byte) 1, (byte) 2, (byte) 3));
        assertEquals((byte) 3, NumberUtils.max((byte) 1, (byte) 2, (byte) 3));

        assertEquals(1.0d, NumberUtils.min(1.0d, 2.0d, 3.0d), 0.0001d);
        assertEquals(3.0d, NumberUtils.max(1.0d, 2.0d, 3.0d), 0.0001d);

        assertEquals(1.0f, NumberUtils.min(1.0f, 2.0f, 3.0f), 0.0001f);
        assertEquals(3.0f, NumberUtils.max(1.0f, 2.0f, 3.0f), 0.0001f);
    }

    // Tests min and max for primitive arrays
    @Test
    public void testMinAndMax_arrays_returnsExtremeValues() {
        assertEquals(1L, NumberUtils.min(new long[]{3L, 1L, 2L}));
        assertEquals(3L, NumberUtils.max(new long[]{1L, 3L, 2L}));

        assertEquals(1, NumberUtils.min(new int[]{3, 1, 2}));
        assertEquals(3, NumberUtils.max(new int[]{1, 3, 2}));

        assertEquals((short) 1, NumberUtils.min(new short[]{3, 1, 2}));
        assertEquals((short) 3, NumberUtils.max(new short[]{1, 3, 2}));

        assertEquals((byte) 1, NumberUtils.min(new byte[]{3, 1, 2}));
        assertEquals((byte) 3, NumberUtils.max(new byte[]{1, 3, 2}));

        assertEquals(1.0d, NumberUtils.min(new double[]{3.0d, 1.0d, 2.0d}), 0.0001d);
        assertTrue(Double.isNaN(NumberUtils.min(new double[]{3.0d, Double.NaN, 2.0d})));
        assertEquals(3.0d, NumberUtils.max(new double[]{1.0d, 3.0d, 2.0d}), 0.0001d);
        assertTrue(Double.isNaN(NumberUtils.max(new double[]{1.0d, Double.NaN, 2.0d})));

        assertEquals(1.0f, NumberUtils.min(new float[]{3.0f, 1.0f, 2.0f}), 0.0001f);
        assertTrue(Float.isNaN(NumberUtils.min(new float[]{3.0f, Float.NaN, 2.0f})));
        assertEquals(3.0f, NumberUtils.max(new float[]{1.0f, 3.0f, 2.0f}), 0.0001f);
        assertTrue(Float.isNaN(NumberUtils.max(new float[]{1.0f, Float.NaN, 2.0f})));
    }

    // Tests min array with null input throwing exception
    @Test(expected = IllegalArgumentException.class)
    public void testMin_nullArray_throwsIllegalArgumentException() {
        NumberUtils.min((int[]) null);
    }

    // Tests min array with empty input throwing exception
    @Test(expected = IllegalArgumentException.class)
    public void testMin_emptyArray_throwsIllegalArgumentException() {
        NumberUtils.min(new int[]{});
    }

    // Tests isDigits method
    @Test
    public void testIsDigits_variousStrings_returnsExpected() {
        assertFalse(NumberUtils.isDigits(null));
        assertFalse(NumberUtils.isDigits(""));
        assertFalse(NumberUtils.isDigits("123a"));
        assertFalse(NumberUtils.isDigits("-123"));
        assertTrue(NumberUtils.isDigits("12345"));
    }

    // Tests isNumber method for valid and invalid formats
    @Test
    public void testIsNumber_variousFormats_returnsExpected() {
        assertFalse(NumberUtils.isNumber(null));
        assertFalse(NumberUtils.isNumber(""));
        assertFalse(NumberUtils.isNumber("abc"));
        assertFalse(NumberUtils.isNumber("1.2.3"));
        assertFalse(NumberUtils.isNumber("1e2e3"));
        assertFalse(NumberUtils.isNumber("--123"));
        assertFalse(NumberUtils.isNumber("0x"));

        assertTrue(NumberUtils.isNumber("123"));
        assertTrue(NumberUtils.isNumber("-123"));
        assertTrue(NumberUtils.isNumber("123.45"));
        assertTrue(NumberUtils.isNumber(".45"));
        assertTrue(NumberUtils.isNumber("123."));
        assertTrue(NumberUtils.isNumber("123e4"));
        assertTrue(NumberUtils.isNumber("123E+4"));
        assertTrue(NumberUtils.isNumber("123e-4"));
        assertTrue(NumberUtils.isNumber("123L"));
        assertTrue(NumberUtils.isNumber("123.45f"));
        assertTrue(NumberUtils.isNumber("123.45d"));
        assertTrue(NumberUtils.isNumber("0x12aF"));
        assertTrue(NumberUtils.isNumber("-0x12aF"));
    }
}