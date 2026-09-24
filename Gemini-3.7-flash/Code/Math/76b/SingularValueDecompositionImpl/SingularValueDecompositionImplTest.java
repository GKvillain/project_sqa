package org.apache.commons.math.linear;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

public class SingularValueDecompositionImplTest {

    private static final double EPSILON = 1e-10;

    // Helper method to verify A = U * S * V^T
    private void checkRecomposition(RealMatrix matrix, SingularValueDecomposition svd) {
        RealMatrix u = svd.getU();
        RealMatrix s = svd.getS();
        RealMatrix vt = svd.getVT();
        RealMatrix reconstructed = u.multiply(s).multiply(vt);
        
        assertEquals(matrix.getRowDimension(), reconstructed.getRowDimension());
        assertEquals(matrix.getColumnDimension(), reconstructed.getColumnDimension());
        
        for (int i = 0; i < matrix.getRowDimension(); ++i) {
            for (int j = 0; j < matrix.getColumnDimension(); ++j) {
                assertEquals(matrix.getEntry(i, j), reconstructed.getEntry(i, j), EPSILON);
            }
        }
    }

    // Tests square matrix decomposition (m == n)
    @Test
    public void testDecomposition_squareMatrix_recomposesAccurately() {
        double[][] data = {
            { 1.0, 2.0, 3.0 },
            { 2.0, 5.0, 6.0 },
            { 3.0, 6.0, 9.0 }
        };
        RealMatrix matrix = MatrixUtils.createRealMatrix(data);
        SingularValueDecomposition svd = new SingularValueDecompositionImpl(matrix);
        checkRecomposition(matrix, svd);
    }

    // Tests tall rectangular matrix decomposition (m > n)
    @Test
    public void testDecomposition_tallMatrix_recomposesAccurately() {
        double[][] data = {
            { 1.0, 2.0 },
            { 3.0, 4.0 },
            { 5.0, 6.0 },
            { 7.0, 8.0 }
        };
        RealMatrix matrix = MatrixUtils.createRealMatrix(data);
        SingularValueDecomposition svd = new SingularValueDecompositionImpl(matrix);
        checkRecomposition(matrix, svd);
    }

    // Tests wide rectangular matrix decomposition (m < n)
    @Test
    public void testDecomposition_wideMatrix_recomposesAccurately() {
        double[][] data = {
            { 1.0, 2.0, 3.0, 4.0 },
            { 5.0, 6.0, 7.0, 8.0 }
        };
        RealMatrix matrix = MatrixUtils.createRealMatrix(data);
        SingularValueDecomposition svd = new SingularValueDecompositionImpl(matrix);
        checkRecomposition(matrix, svd);
    }

    // Tests truncated SVD constructor with max singular values limit
    @Test
    public void testConstructor_maxSingularValues_truncatesCorrectly() {
        double[][] data = {
            { 1.0, 2.0, 3.0 },
            { 4.0, 5.0, 6.0 },
            { 7.0, 8.0, 10.0 }
        };
        RealMatrix matrix = MatrixUtils.createRealMatrix(data);
        SingularValueDecomposition svd = new SingularValueDecompositionImpl(matrix, 2);
        
        assertEquals(2, svd.getSingularValues().length);
        assertEquals(2, svd.getS().getRowDimension());
        assertEquals(2, svd.getS().getColumnDimension());
    }

    // Tests getter methods and caching for U, UT, V, VT, S
    @Test
    public void testGetters_cachingBehavior_returnsSameInstances() {
        double[][] data = {
            { 1.0, 0.0 },
            { 0.0, 2.0 }
        };
        RealMatrix matrix = MatrixUtils.createRealMatrix(data);
        SingularValueDecomposition svd = new SingularValueDecompositionImpl(matrix);

        RealMatrix u1 = svd.getU();
        RealMatrix u2 = svd.getU();
        assertSame(u1, u2);

        RealMatrix ut1 = svd.getUT();
        RealMatrix ut2 = svd.getUT();
        assertSame(ut1, ut2);

        RealMatrix v1 = svd.getV();
        RealMatrix v2 = svd.getV();
        assertSame(v1, v2);

        RealMatrix vt1 = svd.getVT();
        RealMatrix vt2 = svd.getVT();
        assertSame(vt1, vt2);

        RealMatrix s1 = svd.getS();
        RealMatrix s2 = svd.getS();
        assertSame(s1, s2);
    }

    // Tests singular values, norm, and condition number for identity matrix
    @Test
    public void testNormAndConditionNumber_identityMatrix_returnsOne() {
        RealMatrix identity = MatrixUtils.createRealIdentityMatrix(3);
        SingularValueDecomposition svd = new SingularValueDecompositionImpl(identity);

        assertEquals(1.0, svd.getNorm(), EPSILON);
        assertEquals(1.0, svd.getConditionNumber(), EPSILON);
        assertEquals(3, svd.getRank());

        double[] singularValues = svd.getSingularValues();
        assertEquals(3, singularValues.length);
        assertEquals(1.0, singularValues[0], EPSILON);
        assertEquals(1.0, singularValues[1], EPSILON);
        assertEquals(1.0, singularValues[2], EPSILON);
    }

    // Tests rank computation for rank-deficient matrix
    @Test
    public void testGetRank_rankDeficientMatrix_returnsCorrectRank() {
        double[][] data = {
            { 1.0, 2.0, 3.0 },
            { 2.0, 4.0, 6.0 },
            { 3.0, 6.0, 9.0 }
        };
        RealMatrix matrix = MatrixUtils.createRealMatrix(data);
        SingularValueDecomposition svd = new SingularValueDecompositionImpl(matrix);

        assertEquals(1, svd.getRank());
    }

    // Tests covariance matrix calculation
    @Test
    public void testGetCovariance_validMinSingularValue_returnsCovarianceMatrix() {
        double[][] data = {
            { 1.0, 0.0, 0.0 },
            { 0.0, 2.0, 0.0 },
            { 0.0, 0.0, 3.0 }
        };
        RealMatrix matrix = MatrixUtils.createRealMatrix(data);
        SingularValueDecomposition svd = new SingularValueDecompositionImpl(matrix);

        RealMatrix cov = svd.getCovariance(1.5);
        assertNotNull(cov);
        assertEquals(3, cov.getRowDimension());
        assertEquals(3, cov.getColumnDimension());
    }

    // Tests covariance with minSingularValue exceeding the largest singular value
    @Test(expected = IllegalArgumentException.class)
    public void testGetCovariance_cutoffTooLarge_throwsIllegalArgumentException() {
        double[][] data = {
            { 1.0, 0.0 },
            { 0.0, 2.0 }
        };
        RealMatrix matrix = MatrixUtils.createRealMatrix(data);
        SingularValueDecomposition svd = new SingularValueDecompositionImpl(matrix);

        svd.getCovariance(100.0);
    }

    // Tests solver with double array for non-singular system
    @Test
    public void testSolver_doubleArray_solvesLinearSystem() {
        double[][] aData = {
            { 1.0, 2.0 },
            { 3.0, 4.0 }
        };
        RealMatrix a = MatrixUtils.createRealMatrix(aData);
        SingularValueDecomposition svd = new SingularValueDecompositionImpl(a);
        DecompositionSolver solver = svd.getSolver();

        assertTrue(solver.isNonSingular());

        double[] b = { 5.0, 11.0 }; // Expected x = [1.0, 2.0]
        double[] x = solver.solve(b);

        assertEquals(1.0, x[0], EPSILON);
        assertEquals(2.0, x[1], EPSILON);
    }

    // Tests solver with RealVector
    @Test
    public void testSolver_realVector_solvesCorrectly() {
        double[][] aData = {
            { 2.0, 0.0 },
            { 0.0, 4.0 }
        };
        RealMatrix a = MatrixUtils.createRealMatrix(aData);
        SingularValueDecomposition svd = new SingularValueDecompositionImpl(a);
        DecompositionSolver solver = svd.getSolver();

        RealVector b = new ArrayRealVector(new double[] { 4.0, 8.0 });
        RealVector x = solver.solve(b);

        assertEquals(2.0, x.getEntry(0), EPSILON);
        assertEquals(2.0, x.getEntry(1), EPSILON);
    }

    // Tests solver with RealMatrix and getInverse
    @Test
    public void testSolver_realMatrixAndInverse_returnsInverse() {
        double[][] aData = {
            { 1.0, 2.0 },
            { 3.0, 4.0 }
        };
        RealMatrix a = MatrixUtils.createRealMatrix(aData);
        SingularValueDecomposition svd = new SingularValueDecompositionImpl(a);
        DecompositionSolver solver = svd.getSolver();

        RealMatrix inverse = solver.getInverse();
        RealMatrix identity = a.multiply(inverse);

        assertEquals(1.0, identity.getEntry(0, 0), EPSILON);
        assertEquals(0.0, identity.getEntry(0, 1), EPSILON);
        assertEquals(0.0, identity.getEntry(1, 0), EPSILON);
        assertEquals(1.0, identity.getEntry(1, 1), EPSILON);

        RealMatrix b = MatrixUtils.createRealIdentityMatrix(2);
        RealMatrix x = solver.solve(b);
        for (int i = 0; i < 2; ++i) {
            for (int j = 0; j < 2; ++j) {
                assertEquals(inverse.getEntry(i, j), x.getEntry(i, j), EPSILON);
            }
        }
    }

    // Tests solver with singular matrix indicator
    @Test
    public void testSolver_singularMatrix_indicatesSingular() {
        double[][] aData = {
            { 1.0, 2.0 },
            { 2.0, 4.0 }
        };
        RealMatrix a = MatrixUtils.createRealMatrix(aData);
        SingularValueDecomposition svd = new SingularValueDecompositionImpl(a);
        DecompositionSolver solver = svd.getSolver();

        assertFalse(solver.isNonSingular());
    }

    // Tests solver with least squares over-determined system
    @Test
    public void testSolver_overDeterminedSystem_findsLeastSquaresSolution() {
        double[][] aData = {
            { 1.0, 0.0 },
            { 0.0, 1.0 },
            { 0.0, 0.0 }
        };
        RealMatrix a = MatrixUtils.createRealMatrix(aData);
        SingularValueDecomposition svd = new SingularValueDecompositionImpl(a);
        DecompositionSolver solver = svd.getSolver();

        double[] b = { 3.0, 4.0, 5.0 };
        double[] x = solver.solve(b);

        assertEquals(3.0, x[0], EPSILON);
        assertEquals(4.0, x[1], EPSILON);
    }
}