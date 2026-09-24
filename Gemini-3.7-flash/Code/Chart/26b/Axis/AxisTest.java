package org.jfree.chart.axis;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.event.AxisChangeListener;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.util.RectangleEdge;
import org.jfree.chart.util.RectangleInsets;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class AxisTest {

    // Concrete subclass of Axis for testing abstract Axis behavior
    private static class TestAxis extends Axis {
        public TestAxis(String label) {
            super(label);
        }

        public void configure() {
        }

        public AxisSpace reserveSpace(Graphics2D g2, Plot plot,
                                      Rectangle2D plotArea, RectangleEdge edge,
                                      AxisSpace space) {
            return new AxisSpace();
        }

        public AxisState draw(Graphics2D g2, double cursor,
                              Rectangle2D plotArea, Rectangle2D dataArea,
                              RectangleEdge edge, PlotRenderingInfo plotState) {
            AxisState state = new AxisState(cursor);
            if (isVisible()) {
                state = drawLabel(getLabel(), g2, plotArea, dataArea, edge, state, plotState);
            }
            return state;
        }

        public List refreshTicks(Graphics2D g2, AxisState state,
                                 Rectangle2D dataArea, RectangleEdge edge) {
            return new java.util.ArrayList();
        }
    }

    private static class TestAxisListener implements AxisChangeListener {
        private boolean notified = false;

        public void axisChanged(AxisChangeEvent event) {
            this.notified = true;
        }

        public boolean isNotified() {
            return this.notified;
        }
    }

    private TestAxis axis;

    @Before
    public void setUp() {
        this.axis = new TestAxis("Test Label");
    }

    // Tests default constructor initialization and getters
    @Test
    public void testConstructor_defaultValues_initializedCorrectly() {
        assertEquals("Test Label", this.axis.getLabel());
        assertTrue(this.axis.isVisible());
        assertEquals(Axis.DEFAULT_AXIS_LABEL_FONT, this.axis.getLabelFont());
        assertEquals(Axis.DEFAULT_AXIS_LABEL_PAINT, this.axis.getLabelPaint());
        assertEquals(Axis.DEFAULT_AXIS_LABEL_INSETS, this.axis.getLabelInsets());
        assertEquals(0.0, this.axis.getLabelAngle(), 0.0001);
        assertNull(this.axis.getLabelToolTip());
        assertNull(this.axis.getLabelURL());
        assertTrue(this.axis.isAxisLineVisible());
        assertEquals(Axis.DEFAULT_AXIS_LINE_PAINT, this.axis.getAxisLinePaint());
        assertEquals(Axis.DEFAULT_AXIS_LINE_STROKE, this.axis.getAxisLineStroke());
        assertTrue(this.axis.isTickLabelsVisible());
        assertEquals(Axis.DEFAULT_TICK_LABEL_FONT, this.axis.getTickLabelFont());
        assertEquals(Axis.DEFAULT_TICK_LABEL_PAINT, this.axis.getTickLabelPaint());
        assertEquals(Axis.DEFAULT_TICK_LABEL_INSETS, this.axis.getTickLabelInsets());
        assertTrue(this.axis.isTickMarksVisible());
        assertEquals(Axis.DEFAULT_TICK_MARK_STROKE, this.axis.getTickMarkStroke());
        assertEquals(Axis.DEFAULT_TICK_MARK_PAINT, this.axis.getTickMarkPaint());
        assertEquals(Axis.DEFAULT_TICK_MARK_INSIDE_LENGTH, this.axis.getTickMarkInsideLength(), 0.0001f);
        assertEquals(Axis.DEFAULT_TICK_MARK_OUTSIDE_LENGTH, this.axis.getTickMarkOutsideLength(), 0.0001f);
        assertNull(this.axis.getPlot());
    }

    // Tests setVisible and listener notification
    @Test
    public void testSetVisible_changeVisibility_notifiesListener() {
        TestAxisListener listener = new TestAxisListener();
        this.axis.addChangeListener(listener);
        assertTrue(this.axis.hasListener(listener));

        this.axis.setVisible(false);
        assertFalse(this.axis.isVisible());
        assertTrue(listener.isNotified());

        this.axis.removeChangeListener(listener);
        assertFalse(this.axis.hasListener(listener));
    }

    // Tests setLabel with different and null values
    @Test
    public void testSetLabel_variousValues_updatesLabelAndNotifies() {
        TestAxisListener listener = new TestAxisListener();
        this.axis.addChangeListener(listener);

        this.axis.setLabel("New Label");
        assertEquals("New Label", this.axis.getLabel());
        assertTrue(listener.isNotified());

        TestAxis axisWithNull = new TestAxis(null);
        assertNull(axisWithNull.getLabel());
        axisWithNull.setLabel("Initialized");
        assertEquals("Initialized", axisWithNull.getLabel());
    }

    // Tests setLabelFont with valid and null input
    @Test
    public void testSetLabelFont_validFont_updatesFont() {
        Font font = new Font("Serif", Font.BOLD, 14);
        this.axis.setLabelFont(font);
        assertEquals(font, this.axis.getLabelFont());
    }

    // Tests exception path for setLabelFont with null input
    @Test(expected = IllegalArgumentException.class)
    public void testSetLabelFont_nullFont_throwsException() {
        this.axis.setLabelFont(null);
    }

    // Tests setLabelPaint with valid paint
    @Test
    public void testSetLabelPaint_validPaint_updatesPaint() {
        this.axis.setLabelPaint(Color.red);
        assertEquals(Color.red, this.axis.getLabelPaint());
    }

    // Tests exception path for setLabelPaint with null input
    @Test(expected = IllegalArgumentException.class)
    public void testSetLabelPaint_nullPaint_throwsException() {
        this.axis.setLabelPaint(null);
    }

    // Tests setLabelInsets with valid and null input
    @Test
    public void testSetLabelInsets_validInsets_updatesInsets() {
        RectangleInsets insets = new RectangleInsets(5.0, 5.0, 5.0, 5.0);
        this.axis.setLabelInsets(insets);
        assertEquals(insets, this.axis.getLabelInsets());
    }

    // Tests exception path for setLabelInsets with null input
    @Test(expected = IllegalArgumentException.class)
    public void testSetLabelInsets_nullInsets_throwsException() {
        this.axis.setLabelInsets(null);
    }

    // Tests label properties: angle, tooltip, and url
    @Test
    public void testSetLabelAngleAndToolTipAndURL_validValues_updatesProperties() {
        this.axis.setLabelAngle(Math.PI / 4.0);
        assertEquals(Math.PI / 4.0, this.axis.getLabelAngle(), 0.0001);

        this.axis.setLabelToolTip("tooltip");
        assertEquals("tooltip", this.axis.getLabelToolTip());

        this.axis.setLabelURL("http://www.jfree.org");
        assertEquals("http://www.jfree.org", this.axis.getLabelURL());
    }

    // Tests axis line properties
    @Test
    public void testAxisLineProperties_validValues_updatesCorrectly() {
        this.axis.setAxisLineVisible(false);
        assertFalse(this.axis.isAxisLineVisible());

        this.axis.setAxisLinePaint(Color.blue);
        assertEquals(Color.blue, this.axis.getAxisLinePaint());

        Stroke stroke = new BasicStroke(2.0f);
        this.axis.setAxisLineStroke(stroke);
        assertEquals(stroke, this.axis.getAxisLineStroke());
    }

    // Tests tick label and tick mark properties
    @Test
    public void testTickProperties_validValues_updatesCorrectly() {
        this.axis.setTickLabelsVisible(false);
        assertFalse(this.axis.isTickLabelsVisible());

        Font font = new Font("Monospaced", Font.PLAIN, 11);
        this.axis.setTickLabelFont(font);
        assertEquals(font, this.axis.getTickLabelFont());

        this.axis.setTickLabelPaint(Color.green);
        assertEquals(Color.green, this.axis.getTickLabelPaint());

        RectangleInsets tickInsets = new RectangleInsets(1.0, 1.0, 1.0, 1.0);
        this.axis.setTickLabelInsets(tickInsets);
        assertEquals(tickInsets, this.axis.getTickLabelInsets());

        this.axis.setTickMarksVisible(false);
        assertFalse(this.axis.isTickMarksVisible());

        this.axis.setTickMarkInsideLength(1.5f);
        assertEquals(1.5f, this.axis.getTickMarkInsideLength(), 0.0001f);

        this.axis.setTickMarkOutsideLength(3.5f);
        assertEquals(3.5f, this.axis.getTickMarkOutsideLength(), 0.0001f);

        Stroke tickStroke = new BasicStroke(1.5f);
        this.axis.setTickMarkStroke(tickStroke);
        assertEquals(tickStroke, this.axis.getTickMarkStroke());

        this.axis.setTickMarkPaint(Color.magenta);
        assertEquals(Color.magenta, this.axis.getTickMarkPaint());

        this.axis.setFixedDimension(100.0);
        assertEquals(100.0, this.axis.getFixedDimension(), 0.0001);
    }

    // Tests equals and hashCode equivalence
    @Test
    public void testEquals_symmetricAndDifferentAttributes_returnsExpected() {
        TestAxis a1 = new TestAxis("Label");
        TestAxis a2 = new TestAxis("Label");
        assertTrue(a1.equals(a1));
        assertTrue(a1.equals(a2));
        assertTrue(a2.equals(a1));
        assertFalse(a1.equals(null));
        assertFalse(a1.equals("Not an Axis"));

        a2.setLabel("Different");
        assertFalse(a1.equals(a2));

        a2.setLabel("Label");
        a2.setVisible(false);
        assertFalse(a1.equals(a2));

        a2.setVisible(true);
        a2.setFixedDimension(50.0);
        assertFalse(a1.equals(a2));
    }

    // Tests clone creates independent copy
    @Test
    public void testClone_clonedInstance_equalsOriginalAndIndependent() throws CloneNotSupportedException {
        TestAxis a1 = new TestAxis("Clone Test");
        TestAxis a2 = (TestAxis) a1.clone();

        assertEquals(a1, a2);
        assertNotSame(a1, a2);
        assertNull(a2.getPlot());
    }

    // Tests serialization and deserialization
    @Test
    public void testSerialization_serializedAndDeserialized_equalsOriginal() throws Exception {
        TestAxis a1 = new TestAxis("Serialization Test");
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(buffer);
        out.writeObject(a1);
        out.close();

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        TestAxis a2 = (TestAxis) in.readObject();
        in.close();

        assertEquals(a1, a2);
    }

    // Tests drawLabel with PlotRenderingInfo and null owner (Defects4J Chart-26 regression test)
    @Test
    public void testDrawLabel_plotStateWithoutOwner_doesNotThrowNullPointerException() {
        BufferedImage image = new BufferedImage(300, 300, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        Rectangle2D plotArea = new Rectangle2D.Double(0, 0, 300, 300);
        Rectangle2D dataArea = new Rectangle2D.Double(20, 20, 260, 260);

        PlotRenderingInfo plotState = new PlotRenderingInfo(null);
        AxisState state = new AxisState(280.0);

        AxisState result = this.axis.drawLabel(
                "Bottom Axis", g2, plotArea, dataArea, RectangleEdge.BOTTOM, state, plotState);
        assertNotNull(result);

        g2.dispose();
    }

    // Tests drawLabel with EntityCollection across all RectangleEdge locations
    @Test
    public void testDrawLabel_allEdgesWithEntities_addsEntityAndUpdatesCursor() {
        BufferedImage image = new BufferedImage(300, 300, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        Rectangle2D plotArea = new Rectangle2D.Double(0, 0, 300, 300);
        Rectangle2D dataArea = new Rectangle2D.Double(40, 40, 220, 220);

        ChartRenderingInfo chartInfo = new ChartRenderingInfo(new StandardEntityCollection());
        PlotRenderingInfo plotState = new PlotRenderingInfo(chartInfo);

        this.axis.setLabelToolTip("Tooltip");
        this.axis.setLabelURL("URL");

        AxisState stateTop = new AxisState(40.0);
        this.axis.drawLabel("Top", g2, plotArea, dataArea, RectangleEdge.TOP, stateTop, plotState);
        assertTrue(stateTop.getCursor() < 40.0);

        AxisState stateBottom = new AxisState(260.0);
        this.axis.drawLabel("Bottom", g2, plotArea, dataArea, RectangleEdge.BOTTOM, stateBottom, plotState);
        assertTrue(stateBottom.getCursor() > 260.0);

        AxisState stateLeft = new AxisState(40.0);
        this.axis.drawLabel("Left", g2, plotArea, dataArea, RectangleEdge.LEFT, stateLeft, plotState);
        assertTrue(stateLeft.getCursor() < 40.0);

        AxisState stateRight = new AxisState(260.0);
        this.axis.drawLabel("Right", g2, plotArea, dataArea, RectangleEdge.RIGHT, stateRight, plotState);
        assertTrue(stateRight.getCursor() > 260.0);

        assertEquals(4, chartInfo.getEntityCollection().getEntityCount());
        g2.dispose();
    }

    // Tests drawAxisLine along all rectangle edges
    @Test
    public void testDrawAxisLine_allEdges_executesWithoutError() {
        BufferedImage image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        Rectangle2D dataArea = new Rectangle2D.Double(20, 20, 160, 160);

        this.axis.drawAxisLine(g2, 20.0, dataArea, RectangleEdge.TOP);
        this.axis.drawAxisLine(g2, 180.0, dataArea, RectangleEdge.BOTTOM);
        this.axis.drawAxisLine(g2, 20.0, dataArea, RectangleEdge.LEFT);
        this.axis.drawAxisLine(g2, 180.0, dataArea, RectangleEdge.RIGHT);

        g2.dispose();
    }

    // Tests getLabelEnclosure with empty and non-empty label
    @Test
    public void testGetLabelEnclosure_validAndEmptyLabels_returnsBounds() {
        BufferedImage image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();

        Rectangle2D enclosure = this.axis.getLabelEnclosure(g2, RectangleEdge.BOTTOM);
        assertTrue(enclosure.getWidth() > 0);
        assertTrue(enclosure.getHeight() > 0);

        this.axis.setLabel("");
        Rectangle2D emptyEnclosure = this.axis.getLabelEnclosure(g2, RectangleEdge.BOTTOM);
        assertEquals(0.0, emptyEnclosure.getWidth(), 0.0001);
        assertEquals(0.0, emptyEnclosure.getHeight(), 0.0001);

        g2.dispose();
    }
}