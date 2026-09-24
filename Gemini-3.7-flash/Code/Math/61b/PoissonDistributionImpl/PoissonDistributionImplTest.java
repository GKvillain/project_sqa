package org.apache.commons.math.distribution;

import org.apache.commons.math.MathException;
import org.apache.commons.math.util.FastMath;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link PoissonDistributionImpl}.
 */
public class PoissonDistributionImplTest {

    private static final double DEFAULT_EPSILON = 1e-12;

    // Tests negative mean in standard constructor throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_negativeMean_throwsIllegalArgumentException() {
        new PoissonDistributionImpl(-1.0);
    }

    // Tests zero mean in standard constructor throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_zeroMean_throwsIllegalArgumentException() {
        new PoissonDistributionImpl(0.0);
    }

    // Tests negative mean with epsilon and maxIterations throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_negativeMeanWithEpsilonAndIterations_throwsIllegalArgumentException() {
        new PoissonDistributionImpl(-2.5, 1e-5, 100);
    }

    // Tests standard constructor with valid positive mean
    @Test
    public void testConstructor_validMean_setsMeanCorrectly() {
        PoissonDistribution dist = new PoissonDistributionImpl(4.0);
        assertEquals(4.0, dist.getMean(), DEFAULT_EPSILON);
    }

    // Tests constructor with custom epsilon and max iterations
    @Test
    public void testConstructor_withEpsilonAndMaxIterations_setsMeanCorrectly() {
        PoissonDistribution dist = new PoissonDistributionImpl(5.0, 1e-8, 500);
        assertEquals(5.0, dist.getMean(), DEFAULT_EPSILON);
    }

    // Tests constructor with custom epsilon
    @Test
    public void testConstructor_withEpsilon_setsMeanCorrectly() {
        PoissonDistribution dist = new PoissonDistributionImpl(3.0, 1e-6);
        assertEquals(3.0, dist.getMean(), DEFAULT_EPSILON);
    }

    // Tests constructor with custom max iterations
    @Test
    public void testConstructor_withMaxIterations_setsMeanCorrectly() {
        PoissonDistribution dist = new PoissonDistributionImpl(2.5, 1000);
        assertEquals(2.5, dist.getMean(), DEFAULT_EPSILON);
    }

    // Tests probability for negative x returns zero
    @Test
    public void testProbability_negativeX_returnsZero() {
        PoissonDistribution dist = new PoissonDistributionImpl(4.0);
        assertEquals(0.0, dist.probability(-1), DEFAULT_EPSILON);
        assertEquals(0.0, dist.probability(-10), DEFAULT_EPSILON);
    }

    // Tests probability for Integer.MAX_VALUE returns zero
    @Test
    public void testProbability_maxInteger_returnsZero() {
        PoissonDistribution dist = new PoissonDistributionImpl(4.0);
        assertEquals(0.0, dist.probability(Integer.MAX_VALUE), DEFAULT_EPSILON);
    }

    // Tests probability for x = 0 returns exp(-mean)
    @Test
    public void testProbability_zeroX_returnsExpMinusMean() {
        double mean = 3.0;
        PoissonDistribution dist = new PoissonDistributionImpl(mean);
        assertEquals(FastMath.exp(-mean), dist.probability(0), DEFAULT_EPSILON);
    }

    // Tests probability for positive x
    @Test
    public void testProbability_positiveX_returnsCorrectValue() {
        PoissonDistribution dist = new PoissonDistributionImpl(4.0);
        // P(X = 1) = 4^1 * exp(-4) / 1! = 4 * exp(-4)
        double expected = 4.0 * FastMath.exp(-4.0);
        assertEquals(expected, dist.probability(1), 1e-10);
    }

    // Tests cumulative probability for negative x returns zero
    @Test
    public void testCumulativeProbability_negativeX_returnsZero() throws MathException {
        PoissonDistribution dist = new PoissonDistributionImpl(4.0);
        assertEquals(0.0, dist.cumulativeProbability(-1), DEFAULT_EPSILON);
        assertEquals(0.0, dist.cumulativeProbability(-100), DEFAULT_EPSILON);
    }

    // Tests cumulative probability for Integer.MAX_VALUE returns one
    @Test
    public void testCumulativeProbability_maxInteger_returnsOne() throws MathException {
        PoissonDistribution dist = new PoissonDistributionImpl(4.0);
        assertEquals(1.0, dist.cumulativeProbability(Integer.MAX_VALUE), DEFAULT_EPSILON);
    }

    // Tests cumulative probability for valid non-negative values
    @Test
    public void testCumulativeProbability_positiveValues_returnsCorrectSum() throws MathException {
        PoissonDistribution dist = new PoissonDistributionImpl(2.0);
        double p0 = dist.probability(0);
        double p1 = dist.probability(1);
        double p2 = dist.probability(2);

        assertEquals(p0, dist.cumulativeProbability(0), 1e-10);
        assertEquals(p0 + p1, dist.cumulativeProbability(1), 1e-10);
        assertEquals(p0 + p1 + p2, dist.cumulativeProbability(2), 1e-10);
    }

    // Tests normal approximate probability
    @Test
    public void testNormalApproximateProbability_returnsReasonableApproximation() throws MathException {
        PoissonDistributionImpl dist = new PoissonDistributionImpl(100.0);
        double exact = dist.cumulativeProbability(100);
        double approx = dist.normalApproximateProbability(100);
        // For large mean, normal approximation should be reasonably close to exact CDF
        assertEquals(exact, approx, 0.05);
    }

    // Tests sampling generates non-negative values
    @Test
    public void testSample_generatesNonNegativeValue() throws MathException {
        PoissonDistributionImpl dist = new PoissonDistributionImpl(5.0);
        dist.reseedRandomGenerator(1234567L);
        int sample = dist.sample();
        assertTrue(sample >= 0);
    }

    // Tests domain lower and upper bounds
    @Test
    public void testGetDomainBounds_returnsCorrectValues() {
        PoissonDistributionImpl dist = new PoissonDistributionImpl(5.0);
        assertEquals(0, dist.getDomainLowerBound(0.5));
        assertEquals(Integer.MAX_VALUE, dist.getDomainUpperBound(0.5));
    }
}