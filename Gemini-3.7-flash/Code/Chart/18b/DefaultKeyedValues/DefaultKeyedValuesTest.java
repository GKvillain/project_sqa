package org.jfree.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import org.jfree.chart.util.SortOrder;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class DefaultKeyedValuesTest {

    private DefaultKeyedValues data;

    @Before
    public void setUp() {
        this.data = new DefaultKeyedValues();
    }

    // Tests adding values and verifying size and content
    @Test
    public void testAddValue_validInputs_storesCorrectly() {
        this.data.addValue("A", 1.0);
        this.data.addValue("B", new Double(2.0));
        this.data.addValue("C", null);

        assertEquals(3, this.data.getItemCount());
        assertEquals(new Double(1.0), this.data.getValue(0));
        assertEquals(new Double(2.0), this.data.getValue("B"));
        assertNull(this.data.getValue("C"));
        assertEquals("A", this.data.getKey(0));
        assertEquals(1, this.data.getIndex("B"));
    }

    // Tests updating an existing key with setValue
    @Test
    public void testSetValue_existingKey_updatesValue() {
        this.data.setValue("A", 1.0);
        this.data.setValue("A", 5.0);

        assertEquals(1, this.data.getItemCount());
        assertEquals(new Double(5.0), this.data.getValue("A"));
    }

    // Tests setValue with null key throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testSetValue_nullKey_throwsIllegalArgumentException() {
        this.data.setValue(null, 1.0);
    }

    // Tests getIndex with unknown key returns -1
    @Test
    public void testGetIndex_unknownKey_returnsMinusOne() {
        this.data.addValue("A", 1.0);
        assertEquals(-1, this.data.getIndex("Unknown"));
    }

    // Tests getIndex with null key throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testGetIndex_nullKey_throwsIllegalArgumentException() {
        this.data.getIndex(null);
    }

    // Tests getValue with unknown key throws UnknownKeyException
    @Test(expected = UnknownKeyException.class)
    public void testGetValue_unknownKey_throwsUnknownKeyException() {
        this.data.addValue("A", 1.0);
        this.data.getValue("Unknown");
    }

    // Tests removeValue by key throws UnknownKeyException when key is not found
    @Test(expected = UnknownKeyException.class)
    public void testRemoveValue_unknownKey_throwsUnknownKeyException() {
        this.data.addValue("A", 1.0);
        this.data.removeValue("Unknown");
    }

    // Tests removeValue by index updates item count correctly when removing last item
    @Test
    public void testRemoveValue_lastItem_updatesItemCount() {
        this.data.addValue("A", 1.0);
        this.data.addValue("B", 2.0);
        this.data.removeValue(1);

        assertEquals(1, this.data.getItemCount());
        assertEquals(-1, this.data.getIndex("B"));
    }

    // Tests removeValue by key removes item properly
    @Test
    public void testRemoveValue_existingKey_removesItem() {
        this.data.addValue("A", 1.0);
        this.data.addValue("B", 2.0);
        this.data.addValue("C", 3.0);

        this.data.removeValue("B");

        assertEquals(2, this.data.getItemCount());
        assertEquals("C", this.data.getKey(1));
        assertEquals(1, this.data.getIndex("C"));
    }

    // Tests insertValue at new position
    @Test
    public void testInsertValue_validPosition_insertsCorrectly() {
        this.data.addValue("A", 1.0);
        this.data.addValue("C", 3.0);
        this.data.insertValue(1, "B", 2.0);

        assertEquals(3, this.data.getItemCount());
        assertEquals("B", this.data.getKey(1));
        assertEquals(new Double(2.0), this.data.getValue(1));
        assertEquals(1, this.data.getIndex("B"));
        assertEquals(2, this.data.getIndex("C"));
    }

    // Tests insertValue with existing key moves it to new position
    @Test
    public void testInsertValue_existingKey_movesPosition() {
        this.data.addValue("A", 1.0);
        this.data.addValue("B", 2.0);
        this.data.addValue("C", 3.0);

        this.data.insertValue(0, "C", 30.0);

        assertEquals(3, this.data.getItemCount());
        assertEquals("C", this.data.getKey(0));
        assertEquals(new Double(30.0), this.data.getValue(0));
        assertEquals("A", this.data.getKey(1));
        assertEquals(0, this.data.getIndex("C"));
        assertEquals(1, this.data.getIndex("A"));
    }

    // Tests insertValue with out-of-bounds position throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testInsertValue_outOfBounds_throwsIllegalArgumentException() {
        this.data.insertValue(5, "A", 1.0);
    }

    // Tests clear removes all data and resets state
    @Test
    public void testClear_existingData_clearsAll() {
        this.data.addValue("A", 1.0);
        this.data.addValue("B", 2.0);
        this.data.clear();

        assertEquals(0, this.data.getItemCount());
        assertEquals(-1, this.data.getIndex("A"));
    }

    // Tests sortByKeys in ascending order
    @Test
    public void testSortByKeys_ascendingOrder_sortsCorrectly() {
        this.data.addValue("C", 3.0);
        this.data.addValue("A", 1.0);
        this.data.addValue("B", 2.0);

        this.data.sortByKeys(SortOrder.ASCENDING);

        assertEquals("A", this.data.getKey(0));
        assertEquals("B", this.data.getKey(1));
        assertEquals("C", this.data.getKey(2));
    }

    // Tests sortByValues in ascending order with null values placed at end
    @Test
    public void testSortByValues_ascendingWithNull_sortsCorrectly() {
        this.data.addValue("A", 30.0);
        this.data.addValue("B", null);
        this.data.addValue("C", 10.0);

        this.data.sortByValues(SortOrder.ASCENDING);

        assertEquals("C", this.data.getKey(0));
        assertEquals("A", this.data.getKey(1));
        assertEquals("B", this.data.getKey(2));
        assertNull(this.data.getValue(2));
    }

    // Tests getKeys returns a cloned copy of the keys list
    @Test
    public void testGetKeys_returnsIndependentList() {
        this.data.addValue("A", 1.0);
        List keys = this.data.getKeys();
        keys.clear();

        assertEquals(1, this.data.getItemCount());
    }

    // Tests equals and hashCode methods
    @Test
    public void testEqualsAndHashCode_sameValues_returnsTrueAndSameHashCode() {
        DefaultKeyedValues v1 = new DefaultKeyedValues();
        v1.addValue("A", 1.0);
        v1.addValue("B", null);

        DefaultKeyedValues v2 = new DefaultKeyedValues();
        v2.addValue("A", 1.0);
        v2.addValue("B", null);

        assertTrue(v1.equals(v2));
        assertTrue(v2.equals(v1));
        assertEquals(v1.hashCode(), v2.hashCode());

        v2.setValue("A", 2.0);
        assertFalse(v1.equals(v2));
        assertFalse(v1.equals(null));
        assertFalse(v1.equals("Not a KeyedValues"));
    }

    // Tests clone creates independent copy
    @Test
    public void testClone_clonedInstance_isEqualAndIndependent() throws CloneNotSupportedException {
        this.data.addValue("A", 1.0);
        this.data.addValue("B", 2.0);

        DefaultKeyedValues clone = (DefaultKeyedValues) this.data.clone();

        assertTrue(this.data.equals(clone));
        clone.setValue("A", 99.0);
        assertFalse(this.data.equals(clone));
    }

    // Tests sortByKeys in descending order
    @Test
    public void testSortByKeys_descendingOrder_sortsCorrectly() {
        this.data.addValue("A", 1.0);
        this.data.addValue("C", 3.0);
        this.data.addValue("B", 2.0);

        this.data.sortByKeys(SortOrder.DESCENDING);

        assertEquals("C", this.data.getKey(0));
        assertEquals("B", this.data.getKey(1));
        assertEquals("A", this.data.getKey(2));
    }

    // Tests sortByValues in descending order with null values
    @Test
    public void testSortByValues_descendingWithNull_sortsCorrectly() {
        this.data.addValue("A", 30.0);
        this.data.addValue("B", null);
        this.data.addValue("C", 10.0);

        this.data.sortByValues(SortOrder.DESCENDING);

        assertEquals("A", this.data.getKey(0));
        assertEquals("C", this.data.getKey(1));
        assertEquals("B", this.data.getKey(2));
        assertNull(this.data.getValue(2));
    }

    // Tests insertValue with negative position throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testInsertValue_negativePosition_throwsIllegalArgumentException() {
        this.data.insertValue(-1, "A", 1.0);
    }

    // Tests insertValue with null key throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testInsertValue_nullKey_throwsIllegalArgumentException() {
        this.data.insertValue(0, null, 1.0);
    }

    // Tests insertValue moving an item to a later position
    @Test
    public void testInsertValue_existingKeyMovedLater_insertsCorrectly() {
        this.data.addValue("A", 1.0);
        this.data.addValue("B", 2.0);
        this.data.addValue("C", 3.0);

        this.data.insertValue(2, "A", new Double(10.0));

        assertEquals(3, this.data.getItemCount());
        assertEquals("B", this.data.getKey(0));
        assertEquals("C", this.data.getKey(1));
        assertEquals("A", this.data.getKey(2));
        assertEquals(new Double(10.0), this.data.getValue(2));
    }

    // Tests insertValue at current position updates value without moving
    @Test
    public void testInsertValue_existingKeySamePosition_updatesValue() {
        this.data.addValue("A", 1.0);
        this.data.addValue("B", 2.0);

        this.data.insertValue(0, "A", 10.0);

        assertEquals(2, this.data.getItemCount());
        assertEquals("A", this.data.getKey(0));
        assertEquals(new Double(10.0), this.data.getValue(0));
    }

    // Tests getValue(Comparable) with null key throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testGetValue_nullKey_throwsIllegalArgumentException() {
        this.data.getValue((Comparable) null);
    }

    // Tests equals method with different sizes and keys
    @Test
    public void testEquals_variousScenarios_handlesAllBranches() {
        DefaultKeyedValues v1 = new DefaultKeyedValues();
        DefaultKeyedValues v2 = new DefaultKeyedValues();

        assertTrue(v1.equals(v1));

        v1.addValue("A", 1.0);
        assertFalse(v1.equals(v2));

        v2.addValue("B", 1.0);
        assertFalse(v1.equals(v2));

        v2.clear();
        v2.addValue("A", null);
        assertFalse(v1.equals(v2));
        assertFalse(v2.equals(v1));
    }

    // Tests serialization and deserialization
    @Test
    public void testSerialization_roundTrip_preservesState() throws Exception {
        this.data.addValue("A", 1.0);
        this.data.addValue("B", null);
        this.data.addValue("C", 3.0);

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(buffer);
        out.writeObject(this.data);
        out.close();

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        DefaultKeyedValues restored = (DefaultKeyedValues) in.readObject();
        in.close();

        assertEquals(this.data, restored);
    }
}