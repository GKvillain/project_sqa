package org.apache.commons.lang.math;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.junit.Test;
import static org.junit.Assert.*;

public class NumberUtilsTest {

    // Tests constructor
    @Test
    public void testConstructor_defaultInstantiation_notNull() {
        assertNotNull(new NumberUtils());
    }

    // Tests defect Lang-58: parsing short long strings with type qualifier 'l' or 'L'
    @Test
    public void testCreateNumber_singleDigitLongQualifier_returnsLong() {
        assertEquals(Long.valueOf(1L), NumberUtils.createNumber("1l"));
        assertEquals(Long.valueOf(1L), NumberUtils.createNumber("1L"));
        assertEquals(Long.valueOf(0L), NumberUtils.createNumber("0L"));
        assertEquals(Long.valueOf(-1L), NumberUtils.createNumber("-1L"));
    }

    // Tests createNumber with standard Integer, Long, BigInteger, Float, Double, BigDecimal
    @Test
    public void testCreateNumber_variousFormats_returnsCorrectTypes() {
        assertEquals(Integer.valueOf(123), NumberUtils.createNumber("123"));
        assertEquals(Integer.valueOf(-123), NumberUtils.createNumber("-123"));
        assertEquals(Long.valueOf(123456789012L), NumberUtils.createNumber("123456789012"));
        assertEquals(new BigInteger("123456789012345678901234567890"), NumberUtils.createNumber("123456789012345678901234567890"));
        assertEquals(Float.valueOf(1.23f), NumberUtils.createNumber("1.23f"));
        assertEquals(Double.valueOf(1.23d), NumberUtils.createNumber("1.23d"));
        assertEquals(Float.valueOf(1.23e2f), NumberUtils.createNumber("1.23e2f"));
        assertEquals(new BigDecimal("1.23456789012345678901234567890"), NumberUtils.createNumber("1.23456789012345678901234567890"));
    }

    // Tests createNumber with hexadecimal numbers
    @Test
    public void testCreateNumber_hexadecimalStrings_returnsInteger() {
        assertEquals(Integer.valueOf(0x1a), NumberUtils.createNumber("0x1a"));
        assertEquals(Integer.valueOf(-0x1a), NumberUtils.createNumber("-0x1a"));
    }

    // Tests createNumber with null and blank inputs
    @Test
    public void testCreateNumber_nullAndSpecialCases_returnsNullOrValue() {
        assertNull(NumberUtils.createNumber(null));
        assertNull(NumberUtils.createNumber("--123"));
    }

    // Tests createNumber with blank string throwing NumberFormatException
    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_blankInput_throwsNumberFormatException() {
        NumberUtils.createNumber("   ");
    }

    // Tests createNumber with invalid format throwing NumberFormatException
    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_invalidQualifier_throwsNumberFormatException() {
        NumberUtils.createNumber("123a");
    }

    // Tests string conversion helper methods with default values
    @Test
    public void testToPrimitiveTypes_validAndInvalidInputs_returnsExpected() {
        assertEquals(10, NumberUtils.stringToInt("10"));
        assertEquals(5, NumberUtils.stringToInt("invalid", 5));
        assertEquals(10, NumberUtils.toInt("10"));
        assertEquals(5, NumberUtils.toInt(null, 5));
        assertEquals(10L, NumberUtils.toLong("10"));
        assertEquals(5L, NumberUtils.toLong("invalid", 5L));
        assertEquals(10.5f, NumberUtils.toFloat("10.5"), 0.001f);
        assertEquals(5.5f, NumberUtils.toFloat(null, 5.5f), 0.001f);
        assertEquals(10.5d, NumberUtils.toDouble("10.5"), 0.001d);
        assertEquals(5.5d, NumberUtils.toDouble("invalid", 5.5d), 0.001d);
    }

    // Tests specific create methods for Float, Double, Long, BigInteger, BigDecimal
    @Test
    public void testCreateSpecificTypes_validInputs_returnsObjects() {
        assertNull(NumberUtils.createFloat(null));
        assertEquals(Float.valueOf(1.5f), NumberUtils.createFloat("1.5"));
        assertNull(NumberUtils.createDouble(null));
        assertEquals(Double.valueOf(1.5d), NumberUtils.createDouble("1.5"));
        assertNull(NumberUtils.createInteger(null));
        assertEquals(Integer.valueOf(100), NumberUtils.createInteger("100"));
        assertNull(NumberUtils.createLong(null));
        assertEquals(Long.valueOf(100L), NumberUtils.createLong("100"));
        assertNull(NumberUtils.createBigInteger(null));
        assertEquals(new BigInteger("100"), NumberUtils.createBigInteger("100"));
        assertNull(NumberUtils.createBigDecimal(null));
        assertEquals(new BigDecimal("100.5"), NumberUtils.createBigDecimal("100.5"));
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

    // Tests isNumber method for various valid and invalid formats
    @Test
    public void testIsNumber_variousFormats_returnsBoolean() {
        assertFalse(NumberUtils.isNumber(null));
        assertFalse(NumberUtils.isNumber(""));
        assertTrue(NumberUtils.isNumber("123"));
        assertTrue(NumberUtils.isNumber("-123"));
        assertTrue(NumberUtils.isNumber("123.45"));
        assertTrue(NumberUtils.isNumber("1.23e-4"));
        assertTrue(NumberUtils.isNumber("123L"));
        assertTrue(NumberUtils.isNumber("123.4f"));
        assertTrue(NumberUtils.isNumber("123.4d"));
        assertTrue(NumberUtils.isNumber("0x1A"));
        assertFalse(NumberUtils.isNumber("0x"));
        assertFalse(NumberUtils.isNumber("123e"));
        assertFalse(NumberUtils.isNumber("123e+"));
        assertFalse(NumberUtils.isNumber("1.2.3"));
        assertFalse(NumberUtils.isNumber("abc"));
    }

    // Tests min and max for 3 parameters across long, int, short, byte, double, float
    @Test
    public void testMinMax_threeParams_returnsExtremes() {
        assertEquals(1L, NumberUtils.min(1L, 2L, 3L));
        assertEquals(1L, NumberUtils.min(3L, 1L, 2L));
        assertEquals(1L, NumberUtils.min(3L, 2L, 1L));
        assertEquals(3L, NumberUtils.max(1L, 2L, 3L));
        assertEquals(3L, NumberUtils.max(3L, 1L, 2L));
        assertEquals(3L, NumberUtils.max(2L, 3L, 1L));

        assertEquals(1, NumberUtils.min(1, 2, 3));
        assertEquals(3, NumberUtils.max(1, 2, 3));

        assertEquals((short) 1, NumberUtils.min((short) 1, (short) 2, (short) 3));
        assertEquals((short) 3, NumberUtils.max((short) 1, (short) 2, (short) 3));

        assertEquals((byte) 1, NumberUtils.min((byte) 1, (byte) 2, (byte) 3));
        assertEquals((byte) 3, NumberUtils.max((byte) 1, (byte) 2, (byte) 3));

        assertEquals(1.0d, NumberUtils.min(1.0d, 2.0d, 3.0d), 0.001d);
        assertEquals(3.0d, NumberUtils.max(1.0d, 2.0d, 3.0d), 0.001d);

        assertEquals(1.0f, NumberUtils.min(1.0f, 2.0f, 3.0f), 0.001f);
        assertEquals(3.0f, NumberUtils.max(1.0f, 2.0f, 3.0f), 0.001f);
    }

    // Tests min and max for arrays
    @Test
    public void testMinMax_arrays_returnsExtremes() {
        assertEquals(1L, NumberUtils.min(new long[]{3L, 1L, 2L}));
        assertEquals(3L, NumberUtils.max(new long[]{3L, 1L, 2L}));

        assertEquals(1, NumberUtils.min(new int[]{3, 1, 2}));
        assertEquals(3, NumberUtils.max(new int[]{3, 1, 2}));

        assertEquals((short) 1, NumberUtils.min(new short[]{3, 1, 2}));
        assertEquals((short) 3, NumberUtils.max(new short[]{3, 1, 2}));

        assertEquals((byte) 1, NumberUtils.min(new byte[]{3, 1, 2}));
        assertEquals((byte) 3, NumberUtils.max(new byte[]{3, 1, 2}));

        assertEquals(1.0d, NumberUtils.min(new double[]{3.0d, 1.0d, 2.0d}), 0.001d);
        assertEquals(3.0d, NumberUtils.max(new double[]{3.0d, 1.0d, 2.0d}), 0.001d);

        assertEquals(1.0f, NumberUtils.min(new float[]{3.0f, 1.0f, 2.0f}), 0.001f);
        assertEquals(3.0f, NumberUtils.max(new float[]{3.0f, 1.0f, 2.0f}), 0.001f);
    }

    // Tests array min with null array throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testMin_nullArray_throwsIllegalArgumentException() {
        NumberUtils.min((int[]) null);
    }

    // Tests array max with empty array throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testMax_emptyArray_throwsIllegalArgumentException() {
        NumberUtils.max(new int[]{});
    }

    // Tests compare methods for double and float including NaN, -0.0, and +0.0
    @Test
    public void testCompare_doubleAndFloatSpecialValues_returnsOrdering() {
        assertEquals(0, NumberUtils.compare(1.0d, 1.0d));
        assertEquals(-1, NumberUtils.compare(1.0d, 2.0d));
        assertEquals(1, NumberUtils.compare(2.0d, 1.0d));
        assertEquals(-1, NumberUtils.compare(-0.0d, 0.0d));
        assertEquals(1, NumberUtils.compare(Double.NaN, 100.0d));
        assertEquals(0, NumberUtils.compare(Double.NaN, Double.NaN));

        assertEquals(0, NumberUtils.compare(1.0f, 1.0f));
        assertEquals(-1, NumberUtils.compare(1.0f, 2.0f));
        assertEquals(1, NumberUtils.compare(2.0f, 1.0f));
        assertEquals(-1, NumberUtils.compare(-0.0f, 0.0f));
        assertEquals(1, NumberUtils.compare(Float.NaN, 100.0f));
        assertEquals(0, NumberUtils.compare(Float.NaN, Float.NaN));
    }

    // Tests equals methods for primitive arrays
    @Test
    public void testEquals_primitiveArrays_returnsBoolean() {
        assertTrue(NumberUtils.equals((byte[]) null, (byte[]) null));
        assertFalse(NumberUtils.equals(new byte[]{1}, null));
        assertFalse(NumberUtils.equals(new byte[]{1}, new byte[]{1, 2}));
        assertTrue(NumberUtils.equals(new byte[]{1, 2}, new byte[]{1, 2}));
        assertFalse(NumberUtils.equals(new byte[]{1, 2}, new byte[]{1, 3}));

        assertTrue(NumberUtils.equals(new short[]{1, 2}, new short[]{1, 2}));
        assertTrue(NumberUtils.equals(new int[]{1, 2}, new int[]{1, 2}));
        assertTrue(NumberUtils.equals(new long[]{1L, 2L}, new long[]{1L, 2L}));
        assertTrue(NumberUtils.equals(new float[]{1.0f, 2.0f}, new float[]{1.0f, 2.0f}));
        assertTrue(NumberUtils.equals(new double[]{1.0d, 2.0d}, new double[]{1.0d, 2.0d}));
    }
}