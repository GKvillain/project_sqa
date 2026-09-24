package org.apache.commons.math.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.apache.commons.math.MathRuntimeException;
import org.junit.Test;
import static org.junit.Assert.*;

public class MathUtilsTest {

    // Tests distance between integer coordinates, checks for potential integer overflow during square calculation
    @Test
    public void testDistance_largeIntCoordinates_returnsCorrectDistance() {
        int[] p1 = new int[] { Integer.MAX_VALUE / 2, 0 };
        int[] p2 = new int[] { -Integer.MAX_VALUE / 2, 0 };
        double expected = (double) Integer.MAX_VALUE;
        double actual = MathUtils.distance(p1, p2);
        assertEquals(expected, actual, 1.0);
    }

    // Tests distance, distance1, and distanceInf for double and int arrays
    @Test
    public void testDistanceMetrics_variousInputs_returnsAccurateDistances() {
        double[] d1 = new double[] { 1.0, 2.0, 3.0 };
        double[] d2 = new double[] { 4.0, 6.0, 3.0 };
        assertEquals(5.0, MathUtils.distance(d1, d2), 1e-10);
        assertEquals(7.0, MathUtils.distance1(d1, d2), 1e-10);
        assertEquals(4.0, MathUtils.distanceInf(d1, d2), 1e-10);

        int[] i1 = new int[] { 1, 2, 3 };
        int[] i2 = new int[] { 4, 6, 3 };
        assertEquals(5.0, MathUtils.distance(i1, i2), 1e-10);
        assertEquals(7, MathUtils.distance1(i1, i2));
        assertEquals(4, MathUtils.distanceInf(i1, i2));
    }

    // Tests addAndCheck with int and long for normal and overflow cases
    @Test
    public void testAddAndCheck_normalAndOverflow_returnsSumOrThrowsException() {
        assertEquals(15, MathUtils.addAndCheck(7, 8));
        assertEquals(15L, MathUtils.addAndCheck(7L, 8L));

        try {
            MathUtils.addAndCheck(Integer.MAX_VALUE, 1);
            fail("Expected ArithmeticException on int add overflow");
        } catch (ArithmeticException ex) {
            // expected
        }

        try {
            MathUtils.addAndCheck(Long.MIN_VALUE, -1L);
            fail("Expected ArithmeticException on long add overflow");
        } catch (ArithmeticException ex) {
            // expected
        }
    }

    // Tests subAndCheck for int and long values
    @Test
    public void testSubAndCheck_normalAndOverflow_returnsDiffOrThrowsException() {
        assertEquals(-1, MathUtils.subAndCheck(7, 8));
        assertEquals(-1L, MathUtils.subAndCheck(7L, 8L));

        try {
            MathUtils.subAndCheck(Integer.MIN_VALUE, 1);
            fail("Expected ArithmeticException on int sub overflow");
        } catch (ArithmeticException ex) {
            // expected
        }

        try {
            MathUtils.subAndCheck(Long.MAX_VALUE, -1L);
            fail("Expected ArithmeticException on long sub overflow");
        } catch (ArithmeticException ex) {
            // expected
        }
    }

    // Tests mulAndCheck for int and long values
    @Test
    public void testMulAndCheck_normalAndOverflow_returnsProductOrThrowsException() {
        assertEquals(56, MathUtils.mulAndCheck(7, 8));
        assertEquals(56L, MathUtils.mulAndCheck(7L, 8L));
        assertEquals(0L, MathUtils.mulAndCheck(0L, 5L));

        try {
            MathUtils.mulAndCheck(Integer.MAX_VALUE, 2);
            fail("Expected ArithmeticException on int mul overflow");
        } catch (ArithmeticException ex) {
            // expected
        }

        try {
            MathUtils.mulAndCheck(Long.MAX_VALUE, 2L);
            fail("Expected ArithmeticException on long mul overflow");
        } catch (ArithmeticException ex) {
            // expected
        }
    }

    // Tests gcd for standard, negative, and edge values
    @Test
    public void testGcd_variousInputs_returnsGreatestCommonDivisor() {
        assertEquals(6, MathUtils.gcd(12, 18));
        assertEquals(6, MathUtils.gcd(-12, 18));
        assertEquals(12, MathUtils.gcd(12, 0));
        assertEquals(0, MathUtils.gcd(0, 0));
    }

    // Tests gcd overflow when given Integer.MIN_VALUE
    @Test(expected = ArithmeticException.class)
    public void testGcd_minIntValue_throwsArithmeticException() {
        MathUtils.gcd(Integer.MIN_VALUE, 0);
    }

    // Tests lcm for regular and zero inputs
    @Test
    public void testLcm_normalAndZero_returnsLeastCommonMultiple() {
        assertEquals(36, MathUtils.lcm(12, 18));
        assertEquals(0, MathUtils.lcm(0, 18));
        assertEquals(0, MathUtils.lcm(12, 0));
    }

    // Tests binomialCoefficient for valid boundaries and overflow
    @Test
    public void testBinomialCoefficient_variousInputs_returnsCorrectValue() {
        assertEquals(1L, MathUtils.binomialCoefficient(5, 0));
        assertEquals(1L, MathUtils.binomialCoefficient(5, 5));
        assertEquals(5L, MathUtils.binomialCoefficient(5, 1));
        assertEquals(10L, MathUtils.binomialCoefficient(5, 2));
        assertEquals(10L, MathUtils.binomialCoefficient(5, 3));
        assertEquals(2598960L, MathUtils.binomialCoefficient(52, 5));
        assertEquals(10.0, MathUtils.binomialCoefficientDouble(5, 2), 1e-10);
        assertEquals(Math.log(10.0), MathUtils.binomialCoefficientLog(5, 2), 1e-10);
    }

    // Tests binomialCoefficient invalid argument exceptions
    @Test(expected = IllegalArgumentException.class)
    public void testBinomialCoefficient_kGreaterThanN_throwsException() {
        MathUtils.binomialCoefficient(4, 5);
    }

    // Tests factorial functions and exceptions
    @Test
    public void testFactorial_validAndInvalidInputs_returnsFactorialOrThrowsException() {
        assertEquals(1L, MathUtils.factorial(0));
        assertEquals(1L, MathUtils.factorial(1));
        assertEquals(120L, MathUtils.factorial(5));
        assertEquals(120.0, MathUtils.factorialDouble(5), 1e-10);
        assertEquals(Math.log(120.0), MathUtils.factorialLog(5), 1e-10);

        try {
            MathUtils.factorial(-1);
            fail("Expected IllegalArgumentException for negative n");
        } catch (IllegalArgumentException ex) {
            // expected
        }

        try {
            MathUtils.factorial(21);
            fail("Expected ArithmeticException for n > 20");
        } catch (ArithmeticException ex) {
            // expected
        }
    }

    // Tests pow methods with various primitive and BigInteger types
    @Test
    public void testPow_variousTypes_returnsPower() {
        assertEquals(8, MathUtils.pow(2, 3));
        assertEquals(8, MathUtils.pow(2, 3L));
        assertEquals(8L, MathUtils.pow(2L, 3));
        assertEquals(8L, MathUtils.pow(2L, 3L));

        BigInteger two = BigInteger.valueOf(2);
        assertEquals(BigInteger.valueOf(8), MathUtils.pow(two, 3));
        assertEquals(BigInteger.valueOf(8), MathUtils.pow(two, 3L));
        assertEquals(BigInteger.valueOf(8), MathUtils.pow(two, BigInteger.valueOf(3)));
    }

    // Tests pow with negative exponent
    @Test(expected = IllegalArgumentException.class)
    public void testPow_negativeExponent_throwsException() {
        MathUtils.pow(2, -1);
    }

    // Tests round methods with different BigDecimal rounding modes
    @Test
    public void testRound_variousModes_returnsRoundedValue() {
        assertEquals(1.23, MathUtils.round(1.234, 2), 1e-10);
        assertEquals(1.24, MathUtils.round(1.235, 2, BigDecimal.ROUND_HALF_UP), 1e-10);
        assertEquals(1.23f, MathUtils.round(1.234f, 2), 1e-5f);
        assertEquals(1.24f, MathUtils.round(1.235f, 2, BigDecimal.ROUND_HALF_UP), 1e-5f);
    }

    // Tests sign and indicator functions across primitive types
    @Test
    public void testSignAndIndicator_positiveZeroNegative_returnsCorrectSigns() {
        assertEquals(1, MathUtils.sign(10));
        assertEquals(0, MathUtils.sign(0));
        assertEquals(-1, MathUtils.sign(-10));

        assertEquals(1.0, MathUtils.sign(10.0), 1e-10);
        assertEquals(0.0, MathUtils.sign(0.0), 1e-10);
        assertEquals(-1.0, MathUtils.sign(-10.0), 1e-10);
        assertTrue(Double.isNaN(MathUtils.sign(Double.NaN)));

        assertEquals(1, MathUtils.indicator(10));
        assertEquals(1, MathUtils.indicator(0));
        assertEquals(-1, MathUtils.indicator(-10));
        assertEquals(1.0, MathUtils.indicator(0.0), 1e-10);
        assertEquals(-1.0, MathUtils.indicator(-0.5), 1e-10);
        assertTrue(Double.isNaN(MathUtils.indicator(Double.NaN)));
    }

    // Tests equals comparison for double values and arrays
    @Test
    public void testEquals_scalarsAndArrays_evaluatesEquality() {
        assertTrue(MathUtils.equals(Double.NaN, Double.NaN));
        assertTrue(MathUtils.equals(1.0, 1.0));
        assertFalse(MathUtils.equals(1.0, 2.0));
        assertTrue(MathUtils.equals(1.0, 1.05, 0.1));
        assertFalse(MathUtils.equals(1.0, 1.05, 0.01));
        assertTrue(MathUtils.equals(1.0, 1.0, 1));

        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
        assertFalse(MathUtils.equals(new double[] { 1.0 }, null));
        assertTrue(MathUtils.equals(new double[] { 1.0, Double.NaN }, new double[] { 1.0, Double.NaN }));
        assertFalse(MathUtils.equals(new double[] { 1.0 }, new double[] { 1.0, 2.0 }));
    }

    // Tests compareTo with epsilon
    @Test
    public void testCompareTo_withEpsilon_returnsZeroOrSign() {
        assertEquals(0, MathUtils.compareTo(1.0, 1.05, 0.1));
        assertEquals(-1, MathUtils.compareTo(1.0, 1.2, 0.1));
        assertEquals(1, MathUtils.compareTo(1.2, 1.0, 0.1));
    }

    // Tests normalizeAngle to keep angles within 2pi range
    @Test
    public void testNormalizeAngle_variousAngles_normalizesWithinInterval() {
        assertEquals(0.0, MathUtils.normalizeAngle(0.0, 0.0), 1e-10);
        assertEquals(Math.PI, MathUtils.normalizeAngle(3 * Math.PI, 0.0), 1e-10);
        assertEquals(-Math.PI, MathUtils.normalizeAngle(-3 * Math.PI, 0.0), 1e-10);
    }

    // Tests normalizeArray to ensure sum matches target
    @Test
    public void testNormalizeArray_validArray_returnsNormalizedArray() {
        double[] values = new double[] { 1.0, 2.0, 3.0 };
        double[] normalized = MathUtils.normalizeArray(values, 12.0);
        assertEquals(2.0, normalized[0], 1e-10);
        assertEquals(4.0, normalized[1], 1e-10);
        assertEquals(6.0, normalized[2], 1e-10);
    }

    // Tests hyperbolic functions, log, and scalb
    @Test
    public void testSpecialMathFunctions_validInputs_returnsExpectedResults() {
        assertEquals(1.0, MathUtils.cosh(0.0), 1e-10);
        assertEquals(0.0, MathUtils.sinh(0.0), 1e-10);
        assertEquals(3.0, MathUtils.log(2.0, 8.0), 1e-10);
        assertEquals(8.0, MathUtils.scalb(2.0, 2), 1e-10);
        assertEquals(1.0 + MathUtils.EPSILON, MathUtils.nextAfter(1.0, 2.0), 1e-20);
        assertNotNull(MathUtils.hash(1.234));
        assertNotNull(MathUtils.hash(new double[] { 1.0, 2.0 }));
    }

    // Additional tests for missing branch coverage

    @Test
    public void testDistanceDimensionMismatch_throwsException() {
        try {
            MathUtils.distance(new double[] { 1.0 }, new double[] { 1.0, 2.0 });
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // expected
        }

        try {
            MathUtils.distance(new int[] { 1 }, new int[] { 1, 2 });
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // expected
        }

        try {
            MathUtils.distance1(new double[] { 1.0 }, new double[] { 1.0, 2.0 });
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // expected
        }

        try {
            MathUtils.distance1(new int[] { 1 }, new int[] { 1, 2 });
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // expected
        }

        try {
            MathUtils.distanceInf(new double[] { 1.0 }, new double[] { 1.0, 2.0 });
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // expected
        }

        try {
            MathUtils.distanceInf(new int[] { 1 }, new int[] { 1, 2 });
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // expected
        }
    }

    @Test
    public void testAddSubMulCheck_additionalOverflowBranches() {
        try {
            MathUtils.addAndCheck(Integer.MIN_VALUE, -1);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException ex) {
            // expected
        }

        try {
            MathUtils.addAndCheck(Long.MAX_VALUE, 1L);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException ex) {
            // expected
        }

        try {
            MathUtils.subAndCheck(Integer.MAX_VALUE, -1);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException ex) {
            // expected
        }

        try {
            MathUtils.subAndCheck(Long.MIN_VALUE, 1L);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException ex) {
            // expected
        }

        try {
            MathUtils.mulAndCheck(Integer.MIN_VALUE, 2);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException ex) {
            // expected
        }

        try {
            MathUtils.mulAndCheck(Long.MIN_VALUE, 2L);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException ex) {
            // expected
        }

        try {
            MathUtils.mulAndCheck(Long.MIN_VALUE, -1L);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException ex) {
            // expected
        }

        assertEquals(0, MathUtils.mulAndCheck(0, 5));
        assertEquals(0L, MathUtils.mulAndCheck(5L, 0L));
    }

    @Test
    public void testLcm_overflow_throwsException() {
        try {
            MathUtils.lcm(Integer.MAX_VALUE, 2);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException ex) {
            // expected
        }
    }

    @Test
    public void testBinomialCoefficient_edgeAndExceptionBranches() {
        try {
            MathUtils.binomialCoefficient(-1, 0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // expected
        }

        try {
            MathUtils.binomialCoefficient(5, -1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // expected
        }

        try {
            MathUtils.binomialCoefficientDouble(-1, 0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // expected
        }

        try {
            MathUtils.binomialCoefficientDouble(4, 5);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // expected
        }

        try {
            MathUtils.binomialCoefficientLog(-1, 0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // expected
        }

        try {
            MathUtils.binomialCoefficientLog(4, 5);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // expected
        }

        assertEquals(1.0, MathUtils.binomialCoefficientDouble(5, 0), 1e-10);
        assertEquals(1.0, MathUtils.binomialCoefficientDouble(5, 5), 1e-10);
        assertEquals(0.0, MathUtils.binomialCoefficientLog(5, 0), 1e-10);
        assertEquals(0.0, MathUtils.binomialCoefficientLog(5, 5), 1e-10);

        try {
            MathUtils.binomialCoefficient(67, 30);
            fail("Expected ArithmeticException on binomial coefficient overflow");
        } catch (ArithmeticException ex) {
            // expected
        }
    }

    @Test
    public void testFactorial_doubleAndLogEdgeCases() {
        try {
            MathUtils.factorialDouble(-1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // expected
        }

        try {
            MathUtils.factorialLog(-1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // expected
        }

        assertEquals(1.0, MathUtils.factorialDouble(0), 1e-10);
        assertEquals(1.0, MathUtils.factorialDouble(1), 1e-10);
        assertEquals(0.0, MathUtils.factorialLog(0), 1e-10);
        assertEquals(0.0, MathUtils.factorialLog(1), 1e-10);
        assertTrue(MathUtils.factorialDouble(171) == Double.POSITIVE_INFINITY);
    }

    @Test
    public void testPow_zeroExponentAndBigIntegerExceptions() {
        assertEquals(1, MathUtils.pow(2, 0));
        assertEquals(1, MathUtils.pow(2, 0L));
        assertEquals(1L, MathUtils.pow(2L, 0));
        assertEquals(1L, MathUtils.pow(2L, 0L));

        BigInteger two = BigInteger.valueOf(2);
        assertEquals(BigInteger.ONE, MathUtils.pow(two, 0));
        assertEquals(BigInteger.ONE, MathUtils.pow(two, 0L));
        assertEquals(BigInteger.ONE, MathUtils.pow(two, BigInteger.ZERO));

        try {
            MathUtils.pow(two, -1L);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // expected
        }

        try {
            MathUtils.pow(two, BigInteger.valueOf(-1));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // expected
        }
    }

    @Test
    public void testRound_nanAndInfSpecialCases() {
        assertEquals(Double.NaN, MathUtils.round(Double.NaN, 2), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, MathUtils.round(Double.POSITIVE_INFINITY, 2), 0.0);
        assertEquals(Double.NEGATIVE_INFINITY, MathUtils.round(Double.NEGATIVE_INFINITY, 2), 0.0);

        assertEquals(Float.NaN, MathUtils.round(Float.NaN, 2), 0.0f);
        assertEquals(Float.POSITIVE_INFINITY, MathUtils.round(Float.POSITIVE_INFINITY, 2), 0.0f);
        assertEquals(Float.NEGATIVE_INFINITY, MathUtils.round(Float.NEGATIVE_INFINITY, 2), 0.0f);
    }

    @Test
    public void testSignAndIndicator_byteShortLongFloatTypes() {
        assertEquals((byte) 1, MathUtils.sign((byte) 10));
        assertEquals((byte) 0, MathUtils.sign((byte) 0));
        assertEquals((byte) -1, MathUtils.sign((byte) -10));

        assertEquals((short) 1, MathUtils.sign((short) 10));
        assertEquals((short) 0, MathUtils.sign((short) 0));
        assertEquals((short) -1, MathUtils.sign((short) -10));

        assertEquals(1L, MathUtils.sign(10L));
        assertEquals(0L, MathUtils.sign(0L));
        assertEquals(-1L, MathUtils.sign(-10L));

        assertEquals(1.0f, MathUtils.sign(10.0f), 1e-5f);
        assertEquals(0.0f, MathUtils.sign(0.0f), 1e-5f);
        assertEquals(-1.0f, MathUtils.sign(-10.0f), 1e-5f);
        assertTrue(Float.isNaN(MathUtils.sign(Float.NaN)));

        assertEquals((byte) 1, MathUtils.indicator((byte) 10));
        assertEquals((byte) 1, MathUtils.indicator((byte) 0));
        assertEquals((byte) -1, MathUtils.indicator((byte) -10));

        assertEquals((short) 1, MathUtils.indicator((short) 10));
        assertEquals((short) 1, MathUtils.indicator((short) 0));
        assertEquals((short) -1, MathUtils.indicator((short) -10));

        assertEquals(1L, MathUtils.indicator(10L));
        assertEquals(1L, MathUtils.indicator(0L));
        assertEquals(-1L, MathUtils.indicator(-10L));

        assertEquals(1.0f, MathUtils.indicator(10.0f), 1e-5f);
        assertEquals(1.0f, MathUtils.indicator(0.0f), 1e-5f);
        assertEquals(-1.0f, MathUtils.indicator(-10.0f), 1e-5f);
        assertTrue(Float.isNaN(MathUtils.indicator(Float.NaN)));
    }

    @Test
    public void testEquals_floatAndArrayVariants() {
        assertTrue(MathUtils.equals(Float.NaN, Float.NaN));
        assertTrue(MathUtils.equals(1.0f, 1.0f));
        assertFalse(MathUtils.equals(1.0f, 2.0f));
        assertTrue(MathUtils.equals(1.0f, 1.05f, 0.1f));
        assertFalse(MathUtils.equals(1.0f, 1.05f, 0.01f));
        assertTrue(MathUtils.equals(1.0f, 1.0f, 1));

        assertTrue(MathUtils.equals((float[]) null, (float[]) null));
        assertFalse(MathUtils.equals(new float[] { 1.0f }, null));
        assertFalse(MathUtils.equals(null, new float[] { 1.0f }));
        assertTrue(MathUtils.equals(new float[] { 1.0f, Float.NaN }, new float[] { 1.0f, Float.NaN }));
        assertFalse(MathUtils.equals(new float[] { 1.0f }, new float[] { 1.0f, 2.0f }));
        assertFalse(MathUtils.equals(new float[] { 1.0f }, new float[] { 2.0f }));

        assertFalse(MathUtils.equals(null, new double[] { 1.0 }));
        assertFalse(MathUtils.equals(new double[] { 1.0 }, new double[] { 2.0 }));

        assertTrue(MathUtils.equals(Double.NaN, Double.NaN, 1));
        assertTrue(MathUtils.equals(1.0, 1.0 + 1e-15, 10));
        assertFalse(MathUtils.equals(1.0, 2.0, 1));
    }

    @Test
    public void testNormalizeArray_exceptionalInputs() {
        try {
            MathUtils.normalizeArray(new double[] { 0.0, 0.0 }, 1.0);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException ex) {
            // expected
        }

        try {
            MathUtils.normalizeArray(new double[] { Double.POSITIVE_INFINITY, 1.0 }, 1.0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // expected
        }

        try {
            MathUtils.normalizeArray(new double[] { Double.NaN, 1.0 }, 1.0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // expected
        }
    }

    @Test
    public void testNextAfter_variousDirections() {
        assertEquals(1.0 - MathUtils.EPSILON / 2.0, MathUtils.nextAfter(1.0, 0.0), 1e-20);
        assertEquals(1.0, MathUtils.nextAfter(1.0, 1.0), 1e-20);
        assertTrue(Double.isNaN(MathUtils.nextAfter(Double.NaN, 1.0)));
        assertTrue(Double.isNaN(MathUtils.nextAfter(1.0, Double.NaN)));
    }

    @Test
    public void testHash_nullAndNaNArrays() {
        assertEquals(0, MathUtils.hash((double[]) null));
        assertNotNull(MathUtils.hash(new double[] { Double.NaN, 0.0 }));
    }
}