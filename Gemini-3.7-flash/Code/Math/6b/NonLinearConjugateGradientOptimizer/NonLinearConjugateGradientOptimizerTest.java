package org.apache.commons.math3.optim.nonlinear.scalar.gradient;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.apache.commons.math3.analysis.solvers.BrentSolver;
import org.apache.commons.math3.exception.MathUnsupportedOperationException;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.MaxIter;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import org.apache.commons.math3.optim.SimpleValueChecker;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunctionGradient;
import org.junit.Assert;
import org.junit.Test;

public class NonLinearConjugateGradientOptimizerTest {

    // Tests minimization using Fletcher-Reeves formula on a simple quadratic 2D function
    @Test
    public void testOptimize_fletcherReevesMinimization_findsMinimum() {
        LinearProblem problem = new LinearProblem();
        NonLinearConjugateGradientOptimizer optimizer =
            new NonLinearConjugateGradientOptimizer(
                NonLinearConjugateGradientOptimizer.Formula.FLETCHER_REEVES,
                new SimpleValueChecker(1e-10, 1e-10));

        PointValuePair optimum = optimizer.optimize(
            new MaxEval(200),
            new MaxIter(100),
            new ObjectiveFunction(problem.getObjectiveFunction()),
            new ObjectiveFunctionGradient(problem.getGradientFunction()),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] { 10.0, -10.0 })
        );

        Assert.assertEquals(3.0, optimum.getPoint()[0], 1e-4);
        Assert.assertEquals(-2.0, optimum.getPoint()[1], 1e-4);
        Assert.assertEquals(0.0, optimum.getValue(), 1e-4);
        Assert.assertTrue(optimizer.getEvaluations() > 0);
        Assert.assertTrue(optimizer.getIterations() > 0);
    }

    // Tests minimization using Polak-Ribiere formula on a quadratic function
    @Test
    public void testOptimize_polakRibiereMinimization_findsMinimum() {
        LinearProblem problem = new LinearProblem();
        NonLinearConjugateGradientOptimizer optimizer =
            new NonLinearConjugateGradientOptimizer(
                NonLinearConjugateGradientOptimizer.Formula.POLAK_RIBIERE,
                new SimpleValueChecker(1e-10, 1e-10));

        PointValuePair optimum = optimizer.optimize(
            new MaxEval(200),
            new MaxIter(100),
            new ObjectiveFunction(problem.getObjectiveFunction()),
            new ObjectiveFunctionGradient(problem.getGradientFunction()),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] { -5.0, 5.0 })
        );

        Assert.assertEquals(3.0, optimum.getPoint()[0], 1e-4);
        Assert.assertEquals(-2.0, optimum.getPoint()[1], 1e-4);
        Assert.assertEquals(0.0, optimum.getValue(), 1e-4);
    }

    // Tests maximization on an inverted parabola
    @Test
    public void testOptimize_maximization_findsMaximum() {
        MultivariateFunction function = new MultivariateFunction() {
            public double value(double[] point) {
                double x = point[0] - 1.0;
                double y = point[1] - 4.0;
                return -(x * x + y * y) + 10.0;
            }
        };
        MultivariateVectorFunction gradient = new MultivariateVectorFunction() {
            public double[] value(double[] point) {
                return new double[] {
                    -2.0 * (point[0] - 1.0),
                    -2.0 * (point[1] - 4.0)
                };
            }
        };

        NonLinearConjugateGradientOptimizer optimizer =
            new NonLinearConjugateGradientOptimizer(
                NonLinearConjugateGradientOptimizer.Formula.FLETCHER_REEVES,
                new SimpleValueChecker(1e-10, 1e-10));

        PointValuePair optimum = optimizer.optimize(
            new MaxEval(100),
            new MaxIter(50),
            new ObjectiveFunction(function),
            new ObjectiveFunctionGradient(gradient),
            GoalType.MAXIMIZE,
            new InitialGuess(new double[] { 0.0, 0.0 })
        );

        Assert.assertEquals(1.0, optimum.getPoint()[0], 1e-4);
        Assert.assertEquals(4.0, optimum.getPoint()[1], 1e-4);
        Assert.assertEquals(10.0, optimum.getValue(), 1e-4);
    }

    // Tests passing custom BracketingStep optimization data
    @Test
    public void testOptimize_withBracketingStep_convergesCorrectly() {
        LinearProblem problem = new LinearProblem();
        NonLinearConjugateGradientOptimizer optimizer =
            new NonLinearConjugateGradientOptimizer(
                NonLinearConjugateGradientOptimizer.Formula.POLAK_RIBIERE,
                new SimpleValueChecker(1e-10, 1e-10),
                new BrentSolver(1e-10, 1e-10));

        PointValuePair optimum = optimizer.optimize(
            new MaxEval(200),
            new MaxIter(100),
            new ObjectiveFunction(problem.getObjectiveFunction()),
            new ObjectiveFunctionGradient(problem.getGradientFunction()),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] { 0.0, 0.0 }),
            new NonLinearConjugateGradientOptimizer.BracketingStep(0.5)
        );

        Assert.assertEquals(3.0, optimum.getPoint()[0], 1e-4);
        Assert.assertEquals(-2.0, optimum.getPoint()[1], 1e-4);
    }

    // Tests getter and constructor of BracketingStep
    @Test
    public void testBracketingStep_getter_returnsProvidedValue() {
        NonLinearConjugateGradientOptimizer.BracketingStep step =
            new NonLinearConjugateGradientOptimizer.BracketingStep(2.5);
        Assert.assertEquals(2.5, step.getBracketingStep(), 1e-15);
    }

    // Tests IdentityPreconditioner returns a cloned copy of vector
    @Test
    public void testIdentityPreconditioner_precondition_returnsClonedVector() {
        NonLinearConjugateGradientOptimizer.IdentityPreconditioner preconditioner =
            new NonLinearConjugateGradientOptimizer.IdentityPreconditioner();

        double[] point = new double[] { 1.0, 2.0 };
        double[] r = new double[] { 3.0, -4.0 };
        double[] result = preconditioner.precondition(point, r);

        Assert.assertNotSame(r, result);
        Assert.assertArrayEquals(r, result, 1e-15);
    }

    // Tests custom Preconditioner implementation in optimization
    @Test
    public void testOptimize_withCustomPreconditioner_findsMinimum() {
        LinearProblem problem = new LinearProblem();
        Preconditioner diagonalPreconditioner = new Preconditioner() {
            public double[] precondition(double[] point, double[] r) {
                double[] res = new double[r.length];
                res[0] = r[0] / 2.0;
                res[1] = r[1] / 3.0;
                return res;
            }
        };

        NonLinearConjugateGradientOptimizer optimizer =
            new NonLinearConjugateGradientOptimizer(
                NonLinearConjugateGradientOptimizer.Formula.POLAK_RIBIERE,
                new SimpleValueChecker(1e-10, 1e-10),
                new BrentSolver(),
                diagonalPreconditioner);

        PointValuePair optimum = optimizer.optimize(
            new MaxEval(200),
            new MaxIter(100),
            new ObjectiveFunction(problem.getObjectiveFunction()),
            new ObjectiveFunctionGradient(problem.getGradientFunction()),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] { 5.0, -5.0 })
        );

        Assert.assertEquals(3.0, optimum.getPoint()[0], 1e-4);
        Assert.assertEquals(-2.0, optimum.getPoint()[1], 1e-4);
    }

    // Tests that unsupported bounds optimization data throws exception
    @Test(expected = MathUnsupportedOperationException.class)
    public void testOptimize_withBounds_throwsMathUnsupportedOperationException() {
        LinearProblem problem = new LinearProblem();
        NonLinearConjugateGradientOptimizer optimizer =
            new NonLinearConjugateGradientOptimizer(
                NonLinearConjugateGradientOptimizer.Formula.FLETCHER_REEVES,
                new SimpleValueChecker(1e-10, 1e-10));

        optimizer.optimize(
            new MaxEval(100),
            new MaxIter(100),
            new ObjectiveFunction(problem.getObjectiveFunction()),
            new ObjectiveFunctionGradient(problem.getGradientFunction()),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] { 0.0, 0.0 }),
            new SimpleBounds(new double[] { -1.0, -1.0 }, new double[] { 1.0, 1.0 })
        );
    }

    // Tests that exceeding max evaluations throws TooManyEvaluationsException
    @Test(expected = TooManyEvaluationsException.class)
    public void testOptimize_exceedMaxEvaluations_throwsTooManyEvaluationsException() {
        LinearProblem problem = new LinearProblem();
        NonLinearConjugateGradientOptimizer optimizer =
            new NonLinearConjugateGradientOptimizer(
                NonLinearConjugateGradientOptimizer.Formula.FLETCHER_REEVES,
                new SimpleValueChecker(1e-15, 1e-15));

        optimizer.optimize(
            new MaxEval(2),
            new MaxIter(100),
            new ObjectiveFunction(problem.getObjectiveFunction()),
            new ObjectiveFunctionGradient(problem.getGradientFunction()),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] { 100.0, 100.0 })
        );
    }

    // Tests 1D optimization problem convergence
    @Test
    public void testOptimize_oneDimensionalProblem_findsOptimum() {
        MultivariateFunction function = new MultivariateFunction() {
            public double value(double[] point) {
                double diff = point[0] - 5.0;
                return diff * diff;
            }
        };
        MultivariateVectorFunction gradient = new MultivariateVectorFunction() {
            public double[] value(double[] point) {
                return new double[] { 2.0 * (point[0] - 5.0) };
            }
        };

        NonLinearConjugateGradientOptimizer optimizer =
            new NonLinearConjugateGradientOptimizer(
                NonLinearConjugateGradientOptimizer.Formula.POLAK_RIBIERE,
                new SimpleValueChecker(1e-10, 1e-10));

        PointValuePair optimum = optimizer.optimize(
            new MaxEval(100),
            new MaxIter(50),
            new ObjectiveFunction(function),
            new ObjectiveFunctionGradient(gradient),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] { 0.0 })
        );

        Assert.assertEquals(5.0, optimum.getPoint()[0], 1e-4);
        Assert.assertEquals(0.0, optimum.getValue(), 1e-4);
    }

    // Helper class representing f(x, y) = (x - 3)^2 + 2 * (y + 2)^2
    private static class LinearProblem {
        public MultivariateFunction getObjectiveFunction() {
            return new MultivariateFunction() {
                public double value(double[] point) {
                    double dx = point[0] - 3.0;
                    double dy = point[1] + 2.0;
                    return dx * dx + 2.0 * dy * dy;
                }
            };
        }

        public MultivariateVectorFunction getGradientFunction() {
            return new MultivariateVectorFunction() {
                public double[] value(double[] point) {
                    return new double[] {
                        2.0 * (point[0] - 3.0),
                        4.0 * (point[1] + 2.0)
                    };
                }
            };
        }
    }
}