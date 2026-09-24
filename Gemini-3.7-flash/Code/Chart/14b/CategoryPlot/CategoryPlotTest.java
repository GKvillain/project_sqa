package org.jfree.chart.plot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.Collection;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.util.Layer;
import org.jfree.chart.util.RectangleInsets;
import org.jfree.data.category.DefaultCategoryDataset;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CategoryPlotTest {

    private CategoryPlot plot;

    @Before
    public void setUp() {
        this.plot = new CategoryPlot();
    }

    // Tests removing a domain marker when no domain markers exist for that layer (bug Chart-14)
    @Test
    public void testRemoveDomainMarker_nonExistingMarker_returnsFalse() {
        CategoryMarker marker = new CategoryMarker("Category 1");
        boolean removed = this.plot.removeDomainMarker(marker, Layer.FOREGROUND);
        assertFalse(removed);
    }

    // Tests removing a range marker when no range markers exist for foreground (bug Chart-14)
    @Test
    public void testRemoveRangeMarker_nonExistingMarker_returnsFalse() {
        ValueMarker marker = new ValueMarker(50.0);
        boolean removed = this.plot.removeRangeMarker(marker, Layer.FOREGROUND);
        assertFalse(removed);
    }

    // Tests removing a domain marker for a secondary index that has no markers
    @Test
    public void testRemoveDomainMarker_secondaryIndexWithoutMarkers_returnsFalse() {
        CategoryMarker marker = new CategoryMarker("Category 1");
        boolean removed = this.plot.removeDomainMarker(1, marker, Layer.BACKGROUND);
        assertFalse(removed);
    }

    // Tests removing a range marker for a secondary index that has no markers
    @Test
    public void testRemoveRangeMarker_secondaryIndexWithoutMarkers_returnsFalse() {
        ValueMarker marker = new ValueMarker(25.0);
        boolean removed = this.plot.removeRangeMarker(1, marker, Layer.BACKGROUND);
        assertFalse(removed);
    }

    // Tests adding and removing an existing domain marker in foreground layer
    @Test
    public void testRemoveDomainMarker_existingMarker_returnsTrue() {
        CategoryMarker marker = new CategoryMarker("Category 1");
        this.plot.addDomainMarker(marker, Layer.FOREGROUND);
        Collection markers = this.plot.getDomainMarkers(Layer.FOREGROUND);
        assertTrue(markers.contains(marker));

        boolean removed = this.plot.removeDomainMarker(marker, Layer.FOREGROUND);
        assertTrue(removed);

        Collection markersAfter = this.plot.getDomainMarkers(Layer.FOREGROUND);
        assertFalse(markersAfter.contains(marker));
    }

    // Tests adding and removing an existing range marker in foreground layer
    @Test
    public void testRemoveRangeMarker_existingMarker_returnsTrue() {
        ValueMarker marker = new ValueMarker(100.0);
        this.plot.addRangeMarker(marker, Layer.FOREGROUND);
        Collection markers = this.plot.getRangeMarkers(Layer.FOREGROUND);
        assertTrue(markers.contains(marker));

        boolean removed = this.plot.removeRangeMarker(marker, Layer.FOREGROUND);
        assertTrue(removed);

        Collection markersAfter = this.plot.getRangeMarkers(Layer.FOREGROUND);
        assertFalse(markersAfter.contains(marker));
    }

    // Tests removing null range marker throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testRemoveRangeMarker_nullMarker_throwsException() {
        this.plot.removeRangeMarker(null);
    }

    // Tests adding null domain marker throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testAddDomainMarker_nullMarker_throwsException() {
        this.plot.addDomainMarker(null);
    }

    // Tests adding null range marker throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testAddRangeMarker_nullMarker_throwsException() {
        this.plot.addRangeMarker(0, null, Layer.FOREGROUND);
    }

    // Tests clearing domain markers
    @Test
    public void testClearDomainMarkers_clearsAllMarkers() {
        CategoryMarker marker1 = new CategoryMarker("Cat1");
        CategoryMarker marker2 = new CategoryMarker("Cat2");
        this.plot.addDomainMarker(marker1, Layer.FOREGROUND);
        this.plot.addDomainMarker(marker2, Layer.BACKGROUND);

        this.plot.clearDomainMarkers();

        Collection fgMarkers = this.plot.getDomainMarkers(Layer.FOREGROUND);
        Collection bgMarkers = this.plot.getDomainMarkers(Layer.BACKGROUND);
        assertTrue(fgMarkers == null || fgMarkers.isEmpty());
        assertTrue(bgMarkers == null || bgMarkers.isEmpty());
    }

    // Tests clearing range markers for a specific index
    @Test
    public void testClearRangeMarkers_index_clearsMarkersForIndex() {
        ValueMarker marker = new ValueMarker(15.0);
        this.plot.addRangeMarker(0, marker, Layer.FOREGROUND);
        this.plot.clearRangeMarkers(0);

        Collection fgMarkers = this.plot.getRangeMarkers(0, Layer.FOREGROUND);
        assertTrue(fgMarkers == null || fgMarkers.isEmpty());
    }

    // Tests setting and getting domain axis
    @Test
    public void testSetDomainAxis_validAxis_setsAxisAndNotifies() {
        CategoryAxis axis = new CategoryAxis("Domain");
        this.plot.setDomainAxis(axis);
        assertEquals(axis, this.plot.getDomainAxis());
        assertEquals(0, this.plot.getDomainAxisIndex(axis));
    }

    // Tests setting domain axis with null argument
    @Test
    public void testSetDomainAxis_null_clearsAxis() {
        this.plot.setDomainAxis(null);
        assertNull(this.plot.getDomainAxis());
    }

    // Tests getting domain axis index with null throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testGetDomainAxisIndex_null_throwsException() {
        this.plot.getDomainAxisIndex(null);
    }

    // Tests setting and getting range axis
    @Test
    public void testSetRangeAxis_validAxis_setsAxisAndNotifies() {
        NumberAxis axis = new NumberAxis("Range");
        this.plot.setRangeAxis(axis);
        assertEquals(axis, this.plot.getRangeAxis());
        assertEquals(0, this.plot.getRangeAxisIndex(axis));
    }

    // Tests getting range axis index with null throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testGetRangeAxisIndex_null_throwsException() {
        this.plot.getRangeAxisIndex(null);
    }

    // Tests setting orientation with null throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testSetOrientation_null_throwsException() {
        this.plot.setOrientation(null);
    }

    // Tests orientation getter and setter
    @Test
    public void testSetOrientation_validOrientation_updatesOrientation() {
        this.plot.setOrientation(PlotOrientation.HORIZONTAL);
        assertEquals(PlotOrientation.HORIZONTAL, this.plot.getOrientation());
    }

    // Tests setting axis offset with null throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testSetAxisOffset_null_throwsException() {
        this.plot.setAxisOffset(null);
    }

    // Tests dataset mapping and retrieval
    @Test
    public void testDataset_setAndGet() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "R1", "C1");
        this.plot.setDataset(dataset);
        assertEquals(dataset, this.plot.getDataset());
        assertEquals(1, this.plot.getCategories().size());
        assertEquals("C1", this.plot.getCategories().get(0));
    }

    // Tests renderer assignment and lookup
    @Test
    public void testRenderer_setAndGet() {
        BarRenderer renderer = new BarRenderer();
        this.plot.setRenderer(renderer);
        assertEquals(renderer, this.plot.getRenderer());
        assertEquals(0, this.plot.getIndexOf(renderer));
    }

    // Tests equals and clone functionality
    @Test
    public void testEqualsAndClone() throws CloneNotSupportedException {
        CategoryPlot p1 = new CategoryPlot(new DefaultCategoryDataset(),
                new CategoryAxis("X"), new NumberAxis("Y"), new BarRenderer());
        CategoryPlot p2 = (CategoryPlot) p1.clone();

        assertTrue(p1.equals(p2));
        assertTrue(p2.equals(p1));
        assertNotSame(p1, p2);
    }

    // Tests equals when attributes differ
    @Test
    public void testEquals_differentOrientation_returnsFalse() {
        CategoryPlot p1 = new CategoryPlot();
        CategoryPlot p2 = new CategoryPlot();
        p2.setOrientation(PlotOrientation.HORIZONTAL);
        assertFalse(p1.equals(p2));
    }

    // Tests zoom range axes behavior
    @Test
    public void testZoomRangeAxes_positiveFactor_resizesAxis() {
        NumberAxis axis = new NumberAxis("Range");
        axis.setRange(0.0, 100.0);
        this.plot.setRangeAxis(axis);
        this.plot.zoom(0.5);
        assertEquals(50.0, axis.getRange().getLength(), 0.0001);
    }

    // Tests crosshair properties
    @Test
    public void testRangeCrosshair_gettersAndSetters() {
        this.plot.setRangeCrosshairVisible(true);
        assertTrue(this.plot.isRangeCrosshairVisible());

        this.plot.setRangeCrosshairValue(42.0);
        assertEquals(42.0, this.plot.getRangeCrosshairValue(), 0.0001);

        BasicStroke stroke = new BasicStroke(2.0f);
        this.plot.setRangeCrosshairStroke(stroke);
        assertEquals(stroke, this.plot.getRangeCrosshairStroke());

        this.plot.setRangeCrosshairPaint(Color.RED);
        assertEquals(Color.RED, this.plot.getRangeCrosshairPaint());
    }
}