package org.jfree.data.time;

import java.util.Collection;
import org.jfree.data.general.SeriesException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TimeSeriesTest {

    private TimeSeries series;

    @Before
    public void setUp() {
        this.series = new TimeSeries("Test Series", Day.class);
    }

    // Tests createCopy with period range where no observations exist between start and end (Bug Chart-9)
    @Test
    public void testCreateCopy_rangeWithNoData_returnsEmptySeries() throws CloneNotSupportedException {
        this.series.add(new Day(1, 1, 2008), 10.0);
        this.series.add(new Day(10, 1, 2008), 20.0);

        TimeSeries copy = this.series.createCopy(new Day(2, 1, 2008), new Day(5, 1, 2008));
        assertEquals(0, copy.getItemCount());
    }

    // Tests createCopy with period range after all existing data
    @Test
    public void testCreateCopy_rangeAfterAllData_returnsEmptySeries() throws CloneNotSupportedException {
        this.series.add(new Day(1, 1, 2008), 10.0);
        this.series.add(new Day(2, 1, 2008), 20.0);

        TimeSeries copy = this.series.createCopy(new Day(5, 1, 2008), new Day(10, 1, 2008));
        assertEquals(0, copy.getItemCount());
    }

    // Tests createCopy with valid subset of periods
    @Test
    public void testCreateCopy_validPeriodRange_returnsSubset() throws CloneNotSupportedException {
        this.series.add(new Day(1, 1, 2008), 10.0);
        this.series.add(new Day(2, 1, 2008), 20.0);
        this.series.add(new Day(3, 1, 2008), 30.0);

        TimeSeries copy = this.series.createCopy(new Day(1, 1, 2008), new Day(2, 1, 2008));
        assertEquals(2, copy.getItemCount());
        assertEquals(10.0, copy.getValue(0).doubleValue(), 0.00001);
        assertEquals(20.0, copy.getValue(1).doubleValue(), 0.00001);
    }

    // Tests createCopy with null start argument
    @Test(expected = IllegalArgumentException.class)
    public void testCreateCopy_nullStart_throwsException() throws CloneNotSupportedException {
        this.series.createCopy(null, new Day(1, 1, 2008));
    }

    // Tests createCopy with null end argument
    @Test(expected = IllegalArgumentException.class)
    public void testCreateCopy_nullEnd_throwsException() throws CloneNotSupportedException {
        this.series.createCopy(new Day(1, 1, 2008), null);
    }

    // Tests createCopy with start after end argument
    @Test(expected = IllegalArgumentException.class)
    public void testCreateCopy_startAfterEnd_throwsException() throws CloneNotSupportedException {
        this.series.createCopy(new Day(5, 1, 2008), new Day(1, 1, 2008));
    }

    // Tests createCopy using integer indices
    @Test
    public void testCreateCopy_validIndices_returnsSubset() throws CloneNotSupportedException {
        this.series.add(new Day(1, 1, 2008), 10.0);
        this.series.add(new Day(2, 1, 2008), 20.0);
        this.series.add(new Day(3, 1, 2008), 30.0);

        TimeSeries copy = this.series.createCopy(1, 2);
        assertEquals(2, copy.getItemCount());
        assertEquals(20.0, copy.getValue(0).doubleValue(), 0.00001);
        assertEquals(30.0, copy.getValue(1).doubleValue(), 0.00001);
    }

    // Tests add and duplicate detection
    @Test(expected = SeriesException.class)
    public void testAdd_duplicatePeriod_throwsException() {
        this.series.add(new Day(1, 1, 2008), 10.0);
        this.series.add(new Day(1, 1, 2008), 20.0);
    }

    // Tests add with incompatible period class
    @Test(expected = SeriesException.class)
    public void testAdd_incompatibleTimePeriodClass_throwsException() {
        this.series.add(new Year(2008), 10.0);
    }

    // Tests addOrUpdate inserting and updating items
    @Test
    public void testAddOrUpdate_insertAndOverwrite_updatesCorrectly() {
        TimeSeriesDataItem item1 = this.series.addOrUpdate(new Day(1, 1, 2008), 10.0);
        assertNull(item1);
        assertEquals(1, this.series.getItemCount());
        assertEquals(10.0, this.series.getValue(0).doubleValue(), 0.00001);

        TimeSeriesDataItem item2 = this.series.addOrUpdate(new Day(1, 1, 2008), 25.0);
        assertNotNull(item2);
        assertEquals(10.0, item2.getValue().doubleValue(), 0.00001);
        assertEquals(1, this.series.getItemCount());
        assertEquals(25.0, this.series.getValue(0).doubleValue(), 0.00001);
    }

    // Tests update method when period exists and when it does not exist
    @Test
    public void testUpdate_existingPeriod_updatesValue() {
        Day day = new Day(1, 1, 2008);
        this.series.add(day, 10.0);
        this.series.update(day, new Double(50.0));
        assertEquals(50.0, this.series.getValue(day).doubleValue(), 0.00001);
    }

    // Tests update method when period does not exist
    @Test(expected = SeriesException.class)
    public void testUpdate_nonExistingPeriod_throwsException() {
        this.series.update(new Day(1, 1, 2008), new Double(50.0));
    }

    // Tests maximum item count enforcement
    @Test
    public void testSetMaximumItemCount_exceeded_dropsOldestItems() {
        this.series.add(new Day(1, 1, 2008), 1.0);
        this.series.add(new Day(2, 1, 2008), 2.0);
        this.series.add(new Day(3, 1, 2008), 3.0);

        this.series.setMaximumItemCount(2);
        assertEquals(2, this.series.getItemCount());
        assertEquals(new Day(2, 1, 2008), this.series.getTimePeriod(0));
        assertEquals(new Day(3, 1, 2008), this.series.getTimePeriod(1));
    }

    // Tests maximum item age enforcement
    @Test
    public void testSetMaximumItemAge_removesOldItems() {
        this.series.add(new Day(1, 1, 2008), 1.0);
        this.series.add(new Day(5, 1, 2008), 2.0);
        this.series.add(new Day(10, 1, 2008), 3.0);

        this.series.setMaximumItemAge(5);
        assertEquals(2, this.series.getItemCount());
        assertEquals(new Day(5, 1, 2008), this.series.getTimePeriod(0));
        assertEquals(new Day(10, 1, 2008), this.series.getTimePeriod(1));
    }

    // Tests delete by period and delete by index range
    @Test
    public void testDelete_byPeriodAndRange_removesItems() {
        Day d1 = new Day(1, 1, 2008);
        Day d2 = new Day(2, 1, 2008);
        Day d3 = new Day(3, 1, 2008);
        this.series.add(d1, 10.0);
        this.series.add(d2, 20.0);
        this.series.add(d3, 30.0);

        this.series.delete(d2);
        assertEquals(2, this.series.getItemCount());
        assertNull(this.series.getValue(d2));

        this.series.delete(0, 1);
        assertEquals(0, this.series.getItemCount());
    }

    // Tests getTimePeriods and getTimePeriodsUniqueToOtherSeries
    @Test
    public void testGetTimePeriodsUniqueToOtherSeries_returnsDifference() {
        TimeSeries s2 = new TimeSeries("S2", Day.class);
        this.series.add(new Day(1, 1, 2008), 1.0);
        this.series.add(new Day(2, 1, 2008), 2.0);

        s2.add(new Day(2, 1, 2008), 2.0);
        s2.add(new Day(3, 1, 2008), 3.0);

        Collection unique = this.series.getTimePeriodsUniqueToOtherSeries(s2);
        assertEquals(1, unique.size());
        assertTrue(unique.contains(new Day(3, 1, 2008)));
    }

    // Tests addAndOrUpdate merging two series
    @Test
    public void testAddAndOrUpdate_mergesSeriesAndReturnsOverwritten() {
        this.series.add(new Day(1, 1, 2008), 10.0);
        this.series.add(new Day(2, 1, 2008), 20.0);

        TimeSeries other = new TimeSeries("Other", Day.class);
        other.add(new Day(2, 1, 2008), 25.0);
        other.add(new Day(3, 1, 2008), 30.0);

        TimeSeries overwritten = this.series.addAndOrUpdate(other);
        assertEquals(1, overwritten.getItemCount());
        assertEquals(20.0, overwritten.getValue(0).doubleValue(), 0.00001);
        assertEquals(3, this.series.getItemCount());
        assertEquals(25.0, this.series.getValue(new Day(2, 1, 2008)).doubleValue(), 0.00001);
    }

    // Tests clone method independence
    @Test
    public void testClone_createsIndependentCopy() throws CloneNotSupportedException {
        this.series.add(new Day(1, 1, 2008), 10.0);
        TimeSeries clone = (TimeSeries) this.series.clone();

        assertEquals(this.series, clone);
        clone.add(new Day(2, 1, 2008), 20.0);
        assertFalse(this.series.equals(clone));
    }

    // Tests equals and hashCode consistency
    @Test
    public void testEqualsAndHashCode_sameAttributes_areEqualAndHaveSameHashCode() {
        TimeSeries s1 = new TimeSeries("S", "Domain", "Range", Day.class);
        TimeSeries s2 = new TimeSeries("S", "Domain", "Range", Day.class);

        assertTrue(s1.equals(s2));
        assertEquals(s1.hashCode(), s2.hashCode());

        s1.add(new Day(1, 1, 2008), 10.0);
        assertFalse(s1.equals(s2));

        s2.add(new Day(1, 1, 2008), 10.0);
        assertTrue(s1.equals(s2));
        assertEquals(s1.hashCode(), s2.hashCode());
    }

    // Tests domain and range descriptions and clear method
    @Test
    public void testSetDescriptionsAndClear_clearsDataAndUpdatesProperties() {
        this.series.setDomainDescription("Time Domain");
        this.series.setRangeDescription("Value Range");
        assertEquals("Time Domain", this.series.getDomainDescription());
        assertEquals("Value Range", this.series.getRangeDescription());

        this.series.add(new Day(1, 1, 2008), 10.0);
        assertEquals(1, this.series.getItemCount());
        this.series.clear();
        assertEquals(0, this.series.getItemCount());
    }
}