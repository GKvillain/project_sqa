package org.apache.commons.math3.optim.nonlinear.vector;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.DiagonalMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.NonSquareMatrixException;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.optim.OptimizationData;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class WeightTest {

    // Tests constructor with double array creating diagonal matrix entries correctly
    @Test
    public void testConstructor_doubleArray_createsDiagonalMatrix() {
        double[] weights = new double[] { 1.5, 2.5, 3.5 };
        Weight weight = new Weight(weights);
        RealMatrix matrix = weight.getWeight();

        assertEquals(3, matrix.getRowDimension());
        assertEquals(3, matrix.getColumnDimension());
        for (int i = 0; i < weights.length; i++) {
            for (int j = 0; j < weights.length; j++) {
                if (i == j) {
                    assertEquals(weights[i], matrix.getEntry(i, j), 1e-10);
                } else {
                    assertEquals(0.0, matrix.getEntry(i, j), 1e-10);
                }
            }
        }
    }

    // Tests constructor with single element double array
    @Test
    public void testConstructor_singleElementArray_createsSingleElementMatrix() {
        double[] weights = new double[] { 4.0 };
        Weight weight = new Weight(weights);
        RealMatrix matrix = weight.getWeight();

        assertEquals(1, matrix.getRowDimension());
        assertEquals(1, matrix.getColumnDimension());
        assertEquals(4.0, matrix.getEntry(0, 0), 1e-10);
    }

    // Tests constructor with empty double array
    @Test
    public void testConstructor_emptyArray_createsEmptyMatrix() {
        double[] weights = new double[0];
        Weight weight = new Weight(weights);
        RealMatrix matrix = weight.getWeight();

        assertEquals(0, matrix.getRowDimension());
        assertEquals(0, matrix.getColumnDimension());
    }

    // Tests constructor with negative and zero weight values
    @Test
    public void testConstructor_negativeAndZeroWeights_createsCorrectDiagonalMatrix() {
        double[] weights = new double[] { -1.0, 0.0, 5.0 };
        Weight weight = new Weight(weights);
        RealMatrix matrix = weight.getWeight();

        assertEquals(-1.0, matrix.getEntry(0, 0), 1e-10);
        assertEquals(0.0, matrix.getEntry(1, 1), 1e-10);
        assertEquals(5.0, matrix.getEntry(2, 2), 1e-10);
    }

    // Tests constructor with square RealMatrix
    @Test
    public void testConstructor_squareMatrix_createsWeightMatrix() {
        double[][] data = new double[][] {
            { 1.0, 2.0 },
            { 3.0, 4.0 }
        };
        RealMatrix inputMatrix = new Array2DRowRealMatrix(data);
        Weight weight = new Weight(inputMatrix);
        RealMatrix resultMatrix = weight.getWeight();

        assertEquals(2, resultMatrix.getRowDimension());
        assertEquals(2, resultMatrix.getColumnDimension());
        assertEquals(1.0, resultMatrix.getEntry(0, 0), 1e-10);
        assertEquals(2.0, resultMatrix.getEntry(0, 1), 1e-10);
        assertEquals(3.0, resultMatrix.getEntry(1, 0), 1e-10);
        assertEquals(4.0, resultMatrix.getEntry(1, 1), 1e-10);
    }

    // Tests constructor with DiagonalMatrix
    @Test
    public void testConstructor_diagonalMatrix_createsWeightMatrix() {
        double[] diag = new double[] { 2.0, 4.0, 6.0 };
        RealMatrix inputMatrix = new DiagonalMatrix(diag);
        Weight weight = new Weight(inputMatrix);
        RealMatrix resultMatrix = weight.getWeight();

        assertEquals(3, resultMatrix.getRowDimension());
        assertEquals(3, resultMatrix.getColumnDimension());
        assertEquals(2.0, resultMatrix.getEntry(0, 0), 1e-10);
        assertEquals(4.0, resultMatrix.getEntry(1, 1), 1e-10);
        assertEquals(6.0, resultMatrix.getEntry(2, 2), 1e-10);
    }

    // Tests non-square matrix with more rows than columns throws exception
    @Test(expected = NonSquareMatrixException.class)
    public void testConstructor_nonSquareMatrixMoreRows_throwsNonSquareMatrixException() {
        RealMatrix nonSquareMatrix = MatrixUtils.createRealMatrix(3, 2);
        new Weight(nonSquareMatrix);
    }

    // Tests non-square matrix with more columns than rows throws exception
    @Test(expected = NonSquareMatrixException.class)
    public void testConstructor_nonSquareMatrixMoreColumns_throwsNonSquareMatrixException() {
        RealMatrix nonSquareMatrix = MatrixUtils.createRealMatrix(2, 3);
        new Weight(nonSquareMatrix);
    }

    // Tests immutability when modifying the matrix returned by getWeight()
    @Test
    public void testGetWeight_returnsDefensiveCopy() {
        double[] weights = new double[] { 1.0, 2.0 };
        Weight weight = new Weight(weights);
        RealMatrix copy1 = weight.getWeight();
        copy1.setEntry(0, 0, 99.0);

        RealMatrix copy2 = weight.getWeight();
        assertEquals(1.0, copy2.getEntry(0, 0), 1e-10);
    }

    // Tests immutability when modifying the input matrix after construction
    @Test
    public void testConstructor_modifyingInputMatrix_doesNotAffectWeight() {
        double[][] data = new double[][] {
            { 1.0, 0.0 },
            { 0.0, 2.0 }
        };
        RealMatrix inputMatrix = new Array2DRowRealMatrix(data);
        Weight weight = new Weight(inputMatrix);

        inputMatrix.setEntry(0, 0, 99.0);

        RealMatrix resultMatrix = weight.getWeight();
        assertEquals(1.0, resultMatrix.getEntry(0, 0), 1e-10);
    }

    // Tests large diagonal matrix representation
    @Test
    public void testConstructor_largeDiagonalWeight_returnsCorrectMatrix() {
        final int size = 1000;
        double[] weights = new double[size];
        for (int i = 0; i < size; i++) {
            weights[i] = i + 1.0;
        }
        Weight weight = new Weight(weights);
        RealMatrix matrix = weight.getWeight();

        assertEquals(size, matrix.getRowDimension());
        assertEquals(size, matrix.getColumnDimension());
        assertEquals(1.0, matrix.getEntry(0, 0), 1e-10);
        assertEquals(500.0, matrix.getEntry(499, 499), 1e-10);
        assertEquals(1000.0, matrix.getEntry(999, 999), 1e-10);
        assertEquals(0.0, matrix.getEntry(0, 1), 1e-10);
    }

    // Tests that Weight implements OptimizationData interface
    @Test
    public void testImplementsOptimizationData() {
        Weight weight = new Weight(new double[] { 1.0 });
        assertTrue(weight instanceof OptimizationData);
        assertNotNull(weight.getWeight());
    }
}