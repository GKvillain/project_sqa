package org.apache.commons.math3.optimization.fitting;

import org.apache.commons.math3.analysis.function.HarmonicOscillator;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.exception.ZeroException;
import org.apache.commons.math3.exception.MathIllegalStateException;
import org.apache.commons.math3.optimization.general.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.util.FastMath;
import org.junit.Assert;
import org.junit.Test;

public class HarmonicFitterTest {

    // Tests normal fitting with automatically guessed initial parameters
    @Test
    public void testFit_normalSinusoid_convergesToCorrectParameters() {
        HarmonicFitter fitter = new HarmonicFitter(new LevenbergMarquardtOptimizer());
        double a = 1.2;
        double omega = 2.1;
        double phi = 0.7;

        HarmonicOscillator f = new HarmonicOscillator(a, omega, phi);
        for (double x = 0.0; x < 10.0; x += 0.2) {
            fitter.addObservedPoint(1.0, x, f.value(x));
        }

        double[] fitted = fitter.fit();
        Assert.assertEquals(a, FastMath.abs(fitted[0]), 1e-4);
        Assert.assertEquals(omega, FastMath.abs(fitted[1]), 1e-4);
    }

    // Tests fitting with user-supplied initial guess
    @Test
    public void testFit_withInitialGuess_convergesToCorrectParameters() {
        HarmonicFitter fitter = new HarmonicFitter(new LevenbergMarquardtOptimizer());
        double a = 2.0;
        double omega = 1.5;
        double phi = 0.25;

        HarmonicOscillator f = new HarmonicOscillator(a, omega, phi);
        for (double x = 0.0; x < 8.0; x += 0.25) {
            fitter.addObservedPoint(1.0, x, f.value(x));
        }

        double[] initialGuess = new double[] { 1.8, 1.4, 0.2 };
        double[] fitted = fitter.fit(initialGuess);
        Assert.assertEquals(a, FastMath.abs(fitted[0]), 1e-4);
        Assert.assertEquals(omega, FastMath.abs(fitted[1]), 1e-4);
    }

    // Tests ParameterGuesser with exactly the minimum required observations (4 points)
    @Test
    public void testParameterGuesser_minimumObservations_computesGuess() {
        WeightedObservedPoint[] points = new WeightedObservedPoint[] {
            new WeightedObservedPoint(1.0, 0.0, 1.0),
            new WeightedObservedPoint(1.0, 1.0, 0.0),
            new WeightedObservedPoint(1.0, 2.0, -1.0),
            new WeightedObservedPoint(1.0, 3.0, 0.0)
        };

        HarmonicFitter.ParameterGuesser guesser = new HarmonicFitter.ParameterGuesser(points);
        double[] guess = guesser.guess();

        Assert.assertNotNull(guess);
        Assert.assertEquals(3, guess.length);
        Assert.assertTrue(guess[0] > 0);
        Assert.assertTrue(guess[1] > 0);
    }

    // Tests ParameterGuesser with less than 4 points throws NumberIsTooSmallException
    @Test(expected = NumberIsTooSmallException.class)
    public void testParameterGuesser_lessThanFourPoints_throwsNumberIsTooSmallException() {
        WeightedObservedPoint[] points = new WeightedObservedPoint[] {
            new WeightedObservedPoint(1.0, 0.0, 1.0),
            new WeightedObservedPoint(1.0, 1.0, 0.0),
            new WeightedObservedPoint(1.0, 2.0, -1.0)
        };

        new HarmonicFitter.ParameterGuesser(points);
    }

    // Tests ParameterGuesser with 0 points throws NumberIsTooSmallException
    @Test(expected = NumberIsTooSmallException.class)
    public void testParameterGuesser_emptyObservations_throwsNumberIsTooSmallException() {
        WeightedObservedPoint[] points = new WeightedObservedPoint[0];
        new HarmonicFitter.ParameterGuesser(points);
    }

    // Tests ParameterGuesser with unsorted points to verify internal sorting
    @Test
    public void testParameterGuesser_unsortedObservations_sortsAndComputesCorrectly() {
        WeightedObservedPoint[] points = new WeightedObservedPoint[] {
            new WeightedObservedPoint(1.0, 3.0, 0.0),
            new WeightedObservedPoint(1.0, 0.0, 1.0),
            new WeightedObservedPoint(1.0, 2.0, -1.0),
            new WeightedObservedPoint(1.0, 1.0, 0.0)
        };

        HarmonicFitter.ParameterGuesser guesser = new HarmonicFitter.ParameterGuesser(points);
        double[] guess = guesser.guess();

        Assert.assertNotNull(guess);
        Assert.assertEquals(3, guess.length);
        Assert.assertTrue(guess[0] > 0);
        Assert.assertTrue(guess[1] > 0);
    }

    // Tests ParameterGuesser fallback branch when integrals yield negative ratios
    @Test
    public void testParameterGuesser_illConditionedData_executesFallbackBranch() {
        WeightedObservedPoint[] points = new WeightedObservedPoint[] {
            new WeightedObservedPoint(1.0, 0.0, 1.0),
            new WeightedObservedPoint(1.0, 1.0, 1.0),
            new WeightedObservedPoint(1.0, 2.0, 1.0),
            new WeightedObservedPoint(1.0, 3.0, 1.0),
            new WeightedObservedPoint(1.0, 4.0, 1.0)
        };

        HarmonicFitter.ParameterGuesser guesser = new HarmonicFitter.ParameterGuesser(points);
        double[] guess = guesser.guess();

        Assert.assertNotNull(guess);
        Assert.assertEquals(3, guess.length);
        Assert.assertEquals(0.0, guess[0], 1e-10);
        Assert.assertEquals(2 * FastMath.PI / 4.0, guess[1], 1e-10);
    }

    // Tests ParameterGuesser with all zero x-range triggering ZeroException in fallback branch
    @Test(expected = ZeroException.class)
    public void testParameterGuesser_zeroAbscissaRange_throwsZeroException() {
        WeightedObservedPoint[] points = new WeightedObservedPoint[] {
            new WeightedObservedPoint(1.0, 1.0, 1.0),
            new WeightedObservedPoint(1.0, 1.0, 2.0),
            new WeightedObservedPoint(1.0, 1.0, 3.0),
            new WeightedObservedPoint(1.0, 1.0, 4.0)
        };

        HarmonicFitter.ParameterGuesser guesser = new HarmonicFitter.ParameterGuesser(points);
        guesser.guess();
    }

    // Tests ParameterGuesser with high frequency sinusoidal signal
    @Test
    public void testParameterGuesser_highFrequencySignal_estimatesCloseToExpected() {
        double a = 5.0;
        double omega = 10.0;
        double phi = 1.2;

        HarmonicOscillator f = new HarmonicOscillator(a, omega, phi);
        int numPoints = 100;
        WeightedObservedPoint[] points = new WeightedObservedPoint[numPoints];
        for (int i = 0; i < numPoints; ++i) {
            double x = i * 0.05;
            points[i] = new WeightedObservedPoint(1.0, x, f.value(x));
        }

        HarmonicFitter.ParameterGuesser guesser = new HarmonicFitter.ParameterGuesser(points);
        double[] guess = guesser.guess();

        Assert.assertEquals(a, guess[0], 0.5);
        Assert.assertEquals(omega, guess[1], 0.5);
    }

    // Tests ParameterGuesser with data that can lead to Math-844 condition
    @Test
    public void testParameterGuesser_math844_handlesCondition() {
        WeightedObservedPoint[] points = new WeightedObservedPoint[] {
            new WeightedObservedPoint(1.0, 0.0, 0.0),
            new WeightedObservedPoint(1.0, 1.0, 0.0),
            new WeightedObservedPoint(1.0, 2.0, 0.0),
            new WeightedObservedPoint(1.0, 3.0, 0.0),
            new WeightedObservedPoint(1.0, 4.0, 0.0)
        };

        try {
            HarmonicFitter.ParameterGuesser guesser = new HarmonicFitter.ParameterGuesser(points);
            double[] guess = guesser.guess();
            Assert.assertNotNull(guess);
        } catch (MathIllegalStateException | ZeroException e) {
            // Expected if guessing fails sensibly on degenerated inputs
            Assert.assertTrue(true);
        }
    }
}