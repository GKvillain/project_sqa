package org.apache.commons.math3.distribution;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.apache.commons.math3.util.Pair;
import org.junit.Assert;
import org.junit.Test;

public class DiscreteDistributionTest {

    // Tests normal probability calculation for matching and non-matching elements
    @Test
    public void testProbability_existingAndNonExistingElements_returnsCorrectProbabilities() {
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(new Pair<String, Double>("A", 1.0));
        samples.add(new Pair<String, Double>("B", 3.0));
        samples.add(new Pair<String, Double>("C", 6.0));

        DiscreteDistribution<String> dist = new DiscreteDistribution<String>(samples);

        Assert.assertEquals(0.1, dist.probability("A"), 1e-9);
        Assert.assertEquals(0.3, dist.probability("B"), 1e-9);
        Assert.assertEquals(0.6, dist.probability("C"), 1e-9);
        Assert.assertEquals(0.0, dist.probability("D"), 1e-9);
    }

    // Tests probability calculation when duplicate singletons exist
    @Test
    public void testProbability_duplicateElements_accumulatesProbabilities() {
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(new Pair<String, Double>("A", 2.0));
        samples.add(new Pair<String, Double>("A", 3.0));
        samples.add(new Pair<String, Double>("B", 5.0));

        DiscreteDistribution<String> dist = new DiscreteDistribution<String>(samples);

        Assert.assertEquals(0.5, dist.probability("A"), 1e-9);
        Assert.assertEquals(0.5, dist.probability("B"), 1e-9);
    }

    // Tests probability calculation with null elements
    @Test
    public void testProbability_nullElement_returnsCorrectProbability() {
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(new Pair<String, Double>(null, 2.0));
        samples.add(new Pair<String, Double>("A", 8.0));

        DiscreteDistribution<String> dist = new DiscreteDistribution<String>(samples);

        Assert.assertEquals(0.2, dist.probability(null), 1e-9);
        Assert.assertEquals(0.8, dist.probability("A"), 1e-9);
    }

    // Tests getSamples returns normalized probabilities
    @Test
    public void testGetSamples_normalInput_returnsNormalizedProbabilities() {
        List<Pair<Integer, Double>> samples = new ArrayList<Pair<Integer, Double>>();
        samples.add(new Pair<Integer, Double>(1, 10.0));
        samples.add(new Pair<Integer, Double>(2, 30.0));

        DiscreteDistribution<Integer> dist = new DiscreteDistribution<Integer>(samples);
        List<Pair<Integer, Double>> result = dist.getSamples();

        Assert.assertEquals(2, result.size());
        Assert.assertEquals(Integer.valueOf(1), result.get(0).getKey());
        Assert.assertEquals(0.25, result.get(0).getValue(), 1e-9);
        Assert.assertEquals(Integer.valueOf(2), result.get(1).getKey());
        Assert.assertEquals(0.75, result.get(1).getValue(), 1e-9);
    }

    // Tests single sample generation with deterministic RNG
    @Test
    public void testSample_deterministicRng_returnsExpectedValue() {
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(new Pair<String, Double>("first", 0.5));
        samples.add(new Pair<String, Double>("second", 0.5));

        DiscreteDistribution<String> dist = new DiscreteDistribution<String>(new Well19937c(42L), samples);
        dist.reseedRandomGenerator(42L);

        String sample = dist.sample();
        Assert.assertTrue("first".equals(sample) || "second".equals(sample));
    }

    // Tests sample array generation with valid sample size
    @Test
    public void testSample_validSampleSize_returnsSampleArray() {
        List<Pair<Integer, Double>> samples = new ArrayList<Pair<Integer, Double>>();
        samples.add(new Pair<Integer, Double>(1, 0.4));
        samples.add(new Pair<Integer, Double>(2, 0.6));

        DiscreteDistribution<Integer> dist = new DiscreteDistribution<Integer>(samples);
        Integer[] sampled = dist.sample(10);

        Assert.assertEquals(10, sampled.length);
        for (Integer val : sampled) {
            Assert.assertTrue(val.equals(1) || val.equals(2));
        }
    }

    // Tests defect 8b: sampling when singletons are subclasses of variable type
    @Test
    public void testSample_subclassType_returnsArrayWithoutArrayStoreException() {
        List<Pair<Object, Double>> samples = new ArrayList<Pair<Object, Double>>();
        samples.add(new Pair<Object, Double>("string", 0.5));
        samples.add(new Pair<Object, Double>(Integer.valueOf(1), 0.5));

        DiscreteDistribution<Object> dist = new DiscreteDistribution<Object>(samples);
        Object[] sampled = dist.sample(10);

        Assert.assertEquals(10, sampled.length);
    }

    // Tests sample array generation with negative sample size throws exception
    @Test(expected = NotStrictlyPositiveException.class)
    public void testSample_negativeSampleSize_throwsNotStrictlyPositiveException() {
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(new Pair<String, Double>("A", 1.0));

        DiscreteDistribution<String> dist = new DiscreteDistribution<String>(samples);
        dist.sample(-1);
    }

    // Tests sample array generation with zero sample size throws exception
    @Test(expected = NotStrictlyPositiveException.class)
    public void testSample_zeroSampleSize_throwsNotStrictlyPositiveException() {
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(new Pair<String, Double>("A", 1.0));

        DiscreteDistribution<String> dist = new DiscreteDistribution<String>(samples);
        dist.sample(0);
    }

    // Tests constructor with negative probability throws exception
    @Test(expected = NotPositiveException.class)
    public void testConstructor_negativeProbability_throwsNotPositiveException() {
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(new Pair<String, Double>("A", -0.5));

        new DiscreteDistribution<String>(samples);
    }

    // Tests constructor with all zero probabilities throws exception
    @Test(expected = MathArithmeticException.class)
    public void testConstructor_zeroProbabilitiesSum_throwsMathArithmeticException() {
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(new Pair<String, Double>("A", 0.0));
        samples.add(new Pair<String, Double>("B", 0.0));

        new DiscreteDistribution<String>(samples);
    }

    // Tests constructor with infinite probability throws exception
    @Test(expected = MathIllegalArgumentException.class)
    public void testConstructor_infiniteProbability_throwsMathIllegalArgumentException() {
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(new Pair<String, Double>("A", Double.POSITIVE_INFINITY));

        new DiscreteDistribution<String>(samples);
    }

    // Tests constructor with custom RNG and reseed
    @Test
    public void testReseedRandomGenerator_customRng_reseedsCorrectly() {
        RandomGenerator rng = new Well19937c();
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(new Pair<String, Double>("A", 1.0));

        DiscreteDistribution<String> dist = new DiscreteDistribution<String>(rng, samples);
        dist.reseedRandomGenerator(123456L);

        Assert.assertEquals("A", dist.sample());
    }
}