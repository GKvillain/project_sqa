package org.apache.commons.math.linear;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

public class CholeskyDecompositionImplTest {

    private RealMatrix testMatrix;

    @Before
    public void setUp() {
        // L = {{2, 0, 0}, {6, 1, 0}, {-8, 5, 3}}
        // A = L * L^T
        testMatrix = MatrixUtils.createRealMatrix(new double[][] {
            { 4.0,  12.0, -16.0 },
            { 12.0, 37.0, -43.0 },
            { -16.0, -43.0, 98.0 }
        });
    }

    // Tests non-square matrix input throws NonSquareMatrixException
    @Test(expected = NonSquareMatrixException.class)
    public void testConstructor_nonSquareMatrix_throwsException() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 1.0, 2.0, 3.0 },
            { 2.0, 5.0, 6.0 }
        });
        new CholeskyDecompositionImpl(matrix);
    }

    // Tests non-symmetric matrix input throws NotSymmetricMatrixException
    @Test(expected = NotSymmetricMatrixException.class)
    public void testConstructor_notSymmetricMatrix_throwsException() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 4.0, 1.0 },
            { 2.0, 4.0 }
        });
        new CholeskyDecompositionImpl(matrix);
    }

    // Tests matrix with non-positive diagonal element before transformation throws NotPositiveDefiniteMatrixException
    @Test(expected = NotPositiveDefiniteMatrixException.class)
    public void testConstructor_negativeDiagonalElement_throwsException() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { -1.0, 0.0 },
            { 0.0, 2.0 }
        });
        new CholeskyDecompositionImpl(matrix);
    }

    // Tests matrix with zero diagonal element throws NotPositiveDefiniteMatrixException
    @Test(expected = NotPositiveDefiniteMatrixException.class)
    public void testConstructor_zeroDiagonalElement_throwsException() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 0.0, 0.0 },
            { 0.0, 2.0 }
        });
        new CholeskyDecompositionImpl(matrix);
    }

    // Tests symmetric matrix with positive diagonal but not positive definite throws NotPositiveDefiniteMatrixException (Math-86)
    @Test(expected = NotPositiveDefiniteMatrixException.class)
    public void testConstructor_notPositiveDefiniteMatrix_throwsException() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 1.0, 2.0 },
            { 2.0, 1.0 }
        });
        new CholeskyDecompositionImpl(matrix);
    }

    // Tests 3x3 not positive definite matrix throws NotPositiveDefiniteMatrixException
    @Test(expected = NotPositiveDefiniteMatrixException.class)
    public void testConstructor_notPositiveDefinite3x3_throwsException() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 1.0, 2.0, 4.0 },
            { 2.0, 13.0, 23.0 },
            { 4.0, 23.0, 30.0 }
        });
        new CholeskyDecompositionImpl(matrix);
    }

    // Tests constructor with custom thresholds
    @Test
    public void testConstructor_customThresholds_success() {
        CholeskyDecomposition cholesky = new CholeskyDecompositionImpl(testMatrix, 1.0e-10, 1.0e-8);
        assertNotNull(cholesky.getL());
    }

    // Tests getL and getLT reconstruct the original matrix A = L * L^T
    @Test
    public void testGetLAndGetLT_validMatrix_reconstructsOriginalMatrix() {
        CholeskyDecomposition cholesky = new CholeskyDecompositionImpl(testMatrix);
        RealMatrix l = cholesky.getL();
        RealMatrix lT = cholesky.getLT();

        // Check L * L^T == testMatrix
        RealMatrix reconstructed = l.multiply(lT);
        assertEquals(testMatrix.getRowDimension(), reconstructed.getRowDimension());
        assertEquals(testMatrix.getColumnDimension(), reconstructed.getColumnDimension());
        for (int i = 0; i < testMatrix.getRowDimension(); ++i) {
            for (int j = 0; j < testMatrix.getColumnDimension(); ++j) {
                assertEquals(testMatrix.getEntry(i, j), reconstructed.getEntry(i, j), 1.0e-12);
            }
        }

        // Check L is lower triangular
        for (int i = 0; i < l.getRowDimension(); ++i) {
            for (int j = i + 1; j < l.getColumnDimension(); ++j) {
                assertEquals(0.0, l.getEntry(i, j), 1.0e-12);
            }
        }

        // Check cached returns
        assertSame(l, cholesky.getL());
        assertSame(lT, cholesky.getLT());
    }

    // Tests getDeterminant calculation
    @Test
    public void testGetDeterminant_validMatrix_returnsCorrectDeterminant() {
        CholeskyDecomposition cholesky = new CholeskyDecompositionImpl(testMatrix);
        // Determinant = (2 * 1 * 3)^2 = 36
        assertEquals(36.0, cholesky.getDeterminant(), 1.0e-12);
    }

    // Tests solver isNonSingular always returns true for decomposed matrix
    @Test
    public void testSolver_isNonSingular_returnsTrue() {
        DecompositionSolver solver = new CholeskyDecompositionImpl(testMatrix).getSolver();
        assertTrue(solver.isNonSingular());
    }

    // Tests solver solve with double[] array
    @Test
    public void testSolver_solveArray_returnsCorrectSolution() {
        DecompositionSolver solver = new CholeskyDecompositionImpl(testMatrix).getSolver();
        double[] b = new double[] { 4.0, 12.0, -16.0 };
        double[] x = solver.solve(b);

        assertEquals(3, x.length);
        assertEquals(1.0, x[0], 1.0e-12);
        assertEquals(0.0, x[1], 1.0e-12);
        assertEquals(0.0, x[2], 1.0e-12);
    }

    // Tests solver solve with double[] vector length mismatch
    @Test(expected = IllegalArgumentException.class)
    public void testSolver_solveArrayDimensionMismatch_throwsException() {
        DecompositionSolver solver = new CholeskyDecompositionImpl(testMatrix).getSolver();
        solver.solve(new double[] { 1.0, 2.0 });
    }

    // Tests solver solve with RealVector
    @Test
    public void testSolver_solveRealVector_returnsCorrectSolution() {
        DecompositionSolver solver = new CholeskyDecompositionImpl(testMatrix).getSolver();
        RealVector b = new ArrayRealVector(new double[] { 4.0, 12.0, -16.0 });
        RealVector x = solver.solve(b);

        assertEquals(1.0, x.getEntry(0), 1.0e-12);
        assertEquals(0.0, x.getEntry(1), 1.0e-12);
        assertEquals(0.0, x.getEntry(2), 1.0e-12);
    }

    // Tests solver solve with RealVector dimension mismatch
    @Test(expected = IllegalArgumentException.class)
    public void testSolver_solveRealVectorDimensionMismatch_throwsException() {
        DecompositionSolver solver = new CholeskyDecompositionImpl(testMatrix).getSolver();
        solver.solve(new ArrayRealVector(new double[] { 1.0, 2.0 }));
    }

    // Tests solver solve with RealMatrix
    @Test
    public void testSolver_solveRealMatrix_returnsCorrectSolution() {
        DecompositionSolver solver = new CholeskyDecompositionImpl(testMatrix).getSolver();
        RealMatrix b = MatrixUtils.createRealIdentityMatrix(3);
        RealMatrix inverse = solver.solve(b);

        RealMatrix identity = testMatrix.multiply(inverse);
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                double expected = (i == j) ? 1.0 : 0.0;
                assertEquals(expected, identity.getEntry(i, j), 1.0e-12);
            }
        }
    }

    // Tests solver solve with RealMatrix dimension mismatch
    @Test(expected = IllegalArgumentException.class)
    public void testSolver_solveRealMatrixDimensionMismatch_throwsException() {
        DecompositionSolver solver = new CholeskyDecompositionImpl(testMatrix).getSolver();
        RealMatrix b = MatrixUtils.createRealIdentityMatrix(2);
        solver.solve(b);
    }

    // Tests solver getInverse
    @Test
    public void testSolver_getInverse_returnsInverseMatrix() {
        DecompositionSolver solver = new CholeskyDecompositionImpl(testMatrix).getSolver();
        RealMatrix inverse = solver.getInverse();

        RealMatrix identity = testMatrix.multiply(inverse);
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                double expected = (i == j) ? 1.0 : 0.0;
                assertEquals(expected, identity.getEntry(i, j), 1.0e-12);
            }
        }
    }

    // Tests solver solve with ArrayRealVector specific overload
    @Test
    public void testSolver_solveArrayRealVector_returnsCorrectSolution() {
        DecompositionSolver solver = new CholeskyDecompositionImpl(testMatrix).getSolver();
        ArrayRealVector b = new ArrayRealVector(new double[] { 4.0, 12.0, -16.0 });
        ArrayRealVector x = (ArrayRealVector) solver.solve(b);

        assertEquals(1.0, x.getEntry(0), 1.0e-12);
        assertEquals(0.0, x.getEntry(1), 1.0e-12);
        assertEquals(0.0, x.getEntry(2), 1.0e-12);
    }
}