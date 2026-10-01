package org.apache.commons.lang3.math;

import static org.junit.Assert.*;

import java.math.BigInteger;

import org.junit.Test;

public class NumberUtilsTest {

    @Test
    public void testToInt_validAndInvalidValues_returnsExpectedValues() {
        assertEquals(7, NumberUtils.toInt("7"));
        assertEquals(7, NumberUtils.toInt("7", 0));
        assertEquals(0, NumberUtils.toInt("not-a-number"));
        assertEquals(5, NumberUtils.toInt(null, 5));
    }

    @Test
    public void testToLong_validAndInvalidValues_returnsExpectedValues() {
        assertEquals(7L, NumberUtils.toLong("7"));
        assertEquals(5L, NumberUtils.toLong("invalid", 5L));
    }

    @Test
    public void testToFloat_validAndInvalidValues_returnsExpectedValues() {
        assertEquals(7.5f, NumberUtils.toFloat("7.5"), 0.0f);
        assertEquals(5.5f, NumberUtils.toFloat("invalid", 5.5f), 0.0f);
    }

    @Test
    public void testToDouble_validAndInvalidValues_returnsExpectedValues() {
        assertEquals(7.5d, NumberUtils.toDouble("7.5"), 0.0d);
        assertEquals(5.5d, NumberUtils.toDouble("invalid", 5.5d), 0.0d);
    }

    @Test
    public void testToByte_validAndInvalidValues_returnsExpectedValues() {
        assertEquals((byte) 7, NumberUtils.toByte("7"));
        assertEquals((byte) 5, NumberUtils.toByte("invalid", (byte) 5));
    }

    @Test
    public void testToShort_validAndInvalidValues_returnsExpectedValues() {
        assertEquals((short) 7, NumberUtils.toShort("7"));
        assertEquals((short) 5, NumberUtils.toShort("invalid", (short) 5));
    }

    @Test
    public void testMinMaxArrays_returnsExpectedValues() {
        assertEquals(1L, NumberUtils.min(new long[]{3L, 1L, 2L}));
        assertEquals(3L, NumberUtils.max(new long[]{1L, 3L, 2L}));
        assertEquals(1, NumberUtils.min(new int[]{3, 1, 2}));
        assertEquals(3, NumberUtils.max(new int[]{1, 3, 2}));
        assertEquals((short) 1, NumberUtils.min(new short[]{(short) 3, (short) 1, (short) 2}));
        assertEquals((short) 3, NumberUtils.max(new short[]{(short) 1, (short) 3, (short) 2}));
        assertEquals((byte) 1, NumberUtils.min(new byte[]{(byte) 3, (byte) 1, (byte) 2}));
        assertEquals((byte) 3, NumberUtils.max(new byte[]{(byte) 1, (byte) 3, (byte) 2}));
        assertEquals(1.0d, NumberUtils.min(new double[]{3.0d, 1.0d, 2.0d}), 0.0d);
        assertEquals(3.0d, NumberUtils.max(new double[]{1.0d, 3.0d, 2.0d}), 0.0d);
        assertEquals(1.0f, NumberUtils.min(new float[]{3.0f, 1.0f, 2.0f}), 0.0f);
        assertEquals(3.0f, NumberUtils.max(new float[]{1.0f, 3.0f, 2.0f}), 0.0f);
    }

    @Test
    public void testMinMaxThreeValues_returnsExpectedValues() {
        assertEquals(1, NumberUtils.min(3, 1, 2));
        assertEquals(3, NumberUtils.max(1, 3, 2));
        assertEquals(1L, NumberUtils.min(3L, 1L, 2L));
        assertEquals(3L, NumberUtils.max(1L, 3L, 2L));
        assertEquals(1.0d, NumberUtils.min(3.0d, 1.0d, 2.0d), 0.0d);
        assertEquals(3.0f, NumberUtils.max(1.0f, 3.0f, 2.0f), 0.0f);
    }

    @Test
    public void testIsDigits_validAndInvalidValues_returnsExpectedValues() {
        assertTrue(NumberUtils.isDigits("123"));
        assertFalse(NumberUtils.isDigits("12.3"));
        assertFalse(NumberUtils.isDigits(""));
    }

    @Test
    public void testIsNumber_validAndInvalidValues_returnsExpectedValues() {
        assertTrue(NumberUtils.isNumber("123"));
        assertTrue(NumberUtils.isNumber("123L"));
        assertTrue(NumberUtils.isNumber("1.2"));
        assertTrue(NumberUtils.isNumber("0x1F"));
        assertFalse(NumberUtils.isNumber("0x"));
        assertFalse(NumberUtils.isNumber("abc"));
        assertFalse(NumberUtils.isNumber(""));
    }

    @Test
    public void testCreateNumber_null_returnsNull() {
        assertNull(NumberUtils.createNumber(null));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_blankString_throwsNumberFormatException() {
        NumberUtils.createNumber("   ");
    }

    @Test
    public void testCreateNumber_integerString_returnsInteger() {
        Number number = NumberUtils.createNumber("42");
        assertSame(Integer.class, number.getClass());
        assertEquals(42, number.intValue());
    }

    @Test
    public void testCreateNumber_longAndBigIntegerStrings_returnCorrectTypes() {
        Number longNumber = NumberUtils.createNumber("123456789012");
        assertSame(Long.class, longNumber.getClass());
        assertEquals(123456789012L, longNumber.longValue());

        Number bigNumber = NumberUtils.createNumber("123456789012345678901234567890");
        assertSame(BigInteger.class, bigNumber.getClass());
        assertEquals(new BigInteger("123456789012345678901234567890"), bigNumber);
    }

    @Test
    public void testCreateNumber_decimalString_returnsFloat() {
        Number number = NumberUtils.createNumber("1234.5");
        assertSame(Float.class, number.getClass());
        assertEquals(1234.5f, number.floatValue(), 0.0f);
    }

    @Test
    public void testCreateNumber_hexString_returnsInteger() {
        Number number = NumberUtils.createNumber("0x10");
        assertSame(Integer.class, number.getClass());
        assertEquals(16, number.intValue());

        Number negativeNumber = NumberUtils.createNumber("-0x10");
        assertSame(Integer.class, negativeNumber.getClass());
        assertEquals(-16, negativeNumber.intValue());
    }

    // Regression test: a non-zero double below Float.MIN_VALUE must not be treated as zero.
    @Test
    public void testCreateNumber_doubleUnderflow_returnsDouble() {
        Number number = NumberUtils.createNumber("1e-46");
        assertSame(Double.class, number.getClass());
        assertEquals(1e-46, number.doubleValue(), 0.0d);

        Number suffixed = NumberUtils.createNumber("1e-46d");
        assertSame(Double.class, suffixed.getClass());
        assertEquals(1e-46, suffixed.doubleValue(), 0.0d);
    }

    @Test
    public void testCreateNumber_suffixStrings_returnCorrectTypes() {
        Number longNumber = NumberUtils.createNumber("123L");
        assertSame(Long.class, longNumber.getClass());
        assertEquals(123L, longNumber.longValue());

        Number floatNumber = NumberUtils.createNumber("1.5f");
        assertSame(Float.class, floatNumber.getClass());
        assertEquals(1.5f, floatNumber.floatValue(), 0.0f);

        Number doubleNumber = NumberUtils.createNumber("1.5d");
        assertSame(Double.class, doubleNumber.getClass());
        assertEquals(1.5d, doubleNumber.doubleValue(), 0.0d);
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_invalidString_throwsNumberFormatException() {
        NumberUtils.createNumber("123abc");
    }
}