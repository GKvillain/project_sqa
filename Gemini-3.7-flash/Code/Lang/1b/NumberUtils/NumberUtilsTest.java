package org.apache.commons.lang3.math;

import org.junit.Test;
import java.math.BigDecimal;
import java.math.BigInteger;
import static org.junit.Assert.*;

public class NumberUtilsTest {

    // Tests constructor initialization
    @Test
    public void testConstructor_default_instantiatesSuccessfully() {
        assertNotNull(new NumberUtils());
    }

    // Tests toInt with valid string and default value
    @Test
    public void testToInt_validAndInvalidInput_returnsExpectedResult() {
        assertEquals(0, NumberUtils.toInt(null));
        assertEquals(0, NumberUtils.toInt(""));
        assertEquals(123, NumberUtils.toInt("123"));
        assertEquals(5, NumberUtils.toInt("invalid", 5));
        assertEquals(5, NumberUtils.toInt(null, 5));
    }

    // Tests toLong with valid string and default value
    @Test
    public void testToLong_validAndInvalidInput_returnsExpectedResult() {
        assertEquals(0L, NumberUtils.toLong(null));
        assertEquals(0L, NumberUtils.toLong(""));
        assertEquals(123456789012L, NumberUtils.toLong("123456789012"));
        assertEquals(10L, NumberUtils.toLong("abc", 10L));
        assertEquals(10L, NumberUtils.toLong(null, 10L));
    }

    // Tests toFloat and toDouble with default value
    @Test
    public void testToFloatAndToDouble_validAndInvalidInput_returnsExpectedResult() {
        assertEquals(0.0f, NumberUtils.toFloat(null), 0.0001f);
        assertEquals(1.5f, NumberUtils.toFloat("1.5"), 0.0001f);
        assertEquals(2.5f, NumberUtils.toFloat("invalid", 2.5f), 0.0001f);

        assertEquals(0.0d, NumberUtils.toDouble(null), 0.0001d);
        assertEquals(1.5d, NumberUtils.toDouble("1.5"), 0.0001d);
        assertEquals(3.5d, NumberUtils.toDouble("invalid", 3.5d), 0.0001d);
    }

    // Tests toByte and toShort with default value
    @Test
    public void testToByteAndToShort_validAndInvalidInput_returnsExpectedResult() {
        assertEquals((byte) 0, NumberUtils.toByte(null));
        assertEquals((byte) 12, NumberUtils.toByte("12"));
        assertEquals((byte) 5, NumberUtils.toByte("invalid", (byte) 5));

        assertEquals((short) 0, NumberUtils.toShort(null));
        assertEquals((short) 123, NumberUtils.toShort("123"));
        assertEquals((short) 5, NumberUtils.toShort("invalid", (short) 5));
    }

    // Tests createNumber with null and blank inputs
    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_blankString_throwsException() {
        NumberUtils.createNumber("   ");
    }

    // Tests createNumber returning null for null input
    @Test
    public void testCreateNumber_nullInput_returnsNull() {
        assertNull(NumberUtils.createNumber(null));
    }

    // Tests createNumber for hexadecimal numbers (Defects4J Lang-1 target)
    @Test
    public void testCreateNumber_hexadecimalNumbers_returnsCorrectNumber() {
        assertEquals(Integer.valueOf(0x1234), NumberUtils.createNumber("0x1234"));
        assertEquals(Integer.valueOf(0x1234), NumberUtils.createNumber("#1234"));
        assertEquals(Integer.valueOf(-0x1234), NumberUtils.createNumber("-0x1234"));
        assertEquals(Integer.valueOf(-0x1234), NumberUtils.createNumber("-#1234"));
        assertEquals(Integer.valueOf(0x7FFFFFFF), NumberUtils.createNumber("0x7FFFFFFF"));
        assertEquals(Long.valueOf(0x80000000L), NumberUtils.createNumber("0x80000000"));
        assertEquals(Long.valueOf(0x123456789L), NumberUtils.createNumber("0x123456789"));
        assertEquals(new BigInteger("12345678901234567", 16), NumberUtils.createNumber("0x12345678901234567"));
    }

    // Tests createNumber with standard integer, long and big integer representations
    @Test
    public void testCreateNumber_integersAndLongs_returnsCorrectType() {
        assertEquals(Integer.valueOf(12345), NumberUtils.createNumber("12345"));
        assertEquals(Long.valueOf(123456789012L), NumberUtils.createNumber("123456789012"));
        assertEquals(new BigInteger("123456789012345678901234567890"), NumberUtils.createNumber("123456789012345678901234567890"));
    }

    // Tests createNumber with type qualifiers (l, L, f, F, d, D)
    @Test
    public void testCreateNumber_typeQualifiers_returnsCorrectType() {
        assertEquals(Long.valueOf(123L), NumberUtils.createNumber("123L"));
        assertEquals(Long.valueOf(123L), NumberUtils.createNumber("123l"));
        assertEquals(Float.valueOf(1.23f), NumberUtils.createNumber("1.23f"));
        assertEquals(Float.valueOf(1.23f), NumberUtils.createNumber("1.23F"));
        assertEquals(Double.valueOf(1.23d), NumberUtils.createNumber("1.23d"));
        assertEquals(Double.valueOf(1.23d), NumberUtils.createNumber("1.23D"));
    }

    // Tests createNumber with floating point numbers and scientific notation
    @Test
    public void testCreateNumber_floatingAndScientific_returnsCorrectType() {
        assertEquals(Float.valueOf(1.23f), NumberUtils.createNumber("1.23"));
        assertEquals(Double.valueOf(1.234567890123456d), NumberUtils.createNumber("1.234567890123456"));
        assertEquals(new BigDecimal("1.23456789012345678901"), NumberUtils.createNumber("1.23456789012345678901"));
        assertEquals(Float.valueOf(1.2e3f), NumberUtils.createNumber("1.2e3"));
    }

    // Tests createNumber with invalid input strings
    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_invalidFormat_throwsException() {
        NumberUtils.createNumber("1.2.3");
    }

    // Tests createFloat, createDouble, createBigInteger and createBigDecimal
    @Test
    public void testCreateSpecificNumberTypes_validAndNull_returnsExpected() {
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
        assertEquals(new BigInteger("-10"), NumberUtils.createBigInteger("-10"));
        assertEquals(new BigInteger("16", 16), NumberUtils.createBigInteger("0x10"));
        assertEquals(new BigInteger("8", 8), NumberUtils.createBigInteger("010"));

        assertNull(NumberUtils.createBigDecimal(null));
        assertEquals(new BigDecimal("10.5"), NumberUtils.createBigDecimal("10.5"));
    }

    // Tests min and max with 3 parameters for long, int, short, byte, double, float
    @Test
    public void testMinAndMax_threePrimitives_returnsMinAndMax() {
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
        assertEquals(3, NumberUtils.max(3, 1, 2));
        assertEquals(3, NumberUtils.max(2, 3, 1));

        assertEquals((short) 1, NumberUtils.min((short) 1, (short) 2, (short) 3));
        assertEquals((short) 3, NumberUtils.max((short) 1, (short) 2, (short) 3));

        assertEquals((byte) 1, NumberUtils.min((byte) 1, (byte) 2, (byte) 3));
        assertEquals((byte) 3, NumberUtils.max((byte) 1, (byte) 2, (byte) 3));

        assertEquals(1.0d, NumberUtils.min(1.0d, 2.0d, 3.0d), 0.0001d);
        assertEquals(3.0d, NumberUtils.max(1.0d, 2.0d, 3.0d), 0.0001d);

        assertEquals(1.0f, NumberUtils.min(1.0f, 2.0f, 3.0f), 0.0001f);
        assertEquals(3.0f, NumberUtils.max(1.0f, 2.0f, 3.0f), 0.0001f);
    }

    // Tests min and max array methods
    @Test
    public void testMinAndMax_arrays_returnsMinAndMax() {
        long[] longArray = {3L, 1L, 5L};
        assertEquals(1L, NumberUtils.min(longArray));
        assertEquals(5L, NumberUtils.max(longArray));

        int[] intArray = {3, 1, 5};
        assertEquals(1, NumberUtils.min(intArray));
        assertEquals(5, NumberUtils.max(intArray));

        short[] shortArray = {(short) 3, (short) 1, (short) 5};
        assertEquals((short) 1, NumberUtils.min(shortArray));
        assertEquals((short) 5, NumberUtils.max(shortArray));

        byte[] byteArray = {(byte) 3, (byte) 1, (byte) 5};
        assertEquals((byte) 1, NumberUtils.min(byteArray));
        assertEquals((byte) 5, NumberUtils.max(byteArray));

        double[] doubleArray = {3.0d, 1.0d, 5.0d};
        assertEquals(1.0d, NumberUtils.min(doubleArray), 0.0001d);
        assertEquals(5.0d, NumberUtils.max(doubleArray), 0.0001d);

        double[] nanDoubleArray = {1.0d, Double.NaN, 5.0d};
        assertTrue(Double.isNaN(NumberUtils.min(nanDoubleArray)));
        assertTrue(Double.isNaN(NumberUtils.max(nanDoubleArray)));

        float[] floatArray = {3.0f, 1.0f, 5.0f};
        assertEquals(1.0f, NumberUtils.min(floatArray), 0.0001f);
        assertEquals(5.0f, NumberUtils.max(floatArray), 0.0001f);

        float[] nanFloatArray = {1.0f, Float.NaN, 5.0f};
        assertTrue(Float.isNaN(NumberUtils.min(nanFloatArray)));
        assertTrue(Float.isNaN(NumberUtils.max(nanFloatArray)));
    }

    // Tests min and max with invalid array inputs
    @Test(expected = IllegalArgumentException.class)
    public void testMin_nullArray_throwsIllegalArgumentException() {
        NumberUtils.min((int[]) null);
    }

    // Tests min and max with empty array inputs
    @Test(expected = IllegalArgumentException.class)
    public void testMax_emptyArray_throwsIllegalArgumentException() {
        NumberUtils.max(new int[0]);
    }

    // Tests isDigits method
    @Test
    public void testIsDigits_variousStrings_returnsExpectedBoolean() {
        assertFalse(NumberUtils.isDigits(null));
        assertFalse(NumberUtils.isDigits(""));
        assertFalse(NumberUtils.isDigits("123a"));
        assertFalse(NumberUtils.isDigits("-123"));
        assertTrue(NumberUtils.isDigits("12345"));
    }

    // Tests isNumber method for various valid and invalid representations
    @Test
    public void testIsNumber_variousFormats_returnsExpectedBoolean() {
        assertFalse(NumberUtils.isNumber(null));
        assertFalse(NumberUtils.isNumber(""));
        assertFalse(NumberUtils.isNumber("abc"));
        assertFalse(NumberUtils.isNumber("0x"));
        assertFalse(NumberUtils.isNumber("--1"));
        assertFalse(NumberUtils.isNumber("1.2.3"));
        assertFalse(NumberUtils.isNumber("1e2e3"));
        assertFalse(NumberUtils.isNumber("1.2L"));

        assertTrue(NumberUtils.isNumber("123"));
        assertTrue(NumberUtils.isNumber("-123"));
        assertTrue(NumberUtils.isNumber("123.45"));
        assertTrue(NumberUtils.isNumber("-123.45"));
        assertTrue(NumberUtils.isNumber("1234L"));
        assertTrue(NumberUtils.isNumber("1.23e-4"));
        assertTrue(NumberUtils.isNumber("1.23E+4"));
        assertTrue(NumberUtils.isNumber("0x1234"));
        assertTrue(NumberUtils.isNumber("-0x1234"));
        assertTrue(NumberUtils.isNumber("1.5f"));
        assertTrue(NumberUtils.isNumber("1.5d"));
    }
}