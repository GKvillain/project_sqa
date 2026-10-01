package org.apache.commons.math3.optim.nonlinear.vector.jacobian;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import org.apache.commons.math3.exception.ConvergenceException;
import org.apache.commons.math3.exception.MathUnsupportedOperationException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.optim.ConvergenceChecker;
import org.apache.commons.math3.optim.PointVectorValuePair;

/**
 * JUnit 4 test class for GaussNewtonOptimizer (Defects4J bug 6b).
 */
public class GaussNewtonOptimizerTest {

    // ---------------------------------------------------------------
    // Helper: a mock optimizer that allows control over inputs and Jacobian.
    // ---------------------------------------------------------------
    private static class MockOptimizer extends GaussNewtonOptimizer {
        private final double[] target;
        private final RealMatrix weight;
        private final double[] start;
        private final ConvergenceChecker<PointVectorValuePair> checker;
        private final double[] lower;
        private final double[] upper;
        private final boolean singular;

        MockOptimizer(boolean useLU,
                      double[] target,
                      RealMatrix weight,
                      double[] start,
                      ConvergenceChecker<PointVectorValuePair> checker,
                      double[] lower,
                      double[] upper,
                      boolean singular) {
            super(useLU, checker); // checker may be null
            this.target = target;
            this.weight = weight;
            this.start = start;
            this.checker = checker;
            this.lower = lower;
            this.upper = upper;
            this.singular = singular;
        }

        @Override
        public double[] getTarget() {
            return target;
        }

        @Override
        protected RealMatrix getWeight() {
            return weight;
        }

        @Override
        protected double[] getStartPoint() {
            return start;
        }

        @Override
        public ConvergenceChecker<PointVectorValuePair> getConvergenceChecker() {
            return checker;
        }

        @Override
        protected double[] getLowerBound() {
            return lower;
        }

        @Override
        protected double[] getUpperBound() {
            return upper;
        }

        @Override
        protected double[] computeObjectiveValue(double[] point) {
            // Simple linear model: f(p) = [p, 2p]
            return new double[] { point[0], 2 * point[0] };
        }

        @Override
        protected double[] computeResiduals(double[] objective) {
            double[] res = new double[objective.length];
            for (int i = 0; i < objective.length; i++) {
                res[i] = target[i] - objective[i];
            }
            return res;
        }

        @Override
        protected RealMatrix computeWeightedJacobian(double[] point) {
            if (singular) {
                // Zero rows cause singular normal equations.
                return new BlockRealMatrix(new double[][] { { 0.0 }, { 0.0 } });
            } else {
                // Jacobian rows: [1] and [2] (weight = identity)
                return new BlockRealMatrix(new double[][] { { 1.0 }, { 2.0 } });
            }
        }
    }

    // ---------------------------------------------------------------
    // Test: null convergence checker throws NullArgumentException
    // ---------------------------------------------------------------
    @Test(expected = NullArgumentException.class)
    public void testDoOptimize_nullChecker_throwsNullArgumentException() {
        MockOptimizer opt = new MockOptimizer(true, null, null, null, null, null, null, false);
        opt.doOptimize();
    }

    // ---------------------------------------------------------------
    // Test: lower bound present throws MathUnsupportedOperationException
    // ---------------------------------------------------------------
    @Test(expected = MathUnsupportedOperationException.class)
    public void testDoOptimize_withLowerBound_throwsMathUnsupportedOperationException() {
        double[] target = { 1.0, 2.0 };
        RealMatrix weight = new BlockRealMatrix(new double[][] { { 1, 0 }, { 0, 1 } });
        double[] start = { 0.0 };
        ConvergenceChecker<PointVectorValuePair> checker = new ConvergenceChecker<PointVectorValuePair>() {
            @Override
            public boolean converged(int iteration, PointVectorValuePair previous, PointVectorValuePair current) {
                return true;
            }
        };
        MockOptimizer opt = new MockOptimizer(true, target, weight, start, checker,
                                              new double[] { 0.0 }, null, false);
        opt.doOptimize();
    }

    // ---------------------------------------------------------------
    // Test: upper bound present throws MathUnsupportedOperationException
    // ---------------------------------------------------------------
    @Test(expected = MathUnsupportedOperationException.class)
    public void testDoOptimize_withUpperBound_throwsMathUnsupportedOperationException() {
        double[] target = { 1.0, 2.0 };
        RealMatrix weight = new BlockRealMatrix(new double[][] { { 1, 0 }, { 0, 1 } });
        double[] start = { 0.0 };
        ConvergenceChecker<PointVectorValuePair> checker = new ConvergenceChecker<PointVectorValuePair>() {
            @Override
            public boolean converged(int iteration, PointVectorValuePair previous, PointVectorValuePair current) {
                return true;
            }
        };
        MockOptimizer opt = new MockOptimizer(true, target, weight, start, checker,
                                              null, new double[] { 1.0 }, false);
        opt.doOptimize();
    }

    // ---------------------------------------------------------------
    // Test: singular Jacobian (LU) throws ConvergenceException
    // ---------------------------------------------------------------
    @Test(expected = ConvergenceException.class)
    public void testDoOptimize_singularMatrixWithLU_throwsConvergenceException() {
        double[] target = { 0.0, 0.0 };
        RealMatrix weight = new BlockRealMatrix(new double[][] { { 1, 0 }, { 0, 1 } });
        double[] start = { 1.0 };
        ConvergenceChecker<PointVectorValuePair> checker = new ConvergenceChecker<PointVectorValuePair>() {
            @Override
            public boolean converged(int iteration, PointVectorValuePair previous, PointVectorValuePair current) {
                return false; // never converges, but exception should occur earlier
            }
        };
        MockOptimizer opt = new MockOptimizer(true, target, weight, start, checker, null, null, true);
        opt.doOptimize();
    }

    // ---------------------------------------------------------------
    // Test: singular Jacobian (QR) throws ConvergenceException
    // ---------------------------------------------------------------
    @Test(expected = ConvergenceException.class)
    public void testDoOptimize_singularMatrixWithQR_throwsConvergenceException() {
        double[] target = { 0.0, 0.0 };
        RealMatrix weight = new BlockRealMatrix(new double[][] { { 1, 0 }, { 0, 1 } });
        double[] start = { 1.0 };
        ConvergenceChecker<PointVectorValuePair> checker = new ConvergenceChecker<PointVectorValuePair>() {
            @Override
            public boolean converged(int iteration, PointVectorValuePair previous, PointVectorValuePair current) {
                return false;
            }
        };
        MockOptimizer opt = new MockOptimizer(false, target, weight, start, checker, null, null, true);
        opt.doOptimize();
    }

    // ---------------------------------------------------------------
    // Test: normal convergence after first iteration (LU)
    // ---------------------------------------------------------------
    @Test
    public void testDoOptimize_normalConvergenceFirstIterationWithLU_correctResult() {
        double[] target = { 1.0, 2.0 };
        RealMatrix weight = new BlockRealMatrix(new double[][] { { 1, 0 }, { 0, 1 } });
        double[] start = { 0.0 };
        ConvergenceChecker<PointVectorValuePair> checker = new ConvergenceChecker<PointVectorValuePair>() {
            @Override
            public boolean converged(int iteration, PointVectorValuePair previous, PointVectorValuePair current) {
                return iteration >= 1;
            }
        };
        MockOptimizer opt = new MockOptimizer(true, target, weight, start, checker, null, null, false);
        PointVectorValuePair result = opt.doOptimize();
        double[] point = result.getPoint();
        // Expect p = 1.8 after one Gauss-Newton step
        assertEquals(1.8, point[0], 1e-10);
    }

    // ---------------------------------------------------------------
    // Test: normal convergence after first iteration (QR)
    // ---------------------------------------------------------------
    @Test
    public void testDoOptimize_normalConvergenceFirstIterationWithQR_correctResult() {
        double[] target = { 1.0, 2.0 };
        RealMatrix weight = new BlockRealMatrix(new double[][] { { 1, 0 }, { 0, 1 } });
        double[] start = { 0.0 };
        ConvergenceChecker<PointVectorValuePair> checker = new ConvergenceChecker<PointVectorValuePair>() {
            @Override
            public boolean converged(int iteration, PointVectorValuePair previous, PointVectorValuePair current) {
                return iteration >= 1;
            }
        };
        MockOptimizer opt = new MockOptimizer(false, target, weight, start, checker, null, null, false);
        PointVectorValuePair result = opt.doOptimize();
        double[] point = result.getPoint();
        assertEquals(1.8, point[0], 1e-10);
    }

    // ---------------------------------------------------------------
    // Test: normal convergence after two iterations (LU)
    // ---------------------------------------------------------------
    @Test
    public void testDoOptimize_convergedAfterTwoIterationsWithLU_correctResult() {
        double[] target = { 1.0, 2.0 };
        RealMatrix weight = new BlockRealMatrix(new double[][] { { 1, 0 }, { 0, 1 } });
        double[] start = { 0.0 };
        final int[] iterCount = { 0 };
        ConvergenceChecker<PointVectorValuePair> checker = new ConvergenceChecker<PointVectorValuePair>() {
            @Override
            public boolean converged(int iteration, PointVectorValuePair previous, PointVectorValuePair current) {
                iterCount[0]++;
                return iterCount[0] >= 2;
            }
        };
        MockOptimizer opt = new MockOptimizer(true, target, weight, start, checker, null, null, false);
        PointVectorValuePair result = opt.doOptimize();
        double[] point = result.getPoint();
        // Expect p = 0.36 after two Gauss-Newton steps
        assertEquals(0.36, point[0], 1e-10);
    }

    // ---------------------------------------------------------------
    // Test: normal convergence after two iterations (QR)
    // ---------------------------------------------------------------
    @Test
    public void testDoOptimize_convergedAfterTwoIterationsWithQR_correctResult() {
        double[] target = { 1.0, 2.0 };
        RealMatrix weight = new BlockRealMatrix(new double[][] { { 1, 0 }, { 0, 1 } });
        double[] start = { 0.0 };
        final int[] iterCount = { 0 };
        ConvergenceChecker<PointVectorValuePair> checker = new ConvergenceChecker<PointVectorValuePair>() {
            @Override
            public boolean converged(int iteration, PointVectorValuePair previous, PointVectorValuePair current) {
                iterCount[0]++;
                return iterCount[0] >= 2;
            }
        };
        MockOptimizer opt = new MockOptimizer(false, target, weight, start, checker, null, null, false);
        PointVectorValuePair result = opt.doOptimize();
        double[] point = result.getPoint();
        assertEquals(0.36, point[0], 1e-10);
    }
}