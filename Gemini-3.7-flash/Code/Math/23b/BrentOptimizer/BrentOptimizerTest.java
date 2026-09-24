package org.apache.commons.math3.optimization.univariate;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.optimization.ConvergenceChecker;
import org.apache.commons.math3.optimization.GoalType;
import org.apache.commons.math3.util.FastMath;
import org.junit.Assert;
import org.junit.Test;

public class BrentOptimizerTest {

    // Tests exception when relative threshold is smaller than minimum allowed
    @Test(expected = NumberIsTooSmallException.class)
    public void testConstructor_relativeThresholdTooSmall_throwsNumberIsTooSmallException() {
        new BrentOptimizer(1e-17, 1e-10);
    }

    // Tests exception when absolute threshold is zero
    @Test(expected = NotStrictlyPositiveException.class)
    public void testConstructor_absoluteThresholdZero_throwsNotStrictlyPositiveException() {
        new BrentOptimizer(1e-10, 0.0);
    }

    // Tests exception when absolute threshold is negative
    @Test(expected = NotStrictlyPositiveException.class)
    public void testConstructor_absoluteThresholdNegative_throwsNotStrictlyPositiveException() {
        new BrentOptimizer(1e-10, -1.0);
    }

    // Tests minimizing a quadratic function x^2
    @Test
    public void testOptimize_quadraticFunctionMinimization_findsMinimum() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return (x - 3.0) * (x - 3.0) + 5.0;
            }
        };
        BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        UnivariatePointValuePair result = optimizer.optimize(100, f, GoalType.MINIMIZE, 0.0, 5.0, 1.0);
        Assert.assertEquals(3.0, result.getPoint(), 1e-8);
        Assert.assertEquals(5.0, result.getValue(), 1e-8);
    }

    // Tests maximizing a concave quadratic function -(x-2)^2 + 4
    @Test
    public void testOptimize_quadraticFunctionMaximization_findsMaximum() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return -(x - 2.0) * (x - 2.0) + 4.0;
            }
        };
        BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        UnivariatePointValuePair result = optimizer.optimize(100, f, GoalType.MAXIMIZE, 0.0, 4.0, 0.5);
        Assert.assertEquals(2.0, result.getPoint(), 1e-8);
        Assert.assertEquals(4.0, result.getValue(), 1e-8);
    }

    // Tests minimizing sine function around 3*PI/2
    @Test
    public void testOptimize_sinFunction_findsMinimum() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return FastMath.sin(x);
            }
        };
        BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        UnivariatePointValuePair result = optimizer.optimize(100, f, GoalType.MINIMIZE, 4.0, 6.0);
        Assert.assertEquals(1.5 * FastMath.PI, result.getPoint(), 1e-8);
        Assert.assertEquals(-1.0, result.getValue(), 1e-8);
    }

    // Tests optimization when interval bounds are inverted (lo > hi)
    @Test
    public void testOptimize_invertedBounds_findsMinimumCorrectly() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return (x - 1.0) * (x - 1.0);
            }
        };
        BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        UnivariatePointValuePair result = optimizer.optimize(100, f, GoalType.MINIMIZE, 3.0, -1.0, 0.0);
        Assert.assertEquals(1.0, result.getPoint(), 1e-8);
        Assert.assertEquals(0.0, result.getValue(), 1e-8);
    }

    // Tests keeping the initial guess if it is already the optimal point
    @Test
    public void testOptimize_initialGuessIsOptimal_returnsInitialGuess() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return x * x;
            }
        };
        BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        UnivariatePointValuePair result = optimizer.optimize(100, f, GoalType.MINIMIZE, -0.5, 0.5, 0.0);
        Assert.assertEquals(0.0, result.getPoint(), 1e-10);
        Assert.assertEquals(0.0, result.getValue(), 1e-10);
    }

    // Tests optimization with a custom convergence checker
    @Test
    public void testOptimize_customConvergenceChecker_terminatesWhenCheckerConverged() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return (x - 4.0) * (x - 4.0);
            }
        };
        ConvergenceChecker<UnivariatePointValuePair> checker = new ConvergenceChecker<UnivariatePointValuePair>() {
            public boolean converged(int iteration, UnivariatePointValuePair previous, UnivariatePointValuePair current) {
                return previous != null && FastMath.abs(previous.getPoint() - current.getPoint()) < 0.1;
            }
        };
        BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14, checker);
        UnivariatePointValuePair result = optimizer.optimize(100, f, GoalType.MINIMIZE, 0.0, 10.0, 2.0);
        Assert.assertEquals(4.0, result.getPoint(), 0.1);
        Assert.assertEquals(0.0, result.getValue(), 0.1);
    }

    // Tests polynomial with multiple local features
    @Test
    public void testOptimize_polynomialFunction_findsLocalMinimum() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return (x - 1.0) * (x - 2.0) * (x - 3.0) * (x - 4.0);
            }
        };
        BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        UnivariatePointValuePair result = optimizer.optimize(100, f, GoalType.MINIMIZE, 2.5, 4.5, 3.5);
        Assert.assertTrue(result.getPoint() > 3.0 && result.getPoint() < 4.0);
        Assert.assertTrue(result.getValue() < 0.0);
    }

    // Tests maximizing when optimal point is at initial value
    @Test
    public void testOptimize_initialGuessIsMaximum_returnsMaximum() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return -x * x + 10.0;
            }
        };
        BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        UnivariatePointValuePair result = optimizer.optimize(100, f, GoalType.MAXIMIZE, -2.0, 2.0, 0.0);
        Assert.assertEquals(0.0, result.getPoint(), 1e-10);
        Assert.assertEquals(10.0, result.getValue(), 1e-10);
    }

    // Tests minimum relative tolerance boundary check
    @Test
    public void testConstructor_minimumAllowedRelativeTolerance_instantiatesSuccessfully() {
        double minRel = 2 * FastMath.ulp(1.0);
        BrentOptimizer optimizer = new BrentOptimizer(minRel, 1e-10);
        Assert.assertNotNull(optimizer);
    }
}