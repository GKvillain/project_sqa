package org.apache.commons.collections.map;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CaseInsensitiveMapTest {

    // Tests default constructor creates an empty map
    @Test
    public void testCaseInsensitiveMap_defaultConstructor_createsEmptyMap() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
    }

    // Tests constructor with initial capacity
    @Test
    public void testCaseInsensitiveMap_validInitialCapacity_createsEmptyMap() {
        CaseInsensitiveMap map = new CaseInsensitiveMap(32);
        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
    }

    // Tests constructor with invalid initial capacity throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testCaseInsensitiveMap_invalidInitialCapacity_throwsException() {
        new CaseInsensitiveMap(0);
    }

    // Tests constructor with capacity and load factor
    @Test
    public void testCaseInsensitiveMap_validCapacityAndLoadFactor_createsEmptyMap() {
        CaseInsensitiveMap map = new CaseInsensitiveMap(16, 0.75f);
        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
    }

    // Tests constructor with invalid load factor throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testCaseInsensitiveMap_invalidLoadFactor_throwsException() {
        new CaseInsensitiveMap(16, -0.5f);
    }

    // Tests map copy constructor normalizes keys and handles duplicates
    @Test
    public void testCaseInsensitiveMap_copyConstructor_copiesAndNormalizesKeys() {
        Map source = new HashMap();
        source.put("Key", "Value1");
        source.put("KEY", "Value2");
        source.put("Other", "Value3");

        CaseInsensitiveMap map = new CaseInsensitiveMap(source);
        assertEquals(2, map.size());
        assertEquals("Value3", map.get("other"));
        assertTrue(map.containsKey("key"));
    }

    // Tests map copy constructor with null map throws exception
    @Test(expected = NullPointerException.class)
    public void testCaseInsensitiveMap_nullMapConstructor_throwsException() {
        new CaseInsensitiveMap((Map) null);
    }

    // Tests put and get with various case variations of keys
    @Test
    public void testPutAndGet_caseInsensitiveKeys_returnsCorrectValue() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        map.put("One", "ValueOne");
        map.put("TWO", "ValueTwo");

        assertEquals("ValueOne", map.get("one"));
        assertEquals("ValueOne", map.get("ONE"));
        assertEquals("ValueOne", map.get("OnE"));

        assertEquals("ValueTwo", map.get("two"));
        assertEquals("ValueTwo", map.get("TWO"));
        assertEquals("ValueTwo", map.get("TwO"));
    }

    // Tests overwriting value when key differs only by case
    @Test
    public void testPut_duplicateCaseKey_overwritesValueAndMaintainsSize() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        Object prev1 = map.put("Case", "First");
        assertNull(prev1);
        assertEquals(1, map.size());

        Object prev2 = map.put("CASE", "Second");
        assertEquals("First", prev2);
        assertEquals(1, map.size());
        assertEquals("Second", map.get("case"));
    }

    // Tests null key support for put, get, containsKey, and remove
    @Test
    public void testPutAndGet_nullKey_handledCorrectly() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        map.put(null, "NullValue");

        assertEquals(1, map.size());
        assertTrue(map.containsKey(null));
        assertEquals("NullValue", map.get(null));

        Object removed = map.remove(null);
        assertEquals("NullValue", removed);
        assertFalse(map.containsKey(null));
        assertNull(map.get(null));
    }

    // Tests containsKey with case-insensitive matching
    @Test
    public void testContainsKey_caseInsensitiveKey_returnsTrue() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        map.put("AlphaBeta", "Test");

        assertTrue(map.containsKey("alphabeta"));
        assertTrue(map.containsKey("ALPHABETA"));
        assertTrue(map.containsKey("AlphaBeta"));
        assertFalse(map.containsKey("gamma"));
    }

    // Tests remove method with case-insensitive key
    @Test
    public void testRemove_caseInsensitiveKey_removesAndReturnsValue() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        map.put("ToRemove", "Data");

        Object removed = map.remove("TOREMOVE");
        assertEquals("Data", removed);
        assertEquals(0, map.size());
        assertFalse(map.containsKey("toremove"));
        assertNull(map.get("ToRemove"));
    }

    // Tests keySet returns lower-cased keys and null
    @Test
    public void testKeySet_mixedCaseKeys_returnsLowerCaseKeys() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        map.put("Foo", "1");
        map.put("BAR", "2");
        map.put(null, "3");

        Set keys = map.keySet();
        assertEquals(3, keys.size());
        assertTrue(keys.contains("foo"));
        assertTrue(keys.contains("bar"));
        assertTrue(keys.contains(null));
        assertFalse(keys.contains("Foo"));
        assertFalse(keys.contains("BAR"));
    }

    // Tests non-String key conversion via toString()
    @Test
    public void testConvertKey_nonStringKey_convertsToStringLowerCase() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        Integer intKey = Integer.valueOf(12345);
        map.put(intKey, "Numeric");

        assertEquals("Numeric", map.get(intKey));
        assertEquals("Numeric", map.get("12345"));
        assertTrue(map.containsKey("12345"));
    }

    // Tests clone creates an independent shallow copy
    @Test
    public void testClone_populatedMap_createsIndependentCopy() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        map.put("KeyA", "ValA");
        map.put("KeyB", "ValB");

        CaseInsensitiveMap cloned = (CaseInsensitiveMap) map.clone();
        assertEquals(map.size(), cloned.size());
        assertEquals("ValA", cloned.get("keya"));
        assertEquals("ValB", cloned.get("keyb"));

        cloned.put("KeyC", "ValC");
        assertEquals(3, cloned.size());
        assertEquals(2, map.size());
        assertFalse(map.containsKey("KeyC"));
    }

    // Tests serialization and deserialization of the map
    @Test
    public void testSerialization_populatedMap_preservesStateAndBehavior() throws Exception {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        map.put("Hello", "World");
        map.put(null, "NullValue");

        CaseInsensitiveMap deserialized = (CaseInsensitiveMap) serializeAndDeserialize(map);

        assertEquals(2, deserialized.size());
        assertEquals("World", deserialized.get("HELLO"));
        assertEquals("NullValue", deserialized.get(null));
        assertTrue(deserialized.containsKey("hello"));
    }

    // Helper method for serialization
    private Object serializeAndDeserialize(Object obj) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(obj);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object result = ois.readObject();
        ois.close();
        return result;
    }
}