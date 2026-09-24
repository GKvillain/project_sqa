package org.apache.commons.math.optimization;

import org.apache.commons.math.ConvergenceException;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.random.JDKRandomGenerator;
import org.apache.commons.math.random.RandomGenerator;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class MultiStartUnivariateRealOptimizerTest {

    private UnivariateRealFunction quinticFunction;

    @Before
    public void setUp() {
        // A function with multiple local extrema: (x - 1)(x - 0.5)(x)(x + 0.5)(x + 1)
        quinticFunction = new UnivariateRealFunction() {
            public double value(double x) {
                return (x - 1.0) * (x - 0.5) * x * (x + 0.5) * (x + 1.0);
            }
        };
    }

    // Tests getOptima before calling optimize throws IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testGetOptima_beforeOptimize_throwsIllegalStateException() {
        UnivariateRealOptimizer underlying = new DummyOptimizer(0.0, 0.0, 1, 1);
        MultiStartUnivariateRealOptimizer optimizer =
                new MultiStartUnivariateRealOptimizer(underlying, 5, new JDKRandomGenerator());
        optimizer.getOptima();
    }

    // Tests getOptimaValues before calling optimize throws IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testGetOptimaValues_beforeOptimize_throwsIllegalStateException() {
        UnivariateRealOptimizer underlying = new DummyOptimizer(0.0, 0.0, 1, 1);
        MultiStartUnivariateRealOptimizer optimizer =
                new MultiStartUnivariateRealOptimizer(underlying, 5, new JDKRandomGenerator());
        optimizer.getOptimaValues();
    }

    // Tests getResult and getFunctionValue return the best result after minimization (Defects4J Math-67)
    @Test
    public void testOptimize_minimization_returnsBestOptimumAndMatchesGetters() throws Exception {
        // Creates an optimizer returning varying results across starts
        SequenceOptimizer underlying = new SequenceOptimizer(
                new double[] { 2.0, 1.0, 3.0 },
                new double[] { 10.0, 2.0, 15.0 }
        );
        RandomGenerator generator = new JDKRandomGenerator();
        generator.setSeed(42);

        MultiStartUnivariateRealOptimizer optimizer =
                new MultiStartUnivariateRealOptimizer(underlying, 3, generator);

        double result = optimizer.optimize(quinticFunction, GoalType.MINIMIZE, -2.0, 2.0);

        assertEquals(1.0, result, 1e-10);
        assertEquals(1.0, optimizer.getResult(), 1e-10);
        assertEquals(2.0, optimizer.getFunctionValue(), 1e-10);

        double[] optima = optimizer.getOptima();
        double[] optimaValues = optimizer.getOptimaValues();

        assertEquals(3, optima.length);
        assertEquals(1.0, optima[0], 1e-10);
        assertEquals(2.0, optimaValues[0], 1e-10);
        assertEquals(2.0, optima[1], 1e-10);
        assertEquals(10.0, optimaValues[1], 1e-10);
        assertEquals(3.0, optima[2], 1e-10);
        assertEquals(15.0, optimaValues[2], 1e-10);
    }

    // Tests getResult and getFunctionValue return the best result after maximization
    @Test
    public void testOptimize_maximization_returnsBestOptimumAndMatchesGetters() throws Exception {
        SequenceOptimizer underlying = new SequenceOptimizer(
                new double[] { 2.0, 1.0, 3.0 },
                new double[] { 10.0, 25.0, 5.0 }
        );
        RandomGenerator generator = new JDKRandomGenerator();
        generator.setSeed(42);

        MultiStartUnivariateRealOptimizer optimizer =
                new MultiStartUnivariateRealOptimizer(underlying, 3, generator);

        double result = optimizer.optimize(quinticFunction, GoalType.MAXIMIZE, -2.0, 2.0);

        assertEquals(1.0, result, 1e-10);
        assertEquals(1.0, optimizer.getResult(), 1e-10);
        assertEquals(25.0, optimizer.getFunctionValue(), 1e-10);

        double[] optima = optimizer.getOptima();
        double[] optimaValues = optimizer.getOptimaValues();

        assertEquals(3, optima.length);
        assertEquals(1.0, optima[0], 1e-10);
        assertEquals(25.0, optimaValues[0], 1e-10);
        assertEquals(2.0, optima[1], 1e-10);
        assertEquals(10.0, optimaValues[1], 1e-10);
        assertEquals(3.0, optima[2], 1e-10);
        assertEquals(5.0, optimaValues[2], 1e-10);
    }

    // Tests optimize method overload with startValue parameter
    @Test
    public void testOptimize_withStartValue_delegatesSuccessfully() throws Exception {
        SequenceOptimizer underlying = new SequenceOptimizer(
                new double[] { 0.5 },
                new double[] { 1.25 }
        );
        MultiStartUnivariateRealOptimizer optimizer =
                new MultiStartUnivariateRealOptimizer(underlying, 1, new JDKRandomGenerator());

        double result = optimizer.optimize(quinticFunction, GoalType.MINIMIZE, -1.0, 1.0, 0.0);
        assertEquals(0.5, result, 1e-10);
        assertEquals(0.5, optimizer.getResult(), 1e-10);
        assertEquals(1.25, optimizer.getFunctionValue(), 1e-10);
    }

    // Tests that total iterations and evaluations are accumulated across starts
    @Test
    public void testOptimize_accumulatesIterationsAndEvaluations() throws Exception {
        DummyOptimizer underlying = new DummyOptimizer(0.0, 0.0, 5, 10);
        MultiStartUnivariateRealOptimizer optimizer =
                new MultiStartUnivariateRealOptimizer(underlying, 4, new JDKRandomGenerator());

        optimizer.optimize(quinticFunction, GoalType.MINIMIZE, -1.0, 1.0);

        assertEquals(20, optimizer.getIterationCount());
        assertEquals(40, optimizer.getEvaluations());
    }

    // Tests throwing OptimizationException when all starts fail to converge
    @Test(expected = OptimizationException.class)
    public void testOptimize_allStartsThrowConvergenceException_throwsOptimizationException() throws Exception {
        UnivariateRealOptimizer underlying = new FailingOptimizer(true, false);
        MultiStartUnivariateRealOptimizer optimizer =
                new MultiStartUnivariateRealOptimizer(underlying, 3, new JDKRandomGenerator());

        optimizer.optimize(quinticFunction, GoalType.MINIMIZE, -1.0, 1.0);
    }

    // Tests handling when all starts throw FunctionEvaluationException
    @Test(expected = OptimizationException.class)
    public void testOptimize_allStartsThrowEvaluationException_throwsOptimizationException() throws Exception {
        UnivariateRealOptimizer underlying = new FailingOptimizer(false, true);
        MultiStartUnivariateRealOptimizer optimizer =
                new MultiStartUnivariateRealOptimizer(underlying, 3, new JDKRandomGenerator());

        optimizer.optimize(quinticFunction, GoalType.MINIMIZE, -1.0, 1.0);
    }

    // Tests delegation and handling of accuracy and limits getters and setters
    @Test
    public void testAccuracyAndLimits_delegationAndState() {
        DummyOptimizer underlying = new DummyOptimizer(0.0, 0.0, 0, 0);
        MultiStartUnivariateRealOptimizer optimizer =
                new MultiStartUnivariateRealOptimizer(underlying, 2, new JDKRandomGenerator());

        optimizer.setAbsoluteAccuracy(1e-5);
        assertEquals(1e-5, optimizer.getAbsoluteAccuracy(), 1e-12);
        optimizer.resetAbsoluteAccuracy();
        assertEquals(1e-8, optimizer.getAbsoluteAccuracy(), 1e-12);

        optimizer.setRelativeAccuracy(1e-6);
        assertEquals(1e-6, optimizer.getRelativeAccuracy(), 1e-12);
        optimizer.resetRelativeAccuracy();
        assertEquals(1e-8, optimizer.getRelativeAccuracy(), 1e-12);

        optimizer.setMaximalIterationCount(500);
        assertEquals(500, optimizer.getMaximalIterationCount());
        optimizer.resetMaximalIterationCount();
        assertEquals(500, optimizer.getMaximalIterationCount());

        optimizer.setMaxEvaluations(1000);
        assertEquals(1000, optimizer.getMaxEvaluations());
    }

    // Tests that getOptima and getOptimaValues return independent clones
    @Test
    public void testGetOptima_returnsClonedArray() throws Exception {
        SequenceOptimizer underlying = new SequenceOptimizer(
                new double[] { 1.0 },
                new double[] { 2.0 }
        );
        MultiStartUnivariateRealOptimizer optimizer =
                new MultiStartUnivariateRealOptimizer(underlying, 1, new JDKRandomGenerator());

        optimizer.optimize(quinticFunction, GoalType.MINIMIZE, 0.0, 2.0);

        double[] optima1 = optimizer.getOptima();
        optima1[0] = 999.0;
        double[] optima2 = optimizer.getOptima();
        assertEquals(1.0, optima2[0], 1e-10);

        double[] values1 = optimizer.getOptimaValues();
        values1[0] = 888.0;
        double[] values2 = optimizer.getOptimaValues();
        assertEquals(2.0, values2[0], 1e-10);
    }

    // Helper optimizer returning predetermined sequences of results
    private static class SequenceOptimizer implements UnivariateRealOptimizer {
        private final double[] results;
        private final double[] values;
        private int callIndex = 0;
        private int maxIter;
        private int maxEval;
        private double absAcc = 1e-8;
        private double relAcc = 1e-8;

        public SequenceOptimizer(double[] results, double[] values) {
            this.results = results;
            this.values = values;
        }

        public double optimize(UnivariateRealFunction f, GoalType goalType, double min, double max) {
            int index = callIndex % results.length;
            callIndex++;
            return results[index];
        }

        public double optimize(UnivariateRealFunction f, GoalType goalType, double min, double max, double startValue) {
            return optimize(f, goalType, min, max);
        }

        public double getResult() {
            int index = (callIndex - 1) % results.length;
            return results[index];
        }

        public double getFunctionValue() {
            int index = (callIndex - 1) % values.length;
            return values[index];
        }

        public int getIterationCount() { return 1; }
        public int getMaximalIterationCount() { return maxIter; }
        public void setMaximalIterationCount(int count) { this.maxIter = count; }
        public void resetMaximalIterationCount() {}
        public int getEvaluations() { return 1; }
        public int getMaxEvaluations() { return maxEval; }
        public void setMaxEvaluations(int maxEvaluations) { this.maxEval = maxEvaluations; }
        public double getAbsoluteAccuracy() { return absAcc; }
        public void setAbsoluteAccuracy(double accuracy) { this.absAcc = accuracy; }
        public void resetAbsoluteAccuracy() { this.absAcc = 1e-8; }
        public double getRelativeAccuracy() { return relAcc; }
        public void setRelativeAccuracy(double accuracy) { this.relAcc = accuracy; }
        public void resetRelativeAccuracy() { this.relAcc = 1e-8; }
    }

    // Helper dummy optimizer
    private static class DummyOptimizer implements UnivariateRealOptimizer {
        private final double result;
        private final double functionValue;
        private final int iterationCount;
        private final int evaluations;
        private int maxIterations;
        private int maxEvaluations;
        private double absAccuracy = 1e-8;
        private double relAccuracy = 1e-8;

        public DummyOptimizer(double result, double functionValue, int iterationCount, int evaluations) {
            this.result = result;
            this.functionValue = functionValue;
            this.iterationCount = iterationCount;
            this.evaluations = evaluations;
        }

        public double optimize(UnivariateRealFunction f, GoalType goalType, double min, double max) {
            return result;
        }

        public double optimize(UnivariateRealFunction f, GoalType goalType, double min, double max, double startValue) {
            return result;
        }

        public double getResult() { return result; }
        public double getFunctionValue() { return functionValue; }
        public int getIterationCount() { return iterationCount; }
        public int getMaximalIterationCount() { return maxIterations; }
        public void setMaximalIterationCount(int count) { this.maxIterations = count; }
        public void resetMaximalIterationCount() {}
        public int getEvaluations() { return evaluations; }
        public int getMaxEvaluations() { return maxEvaluations; }
        public void setMaxEvaluations(int maxEvaluations) { this.maxEvaluations = maxEvaluations; }
        public double getAbsoluteAccuracy() { return absAccuracy; }
        public void setAbsoluteAccuracy(double accuracy) { this.absAccuracy = accuracy; }
        public void resetAbsoluteAccuracy() { this.absAccuracy = 1e-8; }
        public double getRelativeAccuracy() { return relAccuracy; }
        public void setRelativeAccuracy(double accuracy) { this.relAccuracy = accuracy; }
        public void resetRelativeAccuracy() { this.relAccuracy = 1e-8; }
    }

    // Helper optimizer that simulates convergence or function evaluation failures
    private static class FailingOptimizer implements UnivariateRealOptimizer {
        private final boolean failConvergence;
        private final boolean failEvaluation;

        public FailingOptimizer(boolean failConvergence, boolean failEvaluation) {
            this.failConvergence = failConvergence;
            this.failEvaluation = failEvaluation;
        }

        public double optimize(UnivariateRealFunction f, GoalType goalType, double min, double max)
                throws ConvergenceException, FunctionEvaluationException {
            if (failConvergence) {
                throw new ConvergenceException();
            }
            if (failEvaluation) {
                throw new FunctionEvaluationException(min);
            }
            return 0.0;
        }

        public double optimize(UnivariateRealFunction f, GoalType goalType, double min, double max, double startValue)
                throws ConvergenceException, FunctionEvaluationException {
            return optimize(f, goalType, min, max);
        }

        public double getResult() { return 0.0; }
        public double getFunctionValue() { return 0.0; }
        public int getIterationCount() { return 0; }
        public int getMaximalIterationCount() { return 0; }
        public void setMaximalIterationCount(int count) {}
        public void resetMaximalIterationCount() {}
        public int getEvaluations() { return 0; }
        public int getMaxEvaluations() { return 0; }
        public void setMaxEvaluations(int maxEvaluations) {}
        public double getAbsoluteAccuracy() { return 0.0; }
        public void setAbsoluteAccuracy(double accuracy) {}
        public void resetAbsoluteAccuracy() {}
        public double getRelativeAccuracy() { return 0.0; }
        public void setRelativeAccuracy(double accuracy) {}
        public void resetRelativeAccuracy() {}
    }
}