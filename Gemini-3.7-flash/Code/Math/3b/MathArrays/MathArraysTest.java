package org.apache.commons.math3.util;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.NoDataException;
import org.apache.commons.math3.exception.NonMonotonicSequenceException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MathArraysTest {

    // Tests linearCombination with 1-element arrays (Defects4J Math-3 defect detection)
    @Test
    public void testLinearCombination_singleElementArrays_returnsProduct() {
        double[] a = new double[] { 2.5 };
        double[] b = new double[] { 4.0 };
        double result = MathArrays.linearCombination(a, b);
        assertEquals(10.0, result, 1e-15);
    }

    // Tests linearCombination with multiple element arrays
    @Test
    public void testLinearCombination_arrayInput_returnsAccurateSum() {
        double[] a = new double[] { 1.0, 2.0, 3.0 };
        double[] b = new double[] { 4.0, 5.0, 6.0 };
        double expected = 1.0 * 4.0 + 2.0 * 5.0 + 3.0 * 6.0;
        assertEquals(expected, MathArrays.linearCombination(a, b), 1e-15);
    }

    // Tests linearCombination with mismatched array lengths throwing DimensionMismatchException
    @Test(expected = DimensionMismatchException.class)
    public void testLinearCombination_mismatchedDimensions_throwsDimensionMismatchException() {
        double[] a = new double[] { 1.0, 2.0 };
        double[] b = new double[] { 1.0 };
        MathArrays.linearCombination(a, b);
    }

    // Tests 2-term scalar linearCombination
    @Test
    public void testLinearCombination_twoScalarPairs_returnsCorrectResult() {
        double result = MathArrays.linearCombination(1.5, 2.0, 3.0, 4.0);
        assertEquals(15.0, result, 1e-15);
    }

    // Tests 3-term scalar linearCombination
    @Test
    public void testLinearCombination_threeScalarPairs_returnsCorrectResult() {
        double result = MathArrays.linearCombination(1.0, 2.0, 3.0, 4.0, 5.0, 6.0);
        assertEquals(44.0, result, 1e-15);
    }

    // Tests 4-term scalar linearCombination
    @Test
    public void testLinearCombination_fourScalarPairs_returnsCorrectResult() {
        double result = MathArrays.linearCombination(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0);
        assertEquals(100.0, result, 1e-15);
    }

    // Tests scaling an array to a new array
    @Test
    public void testScale_validArray_returnsScaledCopy() {
        double[] original = new double[] { 1.0, -2.0, 3.0 };
        double[] scaled = MathArrays.scale(2.5, original);
        assertArrayEquals(new double[] { 2.5, -5.0, 7.5 }, scaled, 1e-15);
        assertEquals(1.0, original[0], 1e-15);
    }

    // Tests scaling an array in place
    @Test
    public void testScaleInPlace_validArray_modifiesOriginal() {
        double[] arr = new double[] { 1.0, 2.0, -3.0 };
        MathArrays.scaleInPlace(3.0, arr);
        assertArrayEquals(new double[] { 3.0, 6.0, -9.0 }, arr, 1e-15);
    }

    // Tests element-by-element addition, subtraction, multiplication, and division
    @Test
    public void testEbeOperations_validArrays_returnsCorrectResults() {
        double[] a = new double[] { 6.0, 8.0 };
        double[] b = new double[] { 2.0, 4.0 };

        assertArrayEquals(new double[] { 8.0, 12.0 }, MathArrays.ebeAdd(a, b), 1e-15);
        assertArrayEquals(new double[] { 4.0, 4.0 }, MathArrays.ebeSubtract(a, b), 1e-15);
        assertArrayEquals(new double[] { 12.0, 32.0 }, MathArrays.ebeMultiply(a, b), 1e-15);
        assertArrayEquals(new double[] { 3.0, 2.0 }, MathArrays.ebeDivide(a, b), 1e-15);
    }

    // Tests ebeAdd with mismatched lengths
    @Test(expected = DimensionMismatchException.class)
    public void testEbeAdd_mismatchedDimensions_throwsDimensionMismatchException() {
        MathArrays.ebeAdd(new double[] { 1.0 }, new double[] { 1.0, 2.0 });
    }

    // Tests distance metrics calculations (L1, L2, LInf)
    @Test
    public void testDistances_validPoints_returnsCorrectDistances() {
        double[] p1 = new double[] { 1.0, 2.0, 3.0 };
        double[] p2 = new double[] { 4.0, 6.0, 3.0 };

        assertEquals(7.0, MathArrays.distance1(p1, p2), 1e-15);
        assertEquals(5.0, MathArrays.distance(p1, p2), 1e-15);
        assertEquals(4.0, MathArrays.distanceInf(p1, p2), 1e-15);

        int[] ip1 = new int[] { 1, 2, 3 };
        int[] ip2 = new int[] { 4, 6, 3 };
        assertEquals(7, MathArrays.distance1(ip1, ip2));
        assertEquals(5.0, MathArrays.distance(ip1, ip2), 1e-15);
        assertEquals(4, MathArrays.distanceInf(ip1, ip2));
    }

    // Tests monotonic order verification
    @Test
    public void testIsMonotonic_variousSequences_returnsExpectedBoolean() {
        double[] inc = new double[] { 1.0, 2.0, 3.0 };
        double[] nonStrictInc = new double[] { 1.0, 2.0, 2.0, 3.0 };
        double[] dec = new double[] { 3.0, 2.0, 1.0 };

        assertTrue(MathArrays.isMonotonic(inc, MathArrays.OrderDirection.INCREASING, true));
        assertFalse(MathArrays.isMonotonic(nonStrictInc, MathArrays.OrderDirection.INCREASING, true));
        assertTrue(MathArrays.isMonotonic(nonStrictInc, MathArrays.OrderDirection.INCREASING, false));
        assertTrue(MathArrays.isMonotonic(dec, MathArrays.OrderDirection.DECREASING, true));

        String[] strInc = new String[] { "a", "b", "c" };
        assertTrue(MathArrays.isMonotonic(strInc, MathArrays.OrderDirection.INCREASING, true));
    }

    // Tests checkOrder with non-monotonic input expecting exception
    @Test(expected = NonMonotonicSequenceException.class)
    public void testCheckOrder_nonMonotonicArray_throwsNonMonotonicSequenceException() {
        MathArrays.checkOrder(new double[] { 1.0, 3.0, 2.0 });
    }

    // Tests safeNorm to prevent underflow and overflow
    @Test
    public void testSafeNorm_largeAndSmallValues_returnsAccurateNorm() {
        double[] vLarge = new double[] { 1e100, 1e100 };
        assertEquals(Math.sqrt(2.0) * 1e100, MathArrays.safeNorm(vLarge), 1e90);

        double[] vSmall = new double[] { 1e-100, 1e-100 };
        assertEquals(Math.sqrt(2.0) * 1e-100, MathArrays.safeNorm(vSmall), 1e-110);
    }

    // Tests sortInPlace on multiple arrays
    @Test
    public void testSortInPlace_multipleAssociatedArrays_sortsAllInPlace() {
        double[] x = new double[] { 3.0, 1.0, 2.0 };
        double[] y = new double[] { 30.0, 10.0, 20.0 };
        double[] z = new double[] { 300.0, 100.0, 200.0 };

        MathArrays.sortInPlace(x, y, z);

        assertArrayEquals(new double[] { 1.0, 2.0, 3.0 }, x, 1e-15);
        assertArrayEquals(new double[] { 10.0, 20.0, 30.0 }, y, 1e-15);
        assertArrayEquals(new double[] { 100.0, 200.0, 300.0 }, z, 1e-15);
    }

    // Tests sortInPlace throwing NullArgumentException on null input
    @Test(expected = NullArgumentException.class)
    public void testSortInPlace_nullMainArray_throwsNullArgumentException() {
        MathArrays.sortInPlace(null, new double[] { 1.0 });
    }

    // Tests normalizeArray functionality and exception paths
    @Test
    public void testNormalizeArray_validInput_returnsNormalizedArray() {
        double[] values = new double[] { 1.0, 2.0, 3.0, 4.0 };
        double[] normalized = MathArrays.normalizeArray(values, 100.0);
        assertArrayEquals(new double[] { 10.0, 20.0, 30.0, 40.0 }, normalized, 1e-15);
    }

    // Tests normalizeArray with array summing to zero throwing MathArithmeticException
    @Test(expected = MathArithmeticException.class)
    public void testNormalizeArray_sumToZero_throwsMathArithmeticException() {
        MathArrays.normalizeArray(new double[] { -1.0, 1.0 }, 10.0);
    }

    // Tests normalizeArray with infinite target throwing MathIllegalArgumentException
    @Test(expected = MathIllegalArgumentException.class)
    public void testNormalizeArray_infiniteTarget_throwsMathIllegalArgumentException() {
        MathArrays.normalizeArray(new double[] { 1.0, 2.0 }, Double.POSITIVE_INFINITY);
    }

    // Tests convolution between two sequences
    @Test
    public void testConvolve_validInputs_returnsConvolution() {
        double[] x = new double[] { 1.0, 2.0, 3.0 };
        double[] h = new double[] { 0.5, 1.0 };
        double[] expected = new double[] { 0.5, 2.0, 3.5, 3.0 };
        assertArrayEquals(expected, MathArrays.convolve(x, h), 1e-15);
    }

    // Tests convolve with empty array throwing NoDataException
    @Test(expected = NoDataException.class)
    public void testConvolve_emptyArray_throwsNoDataException() {
        MathArrays.convolve(new double[0], new double[] { 1.0 });
    }

    // Tests array equality check
    @Test
    public void testEquals_variousFloatAndDoubleArrays_returnsExpected() {
        double[] d1 = new double[] { 1.0, Double.NaN };
        double[] d2 = new double[] { 1.0, Double.NaN };

        assertFalse(MathArrays.equals(d1, d2));
        assertTrue(MathArrays.equalsIncludingNaN(d1, d2));

        float[] f1 = new float[] { 1.0f, Float.NaN };
        float[] f2 = new float[] { 1.0f, Float.NaN };

        assertFalse(MathArrays.equals(f1, f2));
        assertTrue(MathArrays.equalsIncludingNaN(f1, f2));
    }
}