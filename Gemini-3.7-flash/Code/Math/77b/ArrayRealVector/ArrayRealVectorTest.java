package org.apache.commons.math.linear;

import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.junit.Test;
import static org.junit.Assert.*;

public class ArrayRealVectorTest {

    private static final double DELTA = 1e-12;

    // Tests getLInfNorm to detect bug where max is accumulated incorrectly
    @Test
    public void testGetLInfNorm_multipleElements_returnsMaxAbsoluteValue() {
        ArrayRealVector v = new ArrayRealVector(new double[] { 1.0, -5.0, 3.0 });
        assertEquals(5.0, v.getLInfNorm(), DELTA);
    }

    // Tests getNorm and getL1Norm with normal vector
    @Test
    public void testGetNormAndL1Norm_validVector_returnsCorrectNorms() {
        ArrayRealVector v = new ArrayRealVector(new double[] { 3.0, -4.0 });
        assertEquals(5.0, v.getNorm(), DELTA);
        assertEquals(7.0, v.getL1Norm(), DELTA);
    }

    // Tests default and sized constructors
    @Test
    public void testConstructors_sizeAndPreset_initializedCorrectly() {
        ArrayRealVector v0 = new ArrayRealVector();
        assertEquals(0, v0.getDimension());

        ArrayRealVector v1 = new ArrayRealVector(3);
        assertEquals(3, v1.getDimension());
        assertEquals(0.0, v1.getEntry(0), DELTA);

        ArrayRealVector v2 = new ArrayRealVector(3, 4.5);
        assertEquals(3, v2.getDimension());
        assertEquals(4.5, v2.getEntry(0), DELTA);
        assertEquals(4.5, v2.getEntry(2), DELTA);
    }

    // Tests array copy and reference constructors
    @Test
    public void testConstructors_arrayCopyAndRef_handlesDataCorrectly() {
        double[] data = new double[] { 1.0, 2.0, 3.0 };
        ArrayRealVector vCopy = new ArrayRealVector(data, true);
        ArrayRealVector vRef = new ArrayRealVector(data, false);

        data[0] = 99.0;
        assertEquals(1.0, vCopy.getEntry(0), DELTA);
        assertEquals(99.0, vRef.getEntry(0), DELTA);
        assertArrayEquals(data, vRef.getDataRef(), DELTA);
    }

    // Tests null input in constructor
    @Test(expected = NullPointerException.class)
    public void testConstructor_nullArray_throwsNullPointerException() {
        new ArrayRealVector((double[]) null, true);
    }

    // Tests empty array in constructor
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_emptyArray_throwsIllegalArgumentException() {
        new ArrayRealVector(new double[0], true);
    }

    // Tests sub-array constructors with valid and invalid bounds
    @Test
    public void testConstructors_subArray_copiesExpectedRange() {
        double[] d = new double[] { 1.0, 2.0, 3.0, 4.0, 5.0 };
        ArrayRealVector v = new ArrayRealVector(d, 1, 3);
        assertEquals(3, v.getDimension());
        assertEquals(2.0, v.getEntry(0), DELTA);
        assertEquals(4.0, v.getEntry(2), DELTA);

        Double[] objD = new Double[] { 1.0, 2.0, 3.0, 4.0 };
        ArrayRealVector vObj = new ArrayRealVector(objD, 0, 2);
        assertEquals(2, vObj.getDimension());
        assertEquals(1.0, vObj.getEntry(0), DELTA);
    }

    // Tests sub-array constructor out of bounds
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_subArrayOutOfBounds_throwsIllegalArgumentException() {
        double[] d = new double[] { 1.0, 2.0 };
        new ArrayRealVector(d, 1, 2);
    }

    // Tests copy constructors and appending constructors
    @Test
    public void testConstructors_appendAndCopy_concatenatesCorrectly() {
        ArrayRealVector v1 = new ArrayRealVector(new double[] { 1.0, 2.0 });
        ArrayRealVector v2 = new ArrayRealVector(new double[] { 3.0, 4.0 });

        ArrayRealVector combined = new ArrayRealVector(v1, v2);
        assertEquals(4, combined.getDimension());
        assertEquals(1.0, combined.getEntry(0), DELTA);
        assertEquals(4.0, combined.getEntry(3), DELTA);

        ArrayRealVector combinedWithArr = new ArrayRealVector(v1, new double[] { 5.0 });
        assertEquals(3, combinedWithArr.getDimension());
        assertEquals(5.0, combinedWithArr.getEntry(2), DELTA);

        ArrayRealVector combinedArrVec = new ArrayRealVector(new double[] { 0.0 }, v2);
        assertEquals(3, combinedArrVec.getDimension());
        assertEquals(0.0, combinedArrVec.getEntry(0), DELTA);
        assertEquals(3.0, combinedArrVec.getEntry(1), DELTA);
    }

    // Tests addition and subtraction with ArrayRealVector and double array
    @Test
    public void testAddAndSubtract_validInputs_returnsCorrectResults() {
        ArrayRealVector v1 = new ArrayRealVector(new double[] { 1.0, 2.0, 3.0 });
        ArrayRealVector v2 = new ArrayRealVector(new double[] { 4.0, 5.0, 6.0 });

        RealVector sum = v1.add(v2);
        assertEquals(5.0, sum.getEntry(0), DELTA);
        assertEquals(7.0, sum.getEntry(1), DELTA);
        assertEquals(9.0, sum.getEntry(2), DELTA);

        RealVector diff = v1.subtract(new double[] { 1.0, 1.0, 1.0 });
        assertEquals(0.0, diff.getEntry(0), DELTA);
        assertEquals(1.0, diff.getEntry(1), DELTA);
        assertEquals(2.0, diff.getEntry(2), DELTA);
    }

    // Tests dimension mismatch exception during addition
    @Test(expected = IllegalArgumentException.class)
    public void testAdd_dimensionMismatch_throwsIllegalArgumentException() {
        ArrayRealVector v1 = new ArrayRealVector(new double[] { 1.0, 2.0 });
        ArrayRealVector v2 = new ArrayRealVector(new double[] { 1.0 });
        v1.add(v2);
    }

    // Tests element-by-element multiplication and division
    @Test
    public void testEbeMultiplyAndDivide_validInputs_returnsCorrectResults() {
        ArrayRealVector v1 = new ArrayRealVector(new double[] { 6.0, 8.0 });
        ArrayRealVector v2 = new ArrayRealVector(new double[] { 2.0, 4.0 });

        ArrayRealVector mult = v1.ebeMultiply(v2);
        assertEquals(12.0, mult.getEntry(0), DELTA);
        assertEquals(32.0, mult.getEntry(1), DELTA);

        ArrayRealVector div = v1.ebeDivide(v2);
        assertEquals(3.0, div.getEntry(0), DELTA);
        assertEquals(2.0, div.getEntry(1), DELTA);
    }

    // Tests map operations modifying vector in-place
    @Test
    public void testMapToSelf_variousOperations_modifiesDataCorrectly() {
        ArrayRealVector v = new ArrayRealVector(new double[] { 1.0, -2.0, 3.0 });
        v.mapAddToSelf(2.0);
        assertEquals(3.0, v.getEntry(0), DELTA);
        assertEquals(0.0, v.getEntry(1), DELTA);
        assertEquals(5.0, v.getEntry(2), DELTA);

        v.mapMultiplyToSelf(2.0);
        assertEquals(6.0, v.getEntry(0), DELTA);

        v.mapDivideToSelf(2.0);
        assertEquals(3.0, v.getEntry(0), DELTA);

        v.mapAbsToSelf();
        assertEquals(3.0, v.getEntry(0), DELTA);
    }

    // Tests dotProduct, outerProduct, and projection
    @Test
    public void testVectorProductsAndProjection_validVectors_computesCorrectly() {
        ArrayRealVector v1 = new ArrayRealVector(new double[] { 1.0, 2.0 });
        ArrayRealVector v2 = new ArrayRealVector(new double[] { 3.0, 4.0 });

        double dot = v1.dotProduct(v2);
        assertEquals(11.0, dot, DELTA);

        RealMatrix outer = v1.outerProduct(v2);
        assertEquals(3.0, outer.getEntry(0, 0), DELTA);
        assertEquals(4.0, outer.getEntry(0, 1), DELTA);
        assertEquals(6.0, outer.getEntry(1, 0), DELTA);
        assertEquals(8.0, outer.getEntry(1, 1), DELTA);

        ArrayRealVector proj = v1.projection(v2);
        double scale = 11.0 / 25.0;
        assertEquals(3.0 * scale, proj.getEntry(0), DELTA);
        assertEquals(4.0 * scale, proj.getEntry(1), DELTA);
    }

    // Tests distance calculations (L2, L1, Linf)
    @Test
    public void testDistances_validVectors_returnsCorrectDistances() {
        ArrayRealVector v1 = new ArrayRealVector(new double[] { 1.0, 2.0 });
        ArrayRealVector v2 = new ArrayRealVector(new double[] { 4.0, 6.0 });

        assertEquals(5.0, v1.getDistance(v2), DELTA);
        assertEquals(7.0, v1.getL1Distance(v2), DELTA);
        assertEquals(4.0, v1.getLInfDistance(v2), DELTA);
    }

    // Tests unitVector and unitize methods
    @Test
    public void testUnitVectorAndUnitize_nonZeroVector_normalizesCorrectly() {
        ArrayRealVector v = new ArrayRealVector(new double[] { 0.0, 3.0, 4.0 });
        RealVector unit = v.unitVector();
        assertEquals(1.0, unit.getNorm(), DELTA);
        assertEquals(0.6, unit.getEntry(1), DELTA);
        assertEquals(0.8, unit.getEntry(2), DELTA);

        v.unitize();
        assertEquals(1.0, v.getNorm(), DELTA);
    }

    // Tests unitVector on zero norm vector throwing ArithmeticException
    @Test(expected = ArithmeticException.class)
    public void testUnitVector_zeroNorm_throwsArithmeticException() {
        ArrayRealVector v = new ArrayRealVector(new double[] { 0.0, 0.0 });
        v.unitVector();
    }

    // Tests subvector getters, setters, and entry modification
    @Test
    public void testSubVectorAndEntryOperations_validIndices_updatesState() {
        ArrayRealVector v = new ArrayRealVector(new double[] { 1.0, 2.0, 3.0, 4.0 });

        RealVector sub = v.getSubVector(1, 2);
        assertEquals(2, sub.getDimension());
        assertEquals(2.0, sub.getEntry(0), DELTA);
        assertEquals(3.0, sub.getEntry(1), DELTA);

        v.setEntry(0, 10.0);
        assertEquals(10.0, v.getEntry(0), DELTA);

        v.setSubVector(1, new double[] { 20.0, 30.0 });
        assertEquals(20.0, v.getEntry(1), DELTA);
        assertEquals(30.0, v.getEntry(2), DELTA);

        v.set(5.0);
        assertEquals(5.0, v.getEntry(0), DELTA);
        assertEquals(5.0, v.getEntry(3), DELTA);
    }

    // Tests append scalar and double array
    @Test
    public void testAppend_scalarAndArray_returnsAppendedVector() {
        ArrayRealVector v = new ArrayRealVector(new double[] { 1.0, 2.0 });
        RealVector vScalar = v.append(3.0);
        assertEquals(3, vScalar.getDimension());
        assertEquals(3.0, vScalar.getEntry(2), DELTA);

        RealVector vArr = v.append(new double[] { 4.0, 5.0 });
        assertEquals(4, vArr.getDimension());
        assertEquals(5.0, vArr.getEntry(3), DELTA);
    }

    // Tests isNaN, isInfinite, equals, and hashCode
    @Test
    public void testSpecialValuesAndEquality_nanAndInfinite_behavesCorrectly() {
        ArrayRealVector vNormal1 = new ArrayRealVector(new double[] { 1.0, 2.0 });
        ArrayRealVector vNormal2 = new ArrayRealVector(new double[] { 1.0, 2.0 });
        ArrayRealVector vNaN = new ArrayRealVector(new double[] { Double.NaN, 2.0 });
        ArrayRealVector vInf = new ArrayRealVector(new double[] { Double.POSITIVE_INFINITY, 2.0 });

        assertFalse(vNormal1.isNaN());
        assertFalse(vNormal1.isInfinite());
        assertTrue(vNaN.isNaN());
        assertFalse(vNaN.isInfinite());
        assertTrue(vInf.isInfinite());
        assertFalse(vInf.isNaN());

        assertEquals(vNormal1, vNormal2);
        assertEquals(vNormal1.hashCode(), vNormal2.hashCode());
        assertFalse(vNormal1.equals(vNaN));
        assertFalse(vNormal1.equals(null));
        assertTrue(vNormal1.equals(vNormal1));
    }

    // Additional Constructors Coverage
    @Test
    public void testAdditionalConstructors() {
        Double[] dBoxed = new Double[] { 1.0, 2.0, 3.0 };
        ArrayRealVector vFromBoxed = new ArrayRealVector(dBoxed);
        assertEquals(3, vFromBoxed.getDimension());
        assertEquals(2.0, vFromBoxed.getEntry(1), DELTA);

        ArrayRealVector vCopy = new ArrayRealVector(vFromBoxed, true);
        assertEquals(3, vCopy.getDimension());

        ArrayRealVector vRef = new ArrayRealVector(vFromBoxed, false);
        assertEquals(3, vRef.getDimension());

        RealVector rv = new ArrayRealVector(new double[] { 4.0, 5.0 });
        ArrayRealVector vFromRV = new ArrayRealVector(rv);
        assertEquals(2, vFromRV.getDimension());

        ArrayRealVector vCombinedRV = new ArrayRealVector(rv, rv);
        assertEquals(4, vCombinedRV.getDimension());
    }

    // Math Map operations coverage (trigonometric, power, exponential, log, etc.)
    @Test
    public void testMathMapFunctions() {
        ArrayRealVector v = new ArrayRealVector(new double[] { 1.0, 2.0, -3.0 });

        RealVector vAdd = v.mapAdd(2.0);
        assertEquals(3.0, vAdd.getEntry(0), DELTA);

        RealVector vSub = v.mapSubtract(1.0);
        assertEquals(0.0, vSub.getEntry(0), DELTA);

        v.mapSubtractToSelf(1.0);
        assertEquals(0.0, v.getEntry(0), DELTA);
        assertEquals(1.0, v.getEntry(1), DELTA);

        ArrayRealVector vPos = new ArrayRealVector(new double[] { 1.0, 4.0, 9.0 });
        RealVector vSqrt = vPos.mapSqrt();
        assertEquals(2.0, vSqrt.getEntry(1), DELTA);
        assertEquals(3.0, vSqrt.getEntry(2), DELTA);

        RealVector vPow = vPos.mapPow(2.0);
        assertEquals(16.0, vPow.getEntry(1), DELTA);

        vPos.mapPowToSelf(2.0);
        assertEquals(16.0, vPos.getEntry(1), DELTA);

        vPos.mapSqrtToSelf();
        assertEquals(4.0, vPos.getEntry(1), DELTA);

        ArrayRealVector vExp = new ArrayRealVector(new double[] { 0.0, 1.0 });
        assertEquals(1.0, vExp.mapExp().getEntry(0), DELTA);
        vExp.mapExpToSelf();
        assertEquals(1.0, vExp.getEntry(0), DELTA);

        ArrayRealVector vLog = new ArrayRealVector(new double[] { 1.0, Math.E });
        assertEquals(0.0, vLog.mapLog().getEntry(0), DELTA);
        vLog.mapLogToSelf();
        assertEquals(1.0, vLog.getEntry(1), DELTA);

        ArrayRealVector vTrig = new ArrayRealVector(new double[] { 0.0, Math.PI / 2 });
        assertEquals(0.0, vTrig.mapSin().getEntry(0), DELTA);
        assertEquals(1.0, vTrig.mapCos().getEntry(0), DELTA);
        assertEquals(0.0, vTrig.mapTan().getEntry(0), DELTA);

        vTrig.mapSinToSelf();
        assertEquals(0.0, vTrig.getEntry(0), DELTA);

        ArrayRealVector vTrig2 = new ArrayRealVector(new double[] { 0.0, Math.PI });
        vTrig2.mapCosToSelf();
        assertEquals(1.0, vTrig2.getEntry(0), DELTA);

        ArrayRealVector vTrig3 = new ArrayRealVector(new double[] { 0.0 });
        vTrig3.mapTanToSelf();
        assertEquals(0.0, vTrig3.getEntry(0), DELTA);

        ArrayRealVector vArc = new ArrayRealVector(new double[] { 0.0, 1.0 });
        assertEquals(0.0, vArc.mapAsin().getEntry(0), DELTA);
        assertEquals(0.0, vArc.mapAcos().getEntry(1), DELTA);
        assertEquals(0.0, vArc.mapAtan().getEntry(0), DELTA);

        vArc.mapAsinToSelf();
        assertEquals(0.0, vArc.getEntry(0), DELTA);

        ArrayRealVector vArc2 = new ArrayRealVector(new double[] { 1.0 });
        vArc2.mapAcosToSelf();
        assertEquals(0.0, vArc2.getEntry(0), DELTA);

        ArrayRealVector vArc3 = new ArrayRealVector(new double[] { 0.0 });
        vArc3.mapAtanToSelf();
        assertEquals(0.0, vArc3.getEntry(0), DELTA);

        ArrayRealVector vHyp = new ArrayRealVector(new double[] { 0.0 });
        assertEquals(0.0, vHyp.mapSinh().getEntry(0), DELTA);
        assertEquals(1.0, vHyp.mapCosh().getEntry(0), DELTA);
        assertEquals(0.0, vHyp.mapTanh().getEntry(0), DELTA);

        vHyp.mapSinhToSelf();
        assertEquals(0.0, vHyp.getEntry(0), DELTA);

        ArrayRealVector vHyp2 = new ArrayRealVector(new double[] { 0.0 });
        vHyp2.mapCoshToSelf();
        assertEquals(1.0, vHyp2.getEntry(0), DELTA);

        ArrayRealVector vHyp3 = new ArrayRealVector(new double[] { 0.0 });
        vHyp3.mapTanhToSelf();
        assertEquals(0.0, vHyp3.getEntry(0), DELTA);

        ArrayRealVector vMisc = new ArrayRealVector(new double[] { 1.2, -2.8, 8.0 });
        assertEquals(2.0, vMisc.mapCbrt().getEntry(2), DELTA);
        vMisc.mapCbrtToSelf();
        assertEquals(2.0, vMisc.getEntry(2), DELTA);

        ArrayRealVector vCeilFloor = new ArrayRealVector(new double[] { 1.2, -2.8 });
        assertEquals(2.0, vCeilFloor.mapCeil().getEntry(0), DELTA);
        assertEquals(1.0, vCeilFloor.mapFloor().getEntry(0), DELTA);
        assertEquals(1.0, vCeilFloor.mapRint().getEntry(0), DELTA);
        assertEquals(1.0, vCeilFloor.mapSignum().getEntry(0), DELTA);

        vCeilFloor.mapCeilToSelf();
        assertEquals(2.0, vCeilFloor.getEntry(0), DELTA);

        ArrayRealVector vFloor = new ArrayRealVector(new double[] { 1.2 });
        vFloor.mapFloorToSelf();
        assertEquals(1.0, vFloor.getEntry(0), DELTA);

        ArrayRealVector vRint = new ArrayRealVector(new double[] { 1.2 });
        vRint.mapRintToSelf();
        assertEquals(1.0, vRint.getEntry(0), DELTA);

        ArrayRealVector vSig = new ArrayRealVector(new double[] { -5.0 });
        vSig.mapSignumToSelf();
        assertEquals(-1.0, vSig.getEntry(0), DELTA);

        ArrayRealVector vUlp = new ArrayRealVector(new double[] { 1.0 });
        assertEquals(Math.ulp(1.0), vUlp.mapUlp().getEntry(0), DELTA);
        vUlp.mapUlpToSelf();
        assertEquals(Math.ulp(1.0), vUlp.getEntry(0), DELTA);

        ArrayRealVector vInv = new ArrayRealVector(new double[] { 2.0 });
        assertEquals(0.5, vInv.mapInv().getEntry(0), DELTA);
        vInv.mapInvToSelf();
        assertEquals(0.5, vInv.getEntry(0), DELTA);

        ArrayRealVector vAbs = new ArrayRealVector(new double[] { -7.0 });
        assertEquals(7.0, vAbs.mapAbs().getEntry(0), DELTA);
    }

    // Tests custom UnivariateRealFunction mapping
    @Test
    public void testMapWithCustomFunction() throws Exception {
        ArrayRealVector v = new ArrayRealVector(new double[] { 1.0, 2.0, 3.0 });
        UnivariateRealFunction square = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x;
            }
        };

        RealVector mapped = v.map(square);
        assertEquals(4.0, mapped.getEntry(1), DELTA);

        v.mapToSelf(square);
        assertEquals(9.0, v.getEntry(2), DELTA);
    }

    // Tests array and RealVector interaction overloads
    @Test
    public void testArrayAndRealVectorOverloads() {
        ArrayRealVector v1 = new ArrayRealVector(new double[] { 1.0, 2.0, 3.0 });
        double[] arr = new double[] { 2.0, 3.0, 4.0 };

        RealVector addedArr = v1.add(arr);
        assertEquals(3.0, addedArr.getEntry(0), DELTA);

        ArrayRealVector v2 = new ArrayRealVector(new double[] { 0.5, 1.0, 2.0 });
        RealVector subVec = v1.subtract(v2);
        assertEquals(0.5, subVec.getEntry(0), DELTA);

        ArrayRealVector multArr = v1.ebeMultiply(arr);
        assertEquals(2.0, multArr.getEntry(0), DELTA);

        ArrayRealVector divArr = v1.ebeDivide(arr);
        assertEquals(0.5, divArr.getEntry(0), DELTA);

        double dotArr = v1.dotProduct(arr);
        assertEquals(20.0, dotArr, DELTA);

        RealMatrix outerArr = v1.outerProduct(arr);
        assertEquals(2.0, outerArr.getEntry(0, 0), DELTA);

        ArrayRealVector projArr = v1.projection(arr);
        assertNotNull(projArr);

        assertEquals(Math.sqrt(3.0), v1.getDistance(arr), DELTA);
        assertEquals(3.0, v1.getL1Distance(arr), DELTA);
        assertEquals(1.0, v1.getLInfDistance(arr), DELTA);

        ArrayRealVector appendedVec = (ArrayRealVector) v1.append((RealVector) v2);
        assertEquals(6, appendedVec.getDimension());

        ArrayRealVector appendedArrVec = v1.append(v2);
        assertEquals(6, appendedArrVec.getDimension());

        v1.setSubVector(1, (RealVector) new ArrayRealVector(new double[] { 8.0, 9.0 }));
        assertEquals(8.0, v1.getEntry(1), DELTA);
        assertEquals(9.0, v1.getEntry(2), DELTA);

        double[] dataCopy = v1.getData();
        assertEquals(3, dataCopy.length);
        double[] toArr = v1.toArray();
        assertArrayEquals(dataCopy, toArr, DELTA);
    }

    // Tests copy and string representations
    @Test
    public void testCopyAndToString() {
        ArrayRealVector v = new ArrayRealVector(new double[] { 1.0, 2.0 });
        RealVector copy = v.copy();
        assertEquals(v, copy);
        assertNotSame(v, copy);

        String str = v.toString();
        assertNotNull(str);
        assertTrue(str.length() > 0);
    }

    // Tests unitize on zero norm vector
    @Test(expected = ArithmeticException.class)
    public void testUnitize_zeroNorm_throwsArithmeticException() {
        ArrayRealVector v = new ArrayRealVector(new double[] { 0.0, 0.0 });
        v.unitize();
    }

    // Tests out-of-bounds access
    @Test(expected = MatrixIndexException.class)
    public void testGetEntry_outOfBounds_throwsMatrixIndexException() {
        ArrayRealVector v = new ArrayRealVector(new double[] { 1.0, 2.0 });
        v.getEntry(5);
    }

    @Test(expected = MatrixIndexException.class)
    public void testSetEntry_outOfBounds_throwsMatrixIndexException() {
        ArrayRealVector v = new ArrayRealVector(new double[] { 1.0, 2.0 });
        v.setEntry(-1, 0.0);
    }

    @Test(expected = MatrixIndexException.class)
    public void testGetSubVector_outOfBounds_throwsMatrixIndexException() {
        ArrayRealVector v = new ArrayRealVector(new double[] { 1.0, 2.0 });
        v.getSubVector(1, 3);
    }
}