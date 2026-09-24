package org.apache.commons.math.util;

import java.math.BigDecimal;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for MathUtils.
 */
public class MathUtilsTest {

    // Tests exact binomial coefficients and symmetry
    @Test
    public void testBinomialCoefficient_largeValues_returnsExactResult() {
        assertEquals(1L, MathUtils.binomialCoefficient(0, 0));
        assertEquals(1L, MathUtils.binomialCoefficient(5, 0));
        assertEquals(1L, MathUtils.binomialCoefficient(5, 5));
        assertEquals(5L, MathUtils.binomialCoefficient(5, 1));
        assertEquals(5L, MathUtils.binomialCoefficient(5, 4));
        assertEquals(10L, MathUtils.binomialCoefficient(5, 2));
        assertEquals(30145280624896L, MathUtils.binomialCoefficient(48, 22));
        assertEquals(7219428434016265740L, MathUtils.binomialCoefficient(66, 33));
    }

    // Tests invalid arguments for binomial coefficient
    @Test(expected = IllegalArgumentException.class)
    public void testBinomialCoefficient_kGreaterThanN_throwsException() {
        MathUtils.binomialCoefficient(4, 5);
    }

    // Tests negative arguments for binomial coefficient
    @Test(expected = IllegalArgumentException.class)
    public void testBinomialCoefficient_negativeN_throwsException() {
        MathUtils.binomialCoefficient(-1, -1);
    }

    // Tests binomialCoefficientDouble and binomialCoefficientLog
    @Test
    public void testBinomialCoefficientDoubleAndLog_validInputs_returnsExpected() {
        assertEquals(10.0, MathUtils.binomialCoefficientDouble(5, 2), 1e-10);
        assertEquals(Math.log(10.0), MathUtils.binomialCoefficientLog(5, 2), 1e-10);
        assertEquals(0.0, MathUtils.binomialCoefficientLog(5, 0), 1e-10);
        assertEquals(Math.log(5.0), MathUtils.binomialCoefficientLog(5, 1), 1e-10);
    }

    // Tests addAndCheck with int values including overflow
    @Test
    public void testAddAndCheckInt_validAndOverflow_handlesCorrectly() {
        assertEquals(5, MathUtils.addAndCheck(2, 3));
        assertEquals(Integer.MAX_VALUE, MathUtils.addAndCheck(Integer.MAX_VALUE - 1, 1));
        try {
            MathUtils.addAndCheck(Integer.MAX_VALUE, 1);
            fail("Expected ArithmeticException on integer positive overflow");
        } catch (ArithmeticException expected) {
        }
        try {
            MathUtils.addAndCheck(Integer.MIN_VALUE, -1);
            fail("Expected ArithmeticException on integer negative overflow");
        } catch (ArithmeticException expected) {
        }
    }

    // Tests addAndCheck with long values including overflow
    @Test
    public void testAddAndCheckLong_validAndOverflow_handlesCorrectly() {
        assertEquals(5L, MathUtils.addAndCheck(2L, 3L));
        assertEquals(-1L, MathUtils.addAndCheck(Long.MAX_VALUE, Long.MIN_VALUE));
        try {
            MathUtils.addAndCheck(Long.MAX_VALUE, 1L);
            fail("Expected ArithmeticException on long positive overflow");
        } catch (ArithmeticException expected) {
        }
        try {
            MathUtils.addAndCheck(Long.MIN_VALUE, -1L);
            fail("Expected ArithmeticException on long negative overflow");
        } catch (ArithmeticException expected) {
        }
    }

    // Tests subAndCheck with int and long including boundary cases
    @Test
    public void testSubAndCheck_validAndOverflow_handlesCorrectly() {
        assertEquals(-1, MathUtils.subAndCheck(2, 3));
        try {
            MathUtils.subAndCheck(Integer.MIN_VALUE, 1);
            fail("Expected ArithmeticException on sub int overflow");
        } catch (ArithmeticException expected) {
        }

        assertEquals(-1L, MathUtils.subAndCheck(2L, 3L));
        assertEquals(0L, MathUtils.subAndCheck(Long.MIN_VALUE, Long.MIN_VALUE));
        try {
            MathUtils.subAndCheck(Long.MIN_VALUE, 1L);
            fail("Expected ArithmeticException on sub long overflow");
        } catch (ArithmeticException expected) {
        }
        try {
            MathUtils.subAndCheck(0L, Long.MIN_VALUE);
            fail("Expected ArithmeticException on sub long MIN_VALUE");
        } catch (ArithmeticException expected) {
        }
    }

    // Tests mulAndCheck with int and long values
    @Test
    public void testMulAndCheck_validAndOverflow_handlesCorrectly() {
        assertEquals(6, MathUtils.mulAndCheck(2, 3));
        assertEquals(0, MathUtils.mulAndCheck(0, Integer.MAX_VALUE));
        try {
            MathUtils.mulAndCheck(Integer.MAX_VALUE, 2);
            fail("Expected ArithmeticException on mul int overflow");
        } catch (ArithmeticException expected) {
        }

        assertEquals(6L, MathUtils.mulAndCheck(2L, 3L));
        assertEquals(0L, MathUtils.mulAndCheck(0L, Long.MAX_VALUE));
        assertEquals(0L, MathUtils.mulAndCheck(Long.MAX_VALUE, 0L));
        try {
            MathUtils.mulAndCheck(Long.MAX_VALUE, 2L);
            fail("Expected ArithmeticException on mul long overflow");
        } catch (ArithmeticException expected) {
        }
        try {
            MathUtils.mulAndCheck(Long.MIN_VALUE, -1L);
            fail("Expected ArithmeticException on mul long negative overflow");
        } catch (ArithmeticException expected) {
        }
    }

    // Tests gcd and lcm calculation
    @Test
    public void testGcdAndLcm_variousInputs_returnsCorrectResults() {
        assertEquals(6, MathUtils.gcd(12, 18));
        assertEquals(6, MathUtils.gcd(-12, 18));
        assertEquals(12, MathUtils.gcd(12, 0));
        assertEquals(18, MathUtils.gcd(0, 18));
        assertEquals(36, MathUtils.lcm(12, 18));
        assertEquals(0, MathUtils.lcm(0, 18));
    }

    // Tests factorial functions and boundary checks
    @Test
    public void testFactorial_validAndBoundary_returnsCorrectResults() {
        assertEquals(1L, MathUtils.factorial(0));
        assertEquals(1L, MathUtils.factorial(1));
        assertEquals(120L, MathUtils.factorial(5));
        assertEquals(2432902008176640000L, MathUtils.factorial(20));

        assertEquals(120.0, MathUtils.factorialDouble(5), 1e-10);
        assertEquals(Math.log(120.0), MathUtils.factorialLog(5), 1e-10);
        assertEquals(0.0, MathUtils.factorialLog(0), 1e-10);

        try {
            MathUtils.factorial(-1);
            fail("Expected IllegalArgumentException on negative input");
        } catch (IllegalArgumentException expected) {
        }
        try {
            MathUtils.factorial(21);
            fail("Expected ArithmeticException on factorial > 20");
        } catch (ArithmeticException expected) {
        }
    }

    // Tests equals method for double and double arrays
    @Test
    public void testEquals_doubleAndArrays_handlesSpecialValues() {
        assertTrue(MathUtils.equals(Double.NaN, Double.NaN));
        assertFalse(MathUtils.equals(Double.NaN, 1.0));
        assertTrue(MathUtils.equals(1.0, 1.0));
        assertFalse(MathUtils.equals(1.0, 2.0));

        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
        assertFalse(MathUtils.equals(new double[]{1.0}, new double[]{1.0, 2.0}));
        assertTrue(MathUtils.equals(new double[]{1.0, Double.NaN}, new double[]{1.0, Double.NaN}));
        assertFalse(MathUtils.equals(new double[]{1.0, 2.0}, new double[]{1.0, 3.0}));
    }

    // Tests hash code methods for double and double array
    @Test
    public void testHash_doubleAndArray_returnsConsistentHashCode() {
        assertEquals(Double.valueOf(1.5).hashCode(), MathUtils.hash(1.5));
        double[] array = new double[]{1.0, 2.0, 3.0};
        assertEquals(java.util.Arrays.hashCode(array), MathUtils.hash(array));
    }

    // Tests sign and indicator functions for all primitive types
    @Test
    public void testSignAndIndicator_allTypes_returnsExpectedResults() {
        assertEquals((byte) 1, MathUtils.sign((byte) 5));
        assertEquals((byte) 0, MathUtils.sign((byte) 0));
        assertEquals((byte) -1, MathUtils.sign((byte) -5));
        assertEquals((short) 1, MathUtils.sign((short) 5));
        assertEquals((short) 0, MathUtils.sign((short) 0));
        assertEquals((short) -1, MathUtils.sign((short) -5));
        assertEquals(1, MathUtils.sign(5));
        assertEquals(0, MathUtils.sign(0));
        assertEquals(-1, MathUtils.sign(-5));
        assertEquals(1L, MathUtils.sign(5L));
        assertEquals(0L, MathUtils.sign(0L));
        assertEquals(-1L, MathUtils.sign(-5L));
        assertEquals(1.0, MathUtils.sign(5.0), 0.0);
        assertEquals(0.0, MathUtils.sign(0.0), 0.0);
        assertEquals(-1.0, MathUtils.sign(-5.0), 0.0);
        assertTrue(Double.isNaN(MathUtils.sign(Double.NaN)));
        assertEquals(1.0f, MathUtils.sign(5.0f), 0.0f);
        assertEquals(0.0f, MathUtils.sign(0.0f), 0.0f);
        assertEquals(-1.0f, MathUtils.sign(-5.0f), 0.0f);
        assertTrue(Float.isNaN(MathUtils.sign(Float.NaN)));

        assertEquals((byte) 1, MathUtils.indicator((byte) 0));
        assertEquals((byte) -1, MathUtils.indicator((byte) -5));
        assertEquals((short) 1, MathUtils.indicator((short) 0));
        assertEquals((short) -1, MathUtils.indicator((short) -5));
        assertEquals(1, MathUtils.indicator(0));
        assertEquals(-1, MathUtils.indicator(-5));
        assertEquals(1L, MathUtils.indicator(0L));
        assertEquals(-1L, MathUtils.indicator(-5L));
        assertEquals(1.0, MathUtils.indicator(0.0), 0.0);
        assertEquals(-1.0, MathUtils.indicator(-5.0), 0.0);
        assertTrue(Double.isNaN(MathUtils.indicator(Double.NaN)));
        assertEquals(1.0f, MathUtils.indicator(0.0f), 0.0f);
        assertEquals(-1.0f, MathUtils.indicator(-5.0f), 0.0f);
        assertTrue(Float.isNaN(MathUtils.indicator(Float.NaN)));
    }

    // Tests hyperbolic sine and cosine functions
    @Test
    public void testHyperbolicFunctions_variousInputs_returnsExpectedResults() {
        assertEquals(1.0, MathUtils.cosh(0.0), 1e-10);
        assertEquals(0.0, MathUtils.sinh(0.0), 1e-10);
        assertEquals(MathUtils.cosh(2.0), MathUtils.cosh(-2.0), 1e-10);
        assertEquals(MathUtils.sinh(2.0), -MathUtils.sinh(-2.0), 1e-10);
    }

    // Tests normalizeAngle
    @Test
    public void testNormalizeAngle_variousAngles_normalizesCorrectly() {
        assertEquals(0.0, MathUtils.normalizeAngle(0.0, 0.0), 1e-10);
        assertEquals(Math.PI, MathUtils.normalizeAngle(3 * Math.PI, Math.PI), 1e-10);
        assertEquals(-Math.PI / 2, MathUtils.normalizeAngle(3 * Math.PI / 2, 0.0), 1e-10);
    }

    // Tests round with double and float using various rounding modes
    @Test
    public void testRound_doubleAndFloat_roundsCorrectly() {
        assertEquals(1.23, MathUtils.round(1.234, 2), 1e-10);
        assertEquals(1.24, MathUtils.round(1.235, 2), 1e-10);
        assertEquals(1.23, MathUtils.round(1.235, 2, BigDecimal.ROUND_DOWN), 1e-10);
        assertEquals(1.24, MathUtils.round(1.231, 2, BigDecimal.ROUND_UP), 1e-10);
        assertEquals(1.24, MathUtils.round(1.231, 2, BigDecimal.ROUND_CEILING), 1e-10);
        assertEquals(1.23, MathUtils.round(1.239, 2, BigDecimal.ROUND_FLOOR), 1e-10);
        assertEquals(1.24, MathUtils.round(1.235, 2, BigDecimal.ROUND_HALF_UP), 1e-10);
        assertEquals(1.23, MathUtils.round(1.235, 2, BigDecimal.ROUND_HALF_DOWN), 1e-10);
        assertEquals(1.24, MathUtils.round(1.235, 2, BigDecimal.ROUND_HALF_EVEN), 1e-10);
        assertEquals(1.24, MathUtils.round(1.245, 2, BigDecimal.ROUND_HALF_EVEN), 1e-10);

        assertTrue(Double.isNaN(MathUtils.round(Double.NaN, 2)));
        assertEquals(Double.POSITIVE_INFINITY, MathUtils.round(Double.POSITIVE_INFINITY, 2), 0.0);

        assertEquals(1.23f, MathUtils.round(1.234f, 2), 1e-5f);
        assertEquals(1.24f, MathUtils.round(1.235f, 2), 1e-5f);
        assertEquals(1.23f, MathUtils.round(1.235f, 2, BigDecimal.ROUND_DOWN), 1e-5f);
        assertEquals(1.24f, MathUtils.round(1.231f, 2, BigDecimal.ROUND_UP), 1e-5f);
    }

    // Tests nextAfter, scalb, and log functions
    @Test
    public void testNextAfterScalbAndLog_variousInputs_returnsExpectedResults() {
        assertEquals(Double.MIN_VALUE, MathUtils.nextAfter(0.0, 1.0), 0.0);
        assertEquals(-Double.MIN_VALUE, MathUtils.nextAfter(0.0, -1.0), 0.0);
        assertTrue(Double.isNaN(MathUtils.nextAfter(Double.NaN, 1.0)));
        assertEquals(Double.POSITIVE_INFINITY, MathUtils.nextAfter(Double.POSITIVE_INFINITY, 1.0), 0.0);
        assertTrue(MathUtils.nextAfter(1.0, 2.0) > 1.0);
        assertTrue(MathUtils.nextAfter(1.0, 0.0) < 1.0);

        assertEquals(8.0, MathUtils.scalb(2.0, 2), 1e-10);
        assertEquals(0.0, MathUtils.scalb(0.0, 5), 0.0);
        assertTrue(Double.isNaN(MathUtils.scalb(Double.NaN, 5)));

        assertEquals(2.0, MathUtils.log(10.0, 100.0), 1e-10);
        assertEquals(3.0, MathUtils.log(2.0, 8.0), 1e-10);
    }
}