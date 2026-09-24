package org.jfree.chart.plot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.List;

import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.AxisSpace;
import org.jfree.chart.axis.CategoryAnchor;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.util.Layer;
import org.jfree.chart.util.RectangleEdge;
import org.jfree.chart.util.RectangleInsets;
import org.jfree.chart.util.SortOrder;
import org.jfree.data.Range;
import org.jfree.data.category.DefaultCategoryDataset;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CategoryPlotTest {

    private CategoryPlot plot;

    @Before
    public void setUp() {
        CategoryAxis domainAxis = new CategoryAxis("Domain");
        ValueAxis rangeAxis = new NumberAxis("Range");
        BarRenderer renderer = new BarRenderer();
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "R1", "C1");
        dataset.addValue(2.0, "R1", "C2");
        this.plot = new CategoryPlot(dataset, domainAxis, rangeAxis, renderer);
    }

    // Tests Defects4J Chart-19 bug: getDomainAxisIndex with null or non-existent axis
    @Test
    public void testGetDomainAxisIndex_validAndInvalidAxis_returnsCorrectIndex() {
        CategoryAxis axis1 = new CategoryAxis("Axis1");
        CategoryAxis axis2 = new CategoryAxis("Axis2");
        this.plot.setDomainAxis(0, axis1);
        
        assertEquals(0, this.plot.getDomainAxisIndex(axis1));
        assertEquals(-1, this.plot.getDomainAxisIndex(axis2));
    }

    // Tests Defects4J Chart-19 bug: getRangeAxisIndex with null or non-existent axis
    @Test
    public void testGetRangeAxisIndex_validAndInvalidAxis_returnsCorrectIndex() {
        ValueAxis axis1 = new NumberAxis("Axis1");
        ValueAxis axis2 = new NumberAxis("Axis2");
        this.plot.setRangeAxis(0, axis1);

        assertEquals(0, this.plot.getRangeAxisIndex(axis1));
        assertEquals(-1, this.plot.getRangeAxisIndex(axis2));
    }

    // Tests getDomainAxisIndex and getRangeAxisIndex with null parameter
    @Test(expected = IllegalArgumentException.class)
    public void testGetDomainAxisIndex_nullAxis_throwsException() {
        this.plot.getDomainAxisIndex(null);
    }

    // Tests getRangeAxisIndex with null parameter
    @Test(expected = IllegalArgumentException.class)
    public void testGetRangeAxisIndex_nullAxis_throwsException() {
        this.plot.getRangeAxisIndex(null);
    }

    // Tests default constructor initialization
    @Test
    public void testConstructor_default_initializesProperly() {
        CategoryPlot defaultPlot = new CategoryPlot();
        assertNull(defaultPlot.getDataset());
        assertNull(defaultPlot.getDomainAxis());
        assertNull(defaultPlot.getRangeAxis());
        assertNull(defaultPlot.getRenderer());
        assertEquals(PlotOrientation.VERTICAL, defaultPlot.getOrientation());
        assertEquals(1, defaultPlot.getRangeMarkers(Layer.BACKGROUND).size());
    }

    // Tests orientation get and set with valid and null values
    @Test
    public void testSetOrientation_validAndNull_updatesProperlyOrThrows() {
        this.plot.setOrientation(PlotOrientation.HORIZONTAL);
        assertEquals(PlotOrientation.HORIZONTAL, this.plot.getOrientation());

        try {
            this.plot.setOrientation(null);
            fail("Expected IllegalArgumentException on null orientation");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    // Tests domain and range axis location and edge resolution
    @Test
    public void testAxisLocationsAndEdges_normalCases_returnsExpectedEdges() {
        this.plot.setOrientation(PlotOrientation.VERTICAL);
        this.plot.setDomainAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
        this.plot.setRangeAxisLocation(AxisLocation.TOP_OR_LEFT);

        assertEquals(AxisLocation.BOTTOM_OR_RIGHT, this.plot.getDomainAxisLocation());
        assertEquals(RectangleEdge.BOTTOM, this.plot.getDomainAxisEdge());
        assertEquals(AxisLocation.TOP_OR_LEFT, this.plot.getRangeAxisLocation());
        assertEquals(RectangleEdge.LEFT, this.plot.getRangeAxisEdge());

        this.plot.setOrientation(PlotOrientation.HORIZONTAL);
        assertEquals(RectangleEdge.RIGHT, this.plot.getDomainAxisEdge());
        assertEquals(RectangleEdge.TOP, this.plot.getRangeAxisEdge());
    }

    // Tests multiple domain and range axes manipulation
    @Test
    public void testMultipleAxes_addAndClear_countsAndIndicesAreCorrect() {
        CategoryAxis domain2 = new CategoryAxis("Domain 2");
        ValueAxis range2 = new NumberAxis("Range 2");
        
        this.plot.setDomainAxis(1, domain2);
        this.plot.setRangeAxis(1, range2);

        assertEquals(2, this.plot.getDomainAxisCount());
        assertEquals(2, this.plot.getRangeAxisCount());
        assertEquals(domain2, this.plot.getDomainAxis(1));
        assertEquals(range2, this.plot.getRangeAxis(1));
        assertEquals(1, this.plot.getDomainAxisIndex(domain2));
        assertEquals(1, this.plot.getRangeAxisIndex(range2));

        this.plot.clearDomainAxes();
        this.plot.clearRangeAxes();
        assertEquals(0, this.plot.getDomainAxisCount());
        assertEquals(0, this.plot.getRangeAxisCount());
    }

    // Tests dataset mapping to domain and range axes
    @Test
    public void testDatasetMapping_customAxes_mapsCorrectly() {
        DefaultCategoryDataset dataset2 = new DefaultCategoryDataset();
        dataset2.addValue(5.0, "R2", "C2");
        CategoryAxis domain2 = new CategoryAxis("D2");
        ValueAxis range2 = new NumberAxis("R2");

        this.plot.setDomainAxis(1, domain2);
        this.plot.setRangeAxis(1, range2);
        this.plot.setDataset(1, dataset2);

        this.plot.mapDatasetToDomainAxis(1, 1);
        this.plot.mapDatasetToRangeAxis(1, 1);

        assertEquals(domain2, this.plot.getDomainAxisForDataset(1));
        assertEquals(range2, this.plot.getRangeAxisForDataset(1));
        assertEquals(this.plot.getDomainAxis(0), this.plot.getDomainAxisForDataset(0));
        assertEquals(this.plot.getRangeAxis(0), this.plot.getRangeAxisForDataset(0));
    }

    // Tests multiple renderers and getDataRange calculation
    @Test
    public void testRenderersAndDataRange_normalValues_calculatesCombinedRange() {
        CategoryItemRenderer renderer2 = new LineAndShapeRenderer();
        this.plot.setRenderer(1, renderer2);
        assertEquals(renderer2, this.plot.getRenderer(1));
        assertEquals(1, this.plot.getIndexOf(renderer2));

        DefaultCategoryDataset dataset2 = new DefaultCategoryDataset();
        dataset2.addValue(10.0, "R2", "C1");
        dataset2.addValue(-5.0, "R2", "C2");
        this.plot.setDataset(1, dataset2);

        Range range = this.plot.getDataRange(this.plot.getRangeAxis(0));
        assertNotNull(range);
        assertEquals(-5.0, range.getLowerBound(), 0.0001);
        assertEquals(10.0, range.getUpperBound(), 0.0001);
    }

    // Tests domain and range markers addition, retrieval, and clearing
    @Test
    public void testMarkers_addGetClear_managesCollectionCorrectly() {
        CategoryMarker domainMarker = new CategoryMarker("C1");
        ValueMarker rangeMarker = new ValueMarker(1.5);

        this.plot.addDomainMarker(domainMarker, Layer.FOREGROUND);
        this.plot.addRangeMarker(rangeMarker, Layer.FOREGROUND);

        Collection domainMarkers = this.plot.getDomainMarkers(Layer.FOREGROUND);
        Collection rangeMarkers = this.plot.getRangeMarkers(Layer.FOREGROUND);

        assertNotNull(domainMarkers);
        assertTrue(domainMarkers.contains(domainMarker));
        assertNotNull(rangeMarkers);
        assertTrue(rangeMarkers.contains(rangeMarker));

        this.plot.clearDomainMarkers();
        this.plot.clearRangeMarkers();

        assertNull(this.plot.getDomainMarkers(Layer.FOREGROUND));
        assertNull(this.plot.getRangeMarkers(Layer.FOREGROUND));
    }

    // Tests crosshair settings and notification behavior
    @Test
    public void testCrosshairs_setAndGet_valuesPersistCorrectly() {
        this.plot.setRangeCrosshairVisible(true);
        assertTrue(this.plot.isRangeCrosshairVisible());

        this.plot.setRangeCrosshairValue(2.5);
        assertEquals(2.5, this.plot.getRangeCrosshairValue(), 0.0001);

        this.plot.setRangeCrosshairLockedOnData(false);
        assertFalse(this.plot.isRangeCrosshairLockedOnData());

        BasicStroke stroke = new BasicStroke(2.0f);
        this.plot.setRangeCrosshairStroke(stroke);
        assertEquals(stroke, this.plot.getRangeCrosshairStroke());

        this.plot.setRangeCrosshairPaint(Color.RED);
        assertEquals(Color.RED, this.plot.getRangeCrosshairPaint());
    }

    // Tests zooming operations on range axes
    @Test
    public void testZooming_rangeAxisZoom_modifiesAxisRange() {
        ValueAxis axis = this.plot.getRangeAxis();
        axis.setRange(0.0, 10.0);

        this.plot.setAnchorValue(5.0);
        this.plot.zoom(0.5);
        assertEquals(2.5, axis.getLowerBound(), 0.0001);
        assertEquals(7.5, axis.getUpperBound(), 0.0001);

        this.plot.zoom(0.0);
        assertTrue(axis.isAutoRange());
    }

    // Tests getCategories and getCategoriesForAxis
    @Test
    public void testGetCategories_withDataset_returnsCategoryList() {
        List categories = this.plot.getCategories();
        assertNotNull(categories);
        assertEquals(2, categories.size());
        assertEquals("C1", categories.get(0));
        assertEquals("C2", categories.get(1));

        List axisCategories = this.plot.getCategoriesForAxis(this.plot.getDomainAxis());
        assertEquals(2, axisCategories.size());
    }

    // Tests drawing operation executes without exceptions
    @Test
    public void testDraw_validGraphics_executesSuccessfully() {
        BufferedImage image = new BufferedImage(400, 300, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        Rectangle2D area = new Rectangle2D.Double(0.0, 0.0, 400.0, 300.0);

        this.plot.setDomainGridlinesVisible(true);
        this.plot.setRangeGridlinesVisible(true);
        this.plot.setRangeCrosshairVisible(true);
        this.plot.setRangeCrosshairValue(1.0);

        this.plot.draw(g2, area, null, null, new PlotRenderingInfo(null));
        g2.dispose();
    }

    // Tests equals method for symmetry and difference detection
    @Test
    public void testEquals_variousProperties_verifiesEquality() {
        CategoryPlot p1 = new CategoryPlot();
        CategoryPlot p2 = new CategoryPlot();
        assertTrue(p1.equals(p2));
        assertTrue(p2.equals(p1));

        p1.setOrientation(PlotOrientation.HORIZONTAL);
        assertFalse(p1.equals(p2));
        p2.setOrientation(PlotOrientation.HORIZONTAL);
        assertTrue(p1.equals(p2));

        p1.setRowRenderingOrder(SortOrder.DESCENDING);
        assertFalse(p1.equals(p2));
        p2.setRowRenderingOrder(SortOrder.DESCENDING);
        assertTrue(p1.equals(p2));

        p1.setDomainGridlinesVisible(true);
        assertFalse(p1.equals(p2));
        p2.setDomainGridlinesVisible(true);
        assertTrue(p1.equals(p2));
    }

    // Tests cloning creates independent clone with copied components
    @Test
    public void testCloning_standardPlot_createsIndependentCopy() throws Exception {
        CategoryPlot clone = (CategoryPlot) this.plot.clone();
        assertNotSame(this.plot, clone);
        assertSame(this.plot.getClass(), clone.getClass());
        assertEquals(this.plot, clone);

        clone.getDomainAxis().setLabel("New Label");
        assertFalse(this.plot.getDomainAxis().getLabel().equals(clone.getDomainAxis().getLabel()));
    }

    // Tests serialization and deserialization
    @Test
    public void testSerialization_roundTrip_restoresEqualPlot() throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(buffer);
        out.writeObject(this.plot);
        out.close();

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        CategoryPlot deserialized = (CategoryPlot) in.readObject();
        in.close();

        assertEquals(this.plot, deserialized);
    }
}