package org.apache.commons.math.linear;

import org.apache.commons.math.exception.NumberIsTooLargeException;
import org.apache.commons.math.exception.OutOfRangeException;
import org.apache.commons.math.exception.DimensionMismatchException;
import org.junit.Test;
import static org.junit.Assert.*;

public class OpenMapRealMatrixTest {

    // Tests matrix construction and basic dimensions
    @Test
    public void testConstructor_validDimensions_returnsCorrectDimensions() {
        OpenMapRealMatrix matrix = new OpenMapRealMatrix(3, 4);
        assertEquals(3, matrix.getRowDimension());
        assertEquals(4, matrix.getColumnDimension());
    }

    // Tests constructor when row * column exceeds Integer.MAX_VALUE (Defects4J Bug 45)
    @Test(expected = NumberIsTooLargeException.class)
    public void testConstructor_dimensionsTooLarge_throwsNumberIsTooLargeException() {
        new OpenMapRealMatrix(1000000, 1000000);
    }

    // Tests copy constructor and copy() method
    @Test
    public void testCopy_validMatrix_createsExactIndependentCopy() {
        OpenMapRealMatrix original = new OpenMapRealMatrix(3, 3);
        original.setEntry(0, 1, 2.5);
        original.setEntry(2, 2, -1.0);

        OpenMapRealMatrix copy = original.copy();
        assertEquals(original.getRowDimension(), copy.getRowDimension());
        assertEquals(original.getColumnDimension(), copy.getColumnDimension());
        assertEquals(2.5, copy.getEntry(0, 1), 1e-10);
        assertEquals(-1.0, copy.getEntry(2, 2), 1e-10);
        assertEquals(0.0, copy.getEntry(0, 0), 1e-10);

        // Modify copy and ensure original remains unchanged
        copy.setEntry(0, 1, 5.0);
        assertEquals(2.5, original.getEntry(0, 1), 1e-10);
    }

    // Tests createMatrix factory method
    @Test
    public void testCreateMatrix_validDimensions_returnsEmptyMatrix() {
        OpenMapRealMatrix matrix = new OpenMapRealMatrix(2, 2);
        RealMatrix created = matrix.createMatrix(4, 5);
        assertTrue(created instanceof OpenMapRealMatrix);
        assertEquals(4, created.getRowDimension());
        assertEquals(5, created.getColumnDimension());
        assertEquals(0.0, created.getEntry(0, 0), 1e-10);
    }

    // Tests setEntry with non-zero and zero values
    @Test
    public void testSetEntry_nonZeroAndZeroValues_storesAndRemovesCorrectly() {
        OpenMapRealMatrix matrix = new OpenMapRealMatrix(3, 3);
        matrix.setEntry(1, 1, 4.0);
        assertEquals(4.0, matrix.getEntry(1, 1), 1e-10);

        // Setting to 0.0 should remove entry from sparse storage
        matrix.setEntry(1, 1, 0.0);
        assertEquals(0.0, matrix.getEntry(1, 1), 1e-10);
    }

    // Tests addToEntry with positive increment and increment leading to zero
    @Test
    public void testAddToEntry_validIncrements_updatesAndRemovesWhenZero() {
        OpenMapRealMatrix matrix = new OpenMapRealMatrix(2, 2);
        matrix.addToEntry(0, 0, 3.5);
        assertEquals(3.5, matrix.getEntry(0, 0), 1e-10);

        matrix.addToEntry(0, 0, 1.5);
        assertEquals(5.0, matrix.getEntry(0, 0), 1e-10);

        // Adding negative to reach exactly 0.0
        matrix.addToEntry(0, 0, -5.0);
        assertEquals(0.0, matrix.getEntry(0, 0), 1e-10);
    }

    // Tests multiplyEntry with non-zero factor and zero factor
    @Test
    public void testMultiplyEntry_validFactors_multipliesAndRemovesWhenZero() {
        OpenMapRealMatrix matrix = new OpenMapRealMatrix(2, 2);
        matrix.setEntry(1, 0, 2.0);

        matrix.multiplyEntry(1, 0, 3.0);
        assertEquals(6.0, matrix.getEntry(1, 0), 1e-10);

        matrix.multiplyEntry(1, 0, 0.0);
        assertEquals(0.0, matrix.getEntry(1, 0), 1e-10);
    }

    // Tests add method with compatible OpenMapRealMatrix
    @Test
    public void testAdd_sameDimensions_returnsCorrectSum() {
        OpenMapRealMatrix m1 = new OpenMapRealMatrix(2, 2);
        m1.setEntry(0, 0, 1.0);
        m1.setEntry(1, 1, 2.0);

        OpenMapRealMatrix m2 = new OpenMapRealMatrix(2, 2);
        m2.setEntry(0, 0, 3.0);
        m2.setEntry(0, 1, 4.0);

        OpenMapRealMatrix result = m1.add(m2);
        assertEquals(4.0, result.getEntry(0, 0), 1e-10);
        assertEquals(4.0, result.getEntry(0, 1), 1e-10);
        assertEquals(0.0, result.getEntry(1, 0), 1e-10);
        assertEquals(2.0, result.getEntry(1, 1), 1e-10);
    }

    // Tests subtract method with OpenMapRealMatrix
    @Test
    public void testSubtract_openMapRealMatrix_returnsCorrectDifference() {
        OpenMapRealMatrix m1 = new OpenMapRealMatrix(2, 2);
        m1.setEntry(0, 0, 5.0);
        m1.setEntry(1, 1, 3.0);

        OpenMapRealMatrix m2 = new OpenMapRealMatrix(2, 2);
        m2.setEntry(0, 0, 2.0);
        m2.setEntry(1, 1, 3.0);

        OpenMapRealMatrix result = m1.subtract(m2);
        assertEquals(3.0, result.getEntry(0, 0), 1e-10);
        assertEquals(0.0, result.getEntry(1, 1), 1e-10);
    }

    // Tests subtract method fallback with generic RealMatrix
    @Test
    public void testSubtract_genericRealMatrix_returnsCorrectDifference() {
        OpenMapRealMatrix m1 = new OpenMapRealMatrix(2, 2);
        m1.setEntry(0, 0, 5.0);
        m1.setEntry(1, 1, 2.0);

        RealMatrix m2 = new Array2DRowRealMatrix(new double[][] {
            {1.0, 2.0},
            {3.0, 4.0}
        });

        RealMatrix result = m1.subtract(m2);
        assertEquals(4.0, result.getEntry(0, 0), 1e-10);
        assertEquals(-2.0, result.getEntry(0, 1), 1e-10);
        assertEquals(-3.0, result.getEntry(1, 0), 1e-10);
        assertEquals(-2.0, result.getEntry(1, 1), 1e-10);
    }

    // Tests multiply method with another OpenMapRealMatrix including zero-result removal
    @Test
    public void testMultiply_openMapRealMatrix_returnsCorrectProduct() {
        OpenMapRealMatrix m1 = new OpenMapRealMatrix(2, 3);
        m1.setEntry(0, 0, 1.0);
        m1.setEntry(0, 1, 2.0);
        m1.setEntry(1, 2, 3.0);

        OpenMapRealMatrix m2 = new OpenMapRealMatrix(3, 2);
        m2.setEntry(0, 0, 4.0);
        m2.setEntry(1, 0, -2.0); // 1.0*4.0 + 2.0*(-2.0) = 0.0 (tests outValue == 0.0 branch)
        m2.setEntry(2, 1, 5.0);

        OpenMapRealMatrix result = m1.multiply(m2);
        assertEquals(2, result.getRowDimension());
        assertEquals(2, result.getColumnDimension());
        assertEquals(0.0, result.getEntry(0, 0), 1e-10);
        assertEquals(0.0, result.getEntry(0, 1), 1e-10);
        assertEquals(0.0, result.getEntry(1, 0), 1e-10);
        assertEquals(15.0, result.getEntry(1, 1), 1e-10);
    }

    // Tests multiply method fallback with non-OpenMapRealMatrix (ClassCastException branch)
    @Test
    public void testMultiply_genericRealMatrix_returnsCorrectProduct() {
        OpenMapRealMatrix m1 = new OpenMapRealMatrix(2, 2);
        m1.setEntry(0, 0, 2.0);
        m1.setEntry(1, 1, 3.0);

        RealMatrix m2 = new BlockRealMatrix(new double[][] {
            {1.0, 2.0},
            {3.0, 4.0}
        });

        RealMatrix result = m1.multiply(m2);
        assertEquals(2.0, result.getEntry(0, 0), 1e-10);
        assertEquals(4.0, result.getEntry(0, 1), 1e-10);
        assertEquals(9.0, result.getEntry(1, 0), 1e-10);
        assertEquals(12.0, result.getEntry(1, 1), 1e-10);
    }

    // Tests exception path for invalid row index
    @Test(expected = OutOfRangeException.class)
    public void testGetEntry_invalidRowIndex_throwsOutOfRangeException() {
        OpenMapRealMatrix matrix = new OpenMapRealMatrix(2, 2);
        matrix.getEntry(2, 0);
    }

    // Tests exception path for invalid column index
    @Test(expected = OutOfRangeException.class)
    public void testSetEntry_invalidColumnIndex_throwsOutOfRangeException() {
        OpenMapRealMatrix matrix = new OpenMapRealMatrix(2, 2);
        matrix.setEntry(0, -1, 1.0);
    }

    // Tests exception path for incompatible dimensions in multiply
    @Test(expected = DimensionMismatchException.class)
    public void testMultiply_incompatibleDimensions_throwsDimensionMismatchException() {
        OpenMapRealMatrix m1 = new OpenMapRealMatrix(2, 3);
        OpenMapRealMatrix m2 = new OpenMapRealMatrix(2, 2);
        m1.multiply(m2);
    }
}