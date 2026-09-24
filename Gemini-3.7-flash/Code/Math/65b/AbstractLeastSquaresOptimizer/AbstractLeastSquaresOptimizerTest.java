package org.apache.commons.math.optimization.general;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.DifferentiableMultivariateVectorialFunction;
import org.apache.commons.math.analysis.MultivariateMatrixFunction;
import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.optimization.SimpleVectorialValueChecker;
import org.apache.commons.math.optimization.VectorialConvergenceChecker;
import org.apache.commons.math.optimization.VectorialPointValuePair;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class AbstractLeastSquaresOptimizerTest {

    private DummyOptimizer optimizer;

    private static class DummyOptimizer extends AbstractLeastSquaresOptimizer {
        @Override
        protected VectorialPointValuePair doOptimize() {
            return new VectorialPointValuePair(point, objective);
        }

        public void callIncrementIterationsCounter() throws OptimizationException {
            incrementIterationsCounter();
        }

        public void callUpdateJacobian() throws FunctionEvaluationException {
            updateJacobian();
        }

        public void callUpdateResidualsAndCost() throws FunctionEvaluationException {
            updateResidualsAndCost();
        }

        public void setRows(int r) {
            this.rows = r;
        }

        public void setCols(int c) {
            this.cols = c;
        }

        public void setResiduals(double[] res) {
            this.residuals = res;
        }

        public void setResidualsWeights(double[] weights) {
            this.residualsWeights = weights;
        }

        public void setPoint(double[] pt) {
            this.point = pt;
        }

        public void setJacobian(double[][] jac) {
            this.jacobian = jac;
        }

        public double getCost() {
            return this.cost;
        }
    }

    @Before
    public void setUp() {
        optimizer = new DummyOptimizer();
    }

    // Tests default constructor initialization values
    @Test
    public void testConstructor_defaultSettings_initializedCorrectly() {
        assertEquals(AbstractLeastSquaresOptimizer.DEFAULT_MAX_ITERATIONS, optimizer.getMaxIterations());
        assertEquals(Integer.MAX_VALUE, optimizer.getMaxEvaluations());
        assertEquals(0, optimizer.getIterations());
        assertEquals(0, optimizer.getEvaluations());
        assertEquals(0, optimizer.getJacobianEvaluations());
        assertNotNull(optimizer.getConvergenceChecker());
    }

    // Tests setters and getters for iterations, evaluations, and checker
    @Test
    public void testSettersAndGetters_customValues_returnsConfiguredValues() {
        optimizer.setMaxIterations(50);
        assertEquals(50, optimizer.getMaxIterations());

        optimizer.setMaxEvaluations(200);
        assertEquals(200, optimizer.getMaxEvaluations());

        VectorialConvergenceChecker customChecker = new SimpleVectorialValueChecker(1e-6, 1e-6);
        optimizer.setConvergenceChecker(customChecker);
        assertSame(customChecker, optimizer.getConvergenceChecker());
    }

    // Tests incrementing iteration counter within valid bounds
    @Test
    public void testIncrementIterationsCounter_belowMax_incrementsSuccessfully() throws OptimizationException {
        optimizer.setMaxIterations(2);
        optimizer.callIncrementIterationsCounter();
        assertEquals(1, optimizer.getIterations());
        optimizer.callIncrementIterationsCounter();
        assertEquals(2, optimizer.getIterations());
    }

    // Tests exceeding max iterations throws OptimizationException
    @Test(expected = OptimizationException.class)
    public void testIncrementIterationsCounter_exceedMax_throwsException() throws OptimizationException {
        optimizer.setMaxIterations(1);
        optimizer.callIncrementIterationsCounter();
        optimizer.callIncrementIterationsCounter();
    }

    // Tests optimize method with dimension mismatch between target and weights
    @Test(expected = OptimizationException.class)
    public void testOptimize_targetWeightDimensionMismatch_throwsOptimizationException()
            throws FunctionEvaluationException, OptimizationException {
        DifferentiableMultivariateVectorialFunction f = createSimpleFunction();
        double[] target = new double[]{1.0, 2.0};
        double[] weights = new double[]{1.0};
        double[] startPoint = new double[]{0.0};
        optimizer.optimize(f, target, weights, startPoint);
    }

    // Tests successful optimization setup and execution
    @Test
    public void testOptimize_validInputs_runsDoOptimize()
            throws FunctionEvaluationException, OptimizationException {
        DifferentiableMultivariateVectorialFunction f = createSimpleFunction();
        double[] target = new double[]{2.0, 4.0};
        double[] weights = new double[]{1.0, 1.0};
        double[] startPoint = new double[]{1.0};

        VectorialPointValuePair result = optimizer.optimize(f, target, weights, startPoint);
        assertNotNull(result);
        assertEquals(0, optimizer.getIterations());
        assertEquals(0, optimizer.getEvaluations());
        assertEquals(0, optimizer.getJacobianEvaluations());
    }

    // Tests updateJacobian correctly evaluates and scales jacobian entries
    @Test
    public void testUpdateJacobian_validDimensions_computesWeightedJacobian()
            throws FunctionEvaluationException, OptimizationException {
        DifferentiableMultivariateVectorialFunction f = createSimpleFunction();
        double[] target = new double[]{2.0, 4.0};
        double[] weights = new double[]{4.0, 9.0};
        double[] startPoint = new double[]{1.0};

        optimizer.optimize(f, target, weights, startPoint);
        optimizer.callUpdateJacobian();

        assertEquals(1, optimizer.getJacobianEvaluations());
        // J = [[1.0], [2.0]], factor_0 = -sqrt(4) = -2, factor_1 = -sqrt(9) = -3
        // jacobian[0][0] = 1.0 * -2 = -2.0, jacobian[1][0] = 2.0 * -3 = -6.0
        double[][] jac = optimizer.jacobian;
        assertEquals(-2.0, jac[0][0], 1e-10);
        assertEquals(-6.0, jac[1][0], 1e-10);
    }

    // Tests updateJacobian throwing exception when row dimension mismatches
    @Test(expected = FunctionEvaluationException.class)
    public void testUpdateJacobian_dimensionMismatch_throwsFunctionEvaluationException()
            throws FunctionEvaluationException, OptimizationException {
        DifferentiableMultivariateVectorialFunction f = new DifferentiableMultivariateVectorialFunction() {
            public double[] value(double[] point) {
                return new double[]{point[0], point[0]};
            }
            public MultivariateMatrixFunction jacobian() {
                return new MultivariateMatrixFunction() {
                    public double[][] value(double[] point) {
                        return new double[][]{{1.0}}; // Returns 1 row instead of 2
                    }
                };
            }
        };

        optimizer.optimize(f, new double[]{1.0, 2.0}, new double[]{1.0, 1.0}, new double[]{0.0});
        optimizer.callUpdateJacobian();
    }

    // Tests updateResidualsAndCost calculation and cost evaluation
    @Test
    public void testUpdateResidualsAndCost_validDimensions_computesResidualsAndCost()
            throws FunctionEvaluationException, OptimizationException {
        DifferentiableMultivariateVectorialFunction f = createSimpleFunction();
        double[] target = new double[]{3.0, 5.0};
        double[] weights = new double[]{1.0, 2.0};
        double[] startPoint = new double[]{1.0}; // f(1.0) = [1.0, 2.0]

        optimizer.optimize(f, target, weights, startPoint);
        optimizer.callUpdateResidualsAndCost();

        assertEquals(1, optimizer.getEvaluations());
        // residuals: r[0] = 3.0 - 1.0 = 2.0, r[1] = 5.0 - 2.0 = 3.0
        assertEquals(2.0, optimizer.residuals[0], 1e-10);
        assertEquals(3.0, optimizer.residuals[1], 1e-10);
        // cost: sqrt(1.0 * 2^2 + 2.0 * 3^2) = sqrt(4 + 18) = sqrt(22)
        assertEquals(Math.sqrt(22.0), optimizer.getCost(), 1e-10);
    }

    // Tests updateResidualsAndCost exceeding max evaluations
    @Test(expected = FunctionEvaluationException.class)
    public void testUpdateResidualsAndCost_maxEvaluationsExceeded_throwsException()
            throws FunctionEvaluationException, OptimizationException {
        DifferentiableMultivariateVectorialFunction f = createSimpleFunction();
        optimizer.setMaxEvaluations(1);
        optimizer.optimize(f, new double[]{1.0, 2.0}, new double[]{1.0, 1.0}, new double[]{0.0});

        optimizer.callUpdateResidualsAndCost(); // 1st evaluation: ok
        optimizer.callUpdateResidualsAndCost(); // 2nd evaluation: exceeds max
    }

    // Tests updateResidualsAndCost throwing exception on value dimension mismatch
    @Test(expected = FunctionEvaluationException.class)
    public void testUpdateResidualsAndCost_valueDimensionMismatch_throwsException()
            throws FunctionEvaluationException, OptimizationException {
        DifferentiableMultivariateVectorialFunction f = new DifferentiableMultivariateVectorialFunction() {
            public double[] value(double[] point) {
                return new double[]{point[0]}; // Returns 1 element instead of 2
            }
            public MultivariateMatrixFunction jacobian() {
                return new MultivariateMatrixFunction() {
                    public double[][] value(double[] point) {
                        return new double[][]{{1.0}, {2.0}};
                    }
                };
            }
        };

        optimizer.optimize(f, new double[]{1.0, 2.0}, new double[]{1.0, 1.0}, new double[]{0.0});
        optimizer.callUpdateResidualsAndCost();
    }

    // Tests getRMS calculation
    @Test
    public void testGetRMS_standardResiduals_returnsCorrectRMS() {
        optimizer.setRows(2);
        optimizer.setResiduals(new double[]{2.0, 4.0});
        optimizer.setResidualsWeights(new double[]{1.0, 0.5});
        // criterion = 2^2 * 1.0 + 4^2 * 0.5 = 4 + 8 = 12.0
        // RMS = sqrt(12.0 / 2) = sqrt(6.0)
        assertEquals(Math.sqrt(6.0), optimizer.getRMS(), 1e-10);
    }

    // Tests getChiSquare calculation
    @Test
    public void testGetChiSquare_standardResiduals_returnsCorrectChiSquare() {
        optimizer.setRows(2);
        optimizer.setResiduals(new double[]{3.0, 4.0});
        optimizer.setResidualsWeights(new double[]{0.5, 2.0});
        // chiSquare = 3^2 / 0.5 + 4^2 / 2.0 = 18 + 8 = 26.0
        assertEquals(26.0, optimizer.getChiSquare(), 1e-10);
    }

    // Tests getCovariances normal computation
    @Test
    public void testGetCovariances_validJacobian_returnsCorrectCovarianceMatrix()
            throws FunctionEvaluationException, OptimizationException {
        DifferentiableMultivariateVectorialFunction f = createSimpleFunction();
        // target: 2 rows, start: 1 col
        optimizer.optimize(f, new double[]{0.0, 0.0}, new double[]{1.0, 1.0}, new double[]{0.0});

        // J after updateJacobian: [[-1.0], [-2.0]]
        // jTj = (-1)^2 + (-2)^2 = 5.0 -> inverse = 1 / 5.0 = 0.2
        double[][] cov = optimizer.getCovariances();
        assertEquals(1, cov.length);
        assertEquals(1, cov[0].length);
        assertEquals(0.2, cov[0][0], 1e-10);
    }

    // Tests getCovariances with singular matrix throws OptimizationException
    @Test(expected = OptimizationException.class)
    public void testGetCovariances_singularMatrix_throwsOptimizationException()
            throws FunctionEvaluationException, OptimizationException {
        DifferentiableMultivariateVectorialFunction f = new DifferentiableMultivariateVectorialFunction() {
            public double[] value(double[] point) {
                return new double[]{0.0, 0.0};
            }
            public MultivariateMatrixFunction jacobian() {
                return new MultivariateMatrixFunction() {
                    public double[][] value(double[] point) {
                        return new double[][]{{0.0, 0.0}, {0.0, 0.0}};
                    }
                };
            }
        };

        optimizer.optimize(f, new double[]{0.0, 0.0}, new double[]{1.0, 1.0}, new double[]{0.0, 0.0});
        optimizer.getCovariances();
    }

    // Tests guessParametersErrors throws OptimizationException when rows <= cols (no degrees of freedom)
    @Test(expected = OptimizationException.class)
    public void testGuessParametersErrors_rowsLessOrEqualToCols_throwsOptimizationException()
            throws FunctionEvaluationException, OptimizationException {
        DifferentiableMultivariateVectorialFunction f = new DifferentiableMultivariateVectorialFunction() {
            public double[] value(double[] point) {
                return new double[]{point[0] + point[1]};
            }
            public MultivariateMatrixFunction jacobian() {
                return new MultivariateMatrixFunction() {
                    public double[][] value(double[] point) {
                        return new double[][]{{1.0, 1.0}};
                    }
                };
            }
        };

        // rows = 1, cols = 2 -> rows <= cols
        optimizer.optimize(f, new double[]{0.0}, new double[]{1.0}, new double[]{0.0, 0.0});
        optimizer.guessParametersErrors();
    }

    // Tests guessParametersErrors normal calculation
    @Test
    public void testGuessParametersErrors_validDegreesOfFreedom_computesErrors()
            throws FunctionEvaluationException, OptimizationException {
        DifferentiableMultivariateVectorialFunction f = createSimpleFunction();
        // rows = 2, cols = 1 -> rows > cols
        optimizer.optimize(f, new double[]{2.0, 4.0}, new double[]{1.0, 1.0}, new double[]{0.0});
        optimizer.callUpdateResidualsAndCost();

        double[] errors = optimizer.guessParametersErrors();
        assertEquals(1, errors.length);
        assertTrue(errors[0] >= 0.0);
    }

    private DifferentiableMultivariateVectorialFunction createSimpleFunction() {
        return new DifferentiableMultivariateVectorialFunction() {
            public double[] value(double[] point) {
                return new double[]{point[0], 2.0 * point[0]};
            }

            public MultivariateMatrixFunction jacobian() {
                return new MultivariateMatrixFunction() {
                    public double[][] value(double[] point) {
                        return new double[][]{
                                {1.0},
                                {2.0}
                        };
                    }
                };
            }
        };
    }
}