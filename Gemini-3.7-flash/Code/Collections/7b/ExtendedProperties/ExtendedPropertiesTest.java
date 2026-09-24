package org.apache.commons.collections;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Vector;

import static org.junit.Assert.*;

public class ExtendedPropertiesTest {

    private ExtendedProperties props;

    @Before
    public void setUp() {
        props = new ExtendedProperties();
    }

    // Tests adding single string property and retrieving it
    @Test
    public void testAddProperty_singleString_storesCorrectly() {
        props.addProperty("key1", "value1");
        assertEquals("value1", props.getString("key1"));
        assertEquals("value1", props.getProperty("key1"));
        assertTrue(props.isInitialized());
    }

    // Tests comma-separated tokens being split into a list
    @Test
    public void testAddProperty_commaSeparatedString_splitsTokens() {
        props.addProperty("tokens", "first, second, third\\,escaped");
        String[] tokens = props.getStringArray("tokens");
        assertEquals(3, tokens.length);
        assertEquals("first", tokens[0]);
        assertEquals("second", tokens[1]);
        assertEquals("third,escaped", tokens[2]);
    }

    // Tests adding multiple values to the same key creating a list
    @Test
    public void testAddProperty_multipleCallsSameKey_appendsToList() {
        props.addProperty("multi", "val1");
        props.addProperty("multi", "val2");
        List list = props.getList("multi");
        assertEquals(2, list.size());
        assertEquals("val1", list.get(0));
        assertEquals("val2", list.get(1));
    }

    // Tests setting property clears existing values
    @Test
    public void testSetProperty_existingKey_overwritesValue() {
        props.addProperty("key", "val1");
        props.addProperty("key", "val2");
        props.setProperty("key", "val3");
        assertEquals("val3", props.getString("key"));
    }

    // Tests property interpolation with ${key} syntax
    @Test
    public void testGetString_withInterpolation_returnsSubstitutedString() {
        props.addProperty("base.dir", "/home/user");
        props.addProperty("work.dir", "${base.dir}/workspace");
        assertEquals("/home/user/workspace", props.getString("work.dir"));
    }

    // Tests circular reference in interpolation throwing IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testGetString_circularInterpolation_throwsIllegalStateException() {
        props.addProperty("keyA", "${keyB}");
        props.addProperty("keyB", "${keyA}");
        props.getString("keyA");
    }

    // Tests retrieving subset of properties by prefix
    @Test
    public void testSubset_matchingPrefix_returnsSubset() {
        props.addProperty("app.db.url", "localhost");
        props.addProperty("app.db.user", "admin");
        props.addProperty("app.other", "ignored");

        ExtendedProperties subset = props.subset("app.db");
        assertNotNull(subset);
        assertEquals("localhost", subset.getString("url"));
        assertEquals("admin", subset.getString("user"));
        assertNull(subset.getString("other"));
    }

    // Tests subset with non-matching prefix returning null
    @Test
    public void testSubset_nonMatchingPrefix_returnsNull() {
        props.addProperty("key", "value");
        assertNull(props.subset("nonexistent"));
    }

    // Tests loading properties from input stream and saving back to output stream
    @Test
    public void testLoadAndSave_validPropertiesStream_persistsAndReloads() throws IOException {
        String data = "# Comment\n"
                    + "key1 = value1\n"
                    + "long.val = line1 \\\n"
                    + "           line2\n"
                    + "list = a, b, c\n";
        InputStream in = new ByteArrayInputStream(data.getBytes("8859_1"));
        props.load(in);

        assertEquals("value1", props.getString("key1"));
        assertEquals("line1line2", props.getString("long.val"));
        assertEquals(3, props.getStringArray("list").length);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        props.save(out, "Header");
        assertTrue(out.toString().contains("key1=value1"));
    }

    // Tests converting java.util.Properties to ExtendedProperties
    @Test
    public void testConvertProperties_standardProperties_convertsSuccessfully() {
        Properties standard = new Properties();
        standard.setProperty("p1", "v1");
        standard.setProperty("p2", "v2");

        ExtendedProperties converted = ExtendedProperties.convertProperties(standard);
        assertEquals("v1", converted.getString("p1"));
        assertEquals("v2", converted.getString("p2"));
    }

    // Tests combining two ExtendedProperties
    @Test
    public void testCombine_twoProperties_mergesValues() {
        props.addProperty("key1", "val1");
        ExtendedProperties other = new ExtendedProperties();
        other.addProperty("key2", "val2");
        other.addProperty("key1", "valOverride");

        props.combine(other);
        assertEquals("valOverride", props.getString("key1"));
        assertEquals("val2", props.getString("key2"));
    }

    // Tests boolean value parsing for various valid inputs
    @Test
    public void testGetBoolean_validInputs_returnsExpectedBoolean() {
        props.addProperty("b1", "true");
        props.addProperty("b2", "on");
        props.addProperty("b3", "yes");
        props.addProperty("b4", "false");
        props.addProperty("b5", "off");
        props.addProperty("b6", "no");

        assertTrue(props.getBoolean("b1"));
        assertTrue(props.getBoolean("b2"));
        assertTrue(props.getBoolean("b3"));
        assertFalse(props.getBoolean("b4"));
        assertFalse(props.getBoolean("b5"));
        assertFalse(props.getBoolean("b6"));
        assertTrue(props.getBoolean("unknown", true));
    }

    // Tests missing boolean property throwing NoSuchElementException
    @Test(expected = NoSuchElementException.class)
    public void testGetBoolean_missingKey_throwsNoSuchElementException() {
        props.getBoolean("missingKey");
    }

    // Tests integer, long, double, and float parsing with default values
    @Test
    public void testGetNumericTypes_validValues_parsesCorrectly() {
        props.addProperty("intKey", "42");
        props.addProperty("longKey", "10000000000");
        props.addProperty("doubleKey", "3.14159");
        props.addProperty("floatKey", "1.5");
        props.addProperty("byteKey", "8");
        props.addProperty("shortKey", "16");

        assertEquals(42, props.getInt("intKey"));
        assertEquals(42, props.getInt("intKey", 0));
        assertEquals(10, props.getInt("missingInt", 10));
        assertEquals(10000000000L, props.getLong("longKey"));
        assertEquals(3.14159, props.getDouble("doubleKey"), 0.0001);
        assertEquals(1.5f, props.getFloat("floatKey"), 0.0001f);
        assertEquals((byte) 8, props.getByte("byteKey"));
        assertEquals((short) 16, props.getShort("shortKey"));
    }

    // Tests getProperties creating sub-Properties from key=value tokens
    @Test
    public void testGetProperties_tokensWithEqualsSign_createsProperties() {
        props.addProperty("mapProp", "k1=v1, k2=v2");
        Properties p = props.getProperties("mapProp");
        assertEquals("v1", p.getProperty("k1"));
        assertEquals("v2", p.getProperty("k2"));
    }

    // Tests getProperties throwing IllegalArgumentException on token without equals sign
    @Test(expected = IllegalArgumentException.class)
    public void testGetProperties_malformedToken_throwsIllegalArgumentException() {
        props.addProperty("invalidProp", "malformedToken");
        props.getProperties("invalidProp");
    }

    // Tests clearProperty removes entry and maintains key order
    @Test
    public void testClearProperty_existingKey_removesKey() {
        props.addProperty("key1", "val1");
        props.addProperty("key2", "val2");
        props.clearProperty("key1");

        assertNull(props.getProperty("key1"));
        Iterator it = props.getKeys();
        assertTrue(it.hasNext());
        assertEquals("key2", it.next());
        assertFalse(it.hasNext());
    }

    // Tests putAll with Map and ExtendedProperties maintaining order
    @Test
    public void testPutAll_fromMapAndExtendedProperties_storesCorrectly() {
        Map map = new HashMap();
        map.put("m1", "v1");
        props.putAll(map);
        assertEquals("v1", props.get("m1"));

        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("ep1", "epVal1");
        props.putAll(ep);
        assertEquals("epVal1", props.get("ep1"));
    }

    // Tests getInclude and setInclude functionality
    @Test
    public void testIncludeProperty_customAndDefault_behavesCorrectly() {
        assertEquals("include", props.getInclude());
        props.setInclude("import");
        assertEquals("import", props.getInclude());
        props.setInclude(null);
        assertNull(props.getInclude());
    }

    // Tests getVector and getList default behaviors
    @Test
    public void testGetVectorAndList_singleAndMissing_returnsList() {
        props.addProperty("item", "singleVal");
        Vector v = props.getVector("item");
        assertEquals(1, v.size());
        assertEquals("singleVal", v.get(0));

        List defaultList = new Vector();
        defaultList.add("def");
        List result = props.getList("missing", defaultList);
        assertEquals(1, result.size());
        assertEquals("def", result.get(0));
    }
}