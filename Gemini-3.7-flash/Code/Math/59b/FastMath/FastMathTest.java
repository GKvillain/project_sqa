package org.apache.commons.math.util;

import org.junit.Test;
import static org.junit.Assert.*;

public class FastMathTest {

    private static final double EPSILON = 1e-10;

    // Tests max with float values to catch bug where max(a, b) returns b when a > b
    @Test
    public void testMax_floatFirstArgLarger_returnsFirstArg() {
        assertEquals(50.0f, FastMath.max(50.0f, 10.0f), 0.0f);
        assertEquals(10.0f, FastMath.max(10.0f, 50.0f), 0.0f);
        assertEquals(-10.0f, FastMath.max(-10.0f, -50.0f), 0.0f);
        assertTrue(Float.isNaN(FastMath.max(Float.NaN, 1.0f)));
        assertTrue(Float.isNaN(FastMath.max(1.0f, Float.NaN)));
    }

    // Tests max with double values
    @Test
    public void testMax_doubleValues_returnsMax() {
        assertEquals(50.0, FastMath.max(50.0, 10.0), 0.0);
        assertEquals(50.0, FastMath.max(10.0, 50.0), 0.0);
        assertEquals(-10.0, FastMath.max(-10.0, -50.0), 0.0);
        assertTrue(Double.isNaN(FastMath.max(Double.NaN, 1.0)));
        assertTrue(Double.isNaN(FastMath.max(1.0, Double.NaN)));
    }

    // Tests max with int and long values
    @Test
    public void testMax_intAndLong_returnsMax() {
        assertEquals(50, FastMath.max(50, 10));
        assertEquals(50, FastMath.max(10, 50));
        assertEquals(50L, FastMath.max(50L, 10L));
        assertEquals(50L, FastMath.max(10L, 50L));
    }

    // Tests min with float values
    @Test
    public void testMin_floatValues_returnsMin() {
        assertEquals(10.0f, FastMath.min(50.0f, 10.0f), 0.0f);
        assertEquals(10.0f, FastMath.min(10.0f, 50.0f), 0.0f);
        assertEquals(-50.0f, FastMath.min(-10.0f, -50.0f), 0.0f);
        assertTrue(Float.isNaN(FastMath.min(Float.NaN, 1.0f)));
        assertTrue(Float.isNaN(FastMath.min(1.0f, Float.NaN)));
    }

    // Tests min with double values
    @Test
    public void testMin_doubleValues_returnsMin() {
        assertEquals(10.0, FastMath.min(50.0, 10.0), 0.0);
        assertEquals(10.0, FastMath.min(10.0, 50.0), 0.0);
        assertEquals(-50.0, FastMath.min(-10.0, -50.0), 0.0);
        assertTrue(Double.isNaN(FastMath.min(Double.NaN, 1.0)));
        assertTrue(Double.isNaN(FastMath.min(1.0, Double.NaN)));
    }

    // Tests min with int and long values
    @Test
    public void testMin_intAndLong_returnsMin() {
        assertEquals(10, FastMath.min(50, 10));
        assertEquals(10, FastMath.min(10, 50));
        assertEquals(10L, FastMath.min(50L, 10L));
        assertEquals(10L, FastMath.min(10L, 50L));
    }

    // Tests abs for all primitive types including edge cases
    @Test
    public void testAbs_variousTypes_returnsAbsoluteValue() {
        assertEquals(5, FastMath.abs(-5));
        assertEquals(5, FastMath.abs(5));
        assertEquals(5L, FastMath.abs(-5L));
        assertEquals(5L, FastMath.abs(5L));
        assertEquals(5.5f, FastMath.abs(-5.5f), 0.0f);
        assertEquals(5.5f, FastMath.abs(5.5f), 0.0f);
        assertEquals(5.5, FastMath.abs(-5.5), 0.0);
        assertEquals(5.5, FastMath.abs(5.5), 0.0);
    }

    // Tests sqrt, signum, and nextUp
    @Test
    public void testSqrtSignumNextUp_variousInputs_returnsExpectedResults() {
        assertEquals(3.0, FastMath.sqrt(9.0), EPSILON);
        assertEquals(1.0, FastMath.signum(10.5), EPSILON);
        assertEquals(-1.0, FastMath.signum(-10.5), EPSILON);
        assertEquals(0.0, FastMath.signum(0.0), EPSILON);
        assertTrue(Double.isNaN(FastMath.signum(Double.NaN)));
        assertEquals(Math.nextUp(1.0), FastMath.nextUp(1.0), 0.0);
    }

    // Tests floor, ceil, rint, round
    @Test
    public void testRoundingFunctions_variousInputs_returnsExpectedResults() {
        assertEquals(2.0, FastMath.floor(2.7), 0.0);
        assertEquals(-3.0, FastMath.floor(-2.7), 0.0);
        assertEquals(3.0, FastMath.ceil(2.3), 0.0);
        assertEquals(-2.0, FastMath.ceil(-2.7), 0.0);
        assertEquals(2.0, FastMath.rint(2.5), 0.0);
        assertEquals(4.0, FastMath.rint(3.5), 0.0);
        assertEquals(3L, FastMath.round(2.6));
        assertEquals(3, FastMath.round(2.6f));
    }

    // Tests exp and expm1
    @Test
    public void testExpAndExpm1_normalAndSpecialValues_returnsExpectedResults() {
        assertEquals(1.0, FastMath.exp(0.0), EPSILON);
        assertEquals(Math.E, FastMath.exp(1.0), EPSILON);
        assertEquals(0.0, FastMath.exp(-800.0), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.exp(800.0), 0.0);
        assertEquals(0.0, FastMath.expm1(0.0), 0.0);
        assertEquals(Math.E - 1.0, FastMath.expm1(1.0), EPSILON);
        assertEquals(Math.exp(0.5) - 1.0, FastMath.expm1(0.5), EPSILON);
        assertEquals(Math.exp(-0.5) - 1.0, FastMath.expm1(-0.5), EPSILON);
    }

    // Tests log, log10, log1p
    @Test
    public void testLogFunctions_variousInputs_returnsExpectedResults() {
        assertEquals(0.0, FastMath.log(1.0), EPSILON);
        assertEquals(1.0, FastMath.log(FastMath.E), EPSILON);
        assertTrue(Double.isNaN(FastMath.log(-1.0)));
        assertEquals(Double.NEGATIVE_INFINITY, FastMath.log(0.0), 0.0);
        assertEquals(1.0, FastMath.log10(10.0), EPSILON);
        assertEquals(2.0, FastMath.log10(100.0), EPSILON);
        assertEquals(0.0, FastMath.log1p(0.0), EPSILON);
        assertEquals(Math.log1p(0.5), FastMath.log1p(0.5), EPSILON);
    }

    // Tests pow function
    @Test
    public void testPow_variousCases_returnsExpectedResults() {
        assertEquals(1.0, FastMath.pow(5.0, 0.0), 0.0);
        assertEquals(8.0, FastMath.pow(2.0, 3.0), EPSILON);
        assertEquals(0.25, FastMath.pow(2.0, -2.0), EPSILON);
        assertEquals(-8.0, FastMath.pow(-2.0, 3.0), EPSILON);
        assertEquals(16.0, FastMath.pow(-2.0, 4.0), EPSILON);
        assertTrue(Double.isNaN(FastMath.pow(-2.0, 2.5)));
        assertEquals(Double.POSITIVE_INFINITY, FastMath.pow(Double.POSITIVE_INFINITY, 2.0), 0.0);
    }

    // Tests trigonometric functions sin, cos, tan
    @Test
    public void testTrigonometricFunctions_standardAngles_returnsExpectedResults() {
        assertEquals(0.0, FastMath.sin(0.0), EPSILON);
        assertEquals(1.0, FastMath.sin(FastMath.PI / 2.0), EPSILON);
        assertEquals(1.0, FastMath.cos(0.0), EPSILON);
        assertEquals(0.0, FastMath.cos(FastMath.PI / 2.0), EPSILON);
        assertEquals(1.0, FastMath.tan(FastMath.PI / 4.0), EPSILON);
        assertEquals(0.0, FastMath.tan(0.0), EPSILON);
        assertTrue(Double.isNaN(FastMath.sin(Double.NaN)));
        assertTrue(Double.isNaN(FastMath.cos(Double.POSITIVE_INFINITY)));
    }

    // Tests inverse trigonometric functions asin, acos, atan, atan2
    @Test
    public void testInverseTrigonometricFunctions_standardValues_returnsExpectedResults() {
        assertEquals(FastMath.PI / 2.0, FastMath.asin(1.0), EPSILON);
        assertEquals(-FastMath.PI / 2.0, FastMath.asin(-1.0), EPSILON);
        assertEquals(0.0, FastMath.acos(1.0), EPSILON);
        assertEquals(FastMath.PI, FastMath.acos(-1.0), EPSILON);
        assertEquals(FastMath.PI / 4.0, FastMath.atan(1.0), EPSILON);
        assertEquals(FastMath.PI / 4.0, FastMath.atan2(1.0, 1.0), EPSILON);
        assertEquals(3.0 * FastMath.PI / 4.0, FastMath.atan2(1.0, -1.0), EPSILON);
        assertTrue(Double.isNaN(FastMath.asin(2.0)));
        assertTrue(Double.isNaN(FastMath.acos(2.0)));
    }

    // Tests hyperbolic functions sinh, cosh, tanh, asinh, acosh, atanh
    @Test
    public void testHyperbolicFunctions_standardValues_returnsExpectedResults() {
        assertEquals(0.0, FastMath.sinh(0.0), EPSILON);
        assertEquals(1.0, FastMath.cosh(0.0), EPSILON);
        assertEquals(0.0, FastMath.tanh(0.0), EPSILON);
        assertEquals(0.0, FastMath.asinh(0.0), EPSILON);
        assertEquals(0.0, FastMath.acosh(1.0), EPSILON);
        assertEquals(0.0, FastMath.atanh(0.0), EPSILON);
        assertEquals(Math.sinh(1.5), FastMath.sinh(1.5), EPSILON);
        assertEquals(Math.cosh(1.5), FastMath.cosh(1.5), EPSILON);
        assertEquals(Math.tanh(1.5), FastMath.tanh(1.5), EPSILON);
    }

    // Tests cbrt, toDegrees, toRadians, ulp, nextAfter
    @Test
    public void testMiscMathFunctions_variousInputs_returnsExpectedResults() {
        assertEquals(3.0, FastMath.cbrt(27.0), EPSILON);
        assertEquals(-3.0, FastMath.cbrt(-27.0), EPSILON);
        assertEquals(180.0, FastMath.toDegrees(FastMath.PI), EPSILON);
        assertEquals(FastMath.PI, FastMath.toRadians(180.0), EPSILON);
        assertTrue(FastMath.ulp(1.0) > 0.0);
        assertEquals(Double.MIN_VALUE, FastMath.nextAfter(0.0, 1.0), 0.0);
        assertEquals(-Double.MIN_VALUE, FastMath.nextAfter(0.0, -1.0), 0.0);
    }

    @Test
    public void testHypot_variousInputs_returnsExpectedResults() {
        assertEquals(5.0, FastMath.hypot(3.0, 4.0), EPSILON);
        assertEquals(5.0, FastMath.hypot(-3.0, -4.0), EPSILON);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.hypot(Double.POSITIVE_INFINITY, 1.0), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.hypot(1.0, Double.NEGATIVE_INFINITY), 0.0);
        assertTrue(Double.isNaN(FastMath.hypot(Double.NaN, 1.0)));
        assertEquals(0.0, FastMath.hypot(0.0, 0.0), 0.0);
    }

    @Test
    public void testCopySign_doubleAndFloat_returnsExpectedResults() {
        assertEquals(2.0, FastMath.copySign(2.0, 1.0), 0.0);
        assertEquals(-2.0, FastMath.copySign(2.0, -1.0), 0.0);
        assertEquals(2.0, FastMath.copySign(-2.0, 1.0), 0.0);
        assertEquals(-2.0, FastMath.copySign(-2.0, -1.0), 0.0);
        assertEquals(2.0f, FastMath.copySign(2.0f, 1.0f), 0.0f);
        assertEquals(-2.0f, FastMath.copySign(2.0f, -1.0f), 0.0f);
        assertEquals(2.0f, FastMath.copySign(-2.0f, 1.0f), 0.0f);
        assertEquals(-2.0f, FastMath.copySign(-2.0f, -1.0f), 0.0f);
    }

    @Test
    public void testGetExponent_doubleAndFloat_returnsExpectedResults() {
        assertEquals(3, FastMath.getExponent(8.0));
        assertEquals(0, FastMath.getExponent(1.0));
        assertEquals(3, FastMath.getExponent(8.0f));
        assertEquals(0, FastMath.getExponent(1.0f));
    }

    @Test
    public void testScalb_doubleAndFloat_returnsExpectedResults() {
        assertEquals(8.0, FastMath.scalb(1.0, 3), EPSILON);
        assertEquals(0.125, FastMath.scalb(1.0, -3), EPSILON);
        assertEquals(8.0f, FastMath.scalb(1.0f, 3), 0.0f);
        assertEquals(0.125f, FastMath.scalb(1.0f, -3), 0.0f);
    }

    @Test
    public void testNextAfter_floatAndEdgeCases_returnsExpectedResults() {
        assertEquals(Float.MIN_VALUE, FastMath.nextAfter(0.0f, 1.0), 0.0f);
        assertEquals(-Float.MIN_VALUE, FastMath.nextAfter(0.0f, -1.0), 0.0f);
        assertEquals(1.0, FastMath.nextAfter(1.0, 1.0), 0.0);
        assertEquals(1.0f, FastMath.nextAfter(1.0f, 1.0), 0.0f);
    }

    @Test
    public void testNextDown_doubleAndFloat_returnsExpectedResults() {
        assertEquals(FastMath.nextAfter(1.0, Double.NEGATIVE_INFINITY), FastMath.nextDown(1.0), 0.0);
        assertEquals(FastMath.nextAfter(1.0f, Double.NEGATIVE_INFINITY), FastMath.nextDown(1.0f), 0.0f);
    }

    @Test
    public void testNextUp_float_returnsExpectedResults() {
        assertEquals(FastMath.nextAfter(1.0f, Double.POSITIVE_INFINITY), FastMath.nextUp(1.0f), 0.0f);
    }

    @Test
    public void testSignum_float_returnsExpectedResults() {
        assertEquals(1.0f, FastMath.signum(10.5f), 0.0f);
        assertEquals(-1.0f, FastMath.signum(-10.5f), 0.0f);
        assertEquals(0.0f, FastMath.signum(0.0f), 0.0f);
        assertTrue(Float.isNaN(FastMath.signum(Float.NaN)));
    }

    @Test
    public void testUlp_float_returnsExpectedResults() {
        assertTrue(FastMath.ulp(1.0f) > 0.0f);
        assertEquals(Float.MIN_VALUE, FastMath.ulp(0.0f), 0.0f);
    }

    @Test
    public void testIEEEremainder_variousInputs_returnsExpectedResults() {
        assertEquals(0.0, FastMath.IEEEremainder(4.0, 2.0), EPSILON);
        assertEquals(1.0, FastMath.IEEEremainder(5.0, 2.0), EPSILON);
        assertEquals(-1.0, FastMath.IEEEremainder(7.0, 4.0), EPSILON);
    }

    @Test
    public void testSpecialFloatingPointSigns_zeroComparisons() {
        assertEquals(0.0, FastMath.max(0.0, -0.0), 0.0);
        assertEquals(-0.0, FastMath.min(0.0, -0.0), 0.0);
        assertEquals(0.0f, FastMath.max(0.0f, -0.0f), 0.0f);
        assertEquals(-0.0f, FastMath.min(0.0f, -0.0f), 0.0f);
    }
}