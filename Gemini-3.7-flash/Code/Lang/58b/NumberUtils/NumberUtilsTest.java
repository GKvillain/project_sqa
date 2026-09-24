package org.apache.commons.lang.math;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class NumberUtilsTest {

    // Tests default constructor instantiation
    @Test
    public void testConstructor_default_createsInstance() {
        assertNotNull(new NumberUtils());
    }

    // Tests toInt with null, valid and invalid values
    @Test
    public void testToInt_variousInputs_returnsExpected() {
        assertEquals(0, NumberUtils.toInt(null));
        assertEquals(0, NumberUtils.toInt(""));
        assertEquals(5, NumberUtils.toInt("5"));
        assertEquals(1, NumberUtils.toInt("invalid", 1));
        assertEquals(1, NumberUtils.stringToInt("invalid", 1));
        assertEquals(5, NumberUtils.stringToInt("5"));
    }

    // Tests toLong with null, valid and invalid values
    @Test
    public void testToLong_variousInputs_returnsExpected() {
        assertEquals(0L, NumberUtils.toLong(null));
        assertEquals(0L, NumberUtils.toLong(""));
        assertEquals(12345L, NumberUtils.toLong("12345"));
        assertEquals(99L, NumberUtils.toLong("abc", 99L));
    }

    // Tests toFloat with null, valid and invalid values
    @Test
    public void testToFloat_variousInputs_returnsExpected() {
        assertEquals(0.0f, NumberUtils.toFloat(null), 0.0001f);
        assertEquals(0.0f, NumberUtils.toFloat(""), 0.0001f);
        assertEquals(1.5f, NumberUtils.toFloat("1.5"), 0.0001f);
        assertEquals(2.5f, NumberUtils.toFloat("invalid", 2.5f), 0.0001f);
    }

    // Tests toDouble with null, valid and invalid values
    @Test
    public void testToDouble_variousInputs_returnsExpected() {
        assertEquals(0.0d, NumberUtils.toDouble(null), 0.0001d);
        assertEquals(0.0d, NumberUtils.toDouble(""), 0.0001d);
        assertEquals(3.14d, NumberUtils.toDouble("3.14"), 0.0001d);
        assertEquals(9.99d, NumberUtils.toDouble("xyz", 9.99d), 0.0001d);
    }

    // Tests createNumber with null and special prefixes
    @Test
    public void testCreateNumber_nullAndSpecialPrefixes_returnsNullOrValue() {
        assertNull(NumberUtils.createNumber(null));
        assertNull(NumberUtils.createNumber("--123"));
        assertEquals(Integer.valueOf(255), NumberUtils.createNumber("0xFF"));
        assertEquals(Integer.valueOf(-255), NumberUtils.createNumber("-0xFF"));
    }

    // Tests createNumber with blank string throwing NumberFormatException
    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_blankString_throwsException() {
        NumberUtils.createNumber("   ");
    }

    // Tests createNumber with single-digit and standard Long qualifiers (Defects4J Lang-58)
    @Test
    public void testCreateNumber_longQualifier_returnsLong() {
        assertEquals(Long.valueOf(1L), NumberUtils.createNumber("1l"));
        assertEquals(Long.valueOf(1L), NumberUtils.createNumber("1L"));
        assertEquals(Long.valueOf(0L), NumberUtils.createNumber("0L"));
        assertEquals(Long.valueOf(-1L), NumberUtils.createNumber("-1L"));
        assertEquals(Long.valueOf(12345L), NumberUtils.createNumber("12345L"));
        assertEquals(new BigInteger("9223372036854775808"), NumberUtils.createNumber("9223372036854775808L"));
    }

    // Tests createNumber with Float and Double qualifiers
    @Test
    public void testCreateNumber_floatingPointQualifiers_returnsFloatOrDouble() {
        assertEquals(Float.valueOf(1.23f), NumberUtils.createNumber("1.23f"));
        assertEquals(Float.valueOf(1.23f), NumberUtils.createNumber("1.23F"));
        assertEquals(Double.valueOf(4.56d), NumberUtils.createNumber("4.56d"));
        assertEquals(Double.valueOf(4.56d), NumberUtils.createNumber("4.56D"));
        assertEquals(new BigDecimal("1234.56789012345678901234567890D"), NumberUtils.createNumber("1234.56789012345678901234567890D"));
    }

    // Tests createNumber without qualifier
    @Test
    public void testCreateNumber_noQualifier_returnsAppropriateNumber() {
        assertEquals(Integer.valueOf(123), NumberUtils.createNumber("123"));
        assertEquals(Long.valueOf(3000000000L), NumberUtils.createNumber("3000000000"));
        assertEquals(new BigInteger("9999999999999999999999999999"), NumberUtils.createNumber("9999999999999999999999999999"));
        assertEquals(Float.valueOf(1.5f), NumberUtils.createNumber("1.5"));
        assertEquals(Double.valueOf(1.5e20), NumberUtils.createNumber("1.5e20"));
        assertEquals(new BigDecimal("1.23456789012345678901234567890e100"), NumberUtils.createNumber("1.23456789012345678901234567890e100"));
    }

    // Tests createNumber with invalid input format throwing exception
    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_invalidFormat_throwsException() {
        NumberUtils.createNumber("1.2.3");
    }

    // Tests isDigits method
    @Test
    public void testIsDigits_variousInputs_returnsBoolean() {
        assertFalse(NumberUtils.isDigits(null));
        assertFalse(NumberUtils.isDigits(""));
        assertFalse(NumberUtils.isDigits("12a34"));
        assertTrue(NumberUtils.isDigits("12345"));
    }

    // Tests isNumber method with valid and invalid representations
    @Test
    public void testIsNumber_variousInputs_returnsBoolean() {
        assertFalse(NumberUtils.isNumber(null));
        assertFalse(NumberUtils.isNumber(""));
        assertFalse(NumberUtils.isNumber("0x"));
        assertTrue(NumberUtils.isNumber("0xABCD"));
        assertTrue(NumberUtils.isNumber("-0x12"));
        assertFalse(NumberUtils.isNumber("0xGHIJ"));
        assertTrue(NumberUtils.isNumber("123"));
        assertTrue(NumberUtils.isNumber("-123"));
        assertTrue(NumberUtils.isNumber("1.23"));
        assertTrue(NumberUtils.isNumber("1.23e-4"));
        assertTrue(NumberUtils.isNumber("1.23E+4"));
        assertTrue(NumberUtils.isNumber("123L"));
        assertTrue(NumberUtils.isNumber("123.4f"));
        assertTrue(NumberUtils.isNumber("123.4d"));
        assertFalse(NumberUtils.isNumber("1.2.3"));
        assertFalse(NumberUtils.isNumber("123e"));
        assertFalse(NumberUtils.isNumber("123e+"));
        assertFalse(NumberUtils.isNumber("123L2"));
        assertFalse(NumberUtils.isNumber("--123"));
    }

    // Tests min and max for long array
    @Test
    public void testMinMax_longArray_returnsCorrectMinMax() {
        long[] array = new long[] { 5L, 2L, 9L, -3L, 7L };
        assertEquals(-3L, NumberUtils.min(array));
        assertEquals(9L, NumberUtils.max(array));
    }

    // Tests min with null long array throwing exception
    @Test(expected = IllegalArgumentException.class)
    public void testMin_nullLongArray_throwsException() {
        NumberUtils.min((long[]) null);
    }

    // Tests min with empty int array throwing exception
    @Test(expected = IllegalArgumentException.class)
    public void testMin_emptyIntArray_throwsException() {
        NumberUtils.min(new int[0]);
    }

    // Tests min and max with 3 parameters
    @Test
    public void testMinMax_threeParams_returnsExpected() {
        assertEquals(1, NumberUtils.min(3, 1, 2));
        assertEquals(3, NumberUtils.max(1, 3, 2));
        assertEquals(1L, NumberUtils.min(3L, 2L, 1L));
        assertEquals(3L, NumberUtils.max(1L, 2L, 3L));
        assertEquals((short) 1, NumberUtils.min((short) 2, (short) 1, (short) 3));
        assertEquals((short) 3, NumberUtils.max((short) 2, (short) 1, (short) 3));
        assertEquals((byte) 1, NumberUtils.min((byte) 1, (byte) 2, (byte) 3));
        assertEquals((byte) 3, NumberUtils.max((byte) 1, (byte) 2, (byte) 3));
        assertEquals(1.0d, NumberUtils.min(2.0d, 1.0d, 3.0d), 0.0001d);
        assertEquals(3.0d, NumberUtils.max(2.0d, 1.0d, 3.0d), 0.0001d);
        assertEquals(1.0f, NumberUtils.min(3.0f, 2.0f, 1.0f), 0.0001f);
        assertEquals(3.0f, NumberUtils.max(3.0f, 2.0f, 1.0f), 0.0001f);
    }

    // Tests compare for float and double including NaN and negative zero
    @Test
    public void testCompare_floatAndDouble_handlesSpecialValues() {
        assertEquals(-1, NumberUtils.compare(1.0, 2.0));
        assertEquals(1, NumberUtils.compare(2.0, 1.0));
        assertEquals(0, NumberUtils.compare(1.0, 1.0));
        assertEquals(-1, NumberUtils.compare(-0.0, 0.0));
        assertEquals(1, NumberUtils.compare(Double.NaN, 1.0));
        assertEquals(0, NumberUtils.compare(Double.NaN, Double.NaN));

        assertEquals(-1, NumberUtils.compare(1.0f, 2.0f));
        assertEquals(1, NumberUtils.compare(2.0f, 1.0f));
        assertEquals(0, NumberUtils.compare(1.0f, 1.0f));
        assertEquals(-1, NumberUtils.compare(-0.0f, 0.0f));
        assertEquals(1, NumberUtils.compare(Float.NaN, 1.0f));
        assertEquals(0, NumberUtils.compare(Float.NaN, Float.NaN));
    }

    // Tests array equals methods
    @Test
    public void testEquals_arrays_returnsExpected() {
        assertTrue(NumberUtils.equals((int[]) null, (int[]) null));
        assertFalse(NumberUtils.equals(new int[] { 1 }, null));
        assertFalse(NumberUtils.equals(new int[] { 1 }, new int[] { 1, 2 }));
        assertTrue(NumberUtils.equals(new int[] { 1, 2 }, new int[] { 1, 2 }));
        assertFalse(NumberUtils.equals(new int[] { 1, 2 }, new int[] { 1, 3 }));

        assertTrue(NumberUtils.equals(new double[] { Double.NaN, 1.0 }, new double[] { Double.NaN, 1.0 }));
        assertFalse(NumberUtils.equals(new double[] { -0.0 }, new double[] { 0.0 }));
        assertTrue(NumberUtils.equals(new float[] { Float.NaN, 1.0f }, new float[] { Float.NaN, 1.0f }));
    }
}