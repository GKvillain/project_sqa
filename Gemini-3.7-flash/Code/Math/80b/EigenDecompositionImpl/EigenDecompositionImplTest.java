package org.apache.commons.math.linear;

import org.junit.Test;
import static org.junit.Assert.*;

public class EigenDecompositionImplTest {

    private static final double TOLERANCE = 1e-6;

    // Tests 1x1 matrix eigenvalue decomposition
    @Test
    public void testDecompose_1x1Matrix_returnsCorrectEigenvalue() {
        double[] main = new double[] { 5.0 };
        double[] secondary = new double[] {};
        EigenDecomposition ed = new EigenDecompositionImpl(main, secondary, 0.0);

        assertEquals(1, ed.getRealEigenvalues().length);
        assertEquals(5.0, ed.getRealEigenvalue(0), TOLERANCE);
        assertEquals(5.0, ed.getDeterminant(), TOLERANCE);
    }

    // Tests 2x2 symmetric matrix decomposition using dedicated 2-row block path
    @Test
    public void testDecompose_2x2Matrix_returnsCorrectEigenvalues() {
        double[] main = new double[] { 2.0, 2.0 };
        double[] secondary = new double[] { 1.0 };
        EigenDecomposition ed = new EigenDecompositionImpl(main, secondary, 0.0);

        double[] eigenvalues = ed.getRealEigenvalues();
        assertEquals(2, eigenvalues.length);
        assertEquals(3.0, eigenvalues[0], TOLERANCE);
        assertEquals(1.0, eigenvalues[1], TOLERANCE);
    }

    // Tests 3x3 symmetric matrix decomposition using dedicated 3-row block path
    @Test
    public void testDecompose_3x3Matrix_returnsCorrectEigenvalues() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 2.0, -1.0,  0.0 },
            {-1.0,  2.0, -1.0 },
            { 0.0, -1.0,  2.0 }
        });
        EigenDecomposition ed = new EigenDecompositionImpl(matrix, 0.0);

        double[] eigenvalues = ed.getRealEigenvalues();
        assertEquals(3, eigenvalues.length);
        assertEquals(2.0 + Math.sqrt(2.0), eigenvalues[0], TOLERANCE);
        assertEquals(2.0, eigenvalues[1], TOLERANCE);
        assertEquals(2.0 - Math.sqrt(2.0), eigenvalues[2], TOLERANCE);
    }

    // Tests general symmetric matrix (n >= 4) triggering general block and flipIfWarranted
    @Test
    public void testDecompose_4x4Matrix_satisfiesOrthogonalDecomposition() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 4.0, 1.0, -2.0, 2.0 },
            { 1.0, 2.0,  0.0, 1.0 },
            {-2.0, 0.0,  3.0, -2.0 },
            { 2.0, 1.0, -2.0, -1.0 }
        });
        EigenDecomposition ed = new EigenDecompositionImpl(matrix, 0.0);

        RealMatrix v = ed.getV();
        RealMatrix d = ed.getD();
        RealMatrix vt = ed.getVT();

        RealMatrix reconstructed = v.multiply(d).multiply(vt);
        for (int r = 0; r < matrix.getRowDimension(); ++r) {
            for (int c = 0; c < matrix.getColumnDimension(); ++c) {
                assertEquals(matrix.getEntry(r, c), reconstructed.getEntry(r, c), TOLERANCE);
            }
        }
    }

    // Tests tridiagonal configuration where array flipping occurs
    @Test
    public void testDecompose_tridiagonalFlipping_computesCorrectEigenvalues() {
        double[] main = new double[] { 1.0, 2.0, 3.0, 4.0, 5.0, 100.0 };
        double[] secondary = new double[] { 0.5, 0.5, 0.5, 0.5, 0.5 };
        EigenDecomposition ed = new EigenDecompositionImpl(main, secondary, 0.0);

        double[] eigenvalues = ed.getRealEigenvalues();
        assertEquals(6, eigenvalues.length);
        for (int i = 0; i < eigenvalues.length - 1; ++i) {
            assertTrue(eigenvalues[i] >= eigenvalues[i + 1]);
        }
        double sum = 0.0;
        for (double val : eigenvalues) {
            sum += val;
        }
        assertEquals(115.0, sum, TOLERANCE);
    }

    // Tests rejection of asymmetric matrices
    @Test(expected = InvalidMatrixException.class)
    public void testConstructor_asymmetricMatrix_throwsInvalidMatrixException() {
        RealMatrix nonSymmetric = MatrixUtils.createRealMatrix(new double[][] {
            { 1.0, 2.0 },
            { 3.0, 4.0 }
        });
        new EigenDecompositionImpl(nonSymmetric, 0.0);
    }

    // Tests determinant calculation for symmetric matrix
    @Test
    public void testGetDeterminant_symmetricMatrix_returnsCorrectDeterminant() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 3.0, 2.0, 4.0 },
            { 2.0, 0.0, 2.0 },
            { 4.0, 2.0, 3.0 }
        });
        EigenDecomposition ed = new EigenDecompositionImpl(matrix, 0.0);
        assertEquals(-24.0, ed.getDeterminant(), TOLERANCE);
    }

    // Tests imaginary eigenvalues are zero for symmetric matrices
    @Test
    public void testGetImagEigenvalues_symmetricMatrix_returnsZeros() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 2.0, 1.0 },
            { 1.0, 2.0 }
        });
        EigenDecomposition ed = new EigenDecompositionImpl(matrix, 0.0);

        double[] imag = ed.getImagEigenvalues();
        assertEquals(2, imag.length);
        assertEquals(0.0, imag[0], 0.0);
        assertEquals(0.0, imag[1], 0.0);
        assertEquals(0.0, ed.getImagEigenvalue(0), 0.0);
    }

    // Tests eigenvector retrieval and properties
    @Test
    public void testGetEigenvector_returnsUnitLengthOrthogonalVector() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 2.0, 0.0 },
            { 0.0, 3.0 }
        });
        EigenDecomposition ed = new EigenDecompositionImpl(matrix, 0.0);

        RealVector ev0 = ed.getEigenvector(0);
        RealVector ev1 = ed.getEigenvector(1);

        assertEquals(1.0, ev0.getNorm(), TOLERANCE);
        assertEquals(1.0, ev1.getNorm(), TOLERANCE);
        assertEquals(0.0, ev0.dotProduct(ev1), TOLERANCE);
    }

    // Tests solving linear equation A * X = B with array input
    @Test
    public void testSolver_solveArray_returnsCorrectSolution() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 2.0, 1.0 },
            { 1.0, 2.0 }
        });
        EigenDecomposition ed = new EigenDecompositionImpl(matrix, 0.0);
        DecompositionSolver solver = ed.getSolver();

        assertTrue(solver.isNonSingular());
        double[] b = new double[] { 3.0, 3.0 };
        double[] x = solver.solve(b);

        assertEquals(1.0, x[0], TOLERANCE);
        assertEquals(1.0, x[1], TOLERANCE);
    }

    // Tests solving linear equation A * X = B with RealVector input
    @Test
    public void testSolver_solveRealVector_returnsCorrectSolution() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 4.0, 1.0 },
            { 1.0, 3.0 }
        });
        EigenDecomposition ed = new EigenDecompositionImpl(matrix, 0.0);
        DecompositionSolver solver = ed.getSolver();

        RealVector b = new ArrayRealVector(new double[] { 5.0, 4.0 });
        RealVector x = solver.solve(b);

        assertEquals(1.0, x.getEntry(0), TOLERANCE);
        assertEquals(1.0, x.getEntry(1), TOLERANCE);
    }

    // Tests solving linear equation A * X = B with RealMatrix input and inverse
    @Test
    public void testSolver_solveRealMatrixAndGetInverse_returnsCorrectMatrices() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 2.0, 1.0 },
            { 1.0, 2.0 }
        });
        EigenDecomposition ed = new EigenDecompositionImpl(matrix, 0.0);
        DecompositionSolver solver = ed.getSolver();

        RealMatrix identity = MatrixUtils.createRealIdentityMatrix(2);
        RealMatrix inverse = solver.getInverse();
        RealMatrix solved = solver.solve(identity);

        for (int r = 0; r < 2; ++r) {
            for (int c = 0; c < 2; ++c) {
                assertEquals(inverse.getEntry(r, c), solved.getEntry(r, c), TOLERANCE);
            }
        }
    }

    // Tests solver exception on singular matrix
    @Test(expected = SingularMatrixException.class)
    public void testSolver_singularMatrix_throwsSingularMatrixException() {
        RealMatrix singular = MatrixUtils.createRealMatrix(new double[][] {
            { 1.0, 1.0 },
            { 1.0, 1.0 }
        });
        EigenDecomposition ed = new EigenDecompositionImpl(singular, 0.0);
        DecompositionSolver solver = ed.getSolver();

        assertFalse(solver.isNonSingular());
        solver.getInverse();
    }

    // Tests solver exception on vector length mismatch
    @Test(expected = IllegalArgumentException.class)
    public void testSolver_vectorDimensionMismatch_throwsIllegalArgumentException() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 2.0, 1.0 },
            { 1.0, 2.0 }
        });
        EigenDecomposition ed = new EigenDecompositionImpl(matrix, 0.0);
        ed.getSolver().solve(new double[] { 1.0, 2.0, 3.0 });
    }

    // Tests solver exception on matrix dimension mismatch
    @Test(expected = IllegalArgumentException.class)
    public void testSolver_matrixDimensionMismatch_throwsIllegalArgumentException() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 2.0, 1.0 },
            { 1.0, 2.0 }
        });
        EigenDecomposition ed = new EigenDecompositionImpl(matrix, 0.0);
        RealMatrix b = MatrixUtils.createRealMatrix(new double[][] {
            { 1.0, 2.0 },
            { 3.0, 4.0 },
            { 5.0, 6.0 }
        });
        ed.getSolver().solve(b);
    }

    // Tests index out of bounds exception for eigenvalues
    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testGetRealEigenvalue_outOfBounds_throwsArrayIndexOutOfBoundsException() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 1.0, 0.0 },
            { 0.0, 1.0 }
        });
        EigenDecomposition ed = new EigenDecompositionImpl(matrix, 0.0);
        ed.getRealEigenvalue(5);
    }
}