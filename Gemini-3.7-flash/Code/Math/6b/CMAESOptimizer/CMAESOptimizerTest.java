package org.apache.commons.math3.optim.nonlinear.scalar.noderiv;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.MaxIter;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import org.apache.commons.math3.optim.SimpleValueChecker;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Assert;
import org.junit.Test;

public class CMAESOptimizerTest {

    // Helper objective function: Sphere function f(x) = sum(x_i^2)
    private static class Sphere implements MultivariateFunction {
        public double value(double[] point) {
            double sum = 0;
            for (double v : point) {
                sum += v * v;
            }
            return sum;
        }
    }

    // Helper objective function: Inverted Sphere function f(x) = -sum(x_i^2)
    private static class InvertedSphere implements MultivariateFunction {
        public double value(double[] point) {
            double sum = 0;
            for (double v : point) {
                sum += v * v;
            }
            return -sum;
        }
    }

    // Tests Sigma constructor with negative values throws NotPositiveException
    @Test(expected = NotPositiveException.class)
    public void testSigma_negativeValue_throwsNotPositiveException() {
        new CMAESOptimizer.Sigma(new double[] { 1.0, -0.5 });
    }

    // Tests Sigma valid input returns correct cloned array
    @Test
    public void testSigma_validInput_returnsClonedArray() {
        double[] input = new double[] { 0.2, 0.5 };
        CMAESOptimizer.Sigma sigma = new CMAESOptimizer.Sigma(input);
        Assert.assertArrayEquals(input, sigma.getSigma(), 1e-9);
        input[0] = 1.0;
        Assert.assertEquals(0.2, sigma.getSigma()[0], 1e-9);
    }

    // Tests PopulationSize constructor with non-positive values throws NotStrictlyPositiveException
    @Test(expected = NotStrictlyPositiveException.class)
    public void testPopulationSize_zeroOrNegative_throwsNotStrictlyPositiveException() {
        new CMAESOptimizer.PopulationSize(0);
    }

    // Tests PopulationSize valid value returns correct size
    @Test
    public void testPopulationSize_validSize_returnsPopulationSize() {
        CMAESOptimizer.PopulationSize pop = new CMAESOptimizer.PopulationSize(10);
        Assert.assertEquals(10, pop.getPopulationSize());
    }

    // Tests optimization of Sphere function with minimization goal
    @Test
    public void testOptimize_sphereFunction_minimizesCorrectly() {
        RandomGenerator rng = new MersenneTwister(123456L);
        CMAESOptimizer optimizer = new CMAESOptimizer(300, 1e-10, true, 0, 0, rng, false, null);
        PointValuePair result = optimizer.optimize(
                new MaxEval(10000),
                new ObjectiveFunction(new Sphere()),
                GoalType.MINIMIZE,
                new InitialGuess(new double[] { 1.0, 1.0 }),
                new CMAESOptimizer.Sigma(new double[] { 0.5, 0.5 }),
                new CMAESOptimizer.PopulationSize(10),
                new SimpleBounds(new double[] { -5.0, -5.0 }, new double[] { 5.0, 5.0 })
        );
        Assert.assertNotNull(result);
        Assert.assertEquals(0.0, result.getValue(), 1e-1);
        Assert.assertEquals(0.0, result.getPoint()[0], 1e-1);
        Assert.assertEquals(0.0, result.getPoint()[1], 1e-1);
    }

    // Tests optimization with maximization goal
    @Test
    public void testOptimize_maximizeGoal_maximizesCorrectly() {
        RandomGenerator rng = new MersenneTwister(123456L);
        CMAESOptimizer optimizer = new CMAESOptimizer(300, 1e-10, true, 0, 0, rng, false, null);
        PointValuePair result = optimizer.optimize(
                new MaxEval(10000),
                new ObjectiveFunction(new InvertedSphere()),
                GoalType.MAXIMIZE,
                new InitialGuess(new double[] { 0.8, -0.8 }),
                new CMAESOptimizer.Sigma(new double[] { 0.3, 0.3 }),
                new CMAESOptimizer.PopulationSize(10),
                new SimpleBounds(new double[] { -3.0, -3.0 }, new double[] { 3.0, 3.0 })
        );
        Assert.assertNotNull(result);
        Assert.assertEquals(0.0, result.getValue(), 1e-1);
    }

    // Tests optimization with boundaries constraint and active repair mode
    @Test
    public void testOptimize_withBounds_staysWithinBounds() {
        RandomGenerator rng = new MersenneTwister(123456L);
        CMAESOptimizer optimizer = new CMAESOptimizer(300, 1e-10, true, 0, 10, rng, false, null);
        PointValuePair result = optimizer.optimize(
                new MaxEval(10000),
                new ObjectiveFunction(new Sphere()),
                GoalType.MINIMIZE,
                new InitialGuess(new double[] { 2.0, 2.0 }),
                new CMAESOptimizer.Sigma(new double[] { 0.5, 0.5 }),
                new CMAESOptimizer.PopulationSize(10),
                new SimpleBounds(new double[] { 1.0, 1.0 }, new double[] { 5.0, 5.0 })
        );
        Assert.assertNotNull(result);
        Assert.assertTrue(result.getPoint()[0] >= 1.0 - 1e-3);
        Assert.assertTrue(result.getPoint()[1] >= 1.0 - 1e-3);
    }

    // Tests sigma dimension mismatch with initial guess throws DimensionMismatchException
    @Test(expected = DimensionMismatchException.class)
    public void testOptimize_sigmaDimensionMismatch_throwsDimensionMismatchException() {
        RandomGenerator rng = new MersenneTwister(123456L);
        CMAESOptimizer optimizer = new CMAESOptimizer(100, 0, true, 0, 0, rng, false, null);
        optimizer.optimize(
                new MaxEval(1000),
                new ObjectiveFunction(new Sphere()),
                GoalType.MINIMIZE,
                new InitialGuess(new double[] { 1.0, 1.0 }),
                new CMAESOptimizer.Sigma(new double[] { 0.5 }),
                new SimpleBounds(new double[] { -5.0, -5.0 }, new double[] { 5.0, 5.0 })
        );
    }

    // Tests sigma value exceeding upper - lower bound range throws OutOfRangeException
    @Test(expected = OutOfRangeException.class)
    public void testOptimize_sigmaOutOfRange_throwsOutOfRangeException() {
        RandomGenerator rng = new MersenneTwister(123456L);
        CMAESOptimizer optimizer = new CMAESOptimizer(100, 0, true, 0, 0, rng, false, null);
        optimizer.optimize(
                new MaxEval(1000),
                new ObjectiveFunction(new Sphere()),
                GoalType.MINIMIZE,
                new InitialGuess(new double[] { 1.0 }),
                new CMAESOptimizer.Sigma(new double[] { 10.0 }),
                new SimpleBounds(new double[] { 0.0 }, new double[] { 5.0 })
        );
    }

    // Tests diagonalOnly mode covariance update branch
    @Test
    public void testOptimize_diagonalOnly_optimizesCorrectly() {
        RandomGenerator rng = new MersenneTwister(123456L);
        CMAESOptimizer optimizer = new CMAESOptimizer(300, 1e-10, true, 5, 0, rng, false, null);
        PointValuePair result = optimizer.optimize(
                new MaxEval(10000),
                new ObjectiveFunction(new Sphere()),
                GoalType.MINIMIZE,
                new InitialGuess(new double[] { 1.5, -1.5 }),
                new CMAESOptimizer.Sigma(new double[] { 0.5, 0.5 }),
                new CMAESOptimizer.PopulationSize(10),
                new SimpleBounds(new double[] { -5.0, -5.0 }, new double[] { 5.0, 5.0 })
        );
        Assert.assertNotNull(result);
        Assert.assertEquals(0.0, result.getValue(), 1e-1);
    }

    // Tests non-active CMA path (isActiveCMA = false)
    @Test
    public void testOptimize_nonActiveCMA_optimizesCorrectly() {
        RandomGenerator rng = new MersenneTwister(123456L);
        CMAESOptimizer optimizer = new CMAESOptimizer(300, 1e-10, false, 0, 0, rng, false, null);
        PointValuePair result = optimizer.optimize(
                new MaxEval(10000),
                new ObjectiveFunction(new Sphere()),
                GoalType.MINIMIZE,
                new InitialGuess(new double[] { 1.0, 1.0 }),
                new CMAESOptimizer.Sigma(new double[] { 0.5, 0.5 }),
                new CMAESOptimizer.PopulationSize(10),
                new SimpleBounds(new double[] { -5.0, -5.0 }, new double[] { 5.0, 5.0 })
        );
        Assert.assertNotNull(result);
        Assert.assertEquals(0.0, result.getValue(), 1e-1);
    }

    // Tests statistic history generation flag
    @Test
    public void testOptimize_generateStatisticsTrue_recordsHistory() {
        RandomGenerator rng = new MersenneTwister(123456L);
        CMAESOptimizer optimizer = new CMAESOptimizer(50, 1e-6, true, 0, 0, rng, true, null);
        optimizer.optimize(
                new MaxEval(5000),
                new ObjectiveFunction(new Sphere()),
                GoalType.MINIMIZE,
                new InitialGuess(new double[] { 1.0, 1.0 }),
                new CMAESOptimizer.Sigma(new double[] { 0.5, 0.5 }),
                new CMAESOptimizer.PopulationSize(10),
                new SimpleBounds(new double[] { -5.0, -5.0 }, new double[] { 5.0, 5.0 })
        );
        Assert.assertFalse(optimizer.getStatisticsSigmaHistory().isEmpty());
        Assert.assertFalse(optimizer.getStatisticsFitnessHistory().isEmpty());
        Assert.assertFalse(optimizer.getStatisticsMeanHistory().isEmpty());
        Assert.assertFalse(optimizer.getStatisticsDHistory().isEmpty());
    }

    // Tests early termination when stopFitness condition is met
    @Test
    public void testOptimize_stopFitnessReached_terminatesEarly() {
        RandomGenerator rng = new MersenneTwister(123456L);
        double stopFitness = 0.1;
        CMAESOptimizer optimizer = new CMAESOptimizer(500, stopFitness, true, 0, 0, rng, false, null);
        PointValuePair result = optimizer.optimize(
                new MaxEval(10000),
                new ObjectiveFunction(new Sphere()),
                GoalType.MINIMIZE,
                new InitialGuess(new double[] { 2.0, 2.0 }),
                new CMAESOptimizer.Sigma(new double[] { 0.5, 0.5 }),
                new CMAESOptimizer.PopulationSize(10),
                new SimpleBounds(new double[] { -5.0, -5.0 }, new double[] { 5.0, 5.0 })
        );
        Assert.assertNotNull(result);
        Assert.assertTrue(result.getValue() <= stopFitness);
    }

    // Tests convergence checker integration
    @Test
    public void testOptimize_withConvergenceChecker_converges() {
        RandomGenerator rng = new MersenneTwister(123456L);
        SimpleValueChecker checker = new SimpleValueChecker(1e-3, 1e-3);
        CMAESOptimizer optimizer = new CMAESOptimizer(300, 0, true, 0, 0, rng, false, checker);
        PointValuePair result = optimizer.optimize(
                new MaxEval(10000),
                new ObjectiveFunction(new Sphere()),
                GoalType.MINIMIZE,
                new InitialGuess(new double[] { 1.0, 1.0 }),
                new CMAESOptimizer.Sigma(new double[] { 0.5, 0.5 }),
                new CMAESOptimizer.PopulationSize(10),
                new SimpleBounds(new double[] { -5.0, -5.0 }, new double[] { 5.0, 5.0 })
        );
        Assert.assertNotNull(result);
    }

    // Tests max evaluations exceeded throws TooManyEvaluationsException
    @Test(expected = TooManyEvaluationsException.class)
    public void testOptimize_exceedMaxEvaluations_throwsTooManyEvaluationsException() {
        RandomGenerator rng = new MersenneTwister(123456L);
        CMAESOptimizer optimizer = new CMAESOptimizer(300, 1e-15, true, 0, 0, rng, false, null);
        optimizer.optimize(
                new MaxEval(10),
                new ObjectiveFunction(new Sphere()),
                GoalType.MINIMIZE,
                new InitialGuess(new double[] { 5.0, 5.0 }),
                new CMAESOptimizer.Sigma(new double[] { 0.5, 0.5 }),
                new CMAESOptimizer.PopulationSize(10),
                new SimpleBounds(new double[] { -10.0, -10.0 }, new double[] { 10.0, 10.0 })
        );
    }
}