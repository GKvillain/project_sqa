package org.jfree.chart.plot;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.event.PlotChangeListener;
import org.jfree.chart.util.TableOrder;
import org.jfree.data.category.DefaultCategoryDataset;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link MultiplePiePlot}.
 */
public class MultiplePiePlotTest implements PlotChangeListener {

    private PlotChangeEvent lastEvent;

    @Before
    public void setUp() {
        this.lastEvent = null;
    }

    public void plotChanged(PlotChangeEvent event) {
        this.lastEvent = event;
    }

    // Tests constructor with dataset properly registers listener (defect detection for Chart-12)
    @Test
    public void testConstructor_withDataset_registersListener() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "Row 1", "Col 1");
        MultiplePiePlot plot = new MultiplePiePlot(dataset);
        plot.addChangeListener(this);
        this.lastEvent = null;

        dataset.addValue(2.0, "Row 1", "Col 1");

        assertNotNull("Plot should receive change event when dataset changes", this.lastEvent);
    }

    // Tests default constructor initializes default values
    @Test
    public void testConstructor_default_initializesDefaults() {
        MultiplePiePlot plot = new MultiplePiePlot();
        assertNull(plot.getDataset());
        assertEquals(TableOrder.BY_COLUMN, plot.getDataExtractOrder());
        assertEquals(0.0, plot.getLimit(), 0.000001);
        assertEquals("Other", plot.getAggregatedItemsKey());
        assertEquals(Color.lightGray, plot.getAggregatedItemsPaint());
        assertNotNull(plot.getPieChart());
        assertEquals("Multiple Pie Plot", plot.getPlotType());
    }

    // Tests setDataset updates dataset and triggers change event
    @Test
    public void testSetDataset_validDataset_updatesAndFiresEvent() {
        MultiplePiePlot plot = new MultiplePiePlot();
        plot.addChangeListener(this);

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(10.0, "R1", "C1");
        plot.setDataset(dataset);

        assertEquals(dataset, plot.getDataset());
        assertNotNull(this.lastEvent);

        this.lastEvent = null;
        dataset.addValue(20.0, "R1", "C2");
        assertNotNull(this.lastEvent);
    }

    // Tests setDataset with null unregisters previous dataset listener
    @Test
    public void testSetDataset_null_removesOldListener() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        MultiplePiePlot plot = new MultiplePiePlot(dataset);
        plot.setDataset(null);
        plot.addChangeListener(this);
        this.lastEvent = null;

        dataset.addValue(10.0, "R1", "C1");
        assertNull(this.lastEvent);
        assertNull(plot.getDataset());
    }

    // Tests setPieChart with valid chart
    @Test
    public void testSetPieChart_validPieChart_updatesChart() {
        MultiplePiePlot plot = new MultiplePiePlot();
        PiePlot newPiePlot = new PiePlot();
        JFreeChart newChart = new JFreeChart(newPiePlot);

        plot.addChangeListener(this);
        plot.setPieChart(newChart);

        assertEquals(newChart, plot.getPieChart());
        assertNotNull(this.lastEvent);
    }

    // Tests setPieChart with null argument throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testSetPieChart_null_throwsException() {
        MultiplePiePlot plot = new MultiplePiePlot();
        plot.setPieChart(null);
    }

    // Tests setPieChart with non-PiePlot chart throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testSetPieChart_nonPiePlot_throwsException() {
        MultiplePiePlot plot = new MultiplePiePlot();
        CategoryPlot categoryPlot = new CategoryPlot();
        JFreeChart chart = new JFreeChart(categoryPlot);
        plot.setPieChart(chart);
    }

    // Tests setDataExtractOrder with valid and null arguments
    @Test
    public void testSetDataExtractOrder_validOrder_updatesOrder() {
        MultiplePiePlot plot = new MultiplePiePlot();
        plot.addChangeListener(this);

        plot.setDataExtractOrder(TableOrder.BY_ROW);
        assertEquals(TableOrder.BY_ROW, plot.getDataExtractOrder());
        assertNotNull(this.lastEvent);
    }

    // Tests setDataExtractOrder with null throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testSetDataExtractOrder_null_throwsException() {
        MultiplePiePlot plot = new MultiplePiePlot();
        plot.setDataExtractOrder(null);
    }

    // Tests setLimit updates limit value and triggers event
    @Test
    public void testSetLimit_validLimit_updatesLimit() {
        MultiplePiePlot plot = new MultiplePiePlot();
        plot.addChangeListener(this);

        plot.setLimit(0.15);
        assertEquals(0.15, plot.getLimit(), 0.000001);
        assertNotNull(this.lastEvent);
    }

    // Tests setAggregatedItemsKey with valid key
    @Test
    public void testSetAggregatedItemsKey_validKey_updatesKey() {
        MultiplePiePlot plot = new MultiplePiePlot();
        plot.addChangeListener(this);

        plot.setAggregatedItemsKey("Miscellaneous");
        assertEquals("Miscellaneous", plot.getAggregatedItemsKey());
        assertNotNull(this.lastEvent);
    }

    // Tests setAggregatedItemsKey with null throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testSetAggregatedItemsKey_null_throwsException() {
        MultiplePiePlot plot = new MultiplePiePlot();
        plot.setAggregatedItemsKey(null);
    }

    // Tests setAggregatedItemsPaint with valid paint
    @Test
    public void testSetAggregatedItemsPaint_validPaint_updatesPaint() {
        MultiplePiePlot plot = new MultiplePiePlot();
        plot.addChangeListener(this);

        plot.setAggregatedItemsPaint(Color.red);
        assertEquals(Color.red, plot.getAggregatedItemsPaint());
        assertNotNull(this.lastEvent);
    }

    // Tests setAggregatedItemsPaint with null throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testSetAggregatedItemsPaint_null_throwsException() {
        MultiplePiePlot plot = new MultiplePiePlot();
        plot.setAggregatedItemsPaint(null);
    }

    // Tests equals method symmetry and difference detection
    @Test
    public void testEquals_variousConditions_returnsExpectedResult() {
        MultiplePiePlot p1 = new MultiplePiePlot();
        MultiplePiePlot p2 = new MultiplePiePlot();

        assertTrue(p1.equals(p1));
        assertFalse(p1.equals(null));
        assertFalse(p1.equals("Not a MultiplePiePlot"));
        assertTrue(p1.equals(p2));
        assertTrue(p2.equals(p1));

        p1.setDataExtractOrder(TableOrder.BY_ROW);
        assertFalse(p1.equals(p2));
        p2.setDataExtractOrder(TableOrder.BY_ROW);
        assertTrue(p1.equals(p2));

        p1.setLimit(0.20);
        assertFalse(p1.equals(p2));
        p2.setLimit(0.20);
        assertTrue(p1.equals(p2));

        p1.setAggregatedItemsKey("Other Key");
        assertFalse(p1.equals(p2));
        p2.setAggregatedItemsKey("Other Key");
        assertTrue(p1.equals(p2));

        p1.setAggregatedItemsPaint(Color.blue);
        assertFalse(p1.equals(p2));
        p2.setAggregatedItemsPaint(Color.blue);
        assertTrue(p1.equals(p2));
    }

    // Tests getLegendItems with null dataset and populated dataset
    @Test
    public void testGetLegendItems_byRowAndByColumn_returnsCorrectItems() {
        MultiplePiePlot plot = new MultiplePiePlot();
        LegendItemCollection legend = plot.getLegendItems();
        assertEquals(0, legend.getItemCount());

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(10.0, "R1", "C1");
        dataset.addValue(20.0, "R1", "C2");
        dataset.addValue(30.0, "R2", "C1");
        dataset.addValue(40.0, "R2", "C2");
        plot.setDataset(dataset);

        plot.setDataExtractOrder(TableOrder.BY_COLUMN);
        legend = plot.getLegendItems();
        assertEquals(2, legend.getItemCount());

        plot.setDataExtractOrder(TableOrder.BY_ROW);
        legend = plot.getLegendItems();
        assertEquals(2, legend.getItemCount());

        plot.setLimit(0.10);
        legend = plot.getLegendItems();
        assertEquals(3, legend.getItemCount());
    }

    // Tests drawing plot with null dataset and with data in both orientations
    @Test
    public void testDraw_validData_rendersWithoutException() {
        BufferedImage image = new BufferedImage(400, 300, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        Rectangle2D area = new Rectangle2D.Double(0, 0, 400, 300);

        MultiplePiePlot plot = new MultiplePiePlot();
        plot.draw(g2, area, null, null, null);

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(10.0, "Row 1", "Col 1");
        dataset.addValue(5.0, "Row 1", "Col 2");
        dataset.addValue(1.0, "Row 1", "Col 3");
        dataset.addValue(15.0, "Row 2", "Col 1");
        dataset.addValue(25.0, "Row 2", "Col 2");
        dataset.addValue(2.0, "Row 2", "Col 3");

        plot.setDataset(dataset);
        plot.setLimit(0.10);
        plot.setDataExtractOrder(TableOrder.BY_ROW);

        ChartRenderingInfo info = new ChartRenderingInfo();
        plot.draw(g2, area, null, null, info.getPlotInfo());

        Rectangle2D tallArea = new Rectangle2D.Double(0, 0, 200, 500);
        plot.setDataExtractOrder(TableOrder.BY_COLUMN);
        plot.draw(g2, tallArea, null, null, null);

        g2.dispose();
    }

    // Tests serialization and deserialization
    @Test
    public void testSerialization_roundTrip_preservesEquality() throws Exception {
        MultiplePiePlot p1 = new MultiplePiePlot();
        p1.setLimit(0.15);
        p1.setAggregatedItemsKey("Aggregated");
        p1.setAggregatedItemsPaint(Color.blue);

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(buffer);
        out.writeObject(p1);
        out.close();

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        MultiplePiePlot p2 = (MultiplePiePlot) in.readObject();
        in.close();

        assertEquals(p1, p2);
    }
}