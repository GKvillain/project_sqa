package org.apache.commons.math3.fraction;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.ZeroException;
import org.junit.Assert;
import org.junit.Test;

public class BigFractionTest {

    // Tests BigInteger constructor with reduction and sign movement to numerator
    @Test
    public void testConstructor_BigInteger_reducesAndHandlesSigns() {
        BigFraction fraction = new BigFraction(BigInteger.valueOf(6), BigInteger.valueOf(-8));
        Assert.assertEquals(BigInteger.valueOf(-3), fraction.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(4), fraction.getDenominator());

        BigFraction zeroFraction = new BigFraction(BigInteger.ZERO, BigInteger.valueOf(5));
        Assert.assertEquals(BigInteger.ZERO, zeroFraction.getNumerator());
        Assert.assertEquals(BigInteger.ONE, zeroFraction.getDenominator());

        BigFraction singleIntFraction = new BigFraction(BigInteger.valueOf(7));
        Assert.assertEquals(BigInteger.valueOf(7), singleIntFraction.getNumerator());
        Assert.assertEquals(BigInteger.ONE, singleIntFraction.getDenominator());
    }

    // Tests zero denominator in BigInteger constructor throws ZeroException
    @Test(expected = ZeroException.class)
    public void testConstructor_zeroDenominator_throwsZeroException() {
        new BigFraction(BigInteger.ONE, BigInteger.ZERO);
    }

    // Tests null numerator in BigInteger constructor throws NullArgumentException
    @Test(expected = NullArgumentException.class)
    public void testConstructor_nullNumerator_throwsNullArgumentException() {
        new BigFraction(null, BigInteger.ONE);
    }

    // Tests exact double constructor and its invalid double arguments
    @Test
    public void testConstructor_doubleExact_convertsProperly() {
        BigFraction bf = new BigFraction(0.5);
        Assert.assertEquals(BigInteger.ONE, bf.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(2), bf.getDenominator());

        BigFraction bfNeg = new BigFraction(-2.0);
        Assert.assertEquals(BigInteger.valueOf(-2), bfNeg.getNumerator());
        Assert.assertEquals(BigInteger.ONE, bfNeg.getDenominator());
    }

    // Tests NaN double constructor throws MathIllegalArgumentException
    @Test(expected = MathIllegalArgumentException.class)
    public void testConstructor_doubleNaN_throwsMathIllegalArgumentException() {
        new BigFraction(Double.NaN);
    }

    // Tests infinite double constructor throws MathIllegalArgumentException
    @Test(expected = MathIllegalArgumentException.class)
    public void testConstructor_doubleInfinite_throwsMathIllegalArgumentException() {
        new BigFraction(Double.POSITIVE_INFINITY);
    }

    // Tests double conversion with epsilon and max iterations
    @Test
    public void testConstructor_doubleEpsilonAndMaxIterations_converges() {
        BigFraction bf = new BigFraction(0.3333333333333333, 1e-10, 100);
        Assert.assertEquals(BigInteger.ONE, bf.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(3), bf.getDenominator());

        BigFraction intBf = new BigFraction(4.0, 1e-10, 100);
        Assert.assertEquals(BigInteger.valueOf(4), intBf.getNumerator());
        Assert.assertEquals(BigInteger.ONE, intBf.getDenominator());
    }

    // Tests double conversion with maxDenominator (related to Defects4J bug Math-1)
    @Test
    public void testConstructor_doubleMaxDenominator_convergesOrDetectsOverflow() {
        BigFraction bf = new BigFraction(0.5, 10);
        Assert.assertEquals(BigInteger.ONE, bf.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(2), bf.getDenominator());

        BigFraction smallBf = new BigFraction(1e-15, 10);
        Assert.assertEquals(BigInteger.ZERO, smallBf.getNumerator());
        Assert.assertEquals(BigInteger.ONE, smallBf.getDenominator());
    }

    // Tests primitive int and long constructors and getReducedFraction
    @Test
    public void testConstructor_primitiveTypesAndFactoryMethods() {
        BigFraction fromInt = new BigFraction(12, 16);
        Assert.assertEquals(3, fromInt.getNumeratorAsInt());
        Assert.assertEquals(4, fromInt.getDenominatorAsInt());

        BigFraction fromLong = new BigFraction(12L, 16L);
        Assert.assertEquals(3L, fromLong.getNumeratorAsLong());
        Assert.assertEquals(4L, fromLong.getDenominatorAsLong());

        BigFraction reducedZero = BigFraction.getReducedFraction(0, 5);
        Assert.assertEquals(BigFraction.ZERO, reducedZero);

        BigFraction reduced = BigFraction.getReducedFraction(2, 4);
        Assert.assertEquals(BigFraction.ONE_HALF, reduced);
    }

    // Tests addition of BigFraction, BigInteger, int, and long
    @Test
    public void testAdd_variousTypes_returnsCorrectSum() {
        BigFraction bf1 = new BigFraction(1, 3);
        BigFraction bf2 = new BigFraction(2, 3);
        Assert.assertEquals(BigFraction.ONE, bf1.add(bf2));
        Assert.assertEquals(bf1, bf1.add(BigFraction.ZERO));

        BigFraction diffDen = new BigFraction(1, 2).add(new BigFraction(1, 3));
        Assert.assertEquals(new BigFraction(5, 6), diffDen);

        Assert.assertEquals(new BigFraction(4, 3), bf1.add(1));
        Assert.assertEquals(new BigFraction(7, 3), bf1.add(2L));
        Assert.assertEquals(new BigFraction(10, 3), bf1.add(BigInteger.valueOf(3)));
    }

    // Tests addition with null throws NullArgumentException
    @Test(expected = NullArgumentException.class)
    public void testAdd_nullFraction_throwsNullArgumentException() {
        BigFraction.ONE.add((BigFraction) null);
    }

    // Tests subtraction of BigFraction, BigInteger, int, and long
    @Test
    public void testSubtract_variousTypes_returnsCorrectDifference() {
        BigFraction bf1 = new BigFraction(2, 3);
        BigFraction bf2 = new BigFraction(1, 3);
        Assert.assertEquals(new BigFraction(1, 3), bf1.subtract(bf2));
        Assert.assertEquals(bf1, bf1.subtract(BigFraction.ZERO));

        BigFraction diffDen = new BigFraction(1, 2).subtract(new BigFraction(1, 3));
        Assert.assertEquals(new BigFraction(1, 6), diffDen);

        Assert.assertEquals(new BigFraction(-1, 3), bf1.subtract(1));
        Assert.assertEquals(new BigFraction(-4, 3), bf1.subtract(2L));
        Assert.assertEquals(new BigFraction(-7, 3), bf1.subtract(BigInteger.valueOf(3)));
    }

    // Tests subtraction with null throws NullArgumentException
    @Test(expected = NullArgumentException.class)
    public void testSubtract_nullFraction_throwsNullArgumentException() {
        BigFraction.ONE.subtract((BigFraction) null);
    }

    // Tests multiplication of BigFraction, BigInteger, int, and long
    @Test
    public void testMultiply_variousTypes_returnsCorrectProduct() {
        BigFraction bf = new BigFraction(2, 3);
        Assert.assertEquals(new BigFraction(4, 9), bf.multiply(bf));
        Assert.assertEquals(BigFraction.ZERO, bf.multiply(BigFraction.ZERO));
        Assert.assertEquals(BigFraction.ZERO, BigFraction.ZERO.multiply(bf));

        Assert.assertEquals(new BigFraction(4, 3), bf.multiply(2));
        Assert.assertEquals(new BigFraction(6, 3).reduce(), bf.multiply(3L));
        Assert.assertEquals(new BigFraction(8, 3), bf.multiply(BigInteger.valueOf(4)));
    }

    // Tests division by fraction and primitive values
    @Test
    public void testDivide_variousTypes_returnsCorrectQuotient() {
        BigFraction bf = new BigFraction(2, 3);
        Assert.assertEquals(BigFraction.ONE, bf.divide(bf));
        Assert.assertEquals(new BigFraction(1, 3), bf.divide(2));
        Assert.assertEquals(new BigFraction(2, 9), bf.divide(3L));
        Assert.assertEquals(new BigFraction(1, 6), bf.divide(BigInteger.valueOf(4)));
    }

    // Tests division by zero fraction throws MathArithmeticException
    @Test(expected = MathArithmeticException.class)
    public void testDivide_byZeroFraction_throwsMathArithmeticException() {
        BigFraction.ONE.divide(BigFraction.ZERO);
    }

    // Tests absolute value, negation, and reciprocal
    @Test
    public void testAbsNegateAndReciprocal_returnsExpectedFractions() {
        BigFraction neg = new BigFraction(-3, 4);
        BigFraction pos = new BigFraction(3, 4);

        Assert.assertEquals(pos, neg.abs());
        Assert.assertEquals(pos, pos.abs());
        Assert.assertEquals(neg, pos.negate());
        Assert.assertEquals(new BigFraction(4, 3), pos.reciprocal());
    }

    // Tests pow methods with integer, long, BigInteger, and double
    @Test
    public void testPow_variousExponents_returnsCorrectResults() {
        BigFraction bf = new BigFraction(2, 3);

        Assert.assertEquals(new BigFraction(4, 9), bf.pow(2));
        Assert.assertEquals(new BigFraction(9, 4), bf.pow(-2));

        Assert.assertEquals(new BigFraction(8, 27), bf.pow(3L));
        Assert.assertEquals(new BigFraction(27, 8), bf.pow(-3L));

        Assert.assertEquals(new BigFraction(16, 81), bf.pow(BigInteger.valueOf(4)));
        Assert.assertEquals(new BigFraction(81, 16), bf.pow(BigInteger.valueOf(-4)));

        Assert.assertEquals(0.25, new BigFraction(1, 2).pow(2.0), 1e-10);
    }

    // Tests conversions to BigDecimal, double, float, int, long, and percentage
    @Test
    public void testConversions_returnsExpectedValues() {
        BigFraction bf = new BigFraction(1, 2);
        Assert.assertEquals(0.5, bf.doubleValue(), 1e-10);
        Assert.assertEquals(0.5f, bf.floatValue(), 1e-10f);
        Assert.assertEquals(0, bf.intValue());
        Assert.assertEquals(0L, bf.longValue());
        Assert.assertEquals(50.0, bf.percentageValue(), 1e-10);
        Assert.assertEquals(new BigDecimal("0.5"), bf.bigDecimalValue());
        Assert.assertEquals(new BigDecimal("0.50"), bf.bigDecimalValue(2, BigDecimal.ROUND_HALF_UP));
        Assert.assertEquals(new BigDecimal("0.5"), bf.bigDecimalValue(BigDecimal.ROUND_HALF_UP));
    }

    // Tests compareTo, equals, hashCode, and toString
    @Test
    public void testCompareToEqualsHashCodeAndToString() {
        BigFraction half1 = new BigFraction(1, 2);
        BigFraction half2 = new BigFraction(2, 4);
        BigFraction third = new BigFraction(1, 3);

        Assert.assertEquals(0, half1.compareTo(half2));
        Assert.assertTrue(half1.compareTo(third) > 0);
        Assert.assertTrue(third.compareTo(half1) < 0);

        Assert.assertTrue(half1.equals(half2));
        Assert.assertFalse(half1.equals(third));
        Assert.assertFalse(half1.equals(null));
        Assert.assertFalse(half1.equals(new Object()));
        Assert.assertTrue(half1.equals(half1));

        Assert.assertEquals(half1.hashCode(), half2.hashCode());

        Assert.assertEquals("1 / 2", half1.toString());
        Assert.assertEquals("0", BigFraction.ZERO.toString());
        Assert.assertEquals("3", new BigFraction(3).toString());
        Assert.assertNotNull(half1.getField());
    }
}