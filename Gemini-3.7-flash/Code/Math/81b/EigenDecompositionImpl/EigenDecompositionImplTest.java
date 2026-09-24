package org.apache.commons.math.linear;

import org.apache.commons.math.util.MathUtils;
import org.junit.Test;
import static org.junit.Assert.*;

public class EigenDecompositionImplTest {

    private static final double TOLERANCE = 1.0e-11;

    // Tests 1x1 matrix eigenvalue and eigenvector decomposition
    @Test
    public void testEigenDecomposition_1x1Matrix_success() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 42.0 }
        });
        EigenDecomposition ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);

        assertEquals(42.0, ed.getRealEigenvalue(0), TOLERANCE);
        assertEquals(0.0, ed.getImagEigenvalue(0), TOLERANCE);
        assertEquals(42.0, ed.getDeterminant(), TOLERANCE);

        RealVector eigenvector = ed.getEigenvector(0);
        assertEquals(1.0, eigenvector.getEntry(0), TOLERANCE);

        RealMatrix d = ed.getD();
        assertEquals(42.0, d.getEntry(0, 0), TOLERANCE);

        RealMatrix v = ed.getV();
        assertEquals(1.0, v.getEntry(0, 0), TOLERANCE);

        RealMatrix vt = ed.getVT();
        assertEquals(1.0, vt.getEntry(0, 0), TOLERANCE);
    }

    // Tests 2x2 symmetric matrix decomposition
    @Test
    public void testEigenDecomposition_2x2Matrix_success() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 2.0, 1.0 },
            { 1.0, 2.0 }
        });
        EigenDecomposition ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);

        double[] realEigenvalues = ed.getRealEigenvalues();
        assertEquals(2, realEigenvalues.length);
        assertEquals(3.0, realEigenvalues[0], TOLERANCE);
        assertEquals(1.0, realEigenvalues[1], TOLERANCE);
        assertEquals(3.0, ed.getDeterminant(), TOLERANCE);

        double[] imagEigenvalues = ed.getImagEigenvalues();
        assertEquals(0.0, imagEigenvalues[0], TOLERANCE);
        assertEquals(0.0, imagEigenvalues[1], TOLERANCE);

        RealMatrix v = ed.getV();
        RealMatrix d = ed.getD();
        RealMatrix vt = ed.getVT();
        RealMatrix reconstructed = v.multiply(d).multiply(vt);

        for (int r = 0; r < 2; ++r) {
            for (int c = 0; c < 2; ++c) {
                assertEquals(matrix.getEntry(r, c), reconstructed.getEntry(r, c), TOLERANCE);
            }
        }
    }

    // Tests 3x3 symmetric matrix decomposition
    @Test
    public void testEigenDecomposition_3x3Matrix_success() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 1.0, 2.0, 0.0 },
            { 2.0, 4.0, 1.0 },
            { 0.0, 1.0, 3.0 }
        });
        EigenDecomposition ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);

        double[] eigenvalues = ed.getRealEigenvalues();
        assertEquals(3, eigenvalues.length);

        RealMatrix v = ed.getV();
        RealMatrix d = ed.getD();
        RealMatrix vt = ed.getVT();
        RealMatrix reconstructed = v.multiply(d).multiply(vt);

        for (int r = 0; r < 3; ++r) {
            for (int c = 0; c < 3; ++c) {
                assertEquals(matrix.getEntry(r, c), reconstructed.getEntry(r, c), 1.0e-10);
            }
        }
    }

    // Tests general symmetric matrix (dimension > 3) to execute general block processing
    @Test
    public void testEigenDecomposition_4x4Matrix_success() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 4.0, 1.0, -2.0, 2.0 },
            { 1.0, 2.0, 0.0, 1.0 },
            { -2.0, 0.0, 3.0, -2.0 },
            { 2.0, 1.0, -2.0, -1.0 }
        });
        EigenDecomposition ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);

        RealMatrix v = ed.getV();
        RealMatrix d = ed.getD();
        RealMatrix vt = ed.getVT();
        RealMatrix reconstructed = v.multiply(d).multiply(vt);

        for (int r = 0; r < 4; ++r) {
            for (int c = 0; c < 4; ++c) {
                assertEquals(matrix.getEntry(r, c), reconstructed.getEntry(r, c), 1.0e-10);
            }
        }
    }

    // Tests tridiagonal array constructor
    @Test
    public void testEigenDecomposition_tridiagonalConstructor_success() {
        double[] main = new double[] { 1.0, 2.0, 3.0, 4.0 };
        double[] secondary = new double[] { 0.5, 0.5, 0.5 };

        EigenDecomposition ed = new EigenDecompositionImpl(main, secondary, MathUtils.SAFE_MIN);
        double[] eigenvalues = ed.getRealEigenvalues();
        assertEquals(4, eigenvalues.length);

        for (int i = 0; i < 4; ++i) {
            RealVector vec = ed.getEigenvector(i);
            assertEquals(4, vec.getDimension());
            assertEquals(1.0, vec.getNorm(), 1.0e-10);
        }
    }

    // Tests large tridiagonal matrix triggering shift increment and split bounds (Defects4J Math-81 regression)
    @Test
    public void testEigenDecomposition_largeTridiagonalMatrix_convergesWithoutException() {
        double[] main = new double[] {
            6.746089e-05, 1.488046e-04, 3.486803e-04, 5.067347e-04,
            9.032128e-04, 1.344445e-03, 1.862788e-03, 2.502937e-03,
            3.303960e-03, 4.316812e-03, 5.589839e-03, 7.168544e-03,
            9.120516e-03, 1.151741e-02, 1.443577e-02, 1.796123e-02,
            2.217311e-02, 2.714416e-02, 3.295058e-02, 3.966056e-02,
            4.734898e-02, 5.608971e-02, 6.595679e-02, 7.702432e-02,
            8.936554e-02, 1.030537e-01, 1.181614e-01, 1.347604e-01,
            1.529202e-01, 1.727088e-01, 1.941916e-01, 2.174312e-01,
            2.424874e-01, 2.694165e-01, 2.982705e-01, 3.290987e-01,
            3.619472e-01, 3.968595e-01, 4.338760e-01, 4.730349e-01,
            5.143719e-01, 5.579198e-01, 6.037090e-01, 6.517676e-01,
            7.021213e-01, 7.547936e-01, 8.098059e-01, 8.671775e-01,
            9.269261e-01, 9.890666e-01, 1.053612e+00, 1.120573e+00,
            1.189958e+00, 1.261775e+00, 1.336029e+00, 1.412727e+00,
            1.491873e+00, 1.573473e+00, 1.657529e+00, 1.744044e+00,
            1.833020e+00, 1.924458e+00, 2.018357e+00, 2.114717e+00,
            2.213536e+00, 2.314811e+00, 2.418540e+00, 2.524718e+00,
            2.633342e+00, 2.744406e+00, 2.857905e+00, 2.973832e+00,
            3.092180e+00, 3.212941e+00, 3.336107e+00, 3.461668e+00,
            3.589616e+00, 3.719940e+00, 3.852630e+00, 3.987676e+00,
            4.125066e+00, 4.264789e+00, 4.406833e+00, 4.551184e+00,
            4.697831e+00, 4.846759e+00, 4.997954e+00, 5.151399e+00,
            5.307080e+00, 5.464979e+00, 5.625081e+00, 5.787368e+00,
            5.951822e+00, 6.118424e+00, 6.287154e+00, 6.457993e+00,
            6.630920e+00, 6.805915e+00, 6.982956e+00, 7.162021e+00
        };
        double[] secondary = new double[] {
            -1.000624e-04, -2.274530e-04, -4.195325e-04, -6.744574e-04,
            -1.006935e-03, -1.417215e-03, -1.916942e-03, -2.506540e-03,
            -3.200150e-03, -3.998492e-03, -4.915729e-03, -5.952672e-03,
            -7.123569e-03, -8.429408e-03, -9.884501e-03, -1.148995e-02,
            -1.326019e-02, -1.519638e-02, -1.731301e-02, -1.961132e-02,
            -2.210586e-02, -2.480798e-02, -2.771900e-02, -3.085353e-02,
            -3.422301e-02, -3.782877e-02, -4.168541e-02, -4.579450e-02,
            -5.017085e-02, -5.481596e-02, -5.974465e-02, -6.495847e-02,
            -7.047242e-02, -7.628800e-02, -8.242007e-02, -8.887010e-02,
            -9.565287e-02, -1.027700e-01, -1.102360e-01, -1.180526e-01,
            -1.262343e-01, -1.347829e-01, -1.437128e-01, -1.530258e-01,
            -1.627364e-01, -1.728464e-01, -1.833703e-01, -1.943098e-01,
            -2.056795e-01, -2.174811e-01, -2.297292e-01, -2.424256e-01,
            -2.555848e-01, -2.692087e-01, -2.833118e-01, -2.978962e-01,
            -3.129763e-01, -3.285542e-01, -3.446445e-01, -3.612496e-01,
            -3.783842e-01, -3.960505e-01, -4.142629e-01, -4.330238e-01,
            -4.523477e-01, -4.722370e-01, -4.927060e-01, -5.137573e-01,
            -5.354054e-01, -5.576527e-01, -5.805141e-01, -6.039919e-01,
            -6.280993e-01, -6.528489e-01, -6.782431e-01, -7.042948e-01,
            -7.310168e-01, -7.584113e-01, -7.864908e-01, -8.152675e-01,
            -8.447540e-01, -8.749622e-01, -9.059045e-01, -9.375936e-01,
            -9.700414e-01, -1.003261e+00, -1.037264e+00, -1.072063e+00,
            -1.107670e+00, -1.144099e+00, -1.181362e+00, -1.219470e+00,
            -1.258438e+00, -1.298276e+00, -1.338997e+00, -1.380614e+00,
            -1.423138e+00, -1.466581e+00, -1.510957e+00
        };

        EigenDecomposition ed = new EigenDecompositionImpl(main, secondary, MathUtils.SAFE_MIN);
        double[] eigenvalues = ed.getRealEigenvalues();
        assertNotNull(eigenvalues);
        assertEquals(100, eigenvalues.length);
        assertTrue(eigenvalues[0] >= eigenvalues[99]);
    }

    // Tests asymmetric matrix throws InvalidMatrixException
    @Test(expected = InvalidMatrixException.class)
    public void testEigenDecomposition_asymmetricMatrix_throwsException() {
        RealMatrix asymmetric = MatrixUtils.createRealMatrix(new double[][] {
            { 1.0, 2.0 },
            { 3.0, 4.0 }
        });
        new EigenDecompositionImpl(asymmetric, MathUtils.SAFE_MIN);
    }

    // Tests solver solve with double array, RealVector, RealMatrix and getInverse
    @Test
    public void testSolver_linearSystem_solvesCorrectly() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 2.0, 1.0 },
            { 1.0, 3.0 }
        });
        EigenDecomposition ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        DecompositionSolver solver = ed.getSolver();

        assertTrue(solver.isNonSingular());

        double[] bArray = new double[] { 5.0, 5.0 };
        double[] xArray = solver.solve(bArray);
        assertEquals(2.0, xArray[0], 1.0e-10);
        assertEquals(1.0, xArray[1], 1.0e-10);

        RealVector bVector = new ArrayRealVector(bArray);
        RealVector xVector = solver.solve(bVector);
        assertEquals(2.0, xVector.getEntry(0), 1.0e-10);
        assertEquals(1.0, xVector.getEntry(1), 1.0e-10);

        RealMatrix bMatrix = MatrixUtils.createRealMatrix(new double[][] {
            { 5.0, 3.0 },
            { 5.0, 4.0 }
        });
        RealMatrix xMatrix = solver.solve(bMatrix);
        assertEquals(2.0, xMatrix.getEntry(0, 0), 1.0e-10);
        assertEquals(1.0, xMatrix.getEntry(1, 0), 1.0e-10);
        assertEquals(1.0, xMatrix.getEntry(0, 1), 1.0e-10);
        assertEquals(1.0, xMatrix.getEntry(1, 1), 1.0e-10);

        RealMatrix inv = solver.getInverse();
        RealMatrix identity = matrix.multiply(inv);
        assertEquals(1.0, identity.getEntry(0, 0), 1.0e-10);
        assertEquals(0.0, identity.getEntry(0, 1), 1.0e-10);
        assertEquals(0.0, identity.getEntry(1, 0), 1.0e-10);
        assertEquals(1.0, identity.getEntry(1, 1), 1.0e-10);
    }

    // Tests solver exception on singular matrix
    @Test(expected = InvalidMatrixException.class)
    public void testSolver_singularMatrixSolveArray_throwsException() {
        RealMatrix singular = MatrixUtils.createRealMatrix(new double[][] {
            { 1.0, 1.0 },
            { 1.0, 1.0 }
        });
        EigenDecomposition ed = new EigenDecompositionImpl(singular, MathUtils.SAFE_MIN);
        DecompositionSolver solver = ed.getSolver();

        assertFalse(solver.isNonSingular());
        solver.solve(new double[] { 1.0, 2.0 });
    }

    // Tests solver getInverse exception on singular matrix
    @Test(expected = InvalidMatrixException.class)
    public void testSolver_singularMatrixGetInverse_throwsException() {
        RealMatrix singular = MatrixUtils.createRealMatrix(new double[][] {
            { 1.0, 1.0 },
            { 1.0, 1.0 }
        });
        EigenDecomposition ed = new EigenDecompositionImpl(singular, MathUtils.SAFE_MIN);
        DecompositionSolver solver = ed.getSolver();

        solver.getInverse();
    }

    // Tests solver dimension mismatch on vector
    @Test(expected = IllegalArgumentException.class)
    public void testSolver_vectorLengthMismatch_throwsException() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 2.0, 1.0 },
            { 1.0, 2.0 }
        });
        EigenDecomposition ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        ed.getSolver().solve(new double[] { 1.0, 2.0, 3.0 });
    }

    // Tests solver dimension mismatch on matrix
    @Test(expected = IllegalArgumentException.class)
    public void testSolver_matrixDimensionMismatch_throwsException() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 2.0, 1.0 },
            { 1.0, 2.0 }
        });
        EigenDecomposition ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        RealMatrix b = MatrixUtils.createRealMatrix(new double[][] {
            { 1.0, 2.0 },
            { 3.0, 4.0 },
            { 5.0, 6.0 }
        });
        ed.getSolver().solve(b);
    }
}