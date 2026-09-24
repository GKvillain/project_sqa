package org.jfree.data.time;

import org.jfree.data.general.SeriesChangeEvent;
import org.jfree.data.general.SeriesChangeListener;
import org.jfree.data.general.SeriesException;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

public class TimeSeriesTest implements SeriesChangeListener {

    private boolean seriesChanged;

    public void seriesChanged(SeriesChangeEvent event) {
        this.seriesChanged = true;
    }

    @Before
    public void setUp() {
        this.seriesChanged = false;
    }

    // Tests createCopy() on empty series (Defects4J Chart-17 bug regression test)
    @Test
    public void testCreateCopy_emptySeries_returnsEmptyClone() throws CloneNotSupportedException {
        TimeSeries series = new TimeSeries("Empty Series", Day.class);
        TimeSeries clone = (TimeSeries) series.clone();
        assertEquals(0, clone.getItemCount());
        assertEquals("Empty Series", clone.getKey());
    }

    // Tests createCopy(RegularTimePeriod, RegularTimePeriod) with valid subset
    @Test
    public void testCreateCopy_validPeriodRange_returnsSubSeries() throws CloneNotSupportedException {
        TimeSeries series = new TimeSeries("Series", Day.class);
        series.add(new Day(1, 1, 2020), 10.0);
        series.add(new Day(2, 1, 2020), 20.0);
        series.add(new Day(3, 1, 2020), 30.0);

        TimeSeries subset = series.createCopy(new Day(1, 1, 2020), new Day(2, 1, 2020));
        assertEquals(2, subset.getItemCount());
        assertEquals(new Double(10.0), subset.getValue(0));
        assertEquals(new Double(20.0), subset.getValue(1));
    }

    // Tests createCopy(RegularTimePeriod, RegularTimePeriod) with start after end throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testCreateCopy_startAfterEnd_throwsException() throws CloneNotSupportedException {
        TimeSeries series = new TimeSeries("Series", Day.class);
        series.createCopy(new Day(2, 1, 2020), new Day(1, 1, 2020));
    }

    // Tests createCopy(RegularTimePeriod, RegularTimePeriod) with null arguments throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testCreateCopy_nullStart_throwsException() throws CloneNotSupportedException {
        TimeSeries series = new TimeSeries("Series", Day.class);
        series.createCopy(null, new Day(1, 1, 2020));
    }

    // Tests createCopy(RegularTimePeriod, RegularTimePeriod) with non-overlapping period range
    @Test
    public void testCreateCopy_outsideRange_returnsEmptySeries() throws CloneNotSupportedException {
        TimeSeries series = new TimeSeries("Series", Day.class);
        series.add(new Day(10, 1, 2020), 10.0);
        series.add(new Day(20, 1, 2020), 20.0);

        TimeSeries subset = series.createCopy(new Day(1, 1, 2020), new Day(5, 1, 2020));
        assertEquals(0, subset.getItemCount());
    }

    // Tests basic add, getIndex, getValue, and getTimePeriod methods
    @Test
    public void testAddAndGet_validItems_retrievesCorrectData() {
        TimeSeries series = new TimeSeries("Test", Day.class);
        series.add(new Day(1, 1, 2020), 100.0);
        series.add(new Day(2, 1, 2020), 200.0);

        assertEquals(2, series.getItemCount());
        assertEquals(0, series.getIndex(new Day(1, 1, 2020)));
        assertEquals(1, series.getIndex(new Day(2, 1, 2020)));
        assertEquals(new Double(100.0), series.getValue(new Day(1, 1, 2020)));
        assertEquals(new Double(200.0), series.getValue(1));
        assertEquals(new Day(1, 1, 2020), series.getTimePeriod(0));
        assertNull(series.getValue(new Day(3, 1, 2020)));
    }

    // Tests add duplicate time period throws SeriesException
    @Test(expected = SeriesException.class)
    public void testAdd_duplicatePeriod_throwsException() {
        TimeSeries series = new TimeSeries("Test", Day.class);
        series.add(new Day(1, 1, 2020), 100.0);
        series.add(new Day(1, 1, 2020), 200.0);
    }

    // Tests add with incompatible period class throws SeriesException
    @Test(expected = SeriesException.class)
    public void testAdd_incompatibleClass_throwsException() {
        TimeSeries series = new TimeSeries("Test", Day.class);
        series.add(new Year(2020), 100.0);
    }

    // Tests addOrUpdate for both inserting new items and updating existing items
    @Test
    public void testAddOrUpdate_newAndExistingItems_updatesCorrectly() {
        TimeSeries series = new TimeSeries("Test", Day.class);
        TimeSeriesDataItem overwritten1 = series.addOrUpdate(new Day(1, 1, 2020), 10.0);
        assertNull(overwritten1);
        assertEquals(1, series.getItemCount());
        assertEquals(new Double(10.0), series.getValue(0));

        TimeSeriesDataItem overwritten2 = series.addOrUpdate(new Day(1, 1, 2020), 20.0);
        assertNotNull(overwritten2);
        assertEquals(new Double(10.0), overwritten2.getValue());
        assertEquals(1, series.getItemCount());
        assertEquals(new Double(20.0), series.getValue(0));
    }

    // Tests update method with existing and non-existing period
    @Test
    public void testUpdate_existingPeriod_updatesValue() {
        TimeSeries series = new TimeSeries("Test", Day.class);
        series.add(new Day(1, 1, 2020), 10.0);
        series.update(new Day(1, 1, 2020), 50.0);
        assertEquals(new Double(50.0), series.getValue(0));
    }

    // Tests update method for non-existing period throws SeriesException
    @Test(expected = SeriesException.class)
    public void testUpdate_nonExistingPeriod_throwsException() {
        TimeSeries series = new TimeSeries("Test", Day.class);
        series.update(new Day(1, 1, 2020), 50.0);
    }

    // Tests maximumItemCount enforcement
    @Test
    public void testSetMaximumItemCount_exceedingLimit_removesOldestItems() {
        TimeSeries series = new TimeSeries("Test", Day.class);
        series.setMaximumItemCount(2);
        series.add(new Day(1, 1, 2020), 1.0);
        series.add(new Day(2, 1, 2020), 2.0);
        series.add(new Day(3, 1, 2020), 3.0);

        assertEquals(2, series.getItemCount());
        assertEquals(new Day(2, 1, 2020), series.getTimePeriod(0));
        assertEquals(new Day(3, 1, 2020), series.getTimePeriod(1));
    }

    // Tests maximumItemAge removal of aged items
    @Test
    public void testRemoveAgedItems_exceedingAge_removesOldest() {
        TimeSeries series = new TimeSeries("Test", Day.class);
        series.setMaximumItemAge(2);
        series.add(new Day(1, 1, 2020), 1.0);
        series.add(new Day(2, 1, 2020), 2.0);
        series.add(new Day(5, 1, 2020), 3.0);

        assertEquals(1, series.getItemCount());
        assertEquals(new Day(5, 1, 2020), series.getTimePeriod(0));
    }

    // Tests delete method with index range and period
    @Test
    public void testDelete_validRangeAndPeriod_deletesItems() {
        TimeSeries series = new TimeSeries("Test", Day.class);
        series.add(new Day(1, 1, 2020), 1.0);
        series.add(new Day(2, 1, 2020), 2.0);
        series.add(new Day(3, 1, 2020), 3.0);

        series.delete(new Day(2, 1, 2020));
        assertEquals(2, series.getItemCount());
        assertEquals(new Day(3, 1, 2020), series.getTimePeriod(1));

        series.delete(0, 1);
        assertEquals(0, series.getItemCount());
    }

    // Tests clear method
    @Test
    public void testClear_populatedSeries_removesAllItems() {
        TimeSeries series = new TimeSeries("Test", Day.class);
        series.add(new Day(1, 1, 2020), 1.0);
        series.add(new Day(2, 1, 2020), 2.0);
        series.clear();
        assertEquals(0, series.getItemCount());
    }

    // Tests equals and hashCode methods
    @Test
    public void testEqualsAndHashCode_sameAndDifferentObjects_worksCorrectly() {
        TimeSeries s1 = new TimeSeries("Series", "Time", "Value", Day.class);
        TimeSeries s2 = new TimeSeries("Series", "Time", "Value", Day.class);

        assertTrue(s1.equals(s1));
        assertTrue(s1.equals(s2));
        assertEquals(s1.hashCode(), s2.hashCode());

        s1.add(new Day(1, 1, 2020), 10.0);
        assertFalse(s1.equals(s2));

        s2.add(new Day(1, 1, 2020), 10.0);
        assertTrue(s1.equals(s2));
        assertEquals(s1.hashCode(), s2.hashCode());

        s2.setDomainDescription("Modified");
        assertFalse(s1.equals(s2));
    }

    // Tests getTimePeriods and getTimePeriodsUniqueToOtherSeries
    @Test
    public void testGetTimePeriodsUniqueToOtherSeries_twoSeries_returnsUniquePeriods() {
        TimeSeries s1 = new TimeSeries("S1", Day.class);
        TimeSeries s2 = new TimeSeries("S2", Day.class);

        Day d1 = new Day(1, 1, 2020);
        Day d2 = new Day(2, 1, 2020);
        Day d3 = new Day(3, 1, 2020);

        s1.add(d1, 1.0);
        s1.add(d2, 2.0);

        s2.add(d2, 2.0);
        s2.add(d3, 3.0);

        Collection unique = s1.getTimePeriodsUniqueToOtherSeries(s2);
        assertEquals(1, unique.size());
        assertTrue(unique.contains(d3));

        Collection allS1 = s1.getTimePeriods();
        assertEquals(2, allS1.size());
        assertTrue(allS1.contains(d1));
        assertTrue(allS1.contains(d2));
    }

    // Tests listener notification when series is modified
    @Test
    public void testNotification_addAndNotify_firesSeriesChangeEvent() {
        TimeSeries series = new TimeSeries("Test", Day.class);
        series.addChangeListener(this);
        assertFalse(this.seriesChanged);

        series.add(new Day(1, 1, 2020), 10.0, true);
        assertTrue(this.seriesChanged);
    }

    // Tests getNextTimePeriod method
    @Test
    public void testGetNextTimePeriod_populatedSeries_returnsNextPeriod() {
        TimeSeries series = new TimeSeries("Test", Day.class);
        series.add(new Day(1, 1, 2020), 10.0);
        RegularTimePeriod next = series.getNextTimePeriod();
        assertEquals(new Day(2, 1, 2020), next);
    }
}