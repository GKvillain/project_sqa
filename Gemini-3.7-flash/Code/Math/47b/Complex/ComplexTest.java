package org.apache.commons.math.complex;

import java.util.List;
import org.apache.commons.math.exception.NotPositiveException;
import org.apache.commons.math.exception.NullArgumentException;
import org.junit.Test;
import static org.junit.Assert.*;

public class ComplexTest {

    private static final double EPSILON = 1e-10;

    // Tests division by zero returning INF per javadoc specification (Defects4J Math-47)
    @Test
    public void testDivide_byZeroComplex_returnsInf() {
        Complex z = new Complex(3.0, 4.0);
        Complex result = z.divide(Complex.ZERO);
        assertEquals(Complex.INF, result);
    }

    // Tests scalar division by zero
    @Test
    public void testDivide_byZeroDouble_returnsInf() {
        Complex z = new Complex(3.0, 4.0);
        Complex result = z.divide(0.0);
        assertTrue(result.isInfinite() || result.equals(Complex.INF));
    }

    // Tests division of zero by zero returning NaN
    @Test
    public void testDivide_zeroByZero_returnsNaN() {
        Complex result = Complex.ZERO.divide(Complex.ZERO);
        assertTrue(result.isNaN());
    }

    // Tests basic division with finite non-zero divisor
    @Test
    public void testDivide_normalValues_returnsCorrectQuotient() {
        Complex x = new Complex(3.0, 4.0);
        Complex y = new Complex(1.0, 2.0);
        Complex result = x.divide(y);
        assertEquals(2.2, result.getReal(), EPSILON);
        assertEquals(-0.4, result.getImaginary(), EPSILON);
    }

    // Tests division with divisor where |real| < |imaginary|
    @Test
    public void testDivide_absRealLessThanAbsImaginary_returnsCorrectQuotient() {
        Complex x = new Complex(2.0, 3.0);
        Complex y = new Complex(1.0, 4.0);
        Complex result = x.divide(y);
        Complex expected = new Complex(14.0 / 17.0, -5.0 / 17.0);
        assertEquals(expected.getReal(), result.getReal(), EPSILON);
        assertEquals(expected.getImaginary(), result.getImaginary(), EPSILON);
    }

    // Tests divide by null throwing NullArgumentException
    @Test(expected = NullArgumentException.class)
    public void testDivide_nullArgument_throwsNullArgumentException() {
        Complex z = new Complex(1.0, 2.0);
        z.divide((Complex) null);
    }

    // Tests divide with infinite divisor returning ZERO
    @Test
    public void testDivide_infiniteDivisor_returnsZero() {
        Complex z = new Complex(3.0, 4.0);
        Complex result = z.divide(Complex.INF);
        assertEquals(Complex.ZERO, result);
    }

    // Tests divide scalar with infinite double
    @Test
    public void testDivide_infiniteDouble_returnsZero() {
        Complex z = new Complex(3.0, 4.0);
        Complex result = z.divide(Double.POSITIVE_INFINITY);
        assertEquals(Complex.ZERO, result);
    }

    // Tests addition and scalar addition
    @Test
    public void testAdd_normalAndScalar_returnsCorrectSum() {
        Complex x = new Complex(1.0, 2.0);
        Complex y = new Complex(3.0, 4.0);
        Complex sum = x.add(y);
        assertEquals(4.0, sum.getReal(), EPSILON);
        assertEquals(6.0, sum.getImaginary(), EPSILON);

        Complex scalarSum = x.add(2.0);
        assertEquals(3.0, scalarSum.getReal(), EPSILON);
        assertEquals(2.0, scalarSum.getImaginary(), EPSILON);
    }

    // Tests add with NaN operand returning NaN
    @Test
    public void testAdd_nanOperand_returnsNaN() {
        Complex x = new Complex(1.0, 2.0);
        Complex result = x.add(Complex.NaN);
        assertTrue(result.isNaN());
    }

    // Tests subtraction and scalar subtraction
    @Test
    public void testSubtract_normalAndScalar_returnsCorrectDifference() {
        Complex x = new Complex(5.0, 6.0);
        Complex y = new Complex(2.0, 4.0);
        Complex diff = x.subtract(y);
        assertEquals(3.0, diff.getReal(), EPSILON);
        assertEquals(2.0, diff.getImaginary(), EPSILON);

        Complex scalarDiff = x.subtract(2.0);
        assertEquals(3.0, scalarDiff.getReal(), EPSILON);
        assertEquals(6.0, scalarDiff.getImaginary(), EPSILON);
    }

    // Tests multiplication and scalar multiplication
    @Test
    public void testMultiply_normalAndInfinite_returnsCorrectProduct() {
        Complex x = new Complex(3.0, 4.0);
        Complex y = new Complex(-1.0, 2.0);
        Complex product = x.multiply(y);
        assertEquals(-11.0, product.getReal(), EPSILON);
        assertEquals(2.0, product.getImaginary(), EPSILON);

        Complex scalarProduct = x.multiply(2.5);
        assertEquals(7.5, scalarProduct.getReal(), EPSILON);
        assertEquals(10.0, scalarProduct.getImaginary(), EPSILON);

        Complex infProduct = x.multiply(Double.POSITIVE_INFINITY);
        assertEquals(Complex.INF, infProduct);
    }

    // Tests absolute value for normal, zero, and infinite cases
    @Test
    public void testAbs_normalZeroAndInfinite_returnsCorrectValues() {
        Complex z = new Complex(3.0, 4.0);
        assertEquals(5.0, z.abs(), EPSILON);

        assertEquals(0.0, Complex.ZERO.abs(), EPSILON);
        assertEquals(Double.POSITIVE_INFINITY, Complex.INF.abs(), EPSILON);
        assertTrue(Double.isNaN(Complex.NaN.abs()));
    }

    // Tests conjugate and negation
    @Test
    public void testConjugateAndNegate_normalValues_returnsExpected() {
        Complex z = new Complex(3.0, -4.0);
        Complex conj = z.conjugate();
        assertEquals(3.0, conj.getReal(), EPSILON);
        assertEquals(4.0, conj.getImaginary(), EPSILON);

        Complex neg = z.negate();
        assertEquals(-3.0, neg.getReal(), EPSILON);
        assertEquals(4.0, neg.getImaginary(), EPSILON);
    }

    // Tests trigonometric and hyperbolic functions
    @Test
    public void testTrigonometricFunctions_normalValues_returnsCorrectResults() {
        Complex z = new Complex(0.0, 0.0);
        Complex sin = z.sin();
        assertEquals(0.0, sin.getReal(), EPSILON);
        assertEquals(0.0, sin.getImaginary(), EPSILON);

        Complex cos = z.cos();
        assertEquals(1.0, cos.getReal(), EPSILON);
        assertEquals(0.0, cos.getImaginary(), EPSILON);

        Complex tan = z.tan();
        assertEquals(0.0, tan.getReal(), EPSILON);
        assertEquals(0.0, tan.getImaginary(), EPSILON);

        Complex sinh = z.sinh();
        assertEquals(0.0, sinh.getReal(), EPSILON);
        assertEquals(0.0, sinh.getImaginary(), EPSILON);

        Complex cosh = z.cosh();
        assertEquals(1.0, cosh.getReal(), EPSILON);
        assertEquals(0.0, cosh.getImaginary(), EPSILON);

        Complex tanh = z.tanh();
        assertEquals(0.0, tanh.getReal(), EPSILON);
        assertEquals(0.0, tanh.getImaginary(), EPSILON);
    }

    // Tests inverse trigonometric functions: asin, acos, atan
    @Test
    public void testInverseTrigonometricFunctions_zero_returnsZero() {
        Complex z = new Complex(0.0, 0.0);
        assertEquals(0.0, z.asin().getReal(), EPSILON);
        assertEquals(0.0, z.asin().getImaginary(), EPSILON);
        assertEquals(Math.PI / 2.0, z.acos().getReal(), EPSILON);
        assertEquals(0.0, z.atan().getReal(), EPSILON);
    }

    // Tests exponential, natural logarithm, and square root
    @Test
    public void testExpLogSqrt_normalValues_returnsCorrectResults() {
        Complex z = new Complex(0.0, Math.PI);
        Complex exp = z.exp();
        assertEquals(-1.0, exp.getReal(), EPSILON);
        assertEquals(0.0, exp.getImaginary(), EPSILON);

        Complex one = new Complex(1.0, 0.0);
        Complex log = one.log();
        assertEquals(0.0, log.getReal(), EPSILON);
        assertEquals(0.0, log.getImaginary(), EPSILON);

        Complex negFour = new Complex(-4.0, 0.0);
        Complex sqrt = negFour.sqrt();
        assertEquals(0.0, sqrt.getReal(), EPSILON);
        assertEquals(2.0, sqrt.getImaginary(), EPSILON);

        Complex sqrt1z = Complex.ZERO.sqrt1z();
        assertEquals(1.0, sqrt1z.getReal(), EPSILON);
        assertEquals(0.0, sqrt1z.getImaginary(), EPSILON);
    }

    // Tests pow with complex exponent and scalar exponent
    @Test
    public void testPow_normalValues_returnsCorrectResult() {
        Complex base = new Complex(0.0, 1.0);
        Complex result = base.pow(2.0);
        assertEquals(-1.0, result.getReal(), EPSILON);
        assertEquals(0.0, result.getImaginary(), EPSILON);

        Complex complexExp = new Complex(2.0, 0.0);
        Complex compResult = base.pow(complexExp);
        assertEquals(-1.0, compResult.getReal(), EPSILON);
        assertEquals(0.0, compResult.getImaginary(), EPSILON);
    }

    // Tests nthRoot with valid degree n
    @Test
    public void testNthRoot_validN_returnsAllRoots() {
        Complex z = new Complex(1.0, 0.0);
        List<Complex> roots = z.nthRoot(2);
        assertEquals(2, roots.size());
        assertEquals(1.0, roots.get(0).getReal(), EPSILON);
        assertEquals(0.0, roots.get(0).getImaginary(), EPSILON);
        assertEquals(-1.0, roots.get(1).getReal(), EPSILON);
        assertEquals(0.0, roots.get(1).getImaginary(), EPSILON);
    }

    // Tests nthRoot throwing NotPositiveException on non-positive n
    @Test(expected = NotPositiveException.class)
    public void testNthRoot_nonPositiveN_throwsNotPositiveException() {
        Complex z = new Complex(1.0, 2.0);
        z.nthRoot(0);
    }

    // Tests equals, hashCode, toString, and valueOf factory methods
    @Test
    public void testEqualsHashCodeAndValueOf_variousCases_returnsExpected() {
        Complex c1 = Complex.valueOf(1.0, 2.0);
        Complex c2 = new Complex(1.0, 2.0);
        Complex c3 = Complex.valueOf(1.0);
        Complex nanVal = Complex.valueOf(Double.NaN, 1.0);

        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
        assertFalse(c1.equals(c3));
        assertFalse(c1.equals(null));
        assertFalse(c1.equals("String"));
        assertTrue(nanVal.isNaN());
        assertEquals(Complex.NaN, nanVal);
        assertEquals(7, Complex.NaN.hashCode());

        assertEquals("(1.0, 2.0)", c1.toString());
        assertNotNull(c1.getField());
        assertEquals(Math.atan2(2.0, 1.0), c1.getArgument(), EPSILON);
    }
}