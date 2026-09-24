package org.apache.commons.math.distribution;

import org.apache.commons.math.MathException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NormalDistributionImplTest {

    private NormalDistributionImpl distribution;
    private final double defaultTolerance = 1e-5;

    @Before
    public void setUp() {
        distribution = new NormalDistributionImpl();
    }

    // Tests default constructor parameter values
    @Test
    public void testDefaultConstructor_defaultParameters_setsMeanZeroAndSdOne() {
        assertEquals(0.0, distribution.getMean(), defaultTolerance);
        assertEquals(1.0, distribution.getStandardDeviation(), defaultTolerance);
    }

    // Tests custom constructor parameter values
    @Test
    public void testCustomConstructor_validParameters_setsMeanAndSd() {
        NormalDistributionImpl dist = new NormalDistributionImpl(2.5, 1.5);
        assertEquals(2.5, dist.getMean(), defaultTolerance);
        assertEquals(1.5, dist.getStandardDeviation(), defaultTolerance);
    }

    // Tests setMean modification
    @Test
    public void testSetMean_validMean_updatesMean() {
        distribution.setMean(5.0);
        assertEquals(5.0, distribution.getMean(), defaultTolerance);
    }

    // Tests setStandardDeviation with valid positive value
    @Test
    public void testSetStandardDeviation_positiveValue_updatesSd() {
        distribution.setStandardDeviation(2.5);
        assertEquals(2.5, distribution.getStandardDeviation(), defaultTolerance);
    }

    // Tests setStandardDeviation with zero throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testSetStandardDeviation_zeroValue_throwsIllegalArgumentException() {
        distribution.setStandardDeviation(0.0);
    }

    // Tests setStandardDeviation with negative value throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testSetStandardDeviation_negativeValue_throwsIllegalArgumentException() {
        distribution.setStandardDeviation(-1.0);
    }

    // Tests cumulativeProbability at mean returning 0.5
    @Test
    public void testCumulativeProbability_atMean_returnsHalf() throws MathException {
        double result = distribution.cumulativeProbability(0.0);
        assertEquals(0.5, result, defaultTolerance);
    }

    // Tests cumulativeProbability for standard normal critical points
    @Test
    public void testCumulativeProbability_standardPoints_returnsCorrectValues() throws MathException {
        assertEquals(0.84134, distribution.cumulativeProbability(1.0), defaultTolerance);
        assertEquals(0.15865, distribution.cumulativeProbability(-1.0), defaultTolerance);
        assertEquals(0.97725, distribution.cumulativeProbability(2.0), defaultTolerance);
    }

    // Tests cumulativeProbability for extreme values (Defects4J Math-103 defect detection)
    @Test
    public void testCumulativeProbability_extremeValues_doesNotThrowConvergenceException() throws MathException {
        assertEquals(0.0, distribution.cumulativeProbability(-20.0), defaultTolerance);
        assertEquals(1.0, distribution.cumulativeProbability(20.0), defaultTolerance);
        assertEquals(0.0, distribution.cumulativeProbability(-100.0), defaultTolerance);
        assertEquals(1.0, distribution.cumulativeProbability(100.0), defaultTolerance);
    }

    // Tests inverseCumulativeProbability boundary p=0 returning negative infinity
    @Test
    public void testInverseCumulativeProbability_zeroProbability_returnsNegativeInfinity() throws MathException {
        double result = distribution.inverseCumulativeProbability(0.0);
        assertEquals(Double.NEGATIVE_INFINITY, result, 0.0);
    }

    // Tests inverseCumulativeProbability boundary p=1 returning positive infinity
    @Test
    public void testInverseCumulativeProbability_oneProbability_returnsPositiveInfinity() throws MathException {
        double result = distribution.inverseCumulativeProbability(1.0);
        assertEquals(Double.POSITIVE_INFINITY, result, 0.0);
    }

    // Tests inverseCumulativeProbability at p=0.5 returning mean
    @Test
    public void testInverseCumulativeProbability_halfProbability_returnsMean() throws MathException {
        double result = distribution.inverseCumulativeProbability(0.5);
        assertEquals(0.0, result, defaultTolerance);
    }

    // Tests inverseCumulativeProbability for invalid probabilities outside [0, 1]
    @Test(expected = IllegalArgumentException.class)
    public void testInverseCumulativeProbability_negativeProbability_throwsIllegalArgumentException() throws MathException {
        distribution.inverseCumulativeProbability(-0.1);
    }

    // Tests inverseCumulativeProbability for invalid probabilities outside [0, 1]
    @Test(expected = IllegalArgumentException.class)
    public void testInverseCumulativeProbability_greaterThanOne_throwsIllegalArgumentException() throws MathException {
        distribution.inverseCumulativeProbability(1.1);
    }

    // Tests getDomainLowerBound branches for p < 0.5 and p >= 0.5
    @Test
    public void testGetDomainLowerBound_branches_returnsExpectedBounds() {
        assertEquals(-Double.MAX_VALUE, distribution.getDomainLowerBound(0.2), 0.0);
        assertEquals(0.0, distribution.getDomainLowerBound(0.5), defaultTolerance);
        assertEquals(0.0, distribution.getDomainLowerBound(0.8), defaultTolerance);
    }

    // Tests getDomainUpperBound branches for p < 0.5 and p >= 0.5
    @Test
    public void testGetDomainUpperBound_branches_returnsExpectedBounds() {
        assertEquals(0.0, distribution.getDomainUpperBound(0.2), defaultTolerance);
        assertEquals(Double.MAX_VALUE, distribution.getDomainUpperBound(0.5), 0.0);
        assertEquals(Double.MAX_VALUE, distribution.getDomainUpperBound(0.8), 0.0);
    }

    // Tests getInitialDomain branches for p < 0.5, p > 0.5, and p == 0.5
    @Test
    public void testGetInitialDomain_allBranches_returnsCorrectInitialPoints() {
        distribution.setMean(10.0);
        distribution.setStandardDeviation(2.0);

        assertEquals(8.0, distribution.getInitialDomain(0.3), defaultTolerance);
        assertEquals(12.0, distribution.getInitialDomain(0.7), defaultTolerance);
        assertEquals(10.0, distribution.getInitialDomain(0.5), defaultTolerance);
    }
}