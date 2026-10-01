package org.apache.commons.collections;

import static org.junit.Assert.*;
import org.junit.Test;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

/**
 * JUnit 4 test class for ExtendedProperties.
 * Covers key methods, branches, and regression for Defects4J bug 2b.
 */
public class ExtendedPropertiesTest {

    // Tests adding a simple string property and retrieving it
    @Test
    public void testAddProperty_singleString_returnsCorrectValue() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key1", "value1");
        assertEquals("value1", ep.getProperty("key1"));
        assertEquals("value1", ep.getString("key1"));
    }

    // Tests adding property with commas creates a list
    @Test
    public void testAddProperty_withComma_createsList() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("colors", "red,green,blue");
        Object val = ep.get("colors");
        assertTrue(val instanceof List);
        List list = (List) val;
        assertEquals(3, list.size());
        assertEquals("red", list.get(0));
        assertEquals("green", list.get(1));
        assertEquals("blue", list.get(2));
    }

    // Tests escaped comma not split
    @Test
    public void testAddProperty_escapedComma_notSplit() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("msg", "Hello\\, World");
        Object val = ep.get("msg");
        assertTrue(val instanceof String);
        assertEquals("Hello, World", val);
    }

    // Tests getProperty returns null for missing key
    @Test
    public void testGetProperty_missingKey_returnsNull() {
        ExtendedProperties ep = new ExtendedProperties();
        assertNull(ep.getProperty("nonexistent"));
    }

    // Tests getString with simple interpolation
    @Test
    public void testGetString_simpleInterpolation_replacesToken() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("name", "John");
        ep.addProperty("greeting", "Hello ${name}!");
        assertEquals("Hello John!", ep.getString("greeting"));
    }

    // Tests interpolation loop detection throws IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testGetString_loopInterpolation_throwsIllegalStateException() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("a", "${b}");
        ep.addProperty("b", "${a}");
        ep.getString("a");
    }

    // Tests getBoolean with true-like strings
    @Test
    public void testGetBoolean_trueString_returnsTrue() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("flag", "true");
        assertTrue(ep.getBoolean("flag"));
        ep.addProperty("flag2", "on");
        assertTrue(ep.getBoolean("flag2"));
        ep.addProperty("flag3", "yes");
        assertTrue(ep.getBoolean("flag3"));
    }

    // Tests getBoolean with false-like strings
    @Test
    public void testGetBoolean_falseString_returnsFalse() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("flag", "false");
        assertFalse(ep.getBoolean("flag"));
        ep.addProperty("flag2", "off");
        assertFalse(ep.getBoolean("flag2"));
        ep.addProperty("flag3", "no");
        assertFalse(ep.getBoolean("flag3"));
    }

    // Tests getBoolean with invalid string returns default
    @Test
    public void testGetBoolean_invalidString_returnsDefault() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("flag", "maybe");
        assertFalse(ep.getBoolean("flag", false));
        assertTrue(ep.getBoolean("flag", true));
    }

    // Tests getInteger from string conversion
    @Test
    public void testGetInteger_stringValue_returnsInteger() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("count", "42");
        assertEquals(42, ep.getInt("count"));
        assertEquals(Integer.valueOf(42), ep.getInteger("count", null));
    }

    // Tests clearProperty removes key
    @Test
    public void testClearProperty_removesKey() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key1", "value1");
        ep.clearProperty("key1");
        assertNull(ep.get("key1"));
        assertFalse(ep.containsKey("key1"));
    }

    // Tests setProperty overwrites and clears previous list
    @Test
    public void testSetProperty_overwritesExistingValue() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("color", "red");
        ep.addProperty("color", "blue"); // now a list: [red, blue]
        ep.setProperty("color", "green");
        Object val = ep.get("color");
        assertTrue(val instanceof String);
        assertEquals("green", val);
    }

    // Tests getKeys returns keys in insertion order
    @Test
    public void testGetKeys_returnsOrderedKeys() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("z", "last");
        ep.addProperty("a", "first");
        ep.addProperty("m", "middle");
        Iterator keys = ep.getKeys();
        assertEquals("z", keys.next());
        assertEquals("a", keys.next());
        assertEquals("m", keys.next());
    }

    // Tests getStringArray with a list
    @Test
    public void testGetStringArray_list_returnsArray() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("items", "x,y,z");
        String[] arr = ep.getStringArray("items");
        assertArrayEquals(new String[]{"x", "y", "z"}, arr);
    }

    // Tests getVector returns a copy of the list
    @Test
    public void testGetVector_list_returnsVector() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("items", "a,b,c");
        Vector v = ep.getVector("items");
        assertEquals(3, v.size());
        assertEquals("a", v.get(0));
        assertEquals("b", v.get(1));
        assertEquals("c", v.get(2));
        // Verify it's a copy
        v.add("d");
        assertEquals(3, ((List) ep.get("items")).size());
    }

    // Tests subset returns a subset with prefix
    @Test
    public void testSubset_prefix_returnsSubset() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("app.name", "MyApp");
        ep.addProperty("app.version", "1.0");
        ep.addProperty("other", "data");
        ExtendedProperties sub = ep.subset("app");
        assertNotNull(sub);
        assertEquals("MyApp", sub.getProperty("name"));
        assertEquals("1.0", sub.getProperty("version"));
        assertNull(sub.getProperty("other"));
        assertNull(ep.subset("nonexistent"));
    }

    // Tests combine merges properties
    @Test
    public void testCombine_addsProperties() {
        ExtendedProperties ep1 = new ExtendedProperties();
        ep1.addProperty("a", "1");
        ExtendedProperties ep2 = new ExtendedProperties();
        ep2.addProperty("b", "2");
        ep1.combine(ep2);
        assertEquals("1", ep1.getProperty("a"));
        assertEquals("2", ep1.getProperty("b"));
    }

    // Tests load from an InputStream with simple properties
    @Test
    public void testLoad_simpleInputStream_parsesProperties() throws IOException {
        String data = "key1 = value1\nkey2 = value2\n";
        ExtendedProperties ep = new ExtendedProperties();
        ep.load(new ByteArrayInputStream(data.getBytes("8859_1")));
        assertEquals("value1", ep.getProperty("key1"));
        assertEquals("value2", ep.getProperty("key2"));
    }

    // Tests getString with null key returns null (via default)
    @Test
    public void testGetString_nullKey_returnsDefault() {
        ExtendedProperties ep = new ExtendedProperties();
        assertEquals("default", ep.getString("nonexistent", "default"));
    }

    // Tests save outputs correct format
    @Test
    public void testSave_outputStream_writesProperly() throws IOException {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key", "value");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ep.save(baos, "Header");
        String output = baos.toString("8859_1");
        assertTrue(output.contains("key=value"));
    }

    // ===== New test cases to cover previously skipped areas =====

    // Tests getString with missing key returns null (no default)
    @Test
    public void testGetString_missingKey_returnsNull() {
        ExtendedProperties ep = new ExtendedProperties();
        assertNull(ep.getString("nonexistent"));
    }

    // Tests getBoolean with invalid string and no default returns false
    @Test
    public void testGetBoolean_invalidStringNoDefault_returnsFalse() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("flag", "maybe");
        assertFalse(ep.getBoolean("flag"));
    }

    // Tests getInteger with missing key returns null (when using getInteger(String))
    @Test
    public void testGetInteger_missingKey_returnsNull() {
        ExtendedProperties ep = new ExtendedProperties();
        assertNull(ep.getInteger("nonexistent"));
    }

    // Tests getInteger with non-numeric string throws NumberFormatException
    @Test(expected = NumberFormatException.class)
    public void testGetInteger_invalidString_throwsNumberFormatException() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("count", "notanumber");
        ep.getInteger("count");
    }

    // Tests getList returns the list value for a key with comma-separated property
    @Test
    public void testGetList_list_returnsList() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("items", "a,b,c");
        List list = ep.getList("items");
        assertEquals(3, list.size());
        assertEquals("a", list.get(0));
        assertEquals("b", list.get(1));
        assertEquals("c", list.get(2));
    }

    // Tests getStringArray for a single string property (not a list)
    @Test
    public void testGetStringArray_singleString_returnsArrayWithOneElement() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("item", "single");
        String[] arr = ep.getStringArray("item");
        assertArrayEquals(new String[]{"single"}, arr);
    }

    // Tests includes returns true when the value exists (single string)
    @Test
    public void testIncludes_valueExists_returnsTrue() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("color", "red");
        assertTrue(ep.includes("color", "red"));
        assertFalse(ep.includes("color", "blue"));
    }

    // Tests includes returns true when value is part of a list
    @Test
    public void testIncludes_valueInList_returnsTrue() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("colors", "red,green,blue");
        assertTrue(ep.includes("colors", "green"));
        assertFalse(ep.includes("colors", "yellow"));
    }

    // Tests setProperty on a new key behaves like add (single string)
    @Test
    public void testSetProperty_newKey_createsProperty() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.setProperty("newKey", "newValue");
        assertEquals("newValue", ep.getProperty("newKey"));
        assertTrue(ep.get("newKey") instanceof String);
    }

    // Tests load with comment lines and blank lines
    @Test
    public void testLoad_withCommentsAndBlankLines_skipsComments() throws IOException {
        String data = "# This is a comment\n\nkey1=value1\n! Another comment\nkey2=value2\n";
        ExtendedProperties ep = new ExtendedProperties();
        ep.load(new ByteArrayInputStream(data.getBytes("8859_1")));
        assertEquals("value1", ep.getProperty("key1"));
        assertEquals("value2", ep.getProperty("key2"));
        assertNull(ep.getProperty("# This is a comment"));
    }
}