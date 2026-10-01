package org.jfree.chart.block;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.jfree.chart.util.RectangleEdge;
import org.jfree.chart.util.Size2D;
import org.jfree.data.Range;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BorderArrangementTest {

    private static final double EPSILON = 0.0001;

    private Graphics2D g2;

    @Before
    public void setUp() {
        BufferedImage image = new BufferedImage(1, 1,
                BufferedImage.TYPE_INT_ARGB);
        g2 = image.createGraphics();
    }

    @After
    public void tearDown() {
        g2.dispose();
    }

    // Tests an empty layout with no blocks.
    @Test
    public void testArrange_noBlocks_returnsZeroSize() {
        BorderArrangement arrangement = new BorderArrangement();
        Size2D size = arrangement.arrange(new BlockContainer(), g2,
                RectangleConstraint.NONE);
        assertEquals(0.0, size.width, EPSILON);
        assertEquals(0.0, size.height, EPSILON);
    }

    // Tests arrangeNN with all five positions occupied.
    @Test
    public void testArrange_noConstraints_allBlocks_returnsCorrectSize() {
        BorderArrangement arrangement = new BorderArrangement();
        arrangement.add(new EmptyBlock(100.0, 20.0), RectangleEdge.TOP);
        arrangement.add(new EmptyBlock(80.0, 30.0), RectangleEdge.BOTTOM);
        arrangement.add(new EmptyBlock(20.0, 40.0), RectangleEdge.LEFT);
        arrangement.add(new EmptyBlock(50.0, 30.0), RectangleEdge.RIGHT);
        arrangement.add(new EmptyBlock(60.0, 70.0), null);

        Size2D size = arrangement.arrange(new BlockContainer(), g2,
                RectangleConstraint.NONE);
        assertEquals(130.0, size.width, EPSILON);
        assertEquals(120.0, size.height, EPSILON);
    }

    // Tests the fixed-width / no-height arrangement path.
    @Test
    public void testArrange_fixedWidthNoHeight_allBlocks_returnsFixedWidthAndComputedHeight() {
        BorderArrangement arrangement = new BorderArrangement();
        arrangement.add(new EmptyBlock(100.0, 20.0), RectangleEdge.TOP);
        arrangement.add(new EmptyBlock(80.0, 30.0), RectangleEdge.BOTTOM);
        arrangement.add(new EmptyBlock(20.0, 40.0), RectangleEdge.LEFT);
        arrangement.add(new EmptyBlock(50.0, 30.0), RectangleEdge.RIGHT);
        arrangement.add(new EmptyBlock(60.0, 70.0), null);

        RectangleConstraint constraint = new RectangleConstraint(100.0, null,
                LengthConstraintType.FIXED, 0.0, null,
                LengthConstraintType.NONE);
        Size2D size = arrangement.arrange(new BlockContainer(), g2, constraint);
        assertEquals(100.0, size.width, EPSILON);
        assertEquals(120.0, size.height, EPSILON);
    }

    // Tests the fixed-width / height-range path when the natural height is within range.
    @Test
    public void testArrange_fixedWidthHeightRange_withinRange_returnsNaturalHeight() {
        BorderArrangement arrangement = new BorderArrangement();
        arrangement.add(new EmptyBlock(100.0, 20.0), RectangleEdge.TOP);
        arrangement.add(new EmptyBlock(80.0, 30.0), RectangleEdge.BOTTOM);
        arrangement.add(new EmptyBlock(20.0, 40.0), RectangleEdge.LEFT);
        arrangement.add(new EmptyBlock(50.0, 30.0), RectangleEdge.RIGHT);
        arrangement.add(new EmptyBlock(60.0, 70.0), null);

        RectangleConstraint constraint = new RectangleConstraint(100.0, null,
                LengthConstraintType.FIXED, 0.0, new Range(0.0, 200.0),
                LengthConstraintType.RANGE);
        Size2D size = arrangement.arrange(new BlockContainer(), g2, constraint);
        assertEquals(100.0, size.width, EPSILON);
        assertEquals(120.0, size.height, EPSILON);
    }

    // Tests the fixed-width / height-range path when the natural height exceeds the range.
    @Test
    public void testArrange_fixedWidthHeightRange_aboveRange_constrainsToUpperBound() {
        BorderArrangement arrangement = new BorderArrangement();
        arrangement.add(new EmptyBlock(100.0, 20.0), RectangleEdge.TOP);
        arrangement.add(new EmptyBlock(80.0, 30.0), RectangleEdge.BOTTOM);
        arrangement.add(new EmptyBlock(20.0, 40.0), RectangleEdge.LEFT);
        arrangement.add(new EmptyBlock(50.0, 30.0), RectangleEdge.RIGHT);
        arrangement.add(new EmptyBlock(60.0, 70.0), null);

        RectangleConstraint constraint = new RectangleConstraint(100.0, null,
                LengthConstraintType.FIXED, 0.0, new Range(0.0, 100.0),
                LengthConstraintType.RANGE);
        Size2D size = arrangement.arrange(new BlockContainer(), g2, constraint);
        assertEquals(100.0, size.width, EPSILON);
        assertEquals(100.0, size.height, EPSILON);
    }

    // Tests arrangeFF and verifies that the center block is not overlapped by left/right blocks.
    @Test
    public void testArrange_fixedWidthFixedHeight_allBlocks_returnsCorrectBounds() {
        BorderArrangement arrangement = new BorderArrangement();
        EmptyBlock top = new EmptyBlock(100.0, 20.0);
        EmptyBlock bottom = new EmptyBlock(80.0, 30.0);
        EmptyBlock left = new EmptyBlock(20.0, 40.0);
        EmptyBlock right = new EmptyBlock(50.0, 30.0);
        EmptyBlock center = new EmptyBlock(60.0, 70.0);
        arrangement.add(top, RectangleEdge.TOP);
        arrangement.add(bottom, RectangleEdge.BOTTOM);
        arrangement.add(left, RectangleEdge.LEFT);
        arrangement.add(right, RectangleEdge.RIGHT);
        arrangement.add(center, null);

        Size2D size = arrangement.arrange(new BlockContainer(), g2,
                new RectangleConstraint(100.0, 80.0));
        assertEquals(100.0, size.width, EPSILON);
        assertEquals(80.0, size.height, EPSILON);
        assertEquals(30.0, center.getBounds().getWidth(), EPSILON);
        assertEquals(30.0, center.getBounds().getHeight(), EPSILON);
        assertEquals(20.0, center.getBounds().getX(), EPSILON);
        assertEquals(20.0, center.getBounds().getY(), EPSILON);
        assertEquals(50.0, right.getBounds().getX(), EPSILON);
        assertEquals(50.0, right.getBounds().getWidth(), EPSILON);
        assertEquals(50.0, bottom.getBounds().getY(), EPSILON);
    }

    // Tests the range-width / range-height arrangement path.
    @Test
    public void testArrange_rangeWidthRangeHeight_allBlocks_returnsConstrainedSize() {
        BorderArrangement arrangement = new BorderArrangement();
        arrangement.add(new EmptyBlock(100.0, 20.0), RectangleEdge.TOP);
        arrangement.add(new EmptyBlock(80.0, 30.0), RectangleEdge.BOTTOM);
        arrangement.add(new EmptyBlock(20.0, 40.0), RectangleEdge.LEFT);
        arrangement.add(new EmptyBlock(50.0, 30.0), RectangleEdge.RIGHT);
        arrangement.add(new EmptyBlock(60.0, 70.0), null);

        RectangleConstraint constraint = new RectangleConstraint(
                new Range(0.0, 100.0), new Range(0.0, 100.0));
        Size2D size = arrangement.arrange(new BlockContainer(), g2, constraint);
        assertEquals(100.0, size.width, EPSILON);
        assertEquals(100.0, size.height, EPSILON);
    }

    // Tests the unsupported width=NONE / height=FIXED constraint path.
    @Test(expected = RuntimeException.class)
    public void testArrange_widthNoneHeightFixed_throwsRuntimeException() {
        BorderArrangement arrangement = new BorderArrangement();
        RectangleConstraint constraint = new RectangleConstraint(0.0, null,
                LengthConstraintType.NONE, 100.0, null,
                LengthConstraintType.FIXED);
        arrangement.arrange(new BlockContainer(), g2, constraint);
    }

    // Tests the unsupported width=NONE / height=RANGE constraint path.
    @Test(expected = RuntimeException.class)
    public void testArrange_widthNoneHeightRange_throwsRuntimeException() {
        BorderArrangement arrangement = new BorderArrangement();
        RectangleConstraint constraint = new RectangleConstraint(0.0, null,
                LengthConstraintType.NONE, 0.0, new Range(0.0, 100.0),
                LengthConstraintType.RANGE);
        arrangement.arrange(new BlockContainer(), g2, constraint);
    }

    // Tests the unsupported width=RANGE / height=NONE constraint path.
    @Test(expected = RuntimeException.class)
    public void testArrange_widthRangeHeightNone_throwsRuntimeException() {
        BorderArrangement arrangement = new BorderArrangement();
        RectangleConstraint constraint = new RectangleConstraint(0.0,
                new Range(0.0, 100.0), LengthConstraintType.RANGE, 0.0, null,
                LengthConstraintType.NONE);
        arrangement.arrange(new BlockContainer(), g2, constraint);
    }

    // Tests the unsupported width=RANGE / height=FIXED constraint path.
    @Test(expected = RuntimeException.class)
    public void testArrange_widthRangeHeightFixed_throwsRuntimeException() {
        BorderArrangement arrangement = new BorderArrangement();
        RectangleConstraint constraint = new RectangleConstraint(0.0,
                new Range(0.0, 100.0), LengthConstraintType.RANGE, 50.0, null,
                LengthConstraintType.FIXED);
        arrangement.arrange(new BlockContainer(), g2, constraint);
    }

    // Tests that add(block, null) places the block in the center.
    @Test
    public void testAdd_nullKey_setsCenterBlock() {
        BorderArrangement arrangement = new BorderArrangement();
        arrangement.add(new EmptyBlock(30.0, 40.0), null);

        Size2D size = arrangement.arrange(new BlockContainer(), g2,
                RectangleConstraint.NONE);
        assertEquals(30.0, size.width, EPSILON);
        assertEquals(40.0, size.height, EPSILON);
    }

    // Tests clear() removes all registered blocks.
    @Test
    public void testClear_removesAllBlocks() {
        BorderArrangement arrangement = new BorderArrangement();
        arrangement.add(new EmptyBlock(10.0, 10.0), RectangleEdge.TOP);
        arrangement.add(new EmptyBlock(20.0, 20.0), null);

        arrangement.clear();
        assertTrue(arrangement.equals(new BorderArrangement()));
    }

    // Tests equals() for identical and different arrangements.
    @Test
    public void testEquals_sameAndDifferentBlocks_returnsExpected() {
        BorderArrangement a = new BorderArrangement();
        BorderArrangement b = new BorderArrangement();
        assertTrue(a.equals(a));
        assertTrue(a.equals(b));
        assertFalse(a.equals(null));
        assertFalse(a.equals("unrelated object"));

        EmptyBlock top = new EmptyBlock(10.0, 10.0);
        a.add(top, RectangleEdge.TOP);
        b.add(top, RectangleEdge.TOP);
        assertTrue(a.equals(b));

        a.add(new EmptyBlock(20.0, 20.0), RectangleEdge.BOTTOM);
        assertFalse(a.equals(b));
    }
}