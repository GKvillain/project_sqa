```java
package org.jfree.data.xy;

import static org.junit.Assert.*;
import org.junit.Test;
import org.jfree.data.general.SeriesException;

public class XYSeriesTest {

    @Test
    public void testConstructor_default_autoSortAndAllowDuplicateTrue() {
        XYSeries s = new XYSeries("S1");
        assertTrue(s.getAutoSort());
        assertTrue(s.getAllowDuplicateXValues());
        assertEquals("S1", s.getKey());
        assertEquals(0, s.getItemCount());
    }

    @Test
    public void testConstructor_customFlags() {
        XYSeries s = new XYSeries("S2", false, false);
        assertFalse(s.getAutoSort());
        assertFalse(s.getAllowDuplicateXValues());
    }

    @Test
    public void testAdd_sortedDuplicateAllowed_addsAfterDuplicates() {
        XYSeries s = new XYSeries("S3", true, true);
        s.add(1.0, 1.0);
        s.add(1.0, 2.0); // duplicate x allowed
        assertEquals(2, s.getItemCount());
        assertEquals(1.0, s.getX(0).doubleValue(), 0.0);
        assertEquals(1.0, s.getY(0).doubleValue(), 0.0);
        assertEquals(1.0, s.getX(1).doubleValue(), 0.0);
        assertEquals(2.0, s.getY(1).doubleValue(), 0.0);
    }

    @Test(expected = SeriesException.class)
    public void testAdd_sortedDuplicateNotAllowed_throwsSeriesException() {
        XYSeries s = new XYSeries("S4", true, false);
        s.add(1.0, 1.0);
        s.add(1.0, 2.0);
    }

    @Test(expected = SeriesException.class)
    public void testAdd_unsortedDuplicateNotAllowed_throwsSeriesException() {
        XYSeries s = new XYSeries("S5", false, false);
        s.add(1.0, 1.0);
        s.add(1.0, 2.0);
    }

    @Test
    public void testAdd_exceedsMaxItemCount_removesFirst() {
        XYSeries s = new XYSeries("S6");
        s.setMaximumItemCount(2);
        s.add(1.0, 1.0);
        s.add(2.0, 2.0);
        s.add(3.0, 3.0);
        assertEquals(2, s.getItemCount());
        assertEquals(2.0, s.getX(0).doubleValue(), 0.0);
        assertEquals(3.0, s.getX(1).doubleValue(), 0.0);
    }

    @Test
    public void testAddOrUpdate_existingXAndDuplicateNotAllowed_updatesItem() {
        XYSeries s = new XYSeries("S7", true, false);
        s.add(1.0, 1.0);
        XYDataItem result = s.addOrUpdate(1.0, 10.0);
        assertNotNull(result);
        assertEquals(1.0, result.getX().doubleValue(), 0.0);
        assertEquals(1.0, result.getY().doubleValue(), 0.0);
        assertEquals(1, s.getItemCount());
        assertEquals(10.0, s.getY(0).doubleValue(), 0.0);
    }

    @Test
    public void testAddOrUpdate_existingXAndDuplicateAllowed_addsDuplicate() {
        // This test detects the defect in addOrUpdate when allowDuplicateXValues is true.
        // In the buggy version, an IndexOutOfBoundsException is thrown.
        XYSeries s = new XYSeries("S8", true, true);
        s.add(1.0, 1.0);
        XYDataItem result = s.addOrUpdate(1.0, 2.0);
        assertEquals(2, s.getItemCount());
        assertNull(result);
        assertEquals(1.0, s.getX(0).doubleValue(), 0.0);
        assertEquals(1.0, s.getY(0).doubleValue(), 0.0);
        assertEquals(1.0, s.getX(1).doubleValue(), 0.0);
        assertEquals(2.0, s.getY(1).doubleValue(), 0.0);
    }

    @Test
    public void testAddOrUpdate_unsortedExistingXAndDuplicateNotAllowed_updatesItem() {
        XYSeries s = new XYSeries("S9", false, false);
        s.add(1.0, 1.0);
        s.add(2.0, 2.0);
        XYDataItem result = s.addOrUpdate(1.0, 10.0);
        assertNotNull(result);
        assertEquals(1.0, result.getY().doubleValue(), 0.0);
        assertEquals(2, s.getItemCount());
        assertEquals(10.0, s.getY(0).doubleValue(), 0.0);
        assertEquals(2.0, s.getY(1).doubleValue(), 0.0);
    }

    @Test
    public void testAddOrUpdate_newX_sorted_addsNewItem() {
        XYSeries s = new XYSeries("S10", true, false);
        s.add(2.0, 2.0);
        XYDataItem result = s.addOrUpdate(1.0, 1.0);
        assertNull(result);
        assertEquals(2, s.getItemCount());
        assertEquals(1.0, s.getX(0).doubleValue(), 0.0);
        assertEquals(2.0, s.getX(1).doubleValue(), 0.0);
    }

    @Test
    public void testAddOrUpdate_unsortedNewX_addsNewItem() {
        XYSeries s = new XYSeries("S11", false, false);
        s.add(2.0, 2.0);
        XYDataItem result = s.addOrUpdate(1.0, 1.0);
        assertNull(result);
        assertEquals(2, s.getItemCount());
        assertEquals(2.0, s.getX(0).doubleValue(), 0.0); // order preserved
        assertEquals(1.0, s.getX(1).doubleValue(), 0.0); // appended at end
    }

    @Test
    public void testIndexOf_sortedExistingX_returnsIndex() {
        XYSeries s = new XYSeries("S12", true, true);
        s.add(5.0, 5.0);
        s.add(3.0, 3.0);
        s.add(7.0, 7.0);
        assertEquals(1, s.indexOf(5.0)); // sorted: 3,5,7 -> index 1
    }

    @Test
    public void testIndexOf_unsortedExistingX_returnsIndex() {
        XYSeries s = new XYSeries("S13", false, false);
        s.add(5.0, 5.0);
        s.add(3.0, 3.0);
        s.add(7.0, 7.0);
        assertEquals(0, s.indexOf(5.0)); // unsorted: first added at index 0
    }

    @Test
    public void testIndexOf_nonExistentX_returnsNegative() {
        XYSeries s = new XYSeries("S14", true, true);
        s.add(1.0, 1.0);
        assertTrue(s.indexOf(99.0) < 0);
    }

    @Test
    public void testSetMaximumItemCount_trimData_removesFirstItems() {
        XYSeries s = new XYSeries("S15");
        s.add(1.0, 1.0);
        s.add(2.0, 2.0);
        s.add(3.0, 3.0);
        s.setMaximumItemCount(2);
        assertEquals(2, s.getItemCount());
        assertEquals(2.0, s.getX(0).doubleValue(), 0.0);
        assertEquals(3.0, s.getX(1).doubleValue(), 0.0);
    }

    @Test
    public void testDelete_range_removesItems() {
        XYSeries s = new XYSeries("S16");
        s.add(1.0, 1.0);
        s.add(2.0, 2.0);
        s.add(3.0, 3.0);
        s.add(4.0, 4.0);
        s.delete(1, 2); // remove indices 1 and 2 (2.0 and 3.0)
        assertEquals(2, s.getItemCount());
        assertEquals(1.0, s.getX(0).doubleValue(), 0.0);
        assertEquals(4.0, s.getX(1).doubleValue(), 0.0);
    }

    @Test
    public void testRemove_byIndex_removesAndReturns() {
        XYSeries s = new XYSeries("S17");
        s.add(1.0, 1.0);
        s.add(2.0, 2.0);
        XYDataItem removed = s.remove(0);
        assertNotNull(removed);
        assertEquals(1.0, removed.getX().doubleValue(), 0.0);
        assertEquals(1, s.getItemCount());
        assertEquals(2.0, s.getX(0).doubleValue(), 0.0);
    }

    @Test
    public void testUpdate_existingX_updatesY() {
        XYSeries s = new XYSeries("S18", true, false);
        s.add(1.0, 1.0);
        s.update(1.0, 200.0);
        assertEquals(200.0, s.getY(0).doubleValue(), 0.0);
    }

    @Test(expected = SeriesException.class)
    public void testUpdate_nonExistingX_throwsSeriesException() {
        XYSeries s = new XYSeries("S19", true, false);
        s.add(1.0, 1.0);
        s.update(2.0, 200.0);
    }

    @Test
    public void testClone_createsIndependentCopy() throws CloneNotSupportedException {
        XYSeries s = new XYSeries("S20");
        s.add(1.0, 1.0);
        XYSeries clone = (XYSeries) s.clone();
        assertEquals(s.getItemCount(), clone.getItemCount());
        assertEquals(s.getX(0), clone.getX(0));
        // modify clone to verify independence
        clone.add(2.0, 2.0);
        assertEquals(1, s.getItemCount());
        assertEquals(2, clone.getItemCount());
    }

    // ========== New test cases for uncovered areas ==========

    @Test
    public void testConstructor_unsortedDuplicateAllowed() {
        XYSeries s = new XYSeries("S", false, true);
        assertFalse(s.getAutoSort());
        assertTrue(s.getAllowDuplicateXValues());
        assertEquals("S", s.getKey());
        assertEquals(0, s.getItemCount());
    }

    @Test
    public void testAdd_unsortedDuplicateAllowed_addsDuplicate() {
        XYSeries s = new XYSeries("S", false, true);
        s.add(1.0, 1.0);
        s.add(1.0, 2.0);
        assertEquals(2, s.getItemCount());
        assertEquals(1.0, s.getX(0).doubleValue(), 0.0);
        assertEquals(1.0, s.getY(0).doubleValue(), 0.0);
        assertEquals(1.0, s.getX(1).doubleValue(), 0.0);
        assertEquals(2.0, s.getY(1).doubleValue(), 0.0);
    }

    @Test
    public void testAddOrUpdate_unsortedDuplicateAllowed_addsDuplicate() {
        XYSeries s = new XYSeries("S", false, true);
        s.add(1.0, 1.0);
        XYDataItem result = s.addOrUpdate(1.0, 2.0);
        assertNull(result);
        assertEquals(2, s.getItemCount());
        assertEquals(1.0, s.getX(0).doubleValue(), 0.0);
        assertEquals(1.0, s.getY(0).doubleValue(), 0.0);
        assertEquals(1.0, s.getX(1).doubleValue(), 0.0);
        assertEquals(2.0, s.getY(1).doubleValue(), 0.0);
    }

    @Test
    public void testRemove_byXValue_removesItem() {
        XYSeries s = new XYSeries("S");
        s.add(1.0, 1.0);
        s.add(2.0, 2.0);
        XYDataItem removed = s.remove(1.0);
        assertNotNull(removed);
        assertEquals(1.0, removed.getX().doubleValue(), 0.0);
        assertEquals(1, s.getItemCount());
        assertEquals(2.0, s.getX(0).doubleValue(), 0.0);
    }

    @Test
    public void testRemove_byXValue_nonExistent_returnsNull() {
        XYSeries s = new XYSeries("S");
        s.add(1.0, 1.0);
        XYDataItem removed = s.remove(99.0);
        assertNull(removed);
        assertEquals(1, s.getItemCount());
    }

    @Test
    public void testClear_removesAllItems() {
        XYSeries s = new XYSeries("S");
        s.add(1.0, 1.0);
        s.add(2.0, 2.0);
        s.clear();
        assertEquals(0, s.getItemCount());
    }

    @Test
    public void testEquals_sameSeries_returnsTrue() {
        XYSeries s1 = new XYSeries("S");
        s1.add(1.0, 1.0);
        XYSeries s2 = new XYSeries("S");
        s2.add(1.0, 1.0);
        assertTrue(s1.equals(s2));
    }

    @Test
    public void testEquals_differentSeries_returnsFalse() {
        XYSeries s1 = new XYSeries("S1");
        s1.add(1.0, 1.0);
        XYSeries s2 = new XYSeries("S2");
        s2.add(1.0, 1.0);
        assertFalse(s1.equals(s2));
    }

    @Test
    public void testHashCode_consistentWithEquals() {
        XYSeries s1 = new XYSeries("S");
        s1.add(1.0, 1.0);
        XYSeries s2 = new XYSeries("S");
        s2.add(1.0, 1.0);
        assertEquals(s1.hashCode(), s2.hashCode());
    }

    @Test
    public void testCreateCopy_returnsCopy() throws CloneNotSupportedException {
        XYSeries s = new XYSeries("S");
        s.add(1.0, 1.0);
        s.add(2.0, 2.0);
        s.add(3.0, 3.0);
        XYSeries copy = s.createCopy(0, 1);
        assertEquals(2, copy.getItemCount());
        assertEquals(1.0, copy.getX(0).doubleValue(), 0.0);
        assertEquals(2.0, copy.getX(1).doubleValue(), 0.0);
    }

    @Test
    public void testGetDataItem_returnsCorrectItem() {
        XYSeries s = new XYSeries("S");
        s.add(1.0, 1.0);
        XYDataItem item = s.getDataItem(0);
        assertEquals(1.0, item.getX().doubleValue(), 0.0);
        assertEquals(1.0, item.getY().doubleValue(), 0.0);
    }

    @Test
    public void testSetKey_changesKey() {
        XYSeries s = new XYSeries("Old");
        s.setKey("New");
        assertEquals("New", s.getKey());
    }

    @Test
    public void testGetMaximumItemCount_defaultValue() {
        XYSeries s = new XYSeries