package org.apache.commons.math3.distribution;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.linear.NonPositiveDefiniteMatrixException;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.random.Well19937c;
import org.apache.commons.math3.util.FastMath;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MultivariateNormalDistributionTest {

    // Tests density calculation for 1D distribution (detects integer division bug in exponent)
    @Test
    public void testDensity_oneDimension_returnsCorrectDensity() {
        final double[] mu = { 2.0 };
        final double[][] sigma = { { 4.0 } };
        final MultivariateNormalDistribution dist = new MultivariateNormalDistribution(mu, sigma);

        final double expected = 1.0 / (FastMath.sqrt(2.0 * FastMath.PI) * 2.0);
        final double actual = dist.density(new double[]{ 2.0 });

        assertEquals(expected, actual, 1e-9);
    }

    // Tests density calculation for 2D distribution
    @Test
    public void testDensity_twoDimensions_returnsCorrectDensity() {
        final double[] mu = { 1.0, 2.0 };
        final double[][] sigma = { { 1.0, 0.0 }, { 0.0, 1.0 } };
        final MultivariateNormalDistribution dist = new MultivariateNormalDistribution(mu, sigma);

        final double expected = 1.0 / (2.0 * FastMath.PI);
        final double actual = dist.density(new double[]{ 1.0, 2.0 });

        assertEquals(expected, actual, 1e-9);
    }

    // Tests density calculation for 3D distribution (odd dimension)
    @Test
    public void testDensity_threeDimensions_returnsCorrectDensity() {
        final double[] mu = { 0.0, 0.0, 0.0 };
        final double[][] sigma = {
            { 1.0, 0.0, 0.0 },
            { 0.0, 1.0, 0.0 },
            { 0.0, 0.0, 1.0 }
        };
        final MultivariateNormalDistribution dist = new MultivariateNormalDistribution(mu, sigma);

        final double expected = FastMath.pow(2.0 * FastMath.PI, -1.5);
        final double actual = dist.density(new double[]{ 0.0, 0.0, 0.0 });

        assertEquals(expected, actual, 1e-9);
    }

    // Tests density off-center calculation with covariance
    @Test
    public void testDensity_offCenterWithCovariance_returnsCorrectDensity() {
        final double[] mu = { 0.0, 0.0 };
        final double[][] sigma = { { 2.0, 0.5 }, { 0.5, 1.0 } };
        final MultivariateNormalDistribution dist = new MultivariateNormalDistribution(mu, sigma);

        final double actual = dist.density(new double[]{ 0.5, -0.5 });
        final double det = 2.0 * 1.0 - 0.5 * 0.5; // 1.75
        // exponent: -0.5 * [0.5, -0.5] * inv(sigma) * [0.5, -0.5]^T
        // inv(sigma) = 1/1.75 * [[1.0, -0.5], [-0.5, 2.0]]
        // [0.5, -0.5] * [[1.0, -0.5], [-0.5, 2.0]] = [0.75, -1.25]
        // [0.75, -1.25] * [0.5, -0.5]^T = 0.375 + 0.625 = 1.0
        // exponent sum = 1.0 / 1.75
        final double expected = (1.0 / (2.0 * FastMath.PI * FastMath.sqrt(det))) * FastMath.exp(-0.5 * (1.0 / det));

        assertEquals(expected, actual, 1e-9);
    }

    // Tests exception when density input dimension does not match distribution dimension
    @Test(expected = DimensionMismatchException.class)
    public void testDensity_dimensionMismatch_throwsException() {
        final double[] mu = { 0.0, 0.0 };
        final double[][] sigma = { { 1.0, 0.0 }, { 0.0, 1.0 } };
        final MultivariateNormalDistribution dist = new MultivariateNormalDistribution(mu, sigma);

        dist.density(new double[]{ 0.0 });
    }

    // Tests exception when covariance row count does not match means dimension
    @Test(expected = DimensionMismatchException.class)
    public void testConstructor_covarianceRowCountMismatch_throwsException() {
        final double[] mu = { 0.0, 0.0 };
        final double[][] sigma = { { 1.0, 0.0 } };
        new MultivariateNormalDistribution(mu, sigma);
    }

    // Tests exception when covariance column count does not match means dimension
    @Test(expected = DimensionMismatchException.class)
    public void testConstructor_covarianceColCountMismatch_throwsException() {
        final double[] mu = { 0.0, 0.0 };
        final double[][] sigma = { { 1.0, 0.0, 0.0 }, { 0.0, 1.0 } };
        new MultivariateNormalDistribution(mu, sigma);
    }

    // Tests exception when covariance matrix is not positive definite
    @Test(expected = NonPositiveDefiniteMatrixException.class)
    public void testConstructor_nonPositiveDefiniteMatrix_throwsException() {
        final double[] mu = { 0.0, 0.0 };
        final double[][] sigma = { { 1.0, 2.0 }, { 2.0, 1.0 } }; // eigenvalues: 3.0 and -1.0
        new MultivariateNormalDistribution(mu, sigma);
    }

    // Tests getMeans returns defensive copy of mean vector
    @Test
    public void testGetMeans_returnsCorrectMeansAndIsDefensiveCopy() {
        final double[] mu = { 1.0, -2.5 };
        final double[][] sigma = { { 1.0, 0.0 }, { 0.0, 1.0 } };
        final MultivariateNormalDistribution dist = new MultivariateNormalDistribution(mu, sigma);

        final double[] retrievedMeans = dist.getMeans();
        assertArrayEquals(mu, retrievedMeans, 1e-9);

        retrievedMeans[0] = 99.0;
        assertEquals(1.0, dist.getMeans()[0], 1e-9);
    }

    // Tests getCovariances returns copy of covariance matrix
    @Test
    public void testGetCovariances_returnsCorrectMatrix() {
        final double[] mu = { 0.0, 0.0 };
        final double[][] sigma = { { 2.0, 0.3 }, { 0.3, 1.5 } };
        final MultivariateNormalDistribution dist = new MultivariateNormalDistribution(mu, sigma);

        final RealMatrix cov = dist.getCovariances();
        assertEquals(2.0, cov.getEntry(0, 0), 1e-9);
        assertEquals(0.3, cov.getEntry(0, 1), 1e-9);
        assertEquals(0.3, cov.getEntry(1, 0), 1e-9);
        assertEquals(1.5, cov.getEntry(1, 1), 1e-9);
    }

    // Tests getStandardDeviations returns square root of diagonal entries
    @Test
    public void testGetStandardDeviations_returnsSquareRootOfDiagonal() {
        final double[] mu = { 0.0, 0.0, 0.0 };
        final double[][] sigma = {
            { 4.0, 0.0, 0.0 },
            { 0.0, 9.0, 0.0 },
            { 0.0, 0.0, 16.0 }
        };
        final MultivariateNormalDistribution dist = new MultivariateNormalDistribution(mu, sigma);

        final double[] expectedStd = { 2.0, 3.0, 4.0 };
        assertArrayEquals(expectedStd, dist.getStandardDeviations(), 1e-9);
    }

    // Tests sample method returns non-null vector of correct dimension
    @Test
    public void testSample_returnsSampleOfCorrectDimension() {
        final double[] mu = { 1.0, 2.0, 3.0 };
        final double[][] sigma = {
            { 1.0, 0.0, 0.0 },
            { 0.0, 1.0, 0.0 },
            { 0.0, 0.0, 1.0 }
        };
        final MultivariateNormalDistribution dist =
            new MultivariateNormalDistribution(new Well19937c(42L), mu, sigma);

        final double[] sample = dist.sample();
        assertNotNull(sample);
        assertEquals(3, sample.length);
    }

    // Tests getDimension returns correct dimension
    @Test
    public void testGetDimension_returnsCorrectDimension() {
        final double[] mu = { 1.0, 2.0 };
        final double[][] sigma = { { 1.0, 0.0 }, { 0.0, 1.0 } };
        final MultivariateNormalDistribution dist = new MultivariateNormalDistribution(mu, sigma);

        assertEquals(2, dist.getDimension());
    }
}