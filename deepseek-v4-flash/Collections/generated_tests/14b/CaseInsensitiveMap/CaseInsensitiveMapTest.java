package org.apache.commons.collections.map;

import static org.junit.Assert.*;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CaseInsensitiveMapTest {

    // Tests null key insertion and retrieval
    @Test
    public void testPutAndGet_nullKey_returnsValue() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        map.put(null, "value");
        assertEquals("value", map.get(null));
    }

    // Tests case-insensitive key lookup
    @Test
    public void testPutAndGet_caseInsensitivity_returnsSameValue() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        map.put("Hello", "world");
        assertEquals("world", map.get("HELLO"));
        assertEquals("world", map.get("hello"));
    }

    // Tests override when key differs only in case
    @Test
    public void testPutAndGet_overrideDueToDifferentCase_replacesValue() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        map.put("key", "first");
        map.put("KEY", "second");
        assertEquals(1, map.size());
        assertEquals("second", map.get("key"));
    }

    // Tests that non-String keys use toString() for conversion
    @Test
    public void testPutAndGet_nonStringKey_usesToString() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        Integer key = 123;
        map.put(key, "value");
        assertEquals("value", map.get("123"));
    }

    // Tests containsKey with null key
    @Test
    public void testContainsKey_nullKey_returnsTrue() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        map.put(null, "value");
        assertTrue(map.containsKey(null));
    }

    // Tests containsKey case-insensitive matching
    @Test
    public void testContainsKey_caseInsensitive_returnsTrue() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        map.put("Alpha", "value");
        assertTrue(map.containsKey("ALPHA"));
        assertTrue(map.containsKey("alpha"));
    }

    // Tests remove with null key
    @Test
    public void testRemove_nullKey_removesEntry() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        map.put(null, "value");
        assertEquals("value", map.remove(null));
        assertNull(map.get(null));
        assertTrue(map.isEmpty());
    }

    // Tests remove with case-insensitive key
    @Test
    public void testRemove_caseInsensitive_removesEntry() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        map.put("Beta", "value");
        assertEquals("value", map.remove("BETA"));
        assertTrue(map.isEmpty());
    }

    // Tests constructor with map containing duplicate case variations
    @Test
    public void testConstructorWithMap_duplicateKeys_keepsLast() {
        Map<String, String> source = new HashMap<String, String>();
        source.put("key1", "first");
        source.put("KEY1", "second");
        source.put("key2", "other");
        CaseInsensitiveMap map = new CaseInsensitiveMap(source);
        assertEquals(2, map.size());
        assertEquals("second", map.get("key1"));
    }

    // Tests constructor with map containing null key
    @Test
    public void testConstructorWithMap_nullKey_included() {
        Map<String, String> source = new HashMap<String, String>();
        source.put(null, "nullVal");
        source.put("A", "aVal");
        CaseInsensitiveMap map = new CaseInsensitiveMap(source);
        assertEquals(2, map.size());
        assertEquals("nullVal", map.get(null));
        assertEquals("aVal", map.get("a"));
    }

    // Tests clone on empty map
    @Test
    public void testClone_emptyMap_equals() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        CaseInsensitiveMap clone = (CaseInsensitiveMap) map.clone();
        assertEquals(map, clone);
    }

    // Tests clone on populated map
    @Test
    public void testClone_populatedMap_equals() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        map.put("One", "1");
        map.put("Two", "2");
        map.put(null, "null");
        CaseInsensitiveMap clone = (CaseInsensitiveMap) map.clone();
        assertEquals(map, clone);
    }

    // Tests serialization round trip with mixed-case keys
    @Test
    public void testSerialization_roundTrip_works() throws Exception {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        map.put("Item1", "value1");
        map.put("item2", "value2");
        map.put("ITEM3", "value3");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(map);
        oos.close();
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        CaseInsensitiveMap deserialized = (CaseInsensitiveMap) ois.readObject();
        ois.close();
        assertEquals(map, deserialized);
        assertEquals("value1", deserialized.get("item1"));
        assertEquals("value2", deserialized.get("Item2"));
        assertEquals("value3", deserialized.get("item3"));
    }

    // Tests serialization round trip with null key
    @Test
    public void testSerialization_nullKey_works() throws Exception {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        map.put(null, "nullVal");
        map.put("X", "xVal");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(map);
        oos.close();
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        CaseInsensitiveMap deserialized = (CaseInsensitiveMap) ois.readObject();
        ois.close();
        assertEquals(map, deserialized);
        assertEquals("nullVal", deserialized.get(null));
        assertEquals("xVal", deserialized.get("x"));
    }

    // Tests that keySet returns lowercase keys and null
    @Test
    public void testKeySet_returnsLowercaseKeysAndNull() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        map.put("One", "1");
        map.put("TWO", "2");
        map.put(null, "null");
        Set keySet = map.keySet();
        assertEquals(3, keySet.size());
        assertTrue(keySet.contains("one"));
        assertTrue(keySet.contains("two"));
        assertTrue(keySet.contains(null));
    }

    // Tests convertKey with null returns AbstractHashedMap.NULL
    @Test
    public void testConvertKey_null_returnsAbstractHashedMapNULL() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        Object result = map.convertKey(null);
        assertSame(AbstractHashedMap.NULL, result);
    }

    // Tests convertKey with non-null returns lower case string
    @Test
    public void testConvertKey_nonNull_returnsLowerCase() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        Object result = map.convertKey("ABC");
        assertEquals("abc", result);
    }

    // Tests constructor with null map throws NullPointerException
    @Test(expected = NullPointerException.class)
    public void testConstructorWithMap_null_throwsNullPointerException() {
        new CaseInsensitiveMap(null);
    }
}