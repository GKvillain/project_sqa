package org.jfree.chart.block;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.jfree.chart.util.RectangleEdge;
import org.jfree.chart.util.Size2D;
import org.jfree.data.Range;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class BorderArrangementTest {

    private BorderArrangement arrangement;
    private BlockContainer container;

    @Before
    public void setUp() {
        this.arrangement = new BorderArrangement();
        this.container = new BlockContainer(this.arrangement);
    }

    // Tests equals method with identical instance, null, and non-BorderArrangement instance
    @Test
    public void testEquals_basicComparisons_returnsExpected() {
        assertTrue(this.arrangement.equals(this.arrangement));
        assertFalse(this.arrangement.equals(null));
        assertFalse(this.arrangement.equals("Not a BorderArrangement"));
    }

    // Tests equals method when different blocks are assigned to different edges
    @Test
    public void testEquals_differentBlocks_returnsFalse() {
        BorderArrangement arr1 = new BorderArrangement();
        BorderArrangement arr2 = new BorderArrangement();
        assertTrue(arr1.equals(arr2));

        EmptyBlock b1 = new EmptyBlock(10.0, 10.0);
        EmptyBlock b2 = new EmptyBlock(20.0, 20.0);

        arr1.add(b1, RectangleEdge.TOP);
        assertFalse(arr1.equals(arr2));
        arr2.add(b1, RectangleEdge.TOP);
        assertTrue(arr1.equals(arr2));

        arr1.add(b1, RectangleEdge.BOTTOM);
        assertFalse(arr1.equals(arr2));
        arr2.add(b1, RectangleEdge.BOTTOM);
        assertTrue(arr1.equals(arr2));

        arr1.add(b1, RectangleEdge.LEFT);
        assertFalse(arr1.equals(arr2));
        arr2.add(b1, RectangleEdge.LEFT);
        assertTrue(arr1.equals(arr2));

        arr1.add(b1, RectangleEdge.RIGHT);
        assertFalse(arr1.equals(arr2));
        arr2.add(b1, RectangleEdge.RIGHT);
        assertTrue(arr1.equals(arr2));

        arr1.add(b1, null);
        assertFalse(arr1.equals(arr2));
        arr2.add(b2, null);
        assertFalse(arr1.equals(arr2));
        arr2.add(b1, null);
        assertTrue(arr1.equals(arr2));
    }

    // Tests clear method resets all placed blocks
    @Test
    public void testClear_populatedArrangement_clearsAllBlocks() {
        EmptyBlock block = new EmptyBlock(10.0, 10.0);
        this.arrangement.add(block, RectangleEdge.TOP);
        this.arrangement.add(block, RectangleEdge.BOTTOM);
        this.arrangement.add(block, RectangleEdge.LEFT);
        this.arrangement.add(block, RectangleEdge.RIGHT);
        this.arrangement.add(block, null);

        this.arrangement.clear();
        BorderArrangement emptyArrangement = new BorderArrangement();
        assertTrue(this.arrangement.equals(emptyArrangement));
    }

    // Tests add method with an unrecognized key
    @Test
    public void testAdd_unknownKey_doesNotAddBlock() {
        BorderArrangement empty = new BorderArrangement();
        this.arrangement.add(new EmptyBlock(10.0, 10.0), "INVALID_KEY");
        // Unknown key should not affect top/bottom/left/right/center
        assertTrue(this.arrangement.equals(empty));
    }

    // Tests arrangeNN with all 5 border blocks present
    @Test
    public void testArrangeNN_allBlocksPresent_calculatesCorrectSize() {
        EmptyBlock top = new EmptyBlock(100.0, 20.0);
        EmptyBlock bottom = new EmptyBlock(100.0, 25.0);
        EmptyBlock left = new EmptyBlock(15.0, 40.0);
        EmptyBlock right = new EmptyBlock(25.0, 50.0);
        EmptyBlock center = new EmptyBlock(60.0, 30.0);

        this.arrangement.add(top, RectangleEdge.TOP);
        this.arrangement.add(bottom, RectangleEdge.BOTTOM);
        this.arrangement.add(left, RectangleEdge.LEFT);
        this.arrangement.add(right, RectangleEdge.RIGHT);
        this.arrangement.add(center, null);

        Size2D size = this.arrangement.arrangeNN(this.container, null);
        // width = max(100, 100, 15 + 60 + 25) = 100
        // centerHeight = max(max(40, 50), max(50, 30)) = 50
        // totalHeight = 20 + 25 + 50 = 95
        assertEquals(100.0, size.getWidth(), 0.001);
        assertEquals(95.0, size.getHeight(), 0.001);

        assertEquals(new Rectangle2D.Double(0.0, 0.0, 100.0, 20.0), top.getBounds());
        assertEquals(new Rectangle2D.Double(0.0, 70.0, 100.0, 25.0), bottom.getBounds());
        assertEquals(new Rectangle2D.Double(0.0, 20.0, 15.0, 50.0), left.getBounds());
        assertEquals(new Rectangle2D.Double(75.0, 20.0, 25.0, 50.0), right.getBounds());
        assertEquals(new Rectangle2D.Double(15.0, 20.0, 60.0, 50.0), center.getBounds());
    }

    // Tests arrangeNN with no blocks attached
    @Test
    public void testArrangeNN_noBlocks_returnsZeroSize() {
        Size2D size = this.arrangement.arrangeNN(this.container, null);
        assertEquals(0.0, size.getWidth(), 0.001);
        assertEquals(0.0, size.getHeight(), 0.001);
    }

    // Tests arrangeFF with all blocks and fixed width/height
    @Test
    public void testArrangeFF_fixedDimensions_arrangesCorrectly() {
        EmptyBlock top = new EmptyBlock(10.0, 20.0);
        EmptyBlock bottom = new EmptyBlock(10.0, 20.0);
        EmptyBlock left = new EmptyBlock(30.0, 10.0);
        EmptyBlock right = new EmptyBlock(40.0, 10.0);
        EmptyBlock center = new EmptyBlock(10.0, 10.0);

        this.arrangement.add(top, RectangleEdge.TOP);
        this.arrangement.add(bottom, RectangleEdge.BOTTOM);
        this.arrangement.add(left, RectangleEdge.LEFT);
        this.arrangement.add(right, RectangleEdge.RIGHT);
        this.arrangement.add(center, null);

        RectangleConstraint constraint = new RectangleConstraint(200.0, 100.0);
        Size2D size = this.arrangement.arrangeFF(this.container, null, constraint);

        assertEquals(200.0, size.getWidth(), 0.001);
        assertEquals(100.0, size.getHeight(), 0.001);

        assertEquals(new Rectangle2D.Double(0.0, 0.0, 200.0, 20.0), top.getBounds());
        assertEquals(new Rectangle2D.Double(0.0, 80.0, 200.0, 20.0), bottom.getBounds());
        assertEquals(new Rectangle2D.Double(0.0, 20.0, 30.0, 60.0), left.getBounds());
        assertEquals(new Rectangle2D.Double(160.0, 20.0, 40.0, 60.0), right.getBounds());
        assertEquals(new Rectangle2D.Double(30.0, 20.0, 130.0, 60.0), center.getBounds());
    }

    // Tests arrangeFN with fixed width and unconstrained height
    @Test
    public void testArrangeFN_fixedWidth_calculatesHeightCorrectly() {
        EmptyBlock top = new EmptyBlock(50.0, 20.0);
        EmptyBlock bottom = new EmptyBlock(50.0, 30.0);
        EmptyBlock left = new EmptyBlock(20.0, 40.0);
        EmptyBlock right = new EmptyBlock(30.0, 50.0);
        EmptyBlock center = new EmptyBlock(40.0, 35.0);

        this.arrangement.add(top, RectangleEdge.TOP);
        this.arrangement.add(bottom, RectangleEdge.BOTTOM);
        this.arrangement.add(left, RectangleEdge.LEFT);
        this.arrangement.add(right, RectangleEdge.RIGHT);
        this.arrangement.add(center, null);

        Size2D size = this.arrangement.arrangeFN(this.container, null, 150.0);
        assertEquals(150.0, size.getWidth(), 0.001);
        // height = h[0](20) + h[1](30) + max(h[2](50), h[3](50), h[4](35)) = 100
        assertEquals(100.0, size.getHeight(), 0.001);
    }

    // Tests arrangeFR when arranged height is already within height range
    @Test
    public void testArrangeFR_heightInRange_returnsUnconstrainedHeight() {
        EmptyBlock top = new EmptyBlock(50.0, 20.0);
        EmptyBlock bottom = new EmptyBlock(50.0, 30.0);
        this.arrangement.add(top, RectangleEdge.TOP);
        this.arrangement.add(bottom, RectangleEdge.BOTTOM);

        RectangleConstraint constraint = new RectangleConstraint(
                100.0, null, LengthConstraintType.FIXED,
                0.0, new Range(40.0, 80.0), LengthConstraintType.RANGE
        );

        Size2D size = this.arrangement.arrangeFR(this.container, null, constraint);
        assertEquals(100.0, size.getWidth(), 0.001);
        assertEquals(50.0, size.getHeight(), 0.001);
    }

    // Tests arrangeFR when arranged height is out of height range and must be constrained
    @Test
    public void testArrangeFR_heightOutOfRange_constrainsHeight() {
        EmptyBlock top = new EmptyBlock(50.0, 20.0);
        EmptyBlock bottom = new EmptyBlock(50.0, 30.0);
        this.arrangement.add(top, RectangleEdge.TOP);
        this.arrangement.add(bottom, RectangleEdge.BOTTOM);

        RectangleConstraint constraint = new RectangleConstraint(
                100.0, null, LengthConstraintType.FIXED,
                0.0, new Range(10.0, 40.0), LengthConstraintType.RANGE
        );

        Size2D size = this.arrangement.arrangeFR(this.container, null, constraint);
        assertEquals(100.0, size.getWidth(), 0.001);
        assertEquals(40.0, size.getHeight(), 0.001);
    }

    // Tests arrangeRR with range constraints on width and height
    @Test
    public void testArrangeRR_rangeConstraints_arrangesCorrectly() {
        EmptyBlock top = new EmptyBlock(50.0, 20.0);
        EmptyBlock bottom = new EmptyBlock(60.0, 30.0);
        EmptyBlock left = new EmptyBlock(20.0, 40.0);
        EmptyBlock right = new EmptyBlock(30.0, 50.0);
        EmptyBlock center = new EmptyBlock(40.0, 30.0);

        this.arrangement.add(top, RectangleEdge.TOP);
        this.arrangement.add(bottom, RectangleEdge.BOTTOM);
        this.arrangement.add(left, RectangleEdge.LEFT);
        this.arrangement.add(right, RectangleEdge.RIGHT);
        this.arrangement.add(center, null);

        Range widthRange = new Range(50.0, 200.0);
        Range heightRange = new Range(50.0, 200.0);

        Size2D size = this.arrangement.arrangeRR(this.container, widthRange, heightRange, null);
        // width = max(50, 60, 20 + 40 + 30) = 90
        // height = 20 + 30 + max(50, 50, 30) = 100
        assertEquals(90.0, size.getWidth(), 0.001);
        assertEquals(100.0, size.getHeight(), 0.001);

        assertEquals(new Rectangle2D.Double(0.0, 0.0, 90.0, 20.0), top.getBounds());
        assertEquals(new Rectangle2D.Double(0.0, 70.0, 90.0, 30.0), bottom.getBounds());
        assertEquals(new Rectangle2D.Double(0.0, 20.0, 20.0, 50.0), left.getBounds());
        assertEquals(new Rectangle2D.Double(60.0, 20.0, 30.0, 50.0), right.getBounds());
        assertEquals(new Rectangle2D.Double(20.0, 20.0, 40.0, 50.0), center.getBounds());
    }

    // Tests arrangeRR when container has no blocks
    @Test
    public void testArrangeRR_noBlocks_returnsZeroSize() {
        Range widthRange = new Range(0.0, 100.0);
        Range heightRange = new Range(0.0, 100.0);

        Size2D size = this.arrangement.arrangeRR(this.container, widthRange, heightRange, null);
        assertEquals(0.0, size.getWidth(), 0.001);
        assertEquals(0.0, size.getHeight(), 0.001);
    }

    // Tests arrange dispatcher with NONE, FIXED, and RANGE length constraints
    @Test
    public void testArrange_dispatchNoneNone_delegatesToArrangeNN() {
        this.arrangement.add(new EmptyBlock(50.0, 50.0), null);
        Size2D size = this.arrangement.arrange(this.container, null, RectangleConstraint.NONE);
        assertEquals(50.0, size.getWidth(), 0.001);
        assertEquals(50.0, size.getHeight(), 0.001);
    }

    // Tests arrange dispatcher with FIXED width and NONE height
    @Test
    public void testArrange_dispatchFixedNone_delegatesToArrangeFN() {
        this.arrangement.add(new EmptyBlock(50.0, 50.0), null);
        RectangleConstraint c = new RectangleConstraint(
                100.0, null, LengthConstraintType.FIXED,
                0.0, null, LengthConstraintType.NONE
        );
        Size2D size = this.arrangement.arrange(this.container, null, c);
        assertEquals(100.0, size.getWidth(), 0.001);
        assertEquals(50.0, size.getHeight(), 0.001);
    }

    // Tests arrange dispatcher with RANGE width and RANGE height
    @Test
    public void testArrange_dispatchRangeRange_delegatesToArrangeRR() {
        this.arrangement.add(new EmptyBlock(50.0, 50.0), null);
        RectangleConstraint c = new RectangleConstraint(
                new Range(10.0, 100.0), new Range(10.0, 100.0)
        );
        Size2D size = this.arrangement.arrange(this.container, null, c);
        assertEquals(50.0, size.getWidth(), 0.001);
        assertEquals(50.0, size.getHeight(), 0.001);
    }

    // Tests arrange dispatcher throwing RuntimeException for unsupported w=NONE, h=FIXED
    @Test(expected = RuntimeException.class)
    public void testArrange_wNoneHFixed_throwsRuntimeException() {
        RectangleConstraint c = new RectangleConstraint(
                0.0, null, LengthConstraintType.NONE,
                100.0, null, LengthConstraintType.FIXED
        );
        this.arrangement.arrange(this.container, null, c);
    }

    // Tests arrange dispatcher throwing RuntimeException for unsupported w=NONE, h=RANGE
    @Test(expected = RuntimeException.class)
    public void testArrange_wNoneHRange_throwsRuntimeException() {
        RectangleConstraint c = new RectangleConstraint(
                0.0, null, LengthConstraintType.NONE,
                0.0, new Range(0.0, 100.0), LengthConstraintType.RANGE
        );
        this.arrangement.arrange(this.container, null, c);
    }

    // Tests arrange dispatcher throwing RuntimeException for unsupported w=RANGE, h=NONE
    @Test(expected = RuntimeException.class)
    public void testArrange_wRangeHNone_throwsRuntimeException() {
        RectangleConstraint c = new RectangleConstraint(
                0.0, new Range(0.0, 100.0), LengthConstraintType.RANGE,
                0.0, null, LengthConstraintType.NONE
        );
        this.arrangement.arrange(this.container, null, c);
    }

    // Tests arrange dispatcher throwing RuntimeException for unsupported w=RANGE, h=FIXED
    @Test(expected = RuntimeException.class)
    public void testArrange_wRangeHFixed_throwsRuntimeException() {
        RectangleConstraint c = new RectangleConstraint(
                0.0, new Range(0.0, 100.0), LengthConstraintType.RANGE,
                100.0, null, LengthConstraintType.FIXED
        );
        this.arrangement.arrange(this.container, null, c);
    }

    // Tests arrangeFF when left block width exceeds fixed total width
    @Test
    public void testArrangeFF_leftBlockExceedsWidth_handlesBounds() {
        EmptyBlock left = new EmptyBlock(150.0, 50.0);
        EmptyBlock right = new EmptyBlock(30.0, 50.0);
        this.arrangement.add(left, RectangleEdge.LEFT);
        this.arrangement.add(right, RectangleEdge.RIGHT);

        RectangleConstraint constraint = new RectangleConstraint(100.0, 100.0);
        Size2D size = this.arrangement.arrangeFF(this.container, null, constraint);
        assertEquals(100.0, size.getWidth(), 0.001);
        assertEquals(100.0, size.getHeight(), 0.001);
    }

    // Tests arrange dispatcher with FIXED width and FIXED height
    @Test
    public void testArrange_dispatchFixedFixed_delegatesToArrangeFF() {
        this.arrangement.add(new EmptyBlock(50.0, 50.0), null);
        RectangleConstraint c = new RectangleConstraint(100.0, 80.0);
        Size2D size = this.arrangement.arrange(this.container, null, c);
        assertEquals(100.0, size.getWidth(), 0.001);
        assertEquals(80.0, size.getHeight(), 0.001);
    }

    // Tests arrange dispatcher with FIXED width and RANGE height
    @Test
    public void testArrange_dispatchFixedRange_delegatesToArrangeFR() {
        this.arrangement.add(new EmptyBlock(50.0, 50.0), null);
        RectangleConstraint c = new RectangleConstraint(
                100.0, null, LengthConstraintType.FIXED,
                0.0, new Range(20.0, 80.0), LengthConstraintType.RANGE
        );
        Size2D size = this.arrangement.arrange(this.container, null, c);
        assertEquals(100.0, size.getWidth(), 0.001);
        assertEquals(50.0, size.getHeight(), 0.001);
    }

    // Tests arrangeFN with no blocks attached
    @Test
    public void testArrangeFN_noBlocks_returnsZeroHeight() {
        Size2D size = this.arrangement.arrangeFN(this.container, null, 100.0);
        assertEquals(100.0, size.getWidth(), 0.001);
        assertEquals(0.0, size.getHeight(), 0.001);
    }

    // Tests arrangeFF with no blocks attached
    @Test
    public void testArrangeFF_noBlocks_returnsConstraintSize() {
        RectangleConstraint constraint = new RectangleConstraint(100.0, 80.0);
        Size2D size = this.arrangement.arrangeFF(this.container, null, constraint);
        assertEquals(100.0, size.getWidth(), 0.001);
        assertEquals(80.0, size.getHeight(), 0.001);
    }

    // Tests serialization of BorderArrangement
    @Test
    public void testSerialization() throws Exception {
        this.arrangement.add(new EmptyBlock(10.0, 10.0), RectangleEdge.TOP);
        this.arrangement.add(new EmptyBlock(20.0, 20.0), RectangleEdge.BOTTOM);
        this.arrangement.add(new EmptyBlock(30.0, 30.0), RectangleEdge.LEFT);
        this.arrangement.add(new EmptyBlock(40.0, 40.0), RectangleEdge.RIGHT);
        this.arrangement.add(new EmptyBlock(50.0, 50.0), null);

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(buffer);
        out.writeObject(this.arrangement);
        out.close();

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        BorderArrangement deserialized = (BorderArrangement) in.readObject();
        in.close();

        assertEquals(this.arrangement, deserialized);
    }

    // Tests arrangeRR with partial blocks (only top and center)
    @Test
    public void testArrangeRR_partialBlocks() {
        EmptyBlock top = new EmptyBlock(40.0, 10.0);
        EmptyBlock center = new EmptyBlock(30.0, 20.0);
        this.arrangement.add(top, RectangleEdge.TOP);
        this.arrangement.add(center, null);

        Range widthRange = new Range(10.0, 100.0);
        Range heightRange = new Range(10.0, 100.0);

        Size2D size = this.arrangement.arrangeRR(this.container, widthRange, heightRange, null);
        assertEquals(40.0, size.getWidth(), 0.001);
        assertEquals(30.0, size.getHeight(), 0.001);
        assertEquals(new Rectangle2D.Double(0.0, 0.0, 40.0, 10.0), top.getBounds());
        assertEquals(new Rectangle2D.Double(0.0, 10.0, 40.0, 20.0), center.getBounds());
    }

    // Tests arrangeNN with only left and right blocks
    @Test
    public void testArrangeNN_leftAndRightOnly() {
        EmptyBlock left = new EmptyBlock(15.0, 40.0);
        EmptyBlock right = new EmptyBlock(25.0, 50.0);
        this.arrangement.add(left, RectangleEdge.LEFT);
        this.arrangement.add(right, RectangleEdge.RIGHT);

        Size2D size = this.arrangement.arrangeNN(this.container, null);
        assertEquals(40.0, size.getWidth(), 0.001);
        assertEquals(50.0, size.getHeight(), 0.001);
    }
}