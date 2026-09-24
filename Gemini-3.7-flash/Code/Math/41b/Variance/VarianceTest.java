package org.apache.commons.math.stat.descriptive.moment;

import org.apache.commons.math.exception.NullArgumentException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class VarianceTest {

    private Variance variance;
    private static final double TOLERANCE = 1e-10;

    @Before
    public void setUp() {
        variance = new Variance();
    }

    // Tests empty state returns NaN
    @Test
    public void testGetResult_emptyState_returnsNaN() {
        assertTrue(Double.isNaN(variance.getResult()));
        assertEquals(0, variance.getN());
    }

    // Tests single value returns zero
    @Test
    public void testIncrementAndGetResult_singleValue_returnsZero() {
        variance.increment(5.0);
        assertEquals(1, variance.getN());
        assertEquals(0.0, variance.getResult(), TOLERANCE);
    }

    // Tests incremental update with multiple values (bias corrected by default)
    @Test
    public void testIncrementAndGetResult_multipleValuesSample_returnsCorrectVariance() {
        double[] values = {1.0, 2.0, 3.0, 4.0, 5.0};
        for (double v : values) {
            variance.increment(v);
        }
        assertEquals(5, variance.getN());
        // Sample variance for 1,2,3,4,5 is 2.5
        assertEquals(2.5, variance.getResult(), TOLERANCE);
    }

    // Tests incremental update with population variance (bias corrected = false)
    @Test
    public void testIncrementAndGetResult_populationVariance_returnsCorrectVariance() {
        variance.setBiasCorrected(false);
        assertFalse(variance.isBiasCorrected());
        double[] values = {1.0, 2.0, 3.0, 4.0, 5.0};
        for (double v : values) {
            variance.increment(v);
        }
        // Population variance for 1,2,3,4,5 is 2.0
        assertEquals(2.0, variance.getResult(), TOLERANCE);
    }

    // Tests constructor with external SecondMoment
    @Test
    public void testConstructor_externalSecondMoment_doesNotIncrementDirectly() {
        SecondMoment m2 = new SecondMoment();
        Variance externalVariance = new Variance(m2);
        externalVariance.increment(10.0);
        // increment on Variance does nothing when external moment is supplied
        assertEquals(0, externalVariance.getN());

        m2.increment(1.0);
        m2.increment(3.0);
        assertEquals(2, externalVariance.getN());
        assertEquals(2.0, externalVariance.getResult(), TOLERANCE);

        externalVariance.clear(); // does nothing when incMoment is false
        assertEquals(2, externalVariance.getN());
    }

    // Tests constructor with biasCorrection and external SecondMoment
    @Test
    public void testConstructor_biasCorrectedAndSecondMoment() {
        SecondMoment m2 = new SecondMoment();
        Variance externalVariance = new Variance(false, m2);
        assertFalse(externalVariance.isBiasCorrected());
        m2.increment(1.0);
        m2.increment(3.0);
        // Population variance for 1, 3 is ((1-2)^2 + (3-2)^2)/2 = 1.0
        assertEquals(1.0, externalVariance.getResult(), TOLERANCE);
    }

    // Tests clear method resets state
    @Test
    public void testClear_afterIncrements_resetsToInitial() {
        variance.increment(10.0);
        variance.increment(20.0);
        variance.clear();
        assertEquals(0, variance.getN());
        assertTrue(Double.isNaN(variance.getResult()));
    }

    // Tests null input array throws NullArgumentException
    @Test(expected = NullArgumentException.class)
    public void testEvaluate_nullArray_throwsException() {
        variance.evaluate((double[]) null);
    }

    // Tests evaluate on empty array returns NaN
    @Test
    public void testEvaluate_emptyArray_returnsNaN() {
        double[] values = new double[0];
        assertTrue(Double.isNaN(variance.evaluate(values)));
        assertTrue(Double.isNaN(variance.evaluate(values, 0, 0)));
    }

    // Tests evaluate on single value array returns 0.0
    @Test
    public void testEvaluate_singleElementArray_returnsZero() {
        double[] values = {42.0};
        assertEquals(0.0, variance.evaluate(values), TOLERANCE);
        assertEquals(0.0, variance.evaluate(values, 0, 1), TOLERANCE);
    }

    // Tests evaluate array with full array and subarray
    @Test
    public void testEvaluate_arrayAndSubarray_returnsCorrectVariance() {
        double[] values = {10.0, 1.0, 2.0, 3.0, 4.0, 5.0, 20.0};
        // Subarray of 1,2,3,4,5 starting at index 1 with length 5
        assertEquals(2.5, variance.evaluate(values, 1, 5), TOLERANCE);
    }

    // Tests evaluate with precomputed mean
    @Test
    public void testEvaluate_withPrecomputedMean_returnsCorrectVariance() {
        double[] values = {1.0, 2.0, 3.0, 4.0, 5.0};
        double mean = 3.0;
        assertEquals(2.5, variance.evaluate(values, mean), TOLERANCE);
        assertEquals(2.5, variance.evaluate(values, mean, 0, 5), TOLERANCE);

        // Single element with precomputed mean
        assertEquals(0.0, variance.evaluate(values, mean, 0, 1), TOLERANCE);

        // Population variance with precomputed mean
        variance.setBiasCorrected(false);
        assertEquals(2.0, variance.evaluate(values, mean), TOLERANCE);
        assertEquals(2.0, variance.evaluate(values, mean, 0, 5), TOLERANCE);
    }

    // Tests weighted evaluate on full array
    @Test
    public void testEvaluate_weightedFullArray_returnsCorrectVariance() {
        double[] values = {1.0, 2.0, 3.0};
        double[] weights = {1.0, 2.0, 1.0};
        // weightedMean = (1*1 + 2*2 + 3*1) / (1+2+1) = 8/4 = 2.0
        // sumWts = 4
        // accum = 1*(1-2)^2 + 2*(2-2)^2 + 1*(3-2)^2 = 1 + 0 + 1 = 2.0
        // accum2 = 1*(1-2) + 2*(2-2) + 1*(3-2) = -1 + 0 + 1 = 0
        // var = 2.0 / (4 - 1) = 2.0 / 3.0
        double expected = 2.0 / 3.0;
        assertEquals(expected, variance.evaluate(values, weights), TOLERANCE);
        assertEquals(expected, variance.evaluate(values, weights, 2.0), TOLERANCE);
    }

    // Tests weighted evaluate on subarray (targets Defects4J Math-41 bug where weights loop summed entire array)
    @Test
    public void testEvaluate_weightedSubarray_returnsCorrectVariance() {
        double[] values = {100.0, 100.0, 1.0, 2.0, 3.0, 99.0};
        double[] weights = {10.0, 10.0, 1.0, 2.0, 1.0, 10.0};
        int begin = 2;
        int length = 3;

        double[] subValues = {1.0, 2.0, 3.0};
        double[] subWeights = {1.0, 2.0, 1.0};

        double expected = variance.evaluate(subValues, subWeights);
        double actual = variance.evaluate(values, weights, begin, length);
        assertEquals(expected, actual, TOLERANCE);

        // Also test evaluate with precomputed mean on subarray
        double mean = 2.0;
        double actualWithMean = variance.evaluate(values, weights, mean, begin, length);
        assertEquals(expected, actualWithMean, TOLERANCE);
    }

    // Tests weighted evaluate on single element returns zero
    @Test
    public void testEvaluate_weightedSingleElement_returnsZero() {
        double[] values = {5.0};
        double[] weights = {2.0};
        assertEquals(0.0, variance.evaluate(values, weights), TOLERANCE);
        assertEquals(0.0, variance.evaluate(values, weights, 5.0), TOLERANCE);
        assertEquals(0.0, variance.evaluate(values, weights, 0, 1), TOLERANCE);
        assertEquals(0.0, variance.evaluate(values, weights, 5.0, 0, 1), TOLERANCE);
    }

    // Tests copy constructor and copy method
    @Test
    public void testCopy_createsIdenticalAndIndependentCopy() {
        variance.setBiasCorrected(false);
        variance.increment(10.0);
        variance.increment(20.0);

        Variance copy = variance.copy();
        assertNotNull(copy);
        assertEquals(variance.getN(), copy.getN());
        assertEquals(variance.isBiasCorrected(), copy.isBiasCorrected());
        assertEquals(variance.getResult(), copy.getResult(), TOLERANCE);

        // Mutating original does not affect copy
        variance.increment(30.0);
        assertFalse(variance.getResult() == copy.getResult());

        // Copy constructor
        Variance copy2 = new Variance(copy);
        assertEquals(copy.getN(), copy2.getN());
        assertEquals(copy.getResult(), copy2.getResult(), TOLERANCE);
    }
}