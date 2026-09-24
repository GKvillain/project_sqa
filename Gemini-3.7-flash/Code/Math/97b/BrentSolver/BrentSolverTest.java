package org.apache.commons.math.analysis;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MaxIterationsExceededException;
import org.junit.Test;
import static org.junit.Assert.*;

public class BrentSolverTest {

    // Tests solving a standard polynomial root where endpoints bracket the root
    @Test
    public void testSolve_standardBracketingInterval_returnsRoot() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return 2 * x - 4;
            }
        };
        BrentSolver solver = new BrentSolver(f);
        double result = solver.solve(0.0, 5.0);
        assertEquals(2.0, result, 1E-6);
    }

    // Tests defect Math-97: solving when root is at lower endpoint
    @Test
    public void testSolve_rootAtLowerEndpoint_returnsRoot() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return (x - 1.0) * (x - 2.0);
            }
        };
        BrentSolver solver = new BrentSolver(f);
        double result = solver.solve(1.0, 1.5);
        assertEquals(1.0, result, 1E-6);
    }

    // Tests defect Math-97: solving when root is at upper endpoint
    @Test
    public void testSolve_rootAtUpperEndpoint_returnsRoot() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return (x - 1.0) * (x - 2.0);
            }
        };
        BrentSolver solver = new BrentSolver(f);
        double result = solver.solve(1.5, 2.0);
        assertEquals(2.0, result, 1E-6);
    }

    // Tests exception when endpoints do not bracket a root and neither is a root
    @Test(expected = IllegalArgumentException.class)
    public void testSolve_noBracketingEndpoints_throwsIllegalArgumentException() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x + 1.0;
            }
        };
        BrentSolver solver = new BrentSolver(f);
        solver.solve(1.0, 2.0);
    }

    // Tests invalid interval where min is greater than max
    @Test(expected = IllegalArgumentException.class)
    public void testSolve_minGreaterThanMax_throwsIllegalArgumentException() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x;
            }
        };
        BrentSolver solver = new BrentSolver(f);
        solver.solve(2.0, 1.0);
    }

    // Tests solve with initial guess when initial guess is outside the interval
    @Test(expected = IllegalArgumentException.class)
    public void testSolveWithInitial_initialOutsideInterval_throwsIllegalArgumentException() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x;
            }
        };
        BrentSolver solver = new BrentSolver(f);
        solver.solve(0.0, 2.0, 3.0);
    }

    // Tests solve with initial guess when initial guess is already the root
    @Test
    public void testSolveWithInitial_initialIsRoot_returnsInitial() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x - 3.0;
            }
        };
        BrentSolver solver = new BrentSolver(f);
        double result = solver.solve(1.0, 5.0, 3.0);
        assertEquals(3.0, result, 1E-6);
    }

    // Tests solve with initial guess when min endpoint is the root
    @Test
    public void testSolveWithInitial_minIsRoot_returnsMin() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x - 1.0;
            }
        };
        BrentSolver solver = new BrentSolver(f);
        double result = solver.solve(1.0, 5.0, 3.0);
        assertEquals(1.0, result, 1E-6);
    }

    // Tests solve with initial guess when max endpoint is the root
    @Test
    public void testSolveWithInitial_maxIsRoot_returnsMax() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x - 5.0;
            }
        };
        BrentSolver solver = new BrentSolver(f);
        double result = solver.solve(1.0, 5.0, 3.0);
        assertEquals(5.0, result, 1E-6);
    }

    // Tests solve with initial guess when root is between min and initial
    @Test
    public void testSolveWithInitial_rootBetweenMinAndInitial_returnsRoot() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x - 2.0;
            }
        };
        BrentSolver solver = new BrentSolver(f);
        double result = solver.solve(1.0, 5.0, 3.0);
        assertEquals(2.0, result, 1E-6);
    }

    // Tests solve with initial guess when root is between initial and max
    @Test
    public void testSolveWithInitial_rootBetweenInitialAndMax_returnsRoot() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x - 4.0;
            }
        };
        BrentSolver solver = new BrentSolver(f);
        double result = solver.solve(1.0, 5.0, 3.0);
        assertEquals(4.0, result, 1E-6);
    }

    // Tests solve for non-linear function requiring inverse quadratic interpolation
    @Test
    public void testSolve_nonLinearFunction_returnsAccurateRoot() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return Math.sin(x);
            }
        };
        BrentSolver solver = new BrentSolver(f);
        double result = solver.solve(3.0, 4.0);
        assertEquals(Math.PI, result, 1E-6);
    }

    // Tests cubic polynomial requiring multiple Brent algorithm iterations
    @Test
    public void testSolve_cubicPolynomial_returnsCorrectRoot() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return (x - 1.0) * (x - 2.0) * (x - 3.0);
            }
        };
        BrentSolver solver = new BrentSolver(f);
        double result = solver.solve(1.5, 2.5);
        assertEquals(2.0, result, 1E-6);
    }

    // Tests exceeding maximal iteration count
    @Test(expected = MaxIterationsExceededException.class)
    public void testSolve_exceedMaximalIterationCount_throwsMaxIterationsExceededException() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return Math.sin(x);
            }
        };
        BrentSolver solver = new BrentSolver(f);
        solver.setMaximalIterationCount(1);
        solver.solve(3.0, 4.0);
    }
}