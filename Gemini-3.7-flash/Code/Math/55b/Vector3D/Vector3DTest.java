package org.apache.commons.math.geometry;

import org.apache.commons.math.exception.MathArithmeticException;
import org.apache.commons.math.util.FastMath;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class Vector3DTest {

    private static final double EPSILON = 1e-10;

    // Tests cross product precision with cancellation (Defects4J Math-55)
    @Test
    public void testCrossProduct_cancellationError_accurateResult() {
        Vector3D v1 = new Vector3D(9070467121.0, 4535233560.0, 1.0);
        Vector3D v2 = new Vector3D(9070467123.0, 4535233561.0, 1.0);
        Vector3D result = Vector3D.crossProduct(v1, v2);

        assertEquals(-1.0, result.getX(), 1e-10);
        assertEquals(2.0, result.getY(), 1e-10);
        assertEquals(1.0, result.getZ(), 1e-10);
    }

    // Tests standard cross product on canonical vectors
    @Test
    public void testCrossProduct_canonicalVectors_returnsOrthogonalVector() {
        Vector3D v1 = Vector3D.PLUS_I;
        Vector3D v2 = Vector3D.PLUS_J;
        Vector3D result = Vector3D.crossProduct(v1, v2);

        assertEquals(0.0, result.getX(), EPSILON);
        assertEquals(0.0, result.getY(), EPSILON);
        assertEquals(1.0, result.getZ(), EPSILON);
    }

    // Tests constructors with different scales and linear combinations
    @Test
    public void testConstructors_variousCombinations_createsExpectedVectors() {
        Vector3D v1 = new Vector3D(1.0, 2.0, 3.0);
        assertEquals(1.0, v1.getX(), EPSILON);
        assertEquals(2.0, v1.getY(), EPSILON);
        assertEquals(3.0, v1.getZ(), EPSILON);

        Vector3D v2 = new Vector3D(2.0, v1);
        assertEquals(2.0, v2.getX(), EPSILON);
        assertEquals(4.0, v2.getY(), EPSILON);
        assertEquals(6.0, v2.getZ(), EPSILON);

        Vector3D v3 = new Vector3D(2.0, v1, -1.0, v2);
        assertEquals(0.0, v3.getX(), EPSILON);
        assertEquals(0.0, v3.getY(), EPSILON);
        assertEquals(0.0, v3.getZ(), EPSILON);

        Vector3D v4 = new Vector3D(1.0, v1, 2.0, v1, 3.0, v1);
        assertEquals(6.0, v4.getX(), EPSILON);
        assertEquals(12.0, v4.getY(), EPSILON);
        assertEquals(18.0, v4.getZ(), EPSILON);

        Vector3D v5 = new Vector3D(1.0, v1, 1.0, v1, 1.0, v1, 1.0, v1);
        assertEquals(4.0, v5.getX(), EPSILON);
        assertEquals(8.0, v5.getY(), EPSILON);
        assertEquals(12.0, v5.getZ(), EPSILON);

        Vector3D spherical = new Vector3D(0.0, 0.0);
        assertEquals(1.0, spherical.getX(), EPSILON);
        assertEquals(0.0, spherical.getY(), EPSILON);
        assertEquals(0.0, spherical.getZ(), EPSILON);
    }

    // Tests norm calculations: L1, L2, L2 squared, Linf
    @Test
    public void testNorms_standardVector_returnsCorrectValues() {
        Vector3D v = new Vector3D(1.0, -2.0, 2.0);

        assertEquals(5.0, v.getNorm1(), EPSILON);
        assertEquals(3.0, v.getNorm(), EPSILON);
        assertEquals(9.0, v.getNormSq(), EPSILON);
        assertEquals(2.0, v.getNormInf(), EPSILON);
    }

    // Tests spherical angles alpha and delta
    @Test
    public void testAngles_alphaAndDelta_returnsCorrectAngles() {
        Vector3D v = new Vector3D(0.0, 2.0, 0.0);
        assertEquals(FastMath.PI / 2.0, v.getAlpha(), EPSILON);
        assertEquals(0.0, v.getDelta(), EPSILON);
    }

    // Tests vector addition, subtraction, multiplication, and negation
    @Test
    public void testArithmeticOperations_validVectors_returnsCorrectResults() {
        Vector3D v1 = new Vector3D(1.0, 2.0, 3.0);
        Vector3D v2 = new Vector3D(4.0, 5.0, 6.0);

        Vector3D added = v1.add(v2);
        assertEquals(5.0, added.getX(), EPSILON);
        assertEquals(7.0, added.getY(), EPSILON);
        assertEquals(9.0, added.getZ(), EPSILON);

        Vector3D scaledAdd = v1.add(2.0, v2);
        assertEquals(9.0, scaledAdd.getX(), EPSILON);
        assertEquals(12.0, scaledAdd.getY(), EPSILON);
        assertEquals(15.0, scaledAdd.getZ(), EPSILON);

        Vector3D sub = v1.subtract(v2);
        assertEquals(-3.0, sub.getX(), EPSILON);
        assertEquals(-3.0, sub.getY(), EPSILON);
        assertEquals(-3.0, sub.getZ(), EPSILON);

        Vector3D scaledSub = v1.subtract(2.0, v2);
        assertEquals(-7.0, scaledSub.getX(), EPSILON);
        assertEquals(-8.0, scaledSub.getY(), EPSILON);
        assertEquals(-9.0, scaledSub.getZ(), EPSILON);

        Vector3D negated = v1.negate();
        assertEquals(-1.0, negated.getX(), EPSILON);
        assertEquals(-2.0, negated.getY(), EPSILON);
        assertEquals(-3.0, negated.getZ(), EPSILON);

        Vector3D multiplied = v1.scalarMultiply(3.0);
        assertEquals(3.0, multiplied.getX(), EPSILON);
        assertEquals(6.0, multiplied.getY(), EPSILON);
        assertEquals(9.0, multiplied.getZ(), EPSILON);
    }

    // Tests normalization of a non-zero vector
    @Test
    public void testNormalize_nonZeroVector_returnsUnitVector() {
        Vector3D v = new Vector3D(0.0, 3.0, 4.0);
        Vector3D normalized = v.normalize();

        assertEquals(0.0, normalized.getX(), EPSILON);
        assertEquals(0.6, normalized.getY(), EPSILON);
        assertEquals(0.8, normalized.getZ(), EPSILON);
        assertEquals(1.0, normalized.getNorm(), EPSILON);
    }

    // Tests normalization of zero vector throws MathArithmeticException
    @Test(expected = MathArithmeticException.class)
    public void testNormalize_zeroVector_throwsMathArithmeticException() {
        Vector3D.ZERO.normalize();
    }

    // Tests orthogonal method branches (x <= threshold, y <= threshold, and else)
    @Test
    public void testOrthogonal_differentVectors_returnsOrthogonalVectors() {
        Vector3D vX = new Vector3D(0.1, 2.0, 3.0);
        Vector3D oX = vX.orthogonal();
        assertEquals(0.0, Vector3D.dotProduct(vX, oX), EPSILON);
        assertEquals(1.0, oX.getNorm(), EPSILON);

        Vector3D vY = new Vector3D(3.0, 0.1, 2.0);
        Vector3D oY = vY.orthogonal();
        assertEquals(0.0, Vector3D.dotProduct(vY, oY), EPSILON);
        assertEquals(1.0, oY.getNorm(), EPSILON);

        Vector3D vZ = new Vector3D(3.0, 2.0, 0.1);
        Vector3D oZ = vZ.orthogonal();
        assertEquals(0.0, Vector3D.dotProduct(vZ, oZ), EPSILON);
        assertEquals(1.0, oZ.getNorm(), EPSILON);
    }

    // Tests orthogonal on zero vector throws MathArithmeticException
    @Test(expected = MathArithmeticException.class)
    public void testOrthogonal_zeroVector_throwsMathArithmeticException() {
        Vector3D.ZERO.orthogonal();
    }

    // Tests angle between vectors across different branches (aligned, opposite, separated)
    @Test
    public void testAngle_variousAlignments_returnsCorrectAngle() {
        Vector3D v1 = new Vector3D(1.0, 0.0, 0.0);
        Vector3D v2 = new Vector3D(0.0, 1.0, 0.0);
        assertEquals(FastMath.PI / 2.0, Vector3D.angle(v1, v2), EPSILON);

        Vector3D v3 = new Vector3D(1.0, 1e-6, 0.0);
        assertTrue(Vector3D.angle(v1, v3) < 1e-4);

        Vector3D v4 = new Vector3D(-1.0, 1e-6, 0.0);
        assertTrue(FastMath.PI - Vector3D.angle(v1, v4) < 1e-4);
    }

    // Tests angle with zero vector throws MathArithmeticException
    @Test(expected = MathArithmeticException.class)
    public void testAngle_zeroVector_throwsMathArithmeticException() {
        Vector3D.angle(Vector3D.ZERO, Vector3D.PLUS_I);
    }

    // Tests dotProduct computation
    @Test
    public void testDotProduct_perpendicularAndParallel_returnsCorrectValues() {
        assertEquals(0.0, Vector3D.dotProduct(Vector3D.PLUS_I, Vector3D.PLUS_J), EPSILON);
        assertEquals(1.0, Vector3D.dotProduct(Vector3D.PLUS_I, Vector3D.PLUS_I), EPSILON);
        assertEquals(-1.0, Vector3D.dotProduct(Vector3D.PLUS_I, Vector3D.MINUS_I), EPSILON);
    }

    // Tests distance functions (L1, L2, L2 squared, Linf)
    @Test
    public void testDistance_variousMetrics_returnsCorrectDistances() {
        Vector3D v1 = new Vector3D(1.0, 2.0, 3.0);
        Vector3D v2 = new Vector3D(4.0, 6.0, 3.0);

        assertEquals(7.0, Vector3D.distance1(v1, v2), EPSILON);
        assertEquals(5.0, Vector3D.distance(v1, v2), EPSILON);
        assertEquals(25.0, Vector3D.distanceSq(v1, v2), EPSILON);
        assertEquals(4.0, Vector3D.distanceInf(v1, v2), EPSILON);
    }

    // Tests isNaN and isInfinite methods
    @Test
    public void testSpecialValues_isNaNAndIsInfinite_returnsExpectedBooleans() {
        assertFalse(Vector3D.PLUS_I.isNaN());
        assertFalse(Vector3D.PLUS_I.isInfinite());

        assertTrue(Vector3D.NaN.isNaN());
        assertFalse(Vector3D.NaN.isInfinite());

        Vector3D withNan = new Vector3D(Double.NaN, 0.0, 0.0);
        assertTrue(withNan.isNaN());
        assertFalse(withNan.isInfinite());

        assertTrue(Vector3D.POSITIVE_INFINITY.isInfinite());
        assertFalse(Vector3D.POSITIVE_INFINITY.isNaN());

        Vector3D withInf = new Vector3D(0.0, Double.NEGATIVE_INFINITY, 0.0);
        assertTrue(withInf.isInfinite());
    }

    // Tests equals and hashCode consistency
    @Test
    public void testEqualsAndHashCode_variousCases_returnsExpectedResults() {
        Vector3D v1 = new Vector3D(1.0, 2.0, 3.0);
        Vector3D v2 = new Vector3D(1.0, 2.0, 3.0);
        Vector3D v3 = new Vector3D(1.0, 2.0, 4.0);

        assertTrue(v1.equals(v1));
        assertTrue(v1.equals(v2));
        assertEquals(v1.hashCode(), v2.hashCode());

        assertFalse(v1.equals(v3));
        assertFalse(v1.equals(null));
        assertFalse(v1.equals("Not a Vector3D"));

        assertTrue(Vector3D.NaN.equals(new Vector3D(Double.NaN, 1.0, 2.0)));
        assertEquals(Vector3D.NaN.hashCode(), new Vector3D(Double.NaN, 0.0, 0.0).hashCode());
    }

    // Tests toString format representation
    @Test
    public void testToString_validVector_returnsFormattedString() {
        Vector3D v = new Vector3D(1.0, 2.0, 3.0);
        String str = v.toString();
        assertNotNull(str);
        assertTrue(str.contains("1"));
        assertTrue(str.contains("2"));
        assertTrue(str.contains("3"));
    }
}