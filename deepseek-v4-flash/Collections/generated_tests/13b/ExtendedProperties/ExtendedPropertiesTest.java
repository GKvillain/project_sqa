package org.apache.commons.collections;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import org.junit.Test;

/**
 * Test class for ExtendedProperties with focus on Defects4J bug 13b.
 * The bug is related to handling of properties with commas and escapes.
 * Tests cover normal cases, edge cases, and exception paths.
 */
public class ExtendedPropertiesTest {

    // Tests basic addProperty with simple string value
    @Test
    public void testAddProperty_simpleString_storesCorrectly() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("key", "value");
        assertEquals("value", props.getProperty("key"));
    }

    // Tests addProperty with comma-separated string splits into list
    @Test
    public void testAddProperty_withComma_splitsIntoList() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("key", "a,b,c");
        String[] arr = props.getStringArray("key");
        assertEquals(3, arr.length);
        assertEquals("a", arr[0]);
        assertEquals("b", arr[1]);
        assertEquals("c", arr[2]);
    }

    // Tests addProperty with escaped comma keeps comma in token
    @Test
    public void testAddProperty_escapedComma_keepsCommaInToken() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("key", "a\\,b");
        String[] arr = props.getStringArray("key");
        assertEquals(1, arr.length);
        assertEquals("a,b", arr[0]);
    }

    // Tests addProperty multiple times for same key creates vector/list
    @Test
    public void testAddProperty_multipleValues_accumulatesInList() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("key", "first");
        props.addProperty("key", "second");
        Object value = props.getProperty("key");
        assertTrue(value instanceof List);
        List list = (List) value;
        assertEquals(2, list.size());
        assertEquals("first", list.get(0));
        assertEquals("second", list.get(1));
    }

    // Tests addProperty with escaped backslash
    @Test
    public void testAddProperty_escapedBackslash_unescapesCorrectly() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("key", "a\\\\b");
        assertEquals("a\\b", props.getProperty("key"));
    }

    // Tests addProperty with non-string value stores directly
    @Test
    public void testAddProperty_nonStringValue_storesDirectly() {
        ExtendedProperties props = new ExtendedProperties();
        Integer intVal = Integer.valueOf(10);
        props.addProperty("key", intVal);
        assertEquals(intVal, props.getProperty("key"));
    }

    // Tests getStringArray with single string value
    @Test
    public void testGetStringArray_singleString_returnsSingleElement() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("key", "value");
        String[] arr = props.getStringArray("key");
        assertEquals(1, arr.length);
        assertEquals("value", arr[0]);
    }

    // Tests getStringArray with list value
    @Test
    public void testGetStringArray_listValue_returnsArray() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("key", "first");
        props.addProperty("key", "second");
        String[] arr = props.getStringArray("key");
        assertEquals(2, arr.length);
        assertEquals("first", arr[0]);
        assertEquals("second", arr[1]);
    }

    // Tests getStringArray with null key returns empty array
    @Test
    public void testGetStringArray_nullKey_returnsEmptyArray() {
        ExtendedProperties props = new ExtendedProperties();
        String[] arr = props.getStringArray("nonexistent");
        assertEquals(0, arr.length);
    }

    // Tests getStringArray with wrong type throws ClassCastException
    @Test(expected = ClassCastException.class)
    public void testGetStringArray_invalidType_throwsClassCastException() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("key", Integer.valueOf(5));
        props.getStringArray("key");
    }

    // Tests getString returns interpolated value
    @Test
    public void testGetString_existingKey_returnsValue() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("key", "value");
        assertEquals("value", props.getString("key"));
    }

    // Tests getString with default value
    @Test
    public void testGetString_missingKey_returnsDefault() {
        ExtendedProperties props = new ExtendedProperties();
        assertEquals("default", props.getString("missing", "default"));
    }

    // Tests getString with list returns first element
    @Test
    public void testGetString_listValue_returnsFirstElement() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("key", "first");
        props.addProperty("key", "second");
        assertEquals("first", props.getString("key"));
    }

    // Tests getString with invalid type throws ClassCastException
    @Test(expected = ClassCastException.class)
    public void testGetString_invalidType_throwsClassCastException() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("key", Integer.valueOf(5));
        props.getString("key");
    }

    // Tests getBoolean with string representation
    @Test
    public void testGetBoolean_stringValue_parsesBoolean() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("key", "true");
        assertTrue(props.getBoolean("key"));
    }

    // Tests getBoolean with on/yes values
    @Test
    public void testGetBoolean_onAndYes_parsesTrue() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("key1", "on");
        props.setProperty("key2", "yes");
        assertTrue(props.getBoolean("key1"));
        assertTrue(props.getBoolean("key2"));
    }

    // Tests getBoolean with off/no values
    @Test
    public void testGetBoolean_offAndNo_parsesFalse() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("key1", "off");
        props.setProperty("key2", "no");
        assertFalse(props.getBoolean("key1"));
        assertFalse(props.getBoolean("key2"));
    }

    // Tests getBoolean with invalid string returns false
    @Test
    public void testGetBoolean_invalidString_returnsFalse() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("key", "invalid");
        assertFalse(props.getBoolean("key"));
    }

    // Tests getBoolean with default value for missing key
    @Test
    public void testGetBoolean_missingKey_returnsDefault() {
        ExtendedProperties props = new ExtendedProperties();
        assertTrue(props.getBoolean("missing", true));
        assertFalse(props.getBoolean("missing", false));
    }

    // Tests getBoolean with non-boolean type throws ClassCastException
    @Test(expected = ClassCastException.class)
    public void testGetBoolean_invalidType_throwsClassCastException() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("key", Integer.valueOf(5));
        props.getBoolean("key");
    }

    // Tests getBoolean with missing key and no default throws NoSuchElementException
    @Test(expected = java.util.NoSuchElementException.class)
    public void testGetBoolean_missingKeyNoDefault_throwsNoSuchElementException() {
        ExtendedProperties props = new ExtendedProperties();
        props.getBoolean("missing");
    }

    // Tests load from input stream with properties
    @Test
    public void testLoad_validInputStream_parsesProperties() throws IOException {
        String content = "key1=value1\nkey2=value2\n";
        InputStream input = new ByteArrayInputStream(content.getBytes(StandardCharsets.ISO_8859_1));
        ExtendedProperties props = new ExtendedProperties();
        props.load(input);
        assertEquals("value1", props.getString("key1"));
        assertEquals("value2", props.getString("key2"));
        assertTrue(props.isInitialized());
    }

    // Tests load with comment and blank lines
    @Test
    public void testLoad_withComments_skipsCommentsAndBlanks() throws IOException {
        String content = "# comment\n\nkey=value\n";
        InputStream input = new ByteArrayInputStream(content.getBytes(StandardCharsets.ISO_8859_1));
        ExtendedProperties props = new ExtendedProperties();
        props.load(input);
        assertEquals("value", props.getString("key"));
        assertNull(props.getString("comment"));
    }

    // Tests load with empty value
    @Test
    public void testLoad_emptyValue_allowsEmptyValue() throws IOException {
        String content = "key=\n";
        InputStream input = new ByteArrayInputStream(content.getBytes(StandardCharsets.ISO_8859_1));
        ExtendedProperties props = new ExtendedProperties();
        props.load(input);
        assertEquals("", props.getString("key"));
    }

    // Tests load with escaped comma in value
    @Test
    public void testLoad_escapedComma_preservesComma() throws IOException {
        String content = "key=value\\,with\\,commas\n";
        InputStream input = new ByteArrayInputStream(content.getBytes(StandardCharsets.ISO_8859_1));
        ExtendedProperties props = new ExtendedProperties();
        props.load(input);
        String[] arr = props.getStringArray("key");
        assertEquals(1, arr.length);
        assertEquals("value,with,commas", arr[0]);
    }

    // Tests combine merges properties
    @Test
    public void testCombine_validProps_mergesValues() {
        ExtendedProperties props1 = new ExtendedProperties();
        props1.setProperty("key1", "value1");
        props1.setProperty("key2", "value2");

        ExtendedProperties props2 = new ExtendedProperties();
        props2.setProperty("key2", "newvalue2");
        props2.setProperty("key3", "value3");

        props1.combine(props2);
        assertEquals("value1", props1.getString("key1"));
        assertEquals("newvalue2", props1.getString("key2"));
        assertEquals("value3", props1.getString("key3"));
    }

    // Tests combine overwrites existing values
    @Test
    public void testCombine_duplicateKeys_overwritesValue() {
        ExtendedProperties props1 = new ExtendedProperties();
        props1.setProperty("key", "old");

        ExtendedProperties props2 = new ExtendedProperties();
        props2.setProperty("key", "new");

        props1.combine(props2);
        assertEquals("new", props1.getString("key"));
    }

    // Tests clearProperty removes key and updates key list
    @Test
    public void testClearProperty_existingKey_removesKey() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("key1", "value1");
        props.setProperty("key2", "value2");
        props.clearProperty("key1");
        assertNull(props.getProperty("key1"));
        assertEquals("value2", props.getString("key2"));
        Iterator keys = props.getKeys();
        assertTrue(keys.hasNext());
        assertEquals("key2", keys.next());
        assertFalse(keys.hasNext());
    }

    // Tests getKeys returns all keys
    @Test
    public void testGetKeys_validProperties_returnsAllKeys() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("key1", "value1");
        props.setProperty("key2", "value2");
        Iterator keys = props.getKeys();
        List<String> keyList = new ArrayList<String>();
        while (keys.hasNext()) {
            keyList.add((String) keys.next());
        }
        assertEquals(2, keyList.size());
        assertTrue(keyList.contains("key1"));
        assertTrue(keyList.contains("key2"));
    }

    // Tests getKeys with prefix filter
    @Test
    public void testGetKeys_prefixFilter_returnsMatchingKeys() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("prefix.key1", "value1");
        props.setProperty("prefix.key2", "value2");
        props.setProperty("other.key3", "value3");
        Iterator keys = props.getKeys("prefix");
        List<String> keyList = new ArrayList<String>();
        while (keys.hasNext()) {
            keyList.add((String) keys.next());
        }
        assertEquals(2, keyList.size());
        assertTrue(keyList.contains("prefix.key1"));
        assertTrue(keyList.contains("prefix.key2"));
    }

    // Tests subset returns subset for matching prefix
    @Test
    public void testSubset_validPrefix_returnsSubsetProps() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("prefix.key1", "value1");
        props.setProperty("prefix.key2", "value2");
        props.setProperty("other.key3", "value3");

        ExtendedProperties subset = props.subset("prefix");
        assertNotNull(subset);
        assertEquals("value1", subset.getString("key1"));
        assertEquals("value2", subset.getString("key2"));
        assertNull(subset.getString("key3"));
    }

    // Tests subset with no matching keys returns null
    @Test
    public void testSubset_noMatchingKeys_returnsNull() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("key1", "value1");
        ExtendedProperties subset = props.subset("nomatch");
        assertNull(subset);
    }

    // Tests convertProperties converts standard properties
    @Test
    public void testConvertProperties_validProps_createsExtendedProperties() {
        Properties props = new Properties();
        props.setProperty("key1", "value1");
        props.setProperty("key2", "value2");
        ExtendedProperties extended = ExtendedProperties.convertProperties(props);
        assertEquals("value1", extended.getString("key1"));
        assertEquals("value2", extended.getString("key2"));
    }

    // Tests put adds property and returns old value
    @Test
    public void testPut_newKey_returnsNullAndAdds() {
        ExtendedProperties props = new ExtendedProperties();
        Object oldValue = props.put("key", "value");
        assertNull(oldValue);
        assertEquals("value", props.getString("key"));
    }

    // Tests put with existing key returns old value
    @Test
    public void testPut_existingKey_returnsOldValue() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("key", "old");
        Object oldValue = props.put("key", "new");
        assertEquals("old", oldValue);
        assertEquals("new", props.getString("key"));
    }

    // Tests putAll with map
    @Test
    public void testPutAll_validMap_addsAllEntries() {
        ExtendedProperties props = new ExtendedProperties();
        java.util.Map<String, String> map = new java.util.HashMap<String, String>();
        map.put("key1", "value1");
        map.put("key2", "value2");
        props.putAll(map);
        assertEquals("value1", props.getString("key1"));
        assertEquals("value2", props.getString("key2"));
    }

    // Tests putAll with ExtendedProperties maintains order
    @Test
    public void testPutAll_extendedPropertiesPreservesOrder() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("key1", "value1");
        props.setProperty("key2", "value2");

        ExtendedProperties target = new ExtendedProperties();
        target.putAll(props);
        Iterator keys = target.getKeys();
        assertEquals("key1", keys.next());
        assertEquals("key2", keys.next());
    }

    // Tests remove removes key and returns old value
    @Test
    public void testRemove_existingKey_removesAndReturns() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("key", "value");
        Object removed = props.remove("key");
        assertEquals("value", removed);
        assertNull(props.getProperty("key"));
    }

    // Tests getVector returns vector with values
    @Test
    public void testGetVector_existingKey_returnsVector() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("key", "value");
        Vector vector = props.getVector("key");
        assertEquals(1, vector.size());
        assertEquals("value", vector.get(0));
    }

    // Tests getList returns list with values
    @Test
    public void testGetList_existingKey_returnsList() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("key", "value");
        List list = props.getList("key");
        assertEquals(1, list.size());
        assertEquals("value", list.get(0));
    }

    // Tests getList with list of multiple values
    @Test
    public void testGetList_multipleValues_returnsAll() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("key", "first");
        props.addProperty("key", "second");
        List list = props.getList("key");
        assertEquals(2, list.size());
        assertEquals("first", list.get(0));
        assertEquals("second", list.get(1));
    }

    // Tests getList with missing key returns empty list
    @Test
    public void testGetList_missingKey_returnsEmptyList() {
        ExtendedProperties props = new ExtendedProperties();
        List list = props.getList("missing");
        assertTrue(list.isEmpty());
    }

    // Tests getInt with string value converts to int
    @Test
    public void testGetInteger_stringValue_returnsInt() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("key", "123");
        assertEquals(123, props.getInt("key"));
    }

    // Tests getInt with existing integer returns value
    @Test
    public void testGetInteger_integerValue_returnsInt() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("key", Integer.valueOf(42));
        assertEquals(42, props.getInt("key"));
    }

    // Tests getInt with default for missing key
    @Test
    public void testGetInteger_missingKey_returnsDefault() {
        ExtendedProperties props = new ExtendedProperties();
        assertEquals(10, props.getInt("missing", 10));
    }

    // Tests getInt missing key throws NoSuchElementException
    @Test(expected = java.util.NoSuchElementException.class)
    public void testGetInteger_missingKeyNoDefault_throwsNoSuchElementException() {
        ExtendedProperties props = new ExtendedProperties();
        props.getInt("missing");
    }

    // Additional tests to improve coverage for missing methods

    // Tests getByte with valid value
    @Test
    public void testGetByte_validValue_returnsByte() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("key", "10");
        assertEquals((byte) 10, props.getByte("key"));
    }

    // Tests getByte with missing key returns default
    @Test
    public void testGetByte_missingKey_returnsDefault() {
        ExtendedProperties props = new ExtendedProperties();
        assertEquals((byte) 5, props.getByte("missing", (byte) 5));
    }

    // Tests getShort with valid value
    @Test
    public void testGetShort_validValue_returnsShort() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("key", "1000");
        assertEquals((short) 1000, props.getShort("key"));
    }

    // Tests getShort with missing key returns default
    @Test
    public void testGetShort_missingKey_returnsDefault() {
        ExtendedProperties props = new ExtendedProperties();
        assertEquals((short) 123, props.getShort("missing", (short) 123));
    }

    // Tests getLong with valid value
    @Test
    public void testGetLong_validValue_returnsLong() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("key", "1234567890123");
        assertEquals(1234567890123L, props.getLong("key"));
    }

    // Tests getLong with missing key returns default
    @Test
    public void testGetLong_missingKey_returnsDefault() {
        ExtendedProperties props = new ExtendedProperties();
        assertEquals(999L, props.getLong("missing", 999L));
    }

    // Tests getFloat with valid value
    @Test
    public void testGetFloat_validValue_returnsFloat() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("key", "3.14");
        assertEquals(3.14f, props.getFloat("key"), 0.001f);
    }

    // Tests getFloat with missing key returns default
    @Test
    public void testGetFloat_missingKey_returnsDefault() {
        ExtendedProperties props = new ExtendedProperties();
        assertEquals(2.5f, props.getFloat("missing", 2.5f), 0.001f);
    }

    // Tests getDouble with valid value
    @Test
    public void testGetDouble_validValue_returnsDouble() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("key", "2.71828");
        assertEquals(2.71828, props.getDouble("key"), 1e-5);
    }

    // Tests getDouble with missing key returns default
    @Test
    public void testGetDouble_missingKey_returnsDefault() {
        ExtendedProperties props = new ExtendedProperties();
        assertEquals(1.23, props.getDouble("missing", 1.23), 1e-5);
    }

    // Tests getProperty with default value for missing key
    @Test
    public void testGetProperty_missingKey_returnsDefault() {
        ExtendedProperties props = new ExtendedProperties();
        assertEquals("fallback", props.getProperty("missing", "fallback"));
    }

    // Tests getVector with default value when key missing
    @Test
    public void testGetVector_missingKey_returnsDefaultVector() {
        ExtendedProperties props = new ExtendedProperties();
        Vector<String> defaultVector = new Vector<>();
        defaultVector.add("default");
        Vector<?> result = props.getVector("missing", defaultVector);
        assertEquals(defaultVector, result);
    }

    // Tests save writes properties to output stream
    @Test
    public void testSave_validProperties_writesToOutputStream() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("key1", "value1");
        props.setProperty("key2", "value2");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        props.save(out, "Test Header");
        String content = out.toString("ISO-8859-1");
        assertTrue(content.contains("key1=value1"));
        assertTrue(content.contains("key2=value2"));
        assertTrue(content.contains("#Test Header"));
    }
}