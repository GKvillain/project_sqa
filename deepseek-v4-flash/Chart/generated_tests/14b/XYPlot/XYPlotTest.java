package org.jfree.chart.plot;

import org.junit.Test;
import static org.junit.Assert.*;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.util.Layer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.List;

public class XYPlotTest {

    /**
     * Helper: creates a basic XYPlot with one dataset, one axis and one renderer.
     */
    private XYPlot createBasicPlot() {
        XYSeries series = new XYSeries("S1");
        series.add(1.0, 2.0);
        XYSeriesCollection dataset = new XYSeriesCollection(series);
        NumberAxis domainAxis = new NumberAxis("x");
        NumberAxis rangeAxis = new NumberAxis("y");
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        return new XYPlot(dataset, domainAxis, rangeAxis, renderer);
    }

    // ====================== domain marker tests ======================

    @Test
    public void testAddDomainMarker_normalMarker_addedSuccessfully() {
        XYPlot plot = createBasicPlot();
        ValueMarker marker = new ValueMarker(1.0);
        plot.addDomainMarker(0, marker, Layer.FOREGROUND);
        Collection markers = plot.getDomainMarkers(Layer.FOREGROUND);
        assertTrue(markers.contains(marker));
    }

    @Test
    public void testRemoveDomainMarker_existingMarker_returnsTrueAndRemoved() {
        XYPlot plot = createBasicPlot();
        ValueMarker marker = new ValueMarker(1.0);
        plot.addDomainMarker(0, marker, Layer.FOREGROUND);
        boolean removed = plot.removeDomainMarker(0, marker, Layer.FOREGROUND);
        assertTrue(removed);
        Collection markers = plot.getDomainMarkers(Layer.FOREGROUND);
        assertFalse(markers.contains(marker));
    }

    // Bug: removeDomainMarker with non-existent index throws NullPointerException
    @Test(expected = NullPointerException.class)
    public void testRemoveDomainMarker_nonExistentIndex_throwsNullPointerException() {
        XYPlot plot = createBasicPlot();
        ValueMarker marker = new ValueMarker(1.0);
        // index 999 has no entry in marker maps
        plot.removeDomainMarker(999, marker, Layer.FOREGROUND);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddDomainMarker_nullMarker_throwsIllegalArgumentException() {
        XYPlot plot = createBasicPlot();
        plot.addDomainMarker(0, null, Layer.FOREGROUND);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveDomainMarker_nullMarker_throwsIllegalArgumentException() {
        XYPlot plot = createBasicPlot();
        plot.removeDomainMarker(0, null, Layer.FOREGROUND);
    }

    // ====================== range marker tests ======================

    @Test
    public void testAddRangeMarker_normalMarker_addedSuccessfully() {
        XYPlot plot = createBasicPlot();
        ValueMarker marker = new ValueMarker(2.0);
        plot.addRangeMarker(0, marker, Layer.BACKGROUND);
        Collection markers = plot.getRangeMarkers(Layer.BACKGROUND);
        assertTrue(markers.contains(marker));
    }

    @Test
    public void testRemoveRangeMarker_existingMarker_returnsTrueAndRemoved() {
        XYPlot plot = createBasicPlot();
        ValueMarker marker = new ValueMarker(2.0);
        plot.addRangeMarker(0, marker, Layer.BACKGROUND);
        boolean removed = plot.removeRangeMarker(0, marker, Layer.BACKGROUND);
        assertTrue(removed);
        Collection markers = plot.getRangeMarkers(Layer.BACKGROUND);
        assertFalse(markers.contains(marker));
    }

    // Bug: removeRangeMarker with non-existent index throws NullPointerException
    @Test(expected = NullPointerException.class)
    public void testRemoveRangeMarker_nonExistentIndex_throwsNullPointerException() {
        XYPlot plot = createBasicPlot();
        ValueMarker marker = new ValueMarker(2.0);
        plot.removeRangeMarker(999, marker, Layer.BACKGROUND);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddRangeMarker_nullMarker_throwsIllegalArgumentException() {
        XYPlot plot = createBasicPlot();
        plot.addRangeMarker(0, null, Layer.BACKGROUND);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveRangeMarker_nullMarker_throwsIllegalArgumentException() {
        XYPlot plot = createBasicPlot();
        plot.removeRangeMarker(0, null, Layer.BACKGROUND);
    }

    // ====================== annotation tests ======================

    @Test(expected = IllegalArgumentException.class)
    public void testAddAnnotation_nullAnnotation_throwsIllegalArgumentException() {
        XYPlot plot = createBasicPlot();
        plot.addAnnotation(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveAnnotation_nullAnnotation_throwsIllegalArgumentException() {
        XYPlot plot = createBasicPlot();
        plot.removeAnnotation(null);
    }

    // ====================== dataset and axis mapping tests ======================

    @Test
    public void testGetDomainAxisForDataset_validIndex_returnsCorrectAxis() {
        XYPlot plot = createBasicPlot();
        ValueAxis axis = plot.getDomainAxisForDataset(0);
        assertNotNull(axis);
        assertTrue(axis instanceof NumberAxis);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetDomainAxisForDataset_negativeIndex_throwsIllegalArgumentException() {
        XYPlot plot = createBasicPlot();
        plot.getDomainAxisForDataset(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetRangeAxisForDataset_outOfBounds_throwsIllegalArgumentException() {
        XYPlot plot = createBasicPlot();
        plot.getRangeAxisForDataset(1);
    }

    // ====================== getDataRange tests ======================

    @Test
    public void testGetDataRange_withDomainAxis_returnsNonNullRange() {
        XYPlot plot = createBasicPlot();
        ValueAxis domainAxis = plot.getDomainAxis();
        org.jfree.data.Range range = plot.getDataRange(domainAxis);
        assertNotNull(range);
    }

    // ====================== orientation tests ======================

    @Test
    public void testSetOrientation_changesOrientation() {
        XYPlot plot = createBasicPlot();
        plot.setOrientation(PlotOrientation.HORIZONTAL);
        assertEquals(PlotOrientation.HORIZONTAL, plot.getOrientation());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetOrientation_nullArgument_throwsIllegalArgumentException() {
        XYPlot plot = createBasicPlot();
        plot.setOrientation(null);
    }

    // ====================== crosshair tests ======================

    @Test
    public void testSetDomainCrosshairVisible_visibleFlagIsSet() {
        XYPlot plot = createBasicPlot();
        plot.setDomainCrosshairVisible(true);
        assertTrue(plot.isDomainCrosshairVisible());
        plot.setDomainCrosshairVisible(false);
        assertFalse(plot.isDomainCrosshairVisible());
    }

    @Test
    public void testSetRangeCrosshairVisible_visibleFlagIsSet() {
        XYPlot plot = createBasicPlot();
        plot.setRangeCrosshairVisible(true);
        assertTrue(plot.isRangeCrosshairVisible());
        plot.setRangeCrosshairVisible(false);
        assertFalse(plot.isRangeCrosshairVisible());
    }

    // ====================== fixed axis space tests ======================

    @Test
    public void testSetFixedDomainAxisSpace_returnsSameSpace() {
        XYPlot plot = createBasicPlot();
        org.jfree.chart.axis.AxisSpace space = new org.jfree.chart.axis.AxisSpace();
        plot.setFixedDomainAxisSpace(space);
        assertSame(space, plot.getFixedDomainAxisSpace());
    }

    @Test
    public void testSetFixedRangeAxisSpace_returnsSameSpace() {
        XYPlot plot = createBasicPlot();
        org.jfree.chart.axis.AxisSpace space = new org.jfree.chart.axis.AxisSpace();
        plot.setFixedRangeAxisSpace(space);
        assertSame(space, plot.getFixedRangeAxisSpace());
    }

    // ====================== equals and clone tests ======================

    @Test
    public void testEquals_samePlot_returnsTrue() {
        XYPlot plot1 = createBasicPlot();
        XYPlot plot2 = createBasicPlot();
        assertTrue(plot1.equals(plot2));
    }

    @Test
    public void testEquals_differentWeight_returnsFalse() {
        XYPlot plot1 = createBasicPlot();
        XYPlot plot2 = createBasicPlot();
        plot2.setWeight(2);
        assertFalse(plot1.equals(plot2));
    }

    @Test
    public void testClone_createsIndependentCopy() throws CloneNotSupportedException {
        XYPlot plot = createBasicPlot();
        XYPlot clone = (XYPlot) plot.clone();
        assertNotSame(clone, plot);
        assertNotNull(clone.getDomainAxis());
        assertNotNull(clone.getRangeAxis());
        assertNotNull(clone.getRenderer());
        // verify axis are different instances
        assertNotSame(clone.getDomainAxis(), plot.getDomainAxis());
    }

    // ====================== getDomainAxisIndex / getRangeAxisIndex ======================

    @Test
    public void testGetDomainAxisIndex_returnsCorrectIndex() {
        XYPlot plot = createBasicPlot();
        ValueAxis axis = plot.getDomainAxis();
        int index = plot.getDomainAxisIndex(axis);
        assertEquals(0, index);
    }

    @Test
    public void testGetRangeAxisIndex_returnsCorrectIndex() {
        XYPlot plot = createBasicPlot();
        ValueAxis axis = plot.getRangeAxis();
        int index = plot.getRangeAxisIndex(axis);
        assertEquals(0, index);
    }
}