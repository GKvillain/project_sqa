package org.apache.commons.lang3.math;

import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.*;

public class NumberUtilsTest {

    // Tests createNumber with null and blank inputs
    @Test
    public void testCreateNumber_nullAndBlank_returnsNullOrThrowsException() {
        assertNull(NumberUtils.createNumber(null));
    }

    // Tests createNumber with blank string throws NumberFormatException
    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_blankString_throwsException() {
        NumberUtils.createNumber("   ");
    }

    // Tests createNumber with double negative prefix
    @Test
    public void testCreateNumber_doubleMinusPrefix_returnsNull() {
        assertNull(NumberUtils.createNumber("--123"));
    }

    // Tests createNumber with hexadecimal numbers
    @Test
    public void testCreateNumber_hexadecimal_returnsInteger() {
        assertEquals(Integer.valueOf(255), NumberUtils.createNumber("0xFF"));
        assertEquals(Integer.valueOf(-255), NumberUtils.createNumber("-0xFF"));
    }

    // Tests createNumber for standard integers, longs, and big integers
    @Test
    public void testCreateNumber_integersAndLongs_returnsCorrectTypes() {
        assertEquals(Integer.valueOf(123), NumberUtils.createNumber("123"));
        assertEquals(Long.valueOf(2147483648L), NumberUtils.createNumber("2147483648"));
        assertEquals(new BigInteger("9223372036854775808"), NumberUtils.createNumber("9223372036854775808"));
    }

    // Tests createNumber with explicit type qualifiers (l, L, f, F, d, D)
    @Test
    public void testCreateNumber_typeQualifiers_returnsCorrectTypes() {
        assertEquals(Long.valueOf(123L), NumberUtils.createNumber("123l"));
        assertEquals(Long.valueOf(123L), NumberUtils.createNumber("123L"));
        assertEquals(Float.valueOf(1.23f), NumberUtils.createNumber("1.23f"));
        assertEquals(Float.valueOf(1.23f), NumberUtils.createNumber("1.23F"));
        assertEquals(Double.valueOf(1.23d), NumberUtils.createNumber("1.23d"));
        assertEquals(Double.valueOf(1.23d), NumberUtils.createNumber("1.23D"));
    }

    // Tests createNumber with floating point numbers without qualifier
    @Test
    public void testCreateNumber_floatingPointNoQualifier_returnsFloatOrDoubleOrBigDecimal() {
        assertEquals(Float.valueOf("1.23"), NumberUtils.createNumber("1.23"));
        assertEquals(Double.valueOf("1.23456789012345"), NumberUtils.createNumber("1.23456789012345"));
    }

    // Tests createNumber with scientific notation exponents (Defects4J Lang-27 regression test)
    @Test
    public void testCreateNumber_scientificNotation_returnsCorrectNumber() {
        assertEquals(Double.valueOf("1e3"), NumberUtils.createNumber("1e3"));
        assertEquals(Double.valueOf("1e-3"), NumberUtils.createNumber("1e-3"));
        assertEquals(Double.valueOf("1.2e3"), NumberUtils.createNumber("1.2e3"));
        assertEquals(Float.valueOf("1.2e3f"), NumberUtils.createNumber("1.2e3f"));
        assertEquals(Double.valueOf("1.2e3d"), NumberUtils.createNumber("1.2e3d"));
        assertEquals(Double.valueOf("0.0"), NumberUtils.createNumber("0.0"));
    }

    // Tests createNumber with invalid exponential or malformed strings
    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_invalidExponentPosition_throwsException() {
        NumberUtils.createNumber("1e2.3");
    }

    // Tests createNumber with invalid trailing qualifier
    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_invalidQualifier_throwsException() {
        NumberUtils.createNumber("123z");
    }

    // Tests toInt, toLong, toFloat, toDouble conversion with defaults
    @Test
    public void testToPrimitive_validAndInvalid_returnsParsedOrDefault() {
        assertEquals(123, NumberUtils.toInt("123", 0));
        assertEquals(0, NumberUtils.toInt("abc", 0));
        assertEquals(0, NumberUtils.toInt(null));

        assertEquals(123L, NumberUtils.toLong("123", 0L));
        assertEquals(0L, NumberUtils.toLong("abc", 0L));
        assertEquals(0L, NumberUtils.toLong(null));

        assertEquals(1.5f, NumberUtils.toFloat("1.5", 0.0f), 0.0001f);
        assertEquals(0.0f, NumberUtils.toFloat("abc", 0.0f), 0.0001f);
        assertEquals(0.0f, NumberUtils.toFloat(null), 0.0001f);

        assertEquals(1.5d, NumberUtils.toDouble("1.5", 0.0d), 0.0001d);
        assertEquals(0.0d, NumberUtils.toDouble("abc", 0.0d), 0.0001d);
        assertEquals(0.0d, NumberUtils.toDouble(null), 0.0001d);

        assertEquals((byte) 5, NumberUtils.toByte("5", (byte) 0));
        assertEquals((byte) 0, NumberUtils.toByte("abc", (byte) 0));
        assertEquals((byte) 0, NumberUtils.toByte(null));

        assertEquals((short) 5, NumberUtils.toShort("5", (short) 0));
        assertEquals((short) 0, NumberUtils.toShort("abc", (short) 0));
        assertEquals((short) 0, NumberUtils.toShort(null));
    }

    // Tests create* helper methods directly
    @Test
    public void testCreateDirectMethods_validInputs_returnsExpectedInstances() {
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
    public void testIsDigits_variousStrings_returnsBoolean() {
        assertFalse(NumberUtils.isDigits(null));
        assertFalse(NumberUtils.isDigits(""));
        assertTrue(NumberUtils.isDigits("12345"));
        assertFalse(NumberUtils.isDigits("123a45"));
        assertFalse(NumberUtils.isDigits("-123"));
    }

    // Tests isNumber method with various valid and invalid formats
    @Test
    public void testIsNumber_variousFormats_returnsBoolean() {
        assertFalse(NumberUtils.isNumber(null));
        assertFalse(NumberUtils.isNumber(""));
        assertFalse(NumberUtils.isNumber("0x"));
        assertTrue(NumberUtils.isNumber("0xABCD"));
        assertTrue(NumberUtils.isNumber("-0xABCD"));
        assertFalse(NumberUtils.isNumber("0xGHIJ"));

        assertTrue(NumberUtils.isNumber("123"));
        assertTrue(NumberUtils.isNumber("-123"));
        assertTrue(NumberUtils.isNumber("123.45"));
        assertTrue(NumberUtils.isNumber("123.45f"));
        assertTrue(NumberUtils.isNumber("123.45d"));
        assertTrue(NumberUtils.isNumber("123L"));
        assertTrue(NumberUtils.isNumber("1.23e4"));
        assertTrue(NumberUtils.isNumber("1.23e+4"));
        assertTrue(NumberUtils.isNumber("1.23e-4"));

        assertFalse(NumberUtils.isNumber("1.2.3"));
        assertFalse(NumberUtils.isNumber("1e2e3"));
        assertFalse(NumberUtils.isNumber("1e"));
        assertFalse(NumberUtils.isNumber("1e+"));
        assertFalse(NumberUtils.isNumber("1.2e3L"));
        assertFalse(NumberUtils.isNumber("abc"));
    }

    // Tests min and max methods for arrays
    @Test
    public void testMinMax_arrays_returnsCorrectValues() {
        assertEquals(1L, NumberUtils.min(new long[]{3L, 1L, 2L}));
        assertEquals(3L, NumberUtils.max(new long[]{3L, 1L, 2L}));

        assertEquals(1, NumberUtils.min(new int[]{3, 1, 2}));
        assertEquals(3, NumberUtils.max(new int[]{3, 1, 2}));

        assertEquals((short) 1, NumberUtils.min(new short[]{(short) 3, (short) 1, (short) 2}));
        assertEquals((short) 3, NumberUtils.max(new short[]{(short) 3, (short) 1, (short) 2}));

        assertEquals((byte) 1, NumberUtils.min(new byte[]{(byte) 3, (byte) 1, (byte) 2}));
        assertEquals((byte) 3, NumberUtils.max(new byte[]{(byte) 3, (byte) 1, (byte) 2}));

        assertEquals(1.0d, NumberUtils.min(new double[]{3.0d, 1.0d, 2.0d}), 0.0001d);
        assertEquals(3.0d, NumberUtils.max(new double[]{3.0d, 1.0d, 2.0d}), 0.0001d);
        assertTrue(Double.isNaN(NumberUtils.min(new double[]{1.0d, Double.NaN, 2.0d})));
        assertTrue(Double.isNaN(NumberUtils.max(new double[]{1.0d, Double.NaN, 2.0d})));

        assertEquals(1.0f, NumberUtils.min(new float[]{3.0f, 1.0f, 2.0f}), 0.0001f);
        assertEquals(3.0f, NumberUtils.max(new float[]{3.0f, 1.0f, 2.0f}), 0.0001f);
        assertTrue(Float.isNaN(NumberUtils.min(new float[]{1.0f, Float.NaN, 2.0f})));
        assertTrue(Float.isNaN(NumberUtils.max(new float[]{1.0f, Float.NaN, 2.0f})));
    }

    // Tests min and max array methods with null or empty arrays
    @Test(expected = IllegalArgumentException.class)
    public void testMin_nullArray_throwsException() {
        NumberUtils.min((int[]) null);
    }

    // Tests min and max array methods with empty array
    @Test(expected = IllegalArgumentException.class)
    public void testMax_emptyArray_throwsException() {
        NumberUtils.max(new int[0]);
    }

    // Tests min and max for 3 parameters
    @Test
    public void testMinMax_threeParams_returnsCorrectValues() {
        assertEquals(1L, NumberUtils.min(3L, 1L, 2L));
        assertEquals(3L, NumberUtils.max(3L, 1L, 2L));

        assertEquals(1, NumberUtils.min(3, 1, 2));
        assertEquals(3, NumberUtils.max(3, 1, 2));

        assertEquals((short) 1, NumberUtils.min((short) 3, (short) 1, (short) 2));
        assertEquals((short) 3, NumberUtils.max((short) 3, (short) 1, (short) 2));

        assertEquals((byte) 1, NumberUtils.min((byte) 3, (byte) 1, (byte) 2));
        assertEquals((byte) 3, NumberUtils.max((byte) 3, (byte) 1, (byte) 2));

        assertEquals(1.0d, NumberUtils.min(3.0d, 1.0d, 2.0d), 0.0001d);
        assertEquals(3.0d, NumberUtils.max(3.0d, 1.0d, 2.0d), 0.0001d);

        assertEquals(1.0f, NumberUtils.min(3.0f, 1.0f, 2.0f), 0.0001f);
        assertEquals(3.0f, NumberUtils.max(3.0f, 1.0f, 2.0f), 0.0001f);
    }

    // Tests default constructor instantiation
    @Test
    public void testConstructor() {
        assertNotNull(new NumberUtils());
    }
}