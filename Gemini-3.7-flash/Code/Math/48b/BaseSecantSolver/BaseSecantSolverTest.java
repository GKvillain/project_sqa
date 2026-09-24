package org.apache.commons.math.analysis.solvers;

import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.exception.ConvergenceException;
import org.apache.commons.math.exception.NoBracketingException;
import org.apache.commons.math.exception.TooManyEvaluationsException;
import org.apache.commons.math.util.FastMath;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BaseSecantSolverTest {

    private static class ConcreteSecantSolver extends BaseSecantSolver {
        ConcreteSecantSolver(final double absoluteAccuracy, final Method method) {
            super(absoluteAccuracy, method);
        }

        ConcreteSecantSolver(final double relativeAccuracy, final double absoluteAccuracy, final Method method) {
            super(relativeAccuracy, absoluteAccuracy, method);
        }

        ConcreteSecantSolver(final double relativeAccuracy, final double absoluteAccuracy,
                             final double functionValueAccuracy, final Method method) {
            super(relativeAccuracy, absoluteAccuracy, functionValueAccuracy, method);
        }
    }

    // Tests exact root located at the minimum bound
    @Test
    public void testSolve_rootAtMin_returnsMin() {
        BaseSecantSolver solver = new ConcreteSecantSolver(1e-6, BaseSecantSolver.Method.ILLINOIS);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x - 1.0;
            }
        };
        double root = solver.solve(100, f, 1.0, 5.0, AllowedSolution.ANY_SIDE);
        assertEquals(1.0, root, 1e-15);
    }

    // Tests exact root located at the maximum bound
    @Test
    public void testSolve_rootAtMax_returnsMax() {
        BaseSecantSolver solver = new ConcreteSecantSolver(1e-6, BaseSecantSolver.Method.ILLINOIS);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x - 5.0;
            }
        };
        double root = solver.solve(100, f, 1.0, 5.0, AllowedSolution.ANY_SIDE);
        assertEquals(5.0, root, 1e-15);
    }

    // Tests non-bracketing interval throwing exception
    @Test(expected = NoBracketingException.class)
    public void testSolve_noBracketing_throwsException() {
        BaseSecantSolver solver = new ConcreteSecantSolver(1e-6, BaseSecantSolver.Method.ILLINOIS);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x + 1.0;
            }
        };
        solver.solve(100, f, 1.0, 5.0, AllowedSolution.ANY_SIDE);
    }

    // Tests exact root hit during intermediate secant approximation step
    @Test
    public void testSolve_exactIntermediateRoot_returnsRoot() {
        BaseSecantSolver solver = new ConcreteSecantSolver(1e-6, BaseSecantSolver.Method.ILLINOIS);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return 2.0 * x - 4.0;
            }
        };
        double root = solver.solve(100, f, 0.0, 4.0, AllowedSolution.ANY_SIDE);
        assertEquals(2.0, root, 1e-15);
    }

    // Tests Illinois method convergence on standard function
    @Test
    public void testSolve_illinoisMethod_convergesCorrectly() {
        BaseSecantSolver solver = new ConcreteSecantSolver(1e-6, BaseSecantSolver.Method.ILLINOIS);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return FastMath.sin(x);
            }
        };
        double root = solver.solve(100, f, 3.0, 4.0);
        assertEquals(FastMath.PI, root, 1e-6);
    }

    // Tests Pegasus method convergence on standard function
    @Test
    public void testSolve_pegasusMethod_convergesCorrectly() {
        BaseSecantSolver solver = new ConcreteSecantSolver(1e-6, BaseSecantSolver.Method.PEGASUS);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return FastMath.sin(x);
            }
        };
        double root = solver.solve(100, f, 3.0, 4.0);
        assertEquals(FastMath.PI, root, 1e-6);
    }

    // Tests Regula Falsi method convergence on standard function
    @Test
    public void testSolve_regulaFalsiMethod_convergesCorrectly() {
        BaseSecantSolver solver = new ConcreteSecantSolver(1e-6, BaseSecantSolver.Method.REGULA_FALSI);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x - 2.0;
            }
        };
        double root = solver.solve(100, f, 1.0, 2.0);
        assertEquals(FastMath.sqrt(2.0), root, 1e-6);
    }

    // Tests 3-parameter constructor and solve with startValue
    @Test
    public void testSolve_customAccuraciesAndStartValue_converges() {
        BaseSecantSolver solver = new ConcreteSecantSolver(1e-10, 1e-8, 1e-12, BaseSecantSolver.Method.ILLINOIS);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return FastMath.exp(x) - 3.0;
            }
        };
        double root = solver.solve(100, f, 0.0, 2.0, 1.0);
        assertEquals(FastMath.log(3.0), root, 1e-8);
    }

    // Tests 2-parameter constructor
    @Test
    public void testSolve_twoAccuracyParameters_converges() {
        BaseSecantSolver solver = new ConcreteSecantSolver(1e-10, 1e-8, BaseSecantSolver.Method.PEGASUS);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x * x - 8.0;
            }
        };
        double root = solver.solve(100, f, 1.0, 3.0, 2.5, AllowedSolution.ANY_SIDE);
        assertEquals(2.0, root, 1e-8);
    }

    // Tests AllowedSolution.LEFT_SIDE returning left bracket
    @Test
    public void testSolve_allowedSolutionLeftSide_returnsLeftBracket() {
        BaseSecantSolver solver = new ConcreteSecantSolver(1e-6, BaseSecantSolver.Method.ILLINOIS);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x - 2.0;
            }
        };
        double root = solver.solve(100, f, 1.0, 2.0, AllowedSolution.LEFT_SIDE);
        assertTrue(root <= FastMath.sqrt(2.0));
    }

    // Tests AllowedSolution.RIGHT_SIDE returning right bracket
    @Test
    public void testSolve_allowedSolutionRightSide_returnsRightBracket() {
        BaseSecantSolver solver = new ConcreteSecantSolver(1e-6, BaseSecantSolver.Method.ILLINOIS);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x - 2.0;
            }
        };
        double root = solver.solve(100, f, 1.0, 2.0, AllowedSolution.RIGHT_SIDE);
        assertTrue(root >= FastMath.sqrt(2.0));
    }

    // Tests AllowedSolution.BELOW_SIDE returning value with non-positive evaluation
    @Test
    public void testSolve_allowedSolutionBelowSide_returnsNegativeOrZeroValue() {
        BaseSecantSolver solver = new ConcreteSecantSolver(1e-6, BaseSecantSolver.Method.ILLINOIS);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x - 2.0;
            }
        };
        double root = solver.solve(100, f, 1.0, 2.0, AllowedSolution.BELOW_SIDE);
        assertTrue(f.value(root) <= 0.0);
    }

    // Tests AllowedSolution.ABOVE_SIDE returning value with non-negative evaluation
    @Test
    public void testSolve_allowedSolutionAboveSide_returnsPositiveOrZeroValue() {
        BaseSecantSolver solver = new ConcreteSecantSolver(1e-6, BaseSecantSolver.Method.ILLINOIS);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x - 2.0;
            }
        };
        double root = solver.solve(100, f, 1.0, 2.0, AllowedSolution.ABOVE_SIDE);
        assertTrue(f.value(root) >= 0.0);
    }

    // Tests Regula Falsi stuck condition leading to ConvergenceException
    @Test(expected = ConvergenceException.class)
    public void testSolve_regulaFalsiStuck_throwsConvergenceException() {
        BaseSecantSolver solver = new ConcreteSecantSolver(1e-14, 1e-15, BaseSecantSolver.Method.REGULA_FALSI);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return FastMath.exp(x) - FastMath.pow(Math.E, 1.0);
            }
        };
        // Should detect convergence failure / algorithm stuck on difficult function
        solver.solve(20, f, 0.0, 5.0, AllowedSolution.ANY_SIDE);
    }

    // Tests function value accuracy threshold returning solution early
    @Test
    public void testSolve_withinFunctionValueAccuracy_returnsRoot() {
        BaseSecantSolver solver = new ConcreteSecantSolver(1e-14, 1e-14, 1e-2, BaseSecantSolver.Method.ILLINOIS);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x - 0.5;
            }
        };
        double root = solver.solve(100, f, 0.0, 1.0, AllowedSolution.ANY_SIDE);
        assertEquals(0.5, root, 1e-2);
    }
}