package org.apache.commons.math.util;

import java.math.BigDecimal;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link MathUtils}.
 */
public class MathUtilsTest {

    // Tests gcd defect when product of non-zero integers overflows to zero (Math-94 bug)
    @Test
    public void testGcd_overflowProductNonZero_returnsCorrectGcd() {
        assertEquals(4, MathUtils.gcd(1073741824, 4));
        assertEquals(4, MathUtils.gcd(4, 1073741824));
    }

    // Tests gcd with zero and normal inputs
    @Test
    public void testGcd_normalAndZeroInputs_returnsCorrectGcd() {
        assertEquals(15, MathUtils.gcd(0, 15));
        assertEquals(15, MathUtils.gcd(15, 0));
        assertEquals(0, MathUtils.gcd(0, 0));
        assertEquals(6, MathUtils.gcd(18, 24));
        assertEquals(6, MathUtils.gcd(-18, 24));
        assertEquals(6, MathUtils.gcd(18, -24));
        assertEquals(6, MathUtils.gcd(-18, -24));
    }

    // Tests lcm normal and overflow cases
    @Test
    public void testLcm_validInputs_returnsCorrectLcm() {
        assertEquals(72, MathUtils.lcm(18, 24));
        assertEquals(0, MathUtils.lcm(0, 24));
    }

    // Tests addAndCheck for int overflow
    @Test(expected = ArithmeticException.class)
    public void testAddAndCheckInt_positiveOverflow_throwsException() {
        MathUtils.addAndCheck(Integer.MAX_VALUE, 1);
    }

    // Tests addAndCheck for int normal and negative overflow
    @Test
    public void testAddAndCheckInt_validAndNegativeOverflow() {
        assertEquals(5, MathUtils.addAndCheck(2, 3));
        try {
            MathUtils.addAndCheck(Integer.MIN_VALUE, -1);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException expected) {
            // expected
        }
    }

    // Tests addAndCheck for long valid, positive and negative overflow
    @Test
    public void testAddAndCheckLong_validAndOverflowCases() {
        assertEquals(5L, MathUtils.addAndCheck(2L, 3L));
        assertEquals(-5L, MathUtils.addAndCheck(-2L, -3L));
        assertEquals(1L, MathUtils.addAndCheck(-2L, 3L));
        assertEquals(1L, MathUtils.addAndCheck(3L, -2L));

        try {
            MathUtils.addAndCheck(Long.MAX_VALUE, 1L);
            fail("Expected ArithmeticException on positive overflow");
        } catch (ArithmeticException expected) {
            // expected
        }

        try {
            MathUtils.addAndCheck(Long.MIN_VALUE, -1L);
            fail("Expected ArithmeticException on negative overflow");
        } catch (ArithmeticException expected) {
            // expected
        }
    }

    // Tests subAndCheck for int and long overflow
    @Test
    public void testSubAndCheck_intAndLongCases() {
        assertEquals(1, MathUtils.subAndCheck(3, 2));
        assertEquals(1L, MathUtils.subAndCheck(3L, 2L));

        try {
            MathUtils.subAndCheck(Integer.MIN_VALUE, 1);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException expected) {
            // expected
        }

        try {
            MathUtils.subAndCheck(0L, Long.MIN_VALUE);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException expected) {
            // expected
        }

        assertEquals(-1L, MathUtils.subAndCheck(Long.MIN_VALUE + 1L, Long.MIN_VALUE));
    }

    // Tests mulAndCheck for int and long
    @Test
    public void testMulAndCheck_intAndLongCases() {
        assertEquals(6, MathUtils.mulAndCheck(2, 3));
        assertEquals(0L, MathUtils.mulAndCheck(0L, 5L));
        assertEquals(0L, MathUtils.mulAndCheck(5L, 0L));
        assertEquals(6L, MathUtils.mulAndCheck(2L, 3L));
        assertEquals(-6L, MathUtils.mulAndCheck(-2L, 3L));
        assertEquals(6L, MathUtils.mulAndCheck(-2L, -3L));

        try {
            MathUtils.mulAndCheck(Integer.MAX_VALUE, 2);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException expected) {
            // expected
        }

        try {
            MathUtils.mulAndCheck(Long.MAX_VALUE, 2L);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException expected) {
            // expected
        }

        try {
            MathUtils.mulAndCheck(Long.MIN_VALUE, 2L);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException expected) {
            // expected
        }

        try {
            MathUtils.mulAndCheck(Long.MIN_VALUE, -1L);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException expected) {
            // expected
        }
    }

    // Tests binomialCoefficient and exceptions
    @Test
    public void testBinomialCoefficient_validAndExceptionCases() {
        assertEquals(1L, MathUtils.binomialCoefficient(5, 0));
        assertEquals(1L, MathUtils.binomialCoefficient(5, 5));
        assertEquals(5L, MathUtils.binomialCoefficient(5, 1));
        assertEquals(5L, MathUtils.binomialCoefficient(5, 4));
        assertEquals(10L, MathUtils.binomialCoefficient(5, 2));

        assertTrue(MathUtils.binomialCoefficientDouble(5, 2) > 0);
        assertTrue(MathUtils.binomialCoefficientLog(5, 2) > 0);

        try {
            MathUtils.binomialCoefficient(4, 5);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // expected
        }

        try {
            MathUtils.binomialCoefficient(-1, 0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // expected
        }

        try {
            MathUtils.binomialCoefficient(67, 33);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException expected) {
            // expected
        }
    }

    // Tests factorial and exceptions
    @Test
    public void testFactorial_validAndExceptionCases() {
        assertEquals(1L, MathUtils.factorial(0));
        assertEquals(1L, MathUtils.factorial(1));
        assertEquals(120L, MathUtils.factorial(5));
        assertEquals(120.0, MathUtils.factorialDouble(5), 1e-10);
        assertEquals(Math.log(120.0), MathUtils.factorialLog(5), 1e-10);

        try {
            MathUtils.factorial(-1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // expected
        }

        try {
            MathUtils.factorial(21);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException expected) {
            // expected
        }
    }

    // Tests equals methods for double and double arrays
    @Test
    public void testEquals_doubleAndArrayCases() {
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

    // Tests hash codes
    @Test
    public void testHash_doubleAndArrayCases() {
        assertEquals(Double.valueOf(1.0).hashCode(), MathUtils.hash(1.0));
        double[] array = new double[]{1.0, 2.0};
        assertEquals(java.util.Arrays.hashCode(array), MathUtils.hash(array));
    }

    // Tests indicator for all primitive types
    @Test
    public void testIndicator_allTypes() {
        assertEquals((byte) 1, MathUtils.indicator((byte) 5));
        assertEquals((byte) 1, MathUtils.indicator((byte) 0));
        assertEquals((byte) -1, MathUtils.indicator((byte) -5));

        assertEquals(1.0, MathUtils.indicator(5.0), 0.0);
        assertEquals(1.0, MathUtils.indicator(0.0), 0.0);
        assertEquals(-1.0, MathUtils.indicator(-5.0), 0.0);
        assertTrue(Double.isNaN(MathUtils.indicator(Double.NaN)));

        assertEquals(1.0f, MathUtils.indicator(5.0f), 0.0f);
        assertEquals(1.0f, MathUtils.indicator(0.0f), 0.0f);
        assertEquals(-1.0f, MathUtils.indicator(-5.0f), 0.0f);
        assertTrue(Float.isNaN(MathUtils.indicator(Float.NaN)));

        assertEquals(1, MathUtils.indicator(5));
        assertEquals(1, MathUtils.indicator(0));
        assertEquals(-1, MathUtils.indicator(-5));

        assertEquals(1L, MathUtils.indicator(5L));
        assertEquals(1L, MathUtils.indicator(0L));
        assertEquals(-1L, MathUtils.indicator(-5L));

        assertEquals((short) 1, MathUtils.indicator((short) 5));
        assertEquals((short) 1, MathUtils.indicator((short) 0));
        assertEquals((short) -1, MathUtils.indicator((short) -5));
    }

    // Tests sign for all primitive types
    @Test
    public void testSign_allTypes() {
        assertEquals((byte) 1, MathUtils.sign((byte) 5));
        assertEquals((byte) 0, MathUtils.sign((byte) 0));
        assertEquals((byte) -1, MathUtils.sign((byte) -5));

        assertEquals(1.0, MathUtils.sign(5.0), 0.0);
        assertEquals(0.0, MathUtils.sign(0.0), 0.0);
        assertEquals(-1.0, MathUtils.sign(-5.0), 0.0);
        assertTrue(Double.isNaN(MathUtils.sign(Double.NaN)));

        assertEquals(1.0f, MathUtils.sign(5.0f), 0.0f);
        assertEquals(0.0f, MathUtils.sign(0.0f), 0.0f);
        assertEquals(-1.0f, MathUtils.sign(-5.0f), 0.0f);
        assertTrue(Float.isNaN(MathUtils.sign(Float.NaN)));

        assertEquals(1, MathUtils.sign(5));
        assertEquals(0, MathUtils.sign(0));
        assertEquals(-1, MathUtils.sign(-5));

        assertEquals(1L, MathUtils.sign(5L));
        assertEquals(0L, MathUtils.sign(0L));
        assertEquals(-1L, MathUtils.sign(-5L));

        assertEquals((short) 1, MathUtils.sign((short) 5));
        assertEquals((short) 0, MathUtils.sign((short) 0));
        assertEquals((short) -1, MathUtils.sign((short) -5));
    }

    // Tests hyperbolic functions cosh and sinh
    @Test
    public void testHyperbolic_coshAndSinh() {
        assertEquals(1.0, MathUtils.cosh(0.0), 1e-10);
        assertEquals(0.0, MathUtils.sinh(0.0), 1e-10);
        assertEquals((Math.exp(1.0) + Math.exp(-1.0)) / 2.0, MathUtils.cosh(1.0), 1e-10);
        assertEquals((Math.exp(1.0) - Math.exp(-1.0)) / 2.0, MathUtils.sinh(1.0), 1e-10);
    }

    // Tests log, scalb, nextAfter, and normalizeAngle
    @Test
    public void testMathFunctions_logScalbNextAfterNormalizeAngle() {
        assertEquals(2.0, MathUtils.log(2.0, 4.0), 1e-10);

        assertEquals(0.0, MathUtils.scalb(0.0, 2), 0.0);
        assertTrue(Double.isNaN(MathUtils.scalb(Double.NaN, 2)));
        assertTrue(Double.isInfinite(MathUtils.scalb(Double.POSITIVE_INFINITY, 2)));
        assertEquals(8.0, MathUtils.scalb(2.0, 2), 1e-10);

        assertTrue(Double.isNaN(MathUtils.nextAfter(Double.NaN, 1.0)));
        assertEquals(Double.MIN_VALUE, MathUtils.nextAfter(0.0, 1.0), 0.0);
        assertEquals(-Double.MIN_VALUE, MathUtils.nextAfter(0.0, -1.0), 0.0);
        assertTrue(MathUtils.nextAfter(1.0, 2.0) > 1.0);
        assertTrue(MathUtils.nextAfter(1.0, 0.0) < 1.0);

        assertEquals(0.0, MathUtils.normalizeAngle(2 * Math.PI, 0.0), 1e-10);
        assertEquals(Math.PI, MathUtils.normalizeAngle(Math.PI, Math.PI), 1e-10);
    }

    // Tests round methods for double and float with different modes
    @Test
    public void testRound_doubleAndFloat() {
        assertEquals(1.23, MathUtils.round(1.2345, 2), 1e-10);
        assertEquals(1.24, MathUtils.round(1.2355, 2), 1e-10);
        assertTrue(Double.isInfinite(MathUtils.round(Double.POSITIVE_INFINITY, 2, BigDecimal.ROUND_HALF_UP)));
        assertTrue(Double.isNaN(MathUtils.round(Double.NaN, 2, BigDecimal.ROUND_HALF_UP)));

        assertEquals(1.23f, MathUtils.round(1.2345f, 2), 1e-5f);
        assertEquals(1.24f, MathUtils.round(1.2355f, 2), 1e-5f);

        assertEquals(2.0f, MathUtils.round(1.2f, 0, BigDecimal.ROUND_UP), 1e-5f);
        assertEquals(1.0f, MathUtils.round(1.8f, 0, BigDecimal.ROUND_DOWN), 1e-5f);
        assertEquals(2.0f, MathUtils.round(1.2f, 0, BigDecimal.ROUND_CEILING), 1e-5f);
        assertEquals(-2.0f, MathUtils.round(-1.2f, 0, BigDecimal.ROUND_CEILING), 1e-5f);
        assertEquals(1.0f, MathUtils.round(1.8f, 0, BigDecimal.ROUND_FLOOR), 1e-5f);
        assertEquals(-2.0f, MathUtils.round(-1.8f, 0, BigDecimal.ROUND_FLOOR), 1e-5f);
        assertEquals(1.0f, MathUtils.round(1.5f, 0, BigDecimal.ROUND_HALF_DOWN), 1e-5f);
        assertEquals(2.0f, MathUtils.round(1.51f, 0, BigDecimal.ROUND_HALF_DOWN), 1e-5f);
        assertEquals(2.0f, MathUtils.round(2.5f, 0, BigDecimal.ROUND_HALF_EVEN), 1e-5f);
        assertEquals(4.0f, MathUtils.round(3.5f, 0, BigDecimal.ROUND_HALF_EVEN), 1e-5f);

        try {
            MathUtils.round(1.5f, 0, BigDecimal.ROUND_UNNECESSARY);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException expected) {
            // expected
        }

        try {
            MathUtils.round(1.5f, 0, 9999);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // expected
        }
    }
}