package org.apache.commons.math.analysis.solvers;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MaxIterationsExceededException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class BisectionSolverTest {

    private UnivariateRealFunction linearFunction;
    private UnivariateRealFunction quadraticFunction;

    @Before
    public void setUp() {
        linearFunction = new UnivariateRealFunction() {
            public double value(double x) {
                return 2.0 * x - 4.0; // root at x = 2.0
            }
        };

        quadraticFunction = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x - 4.0; // roots at x = -2.0 and x = 2.0
            }
        };
    }

    // Tests solving with function and initial value using default constructor (catches Math-70 bug)
    @Test
    public void testSolve_withFunctionAndInitial_solvesCorrectly() throws Exception {
        BisectionSolver solver = new BisectionSolver();
        double result = solver.solve(linearFunction, 1.0, 3.0, 1.5);
        assertEquals(2.0, result, 1E-6);
    }

    // Tests solving with function, min, and max using default constructor
    @Test
    public void testSolve_withFunctionAndMinMax_solvesCorrectly() throws Exception {
        BisectionSolver solver = new BisectionSolver();
        double result = solver.solve(linearFunction, 0.0, 5.0);
        assertEquals(2.0, result, 1E-6);
    }

    // Tests solving quadratic function root
    @Test
    public void testSolve_quadraticFunction_returnsAccurateRoot() throws Exception {
        BisectionSolver solver = new BisectionSolver();
        double result = solver.solve(quadraticFunction, 0.0, 5.0);
        assertEquals(2.0, result, 1E-6);
    }

    // Tests solving with deprecated constructor and 2-parameter solve
    @Test
    @SuppressWarnings("deprecation")
    public void testSolve_deprecatedConstructorMinMax_solvesCorrectly() throws Exception {
        BisectionSolver solver = new BisectionSolver(linearFunction);
        double result = solver.solve(0.0, 4.0);
        assertEquals(2.0, result, 1E-6);
    }

    // Tests solving with deprecated constructor and 3-parameter solve
    @Test
    @SuppressWarnings("deprecation")
    public void testSolve_deprecatedConstructorMinMaxInitial_solvesCorrectly() throws Exception {
        BisectionSolver solver = new BisectionSolver(linearFunction);
        double result = solver.solve(0.0, 4.0, 1.0);
        assertEquals(2.0, result, 1E-6);
    }

    // Tests branch where fm * fmin > 0.0 moves min to midpoint
    @Test
    public void testSolve_rootCloserToMax_updatesMinBoundary() throws Exception {
        BisectionSolver solver = new BisectionSolver();
        // Root at 3.0 in [0, 4.0], mid is 2.0, f(0)=-3, f(2)=-1 -> f(0)*f(2)>0 -> min becomes 2.0
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x - 3.0;
            }
        };
        double result = solver.solve(f, 0.0, 4.0);
        assertEquals(3.0, result, 1E-6);
    }

    // Tests branch where fm * fmin <= 0.0 moves max to midpoint
    @Test
    public void testSolve_rootCloserToMin_updatesMaxBoundary() throws Exception {
        BisectionSolver solver = new BisectionSolver();
        // Root at 1.0 in [0, 4.0], mid is 2.0, f(0)=-1, f(2)=1 -> f(0)*f(2)<0 -> max becomes 2.0
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x - 1.0;
            }
        };
        double result = solver.solve(f, 0.0, 4.0);
        assertEquals(1.0, result, 1E-6);
    }

    // Tests invalid interval where min is greater than max
    @Test(expected = IllegalArgumentException.class)
    public void testSolve_minGreaterThanMax_throwsIllegalArgumentException() throws Exception {
        BisectionSolver solver = new BisectionSolver();
        solver.solve(linearFunction, 5.0, 1.0);
    }

    // Tests invalid interval where min equals max
    @Test(expected = IllegalArgumentException.class)
    public void testSolve_minEqualsMax_throwsIllegalArgumentException() throws Exception {
        BisectionSolver solver = new BisectionSolver();
        solver.solve(linearFunction, 2.0, 2.0);
    }

    // Tests exception when maximum iteration count is exceeded
    @Test(expected = MaxIterationsExceededException.class)
    public void testSolve_lowIterationCount_throwsMaxIterationsExceededException() throws Exception {
        BisectionSolver solver = new BisectionSolver();
        solver.setMaximalIterationCount(1);
        solver.setAbsoluteAccuracy(1E-15);
        solver.solve(linearFunction, 0.0, 5.0);
    }

    // Tests result count and iteration count recorded after solve
    @Test
    public void testSolve_verifyResultAndIterationCount() throws Exception {
        BisectionSolver solver = new BisectionSolver();
        double result = solver.solve(linearFunction, 1.0, 4.0);
        assertEquals(2.0, result, 1E-6);
        assertEquals(2.0, solver.getResult(), 1E-6);
        assertTrue(solver.getIterationCount() > 0);
    }

    // Tests negative values interval
    @Test
    public void testSolve_negativeInterval_returnsCorrectRoot() throws Exception {
        BisectionSolver solver = new BisectionSolver();
        double result = solver.solve(quadraticFunction, -4.0, 0.0);
        assertEquals(-2.0, result, 1E-6);
    }
}