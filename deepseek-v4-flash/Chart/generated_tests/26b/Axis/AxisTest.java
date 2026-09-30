package org.jfree.chart.axis;

import static org.junit.Assert.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;

import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.event.AxisChangeListener;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.util.RectangleEdge;
import org.jfree.chart.util.RectangleInsets;
import org.junit.Test;

/**
 * JUnit 4 test class for {@link Axis}.  Tests the non-abstract methods
 * using a concrete stub implementation.
 */
public class AxisTest {

    /**
     * A concrete stub subclass of Axis used for testing.
     */
    private static class ConcreteAxis extends Axis {
        protected ConcreteAxis(String label) {
            super(label);
        }

        @Override
        public void configure() {
            // stub
        }

        @Override
        public AxisSpace reserveSpace(Graphics2D g2, Plot plot,
                                      Rectangle2D plotArea,
                                      RectangleEdge edge,
                                      AxisSpace space) {
            return new AxisSpace();
        }

        @Override
        public AxisState draw(Graphics2D g2, double cursor,
                              Rectangle2D plotArea, Rectangle2D dataArea,
                              RectangleEdge edge,
                              PlotRenderingInfo plotState) {
            return new AxisState(cursor);
        }

        @Override
        public List refreshTicks(Graphics2D g2, AxisState state,
                                 Rectangle2D dataArea, RectangleEdge edge) {
            return new java.util.ArrayList();
        }
    }

    /** A helper listener that records the number of events. */
    private int eventCount = 0;
    private AxisChangeListener listener = new AxisChangeListener() {
        @Override
        public void axisChanged(AxisChangeEvent event) {
            eventCount++;
        }
    };

    // ---------- Constructor and defaults ----------

    @Test
    public void testConstructor_defaultValues() {
        Axis axis = new ConcreteAxis("Test");
        assertTrue("visible should be true", axis.isVisible());
        assertEquals("label", "Test", axis.getLabel());
        assertEquals("labelFont", Axis.DEFAULT_AXIS_LABEL_FONT, axis.getLabelFont());
        assertEquals("labelPaint", Axis.DEFAULT_AXIS_LABEL_PAINT, axis.getLabelPaint());
        assertEquals("labelInsets", Axis.DEFAULT_AXIS_LABEL_INSETS, axis.getLabelInsets());
        assertEquals("labelAngle", 0.0, axis.getLabelAngle(), 0.0001);
        assertNull("labelToolTip", axis.getLabelToolTip());
        assertNull("labelURL", axis.getLabelURL());
        assertTrue("axisLineVisible", axis.isAxisLineVisible());
        assertEquals("axisLinePaint", Axis.DEFAULT_AXIS_LINE_PAINT, axis.getAxisLinePaint());
        assertEquals("axisLineStroke", Axis.DEFAULT_AXIS_LINE_STROKE, axis.getAxisLineStroke());
        assertTrue("tickLabelsVisible", axis.isTickLabelsVisible());
        assertEquals("tickLabelFont", Axis.DEFAULT_TICK_LABEL_FONT, axis.getTickLabelFont());
        assertEquals("tickLabelPaint", Axis.DEFAULT_TICK_LABEL_PAINT, axis.getTickLabelPaint());
        assertEquals("tickLabelInsets", Axis.DEFAULT_TICK_LABEL_INSETS, axis.getTickLabelInsets());
        assertTrue("tickMarksVisible", axis.isTickMarksVisible());
        assertEquals("tickMarkInsideLength", Axis.DEFAULT_TICK_MARK_INSIDE_LENGTH, axis.getTickMarkInsideLength(), 0.0f);
        assertEquals("tickMarkOutsideLength", Axis.DEFAULT_TICK_MARK_OUTSIDE_LENGTH, axis.getTickMarkOutsideLength(), 0.0f);
        assertEquals("tickMarkStroke", Axis.DEFAULT_TICK_MARK_STROKE, axis.getTickMarkStroke());
        assertEquals("tickMarkPaint", Axis.DEFAULT_TICK_MARK_PAINT, axis.getTickMarkPaint());
        assertEquals("fixedDimension", 0.0, axis.getFixedDimension(), 0.0);
        assertNull("plot", axis.getPlot());
    }

    // ---------- setVisible / isVisible ----------

    @Test
    public void testSetVisible_trueToFalse_firesEvent() {
        Axis axis = new ConcreteAxis("X");
        axis.addChangeListener(listener);
        eventCount = 0;
        axis.setVisible(false);
        assertFalse(axis.isVisible());
        assertEquals("event fired", 1, eventCount);
    }

    @Test
    public void testSetVisible_sameValue_noEvent() {
        Axis axis = new ConcreteAxis("X");
        axis.setVisible(true);
        axis.addChangeListener(listener);
        eventCount = 0;
        axis.setVisible(true);
        assertEquals("no event", 0, eventCount);
    }

    // ---------- setLabel ----------

    @Test
    public void testSetLabel_nullToNonNull_firesEvent() {
        Axis axis = new ConcreteAxis(null);
        axis.addChangeListener(listener);
        eventCount = 0;
        axis.setLabel("NewLabel");
        assertEquals("label", "NewLabel", axis.getLabel());
        assertEquals("event fired", 1, eventCount);
    }

    @Test
    public void testSetLabel_nonNullToNull_firesEvent() {
        Axis axis = new ConcreteAxis("Old");
        axis.addChangeListener(listener);
        eventCount = 0;
        axis.setLabel(null);
        assertNull("label", axis.getLabel());
        assertEquals("event fired", 1, eventCount);
    }

    @Test
    public void testSetLabel_sameString_noEvent() {
        Axis axis = new ConcreteAxis("Same");
        axis.addChangeListener(listener);
        eventCount = 0;
        axis.setLabel("Same");
        assertEquals("label unchanged", "Same", axis.getLabel());
        assertEquals("no event", 0, eventCount);
    }

    @Test
    public void testSetLabel_nullToNull_noEvent() {
        Axis axis = new ConcreteAxis(null);
        axis.addChangeListener(listener);
        eventCount = 0;
        axis.setLabel(null);
        assertNull("label still null", axis.getLabel());
        assertEquals("no event", 0, eventCount);
    }

    // ---------- setLabelFont ----------

    @Test(expected = IllegalArgumentException.class)
    public void testSetLabelFont_null_throws() {
        Axis axis = new ConcreteAxis("X");
        axis.setLabelFont(null);
    }

    @Test
    public void testSetLabelFont_differentFont_firesEvent() {
        Axis axis = new ConcreteAxis("X");
        Font newFont = new Font("Serif", Font.BOLD, 14);
        axis.addChangeListener(listener);
        eventCount = 0;
        axis.setLabelFont(newFont);
        assertEquals("font", newFont, axis.getLabelFont());
        assertEquals("event fired", 1, eventCount);
    }

    @Test
    public void testSetLabelFont_sameFont_noEvent() {
        Axis axis = new ConcreteAxis("X");
        Font same = axis.getLabelFont();
        axis.addChangeListener(listener);
        eventCount = 0;
        axis.setLabelFont(same);
        assertEquals("no event", 0, eventCount);
    }

    // ---------- setLabelPaint ----------

    @Test(expected = IllegalArgumentException.class)
    public void testSetLabelPaint_null_throws() {
        Axis axis = new ConcreteAxis("X");
        axis.setLabelPaint(null);
    }

    @Test
    public void testSetLabelPaint_differentPaint_firesEvent() {
        Axis axis = new ConcreteAxis("X");
        Paint newPaint = Color.RED;
        axis.addChangeListener(listener);
        eventCount = 0;
        axis.setLabelPaint(newPaint);
        assertEquals("paint", newPaint, axis.getLabelPaint());
        assertEquals("event fired", 1, eventCount);
    }

    @Test
    public void testSetLabelPaint_samePaint_noEvent() {
        Axis axis = new ConcreteAxis("X");
        Paint same = axis.getLabelPaint();
        axis.addChangeListener(listener);
        eventCount = 0;
        axis.setLabelPaint(same);
        assertEquals("no event", 0, eventCount);
    }

    // ---------- setLabelInsets ----------

    @Test(expected = IllegalArgumentException.class)
    public void testSetLabelInsets_null_throws() {
        Axis axis = new ConcreteAxis("X");
        axis.setLabelInsets(null);
    }

    @Test
    public void testSetLabelInsets_differentInsets_firesEvent() {
        Axis axis = new ConcreteAxis("X");
        RectangleInsets newInsets = new RectangleInsets(1,2,3,4);
        axis.addChangeListener(listener);
        eventCount = 0;
        axis.setLabelInsets(newInsets);
        assertEquals("insets", newInsets, axis.getLabelInsets());
        assertEquals("event fired", 1, eventCount);
    }

    @Test
    public void testSetLabelInsets_sameInsets_noEvent() {
        Axis axis = new ConcreteAxis("X");
        RectangleInsets same = axis.getLabelInsets();
        axis.addChangeListener(listener);
        eventCount = 0;
        axis.setLabelInsets(same);
        assertEquals("no event", 0, eventCount);
    }

    // ---------- setLabelToolTip ----------

    @Test
    public void testSetLabelToolTip_nullToNonNull_firesEvent() {
        Axis axis = new ConcreteAxis("X");
        axis.addChangeListener(listener);
        eventCount = 0;
        axis.setLabelToolTip("tooltip");
        assertEquals("tooltip", "tooltip", axis.getLabelToolTip());
        assertEquals("event fired", 1, eventCount);
    }

    @Test
    public void testSetLabelToolTip_nonNullToNull_firesEvent() {
        Axis axis = new ConcreteAxis("X");
        axis.setLabelToolTip("tooltip");
        axis.addChangeListener(listener);
        eventCount = 0;
        axis.setLabelToolTip(null);
        assertNull("tooltip", axis.getLabelToolTip());
        assertEquals("event fired", 1, eventCount);
    }

    @Test
    public void testSetLabelToolTip_sameValue_noEvent() {
        Axis axis = new ConcreteAxis("X");
        axis.setLabelToolTip("tooltip");
        axis.addChangeListener(listener);
        eventCount = 0;
        axis.setLabelToolTip("tooltip");
        assertEquals("no event", 0, eventCount);
    }

    // ---------- setLabelURL ----------

    @Test
    public void testSetLabelURL_nullToNonNull_firesEvent() {
        Axis axis = new ConcreteAxis("X");
        axis.addChangeListener(listener);
        eventCount = 0;
        axis.setLabelURL("http://example.com");
        assertEquals("url", "http://example.com", axis.getLabelURL());
        assertEquals("event fired", 1, eventCount);
    }

    @Test
    public void testSetLabelURL_nonNullToNull_firesEvent() {
        Axis axis = new ConcreteAxis("X");
        axis.setLabelURL("http://example.com");
        axis.addChangeListener(listener);
        eventCount = 0;
        axis.setLabelURL(null);
        assertNull("url", axis.getLabelURL());
        assertEquals("event fired", 1, eventCount);
    }

    @Test
    public void testSetLabelURL_sameValue_noEvent() {
        Axis axis = new ConcreteAxis("X");
        axis.setLabelURL("http://example.com");
        axis.addChangeListener(listener);
        eventCount = 0;
        axis.setLabelURL("http://example.com");
        assertEquals("url", "http://example.com", axis.getLabelURL());
        assertEquals("no event", 0, eventCount);
    }

    // ---------- setAxisLineVisible ----------

    @Test
    public void testSetAxisLineVisible_change_firesEvent() {
        Axis axis = new ConcreteAxis("X");
        axis.addChangeListener(listener);
        eventCount = 0;
        axis.setAxisLineVisible(false);
        assertFalse(axis.isAxisLineVisible());
        assertEquals("event fired", 1, eventCount);
    }

    @Test
    public void testSetAxisLineVisible_sameValue_noEvent() {
        Axis axis = new ConcreteAxis("X");
        axis.setAxisLineVisible(true);
        axis.addChangeListener(listener);
        eventCount = 0;
        axis.setAxisLineVisible(true);
        assertEquals("no event", 0, eventCount);
    }

    // ---------- setAxisLinePaint ----------

    @Test
    public void testSetAxisLinePaint_differentPaint_firesEvent() {
        Axis axis = new ConcreteAxis("X");
        axis.addChangeListener(listener);
        eventCount = 0;
        Paint newPaint = Color.BLUE;
        axis.setAxisLinePaint(newPaint);
        assertEquals("paint", newPaint, axis.getAxisLinePaint());
        assertEquals("event fired", 1, eventCount);
    }

    @Test
    public void testSetAxisLinePaint_samePaint_noEvent() {
        Axis axis = new ConcreteAxis("X");
        Paint same = axis.getAxisLinePaint();
        axis.addChangeListener(listener);
        eventCount = 0;
        axis.setAxisLinePaint(same);
        assertEquals("no event", 0, eventCount);
    }

    // ---------- setAxisLineStroke ----------

    @Test(expected = IllegalArgumentException.class)
    public void testSetAxisLineStroke_null_throws() {
        Axis axis = new ConcreteAxis("X");
        axis.setAxisLineStroke(null);
    }

    @Test
    public void testSetAxisLineStroke_differentStroke_firesEvent() {
        Axis axis = new ConcreteAxis("X");
        axis.addChangeListener(listener);
        eventCount = 0;
        Stroke newStroke = new BasicStroke(2.0f);
        axis.setAxisLineStroke(newStroke);
        assertEquals("stroke", newStroke, axis.getAxisLineStroke());
        assertEquals("event fired", 1, eventCount);
    }

    @Test
    public void testSetAxisLineStroke_sameStroke_noEvent() {
        Axis axis = new ConcreteAxis("X");
        Stroke same = axis.getAxisLineStroke();
        axis.addChangeListener(listener);
        eventCount = 0;
        axis.setAxisLineStroke(same);
        assertEquals("no event", 0, eventCount);
    }

    // ---------- setTickLabelsVisible ----------

    @Test
    public void testSetTickLabelsVisible_change_firesEvent() {
        Axis axis = new ConcreteAxis("X");
        axis.addChangeListener(listener);
        eventCount = 0;
        axis.setTickLabelsVisible(false);
        assertFalse(axis.isTickLabelsVisible());
        assertEquals("event fired", 1, eventCount);
    }

    @Test
    public void testSetTickLabelsVisible_sameValue_noEvent() {
        Axis axis = new ConcreteAxis("X");
        axis.setTickLabelsVisible(true);
        axis.addChangeListener(listener);
        eventCount = 0;
        axis.setTickLabelsVisible(true);
        assertEquals("no event", 0, eventCount);
    }

    // ---------- setTickLabelFont ----------

    @Test(expected = IllegalArgumentException.class)
    public void testSetTickLabelFont_null_throws() {
        Axis axis = new ConcreteAxis("X");
        axis.setTickLabelFont(null);
    }

    @Test
    public void testSetTickLabelFont_differentFont_firesEvent() {
        Axis axis = new ConcreteAxis("X");
        axis.addChangeListener(listener);
        eventCount = 0;
        Font newFont = new Font("Monospaced", Font.ITALIC, 10);
        axis.setTickLabelFont(newFont);
        assertEquals("font", newFont, axis.getTickLabelFont());
        assertEquals("event fired", 1, eventCount);
    }

    @Test
    public void testSetTickLabelFont_sameFont_noEvent() {
        Axis axis = new ConcreteAxis("X");
        Font same = axis.getTickLabelFont();
        axis.addChangeListener(listener);
        eventCount = 0;
        axis.setTickLabelFont(same);
        assertEquals("no event", 0, eventCount);
    }

    // ---------- setTickLabelPaint ----------

    @Test
    public void testSetTickLabelPaint_differentPaint_firesEvent() {
        Axis axis = new ConcreteAxis("X");
        axis.addChangeListener(listener);
        eventCount = 0;
        Paint newPaint = Color.GREEN;
        axis.setTickLabelPaint(newPaint);
        assertEquals("paint", newPaint, axis.getTickLabelPaint());
        assertEquals("event fired", 1, eventCount);
    }

    @Test
    public void testSetTickLabelPaint_samePaint_noEvent() {
        Axis axis = new ConcreteAxis("X");
        Paint same = axis.getTickLabelPaint();
        axis.addChangeListener(listener);
        eventCount = 0;
        axis.setTickLabelPaint(same);
        assertEquals("no event", 0, eventCount);
    }

    // ---------- setTickLabelInsets ----------

    @Test
    public void testSetTickLabelInsets_differentInsets_firesEvent() {
        Axis axis = new ConcreteAxis("X");
        axis.addChangeListener(listener);
        eventCount = 0;
        RectangleInsets newInsets = new RectangleInsets(2,3,4,5);
        axis.setTickLabelInsets(newInsets);
        assertEquals("insets", newInsets, axis.getTickLabelInsets());
        assertEquals("event fired", 1, eventCount);
    }

    @Test
    public void testSetTickLabelInsets_sameInsets_noEvent() {
        Axis axis = new ConcreteAxis("X");
        RectangleInsets same = axis.getTickLabelInsets();
        axis.addChangeListener(listener);
        eventCount = 0;
        axis.setTickLabelInsets(same);
        assertEquals("no event", 0, eventCount);
    }

    // ---------- setTickMarksVisible ----------

    @Test
    public void testSetTickMarksVisible_change_firesEvent() {
        Axis axis = new ConcreteAxis("X");
        axis.addChangeListener(listener);
        eventCount = 0;
        axis.setTickMarksVisible(false);
        assertFalse(axis.isTickMarksVisible());
        assertEquals("event fired", 1, eventCount);
    }

    @Test
    public void testSetTickMarksVisible_sameValue_noEvent() {
        Axis axis = new ConcreteAxis("X");
        axis.setTickMarksVisible(true);
        axis.addChangeListener(listener);
        eventCount = 0;
        axis.setTickMarksVisible(true);
        assertEquals("no event", 0, eventCount);
    }

    // ---------- setTickMarkInsideLength ----------

    @Test
    public void testSetTickMarkInsideLength_change_firesEvent() {
        Axis axis = new ConcreteAxis("X");
        axis.addChangeListener(listener);
        eventCount = 0;
        axis.setTickMarkInsideLength(5.0f);
        assertEquals(5.0f, axis.getTickMarkInsideLength(), 0.0f);
        assertEquals("event fired", 1, eventCount);
    }

    @Test
    public void testSetTickMarkInsideLength_sameValue_noEvent() {
        Axis axis = new ConcreteAxis("X");
        float original = axis.getTickMarkInsideLength();
        axis.addChangeListener(listener);
        eventCount = 0;
        axis.setTickMarkInsideLength(original);
        assertEquals("no event", 0, eventCount);
    }

    // ---------- setTickMarkOutsideLength ----------

    @Test
    public void testSetTickMarkOutsideLength_change_firesEvent() {
        Axis axis = new ConcreteAxis("X");
        axis.addChangeListener(listener);
        eventCount = 0;
        axis.setTickMarkOutsideLength(8.0f);
        assertEquals(8.0f, axis.getTickMarkOutsideLength(), 0.0f);
        assertEquals("event fired", 1, eventCount);
    }

    @Test
    public void testSetTickMarkOutsideLength_sameValue_noEvent() {
        Axis axis = new ConcreteAxis("X");
        float original = axis.getTickMarkOutsideLength();
        axis.addChangeListener(listener);
        eventCount = 0;
        axis.setTickMarkOutsideLength(original);
        assertEquals("no event", 0, eventCount);
    }

    // ---------- setTickMarkStroke ----------

    @Test(expected = IllegalArgumentException.class)
    public void testSetTickMarkStroke_null_throws() {
        Axis axis = new ConcreteAxis("X");
        axis.setTickMarkStroke(null);
    }

    @Test
    public void testSetTickMarkStroke_differentStroke_firesEvent() {
        Axis axis = new ConcreteAxis("X");
        axis.addChangeListener(listener);
        eventCount = 0;
        Stroke newStroke = new BasicStroke(3.0f);
        axis.setTickMarkStroke(newStroke);
        assertEquals("stroke", newStroke, axis.getTickMarkStroke());
        assertEquals("event fired", 1, eventCount);
    }

    @Test
    public void testSetTickMarkStroke_sameStroke_noEvent() {
        Axis axis = new ConcreteAxis("X");
        Stroke same = axis.getTickMarkStroke();
        axis.addChangeListener(listener);
        eventCount = 0;
        axis.setTickMarkStroke(same);
        assertEquals("no event", 0, eventCount);
    }

    // ---------- setTickMarkPaint ----------

    @Test
    public void testSetTickMarkPaint_differentPaint_firesEvent() {
        Axis axis = new ConcreteAxis("X");
        axis.addChangeListener(listener);
        eventCount = 0;
        Paint newPaint = Color.MAGENTA;
        axis.setTickMarkPaint(newPaint);
        assertEquals("paint", newPaint, axis.getTickMarkPaint());
        assertEquals("event fired", 1, eventCount);
    }

    @Test
    public void testSetTickMarkPaint_samePaint_noEvent() {
        Axis axis = new ConcreteAxis("X");
        Paint same = axis.getTickMarkPaint();
        axis.addChangeListener(listener);
        eventCount = 0;
        axis.setTickMarkPaint(same);
        assertEquals("no event", 0, eventCount);
    }

    // ---------- setFixedDimension ----------

    @Test
    public void testSetFixedDimension_change_firesEvent() {
        Axis axis = new ConcreteAxis("X");
        axis.addChangeListener(listener);
        eventCount = 0;
        axis.setFixedDimension(50.0);
        assertEquals(50.0, axis.getFixedDimension(), 0.0);
        assertEquals("event fired", 1, eventCount);
    }

    @Test
    public void testSetFixedDimension_sameValue_noEvent() {
        Axis axis = new ConcreteAxis("X");
        double original = axis.getFixedDimension();
        axis.addChangeListener(listener);
        eventCount = 0;
        axis.setFixedDimension(original);
        assertEquals("no event", 0, eventCount);
    }

    // ---------- setPlot ----------

    @Test
    public void testSetPlot_null_doesNotThrow() {
        Axis axis = new ConcreteAxis("X");
        axis.setPlot(null); // should not throw
        assertNull("plot", axis.getPlot());
    }

    // ---------- addChangeListener / removeChangeListener / hasListener ----------

    @Test
    public void testHasListener_registeredListener_returnsTrue() {
        Axis axis = new ConcreteAxis("X");
        axis.addChangeListener(listener);
        assertTrue("hasListener", axis.hasListener(listener));
    }

    @Test
    public void testRemoveChangeListener_removesListener() {
        Axis axis = new ConcreteAxis("X");
        axis.addChangeListener(listener);
        axis.removeChangeListener(listener);
        assertFalse("hasListener after removal", axis.hasListener(listener));
    }

    // ---------- getLabelEnclosure ----------

    @Test
    public void testGetLabelEnclosure_nonNullLabel_topEdge_returnsNonEmpty() {
        Axis axis = new ConcreteAxis("Label");
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        Rectangle2D enclosure = axis.getLabelEnclosure(g2, RectangleEdge.TOP);
        assertNotNull("enclosure", enclosure);
        assertTrue("width > 0", enclosure.getWidth() > 0);
        assertTrue("height > 0", enclosure.getHeight() > 0);
        g2.dispose();
    }

    @Test
    public void testGetLabelEnclosure_emptyLabel_returnsEmpty() {
        Axis axis = new ConcreteAxis("");
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        Rectangle2D enclosure = axis.getLabelEnclosure(g2, RectangleEdge.TOP);
        assertNotNull("enclosure", enclosure);
        assertEquals("width 0", 0.0, enclosure.getWidth(), 0.0);
        assertEquals("height 0", 0.0, enclosure.getHeight(), 0.0);
        g2.dispose();
    }

    @Test
    public void testGetLabelEnclosure_nullLabel_returnsEmpty() {
        Axis axis = new ConcreteAxis(null);
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        Rectangle2D enclosure = axis.getLabelEnclosure(g2, RectangleEdge.TOP);
        assertNotNull("enclosure", enclosure);
        assertEquals("width 0", 0.0, enclosure.getWidth(), 0.0);
        assertEquals("height 0", 0.0, enclosure.getHeight(), 0.0);
        g2.dispose();
    }

    @Test
    public void testGetLabelEnclosure_leftEdge_returnsNonEmpty() {
        Axis axis = new ConcreteAxis("Label");
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        Rectangle2D enclosure = axis.getLabelEnclosure(g2, RectangleEdge.LEFT);
        assertNotNull("enclosure", enclosure);
        assertTrue("width > 0", enclosure.getWidth() > 0);
        assertTrue("height > 0", enclosure.getHeight() > 0);
        g2.dispose();
    }

    @Test
    public void testGetLabelEnclosure_rightEdge_returnsNonEmpty() {
        Axis axis = new ConcreteAxis("Label");
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        Rectangle2D enclosure = axis.getLabelEnclosure(g2, RectangleEdge.RIGHT);
        assertNotNull("enclosure", enclosure);
        assertTrue("width > 0", enclosure.getWidth() > 0);
        assertTrue("height > 0", enclosure.getHeight() > 0);
        g2.dispose();
    }

    @Test
    public void testGetLabelEnclosure_bottomEdge_returnsNonEmpty() {
        Axis axis = new ConcreteAxis("Label");
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        Rectangle2D enclosure = axis.getLabelEnclosure(g2, RectangleEdge.BOTTOM);
        assertNotNull("enclosure", enclosure);
        assertTrue("width > 0", enclosure.getWidth() > 0);
        assertTrue("height > 0", enclosure.getHeight() > 0);
        g2.dispose();
    }

    // ---------- drawLabel ----------

    @Test(expected = IllegalArgumentException.class)
    public void testDrawLabel_nullState_throws() {
        Axis axis = new ConcreteAxis("X");
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        Rectangle2D plotArea = new Rectangle2D.Double(0,0,100,100);
        Rectangle2D dataArea = new Rectangle2D.Double(10,10,80,80);
        axis.drawLabel("Test", g2, plotArea, dataArea, RectangleEdge.TOP, null, null);
        g2.dispose();
    }

    @Test
    public void testDrawLabel_nullLabel_returnsState() {
        Axis axis = new ConcreteAxis("X");
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        Rectangle2D plotArea = new Rectangle2D.Double(0,0,100,100);
        Rectangle2D dataArea = new Rectangle2D.Double(10,10,80,80);
        AxisState state = new AxisState(50);
        AxisState result = axis.drawLabel(null, g2, plotArea, dataArea, RectangleEdge.TOP, state, null);
        assertSame("same state", state, result);
        g2.dispose();
    }

    @Test
    public void testDrawLabel_topEdge_changesCursor() {
        Axis axis = new ConcreteAxis("X");
        axis.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        BufferedImage image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        Rectangle2D plotArea = new Rectangle2D.Double(0,0,200,200);
        Rectangle2D dataArea = new Rectangle2D.Double(20,20,160,160);
        AxisState state = new AxisState(50);
        AxisState result = axis.drawLabel("TestLabel", g2, plotArea, dataArea, RectangleEdge.TOP, state, null);
        assertNotNull("result", result);
        assertTrue("cursor decreased", result.getCursor() < 50);
        g2.dispose();
    }

    @Test
    public void testDrawLabel_bottomEdge_changesCursor() {
        Axis axis = new ConcreteAxis("X");
        axis.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        BufferedImage image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        Rectangle2D plotArea = new Rectangle2D.Double(0,0,200,200);
        Rectangle2D dataArea = new Rectangle2D.Double(20,20,160,160);
        AxisState state = new AxisState(50);
        AxisState result = axis.drawLabel("TestLabel", g2, plotArea, dataArea, RectangleEdge.BOTTOM, state, null);
        assertNotNull("result", result);
        assertTrue("cursor changed", result.getCursor() != 50);
        g2.dispose();
    }

    @Test
    public void testDrawLabel_leftEdge_changesCursor() {
        Axis axis = new ConcreteAxis("X");
        axis.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        BufferedImage image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        Rectangle2D plotArea = new Rectangle2D.Double(0,0,200,200);
        Rectangle2D dataArea = new Rectangle2D.Double(20,20,160,160);
        AxisState state = new AxisState(50);
        AxisState result = axis.drawLabel("TestLabel", g2, plotArea, dataArea, RectangleEdge.LEFT, state, null);
        assertNotNull("result", result);
        assertTrue("cursor changed", result.getCursor() != 50);
        g2.dispose();
    }

    @Test
    public void testDrawLabel_rightEdge_changesCursor() {
        Axis axis = new ConcreteAxis("X");
        axis.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        BufferedImage image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        Rectangle2D plotArea = new Rectangle2D.Double(0,0,200,200);
        Rectangle2D dataArea = new Rectangle2D.Double(20,20,160,160);
        AxisState state = new AxisState(50);
        AxisState result = axis.drawLabel("TestLabel", g2, plotArea, dataArea, RectangleEdge.RIGHT, state, null);
        assertNotNull("result", result);
        assertTrue("cursor changed", result.getCursor() != 50);
        g2.dispose();
    }

    // ---------- drawAxisLine ----------

    @Test
    public void testDrawAxisLine_topEdge_noException() {
        Axis axis = new ConcreteAxis("X");
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        Rectangle2D dataArea = new Rectangle2D.Double(10,10,80,80);
        axis.drawAxisLine(g2, 50, dataArea, RectangleEdge.TOP);
        g2.dispose();
    }

    @Test
    public void testDrawAxisLine_bottomEdge_noException() {
        Axis axis = new ConcreteAxis("X");
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        Rectangle2D dataArea = new Rectangle2D.Double(10,10,80,80);
        axis.drawAxisLine(g2, 50, dataArea, RectangleEdge.BOTTOM);
        g2.dispose();
    }

    @Test
    public void testDrawAxisLine_leftEdge_noException() {
        Axis axis = new ConcreteAxis("X");
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        Rectangle2D dataArea = new Rectangle2D.Double(10,10,80,80);
        axis.drawAxisLine(g2, 50, dataArea, RectangleEdge.LEFT);
        g2.dispose();
    }

    @Test
    public void testDrawAxisLine_rightEdge_noException() {
        Axis axis = new ConcreteAxis("X");
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        Rectangle2D dataArea = new Rectangle2D.Double(10,10,80,80);
        axis.drawAxisLine(g2, 50, dataArea, RectangleEdge.RIGHT);
        g2.dispose();
    }

    // ---------- equals ----------

    @Test
    public void testEquals_sameInstance_returnsTrue() {
        Axis axis = new ConcreteAxis("X");
        assertTrue(axis.equals(axis));
    }

    @Test
    public void testEquals_differentClass_returnsFalse() {
        Axis axis = new ConcreteAxis("X");
        assertFalse(axis.equals("not an axis"));
    }

    @Test
    public void testEquals_equalAxes_returnsTrue() {
        Axis a1 = new ConcreteAxis("Label");
        Axis a2 = new ConcreteAxis("Label");
        assertTrue(a1.equals(a2));
    }

    @Test
    public void testEquals_differentLabel_returnsFalse() {
        Axis a1 = new ConcreteAxis("A");
        Axis a2 = new ConcreteAxis("B");
        assertFalse(a1.equals(a2));
    }

    @Test
    public void testEquals_differentVisible_returnsFalse() {
        Axis a1 = new ConcreteAxis("X");
        a1.setVisible(false);
        Axis a2 = new ConcreteAxis("X");
        assertFalse(a1.equals(a2));
    }

    @Test
    public void testEquals_differentLabelFont_returnsFalse() {
        Axis a1 = new ConcreteAxis("X");
        Axis a2 = new ConcreteAxis("X");
        a2.setLabelFont(new Font("Serif", Font.PLAIN, 20));
        assertFalse(a1.equals(a2));
    }

    @Test
    public void testEquals_differentLabelPaint_returnsFalse() {
        Axis a1 = new ConcreteAxis("X");
        Axis a2 = new ConcreteAxis("X");
        a2.setLabelPaint(Color.RED);
        assertFalse(a1.equals(a2));
    }

    @Test
    public void testEquals_differentLabelInsets_returnsFalse() {
        Axis a1 = new ConcreteAxis("X");
        Axis a2 = new ConcreteAxis("X");
        a2.setLabelInsets(new RectangleInsets(1,2,3,4));
        assertFalse(a1.equals(a2));
    }

    @Test
    public void testEquals_differentLabelAngle_returnsFalse() {
        Axis a1 = new ConcreteAxis("X");
        Axis a2 = new ConcreteAxis("X");
        a2.setLabelAngle(0.5);
        assertFalse(a1.equals(a2));
    }

    @Test
    public void testEquals_differentLabelToolTip_returnsFalse() {
        Axis a1 = new ConcreteAxis("X");
        Axis a2 = new ConcreteAxis("X");
        a2.setLabelToolTip("tooltip");
        assertFalse(a1.equals(a2));
    }

    @Test
    public void testEquals_differentLabelURL_returnsFalse() {
        Axis a1 = new ConcreteAxis("X");
        Axis a2 = new ConcreteAxis("X");
        a2.setLabelURL("url");
        assertFalse(a1.equals(a2));
    }

    @Test
    public void testEquals_differentAxisLineVisible_returnsFalse() {
        Axis a1 = new ConcreteAxis("X");
        Axis a2 = new ConcreteAxis("X");
        a2.setAxisLineVisible(false);
        assertFalse(a1.equals(a2));
    }

    @Test
    public void testEquals_differentAxisLinePaint_returnsFalse() {
        Axis a1 = new ConcreteAxis("X");
        Axis a2 = new ConcreteAxis("X");
        a2.setAxisLinePaint(Color.RED);
        assertFalse(a1.equals(a2));
    }

    @Test
    public void testEquals_differentAxisLineStroke_returnsFalse() {
        Axis a1 = new ConcreteAxis("X");
        Axis a2 = new ConcreteAxis("X");
        a2.setAxisLineStroke(new BasicStroke(2.0f));
        assertFalse(a1.equals(a2));
    }

    @Test
    public void testEquals_differentTickLabelsVisible_returnsFalse() {
        Axis a1 = new ConcreteAxis("X");
        Axis a2 = new ConcreteAxis("X");
        a2.setTickLabelsVisible(false);
        assertFalse(a1.equals(a2));
    }

    @Test
    public void testEquals_differentTickLabelFont_returnsFalse() {
        Axis a1 = new ConcreteAxis("X");
        Axis a2 = new ConcreteAxis("X");
        a2.setTickLabelFont(new Font("Monospaced", Font.BOLD, 9));
        assertFalse(a1.equals(a2));
    }

    @Test
    public void testEquals_differentTickLabelPaint_returnsFalse() {
        Axis a1 = new ConcreteAxis("X");
        Axis a2 = new ConcreteAxis("X");
        a2.setTickLabelPaint(Color.RED);
        assertFalse(a1.equals(a2));
    }

    @Test
    public void testEquals_differentTickLabelInsets_returnsFalse() {
        Axis a1 = new ConcreteAxis("X");
        Axis a2 = new ConcreteAxis("X");
        a2.setTickLabelInsets(new RectangleInsets(1,2,3,4));
        assertFalse(a1.equals(a2));
    }

    @Test
    public void testEquals_differentTickMarksVisible_returnsFalse() {
        Axis a1 = new ConcreteAxis("X");
        Axis a2 = new ConcreteAxis("X");
        a2.setTickMarksVisible(false);
        assertFalse(a1.equals(a2));
    }

    @Test
    public void testEquals_differentTickMarkInsideLength_returnsFalse() {
        Axis a1 = new ConcreteAxis("X");
        Axis a2 = new ConcreteAxis("X");
        a2.setTickMarkInsideLength(5.0f);
        assertFalse(a1.equals(a2));
    }

    @Test
    public void testEquals_differentTickMarkOutsideLength_returnsFalse() {
        Axis a1 = new ConcreteAxis("X");
        Axis a2 = new ConcreteAxis("X");
        a2.setTickMarkOutsideLength(5.0f);
        assertFalse(a1.equals(a2));
    }

    @Test
    public void testEquals_differentTickMarkStroke_returnsFalse() {
        Axis a1 = new ConcreteAxis("X");
        Axis a2 = new ConcreteAxis("X");
        a2.setTickMarkStroke(new BasicStroke(2.0f));
        assertFalse(a1.equals(a2));
    }

    @Test
    public void testEquals_differentTickMarkPaint_returnsFalse() {
        Axis a1 = new ConcreteAxis("X");
        Axis a2 = new ConcreteAxis("X");
        a2.setTickMarkPaint(Color.RED);
        assertFalse(a1.equals(a2));
    }

    @Test
    public void testEquals_differentFixedDimension_returnsFalse() {
        Axis a1 = new ConcreteAxis("X");
        Axis a2 = new ConcreteAxis("X");
        a2.setFixedDimension(100.0);
        assertFalse(a1.equals(a2));
    }

    // ---------- clone ----------

    @Test
    public void testClone_basicProperties_cloned() throws CloneNotSupportedException {
        Axis axis = new ConcreteAxis("Test");
        axis.setLabelAngle(0.5);
        axis.setFixedDimension(100.0);
        Axis cloned = (Axis) axis.clone();
        assertNotNull("cloned", cloned);
        assertEquals("label", axis.getLabel(), cloned.getLabel());
        assertEquals("labelAngle", axis.getLabelAngle(), cloned.getLabelAngle(), 0.0001);
        assertEquals("fixedDimension", axis.getFixedDimension(), cloned.getFixedDimension(), 0.0);
        assertNull("plot should be null", cloned.getPlot());
        assertFalse("listenerList different", axis.hasListener(listener)); // not registered
    }
}