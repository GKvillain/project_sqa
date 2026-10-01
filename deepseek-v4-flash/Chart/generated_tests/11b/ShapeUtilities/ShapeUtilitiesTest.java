package org.jfree.chart.util;

import static org.junit.Assert.*;
import org.junit.Test;

import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class ShapeUtilitiesTest {

    // ------------------ equal(Shape, Shape) ------------------

    // Test equal with two null shapes -> true
    @Test
    public void testEqual_bothNull_returnsTrue() {
        assertTrue(ShapeUtilities.equal((Shape) null, (Shape) null));
    }

    // Test equal with first null, second not null -> false
    @Test
    public void testEqual_firstNull_returnsFalse() {
        assertFalse(ShapeUtilities.equal(null, new Rectangle2D.Double()));
    }

    // Test equal with two identical Line2D objects -> true
    @Test
    public void testEqual_sameLine2D_returnsTrue() {
        Line2D l1 = new Line2D.Double(1.0, 2.0, 3.0, 4.0);
        Line2D l2 = new Line2D.Double(1.0, 2.0, 3.0, 4.0);
        assertTrue(ShapeUtilities.equal(l1, l2));
    }

    // Test equal with different Line2D objects -> false
    @Test
    public void testEqual_diffLine2D_returnsFalse() {
        Line2D l1 = new Line2D.Double(0.0, 0.0, 10.0, 10.0);
        Line2D l2 = new Line2D.Double(0.0, 0.0, 20.0, 20.0);
        assertFalse(ShapeUtilities.equal(l1, l2));
    }

    // Test equal with two identical Ellipse2D -> true
    @Test
    public void testEqual_sameEllipse2D_returnsTrue() {
        Ellipse2D e1 = new Ellipse2D.Double(5, 5, 10, 20);
        Ellipse2D e2 = new Ellipse2D.Double(5, 5, 10, 20);
        assertTrue(ShapeUtilities.equal(e1, e2));
    }

    // Test equal with different Ellipse2D -> false
    @Test
    public void testEqual_diffEllipse2D_returnsFalse() {
        Ellipse2D e1 = new Ellipse2D.Double(1, 2, 3, 4);
        Ellipse2D e2 = new Ellipse2D.Double(2, 2, 3, 4);
        assertFalse(ShapeUtilities.equal(e1, e2));
    }

    // Test equal with two identical Arc2D -> true
    @Test
    public void testEqual_sameArc2D_returnsTrue() {
        Arc2D a1 = new Arc2D.Double(0, 0, 100, 100, 30, 60, Arc2D.PIE);
        Arc2D a2 = new Arc2D.Double(0, 0, 100, 100, 30, 60, Arc2D.PIE);
        assertTrue(ShapeUtilities.equal(a1, a2));
    }

    // Test equal with different Arc2D (different start angle) -> false
    @Test
    public void testEqual_diffArc2D_returnsFalse() {
        Arc2D a1 = new Arc2D.Double(0, 0, 100, 100, 30, 60, Arc2D.PIE);
        Arc2D a2 = new Arc2D.Double(0, 0, 100, 100, 45, 60, Arc2D.PIE);
        assertFalse(ShapeUtilities.equal(a1, a2));
    }

    // Test equal with two identical Polygon -> true
    @Test
    public void testEqual_samePolygon_returnsTrue() {
        Polygon p1 = new Polygon(new int[]{0,10,20}, new int[]{0,10,0}, 3);
        Polygon p2 = new Polygon(new int[]{0,10,20}, new int[]{0,10,0}, 3);
        assertTrue(ShapeUtilities.equal(p1, p2));
    }

    // Test equal with different Polygon (different points) -> false
    @Test
    public void testEqual_diffPolygon_returnsFalse() {
        Polygon p1 = new Polygon(new int[]{0,10}, new int[]{0,10}, 2);
        Polygon p2 = new Polygon(new int[]{0,20}, new int[]{0,20}, 2);
        assertFalse(ShapeUtilities.equal(p1, p2));
    }

    // Test equal with two identical GeneralPath -> true
    @Test
    public void testEqual_sameGeneralPath_returnsTrue() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(10, 20);
        gp1.lineTo(30, 40);
        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(10, 20);
        gp2.lineTo(30, 40);
        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    // ** Defect detection test **
    // equal(GeneralPath, GeneralPath) has a bug: it compares both paths using
    // the same iterator (p1.getPathIterator(null) for both).
    // This test should fail when run against the buggy version because
    // different paths will be reported as equal.
    @Test
    public void testEqual_diffGeneralPath_returnsFalse() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(10, 10);
        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(20, 20); // different point
        assertFalse(ShapeUtilities.equal(gp1, gp2));
    }

    // Test equal with Rectangle2D (fallback to ObjectUtilities.equal) -> true
    @Test
    public void testEqual_sameRectangle2D_returnsTrue() {
        Rectangle2D r1 = new Rectangle2D.Double(1, 2, 3, 4);
        Rectangle2D r2 = new Rectangle2D.Double(1, 2, 3, 4);
        assertTrue(ShapeUtilities.equal(r1, r2));
    }

    // Test equal with different Rectangle2D -> false
    @Test
    public void testEqual_diffRectangle2D_returnsFalse() {
        Rectangle2D r1 = new Rectangle2D.Double(1, 2, 3, 4);
        Rectangle2D r2 = new Rectangle2D.Double(1, 2, 5, 4);
        assertFalse(ShapeUtilities.equal(r1, r2));
    }

    // ------------------ clone ------------------

    @Test
    public void testClone_nullInput_returnsNull() {
        assertNull(ShapeUtilities.clone(null));
    }

    @Test
    public void testClone_Line2D_returnsEqualClone() {
        Line2D line = new Line2D.Double(1.0, 2.0, 3.0, 4.0);
        Shape cloned = ShapeUtilities.clone(line);
        assertNotNull(cloned);
        assertTrue(ShapeUtilities.equal(line, cloned));
    }

    // ------------------ createTranslatedShape ------------------

    @Test(expected = IllegalArgumentException.class)
    public void testCreateTranslatedShape_nullShape_throwsException() {
        ShapeUtilities.createTranslatedShape(null, 5, 5);
    }

    @Test
    public void testCreateTranslatedShape_positiveTranslation_returnsTranslated() {
        Rectangle2D rect = new Rectangle2D.Double(0, 0, 10, 10);
        Shape translated = ShapeUtilities.createTranslatedShape(rect, 5, 5);
        Rectangle2D bounds = translated.getBounds2D();
        assertEquals(5, bounds.getX(), 0.0001);
        assertEquals(5, bounds.getY(), 0.0001);
        assertEquals(10, bounds.getWidth(), 0.0001);
        assertEquals(10, bounds.getHeight(), 0.0001);
    }

    // ------------------ rotateShape ------------------

    @Test
    public void testRotateShape_nullBase_returnsNull() {
        assertNull(ShapeUtilities.rotateShape(null, 0.5, 10, 10));
    }

    @Test
    public void testRotateShape_zeroAngle_returnsSameShape() {
        Rectangle2D rect = new Rectangle2D.Double(0, 0, 10, 10);
        Shape rotated = ShapeUtilities.rotateShape(rect, 0.0, 0, 0);
        assertTrue(ShapeUtilities.equal(rect, rotated));
    }

    // ------------------ createLineRegion ------------------

    @Test
    public void testCreateLineRegion_verticalLine_createsRegion() {
        Line2D line = new Line2D.Double(5, 0, 5, 10); // vertical
        Shape region = ShapeUtilities.createLineRegion(line, 2.0f);
        assertNotNull(region);
        // shape should be a GeneralPath (non-null)
        assertTrue(region instanceof GeneralPath);
    }

    @Test
    public void testCreateLineRegion_horizontalLine_createsRegion() {
        Line2D line = new Line2D.Double(0, 5, 10, 5); // horizontal
        Shape region = ShapeUtilities.createLineRegion(line, 2.0f);
        assertNotNull(region);
        assertTrue(region instanceof GeneralPath);
    }

    // ------------------ createDiagonalCross / createRegularCross / etc. ------------------

    @Test
    public void testCreateDiagonalCross_returnsNonNullShape() {
        assertNotNull(ShapeUtilities.createDiagonalCross(5, 2));
    }

    @Test
    public void testCreateRegularCross_returnsNonNullShape() {
        assertNotNull(ShapeUtilities.createRegularCross(5, 2));
    }

    @Test
    public void testCreateDiamond_returnsNonNullShape() {
        assertNotNull(ShapeUtilities.createDiamond(10));
    }

    @Test
    public void testCreateUpTriangle_returnsNonNullShape() {
        assertNotNull(ShapeUtilities.createUpTriangle(10));
    }

    @Test
    public void testCreateDownTriangle_returnsNonNullShape() {
        assertNotNull(ShapeUtilities.createDownTriangle(10));
    }

    // ------------------ getPointInRectangle ------------------

    @Test
    public void testGetPointInRectangle_pointInside_returnsSamePoint() {
        Rectangle2D rect = new Rectangle2D.Double(0, 0, 100, 100);
        Point2D result = ShapeUtilities.getPointInRectangle(50, 50, rect);
        assertEquals(50, result.getX(), 0.0001);
        assertEquals(50, result.getY(), 0.0001);
    }

    @Test
    public void testGetPointInRectangle_pointAboveBounds_clampsToEdge() {
        Rectangle2D rect = new Rectangle2D.Double(0, 0, 100, 100);
        Point2D result = ShapeUtilities.getPointInRectangle(50, 150, rect);
        assertEquals(50, result.getX(), 0.0001);
        assertEquals(100, result.getY(), 0.0001);
    }

    // ------------------ contains ------------------

    @Test
    public void testContains_rectContained_returnsTrue() {
        Rectangle2D outer = new Rectangle2D.Double(0, 0, 20, 20);
        Rectangle2D inner = new Rectangle2D.Double(5, 5, 10, 10);
        assertTrue(ShapeUtilities.contains(outer, inner));
    }

    @Test
    public void testContains_rectPartlyOutside_returnsFalse() {
        Rectangle2D outer = new Rectangle2D.Double(0, 0, 20, 20);
        Rectangle2D inner = new Rectangle2D.Double(5, 5, 30, 10);
        assertFalse(ShapeUtilities.contains(outer, inner));
    }

    // ------------------ intersects ------------------

    @Test
    public void testIntersects_overlapping_returnsTrue() {
        Rectangle2D r1 = new Rectangle2D.Double(0, 0, 10, 10);
        Rectangle2D r2 = new Rectangle2D.Double(5, 5, 10, 10);
        assertTrue(ShapeUtilities.intersects(r1, r2));
    }

    @Test
    public void testIntersects_nonOverlapping_returnsFalse() {
        Rectangle2D r1 = new Rectangle2D.Double(0, 0, 10, 10);
        Rectangle2D r2 = new Rectangle2D.Double(20, 20, 10, 10);
        assertFalse(ShapeUtilities.intersects(r1, r2));
    }
}