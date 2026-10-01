package org.jfree.chart.plot;

import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.LegendItem;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.util.Layer;
import org.jfree.chart.util.RectangleEdge;
import org.jfree.chart.util.RectangleInsets;
import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.data.Range;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Stroke;

public class XYPlotTest {

    private XYPlot plot;
    private XYSeriesCollection dataset;
    private NumberAxis domainAxis;
    private NumberAxis rangeAxis;
    private XYLineAndShapeRenderer renderer;

    @Before
    public void setUp() {
        plot = new XYPlot();
        dataset = new XYSeriesCollection();
        XYSeries series = new XYSeries("S1");
        series.add(1.0, 2.0);
        series.add(3.0, 4.0);
        dataset.addSeries(series);
        domainAxis = new NumberAxis("X");
        rangeAxis = new NumberAxis("Y");
        renderer = new XYLineAndShapeRenderer();
    }

    // Tests default constructor initializes fields
    @Test
    public void testConstructor_noArgs_initializesDefaults() {
        XYPlot p = new XYPlot();
        assertNull(p.getDataset());
        assertNull(p.getDomainAxis());
        assertNull(p.getRangeAxis());
        assertNull(p.getRenderer());
        assertEquals(PlotOrientation.VERTICAL, p.getOrientation());
        assertEquals(1, p.getWeight());
        assertEquals(AxisLocation.BOTTOM_OR_LEFT, p.getDomainAxisLocation());
        assertEquals(AxisLocation.BOTTOM_OR_LEFT, p.getRangeAxisLocation());
    }

    // Tests constructor with all main arguments
    @Test
    public void testConstructor_withArgs_setsAllFields() {
        XYPlot p = new XYPlot(dataset, domainAxis, rangeAxis, renderer);
        assertSame(dataset, p.getDataset());
        assertSame(domainAxis, p.getDomainAxis());
        assertSame(rangeAxis, p.getRangeAxis());
        assertSame(renderer, p.getRenderer());
    }

    // Tests null orientation is rejected
    @Test(expected = IllegalArgumentException.class)
    public void testSetOrientation_null_throwsException() {
        plot.setOrientation(null);
    }

    // Tests changing orientation
    @Test
    public void testSetOrientation_valid_updatesOrientation() {
        plot.setOrientation(PlotOrientation.HORIZONTAL);
        assertEquals(PlotOrientation.HORIZONTAL, plot.getOrientation());
    }

    // Tests null axis offset is rejected
    @Test(expected = IllegalArgumentException.class)
    public void testSetAxisOffset_null_throwsException() {
        plot.setAxisOffset(null);
    }

    // Tests setting a valid axis offset
    @Test
    public void testSetAxisOffset_valid_updatesOffset() {
        RectangleInsets insets = new RectangleInsets(1, 2, 3, 4);
        plot.setAxisOffset(insets);
        assertSame(insets, plot.getAxisOffset());
    }

    // Tests replacing the domain axis
    @Test
    public void testSetDomainAxis_replacesAxis() {
        NumberAxis newAxis = new NumberAxis("X2");
        plot.setDomainAxis(newAxis);
        assertSame(newAxis, plot.getDomainAxis());
    }

    // Tests replacing the range axis
    @Test
    public void testSetRangeAxis_replacesAxis() {
        NumberAxis newAxis = new NumberAxis("Y2");
        plot.setRangeAxis(newAxis);
        assertSame(newAxis, plot.getRangeAxis());
    }

    // Tests setting dataset and retrieving it
    @Test
    public void testSetDataset_replacesDataset() {
        plot.setDataset(0, dataset);
        assertSame(dataset, plot.getDataset(0));
        assertEquals(1, plot.getDatasetCount());
    }

    // Tests default mapping to domain axis
    @Test
    public void testGetDomainAxisForDataset_default_returnsPrimaryAxis() {
        XYPlot p = new XYPlot(dataset, domainAxis, rangeAxis, renderer);
        assertSame(domainAxis, p.getDomainAxisForDataset(0));
    }

    // Tests default mapping to range axis
    @Test
    public void testGetRangeAxisForDataset_default_returnsPrimaryAxis() {
        XYPlot p = new XYPlot(dataset, domainAxis, rangeAxis, renderer);
        assertSame(rangeAxis, p.getRangeAxisForDataset(0));
    }

    // Tests explicit mapping of dataset to domain axis
    @Test
    public void testMapDatasetToDomainAxis_validMapping() {
        plot.setDataset(0, dataset);
        plot.setDomainAxis(0, domainAxis);
        plot.mapDatasetToDomainAxis(0, 0);
        assertSame(domainAxis, plot.getDomainAxisForDataset(0));
    }

    // Tests explicit mapping of dataset to range axis
    @Test
    public void testMapDatasetToRangeAxis_validMapping() {
        plot.setDataset(0, dataset);
        plot.setRangeAxis(0, rangeAxis);
        plot.mapDatasetToRangeAxis(0, 0);
        assertSame(rangeAxis, plot.getRangeAxisForDataset(0));
    }

    // Tests getDataRange for a domain axis with data
    @Test
    public void testGetDataRange_forDomainAxis_returnsCorrectRange() {
        XYPlot p = new XYPlot(dataset, domainAxis, rangeAxis, renderer);
        Range r = p.getDataRange(domainAxis);
        assertNotNull(r);
        assertEquals(1.0, r.getLowerBound(), 0.0001);
        assertEquals(3.0, r.getUpperBound(), 0.0001);
    }

    // Tests getDataRange for a range axis with data
    @Test
    public void testGetDataRange_forRangeAxis_returnsCorrectRange() {
        XYPlot p = new XYPlot(dataset, domainAxis, rangeAxis, renderer);
        Range r = p.getDataRange(rangeAxis);
        assertNotNull(r);
        assertEquals(2.0, r.getLowerBound(), 0.0001);
        assertEquals(4.0, r.getUpperBound(), 0.0001);
    }

    // Tests getDataRange when no renderer is set, should not throw NPE
    @Test
    public void testGetDataRange_nullRenderer_doesNotThrowNPE() {
        plot.setDataset(dataset);
        plot.setDomainAxis(domainAxis);
        plot.setRangeAxis(rangeAxis);
        Range r = plot.getDataRange(domainAxis);
        assertNotNull(r);
    }

    // Tests getDataRange with an unknown axis returns null
    @Test
    public void testGetDataRange_unknownAxis_returnsNull() {
        XYPlot p = new XYPlot(dataset, domainAxis, rangeAxis, renderer);
        ValueAxis unknown = new NumberAxis("Unknown");
        assertNull(p.getDataRange(unknown));
    }

    // Tests renderer retrieval for dataset
    @Test
    public void testGetRendererForDataset_returnsRenderer() {
        XYPlot p = new XYPlot(dataset, domainAxis, rangeAxis, renderer);
        assertSame(renderer, p.getRendererForDataset(dataset));
    }

    // Tests index of renderer
    @Test
    public void testGetIndexOfRenderer_returnsIndex() {
        XYPlot p = new XYPlot(dataset, domainAxis, rangeAxis, renderer);
        assertEquals(0, p.getIndexOf(renderer));
    }

    // Tests setting a renderer at a specific index
    @Test
    public void testSetRendererAtIndex_setsRenderer() {
        XYLineAndShapeRenderer r2 = new XYLineAndShapeRenderer();
        plot.setRenderer(1, r2);
        assertSame(r2, plot.getRenderer(1));
    }

    // Tests renderer count
    @Test
    public void testGetRendererCount_returnsCount() {
        plot.setRenderer(0, renderer);
        assertEquals(1, plot.getRendererCount());
    }

    // Tests toggling domain gridlines visibility
    @Test
    public void testSetDomainGridlinesVisible_changesFlag() {
        plot.setDomainGridlinesVisible(false);
        assertFalse(plot.isDomainGridlinesVisible());
    }

    // Tests toggling range gridlines visibility
    @Test
    public void testSetRangeGridlinesVisible_changesFlag() {
        plot.setRangeGridlinesVisible(false);
        assertFalse(plot.isRangeGridlinesVisible());
    }

    // Tests setting domain crosshair value
    @Test
    public void testSetDomainCrosshairValue_setsValue() {
        plot.setDomainCrosshairVisible(true);
        plot.setDomainCrosshairValue(7.0);
        assertEquals(7.0, plot.getDomainCrosshairValue(), 0.0);
    }

    // Tests setting range crosshair value
    @Test
    public void testSetRangeCrosshairValue_setsValue() {
        plot.setRangeCrosshairVisible(true);
        plot.setRangeCrosshairValue(8.0);
        assertEquals(8.0, plot.getRangeCrosshairValue(), 0.0);
    }

    // Tests adding a domain marker
    @Test
    public void testAddDomainMarker_addsToForeground() {
        Marker marker = new ValueMarker(2.0);
        plot.addDomainMarker(marker);
        assertTrue(plot.getDomainMarkers(Layer.FOREGROUND).contains(marker));
    }

    // Tests adding a range marker
    @Test
    public void testAddRangeMarker_addsToForeground() {
        Marker marker = new ValueMarker(3.0);
        plot.addRangeMarker(marker);
        assertTrue(plot.getRangeMarkers(Layer.FOREGROUND).contains(marker));
    }

    // Tests removing a domain marker
    @Test
    public void testRemoveDomainMarker_removesMarker() {
        Marker marker = new ValueMarker(2.0);
        plot.addDomainMarker(marker);
        assertTrue(plot.removeDomainMarker(marker));
    }

    // Tests removing a range marker
    @Test
    public void testRemoveRangeMarker_removesMarker() {
        Marker marker = new ValueMarker(3.0);
        plot.addRangeMarker(marker);
        assertTrue(plot.removeRangeMarker(marker));
    }

    // Tests clearing domain markers
    @Test
    public void testClearDomainMarkers_clearsMarkers() {
        plot.addDomainMarker(new ValueMarker(1.0));
        plot.addDomainMarker(new ValueMarker(2.0));
        plot.clearDomainMarkers();
        assertTrue(plot.getDomainMarkers(Layer.FOREGROUND).isEmpty());
    }

    // Tests clearing range markers
    @Test
    public void testClearRangeMarkers_clearsMarkers() {
        plot.addRangeMarker(new ValueMarker(1.0));
        plot.addRangeMarker(new ValueMarker(2.0));
        plot.clearRangeMarkers();
        assertTrue(plot.getRangeMarkers(Layer.FOREGROUND).isEmpty());
    }

    // Tests fixed legend items
    @Test
    public void testSetFixedLegendItems_updatesItems() {
        LegendItemCollection items = new LegendItemCollection();
        plot.setFixedLegendItems(items);
        assertSame(items, plot.getFixedLegendItems());
    }

    // Tests legend items generated from renderer
    @Test
    public void testGetLegendItems_returnsItemsFromRenderer() {
        XYPlot p = new XYPlot(dataset, domainAxis, rangeAxis, renderer);
        assertNotNull(p.getLegendItems());
        assertEquals(1, p.getLegendItems().getItemCount());
    }

    // Tests clone produces equal independent plot
    @Test
    public void testClone_returnsIndependentCopy() throws CloneNotSupportedException {
        XYPlot p1 = new XYPlot(dataset, domainAxis, rangeAxis, renderer);
        XYPlot p2 = (XYPlot) p1.clone();
        assertNotSame(p1, p2);
        assertTrue(p1.equals(p2));
    }

    // Tests equals with itself returns true
    @Test
    public void testEquals_sameObject_returnsTrue() {
        XYPlot p = new XYPlot();
        assertTrue(p.equals(p));
    }

    // Tests equals with null returns false
    @Test
    public void testEquals_null_returnsFalse() {
        XYPlot p = new XYPlot();
        assertFalse(p.equals(null));
    }

    // Tests equals with different orientation returns false
    @Test
    public void testEquals_differentOrientation_returnsFalse() {
        XYPlot p1 = new XYPlot();
        XYPlot p2 = new XYPlot();
        p2.setOrientation(PlotOrientation.HORIZONTAL);
        assertFalse(p1.equals(p2));
    }

    // Tests setting domain axis location
    @Test
    public void testSetDomainAxisLocation_updatesLocation() {
        plot.setDomainAxisLocation(AxisLocation.TOP_OR_RIGHT);
        assertEquals(AxisLocation.TOP_OR_RIGHT, plot.getDomainAxisLocation());
    }

    // Tests setting range axis location
    @Test
    public void testSetRangeAxisLocation_updatesLocation() {
        plot.setRangeAxisLocation(AxisLocation.TOP_OR_RIGHT);
        assertEquals(AxisLocation.TOP_OR_RIGHT, plot.getRangeAxisLocation());
    }

    // Tests null domain gridline stroke is rejected
    @Test(expected = IllegalArgumentException.class)
    public void testSetDomainGridlineStroke_null_throwsException() {
        plot.setDomainGridlineStroke(null);
    }

    // Tests null range gridline paint is rejected
    @Test(expected = IllegalArgumentException.class)
    public void testSetRangeGridlinePaint_null_throwsException() {
        plot.setRangeGridlinePaint(null);
    }

    // Tests null domain crosshair stroke is rejected
    @Test(expected = IllegalArgumentException.class)
    public void testSetDomainCrosshairStroke_null_throwsException() {
        plot.setDomainCrosshairStroke(null);
    }

    // Tests null range crosshair paint is rejected
    @Test(expected = IllegalArgumentException.class)
    public void testSetRangeCrosshairPaint_null_throwsException() {
        plot.setRangeCrosshairPaint(null);
    }

    // Tests null annotation is rejected
    @Test(expected = IllegalArgumentException.class)
    public void testAddAnnotation_null_throwsException() {
        plot.addAnnotation(null);
    }

    // Tests setting domain axis to null works
    @Test
    public void testSetDomainAxis_null_removesAxis() {
        plot.setDomainAxis(null);
        assertNull(plot.getDomainAxis());
    }

    // Tests setting weight
    @Test
    public void testSetWeight_updates() {
        plot.setWeight(5);
        assertEquals(5, plot.getWeight());
    }

    // ========== New tests for uncovered areas ==========

    // Tests setDomainAxis with index
    @Test
    public void testSetDomainAxisIndexed_setsAxis() {
        NumberAxis axis = new NumberAxis("X2");
        plot.setDomainAxis(0, axis);
        assertSame(axis, plot.getDomainAxis(0));
    }

    @Test
    public void testSetDomainAxisIndexed_null_removesAxis() {
        plot.setDomainAxis(0, null);
        assertNull(plot.getDomainAxis(0));
    }

    // Tests setRangeAxis with index
    @Test
    public void testSetRangeAxisIndexed_setsAxis() {
        NumberAxis axis = new NumberAxis("Y2");
        plot.setRangeAxis(0, axis);
        assertSame(axis, plot.getRangeAxis(0));
    }

    @Test
    public void testSetRangeAxisIndexed_null_removesAxis() {
        plot.setRangeAxis(0, null);
        assertNull(plot.getRangeAxis(0));
    }

    // Tests setDomainAxisLocation with notify flag
    @Test
    public void testSetDomainAxisLocationWithNotifyFalse() {
        plot.setDomainAxisLocation(AxisLocation.TOP_OR_RIGHT, false);
        assertEquals(AxisLocation.TOP_OR_RIGHT, plot.getDomainAxisLocation());
    }

    @Test
    public void testSetRangeAxisLocationWithNotifyFalse() {
        plot.setRangeAxisLocation(AxisLocation.TOP_OR_RIGHT, false);
        assertEquals(AxisLocation.TOP_OR_RIGHT, plot.getRangeAxisLocation());
    }

    // Tests domain crosshair visibility
    @Test
    public void testDomainCrosshairVisibleToggle() {
        assertFalse(plot.isDomainCrosshairVisible());
        plot.setDomainCrosshairVisible(true);
        assertTrue(plot.isDomainCrosshairVisible());
        plot.setDomainCrosshairVisible(false);
        assertFalse(plot.isDomainCrosshairVisible());
    }

    @Test
    public void testRangeCrosshairVisibleToggle() {
        assertFalse(plot.isRangeCrosshairVisible());
        plot.setRangeCrosshairVisible(true);
        assertTrue(plot.isRangeCrosshairVisible());
        plot.setRangeCrosshairVisible(false);
        assertFalse(plot.isRangeCrosshairVisible());
    }

    // Tests crosshair locked on data
    @Test
    public void testDomainCrosshairLockedOnData() {
        assertTrue(plot.isDomainCrosshairLockedOnData());
        plot.setDomainCrosshairLockedOnData(false);
        assertFalse(plot.isDomainCrosshairLockedOnData());
    }

    @Test
    public void testRangeCrosshairLockedOnData() {
        assertTrue(plot.isRangeCrosshairLockedOnData());
        plot.setRangeCrosshairLockedOnData(false);
        assertFalse(plot.isRangeCrosshairLockedOnData());
    }

    // Tests valid gridline stroke/paint setters (non-null should succeed)
    @Test
    public void testSetDomainGridlineStrokeValid() {
        Stroke stroke = new BasicStroke(2.0f);
        plot.setDomainGridlineStroke(stroke);
        assertSame(stroke, plot.getDomainGridlineStroke());
    }

    @Test
    public void testSetRangeGridlineStrokeValid() {
        Stroke stroke = new BasicStroke(2.0f);
        plot.setRangeGridlineStroke(stroke);
        assertSame(stroke, plot.getRangeGridlineStroke());
    }

    @Test
    public void testSetDomainGridlinePaintValid() {
        Paint paint = Color.RED;
        plot.setDomainGridlinePaint(paint);
        assertSame(paint, plot.getDomainGridlinePaint());
    }

    @Test
    public void testSetRangeGridlinePaintValid() {
        Paint paint = Color.RED;
        plot.setRangeGridlinePaint(paint);
        assertSame(paint, plot.getRangeGridlinePaint());
    }

    // Tests zero baseline visibility
    @Test
    public void testSetDomainZeroBaselineVisible() {
        assertFalse(plot.isDomainZeroBaselineVisible());
        plot.setDomainZeroBaselineVisible(true);
        assertTrue(plot.isDomainZeroBaselineVisible());
    }

    @Test
    public void testSetRangeZeroBaselineVisible() {
        assertFalse(plot.isRangeZeroBaselineVisible());
        plot.setRangeZeroBaselineVisible(true);
        assertTrue(plot.isRangeZeroBaselineVisible());
    }

    // Tests pannable flags
    @Test
    public void testSetDomainPannable() {
        assertFalse(plot.isDomainPannable());
        plot.setDomainPannable(true);
        assertTrue(plot.isDomainPannable());
    }

    @Test
    public void testSetRangePannable() {
        assertFalse(plot.isRangePannable());
        plot.setRangePannable(true);
        assertTrue(plot.isRangePannable());
    }

    // Tests removeDataset
    @Test
    public void testRemoveDataset_valid() {
        plot.setDataset(0, dataset);
        assertEquals(1, plot.getDatasetCount());
        plot.removeDataset(0);
        assertEquals(0, plot.getDatasetCount());
        assertNull(plot.getDataset(0));
    }

    @Test
    public void testRemoveDataset_invalidIndex_doesNothing() {
        // Should not throw exception for non-existent index
        plot.removeDataset(5);
    }

    // Tests indexOf dataset
    @Test
    public void testIndexOfDataset_returnsIndex() {
        plot.setDataset(0, dataset);
        assertEquals(0, plot.indexOf(dataset));
    }

    @Test
    public void testIndexOfDataset_notFound_returnsMinusOne() {
        assertEquals(-1, plot.indexOf(dataset));
    }

    // Tests addAnnotation and removeAnnotation
    @Test
    public void testAddAnnotation_valid() {
        XYLineAnnotation annotation = new XYLineAnnotation(0, 0, 1, 1);
        plot.addAnnotation(annotation);
        assertTrue(plot.getAnnotations().contains(annotation));
    }

    @Test
    public void testRemoveAnnotation_valid() {
        XYLineAnnotation annotation = new XYLineAnnotation(0, 0, 1, 1);
        plot.addAnnotation(annotation);
        assertTrue(plot.removeAnnotation(annotation));
        assertFalse(plot.getAnnotations().contains(annotation));
    }

    // Tests getDomainMarkers / getRangeMarkers with different layers
    @Test
    public void testGetDomainMarkersLayerBackground() {
        Marker marker = new ValueMarker(2.0);
        plot.addDomainMarker(marker, Layer.BACKGROUND);
        assertTrue(plot.getDomainMarkers(Layer.BACKGROUND).contains(marker));
    }

    @Test
    public void testGetRangeMarkersLayerBackground() {
        Marker marker = new ValueMarker(3.0);
        plot.addRangeMarker(marker, Layer.BACKGROUND);
        assertTrue(plot.getRangeMarkers(Layer.BACKGROUND).contains(marker));
    }

    // Tests clearing markers for a specific layer
    @Test
    public void testClearDomainMarkersForBackgroundLayer() {
        Marker marker = new ValueMarker(2.0);
        plot.addDomainMarker(marker, Layer.BACKGROUND);
        plot.clearDomainMarkers();
        assertTrue(plot.getDomainMarkers(Layer.BACKGROUND).isEmpty());
    }

    @Test
    public void testClearRangeMarkersForBackgroundLayer() {
        Marker marker = new ValueMarker(3.0);
        plot.addRangeMarker(marker, Layer.BACKGROUND);
        plot.clearRangeMarkers();
        assertTrue(plot.getRangeMarkers(Layer.BACKGROUND).isEmpty());
    }

    // Tests equals with different datasets, renderers, axes
    @Test
    public void testEqualsDifferentDataset() {
        XYPlot p1 = new XYPlot(dataset, domainAxis, rangeAxis, renderer);
        XYSeriesCollection dataset2 = new XYSeriesCollection();
        XYPlot p2 = new XYPlot(dataset2, domainAxis, rangeAxis, renderer);
        assertFalse(p1.equals(p2));
    }

    @Test
    public void testEqualsDifferentRenderer() {
        XYPlot p1 = new XYPlot(dataset, domainAxis, rangeAxis, renderer);
        XYLineAndShapeRenderer renderer2 = new XYLineAndShapeRenderer();
        renderer2.setSeriesLinesVisible(0, true);
        XYPlot p2 = new XYPlot(dataset, domainAxis, rangeAxis, renderer2);
        assertFalse(p1.equals(p2));
    }

    @Test
    public void testEqualsDifferentDomainAxis() {
        NumberAxis axis1 = new NumberAxis("X1");
        NumberAxis axis2 = new NumberAxis("X2");
        XYPlot p1 = new XYPlot(dataset, axis1, rangeAxis, renderer);
        XYPlot p2 = new XYPlot(dataset, axis2, rangeAxis, renderer);
        assertFalse(p1.equals(p2));
    }

    @Test
    public void testEqualsDifferentRangeAxis() {
        NumberAxis axis1 = new NumberAxis("Y1");
        NumberAxis axis2 = new NumberAxis("Y2");
        XYPlot p1 = new XYPlot(dataset, domainAxis, axis1, renderer);
        XYPlot p2 = new XYPlot(dataset, domainAxis, axis2, renderer);
        assertFalse(p1.equals(p2));
    }

    // Tests deep clone and modification
    @Test
    public void testCloneDeepEqualityAfterChange() throws CloneNotSupportedException {
        XYPlot p1 = new XYPlot(dataset, domainAxis, rangeAxis, renderer);
        XYPlot p2 = (XYPlot) p1.clone();
        // change p1's dataset
        XYSeriesCollection newDataset = new XYSeriesCollection();
        p1.setDataset(newDataset);
        assertFalse(p1.equals(p2));
        // change p2's range axis
        p2.setRangeAxis(new NumberAxis("Z"));
        assertFalse(p1.equals(p2));
    }

    // Tests getLegendItems with fixed legend items set
    @Test
    public void testGetLegendItemsWithFixedLegendItems_returnsFixed() {
        LegendItemCollection items = new LegendItemCollection();
        items.add(new LegendItem("fixed"));
        plot.setFixedLegendItems(items);
        assertSame(items, plot.getLegendItems());
    }

    // Tests setFixedLegendItems null
    @Test
    public void testSetFixedLegendItems_null_removesFixed() {
        LegendItemCollection items = new LegendItemCollection();
        plot.setFixedLegendItems(items);
        plot.setFixedLegendItems(null);
        assertNull(plot.getFixedLegendItems());
    }

    // Tests getDomainAxisEdge and getRangeAxisEdge
    @Test
    public void testGetDomainAxisEdge_defaultBottom() {
        XYPlot p = new XYPlot();
        assertEquals(RectangleEdge.BOTTOM, p.getDomainAxisEdge());
    }

    @Test
    public void testGetRangeAxisEdge_defaultLeft() {
        XYPlot p = new XYPlot();
        assertEquals(RectangleEdge.LEFT, p.getRangeAxisEdge());
    }
}