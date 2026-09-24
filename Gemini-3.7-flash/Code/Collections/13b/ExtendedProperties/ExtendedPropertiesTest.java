package org.apache.commons.collections;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

    // Tests adding simple properties and retrieving strings
    @Test
    public void testGetString_validKey_returnsValue() {
        props.addProperty("key1", "value1");
        assertEquals("value1", props.getString("key1"));
        assertEquals("value1", props.getString("key1", "defaultValue"));
    }

    // Tests retrieving string with default value when key does not exist
    @Test
    public void testGetString_missingKey_returnsDefaultValue() {
        assertNull(props.getString("nonExisting"));
        assertEquals("default", props.getString("nonExisting", "default"));
    }

    // Tests multi-value comma-separated strings tokenization and vector/list retrieval
    @Test
    public void testGetVector_commaSeparatedValues_returnsListTokens() {
        props.addProperty("tokens", "a, b, c\\,d");
        Vector vector = props.getVector("tokens");
        assertEquals(3, vector.size());
        assertEquals("a", vector.get(0));
        assertEquals("b", vector.get(1));
        assertEquals("c,d", vector.get(2));

        List list = props.getList("tokens");
        assertEquals(3, list.size());
        assertEquals("a", list.get(0));
        assertEquals("b", list.get(1));
        assertEquals("c,d", list.get(2));
    }

    // Tests string array extraction from property
    @Test
    public void testGetStringArray_commaSeparatedValues_returnsArray() {
        props.addProperty("arrayKey", "one, two");
        String[] arr = props.getStringArray("arrayKey");
        assertEquals(2, arr.length);
        assertEquals("one", arr[0]);
        assertEquals("two", arr[1]);
    }

    // Tests setProperty overwriting existing values
    @Test
    public void testSetProperty_existingKey_overwritesValue() {
        props.addProperty("key", "val1");
        props.addProperty("key", "val2");
        props.setProperty("key", "val3");
        assertEquals("val3", props.getString("key"));
    }

    // Tests clearProperty removes key and updates keys listing
    @Test
    public void testClearProperty_existingKey_removesKey() {
        props.addProperty("key1", "val1");
        props.addProperty("key2", "val2");
        props.clearProperty("key1");
        assertNull(props.getString("key1"));
        assertFalse(props.containsKey("key1"));

        Iterator it = props.getKeys();
        assertTrue(it.hasNext());
        assertEquals("key2", it.next());
        assertFalse(it.hasNext());
    }

    // Tests getKeys with prefix matching
    @Test
    public void testGetKeys_withPrefix_returnsMatchingKeys() {
        props.addProperty("db.host", "localhost");
        props.addProperty("db.port", "3306");
        props.addProperty("app.name", "testApp");

        Iterator it = props.getKeys("db");
        int count = 0;
        while (it.hasNext()) {
            String k = (String) it.next();
            assertTrue(k.startsWith("db"));
            count++;
        }
        assertEquals(2, count);
    }

    // Tests subset extraction by prefix
    @Test
    public void testSubset_matchingPrefix_returnsSubProperties() {
        props.addProperty("database.host", "localhost");
        props.addProperty("database.port", "5432");
        props.addProperty("other", "val");

        ExtendedProperties sub = props.subset("database");
        assertNotNull(sub);
        assertEquals("localhost", sub.getString("host"));
        assertEquals("5432", sub.getString("port"));
        assertNull(sub.getString("other"));
    }

    // Tests interpolation of property values
    @Test
    public void testInterpolate_variablesPresent_substitutesVariables() {
        props.addProperty("baseDir", "/home/user");
        props.addProperty("subDir", "${baseDir}/data");
        assertEquals("/home/user/data", props.getString("subDir"));
    }

    // Tests cyclic interpolation throwing IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testInterpolate_circularReference_throwsIllegalStateException() {
        props.addProperty("loopA", "${loopB}");
        props.addProperty("loopB", "${loopA}");
        props.getString("loopA");
    }

    // Tests boolean conversions with various true/false textual representations
    @Test
    public void testGetBoolean_validBooleanStrings_returnsCorrectValues() {
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
        assertTrue(props.getBoolean("nonExisting", true));
    }

    // Tests getBoolean throwing NoSuchElementException when key does not exist
    @Test(expected = NoSuchElementException.class)
    public void testGetBoolean_missingKey_throwsNoSuchElementException() {
        props.getBoolean("missingKey");
    }

    // Tests numeric conversions (Integer, Long, Byte, Short, Float, Double)
    @Test
    public void testNumericGetters_validValues_returnsPrimitives() {
        props.addProperty("num.int", "123");
        props.addProperty("num.long", "4567890123");
        props.addProperty("num.byte", "12");
        props.addProperty("num.short", "1234");
        props.addProperty("num.float", "12.34");
        props.addProperty("num.double", "56.78");

        assertEquals(123, props.getInt("num.int"));
        assertEquals(123, props.getInteger("num.int", 0));
        assertEquals(4567890123L, props.getLong("num.long"));
        assertEquals((byte) 12, props.getByte("num.byte"));
        assertEquals((short) 1234, props.getShort("num.short"));
        assertEquals(12.34f, props.getFloat("num.float"), 0.001f);
        assertEquals(56.78d, props.getDouble("num.double"), 0.001d);
    }

    // Tests getProperties parsing key=value tokens
    @Test
    public void testGetProperties_tokenKeyValue_returnsPropertiesObject() {
        props.addProperty("dbprops", "user=admin, password=secret");
        Properties p = props.getProperties("dbprops");
        assertEquals("admin", p.getProperty("user"));
        assertEquals("secret", p.getProperty("password"));
    }

    // Tests getProperties with malformed token throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testGetProperties_invalidToken_throwsIllegalArgumentException() {
        props.addProperty("badProp", "onlyKeyWithoutEqualSign");
        props.getProperties("badProp");
    }

    // Tests combine method merging another ExtendedProperties
    @Test
    public void testCombine_anotherProperties_mergesEntries() {
        props.addProperty("key1", "orig1");
        ExtendedProperties other = new ExtendedProperties();
        other.addProperty("key1", "new1");
        other.addProperty("key2", "val2");

        props.combine(other);
        assertEquals("new1", props.getString("key1"));
        assertEquals("val2", props.getString("key2"));
    }

    // Tests load and save methods using streams
    @Test
    public void testLoadAndSave_validStream_readsAndWritesProperties() throws IOException {
        String data = "app.title = My Application \\\n"
                + "            Continued\n"
                + "app.version = 1.0.0\n"
                + "# comment line\n"
                + "app.items = first, second\n";

        ByteArrayInputStream in = new ByteArrayInputStream(data.getBytes("ISO-8859-1"));
        props.load(in);

        assertTrue(props.isInitialized());
        assertEquals("My Application Continued", props.getString("app.title"));
        assertEquals("1.0.0", props.getString("app.version"));
        Vector items = props.getVector("app.items");
        assertEquals(2, items.size());
        assertEquals("first", items.get(0));
        assertEquals("second", items.get(1));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        props.save(out, "Header Comment");
        String saved = new String(out.toByteArray(), "ISO-8859-1");
        assertTrue(saved.contains("Header Comment"));
        assertTrue(saved.contains("app.version=1.0.0"));
    }

    // Tests convertProperties with parent defaults
    @Test
    public void testConvertProperties_standardPropertiesWithDefaults_convertsAll() {
        Properties defaultProps = new Properties();
        defaultProps.setProperty("defaultKey", "defaultValue");
        Properties testProps = new Properties(defaultProps);
        testProps.setProperty("childKey", "childValue");

        ExtendedProperties converted = ExtendedProperties.convertProperties(testProps);
        assertEquals("childValue", converted.getString("childKey"));
        assertEquals("defaultValue", converted.getString("defaultKey"));
    }

    // Tests put, putAll, and remove map methods
    @Test
    public void testPutPutAllRemove_mapMethods_properlyMutatesProperties() {
        props.put("p1", "v1");
        assertEquals("v1", props.getString("p1"));

        ExtendedProperties mapProps = new ExtendedProperties();
        mapProps.put("p2", "v2");
        mapProps.put("p3", "v3");

        props.putAll(mapProps);
        assertEquals("v2", props.getString("p2"));
        assertEquals("v3", props.getString("p3"));

        Object removed = props.remove("p2");
        assertEquals("v2", removed);
        assertNull(props.getString("p2"));
    }

    // Tests include property name setter and getter
    @Test
    public void testIncludePropertyName_getAndSet_handlesValues() {
        assertEquals("include", props.getInclude());
        props.setInclude("import");
        assertEquals("import", props.getInclude());
        props.setInclude(null);
        assertNull(props.getInclude());
    }
}