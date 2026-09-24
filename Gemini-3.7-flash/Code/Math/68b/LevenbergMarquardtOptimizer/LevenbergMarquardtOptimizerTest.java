package org.apache.commons.math.optimization.general;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.DifferentiableMultivariateVectorialFunction;
import org.apache.commons.math.analysis.MultivariateMatrixFunction;
import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.optimization.SimpleVectorialValueChecker;
import org.apache.commons.math.optimization.VectorialPointValuePair;
import org.junit.Test;

import static org.junit.Assert.*;

public class LevenbergMarquardtOptimizerTest {

    // Helper class representing a 2D line fitting problem: y = a * x + b
    private static class LinearProblem implements DifferentiableMultivariateVectorialFunction {
        private final double[] x;
        private final double[] y;

        public LinearProblem(double[] x, double[] y) {
            this.x = x;
            this.y = y;
        }

        public double[] value(double[] point) {
            double a = point[0];
            double b = point[1];
            double[] values = new double[x.length];
            for (int i = 0; i < x.length; ++i) {
                values[i] = a * x[i] + b;
            }
            return values;
        }

        public MultivariateMatrixFunction jacobian() {
            return new MultivariateMatrixFunction() {
                public double[][] value(double[] point) {
                    double[][] jacobian = new double[x.length][2];
                    for (int i = 0; i < x.length; ++i) {
                        jacobian[i][0] = x[i];
                        jacobian[i][1] = 1.0;
                    }
                    return jacobian;
                }
            };
        }
    }

    // Helper class for circle fitting: (x - cx)^2 + (y - cy)^2 = r^2
    private static class CircleProblem implements DifferentiableMultivariateVectorialFunction {
        private final double[] x;
        private final double[] y;

        public CircleProblem(double[] x, double[] y) {
            this.x = x;
            this.y = y;
        }

        public double[] value(double[] point) {
            double cx = point[0];
            double cy = point[1];
            double r = point[2];
            double[] residuals = new double[x.length];
            for (int i = 0; i < x.length; ++i) {
                double dx = x[i] - cx;
                double dy = y[i] - cy;
                residuals[i] = Math.sqrt(dx * dx + dy * dy) - r;
            }
            return residuals;
        }

        public MultivariateMatrixFunction jacobian() {
            return new MultivariateMatrixFunction() {
                public double[][] value(double[] point) {
                    double cx = point[0];
                    double cy = point[1];
                    double[][] jacobian = new double[x.length][3];
                    for (int i = 0; i < x.length; ++i) {
                        double dx = x[i] - cx;
                        double dy = y[i] - cy;
                        double dist = Math.sqrt(dx * dx + dy * dy);
                        if (dist == 0) {
                            jacobian[i][0] = 0;
                            jacobian[i][1] = 0;
                        } else {
                            jacobian[i][0] = -dx / dist;
                            jacobian[i][1] = -dy / dist;
                        }
                        jacobian[i][2] = -1.0;
                    }
                    return jacobian;
                }
            };
        }
    }

    // Helper class for problem with NaN Jacobian
    private static class NanJacobianProblem implements DifferentiableMultivariateVectorialFunction {
        public double[] value(double[] point) {
            return new double[]{point[0] * point[0], point[1] * point[1]};
        }

        public MultivariateMatrixFunction jacobian() {
            return new MultivariateMatrixFunction() {
                public double[][] value(double[] point) {
                    return new double[][]{
                        {Double.NaN, 0.0},
                        {0.0, 2.0 * point[1]}
                    };
                }
            };
        }
    }

    // Helper class throwing exception on evaluation
    private static class ExceptionProblem implements DifferentiableMultivariateVectorialFunction {
        public double[] value(double[] point) throws FunctionEvaluationException {
            throw new FunctionEvaluationException(point, "Evaluation failed");
        }

        public MultivariateMatrixFunction jacobian() {
            return new MultivariateMatrixFunction() {
                public double[][] value(double[] point) {
                    return new double[][]{{1.0}};
                }
            };
        }
    }

    // Tests default constructor parameter values and simple linear optimization
    @Test
    public void testOptimize_simpleLinearProblem_convergesToExactSolution() throws Exception {
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        double[] x = {1.0, 2.0, 3.0, 4.0};
        double[] y = {3.0, 5.0, 7.0, 9.0}; // y = 2x + 1
        double[] weights = {1.0, 1.0, 1.0, 1.0};
        LinearProblem problem = new LinearProblem(x, y);

        VectorialPointValuePair optimum = optimizer.optimize(problem, y, weights, new double[]{0.0, 0.0});
        double[] point = optimum.getPointRef();

        assertEquals(2.0, point[0], 1.0e-5);
        assertEquals(1.0, point[1], 1.0e-5);
        assertEquals(0.0, optimizer.getRMS(), 1.0e-5);
        assertEquals(0.0, optimizer.getChiSquare(), 1.0e-5);
        assertTrue(optimizer.getEvaluations() > 0);
        assertTrue(optimizer.getJacobianEvaluations() > 0);
        assertTrue(optimizer.getIterations() > 0);
    }

    // Tests optimization for a circle fitting problem (non-linear)
    @Test
    public void testOptimize_circleFitting_findsCenterAndRadius() throws Exception {
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        double[] x = {1.0, -1.0, 0.0, 0.0};
        double[] y = {0.0, 0.0, 1.0, -1.0};
        double[] target = {0.0, 0.0, 0.0, 0.0};
        double[] weights = {1.0, 1.0, 1.0, 1.0};
        CircleProblem problem = new CircleProblem(x, y);

        VectorialPointValuePair optimum = optimizer.optimize(problem, target, weights, new double[]{0.1, 0.1, 0.8});
        double[] point = optimum.getPointRef();

        assertEquals(0.0, point[0], 1.0e-5);
        assertEquals(0.0, point[1], 1.0e-5);
        assertEquals(1.0, point[2], 1.0e-5);
        assertEquals(0.0, optimizer.getRMS(), 1.0e-5);

        double[][] cov = optimizer.getCovariances();
        assertNotNull(cov);
        assertEquals(3, cov.length);

        double[] errors = optimizer.guessParametersErrors();
        assertNotNull(errors);
        assertEquals(3, errors.length);
    }

    // Tests optimization when already starting at the optimum
    @Test
    public void testOptimize_startingAtOptimum_returnsImmediately() throws Exception {
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        double[] x = {1.0, 2.0, 3.0};
        double[] y = {2.0, 4.0, 6.0};
        double[] weights = {1.0, 1.0, 1.0};
        LinearProblem problem = new LinearProblem(x, y);

        VectorialPointValuePair optimum = optimizer.optimize(problem, y, weights, new double[]{2.0, 0.0});
        double[] point = optimum.getPointRef();

        assertEquals(2.0, point[0], 1.0e-8);
        assertEquals(0.0, point[1], 1.0e-8);
        assertEquals(0.0, optimizer.getRMS(), 1.0e-8);
    }

    // Tests optimization with custom convergence checker
    @Test
    public void testOptimize_customConvergenceChecker_convergesSuccessfully() throws Exception {
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        optimizer.setConvergenceChecker(new SimpleVectorialValueChecker(1.0e-6, 1.0e-6));

        double[] x = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] y = {4.0, 7.0, 10.0, 13.0, 16.0}; // y = 3x + 1
        double[] weights = {1.0, 1.0, 1.0, 1.0, 1.0};
        LinearProblem problem = new LinearProblem(x, y);

        VectorialPointValuePair optimum = optimizer.optimize(problem, y, weights, new double[]{0.0, 0.0});
        double[] point = optimum.getPointRef();

        assertEquals(3.0, point[0], 1.0e-4);
        assertEquals(1.0, point[1], 1.0e-4);
    }

    // Tests tuning parameters setters: initialStepBoundFactor, costRelativeTolerance, parRelativeTolerance, orthoTolerance
    @Test
    public void testSetTuningParameters_validValues_convergesCorrectly() throws Exception {
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        optimizer.setInitialStepBoundFactor(50.0);
        optimizer.setCostRelativeTolerance(1.0e-8);
        optimizer.setParRelativeTolerance(1.0e-8);
        optimizer.setOrthoTolerance(1.0e-8);

        double[] x = {1.0, 2.0, 3.0};
        double[] y = {1.0, 2.0, 3.0};
        double[] weights = {1.0, 1.0, 1.0};
        LinearProblem problem = new LinearProblem(x, y);

        VectorialPointValuePair optimum = optimizer.optimize(problem, y, weights, new double[]{0.5, 0.5});
        double[] point = optimum.getPointRef();

        assertEquals(1.0, point[0], 1.0e-5);
        assertEquals(0.0, point[1], 1.0e-5);
    }

    // Tests exception path when max iterations is exceeded
    @Test(expected = OptimizationException.class)
    public void testOptimize_maxIterationsExceeded_throwsOptimizationException() throws Exception {
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        optimizer.setMaxIterations(1);

        double[] x = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] y = {2.0, 5.0, 10.0, 17.0, 26.0};
        double[] weights = {1.0, 1.0, 1.0, 1.0, 1.0};
        LinearProblem problem = new LinearProblem(x, y);

        optimizer.optimize(problem, y, weights, new double[]{100.0, 100.0});
    }

    // Tests exception path when Jacobian contains NaN
    @Test(expected = OptimizationException.class)
    public void testOptimize_nanInJacobian_throwsOptimizationException() throws Exception {
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        NanJacobianProblem problem = new NanJacobianProblem();
        double[] target = {0.0, 0.0};
        double[] weights = {1.0, 1.0};

        optimizer.optimize(problem, target, weights, new double[]{1.0, 1.0});
    }

    // Tests rank deficient / over-determined system
    @Test
    public void testOptimize_overDeterminedRankDeficient_findsLeastSquaresSolution() throws Exception {
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        // A problem where the second column of Jacobian is zero (no impact on output)
        DifferentiableMultivariateVectorialFunction problem = new DifferentiableMultivariateVectorialFunction() {
            public double[] value(double[] point) {
                return new double[]{point[0] - 2.0, 2.0 * point[0] - 4.0, 3.0 * point[0] - 6.0};
            }

            public MultivariateMatrixFunction jacobian() {
                return new MultivariateMatrixFunction() {
                    public double[][] value(double[] point) {
                        return new double[][]{
                            {1.0, 0.0},
                            {2.0, 0.0},
                            {3.0, 0.0}
                        };
                    }
                };
            }
        };

        double[] target = {0.0, 0.0, 0.0};
        double[] weights = {1.0, 1.0, 1.0};
        VectorialPointValuePair optimum = optimizer.optimize(problem, target, weights, new double[]{0.0, 0.0});
        double[] point = optimum.getPointRef();

        assertEquals(2.0, point[0], 1.0e-5);
    }

    // Tests with zero start vector to exercise initial step bound when xNorm is 0
    @Test
    public void testOptimize_zeroInitialPoint_convergesSuccessfully() throws Exception {
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        double[] x = {1.0, 2.0};
        double[] y = {2.0, 4.0};
        double[] weights = {1.0, 1.0};
        LinearProblem problem = new LinearProblem(x, y);

        VectorialPointValuePair optimum = optimizer.optimize(problem, y, weights, new double[]{0.0, 0.0});
        double[] point = optimum.getPointRef();

        assertEquals(2.0, point[0], 1.0e-5);
        assertEquals(0.0, point[1], 1.0e-5);
    }

    // Tests non-zero residual optimization (approximate least squares)
    @Test
    public void testOptimize_inconsistentData_findsBestApproximation() throws Exception {
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        double[] x = {1.0, 2.0, 3.0};
        double[] y = {2.1, 3.9, 6.2}; // slightly noisy data for y = 2x
        double[] weights = {1.0, 1.0, 1.0};
        LinearProblem problem = new LinearProblem(x, y);

        VectorialPointValuePair optimum = optimizer.optimize(problem, y, weights, new double[]{1.0, 0.0});
        double[] point = optimum.getPointRef();

        assertEquals(2.05, point[0], 0.1);
        assertTrue(optimizer.getRMS() > 0.0);
    }

    // Tests function evaluation exception handling during optimization
    @Test(expected = FunctionEvaluationException.class)
    public void testOptimize_functionEvaluationException_propagatesException() throws Exception {
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        ExceptionProblem problem = new ExceptionProblem();
        double[] target = {0.0};
        double[] weights = {1.0};
        optimizer.optimize(problem, target, weights, new double[]{0.0});
    }

    // Tests threshold-based getCovariances method
    @Test
    public void testGetCovariances_withThreshold() throws Exception {
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        double[] x = {1.0, 2.0, 3.0, 4.0};
        double[] y = {2.0, 4.0, 6.0, 8.0};
        double[] weights = {1.0, 1.0, 1.0, 1.0};
        LinearProblem problem = new LinearProblem(x, y);

        optimizer.optimize(problem, y, weights, new double[]{0.0, 0.0});
        double[][] cov = optimizer.getCovariances(1.0e-5);
        assertNotNull(cov);
        assertEquals(2, cov.length);
        assertEquals(2, cov[0].length);
    }
}