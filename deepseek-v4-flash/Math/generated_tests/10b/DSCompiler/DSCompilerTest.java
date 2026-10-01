package org.apache.commons.math3.analysis.differentiation;

import static org.junit.Assert.*;
import org.junit.Test;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NumberIsTooLargeException;

public class DSCompilerTest {
    private static final double EPS = 1e-15;
    private final DSCompiler compiler = DSCompiler.getCompiler(2, 2);
    private final int size = compiler.getSize();

    @Test
    public void testGetCompiler_typical_returnsNonNull() {
        assertNotNull(compiler);
        assertEquals(2, compiler.getFreeParameters());
        assertEquals(2, compiler.getOrder());
    }

    @Test
    public void testGetSize_twoParamsOrderTwo_returnsSix() {
        assertEquals(6, size);
    }

    @Test
    public void testAdd_simpleValues_returnsCorrectSum() {
        double[] x = new double[size];
        double[] y = new double[size];
        double[] result = new double[size];
        x[0] = 3.5;
        y[0] = 2.1;
        compiler.add(x, 0, y, 0, result, 0);
        assertEquals(5.6, result[0], EPS);
    }

    @Test
    public void testSubtract_simpleValues_returnsCorrectDifference() {
        double[] x = new double[size];
        double[] y = new double[size];
        double[] result = new double[size];
        x[0] = 5.0;
        y[0] = 2.0;
        compiler.subtract(x, 0, y, 0, result, 0);
        assertEquals(3.0, result[0], EPS);
    }

    @Test
    public void testMultiply_simpleValues_returnsCorrectProduct() {
        double[] x = new double[size];
        double[] y = new double[size];
        double[] result = new double[size];
        x[0] = 4.0;
        y[0] = 3.0;
        compiler.multiply(x, 0, y, 0, result, 0);
        assertEquals(12.0, result[0], EPS);
    }

    @Test
    public void testDivide_simpleValues_returnsCorrectQuotient() {
        double[] x = new double[size];
        double[] y = new double[size];
        double[] result = new double[size];
        x[0] = 10.0;
        y[0] = 2.0;
        compiler.divide(x, 0, y, 0, result, 0);
        assertEquals(5.0, result[0], EPS);
    }

    @Test
    public void testPow_doubleExponent_returnsCorrectValue() {
        double[] op = new double[size];
        double[] result = new double[size];
        op[0] = 2.0;
        compiler.pow(op, 0, 3.0, result, 0);
        assertEquals(8.0, result[0], EPS);
    }

    @Test
    public void testPow_intExponent_returnsCorrectValue() {
        double[] op = new double[size];
        double[] result = new double[size];
        op[0] = 2.0;
        compiler.pow(op, 0, 3, result, 0);
        assertEquals(8.0, result[0], EPS);
    }

    @Test
    public void testRemainder_simpleValues_returnsCorrectRemainder() {
        double[] lhs = new double[size];
        double[] rhs = new double[size];
        double[] result = new double[size];
        lhs[0] = 10.0;
        rhs[0] = 3.0;
        compiler.remainder(lhs, 0, rhs, 0, result, 0);
        assertEquals(1.0, result[0], EPS);
    }

    @Test
    public void testAtan2_quadrant1_xPositive_yPositive_returnsCorrectAngle() {
        double[] y = new double[size];
        double[] x = new double[size];
        double[] result = new double[size];
        y[0] = 1.0;
        x[0] = 1.0;
        compiler.atan2(y, 0, x, 0, result, 0);
        assertEquals(Math.PI / 4, result[0], EPS);
    }

    @Test
    public void testAtan2_quadrant2_xNegative_yPositive_returnsCorrectAngle() {
        double[] y = new double[size];
        double[] x = new double[size];
        double[] result = new double[size];
        y[0] = 1.0;
        x[0] = -1.0;
        compiler.atan2(y, 0, x, 0, result, 0);
        assertEquals(3 * Math.PI / 4, result[0], EPS);
    }

    @Test
    public void testAtan2_quadrant3_xNegative_yNegative_returnsCorrectAngle() {
        double[] y = new double[size];
        double[] x = new double[size];
        double[] result = new double[size];
        y[0] = -1.0;
        x[0] = -1.0;
        compiler.atan2(y, 0, x, 0, result, 0);
        assertEquals(-3 * Math.PI / 4, result[0], EPS);
    }

    @Test
    public void testAtan2_quadrant4_xPositive_yNegative_returnsCorrectAngle() {
        double[] y = new double[size];
        double[] x = new double[size];
        double[] result = new double[size];
        y[0] = -1.0;
        x[0] = 1.0;
        compiler.atan2(y, 0, x, 0, result, 0);
        assertEquals(-Math.PI / 4, result[0], EPS);
    }

    @Test
    public void testAtan2_specialZeroSigns_returnsCorrectSigns() {
        double[] y = new double[size];
        double[] x = new double[size];
        double[] result = new double[size];

        // (+0, +0)
        y[0] = 0.0;
        x[0] = 0.0;
        compiler.atan2(y, 0, x, 0, result, 0);
        assertEquals(0.0, result[0], EPS);

        // (-0, +0)
        y[0] = -0.0;
        x[0] = 0.0;
        compiler.atan2(y, 0, x, 0, result, 0);
        assertTrue("Expected -0.0 for atan2(-0, 0)", Double.compare(result[0], -0.0) == 0);

        // (+0, -0)
        y[0] = 0.0;
        x[0] = -0.0;
        compiler.atan2(y, 0, x, 0, result, 0);
        assertEquals(Math.PI, result[0], EPS);

        // (-0, -0)
        y[0] = -0.0;
        x[0] = -0.0;
        compiler.atan2(y, 0, x, 0, result, 0);
        assertEquals(-Math.PI, result[0], EPS);
    }

    @Test(expected = NumberIsTooLargeException.class)
    public void testGetPartialDerivativeIndex_sumExceedsOrder_throwsException() {
        compiler.getPartialDerivativeIndex(1, 2);
    }

    @Test(expected = DimensionMismatchException.class)
    public void testGetPartialDerivativeIndex_wrongNumberOfOrders_throwsException() {
        compiler.getPartialDerivativeIndex(1);
    }

    @Test
    public void testGetPartialDerivativeIndex_zeroOrders_returnsZero() {
        assertEquals(0, compiler.getPartialDerivativeIndex(0, 0));
    }

    @Test
    public void testGetPartialDerivativeOrders_zeroIndex_returnsZeroOrders() {
        int[] orders = compiler.getPartialDerivativeOrders(0);
        assertArrayEquals(new int[]{0, 0}, orders);
    }

    @Test
    public void testGetPartialDerivativeIndex_andOrders_areInverse() {
        int idx = compiler.getPartialDerivativeIndex(1, 0);
        int[] orders = compiler.getPartialDerivativeOrders(idx);
        assertArrayEquals(new int[]{1, 0}, orders);
    }

    @Test(expected = DimensionMismatchException.class)
    public void testCheckCompatibility_differentParameters_throwsException() {
        DSCompiler other = DSCompiler.getCompiler(3, 2);
        compiler.checkCompatibility(other);
    }

    @Test(expected = DimensionMismatchException.class)
    public void testCheckCompatibility_differentOrder_throwsException() {
        DSCompiler other = DSCompiler.getCompiler(2, 3);
        compiler.checkCompatibility(other);
    }

    @Test
    public void testCheckCompatibility_sameCompiler_doesNotThrow() {
        compiler.checkCompatibility(DSCompiler.getCompiler(2, 2));
    }

    // ========================  New test cases for uncovered parts ========================

    @Test
    public void testCompose_identity_returnsSameValue() {
        double[] operand = new double[size];
        operand[0] = 2.0;
        operand[compiler.getPartialDerivativeIndex(1, 0)] = 1.0;
        double[] f = {2.0, 1.0, 0.0};
        double[] result = new double[size];
        compiler.compose(operand, 0, f, result, 0);
        assertEquals(2.0, result[0], EPS);
        assertEquals(1.0, result[compiler.getPartialDerivativeIndex(1, 0)], EPS);
        assertEquals(0.0, result[compiler.getPartialDerivativeIndex(0, 1)], EPS);
        assertEquals(0.0, result[compiler.getPartialDerivativeIndex(2, 0)], EPS);
        assertEquals(0.0, result[compiler.getPartialDerivativeIndex(1, 1)], EPS);
        assertEquals(0.0, result[compiler.getPartialDerivativeIndex(0, 2)], EPS);
    }

    @Test
    public void testCompose_square_returnsCorrectDerivative() {
        double[] operand = new double[size];
        operand[0] = 3.0;
        operand[compiler.getPartialDerivativeIndex(1, 0)] = 1.0;
        double[] f = {9.0, 6.0, 2.0};
        double[] result = new double[size];
        compiler.compose(operand, 0, f, result, 0);
        assertEquals(9.0, result[0], EPS);
        assertEquals(6.0, result[compiler.getPartialDerivativeIndex(1, 0)], EPS);
        assertEquals(2.0, result[compiler.getPartialDerivativeIndex(2, 0)], EPS);
    }

    @Test
    public void testExp_zero_returnsOne() {
        double[] operand = new double[size];
        operand[0] = 0.0;
        operand[compiler.getPartialDerivativeIndex(1, 0)] = 1.0;
        double[] result = new double[size];
        compiler.exp(operand, 0, result, 0);
        assertEquals(1.0, result[0], EPS);
        assertEquals(1.0, result[compiler.getPartialDerivativeIndex(1, 0)], EPS);
    }

    @Test
    public void testLog_one_returnsZero() {
        double[] operand = new double[size];
        operand[0] = 1.0;
        operand[compiler.getPartialDerivativeIndex(1, 0)] = 1.0;
        double[] result = new double[size];
        compiler.log(operand, 0, result, 0);
        assertEquals(0.0, result[0], EPS);
        assertEquals(1.0, result[compiler.getPartialDerivativeIndex(1, 0)], EPS);
    }

    @Test
    public void testSin_zero_returnsZero() {
        double[] operand = new double[size];
        operand[0] = 0.0;
        operand[compiler.getPartialDerivativeIndex(1, 0)] = 1.0;
        double[] result = new double[size];
        compiler.sin(operand, 0, result, 0);
        assertEquals(0.0, result[0], EPS);
        assertEquals(1.0, result[compiler.getPartialDerivativeIndex(1, 0)], EPS);
        assertEquals(0.0, result[compiler.getPartialDerivativeIndex(2, 0)], EPS);
    }

    @Test
    public void testCos_zero_returnsOne() {
        double[] operand = new double[size];
        operand[0] = 0.0;
        operand[compiler.getPartialDerivativeIndex(1, 0)] = 1.0;
        double[] result = new double[size];
        compiler.cos(operand, 0, result, 0);
        assertEquals(1.0, result[0], EPS);
        assertEquals(0.0, result[compiler.getPartialDerivativeIndex(1, 0)], EPS);
        assertEquals(-1.0, result[compiler.getPartialDerivativeIndex(2, 0)], EPS);
    }

    @Test
    public void testTan_zero_returnsZero() {
        double[] operand = new double[size];
        operand[0] = 0.0;
        operand[compiler.getPartialDerivativeIndex(1, 0)] = 1.0;
        double[] result = new double[size];
        compiler.tan(operand, 0, result, 0);
        assertEquals(0.0, result[0], EPS);
        assertEquals(1.0, result[compiler.getPartialDerivativeIndex(1, 0)], EPS);
    }

    @Test
    public void testAsin_zero_returnsZero() {
        double[] operand = new double[size];
        operand[0] = 0.0;
        operand[compiler.getPartialDerivativeIndex(1, 0)] = 1.0;
        double[] result = new double[size];
        compiler.asin(operand, 0, result, 0);
        assertEquals(0.0, result[0], EPS);
        assertEquals(1.0, result[compiler.getPartialDerivativeIndex(1, 0)], EPS);
    }

    @Test
    public void testAcos_one_returnsZero() {
        double[] operand = new double[size];
        operand[0] = 1.0;
        operand[compiler.getPartialDerivativeIndex(1, 0)] = 1.0;
        double[] result = new double[size];
        compiler.acos(operand, 0, result, 0);
        assertEquals(0.0, result[0], EPS);
    }

    @Test
    public void testAtan_zero_returnsZero() {
        double[] operand = new double[size];
        operand[0] = 0.0;
        operand[compiler.getPartialDerivativeIndex(1, 0)] = 1.0;
        double[] result = new double[size];
        compiler.atan(operand, 0, result, 0);
        assertEquals(0.0, result[0], EPS);
        assertEquals(1.0, result[compiler.getPartialDerivativeIndex(1, 0)], EPS);
    }

    @Test
    public void testSinh_zero_returnsZero() {
        double[] operand = new double[size];
        operand[0] = 0.0;
        operand[compiler.getPartialDerivativeIndex(1, 0)] = 1.0;
        double[] result = new double[size];
        compiler.sinh(operand, 0, result, 0);
        assertEquals(0.0, result[0], EPS);
        assertEquals(1.0, result[compiler.getPartialDerivativeIndex(1, 0)], EPS);
    }

    @Test
    public void testCosh_zero_returnsOne() {
        double[] operand = new double[size];
        operand[0] = 0.0;
        operand[compiler.getPartialDerivativeIndex(1, 0)] = 1.0;
        double[] result = new double[size];
        compiler.cosh(operand, 0, result, 0);
        assertEquals(1.0, result[0], EPS);
        assertEquals(0.0, result[compiler.getPartialDerivativeIndex(1, 0)], EPS);
    }

    @Test
    public void testTanh_zero_returnsZero() {
        double[] operand = new double[size];
        operand[0] = 0.0;
        operand[compiler.getPartialDerivativeIndex(1, 0)] = 1.0;
        double[] result = new double[size];
        compiler.tanh(operand, 0, result, 0);
        assertEquals(0.0, result[0], EPS);
        assertEquals(1.0, result[compiler.getPartialDerivativeIndex(1, 0)], EPS);
    }

    @Test
    public void testAsinh_zero_returnsZero() {
        double[] operand = new double[size];
        operand[0] = 0.0;
        operand[compiler.getPartialDerivativeIndex(1, 0)] = 1.0;
        double[] result = new double[size];
        compiler.asinh(operand, 0, result, 0);
        assertEquals(0.0, result[0], EPS);
        assertEquals(1.0, result[compiler.getPartialDerivativeIndex(1, 0)], EPS);
    }

    @Test
    public void testAcosh_one_returnsZero() {
        double[] operand = new double[size];
        operand[0] = 1.0;
        operand[compiler.getPartialDerivativeIndex(1, 0)] = 1.0;
        double[] result = new double[size];
        compiler.acosh(operand, 0, result, 0);
        assertEquals(0.0, result[0], EPS);
    }

    @Test
    public void testAtanh_zero_returnsZero() {
        double[] operand = new double[size];
        operand[0] = 0.0;
        operand[compiler.getPartialDerivativeIndex(1, 0)] = 1.0;
        double[] result = new double[size];
        compiler.atanh(operand, 0, result, 0);
        assertEquals(0.0, result[0], EPS);
        assertEquals(1.0, result[compiler.getPartialDerivativeIndex(1, 0)], EPS);
    }

    @Test
    public void testSqrt_one_returnsOne() {
        double[] operand = new double[size];
        operand[0] = 1.0;
        operand[compiler.getPartialDerivativeIndex(1, 0)] = 1.0;
        double[] result = new double[size];
        compiler.sqrt(operand, 0, result, 0);
        assertEquals(1.0, result[0], EPS);
        assertEquals(0.5, result[compiler.getPartialDerivativeIndex(1, 0)], EPS);
    }

    @Test
    public void testCbrt_one_returnsOne() {
        double[] operand = new double[size];
        operand[0] = 1.0;
        operand[compiler.getPartialDerivativeIndex(1, 0)] = 1.0;
        double[] result = new double[size];
        compiler.cbrt(operand, 0, result, 0);
        assertEquals(1.0, result[0], EPS);
        assertEquals(1.0/3.0, result[compiler.getPartialDerivativeIndex(1, 0)], EPS);
    }

    @Test
    public void testRootN_one_returnsOne() {
        double[] operand = new double[size];
        operand[0] = 1.0;
        operand[compiler.getPartialDerivativeIndex(1, 0)] = 1.0;
        double[] result = new double[size];
        compiler.rootN(operand, 0, 5, result, 0);
        assertEquals(1.0, result[0], EPS);
        assertEquals(1.0/5.0, result[compiler.getPartialDerivativeIndex(1, 0)], EPS);
    }

    @Test
    public void testHypot_threeFour_returnsFive() {
        double[] x = new double[size];
        double[] y = new double[size];
        double[] result = new double[size];
        x[0] = 3.0;
        y[0] = 4.0;
        compiler.hypot(x, 0, y, 0, result, 0);
        assertEquals(5.0, result[0], EPS);
    }

    @Test
    public void testPowArrayExponent_simple_returnsCorrectValue() {
        double[] a = new double[size];
        double[] b = new double[size];
        double[] result = new double[size];
        a[0] = 2.0;
        b[0] = 3.0;
        compiler.pow(a, 0, b, 0, result, 0);
        assertEquals(8.0, result[0], EPS);
    }

    @Test
    public void testLinearCombination_twoTerms_returnsCorrectValue() {
        double[] a1 = new double[size]; a1[0] = 1.0;
        double[] b1 = new double[size]; b1[0] = 2.0;
        double[] a2 = new double[size]; a2[0] = 3.0;
        double[] b2 = new double[size]; b2[0] = 4.0;
        double[] result = new double[size];
        compiler.linearCombination(a1, 0, b1, 0, a2, 0, b2, 0, result, 0);
        assertEquals(14.0, result[0], EPS);
    }

    @Test
    public void testLinearCombination_threeTerms_returnsCorrectValue() {
        double[] a1 = new double[size]; a1[0] = 1.0;
        double[] b1 = new double[size]; b1[0] = 2.0;
        double[] a2 = new double[size]; a2[0] = 3.0;
        double[] b2 = new double[size]; b2[0] = 4.0;
        double[] a3 = new double[size]; a3[0] = 5.0;
        double[] b3 = new double[size]; b3[0] = 6.0;
        double[] result = new double[size];
        compiler.linearCombination(a1, 0, b1, 0, a2, 0, b2, 0, a3, 0, b3, 0, result, 0);
        assertEquals(1*2+3*4+5*6, result[0], EPS);
    }

    @Test
    public void testGetPartialDerivativeIndex_variousOrders() {
        int[][] pairs = {{0,0}, {1,0}, {0,1}, {2,0}, {1,1}, {0,2}};
        for (int[] p : pairs) {
            int idx = compiler.getPartialDerivativeIndex(p[0], p[1]);
            assertTrue("Index out of bounds: " + idx, idx >= 0 && idx < size);
            int[] orders = compiler.getPartialDerivativeOrders(idx);
            assertArrayEquals(p, orders);
        }
    }

    @Test
    public void testGetPartialDerivativeOrders_allIndices() {
        for (int idx = 0; idx < size; idx++) {
            int[] orders = compiler.getPartialDerivativeOrders(idx);
            int sum = 0;
            for (int o : orders) sum += o;
            assertTrue("Sum of orders exceeds order", sum <= compiler.getOrder());
            int idx2 = compiler.getPartialDerivativeIndex(orders);
            assertEquals(idx, idx2);
        }
    }

    @Test(expected = DimensionMismatchException.class)
    public void testCheckCompatibility_differentBothParamsAndOrder_throwsException() {
        DSCompiler other = DSCompiler.getCompiler(3, 3);
        compiler.checkCompatibility(other);
    }

    @Test
    public void testGetCompiler_zeroParamsZeroOrder_returnsNonNull() {
        DSCompiler c = DSCompiler.getCompiler(0, 0);
        assertNotNull(c);
        assertEquals(0, c.getFreeParameters());
        assertEquals(0, c.getOrder());
        assertEquals(1, c.getSize());
    }

    @Test
    public void testGetCompiler_oneParamOneOrder_returnsNonNull() {
        DSCompiler c = DSCompiler.getCompiler(1, 1);
        assertNotNull(c);
        assertEquals(1, c.getFreeParameters());
        assertEquals(1, c.getOrder());
        assertEquals(2, c.getSize());
    }

    @Test
    public void testGetCompiler_paramsPositiveOrderZero_returnsNonNull() {
        DSCompiler c = DSCompiler.getCompiler(2, 0);
        assertNotNull(c);
        assertEquals(2, c.getFreeParameters());
        assertEquals(0, c.getOrder());
        assertEquals(1, c.getSize());
    }
}