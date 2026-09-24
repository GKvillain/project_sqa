package org.jfree.data;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import java.util.List;

/**
 * Unit tests for {@link KeyedObjects2D}.
 */
public class KeyedObjects2DTest {

    private KeyedObjects2D data;

    @Before
    public void setUp() {
        this.data = new KeyedObjects2D();
    }

    // Tests initial state of empty KeyedObjects2D
    @Test
    public void testEmptyInstance_initialState_zeroCounts() {
        assertEquals(0, this.data.getRowCount());
        assertEquals(0, this.data.getColumnCount());
        assertTrue(this.data.getRowKeys().isEmpty());
        assertTrue(this.data.getColumnKeys().isEmpty());
    }

    // Tests setObject and getObject with valid keys
    @Test
    public void testSetObject_validKeys_storesAndRetrievesObject() {
        this.data.setObject("Value1", "R1", "C1");
        assertEquals(1, this.data.getRowCount());
        assertEquals(1, this.data.getColumnCount());
        assertEquals("Value1", this.data.getObject("R1", "C1"));
        assertEquals("Value1", this.data.getObject(0, 0));
        assertEquals("R1", this.data.getRowKey(0));
        assertEquals("C1", this.data.getColumnKey(0));
        assertEquals(0, this.data.getRowIndex("R1"));
        assertEquals(0, this.data.getColumnIndex("C1"));
    }

    // Tests addObject delegates to setObject
    @Test
    public void testAddObject_validKeys_storesObject() {
        this.data.addObject("ValueA", "R1", "C1");
        assertEquals("ValueA", this.data.getObject("R1", "C1"));
    }

    // Tests setObject updating existing entry
    @Test
    public void testSetObject_existingKeys_updatesValue() {
        this.data.setObject("Old", "R1", "C1");
        this.data.setObject("New", "R1", "C1");
        assertEquals("New", this.data.getObject("R1", "C1"));
        assertEquals(1, this.data.getRowCount());
        assertEquals(1, this.data.getColumnCount());
    }

    // Tests setObject with null rowKey throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testSetObject_nullRowKey_throwsException() {
        this.data.setObject("Value", null, "C1");
    }

    // Tests setObject with null columnKey throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testSetObject_nullColumnKey_throwsException() {
        this.data.setObject("Value", "R1", null);
    }

    // Tests getObject with null rowKey throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testGetObject_nullRowKey_throwsException() {
        this.data.getObject(null, "C1");
    }

    // Tests getObject with null columnKey throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testGetObject_nullColumnKey_throwsException() {
        this.data.getObject("R1", null);
    }

    // Tests getObject with unknown row key throws exception
    @Test(expected = UnknownKeyException.class)
    public void testGetObject_unknownRowKey_throwsException() {
        this.data.setObject("Val", "R1", "C1");
        this.data.getObject("UnknownRow", "C1");
    }

    // Tests getObject with unknown column key throws exception
    @Test(expected = UnknownKeyException.class)
    public void testGetObject_unknownColumnKey_throwsException() {
        this.data.setObject("Val", "R1", "C1");
        this.data.getObject("R1", "UnknownCol");
    }

    // Tests getObject by coordinates where row has no entry for column
    @Test
    public void testGetObject_rowWithoutColumnEntry_returnsNull() {
        this.data.setObject("V1", "R1", "C1");
        this.data.setObject("V2", "R2", "C2");
        assertNull(this.data.getObject(0, 1));
        assertNull(this.data.getObject(1, 0));
    }

    // Tests getRowIndex and getColumnIndex with unknown key returns -1
    @Test
    public void testGetIndex_unknownKey_returnsMinusOne() {
        assertEquals(-1, this.data.getRowIndex("NonExisting"));
        assertEquals(-1, this.data.getColumnIndex("NonExisting"));
    }

    // Tests removeRow by row key
    @Test
    public void testRemoveRow_byRowKey_removesRow() {
        this.data.setObject("V1", "R1", "C1");
        this.data.setObject("V2", "R2", "C1");
        this.data.removeRow("R1");
        assertEquals(1, this.data.getRowCount());
        assertEquals("R2", this.data.getRowKey(0));
    }

    // Tests removeRow with unknown key throws exception
    @Test(expected = UnknownKeyException.class)
    public void testRemoveRow_unknownKey_throwsException() {
        this.data.removeRow("UnknownRow");
    }

    // Tests removeColumn by column key
    @Test
    public void testRemoveColumn_byColumnKey_removesColumn() {
        this.data.setObject("V1", "R1", "C1");
        this.data.setObject("V2", "R1", "C2");
        this.data.removeColumn("C1");
        assertEquals(1, this.data.getColumnCount());
        assertEquals("C2", this.data.getColumnKey(0));
    }

    // Tests removeColumn with unknown key throws exception
    @Test(expected = UnknownKeyException.class)
    public void testRemoveColumn_unknownKey_throwsException() {
        this.data.removeColumn("UnknownCol");
    }

    // Tests removeRow by index
    @Test
    public void testRemoveRow_byIndex_removesRow() {
        this.data.setObject("V1", "R1", "C1");
        this.data.setObject("V2", "R2", "C1");
        this.data.removeRow(0);
        assertEquals(1, this.data.getRowCount());
        assertEquals("R2", this.data.getRowKey(0));
    }

    // Tests removeColumn by index
    @Test
    public void testRemoveColumn_byIndex_removesColumn() {
        this.data.setObject("V1", "R1", "C1");
        this.data.setObject("V2", "R1", "C2");
        this.data.removeColumn(0);
        assertEquals(1, this.data.getColumnCount());
        assertEquals("C2", this.data.getColumnKey(0));
    }

    // Tests removeObject when row becomes all null
    @Test
    public void testRemoveObject_singleEntry_removesRow() {
        this.data.setObject("V1", "R1", "C1");
        this.data.removeObject("R1", "C1");
        assertEquals(0, this.data.getRowCount());
    }

    // Tests removeObject when row still has other non-null entries
    @Test
    public void testRemoveObject_rowHasOtherEntries_keepsRow() {
        this.data.setObject("V1", "R1", "C1");
        this.data.setObject("V2", "R1", "C2");
        this.data.removeObject("R1", "C1");
        assertEquals(1, this.data.getRowCount());
        assertNull(this.data.getObject("R1", "C1"));
        assertEquals("V2", this.data.getObject("R1", "C2"));
    }

    // Tests getRowKeys and getColumnKeys returns unmodifiable list
    @Test(expected = UnsupportedOperationException.class)
    public void testGetRowKeys_unmodifiable_throwsExceptionOnModify() {
        this.data.setObject("V1", "R1", "C1");
        List keys = this.data.getRowKeys();
        keys.add("R2");
    }

    // Tests equals method
    @Test
    public void testEquals_variousScenarios_returnsExpected() {
        KeyedObjects2D k1 = new KeyedObjects2D();
        KeyedObjects2D k2 = new KeyedObjects2D();

        assertTrue(k1.equals(k1));
        assertFalse(k1.equals(null));
        assertFalse(k1.equals("NotAKeyedObjects2D"));
        assertTrue(k1.equals(k2));

        k1.setObject("Val", "R1", "C1");
        assertFalse(k1.equals(k2));

        k2.setObject("Val", "R1", "C1");
        assertTrue(k1.equals(k2));
        assertEquals(k1.hashCode(), k2.hashCode());

        k2.setObject("DifferentVal", "R1", "C1");
        assertFalse(k1.equals(k2));
    }

    // Tests clone method produces independent copy
    @Test
    public void testClone_independentCopy_matchesOriginal() throws CloneNotSupportedException {
        this.data.setObject("V1", "R1", "C1");
        KeyedObjects2D clone = (KeyedObjects2D) this.data.clone();

        assertTrue(this.data.equals(clone));
        assertNotSame(this.data, clone);

        clone.setObject("V2", "R1", "C1");
        assertFalse(this.data.equals(clone));
    }
}