package org.apache.commons.math.optimization.fitting;

import org.apache.commons.math.exception.NullArgumentException;
import org.apache.commons.math.exception.NumberIsTooSmallException;
import org.apache.commons.math.optimization.general.LevenbergMarquardtOptimizer;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class GaussianFitterTest {

    // Tests ParameterGuesser constructor with null input
    @Test(expected = NullArgumentException.class)
    public void testParameterGuesser_nullInput_throwsNullArgumentException() {
        new GaussianFitter.ParameterGuesser(null);
    }

    // Tests ParameterGuesser constructor with less than 3 points
    @Test(expected = NumberIsTooSmallException.class)
    public void testParameterGuesser_tooFewPoints_throwsNumberIsTooSmallException() {
        WeightedObservedPoint[] points = new WeightedObservedPoint[] {
            new WeightedObservedPoint(1.0, 0.0, 1.0),
            new WeightedObservedPoint(1.0, 1.0, 2.0)
        };
        new GaussianFitter.ParameterGuesser(points);
    }

    // Tests ParameterGuesser with exactly 3 points (boundary case)
    @Test
    public void testParameterGuesser_threePoints_returnsGuessedParameters() {
        WeightedObservedPoint[] points = new WeightedObservedPoint[] {
            new WeightedObservedPoint(1.0, 1.0, 10.0),
            new WeightedObservedPoint(1.0, 2.0, 50.0),
            new WeightedObservedPoint(1.0, 3.0, 10.0)
        };
        GaussianFitter.ParameterGuesser guesser = new GaussianFitter.ParameterGuesser(points);
        double[] params = guesser.guess();

        assertNotNull(params);
        assertEquals(3, params.length);
        assertEquals(50.0, params[0], 1e-6); // Peak norm/height
        assertEquals(2.0, params[1], 1e-6);  // Mean x position
        assertTrue(params[2] > 0.0);          // Sigma
    }

    // Tests ParameterGuesser with unsorted points and comparator ordering
    @Test
    public void testParameterGuesser_unsortedPoints_sortsAndGuessesCorrectly() {
        WeightedObservedPoint[] points = new WeightedObservedPoint[] {
            new WeightedObservedPoint(1.0, 3.0, 2.0),
            new WeightedObservedPoint(1.0, 1.0, 2.0),
            new WeightedObservedPoint(1.0, 2.0, 8.0),
            new WeightedObservedPoint(2.0, 2.0, 8.0) // duplicate x with higher weight
        };
        GaussianFitter.ParameterGuesser guesser = new GaussianFitter.ParameterGuesser(points);
        double[] params = guesser.guess();

        assertNotNull(params);
        assertEquals(8.0, params[0], 1e-6);
        assertEquals(2.0, params[1], 1e-6);
        assertTrue(params[2] > 0.0);
    }

    // Tests ParameterGuesser caching behavior on repeated guess calls
    @Test
    public void testParameterGuesser_repeatedGuessCalls_returnsEqualArrays() {
        WeightedObservedPoint[] points = new WeightedObservedPoint[] {
            new WeightedObservedPoint(1.0, -1.0, 1.0),
            new WeightedObservedPoint(1.0, 0.0, 10.0),
            new WeightedObservedPoint(1.0, 1.0, 1.0)
        };
        GaussianFitter.ParameterGuesser guesser = new GaussianFitter.ParameterGuesser(points);
        double[] first = guesser.guess();
        double[] second = guesser.guess();

        assertEquals(first[0], second[0], 1e-12);
        assertEquals(first[1], second[1], 1e-12);
        assertEquals(first[2], second[2], 1e-12);
    }

    // Tests fit with explicit initial guess on ideal Gaussian data
    @Test
    public void testFit_withInitialGuess_convergesToExpectedParameters() {
        GaussianFitter fitter = new GaussianFitter(new LevenbergMarquardtOptimizer());

        // Ideal Gaussian: norm = 4.0, mean = 2.0, sigma = 1.0
        // y = norm * exp(- (x - mean)^2 / (2 * sigma^2))
        for (double x = -1.0; x <= 5.0; x += 0.5) {
            double y = 4.0 * Math.exp(-Math.pow(x - 2.0, 2.0) / 2.0);
            fitter.addObservedPoint(1.0, x, y);
        }

        double[] initialGuess = new double[] { 3.5, 1.8, 1.2 };
        double[] result = fitter.fit(initialGuess);

        assertNotNull(result);
        assertEquals(4.0, result[0], 1e-4);
        assertEquals(2.0, result[1], 1e-4);
        assertEquals(1.0, result[2], 1e-4);
    }

    // Tests fit with invalid/negative sigma in custom function evaluation path
    @Test
    public void testFit_withNegativeSigmaInitialGuess_handlesPositiveInfinityGracefully() {
        GaussianFitter fitter = new GaussianFitter(new LevenbergMarquardtOptimizer());

        for (double x = 0.0; x <= 4.0; x += 0.5) {
            double y = 2.0 * Math.exp(-Math.pow(x - 2.0, 2.0) / 2.0);
            fitter.addObservedPoint(1.0, x, y);
        }

        // Initial guess with non-strictly positive sigma (-1.0) to test exception catch in custom function
        double[] initialGuess = new double[] { 2.0, 2.0, -1.0 };
        try {
            fitter.fit(initialGuess);
        } catch (Exception e) {
            // Optimizer may fail to converge from negative sigma, but should not propagate NotStrictlyPositiveException uncaught
            assertTrue(e.getClass().getName().contains("Math") || e.getClass().getName().contains("Convergence"));
        }
    }

    // Tests automatic fit() without initial guess on standard dataset
    @Test
    public void testFit_withoutInitialGuess_convergesUsingParameterGuesser() {
        GaussianFitter fitter = new GaussianFitter(new LevenbergMarquardtOptimizer());

        double norm = 10.0;
        double mean = 5.0;
        double sigma = 2.0;

        for (double x = 0.0; x <= 10.0; x += 1.0) {
            double y = norm * Math.exp(-Math.pow(x - mean, 2.0) / (2.0 * sigma * sigma));
            fitter.addObservedPoint(1.0, x, y);
        }

        double[] result = fitter.fit();

        assertNotNull(result);
        assertEquals(norm, result[0], 1e-2);
        assertEquals(mean, result[1], 1e-2);
        assertEquals(sigma, result[2], 1e-2);
    }

    // Tests Math-58 regression: fit() should handle datasets where optimizer probes negative sigma
    @Test
    public void testFit_math58Dataset_fitsWithoutNotStrictlyPositiveException() {
        GaussianFitter fitter = new GaussianFitter(new LevenbergMarquardtOptimizer());

        fitter.addObservedPoint(4.0254623,  531026.0);
        fitter.addObservedPoint(4.03128248, 984167.0);
        fitter.addObservedPoint(4.03839603, 1887233.0);
        fitter.addObservedPoint(4.04421621, 2687152.0);
        fitter.addObservedPoint(4.05132976, 3461228.0);
        fitter.addObservedPoint(4.05326982, 3580526.0);
        fitter.addObservedPoint(4.05779662, 3439750.0);
        fitter.addObservedPoint(4.0636168,  2877648.0);
        fitter.addObservedPoint(4.06943698, 2175960.0);
        fitter.addObservedPoint(4.07525716, 1447024.0);
        fitter.addObservedPoint(4.08237071, 717104.0);
        fitter.addObservedPoint(4.08366408, 620014.0);

        double[] parameters = fitter.fit();

        assertNotNull(parameters);
        assertEquals(3, parameters.length);
        assertTrue(parameters[0] > 0.0); // Norm
        assertEquals(4.05, parameters[1], 0.02); // Mean
        assertTrue(parameters[2] > 0.0); // Sigma
    }
}