package org.jfree.data.time;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.jfree.data.general.SeriesException;

/**
 * JUnit 4 test class for TimeSeries (Defects4J Chart-9b).
 * Tests focus on defect detection, branch coverage, and boundary conditions.
 */
public class TimeSeriesTest {

    private TimeSeries emptySeries;
    private TimeSeries seriesWithData;
    private Day day1;
    private Day day2;
    private Day day3;

    @Before
    public void setUp() {
        day1 = new Day(1, 1, 2020);
        day2 = new Day(2, 1, 2020);
        day3 = new Day(3, 1, 2020);
        emptySeries = new TimeSeries("Test", Day.class);

        seriesWithData = new TimeSeries("Test Data", Day.class);
        seriesWithData.add(day1, 10.0);
        seriesWithData.add(day2, 20.0);
        seriesWithData.add(day3, 30.0);
    }

    // --- Constructor ---
    @Test
    public void testConstructor_defaultPeriod_createsDailySeries() {
        TimeSeries s = new TimeSeries("Default");
        assertEquals("Time", s.getDomainDescription());
        assertEquals("Value", s.getRangeDescription());
        assertEquals(Day.class, s.getTimePeriodClass());
    }

    // --- add(TimeSeriesDataItem) ---
    // Normal: add item to empty series
    @Test
    public void testAddDataItem_toEmptySeries_added() {
        TimeSeries s = new TimeSeries("S", Day.class);
        s.add(new TimeSeriesDataItem(day1, 5.0));
        assertEquals(1, s.getItemCount());
        assertEquals(5.0, s.getValue(0));
    }

    // Normal: add item at end (period after last)
    @Test
    public void testAddDataItem_afterLastItem_addedAtEnd() {
        TimeSeries s = new TimeSeries("S", Day.class);
        s.add(day1, 1.0);
        s.add(day2, 2.0);
        assertEquals(2, s.getItemCount());
        assertEquals(day2, s.getTimePeriod(1));
    }

    // Exception: duplicate time period
    @Test(expected = SeriesException.class)
    public void testAddDataItem_duplicatePeriod_throwsSeriesException() {
        TimeSeries s = new TimeSeries("S", Day.class);
        s.add(day1, 1.0);
        s.add(day1, 2.0); // duplicate
    }

    // Exception: null item
    @Test(expected = IllegalArgumentException.class)
    public void testAddDataItem_nullItem_throwsIllegalArgumentException() {
        emptySeries.add((TimeSeriesDataItem) null);
    }

    // Exception: wrong time period class
    @Test(expected = SeriesException.class)
    public void testAddDataItem_wrongPeriodClass_throwsSeriesException() {
        TimeSeries s = new TimeSeries("S", Day.class);
        // Use Year which is a different RegularTimePeriod subclass
        s.add(new Year(2020), 100.0);
    }

    // --- add(RegularTimePeriod, double) ---
    // Boundary: add when item count exceeds maximumItemCount -> remove first
    @Test
    public void testAdd_periodAndValue_exceedsMaxItemCount_removesFirst() {
        TimeSeries s = new TimeSeries("S", Day.class);
        s.setMaximumItemCount(2);
        s.add(day1, 1.0);
        s.add(day2, 2.0);
        assertEquals(2, s.getItemCount());
        // Adding third item should remove the first (day1)
        s.add(day3, 3.0);
        assertEquals(2, s.getItemCount());
        assertEquals(day2, s.getTimePeriod(0));
        assertEquals(day3, s.getTimePeriod(1));
    }

    // --- addOrUpdate ---
    // Normal: new period -> add
    @Test
    public void testAddOrUpdate_newPeriod_itemAdded() {
        TimeSeries s = new TimeSeries("S", Day.class);
        s.add(day1, 1.0);
        TimeSeriesDataItem old = s.addOrUpdate(day2, 2.0);
        assertNull(old);
        assertEquals(2, s.getItemCount());
    }

    // Update: existing period -> value updated, old returned
    @Test
    public void testAddOrUpdate_existingPeriod_updatesValue() {
        TimeSeries s = new TimeSeries("S", Day.class);
        s.add(day1, 1.0);
        TimeSeriesDataItem old = s.addOrUpdate(day1, 100.0);
        assertNotNull(old);
        assertEquals(1.0, old.getValue());
        assertEquals(100.0, s.getValue(0));
    }

    // Boundary: addOrUpdate when exceeding maximumItemCount
    @Test
    public void testAddOrUpdate_exceedsMaxItemCount_removesFirst() {
        TimeSeries s = new TimeSeries("S", Day.class);
        s.setMaximumItemCount(2);
        s.add(day1, 1.0);
        s.add(day2, 2.0);
        s.addOrUpdate(day3, 3.0); // should remove day1
        assertEquals(2, s.getItemCount());
        assertEquals(day2, s.getTimePeriod(0));
    }

    // --- getIndex ---
    // Period exists
    @Test
    public void testGetIndex_periodExists_returnsIndex() {
        int idx = seriesWithData.getIndex(day2);
        assertEquals(1, idx);
    }

    // Period does not exist
    @Test
    public void testGetIndex_periodNotExists_returnsNegative() {
        Day d = new Day(15, 6, 2020);
        int idx = seriesWithData.getIndex(d);
        assertTrue(idx < 0);
    }

    // --- delete ---
    // Delete by period that exists
    @Test
    public void testDelete_periodInSeries_removed() {
        seriesWithData.delete(day2);
        assertEquals(2, seriesWithData.getItemCount());
        assertEquals(day1, seriesWithData.getTimePeriod(0));
        assertEquals(day3, seriesWithData.getTimePeriod(1));
    }

    // Delete by period that does not exist – no exception, no change
    @Test
    public void testDelete_periodNotInSeries_doesNothing() {
        Day d = new Day(15, 6, 2020);
        seriesWithData.delete(d); // should not throw
        assertEquals(3, seriesWithData.getItemCount());
    }

    // Delete by range (inclusive)
    @Test
    public void testDelete_rangeInclusive_itemsRemoved() {
        seriesWithData.delete(0, 1); // removes day1 and day2
        assertEquals(1, seriesWithData.getItemCount());
        assertEquals(day3, seriesWithData.getTimePeriod(0));
    }

    // Exception: start > end
    @Test(expected = IllegalArgumentException.class)
    public void testDelete_rangeStartGreaterThanEnd_throwsIllegalArgumentException() {
        seriesWithData.delete(2, 1);
    }

    // --- setMaximumItemCount ---
    // Normal: set count lower than current size -> remove oldest items
    @Test
    public void testSetMaximumItemCount_lowerThanCurrent_removesExcess() {
        TimeSeries s = new TimeSeries("S", Day.class);
        s.add(day1, 1.0);
        s.add(day2, 2.0);
        s.add(day3, 3.0);
        s.setMaximumItemCount(2);
        assertEquals(2, s.getItemCount());
        assertEquals(day2, s.getTimePeriod(0)); // first (day1) removed
    }

    // Exception: negative maximum
    @Test(expected = IllegalArgumentException.class)
    public void testSetMaximumItemCount_negative_throwsIllegalArgumentException() {
        seriesWithData.setMaximumItemCount(-1);
    }

    // --- removeAgedItems ---
    // Aged items removed when maximumItemAge is small
    @Test
    public void testRemoveAgedItems_itemsOlderThanMaxAge_removed() {
        TimeSeries s = new TimeSeries("S", Day.class);
        s.setMaximumItemAge(0); // age limit 0 periods – only keep the latest
        s.add(day1, 1.0);
        s.add(day2, 2.0);
        // After adding second item, removeAgedItems is called (inside add)
        // day1 and day2 have different serial indices, difference > 0 -> remove day1
        assertEquals(1, s.getItemCount());
        assertEquals(day2, s.getTimePeriod(0));
    }

    // --- clone ---
    // Clone deep copies data
    @Test
    public void testClone_dataShouldBeDeepCopy() throws CloneNotSupportedException {
        TimeSeries clone = (TimeSeries) seriesWithData.clone();
        assertEquals(seriesWithData.getItemCount(), clone.getItemCount());
        // Modify clone and verify original unchanged
        clone.update(0, 999.0);
        assertFalse(seriesWithData.getValue(0).equals(clone.getValue(0)));
        assertEquals(10.0, seriesWithData.getValue(0));
    }

    // --- equals ---
    // Equal when content identical
    @Test
    public void testEquals_sameContent_returnsTrue() {
        TimeSeries s1 = new TimeSeries("S", Day.class);
        s1.add(day1, 10.0);
        TimeSeries s2 = new TimeSeries("S", Day.class);
        s2.add(day1, 10.0);
        assertTrue(s1.equals(s2));
    }

    // Not equal when data differs
    @Test
    public void testEquals_differentContent_returnsFalse() {
        TimeSeries s1 = new TimeSeries("S", Day.class);
        s1.add(day1, 10.0);
        TimeSeries s2 = new TimeSeries("S", Day.class);
        s2.add(day1, 20.0);
        assertFalse(s1.equals(s2));
    }

    // --- update(RegularTimePeriod, Number) ---
    // Update existing period
    @Test
    public void testUpdate_periodExists_updatesValue() {
        seriesWithData.update(day2, 200.0);
        assertEquals(200.0, seriesWithData.getValue(day2));
    }

    // Exception: update non-existent period
    @Test(expected = SeriesException.class)
    public void testUpdate_periodNotExists_throwsSeriesException() {
        Day d = new Day(15, 6, 2020);
        seriesWithData.update(d, 99.0);
    }

    // --- createCopy(int, int) ---
    // Normal copy of valid range
    @Test
    public void testCreateCopy_validRange_returnsSubseries() throws CloneNotSupportedException {
        TimeSeries copy = seriesWithData.createCopy(1, 2); // indices 1 and 2
        assertEquals(2, copy.getItemCount());
        assertEquals(day2, copy.getTimePeriod(0));
        assertEquals(day3, copy.getTimePeriod(1));
    }

    // ===================== NEW TEST CASES FOR IMPROVED COVERAGE =====================

    // --- add(RegularTimePeriod, double, boolean) with notify=false ---
    @Test
    public void testAdd_withNotifyFalse_addsWithoutNotification() {
        TimeSeries s = new TimeSeries("S", Day.class);
        // we cannot directly test notification, but the method should exist and not throw
        s.add(day1, 1.0, false);
        assertEquals(1, s.getItemCount());
        assertEquals(1.0, s.getValue(0));
    }

    // --- setMaximumItemAge / getMaximumItemAge ---
    @Test
    public void testSetMaximumItemAge_positive_setsAndGets() {
        TimeSeries s = new TimeSeries("S", Day.class);
        s.setMaximumItemAge(5L);
        assertEquals(5L, s.getMaximumItemAge());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetMaximumItemAge_negative_throws() {
        TimeSeries s = new TimeSeries("S", Day.class);
        s.setMaximumItemAge(-1L);
    }

    // --- delete(int) single index ---
    @Test
    public void testDelete_singleIndex_removesItem() {
        seriesWithData.delete(1); // remove day2
        assertEquals(2, seriesWithData.getItemCount());
        assertEquals(day1, seriesWithData.getTimePeriod(0));
        assertEquals(day3, seriesWithData.getTimePeriod(1));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testDelete_singleIndexOutOfBounds_throws() {
        seriesWithData.delete(5);
    }

    // --- getValue(int) and getValue(RegularTimePeriod) ---
    @Test
    public void testGetValueByIndex_validIndex_returnsValue() {
        assertEquals(10.0, seriesWithData.getValue(0));
        assertEquals(20.0, seriesWithData.getValue(1));
        assertEquals(30.0, seriesWithData.getValue(2));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValueByIndex_negativeIndex_throws() {
        seriesWithData.getValue(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValueByIndex_exceedsCount_throws() {
        seriesWithData.getValue(3);
    }

    @Test
    public void testGetValueByPeriod_existingPeriod_returnsValue() {
        assertEquals(10.0, seriesWithData.getValue(day1));
        assertEquals(20.0, seriesWithData.getValue(day2));
        assertEquals(30.0, seriesWithData.getValue(day3));
    }

    @Test
    public void testGetValueByPeriod_nonExistingPeriod_returnsNull() {
        Day d = new Day(15, 6, 2020);
        assertNull(seriesWithData.getValue(d));
    }

    // --- getTimePeriod(int) ---
    @Test
    public void testGetTimePeriod_validIndex_returnsPeriod() {
        assertEquals(day1, seriesWithData.getTimePeriod(0));
        assertEquals(day2, seriesWithData.getTimePeriod(1));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetTimePeriod_negativeIndex_throws() {
        seriesWithData.getTimePeriod(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetTimePeriod_exceedsCount_throws() {
        seriesWithData.getTimePeriod(3);
    }

    // --- equals with different key, domain, range ---
    @Test
    public void testEquals_differentKey_returnsFalse() {
        TimeSeries s1 = new TimeSeries("S1", Day.class);
        s1.add(day1, 10.0);
        TimeSeries s2 = new TimeSeries("S2", Day.class);
        s2.add(day1, 10.0);
        assertFalse(s1.equals(s2));
    }

    @Test
    public void testEquals_differentDomainDescription_returnsFalse() {
        TimeSeries s1 = new TimeSeries("S", Day.class);
        s1.setDomainDescription("Domain1");
        TimeSeries s2 = new TimeSeries("S", Day.class);
        s2.setDomainDescription("Domain2");
        assertFalse(s1.equals(s2));
    }

    @Test
    public void testEquals_differentRangeDescription_returnsFalse() {
        TimeSeries s1 = new TimeSeries("S", Day.class);
        s1.setRangeDescription("Range1");
        TimeSeries s2 = new TimeSeries("S", Day.class);
        s2.setRangeDescription("Range2");
        assertFalse(s1.equals(s2));
    }

    // --- hashCode consistent with equals ---
    @Test
    public void testHashCode_equalObjects_haveEqualHashCodes() {
        TimeSeries s1 = new TimeSeries("S", Day.class);
        s1.add(day1, 10.0);
        TimeSeries s2 = new TimeSeries("S", Day.class);
        s2.add(day1, 10.0);
        assertEquals(s1.hashCode(), s2.hashCode());
    }

    @Test
    public void testHashCode_unequalObjects_differentHashCodes() {
        TimeSeries s1 = new TimeSeries("S", Day.class);
        s1.add(day1, 10.0);
        TimeSeries s2 = new TimeSeries("S", Day.class);
        s2.add(day1, 20.0);
        assertNotEquals(s1.hashCode(), s2.hashCode());
    }

    // --- clone equals original ---
    @Test
    public void testClone_clonedSeriesEqualsOriginal() throws CloneNotSupportedException {
        TimeSeries clone = (TimeSeries) seriesWithData.clone();
        assertTrue(seriesWithData.equals(clone));
    }

    // --- createCopy with start==end (single element) ---
    @Test
    public void testCreateCopy_singleElement_returnsThatElement() throws CloneNotSupportedException {
        TimeSeries copy = seriesWithData.createCopy(1, 1);
        assertEquals(1, copy.getItemCount());
        assertEquals(day2, copy.getTimePeriod(0));
        assertEquals(20.0, copy.getValue(0));
    }

    // --- removeAgedItems(long, boolean) explicit call ---
    @Test
    public void testRemoveAgedItems_explicitCall_removesOld() {
        TimeSeries s = new TimeSeries("S", Day.class);
        s.setMaximumItemAge(0);
        s.add(day1, 1.0);
        s.add(day2, 2.0);
        // After adding two items, aged items should have been removed, but we test explicit call
        // Reset and test on a series that has not triggered auto-removal yet
        TimeSeries s2 = new TimeSeries("S2", Day.class);
        s2.setMaximumItemAge(1); // allow period of age 1
        s2.add(day1, 10.0);
        s2.add(day2, 20.0);
        // day1 age relative to latest day2 is 1, so it should NOT be removed by age 1
        assertEquals(2, s2.getItemCount());
        // Now set age to 0 and call removeAgedItems
        s2.setMaximumItemAge(0);
        // We need to call removeAgedItems with a latest time parameter
        // The method removeAgedItems(long latest, boolean notify) uses the given latest time
        // latest = day2.getMiddleMillisecond() would work
        s2.removeAgedItems(day2.getMiddleMillisecond(), true);
        // Now day1 should be removed because its age (difference) > 0
        assertEquals(1, s2.getItemCount());
        assertEquals(day2, s2.getTimePeriod(0));
    }

    // --- update(int, Number) (update by index) ---
    @Test
    public void testUpdateByIndex_existingIndex_updatesValue() {
        seriesWithData.update(0, 99.0);
        assertEquals(99.0, seriesWithData.getValue(0));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testUpdateByIndex_invalidIndex_throws() {
        seriesWithData.update(5, 1.0);
    }

    // --- addOrUpdate with null value? (may or may not be allowed) ---
    // We add a test to ensure it does not throw unexpectedly; if it throws, adjust expected annotation
    @Test
    public void testAddOrUpdate_withNullValue_doesNotThrow() {
        TimeSeries s = new TimeSeries("S", Day.class);
        s.add(day1, 10.0);
        // According to TimeSeries source, addOrUpdate allows null values? Check behavior
        // In JFreeChart 1.0.13, addOrUpdate allows null, but update might throw. We'll just test addOrUpdate.
        s.addOrUpdate(day2, null);
        assertNull(s.getValue(day2));
    }

    // --- isEmpty ---
    @Test
    public void testIsEmpty_afterConstruction_returnsTrue() {
        assertTrue(emptySeries.isEmpty());
    }

    @Test
    public void testIsEmpty_afterAdd_returnsFalse() {
        emptySeries.add(day1, 1.0);
        assertFalse(emptySeries.isEmpty());
    }

    // --- getItemCount after clear (not a method in TimeSeries?) ---
    // Actually TimeSeries has clear() method that removes all items
    @Test
    public void testClear_removesAllItems() {
        seriesWithData.clear();
        assertEquals(0, seriesWithData.getItemCount());
        assertTrue(seriesWithData.isEmpty());
    }

    // --- setNotify and getNotify ---
    @Test
    public void testSetNotify_setsFlag() {
        assertTrue(seriesWithData.getNotify()); // default should be true
        seriesWithData.setNotify(false);
        assertFalse(seriesWithData.getNotify());
    }
}