package org.apache.commons.math.analysis.solvers;

import org.apache.commons.math.ConvergenceException;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MathException;
import org.apache.commons.math.analysis.SinFunction;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link UnivariateRealSolverUtils}.
 */
public class UnivariateRealSolverUtilsTest {

    // Tests midpoint calculation with positive numbers
    @Test
    public void testMidpoint_positiveValues_returnsCorrectMidpoint() {
        assertEquals(2.0, UnivariateRealSolverUtils.midpoint(1.0, 3.0), 1e-15);
    }

    // Tests midpoint calculation with mixed signs
    @Test
    public void testMidpoint_mixedValues_returnsCorrectMidpoint() {
        assertEquals(0.0, UnivariateRealSolverUtils.midpoint(-2.5, 2.5), 1e-15);
    }

    // Tests solve method with valid function and interval
    @Test
    public void testSolve_validInterval_findsRoot() throws MathException {
        UnivariateRealFunction f = new SinFunction();
        double root = UnivariateRealSolverUtils.solve(f, 3.0, 4.0);
        assertEquals(Math.PI, root, 1e-6);
    }

    // Tests solve method with custom absolute accuracy
    @Test
    public void testSolve_withAbsoluteAccuracy_findsRootAccurately() throws MathException {
        UnivariateRealFunction f = new SinFunction();
        double root = UnivariateRealSolverUtils.solve(f, 3.0, 4.0, 1e-10);
        assertEquals(Math.PI, root, 1e-10);
    }

    // Tests solve method with null function
    @Test(expected = IllegalArgumentException.class)
    public void testSolve_nullFunction_throwsIllegalArgumentException() throws MathException {
        UnivariateRealSolverUtils.solve(null, 0.0, 1.0);
    }

    // Tests solve method with invalid interval
    @Test(expected = IllegalArgumentException.class)
    public void testSolve_invalidInterval_throwsIllegalArgumentException() throws MathException {
        UnivariateRealFunction f = new SinFunction();
        UnivariateRealSolverUtils.solve(f, 2.0, 1.0);
    }

    // Tests bracket method with valid function finding a root interval
    @Test
    public void testBracket_validFunction_returnsBracketingInterval() throws MathException {
        UnivariateRealFunction f = new SinFunction();
        double[] result = UnivariateRealSolverUtils.bracket(f, 3.0, 0.0, 4.0);
        assertEquals(2, result.length);
        assertTrue(result[0] <= 3.0);
        assertTrue(result[1] >= 3.0);
        assertTrue(f.value(result[0]) * f.value(result[1]) <= 0.0);
    }

    // Tests bracket method when root is directly evaluated to zero (fa * fb == 0)
    @Test
    public void testBracket_rootHitExactly_returnsInterval() throws MathException {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x - 2.0;
            }
        };
        // initial = 1.0 -> a = 0.0, b = 2.0 -> f(b) = 0.0 -> fa * fb == 0.0
        double[] result = UnivariateRealSolverUtils.bracket(f, 1.0, 0.0, 3.0, 10);
        assertEquals(0.0, result[0], 1e-15);
        assertEquals(2.0, result[1], 1e-15);
        assertTrue(f.value(result[0]) * f.value(result[1]) <= 0.0);
    }

    // Tests bracket method with null function
    @Test(expected = IllegalArgumentException.class)
    public void testBracket_nullFunction_throwsIllegalArgumentException() throws MathException {
        UnivariateRealSolverUtils.bracket(null, 1.0, 0.0, 2.0);
    }

    // Tests bracket method with non-positive maximum iterations
    @Test(expected = IllegalArgumentException.class)
    public void testBracket_invalidMaxIterations_throwsIllegalArgumentException() throws MathException {
        UnivariateRealFunction f = new SinFunction();
        UnivariateRealSolverUtils.bracket(f, 1.0, 0.0, 2.0, 0);
    }

    // Tests bracket method when initial is below lower bound
    @Test(expected = IllegalArgumentException.class)
    public void testBracket_initialBelowLowerBound_throwsIllegalArgumentException() throws MathException {
        UnivariateRealFunction f = new SinFunction();
        UnivariateRealSolverUtils.bracket(f, -1.0, 0.0, 2.0);
    }

    // Tests bracket method when initial is above upper bound
    @Test(expected = IllegalArgumentException.class)
    public void testBracket_initialAboveUpperBound_throwsIllegalArgumentException() throws MathException {
        UnivariateRealFunction f = new SinFunction();
        UnivariateRealSolverUtils.bracket(f, 3.0, 0.0, 2.0);
    }

    // Tests bracket method when lower bound is greater than or equal to upper bound
    @Test(expected = IllegalArgumentException.class)
    public void testBracket_lowerBoundGeUpperBound_throwsIllegalArgumentException() throws MathException {
        UnivariateRealFunction f = new SinFunction();
        UnivariateRealSolverUtils.bracket(f, 1.0, 2.0, 2.0);
    }

    // Tests bracket method when root cannot be bracketed within max iterations
    @Test(expected = ConvergenceException.class)
    public void testBracket_exceedMaxIterations_throwsConvergenceException() throws MathException {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x + 100.0;
            }
        };
        UnivariateRealSolverUtils.bracket(f, 1.0, 0.0, 20.0, 2);
    }

    // Tests bracket method reaching boundaries without bracketing root
    @Test(expected = ConvergenceException.class)
    public void testBracket_reachBoundsWithoutRoot_throwsConvergenceException() throws MathException {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return 1.0;
            }
        };
        UnivariateRealSolverUtils.bracket(f, 1.0, 0.0, 2.0, 10);
    }
}