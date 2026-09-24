package org.apache.commons.math3.optimization.direct;

import java.util.List;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MathUnsupportedOperationException;
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.optimization.GoalType;
import org.apache.commons.math3.optimization.PointValuePair;
import org.apache.commons.math3.optimization.SimpleValueChecker;
import org.apache.commons.math3.random.MersenneTwister;
import org.junit.Assert;
import org.junit.Test;

public class CMAESOptimizerTest {

    // Tests simple unconstrained sphere minimization
    @Test
    public void testOptimize_unconstrainedSphere_findsMinimum() {
        MultivariateFunction sphere = new MultivariateFunction() {
            public double value(double[] point) {
                double f = 0;
                for (double x : point) {
                    f += x * x;
                }
                return f;
            }
        };

        double[] startPoint = new double[] { 1.0, -1.0 };
        double[] insigma = new double[] { 0.5, 0.5 };
        CMAESOptimizer optimizer = new CMAESOptimizer(10, insigma, 1000, 1e-6, true, 0, 0, new MersenneTwister(42), false, new SimpleValueChecker(1e-6, 1e-6));
        PointValuePair result = optimizer.optimize(5000, sphere, GoalType.MINIMIZE, startPoint);

        Assert.assertEquals(0.0, result.getPoint()[0], 0.1);
        Assert.assertEquals(0.0, result.getPoint()[1], 0.1);
        Assert.assertTrue(result.getValue() < 0.05);
    }

    // Tests maximization goal type
    @Test
    public void testOptimize_maximization_findsMaximum() {
        MultivariateFunction invertedSphere = new MultivariateFunction() {
            public double value(double[] point) {
                double f = 10.0;
                for (double x : point) {
                    f -= (x - 2.0) * (x - 2.0);
                }
                return f;
            }
        };

        double[] startPoint = new double[] { 0.0, 0.0 };
        double[] insigma = new double[] { 0.5, 0.5 };
        CMAESOptimizer optimizer = new CMAESOptimizer(10, insigma, 1000, 0, true, 0, 0, new MersenneTwister(42), false);
        PointValuePair result = optimizer.optimize(5000, invertedSphere, GoalType.MAXIMIZE, startPoint);

        Assert.assertEquals(2.0, result.getPoint()[0], 0.1);
        Assert.assertEquals(2.0, result.getPoint()[1], 0.1);
        Assert.assertEquals(10.0, result.getValue(), 0.1);
    }

    // Tests bounded optimization within feasible region
    @Test
    public void testOptimize_boundedOptimization_findsFeasibleMinimum() {
        MultivariateFunction sphere = new MultivariateFunction() {
            public double value(double[] point) {
                return (point[0] - 3.0) * (point[0] - 3.0) + (point[1] - 3.0) * (point[1] - 3.0);
            }
        };

        double[] startPoint = new double[] { 1.0, 1.0 };
        double[] lower = new double[] { 0.0, 0.0 };
        double[] upper = new double[] { 5.0, 5.0 };
        double[] insigma = new double[] { 0.5, 0.5 };

        CMAESOptimizer optimizer = new CMAESOptimizer(10, insigma, 1000, 1e-6, true, 0, 5, new MersenneTwister(42), true);
        PointValuePair result = optimizer.optimize(5000, sphere, GoalType.MINIMIZE, startPoint, lower, upper);

        Assert.assertEquals(3.0, result.getPoint()[0], 0.1);
        Assert.assertEquals(3.0, result.getPoint()[1], 0.1);
        Assert.assertTrue(result.getValue() < 0.05);
    }

    // Tests diagonalOnly option and statistics generation
    @Test
    public void testOptimize_diagonalOnlyAndStatistics_populatesHistory() {
        MultivariateFunction sphere = new MultivariateFunction() {
            public double value(double[] point) {
                return point[0] * point[0] + point[1] * point[1];
            }
        };

        double[] startPoint = new double[] { 2.0, 2.0 };
        double[] insigma = new double[] { 0.5, 0.5 };
        CMAESOptimizer optimizer = new CMAESOptimizer(10, insigma, 50, 0, false, 2, 0, new MersenneTwister(42), true);
        optimizer.optimize(1000, sphere, GoalType.MINIMIZE, startPoint);

        List<Double> sigmaHist = optimizer.getStatisticsSigmaHistory();
        List<RealMatrix> meanHist = optimizer.getStatisticsMeanHistory();
        List<Double> fitHist = optimizer.getStatisticsFitnessHistory();
        List<RealMatrix> dHist = optimizer.getStatisticsDHistory();

        Assert.assertFalse(sigmaHist.isEmpty());
        Assert.assertFalse(meanHist.isEmpty());
        Assert.assertFalse(fitHist.isEmpty());
        Assert.assertFalse(dHist.isEmpty());
    }

    // Tests default constructors initialization
    @Test
    public void testConstructors_defaultParameters_instantiatesCorrectly() {
        CMAESOptimizer opt1 = new CMAESOptimizer();
        Assert.assertNotNull(opt1.getStatisticsSigmaHistory());

        CMAESOptimizer opt2 = new CMAESOptimizer(8);
        Assert.assertNotNull(opt2.getStatisticsMeanHistory());

        CMAESOptimizer opt3 = new CMAESOptimizer(8, new double[] { 0.2 });
        Assert.assertNotNull(opt3.getStatisticsFitnessHistory());
    }

    // Tests exception when inputSigma dimension mismatches startPoint dimension
    @Test(expected = DimensionMismatchException.class)
    public void testCheckParameters_sigmaDimensionMismatch_throwsException() {
        MultivariateFunction sphere = new MultivariateFunction() {
            public double value(double[] point) {
                return point[0] * point[0];
            }
        };

        double[] startPoint = new double[] { 1.0, 1.0 };
        double[] insigma = new double[] { 0.5 };
        CMAESOptimizer optimizer = new CMAESOptimizer(10, insigma);
        optimizer.optimize(100, sphere, GoalType.MINIMIZE, startPoint);
    }

    // Tests exception when inputSigma contains negative values
    @Test(expected = NotPositiveException.class)
    public void testCheckParameters_negativeSigma_throwsException() {
        MultivariateFunction sphere = new MultivariateFunction() {
            public double value(double[] point) {
                return point[0] * point[0];
            }
        };

        double[] startPoint = new double[] { 1.0 };
        double[] insigma = new double[] { -0.5 };
        CMAESOptimizer optimizer = new CMAESOptimizer(10, insigma);
        optimizer.optimize(100, sphere, GoalType.MINIMIZE, startPoint);
    }

    // Tests exception when inputSigma exceeds bounds range
    @Test(expected = OutOfRangeException.class)
    public void testCheckParameters_sigmaExceedsBoundRange_throwsException() {
        MultivariateFunction sphere = new MultivariateFunction() {
            public double value(double[] point) {
                return point[0] * point[0];
            }
        };

        double[] startPoint = new double[] { 1.0 };
        double[] lower = new double[] { 0.0 };
        double[] upper = new double[] { 1.0 };
        double[] insigma = new double[] { 2.0 };
        CMAESOptimizer optimizer = new CMAESOptimizer(10, insigma);
        optimizer.optimize(100, sphere, GoalType.MINIMIZE, startPoint, lower, upper);
    }

    // Tests exception when mixed finite and infinite bounds are provided
    @Test(expected = MathUnsupportedOperationException.class)
    public void testCheckParameters_mixedInfiniteAndFiniteBounds_throwsException() {
        MultivariateFunction sphere = new MultivariateFunction() {
            public double value(double[] point) {
                return point[0] * point[0] + point[1] * point[1];
            }
        };

        double[] startPoint = new double[] { 1.0, 1.0 };
        double[] lower = new double[] { 0.0, Double.NEGATIVE_INFINITY };
        double[] upper = new double[] { 2.0, Double.POSITIVE_INFINITY };
        CMAESOptimizer optimizer = new CMAESOptimizer(10);
        optimizer.optimize(100, sphere, GoalType.MINIMIZE, startPoint, lower, upper);
    }

    // Tests fully infinite bounds treated as unconstrained
    @Test
    public void testCheckParameters_allInfiniteBounds_treatedAsUnconstrained() {
        MultivariateFunction sphere = new MultivariateFunction() {
            public double value(double[] point) {
                return point[0] * point[0];
            }
        };

        double[] startPoint = new double[] { 2.0 };
        double[] lower = new double[] { Double.NEGATIVE_INFINITY };
        double[] upper = new double[] { Double.POSITIVE_INFINITY };
        CMAESOptimizer optimizer = new CMAESOptimizer(6, new double[] { 0.5 }, 200, 1e-4, true, 0, 0, new MersenneTwister(42), false);
        PointValuePair result = optimizer.optimize(1000, sphere, GoalType.MINIMIZE, startPoint, lower, upper);

        Assert.assertEquals(0.0, result.getPoint()[0], 0.1);
    }

    // Tests stopFitness termination condition
    @Test
    public void testOptimize_stopFitnessReached_terminatesEarly() {
        MultivariateFunction sphere = new MultivariateFunction() {
            public double value(double[] point) {
                return point[0] * point[0];
            }
        };

        double[] startPoint = new double[] { 5.0 };
        double stopFitness = 1.0;
        CMAESOptimizer optimizer = new CMAESOptimizer(6, new double[] { 0.5 }, 10000, stopFitness, true, 0, 0, new MersenneTwister(42), false);
        PointValuePair result = optimizer.optimize(10000, sphere, GoalType.MINIMIZE, startPoint);

        Assert.assertTrue(result.getValue() <= stopFitness);
    }

    // Tests max evaluation count exhaustion breaks generation loop cleanly
    @Test
    public void testOptimize_lowMaxEvaluations_stopsGracefully() {
        MultivariateFunction sphere = new MultivariateFunction() {
            public double value(double[] point) {
                return point[0] * point[0];
            }
        };

        double[] startPoint = new double[] { 5.0 };
        CMAESOptimizer optimizer = new CMAESOptimizer(10, new double[] { 0.5 });
        PointValuePair result = optimizer.optimize(15, sphere, GoalType.MINIMIZE, startPoint);

        Assert.assertNotNull(result);
        Assert.assertNotNull(result.getPoint());
    }
}