package org.apache.commons.math3.optim;

import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.exception.TooManyIterationsException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class BaseOptimizerTest {

    private static class DummyOptimizer extends BaseOptimizer<String> {
        private int evalToPerform = 0;
        private int iterToPerform = 0;
        private String returnResult = "success";

        public DummyOptimizer(ConvergenceChecker<String> checker) {
            super(checker);
        }

        public DummyOptimizer(ConvergenceChecker<String> checker, int maxEval, int maxIter) {
            super(checker, maxEval, maxIter);
        }

        public void setSteps(int evalToPerform, int iterToPerform) {
            this.evalToPerform = evalToPerform;
            this.iterToPerform = iterToPerform;
        }

        public void setReturnResult(String returnResult) {
            this.returnResult = returnResult;
        }

        @Override
        protected String doOptimize() {
            for (int i = 0; i < evalToPerform; i++) {
                incrementEvaluationCount();
            }
            for (int i = 0; i < iterToPerform; i++) {
                incrementIterationCount();
            }
            return returnResult;
        }
    }

    private static class DummyChecker implements ConvergenceChecker<String> {
        public boolean converged(int iteration, String previous, String current) {
            return true;
        }
    }

    private static class OtherOptimizationData implements OptimizationData {}

    // Tests constructor initialization and default counter values
    @Test
    public void testConstructor_withChecker_initializesCorrectly() {
        ConvergenceChecker<String> checker = new DummyChecker();
        DummyOptimizer optimizer = new DummyOptimizer(checker);

        assertSame(checker, optimizer.getConvergenceChecker());
        assertEquals(0, optimizer.getEvaluations());
        assertEquals(0, optimizer.getIterations());
        assertEquals(0, optimizer.getMaxEvaluations());
        assertEquals(0, optimizer.getMaxIterations());
    }

    // Tests constructor with null checker
    @Test
    public void testConstructor_nullChecker_returnsNullChecker() {
        DummyOptimizer optimizer = new DummyOptimizer(null);

        assertNull(optimizer.getConvergenceChecker());
    }

    // Tests constructor with checker, maxEval, and maxIter
    @Test
    public void testConstructor_withCheckerAndMaxLimits_initializesCorrectly() {
        ConvergenceChecker<String> checker = new DummyChecker();
        DummyOptimizer optimizer = new DummyOptimizer(checker, 100, 50);

        assertSame(checker, optimizer.getConvergenceChecker());
        assertEquals(0, optimizer.getEvaluations());
        assertEquals(0, optimizer.getIterations());
        assertEquals(100, optimizer.getMaxEvaluations());
        assertEquals(50, optimizer.getMaxIterations());
    }

    // Tests optimize execution with limits initialized from 3-arg constructor
    @Test
    public void testOptimize_withInitialMaxLimits_usesConstructorLimits() {
        DummyOptimizer optimizer = new DummyOptimizer(null, 20, 10);
        optimizer.setSteps(5, 3);
        optimizer.setReturnResult("optimal");

        String result = optimizer.optimize();

        assertEquals("optimal", result);
        assertEquals(20, optimizer.getMaxEvaluations());
        assertEquals(10, optimizer.getMaxIterations());
        assertEquals(5, optimizer.getEvaluations());
        assertEquals(3, optimizer.getIterations());
    }

    // Tests optimize execution with MaxEval and MaxIter options
    @Test
    public void testOptimize_validData_returnsExpectedResult() {
        DummyOptimizer optimizer = new DummyOptimizer(null);
        optimizer.setSteps(3, 2);
        optimizer.setReturnResult("optimal");

        String result = optimizer.optimize(new MaxEval(10), new MaxIter(5));

        assertEquals("optimal", result);
        assertEquals(10, optimizer.getMaxEvaluations());
        assertEquals(5, optimizer.getMaxIterations());
        assertEquals(3, optimizer.getEvaluations());
        assertEquals(2, optimizer.getIterations());
    }

    // Tests evaluations exceeding maximum limit throws TooManyEvaluationsException
    @Test(expected = TooManyEvaluationsException.class)
    public void testIncrementEvaluationCount_exceedsMaxEvaluations_throwsException() {
        DummyOptimizer optimizer = new DummyOptimizer(null);
        optimizer.setSteps(6, 1);

        optimizer.optimize(new MaxEval(5), new MaxIter(10));
    }

    // Tests iterations exceeding maximum limit throws TooManyIterationsException
    @Test(expected = TooManyIterationsException.class)
    public void testIncrementIterationCount_exceedsMaxIterations_throwsException() {
        DummyOptimizer optimizer = new DummyOptimizer(null);
        optimizer.setSteps(1, 4);

        optimizer.optimize(new MaxEval(10), new MaxIter(3));
    }

    // Tests boundary condition where evaluation count equals maximum allowed
    @Test
    public void testOptimize_evaluationsEqualToMax_doesNotThrowException() {
        DummyOptimizer optimizer = new DummyOptimizer(null);
        optimizer.setSteps(5, 0);

        optimizer.optimize(new MaxEval(5), new MaxIter(10));

        assertEquals(5, optimizer.getEvaluations());
        assertEquals(5, optimizer.getMaxEvaluations());
    }

    // Tests boundary condition where iteration count equals maximum allowed
    @Test
    public void testOptimize_iterationsEqualToMax_doesNotThrowException() {
        DummyOptimizer optimizer = new DummyOptimizer(null);
        optimizer.setSteps(0, 5);

        optimizer.optimize(new MaxEval(10), new MaxIter(5));

        assertEquals(5, optimizer.getIterations());
        assertEquals(5, optimizer.getMaxIterations());
    }

    // Tests resetting of evaluation and iteration counts upon multiple optimize calls
    @Test
    public void testOptimize_multipleCalls_resetsCountsEachTime() {
        DummyOptimizer optimizer = new DummyOptimizer(null);
        optimizer.setSteps(4, 3);
        optimizer.optimize(new MaxEval(10), new MaxIter(10));

        assertEquals(4, optimizer.getEvaluations());
        assertEquals(3, optimizer.getIterations());

        optimizer.setSteps(2, 1);
        optimizer.optimize();

        assertEquals(2, optimizer.getEvaluations());
        assertEquals(1, optimizer.getIterations());
    }

    // Tests retention of previous optimization data when not passed in subsequent call
    @Test
    public void testOptimize_retainsPreviousSettings_whenOmittedInSubsequentCall() {
        DummyOptimizer optimizer = new DummyOptimizer(null);
        optimizer.optimize(new MaxEval(20), new MaxIter(15));

        assertEquals(20, optimizer.getMaxEvaluations());
        assertEquals(15, optimizer.getMaxIterations());

        optimizer.optimize();

        assertEquals(20, optimizer.getMaxEvaluations());
        assertEquals(15, optimizer.getMaxIterations());
    }

    // Tests updating only one optimization data parameter
    @Test
    public void testOptimize_updateSingleOption_overwritesOnlySpecifiedData() {
        DummyOptimizer optimizer = new DummyOptimizer(null);
        optimizer.optimize(new MaxEval(20), new MaxIter(15));

        optimizer.optimize(new MaxEval(30));

        assertEquals(30, optimizer.getMaxEvaluations());
        assertEquals(15, optimizer.getMaxIterations());
    }

    // Tests parseOptimizationData ignores unrecognized optimization data types
    @Test
    public void testParseOptimizationData_unrecognizedData_ignoredSuccessfully() {
        DummyOptimizer optimizer = new DummyOptimizer(null);
        optimizer.optimize(new OtherOptimizationData(), new MaxEval(50));

        assertEquals(50, optimizer.getMaxEvaluations());
        assertEquals(0, optimizer.getMaxIterations());
    }
}