package org.apache.commons.lang3.math;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.junit.Test;
import static org.junit.Assert.*;

public class NumberUtilsTest {

    // Tests constructor instantiation
    @Test
    public void testConstructor_publicInstance_createsObject() {
        assertNotNull(new NumberUtils());
    }

    // Tests toInt with defaults and conversions
    @Test
    public void testToInt_variousInputs_returnsExpected() {
        assertEquals(0, NumberUtils.toInt(null));
        assertEquals(0, NumberUtils.toInt(""));
        assertEquals(123, NumberUtils.toInt("123"));
        assertEquals(5, NumberUtils.toInt("invalid", 5));
        assertEquals(5, NumberUtils.toInt(null, 5));
        assertEquals(10, NumberUtils.toInt("10", 5));
    }

    // Tests toLong with defaults and conversions
    @Test
    public void testToLong_variousInputs_returnsExpected() {
        assertEquals(0L, NumberUtils.toLong(null));
        assertEquals(0L, NumberUtils.toLong(""));
        assertEquals(123L, NumberUtils.toLong("123"));
        assertEquals(5L, NumberUtils.toLong("invalid", 5L));
        assertEquals(5L, NumberUtils.toLong(null, 5L));
        assertEquals(10L, NumberUtils.toLong("10", 5L));
    }

    // Tests toFloat with defaults and conversions
    @Test
    public void testToFloat_variousInputs_returnsExpected() {
        assertEquals(0.0f, NumberUtils.toFloat(null), 0.0001f);
        assertEquals(0.0f, NumberUtils.toFloat(""), 0.0001f);
        assertEquals(1.5f, NumberUtils.toFloat("1.5"), 0.0001f);
        assertEquals(2.5f, NumberUtils.toFloat("invalid", 2.5f), 0.0001f);
        assertEquals(2.5f, NumberUtils.toFloat(null, 2.5f), 0.0001f);
    }

    // Tests toDouble with defaults and conversions
    @Test
    public void testToDouble_variousInputs_returnsExpected() {
        assertEquals(0.0d, NumberUtils.toDouble(null), 0.0001d);
        assertEquals(0.0d, NumberUtils.toDouble(""), 0.0001d);
        assertEquals(1.5d, NumberUtils.toDouble("1.5"), 0.0001d);
        assertEquals(2.5d, NumberUtils.toDouble("invalid", 2.5d), 0.0001d);
        assertEquals(2.5d, NumberUtils.toDouble(null, 2.5d), 0.0001d);
    }

    // Tests toByte and toShort conversions
    @Test
    public void testToByteAndToShort_variousInputs_returnsExpected() {
        assertEquals((byte) 0, NumberUtils.toByte(null));
        assertEquals((byte) 0, NumberUtils.toByte(""));
        assertEquals((byte) 12, NumberUtils.toByte("12"));
        assertEquals((byte) 3, NumberUtils.toByte("invalid", (byte) 3));
        assertEquals((byte) 3, NumberUtils.toByte(null, (byte) 3));

        assertEquals((short) 0, NumberUtils.toShort(null));
        assertEquals((short) 0, NumberUtils.toShort(""));
        assertEquals((short) 123, NumberUtils.toShort("123"));
        assertEquals((short) 4, NumberUtils.toShort("invalid", (short) 4));
        assertEquals((short) 4, NumberUtils.toShort(null, (short) 4));
    }

    // Tests createNumber with integers, long, hex, and big integer
    @Test
    public void testCreateNumber_integerAndHex_returnsCorrectNumber() {
        assertNull(NumberUtils.createNumber(null));
        assertNull(NumberUtils.createNumber("--123"));
        assertEquals(Integer.valueOf(123), NumberUtils.createNumber("123"));
        assertEquals(Integer.valueOf(-123), NumberUtils.createNumber("-123"));
        assertEquals(Integer.valueOf(0x1a), NumberUtils.createNumber("0x1a"));
        assertEquals(Integer.valueOf(-0x1a), NumberUtils.createNumber("-0x1a"));
        assertEquals(Long.valueOf(2147483648L), NumberUtils.createNumber("2147483648"));
        assertEquals(Long.valueOf(123L), NumberUtils.createNumber("123L"));
        assertEquals(Long.valueOf(123L), NumberUtils.createNumber("123l"));
        assertEquals(new BigInteger("9223372036854775808"), NumberUtils.createNumber("9223372036854775808"));
        assertEquals(new BigInteger("9223372036854775808"), NumberUtils.createNumber("9223372036854775808L"));
    }

    // Tests createNumber with float, double, qualifiers, and scientific notation
    @Test
    public void testCreateNumber_floatingPointAndScientific_returnsCorrectNumber() {
        assertEquals(Float.valueOf(1.23f), NumberUtils.createNumber("1.23f"));
        assertEquals(Float.valueOf(1.23f), NumberUtils.createNumber("1.23F"));
        assertEquals(Double.valueOf(1.23d), NumberUtils.createNumber("1.23d"));
        assertEquals(Double.valueOf(1.23d), NumberUtils.createNumber("1.23D"));
        assertEquals(Double.valueOf(1.23), NumberUtils.createNumber("1.23"));
        assertEquals(Double.valueOf(1.23e4), NumberUtils.createNumber("1.23e4"));
        assertEquals(Double.valueOf(1.23E4), NumberUtils.createNumber("1.23E4"));
        assertEquals(Float.valueOf(0.0f), NumberUtils.createNumber("0.0f"));
        assertEquals(Double.valueOf(0.0d), NumberUtils.createNumber("0.0d"));
        assertEquals(new BigDecimal("1.2345678901234567890123456789"), NumberUtils.createNumber("1.2345678901234567890123456789"));
        assertEquals(new BigDecimal("1.2345678901234567890123456789"), NumberUtils.createNumber("1.2345678901234567890123456789D"));
    }

    // Tests createNumber edge case of trailing decimal point (Defects4J Lang-36)
    @Test
    public void testCreateNumber_trailingDecimalPoint_returnsCorrectNumber() {
        assertEquals(Float.valueOf(2.0f), NumberUtils.createNumber("2."));
    }

    // Tests createNumber with blank string throws NumberFormatException
    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_blankString_throwsNumberFormatException() {
        NumberUtils.createNumber("   ");
    }

    // Tests createNumber with invalid format throws NumberFormatException
    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_invalidFormat_throwsNumberFormatException() {
        NumberUtils.createNumber("1.2.3");
    }

    // Tests direct creator methods (createFloat, createDouble, createInteger, createLong, createBigInteger, createBigDecimal)
    @Test
    public void testDirectCreators_validAndNull_returnsExpected() {
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
    public void testIsDigits_variousStrings_returnsCorrectBoolean() {
        assertFalse(NumberUtils.isDigits(null));
        assertFalse(NumberUtils.isDigits(""));
        assertFalse(NumberUtils.isDigits("12a34"));
        assertFalse(NumberUtils.isDigits("-1234"));
        assertTrue(NumberUtils.isDigits("12345"));
    }

    // Tests isNumber method for various valid and invalid formats
    @Test
    public void testIsNumber_variousFormats_returnsCorrectBoolean() {
        assertFalse(NumberUtils.isNumber(null));
        assertFalse(NumberUtils.isNumber(""));
        assertFalse(NumberUtils.isNumber("0x"));
        assertFalse(NumberUtils.isNumber("0xxyz"));
        assertTrue(NumberUtils.isNumber("0x123A"));
        assertTrue(NumberUtils.isNumber("-0x123A"));
        assertTrue(NumberUtils.isNumber("12345"));
        assertTrue(NumberUtils.isNumber("-12345"));
        assertTrue(NumberUtils.isNumber("12.34"));
        assertTrue(NumberUtils.isNumber("-12.34"));
        assertTrue(NumberUtils.isNumber("12.34e5"));
        assertTrue(NumberUtils.isNumber("12.34E-5"));
        assertTrue(NumberUtils.isNumber("12.34f"));
        assertTrue(NumberUtils.isNumber("12.34d"));
        assertTrue(NumberUtils.isNumber("1234L"));
        assertFalse(NumberUtils.isNumber("1234e5L"));
        assertFalse(NumberUtils.isNumber("1.2.3"));
        assertFalse(NumberUtils.isNumber("1e2e3"));
        assertFalse(NumberUtils.isNumber("1e"));
        assertFalse(NumberUtils.isNumber("1e+"));
        assertFalse(NumberUtils.isNumber("123a"));
    }

    // Tests isNumber with trailing decimal point (Defects4J Lang-36)
    @Test
    public void testIsNumber_trailingDecimalPoint_returnsTrue() {
        assertTrue(NumberUtils.isNumber("2."));
        assertTrue(NumberUtils.isNumber(".2"));
        assertFalse(NumberUtils.isNumber("."));
    }

    // Tests min and max for primitive arrays
    @Test
    public void testMinMax_primitiveArrays_returnsCorrectExtremes() {
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

        assertEquals(1.0f, NumberUtils.min(new float[]{3.0f, 1.0f, 2.0f}), 0.0001f);
        assertEquals(3.0f, NumberUtils.max(new float[]{3.0f, 1.0f, 2.0f}), 0.0001f);
    }

    // Tests min and max with NaN in double and float arrays
    @Test
    public void testMinMax_floatingPointWithNaN_returnsNaN() {
        assertTrue(Double.isNaN(NumberUtils.min(new double[]{1.0d, Double.NaN, 2.0d})));
        assertTrue(Double.isNaN(NumberUtils.max(new double[]{1.0d, Double.NaN, 2.0d})));
        assertTrue(Float.isNaN(NumberUtils.min(new float[]{1.0f, Float.NaN, 2.0f})));
        assertTrue(Float.isNaN(NumberUtils.max(new float[]{1.0f, Float.NaN, 2.0f})));
    }

    // Tests 3-parameter min and max methods
    @Test
    public void testMinMax_threeParameters_returnsCorrectExtremes() {
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

    // Tests array validation for null or empty inputs
    @Test(expected = IllegalArgumentException.class)
    public void testMin_nullArray_throwsIllegalArgumentException() {
        NumberUtils.min((int[]) null);
    }

    // Tests array validation for empty array
    @Test(expected = IllegalArgumentException.class)
    public void testMax_emptyArray_throwsIllegalArgumentException() {
        NumberUtils.max(new int[]{});
    }
}