package org.apache.commons.math3.distribution;

import org.junit.Test;
import static org.junit.Assert.*;
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.NumberIsTooLargeException;

public class HypergeometricDistributionTest {

    // Tests constructor with populationSize <= 0
    @Test(expected = NotStrictlyPositiveException.class)
    public void testConstructor_zeroPopulationSize_throwsNotStrictlyPositiveException() {
        new HypergeometricDistribution(0, 1, 1);
    }

    // Tests constructor with negative numberOfSuccesses
    @Test(expected = NotPositiveException.class)
    public void testConstructor_negativeNumberOfSuccesses_throwsNotPositiveException() {
        new HypergeometricDistribution(10, -1, 5);
    }

    // Tests constructor with negative sampleSize
    @Test(expected = NotPositiveException.class)
    public void testConstructor_negativeSampleSize_throwsNotPositiveException() {
        new HypergeometricDistribution(10, 5, -1);
    }

    // Tests constructor when numberOfSuccesses > populationSize
    @Test(expected = NumberIsTooLargeException.class)
    public void testConstructor_numberOfSuccessesExceedsPopulation_throwsNumberIsTooLargeException() {
        new HypergeometricDistribution(10, 15, 5);
    }

    // Tests constructor when sampleSize > populationSize
    @Test(expected = NumberIsTooLargeException.class)
    public void testConstructor_sampleSizeExceedsPopulation_throwsNumberIsTooLargeException() {
        new HypergeometricDistribution(10, 5, 15);
    }

    // Tests constructor with valid parameters and getters
    @Test
    public void testConstructor_validParameters_createsDistribution() {
        HypergeometricDistribution dist = new HypergeometricDistribution(50, 10, 5);
        assertEquals(50, dist.getPopulationSize());
        assertEquals(10, dist.getNumberOfSuccesses());
        assertEquals(5, dist.getSampleSize());
    }

    // Tests probability for x outside support (should return 0)
    @Test
    public void testProbability_outsideSupport_returnsZero() {
        HypergeometricDistribution dist = new HypergeometricDistribution(50, 10, 5);
        assertEquals(0.0, dist.probability(-1), 0.0);
        assertEquals(0.0, dist.probability(11), 0.0); // upper bound is min(10,5)=5
        assertEquals(0.0, dist.probability(6), 0.0);
    }

    // Tests probability for x within support returns positive value
    @Test
    public void testProbability_withinSupport_returnsPositive() {
        HypergeometricDistribution dist = new HypergeometricDistribution(50, 10, 5);
        double p = dist.probability(2);
        assertTrue(p > 0.0);
    }

    // Tests sum of probabilities over full support equals 1
    @Test
    public void testProbability_sumOverSupport_equalsOne() {
        HypergeometricDistribution dist = new HypergeometricDistribution(50, 10, 5);
        int lower = dist.getSupportLowerBound();
        int upper = dist.getSupportUpperBound();
        double sum = 0.0;
        for (int x = lower; x <= upper; x++) {
            sum += dist.probability(x);
        }
        assertEquals(1.0, sum, 1e-12);
    }

    // Tests cumulativeProbability for x < lower bound returns 0
    @Test
    public void testCumulativeProbability_belowLowerBound_returnsZero() {
        HypergeometricDistribution dist = new HypergeometricDistribution(50, 10, 5);
        assertEquals(0.0, dist.cumulativeProbability(-1), 0.0);
    }

    // Tests cumulativeProbability for x >= upper bound returns 1
    @Test
    public void testCumulativeProbability_aboveUpperBound_returnsOne() {
        HypergeometricDistribution dist = new HypergeometricDistribution(50, 10, 5);
        assertEquals(1.0, dist.cumulativeProbability(10), 0.0);
    }

    // Tests cumulativeProbability at lower bound returns probability at lower bound
    @Test
    public void testCumulativeProbability_atLowerBound_returnsCorrect() {
        HypergeometricDistribution dist = new HypergeometricDistribution(50, 10, 5);
        int lower = dist.getSupportLowerBound();
        double expected = dist.probability(lower);
        assertEquals(expected, dist.cumulativeProbability(lower), 1e-12);
    }

    // Tests cumulativeProbability at upper bound returns 1
    @Test
    public void testCumulativeProbability_atUpperBound_returnsOne() {
        HypergeometricDistribution dist = new HypergeometricDistribution(50, 10, 5);
        int upper = dist.getSupportUpperBound();
        assertEquals(1.0, dist.cumulativeProbability(upper), 1e-12);
    }

    // Tests cumulativeProbability monotonic non-decreasing
    @Test
    public void testCumulativeProbability_increasesMonotonically() {
        HypergeometricDistribution dist = new HypergeometricDistribution(100, 30, 10);
        double prev = 0.0;
        int lower = dist.getSupportLowerBound();
        int upper = dist.getSupportUpperBound();
        for (int x = lower; x <= upper; x++) {
            double cur = dist.cumulativeProbability(x);
            assertTrue(cur >= prev - 1e-12);
            prev = cur;
        }
    }

    // Tests upperCumulativeProbability for x <= lower bound returns 1
    @Test
    public void testUpperCumulativeProbability_belowLowerBound_returnsOne() {
        HypergeometricDistribution dist = new HypergeometricDistribution(50, 10, 5);
        assertEquals(1.0, dist.upperCumulativeProbability(-1), 0.0);
    }

    // Tests upperCumulativeProbability for x > upper bound returns 0
    @Test
    public void testUpperCumulativeProbability_aboveUpperBound_returnsZero() {
        HypergeometricDistribution dist = new HypergeometricDistribution(50, 10, 5);
        assertEquals(0.0, dist.upperCumulativeProbability(6), 0.0);
    }

    // Tests upperCumulativeProbability at lower bound returns 1
    @Test
    public void testUpperCumulativeProbability_atLowerBound_returnsOne() {
        HypergeometricDistribution dist = new HypergeometricDistribution(50, 10, 5);
        int lower = dist.getSupportLowerBound();
        assertEquals(1.0, dist.upperCumulativeProbability(lower), 1e-12);
    }

    // Tests upperCumulativeProbability at upper bound returns probability at upper bound
    @Test
    public void testUpperCumulativeProbability_atUpperBound_returnsCorrect() {
        HypergeometricDistribution dist = new HypergeometricDistribution(50, 10, 5);
        int upper = dist.getSupportUpperBound();
        double expected = dist.probability(upper);
        assertEquals(expected, dist.upperCumulativeProbability(upper), 1e-12);
    }

    // Tests getNumericalMean with known values
    @Test
    public void testGetNumericalMean_knownValues() {
        HypergeometricDistribution dist = new HypergeometricDistribution(50, 10, 5);
        double expected = (5.0 * 10.0) / 50.0; // 1.0
        assertEquals(expected, dist.getNumericalMean(), 1e-12);
    }

    // Tests getNumericalVariance with known values
    @Test
    public void testGetNumericalVariance_knownValues() {
        HypergeometricDistribution dist = new HypergeometricDistribution(50, 10, 5);
        double N = 50.0, m = 10.0, n = 5.0;
        double expected = (n * m * (N - n) * (N - m)) / (N * N * (N - 1));
        assertEquals(expected, dist.getNumericalVariance(), 1e-12);
    }

    // Tests getSupportLowerBound
    @Test
    public void testGetSupportLowerBound_returnsCorrect() {
        HypergeometricDistribution dist = new HypergeometricDistribution(50, 10, 5);
        assertEquals(Math.max(0, 5 + 10 - 50), dist.getSupportLowerBound());
    }

    // Tests getSupportUpperBound
    @Test
    public void testGetSupportUpperBound_returnsCorrect() {
        HypergeometricDistribution dist = new HypergeometricDistribution(50, 10, 5);
        assertEquals(Math.min(5, 10), dist.getSupportUpperBound());
    }

    // Tests isSupportConnected returns true
    @Test
    public void testIsSupportConnected_returnsTrue() {
        HypergeometricDistribution dist = new HypergeometricDistribution(50, 10, 5);
        assertTrue(dist.isSupportConnected());
    }

    // Edge case: populationSize = 1, successes = 1, sampleSize = 1
    @Test
    public void testEdgeCase_singleElement_distribution() {
        HypergeometricDistribution dist = new HypergeometricDistribution(1, 1, 1);
        assertEquals(0, dist.getSupportLowerBound());
        assertEquals(1, dist.getSupportUpperBound());
        assertEquals(1.0, dist.probability(1), 1e-12);
        double sum = 0;
        for (int x = 0; x <= 1; x++) {
            sum += dist.probability(x);
        }
        assertEquals(1.0, sum, 1e-12);
        assertEquals(1.0, dist.cumulativeProbability(1), 1e-12);
    }

    // Edge case: sampleSize = 0, successes = 0
    @Test
    public void testEdgeCase_zeroSampleSize() {
        HypergeometricDistribution dist = new HypergeometricDistribution(10, 0, 0);
        assertEquals(0, dist.getSupportLowerBound());
        assertEquals(0, dist.getSupportUpperBound());
        assertEquals(1.0, dist.probability(0), 1e-12);
        assertEquals(1.0, dist.cumulativeProbability(0), 1e-12);
    }

    // Edge case: sampleSize = populationSize (p = 1)
    @Test
    public void testEdgeCase_fullSample() {
        HypergeometricDistribution dist = new HypergeometricDistribution(10, 5, 10);
        // Support: max(0, 10+5-10)=5, min(10,5)=5 => only x=5
        assertEquals(5, dist.getSupportLowerBound());
        assertEquals(5, dist.getSupportUpperBound());
        assertEquals(1.0, dist.probability(5), 1e-12);
        assertEquals(1.0, dist.cumulativeProbability(5), 1e-12);
    }
}