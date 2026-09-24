package org.apache.commons.collections.keyvalue;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

/**
 * Unit tests for {@link MultiKey}.
 */
public class MultiKeyTest {

    // Tests 2-key constructor and basic getters
    @Test
    public void testConstructor_twoKeys_initializesCorrectly() {
        MultiKey mk = new MultiKey("A", "B");
        assertEquals(2, mk.size());
        assertEquals("A", mk.getKey(0));
        assertEquals("B", mk.getKey(1));
        assertArrayEquals(new Object[]{"A", "B"}, mk.getKeys());
    }

    // Tests 3-key constructor
    @Test
    public void testConstructor_threeKeys_initializesCorrectly() {
        MultiKey mk = new MultiKey("A", "B", "C");
        assertEquals(3, mk.size());
        assertEquals("A", mk.getKey(0));
        assertEquals("B", mk.getKey(1));
        assertEquals("C", mk.getKey(2));
    }

    // Tests 4-key constructor
    @Test
    public void testConstructor_fourKeys_initializesCorrectly() {
        MultiKey mk = new MultiKey("A", "B", "C", "D");
        assertEquals(4, mk.size());
        assertEquals("D", mk.getKey(3));
    }

    // Tests 5-key constructor
    @Test
    public void testConstructor_fiveKeys_initializesCorrectly() {
        MultiKey mk = new MultiKey("A", "B", "C", "D", "E");
        assertEquals(5, mk.size());
        assertEquals("E", mk.getKey(4));
    }

    // Tests array constructor with makeClone true
    @Test
    public void testConstructor_arrayCloneTrue_clonesArray() {
        Object[] keys = new Object[]{"A", "B"};
        MultiKey mk = new MultiKey(keys, true);
        keys[0] = "Z";
        assertEquals("A", mk.getKey(0));
    }

    // Tests array constructor with makeClone false
    @Test
    public void testConstructor_arrayCloneFalse_assignsDirectly() {
        Object[] keys = new Object[]{"A", "B"};
        MultiKey mk = new MultiKey(keys, false);
        keys[0] = "Z";
        assertEquals("Z", mk.getKey(0));
    }

    // Tests default array constructor clones by default
    @Test
    public void testConstructor_defaultArray_clonesArray() {
        Object[] keys = new Object[]{"A", "B"};
        MultiKey mk = new MultiKey(keys);
        keys[0] = "Z";
        assertEquals("A", mk.getKey(0));
    }

    // Tests null array passed to constructor throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullArray_throwsIllegalArgumentException() {
        new MultiKey((Object[]) null);
    }

    // Tests getKeys returns a cloned array
    @Test
    public void testGetKeys_modifyingReturnedArray_doesNotAffectMultiKey() {
        MultiKey mk = new MultiKey("A", "B");
        Object[] keys = mk.getKeys();
        keys[0] = "Z";
        assertEquals("A", mk.getKey(0));
    }

    // Tests getKey with invalid index throws exception
    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetKey_outOfBoundsIndex_throwsIndexOutOfBoundsException() {
        MultiKey mk = new MultiKey("A", "B");
        mk.getKey(2);
    }

    // Tests equals with same instance, equal instance, different instance, and non-MultiKey
    @Test
    public void testEquals_variousObjects_returnsExpected() {
        MultiKey mk1 = new MultiKey("A", "B");
        MultiKey mk2 = new MultiKey("A", "B");
        MultiKey mk3 = new MultiKey("A", "C");
        MultiKey mk4 = new MultiKey("A", "B", "C");

        assertTrue(mk1.equals(mk1));
        assertTrue(mk1.equals(mk2));
        assertFalse(mk1.equals(mk3));
        assertFalse(mk1.equals(mk4));
        assertFalse(mk1.equals(null));
        assertFalse(mk1.equals("Not a MultiKey"));
    }

    // Tests hashCode calculation with null and non-null keys
    @Test
    public void testHashCode_nullAndNonNullKeys_calculatesCorrectly() {
        MultiKey mk1 = new MultiKey("A", "B");
        int expectedHash = "A".hashCode() ^ "B".hashCode();
        assertEquals(expectedHash, mk1.hashCode());

        MultiKey mkWithNull = new MultiKey("A", null);
        assertEquals("A".hashCode(), mkWithNull.hashCode());

        MultiKey mkAllNull = new MultiKey(null, null);
        assertEquals(0, mkAllNull.hashCode());
    }

    // Tests toString format
    @Test
    public void testToString_validKeys_returnsFormattedString() {
        MultiKey mk = new MultiKey("A", "B");
        assertEquals("MultiKey[A, B]", mk.toString());
    }

    // Tests deserialization recalculates hashCode (regression test for Defects4J Collections-11)
    @Test
    public void testSerialization_deserializedInstance_hasRecalculatedHashCode() throws Exception {
        MultiKey original = new MultiKey("A", "B");
        int originalHashCode = original.hashCode();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        MultiKey deserialized = (MultiKey) ois.readObject();
        ois.close();

        assertEquals(original, deserialized);
        assertEquals(originalHashCode, deserialized.hashCode());
    }
}