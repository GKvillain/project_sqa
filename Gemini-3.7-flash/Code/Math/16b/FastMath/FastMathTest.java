package org.apache.commons.math3.util;

import org.junit.Test;
import static org.junit.Assert.*;

public class FastMathTest {

    private static final double EPSILON = 1e-15;

    // Tests hyperbolic cosine with large positive value (MATH-905 / Defect 16b check)
    @Test
    public void testCosh_largePositiveValue_returnsInfinityOrCorrectValue() {
        double result = FastMath.cosh(710.0);
        assertTrue(Double.isInfinite(result) || result > 0.0);
        assertEquals(Math.cosh(710.0), result, EPSILON);
    }

    // Tests hyperbolic cosine with large negative value (MATH-905 / Defect 16b check)
    @Test
    public void testCosh_largeNegativeValue_returnsInfinityOrCorrectValue() {
        double result = FastMath.cosh(-710.0);
        assertTrue(Double.isInfinite(result) || result > 0.0);
        assertEquals(Math.cosh(-710.0), result, EPSILON);
    }

    // Tests hyperbolic sine with large positive value (MATH-905 / Defect 16b check)
    @Test
    public void testSinh_largePositiveValue_returnsInfinityOrCorrectValue() {
        double result = FastMath.sinh(710.0);
        assertTrue(Double.isInfinite(result) || result > 0.0);
        assertEquals(Math.sinh(710.0), result, EPSILON);
    }

    // Tests hyperbolic sine with large negative value (MATH-905 / Defect 16b check)
    @Test
    public void testSinh_largeNegativeValue_returnsInfinityOrCorrectValue() {
        double result = FastMath.sinh(-710.0);
        assertTrue(Double.isInfinite(result) || result < 0.0);
        assertEquals(Math.sinh(-710.0), result, EPSILON);
    }

    // Tests hyperbolic cosine and sine around standard values
    @Test
    public void testCoshSinh_normalAndBoundaryValues_returnsExpected() {
        assertEquals(1.0, FastMath.cosh(0.0), EPSILON);
        assertEquals(0.0, FastMath.sinh(0.0), EPSILON);
        assertEquals(Math.cosh(1.0), FastMath.cosh(1.0), 1e-14);
        assertEquals(Math.sinh(1.0), FastMath.sinh(1.0), 1e-14);
        assertEquals(Math.cosh(0.2), FastMath.cosh(0.2), 1e-14);
        assertEquals(Math.sinh(0.2), FastMath.sinh(0.2), 1e-14);
        assertEquals(Math.cosh(-1.0), FastMath.cosh(-1.0), 1e-14);
        assertEquals(Math.sinh(-1.0), FastMath.sinh(-1.0), 1e-14);
    }

    // Tests hyperbolic tangent with large and normal values
    @Test
    public void testTanh_variousValues_returnsExpected() {
        assertEquals(1.0, FastMath.tanh(25.0), EPSILON);
        assertEquals(-1.0, FastMath.tanh(-25.0), EPSILON);
        assertEquals(0.0, FastMath.tanh(0.0), EPSILON);
        assertEquals(Math.tanh(0.2), FastMath.tanh(0.2), 1e-14);
        assertEquals(Math.tanh(1.5), FastMath.tanh(1.5), 1e-14);
    }

    // Tests inverse hyperbolic functions
    @Test
    public void testInverseHyperbolic_validInputs_returnsExpected() {
        assertEquals(0.0, FastMath.acosh(1.0), EPSILON);
        assertEquals(0.881373587019543, FastMath.asinh(1.0), 1e-14);
        assertEquals(-0.881373587019543, FastMath.asinh(-1.0), 1e-14);
        assertEquals(0.05, FastMath.asinh(0.05), 1e-4);
        assertEquals(0.5493061443340548, FastMath.atanh(0.5), 1e-14);
        assertEquals(-0.5493061443340548, FastMath.atanh(-0.5), 1e-14);
        assertEquals(0.05, FastMath.atanh(0.05), 1e-4);
    }

    // Tests natural logarithm and edge cases
    @Test
    public void testLog_variousInputs_returnsExpected() {
        assertEquals(Double.NEGATIVE_INFINITY, FastMath.log(0.0), EPSILON);
        assertTrue(Double.isNaN(FastMath.log(-1.0)));
        assertEquals(Double.POSITIVE_INFINITY, FastMath.log(Double.POSITIVE_INFINITY), EPSILON);
        assertEquals(0.0, FastMath.log(1.0), EPSILON);
        assertEquals(1.0, FastMath.log(FastMath.E), 1e-14);
        assertEquals(Math.log(1.005), FastMath.log(1.005), 1e-14);
        assertEquals(Math.log(100.0), FastMath.log(100.0), 1e-14);
    }

    // Tests log1p and log10
    @Test
    public void testLog1pAndLog10_variousInputs_returnsExpected() {
        assertEquals(Double.NEGATIVE_INFINITY, FastMath.log1p(-1.0), EPSILON);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.log1p(Double.POSITIVE_INFINITY), EPSILON);
        assertEquals(Math.log1p(1e-7), FastMath.log1p(1e-7), 1e-14);
        assertEquals(Math.log1p(0.5), FastMath.log1p(0.5), 1e-14);
        assertEquals(2.0, FastMath.log10(100.0), 1e-14);
        assertEquals(1.0, FastMath.log(10.0, 10.0), 1e-14);
    }

    // Tests exponential function and expm1
    @Test
    public void testExpAndExpm1_variousInputs_returnsExpected() {
        assertEquals(1.0, FastMath.exp(0.0), EPSILON);
        assertEquals(FastMath.E, FastMath.exp(1.0), 1e-14);
        assertEquals(0.0, FastMath.exp(-800.0), EPSILON);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.exp(800.0), EPSILON);
        assertEquals(0.0, FastMath.expm1(0.0), EPSILON);
        assertEquals(Math.expm1(0.5), FastMath.expm1(0.5), 1e-14);
        assertEquals(Math.expm1(-0.5), FastMath.expm1(-0.5), 1e-14);
        assertEquals(Math.expm1(2.0), FastMath.expm1(2.0), 1e-14);
    }

    // Tests power function with double and integer exponents
    @Test
    public void testPow_variousInputs_returnsExpected() {
        assertEquals(1.0, FastMath.pow(5.0, 0.0), EPSILON);
        assertEquals(8.0, FastMath.pow(2.0, 3.0), EPSILON);
        assertEquals(8.0, FastMath.pow(2.0, 3), EPSILON);
        assertEquals(0.125, FastMath.pow(2.0, -3), EPSILON);
        assertEquals(-8.0, FastMath.pow(-2.0, 3.0), EPSILON);
        assertEquals(4.0, FastMath.pow(-2.0, 2.0), EPSILON);
        assertTrue(Double.isNaN(FastMath.pow(-2.0, 2.5)));
    }

    // Tests trigonometric functions sin, cos, tan
    @Test
    public void testTrigonometric_specialAndNormalValues_returnsExpected() {
        assertEquals(0.0, FastMath.sin(0.0), EPSILON);
        assertEquals(1.0, FastMath.cos(0.0), EPSILON);
        assertEquals(0.0, FastMath.tan(0.0), EPSILON);
        assertEquals(Math.sin(Math.PI / 6.0), FastMath.sin(FastMath.PI / 6.0), 1e-14);
        assertEquals(Math.cos(Math.PI / 3.0), FastMath.cos(FastMath.PI / 3.0), 1e-14);
        assertEquals(Math.tan(Math.PI / 4.0), FastMath.tan(FastMath.PI / 4.0), 1e-14);
        assertEquals(Math.sin(10000000.0), FastMath.sin(10000000.0), 1e-10);
        assertEquals(Math.cos(10000000.0), FastMath.cos(10000000.0), 1e-10);
    }

    // Tests inverse trigonometric functions asin, acos, atan, atan2
    @Test
    public void testInverseTrigonometric_validInputs_returnsExpected() {
        assertEquals(Math.PI / 2.0, FastMath.asin(1.0), 1e-14);
        assertEquals(-Math.PI / 2.0, FastMath.asin(-1.0), 1e-14);
        assertEquals(0.0, FastMath.asin(0.0), EPSILON);
        assertTrue(Double.isNaN(FastMath.asin(2.0)));
        assertEquals(0.0, FastMath.acos(1.0), 1e-14);
        assertEquals(Math.PI, FastMath.acos(-1.0), 1e-14);
        assertEquals(Math.PI / 2.0, FastMath.acos(0.0), 1e-14);
        assertEquals(Math.PI / 4.0, FastMath.atan(1.0), 1e-14);
        assertEquals(Math.PI / 4.0, FastMath.atan2(1.0, 1.0), 1e-14);
        assertEquals(3.0 * Math.PI / 4.0, FastMath.atan2(1.0, -1.0), 1e-14);
    }

    // Tests cbrt and sqrt methods
    @Test
    public void testCbrtAndSqrt_variousValues_returnsExpected() {
        assertEquals(2.0, FastMath.sqrt(4.0), EPSILON);
        assertEquals(3.0, FastMath.cbrt(27.0), 1e-14);
        assertEquals(-3.0, FastMath.cbrt(-27.0), 1e-14);
        assertEquals(0.0, FastMath.cbrt(0.0), EPSILON);
    }

    // Tests rounding and integer boundary functions
    @Test
    public void testRoundingFunctions_variousInputs_returnsExpected() {
        assertEquals(2.0, FastMath.floor(2.7), EPSILON);
        assertEquals(-3.0, FastMath.floor(-2.7), EPSILON);
        assertEquals(3.0, FastMath.ceil(2.3), EPSILON);
        assertEquals(-2.0, FastMath.ceil(-2.3), EPSILON);
        assertEquals(2.0, FastMath.rint(2.5), EPSILON);
        assertEquals(4.0, FastMath.rint(3.5), EPSILON);
        assertEquals(3L, FastMath.round(2.6));
        assertEquals(3, FastMath.round(2.6f));
    }

    // Tests signum, copySign and abs for double and float
    @Test
    public void testSignumCopySignAndAbs_variousInputs_returnsExpected() {
        assertEquals(1.0, FastMath.signum(5.0), EPSILON);
        assertEquals(-1.0, FastMath.signum(-5.0), EPSILON);
        assertEquals(1.0f, FastMath.signum(5.0f), 0.0f);
        assertEquals(-1.0f, FastMath.signum(-5.0f), 0.0f);
        assertEquals(5.0, FastMath.copySign(5.0, -1.0), -5.0);
        assertEquals(5.0f, FastMath.copySign(5.0f, -1.0f), -5.0f);
        assertEquals(5, FastMath.abs(-5));
        assertEquals(5L, FastMath.abs(-5L));
        assertEquals(5.0f, FastMath.abs(-5.0f), 0.0f);
        assertEquals(5.0, FastMath.abs(-5.0), EPSILON);
    }

    // Tests min, max and hypot
    @Test
    public void testMinMaxAndHypot_variousInputs_returnsExpected() {
        assertEquals(1, FastMath.min(1, 2));
        assertEquals(2, FastMath.max(1, 2));
        assertEquals(1L, FastMath.min(1L, 2L));
        assertEquals(2L, FastMath.max(1L, 2L));
        assertEquals(1.0f, FastMath.min(1.0f, 2.0f), 0.0f);
        assertEquals(2.0f, FastMath.max(1.0f, 2.0f), 0.0f);
        assertEquals(1.0, FastMath.min(1.0, 2.0), EPSILON);
        assertEquals(2.0, FastMath.max(1.0, 2.0), EPSILON);
        assertEquals(5.0, FastMath.hypot(3.0, 4.0), EPSILON);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.hypot(Double.POSITIVE_INFINITY, 1.0), EPSILON);
    }

    // Tests scalb, getExponent, nextAfter, nextUp and ulp
    @Test
    public void testFloatingPointManipulations_variousInputs_returnsExpected() {
        assertEquals(8.0, FastMath.scalb(2.0, 2), EPSILON);
        assertEquals(8.0f, FastMath.scalb(2.0f, 2), 0.0f);
        assertEquals(3, FastMath.getExponent(8.0));
        assertEquals(3, FastMath.getExponent(8.0f));
        assertEquals(Math.nextUp(1.0), FastMath.nextUp(1.0), EPSILON);
        assertEquals(Math.nextUp(1.0f), FastMath.nextUp(1.0f), 0.0f);
        assertEquals(Math.nextAfter(1.0, 2.0), FastMath.nextAfter(1.0, 2.0), EPSILON);
        assertEquals(Math.nextAfter(1.0f, 2.0), FastMath.nextAfter(1.0f, 2.0), 0.0f);
        assertEquals(Math.ulp(1.0), FastMath.ulp(1.0), EPSILON);
        assertEquals(Math.ulp(1.0f), FastMath.ulp(1.0f), 0.0f);
    }

    // Tests angle conversions and IEEEremainder
    @Test
    public void testAngleConversionsAndRemainder_variousInputs_returnsExpected() {
        assertEquals(FastMath.PI, FastMath.toRadians(180.0), 1e-14);
        assertEquals(180.0, FastMath.toDegrees(FastMath.PI), 1e-14);
        assertEquals(0.0, FastMath.toRadians(0.0), EPSILON);
        assertEquals(0.0, FastMath.toDegrees(0.0), EPSILON);
        assertEquals(StrictMath.IEEEremainder(5.0, 3.0), FastMath.IEEEremainder(5.0, 3.0), EPSILON);
        double rand = FastMath.random();
        assertTrue(rand >= 0.0 && rand < 1.0);
    }
}