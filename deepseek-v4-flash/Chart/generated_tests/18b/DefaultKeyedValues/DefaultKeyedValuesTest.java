package org.jfree.data;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * JUnit 4 test class for DefaultKeyedValues (Defects4J Bug 18b).
 */
public class DefaultKeyedValuesTest {

    // --- Test insertValue ---

    // Tests inserting a value at the beginning of an empty collection.
    @Test
    public void testInsertValue_positionZeroEmptyList_insertsSuccessfully() {
        DefaultKeyedValues d = new DefaultKeyedValues();
        d.insertValue(0, "K1", 1.0);
        assertEquals("K1", d.getKey(0));
        assertEquals(1.0, d.getValue(0).doubleValue(), 0.0001);
    }

    // Tests inserting a value at the end of a non-empty collection.
    @Test
    public void testInsertValue_positionAtEnd_appendedSuccessfully() {
        DefaultKeyedValues d = new DefaultKeyedValues();
        d.setValue("A", 1);
        d.insertValue(1, "B", 2);
        assertEquals(2, d.getItemCount());
        assertEquals("B", d.getKey(1));
    }

    // Tests inserting a value at a position before an existing key with the same key (update + move).
    // This targets the defect in insertValue where indexMap is not correctly updated.
    @Test
    public void testInsertValue_existingKeyMovesToDifferentPosition_indexMapUpdated() {
        DefaultKeyedValues d = new DefaultKeyedValues();
        d.setValue("A", 1.0);
        d.setValue("B", 2.0);
        d.setValue("C", 3.0); // keys: [A, B, C]
        // Move key "C" to position 0 with a new value
        d.insertValue(0, "C", 10.0);
        assertEquals(3, d.getItemCount());
        assertEquals("C", d.getKey(0));
        assertEquals(10.0, d.getValue(0).doubleValue(), 0.0001);
        assertEquals("A", d.getKey(1));
        assertEquals("B", d.getKey(2));
        // Verify getIndex returns correct index after move
        assertEquals(0, d.getIndex("C"));
        assertEquals(1, d.getIndex("A"));
        assertEquals(2, d.getIndex("B"));
    }

    // Tests inserting a new key at a specific position.
    @Test
    public void testInsertValue_newKeyAtPosition_indexMapUpdated() {
        DefaultKeyedValues d = new DefaultKeyedValues();
        d.setValue("A", 1);
        d.setValue("C", 3);
        d.insertValue(1, "B", 2);
        assertEquals(3, d.getItemCount());
        assertEquals("A", d.getKey(0));
        assertEquals("B", d.getKey(1));
        assertEquals("C", d.getKey(2));
        assertEquals(1, d.getIndex("B"));
    }

    // Tests inserting a value at position 0 with existing key (update + move).
    @Test
    public void testInsertValue_existingKeyMovesToZero_indexMapCorrect() {
        DefaultKeyedValues d = new DefaultKeyedValues();
        d.setValue("X", 10);
        d.setValue("Y", 20);
        d.insertValue(0, "Y", 25); // move Y to front, update value
        assertEquals("Y", d.getKey(0));
        assertEquals(25.0, d.getValue(0).doubleValue(), 0.0001);
        assertEquals("X", d.getKey(1));
        assertEquals(10.0, d.getValue(1).doubleValue(), 0.0001);
        assertEquals(0, d.getIndex("Y"));
        assertEquals(1, d.getIndex("X"));
    }

    // Tests insertValue with invalid position (negative) throws exception.
    @Test(expected = IllegalArgumentException.class)
    public void testInsertValue_negativePosition_throwsException() {
        DefaultKeyedValues d = new DefaultKeyedValues();
        d.insertValue(-1, "K", 1.0);
    }

    // Tests insertValue with position > getItemCount() throws exception.
    @Test(expected = IllegalArgumentException.class)
    public void testInsertValue_positionGreaterThanCount_throwsException() {
        DefaultKeyedValues d = new DefaultKeyedValues();
        d.setValue("A", 1);
        d.insertValue(2, "B", 2); // getItemCount() == 1, position 2 > 1
    }

    // Tests insertValue with null key throws exception.
    @Test(expected = IllegalArgumentException.class)
    public void testInsertValue_nullKey_throwsException() {
        DefaultKeyedValues d = new DefaultKeyedValues();
        d.insertValue(0, null, 1.0);
    }

    // Tests insertValue when key already exists at the same position (update only, no move).
    @Test
    public void testInsertValue_samePositionExistingKey_updatesValue() {
        DefaultKeyedValues d = new DefaultKeyedValues();
        d.setValue("A", 1);
        d.insertValue(0, "A", 99);
        assertEquals(1, d.getItemCount());
        assertEquals(99.0, d.getValue(0).doubleValue(), 0.0001);
        assertEquals(0, d.getIndex("A"));
    }

    // --- Test removeValue(int) ---

    // Tests removing a value by index when index < keys.size() (rebuildIndex is called).
    @Test
    public void testRemoveValue_intIndexLessThanSize_rebuildsIndex() {
        DefaultKeyedValues d = new DefaultKeyedValues();
        d.setValue("A", 1);
        d.setValue("B", 2);
        d.setValue("C", 3);
        d.removeValue(0); // remove first element
        assertEquals(2, d.getItemCount());
        assertEquals("B", d.getKey(0));
        assertEquals("C", d.getKey(1));
        // Verify indexMap is rebuilt correctly
        assertEquals(0, d.getIndex("B"));
        assertEquals(1, d.getIndex("C"));
    }

    // Tests removing the last element (index == keys.size() after removal -> rebuildIndex not called).
    @Test
    public void testRemoveValue_intIndexEqualsSize_doesNotRebuildIndex() {
        DefaultKeyedValues d = new DefaultKeyedValues();
        d.setValue("A", 1);
        d.setValue("B", 2);
        d.removeValue(1); // remove last element, after removal keys.size() == 1, index == 1 == size()
        assertEquals(1, d.getItemCount());
        assertEquals("A", d.getKey(0));
        assertEquals(0, d.getIndex("A"));
    }

    // Tests removing the only element (index == 0, keys.size() becomes 0, rebuildIndex not called).
    @Test
    public void testRemoveValue_intIndexOnlyElement_clearsSuccessfully() {
        DefaultKeyedValues d = new DefaultKeyedValues();
        d.setValue("X", 100);
        d.removeValue(0);
        assertEquals(0, d.getItemCount());
        assertEquals(-1, d.getIndex("X"));
    }

    // --- Test getValue(Comparable) ---

    // Tests getValue with unknown key throws exception.
    @Test(expected = UnknownKeyException.class)
    public void testGetValue_unknownKey_throwsUnknownKeyException() {
        DefaultKeyedValues d = new DefaultKeyedValues();
        d.setValue("A", 1);
        d.getValue("B");
    }

    // Tests getValue with null key throws IllegalArgumentException.
    @Test(expected = IllegalArgumentException.class)
    public void testGetValue_nullKey_throwsIllegalArgumentException() {
        DefaultKeyedValues d = new DefaultKeyedValues();
        d.getValue(null);
    }

    // --- Test removeValue(Comparable) ---

    // Tests removeValue by unknown key does nothing (no exception).
    @Test
    public void testRemoveValue_unknownKey_doesNothing() {
        DefaultKeyedValues d = new DefaultKeyedValues();
        d.setValue("A", 1);
        d.removeValue("B"); // should not throw, just return
        assertEquals(1, d.getItemCount());
        assertEquals(0, d.getIndex("A"));
    }

    // --- Test equals ---

    // Tests equals with non-KeyedValues object returns false.
    @Test
    public void testEquals_nonKeyedValuesObject_returnsFalse() {
        DefaultKeyedValues d = new DefaultKeyedValues();
        assertFalse(d.equals("Not a KeyedValues"));
    }

    // Tests equals with different size returns false.
    @Test
    public void testEquals_differentSize_returnsFalse() {
        DefaultKeyedValues d1 = new DefaultKeyedValues();
        d1.setValue("A", 1);
        DefaultKeyedValues d2 = new DefaultKeyedValues();
        assertFalse(d1.equals(d2));
    }

    // --- Test clone ---

    // Tests clone produces independent copy.
    @Test
    public void testClone_clonedObjectIsIndependent() throws CloneNotSupportedException {
        DefaultKeyedValues d = new DefaultKeyedValues();
        d.setValue("A", 1);
        DefaultKeyedValues clone = (DefaultKeyedValues) d.clone();
        clone.setValue("A", 99);
        assertEquals(1.0, d.getValue(0).doubleValue(), 0.0001);
    }

    // --- Test setValue ---

    // Tests setValue with existing key updates value.
    @Test
    public void testSetValue_existingKey_updatesValue() {
        DefaultKeyedValues d = new DefaultKeyedValues();
        d.setValue("K", 10);
        d.setValue("K", 20);
        assertEquals(1, d.getItemCount());
        assertEquals(20.0, d.getValue(0).doubleValue(), 0.0001);
    }

    // Tests setValue with new key adds value.
    @Test
    public void testSetValue_newKey_addsValue() {
        DefaultKeyedValues d = new DefaultKeyedValues();
        d.setValue("K", 5);
        assertEquals(1, d.getItemCount());
        assertEquals(5.0, d.getValue(0).doubleValue(), 0.0001);
    }

    // Tests setValue with null key throws exception.
    @Test(expected = IllegalArgumentException.class)
    public void testSetValue_nullKey_throwsException() {
        DefaultKeyedValues d = new DefaultKeyedValues();
        d.setValue(null, 1.0);
    }

    // --- Test getIndex ---

    // Tests getIndex with null key throws exception.
    @Test(expected = IllegalArgumentException.class)
    public void testGetIndex_nullKey_throwsException() {
        DefaultKeyedValues d = new DefaultKeyedValues();
        d.getIndex(null);
    }

    // ======================== New Test Cases (เพิ่มใหม่) ========================

    // --- Test getValue(int) ---

    @Test
    public void testGetValueByIndex_normal() {
        DefaultKeyedValues d = new DefaultKeyedValues();
        d.setValue("A", 10.0);
        d.setValue("B", 20.0);
        assertEquals(10.0, d.getValue(0).doubleValue(), 0.0001);
        assertEquals(20.0, d.getValue(1).doubleValue(), 0.0001);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValueByIndex_negativeIndex_throwsException() {
        DefaultKeyedValues d = new DefaultKeyedValues();
        d.setValue("A", 1);
        d.getValue(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValueByIndex_indexTooHigh_throwsException() {
        DefaultKeyedValues d = new DefaultKeyedValues();
        d.setValue("A", 1);
        d.getValue(1); // index == getItemCount() -> out of bounds
    }

    // --- Test getKey(int) ---

    @Test
    public void testGetKeyByIndex_normal() {
        DefaultKeyedValues d = new DefaultKeyedValues();
        d.setValue("X", 1);
        d.setValue("Y", 2);
        assertEquals("X", d.getKey(0));
        assertEquals("Y", d.getKey(1));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetKeyByIndex_negativeIndex_throwsException() {
        DefaultKeyedValues d = new DefaultKeyedValues();
        d.setValue("A", 1);
        d.getKey(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetKeyByIndex_indexTooHigh_throwsException() {
        DefaultKeyedValues d = new DefaultKeyedValues();
        d.setValue("A", 1);
        d.getKey(1);
    }

    // --- Test getKeys() ---

    @Test
    public void testGetKeys_returnsOrderedList() {
        DefaultKeyedValues d = new DefaultKeyedValues();
        d.setValue("C", 3);
        d.setValue("A", 1);
        d.setValue("B", 2);
        java.util.List keys = d.getKeys();
        assertEquals(3, keys.size());
        assertEquals("C", keys.get(0));
        assertEquals("A", keys.get(1));
        assertEquals("B", keys.get(2));
    }

    // --- Test clear() ---

    @Test
    public void testClear_removesAllItems() {
        DefaultKeyedValues d = new DefaultKeyedValues();
        d.setValue("A", 1);
        d.setValue("B", 2);
        d.clear();
        assertEquals(0, d.getItemCount());
        assertEquals(-1, d.getIndex("A"));
        assertEquals(-1, d.getIndex("B"));
    }

    // --- Test removeValue(int) with invalid index ---

    @Test(expected = IndexOutOfBoundsException.class)
    public void testRemoveValueByIndex_negativeIndex_throwsException() {
        DefaultKeyedValues d = new DefaultKeyedValues();
        d.setValue("A", 1);
        d.removeValue(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testRemoveValueByIndex_indexTooHigh_throwsException() {
        DefaultKeyedValues d = new DefaultKeyedValues();
        d.setValue("A", 1);
        d.removeValue(1); // index == getItemCount() -> out of bounds
    }

    // --- Test insertValue with new key at position 0 in non-empty list ---

    @Test
    public void testInsertValue_newKeyAtPositionZero_nonEmptyList_insertsAtFront() {
        DefaultKeyedValues d = new DefaultKeyedValues();
        d.setValue("B", 2);
        d.setValue("C", 3);
        d.insertValue(0, "A", 1);
        assertEquals(3, d.getItemCount());
        assertEquals("A", d.getKey(0));
        assertEquals("B", d.getKey(1));
        assertEquals("C", d.getKey(2));
        assertEquals(0, d.getIndex("A"));
        assertEquals(1, d.getIndex("B"));
        assertEquals(2, d.getIndex("C"));
    }

    // --- Test getValue(Comparable) with existing key ---

    @Test
    public void testGetValueByKey_existingKey_returnsValue() {
        DefaultKeyedValues d = new DefaultKeyedValues();
        d.setValue("K", 42.5);
        assertEquals(42.5, d.getValue("K").doubleValue(), 0.0001);
    }

    @Test
    public void testGetValueByKey_existingKeyNullValue_returnsNull() {
        DefaultKeyedValues d = new DefaultKeyedValues();
        d.setValue("K", null);
        assertNull(d.getValue("K"));
    }

    // --- Test setValue with null value ---

    @Test
    public void testSetValue_nullValue_doesNotThrow() {
        DefaultKeyedValues d = new DefaultKeyedValues();
        d.setValue("K", null);
        assertNull(d.getValue("K"));
        assertEquals(1, d.getItemCount());
    }

    // --- Test equals with same content and same object ---

    @Test
    public void testEquals_sameContent_returnsTrue() {
        DefaultKeyedValues d1 = new DefaultKeyedValues();
        d1.setValue("A", 1);
        d1.setValue("B", 2);
        DefaultKeyedValues d2 = new DefaultKeyedValues();
        d2.setValue("A", 1);
        d2.setValue("B", 2);
        assertTrue(d1.equals(d2));
    }

    @Test
    public void testEquals_differentContent_returnsFalse() {
        DefaultKeyedValues d1 = new DefaultKeyedValues();
        d1.setValue("A", 1);
        DefaultKeyedValues d2 = new DefaultKeyedValues();
        d2.setValue("B", 2);
        assertFalse(d1.equals(d2));
    }

    @Test
    public void testEquals_sameObject_returnsTrue() {
        DefaultKeyedValues d = new DefaultKeyedValues();
        d.setValue("A", 1);
        assertTrue(d.equals(d));
    }

    // --- Test getIndex with existing and non-existing keys ---

    @Test
    public void testGetIndex_existingKey_returnsCorrectIndex() {
        DefaultKeyedValues d = new DefaultKeyedValues();
        d.setValue("X", 1);
        d.setValue("Y", 2);
        assertEquals(0, d.getIndex("X"));
        assertEquals(1, d.getIndex("Y"));
    }

    @Test
    public void testGetIndex_nonExistingKey_returnsNegativeOne() {
        DefaultKeyedValues d = new DefaultKeyedValues();
        d.setValue("A", 1);
        assertEquals(-1, d.getIndex("B"));
    }

    // --- Test clone with multiple values ---

    @Test
    public void testClone_deepCopy_multipleValues() throws CloneNotSupportedException {
        DefaultKeyedValues d = new DefaultKeyedValues();
        d.setValue("A", 1);
        d.setValue("B", 2);
        DefaultKeyedValues clone = (DefaultKeyedValues) d.clone();
        assertEquals(2, clone.getItemCount());
        assertEquals(1.0, clone.getValue(0).doubleValue(), 0.0001);
        assertEquals(2.0, clone.getValue(1).doubleValue(), 0.0001);
        // Modify clone and ensure original unchanged
        clone.setValue("A", 100);
        assertEquals(1.0, d.getValue(0).doubleValue(), 0.0001);
        clone.clear();
        assertEquals(2, d.getItemCount());
    }

    // --- Test removeValue(Comparable) with null key (should throw IllegalArgumentException) ---
    @Test(expected = IllegalArgumentException.class)
    public void testRemoveValueByKey_nullKey_throwsException() {
        DefaultKeyedValues d = new DefaultKeyedValues();
        d.setValue("A", 1);
        d.removeValue(null);
    }
}