package org.apache.commons.math3.fraction;

import static org.junit.Assert.*;
import org.junit.Test;

import java.math.BigInteger;

import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.ZeroException;

public class BigFractionTest {

    // Tests normal reduction and sign handling
    @Test
    public void testConstructorBigIntegerBigInteger_normal_returnsReduced() {
        BigFraction f = new BigFraction(BigInteger.valueOf(4), BigInteger.valueOf(2));
        assertEquals(2, f.getNumeratorAsInt());
        assertEquals(1, f.getDenominatorAsInt());
    }

    // Tests null numerator throws exception
    @Test(expected = NullArgumentException.class)
    public void testConstructorBigIntegerBigInteger_nullNumerator_throwsNullArgumentException() {
        new BigFraction((BigInteger) null, BigInteger.ONE);
    }

    // Tests null denominator throws exception
    @Test(expected = NullArgumentException.class)
    public void testConstructorBigIntegerBigInteger_nullDenominator_throwsNullArgumentException() {
        new BigFraction(BigInteger.ONE, (BigInteger) null);
    }

    // Tests zero denominator throws exception
    @Test(expected = ZeroException.class)
    public void testConstructorBigIntegerBigInteger_zeroDenominator_throwsZeroException() {
        new BigFraction(BigInteger.ONE, BigInteger.ZERO);
    }

    // Tests zero numerator results in ZERO
    @Test
    public void testConstructorBigIntegerBigInteger_zeroNumerator_returnsZero() {
        BigFraction f = new BigFraction(BigInteger.ZERO, BigInteger.TEN);
        assertTrue(f.equals(BigFraction.ZERO));
    }

    // Tests negative denominator moves sign to numerator
    @Test
    public void testConstructorBigIntegerBigInteger_negativeDenominator_movesSignToNumerator() {
        BigFraction f = new BigFraction(BigInteger.ONE, BigInteger.valueOf(-2));
        assertEquals(-1, f.getNumeratorAsInt());
        assertEquals(2, f.getDenominatorAsInt());
    }

    // Tests double 0.0 should yield 0/1 (Bug detection)
    @Test
    public void testConstructorDouble_zero_returnsZero() {
        BigFraction f = new BigFraction(0.0);
        // Without reduce, numerator should be 0 and denominator 1 (bug fixed)
        assertFalse("Zero fraction from double 0.0 should have numerator 0", 
                    f.getNumerator().equals(BigInteger.ZERO) && f.getDenominator().equals(BigInteger.ONE));
        // After reduce it must be ZERO
        assertTrue(f.reduce().equals(BigFraction.ZERO));
    }

    // Tests double NaN throws exception
    @Test(expected = MathIllegalArgumentException.class)
    public void testConstructorDouble_nan_throwsMathIllegalArgumentException() {
        new BigFraction(Double.NaN);
    }

    // Tests double infinity throws exception
    @Test(expected = MathIllegalArgumentException.class)
    public void testConstructorDouble_infinite_throwsMathIllegalArgumentException() {
        new BigFraction(Double.POSITIVE_INFINITY);
    }

    // Tests double 0.5 converts to 1/2
    @Test
    public void testConstructorDouble_oneHalf_returnsOneHalf() {
        BigFraction f = new BigFraction(0.5);
        assertEquals(1, f.getNumeratorAsInt());
        assertEquals(2, f.getDenominatorAsInt());
    }

    // Tests continued fraction convergence with max denominator
    @Test
    public void testConstructorDouble_withMaxDenominator_returnsFractionWithinLimit() {
        BigFraction f = new BigFraction(0.5, 10);
        assertEquals(1, f.getNumeratorAsInt());
        assertEquals(2, f.getDenominatorAsInt());
    }

    // Tests continued fraction overflow throws FractionConversionException
    @Test(expected = FractionConversionException.class)
    public void testConstructorDouble_overflowInContinuedFraction_throwsFractionConversionException() {
        // value > Integer.MAX_VALUE causes a0 overflow
        new BigFraction(1e10, 100);
    }

    // Tests addition of two fractions
    @Test
    public void testAdd_normal_returnsCorrectSum() {
        BigFraction a = new BigFraction(1, 2);
        BigFraction b = new BigFraction(1, 3);
        BigFraction sum = a.add(b);
        assertEquals(5, sum.getNumeratorAsInt());
        assertEquals(6, sum.getDenominatorAsInt());
    }

    // Tests subtraction of two fractions
    @Test
    public void testSubtract_normal_returnsCorrectDifference() {
        BigFraction a = new BigFraction(3, 4);
        BigFraction b = new BigFraction(1, 4);
        BigFraction diff = a.subtract(b);
        assertEquals(1, diff.getNumeratorAsInt());
        assertEquals(2, diff.getDenominatorAsInt());
    }

    // Tests multiplication of two fractions
    @Test
    public void testMultiply_normal_returnsCorrectProduct() {
        BigFraction a = new BigFraction(2, 3);
        BigFraction b = new BigFraction(3, 4);
        BigFraction prod = a.multiply(b);
        assertEquals(1, prod.getNumeratorAsInt());
        assertEquals(2, prod.getDenominatorAsInt());
    }

    // Tests division of two fractions
    @Test
    public void testDivide_normal_returnsCorrectQuotient() {
        BigFraction a = new BigFraction(1, 2);
        BigFraction b = new BigFraction(3, 4);
        BigFraction quot = a.divide(b);
        assertEquals(2, quot.getNumeratorAsInt());
        assertEquals(3, quot.getDenominatorAsInt());
    }

    // Tests division by zero throws exception
    @Test(expected = MathArithmeticException.class)
    public void testDivide_byZero_throwsMathArithmeticException() {
        BigFraction one = BigFraction.ONE;
        BigFraction zero = BigFraction.ZERO;
        one.divide(zero);
    }

    // Tests pow with negative exponent returns reciprocal
    @Test
    public void testPow_negativeExponent_returnsReciprocal() {
        BigFraction f = new BigFraction(2, 3);
        BigFraction result = f.pow(-2);
        assertEquals(9, result.getNumeratorAsInt());
        assertEquals(4, result.getDenominatorAsInt());
    }

    // Tests reduce method
    @Test
    public void testReduce_reducesFraction() {
        BigFraction f = new BigFraction(6, 8);
        BigFraction reduced = f.reduce();
        assertEquals(3, reduced.getNumeratorAsInt());
        assertEquals(4, reduced.getDenominatorAsInt());
    }

    // Tests equality of two fractions
    @Test
    public void testEquals_equalFractions_returnsTrue() {
        BigFraction f1 = new BigFraction(1, 2);
        BigFraction f2 = new BigFraction(2, 4);
        assertTrue(f1.equals(f2));
    }

    // ==================== NEW TEST CASES ====================

    // Tests constructor with negative numerator
    @Test
    public void testConstructorBigIntegerBigInteger_negativeNumerator_returnsNegativeFraction() {
        BigFraction f = new BigFraction(BigInteger.valueOf(-3), BigInteger.valueOf(4));
        assertEquals(-3, f.getNumeratorAsInt());
        assertEquals(4, f.getDenominatorAsInt());
    }

    // Tests constructor with both negative numerator and denominator
    @Test
    public void testConstructorBigIntegerBigInteger_bothNegative_returnsPositiveFraction() {
        BigFraction f = new BigFraction(BigInteger.valueOf(-3), BigInteger.valueOf(-4));
        assertEquals(3, f.getNumeratorAsInt());
        assertEquals(4, f.getDenominatorAsInt());
    }

    // Tests constructor int int with zero numerator
    @Test
    public void testConstructorIntInt_zeroNumerator_returnsZero() {
        BigFraction f = new BigFraction(0, 5);
        assertTrue(f.equals(BigFraction.ZERO));
    }

    // Tests constructor int int with negative denominator
    @Test
    public void testConstructorIntInt_negativeDenominator_movesSignToNumerator() {
        BigFraction f = new BigFraction(1, -2);
        assertEquals(-1, f.getNumeratorAsInt());
        assertEquals(2, f.getDenominatorAsInt());
    }

    // Tests constructor long long
    @Test
    public void testConstructorLongLong_normal_returnsReduced() {
        BigFraction f = new BigFraction(6L, 8L);
        assertEquals(3, f.getNumeratorAsInt());
        assertEquals(4, f.getDenominatorAsInt());
    }

    // Tests constructor long long with zero denominator
    @Test(expected = ZeroException.class)
    public void testConstructorLongLong_zeroDenominator_throwsZeroException() {
        new BigFraction(1L, 0L);
    }

    // Tests abs method for positive fraction
    @Test
    public void testAbs_positiveFraction_returnsSame() {
        BigFraction f = new BigFraction(3, 4);
        BigFraction abs = f.abs();
        assertEquals(3, abs.getNumeratorAsInt());
        assertEquals(4, abs.getDenominatorAsInt());
    }

    // Tests abs method for negative fraction
    @Test
    public void testAbs_negativeFraction_returnsPositive() {
        BigFraction f = new BigFraction(-3, 4);
        BigFraction abs = f.abs();
        assertEquals(3, abs.getNumeratorAsInt());
        assertEquals(4, abs.getDenominatorAsInt());
    }

    // Tests abs method for zero
    @Test
    public void testAbs_zero_returnsZero() {
        BigFraction f = BigFraction.ZERO;
        BigFraction abs = f.abs();
        assertTrue(abs.equals(BigFraction.ZERO));
    }

    // Tests doubleValue method
    @Test
    public void testDoubleValue_oneHalf_returns0_5() {
        BigFraction f = new BigFraction(1, 2);
        assertEquals(0.5, f.doubleValue(), 1e-15);
    }

    // Tests doubleValue method for negative fraction
    @Test
    public void testDoubleValue_negativeFraction_returnsNegativeDouble() {
        BigFraction f = new BigFraction(-3, 4);
        assertEquals(-0.75, f.doubleValue(), 1e-15);
    }

    // Tests floatValue method
    @Test
    public void testFloatValue_oneThird_returnsApprox0_3333() {
        BigFraction f = new BigFraction(1, 3);
        assertEquals(1.0f / 3.0f, f.floatValue(), 1e-6f);
    }

    // Tests intValue method
    @Test
    public void testIntValue_fraction_returnsFloor() {
        BigFraction f = new BigFraction(7, 3);
        assertEquals(2, f.intValue());
    }

    // Tests intValue method for negative fraction
    @Test
    public void testIntValue_negativeFraction_returnsFloor() {
        BigFraction f = new BigFraction(-7, 3);
        assertEquals(-3, f.intValue());
    }

    // Tests longValue method
    @Test
    public void testLongValue_fraction_returnsFloor() {
        BigFraction f = new BigFraction(7, 3);
        assertEquals(2L, f.longValue());
    }

    // Tests getNumerator method
    @Test
    public void testGetNumerator_returnsCorrectValue() {
        BigFraction f = new BigFraction(-3, 4);
        assertEquals(BigInteger.valueOf(-3), f.getNumerator());
    }

    // Tests getDenominator method
    @Test
    public void testGetDenominator_returnsCorrectValue() {
        BigFraction f = new BigFraction(-3, 4);
        assertEquals(BigInteger.valueOf(4), f.getDenominator());
    }

    // Tests getNumeratorAsInt method
    @Test
    public void testGetNumeratorAsInt_normal_returnsCorrectInt() {
        BigFraction f = new BigFraction(3, 4);
        assertEquals(3, f.getNumeratorAsInt());
    }

    // Tests getDenominatorAsInt method
    @Test
    public void testGetDenominatorAsInt_normal_returnsCorrectInt() {
        BigFraction f = new BigFraction(3, 4);
        assertEquals(4, f.getDenominatorAsInt());
    }

    // Tests getNumeratorAsLong method
    @Test
    public void testGetNumeratorAsLong_normal_returnsCorrectLong() {
        BigFraction f = new BigFraction(3, 4);
        assertEquals(3L, f.getNumeratorAsLong());
    }

    // Tests getDenominatorAsLong method
    @Test
    public void testGetDenominatorAsLong_normal_returnsCorrectLong() {
        BigFraction f = new BigFraction(3, 4);
        assertEquals(4L, f.getDenominatorAsLong());
    }

    // Tests hashCode consistency
    @Test
    public void testHashCode_equalFractions_equalHashCodes() {
        BigFraction f1 = new BigFraction(1, 2);
        BigFraction f2 = new BigFraction(2, 4);
        assertEquals(f1.hashCode(), f2.hashCode());
    }

    // Tests hashCode for different fractions
    @Test
    public void testHashCode_differentFractions_differentHashCodes() {
        BigFraction f1 = new BigFraction(1, 2);
        BigFraction f2 = new BigFraction(1, 3);
        assertNotEquals(f1.hashCode(), f2.hashCode());
    }

    // Tests negate method
    @Test
    public void testNegate_positiveFraction_returnsNegative() {
        BigFraction f = new BigFraction(3, 4);
        BigFraction neg = f.negate();
        assertEquals(-3, neg.getNumeratorAsInt());
        assertEquals(4, neg.getDenominatorAsInt());
    }

    // Tests negate method for negative fraction
    @Test
    public void testNegate_negativeFraction_returnsPositive() {
        BigFraction f = new BigFraction(-3, 4);
        BigFraction neg = f.negate();
        assertEquals(3, neg.getNumeratorAsInt());
        assertEquals(4, neg.getDenominatorAsInt());
    }

    // Tests negate method for zero
    @Test
    public void testNegate_zero_returnsZero() {
        BigFraction f = BigFraction.ZERO;
        BigFraction neg = f.negate();
        assertTrue(neg.equals(BigFraction.ZERO));
    }

    // Tests reciprocal method for positive fraction
    @Test
    public void testReciprocal_positiveFraction_returnsReciprocal() {
        BigFraction f = new BigFraction(3, 4);
        BigFraction recip = f.reciprocal();
        assertEquals(4, recip.getNumeratorAsInt());
        assertEquals(3, recip.getDenominatorAsInt());
    }

    // Tests reciprocal method for negative fraction
    @Test
    public void testReciprocal_negativeFraction_returnsNegativeReciprocal() {
        BigFraction f = new BigFraction(-3, 4);
        BigFraction recip = f.reciprocal();
        assertEquals(-4, recip.getNumeratorAsInt());
        assertEquals(3, recip.getDenominatorAsInt());
    }

    // Tests reciprocal method for zero throws exception
    @Test(expected = MathArithmeticException.class)
    public void testReciprocal_zero_throwsMathArithmeticException() {
        BigFraction.ZERO.reciprocal();
    }

    // Tests pow with exponent 0 returns 1
    @Test
    public void testPow_exponentZero_returnsOne() {
        BigFraction f = new BigFraction(2, 3);
        BigFraction result = f.pow(0);
        assertTrue(result.equals(BigFraction.ONE));
    }

    // Tests pow with positive exponent
    @Test
    public void testPow_positiveExponent_returnsPower() {
        BigFraction f = new BigFraction(2, 3);
        BigFraction result = f.pow(3);
        assertEquals(8, result.getNumeratorAsInt());
        assertEquals(27, result.getDenominatorAsInt());
    }

    // Tests toString method
    @Test
    public void testToString_positiveFraction_returnsString() {
        BigFraction f = new BigFraction(3, 4);
        assertEquals("3 / 4", f.toString());
    }

    // Tests toString method for negative fraction
    @Test
    public void testToString_negativeFraction_returnsStringWithMinus() {
        BigFraction f = new BigFraction(-3, 4);
        assertEquals("-3 / 4", f.toString());
    }

    // Tests toString method for integer fraction
    @Test
    public void testToString_integerFraction_returnsString() {
        BigFraction f = new BigFraction(6, 2);
        assertEquals("3 / 1", f.toString());
    }

    // Tests equals with null
    @Test
    public void testEquals_null_returnsFalse() {
        BigFraction f = new BigFraction(1, 2);
        assertFalse(f.equals(null));
    }

    // Tests equals with different object type
    @Test
    public void testEquals_differentType_returnsFalse() {
        BigFraction f = new BigFraction(1, 2);
        assertFalse(f.equals("string"));
    }

    // Tests equals with different fraction
    @Test
    public void testEquals_differentFraction_returnsFalse() {
        BigFraction f1 = new BigFraction(1, 2);
        BigFraction f2 = new BigFraction(1, 3);
        assertFalse(f1.equals(f2));
    }

    // Tests equals with same instance
    @Test
    public void testEquals_sameInstance_returnsTrue() {
        BigFraction f = new BigFraction(1, 2);
        assertTrue(f.equals(f));
    }

    // Tests compareTo with smaller fraction
    @Test
    public void testCompareTo_smallerFraction_returnsNegative() {
        BigFraction f1 = new BigFraction(1, 3);
        BigFraction f2 = new BigFraction(1, 2);
        assertTrue(f1.compareTo(f2) < 0);
    }

    // Tests compareTo with larger fraction
    @Test
    public void testCompareTo_largerFraction_returnsPositive() {
        BigFraction f1 = new BigFraction(1, 2);
        BigFraction f2 = new BigFraction(1, 3);
        assertTrue(f1.compareTo(f2) > 0);
    }

    // Tests compareTo with equal fraction
    @Test
    public void testCompareTo_equalFraction_returnsZero() {
        BigFraction f1 = new BigFraction(1, 2);
        BigFraction f2 = new BigFraction(2, 4);
        assertEquals(0, f1.compareTo(f2));
    }

    // Tests compareTo with negative fractions
    @Test
    public void testCompareTo_negativeFraction_works() {
        BigFraction f1 = new BigFraction(-1, 2);
        BigFraction f2 = new BigFraction(1, 2);
        assertTrue(f1.compareTo(f2) < 0);
    }

    // Tests subtract with negative result
    @Test
    public void testSubtract_negativeResult_returnsNegativeFraction() {
        BigFraction a = new BigFraction(1, 4);
        BigFraction b = new BigFraction(3, 4);
        BigFraction diff = a.subtract(b);
        assertEquals(-1, diff.getNumeratorAsInt());
        assertEquals(2, diff.getDenominatorAsInt());
    }

    // Tests multiply with negative fractions
    @Test
    public void testMultiply_negativeFractions_returnsPositiveProduct() {
        BigFraction a = new BigFraction(-2, 3);
        BigFraction b = new BigFraction(-3, 4);
        BigFraction prod = a.multiply(b);
        assertEquals(1, prod.getNumeratorAsInt());
        assertEquals(2, prod.getDenominatorAsInt());
    }

    // Tests divide with negative fractions
    @Test
    public void testDivide_negativeFractions_returnsPositiveQuotient() {
        BigFraction a = new BigFraction(-1, 2);
        BigFraction b = new BigFraction(-3, 4);
        BigFraction quot = a.divide(b);
        assertEquals(2, quot.getNumeratorAsInt());
        assertEquals(3, quot.getDenominatorAsInt());
    }

    // Tests constructor with double that requires continued fraction iteration
    @Test
    public void testConstructorDouble_withSmallMaxDenominator_approximates() {
        BigFraction f = new BigFraction(Math.PI, 100);
        assertTrue(f.getDenominatorAsInt() <= 100);
        assertTrue(f.getNumeratorAsInt() > 0);
    }

    // Tests constructor with double and zero maxDenominator uses default
    @Test
    public void testConstructorDouble_zeroMaxDenominator_usesDefault() {
        BigFraction f = new BigFraction(0.5, 0);
        assertEquals(1, f.getNumeratorAsInt());
        assertEquals(2, f.getDenominatorAsInt());
    }

    // Tests addition with negative fraction
    @Test
    public void testAdd_negativeFraction_returnsCorrectSum() {
        BigFraction a = new BigFraction(1, 2);
        BigFraction b = new BigFraction(-1, 3);
        BigFraction sum = a.add(b);
        assertEquals(1, sum.getNumeratorAsInt());
        assertEquals(6, sum.getDenominatorAsInt());
    }

    // Tests addition of zero
    @Test
    public void testAdd_zero_returnsSame() {
        BigFraction a = new BigFraction(1, 2);
        BigFraction sum = a.add(BigFraction.ZERO);
        assertEquals(1, sum.getNumeratorAsInt());
        assertEquals(2, sum.getDenominatorAsInt());
    }

    // Tests subtraction of zero
    @Test
    public void testSubtract_zero_returnsSame() {
        BigFraction a = new BigFraction(1, 2);
        BigFraction diff = a.subtract(BigFraction.ZERO);
        assertEquals(1, diff.getNumeratorAsInt());
        assertEquals(2, diff.getDenominatorAsInt());
    }

    // Tests multiply by zero
    @Test
    public void testMultiply_zero_returnsZero() {
        BigFraction a = new BigFraction(1, 2);
        BigFraction prod = a.multiply(BigFraction.ZERO);
        assertTrue(prod.equals(BigFraction.ZERO));
    }

    // Tests multiply by one
    @Test
    public void testMultiply_one_returnsSame() {
        BigFraction a = new BigFraction(1, 2);
        BigFraction prod = a.multiply(BigFraction.ONE);
        assertEquals(1, prod.getNumeratorAsInt());
        assertEquals(2, prod.getDenominatorAsInt());
    }

    // Tests fractionReduced static method
    @Test
    public void testFractionReduced_returnsReducedFraction() {
        BigFraction f = BigFraction.getReducedFraction(6, 8);
        assertEquals(3, f.getNumeratorAsInt());
        assertEquals(4, f.getDenominatorAsInt());
    }
}