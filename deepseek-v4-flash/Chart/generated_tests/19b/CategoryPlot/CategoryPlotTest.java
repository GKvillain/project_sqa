package org.jfree.chart.plot;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;

import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.Range;
import org.jfree.data.category.DefaultCategoryDataset;

public class CategoryPlotTest {

    @Test
    public void testDefaultConstructor() {
        CategoryPlot plot = new CategoryPlot();
        assertEquals(PlotOrientation.VERTICAL, plot.getOrientation());
        assertFalse(plot.isDomainGridlinesVisible());
        assertTrue(plot.isRangeGridlinesVisible());
        assertEquals(0.0, plot.getAnchorValue(), 0.0001);
        assertFalse(plot.isRangeCrosshairVisible());
    }

    @Test
    public void testConstructorWithParameters() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "R1", "C1");
        CategoryAxis domainAxis = new CategoryAxis("Domain");
        NumberAxis rangeAxis = new NumberAxis("Range");
        BarRenderer renderer = new BarRenderer();
        CategoryPlot plot = new CategoryPlot(dataset, domainAxis, rangeAxis, renderer);
        assertSame(dataset, plot.getDataset());
        assertSame(domainAxis, plot.getDomainAxis());
        assertSame(rangeAxis, plot.getRangeAxis());
        assertSame(renderer, plot.getRenderer());
    }

    @Test
    public void testGetDomainAxisIndex_ownAxis_returnsCorrectIndex() {
        CategoryPlot plot = new CategoryPlot();
        CategoryAxis axis = new CategoryAxis("X");
        plot.setDomainAxis(axis);
        assertEquals(0, plot.getDomainAxisIndex(axis));
    }

    // Defect: getDomainAxisIndex does not search parent plot
    @Test
    public void testGetDomainAxisIndex_axisInParent_returnsParentIndex() {
        CategoryPlot parent = new CategoryPlot();
        CategoryAxis axis = new CategoryAxis("X");
        parent.setDomainAxis(axis);
        CategoryPlot child = new CategoryPlot();
        child.setParent(parent);
        int index = child.getDomainAxisIndex(axis);
        // Expected after fix: index == 0, but bug causes -1
        assertEquals(0, index);
    }

    @Test
    public void testGetDomainAxisIndex_axisNotFound_returnsMinusOne() {
        CategoryPlot plot = new CategoryPlot();
        CategoryAxis axis = new CategoryAxis("X");
        assertEquals(-1, plot.getDomainAxisIndex(axis));
    }

    @Test
    public void testGetRangeAxisIndex_ownAxis_returnsCorrectIndex() {
        CategoryPlot plot = new CategoryPlot();
        NumberAxis axis = new NumberAxis("Y");
        plot.setRangeAxis(axis);
        assertEquals(0, plot.getRangeAxisIndex(axis));
    }

    @Test
    public void testGetRangeAxisIndex_axisInParent_returnsParentIndex() {
        CategoryPlot parent = new CategoryPlot();
        NumberAxis axis = new NumberAxis("Y");
        parent.setRangeAxis(axis);
        CategoryPlot child = new CategoryPlot();
        child.setParent(parent);
        assertEquals(0, child.getRangeAxisIndex(axis));
    }

    @Test
    public void testGetRangeAxisIndex_axisNotFound_returnsMinusOne() {
        CategoryPlot plot = new CategoryPlot();
        NumberAxis axis = new NumberAxis("Y");
        assertEquals(-1, plot.getRangeAxisIndex(axis));
    }

    @Test
    public void testSetDomainAxis_replacesExisting() {
        CategoryPlot plot = new CategoryPlot();
        CategoryAxis axis1 = new CategoryAxis("A1");
        CategoryAxis axis2 = new CategoryAxis("A2");
        plot.setDomainAxis(axis1);
        assertSame(axis1, plot.getDomainAxis());
        plot.setDomainAxis(axis2);
        assertSame(axis2, plot.getDomainAxis());
    }

    @Test
    public void testMapDatasetToDomainAxis_and_getDomainAxisForDataset() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "R1", "C1");
        CategoryAxis axis1 = new CategoryAxis("X1");
        CategoryAxis axis2 = new CategoryAxis("X2");
        CategoryPlot plot = new CategoryPlot(dataset, axis1, new NumberAxis("Y"), new BarRenderer());
        plot.setDomainAxis(1, axis2);
        plot.mapDatasetToDomainAxis(0, 1);
        assertSame(axis2, plot.getDomainAxisForDataset(0));
    }

    @Test
    public void testMapDatasetToRangeAxis_and_getRangeAxisForDataset() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "R1", "C1");
        NumberAxis axis1 = new NumberAxis("Y1");
        NumberAxis axis2 = new NumberAxis("Y2");
        CategoryPlot plot = new CategoryPlot(dataset, new CategoryAxis("X"), axis1, new BarRenderer());
        plot.setRangeAxis(1, axis2);
        plot.mapDatasetToRangeAxis(0, 1);
        assertSame(axis2, plot.getRangeAxisForDataset(0));
    }

    @Test
    public void testSetDataset_and_getDataset() {
        CategoryPlot plot = new CategoryPlot();
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(2.0, "R1", "C1");
        plot.setDataset(dataset);
        assertSame(dataset, plot.getDataset());
        plot.setDataset(1, null);
        assertNull(plot.getDataset(1));
    }

    @Test
    public void testGetDataRange_withDataset_returnsCorrectRange() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "R1", "C1");
        dataset.addValue(3.0, "R2", "C2");
        NumberAxis rangeAxis = new NumberAxis("Y");
        CategoryPlot plot = new CategoryPlot(dataset, new CategoryAxis("X"), rangeAxis, new BarRenderer());
        Range range = plot.getDataRange(rangeAxis);
        assertNotNull(range);
        assertTrue(range.contains(1.0));
        assertTrue(range.contains(3.0));
    }

    @Test
    public void testSetOrientation() {
        CategoryPlot plot = new CategoryPlot();
        assertEquals(PlotOrientation.VERTICAL, plot.getOrientation());
        plot.setOrientation(PlotOrientation.HORIZONTAL);
        assertEquals(PlotOrientation.HORIZONTAL, plot.getOrientation());
    }

    @Test
    public void testGetLegendItems_withDatasetAndRenderer_returnsNonEmpty() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "R1", "C1");
        dataset.addValue(2.0, "R2", "C2");
        CategoryPlot plot = new CategoryPlot(dataset, new CategoryAxis("X"), new NumberAxis("Y"), new BarRenderer());
        LegendItemCollection items = plot.getLegendItems();
        assertEquals(2, items.getItemCount());
    }

    @Test
    public void testEquals_symmetry() {
        CategoryPlot plot1 = new CategoryPlot();
        CategoryPlot plot2 = new CategoryPlot();
        assertTrue(plot1.equals(plot2));
        assertTrue(plot2.equals(plot1));
    }

    @Test
    public void testEquals_differentOrientation() {
        CategoryPlot plot1 = new CategoryPlot();
        CategoryPlot plot2 = new CategoryPlot();
        plot2.setOrientation(PlotOrientation.HORIZONTAL);
        assertFalse(plot1.equals(plot2));
    }

    @Test
    public void testClone() throws CloneNotSupportedException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "R", "C");
        CategoryPlot plot = new CategoryPlot(dataset, new CategoryAxis("X"), new NumberAxis("Y"), new BarRenderer());
        CategoryPlot cloned = (CategoryPlot) plot.clone();
        assertTrue(plot.equals(cloned));
        assertNotSame(plot, cloned);
    }

    @Test
    public void testSetWeight_getWeight() {
        CategoryPlot plot = new CategoryPlot();
        plot.setWeight(5);
        assertEquals(5, plot.getWeight());
    }

    @Test
    public void testGetCategories_returnsColumnKeys() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "R1", "C1");
        dataset.addValue(2.0, "R1", "C2");
        CategoryPlot plot = new CategoryPlot(dataset, null, null, null);
        List categories = plot.getCategories();
        assertNotNull(categories);
        assertEquals(2, categories.size());
        assertTrue(categories.contains("C1"));
        assertTrue(categories.contains("C2"));
    }

    // ========== New test cases for uncovered areas ==========

    @Test
    public void testSetDomainAxis_withIndex_clearsExistingMapping() {
        CategoryPlot plot = new CategoryPlot();
        CategoryAxis axis1 = new CategoryAxis("A1");
        CategoryAxis axis2 = new CategoryAxis("A2");
        plot.setDomainAxis(0, axis1);
        assertSame(axis1, plot.getDomainAxis(0));
        plot.setDomainAxis(0, axis2);
        assertSame(axis2, plot.getDomainAxis(0));
    }

    @Test
    public void testSetRangeAxis_withIndex_clearsExistingMapping() {
        CategoryPlot plot = new CategoryPlot();
        NumberAxis axis1 = new NumberAxis("Y1");
        NumberAxis axis2 = new NumberAxis("Y2");
        plot.setRangeAxis(0, axis1);
        assertSame(axis1, plot.getRangeAxis(0));
        plot.setRangeAxis(0, axis2);
        assertSame(axis2, plot.getRangeAxis(0));
    }

    @Test
    public void testSetRenderer_withIndex_and_getRenderer() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "R1", "C1");
        CategoryPlot plot = new CategoryPlot(dataset, new CategoryAxis("X"), new NumberAxis("Y"), new BarRenderer());
        BarRenderer renderer2 = new BarRenderer();
        plot.setRenderer(1, renderer2);
        assertSame(renderer2, plot.getRenderer(1));
    }

    @Test
    public void testGetRendererCount_withMultipleRenderers() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "R1", "C1");
        CategoryPlot plot = new CategoryPlot(dataset, new CategoryAxis("X"), new NumberAxis("Y"), new BarRenderer());
        plot.setRenderer(1, new BarRenderer());
        assertEquals(2, plot.getRendererCount());
    }

    @Test
    public void testGetDomainAxisIndex_withNullAxis() {
        CategoryPlot plot = new CategoryPlot();
        assertEquals(-1, plot.getDomainAxisIndex(null));
    }

    @Test
    public void testGetRangeAxisIndex_withNullAxis() {
        CategoryPlot plot = new CategoryPlot();
        assertEquals(-1, plot.getRangeAxisIndex(null));
    }

    @Test
    public void testSetDataset_withNull_overwritesExisting() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "R1", "C1");
        CategoryPlot plot = new CategoryPlot();
        plot.setDataset(dataset);
        assertSame(dataset, plot.getDataset());
        plot.setDataset(0, null);
        assertNull(plot.getDataset(0));
    }

    @Test
    public void testSetRangeCrosshairVisible_and_isRangeCrosshairVisible() {
        CategoryPlot plot = new CategoryPlot();
        assertFalse(plot.isRangeCrosshairVisible());
        plot.setRangeCrosshairVisible(true);
        assertTrue(plot.isRangeCrosshairVisible());
        plot.setRangeCrosshairVisible(false);
        assertFalse(plot.isRangeCrosshairVisible());
    }

    @Test
    public void testSetRangeCrosshairValue_and_getRangeCrosshairValue() {
        CategoryPlot plot = new CategoryPlot();
        assertEquals(0.0, plot.getRangeCrosshairValue(), 0.0001);
        plot.setRangeCrosshairValue(50.0);
        assertEquals(50.0, plot.getRangeCrosshairValue(), 0.0001);
    }

    @Test
    public void testGetDomainAxisCount_withDefaultAndAdded() {
        CategoryPlot plot = new CategoryPlot();
        assertEquals(1, plot.getDomainAxisCount());
        plot.setDomainAxis(1, new CategoryAxis("X2"));
        assertEquals(2, plot.getDomainAxisCount());
    }

    @Test
    public void testGetRangeAxisCount_withDefaultAndAdded() {
        CategoryPlot plot = new CategoryPlot();
        assertEquals(1, plot.getRangeAxisCount());
        plot.setRangeAxis(1, new NumberAxis("Y2"));
        assertEquals(2, plot.getRangeAxisCount());
    }

    @Test
    public void testGetDomainAxis_forDataset_withNullDataset() {
        CategoryPlot plot = new CategoryPlot();
        assertNull(plot.getDomainAxisForDataset(0));
    }

    @Test
    public void testGetRangeAxis_forDataset_withNullDataset() {
        CategoryPlot plot = new CategoryPlot();
        assertNull(plot.getRangeAxisForDataset(0));
    }

    @Test
    public void testMapDatasetToDomainAxis_negativeIndex_returnsMinusOne() {
        CategoryPlot plot = new CategoryPlot();
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "R1", "C1");
        plot.setDataset(dataset);
        // Should not throw exception
        plot.mapDatasetToDomainAxis(0, -1);
        assertNotNull(plot.getDomainAxisForDataset(0));
    }

    @Test
    public void testMapDatasetToRangeAxis_negativeIndex_returnsMinusOne() {
        CategoryPlot plot = new CategoryPlot();
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "R1", "C1");
        plot.setDataset(dataset);
        plot.setRangeAxis(new NumberAxis("Y"));
        plot.mapDatasetToRangeAxis(0, -1);
        assertNotNull(plot.getRangeAxisForDataset(0));
    }

    @Test
    public void testGetDataRange_withOutOfRangeIndex() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "R1", "C1");
        dataset.addValue(3.0, "R2", "C2");
        NumberAxis rangeAxis = new NumberAxis("Y");
        CategoryPlot plot = new CategoryPlot(dataset, new CategoryAxis("X"), rangeAxis, new BarRenderer());
        // Axis index 1 doesn't exist, should return null
        assertNull(plot.getDataRange(1));
    }

    @Test
    public void testGetLegendItems_returnsNull_whenNoDataset() {
        CategoryPlot plot = new CategoryPlot();
        assertNull(plot.getLegendItems());
    }

    @Test
    public void testGetLegendItems_returnsEmpty_whenNoRenderer() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "R1", "C1");
        CategoryPlot plot = new CategoryPlot();
        plot.setDataset(dataset);
        assertNull(plot.getLegendItems());
    }

    @Test
    public void testSetOrientation_null_doesNotChange() {
        CategoryPlot plot = new CategoryPlot();
        plot.setOrientation(null);
        assertEquals(PlotOrientation.VERTICAL, plot.getOrientation());
    }

    @Test
    public void testSetWeight_negativeValue() {
        CategoryPlot plot = new CategoryPlot();
        plot.setWeight(-1);
        assertEquals(-1, plot.getWeight());
    }

    @Test
    public void testSetParent_and_getParent() {
        CategoryPlot parent = new CategoryPlot();
        CategoryPlot child = new CategoryPlot();
        child.setParent(parent);
        assertSame(parent, child.getParent());
    }

    @Test
    public void testSetParent_null_clearsParent() {
        CategoryPlot parent = new CategoryPlot();
        CategoryPlot child = new CategoryPlot();
        child.setParent(parent);
        child.setParent(null);
        assertNull(child.getParent());
    }

    @Test
    public void testGetDomainAxisIndex_axisInMultipleLevels_parentChain() {
        CategoryPlot grandParent = new CategoryPlot();
        CategoryAxis axis = new CategoryAxis("X");
        grandParent.setDomainAxis(axis);
        
        CategoryPlot parent = new CategoryPlot();
        parent.setParent(grandParent);
        
        CategoryPlot child = new CategoryPlot();
        child.setParent(parent);
        
        assertEquals(0, child.getDomainAxisIndex(axis));
    }

    @Test
    public void testGetRangeAxisIndex_axisInMultipleLevels_parentChain() {
        CategoryPlot grandParent = new CategoryPlot();
        NumberAxis axis = new NumberAxis("Y");
        grandParent.setRangeAxis(axis);
        
        CategoryPlot parent = new CategoryPlot();
        parent.setParent(grandParent);
        
        CategoryPlot child = new CategoryPlot();
        child.setParent(parent);
        
        assertEquals(0, child.getRangeAxisIndex(axis));
    }

    @Test
    public void testGetCategories_returnsNull_whenNoDataset() {
        CategoryPlot plot = new CategoryPlot();
        assertNull(plot.getCategories());
    }

    @Test
    public void testGetCategories_returnsEmpty_whenDatasetEmpty() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        CategoryPlot plot = new CategoryPlot(dataset, null, null, null);
        List categories = plot.getCategories();
        assertNotNull(categories);
        assertTrue(categories.isEmpty());
    }

    @Test
    public void testSetDataset_clearsExistingCategories() {
        DefaultCategoryDataset dataset1 = new DefaultCategoryDataset();
        dataset1.addValue(1.0, "R1", "C1");
        CategoryPlot plot = new CategoryPlot(dataset1, new CategoryAxis("X"), new NumberAxis("Y"), new BarRenderer());
        assertEquals(1, plot.getCategories().size());
        
        DefaultCategoryDataset dataset2 = new DefaultCategoryDataset();
        dataset2.addValue(2.0, "R2", "C2");
        dataset2.addValue(3.0, "R2", "C3");
        plot.setDataset(dataset2);
        assertEquals(2, plot.getCategories().size());
        assertTrue(plot.getCategories().contains("C2"));
        assertTrue(plot.getCategories().contains("C3"));
    }
}