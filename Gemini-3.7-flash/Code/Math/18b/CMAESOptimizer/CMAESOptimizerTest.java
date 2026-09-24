package org.apache.commons.math3.optimization.direct;

import java.util.Arrays;
import java.util.List;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MathUnsupportedOperationException;
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.NumberIsTooLargeException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.optimization.GoalType;
import org.apache.commons.math3.optimization.PointValuePair;
import org.apache.commons.math3.optimization.SimpleValueChecker;
import org.apache.commons.math3.random.MersenneTwister;
import org.junit.Assert;
import org.junit.Test;

public class CMAESOptimizerTest {

    // Helper sphere function: f(x) = sum(x_i^2)
    private static class SphereFunction implements MultivariateFunction {
        public double value(double[] point) {
            double sum = 0;
            for (double v : point) {
                sum += v * v;
            }
            return sum;
        }
    }

    // Helper bounded sphere function checking feasibility
    private static class BoundedSphereFunction implements MultivariateFunction {
        private final double lower;
        private final double upper;

        BoundedSphereFunction(double lower, double upper) {
            this.lower = lower;
            this.upper = upper;
        }

        public double value(double[] point) {
            double sum = 0;
            for (double v : point) {
                if (v < lower || v > upper) {
                    throw new IllegalArgumentException("Point out of bounds: " + v);
                }
                sum += (v - 1.0) * (v - 1.0);
            }
            return sum;
        }
    }

    // Tests default constructor and minimization of a simple sphere function
    @Test
    public void testOptimize_sphereFunction_minimizesCorrectly() {
        CMAESOptimizer optimizer = new CMAESOptimizer();
        double[] startPoint = new double[] { 1.0, 2.0 };
        PointValuePair result = optimizer.optimize(10000, new SphereFunction(), GoalType.MINIMIZE, startPoint);

        Assert.assertNotNull(result);
        Assert.assertEquals(0.0, result.getValue(), 1e-1);
        Assert.assertEquals(0.0, result.getPoint()[0], 1e-1);
        Assert.assertEquals(0.0, result.getPoint()[1], 1e-1);
    }

    // Tests optimization with maximization goal
    @Test
    public void testOptimize_sphereFunction_maximizesCorrectly() {
        MultivariateFunction invertedSphere = new MultivariateFunction() {
            public double value(double[] point) {
                return 10.0 - (point[0] * point[0] + point[1] * point[1]);
            }
        };
        CMAESOptimizer optimizer = new CMAESOptimizer(10);
        double[] startPoint = new double[] { 1.5, -1.5 };
        PointValuePair result = optimizer.optimize(10000, invertedSphere, GoalType.MAXIMIZE, startPoint);

        Assert.assertNotNull(result);
        Assert.assertEquals(10.0, result.getValue(), 1e-1);
        Assert.assertEquals(0.0, result.getPoint()[0], 1e-1);
        Assert.assertEquals(0.0, result.getPoint()[1], 1e-1);
    }

    // Tests optimization with boundaries and input sigma
    @Test
    public void testOptimize_withBoundaries_findsOptimalWithinBounds() {
        int dim = 2;
        double[] start = new double[] { 2.0, 2.0 };
        double[] lower = new double[] { 0.0, 0.0 };
        double[] upper = new double[] { 5.0, 5.0 };
        double[] sigma = new double[] { 0.5, 0.5 };

        CMAESOptimizer optimizer = new CMAESOptimizer(10, sigma, 1000, 1e-6, true, 0, 10,
                new MersenneTwister(42), false, new SimpleValueChecker(1e-6, 1e-6));

        PointValuePair result = optimizer.optimize(10000, new BoundedSphereFunction(0.0, 5.0),
                GoalType.MINIMIZE, start, lower, upper);

        Assert.assertNotNull(result);
        Assert.assertEquals(1.0, result.getPoint()[0], 1e-1);
        Assert.assertEquals(1.0, result.getPoint()[1], 1e-1);
        Assert.assertEquals(0.0, result.getValue(), 1e-1);
    }

    // Tests diagonalOnly covariance update branch
    @Test
    public void testOptimize_diagonalOnly_converges() {
        double[] start = new double[] { 2.0, -2.0 };
        double[] sigma = new double[] { 0.5, 0.5 };
        CMAESOptimizer optimizer = new CMAESOptimizer(10, sigma, 1000, 1e-8, true, 5, 0,
                new MersenneTwister(42), false, new SimpleValueChecker(1e-6, 1e-6));

        PointValuePair result = optimizer.optimize(10000, new SphereFunction(), GoalType.MINIMIZE, start);

        Assert.assertNotNull(result);
        Assert.assertEquals(0.0, result.getValue(), 1e-1);
    }

    // Tests non-active CMA path
    @Test
    public void testOptimize_nonActiveCMA_converges() {
        double[] start = new double[] { 1.0, 1.0 };
        double[] sigma = new double[] { 0.3, 0.3 };
        CMAESOptimizer optimizer = new CMAESOptimizer(10, sigma, 1000, 1e-8, false, 0, 0,
                new MersenneTwister(42), false, new SimpleValueChecker(1e-6, 1e-6));

        PointValuePair result = optimizer.optimize(10000, new SphereFunction(), GoalType.MINIMIZE, start);

        Assert.assertNotNull(result);
        Assert.assertEquals(0.0, result.getValue(), 1e-1);
    }

    // Tests statistics generation history collection
    @Test
    public void testOptimize_generateStatistics_collectsHistoryData() {
        double[] start = new double[] { 0.5, -0.5 };
        CMAESOptimizer optimizer = new CMAESOptimizer(8, null, 100, 0, true, 0, 0,
                new MersenneTwister(42), true, new SimpleValueChecker(1e-4, 1e-4));

        optimizer.optimize(1000, new SphereFunction(), GoalType.MINIMIZE, start);

        List<Double> sigmaHistory = optimizer.getStatisticsSigmaHistory();
        List<Double> fitnessHistory = optimizer.getStatisticsFitnessHistory();
        List<RealMatrix> meanHistory = optimizer.getStatisticsMeanHistory();
        List<RealMatrix> dHistory = optimizer.getStatisticsDHistory();

        Assert.assertFalse(sigmaHistory.isEmpty());
        Assert.assertFalse(fitnessHistory.isEmpty());
        Assert.assertFalse(meanHistory.isEmpty());
        Assert.assertFalse(dHistory.isEmpty());
        Assert.assertEquals(sigmaHistory.size(), fitnessHistory.size());
    }

    // Tests early termination when stopFitness threshold is met
    @Test
    public void testOptimize_stopFitnessReached_terminatesEarly() {
        double[] start = new double[] { 1.0, 1.0 };
        double stopFitness = 0.5;
        CMAESOptimizer optimizer = new CMAESOptimizer(8, null, 1000, stopFitness, true, 0, 0,
                new MersenneTwister(42), false, new SimpleValueChecker(1e-9, 1e-9));

        PointValuePair result = optimizer.optimize(5000, new SphereFunction(), GoalType.MINIMIZE, start);

        Assert.assertNotNull(result);
        Assert.assertTrue(result.getValue() <= stopFitness);
    }

    // Tests exception when inputSigma dimension does not match startPoint dimension
    @Test(expected = DimensionMismatchException.class)
    public void testCheckParameters_inputSigmaDimensionMismatch_throwsException() {
        double[] start = new double[] { 1.0, 2.0 };
        double[] sigma = new double[] { 0.5 };
        CMAESOptimizer optimizer = new CMAESOptimizer(8, sigma);

        optimizer.optimize(100, new SphereFunction(), GoalType.MINIMIZE, start);
    }

    // Tests exception when inputSigma contains negative value
    @Test(expected = NotPositiveException.class)
    public void testCheckParameters_negativeInputSigma_throwsException() {
        double[] start = new double[] { 1.0, 2.0 };
        double[] sigma = new double[] { 0.5, -0.1 };
        CMAESOptimizer optimizer = new CMAESOptimizer(8, sigma);

        optimizer.optimize(100, new SphereFunction(), GoalType.MINIMIZE, start);
    }

    // Tests exception when inputSigma exceeds boundary range
    @Test(expected = OutOfRangeException.class)
    public void testCheckParameters_inputSigmaExceedsBoundaryRange_throwsException() {
        double[] start = new double[] { 1.0, 1.0 };
        double[] lower = new double[] { 0.0, 0.0 };
        double[] upper = new double[] { 2.0, 2.0 };
        double[] sigma = new double[] { 0.5, 3.0 };
        CMAESOptimizer optimizer = new CMAESOptimizer(8, sigma);

        optimizer.optimize(100, new SphereFunction(), GoalType.MINIMIZE, start, lower, upper);
    }

    // Tests exception when boundaries have mixed finite and infinite bounds
    @Test(expected = MathUnsupportedOperationException.class)
    public void testCheckParameters_mixedFiniteAndInfiniteBounds_throwsException() {
        double[] start = new double[] { 1.0, 1.0 };
        double[] lower = new double[] { 0.0, Double.NEGATIVE_INFINITY };
        double[] upper = new double[] { 2.0, 2.0 };
        CMAESOptimizer optimizer = new CMAESOptimizer();

        optimizer.optimize(100, new SphereFunction(), GoalType.MINIMIZE, start, lower, upper);
    }

    // Tests exception when boundary range difference overflows Double.MAX_VALUE
    @Test(expected = NumberIsTooLargeException.class)
    public void testCheckParameters_boundaryRangeOverflow_throwsException() {
        double[] start = new double[] { 0.0 };
        double[] lower = new double[] { -Double.MAX_VALUE / 2.0 };
        double[] upper = new double[] { Double.MAX_VALUE };
        CMAESOptimizer optimizer = new CMAESOptimizer();

        optimizer.optimize(100, new SphereFunction(), GoalType.MINIMIZE, start, lower, upper);
    }

    // Tests optimization when all boundaries are infinite (treated as unbounded)
    @Test
    public void testOptimize_allInfiniteBounds_treatedAsUnbounded() {
        double[] start = new double[] { 1.0, 1.0 };
        double[] lower = new double[] { Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY };
        double[] upper = new double[] { Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY };
        CMAESOptimizer optimizer = new CMAESOptimizer(8);

        PointValuePair result = optimizer.optimize(5000, new SphereFunction(), GoalType.MINIMIZE, start, lower, upper);

        Assert.assertNotNull(result);
        Assert.assertEquals(0.0, result.getValue(), 1e-1);
    }

    // Tests deprecated constructor with default parameters
    @SuppressWarnings("deprecation")
    @Test
    public void testDeprecatedConstructor_initializesAndOptimizesCorrectly() {
        double[] start = new double[] { 1.0, -1.0 };
        CMAESOptimizer optimizer = new CMAESOptimizer(8, new double[] { 0.2, 0.2 }, 1000, 0,
                true, 0, 0, new MersenneTwister(42), false);

        PointValuePair result = optimizer.optimize(5000, new SphereFunction(), GoalType.MINIMIZE, start);

        Assert.assertNotNull(result);
        Assert.assertEquals(0.0, result.getValue(), 1e-1);
    }
}