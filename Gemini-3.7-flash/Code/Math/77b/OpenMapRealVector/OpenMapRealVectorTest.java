package org.apache.commons.math.linear;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.apache.commons.math.MathRuntimeException;
import org.junit.Test;
import static org.junit.Assert.*;

public class OpenMapRealVectorTest {

    private static final double EPSILON = 1.0e-12;

    // Tests constructors and dimensions
    @Test
    public void testConstructors_variousInputs_createsValidVectors() {
        OpenMapRealVector v0 = new OpenMapRealVector();
        assertEquals(0, v0.getDimension());

        OpenMapRealVector v1 = new OpenMapRealVector(5);
        assertEquals(5, v1.getDimension());
        assertEquals(0.0, v1.getEntry(0), EPSILON);

        OpenMapRealVector v2 = new OpenMapRealVector(5, 1.0e-5);
        assertEquals(5, v2.getDimension());

        OpenMapRealVector v3 = new OpenMapRealVector(5, 3);
        assertEquals(5, v3.getDimension());

        OpenMapRealVector v4 = new OpenMapRealVector(5, 3, 1.0e-5);
        assertEquals(5, v4.getDimension());

        double[] data = new double[]{1.0, 0.0, 2.0};
        OpenMapRealVector v5 = new OpenMapRealVector(data);
        assertEquals(3, v5.getDimension());
        assertEquals(1.0, v5.getEntry(0), EPSILON);
        assertEquals(0.0, v5.getEntry(1), EPSILON);
        assertEquals(2.0, v5.getEntry(2), EPSILON);

        Double[] doubleData = new Double[]{Double.valueOf(0.0), Double.valueOf(3.0)};
        OpenMapRealVector v6 = new OpenMapRealVector(doubleData, 1.0e-5);
        assertEquals(2, v6.getDimension());
        assertEquals(3.0, v6.getEntry(1), EPSILON);

        OpenMapRealVector v7 = new OpenMapRealVector(v5);
        assertEquals(3, v7.getDimension());
        assertEquals(2.0, v7.getEntry(2), EPSILON);

        RealVector v8 = new ArrayRealVector(new double[]{4.0, 5.0});
        OpenMapRealVector v9 = new OpenMapRealVector(v8);
        assertEquals(2, v9.getDimension());
        assertEquals(4.0, v9.getEntry(0), EPSILON);
    }

    // Tests getLInfNorm calculation (Defect 77b detection)
    @Test
    public void testGetLInfNorm_multipleElements_returnsMaxAbsoluteValue() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{-6.0, 2.0, 0.0, 4.0});
        assertEquals(6.0, v.getLInfNorm(), EPSILON);

        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{1.0, 2.0, 3.0});
        assertEquals(3.0, v2.getLInfNorm(), EPSILON);
    }

    // Tests add operations with OpenMapRealVector and RealVector
    @Test
    public void testAdd_openMapAndGenericRealVector_returnsCorrectSum() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{1.0, 0.0, 3.0});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{0.0, 2.0, 5.0});
        OpenMapRealVector sum = v1.add(v2);

        assertEquals(1.0, sum.getEntry(0), EPSILON);
        assertEquals(2.0, sum.getEntry(1), EPSILON);
        assertEquals(8.0, sum.getEntry(2), EPSILON);

        RealVector vDense = new ArrayRealVector(new double[]{2.0, 2.0, 2.0});
        RealVector sumDense = v1.add(vDense);
        assertEquals(3.0, sumDense.getEntry(0), EPSILON);
        assertEquals(2.0, sumDense.getEntry(1), EPSILON);
        assertEquals(5.0, sumDense.getEntry(2), EPSILON);
    }

    // Tests subtract operations
    @Test
    public void testSubtract_openMapAndArray_returnsCorrectDifference() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{5.0, 0.0, 2.0});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{2.0, 3.0, 0.0});

        OpenMapRealVector diff1 = v1.subtract(v2);
        assertEquals(3.0, diff1.getEntry(0), EPSILON);
        assertEquals(-3.0, diff1.getEntry(1), EPSILON);
        assertEquals(2.0, diff1.getEntry(2), EPSILON);

        OpenMapRealVector diff2 = v1.subtract(new double[]{1.0, 1.0, 1.0});
        assertEquals(4.0, diff2.getEntry(0), EPSILON);
        assertEquals(-1.0, diff2.getEntry(1), EPSILON);
        assertEquals(1.0, diff2.getEntry(2), EPSILON);

        RealVector generic = new ArrayRealVector(new double[]{2.0, 3.0, 0.0});
        OpenMapRealVector diff3 = v1.subtract(generic);
        assertEquals(3.0, diff3.getEntry(0), EPSILON);
        assertEquals(-3.0, diff3.getEntry(1), EPSILON);
        assertEquals(2.0, diff3.getEntry(2), EPSILON);
    }

    // Tests dotProduct computation
    @Test
    public void testDotProduct_matchingDimensions_returnsDotProduct() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{1.0, 2.0, 0.0, 4.0});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{0.0, 3.0, 5.0, 2.0});

        double dot = v1.dotProduct(v2);
        assertEquals(14.0, dot, EPSILON);

        RealVector vDense = new ArrayRealVector(new double[]{2.0, 1.0, 1.0, 1.0});
        assertEquals(8.0, v1.dotProduct(vDense), EPSILON);
    }

    // Tests element-by-element multiply and divide
    @Test
    public void testEbeMultiplyAndDivide_validInputs_returnsCorrectResults() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{4.0, 0.0, 9.0});
        RealVector v2 = new ArrayRealVector(new double[]{2.0, 5.0, 3.0});
        double[] array = new double[]{2.0, 5.0, 3.0};

        OpenMapRealVector mul = v1.ebeMultiply(v2);
        assertEquals(8.0, mul.getEntry(0), EPSILON);
        assertEquals(0.0, mul.getEntry(1), EPSILON);
        assertEquals(27.0, mul.getEntry(2), EPSILON);

        OpenMapRealVector mulArr = v1.ebeMultiply(array);
        assertEquals(8.0, mulArr.getEntry(0), EPSILON);
        assertEquals(0.0, mulArr.getEntry(1), EPSILON);

        OpenMapRealVector div = v1.ebeDivide(v2);
        assertEquals(2.0, div.getEntry(0), EPSILON);
        assertEquals(0.0, div.getEntry(1), EPSILON);
        assertEquals(3.0, div.getEntry(2), EPSILON);

        OpenMapRealVector divArr = v1.ebeDivide(array);
        assertEquals(2.0, divArr.getEntry(0), EPSILON);
        assertEquals(0.0, divArr.getEntry(1), EPSILON);
    }

    // Tests distance, L1 distance, and L-Inf distance
    @Test
    public void testDistanceMetrics_validVectors_returnsAccurateDistances() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{1.0, 0.0, 4.0});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{0.0, 3.0, 0.0});

        assertEquals(Math.sqrt(1.0 + 9.0 + 16.0), v1.getDistance(v2), EPSILON);
        assertEquals(Math.sqrt(1.0 + 9.0 + 16.0), v1.getDistance((RealVector) v2), EPSILON);
        assertEquals(Math.sqrt(1.0 + 9.0 + 16.0), v1.getDistance(new double[]{0.0, 3.0, 0.0}), EPSILON);

        assertEquals(1.0 + 3.0 + 4.0, v1.getL1Distance(v2), EPSILON);
        assertEquals(1.0 + 3.0 + 4.0, v1.getL1Distance((RealVector) v2), EPSILON);
        assertEquals(1.0 + 3.0 + 4.0, v1.getL1Distance(new double[]{0.0, 3.0, 0.0}), EPSILON);

        assertEquals(4.0, v1.getLInfDistance((RealVector) v2), EPSILON);
        assertEquals(4.0, v1.getLInfDistance(new double[]{0.0, 3.0, 0.0}), EPSILON);
    }

    // Tests append methods
    @Test
    public void testAppend_variousTypes_appendsCorrectly() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{1.0, 2.0});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{3.0, 4.0});

        OpenMapRealVector res1 = v1.append(v2);
        assertEquals(4, res1.getDimension());
        assertArrayEquals(new double[]{1.0, 2.0, 3.0, 4.0}, res1.getData(), EPSILON);

        OpenMapRealVector res2 = v1.append(5.0);
        assertEquals(3, res2.getDimension());
        assertArrayEquals(new double[]{1.0, 2.0, 5.0}, res2.getData(), EPSILON);

        OpenMapRealVector res3 = v1.append(new double[]{6.0, 7.0});
        assertEquals(4, res3.getDimension());
        assertArrayEquals(new double[]{1.0, 2.0, 6.0, 7.0}, res3.getData(), EPSILON);

        OpenMapRealVector res4 = v1.append((RealVector) new ArrayRealVector(new double[]{8.0}));
        assertEquals(3, res4.getDimension());
        assertArrayEquals(new double[]{1.0, 2.0, 8.0}, res4.getData(), EPSILON);
    }

    // Tests getSubVector and setSubVector
    @Test
    public void testSubVectorOperations_validIndices_getsAndSetsCorrectly() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1.0, 2.0, 3.0, 4.0, 5.0});

        OpenMapRealVector sub = v.getSubVector(1, 3);
        assertEquals(3, sub.getDimension());
        assertArrayEquals(new double[]{2.0, 3.0, 4.0}, sub.getData(), EPSILON);

        v.setSubVector(1, new double[]{9.0, 8.0});
        assertEquals(9.0, v.getEntry(1), EPSILON);
        assertEquals(8.0, v.getEntry(2), EPSILON);

        v.setSubVector(3, new ArrayRealVector(new double[]{7.0, 6.0}));
        assertEquals(7.0, v.getEntry(3), EPSILON);
        assertEquals(6.0, v.getEntry(4), EPSILON);
    }

    // Tests setEntry, set, and removing default zero values
    @Test
    public void testSetAndSetEntry_zeroAndNonZero_updatesMapProperly() {
        OpenMapRealVector v = new OpenMapRealVector(3);
        v.setEntry(0, 5.0);
        assertEquals(5.0, v.getEntry(0), EPSILON);

        v.setEntry(0, 0.0);
        assertEquals(0.0, v.getEntry(0), EPSILON);

        v.set(7.0);
        assertArrayEquals(new double[]{7.0, 7.0, 7.0}, v.getData(), EPSILON);
        assertEquals(1.0, v.getSparcity(), EPSILON);
    }

    // Tests mapAdd and mapAddToSelf
    @Test
    public void testMapAddAndSelf_scalarValue_addsToAllEntries() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1.0, 0.0, -1.0});
        OpenMapRealVector mapped = v.mapAdd(2.0);

        assertArrayEquals(new double[]{3.0, 2.0, 1.0}, mapped.getData(), EPSILON);
        assertArrayEquals(new double[]{1.0, 0.0, -1.0}, v.getData(), EPSILON);

        v.mapAddToSelf(5.0);
        assertArrayEquals(new double[]{6.0, 5.0, 4.0}, v.getData(), EPSILON);
    }

    // Tests isNaN and isInfinite detection
    @Test
    public void testIsNaNAndIsInfinite_variousValues_detectsProperly() {
        OpenMapRealVector vNormal = new OpenMapRealVector(new double[]{1.0, 2.0});
        assertFalse(vNormal.isNaN());
        assertFalse(vNormal.isInfinite());

        OpenMapRealVector vNaN = new OpenMapRealVector(new double[]{1.0, Double.NaN});
        assertTrue(vNaN.isNaN());
        assertFalse(vNaN.isInfinite());

        OpenMapRealVector vInf = new OpenMapRealVector(new double[]{1.0, Double.POSITIVE_INFINITY});
        assertFalse(vInf.isNaN());
        assertTrue(vInf.isInfinite());

        OpenMapRealVector vBoth = new OpenMapRealVector(new double[]{Double.POSITIVE_INFINITY, Double.NaN});
        assertTrue(vBoth.isNaN());
        assertFalse(vBoth.isInfinite());
    }

    // Tests unitVector and unitize normalization
    @Test
    public void testUnitVectorAndUnitize_normalVector_normalizesToUnitLength() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{0.0, 3.0, 4.0});
        OpenMapRealVector unit = v.unitVector();

        assertEquals(1.0, unit.getNorm(), EPSILON);
        assertEquals(0.6, unit.getEntry(1), EPSILON);
        assertEquals(0.8, unit.getEntry(2), EPSILON);

        v.unitize();
        assertEquals(1.0, v.getNorm(), EPSILON);
        assertEquals(0.6, v.getEntry(1), EPSILON);
        assertEquals(0.8, v.getEntry(2), EPSILON);
    }

    // Tests unitize on zero norm vector throwing ArithmeticException
    @Test(expected = ArithmeticException.class)
    public void testUnitize_zeroNormVector_throwsArithmeticException() {
        OpenMapRealVector v = new OpenMapRealVector(3);
        v.unitize();
    }

    // Tests outerProduct and projection
    @Test
    public void testOuterProductAndProjection_validVectors_returnsExpectedResults() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{2.0, 0.0});
        RealMatrix outer = v1.outerProduct(new double[]{3.0, 4.0});

        assertEquals(6.0, outer.getEntry(0, 0), EPSILON);
        assertEquals(8.0, outer.getEntry(0, 1), EPSILON);
        assertEquals(0.0, outer.getEntry(1, 0), EPSILON);
        assertEquals(0.0, outer.getEntry(1, 1), EPSILON);

        OpenMapRealVector u = new OpenMapRealVector(new double[]{1.0, 0.0});
        RealVector proj = u.projection(new ArrayRealVector(new double[]{2.0, 0.0}));
        assertEquals(1.0, proj.getEntry(0), EPSILON);

        OpenMapRealVector projArr = u.projection(new double[]{2.0, 0.0});
        assertEquals(1.0, projArr.getEntry(0), EPSILON);
    }

    // Tests equals and hashCode consistency
    @Test
    public void testEqualsAndHashCode_equalAndUnequalInstances_behaveCorrectly() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{1.0, 0.0, 2.0});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{1.0, 0.0, 2.0});
        OpenMapRealVector v3 = new OpenMapRealVector(new double[]{1.0, 2.0, 0.0});
        OpenMapRealVector vDiffDim = new OpenMapRealVector(new double[]{1.0, 0.0});

        assertTrue(v1.equals(v1));
        assertTrue(v1.equals(v2));
        assertEquals(v1.hashCode(), v2.hashCode());

        assertFalse(v1.equals(null));
        assertFalse(v1.equals("non-vector"));
        assertFalse(v1.equals(v3));
        assertFalse(v1.equals(vDiffDim));
    }

    // Tests sparse iterator
    @Test
    public void testSparseIterator_nonZeroEntries_iteratesOverNonZeros() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{0.0, 5.0, 0.0, 7.0});
        Iterator<RealVector.Entry> iter = v.sparseIterator();

        int count = 0;
        while (iter.hasNext()) {
            RealVector.Entry entry = iter.next();
            assertTrue(entry.getIndex() == 1 || entry.getIndex() == 3);
            if (entry.getIndex() == 1) {
                assertEquals(5.0, entry.getValue(), EPSILON);
                entry.setValue(10.0);
            }
            count++;
        }
        assertEquals(2, count);
        assertEquals(10.0, v.getEntry(1), EPSILON);
    }

    // Tests dimension mismatch exception
    @Test(expected = IllegalArgumentException.class)
    public void testAdd_dimensionMismatch_throwsIllegalArgumentException() {
        OpenMapRealVector v1 = new OpenMapRealVector(2);
        OpenMapRealVector v2 = new OpenMapRealVector(3);
        v1.add(v2);
    }

    // Tests out of bounds index exception
    @Test(expected = MatrixIndexException.class)
    public void testGetEntry_invalidIndex_throwsMatrixIndexException() {
        OpenMapRealVector v = new OpenMapRealVector(3);
        v.getEntry(5);
    }

    // Additional tests for full coverage

    @Test
    public void testConstructor_doubleObjectArray_createsVector() {
        Double[] data = new Double[]{Double.valueOf(1.0), Double.valueOf(0.0), Double.valueOf(2.5)};
        OpenMapRealVector v = new OpenMapRealVector(data);
        assertEquals(3, v.getDimension());
        assertEquals(1.0, v.getEntry(0), EPSILON);
        assertEquals(0.0, v.getEntry(1), EPSILON);
        assertEquals(2.5, v.getEntry(2), EPSILON);
    }

    @Test
    public void testCopy_createsIdenticalIndependentInstance() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{1.0, 2.0, 3.0});
        OpenMapRealVector v2 = v1.copy();
        assertEquals(v1, v2);
        assertNotSame(v1, v2);
        v2.setEntry(0, 10.0);
        assertEquals(1.0, v1.getEntry(0), EPSILON);
        assertEquals(10.0, v2.getEntry(0), EPSILON);
    }

    @Test
    public void testAdd_openMapAndDoubleArray_returnsCorrectSum() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1.0, 0.0, 3.0});
        OpenMapRealVector res = v.add(new double[]{2.0, 4.0, -1.0});
        assertEquals(3.0, res.getEntry(0), EPSILON);
        assertEquals(4.0, res.getEntry(1), EPSILON);
        assertEquals(2.0, res.getEntry(2), EPSILON);
    }

    @Test
    public void testMapMultiplyAndDivide_scalarValues_returnsExpectedResults() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{2.0, 0.0, 4.0});
        OpenMapRealVector mul = v.mapMultiply(3.0);
        assertArrayEquals(new double[]{6.0, 0.0, 12.0}, mul.getData(), EPSILON);

        v.mapMultiplyToSelf(2.0);
        assertArrayEquals(new double[]{4.0, 0.0, 8.0}, v.getData(), EPSILON);

        OpenMapRealVector div = v.mapDivide(2.0);
        assertArrayEquals(new double[]{2.0, 0.0, 4.0}, div.getData(), EPSILON);

        v.mapDivideToSelf(4.0);
        assertArrayEquals(new double[]{1.0, 0.0, 2.0}, v.getData(), EPSILON);
    }

    @Test
    public void testMapInvAndMapInvToSelf_validVector_invertsNonZeroEntries() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{2.0, 4.0});
        OpenMapRealVector inv = v.mapInv();
        assertEquals(0.5, inv.getEntry(0), EPSILON);
        assertEquals(0.25, inv.getEntry(1), EPSILON);

        v.mapInvToSelf();
        assertEquals(0.5, v.getEntry(0), EPSILON);
        assertEquals(0.25, v.getEntry(1), EPSILON);
    }

    @Test
    public void testEbeMultiplyAndDivide_openMapRealVector_returnsExpectedResults() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{4.0, 0.0, 6.0});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{2.0, 3.0, 3.0});

        OpenMapRealVector mul = v1.ebeMultiply(v2);
        assertEquals(8.0, mul.getEntry(0), EPSILON);
        assertEquals(0.0, mul.getEntry(1), EPSILON);
        assertEquals(18.0, mul.getEntry(2), EPSILON);

        OpenMapRealVector div = v1.ebeDivide(v2);
        assertEquals(2.0, div.getEntry(0), EPSILON);
        assertEquals(0.0, div.getEntry(1), EPSILON);
        assertEquals(2.0, div.getEntry(2), EPSILON);
    }

    @Test
    public void testGetL1NormAndGetNorm_computesCorrectNorms() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{3.0, -4.0, 0.0});
        assertEquals(7.0, v.getL1Norm(), EPSILON);
        assertEquals(5.0, v.getNorm(), EPSILON);
    }

    @Test
    public void testGetLInfDistance_openMapVector_returnsMaxDifference() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{1.0, 0.0, 5.0});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{4.0, -2.0, 1.0});
        assertEquals(4.0, v1.getLInfDistance(v2), EPSILON);
    }

    @Test
    public void testOuterProduct_openMapAndRealVector_returnsMatrix() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{1.0, 2.0});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{3.0, 4.0});

        RealMatrix m1 = v1.outerProduct(v2);
        assertEquals(3.0, m1.getEntry(0, 0), EPSILON);
        assertEquals(4.0, m1.getEntry(0, 1), EPSILON);
        assertEquals(6.0, m1.getEntry(1, 0), EPSILON);
        assertEquals(8.0, m1.getEntry(1, 1), EPSILON);

        RealMatrix m2 = v1.outerProduct((RealVector) v2);
        assertEquals(3.0, m2.getEntry(0, 0), EPSILON);
        assertEquals(8.0, m2.getEntry(1, 1), EPSILON);
    }

    @Test
    public void testProjection_openMapRealVector_returnsProjectedVector() {
        OpenMapRealVector u = new OpenMapRealVector(new double[]{3.0, 4.0});
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1.0, 0.0});
        OpenMapRealVector proj = u.projection(v);
        assertEquals(3.0, proj.getEntry(0), EPSILON);
        assertEquals(0.0, proj.getEntry(1), EPSILON);
    }

    @Test
    public void testToArray_returnsFullArray() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1.0, 0.0, 3.0});
        double[] array = v.toArray();
        assertArrayEquals(new double[]{1.0, 0.0, 3.0}, array, EPSILON);
    }

    @Test
    public void testIterator_fullDenseIterator_visitsAllEntries() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1.0, 0.0, 3.0});
        Iterator<RealVector.Entry> it = v.iterator();
        int count = 0;
        while (it.hasNext()) {
            RealVector.Entry e = it.next();
            assertEquals(count, e.getIndex());
            count++;
        }
        assertEquals(3, count);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testIterator_remove_throwsUnsupportedOperationException() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1.0, 2.0});
        Iterator<RealVector.Entry> it = v.iterator();
        it.next();
        it.remove();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSparseIterator_remove_throwsUnsupportedOperationException() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1.0, 2.0});
        Iterator<RealVector.Entry> it = v.sparseIterator();
        it.next();
        it.remove();
    }

    @Test(expected = MatrixIndexException.class)
    public void testSetEntry_invalidIndex_throwsMatrixIndexException() {
        OpenMapRealVector v = new OpenMapRealVector(3);
        v.setEntry(-1, 5.0);
    }

    @Test(expected = MatrixIndexException.class)
    public void testSetSubVector_invalidIndex_throwsMatrixIndexException() {
        OpenMapRealVector v = new OpenMapRealVector(3);
        v.setSubVector(2, new double[]{1.0, 2.0});
    }

    @Test(expected = MatrixIndexException.class)
    public void testGetSubVector_invalidIndex_throwsMatrixIndexException() {
        OpenMapRealVector v = new OpenMapRealVector(3);
        v.getSubVector(2, 5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubtract_dimensionMismatch_throwsIllegalArgumentException() {
        OpenMapRealVector v1 = new OpenMapRealVector(2);
        OpenMapRealVector v2 = new OpenMapRealVector(3);
        v1.subtract(v2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubtract_arrayDimensionMismatch_throwsIllegalArgumentException() {
        OpenMapRealVector v = new OpenMapRealVector(2);
        v.subtract(new double[]{1.0, 2.0, 3.0});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDotProduct_dimensionMismatch_throwsIllegalArgumentException() {
        OpenMapRealVector v1 = new OpenMapRealVector(2);
        OpenMapRealVector v2 = new OpenMapRealVector(3);
        v1.dotProduct(v2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEbeMultiply_dimensionMismatch_throwsIllegalArgumentException() {
        OpenMapRealVector v1 = new OpenMapRealVector(2);
        OpenMapRealVector v2 = new OpenMapRealVector(3);
        v1.ebeMultiply(v2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEbeDivide_dimensionMismatch_throwsIllegalArgumentException() {
        OpenMapRealVector v1 = new OpenMapRealVector(2);
        OpenMapRealVector v2 = new OpenMapRealVector(3);
        v1.ebeDivide(v2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetDistance_dimensionMismatch_throwsIllegalArgumentException() {
        OpenMapRealVector v1 = new OpenMapRealVector(2);
        OpenMapRealVector v2 = new OpenMapRealVector(3);
        v1.getDistance(v2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetL1Distance_dimensionMismatch_throwsIllegalArgumentException() {
        OpenMapRealVector v1 = new OpenMapRealVector(2);
        OpenMapRealVector v2 = new OpenMapRealVector(3);
        v1.getL1Distance(v2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetLInfDistance_dimensionMismatch_throwsIllegalArgumentException() {
        OpenMapRealVector v1 = new OpenMapRealVector(2);
        OpenMapRealVector v2 = new OpenMapRealVector(3);
        v1.getLInfDistance(v2);
    }

    @Test
    public void testEquals_withNaNValues_comparesCorrectly() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{Double.NaN, 2.0});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{Double.NaN, 2.0});
        OpenMapRealVector v3 = new OpenMapRealVector(new double[]{Double.NaN, 3.0});
        assertTrue(v1.equals(v2));
        assertFalse(v1.equals(v3));
    }
}