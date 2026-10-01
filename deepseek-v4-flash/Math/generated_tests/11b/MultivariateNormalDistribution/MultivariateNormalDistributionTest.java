package org.apache.commons.math3.distribution;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.linear.NonPositiveDefiniteMatrixException;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularMatrixException;
import org.apache.commons.math3.random.Well19937c;
import org.junit.Test;

import static org.junit.Assert.*;

public class MultivariateNormalDistributionTest {

    // Tests dimension mismatch between means length and covariance matrix rows
    @Test(expected = DimensionMismatchException.class)
    public void testConstructor_meansArrayLengthMismatch_throwsDimensionMismatch() {
        new MultivariateNormalDistribution(new double[]{1, 2}, new double[3][3]);
    }

    // Tests dimension mismatch inside a covariance matrix row
    @Test(expected = DimensionMismatchException.class)
    public void testConstructor_covarianceRowLengthMismatch_throwsDimensionMismatch() {
        double[][] covariances = {{1, 2, 3}, {4, 5, 6}};
        new MultivariateNormalDistribution(new double[]{1, 2}, covariances);
    }

    // Tests non-positive-definite matrix (negative eigenvalue)
    @Test(expected = NonPositiveDefiniteMatrixException.class)
    public void testConstructor_negativeEigenvalue_throwsNonPositiveDefiniteMatrix() {
        double[][] covariances = {{1, 2}, {2, 1}};
        new MultivariateNormalDistribution(new double[]{0, 0}, covariances);
    }

    // Tests singular covariance matrix (zero eigenvalue)
    @Test(expected = SingularMatrixException.class)
    public void testConstructor_singularCovariance_throwsSingularMatrix() {
        double[][] covariances = {{1, 2}, {2, 4}};
        new MultivariateNormalDistribution(new double[]{0, 0}, covariances);
    }

    // Tests univariate density at the mean (zero variance identity case)
    @Test
    public void testDensity_univariateMeanZeroVarianceOne_atZero_returnsExpected() {
        MultivariateNormalDistribution dist = new MultivariateNormalDistribution(
            new double[]{0}, new double[][]{{1}});
        double expected = 1.0 / Math.sqrt(2.0 * Math.PI);
        assertEquals(expected, dist.density(new double[]{0}), 1e-10);
    }

    // Tests univariate density away from the mean
    @Test
    public void testDensity_univariateMeanZeroVarianceOne_atOne_returnsExpected() {
        MultivariateNormalDistribution dist = new MultivariateNormalDistribution(
            new double[]{0}, new double[][]{{1}});
        double expected = 1.0 / Math.sqrt(2.0 * Math.PI) * Math.exp(-0.5);
        assertEquals(expected, dist.density(new double[]{1}), 1e-10);
    }

    // Tests bivariate density with identity covariance at the origin
    @Test
    public void testDensity_bivariateIdentity_atZeroZero_returnsExpected() {
        MultivariateNormalDistribution dist = new MultivariateNormalDistribution(
            new double[]{0, 0}, new double[][]{{1, 0}, {0, 1}});
        double expected = 1.0 / (2.0 * Math.PI);
        assertEquals(expected, dist.density(new double[]{0, 0}), 1e-10);
    }

    // Tests bivariate density with a non-identity covariance at the origin
    @Test
    public void testDensity_bivariateNonIdentity_atZeroZero_returnsExpected() {
        double[][] cov = {{2, 0.5}, {0.5, 1}};
        MultivariateNormalDistribution dist = new MultivariateNormalDistribution(
            new double[]{0, 0}, cov);
        double det = 2.0 * 1.0 - 0.5 * 0.5;
        double expected = 1.0 / (2.0 * Math.PI * Math.sqrt(det));
        assertEquals(expected, dist.density(new double[]{0, 0}), 1e-10);
    }

    // Tests input dimension mismatch in density method
    @Test(expected = DimensionMismatchException.class)
    public void testDensity_inputLengthMismatch_throwsDimensionMismatch() {
        MultivariateNormalDistribution dist = new MultivariateNormalDistribution(
            new double[]{0, 0}, new double[][]{{1, 0}, {0, 1}});
        dist.density(new double[]{0});
    }

    // Tests that getMeans returns a defensive copy of the input means
    @Test
    public void testGetMeans_returnsCopyOfInput() {
        double[] means = {1, -2};
        double[][] cov = {{1, 0}, {0, 1}};
        MultivariateNormalDistribution dist = new MultivariateNormalDistribution(means, cov);
        means[0] = 99; // modify the original input
        assertArrayEquals(new double[]{1, -2}, dist.getMeans(), 1e-12);
    }

    // Tests that getCovariances returns a defensive copy with correct entries
    @Test
    public void testGetCovariances_returnsCopy() {
        MultivariateNormalDistribution dist = new MultivariateNormalDistribution(
            new double[]{0, 0}, new double[][]{{2, 0.5}, {0.5, 1}});
        RealMatrix cov = dist.getCovariances();
        assertEquals(2.0, cov.getEntry(0, 0), 1e-12);
        assertEquals(0.5, cov.getEntry(0, 1), 1e-12);
        assertEquals(0.5, cov.getEntry(1, 0), 1e-12);
        assertEquals(1.0, cov.getEntry(1, 1), 1e-12);
    }

    // Tests that getStandardDeviations returns the square roots of diagonal entries
    @Test
    public void testGetStandardDeviations_returnsSqrtDiagonal() {
        double[][] cov = {{4, 1}, {1, 9}};
        MultivariateNormalDistribution dist = new MultivariateNormalDistribution(
            new double[]{0, 0}, cov);
        assertArrayEquals(new double[]{2.0, 3.0}, dist.getStandardDeviations(), 1e-12);
    }

    // Tests that samples follow the specified mean and covariance within tolerance
    @Test
    public void testSample_returnsSamplesWithExpectedMeanAndCovariance() {
        Well19937c rng = new Well19937c(12345L);
        MultivariateNormalDistribution dist = new MultivariateNormalDistribution(
            rng, new double[]{1, 2}, new double[][]{{2, 0.5}, {0.5, 1}});
        final int n = 5000;
        double[] sum = new double[2];
        double[] sumSq = new double[3]; // [0] x0^2, [1] x0*x1, [2] x1^2
        for (int i = 0; i < n; i++) {
            double[] s = dist.sample();
            assertEquals(2, s.length);
            sum[0] += s[0];
            sum[1] += s[1];
            sumSq[0] += s[0] * s[0];
            sumSq[1] += s[0] * s[1];
            sumSq[2] += s[1] * s[1];
        }
        double mean0 = sum[0] / n;
        double mean1 = sum[1] / n;
        double var0 = sumSq[0] / n - mean0 * mean0;
        double cov01 = sumSq[1] / n - mean0 * mean1;
        double var1 = sumSq[2] / n - mean1 * mean1;

        assertEquals(1.0, mean0, 0.1);
        assertEquals(2.0, mean1, 0.1);
        assertEquals(2.0, var0, 0.2);
        assertEquals(0.5, cov01, 0.2);
        assertEquals(1.0, var1, 0.2);
    }
}