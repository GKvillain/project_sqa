package org.jfree.chart.renderer.category;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.annotations.CategoryAnnotation;
import org.jfree.chart.annotations.CategoryTextAnnotation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardCategorySeriesLabelGenerator;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.urls.StandardCategoryURLGenerator;
import org.jfree.chart.util.Layer;
import org.jfree.data.Range;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class AbstractCategoryItemRendererTest {

    private static class ConcreteCategoryItemRenderer extends AbstractCategoryItemRenderer {
        public void drawItem(Graphics2D g2, CategoryItemRendererState state,
                Rectangle2D dataArea, CategoryPlot plot, CategoryAxis domainAxis,
                ValueAxis rangeAxis, CategoryDataset dataset, int row, int column,
                int pass) {
            // stub implementation for testing
        }
    }

    private ConcreteCategoryItemRenderer renderer;

    @Before
    public void setUp() {
        this.renderer = new ConcreteCategoryItemRenderer();
    }

    // Tests defect in getLegendItems when dataset is present
    @Test
    public void testGetLegendItems_withDataset_returnsCollectionWithItems() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "S1", "C1");
        dataset.addValue(2.0, "S2", "C1");
        CategoryPlot plot = new CategoryPlot(dataset, new CategoryAxis("Category"),
                new NumberAxis("Value"), this.renderer);

        LegendItemCollection items = this.renderer.getLegendItems();
        assertNotNull(items);
        assertEquals(2, items.getItemCount());
        assertEquals("S1", items.get(0).getLabel());
        assertEquals("S2", items.get(1).getLabel());
    }

    // Tests getLegendItems when plot is null
    @Test
    public void testGetLegendItems_nullPlot_returnsEmptyCollection() {
        LegendItemCollection items = this.renderer.getLegendItems();
        assertNotNull(items);
        assertEquals(0, items.getItemCount());
    }

    // Tests getLegendItems when dataset is null
    @Test
    public void testGetLegendItems_nullDataset_returnsEmptyCollection() {
        CategoryPlot plot = new CategoryPlot(null, new CategoryAxis("Category"),
                new NumberAxis("Value"), this.renderer);
        LegendItemCollection items = this.renderer.getLegendItems();
        assertNotNull(items);
        assertEquals(0, items.getItemCount());
    }

    // Tests getPassCount default return value
    @Test
    public void testGetPassCount_default_returnsOne() {
        assertEquals(1, this.renderer.getPassCount());
    }

    // Tests setPlot with null input
    @Test(expected = IllegalArgumentException.class)
    public void testSetPlot_nullPlot_throwsIllegalArgumentException() {
        this.renderer.setPlot(null);
    }

    // Tests item label generator fallback mechanism
    @Test
    public void testGetItemLabelGenerator_seriesAndBaseFallback_returnsExpectedGenerator() {
        StandardCategoryItemLabelGenerator baseGen = new StandardCategoryItemLabelGenerator();
        StandardCategoryItemLabelGenerator seriesGen = new StandardCategoryItemLabelGenerator();

        this.renderer.setBaseItemLabelGenerator(baseGen);
        assertEquals(baseGen, this.renderer.getItemLabelGenerator(0, 0, false));

        this.renderer.setSeriesItemLabelGenerator(0, seriesGen);
        assertEquals(seriesGen, this.renderer.getItemLabelGenerator(0, 0, false));
        assertEquals(baseGen, this.renderer.getItemLabelGenerator(1, 0, false));
    }

    // Tests tool tip generator fallback mechanism
    @Test
    public void testGetToolTipGenerator_seriesAndBaseFallback_returnsExpectedGenerator() {
        StandardCategoryToolTipGenerator baseGen = new StandardCategoryToolTipGenerator();
        StandardCategoryToolTipGenerator seriesGen = new StandardCategoryToolTipGenerator();

        this.renderer.setBaseToolTipGenerator(baseGen);
        assertEquals(baseGen, this.renderer.getToolTipGenerator(0, 0, false));

        this.renderer.setSeriesToolTipGenerator(0, seriesGen);
        assertEquals(seriesGen, this.renderer.getToolTipGenerator(0, 0, false));
        assertEquals(baseGen, this.renderer.getToolTipGenerator(1, 0, false));
    }

    // Tests URL generator fallback mechanism
    @Test
    public void testGetURLGenerator_seriesAndBaseFallback_returnsExpectedGenerator() {
        StandardCategoryURLGenerator baseGen = new StandardCategoryURLGenerator();
        StandardCategoryURLGenerator seriesGen = new StandardCategoryURLGenerator();

        this.renderer.setBaseURLGenerator(baseGen);
        assertEquals(baseGen, this.renderer.getURLGenerator(0, 0, false));

        this.renderer.setSeriesURLGenerator(0, seriesGen);
        assertEquals(seriesGen, this.renderer.getURLGenerator(0, 0, false));
        assertEquals(baseGen, this.renderer.getURLGenerator(1, 0, false));
    }

    // Tests add and remove annotations in foreground and background layers
    @Test
    public void testAddAndRemoveAnnotation_foregroundAndBackground_removesCorrectly() {
        CategoryAnnotation a1 = new CategoryTextAnnotation("A1", "C1", 1.0);
        CategoryAnnotation a2 = new CategoryTextAnnotation("A2", "C2", 2.0);

        this.renderer.addAnnotation(a1, Layer.FOREGROUND);
        this.renderer.addAnnotation(a2, Layer.BACKGROUND);

        assertTrue(this.renderer.removeAnnotation(a1));
        assertFalse(this.renderer.removeAnnotation(a1));

        this.renderer.removeAnnotations();
        assertFalse(this.renderer.removeAnnotation(a2));
    }

    // Tests addAnnotation with null argument
    @Test(expected = IllegalArgumentException.class)
    public void testAddAnnotation_nullAnnotation_throwsIllegalArgumentException() {
        this.renderer.addAnnotation(null);
    }

    // Tests setLegendItemLabelGenerator with null argument
    @Test(expected = IllegalArgumentException.class)
    public void testSetLegendItemLabelGenerator_nullGenerator_throwsIllegalArgumentException() {
        this.renderer.setLegendItemLabelGenerator(null);
    }

    // Tests findRangeBounds with valid dataset
    @Test
    public void testFindRangeBounds_validDataset_returnsCorrectRange() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.5, "S1", "C1");
        dataset.addValue(4.5, "S1", "C2");

        Range range = this.renderer.findRangeBounds(dataset);
        assertNotNull(range);
        assertEquals(1.5, range.getLowerBound(), 0.0001);
        assertEquals(4.5, range.getUpperBound(), 0.0001);
    }

    // Tests findRangeBounds with null dataset
    @Test
    public void testFindRangeBounds_nullDataset_returnsNull() {
        assertNull(this.renderer.findRangeBounds(null));
    }

    // Tests initialise method and state setup
    @Test
    public void testInitialise_withDataset_populatesRowCountAndColumnCount() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "S1", "C1");
        dataset.addValue(2.0, "S1", "C2");
        CategoryPlot plot = new CategoryPlot(dataset, new CategoryAxis("Domain"),
                new NumberAxis("Range"), this.renderer);

        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        Rectangle2D dataArea = new Rectangle2D.Double(0, 0, 100, 100);

        CategoryItemRendererState state = this.renderer.initialise(
                g2, dataArea, plot, dataset, new PlotRenderingInfo(null));
        assertNotNull(state);
        assertEquals(1, this.renderer.getRowCount());
        assertEquals(2, this.renderer.getColumnCount());
        assertEquals(plot, this.renderer.getPlot());
    }

    // Tests drawDomainLine with null paint
    @Test(expected = IllegalArgumentException.class)
    public void testDrawDomainLine_nullPaint_throwsIllegalArgumentException() {
        BufferedImage img = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        CategoryPlot plot = new CategoryPlot();
        this.renderer.drawDomainLine(g2, plot, new Rectangle(0, 0, 50, 50), 10.0,
                null, new java.awt.BasicStroke(1.0f));
    }

    // Tests drawDomainLine with null stroke
    @Test(expected = IllegalArgumentException.class)
    public void testDrawDomainLine_nullStroke_throwsIllegalArgumentException() {
        BufferedImage img = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        CategoryPlot plot = new CategoryPlot();
        this.renderer.drawDomainLine(g2, plot, new Rectangle(0, 0, 50, 50), 10.0,
                Color.BLACK, null);
    }

    // Tests drawRangeMarker with value and interval markers
    @Test
    public void testDrawRangeMarker_valueAndIntervalMarkers_drawsWithoutException() {
        BufferedImage img = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        CategoryPlot plot = new CategoryPlot();
        plot.setOrientation(PlotOrientation.VERTICAL);
        NumberAxis axis = new NumberAxis("Range");
        axis.setRange(0.0, 100.0);
        Rectangle2D dataArea = new Rectangle2D.Double(10, 10, 180, 180);

        ValueMarker vm = new ValueMarker(50.0, Color.RED, new java.awt.BasicStroke(1.0f));
        vm.setLabel("ValueMarker");
        this.renderer.drawRangeMarker(g2, plot, axis, vm, dataArea);

        IntervalMarker im = new IntervalMarker(20.0, 40.0,
                new GradientPaint(0, 0, Color.BLUE, 100, 100, Color.GREEN));
        im.setOutlinePaint(Color.BLACK);
        im.setOutlineStroke(new java.awt.BasicStroke(1.0f));
        im.setLabel("IntervalMarker");
        this.renderer.drawRangeMarker(g2, plot, axis, im, dataArea);
    }

    // Tests equals and clone methods
    @Test
    public void testEqualsAndClone_modifiedRenderer_preservesEquality() throws CloneNotSupportedException {
        this.renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        this.renderer.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator());
        this.renderer.setBaseURLGenerator(new StandardCategoryURLGenerator());
        this.renderer.setLegendItemLabelGenerator(new StandardCategorySeriesLabelGenerator("Series {0}"));

        ConcreteCategoryItemRenderer r2 = (ConcreteCategoryItemRenderer) this.renderer.clone();
        assertTrue(this.renderer.equals(r2));
        assertEquals(this.renderer.hashCode(), r2.hashCode());
    }

    // Tests hitTest and createHotSpotBounds with missing and present data
    @Test
    public void testCreateHotSpotBounds_validAndNullValues_returnsExpectedBounds() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(10.0, "S1", "C1");
        CategoryAxis domainAxis = new CategoryAxis("Category");
        NumberAxis rangeAxis = new NumberAxis("Value");
        CategoryPlot plot = new CategoryPlot(dataset, domainAxis, rangeAxis, this.renderer);

        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        Rectangle2D dataArea = new Rectangle2D.Double(0, 0, 100, 100);

        CategoryItemRendererState state = this.renderer.initialise(g2, dataArea, plot, dataset, null);
        Rectangle2D bounds = this.renderer.createHotSpotBounds(g2, dataArea, plot,
                domainAxis, rangeAxis, dataset, 0, 0, false, state, null);
        assertNotNull(bounds);
        assertEquals(4.0, bounds.getWidth(), 0.001);
        assertEquals(4.0, bounds.getHeight(), 0.001);

        dataset.addValue(null, "S1", "C2");
        Rectangle2D nullBounds = this.renderer.createHotSpotBounds(g2, dataArea, plot,
                domainAxis, rangeAxis, dataset, 0, 1, false, state, null);
        assertNull(nullBounds);
    }
}