package org.apache.commons.collections;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import org.junit.Test;

/**
 * JUnit 4 test class for ExtendedProperties.
 * Focuses on detecting the Defects4J bug and covering key branches.
 */
public class ExtendedPropertiesTest {

    // Helper to create an ExtendedProperties object from a string
    private ExtendedProperties createProperties(String content) throws IOException {
        InputStream in = new ByteArrayInputStream(content.getBytes(StandardCharsets.ISO_8859_1));
        ExtendedProperties props = new ExtendedProperties();
        props.load(in);
        return props;
    }

    // Test basic load and getProperty
    @Test
    public void testLoad_simpleProperty_returnsValue() throws IOException {
        ExtendedProperties props = createProperties("key = value\n");
        assertEquals("value", props.getProperty("key"));
    }

    // Test addProperty with a single value
    @Test
    public void testAddProperty_singleValue_storesValue() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("key", "value");
        assertEquals("value", props.getProperty("key"));
    }

    // Test addProperty with comma-separated values (vectorized)
    @Test
    public void testAddProperty_commaSeparatedValue_createsList() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("key", "a,b,c");
        Object result = props.getProperty("key");
        assertTrue(result instanceof List);
        List list = (List) result;
        assertEquals(3, list.size());
        assertEquals("a", list.get(0));
        assertEquals("b", list.get(1));
        assertEquals("c", list.get(2));
    }

    // Test addPropertyInternal with escaped comma (COLLECTIONS-238 related)
    @Test
    public void testAddProperty_escapedComma_singleValue() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("key", "a\\,b");
        Object result = props.getProperty("key");
        assertEquals("a,b", result);
    }

    // Test setProperty replaces existing value
    @Test
    public void testSetProperty_existingKey_replacesValue() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("key", "old");
        props.setProperty("key", "new");
        assertEquals("new", props.getProperty("key"));
    }

    // Test getString with a simple string value
    @Test
    public void testGetString_simpleValue_returnsString() throws IOException {
        ExtendedProperties props = createProperties("key = value\n");
        assertEquals("value", props.getString("key"));
    }

    // Test getString with defaults
    @Test
    public void testGetString_keyNotFound_returnsDefault() {
        ExtendedProperties props = new ExtendedProperties();
        assertEquals("default", props.getString("missing", "default"));
    }

    // Test getStringArray with single string value
    @Test
    public void testGetStringArray_singleValue_returnsArray() throws IOException {
        ExtendedProperties props = createProperties("key = value\n");
        String[] result = props.getStringArray("key");
        assertEquals(1, result.length);
        assertEquals("value", result[0]);
    }

    // Test getStringArray with list value
    @Test
    public void testGetStringArray_listValue_returnsArray() throws IOException {
        ExtendedProperties props = createProperties("key = a,b,c\n");
        String[] result = props.getStringArray("key");
        assertEquals(3, result.length);
        assertEquals("a", result[0]);
        assertEquals("b", result[1]);
        assertEquals("c", result[2]);
    }

    // Test getVector with list value
    @Test
    public void testGetVector_listValue_returnsVector() throws IOException {
        ExtendedProperties props = createProperties("key = a,b,c\n");
        Vector result = props.getVector("key");
        assertEquals(3, result.size());
        assertEquals("a", result.get(0));
        assertEquals("b", result.get(1));
        assertEquals("c", result.get(2));
    }

    // Test getList with list value
    @Test
    public void testGetList_listValue_returnsList() throws IOException {
        ExtendedProperties props = createProperties("key = a,b,c\n");
        List result = props.getList("key");
        assertEquals(3, result.size());
        assertEquals("a", result.get(0));
        assertEquals("b", result.get(1));
        assertEquals("c", result.get(2));
    }

    // Test getBoolean with "true" string value
    @Test
    public void testGetBoolean_trueString_returnsTrue() throws IOException {
        ExtendedProperties props = createProperties("flag = true\n");
        assertTrue(props.getBoolean("flag"));
    }

    // Test getBoolean with "yes" string value
    @Test
    public void testGetBoolean_yesString_returnsTrue() throws IOException {
        ExtendedProperties props = createProperties("flag = yes\n");
        assertTrue(props.getBoolean("flag"));
    }

    // Test getBoolean with "off" string value
    @Test
    public void testGetBoolean_offString_returnsFalse() throws IOException {
        ExtendedProperties props = createProperties("flag = off\n");
        assertFalse(props.getBoolean("flag"));
    }

    // Test getBoolean with default value when key not found
    @Test
    public void testGetBoolean_keyNotFound_returnsDefault() {
        ExtendedProperties props = new ExtendedProperties();
        assertTrue(props.getBoolean("missing", true));
        assertFalse(props.getBoolean("missing", false));
    }

    // Test getInt with string value
    @Test
    public void testGetInt_stringValue_returnsInt() throws IOException {
        ExtendedProperties props = createProperties("count = 42\n");
        assertEquals(42, props.getInt("count"));
    }

    // Test getInteger with default value when key not found
    @Test
    public void testGetInteger_keyNotFound_returnsDefault() {
        ExtendedProperties props = new ExtendedProperties();
        assertEquals(10, props.getInteger("missing", 10));
    }

    // Test getKeys returns keys in insertion order
    @Test
    public void testGetKeys_returnsKeysInOrder() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("first", "1");
        props.addProperty("second", "2");
        props.addProperty("third", "3");
        Iterator it = props.getKeys();
        assertEquals("first", it.next());
        assertEquals("second", it.next());
        assertEquals("third", it.next());
        assertFalse(it.hasNext());
    }

    // Test getKeys with prefix filtering
    @Test
    public void testGetKeys_withPrefix_filtersKeys() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("foo.bar", "1");
        props.addProperty("foo.baz", "2");
        props.addProperty("bar.foo", "3");
        Iterator it = props.getKeys("foo.");
        List keys = new ArrayList();
        while (it.hasNext()) {
            keys.add(it.next());
        }
        assertEquals(2, keys.size());
        assertTrue(keys.contains("foo.bar"));
        assertTrue(keys.contains("foo.baz"));
    }

    // Test subset with prefix
    @Test
    public void testSubset_prefix_returnsSubset() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("foo.bar", "1");
        props.addProperty("foo.baz", "2");
        props.addProperty("bar.foo", "3");
        ExtendedProperties subset = props.subset("foo");
        assertNotNull(subset);
        assertEquals("1", subset.getProperty("bar"));
        assertEquals("2", subset.getProperty("baz"));
        assertNull(subset.getProperty("foo"));
    }

    // Test subset when prefix not found returns null
    @Test
    public void testSubset_noMatchingKeys_returnsNull() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("foo.bar", "1");
        assertNull(props.subset("nonexistent"));
    }

    // Test clearProperty removes key and its entry from keysAsListed
    @Test
    public void testClearProperty_existingKey_removesKey() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("key", "value");
        props.clearProperty("key");
        assertNull(props.getProperty("key"));
        assertFalse(props.getKeys().hasNext());
    }

    // Test combine merges properties
    @Test
    public void testCombine_withAnotherProps_merges() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("key1", "value1");
        ExtendedProperties other = new ExtendedProperties();
        other.addProperty("key2", "value2");
        props.combine(other);
        assertEquals("value1", props.getProperty("key1"));
        assertEquals("value2", props.getProperty("key2"));
    }

    // Test convertProperties with standard Properties
    @Test
    public void testConvertProperties_standardProps_returnsExtendedProps() {
        Properties standard = new Properties();
        standard.setProperty("key", "value");
        ExtendedProperties converted = ExtendedProperties.convertProperties(standard);
        assertEquals("value", converted.getProperty("key"));
    }

    // Test put and get behavior via Hashtable interface
    @Test
    public void testPut_stringKeyAndValue_storesProperty() {
        ExtendedProperties props = new ExtendedProperties();
        props.put("key", "value");
        assertEquals("value", props.get("key"));
    }

    // Test remove via Hashtable interface
    @Test
    public void testRemove_existingKey_removesProperty() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("key", "value");
        props.remove("key");
        assertNull(props.getProperty("key"));
    }

    // Test isInitialized after load
    @Test
    public void testLoad_setsInitializedFlag() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        assertFalse(props.isInitialized());
        createProperties("key = value\n");
        InputStream in = new ByteArrayInputStream("key = value\n".getBytes(StandardCharsets.ISO_8859_1));
        props.load(in);
        assertTrue(props.isInitialized());
    }

    // Test addProperty with empty value (COLLECTIONS-238: empty values allowed)
    @Test
    public void testAddProperty_emptyValue_allowed() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("key", "");
        assertEquals("", props.getProperty("key"));
    }

    // Test load with empty file
    @Test
    public void testLoad_emptyFile_noProperties() throws IOException {
        ExtendedProperties props = createProperties("");
        assertFalse(props.getKeys().hasNext());
    }

    // Test load with comments and blank lines
    @Test
    public void testLoad_withComments_skipsLines() throws IOException {
        ExtendedProperties props = createProperties("# comment\n\nkey = value\n");
        assertEquals("value", props.getProperty("key"));
    }

    // Test getString with null key (should return null)
    @Test
    public void testGetString_nullKey_returnsNull() {
        ExtendedProperties props = new ExtendedProperties();
        assertNull(props.getString(null));
    }

    // Test getProperty with null key
    @Test
    public void testGetProperty_nullKey_returnsNull() {
        ExtendedProperties props = new ExtendedProperties();
        assertNull(props.getProperty(null));
    }

    // ==================== New tests for uncovered branches ====================

    // Test addProperty appends to existing list
    @Test
    public void testAddProperty_appendsToList() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("key", "a,b,c");
        props.addProperty("key", "d");
        Object result = props.getProperty("key");
        assertTrue("Result should be a list", result instanceof List);
        List list = (List) result;
        assertEquals(4, list.size());
        assertEquals("a", list.get(0));
        assertEquals("b", list.get(1));
        assertEquals("c", list.get(2));
        assertEquals("d", list.get(3));
    }

    // Test addProperty with escaped comma inside a list value
    @Test
    public void testAddProperty_escapedCommaInList_createsCorrectList() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("key", "a\\,b,c");
        Object result = props.getProperty("key");
        assertTrue("Result should be a list", result instanceof List);
        List list = (List) result;
        assertEquals(2, list.size());
        assertEquals("a,b", list.get(0));
        assertEquals("c", list.get(1));
    }

    // Test getStringArray with escaped comma in list value
    @Test
    public void testGetStringArray_listWithEscapedComma() throws IOException {
        ExtendedProperties props = createProperties("key = a\\,b,c\n");
        String[] result = props.getStringArray("key");
        assertEquals(2, result.length);
        assertEquals("a,b", result[0]);
        assertEquals("c", result[1]);
    }

    // Test setProperty replaces existing list with single value
    @Test
    public void testSetProperty_existingList_replacesWithSingleValue() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("key", "a,b,c");
        props.setProperty("key", "single");
        Object result = props.getProperty("key");
        assertEquals("single", result);
    }

    // Test getBoolean with "on" string returns true
    @Test
    public void testGetBoolean_onString_returnsTrue() throws IOException {
        ExtendedProperties props = createProperties("flag = on\n");
        assertTrue(props.getBoolean("flag"));
    }

    // Test getBoolean with "false" string returns false
    @Test
    public void testGetBoolean_falseString_returnsFalse() throws IOException {
        ExtendedProperties props = createProperties("flag = false\n");
        assertFalse(props.getBoolean("flag"));
    }

    // Test getBoolean with invalid string returns false (default behavior)
    @Test
    public void testGetBoolean_invalidString_returnsFalse() throws IOException {
        ExtendedProperties props = createProperties("flag = invalid\n");
        assertFalse(props.getBoolean("flag"));
    }

    // Test getInt with default when key not found
    @Test
    public void testGetInt_keyNotFound_returnsDefault() {
        ExtendedProperties props = new ExtendedProperties();
        assertEquals(0, props.getInt("missing", 0));
    }

    // Test getInt with invalid number string throws NumberFormatException
    @Test(expected = NumberFormatException.class)
    public void testGetInt_invalidString_throwsNumberFormatException() throws IOException {
        ExtendedProperties props = createProperties("count = abc\n");
        props.getInt("count");
    }

    // Test getStringArray when key is missing returns empty array
    @Test
    public void testGetStringArray_keyMissing_returnsEmptyArray() {
        ExtendedProperties props = new ExtendedProperties();
        String[] result = props.getStringArray("missing");
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    // Test getVector when key is missing returns null
    @Test
    public void testGetVector_keyMissing_returnsNull() {
        ExtendedProperties props = new ExtendedProperties();
        assertNull(props.getVector("missing"));
    }

    // Test getList when key is missing returns null
    @Test
    public void testGetList_keyMissing_returnsNull() {
        ExtendedProperties props = new ExtendedProperties();
        assertNull(props.getList("missing"));
    }

    // Test getKeys with prefix that has no matching keys returns empty iterator
    @Test
    public void testGetKeys_prefixNoMatch_returnsEmptyIterator() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("foo.bar", "1");
        Iterator it = props.getKeys("nonexist.");
        assertFalse(it.hasNext());
    }

    // Test clearProperty on a non-existent key does not throw and leaves state unchanged
    @Test
    public void testClearProperty_nonexistentKey_doesNothing() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("key", "value");
        props.clearProperty("nonexistent");
        assertEquals("value", props.getProperty("key"));
        assertTrue(props.getKeys().hasNext());
    }

    // Test load with null InputStream throws NullPointerException
    @Test(expected = NullPointerException.class)
    public void testLoad_nullInputStream_throwsNullPointerException() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        props.load(null);
    }

    // Test combine with null argument does not throw and remains unchanged
    @Test
    public void testCombine_nullArgument_doesNotThrow() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("key", "value");
        props.combine(null);
        assertEquals("value", props.getProperty("key"));
    }

    // Test isInitialized returns false before any load
    @Test
    public void testIsInitialized_default_false() {
        ExtendedProperties props = new ExtendedProperties();
        assertFalse(props.isInitialized());
    }
}