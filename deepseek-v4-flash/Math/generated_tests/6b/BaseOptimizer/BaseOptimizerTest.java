package org.apache.commons.math3.optim;

import static org.junit.Assert.*;

import org.junit.Test;

import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.exception.TooManyIterationsException;

/**
 * JUnit 4 test for BaseOptimizer.
 * Detects the Defects4J MATH-6b bug (default max counts cause immediate
 * TooManyEvaluationsException / TooManyIterationsException).
 */
public class BaseOptimizerTest {

    /**
     * Stub implementation of ConvergenceChecker.
     */
    private static class TestConvergenceChecker implements ConvergenceChecker<Object> {
        @Override
        public boolean converged(int iteration, Object previous, Object current) {
            return false;
        }
    }

    /**
     * Concrete stub that extends BaseOptimizer for testing purposes.
     */
    private static class TestOptimizer extends BaseOptimizer<Object> {
        private int evalInc;
        private int iterInc;
        private Object result;

        TestOptimizer(ConvergenceChecker<Object> checker) {
            super(checker);
            this.result = new Object();
        }

        public void setEvalIncrement(int evalInc) {
            this.evalInc = evalInc;
        }

        public void setIterIncrement(int iterInc) {
            this.iterInc = iterInc;
        }

        public void setResult(Object result) {
            this.result = result;
        }

        @Override
        protected Object doOptimize() {
            for (int i = 0; i < evalInc; i++) {
                incrementEvaluationCount();
            }
            for (int i = 0; i < iterInc; i++) {
                incrementIterationCount();
            }
            return result;
        }
    }

    // --- Constructor tests ---

    // Tests that a null checker is stored and returned.
    @Test
    public void testConstructor_nullChecker_checkerIsNull() {
        BaseOptimizer<Object> optimizer = new TestOptimizer(null);
        assertNull(optimizer.getConvergenceChecker());
    }

    // Tests that a valid checker is stored correctly.
    @Test
    public void testConstructor_validChecker_storesChecker() {
        ConvergenceChecker<Object> checker = new TestConvergenceChecker();
        BaseOptimizer<Object> optimizer = new TestOptimizer(checker);
        assertSame(checker, optimizer.getConvergenceChecker());
    }

    // --- Default getter tests ---

    // Tests that the evaluation counter starts at 0.
    @Test
    public void testGetEvaluations_initialCall_returnsZero() {
        BaseOptimizer<Object> optimizer = new TestOptimizer(null);
        assertEquals(0, optimizer.getEvaluations());
    }

    // Tests that the iteration counter starts at 0.
    @Test
    public void testGetIterations_initialCall_returnsZero() {
        BaseOptimizer<Object> optimizer = new TestOptimizer(null);
        assertEquals(0, optimizer.getIterations());
    }

    // Tests that the default max evaluations is 0.
    @Test
    public void testGetMaxEvaluations_default_returnsZero() {
        BaseOptimizer<Object> optimizer = new TestOptimizer(null);
        assertEquals(0, optimizer.getMaxEvaluations());
    }

    // Tests that the default max iterations is 0.
    @Test
    public void testGetMaxIterations_default_returnsZero() {
        BaseOptimizer<Object> optimizer = new TestOptimizer(null);
        assertEquals(0, optimizer.getMaxIterations());
    }

    // --- Defect Detection: Bug MATH-6b ---

    // Detects bug: optimize() without MaxEval throws TooManyEvaluationsException.
    @Test(expected = TooManyEvaluationsException.class)
    public void testOptimize_noMaxEval_throwsTooManyEvaluationsException() {
        TestOptimizer optimizer = new TestOptimizer(null);
        optimizer.setEvalIncrement(1);
        optimizer.optimize();
    }

    // Detects bug: optimize() without MaxIter throws TooManyIterationsException.
    @Test(expected = TooManyIterationsException.class)
    public void testOptimize_noMaxIter_throwsTooManyIterationsException() {
        TestOptimizer optimizer = new TestOptimizer(null);
        optimizer.setIterIncrement(1);
        optimizer.optimize();
    }

    // --- Normal success cases ---

    // Tests that within-limit evaluations increment the counter correctly.
    @Test
    public void testOptimize_withMaxEval_incrementsCount() {
        TestOptimizer optimizer = new TestOptimizer(null);
        optimizer.setEvalIncrement(5);
        optimizer.optimize(new MaxEval(10));
        assertEquals(5, optimizer.getEvaluations());
        assertEquals(0, optimizer.getIterations());
    }

    // Tests that within-limit iterations increment the counter correctly.
    @Test
    public void testOptimize_withMaxIter_incrementsCount() {
        TestOptimizer optimizer = new TestOptimizer(null);
        optimizer.setIterIncrement(5);
        optimizer.optimize(new MaxIter(10));
        assertEquals(0, optimizer.getEvaluations());
        assertEquals(5, optimizer.getIterations());
    }

    // Tests that both evaluations and iterations increment correctly.
    @Test
    public void testOptimize_withMaxEvalAndMaxIter_incrementsBoth() {
        TestOptimizer optimizer = new TestOptimizer(null);
        optimizer.setEvalIncrement(3);
        optimizer.setIterIncrement(3);
        optimizer.optimize(new MaxEval(10), new MaxIter(10));
        assertEquals(3, optimizer.getEvaluations());
        assertEquals(3, optimizer.getIterations());
    }

    // --- Exception paths ---

    // Tests that exceeding MaxEval throws the correct exception.
    @Test(expected = TooManyEvaluationsException.class)
    public void testOptimize_maxEvalExceeded_throwsTooManyEvaluationsException() {
        TestOptimizer optimizer = new TestOptimizer(null);
        optimizer.setEvalIncrement(11);
        optimizer.optimize(new MaxEval(10));
    }

    // Tests that exceeding MaxIter throws the correct exception.
    @Test(expected = TooManyIterationsException.class)
    public void testOptimize_maxIterExceeded_throwsTooManyIterationsException() {
        TestOptimizer optimizer = new TestOptimizer(null);
        optimizer.setIterIncrement(11);
        optimizer.optimize(new MaxIter(10));
    }

    // --- Regression / State ---

    // Tests that counters are reset on each call to optimize().
    @Test
    public void testOptimize_multipleCalls_resetsCounters() {
        TestOptimizer optimizer = new TestOptimizer(null);
        optimizer.setEvalIncrement(5);
        optimizer.setIterIncrement(5);
        optimizer.optimize(new MaxEval(10), new MaxIter(10));
        assertEquals(5, optimizer.getEvaluations());
        assertEquals(5, optimizer.getIterations());

        optimizer.setEvalIncrement(2);
        optimizer.setIterIncrement(2);
        optimizer.optimize();
        assertEquals(2, optimizer.getEvaluations());
        assertEquals(2, optimizer.getIterations());
    }

    // Tests that parseOptimizationData updates the maximal evaluation count.
    @Test
    public void testOptimize_updatesMaxEval_getMaxEvaluationsUpdated() {
        TestOptimizer optimizer = new TestOptimizer(null);
        optimizer.setEvalIncrement(1);
        optimizer.optimize(new MaxEval(50));
        assertEquals(50, optimizer.getMaxEvaluations());
    }

    // Tests that parseOptimizationData updates the maximal iteration count.
    @Test
    public void testOptimize_updatesMaxIter_getMaxIterationsUpdated() {
        TestOptimizer optimizer = new TestOptimizer(null);
        optimizer.setIterIncrement(1);
        optimizer.optimize(new MaxIter(100));
        assertEquals(100, optimizer.getMaxIterations());
    }

    // Tests that unknown OptimizationData is ignored gracefully.
    @Test
    public void testOptimize_unknownData_ignoresIt() {
        TestOptimizer optimizer = new TestOptimizer(null);
        OptimizationData unknownData = new OptimizationData() {};
        optimizer.setEvalIncrement(1);
        optimizer.optimize(new MaxEval(10), unknownData);
        assertEquals(1, optimizer.getEvaluations());
    }

    // Tests that null entry in OptimizationData varargs is ignored.
    @Test
    public void testOptimize_parseWithNullData_ignoresIt() {
        TestOptimizer optimizer = new TestOptimizer(null);
        optimizer.setEvalIncrement(0);
        optimizer.optimize((OptimizationData) null);
    }

    // Tests that the result of doOptimize is returned by optimize.
    @Test
    public void testOptimize_returnsResultFromDoOptimize() {
        TestOptimizer optimizer = new TestOptimizer(null);
        Object expected = new Object();
        optimizer.setResult(expected);
        Object actual = optimizer.optimize(new MaxEval(10));
        assertSame(expected, actual);
    }

    // Tests parseOptimizationData when no data is provided (loop runs 0 times).
    @Test
    public void testParseOptimizationData_noData_noEffect() {
        TestOptimizer optimizer = new TestOptimizer(null);
        optimizer.setEvalIncrement(0);
        optimizer.setIterIncrement(0);
        optimizer.optimize();
    }
}