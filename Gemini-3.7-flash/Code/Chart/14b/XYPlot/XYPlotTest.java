package org.jfree.chart.plot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.List;

import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.AxisSpace;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.util.Layer;
import org.jfree.chart.util.RectangleInsets;
import org.jfree.data.Range;
import org.jfree.data.xy.DefaultTableXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link XYPlot}.
 */
public class XYPlotTest {

    private XYPlot plot;

    @Before
    public void setUp() {
        ValueAxis domainAxis = new NumberAxis("Domain");
        ValueAxis rangeAxis = new NumberAxis("Range");
        XYItemRenderer renderer = new StandardXYItemRenderer();
        this.plot = new XYPlot(null, domainAxis, rangeAxis, renderer);
    }

    // Tests removeDomainMarker when no markers have been added
    @Test
    public void testRemoveDomainMarker_whenNoMarkersAdded_returnsFalse() {
        Marker marker = new ValueMarker(50.0);
        boolean removed = this.plot.removeDomainMarker(marker);
        assertFalse(removed);
    }

    // Tests removeDomainMarker in background layer when no markers exist
    @Test
    public void testRemoveDomainMarker_backgroundLayerNoMarkers_returnsFalse() {
        Marker marker = new ValueMarker(25.0);
        boolean removed = this.plot.removeDomainMarker(0, marker, Layer.BACKGROUND);
        assertFalse(removed);
    }

    // Tests removeRangeMarker when no markers have been added
    @Test
    public void testRemoveRangeMarker_whenNoMarkersAdded_returnsFalse() {
        Marker marker = new ValueMarker(100.0);
        boolean removed = this.plot.removeRangeMarker(marker);
        assertFalse(removed);
    }

    // Tests removeRangeMarker in background layer when no markers exist
    @Test
    public void testRemoveRangeMarker_backgroundLayerNoMarkers_returnsFalse() {
        Marker marker = new ValueMarker(75.0);
        boolean removed = this.plot.removeRangeMarker(0, marker, Layer.BACKGROUND);
        assertFalse(removed);
    }

    // Tests addDomainMarker and removeDomainMarker for foreground and background layers
    @Test
    public void testAddAndRemoveDomainMarker_validMarker_modifiesCollection() {
        Marker marker1 = new ValueMarker(10.0);
        Marker marker2 = new ValueMarker(20.0);

        this.plot.addDomainMarker(marker1, Layer.FOREGROUND);
        this.plot.addDomainMarker(marker2, Layer.BACKGROUND);

        Collection fgMarkers = this.plot.getDomainMarkers(Layer.FOREGROUND);
        Collection bgMarkers = this.plot.getDomainMarkers(Layer.BACKGROUND);

        assertNotNull(fgMarkers);
        assertEquals(1, fgMarkers.size());
        assertTrue(fgMarkers.contains(marker1));

        assertNotNull(bgMarkers);
        assertEquals(1, bgMarkers.size());
        assertTrue(bgMarkers.contains(marker2));

        boolean removedFg = this.plot.removeDomainMarker(marker1, Layer.FOREGROUND);
        assertTrue(removedFg);
        assertEquals(0, this.plot.getDomainMarkers(Layer.FOREGROUND).size());

        boolean removedBg = this.plot.removeDomainMarker(0, marker2, Layer.BACKGROUND);
        assertTrue(removedBg);
        assertEquals(0, this.plot.getDomainMarkers(Layer.BACKGROUND).size());
    }

    // Tests addRangeMarker and removeRangeMarker for foreground and background layers
    @Test
    public void testAddAndRemoveRangeMarker_validMarker_modifiesCollection() {
        Marker marker1 = new ValueMarker(30.0);
        Marker marker2 = new ValueMarker(40.0);

        this.plot.addRangeMarker(marker1, Layer.FOREGROUND);
        this.plot.addRangeMarker(marker2, Layer.BACKGROUND);

        Collection fgMarkers = this.plot.getRangeMarkers(Layer.FOREGROUND);
        Collection bgMarkers = this.plot.getRangeMarkers(Layer.BACKGROUND);

        assertNotNull(fgMarkers);
        assertEquals(1, fgMarkers.size());
        assertTrue(fgMarkers.contains(marker1));

        assertNotNull(bgMarkers);
        assertEquals(1, bgMarkers.size());
        assertTrue(bgMarkers.contains(marker2));

        boolean removedFg = this.plot.removeRangeMarker(marker1, Layer.FOREGROUND);
        assertTrue(removedFg);
        assertEquals(0, this.plot.getRangeMarkers(Layer.FOREGROUND).size());

        boolean removedBg = this.plot.removeRangeMarker(0, marker2, Layer.BACKGROUND);
        assertTrue(removedBg);
        assertEquals(0, this.plot.getRangeMarkers(Layer.BACKGROUND).size());
    }

    // Tests clearDomainMarkers and clearRangeMarkers
    @Test
    public void testClearMarkers_clearsAllLayers() {
        Marker dm1 = new ValueMarker(1.0);
        Marker dm2 = new ValueMarker(2.0);
        Marker rm1 = new ValueMarker(3.0);
        Marker rm2 = new ValueMarker(4.0);

        this.plot.addDomainMarker(dm1, Layer.FOREGROUND);
        this.plot.addDomainMarker(dm2, Layer.BACKGROUND);
        this.plot.addRangeMarker(rm1, Layer.FOREGROUND);
        this.plot.addRangeMarker(rm2, Layer.BACKGROUND);

        this.plot.clearDomainMarkers();
        assertNull(this.plot.getDomainMarkers(Layer.FOREGROUND));
        assertNull(this.plot.getDomainMarkers(Layer.BACKGROUND));

        this.plot.clearRangeMarkers();
        assertNull(this.plot.getRangeMarkers(Layer.FOREGROUND));
        assertNull(this.plot.getRangeMarkers(Layer.BACKGROUND));
    }

    // Tests removeRangeMarker with null argument throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testRemoveRangeMarker_nullMarker_throwsException() {
        this.plot.removeRangeMarker(0, null, Layer.FOREGROUND, true);
    }

    // Tests addDomainMarker with null marker throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testAddDomainMarker_nullMarker_throwsException() {
        this.plot.addDomainMarker(0, null, Layer.FOREGROUND, true);
    }

    // Tests orientation getter, setter, and null check
    @Test
    public void testSetOrientation_validAndNull() {
        assertEquals(PlotOrientation.VERTICAL, this.plot.getOrientation());
        this.plot.setOrientation(PlotOrientation.HORIZONTAL);
        assertEquals(PlotOrientation.HORIZONTAL, this.plot.getOrientation());
    }

    // Tests setOrientation with null throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testSetOrientation_null_throwsException() {
        this.plot.setOrientation(null);
    }

    // Tests domain and range crosshair visibility, value, and lockedOnData flags
    @Test
    public void testCrosshairProperties_getAndSet() {
        assertFalse(this.plot.isDomainCrosshairVisible());
        this.plot.setDomainCrosshairVisible(true);
        assertTrue(this.plot.isDomainCrosshairVisible());

        this.plot.setDomainCrosshairValue(12.34);
        assertEquals(12.34, this.plot.getDomainCrosshairValue(), 1e-9);

        assertTrue(this.plot.isDomainCrosshairLockedOnData());
        this.plot.setDomainCrosshairLockedOnData(false);
        assertFalse(this.plot.isDomainCrosshairLockedOnData());

        assertFalse(this.plot.isRangeCrosshairVisible());
        this.plot.setRangeCrosshairVisible(true);
        assertTrue(this.plot.isRangeCrosshairVisible());

        this.plot.setRangeCrosshairValue(56.78);
        assertEquals(56.78, this.plot.getRangeCrosshairValue(), 1e-9);

        assertTrue(this.plot.isRangeCrosshairLockedOnData());
        this.plot.setRangeCrosshairLockedOnData(false);
        assertFalse(this.plot.isRangeCrosshairLockedOnData());
    }

    // Tests quadrant origin and quadrant paint settings
    @Test
    public void testQuadrantSettings_getAndSet() {
        Point2D origin = new Point2D.Double(5.0, 10.0);
        this.plot.setQuadrantOrigin(origin);
        assertEquals(origin, this.plot.getQuadrantOrigin());

        assertNull(this.plot.getQuadrantPaint(0));
        this.plot.setQuadrantPaint(0, Color.RED);
        this.plot.setQuadrantPaint(1, Color.GREEN);
        this.plot.setQuadrantPaint(2, Color.BLUE);
        this.plot.setQuadrantPaint(3, Color.YELLOW);

        assertEquals(Color.RED, this.plot.getQuadrantPaint(0));
        assertEquals(Color.GREEN, this.plot.getQuadrantPaint(1));
        assertEquals(Color.BLUE, this.plot.getQuadrantPaint(2));
        assertEquals(Color.YELLOW, this.plot.getQuadrantPaint(3));
    }

    // Tests quadrant paint index outside 0-3 throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testSetQuadrantPaint_invalidIndex_throwsException() {
        this.plot.setQuadrantPaint(4, Color.RED);
    }

    // Tests domain and range axis location setters and getters
    @Test
    public void testAxisLocations_getAndSet() {
        assertEquals(AxisLocation.BOTTOM_OR_LEFT, this.plot.getDomainAxisLocation());
        this.plot.setDomainAxisLocation(AxisLocation.TOP_OR_RIGHT);
        assertEquals(AxisLocation.TOP_OR_RIGHT, this.plot.getDomainAxisLocation());

        assertEquals(AxisLocation.BOTTOM_OR_LEFT, this.plot.getRangeAxisLocation());
        this.plot.setRangeAxisLocation(AxisLocation.TOP_OR_RIGHT);
        assertEquals(AxisLocation.TOP_OR_RIGHT, this.plot.getRangeAxisLocation());
    }

    // Tests setDomainAxisLocation index 0 with null throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testSetDomainAxisLocation_nullIndexZero_throwsException() {
        this.plot.setDomainAxisLocation(0, null, true);
    }

    // Tests getDataRange calculation with dataset attached
    @Test
    public void testGetDataRange_withDataset_returnsCorrectRange() {
        DefaultTableXYDataset dataset = new DefaultTableXYDataset();
        XYSeries s1 = new XYSeries("Series 1", true, false);
        s1.add(1.0, 2.0);
        s1.add(5.0, 10.0);
        dataset.addSeries(s1);

        this.plot.setDataset(dataset);
        Range domainRange = this.plot.getDataRange(this.plot.getDomainAxis());
        assertNotNull(domainRange);
        assertEquals(1.0, domainRange.getLowerBound(), 1e-9);
        assertEquals(5.0, domainRange.getUpperBound(), 1e-9);

        Range rangeRange = this.plot.getDataRange(this.plot.getRangeAxis());
        assertNotNull(rangeRange);
        assertEquals(2.0, rangeRange.getLowerBound(), 1e-9);
        assertEquals(10.0, rangeRange.getUpperBound(), 1e-9);
    }

    // Tests zoomDomainAxes and zoomRangeAxes
    @Test
    public void testZoomAxes_modifiesAxisRanges() {
        ValueAxis domainAxis = this.plot.getDomainAxis();
        domainAxis.setRange(0.0, 100.0);
        ValueAxis rangeAxis = this.plot.getRangeAxis();
        rangeAxis.setRange(0.0, 100.0);

        this.plot.zoomDomainAxes(0.5, null, new Point2D.Double(0.0, 0.0));
        assertEquals(25.0, domainAxis.getLowerBound(), 1e-9);
        assertEquals(75.0, domainAxis.getUpperBound(), 1e-9);

        this.plot.zoomRangeAxes(0.5, null, new Point2D.Double(0.0, 0.0));
        assertEquals(25.0, rangeAxis.getLowerBound(), 1e-9);
        assertEquals(75.0, rangeAxis.getUpperBound(), 1e-9);
    }

    // Tests equals and clone methods
    @Test
    public void testEqualsAndClone() throws CloneNotSupportedException {
        XYPlot p1 = new XYPlot(null, new NumberAxis("X"), new NumberAxis("Y"), new StandardXYItemRenderer());
        XYPlot p2 = new XYPlot(null, new NumberAxis("X"), new NumberAxis("Y"), new StandardXYItemRenderer());

        assertTrue(p1.equals(p2));
        assertTrue(p2.equals(p1));

        XYPlot cloned = (XYPlot) p1.clone();
        assertNotSame(p1, cloned);
        assertEquals(p1.getClass(), cloned.getClass());
        assertTrue(p1.equals(cloned));
    }
}