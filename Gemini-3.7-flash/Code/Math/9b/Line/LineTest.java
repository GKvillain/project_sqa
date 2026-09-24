package org.apache.commons.math3.geometry.euclidean.threed;

import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.geometry.euclidean.oned.Vector1D;
import org.apache.commons.math3.util.FastMath;
import org.junit.Assert;
import org.junit.Test;

public class LineTest {

    // Tests constructor with identical points throwing exception
    @Test(expected = MathIllegalArgumentException.class)
    public void testLineConstructor_identicalPoints_throwsException() {
        Vector3D p = new Vector3D(1.0, 2.0, 3.0);
        new Line(p, p);
    }

    // Tests copy constructor creates an independent and identical instance
    @Test
    public void testCopyConstructor_validLine_createsEqualInstance() {
        Line line = new Line(new Vector3D(1.0, 2.0, 3.0), new Vector3D(4.0, 6.0, 8.0));
        Line copy = new Line(line);

        Assert.assertEquals(0.0, line.getDirection().distance(copy.getDirection()), 1.0e-10);
        Assert.assertEquals(0.0, line.getOrigin().distance(copy.getOrigin()), 1.0e-10);
        Assert.assertTrue(line.isSimilarTo(copy));
    }

    // Tests reset method reinitializes direction and origin
    @Test
    public void testReset_validPoints_updatesDirectionAndOrigin() {
        Line line = new Line(new Vector3D(1.0, 0.0, 0.0), new Vector3D(2.0, 0.0, 0.0));
        line.reset(new Vector3D(0.0, 1.0, 0.0), new Vector3D(0.0, 3.0, 0.0));

        Assert.assertEquals(0.0, line.getDirection().distance(Vector3D.PLUS_J), 1.0e-10);
        Assert.assertEquals(0.0, line.getOrigin().distance(Vector3D.ZERO), 1.0e-10);
    }

    // Tests reset method with identical points throwing exception
    @Test(expected = MathIllegalArgumentException.class)
    public void testReset_identicalPoints_throwsException() {
        Line line = new Line(new Vector3D(1.0, 0.0, 0.0), new Vector3D(2.0, 0.0, 0.0));
        line.reset(new Vector3D(1.0, 1.0, 1.0), new Vector3D(1.0, 1.0, 1.0));
    }

    // Tests revert method reverses line direction while preserving the line
    @Test
    public void testRevert_validLine_reversesDirection() {
        Vector3D p1 = new Vector3D(1.0, 2.0, 3.0);
        Vector3D p2 = new Vector3D(4.0, 6.0, 8.0);
        Line line = new Line(p1, p2);
        Line reverted = line.revert();

        Assert.assertEquals(0.0, line.getDirection().add(reverted.getDirection()).getNorm(), 1.0e-10);
        Assert.assertTrue(line.isSimilarTo(reverted));
        Assert.assertTrue(reverted.contains(p1));
        Assert.assertTrue(reverted.contains(p2));
    }

    // Tests abscissa computation and point projection along the line
    @Test
    public void testGetAbscissaAndPointAt_validCoordinates_returnsExpectedPoints() {
        Line line = new Line(new Vector3D(0.0, 1.0, 0.0), new Vector3D(0.0, 4.0, 0.0));

        Assert.assertEquals(0.0, line.getAbscissa(line.getOrigin()), 1.0e-10);
        Assert.assertEquals(1.0, line.getAbscissa(new Vector3D(0.0, 1.0, 0.0)), 1.0e-10);

        Vector3D pointAt2 = line.pointAt(2.0);
        Assert.assertEquals(0.0, pointAt2.distance(new Vector3D(0.0, 2.0, 0.0)), 1.0e-10);
    }

    // Tests 1D subspace and 3D space embedding conversions
    @Test
    public void testToSubSpaceAndToSpace_validVectors_returnsConsistentConversions() {
        Line line = new Line(new Vector3D(1.0, 0.0, 0.0), new Vector3D(1.0, 0.0, 5.0));
        Vector3D p = new Vector3D(1.0, 0.0, 3.0);

        Vector1D sub = line.toSubSpace(p);
        Assert.assertEquals(3.0, sub.getX(), 1.0e-10);

        Vector3D reconstructed = line.toSpace(sub);
        Assert.assertEquals(0.0, p.distance(reconstructed), 1.0e-10);
    }

    // Tests similarity between identical, opposite, parallel, and distinct lines
    @Test
    public void testIsSimilarTo_variousLineConfigurations_returnsExpectedBoolean() {
        Line line1 = new Line(new Vector3D(0.0, 0.0, 0.0), new Vector3D(1.0, 0.0, 0.0));
        Line line2 = new Line(new Vector3D(2.0, 0.0, 0.0), new Vector3D(5.0, 0.0, 0.0));
        Line line3 = new Line(new Vector3D(5.0, 0.0, 0.0), new Vector3D(2.0, 0.0, 0.0));
        Line lineParallel = new Line(new Vector3D(0.0, 1.0, 0.0), new Vector3D(1.0, 1.0, 0.0));
        Line lineOrthogonal = new Line(new Vector3D(0.0, 0.0, 0.0), new Vector3D(0.0, 1.0, 0.0));

        Assert.assertTrue(line1.isSimilarTo(line2));
        Assert.assertTrue(line1.isSimilarTo(line3));
        Assert.assertFalse(line1.isSimilarTo(lineParallel));
        Assert.assertFalse(line1.isSimilarTo(lineOrthogonal));
    }

    // Tests contains method for points on and off the line
    @Test
    public void testContains_pointsOnAndOffLine_returnsCorrectBoolean() {
        Line line = new Line(new Vector3D(0.0, 0.0, 0.0), new Vector3D(0.0, 0.0, 2.0));

        Assert.assertTrue(line.contains(new Vector3D(0.0, 0.0, 10.0)));
        Assert.assertFalse(line.contains(new Vector3D(0.1, 0.0, 0.0)));
    }

    // Tests distance from a point to the line
    @Test
    public void testDistance_pointToLine_returnsCorrectDistance() {
        Line line = new Line(new Vector3D(0.0, 0.0, 0.0), new Vector3D(1.0, 0.0, 0.0));
        Vector3D p = new Vector3D(5.0, 3.0, 4.0);

        Assert.assertEquals(5.0, line.distance(p), 1.0e-10);
        Assert.assertEquals(0.0, line.distance(new Vector3D(10.0, 0.0, 0.0)), 1.0e-10);
    }

    // Tests distance between two lines (parallel, intersecting, and skew)
    @Test
    public void testDistance_lineToLine_returnsCorrectDistance() {
        Line line1 = new Line(new Vector3D(0.0, 0.0, 0.0), new Vector3D(1.0, 0.0, 0.0));
        Line parallelLine = new Line(new Vector3D(0.0, 2.0, 0.0), new Vector3D(1.0, 2.0, 0.0));
        Line intersectingLine = new Line(new Vector3D(0.0, 0.0, 0.0), new Vector3D(0.0, 1.0, 0.0));
        Line skewLine = new Line(new Vector3D(0.0, 0.0, 3.0), new Vector3D(0.0, 1.0, 3.0));

        Assert.assertEquals(2.0, line1.distance(parallelLine), 1.0e-10);
        Assert.assertEquals(0.0, line1.distance(intersectingLine), 1.0e-10);
        Assert.assertEquals(3.0, line1.distance(skewLine), 1.0e-10);
    }

    // Tests closestPoint computation between lines
    @Test
    public void testClosestPoint_parallelAndSkewLines_returnsExpectedPoint() {
        Line line1 = new Line(new Vector3D(0.0, 0.0, 0.0), new Vector3D(1.0, 0.0, 0.0));
        Line parallelLine = new Line(new Vector3D(0.0, 2.0, 0.0), new Vector3D(1.0, 2.0, 0.0));
        Line skewLine = new Line(new Vector3D(2.0, -1.0, 3.0), new Vector3D(2.0, 1.0, 3.0));

        Assert.assertEquals(0.0, line1.closestPoint(parallelLine).distance(line1.getOrigin()), 1.0e-10);
        Assert.assertEquals(0.0, line1.closestPoint(skewLine).distance(new Vector3D(2.0, 0.0, 0.0)), 1.0e-10);
    }

    // Tests intersection between lines
    @Test
    public void testIntersection_intersectingParallelAndSkewLines_returnsPointOrNull() {
        Line line1 = new Line(new Vector3D(0.0, 0.0, 0.0), new Vector3D(2.0, 0.0, 0.0));
        Line line2 = new Line(new Vector3D(1.0, -1.0, 0.0), new Vector3D(1.0, 1.0, 0.0));
        Line parallelLine = new Line(new Vector3D(0.0, 1.0, 0.0), new Vector3D(1.0, 1.0, 0.0));
        Line skewLine = new Line(new Vector3D(1.0, -1.0, 1.0), new Vector3D(1.0, 1.0, 1.0));

        Vector3D intersection = line1.intersection(line2);
        Assert.assertNotNull(intersection);
        Assert.assertEquals(0.0, intersection.distance(new Vector3D(1.0, 0.0, 0.0)), 1.0e-10);

        Assert.assertNull(line1.intersection(parallelLine));
        Assert.assertNull(line1.intersection(skewLine));
    }

    // Tests wholeLine returns a non-null SubLine instance
    @Test
    public void testWholeLine_validLine_returnsNonNullSubLine() {
        Line line = new Line(new Vector3D(0.0, 0.0, 0.0), new Vector3D(1.0, 1.0, 1.0));
        SubLine subLine = line.wholeLine();
        Assert.assertNotNull(subLine);
    }
}