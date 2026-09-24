package org.apache.commons.math.util;

import java.math.BigDecimal;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test class for MathUtils.
 */
public class MathUtilsTest {

    // Tests integer addAndCheck normal and overflow conditions
    @Test
    public void testAddAndCheckInt_validAndOverflow() {
        assertEquals(5, MathUtils.addAndCheck(2, 3));
        assertEquals(Integer.MAX_VALUE, MathUtils.addAndCheck(Integer.MAX_VALUE - 1, 1));
        assertEquals(Integer.MIN_VALUE, MathUtils.addAndCheck(Integer.MIN_VALUE + 1, -1));
    }

    // Tests integer addAndCheck positive overflow exception
    @Test(expected = ArithmeticException.class)
    public void testAddAndCheckInt_positiveOverflow_throwsException() {
        MathUtils.addAndCheck(Integer.MAX_VALUE, 1);
    }

    // Tests integer addAndCheck negative overflow exception
    @Test(expected = ArithmeticException.class)
    public void testAddAndCheckInt_negativeOverflow_throwsException() {
        MathUtils.addAndCheck(Integer.MIN_VALUE, -1);
    }

    // Tests long addAndCheck and subAndCheck with boundary conditions and overflow
    @Test
    public void testAddAndSubCheckLong_validAndOverflow() {
        assertEquals(7L, MathUtils.addAndCheck(3L, 4L));
        assertEquals(-7L, MathUtils.subAndCheck(-3L, 4L));
        assertEquals(0L, MathUtils.addAndCheck(-5L, 5L));
        assertEquals(Long.MAX_VALUE, MathUtils.subAndCheck(Long.MAX_VALUE - 1L, -1L));
        assertEquals(Long.MIN_VALUE + 1L, MathUtils.subAndCheck(Long.MIN_VALUE, -1L));
    }

    // Tests long addAndCheck overflow exception
    @Test(expected = ArithmeticException.class)
    public void testAddAndCheckLong_overflow_throwsException() {
        MathUtils.addAndCheck(Long.MAX_VALUE, 1L);
    }

    // Tests long subAndCheck overflow with Long.MIN_VALUE
    @Test(expected = ArithmeticException.class)
    public void testSubAndCheckLong_overflowWithMin_throwsException() {
        MathUtils.subAndCheck(0L, Long.MIN_VALUE);
    }

    // Tests integer and long mulAndCheck with valid values and overflow
    @Test
    public void testMulAndCheck_intAndLong() {
        assertEquals(6, MathUtils.mulAndCheck(2, 3));
        assertEquals(0, MathUtils.mulAndCheck(0, 5));
        assertEquals(-6, MathUtils.mulAndCheck(-2, 3));
        assertEquals(6L, MathUtils.mulAndCheck(2L, 3L));
        assertEquals(0L, MathUtils.mulAndCheck(0L, -5L));
    }

    // Tests int mulAndCheck overflow exception
    @Test(expected = ArithmeticException.class)
    public void testMulAndCheckInt_overflow_throwsException() {
        MathUtils.mulAndCheck(Integer.MAX_VALUE, 2);
    }

    // Tests long mulAndCheck overflow exception
    @Test(expected = ArithmeticException.class)
    public void testMulAndCheckLong_overflow_throwsException() {
        MathUtils.mulAndCheck(Long.MAX_VALUE, 2L);
    }

    // Tests gcd and lcm methods including edge cases
    @Test
    public void testGcdAndLcm_validValues() {
        assertEquals(6, MathUtils.gcd(12, 18));
        assertEquals(6, MathUtils.gcd(-12, 18));
        assertEquals(5, MathUtils.gcd(0, 5));
        assertEquals(5, MathUtils.gcd(5, 0));
        assertEquals(36, MathUtils.lcm(12, 18));
    }

    // Tests factorial and factorialDouble correctness and boundary limits
    @Test
    public void testFactorial_validAndBoundaries() {
        assertEquals(1L, MathUtils.factorial(0));
        assertEquals(1L, MathUtils.factorial(1));
        assertEquals(120L, MathUtils.factorial(5));
        assertEquals(2432902008176640000L, MathUtils.factorial(20));

        assertEquals(1.0, MathUtils.factorialDouble(0), 1e-5);
        assertEquals(120.0, MathUtils.factorialDouble(5), 1e-5);
        assertEquals(0.0, MathUtils.factorialLog(0), 1e-5);
        assertEquals(Math.log(120.0), MathUtils.factorialLog(5), 1e-5);
    }

    // Tests factorial negative input exception
    @Test(expected = IllegalArgumentException.class)
    public void testFactorial_negativeArgument_throwsException() {
        MathUtils.factorial(-1);
    }

    // Tests factorial overflow for n > 20
    @Test(expected = ArithmeticException.class)
    public void testFactorial_overflow_throwsException() {
        MathUtils.factorial(21);
    }

    // Tests binomialCoefficient for valid values and edge cases
    @Test
    public void testBinomialCoefficient_validValues() {
        assertEquals(1L, MathUtils.binomialCoefficient(5, 0));
        assertEquals(1L, MathUtils.binomialCoefficient(5, 5));
        assertEquals(5L, MathUtils.binomialCoefficient(5, 1));
        assertEquals(5L, MathUtils.binomialCoefficient(5, 4));
        assertEquals(10L, MathUtils.binomialCoefficient(5, 2));
        assertEquals(10.0, MathUtils.binomialCoefficientDouble(5, 2), 1e-5);
        assertEquals(Math.log(10.0), MathUtils.binomialCoefficientLog(5, 2), 1e-5);
    }

    // Tests binomialCoefficient invalid inputs exception
    @Test(expected = IllegalArgumentException.class)
    public void testBinomialCoefficient_invalidArguments_throwsException() {
        MathUtils.binomialCoefficient(2, 5);
    }

    // Tests equals for double and double arrays
    @Test
    public void testEquals_doublesAndArrays() {
        assertTrue(MathUtils.equals(Double.NaN, Double.NaN));
        assertFalse(MathUtils.equals(1.0, Double.NaN));
        assertTrue(MathUtils.equals(1.5, 1.5));
        assertFalse(MathUtils.equals(1.5, 2.5));

        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
        assertFalse(MathUtils.equals(new double[]{1.0}, new double[]{1.0, 2.0}));
        assertTrue(MathUtils.equals(new double[]{1.0, Double.NaN}, new double[]{1.0, Double.NaN}));
        assertFalse(MathUtils.equals(new double[]{1.0, 2.0}, new double[]{1.0, 3.0}));
    }

    // Tests indicator and sign functions across primitives
    @Test
    public void testIndicatorAndSign_primitives() {
        assertEquals(1, MathUtils.indicator(10));
        assertEquals(-1, MathUtils.indicator(-10));
        assertEquals(1, MathUtils.indicator(0));

        assertEquals(1.0, MathUtils.indicator(5.0), 1e-9);
        assertEquals(-1.0, MathUtils.indicator(-5.0), 1e-9);
        assertTrue(Double.isNaN(MathUtils.indicator(Double.NaN)));
        assertTrue(Float.isNaN(MathUtils.indicator(Float.NaN)));

        assertEquals(1, MathUtils.sign(10));
        assertEquals(0, MathUtils.sign(0));
        assertEquals(-1, MathUtils.sign(-10));

        assertEquals(1.0, MathUtils.sign(10.0), 1e-9);
        assertEquals(0.0, MathUtils.sign(0.0), 1e-9);
        assertEquals(-1.0, MathUtils.sign(-10.0), 1e-9);
        assertTrue(Double.isNaN(MathUtils.sign(Double.NaN)));
    }

    // Tests hyperbolic functions, log, and angle normalization
    @Test
    public void testHyperbolicAndMathFunctions() {
        assertEquals(1.0, MathUtils.cosh(0.0), 1e-9);
        assertEquals(0.0, MathUtils.sinh(0.0), 1e-9);
        assertEquals(2.0, MathUtils.log(10.0, 100.0), 1e-9);
        assertEquals(Math.PI, MathUtils.normalizeAngle(3 * Math.PI, Math.PI), 1e-9);
        assertEquals(0.0, MathUtils.normalizeAngle(2 * Math.PI, 0.0), 1e-9);
    }

    // Tests rounding methods for double and float
    @Test
    public void testRound_doubleAndFloat() {
        assertEquals(1.23, MathUtils.round(1.2345, 2), 1e-9);
        assertEquals(1.24, MathUtils.round(1.2355, 2), 1e-9);
        assertEquals(1.23f, MathUtils.round(1.2345f, 2), 1e-5f);
        assertEquals(1.23, MathUtils.round(1.2345, 2, BigDecimal.ROUND_DOWN), 1e-9);
        assertEquals(1.24, MathUtils.round(1.2345, 2, BigDecimal.ROUND_UP), 1e-9);
        assertTrue(Double.isNaN(MathUtils.round(Double.NaN, 2)));
        assertEquals(Double.POSITIVE_INFINITY, MathUtils.round(Double.POSITIVE_INFINITY, 2), 1e-9);
    }

    // Tests nextAfter, scalb, and hash methods
    @Test
    public void testNextAfterScalbAndHash() {
        assertTrue(MathUtils.nextAfter(1.0, 2.0) > 1.0);
        assertTrue(MathUtils.nextAfter(1.0, 0.0) < 1.0);
        assertEquals(Double.MIN_VALUE, MathUtils.nextAfter(0.0, 1.0), 0.0);
        assertEquals(-Double.MIN_VALUE, MathUtils.nextAfter(0.0, -1.0), 0.0);
        assertTrue(Double.isNaN(MathUtils.nextAfter(Double.NaN, 1.0)));

        assertEquals(8.0, MathUtils.scalb(2.0, 2), 1e-9);
        assertEquals(0.0, MathUtils.scalb(0.0, 5), 0.0);
        assertTrue(Double.isNaN(MathUtils.scalb(Double.NaN, 2)));

        assertEquals(new Double(1.5).hashCode(), MathUtils.hash(1.5));
        assertEquals(java.util.Arrays.hashCode(new double[]{1.0, 2.0}), MathUtils.hash(new double[]{1.0, 2.0}));
    }
}