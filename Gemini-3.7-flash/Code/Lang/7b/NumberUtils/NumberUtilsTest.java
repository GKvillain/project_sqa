package org.apache.commons.lang3.math;

import org.junit.Test;
import java.math.BigDecimal;
import java.math.BigInteger;
import static org.junit.Assert.*;

public class NumberUtilsTest {

    // Tests createNumber with hex string starting with # or positive/negative hex
    @Test
    public void testCreateNumber_hexadecimalPrefixes_returnsCorrectNumber() {
        assertEquals(Integer.valueOf(0x1234), NumberUtils.createNumber("0x1234"));
        assertEquals(Integer.valueOf(-0x1234), NumberUtils.createNumber("-0x1234"));
        assertEquals(Integer.valueOf(0xABCD), NumberUtils.createNumber("0XABCD"));
        assertEquals(Integer.valueOf(-0xABCD), NumberUtils.createNumber("-0XABCD"));
        assertEquals(Long.valueOf(0x123456789L), NumberUtils.createNumber("0x123456789"));
        assertEquals(Long.valueOf(-0x123456789L), NumberUtils.createNumber("-0x123456789"));
    }

    // Tests createNumber with # prefix or negative # prefix (Defects4J Lang-7 defect target)
    @Test
    public void testCreateNumber_hashPrefixHex_returnsCorrectNumber() {
        assertEquals(Integer.valueOf(0x1234), NumberUtils.createNumber("#1234"));
        assertEquals(Integer.valueOf(-0x1234), NumberUtils.createNumber("-#1234"));
    }

    // Tests createNumber with null and blank inputs
    @Test
    public void testCreateNumber_nullAndBlankInput_returnsNullOrThrowsException() {
        assertNull(NumberUtils.createNumber(null));
        assertNull(NumberUtils.createNumber("--123"));
    }

    // Tests createNumber with blank string throws NumberFormatException
    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_blankString_throwsNumberFormatException() {
        NumberUtils.createNumber("   ");
    }

    // Tests createNumber with standard integers, longs, bigintegers, floats, doubles, bigdecimals
    @Test
    public void testCreateNumber_variousValidNumbers_returnsCorrectTypes() {
        assertEquals(Integer.valueOf(123), NumberUtils.createNumber("123"));
        assertEquals(Long.valueOf(1234567890123L), NumberUtils.createNumber("1234567890123"));
        assertEquals(new BigInteger("123456789012345678901234567890"), NumberUtils.createNumber("123456789012345678901234567890"));
        assertEquals(Float.valueOf("1.23f"), NumberUtils.createNumber("1.23f"));
        assertEquals(Double.valueOf("1.23d"), NumberUtils.createNumber("1.23d"));
        assertEquals(Long.valueOf(123L), NumberUtils.createNumber("123L"));
        assertEquals(new BigInteger("123456789012345678901234567890"), NumberUtils.createNumber("123456789012345678901234567890L"));
        assertEquals(Double.valueOf("1.23"), NumberUtils.createNumber("1.23"));
        assertEquals(new BigDecimal("1.23456789012345678901234567890"), NumberUtils.createNumber("1.23456789012345678901234567890"));
    }

    // Tests createNumber with scientific notation exponents
    @Test
    public void testCreateNumber_scientificNotation_returnsCorrectNumber() {
        assertEquals(Double.valueOf(1.23e4), NumberUtils.createNumber("1.23e4"));
        assertEquals(Float.valueOf(1.23e4f), NumberUtils.createNumber("1.23e4f"));
        assertEquals(Double.valueOf(1.23e4d), NumberUtils.createNumber("1.23e4d"));
    }

    // Tests createNumber with invalid format string throws NumberFormatException
    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_invalidFormat_throwsNumberFormatException() {
        NumberUtils.createNumber("1.2.3");
    }

    // Tests toInt, toLong, toFloat, toDouble, toByte, toShort with valid and fallback values
    @Test
    public void testToPrimitives_validAndDefaults_returnsCorrectValues() {
        assertEquals(10, NumberUtils.toInt("10"));
        assertEquals(5, NumberUtils.toInt("invalid", 5));
        assertEquals(0, NumberUtils.toInt(null));

        assertEquals(10L, NumberUtils.toLong("10"));
        assertEquals(5L, NumberUtils.toLong("invalid", 5L));
        assertEquals(0L, NumberUtils.toLong(null));

        assertEquals(1.5f, NumberUtils.toFloat("1.5"), 0.001f);
        assertEquals(5.5f, NumberUtils.toFloat("invalid", 5.5f), 0.001f);
        assertEquals(0.0f, NumberUtils.toFloat(null), 0.001f);

        assertEquals(1.5d, NumberUtils.toDouble("1.5"), 0.001d);
        assertEquals(5.5d, NumberUtils.toDouble("invalid", 5.5d), 0.001d);
        assertEquals(0.0d, NumberUtils.toDouble(null), 0.001d);

        assertEquals((byte) 10, NumberUtils.toByte("10"));
        assertEquals((byte) 5, NumberUtils.toByte("invalid", (byte) 5));
        assertEquals((byte) 0, NumberUtils.toByte(null));

        assertEquals((short) 10, NumberUtils.toShort("10"));
        assertEquals((short) 5, NumberUtils.toShort("invalid", (short) 5));
        assertEquals((short) 0, NumberUtils.toShort(null));
    }

    // Tests min and max helper methods with 3 primitives
    @Test
    public void testMinAndMax_threePrimitives_returnsExtremes() {
        assertEquals(1, NumberUtils.min(1, 2, 3));
        assertEquals(1, NumberUtils.min(3, 1, 2));
        assertEquals(1, NumberUtils.min(3, 2, 1));
        assertEquals(3, NumberUtils.max(1, 2, 3));
        assertEquals(3, NumberUtils.max(1, 3, 2));
        assertEquals(3, NumberUtils.max(3, 2, 1));

        assertEquals(1L, NumberUtils.min(1L, 2L, 3L));
        assertEquals(3L, NumberUtils.max(1L, 2L, 3L));

        assertEquals((short) 1, NumberUtils.min((short) 1, (short) 2, (short) 3));
        assertEquals((short) 3, NumberUtils.max((short) 1, (short) 2, (short) 3));

        assertEquals((byte) 1, NumberUtils.min((byte) 1, (byte) 2, (byte) 3));
        assertEquals((byte) 3, NumberUtils.max((byte) 1, (byte) 2, (byte) 3));

        assertEquals(1.0d, NumberUtils.min(1.0d, 2.0d, 3.0d), 0.001d);
        assertEquals(3.0d, NumberUtils.max(1.0d, 2.0d, 3.0d), 0.001d);

        assertEquals(1.0f, NumberUtils.min(1.0f, 2.0f, 3.0f), 0.001f);
        assertEquals(3.0f, NumberUtils.max(1.0f, 2.0f, 3.0f), 0.001f);
    }

    // Tests min and max array methods
    @Test
    public void testMinAndMax_arrays_returnsExtremes() {
        assertEquals(1, NumberUtils.min(new int[]{3, 1, 2}));
        assertEquals(3, NumberUtils.max(new int[]{1, 3, 2}));

        assertEquals(1L, NumberUtils.min(new long[]{3L, 1L, 2L}));
        assertEquals(3L, NumberUtils.max(new long[]{1L, 3L, 2L}));

        assertEquals((short) 1, NumberUtils.min(new short[]{(short) 3, (short) 1, (short) 2}));
        assertEquals((short) 3, NumberUtils.max(new short[]{(short) 1, (short) 3, (short) 2}));

        assertEquals((byte) 1, NumberUtils.min(new byte[]{(byte) 3, (byte) 1, (byte) 2}));
        assertEquals((byte) 3, NumberUtils.max(new byte[]{(byte) 1, (byte) 3, (byte) 2}));

        assertEquals(1.0d, NumberUtils.min(new double[]{3.0d, 1.0d, 2.0d}), 0.001d);
        assertEquals(3.0d, NumberUtils.max(new double[]{1.0d, 3.0d, 2.0d}), 0.001d);
        assertTrue(Double.isNaN(NumberUtils.min(new double[]{1.0d, Double.NaN, 2.0d})));
        assertTrue(Double.isNaN(NumberUtils.max(new double[]{1.0d, Double.NaN, 2.0d})));

        assertEquals(1.0f, NumberUtils.min(new float[]{3.0f, 1.0f, 2.0f}), 0.001f);
        assertEquals(3.0f, NumberUtils.max(new float[]{1.0f, 3.0f, 2.0f}), 0.001f);
        assertTrue(Float.isNaN(NumberUtils.min(new float[]{1.0f, Float.NaN, 2.0f})));
        assertTrue(Float.isNaN(NumberUtils.max(new float[]{1.0f, Float.NaN, 2.0f})));
    }

    // Tests min array with null input throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testMin_nullArray_throwsIllegalArgumentException() {
        NumberUtils.min((int[]) null);
    }

    // Tests min array with empty input throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testMin_emptyArray_throwsIllegalArgumentException() {
        NumberUtils.min(new int[]{});
    }

    // Tests isDigits method
    @Test
    public void testIsDigits_variousStrings_returnsExpectedBoolean() {
        assertTrue(NumberUtils.isDigits("12345"));
        assertFalse(NumberUtils.isDigits("123.45"));
        assertFalse(NumberUtils.isDigits(""));
        assertFalse(NumberUtils.isDigits(null));
        assertFalse(NumberUtils.isDigits("12a34"));
    }

    // Tests isNumber method with valid and invalid inputs
    @Test
    public void testIsNumber_variousFormats_returnsExpectedBoolean() {
        assertTrue(NumberUtils.isNumber("123"));
        assertTrue(NumberUtils.isNumber("-123"));
        assertTrue(NumberUtils.isNumber("1.23"));
        assertTrue(NumberUtils.isNumber("1.23e4"));
        assertTrue(NumberUtils.isNumber("1.23E4"));
        assertTrue(NumberUtils.isNumber("1.23E-4"));
        assertTrue(NumberUtils.isNumber("1.23E+4"));
        assertTrue(NumberUtils.isNumber("0x1234"));
        assertTrue(NumberUtils.isNumber("-0x1234"));
        assertTrue(NumberUtils.isNumber("123L"));
        assertTrue(NumberUtils.isNumber("123f"));
        assertTrue(NumberUtils.isNumber("123d"));

        assertFalse(NumberUtils.isNumber(null));
        assertFalse(NumberUtils.isNumber(""));
        assertFalse(NumberUtils.isNumber("0x"));
        assertFalse(NumberUtils.isNumber("--123"));
        assertFalse(NumberUtils.isNumber("1.2.3"));
        assertFalse(NumberUtils.isNumber("123e"));
        assertFalse(NumberUtils.isNumber("123e+"));
        assertFalse(NumberUtils.isNumber("123a"));
    }

    // Tests constructor and constant instances
    @Test
    public void testConstructorAndConstants_instantiation_returnsValidObjects() {
        assertNotNull(new NumberUtils());
        assertEquals(Long.valueOf(0L), NumberUtils.LONG_ZERO);
        assertEquals(Integer.valueOf(0), NumberUtils.INTEGER_ZERO);
        assertEquals(Short.valueOf((short) 0), NumberUtils.SHORT_ZERO);
        assertEquals(Byte.valueOf((byte) 0), NumberUtils.BYTE_ZERO);
        assertEquals(Double.valueOf(0.0d), NumberUtils.DOUBLE_ZERO);
        assertEquals(Float.valueOf(0.0f), NumberUtils.FLOAT_ZERO);
    }
}