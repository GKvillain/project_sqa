package org.apache.commons.collections.keyvalue;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

import org.junit.Test;

public class MultiKeyTest {

    // Tests two-argument constructor
    @Test
    public void testConstructor_twoKeys_sizeAndKeysCorrect() {
        MultiKey mk = new MultiKey("a", "b");
        assertEquals(2, mk.size());
        assertArrayEquals(new Object[] {"a", "b"}, mk.getKeys());
        assertEquals("a", mk.getKey(0));
        assertEquals("b", mk.getKey(1));
    }

    // Tests three-argument constructor
    @Test
    public void testConstructor_threeKeys_sizeAndKeysCorrect() {
        MultiKey mk = new MultiKey("x", "y", "z");
        assertEquals(3, mk.size());
        assertArrayEquals(new Object[] {"x", "y", "z"}, mk.getKeys());
    }

    // Tests four-argument constructor
    @Test
    public void testConstructor_fourKeys_sizeAndKeysCorrect() {
        MultiKey mk = new MultiKey(1, 2, 3, 4);
        assertEquals(4, mk.size());
        assertArrayEquals(new Object[] {1, 2, 3, 4}, mk.getKeys());
    }

    // Tests five-argument constructor
    @Test
    public void testConstructor_fiveKeys_sizeAndKeysCorrect() {
        MultiKey mk = new MultiKey("A", "B", "C", "D", "E");
        assertEquals(5, mk.size());
        assertArrayEquals(new Object[] {"A", "B", "C", "D", "E"}, mk.getKeys());
    }

    // Tests array constructor with clone (default)
    @Test
    public void testConstructor_arrayWithClone_createsIndependentCopy() {
        Object[] original = {"key1", "key2"};
        MultiKey mk = new MultiKey(original, true);
        // getKeys() should be a clone, not same reference
        assertNotSame(original, mk.getKeys());
        assertTrue(Arrays.equals(original, mk.getKeys()));
    }

    // Tests array constructor without clone (assign directly)
    @Test
    public void testConstructor_arrayWithoutClone_assignsDirectly() {
        Object[] original = {"only"};
        MultiKey mk = new MultiKey(original, false);
        // internal reference is same as original, but getKeys still clones
        assertNotSame(original, mk.getKeys());
        assertTrue(Arrays.equals(original, mk.getKeys()));
    }

    // Tests null key array throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullArray_throwsIllegalArgumentException() {
        new MultiKey(null);
    }

    // Tests null key array with explicit makeClone also throws
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullArrayWithMakeClone_throwsIllegalArgumentException() {
        new MultiKey(null, true);
    }

    // Tests presence of null keys does not cause exception (branch inside calculateHashCode)
    @Test
    public void testConstructor_nullKeys_doesNotThrow() {
        MultiKey mk = new MultiKey(new Object[] {null, "value"});
        assertEquals(2, mk.size());
        assertNotNull(mk);
        // hash code is sum of non-null keys' hash codes; null contributes 0
        int expectedHash = "value".hashCode();
        assertEquals(expectedHash, mk.hashCode());
    }

    // Tests getKey with valid index
    @Test
    public void testGetKey_validIndex_returnsCorrectKey() {
        MultiKey mk = new MultiKey("first", "second");
        assertEquals("first", mk.getKey(0));
        assertEquals("second", mk.getKey(1));
    }

    // Tests getKey with negative index throws IndexOutOfBoundsException
    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetKey_negativeIndex_throwsIndexOutOfBounds() {
        MultiKey mk = new MultiKey("a", "b");
        mk.getKey(-1);
    }

    // Tests getKey with index equal to size throws IndexOutOfBoundsException
    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetKey_indexTooLarge_throwsIndexOutOfBounds() {
        MultiKey mk = new MultiKey("a", "b");
        mk.getKey(2);
    }

    // Tests size method
    @Test
    public void testSize_variousKeys_returnsCorrectLength() {
        assertEquals(2, new MultiKey("a", "b").size());
        assertEquals(1, new MultiKey(new Object[] {"single"}).size());
    }

    // Tests equals: same object
    @Test
    public void testEquals_sameObject_returnsTrue() {
        MultiKey mk = new MultiKey("a", "b");
        assertTrue(mk.equals(mk));
    }

    // Tests equals: equal MultiKeys
    @Test
    public void testEquals_equalMultiKey_returnsTrue() {
        MultiKey mk1 = new MultiKey("a", "b");
        MultiKey mk2 = new MultiKey("a", "b");
        assertTrue(mk1.equals(mk2));
    }

    // Tests equals: different keys
    @Test
    public void testEquals_differentKeys_returnsFalse() {
        MultiKey mk1 = new MultiKey("a", "b");
        MultiKey mk2 = new MultiKey("a", "c");
        assertFalse(mk1.equals(mk2));
    }

    // Tests equals: different number of keys
    @Test
    public void testEquals_differentLength_returnsFalse() {
        MultiKey mk1 = new MultiKey("a", "b");
        MultiKey mk2 = new MultiKey("a", "b", "c");
        assertFalse(mk1.equals(mk2));
    }

    // Tests equals: null argument
    @Test
    public void testEquals_null_returnsFalse() {
        MultiKey mk = new MultiKey("a", "b");
        assertFalse(mk.equals(null));
    }

    // Tests equals: non-MultiKey object
    @Test
    public void testEquals_nonMultiKey_returnsFalse() {
        MultiKey mk = new MultiKey("a", "b");
        assertFalse(mk.equals("some string"));
    }

    // Tests hashCode consistency with equals
    @Test
    public void testHashCode_equalObjects_consistentHashCode() {
        MultiKey mk1 = new MultiKey("a", "b");
        MultiKey mk2 = new MultiKey("a", "b");
        assertEquals(mk1.hashCode(), mk2.hashCode());
    }

    // Tests that hashCode is cached (multiple calls return same value)
    @Test
    public void testHashCode_cached_returnsSameValue() {
        MultiKey mk = new MultiKey("a", "b");
        int hash1 = mk.hashCode();
        int hash2 = mk.hashCode();
        assertEquals(hash1, hash2);
    }

    // Tests toString contains all keys
    @Test
    public void testToString_containsKeys() {
        MultiKey mk = new MultiKey("hello", "world");
        String str = mk.toString();
        assertTrue(str.contains("hello"));
        assertTrue(str.contains("world"));
    }

    // Tests serialization/deserialization: hash code should be restored correctly
    // This test detects the defect (missing readObject) – after deserialization
    // the transient hashCode field becomes 0 instead of the original value.
    @Test
    public void testSerialization_deserializedHashCodeEqualsOriginal() throws Exception {
        MultiKey original = new MultiKey("a", "b");
        int originalHash = original.hashCode();
        assertTrue("Hash code should not be zero for non-null keys", originalHash != 0);

        // Serialize
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(original);
        oos.close();

        // Deserialize
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
        MultiKey deserialized = (MultiKey) ois.readObject();
        ois.close();

        // This assertion will fail if the defect is present (hashCode == 0 after deserialization)
        assertEquals("Hash code after deserialization should equal original", originalHash, deserialized.hashCode());
    }
}