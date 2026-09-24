package org.apache.commons.math3.distribution;

import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.random.Well19937c;
import org.junit.Assert;
import org.junit.Test;

public class FDistributionTest {

    private static final double TOLERANCE = 1e-5;

    // Tests constructor with invalid numerator degrees of freedom <= 0
    @Test(expected = NotStrictlyPositiveException.class)
    public void testConstructor_invalidNumeratorDF_throwsException() {
        new FDistribution(0.0, 5.0);
    }

    // Tests constructor with negative numerator degrees of freedom
    @Test(expected = NotStrictlyPositiveException.class)
    public void testConstructor_negativeNumeratorDF_throwsException() {
        new FDistribution(-1.0, 5.0);
    }

    // Tests constructor with invalid denominator degrees of freedom <= 0
    @Test(expected = NotStrictlyPositiveException.class)
    public void testConstructor_invalidDenominatorDF_throwsException() {
        new FDistribution(5.0, 0.0);
    }

    // Tests constructor with negative denominator degrees of freedom
    @Test(expected = NotStrictlyPositiveException.class)
    public void testConstructor_negativeDenominatorDF_throwsException() {
        new FDistribution(5.0, -2.0);
    }

    // Tests 3-arg constructor and getters for degrees of freedom and solver accuracy
    @Test
    public void testGetDegreesOfFreedomAndSolverAbsoluteAccuracy() {
        double numDF = 4.0;
        double denDF = 8.0;
        double accuracy = 1e-8;
        FDistribution dist = new FDistribution(numDF, denDF, accuracy);

        Assert.assertEquals(numDF, dist.getNumeratorDegreesOfFreedom(), 0.0);
        Assert.assertEquals(denDF, dist.getDenominatorDegreesOfFreedom(), 0.0);
        Assert.assertEquals(accuracy, dist.getSolverAbsoluteAccuracy(), 0.0);
    }

    // Tests 4-arg constructor with custom RandomGenerator
    @Test
    public void testConstructor_withRng_createsInstance() {
        FDistribution dist = new FDistribution(new Well19937c(123456L), 2.0, 4.0, 1e-9);
        Assert.assertEquals(2.0, dist.getNumeratorDegreesOfFreedom(), 0.0);
        Assert.assertEquals(4.0, dist.getDenominatorDegreesOfFreedom(), 0.0);
    }

    // Tests density function at positive x value
    @Test
    public void testDensity_positiveValue_returnsCorrectDensity() {
        FDistribution dist = new FDistribution(5.0, 2.0);
        double density = dist.density(1.0);
        Assert.assertTrue(density > 0.0);
        Assert.assertEquals(0.244301, density, TOLERANCE);
    }

    // Tests cumulative probability for x <= 0 (boundary)
    @Test
    public void testCumulativeProbability_nonPositiveX_returnsZero() {
        FDistribution dist = new FDistribution(5.0, 5.0);
        Assert.assertEquals(0.0, dist.cumulativeProbability(0.0), 0.0);
        Assert.assertEquals(0.0, dist.cumulativeProbability(-1.0), 0.0);
    }

    // Tests cumulative probability for x > 0
    @Test
    public void testCumulativeProbability_positiveX_returnsCorrectProbability() {
        FDistribution dist = new FDistribution(5.0, 5.0);
        Assert.assertEquals(0.5, dist.cumulativeProbability(1.0), TOLERANCE);
    }

    // Tests numerical mean when denominator degrees of freedom > 2
    @Test
    public void testGetNumericalMean_denDFGreaterThanTwo_returnsCorrectMean() {
        FDistribution dist = new FDistribution(5.0, 6.0);
        Assert.assertEquals(6.0 / (6.0 - 2.0), dist.getNumericalMean(), TOLERANCE);
    }

    // Tests numerical mean when denominator degrees of freedom <= 2
    @Test
    public void testGetNumericalMean_denDFLessOrEqualToTwo_returnsNaN() {
        FDistribution dist1 = new FDistribution(5.0, 2.0);
        Assert.assertTrue(Double.isNaN(dist1.getNumericalMean()));

        FDistribution dist2 = new FDistribution(5.0, 1.5);
        Assert.assertTrue(Double.isNaN(dist2.getNumericalMean()));
    }

    // Tests numerical variance when denominator degrees of freedom > 4
    @Test
    public void testGetNumericalVariance_denDFGreaterThanFour_returnsCorrectVariance() {
        double numDF = 5.0;
        double denDF = 6.0;
        FDistribution dist = new FDistribution(numDF, denDF);
        double expectedVariance = (2.0 * (denDF * denDF) * (numDF + denDF - 2.0)) /
                (numDF * ((denDF - 2.0) * (denDF - 2.0)) * (denDF - 4.0));
        Assert.assertEquals(expectedVariance, dist.getNumericalVariance(), TOLERANCE);
        // Repeated call to verify cached variance
        Assert.assertEquals(expectedVariance, dist.getNumericalVariance(), TOLERANCE);
    }

    // Tests numerical variance when denominator degrees of freedom <= 4
    @Test
    public void testGetNumericalVariance_denDFLessOrEqualToFour_returnsNaN() {
        FDistribution dist1 = new FDistribution(5.0, 4.0);
        Assert.assertTrue(Double.isNaN(dist1.getNumericalVariance()));

        FDistribution dist2 = new FDistribution(5.0, 3.0);
        Assert.assertTrue(Double.isNaN(dist2.getNumericalVariance()));
    }

    // Tests support bounds and connectivity properties
    @Test
    public void testSupportBoundsAndProperties() {
        FDistribution dist = new FDistribution(5.0, 5.0);
        Assert.assertEquals(0.0, dist.getSupportLowerBound(), 0.0);
        Assert.assertEquals(Double.POSITIVE_INFINITY, dist.getSupportUpperBound(), 0.0);
        Assert.assertTrue(dist.isSupportConnected());
        Assert.assertFalse(dist.isSupportUpperBoundInclusive());
    }

    // Tests support lower bound inclusive (Defects4J bug regression)
    @Test
    public void testIsSupportLowerBoundInclusive_returnsFalse() {
        FDistribution dist = new FDistribution(5.0, 5.0);
        Assert.assertFalse(dist.isSupportLowerBoundInclusive());
    }

    // Tests 3-arg constructor with custom RandomGenerator
    @Test
    public void testConstructor_withRng3Args_createsInstance() {
        FDistribution dist = new FDistribution(new Well19937c(123456L), 2.0, 4.0);
        Assert.assertEquals(2.0, dist.getNumeratorDegreesOfFreedom(), 0.0);
        Assert.assertEquals(4.0, dist.getDenominatorDegreesOfFreedom(), 0.0);
        Assert.assertEquals(FDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY, dist.getSolverAbsoluteAccuracy(), 0.0);
    }

    // Tests logDensity calculation
    @Test
    public void testLogDensity() {
        FDistribution dist = new FDistribution(5.0, 2.0);
        double density = dist.density(1.0);
        double logDensity = dist.logDensity(1.0);
        Assert.assertEquals(Math.log(density), logDensity, TOLERANCE);
    }

    // Tests inverse cumulative probability
    @Test
    public void testInverseCumulativeProbability() {
        FDistribution dist = new FDistribution(5.0, 5.0);
        Assert.assertEquals(0.0, dist.inverseCumulativeProbability(0.0), 0.0);
        Assert.assertEquals(Double.POSITIVE_INFINITY, dist.inverseCumulativeProbability(1.0), 0.0);
        double p = dist.cumulativeProbability(1.0);
        Assert.assertEquals(1.0, dist.inverseCumulativeProbability(p), TOLERANCE);
    }

    // Tests sampling from distribution
    @Test
    public void testSample() {
        FDistribution dist = new FDistribution(new Well19937c(123456L), 5.0, 5.0);
        double sample = dist.sample();
        Assert.assertTrue(sample > 0.0);
    }
}