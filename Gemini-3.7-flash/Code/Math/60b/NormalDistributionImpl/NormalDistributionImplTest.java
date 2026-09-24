package org.apache.commons.math.distribution;

import org.apache.commons.math.MathException;
import org.apache.commons.math.exception.NotStrictlyPositiveException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NormalDistributionImplTest {

    private static final double TOLERANCE = 1e-5;

    // Tests default constructor with standard normal parameters (0, 1)
    @Test
    public void testDefaultConstructor_defaultParameters_createsStandardDistribution() {
        NormalDistribution distribution = new NormalDistributionImpl();
        assertEquals(0.0, distribution.getMean(), TOLERANCE);
        assertEquals(1.0, distribution.getStandardDeviation(), TOLERANCE);
    }

    // Tests constructor setting mean and standard deviation
    @Test
    public void testConstructor_customParameters_setsFieldsCorrectly() {
        NormalDistributionImpl distribution = new NormalDistributionImpl(2.5, 1.5, 1e-8);
        assertEquals(2.5, distribution.getMean(), TOLERANCE);
        assertEquals(1.5, distribution.getStandardDeviation(), TOLERANCE);
        assertEquals(1e-8, distribution.getSolverAbsoluteAccuracy(), 1e-12);
    }

    // Tests constructor exception on non-positive standard deviation (zero)
    @Test(expected = NotStrictlyPositiveException.class)
    public void testConstructor_zeroStandardDeviation_throwsException() {
        new NormalDistributionImpl(0.0, 0.0);
    }

    // Tests constructor exception on negative standard deviation
    @Test(expected = NotStrictlyPositiveException.class)
    public void testConstructor_negativeStandardDeviation_throwsException() {
        new NormalDistributionImpl(0.0, -1.0);
    }

    // Tests density calculation at the mean
    @Test
    public void testDensity_atMean_returnsExpectedValue() {
        NormalDistribution distribution = new NormalDistributionImpl(0.0, 1.0);
        double expectedDensity = 1.0 / Math.sqrt(2.0 * Math.PI);
        assertEquals(expectedDensity, distribution.density(0.0), TOLERANCE);
    }

    // Tests density calculation away from the mean
    @Test
    public void testDensity_awayFromMean_returnsExpectedValue() {
        NormalDistribution distribution = new NormalDistributionImpl(0.0, 1.0);
        double expectedDensity = Math.exp(-0.5) / Math.sqrt(2.0 * Math.PI);
        assertEquals(expectedDensity, distribution.density(1.0), TOLERANCE);
    }

    // Tests cumulative probability at mean (P(X < mean) = 0.5)
    @Test
    public void testCumulativeProbability_atMean_returnsHalf() throws MathException {
        NormalDistribution distribution = new NormalDistributionImpl(5.0, 2.0);
        assertEquals(0.5, distribution.cumulativeProbability(5.0), TOLERANCE);
    }

    // Tests cumulative probability at known standard deviation points
    @Test
    public void testCumulativeProbability_oneStandardDeviation_returnsExpectedValue() throws MathException {
        NormalDistribution distribution = new NormalDistributionImpl(0.0, 1.0);
        assertEquals(0.8413447, distribution.cumulativeProbability(1.0), TOLERANCE);
        assertEquals(0.1586553, distribution.cumulativeProbability(-1.0), TOLERANCE);
    }

    // Tests cumulative probability with extreme positive values (regression test for Math-60)
    @Test
    public void testCumulativeProbability_extremePositiveValue_returnsOne() throws MathException {
        NormalDistribution distribution = new NormalDistributionImpl(0.0, 1.0);
        assertEquals(1.0, distribution.cumulativeProbability(Double.MAX_VALUE), TOLERANCE);
        assertEquals(1.0, distribution.cumulativeProbability(40.0), TOLERANCE);
    }

    // Tests cumulative probability with extreme negative values (regression test for Math-60)
    @Test
    public void testCumulativeProbability_extremeNegativeValue_returnsZero() throws MathException {
        NormalDistribution distribution = new NormalDistributionImpl(0.0, 1.0);
        assertEquals(0.0, distribution.cumulativeProbability(-Double.MAX_VALUE), TOLERANCE);
        assertEquals(0.0, distribution.cumulativeProbability(-40.0), TOLERANCE);
    }

    // Tests inverse cumulative probability at boundary p = 0
    @Test
    public void testInverseCumulativeProbability_probabilityZero_returnsNegativeInfinity() throws MathException {
        NormalDistribution distribution = new NormalDistributionImpl();
        assertEquals(Double.NEGATIVE_INFINITY, distribution.inverseCumulativeProbability(0.0), 0.0);
    }

    // Tests inverse cumulative probability at boundary p = 1
    @Test
    public void testInverseCumulativeProbability_probabilityOne_returnsPositiveInfinity() throws MathException {
        NormalDistribution distribution = new NormalDistributionImpl();
        assertEquals(Double.POSITIVE_INFINITY, distribution.inverseCumulativeProbability(1.0), 0.0);
    }

    // Tests inverse cumulative probability at median p = 0.5
    @Test
    public void testInverseCumulativeProbability_probabilityHalf_returnsMean() throws MathException {
        NormalDistribution distribution = new NormalDistributionImpl(10.0, 3.0);
        assertEquals(10.0, distribution.inverseCumulativeProbability(0.5), TOLERANCE);
    }

    // Tests inverse cumulative probability at standard probability points
    @Test
    public void testInverseCumulativeProbability_standardProbabilities_returnsExpectedValues() throws MathException {
        NormalDistribution distribution = new NormalDistributionImpl(0.0, 1.0);
        assertEquals(1.0, distribution.inverseCumulativeProbability(0.8413447), TOLERANCE);
        assertEquals(-1.0, distribution.inverseCumulativeProbability(0.1586553), TOLERANCE);
    }

    // Tests sample generation produces finite values within reasonable bounds
    @Test
    public void testSample_generatesFiniteValue() throws MathException {
        NormalDistribution distribution = new NormalDistributionImpl(0.0, 1.0);
        double sample = distribution.sample();
        assertTrue(Double.isFinite(sample));
    }

    // Tests getDomainLowerBound branches for p < 0.5 and p >= 0.5
    @Test
    public void testGetDomainLowerBound_variousProbabilities_returnsExpectedBounds() {
        NormalDistributionImpl distribution = new NormalDistributionImpl(5.0, 2.0);
        assertEquals(-Double.MAX_VALUE, distribution.getDomainLowerBound(0.25), 0.0);
        assertEquals(5.0, distribution.getDomainLowerBound(0.5), 0.0);
        assertEquals(5.0, distribution.getDomainLowerBound(0.75), 0.0);
    }

    // Tests getDomainUpperBound branches for p < 0.5 and p >= 0.5
    @Test
    public void testGetDomainUpperBound_variousProbabilities_returnsExpectedBounds() {
        NormalDistributionImpl distribution = new NormalDistributionImpl(5.0, 2.0);
        assertEquals(5.0, distribution.getDomainUpperBound(0.25), 0.0);
        assertEquals(Double.MAX_VALUE, distribution.getDomainUpperBound(0.5), 0.0);
        assertEquals(Double.MAX_VALUE, distribution.getDomainUpperBound(0.75), 0.0);
    }

    // Tests getInitialDomain branches for p < 0.5, p > 0.5, and p == 0.5
    @Test
    public void testGetInitialDomain_variousProbabilities_returnsExpectedValues() {
        NormalDistributionImpl distribution = new NormalDistributionImpl(10.0, 2.0);
        assertEquals(8.0, distribution.getInitialDomain(0.25), TOLERANCE);
        assertEquals(10.0, distribution.getInitialDomain(0.5), TOLERANCE);
        assertEquals(12.0, distribution.getInitialDomain(0.75), TOLERANCE);
    }
}