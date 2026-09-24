package org.apache.commons.math.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import org.junit.Test;

public class MathUtilsTest {

    // Tests normal and edge cases for gcd
    @Test
    public void testGcd_normalAndZeroInputs_returnsCorrectGcd() {
        assertEquals(6, MathUtils.gcd(30, 24));
        assertEquals(6, MathUtils.gcd(-30, 24));
        assertEquals(6, MathUtils.gcd(30, -24));
        assertEquals(1, MathUtils.gcd(17, 13));
        assertEquals(15, MathUtils.gcd(0, 15));
        assertEquals(15, MathUtils.gcd(15, 0));
        assertEquals(0, MathUtils.gcd(0, 0));
    }

    // Tests gcd with Integer.MIN_VALUE and 0 throwing ArithmeticException
    @Test(expected = ArithmeticException.class)
    public void testGcd_minValueAndZero_throwsArithmeticException() {
        MathUtils.gcd(Integer.MIN_VALUE, 0);
    }

    // Tests gcd with 0 and Integer.MIN_VALUE throwing ArithmeticException
    @Test(expected = ArithmeticException.class)
    public void testGcd_zeroAndMinValue_throwsArithmeticException() {
        MathUtils.gcd(0, Integer.MIN_VALUE);
    }

    // Tests gcd with both Integer.MIN_VALUE throwing ArithmeticException
    @Test(expected = ArithmeticException.class)
    public void testGcd_bothMinValue_throwsArithmeticException() {
        MathUtils.gcd(Integer.MIN_VALUE, Integer.MIN_VALUE);
    }

    // Tests lcm with normal and zero values
    @Test
    public void testLcm_normalAndZeroInputs_returnsCorrectLcm() {
        assertEquals(0, MathUtils.lcm(0, 10));
        assertEquals(0, MathUtils.lcm(10, 0));
        assertEquals(12, MathUtils.lcm(4, 6));
        assertEquals(12, MathUtils.lcm(-4, 6));
        assertEquals(12, MathUtils.lcm(4, -6));
    }

    // Tests lcm with Integer.MIN_VALUE throwing ArithmeticException
    @Test(expected = ArithmeticException.class)
    public void testLcm_minValueOverflow_throwsArithmeticException() {
        MathUtils.lcm(Integer.MIN_VALUE, 1);
    }

    // Tests addAndCheck for int normal and overflow cases
    @Test
    public void testAddAndCheckInt_validAndOverflow() {
        assertEquals(10, MathUtils.addAndCheck(4, 6));
        assertEquals(-10, MathUtils.addAndCheck(-4, -6));
    }

    // Tests addAndCheck int positive overflow exception
    @Test(expected = ArithmeticException.class)
    public void testAddAndCheckInt_positiveOverflow_throwsException() {
        MathUtils.addAndCheck(Integer.MAX_VALUE, 1);
    }

    // Tests addAndCheck int negative overflow exception
    @Test(expected = ArithmeticException.class)
    public void testAddAndCheckInt_negativeOverflow_throwsException() {
        MathUtils.addAndCheck(Integer.MIN_VALUE, -1);
    }

    // Tests addAndCheck for long values and overflow
    @Test
    public void testAddAndCheckLong_validAndOverflow() {
        assertEquals(15L, MathUtils.addAndCheck(7L, 8L));
        assertEquals(-1L, MathUtils.addAndCheck(Long.MAX_VALUE, Long.MIN_VALUE));
    }

    // Tests addAndCheck long positive overflow exception
    @Test(expected = ArithmeticException.class)
    public void testAddAndCheckLong_positiveOverflow_throwsException() {
        MathUtils.addAndCheck(Long.MAX_VALUE, 1L);
    }

    // Tests subAndCheck for int and long
    @Test
    public void testSubAndCheck_validOperations() {
        assertEquals(5, MathUtils.subAndCheck(10, 5));
        assertEquals(5L, MathUtils.subAndCheck(10L, 5L));
        assertEquals(Long.MIN_VALUE, MathUtils.subAndCheck(Long.MIN_VALUE + 1L, 1L));
    }

    // Tests subAndCheck long overflow
    @Test(expected = ArithmeticException.class)
    public void testSubAndCheckLong_overflow_throwsException() {
        MathUtils.subAndCheck(Long.MIN_VALUE, 1L);
    }

    // Tests mulAndCheck for int and long
    @Test
    public void testMulAndCheck_validOperations() {
        assertEquals(42, MathUtils.mulAndCheck(6, 7));
        assertEquals(0, MathUtils.mulAndCheck(0, 100));
        assertEquals(42L, MathUtils.mulAndCheck(6L, 7L));
        assertEquals(0L, MathUtils.mulAndCheck(0L, Long.MAX_VALUE));
    }

    // Tests mulAndCheck long overflow
    @Test(expected = ArithmeticException.class)
    public void testMulAndCheckLong_overflow_throwsException() {
        MathUtils.mulAndCheck(Long.MAX_VALUE, 2L);
    }

    // Tests binomialCoefficient calculations
    @Test
    public void testBinomialCoefficient_validInputs() {
        assertEquals(1L, MathUtils.binomialCoefficient(5, 0));
        assertEquals(1L, MathUtils.binomialCoefficient(5, 5));
        assertEquals(5L, MathUtils.binomialCoefficient(5, 1));
        assertEquals(10L, MathUtils.binomialCoefficient(5, 2));
        assertEquals(10L, MathUtils.binomialCoefficient(5, 3));
        assertEquals(2598960L, MathUtils.binomialCoefficient(52, 5));
        assertEquals(10.0, MathUtils.binomialCoefficientDouble(5, 2), 1e-10);
        assertEquals(Math.log(10.0), MathUtils.binomialCoefficientLog(5, 2), 1e-10);
    }

    // Tests binomialCoefficient with invalid input
    @Test(expected = IllegalArgumentException.class)
    public void testBinomialCoefficient_invalidNLessThanK_throwsException() {
        MathUtils.binomialCoefficient(3, 5);
    }

    // Tests factorial functions
    @Test
    public void testFactorial_validInputs() {
        assertEquals(1L, MathUtils.factorial(0));
        assertEquals(1L, MathUtils.factorial(1));
        assertEquals(120L, MathUtils.factorial(5));
        assertEquals(2432902008176640000L, MathUtils.factorial(20));
        assertEquals(120.0, MathUtils.factorialDouble(5), 1e-10);
        assertEquals(Math.log(120.0), MathUtils.factorialLog(5), 1e-10);
    }

    // Tests factorial with n > 20 throwing ArithmeticException
    @Test(expected = ArithmeticException.class)
    public void testFactorial_overflow_throwsException() {
        MathUtils.factorial(21);
    }

    // Tests indicator and sign functions
    @Test
    public void testIndicatorAndSign() {
        assertEquals((byte) 1, MathUtils.indicator((byte) 5));
        assertEquals((byte) -1, MathUtils.indicator((byte) -5));
        assertEquals(1.0, MathUtils.indicator(5.0), 1e-10);
        assertEquals(-1.0, MathUtils.indicator(-5.0), 1e-10);
        assertTrue(Double.isNaN(MathUtils.indicator(Double.NaN)));

        assertEquals(0, MathUtils.sign(0));
        assertEquals(1, MathUtils.sign(10));
        assertEquals(-1, MathUtils.sign(-10));
        assertEquals(0.0, MathUtils.sign(0.0), 1e-10);
        assertEquals(1.0, MathUtils.sign(10.0), 1e-10);
        assertEquals(-1.0, MathUtils.sign(-10.0), 1e-10);
        assertTrue(Double.isNaN(MathUtils.sign(Double.NaN)));
    }

    // Tests equals methods for double and double arrays
    @Test
    public void testEquals_doublesAndArrays() {
        assertTrue(MathUtils.equals(1.0, 1.0));
        assertFalse(MathUtils.equals(1.0, 2.0));
        assertTrue(MathUtils.equals(Double.NaN, Double.NaN));
        assertTrue(MathUtils.equals(1.0, 1.05, 0.1));
        assertFalse(MathUtils.equals(1.0, 1.2, 0.1));

        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertTrue(MathUtils.equals(new double[]{1.0, 2.0}, new double[]{1.0, 2.0}));
        assertFalse(MathUtils.equals(new double[]{1.0}, new double[]{1.0, 2.0}));
    }

    // Tests round, scalb, normalizeAngle, hyperbolic and log functions
    @Test
    public void testMiscellaneousMathFunctions() {
        assertEquals(1.23, MathUtils.round(1.2345, 2), 1e-10);
        assertEquals(1.24f, MathUtils.round(1.2355f, 2), 1e-5f);
        assertEquals(1.23, MathUtils.round(1.2345, 2, BigDecimal.ROUND_HALF_UP), 1e-10);

        assertEquals(8.0, MathUtils.scalb(2.0, 2), 1e-10);
        assertEquals(0.0, MathUtils.scalb(0.0, 2), 1e-10);

        assertEquals(0.0, MathUtils.normalizeAngle(2 * Math.PI, 0.0), 1e-10);
        assertEquals(Math.PI, MathUtils.normalizeAngle(3 * Math.PI, Math.PI), 1e-10);

        assertEquals(1.0, MathUtils.cosh(0.0), 1e-10);
        assertEquals(0.0, MathUtils.sinh(0.0), 1e-10);
        assertEquals(2.0, MathUtils.log(10.0, 100.0), 1e-10);
    }

    // Additional tests for full coverage

    @Test(expected = ArithmeticException.class)
    public void testAddAndCheckLong_negativeOverflow_throwsException() {
        MathUtils.addAndCheck(Long.MIN_VALUE, -1L);
    }

    @Test(expected = ArithmeticException.class)
    public void testSubAndCheckInt_positiveOverflow_throwsException() {
        MathUtils.subAndCheck(Integer.MAX_VALUE, -1);
    }

    @Test(expected = ArithmeticException.class)
    public void testSubAndCheckInt_negativeOverflow_throwsException() {
        MathUtils.subAndCheck(Integer.MIN_VALUE, 1);
    }

    @Test(expected = ArithmeticException.class)
    public void testSubAndCheckLong_positiveOverflow_throwsException() {
        MathUtils.subAndCheck(Long.MAX_VALUE, -1L);
    }

    @Test(expected = ArithmeticException.class)
    public void testMulAndCheckInt_positiveOverflow_throwsException() {
        MathUtils.mulAndCheck(Integer.MAX_VALUE, 2);
    }

    @Test(expected = ArithmeticException.class)
    public void testMulAndCheckInt_negativeOverflow_throwsException() {
        MathUtils.mulAndCheck(Integer.MIN_VALUE, 2);
    }

    @Test(expected = ArithmeticException.class)
    public void testMulAndCheckLong_negativeOverflow1_throwsException() {
        MathUtils.mulAndCheck(Long.MIN_VALUE, 2L);
    }

    @Test(expected = ArithmeticException.class)
    public void testMulAndCheckLong_negativeOverflow2_throwsException() {
        MathUtils.mulAndCheck(Long.MIN_VALUE, -1L);
    }

    @Test(expected = ArithmeticException.class)
    public void testMulAndCheckLong_negativeOverflow3_throwsException() {
        MathUtils.mulAndCheck(-1L, Long.MIN_VALUE);
    }

    @Test
    public void testMulAndCheckLong_validNegativeProducts() {
        assertEquals(-42L, MathUtils.mulAndCheck(-6L, 7L));
        assertEquals(-42L, MathUtils.mulAndCheck(6L, -7L));
        assertEquals(42L, MathUtils.mulAndCheck(-6L, -7L));
        assertEquals(Long.MIN_VALUE, MathUtils.mulAndCheck(Long.MIN_VALUE, 1L));
        assertEquals(Long.MIN_VALUE, MathUtils.mulAndCheck(1L, Long.MIN_VALUE));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFactorial_negativeInput_throwsException() {
        MathUtils.factorial(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFactorialDouble_negativeInput_throwsException() {
        MathUtils.factorialDouble(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFactorialLog_negativeInput_throwsException() {
        MathUtils.factorialLog(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBinomialCoefficient_negativeN_throwsException() {
        MathUtils.binomialCoefficient(-1, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBinomialCoefficient_negativeK_throwsException() {
        MathUtils.binomialCoefficient(5, -1);
    }

    @Test(expected = ArithmeticException.class)
    public void testBinomialCoefficient_overflow_throwsException() {
        MathUtils.binomialCoefficient(67, 30);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBinomialCoefficientDouble_negativeInput_throwsException() {
        MathUtils.binomialCoefficientDouble(-1, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBinomialCoefficientDouble_kGreaterThanN_throwsException() {
        MathUtils.binomialCoefficientDouble(3, 5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBinomialCoefficientLog_negativeInput_throwsException() {
        MathUtils.binomialCoefficientLog(-1, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBinomialCoefficientLog_kGreaterThanN_throwsException() {
        MathUtils.binomialCoefficientLog(3, 5);
    }

    @Test
    public void testIndicatorAdditionalTypes() {
        assertEquals((short) 1, MathUtils.indicator((short) 5));
        assertEquals((short) -1, MathUtils.indicator((short) -5));
        assertEquals(1, MathUtils.indicator(5));
        assertEquals(-1, MathUtils.indicator(-5));
        assertEquals(1L, MathUtils.indicator(5L));
        assertEquals(-1L, MathUtils.indicator(-5L));
        assertEquals(1.0f, MathUtils.indicator(5.0f), 1e-5f);
        assertEquals(-1.0f, MathUtils.indicator(-5.0f), 1e-5f);
        assertTrue(Float.isNaN(MathUtils.indicator(Float.NaN)));
    }

    @Test
    public void testSignAdditionalTypes() {
        assertEquals((byte) 0, MathUtils.sign((byte) 0));
        assertEquals((byte) 1, MathUtils.sign((byte) 10));
        assertEquals((byte) -1, MathUtils.sign((byte) -10));

        assertEquals((short) 0, MathUtils.sign((short) 0));
        assertEquals((short) 1, MathUtils.sign((short) 10));
        assertEquals((short) -1, MathUtils.sign((short) -10));

        assertEquals(0L, MathUtils.sign(0L));
        assertEquals(1L, MathUtils.sign(10L));
        assertEquals(-1L, MathUtils.sign(-10L));

        assertEquals(0.0f, MathUtils.sign(0.0f), 1e-5f);
        assertEquals(1.0f, MathUtils.sign(10.0f), 1e-5f);
        assertEquals(-1.0f, MathUtils.sign(-10.0f), 1e-5f);
        assertTrue(Float.isNaN(MathUtils.sign(Float.NaN)));
    }

    @Test
    public void testEqualsAdditionalCases() {
        assertFalse(MathUtils.equals(Double.NaN, 1.0));
        assertFalse(MathUtils.equals(1.0, Double.NaN));
        assertTrue(MathUtils.equals(Double.NaN, Double.NaN, 0.1));
        assertFalse(MathUtils.equals(Double.NaN, 1.0, 0.1));
        assertFalse(MathUtils.equals(1.0, Double.NaN, 0.1));

        assertFalse(MathUtils.equals(null, new double[]{1.0}));
        assertTrue(MathUtils.equals(new double[]{Double.NaN}, new double[]{Double.NaN}));
        assertFalse(MathUtils.equals(new double[]{1.0}, new double[]{2.0}));
    }

    @Test
    public void testHash() {
        assertEquals(MathUtils.hash(1.0), MathUtils.hash(1.0));
        assertTrue(MathUtils.hash(1.0) != MathUtils.hash(2.0));
        assertEquals(0, MathUtils.hash((double[]) null));
        assertEquals(MathUtils.hash(new double[]{1.0, 2.0}), MathUtils.hash(new double[]{1.0, 2.0}));
    }

    @Test
    public void testRoundSpecialValues() {
        assertTrue(Double.isNaN(MathUtils.round(Double.NaN, 2)));
        assertEquals(Double.POSITIVE_INFINITY, MathUtils.round(Double.POSITIVE_INFINITY, 2), 1e-10);
        assertEquals(Double.NEGATIVE_INFINITY, MathUtils.round(Double.NEGATIVE_INFINITY, 2), 1e-10);

        assertTrue(Float.isNaN(MathUtils.round(Float.NaN, 2)));
        assertEquals(Float.POSITIVE_INFINITY, MathUtils.round(Float.POSITIVE_INFINITY, 2), 1e-5f);
        assertEquals(Float.NEGATIVE_INFINITY, MathUtils.round(Float.NEGATIVE_INFINITY, 2), 1e-5f);

        assertEquals(1.23f, MathUtils.round(1.2345f, 2, BigDecimal.ROUND_HALF_UP), 1e-5f);
    }

    @Test
    public void testCompareTo() {
        assertEquals(0, MathUtils.compareTo(1.0, 1.05, 0.1));
        assertEquals(-1, MathUtils.compareTo(1.0, 2.0, 0.1));
        assertEquals(1, MathUtils.compareTo(2.0, 1.0, 0.1));
    }
}