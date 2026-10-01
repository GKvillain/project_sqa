package org.apache.commons.math3.optim.nonlinear.scalar.noderiv;

import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.ConvergenceChecker;
import org.apache.commons.math3.optim.SimpleValueChecker;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.exception.MathUnsupportedOperationException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.junit.Test;
import static org.junit.Assert.*;

public class PowellOptimizerTest {

    @Test(expected = NumberIsTooSmallException.class)
    public void testConstructor_relBelowMin_ThrowsNumberIsTooSmall() {
        new PowellOptimizer(1e-16, 1e-10);
    }

    @Test(expected = NotStrictlyPositiveException.class)
    public void testConstructor_absNonPositive_ThrowsNotStrictlyPositive() {
        new PowellOptimizer(1e-8, 0);
    }

    @Test
    public void testOptimize_quadraticMinimize_convergesToMinimum() {
        PowellOptimizer optimizer = new PowellOptimizer(1e-8, 1e-10);
        PointValuePair result = optimizer.optimize(new MaxEval(10000),
                                                   new InitialGuess(new double[]{2, 2}),
                                                   new ObjectiveFunction(quadratic()),
                                                   GoalType.MINIMIZE);
        double[] point = result.getPoint();
        assertEquals(0.0, point[0], 1e-5);
        assertEquals(0.0, point[1], 1e-5);
        assertEquals(0.0, result.getValue(), 1e-5);
    }

    @Test
    public void testOptimize_quadraticMaximize_convergesToMaximum() {
        PowellOptimizer optimizer = new PowellOptimizer(1e-8, 1e-10);
        PointValuePair result = optimizer.optimize(new MaxEval(10000),
                                                   new InitialGuess(new double[]{2, 2}),
                                                   new ObjectiveFunction(concaveQuadratic()),
                                                   GoalType.MAXIMIZE);
        double[] point = result.getPoint();
        assertEquals(0.0, point[0], 1e-5);
        assertEquals(0.0, point[1], 1e-5);
        assertEquals(0.0, result.getValue(), 1e-5);
    }

    @Test
    public void testOptimize_rosenbrockMinimize_convergesToMinimum() {
        PowellOptimizer optimizer = new PowellOptimizer(1e-8, 1e-10);
        PointValuePair result = optimizer.optimize(new MaxEval(20000),
                                                   new InitialGuess(new double[]{-1, 2}),
                                                   new ObjectiveFunction(rosenbrock()),
                                                   GoalType.MINIMIZE);
        double[] point = result.getPoint();
        assertEquals(1.0, point[0], 1e-3);
        assertEquals(1.0, point[1], 1e-3);
        assertEquals(0.0, result.getValue(), 1e-3);
    }

    @Test
    public void testOptimize_startAtMinimum_returnsSamePoint() {
        PowellOptimizer optimizer = new PowellOptimizer(1e-8, 1e-10);
        PointValuePair result = optimizer.optimize(new MaxEval(100),
                                                   new InitialGuess(new double[]{0, 0}),
                                                   new ObjectiveFunction(quadratic()),
                                                   GoalType.MINIMIZE);
        double[] point = result.getPoint();
        assertEquals(0.0, point[0], 1e-8);
        assertEquals(0.0, point[1], 1e-8);
        assertEquals(0.0, result.getValue(), 1e-8);
    }

    @Test
    public void testOptimize_startAtMaximum_returnsSamePoint() {
        PowellOptimizer optimizer = new PowellOptimizer(1e-8, 1e-10);
        PointValuePair result = optimizer.optimize(new MaxEval(100),
                                                   new InitialGuess(new double[]{0, 0}),
                                                   new ObjectiveFunction(concaveQuadratic()),
                                                   GoalType.MAXIMIZE);
        double[] point = result.getPoint();
        assertEquals(0.0, point[0], 1e-8);
        assertEquals(0.0, point[1], 1e-8);
        assertEquals(0.0, result.getValue(), 1e-8);
    }

    @Test
    public void testOptimize_oneDimension_quadraticMinimize_convergesToMinimum() {
        PowellOptimizer optimizer = new PowellOptimizer(1e-8, 1e-10);
        PointValuePair result = optimizer.optimize(new MaxEval(1000),
                                                   new InitialGuess(new double[]{0}),
                                                   new ObjectiveFunction(oneDimensionalQuadratic()),
                                                   GoalType.MINIMIZE);
        double[] point = result.getPoint();
        assertEquals(2.0, point[0], 1e-5);
        assertEquals(0.0, result.getValue(), 1e-5);
    }

    @Test(expected = MathUnsupportedOperationException.class)
    public void testOptimize_withBounds_ThrowsMathUnsupportedOperationException() {
        PowellOptimizer optimizer = new PowellOptimizer(1e-8, 1e-10);
        optimizer.optimize(new MaxEval(1000),
                           new InitialGuess(new double[]{0, 0}),
                           new ObjectiveFunction(quadratic()),
                           GoalType.MINIMIZE,
                           new org.apache.commons.math3.optim.SimpleBounds(new double[]{-1, -1},
                                                                           new double[]{1, 1}));
    }

    @Test
    public void testOptimize_withCustomConvergenceChecker_returnsPoint() {
        ConvergenceChecker<PointValuePair> checker = new ConvergenceChecker<PointValuePair>() {
            public boolean converged(int iteration, PointValuePair previous, PointValuePair current) {
                return true;
            }
        };
        PowellOptimizer optimizer = new PowellOptimizer(1e-8, 1e-10, checker);
        PointValuePair result = optimizer.optimize(new MaxEval(100),
                                                   new InitialGuess(new double[]{1, 1}),
                                                   new ObjectiveFunction(quadratic()),
                                                   GoalType.MINIMIZE);
        assertEquals(2, result.getPoint().length);
        assertTrue(result.getValue() >= 0);
    }

    @Test
    public void testOptimize_quadraticMinimize_largeTolerances_stopsEarly() {
        PowellOptimizer optimizer = new PowellOptimizer(1e-2, 1e-2);
        PointValuePair result = optimizer.optimize(new MaxEval(1000),
                                                   new InitialGuess(new double[]{0.1, 0.1}),
                                                   new ObjectiveFunction(quadratic()),
                                                   GoalType.MINIMIZE);
        double[] point = result.getPoint();
        assertTrue(point[0] <= 0.1);
        assertTrue(point[1] <= 0.1);
        assertTrue(result.getValue() <= 0.1);
    }

    @Test
    public void testOptimize_quadraticMaximize_largeTolerances_stopsEarly() {
        PowellOptimizer optimizer = new PowellOptimizer(1e-2, 1e-2);
        PointValuePair result = optimizer.optimize(new MaxEval(1000),
                                                   new InitialGuess(new double[]{0.1, 0.1}),
                                                   new ObjectiveFunction(concaveQuadratic()),
                                                   GoalType.MAXIMIZE);
        double[] point = result.getPoint();
        assertEquals(2, point.length);
        assertTrue(result.getValue() < 0);
    }

    @Test
    public void testOptimize_quadraticMinimize_withLineSearchTolerances_usesSpecified() {
        PowellOptimizer optimizer = new PowellOptimizer(1e-8, 1e-10, 1e-6, 1e-8);
        PointValuePair result = optimizer.optimize(new MaxEval(10000),
                                                   new InitialGuess(new double[]{1, 2}),
                                                   new ObjectiveFunction(quadratic()),
                                                   GoalType.MINIMIZE);
        double[] point = result.getPoint();
        assertEquals(0.0, point[0], 1e-4);
        assertEquals(0.0, point[1], 1e-4);
        assertEquals(0.0, result.getValue(), 1e-4);
    }

    // ==================== New tests for uncovered code ====================

    @Test(expected = TooManyEvaluationsException.class)
    public void testOptimize_maxEvalTooSmall_throwsTooManyEvaluations() {
        PowellOptimizer optimizer = new PowellOptimizer(1e-8, 1e-10);
        optimizer.optimize(new MaxEval(2),
                           new InitialGuess(new double[]{2, 2}),
                           new ObjectiveFunction(quadratic()),
                           GoalType.MINIMIZE);
    }

    @Test
    public void testOptimize_threeDimensionalQuadraticMinimize_converges() {
        PowellOptimizer optimizer = new PowellOptimizer(1e-8, 1e-10);
        PointValuePair result = optimizer.optimize(new MaxEval(10000),
                                                   new InitialGuess(new double[]{2, 2, 2}),
                                                   new ObjectiveFunction(threeDimensionalQuadratic()),
                                                   GoalType.MINIMIZE);
        double[] point = result.getPoint();
        assertEquals(3, point.length);
        assertEquals(0.0, point[0], 1e-5);
        assertEquals(0.0, point[1], 1e-5);
        assertEquals(0.0, point[2], 1e-5);
        assertEquals(0.0, result.getValue(), 1e-5);
    }

    @Test
    public void testOptimize_constantFunctionMinimize_convergesImmediately() {
        PowellOptimizer optimizer = new PowellOptimizer(1e-8, 1e-10);
        PointValuePair result = optimizer.optimize(new MaxEval(100),
                                                   new InitialGuess(new double[]{0, 0}),
                                                   new ObjectiveFunction(constantFunction()),
                                                   GoalType.MINIMIZE);
        double[] point = result.getPoint();
        assertEquals(0.0, point[0], 0.0);
        assertEquals(0.0, point[1], 0.0);
        assertEquals(1.0, result.getValue(), 1e-10);
    }

    @Test
    public void testOptimize_rosenbrockWithRelaxedTolerances_converges() {
        PowellOptimizer optimizer = new PowellOptimizer(1e-4, 1e-4);
        PointValuePair result = optimizer.optimize(new MaxEval(20000),
                                                   new InitialGuess(new double[]{-1, 2}),
                                                   new ObjectiveFunction(rosenbrock()),
                                                   GoalType.MINIMIZE);
        double[] point = result.getPoint();
        assertEquals(1.0, point[0], 1e-2);
        assertEquals(1.0, point[1], 1e-2);
        assertEquals(0.0, result.getValue(), 1e-2);
    }

    @Test
    public void testOptimize_quadraticMinimize_withLineSearchTolerances2() {
        // Different line search tolerances from existing test
        PowellOptimizer optimizer = new PowellOptimizer(1e-8, 1e-10, 1e-4, 1e-6);
        PointValuePair result = optimizer.optimize(new MaxEval(10000),
                                                   new InitialGuess(new double[]{1, 2}),
                                                   new ObjectiveFunction(quadratic()),
                                                   GoalType.MINIMIZE);
        double[] point = result.getPoint();
        assertEquals(0.0, point[0], 1e-4);
        assertEquals(0.0, point[1], 1e-4);
        assertEquals(0.0, result.getValue(), 1e-4);
    }

    @Test
    public void testOptimize_oneDimensionalPolynomialMinimize_converges() {
        PowellOptimizer optimizer = new PowellOptimizer(1e-8, 1e-10);
        PointValuePair result = optimizer.optimize(new MaxEval(1000),
                                                   new InitialGuess(new double[]{2}),
                                                   new ObjectiveFunction(oneDimensionalPolynomial()),
                                                   GoalType.MINIMIZE);
        double[] point = result.getPoint();
        assertEquals(0.0, point[0], 1e-5);
        assertEquals(0.0, result.getValue(), 1e-5);
    }

    @Test
    public void testOptimize_withDefaultSimpleValueChecker() {
        // Explicitly use SimpleValueChecker via 3-arg constructor
        SimpleValueChecker checker = new SimpleValueChecker(1e-8, 1e-10);
        PowellOptimizer optimizer = new PowellOptimizer(1e-8, 1e-10, checker);
        PointValuePair result = optimizer.optimize(new MaxEval(1000),
                                                   new InitialGuess(new double[]{2, 2}),
                                                   new ObjectiveFunction(quadratic()),
                                                   GoalType.MINIMIZE);
        double[] point = result.getPoint();
        assertEquals(0.0, point[0], 1e-5);
        assertEquals(0.0, point[1], 1e-5);
        assertEquals(0.0, result.getValue(), 1e-5);
    }

    // ==================== Helper functions ====================

    private MultivariateFunction quadratic() {
        return new MultivariateFunction() {
            public double value(double[] x) {
                return x[0] * x[0] + x[1] * x[1];
            }
        };
    }

    private MultivariateFunction concaveQuadratic() {
        return new MultivariateFunction() {
            public double value(double[] x) {
                return -x[0] * x[0] - x[1] * x[1];
            }
        };
    }

    private MultivariateFunction rosenbrock() {
        return new MultivariateFunction() {
            public double value(double[] x) {
                double a = 1 - x[0];
                double b = x[1] - x[0] * x[0];
                return a * a + 100 * b * b;
            }
        };
    }

    private MultivariateFunction oneDimensionalQuadratic() {
        return new MultivariateFunction() {
            public double value(double[] x) {
                return (x[0] - 2) * (x[0] - 2);
            }
        };
    }

    private MultivariateFunction threeDimensionalQuadratic() {
        return new MultivariateFunction() {
            public double value(double[] x) {
                return x[0] * x[0] + x[1] * x[1] + x[2] * x[2];
            }
        };
    }

    private MultivariateFunction constantFunction() {
        return new MultivariateFunction() {
            public double value(double[] x) {
                return 1.0;
            }
        };
    }

    private MultivariateFunction oneDimensionalPolynomial() {
        return new MultivariateFunction() {
            public double value(double[] x) {
                return x[0] * x[0] * x[0] * x[0];
            }
        };
    }
}