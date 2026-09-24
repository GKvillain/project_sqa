package org.apache.commons.math.linear;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BigMatrixImplTest {

    private double[][] testData;
    private double[][] testDataLU;
    private double[][] singularData;

    @Before
    public void setUp() {
        testData = new double[][] {
            { 1.0, 2.0, 3.0 },
            { 2.0, 5.0, 3.0 },
            { 1.0, 0.0, 8.0 }
        };
        testDataLU = new double[][] {
            { 2.0, 3.0, 1.0 },
            { 5.0, 4.0, 6.0 },
            { 1.0, 7.0, 8.0 }
        };
        singularData = new double[][] {
            { 1.0, 2.0, 3.0 },
            { 2.0, 4.0, 6.0 },
            { 3.0, 6.0, 9.0 }
        };
    }

    // Tests operate with a rectangular matrix (rows > cols) to catch vector dimension defect
    @Test
    public void testOperate_nonSquareMatrixMoreRowsThanColumns_returnsCorrectLength() {
        double[][] data = {
            { 1.0, 2.0 },
            { 3.0, 4.0 },
            { 5.0, 6.0 }
        };
        BigMatrix matrix = new BigMatrixImpl(data);
        BigDecimal[] vector = new BigDecimal[] { new BigDecimal(1.0), new BigDecimal(2.0) };
        BigDecimal[] result = matrix.operate(vector);

        assertNotNull(result);
        assertEquals(3, result.length);
        assertEquals(new BigDecimal(5.0), result[0]);
        assertEquals(new BigDecimal(11.0), result[1]);
        assertEquals(new BigDecimal(17.0), result[2]);
    }

    // Tests operate with double array input
    @Test
    public void testOperate_doubleArrayInput_returnsCorrectResult() {
        double[][] data = {
            { 1.0, 2.0 },
            { 3.0, 4.0 }
        };
        BigMatrix matrix = new BigMatrixImpl(data);
        double[] vector = new double[] { 2.0, 3.0 };
        BigDecimal[] result = matrix.operate(vector);

        assertEquals(2, result.length);
        assertEquals(new BigDecimal(8.0), result[0]);
        assertEquals(new BigDecimal(18.0), result[1]);
    }

    // Tests operate with incompatible vector dimension
    @Test(expected = IllegalArgumentException.class)
    public void testOperate_incompatibleVectorLength_throwsIllegalArgumentException() {
        BigMatrix matrix = new BigMatrixImpl(testData);
        matrix.operate(new BigDecimal[] { new BigDecimal(1.0), new BigDecimal(2.0) });
    }

    // Tests preMultiply vector with matrix
    @Test
    public void testPreMultiply_vector_returnsCorrectResult() {
        double[][] data = {
            { 1.0, 2.0, 3.0 },
            { 4.0, 5.0, 6.0 }
        };
        BigMatrix matrix = new BigMatrixImpl(data);
        BigDecimal[] vector = new BigDecimal[] { new BigDecimal(1.0), new BigDecimal(2.0) };
        BigDecimal[] result = matrix.preMultiply(vector);

        assertEquals(3, result.length);
        assertEquals(new BigDecimal(9.0), result[0]);
        assertEquals(new BigDecimal(12.0), result[1]);
        assertEquals(new BigDecimal(15.0), result[2]);
    }

    // Tests preMultiply with incompatible vector length
    @Test(expected = IllegalArgumentException.class)
    public void testPreMultiply_incompatibleVectorLength_throwsIllegalArgumentException() {
        BigMatrix matrix = new BigMatrixImpl(testData);
        matrix.preMultiply(new BigDecimal[] { new BigDecimal(1.0) });
    }

    // Tests constructors with invalid dimensions
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_negativeDimensions_throwsIllegalArgumentException() {
        new BigMatrixImpl(-1, 2);
    }

    // Tests constructor with String array
    @Test
    public void testConstructor_stringArray_createsMatrixProperly() {
        String[][] strData = {
            { "1.0", "2.0" },
            { "3.0", "4.0" }
        };
        BigMatrix matrix = new BigMatrixImpl(strData);
        assertEquals(2, matrix.getRowDimension());
        assertEquals(2, matrix.getColumnDimension());
        assertEquals(new BigDecimal("1.0"), matrix.getEntry(0, 0));
        assertEquals(new BigDecimal("4.0"), matrix.getEntry(1, 1));
    }

    // Tests constructor with BigDecimal column vector
    @Test
    public void testConstructor_columnVector_createsColumnMatrix() {
        BigDecimal[] vector = new BigDecimal[] { new BigDecimal(1), new BigDecimal(2), new BigDecimal(3) };
        BigMatrix matrix = new BigMatrixImpl(vector);
        assertEquals(3, matrix.getRowDimension());
        assertEquals(1, matrix.getColumnDimension());
        assertEquals(new BigDecimal(2), matrix.getEntry(1, 0));
    }

    // Tests matrix addition and dimension mismatch exception
    @Test
    public void testAdd_validDimensions_returnsSum() {
        BigMatrix m1 = new BigMatrixImpl(testData);
        BigMatrix m2 = new BigMatrixImpl(testData);
        BigMatrix result = m1.add(m2);

        assertEquals(new BigDecimal(2.0), result.getEntry(0, 0));
        assertEquals(new BigDecimal(10.0), result.getEntry(1, 1));
        assertEquals(new BigDecimal(16.0), result.getEntry(2, 2));
    }

    // Tests matrix subtraction and dimension mismatch exception
    @Test(expected = IllegalArgumentException.class)
    public void testSubtract_dimensionMismatch_throwsIllegalArgumentException() {
        BigMatrix m1 = new BigMatrixImpl(testData);
        BigMatrix m2 = new BigMatrixImpl(new double[][] { { 1.0, 2.0 } });
        m1.subtract(m2);
    }

    // Tests scalar addition and scalar multiplication
    @Test
    public void testScalarOperations_validScalar_returnsExpectedMatrix() {
        BigMatrix matrix = new BigMatrixImpl(testData);
        BigMatrix added = matrix.scalarAdd(new BigDecimal(2.0));
        assertEquals(new BigDecimal(3.0), added.getEntry(0, 0));

        BigMatrix multiplied = matrix.scalarMultiply(new BigDecimal(3.0));
        assertEquals(new BigDecimal(3.0), multiplied.getEntry(0, 0));
        assertEquals(new BigDecimal(15.0), multiplied.getEntry(1, 1));
    }

    // Tests matrix multiplication
    @Test
    public void testMultiply_compatibleMatrices_returnsProduct() {
        BigMatrix m1 = new BigMatrixImpl(new double[][] { { 1.0, 2.0 }, { 3.0, 4.0 } });
        BigMatrix m2 = new BigMatrixImpl(new double[][] { { 2.0, 0.0 }, { 1.0, 2.0 } });
        BigMatrix product = m1.multiply(m2);

        assertEquals(new BigDecimal(4.0), product.getEntry(0, 0));
        assertEquals(new BigDecimal(4.0), product.getEntry(0, 1));
        assertEquals(new BigDecimal(10.0), product.getEntry(1, 0));
        assertEquals(new BigDecimal(8.0), product.getEntry(1, 1));
    }

    // Tests matrix transpose and trace
    @Test
    public void testTransposeAndTrace_squareMatrix_returnsExpectedValues() {
        BigMatrix matrix = new BigMatrixImpl(testData);
        BigMatrix transposed = matrix.transpose();

        assertEquals(matrix.getEntry(0, 1), transposed.getEntry(1, 0));
        assertEquals(matrix.getEntry(1, 2), transposed.getEntry(2, 1));
        assertEquals(new BigDecimal(14.0), matrix.getTrace());
    }

    // Tests trace on non-square matrix throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testGetTrace_nonSquareMatrix_throwsIllegalArgumentException() {
        BigMatrix matrix = new BigMatrixImpl(new double[][] { { 1.0, 2.0, 3.0 }, { 4.0, 5.0, 6.0 } });
        matrix.getTrace();
    }

    // Tests getSubMatrix with start and end indices
    @Test
    public void testGetSubMatrix_validIndices_returnsSubMatrix() {
        BigMatrix matrix = new BigMatrixImpl(testData);
        BigMatrix sub = matrix.getSubMatrix(0, 1, 1, 2);

        assertEquals(2, sub.getRowDimension());
        assertEquals(2, sub.getColumnDimension());
        assertEquals(new BigDecimal(2.0), sub.getEntry(0, 0));
        assertEquals(new BigDecimal(3.0), sub.getEntry(0, 1));
    }

    // Tests getSubMatrix with invalid indices
    @Test(expected = MatrixIndexException.class)
    public void testGetSubMatrix_invalidIndices_throwsMatrixIndexException() {
        BigMatrix matrix = new BigMatrixImpl(testData);
        matrix.getSubMatrix(1, 0, 0, 1);
    }

    // Tests setSubMatrix
    @Test
    public void testSetSubMatrix_validInput_updatesMatrix() {
        BigMatrixImpl matrix = new BigMatrixImpl(testData);
        BigDecimal[][] sub = new BigDecimal[][] {
            { new BigDecimal(99.0), new BigDecimal(98.0) }
        };
        matrix.setSubMatrix(sub, 1, 1);

        assertEquals(new BigDecimal(99.0), matrix.getEntry(1, 1));
        assertEquals(new BigDecimal(98.0), matrix.getEntry(1, 2));
    }

    // Tests solve and inverse for invertible matrix
    @Test
    public void testSolveAndInverse_invertibleMatrix_returnsCorrectSolution() {
        BigMatrix matrix = new BigMatrixImpl(testDataLU);
        BigMatrix identity = MatrixUtils.createBigIdentityMatrix(3);
        BigMatrix inverse = matrix.inverse();
        BigMatrix product = matrix.multiply(inverse);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                assertEquals(identity.getEntry(i, j).doubleValue(), product.getEntry(i, j).doubleValue(), 1e-10);
            }
        }
    }

    // Tests solve on singular matrix throws exception
    @Test(expected = InvalidMatrixException.class)
    public void testSolve_singularMatrix_throwsInvalidMatrixException() {
        BigMatrix matrix = new BigMatrixImpl(singularData);
        matrix.solve(new double[] { 1.0, 2.0, 3.0 });
    }

    // Tests getDeterminant and isSingular
    @Test
    public void testDeterminantAndIsSingular_matrices_returnsCorrectValues() {
        BigMatrix singularMatrix = new BigMatrixImpl(singularData);
        assertTrue(singularMatrix.isSingular());
        assertEquals(BigDecimal.ZERO, singularMatrix.getDeterminant());

        BigMatrix nonSingular = new BigMatrixImpl(testDataLU);
        assertFalse(nonSingular.isSingular());
        assertFalse(BigDecimal.ZERO.equals(nonSingular.getDeterminant()));
    }

    // Tests equals and hashCode consistency
    @Test
    public void testEqualsAndHashCode_sameAndDifferentData_returnsExpectedResults() {
        BigMatrix m1 = new BigMatrixImpl(testData);
        BigMatrix m2 = new BigMatrixImpl(testData);
        BigMatrix m3 = new BigMatrixImpl(singularData);

        assertTrue(m1.equals(m1));
        assertTrue(m1.equals(m2));
        assertFalse(m1.equals(m3));
        assertFalse(m1.equals(null));
        assertFalse(m1.equals("Not a matrix"));
        assertEquals(m1.hashCode(), m2.hashCode());
    }

    // Additional coverage tests

    @Test
    public void testDefaultConstructor() {
        BigMatrix m = new BigMatrixImpl();
        assertEquals(0, m.getRowDimension());
        assertEquals(0, m.getColumnDimension());
    }

    @Test
    public void testConstructor_copyArrayFlag() {
        BigDecimal[][] data = new BigDecimal[][] {
            { new BigDecimal(1.0), new BigDecimal(2.0) },
            { new BigDecimal(3.0), new BigDecimal(4.0) }
        };
        BigMatrix mCopy = new BigMatrixImpl(data, true);
        BigMatrix mRef = new BigMatrixImpl(data, false);

        data[0][0] = new BigDecimal(99.0);
        assertEquals(new BigDecimal(1.0), mCopy.getEntry(0, 0));
        assertEquals(new BigDecimal(99.0), mRef.getEntry(0, 0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_emptyArray_throwsIllegalArgumentException() {
        new BigMatrixImpl(new double[][] {});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_emptySubArray_throwsIllegalArgumentException() {
        new BigMatrixImpl(new double[][] { {} });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_raggedArray_throwsIllegalArgumentException() {
        new BigMatrixImpl(new double[][] { { 1.0, 2.0 }, { 3.0 } });
    }

    @Test(expected = NullPointerException.class)
    public void testConstructor_nullArray_throwsNullPointerException() {
        new BigMatrixImpl((double[][]) null);
    }

    @Test
    public void testCopyAndGetData() {
        BigMatrix m = new BigMatrixImpl(testData);
        BigMatrix copy = m.copy();
        assertEquals(m, copy);

        BigDecimal[][] data = m.getData();
        assertEquals(3, data.length);
        assertEquals(3, data[0].length);

        double[][] doubleData = m.getDataAsDoubleArray();
        assertEquals(1.0, doubleData[0][0], 1e-10);
        assertEquals(8.0, doubleData[2][2], 1e-10);
    }

    @Test
    public void testGetRowAndColumn() {
        BigMatrix m = new BigMatrixImpl(testData);

        BigDecimal[] row = m.getRow(1);
        assertEquals(3, row.length);
        assertEquals(new BigDecimal(2.0), row[0]);
        assertEquals(new BigDecimal(5.0), row[1]);
        assertEquals(new BigDecimal(3.0), row[2]);

        double[] rowD = m.getRowAsDoubleArray(1);
        assertEquals(2.0, rowD[0], 1e-10);

        BigDecimal[] col = m.getColumn(1);
        assertEquals(3, col.length);
        assertEquals(new BigDecimal(2.0), col[0]);
        assertEquals(new BigDecimal(5.0), col[1]);
        assertEquals(new BigDecimal(0.0), col[2]);

        double[] colD = m.getColumnAsDoubleArray(1);
        assertEquals(0.0, colD[2], 1e-10);

        BigMatrix rowMatrix = m.getRowMatrix(1);
        assertEquals(1, rowMatrix.getRowDimension());
        assertEquals(3, rowMatrix.getColumnDimension());

        BigMatrix colMatrix = m.getColumnMatrix(1);
        assertEquals(3, colMatrix.getRowDimension());
        assertEquals(1, colMatrix.getColumnDimension());
    }

    @Test(expected = MatrixIndexException.class)
    public void testGetRow_invalidIndex_throwsMatrixIndexException() {
        BigMatrix m = new BigMatrixImpl(testData);
        m.getRow(-1);
    }

    @Test(expected = MatrixIndexException.class)
    public void testGetColumn_invalidIndex_throwsMatrixIndexException() {
        BigMatrix m = new BigMatrixImpl(testData);
        m.getColumn(5);
    }

    @Test
    public void testGetSubMatrix_selectedRowsAndCols() {
        BigMatrix m = new BigMatrixImpl(testData);
        BigMatrix sub = m.getSubMatrix(new int[] { 0, 2 }, new int[] { 1, 2 });
        assertEquals(2, sub.getRowDimension());
        assertEquals(2, sub.getColumnDimension());
        assertEquals(new BigDecimal(2.0), sub.getEntry(0, 0));
        assertEquals(new BigDecimal(3.0), sub.getEntry(0, 1));
        assertEquals(new BigDecimal(0.0), sub.getEntry(1, 0));
        assertEquals(new BigDecimal(8.0), sub.getEntry(1, 1));
    }

    @Test(expected = MatrixIndexException.class)
    public void testGetSubMatrix_invalidSelectedRows_throwsMatrixIndexException() {
        BigMatrix m = new BigMatrixImpl(testData);
        m.getSubMatrix(new int[] { 0, 4 }, new int[] { 0, 1 });
    }

    @Test
    public void testNormsAndIsSquare() {
        BigMatrix m = new BigMatrixImpl(testData);
        assertTrue(m.isSquare());

        BigMatrix rect = new BigMatrixImpl(new double[][] { { 1.0, 2.0 } });
        assertFalse(rect.isSquare());

        BigDecimal norm = m.getNorm();
        assertNotNull(norm);
        assertTrue(norm.compareTo(BigDecimal.ZERO) > 0);

        BigDecimal frobeniusNorm = m.getFrobeniusNorm();
        assertNotNull(frobeniusNorm);
        assertTrue(frobeniusNorm.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    public void testScaleAndRoundingMode() {
        BigMatrixImpl m = new BigMatrixImpl(testData);
        m.setScale(4);
        assertEquals(4, m.getScale());

        m.setRoundingMode(BigDecimal.ROUND_HALF_UP);
        assertEquals(BigDecimal.ROUND_HALF_UP, m.getRoundingMode());
    }

    @Test
    public void testSolve_matrixAndVector() {
        BigMatrix matrix = new BigMatrixImpl(testDataLU);
        BigMatrix bMatrix = new BigMatrixImpl(new double[][] { { 1.0 }, { 2.0 }, { 3.0 } });
        BigMatrix solutionMatrix = matrix.solve(bMatrix);
        assertEquals(3, solutionMatrix.getRowDimension());
        assertEquals(1, solutionMatrix.getColumnDimension());

        BigDecimal[] bVec = new BigDecimal[] { new BigDecimal(1.0), new BigDecimal(2.0), new BigDecimal(3.0) };
        BigDecimal[] solutionVec = matrix.solve(bVec);
        assertEquals(3, solutionVec.length);
    }

    @Test
    public void testPreMultiply_doubleArray() {
        BigMatrix m = new BigMatrixImpl(testData);
        double[] v = new double[] { 1.0, 2.0, 3.0 };
        BigDecimal[] result = m.preMultiply(v);
        assertEquals(3, result.length);
        assertEquals(new BigDecimal(8.0), result[0]);
        assertEquals(new BigDecimal(12.0), result[1]);
        assertEquals(new BigDecimal(33.0), result[2]);
    }
}