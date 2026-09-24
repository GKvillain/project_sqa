package org.jfree.data.xy;

import org.jfree.data.general.SeriesChangeEvent;
import org.jfree.data.general.SeriesChangeListener;
import org.jfree.data.general.SeriesException;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class XYSeriesTest {

    private XYSeries series;

    @Before
    public void setUp() {
        this.series = new XYSeries("Test Series", true, true);
    }

    // Tests adding an existing X value with addOrUpdate when allowDuplicateXValues is true (Defects4J Chart-5)
    @Test
    public void testAddOrUpdate_duplicateXAllowedAndExists_addsItem() {
        XYSeries s = new XYSeries("S", true, true);
        s.add(1.0, 10.0);
        s.addOrUpdate(1.0, 20.0);
        assertEquals(2, s.getItemCount());
        assertEquals(10.0, s.getY(0).doubleValue(), 1e-9);
        assertEquals(20.0, s.getY(1).doubleValue(), 1e-9);
    }

    // Tests addOrUpdate overwrites existing item when duplicates are not allowed
    @Test
    public void testAddOrUpdate_duplicateXNotAllowed_overwritesItem() {
        XYSeries s = new XYSeries("S", true, false);
        s.add(1.0, 10.0);
        XYDataItem overwritten = s.addOrUpdate(1.0, 20.0);
        assertNotNull(overwritten);
        assertEquals(10.0, overwritten.getY().doubleValue(), 1e-9);
        assertEquals(1, s.getItemCount());
        assertEquals(20.0, s.getY(0).doubleValue(), 1e-9);
    }

    // Tests addOrUpdate with new item adds data to series
    @Test
    public void testAddOrUpdate_newItem_returnsNullAndAdds() {
        XYSeries s = new XYSeries("S", true, false);
        XYDataItem overwritten = s.addOrUpdate(1.0, 10.0);
        assertNull(overwritten);
        assertEquals(1, s.getItemCount());
        assertEquals(1.0, s.getX(0).doubleValue(), 1e-9);
        assertEquals(10.0, s.getY(0).doubleValue(), 1e-9);
    }

    // Tests addOrUpdate with null x throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testAddOrUpdate_nullX_throwsException() {
        this.series.addOrUpdate(null, new Double(10.0));
    }

    // Tests add with null item throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testAdd_nullItem_throwsException() {
        this.series.add((XYDataItem) null);
    }

    // Tests adding duplicate X throws SeriesException when duplicates not allowed
    @Test(expected = SeriesException.class)
    public void testAdd_duplicateXNotAllowedSorted_throwsException() {
        XYSeries s = new XYSeries("S", true, false);
        s.add(1.0, 10.0);
        s.add(1.0, 20.0);
    }

    // Tests adding duplicate X in unsorted series throws SeriesException when duplicates not allowed
    @Test(expected = SeriesException.class)
    public void testAdd_duplicateXNotAllowedUnsorted_throwsException() {
        XYSeries s = new XYSeries("S", false, false);
        s.add(1.0, 10.0);
        s.add(1.0, 20.0);
    }

    // Tests auto-sorting order when items are added out of order
    @Test
    public void testAdd_autoSorted_maintainsAscendingOrder() {
        this.series.add(3.0, 30.0);
        this.series.add(1.0, 10.0);
        this.series.add(2.0, 20.0);

        assertEquals(3, this.series.getItemCount());
        assertEquals(1.0, this.series.getX(0).doubleValue(), 1e-9);
        assertEquals(2.0, this.series.getX(1).doubleValue(), 1e-9);
        assertEquals(3.0, this.series.getX(2).doubleValue(), 1e-9);
    }

    // Tests unsorted series preserves insertion order
    @Test
    public void testAdd_unsorted_preservesInsertionOrder() {
        XYSeries s = new XYSeries("Unsorted", false, true);
        s.add(3.0, 30.0);
        s.add(1.0, 10.0);
        s.add(2.0, 20.0);

        assertEquals(3, s.getItemCount());
        assertEquals(3.0, s.getX(0).doubleValue(), 1e-9);
        assertEquals(1.0, s.getX(1).doubleValue(), 1e-9);
        assertEquals(2.0, s.getX(2).doubleValue(), 1e-9);
    }

    // Tests maximum item count enforcement on adding items
    @Test
    public void testSetMaximumItemCount_exceedingCapacity_removesOldest() {
        this.series.setMaximumItemCount(2);
        this.series.add(1.0, 10.0);
        this.series.add(2.0, 20.0);
        this.series.add(3.0, 30.0);

        assertEquals(2, this.series.getItemCount());
        assertEquals(2.0, this.series.getX(0).doubleValue(), 1e-9);
        assertEquals(3.0, this.series.getX(1).doubleValue(), 1e-9);
    }

    // Tests reducing maximum item count trims existing elements
    @Test
    public void testSetMaximumItemCount_reduceAfterPopulating_removesExcess() {
        this.series.add(1.0, 10.0);
        this.series.add(2.0, 20.0);
        this.series.add(3.0, 30.0);
        this.series.setMaximumItemCount(1);

        assertEquals(1, this.series.getItemCount());
        assertEquals(3.0, this.series.getX(0).doubleValue(), 1e-9);
    }

    // Tests delete removes range of items
    @Test
    public void testDelete_validRange_removesItems() {
        this.series.add(1.0, 10.0);
        this.series.add(2.0, 20.0);
        this.series.add(3.0, 30.0);
        this.series.add(4.0, 40.0);

        this.series.delete(1, 2);
        assertEquals(2, this.series.getItemCount());
        assertEquals(1.0, this.series.getX(0).doubleValue(), 1e-9);
        assertEquals(4.0, this.series.getX(1).doubleValue(), 1e-9);
    }

    // Tests remove by index and by X value
    @Test
    public void testRemove_byIndexAndByX_removesItem() {
        this.series.add(1.0, 10.0);
        this.series.add(2.0, 20.0);

        XYDataItem item1 = this.series.remove(0);
        assertEquals(1.0, item1.getX().doubleValue(), 1e-9);
        assertEquals(1, this.series.getItemCount());

        XYDataItem item2 = this.series.remove(new Double(2.0));
        assertEquals(2.0, item2.getX().doubleValue(), 1e-9);
        assertEquals(0, this.series.getItemCount());
    }

    // Tests clear removes all items
    @Test
    public void testClear_populatedSeries_clearsAll() {
        this.series.add(1.0, 10.0);
        this.series.add(2.0, 20.0);
        this.series.clear();
        assertEquals(0, this.series.getItemCount());
    }

    // Tests updateByIndex changes y value
    @Test
    public void testUpdateByIndex_validIndex_updatesValue() {
        this.series.add(1.0, 10.0);
        this.series.updateByIndex(0, new Double(15.0));
        assertEquals(15.0, this.series.getY(0).doubleValue(), 1e-9);
    }

    // Tests update by X value throws SeriesException if X does not exist
    @Test(expected = SeriesException.class)
    public void testUpdate_nonExistingX_throwsException() {
        this.series.add(1.0, 10.0);
        this.series.update(new Double(2.0), new Double(20.0));
    }

    // Tests update by X value modifies existing item
    @Test
    public void testUpdate_existingX_updatesValue() {
        this.series.add(1.0, 10.0);
        this.series.update(new Double(1.0), new Double(50.0));
        assertEquals(50.0, this.series.getY(0).doubleValue(), 1e-9);
    }

    // Tests toArray converts series to 2D double array including NaN for null y
    @Test
    public void testToArray_withNullY_convertsToNaN() {
        this.series.add(new Double(1.0), null);
        this.series.add(2.0, 20.0);

        double[][] array = this.series.toArray();
        assertEquals(2, array.length);
        assertEquals(2, array[0].length);
        assertEquals(1.0, array[0][0], 1e-9);
        assertTrue(Double.isNaN(array[1][0]));
        assertEquals(2.0, array[0][1], 1e-9);
        assertEquals(20.0, array[1][1], 1e-9);
    }

    // Tests cloning and equality check
    @Test
    public void testCloneAndEquals_clonedSeries_areEqual() throws CloneNotSupportedException {
        this.series.add(1.0, 10.0);
        this.series.add(2.0, 20.0);

        XYSeries clone = (XYSeries) this.series.clone();
        assertEquals(this.series, clone);
        assertEquals(this.series.hashCode(), clone.hashCode());

        clone.add(3.0, 30.0);
        assertFalse(this.series.equals(clone));
    }

    // Tests createCopy copies a subset of data
    @Test
    public void testCreateCopy_validRange_createsSubSeries() throws CloneNotSupportedException {
        this.series.add(1.0, 10.0);
        this.series.add(2.0, 20.0);
        this.series.add(3.0, 30.0);

        XYSeries copy = this.series.createCopy(1, 2);
        assertEquals(2, copy.getItemCount());
        assertEquals(2.0, copy.getX(0).doubleValue(), 1e-9);
        assertEquals(3.0, copy.getX(1).doubleValue(), 1e-9);
    }

    // Tests getItems returns unmodifiable list
    @Test(expected = UnsupportedOperationException.class)
    public void testGetItems_unmodifiableList_throwsExceptionOnModification() {
        this.series.add(1.0, 10.0);
        List items = this.series.getItems();
        items.add(new XYDataItem(2.0, 20.0));
    }
}