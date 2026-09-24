package org.apache.commons.math.stat.correlation;

import org.apache.commons.math.MathException;
import org.apache.commons.math.linear.BlockRealMatrix;
import org.apache.commons.math.linear.RealMatrix;
import org.junit.Test;
import static org.junit.Assert.*;

public class PearsonsCorrelationTest {

    private static final double TOLERANCE = 10E-15;

    // Tests default constructor creates empty instance
    @Test
    public void testPearsonsCorrelation_defaultConstructor_hasNullMatrixAndZeroObs() {
        PearsonsCorrelation corr = new PearsonsCorrelation();
        assertNull(corr.getCorrelationMatrix());
    }

    // Tests 2D array constructor and correlation matrix computation
    @Test
    public void testPearsonsCorrelation_doubleArray_computesCorrectCorrelation() {
        double[][] data = {
            {1.0, 2.0},
            {3.0, 6.0},
            {5.0, 10.0}
        };
        PearsonsCorrelation corr = new PearsonsCorrelation(data);
        RealMatrix matrix = corr.getCorrelationMatrix();

        assertNotNull(matrix);
        assertEquals(2, matrix.getRowDimension());
        assertEquals(2, matrix.getColumnDimension());
        assertEquals(1.0, matrix.getEntry(0, 0), TOLERANCE);
        assertEquals(1.0, matrix.getEntry(1, 1), TOLERANCE);
        assertEquals(1.0, matrix.getEntry(0, 1), 1E-6);
        assertEquals(1.0, matrix.getEntry(1, 0), 1E-6);
    }

    // Tests RealMatrix constructor with valid data
    @Test
    public void testPearsonsCorrelation_realMatrix_computesCorrectCorrelation() {
        double[][] data = {
            {1.0, 2.0, 3.0},
            {2.0, 5.0, 6.0},
            {3.0, 4.0, 8.0},
            {4.0, 8.0, 10.0}
        };
        RealMatrix inputMatrix = new BlockRealMatrix(data);
        PearsonsCorrelation corr = new PearsonsCorrelation(inputMatrix);
        RealMatrix matrix = corr.getCorrelationMatrix();

        assertNotNull(matrix);
        assertEquals(3, matrix.getRowDimension());
        assertEquals(3, matrix.getColumnDimension());
        for (int i = 0; i < 3; i++) {
            assertEquals(1.0, matrix.getEntry(i, i), TOLERANCE);
        }
    }

    // Tests Covariance constructor
    @Test
    public void testPearsonsCorrelation_covariance_computesCorrectCorrelation() {
        double[][] data = {
            {1.0, 2.0},
            {3.0, 4.0},
            {5.0, 6.0}
        };
        Covariance cov = new Covariance(data);
        PearsonsCorrelation corr = new PearsonsCorrelation(cov);
        RealMatrix matrix = corr.getCorrelationMatrix();

        assertNotNull(matrix);
        assertEquals(1.0, matrix.getEntry(0, 0), TOLERANCE);
        assertEquals(1.0, matrix.getEntry(1, 1), TOLERANCE);
        assertEquals(1.0, matrix.getEntry(0, 1), 1E-6);
    }

    // Tests covariance constructor with null covariance matrix throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testPearsonsCorrelation_nullCovarianceMatrix_throwsException() {
        Covariance cov = new Covariance();
        new PearsonsCorrelation(cov);
    }

    // Tests constructor with covariance matrix and observation count
    @Test
    public void testPearsonsCorrelation_covarianceMatrixAndNObs_createsInstance() {
        double[][] covData = {
            {4.0, 2.0},
            {2.0, 9.0}
        };
        RealMatrix covMatrix = new BlockRealMatrix(covData);
        PearsonsCorrelation corr = new PearsonsCorrelation(covMatrix, 10);
        RealMatrix matrix = corr.getCorrelationMatrix();

        assertNotNull(matrix);
        assertEquals(1.0, matrix.getEntry(0, 0), TOLERANCE);
        assertEquals(1.0, matrix.getEntry(1, 1), TOLERANCE);
        assertEquals(2.0 / (2.0 * 3.0), matrix.getEntry(0, 1), TOLERANCE);
    }

    // Tests insufficient rows in input matrix throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testPearsonsCorrelation_insufficientRows_throwsException() {
        double[][] data = {
            {1.0, 2.0}
        };
        new PearsonsCorrelation(data);
    }

    // Tests insufficient columns in input matrix throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testPearsonsCorrelation_insufficientColumns_throwsException() {
        double[][] data = {
            {1.0},
            {2.0}
        };
        new PearsonsCorrelation(data);
    }

    // Tests array correlation method with valid input
    @Test
    public void testCorrelation_validArrays_returnsCorrectValue() {
        PearsonsCorrelation corr = new PearsonsCorrelation();
        double[] x = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] y = {2.0, 4.0, 6.0, 8.0, 10.0};
        double r = corr.correlation(x, y);
        assertEquals(1.0, r, 1E-6);

        double[] yInverse = {5.0, 4.0, 3.0, 2.0, 1.0};
        double rInverse = corr.correlation(x, yInverse);
        assertEquals(-1.0, rInverse, 1E-6);
    }

    // Tests correlation with different array lengths throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testCorrelation_mismatchedLengths_throwsException() {
        PearsonsCorrelation corr = new PearsonsCorrelation();
        double[] x = {1.0, 2.0, 3.0};
        double[] y = {1.0, 2.0};
        corr.correlation(x, y);
    }

    // Tests correlation with length less than 2 throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testCorrelation_insufficientLength_throwsException() {
        PearsonsCorrelation corr = new PearsonsCorrelation();
        double[] x = {1.0};
        double[] y = {2.0};
        corr.correlation(x, y);
    }

    // Tests standard error matrix calculation
    @Test
    public void testGetCorrelationStandardErrors_validData_returnsStandardErrors() {
        double[][] data = {
            {1.0, 2.0},
            {3.0, 5.0},
            {4.0, 7.0},
            {5.0, 9.0},
            {6.0, 12.0}
        };
        PearsonsCorrelation corr = new PearsonsCorrelation(data);
        RealMatrix se = corr.getCorrelationStandardErrors();

        assertNotNull(se);
        assertEquals(2, se.getRowDimension());
        assertEquals(2, se.getColumnDimension());
        assertEquals(0.0, se.getEntry(0, 0), TOLERANCE);
        assertEquals(0.0, se.getEntry(1, 1), TOLERANCE);
        assertTrue(se.getEntry(0, 1) >= 0.0);
        assertEquals(se.getEntry(0, 1), se.getEntry(1, 0), TOLERANCE);
    }

    // Tests p-value matrix calculation and diagonal properties
    @Test
    public void testGetCorrelationPValues_validData_returnsPValues() throws MathException {
        double[][] data = {
            {1.0, 2.0},
            {3.0, 5.0},
            {4.0, 7.0},
            {5.0, 9.0},
            {6.0, 12.0}
        };
        PearsonsCorrelation corr = new PearsonsCorrelation(data);
        RealMatrix pValues = corr.getCorrelationPValues();

        assertNotNull(pValues);
        assertEquals(2, pValues.getRowDimension());
        assertEquals(2, pValues.getColumnDimension());
        assertEquals(0.0, pValues.getEntry(0, 0), TOLERANCE);
        assertEquals(0.0, pValues.getEntry(1, 1), TOLERANCE);
        assertTrue(pValues.getEntry(0, 1) >= 0.0 && pValues.getEntry(0, 1) <= 1.0);
        assertEquals(pValues.getEntry(0, 1), pValues.getEntry(1, 0), TOLERANCE);
    }

    // Tests that small p-values for high correlation do not evaluate to zero/NaN (Defects4J Math-69)
    @Test
    public void testGetCorrelationPValues_highCorrelation_pValuesGreaterThanZero() throws MathException {
        double[][] data = {
            {1.0, 2.0},
            {2.0, 4.0},
            {3.0, 6.0},
            {4.0, 8.0},
            {5.0, 10.0},
            {6.0, 12.0},
            {7.0, 14.0},
            {8.0, 16.0},
            {9.0, 18.0},
            {10.0, 20.0000000000001}
        };
        PearsonsCorrelation corr = new PearsonsCorrelation(data);
        RealMatrix pValues = corr.getCorrelationPValues();
        double pValue = pValues.getEntry(0, 1);
        assertTrue("P-value should be strictly greater than 0", pValue > 0.0);
    }

    // Tests computeCorrelationMatrix with 2D array input
    @Test
    public void testComputeCorrelationMatrix_doubleArray_returnsSymmetricMatrix() {
        PearsonsCorrelation corr = new PearsonsCorrelation();
        double[][] data = {
            {1.0, 5.0, 10.0},
            {2.0, 4.0, 20.0},
            {3.0, 3.0, 30.0},
            {4.0, 2.0, 40.0},
            {5.0, 1.0, 50.0}
        };
        RealMatrix matrix = corr.computeCorrelationMatrix(data);
        assertNotNull(matrix);
        assertEquals(3, matrix.getRowDimension());
        assertEquals(3, matrix.getColumnDimension());
        assertEquals(1.0, matrix.getEntry(0, 0), TOLERANCE);
        assertEquals(-1.0, matrix.getEntry(0, 1), 1E-6);
        assertEquals(1.0, matrix.getEntry(0, 2), 1E-6);
        assertEquals(matrix.getEntry(0, 1), matrix.getEntry(1, 0), TOLERANCE);
    }

    // Tests covarianceToCorrelation derivation
    @Test
    public void testCovarianceToCorrelation_validCovarianceMatrix_returnsCorrelationMatrix() {
        PearsonsCorrelation corr = new PearsonsCorrelation();
        double[][] covData = {
            {16.0, 12.0},
            {12.0, 25.0}
        };
        RealMatrix covMatrix = new BlockRealMatrix(covData);
        RealMatrix corrMatrix = corr.covarianceToCorrelation(covMatrix);

        assertEquals(1.0, corrMatrix.getEntry(0, 0), TOLERANCE);
        assertEquals(1.0, corrMatrix.getEntry(1, 1), TOLERANCE);
        assertEquals(12.0 / (4.0 * 5.0), corrMatrix.getEntry(0, 1), TOLERANCE);
        assertEquals(12.0 / (4.0 * 5.0), corrMatrix.getEntry(1, 0), TOLERANCE);
    }
}