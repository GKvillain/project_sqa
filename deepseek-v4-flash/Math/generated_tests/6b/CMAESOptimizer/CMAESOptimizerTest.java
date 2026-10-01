package org.apache.commons.math3.optim.nonlinear.scalar.noderiv;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * JUnit 4 tests for CMAESOptimizer.
 * Covers constructor validation, parameter validation, and basic optimization behavior.
 */
public class CMAESOptimizerTest {

    /**
     * Test Sigma constructor with non-negative values.
     */
    @Test
    public void testSigma_NonNegative_Ok() {
        new CMAESOptimizer.Sigma(new double[] {0.5, 1.0});
    }

    /**
     * Test Sigma constructor with negative value throws NotPositiveException.
     */
    @Test(expected = org.apache.commons.math3.exception.NotPositiveException.class)
    public void testSigma_Negative_ThrowsNotPositiveException() {
        new CMAESOptimizer.Sigma(new double[] {-0.5});
    }

    /**
     * Test that Sigma.getSigma returns a copy (modification does not affect internal state).
     */
    @Test
    public void testSigma_getSigma_ReturnsCopy() {
        double[] sigma = {0.5, 1.0};
        CMAESOptimizer.Sigma s = new CMAESOptimizer.Sigma(sigma);
        double[] result = s.getSigma();
        result[0] = 10.0;
        double[] original = s.getSigma();
        assertEquals(0.5, original[0], 1e-15);
    }

    /**
     * Test PopulationSize constructor with positive value.
     */
    @Test
    public void testPopulationSize_Positive_Ok() {
        new CMAESOptimizer.PopulationSize(10);
    }

    /**
     * Test PopulationSize constructor with zero throws NotStrictlyPositiveException.
     */
    @Test(expected = org.apache.commons.math3.exception.NotStrictlyPositiveException.class)
    public void testPopulationSize_Zero_ThrowsNotStrictlyPositiveException() {
        new CMAESOptimizer.PopulationSize(0);
    }

    /**
     * Test optimize with sigma exceeding bounds throws OutOfRangeException.
     */
    @Test(expected = org.apache.commons.math3.exception.OutOfRangeException.class)
    public void testOptimize_SigmaOutOfBounds_ThrowsOutOfRangeException() {
        org.apache.commons.math3.random.JDKRandomGenerator rng = new org.apache.commons.math3.random.JDKRandomGenerator();
        rng.setSeed(1);
        CMAESOptimizer optimizer = new CMAESOptimizer(100, 1e-9, true, 0, 0, rng, false, null);
        org.apache.commons.math3.analysis.MultivariateFunction fun = point -> point[0] * point[0];
        org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction obj = new org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction(fun);
        org.apache.commons.math3.optim.InitialGuess init = new org.apache.commons.math3.optim.InitialGuess(new double[] {1.0});
        org.apache.commons.math3.optim.SimpleBounds bounds = new org.apache.commons.math3.optim.SimpleBounds(
            new double[] {-1.0}, new double[] {1.0});
        org.apache.commons.math3.optim.nonlinear.scalar.GoalType goal = org.apache.commons.math3.optim.nonlinear.scalar.GoalType.MINIMIZE;
        CMAESOptimizer.Sigma sigma = new CMAESOptimizer.Sigma(new double[] {2.5}); // > (1-(-1)) = 2
        CMAESOptimizer.PopulationSize pop = new CMAESOptimizer.PopulationSize(5);
        optimizer.optimize(obj, init, goal, bounds, sigma, pop);
    }

    /**
     * Test optimize with sigma dimension mismatch throws DimensionMismatchException.
     */
    @Test(expected = org.apache.commons.math3.exception.DimensionMismatchException.class)
    public void testOptimize_SigmaDimensionMismatch_ThrowsDimensionMismatchException() {
        org.apache.commons.math3.random.JDKRandomGenerator rng = new org.apache.commons.math3.random.JDKRandomGenerator();
        rng.setSeed(1);
        CMAESOptimizer optimizer = new CMAESOptimizer(100, 1e-9, true, 0, 0, rng, false, null);
        org.apache.commons.math3.analysis.MultivariateFunction fun = point -> point[0] * point[0];
        org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction obj = new org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction(fun);
        org.apache.commons.math3.optim.InitialGuess init = new org.apache.commons.math3.optim.InitialGuess(new double[] {1.0, 2.0});
        org.apache.commons.math3.optim.SimpleBounds bounds = new org.apache.commons.math3.optim.SimpleBounds(
            new double[] {-10.0, -10.0}, new double[] {10.0, 10.0});
        org.apache.commons.math3.optim.nonlinear.scalar.GoalType goal = org.apache.commons.math3.optim.nonlinear.scalar.GoalType.MINIMIZE;
        CMAESOptimizer.Sigma sigma = new CMAESOptimizer.Sigma(new double[] {0.5}); // wrong length
        CMAESOptimizer.PopulationSize pop = new CMAESOptimizer.PopulationSize(5);
        optimizer.optimize(obj, init, goal, bounds, sigma, pop);
    }

    /**
     * Test simple quadratic minimization (x^2 + y^2) converges near (0,0).
     */
    @Test
    public void testOptimize_SimpleQuadratic_FindsMinimum() {
        org.apache.commons.math3.random.JDKRandomGenerator rng = new org.apache.commons.math3.random.JDKRandomGenerator();
        rng.setSeed(42);
        CMAESOptimizer optimizer = new CMAESOptimizer(200, 1e-9, true, 0, 0, rng, false, null);
        org.apache.commons.math3.analysis.MultivariateFunction fun = point -> point[0] * point[0] + point[1] * point[1];
        org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction obj = new org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction(fun);
        double[] guess = {1.0, 1.0};
        org.apache.commons.math3.optim.InitialGuess init = new org.apache.commons.math3.optim.InitialGuess(guess);
        org.apache.commons.math3.optim.SimpleBounds bounds = new org.apache.commons.math3.optim.SimpleBounds(
            new double[] {-10.0, -10.0}, new double[] {10.0, 10.0});
        org.apache.commons.math3.optim.nonlinear.scalar.GoalType goal = org.apache.commons.math3.optim.nonlinear.scalar.GoalType.MINIMIZE;
        CMAESOptimizer.Sigma sigma = new CMAESOptimizer.Sigma(new double[] {0.5, 0.5});
        CMAESOptimizer.PopulationSize pop = new CMAESOptimizer.PopulationSize(10);
        org.apache.commons.math3.optim.PointValuePair result = optimizer.optimize(obj, init, goal, bounds, sigma, pop);
        double[] point = result.getPoint();
        double value = result.getValue();
        assertEquals(0.0, point[0], 1e-3);
        assertEquals(0.0, point[1], 1e-3);
        assertEquals(0.0, value, 1e-3);
    }

    /**
     * Test that the optimizer respects bounds (initial guess out of bounds is repaired).
     */
    @Test
    public void testOptimize_InitialGuessOutOfBounds_StaysInBounds() {
        org.apache.commons.math3.random.JDKRandomGenerator rng = new org.apache.commons.math3.random.JDKRandomGenerator();
        rng.setSeed(7);
        CMAESOptimizer optimizer = new CMAESOptimizer(100, 1e-9, true, 0, 0, rng, false, null);
        org.apache.commons.math3.analysis.MultivariateFunction fun = point -> point[0] * point[0] + point[1] * point[1];
        org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction obj = new org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction(fun);
        double[] guess = {1.0, 1.0};
        org.apache.commons.math3.optim.InitialGuess init = new org.apache.commons.math3.optim.InitialGuess(guess);
        // Bounds are extremely narrow
        org.apache.commons.math3.optim.SimpleBounds bounds = new org.apache.commons.math3.optim.SimpleBounds(
            new double[] {-0.2, -0.2}, new double[] {0.2, 0.2});
        org.apache.commons.math3.optim.nonlinear.scalar.GoalType goal = org.apache.commons.math3.optim.nonlinear.scalar.GoalType.MINIMIZE;
        CMAESOptimizer.Sigma sigma = new CMAESOptimizer.Sigma(new double[] {0.1, 0.1});
        CMAESOptimizer.PopulationSize pop = new CMAESOptimizer.PopulationSize(10);
        org.apache.commons.math3.optim.PointValuePair result = optimizer.optimize(obj, init, goal, bounds, sigma, pop);
        double[] point = result.getPoint();
        for (int i = 0; i < point.length; i++) {
            assertTrue(point[i] >= -0.2 - 1e-6);
            assertTrue(point[i] <= 0.2 + 1e-6);
        }
    }

    /**
     * Test maximization of -x^2 (should approach 0).
     */
    @Test
    public void testOptimize_MaximizeGoalType_FindsMaximum() {
        org.apache.commons.math3.random.JDKRandomGenerator rng = new org.apache.commons.math3.random.JDKRandomGenerator();
        rng.setSeed(11);
        CMAESOptimizer optimizer = new CMAESOptimizer(100, 1e-9, true, 0, 0, rng, false, null);
        org.apache.commons.math3.analysis.MultivariateFunction fun = point -> -point[0] * point[0];
        org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction obj = new org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction(fun);
        org.apache.commons.math3.optim.InitialGuess init = new org.apache.commons.math3.optim.InitialGuess(new double[] {1.0});
        org.apache.commons.math3.optim.SimpleBounds bounds = new org.apache.commons.math3.optim.SimpleBounds(
            new double[] {-10.0}, new double[] {10.0});
        org.apache.commons.math3.optim.nonlinear.scalar.GoalType goal = org.apache.commons.math3.optim.nonlinear.scalar.GoalType.MAXIMIZE;
        CMAESOptimizer.Sigma sigma = new CMAESOptimizer.Sigma(new double[] {0.5});
        CMAESOptimizer.PopulationSize pop = new CMAESOptimizer.PopulationSize(10);
        org.apache.commons.math3.optim.PointValuePair result = optimizer.optimize(obj, init, goal, bounds, sigma, pop);
        double[] point = result.getPoint();
        assertEquals(0.0, point[0], 1e-3);
    }

    /**
     * Test optimization with diagonalOnly mode.
     */
    @Test
    public void testOptimize_DiagonalOnly_Works() {
        org.apache.commons.math3.random.JDKRandomGenerator rng = new org.apache.commons.math3.random.JDKRandomGenerator();
        rng.setSeed(5);
        // diagonalOnly = 1: keep covariance diagonal always
        CMAESOptimizer optimizer = new CMAESOptimizer(100, 1e-9, true, 1, 0, rng, false, null);
        org.apache.commons.math3.analysis.MultivariateFunction fun = point -> point[0] * point[0] + point[1] * point[1];
        org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction obj = new org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction(fun);
        org.apache.commons.math3.optim.InitialGuess init = new org.apache.commons.math3.optim.InitialGuess(new double[] {1.0, 1.0});
        org.apache.commons.math3.optim.SimpleBounds bounds = new org.apache.commons.math3.optim.SimpleBounds(
            new double[] {-10.0, -10.0}, new double[] {10.0, 10.0});
        org.apache.commons.math3.optim.nonlinear.scalar.GoalType goal = org.apache.commons.math3.optim.nonlinear.scalar.GoalType.MINIMIZE;
        CMAESOptimizer.Sigma sigma = new CMAESOptimizer.Sigma(new double[] {0.5, 0.5});
        CMAESOptimizer.PopulationSize pop = new CMAESOptimizer.PopulationSize(10);
        org.apache.commons.math3.optim.PointValuePair result = optimizer.optimize(obj, init, goal, bounds, sigma, pop);
        double[] point = result.getPoint();
        assertEquals(0.0, point[0], 1e-3);
        assertEquals(0.0, point[1], 1e-3);
    }

    /**
     * Test optimization with non-active CMA (isActiveCMA = false).
     */
    @Test
    public void testOptimize_NonActiveCMA_Works() {
        org.apache.commons.math3.random.JDKRandomGenerator rng = new org.apache.commons.math3.random.JDKRandomGenerator();
        rng.setSeed(9);
        CMAESOptimizer optimizer = new CMAESOptimizer(100, 1e-9, false, 0, 0, rng, false, null);
        org.apache.commons.math3.analysis.MultivariateFunction fun = point -> point[0] * point[0];
        org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction obj = new org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction(fun);
        org.apache.commons.math3.optim.InitialGuess init = new org.apache.commons.math3.optim.InitialGuess(new double[] {2.0});
        org.apache.commons.math3.optim.SimpleBounds bounds = new org.apache.commons.math3.optim.SimpleBounds(
            new double[] {-10.0}, new double[] {10.0});
        org.apache.commons.math3.optim.nonlinear.scalar.GoalType goal = org.apache.commons.math3.optim.nonlinear.scalar.GoalType.MINIMIZE;
        CMAESOptimizer.Sigma sigma = new CMAESOptimizer.Sigma(new double[] {0.5});
        CMAESOptimizer.PopulationSize pop = new CMAESOptimizer.PopulationSize(10);
        org.apache.commons.math3.optim.PointValuePair result = optimizer.optimize(obj, init, goal, bounds, sigma, pop);
        assertEquals(0.0, result.getPoint()[0], 1e-3);
    }

    /**
     * Test that statistics history lists are initially empty.
     */
    @Test
    public void testGetStatistics_InitiallyEmpty() {
        org.apache.commons.math3.random.JDKRandomGenerator rng = new org.apache.commons.math3.random.JDKRandomGenerator();
        rng.setSeed(2);
        CMAESOptimizer optimizer = new CMAESOptimizer(10, 1e-9, true, 0, 0, rng, false, null);
        assertTrue(optimizer.getStatisticsSigmaHistory().isEmpty());
        assertTrue(optimizer.getStatisticsMeanHistory().isEmpty());
        assertTrue(optimizer.getStatisticsFitnessHistory().isEmpty());
        assertTrue(optimizer.getStatisticsDHistory().isEmpty());
    }

    /**
     * Test that statistics are collected when generateStatistics is true.
     */
    @Test
    public void testGetStatistics_AfterOptimize_NotEmpty() {
        org.apache.commons.math3.random.JDKRandomGenerator rng = new org.apache.commons.math3.random.JDKRandomGenerator();
        rng.setSeed(3);
        CMAESOptimizer optimizer = new CMAESOptimizer(20, 1e-9, true, 0, 0, rng, true, null);
        org.apache.commons.math3.analysis.MultivariateFunction fun = point -> point[0] * point[0];
        org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction obj = new org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction(fun);
        org.apache.commons.math3.optim.InitialGuess init = new org.apache.commons.math3.optim.InitialGuess(new double[] {1.0});
        org.apache.commons.math3.optim.SimpleBounds bounds = new org.apache.commons.math3.optim.SimpleBounds(
            new double[] {-10.0}, new double[] {10.0});
        org.apache.commons.math3.optim.nonlinear.scalar.GoalType goal = org.apache.commons.math3.optim.nonlinear.scalar.GoalType.MINIMIZE;
        CMAESOptimizer.Sigma sigma = new CMAESOptimizer.Sigma(new double[] {0.5});
        CMAESOptimizer.PopulationSize pop = new CMAESOptimizer.PopulationSize(10);
        optimizer.optimize(obj, init, goal, bounds, sigma, pop);
        assertFalse(optimizer.getStatisticsSigmaHistory().isEmpty());
        assertFalse(optimizer.getStatisticsMeanHistory().isEmpty());
        assertFalse(optimizer.getStatisticsFitnessHistory().isEmpty());
        assertFalse(optimizer.getStatisticsDHistory().isEmpty());
    }

    /**
     * Test optimize with maxIterations = 0 returns the initial guess as optimum.
     */
    @Test
    public void testOptimize_MaxIterationsZero_ReturnsInitialPoint() {
        org.apache.commons.math3.random.JDKRandomGenerator rng = new org.apache.commons.math3.random.JDKRandomGenerator();
        rng.setSeed(4);
        CMAESOptimizer optimizer = new CMAESOptimizer(0, 1e-9, true, 0, 0, rng, false, null);
        org.apache.commons.math3.analysis.MultivariateFunction fun = point -> point[0] * point[0];
        org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction obj = new org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction(fun);
        double[] guess = {3.0};
        org.apache.commons.math3.optim.InitialGuess init = new org.apache.commons.math3.optim.InitialGuess(guess);
        org.apache.commons.math3.optim.SimpleBounds bounds = new org.apache.commons.math3.optim.SimpleBounds(
            new double[] {-10.0}, new double[] {10.0});
        org.apache.commons.math3.optim.nonlinear.scalar.GoalType goal = org.apache.commons.math3.optim.nonlinear.scalar.GoalType.MINIMIZE;
        CMAESOptimizer.Sigma sigma = new CMAESOptimizer.Sigma(new double[] {0.5});
        CMAESOptimizer.PopulationSize pop = new CMAESOptimizer.PopulationSize(10);
        org.apache.commons.math3.optim.PointValuePair result = optimizer.optimize(obj, init, goal, bounds, sigma, pop);
        assertArrayEquals(guess, result.getPoint(), 1e-12);
    }
}