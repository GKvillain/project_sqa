package org.apache.commons.math.optimization.direct;

import org.apache.commons.math.analysis.MultivariateFunction;
import org.apache.commons.math.exception.NumberIsTooSmallException;
import org.apache.commons.math.exception.OutOfRangeException;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.RealPointValuePair;
import org.junit.Assert;
import org.junit.Test;

public class BOBYQAOptimizerTest {

    // Tests problem dimension smaller than MINIMUM_PROBLEM_DIMENSION throws exception
    @Test(expected = NumberIsTooSmallException.class)
    public void testSetup_dimensionTooSmall_throwsNumberIsTooSmallException() {
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(4);
        MultivariateFunction function = new MultivariateFunction() {
            public double value(double[] point) {
                return point[0] * point[0];
            }
        };
        optimizer.optimize(100, function, GoalType.MINIMIZE, new double[] { 1.0 }, new double[] { 0.0 }, new double[] { 2.0 });
    }

    // Tests number of interpolation points smaller than lower bound throws exception
    @Test(expected = OutOfRangeException.class)
    public void testSetup_interpolationPointsTooFew_throwsOutOfRangeException() {
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(3); // n=2 requires at least n+2 = 4
        MultivariateFunction function = new MultivariateFunction() {
            public double value(double[] point) {
                return point[0] * point[0] + point[1] * point[1];
            }
        };
        optimizer.optimize(100, function, GoalType.MINIMIZE, new double[] { 1.0, 1.0 }, new double[] { 0.0, 0.0 }, new double[] { 2.0, 2.0 });
    }

    // Tests number of interpolation points greater than upper bound throws exception
    @Test(expected = OutOfRangeException.class)
    public void testSetup_interpolationPointsTooMany_throwsOutOfRangeException() {
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(7); // n=2 requires at most (n+1)(n+2)/2 = 6
        MultivariateFunction function = new MultivariateFunction() {
            public double value(double[] point) {
                return point[0] * point[0] + point[1] * point[1];
            }
        };
        optimizer.optimize(100, function, GoalType.MINIMIZE, new double[] { 1.0, 1.0 }, new double[] { 0.0, 0.0 }, new double[] { 2.0, 2.0 });
    }

    // Tests minimization of 2D sphere function
    @Test
    public void testOptimize_sphereFunctionMinimization_findsOptimum() {
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(5);
        MultivariateFunction function = new MultivariateFunction() {
            public double value(double[] point) {
                double d0 = point[0] - 1.5;
                double d1 = point[1] + 2.0;
                return d0 * d0 + d1 * d1;
            }
        };
        double[] start = new double[] { 0.0, 0.0 };
        double[] lower = new double[] { -10.0, -10.0 };
        double[] upper = new double[] { 10.0, 10.0 };

        RealPointValuePair result = optimizer.optimize(200, function, GoalType.MINIMIZE, start, lower, upper);
        Assert.assertEquals(1.5, result.getPoint()[0], 1e-4);
        Assert.assertEquals(-2.0, result.getPoint()[1], 1e-4);
        Assert.assertEquals(0.0, result.getValue(), 1e-4);
    }

    // Tests maximization of 2D parabolic function
    @Test
    public void testOptimize_parabolicFunctionMaximization_findsOptimum() {
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(5);
        MultivariateFunction function = new MultivariateFunction() {
            public double value(double[] point) {
                double d0 = point[0] - 2.0;
                double d1 = point[1] - 3.0;
                return 10.0 - (d0 * d0 + d1 * d1);
            }
        };
        double[] start = new double[] { 0.0, 0.0 };
        double[] lower = new double[] { -10.0, -10.0 };
        double[] upper = new double[] { 10.0, 10.0 };

        RealPointValuePair result = optimizer.optimize(200, function, GoalType.MAXIMIZE, start, lower, upper);
        Assert.assertEquals(2.0, result.getPoint()[0], 1e-4);
        Assert.assertEquals(3.0, result.getPoint()[1], 1e-4);
        Assert.assertEquals(10.0, result.getValue(), 1e-4);
    }

    // Tests narrow bounds where minDiff < 2 * initialTrustRegionRadius
    @Test
    public void testOptimize_narrowBounds_adjustsInitialRadiusAndFindsOptimum() {
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(5, 10.0, 1e-8);
        MultivariateFunction function = new MultivariateFunction() {
            public double value(double[] point) {
                return (point[0] - 0.5) * (point[0] - 0.5) + (point[1] - 0.5) * (point[1] - 0.5);
            }
        };
        double[] start = new double[] { 0.2, 0.2 };
        double[] lower = new double[] { 0.0, 0.0 };
        double[] upper = new double[] { 1.0, 1.0 }; // bound diff = 1.0 < 2 * 10.0

        RealPointValuePair result = optimizer.optimize(300, function, GoalType.MINIMIZE, start, lower, upper);
        Assert.assertEquals(0.5, result.getPoint()[0], 1e-3);
        Assert.assertEquals(0.5, result.getPoint()[1], 1e-3);
        Assert.assertEquals(0.0, result.getValue(), 1e-3);
    }

    // Tests start point placed exactly on the lower bound
    @Test
    public void testOptimize_startPointAtLowerBound_convergesCorrectly() {
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(5);
        MultivariateFunction function = new MultivariateFunction() {
            public double value(double[] point) {
                return (point[0] - 2.0) * (point[0] - 2.0) + (point[1] - 2.0) * (point[1] - 2.0);
            }
        };
        double[] start = new double[] { 0.0, 0.0 };
        double[] lower = new double[] { 0.0, 0.0 };
        double[] upper = new double[] { 5.0, 5.0 };

        RealPointValuePair result = optimizer.optimize(200, function, GoalType.MINIMIZE, start, lower, upper);
        Assert.assertEquals(2.0, result.getPoint()[0], 1e-4);
        Assert.assertEquals(2.0, result.getPoint()[1], 1e-4);
    }

    // Tests start point placed exactly on the upper bound
    @Test
    public void testOptimize_startPointAtUpperBound_convergesCorrectly() {
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(5);
        MultivariateFunction function = new MultivariateFunction() {
            public double value(double[] point) {
                return (point[0] - 3.0) * (point[0] - 3.0) + (point[1] - 3.0) * (point[1] - 3.0);
            }
        };
        double[] start = new double[] { 5.0, 5.0 };
        double[] lower = new double[] { 0.0, 0.0 };
        double[] upper = new double[] { 5.0, 5.0 };

        RealPointValuePair result = optimizer.optimize(200, function, GoalType.MINIMIZE, start, lower, upper);
        Assert.assertEquals(3.0, result.getPoint()[0], 1e-4);
        Assert.assertEquals(3.0, result.getPoint()[1], 1e-4);
    }

    // Tests optimization with optimum constrained at the boundary
    @Test
    public void testOptimize_optimumOnBoundary_convergesToBoundary() {
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(5);
        MultivariateFunction function = new MultivariateFunction() {
            public double value(double[] point) {
                return (point[0] - 10.0) * (point[0] - 10.0) + (point[1] - 10.0) * (point[1] - 10.0);
            }
        };
        double[] start = new double[] { 1.0, 1.0 };
        double[] lower = new double[] { 0.0, 0.0 };
        double[] upper = new double[] { 5.0, 5.0 };

        RealPointValuePair result = optimizer.optimize(200, function, GoalType.MINIMIZE, start, lower, upper);
        Assert.assertEquals(5.0, result.getPoint()[0], 1e-3);
        Assert.assertEquals(5.0, result.getPoint()[1], 1e-3);
        Assert.assertEquals(50.0, result.getValue(), 1e-2);
    }

    // Tests Rosenbrock 2D banana function
    @Test
    public void testOptimize_rosenbrock2D_findsOptimum() {
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(6);
        MultivariateFunction rosenbrock = new MultivariateFunction() {
            public double value(double[] x) {
                double a = x[1] - x[0] * x[0];
                double b = 1.0 - x[0];
                return 100.0 * a * a + b * b;
            }
        };
        double[] start = new double[] { -1.2, 1.0 };
        double[] lower = new double[] { -5.0, -5.0 };
        double[] upper = new double[] { 5.0, 5.0 };

        RealPointValuePair result = optimizer.optimize(500, rosenbrock, GoalType.MINIMIZE, start, lower, upper);
        Assert.assertEquals(1.0, result.getPoint()[0], 1e-2);
        Assert.assertEquals(1.0, result.getPoint()[1], 1e-2);
        Assert.assertEquals(0.0, result.getValue(), 1e-2);
    }

    // Tests 3D optimization with higher number of interpolation points (npt > 2*n + 1)
    @Test
    public void testOptimize_3DWithNineInterpolationPoints_findsOptimum() {
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(9); // n=3, 2n+1=7, npt=9 > 2n+1
        MultivariateFunction function = new MultivariateFunction() {
            public double value(double[] point) {
                double d0 = point[0] - 1.0;
                double d1 = point[1] - 2.0;
                double d2 = point[2] - 3.0;
                return d0 * d0 + d1 * d1 + d2 * d2;
            }
        };
        double[] start = new double[] { 0.0, 0.0, 0.0 };
        double[] lower = new double[] { -5.0, -5.0, -5.0 };
        double[] upper = new double[] { 5.0, 5.0, 5.0 };

        RealPointValuePair result = optimizer.optimize(400, function, GoalType.MINIMIZE, start, lower, upper);
        Assert.assertEquals(1.0, result.getPoint()[0], 1e-3);
        Assert.assertEquals(2.0, result.getPoint()[1], 1e-3);
        Assert.assertEquals(3.0, result.getPoint()[2], 1e-3);
    }

    // Tests 4D optimization with minimum interpolation conditions (npt = n + 2)
    @Test
    public void testOptimize_4DWithMinimumInterpolationPoints_findsOptimum() {
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(6); // n=4, min points = 4+2 = 6
        MultivariateFunction function = new MultivariateFunction() {
            public double value(double[] point) {
                double sum = 0.0;
                for (int i = 0; i < point.length; i++) {
                    double diff = point[i] - (i + 1.0);
                    sum += diff * diff;
                }
                return sum;
            }
        };
        double[] start = new double[] { 0.0, 0.0, 0.0, 0.0 };
        double[] lower = new double[] { -10.0, -10.0, -10.0, -10.0 };
        double[] upper = new double[] { 10.0, 10.0, 10.0, 10.0 };

        RealPointValuePair result = optimizer.optimize(500, function, GoalType.MINIMIZE, start, lower, upper);
        for (int i = 0; i < 4; i++) {
            Assert.assertEquals(i + 1.0, result.getPoint()[i], 1e-2);
        }
    }

    // Tests custom initial and stopping radius constructor
    @Test
    public void testOptimize_customRadii_convergesAccurately() {
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(6, 2.0, 1e-6);
        MultivariateFunction function = new MultivariateFunction() {
            public double value(double[] point) {
                return (point[0] - 0.3) * (point[0] - 0.3) + (point[1] + 0.7) * (point[1] + 0.7);
            }
        };
        double[] start = new double[] { 0.0, 0.0 };
        double[] lower = new double[] { -5.0, -5.0 };
        double[] upper = new double[] { 5.0, 5.0 };

        RealPointValuePair result = optimizer.optimize(300, function, GoalType.MINIMIZE, start, lower, upper);
        Assert.assertEquals(0.3, result.getPoint()[0], 1e-4);
        Assert.assertEquals(-0.7, result.getPoint()[1], 1e-4);
        Assert.assertEquals(0.0, result.getValue(), 1e-4);
    }
}