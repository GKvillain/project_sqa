package org.apache.commons.math3.optim.nonlinear.vector.jacobian;

import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.DiagonalMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularMatrixException;
import org.apache.commons.math3.optim.ConvergenceChecker;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointVectorValuePair;
import org.apache.commons.math3.optim.nonlinear.vector.ModelFunction;
import org.apache.commons.math3.optim.nonlinear.vector.ModelFunctionJacobian;
import org.apache.commons.math3.optim.nonlinear.vector.Target;
import org.apache.commons.math3.optim.nonlinear.vector.Weight;
import org.junit.Assert;
import org.junit.Test;

public class AbstractLeastSquaresOptimizerTest {

    private static class DummyOptimizer extends AbstractLeastSquaresOptimizer {
        public DummyOptimizer() {
            super(null);
        }

        public DummyOptimizer(ConvergenceChecker<PointVectorValuePair> checker) {
            super(checker);
        }

        @Override
        protected PointVectorValuePair doOptimize() {
            final double[] params = getStartPoint();
            final double[] objVal = computeObjectiveValue(params);
            final double[] residuals = computeResiduals(objVal);
            setCost(computeCost(residuals));
            return new PointVectorValuePair(params, objVal);
        }

        public RealMatrix testComputeWeightedJacobian(double[] params) {
            return computeWeightedJacobian(params);
        }

        public double testComputeCost(double[] residuals) {
            return computeCost(residuals);
        }

        public double[] testComputeResiduals(double[] objectiveValue) {
            return computeResiduals(objectiveValue);
        }

        public void testSetCost(double cost) {
            setCost(cost);
        }
    }

    // Tests computeCost with identity weight matrix
    @Test
    public void testComputeCost_identityWeight_returnsCorrectCost() {
        DummyOptimizer optimizer = new DummyOptimizer();
        optimizer.optimize(
            new MaxEval(100),
            new Target(new double[]{0.0, 0.0}),
            new Weight(new double[]{1.0, 1.0}),
            new InitialGuess(new double[]{0.0}),
            new ModelFunction(new MultivariateVectorFunction() {
                public double[] value(double[] point) {
                    return new double[]{0.0, 0.0};
                }
            }),
            new ModelFunctionJacobian(new MultivariateMatrixFunction() {
                public double[][] value(double[] point) {
                    return new double[][]{{1.0}, {1.0}};
                }
            })
        );

        double cost = optimizer.testComputeCost(new double[]{3.0, 4.0});
        Assert.assertEquals(5.0, cost, 1e-10);
    }

    // Tests getChiSquare and getRMS calculations
    @Test
    public void testGetRMSAndChiSquare_validCost_returnsExpectedValues() {
        DummyOptimizer optimizer = new DummyOptimizer();
        optimizer.optimize(
            new MaxEval(100),
            new Target(new double[]{1.0, 2.0, 3.0, 4.0}),
            new Weight(new double[]{1.0, 1.0, 1.0, 1.0}),
            new InitialGuess(new double[]{0.0}),
            new ModelFunction(new MultivariateVectorFunction() {
                public double[] value(double[] point) {
                    return new double[]{0.0, 0.0, 0.0, 0.0};
                }
            }),
            new ModelFunctionJacobian(new MultivariateMatrixFunction() {
                public double[][] value(double[] point) {
                    return new double[][]{{1.0}, {1.0}, {1.0}, {1.0}};
                }
            })
        );

        optimizer.testSetCost(4.0);
        Assert.assertEquals(16.0, optimizer.getChiSquare(), 1e-10);
        Assert.assertEquals(2.0, optimizer.getRMS(), 1e-10);
    }

    // Tests computeResiduals with valid matching dimension
    @Test
    public void testComputeResiduals_matchingDimension_returnsDifferences() {
        DummyOptimizer optimizer = new DummyOptimizer();
        optimizer.optimize(
            new MaxEval(100),
            new Target(new double[]{10.0, 20.0}),
            new Weight(new double[]{1.0, 1.0}),
            new InitialGuess(new double[]{0.0}),
            new ModelFunction(new MultivariateVectorFunction() {
                public double[] value(double[] point) {
                    return new double[]{0.0, 0.0};
                }
            }),
            new ModelFunctionJacobian(new MultivariateMatrixFunction() {
                public double[][] value(double[] point) {
                    return new double[][]{{1.0}, {1.0}};
                }
            })
        );

        double[] residuals = optimizer.testComputeResiduals(new double[]{3.0, 5.0});
        Assert.assertEquals(2, residuals.length);
        Assert.assertEquals(7.0, residuals[0], 1e-10);
        Assert.assertEquals(15.0, residuals[1], 1e-10);
    }

    // Tests computeResiduals throwing DimensionMismatchException on mismatch
    @Test(expected = DimensionMismatchException.class)
    public void testComputeResiduals_dimensionMismatch_throwsException() {
        DummyOptimizer optimizer = new DummyOptimizer();
        optimizer.optimize(
            new MaxEval(100),
            new Target(new double[]{10.0, 20.0}),
            new Weight(new double[]{1.0, 1.0}),
            new InitialGuess(new double[]{0.0}),
            new ModelFunction(new MultivariateVectorFunction() {
                public double[] value(double[] point) {
                    return new double[]{0.0, 0.0};
                }
            }),
            new ModelFunctionJacobian(new MultivariateMatrixFunction() {
                public double[][] value(double[] point) {
                    return new double[][]{{1.0}, {1.0}};
                }
            })
        );

        optimizer.testComputeResiduals(new double[]{1.0, 2.0, 3.0});
    }

    // Tests computeWeightedJacobian with 2D weight matrix
    @Test
    public void testComputeWeightedJacobian_validJacobianAndWeight_returnsWeighted() {
        DummyOptimizer optimizer = new DummyOptimizer();
        optimizer.optimize(
            new MaxEval(100),
            new Target(new double[]{0.0, 0.0}),
            new Weight(new Array2DRowRealMatrix(new double[][]{{4.0, 0.0}, {0.0, 9.0}})),
            new InitialGuess(new double[]{1.0}),
            new ModelFunction(new MultivariateVectorFunction() {
                public double[] value(double[] point) {
                    return new double[]{point[0], point[0]};
                }
            }),
            new ModelFunctionJacobian(new MultivariateMatrixFunction() {
                public double[][] value(double[] point) {
                    return new double[][]{{2.0}, {3.0}};
                }
            })
        );

        RealMatrix weightedJacobian = optimizer.testComputeWeightedJacobian(new double[]{1.0});
        Assert.assertEquals(4.0, weightedJacobian.getEntry(0, 0), 1e-10);
        Assert.assertEquals(9.0, weightedJacobian.getEntry(1, 0), 1e-10);
    }

    // Tests computeCovariances and computeSigma with invertible matrix
    @Test
    public void testComputeCovariancesAndSigma_invertibleJacobian_returnsCorrectCovariance() {
        DummyOptimizer optimizer = new DummyOptimizer();
        optimizer.optimize(
            new MaxEval(100),
            new Target(new double[]{0.0, 0.0}),
            new Weight(new double[]{1.0, 1.0}),
            new InitialGuess(new double[]{0.0, 0.0}),
            new ModelFunction(new MultivariateVectorFunction() {
                public double[] value(double[] point) {
                    return new double[]{point[0], point[1]};
                }
            }),
            new ModelFunctionJacobian(new MultivariateMatrixFunction() {
                public double[][] value(double[] point) {
                    return new double[][]{{2.0, 0.0}, {0.0, 4.0}};
                }
            })
        );

        double[] params = new double[]{0.0, 0.0};
        double[][] cov = optimizer.computeCovariances(params, 1e-10);
        Assert.assertEquals(0.25, cov[0][0], 1e-10);
        Assert.assertEquals(0.0, cov[0][1], 1e-10);
        Assert.assertEquals(0.0, cov[1][0], 1e-10);
        Assert.assertEquals(0.0625, cov[1][1], 1e-10);

        double[] sigma = optimizer.computeSigma(params, 1e-10);
        Assert.assertEquals(0.5, sigma[0], 1e-10);
        Assert.assertEquals(0.25, sigma[1], 1e-10);
    }

    // Tests computeCovariances with singular matrix throwing SingularMatrixException
    @Test(expected = SingularMatrixException.class)
    public void testComputeCovariances_singularJacobian_throwsException() {
        DummyOptimizer optimizer = new DummyOptimizer();
        optimizer.optimize(
            new MaxEval(100),
            new Target(new double[]{0.0, 0.0}),
            new Weight(new double[]{1.0, 1.0}),
            new InitialGuess(new double[]{0.0, 0.0}),
            new ModelFunction(new MultivariateVectorFunction() {
                public double[] value(double[] point) {
                    return new double[]{0.0, 0.0};
                }
            }),
            new ModelFunctionJacobian(new MultivariateMatrixFunction() {
                public double[][] value(double[] point) {
                    return new double[][]{{1.0, 1.0}, {1.0, 1.0}};
                }
            })
        );

        optimizer.computeCovariances(new double[]{0.0, 0.0}, 1e-10);
    }

    // Tests getWeightSquareRoot returns copy of sqrt weight matrix
    @Test
    public void testGetWeightSquareRoot_returnsCopy() {
        DummyOptimizer optimizer = new DummyOptimizer();
        optimizer.optimize(
            new MaxEval(100),
            new Target(new double[]{0.0, 0.0}),
            new Weight(new Array2DRowRealMatrix(new double[][]{{4.0, 0.0}, {0.0, 16.0}})),
            new InitialGuess(new double[]{0.0}),
            new ModelFunction(new MultivariateVectorFunction() {
                public double[] value(double[] point) {
                    return new double[]{0.0, 0.0};
                }
            }),
            new ModelFunctionJacobian(new MultivariateMatrixFunction() {
                public double[][] value(double[] point) {
                    return new double[][]{{1.0}, {1.0}};
                }
            })
        );

        RealMatrix sqrtW = optimizer.getWeightSquareRoot();
        Assert.assertEquals(2.0, sqrtW.getEntry(0, 0), 1e-10);
        Assert.assertEquals(4.0, sqrtW.getEntry(1, 1), 1e-10);
    }

    // Tests optimizing with DiagonalMatrix weight representation (Defects4J Math-14 bug detection)
    @Test
    public void testOptimize_diagonalMatrixWeight_computesWeightSquareRoot() {
        DummyOptimizer optimizer = new DummyOptimizer();
        PointVectorValuePair result = optimizer.optimize(
            new MaxEval(100),
            new Target(new double[]{1.0, 2.0}),
            new Weight(new DiagonalMatrix(new double[]{4.0, 9.0})),
            new InitialGuess(new double[]{0.5}),
            new ModelFunction(new MultivariateVectorFunction() {
                public double[] value(double[] point) {
                    return new double[]{point[0] * 2, point[0] * 4};
                }
            }),
            new ModelFunctionJacobian(new MultivariateMatrixFunction() {
                public double[][] value(double[] point) {
                    return new double[][]{{2.0}, {4.0}};
                }
            })
        );

        Assert.assertNotNull(result);
        RealMatrix sqrtW = optimizer.getWeightSquareRoot();
        Assert.assertEquals(2.0, sqrtW.getEntry(0, 0), 1e-10);
        Assert.assertEquals(3.0, sqrtW.getEntry(1, 1), 1e-10);
    }

    // Tests optimize execution and resulting point vector value pair
    @Test
    public void testOptimize_standardExecution_returnsOptimizedPair() {
        DummyOptimizer optimizer = new DummyOptimizer();
        PointVectorValuePair pair = optimizer.optimize(
            new MaxEval(100),
            new Target(new double[]{5.0}),
            new Weight(new double[]{1.0}),
            new InitialGuess(new double[]{2.0}),
            new ModelFunction(new MultivariateVectorFunction() {
                public double[] value(double[] point) {
                    return new double[]{point[0] * 2.0};
                }
            }),
            new ModelFunctionJacobian(new MultivariateMatrixFunction() {
                public double[][] value(double[] point) {
                    return new double[][]{{2.0}};
                }
            })
        );

        Assert.assertArrayEquals(new double[]{2.0}, pair.getPoint(), 1e-10);
        Assert.assertArrayEquals(new double[]{4.0}, pair.getValue(), 1e-10);
        Assert.assertEquals(1.0, optimizer.getChiSquare(), 1e-10);
        Assert.assertEquals(1.0, optimizer.getRMS(), 1e-10);
    }
}