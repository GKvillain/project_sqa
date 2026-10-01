package org.jfree.data;

import static org.junit.Assert.*;
import org.junit.Test;

public class KeyedObjects2DTest {

    // Tests getObject(int, int) for valid indices returns correct object
    @Test
    public void testGetObjectByIndex_validIndices_returnsObject() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("Value1", "Row1", "Col1");
        assertEquals("Value1", data.getObject(0, 0));
    }

    // Tests getObject(int, int) when rowData is null (edge case)
    @Test
    public void testGetObjectByIndex_nullRowData_returnsNull() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("Value1", "Row1", "Col1");
        data.removeRow(0);
        assertEquals(null, data.getObject(0, 0));
    }

    // Tests getObject(Comparable, Comparable) with valid keys returns correct object
    @Test
    public void testGetObjectByKey_validKeys_returnsObject() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("Value1", "Row1", "Col1");
        assertEquals("Value1", data.getObject("Row1", "Col1"));
    }

    // Tests getObject(Comparable, Comparable) with null rowKey throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testGetObjectByKey_nullRowKey_throwsException() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.getObject(null, "Col1");
    }

    // Tests getObject(Comparable, Comparable) with null columnKey throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testGetObjectByKey_nullColumnKey_throwsException() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.getObject("Row1", null);
    }

    // Tests getObject(Comparable, Comparable) with unknown rowKey throws exception
    @Test(expected = UnknownKeyException.class)
    public void testGetObjectByKey_unknownRowKey_throwsException() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("Value1", "Row1", "Col1");
        data.getObject("UnknownRow", "Col1");
    }

    // Tests getObject(Comparable, Comparable) with unknown columnKey throws exception
    @Test(expected = UnknownKeyException.class)
    public void testGetObjectByKey_unknownColumnKey_throwsException() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("Value1", "Row1", "Col1");
        data.getObject("Row1", "UnknownCol");
    }

    // Tests setObject with new row and column keys adds new entries
    @Test
    public void testSetObject_newKeys_addsEntries() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("Value1", "Row1", "Col1");
        assertEquals(1, data.getRowCount());
        assertEquals(1, data.getColumnCount());
        assertEquals("Value1", data.getObject(0, 0));
    }

    // Tests setObject with existing row key updates object
    @Test
    public void testSetObject_existingRow_updatesObject() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("Value1", "Row1", "Col1");
        data.setObject("Value2", "Row1", "Col1");
        assertEquals("Value2", data.getObject("Row1", "Col1"));
    }

    // Tests setObject with existing column key updates object
    @Test
    public void testSetObject_existingColumn_updatesObject() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("Value1", "Row1", "Col1");
        data.setObject("Value2", "Row2", "Col1");
        assertEquals("Value2", data.getObject("Row2", "Col1"));
        assertEquals("Value1", data.getObject("Row1", "Col1"));
    }

    // Tests setObject with null object removes entry
    @Test
    public void testSetObject_nullObject_removesEntry() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("Value1", "Row1", "Col1");
        data.setObject(null, "Row1", "Col1");
        assertNull(data.getObject("Row1", "Col1"));
    }

    // Tests setObject with null rowKey throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testSetObject_nullRowKey_throwsException() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("Value1", null, "Col1");
    }

    // Tests setObject with null columnKey throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testSetObject_nullColumnKey_throwsException() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("Value1", "Row1", null);
    }

    // Tests removeObject empties row and column when all objects are null
    @Test
    public void testRemoveObject_emptiesRowAndColumn_removesRowAndColumn() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("Value1", "Row1", "Col1");
        data.removeObject("Row1", "Col1");
        assertEquals(0, data.getRowCount());
        assertEquals(0, data.getColumnCount());
    }

    // Tests removeRow by index removes row and its data
    @Test
    public void testRemoveRowByIndex_validIndex_removesRow() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("Value1", "Row1", "Col1");
        data.removeRow(0);
        assertEquals(0, data.getRowCount());
    }

    // Tests removeRow by key removes row and its data
    @Test
    public void testRemoveRowByKey_validKey_removesRow() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("Value1", "Row1", "Col1");
        data.removeRow("Row1");
        assertEquals(0, data.getRowCount());
    }

    // Tests removeColumn by index removes column from all rows
    @Test
    public void testRemoveColumnByIndex_validIndex_removesColumn() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("Value1", "Row1", "Col1");
        data.setObject("Value2", "Row1", "Col2");
        data.removeColumn(0);
        assertEquals(1, data.getColumnCount());
        assertEquals("Col2", data.getColumnKey(0));
        assertNull(data.getObject("Row1", "Col1"));
    }

    // Tests removeColumn by key removes column from all rows
    @Test
    public void testRemoveColumnByKey_validKey_removesColumn() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("Value1", "Row1", "Col1");
        data.setObject("Value2", "Row1", "Col2");
        data.removeColumn("Col1");
        assertEquals(1, data.getColumnCount());
        assertEquals("Col2", data.getColumnKey(0));
        assertNull(data.getObject("Row1", "Col1"));
    }

    // Tests removeColumn by key throws exception for unknown key
    @Test(expected = UnknownKeyException.class)
    public void testRemoveColumnByKey_unknownKey_throwsException() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.removeColumn("UnknownColumn");
    }

    // Tests equals with identical objects returns true
    @Test
    public void testEquals_identicalObjects_returnsTrue() {
        KeyedObjects2D data1 = new KeyedObjects2D();
        data1.setObject("Value1", "Row1", "Col1");
        KeyedObjects2D data2 = new KeyedObjects2D();
        data2.setObject("Value1", "Row1", "Col1");
        assertTrue(data1.equals(data2));
    }

    // Tests equals with different row keys returns false
    @Test
    public void testEquals_differentRowKeys_returnsFalse() {
        KeyedObjects2D data1 = new KeyedObjects2D();
        data1.setObject("Value1", "Row1", "Col1");
        KeyedObjects2D data2 = new KeyedObjects2D();
        data2.setObject("Value1", "Row2", "Col1");
        assertFalse(data1.equals(data2));
    }

    // Tests clone returns independent object
    @Test
    public void testClone_independentFromOriginal_differentObject() throws CloneNotSupportedException {
        KeyedObjects2D original = new KeyedObjects2D();
        original.setObject("Value1", "Row1", "Col1");
        KeyedObjects2D cloned = (KeyedObjects2D) original.clone();
        cloned.setObject("Value2", "Row1", "Col1");
        assertEquals("Value1", original.getObject("Row1", "Col1"));
        assertEquals("Value2", cloned.getObject("Row1", "Col1"));
    }

    // Tests getRowKeys returns unmodifiable list
    @Test(expected = UnsupportedOperationException.class)
    public void testGetRowKeys_returnsUnmodifiableList_throwsException() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("Value1", "Row1", "Col1");
        data.getRowKeys().add("Row2");
    }
}