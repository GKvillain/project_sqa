package org.jfree.chart.renderer.category;

import static org.junit.Assert.*;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.annotations.CategoryAnnotation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryCrosshairState;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.urls.CategoryURLGenerator;
import org.jfree.chart.urls.StandardCategoryURLGenerator;
import org.jfree.chart.util.Layer;
import org.jfree.data.Range;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.category.CategoryDataset;
import org.junit.Before;
import org.junit.Test;

public class AbstractCategoryItemRendererTest {

    private TestRenderer renderer;
    private CategoryPlot plot;
    private DefaultCategoryDataset dataset;
    private CategoryAxis xAxis;
    private NumberAxis yAxis;

    private static class TestRenderer extends AbstractCategoryItemRenderer {
        @Override
        public void drawItem(Graphics2D g2, CategoryItemRendererState state,
                             Rectangle2D dataArea, CategoryPlot plot,
                             CategoryAxis domainAxis, ValueAxis rangeAxis,
                             CategoryDataset dataset, int row, int column, int pass) {
            // no-op
        }
    }

    @Before
    public void setUp() {
        renderer = new TestRenderer();
        xAxis = new CategoryAxis("X");
        yAxis = new NumberAxis("Y");
        dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "R1", "C1");
        dataset.addValue(2.0, "R2", "C1");
        plot = new CategoryPlot(dataset, xAxis, yAxis, renderer);
    }

    // Test getLegendItems when plot is null -> empty collection
    @Test
    public void testGetLegendItems_nullPlot_returnsEmptyCollection() {
        TestRenderer r = new TestRenderer();
        LegendItemCollection items = r.getLegendItems();
        assertTrue(items.getItemCount() == 0);
    }

    // Test getLegendItems when dataset is not null -> non-empty collection (defect detection)
    @Test
    public void testGetLegendItems_datasetNotNull_returnsNonEmptyCollection() {
        LegendItemCollection items = renderer.getLegendItems();
        assertTrue(items.getItemCount() > 0);
    }

    // Test getLegendItems when dataset is null -> empty collection (defect detection)
    @Test
    public void testGetLegendItems_datasetNull_returnsEmptyCollection() {
        CategoryPlot p = new CategoryPlot(null, xAxis, yAxis, new TestRenderer());
        CategoryItemRenderer r = p.getRenderer();
        LegendItemCollection items = r.getLegendItems();
        assertTrue(items.getItemCount() == 0);
    }

    @Test
    public void testSetPlot_assignsPlot() {
        TestRenderer r = new TestRenderer();
        CategoryPlot p = new CategoryPlot(dataset, xAxis, yAxis, r);
        assertSame(p, r.getPlot());
    }

    @Test
    public void testGetPlot_returnsPlot() {
        assertNotNull(renderer.getPlot());
        assertSame(plot, renderer.getPlot());
    }

    @Test
    public void testGetItemLabelGenerator_seriesSet_returnsSeriesGenerator() {
        CategoryItemLabelGenerator gen = new StandardCategoryItemLabelGenerator();
        renderer.setSeriesItemLabelGenerator(0, gen);
        assertSame(gen, renderer.getSeriesItemLabelGenerator(0));
    }

    @Test
    public void testGetItemLabelGenerator_usesBaseIfNoSeries() {
        CategoryItemLabelGenerator gen = new StandardCategoryItemLabelGenerator();
        renderer.setBaseItemLabelGenerator(gen);
        assertSame(gen, renderer.getItemLabelGenerator(0, 0, false));
    }

    @Test
    public void testGetToolTipGenerator_seriesSet_returnsSeriesGenerator() {
        CategoryToolTipGenerator gen = new StandardCategoryToolTipGenerator();
        renderer.setSeriesToolTipGenerator(0, gen);
        assertSame(gen, renderer.getSeriesToolTipGenerator(0));
    }

    @Test
    public void testGetURLGenerator_seriesSet_returnsSeriesGenerator() {
        CategoryURLGenerator gen = new StandardCategoryURLGenerator();
        renderer.setSeriesURLGenerator(0, gen);
        assertSame(gen, renderer.getSeriesURLGenerator(0));
    }

    @Test
    public void testAddAnnotation_foregroundLayer_addsToList() {
        CategoryAnnotation ann = new CategoryAnnotation() {
            @Override
            public void draw(Graphics2D g2, CategoryPlot plot, Rectangle2D dataArea,
                             CategoryAxis domainAxis, ValueAxis rangeAxis,
                             int rendererIndex, PlotRenderingInfo info) {}
        };
        renderer.addAnnotation(ann, Layer.FOREGROUND);
        assertTrue(renderer.removeAnnotation(ann));
    }

    @Test
    public void testRemoveAnnotation_removesFromBoth() {
        CategoryAnnotation ann = new CategoryAnnotation() {
            @Override
            public void draw(Graphics2D g2, CategoryPlot plot, Rectangle2D dataArea,
                             CategoryAxis domainAxis, ValueAxis rangeAxis,
                             int rendererIndex, PlotRenderingInfo info) {}
        };
        renderer.addAnnotation(ann, Layer.FOREGROUND);
        assertTrue(renderer.removeAnnotation(ann));
        assertFalse(renderer.removeAnnotation(ann)); // already removed

        // background layer
        renderer.addAnnotation(ann, Layer.BACKGROUND);
        assertTrue(renderer.removeAnnotation(ann));
    }

    @Test
    public void testFindRangeBounds_nullDataset_returnsNull() {
        assertNull(renderer.findRangeBounds(null));
    }

    @Test
    public void testFindRangeBounds_withDataset_returnsCorrectRange() {
        Range r = renderer.findRangeBounds(dataset);
        assertNotNull(r);
        assertEquals(1.0, r.getLowerBound(), 0.0001);
        assertEquals(2.0, r.getUpperBound(), 0.0001);
    }

    @Test
    public void testEquals_sameInstance_true() {
        assertTrue(renderer.equals(renderer));
    }

    @Test
    public void testEquals_differentBaseItemLabelGenerator_false() {
        TestRenderer other = new TestRenderer();
        renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        other.setBaseItemLabelGenerator(null);
        assertFalse(renderer.equals(other));
    }

    @Test
    public void testClone_basicClone() throws CloneNotSupportedException {
        TestRenderer cloned = (TestRenderer) renderer.clone();
        assertNotNull(cloned);
        assertNotSame(renderer, cloned);
        assertEquals(renderer, cloned);
    }

    @Test
    public void testGetDrawingSupplier_plotNull_returnsNull() {
        TestRenderer r = new TestRenderer();
        assertNull(r.getDrawingSupplier());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateCrosshairState_nullOrientation_throwsIllegalArgumentException() {
        renderer.updateCrosshairValues(null, "R1", "C1", 1.0, 0, 50, 50, null);
    }

    @Test
    public void testGetPassCount_default_returnsOne() {
        assertEquals(1, renderer.getPassCount());
    }

    @Test
    public void testGetLegendItem_seriesNotVisible_returnsNull() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        assertNull(renderer.getLegendItem(0, 0));
    }

    @Test
    public void testGetRowCount_afterInitialise() {
        renderer.initialise(null, new Rectangle2D.Double(0, 0, 100, 100),
                            plot, dataset, null);
        assertEquals(2, renderer.getRowCount());
        assertEquals(1, renderer.getColumnCount());
    }
}