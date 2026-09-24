package org.apache.commons.lang3.math;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.junit.Test;
import static org.junit.Assert.*;

public class NumberUtilsTest {

    // Tests createNumber with hex prefixes (0x, 0X, -0x, -0X)
    @Test
    public void testCreateNumber_hexPrefixes_returnsInteger() {
        assertEquals(Integer.valueOf(255), NumberUtils.createNumber("0xff"));
        assertEquals(Integer.valueOf(255), NumberUtils.createNumber("0Xff"));
        assertEquals(Integer.valueOf(255), NumberUtils.createNumber("0XFF"));
        assertEquals(Integer.valueOf(-255), NumberUtils.createNumber("-0xff"));
        assertEquals(Integer.valueOf(-255), NumberUtils.createNumber("-0XFF"));
    }

    // Tests createNumber with standard integer, long, and BigInteger strings
    @Test
    public void testCreateNumber_integersAndDecimals_returnsProperNumberTypes() {
        assertEquals(Integer.valueOf(123), NumberUtils.createNumber("123"));
        assertEquals(Long.valueOf(1234567890123L), NumberUtils.createNumber("1234567890123"));
        assertEquals(new BigInteger("123456789012345678901234567890"), NumberUtils.createNumber("123456789012345678901234567890"));
        assertEquals(Float.valueOf("1.23"), NumberUtils.createNumber("1.23"));
        assertEquals(Double.valueOf("1.2345678901234567"), NumberUtils.createNumber("1.2345678901234567"));
        assertEquals(new BigDecimal("1.23456789012345678901234567890"), NumberUtils.createNumber("1.23456789012345678901234567890"));
    }

    // Tests createNumber with explicit type qualifiers (l, L, f, F, d, D)
    @Test
    public void testCreateNumber_typeQualifiers_returnsSpecifiedType() {
        assertEquals(Long.valueOf(123), NumberUtils.createNumber("123l"));
        assertEquals(Long.valueOf(123), NumberUtils.createNumber("123L"));
        assertEquals(Float.valueOf(12.3f), NumberUtils.createNumber("12.3f"));
        assertEquals(Float.valueOf(12.3f), NumberUtils.createNumber("12.3F"));
        assertEquals(Double.valueOf(12.3d), NumberUtils.createNumber("12.3d"));
        assertEquals(Double.valueOf(12.3d), NumberUtils.createNumber("12.3D"));
    }

    // Tests createNumber with null, blank, and special marker values
    @Test
    public void testCreateNumber_nullAndSpecialCases_returnsExpected() {
        assertNull(NumberUtils.createNumber(null));
        assertNull(NumberUtils.createNumber("--123"));
    }

    // Tests createNumber with blank string throws NumberFormatException
    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_blankString_throwsNumberFormatException() {
        NumberUtils.createNumber("   ");
    }

    // Tests createNumber with invalid string throws NumberFormatException
    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_invalidFormat_throwsNumberFormatException() {
        NumberUtils.createNumber("123.45.67");
    }

    // Tests toInt with valid input, null, and default value fallbacks
    @Test
    public void testToInt_variousInputs_returnsParsedOrDefault() {
        assertEquals(123, NumberUtils.toInt("123"));
        assertEquals(0, NumberUtils.toInt(null));
        assertEquals(0, NumberUtils.toInt("invalid"));
        assertEquals(5, NumberUtils.toInt(null, 5));
        assertEquals(5, NumberUtils.toInt("invalid", 5));
        assertEquals(10, NumberUtils.toInt("10", 5));
    }

    // Tests toLong with valid input, null, and default value fallbacks
    @Test
    public void testToLong_variousInputs_returnsParsedOrDefault() {
        assertEquals(123L, NumberUtils.toLong("123"));
        assertEquals(0L, NumberUtils.toLong(null));
        assertEquals(0L, NumberUtils.toLong("invalid"));
        assertEquals(5L, NumberUtils.toLong(null, 5L));
        assertEquals(5L, NumberUtils.toLong("invalid", 5L));
        assertEquals(10L, NumberUtils.toLong("10", 5L));
    }

    // Tests toFloat with valid input, null, and default value fallbacks
    @Test
    public void testToFloat_variousInputs_returnsParsedOrDefault() {
        assertEquals(1.5f, NumberUtils.toFloat("1.5"), 0.001f);
        assertEquals(0.0f, NumberUtils.toFloat(null), 0.001f);
        assertEquals(0.0f, NumberUtils.toFloat("invalid"), 0.001f);
        assertEquals(2.5f, NumberUtils.toFloat(null, 2.5f), 0.001f);
        assertEquals(2.5f, NumberUtils.toFloat("invalid", 2.5f), 0.001f);
    }

    // Tests toDouble with valid input, null, and default value fallbacks
    @Test
    public void testToDouble_variousInputs_returnsParsedOrDefault() {
        assertEquals(1.5d, NumberUtils.toDouble("1.5"), 0.001d);
        assertEquals(0.0d, NumberUtils.toDouble(null), 0.001d);
        assertEquals(0.0d, NumberUtils.toDouble("invalid"), 0.001d);
        assertEquals(2.5d, NumberUtils.toDouble(null, 2.5d), 0.001d);
        assertEquals(2.5d, NumberUtils.toDouble("invalid", 2.5d), 0.001d);
    }

    // Tests toByte and toShort with valid input, null, and invalid strings
    @Test
    public void testToByteAndToShort_variousInputs_returnsParsedOrDefault() {
        assertEquals((byte) 10, NumberUtils.toByte("10"));
        assertEquals((byte) 0, NumberUtils.toByte(null));
        assertEquals((byte) 2, NumberUtils.toByte("invalid", (byte) 2));

        assertEquals((short) 100, NumberUtils.toShort("100"));
        assertEquals((short) 0, NumberUtils.toShort(null));
        assertEquals((short) 5, NumberUtils.toShort("invalid", (short) 5));
    }

    // Tests min and max helper methods for arrays of primitives
    @Test
    public void testMinAndMax_primitiveArrays_returnsExtremes() {
        assertEquals(1L, NumberUtils.min(new long[]{3L, 1L, 2L}));
        assertEquals(3L, NumberUtils.max(new long[]{3L, 1L, 2L}));

        assertEquals(1, NumberUtils.min(new int[]{3, 1, 2}));
        assertEquals(3, NumberUtils.max(new int[]{3, 1, 2}));

        assertEquals((short) 1, NumberUtils.min(new short[]{(short) 3, (short) 1, (short) 2}));
        assertEquals((short) 3, NumberUtils.max(new short[]{(short) 3, (short) 1, (short) 2}));

        assertEquals((byte) 1, NumberUtils.min(new byte[]{(byte) 3, (byte) 1, (byte) 2}));
        assertEquals((byte) 3, NumberUtils.max(new byte[]{(byte) 3, (byte) 1, (byte) 2}));

        assertEquals(1.0d, NumberUtils.min(new double[]{3.0d, 1.0d, 2.0d}), 0.001d);
        assertEquals(3.0d, NumberUtils.max(new double[]{3.0d, 1.0d, 2.0d}), 0.001d);

        assertEquals(1.0f, NumberUtils.min(new float[]{3.0f, 1.0f, 2.0f}), 0.001f);
        assertEquals(3.0f, NumberUtils.max(new float[]{3.0f, 1.0f, 2.0f}), 0.001f);
    }

    // Tests min and max array methods when input array is null
    @Test(expected = IllegalArgumentException.class)
    public void testMin_nullArray_throwsIllegalArgumentException() {
        NumberUtils.min((int[]) null);
    }

    // Tests min and max array methods when input array is empty
    @Test(expected = IllegalArgumentException.class)
    public void testMax_emptyArray_throwsIllegalArgumentException() {
        NumberUtils.max(new int[]{});
    }

    // Tests min and max methods taking three primitive arguments
    @Test
    public void testMinAndMax_threeArguments_returnsExtremes() {
        assertEquals(1L, NumberUtils.min(3L, 1L, 2L));
        assertEquals(3L, NumberUtils.max(1L, 3L, 2L));

        assertEquals(1, NumberUtils.min(3, 1, 2));
        assertEquals(3, NumberUtils.max(1, 3, 2));

        assertEquals((short) 1, NumberUtils.min((short) 3, (short) 1, (short) 2));
        assertEquals((short) 3, NumberUtils.max((short) 1, (short) 3, (short) 2));

        assertEquals((byte) 1, NumberUtils.min((byte) 3, (byte) 1, (byte) 2));
        assertEquals((byte) 3, NumberUtils.max((byte) 1, (byte) 3, (byte) 2));

        assertEquals(1.0d, NumberUtils.min(3.0d, 1.0d, 2.0d), 0.001d);
        assertEquals(3.0d, NumberUtils.max(1.0d, 3.0d, 2.0d), 0.001d);

        assertEquals(1.0f, NumberUtils.min(3.0f, 1.0f, 2.0f), 0.001f);
        assertEquals(3.0f, NumberUtils.max(1.0f, 3.0f, 2.0f), 0.001f);
    }

    // Tests isDigits with valid digits, invalid characters, empty, and null
    @Test
    public void testIsDigits_variousStrings_returnsExpectedBoolean() {
        assertTrue(NumberUtils.isDigits("12345"));
        assertFalse(NumberUtils.isDigits("123a45"));
        assertFalse(NumberUtils.isDigits(""));
        assertFalse(NumberUtils.isDigits(null));
    }

    // Tests isNumber with various numeric formats, exponents, hex, qualifiers, and invalid strings
    @Test
    public void testIsNumber_variousFormats_returnsExpectedBoolean() {
        assertTrue(NumberUtils.isNumber("123"));
        assertTrue(NumberUtils.isNumber("-123"));
        assertTrue(NumberUtils.isNumber("123.45"));
        assertTrue(NumberUtils.isNumber(".45"));
        assertTrue(NumberUtils.isNumber("1234L"));
        assertTrue(NumberUtils.isNumber("1234l"));
        assertTrue(NumberUtils.isNumber("1.23e4"));
        assertTrue(NumberUtils.isNumber("1.23E-4"));
        assertTrue(NumberUtils.isNumber("1.23f"));
        assertTrue(NumberUtils.isNumber("1.23d"));
        assertTrue(NumberUtils.isNumber("0x1a"));
        assertTrue(NumberUtils.isNumber("0X1A"));
        assertTrue(NumberUtils.isNumber("-0x1a"));
        assertTrue(NumberUtils.isNumber("-0X1A"));

        assertFalse(NumberUtils.isNumber(null));
        assertFalse(NumberUtils.isNumber(""));
        assertFalse(NumberUtils.isNumber("   "));
        assertFalse(NumberUtils.isNumber("0x"));
        assertFalse(NumberUtils.isNumber("0xxyz"));
        assertFalse(NumberUtils.isNumber("1.2.3"));
        assertFalse(NumberUtils.isNumber("1e2e3"));
        assertFalse(NumberUtils.isNumber("1.23e"));
        assertFalse(NumberUtils.isNumber("1.23e+"));
        assertFalse(NumberUtils.isNumber("123L45"));
        assertFalse(NumberUtils.isNumber("abc"));
    }

    // Tests instantiation of public constructor
    @Test
    public void testConstructor_createsInstance() {
        assertNotNull(new NumberUtils());
    }
}