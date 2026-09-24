package org.apache.commons.math3.distribution;

import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.NumberIsTooLargeException;
import org.apache.commons.math3.random.Well19937c;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HypergeometricDistributionTest {

    private static final double DEFAULT_TOLERANCE = 1e-12;

    // Tests exception when population size is zero
    @Test(expected = NotStrictlyPositiveException.class)
    public void testConstructor_populationSizeZero_throwsNotStrictlyPositiveException() {
        new HypergeometricDistribution(0, 0, 0);
    }

    // Tests exception when population size is negative
    @Test(expected = NotStrictlyPositiveException.class)
    public void testConstructor_populationSizeNegative_throwsNotStrictlyPositiveException() {
        new HypergeometricDistribution(-10, 5, 5);
    }

    // Tests exception when number of successes is negative
    @Test(expected = NotPositiveException.class)
    public void testConstructor_numberOfSuccessesNegative_throwsNotPositiveException() {
        new HypergeometricDistribution(10, -1, 5);
    }

    // Tests exception when sample size is negative
    @Test(expected = NotPositiveException.class)
    public void testConstructor_sampleSizeNegative_throwsNotPositiveException() {
        new HypergeometricDistribution(10, 5, -1);
    }

    // Tests exception when number of successes exceeds population size
    @Test(expected = NumberIsTooLargeException.class)
    public void testConstructor_numberOfSuccessesLargerThanPopulation_throwsNumberIsTooLargeException() {
        new HypergeometricDistribution(10, 11, 5);
    }

    // Tests exception when sample size exceeds population size
    @Test(expected = NumberIsTooLargeException.class)
    public void testConstructor_sampleSizeLargerThanPopulation_throwsNumberIsTooLargeException() {
        new HypergeometricDistribution(10, 5, 11);
    }

    // Tests getters with valid constructor parameters
    @Test
    public void testConstructor_validParameters_gettersReturnCorrectValues() {
        HypergeometricDistribution dist = new HypergeometricDistribution(100, 30, 20);
        assertEquals(100, dist.getPopulationSize());
        assertEquals(30, dist.getNumberOfSuccesses());
        assertEquals(20, dist.getSampleSize());
        assertTrue(dist.isSupportConnected());
    }

    // Tests custom random generator constructor
    @Test
    public void testConstructor_withRandomGenerator_success() {
        HypergeometricDistribution dist = new HypergeometricDistribution(new Well19937c(123456L), 50, 10, 5);
        assertEquals(50, dist.getPopulationSize());
        assertEquals(10, dist.getNumberOfSuccesses());
        assertEquals(5, dist.getSampleSize());
    }

    // Tests probability for values outside support domain
    @Test
    public void testProbability_outOfDomain_returnsZero() {
        HypergeometricDistribution dist = new HypergeometricDistribution(10, 5, 5);
        assertEquals(0.0, dist.probability(-1), DEFAULT_TOLERANCE);
        assertEquals(0.0, dist.probability(6), DEFAULT_TOLERANCE);
    }

    // Tests probability for valid values in domain
    @Test
    public void testProbability_validValue_returnsCorrectProbability() {
        HypergeometricDistribution dist = new HypergeometricDistribution(10, 5, 5);
        // P(X = 2) = (5C2 * 5C3) / 10C5 = (10 * 10) / 252 = 100 / 252
        assertEquals(100.0 / 252.0, dist.probability(2), DEFAULT_TOLERANCE);
        // P(X = 0) = (5C0 * 5C5) / 10C5 = (1 * 1) / 252 = 1 / 252
        assertEquals(1.0 / 252.0, dist.probability(0), DEFAULT_TOLERANCE);
    }

    // Tests cumulative probability below lower domain boundary
    @Test
    public void testCumulativeProbability_belowLowerBound_returnsZero() {
        HypergeometricDistribution dist = new HypergeometricDistribution(10, 5, 5);
        assertEquals(0.0, dist.cumulativeProbability(-1), DEFAULT_TOLERANCE);
    }

    // Tests cumulative probability at and above upper domain boundary
    @Test
    public void testCumulativeProbability_atOrAboveUpperBound_returnsOne() {
        HypergeometricDistribution dist = new HypergeometricDistribution(10, 5, 5);
        assertEquals(1.0, dist.cumulativeProbability(5), DEFAULT_TOLERANCE);
        assertEquals(1.0, dist.cumulativeProbability(6), DEFAULT_TOLERANCE);
    }

    // Tests cumulative probability within domain
    @Test
    public void testCumulativeProbability_withinDomain_returnsCorrectValue() {
        HypergeometricDistribution dist = new HypergeometricDistribution(10, 5, 5);
        // P(X <= 2) = (1 + 25 + 100) / 252 = 126 / 252 = 0.5
        assertEquals(0.5, dist.cumulativeProbability(2), DEFAULT_TOLERANCE);
    }

    // Tests upper cumulative probability at or below lower domain boundary
    @Test
    public void testUpperCumulativeProbability_atOrBelowLowerBound_returnsOne() {
        HypergeometricDistribution dist = new HypergeometricDistribution(10, 5, 5);
        assertEquals(1.0, dist.upperCumulativeProbability(0), DEFAULT_TOLERANCE);
        assertEquals(1.0, dist.upperCumulativeProbability(-1), DEFAULT_TOLERANCE);
    }

    // Tests upper cumulative probability above upper domain boundary
    @Test
    public void testUpperCumulativeProbability_aboveUpperBound_returnsZero() {
        HypergeometricDistribution dist = new HypergeometricDistribution(10, 5, 5);
        assertEquals(0.0, dist.upperCumulativeProbability(6), DEFAULT_TOLERANCE);
    }

    // Tests upper cumulative probability within domain
    @Test
    public void testUpperCumulativeProbability_withinDomain_returnsCorrectValue() {
        HypergeometricDistribution dist = new HypergeometricDistribution(10, 5, 5);
        // P(X >= 3) = (100 + 25 + 1) / 252 = 126 / 252 = 0.5
        assertEquals(0.5, dist.upperCumulativeProbability(3), DEFAULT_TOLERANCE);
    }

    // Tests numerical mean calculation with standard values
    @Test
    public void testGetNumericalMean_standardValues_returnsCorrectMean() {
        HypergeometricDistribution dist = new HypergeometricDistribution(10, 5, 5);
        // mean = 5 * 5 / 10 = 2.5
        assertEquals(2.5, dist.getNumericalMean(), DEFAULT_TOLERANCE);
    }

    // Tests numerical mean with large values to ensure no integer overflow
    @Test
    public void testGetNumericalMean_largeValues_returnsCorrectMean() {
        HypergeometricDistribution dist = new HypergeometricDistribution(30000000, 3000000, 3000000);
        // mean = 3000000 * 3000000 / 30000000 = 300000
        assertEquals(300000.0, dist.getNumericalMean(), DEFAULT_TOLERANCE);
    }

    // Tests numerical variance calculation and cached value
    @Test
    public void testGetNumericalVariance_standardValues_returnsCorrectVariance() {
        HypergeometricDistribution dist = new HypergeometricDistribution(10, 5, 5);
        // variance = (5 * 5 * 5 * 5) / (100 * 9) = 625 / 900 = 25 / 36
        double expectedVariance = 25.0 / 36.0;
        assertEquals(expectedVariance, dist.getNumericalVariance(), DEFAULT_TOLERANCE);
        // Verify cached value returns identical result
        assertEquals(expectedVariance, dist.getNumericalVariance(), DEFAULT_TOLERANCE);
    }

    // Tests support lower and upper bounds
    @Test
    public void testGetSupportBounds_variousParameters_returnsCorrectBounds() {
        HypergeometricDistribution dist1 = new HypergeometricDistribution(10, 5, 5);
        assertEquals(0, dist1.getSupportLowerBound());
        assertEquals(5, dist1.getSupportUpperBound());

        HypergeometricDistribution dist2 = new HypergeometricDistribution(10, 7, 7);
        // lower: max(0, 7 + 7 - 10) = 4, upper: min(7, 7) = 7
        assertEquals(4, dist2.getSupportLowerBound());
        assertEquals(7, dist2.getSupportUpperBound());

        HypergeometricDistribution dist3 = new HypergeometricDistribution(10, 7, 3);
        // lower: max(0, 7 + 3 - 10) = 0, upper: min(7, 3) = 3
        assertEquals(0, dist3.getSupportLowerBound());
        assertEquals(3, dist3.getSupportUpperBound());
    }
}