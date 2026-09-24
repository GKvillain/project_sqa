package org.apache.commons.collections4.keyvalue;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class MultiKeyTest {

    // Tests constructor with two keys
    @Test
    public void testConstructor_twoKeys_createsInstanceCorrectly() {
        final MultiKey<String> mk = new MultiKey<>("A", "B");
        assertEquals(2, mk.size());
        assertEquals("A", mk.getKey(0));
        assertEquals("B", mk.getKey(1));
    }

    // Tests constructor with three keys
    @Test
    public void testConstructor_threeKeys_createsInstanceCorrectly() {
        final MultiKey<String> mk = new MultiKey<>("A", "B", "C");
        assertEquals(3, mk.size());
        assertEquals("A", mk.getKey(0));
        assertEquals("B", mk.getKey(1));
        assertEquals("C", mk.getKey(2));
    }

    // Tests constructor with four keys
    @Test
    public void testConstructor_fourKeys_createsInstanceCorrectly() {
        final MultiKey<String> mk = new MultiKey<>("A", "B", "C", "D");
        assertEquals(4, mk.size());
        assertEquals("A", mk.getKey(0));
        assertEquals("B", mk.getKey(1));
        assertEquals("C", mk.getKey(2));
        assertEquals("D", mk.getKey(3));
    }

    // Tests constructor with five keys
    @Test
    public void testConstructor_fiveKeys_createsInstanceCorrectly() {
        final MultiKey<String> mk = new MultiKey<>("A", "B", "C", "D", "E");
        assertEquals(5, mk.size());
        assertEquals("A", mk.getKey(0));
        assertEquals("B", mk.getKey(1));
        assertEquals("C", mk.getKey(2));
        assertEquals("D", mk.getKey(3));
        assertEquals("E", mk.getKey(4));
    }

    // Tests constructor taking an array that clones by default
    @Test
    public void testConstructor_arrayCloneByDefault_modifyingOriginalArrayDoesNotAffectMultiKey() {
        final String[] keys = new String[] { "A", "B" };
        final MultiKey<String> mk = new MultiKey<>(keys);
        keys[0] = "Z";
        assertEquals("A", mk.getKey(0));
    }

    // Tests constructor with makeClone set to false
    @Test
    public void testConstructor_arrayNoClone_sharesArrayReference() {
        final String[] keys = new String[] { "A", "B" };
        final MultiKey<String> mk = new MultiKey<>(keys, false);
        assertEquals(2, mk.size());
        assertEquals("A", mk.getKey(0));
    }

    // Tests constructor with null array throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullArray_throwsIllegalArgumentException() {
        new MultiKey<>((Object[]) null);
    }

    // Tests constructor with null array and boolean flag throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullArrayWithFlag_throwsIllegalArgumentException() {
        new MultiKey<>((Object[]) null, true);
    }

    // Tests getKey with out of bounds index
    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetKey_outOfBounds_throwsIndexOutOfBoundsException() {
        final MultiKey<String> mk = new MultiKey<>("A", "B");
        mk.getKey(2);
    }

    // Tests getKey with negative index
    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetKey_negativeIndex_throwsIndexOutOfBoundsException() {
        final MultiKey<String> mk = new MultiKey<>("A", "B");
        mk.getKey(-1);
    }

    // Tests getKeys returns cloned array
    @Test
    public void testGetKeys_returnsClonedArray() {
        final MultiKey<String> mk = new MultiKey<>("A", "B");
        final String[] keys = mk.getKeys();
        assertNotNull(keys);
        assertEquals(2, keys.length);
        keys[0] = "Z";
        assertEquals("A", mk.getKey(0));
        assertNotSame(keys, mk.getKeys());
    }

    // Tests equals with same instance and identical keys
    @Test
    public void testEquals_sameAndEqualInstances_returnsTrue() {
        final MultiKey<String> mk1 = new MultiKey<>("A", "B");
        final MultiKey<String> mk2 = new MultiKey<>("A", "B");

        assertTrue(mk1.equals(mk1));
        assertTrue(mk1.equals(mk2));
        assertTrue(mk2.equals(mk1));
    }

    // Tests equals with non-matching instances and other types
    @Test
    public void testEquals_differentValuesAndTypes_returnsFalse() {
        final MultiKey<String> mk1 = new MultiKey<>("A", "B");
        final MultiKey<String> mk2 = new MultiKey<>("A", "C");
        final MultiKey<String> mk3 = new MultiKey<>("A", "B", "C");

        assertFalse(mk1.equals(null));
        assertFalse(mk1.equals("A"));
        assertFalse(mk1.equals(mk2));
        assertFalse(mk1.equals(mk3));
    }

    // Tests hashCode calculation including null values
    @Test
    public void testHashCode_withNullKeys_calculatesCorrectly() {
        final MultiKey<String> mk1 = new MultiKey<>("A", null);
        final MultiKey<String> mk2 = new MultiKey<>("A", null);
        final MultiKey<String> mk3 = new MultiKey<>(null, null);

        assertEquals(mk1.hashCode(), mk2.hashCode());
        assertEquals("A".hashCode(), mk1.hashCode());
        assertEquals(0, mk3.hashCode());
        assertNull(mk1.getKey(1));
    }

    // Tests toString format
    @Test
    public void testToString_validKeys_returnsExpectedFormat() {
        final MultiKey<String> mk = new MultiKey<>("A", "B");
        assertEquals("MultiKey" + Arrays.toString(new String[] { "A", "B" }), mk.toString());
    }

    // Tests serialization and deserialization recalculates hashCode
    @Test
    public void testSerialization_roundTrip_recalculatesHashCode() throws Exception {
        final MultiKey<String> original = new MultiKey<>("Key1", "Key2");

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();

        final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        final ObjectInputStream ois = new ObjectInputStream(bais);
        @SuppressWarnings("unchecked")
        final MultiKey<String> deserialized = (MultiKey<String>) ois.readObject();
        ois.close();

        assertEquals(original, deserialized);
        assertEquals(original.hashCode(), deserialized.hashCode());
        assertEquals(original.size(), deserialized.size());
        assertEquals(original.getKey(0), deserialized.getKey(0));
        assertEquals(original.getKey(1), deserialized.getKey(1));
    }
}