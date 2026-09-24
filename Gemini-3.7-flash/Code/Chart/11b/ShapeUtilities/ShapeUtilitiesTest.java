package org.jfree.chart.util;

import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link ShapeUtilities}.
 */
public class ShapeUtilitiesTest {

    // Tests clone with null and cloneable shapes
    @Test
    public void testClone_variousInputs_clonesCorrectly() {
        assertNull(ShapeUtilities.clone(null));

        Rectangle2D rect = new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0);
        Shape clonedRect = ShapeUtilities.clone(rect);
        assertNotNull(clonedRect);
        assertEquals(rect, clonedRect);

        GeneralPath path = new GeneralPath();
        path.moveTo(1.0f, 2.0f);
        path.lineTo(3.0f, 4.0f);
        Shape clonedPath = ShapeUtilities.clone(path);
        assertNotNull(clonedPath);
        assertTrue(ShapeUtilities.equal(path, (GeneralPath) clonedPath));
    }

    // Tests equal with general Shape interface
    @Test
    public void testEqualShape_variousShapeTypes_returnsExpected() {
        assertTrue(ShapeUtilities.equal((Shape) null, (Shape) null));
        assertFalse(ShapeUtilities.equal(new Line2D.Double(0, 0, 1, 1), null));
        assertFalse(ShapeUtilities.equal(null, new Line2D.Double(0, 0, 1, 1)));

        Shape s1 = new Line2D.Double(0.0, 0.0, 1.0, 1.0);
        Shape s2 = new Line2D.Double(0.0, 0.0, 1.0, 1.0);
        Shape s3 = new Line2D.Double(0.0, 0.0, 2.0, 2.0);
        assertTrue(ShapeUtilities.equal(s1, s2));
        assertFalse(ShapeUtilities.equal(s1, s3));

        Shape e1 = new Ellipse2D.Double(1, 2, 3, 4);
        Shape e2 = new Ellipse2D.Double(1, 2, 3, 4);
        assertTrue(ShapeUtilities.equal(e1, e2));
        assertFalse(ShapeUtilities.equal(s1, e1));

        Shape r1 = new Rectangle2D.Double(1, 2, 3, 4);
        Shape r2 = new Rectangle2D.Double(1, 2, 3, 4);
        Shape r3 = new Rectangle2D.Double(1, 2, 3, 5);
        assertTrue(ShapeUtilities.equal(r1, r2));
        assertFalse(ShapeUtilities.equal(r1, r3));
    }

    // Tests Line2D equality
    @Test
    public void testEqualLine2D_nullAndDifferentValues_returnsExpected() {
        assertTrue(ShapeUtilities.equal((Line2D) null, (Line2D) null));
        assertFalse(ShapeUtilities.equal(new Line2D.Double(1, 2, 3, 4), null));
        assertFalse(ShapeUtilities.equal(null, new Line2D.Double(1, 2, 3, 4)));

        Line2D l1 = new Line2D.Double(1.0, 2.0, 3.0, 4.0);
        Line2D l2 = new Line2D.Double(1.0, 2.0, 3.0, 4.0);
        Line2D l3 = new Line2D.Double(1.1, 2.0, 3.0, 4.0);
        Line2D l4 = new Line2D.Double(1.0, 2.0, 3.1, 4.0);

        assertTrue(ShapeUtilities.equal(l1, l2));
        assertFalse(ShapeUtilities.equal(l1, l3));
        assertFalse(ShapeUtilities.equal(l1, l4));
    }

    // Tests Ellipse2D equality
    @Test
    public void testEqualEllipse2D_nullAndDifferentValues_returnsExpected() {
        assertTrue(ShapeUtilities.equal((Ellipse2D) null, (Ellipse2D) null));
        assertFalse(ShapeUtilities.equal(new Ellipse2D.Double(1, 2, 3, 4), null));
        assertFalse(ShapeUtilities.equal(null, new Ellipse2D.Double(1, 2, 3, 4)));

        Ellipse2D e1 = new Ellipse2D.Double(1.0, 2.0, 3.0, 4.0);
        Ellipse2D e2 = new Ellipse2D.Double(1.0, 2.0, 3.0, 4.0);
        Ellipse2D e3 = new Ellipse2D.Double(1.1, 2.0, 3.0, 4.0);

        assertTrue(ShapeUtilities.equal(e1, e2));
        assertFalse(ShapeUtilities.equal(e1, e3));
    }

    // Tests Arc2D equality
    @Test
    public void testEqualArc2D_allBranches_returnsExpected() {
        assertTrue(ShapeUtilities.equal((Arc2D) null, (Arc2D) null));
        assertFalse(ShapeUtilities.equal(new Arc2D.Double(1, 2, 3, 4, 0, 90, Arc2D.OPEN), null));
        assertFalse(ShapeUtilities.equal(null, new Arc2D.Double(1, 2, 3, 4, 0, 90, Arc2D.OPEN)));

        Arc2D a1 = new Arc2D.Double(1, 2, 3, 4, 0, 90, Arc2D.OPEN);
        Arc2D a2 = new Arc2D.Double(1, 2, 3, 4, 0, 90, Arc2D.OPEN);
        assertTrue(ShapeUtilities.equal(a1, a2));

        Arc2D aFrame = new Arc2D.Double(2, 2, 3, 4, 0, 90, Arc2D.OPEN);
        assertFalse(ShapeUtilities.equal(a1, aFrame));

        Arc2D aStart = new Arc2D.Double(1, 2, 3, 4, 10, 90, Arc2D.OPEN);
        assertFalse(ShapeUtilities.equal(a1, aStart));

        Arc2D aExtent = new Arc2D.Double(1, 2, 3, 4, 0, 180, Arc2D.OPEN);
        assertFalse(ShapeUtilities.equal(a1, aExtent));

        Arc2D aType = new Arc2D.Double(1, 2, 3, 4, 0, 90, Arc2D.PIE);
        assertFalse(ShapeUtilities.equal(a1, aType));
    }

    // Tests Polygon equality
    @Test
    public void testEqualPolygon_allBranches_returnsExpected() {
        assertTrue(ShapeUtilities.equal((Polygon) null, (Polygon) null));
        assertFalse(ShapeUtilities.equal(new Polygon(new int[] {0}, new int[] {0}, 1), null));
        assertFalse(ShapeUtilities.equal(null, new Polygon(new int[] {0}, new int[] {0}, 1)));

        Polygon p1 = new Polygon(new int[] {0, 1, 2}, new int[] {0, 2, 4}, 3);
        Polygon p2 = new Polygon(new int[] {0, 1, 2}, new int[] {0, 2, 4}, 3);
        assertTrue(ShapeUtilities.equal(p1, p2));

        Polygon pDiffPoints = new Polygon(new int[] {0, 1}, new int[] {0, 2}, 2);
        assertFalse(ShapeUtilities.equal(p1, pDiffPoints));

        Polygon pDiffX = new Polygon(new int[] {0, 2, 2}, new int[] {0, 2, 4}, 3);
        assertFalse(ShapeUtilities.equal(p1, pDiffX));

        Polygon pDiffY = new Polygon(new int[] {0, 1, 2}, new int[] {0, 3, 4}, 3);
        assertFalse(ShapeUtilities.equal(p1, pDiffY));
    }

    // Tests GeneralPath equality
    @Test
    public void testEqualGeneralPath_allBranches_returnsExpected() {
        assertTrue(ShapeUtilities.equal((GeneralPath) null, (GeneralPath) null));
        GeneralPath g1 = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        g1.moveTo(0.0f, 0.0f);
        g1.lineTo(1.0f, 1.0f);

        assertFalse(ShapeUtilities.equal(g1, null));
        assertFalse(ShapeUtilities.equal(null, g1));

        GeneralPath g2 = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        g2.moveTo(0.0f, 0.0f);
        g2.lineTo(1.0f, 1.0f);
        assertTrue(ShapeUtilities.equal(g1, g2));

        GeneralPath gDiffWinding = new GeneralPath(GeneralPath.WIND_NON_ZERO);
        gDiffWinding.moveTo(0.0f, 0.0f);
        gDiffWinding.lineTo(1.0f, 1.0f);
        assertFalse(ShapeUtilities.equal(g1, gDiffWinding));

        GeneralPath gDiffCoords = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        gDiffCoords.moveTo(0.0f, 0.0f);
        gDiffCoords.lineTo(2.0f, 1.0f);
        assertFalse(ShapeUtilities.equal(g1, gDiffCoords));

        GeneralPath gDiffSegments = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        gDiffSegments.moveTo(0.0f, 0.0f);
        gDiffSegments.lineTo(1.0f, 1.0f);
        gDiffSegments.closePath();
        assertFalse(ShapeUtilities.equal(g1, gDiffSegments));
    }

    // Tests createTranslatedShape with translation offsets
    @Test
    public void testCreateTranslatedShape_offsets_translatesCorrectly() {
        Shape rect = new Rectangle2D.Double(0.0, 0.0, 10.0, 10.0);
        Shape translated = ShapeUtilities.createTranslatedShape(rect, 5.0, -2.0);
        Rectangle2D bounds = translated.getBounds2D();
        assertEquals(5.0, bounds.getX(), 0.001);
        assertEquals(-2.0, bounds.getY(), 0.001);
        assertEquals(10.0, bounds.getWidth(), 0.001);
        assertEquals(10.0, bounds.getHeight(), 0.001);
    }

    // Tests createTranslatedShape with null shape throwing exception
    @Test(expected = IllegalArgumentException.class)
    public void testCreateTranslatedShape_nullShape_throwsException() {
        ShapeUtilities.createTranslatedShape(null, 1.0, 1.0);
    }

    // Tests createTranslatedShape with RectangleAnchor
    @Test
    public void testCreateTranslatedShape_withAnchor_translatesCorrectly() {
        Shape rect = new Rectangle2D.Double(0.0, 0.0, 10.0, 20.0);
        Shape translated = ShapeUtilities.createTranslatedShape(rect, RectangleAnchor.CENTER, 50.0, 50.0);
        Rectangle2D bounds = translated.getBounds2D();
        assertEquals(45.0, bounds.getX(), 0.001);
        assertEquals(40.0, bounds.getY(), 0.001);
    }

    // Tests createTranslatedShape with null anchor throwing exception
    @Test(expected = IllegalArgumentException.class)
    public void testCreateTranslatedShape_nullAnchor_throwsException() {
        Shape rect = new Rectangle2D.Double(0.0, 0.0, 10.0, 10.0);
        ShapeUtilities.createTranslatedShape(rect, null, 1.0, 1.0);
    }

    // Tests rotateShape with valid and null input
    @Test
    public void testRotateShape_validAndNull_rotatesOrReturnsNull() {
        assertNull(ShapeUtilities.rotateShape(null, Math.PI, 0.0f, 0.0f));

        Shape rect = new Rectangle2D.Double(0.0, 0.0, 10.0, 10.0);
        Shape rotated = ShapeUtilities.rotateShape(rect, Math.PI / 2.0, 0.0f, 0.0f);
        assertNotNull(rotated);
        Rectangle2D bounds = rotated.getBounds2D();
        assertEquals(-10.0, bounds.getX(), 0.001);
        assertEquals(0.0, bounds.getY(), 0.001);
    }

    // Tests drawRotatedShape execution
    @Test
    public void testDrawRotatedShape_validGraphics_executesWithoutError() {
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        Shape rect = new Rectangle2D.Double(10, 10, 20, 20);
        ShapeUtilities.drawRotatedShape(g2, rect, Math.PI / 4, 20, 20);
        g2.dispose();
    }

    // Tests shape creation helper methods
    @Test
    public void testShapeCreationMethods_validDimensions_createsExpectedShapes() {
        Shape cross = ShapeUtilities.createDiagonalCross(5.0f, 1.0f);
        assertNotNull(cross);

        Shape regularCross = ShapeUtilities.createRegularCross(5.0f, 1.0f);
        assertNotNull(regularCross);

        Shape diamond = ShapeUtilities.createDiamond(5.0f);
        assertNotNull(diamond);

        Shape upTriangle = ShapeUtilities.createUpTriangle(5.0f);
        assertNotNull(upTriangle);

        Shape downTriangle = ShapeUtilities.createDownTriangle(5.0f);
        assertNotNull(downTriangle);
    }

    // Tests createLineRegion for non-vertical and vertical lines
    @Test
    public void testCreateLineRegion_horizontalAndVertical_createsRegion() {
        Line2D nonVertical = new Line2D.Double(0.0, 0.0, 10.0, 10.0);
        Shape region1 = ShapeUtilities.createLineRegion(nonVertical, 2.0f);
        assertNotNull(region1);
        assertTrue(region1.getBounds2D().getWidth() > 0);

        Line2D vertical = new Line2D.Double(5.0, 0.0, 5.0, 10.0);
        Shape region2 = ShapeUtilities.createLineRegion(vertical, 2.0f);
        assertNotNull(region2);
        assertTrue(region2.getBounds2D().getWidth() > 0);
    }

    // Tests getPointInRectangle constraint
    @Test
    public void testGetPointInRectangle_insideAndOutside_constrainsPoint() {
        Rectangle2D area = new Rectangle2D.Double(10.0, 20.0, 100.0, 50.0);

        Point2D inside = ShapeUtilities.getPointInRectangle(30.0, 40.0, area);
        assertEquals(30.0, inside.getX(), 0.001);
        assertEquals(40.0, inside.getY(), 0.001);

        Point2D outside = ShapeUtilities.getPointInRectangle(0.0, 100.0, area);
        assertEquals(10.0, outside.getX(), 0.001);
        assertEquals(70.0, outside.getY(), 0.001);
    }

    // Tests contains method
    @Test
    public void testContains_variousRectangles_returnsCorrectResult() {
        Rectangle2D r1 = new Rectangle2D.Double(0.0, 0.0, 10.0, 10.0);
        Rectangle2D r2 = new Rectangle2D.Double(2.0, 2.0, 5.0, 5.0);
        Rectangle2D r3 = new Rectangle2D.Double(5.0, 5.0, 10.0, 10.0);

        assertTrue(ShapeUtilities.contains(r1, r2));
        assertFalse(ShapeUtilities.contains(r1, r3));
    }

    // Tests intersects method
    @Test
    public void testIntersects_overlappingAndDisjoint_returnsCorrectResult() {
        Rectangle2D r1 = new Rectangle2D.Double(0.0, 0.0, 10.0, 10.0);
        Rectangle2D r2 = new Rectangle2D.Double(5.0, 5.0, 10.0, 10.0);
        Rectangle2D r3 = new Rectangle2D.Double(20.0, 20.0, 5.0, 5.0);

        assertTrue(ShapeUtilities.intersects(r1, r2));
        assertFalse(ShapeUtilities.intersects(r1, r3));
    }

    // Tests equal(Shape, Shape) routing for Arc2D, Polygon, GeneralPath, and unknown/mismatched Shape types
    @Test
    public void testEqualShape_arcPolygonGeneralPathAndUnsupported_returnsExpected() {
        Shape a1 = new Arc2D.Double(1, 2, 3, 4, 0, 90, Arc2D.OPEN);
        Shape a2 = new Arc2D.Double(1, 2, 3, 4, 0, 90, Arc2D.OPEN);
        Shape a3 = new Arc2D.Double(1, 2, 3, 4, 0, 180, Arc2D.OPEN);
        assertTrue(ShapeUtilities.equal(a1, a2));
        assertFalse(ShapeUtilities.equal(a1, a3));

        Shape p1 = new Polygon(new int[] {0, 1, 2}, new int[] {0, 2, 4}, 3);
        Shape p2 = new Polygon(new int[] {0, 1, 2}, new int[] {0, 2, 4}, 3);
        Shape p3 = new Polygon(new int[] {0, 1}, new int[] {0, 2}, 2);
        assertTrue(ShapeUtilities.equal(p1, p2));
        assertFalse(ShapeUtilities.equal(p1, p3));

        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0.0f, 0.0f);
        gp1.lineTo(1.0f, 1.0f);
        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0.0f, 0.0f);
        gp2.lineTo(1.0f, 1.0f);
        GeneralPath gp3 = new GeneralPath();
        gp3.moveTo(0.0f, 0.0f);
        gp3.lineTo(2.0f, 2.0f);
        assertTrue(ShapeUtilities.equal((Shape) gp1, (Shape) gp2));
        assertFalse(ShapeUtilities.equal((Shape) gp1, (Shape) gp3));

        // Mismatched Shape types
        assertFalse(ShapeUtilities.equal(a1, p1));
        assertFalse(ShapeUtilities.equal(p1, gp1));
        assertFalse(ShapeUtilities.equal(gp1, a1));

        // Custom shape using fallback .equals()
        Shape custom1 = new CustomShape(1.0);
        Shape custom2 = new CustomShape(1.0);
        Shape custom3 = new CustomShape(2.0);
        assertTrue(ShapeUtilities.equal(custom1, custom2));
        assertFalse(ShapeUtilities.equal(custom1, custom3));
    }

    // Tests Line2D equality for differences in y1, y2, x2
    @Test
    public void testEqualLine2D_remainingCoordinateBranches_returnsExpected() {
        Line2D lBase = new Line2D.Double(1.0, 2.0, 3.0, 4.0);
        Line2D lDiffY1 = new Line2D.Double(1.0, 2.1, 3.0, 4.0);
        Line2D lDiffY2 = new Line2D.Double(1.0, 2.0, 3.0, 4.1);

        assertFalse(ShapeUtilities.equal(lBase, lDiffY1));
        assertFalse(ShapeUtilities.equal(lBase, lDiffY2));
    }

    // Tests Ellipse2D equality for differences in y, width, height
    @Test
    public void testEqualEllipse2D_remainingCoordinateBranches_returnsExpected() {
        Ellipse2D eBase = new Ellipse2D.Double(1.0, 2.0, 3.0, 4.0);
        Ellipse2D eDiffY = new Ellipse2D.Double(1.0, 2.1, 3.0, 4.0);
        Ellipse2D eDiffW = new Ellipse2D.Double(1.0, 2.0, 3.1, 4.0);
        Ellipse2D eDiffH = new Ellipse2D.Double(1.0, 2.0, 3.0, 4.1);

        assertFalse(ShapeUtilities.equal(eBase, eDiffY));
        assertFalse(ShapeUtilities.equal(eBase, eDiffW));
        assertFalse(ShapeUtilities.equal(eBase, eDiffH));
    }

    // Tests GeneralPath equality covering quadTo, curveTo, and differing segment counts
    @Test
    public void testEqualGeneralPath_quadToCurveToAndSegmentCounts_returnsExpected() {
        GeneralPath gpQuad1 = new GeneralPath();
        gpQuad1.moveTo(0.0f, 0.0f);
        gpQuad1.quadTo(1.0f, 2.0f, 3.0f, 4.0f);

        GeneralPath gpQuad2 = new GeneralPath();
        gpQuad2.moveTo(0.0f, 0.0f);
        gpQuad2.quadTo(1.0f, 2.0f, 3.0f, 4.0f);
        assertTrue(ShapeUtilities.equal(gpQuad1, gpQuad2));

        GeneralPath gpCurve1 = new GeneralPath();
        gpCurve1.moveTo(0.0f, 0.0f);
        gpCurve1.curveTo(1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f);

        GeneralPath gpCurve2 = new GeneralPath();
        gpCurve2.moveTo(0.0f, 0.0f);
        gpCurve2.curveTo(1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f);
        assertTrue(ShapeUtilities.equal(gpCurve1, gpCurve2));

        // Different segment types at same step
        assertFalse(ShapeUtilities.equal(gpQuad1, gpCurve1));

        // Differing number of segments (path1 has fewer segments than path2)
        GeneralPath gpShort = new GeneralPath();
        gpShort.moveTo(0.0f, 0.0f);
        GeneralPath gpLong = new GeneralPath();
        gpLong.moveTo(0.0f, 0.0f);
        gpLong.lineTo(1.0f, 1.0f);
        assertFalse(ShapeUtilities.equal(gpShort, gpLong));
        assertFalse(ShapeUtilities.equal(gpLong, gpShort));
    }

    // Tests clone with custom non-cloneable Shape
    @Test
    public void testClone_nonCloneableShape_returnsCopyOrNull() {
        CustomShape nonCloneable = new CustomShape(5.0);
        Shape result = ShapeUtilities.clone(nonCloneable);
        assertNull(result);
    }

    // Tests createTranslatedShape(Shape, RectangleAnchor, double, double) with null shape
    @Test(expected = IllegalArgumentException.class)
    public void testCreateTranslatedShape_withAnchorAndNullShape_throwsException() {
        ShapeUtilities.createTranslatedShape(null, RectangleAnchor.TOP_LEFT, 1.0, 1.0);
    }

    // Tests drawRotatedShape with null shape or null Graphics2D
    @Test
    public void testDrawRotatedShape_nullInputs_noExceptionThrown() {
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        ShapeUtilities.drawRotatedShape(g2, null, Math.PI / 4, 10.0f, 10.0f);
        ShapeUtilities.drawRotatedShape(null, new Rectangle2D.Double(0, 0, 10, 10), Math.PI / 4, 10.0f, 10.0f);
        g2.dispose();
    }

    // Tests createLineRegion with horizontal line
    @Test
    public void testCreateLineRegion_horizontalLine_createsValidRegion() {
        Line2D horizontal = new Line2D.Double(0.0, 5.0, 10.0, 5.0);
        Shape region = ShapeUtilities.createLineRegion(horizontal, 2.0f);
        assertNotNull(region);
        assertTrue(region.getBounds2D().getHeight() > 0);
    }

    // Tests contains method for all edge boundary conditions
    @Test
    public void testContains_allBoundaryConditions_returnsCorrectResult() {
        Rectangle2D base = new Rectangle2D.Double(10.0, 10.0, 50.0, 50.0);

        Rectangle2D xSmaller = new Rectangle2D.Double(9.0, 10.0, 50.0, 50.0);
        Rectangle2D ySmaller = new Rectangle2D.Double(10.0, 9.0, 50.0, 50.0);
        Rectangle2D xLarger = new Rectangle2D.Double(10.0, 10.0, 51.0, 50.0);
        Rectangle2D yLarger = new Rectangle2D.Double(10.0, 10.0, 50.0, 51.0);

        assertFalse(ShapeUtilities.contains(base, xSmaller));
        assertFalse(ShapeUtilities.contains(base, ySmaller));
        assertFalse(ShapeUtilities.contains(base, xLarger));
        assertFalse(ShapeUtilities.contains(base, yLarger));
    }

    // Tests intersects method for all 4 non-overlapping quadrants
    @Test
    public void testIntersects_allDisjointDirections_returnsFalse() {
        Rectangle2D base = new Rectangle2D.Double(10.0, 10.0, 20.0, 20.0);

        Rectangle2D left = new Rectangle2D.Double(0.0, 10.0, 5.0, 20.0);
        Rectangle2D right = new Rectangle2D.Double(40.0, 10.0, 5.0, 20.0);
        Rectangle2D above = new Rectangle2D.Double(10.0, 0.0, 20.0, 5.0);
        Rectangle2D below = new Rectangle2D.Double(10.0, 40.0, 20.0, 5.0);

        assertFalse(ShapeUtilities.intersects(base, left));
        assertFalse(ShapeUtilities.intersects(base, right));
        assertFalse(ShapeUtilities.intersects(base, above));
        assertFalse(ShapeUtilities.intersects(base, below));
    }

    // Tests private constructor for code coverage
    @Test
    public void testPrivateConstructor() throws Exception {
        Constructor<ShapeUtilities> constructor = ShapeUtilities.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        try {
            ShapeUtilities instance = constructor.newInstance();
            assertNotNull(instance);
        } catch (InvocationTargetException e) {
            // If constructor throws an exception (e.g. AssertionError), that is also acceptable
        }
    }

    /**
     * Dummy non-cloneable Shape implementation for testing fallback branches.
     */
    private static class CustomShape implements Shape {
        private final double value;

        CustomShape(double value) {
            this.value = value;
        }

        @Override
        public java.awt.Rectangle getBounds() {
            return new java.awt.Rectangle(0, 0, (int) value, (int) value);
        }

        @Override
        public Rectangle2D getBounds2D() {
            return new Rectangle2D.Double(0, 0, value, value);
        }

        @Override
        public boolean contains(double x, double y) {
            return false;
        }

        @Override
        public boolean contains(Point2D p) {
            return false;
        }

        @Override
        public boolean intersects(double x, double y, double w, double h) {
            return false;
        }

        @Override
        public boolean intersects(Rectangle2D r) {
            return false;
        }

        @Override
        public boolean contains(double x, double y, double w, double h) {
            return false;
        }

        @Override
        public boolean contains(Rectangle2D r) {
            return false;
        }

        @Override
        public PathIterator getPathIterator(AffineTransform at) {
            return new Rectangle2D.Double(0, 0, value, value).getPathIterator(at);
        }

        @Override
        public PathIterator getPathIterator(AffineTransform at, double flatness) {
            return new Rectangle2D.Double(0, 0, value, value).getPathIterator(at, flatness);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (!(obj instanceof CustomShape)) return false;
            CustomShape other = (CustomShape) obj;
            return Double.compare(this.value, other.value) == 0;
        }

        @Override
        public int hashCode() {
            return Double.valueOf(value).hashCode();
        }
    }
}