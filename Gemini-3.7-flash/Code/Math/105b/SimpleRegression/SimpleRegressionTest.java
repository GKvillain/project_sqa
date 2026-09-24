package org.apache.commons.math.stat.regression;

import org.apache.commons.math.MathException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SimpleRegressionTest {

    private SimpleRegression regression;

    @Before
    public void setUp() {
        regression = new SimpleRegression();
    }

    // Tests that getSumSquaredErrors is always non-negative even with rounding artifacts (Defects4J Math-105)
    @Test
    public void testGetSumSquaredErrors_perfectLinearData_returnsNonNegative() {
        double[][] data = {
            { 1.0, 1.0 },
            { 2.0, 2.0 },
            { 3.0, 3.0 },
            { 4.0, 4.0 },
            { 5.0, 5.0 }
        };
        regression.addData(data);
        double sse = regression.getSumSquaredErrors();
        assertTrue("SSE must be non-negative", sse >= 0.0);
    }

    // Tests addData using 2D array and getN
    @Test
    public void testAddData_arrayInput_updatesNCorrectly() {
        double[][] data = { { 1.0, 2.0 }, { 2.0, 3.0 }, { 3.0, 4.0 } };
        regression.addData(data);
        assertEquals(3L, regression.getN());
    }

    // Tests clear method resetting all internal statistics
    @Test
    public void testClear_populatedModel_resetsAllData() {
        regression.addData(1.0, 2.0);
        regression.addData(2.0, 4.0);
        regression.clear();

        assertEquals(0L, regression.getN());
        assertTrue(Double.isNaN(regression.getSlope()));
        assertTrue(Double.isNaN(regression.getIntercept()));
    }

    // Tests getSlope with fewer than 2 data points returns NaN
    @Test
    public void testGetSlope_insufficientData_returnsNaN() {
        assertTrue(Double.isNaN(regression.getSlope()));
        regression.addData(1.0, 2.0);
        assertTrue(Double.isNaN(regression.getSlope()));
    }

    // Tests getSlope when there is no variation in x values returns NaN
    @Test
    public void testGetSlope_noVariationInX_returnsNaN() {
        regression.addData(2.0, 1.0);
        regression.addData(2.0, 5.0);
        regression.addData(2.0, 9.0);
        assertTrue(Double.isNaN(regression.getSlope()));
    }

    // Tests slope, intercept, and predict computation for standard linear data
    @Test
    public void testPredict_validData_returnsExpectedPrediction() {
        regression.addData(1.0, 3.0);
        regression.addData(2.0, 5.0);
        regression.addData(3.0, 7.0);

        assertEquals(2.0, regression.getSlope(), 1e-10);
        assertEquals(1.0, regression.getIntercept(), 1e-10);
        assertEquals(9.0, regression.predict(4.0), 1e-10);
    }

    // Tests getTotalSumSquares returns NaN when n < 2
    @Test
    public void testGetTotalSumSquares_insufficientData_returnsNaN() {
        assertTrue(Double.isNaN(regression.getTotalSumSquares()));
        regression.addData(1.0, 2.0);
        assertTrue(Double.isNaN(regression.getTotalSumSquares()));
    }

    // Tests getTotalSumSquares and getRegressionSumSquares with valid data
    @Test
    public void testGetTotalSumSquares_validData_returnsCorrectSums() {
        regression.addData(1.0, 2.0);
        regression.addData(2.0, 4.0);
        regression.addData(3.0, 5.0);

        assertEquals(4.666666666666667, regression.getTotalSumSquares(), 1e-10);
        assertEquals(4.5, regression.getRegressionSumSquares(), 1e-10);
    }

    // Tests getRSquare and getR with positive correlation
    @Test
    public void testGetR_positiveSlope_returnsPositiveR() {
        regression.addData(1.0, 2.0);
        regression.addData(2.0, 4.0);
        regression.addData(3.0, 6.0);

        assertEquals(1.0, regression.getRSquare(), 1e-10);
        assertEquals(1.0, regression.getR(), 1e-10);
    }

    // Tests getR with negative slope returns negative R value
    @Test
    public void testGetR_negativeSlope_returnsNegativeR() {
        regression.addData(1.0, 6.0);
        regression.addData(2.0, 4.0);
        regression.addData(3.0, 2.0);

        assertTrue(regression.getSlope() < 0);
        assertEquals(-1.0, regression.getR(), 1e-10);
    }

    // Tests getMeanSquareError with fewer than 3 points returns NaN
    @Test
    public void testGetMeanSquareError_lessThanThreeObservations_returnsNaN() {
        regression.addData(1.0, 2.0);
        regression.addData(2.0, 4.0);
        assertTrue(Double.isNaN(regression.getMeanSquareError()));
    }

    // Tests standard error calculations for intercept and slope
    @Test
    public void testGetStdErrors_validData_returnsCorrectErrors() {
        regression.addData(1.0, 2.0);
        regression.addData(2.0, 3.0);
        regression.addData(3.0, 5.0);
        regression.addData(4.0, 4.0);
        regression.addData(5.0, 6.0);

        assertFalse(Double.isNaN(regression.getInterceptStdErr()));
        assertFalse(Double.isNaN(regression.getSlopeStdErr()));
        assertTrue(regression.getInterceptStdErr() > 0.0);
        assertTrue(regression.getSlopeStdErr() > 0.0);
    }

    // Tests getSlopeConfidenceInterval default 95% interval
    @Test
    public void testGetSlopeConfidenceInterval_defaultAlpha_returnsPositiveHalfWidth() throws MathException {
        regression.addData(1.0, 2.0);
        regression.addData(2.0, 3.0);
        regression.addData(3.0, 5.0);
        regression.addData(4.0, 4.0);
        regression.addData(5.0, 6.0);

        double halfWidth = regression.getSlopeConfidenceInterval();
        assertTrue(halfWidth > 0.0);
        assertFalse(Double.isNaN(halfWidth));
    }

    // Tests getSlopeConfidenceInterval with alpha <= 0 throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testGetSlopeConfidenceInterval_alphaZeroOrNegative_throwsIllegalArgumentException() throws MathException {
        regression.addData(1.0, 2.0);
        regression.addData(2.0, 3.0);
        regression.addData(3.0, 4.0);
        regression.getSlopeConfidenceInterval(0.0);
    }

    // Tests getSlopeConfidenceInterval with alpha >= 1 throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testGetSlopeConfidenceInterval_alphaGreaterOrEqualToOne_throwsIllegalArgumentException() throws MathException {
        regression.addData(1.0, 2.0);
        regression.addData(2.0, 3.0);
        regression.addData(3.0, 4.0);
        regression.getSlopeConfidenceInterval(1.0);
    }

    // Tests getSignificance calculation
    @Test
    public void testGetSignificance_validData_returnsPValue() throws MathException {
        regression.addData(1.0, 2.0);
        regression.addData(2.0, 4.0);
        regression.addData(3.0, 5.0);
        regression.addData(4.0, 4.0);
        regression.addData(5.0, 6.0);

        double pVal = regression.getSignificance();
        assertTrue(pVal >= 0.0 && pVal <= 1.0);
    }
}