package org.apache.commons.math3.optimization.general;

import org.apache.commons.math3.analysis.DifferentiableMultivariateVectorFunction;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.optimization.ConvergenceChecker;
import org.apache.commons.math3.optimization.PointVectorValuePair;
import org.apache.commons.math3.util.FastMath;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

public class AbstractLeastSquaresOptimizerTest {

    private TestOptimizer optimizer;
    private double[] target;
    private double[] weights;
    private double[] startPoint;
    private DifferentiableMultivariateVectorFunction identityFunction;

    private static class TestOptimizer extends AbstractLeastSquaresOptimizer {
        public TestOptimizer(ConvergenceChecker<PointVectorValuePair> checker) {
            super(checker);
        }
        @Override
        protected PointVectorValuePair doOptimize() {
            return null;
        }
    }

    @Before
    public void setup() {
        identityFunction = new DifferentiableMultivariateVectorFunction() {
            @Override
            public double[] value(double[] params) {
                return params.clone();
            }
            @Override
            public RealMatrix jacobian(double[] params) {
                return MatrixUtils.createRealIdentityMatrix(params.length);
            }
        };
        target = new double[]{1.0, 1.0};
        weights = new double[]{1.0, 1.0};
        startPoint = new double[]{0.0, 0.0};
        optimizer = new TestOptimizer(new ConvergenceChecker<PointVectorValuePair>() {
            @Override
            public boolean converged(int iteration, PointVectorValuePair previous, PointVectorValuePair current) {
                return true;
            }
        });
        optimizer.optimize(100, identityFunction, target, weights, startPoint);
    }

    // Tests normal computation of cost with identity weight matrix
    @Test
    public void testComputeCost_identityWeight_returnsCorrectCost() {
        double[] residuals = new double[]{1.0, 1.0};
        double expectedCost = FastMath.sqrt(2.0);
        double actualCost = optimizer.computeCost(residuals);
        assertEquals(expectedCost, actualCost, 1e-15);
    }

    // Tests cost when residuals are zero
    @Test
    public void testComputeCost_zeroResiduals_returnsZero() {
        double[] residuals = new double[]{0.0, 0.0};
        double actualCost = optimizer.computeCost(residuals);
        assertEquals(0.0, actualCost, 0.0);
    }

    // Tests normal computation of residuals
    @Test
    public void testComputeResiduals_normal_returnsCorrectResiduals() {
        double[] objectiveValue = {0.0, 0.0};
        double[] expected = {1.0, 1.0};
        double[] actual = optimizer.computeResiduals(objectiveValue);
        assertArrayEquals(expected, actual, 1e-15);
    }

    // Tests dimension mismatch in computeResiduals
    @Test(expected = DimensionMismatchException.class)
    public void testComputeResiduals_dimensionMismatch_throwsException() {
        double[] objectiveValue = {0.0, 0.0, 0.0};
        optimizer.computeResiduals(objectiveValue);
    }

    // Tests Jacobian computation with identity function
    @Test
    public void testComputeWeightedJacobian_identity_returnsWeightSqrt() {
        double[] params = {0.0, 0.0};
        RealMatrix jacobian = optimizer.computeWeightedJacobian(params);
        RealMatrix expected = MatrixUtils.createRealIdentityMatrix(2);
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                assertEquals(expected.getEntry(i, j), jacobian.getEntry(i, j), 1e-15);
            }
        }
    }

    // Tests dimension mismatch in computeWeightedJacobian
    @Test(expected = DimensionMismatchException.class)
    public void testComputeWeightedJacobian_dimensionMismatch_throwsException() {
        DifferentiableMultivariateVectorFunction badFunction = new DifferentiableMultivariateVectorFunction() {
            @Override
            public double[] value(double[] params) {
                return new double[]{0.0, 0.0, 0.0};
            }
            @Override
            public RealMatrix jacobian(double[] params) {
                return MatrixUtils.createRealMatrix(3, params.length);
            }
        };
        TestOptimizer badOptimizer = new TestOptimizer(new ConvergenceChecker<PointVectorValuePair>() {
            @Override
            public boolean converged(int iteration, PointVectorValuePair previous, PointVectorValuePair current) {
                return true;
            }
        });
        badOptimizer.optimize(100, badFunction, new double[]{1.0, 1.0}, new double[]{1.0, 1.0}, new double[]{0.0, 0.0});
        badOptimizer.computeWeightedJacobian(new double[]{0.0, 0.0});
    }

    // Tests covariance computation with identity Jacobian
    @Test
    public void testComputeCovariances_identity_returnsIdentity() {
        double[][] cov = optimizer.computeCovariances(new double[]{0.0, 0.0}, 1e-14);
        RealMatrix covMatrix = MatrixUtils.createRealMatrix(cov);
        RealMatrix expected = MatrixUtils.createRealIdentityMatrix(2);
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                assertEquals(expected.getEntry(i, j), covMatrix.getEntry(i, j), 1e-10);
            }
        }
    }

    // Tests sigma computation with identity Jacobian
    @Test
    public void testComputeSigma_identity_returnsOnes() {
        double[] sigma = optimizer.computeSigma(new double[]{0.0, 0.0}, 1e-14);
        double[] expected = {1.0, 1.0};
        assertArrayEquals(expected, sigma, 1e-10);
    }

    // Tests getRMS after setting cost
    @Test
    public void testGetRMS_afterSetCost_returnsExpected() {
        optimizer.setCost(FastMath.sqrt(2.0));
        double expectedRMS = 1.0;
        assertEquals(expectedRMS, optimizer.getRMS(), 1e-15);
    }

    // Tests getChiSquare after setting cost
    @Test
    public void testGetChiSquare_afterSetCost_returnsExpected() {
        optimizer.setCost(FastMath.sqrt(2.0));
        double expectedChiSquare = 2.0;
        assertEquals(expectedChiSquare, optimizer.getChiSquare(), 1e-15);
    }

    // Tests guessParametersErrors throws exception when rows <= cols
    @Test(expected = NumberIsTooSmallException.class)
    public void testGuessParametersErrors_insufficientDegrees_throwsException() {
        optimizer.guessParametersErrors();
    }

    // Tests guessParametersErrors with sufficient degrees of freedom
    @Test
    public void testGuessParametersErrors_normalDegrees_returnsErrors() {
        DifferentiableMultivariateVectorFunction function3x2 = new DifferentiableMultivariateVectorFunction() {
            @Override
            public double[] value(double[] params) {
                return new double[]{params[0], params[1], params[0] + params[1]};
            }
            @Override
            public RealMatrix jacobian(double[] params) {
                double[][] data = {{1,0},{0,1},{1,1}};
                return MatrixUtils.createRealMatrix(data);
            }
        };
        TestOptimizer opt = new TestOptimizer(new ConvergenceChecker<PointVectorValuePair>() {
            @Override
            public boolean converged(int iteration, PointVectorValuePair previous, PointVectorValuePair current) {
                return true;
            }
        });
        opt.optimize(100, function3x2, new double[]{1,1,1}, new double[]{1,1,1}, new double[]{0,0});
        double[] errors = opt.guessParametersErrors();
        double expected = FastMath.sqrt(2.0);
        assertArrayEquals(new double[]{expected, expected}, errors, 1e-12);
    }

    // Tests setCost updates the cost field
    @Test
    public void testSetCost_updatesField() {
        optimizer.setCost(3.0);
        assertEquals(3.0, optimizer.cost, 1e-15);
    }

    // Tests getJacobianEvaluations increments after computeWeightedJacobian
    @Test
    public void testGetJacobianEvaluations_afterCompute_returnsCount() {
        int initial = optimizer.getJacobianEvaluations();
        optimizer.computeWeightedJacobian(new double[]{0,0});
        assertEquals(initial + 1, optimizer.getJacobianEvaluations());
        optimizer.computeWeightedJacobian(new double[]{0,0});
        assertEquals(initial + 2, optimizer.getJacobianEvaluations());
    }

    // Tests getWeightSquareRoot returns a copy
    @Test
    public void testGetWeightSquareRoot_returnsCopy() {
        RealMatrix sqrt = optimizer.getWeightSquareRoot();
        assertNotNull(sqrt);
        assertEquals(2, sqrt.getRowDimension());
        assertEquals(2, sqrt.getColumnDimension());
        for (int i = 0; i < 2; i++) {
            assertEquals(1.0, sqrt.getEntry(i, i), 1e-15);
            for (int j = 0; j < 2; j++) {
                if (i != j) {
                    assertEquals(0.0, sqrt.getEntry(i, j), 1e-15);
                }
            }
        }
        sqrt.setEntry(0, 0, 10.0);
        RealMatrix internalCopy = optimizer.getWeightSquareRoot();
        assertEquals(1.0, internalCopy.getEntry(0, 0), 1e-15);
    }

    // Tests deprecated updateJacobian sets weightedResidualJacobian
    @Test
    public void testUpdateJacobian_deprecated_setsWeightedResidualJacobian() {
        optimizer.updateJacobian();
        RealMatrix wj = optimizer.computeWeightedJacobian(optimizer.point);
        double[][] expected = wj.scalarMultiply(-1).getData();
        double[][] actual = optimizer.weightedResidualJacobian;
        assertNotNull(actual);
        assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            assertArrayEquals(expected[i], actual[i], 1e-15);
        }
    }

    // Tests deprecated updateResidualsAndCost updates fields
    @Test
    public void testUpdateResidualsAndCost_deprecated_updatesFields() {
        optimizer.updateResidualsAndCost();
        assertArrayEquals(new double[]{0.0, 0.0}, optimizer.objective, 1e-15);
        assertArrayEquals(new double[]{1.0, 1.0}, optimizer.weightedResiduals, 1e-15);
        assertEquals(FastMath.sqrt(2.0), optimizer.cost, 1e-15);
    }
}