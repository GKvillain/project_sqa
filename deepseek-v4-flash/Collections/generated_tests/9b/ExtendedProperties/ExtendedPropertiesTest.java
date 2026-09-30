package org.apache.commons.collections;

import static org.junit.Assert.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.junit.Test;

public class ExtendedPropertiesTest {

    private ExtendedProperties createFromContent(String content) throws IOException {
        ExtendedProperties ep = new ExtendedProperties();
        ep.load(new ByteArrayInputStream(content.getBytes("ISO-8859-1")));
        return ep;
    }

    @Test
    public void testLoad_simpleProperty_returnsValue() throws IOException {
        ExtendedProperties ep = createFromContent("key=value\n");
        assertEquals("value", ep.getString("key"));
    }

    @Test
    public void testLoad_propertyWithComma_createsList() throws IOException {
        ExtendedProperties ep = createFromContent("key=a,b\n");
        String[] arr = ep.getStringArray("key");
        assertEquals(2, arr.length);
        assertEquals("a", arr[0]);
        assertEquals("b", arr[1]);
    }

    @Test
    public void testLoad_propertyWithEscapedComma_returnsSingleToken() throws IOException {
        ExtendedProperties ep = createFromContent("key=a\\,b\n");
        assertEquals("a,b", ep.getString("key"));
    }

    @Test
    public void testLoad_propertyWithEscapedBackslashAndComma_returnsTwoTokens() throws IOException {
        ExtendedProperties ep = createFromContent("key=a\\\\,b\n");
        List list = ep.getList("key");
        assertEquals(2, list.size());
        assertEquals("a\\", list.get(0));
        assertEquals("b", list.get(1));
    }

    @Test
    public void testLoad_propertyWithLineContinuation_concatenates() throws IOException {
        ExtendedProperties ep = createFromContent("key=line1\\\nline2\n");
        assertEquals("line1line2", ep.getString("key"));
    }

    @Test
    public void testAddProperty_stringWithoutComma_storesAsString() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("k", "v");
        assertEquals("v", ep.getString("k"));
    }

    @Test
    public void testAddProperty_stringWithComma_addsTokensToList() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("k", "a,b");
        List list = ep.getList("k");
        assertEquals(2, list.size());
        assertEquals("a", list.get(0));
        assertEquals("b", list.get(1));
    }

    @Test
    public void testAddProperty_escapedComma_handledByTokenizer() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("k", "a\\,b");
        List list = ep.getList("k");
        assertEquals(2, list.size());
        assertEquals("a\\", list.get(0));
        assertEquals("b", list.get(1));
    }

    @Test
    public void testGetString_withDefault_returnsDefault() {
        ExtendedProperties ep = new ExtendedProperties();
        assertEquals("def", ep.getString("missing", "def"));
    }

    @Test
    public void testGetBoolean_trueString_returnsTrue() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("b", "true");
        assertTrue(ep.getBoolean("b"));
    }

    @Test
    public void testGetBoolean_falseString_returnsFalse() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("b", "false");
        assertFalse(ep.getBoolean("b"));
    }

    @Test
    public void testGetInteger_validString_returnsInteger() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("i", "123");
        assertEquals(123, ep.getInt("i"));
    }

    @Test
    public void testInterpolation_basic_replaces() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("var", "world");
        ep.addProperty("msg", "hello ${var}");
        assertEquals("hello world", ep.getString("msg"));
    }

    @Test(expected = IllegalStateException.class)
    public void testInterpolation_loop_throwsException() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("a", "${b}");
        ep.addProperty("b", "${a}");
        ep.getString("a");
    }

    @Test
    public void testClearProperty_removesKey() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("k", "v");
        assertTrue(ep.containsKey("k"));
        ep.clearProperty("k");
        assertFalse(ep.containsKey("k"));
    }

    @Test
    public void testCombine_mergesProperties() {
        ExtendedProperties ep1 = new ExtendedProperties();
        ep1.addProperty("a", "1");
        ExtendedProperties ep2 = new ExtendedProperties();
        ep2.addProperty("b", "2");
        ep1.combine(ep2);
        assertEquals("1", ep1.getString("a"));
        assertEquals("2", ep1.getString("b"));
    }

    // ========== New test cases for uncovered areas ==========

    @Test
    public void testGetInt_withDefault() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("i", "456");
        assertEquals(456, ep.getInt("i", 0));
        assertEquals(789, ep.getInt("missing", 789));
    }

    @Test
    public void testGetBoolean_withDefault() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("b1", "true");
        assertTrue(ep.getBoolean("b1", false));
        assertFalse(ep.getBoolean("missing", false));
        assertTrue(ep.getBoolean("missing", true));
    }

    @Test
    public void testSetProperty_replacesValue() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("k", "v1");
        ep.setProperty("k", "v2");
        assertEquals("v2", ep.getString("k"));
    }

    @Test
    public void testGetList_accumulation_usingAddProperty() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("list", "a");
        ep.addProperty("list", "b");
        List list = ep.getList("list");
        assertEquals(2, list.size());
        assertEquals("a", list.get(0));
        assertEquals("b", list.get(1));
    }

    @Test
    public void testGetStringArray_accumulation_usingAddProperty() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("arr", "x");
        ep.addProperty("arr", "y");
        String[] arr = ep.getStringArray("arr");
        assertEquals(2, arr.length);
        assertEquals("x", arr[0]);
        assertEquals("y", arr[1]);
    }

    @Test
    public void testKeys_returnsAllKeys() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("a", "1");
        ep.addProperty("b", "2");
        ep.addProperty("c", "3");
        Iterator keys = ep.keys();
        Set<String> keySet = new HashSet<>();
        while (keys.hasNext()) {
            keySet.add((String) keys.next());
        }
        assertEquals(3, keySet.size());
        assertTrue(keySet.contains("a"));
        assertTrue(keySet.contains("b"));
        assertTrue(keySet.contains("c"));
    }

    @Test
    public void testSubset_createsSubProperties() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("prefix.key1", "v1");
        ep.addProperty("prefix.key2", "v2");
        ep.addProperty("other", "v3");
        ExtendedProperties sub = ep.getSubset("prefix");
        assertEquals("v1", sub.getString("key1"));
        assertEquals("v2", sub.getString("key2"));
        assertFalse(sub.containsKey("other"));
    }
}