package org.jfree.chart.plot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.util.RectangleInsets;
import org.jfree.chart.util.Rotation;
import org.jfree.data.DefaultKeyedValues;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link PiePlot}.
 */
public class PiePlotTest {

    // Tests default constructor initialization and default property values
    @Test
    public void testConstructor_default_initializesCorrectly() {
        PiePlot plot = new PiePlot();
        assertNull(plot.getDataset());
        assertEquals(PiePlot.DEFAULT_INTERIOR_GAP, plot.getInteriorGap(), 0.0001);
        assertEquals(PiePlot.DEFAULT_START_ANGLE, plot.getStartAngle(), 0.0001);
        assertEquals(Rotation.CLOCKWISE, plot.getDirection());
        assertTrue(plot.isCircular());
        assertTrue(plot.getSectionOutlinesVisible());
        assertFalse(plot.getIgnoreNullValues());
        assertFalse(plot.getIgnoreZeroValues());
        assertEquals("Pie_Plot", plot.getPlotType());
    }

    // Tests constructor with a valid dataset
    @Test
    public void testConstructor_withDataset_setsDatasetAndListeners() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("A", 10.0);
        PiePlot plot = new PiePlot(dataset);
        assertSame(dataset, plot.getDataset());
    }

    // Tests setDataset with null and non-null values
    @Test
    public void testSetDataset_validAndNull_updatesCorrectly() {
        PiePlot plot = new PiePlot();
        DefaultPieDataset ds1 = new DefaultPieDataset();
        ds1.setValue("Section 1", 25.0);

        plot.setDataset(ds1);
        assertSame(ds1, plot.getDataset());

        plot.setDataset(null);
        assertNull(plot.getDataset());
    }

    // Tests startAngle and pieIndex getters and setters
    @Test
    public void testGetSetStartAngleAndPieIndex_validValues_updatesCorrectly() {
        PiePlot plot = new PiePlot();
        plot.setStartAngle(180.0);
        assertEquals(180.0, plot.getStartAngle(), 0.0001);

        plot.setPieIndex(2);
        assertEquals(2, plot.getPieIndex());
    }

    // Tests setDirection valid value and null exception
    @Test
    public void testSetDirection_validAndNull_updatesOrThrowsException() {
        PiePlot plot = new PiePlot();
        plot.setDirection(Rotation.ANTICLOCKWISE);
        assertEquals(Rotation.ANTICLOCKWISE, plot.getDirection());

        try {
            plot.setDirection(null);
            fail("Expected IllegalArgumentException for null direction");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    // Tests interior gap boundary values and out-of-range exceptions
    @Test
    public void testSetInteriorGap_boundariesAndInvalid_updatesOrThrows() {
        PiePlot plot = new PiePlot();
        plot.setInteriorGap(0.0);
        assertEquals(0.0, plot.getInteriorGap(), 0.0001);

        plot.setInteriorGap(PiePlot.MAX_INTERIOR_GAP);
        assertEquals(PiePlot.MAX_INTERIOR_GAP, plot.getInteriorGap(), 0.0001);

        try {
            plot.setInteriorGap(-0.01);
            fail("Expected IllegalArgumentException for negative interiorGap");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            plot.setInteriorGap(PiePlot.MAX_INTERIOR_GAP + 0.01);
            fail("Expected IllegalArgumentException for interiorGap > MAX_INTERIOR_GAP");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    // Tests section paint lookup and fallbacks
    @Test
    public void testSectionPaint_lookupAndAutoPopulate_returnsCorrectPaint() {
        PiePlot plot = new PiePlot();
        plot.setSectionPaint("A", Color.red);
        assertEquals(Color.red, plot.getSectionPaint("A"));
        assertEquals(Color.red, plot.lookupSectionPaint("A"));

        // Fallback to baseSectionPaint when not configured and autoPopulate is false
        plot.setBaseSectionPaint(Color.blue);
        assertEquals(Color.blue, plot.lookupSectionPaint("NonExistent", false));

        // Auto-populate from DrawingSupplier
        Paint populated = plot.lookupSectionPaint("B", true);
        assertNotNull(populated);
        assertEquals(populated, plot.getSectionPaint("B"));
    }

    // Tests section outline paint and stroke lookup with base fallbacks
    @Test
    public void testSectionOutlinePaintAndStroke_lookupAndSet_worksCorrectly() {
        PiePlot plot = new PiePlot();
        plot.setSectionOutlinePaint("A", Color.green);
        assertEquals(Color.green, plot.getSectionOutlinePaint("A"));
        assertEquals(Color.green, plot.lookupSectionOutlinePaint("A"));

        Stroke stroke = new BasicStroke(2.0f);
        plot.setSectionOutlineStroke("A", stroke);
        assertEquals(stroke, plot.getSectionOutlineStroke("A"));
        assertEquals(stroke, plot.lookupSectionOutlineStroke("A"));

        plot.setBaseSectionOutlinePaint(Color.yellow);
        assertEquals(Color.yellow, plot.lookupSectionOutlinePaint("Unknown", false));

        Stroke baseStroke = new BasicStroke(1.5f);
        plot.setBaseSectionOutlineStroke(baseStroke);
        assertEquals(baseStroke, plot.lookupSectionOutlineStroke("Unknown", false));
    }

    // Tests explode percentage methods
    @Test
    public void testExplodePercent_getAndSet_handlesValuesCorrectly() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("A", 10.0);
        dataset.setValue("B", 20.0);

        PiePlot plot = new PiePlot(dataset);
        assertEquals(0.0, plot.getExplodePercent("A"), 0.0001);

        plot.setExplodePercent("A", 0.30);
        assertEquals(0.30, plot.getExplodePercent("A"), 0.0001);
        assertEquals(0.30, plot.getMaximumExplodePercent(), 0.0001);

        plot.setExplodePercent("B", 0.50);
        assertEquals(0.50, plot.getMaximumExplodePercent(), 0.0001);

        try {
            plot.setExplodePercent(null, 0.1);
            fail("Expected IllegalArgumentException for null key");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    // Tests getLegendItems with null dataset
    @Test
    public void testGetLegendItems_nullDataset_returnsEmptyCollection() {
        PiePlot plot = new PiePlot(null);
        LegendItemCollection items = plot.getLegendItems();
        assertNotNull(items);
        assertEquals(0, items.getItemCount());
    }

    // Tests getLegendItems filtering zero and null values based on flags
    @Test
    public void testGetLegendItems_withNullAndZeroValues_respectsFlags() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("Positive", 50.0);
        dataset.setValue("Zero", 0.0);
        dataset.setValue("NullVal", (Number) null);

        PiePlot plot = new PiePlot(dataset);
        plot.setIgnoreNullValues(false);
        plot.setIgnoreZeroValues(false);
        assertEquals(3, plot.getLegendItems().getItemCount());

        plot.setIgnoreNullValues(true);
        assertEquals(2, plot.getLegendItems().getItemCount());

        plot.setIgnoreZeroValues(true);
        assertEquals(1, plot.getLegendItems().getItemCount());
    }

    // Tests draw with a null dataset without crashing
    @Test
    public void testDraw_nullDataset_drawsNoDataMessage() {
        PiePlot plot = new PiePlot(null);
        BufferedImage image = new BufferedImage(300, 200, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());
        plot.draw(g2, new Rectangle2D.Double(0, 0, 300, 200), null, null, info.getPlotInfo());
        g2.dispose();
        assertNotNull(info.getPlotInfo().getDataArea());
    }

    // Tests draw with standard labels and exploded sections
    @Test
    public void testDraw_withDatasetAndExplode_rendersSuccessfully() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("LeftSection", 40.0);
        dataset.setValue("RightSection", 60.0);

        PiePlot plot = new PiePlot(dataset);
        plot.setExplodePercent("LeftSection", 0.15);
        plot.setSimpleLabels(false);

        BufferedImage image = new BufferedImage(400, 300, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());
        plot.draw(g2, new Rectangle2D.Double(0, 0, 400, 300), null, null, info.getPlotInfo());
        g2.dispose();

        assertTrue(info.getEntityCollection().getEntityCount() > 0);
    }

    // Tests draw with simple labels option enabled
    @Test
    public void testDraw_simpleLabels_rendersSuccessfully() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("Section 1", 30.0);
        dataset.setValue("Section 2", 70.0);

        PiePlot plot = new PiePlot(dataset);
        plot.setSimpleLabels(true);

        BufferedImage image = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());
        plot.draw(g2, new Rectangle2D.Double(0, 0, 300, 300), null, null, info.getPlotInfo());
        g2.dispose();

        assertTrue(info.getEntityCollection().getEntityCount() > 0);
    }

    // Tests equals method for symmetry and difference detection
    @Test
    public void testEquals_variousProperties_identifiesEqualityCorrectly() {
        PiePlot p1 = new PiePlot();
        PiePlot p2 = new PiePlot();
        assertTrue(p1.equals(p2));
        assertTrue(p2.equals(p1));

        p1.setStartAngle(45.0);
        assertFalse(p1.equals(p2));
        p2.setStartAngle(45.0);
        assertTrue(p1.equals(p2));

        p1.setCircular(false);
        assertFalse(p1.equals(p2));
        p2.setCircular(false);
        assertTrue(p1.equals(p2));

        p1.setSimpleLabels(true);
        assertFalse(p1.equals(p2));
        p2.setSimpleLabels(true);
        assertTrue(p1.equals(p2));

        p1.setSectionPaint("K", Color.magenta);
        assertFalse(p1.equals(p2));
        p2.setSectionPaint("K", Color.magenta);
        assertTrue(p1.equals(p2));
    }

    // Tests cloning creates independent clone
    @Test
    public void testClone_createsIndependentCopy() throws CloneNotSupportedException {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("A", 10.0);
        PiePlot p1 = new PiePlot(dataset);
        PiePlot p2 = (PiePlot) p1.clone();

        assertNotSame(p1, p2);
        assertSame(p1.getClass(), p2.getClass());
        assertTrue(p1.equals(p2));

        p2.setStartAngle(33.0);
        assertFalse(p1.equals(p2));
    }

    // Tests serialization and deserialization
    @Test
    public void testSerialization_roundTrip_retainsState() throws Exception {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("S1", 100.0);
        PiePlot p1 = new PiePlot(dataset);
        p1.setSectionPaint("S1", Color.cyan);
        p1.setExplodePercent("S1", 0.25);

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(buffer);
        out.writeObject(p1);
        out.close();

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        PiePlot p2 = (PiePlot) in.readObject();
        in.close();

        assertEquals(p1, p2);
    }

    // Tests validation / exception on null arguments for various setters
    @Test
    public void testSetters_nullArguments_throwExceptions() {
        PiePlot plot = new PiePlot();

        try {
            plot.setBaseSectionPaint(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            plot.setBaseSectionOutlinePaint(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            plot.setBaseSectionOutlineStroke(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            plot.setLabelFont(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            plot.setLabelPaint(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            plot.setLabelPadding(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            plot.setLabelDistributor(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }
}