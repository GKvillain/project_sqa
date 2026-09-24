package org.apache.commons.math3.geometry.euclidean.twod;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.oned.Interval;
import org.apache.commons.math3.geometry.euclidean.oned.IntervalsSet;
import org.apache.commons.math3.geometry.euclidean.oned.OrientedPoint;
import org.apache.commons.math3.geometry.euclidean.oned.SubOrientedPoint;
import org.apache.commons.math3.geometry.euclidean.oned.Vector1D;
import org.apache.commons.math3.geometry.partitioning.BSPTree;
import org.apache.commons.math3.geometry.partitioning.Hyperplane;
import org.apache.commons.math3.geometry.partitioning.Region;
import org.apache.commons.math3.geometry.partitioning.Side;
import org.apache.commons.math3.geometry.partitioning.SubHyperplane;
import org.junit.Test;

import static org.junit.Assert.*;

public class SubLineTest {

    // Tests simple constructor with start and end Vector2D
    @Test
    public void testConstructor_twoPoints_createsValidSubLine() {
        Vector2D start = new Vector2D(0, 0);
        Vector2D end = new Vector2D(1, 1);
        SubLine subLine = new SubLine(start, end);

        List<Segment> segments = subLine.getSegments();
        assertEquals(1, segments.size());
        assertEquals(0.0, segments.get(0).getStart().distance(start), 1.0e-10);
        assertEquals(0.0, segments.get(0).getEnd().distance(end), 1.0e-10);
    }

    // Tests constructor with Segment
    @Test
    public void testConstructor_segment_createsValidSubLine() {
        Vector2D start = new Vector2D(1, 2);
        Vector2D end = new Vector2D(3, 4);
        Line line = new Line(start, end);
        Segment segment = new Segment(start, end, line);
        SubLine subLine = new SubLine(segment);

        List<Segment> segments = subLine.getSegments();
        assertEquals(1, segments.size());
        assertEquals(0.0, segments.get(0).getStart().distance(start), 1.0e-10);
        assertEquals(0.0, segments.get(0).getEnd().distance(end), 1.0e-10);
    }

    // Tests constructor with Hyperplane and Region
    @Test
    public void testConstructor_hyperplaneAndRegion_createsValidSubLine() {
        Line line = new Line(new Vector2D(0, 0), new Vector2D(1, 0));
        IntervalsSet set = new IntervalsSet(0.0, 2.0);
        SubLine subLine = new SubLine(line, set);

        List<Segment> segments = subLine.getSegments();
        assertEquals(1, segments.size());
        assertEquals(0.0, segments.get(0).getStart().getX(), 1.0e-10);
        assertEquals(2.0, segments.get(0).getEnd().getX(), 1.0e-10);
    }

    // Tests intersection of intersecting sublines with includeEndPoints = true
    @Test
    public void testIntersection_crossingLinesIncludeEndPoints_returnsIntersectionPoint() {
        SubLine line1 = new SubLine(new Vector2D(-1, 0), new Vector2D(1, 0));
        SubLine line2 = new SubLine(new Vector2D(0, -1), new Vector2D(0, 1));

        Vector2D intersection = line1.intersection(line2, true);
        assertNotNull(intersection);
        assertEquals(0.0, intersection.getX(), 1.0e-10);
        assertEquals(0.0, intersection.getY(), 1.0e-10);
    }

    // Tests intersection of intersecting sublines with includeEndPoints = false
    @Test
    public void testIntersection_crossingLinesExcludeEndPoints_returnsIntersectionPoint() {
        SubLine line1 = new SubLine(new Vector2D(-1, 0), new Vector2D(1, 0));
        SubLine line2 = new SubLine(new Vector2D(0, -1), new Vector2D(0, 1));

        Vector2D intersection = line1.intersection(line2, false);
        assertNotNull(intersection);
        assertEquals(0.0, intersection.getX(), 1.0e-10);
        assertEquals(0.0, intersection.getY(), 1.0e-10);
    }

    // Tests intersection on endpoints with includeEndPoints = true
    @Test
    public void testIntersection_onEndpointIncludeEndPoints_returnsIntersectionPoint() {
        SubLine line1 = new SubLine(new Vector2D(0, 0), new Vector2D(1, 0));
        SubLine line2 = new SubLine(new Vector2D(0, 0), new Vector2D(0, 1));

        Vector2D intersection = line1.intersection(line2, true);
        assertNotNull(intersection);
        assertEquals(0.0, intersection.getX(), 1.0e-10);
        assertEquals(0.0, intersection.getY(), 1.0e-10);
    }

    // Tests intersection on endpoints with includeEndPoints = false
    @Test
    public void testIntersection_onEndpointExcludeEndPoints_returnsNull() {
        SubLine line1 = new SubLine(new Vector2D(0, 0), new Vector2D(1, 0));
        SubLine line2 = new SubLine(new Vector2D(0, 0), new Vector2D(0, 1));

        Vector2D intersection = line1.intersection(line2, false);
        assertNull(intersection);
    }

    // Tests intersection when segments do not cross (outside subline range)
    @Test
    public void testIntersection_disjointSegments_returnsNull() {
        SubLine line1 = new SubLine(new Vector2D(0, 0), new Vector2D(1, 0));
        SubLine line2 = new SubLine(new Vector2D(2, -1), new Vector2D(2, 1));

        Vector2D intersection = line1.intersection(line2, true);
        assertNull(intersection);
    }

    // Tests intersection of parallel lines
    @Test
    public void testIntersection_parallelLines_returnsNull() {
        SubLine line1 = new SubLine(new Vector2D(0, 0), new Vector2D(1, 0));
        SubLine line2 = new SubLine(new Vector2D(0, 1), new Vector2D(1, 1));

        Vector2D intersection = line1.intersection(line2, true);
        assertNull(intersection);
    }

    // Tests side when hyperplane intersects the subline
    @Test
    public void testSide_intersectingHyperplane_returnsBoth() {
        SubLine subLine = new SubLine(new Vector2D(-1, 0), new Vector2D(1, 0));
        Line hyperplane = new Line(new Vector2D(0, -1), new Vector2D(0, 1));

        Side side = subLine.side(hyperplane);
        assertEquals(Side.BOTH, side);
    }

    // Tests side when hyperplane does not cross segment range
    @Test
    public void testSide_lineIntersectingUnderlyingLineOnly_returnsPlusOrMinus() {
        SubLine subLine = new SubLine(new Vector2D(1, 0), new Vector2D(2, 0));
        Line hyperplane = new Line(new Vector2D(0, -1), new Vector2D(0, 1));

        Side side = subLine.side(hyperplane);
        assertTrue(side == Side.PLUS || side == Side.MINUS);
    }

    // Tests side when hyperplane is parallel and offset is positive
    @Test
    public void testSide_parallelHyperplanePositiveOffset_returnsPlus() {
        SubLine subLine = new SubLine(new Vector2D(0, 0), new Vector2D(1, 0));
        Line hyperplane = new Line(new Vector2D(0, 1), new Vector2D(1, 1));

        Side side = subLine.side(hyperplane);
        assertEquals(Side.PLUS, side);
    }

    // Tests side when hyperplane is parallel and offset is negative
    @Test
    public void testSide_parallelHyperplaneNegativeOffset_returnsMinus() {
        SubLine subLine = new SubLine(new Vector2D(0, 1), new Vector2D(1, 1));
        Line hyperplane = new Line(new Vector2D(0, 0), new Vector2D(1, 0));

        Side side = subLine.side(hyperplane);
        assertEquals(Side.MINUS, side);
    }

    // Tests split when hyperplane cuts the subline
    @Test
    public void testSplit_intersectingHyperplane_splitsBoth() {
        SubLine subLine = new SubLine(new Vector2D(-1, 0), new Vector2D(1, 0));
        Line hyperplane = new Line(new Vector2D(0, -1), new Vector2D(0, 1));

        SubHyperplane.SplitSubHyperplane<Euclidean2D> split = subLine.split(hyperplane);
        assertNotNull(split.getPlus());
        assertNotNull(split.getMinus());

        List<Segment> plusSegments = ((SubLine) split.getPlus()).getSegments();
        List<Segment> minusSegments = ((SubLine) split.getMinus()).getSegments();
        assertEquals(1, plusSegments.size());
        assertEquals(1, minusSegments.size());
    }

    // Tests split when hyperplane is parallel with positive offset
    @Test
    public void testSplit_parallelHyperplanePositiveOffset_returnsOnlyPlus() {
        SubLine subLine = new SubLine(new Vector2D(0, 0), new Vector2D(1, 0));
        Line hyperplane = new Line(new Vector2D(0, 1), new Vector2D(1, 1));

        SubHyperplane.SplitSubHyperplane<Euclidean2D> split = subLine.split(hyperplane);
        assertNotNull(split.getPlus());
        assertNull(split.getMinus());
    }

    // Tests split when hyperplane is parallel with negative offset
    @Test
    public void testSplit_parallelHyperplaneNegativeOffset_returnsOnlyMinus() {
        SubLine subLine = new SubLine(new Vector2D(0, 1), new Vector2D(1, 1));
        Line hyperplane = new Line(new Vector2D(0, 0), new Vector2D(1, 0));

        SubHyperplane.SplitSubHyperplane<Euclidean2D> split = subLine.split(hyperplane);
        assertNull(split.getPlus());
        assertNotNull(split.getMinus());
    }

    // Tests buildNew creates a correct SubLine instance
    @Test
    public void testBuildNew_validHyperplaneAndRegion_returnsSubLineInstance() {
        Line line = new Line(new Vector2D(0, 0), new Vector2D(1, 0));
        IntervalsSet set = new IntervalsSet(0.0, 1.0);
        SubLine original = new SubLine(line, set);

        Line newLine = new Line(new Vector2D(0, 0), new Vector2D(0, 1));
        IntervalsSet newSet = new IntervalsSet(-1.0, 1.0);
        SubHyperplane<Euclidean2D> built = original.buildNew(newLine, newSet);

        assertTrue(built instanceof SubLine);
        List<Segment> segments = ((SubLine) built).getSegments();
        assertEquals(1, segments.size());
        assertEquals(-1.0, segments.get(0).getStart().getY(), 1.0e-10);
        assertEquals(1.0, segments.get(0).getEnd().getY(), 1.0e-10);
    }

    // Tests getSegments for unbounded half-line
    @Test
    public void testGetSegments_halfInfiniteSubLine_containsInfiniteCoordinates() {
        Line line = new Line(new Vector2D(0, 0), new Vector2D(1, 0));
        IntervalsSet set = new IntervalsSet(0.0, Double.POSITIVE_INFINITY);
        SubLine subLine = new SubLine(line, set);

        List<Segment> segments = subLine.getSegments();
        assertEquals(1, segments.size());
        assertEquals(0.0, segments.get(0).getStart().getX(), 1.0e-10);
        assertTrue(Double.isInfinite(segments.get(0).getEnd().getX()));
    }

    // Tests getSegments for empty subline
    @Test
    public void testGetSegments_emptyRegion_returnsEmptyList() {
        Line line = new Line(new Vector2D(0, 0), new Vector2D(1, 0));
        IntervalsSet set = new IntervalsSet(1.0, 0.0);
        SubLine subLine = new SubLine(line, set);

        List<Segment> segments = subLine.getSegments();
        assertTrue(segments.isEmpty());
    }

    // Tests getSegments for fully infinite line (-infinity to +infinity)
    @Test
    public void testGetSegments_fullyInfiniteLine_containsInfiniteEndpoints() {
        Line line = new Line(new Vector2D(0, 0), new Vector2D(1, 0));
        IntervalsSet set = new IntervalsSet(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        SubLine subLine = new SubLine(line, set);

        List<Segment> segments = subLine.getSegments();
        assertEquals(1, segments.size());
        assertTrue(Double.isInfinite(segments.get(0).getStart().getX()));
        assertTrue(Double.isInfinite(segments.get(0).getEnd().getX()));
    }

    // Tests getSegments for half-line starting from -infinity to a finite point
    @Test
    public void testGetSegments_negativeHalfInfiniteSubLine_containsInfiniteStart() {
        Line line = new Line(new Vector2D(0, 0), new Vector2D(1, 0));
        IntervalsSet set = new IntervalsSet(Double.NEGATIVE_INFINITY, 0.0);
        SubLine subLine = new SubLine(line, set);

        List<Segment> segments = subLine.getSegments();
        assertEquals(1, segments.size());
        assertTrue(Double.isInfinite(segments.get(0).getStart().getX()));
        assertEquals(0.0, segments.get(0).getEnd().getX(), 1.0e-10);
    }

    // Tests split when intersecting line splits only into plus or minus side
    @Test
    public void testSplit_intersectingLineOutsideSegment_returnsSingleSide() {
        SubLine subLine = new SubLine(new Vector2D(1, 0), new Vector2D(2, 0));
        Line hyperplane = new Line(new Vector2D(0, -1), new Vector2D(0, 1));

        SubHyperplane.SplitSubHyperplane<Euclidean2D> split = subLine.split(hyperplane);
        assertTrue((split.getPlus() != null && split.getMinus() == null) ||
                   (split.getPlus() == null && split.getMinus() != null));
    }
}