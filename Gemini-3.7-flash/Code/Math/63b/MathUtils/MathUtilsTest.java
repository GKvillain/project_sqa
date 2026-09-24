package org.apache.commons.math.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.exception.NonMonotonousSequenceException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link MathUtils}.
 */
public class MathUtilsTest {

    // Tests addition with overflow check for integer values and overflow exception
    @Test(expected = ArithmeticException.class)
    public void testAddAndCheckInt_overflow_throwsException() {
        assertEquals(5, MathUtils.addAndCheck(2, 3));
        assertEquals(-5, MathUtils.addAndCheck(-2, -3));
        MathUtils.addAndCheck(Integer.MAX_VALUE, 1);
    }

    // Tests addition with overflow check for long values and negative overflow exception
    @Test(expected = ArithmeticException.class)
    public void testAddAndCheckLong_negativeOverflow_throwsException() {
        assertEquals(5L, MathUtils.addAndCheck(2L, 3L));
        assertEquals(-1L, MathUtils.addAndCheck(Long.MAX_VALUE, Long.MIN_VALUE));
        MathUtils.addAndCheck(Long.MIN_VALUE, -1L);
    }

    // Tests subtraction with overflow check for integer and long values
    @Test(expected = ArithmeticException.class)
    public void testSubAndCheckInt_underflow_throwsException() {
        assertEquals(1, MathUtils.subAndCheck(3, 2));
        assertEquals(5, MathUtils.subAndCheck(2, -3));
        MathUtils.subAndCheck(Integer.MIN_VALUE, 1);
    }

    // Tests subtraction with overflow for long values
    @Test(expected = ArithmeticException.class)
    public void testSubAndCheckLong_overflow_throwsException() {
        assertEquals(1L, MathUtils.subAndCheck(3L, 2L));
        assertEquals(0L, MathUtils.subAndCheck(Long.MIN_VALUE, Long.MIN_VALUE));
        MathUtils.subAndCheck(0L, Long.MIN_VALUE);
    }

    // Tests integer and long multiplication with overflow detection
    @Test(expected = ArithmeticException.class)
    public void testMulAndCheck_overflow_throwsException() {
        assertEquals(6, MathUtils.mulAndCheck(2, 3));
        assertEquals(-6, MathUtils.mulAndCheck(-2, 3));
        assertEquals(0L, MathUtils.mulAndCheck(0L, Long.MAX_VALUE));
        assertEquals(12L, MathUtils.mulAndCheck(3L, 4L));
        MathUtils.mulAndCheck(Long.MAX_VALUE, 2L);
    }

    // Tests binomial coefficient calculation for regular, symmetry, and boundary cases
    @Test
    public void testBinomialCoefficient_validInputs_returnsCorrectResult() {
        assertEquals(1L, MathUtils.binomialCoefficient(5, 0));
        assertEquals(1L, MathUtils.binomialCoefficient(5, 5));
        assertEquals(5L, MathUtils.binomialCoefficient(5, 1));
        assertEquals(5L, MathUtils.binomialCoefficient(5, 4));
        assertEquals(10L, MathUtils.binomialCoefficient(5, 2));
        assertEquals(10L, MathUtils.binomialCoefficient(5, 3));
        assertEquals(184756L, MathUtils.binomialCoefficient(20, 10));
        assertEquals(10d, MathUtils.binomialCoefficientDouble(5, 2), 1e-10);
        assertEquals(FastMath.log(10.0), MathUtils.binomialCoefficientLog(5, 2), 1e-10);
    }

    // Tests binomial coefficient with invalid parameters
    @Test(expected = IllegalArgumentException.class)
    public void testBinomialCoefficient_invalidParameters_throwsException() {
        MathUtils.binomialCoefficient(2, 5);
    }

    // Tests double equality comparison including NaN handling and array comparison
    @Test
    public void testEquals_variousDoubleComparisons_returnsCorrectBoolean() {
        assertTrue(MathUtils.equals(1.0, 1.0));
        assertFalse(MathUtils.equals(1.0, 2.0));
        assertTrue(MathUtils.equals(Double.NaN, Double.NaN));
        assertFalse(MathUtils.equals(Double.NaN, 1.0));
        assertTrue(MathUtils.equalsIncludingNaN(Double.NaN, Double.NaN));

        assertTrue(MathUtils.equals(1.0, 1.05, 0.1));
        assertFalse(MathUtils.equals(1.0, 1.2, 0.1));
        assertTrue(MathUtils.equalsIncludingNaN(Double.NaN, Double.NaN, 0.1));

        double[] arr1 = new double[] {1.0, 2.0, Double.NaN};
        double[] arr2 = new double[] {1.0, 2.0, Double.NaN};
        double[] arr3 = new double[] {1.0, 2.0, 3.0};
        assertTrue(MathUtils.equals(arr1, arr2));
        assertFalse(MathUtils.equals(arr1, arr3));
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
        assertFalse(MathUtils.equals(arr1, null));
        assertTrue(MathUtils.equalsIncludingNaN(arr1, arr2));
    }

    // Tests compareTo method with epsilon
    @Test
    public void testCompareTo_withTolerance_returnsCorrectComparison() {
        assertEquals(0, MathUtils.compareTo(1.0, 1.05, 0.1));
        assertEquals(-1, MathUtils.compareTo(1.0, 2.0, 0.1));
        assertEquals(1, MathUtils.compareTo(2.0, 1.0, 0.1));
    }

    // Tests factorial calculation for valid inputs and error conditions
    @Test
    public void testFactorial_validInputs_returnsCorrectResult() {
        assertEquals(1L, MathUtils.factorial(0));
        assertEquals(1L, MathUtils.factorial(1));
        assertEquals(120L, MathUtils.factorial(5));
        assertEquals(2432902008176640000L, MathUtils.factorial(20));
        assertEquals(120.0, MathUtils.factorialDouble(5), 1e-10);
        assertEquals(FastMath.log(120.0), MathUtils.factorialLog(5), 1e-10);
    }

    // Tests factorial with negative argument
    @Test(expected = IllegalArgumentException.class)
    public void testFactorial_negativeArgument_throwsException() {
        MathUtils.factorial(-1);
    }

    // Tests gcd and lcm methods for int and long inputs
    @Test
    public void testGcdAndLcm_variousInputs_returnsCorrectResult() {
        assertEquals(6, MathUtils.gcd(54, 24));
        assertEquals(6, MathUtils.gcd(-54, 24));
        assertEquals(5, MathUtils.gcd(0, 5));
        assertEquals(0, MathUtils.gcd(0, 0));
        assertEquals(12, MathUtils.lcm(4, 6));
        assertEquals(0, MathUtils.lcm(0, 6));

        assertEquals(6L, MathUtils.gcd(54L, 24L));
        assertEquals(6L, MathUtils.gcd(-54L, 24L));
        assertEquals(5L, MathUtils.gcd(0L, 5L));
        assertEquals(0L, MathUtils.gcd(0L, 0L));
        assertEquals(12L, MathUtils.lcm(4L, 6L));
        assertEquals(0L, MathUtils.lcm(0L, 6L));
    }

    // Tests indicator and sign functions across primitive types
    @Test
    public void testIndicatorAndSign_allTypes_returnsExpectedValues() {
        assertEquals((byte) 1, MathUtils.indicator((byte) 5));
        assertEquals((byte) -1, MathUtils.indicator((byte) -5));
        assertEquals((short) 1, MathUtils.indicator((short) 5));
        assertEquals((short) -1, MathUtils.indicator((short) -5));
        assertEquals(1, MathUtils.indicator(5));
        assertEquals(-1, MathUtils.indicator(-5));
        assertEquals(1L, MathUtils.indicator(5L));
        assertEquals(-1L, MathUtils.indicator(-5L));
        assertEquals(1.0, MathUtils.indicator(5.0), 1e-10);
        assertEquals(-1.0, MathUtils.indicator(-5.0), 1e-10);
        assertTrue(Double.isNaN(MathUtils.indicator(Double.NaN)));

        assertEquals((byte) 1, MathUtils.sign((byte) 5));
        assertEquals((byte) 0, MathUtils.sign((byte) 0));
        assertEquals((byte) -1, MathUtils.sign((byte) -5));
        assertEquals(1, MathUtils.sign(5));
        assertEquals(0, MathUtils.sign(0));
        assertEquals(-1, MathUtils.sign(-5));
        assertEquals(1.0, MathUtils.sign(5.0), 1e-10);
        assertEquals(0.0, MathUtils.sign(0.0), 1e-10);
        assertEquals(-1.0, MathUtils.sign(-5.0), 1e-10);
        assertTrue(Double.isNaN(MathUtils.sign(Double.NaN)));
    }

    // Tests power functions for int, long, and BigInteger
    @Test
    public void testPow_variousTypes_returnsCorrectPower() {
        assertEquals(8, MathUtils.pow(2, 3));
        assertEquals(1, MathUtils.pow(5, 0));
        assertEquals(8, MathUtils.pow(2, 3L));
        assertEquals(8L, MathUtils.pow(2L, 3));
        assertEquals(8L, MathUtils.pow(2L, 3L));

        BigInteger b2 = BigInteger.valueOf(2);
        assertEquals(BigInteger.valueOf(8), MathUtils.pow(b2, 3));
        assertEquals(BigInteger.valueOf(8), MathUtils.pow(b2, 3L));
        assertEquals(BigInteger.valueOf(8), MathUtils.pow(b2, BigInteger.valueOf(3)));
    }

    // Tests power function with negative exponent throwing exception
    @Test(expected = IllegalArgumentException.class)
    public void testPow_negativeExponent_throwsException() {
        MathUtils.pow(2, -1);
    }

    // Tests rounding functions for double and float values
    @Test
    public void testRound_variousRoundingModes_returnsRoundedValues() {
        assertEquals(1.23, MathUtils.round(1.23456, 2), 1e-10);
        assertEquals(1.24, MathUtils.round(1.23556, 2), 1e-10);
        assertEquals(1.23, MathUtils.round(1.23556, 2, BigDecimal.ROUND_DOWN), 1e-10);
        assertEquals(1.24, MathUtils.round(1.23456, 2, BigDecimal.ROUND_UP), 1e-10);
        assertEquals(1.23f, MathUtils.round(1.23456f, 2), 1e-5f);
        assertEquals(1.24f, MathUtils.round(1.23556f, 2), 1e-5f);
    }

    // Tests angle normalization and array normalization
    @Test
    public void testNormalize_angleAndArray_returnsNormalizedResults() {
        double normalizedAngle = MathUtils.normalizeAngle(4 * FastMath.PI + 0.5, 0.0);
        assertEquals(0.5, normalizedAngle, 1e-10);

        double[] input = new double[] {1.0, 2.0, 3.0};
        double[] normalized = MathUtils.normalizeArray(input, 12.0);
        assertEquals(2.0, normalized[0], 1e-10);
        assertEquals(4.0, normalized[1], 1e-10);
        assertEquals(6.0, normalized[2], 1e-10);
    }

    // Tests distance functions (L1, L2, Linf) for double and int arrays
    @Test
    public void testDistance_variousMetrics_returnsCorrectDistance() {
        double[] p1 = new double[] {1.0, 2.0};
        double[] p2 = new double[] {4.0, 6.0};
        assertEquals(7.0, MathUtils.distance1(p1, p2), 1e-10);
        assertEquals(5.0, MathUtils.distance(p1, p2), 1e-10);
        assertEquals(4.0, MathUtils.distanceInf(p1, p2), 1e-10);

        int[] ip1 = new int[] {1, 2};
        int[] ip2 = new int[] {4, 6};
        assertEquals(7, MathUtils.distance1(ip1, ip2));
        assertEquals(5.0, MathUtils.distance(ip1, ip2), 1e-10);
        assertEquals(4, MathUtils.distanceInf(ip1, ip2));
    }

    // Tests checkOrder monotonicity validation and exception on violation
    @Test(expected = NonMonotonousSequenceException.class)
    public void testCheckOrder_nonMonotonousSequence_throwsException() {
        double[] strictlyIncreasing = new double[] {1.0, 2.0, 3.0};
        MathUtils.checkOrder(strictlyIncreasing);
        MathUtils.checkOrder(strictlyIncreasing, MathUtils.OrderDirection.INCREASING, true);

        double[] decreasing = new double[] {3.0, 2.0, 1.0};
        MathUtils.checkOrder(decreasing, MathUtils.OrderDirection.DECREASING, true);

        double[] nonMonotonous = new double[] {1.0, 3.0, 2.0};
        MathUtils.checkOrder(nonMonotonous);
    }

    // Tests safeNorm, cosh, sinh, scalb, and hash utilities
    @Test
    public void testMiscellaneous_mathFunctions_returnsCorrectResults() {
        double[] vec = new double[] {3.0, 4.0};
        assertEquals(5.0, MathUtils.safeNorm(vec), 1e-10);
        assertEquals(1.0, MathUtils.cosh(0.0), 1e-10);
        assertEquals(0.0, MathUtils.sinh(0.0), 1e-10);
        assertEquals(8.0, MathUtils.scalb(2.0, 2), 1e-10);
        assertEquals(new Double(5.0).hashCode(), MathUtils.hash(5.0));
        assertEquals(MathUtils.hash(new double[] {1.0, 2.0}), MathUtils.hash(new double[] {1.0, 2.0}));
    }
}