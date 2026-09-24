package org.jfree.data.statistics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.jfree.data.Range;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class DefaultBoxAndWhiskerCategoryDatasetTest {

    private static final double EPSILON = 0.000000001;
    private DefaultBoxAndWhiskerCategoryDataset dataset;

    @Before
    public void setUp() {
        this.dataset = new DefaultBoxAndWhiskerCategoryDataset();
    }

    // Tests initial state and default range bounds of empty dataset
    @Test
    public void testConstructor_initialState_returnsDefaultValues() {
        assertEquals(0, this.dataset.getRowCount());
        assertEquals(0, this.dataset.getColumnCount());
        assertTrue(Double.isNaN(this.dataset.getRangeLowerBound(true)));
        assertTrue(Double.isNaN(this.dataset.getRangeUpperBound(true)));
        assertEquals(new Range(0.0, 0.0), this.dataset.getRangeBounds(true));
    }

    // Tests adding an item using List of numbers and verifying statistics
    @Test
    public void testAdd_withList_calculatesStatisticsAndUpdatesBounds() {
        List list = Arrays.asList(new Double(1.0), new Double(2.0), new Double(3.0), new Double(4.0), new Double(5.0));
        this.dataset.add(list, "R1", "C1");

        assertEquals(1, this.dataset.getRowCount());
        assertEquals(1, this.dataset.getColumnCount());
        assertEquals(3.0, this.dataset.getValue("R1", "C1").doubleValue(), EPSILON);
        assertEquals(3.0, this.dataset.getValue(0, 0).doubleValue(), EPSILON);
        assertEquals(3.0, this.dataset.getMeanValue("R1", "C1").doubleValue(), EPSILON);
        assertEquals(3.0, this.dataset.getMedianValue(0, 0).doubleValue(), EPSILON);
        assertEquals(1.5, this.dataset.getQ1Value(0, 0).doubleValue(), EPSILON);
        assertEquals(4.5, this.dataset.getQ3Value(0, 0).doubleValue(), EPSILON);
        assertEquals(1.0, this.dataset.getMinRegularValue(0, 0).doubleValue(), EPSILON);
        assertEquals(5.0, this.dataset.getMaxRegularValue(0, 0).doubleValue(), EPSILON);
        assertEquals(1.0, this.dataset.getMinOutlier(0, 0).doubleValue(), EPSILON);
        assertEquals(5.0, this.dataset.getMaxOutlier(0, 0).doubleValue(), EPSILON);
        assertNotNull(this.dataset.getOutliers(0, 0));
        assertEquals(1.0, this.dataset.getRangeLowerBound(true), EPSILON);
        assertEquals(5.0, this.dataset.getRangeUpperBound(true), EPSILON);
    }

    // Tests adding BoxAndWhiskerItem with null min/max outliers
    @Test
    public void testAdd_itemWithNullOutliers_handlesNaNBoundsGracefully() {
        BoxAndWhiskerItem item = new BoxAndWhiskerItem(
                new Double(10.0), new Double(10.0), new Double(5.0),
                new Double(15.0), new Double(2.0), new Double(18.0),
                null, null, Collections.EMPTY_LIST
        );
        this.dataset.add(item, "R1", "C1");

        assertTrue(Double.isNaN(this.dataset.getRangeLowerBound(true)));
        assertTrue(Double.isNaN(this.dataset.getRangeUpperBound(true)));
        assertNull(this.dataset.getMinOutlier("R1", "C1"));
        assertNull(this.dataset.getMaxOutlier("R1", "C1"));
    }

    // Tests adding multiple items and updating minimum/maximum range bounds (Defects4J Chart-21 regression)
    @Test
    public void testAdd_overwritingMinMaxCell_recalculatesBoundsCorrectly() {
        BoxAndWhiskerItem item1 = new BoxAndWhiskerItem(
                new Double(10.0), new Double(10.0), new Double(5.0),
                new Double(15.0), new Double(1.0), new Double(20.0),
                new Double(1.0), new Double(20.0), Collections.EMPTY_LIST
        );
        BoxAndWhiskerItem item2 = new BoxAndWhiskerItem(
                new Double(10.0), new Double(10.0), new Double(5.0),
                new Double(15.0), new Double(4.0), new Double(12.0),
                new Double(4.0), new Double(12.0), Collections.EMPTY_LIST
        );
        this.dataset.add(item1, "R1", "C1");
        this.dataset.add(item2, "R2", "C2");

        assertEquals(1.0, this.dataset.getRangeLowerBound(true), EPSILON);
        assertEquals(20.0, this.dataset.getRangeUpperBound(true), EPSILON);

        // Replace item1 (which defined min=1.0, max=20.0) with an item having narrower bounds
        BoxAndWhiskerItem item3 = new BoxAndWhiskerItem(
                new Double(10.0), new Double(10.0), new Double(5.0),
                new Double(15.0), new Double(6.0), new Double(8.0),
                new Double(6.0), new Double(8.0), Collections.EMPTY_LIST
        );
        this.dataset.add(item3, "R1", "C1");

        // After updating the cell that held min/max, dataset bounds should reflect the actual remaining bounds
        assertEquals(4.0, this.dataset.getRangeLowerBound(true), EPSILON);
        assertEquals(12.0, this.dataset.getRangeUpperBound(true), EPSILON);
    }

    // Tests updating range bounds with larger max and smaller min values
    @Test
    public void testAdd_subsequentItemsExpandingBounds_updatesMinMaxRange() {
        BoxAndWhiskerItem item1 = new BoxAndWhiskerItem(
                new Double(10.0), new Double(10.0), new Double(8.0),
                new Double(12.0), new Double(5.0), new Double(15.0),
                new Double(5.0), new Double(15.0), Collections.EMPTY_LIST
        );
        this.dataset.add(item1, "R1", "C1");
        assertEquals(5.0, this.dataset.getRangeLowerBound(true), EPSILON);
        assertEquals(15.0, this.dataset.getRangeUpperBound(true), EPSILON);

        BoxAndWhiskerItem item2 = new BoxAndWhiskerItem(
                new Double(10.0), new Double(10.0), new Double(8.0),
                new Double(12.0), new Double(2.0), new Double(25.0),
                new Double(2.0), new Double(25.0), Collections.EMPTY_LIST
        );
        this.dataset.add(item2, "R2", "C1");
        assertEquals(2.0, this.dataset.getRangeLowerBound(true), EPSILON);
        assertEquals(25.0, this.dataset.getRangeUpperBound(true), EPSILON);
    }

    // Tests getters with row and column keys
    @Test
    public void testGetters_byRowAndColumnKey_returnsExpectedValues() {
        List outliers = new ArrayList();
        outliers.add(new Double(0.5));
        BoxAndWhiskerItem item = new BoxAndWhiskerItem(
                new Double(5.0), new Double(5.2), new Double(3.0),
                new Double(7.0), new Double(1.0), new Double(9.0),
                new Double(0.5), new Double(9.5), outliers
        );
        this.dataset.add(item, "RowA", "ColA");

        assertEquals(new Double(5.0), this.dataset.getMeanValue("RowA", "ColA"));
        assertEquals(new Double(5.2), this.dataset.getMedianValue("RowA", "ColA"));
        assertEquals(new Double(3.0), this.dataset.getQ1Value("RowA", "ColA"));
        assertEquals(new Double(7.0), this.dataset.getQ3Value("RowA", "ColA"));
        assertEquals(new Double(1.0), this.dataset.getMinRegularValue("RowA", "ColA"));
        assertEquals(new Double(9.0), this.dataset.getMaxRegularValue("RowA", "ColA"));
        assertEquals(new Double(0.5), this.dataset.getMinOutlier("RowA", "ColA"));
        assertEquals(new Double(9.5), this.dataset.getMaxOutlier("RowA", "ColA"));
        assertEquals(outliers, this.dataset.getOutliers("RowA", "ColA"));
        assertEquals(item, this.dataset.getItem(0, 0));
    }

    // Tests getters returning null for non-existing or null cell items
    @Test
    public void testGetters_nonExistingItem_returnsNull() {
        assertNull(this.dataset.getMeanValue("R_NONE", "C_NONE"));
        assertNull(this.dataset.getMedianValue("R_NONE", "C_NONE"));
        assertNull(this.dataset.getQ1Value("R_NONE", "C_NONE"));
        assertNull(this.dataset.getQ3Value("R_NONE", "C_NONE"));
        assertNull(this.dataset.getMinRegularValue("R_NONE", "C_NONE"));
        assertNull(this.dataset.getMaxRegularValue("R_NONE", "C_NONE"));
        assertNull(this.dataset.getMinOutlier("R_NONE", "C_NONE"));
        assertNull(this.dataset.getMaxOutlier("R_NONE", "C_NONE"));
        assertNull(this.dataset.getOutliers("R_NONE", "C_NONE"));
    }

    // Tests row and column key/index lookup methods
    @Test
    public void testKeyAndIndexLookups_returnsCorrectKeysAndIndices() {
        this.dataset.add(Arrays.asList(new Double(1.0)), "R1", "C1");
        this.dataset.add(Arrays.asList(new Double(2.0)), "R2", "C2");

        assertEquals(0, this.dataset.getRowIndex("R1"));
        assertEquals(1, this.dataset.getRowIndex("R2"));
        assertEquals(-1, this.dataset.getRowIndex("R3"));

        assertEquals(0, this.dataset.getColumnIndex("C1"));
        assertEquals(1, this.dataset.getColumnIndex("C2"));
        assertEquals(-1, this.dataset.getColumnIndex("C3"));

        assertEquals("R1", this.dataset.getRowKey(0));
        assertEquals("R2", this.dataset.getRowKey(1));

        assertEquals("C1", this.dataset.getColumnKey(0));
        assertEquals("C2", this.dataset.getColumnKey(1));

        assertEquals(Arrays.asList("R1", "R2"), this.dataset.getRowKeys());
        assertEquals(Arrays.asList("C1", "C2"), this.dataset.getColumnKeys());
    }

    // Tests equals method with same, identical, different, and null objects
    @Test
    public void testEquals_variousObjects_returnsExpectedEquality() {
        assertTrue(this.dataset.equals(this.dataset));
        assertFalse(this.dataset.equals(null));
        assertFalse(this.dataset.equals("Not a Dataset"));

        DefaultBoxAndWhiskerCategoryDataset dataset2 = new DefaultBoxAndWhiskerCategoryDataset();
        assertTrue(this.dataset.equals(dataset2));

        this.dataset.add(Arrays.asList(new Double(1.0)), "R1", "C1");
        assertFalse(this.dataset.equals(dataset2));

        dataset2.add(Arrays.asList(new Double(1.0)), "R1", "C1");
        assertTrue(this.dataset.equals(dataset2));
    }

    // Tests clone method creating independent copy
    @Test
    public void testClone_clonedDataset_createsIndependentCopy() throws CloneNotSupportedException {
        this.dataset.add(Arrays.asList(new Double(1.0), new Double(2.0)), "R1", "C1");
        DefaultBoxAndWhiskerCategoryDataset clone = (DefaultBoxAndWhiskerCategoryDataset) this.dataset.clone();

        assertTrue(this.dataset.equals(clone));
        assertEquals(this.dataset.getRowCount(), clone.getRowCount());
        assertEquals(this.dataset.getColumnCount(), clone.getColumnCount());

        clone.add(Arrays.asList(new Double(3.0)), "R2", "C2");
        assertFalse(this.dataset.equals(clone));
    }
}