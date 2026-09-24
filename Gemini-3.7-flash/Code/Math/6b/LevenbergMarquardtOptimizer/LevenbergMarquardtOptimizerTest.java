package org.apache.commons.math3.optim.nonlinear.vector.jacobian;

import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.apache.commons.math3.exception.ConvergenceException;
import org.apache.commons.math3.exception.MathUnsupportedOperationException;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.exception.TooManyIterationsException;
import org.apache.commons.math3.linear.DiagonalMatrix;
import org.apache.commons.math3.optim.ConvergenceChecker;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.MaxIter;
import org.apache.commons.math3.optim.PointVectorValuePair;
import org.apache.commons.math3.optim.SimplePointChecker;
import org.apache.commons.math3.optim.SimpleVectorValueChecker;
import org.apache.commons.math3.optim.nonlinear.vector.ModelFunction;
import org.apache.commons.math3.optim.nonlinear.vector.ModelFunctionJacobian;
import org.apache.commons.math3.optim.nonlinear.vector.SimpleBounds;
import org.apache.commons.math3.optim.nonlinear.vector.Target;
import org.apache.commons.math3.optim.nonlinear.vector.Weight;
import org.junit.Assert;
import org.junit.Test;

public class LevenbergMarquardtOptimizerTest {

    // Tests default constructor and normal quadratic fitting
    @Test
    public void testOptimize_linearProblem_convergesToExactSolution() {
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();

        LinearProblem problem = new LinearProblem(
            new double[][] { { 1, 2 }, { 2, 3 }, { 3, 4 } },
            new double[] { 5, 8, 11 }
        );

        PointVectorValuePair optimum = optimizer.optimize(
            new MaxEval(100),
            new MaxIter(100),
            new Target(problem.getTarget()),
            new Weight(new double[] { 1, 1, 1 }),
            new InitialGuess(new double[] { 0, 0 }),
            problem.getModelFunction(),
            problem.getModelFunctionJacobian()
        );

        Assert.assertEquals(1.0, optimum.getPoint()[0], 1e-5);
        Assert.assertEquals(2.0, optimum.getPoint()[1], 1e-5);
        Assert.assertTrue(optimizer.getIterations() > 0);
        Assert.assertTrue(optimizer.getEvaluations() > 0);
    }

    // Tests constructor with custom convergence checker
    @Test
    public void testOptimize_customChecker_convergesProperly() {
        ConvergenceChecker<PointVectorValuePair> checker =
            new SimplePointChecker<PointVectorValuePair>(1e-6, 1e-6);
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer(checker);

        LinearProblem problem = new LinearProblem(
            new double[][] { { 2, 0 }, { 0, 3 } },
            new double[] { 4, 9 }
        );

        PointVectorValuePair optimum = optimizer.optimize(
            new MaxEval(100),
            new MaxIter(100),
            new Target(problem.getTarget()),
            new Weight(new double[] { 1, 1 }),
            new InitialGuess(new double[] { 1, 1 }),
            problem.getModelFunction(),
            problem.getModelFunctionJacobian()
        );

        Assert.assertEquals(2.0, optimum.getPoint()[0], 1e-5);
        Assert.assertEquals(3.0, optimum.getPoint()[1], 1e-5);
    }

    // Tests constructor specifying 3 tolerances
    @Test
    public void testOptimize_threeTolerancesConstructor_converges() {
        LevenbergMarquardtOptimizer optimizer =
            new LevenbergMarquardtOptimizer(1e-10, 1e-10, 1e-10);

        LinearProblem problem = new LinearProblem(
            new double[][] { { 1, 1 }, { 2, -1 } },
            new double[] { 3, 0 }
        );

        PointVectorValuePair optimum = optimizer.optimize(
            new MaxEval(100),
            new MaxIter(100),
            new Target(problem.getTarget()),
            new Weight(new double[] { 1, 1 }),
            new InitialGuess(new double[] { 0, 0 }),
            problem.getModelFunction(),
            problem.getModelFunctionJacobian()
        );

        Assert.assertEquals(1.0, optimum.getPoint()[0], 1e-5);
        Assert.assertEquals(2.0, optimum.getPoint()[1], 1e-5);
    }

    // Tests full parameter constructor with checker and custom settings
    @Test
    public void testOptimize_fullConstructorWithChecker_converges() {
        ConvergenceChecker<PointVectorValuePair> checker =
            new SimpleVectorValueChecker(1e-6, 1e-6);
        LevenbergMarquardtOptimizer optimizer =
            new LevenbergMarquardtOptimizer(100.0, checker, 1e-10, 1e-10, 1e-10, 1e-14);

        LinearProblem problem = new LinearProblem(
            new double[][] { { 1, 0 }, { 0, 1 } },
            new double[] { 5, 7 }
        );

        PointVectorValuePair optimum = optimizer.optimize(
            new MaxEval(100),
            new MaxIter(100),
            new Target(problem.getTarget()),
            new Weight(new double[] { 1, 1 }),
            new InitialGuess(new double[] { 0, 0 }),
            problem.getModelFunction(),
            problem.getModelFunctionJacobian()
        );

        Assert.assertEquals(5.0, optimum.getPoint()[0], 1e-5);
        Assert.assertEquals(7.0, optimum.getPoint()[1], 1e-5);
    }

    // Tests 5-parameters constructor without checker
    @Test
    public void testOptimize_fiveParametersConstructor_converges() {
        LevenbergMarquardtOptimizer optimizer =
            new LevenbergMarquardtOptimizer(50.0, 1e-8, 1e-8, 1e-8, 1e-12);

        LinearProblem problem = new LinearProblem(
            new double[][] { { 3, 1 }, { 1, 2 } },
            new double[] { 9, 8 }
        );

        PointVectorValuePair optimum = optimizer.optimize(
            new MaxEval(100),
            new MaxIter(100),
            new Target(problem.getTarget()),
            new Weight(new double[] { 1, 1 }),
            new InitialGuess(new double[] { 0, 0 }),
            problem.getModelFunction(),
            problem.getModelFunctionJacobian()
        );

        Assert.assertEquals(2.0, optimum.getPoint()[0], 1e-5);
        Assert.assertEquals(3.0, optimum.getPoint()[1], 1e-5);
    }

    // Tests unsupported bounds throwing MathUnsupportedOperationException
    @Test(expected = MathUnsupportedOperationException.class)
    public void testOptimize_withBounds_throwsMathUnsupportedOperationException() {
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();

        LinearProblem problem = new LinearProblem(
            new double[][] { { 1, 0 }, { 0, 1 } },
            new double[] { 1, 1 }
        );

        optimizer.optimize(
            new MaxEval(100),
            new MaxIter(100),
            new Target(problem.getTarget()),
            new Weight(new double[] { 1, 1 }),
            new InitialGuess(new double[] { 0, 0 }),
            new SimpleBounds(new double[] { -1, -1 }, new double[] { 1, 1 }),
            problem.getModelFunction(),
            problem.getModelFunctionJacobian()
        );
    }

    // Tests iteration counter correctly increments and respects max iterations (Defects4J Math-6)
    @Test(expected = TooManyIterationsException.class)
    public void testOptimize_exceedMaxIterations_throwsTooManyIterationsException() {
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();

        CircleProblem problem = new CircleProblem();
        problem.addPoint(1, 0);
        problem.addPoint(0, 1);
        problem.addPoint(-1, 0);
        problem.addPoint(0, -1);

        optimizer.optimize(
            new MaxEval(1000),
            new MaxIter(1),
            new Target(problem.getTarget()),
            new Weight(new double[] { 1, 1, 1, 1 }),
            new InitialGuess(new double[] { 10.0, 10.0, 0.5 }),
            problem.getModelFunction(),
            problem.getModelFunctionJacobian()
        );
    }

    // Tests evaluation counter correctly increments and respects max evaluations
    @Test(expected = TooManyEvaluationsException.class)
    public void testOptimize_exceedMaxEvaluations_throwsTooManyEvaluationsException() {
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();

        CircleProblem problem = new CircleProblem();
        problem.addPoint(1, 0);
        problem.addPoint(0, 1);
        problem.addPoint(-1, 0);
        problem.addPoint(0, -1);

        optimizer.optimize(
            new MaxEval(1),
            new MaxIter(100),
            new Target(problem.getTarget()),
            new Weight(new double[] { 1, 1, 1, 1 }),
            new InitialGuess(new double[] { 10.0, 10.0, 0.5 }),
            problem.getModelFunction(),
            problem.getModelFunctionJacobian()
        );
    }

    // Tests non-linear circle fitting convergence
    @Test
    public void testOptimize_circleFitting_findsCenterAndRadius() {
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();

        CircleProblem problem = new CircleProblem();
        problem.addPoint(1, 0);
        problem.addPoint(0, 1);
        problem.addPoint(-1, 0);
        problem.addPoint(0, -1);

        PointVectorValuePair optimum = optimizer.optimize(
            new MaxEval(100),
            new MaxIter(100),
            new Target(problem.getTarget()),
            new Weight(new double[] { 1, 1, 1, 1 }),
            new InitialGuess(new double[] { 0.1, 0.1, 0.8 }),
            problem.getModelFunction(),
            problem.getModelFunctionJacobian()
        );

        Assert.assertEquals(0.0, optimum.getPoint()[0], 1e-3);
        Assert.assertEquals(0.0, optimum.getPoint()[1], 1e-3);
        Assert.assertEquals(1.0, optimum.getPoint()[2], 1e-3);
    }

    // Tests start point already at minimum (orthogonality convergence)
    @Test
    public void testOptimize_alreadyAtOptimum_convergesImmediately() {
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();

        LinearProblem problem = new LinearProblem(
            new double[][] { { 1, 0 }, { 0, 1 } },
            new double[] { 2, 3 }
        );

        PointVectorValuePair optimum = optimizer.optimize(
            new MaxEval(100),
            new MaxIter(100),
            new Target(problem.getTarget()),
            new Weight(new double[] { 1, 1 }),
            new InitialGuess(new double[] { 2, 3 }),
            problem.getModelFunction(),
            problem.getModelFunctionJacobian()
        );

        Assert.assertEquals(2.0, optimum.getPoint()[0], 1e-8);
        Assert.assertEquals(3.0, optimum.getPoint()[1], 1e-8);
    }

    // Tests over-determined system
    @Test
    public void testOptimize_overDeterminedSystem_findsLeastSquaresSolution() {
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();

        LinearProblem problem = new LinearProblem(
            new double[][] { { 1 }, { 1 }, { 1 } },
            new double[] { 1.0, 2.0, 3.0 }
        );

        PointVectorValuePair optimum = optimizer.optimize(
            new MaxEval(100),
            new MaxIter(100),
            new Target(problem.getTarget()),
            new Weight(new double[] { 1, 1, 1 }),
            new InitialGuess(new double[] { 0 }),
            problem.getModelFunction(),
            problem.getModelFunctionJacobian()
        );

        Assert.assertEquals(2.0, optimum.getPoint()[0], 1e-5);
    }

    // Tests QR decomposition with NaN Jacobian throwing ConvergenceException
    @Test(expected = ConvergenceException.class)
    public void testOptimize_nanInJacobian_throwsConvergenceException() {
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();

        ModelFunction model = new ModelFunction(new MultivariateVectorFunction() {
            public double[] value(double[] point) {
                return new double[] { point[0] };
            }
        });

        ModelFunctionJacobian jacobian = new ModelFunctionJacobian(new MultivariateMatrixFunction() {
            public double[][] value(double[] point) {
                return new double[][] { { Double.NaN } };
            }
        });

        optimizer.optimize(
            new MaxEval(100),
            new MaxIter(100),
            new Target(new double[] { 1.0 }),
            new Weight(new double[] { 1.0 }),
            new InitialGuess(new double[] { 0.0 }),
            model,
            jacobian
        );
    }

    // Tests non-diagonal weight matrix operates correctly
    @Test
    public void testOptimize_diagonalWeightMatrix_convergesCorrectly() {
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();

        LinearProblem problem = new LinearProblem(
            new double[][] { { 1, 0 }, { 0, 1 } },
            new double[] { 2, 4 }
        );

        PointVectorValuePair optimum = optimizer.optimize(
            new MaxEval(100),
            new MaxIter(100),
            new Target(problem.getTarget()),
            new Weight(new DiagonalMatrix(new double[] { 4.0, 1.0 })),
            new InitialGuess(new double[] { 0, 0 }),
            problem.getModelFunction(),
            problem.getModelFunctionJacobian()
        );

        Assert.assertEquals(2.0, optimum.getPoint()[0], 1e-5);
        Assert.assertEquals(4.0, optimum.getPoint()[1], 1e-5);
    }

    // Helper class for linear least-squares problems
    private static class LinearProblem {
        private final double[][] matrix;
        private final double[] target;

        public LinearProblem(double[][] matrix, double[] target) {
            this.matrix = matrix;
            this.target = target;
        }

        public double[] getTarget() {
            return target;
        }

        public ModelFunction getModelFunction() {
            return new ModelFunction(new MultivariateVectorFunction() {
                public double[] value(double[] params) {
                    double[] result = new double[matrix.length];
                    for (int i = 0; i < matrix.length; ++i) {
                        double sum = 0;
                        for (int j = 0; j < matrix[i].length; ++j) {
                            sum += matrix[i][j] * params[j];
                        }
                        result[i] = sum;
                    }
                    return result;
                }
            });
        }

        public ModelFunctionJacobian getModelFunctionJacobian() {
            return new ModelFunctionJacobian(new MultivariateMatrixFunction() {
                public double[][] value(double[] params) {
                    return matrix;
                }
            });
        }
    }

    // Helper class for 2D circle fitting problem: (x-cx)^2 + (y-cy)^2 = r^2
    private static class CircleProblem {
        private java.util.List<double[]> points = new java.util.ArrayList<double[]>();

        public void addPoint(double px, double py) {
            points.add(new double[] { px, py });
        }

        public double[] getTarget() {
            return new double[points.size()];
        }

        public ModelFunction getModelFunction() {
            return new ModelFunction(new MultivariateVectorFunction() {
                public double[] value(double[] params) {
                    double cx = params[0];
                    double cy = params[1];
                    double r = params[2];
                    double[] values = new double[points.size()];
                    for (int i = 0; i < points.size(); ++i) {
                        double[] p = points.get(i);
                        double dx = p[0] - cx;
                        double dy = p[1] - cy;
                        values[i] = Math.sqrt(dx * dx + dy * dy) - r;
                    }
                    return values;
                }
            });
        }

        public ModelFunctionJacobian getModelFunctionJacobian() {
            return new ModelFunctionJacobian(new MultivariateMatrixFunction() {
                public double[][] value(double[] params) {
                    double cx = params[0];
                    double cy = params[1];
                    double[][] jacobian = new double[points.size()][3];
                    for (int i = 0; i < points.size(); ++i) {
                        double[] p = points.get(i);
                        double dx = p[0] - cx;
                        double dy = p[1] - cy;
                        double d = Math.sqrt(dx * dx + dy * dy);
                        if (d == 0) {
                            jacobian[i][0] = 0;
                            jacobian[i][1] = 0;
                        } else {
                            jacobian[i][0] = -dx / d;
                            jacobian[i][1] = -dy / d;
                        }
                        jacobian[i][2] = -1.0;
                    }
                    return jacobian;
                }
            });
        }
    }
}