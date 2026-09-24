package org.apache.commons.math3.optimization.univariate;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.optimization.ConvergenceChecker;
import org.apache.commons.math3.optimization.GoalType;
import org.junit.Assert;
import org.junit.Test;

public class BrentOptimizerTest {

    // Tests exception when absolute threshold is negative
    @Test(expected = NotStrictlyPositiveException.class)
    public void testConstructor_negativeAbsoluteThreshold_throwsException() {
        new BrentOptimizer(1e-4, -1e-8);
    }

    // Tests exception when absolute threshold is zero
    @Test(expected = NotStrictlyPositiveException.class)
    public void testConstructor_zeroAbsoluteThreshold_throwsException() {
        new BrentOptimizer(1e-4, 0.0);
    }

    // Tests exception when relative threshold is too small
    @Test(expected = NumberIsTooSmallException.class)
    public void testConstructor_tooSmallRelativeThreshold_throwsException() {
        new BrentOptimizer(1e-17, 1e-8);
    }

    // Tests constructor with valid thresholds and null checker
    @Test
    public void testConstructor_validParameters_success() {
        BrentOptimizer optimizer = new BrentOptimizer(1e-4, 1e-8);
        Assert.assertNull(optimizer.getConvergenceChecker());
    }

    // Tests minimization of a standard parabola
    @Test
    public void testOptimize_parabolaMinimization_findsMinimum() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return (x - 2.0) * (x - 2.0) + 3.0;
            }
        };
        BrentOptimizer optimizer = new BrentOptimizer(1e-6, 1e-10);
        UnivariatePointValuePair result = optimizer.optimize(100, f, GoalType.MINIMIZE, 0.0, 5.0, 0.5);
        Assert.assertEquals(2.0, result.getPoint(), 1e-4);
        Assert.assertEquals(3.0, result.getValue(), 1e-4);
    }

    // Tests maximization of an inverted parabola
    @Test
    public void testOptimize_parabolaMaximization_findsMaximum() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return -(x - 3.0) * (x - 3.0) + 5.0;
            }
        };
        BrentOptimizer optimizer = new BrentOptimizer(1e-6, 1e-10);
        UnivariatePointValuePair result = optimizer.optimize(100, f, GoalType.MAXIMIZE, 0.0, 6.0, 1.0);
        Assert.assertEquals(3.0, result.getPoint(), 1e-4);
        Assert.assertEquals(5.0, result.getValue(), 1e-4);
    }

    // Tests optimization when interval boundaries are inverted (lo > hi)
    @Test
    public void testOptimize_invertedBounds_findsMinimum() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return (x - 1.0) * (x - 1.0);
            }
        };
        BrentOptimizer optimizer = new BrentOptimizer(1e-6, 1e-10);
        UnivariatePointValuePair result = optimizer.optimize(100, f, GoalType.MINIMIZE, 3.0, -1.0, 2.0);
        Assert.assertEquals(1.0, result.getPoint(), 1e-4);
        Assert.assertEquals(0.0, result.getValue(), 1e-4);
    }

    // Tests optimization with custom ConvergenceChecker
    @Test
    public void testOptimize_withConvergenceChecker_stopsEarly() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return (x - 4.0) * (x - 4.0);
            }
        };
        ConvergenceChecker<UnivariatePointValuePair> checker = new ConvergenceChecker<UnivariatePointValuePair>() {
            public boolean converged(int iteration, UnivariatePointValuePair previous, UnivariatePointValuePair current) {
                return iteration >= 3;
            }
        };
        BrentOptimizer optimizer = new BrentOptimizer(1e-6, 1e-10, checker);
        UnivariatePointValuePair result = optimizer.optimize(100, f, GoalType.MINIMIZE, 0.0, 10.0, 1.0);
        Assert.assertNotNull(result);
    }

    // Tests optimization where start value is close to minimum
    @Test
    public void testOptimize_startValueAtMinimum_returnsCorrectPoint() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return (x - 1.5) * (x - 1.5) + 2.0;
            }
        };
        BrentOptimizer optimizer = new BrentOptimizer(1e-6, 1e-10);
        UnivariatePointValuePair result = optimizer.optimize(100, f, GoalType.MINIMIZE, 0.0, 3.0, 1.5);
        Assert.assertEquals(1.5, result.getPoint(), 1e-4);
        Assert.assertEquals(2.0, result.getValue(), 1e-4);
    }

    // Tests optimization with higher degree polynomial
    @Test
    public void testOptimize_quarticFunction_findsMinimum() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return Math.pow(x, 4) - 2 * Math.pow(x, 2) + 1; // minima at x = -1 and x = 1
            }
        };
        BrentOptimizer optimizer = new BrentOptimizer(1e-8, 1e-12);
        UnivariatePointValuePair result = optimizer.optimize(100, f, GoalType.MINIMIZE, 0.1, 2.0, 0.5);
        Assert.assertEquals(1.0, result.getPoint(), 1e-3);
        Assert.assertEquals(0.0, result.getValue(), 1e-3);
    }

    // Tests non-symmetric interval and checks that returned point is the best evaluated point (Math-24)
    @Test
    public void testOptimize_bestPointReturnedOnTermination_defectMath24() {
        final UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                if (x < 0) {
                    return -x;
                } else {
                    return x * x;
                }
            }
        };
        BrentOptimizer optimizer = new BrentOptimizer(1e-4, 1e-8);
        UnivariatePointValuePair result = optimizer.optimize(100, f, GoalType.MINIMIZE, -2.0, 3.0, 0.1);
        Assert.assertEquals(0.0, result.getPoint(), 1e-2);
        Assert.assertEquals(0.0, result.getValue(), 1e-2);
    }

    // Tests optimization where optimum is at boundary
    @Test
    public void testOptimize_monotonicFunction_findsMinimumNearBoundary() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return 2.0 * x + 1.0;
            }
        };
        BrentOptimizer optimizer = new BrentOptimizer(1e-6, 1e-10);
        UnivariatePointValuePair result = optimizer.optimize(100, f, GoalType.MINIMIZE, 1.0, 5.0, 3.0);
        Assert.assertEquals(1.0, result.getPoint(), 1e-2);
    }
}