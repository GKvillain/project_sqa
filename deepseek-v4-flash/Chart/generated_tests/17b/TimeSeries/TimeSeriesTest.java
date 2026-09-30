package org.jfree.data.time;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.jfree.data.general.SeriesException;

public class TimeSeriesTest {

    private TimeSeries series;

    @Before
    public void setUp() {
        series = new TimeSeries("Test", Day.class);
        series.add(new Day(1, 1, 2024), 100.0);
        series.add(new Day(2, 1, 2024), 200.0);
        series.add(new Day(3, 1, 2024), 300.0);
    }

    // ============ Existing tests (unchanged) ============

    @Test
    public void testConstructor_defaultTimePeriodClassIsDay() {
        TimeSeries s = new TimeSeries("Default");
        assertEquals(Day.class, s.getTimePeriodClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAdd_nullItem_throwsException() {
        series.add((TimeSeriesDataItem) null);
    }

    @Test(expected = SeriesException.class)
    public void testAdd_wrongPeriodClass_throwsSeriesException() {
        series.add(new Month(1, 2024), 400.0);
    }

    @Test(expected = SeriesException.class)
    public void testAdd_duplicatePeriod_throwsSeriesException() {
        series.add(new Day(1, 1, 2024), 400.0);
    }

    @Test
    public void testAdd_normalItem_addsCorrectly() {
        // Append new item after last
        series.add(new Day(4, 1, 2024), 400.0);
        assertEquals(4, series.getItemCount());
        assertEquals(400.0, series.getValue(3).doubleValue(), 0.0001);

        // Insert new item before first
        series.add(new Day(31, 12, 2023), 50.0);
        assertEquals(5, series.getItemCount());
        assertEquals(50.0, series.getValue(0).doubleValue(), 0.0001);
    }

    @Test
    public void testAdd_exceedMaximumItemCount_removesFirst() {
        series.setMaximumItemCount(2);
        assertEquals(2, series.getItemCount());
        assertEquals(200.0, series.getValue(0).doubleValue(), 0.0001);
        assertEquals(300.0, series.getValue(1).doubleValue(), 0.0001);
    }

    @Test
    public void testAddOrUpdate_existingPeriod_updatesValue() {
        TimeSeriesDataItem old = series.addOrUpdate(new Day(1, 1, 2024), 150.0);
        assertNotNull(old);
        assertEquals(100.0, old.getValue().doubleValue(), 0.0001);
        assertEquals(150.0, series.getValue(0).doubleValue(), 0.0001);
        assertEquals(3, series.getItemCount());
    }

    @Test
    public void testAddOrUpdate_newPeriod_addsNewItem() {
        TimeSeriesDataItem old = series.addOrUpdate(new Day(4, 1, 2024), 400.0);
        assertNull(old);
        assertEquals(4, series.getItemCount());
        assertEquals(400.0, series.getValue(3).doubleValue(), 0.0001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetMaximumItemCount_negative_throwsException() {
        series.setMaximumItemCount(-1);
    }

    @Test
    public void testSetMaximumItemCount_zero_removesAll() {
        series.setMaximumItemCount(0);
        assertEquals(0, series.getItemCount());
    }

    @Test
    public void testClone_returnsIndependentCopy() throws CloneNotSupportedException {
        TimeSeries copy = (TimeSeries) series.clone();
        assertEquals(series, copy);
        copy.add(new Day(4, 1, 2024), 400.0);
        assertFalse(series.equals(copy));
        assertTrue(series.getItemCount() < copy.getItemCount());
    }

    @Test
    public void testCreateCopy_intRange_normal_returnsCopy() throws CloneNotSupportedException {
        TimeSeries copy = series.createCopy(0, 1);
        assertEquals(2, copy.getItemCount());
        assertEquals(100.0, copy.getValue(0).doubleValue(), 0.0001);
        assertEquals(200.0, copy.getValue(1).doubleValue(), 0.0001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCopy_intStartNegative_throwsException() throws CloneNotSupportedException {
        series.createCopy(-1, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCopy_intStartGreaterThanEnd_throwsException() throws CloneNotSupportedException {
        series.createCopy(2, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCopy_period_nullStart_throwsException() throws CloneNotSupportedException {
        series.createCopy(null, new Day(2, 1, 2024));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCopy_period_startAfterEnd_throwsException() throws CloneNotSupportedException {
        series.createCopy(new Day(3, 1, 2024), new Day(1, 1, 2024));
    }

    @Test
    public void testCreateCopy_period_bothInSeries_returnsCopy() throws CloneNotSupportedException {
        TimeSeries copy = series.createCopy(new Day(1, 1, 2024), new Day(2, 1, 2024));
        assertEquals(2, copy.getItemCount());
        assertEquals(100.0, copy.getValue(0).doubleValue(), 0.0001);
        assertEquals(200.0, copy.getValue(1).doubleValue(), 0.0001);
    }

    @Test
    public void testCreateCopy_period_startNotInSeries_beforeFirst_returnsCopyFromStart() throws CloneNotSupportedException {
        // start period before first data item
        TimeSeries copy = series.createCopy(new Day(31, 12, 2023), new Day(2, 1, 2024));
        assertEquals(2, copy.getItemCount());
        assertEquals(100.0, copy.getValue(0).doubleValue(), 0.0001);
        assertEquals(200.0, copy.getValue(1).doubleValue(), 0.0001);
    }

    @Test
    public void testCreateCopy_period_endNotInSeries_afterLast_returnsCopyUpToLast() throws CloneNotSupportedException {
        TimeSeries copy = series.createCopy(new Day(2, 1, 2024), new Day(5, 1, 2024));
        assertEquals(2, copy.getItemCount());
        assertEquals(200.0, copy.getValue(0).doubleValue(), 0.0001);
        assertEquals(300.0, copy.getValue(1).doubleValue(), 0.0001);
    }

    @Test
    public void testRemoveAgedItems_ageZero_removesAllButLast() {
        series.setMaximumItemAge(0);
        assertEquals(1, series.getItemCount());
        assertEquals(300.0, series.getValue(0).doubleValue(), 0.0001);
    }

    @Test
    public void testDelete_existingPeriod_removesItem() {
        series.delete(new Day(2, 1, 2024));
        assertEquals(2, series.getItemCount());
        assertEquals(100.0, series.getValue(0).doubleValue(), 0.0001);
        assertEquals(300.0, series.getValue(1).doubleValue(), 0.0001);
    }

    @Test
    public void testDelete_byIndexRange_removesItems() {
        series.delete(1, 2);
        assertEquals(1, series.getItemCount());
        assertEquals(100.0, series.getValue(0).doubleValue(), 0.0001);
    }

    // ============ New tests to improve coverage ============

    @Test
    public void testGetValueInt_ValidIndex() {
        assertEquals(100.0, series.getValue(0).doubleValue(), 0.0001);
        assertEquals(200.0, series.getValue(1).doubleValue(), 0.0001);
        assertEquals(300.0, series.getValue(2).doubleValue(), 0.0001);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValueInt_NegativeIndex() {
        series.getValue(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValueInt_IndexTooLarge() {
        series.getValue(3);
    }

    @Test
    public void testGetValuePeriod_ExistingPeriod() {
        Number val = series.getValue(new Day(2, 1, 2024));
        assertNotNull(val);
        assertEquals(200.0, val.doubleValue(), 0.0001);
    }

    @Test
    public void testGetValuePeriod_NonExistingPeriod() {
        assertNull(series.getValue(new Day(4, 1, 2024)));
    }

    @Test
    public void testGetIndex_ExistingPeriod() {
        assertEquals(0, series.getIndex(new Day(1, 1, 2024)));
        assertEquals(1, series.getIndex(new Day(2, 1, 2024)));
        assertEquals(2, series.getIndex(new Day(3, 1, 2024)));
    }

    @Test
    public void testGetIndex_NonExistingPeriod() {
        assertEquals(-1, series.getIndex(new Day(4, 1, 2024)));
    }

    @Test
    public void testIsEmpty_EmptySeries() {
        TimeSeries empty = new TimeSeries("Empty", Day.class);
        assertTrue(empty.isEmpty());
    }

    @Test
    public void testIsEmpty_NonEmptySeries() {
        assertFalse(series.isEmpty());
    }

    @Test
    public void testGetDataItemInt_ValidIndex() {
        TimeSeriesDataItem item = series.getDataItem(0);
        assertEquals(new Day(1, 1, 2024), item.getPeriod());
        assertEquals(100.0, item.getValue().doubleValue(), 0.0001);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetDataItemInt_NegativeIndex() {
        series.getDataItem(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetDataItemInt_IndexTooLarge() {
        series.getDataItem(3);
    }

    @Test
    public void testGetDataItemPeriod_ExistingPeriod() {
        TimeSeriesDataItem item = series.getDataItem(new Day(2, 1, 2024));
        assertNotNull(item);
        assertEquals(200.0, item.getValue().doubleValue(), 0.0001);
    }

    @Test
    public void testGetDataItemPeriod_NonExistingPeriod() {
        assertNull(series.getDataItem(new Day(4, 1, 2024)));
    }

    @Test
    public void testUpdateIntNumber_Valid() {
        series.update(1, 250.0);
        assertEquals(250.0, series.getValue(1).doubleValue(), 0.0001);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testUpdateIntNumber_InvalidIndex() {
        series.update(3, 400.0);
    }

    @Test
    public void testUpdatePeriodNumber_ExistingPeriod() {
        series.update(new Day(1, 1, 2024), 150.0);
        assertEquals(150.0, series.getValue(0).doubleValue(), 0.0001);
    }

    @Test(expected = SeriesException.class)
    public void testUpdatePeriodNumber_NonExistingPeriod() {
        series.update(new Day(4, 1, 2024), 400.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetMaximumItemAge_Negative() {
        series.setMaximumItemAge(-1);
    }

    @Test
    public void testSetMaximumItemAge_Zero() {
        series.setMaximumItemAge(0);
        assertEquals(0L, series.getMaximumItemAge());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeleteIntRange_StartGreaterThanEnd() {
        series.delete(2, 1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testDeleteIntRange_NegativeStart() {
        series.delete(-1, 1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testDeleteIntRange_EndOutOfBounds() {
        series.delete(1, 3);
    }

    @Test
    public void testDeleteIntRange_StartEqualsEnd() {
        series.delete(1, 1);
        assertEquals(2, series.getItemCount());
        assertEquals(100.0, series.getValue(0).doubleValue(), 0.0001);
        assertEquals(300.0, series.getValue(1).doubleValue(), 0.0001);
    }

    @Test
    public void testAddWithNotifyFalse() {
        TimeSeries s = new TimeSeries("Test", Day.class);
        s.add(new Day(1, 1, 2024), 100.0, false);
        assertEquals(1, s.getItemCount());
    }

    @Test
    public void testEquals_SameContent() {
        TimeSeries s1 = new TimeSeries("Test", Day.class);
        s1.add(new Day(1, 1, 2024), 100.0);
        TimeSeries s2 = new TimeSeries("Test", Day.class);
        s2.add(new Day(1, 1, 2024), 100.0);
        assertTrue(s1.equals(s2));
    }

    @Test
    public void testEquals_DifferentKey() {
        TimeSeries s1 = new TimeSeries("Test1", Day.class);
        s1.add(new Day(1, 1, 2024), 100.0);
        TimeSeries s2 = new TimeSeries("Test2", Day.class);
        s2.add(new Day(1, 1, 2024), 100.0);
        assertFalse(s1.equals(s2));
    }

    @Test
    public void testEquals_DifferentData() {
        TimeSeries s1 = new TimeSeries("Test", Day.class);
        s1.add(new Day(1, 1, 2024), 100.0);
        TimeSeries s2 = new TimeSeries("Test", Day.class);
        s2.add(new Day(1, 1, 2024), 200.0);
        assertFalse(s1.equals(s2));
    }

    @Test
    public void testEquals_Null() {
        assertFalse(series.equals(null));
    }

    @Test
    public void testHashCode_Consistency() {
        TimeSeries s1 = new TimeSeries("Test", Day.class);
        s1.add(new Day(1, 1, 2024), 100.0);
        TimeSeries s2 = new TimeSeries("Test", Day.class);
        s2.add(new Day(1, 1, 2024), 100.0);
        assertEquals(s1.hashCode(), s2.hashCode());
    }

    @Test
    public void testGetTimePeriodClass() {
        assertEquals(Day.class, series.getTimePeriodClass());
    }

    @Test
    public void testSetKey() {
        series.setKey("NewKey");
        assertEquals("NewKey", series.getKey());
    }
}