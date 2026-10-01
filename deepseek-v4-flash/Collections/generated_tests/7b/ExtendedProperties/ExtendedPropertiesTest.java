package org.apache.commons.collections;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.*;
import java.util.*;

/**
 * JUnit 4 test class for ExtendedProperties (Defects4J bug 7b).
 * Covers normal, boundary, edge cases; focuses on areas likely related to the defect.
 */
public class ExtendedPropertiesTest {

    // Helper: load properties from a string content
    private ExtendedProperties fromString(String content) throws IOException {
        ExtendedProperties ep = new ExtendedProperties();
        ep.load(new ByteArrayInputStream(content.getBytes("ISO-8859-1")));
        return ep;
    }

    // ------- Basic add / get -------

    @Test
    public void testAddProperty_simpleString_returnsValue() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key", "value");
        assertEquals("value", ep.getProperty("key"));
    }

    @Test
    public void testAddProperty_commaSeparated_createsList() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key", "a,b");
        Object val = ep.getProperty("key");
        assertTrue(val instanceof List);
        List list = (List) val;
        assertEquals(2, list.size());
        assertEquals("a", list.get(0));
        assertEquals("b", list.get(1));
    }

    // Tests escaped comma: backslash before comma -> single token with comma
    @Test
    public void testAddProperty_escapedComma_singleToken() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key", "a\\,b");
        Object val = ep.getProperty("key");
        assertTrue(val instanceof String);
        assertEquals("a,b", val);
    }

    // Tests multiple addProperty on same key -> list
    @Test
    public void testAddProperty_multipleAdd_sameKey_appendsToList() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key", "first");
        ep.addProperty("key", "second");
        Object val = ep.getProperty("key");
        assertTrue(val instanceof List);
        List list = (List) val;
        assertEquals(2, list.size());
        assertEquals("first", list.get(0));
        assertEquals("second", list.get(1));
    }

    // Tests getProperty fallback to defaults
    @Test
    public void testGetProperty_nonExistingKey_returnsDefault() {
        ExtendedProperties defaults = new ExtendedProperties();
        defaults.addProperty("defKey", "defVal");
        ExtendedProperties ep = new ExtendedProperties();
        ep.defaults = defaults;
        assertEquals("defVal", ep.getProperty("defKey"));
    }

    // ------- Interpolation -------

    @Test
    public void testGetString_simpleInterpolation_resolves() throws IOException {
        ExtendedProperties ep = fromString("x=hello\ny=${x} world\n");
        assertEquals("hello world", ep.getString("y"));
    }

    // Circular interpolation should throw IllegalStateException (defect area)
    @Test(expected = IllegalStateException.class)
    public void testGetString_circularInterpolation_throwsException() throws IOException {
        ExtendedProperties ep = fromString("a=${b}\nb=${a}\n");
        ep.getString("a");
    }

    // ------- Typed getters -------

    @Test
    public void testGetVector_returnsCopy() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key", "a");
        ep.addProperty("key", "b");
        Vector v = ep.getVector("key");
        assertEquals(2, v.size());
        assertEquals("a", v.get(0));
        assertEquals("b", v.get(1));
    }

    @Test
    public void testGetList_returnsCopy() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key", "x,y");
        List l = ep.getList("key");
        assertEquals(2, l.size());
    }

    @Test
    public void testGetBoolean_trueString_returnsTrue() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("flag", "true");
        assertTrue(ep.getBoolean("flag"));
    }

    // ------- Key management -------

    @Test
    public void testClearProperty_removesKey() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key", "value");
        ep.clearProperty("key");
        assertNull(ep.getProperty("key"));
        assertFalse(ep.containsKey("key"));
    }

    @Test
    public void testGetKeys_returnsAllInInsertionOrder() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("b", "1");
        ep.addProperty("a", "2");
        Iterator it = ep.getKeys();
        assertEquals("b", it.next());
        assertEquals("a", it.next());
        assertFalse(it.hasNext());
    }

    @Test
    public void testGetKeys_prefix_filtersCorrectly() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("app.name", "test");
        ep.addProperty("app.version", "1.0");
        ep.addProperty("other", "x");
        Iterator it = ep.getKeys("app.");
        List keys = new ArrayList();
        while (it.hasNext()) keys.add(it.next());
        assertEquals(2, keys.size());
        assertTrue(keys.contains("app.name"));
        assertTrue(keys.contains("app.version"));
    }

    // ------- Subset -------

    @Test
    public void testSubset_validPrefix_returnsSubset() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("a.x", "1");
        ep.addProperty("a.y", "2");
        ep.addProperty("b.z", "3");
        ExtendedProperties sub = ep.subset("a");
        assertNotNull(sub);
        assertEquals("1", sub.getProperty("x"));
        assertEquals("2", sub.getProperty("y"));
        assertNull(sub.getProperty("z"));
    }

    // ------- Save -------

    @Test
    public void testSave_escapesCommas() throws IOException {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key", "value with comma, ok");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ep.save(bos, null);
        String output = new String(bos.toByteArray(), "ISO-8859-1");
        assertTrue(output.contains("key=value with comma\\, ok"));
    }

    // ------- Load with include -------

    @Test
    public void testLoad_includeProperty_loadsIncludedFile() throws IOException {
        File included = File.createTempFile("testInclude", ".properties");
        included.deleteOnExit();
        FileWriter fw = new FileWriter(included);
        fw.write("inc.key=inc.value\n");
        fw.close();

        String main = "include=" + included.getAbsolutePath().replace("\\", "\\\\") + "\n";
        ExtendedProperties ep = new ExtendedProperties();
        ep.load(new ByteArrayInputStream(main.getBytes("ISO-8859-1")));
        assertEquals("inc.value", ep.getProperty("inc.key"));
    }

    // ------- convertProperties -------

    @Test
    public void testConvertProperties_standardProperties_returnsExtended() {
        Properties p = new Properties();
        p.setProperty("a", "1");
        p.setProperty("b", "2");
        ExtendedProperties ep = ExtendedProperties.convertProperties(p);
        assertEquals("1", ep.getProperty("a"));
        assertEquals("2", ep.getProperty("b"));
    }

    // ------- putAll with ExtendedProperties maintains order -------

    @Test
    public void testPutAll_extendedProperties_maintainsOrder() {
        ExtendedProperties src = new ExtendedProperties();
        src.addProperty("first", "1");
        src.addProperty("second", "2");
        ExtendedProperties dest = new ExtendedProperties();
        dest.putAll(src);
        Iterator it = dest.getKeys();
        assertEquals("first", it.next());
        assertEquals("second", it.next());
    }

    // ====================== Additional tests for uncovered areas ======================

    // Typed getters: int
    @Test
    public void testGetInt_validNumber_returnsValue() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("port", "8080");
        assertEquals(8080, ep.getInt("port"));
    }

    @Test
    public void testGetInt_invalidNumber_returnsDefault() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("port", "abc");
        assertEquals(123, ep.getInt("port", 123));
    }

    // Typed getters: long
    @Test
    public void testGetLong_validNumber_returnsValue() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("size", "1234567890123");
        assertEquals(1234567890123L, ep.getLong("size"));
    }

    // Typed getters: double
    @Test
    public void testGetDouble_validNumber_returnsValue() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("ratio", "3.14");
        assertEquals(3.14, ep.getDouble("ratio"), 0.001);
    }

    // Boolean variants
    @Test
    public void testGetBoolean_variants_true() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("flag1", "yes");
        ep.addProperty("flag2", "on");
        ep.addProperty("flag3", "1");
        assertTrue(ep.getBoolean("flag1"));
        assertTrue(ep.getBoolean("flag2"));
        assertTrue(ep.getBoolean("flag3"));
    }

    @Test
    public void testGetBoolean_invalidString_returnsFalse() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("flag", "maybe");
        assertFalse(ep.getBoolean("flag"));
    }

    // String array
    @Test
    public void testGetStringArray_withString_returnsSingleElementArray() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key", "single");
        String[] arr = ep.getStringArray("key");
        assertArrayEquals(new String[]{"single"}, arr);
    }

    @Test
    public void testGetStringArray_withList_returnsArrayOfElements() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key", "a,b");
        String[] arr = ep.getStringArray("key");
        assertArrayEquals(new String[]{"a", "b"}, arr);
    }

    @Test
    public void testGetStringArray_missingKey_returnsNull() {
        ExtendedProperties ep = new ExtendedProperties();
        assertNull(ep.getStringArray("missing"));
    }

    // Interpolation with missing property (should keep literal)
    @Test
    public void testGetString_interpolation_missingProperty_keepsLiteral() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("a", "${undefined}");
        assertEquals("${undefined}", ep.getString("a"));
    }

    // Subset with no matching prefix returns null
    @Test
    public void testSubset_noMatchingPrefix_returnsNull() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("a.x", "1");
        ExtendedProperties sub = ep.subset("b");
        assertNull(sub);
    }

    // Save with header
    @Test
    public void testSave_withHeader_writesHeader() throws IOException {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key", "value");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ep.save(bos, "header comment");
        String output = new String(bos.toByteArray(), "ISO-8859-1");
        assertTrue(output.startsWith("#header comment"));
    }

    // Clear nonexistent property
    @Test
    public void testClearProperty_nonExisting_doesNotThrow() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.clearProperty("nonexistent");
        assertNull(ep.getProperty("nonexistent"));
    }

    // setProperty overrides
    @Test
    public void testSetProperty_overridesPreviousValue() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.setProperty("key", "first");
        ep.setProperty("key", "second");
        assertEquals("second", ep.getProperty("key"));
    }

    // getKeys when no keys
    @Test
    public void testGetKeys_noKeys_emptyIterator() {
        ExtendedProperties ep = new ExtendedProperties();
        Iterator it = ep.getKeys();
        assertFalse(it.hasNext());
    }

    // putAll with normal Properties
    @Test
    public void testPutAll_normalProperties() {
        ExtendedProperties ep = new ExtendedProperties();
        Properties props = new Properties();
        props.setProperty("a", "1");
        props.setProperty("b", "2");
        ep.putAll(props);
        assertEquals("1", ep.getProperty("a"));
        assertEquals("2", ep.getProperty("b"));
    }

    // getString with default value
    @Test
    public void testGetString_missingKey_returnsDefault() {
        ExtendedProperties ep = new ExtendedProperties();
        assertEquals("default", ep.getString("missing", "default"));
    }
}