package org.apache.commons.math3.distribution;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.random.Well19937c;
import org.apache.commons.math3.util.Pair;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class DiscreteDistributionTest {

    // Test constructor with valid samples
    @Test
    public void testConstructor_validSamples_createsDistribution() {
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(new Pair<String, Double>("A", 0.2));
        samples.add(new Pair<String, Double>("B", 0.3));
        samples.add(new Pair<String, Double>("C", 0.5));
        
        DiscreteDistribution<String> dist = new DiscreteDistribution<String>(samples);
        
        assertEquals(3, dist.getSamples().size());
        assertEquals(0.2, dist.probability("A"), 1e-15);
        assertEquals(0.3, dist.probability("B"), 1e-15);
        assertEquals(0.5, dist.probability("C"), 1e-15);
    }

    // Tests exception when probability is negative
    @Test(expected = NotPositiveException.class)
    public void testConstructor_negativeProbability_throwsException() {
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(new Pair<String, Double>("A", -0.1));
        samples.add(new Pair<String, Double>("B", 1.1));
        
        new DiscreteDistribution<String>(samples);
    }

    // Tests exception when all probabilities sum to zero
    @Test(expected = MathArithmeticException.class)
    public void testConstructor_zeroSumProbabilities_throwsException() {
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(new Pair<String, Double>("A", 0.0));
        samples.add(new Pair<String, Double>("B", 0.0));
        
        new DiscreteDistribution<String>(samples);
    }

    // Tests exception when probability is infinite
    @Test(expected = MathIllegalArgumentException.class)
    public void testConstructor_infiniteProbability_throwsException() {
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(new Pair<String, Double>("A", Double.POSITIVE_INFINITY));
        samples.add(new Pair<String, Double>("B", 1.0));
        
        new DiscreteDistribution<String>(samples);
    }

    // Test constructor with custom RNG
    @Test
    public void testConstructor_customRng_createsDistribution() {
        Well19937c rng = new Well19937c();
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(new Pair<String, Double>("X", 1.0));
        
        DiscreteDistribution<String> dist = new DiscreteDistribution<String>(rng, samples);
        
        assertEquals("X", dist.sample());
        assertEquals(1.0, dist.probability("X"), 1e-15);
    }

    // Tests probability for existing value
    @Test
    public void testProbability_existingValue_returnsCorrectProbability() {
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(new Pair<String, Double>("A", 0.25));
        samples.add(new Pair<String, Double>("B", 0.75));
        
        DiscreteDistribution<String> dist = new DiscreteDistribution<String>(samples);
        
        assertEquals(0.25, dist.probability("A"), 1e-15);
        assertEquals(0.75, dist.probability("B"), 1e-15);
    }

    // Tests probability for non-existing value
    @Test
    public void testProbability_nonExistingValue_returnsZero() {
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(new Pair<String, Double>("A", 1.0));
        
        DiscreteDistribution<String> dist = new DiscreteDistribution<String>(samples);
        
        assertEquals(0.0, dist.probability("Z"), 1e-15);
    }

    // Tests probability for null value when singleton is null
    @Test
    public void testProbability_nullValueWithNullSingleton_returnsCorrectProbability() {
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(new Pair<String, Double>(null, 0.5));
        samples.add(new Pair<String, Double>("B", 0.5));
        
        DiscreteDistribution<String> dist = new DiscreteDistribution<String>(samples);
        
        assertEquals(0.5, dist.probability(null), 1e-15);
    }

    // Tests probability for null value when singleton is not null
    @Test
    public void testProbability_nullValueWithNonNullSingleton_returnsZero() {
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(new Pair<String, Double>("A", 1.0));
        
        DiscreteDistribution<String> dist = new DiscreteDistribution<String>(samples);
        
        assertEquals(0.0, dist.probability(null), 1e-15);
    }

    // Tests getSamples returns normalized probabilities
    @Test
    public void testGetSamples_returnsNormalizedProbabilities() {
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(new Pair<String, Double>("A", 0.2));
        samples.add(new Pair<String, Double>("B", 0.3));
        samples.add(new Pair<String, Double>("C", 0.5));
        
        DiscreteDistribution<String> dist = new DiscreteDistribution<String>(samples);
        
        List<Pair<String, Double>> result = dist.getSamples();
        assertEquals(3, result.size());
        assertEquals("A", result.get(0).getKey());
        assertEquals(0.2, result.get(0).getValue(), 1e-15);
        assertEquals("B", result.get(1).getKey());
        assertEquals(0.3, result.get(1).getValue(), 1e-15);
        assertEquals("C", result.get(2).getKey());
        assertEquals(0.5, result.get(2).getValue(), 1e-15);
    }

    // Tests getSamples normalizes probabilities that don't sum to 1
    @Test
    public void testGetSamples_nonNormalizedInput_returnsNormalizedProbabilities() {
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(new Pair<String, Double>("A", 0.2));
        samples.add(new Pair<String, Double>("B", 0.6));
        samples.add(new Pair<String, Double>("C", 0.2));
        
        DiscreteDistribution<String> dist = new DiscreteDistribution<String>(samples);
        
        List<Pair<String, Double>> result = dist.getSamples();
        assertEquals(3, result.size());
        assertEquals(0.2, result.get(0).getValue(), 1e-15);
        assertEquals(0.6, result.get(1).getValue(), 1e-15);
        assertEquals(0.2, result.get(2).getValue(), 1e-15);
    }

    // Tests sample method generates a sample from the distribution
    @Test
    public void testSample_singleCall_returnsExpectedValue() {
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(new Pair<String, Double>("A", 0.5));
        samples.add(new Pair<String, Double>("B", 0.5));
        
        DiscreteDistribution<String> dist = new DiscreteDistribution<String>(samples);
        dist.reseedRandomGenerator(12345L);
        
        String sample = dist.sample();
        assertTrue(sample.equals("A") || sample.equals("B"));
    }

    // Tests sample with size returns array of requested size
    @Test
    public void testSample_withSize_returnsArrayOfCorrectLength() {
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(new Pair<String, Double>("A", 0.5));
        samples.add(new Pair<String, Double>("B", 0.5));
        
        DiscreteDistribution<String> dist = new DiscreteDistribution<String>(samples);
        dist.reseedRandomGenerator(12345L);
        
        String[] result = dist.sample(10);
        assertEquals(10, result.length);
        for (String s : result) {
            assertTrue(s.equals("A") || s.equals("B"));
        }
    }

    // Tests sample with size 1
    @Test
    public void testSample_sizeOne_returnsSingleElementArray() {
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(new Pair<String, Double>("A", 1.0));
        
        DiscreteDistribution<String> dist = new DiscreteDistribution<String>(samples);
        dist.reseedRandomGenerator(12345L);
        
        String[] result = dist.sample(1);
        assertEquals(1, result.length);
        assertEquals("A", result[0]);
    }

    // Tests sample with size 0 throws exception
    @Test(expected = NotStrictlyPositiveException.class)
    public void testSample_zeroSize_throwsException() {
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(new Pair<String, Double>("A", 1.0));
        
        DiscreteDistribution<String> dist = new DiscreteDistribution<String>(samples);
        
        dist.sample(0);
    }

    // Tests sample with negative size throws exception
    @Test(expected = NotStrictlyPositiveException.class)
    public void testSample_negativeSize_throwsException() {
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(new Pair<String, Double>("A", 1.0));
        
        DiscreteDistribution<String> dist = new DiscreteDistribution<String>(samples);
        
        dist.sample(-5);
    }

    // Tests reseedRandomGenerator changes the random sequence
    @Test
    public void testReseedRandomGenerator_sameSeed_producesSameSequence() {
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(new Pair<String, Double>("A", 0.5));
        samples.add(new Pair<String, Double>("B", 0.5));
        
        DiscreteDistribution<String> dist1 = new DiscreteDistribution<String>(samples);
        DiscreteDistribution<String> dist2 = new DiscreteDistribution<String>(samples);
        
        dist1.reseedRandomGenerator(42L);
        dist2.reseedRandomGenerator(42L);
        
        assertEquals(dist1.sample(), dist2.sample());
        assertEquals(dist1.sample(), dist2.sample());
        assertEquals(dist1.sample(), dist2.sample());
    }

    // Tests probability for a value that appears multiple times sums probabilities
    @Test
    public void testProbability_duplicateValues_sumsProbabilities() {
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(new Pair<String, Double>("A", 0.1));
        samples.add(new Pair<String, Double>("B", 0.3));
        samples.add(new Pair<String, Double>("A", 0.2));
        samples.add(new Pair<String, Double>("C", 0.4));
        
        DiscreteDistribution<String> dist = new DiscreteDistribution<String>(samples);
        
        assertEquals(0.3, dist.probability("A"), 1e-15);
        assertEquals(0.3, dist.probability("B"), 1e-15);
        assertEquals(0.4, dist.probability("C"), 1e-15);
    }

    // Tests sample with custom RNG produces deterministic results
    @Test
    public void testSample_customRng_returnsExpectedValue() {
        Well19937c rng = new Well19937c();
        rng.setSeed(123L);
        
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(new Pair<String, Double>("A", 0.2));
        samples.add(new Pair<String, Double>("B", 0.3));
        samples.add(new Pair<String, Double>("C", 0.5));
        
        DiscreteDistribution<String> dist = new DiscreteDistribution<String>(rng, samples);
        
        String sample1 = dist.sample();
        String sample2 = dist.sample();
        assertTrue(sample1.equals("A") || sample1.equals("B") || sample1.equals("C"));
        assertTrue(sample2.equals("A") || sample2.equals("B") || sample2.equals("C"));
    }
}