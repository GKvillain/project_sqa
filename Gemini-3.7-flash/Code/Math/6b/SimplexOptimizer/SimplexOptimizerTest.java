package org.apache.commons.math3.optim.nonlinear.scalar.noderiv;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.exception.MathUnsupportedOperationException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.MaxIter;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import org.apache.commons.math3.optim.SimplePointChecker;
import org.apache.commons.math3.optim.SimpleValueChecker;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.junit.Assert;
import org.junit.Test;

public class SimplexOptimizerTest {

    // Tests throwing NullArgumentException when no initial simplex is provided
    @Test(expected = NullArgumentException.class)
    public void testOptimize_noSimplexPassed_throwsNullArgumentException() {
        SimplexOptimizer optimizer = new SimplexOptimizer(1e-10, 1e-30);
        MultivariateFunction fourExtrema = new FourExtrema();

        optimizer.optimize(
            new MaxEval(100),
            new ObjectiveFunction(fourExtrema),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] { -3, 0 })
        );
    }

    // Tests throwing MathUnsupportedOperationException when bounds are provided
    @Test(expected = MathUnsupportedOperationException.class)
    public void testOptimize_withBounds_throwsMathUnsupportedOperationException() {
        SimplexOptimizer optimizer = new SimplexOptimizer(1e-10, 1e-30);
        MultivariateFunction fourExtrema = new FourExtrema();

        optimizer.optimize(
            new MaxEval(100),
            new ObjectiveFunction(fourExtrema),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] { -3, 0 }),
            new NelderMeadSimplex(new double[] { 0.2, 0.2 }),
            new SimpleBounds(new double[] { -5, -5 }, new double[] { 5, 5 })
        );
    }

    // Tests constructor with custom ConvergenceChecker
    @Test
    public void testConstructor_withCustomChecker_initializesCorrectly() {
        SimplePointChecker<PointValuePair> checker = new SimplePointChecker<PointValuePair>(1e-5, 1e-5);
        SimplexOptimizer optimizer = new SimplexOptimizer(checker);
        Assert.assertNotNull(optimizer.getConvergenceChecker());
    }

    // Tests Nelder-Mead minimization on a quadratic function
    @Test
    public void testOptimize_minimizeParaboloidNelderMead_convergesToMinimum() {
        SimplexOptimizer optimizer = new SimplexOptimizer(1e-10, 1e-30);
        MultivariateFunction paraboloid = new MultivariateFunction() {
            public double value(double[] point) {
                return (point[0] - 1.0) * (point[0] - 1.0) + (point[1] - 2.0) * (point[1] - 2.0);
            }
        };

        PointValuePair optimum = optimizer.optimize(
            new MaxEval(200),
            new MaxIter(100),
            new ObjectiveFunction(paraboloid),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] { 0.0, 0.0 }),
            new NelderMeadSimplex(2)
        );

        Assert.assertEquals(1.0, optimum.getPoint()[0], 1e-3);
        Assert.assertEquals(2.0, optimum.getPoint()[1], 1e-3);
        Assert.assertEquals(0.0, optimum.getValue(), 1e-4);
        Assert.assertTrue(optimizer.getIterations() > 0);
        Assert.assertTrue(optimizer.getEvaluations() > 0);
    }

    // Tests MultiDirectionalSimplex minimization
    @Test
    public void testOptimize_minimizeMultiDirectionalSimplex_convergesToMinimum() {
        SimplexOptimizer optimizer = new SimplexOptimizer(1e-11, 1e-30);
        MultivariateFunction paraboloid = new MultivariateFunction() {
            public double value(double[] point) {
                return (point[0] - 2.0) * (point[0] - 2.0) + (point[1] + 3.0) * (point[1] + 3.0);
            }
        };

        PointValuePair optimum = optimizer.optimize(
            new MaxEval(300),
            new ObjectiveFunction(paraboloid),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] { 0.0, 0.0 }),
            new MultiDirectionalSimplex(2)
        );

        Assert.assertEquals(2.0, optimum.getPoint()[0], 1e-3);
        Assert.assertEquals(-3.0, optimum.getPoint()[1], 1e-3);
        Assert.assertEquals(0.0, optimum.getValue(), 1e-4);
    }

    // Tests maximization goal type
    @Test
    public void testOptimize_maximizeQuadraticFunction_convergesToMaximum() {
        SimplexOptimizer optimizer = new SimplexOptimizer(1e-10, 1e-30);
        MultivariateFunction invertedParaboloid = new MultivariateFunction() {
            public double value(double[] point) {
                return 10.0 - (point[0] - 3.0) * (point[0] - 3.0) - (point[1] - 4.0) * (point[1] - 4.0);
            }
        };

        PointValuePair optimum = optimizer.optimize(
            new MaxEval(200),
            new ObjectiveFunction(invertedParaboloid),
            GoalType.MAXIMIZE,
            new InitialGuess(new double[] { 0.0, 0.0 }),
            new NelderMeadSimplex(2)
        );

        Assert.assertEquals(3.0, optimum.getPoint()[0], 1e-3);
        Assert.assertEquals(4.0, optimum.getPoint()[1], 1e-3);
        Assert.assertEquals(10.0, optimum.getValue(), 1e-4);
    }

    // Tests reusing simplex across consecutive optimize calls without passing it again
    @Test
    public void testOptimize_reusingSimplexAcrossCalls_succeeds() {
        SimplexOptimizer optimizer = new SimplexOptimizer(1e-10, 1e-30);
        MultivariateFunction paraboloid = new MultivariateFunction() {
            public double value(double[] point) {
                return point[0] * point[0] + point[1] * point[1];
            }
        };

        optimizer.optimize(
            new MaxEval(200),
            new ObjectiveFunction(paraboloid),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] { 1.0, 1.0 }),
            new NelderMeadSimplex(2)
        );

        PointValuePair optimum = optimizer.optimize(
            new MaxEval(200),
            new ObjectiveFunction(paraboloid),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] { 2.0, 2.0 })
        );

        Assert.assertEquals(0.0, optimum.getPoint()[0], 1e-3);
        Assert.assertEquals(0.0, optimum.getPoint()[1], 1e-3);
        Assert.assertEquals(0.0, optimum.getValue(), 1e-4);
    }

    // Tests 1D optimization problem
    @Test
    public void testOptimize_oneDimensionalFunction_converges() {
        SimplexOptimizer optimizer = new SimplexOptimizer(1e-10, 1e-30);
        MultivariateFunction quad1D = new MultivariateFunction() {
            public double value(double[] point) {
                return (point[0] - 5.0) * (point[0] - 5.0);
            }
        };

        PointValuePair optimum = optimizer.optimize(
            new MaxEval(100),
            new ObjectiveFunction(quad1D),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] { 0.0 }),
            new NelderMeadSimplex(1)
        );

        Assert.assertEquals(5.0, optimum.getPoint()[0], 1e-3);
        Assert.assertEquals(0.0, optimum.getValue(), 1e-4);
    }

    // Tests exceeding max evaluations throws TooManyEvaluationsException
    @Test(expected = TooManyEvaluationsException.class)
    public void testOptimize_exceedsMaxEvaluations_throwsTooManyEvaluationsException() {
        SimplexOptimizer optimizer = new SimplexOptimizer(1e-15, 1e-30);
        MultivariateFunction paraboloid = new MultivariateFunction() {
            public double value(double[] point) {
                return point[0] * point[0] + point[1] * point[1];
            }
        };

        optimizer.optimize(
            new MaxEval(5),
            new ObjectiveFunction(paraboloid),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] { 10.0, 10.0 }),
            new NelderMeadSimplex(2)
        );
    }

    // Tests custom step size simplex
    @Test
    public void testOptimize_customStepsNelderMead_converges() {
        SimplexOptimizer optimizer = new SimplexOptimizer(1e-10, 1e-30);
        MultivariateFunction fourExtrema = new FourExtrema();

        PointValuePair optimum = optimizer.optimize(
            new MaxEval(200),
            new ObjectiveFunction(fourExtrema),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] { -3.0, 0.0 }),
            new NelderMeadSimplex(new double[] { 0.2, 0.2 })
        );

        Assert.assertEquals(-3.1415926, optimum.getPoint()[0], 1e-2);
        Assert.assertEquals(0.0, optimum.getPoint()[1], 1e-2);
    }

    private static class FourExtrema implements MultivariateFunction {
        public static final double X_M = -Double.valueOf(Math.PI);
        public static final double Y_M = 0;

        public double value(double[] point) {
            double x = point[0];
            double y = point[1];
            return (x - X_M) * (x - X_M) + (y - Y_M) * (y - Y_M);
        }
    }
}