package org.jfree.data.statistics;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.jfree.data.Range;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for DefaultBoxAndWhiskerCategoryDataset.
 * Tests focus on the add method and related cached bounds logic.
 */
public class DefaultBoxAndWhiskerCategoryDatasetTest {

    private DefaultBoxAndWhiskerCategoryDataset dataset;
    private static final Comparable ROW_KEY = "Row1";
    private static final Comparable COL_KEY = "Col1";
    private static final Comparable ROW_KEY2 = "Row2";
    private static final Comparable COL_KEY2 = "Col2";

    @Before
    public void setUp() {
        dataset = new DefaultBoxAndWhiskerCategoryDataset();
    }

    // Helper to create a BoxAndWhiskerItem with custom min and max outlier values
    private BoxAndWhiskerItem createItem(Double minOutlier, Double maxOutlier) {
        // Using null for other fields since they are not directly tested
        return new BoxAndWhiskerItem(new Double(1.0), new Double(1.0),
                new Double(1.0), new Double(1.0), new Double(1.0),
                new Double(1.0), minOutlier, maxOutlier, new ArrayList<Double>());
    }

    // Tests adding first item: initializes min/max range values
    @Test
    public void testAdd_firstItem_setsMinMaxRangeValues() {
        BoxAndWhiskerItem item = createItem(1.0, 10.0);
        dataset.add(item, ROW_KEY, COL_KEY);
        assertEquals(1.0, dataset.getRangeLowerBound(false), 0.0001);
        assertEquals(10.0, dataset.getRangeUpperBound(false), 0.0001);
    }

    // Tests adding second item with higher max and lower min: updates bounds
    @Test
    public void testAdd_secondItemHigherMaxAndLowerMin_updatesBounds() {
        dataset.add(createItem(1.0, 10.0), ROW_KEY, COL_KEY);
        dataset.add(createItem(0.5, 20.0), ROW_KEY2, COL_KEY2);
        assertEquals(0.5, dataset.getRangeLowerBound(false), 0.0001);
        assertEquals(20.0, dataset.getRangeUpperBound(false), 0.0001);
    }

    // Tests adding an item at the same row/col position as existing: triggers updateBounds() and recalculates
    @Test
    public void testAdd_sameRowColAsExisting_triggersUpdateBounds() {
        dataset.add(createItem(1.0, 10.0), ROW_KEY, COL_KEY);
        // Override the same cell
        dataset.add(createItem(5.0, 6.0), ROW_KEY, COL_KEY);
        // After updateBounds(), min and max are recalculated by iterating over dataset
        // With only one item left, min/max should be based on that item
        assertEquals(5.0, dataset.getRangeLowerBound(false), 0.0001);
        assertEquals(6.0, dataset.getRangeUpperBound(false), 0.0001);
    }

    // Tests adding an item with null minOutlier and maxOutlier: should handle NaN
    @Test
    public void testAdd_nullMinMaxOutlier_handlesNaN() {
        BoxAndWhiskerItem item = new BoxAndWhiskerItem(new Double(1.0), new Double(1.0),
                new Double(1.0), new Double(1.0), new Double(1.0),
                new Double(1.0), null, null, new ArrayList<Double>());
        dataset.add(item, ROW_KEY, COL_KEY);
        assertTrue(Double.isNaN(dataset.getRangeLowerBound(false)));
        assertTrue(Double.isNaN(dataset.getRangeUpperBound(false)));
    }

    // Tests that adding an item with null minOutlier but valid maxOutlier sets min to NaN initially
    @Test
    public void testAdd_nullMinOutlier_setsMinToNaN() {
        BoxAndWhiskerItem item = new BoxAndWhiskerItem(new Double(1.0), new Double(1.0),
                new Double(1.0), new Double(1.0), new Double(1.0),
                new Double(1.0), null, 5.0, new ArrayList<Double>());
        dataset.add(item, ROW_KEY, COL_KEY);
        assertTrue(Double.isNaN(dataset.getRangeLowerBound(false)));
        assertEquals(5.0, dataset.getRangeUpperBound(false), 0.0001);
    }

    // Tests that adding an item with null maxOutlier but valid minOutlier sets max to NaN initially
    @Test
    public void testAdd_nullMaxOutlier_setsMaxToNaN() {
        BoxAndWhiskerItem item = new BoxAndWhiskerItem(new Double(1.0), new Double(1.0),
                new Double(1.0), new Double(1.0), new Double(1.0),
                new Double(1.0), 2.0, null, new ArrayList<Double>());
        dataset.add(item, ROW_KEY, COL_KEY);
        assertEquals(2.0, dataset.getRangeLowerBound(false), 0.0001);
        assertTrue(Double.isNaN(dataset.getRangeUpperBound(false)));
    }

    // Tests getRangeLowerBound and getRangeUpperBound with only one item
    @Test
    public void testGetRangeBounds_singleItem_returnsCorrectBounds() {
        dataset.add(createItem(3.0, 7.0), ROW_KEY, COL_KEY);
        assertEquals(3.0, dataset.getRangeLowerBound(false), 0.0001);
        assertEquals(7.0, dataset.getRangeUpperBound(false), 0.0001);
    }

    // Tests getRangeBounds with multiple items: verifies overall min and max
    @Test
    public void testGetRangeBounds_multipleItems_returnsCorrectBounds() {
        dataset.add(createItem(2.0, 8.0), ROW_KEY, COL_KEY);
        dataset.add(createItem(1.0, 9.0), ROW_KEY2, COL_KEY2);
        assertEquals(1.0, dataset.getRangeLowerBound(false), 0.0001);
        assertEquals(9.0, dataset.getRangeUpperBound(false), 0.0001);
    }

    // Tests getValue (which delegates to getMedianValue) for a normal item
    @Test
    public void testGetValue_normalItem_returnsMedian() {
        BoxAndWhiskerItem item = new BoxAndWhiskerItem(new Double(5.0), new Double(5.0),
                new Double(3.0), new Double(7.0), new Double(2.0),
                new Double(8.0), new Double(1.0), new Double(9.0), new ArrayList<Double>());
        dataset.add(item, ROW_KEY, COL_KEY);
        assertEquals(5.0, dataset.getValue(0, 0));
    }

    // Tests getValue for a non-existent row/column: should return null
    @Test
    public void testGetValue_nonExistentCell_returnsNull() {
        assertNull(dataset.getValue(0, 0));
    }

    // Tests getMeanValue for a normal item
    @Test
    public void testGetMeanValue_normalItem_returnsMean() {
        BoxAndWhiskerItem item = new BoxAndWhiskerItem(new Double(5.0), new Double(10.0),
                new Double(3.0), new Double(7.0), new Double(2.0),
                new Double(8.0), new Double(1.0), new Double(9.0), new ArrayList<Double>());
        dataset.add(item, ROW_KEY, COL_KEY);
        assertEquals(10.0, dataset.getMeanValue(0, 0));
    }

    // Tests getMeanValue for a non-existent cell: returns null
    @Test
    public void testGetMeanValue_nonExistentCell_returnsNull() {
        assertNull(dataset.getMeanValue(ROW_KEY, COL_KEY));
    }

    // Tests getMedianValue for a normal item
    @Test
    public void testGetMedianValue_normalItem_returnsMedian() {
        BoxAndWhiskerItem item = new BoxAndWhiskerItem(new Double(7.0), new Double(7.0),
                new Double(3.0), new Double(7.0), new Double(2.0),
                new Double(8.0), new Double(1.0), new Double(9.0), new ArrayList<Double>());
        dataset.add(item, ROW_KEY, COL_KEY);
        assertEquals(7.0, dataset.getMedianValue(0, 0));
    }

    // Tests getMedianValue by rowKey/columnKey for a normal item
    @Test
    public void testGetMedianValue_byKeys_normalItem_returnsMedian() {
        BoxAndWhiskerItem item = new BoxAndWhiskerItem(new Double(3.0), new Double(3.0),
                new Double(1.0), new Double(5.0), new Double(0.0),
                new Double(6.0), new Double(-1.0), new Double(7.0), new ArrayList<Double>());
        dataset.add(item, ROW_KEY, COL_KEY);
        assertEquals(3.0, dataset.getMedianValue(ROW_KEY, COL_KEY));
    }

    // Tests getRangeBounds for an empty dataset: should return Range(0.0, 0.0) by constructor
    @Test
    public void testGetRangeBounds_emptyDataset_returnsZeroRange() {
        Range r = dataset.getRangeBounds(false);
        assertEquals(0.0, r.getLowerBound(), 0.0001);
        assertEquals(0.0, r.getUpperBound(), 0.0001);
    }

    // Tests that add(List, Comparable, Comparable) works by calling BoxAndWhiskerCalculator
    @Test
    public void testAdd_listParam_addsItem() {
        List<Double> values = new ArrayList<>();
        values.add(1.0);
        values.add(2.0);
        values.add(3.0);
        dataset.add(values, ROW_KEY, COL_KEY);
        // There should be one item now
        assertEquals(1, dataset.getRowCount());
        assertEquals(1, dataset.getColumnCount());
        // Median of {1,2,3} is 2.0
        assertEquals(2.0, dataset.getMedianValue(0, 0));
    }

    // Tests getRowCount and getColumnCount with multiple items
    @Test
    public void testGetRowColumnCount_multipleItems_returnsCorrectCount() {
        dataset.add(createItem(1.0, 2.0), ROW_KEY, COL_KEY);
        dataset.add(createItem(3.0, 4.0), ROW_KEY2, COL_KEY2);
        assertEquals(2, dataset.getRowCount());
        assertEquals(2, dataset.getColumnCount());
    }

    // Tests that adding an item with null rowKey throws IllegalArgumentException (or similar)
    // Since the source code does not show explicit null check, we test that it does not crash unexpectedly
    @Test(expected = IllegalArgumentException.class)
    public void testAdd_nullRowKey_throwsException() {
        BoxAndWhiskerItem item = createItem(1.0, 2.0);
        dataset.add(item, null, COL_KEY);
    }

    // Tests that adding an item with null columnKey throws appropriate exception
    @Test(expected = IllegalArgumentException.class)
    public void testAdd_nullColumnKey_throwsException() {
        BoxAndWhiskerItem item = createItem(1.0, 2.0);
        dataset.add(item, ROW_KEY, null);
    }
}