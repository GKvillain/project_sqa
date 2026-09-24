package org.apache.commons.math.stat.inference;

import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.ChiSquaredDistributionImpl;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for ChiSquareTestImpl.
 */
public class ChiSquareTestImplTest {

    private ChiSquareTestImpl testStatistic;

    @Before
    public void setUp() {
        testStatistic = new ChiSquareTestImpl();
    }

    // Tests chiSquare computation with standard expected and observed arrays
    @Test
    public void testChiSquare_standardCounts_returnsCorrectStatistic() {
        double[] expected = new double[]{500, 500};
        long[] observed = new long[]{500, 500};
        assertEquals(0.0, testStatistic.chiSquare(expected, observed), 1E-10);

        double[] expected2 = new double[]{0.5, 0.5};
        long[] observed2 = new long[]{10, 10};
        assertEquals(0.0, testStatistic.chiSquare(expected2, observed2), 1E-10);
    }

    // Tests chiSquare where expected counts sum does not match observed counts sum (rescaling bug)
    @Test
    public void testChiSquare_unscaledExpected_rescalesAndComputesCorrectStatistic() {
        double[] expected = new double[]{1.0, 1.0};
        long[] observed = new long[]{20, 30};
        // Sum observed = 50, sum expected = 2. Rescaled expected: 25, 25.
        // ChiSquare = (20-25)^2 / 25 + (30-25)^2 / 25 = 25/25 + 25/25 = 2.0
        assertEquals(2.0, testStatistic.chiSquare(expected, observed), 1E-5);
    }

    // Tests chiSquare throws exception when array length is less than 2
    @Test(expected = IllegalArgumentException.class)
    public void testChiSquare_lengthLessThanTwo_throwsIllegalArgumentException() {
        double[] expected = new double[]{10.0};
        long[] observed = new long[]{10};
        testStatistic.chiSquare(expected, observed);
    }

    // Tests chiSquare throws exception when array lengths do not match
    @Test(expected = IllegalArgumentException.class)
    public void testChiSquare_mismatchedLengths_throwsIllegalArgumentException() {
        double[] expected = new double[]{10.0, 20.0, 30.0};
        long[] observed = new long[]{10, 20};
        testStatistic.chiSquare(expected, observed);
    }

    // Tests chiSquare throws exception when expected has zero or negative values
    @Test(expected = IllegalArgumentException.class)
    public void testChiSquare_nonPositiveExpected_throwsIllegalArgumentException() {
        double[] expected = new double[]{10.0, 0.0};
        long[] observed = new long[]{10, 20};
        testStatistic.chiSquare(expected, observed);
    }

    // Tests chiSquare throws exception when observed has negative values
    @Test(expected = IllegalArgumentException.class)
    public void testChiSquare_negativeObserved_throwsIllegalArgumentException() {
        double[] expected = new double[]{10.0, 20.0};
        long[] observed = new long[]{10, -5};
        testStatistic.chiSquare(expected, observed);
    }

    // Tests chiSquareTest p-value computation
    @Test
    public void testChiSquareTest_validInputs_returnsCorrectPValue() throws MathException {
        double[] expected = new double[]{25.0, 25.0, 25.0, 25.0};
        long[] observed = new long[]{20, 25, 25, 30};
        // chiSquare = 25/25 + 0 + 0 + 25/25 = 2.0, df = 3
        double pValue = testStatistic.chiSquareTest(expected, observed);
        assertTrue(pValue > 0.0 && pValue < 1.0);
    }

    // Tests chiSquareTest with alpha decision
    @Test
    public void testChiSquareTest_withAlpha_returnsCorrectDecision() throws MathException {
        double[] expected = new double[]{50.0, 50.0};
        long[] observed = new long[]{50, 50};
        assertFalse(testStatistic.chiSquareTest(expected, observed, 0.05));

        long[] skewedObserved = new long[]{0, 100};
        assertTrue(testStatistic.chiSquareTest(expected, skewedObserved, 0.05));
    }

    // Tests chiSquareTest throws exception for invalid alpha
    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareTest_invalidAlpha_throwsIllegalArgumentException() throws MathException {
        double[] expected = new double[]{50.0, 50.0};
        long[] observed = new long[]{50, 50};
        testStatistic.chiSquareTest(expected, observed, 0.0);
    }

    // Tests 2-way table chiSquare statistic calculation
    @Test
    public void testChiSquare2D_validTable_returnsCorrectStatistic() {
        long[][] counts = new long[][]{
            {40, 60},
            {60, 40}
        };
        double stat = testStatistic.chiSquare(counts);
        assertEquals(8.0, stat, 1E-5);
    }

    // Tests 2-way table chiSquareTest p-value and significance test
    @Test
    public void testChiSquareTest2D_validTable_returnsCorrectResults() throws MathException {
        long[][] counts = new long[][]{
            {40, 60},
            {60, 40}
        };
        double pValue = testStatistic.chiSquareTest(counts);
        assertTrue(pValue < 0.01);
        assertTrue(testStatistic.chiSquareTest(counts, 0.05));
    }

    // Tests 2-way table invalid dimensions exception (rows < 2)
    @Test(expected = IllegalArgumentException.class)
    public void testChiSquare2D_insufficientRows_throwsIllegalArgumentException() {
        long[][] counts = new long[][]{
            {40, 60}
        };
        testStatistic.chiSquare(counts);
    }

    // Tests 2-way table invalid dimensions exception (cols < 2)
    @Test(expected = IllegalArgumentException.class)
    public void testChiSquare2D_insufficientCols_throwsIllegalArgumentException() {
        long[][] counts = new long[][]{
            {40},
            {60}
        };
        testStatistic.chiSquare(counts);
    }

    // Tests 2-way table non-rectangular exception
    @Test(expected = IllegalArgumentException.class)
    public void testChiSquare2D_nonRectangular_throwsIllegalArgumentException() {
        long[][] counts = new long[][]{
            {40, 60},
            {60, 40, 10}
        };
        testStatistic.chiSquare(counts);
    }

    // Tests 2-way table negative entry exception
    @Test(expected = IllegalArgumentException.class)
    public void testChiSquare2D_negativeEntry_throwsIllegalArgumentException() {
        long[][] counts = new long[][]{
            {40, -60},
            {60, 40}
        };
        testStatistic.chiSquare(counts);
    }

    // Tests datasets comparison with equal counts sum
    @Test
    public void testChiSquareDataSetsComparison_equalCounts_returnsCorrectStatistic() {
        long[] observed1 = new long[]{10, 20, 30};
        long[] observed2 = new long[]{10, 20, 30};
        assertEquals(0.0, testStatistic.chiSquareDataSetsComparison(observed1, observed2), 1E-10);
    }

    // Tests datasets comparison with unequal counts sum
    @Test
    public void testChiSquareDataSetsComparison_unequalCounts_returnsCorrectStatistic() throws MathException {
        long[] observed1 = new long[]{10, 20, 30};
        long[] observed2 = new long[]{20, 40, 60};
        assertEquals(0.0, testStatistic.chiSquareDataSetsComparison(observed1, observed2), 1E-10);
        assertEquals(1.0, testStatistic.chiSquareTestDataSetsComparison(observed1, observed2), 1E-5);
        assertFalse(testStatistic.chiSquareTestDataSetsComparison(observed1, observed2, 0.05));
    }

    // Tests datasets comparison throws exception when sum of observed counts is zero
    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareDataSetsComparison_allZeros_throwsIllegalArgumentException() {
        long[] observed1 = new long[]{0, 0};
        long[] observed2 = new long[]{10, 20};
        testStatistic.chiSquareDataSetsComparison(observed1, observed2);
    }

    // Tests datasets comparison throws exception when a cell is zero in both datasets
    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareDataSetsComparison_bothZeroEntry_throwsIllegalArgumentException() {
        long[] observed1 = new long[]{0, 20};
        long[] observed2 = new long[]{0, 30};
        testStatistic.chiSquareDataSetsComparison(observed1, observed2);
    }

    // Tests constructor and custom distribution injection
    @Test
    public void testSetDistribution_customDistribution_setsCorrectly() {
        ChiSquaredDistributionImpl customDist = new ChiSquaredDistributionImpl(2.0);
        ChiSquareTestImpl customTest = new ChiSquareTestImpl(customDist);
        customTest.setDistribution(customDist);
    }
}