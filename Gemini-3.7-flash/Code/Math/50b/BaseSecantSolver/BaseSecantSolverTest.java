package org.apache.commons.math.analysis.solvers;

import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.exception.NoBracketingException;
import org.apache.commons.math.exception.TooManyEvaluationsException;
import org.apache.commons.math.util.FastMath;
import org.junit.Test;
import static org.junit.Assert.*;

public class BaseSecantSolverTest {

    /**
     * Concrete test implementation of BaseSecantSolver.
     */
    private static class TestSecantSolver extends BaseSecantSolver {
        TestSecantSolver(final double absoluteAccuracy, final Method method) {
            super(absoluteAccuracy, method);
        }

        TestSecantSolver(final double relativeAccuracy, final double absoluteAccuracy, final Method method) {
            super(relativeAccuracy, absoluteAccuracy, method);
        }

        TestSecantSolver(final double relativeAccuracy, final double absoluteAccuracy,
                         final double functionValueAccuracy, final Method method) {
            super(relativeAccuracy, absoluteAccuracy, functionValueAccuracy, method);
        }
    }

    // Tests exact root at the lower bound
    @Test
    public void testSolve_exactRootAtMin_returnsMin() {
        BaseSecantSolver solver = new TestSecantSolver(1e-6, BaseSecantSolver.Method.ILLINOIS);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x - 1.0;
            }
        };
        double root = solver.solve(100, f, 1.0, 3.0, AllowedSolution.ANY_SIDE);
        assertEquals(1.0, root, 1e-15);
    }

    // Tests exact root at the upper bound
    @Test
    public void testSolve_exactRootAtMax_returnsMax() {
        BaseSecantSolver solver = new TestSecantSolver(1e-6, BaseSecantSolver.Method.PEGASUS);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x - 3.0;
            }
        };
        double root = solver.solve(100, f, 1.0, 3.0, AllowedSolution.ANY_SIDE);
        assertEquals(3.0, root, 1e-15);
    }

    // Tests finding root using Illinois method
    @Test
    public void testSolve_illinoisMethod_findsCorrectRoot() {
        BaseSecantSolver solver = new TestSecantSolver(1e-14, 1e-10, BaseSecantSolver.Method.ILLINOIS);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x * x - 27.0;
            }
        };
        double root = solver.solve(100, f, 1.0, 5.0, AllowedSolution.ANY_SIDE);
        assertEquals(3.0, root, 1e-6);
        assertEquals(0.0, f.value(root), 1e-5);
    }

    // Tests finding root using Pegasus method
    @Test
    public void testSolve_pegasusMethod_findsCorrectRoot() {
        BaseSecantSolver solver = new TestSecantSolver(1e-14, 1e-10, BaseSecantSolver.Method.PEGASUS);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return FastMath.sin(x);
            }
        };
        double root = solver.solve(100, f, 3.0, 4.0, AllowedSolution.ANY_SIDE);
        assertEquals(FastMath.PI, root, 1e-6);
    }

    // Tests finding root using Regula Falsi method
    @Test
    public void testSolve_regulaFalsiMethod_findsCorrectRoot() {
        BaseSecantSolver solver = new TestSecantSolver(1e-14, 1e-10, 1e-15, BaseSecantSolver.Method.REGULA_FALSI);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x - 4.0;
            }
        };
        double root = solver.solve(100, f, 1.0, 3.0);
        assertEquals(2.0, root, 1e-6);
    }

    // Tests solution with LEFT_SIDE constraint
    @Test
    public void testSolve_allowedSolutionLeftSide_returnsRootOnLeftSide() {
        BaseSecantSolver solver = new TestSecantSolver(1e-14, 1e-6, BaseSecantSolver.Method.ILLINOIS);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x - 2.0;
            }
        };
        double root = solver.solve(100, f, 1.0, 2.0, AllowedSolution.LEFT_SIDE);
        assertTrue(root <= FastMath.sqrt(2.0));
        assertEquals(FastMath.sqrt(2.0), root, 1e-5);
    }

    // Tests solution with RIGHT_SIDE constraint
    @Test
    public void testSolve_allowedSolutionRightSide_returnsRootOnRightSide() {
        BaseSecantSolver solver = new TestSecantSolver(1e-14, 1e-6, BaseSecantSolver.Method.ILLINOIS);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x - 2.0;
            }
        };
        double root = solver.solve(100, f, 1.0, 2.0, AllowedSolution.RIGHT_SIDE);
        assertTrue(root >= FastMath.sqrt(2.0));
        assertEquals(FastMath.sqrt(2.0), root, 1e-5);
    }

    // Tests solution with BELOW_SIDE constraint
    @Test
    public void testSolve_allowedSolutionBelowSide_returnsNegativeFunctionValue() {
        BaseSecantSolver solver = new TestSecantSolver(1e-14, 1e-6, BaseSecantSolver.Method.PEGASUS);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x - 2.0;
            }
        };
        double root = solver.solve(100, f, 1.0, 2.0, AllowedSolution.BELOW_SIDE);
        assertTrue(f.value(root) <= 0.0);
        assertEquals(FastMath.sqrt(2.0), root, 1e-5);
    }

    // Tests solution with ABOVE_SIDE constraint
    @Test
    public void testSolve_allowedSolutionAboveSide_returnsPositiveFunctionValue() {
        BaseSecantSolver solver = new TestSecantSolver(1e-14, 1e-6, BaseSecantSolver.Method.PEGASUS);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x - 2.0;
            }
        };
        double root = solver.solve(100, f, 1.0, 2.0, AllowedSolution.ABOVE_SIDE);
        assertTrue(f.value(root) >= 0.0);
        assertEquals(FastMath.sqrt(2.0), root, 1e-5);
    }

    // Tests solve with initial start value parameter
    @Test
    public void testSolve_withStartValue_returnsCorrectRoot() {
        BaseSecantSolver solver = new TestSecantSolver(1e-6, BaseSecantSolver.Method.ILLINOIS);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return FastMath.exp(x) - 3.0;
            }
        };
        double root = solver.solve(100, f, 0.0, 2.0, 1.0, AllowedSolution.ANY_SIDE);
        assertEquals(FastMath.log(3.0), root, 1e-6);
    }

    // Tests non-bracketing interval throws NoBracketingException
    @Test(expected = NoBracketingException.class)
    public void testSolve_noBracketing_throwsException() {
        BaseSecantSolver solver = new TestSecantSolver(1e-6, BaseSecantSolver.Method.ILLINOIS);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x + 1.0;
            }
        };
        solver.solve(100, f, 1.0, 5.0, AllowedSolution.ANY_SIDE);
    }

    // Tests evaluation limit exhaustion
    @Test(expected = TooManyEvaluationsException.class)
    public void testSolve_exceedMaxEvaluations_throwsException() {
        BaseSecantSolver solver = new TestSecantSolver(1e-15, 1e-15, BaseSecantSolver.Method.REGULA_FALSI);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return FastMath.exp(x) - FastMath.pow(x, 4);
            }
        };
        solver.solve(2, f, -1.0, 0.0);
    }

    // Tests 1-argument constructor accuracy default
    @Test
    public void testConstructor_singleAccuracyParameter_setsCorrectAccuracy() {
        BaseSecantSolver solver = new TestSecantSolver(1e-8, BaseSecantSolver.Method.PEGASUS);
        assertEquals(1e-8, solver.getAbsoluteAccuracy(), 1e-15);
    }
}