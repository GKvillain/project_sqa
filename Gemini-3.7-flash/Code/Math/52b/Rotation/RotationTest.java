package org.apache.commons.math.geometry.euclidean.threed;

import org.apache.commons.math.util.FastMath;
import org.junit.Test;
import static org.junit.Assert.*;

public class RotationTest {

    private static final double EPSILON = 1.0e-10;

    // Tests identity rotation constants and default properties
    @Test
    public void testIdentity_defaultConstant_hasExpectedValues() {
        Rotation r = Rotation.IDENTITY;
        assertEquals(1.0, r.getQ0(), EPSILON);
        assertEquals(0.0, r.getQ1(), EPSILON);
        assertEquals(0.0, r.getQ2(), EPSILON);
        assertEquals(0.0, r.getQ3(), EPSILON);
        assertEquals(0.0, r.getAngle(), EPSILON);
        assertEquals(1.0, r.getAxis().getX(), EPSILON);
        assertEquals(0.0, r.getAxis().getY(), EPSILON);
        assertEquals(0.0, r.getAxis().getZ(), EPSILON);
    }

    // Tests quaternion constructor with normalization enabled
    @Test
    public void testConstructor_quaternionWithNormalization_normalizesCoordinates() {
        Rotation r = new Rotation(2.0, 2.0, 2.0, 2.0, true);
        assertEquals(0.5, r.getQ0(), EPSILON);
        assertEquals(0.5, r.getQ1(), EPSILON);
        assertEquals(0.5, r.getQ2(), EPSILON);
        assertEquals(0.5, r.getQ3(), EPSILON);
    }

    // Tests quaternion constructor without normalization
    @Test
    public void testConstructor_quaternionWithoutNormalization_preservesCoordinates() {
        Rotation r = new Rotation(1.0, 0.0, 0.0, 0.0, false);
        assertEquals(1.0, r.getQ0(), EPSILON);
        assertEquals(0.0, r.getQ1(), EPSILON);
        assertEquals(0.0, r.getQ2(), EPSILON);
        assertEquals(0.0, r.getQ3(), EPSILON);
    }

    // Tests axis and angle constructor with normal vector
    @Test
    public void testConstructor_axisAndAngle_createsCorrectRotation() {
        Rotation r = new Rotation(Vector3D.PLUS_K, FastMath.PI / 2.0);
        Vector3D v = r.applyTo(Vector3D.PLUS_I);
        assertEquals(0.0, v.getX(), EPSILON);
        assertEquals(1.0, v.getY(), EPSILON);
        assertEquals(0.0, v.getZ(), EPSILON);
        assertEquals(FastMath.PI / 2.0, r.getAngle(), EPSILON);
    }

    // Tests zero norm axis constructor throws ArithmeticException
    @Test(expected = ArithmeticException.class)
    public void testConstructor_zeroNormAxis_throwsArithmeticException() {
        new Rotation(Vector3D.ZERO, FastMath.PI / 2.0);
    }

    // Tests matrix constructor with valid orthogonal rotation matrix
    @Test
    public void testConstructor_validMatrix_createsCorrectRotation() throws NotARotationMatrixException {
        double[][] m = new double[][] {
            { 0.0, -1.0, 0.0 },
            { 1.0,  0.0, 0.0 },
            { 0.0,  0.0, 1.0 }
        };
        Rotation r = new Rotation(m, 1.0e-7);
        Vector3D v = r.applyTo(Vector3D.PLUS_I);
        assertEquals(0.0, v.getX(), EPSILON);
        assertEquals(1.0, v.getY(), EPSILON);
        assertEquals(0.0, v.getZ(), EPSILON);
    }

    // Tests matrix constructor with invalid matrix dimensions
    @Test(expected = NotARotationMatrixException.class)
    public void testConstructor_invalidMatrixDimensions_throwsException() throws NotARotationMatrixException {
        double[][] m = new double[][] {
            { 1.0, 0.0 },
            { 0.0, 1.0 }
        };
        new Rotation(m, 1.0e-7);
    }

    // Tests matrix constructor with negative determinant matrix
    @Test(expected = NotARotationMatrixException.class)
    public void testConstructor_negativeDeterminantMatrix_throwsException() throws NotARotationMatrixException {
        double[][] m = new double[][] {
            { -1.0,  0.0,  0.0 },
            {  0.0, -1.0,  0.0 },
            {  0.0,  0.0, -1.0 }
        };
        new Rotation(m, 1.0e-7);
    }

    // Tests two vector pairs constructor with non-orthogonal target
    @Test
    public void testConstructor_vectorPairs_createsRotationTransformingPairs() {
        Vector3D u1 = new Vector3D(1.0, 0.0, 0.0);
        Vector3D u2 = new Vector3D(0.0, 0.0, 1.0);
        Vector3D v1 = new Vector3D(0.0, 1.0, 0.0);
        Vector3D v2 = new Vector3D(-0.5, 0.5, 0.0);
        Rotation r = new Rotation(u1, u2, v1, v2);

        assertFalse(Double.isNaN(r.getQ0()));
        assertFalse(Double.isNaN(r.getQ1()));
        assertFalse(Double.isNaN(r.getQ2()));
        assertFalse(Double.isNaN(r.getQ3()));

        Vector3D resU1 = r.applyTo(u1);
        assertEquals(0.0, resU1.subtract(v1).getNorm(), EPSILON);
    }

    // Tests two vector pairs constructor with identical pairs (identity)
    @Test
    public void testConstructor_identicalVectorPairs_createsIdentityRotation() {
        Vector3D u1 = new Vector3D(1.0, 0.0, 0.0);
        Vector3D u2 = new Vector3D(0.0, 1.0, 0.0);
        Rotation r = new Rotation(u1, u2, u1, u2);
        assertEquals(1.0, r.getQ0(), EPSILON);
        assertEquals(0.0, r.getQ1(), EPSILON);
        assertEquals(0.0, r.getQ2(), EPSILON);
        assertEquals(0.0, r.getQ3(), EPSILON);
    }

    // Tests two vector pairs constructor with zero norm vector throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_vectorPairsWithZeroNorm_throwsIllegalArgumentException() {
        new Rotation(Vector3D.ZERO, Vector3D.PLUS_I, Vector3D.PLUS_J, Vector3D.PLUS_K);
    }

    // Tests two vectors constructor with opposite directions
    @Test
    public void testConstructor_oppositeVectors_rotatesPI() {
        Vector3D u = new Vector3D(1.0, 0.0, 0.0);
        Vector3D v = new Vector3D(-1.0, 0.0, 0.0);
        Rotation r = new Rotation(u, v);
        Vector3D res = r.applyTo(u);
        assertEquals(-1.0, res.getX(), EPSILON);
        assertEquals(0.0, res.getY(), EPSILON);
        assertEquals(0.0, res.getZ(), EPSILON);
        assertEquals(FastMath.PI, r.getAngle(), EPSILON);
    }

    // Tests two vectors constructor with standard angle
    @Test
    public void testConstructor_orthogonalVectors_rotatesHalfPI() {
        Vector3D u = Vector3D.PLUS_I;
        Vector3D v = Vector3D.PLUS_J;
        Rotation r = new Rotation(u, v);
        Vector3D res = r.applyTo(u);
        assertEquals(0.0, res.getX(), EPSILON);
        assertEquals(1.0, res.getY(), EPSILON);
        assertEquals(0.0, res.getZ(), EPSILON);
    }

    // Tests two vectors constructor with zero vector throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_twoVectorsWithZeroNorm_throwsIllegalArgumentException() {
        new Rotation(Vector3D.ZERO, Vector3D.PLUS_I);
    }

    // Tests Cardan/Euler angles constructor and getAngles for XYZ
    @Test
    public void testEulerAngles_XYZ_roundTrip() throws CardanEulerSingularityException {
        double alpha1 = 0.1;
        double alpha2 = 0.2;
        double alpha3 = 0.3;
        Rotation r = new Rotation(RotationOrder.XYZ, alpha1, alpha2, alpha3);
        double[] angles = r.getAngles(RotationOrder.XYZ);
        assertEquals(alpha1, angles[0], EPSILON);
        assertEquals(alpha2, angles[1], EPSILON);
        assertEquals(alpha3, angles[2], EPSILON);
    }

    // Tests Euler angles singularity detection throwing CardanEulerSingularityException
    @Test(expected = CardanEulerSingularityException.class)
    public void testEulerAngles_singularConfiguration_throwsSingularityException() throws CardanEulerSingularityException {
        Rotation r = new Rotation(RotationOrder.XYZ, 0.1, FastMath.PI / 2.0, 0.3);
        r.getAngles(RotationOrder.XYZ);
    }

    // Tests revert method
    @Test
    public void testRevert_appliedToVector_reversesRotation() {
        Rotation r = new Rotation(Vector3D.PLUS_K, FastMath.PI / 3.0);
        Rotation rev = r.revert();
        Vector3D v = new Vector3D(1.0, 2.0, 3.0);
        Vector3D transformed = r.applyTo(v);
        Vector3D reverted = rev.applyTo(transformed);
        assertEquals(v.getX(), reverted.getX(), EPSILON);
        assertEquals(v.getY(), reverted.getY(), EPSILON);
        assertEquals(v.getZ(), reverted.getZ(), EPSILON);
    }

    // Tests applyInverseTo on vector
    @Test
    public void testApplyInverseTo_vector_returnsOriginalVector() {
        Rotation r = new Rotation(Vector3D.PLUS_J, 0.75);
        Vector3D v = new Vector3D(2.0, -1.0, 4.0);
        Vector3D direct = r.applyTo(v);
        Vector3D inverse = r.applyInverseTo(direct);
        assertEquals(v.getX(), inverse.getX(), EPSILON);
        assertEquals(v.getY(), inverse.getY(), EPSILON);
        assertEquals(v.getZ(), inverse.getZ(), EPSILON);
    }

    // Tests applyTo and applyInverseTo on Rotation composition
    @Test
    public void testApplyToRotation_composition_combinesRotations() {
        Rotation r1 = new Rotation(Vector3D.PLUS_I, 0.5);
        Rotation r2 = new Rotation(Vector3D.PLUS_J, 0.3);
        Rotation combined = r1.applyTo(r2);
        Rotation inverseCombined = r1.applyInverseTo(combined);

        assertEquals(0.0, Rotation.distance(r2, inverseCombined), EPSILON);
    }

    // Tests getMatrix representation of rotation
    @Test
    public void testGetMatrix_convertedToMatrix_transformsVectorCorrectly() {
        Rotation r = new Rotation(Vector3D.PLUS_K, FastMath.PI / 2.0);
        double[][] m = r.getMatrix();
        Vector3D u = new Vector3D(1.0, 0.0, 0.0);
        double x = m[0][0] * u.getX() + m[0][1] * u.getY() + m[0][2] * u.getZ();
        double y = m[1][0] * u.getX() + m[1][1] * u.getY() + m[1][2] * u.getZ();
        double z = m[2][0] * u.getX() + m[2][1] * u.getY() + m[2][2] * u.getZ();

        assertEquals(0.0, x, EPSILON);
        assertEquals(1.0, y, EPSILON);
        assertEquals(0.0, z, EPSILON);
    }

    // Tests distance between identical and distinct rotations
    @Test
    public void testDistance_variousRotations_computesCorrectDistance() {
        Rotation r1 = new Rotation(Vector3D.PLUS_K, 0.2);
        Rotation r2 = new Rotation(Vector3D.PLUS_K, 0.5);
        assertEquals(0.0, Rotation.distance(r1, r1), EPSILON);
        assertEquals(0.3, Rotation.distance(r1, r2), EPSILON);
    }
}