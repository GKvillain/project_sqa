package org.apache.commons.math.optimization.univariate;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MaxIterationsExceededException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.exception.NotStrictlyPositiveException;
import org.apache.commons.math.optimization.GoalType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class BrentOptimizerTest {

    private BrentOptimizer optimizer;

    @Before
    public void setUp() {
        optimizer = new BrentOptimizer();
    }

    // Tests minimization of a standard quadratic function
    @Test
    public void testOptimize_quadraticMinimization_findsMinimum() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return (x - 2.0) * (x - 2.0) + 3.0;
            }
        };
        double result = optimizer.optimize(f, GoalType.MINIMIZE, 0.0, 5.0);
        Assert.assertEquals(2.0, result, 1e-6);
        Assert.assertEquals(3.0, optimizer.getFunctionValue(), 1e-6);
        Assert.assertEquals(2.0, optimizer.getResult(), 1e-6);
        Assert.assertTrue(optimizer.getIterationCount() > 0);
        Assert.assertTrue(optimizer.getEvaluations() > 0);
    }

    // Tests maximization of a standard quadratic function
    @Test
    public void testOptimize_quadraticMaximization_findsMaximum() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return -(x - 3.0) * (x - 3.0) + 5.0;
            }
        };
        double result = optimizer.optimize(f, GoalType.MAXIMIZE, 0.0, 6.0);
        Assert.assertEquals(3.0, result, 1e-6);
        Assert.assertEquals(5.0, optimizer.getFunctionValue(), 1e-6);
        Assert.assertEquals(3.0, optimizer.getResult(), 1e-6);
    }

    // Tests optimization with custom startValue
    @Test
    public void testOptimize_withStartValue_findsOptimum() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return Math.cos(x);
            }
        };
        double result = optimizer.optimize(f, GoalType.MINIMIZE, 0.0, 6.0, 3.0);
        Assert.assertEquals(Math.PI, result, 1e-6);
        Assert.assertEquals(-1.0, optimizer.getFunctionValue(), 1e-6);
    }

    // Tests optimization when lower bound is greater than upper bound
    @Test
    public void testOptimize_invertedInterval_findsOptimum() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return (x + 1.0) * (x + 1.0);
            }
        };
        double result = optimizer.optimize(f, GoalType.MINIMIZE, 2.0, -4.0);
        Assert.assertEquals(-1.0, result, 1e-6);
    }

    // Tests sine function minimization
    @Test
    public void testOptimize_sinFunctionMinimization_findsMinimum() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return Math.sin(x);
            }
        };
        double result = optimizer.optimize(f, GoalType.MINIMIZE, 3.0, 6.0);
        Assert.assertEquals(1.5 * Math.PI, result, 1e-6);
        Assert.assertEquals(-1.0, optimizer.getFunctionValue(), 1e-6);
    }

    // Tests sine function maximization
    @Test
    public void testOptimize_sinFunctionMaximization_findsMaximum() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return Math.sin(x);
            }
        };
        double result = optimizer.optimize(f, GoalType.MAXIMIZE, 0.0, 3.0);
        Assert.assertEquals(0.5 * Math.PI, result, 1e-6);
        Assert.assertEquals(1.0, optimizer.getFunctionValue(), 1e-6);
    }

    // Tests quintic polynomial with local minima to exercise parabolic and golden steps
    @Test
    public void testOptimize_quinticFunction_findsLocalMinimum() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return (x - 1) * (x - 0.5) * x * (x + 0.5) * (x + 1);
            }
        };
        double result = optimizer.optimize(f, GoalType.MINIMIZE, -0.3, 0.4);
        Assert.assertEquals(0.271956, result, 1e-4);
    }

    // Tests doOptimize directly throwing UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testDoOptimize_throwsUnsupportedOperationException() throws Exception {
        optimizer.doOptimize();
    }

    // Tests non-positive relative accuracy throwing NotStrictlyPositiveException
    @Test(expected = NotStrictlyPositiveException.class)
    public void testOptimize_nonPositiveRelativeAccuracy_throwsException() throws Exception {
        optimizer.setRelativeAccuracy(0.0);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x;
            }
        };
        optimizer.optimize(f, GoalType.MINIMIZE, -1.0, 1.0);
    }

    // Tests non-positive absolute accuracy throwing NotStrictlyPositiveException
    @Test(expected = NotStrictlyPositiveException.class)
    public void testOptimize_nonPositiveAbsoluteAccuracy_throwsException() throws Exception {
        optimizer.setAbsoluteAccuracy(-1e-10);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x;
            }
        };
        optimizer.optimize(f, GoalType.MINIMIZE, -1.0, 1.0);
    }

    // Tests exceeding max iterations throwing MaxIterationsExceededException
    @Test(expected = MaxIterationsExceededException.class)
    public void testOptimize_exceedsMaxIterations_throwsException() throws Exception {
        optimizer.setMaximalIterationCount(1);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return Math.sin(x);
            }
        };
        optimizer.optimize(f, GoalType.MINIMIZE, -100.0, 100.0);
    }

    // Tests function evaluation exception propagation
    @Test(expected = FunctionEvaluationException.class)
    public void testOptimize_functionThrowsException_propagatesException() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) throws FunctionEvaluationException {
                throw new FunctionEvaluationException(x);
            }
        };
        optimizer.optimize(f, GoalType.MINIMIZE, 0.0, 1.0);
    }

    // Tests default constructor parameter values
    @Test
    public void testConstructor_defaultSettings_areInitializedCorrectly() {
        BrentOptimizer opt = new BrentOptimizer();
        Assert.assertEquals(Integer.MAX_VALUE, opt.getMaxEvaluations());
        Assert.assertEquals(100, opt.getMaximalIterationCount());
        Assert.assertEquals(1E-10, opt.getAbsoluteAccuracy(), 1e-15);
        Assert.assertEquals(1.0e-14, opt.getRelativeAccuracy(), 1e-18);
    }
}