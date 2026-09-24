package org.apache.commons.math.fraction;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.apache.commons.math.exception.MathIllegalArgumentException;
import org.apache.commons.math.exception.NullArgumentException;
import org.apache.commons.math.exception.ZeroException;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class BigFractionTest {

    // Tests doubleValue with large numbers that exceed Double range (Defects4J Math-36)
    @Test
    public void testDoubleValue_largeNumeratorAndDenominator_returnsCorrectDouble() {
        BigInteger num = BigInteger.ONE.shiftLeft(1050);
        BigInteger den = BigInteger.ONE.shiftLeft(1040);
        BigFraction fraction = new BigFraction(num, den);
        assertEquals(1024.0, fraction.doubleValue(), 1e-10);
    }

    // Tests floatValue with large numbers that exceed Float range (Defects4J Math-36)
    @Test
    public void testFloatValue_largeNumeratorAndDenominator_returnsCorrectFloat() {
        BigInteger num = BigInteger.ONE.shiftLeft(200);
        BigInteger den = BigInteger.ONE.shiftLeft(190);
        BigFraction fraction = new BigFraction(num, den);
        assertEquals(1024.0f, fraction.floatValue(), 1e-5f);
    }

    // Tests constructor with BigInteger zero denominator throwing exception
    @Test(expected = ZeroException.class)
    public void testConstructor_zeroDenominator_throwsZeroException() {
        new BigFraction(BigInteger.ONE, BigInteger.ZERO);
    }

    // Tests constructor with null numerator throwing exception
    @Test(expected = NullArgumentException.class)
    public void testConstructor_nullNumerator_throwsNullArgumentException() {
        new BigFraction(null, BigInteger.ONE);
    }

    // Tests constructor with double NaN throwing exception
    @Test(expected = MathIllegalArgumentException.class)
    public void testConstructor_doubleNaN_throwsMathIllegalArgumentException() {
        new BigFraction(Double.NaN);
    }

    // Tests constructor with double Infinite throwing exception
    @Test(expected = MathIllegalArgumentException.class)
    public void testConstructor_doubleInfinite_throwsMathIllegalArgumentException() {
        new BigFraction(Double.POSITIVE_INFINITY);
    }

    // Tests constructor and reduction with negative denominator
    @Test
    public void testConstructor_negativeDenominator_normalizesSignAndReduces() {
        BigFraction fraction = new BigFraction(6, -8);
        assertEquals(BigInteger.valueOf(-3), fraction.getNumerator());
        assertEquals(BigInteger.valueOf(4), fraction.getDenominator());
        assertEquals(-3, fraction.getNumeratorAsInt());
        assertEquals(4, fraction.getDenominatorAsInt());
    }

    // Tests constructor with zero numerator
    @Test
    public void testConstructor_zeroNumerator_normalizesToOneDenominator() {
        BigFraction fraction = new BigFraction(0, 5);
        assertEquals(BigInteger.ZERO, fraction.getNumerator());
        assertEquals(BigInteger.ONE, fraction.getDenominator());
    }

    // Tests getReducedFraction factory method
    @Test
    public void testGetReducedFraction_validInputs_returnsReducedFraction() {
        BigFraction fraction = BigFraction.getReducedFraction(0, 5);
        assertEquals(BigFraction.ZERO, fraction);

        BigFraction fraction2 = BigFraction.getReducedFraction(10, 15);
        assertEquals(BigFraction.TWO_THIRDS, fraction2);
    }

    // Tests add operation with another BigFraction and zero
    @Test
    public void testAdd_fractionAndZero_returnsCorrectSum() {
        BigFraction f1 = new BigFraction(1, 3);
        BigFraction f2 = new BigFraction(2, 3);
        assertEquals(BigFraction.ONE, f1.add(f2));
        assertEquals(f1, f1.add(BigFraction.ZERO));
        assertEquals(new BigFraction(4, 3), f1.add(1));
        assertEquals(new BigFraction(7, 3), f1.add(2L));
        assertEquals(new BigFraction(4, 3), f1.add(BigInteger.ONE));
    }

    // Tests subtract operation
    @Test
    public void testSubtract_fractionAndInt_returnsCorrectDifference() {
        BigFraction f1 = new BigFraction(3, 4);
        BigFraction f2 = new BigFraction(1, 4);
        assertEquals(BigFraction.ONE_HALF, f1.subtract(f2));
        assertEquals(f1, f1.subtract(BigFraction.ZERO));
        assertEquals(new BigFraction(-1, 4), f1.subtract(1));
        assertEquals(new BigFraction(-1, 4), f1.subtract(1L));
        assertEquals(new BigFraction(-1, 4), f1.subtract(BigInteger.ONE));
    }

    // Tests multiply operation
    @Test
    public void testMultiply_validInputs_returnsCorrectProduct() {
        BigFraction f1 = new BigFraction(2, 3);
        BigFraction f2 = new BigFraction(3, 4);
        assertEquals(BigFraction.ONE_HALF, f1.multiply(f2));
        assertEquals(BigFraction.ZERO, f1.multiply(BigFraction.ZERO));
        assertEquals(BigFraction.TWO, f1.multiply(3));
        assertEquals(BigFraction.TWO, f1.multiply(3L));
        assertEquals(BigFraction.TWO, f1.multiply(BigInteger.valueOf(3)));
    }

    // Tests divide operation by zero throwing exception
    @Test(expected = ZeroException.class)
    public void testDivide_byZeroFraction_throwsZeroException() {
        BigFraction f = new BigFraction(1, 2);
        f.divide(BigFraction.ZERO);
    }

    // Tests divide operation with valid values
    @Test
    public void testDivide_validInputs_returnsCorrectQuotient() {
        BigFraction f1 = new BigFraction(1, 2);
        BigFraction f2 = new BigFraction(1, 4);
        assertEquals(BigFraction.TWO, f1.divide(f2));
        assertEquals(BigFraction.ONE_QUARTER, f1.divide(2));
        assertEquals(BigFraction.ONE_QUARTER, f1.divide(2L));
        assertEquals(BigFraction.ONE_QUARTER, f1.divide(BigInteger.valueOf(2)));
    }

    // Tests abs, negate, and reciprocal methods
    @Test
    public void testAbsNegateReciprocal_standardValues_returnsExpectedResults() {
        BigFraction f = new BigFraction(-3, 4);
        assertEquals(BigFraction.THREE_QUARTERS, f.abs());
        assertEquals(BigFraction.THREE_QUARTERS, f.negate());
        assertEquals(new BigFraction(-4, 3), f.reciprocal());

        BigFraction positive = new BigFraction(3, 4);
        assertEquals(positive, positive.abs());
    }

    // Tests pow operations with positive and negative exponents
    @Test
    public void testPow_variousExponents_returnsCorrectPowers() {
        BigFraction f = new BigFraction(2, 3);
        assertEquals(new BigFraction(4, 9), f.pow(2));
        assertEquals(new BigFraction(9, 4), f.pow(-2));
        assertEquals(new BigFraction(8, 27), f.pow(3L));
        assertEquals(new BigFraction(27, 8), f.pow(-3L));
        assertEquals(new BigFraction(4, 9), f.pow(BigInteger.valueOf(2)));
        assertEquals(new BigFraction(9, 4), f.pow(BigInteger.valueOf(-2)));
        assertEquals(4.0 / 9.0, f.pow(2.0), 1e-10);
    }

    // Tests conversions to BigDecimal, percentage, int, and long
    @Test
    public void testConversions_validFraction_returnsExpectedValues() {
        BigFraction f = new BigFraction(3, 2);
        assertEquals(new BigDecimal("1.5"), f.bigDecimalValue());
        assertEquals(new BigDecimal("1.50"), f.bigDecimalValue(2, BigDecimal.ROUND_HALF_UP));
        assertEquals(new BigDecimal("1.5"), f.bigDecimalValue(BigDecimal.ROUND_HALF_UP));
        assertEquals(150.0, f.percentageValue(), 1e-10);
        assertEquals(1, f.intValue());
        assertEquals(1L, f.longValue());
        assertEquals(3L, f.getNumeratorAsLong());
        assertEquals(2L, f.getDenominatorAsLong());
    }

    // Tests compareTo, equals, hashCode, and toString
    @Test
    public void testComparisonsAndStringRepresentation_validInstances_returnsExpectedResults() {
        BigFraction f1 = new BigFraction(1, 2);
        BigFraction f2 = new BigFraction(2, 4);
        BigFraction f3 = new BigFraction(3, 4);

        assertEquals(0, f1.compareTo(f2));
        assertTrue(f1.compareTo(f3) < 0);
        assertTrue(f3.compareTo(f1) > 0);

        assertTrue(f1.equals(f2));
        assertTrue(f1.equals(f1));
        assertFalse(f1.equals(f3));
        assertFalse(f1.equals(null));
        assertFalse(f1.equals("1/2"));

        assertEquals(f1.hashCode(), f2.hashCode());

        assertEquals("1 / 2", f1.toString());
        assertEquals("0", BigFraction.ZERO.toString());
        assertEquals("2", BigFraction.TWO.toString());
        assertNotNull(f1.getField());
    }

    // Additional Tests for Uncovered Branches and Methods

    @Test(expected = NullArgumentException.class)
    public void testConstructor_nullDenominator_throwsNullArgumentException() {
        new BigFraction(BigInteger.ONE, null);
    }

    @Test(expected = ZeroException.class)
    public void testConstructor_longZeroDenominator_throwsZeroException() {
        new BigFraction(1L, 0L);
    }

    @Test
    public void testConstructors_primitiveAndBigIntegerValues() {
        BigFraction fromInt = new BigFraction(5);
        assertEquals(BigInteger.valueOf(5), fromInt.getNumerator());
        assertEquals(BigInteger.ONE, fromInt.getDenominator());

        BigFraction fromLong = new BigFraction(123456789L);
        assertEquals(BigInteger.valueOf(123456789L), fromLong.getNumerator());
        assertEquals(BigInteger.ONE, fromLong.getDenominator());

        BigFraction fromBigInt = new BigFraction(BigInteger.valueOf(42));
        assertEquals(BigInteger.valueOf(42), fromBigInt.getNumerator());
        assertEquals(BigInteger.ONE, fromBigInt.getDenominator());

        BigFraction fromLongs = new BigFraction(6L, -8L);
        assertEquals(BigInteger.valueOf(-3), fromLongs.getNumerator());
        assertEquals(BigInteger.valueOf(4), fromLongs.getDenominator());
    }

    @Test
    public void testConstructor_doubleConversion() {
        BigFraction f1 = new BigFraction(0.5);
        assertEquals(BigFraction.ONE_HALF, f1);

        BigFraction f2 = new BigFraction(1.0 / 3.0, 1e-5, 100);
        assertEquals(BigFraction.ONE_THIRD, f2);

        BigFraction f3 = new BigFraction(0.75, 10);
        assertEquals(BigFraction.THREE_QUARTERS, f3);

        BigFraction fZero = new BigFraction(0.0);
        assertEquals(BigFraction.ZERO, fZero);

        BigFraction fNegative = new BigFraction(-0.5);
        assertEquals(new BigFraction(-1, 2), fNegative);
    }

    @Test(expected = FractionConversionException.class)
    public void testConstructor_doubleMaxIterationsExceeded_throwsFractionConversionException() {
        new BigFraction(0.123456789, 1e-15, 1);
    }

    @Test(expected = FractionConversionException.class)
    public void testConstructor_doubleOverflow_throwsFractionConversionException() {
        new BigFraction(Double.MAX_VALUE, 1.0e-5, 100);
    }

    @Test(expected = NullArgumentException.class)
    public void testAdd_nullBigFraction_throwsNullArgumentException() {
        BigFraction.ONE.add((BigFraction) null);
    }

    @Test(expected = NullArgumentException.class)
    public void testAdd_nullBigInteger_throwsNullArgumentException() {
        BigFraction.ONE.add((BigInteger) null);
    }

    @Test(expected = NullArgumentException.class)
    public void testSubtract_nullBigFraction_throwsNullArgumentException() {
        BigFraction.ONE.subtract((BigFraction) null);
    }

    @Test(expected = NullArgumentException.class)
    public void testSubtract_nullBigInteger_throwsNullArgumentException() {
        BigFraction.ONE.subtract((BigInteger) null);
    }

    @Test(expected = NullArgumentException.class)
    public void testMultiply_nullBigFraction_throwsNullArgumentException() {
        BigFraction.ONE.multiply((BigFraction) null);
    }

    @Test(expected = NullArgumentException.class)
    public void testMultiply_nullBigInteger_throwsNullArgumentException() {
        BigFraction.ONE.multiply((BigInteger) null);
    }

    @Test(expected = NullArgumentException.class)
    public void testDivide_nullBigFraction_throwsNullArgumentException() {
        BigFraction.ONE.divide((BigFraction) null);
    }

    @Test(expected = NullArgumentException.class)
    public void testDivide_nullBigInteger_throwsNullArgumentException() {
        BigFraction.ONE.divide((BigInteger) null);
    }

    @Test(expected = ZeroException.class)
    public void testDivide_intZero_throwsZeroException() {
        BigFraction.ONE.divide(0);
    }

    @Test(expected = ZeroException.class)
    public void testDivide_longZero_throwsZeroException() {
        BigFraction.ONE.divide(0L);
    }

    @Test(expected = ZeroException.class)
    public void testDivide_bigIntegerZero_throwsZeroException() {
        BigFraction.ONE.divide(BigInteger.ZERO);
    }

    @Test(expected = ZeroException.class)
    public void testReciprocal_zeroFraction_throwsZeroException() {
        BigFraction.ZERO.reciprocal();
    }

    @Test(expected = NullArgumentException.class)
    public void testPow_nullBigInteger_throwsNullArgumentException() {
        BigFraction.ONE.pow((BigInteger) null);
    }

    @Test
    public void testPow_zeroExponent_returnsOne() {
        BigFraction f = new BigFraction(2, 3);
        assertEquals(BigFraction.ONE, f.pow(0));
        assertEquals(BigFraction.ONE, f.pow(0L));
        assertEquals(BigFraction.ONE, f.pow(BigInteger.ZERO));
    }

    @Test
    public void testConstants() {
        assertEquals(new BigFraction(-1, 1), BigFraction.MINUS_ONE);
        assertEquals(new BigFraction(4, 5), BigFraction.FOUR_FIFTHS);
        assertEquals(new BigFraction(1, 5), BigFraction.ONE_FIFTH);
        assertEquals(new BigFraction(2, 5), BigFraction.TWO_FIFTHS);
        assertEquals(new BigFraction(3, 5), BigFraction.THREE_FIFTHS);
        assertEquals(new BigFraction(1, 3), BigFraction.ONE_THIRD);
    }

    @Test(expected = NullArgumentException.class)
    public void testCompareTo_nullFraction_throwsNullPointerExceptionOrMathException() {
        BigFraction.ONE.compareTo(null);
    }

    @Test(expected = ZeroException.class)
    public void testGetReducedFraction_zeroDenominator_throwsZeroException() {
        BigFraction.getReducedFraction(1, 0);
    }

    @Test
    public void testGetReducedFraction_negativeDenominator() {
        BigFraction fraction = BigFraction.getReducedFraction(2, -4);
        assertEquals(BigInteger.valueOf(-1), fraction.getNumerator());
        assertEquals(BigInteger.valueOf(2), fraction.getDenominator());
    }
}