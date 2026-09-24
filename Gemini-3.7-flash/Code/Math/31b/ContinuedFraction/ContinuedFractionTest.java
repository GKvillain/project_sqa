package org.apache.commons.math3.util;

import org.apache.commons.math3.exception.ConvergenceException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for {@link ContinuedFraction}.
 */
public class ContinuedFractionTest {

    // Tests golden ratio continued fraction evaluation with default settings
    @Test
    public void testEvaluate_goldenRatio_returnsCorrectValue() {
        ContinuedFraction cf = new ContinuedFraction() {
            @Override
            protected double getA(int n, double x) {
                return 1.0;
            }

            @Override
            protected double getB(int n, double x) {
                return 1.0;
            }
        };

        double expected = (1.0 + FastMath.sqrt(5.0)) / 2.0;
        double actual = cf.evaluate(0.0);
        Assert.assertEquals(expected, actual, 1e-8);
    }

    // Tests square root of 2 continued fraction: [1; 2, 2, 2, ...]
    @Test
    public void testEvaluate_sqrtTwo_returnsCorrectValue() {
        ContinuedFraction cf = new ContinuedFraction() {
            @Override
            protected double getA(int n, double x) {
                return (n == 0) ? 1.0 : 2.0;
            }

            @Override
            protected double getB(int n, double x) {
                return 1.0;
            }
        };

        double expected = FastMath.sqrt(2.0);
        double actual = cf.evaluate(0.0);
        Assert.assertEquals(expected, actual, 1e-8);
    }

    // Tests condition where leading term a0 is zero (branch where hPrev == 0.0 is adjusted to small)
    @Test
    public void testEvaluate_initialZeroA_convergesCorrectly() {
        ContinuedFraction cf = new ContinuedFraction() {
            @Override
            protected double getA(int n, double x) {
                return (n == 0) ? 0.0 : 1.0;
            }

            @Override
            protected double getB(int n, double x) {
                return 1.0;
            }
        };

        double expected = (FastMath.sqrt(5.0) - 1.0) / 2.0;
        double actual = cf.evaluate(0.0);
        Assert.assertEquals(expected, actual, 1e-8);
    }

    // Tests evaluation overload with custom epsilon
    @Test
    public void testEvaluate_customEpsilon_convergesWithinTolerance() {
        ContinuedFraction cf = new ContinuedFraction() {
            @Override
            protected double getA(int n, double x) {
                return 1.0;
            }

            @Override
            protected double getB(int n, double x) {
                return 1.0;
            }
        };

        double expected = (1.0 + FastMath.sqrt(5.0)) / 2.0;
        double actual = cf.evaluate(0.0, 1e-4);
        Assert.assertEquals(expected, actual, 1e-4);
    }

    // Tests evaluation overload with custom max iterations
    @Test
    public void testEvaluate_customMaxIterations_converges() {
        ContinuedFraction cf = new ContinuedFraction() {
            @Override
            protected double getA(int n, double x) {
                return 1.0;
            }

            @Override
            protected double getB(int n, double x) {
                return 1.0;
            }
        };

        double expected = (1.0 + FastMath.sqrt(5.0)) / 2.0;
        double actual = cf.evaluate(0.0, 100);
        Assert.assertEquals(expected, actual, 1e-8);
    }

    // Tests max iterations exceeded throwing MaxCountExceededException
    @Test(expected = MaxCountExceededException.class)
    public void testEvaluate_insufficientIterations_throwsMaxCountExceededException() {
        ContinuedFraction cf = new ContinuedFraction() {
            @Override
            protected double getA(int n, double x) {
                return 1.0;
            }

            @Override
            protected double getB(int n, double x) {
                return 1.0;
            }
        };

        cf.evaluate(0.0, 1e-12, 2);
    }

    // Tests NaN coefficient resulting in ConvergenceException
    @Test(expected = ConvergenceException.class)
    public void testEvaluate_nanCoefficient_throwsConvergenceException() {
        ContinuedFraction cf = new ContinuedFraction() {
            @Override
            protected double getA(int n, double x) {
                return (n == 1) ? Double.NaN : 1.0;
            }

            @Override
            protected double getB(int n, double x) {
                return 1.0;
            }
        };

        cf.evaluate(0.0);
    }

    // Tests infinite divergence with negative scale factor throwing ConvergenceException
    @Test(expected = ConvergenceException.class)
    public void testEvaluate_infiniteDivergenceNegativeScale_throwsConvergenceException() {
        ContinuedFraction cf = new ContinuedFraction() {
            @Override
            protected double getA(int n, double x) {
                return (n == 1) ? Double.POSITIVE_INFINITY : -1.0;
            }

            @Override
            protected double getB(int n, double x) {
                return -2.0;
            }
        };

        cf.evaluate(0.0);
    }

    // Tests scaling loop branch when a is larger than b and triggers infinity scaling
    @Test
    public void testEvaluate_infiniteIntermediateScaling_recoversOrEvaluates() {
        ContinuedFraction cf = new ContinuedFraction() {
            @Override
            protected double getA(int n, double x) {
                return (n == 1) ? Double.MAX_VALUE : 1.0;
            }

            @Override
            protected double getB(int n, double x) {
                return 1.0;
            }
        };

        double result = cf.evaluate(0.0, 1e-5, 10);
        Assert.assertFalse(Double.isNaN(result));
    }

    // Tests scaling loop branch when b is larger than a and triggers infinity scaling
    @Test
    public void testEvaluate_infiniteIntermediateScalingBLarger_recoversOrEvaluates() {
        ContinuedFraction cf = new ContinuedFraction() {
            @Override
            protected double getA(int n, double x) {
                return 1.0;
            }

            @Override
            protected double getB(int n, double x) {
                return (n == 1) ? Double.MAX_VALUE : 1.0;
            }
        };

        double result = cf.evaluate(0.0, 1e-5, 10);
        Assert.assertFalse(Double.isNaN(result));
    }
}