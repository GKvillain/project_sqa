package org.apache.commons.math3.optim.nonlinear.scalar.gradient;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import org.junit.Test;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.solvers.BrentSolver;
import org.apache.commons.math3.analysis.solvers.UnivariateSolver;
import org.apache.commons.math3.exception.MathInternalError;
import org.apache.commons.math3.exception.MathIllegalStateException;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.exception.MathUnsupportedOperationException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.optim.OptimizationData;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.ConvergenceChecker;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.GradientMultivariateOptimizer;
import org.apache.commons.math3.util.FastMath;

/**
 * JUnit 4 test class for NonLinearConjugateGradientOptimizer.
 * The tests aim to cover the main functionality, branch coverage, and
 * the defect fixed in Defects4J Math-6b.
 */
public class NonLinearConjugateGradientOptimizerTest {

    // ------------------------------------------------------------------
    // Helper classes
    // ------------------------------------------------------------------

    private static class NeverConverge implements ConvergenceChecker<PointValuePair> {
        @Override
        public boolean converged(int iteration, PointValuePair previous, PointValuePair current) {
            return false;
        }
    }

    private static class ConvergeAfterTwo implements ConvergenceChecker<PointValuePair> {
        private int count = 0;
        @Override
        public boolean converged(int iteration, PointValuePair previous, PointValuePair current) {
            return ++count >= 2;
        }
    }

    /**
     * A dummy solver that uses exactly one evaluation per call and returns the
     * midpoint of the interval. It never throws TooManyEvaluationsException.
     */
    private static class CustomSolver extends BrentSolver {
        private int evals;

        @Override
        public double solve(int maxEval, UnivariateFunction f, double a, double b, double y) {
            evals = 1;
            return (a + b) / 2;
        }

        @Override
        public int getEvaluations() {
            return evals;
        }
    }

    /**
     * A testable subclass that allows us to inject configuration without
     * going through the full optimization-data framework.
     */
    private static class TestOptimizer extends NonLinearConjugateGradientOptimizer {
        public double[] startPoint;
        public GoalType goalType;
        public int maxEvaluations;
        public ConvergenceChecker<PointValuePair> convergenceChecker;
        public double[] lowerBound;
        public double[] upperBound;

        public TestOptimizer(Formula updateFormula,
                             ConvergenceChecker<PointValuePair> checker,
                             UnivariateSolver lineSearchSolver,
                             Preconditioner preconditioner) {
            super(updateFormula, checker, lineSearchSolver, preconditioner);
            this.convergenceChecker = checker;
            this.maxEvaluations = 100;
        }

        @Override
        protected double computeObjectiveValue(double[] point) {
            // simple quadratic: f = x1^2 + x2^2
            return point[0] * point[0] + point[1] * point[1];
        }

        @Override
        protected double[] computeObjectiveGradient(double[] point) {
            // gradient: [2*x1, 2*x2]
            return new double[] {2 * point[0], 2 * point[1]};
        }

        @Override
        public double[] getStartPoint() { return startPoint; }

        @Override
        public GoalType getGoalType() { return goalType; }

        @Override
        public int getMaxEvaluations() { return maxEvaluations; }

        @Override
        public ConvergenceChecker<PointValuePair> getConvergenceChecker() {
            return convergenceChecker;
        }

        @Override
        public double[] getLowerBound() { return lowerBound; }

        @Override
        public double[] getUpperBound() { return upperBound; }

        /** Expose the protected method. */
        public void parseOptimizationDataPublic(OptimizationData... data) {
            parseOptimizationData(data);
        }
    }

    // ------------------------------------------------------------------
    // Test cases
    // ------------------------------------------------------------------

    // Tests for inner class BracketingStep
    @Test
    public void testBracketingStep_ConstructorAndGetter_ReturnsCorrectValue() {
        NonLinearConjugateGradientOptimizer.BracketingStep step = new NonLinearConjugateGradientOptimizer.BracketingStep(2.5);
        assertEquals(2.5, step.getBracketingStep(), 0.0);
    }

    // Tests for inner class IdentityPreconditioner
    @Test
    public void testIdentityPreconditioner_ReturnsClonedArray() {
        IdentityPreconditioner ip = new IdentityPreconditioner();
        double[] r = {1.0, 2.0, 3.0};
        double[] result = ip.precondition(new double[] {0, 0, 0}, r);
        assertArrayEquals(r, result, 0.0);
        assertNotSame("precondition should return a clone", r, result);
    }

    // Constructor with default solver and preconditioner
    @Test
    public void testConstructor_DefaultSolverAndPreconditioner_ShouldNotThrow() {
        try {
            new NonLinearConjugateGradientOptimizer(Formula.FLETCHER_REEVES, new NeverConverge());
        } catch (Exception e) {
            fail("Constructor threw exception: " + e);
        }
    }

    // Constructor with all parameters
    @Test
    public void testConstructor_WithSolverAndPreconditioner_ShouldNotThrow() {
        try {
            new NonLinearConjugateGradientOptimizer(Formula.FLETCHER_REEVES,
                    new NeverConverge(),
                    new BrentSolver(),
                    new IdentityPreconditioner());
        } catch (Exception e) {
            fail("Constructor threw exception: " + e);
        }
    }

    // Parsing of BracketingStep data updates the initialStep field
    @Test
    public void testParseOptimizationData_BracketingStep_UpdatesInitialStep() throws Exception {
        TestOptimizer optimizer = new TestOptimizer(Formula.FLETCHER_REEVES,
                new NeverConverge(),
                new BrentSolver(),
                new IdentityPreconditioner());
        NonLinearConjugateGradientOptimizer.BracketingStep step = new NonLinearConjugateGradientOptimizer.BracketingStep(7.5);
        optimizer.parseOptimizationDataPublic(step);

        Field f = NonLinearConjugateGradientOptimizer.class.getDeclaredField("initialStep");
        f.setAccessible(true);
        double actual = f.getDouble(optimizer);
        assertEquals(7.5, actual, 1e-15);
    }

    // Bounds are not supported -> MathUnsupportedOperationException
    @Test(expected = MathUnsupportedOperationException.class)
    public void testOptimize_WithBounds_ThrowsMathUnsupportedOperationException() {
        TestOptimizer opt = new TestOptimizer(Formula.FLETCHER_REEVES,
                new NeverConverge(), new BrentSolver(), new IdentityPreconditioner());
        opt.startPoint = new double[] {1.0, 1.0};
        opt.goalType = GoalType.MINIMIZE;
        opt.maxEvaluations = 50;
        opt.lowerBound = new double[] {-1.0, -1.0};
        opt.upperBound = new double[] {2.0, 2.0};
        opt.optimize();
    }

    // Normal optimization with Fletcher-Reeves and a simple quadratic
    @Test
    public void testOptimize_FletcherReeves_QuadraticMinimization_ReturnsSolution() {
        ConvergeAfterTwo checker = new ConvergeAfterTwo();
        TestOptimizer opt = new TestOptimizer(Formula.FLETCHER_REEVES, checker,
                new BrentSolver(), new IdentityPreconditioner());
        opt.startPoint = new double[] {3.0, 4.0};
        opt.goalType = GoalType.MINIMIZE;
        opt.maxEvaluations = 100;
        PointValuePair result = opt.optimize();
        assertNotNull("Result should not be null", result);
        assertNotNull("Result point should not be null", result.getPoint());
        double[] point = result.getPoint();
        double magnitude = Math.sqrt(point[0] * point[0] + point[1] * point[1]);
        assertTrue("Point should be closer to the origin after iterations", magnitude < 5.0);
    }

    // Normal optimization with Polak-Ribière
    @Test
    public void testOptimize_PolakRibiere_QuadraticMinimization_ReturnsSolution() {
        ConvergeAfterTwo checker = new ConvergeAfterTwo();
        TestOptimizer opt = new TestOptimizer(Formula.POLAK_RIBIERE, checker,
                new BrentSolver(), new IdentityPreconditioner());
        opt.startPoint = new double[] {3.0, 4.0};
        opt.goalType = GoalType.MINIMIZE;
        opt.maxEvaluations = 100;
        PointValuePair result = opt.optimize();
        assertNotNull("Result should not be null", result);
        assertNotNull("Result point should not be null", result.getPoint());
        double[] point = result.getPoint();
        double magnitude = Math.sqrt(point[0] * point[0] + point[1] * point[1]);
        assertTrue("Point should be closer to the origin after iterations", magnitude < 5.0);
    }

    // Exceeding the maximal number of evaluations should throw TooManyEvaluationsException
    // This test exposes the Defects4J Math-6b bug.
    @Test(expected = TooManyEvaluationsException.class, timeout = 5000)
    public void testOptimize_MaxEvalExceeded_ThrowsTooManyEvaluationsException() {
        TestOptimizer opt = new TestOptimizer(Formula.FLETCHER_REEVES,
                new NeverConverge(),
                new CustomSolver(),
                new IdentityPreconditioner());
        opt.startPoint = new double[] {1.0, 2.0};
        opt.goalType = GoalType.MINIMIZE;
        opt.maxEvaluations = 5;
        opt.optimize();
    }
}