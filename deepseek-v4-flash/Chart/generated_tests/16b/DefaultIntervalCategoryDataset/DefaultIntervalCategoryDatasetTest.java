package org.jfree.data.category;

import static org.junit.Assert.*;
import org.junit.Test;
import org.jfree.data.UnknownKeyException;

public class DefaultIntervalCategoryDatasetTest {

    private DefaultIntervalCategoryDataset createExplicitDataset() {
        return new DefaultIntervalCategoryDataset(
                new Comparable[] {"S1", "S2"},
                new Comparable[] {"C1", "C2"},
                new Number[][] {{1.0, 2.0}, {3.0, 4.0}},
                new Number[][] {{5.0, 6.0}, {7.0, 8.0}});
    }

    // Tests normal construction with explicit series/category keys.
    @Test
    public void testConstructor_explicitKeys_gettersReturnExpectedValues() {
        DefaultIntervalCategoryDataset d = createExplicitDataset();

        assertEquals(2, d.getSeriesCount());
        assertEquals(2, d.getRowCount());
        assertEquals(2, d.getColumnCount());
        assertEquals(2, d.getCategoryCount());

        assertEquals("S1", d.getRowKey(0));
        assertEquals("S2", d.getRowKey(1));
        assertEquals("C1", d.getColumnKey(0));
        assertEquals("C2", d.getColumnKey(1));

        assertEquals(1, d.getRowIndex("S2"));
        assertEquals(1, d.getColumnIndex("C2"));

        assertEquals(1.0, d.getStartValue(0, 0).doubleValue(), 0.0);
        assertEquals(8.0, d.getEndValue(1, 1).doubleValue(), 0.0);
        assertEquals(5.0, d.getValue(0, 0).doubleValue(), 0.0);
        assertEquals(5.0, d.getValue("S1", "C1").doubleValue(), 0.0);
    }

    // Tests generated series and category keys.
    @Test
    public void testConstructor_generatedKeys_rowAndColumnCountsAreCorrect() {
        DefaultIntervalCategoryDataset d = new DefaultIntervalCategoryDataset(
                new Number[][] {{1.0}, {2.0}},
                new Number[][] {{3.0}, {4.0}});

        assertEquals(2, d.getSeriesCount());
        assertEquals(2, d.getRowCount());
        assertEquals(1, d.getColumnCount());
        assertEquals(1, d.getCategoryCount());
    }

    // Tests updating a start value.
    @Test
    public void testSetStartValue_updatesStartValue() {
        DefaultIntervalCategoryDataset d = createExplicitDataset();

        d.setStartValue(1, "C2", Double.valueOf(10.0));

        assertEquals(10.0, d.getStartValue(1, 1).doubleValue(), 0.0);
        assertEquals(8.0, d.getEndValue(1, 1).doubleValue(), 0.0);
    }

    // Tests updating an end value.
    @Test
    public void testSetEndValue_updatesEndValue() {
        DefaultIntervalCategoryDataset d = createExplicitDataset();

        d.setEndValue(0, "C1", Double.valueOf(50.0));

        assertEquals(50.0, d.getEndValue(0, 0).doubleValue(), 0.0);
        assertEquals(1.0, d.getStartValue(0, 0).doubleValue(), 0.0);
    }

    // Tests replacing series keys.
    @Test
    public void testSetSeriesKeys_updatesSeriesKeys() {
        DefaultIntervalCategoryDataset d = createExplicitDataset();

        d.setSeriesKeys(new Comparable[] {"X", "Y"});

        assertEquals("X", d.getRowKey(0));
        assertEquals("Y", d.getRowKey(1));
    }

    // Tests replacing category keys.
    @Test
    public void testSetCategoryKeys_updatesCategoryKeys() {
        DefaultIntervalCategoryDataset d = createExplicitDataset();

        d.setCategoryKeys(new Comparable[] {"X", "Y"});

        assertEquals("X", d.getColumnKey(0));
        assertEquals("Y", d.getColumnKey(1));
    }

    // Tests unknown series key path.
    @Test(expected = UnknownKeyException.class)
    public void testGetValue_unknownSeries_throwsUnknownKeyException() {
        createExplicitDataset().getValue("UNKNOWN", "C1");
    }

    // Tests unknown category key path.
    @Test(expected = UnknownKeyException.class)
    public void testGetStartValue_unknownCategory_throwsUnknownKeyException() {
        createExplicitDataset().getStartValue("S1", "UNKNOWN");
    }

    // Tests invalid series index boundary.
    @Test(expected = IllegalArgumentException.class)
    public void testGetStartValue_invalidSeriesIndex_throwsIllegalArgumentException() {
        createExplicitDataset().getStartValue(2, 0);
    }

    // Tests invalid category index boundary.
    @Test(expected = IllegalArgumentException.class)
    public void testGetEndValue_invalidCategoryIndex_throwsIllegalArgumentException() {
        createExplicitDataset().getEndValue(0, 2);
    }

    // Tests constructor validation for mismatched series counts.
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_mismatchedSeriesCount_throwsIllegalArgumentException() {
        new DefaultIntervalCategoryDataset(
                new Number[][] {{1.0, 2.0}},
                new Number[][] {{3.0, 4.0}, {5.0, 6.0}});
    }

    // Tests constructor validation for mismatched category counts.
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_mismatchedCategoryCount_throwsIllegalArgumentException() {
        new DefaultIntervalCategoryDataset(
                new Number[][] {{1.0, 2.0}},
                new Number[][] {{3.0}});
    }

    // Tests null series keys rejection.
    @Test(expected = IllegalArgumentException.class)
    public void testSetSeriesKeys_null_throwsIllegalArgumentException() {
        createExplicitDataset().setSeriesKeys(null);
    }

    // Tests null category keys rejection.
    @Test(expected = IllegalArgumentException.class)
    public void testSetCategoryKeys_null_throwsIllegalArgumentException() {
        createExplicitDataset().setCategoryKeys(null);
    }

    // Tests null category element rejection.
    @Test(expected = IllegalArgumentException.class)
    public void testSetCategoryKeys_nullElement_throwsIllegalArgumentException() {
        createExplicitDataset().setCategoryKeys(new Comparable[] {"C1", null});
    }

    // Tests wrong series key count rejection.
    @Test(expected = IllegalArgumentException.class)
    public void testSetSeriesKeys_wrongLength_throwsIllegalArgumentException() {
        createExplicitDataset().setSeriesKeys(new Comparable[] {"Only"});
    }

    // Tests empty dataset counts; this should not throw NullPointerException.
    @Test
    public void testEmptyDataset_getRowAndColumnCounts_areZero() {
        DefaultIntervalCategoryDataset d = new DefaultIntervalCategoryDataset(
                new Number[0][0], new Number[0][0]);

        assertEquals(0, d.getSeriesCount());
        assertEquals(0, d.getCategoryCount());
        assertEquals(0, d.getRowCount());
        assertEquals(0, d.getColumnCount());
    }

    // Tests a dataset with one series and zero categories.
    @Test
    public void testDatasetWithOneSeriesAndNoCategories_getCountsAreCorrect() {
        DefaultIntervalCategoryDataset d = new DefaultIntervalCategoryDataset(
                new Number[][] { new Number[0] },
                new Number[][] { new Number[0] });

        assertEquals(1, d.getSeriesCount());
        assertEquals(1, d.getRowCount());
        assertEquals(0, d.getCategoryCount());
        assertEquals(0, d.getColumnCount());
    }

    // Tests equality for identical and different data.
    @Test
    public void testEquals_comparesDataValues() {
        DefaultIntervalCategoryDataset d1 = createExplicitDataset();
        DefaultIntervalCategoryDataset d2 = createExplicitDataset();

        assertTrue(d1.equals(d2));

        d2.setStartValue(0, "C1", Double.valueOf(99.0));
        assertFalse(d1.equals(d2));
    }

    // Tests clone returns an independent copy.
    @Test
    public void testClone_returnsIndependentCopy() throws Exception {
        DefaultIntervalCategoryDataset original = createExplicitDataset();
        DefaultIntervalCategoryDataset copy = (DefaultIntervalCategoryDataset) original.clone();

        assertEquals(original, copy);
        assertNotSame(original, copy);

        original.setStartValue(0, "C1", Double.valueOf(99.0));

        assertEquals(1.0, copy.getStartValue(0, 0).doubleValue(), 0.0);
    }
}