package org.jfree.data;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link DefaultKeyedValues2D}.
 */
public class DefaultKeyedValues2DTest {

    private DefaultKeyedValues2D data;

    @Before
    public void setUp() {
        this.data = new DefaultKeyedValues2D();
    }

    // Tests initial state of empty DefaultKeyedValues2D
    @Test
    public void testConstructor_default_isEmpty() {
        assertEquals(0, this.data.getRowCount());
        assertEquals(0, this.data.getColumnCount());
        assertTrue(this.data.getRowKeys().isEmpty());
        assertTrue(this.data.getColumnKeys().isEmpty());
    }

    // Tests adding and retrieving values by index
    @Test
    public void testGetValue_byIndex_returnsCorrectValue() {
        this.data.addValue(Double.valueOf(1.0), "R1", "C1");
        this.data.addValue(Double.valueOf(2.0), "R1", "C2");
        this.data.addValue(Double.valueOf(3.0), "R2", "C1");

        assertEquals(2, this.data.getRowCount());
        assertEquals(2, this.data.getColumnCount());
        assertEquals(Double.valueOf(1.0), this.data.getValue(0, 0));
        assertEquals(Double.valueOf(2.0), this.data.getValue(0, 1));
        assertEquals(Double.valueOf(3.0), this.data.getValue(1, 0));
        assertNull(this.data.getValue(1, 1));
    }

    // Tests retrieving values by comparable keys
    @Test
    public void testGetValue_byKeys_returnsCorrectValue() {
        this.data.setValue(Double.valueOf(10.0), "R1", "C1");
        this.data.setValue(Double.valueOf(20.0), "R2", "C2");

        assertEquals(Double.valueOf(10.0), this.data.getValue("R1", "C1"));
        assertNull(this.data.getValue("R1", "C2"));
        assertEquals(Double.valueOf(20.0), this.data.getValue("R2", "C2"));
    }

    // Tests getValue with null rowKey
    @Test(expected = IllegalArgumentException.class)
    public void testGetValue_nullRowKey_throwsException() {
        this.data.addValue(Double.valueOf(1.0), "R1", "C1");
        this.data.getValue(null, "C1");
    }

    // Tests getValue with null columnKey
    @Test(expected = IllegalArgumentException.class)
    public void testGetValue_nullColumnKey_throwsException() {
        this.data.addValue(Double.valueOf(1.0), "R1", "C1");
        this.data.getValue("R1", null);
    }

    // Tests getValue with unknown column key
    @Test(expected = UnknownKeyException.class)
    public void testGetValue_unknownColumnKey_throwsException() {
        this.data.addValue(Double.valueOf(1.0), "R1", "C1");
        this.data.getValue("R1", "UnknownCol");
    }

    // Tests getValue with unknown row key
    @Test(expected = UnknownKeyException.class)
    public void testGetValue_unknownRowKey_throwsException() {
        this.data.addValue(Double.valueOf(1.0), "R1", "C1");
        this.data.getValue("UnknownRow", "C1");
    }

    // Tests getRowIndex with null key
    @Test(expected = IllegalArgumentException.class)
    public void testGetRowIndex_nullKey_throwsException() {
        this.data.getRowIndex(null);
    }

    // Tests getColumnIndex with null key
    @Test(expected = IllegalArgumentException.class)
    public void testGetColumnIndex_nullKey_throwsException() {
        this.data.getColumnIndex(null);
    }

    // Tests sorted row keys constructor and behavior
    @Test
    public void testSortedRowKeys_addOutOfOrder_keepsSorted() {
        DefaultKeyedValues2D sortedData = new DefaultKeyedValues2D(true);
        sortedData.setValue(Double.valueOf(3.0), "R3", "C1");
        sortedData.setValue(Double.valueOf(1.0), "R1", "C1");
        sortedData.setValue(Double.valueOf(2.0), "R2", "C1");

        assertEquals("R1", sortedData.getRowKey(0));
        assertEquals("R2", sortedData.getRowKey(1));
        assertEquals("R3", sortedData.getRowKey(2));
        assertEquals(0, sortedData.getRowIndex("R1"));
        assertEquals(1, sortedData.getRowIndex("R2"));
        assertEquals(2, sortedData.getRowIndex("R3"));
    }

    // Tests removing a value and verifying row/column removal when all values are null
    @Test
    public void testRemoveValue_onlyValueInTable_removesRowAndColumn() {
        this.data.setValue(Double.valueOf(1.0), "R1", "C1");
        assertEquals(1, this.data.getRowCount());
        assertEquals(1, this.data.getColumnCount());

        this.data.removeValue("R1", "C1");
        assertEquals(0, this.data.getRowCount());
        assertEquals(0, this.data.getColumnCount());
    }

    // Tests removing a value when other values exist in the same row/column
    @Test
    public void testRemoveValue_sharedRowAndColumn_updatesCorrectly() {
        this.data.setValue(Double.valueOf(1.0), "R1", "C1");
        this.data.setValue(Double.valueOf(2.0), "R1", "C2");
        this.data.setValue(Double.valueOf(3.0), "R2", "C1");

        this.data.removeValue("R1", "C1");

        assertEquals(2, this.data.getRowCount());
        assertEquals(2, this.data.getColumnCount());
        assertNull(this.data.getValue("R1", "C1"));
        assertEquals(Double.valueOf(2.0), this.data.getValue("R1", "C2"));
        assertEquals(Double.valueOf(3.0), this.data.getValue("R2", "C1"));
    }

    // Tests removeRow by index
    @Test
    public void testRemoveRow_byIndex_removesRow() {
        this.data.setValue(Double.valueOf(1.0), "R1", "C1");
        this.data.setValue(Double.valueOf(2.0), "R2", "C1");

        this.data.removeRow(0);

        assertEquals(1, this.data.getRowCount());
        assertEquals("R2", this.data.getRowKey(0));
    }

    // Tests removeRow by key
    @Test
    public void testRemoveRow_byKey_removesRow() {
        this.data.setValue(Double.valueOf(1.0), "R1", "C1");
        this.data.setValue(Double.valueOf(2.0), "R2", "C1");

        this.data.removeRow("R1");

        assertEquals(1, this.data.getRowCount());
        assertEquals("R2", this.data.getRowKey(0));
    }

    // Tests removeColumn by index
    @Test
    public void testRemoveColumn_byIndex_removesColumn() {
        this.data.setValue(Double.valueOf(1.0), "R1", "C1");
        this.data.setValue(Double.valueOf(2.0), "R1", "C2");

        this.data.removeColumn(0);

        assertEquals(1, this.data.getColumnCount());
        assertEquals("C2", this.data.getColumnKey(0));
    }

    // Tests removeColumn by key
    @Test
    public void testRemoveColumn_byKey_removesColumn() {
        this.data.setValue(Double.valueOf(1.0), "R1", "C1");
        this.data.setValue(Double.valueOf(2.0), "R1", "C2");

        this.data.removeColumn("C1");

        assertEquals(1, this.data.getColumnCount());
        assertEquals("C2", this.data.getColumnKey(0));
        assertNull(this.data.getValue("R1", 0));
    }

    // Tests clear method
    @Test
    public void testClear_nonEmptyData_clearsAll() {
        this.data.setValue(Double.valueOf(1.0), "R1", "C1");
        this.data.setValue(Double.valueOf(2.0), "R2", "C2");

        this.data.clear();

        assertEquals(0, this.data.getRowCount());
        assertEquals(0, this.data.getColumnCount());
        assertEquals(0, this.data.getRowKeys().size());
        assertEquals(0, this.data.getColumnKeys().size());
    }

    // Tests equals and hashCode methods
    @Test
    public void testEqualsAndHashCode_sameValues_equalAndSameHashCode() {
        DefaultKeyedValues2D d1 = new DefaultKeyedValues2D();
        d1.setValue(Double.valueOf(1.0), "R1", "C1");
        d1.setValue(null, "R1", "C2");

        DefaultKeyedValues2D d2 = new DefaultKeyedValues2D();
        d2.setValue(Double.valueOf(1.0), "R1", "C1");
        d2.setValue(null, "R1", "C2");

        assertTrue(d1.equals(d1));
        assertTrue(d1.equals(d2));
        assertTrue(d2.equals(d1));
        assertEquals(d1.hashCode(), d2.hashCode());

        assertFalse(d1.equals(null));
        assertFalse(d1.equals("Not a KeyedValues2D"));

        d2.setValue(Double.valueOf(2.0), "R1", "C2");
        assertFalse(d1.equals(d2));
    }

    // Tests clone method deep copying rows
    @Test
    public void testClone_independentCopy_modificationsDoNotAffectOriginal() throws CloneNotSupportedException {
        this.data.setValue(Double.valueOf(1.0), "R1", "C1");

        DefaultKeyedValues2D clone = (DefaultKeyedValues2D) this.data.clone();

        assertNotSame(this.data, clone);
        assertTrue(this.data.equals(clone));

        clone.setValue(Double.valueOf(2.0), "R1", "C1");
        assertFalse(this.data.equals(clone));
        assertEquals(Double.valueOf(1.0), this.data.getValue("R1", "C1"));
        assertEquals(Double.valueOf(2.0), clone.getValue("R1", "C1"));
    }

    // Tests unmodifiable getRowKeys and getColumnKeys
    @Test(expected = UnsupportedOperationException.class)
    public void testGetRowKeys_unmodifiableList_throwsExceptionOnModification() {
        this.data.setValue(Double.valueOf(1.0), "R1", "C1");
        List rowKeys = this.data.getRowKeys();
        rowKeys.add("R2");
    }
}