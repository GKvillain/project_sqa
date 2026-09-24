package org.apache.commons.math3.linear;

import org.junit.Test;
import static org.junit.Assert.*;

public class RectangularCholeskyDecompositionTest {

    // Tests simple 2x2 positive definite matrix
    @Test
    public void testRectangularCholeskyDecomposition_positiveDefinite2x2_correctDecomposition() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 2.0, 1.0 },
            { 1.0, 2.0 }
        });
        RectangularCholeskyDecomposition rcd = new RectangularCholeskyDecomposition(matrix, 1.0e-10);
        assertEquals(2, rcd.getRank());

        RealMatrix root = rcd.getRootMatrix();
        assertEquals(2, root.getRowDimension());
        assertEquals(2, root.getColumnDimension());

        RealMatrix reconstructed = root.multiply(root.transpose());
        checkMatrixDifference(matrix, reconstructed, 1.0e-10);
    }

    // Tests 3x3 matrix requiring diagonal swap and permutation
    @Test
    public void testRectangularCholeskyDecomposition_diagonalPermutationNeeded_reconstructsOriginalMatrix() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 1.0, 1.0, 1.0 },
            { 1.0, 3.0, 2.0 },
            { 1.0, 2.0, 4.0 }
        });
        RectangularCholeskyDecomposition rcd = new RectangularCholeskyDecomposition(matrix, 1.0e-10);
        assertEquals(3, rcd.getRank());

        RealMatrix root = rcd.getRootMatrix();
        RealMatrix reconstructed = root.multiply(root.transpose());
        checkMatrixDifference(matrix, reconstructed, 1.0e-10);
    }

    // Tests 4x4 matrix with arbitrary diagonal order
    @Test
    public void testRectangularCholeskyDecomposition_multipleSwaps_reconstructsOriginalMatrix() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 1.0, 0.5, 0.2, 0.1 },
            { 0.5, 5.0, 1.0, 0.3 },
            { 0.2, 1.0, 3.0, 0.4 },
            { 0.1, 0.3, 0.4, 7.0 }
        });
        RectangularCholeskyDecomposition rcd = new RectangularCholeskyDecomposition(matrix, 1.0e-10);
        assertEquals(4, rcd.getRank());

        RealMatrix root = rcd.getRootMatrix();
        RealMatrix reconstructed = root.multiply(root.transpose());
        checkMatrixDifference(matrix, reconstructed, 1.0e-10);
    }

    // Tests rank-deficient positive semidefinite matrix
    @Test
    public void testRectangularCholeskyDecomposition_rankDeficientMatrix_computesCorrectRank() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 1.0, 2.0, 3.0 },
            { 2.0, 4.0, 6.0 },
            { 3.0, 6.0, 9.0 }
        });
        RectangularCholeskyDecomposition rcd = new RectangularCholeskyDecomposition(matrix, 1.0e-10);
        assertEquals(1, rcd.getRank());

        RealMatrix root = rcd.getRootMatrix();
        assertEquals(3, root.getRowDimension());
        assertEquals(1, root.getColumnDimension());

        RealMatrix reconstructed = root.multiply(root.transpose());
        checkMatrixDifference(matrix, reconstructed, 1.0e-10);
    }

    // Tests rank 2 matrix out of 3 dimensions
    @Test
    public void testRectangularCholeskyDecomposition_rank2Of3_reconstructsMatrix() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 1.0, 1.0, 2.0 },
            { 1.0, 2.0, 3.0 },
            { 2.0, 3.0, 5.0 }
        });
        RectangularCholeskyDecomposition rcd = new RectangularCholeskyDecomposition(matrix, 1.0e-10);
        assertEquals(2, rcd.getRank());

        RealMatrix root = rcd.getRootMatrix();
        assertEquals(3, root.getRowDimension());
        assertEquals(2, root.getColumnDimension());

        RealMatrix reconstructed = root.multiply(root.transpose());
        checkMatrixDifference(matrix, reconstructed, 1.0e-10);
    }

    // Tests 1x1 matrix
    @Test
    public void testRectangularCholeskyDecomposition_singleElement_computesCorrectRoot() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 4.0 }
        });
        RectangularCholeskyDecomposition rcd = new RectangularCholeskyDecomposition(matrix, 1.0e-10);
        assertEquals(1, rcd.getRank());

        RealMatrix root = rcd.getRootMatrix();
        assertEquals(1, root.getRowDimension());
        assertEquals(1, root.getColumnDimension());
        assertEquals(2.0, root.getEntry(0, 0), 1.0e-10);
    }

    // Tests first diagonal element smaller than threshold (r == 0)
    @Test(expected = NonPositiveDefiniteMatrixException.class)
    public void testRectangularCholeskyDecomposition_firstDiagonalBelowThreshold_throwsException() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { -1.0, 0.0 },
            {  0.0, 2.0 }
        });
        new RectangularCholeskyDecomposition(matrix, 1.0e-10);
    }

    // Tests all diagonal elements below threshold (zero matrix)
    @Test(expected = NonPositiveDefiniteMatrixException.class)
    public void testRectangularCholeskyDecomposition_allZeros_throwsException() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 0.0, 0.0 },
            { 0.0, 0.0 }
        });
        new RectangularCholeskyDecomposition(matrix, 1.0e-10);
    }

    // Tests negative diagonal appearing in remaining elements during decomposition
    @Test(expected = NonPositiveDefiniteMatrixException.class)
    public void testRectangularCholeskyDecomposition_negativeDiagonalLater_throwsException() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 1.0, 2.0 },
            { 2.0, 1.0 }
        });
        new RectangularCholeskyDecomposition(matrix, 1.0e-10);
    }

    // Tests diagonal elements in increasing order (triggers swap at each step)
    @Test
    public void testRectangularCholeskyDecomposition_increasingDiagonal_reconstructsOriginalMatrix() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 1.0, 0.1, 0.1 },
            { 0.1, 2.0, 0.2 },
            { 0.1, 0.2, 3.0 }
        });
        RectangularCholeskyDecomposition rcd = new RectangularCholeskyDecomposition(matrix, 1.0e-10);
        assertEquals(3, rcd.getRank());

        RealMatrix root = rcd.getRootMatrix();
        RealMatrix reconstructed = root.multiply(root.transpose());
        checkMatrixDifference(matrix, reconstructed, 1.0e-10);
    }

    // Tests diagonal elements in decreasing order (no swaps required)
    @Test
    public void testRectangularCholeskyDecomposition_decreasingDiagonal_reconstructsOriginalMatrix() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 5.0, 0.3, 0.2 },
            { 0.3, 3.0, 0.1 },
            { 0.2, 0.1, 1.0 }
        });
        RectangularCholeskyDecomposition rcd = new RectangularCholeskyDecomposition(matrix, 1.0e-10);
        assertEquals(3, rcd.getRank());

        RealMatrix root = rcd.getRootMatrix();
        RealMatrix reconstructed = root.multiply(root.transpose());
        checkMatrixDifference(matrix, reconstructed, 1.0e-10);
    }

    // Helper method to verify matrix equality within tolerance
    private void checkMatrixDifference(RealMatrix expected, RealMatrix actual, double tolerance) {
        assertEquals(expected.getRowDimension(), actual.getRowDimension());
        assertEquals(expected.getColumnDimension(), actual.getColumnDimension());
        for (int i = 0; i < expected.getRowDimension(); ++i) {
            for (int j = 0; j < expected.getColumnDimension(); ++j) {
                assertEquals(expected.getEntry(i, j), actual.getEntry(i, j), tolerance);
            }
        }
    }
}