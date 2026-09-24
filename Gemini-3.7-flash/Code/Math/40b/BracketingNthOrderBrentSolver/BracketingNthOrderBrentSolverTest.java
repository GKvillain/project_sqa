package org.apache.commons.math.analysis.solvers;

import org.apache.commons.math.analysis.QuinticFunction;
import org.apache.commons.math.analysis.SinFunction;
import org.apache.commons.math.analysis.UnivariateFunction;
import org.apache.commons.math.exception.NoBracketingException;
import org.apache.commons.math.exception.NumberIsTooSmallException;
import org.apache.commons.math.exception.TooManyEvaluationsException;
import org.apache.commons.math.util.FastMath;
import org.junit.Assert;
import org.junit.Test;

public class BracketingNthOrderBrentSolverTest {

    // Tests default constructor and getter
    @Test
    public void testConstructor_default_maximalOrderIsFive() {
        BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver();
        Assert.assertEquals(5, solver.getMaximalOrder());
        Assert.assertEquals(1e-6, solver.getAbsoluteAccuracy(), 1.0e-15);
    }

    // Tests custom constructor with 2 arguments
    @Test
    public void testConstructor_customOrderAndAccuracy_configuredCorrectly() {
        BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver(1e-10, 4);
        Assert.assertEquals(4, solver.getMaximalOrder());
        Assert.assertEquals(1e-10, solver.getAbsoluteAccuracy(), 1.0e-15);
    }

    // Tests custom constructor with 3 arguments
    @Test
    public void testConstructor_relativeAndAbsoluteAccuracy_configuredCorrectly() {
        BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver(1e-12, 1e-10, 3);
        Assert.assertEquals(3, solver.getMaximalOrder());
        Assert.assertEquals(1e-12, solver.getRelativeAccuracy(), 1.0e-15);
        Assert.assertEquals(1e-10, solver.getAbsoluteAccuracy(), 1.0e-15);
    }

    // Tests custom constructor with 4 arguments
    @Test
    public void testConstructor_allParameters_configuredCorrectly() {
        BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver(1e-12, 1e-10, 1e-8, 2);
        Assert.assertEquals(2, solver.getMaximalOrder());
        Assert.assertEquals(1e-12, solver.getRelativeAccuracy(), 1.0e-15);
        Assert.assertEquals(1e-10, solver.getAbsoluteAccuracy(), 1.0e-15);
        Assert.assertEquals(1e-8, solver.getFunctionValueAccuracy(), 1.0e-15);
    }

    // Tests exception path for invalid maximal order (< 2)
    @Test(expected = NumberIsTooSmallException.class)
    public void testConstructor_maximalOrderTooSmall_throwsException() {
        new BracketingNthOrderBrentSolver(1e-6, 1);
    }

    // Tests exception path for constructor with 3 parameters and invalid maximal order
    @Test(expected = NumberIsTooSmallException.class)
    public void testConstructor_threeArgsMaximalOrderTooSmall_throwsException() {
        new BracketingNthOrderBrentSolver(1e-6, 1e-6, 1);
    }

    // Tests exception path for constructor with 4 parameters and invalid maximal order
    @Test(expected = NumberIsTooSmallException.class)
    public void testConstructor_fourArgsMaximalOrderTooSmall_throwsException() {
        new BracketingNthOrderBrentSolver(1e-6, 1e-6, 1e-6, 0);
    }

    // Tests exact root hit at initial guess / start value
    @Test
    public void testSolve_startValueIsRoot_returnsStartValue() {
        BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver();
        UnivariateFunction f = new SinFunction();
        double root = solver.solve(100, f, -0.5, 0.5, 0.0, AllowedSolution.ANY_SIDE);
        Assert.assertEquals(0.0, root, 1.0e-15);
        Assert.assertEquals(1, solver.getEvaluations());
    }

    // Tests exact root hit at min endpoint
    @Test
    public void testSolve_minIsRoot_returnsMin() {
        BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver();
        UnivariateFunction f = new SinFunction();
        double root = solver.solve(100, f, 0.0, 1.5, 0.5, AllowedSolution.ANY_SIDE);
        Assert.assertEquals(0.0, root, 1.0e-15);
        Assert.assertEquals(2, solver.getEvaluations());
    }

    // Tests exact root hit at max endpoint
    @Test
    public void testSolve_maxIsRoot_returnsMax() {
        BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver();
        UnivariateFunction f = new SinFunction();
        double root = solver.solve(100, f, -1.5, 0.0, -0.5, AllowedSolution.ANY_SIDE);
        Assert.assertEquals(0.0, root, 1.0e-15);
        Assert.assertEquals(3, solver.getEvaluations());
    }

    // Tests non-bracketing interval throwing NoBracketingException
    @Test(expected = NoBracketingException.class)
    public void testSolve_noBracketing_throwsException() {
        BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver();
        UnivariateFunction f = new SinFunction();
        solver.solve(100, f, 1.0, 2.0, 1.5, AllowedSolution.ANY_SIDE);
    }

    // Tests normal root finding on quintic function
    @Test
    public void testSolve_quinticFunction_returnsCorrectRoot() {
        BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver(1.0e-12, 1.0e-10, 5);
        UnivariateFunction f = new QuinticFunction();
        double root = solver.solve(100, f, 0.2, 0.7, 0.4, AllowedSolution.ANY_SIDE);
        Assert.assertEquals(0.5, root, 1.0e-8);
        Assert.assertTrue(FastMath.abs(f.value(root)) <= 1.0e-8);
    }

    // Tests AllowedSolution side selection (LEFT, RIGHT, BELOW, ABOVE)
    @Test
    public void testSolve_allowedSolutions_returnsBoundedRoots() {
        BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver(1.0e-14, 1.0e-10, 1.0e-15, 5);
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return FastMath.sin(x);
            }
        };

        double left = solver.solve(100, f, -0.5, 1.0, AllowedSolution.LEFT_SIDE);
        Assert.assertTrue(left <= 0.0);

        double right = solver.solve(100, f, -0.5, 1.0, AllowedSolution.RIGHT_SIDE);
        Assert.assertTrue(right >= 0.0);

        double below = solver.solve(100, f, -0.5, 1.0, AllowedSolution.BELOW_SIDE);
        Assert.assertTrue(f.value(below) <= 0.0);

        double above = solver.solve(100, f, -0.5, 1.0, AllowedSolution.ABOVE_SIDE);
        Assert.assertTrue(f.value(above) >= 0.0);
    }

    // Tests solve method without startValue parameter
    @Test
    public void testSolve_withoutStartValue_findsRoot() {
        BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver();
        UnivariateFunction f = new SinFunction();
        double root = solver.solve(100, f, 3.0, 4.0, AllowedSolution.ANY_SIDE);
        Assert.assertEquals(FastMath.PI, root, 1.0e-6);
    }

    // Tests high order convergence and defect 40b bug trigger (aging and array updates in sub-step evaluation)
    @Test
    public void testSolve_convergenceIssueReproduction_defect40() {
        BracketingNthOrderBrentSolver solver =
            new BracketingNthOrderBrentSolver(1.0e-12, 1.0e-10, 1.0e-15, 5);
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return (x - 1.5) * (x - 1.5) * (x - 1.5) - 1.0;
            }
        };

        double root = solver.solve(100, f, 1.0, 3.0, 2.0, AllowedSolution.ANY_SIDE);
        Assert.assertEquals(2.5, root, 1.0e-8);
    }

    // Tests step function causing fallback to bisection / order reductions
    @Test
    public void testSolve_stepFunction_fallsBackToBisection() {
        BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver(1.0e-6, 5);
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return x < 0.5 ? -1.0 : 1.0;
            }
        };

        double root = solver.solve(100, f, 0.0, 1.0, 0.3, AllowedSolution.ANY_SIDE);
        Assert.assertEquals(0.5, root, 1.0e-5);
    }

    // Tests exceeding max evaluation count throws exception
    @Test(expected = TooManyEvaluationsException.class)
    public void testSolve_exceedingMaxEvaluations_throwsException() {
        BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver(1.0e-14, 1.0e-14, 2);
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return FastMath.sin(x);
            }
        };
        solver.solve(2, f, 3.0, 4.0, AllowedSolution.ANY_SIDE);
    }
}