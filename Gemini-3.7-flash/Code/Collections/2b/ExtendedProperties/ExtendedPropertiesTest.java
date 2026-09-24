package org.apache.commons.collections;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Vector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ExtendedPropertiesTest {

    private ExtendedProperties props;

    @Before
    public void setUp() {
        props = new ExtendedProperties();
    }

    // Tests adding single and multiple property values (list creation)
    @Test
    public void testAddProperty_multipleValues_createsList() {
        props.addProperty("key", "val1");
        assertEquals("val1", props.getString("key"));

        props.addProperty("key", "val2");
        List list = props.getList("key");
        assertEquals(2, list.size());
        assertEquals("val1", list.get(0));
        assertEquals("val2", list.get(1));
        assertTrue(props.isInitialized());
    }

    // Tests token splitting and comma escaping during addProperty
    @Test
    public void testAddProperty_commaSeparatedAndEscaped_splitsCorrectly() {
        props.addProperty("tokens", "a, b\\, c, d\\\\e");
        List list = props.getList("tokens");
        assertEquals(3, list.size());
        assertEquals("a", list.get(0));
        assertEquals("b, c", list.get(1));
        assertEquals("d\\e", list.get(2));
    }

    // Tests setProperty overriding existing values
    @Test
    public void testSetProperty_existingKey_replacesValue() {
        props.addProperty("key", "initial");
        props.setProperty("key", "updated");
        assertEquals("updated", props.getString("key"));
    }

    // Tests clearing property and ensuring keysAsListed ordering update
    @Test
    public void testClearProperty_existingKey_removesKeyAndListing() {
        props.addProperty("key1", "val1");
        props.addProperty("key2", "val2");
        props.clearProperty("key1");

        assertNull(props.getProperty("key1"));
        Iterator it = props.getKeys();
        assertTrue(it.hasNext());
        assertEquals("key2", it.next());
        assertFalse(it.hasNext());
    }

    // Tests loading properties from an InputStream with continuation lines and comments
    @Test
    public void testLoad_streamWithContinuationAndComments_parsesCorrectly() throws IOException {
        String content = "# Comment line\n"
                + "\n"
                + "key1 = value1\n"
                + "key2 = multi \\\n"
                + "       line \\\n"
                + "       value\n"
                + "empty = \n";
        InputStream in = new ByteArrayInputStream(content.getBytes("ISO-8859-1"));
        props.load(in);

        assertEquals("value1", props.getString("key1"));
        assertEquals("multilinevalue", props.getString("key2"));
        assertNull(props.getProperty("empty"));
    }

    // Tests saving and reloading properties to/from OutputStream
    @Test
    public void testSave_validProperties_writesCorrectFormat() throws IOException {
        props.addProperty("key1", "val,ue");
        props.addProperty("key2", "val2_1");
        props.addProperty("key2", "val2_2");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        props.save(out, "Header Comment");

        ExtendedProperties loaded = new ExtendedProperties();
        loaded.load(new ByteArrayInputStream(out.toByteArray()));

        assertEquals("val,ue", loaded.getString("key1"));
        List list = loaded.getList("key2");
        assertEquals(2, list.size());
        assertEquals("val2_1", list.get(0));
        assertEquals("val2_2", list.get(1));
    }

    // Tests property interpolation with single and chained variables
    @Test
    public void testInterpolate_nestedVariables_interpolatesCorrectly() {
        props.addProperty("base", "hello");
        props.addProperty("greeting", "${base} world");
        props.addProperty("full", "${greeting}!");

        assertEquals("hello world!", props.getString("full"));
    }

    // Tests property interpolation loop detection throwing IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testInterpolate_circularReference_throwsIllegalStateException() {
        props.addProperty("keyA", "${keyB}");
        props.addProperty("keyB", "${keyA}");
        props.getString("keyA");
    }

    // Tests subset generation with prefix
    @Test
    public void testSubset_matchingPrefix_returnsSubsetProperties() {
        props.addProperty("db.host", "localhost");
        props.addProperty("db.port", "3306");
        props.addProperty("app.name", "testApp");

        ExtendedProperties sub = props.subset("db");
        assertNotNull(sub);
        assertEquals("localhost", sub.getString("host"));
        assertEquals("3306", sub.getString("port"));
        assertNull(sub.getProperty("app.name"));

        assertNull(props.subset("nonexistent"));
    }

    // Tests getKeys with prefix filtering
    @Test
    public void testGetKeys_withPrefix_returnsFilteredIterator() {
        props.addProperty("test.a", "1");
        props.addProperty("test.b", "2");
        props.addProperty("other.c", "3");

        Iterator it = props.getKeys("test");
        assertTrue(it.hasNext());
        assertEquals("test.a", it.next());
        assertTrue(it.hasNext());
        assertEquals("test.b", it.next());
        assertFalse(it.hasNext());
    }

    // Tests combining another ExtendedProperties instance
    @Test
    public void testCombine_mergesProperties() {
        props.addProperty("k1", "v1");
        ExtendedProperties other = new ExtendedProperties();
        other.addProperty("k1", "v1_new");
        other.addProperty("k2", "v2");

        props.combine(other);
        assertEquals("v1_new", props.getString("k1"));
        assertEquals("v2", props.getString("k2"));
    }

    // Tests convertProperties from java.util.Properties
    @Test
    public void testConvertProperties_fromJavaUtilProperties_returnsExtendedProperties() {
        Properties p = new Properties();
        p.setProperty("prop1", "val1");
        p.setProperty("prop2", "val2");

        ExtendedProperties converted = ExtendedProperties.convertProperties(p);
        assertEquals("val1", converted.getString("prop1"));
        assertEquals("val2", converted.getString("prop2"));
    }

    // Tests getProperties parsing sub-properties key=value format
    @Test
    public void testGetProperties_validFormat_returnsPropertiesObject() {
        props.addProperty("subprops", "k1=v1, k2=v2");
        Properties sub = props.getProperties("subprops");
        assertEquals("v1", sub.getProperty("k1"));
        assertEquals("v2", sub.getProperty("k2"));
    }

    // Tests getProperties with invalid token format throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testGetProperties_invalidFormat_throwsIllegalArgumentException() {
        props.addProperty("subprops", "invalid_token_without_equal");
        props.getProperties("subprops");
    }

    // Tests typed getters: boolean, byte, short, int, long, float, double
    @Test
    public void testTypedGetters_validInputs_returnsParsedValues() {
        props.addProperty("bool.true1", "true");
        props.addProperty("bool.true2", "yes");
        props.addProperty("bool.true3", "on");
        props.addProperty("bool.false1", "false");
        props.addProperty("bool.false2", "no");
        props.addProperty("bool.false3", "off");

        assertTrue(props.getBoolean("bool.true1"));
        assertTrue(props.getBoolean("bool.true2"));
        assertTrue(props.getBoolean("bool.true3"));
        assertFalse(props.getBoolean("bool.false1"));
        assertFalse(props.getBoolean("bool.false2"));
        assertFalse(props.getBoolean("bool.false3"));

        props.addProperty("num.byte", "8");
        props.addProperty("num.short", "16");
        props.addProperty("num.int", "32");
        props.addProperty("num.long", "64");
        props.addProperty("num.float", "12.34");
        props.addProperty("num.double", "56.78");

        assertEquals((byte) 8, props.getByte("num.byte"));
        assertEquals((short) 16, props.getShort("num.short"));
        assertEquals(32, props.getInt("num.int"));
        assertEquals(32, props.getInteger("num.int"));
        assertEquals(64L, props.getLong("num.long"));
        assertEquals(12.34f, props.getFloat("num.float"), 0.001f);
        assertEquals(56.78d, props.getDouble("num.double"), 0.001d);
    }

    // Tests typed getters default fallback when key is missing
    @Test
    public void testTypedGetters_missingKey_returnsDefaults() {
        assertTrue(props.getBoolean("missing", true));
        assertEquals((byte) 5, props.getByte("missing", (byte) 5));
        assertEquals((short) 10, props.getShort("missing", (short) 10));
        assertEquals(20, props.getInt("missing", 20));
        assertEquals(30L, props.getLong("missing", 30L));
        assertEquals(1.5f, props.getFloat("missing", 1.5f), 0.001f);
        assertEquals(2.5d, props.getDouble("missing", 2.5d), 0.001d);
        assertEquals("default", props.getString("missing", "default"));
    }

    // Tests typed getters throwing NoSuchElementException when missing without default
    @Test(expected = NoSuchElementException.class)
    public void testGetBoolean_missingKey_throwsNoSuchElementException() {
        props.getBoolean("nonexistent");
    }

    // Tests getVector, getList, and getStringArray getters
    @Test
    public void testCollectionGetters_singleAndMultiValues_returnsCorrectCollections() {
        props.addProperty("items", "item1, item2");

        String[] arr = props.getStringArray("items");
        assertEquals(2, arr.length);
        assertEquals("item1", arr[0]);
        assertEquals("item2", arr[1]);

        Vector vec = props.getVector("items");
        assertEquals(2, vec.size());
        assertEquals("item1", vec.get(0));

        List list = props.getList("items");
        assertEquals(2, list.size());
        assertEquals("item2", list.get(1));

        String[] emptyArr = props.getStringArray("missing");
        assertEquals(0, emptyArr.length);
    }

    // Tests type casting exception when value cannot be cast to String
    @Test(expected = ClassCastException.class)
    public void testGetString_incompatibleType_throwsClassCastException() {
        props.put("objectKey", new Object());
        props.getString("objectKey");
    }
}