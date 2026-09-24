package org.jfree.data.time;

import org.jfree.data.general.SeriesChangeEvent;
import org.jfree.data.general.SeriesChangeListener;
import org.jfree.data.general.SeriesException;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TimeSeriesTest implements SeriesChangeListener {

    private TimeSeries series;
    private boolean gotSeriesChangeEvent;

    public void seriesChanged(SeriesChangeEvent event) {
        this.gotSeriesChangeEvent = true;
    }

    @Before
    public void setUp() {
        this.series = new TimeSeries("Test Series");
        this.series.addChangeListener(this);
        this.gotSeriesChangeEvent = false;
    }

    // Tests initial state and default property values
    @Test
    public void testConstructor_defaultValues_initializedProperly() {
        assertEquals("Test Series", this.series.getKey());
        assertEquals("Time", this.series.getDomainDescription());
        assertEquals("Value", this.series.getRangeDescription());
        assertEquals(0, this.series.getItemCount());
        assertEquals(Integer.MAX_VALUE, this.series.getMaximumItemCount());
        assertEquals(Long.MAX_VALUE, this.series.getMaximumItemAge());
        assertTrue(Double.isNaN(this.series.getMinY()));
        assertTrue(Double.isNaN(this.series.getMaxY()));
        assertNull(this.series.getTimePeriodClass());
    }

    // Tests setting domain and range descriptions with event notification
    @Test
    public void testSetDescriptions_validStrings_updatesProperties() {
        this.series.setDomainDescription("New Domain");
        assertEquals("New Domain", this.series.getDomainDescription());
        this.series.setRangeDescription("New Range");
        assertEquals("New Range", this.series.getRangeDescription());
    }

    // Tests adding regular items and tracking min/max Y bounds
    @Test
    public void testAdd_multipleItems_updatesBoundsAndCount() {
        Day d1 = new Day(1, 1, 2020);
        Day d2 = new Day(2, 1, 2020);
        Day d3 = new Day(3, 1, 2020);

        this.series.add(d1, 10.0);
        assertEquals(1, this.series.getItemCount());
        assertEquals(Day.class, this.series.getTimePeriodClass());
        assertEquals(10.0, this.series.getMinY(), 1e-9);
        assertEquals(10.0, this.series.getMaxY(), 1e-9);
        assertTrue(this.gotSeriesChangeEvent);

        this.series.add(d2, 25.0);
        this.series.add(d3, 5.0);

        assertEquals(3, this.series.getItemCount());
        assertEquals(5.0, this.series.getMinY(), 1e-9);
        assertEquals(25.0, this.series.getMaxY(), 1e-9);
        assertEquals(new Double(25.0), this.series.getValue(d2));
    }

    // Tests adding items in unsorted order (binary search insertion)
    @Test
    public void testAdd_outOfOrder_sortsCorrectly() {
        Day d1 = new Day(1, 1, 2020);
        Day d2 = new Day(2, 1, 2020);
        Day d3 = new Day(3, 1, 2020);

        this.series.add(d3, 30.0);
        this.series.add(d1, 10.0);
        this.series.add(d2, 20.0);

        assertEquals(d1, this.series.getTimePeriod(0));
        assertEquals(d2, this.series.getTimePeriod(1));
        assertEquals(d3, this.series.getTimePeriod(2));
    }

    // Tests adding duplicate time period throws SeriesException
    @Test(expected = SeriesException.class)
    public void testAdd_duplicatePeriod_throwsSeriesException() {
        Day d1 = new Day(1, 1, 2020);
        this.series.add(d1, 10.0);
        this.series.add(d1, 20.0);
    }

    // Tests adding incompatible time period class throws SeriesException
    @Test(expected = SeriesException.class)
    public void testAdd_incompatiblePeriodClass_throwsSeriesException() {
        this.series.add(new Day(1, 1, 2020), 10.0);
        this.series.add(new Year(2021), 20.0);
    }

    // Tests adding null item throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testAdd_nullItem_throwsIllegalArgumentException() {
        this.series.add((TimeSeriesDataItem) null);
    }

    // Tests addOrUpdate with existing period updates maxY and minY correctly (Defects4J Chart-3 target)
    @Test
    public void testAddOrUpdate_updateExistingItemHigherValue_updatesMaxYCorrectly() {
        Day d1 = new Day(1, 1, 2020);
        Day d2 = new Day(2, 1, 2020);

        this.series.add(d1, 10.0);
        this.series.add(d2, 20.0);

        assertEquals(10.0, this.series.getMinY(), 1e-9);
        assertEquals(20.0, this.series.getMaxY(), 1e-9);

        // Updating d2 with a higher value (50.0)
        TimeSeriesDataItem overwritten = this.series.addOrUpdate(d2, 50.0);

        assertNotNull(overwritten);
        assertEquals(new Double(20.0), overwritten.getValue());
        assertEquals(10.0, this.series.getMinY(), 1e-9);
        assertEquals(50.0, this.series.getMaxY(), 1e-9);
    }

    // Tests addOrUpdate with new period inserts correctly
    @Test
    public void testAddOrUpdate_newItem_addsSuccessfully() {
        Day d1 = new Day(1, 1, 2020);
        TimeSeriesDataItem overwritten = this.series.addOrUpdate(d1, 100.0);
        assertNull(overwritten);
        assertEquals(1, this.series.getItemCount());
        assertEquals(new Double(100.0), this.series.getValue(0));
    }

    // Tests update method with valid index and period
    @Test
    public void testUpdate_validPeriod_updatesValueAndBounds() {
        Day d1 = new Day(1, 1, 2020);
        Day d2 = new Day(2, 1, 2020);
        this.series.add(d1, 10.0);
        this.series.add(d2, 20.0);

        this.series.update(d1, 15.0);
        assertEquals(new Double(15.0), this.series.getValue(0));
        assertEquals(15.0, this.series.getMinY(), 1e-9);
        assertEquals(20.0, this.series.getMaxY(), 1e-9);
    }

    // Tests update non-existing period throws SeriesException
    @Test(expected = SeriesException.class)
    public void testUpdate_nonExistingPeriod_throwsSeriesException() {
        Day d1 = new Day(1, 1, 2020);
        this.series.update(d1, 10.0);
    }

    // Tests setMaximumItemCount drops oldest items and recalculates bounds
    @Test
    public void testSetMaximumItemCount_exceedLimit_removesOldestItems() {
        Day d1 = new Day(1, 1, 2020);
        Day d2 = new Day(2, 1, 2020);
        Day d3 = new Day(3, 1, 2020);
        this.series.add(d1, 5.0);
        this.series.add(d2, 10.0);
        this.series.add(d3, 15.0);

        this.series.setMaximumItemCount(2);

        assertEquals(2, this.series.getItemCount());
        assertEquals(d2, this.series.getTimePeriod(0));
        assertEquals(d3, this.series.getTimePeriod(1));
        assertEquals(10.0, this.series.getMinY(), 1e-9);
        assertEquals(15.0, this.series.getMaxY(), 1e-9);
    }

    // Tests setMaximumItemCount with negative value throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testSetMaximumItemCount_negativeValue_throwsException() {
        this.series.setMaximumItemCount(-1);
    }

    // Tests removeAgedItems based on maximumItemAge
    @Test
    public void testSetMaximumItemAge_removesOldItems() {
        Day d1 = new Day(1, 1, 2020);
        Day d2 = new Day(2, 1, 2020);
        Day d3 = new Day(10, 1, 2020);

        this.series.add(d1, 10.0);
        this.series.add(d2, 20.0);
        this.series.add(d3, 30.0);

        this.series.setMaximumItemAge(5);

        assertEquals(1, this.series.getItemCount());
        assertEquals(d3, this.series.getTimePeriod(0));
    }

    // Tests delete by period and index range
    @Test
    public void testDelete_byPeriodAndRange_updatesListAndBounds() {
        Day d1 = new Day(1, 1, 2020);
        Day d2 = new Day(2, 1, 2020);
        Day d3 = new Day(3, 1, 2020);
        this.series.add(d1, 10.0);
        this.series.add(d2, 20.0);
        this.series.add(d3, 30.0);

        this.series.delete(d1);
        assertEquals(2, this.series.getItemCount());
        assertEquals(20.0, this.series.getMinY(), 1e-9);

        this.series.delete(0, 1);
        assertEquals(0, this.series.getItemCount());
        assertNull(this.series.getTimePeriodClass());
        assertTrue(Double.isNaN(this.series.getMinY()));
        assertTrue(Double.isNaN(this.series.getMaxY()));
    }

    // Tests clear removes all data and resets state
    @Test
    public void testClear_populatedSeries_resetsAllFields() {
        this.series.add(new Day(1, 1, 2020), 10.0);
        this.series.clear();

        assertEquals(0, this.series.getItemCount());
        assertNull(this.series.getTimePeriodClass());
        assertTrue(Double.isNaN(this.series.getMinY()));
        assertTrue(Double.isNaN(this.series.getMaxY()));
    }

    // Tests createCopy for subset index range and minY/maxY bounds
    @Test
    public void testCreateCopy_validRange_createsIndependentCopy() throws CloneNotSupportedException {
        Day d1 = new Day(1, 1, 2020);
        Day d2 = new Day(2, 1, 2020);
        Day d3 = new Day(3, 1, 2020);

        this.series.add(d1, 10.0);
        this.series.add(d2, 20.0);
        this.series.add(d3, 30.0);

        TimeSeries copy = this.series.createCopy(1, 2);

        assertEquals(2, copy.getItemCount());
        assertEquals(d2, copy.getTimePeriod(0));
        assertEquals(d3, copy.getTimePeriod(1));
        assertEquals(20.0, copy.getMinY(), 1e-9);
        assertEquals(30.0, copy.getMaxY(), 1e-9);
    }

    // Tests createCopy with period range
    @Test
    public void testCreateCopy_periodRange_copiesCorrectSubset() throws CloneNotSupportedException {
        Day d1 = new Day(1, 1, 2020);
        Day d2 = new Day(2, 1, 2020);
        Day d3 = new Day(3, 1, 2020);

        this.series.add(d1, 10.0);
        this.series.add(d2, 20.0);
        this.series.add(d3, 30.0);

        TimeSeries copy = this.series.createCopy(d2, d3);
        assertEquals(2, copy.getItemCount());
        assertEquals(new Double(20.0), copy.getValue(0));

        // Period range outside data returns empty series
        TimeSeries emptyCopy = this.series.createCopy(new Day(10, 1, 2020), new Day(15, 1, 2020));
        assertEquals(0, emptyCopy.getItemCount());
    }

    // Tests clone creates independent deep copy of data
    @Test
    public void testClone_populatedSeries_producesIndependentObject() throws CloneNotSupportedException {
        Day d1 = new Day(1, 1, 2020);
        this.series.add(d1, 10.0);

        TimeSeries clone = (TimeSeries) this.series.clone();
        assertEquals(this.series, clone);
        assertEquals(this.series.hashCode(), clone.hashCode());

        clone.add(new Day(2, 1, 2020), 20.0);
        assertFalse(this.series.equals(clone));
        assertEquals(1, this.series.getItemCount());
        assertEquals(2, clone.getItemCount());
    }

    // Tests equals and hashCode consistency
    @Test
    public void testEqualsAndHashCode_variousStates_correctEquality() {
        TimeSeries s1 = new TimeSeries("S1", "D1", "R1");
        TimeSeries s2 = new TimeSeries("S1", "D1", "R1");
        assertTrue(s1.equals(s2));
        assertEquals(s1.hashCode(), s2.hashCode());

        s1.add(new Day(1, 1, 2020), 10.0);
        assertFalse(s1.equals(s2));

        s2.add(new Day(1, 1, 2020), 10.0);
        assertTrue(s1.equals(s2));
        assertEquals(s1.hashCode(), s2.hashCode());
    }

    // Tests helper queries like getNextTimePeriod, getTimePeriods, getTimePeriodsUniqueToOtherSeries
    @Test
    public void testCollectionAndNavigationQueries_returnsExpectedResults() {
        Day d1 = new Day(1, 1, 2020);
        Day d2 = new Day(2, 1, 2020);
        Day d3 = new Day(3, 1, 2020);

        this.series.add(d1, 1.0);
        this.series.add(d2, 2.0);

        assertEquals(new Day(3, 1, 2020), this.series.getNextTimePeriod());

        Collection periods = this.series.getTimePeriods();
        assertEquals(2, periods.size());

        TimeSeries other = new TimeSeries("Other");
        other.add(d2, 20.0);
        other.add(d3, 30.0);

        Collection unique = this.series.getTimePeriodsUniqueToOtherSeries(other);
        assertEquals(1, unique.size());
        assertTrue(unique.contains(d3));
    }

    // Tests addAndOrUpdate merges another series into this series
    @Test
    public void testAddAndOrUpdate_mergeSeries_overwritesAndMerges() {
        Day d1 = new Day(1, 1, 2020);
        Day d2 = new Day(2, 1, 2020);
        Day d3 = new Day(3, 1, 2020);

        this.series.add(d1, 10.0);
        this.series.add(d2, 20.0);

        TimeSeries other = new TimeSeries("Other");
        other.add(d2, 25.0);
        other.add(d3, 30.0);

        TimeSeries overwritten = this.series.addAndOrUpdate(other);

        assertEquals(3, this.series.getItemCount());
        assertEquals(new Double(25.0), this.series.getValue(d2));
        assertEquals(1, overwritten.getItemCount());
        assertEquals(new Double(20.0), overwritten.getValue(0));
    }
}