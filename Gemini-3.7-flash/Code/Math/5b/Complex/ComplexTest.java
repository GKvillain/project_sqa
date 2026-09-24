package org.apache.commons.math3.complex;

import java.util.List;
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.junit.Test;
import static org.junit.Assert.*;

public class ComplexTest {

    private static final double EPSILON = 1.0e-15;

    // Tests reciprocal of zero (Defects4J Math-5 regression)
    @Test
    public void testReciprocal_zero_returnsInf() {
        Complex z = Complex.ZERO.reciprocal();
        assertEquals(Complex.INF, z);
    }

    // Tests normal reciprocal operation
    @Test
    public void testReciprocal_normalValues_returnsCorrectReciprocal() {
        Complex z = new Complex(3.0, 4.0);
        Complex result = z.reciprocal();
        assertEquals(0.12, result.getReal(), EPSILON);
        assertEquals(-0.16, result.getImaginary(), EPSILON);
    }

    // Tests reciprocal of infinite and NaN values
    @Test
    public void testReciprocal_infiniteAndNaN_returnsZeroAndNaN() {
        assertEquals(Complex.ZERO, Complex.INF.reciprocal());
        assertEquals(Complex.NaN, Complex.NaN.reciprocal());
    }

    // Tests addition with normal, NaN and null values
    @Test
    public void testAdd_variousInputs_returnsExpectedResult() {
        Complex c1 = new Complex(1.0, 2.0);
        Complex c2 = new Complex(3.0, 4.0);
        Complex result = c1.add(c2);
        assertEquals(4.0, result.getReal(), EPSILON);
        assertEquals(6.0, result.getImaginary(), EPSILON);

        assertEquals(Complex.NaN, c1.add(Complex.NaN));
        Complex realAdd = c1.add(3.0);
        assertEquals(4.0, realAdd.getReal(), EPSILON);
        assertEquals(2.0, realAdd.getImaginary(), EPSILON);
    }

    // Tests add method throwing NullArgumentException
    @Test(expected = NullArgumentException.class)
    public void testAdd_nullInput_throwsNullArgumentException() {
        Complex.ONE.add(null);
    }

    // Tests subtraction operations
    @Test
    public void testSubtract_normalAndDouble_returnsCorrectResult() {
        Complex c1 = new Complex(5.0, 7.0);
        Complex c2 = new Complex(2.0, 3.0);
        Complex result = c1.subtract(c2);
        assertEquals(3.0, result.getReal(), EPSILON);
        assertEquals(4.0, result.getImaginary(), EPSILON);

        Complex realSub = c1.subtract(2.0);
        assertEquals(3.0, realSub.getReal(), EPSILON);
        assertEquals(7.0, realSub.getImaginary(), EPSILON);
    }

    // Tests subtract method throwing NullArgumentException
    @Test(expected = NullArgumentException.class)
    public void testSubtract_nullInput_throwsNullArgumentException() {
        Complex.ONE.subtract(null);
    }

    // Tests multiplication behavior including infinite rules
    @Test
    public void testMultiply_normalAndInf_returnsCorrectResult() {
        Complex c1 = new Complex(2.0, 3.0);
        Complex c2 = new Complex(4.0, 5.0);
        Complex result = c1.multiply(c2);
        assertEquals(-7.0, result.getReal(), EPSILON);
        assertEquals(22.0, result.getImaginary(), EPSILON);

        assertEquals(Complex.INF, c1.multiply(Complex.INF));
        assertEquals(Complex.INF, c1.multiply(Double.POSITIVE_INFINITY));

        Complex intMul = c1.multiply(2);
        assertEquals(4.0, intMul.getReal(), EPSILON);
        assertEquals(6.0, intMul.getImaginary(), EPSILON);
    }

    // Tests division with normal and infinite divisors
    @Test
    public void testDivide_normalAndInfinite_returnsCorrectResult() {
        Complex c1 = new Complex(1.0, 1.0);
        Complex c2 = new Complex(1.0, 1.0);
        assertEquals(new Complex(1.0, 0.0), c1.divide(c2));

        assertEquals(Complex.ZERO, c1.divide(Complex.INF));
        assertEquals(Complex.NaN, c1.divide(Complex.ZERO));
        assertEquals(Complex.NaN, Complex.INF.divide(Complex.INF));

        Complex realDiv = c1.divide(2.0);
        assertEquals(0.5, realDiv.getReal(), EPSILON);
        assertEquals(0.5, realDiv.getImaginary(), EPSILON);
    }

    // Tests absolute value computation including boundary conditions
    @Test
    public void testAbs_normalNaNInfinite_returnsCorrectValue() {
        assertEquals(5.0, new Complex(3.0, 4.0).abs(), EPSILON);
        assertEquals(5.0, new Complex(4.0, 3.0).abs(), EPSILON);
        assertEquals(3.0, new Complex(3.0, 0.0).abs(), EPSILON);
        assertEquals(3.0, new Complex(0.0, 3.0).abs(), EPSILON);
        assertTrue(Double.isNaN(Complex.NaN.abs()));
        assertEquals(Double.POSITIVE_INFINITY, Complex.INF.abs(), EPSILON);
    }

    // Tests conjugate and negation operations
    @Test
    public void testConjugateAndNegate_normalValues_returnsExpectedResults() {
        Complex z = new Complex(3.0, -4.0);
        Complex conj = z.conjugate();
        assertEquals(3.0, conj.getReal(), EPSILON);
        assertEquals(4.0, conj.getImaginary(), EPSILON);

        Complex neg = z.negate();
        assertEquals(-3.0, neg.getReal(), EPSILON);
        assertEquals(4.0, neg.getImaginary(), EPSILON);

        assertEquals(Complex.NaN, Complex.NaN.conjugate());
        assertEquals(Complex.NaN, Complex.NaN.negate());
    }

    // Tests exponential, logarithm and power operations
    @Test
    public void testExpLogPow_validInputs_returnsCorrectResults() {
        Complex z = new Complex(0.0, 0.0);
        assertEquals(Complex.ONE, z.exp());
        assertEquals(Complex.ONE, Complex.ONE.pow(Complex.ONE));
        assertEquals(Complex.ONE, Complex.ONE.pow(2.0));

        Complex iExp = Complex.I.multiply(new Complex(Math.PI, 0.0)).exp();
        assertEquals(-1.0, iExp.getReal(), 1.0e-10);
        assertEquals(0.0, iExp.getImaginary(), 1.0e-10);

        Complex logOne = Complex.ONE.log();
        assertEquals(0.0, logOne.getReal(), EPSILON);
        assertEquals(0.0, logOne.getImaginary(), EPSILON);
    }

    // Tests trigonometric functions sin, cos and sqrt
    @Test
    public void testTrigonometricAndSqrt_validInputs_returnsCorrectResults() {
        Complex zero = Complex.ZERO;
        assertEquals(Complex.ZERO, zero.sin());
        assertEquals(Complex.ONE, zero.cos());
        assertEquals(Complex.ZERO, zero.sqrt());

        Complex posRealSqrt = new Complex(4.0, 0.0).sqrt();
        assertEquals(2.0, posRealSqrt.getReal(), EPSILON);
        assertEquals(0.0, posRealSqrt.getImaginary(), EPSILON);

        Complex negRealSqrt = new Complex(-4.0, 0.0).sqrt();
        assertEquals(0.0, negRealSqrt.getReal(), EPSILON);
        assertEquals(2.0, negRealSqrt.getImaginary(), EPSILON);

        Complex sqrt1z = new Complex(0.0, 0.0).sqrt1z();
        assertEquals(Complex.ONE, sqrt1z);
    }

    // Tests tangent and hyperbolic branches including >20 and <-20 saturation
    @Test
    public void testTanAndTanh_boundaries_returnsExpectedResults() {
        Complex largeImagPositive = new Complex(1.0, 25.0);
        assertEquals(new Complex(0.0, 1.0), largeImagPositive.tan());

        Complex largeImagNegative = new Complex(1.0, -25.0);
        assertEquals(new Complex(0.0, -1.0), largeImagNegative.tan());

        Complex largeRealPositive = new Complex(25.0, 1.0);
        assertEquals(new Complex(1.0, 0.0), largeRealPositive.tanh());

        Complex largeRealNegative = new Complex(-25.0, 1.0);
        assertEquals(new Complex(-1.0, 0.0), largeRealNegative.tanh());
    }

    // Tests nth roots computation for positive n
    @Test
    public void testNthRoot_validDegree_returnsCorrectRoots() {
        Complex z = Complex.ONE;
        List<Complex> roots = z.nthRoot(2);
        assertEquals(2, roots.size());
        assertEquals(1.0, roots.get(0).getReal(), 1.0e-5);
        assertEquals(0.0, roots.get(0).getImaginary(), 1.0e-5);
        assertEquals(-1.0, roots.get(1).getReal(), 1.0e-5);
        assertEquals(0.0, roots.get(1).getImaginary(), 1.0e-5);

        List<Complex> nanRoots = Complex.NaN.nthRoot(2);
        assertEquals(1, nanRoots.size());
        assertEquals(Complex.NaN, nanRoots.get(0));

        List<Complex> infRoots = Complex.INF.nthRoot(2);
        assertEquals(1, infRoots.size());
        assertEquals(Complex.INF, infRoots.get(0));
    }

    // Tests nthRoot throwing NotPositiveException on non-positive n
    @Test(expected = NotPositiveException.class)
    public void testNthRoot_nonPositiveN_throwsNotPositiveException() {
        Complex.ONE.nthRoot(0);
    }

    // Tests equals, hashCode, and valueOf factory methods
    @Test
    public void testEqualsHashCodeAndValueOf_contracts() {
        Complex c1 = new Complex(1.0, 2.0);
        Complex c2 = new Complex(1.0, 2.0);
        Complex c3 = new Complex(1.0, 3.0);

        assertTrue(c1.equals(c1));
        assertTrue(c1.equals(c2));
        assertFalse(c1.equals(c3));
        assertFalse(c1.equals(null));
        assertFalse(c1.equals(new Object()));
        assertEquals(c1.hashCode(), c2.hashCode());

        assertTrue(Complex.NaN.equals(new Complex(Double.NaN, 0.0)));
        assertEquals(Complex.NaN.hashCode(), new Complex(0.0, Double.NaN).hashCode());

        assertEquals(Complex.NaN, Complex.valueOf(Double.NaN, 1.0));
        assertEquals(Complex.NaN, Complex.valueOf(Double.NaN));
        assertEquals(new Complex(1.0, 2.0), Complex.valueOf(1.0, 2.0));
        assertEquals(new Complex(1.0, 0.0), Complex.valueOf(1.0));
    }
}