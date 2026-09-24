package org.apache.commons.math3.distribution;

import org.apache.commons.math3.exception.NumberIsTooLargeException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.random.Well19937c;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UniformRealDistributionTest {

    private static final double DEFAULT_TOLERANCE = 1e-9;

    // Tests default constructor initializes standard uniform distribution on [0, 1]
    @Test
    public void testConstructor_default_createsStandardDistribution() {
        UniformRealDistribution dist = new UniformRealDistribution();
        assertEquals(0.0, dist.getSupportLowerBound(), DEFAULT_TOLERANCE);
        assertEquals(1.0, dist.getSupportUpperBound(), DEFAULT_TOLERANCE);
        assertEquals(0.5, dist.getNumericalMean(), DEFAULT_TOLERANCE);
        assertEquals(1.0 / 12.0, dist.getNumericalVariance(), DEFAULT_TOLERANCE);
    }

    // Tests custom bounds constructor with valid parameters
    @Test
    public void testConstructor_validBounds_initializesCorrectly() {
        UniformRealDistribution dist = new UniformRealDistribution(-2.0, 3.0);
        assertEquals(-2.0, dist.getSupportLowerBound(), DEFAULT_TOLERANCE);
        assertEquals(3.0, dist.getSupportUpperBound(), DEFAULT_TOLERANCE);
        assertEquals(0.5, dist.getNumericalMean(), DEFAULT_TOLERANCE);
        assertEquals(25.0 / 12.0, dist.getNumericalVariance(), DEFAULT_TOLERANCE);
    }

    // Tests constructor when lower bound equals upper bound throws NumberIsTooLargeException
    @Test(expected = NumberIsTooLargeException.class)
    public void testConstructor_lowerEqualsUpper_throwsException() {
        new UniformRealDistribution(2.0, 2.0);
    }

    // Tests constructor when lower bound is greater than upper bound throws NumberIsTooLargeException
    @Test(expected = NumberIsTooLargeException.class)
    public void testConstructor_lowerGreaterThanUpper_throwsException() {
        new UniformRealDistribution(5.0, 2.0);
    }

    // Tests constructor with custom inverse cumulative accuracy and custom RNG
    @Test
    public void testConstructor_customRngAndAccuracy_initializesCorrectly() {
        double customAccuracy = 1e-6;
        UniformRealDistribution dist = new UniformRealDistribution(new Well19937c(123456L), 1.0, 5.0, customAccuracy);
        assertEquals(customAccuracy, dist.getSolverAbsoluteAccuracy(), DEFAULT_TOLERANCE);
    }

    // Tests density function within support bounds
    @Test
    public void testDensity_withinBounds_returnsConstantDensity() {
        UniformRealDistribution dist = new UniformRealDistribution(0.0, 4.0);
        assertEquals(0.25, dist.density(2.0), DEFAULT_TOLERANCE);
        assertEquals(0.25, dist.density(0.0), DEFAULT_TOLERANCE);
        assertEquals(0.25, dist.density(4.0), DEFAULT_TOLERANCE);
    }

    // Tests density function outside support bounds
    @Test
    public void testDensity_outsideBounds_returnsZero() {
        UniformRealDistribution dist = new UniformRealDistribution(1.0, 3.0);
        assertEquals(0.0, dist.density(0.999), DEFAULT_TOLERANCE);
        assertEquals(0.0, dist.density(3.001), DEFAULT_TOLERANCE);
    }

    // Tests cumulative probability for values strictly within bounds
    @Test
    public void testCumulativeProbability_withinBounds_returnsLinearProbability() {
        UniformRealDistribution dist = new UniformRealDistribution(2.0, 6.0);
        assertEquals(0.25, dist.cumulativeProbability(3.0), DEFAULT_TOLERANCE);
        assertEquals(0.50, dist.cumulativeProbability(4.0), DEFAULT_TOLERANCE);
        assertEquals(0.75, dist.cumulativeProbability(5.0), DEFAULT_TOLERANCE);
    }

    // Tests cumulative probability at and below lower boundary
    @Test
    public void testCumulativeProbability_atAndBelowLowerBound_returnsZero() {
        UniformRealDistribution dist = new UniformRealDistribution(2.0, 6.0);
        assertEquals(0.0, dist.cumulativeProbability(2.0), DEFAULT_TOLERANCE);
        assertEquals(0.0, dist.cumulativeProbability(1.0), DEFAULT_TOLERANCE);
        assertEquals(0.0, dist.cumulativeProbability(Double.NEGATIVE_INFINITY), DEFAULT_TOLERANCE);
    }

    // Tests cumulative probability at and above upper boundary
    @Test
    public void testCumulativeProbability_atAndAboveUpperBound_returnsOne() {
        UniformRealDistribution dist = new UniformRealDistribution(2.0, 6.0);
        assertEquals(1.0, dist.cumulativeProbability(6.0), DEFAULT_TOLERANCE);
        assertEquals(1.0, dist.cumulativeProbability(7.0), DEFAULT_TOLERANCE);
        assertEquals(1.0, dist.cumulativeProbability(Double.POSITIVE_INFINITY), DEFAULT_TOLERANCE);
    }

    // Tests mean and variance calculation with negative lower bound
    @Test
    public void testGetNumericalMeanAndVariance_negativeBounds_returnsCorrectValues() {
        UniformRealDistribution dist = new UniformRealDistribution(-5.0, -1.0);
        assertEquals(-3.0, dist.getNumericalMean(), DEFAULT_TOLERANCE);
        assertEquals(16.0 / 12.0, dist.getNumericalVariance(), DEFAULT_TOLERANCE);
    }

    // Tests support bounds retrieval
    @Test
    public void testGetSupportLowerAndUpperBound_validRange_returnsConfiguredBounds() {
        UniformRealDistribution dist = new UniformRealDistribution(-10.5, 20.5);
        assertEquals(-10.5, dist.getSupportLowerBound(), DEFAULT_TOLERANCE);
        assertEquals(20.5, dist.getSupportUpperBound(), DEFAULT_TOLERANCE);
    }

    // Tests support properties including connectivity and bound inclusivity
    @Test
    public void testSupportProperties_standardDistribution_returnsExpectedFlags() {
        UniformRealDistribution dist = new UniformRealDistribution(0.0, 1.0);
        assertTrue(dist.isSupportConnected());
        assertTrue(dist.isSupportLowerBoundInclusive());
        assertFalse(dist.isSupportUpperBoundInclusive());
    }

    // Tests sample generation produces values within the valid support interval
    @Test
    public void testSample_randomSampling_valuesWithinRange() {
        UniformRealDistribution dist = new UniformRealDistribution(new Well19937c(42L), 10.0, 20.0, UniformRealDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
        for (int i = 0; i < 50; i++) {
            double sample = dist.sample();
            assertTrue("Sample " + sample + " should be >= 10.0", sample >= 10.0);
            assertTrue("Sample " + sample + " should be <= 20.0", sample <= 20.0);
        }
    }

    // Tests constructor with lower, upper, and inverse cumulative accuracy parameters
    @Test
    public void testConstructor_boundsAndAccuracy_initializesCorrectly() {
        double customAccuracy = 1e-4;
        UniformRealDistribution dist = new UniformRealDistribution(2.0, 8.0, customAccuracy);
        assertEquals(2.0, dist.getSupportLowerBound(), DEFAULT_TOLERANCE);
        assertEquals(8.0, dist.getSupportUpperBound(), DEFAULT_TOLERANCE);
        assertEquals(customAccuracy, dist.getSolverAbsoluteAccuracy(), DEFAULT_TOLERANCE);
    }

    // Tests constructor with custom RNG, lower, and upper parameters
    @Test
    public void testConstructor_rngAndBounds_initializesCorrectly() {
        UniformRealDistribution dist = new UniformRealDistribution(new Well19937c(999L), -4.0, 4.0);
        assertEquals(-4.0, dist.getSupportLowerBound(), DEFAULT_TOLERANCE);
        assertEquals(4.0, dist.getSupportUpperBound(), DEFAULT_TOLERANCE);
        assertEquals(UniformRealDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY, dist.getSolverAbsoluteAccuracy(), DEFAULT_TOLERANCE);
    }

    // Tests inverse cumulative probability calculation across the domain [0, 1]
    @Test
    public void testInverseCumulativeProbability_validProbabilities_returnsCorrectValues() {
        UniformRealDistribution dist = new UniformRealDistribution(2.0, 10.0);
        assertEquals(2.0, dist.inverseCumulativeProbability(0.0), DEFAULT_TOLERANCE);
        assertEquals(4.0, dist.inverseCumulativeProbability(0.25), DEFAULT_TOLERANCE);
        assertEquals(6.0, dist.inverseCumulativeProbability(0.50), DEFAULT_TOLERANCE);
        assertEquals(8.0, dist.inverseCumulativeProbability(0.75), DEFAULT_TOLERANCE);
        assertEquals(10.0, dist.inverseCumulativeProbability(1.0), DEFAULT_TOLERANCE);
    }

    // Tests inverse cumulative probability throws exception when p < 0
    @Test(expected = OutOfRangeException.class)
    public void testInverseCumulativeProbability_negativeProbability_throwsException() {
        UniformRealDistribution dist = new UniformRealDistribution(0.0, 1.0);
        dist.inverseCumulativeProbability(-0.01);
    }

    // Tests inverse cumulative probability throws exception when p > 1
    @Test(expected = OutOfRangeException.class)
    public void testInverseCumulativeProbability_probabilityGreaterThanOne_throwsException() {
        UniformRealDistribution dist = new UniformRealDistribution(0.0, 1.0);
        dist.inverseCumulativeProbability(1.01);
    }
}