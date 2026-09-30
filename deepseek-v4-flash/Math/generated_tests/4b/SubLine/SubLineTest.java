package org.apache.commons.math3.geometry.euclidean.twod;

import java.util.List;

import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.commons.math3.geometry.euclidean.oned.Euclidean1D;
import org.apache.commons.math3.geometry.euclidean.oned.Interval;
import org.apache.commons.math3.geometry.euclidean.oned.IntervalsSet;
import org.apache.commons.math3.geometry.euclidean.oned.OrientedPoint;
import org.apache.commons.math3.geometry.euclidean.oned.Vector1D;
import org.apache.commons.math3.geometry.euclidean.twod.Euclidean2D;
import org.apache.commons.math3.geometry.euclidean.twod.Line;
import org.apache.commons.math3.geometry.euclidean.twod.Segment;
import org.apache.commons.math3.geometry.euclidean.twod.SubLine;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.geometry.partitioning.AbstractSubHyperplane;
import org.apache.commons.math3.geometry.partitioning.BSPTree;
import org.apache.commons.math3.geometry.partitioning.Hyperplane;
import org.apache.commons.math3.geometry.partitioning.Region;
import org.apache.commons.math3.geometry.partitioning.Region.Location;
import org.apache.commons.math3.geometry.partitioning.Side;
import org.apache.commons.math3.geometry.partitioning.SubHyperplane;

public class SubLineTest {

    // Tests constructor with two points
    @Test
    public void testConstructor_startEnd_createsOneSegment() {
        Vector2D start = new Vector2D(1, 2);
        Vector2D end = new Vector2D(5, 6);
        SubLine sub = new SubLine(start, end);
        List<Segment> segments = sub.getSegments();
        assertEquals(1, segments.size());
        Segment seg = segments.get(0);
        assertEquals(start, seg.getStart());
        assertEquals(end, seg.getEnd());
    }

    // Tests constructor with Segment
    @Test
    public void testConstructor_segment_createsOneSegment() {
        Vector2D start = new Vector2D(0, 0);
        Vector2D end = new Vector2D(3, 4);
        Line line = new Line(start, end);
        Segment seg = new Segment(start, end, line);
        SubLine sub = new SubLine(seg);
        List<Segment> segments = sub.getSegments();
        assertEquals(1, segments.size());
        assertEquals(start, segments.get(0).getStart());
        assertEquals(end, segments.get(0).getEnd());
    }

    // Tests getSegments returns correct segments
    @Test
    public void testGetSegments_returnsCorrectSegments() {
        Vector2D start = new Vector2D(1, 1);
        Vector2D end = new Vector2D(4, 5);
        SubLine sub = new SubLine(start, end);
        List<Segment> segments = sub.getSegments();
        assertEquals(1, segments.size());
        Segment seg = segments.get(0);
        assertEquals(start, seg.getStart());
        assertEquals(end, seg.getEnd());
        assertNotNull(seg.getLine());
    }

    // Tests intersection with parallel lines (bug: currently throws NPE)
    @Test
    public void testIntersection_parallelLines_returnsNull() {
        SubLine sub1 = new SubLine(new Vector2D(0, 0), new Vector2D(10, 0));
        SubLine sub2 = new SubLine(new Vector2D(0, 2), new Vector2D(10, 2));
        Vector2D result = sub1.intersection(sub2, false);
        assertNull("Intersection of parallel lines should be null", result);
    }

    // Tests intersection of two lines that cross inside both ranges
    @Test
    public void testIntersection_intersectingLinesInsideRange_returnsPoint() {
        SubLine horizontal = new SubLine(new Vector2D(0, 0), new Vector2D(10, 0));
        SubLine vertical = new SubLine(new Vector2D(5, -5), new Vector2D(5, 5));
        Vector2D result = horizontal.intersection(vertical, false);
        assertNotNull(result);
        assertEquals(5.0, result.getX(), 1e-10);
        assertEquals(0.0, result.getY(), 1e-10);
    }

    // Tests intersection of two lines that cross outside one range
    @Test
    public void testIntersection_intersectingLinesOutsideRange_returnsNull() {
        SubLine horizontal = new SubLine(new Vector2D(0, 0), new Vector2D(10, 0));
        SubLine vertical = new SubLine(new Vector2D(15, -5), new Vector2D(15, 5));
        Vector2D result = horizontal.intersection(vertical, false);
        assertNull("Intersection point outside horizontal subline", result);
    }

    // Tests intersection with includeEndPoints true when point is endpoint
    @Test
    public void testIntersection_includeEndPointsTrue_endpointHit_returnsPoint() {
        SubLine horizontal = new SubLine(new Vector2D(0, 0), new Vector2D(10, 0));
        SubLine vertical = new SubLine(new Vector2D(10, 0), new Vector2D(10, 5));
        Vector2D result = horizontal.intersection(vertical, true);
        assertNotNull(result);
        assertEquals(10.0, result.getX(), 1e-10);
        assertEquals(0.0, result.getY(), 1e-10);
    }

    // Tests intersection with includeEndPoints false when point is endpoint
    @Test
    public void testIntersection_includeEndPointsFalse_endpointHit_returnsNull() {
        SubLine horizontal = new SubLine(new Vector2D(0, 0), new Vector2D(10, 0));
        SubLine vertical = new SubLine(new Vector2D(10, 0), new Vector2D(10, 5));
        Vector2D result = horizontal.intersection(vertical, false);
        assertNull("Intersection at endpoint should be null when includeEndPoints=false", result);
    }

    // Tests side with parallel line above (this line is below the other line)
    @Test
    public void testSide_parallelAbove_returnsMinus() {
        SubLine sub = new SubLine(new Vector2D(0, 0), new Vector2D(10, 0));
        Line other = new Line(new Vector2D(0, 1), new Vector2D(10, 1));
        Side result = sub.side(other);
        assertEquals(Side.MINUS, result);
    }

    // Tests side with parallel line below (this line is above the other line)
    @Test
    public void testSide_parallelBelow_returnsPlus() {
        SubLine sub = new SubLine(new Vector2D(0, 0), new Vector2D(10, 0));
        Line other = new Line(new Vector2D(0, -1), new Vector2D(10, -1));
        Side result = sub.side(other);
        assertEquals(Side.PLUS, result);
    }

    // Tests side with same line (parallel and coincident)
    @Test
    public void testSide_parallelSameLine_returnsHyper() {
        SubLine sub = new SubLine(new Vector2D(0, 0), new Vector2D(10, 0));
        Line same = new Line(new Vector2D(0, 0), new Vector2D(10, 0));
        Side result = sub.side(same);
        assertEquals(Side.HYPER, result);
    }

    // Tests buildNew method returns SubLine instance
    @Test
    public void testBuildNew_returnsSubLineInstance() {
        SubLine sub = new SubLine(new Vector2D(0, 0), new Vector2D(1, 1));
        Hyperplane<Euclidean2D> hyper = sub.getHyperplane();
        Region<Euclidean1D> region = sub.getRemainingRegion();
        AbstractSubHyperplane<Euclidean2D, Euclidean1D> newSub = sub.buildNew(hyper, region);
        assertNotNull(newSub);
        assertTrue(newSub instanceof SubLine);
    }

    // ========== New tests for uncovered areas ==========

    // Tests side with a non-parallel line lying entirely above the subline
    @Test
    public void testSide_nonParallelLineAbove_returnsPlus() {
        SubLine sub = new SubLine(new Vector2D(0, 0), new Vector2D(10, 0));
        // Line through (0,1) and (10,2) has positive slope, all points are above y=0
        Line other = new Line(new Vector2D(0, 1), new Vector2D(10, 2));
        Side result = sub.side(other);
        assertEquals(Side.PLUS, result);
    }

    // Tests side with a non-parallel line lying entirely below the subline
    @Test
    public void testSide_nonParallelLineBelow_returnsMinus() {
        SubLine sub = new SubLine(new Vector2D(0, 0), new Vector2D(10, 0));
        // Line through (0,-1) and (10,-2) is entirely below y=0
        Line other = new Line(new Vector2D(0, -1), new Vector2D(10, -2));
        Side result = sub.side(other);
        assertEquals(Side.MINUS, result);
    }

    // Tests side with a non-parallel line that crosses through the subline
    @Test
    public void testSide_nonParallelLineIntersecting_returnsBoth() {
        SubLine sub = new SubLine(new Vector2D(0, 0), new Vector2D(10, 0));
        // Vertical line at x=5 crosses the subline at (5,0)
        Line other = new Line(new Vector2D(5, -5), new Vector2D(5, 5));
        Side result = sub.side(other);
        assertEquals(Side.BOTH, result);
    }

    // Tests intersection of coincident (collinear) lines with no overlap
    @Test
    public void testIntersection_coincidentLinesNoOverlap_returnsNull() {
        SubLine sub1 = new SubLine(new Vector2D(0, 0), new Vector2D(5, 0));
        SubLine sub2 = new SubLine(new Vector2D(6, 0), new Vector2D(10, 0));
        Vector2D result = sub1.intersection(sub2, false);
        assertNull("Coincident non-overlapping lines should give null", result);
    }

    // Tests intersection of coincident lines with partial overlap (still no unique point)
    @Test
    public void testIntersection_coincidentLinesOverlap_returnsNull() {
        SubLine sub1 = new SubLine(new Vector2D(0, 0), new Vector2D(10, 0));
        SubLine sub2 = new SubLine(new Vector2D(5, 0), new Vector2D(15, 0));
        Vector2D result = sub1.intersection(sub2, false);
        assertNull("Coincident overlapping lines should give null (no unique point)", result);
    }

    // Tests intersection where the point lies at the endpoint of only one subline
    // (includeEndPoints = true)
    @Test
    public void testIntersection_includeEndPointsTrue_pointOnEndpointOfOneSubline_returnsPoint() {
        SubLine horizontal = new SubLine(new Vector2D(0, 0), new Vector2D(10, 0));
        // Vertical line spanning from (10,-5) to (10,5): (10,0) is endpoint of horizontal only
        SubLine vertical = new SubLine(new Vector2D(10, -5), new Vector2D(10, 5));
        Vector2D result = horizontal.intersection(vertical, true);
        assertNotNull(result);
        assertEquals(10.0, result.getX(), 1e-10);
        assertEquals(0.0, result.getY(), 1e-10);
    }

    // Tests intersection where the point lies at the endpoint of only one subline
    // (includeEndPoints = false)
    @Test
    public void testIntersection_includeEndPointsFalse_pointOnEndpointOfOneSubline_returnsNull() {
        SubLine horizontal = new SubLine(new Vector2D(0, 0), new Vector2D(10, 0));
        SubLine vertical = new SubLine(new Vector2D(10, -5), new Vector2D(10, 5));
        Vector2D result = horizontal.intersection(vertical, false);
        assertNull("Endpoint of horizontal not included when includeEndPoints=false", result);
    }

    // Tests that buildNew produces a SubLine with identical segments
    @Test
    public void testBuildNew_consistentSegments() {
        SubLine original = new SubLine(new Vector2D(2, 3), new Vector2D(7, 8));
        Hyperplane<Euclidean2D> hyper = original.getHyperplane();
        Region<Euclidean1D> region = original.getRemainingRegion();
        SubLine rebuilt = (SubLine) original.buildNew(hyper, region);
        List<Segment> originalSegments = original.getSegments();
        List<Segment> rebuiltSegments = rebuilt.getSegments();
        assertEquals(originalSegments.size(), rebuiltSegments.size());
        for (int i = 0; i < originalSegments.size(); i++) {
            Segment s1 = originalSegments.get(i);
            Segment s2 = rebuiltSegments.get(i);
            assertEquals(s1.getStart(), s2.getStart());
            assertEquals(s1.getEnd(), s2.getEnd());
            // Line objects may differ but should represent the same line
            assertTrue(s1.getLine().contains(s2.getStart()));
        }
    }
}