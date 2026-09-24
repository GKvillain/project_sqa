package org.apache.commons.math.estimation;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class AbstractEstimatorTest {

    private TestEstimator estimator;

    @Before
    public void setUp() {
        estimator = new TestEstimator();
    }

    // Tests getter and setter for maxCostEval and initial evaluation counters
    @Test
    public void testSetMaxCostEval_andInitialCounters() {
        estimator.setMaxCostEval(100);
        assertEquals(0, estimator.getCostEvaluations());
        assertEquals(0, estimator.getJacobianEvaluations());
    }

    // Tests updateResidualsAndCost normal calculation
    @Test
    public void testUpdateResidualsAndCost_normalExecution() throws Exception {
        SimpleEstimationProblem problem = new SimpleEstimationProblem();
        EstimatedParameter p1 = new EstimatedParameter("p1", 2.0);
        problem.addParameter(p1);

        // Theoretical value = 2.0 * 2.0 = 4.0; measured = 5.0; residual = 5.0 - 4.0 = 1.0; weight = 4.0
        problem.addMeasurement(new LinearMeasurement(4.0, 5.0, new EstimatedParameter[] { p1 }, new double[] { 2.0 }));

        estimator.setMaxCostEval(10);
        estimator.initializeEstimate(problem);
        estimator.updateResidualsAndCost();

        assertEquals(1, estimator.getCostEvaluations());
        // residual = sqrt(4.0) * (5.0 - 4.0) = 2.0
        assertEquals(2.0, estimator.getResiduals()[0], 1e-10);
        // cost = sqrt(4.0 * 1.0^2) = 2.0
        assertEquals(2.0, estimator.getCost(), 1e-10);
    }

    // Tests updateResidualsAndCost when max cost evaluations is exceeded
    @Test(expected = EstimationException.class)
    public void testUpdateResidualsAndCost_maxCostEvaluationsExceeded_throwsException() throws Exception {
        SimpleEstimationProblem problem = new SimpleEstimationProblem();
        EstimatedParameter p1 = new EstimatedParameter("p1", 1.0);
        problem.addParameter(p1);
        problem.addMeasurement(new LinearMeasurement(1.0, 1.0, new EstimatedParameter[] { p1 }, new double[] { 1.0 }));

        estimator.setMaxCostEval(0);
        estimator.initializeEstimate(problem);
        estimator.updateResidualsAndCost();
    }

    // Tests updateJacobian calculation and counter increment
    @Test
    public void testUpdateJacobian_normalExecution() {
        SimpleEstimationProblem problem = new SimpleEstimationProblem();
        EstimatedParameter p1 = new EstimatedParameter("p1", 1.0);
        EstimatedParameter p2 = new EstimatedParameter("p2", 2.0);
        problem.addParameter(p1);
        problem.addParameter(p2);

        // Weight = 4.0 -> factor = -sqrt(4.0) = -2.0; partials = [3.0, 5.0] -> jacobian row = [-6.0, -10.0]
        problem.addMeasurement(new LinearMeasurement(4.0, 10.0, new EstimatedParameter[] { p1, p2 }, new double[] { 3.0, 5.0 }));

        estimator.initializeEstimate(problem);
        estimator.updateJacobian();

        assertEquals(1, estimator.getJacobianEvaluations());
        double[] jac = estimator.getJacobian();
        assertEquals(-6.0, jac[0], 1e-10);
        assertEquals(-10.0, jac[1], 1e-10);
    }

    // Tests getRMS calculation
    @Test
    public void testGetRMS_normalExecution() {
        SimpleEstimationProblem problem = new SimpleEstimationProblem();
        EstimatedParameter p1 = new EstimatedParameter("p1", 1.0);
        problem.addParameter(p1);

        // m1: weight=1.0, residual=2.0 -> weighted sq = 1.0 * 4 = 4
        problem.addMeasurement(new LinearMeasurement(1.0, 3.0, new EstimatedParameter[] { p1 }, new double[] { 1.0 }));
        // m2: weight=2.0, residual=3.0 -> weighted sq = 2.0 * 9 = 18
        problem.addMeasurement(new LinearMeasurement(2.0, 5.0, new EstimatedParameter[] { p1 }, new double[] { 2.0 }));

        // RMS = sqrt((4 + 18) / 2) = sqrt(11)
        double rms = estimator.getRMS(problem);
        assertEquals(Math.sqrt(11.0), rms, 1e-10);
    }

    // Tests getChiSquare calculation
    @Test
    public void testGetChiSquare_normalExecution() {
        SimpleEstimationProblem problem = new SimpleEstimationProblem();
        EstimatedParameter p1 = new EstimatedParameter("p1", 1.0);
        problem.addParameter(p1);

        // m1: residual=2.0, weight=4.0 -> residual^2 / weight = 4.0 / 4.0 = 1.0
        problem.addMeasurement(new LinearMeasurement(4.0, 3.0, new EstimatedParameter[] { p1 }, new double[] { 1.0 }));
        // m2: residual=6.0, weight=2.0 -> residual^2 / weight = 36.0 / 2.0 = 18.0
        problem.addMeasurement(new LinearMeasurement(2.0, 8.0, new EstimatedParameter[] { p1 }, new double[] { 2.0 }));

        double chiSquare = estimator.getChiSquare(problem);
        assertEquals(19.0, chiSquare, 1e-10);
    }

    // Tests getCovariances with well-conditioned matrix
    @Test
    public void testGetCovariances_normalExecution() throws Exception {
        SimpleEstimationProblem problem = new SimpleEstimationProblem();
        EstimatedParameter p1 = new EstimatedParameter("p1", 0.0);
        EstimatedParameter p2 = new EstimatedParameter("p2", 0.0);
        problem.addParameter(p1);
        problem.addParameter(p2);

        // Row 1: partials [1, 0], weight 1.0 -> jacobian row [-1, 0]
        problem.addMeasurement(new LinearMeasurement(1.0, 1.0, new EstimatedParameter[] { p1, p2 }, new double[] { 1.0, 0.0 }));
        // Row 2: partials [0, 2], weight 1.0 -> jacobian row [0, -2]
        problem.addMeasurement(new LinearMeasurement(1.0, 2.0, new EstimatedParameter[] { p1, p2 }, new double[] { 0.0, 2.0 }));

        estimator.initializeEstimate(problem);
        double[][] cov = estimator.getCovariances(problem);

        assertNotNull(cov);
        assertEquals(2, cov.length);
        assertEquals(2, cov[0].length);
        // J^T J = diag(1, 4), inverse = diag(1, 0.25)
        assertEquals(1.0, cov[0][0], 1e-10);
        assertEquals(0.0, cov[0][1], 1e-10);
        assertEquals(0.0, cov[1][0], 1e-10);
        assertEquals(0.25, cov[1][1], 1e-10);
    }

    // Tests getCovariances with singular matrix throwing EstimationException
    @Test(expected = EstimationException.class)
    public void testGetCovariances_singularMatrix_throwsException() throws Exception {
        SimpleEstimationProblem problem = new SimpleEstimationProblem();
        EstimatedParameter p1 = new EstimatedParameter("p1", 0.0);
        EstimatedParameter p2 = new EstimatedParameter("p2", 0.0);
        problem.addParameter(p1);
        problem.addParameter(p2);

        // Dependent measurements leading to singular J^T J
        problem.addMeasurement(new LinearMeasurement(1.0, 1.0, new EstimatedParameter[] { p1, p2 }, new double[] { 1.0, 1.0 }));
        problem.addMeasurement(new LinearMeasurement(1.0, 2.0, new EstimatedParameter[] { p1, p2 }, new double[] { 2.0, 2.0 }));

        estimator.initializeEstimate(problem);
        estimator.getCovariances(problem);
    }

    // Tests guessParametersErrors when measurements are lesser than or equal to parameters
    @Test(expected = EstimationException.class)
    public void testGuessParametersErrors_noDegreesOfFreedom_throwsException() throws Exception {
        SimpleEstimationProblem problem = new SimpleEstimationProblem();
        EstimatedParameter p1 = new EstimatedParameter("p1", 0.0);
        EstimatedParameter p2 = new EstimatedParameter("p2", 0.0);
        problem.addParameter(p1);
        problem.addParameter(p2);

        problem.addMeasurement(new LinearMeasurement(1.0, 1.0, new EstimatedParameter[] { p1, p2 }, new double[] { 1.0, 0.0 }));

        estimator.initializeEstimate(problem);
        estimator.guessParametersErrors(problem);
    }

    // Tests guessParametersErrors normal calculation
    @Test
    public void testGuessParametersErrors_normalExecution() throws Exception {
        SimpleEstimationProblem problem = new SimpleEstimationProblem();
        EstimatedParameter p1 = new EstimatedParameter("p1", 0.0);
        problem.addParameter(p1);

        // 2 measurements, 1 parameter -> m > p (degrees of freedom = 1)
        problem.addMeasurement(new LinearMeasurement(1.0, 2.0, new EstimatedParameter[] { p1 }, new double[] { 1.0 }));
        problem.addMeasurement(new LinearMeasurement(1.0, 0.0, new EstimatedParameter[] { p1 }, new double[] { 1.0 }));

        estimator.initializeEstimate(problem);
        double[] errors = estimator.guessParametersErrors(problem);

        assertNotNull(errors);
        assertEquals(1, errors.length);
        assertTrue(errors[0] > 0);
    }

    // Tests handling of bound and unbound parameters in covariances and parameter errors estimation
    @Test
    public void testBoundParameters_getCovariancesAndErrors() throws Exception {
        SimpleEstimationProblem problem = new SimpleEstimationProblem();
        EstimatedParameter p1 = new EstimatedParameter("p1", 1.0, false); // unbound
        EstimatedParameter p2 = new EstimatedParameter("p2", 2.0, true);  // bound
        problem.addParameter(p1);
        problem.addParameter(p2);

        // Measurements dependent on unbound parameter p1 and bound parameter p2
        problem.addMeasurement(new LinearMeasurement(1.0, 1.0, new EstimatedParameter[] { p1, p2 }, new double[] { 1.0, 2.0 }));
        problem.addMeasurement(new LinearMeasurement(1.0, 3.0, new EstimatedParameter[] { p1, p2 }, new double[] { 2.0, 1.0 }));

        estimator.initializeEstimate(problem);

        try {
            double[][] cov = estimator.getCovariances(problem);
            assertNotNull(cov);
            // cov should match unbound parameters dimension (1x1)
            assertEquals(1, cov.length);
            assertEquals(1, cov[0].length);

            double[] errors = estimator.guessParametersErrors(problem);
            assertNotNull(errors);
            assertEquals(1, errors.length);
        } catch (ArrayIndexOutOfBoundsException aioobe) {
            fail("Should handle bound parameters without ArrayIndexOutOfBoundsException: " + aioobe.getMessage());
        }
    }

    // Helper concrete implementation of AbstractEstimator for white-box testing
    private static class TestEstimator extends AbstractEstimator {

        public void estimate(EstimationProblem problem) throws EstimationException {
            initializeEstimate(problem);
        }

        public double[] getJacobian() {
            return jacobian;
        }

        public double[] getResiduals() {
            return residuals;
        }

        public double getCost() {
            return cost;
        }

        @Override
        public void initializeEstimate(EstimationProblem problem) {
            super.initializeEstimate(problem);
        }

        @Override
        public void updateResidualsAndCost() throws EstimationException {
            super.updateResidualsAndCost();
        }

        @Override
        public void updateJacobian() {
            super.updateJacobian();
        }
    }

    // Helper concrete implementation of WeightedMeasurement for testing
    private static class LinearMeasurement extends WeightedMeasurement {

        private static final long serialVersionUID = 1L;
        private final EstimatedParameter[] params;
        private final double[] factors;

        public LinearMeasurement(double weight, double measuredValue,
                                 EstimatedParameter[] params, double[] factors) {
            super(weight, measuredValue);
            this.params = params;
            this.factors = factors;
        }

        public double getTheoreticalValue() {
            double value = 0.0;
            for (int i = 0; i < params.length; ++i) {
                value += factors[i] * params[i].getEstimate();
            }
            return value;
        }

        public double getPartial(EstimatedParameter parameter) {
            for (int i = 0; i < params.length; ++i) {
                if (params[i] == parameter) {
                    return factors[i];
                }
            }
            return 0.0;
        }
    }
}