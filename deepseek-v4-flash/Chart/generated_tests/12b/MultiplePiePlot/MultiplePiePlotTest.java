package org.jfree.chart.plot;

import org.junit.Test;
import static org.junit.Assert.*;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.event.PlotChangeListener;
import org.jfree.chart.util.TableOrder;
import org.jfree.data.category.DefaultCategoryDataset;
import java.awt.Color;
import java.awt.Paint;

public class MultiplePiePlotTest {

    private static class MyPlotChangeListener implements PlotChangeListener {
        private boolean changed = false;
        public void plotChanged(PlotChangeEvent event) {
            changed = true;
        }
        public boolean isChanged() { return changed; }
        public void reset() { changed = false; }
    }

    // Tests default constructor creates a plot with null dataset
    @Test
    public void testConstructor_default_createsPlotWithNoDataset() {
        MultiplePiePlot plot = new MultiplePiePlot();
        assertNull(plot.getDataset());
    }

    // Tests constructor with dataset stores dataset (checks listener registration)
    @Test
    public void testConstructor_withDataset_shouldRegisterListener() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        MultiplePiePlot plot = new MultiplePiePlot(dataset);
        MyPlotChangeListener listener = new MyPlotChangeListener();
        plot.addChangeListener(listener);
        dataset.addValue(1.0, "R1", "C1");
        assertTrue("Plot should fire change event after dataset change", listener.isChanged());
    }

    // Tests setDataset with null dataset fires event and clears dataset
    @Test
    public void testSetDataset_nullDataset_firesEventAndClears() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        MultiplePiePlot plot = new MultiplePiePlot(dataset);
        MyPlotChangeListener listener = new MyPlotChangeListener();
        plot.addChangeListener(listener);
        plot.setDataset(null);
        assertTrue("setDataset(null) should fire plot change event", listener.isChanged());
        assertNull(plot.getDataset());
    }

    // Tests setDataset with new dataset registers listener and responds to changes
    @Test
    public void testSetDataset_withDataset_registersListener() {
        MultiplePiePlot plot = new MultiplePiePlot(null);
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        MyPlotChangeListener listener = new MyPlotChangeListener();
        plot.addChangeListener(listener);
        plot.setDataset(dataset);
        listener.reset();
        dataset.addValue(2.0, "R", "C");
        assertTrue("Plot should fire change event after dataset change", listener.isChanged());
    }

    // Tests setPieChart with null argument throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testSetPieChart_nullArgument_throwsException() {
        MultiplePiePlot plot = new MultiplePiePlot();
        plot.setPieChart(null);
    }

    // Tests setPieChart with valid PiePlot
    @Test
    public void testSetPieChart_validPiePlot_accepts() {
        MultiplePiePlot plot = new MultiplePiePlot();
        JFreeChart chart = new JFreeChart(new PiePlot());
        plot.setPieChart(chart);
        assertSame(chart, plot.getPieChart());
    }

    // Tests setDataExtractOrder with null throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testSetDataExtractOrder_null_throwsException() {
        MultiplePiePlot plot = new MultiplePiePlot();
        plot.setDataExtractOrder(null);
    }

    // Tests setDataExtractOrder with valid order
    @Test
    public void testSetDataExtractOrder_validOrder_accepts() {
        MultiplePiePlot plot = new MultiplePiePlot();
        plot.setDataExtractOrder(TableOrder.BY_ROW);
        assertEquals(TableOrder.BY_ROW, plot.getDataExtractOrder());
    }

    // Tests setLimit with zero value
    @Test
    public void testSetLimit_zeroValue_accepts() {
        MultiplePiePlot plot = new MultiplePiePlot();
        plot.setLimit(0.0);
        assertEquals(0.0, plot.getLimit(), 0.0001);
    }

    // Tests setLimit with positive value
    @Test
    public void testSetLimit_positiveValue_accepts() {
        MultiplePiePlot plot = new MultiplePiePlot();
        plot.setLimit(0.25);
        assertEquals(0.25, plot.getLimit(), 0.0001);
    }

    // Tests setAggregatedItemsKey with null throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testSetAggregatedItemsKey_null_throwsException() {
        MultiplePiePlot plot = new MultiplePiePlot();
        plot.setAggregatedItemsKey(null);
    }

    // Tests setAggregatedItemsPaint with null throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testSetAggregatedItemsPaint_null_throwsException() {
        MultiplePiePlot plot = new MultiplePiePlot();
        plot.setAggregatedItemsPaint(null);
    }

    // Tests getPlotType returns correct string
    @Test
    public void testGetPlotType_returnsCorrectString() {
        MultiplePiePlot plot = new MultiplePiePlot();
        assertEquals("Multiple Pie Plot", plot.getPlotType());
    }

    // Tests getLegendItems with null dataset returns empty
    @Test
    public void testGetLegendItems_nullDataset_returnsEmpty() {
        MultiplePiePlot plot = new MultiplePiePlot(null);
        assertTrue(plot.getLegendItems().getItemCount() == 0);
    }

    // Tests getLegendItems with dataset and no limit
    @Test
    public void testGetLegendItems_withDatasetNoLimit_returnsItems() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "R1", "C1");
        dataset.addValue(2.0, "R2", "C1");
        MultiplePiePlot plot = new MultiplePiePlot(dataset);
        assertEquals(2, plot.getLegendItems().getItemCount());
    }

    // Tests getLegendItems with dataset and limit > 0 includes aggregated item
    @Test
    public void testGetLegendItems_withDatasetAndLimit_includesAggregated() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "R1", "C1");
        dataset.addValue(50.0, "R2", "C1");
        dataset.addValue(50.0, "R3", "C1");
        MultiplePiePlot plot = new MultiplePiePlot(dataset);
        plot.setLimit(0.2);
        assertEquals(3, plot.getLegendItems().getItemCount());
    }

    // Tests equals with same object
    @Test
    public void testEquals_sameObject_returnsTrue() {
        MultiplePiePlot plot = new MultiplePiePlot();
        assertTrue(plot.equals(plot));
    }

    // Tests equals with null
    @Test
    public void testEquals_null_returnsFalse() {
        MultiplePiePlot plot = new MultiplePiePlot();
        assertFalse(plot.equals(null));
    }

    // Tests equals with different dataExtractOrder
    @Test
    public void testEquals_differentDataExtractOrder_returnsFalse() {
        MultiplePiePlot plot1 = new MultiplePiePlot();
        MultiplePiePlot plot2 = new MultiplePiePlot();
        plot2.setDataExtractOrder(TableOrder.BY_ROW);
        assertFalse(plot1.equals(plot2));
    }

    // Tests equals with different limit
    @Test
    public void testEquals_differentLimit_returnsFalse() {
        MultiplePiePlot plot1 = new MultiplePiePlot();
        MultiplePiePlot plot2 = new MultiplePiePlot();
        plot2.setLimit(0.5);
        assertFalse(plot1.equals(plot2));
    }

    // Tests equals with different aggregatedItemsKey
    @Test
    public void testEquals_differentAggregatedItemsKey_returnsFalse() {
        MultiplePiePlot plot1 = new MultiplePiePlot();
        MultiplePiePlot plot2 = new MultiplePiePlot();
        plot2.setAggregatedItemsKey("Misc");
        assertFalse(plot1.equals(plot2));
    }

    // Tests equals with different aggregatedItemsPaint
    @Test
    public void testEquals_differentAggregatedItemsPaint_returnsFalse() {
        MultiplePiePlot plot1 = new MultiplePiePlot();
        MultiplePiePlot plot2 = new MultiplePiePlot();
        plot2.setAggregatedItemsPaint(Color.RED);
        assertFalse(plot1.equals(plot2));
    }

    // Tests equals with different pieChart
    @Test
    public void testEquals_differentPieChart_returnsFalse() {
        MultiplePiePlot plot1 = new MultiplePiePlot();
        MultiplePiePlot plot2 = new MultiplePiePlot();
        JFreeChart newChart = new JFreeChart(new PiePlot());
        newChart.setTitle("Different");
        plot2.setPieChart(newChart);
        assertFalse(plot1.equals(plot2));
    }

    // ===== Additional tests to improve coverage =====

    @Test
    public void testGetDataset_defaultConstructor_returnsNull() {
        MultiplePiePlot plot = new MultiplePiePlot();
        assertNull(plot.getDataset());
    }

    @Test
    public void testGetDataset_afterConstructorWithDataset_returnsDataset() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        MultiplePiePlot plot = new MultiplePiePlot(dataset);
        assertSame(dataset, plot.getDataset());
    }

    @Test
    public void testSetPieChart_firesPlotChangeEvent() {
        MultiplePiePlot plot = new MultiplePiePlot();
        MyPlotChangeListener listener = new MyPlotChangeListener();
        plot.addChangeListener(listener);
        JFreeChart chart = new JFreeChart(new PiePlot());
        plot.setPieChart(chart);
        assertTrue("setPieChart should fire event", listener.isChanged());
    }

    @Test
    public void testSetDataExtractOrder_firesPlotChangeEvent() {
        MultiplePiePlot plot = new MultiplePiePlot();
        MyPlotChangeListener listener = new MyPlotChangeListener();
        plot.addChangeListener(listener);
        plot.setDataExtractOrder(TableOrder.BY_ROW);
        assertTrue("setDataExtractOrder should fire event", listener.isChanged());
    }

    @Test
    public void testSetLimit_firesPlotChangeEvent() {
        MultiplePiePlot plot = new MultiplePiePlot();
        MyPlotChangeListener listener = new MyPlotChangeListener();
        plot.addChangeListener(listener);
        plot.setLimit(0.5);
        assertTrue("setLimit should fire event", listener.isChanged());
    }

    @Test
    public void testSetAggregatedItemsKey_firesPlotChangeEvent() {
        MultiplePiePlot plot = new MultiplePiePlot();
        MyPlotChangeListener listener = new MyPlotChangeListener();
        plot.addChangeListener(listener);
        plot.setAggregatedItemsKey("Misc");
        assertTrue("setAggregatedItemsKey should fire event", listener.isChanged());
    }

    @Test
    public void testSetAggregatedItemsPaint_firesPlotChangeEvent() {
        MultiplePiePlot plot = new MultiplePiePlot();
        MyPlotChangeListener listener = new MyPlotChangeListener();
        plot.addChangeListener(listener);
        plot.setAggregatedItemsPaint(Color.RED);
        assertTrue("setAggregatedItemsPaint should fire event", listener.isChanged());
    }

    @Test
    public void testSetAggregatedItemsKey_validKey_storesKey() {
        MultiplePiePlot plot = new MultiplePiePlot();
        plot.setAggregatedItemsKey("Misc");
        assertEquals("Misc", plot.getAggregatedItemsKey());
    }

    @Test
    public void testSetAggregatedItemsPaint_validPaint_storesPaint() {
        MultiplePiePlot plot = new MultiplePiePlot();
        plot.setAggregatedItemsPaint(Color.BLUE);
        assertEquals(Color.BLUE, plot.getAggregatedItemsPaint());
    }

    @Test
    public void testEquals_differentDataset_returnsFalse() {
        DefaultCategoryDataset dataset1 = new DefaultCategoryDataset();
        dataset1.addValue(1.0, "R1", "C1");
        DefaultCategoryDataset dataset2 = new DefaultCategoryDataset();
        dataset2.addValue(2.0, "R2", "C2");
        MultiplePiePlot plot1 = new MultiplePiePlot(dataset1);
        MultiplePiePlot plot2 = new MultiplePiePlot(dataset2);
        assertFalse(plot1.equals(plot2));
    }

    @Test
    public void testClone_createsIndependentCopy() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "R1", "C1");
        MultiplePiePlot plot1 = new MultiplePiePlot(dataset);
        try {
            MultiplePiePlot plot2 = (MultiplePiePlot) plot1.clone();
            assertNotSame(plot1, plot2);
            assertTrue(plot1.equals(plot2));
            // modify original dataset
            dataset.addValue(3.0, "R2", "C1");
            assertFalse(plot1.equals(plot2));
        } catch (CloneNotSupportedException e) {
            fail("Clone should be supported");
        }
    }

    @Test
    public void testGetPieChart_defaultNotNull() {
        MultiplePiePlot plot = new MultiplePiePlot();
        assertNotNull(plot.getPieChart());
    }

    @Test
    public void testGetDataExtractOrder_default() {
        MultiplePiePlot plot = new MultiplePiePlot();
        assertNotNull(plot.getDataExtractOrder());
    }

    @Test
    public void testGetLimit_default() {
        MultiplePiePlot plot = new MultiplePiePlot();
        assertEquals(0.0, plot.getLimit(), 0.0001);
    }

    @Test
    public void testGetAggregatedItemsKey_default() {
        MultiplePiePlot plot = new MultiplePiePlot();
        assertNotNull(plot.getAggregatedItemsKey());
    }

    @Test
    public void testGetAggregatedItemsPaint_default() {
        MultiplePiePlot plot = new MultiplePiePlot();
        assertNotNull(plot.getAggregatedItemsPaint());
    }
}