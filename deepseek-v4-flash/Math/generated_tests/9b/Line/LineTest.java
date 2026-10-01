package org.apache.commons.math3.geometry.euclidean.threed;

import static org.junit.Assert.*;
import org.junit.Test;

import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.geometry.euclidean.oned.Vector1D;
import org.apache.commons.math3.util.FastMath;

/**
 * JUnit 4 test class for org.apache.commons.math3.geometry.euclidean.threed.Line.
 * Designed to detect defect in Defects4J bug 9b.
 */
public class LineTest {

    private static final double EPS = 1.0e-10;

    // Tests constructor with two distinct points, normal case
    @Test
    public void testConstructor_validPoints_createsLine() {
        Vector3D p1 = new Vector3D(0, 0, 0);
        Vector3D p2 = new Vector3D(1, 0, 0);
        Line line = new Line(p1, p2);
        assertNotNull(line);
        assertEquals(0.0, line.getOrigin().getX(), EPS);
        assertEquals(0.0, line.getOrigin().getY(), EPS);
        assertEquals(0.0, line.getOrigin().getZ(), EPS);
        assertEquals(1.0, line.getDirection().getNorm(), EPS);
    }

    // Tests constructor with two distinct but not unit direction points
    @Test
    public void testConstructor_nonUnitDelta_createsLine() {
        Vector3D p1 = new Vector3D(1, 1, 1);
        Vector3D p2 = new Vector3D(4, 5, 6);
        Line line = new Line(p1, p2);
        assertEquals(3.0, line.getDirection().getNormSq(), 1.0e-12);
    }

    // Tests exception when points are equal
    @Test(expected = MathIllegalArgumentException.class)
    public void testConstructor_equalPoints_throwsException() {
        Vector3D p1 = new Vector3D(1, 1, 1);
        Vector3D p2 = new Vector3D(1, 1, 1);
        new Line(p1, p2);
    }

    // Tests copy constructor
    @Test
    public void testCopyConstructor_originalLine_createsIndependentCopy() {
        Vector3D p1 = new Vector3D(0, 0, 0);
        Vector3D p2 = new Vector3D(1, 0, 0);
        Line original = new Line(p1, p2);
        Line copy = new Line(original);
        assertNotNull(copy);
        assertEquals(original.getDirection().getX(), copy.getDirection().getX(), EPS);
        assertEquals(original.getOrigin().getX(), copy.getOrigin().getX(), EPS);
        // ensure independent
        original.revert();
        assertEquals(1.0, copy.getDirection().getX(), EPS);
    }

    // Tests reset with valid points
    @Test
    public void testReset_validPoints_updatesLine() {
        Vector3D p1 = new Vector3D(0, 0, 0);
        Vector3D p2 = new Vector3D(1, 0, 0);
        Line line = new Line(p1, p2);
        Vector3D p3 = new Vector3D(0, 0, 0);
        Vector3D p4 = new Vector3D(0, 1, 0);
        line.reset(p3, p4);
        assertEquals(0.0, line.getOrigin().getX(), EPS);
        assertEquals(0.0, line.getOrigin().getY(), EPS);
        assertEquals(0.0, line.getOrigin().getZ(), EPS);
        assertEquals(0.0, line.getDirection().getX(), EPS);
        assertEquals(1.0, line.getDirection().getY(), EPS);
        assertEquals(0.0, line.getDirection().getZ(), EPS);
    }

    // Tests reset with equal points throws exception
    @Test(expected = MathIllegalArgumentException.class)
    public void testReset_equalPoints_throwsException() {
        Vector3D p1 = new Vector3D(0, 0, 0);
        Vector3D p2 = new Vector3D(1, 0, 0);
        Line line = new Line(p1, p2);
        line.reset(p1, p1);
    }

    // Tests revert returns a line with opposite direction
    @Test
    public void testRevert_anyLine_returnsOppositeDirection() {
        Vector3D p1 = new Vector3D(0, 0, 0);
        Vector3D p2 = new Vector3D(1, 1, 0);
        Line line = new Line(p1, p2);
        Line reverted = line.revert();
        assertEquals(-line.getDirection().getX(), reverted.getDirection().getX(), EPS);
        assertEquals(-line.getDirection().getY(), reverted.getDirection().getY(), EPS);
        assertEquals(-line.getDirection().getZ(), reverted.getDirection().getZ(), EPS);
    }

    // Tests revert returns a line with same origin
    @Test
    public void testRevert_anyLine_originSame() {
        Vector3D p1 = new Vector3D(1, 2, 3);
        Vector3D p2 = new Vector3D(4, 5, 6);
        Line line = new Line(p1, p2);
        Line reverted = line.revert();
        assertEquals(line.getOrigin().getX(), reverted.getOrigin().getX(), EPS);
        assertEquals(line.getOrigin().getY(), reverted.getOrigin().getY(), EPS);
        assertEquals(line.getOrigin().getZ(), reverted.getOrigin().getZ(), EPS);
    }

    // Tests getAbscissa for a point on the line
    @Test
    public void testGetAbscissa_pointOnLine_returnsCorrectValue() {
        Vector3D p1 = new Vector3D(0, 0, 0);
        Vector3D p2 = new Vector3D(1, 0, 0);
        Line line = new Line(p1, p2);
        Vector3D point = new Vector3D(5, 0, 0);
        assertEquals(5.0, line.getAbscissa(point), EPS);
    }

    // Tests getAbscissa for a point off the line
    @Test
    public void testGetAbscissa_pointOffLine_returnsCorrectValue() {
        Vector3D p1 = new Vector3D(0, 0, 0);
        Vector3D p2 = new Vector3D(1, 0, 0);
        Line line = new Line(p1, p2);
        Vector3D point = new Vector3D(3, 4, 0);
        assertEquals(3.0, line.getAbscissa(point), EPS);
    }

    // Tests pointAt with positive abscissa
    @Test
    public void testPointAt_positiveAbscissa_returnsCorrectPoint() {
        Vector3D p1 = new Vector3D(0, 0, 0);
        Vector3D p2 = new Vector3D(1, 0, 0);
        Line line = new Line(p1, p2);
        Vector3D point = line.pointAt(2.0);
        assertEquals(2.0, point.getX(), EPS);
        assertEquals(0.0, point.getY(), EPS);
        assertEquals(0.0, point.getZ(), EPS);
    }

    // Tests pointAt with negative abscissa
    @Test
    public void testPointAt_negativeAbscissa_returnsCorrectPoint() {
        Vector3D p1 = new Vector3D(0, 0, 0);
        Vector3D p2 = new Vector3D(1, 0, 0);
        Line line = new Line(p1, p2);
        Vector3D point = line.pointAt(-3.0);
        assertEquals(-3.0, point.getX(), EPS);
    }

    // Tests toSubSpace
    @Test
    public void testToSubSpace_pointOnLine_returnsCorrectAbscissa() {
        Vector3D p1 = new Vector3D(0, 0, 0);
        Vector3D p2 = new Vector3D(1, 1, 0);
        Line line = new Line(p1, p2);
        Vector3D point = new Vector3D(2, 2, 0);
        Vector1D result = line.toSubSpace(point);
        // direction is (1/sqrt2, 1/sqrt2, 0); zero is (0,0,0); abscissa = (2,2,0).dot(direction) = 2*sqrt2
        double expected = 2.0 * FastMath.sqrt(2);
        assertEquals(expected, result.getX(), 1.0e-12);
    }

    // Tests toSpace
    @Test
    public void testToSpace_abscissa_returnsCorrectPoint() {
        Vector3D p1 = new Vector3D(0, 0, 0);
        Vector3D p2 = new Vector3D(1, 0, 0);
        Line line = new Line(p1, p2);
        Vector1D abscissa = new Vector1D(3.0);
        Vector3D point = line.toSpace(abscissa);
        assertEquals(3.0, point.getX(), EPS);
        assertEquals(0.0, point.getY(), EPS);
        assertEquals(0.0, point.getZ(), EPS);
    }

    // Tests isSimilarTo with parallel lines containing same points
    @Test
    public void testIsSimilarTo_sameDirection_returnsTrue() {
        Vector3D p1 = new Vector3D(0, 0, 0);
        Vector3D p2 = new Vector3D(1, 0, 0);
        Line line1 = new Line(p1, p2);
        Line line2 = new Line(new Vector3D(2, 0, 0), new Vector3D(3, 0, 0));
        assertTrue(line1.isSimilarTo(line2));
    }

    // Tests isSimilarTo with opposite direction but same points
    @Test
    public void testIsSimilarTo_oppositeDirection_returnsTrue() {
        Vector3D p1 = new Vector3D(0, 0, 0);
        Vector3D p2 = new Vector3D(1, 0, 0);
        Line line1 = new Line(p1, p2);
        Line line2 = new Line(new Vector3D(1, 0, 0), new Vector3D(0, 0, 0));
        assertTrue(line1.isSimilarTo(line2));
    }

    // Tests isSimilarTo with different lines
    @Test
    public void testIsSimilarTo_differentLines_returnsFalse() {
        Vector3D p1 = new Vector3D(0, 0, 0);
        Vector3D p2 = new Vector3D(1, 0, 0);
        Line line1 = new Line(p1, p2);
        Line line2 = new Line(new Vector3D(0, 1, 0), new Vector3D(1, 1, 0));
        assertFalse(line1.isSimilarTo(line2));
    }

    // Tests contains with point on line
    @Test
    public void testContains_pointOnLine_returnsTrue() {
        Vector3D p1 = new Vector3D(0, 0, 0);
        Vector3D p2 = new Vector3D(1, 0, 0);
        Line line = new Line(p1, p2);
        assertTrue(line.contains(new Vector3D(5, 0, 0)));
    }

    // Tests contains with point off line
    @Test
    public void testContains_pointOffLine_returnsFalse() {
        Vector3D p1 = new Vector3D(0, 0, 0);
        Vector3D p2 = new Vector3D(1, 0, 0);
        Line line = new Line(p1, p2);
        assertFalse(line.contains(new Vector3D(0, 1, 0)));
    }

    // Tests distance to point
    @Test
    public void testDistance_point_onLine_returnsZero() {
        Vector3D p1 = new Vector3D(0, 0, 0);
        Vector3D p2 = new Vector3D(1, 0, 0);
        Line line = new Line(p1, p2);
        assertEquals(0.0, line.distance(new Vector3D(3, 0, 0)), EPS);
    }

    // Tests distance to point off line
    @Test
    public void testDistance_point_offLine_returnsCorrectValue() {
        Vector3D p1 = new Vector3D(0, 0, 0);
        Vector3D p2 = new Vector3D(1, 0, 0);
        Line line = new Line(p1, p2);
        assertEquals(2.0, line.distance(new Vector3D(0, 2, 0)), EPS);
    }

    // Tests distance to parallel line (should use distance to point)
    @Test
    public void testDistance_line_parallel_returnsCorrectValue() {
        Vector3D p1 = new Vector3D(0, 0, 0);
        Vector3D p2 = new Vector3D(1, 0, 0);
        Line line1 = new Line(p1, p2);
        Line line2 = new Line(new Vector3D(0, 3, 0), new Vector3D(1, 3, 0));
        assertEquals(3.0, line1.distance(line2), EPS);
    }

    // Tests distance to intersecting line
    @Test
    public void testDistance_line_intersecting_returnsZero() {
        Vector3D p1 = new Vector3D(0, 0, 0);
        Vector3D p2 = new Vector3D(1, 0, 0);
        Line line1 = new Line(p1, p2);
        Line line2 = new Line(new Vector3D(0, 0, 0), new Vector3D(0, 1, 0));
        assertEquals(0.0, line1.distance(line2), EPS);
    }

    // Tests closestPoint with parallel lines
    @Test
    public void testClosestPoint_parallelLines_returnsOrigin() {
        Vector3D p1 = new Vector3D(0, 0, 0);
        Vector3D p2 = new Vector3D(1, 0, 0);
        Line line1 = new Line(p1, p2);
        Line line2 = new Line(new Vector3D(0, 1, 0), new Vector3D(1, 1, 0));
        Vector3D closest = line1.closestPoint(line2);
        assertEquals(line1.getOrigin().getX(), closest.getX(), EPS);
        assertEquals(line1.getOrigin().getY(), closest.getY(), EPS);
        assertEquals(line1.getOrigin().getZ(), closest.getZ(), EPS);
    }

    // Tests closestPoint with intersecting lines
    @Test
    public void testClosestPoint_intersectingLines_returnsIntersectionPoint() {
        Vector3D p1 = new Vector3D(0, 0, 0);
        Vector3D p2 = new Vector3D(1, 0, 0);
        Line line1 = new Line(p1, p2);
        Line line2 = new Line(new Vector3D(2, 0, 0), new Vector3D(2, 1, 0));
        Vector3D closest = line1.closestPoint(line2);
        assertEquals(2.0, closest.getX(), EPS);
        assertEquals(0.0, closest.getY(), EPS);
        assertEquals(0.0, closest.getZ(), EPS);
    }

    // Tests intersection with intersecting lines
    @Test
    public void testIntersection_intersectingLines_returnsPoint() {
        Vector3D p1 = new Vector3D(0, 0, 0);
        Vector3D p2 = new Vector3D(1, 0, 0);
        Line line1 = new Line(p1, p2);
        Line line2 = new Line(new Vector3D(1, -1, 0), new Vector3D(1, 1, 0));
        Vector3D intersection = line1.intersection(line2);
        assertNotNull(intersection);
        assertEquals(1.0, intersection.getX(), EPS);
        assertEquals(0.0, intersection.getY(), EPS);
        assertEquals(0.0, intersection.getZ(), EPS);
    }

    // Tests intersection with parallel lines
    @Test
    public void testIntersection_parallelLines_returnsNull() {
        Vector3D p1 = new Vector3D(0, 0, 0);
        Vector3D p2 = new Vector3D(1, 0, 0);
        Line line1 = new Line(p1, p2);
        Line line2 = new Line(new Vector3D(0, 1, 0), new Vector3D(1, 1, 0));
        assertNull(line1.intersection(line2));
    }

    // Tests intersection with skew lines
    @Test
    public void testIntersection_skewLines_returnsNull() {
        Vector3D p1 = new Vector3D(0, 0, 0);
        Vector3D p2 = new Vector3D(1, 0, 0);
        Line line1 = new Line(p1, p2);
        Line line2 = new Line(new Vector3D(0, 1, 0), new Vector3D(0, 1, 1));
        assertNull(line1.intersection(line2));
    }

    // Tests wholeLine returns a SubLine
    @Test
    public void testWholeLine_anyLine_returnsSubLine() {
        Vector3D p1 = new Vector3D(0, 0, 0);
        Vector3D p2 = new Vector3D(1, 0, 0);
        Line line = new Line(p1, p2);
        SubLine subLine = line.wholeLine();
        assertNotNull(subLine);
    }
}