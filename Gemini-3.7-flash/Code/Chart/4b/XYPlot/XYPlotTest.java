package org.jfree.chart.plot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.util.Layer;
import org.jfree.chart.util.RectangleInsets;
import org.jfree.data.Range;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class XYPlotTest {

    private XYPlot plot;
    private NumberAxis domainAxis;
    private NumberAxis rangeAxis;
    private XYSeriesCollection dataset;
    private XYLineAndShapeRenderer renderer;

    @Before
    public void setUp() {
        this.domainAxis = new NumberAxis("X");
        this.rangeAxis = new NumberAxis("Y");
        this.dataset = new XYSeriesCollection();
        XYSeries series = new XYSeries("S1");
        series.add(1.0, 10.0);
        series.add(2.0, 20.0);
        this.dataset.addSeries(series);
        this.renderer = new XYLineAndShapeRenderer();
        this.plot = new XYPlot(this.dataset, this.domainAxis, this.rangeAxis, this.renderer);
    }

    // Tests defect where getDataRange throws NullPointerException when renderer is null
    @Test
    public void testGetDataRange_nullRenderer_returnsDatasetBounds() {
        this.plot.setRenderer(null);
        Range domainRange = this.plot.getDataRange(this.domainAxis);
        assertNotNull(domainRange);
        assertEquals(1.0, domainRange.getLowerBound(), 0.0001);
        assertEquals(2.0, domainRange.getUpperBound(), 0.0001);

        Range rangeBounds = this.plot.getDataRange(this.rangeAxis);
        assertNotNull(rangeBounds);
        assertEquals(10.0, rangeBounds.getLowerBound(), 0.0001);
        assertEquals(20.0, rangeBounds.getUpperBound(), 0.0001);
    }

    // Tests normal case of getDataRange with non-null dataset and renderer
    @Test
    public void testGetDataRange_normal_returnsCorrectRange() {
        Range domainRange = this.plot.getDataRange(this.domainAxis);
        assertNotNull(domainRange);
        assertEquals(1.0, domainRange.getLowerBound(), 0.0001);
        assertEquals(2.0, domainRange.getUpperBound(), 0.0001);

        Range rangeBounds = this.plot.getDataRange(this.rangeAxis);
        assertNotNull(rangeBounds);
        assertEquals(10.0, rangeBounds.getLowerBound(), 0.0001);
        assertEquals(20.0, rangeBounds.getUpperBound(), 0.0001);
    }

    // Tests getDataRange when dataset is empty or axis is unmapped
    @Test
    public void testGetDataRange_unmappedAxis_returnsNull() {
        NumberAxis unmappedAxis = new NumberAxis("Unmapped");
        Range r = this.plot.getDataRange(unmappedAxis);
        assertNull(r);
    }

    // Tests default constructor initialization
    @Test
    public void testConstructor_default_initializesCorrectly() {
        XYPlot p = new XYPlot();
        assertNull(p.getDataset());
        assertNull(p.getDomainAxis());
        assertNull(p.getRangeAxis());
        assertNull(p.getRenderer());
        assertEquals(PlotOrientation.VERTICAL, p.getOrientation());
        assertEquals("XY Plot", p.getPlotType());
    }

    // Tests setOrientation with valid and null input
    @Test
    public void testSetOrientation_validAndNull_updatesCorrectly() {
        this.plot.setOrientation(PlotOrientation.HORIZONTAL);
        assertEquals(PlotOrientation.HORIZONTAL, this.plot.getOrientation());

        this.plot.setOrientation(PlotOrientation.VERTICAL);
        assertEquals(PlotOrientation.VERTICAL, this.plot.getOrientation());
    }

    // Tests setOrientation throws IllegalArgumentException when null
    @Test(expected = IllegalArgumentException.class)
    public void testSetOrientation_nullInput_throwsException() {
        this.plot.setOrientation(null);
    }

    // Tests axis offset getter and setter
    @Test
    public void testSetAxisOffset_validInput_setsOffset() {
        RectangleInsets insets = new RectangleInsets(5.0, 5.0, 5.0, 5.0);
        this.plot.setAxisOffset(insets);
        assertEquals(insets, this.plot.getAxisOffset());
    }

    // Tests setAxisOffset throws IllegalArgumentException when null
    @Test(expected = IllegalArgumentException.class)
    public void testSetAxisOffset_nullInput_throwsException() {
        this.plot.setAxisOffset(null);
    }

    // Tests get and set domain axis
    @Test
    public void testSetDomainAxis_multipleAxes_managesIndicesCorrectly() {
        NumberAxis axis2 = new NumberAxis("X2");
        this.plot.setDomainAxis(1, axis2);
        assertEquals(2, this.plot.getDomainAxisCount());
        assertEquals(axis2, this.plot.getDomainAxis(1));

        assertEquals(0, this.plot.getDomainAxisIndex(this.domainAxis));
        assertEquals(1, this.plot.getDomainAxisIndex(axis2));

        this.plot.clearDomainAxes();
        assertEquals(0, this.plot.getDomainAxisCount());
    }

    // Tests get and set range axis
    @Test
    public void testSetRangeAxis_multipleAxes_managesIndicesCorrectly() {
        NumberAxis axis2 = new NumberAxis("Y2");
        this.plot.setRangeAxis(1, axis2);
        assertEquals(2, this.plot.getRangeAxisCount());
        assertEquals(axis2, this.plot.getRangeAxis(1));

        assertEquals(0, this.plot.getRangeAxisIndex(this.rangeAxis));
        assertEquals(1, this.plot.getRangeAxisIndex(axis2));

        this.plot.clearRangeAxes();
        assertEquals(0, this.plot.getRangeAxisCount());
    }

    // Tests dataset handling
    @Test
    public void testDataset_indexAndCount_tracksCorrectly() {
        assertEquals(1, this.plot.getDatasetCount());
        assertEquals(0, this.plot.indexOf(this.dataset));

        XYSeriesCollection dataset2 = new XYSeriesCollection();
        this.plot.setDataset(1, dataset2);
        assertEquals(2, this.plot.getDatasetCount());
        assertEquals(dataset2, this.plot.getDataset(1));
        assertEquals(1, this.plot.indexOf(dataset2));

        assertEquals(-1, this.plot.indexOf(new XYSeriesCollection()));
    }

    // Tests mapDatasetToDomainAxes with valid list and invalid input
    @Test
    public void testMapDatasetToDomainAxes_validIndices_mapsCorrectly() {
        List<Integer> axes = new ArrayList<Integer>();
        axes.add(new Integer(0));
        this.plot.mapDatasetToDomainAxes(0, axes);
        assertEquals(this.domainAxis, this.plot.getDomainAxisForDataset(0));
    }

    // Tests mapDatasetToDomainAxes with negative index throws Exception
    @Test(expected = IllegalArgumentException.class)
    public void testMapDatasetToDomainAxes_negativeIndex_throwsException() {
        List<Integer> axes = new ArrayList<Integer>();
        axes.add(new Integer(0));
        this.plot.mapDatasetToDomainAxes(-1, axes);
    }

    // Tests mapDatasetToRangeAxes with negative index throws Exception
    @Test(expected = IllegalArgumentException.class)
    public void testMapDatasetToRangeAxes_negativeIndex_throwsException() {
        List<Integer> axes = new ArrayList<Integer>();
        axes.add(new Integer(0));
        this.plot.mapDatasetToRangeAxes(-1, axes);
    }

    // Tests domain and range markers add, remove, and clear
    @Test
    public void testMarkers_addAndRemove_behavesCorrectly() {
        ValueMarker marker1 = new ValueMarker(1.5);
        ValueMarker marker2 = new ValueMarker(15.0);

        this.plot.addDomainMarker(marker1, Layer.FOREGROUND);
        Collection domainMarkers = this.plot.getDomainMarkers(Layer.FOREGROUND);
        assertNotNull(domainMarkers);
        assertTrue(domainMarkers.contains(marker1));

        this.plot.addRangeMarker(marker2, Layer.BACKGROUND);
        Collection rangeMarkers = this.plot.getRangeMarkers(0, Layer.BACKGROUND);
        assertNotNull(rangeMarkers);
        assertTrue(rangeMarkers.contains(marker2));

        boolean removedDomain = this.plot.removeDomainMarker(marker1, Layer.FOREGROUND);
        assertTrue(removedDomain);
        assertFalse(this.plot.getDomainMarkers(Layer.FOREGROUND).contains(marker1));

        boolean removedRange = this.plot.removeRangeMarker(0, marker2, Layer.BACKGROUND);
        assertTrue(removedRange);

        this.plot.addDomainMarker(marker1);
        this.plot.addRangeMarker(marker2);
        this.plot.clearDomainMarkers();
        this.plot.clearRangeMarkers();
        assertNull(this.plot.getDomainMarkers(0, Layer.FOREGROUND));
        assertNull(this.plot.getRangeMarkers(0, Layer.FOREGROUND));
    }

    // Tests annotations addition, retrieval, removal, and clearing
    @Test
    public void testAnnotations_addRemoveClear_functionsCorrectly() {
        XYTextAnnotation annotation = new XYTextAnnotation("Label", 1.0, 10.0);
        this.plot.addAnnotation(annotation);

        List annotations = this.plot.getAnnotations();
        assertEquals(1, annotations.size());
        assertTrue(annotations.contains(annotation));

        boolean removed = this.plot.removeAnnotation(annotation);
        assertTrue(removed);
        assertEquals(0, this.plot.getAnnotations().size());

        this.plot.addAnnotation(annotation);
        this.plot.clearAnnotations();
        assertEquals(0, this.plot.getAnnotations().size());
    }

    // Tests quadrant paint settings and boundary checks
    @Test
    public void testQuadrantPaint_validAndInvalidIndices_getsAndSets() {
        this.plot.setQuadrantOrigin(new Point2D.Double(1.5, 15.0));
        assertEquals(new Point2D.Double(1.5, 15.0), this.plot.getQuadrantOrigin());

        this.plot.setQuadrantPaint(0, Color.RED);
        assertEquals(Color.RED, this.plot.getQuadrantPaint(0));
        assertNull(this.plot.getQuadrantPaint(1));
    }

    // Tests getQuadrantPaint boundary with invalid index (< 0)
    @Test(expected = IllegalArgumentException.class)
    public void testGetQuadrantPaint_negativeIndex_throwsException() {
        this.plot.getQuadrantPaint(-1);
    }

    // Tests getQuadrantPaint boundary with invalid index (> 3)
    @Test(expected = IllegalArgumentException.class)
    public void testGetQuadrantPaint_tooLargeIndex_throwsException() {
        this.plot.getQuadrantPaint(4);
    }

    // Tests pannable and zoomable properties
    @Test
    public void testPanAndZoomProperties_flagsAndOperations_updatesCorrectly() {
        assertTrue(this.plot.isDomainZoomable());
        assertTrue(this.plot.isRangeZoomable());

        assertFalse(this.plot.isDomainPannable());
        assertFalse(this.plot.isRangePannable());

        this.plot.setDomainPannable(true);
        this.plot.setRangePannable(true);
        assertTrue(this.plot.isDomainPannable());
        assertTrue(this.plot.isRangePannable());

        this.plot.zoomDomainAxes(0.5, null, new Point2D.Double(0.0, 0.0));
        this.plot.zoomRangeAxes(0.5, null, new Point2D.Double(0.0, 0.0));
    }

    // Tests crosshair settings
    @Test
    public void testCrosshairs_setAndGet_retainsValues() {
        this.plot.setDomainCrosshairVisible(true);
        assertTrue(this.plot.isDomainCrosshairVisible());
        this.plot.setDomainCrosshairValue(1.23);
        assertEquals(1.23, this.plot.getDomainCrosshairValue(), 0.0001);

        this.plot.setRangeCrosshairVisible(true);
        assertTrue(this.plot.isRangeCrosshairVisible());
        this.plot.setRangeCrosshairValue(4.56);
        assertEquals(4.56, this.plot.getRangeCrosshairValue(), 0.0001);

        Stroke stroke = new BasicStroke(1.5f);
        this.plot.setDomainCrosshairStroke(stroke);
        assertEquals(stroke, this.plot.getDomainCrosshairStroke());

        this.plot.setDomainCrosshairPaint(Color.GREEN);
        assertEquals(Color.GREEN, this.plot.getDomainCrosshairPaint());
    }

    // Tests equals and clone methods
    @Test
    public void testEqualsAndClone_deepCopyAndEquality_worksCorrectly() throws CloneNotSupportedException {
        XYPlot clone = (XYPlot) this.plot.clone();
        assertEquals(this.plot, clone);
        assertNotSame(this.plot, clone);

        clone.setWeight(2);
        assertFalse(this.plot.equals(clone));
    }

    // Tests fixed legend items
    @Test
    public void testLegendItems_fixedAndGenerated_returnsItems() {
        LegendItemCollection generated = this.plot.getLegendItems();
        assertNotNull(generated);

        LegendItemCollection fixed = new LegendItemCollection();
        fixed.add(new LegendItem("Custom"));
        this.plot.setFixedLegendItems(fixed);
        assertEquals(fixed, this.plot.getFixedLegendItems());
        assertEquals(1, this.plot.getLegendItems().getItemCount());
    }
}