package org.apache.commons.lang3.math;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link NumberUtils}.
 */
public class NumberUtilsTest {

    // Tests constructor instantiation
    @Test
    public void testConstructor_defaultConstructor_instantiatedSuccessfully() {
        assertNotNull(new NumberUtils());
    }

    // Tests toInt with valid input, null, and default values
    @Test
    public void testToInt_variousInputs_returnsExpectedInt() {
        assertEquals(0, NumberUtils.toInt(null));
        assertEquals(0, NumberUtils.toInt(""));
        assertEquals(123, NumberUtils.toInt("123"));
        assertEquals(5, NumberUtils.toInt("invalid", 5));
        assertEquals(10, NumberUtils.toInt(null, 10));
    }

    // Tests toLong with valid input, null, and default values
    @Test
    public void testToLong_variousInputs_returnsExpectedLong() {
        assertEquals(0L, NumberUtils.toLong(null));
        assertEquals(0L, NumberUtils.toLong(""));
        assertEquals(123456789012L, NumberUtils.toLong("123456789012"));
        assertEquals(99L, NumberUtils.toLong("invalid", 99L));
        assertEquals(77L, NumberUtils.toLong(null, 77L));
    }

    // Tests toFloat and toDouble with valid input, null, and default values
    @Test
    public void testToFloatAndToDouble_variousInputs_returnsExpectedValues() {
        assertEquals(0.0f, NumberUtils.toFloat(null), 0.0001f);
        assertEquals(1.5f, NumberUtils.toFloat("1.5"), 0.0001f);
        assertEquals(2.5f, NumberUtils.toFloat("invalid", 2.5f), 0.0001f);

        assertEquals(0.0d, NumberUtils.toDouble(null), 0.0001d);
        assertEquals(1.5d, NumberUtils.toDouble("1.5"), 0.0001d);
        assertEquals(3.5d, NumberUtils.toDouble("invalid", 3.5d), 0.0001d);
    }

    // Tests toByte and toShort with valid input, null, and default values
    @Test
    public void testToByteAndToShort_variousInputs_returnsExpectedValues() {
        assertEquals((byte) 0, NumberUtils.toByte(null));
        assertEquals((byte) 12, NumberUtils.toByte("12"));
        assertEquals((byte) 7, NumberUtils.toByte("invalid", (byte) 7));

        assertEquals((short) 0, NumberUtils.toShort(null));
        assertEquals((short) 1234, NumberUtils.toShort("1234"));
        assertEquals((short) 9, NumberUtils.toShort("invalid", (short) 9));
    }

    // Tests createNumber with null and blank inputs
    @Test
    public void testCreateNumber_nullAndBlank_returnsNullOrThrowsException() {
        assertNull(NumberUtils.createNumber(null));
    }

    // Tests createNumber blank input exception
    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_blankString_throwsNumberFormatException() {
        NumberUtils.createNumber("   ");
    }

    // Tests createNumber with hexadecimal formats
    @Test
    public void testCreateNumber_hexPrefixes_returnsCorrectNumberTypes() {
        assertEquals(Integer.valueOf(255), NumberUtils.createNumber("0xFF"));
        assertEquals(Integer.valueOf(255), NumberUtils.createNumber("0XFF"));
        assertEquals(Integer.valueOf(-255), NumberUtils.createNumber("-0xFF"));
        assertEquals(Integer.valueOf(255), NumberUtils.createNumber("#FF"));
        assertEquals(Integer.valueOf(-255), NumberUtils.createNumber("-#FF"));
        assertEquals(Long.valueOf(0x1234567890L), NumberUtils.createNumber("0x1234567890"));
        assertEquals(new BigInteger("123456789012345678", 16), NumberUtils.createNumber("0x123456789012345678"));
    }

    // Tests createNumber with type qualifiers (l, f, d)
    @Test
    public void testCreateNumber_typeQualifiers_returnsCorrectTypes() {
        assertEquals(Long.valueOf(12345L), NumberUtils.createNumber("12345L"));
        assertEquals(Long.valueOf(12345L), NumberUtils.createNumber("12345l"));
        assertEquals(Float.valueOf(1.234f), NumberUtils.createNumber("1.234f"));
        assertEquals(Float.valueOf(1.234f), NumberUtils.createNumber("1.234F"));
        assertEquals(Double.valueOf(1.234d), NumberUtils.createNumber("1.234d"));
        assertEquals(Double.valueOf(1.234d), NumberUtils.createNumber("1.234D"));
        assertEquals(new BigInteger("9223372036854775808"), NumberUtils.createNumber("9223372036854775808L"));
    }

    // Tests createNumber without type qualifiers
    @Test
    public void testCreateNumber_noTypeQualifier_returnsAppropriateNumber() {
        assertEquals(Integer.valueOf(123), NumberUtils.createNumber("123"));
        assertEquals(Long.valueOf(2147483648L), NumberUtils.createNumber("2147483648"));
        assertEquals(new BigInteger("9223372036854775808"), NumberUtils.createNumber("9223372036854775808"));
        assertEquals(Float.valueOf("1.23"), NumberUtils.createNumber("1.23"));
        assertEquals(Double.valueOf("3.40282354e+38"), NumberUtils.createNumber("3.40282354e+38"));
    }

    // Tests createNumber with invalid formatted strings
    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_invalidExponentPlacement_throwsNumberFormatException() {
        NumberUtils.createNumber("1.2.3e4");
    }

    // Tests createFloat, createDouble, createInteger, createLong helper methods
    @Test
    public void testCreatePrimitiveWrappers_validAndNull_returnsExpected() {
        assertNull(NumberUtils.createFloat(null));
        assertEquals(Float.valueOf(1.5f), NumberUtils.createFloat("1.5"));

        assertNull(NumberUtils.createDouble(null));
        assertEquals(Double.valueOf(2.5d), NumberUtils.createDouble("2.5"));

        assertNull(NumberUtils.createInteger(null));
        assertEquals(Integer.valueOf(10), NumberUtils.createInteger("10"));
        assertEquals(Integer.valueOf(16), NumberUtils.createInteger("0x10"));

        assertNull(NumberUtils.createLong(null));
        assertEquals(Long.valueOf(100L), NumberUtils.createLong("100"));
        assertEquals(Long.valueOf(16L), NumberUtils.createLong("0x10"));
    }

    // Tests createBigInteger and createBigDecimal
    @Test
    public void testCreateBigIntegerAndBigDecimal_variousInputs_returnsExpected() {
        assertNull(NumberUtils.createBigInteger(null));
        assertEquals(new BigInteger("123"), NumberUtils.createBigInteger("123"));
        assertEquals(new BigInteger("-123"), NumberUtils.createBigInteger("-123"));
        assertEquals(new BigInteger("16"), NumberUtils.createBigInteger("0x10"));
        assertEquals(new BigInteger("8"), NumberUtils.createBigInteger("010"));
        assertEquals(new BigInteger("16"), NumberUtils.createBigInteger("#10"));

        assertNull(NumberUtils.createBigDecimal(null));
        assertEquals(new BigDecimal("123.456"), NumberUtils.createBigDecimal("123.456"));
    }

    // Tests createBigDecimal with invalid double minus
    @Test(expected = NumberFormatException.class)
    public void testCreateBigDecimal_doubleMinus_throwsNumberFormatException() {
        NumberUtils.createBigDecimal("--1.5");
    }

    // Tests min and max for primitive arrays
    @Test
    public void testMinAndMax_primitiveArrays_returnsMinAndMax() {
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

        assertEquals(1.0f, NumberUtils.min(new float[]{3.0f, 1.0f, 2.0f}), 0.0001f);
        assertEquals(3.0f, NumberUtils.max(new float[]{3.0f, 1.0f, 2.0f}), 0.0001f);
    }

    // Tests min and max for floating arrays containing NaN
    @Test
    public void testMinAndMax_arraysWithNaN_returnsNaN() {
        assertTrue(Double.isNaN(NumberUtils.min(new double[]{1.0d, Double.NaN, 2.0d})));
        assertTrue(Double.isNaN(NumberUtils.max(new double[]{1.0d, Double.NaN, 2.0d})));

        assertTrue(Float.isNaN(NumberUtils.min(new float[]{1.0f, Float.NaN, 2.0f})));
        assertTrue(Float.isNaN(NumberUtils.max(new float[]{1.0f, Float.NaN, 2.0f})));
    }

    // Tests array validation for null input
    @Test(expected = IllegalArgumentException.class)
    public void testMin_nullArray_throwsIllegalArgumentException() {
        NumberUtils.min((int[]) null);
    }

    // Tests array validation for empty array input
    @Test(expected = IllegalArgumentException.class)
    public void testMax_emptyArray_throwsIllegalArgumentException() {
        NumberUtils.max(new int[]{});
    }

    // Tests three-argument min and max methods
    @Test
    public void testMinAndMax_threeArguments_returnsCorrectExtremes() {
        assertEquals(1L, NumberUtils.min(3L, 1L, 2L));
        assertEquals(3L, NumberUtils.max(1L, 3L, 2L));

        assertEquals(1, NumberUtils.min(2, 3, 1));
        assertEquals(3, NumberUtils.max(2, 1, 3));

        assertEquals((short) 1, NumberUtils.min((short) 3, (short) 1, (short) 2));
        assertEquals((short) 3, NumberUtils.max((short) 1, (short) 2, (short) 3));

        assertEquals((byte) 1, NumberUtils.min((byte) 2, (byte) 1, (byte) 3));
        assertEquals((byte) 3, NumberUtils.max((byte) 3, (byte) 2, (byte) 1));

        assertEquals(1.0d, NumberUtils.min(3.0d, 1.0d, 2.0d), 0.0001d);
        assertEquals(3.0d, NumberUtils.max(1.0d, 3.0d, 2.0d), 0.0001d);

        assertEquals(1.0f, NumberUtils.min(2.0f, 3.0f, 1.0f), 0.0001f);
        assertEquals(3.0f, NumberUtils.max(2.0f, 1.0f, 3.0f), 0.0001f);
    }

    // Tests isDigits method with various strings
    @Test
    public void testIsDigits_variousStrings_returnsTrueForDigitsOnly() {
        assertFalse(NumberUtils.isDigits(null));
        assertFalse(NumberUtils.isDigits(""));
        assertFalse(NumberUtils.isDigits("12a34"));
        assertFalse(NumberUtils.isDigits("-123"));
        assertTrue(NumberUtils.isDigits("12345"));
    }

    // Tests isNumber method with valid and invalid representations
    @Test
    public void testIsNumber_variousFormats_validatesCorrectly() {
        assertFalse(NumberUtils.isNumber(null));
        assertFalse(NumberUtils.isNumber(""));
        assertFalse(NumberUtils.isNumber("0x"));
        assertFalse(NumberUtils.isNumber("0xxyz"));
        assertFalse(NumberUtils.isNumber("1.2.3"));
        assertFalse(NumberUtils.isNumber("1e2e3"));
        assertFalse(NumberUtils.isNumber("1e"));
        assertFalse(NumberUtils.isNumber("--1"));
        assertFalse(NumberUtils.isNumber("123L2"));

        assertTrue(NumberUtils.isNumber("123"));
        assertTrue(NumberUtils.isNumber("-123"));
        assertTrue(NumberUtils.isNumber("0x1aF"));
        assertTrue(NumberUtils.isNumber("-0x1AF"));
        assertTrue(NumberUtils.isNumber("1.234"));
        assertTrue(NumberUtils.isNumber("1.234e+5"));
        assertTrue(NumberUtils.isNumber("1.234E-5"));
        assertTrue(NumberUtils.isNumber("1234L"));
        assertTrue(NumberUtils.isNumber("1.23f"));
        assertTrue(NumberUtils.isNumber("1.23d"));
        assertTrue(NumberUtils.isNumber(".5"));
        assertTrue(NumberUtils.isNumber("1."));
    }
}