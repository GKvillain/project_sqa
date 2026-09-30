package org.apache.commons.collections4.keyvalue;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * JUnit 4 test class for MultiKey.
 */
public class MultiKeyTest {

    // Tests constructor with two non-null keys
    @Test
    public void testConstructor_twoKeys_createsMultiKey() {
        MultiKey<String> mk = new MultiKey<String>("a", "b");
        assertEquals(2, mk.size());
        assertEquals("a", mk.getKey(0));
        assertEquals("b", mk.getKey(1));
    }

    // Tests constructor with three keys
    @Test
    public void testConstructor_threeKeys_createsMultiKey() {
        MultiKey<String> mk = new MultiKey<String>("x", "y", "z");
        assertEquals(3, mk.size());
        assertEquals("y", mk.getKey(1));
    }

    // Tests constructor with four keys
    @Test
    public void testConstructor_fourKeys_createsMultiKey() {
        MultiKey<Integer> mk = new MultiKey<Integer>(1, 2, 3, 4);
        assertEquals(4, mk.size());
        assertEquals(Integer.valueOf(4), mk.getKey(3));
    }

    // Tests constructor with five keys
    @Test
    public void testConstructor_fiveKeys_createsMultiKey() {
        MultiKey<String> mk = new MultiKey<String>("a", "b", "c", "d", "e");
        assertEquals(5, mk.size());
        assertEquals("e", mk.getKey(4));
    }

    // Tests constructor with array of keys, makeClone = true
    @Test
    public void testConstructor_arrayWithClone_createsMultiKey() {
        String[] keys = {"p", "q"};
        MultiKey<String> mk = new MultiKey<String>(keys, true);
        keys[0] = "changed";
        assertEquals("p", mk.getKey(0));
    }

    // Tests constructor with array of keys, makeClone = false
    @Test
    public void testConstructor_arrayWithoutClone_usesSameArray() {
        String[] keys = {"r", "s"};
        MultiKey<String> mk = new MultiKey<String>(keys, false);
        assertEquals("r", mk.getKey(0));
    }

    // Tests constructor with null array throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullArray_throwsIllegalArgumentException() {
        new MultiKey<String>((String[]) null);
    }

    // Tests constructor with null array and makeClone flag
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullArrayWithFlag_throwsIllegalArgumentException() {
        new MultiKey<String>((String[]) null, true);
    }

    // Tests getKeys returns a clone
    @Test
    public void testGetKeys_returnsClone() {
        MultiKey<String> mk = new MultiKey<String>("a", "b");
        String[] keys = mk.getKeys();
        keys[0] = "modified";
        assertEquals("a", mk.getKey(0));
    }

    // Tests getKey with valid index
    @Test
    public void testGetKey_validIndex_returnsCorrectKey() {
        MultiKey<String> mk = new MultiKey<String>("first", "second", "third");
        assertEquals("second", mk.getKey(1));
    }

    // Tests getKey with invalid index throws exception
    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testGetKey_invalidIndex_throwsException() {
        MultiKey<String> mk = new MultiKey<String>("a", "b");
        mk.getKey(5);
    }

    // Tests size method
    @Test
    public void testSize_variousSizes_returnsCorrectCount() {
        assertEquals(2, new MultiKey<String>("a", "b").size());
        assertEquals(3, new MultiKey<String>("a", "b", "c").size());
    }

    // Tests equals when same object
    @Test
    public void testEquals_sameObject_returnsTrue() {
        MultiKey<String> mk = new MultiKey<String>("a", "b");
        assertTrue(mk.equals(mk));
    }

    // Tests equals when keys are equal
    @Test
    public void testEquals_equalKeys_returnsTrue() {
        MultiKey<String> mk1 = new MultiKey<String>("a", "b");
        MultiKey<String> mk2 = new MultiKey<String>("a", "b");
        assertTrue(mk1.equals(mk2));
    }

    // Tests equals when keys are different
    @Test
    public void testEquals_differentKeys_returnsFalse() {
        MultiKey<String> mk1 = new MultiKey<String>("a", "b");
        MultiKey<String> mk2 = new MultiKey<String>("a", "c");
        assertFalse(mk1.equals(mk2));
    }

    // Tests equals when other object is not MultiKey
    @Test
    public void testEquals_nonMultiKey_returnsFalse() {
        MultiKey<String> mk = new MultiKey<String>("a", "b");
        assertFalse(mk.equals("a string"));
    }

    // Tests equals when other is null
    @Test
    public void testEquals_nullObject_returnsFalse() {
        MultiKey<String> mk = new MultiKey<String>("a", "b");
        assertFalse(mk.equals(null));
    }

    // Tests hashCode consistency with equals
    @Test
    public void testHashCode_equalKeys_sameHashCode() {
        MultiKey<String> mk1 = new MultiKey<String>("a", "b");
        MultiKey<String> mk2 = new MultiKey<String>("a", "b");
        assertEquals(mk1.hashCode(), mk2.hashCode());
    }

    // Tests hashCode with null keys
    @Test
    public void testHashCode_nullKeys_consistentHash() {
        MultiKey<String> mk = new MultiKey<String>(null, "b");
        assertNotNull(mk.hashCode());
    }

    // Tests toString output
    @Test
    public void testToString_returnsFormattedString() {
        MultiKey<String> mk = new MultiKey<String>("hello", "world");
        assertTrue(mk.toString().contains("hello"));
        assertTrue(mk.toString().contains("world"));
    }

    // Tests constructor with array (cloned) using single-arg constructor
    @Test
    public void testConstructor_arraySingleArg_clonesArray() {
        String[] keys = {"x", "y"};
        MultiKey<String> mk = new MultiKey<String>(keys);
        keys[0] = "z";
        assertEquals("x", mk.getKey(0));
    }

    // ====== New test cases for uncovered areas ======

    // Tests constructor with empty array throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_emptyArray_throwsIllegalArgumentException() {
        new MultiKey<String>(new String[0]);
    }

    // Tests constructor with empty array and makeClone flag throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_emptyArrayWithFlag_throwsIllegalArgumentException() {
        new MultiKey<String>(new String[0], true);
    }

    // Tests constructor with array containing null keys (cloned)
    @Test
    public void testConstructor_arrayWithNullKeys_cloned() {
        String[] keys = {"a", null, "c"};
        MultiKey<String> mk = new MultiKey<String>(keys, true);
        keys[1] = "changed";
        assertNull(mk.getKey(1));
        assertEquals("a", mk.getKey(0));
        assertEquals("c", mk.getKey(2));
    }

    // Tests constructor with array containing null keys (not cloned)
    @Test
    public void testConstructor_arrayWithNullKeys_noClone() {
        String[] keys = {"x", null, "z"};
        MultiKey<String> mk = new MultiKey<String>(keys, false);
        keys[1] = "changed";
        assertEquals("changed", mk.getKey(1));
    }

    // Tests getKeys when makeClone was false - returns clone anyway
    @Test
    public void testGetKeys_noClone_returnsClone() {
        String[] keys = {"a", "b"};
        MultiKey<String> mk = new MultiKey<String>(keys, false);
        String[] retrieved = mk.getKeys();
        retrieved[0] = "modified";
        assertEquals("a", mk.getKey(0));
    }

    // Tests equals with different order of keys
    @Test
    public void testEquals_differentOrder_returnsFalse() {
        MultiKey<String> mk1 = new MultiKey<String>("a", "b");
        MultiKey<String> mk2 = new MultiKey<String>("b", "a");
        assertFalse(mk1.equals(mk2));
    }

    // Tests equals with different lengths
    @Test
    public void testEquals_differentLength_returnsFalse() {
        MultiKey<String> mk1 = new MultiKey<String>("a", "b");
        MultiKey<String> mk2 = new MultiKey<String>("a", "b", "c");
        assertFalse(mk1.equals(mk2));
    }

    // Tests hashCode with different keys produces different hash
    @Test
    public void testHashCode_differentKeys_differentHashCode() {
        MultiKey<String> mk1 = new MultiKey<String>("a", "b");
        MultiKey<String> mk2 = new MultiKey<String>("a", "c");
        assertNotEquals(mk1.hashCode(), mk2.hashCode());
    }

    // Tests toString with null keys
    @Test
    public void testToString_nullKeys_containsNull() {
        MultiKey<String> mk = new MultiKey<String>(null, "test");
        assertTrue(mk.toString().contains("null"));
        assertTrue(mk.toString().contains("test"));
    }

    // Tests constructor with array (single arg) and null elements
    @Test
    public void testConstructor_arraySingleArg_nullElements() {
        String[] keys = {"a", null};
        MultiKey<String> mk = new MultiKey<String>(keys);
        assertNotNull(mk);
        assertNull(mk.getKey(1));
    }

    // Tests getKey with index 0 for one-element MultiKey
    @Test
    public void testGetKey_singleKey_returnsKey() {
        MultiKey<String> mk = new MultiKey<String>("only");
        assertEquals("only", mk.getKey(0));
    }

    // Tests hashCode with all null keys
    @Test
    public void testHashCode_allNullKeys_consistentHash() {
        MultiKey<String> mk1 = new MultiKey<String>(null, null);
        MultiKey<String> mk2 = new MultiKey<String>(null, null);
        assertEquals(mk1.hashCode(), mk2.hashCode());
    }
}