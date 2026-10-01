package org.jfree.data.time;

import static org.junit.Assert.*;
import org.junit.Test;

import java.util.Date;

/**
 * JUnit 4 test class for TimePeriodValues.
 */
public class TimePeriodValuesTest {

    // Helper method to create a simple TimePeriod for testing
    private TimePeriod createTimePeriod(long startMillis, long endMillis) {
        return new FixedMillisecond(new Date(startMillis)) {
            // FixedMillisecond already implements TimePeriod appropriately
            // We use a subclass to provide custom start/end times directly.
            @Override
            public Date getStart() {
                return new Date(startMillis);
            }

            @Override
            public Date getEnd() {
                return new Date(endMillis);
            }
        };
    }

    // Tests constructor and basic getters
    @Test
    public void testTimePeriodValues_constructWithName_createsEmptySeries() {
        TimePeriodValues s = new TimePeriodValues("Test");
        assertEquals("Time", s.getDomainDescription());
        assertEquals("Value", s.getRangeDescription());
        assertEquals(0, s.getItemCount());
    }

    // Tests adding a single item and checking bounds
    @Test
    public void testAdd_singleItem_updatesBoundsCorrectly() {
        TimePeriodValues s = new TimePeriodValues("Test");
        TimePeriod period = createTimePeriod(1000, 2000);
        s.add(period, 10.0);
        assertEquals(1, s.getItemCount());
        assertEquals(0, s.getMinStartIndex());
        assertEquals(0, s.getMaxStartIndex());
        assertEquals(0, s.getMinMiddleIndex());
        assertEquals(0, s.getMaxMiddleIndex());
        assertEquals(0, s.getMinEndIndex());
        assertEquals(0, s.getMaxEndIndex());
    }

    // Tests adding two items and verifying boundary indices (normal case)
    @Test
    public void testAdd_twoItems_correctMinMaxIndices() {
        TimePeriodValues s = new TimePeriodValues("Test");
        TimePeriod period1 = createTimePeriod(1000, 2000);
        TimePeriod period2 = createTimePeriod(500, 1500);
        s.add(period1, 10.0);
        s.add(period2, 20.0);
        // period2 starts at 500 vs period1 at 1000, so min start is index 1
        assertEquals(1, s.getMinStartIndex());
        assertEquals(0, s.getMaxStartIndex());
        assertEquals(1, s.getMinEndIndex());   // period2 ends at 1500 vs period1 at 2000 => min end index 1
        assertEquals(0, s.getMaxEndIndex());   // period1 ends at 2000 => max end index 0
    }

    // Tests adding items that set new min and max for middle
    @Test
    public void testAdd_itemsWithDifferentMiddles_updatesMiddleIndices() {
        TimePeriodValues s = new TimePeriodValues("Test");
        // middle = start + (end-start)/2
        TimePeriod period1 = createTimePeriod(1000, 2000); // middle = 1500
        TimePeriod period2 = createTimePeriod(500, 1000);  // middle = 750
        s.add(period1, 10.0);
        s.add(period2, 20.0);
        assertEquals(1, s.getMinMiddleIndex()); // min middle is 750 at index 1
        assertEquals(0, s.getMaxMiddleIndex()); // max middle is 1500 at index 0
    }

    // Tests adding a null item throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testAdd_nullItem_throwsIllegalArgumentException() {
        TimePeriodValues s = new TimePeriodValues("Test");
        s.add((TimePeriodValue) null);
    }

    // Tests getDataItem returns correct item
    @Test
    public void testGetDataItem_validIndex_returnsCorrectItem() {
        TimePeriodValues s = new TimePeriodValues("Test");
        TimePeriod period = createTimePeriod(1000, 2000);
        s.add(period, 42.0);
        TimePeriodValue item = s.getDataItem(0);
        assertEquals(42.0, item.getValue().doubleValue(), 0.0001);
    }

    // Tests getTimePeriod and getValue methods
    @Test
    public void testGetTimePeriodAndGetValue_validIndex_returnsCorrectValues() {
        TimePeriodValues s = new TimePeriodValues("Test");
        TimePeriod period = createTimePeriod(1000, 2000);
        s.add(period, 123.0);
        assertNotNull(s.getTimePeriod(0));
        assertEquals(123.0, s.getValue(0).doubleValue(), 0.0001);
    }

    // Tests update method changes value
    @Test
    public void testUpdate_validIndex_changesValueAndFiresEvent() {
        TimePeriodValues s = new TimePeriodValues("Test");
        s.add(createTimePeriod(1000, 2000), 10.0);
        s.update(0, 99.0);
        assertEquals(99.0, s.getValue(0).doubleValue(), 0.0001);
    }

    // Tests delete method and bounds recalculation
    @Test
    public void testDelete_singleElement_clearsBounds() {
        TimePeriodValues s = new TimePeriodValues("Test");
        s.add(createTimePeriod(1000, 2000), 10.0);
        s.delete(0, 0);
        assertEquals(0, s.getItemCount());
        assertEquals(-1, s.getMinStartIndex());
        assertEquals(-1, s.getMaxStartIndex());
        assertEquals(-1, s.getMinMiddleIndex());
        assertEquals(-1, s.getMaxMiddleIndex());
        assertEquals(-1, s.getMinEndIndex());
        assertEquals(-1, s.getMaxEndIndex());
    }

    // Tests delete multiple elements and recalculates bounds
    @Test
    public void testDelete_multipleElements_recalculatesBounds() {
        TimePeriodValues s = new TimePeriodValues("Test");
        s.add(createTimePeriod(1000, 2000), 10.0); // index 0
        s.add(createTimePeriod(500, 1500), 20.0);  // index 1
        s.add(createTimePeriod(2000, 3000), 30.0); // index 2
        s.delete(0, 1); // delete first two, only index 2 remains
        assertEquals(1, s.getItemCount());
        assertEquals(0, s.getMinStartIndex());
        assertEquals(0, s.getMaxStartIndex());
    }

    // Tests equals method with equal objects
    @Test
    public void testEquals_equalObjects_returnsTrue() {
        TimePeriodValues s1 = new TimePeriodValues("Test");
        TimePeriodValues s2 = new TimePeriodValues("Test");
        s1.add(createTimePeriod(1000, 2000), 10.0);
        s2.add(createTimePeriod(1000, 2000), 10.0);
        assertTrue(s1.equals(s2));
    }

    // Tests equals method with different domain descriptions
    @Test
    public void testEquals_differentDomainDescription_returnsFalse() {
        TimePeriodValues s1 = new TimePeriodValues("Test", "Domain1", "Range");
        TimePeriodValues s2 = new TimePeriodValues("Test", "Domain2", "Range");
        assertFalse(s1.equals(s2));
    }

    // Tests equals method with different range descriptions
    @Test
    public void testEquals_differentRangeDescription_returnsFalse() {
        TimePeriodValues s1 = new TimePeriodValues("Test", "Domain", "Range1");
        TimePeriodValues s2 = new TimePeriodValues("Test", "Domain", "Range2");
        assertFalse(s1.equals(s2));
    }

    // Tests equals method with different data
    @Test
    public void testEquals_differentData_returnsFalse() {
        TimePeriodValues s1 = new TimePeriodValues("Test");
        TimePeriodValues s2 = new TimePeriodValues("Test");
        s1.add(createTimePeriod(1000, 2000), 10.0);
        s2.add(createTimePeriod(1000, 2000), 20.0);
        assertFalse(s1.equals(s2));
    }

    // Tests equals with null
    @Test
    public void testEquals_nullObject_returnsFalse() {
        TimePeriodValues s = new TimePeriodValues("Test");
        assertFalse(s.equals(null));
    }

    // Tests equals with different type
    @Test
    public void testEquals_differentType_returnsFalse() {
        TimePeriodValues s = new TimePeriodValues("Test");
        assertFalse(s.equals("string"));
    }

    // Tests clone method
    @Test
    public void testClone_returnsEqualCopy() throws CloneNotSupportedException {
        TimePeriodValues s = new TimePeriodValues("Test");
        s.add(createTimePeriod(1000, 2000), 10.0);
        TimePeriodValues cloned = (TimePeriodValues) s.clone();
        assertNotSame(s, cloned);
        assertEquals(s, cloned);
    }

    // Tests createCopy method
    @Test
    public void testCreateCopy_subset_returnsCorrectCopy() throws CloneNotSupportedException {
        TimePeriodValues s = new TimePeriodValues("Test");
        s.add(createTimePeriod(1000, 2000), 10.0);
        s.add(createTimePeriod(500, 1500), 20.0);
        s.add(createTimePeriod(2000, 3000), 30.0);
        TimePeriodValues copy = s.createCopy(1, 2);
        assertEquals(2, copy.getItemCount());
        assertEquals(20.0, copy.getValue(0).doubleValue(), 0.0001);
        assertEquals(30.0, copy.getValue(1).doubleValue(), 0.0001);
    }

    // Tests setDomainDescription and setRangeDescription fire property change
    @Test
    public void testSetDomainDescription_newValue_updatesDescription() {
        TimePeriodValues s = new TimePeriodValues("Test");
        s.setDomainDescription("New Domain");
        assertEquals("New Domain", s.getDomainDescription());
    }

    @Test
    public void testSetRangeDescription_newValue_updatesDescription() {
        TimePeriodValues s = new TimePeriodValues("Test");
        s.setRangeDescription("New Range");
        assertEquals("New Range", s.getRangeDescription());
    }
}