package org.jfree.data.general;

import org.junit.Test;
import static org.junit.Assert.*;
import org.jfree.data.Range;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.pie.DefaultPieDataset;
import org.jfree.data.pie.PieDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.category.DefaultIntervalCategoryDataset;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYIntervalSeries;
import org.jfree.data.xy.XYIntervalSeriesCollection;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.function.Function2D;
import java.util.List;
import java.util.ArrayList;

public class DatasetUtilitiesTest {

    // Tests that calculatePieDatasetTotal throws exception on null input
    @Test(expected = IllegalArgumentException.class)
    public void testCalculatePieDatasetTotal_nullDataset_throwsException() {
        DatasetUtilities.calculatePieDatasetTotal(null);
    }

    // Tests normal total calculation with positive values
    @Test
    public void testCalculatePieDatasetTotal_positiveValues_returnsCorrectTotal() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("A", 1.0);
        dataset.setValue("B", 2.0);
        dataset.setValue("C", 3.0);
        double total = DatasetUtilities.calculatePieDatasetTotal(dataset);
        assertEquals(6.0, total, 0.0000001);
    }

    // Tests that negative values are ignored in total
    @Test
    public void testCalculatePieDatasetTotal_negativeValues_ignored() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("A", 5.0);
        dataset.setValue("B", -2.0);
        dataset.setValue("C", 3.0);
        double total = DatasetUtilities.calculatePieDatasetTotal(dataset);
        assertEquals(8.0, total, 0.0000001);
    }

    // Tests that null value is ignored in total
    @Test
    public void testCalculatePieDatasetTotal_nullValue_ignored() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("A", 4.0);
        dataset.setValue("B", null);
        dataset.setValue("C", 6.0);
        double total = DatasetUtilities.calculatePieDatasetTotal(dataset);
        assertEquals(10.0, total, 0.0000001);
    }

    // Tests isEmptyOrNull for null PieDataset returns true
    @Test
    public void testIsEmptyOrNull_PieDataset_null_returnsTrue() {
        assertTrue(DatasetUtilities.isEmptyOrNull((PieDataset) null));
    }

    // Tests isEmptyOrNull for empty PieDataset returns true
    @Test
    public void testIsEmptyOrNull_PieDataset_empty_returnsTrue() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        assertTrue(DatasetUtilities.isEmptyOrNull(dataset));
    }

    // Tests isEmptyOrNull for CategoryDataset with all null values returns true
    @Test
    public void testIsEmptyOrNull_CategoryDataset_allNull_returnsTrue() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(null, "R1", "C1");
        dataset.addValue(null, "R1", "C2");
        assertTrue(DatasetUtilities.isEmptyOrNull(dataset));
    }

    // Tests isEmptyOrNull for XYDataset with no items returns true
    @Test
    public void testIsEmptyOrNull_XYDataset_noItems_returnsTrue() {
        XYSeries series = new XYSeries("S1");
        XYSeriesCollection dataset = new XYSeriesCollection(series);
        assertTrue(DatasetUtilities.isEmptyOrNull(dataset));
    }

    // Tests findDomainBounds on plain XYDataset returns correct range
    @Test
    public void testFindDomainBounds_XYDataset_returnsRange() {
        XYSeries series = new XYSeries("S1");
        series.add(1.0, 10.0);
        series.add(5.0, 20.0);
        XYSeriesCollection dataset = new XYSeriesCollection(series);
        Range range = DatasetUtilities.findDomainBounds(dataset);
        assertEquals(new Range(1.0, 5.0), range);
    }

    // Tests findDomainBounds on IntervalXYDataset with includeInterval
    @Test
    public void testFindDomainBounds_IntervalXYDataset_includeInterval_returnsRange() {
        XYIntervalSeries series = new XYIntervalSeries("S1");
        series.add(1.0, 0.5, 1.5, 10.0, 9.0, 11.0);
        XYIntervalSeriesCollection dataset = new XYIntervalSeriesCollection();
        dataset.addSeries(series);
        Range range = DatasetUtilities.findDomainBounds(dataset, true);
        assertEquals(new Range(0.5, 1.5), range);
    }

    // Tests findRangeBounds on plain XYDataset
    @Test
    public void testFindRangeBounds_XYDataset_returnsRange() {
        XYSeries series = new XYSeries("S1");
        series.add(1.0, 10.0);
        series.add(2.0, 20.0);
        XYSeriesCollection dataset = new XYSeriesCollection(series);
        Range range = DatasetUtilities.findRangeBounds(dataset);
        assertEquals(new Range(10.0, 20.0), range);
    }

    // Tests iterateRangeBounds on CategoryDataset with plain values
    @Test
    public void testIterateRangeBounds_CategoryDataset_plain_returnsRange() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(10.0, "R1", "C1");
        dataset.addValue(20.0, "R1", "C2");
        dataset.addValue(5.0, "R2", "C1");
        Range range = DatasetUtilities.iterateRangeBounds(dataset);
        assertEquals(new Range(5.0, 20.0), range);
    }

    // Tests iterateRangeBounds on IntervalCategoryDataset with intervals
    @Test
    public void testIterateRangeBounds_CategoryDataset_interval_returnsRange() {
        double[][] starts = {{1.0, 2.0}, {3.0, 4.0}};
        double[][] ends = {{5.0, 6.0}, {7.0, 8.0}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(
                new String[]{"R1", "R2"}, new String[]{"C1", "C2"}, starts, ends);
        Range range = DatasetUtilities.iterateRangeBounds(dataset, true);
        assertEquals(new Range(1.0, 8.0), range);
    }

    // Tests iterateRangeBounds on XYDataset with plain values
    @Test
    public void testIterateRangeBounds_XYDataset_plain_returnsRange() {
        XYSeries series = new XYSeries("S1");
        series.add(1.0, 10.0);
        series.add(2.0, 20.0);
        XYSeriesCollection dataset = new XYSeriesCollection(series);
        Range range = DatasetUtilities.iterateRangeBounds(dataset, false);
        assertEquals(new Range(10.0, 20.0), range);
    }

    // Tests iterateRangeBounds on IntervalXYDataset with intervals
    @Test
    public void testIterateRangeBounds_XYDataset_interval_returnsRange() {
        XYIntervalSeries series = new XYIntervalSeries("S1");
        series.add(1.0, 0.5, 1.5, 10.0, 9.0, 11.0);
        XYIntervalSeriesCollection dataset = new XYIntervalSeriesCollection();
        dataset.addSeries(series);
        Range range = DatasetUtilities.iterateRangeBounds(dataset, true);
        assertEquals(new Range(9.0, 11.0), range);
    }

    // Tests findStackedRangeBounds on CategoryDataset with positive and negative values
    @Test
    public void testFindStackedRangeBounds_CategoryDataset_returnsRange() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(10.0, "S1", "C1");
        dataset.addValue(5.0, "S2", "C1");
        dataset.addValue(-3.0, "S1", "C2");
        dataset.addValue(2.0, "S2", "C2");
        Range range = DatasetUtilities.findStackedRangeBounds(dataset, 0.0);
        assertEquals(new Range(-3.0, 15.0), range);
    }

    // Tests findCumulativeRangeBounds on CategoryDataset
    @Test
    public void testFindCumulativeRangeBounds_nonNullValues_returnsRange() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "R1", "C1");
        dataset.addValue(2.0, "R1", "C2");
        dataset.addValue(3.0, "R1", "C3");
        Range range = DatasetUtilities.findCumulativeRangeBounds(dataset);
        assertEquals(new Range(1.0, 6.0), range);
    }

    // Tests sampleFunction2DToSeries with start >= end throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testSampleFunction2DToSeries_startGreaterThanEnd_throwsException() {
        Function2D f = new Function2D() {
            public double getValue(double x) { return x; }
        };
        DatasetUtilities.sampleFunction2DToSeries(f, 5.0, 3.0, 10, "S");
    }

    // Tests sampleFunction2DToSeries with samples < 2 throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testSampleFunction2DToSeries_samplesLessThanTwo_throwsException() {
        Function2D f = new Function2D() {
            public double getValue(double x) { return x; }
        };
        DatasetUtilities.sampleFunction2DToSeries(f, 0.0, 10.0, 1, "S");
    }

    // Tests findRangeBounds on CategoryDataset with plain values
    @Test
    public void testFindRangeBounds_CategoryDataset_returnsRange() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(7.0, "R1", "C1");
        dataset.addValue(3.0, "R1", "C2");
        Range range = DatasetUtilities.findRangeBounds(dataset);
        assertEquals(new Range(3.0, 7.0), range);
    }
}