package org.apache.commons.math3.complex;

import static org.junit.Assert.*;
import org.junit.Test;
import org.apache.commons.math3.exception.NullArgumentException;

/**
 * JUnit 4 test class for Complex (Defects4J Bug 5b).
 */
public class ComplexTest {

    // Tests normal constructor and getters
    @Test
    public void testConstructor_normalValues_createsCorrectComplex() {
        Complex c = new Complex(3.0, 4.0);
        assertEquals(3.0, c.getReal(), 0.0);
        assertEquals(4.0, c.getImaginary(), 0.0);
        assertFalse(c.isNaN());
        assertFalse(c.isInfinite());
    }

    // Tests NaN constructor
    @Test
    public void testConstructor_nanParts_isNaN() {
        Complex c = new Complex(Double.NaN, 1.0);
        assertTrue(c.isNaN());
        assertFalse(c.isInfinite());
    }

    // Tests infinite constructor
    @Test
    public void testConstructor_infiniteReal_isInfiniteNotNaN() {
        Complex c = new Complex(Double.POSITIVE_INFINITY, 0.0);
        assertTrue(c.isInfinite());
        assertFalse(c.isNaN());
    }

    // Tests abs() with NaN
    @Test
    public void testAbs_nan_returnsNaN() {
        Complex c = new Complex(Double.NaN, 2.0);
        assertEquals(Double.NaN, c.abs(), 0.0);
    }

    // Tests abs() with infinite
    @Test
    public void testAbs_infinite_returnsInfinity() {
        Complex c = new Complex(Double.POSITIVE_INFINITY, 0.0);
        assertEquals(Double.POSITIVE_INFINITY, c.abs(), 0.0);
    }

    // Tests abs() with zero
    @Test
    public void testAbs_zero_returnsZero() {
        Complex c = Complex.ZERO;
        assertEquals(0.0, c.abs(), 0.0);
    }

    // Tests abs() with normal values (3+4i => 5)
    @Test
    public void testAbs_normalPythagorean_returnsCorrect() {
        Complex c = new Complex(3.0, 4.0);
        assertEquals(5.0, c.abs(), 1e-14);
    }

    // Tests add() with null
    @Test(expected = NullArgumentException.class)
    public void testAdd_null_throwsNullArgumentException() {
        Complex.ONE.add((Complex) null);
    }

    // Tests add() leading to NaN
    @Test
    public void testAdd_nanAddend_returnsNaN() {
        Complex result = Complex.ONE.add(Complex.NaN);
        assertTrue(result.isNaN());
    }

    // Tests add() normal
    @Test
    public void testAdd_normalValues_returnsSum() {
        Complex a = new Complex(1.0, 2.0);
        Complex b = new Complex(3.0, 4.0);
        Complex sum = a.add(b);
        assertEquals(4.0, sum.getReal(), 0.0);
        assertEquals(6.0, sum.getImaginary(), 0.0);
    }

    // Tests multiply() with NaN
    @Test
    public void testMultiply_nan_returnsNaN() {
        Complex result = Complex.I.multiply(Complex.NaN);
        assertTrue(result.isNaN());
    }

    // Tests multiply() with infinite factor => returns INF
    @Test
    public void testMultiply_infiniteFactor_returnsInf() {
        Complex result = Complex.ONE.multiply(Double.POSITIVE_INFINITY);
        assertTrue(result.isInfinite());
    }

    // Tests multiply() normal
    @Test
    public void testMultiply_normalValues_returnsProduct() {
        Complex a = new Complex(1.0, 2.0);
        Complex b = new Complex(3.0, 4.0);
        Complex product = a.multiply(b);
        assertEquals(-5.0, product.getReal(), 1e-14);
        assertEquals(10.0, product.getImaginary(), 1e-14);
    }

    // Tests divide() with zero divisor
    @Test
    public void testDivide_zeroDivisor_returnsNaN() {
        Complex result = Complex.ONE.divide(Complex.ZERO);
        assertTrue(result.isNaN());
    }

    // Tests divide() with NaN
    @Test
    public void testDivide_nan_returnsNaN() {
        Complex result = Complex.ONE.divide(Complex.NaN);
        assertTrue(result.isNaN());
    }

    // Tests reciprocal() with zero
    @Test
    public void testReciprocal_zero_returnsNaN() {
        Complex result = Complex.ZERO.reciprocal();
        assertTrue(result.isNaN());
    }

    // Tests reciprocal() with normal
    @Test
    public void testReciprocal_normal_returnsInverse() {
        Complex c = new Complex(2.0, 0.0);
        Complex inv = c.reciprocal();
        assertEquals(0.5, inv.getReal(), 1e-14);
        assertEquals(0.0, inv.getImaginary(), 1e-14);
    }

    // Tests equals with NaN (both NaN considered equal)
    @Test
    public void testEquals_bothNaN_returnsTrue() {
        Complex nan1 = new Complex(Double.NaN, 1.0);
        Complex nan2 = new Complex(2.0, Double.NaN);
        assertTrue(nan1.equals(nan2));
    }

    // Tests equals with normal equal values
    @Test
    public void testEquals_equalComplex_returnsTrue() {
        Complex a = new Complex(1.0, 2.0);
        Complex b = new Complex(1.0, 2.0);
        assertTrue(a.equals(b));
    }

    // Tests equals with different real
    @Test
    public void testEquals_differentReal_returnsFalse() {
        Complex a = new Complex(1.0, 2.0);
        Complex b = new Complex(3.0, 2.0);
        assertFalse(a.equals(b));
    }

    // Tests sqrt() with NaN
    @Test
    public void testSqrt_nan_returnsNaN() {
        Complex result = new Complex(Double.NaN, 0.0).sqrt();
        assertTrue(result.isNaN());
    }

    // Tests sqrt() with zero
    @Test
    public void testSqrt_zero_returnsZero() {
        Complex result = Complex.ZERO.sqrt();
        assertEquals(0.0, result.getReal(), 0.0);
        assertEquals(0.0, result.getImaginary(), 0.0);
    }

    // Tests sqrt() with negative real (i.e. -1 => i)
    @Test
    public void testSqrt_negativeReal_returnsPositiveImaginary() {
        Complex result = new Complex(-1.0, 0.0).sqrt();
        assertEquals(0.0, result.getReal(), 1e-14);
        assertEquals(1.0, result.getImaginary(), 1e-14);
    }

    // Tests tan() with imaginary > 20 => returns (0,1)
    @Test
    public void testTan_largeImaginary_returns0plusI() {
        Complex result = new Complex(0.0, 30.0).tan();
        assertEquals(0.0, result.getReal(), 1e-14);
        assertEquals(1.0, result.getImaginary(), 1e-14);
    }

    // Tests tan() with infinite real => NaN
    @Test
    public void testTan_infiniteReal_returnsNaN() {
        Complex result = new Complex(Double.POSITIVE_INFINITY, 0.0).tan();
        assertTrue(result.isNaN());
    }

    // Tests tanh() with real > 20 => returns (1,0)
    @Test
    public void testTanh_largeReal_returns1() {
        Complex result = new Complex(30.0, 0.0).tanh();
        assertEquals(1.0, result.getReal(), 1e-14);
        assertEquals(0.0, result.getImaginary(), 1e-14);
    }

    // Tests tanh() with infinite imaginary => NaN
    @Test
    public void testTanh_infiniteImaginary_returnsNaN() {
        Complex result = new Complex(0.0, Double.NEGATIVE_INFINITY).tanh();
        assertTrue(result.isNaN());
    }

    // Tests log() with zero => returns (-Infinity, 0)
    @Test
    public void testLog_zero_returnsNegativeInfinity() {
        Complex result = Complex.ZERO.log();
        assertEquals(Double.NEGATIVE_INFINITY, result.getReal(), 0.0);
        assertEquals(0.0, result.getImaginary(), 1e-14);
    }

    // Tests valueOf with NaN parts returns NaN constant
    @Test
    public void testValueOf_nanParts_returnsNaN() {
        Complex c = Complex.valueOf(Double.NaN, 0.0);
        assertTrue(c.isNaN());
    }
}