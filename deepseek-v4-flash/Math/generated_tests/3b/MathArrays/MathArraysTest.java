package org.apache.commons.math3.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.NonMonotonicSequenceException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.NoDataException;

/**
 * Test class for MathArrays (Defects4J bug 3b).
 */
public class MathArraysTest {

    // ---- linearCombination (array) ----

    @Test
    // Tests length 1 (may expose defect: ArrayIndexOutOfBoundsException)
    public void testLinearCombinationArray_length1_returnsCorrectProduct() {
        double[] a = {5.0};
        double[] b = {6.0};
        double result = MathArrays.linearCombination(a, b);
        assertEquals(30.0, result, 1e-14);
    }

    @Test
    public void testLinearCombinationArray_length2_returnsCorrectSum() {
        double[] a = {1.0, 2.0};
        double[] b = {3.0, 4.0};
        double result = MathArrays.linearCombination(a, b);
        assertEquals(11.0, result, 1e-14);
    }

    @Test
    public void testLinearCombinationArray_withNaN_returnsNaN() {
        double[] a = {Double.NaN, 1.0};
        double[] b = {1.0, 1.0};
        double result = MathArrays.linearCombination(a, b);
        assertTrue(Double.isNaN(result));
    }

    @Test
    public void testLinearCombinationArray_withInfinity_returnsInfinity() {
        double[] a = {Double.POSITIVE_INFINITY, 1.0};
        double[] b = {1.0, 1.0};
        double result = MathArrays.linearCombination(a, b);
        assertEquals(Double.POSITIVE_INFINITY, result, 0.0);
    }

    @Test(expected = DimensionMismatchException.class)
    public void testLinearCombinationArray_dimensionMismatch_throwsException() {
        MathArrays.linearCombination(new double[]{1.0}, new double[]{2.0, 3.0});
    }

    // ---- linearCombination (2, 3, 4 terms) ----

    @Test
    public void testLinearCombination2Term_returnsCorrectSum() {
        double result = MathArrays.linearCombination(1.0, 2.0, 3.0, 4.0);
        assertEquals(14.0, result, 1e-14);
    }

    @Test
    public void testLinearCombination3Term_returnsCorrectSum() {
        double result = MathArrays.linearCombination(1.0, 2.0, 3.0, 4.0, 5.0, 6.0);
        assertEquals(44.0, result, 1e-14);
    }

    @Test
    public void testLinearCombination4Term_returnsCorrectSum() {
        double result = MathArrays.linearCombination(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0);
        assertEquals(100.0, result, 1e-14);
    }

    // ---- normalizeArray ----

    @Test
    public void testNormalizeArray_normal_returnsNormalized() {
        double[] values = {1.0, 2.0, 3.0};
        double[] normalized = MathArrays.normalizeArray(values, 10.0);
        assertEquals(10.0 / 6, normalized[0], 1e-14);
        assertEquals(20.0 / 6, normalized[1], 1e-14);
        assertEquals(5.0, normalized[2], 1e-14);
        double sum = 0;
        for (double v : normalized) {
            sum += v;
        }
        assertEquals(10.0, sum, 1e-14);
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testNormalizeArray_targetSumInfinite_throwsException() {
        MathArrays.normalizeArray(new double[]{1.0}, Double.POSITIVE_INFINITY);
    }

    @Test(expected = MathArithmeticException.class)
    public void testNormalizeArray_sumZero_throwsException() {
        MathArrays.normalizeArray(new double[]{0.0, 0.0}, 10.0);
    }

    // ---- ebeAdd ----

    @Test(expected = DimensionMismatchException.class)
    public void testEbeAdd_dimensionMismatch_throwsException() {
        MathArrays.ebeAdd(new double[]{1.0}, new double[]{2.0, 3.0});
    }

    @Test
    public void testEbeAdd_returnsCorrectSum() {
        double[] a = {1.0, 2.0, 3.0};
        double[] b = {4.0, 5.0, 6.0};
        double[] result = MathArrays.ebeAdd(a, b);
        assertArrayEquals(new double[]{5.0, 7.0, 9.0}, result, 1e-14);
    }

    // ---- checkOrder / isMonotonic ----

    @Test
    public void testCheckOrder_strictIncreasing_returnsTrue() {
        assertTrue(MathArrays.isMonotonic(new double[]{1.0, 2.0, 3.0},
                MathArrays.OrderDirection.INCREASING, true));
    }

    @Test(expected = NonMonotonicSequenceException.class)
    public void testCheckOrder_nonMonotonic_throwsException() {
        MathArrays.checkOrder(new double[]{2.0, 1.0},
                MathArrays.OrderDirection.INCREASING, true);
    }

    // ---- convolve ----

    @Test
    public void testConvolve_returnsCorrectResult() {
        double[] x = {1.0, 2.0, 3.0};
        double[] h = {4.0, 5.0};
        double[] y = MathArrays.convolve(x, h);
        assertArrayEquals(new double[]{4.0, 13.0, 22.0, 15.0}, y, 1e-14);
    }

    @Test(expected = NullArgumentException.class)
    public void testConvolve_nullX_throwsException() {
        MathArrays.convolve(null, new double[]{1.0});
    }

    // ---- sortInPlace ----

    @Test(expected = NullArgumentException.class)
    public void testSortInPlace_nullX_throwsException() {
        MathArrays.sortInPlace(null);
    }

    // ========== NEW TESTS for uncovered areas ==========

    @Test
    public void testLinearCombinationArray_length0_returnsZero() {
        double[] a = {};
        double[] b = {};
        double result = MathArrays.linearCombination(a, b);
        assertEquals(0.0, result, 1e-14);
    }

    @Test
    public void testLinearCombination2Term_withNaN() {
        double result = MathArrays.linearCombination(Double.NaN, 1.0, 1.0, 1.0);
        assertTrue(Double.isNaN(result));
    }

    @Test
    public void testLinearCombination2Term_withInfinity() {
        double result = MathArrays.linearCombination(Double.POSITIVE_INFINITY, 1.0, 1.0, 1.0);
        assertEquals(Double.POSITIVE_INFINITY, result, 0.0);
    }

    @Test
    public void testEbeMultiply_normal() {
        double[] a = {1.0, 2.0, 3.0};
        double[] b = {4.0, 5.0, 6.0};
        double[] result = MathArrays.ebeMultiply(a, b);
        assertArrayEquals(new double[]{4.0, 10.0, 18.0}, result, 1e-14);
    }

    @Test(expected = DimensionMismatchException.class)
    public void testEbeMultiply_dimensionMismatch() {
        MathArrays.ebeMultiply(new double[]{1.0}, new double[]{2.0, 3.0});
    }

    @Test
    public void testEbeSubtract_normal() {
        double[] a = {5.0, 7.0, 9.0};
        double[] b = {1.0, 2.0, 3.0};
        double[] result = MathArrays.ebeSubtract(a, b);
        assertArrayEquals(new double[]{4.0, 5.0, 6.0}, result, 1e-14);
    }

    @Test
    public void testDistance_euclidean() {
        double[] a = {0.0, 0.0};
        double[] b = {3.0, 4.0};
        double d = MathArrays.distance(a, b);
        assertEquals(5.0, d, 1e-14);
    }

    @Test
    public void testDistance1() {
        double[] a = {0.0, 0.0};
        double[] b = {3.0, 4.0};
        double d = MathArrays.distance1(a, b);
        assertEquals(7.0, d, 1e-14);
    }

    @Test
    public void testDistanceInf() {
        double[] a = {1.0, -2.0};
        double[] b = {4.0, 1.0};
        double d = MathArrays.distanceInf(a, b);
        assertEquals(3.0, d, 1e-14);
    }

    @Test
    public void testEquals_withTolerance_equal() {
        double[] a = {1.0, 2.0};
        double[] b = {1.0000000001, 2.0000000001};
        assertTrue(MathArrays.equals(a, b, 1e-9));
    }

    @Test
    public void testScale() {
        double[] a = {1.0, 2.0, 3.0};
        double[] result = MathArrays.scale(2.0, a);
        assertArrayEquals(new double[]{2.0, 4.0, 6.0}, result, 1e-14);
    }

    @Test
    public void testCheckOrder_decreasingNotStrict_allowsEqual() {
        MathArrays.checkOrder(new double[]{3.0, 2.0, 2.0, 1.0},
                MathArrays.OrderDirection.DECREASING, false);
        // no exception expected
    }
}