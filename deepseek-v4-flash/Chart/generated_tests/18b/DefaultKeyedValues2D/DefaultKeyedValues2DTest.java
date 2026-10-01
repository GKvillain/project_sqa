package org.jfree.data;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class DefaultKeyedValues2DTest {

    private DefaultKeyedValues2D data;

    @Before
    public void setUp() {
        data = new DefaultKeyedValues2D();
    }

    // Tests normal add and get by index
    @Test
    public void testAddValue_shouldAddValueToCorrectCell() {
        data.addValue(1.0, "Row1", "Col1");
        assertEquals(1.0, data.getValue(0, 0).doubleValue(), 0.0001);
    }

    // Tests getValue by keys returns correct value
    @Test
    public void testGetValue_byKeys_shouldReturnCorrectValue() {
        data.addValue(2.5, "Row1", "Col1");
        assertEquals(2.5, data.getValue("Row1", "Col1").doubleValue(), 0.0001);
    }

    // Tests getValue by unknown row key throws exception
    @Test(expected = UnknownKeyException.class)
    public void testGetValue_unknownRowKey_throwsUnknownKeyException() {
        data.addValue(1.0, "Row1", "Col1");
        data.getValue("Row2", "Col1");
    }

    // Tests getValue by unknown column key throws exception
    @Test(expected = UnknownKeyException.class)
    public void testGetValue_unknownColumnKey_throwsUnknownKeyException() {
        data.addValue(1.0, "Row1", "Col1");
        data.getValue("Row1", "Col2");
    }

    // Tests row count after adding rows
    @Test
    public void testGetRowCount_afterAddingRows_shouldReturnCorrectCount() {
        data.addValue(1.0, "R1", "C1");
        data.addValue(2.0, "R2", "C1");
        assertEquals(2, data.getRowCount());
    }

    // Tests column count after adding columns
    @Test
    public void testGetColumnCount_afterAddingColumns_shouldReturnCorrectCount() {
        data.addValue(1.0, "R1", "C1");
        data.addValue(2.0, "R1", "C2");
        assertEquals(2, data.getColumnCount());
    }

    // Tests removeValue that results in row and column removal when all null
    @Test
    public void testRemoveValue_withSingleCell_shouldRemoveRowAndColumn() {
        data.addValue(1.0, "R1", "C1");
        data.removeValue("R1", "C1");
        assertEquals(0, data.getRowCount());
        assertEquals(0, data.getColumnCount());
    }

    // Tests removeValue that removes only one cell but leaves other cells
    @Test
    public void testRemoveValue_withOtherValues_shouldNotRemoveRowOrColumn() {
        DefaultKeyedValues2D d = new DefaultKeyedValues2D();
        d.addValue(1.0, "R1", "C1");
        d.addValue(2.0, "R2", "C1");
        d.addValue(3.0, "R1", "C2");
        // remove R1,C1
        d.removeValue("R1", "C1");
        // column C1 still has R2's value, so column stays
        assertEquals(2, d.getRowCount());
        assertEquals(2, d.getColumnCount()); // C1 and C2
        // check value R2,C1 is still 2.0
        assertEquals(2.0, d.getValue("R2", "C1").doubleValue(), 0.0001);
        // R1,C1 should be null (column exists but no entry for that row)
        assertNull(d.getValue("R1", "C1"));
    }

    // Tests removeColumn removes the specified column
    @Test
    public void testRemoveColumn_removeColumn_shouldRemoveColumnAndValues() {
        data.addValue(1.0, "R1", "C1");
        data.addValue(2.0, "R1", "C2");
        data.addValue(3.0, "R2", "C1");
        data.removeColumn("C1");
        assertEquals(1, data.getColumnCount()); // only C2 left
        assertFalse(data.getColumnKeys().contains("C1"));
        assertEquals(2.0, data.getValue("R1", "C2").doubleValue(), 0.0001);
        // row R2 still exists (no values)
        assertEquals(2, data.getRowCount());
    }

    // Tests removeColumn(int) works correctly
    @Test
    public void testRemoveColumn_byIndex_shouldRemoveColumn() {
        data.addValue(1.0, "R1", "C1");
        data.addValue(2.0, "R1", "C2");
        data.removeColumn(0);
        assertEquals(1, data.getColumnCount());
        assertTrue(data.getColumnKeys().contains("C2"));
    }

    // Tests removeRow removes row
    @Test
    public void testRemoveRow_removeRow_shouldRemoveRow() {
        data.addValue(1.0, "R1", "C1");
        data.addValue(2.0, "R2", "C1");
        data.removeRow("R1");
        assertEquals(1, data.getRowCount());
        assertEquals("R2", data.getRowKey(0));
    }

    // Tests removeRow by index
    @Test
    public void testRemoveRow_byIndex_shouldRemoveRow() {
        data.addValue(1.0, "R1", "C1");
        data.addValue(2.0, "R2", "C1");
        data.removeRow(0);
        assertEquals(1, data.getRowCount());
        assertEquals("R2", data.getRowKey(0));
    }

    // Tests clear all data
    @Test
    public void testClear_shouldRemoveAll() {
        data.addValue(1.0, "R1", "C1");
        data.addValue(2.0, "R2", "C2");
        data.clear();
        assertEquals(0, data.getRowCount());
        assertEquals(0, data.getColumnCount());
    }

    // Tests sortRowKeys constructor: inserted in sorted order
    @Test
    public void testSetValue_withSortRowKeys_shouldInsertInSortedOrder() {
        DefaultKeyedValues2D sorted = new DefaultKeyedValues2D(true);
        sorted.addValue(1.0, "Z", "C");
        sorted.addValue(2.0, "A", "C");
        sorted.addValue(3.0, "M", "C");
        assertEquals("A", sorted.getRowKey(0));
        assertEquals("M", sorted.getRowKey(1));
        assertEquals("Z", sorted.getRowKey(2));
    }

    // Tests clone produces equal object
    @Test
    public void testClone_shouldProduceEqualObject() throws CloneNotSupportedException {
        data.addValue(1.0, "R1", "C1");
        data.addValue(2.0, "R1", "C2");
        DefaultKeyedValues2D cloned = (DefaultKeyedValues2D) data.clone();
        assertTrue(data.equals(cloned));
        // modify original, cloned should not change
        data.addValue(3.0, "R2", "C1");
        assertFalse(data.equals(cloned));
    }

    // Tests equals same object returns true
    @Test
    public void testEquals_sameObject_shouldReturnTrue() {
        assertTrue(data.equals(data));
    }

    // Tests equals null returns false
    @Test
    public void testEquals_null_shouldReturnFalse() {
        assertFalse(data.equals(null));
    }

    // Tests equals different row keys
    @Test
    public void testEquals_differentRowKeys_shouldReturnFalse() {
        data.addValue(1.0, "R1", "C1");
        DefaultKeyedValues2D other = new DefaultKeyedValues2D();
        other.addValue(2.0, "R2", "C1");
        assertFalse(data.equals(other));
    }

    // Tests getRowCount and getColumnCount for empty
    @Test
    public void testGetRowCount_empty_shouldBeZero() {
        assertEquals(0, data.getRowCount());
        assertEquals(0, data.getColumnCount());
    }

    // Tests getValue by index returns null when row has no value for column
    @Test
    public void testGetValue_byIndex_returnsNullIfNoValue() {
        DefaultKeyedValues2D d = new DefaultKeyedValues2D();
        d.addValue(1.0, "R1", "C1");
        d.addValue(2.0, "R2", "C2");
        // R1 does not have C2, R2 does not have C1
        assertNull(d.getValue(0, 1));
        assertNull(d.getValue(1, 0));
    }

    // ====================== NEW TEST CASES ======================

    // Tests getRowKey with invalid index throws exception
    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetRowKey_invalidNegativeIndex_throwsException() {
        data.addValue(1.0, "R1", "C1");
        data.getRowKey(-1);
    }

    // Tests getRowKey with out-of-bound index throws exception
    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetRowKey_invalidTooHighIndex_throwsException() {
        data.addValue(1.0, "R1", "C1");
        data.getRowKey(5);
    }

    // Tests getColumnKey with invalid index throws exception
    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetColumnKey_invalidNegativeIndex_throwsException() {
        data.addValue(1.0, "R1", "C1");
        data.getColumnKey(-1);
    }

    // Tests getColumnKey with out-of-bound index throws exception
    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetColumnKey_invalidTooHighIndex_throwsException() {
        data.addValue(1.0, "R1", "C1");
        data.getColumnKey(5);
    }

    // Tests getRowIndex returns correct index for existing row
    @Test
    public void testGetRowIndex_existingRow_shouldReturnCorrectIndex() {
        data.addValue(1.0, "R1", "C1");
        data.addValue(2.0, "R2", "C1");
        assertEquals(0, data.getRowIndex("R1"));
        assertEquals(1, data.getRowIndex("R2"));
    }

    // Tests getRowIndex for non-existent row throws exception
    @Test(expected = UnknownKeyException.class)
    public void testGetRowIndex_unknownRow_throwsException() {
        data.addValue(1.0, "R1", "C1");
        data.getRowIndex("R3");
    }

    // Tests getColumnIndex returns correct index for existing column
    @Test
    public void testGetColumnIndex_existingColumn_shouldReturnCorrectIndex() {
        data.addValue(1.0, "R1", "C1");
        data.addValue(2.0, "R1", "C2");
        assertEquals(0, data.getColumnIndex("C1"));
        assertEquals(1, data.getColumnIndex("C2"));
    }

    // Tests getColumnIndex for non-existent column throws exception
    @Test(expected = UnknownKeyException.class)
    public void testGetColumnIndex_unknownColumn_throwsException() {
        data.addValue(1.0, "R1", "C1");
        data.getColumnIndex("C3");
    }

    // Tests getRowKeys returns all row keys
    @Test
    public void testGetRowKeys_shouldReturnAllKeys() {
        data.addValue(1.0, "R1", "C1");
        data.addValue(2.0, "R2", "C1");
        assertEquals(2, data.getRowKeys().size());
        assertTrue(data.getRowKeys().contains("R1"));
        assertTrue(data.getRowKeys().contains("R2"));
    }

    // Tests getColumnKeys returns all column keys
    @Test
    public void testGetColumnKeys_shouldReturnAllKeys() {
        data.addValue(1.0, "R1", "C1");
        data.addValue(2.0, "R1", "C2");
        assertEquals(2, data.getColumnKeys().size());
        assertTrue(data.getColumnKeys().contains("C1"));
        assertTrue(data.getColumnKeys().contains("C2"));
    }

    // Tests getValue by index returns correct value
    @Test
    public void testGetValue_byIndex_shouldReturnCorrectValue() {
        data.addValue(1.0, "R1", "C1");
        data.addValue(2.0, "R2", "C2");
        assertEquals(1.0, data.getValue(0, 0).doubleValue(), 0.0001);
        assertEquals(2.0, data.getValue(1, 1).doubleValue(), 0.0001);
    }

    // Tests addValue with null value should work
    @Test
    public void testAddValue_withNullValue_shouldAllow() {
        data.addValue(null, "R1", "C1");
        assertNull(data.getValue("R1", "C1"));
        assertEquals(1, data.getRowCount());
        assertEquals(1, data.getColumnCount());
    }

    // Tests addValue with duplicate row and column should update value
    @Test
    public void testAddValue_duplicateRowColumn_shouldUpdateValue() {
        data.addValue(1.0, "R1", "C1");
        data.addValue(2.0, "R1", "C1");
        assertEquals(2.0, data.getValue("R1", "C1").doubleValue(), 0.0001);
        assertEquals(1, data.getRowCount());
        assertEquals(1, data.getColumnCount());
    }

    // Tests removeValue with non-existent row throws exception
    @Test(expected = UnknownKeyException.class)
    public void testRemoveValue_unknownRow_throwsException() {
        data.addValue(1.0, "R1", "C1");
        data.removeValue("R2", "C1");
    }

    // Tests removeValue with non-existent column throws exception
    @Test(expected = UnknownKeyException.class)
    public void testRemoveValue_unknownColumn_throwsException() {
        data.addValue(1.0, "R1", "C1");
        data.removeValue("R1", "C2");
    }

    // Tests removeColumn with non-existent key throws exception
    @Test(expected = UnknownKeyException.class)
    public void testRemoveColumn_unknownKey_throwsException() {
        data.addValue(1.0, "R1", "C1");
        data.removeColumn("C2");
    }

    // Tests removeColumn by index with out-of-bound throws exception
    @Test(expected = IndexOutOfBoundsException.class)
    public void testRemoveColumn_byInvalidIndex_throwsException() {
        data.addValue(1.0, "R1", "C1");
        data.removeColumn(5);
    }

    // Tests removeRow with non-existent key throws exception
    @Test(expected = UnknownKeyException.class)
    public void testRemoveRow_unknownKey_throwsException() {
        data.addValue(1.0, "R1", "C1");
        data.removeRow("R2");
    }

    // Tests removeRow by index with out-of-bound throws exception
    @Test(expected = IndexOutOfBoundsException.class)
    public void testRemoveRow_byInvalidIndex_throwsException() {
        data.addValue(1.0, "R1", "C1");
        data.removeRow(5);
    }

    // Tests equals with different column keys
    @Test
    public void testEquals_differentColumnKeys_shouldReturnFalse() {
        data.addValue(1.0, "R1", "C1");
        DefaultKeyedValues2D other = new DefaultKeyedValues2D();
        other.addValue(1.0, "R1", "C2");
        assertFalse(data.equals(other));
    }

    // Tests equals with different values
    @Test
    public void testEquals_differentValues_shouldReturnFalse() {
        data.addValue(1.0, "R1", "C1");
        DefaultKeyedValues2D other = new DefaultKeyedValues2D();
        other.addValue(2.0, "R1", "C1");
        assertFalse(data.equals(other));
    }

    // Tests equals with same content returns true
    @Test
    public void testEquals_sameContent_shouldReturnTrue() {
        data.addValue(1.0, "R1", "C1");
        data.addValue(2.0, "R2", "C2");
        DefaultKeyedValues2D other = new DefaultKeyedValues2D();
        other.addValue(1.0, "R1", "C1");
        other.addValue(2.0, "R2", "C2");
        assertTrue(data.equals(other));
    }

    // Tests hashCode consistency with equals
    @Test
    public void testHashCode_equalObjects_shouldHaveSameHashCode() {
        data.addValue(1.0, "R1", "C1");
        DefaultKeyedValues2D other = new DefaultKeyedValues2D();
        other.addValue(1.0, "R1", "C1");
        assertEquals(data.hashCode(), other.hashCode());
    }

    // Tests clone returns a deep copy (modifying clone doesn't affect original)
    @Test
    public void testClone_deepCopy_shouldNotAffectOriginal() throws CloneNotSupportedException {
        data.addValue(1.0, "R1", "C1");
        DefaultKeyedValues2D cloned = (DefaultKeyedValues2D) data.clone();
        cloned.addValue(2.0, "R2", "C2");
        assertFalse(data.equals(cloned));
        assertEquals(1, data.getRowCount());
    }

    // Tests getValue by index with invalid row index throws exception
    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValue_byIndex_invalidRow_throwsException() {
        data.addValue(1.0, "R1", "C1");
        data.getValue(5, 0);
    }

    // Tests getValue by index with invalid column index throws exception
    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValue_byIndex_invalidColumn_throwsException() {
        data.addValue(1.0, "R1", "C1");
        data.getValue(0, 5);
    }

    // Tests setValue with null value should set null
    @Test
    public void testSetValue_withNullValue_shouldSetNull() {
        data.addValue(1.0, "R1", "C1");
        data.setValue(null, "R1", "C1");
        assertNull(data.getValue("R1", "C1"));
    }

    // Tests setValue with new rows/columns should add them
    @Test
    public void testSetValue_withNewRowColumn_shouldAddThem() {
        data.setValue(1.0, "R1", "C1");
        assertEquals(1, data.getRowCount());
        assertEquals(1, data.getColumnCount());
        assertEquals(1.0, data.getValue("R1", "C1").doubleValue(), 0.0001);
    }

    // Tests setValue updates existing value
    @Test
    public void testSetValue_updateExisting_shouldUpdate() {
        data.addValue(1.0, "R1", "C1");
        data.setValue(2.0, "R1", "C1");
        assertEquals(2.0, data.getValue("R1", "C1").doubleValue(), 0.0001);
    }
}