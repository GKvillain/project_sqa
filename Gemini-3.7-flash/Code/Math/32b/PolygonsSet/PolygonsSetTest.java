package org.apache.commons.math3.geometry.euclidean.twod;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.math3.geometry.partitioning.BSPTree;
import org.apache.commons.math3.geometry.partitioning.Region;
import org.apache.commons.math3.geometry.partitioning.RegionFactory;
import org.apache.commons.math3.geometry.partitioning.SubHyperplane;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class PolygonsSetTest {

    // Tests default constructor representing full 2D space
    @Test
    public void testDefaultConstructor_fullSpace_infiniteSize() {
        PolygonsSet set = new PolygonsSet();
        assertEquals(Double.POSITIVE_INFINITY, set.getSize(), 1.0e-10);
        assertTrue(Double.isNaN(set.getBarycenter().getX()));
        assertTrue(Double.isNaN(set.getBarycenter().getY()));
        assertEquals(0, set.getVertices().length);
        assertFalse(set.isEmpty());
    }

    // Tests box constructor with finite bounds and calculates size and barycenter
    @Test
    public void testBoxConstructor_validBounds_computesSizeAndBarycenter() {
        PolygonsSet set = new PolygonsSet(1.0, 3.0, -2.0, 4.0);
        assertEquals(12.0, set.getSize(), 1.0e-10);
        assertEquals(2.0, set.getBarycenter().getX(), 1.0e-10);
        assertEquals(1.0, set.getBarycenter().getY(), 1.0e-10);
        assertEquals(Region.Location.INSIDE, set.checkPoint(new Vector2D(2.0, 1.0)));
        assertEquals(Region.Location.OUTSIDE, set.checkPoint(new Vector2D(0.0, 0.0)));
        assertEquals(Region.Location.BOUNDARY, set.checkPoint(new Vector2D(1.0, 0.0)));
    }

    // Tests getVertices for a simple box polygon
    @Test
    public void testGetVertices_boxPolygon_returnsFourVertices() {
        PolygonsSet set = new PolygonsSet(0.0, 2.0, 0.0, 3.0);
        Vector2D[][] vertices = set.getVertices();
        assertNotNull(vertices);
        assertEquals(1, vertices.length);
        assertEquals(4, vertices[0].length);
        assertNotNull(vertices[0][0]);
    }

    // Tests buildNew creates a new PolygonsSet from BSPTree
    @Test
    public void testBuildNew_fromTree_returnsNewInstance() {
        PolygonsSet set = new PolygonsSet(0.0, 1.0, 0.0, 1.0);
        BSPTree<Euclidean2D> tree = set.getTree(false);
        PolygonsSet copy = set.buildNew(tree);
        assertNotNull(copy);
        assertEquals(1.0, copy.getSize(), 1.0e-10);
    }

    // Tests an empty polygon set created via empty tree
    @Test
    public void testEmptyTree_emptyRegion_zeroSize() {
        BSPTree<Euclidean2D> tree = new BSPTree<Euclidean2D>(Boolean.FALSE);
        PolygonsSet set = new PolygonsSet(tree);
        assertEquals(0.0, set.getSize(), 1.0e-10);
        assertEquals(0.0, set.getBarycenter().getX(), 1.0e-10);
        assertEquals(0.0, set.getBarycenter().getY(), 1.0e-10);
        assertEquals(0, set.getVertices().length);
        assertTrue(set.isEmpty());
    }

    // Tests whole space polygon created via true tree
    @Test
    public void testFullTree_wholeSpace_infiniteSize() {
        BSPTree<Euclidean2D> tree = new BSPTree<Euclidean2D>(Boolean.TRUE);
        PolygonsSet set = new PolygonsSet(tree);
        assertEquals(Double.POSITIVE_INFINITY, set.getSize(), 1.0e-10);
        assertTrue(Double.isNaN(set.getBarycenter().getX()));
        assertTrue(Double.isNaN(set.getBarycenter().getY()));
    }

    // Tests half-space open polygon with infinite boundaries
    @Test
    public void testHalfSpace_openLoop_infiniteSize() {
        SubHyperplane<Euclidean2D> halfPlane = new Line(new Vector2D(0.0, 0.0), new Vector2D(1.0, 0.0)).wholeHyperplane();
        BSPTree<Euclidean2D> tree = new BSPTree<Euclidean2D>(halfPlane,
                                                            new BSPTree<Euclidean2D>(Boolean.FALSE),
                                                            new BSPTree<Euclidean2D>(Boolean.TRUE),
                                                            null);
        PolygonsSet set = new PolygonsSet(tree);
        assertEquals(Double.POSITIVE_INFINITY, set.getSize(), 1.0e-10);
        Vector2D[][] vertices = set.getVertices();
        assertEquals(1, vertices.length);
        assertNull(vertices[0][0]);
    }

    // Tests constructor with collection of sub-hyperplanes
    @Test
    public void testCollectionConstructor_boundariesList_formsPolygon() {
        Collection<SubHyperplane<Euclidean2D>> boundaries = new ArrayList<SubHyperplane<Euclidean2D>>();
        Line l1 = new Line(new Vector2D(0, 0), new Vector2D(1, 0));
        Line l2 = new Line(new Vector2D(1, 0), new Vector2D(0, 1));
        Line l3 = new Line(new Vector2D(0, 1), new Vector2D(0, 0));

        boundaries.add(new SubLine(new Vector2D(0, 0), new Vector2D(1, 0)));
        boundaries.add(new SubLine(new Vector2D(1, 0), new Vector2D(0, 1)));
        boundaries.add(new SubLine(new Vector2D(0, 1), new Vector2D(0, 0)));

        PolygonsSet set = new PolygonsSet(boundaries);
        assertEquals(0.5, set.getSize(), 1.0e-10);
        assertEquals(1.0 / 3.0, set.getBarycenter().getX(), 1.0e-10);
        assertEquals(1.0 / 3.0, set.getBarycenter().getY(), 1.0e-10);
    }

    // Tests empty collection of boundaries creating whole space
    @Test
    public void testCollectionConstructor_emptyCollection_representsWholeSpace() {
        Collection<SubHyperplane<Euclidean2D>> boundaries = new ArrayList<SubHyperplane<Euclidean2D>>();
        PolygonsSet set = new PolygonsSet(boundaries);
        assertEquals(Double.POSITIVE_INFINITY, set.getSize(), 1.0e-10);
    }

    // Tests complement of a finite box producing infinite space outside
    @Test
    public void testInvertedBox_infiniteRegion_infiniteSize() {
        RegionFactory<Euclidean2D> factory = new RegionFactory<Euclidean2D>();
        PolygonsSet box = new PolygonsSet(0.0, 1.0, 0.0, 1.0);
        PolygonsSet complement = (PolygonsSet) factory.getComplement(box);

        assertEquals(Double.POSITIVE_INFINITY, complement.getSize(), 1.0e-10);
        assertTrue(Double.isNaN(complement.getBarycenter().getX()));
        assertTrue(Double.isNaN(complement.getBarycenter().getY()));
    }

    // Tests polygon with a hole (two closed loops)
    @Test
    public void testPolygonWithHole_differenceOfBoxes_correctArea() {
        RegionFactory<Euclidean2D> factory = new RegionFactory<Euclidean2D>();
        PolygonsSet outer = new PolygonsSet(-2.0, 2.0, -2.0, 2.0);
        PolygonsSet inner = new PolygonsSet(-1.0, 1.0, -1.0, 1.0);
        PolygonsSet set = (PolygonsSet) factory.difference(outer, inner);

        assertEquals(12.0, set.getSize(), 1.0e-10);
        assertEquals(0.0, set.getBarycenter().getX(), 1.0e-10);
        assertEquals(0.0, set.getBarycenter().getY(), 1.0e-10);
        assertEquals(2, set.getVertices().length);
    }

    // Tests intersection of two infinite half-spaces forming a quadrant
    @Test
    public void testOpenLoopWithRealPoints_quadrant_infiniteSize() {
        RegionFactory<Euclidean2D> factory = new RegionFactory<Euclidean2D>();
        SubHyperplane<Euclidean2D> plane1 = new Line(new Vector2D(0, 0), new Vector2D(1, 0)).wholeHyperplane();
        SubHyperplane<Euclidean2D> plane2 = new Line(new Vector2D(0, 0), new Vector2D(0, 1)).wholeHyperplane();

        PolygonsSet set1 = new PolygonsSet(new BSPTree<Euclidean2D>(plane1,
                                                                   new BSPTree<Euclidean2D>(Boolean.FALSE),
                                                                   new BSPTree<Euclidean2D>(Boolean.TRUE),
                                                                   null));
        PolygonsSet set2 = new PolygonsSet(new BSPTree<Euclidean2D>(plane2,
                                                                   new BSPTree<Euclidean2D>(Boolean.TRUE),
                                                                   new BSPTree<Euclidean2D>(Boolean.FALSE),
                                                                   null));

        PolygonsSet intersection = (PolygonsSet) factory.intersection(set1, set2);
        assertEquals(Double.POSITIVE_INFINITY, intersection.getSize(), 1.0e-10);
        Vector2D[][] vertices = intersection.getVertices();
        assertEquals(1, vertices.length);
        assertNull(vertices[0][0]);
    }

    // Tests computeGeometricalProperties on a BSP tree with attributes (Defects4J Math-32 regression)
    @Test
    public void testComputeGeometricalProperties_bspTreeWithBoundaryAttribute_computesProperties() {
        RegionFactory<Euclidean2D> factory = new RegionFactory<Euclidean2D>();
        PolygonsSet s1 = new PolygonsSet(0.0, 1.0, 0.0, 1.0);
        PolygonsSet s2 = new PolygonsSet(0.0, 1.0, 0.0, 1.0);
        PolygonsSet diff = (PolygonsSet) factory.difference(s1, s2);

        assertEquals(0.0, diff.getSize(), 1.0e-10);
        assertTrue(diff.isEmpty());
    }
}