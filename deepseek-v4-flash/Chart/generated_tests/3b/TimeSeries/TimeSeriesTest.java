package org.jfree.data.time;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;
import org.jfree.data.general.SeriesException;

/**
 * JUnit 4 test class for TimeSeries.
 * Designed to detect Defects4J bug 3b (incorrect use of minIgnoreNaN in addOrUpdate).
 */
public class TimeSeriesTest {
    
    private TimeSeries series;
    
    @Before
    public void setUp() {
        series = new TimeSeries("Test Series");
    }
    
    @Test
    public void testConstructor_defaultValues_expectedEmpty() {
        assertEquals("Test Series", series.getKey());
        assertEquals("Time", series.getDomainDescription());
        assertEquals("Value", series.getRangeDescription());
        assertEquals(0, series.getItemCount());
        assertTrue(Double.isNaN(series.getMinY()));
        assertTrue(Double.isNaN(series.getMaxY()));
        assertNull(series.getTimePeriodClass());
        assertEquals(Integer.MAX_VALUE, series.getMaximumItemCount());
        assertEquals(Long.MAX_VALUE, series.getMaximumItemAge());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testAdd_nullItem_throwsException() {
        series.add((TimeSeriesDataItem) null);
    }
    
    @Test(expected = SeriesException.class)
    public void testAdd_duplicatePeriod_throwsSeriesException() {
        series.add(new Year(2020), 10.0);
        series.add(new Year(2020), 20.0);
    }
    
    @Test
    public void testAdd_validItem_increasesCountAndSetsBounds() {
        series.add(new Year(2020), 5.0);
        assertEquals(1, series.getItemCount());
        assertEquals(5.0, series.getMinY(), 0.0001);
        assertEquals(5.0, series.getMaxY(), 0.0001);
    }
    
    @Test
    public void testAdd_multipleItems_maintainsSortedOrder() {
        series.add(new Year(2021), 30.0);
        series.add(new Year(2020), 10.0);
        assertEquals(2, series.getItemCount());
        assertEquals(new Year(2020), series.getTimePeriod(0));
        assertEquals(new Year(2021), series.getTimePeriod(1));
    }
    
    @Test
    public void testAddOrUpdate_newItem_returnsNullAndAddsItem() {
        TimeSeriesDataItem result = series.addOrUpdate(new Year(2020), 15.0);
        assertNull(result);
        assertEquals(1, series.getItemCount());
        assertEquals(15.0, series.getValue(0).doubleValue(), 0.0001);
    }
    
    @Test
    public void testAddOrUpdate_existingItem_returnsOldAndUpdates() {
        series.add(new Year(2020), 10.0);
        TimeSeriesDataItem result = series.addOrUpdate(new Year(2020), 20.0);
        assertNotNull(result);
        assertEquals(10.0, result.getValue().doubleValue(), 0.0001);
        assertEquals(20.0, series.getValue(0).doubleValue(), 0.0001);
    }
    
    // Detects Defects4J bug 3b: addOrUpdate uses minIgnoreNaN for maxY instead of maxIgnoreNaN
    @Test
    public void testAddOrUpdate_existingItem_minMaxUpdate_bugDetection() {
        series.add(new Year(2020), 10.0);
        assertEquals(10.0, series.getMaxY(), 0.0001);
        series.addOrUpdate(new Year(2020), 30.0);
        // If bug exists, maxY would remain 10.0; correct value is 30.0
        assertEquals(30.0, series.getMaxY(), 0.0001);
    }
    
    @Test
    public void testUpdate_existingPeriod_updatesValue() {
        series.add(new Year(2020), 5.0);
        series.update(new Year(2020), 25.0);
        assertEquals(25.0, series.getValue(0).doubleValue(), 0.0001);
    }
    
    @Test(expected = SeriesException.class)
    public void testUpdate_nonExistingPeriod_throwsException() {
        series.update(new Year(2020), 10.0);
    }
    
    @Test
    public void testDelete_periodExists_removesItem() {
        series.add(new Year(2020), 10.0);
        series.add(new Year(2021), 20.0);
        series.delete(new Year(2020));
        assertEquals(1, series.getItemCount());
        assertEquals(new Year(2021), series.getTimePeriod(0));
    }
    
    @Test
    public void testDelete_rangeValid_removesItems() {
        series.add(new Year(2020), 1.0);
        series.add(new Year(2021), 2.0);
        series.add(new Year(2022), 3.0);
        series.delete(0, 1);
        assertEquals(1, series.getItemCount());
        assertEquals(new Year(2022), series.getTimePeriod(0));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testDelete_rangeInvalid_throwsException() {
        series.delete(2, 1);
    }
    
    @Test
    public void testSetMaximumItemCount_removesExcessItems() {
        series.add(new Year(2020), 10.0);
        series.add(new Year(2021), 20.0);
        series.add(new Year(2022), 30.0);
        series.setMaximumItemCount(2);
        assertEquals(2, series.getItemCount());
        assertEquals(new Year(2021), series.getTimePeriod(0));
    }
    
    @Test
    public void testSetMaximumItemAge_removesOldItems() {
        series.add(new Year(2020), 10.0);
        series.add(new Year(2021), 20.0);
        series.setMaximumItemAge(1);
        assertEquals(2, series.getItemCount());
        series.add(new Year(2022), 30.0);
        assertEquals(2, series.getItemCount());
        assertEquals(new Year(2021), series.getTimePeriod(0));
    }
    
    @Test
    public void testGetMinY_emptySeries_returnsNaN() {
        assertTrue(Double.isNaN(series.getMinY()));
    }
    
    @Test
    public void testGetMinY_getMaxY_mixedValues() {
        series.add(new Year(2020), 5.0);
        series.add(new Year(2021), -3.0);
        series.add(new Year(2022), 10.0);
        assertEquals(-3.0, series.getMinY(), 0.0001);
        assertEquals(10.0, series.getMaxY(), 0.0001);
    }
    
    @Test
    public void testAdd_NaNValue_ignoredInMinMax() {
        series.add(new Year(2020), Double.NaN);
        assertTrue(Double.isNaN(series.getMinY()));
        assertTrue(Double.isNaN(series.getMaxY()));
        series.add(new Year(2021), 5.0);
        assertEquals(5.0, series.getMinY(), 0.0001);
        assertEquals(5.0, series.getMaxY(), 0.0001);
    }
    
    @Test
    public void testClone_independentCopy() throws CloneNotSupportedException {
        series.add(new Year(2020), 10.0);
        TimeSeries clone = (TimeSeries) series.clone();
        assertEquals(1, clone.getItemCount());
        series.add(new Year(2021), 20.0);
        assertEquals(1, clone.getItemCount());
    }
    
    @Test
    public void testCreateCopy_validRange_returnsCopy() throws CloneNotSupportedException {
        series.add(new Year(2020), 1.0);
        series.add(new Year(2021), 2.0);
        series.add(new Year(2022), 3.0);
        TimeSeries copy = series.createCopy(1, 2);
        assertEquals(2, copy.getItemCount());
        assertEquals(new Year(2021), copy.getTimePeriod(0));
        assertEquals(new Year(2022), copy.getTimePeriod(1));
    }
    
    @Test
    public void testAdd_negativeValue_updatesBounds() {
        series.add(new Year(2020), -10.0);
        series.add(new Year(2021), -5.0);
        assertEquals(-10.0, series.getMinY(), 0.0001);
        assertEquals(-5.0, series.getMaxY(), 0.0001);
    }

    // ========== New test cases to improve coverage ==========

    @Test
    public void testSetDomainDescription_roundTrip() {
        series.setDomainDescription("Custom Domain");
        assertEquals("Custom Domain", series.getDomainDescription());
    }

    @Test
    public void testSetRangeDescription_roundTrip() {
        series.setRangeDescription("Custom Range");
        assertEquals("Custom Range", series.getRangeDescription());
    }

    @Test
    public void testSetKey_changesKey() {
        series.setKey("New Key");
        assertEquals("New Key", series.getKey());
    }

    @Test
    public void testSetKey_null_throwsException() {
        try {
            series.setKey(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testAdd_TimeSeriesDataItem_withBoundsUpdate() {
        TimeSeriesDataItem item = new TimeSeriesDataItem(new Year(2020), 42.0);
        series.add(item);
        assertEquals(1, series.getItemCount());
        assertEquals(42.0, series.getMinY(), 0.0001);
        assertEquals(42.0, series.getMaxY(), 0.0001);
    }

    @Test
    public void testAdd_TimeSeriesDataItem_duplicate_throwsException() {
        series.add(new TimeSeriesDataItem(new Year(2020), 1.0));
        try {
            series.add(new TimeSeriesDataItem(new Year(2020), 2.0));
            fail("Expected SeriesException");
        } catch (SeriesException e) {
            // expected
        }
    }

    @Test
    public void testDelete_singleItemByIndex() {
        series.add(new Year(2020), 10.0);
        series.delete(0, 0);
        assertEquals(0, series.getItemCount());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testDelete_rangeStartNegative_throwsException() {
        series.delete(-1, 0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testDelete_rangeEndOutOfBounds_throwsException() {
        series.add(new Year(2020), 10.0);
        series.delete(0, 1);
    }

    @Test
    public void testSetMaximumItemCount_noChangeNeeded() {
        series.add(new Year(2020), 10.0);
        series.setMaximumItemCount(5);
        assertEquals(1, series.getItemCount());
    }

    @Test
    public void testSetMaximumItemCount_zero_removesAll() {
        series.add(new Year(2020), 10.0);
        series.setMaximumItemCount(0);
        assertEquals(0, series.getItemCount());
    }

    @Test
    public void testSetMaximumItemCount_negative_throwsException() {
        try {
            series.setMaximumItemCount(-1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testGetTimePeriod_validIndex() {
        series.add(new Year(2020), 10.0);
        series.add(new Year(2021), 20.0);
        assertEquals(new Year(2020), series.getTimePeriod(0));
        assertEquals(new Year(2021), series.getTimePeriod(1));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetTimePeriod_invalidIndex_throwsException() {
        series.getTimePeriod(0);
    }

    @Test
    public void testGetValue_validIndex() {
        series.add(new Year(2020), 10.0);
        assertEquals(10.0, series.getValue(0).doubleValue(), 0.0001);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValue_invalidIndex_throwsException() {
        series.getValue(0);
    }

    @Test
    public void testGetDataItem_validIndex() {
        series.add(new Year(2020), 10.0);
        TimeSeriesDataItem item = series.getDataItem(0);
        assertEquals(new Year(2020), item.getPeriod());
        assertEquals(10.0, item.getValue().doubleValue(), 0.0001);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetDataItem_invalidIndex_throwsException() {
        series.getDataItem(0);
    }

    @Test
    public void testGetDataItem_byPeriod_found() {
        series.add(new Year(2020), 10.0);
        TimeSeriesDataItem item = series.getDataItem(new Year(2020));
        assertNotNull(item);
        assertEquals(10.0, item.getValue().doubleValue(), 0.0001);
    }

    @Test
    public void testGetDataItem_byPeriod_notFound() {
        assertNull(series.getDataItem(new Year(2020)));
    }

    @Test
    public void testGetIndex_periodFound() {
        series.add(new Year(2020), 10.0);
        series.add(new Year(2021), 20.0);
        assertEquals(0, series.getIndex(new Year(2020)));
        assertEquals(1, series.getIndex(new Year(2021)));
    }

    @Test
    public void testGetIndex_periodNotFound() {
        series.add(new Year(2020), 10.0);
        assertEquals(-1, series.getIndex(new Year(2021)));
    }

    @Test
    public void testGetValue_byPeriod_found() {
        series.add(new Year(2020), 10.0);
        assertEquals(10.0, series.getValue(new Year(2020)).doubleValue(), 0.0001);
    }

    @Test
    public void testGetValue_byPeriod_notFound() {
        assertNull(series.getValue(new Year(2020)));
    }

    @Test
    public void testAddOrUpdate_withNaN_updatesCorrectly() {
        series.addOrUpdate(new Year(2020), Double.NaN);
        assertTrue(Double.isNaN(series.getMinY()));
        assertTrue(Double.isNaN(series.getMaxY()));
    }

    @Test
    public void testAddOrUpdate_existingItemUpdateToNaN() {
        series.add(new Year(2020), 10.0);
        series.addOrUpdate(new Year(2020), Double.NaN);
        assertTrue(Double.isNaN(series.getValue(0).doubleValue()));
    }

    @Test
    public void testDeleteByPeriod_notFound() {
        series.add(new Year(2020), 10.0);
        series.delete(new Year(2021));
        assertEquals(1, series.getItemCount());
    }

    @Test
    public void testSetMaximumItemAge_zero_removesAllOldItems() {
        series.add(new Year(2020), 10.0);
        series.setMaximumItemAge(0);
        assertEquals(1, series.getItemCount());
        series.add(new Year(2021), 20.0);
        assertEquals(1, series.getItemCount());
        assertEquals(new Year(2021), series.getTimePeriod(0));
    }

    @Test
    public void testGetMinY_withNaNValues_ignoresNaN() {
        series.add(new Year(2020), Double.NaN);
        series.add(new Year(2021), 5.0);
        series.add(new Year(2022), Double.NaN);
        assertEquals(5.0, series.getMinY(), 0.0001);
    }

    @Test
    public void testGetMaxY_withNaNValues_ignoresNaN() {
        series.add(new Year(2020), Double.NaN);
        series.add(new Year(2021), 5.0);
        series.add(new Year(2022), Double.NaN);
        assertEquals(5.0, series.getMaxY(), 0.0001);
    }

    @Test
    public void testGetMinY_allNaN_returnsNaN() {
        series.add(new Year(2020), Double.NaN);
        series.add(new Year(2021), Double.NaN);
        assertTrue(Double.isNaN(series.getMinY()));
    }

    @Test
    public void testGetMaxY_allNaN_returnsNaN() {
        series.add(new Year(2020), Double.NaN);
        series.add(new Year(2021), Double.NaN);
        assertTrue(Double.isNaN(series.getMaxY()));
    }

    @Test
    public void testCreateCopy_startEndIdentical() throws CloneNotSupportedException {
        series.add(new Year(2020), 1.0);
        TimeSeries copy = series.createCopy(0, 0);
        assertEquals(1, copy.getItemCount());
        assertEquals(new Year(2020), copy.getTimePeriod(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCopy_startGreaterThanEnd_throwsException() throws CloneNotSupportedException {
        series.add(new Year(2020), 1.0);
        series.createCopy(1, 0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testCreateCopy_startNegative_throwsException() throws CloneNotSupportedException {
        series.add(new Year(2020), 1.0);
        series.createCopy(-1, 0);
    }

    @Test
    public void testAddOrUpdate_whenItemCountExceedsMaxItemCount() {
        series.setMaximumItemCount(2);
        series.addOrUpdate(new Year(2020), 10.0);
        series.addOrUpdate(new Year(2021), 20.0);
        series.addOrUpdate(new Year(2022), 30.0);
        assertEquals(2, series.getItemCount());
        assertEquals(new Year(2021), series.getTimePeriod(0));
        assertEquals(new Year(2022), series.getTimePeriod(1));
    }

    @Test
    public void testAdd_whenItemCountExceedsMaxItemCount() {
        series.setMaximumItemCount(2);
        series.add(new Year(2020), 10.0);
        series.add(new Year(2021), 20.0);
        series.add(new Year(2022), 30.0);
        assertEquals(2, series.getItemCount());
        assertEquals(new Year(2021), series.getTimePeriod(0));
        assertEquals(new Year(2022), series.getTimePeriod(1));
    }

    @Test
    public void testEquals_sameContent_returnsTrue() {
        TimeSeries series1 = new TimeSeries("Test");
        TimeSeries series2 = new TimeSeries("Test");
        series1.add(new Year(2020), 10.0);
        series2.add(new Year(2020), 10.0);
        assertTrue(series1.equals(series2));
    }

    @Test
    public void testEquals_differentKey_returnsFalse() {
        TimeSeries series1 = new TimeSeries("Test1");
        TimeSeries series2 = new TimeSeries("Test2");
        assertFalse(series1.equals(series2));
    }

    @Test
    public void testEquals_differentData_returnsFalse() {
        TimeSeries series1 = new TimeSeries("Test");
        TimeSeries series2 = new TimeSeries("Test");
        series1.add(new Year(2020), 10.0);
        series2.add(new Year(2020), 20.0);
        assertFalse(series1.equals(series2));
    }

    @Test
    public void testEquals_null_returnsFalse() {
        assertFalse(series.equals(null));
    }

    @Test
    public void testHashCode_consistentWithEquals() {
        TimeSeries series1 = new TimeSeries("Test");
        TimeSeries series2 = new TimeSeries("Test");
        series1.add(new Year(2020), 10.0);
        series2.add(new Year(2020), 10.0);
        assertEquals(series1.hashCode(), series2.hashCode());
    }

    @Test
    public void testGetItemCount_emptySeries() {
        assertEquals(0, series.getItemCount());
    }

    @Test
    public void testGetItemCount_afterAdd() {
        series.add(new Year(2020), 10.0);
        assertEquals(1, series.getItemCount());
    }

    @Test
    public void testGetItemCount_afterDelete() {
        series.add(new Year(2020), 10.0);
        series.delete(0, 0);
        assertEquals(0, series.getItemCount());
    }

    @Test
    public void testGetTimePeriodClass_whenEmpty() {
        assertNull(series.getTimePeriodClass());
    }

    @Test
    public void testGetTimePeriodClass_afterAdd() {
        series.add(new Year(2020), 10.0);
        assertEquals(Year.class, series.getTimePeriodClass());
    }

    @Test
    public void testToString_notEmpty() {
        series.add(new Year(2020), 10.0);
        String str = series.toString();
        assertNotNull(str);
        assertTrue(str.contains("Test Series"));
    }

    @Test
    public void testHistoryCount_notNegative() {
        assertTrue(series.getHistoryCount() >= 0);
    }

    @Test
    public void testGetItemCount_maxItemCountEnforced() {
        series.setMaximumItemCount(1);
        series.add(new Year(2020), 10.0);
        series.add(new Year(2021), 20.0);
        assertEquals(1, series.getItemCount());
    }
}