package org.apache.commons.math3.optimization.direct;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MathUnsupportedOperationException;
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.optimization.GoalType;
import org.apache.commons.math3.optimization.PointValuePair;
import org.apache.commons.math3.optimization.SimpleValueChecker;
import org.apache.commons.math3.random.MersenneTwister;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CMAESOptimizerTest {

    // Tests standard minimization of a sphere function
    @Test
    public void testOptimize_sphereFunctionMinimization_findsOptimum() {
        MultivariateFunction sphere = new MultivariateFunction() {
            public double value(double[] point) {
                double sum = 0;
                for (double v : point) {
                    sum += v * v;
                }
                return sum;
            }
        };

        double[] startPoint = new double[] { 1.0, 1.5, -2.0 };
        double[] inputSigma = new double[] { 0.5, 0.5, 0.5 };
        CMAESOptimizer optimizer = new CMAESOptimizer(10, inputSigma, 3000, 1e-10, true, 0, 0,
                new MersenneTwister(42), false);

        PointValuePair result = optimizer.optimize(10000, sphere, GoalType.MINIMIZE, startPoint);

        assertNotNull(result);
        assertEquals(0.0, result.getValue(), 1e-3);
        for (double val : result.getPoint()) {
            assertEquals(0.0, val, 1e-2);
        }
    }

    // Tests maximization goal type
    @Test
    public void testOptimize_sphereFunctionMaximization_findsOptimum() {
        MultivariateFunction invertedSphere = new MultivariateFunction() {
            public double value(double[] point) {
                double sum = 0;
                for (double v : point) {
                    sum += v * v;
                }
                return 10.0 - sum;
            }
        };

        double[] startPoint = new double[] { 0.5, -0.5 };
        double[] inputSigma = new double[] { 0.2, 0.2 };
        CMAESOptimizer optimizer = new CMAESOptimizer(8, inputSigma, 2000, 0, true, 0, 0,
                new MersenneTwister(42), false);

        PointValuePair result = optimizer.optimize(5000, invertedSphere, GoalType.MAXIMIZE, startPoint);

        assertNotNull(result);
        assertEquals(10.0, result.getValue(), 1e-3);
        for (double val : result.getPoint()) {
            assertEquals(0.0, val, 1e-2);
        }
    }

    // Tests bounded optimization and verifies that the returned point is strictly within bounds (Defects4J Math-20)
    @Test
    public void testOptimize_boundedOptimization_resultWithinBounds() {
        MultivariateFunction function = new MultivariateFunction() {
            public double value(double[] point) {
                return (point[0] - 5.0) * (point[0] - 5.0) + (point[1] - 5.0) * (point[1] - 5.0);
            }
        };

        double[] startPoint = new double[] { 0.5, 0.5 };
        double[] lower = new double[] { 0.0, 0.0 };
        double[] upper = new double[] { 1.0, 1.0 };
        double[] inputSigma = new double[] { 0.2, 0.2 };

        CMAESOptimizer optimizer = new CMAESOptimizer(10, inputSigma, 2000, 1e-8, true, 0, 10,
                new MersenneTwister(42), false);

        PointValuePair result = optimizer.optimize(5000, function, GoalType.MINIMIZE, startPoint, lower, upper);

        assertNotNull(result);
        double[] point = result.getPoint();
        for (int i = 0; i < point.length; i++) {
            assertTrue("Point must be >= lower bound", point[i] >= lower[i]);
            assertTrue("Point must be <= upper bound", point[i] <= upper[i]);
        }
    }

    // Tests diagonal-only mode execution path
    @Test
    public void testOptimize_diagonalOnlyMode_convergesCorrectly() {
        MultivariateFunction sphere = new MultivariateFunction() {
            public double value(double[] point) {
                return point[0] * point[0] + point[1] * point[1];
            }
        };

        double[] startPoint = new double[] { 2.0, -2.0 };
        double[] inputSigma = new double[] { 0.5, 0.5 };
        CMAESOptimizer optimizer = new CMAESOptimizer(8, inputSigma, 2000, 1e-10, false, 1, 0,
                new MersenneTwister(42), false);

        PointValuePair result = optimizer.optimize(5000, sphere, GoalType.MINIMIZE, startPoint);

        assertNotNull(result);
        assertEquals(0.0, result.getValue(), 1e-3);
    }

    // Tests generation of statistics history
    @Test
    public void testOptimize_generateStatisticsEnabled_recordsHistories() {
        MultivariateFunction sphere = new MultivariateFunction() {
            public double value(double[] point) {
                return point[0] * point[0];
            }
        };

        double[] startPoint = new double[] { 1.0 };
        double[] inputSigma = new double[] { 0.2 };
        CMAESOptimizer optimizer = new CMAESOptimizer(6, inputSigma, 50, 0, true, 0, 0,
                new MersenneTwister(42), true);

        optimizer.optimize(200, sphere, GoalType.MINIMIZE, startPoint);

        assertTrue(optimizer.getStatisticsSigmaHistory().size() > 0);
        assertTrue(optimizer.getStatisticsFitnessHistory().size() > 0);
        assertTrue(optimizer.getStatisticsMeanHistory().size() > 0);
        assertTrue(optimizer.getStatisticsDHistory().size() > 0);
    }

    // Tests exception path for mixed finite and infinite bounds
    @Test(expected = MathUnsupportedOperationException.class)
    public void testCheckParameters_mixedFiniteAndInfiniteBounds_throwsException() {
        MultivariateFunction dummy = new MultivariateFunction() {
            public double value(double[] point) {
                return 0;
            }
        };

        double[] startPoint = new double[] { 0.5, 0.5 };
        double[] lower = new double[] { 0.0, Double.NEGATIVE_INFINITY };
        double[] upper = new double[] { 1.0, Double.POSITIVE_INFINITY };

        CMAESOptimizer optimizer = new CMAESOptimizer();
        optimizer.optimize(100, dummy, GoalType.MINIMIZE, startPoint, lower, upper);
    }

    // Tests exception path for inputSigma dimension mismatch with start point
    @Test(expected = DimensionMismatchException.class)
    public void testCheckParameters_inputSigmaDimensionMismatch_throwsException() {
        MultivariateFunction dummy = new MultivariateFunction() {
            public double value(double[] point) {
                return 0;
            }
        };

        double[] startPoint = new double[] { 1.0, 2.0 };
        double[] inputSigma = new double[] { 0.5 };

        CMAESOptimizer optimizer = new CMAESOptimizer(10, inputSigma);
        optimizer.optimize(100, dummy, GoalType.MINIMIZE, startPoint);
    }

    // Tests exception path for negative inputSigma entry
    @Test(expected = NotPositiveException.class)
    public void testCheckParameters_negativeInputSigma_throwsException() {
        MultivariateFunction dummy = new MultivariateFunction() {
            public double value(double[] point) {
                return 0;
            }
        };

        double[] startPoint = new double[] { 1.0, 2.0 };
        double[] inputSigma = new double[] { 0.5, -0.1 };

        CMAESOptimizer optimizer = new CMAESOptimizer(10, inputSigma);
        optimizer.optimize(100, dummy, GoalType.MINIMIZE, startPoint);
    }

    // Tests exception path when inputSigma exceeds boundary range
    @Test(expected = OutOfRangeException.class)
    public void testCheckParameters_inputSigmaExceedsBoundaryRange_throwsException() {
        MultivariateFunction dummy = new MultivariateFunction() {
            public double value(double[] point) {
                return 0;
            }
        };

        double[] startPoint = new double[] { 0.5 };
        double[] lower = new double[] { 0.0 };
        double[] upper = new double[] { 1.0 };
        double[] inputSigma = new double[] { 1.5 }; // Range is 1.0, sigma 1.5 > 1.0

        CMAESOptimizer optimizer = new CMAESOptimizer(10, inputSigma);
        optimizer.optimize(100, dummy, GoalType.MINIMIZE, startPoint, lower, upper);
    }

    // Tests termination criterion when stopFitness is reached
    @Test
    public void testOptimize_stopFitnessReached_terminatesSuccessfully() {
        MultivariateFunction sphere = new MultivariateFunction() {
            public double value(double[] point) {
                return point[0] * point[0] + point[1] * point[1];
            }
        };

        double[] startPoint = new double[] { 1.0, 1.0 };
        double[] inputSigma = new double[] { 0.3, 0.3 };
        double stopFitness = 0.5;

        CMAESOptimizer optimizer = new CMAESOptimizer(10, inputSigma, 2000, stopFitness, true, 0, 0,
                new MersenneTwister(42), false);

        PointValuePair result = optimizer.optimize(5000, sphere, GoalType.MINIMIZE, startPoint);

        assertNotNull(result);
        assertTrue(result.getValue() <= stopFitness);
    }

    // Tests custom ConvergenceChecker parameter
    @Test
    public void testOptimize_customConvergenceChecker_converges() {
        MultivariateFunction sphere = new MultivariateFunction() {
            public double value(double[] point) {
                return point[0] * point[0];
            }
        };

        double[] startPoint = new double[] { 0.5 };
        double[] inputSigma = new double[] { 0.1 };
        SimpleValueChecker checker = new SimpleValueChecker(1e-2, 1e-2);

        CMAESOptimizer optimizer = new CMAESOptimizer(6, inputSigma, 1000, 0, true, 0, 0,
                new MersenneTwister(42), false, checker);

        PointValuePair result = optimizer.optimize(1000, sphere, GoalType.MINIMIZE, startPoint);

        assertNotNull(result);
        assertEquals(0.0, result.getValue(), 0.1);
    }

    // Tests default constructor and initial history getters
    @Test
    public void testConstructor_default_initializesEmptyHistories() {
        CMAESOptimizer optimizer = new CMAESOptimizer();

        assertNotNull(optimizer.getStatisticsSigmaHistory());
        assertEquals(0, optimizer.getStatisticsSigmaHistory().size());
        assertNotNull(optimizer.getStatisticsMeanHistory());
        assertEquals(0, optimizer.getStatisticsMeanHistory().size());
        assertNotNull(optimizer.getStatisticsFitnessHistory());
        assertEquals(0, optimizer.getStatisticsFitnessHistory().size());
        assertNotNull(optimizer.getStatisticsDHistory());
        assertEquals(0, optimizer.getStatisticsDHistory().size());
    }
}