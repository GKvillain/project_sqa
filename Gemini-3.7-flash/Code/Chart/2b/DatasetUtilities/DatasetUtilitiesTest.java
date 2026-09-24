package org.jfree.data.general;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jfree.data.KeyToGroupMap;
import org.jfree.data.Range;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.category.DefaultIntervalCategoryDataset;
import org.jfree.data.function.Function2D;
import org.jfree.data.pie.DefaultPieDataset;
import org.jfree.data.pie.PieDataset;
import org.jfree.data.xy.DefaultTableXYDataset;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link DatasetUtilities}.
 */
public class DatasetUtilitiesTest {

    private static final double EPSILON = 0.0000001;

    // Tests calculatePieDatasetTotal with positive and negative/null values
    @Test
    public void testCalculatePieDatasetTotal_validAndInvalidValues_returnsPositiveSum() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("A", 10.0);
        dataset.setValue("B", -5.0);
        dataset.setValue("C", null);
        dataset.setValue("D", 25.0);

        double total = DatasetUtilities.calculatePieDatasetTotal(dataset);
        assertEquals(35.0, total, EPSILON);
    }

    // Tests calculatePieDatasetTotal with null dataset throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testCalculatePieDatasetTotal_nullDataset_throwsException() {
        DatasetUtilities.calculatePieDatasetTotal(null);
    }

    // Tests createPieDatasetForRow and createPieDatasetForColumn
    @Test
    public void testCreatePieDatasetForRowAndColumn_validDataset_returnsExtractedPieDatasets() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "R1", "C1");
        dataset.addValue(2.0, "R1", "C2");
        dataset.addValue(3.0, "R2", "C1");
        dataset.addValue(4.0, "R2", "C2");

        PieDataset rowPie = DatasetUtilities.createPieDatasetForRow(dataset, "R1");
        assertEquals(2, rowPie.getItemCount());
        assertEquals(1.0, rowPie.getValue("C1").doubleValue(), EPSILON);
        assertEquals(2.0, rowPie.getValue("C2").doubleValue(), EPSILON);

        PieDataset colPie = DatasetUtilities.createPieDatasetForColumn(dataset, 0);
        assertEquals(2, colPie.getItemCount());
        assertEquals(1.0, colPie.getValue("R1").doubleValue(), EPSILON);
        assertEquals(3.0, colPie.getValue("R2").doubleValue(), EPSILON);
    }

    // Tests createConsolidatedPieDataset aggregating below threshold
    @Test
    public void testCreateConsolidatedPieDataset_lowValues_consolidatesUnderOtherKey() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("A", 80.0);
        dataset.setValue("B", 10.0);
        dataset.setValue("C", 5.0);
        dataset.setValue("D", 5.0);

        PieDataset consolidated = DatasetUtilities.createConsolidatedPieDataset(dataset, "Other", 0.15, 2);
        assertEquals(2, consolidated.getItemCount());
        assertEquals(80.0, consolidated.getValue("A").doubleValue(), EPSILON);
        assertEquals(20.0, consolidated.getValue("Other").doubleValue(), EPSILON);
    }

    // Tests createCategoryDataset from 2D double array with prefixes
    @Test
    public void testCreateCategoryDataset_doubleArrayPrefixes_returnsCorrectDataset() {
        double[][] data = new double[][] { { 1.0, 2.0 }, { 3.0, 4.0 } };
        CategoryDataset dataset = DatasetUtilities.createCategoryDataset("Row", "Col", data);

        assertEquals(2, dataset.getRowCount());
        assertEquals(2, dataset.getColumnCount());
        assertEquals(1.0, dataset.getValue("Row1", "Col1").doubleValue(), EPSILON);
        assertEquals(4.0, dataset.getValue("Row2", "Col2").doubleValue(), EPSILON);
    }

    // Tests createCategoryDataset with mismatched rowKeys length throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testCreateCategoryDataset_mismatchedRowKeys_throwsException() {
        Comparable[] rowKeys = new Comparable[] { "R1" };
        Comparable[] colKeys = new Comparable[] { "C1", "C2" };
        double[][] data = new double[][] { { 1.0, 2.0 }, { 3.0, 4.0 } };

        DatasetUtilities.createCategoryDataset(rowKeys, colKeys, data);
    }

    // Tests sampleFunction2D and sampleFunction2DToSeries
    @Test
    public void testSampleFunction2D_linearFunction_createsValidXYDataset() {
        Function2D f = new Function2D() {
            public double getValue(double x) {
                return 2.0 * x;
            }
        };

        XYDataset dataset = DatasetUtilities.sampleFunction2D(f, 0.0, 4.0, 5, "Line");
        assertEquals(1, dataset.getSeriesCount());
        assertEquals(5, dataset.getItemCount(0));
        assertEquals(0.0, dataset.getXValue(0, 0), EPSILON);
        assertEquals(0.0, dataset.getYValue(0, 0), EPSILON);
        assertEquals(4.0, dataset.getXValue(0, 4), EPSILON);
        assertEquals(8.0, dataset.getYValue(0, 4), EPSILON);
    }

    // Tests sampleFunction2D with start >= end throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testSampleFunction2DToSeries_invalidRange_throwsException() {
        Function2D f = new Function2D() {
            public double getValue(double x) {
                return x;
            }
        };
        DatasetUtilities.sampleFunction2DToSeries(f, 5.0, 2.0, 10, "S1");
    }

    // Tests isEmptyOrNull for PieDataset, CategoryDataset, and XYDataset
    @Test
    public void testIsEmptyOrNull_variousDatasets_returnsExpectedBooleans() {
        assertTrue(DatasetUtilities.isEmptyOrNull((PieDataset) null));
        assertTrue(DatasetUtilities.isEmptyOrNull(new DefaultPieDataset()));

        DefaultPieDataset pie = new DefaultPieDataset();
        pie.setValue("A", 0.0);
        assertTrue(DatasetUtilities.isEmptyOrNull(pie));
        pie.setValue("B", 10.0);
        assertFalse(DatasetUtilities.isEmptyOrNull(pie));

        assertTrue(DatasetUtilities.isEmptyOrNull((CategoryDataset) null));
        assertTrue(DatasetUtilities.isEmptyOrNull(new DefaultCategoryDataset()));

        DefaultCategoryDataset cat = new DefaultCategoryDataset();
        cat.addValue(null, "R1", "C1");
        assertTrue(DatasetUtilities.isEmptyOrNull(cat));
        cat.addValue(5.0, "R1", "C2");
        assertFalse(DatasetUtilities.isEmptyOrNull(cat));

        assertTrue(DatasetUtilities.isEmptyOrNull((XYDataset) null));
        assertTrue(DatasetUtilities.isEmptyOrNull(new XYSeriesCollection()));

        XYSeries series = new XYSeries("S1");
        XYSeriesCollection xyCol = new XYSeriesCollection(series);
        assertTrue(DatasetUtilities.isEmptyOrNull(xyCol));
        series.add(1.0, 2.0);
        assertFalse(DatasetUtilities.isEmptyOrNull(xyCol));
    }

    // Tests findDomainBounds and iterateDomainBounds with regular XYDataset
    @Test
    public void testFindDomainBounds_regularXYDataset_returnsCorrectRange() {
        XYSeries series = new XYSeries("S1");
        series.add(1.0, 10.0);
        series.add(5.0, 20.0);
        series.add(3.0, 15.0);
        XYSeriesCollection collection = new XYSeriesCollection(series);

        Range range = DatasetUtilities.findDomainBounds(collection);
        assertNotNull(range);
        assertEquals(1.0, range.getLowerBound(), EPSILON);
        assertEquals(5.0, range.getUpperBound(), EPSILON);
    }

    // Tests iterateDomainBounds when dataset contains Double.NaN
    @Test
    public void testIterateDomainBounds_datasetWithNaN_ignoresNaNAndReturnsValidRange() {
        DefaultXYDataset dataset = new DefaultXYDataset();
        double[][] data = new double[][] { { Double.NaN, 2.0, 8.0 }, { 1.0, Double.NaN, 3.0 } };
        dataset.addSeries("S1", data);

        Range domainRange = DatasetUtilities.iterateDomainBounds(dataset, false);
        assertNotNull(domainRange);
        assertEquals(2.0, domainRange.getLowerBound(), EPSILON);
        assertEquals(8.0, domainRange.getUpperBound(), EPSILON);

        Range rangeRange = DatasetUtilities.iterateRangeBounds(dataset, false);
        assertNotNull(rangeRange);
        assertEquals(1.0, rangeRange.getLowerBound(), EPSILON);
        assertEquals(3.0, rangeRange.getUpperBound(), EPSILON);
    }

    // Tests iterateDomainBounds and iterateRangeBounds with all NaN values returning null
    @Test
    public void testIterateBounds_allNaN_returnsNull() {
        DefaultXYDataset dataset = new DefaultXYDataset();
        double[][] data = new double[][] { { Double.NaN, Double.NaN }, { Double.NaN, Double.NaN } };
        dataset.addSeries("S1", data);

        assertNull(DatasetUtilities.iterateDomainBounds(dataset, false));
        assertNull(DatasetUtilities.iterateRangeBounds(dataset, false));
    }

    // Tests findRangeBounds and iterateRangeBounds with CategoryDataset
    @Test
    public void testFindRangeBounds_categoryDataset_returnsCorrectMinMax() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(-10.0, "R1", "C1");
        dataset.addValue(25.0, "R1", "C2");
        dataset.addValue(null, "R2", "C1");
        dataset.addValue(15.0, "R2", "C2");

        Range range = DatasetUtilities.findRangeBounds(dataset, false);
        assertNotNull(range);
        assertEquals(-10.0, range.getLowerBound(), EPSILON);
        assertEquals(25.0, range.getUpperBound(), EPSILON);

        Number minVal = DatasetUtilities.findMinimumRangeValue(dataset);
        Number maxVal = DatasetUtilities.findMaximumRangeValue(dataset);
        assertEquals(-10.0, minVal.doubleValue(), EPSILON);
        assertEquals(25.0, maxVal.doubleValue(), EPSILON);
    }

    // Tests iterateRangeBounds on IntervalCategoryDataset including interval bounds
    @Test
    public void testIterateRangeBounds_intervalCategoryDataset_includesInterval() {
        Double[][] starts = new Double[][] { { 1.0, 2.0 } };
        Double[][] ends = new Double[][] { { 3.0, 6.0 } };
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(
                new Comparable[] { "R1" }, new Comparable[] { "C1", "C2" }, starts, ends);

        Range range = DatasetUtilities.iterateRangeBounds(dataset, true);
        assertNotNull(range);
        assertEquals(1.0, range.getLowerBound(), EPSILON);
        assertEquals(6.0, range.getUpperBound(), EPSILON);
    }

    // Tests iterateToFindRangeBounds for XYDataset filtered by visible series and xRange
    @Test
    public void testIterateToFindRangeBounds_xyDatasetFiltered_returnsSubRange() {
        DefaultXYDataset dataset = new DefaultXYDataset();
        double[][] data1 = new double[][] { { 1.0, 2.0, 3.0, 4.0 }, { 10.0, 20.0, 30.0, 40.0 } };
        double[][] data2 = new double[][] { { 1.0, 2.0, 3.0, 4.0 }, { 100.0, 200.0, 300.0, 400.0 } };
        dataset.addSeries("S1", data1);
        dataset.addSeries("S2", data2);

        List visibleKeys = Collections.singletonList("S1");
        Range xRange = new Range(1.5, 3.5);

        Range yRange = DatasetUtilities.iterateToFindRangeBounds(dataset, visibleKeys, xRange, false);
        assertNotNull(yRange);
        assertEquals(20.0, yRange.getLowerBound(), EPSILON);
        assertEquals(30.0, yRange.getUpperBound(), EPSILON);
    }

    // Tests findStackedRangeBounds for CategoryDataset with positive and negative stacks
    @Test
    public void testFindStackedRangeBounds_categoryDataset_calculatesPositiveAndNegativeTotals() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(5.0, "R1", "C1");
        dataset.addValue(-3.0, "R2", "C1");
        dataset.addValue(10.0, "R1", "C2");
        dataset.addValue(-7.0, "R2", "C2");

        Range stackedRange = DatasetUtilities.findStackedRangeBounds(dataset, 0.0);
        assertNotNull(stackedRange);
        assertEquals(-7.0, stackedRange.getLowerBound(), EPSILON);
        assertEquals(10.0, stackedRange.getUpperBound(), EPSILON);

        Number minStacked = DatasetUtilities.findMinimumStackedRangeValue(dataset);
        Number maxStacked = DatasetUtilities.findMaximumStackedRangeValue(dataset);
        assertEquals(-7.0, minStacked.doubleValue(), EPSILON);
        assertEquals(10.0, maxStacked.doubleValue(), EPSILON);
    }

    // Tests findStackedRangeBounds for CategoryDataset with KeyToGroupMap
    @Test
    public void testFindStackedRangeBounds_keyToGroupMap_correctlyCombinesGroupRanges() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(10.0, "R1", "C1");
        dataset.addValue(20.0, "R2", "C1");
        dataset.addValue(30.0, "R3", "C1");

        KeyToGroupMap map = new KeyToGroupMap("G1");
        map.mapKeyToGroup("R1", "G1");
        map.mapKeyToGroup("R2", "G1");
        map.mapKeyToGroup("R3", "G2");

        Range range = DatasetUtilities.findStackedRangeBounds(dataset, map);
        assertNotNull(range);
        assertEquals(0.0, range.getLowerBound(), EPSILON);
        assertEquals(30.0, range.getUpperBound(), EPSILON);
    }

    // Tests findStackedRangeBounds and calculateStackTotal for TableXYDataset
    @Test
    public void testStackedRangeBoundsAndCalculateStackTotal_tableXYDataset_returnsStackedBounds() {
        DefaultTableXYDataset dataset = new DefaultTableXYDataset();
        XYSeries s1 = new XYSeries("S1", true, false);
        s1.add(1.0, 5.0);
        s1.add(2.0, 15.0);
        XYSeries s2 = new XYSeries("S2", true, false);
        s2.add(1.0, -2.0);
        s2.add(2.0, 10.0);

        dataset.addSeries(s1);
        dataset.addSeries(s2);

        Range range = DatasetUtilities.findStackedRangeBounds(dataset, 0.0);
        assertNotNull(range);
        assertEquals(-2.0, range.getLowerBound(), EPSILON);
        assertEquals(25.0, range.getUpperBound(), EPSILON);

        double totalItem0 = DatasetUtilities.calculateStackTotal(dataset, 0);
        assertEquals(3.0, totalItem0, EPSILON);
    }

    // Tests findCumulativeRangeBounds for CategoryDataset
    @Test
    public void testFindCumulativeRangeBounds_categoryDataset_calculatesCumulativeRunningTotals() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(10.0, "R1", "C1");
        dataset.addValue(-15.0, "R1", "C2");
        dataset.addValue(20.0, "R1", "C3");

        Range cumulativeRange = DatasetUtilities.findCumulativeRangeBounds(dataset);
        assertNotNull(cumulativeRange);
        assertEquals(-5.0, cumulativeRange.getLowerBound(), EPSILON);
        assertEquals(15.0, cumulativeRange.getUpperBound(), EPSILON);
    }

    // Tests findCumulativeRangeBounds with null/empty dataset
    @Test
    public void testFindCumulativeRangeBounds_emptyOrAllNullDataset_returnsNull() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(null, "R1", "C1");
        assertNull(DatasetUtilities.findCumulativeRangeBounds(dataset));
    }
}