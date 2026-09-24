package org.apache.commons.math3.optim.nonlinear.scalar.noderiv;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.exception.MathUnsupportedOperationException;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.optim.ConvergenceChecker;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.MaxIter;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import org.apache.commons.math3.optim.SimplePointChecker;
import org.apache.commons.math3.optim.SimpleValueChecker;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.util.FastMath;
import org.junit.Assert;
import org.junit.Test;

public class PowellOptimizerTest {

    // Tests constructor with relative threshold below minimum allowed value
    @Test(expected = NumberIsTooSmallException.class)
    public void testConstructor_relBelowMinimum_throwsNumberIsTooSmallException() {
        new PowellOptimizer(1e-20, 1e-6);
    }

    // Tests constructor with zero absolute threshold
    @Test(expected = NotStrictlyPositiveException.class)
    public void testConstructor_absZero_throwsNotStrictlyPositiveException() {
        new PowellOptimizer(1e-6, 0.0);
    }

    // Tests constructor with negative absolute threshold
    @Test(expected = NotStrictlyPositiveException.class)
    public void testConstructor_absNegative_throwsNotStrictlyPositiveException() {
        new PowellOptimizer(1e-6, -1e-6);
    }

    // Tests constructor with custom line search thresholds throwing on invalid rel
    @Test(expected = NumberIsTooSmallException.class)
    public void testConstructor_customLineSearchRelTooSmall_throwsException() {
        new PowellOptimizer(1e-20, 1e-6, 1e-6, 1e-6);
    }

    // Tests constructor with custom line search thresholds throwing on invalid abs
    @Test(expected = NotStrictlyPositiveException.class)
    public void testConstructor_customLineSearchAbsNegative_throwsException() {
        new PowellOptimizer(1e-6, -1.0, 1e-6, 1e-6);
    }

    // Tests optimize method rejecting constraints / bounds
    @Test(expected = MathUnsupportedOperationException.class)
    public void testOptimize_withBounds_throwsMathUnsupportedOperationException() {
        PowellOptimizer optimizer = new PowellOptimizer(1e-6, 1e-6);
        optimizer.optimize(
            new MaxEval(100),
            new ObjectiveFunction(new MultivariateFunction() {
                public double value(double[] point) {
                    return point[0] * point[0];
                }
            }),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] { 1.0 }),
            new SimpleBounds(new double[] { 0.0 }, new double[] { 2.0 })
        );
    }

    // Tests 1D quadratic function minimization
    @Test
    public void testOptimize_1DQuadraticMinimization_findsOptimum() {
        PowellOptimizer optimizer = new PowellOptimizer(1e-8, 1e-8);
        MultivariateFunction function = new MultivariateFunction() {
            public double value(double[] point) {
                double x = point[0];
                return (x - 3.0) * (x - 3.0) + 5.0;
            }
        };

        PointValuePair result = optimizer.optimize(
            new MaxEval(1000),
            new MaxIter(500),
            new ObjectiveFunction(function),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] { 0.0 })
        );

        Assert.assertEquals(3.0, result.getPoint()[0], 1e-4);
        Assert.assertEquals(5.0, result.getValue(), 1e-4);
        Assert.assertTrue(optimizer.getEvaluations() > 0);
        Assert.assertTrue(optimizer.getIterations() > 0);
    }

    // Tests 2D Rosenbrock function minimization
    @Test
    public void testOptimize_2DRosenbrockMinimization_findsOptimum() {
        PowellOptimizer optimizer = new PowellOptimizer(1e-9, 1e-9);
        MultivariateFunction rosenbrock = new MultivariateFunction() {
            public double value(double[] point) {
                double x = point[0];
                double y = point[1];
                return 100.0 * (y - x * x) * (y - x * x) + (1.0 - x) * (1.0 - x);
            }
        };

        PointValuePair result = optimizer.optimize(
            new MaxEval(2000),
            new MaxIter(1000),
            new ObjectiveFunction(rosenbrock),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] { -1.2, 1.0 })
        );

        Assert.assertEquals(1.0, result.getPoint()[0], 1e-2);
        Assert.assertEquals(1.0, result.getPoint()[1], 1e-2);
        Assert.assertEquals(0.0, result.getValue(), 1e-3);
        Assert.assertTrue(optimizer.getIterations() > 0);
    }

    // Tests 2D maximization
    @Test
    public void testOptimize_2DMaximization_findsOptimum() {
        PowellOptimizer optimizer = new PowellOptimizer(1e-8, 1e-8);
        MultivariateFunction invertedParaboloid = new MultivariateFunction() {
            public double value(double[] point) {
                double x = point[0];
                double y = point[1];
                return 10.0 - (x - 2.0) * (x - 2.0) - (y + 1.0) * (y + 1.0);
            }
        };

        PointValuePair result = optimizer.optimize(
            new MaxEval(1000),
            new MaxIter(500),
            new ObjectiveFunction(invertedParaboloid),
            GoalType.MAXIMIZE,
            new InitialGuess(new double[] { 0.0, 0.0 })
        );

        Assert.assertEquals(2.0, result.getPoint()[0], 1e-4);
        Assert.assertEquals(-1.0, result.getPoint()[1], 1e-4);
        Assert.assertEquals(10.0, result.getValue(), 1e-4);
    }

    // Tests optimization with custom convergence checker
    @Test
    public void testOptimize_withCustomConvergenceChecker_converges() {
        ConvergenceChecker<PointValuePair> checker = new SimplePointChecker<PointValuePair>(1e-4, 1e-4);
        PowellOptimizer optimizer = new PowellOptimizer(1e-6, 1e-6, checker);

        MultivariateFunction function = new MultivariateFunction() {
            public double value(double[] point) {
                return point[0] * point[0] + point[1] * point[1];
            }
        };

        PointValuePair result = optimizer.optimize(
            new MaxEval(1000),
            new MaxIter(500),
            new ObjectiveFunction(function),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] { 5.0, -5.0 })
        );

        Assert.assertEquals(0.0, result.getPoint()[0], 1e-2);
        Assert.assertEquals(0.0, result.getPoint()[1], 1e-2);
        Assert.assertEquals(0.0, result.getValue(), 1e-3);
    }

    // Tests constructor with custom line search tolerances and custom checker
    @Test
    public void testOptimize_withCustomLineSearchAndChecker_findsOptimum() {
        ConvergenceChecker<PointValuePair> checker = new SimpleValueChecker(1e-6, 1e-6);
        PowellOptimizer optimizer = new PowellOptimizer(1e-7, 1e-7, 1e-7, 1e-7, checker);

        MultivariateFunction function = new MultivariateFunction() {
            public double value(double[] point) {
                return (point[0] - 1.0) * (point[0] - 1.0) + (point[1] - 2.0) * (point[1] - 2.0);
            }
        };

        PointValuePair result = optimizer.optimize(
            new MaxEval(1000),
            new MaxIter(500),
            new ObjectiveFunction(function),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] { 0.0, 0.0 })
        );

        Assert.assertEquals(1.0, result.getPoint()[0], 1e-3);
        Assert.assertEquals(2.0, result.getPoint()[1], 1e-3);
        Assert.assertEquals(0.0, result.getValue(), 1e-4);
    }

    // Tests constructor with custom line search tolerances without custom checker
    @Test
    public void testConstructor_fourArgWithoutChecker_worksCorrectly() {
        PowellOptimizer optimizer = new PowellOptimizer(1e-6, 1e-6, 1e-6, 1e-6);
        MultivariateFunction function = new MultivariateFunction() {
            public double value(double[] point) {
                return point[0] * point[0];
            }
        };

        PointValuePair result = optimizer.optimize(
            new MaxEval(1000),
            new ObjectiveFunction(function),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] { 2.0 })
        );

        Assert.assertEquals(0.0, result.getPoint()[0], 1e-3);
        Assert.assertEquals(0.0, result.getValue(), 1e-4);
    }

    // Tests starting point already at optimum
    @Test
    public void testOptimize_startingAtOptimum_terminatesImmediately() {
        PowellOptimizer optimizer = new PowellOptimizer(1e-6, 1e-6);
        MultivariateFunction function = new MultivariateFunction() {
            public double value(double[] point) {
                return point[0] * point[0] + point[1] * point[1];
            }
        };

        PointValuePair result = optimizer.optimize(
            new MaxEval(500),
            new MaxIter(100),
            new ObjectiveFunction(function),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] { 0.0, 0.0 })
        );

        Assert.assertEquals(0.0, result.getPoint()[0], 1e-6);
        Assert.assertEquals(0.0, result.getPoint()[1], 1e-6);
        Assert.assertEquals(0.0, result.getValue(), 1e-6);
    }

    // Tests that iterations and evaluations are tracked and incremented
    @Test
    public void testOptimize_iterationAndEvaluationCounts_incrementedProperly() {
        PowellOptimizer optimizer = new PowellOptimizer(1e-6, 1e-6);
        MultivariateFunction function = new MultivariateFunction() {
            public double value(double[] point) {
                return (point[0] - 4.0) * (point[0] - 4.0) + (point[1] + 3.0) * (point[1] + 3.0);
            }
        };

        optimizer.optimize(
            new MaxEval(1000),
            new MaxIter(500),
            new ObjectiveFunction(function),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] { 0.0, 0.0 })
        );

        Assert.assertTrue("Evaluations should be positive", optimizer.getEvaluations() > 0);
        Assert.assertTrue("Iterations should be positive", optimizer.getIterations() > 0);
    }
}