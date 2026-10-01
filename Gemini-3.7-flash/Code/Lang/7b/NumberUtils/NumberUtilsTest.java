package org.apache.commons.lang3.math;

import org.junit.Test;
import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.*;

public class NumberUtilsTest {

    // Tests default constructor
    @Test
    public void testConstructor_instanceCreation_success() {
        assertNotNull(new NumberUtils());
    }

    // Tests toInt with null, valid string, and default fallback
    @Test
    public void testToInt_variousInputs_returnsExpected() {
        assertEquals(0, NumberUtils.toInt(null));
        assertEquals(0, NumberUtils.toInt(""));
        assertEquals(123, NumberUtils.toInt("123"));
        assertEquals(5, NumberUtils.toInt("invalid", 5));
        assertEquals(5, NumberUtils.toInt(null, 5));
    }

    // Tests toLong with null, valid string, and default fallback
    @Test
    public void testToLong_variousInputs_returnsExpected() {
        assertEquals(0L, NumberUtils.toLong(null));
        assertEquals(0L, NumberUtils.toLong(""));
        assertEquals(123456789012L, NumberUtils.toLong("123456789012"));
        assertEquals(5L, NumberUtils.toLong("invalid", 5L));
        assertEquals(5L, NumberUtils.toLong(null, 5L));
    }

    // Tests toFloat with null, valid string, and default fallback
    @Test
    public void testToFloat_variousInputs_returnsExpected() {
        assertEquals(0.0f, NumberUtils.toFloat(null), 0.0f);
        assertEquals(0.0f, NumberUtils.toFloat(""), 0.0f);
        assertEquals(1.5f, NumberUtils.toFloat("1.5"), 0.0f);
        assertEquals(2.5f, NumberUtils.toFloat("invalid", 2.5f), 0.0f);
        assertEquals(2.5f, NumberUtils.toFloat(null, 2.5f), 0.0f);
    }

    // Tests toDouble with null, valid string, and default fallback
    @Test
    public void testToDouble_variousInputs_returnsExpected() {
        assertEquals(0.0d, NumberUtils.toDouble(null), 0.0d);
        assertEquals(0.0d, NumberUtils.toDouble(""), 0.0d);
        assertEquals(1.5d, NumberUtils.toDouble("1.5"), 0.0d);
        assertEquals(2.5d, NumberUtils.toDouble("invalid", 2.5d), 0.0d);
        assertEquals(2.5d, NumberUtils.toDouble(null, 2.5d), 0.0d);
    }

    // Tests toByte and toShort
    @Test
    public void testToByteAndToShort_variousInputs_returnsExpected() {
        assertEquals((byte) 0, NumberUtils.toByte(null));
        assertEquals((byte) 10, NumberUtils.toByte("10"));
        assertEquals((byte) 5, NumberUtils.toByte("invalid", (byte) 5));

        assertEquals((short) 0, NumberUtils.toShort(null));
        assertEquals((short) 100, NumberUtils.toShort("100"));
        assertEquals((short) 5, NumberUtils.toShort("invalid", (short) 5));
    }

    // Tests createNumber with null and leading double minus
    @Test
    public void testCreateNumber_nullAndDoubleMinus_returnsNull() {
        assertNull(NumberUtils.createNumber(null));
        assertNull(NumberUtils.createNumber("--123"));
    }

    // Tests createNumber with blank string throwing NumberFormatException
    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_blankString_throwsException() {
        NumberUtils.createNumber("   ");
    }

    // Tests createNumber with hexadecimal prefix (Defects4J Lang-7b bug detection)
    @Test
    public void testCreateNumber_hexadecimalPrefix_returnsCorrectNumber() {
        assertEquals(Integer.valueOf(255), NumberUtils.createNumber("0xFF"));
        assertEquals(Integer.valueOf(255), NumberUtils.createNumber("0xff"));
        assertEquals(Integer.valueOf(255), NumberUtils.createNumber("#FF"));
        assertEquals(Integer.valueOf(-255), NumberUtils.createNumber("-0xFF"));
        assertEquals(Integer.valueOf(-255), NumberUtils.createNumber("-#FF"));
        assertEquals(Long.valueOf(0x1234567890L), NumberUtils.createNumber("0x1234567890"));
        assertEquals(Long.valueOf(-0x1234567890L), NumberUtils.createNumber("-0x1234567890"));
    }

    // Tests createNumber with decimal, exponent and type qualifiers
    @Test
    public void testCreateNumber_variousTypesAndQualifiers_returnsCorrectType() {
        assertEquals(Integer.valueOf(123), NumberUtils.createNumber("123"));
        assertEquals(Long.valueOf(123456789012L), NumberUtils.createNumber("123456789012"));
        assertEquals(new BigInteger("123456789012345678901234567890"), NumberUtils.createNumber("123456789012345678901234567890"));

        assertEquals(Long.valueOf(123L), NumberUtils.createNumber("123L"));
        assertEquals(Long.valueOf(123L), NumberUtils.createNumber("123l"));
        assertEquals(Float.valueOf(1.23f), NumberUtils.createNumber("1.23f"));
        assertEquals(Float.valueOf(1.23F), NumberUtils.createNumber("1.23F"));
        assertEquals(Double.valueOf(1.23d), NumberUtils.createNumber("1.23d"));
        assertEquals(Double.valueOf(1.23D), NumberUtils.createNumber("1.23D"));

        assertEquals(Float.valueOf(1.23f), NumberUtils.createNumber("1.23"));
        assertEquals(Double.valueOf(1.234567890123456), NumberUtils.createNumber("1.234567890123456"));
        assertEquals(new BigDecimal("1.23E1000"), NumberUtils.createNumber("1.23E1000"));
    }

    // Tests createNumber with invalid formatted strings
    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_invalidFormat_throwsException() {
        NumberUtils.createNumber("1.2.3");
    }

    // Tests individual create methods
    @Test
    public void testDirectCreateMethods_validInputs_returnsNumbers() {
        assertEquals(Float.valueOf(1.5f), NumberUtils.createFloat("1.5"));
        assertNull(NumberUtils.createFloat(null));

        assertEquals(Double.valueOf(1.5d), NumberUtils.createDouble("1.5"));
        assertNull(NumberUtils.createDouble(null));

        assertEquals(Integer.valueOf(15), NumberUtils.createInteger("15"));
        assertNull(NumberUtils.createInteger(null));

        assertEquals(Long.valueOf(15L), NumberUtils.createLong("15"));
        assertNull(NumberUtils.createLong(null));

        assertEquals(new BigInteger("15"), NumberUtils.createBigInteger("15"));
        assertNull(NumberUtils.createBigInteger(null));

        assertEquals(new BigDecimal("15.5"), NumberUtils.createBigDecimal("15.5"));
        assertNull(NumberUtils.createBigDecimal(null));
    }

    // Tests array min methods for primitives
    @Test
    public void testMin_primitiveArrays_returnsMinimum() {
        assertEquals(1L, NumberUtils.min(new long[]{3L, 1L, 2L}));
        assertEquals(1, NumberUtils.min(new int[]{3, 1, 2}));
        assertEquals((short) 1, NumberUtils.min(new short[]{(short) 3, (short) 1, (short) 2}));
        assertEquals((byte) 1, NumberUtils.min(new byte[]{(byte) 3, (byte) 1, (byte) 2}));
        assertEquals(1.0d, NumberUtils.min(new double[]{3.0d, 1.0d, 2.0d}), 0.0d);
        assertEquals(1.0f, NumberUtils.min(new float[]{3.0f, 1.0f, 2.0f}), 0.0f);
        assertTrue(Double.isNaN(NumberUtils.min(new double[]{3.0d, Double.NaN, 2.0d})));
        assertTrue(Float.isNaN(NumberUtils.min(new float[]{3.0f, Float.NaN, 2.0f})));
    }

    // Tests array min with null array throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testMin_nullArray_throwsException() {
        NumberUtils.min((int[]) null);
    }

    // Tests array min with empty array throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testMin_emptyArray_throwsException() {
        NumberUtils.min(new int[]{});
    }

    // Tests array max methods for primitives
    @Test
    public void testMax_primitiveArrays_returnsMaximum() {
        assertEquals(3L, NumberUtils.max(new long[]{1L, 3L, 2L}));
        assertEquals(3, NumberUtils.max(new int[]{1, 3, 2}));
        assertEquals((short) 3, NumberUtils.max(new short[]{(short) 1, (short) 3, (short) 2}));
        assertEquals((byte) 3, NumberUtils.max(new byte[]{(byte) 1, (byte) 3, (byte) 2}));
        assertEquals(3.0d, NumberUtils.max(new double[]{1.0d, 3.0d, 2.0d}), 0.0d);
        assertEquals(3.0f, NumberUtils.max(new float[]{1.0f, 3.0f, 2.0f}), 0.0f);
        assertTrue(Double.isNaN(NumberUtils.max(new double[]{1.0d, Double.NaN, 2.0d})));
        assertTrue(Float.isNaN(NumberUtils.max(new float[]{1.0f, Float.NaN, 2.0f})));
    }

    // Tests 3-parameter min and max methods
    @Test
    public void testMinAndMax_threeParameters_returnsCorrectValue() {
        assertEquals(1L, NumberUtils.min(1L, 2L, 3L));
        assertEquals(1L, NumberUtils.min(3L, 1L, 2L));
        assertEquals(1L, NumberUtils.min(3L, 2L, 1L));

        assertEquals(1, NumberUtils.min(1, 2, 3));
        assertEquals(1, NumberUtils.min(3, 1, 2));
        assertEquals(1, NumberUtils.min(3, 2, 1));

        assertEquals((short) 1, NumberUtils.min((short) 1, (short) 2, (short) 3));
        assertEquals((byte) 1, NumberUtils.min((byte) 1, (byte) 2, (byte) 3));
        assertEquals(1.0d, NumberUtils.min(1.0d, 2.0d, 3.0d), 0.0d);
        assertEquals(1.0f, NumberUtils.min(1.0f, 2.0f, 3.0f), 0.0f);

        assertEquals(3L, NumberUtils.max(1L, 2L, 3L));
        assertEquals(3L, NumberUtils.max(1L, 3L, 2L));
        assertEquals(3L, NumberUtils.max(3L, 2L, 1L));

        assertEquals(3, NumberUtils.max(1, 2, 3));
        assertEquals(3, NumberUtils.max(1, 3, 2));
        assertEquals(3, NumberUtils.max(3, 2, 1));

        assertEquals((short) 3, NumberUtils.max((short) 1, (short) 2, (short) 3));
        assertEquals((byte) 3, NumberUtils.max((byte) 1, (byte) 2, (byte) 3));
        assertEquals(3.0d, NumberUtils.max(1.0d, 2.0d, 3.0d), 0.0d);
        assertEquals(3.0f, NumberUtils.max(1.0f, 2.0f, 3.0f), 0.0f);
    }

    // Tests isDigits method
    @Test
    public void testIsDigits_variousInputs_returnsExpected() {
        assertFalse(NumberUtils.isDigits(null));
        assertFalse(NumberUtils.isDigits(""));
        assertTrue(NumberUtils.isDigits("12345"));
        assertFalse(NumberUtils.isDigits("1234a5"));
        assertFalse(NumberUtils.isDigits("-12345"));
    }

    // Tests isNumber method for various valid and invalid formats
    @Test
    public void testIsNumber_variousInputs_returnsExpected() {
        assertFalse(NumberUtils.isNumber(null));
        assertFalse(NumberUtils.isNumber(""));
        assertTrue(NumberUtils.isNumber("123"));
        assertTrue(NumberUtils.isNumber("-123"));
        assertTrue(NumberUtils.isNumber("123.45"));
        assertTrue(NumberUtils.isNumber("-123.45"));
        assertTrue(NumberUtils.isNumber(".45"));
        assertTrue(NumberUtils.isNumber("1.2e3"));
        assertTrue(NumberUtils.isNumber("1.2E-3"));
        assertTrue(NumberUtils.isNumber("123L"));
        assertTrue(NumberUtils.isNumber("123.4f"));
        assertTrue(NumberUtils.isNumber("123.4d"));
        assertTrue(NumberUtils.isNumber("0x1A"));
        assertTrue(NumberUtils.isNumber("-0x1A"));

        assertFalse(NumberUtils.isNumber("0x"));
        assertFalse(NumberUtils.isNumber("1.2.3"));
        assertFalse(NumberUtils.isNumber("1e2e3"));
        assertFalse(NumberUtils.isNumber("123e"));
        assertFalse(NumberUtils.isNumber("123e+"));
        assertFalse(NumberUtils.isNumber("--123"));
        assertFalse(NumberUtils.isNumber("abc"));
        assertFalse(NumberUtils.isNumber("123L45"));
    }
}