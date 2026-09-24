package org.apache.commons.math.analysis.solvers;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MaxIterationsExceededException;
import org.apache.commons.math.analysis.UnivariateRealFunction;

public class BrentSolverTest {

    // Tests finding root when initial value is already an exact root
    @Test
    public void testSolve_rootAtInitial_returnsInitial() throws Exception {
        BrentSolver solver = new BrentSolver();
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return (x - 1.0) * (x - 2.0);
            }
        };

        double result = solver.solve(f, 0.5, 2.5, 1.0);
        assertEquals(1.0, result, 1E-6);
    }

    // Tests finding root when min endpoint is an exact root (with initial guess)
    @Test
    public void testSolve_rootAtMinWithInitial_returnsMin() throws Exception {
        BrentSolver solver = new BrentSolver();
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return Math.sin(x);
            }
        };

        // Root at min = Math.PI
        double result = solver.solve(f, Math.PI, 4.0, 3.5);
        assertEquals(Math.PI, result, 1E-6);
    }

    // Tests finding root when max endpoint is an exact root (with initial guess)
    @Test
    public void testSolve_rootAtMaxWithInitial_returnsMax() throws Exception {
        BrentSolver solver = new BrentSolver();
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return Math.sin(x);
            }
        };

        // Root at max = Math.PI
        double result = solver.solve(f, 2.5, Math.PI, 2.8);
        assertEquals(Math.PI, result, 1E-6);
    }

    // Tests bracketing interval between min and initial
    @Test
    public void testSolve_rootBetweenMinAndInitial_returnsCorrectResult() throws Exception {
        BrentSolver solver = new BrentSolver();
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x - 4.0;
            }
        };

        // Root at 2.0 is between min (1.0) and initial (2.5)
        double result = solver.solve(f, 1.0, 4.0, 2.5);
        assertEquals(2.0, result, 1E-6);
    }

    // Tests bracketing interval between initial and max
    @Test
    public void testSolve_rootBetweenInitialAndMax_returnsCorrectResult() throws Exception {
        BrentSolver solver = new BrentSolver();
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x - 4.0;
            }
        };

        // Root at 2.0 is between initial (1.5) and max (3.0)
        double result = solver.solve(f, 0.0, 3.0, 1.5);
        assertEquals(2.0, result, 1E-6);
    }

    // Tests non-bracketing interval with initial guess throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testSolve_noRootInIntervalWithInitial_throwsIllegalArgumentException() throws Exception {
        BrentSolver solver = new BrentSolver();
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x + 1.0;
            }
        };

        solver.solve(f, 1.0, 3.0, 2.0);
    }

    // Tests invalid sequence where initial is not between min and max
    @Test(expected = IllegalArgumentException.class)
    public void testSolve_invalidSequence_throwsIllegalArgumentException() throws Exception {
        BrentSolver solver = new BrentSolver();
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x - 2.0;
            }
        };

        solver.solve(f, 3.0, 1.0, 2.0);
    }

    // Tests 2-argument solve method with standard bracketing interval
    @Test
    public void testSolve_twoArgsStandardInterval_returnsCorrectResult() throws Exception {
        BrentSolver solver = new BrentSolver();
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x * x - 8.0;
            }
        };

        double result = solver.solve(f, 1.0, 3.0);
        assertEquals(2.0, result, 1E-6);
    }

    // Tests 2-argument solve method when min is exact root
    @Test
    public void testSolve_twoArgsRootAtMin_returnsMin() throws Exception {
        BrentSolver solver = new BrentSolver();
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return (x - 1.0) * (x - 3.0);
            }
        };

        double result = solver.solve(f, 1.0, 2.0);
        assertEquals(1.0, result, 1E-6);
    }

    // Tests 2-argument solve method when max is exact root
    @Test
    public void testSolve_twoArgsRootAtMax_returnsMax() throws Exception {
        BrentSolver solver = new BrentSolver();
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return (x - 1.0) * (x - 3.0);
            }
        };

        double result = solver.solve(f, 2.0, 3.0);
        assertEquals(3.0, result, 1E-6);
    }

    // Tests 2-argument solve method when endpoints have same sign but min is close to zero
    @Test
    public void testSolve_twoArgsSameSignRootNearMin_returnsMin() throws Exception {
        BrentSolver solver = new BrentSolver();
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return (x - 1.0) * (x - 1.0);
            }
        };

        double result = solver.solve(f, 1.0, 2.0);
        assertEquals(1.0, result, 1E-6);
    }

    // Tests 2-argument solve method when endpoints have same sign but max is close to zero
    @Test
    public void testSolve_twoArgsSameSignRootNearMax_returnsMax() throws Exception {
        BrentSolver solver = new BrentSolver();
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return (x - 2.0) * (x - 2.0);
            }
        };

        double result = solver.solve(f, 1.0, 2.0);
        assertEquals(2.0, result, 1E-6);
    }

    // Tests 2-argument solve method when neither endpoint is root and both have same sign
    @Test(expected = IllegalArgumentException.class)
    public void testSolve_twoArgsNonBracketing_throwsIllegalArgumentException() throws Exception {
        BrentSolver solver = new BrentSolver();
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x + 1.0;
            }
        };

        solver.solve(f, 1.0, 2.0);
    }

    // Tests deprecated constructors and deprecated solve methods
    @Test
    @SuppressWarnings("deprecation")
    public void testSolve_deprecatedMethods_returnsCorrectResult() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return 2.0 * x - 4.0;
            }
        };

        BrentSolver solver = new BrentSolver(f);
        double result1 = solver.solve(0.0, 3.0);
        assertEquals(2.0, result1, 1E-6);

        double result2 = solver.solve(0.0, 3.0, 1.0);
        assertEquals(2.0, result2, 1E-6);
    }

    // Tests linear and inverse quadratic interpolation branches in algorithm
    @Test
    public void testSolve_transcendentalFunction_findsAccurateRoot() throws Exception {
        BrentSolver solver = new BrentSolver();
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return Math.exp(x) - 3.0;
            }
        };

        double expected = Math.log(3.0);
        double result = solver.solve(f, 0.0, 2.0, 0.5);
        assertEquals(expected, result, 1E-6);
    }
}