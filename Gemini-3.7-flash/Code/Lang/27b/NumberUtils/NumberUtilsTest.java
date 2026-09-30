package org.apache.commons.lang3.math;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.junit.Test;
import static org.junit.Assert.*;

public class NumberUtilsTest {

    // Tests constructor
    @Test
    public void testConstructor_instantiation_notNull() {
        assertNotNull(new NumberUtils());
    }

    // Tests toInt with defaults and invalid values
    @Test
    public void testToInt_variousInputs_returnsExpected() {
        assertEquals(0, NumberUtils.toInt(null));
        assertEquals(0, NumberUtils.toInt(""));
        assertEquals(1, NumberUtils.toInt("1"));
        assertEquals(5, NumberUtils.toInt(null, 5));
        assertEquals(5, NumberUtils.toInt("invalid", 5));
        assertEquals(10, NumberUtils.toInt("10", 5));
    }

    // Tests toLong with defaults and invalid values
    @Test
    public void testToLong_variousInputs_returnsExpected() {
        assertEquals(0L, NumberUtils.toLong(null));
        assertEquals(0L, NumberUtils.toLong(""));
        assertEquals(123L, NumberUtils.toLong("123"));
        assertEquals(5L, NumberUtils.toLong(null, 5L));
        assertEquals(5L, NumberUtils.toLong("invalid", 5L));
        assertEquals(10L, NumberUtils.toLong("10", 5L));
    }

    // Tests toFloat, toDouble, toByte, toShort
    @Test
    public void testPrimitiveConversions_withDefaults_returnsExpected() {
        assertEquals(0.0f, NumberUtils.toFloat(null), 0.0001f);
        assertEquals(1.5f, NumberUtils.toFloat("1.5", 0.0f), 0.0001f);
        assertEquals(2.5f, NumberUtils.toFloat("invalid", 2.5f), 0.0001f);

        assertEquals(0.0d, NumberUtils.toDouble(null), 0.0001d);
        assertEquals(1.5d, NumberUtils.toDouble("1.5", 0.0d), 0.0001d);
        assertEquals(2.5d, NumberUtils.toDouble("invalid", 2.5d), 0.0001d);

        assertEquals((byte) 0, NumberUtils.toByte(null));
        assertEquals((byte) 5, NumberUtils.toByte("invalid", (byte) 5));
        assertEquals((byte) 12, NumberUtils.toByte("12", (byte) 0));

        assertEquals((short) 0, NumberUtils.toShort(null));
        assertEquals((short) 5, NumberUtils.toShort("invalid", (short) 5));
        assertEquals((short) 123, NumberUtils.toShort("123", (short) 0));
    }

    // Tests createNumber with null and special prefixes
    @Test
    public void testCreateNumber_specialPrefixes_returnsCorrectType() {
        assertNull(NumberUtils.createNumber(null));
        assertNull(NumberUtils.createNumber("--123"));
        assertEquals(Integer.valueOf(16), NumberUtils.createNumber("0x10"));
        assertEquals(Integer.valueOf(-16), NumberUtils.createNumber("-0x10"));
    }

    // Tests createNumber with blank string throws NumberFormatException
    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_blankString_throwsException() {
        NumberUtils.createNumber("   ");
    }

    // Tests createNumber with valid integers, longs, and BigIntegers
    @Test
    public void testCreateNumber_integerTypes_returnsExpected() {
        assertEquals(Integer.valueOf(123), NumberUtils.createNumber("123"));
        assertEquals(Integer.valueOf(-123), NumberUtils.createNumber("-123"));
        assertEquals(Long.valueOf(123456789012L), NumberUtils.createNumber("123456789012"));
        assertEquals(new BigInteger("123456789012345678901234567890"), NumberUtils.createNumber("123456789012345678901234567890"));
    }

    // Tests createNumber with type qualifiers (l, f, d)
    @Test
    public void testCreateNumber_typeQualifiers_returnsCorrectType() {
        assertEquals(Long.valueOf(123L), NumberUtils.createNumber("123l"));
        assertEquals(Long.valueOf(123L), NumberUtils.createNumber("123L"));
        assertEquals(Float.valueOf(1.23f), NumberUtils.createNumber("1.23f"));
        assertEquals(Float.valueOf(1.23f), NumberUtils.createNumber("1.23F"));
        assertEquals(Double.valueOf(1.23d), NumberUtils.createNumber("1.23d"));
        assertEquals(Double.valueOf(1.23d), NumberUtils.createNumber("1.23D"));
        assertEquals(new BigInteger("123456789012345678901234567890"), NumberUtils.createNumber("123456789012345678901234567890L"));
    }

    // Tests createNumber with floating point numbers and exponents
    @Test
    public void testCreateNumber_floatingPointAndExponents_returnsExpected() {
        assertEquals(Float.valueOf(1.23f), NumberUtils.createNumber("1.23"));
        assertEquals(Double.valueOf(1.23e100), (Double) NumberUtils.createNumber("1.23e100"), 0.0001);
        assertEquals(new BigDecimal("1.23e1000"), NumberUtils.createNumber("1.23e1000"));
        assertEquals(Float.valueOf(0.0f), NumberUtils.createNumber("0.0"));
        assertEquals(Float.valueOf(0.0f), NumberUtils.createNumber("0.0f"));
        assertEquals(Double.valueOf(0.0d), NumberUtils.createNumber("0.0d"));
    }

    // Tests createNumber invalid inputs throw NumberFormatException
    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_invalidInput_throwsException() {
        NumberUtils.createNumber("1.2.3");
    }

    // Tests individual create methods (Float, Double, Integer, Long, BigInteger, BigDecimal)
    @Test
    public void testSpecificCreateMethods_validAndNull_returnsExpected() {
        assertNull(NumberUtils.createFloat(null));
        assertEquals(Float.valueOf(1.5f), NumberUtils.createFloat("1.5"));

        assertNull(NumberUtils.createDouble(null));
        assertEquals(Double.valueOf(1.5d), NumberUtils.createDouble("1.5"));

        assertNull(NumberUtils.createInteger(null));
        assertEquals(Integer.valueOf(10), NumberUtils.createInteger("10"));

        assertNull(NumberUtils.createLong(null));
        assertEquals(Long.valueOf(10L), NumberUtils.createLong("10"));

        assertNull(NumberUtils.createBigInteger(null));
        assertEquals(new BigInteger("100"), NumberUtils.createBigInteger("100"));

        assertNull(NumberUtils.createBigDecimal(null));
        assertEquals(new BigDecimal("100.5"), NumberUtils.createBigDecimal("100.5"));
    }

    // Tests createBigDecimal blank string throws NumberFormatException
    @Test(expected = NumberFormatException.class)
    public void testCreateBigDecimal_blankString_throwsException() {
        NumberUtils.createBigDecimal("  ");
    }

    // Tests min and max for long arrays
    @Test
    public void testMinMax_longArray_returnsCorrectMinMax() {
        long[] array = new long[] { 5L, 2L, 9L, -1L };
        assertEquals(-1L, NumberUtils.min(array));
        assertEquals(9L, NumberUtils.max(array));
    }

    // Tests min and max for int arrays
    @Test
    public void testMinMax_intArray_returnsCorrectMinMax() {
        int[] array = new int[] { 5, 2, 9, -1 };
        assertEquals(-1, NumberUtils.min(array));
        assertEquals(9, NumberUtils.max(array));
    }

    // Tests min and max for short, byte, float, double arrays
    @Test
    public void testMinMax_otherPrimitiveArrays_returnsCorrectMinMax() {
        short[] sArray = new short[] { 5, 2, 9, -1 };
        assertEquals((short) -1, NumberUtils.min(sArray));
        assertEquals((short) 9, NumberUtils.max(sArray));

        byte[] bArray = new byte[] { 5, 2, 9, -1 };
        assertEquals((byte) -1, NumberUtils.min(bArray));
        assertEquals((byte) 9, NumberUtils.max(bArray));

        double[] dArray = new double[] { 5.0, 2.0, Double.NaN, 9.0 };
        assertTrue(Double.isNaN(NumberUtils.min(dArray)));
        assertTrue(Double.isNaN(NumberUtils.max(dArray)));
        double[] dArray2 = new double[] { 5.0, 2.0, 9.0, -1.0 };
        assertEquals(-1.0, NumberUtils.min(dArray2), 0.0001);
        assertEquals(9.0, NumberUtils.max(dArray2), 0.0001);

        float[] fArray = new float[] { 5.0f, 2.0f, Float.NaN, 9.0f };
        assertTrue(Float.isNaN(NumberUtils.min(fArray)));
        assertTrue(Float.isNaN(NumberUtils.max(fArray)));
        float[] fArray2 = new float[] { 5.0f, 2.0f, 9.0f, -1.0f };
        assertEquals(-1.0f, NumberUtils.min(fArray2), 0.0001f);
        assertEquals(9.0f, NumberUtils.max(fArray2), 0.0001f);
    }

    // Tests array min with null array throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testMin_nullArray_throwsException() {
        NumberUtils.min((int[]) null);
    }

    // Tests array max with empty array throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testMax_emptyArray_throwsException() {
        NumberUtils.max(new int[0]);
    }

    // Tests 3-argument min and max methods
    @Test
    public void testMinMax_threeParams_returnsCorrectValue() {
        assertEquals(1L, NumberUtils.min(3L, 1L, 2L));
        assertEquals(3L, NumberUtils.max(1L, 3L, 2L));

        assertEquals(1, NumberUtils.min(3, 1, 2));
        assertEquals(3, NumberUtils.max(1, 3, 2));

        assertEquals((short) 1, NumberUtils.min((short) 3, (short) 1, (short) 2));
        assertEquals((short) 3, NumberUtils.max((short) 1, (short) 3, (short) 2));

        assertEquals((byte) 1, NumberUtils.min((byte) 3, (byte) 1, (byte) 2));
        assertEquals((byte) 3, NumberUtils.max((byte) 1, (byte) 3, (byte) 2));

        assertEquals(1.0, NumberUtils.min(3.0, 1.0, 2.0), 0.0001);
        assertEquals(3.0, NumberUtils.max(1.0, 3.0, 2.0), 0.0001);

        assertEquals(1.0f, NumberUtils.min(3.0f, 1.0f, 2.0f), 0.0001f);
        assertEquals(3.0f, NumberUtils.max(1.0f, 3.0f, 2.0f), 0.0001f);
    }

    // Tests isDigits method
    @Test
    public void testIsDigits_variousStrings_returnsExpected() {
        assertFalse(NumberUtils.isDigits(null));
        assertFalse(NumberUtils.isDigits(""));
        assertFalse(NumberUtils.isDigits("12a34"));
        assertFalse(NumberUtils.isDigits("-1234"));
        assertTrue(NumberUtils.isDigits("12345"));
    }

    // Tests isNumber method for various formats
    @Test
    public void testIsNumber_variousFormats_returnsExpected() {
        assertFalse(NumberUtils.isNumber(null));
        assertFalse(NumberUtils.isNumber(""));
        assertFalse(NumberUtils.isNumber("   "));
        assertFalse(NumberUtils.isNumber("0x"));
        assertFalse(NumberUtils.isNumber("0xxyz"));
        assertTrue(NumberUtils.isNumber("0x1234"));
        assertTrue(NumberUtils.isNumber("-0x1234"));
        assertTrue(NumberUtils.isNumber("1234"));
        assertTrue(NumberUtils.isNumber("-1234"));
        assertTrue(NumberUtils.isNumber("12.34"));
        assertTrue(NumberUtils.isNumber("-12.34"));
        assertTrue(NumberUtils.isNumber(".34"));
        assertTrue(NumberUtils.isNumber("1234L"));
        assertTrue(NumberUtils.isNumber("1234f"));
        assertTrue(NumberUtils.isNumber("1234d"));
        assertTrue(NumberUtils.isNumber("1.23e4"));
        assertTrue(NumberUtils.isNumber("1.23E+4"));
        assertTrue(NumberUtils.isNumber("1.23E-4"));
        assertFalse(NumberUtils.isNumber("1.2.3"));
        assertFalse(NumberUtils.isNumber("1.23e"));
        assertFalse(NumberUtils.isNumber("1.23e-"));
        assertFalse(NumberUtils.isNumber("1.23e4L"));
        assertFalse(NumberUtils.isNumber("junk"));
    }
}