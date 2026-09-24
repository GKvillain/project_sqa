package org.apache.commons.collections;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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

    // Tests adding string properties and retrieving basic string values
    @Test
    public void testGetString_existingKey_returnsValue() {
        props.addProperty("key1", "value1");
        assertEquals("value1", props.getString("key1"));
        assertEquals("value1", props.getString("key1", "default"));
        assertEquals("default", props.getString("nonExistingKey", "default"));
        assertNull(props.getString("nonExistingKey"));
    }

    // Tests interpolation of properties with ${key} syntax
    @Test
    public void testInterpolate_variablesPresent_interpolatesCorrectly() {
        props.addProperty("base.url", "http://localhost");
        props.addProperty("port", "8080");
        props.addProperty("full.url", "${base.url}:${port}/api");

        assertEquals("http://localhost:8080/api", props.getString("full.url"));
    }

    // Tests exception thrown on circular interpolation
    @Test(expected = IllegalStateException.class)
    public void testInterpolate_circularReference_throwsIllegalStateException() {
        props.addProperty("keyA", "${keyB}");
        props.addProperty("keyB", "${keyA}");
        props.getString("keyA");
    }

    // Tests adding multiple values for the same key resulting in a list/vector
    @Test
    public void testAddProperty_multipleValues_createsList() {
        props.addProperty("letters", "a");
        props.addProperty("letters", "b,c");

        String[] array = props.getStringArray("letters");
        assertEquals(3, array.length);
        assertEquals("a", array[0]);
        assertEquals("b", array[1]);
        assertEquals("c", array[2]);

        List list = props.getList("letters");
        assertEquals(3, list.size());
        assertEquals("a", list.get(0));

        Vector vector = props.getVector("letters");
        assertEquals(3, vector.size());
        assertEquals("b", vector.get(1));
    }

    // Tests boolean conversions for various string inputs
    @Test
    public void testGetBoolean_validAndInvalidInputs_returnsExpected() {
        props.addProperty("bool.true", "true");
        props.addProperty("bool.yes", "yes");
        props.addProperty("bool.on", "on");
        props.addProperty("bool.false", "false");
        props.addProperty("bool.no", "no");
        props.addProperty("bool.off", "off");

        assertTrue(props.getBoolean("bool.true"));
        assertTrue(props.getBoolean("bool.yes"));
        assertTrue(props.getBoolean("bool.on"));
        assertFalse(props.getBoolean("bool.false"));
        assertFalse(props.getBoolean("bool.no"));
        assertFalse(props.getBoolean("bool.off"));
        assertTrue(props.getBoolean("unknown.key", true));
    }

    // Tests NoSuchElementException when boolean key is absent
    @Test(expected = NoSuchElementException.class)
    public void testGetBoolean_missingKey_throwsNoSuchElementException() {
        props.getBoolean("nonExistingBool");
    }

    // Tests numeric type getters with valid inputs and defaults
    @Test
    public void testNumericGetters_validInputs_returnsCorrectTypes() {
        props.addProperty("num.byte", "10");
        props.addProperty("num.short", "100");
        props.addProperty("num.int", "1000");
        props.addProperty("num.long", "10000");
        props.addProperty("num.float", "10.5");
        props.addProperty("num.double", "20.5");

        assertEquals((byte) 10, props.getByte("num.byte"));
        assertEquals((short) 100, props.getShort("num.short"));
        assertEquals(1000, props.getInt("num.int"));
        assertEquals(1000, props.getInteger("num.int"));
        assertEquals(10000L, props.getLong("num.long"));
        assertEquals(10.5f, props.getFloat("num.float"), 0.001f);
        assertEquals(20.5d, props.getDouble("num.double"), 0.001d);

        assertEquals(42, props.getInt("missing.int", 42));
        assertEquals((byte) 5, props.getByte("missing.byte", (byte) 5));
        assertEquals((short) 6, props.getShort("missing.short", (short) 6));
        assertEquals(7L, props.getLong("missing.long", 7L));
        assertEquals(1.5f, props.getFloat("missing.float", 1.5f), 0.001f);
        assertEquals(2.5d, props.getDouble("missing.double", 2.5d), 0.001d);
    }

    // Tests NoSuchElementException when numeric key is absent
    @Test(expected = NoSuchElementException.class)
    public void testGetInt_missingKey_throwsNoSuchElementException() {
        props.getInt("nonExistingInt");
    }

    // Tests loading properties from an input stream with multiline and escaped commas
    @Test
    public void testLoad_validStream_parsesPropertiesCorrectly() throws IOException {
        String data = "# Comment line\n"
                + "simple = hello\n"
                + "multi = line1 \\\n  line2\n"
                + "escaped = first\\,second,third\n";
        InputStream in = new ByteArrayInputStream(data.getBytes("ISO-8859-1"));
        props.load(in);

        assertEquals("hello", props.getString("simple"));
        assertEquals("line1line2", props.getString("multi"));
        String[] escapedTokens = props.getStringArray("escaped");
        assertEquals(2, escapedTokens.length);
        assertEquals("first,second", escapedTokens[0]);
        assertEquals("third", escapedTokens[1]);
        assertTrue(props.isInitialized());
    }

    // Tests save and re-load functionality through streams
    @Test
    public void testSave_outputStream_savesAndReloadsProperly() throws IOException {
        props.setProperty("prop1", "val1");
        props.addProperty("prop2", "val2a");
        props.addProperty("prop2", "val2b");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        props.save(out, "Header Comment");

        ExtendedProperties loaded = new ExtendedProperties();
        loaded.load(new ByteArrayInputStream(out.toByteArray()));

        assertEquals("val1", loaded.getString("prop1"));
        List list = loaded.getList("prop2");
        assertEquals(2, list.size());
        assertEquals("val2a", list.get(0));
        assertEquals("val2b", list.get(1));
    }

    // Tests subset extraction by prefix
    @Test
    public void testSubset_matchingPrefix_returnsSubset() {
        props.setProperty("app.name", "TestApp");
        props.setProperty("app.version", "1.0");
        props.setProperty("db.host", "localhost");

        ExtendedProperties sub = props.subset("app");
        assertNotNull(sub);
        assertEquals("TestApp", sub.getString("name"));
        assertEquals("1.0", sub.getString("version"));
        assertNull(sub.getString("db.host"));

        assertNull(props.subset("nonexistent"));
    }

    // Tests getting keys matching a prefix
    @Test
    public void testGetKeys_prefixFilter_returnsMatchingKeys() {
        props.setProperty("server.host", "localhost");
        props.setProperty("server.port", "8080");
        props.setProperty("client.timeout", "5000");

        Iterator it = props.getKeys("server");
        List keys = new ArrayList();
        while (it.hasNext()) {
            keys.add(it.next());
        }
        assertEquals(2, keys.size());
        assertTrue(keys.contains("server.host"));
        assertTrue(keys.contains("server.port"));
        assertFalse(keys.contains("client.timeout"));
    }

    // Tests clearing property removes it from configuration and listed keys
    @Test
    public void testClearProperty_existingKey_removesKeyAndListing() {
        props.setProperty("keyA", "valA");
        props.setProperty("keyB", "valB");

        props.clearProperty("keyA");
        assertNull(props.getProperty("keyA"));

        Iterator it = props.getKeys();
        List keys = new ArrayList();
        while (it.hasNext()) {
            keys.add(it.next());
        }
        assertEquals(1, keys.size());
        assertEquals("keyB", keys.get(0));
    }

    // Tests convertProperties converts standard java.util.Properties to ExtendedProperties
    @Test
    public void testConvertProperties_standardProperties_convertsAll() {
        Properties standardProps = new Properties();
        standardProps.setProperty("p1", "v1");
        standardProps.setProperty("p2", "v2");

        ExtendedProperties converted = ExtendedProperties.convertProperties(standardProps);
        assertEquals("v1", converted.getString("p1"));
        assertEquals("v2", converted.getString("p2"));
    }

    // Tests combine method combines two ExtendedProperties
    @Test
    public void testCombine_anotherProperties_mergesValues() {
        props.setProperty("k1", "v1");
        props.setProperty("k2", "v2");

        ExtendedProperties other = new ExtendedProperties();
        other.setProperty("k2", "v2_override");
        other.setProperty("k3", "v3");

        props.combine(other);
        assertEquals("v1", props.getString("k1"));
        assertEquals("v2_override", props.getString("k2"));
        assertEquals("v3", props.getString("k3"));
    }

    // Tests getProperties parsing sub-properties key=value format
    @Test
    public void testGetProperties_subPropertyFormat_parsesAsProperties() {
        props.addProperty("subprops", "sub1=val1");
        props.addProperty("subprops", "sub2=val2");

        Properties parsed = props.getProperties("subprops");
        assertEquals("val1", parsed.getProperty("sub1"));
        assertEquals("val2", parsed.getProperty("sub2"));
    }

    // Tests getProperties throws IllegalArgumentException when token missing equals sign
    @Test(expected = IllegalArgumentException.class)
    public void testGetProperties_malformedToken_throwsIllegalArgumentException() {
        props.addProperty("badprops", "invalidTokenWithoutEquals");
        props.getProperties("badprops");
    }

    // Tests put, putAll, remove methods
    @Test
    public void testPutAndPutAllAndRemove_mapOperations_workCorrectly() {
        Map map = new HashMap();
        map.put("m1", "v1");
        map.put("m2", "v2");

        props.putAll(map);
        assertEquals("v1", props.getString("m1"));
        assertEquals("v2", props.getString("m2"));

        Object oldVal = props.put("m1", "v1_new");
        assertNotNull(oldVal);

        Object removed = props.remove("m2");
        assertEquals("v2", removed);
        assertNull(props.getProperty("m2"));
    }

    // Tests include property getter and setter
    @Test
    public void testSetInclude_customIncludeProperty_returnsCustomName() {
        assertEquals("include", props.getInclude());
        props.setInclude("import");
        assertEquals("import", props.getInclude());
        props.setInclude("");
        assertNull(props.getInclude());
    }
}