package org.apache.commons.math3.optim.nonlinear.vector.jacobian;

import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.apache.commons.math3.exception.ConvergenceException;
import org.apache.commons.math3.exception.MathUnsupportedOperationException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointVectorValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import org.apache.commons.math3.optim.SimplePointChecker;
import org.apache.commons.math3.optim.SimpleVectorValueChecker;
import org.apache.commons.math3.optim.nonlinear.vector.ModelFunction;
import org.apache.commons.math3.optim.nonlinear.vector.ModelFunctionJacobian;
import org.apache.commons.math3.optim.nonlinear.vector.Target;
import org.apache.commons.math3.optim.nonlinear.vector.Weight;
import org.junit.Assert;
import org.junit.Test;

public class GaussNewtonOptimizerTest {

    // Tests that passing bounds throws MathUnsupportedOperationException
    @Test(expected = MathUnsupportedOperationException.class)
    public void testOptimize_withBounds_throwsMathUnsupportedOperationException() {
        GaussNewtonOptimizer optimizer = new GaussNewtonOptimizer(
                new SimplePointChecker<PointVectorValuePair>(1e-6, 1e-6));

        optimizer.optimize(
                new MaxEval(100),
                new Target(new double[] { 1.0 }),
                new Weight(new double[] { 1.0 }),
                new InitialGuess(new double[] { 0.0 }),
                new SimpleBounds(new double[] { -1.0 }, new double[] { 1.0 }),
                new ModelFunction(new MultivariateVectorFunction() {
                    public double[] value(double[] point) {
                        return new double[] { point[0] };
                    }
                }),
                new ModelFunctionJacobian(new MultivariateMatrixFunction() {
                    public double[][] value(double[] point) {
                        return new double[][] { { 1.0 } };
                    }
                })
        );
    }

    // Tests that null convergence checker throws NullArgumentException
    @Test(expected = NullArgumentException.class)
    public void testOptimize_nullChecker_throwsNullArgumentException() {
        GaussNewtonOptimizer optimizer = new GaussNewtonOptimizer(null);

        optimizer.optimize(
                new MaxEval(100),
                new Target(new double[] { 1.0 }),
                new Weight(new double[] { 1.0 }),
                new InitialGuess(new double[] { 0.0 }),
                new ModelFunction(new MultivariateVectorFunction() {
                    public double[] value(double[] point) {
                        return new double[] { point[0] };
                    }
                }),
                new ModelFunctionJacobian(new MultivariateMatrixFunction() {
                    public double[][] value(double[] point) {
                        return new double[][] { { 1.0 } };
                    }
                })
        );
    }

    // Tests standard linear optimization using default LU decomposition
    @Test
    public void testOptimize_linearProblemLU_convergesToExactSolution() {
        GaussNewtonOptimizer optimizer = new GaussNewtonOptimizer(
                new SimpleVectorValueChecker(1e-10, 1e-10));

        // Model: f0(x, y) = 2*x + y, f1(x, y) = x - y
        MultivariateVectorFunction model = new MultivariateVectorFunction() {
            public double[] value(double[] point) {
                return new double[] {
                    2.0 * point[0] + point[1],
                    point[0] - point[1]
                };
            }
        };

        MultivariateMatrixFunction jacobian = new MultivariateMatrixFunction() {
            public double[][] value(double[] point) {
                return new double[][] {
                    { 2.0, 1.0 },
                    { 1.0, -1.0 }
                };
            }
        };

        PointVectorValuePair optimum = optimizer.optimize(
                new MaxEval(100),
                new Target(new double[] { 5.0, 1.0 }),
                new Weight(new double[] { 1.0, 1.0 }),
                new InitialGuess(new double[] { 0.0, 0.0 }),
                new ModelFunction(model),
                new ModelFunctionJacobian(jacobian)
        );

        Assert.assertNotNull(optimum);
        Assert.assertEquals(2.0, optimum.getPoint()[0], 1e-5);
        Assert.assertEquals(1.0, optimum.getPoint()[1], 1e-5);
        Assert.assertEquals(5.0, optimum.getValue()[0], 1e-5);
        Assert.assertEquals(1.0, optimum.getValue()[1], 1e-5);
        Assert.assertEquals(0.0, optimizer.getCost(), 1e-5);
    }

    // Tests linear optimization using QR decomposition solver
    @Test
    public void testOptimize_linearProblemQR_convergesToExactSolution() {
        GaussNewtonOptimizer optimizer = new GaussNewtonOptimizer(
                false,
                new SimpleVectorValueChecker(1e-10, 1e-10));

        MultivariateVectorFunction model = new MultivariateVectorFunction() {
            public double[] value(double[] point) {
                return new double[] {
                    3.0 * point[0] + 4.0 * point[1],
                    point[0] - 2.0 * point[1]
                };
            }
        };

        MultivariateMatrixFunction jacobian = new MultivariateMatrixFunction() {
            public double[][] value(double[] point) {
                return new double[][] {
                    { 3.0, 4.0 },
                    { 1.0, -2.0 }
                };
            }
        };

        PointVectorValuePair optimum = optimizer.optimize(
                new MaxEval(100),
                new Target(new double[] { 11.0, -3.0 }),
                new Weight(new double[] { 1.0, 1.0 }),
                new InitialGuess(new double[] { 0.0, 0.0 }),
                new ModelFunction(model),
                new ModelFunctionJacobian(jacobian)
        );

        Assert.assertNotNull(optimum);
        Assert.assertEquals(1.0, optimum.getPoint()[0], 1e-5);
        Assert.assertEquals(2.0, optimum.getPoint()[1], 1e-5);
        Assert.assertEquals(0.0, optimizer.getCost(), 1e-5);
    }

    // Tests non-linear problem convergence
    @Test
    public void testOptimize_nonLinearProblem_convergesCorrectly() {
        GaussNewtonOptimizer optimizer = new GaussNewtonOptimizer(
                new SimplePointChecker<PointVectorValuePair>(1e-6, 1e-6));

        // Model: f0(x) = x^2, f1(x) = x^3
        MultivariateVectorFunction model = new MultivariateVectorFunction() {
            public double[] value(double[] point) {
                return new double[] {
                    point[0] * point[0],
                    point[0] * point[0] * point[0]
                };
            }
        };

        MultivariateMatrixFunction jacobian = new MultivariateMatrixFunction() {
            public double[][] value(double[] point) {
                return new double[][] {
                    { 2.0 * point[0] },
                    { 3.0 * point[0] * point[0] }
                };
            }
        };

        PointVectorValuePair optimum = optimizer.optimize(
                new MaxEval(100),
                new Target(new double[] { 4.0, 8.0 }),
                new Weight(new double[] { 1.0, 1.0 }),
                new InitialGuess(new double[] { 1.5 }),
                new ModelFunction(model),
                new ModelFunctionJacobian(jacobian)
        );

        Assert.assertNotNull(optimum);
        Assert.assertEquals(2.0, optimum.getPoint()[0], 1e-4);
        Assert.assertTrue(optimizer.getCost() < 1e-4);
    }

    // Tests singular matrix leading to ConvergenceException
    @Test(expected = ConvergenceException.class)
    public void testOptimize_singularJacobian_throwsConvergenceException() {
        GaussNewtonOptimizer optimizer = new GaussNewtonOptimizer(
                new SimpleVectorValueChecker(1e-6, 1e-6));

        // Constant function yielding a zero Jacobian (singular normal equation)
        MultivariateVectorFunction model = new MultivariateVectorFunction() {
            public double[] value(double[] point) {
                return new double[] { 0.0, 0.0 };
            }
        };

        MultivariateMatrixFunction jacobian = new MultivariateMatrixFunction() {
            public double[][] value(double[] point) {
                return new double[][] {
                    { 0.0, 0.0 },
                    { 0.0, 0.0 }
                };
            }
        };

        optimizer.optimize(
                new MaxEval(100),
                new Target(new double[] { 1.0, 1.0 }),
                new Weight(new double[] { 1.0, 1.0 }),
                new InitialGuess(new double[] { 0.0, 0.0 }),
                new ModelFunction(model),
                new ModelFunctionJacobian(jacobian)
        );
    }

    // Tests weighted least squares where weights differ between residuals
    @Test
    public void testOptimize_nonUniformWeights_findsWeightedLeastSquaresSolution() {
        GaussNewtonOptimizer optimizer = new GaussNewtonOptimizer(
                new SimpleVectorValueChecker(1e-10, 1e-10));

        // Overdetermined system: x = 1 (weight 1.0), x = 3 (weight 9.0)
        MultivariateVectorFunction model = new MultivariateVectorFunction() {
            public double[] value(double[] point) {
                return new double[] { point[0], point[0] };
            }
        };

        MultivariateMatrixFunction jacobian = new MultivariateMatrixFunction() {
            public double[][] value(double[] point) {
                return new double[][] { { 1.0 }, { 1.0 } };
            }
        };

        PointVectorValuePair optimum = optimizer.optimize(
                new MaxEval(100),
                new Target(new double[] { 1.0, 3.0 }),
                new Weight(new double[] { 1.0, 9.0 }),
                new InitialGuess(new double[] { 0.0 }),
                new ModelFunction(model),
                new ModelFunctionJacobian(jacobian)
        );

        Assert.assertNotNull(optimum);
        // Weighted average: (1*1 + 9*3) / (1 + 9) = 28 / 10 = 2.8
        Assert.assertEquals(2.8, optimum.getPoint()[0], 1e-5);
    }
}