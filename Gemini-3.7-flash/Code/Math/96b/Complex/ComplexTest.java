package org.apache.commons.math.complex;

import org.junit.Test;
import static org.junit.Assert.*;

public class ComplexTest {

    private static final double EPSILON = 1e-10;

    // Tests equals with the same instance, null, other types, and identical values
    @Test
    public void testEquals_variousObjects_returnsCorrectComparison() {
        Complex z = new Complex(3.0, 4.0);
        assertTrue(z.equals(z));
        assertFalse(z.equals(null));
        assertFalse(z.equals("Not a complex"));
        assertTrue(z.equals(new Complex(3.0, 4.0)));
        assertFalse(z.equals(new Complex(3.0, 5.0)));
        assertFalse(z.equals(new Complex(4.0, 4.0)));
    }

    // Tests equals and hashCode consistency for NaN complex numbers
    @Test
    public void testEqualsAndHashCode_nanValues_returnsTrueAndConsistentHash() {
        Complex nan1 = Complex.NaN;
        Complex nan2 = new Complex(Double.NaN, 1.0);
        Complex nan3 = new Complex(1.0, Double.NaN);

        assertTrue(nan1.equals(nan2));
        assertTrue(nan2.equals(nan3));
        assertEquals(nan1.hashCode(), nan2.hashCode());
        assertEquals(7, nan1.hashCode());

        Complex regular = new Complex(2.0, 3.0);
        assertNotEquals(7, regular.hashCode());
    }

    // Tests multiplication of pure imaginary numbers and equality comparison
    @Test
    public void testMultiply_pureImaginaryNumbers_returnsExpectedProduct() {
        Complex z = new Complex(0.0, 1.0);
        Complex w = new Complex(0.0, -1.0);
        Complex result = z.multiply(w);

        assertEquals(1.0, result.getReal(), EPSILON);
        assertEquals(0.0, result.getImaginary(), EPSILON);
        assertTrue(result.equals(Complex.ONE));
    }

    // Tests getters, isNaN, and isInfinite
    @Test
    public void testGettersAndPredicates_finiteInfiniteAndNaN_returnsCorrectBooleans() {
        Complex c = new Complex(1.5, -2.5);
        assertEquals(1.5, c.getReal(), EPSILON);
        assertEquals(-2.5, c.getImaginary(), EPSILON);
        assertFalse(c.isNaN());
        assertFalse(c.isInfinite());

        assertTrue(Complex.NaN.isNaN());
        assertFalse(Complex.NaN.isInfinite());

        assertTrue(Complex.INF.isInfinite());
        assertFalse(Complex.INF.isNaN());

        Complex infReal = new Complex(Double.POSITIVE_INFINITY, 0.0);
        assertTrue(infReal.isInfinite());
    }

    // Tests abs for normal, zero, NaN, and Infinite values
    @Test
    public void testAbs_normalInfiniteAndNaN_returnsExpectedValues() {
        Complex z = new Complex(3.0, 4.0);
        assertEquals(5.0, z.abs(), EPSILON);

        Complex z2 = new Complex(4.0, 3.0);
        assertEquals(5.0, z2.abs(), EPSILON);

        assertEquals(0.0, Complex.ZERO.abs(), EPSILON);
        assertEquals(4.0, new Complex(0.0, 4.0).abs(), EPSILON);
        assertEquals(3.0, new Complex(3.0, 0.0).abs(), EPSILON);

        assertTrue(Double.isNaN(Complex.NaN.abs()));
        assertEquals(Double.POSITIVE_INFINITY, Complex.INF.abs(), EPSILON);
    }

    // Tests add method for normal and NaN cases
    @Test
    public void testAdd_normalAndNaN_returnsCorrectSum() {
        Complex a = new Complex(1.0, 2.0);
        Complex b = new Complex(3.0, 4.0);
        Complex result = a.add(b);

        assertEquals(4.0, result.getReal(), EPSILON);
        assertEquals(6.0, result.getImaginary(), EPSILON);

        assertTrue(a.add(Complex.NaN).isNaN());
    }

    // Tests subtract method for normal and NaN cases
    @Test
    public void testSubtract_normalAndNaN_returnsCorrectDifference() {
        Complex a = new Complex(5.0, 7.0);
        Complex b = new Complex(2.0, 3.0);
        Complex result = a.subtract(b);

        assertEquals(3.0, result.getReal(), EPSILON);
        assertEquals(4.0, result.getImaginary(), EPSILON);

        assertTrue(a.subtract(Complex.NaN).isNaN());
    }

    // Tests conjugate and negate methods
    @Test
    public void testConjugateAndNegate_normalAndNaN_returnsConjugateAndInverse() {
        Complex z = new Complex(2.0, -3.0);

        Complex conj = z.conjugate();
        assertEquals(2.0, conj.getReal(), EPSILON);
        assertEquals(3.0, conj.getImaginary(), EPSILON);
        assertTrue(Complex.NaN.conjugate().isNaN());

        Complex neg = z.negate();
        assertEquals(-2.0, neg.getReal(), EPSILON);
        assertEquals(3.0, neg.getImaginary(), EPSILON);
        assertTrue(Complex.NaN.negate().isNaN());
    }

    // Tests multiply method with finite, infinite, and NaN operands
    @Test
    public void testMultiply_finiteInfiniteAndNaN_returnsCorrectProduct() {
        Complex a = new Complex(2.0, 3.0);
        Complex b = new Complex(4.0, 5.0);
        Complex result = a.multiply(b);

        assertEquals(-7.0, result.getReal(), EPSILON);
        assertEquals(22.0, result.getImaginary(), EPSILON);

        assertTrue(a.multiply(Complex.NaN).isNaN());
        assertTrue(a.multiply(Complex.INF).isInfinite());
    }

    // Tests divide method including divisions by zero, infinite rhs, and standard cases
    @Test
    public void testDivide_variousOperands_returnsCorrectQuotient() {
        Complex a = new Complex(1.0, 2.0);
        Complex b = new Complex(3.0, 4.0);
        Complex result = a.divide(b);

        assertEquals(11.0 / 25.0, result.getReal(), EPSILON);
        assertEquals(2.0 / 25.0, result.getImaginary(), EPSILON);

        // Branch Math.abs(c) < Math.abs(d)
        Complex c = new Complex(2.0, 4.0);
        Complex d = new Complex(1.0, 3.0);
        Complex resCD = c.divide(d);
        assertEquals(1.4, resCD.getReal(), EPSILON);
        assertEquals(-0.2, resCD.getImaginary(), EPSILON);

        assertTrue(a.divide(Complex.ZERO).isNaN());
        assertTrue(a.divide(Complex.NaN).isNaN());
        assertEquals(Complex.ZERO, a.divide(Complex.INF));
    }

    // Tests exp and log functions
    @Test
    public void testExpAndLog_normalAndNaN_returnsExpectedResults() {
        Complex z = new Complex(0.0, Math.PI);
        Complex expZ = z.exp();
        assertEquals(-1.0, expZ.getReal(), EPSILON);
        assertEquals(0.0, expZ.getImaginary(), EPSILON);

        Complex logOne = Complex.ONE.log();
        assertEquals(0.0, logOne.getReal(), EPSILON);
        assertEquals(0.0, logOne.getImaginary(), EPSILON);

        assertTrue(Complex.NaN.exp().isNaN());
        assertTrue(Complex.NaN.log().isNaN());
    }

    // Tests power function and null exponent exception
    @Test
    public void testPow_normalAndNull_returnsPowerOrThrows() {
        Complex base = new Complex(2.0, 0.0);
        Complex exp = new Complex(3.0, 0.0);
        Complex result = base.pow(exp);

        assertEquals(8.0, result.getReal(), EPSILON);
        assertEquals(0.0, result.getImaginary(), EPSILON);
    }

    // Tests pow method throwing NullPointerException for null argument
    @Test(expected = NullPointerException.class)
    public void testPow_nullExponent_throwsNullPointerException() {
        Complex.ONE.pow(null);
    }

    // Tests trigonometric functions sin, cos, and tan
    @Test
    public void testSinCosTan_standardValues_returnsCorrectValues() {
        Complex z = new Complex(Math.PI / 2.0, 0.0);

        Complex sinZ = z.sin();
        assertEquals(1.0, sinZ.getReal(), EPSILON);
        assertEquals(0.0, sinZ.getImaginary(), EPSILON);

        Complex cosZ = z.cos();
        assertEquals(0.0, cosZ.getReal(), EPSILON);
        assertEquals(0.0, cosZ.getImaginary(), EPSILON);

        Complex tanZ = new Complex(0.0, 0.0).tan();
        assertEquals(0.0, tanZ.getReal(), EPSILON);
        assertEquals(0.0, tanZ.getImaginary(), EPSILON);

        assertTrue(Complex.NaN.sin().isNaN());
        assertTrue(Complex.NaN.cos().isNaN());
        assertTrue(Complex.NaN.tan().isNaN());
    }

    // Tests hyperbolic functions sinh, cosh, and tanh
    @Test
    public void testSinhCoshTanh_standardValues_returnsCorrectValues() {
        Complex zero = Complex.ZERO;

        Complex sinhZero = zero.sinh();
        assertEquals(0.0, sinhZero.getReal(), EPSILON);
        assertEquals(0.0, sinhZero.getImaginary(), EPSILON);

        Complex coshZero = zero.cosh();
        assertEquals(1.0, coshZero.getReal(), EPSILON);
        assertEquals(0.0, coshZero.getImaginary(), EPSILON);

        Complex tanhZero = zero.tanh();
        assertEquals(0.0, tanhZero.getReal(), EPSILON);
        assertEquals(0.0, tanhZero.getImaginary(), EPSILON);

        assertTrue(Complex.NaN.sinh().isNaN());
        assertTrue(Complex.NaN.cosh().isNaN());
        assertTrue(Complex.NaN.tanh().isNaN());
    }

    // Tests inverse trigonometric functions asin, acos, and atan
    @Test
    public void testAsinAcosAtan_standardValues_returnsCorrectValues() {
        Complex zero = Complex.ZERO;

        Complex asinZero = zero.asin();
        assertEquals(0.0, asinZero.getReal(), EPSILON);
        assertEquals(0.0, asinZero.getImaginary(), EPSILON);

        Complex acosOne = Complex.ONE.acos();
        assertEquals(0.0, acosOne.getReal(), EPSILON);
        assertEquals(0.0, acosOne.getImaginary(), EPSILON);

        Complex atanZero = zero.atan();
        assertEquals(0.0, atanZero.getReal(), EPSILON);
        assertEquals(0.0, atanZero.getImaginary(), EPSILON);

        assertTrue(Complex.NaN.asin().isNaN());
        assertTrue(Complex.NaN.acos().isNaN());
        assertTrue(Complex.NaN.atan().isNaN());
    }

    // Tests sqrt and sqrt1z functions
    @Test
    public void testSqrtAndSqrt1z_standardValues_returnsSquareRoots() {
        Complex z = new Complex(0.0, 4.0);
        Complex sqrtZ = z.sqrt();
        assertEquals(Math.sqrt(2.0), sqrtZ.getReal(), EPSILON);
        assertEquals(Math.sqrt(2.0), sqrtZ.getImaginary(), EPSILON);

        Complex negReal = new Complex(-4.0, 0.0);
        Complex sqrtNeg = negReal.sqrt();
        assertEquals(0.0, sqrtNeg.getReal(), EPSILON);
        assertEquals(2.0, sqrtNeg.getImaginary(), EPSILON);

        Complex zeroSqrt = Complex.ZERO.sqrt();
        assertEquals(0.0, zeroSqrt.getReal(), EPSILON);
        assertEquals(0.0, zeroSqrt.getImaginary(), EPSILON);

        Complex sqrt1zZero = Complex.ZERO.sqrt1z();
        assertEquals(1.0, sqrt1zZero.getReal(), EPSILON);
        assertEquals(0.0, sqrt1zZero.getImaginary(), EPSILON);

        assertTrue(Complex.NaN.sqrt().isNaN());
        assertTrue(Complex.NaN.sqrt1z().isNaN());
    }
}