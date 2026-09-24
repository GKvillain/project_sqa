package org.jfree.data.category;

import java.util.List;
import org.jfree.data.UnknownKeyException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class DefaultIntervalCategoryDatasetTest {

    private DefaultIntervalCategoryDataset dataset;

    @Before
    public void setUp() {
        double[][] starts = new double[][] {{0.1, 0.2, 0.3}, {1.1, 1.2, 1.3}};
        double[][] ends = new double[][] {{0.5, 0.6, 0.7}, {1.5, 1.6, 1.7}};
        this.dataset = new DefaultIntervalCategoryDataset(starts, ends);
    }

    // Tests normal retrieval of series and category counts
    @Test
    public void testGetCount_validData_returnsCorrectDimensions() {
        assertEquals(2, this.dataset.getSeriesCount());
        assertEquals(3, this.dataset.getCategoryCount());
        assertEquals(2, this.dataset.getRowCount());
        assertEquals(3, this.dataset.getColumnCount());
    }

    // Tests empty dataset initialization and dimension methods (Defects4J Chart-16 regression)
    @Test
    public void testEmptyDataset_zeroDimensions_handlesGracefully() {
        DefaultIntervalCategoryDataset empty = new DefaultIntervalCategoryDataset(
                new double[0][0], new double[0][0]
        );
        assertEquals(0, empty.getSeriesCount());
        assertEquals(0, empty.getCategoryCount());
        assertEquals(0, empty.getRowCount());
        assertEquals(0, empty.getColumnCount());
        assertEquals(-1, empty.getSeriesIndex("Series 1"));
        assertEquals(-1, empty.getCategoryIndex("Category 1"));
        assertTrue(empty.getRowKeys().isEmpty());
        assertTrue(empty.getColumnKeys().isEmpty());
    }

    // Tests key lookup methods with valid inputs
    @Test
    public void testKeysAndIndices_validKeys_returnsExpectedValues() {
        assertEquals("Series 1", this.dataset.getSeriesKey(0));
        assertEquals("Series 2", this.dataset.getRowKey(1));
        assertEquals(0, this.dataset.getSeriesIndex("Series 1"));
        assertEquals(1, this.dataset.getRowIndex("Series 2"));
        assertEquals(-1, this.dataset.getSeriesIndex("NonExistent"));

        assertEquals("Category 1", this.dataset.getColumnKey(0));
        assertEquals(0, this.dataset.getCategoryIndex("Category 1"));
        assertEquals(1, this.dataset.getColumnIndex("Category 2"));
        assertEquals(-1, this.dataset.getCategoryIndex("NonExistent"));

        List rowKeys = this.dataset.getRowKeys();
        assertEquals(2, rowKeys.size());
        assertEquals("Series 1", rowKeys.get(0));

        List colKeys = this.dataset.getColumnKeys();
        assertEquals(3, colKeys.size());
        assertEquals("Category 1", colKeys.get(0));
    }

    // Tests start, end, and general values retrieval by index and key
    @Test
    public void testGetValues_validIndicesAndKeys_returnsCorrectNumbers() {
        assertEquals(0.1, this.dataset.getStartValue(0, 0).doubleValue(), 0.000001);
        assertEquals(0.5, this.dataset.getEndValue(0, 0).doubleValue(), 0.000001);
        assertEquals(0.5, this.dataset.getValue(0, 0).doubleValue(), 0.000001);

        assertEquals(0.2, this.dataset.getStartValue("Series 1", "Category 2").doubleValue(), 0.000001);
        assertEquals(0.6, this.dataset.getEndValue("Series 1", "Category 2").doubleValue(), 0.000001);
        assertEquals(0.6, this.dataset.getValue("Series 1", "Category 2").doubleValue(), 0.000001);
    }

    // Tests setting start and end values with valid inputs
    @Test
    public void testSetValues_validInputs_updatesValuesCorrectly() {
        this.dataset.setStartValue(0, "Category 1", new Double(0.99));
        assertEquals(0.99, this.dataset.getStartValue(0, 0).doubleValue(), 0.000001);

        this.dataset.setEndValue(1, "Category 3", new Double(2.99));
        assertEquals(2.99, this.dataset.getEndValue(1, 2).doubleValue(), 0.000001);
    }

    // Tests updating series keys and category keys
    @Test
    public void testSetKeys_validKeys_updatesKeysCorrectly() {
        String[] newSeries = new String[] {"S1", "S2"};
        this.dataset.setSeriesKeys(newSeries);
        assertEquals("S1", this.dataset.getSeriesKey(0));
        assertEquals("S2", this.dataset.getSeriesKey(1));

        String[] newCategories = new String[] {"C1", "C2", "C3"};
        this.dataset.setCategoryKeys(newCategories);
        assertEquals("C1", this.dataset.getColumnKey(0));
        assertEquals("C2", this.dataset.getColumnKey(1));
        assertEquals("C3", this.dataset.getColumnKey(2));
    }

    // Tests constructor with custom series and category keys
    @Test
    public void testConstructor_customKeys_initializedCorrectly() {
        Comparable[] sKeys = new Comparable[] {"A", "B"};
        Comparable[] cKeys = new Comparable[] {"X", "Y"};
        Number[][] starts = new Number[][] {{1, 2}, {3, 4}};
        Number[][] ends = new Number[][] {{2, 3}, {4, 5}};

        DefaultIntervalCategoryDataset d = new DefaultIntervalCategoryDataset(
                sKeys, cKeys, starts, ends
        );
        assertEquals("A", d.getSeriesKey(0));
        assertEquals("X", d.getColumnKey(0));
        assertEquals(1, d.getStartValue(0, 0).intValue());
    }

    // Tests constructor with series length mismatch
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_seriesMismatch_throwsException() {
        Number[][] starts = new Number[][] {{1, 2}, {3, 4}};
        Number[][] ends = new Number[][] {{1, 2}};
        new DefaultIntervalCategoryDataset(starts, ends);
    }

    // Tests constructor with category length mismatch
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_categoryMismatch_throwsException() {
        Number[][] starts = new Number[][] {{1, 2}};
        Number[][] ends = new Number[][] {{1, 2, 3}};
        new DefaultIntervalCategoryDataset(starts, ends);
    }

    // Tests constructor with invalid series key length
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_invalidSeriesKeyLength_throwsException() {
        Comparable[] sKeys = new Comparable[] {"A"};
        Number[][] starts = new Number[][] {{1}, {2}};
        Number[][] ends = new Number[][] {{2}, {3}};
        new DefaultIntervalCategoryDataset(sKeys, null, starts, ends);
    }

    // Tests constructor with invalid category key length
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_invalidCategoryKeyLength_throwsException() {
        Comparable[] cKeys = new Comparable[] {"X"};
        Number[][] starts = new Number[][] {{1, 2}};
        Number[][] ends = new Number[][] {{2, 3}};
        new DefaultIntervalCategoryDataset(null, cKeys, starts, ends);
    }

    // Tests getSeriesKey out of bounds
    @Test(expected = IllegalArgumentException.class)
    public void testGetSeriesKey_outOfBounds_throwsException() {
        this.dataset.getSeriesKey(5);
    }

    // Tests getRowKey negative index
    @Test(expected = IllegalArgumentException.class)
    public void testGetRowKey_negativeIndex_throwsException() {
        this.dataset.getRowKey(-1);
    }

    // Tests getValue with unknown series key
    @Test(expected = UnknownKeyException.class)
    public void testGetValue_unknownSeriesKey_throwsException() {
        this.dataset.getValue("Unknown", "Category 1");
    }

    // Tests getValue with unknown category key
    @Test(expected = UnknownKeyException.class)
    public void testGetValue_unknownCategoryKey_throwsException() {
        this.dataset.getValue("Series 1", "Unknown");
    }

    // Tests getColumnIndex with null argument
    @Test(expected = IllegalArgumentException.class)
    public void testGetColumnIndex_nullArgument_throwsException() {
        this.dataset.getColumnIndex(null);
    }

    // Tests setCategoryKeys with null array
    @Test(expected = IllegalArgumentException.class)
    public void testSetCategoryKeys_nullArray_throwsException() {
        this.dataset.setCategoryKeys(null);
    }

    // Tests setCategoryKeys containing null element
    @Test(expected = IllegalArgumentException.class)
    public void testSetCategoryKeys_nullElement_throwsException() {
        this.dataset.setCategoryKeys(new Comparable[] {"C1", null, "C3"});
    }

    // Tests setStartValue with invalid series index
    @Test(expected = IllegalArgumentException.class)
    public void testSetStartValue_invalidSeriesIndex_throwsException() {
        this.dataset.setStartValue(-1, "Category 1", new Double(1.0));
    }

    // Tests setEndValue with invalid category key
    @Test(expected = IllegalArgumentException.class)
    public void testSetEndValue_invalidCategoryKey_throwsException() {
        this.dataset.setEndValue(0, "InvalidCategory", new Double(1.0));
    }

    // Tests equals method for symmetry, reflexivity, and difference detection
    @Test
    public void testEquals_variousObjects_returnsExpectedResults() {
        assertTrue(this.dataset.equals(this.dataset));
        assertFalse(this.dataset.equals(null));
        assertFalse(this.dataset.equals("Not a dataset"));

        double[][] s1 = new double[][] {{0.1, 0.2, 0.3}, {1.1, 1.2, 1.3}};
        double[][] e1 = new double[][] {{0.5, 0.6, 0.7}, {1.5, 1.6, 1.7}};
        DefaultIntervalCategoryDataset d2 = new DefaultIntervalCategoryDataset(s1, e1);
        assertTrue(this.dataset.equals(d2));

        d2.setStartValue(0, "Category 1", new Double(0.99));
        assertFalse(this.dataset.equals(d2));
    }

    // Tests clone functionality creates an independent copy
    @Test
    public void testClone_validDataset_createsIndependentCopy() throws CloneNotSupportedException {
        DefaultIntervalCategoryDataset clone = (DefaultIntervalCategoryDataset) this.dataset.clone();
        assertNotSame(this.dataset, clone);
        assertEquals(this.dataset, clone);

        clone.setStartValue(0, "Category 1", new Double(99.9));
        assertFalse(this.dataset.equals(clone));
    }
}