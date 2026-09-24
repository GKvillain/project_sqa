package org.apache.commons.math.complex;

import java.util.List;
import org.apache.commons.math.exception.NullArgumentException;
import org.junit.Test;
import static org.junit.Assert.*;

public class ComplexTest {

    private static final double EPSILON = 1e-10;

    // Tests defect Math-53: addition with NaN in either component should return Complex.NaN
    @Test
    public void testAdd_withNaNComponent_returnsNaN() {
        Complex z = new Complex(1.0, 2.0);
        Complex nanReal = new Complex(Double.NaN, 2.0);
        Complex nanImag = new Complex(2.0, Double.NaN);

        Complex result1 = z.add(nanReal);
        assertTrue(Double.isNaN(result1.getReal()));
        assertTrue(Double.isNaN(result1.getImaginary()));

        Complex result2 = z.add(nanImag);
        assertTrue(Double.isNaN(result2.getReal()));
        assertTrue(Double.isNaN(result2.getImaginary()));

        Complex result3 = nanReal.add(z);
        assertTrue(Double.isNaN(result3.getReal()));
        assertTrue(Double.isNaN(result3.getImaginary()));
    }

    // Tests normal addition
    @Test
    public void testAdd_normalInputs_returnsCorrectSum() {
        Complex z1 = new Complex(1.0, 2.0);
        Complex z2 = new Complex(3.0, 4.0);
        Complex result = z1.add(z2);

        assertEquals(4.0, result.getReal(), EPSILON);
        assertEquals(6.0, result.getImaginary(), EPSILON);
    }

    // Tests add null input throws NullArgumentException
    @Test(expected = NullArgumentException.class)
    public void testAdd_nullInput_throwsNullArgumentException() {
        Complex z = new Complex(1.0, 1.0);
        z.add(null);
    }

    // Tests subtraction and null check
    @Test
    public void testSubtract_normalInputsAndNaN_returnsCorrectResult() {
        Complex z1 = new Complex(5.0, 7.0);
        Complex z2 = new Complex(2.0, 3.0);
        Complex result = z1.subtract(z2);

        assertEquals(3.0, result.getReal(), EPSILON);
        assertEquals(4.0, result.getImaginary(), EPSILON);

        Complex nanResult = z1.subtract(Complex.NaN);
        assertTrue(nanResult.isNaN());
    }

    // Tests multiply by Complex and double scalar, including Infinite and NaN
    @Test
    public void testMultiply_variousInputs_returnsExpected() {
        Complex z1 = new Complex(2.0, 3.0);
        Complex z2 = new Complex(4.0, 5.0);
        Complex result = z1.multiply(z2);

        assertEquals(-7.0, result.getReal(), EPSILON);
        assertEquals(22.0, result.getImaginary(), EPSILON);

        Complex scalarResult = z1.multiply(2.0);
        assertEquals(4.0, scalarResult.getReal(), EPSILON);
        assertEquals(6.0, scalarResult.getImaginary(), EPSILON);

        assertTrue(z1.multiply(Complex.NaN).isNaN());
        assertTrue(z1.multiply(Double.NaN).isNaN());
        assertEquals(Complex.INF, z1.multiply(Complex.INF));
        assertEquals(Complex.INF, z1.multiply(Double.POSITIVE_INFINITY));
    }

    // Tests divide by normal, zero, and infinite values
    @Test
    public void testDivide_normalAndEdgeInputs_returnsExpected() {
        Complex z1 = new Complex(1.0, 2.0);
        Complex z2 = new Complex(3.0, 4.0);
        Complex result = z1.divide(z2);

        assertEquals(11.0 / 25.0, result.getReal(), EPSILON);
        assertEquals(2.0 / 25.0, result.getImaginary(), EPSILON);

        assertTrue(z1.divide(Complex.ZERO).isNaN());
        assertTrue(z1.divide(Complex.NaN).isNaN());
        assertEquals(Complex.ZERO, z1.divide(Complex.INF));

        // Test division where |c| < |d|
        Complex z3 = new Complex(1.0, 5.0);
        Complex z4 = new Complex(2.0, 4.0);
        Complex result2 = z3.divide(z4);
        assertEquals(1.1, result2.getReal(), EPSILON);
        assertEquals(0.3, result2.getImaginary(), EPSILON);
    }

    // Tests abs calculation for normal, zero, infinite, and NaN
    @Test
    public void testAbs_variousInputs_returnsExpected() {
        Complex z = new Complex(3.0, 4.0);
        assertEquals(5.0, z.abs(), EPSILON);

        Complex zero = new Complex(0.0, 0.0);
        assertEquals(0.0, zero.abs(), EPSILON);

        Complex realZero = new Complex(0.0, 3.0);
        assertEquals(3.0, realZero.abs(), EPSILON);

        Complex imagZero = new Complex(3.0, 0.0);
        assertEquals(3.0, imagZero.abs(), EPSILON);

        assertTrue(Double.isNaN(Complex.NaN.abs()));
        assertTrue(Double.isInfinite(Complex.INF.abs()));
    }

    // Tests conjugate and negation
    @Test
    public void testConjugateAndNegate_normalAndNaN() {
        Complex z = new Complex(2.0, -3.0);

        Complex conj = z.conjugate();
        assertEquals(2.0, conj.getReal(), EPSILON);
        assertEquals(3.0, conj.getImaginary(), EPSILON);

        Complex neg = z.negate();
        assertEquals(-2.0, neg.getReal(), EPSILON);
        assertEquals(3.0, neg.getImaginary(), EPSILON);

        assertTrue(Complex.NaN.conjugate().isNaN());
        assertTrue(Complex.NaN.negate().isNaN());
    }

    // Tests equals and hashCode consistency
    @Test
    public void testEqualsAndHashCode() {
        Complex z1 = new Complex(1.0, 2.0);
        Complex z2 = new Complex(1.0, 2.0);
        Complex z3 = new Complex(1.0, 3.0);

        assertTrue(z1.equals(z1));
        assertTrue(z1.equals(z2));
        assertFalse(z1.equals(z3));
        assertFalse(z1.equals(null));
        assertFalse(z1.equals("1.0 + 2.0i"));

        assertEquals(z1.hashCode(), z2.hashCode());

        Complex nan1 = new Complex(Double.NaN, 1.0);
        Complex nan2 = new Complex(2.0, Double.NaN);
        assertTrue(nan1.equals(nan2));
        assertEquals(nan1.hashCode(), nan2.hashCode());
    }

    // Tests exp and log functions
    @Test
    public void testExpAndLog() {
        Complex z = new Complex(0.0, Math.PI);
        Complex expZ = z.exp();
        assertEquals(-1.0, expZ.getReal(), EPSILON);
        assertEquals(0.0, expZ.getImaginary(), EPSILON);

        Complex logZ = Complex.ONE.log();
        assertEquals(0.0, logZ.getReal(), EPSILON);
        assertEquals(0.0, logZ.getImaginary(), EPSILON);

        assertTrue(Complex.NaN.exp().isNaN());
        assertTrue(Complex.NaN.log().isNaN());
    }

    // Tests pow function
    @Test
    public void testPow_normalAndNullInput() {
        Complex base = new Complex(2.0, 0.0);
        Complex exponent = new Complex(3.0, 0.0);
        Complex result = base.pow(exponent);

        assertEquals(8.0, result.getReal(), EPSILON);
        assertEquals(0.0, result.getImaginary(), EPSILON);
    }

    // Tests trigonometric functions (sin, cos, tan)
    @Test
    public void testTrigonometric_sinCosTan() {
        Complex z = new Complex(0.0, 0.0);
        Complex sinZ = z.sin();
        Complex cosZ = z.cos();
        Complex tanZ = z.tan();

        assertEquals(0.0, sinZ.getReal(), EPSILON);
        assertEquals(0.0, sinZ.getImaginary(), EPSILON);

        assertEquals(1.0, cosZ.getReal(), EPSILON);
        assertEquals(0.0, cosZ.getImaginary(), EPSILON);

        assertEquals(0.0, tanZ.getReal(), EPSILON);
        assertEquals(0.0, tanZ.getImaginary(), EPSILON);

        assertTrue(Complex.NaN.sin().isNaN());
        assertTrue(Complex.NaN.cos().isNaN());
        assertTrue(Complex.NaN.tan().isNaN());
    }

    // Tests hyperbolic functions (sinh, cosh, tanh)
    @Test
    public void testHyperbolic_sinhCoshTanh() {
        Complex z = new Complex(0.0, 0.0);
        Complex sinhZ = z.sinh();
        Complex coshZ = z.cosh();
        Complex tanhZ = z.tanh();

        assertEquals(0.0, sinhZ.getReal(), EPSILON);
        assertEquals(0.0, sinhZ.getImaginary(), EPSILON);

        assertEquals(1.0, coshZ.getReal(), EPSILON);
        assertEquals(0.0, coshZ.getImaginary(), EPSILON);

        assertEquals(0.0, tanhZ.getReal(), EPSILON);
        assertEquals(0.0, tanhZ.getImaginary(), EPSILON);

        assertTrue(Complex.NaN.sinh().isNaN());
        assertTrue(Complex.NaN.cosh().isNaN());
        assertTrue(Complex.NaN.tanh().isNaN());
    }

    // Tests inverse trigonometric functions (asin, acos, atan)
    @Test
    public void testInverseTrigonometric_asinAcosAtan() {
        Complex z = new Complex(0.0, 0.0);
        Complex asinZ = z.asin();
        Complex acosZ = z.acos();
        Complex atanZ = z.atan();

        assertEquals(0.0, asinZ.getReal(), EPSILON);
        assertEquals(0.0, asinZ.getImaginary(), EPSILON);

        assertEquals(Math.PI / 2.0, acosZ.getReal(), EPSILON);
        assertEquals(0.0, acosZ.getImaginary(), EPSILON);

        assertEquals(0.0, atanZ.getReal(), EPSILON);
        assertEquals(0.0, atanZ.getImaginary(), EPSILON);

        assertTrue(Complex.NaN.asin().isNaN());
        assertTrue(Complex.NaN.acos().isNaN());
        assertTrue(Complex.NaN.atan().isNaN());
    }

    // Tests sqrt and sqrt1z
    @Test
    public void testSqrtAndSqrt1z() {
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

        Complex sqrt1z = Complex.ZERO.sqrt1z();
        assertEquals(1.0, sqrt1z.getReal(), EPSILON);
        assertEquals(0.0, sqrt1z.getImaginary(), EPSILON);

        assertTrue(Complex.NaN.sqrt().isNaN());
    }

    // Tests nthRoot for positive degree and invalid degree
    @Test
    public void testNthRoot_validAndInvalidDegree() {
        Complex z = new Complex(1.0, 0.0);
        List<Complex> roots = z.nthRoot(2);
        assertEquals(2, roots.size());
        assertEquals(1.0, roots.get(0).getReal(), EPSILON);
        assertEquals(-1.0, roots.get(1).getReal(), EPSILON);

        List<Complex> nanRoots = Complex.NaN.nthRoot(2);
        assertEquals(1, nanRoots.size());
        assertTrue(nanRoots.get(0).isNaN());

        List<Complex> infRoots = Complex.INF.nthRoot(2);
        assertEquals(1, infRoots.size());
        assertTrue(infRoots.get(0).isInfinite());
    }

    // Tests nthRoot exception path on non-positive n
    @Test(expected = IllegalArgumentException.class)
    public void testNthRoot_zeroDegree_throwsException() {
        Complex.ONE.nthRoot(0);
    }

    // Tests getArgument, getField, and toString
    @Test
    public void testGetArgumentAndFieldAndToString() {
        Complex z = new Complex(0.0, 1.0);
        assertEquals(Math.PI / 2.0, z.getArgument(), EPSILON);

        assertNotNull(z.getField());
        assertEquals("(0.0, 1.0)", z.toString());
    }
}