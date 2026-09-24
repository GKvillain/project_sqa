package org.apache.commons.math.distribution;

import org.apache.commons.math.MathException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link FDistributionImpl}.
 */
public class FDistributionImplTest {

    private static final double TOLERANCE = 1E-5;
    private FDistributionImpl distribution;

    @Before
    public void setUp() {
        distribution = new FDistributionImpl(5.0, 6.0);
    }

    // Tests valid instantiation and getters
    @Test
    public void testConstructor_validParameters_setsDegreesOfFreedom() {
        FDistributionImpl dist = new FDistributionImpl(10.0, 20.0);
        assertEquals(10.0, dist.getNumeratorDegreesOfFreedom(), TOLERANCE);
        assertEquals(20.0, dist.getDenominatorDegreesOfFreedom(), TOLERANCE);
    }

    // Tests constructor with non-positive numerator degrees of freedom
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nonPositiveNumeratorDf_throwsIllegalArgumentException() {
        new FDistributionImpl(0.0, 5.0);
    }

    // Tests constructor with non-positive denominator degrees of freedom
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nonPositiveDenominatorDf_throwsIllegalArgumentException() {
        new FDistributionImpl(5.0, -1.0);
    }

    // Tests setting valid numerator degrees of freedom
    @Test
    public void testSetNumeratorDegreesOfFreedom_validValue_updatesCorrectly() {
        distribution.setNumeratorDegreesOfFreedom(8.0);
        assertEquals(8.0, distribution.getNumeratorDegreesOfFreedom(), TOLERANCE);
    }

    // Tests setting non-positive numerator degrees of freedom
    @Test(expected = IllegalArgumentException.class)
    public void testSetNumeratorDegreesOfFreedom_zeroValue_throwsIllegalArgumentException() {
        distribution.setNumeratorDegreesOfFreedom(0.0);
    }

    // Tests setting valid denominator degrees of freedom
    @Test
    public void testSetDenominatorDegreesOfFreedom_validValue_updatesCorrectly() {
        distribution.setDenominatorDegreesOfFreedom(12.0);
        assertEquals(12.0, distribution.getDenominatorDegreesOfFreedom(), TOLERANCE);
    }

    // Tests setting non-positive denominator degrees of freedom
    @Test(expected = IllegalArgumentException.class)
    public void testSetDenominatorDegreesOfFreedom_negativeValue_throwsIllegalArgumentException() {
        distribution.setDenominatorDegreesOfFreedom(-5.0);
    }

    // Tests cumulative probability for negative x
    @Test
    public void testCumulativeProbability_negativeInput_returnsZero() throws MathException {
        assertEquals(0.0, distribution.cumulativeProbability(-1.5), TOLERANCE);
    }

    // Tests cumulative probability for zero x
    @Test
    public void testCumulativeProbability_zeroInput_returnsZero() throws MathException {
        assertEquals(0.0, distribution.cumulativeProbability(0.0), TOLERANCE);
    }

    // Tests cumulative probability for positive x
    @Test
    public void testCumulativeProbability_positiveInput_returnsCorrectProbability() throws MathException {
        double prob = distribution.cumulativeProbability(4.387);
        assertEquals(0.95, prob, 1E-2);
    }

    // Tests inverse cumulative probability boundary at p = 0
    @Test
    public void testInverseCumulativeProbability_zeroProbability_returnsZero() throws MathException {
        assertEquals(0.0, distribution.inverseCumulativeProbability(0.0), TOLERANCE);
    }

    // Tests inverse cumulative probability boundary at p = 1
    @Test
    public void testInverseCumulativeProbability_oneProbability_returnsPositiveInfinity() throws MathException {
        assertEquals(Double.POSITIVE_INFINITY, distribution.inverseCumulativeProbability(1.0), TOLERANCE);
    }

    // Tests inverse cumulative probability out of bounds (p < 0)
    @Test(expected = IllegalArgumentException.class)
    public void testInverseCumulativeProbability_negativeProbability_throwsIllegalArgumentException() throws MathException {
        distribution.inverseCumulativeProbability(-0.1);
    }

    // Tests inverse cumulative probability out of bounds (p > 1)
    @Test(expected = IllegalArgumentException.class)
    public void testInverseCumulativeProbability_greaterThanOneProbability_throwsIllegalArgumentException() throws MathException {
        distribution.inverseCumulativeProbability(1.1);
    }

    // Tests inverse cumulative probability for standard valid probability
    @Test
    public void testInverseCumulativeProbability_validProbability_returnsCriticalValue() throws MathException {
        double x = distribution.inverseCumulativeProbability(0.95);
        assertEquals(0.95, distribution.cumulativeProbability(x), TOLERANCE);
    }

    // Tests domain lower bound
    @Test
    public void testGetDomainLowerBound_anyProbability_returnsZero() {
        assertEquals(0.0, distribution.getDomainLowerBound(0.5), TOLERANCE);
    }

    // Tests domain upper bound
    @Test
    public void testGetDomainUpperBound_anyProbability_returnsDoubleMaxValue() {
        assertEquals(Double.MAX_VALUE, distribution.getDomainUpperBound(0.5), TOLERANCE);
    }

    // Tests initial domain calculation for d > 2
    @Test
    public void testGetInitialDomain_dGreaterThanTwo_returnsMean() {
        // d = 6.0, expected = 6.0 / (6.0 - 2.0) = 1.5
        assertEquals(1.5, distribution.getInitialDomain(0.5), TOLERANCE);
    }

    // Tests inverse cumulative probability when denominator degrees of freedom is small (Math-95 defect)
    @Test
    public void testInverseCumulativeProbability_smallDenominatorDf_calculatesSuccessfully() throws MathException {
        FDistributionImpl smallDfDist = new FDistributionImpl(1.0, 2.0);
        double x = smallDfDist.inverseCumulativeProbability(0.975);
        assertTrue(x > 0.0);
        assertEquals(0.975, smallDfDist.cumulativeProbability(x), TOLERANCE);
    }
}