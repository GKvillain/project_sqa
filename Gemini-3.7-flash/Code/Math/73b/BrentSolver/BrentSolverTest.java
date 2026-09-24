package org.apache.commons.math.analysis.solvers;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MaxIterationsExceededException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.junit.Test;
import static org.junit.Assert.*;

public class BrentSolverTest {

    // Tests finding root when initial guess is already the root
    @Test
    public void testSolve_rootAtInitial_returnsInitial() throws Exception {
        BrentSolver solver = new BrentSolver();
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x - 4.0;
            }
        };
        double result = solver.solve(f, 1.0, 3.0, 2.0);
        assertEquals(2.0, result, 1E-6);
    }

    // Tests finding root when min boundary is the root
    @Test
    public void testSolve_rootAtMin_returnsMin() throws Exception {
        BrentSolver solver = new BrentSolver();
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x - 1.0;
            }
        };
        double result = solver.solve(f, 1.0, 3.0, 2.0);
        assertEquals(1.0, result, 1E-6);
    }

    // Tests finding root when max boundary is the root
    @Test
    public void testSolve_rootAtMax_returnsMax() throws Exception {
        BrentSolver solver = new BrentSolver();
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x - 3.0;
            }
        };
        double result = solver.solve(f, 1.0, 3.0, 2.0);
        assertEquals(3.0, result, 1E-6);
    }

    // Tests root bracketing in the lower sub-interval [min, initial]
    @Test
    public void testSolve_bracketMinInitial_findsRoot() throws Exception {
        BrentSolver solver = new BrentSolver();
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x - 1.5;
            }
        };
        double result = solver.solve(f, 1.0, 3.0, 2.0);
        assertEquals(1.5, result, 1E-6);
    }

    // Tests root bracketing in the upper sub-interval [initial, max]
    @Test
    public void testSolve_bracketInitialMax_findsRoot() throws Exception {
        BrentSolver solver = new BrentSolver();
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x - 2.5;
            }
        };
        double result = solver.solve(f, 1.0, 3.0, 2.0);
        assertEquals(2.5, result, 1E-6);
    }

    // Tests exception path when non-bracketing interval is provided with initial value (Math-73 defect)
    @Test(expected = IllegalArgumentException.class)
    public void testSolve_noBracketingWithInitial_throwsIllegalArgumentException() throws Exception {
        BrentSolver solver = new BrentSolver();
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x + 1.0;
            }
        };
        solver.solve(f, 1.0, 5.0, 2.0);
    }

    // Tests invalid sequence where min >= initial or initial >= max
    @Test(expected = IllegalArgumentException.class)
    public void testSolve_invalidSequence_throwsIllegalArgumentException() throws Exception {
        BrentSolver solver = new BrentSolver();
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x;
            }
        };
        solver.solve(f, 3.0, 1.0, 2.0);
    }

    // Tests standard interval solve without initial guess
    @Test
    public void testSolve_intervalWithoutInitial_findsRoot() throws Exception {
        BrentSolver solver = new BrentSolver();
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x - 4.0;
            }
        };
        double result = solver.solve(f, 0.0, 3.0);
        assertEquals(2.0, result, 1E-6);
    }

    // Tests standard interval solve when endpoints have identical signs
    @Test(expected = IllegalArgumentException.class)
    public void testSolve_intervalEndpointsSameSign_throwsIllegalArgumentException() throws Exception {
        BrentSolver solver = new BrentSolver();
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x - 4.0;
            }
        };
        solver.solve(f, 3.0, 5.0);
    }

    // Tests standard interval solve when min is exact zero
    @Test
    public void testSolve_intervalRootAtMin_returnsMin() throws Exception {
        BrentSolver solver = new BrentSolver();
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x;
            }
        };
        double result = solver.solve(f, 0.0, 2.0);
        assertEquals(0.0, result, 1E-6);
    }

    // Tests standard interval solve when max is exact zero
    @Test
    public void testSolve_intervalRootAtMax_returnsMax() throws Exception {
        BrentSolver solver = new BrentSolver();
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x - 2.0;
            }
        };
        double result = solver.solve(f, 0.0, 2.0);
        assertEquals(2.0, result, 1E-6);
    }

    // Tests standard interval solve when min value is within function accuracy
    @Test
    public void testSolve_intervalMinNearZero_returnsMin() throws Exception {
        BrentSolver solver = new BrentSolver();
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return (x == 1.0) ? 1E-7 : 2.0;
            }
        };
        double result = solver.solve(f, 1.0, 3.0);
        assertEquals(1.0, result, 1E-6);
    }

    // Tests standard interval solve when max value is within function accuracy
    @Test
    public void testSolve_intervalMaxNearZero_returnsMax() throws Exception {
        BrentSolver solver = new BrentSolver();
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return (x == 3.0) ? 1E-7 : 2.0;
            }
        };
        double result = solver.solve(f, 1.0, 3.0);
        assertEquals(3.0, result, 1E-6);
    }

    // Tests trigonometric continuous function solving
    @Test
    public void testSolve_sinFunction_findsPi() throws Exception {
        BrentSolver solver = new BrentSolver();
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return Math.sin(x);
            }
        };
        double result = solver.solve(f, 3.0, 4.0);
        assertEquals(Math.PI, result, 1E-6);
    }

    // Tests polynomial requiring inverse quadratic interpolation and multiple iterations
    @Test
    public void testSolve_cubicPolynomial_convergesToRoot() throws Exception {
        BrentSolver solver = new BrentSolver();
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return (x - 1.0) * (x - 2.0) * (x - 3.0);
            }
        };
        double result = solver.solve(f, -0.5, 1.5, 0.2);
        assertEquals(1.0, result, 1E-6);
    }

    // Tests deprecated constructor and solve methods
    @SuppressWarnings("deprecation")
    @Test
    public void testSolve_deprecatedConstructorAndMethods_returnsRoot() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return 2.0 * x - 4.0;
            }
        };
        BrentSolver solver = new BrentSolver(f);
        double result1 = solver.solve(0.0, 3.0);
        assertEquals(2.0, result1, 1E-6);

        double result2 = solver.solve(0.0, 3.0, 1.5);
        assertEquals(2.0, result2, 1E-6);
    }
}