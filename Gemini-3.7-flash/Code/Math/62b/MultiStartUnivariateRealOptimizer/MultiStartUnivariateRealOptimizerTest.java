package org.apache.commons.math.optimization.univariate;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.exception.ConvergenceException;
import org.apache.commons.math.exception.MathIllegalStateException;
import org.apache.commons.math.optimization.ConvergenceChecker;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.random.JDKRandomGenerator;
import org.apache.commons.math.random.RandomGenerator;
import org.apache.commons.math.util.FastMath;
import org.junit.Assert;
import org.junit.Test;

public class MultiStartUnivariateRealOptimizerTest {

    // Tests getter and setter for max evaluations
    @Test
    public void testSetGetMaxEvaluations_validValue_returnsSetValue() {
        UnivariateRealOptimizer underlying = new BrentOptimizer(1e-10, 1e-14);
        JDKRandomGenerator g = new JDKRandomGenerator();
        MultiStartUnivariateRealOptimizer<UnivariateRealFunction> optimizer =
            new MultiStartUnivariateRealOptimizer<UnivariateRealFunction>(underlying, 5, g);

        optimizer.setMaxEvaluations(100);
        Assert.assertEquals(100, optimizer.getMaxEvaluations());
        Assert.assertEquals(100, underlying.getMaxEvaluations());
    }

    // Tests getter and setter for convergence checker delegation
    @Test
    public void testSetGetConvergenceChecker_customChecker_delegatesToUnderlying() {
        UnivariateRealOptimizer underlying = new BrentOptimizer(1e-10, 1e-14);
        JDKRandomGenerator g = new JDKRandomGenerator();
        MultiStartUnivariateRealOptimizer<UnivariateRealFunction> optimizer =
            new MultiStartUnivariateRealOptimizer<UnivariateRealFunction>(underlying, 5, g);

        ConvergenceChecker<UnivariateRealPointValuePair> checker =
            new ConvergenceChecker<UnivariateRealPointValuePair>() {
                public boolean converged(int iteration, UnivariateRealPointValuePair previous, UnivariateRealPointValuePair current) {
                    return true;
                }
            };

        optimizer.setConvergenceChecker(checker);
        Assert.assertSame(checker, optimizer.getConvergenceChecker());
    }

    // Tests getOptima before optimization throws MathIllegalStateException
    @Test(expected = MathIllegalStateException.class)
    public void testGetOptima_beforeOptimize_throwsMathIllegalStateException() {
        UnivariateRealOptimizer underlying = new BrentOptimizer(1e-10, 1e-14);
        JDKRandomGenerator g = new JDKRandomGenerator();
        MultiStartUnivariateRealOptimizer<UnivariateRealFunction> optimizer =
            new MultiStartUnivariateRealOptimizer<UnivariateRealFunction>(underlying, 5, g);

        optimizer.getOptima();
    }

    // Tests minimization of a multi-modal function
    @Test
    public void testOptimize_minimizeSinFunction_findsGlobalMinimum() throws FunctionEvaluationException {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return FastMath.sin(x);
            }
        };

        UnivariateRealOptimizer underlying = new BrentOptimizer(1e-10, 1e-14);
        JDKRandomGenerator g = new JDKRandomGenerator();
        g.setSeed(439274028L);
        MultiStartUnivariateRealOptimizer<UnivariateRealFunction> optimizer =
            new MultiStartUnivariateRealOptimizer<UnivariateRealFunction>(underlying, 10, g);
        optimizer.setMaxEvaluations(300);

        UnivariateRealPointValuePair optimum =
            optimizer.optimize(f, GoalType.MINIMIZE, -100.0, 100.0);

        Assert.assertEquals(-1.0, optimum.getValue(), 1e-6);
        Assert.assertEquals(-1.0, FastMath.sin(optimum.getPoint()), 1e-6);
        Assert.assertTrue(optimizer.getEvaluations() > 0);

        UnivariateRealPointValuePair[] optima = optimizer.getOptima();
        Assert.assertEquals(10, optima.length);
        for (int i = 1; i < optima.length; ++i) {
            if (optima[i] != null) {
                Assert.assertTrue(optima[i - 1].getValue() <= optima[i].getValue());
            }
        }
    }

    // Tests maximization of a multi-modal function
    @Test
    public void testOptimize_maximizeSinFunction_findsGlobalMaximum() throws FunctionEvaluationException {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return FastMath.sin(x);
            }
        };

        UnivariateRealOptimizer underlying = new BrentOptimizer(1e-10, 1e-14);
        JDKRandomGenerator g = new JDKRandomGenerator();
        g.setSeed(439274028L);
        MultiStartUnivariateRealOptimizer<UnivariateRealFunction> optimizer =
            new MultiStartUnivariateRealOptimizer<UnivariateRealFunction>(underlying, 10, g);
        optimizer.setMaxEvaluations(300);

        UnivariateRealPointValuePair optimum =
            optimizer.optimize(f, GoalType.MAXIMIZE, -100.0, 100.0);

        Assert.assertEquals(1.0, optimum.getValue(), 1e-6);
        Assert.assertEquals(1.0, FastMath.sin(optimum.getPoint()), 1e-6);

        UnivariateRealPointValuePair[] optima = optimizer.getOptima();
        Assert.assertEquals(10, optima.length);
        for (int i = 1; i < optima.length; ++i) {
            if (optima[i] != null) {
                Assert.assertTrue(optima[i - 1].getValue() >= optima[i].getValue());
            }
        }
    }

    // Tests optimize with 5-parameter signature including startValue
    @Test
    public void testOptimize_withStartValue_returnsOptimum() throws FunctionEvaluationException {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return (x - 2.5) * (x - 2.5) + 3.0;
            }
        };

        UnivariateRealOptimizer underlying = new BrentOptimizer(1e-10, 1e-14);
        JDKRandomGenerator g = new JDKRandomGenerator();
        g.setSeed(123456L);
        MultiStartUnivariateRealOptimizer<UnivariateRealFunction> optimizer =
            new MultiStartUnivariateRealOptimizer<UnivariateRealFunction>(underlying, 5, g);
        optimizer.setMaxEvaluations(200);

        UnivariateRealPointValuePair optimum =
            optimizer.optimize(f, GoalType.MINIMIZE, 0.0, 5.0, 2.0);

        Assert.assertEquals(2.5, optimum.getPoint(), 1e-5);
        Assert.assertEquals(3.0, optimum.getValue(), 1e-5);
    }

    // Tests single start configuration (starts = 1)
    @Test
    public void testOptimize_singleStart_returnsOptimum() throws FunctionEvaluationException {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return (x - 1.0) * (x - 1.0);
            }
        };

        UnivariateRealOptimizer underlying = new BrentOptimizer(1e-10, 1e-14);
        JDKRandomGenerator g = new JDKRandomGenerator();
        MultiStartUnivariateRealOptimizer<UnivariateRealFunction> optimizer =
            new MultiStartUnivariateRealOptimizer<UnivariateRealFunction>(underlying, 1, g);
        optimizer.setMaxEvaluations(100);

        UnivariateRealPointValuePair optimum =
            optimizer.optimize(f, GoalType.MINIMIZE, -2.0, 4.0);

        Assert.assertEquals(1.0, optimum.getPoint(), 1e-5);
        Assert.assertEquals(0.0, optimum.getValue(), 1e-5);
        Assert.assertEquals(1, optimizer.getOptima().length);
    }

    // Tests exception path when no convergence can be achieved across any start point
    @Test(expected = ConvergenceException.class)
    public void testOptimize_allStartsFail_throwsConvergenceException() throws FunctionEvaluationException {
        UnivariateRealOptimizer failingOptimizer = new BaseUnivariateRealOptimizer<UnivariateRealFunction>() {
            private int maxEval;
            private int eval;
            public void setMaxEvaluations(int maxEvaluations) { this.maxEval = maxEvaluations; }
            public int getMaxEvaluations() { return maxEval; }
            public int getEvaluations() { return eval; }
            public void setConvergenceChecker(ConvergenceChecker<UnivariateRealPointValuePair> checker) {}
            public ConvergenceChecker<UnivariateRealPointValuePair> getConvergenceChecker() { return null; }
            public UnivariateRealPointValuePair optimize(UnivariateRealFunction f, GoalType goalType, double min, double max) {
                eval = 1;
                throw new ConvergenceException(org.apache.commons.math.exception.util.LocalizedFormats.FAILED_FRACTION_CONVERSION, 0, 0);
            }
            public UnivariateRealPointValuePair optimize(UnivariateRealFunction f, GoalType goalType, double min, double max, double startValue) {
                eval = 1;
                throw new ConvergenceException(org.apache.commons.math.exception.util.LocalizedFormats.FAILED_FRACTION_CONVERSION, 0, 0);
            }
        };

        JDKRandomGenerator g = new JDKRandomGenerator();
        MultiStartUnivariateRealOptimizer<UnivariateRealFunction> optimizer =
            new MultiStartUnivariateRealOptimizer<UnivariateRealFunction>(failingOptimizer, 3, g);
        optimizer.setMaxEvaluations(100);

        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x;
            }
        };

        optimizer.optimize(f, GoalType.MINIMIZE, 0.0, 1.0);
    }

    // Tests handling of FunctionEvaluationException during individual start
    @Test
    public void testOptimize_partialFunctionEvaluationException_recoversAndSortsOptima() throws FunctionEvaluationException {
        UnivariateRealOptimizer partiallyFailingOptimizer = new BaseUnivariateRealOptimizer<UnivariateRealFunction>() {
            private int maxEval = 100;
            private int eval = 0;
            private int callCount = 0;
            public void setMaxEvaluations(int maxEvaluations) { this.maxEval = maxEvaluations; }
            public int getMaxEvaluations() { return maxEval; }
            public int getEvaluations() { return eval; }
            public void setConvergenceChecker(ConvergenceChecker<UnivariateRealPointValuePair> checker) {}
            public ConvergenceChecker<UnivariateRealPointValuePair> getConvergenceChecker() { return null; }
            public UnivariateRealPointValuePair optimize(UnivariateRealFunction f, GoalType goalType, double min, double max) throws FunctionEvaluationException {
                return optimize(f, goalType, min, max, 0);
            }
            public UnivariateRealPointValuePair optimize(UnivariateRealFunction f, GoalType goalType, double min, double max, double startValue) throws FunctionEvaluationException {
                callCount++;
                eval = 5;
                if (callCount == 1) {
                    throw new FunctionEvaluationException(min);
                }
                return new UnivariateRealPointValuePair(callCount, (double) callCount);
            }
        };

        JDKRandomGenerator g = new JDKRandomGenerator();
        MultiStartUnivariateRealOptimizer<UnivariateRealFunction> optimizer =
            new MultiStartUnivariateRealOptimizer<UnivariateRealFunction>(partiallyFailingOptimizer, 2, g);
        optimizer.setMaxEvaluations(100);

        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x;
            }
        };

        UnivariateRealPointValuePair result = optimizer.optimize(f, GoalType.MINIMIZE, 0.0, 10.0);
        Assert.assertNotNull(result);
        Assert.assertEquals(2.0, result.getValue(), 1e-9);

        UnivariateRealPointValuePair[] optima = optimizer.getOptima();
        Assert.assertNotNull(optima[0]);
        Assert.assertNull(optima[1]);
    }

    // Tests cloning behavior of getOptima
    @Test
    public void testGetOptima_cloning_modifyingReturnedArrayDoesNotAffectState() throws FunctionEvaluationException {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x;
            }
        };

        UnivariateRealOptimizer underlying = new BrentOptimizer(1e-10, 1e-14);
        JDKRandomGenerator g = new JDKRandomGenerator();
        MultiStartUnivariateRealOptimizer<UnivariateRealFunction> optimizer =
            new MultiStartUnivariateRealOptimizer<UnivariateRealFunction>(underlying, 3, g);
        optimizer.setMaxEvaluations(100);

        optimizer.optimize(f, GoalType.MINIMIZE, -1.0, 1.0);

        UnivariateRealPointValuePair[] firstOptima = optimizer.getOptima();
        UnivariateRealPointValuePair originalFirst = firstOptima[0];
        firstOptima[0] = null;

        UnivariateRealPointValuePair[] secondOptima = optimizer.getOptima();
        Assert.assertNotNull(secondOptima[0]);
        Assert.assertEquals(originalFirst.getValue(), secondOptima[0].getValue(), 1e-10);
    }
}