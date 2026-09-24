package org.apache.commons.math.optimization.direct;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.MultivariateRealFunction;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.optimization.RealConvergenceChecker;
import org.apache.commons.math.optimization.RealPointValuePair;
import org.apache.commons.math.optimization.SimpleScalarValueChecker;
import org.junit.Assert;
import org.junit.Test;

public class MultiDirectionalTest {

    // Tests default constructor parameter values and basic minimization
    @Test
    public void testOptimize_defaultConstructor_minimizesSimpleQuadratic()
        throws FunctionEvaluationException, OptimizationException {
        MultiDirectional optimizer = new MultiDirectional();
        optimizer.setMaxIterations(100);
        optimizer.setMaxEvaluations(1000);
        optimizer.setConvergenceChecker(new SimpleScalarValueChecker(1.0e-5, 1.0e-5));

        MultivariateRealFunction f = new MultivariateRealFunction() {
            public double value(double[] point) {
                double x = point[0] - 2.0;
                double y = point[1] + 3.0;
                return x * x + y * y;
            }
        };

        RealPointValuePair optimum = optimizer.optimize(f, GoalType.MINIMIZE, new double[] { 0.0, 0.0 });
        Assert.assertEquals(2.0, optimum.getPoint()[0], 1.0e-2);
        Assert.assertEquals(-3.0, optimum.getPoint()[1], 1.0e-2);
        Assert.assertEquals(0.0, optimum.getValue(), 1.0e-3);
    }

    // Tests custom constructor with custom khi and gamma parameters
    @Test
    public void testOptimize_customCoefficients_minimizesCorrectly()
        throws FunctionEvaluationException, OptimizationException {
        MultiDirectional optimizer = new MultiDirectional(1.5, 0.6);
        optimizer.setMaxIterations(100);
        optimizer.setMaxEvaluations(1000);
        optimizer.setConvergenceChecker(new SimpleScalarValueChecker(1.0e-5, 1.0e-5));

        MultivariateRealFunction f = new MultivariateRealFunction() {
            public double value(double[] point) {
                double x = point[0] - 1.0;
                return x * x + 4.0;
            }
        };

        RealPointValuePair optimum = optimizer.optimize(f, GoalType.MINIMIZE, new double[] { 5.0 });
        Assert.assertEquals(1.0, optimum.getPoint()[0], 1.0e-2);
        Assert.assertEquals(4.0, optimum.getValue(), 1.0e-3);
    }

    // Tests maximization goal
    @Test
    public void testOptimize_maximizationGoal_findsMaximum()
        throws FunctionEvaluationException, OptimizationException {
        MultiDirectional optimizer = new MultiDirectional();
        optimizer.setMaxIterations(100);
        optimizer.setMaxEvaluations(1000);
        optimizer.setConvergenceChecker(new SimpleScalarValueChecker(1.0e-5, 1.0e-5));

        MultivariateRealFunction f = new MultivariateRealFunction() {
            public double value(double[] point) {
                double x = point[0] - 1.0;
                double y = point[1] - 1.0;
                return 10.0 - (x * x + y * y);
            }
        };

        RealPointValuePair optimum = optimizer.optimize(f, GoalType.MAXIMIZE, new double[] { 0.0, 0.0 });
        Assert.assertEquals(1.0, optimum.getPoint()[0], 1.0e-2);
        Assert.assertEquals(1.0, optimum.getPoint()[1], 1.0e-2);
        Assert.assertEquals(10.0, optimum.getValue(), 1.0e-3);
    }

    // Tests Rosenbrock function (triggers reflection and expansion branches)
    @Test
    public void testOptimize_rosenbrockFunction_convergesToMinimum()
        throws FunctionEvaluationException, OptimizationException {
        MultiDirectional optimizer = new MultiDirectional();
        optimizer.setMaxIterations(200);
        optimizer.setMaxEvaluations(2000);
        optimizer.setConvergenceChecker(new SimpleScalarValueChecker(1.0e-4, 1.0e-4));

        MultivariateRealFunction rosenbrock = new MultivariateRealFunction() {
            public double value(double[] point) {
                double x = point[0];
                double y = point[1];
                return 100.0 * Math.pow(y - x * x, 2) + Math.pow(1.0 - x, 2);
            }
        };

        RealPointValuePair optimum = optimizer.optimize(rosenbrock, GoalType.MINIMIZE, new double[] { -1.2, 1.0 });
        Assert.assertEquals(1.0, optimum.getPoint()[0], 1.0e-1);
        Assert.assertEquals(1.0, optimum.getPoint()[1], 1.0e-1);
        Assert.assertTrue(optimum.getValue() < 0.1);
    }

    // Tests 4-dimensional sphere function
    @Test
    public void testOptimize_fourDimensionalSphere_convergesNearZero()
        throws FunctionEvaluationException, OptimizationException {
        MultiDirectional optimizer = new MultiDirectional();
        optimizer.setMaxIterations(150);
        optimizer.setMaxEvaluations(2000);
        optimizer.setConvergenceChecker(new SimpleScalarValueChecker(1.0e-5, 1.0e-5));

        MultivariateRealFunction f = new MultivariateRealFunction() {
            public double value(double[] point) {
                double sum = 0;
                for (double v : point) {
                    sum += v * v;
                }
                return sum;
            }
        };

        RealPointValuePair optimum = optimizer.optimize(f, GoalType.MINIMIZE, new double[] { 1.0, -1.0, 2.0, -2.0 });
        for (int i = 0; i < 4; ++i) {
            Assert.assertEquals(0.0, optimum.getPoint()[i], 1.0e-2);
        }
        Assert.assertEquals(0.0, optimum.getValue(), 1.0e-3);
    }

    // Tests exceeding max iterations / evaluations (regression for infinite loop / evaluation limit)
    @Test(expected = OptimizationException.class)
    public void testOptimize_maxEvaluationsExceeded_throwsOptimizationException()
        throws FunctionEvaluationException, OptimizationException {
        MultiDirectional optimizer = new MultiDirectional();
        optimizer.setMaxIterations(5);
        optimizer.setMaxEvaluations(10);
        optimizer.setConvergenceChecker(new SimpleScalarValueChecker(1.0e-15, 1.0e-15));

        MultivariateRealFunction f = new MultivariateRealFunction() {
            public double value(double[] point) {
                return Math.sin(point[0]) * Math.cos(point[1]);
            }
        };

        optimizer.optimize(f, GoalType.MINIMIZE, new double[] { 0.0, 0.0 });
    }

    // Tests when start point is already at optimum
    @Test
    public void testOptimize_alreadyAtOptimum_returnsStartPoint()
        throws FunctionEvaluationException, OptimizationException {
        MultiDirectional optimizer = new MultiDirectional();
        optimizer.setMaxIterations(100);
        optimizer.setMaxEvaluations(500);
        optimizer.setConvergenceChecker(new SimpleScalarValueChecker(1.0e-5, 1.0e-5));

        MultivariateRealFunction f = new MultivariateRealFunction() {
            public double value(double[] point) {
                return point[0] * point[0] + point[1] * point[1];
            }
        };

        RealPointValuePair optimum = optimizer.optimize(f, GoalType.MINIMIZE, new double[] { 0.0, 0.0 });
        Assert.assertEquals(0.0, optimum.getPoint()[0], 1.0e-2);
        Assert.assertEquals(0.0, optimum.getPoint()[1], 1.0e-2);
        Assert.assertEquals(0.0, optimum.getValue(), 1.0e-3);
    }

    // Tests function evaluation exception propagation
    @Test(expected = FunctionEvaluationException.class)
    public void testOptimize_functionThrowsException_propagatesException()
        throws FunctionEvaluationException, OptimizationException {
        MultiDirectional optimizer = new MultiDirectional();
        optimizer.setMaxIterations(100);
        optimizer.setMaxEvaluations(1000);

        MultivariateRealFunction f = new MultivariateRealFunction() {
            public double value(double[] point) throws FunctionEvaluationException {
                throw new FunctionEvaluationException(point);
            }
        };

        optimizer.optimize(f, GoalType.MINIMIZE, new double[] { 1.0, 2.0 });
    }

    // Tests constant function where reflection and contraction are not better than best (Math-84 regression)
    @Test
    public void testOptimize_constantFunction_terminatesProperly()
        throws FunctionEvaluationException, OptimizationException {
        MultiDirectional optimizer = new MultiDirectional();
        optimizer.setMaxIterations(50);
        optimizer.setMaxEvaluations(100);
        optimizer.setConvergenceChecker(new RealConvergenceChecker() {
            public boolean converged(int iteration, RealPointValuePair previous, RealPointValuePair current) {
                return true;
            }
        });

        MultivariateRealFunction f = new MultivariateRealFunction() {
            public double value(double[] point) {
                return 42.0;
            }
        };

        RealPointValuePair optimum = optimizer.optimize(f, GoalType.MINIMIZE, new double[] { 1.0, 1.0 });
        Assert.assertEquals(42.0, optimum.getValue(), 1.0e-10);
    }
}