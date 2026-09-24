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

    // Tests adding simple single and multiple comma-separated properties
    @Test
    public void testAddProperty_commaSeparated_splitsIntoList() {
        props.addProperty("single", "value1");
        assertEquals("value1", props.getString("single"));

        props.addProperty("multi", "val1, val2, val3");
        List list = props.getList("multi");
        assertEquals(3, list.size());
        assertEquals("val1", list.get(0));
        assertEquals("val2", list.get(1));
        assertEquals("val3", list.get(2));

        String[] array = props.getStringArray("multi");
        assertEquals(3, array.length);
        assertEquals("val1", array[0]);
    }

    // Tests escaping of comma within tokens
    @Test
    public void testAddProperty_escapedComma_preservedInSingleToken() {
        props.addProperty("escaped", "val1\\,withcomma, val2");
        List list = props.getList("escaped");
        assertEquals(2, list.size());
        assertEquals("val1,withcomma", list.get(0));
        assertEquals("val2", list.get(1));
    }

    // Tests setProperty replacing existing values
    @Test
    public void testSetProperty_overwritesExistingValue() {
        props.addProperty("key", "first");
        props.addProperty("key", "second");
        assertEquals(2, props.getList("key").size());

        props.setProperty("key", "replacement");
        assertEquals("replacement", props.getString("key"));
        assertEquals(1, props.getStringArray("key").length);
    }

    // Tests clearProperty and remove functionality
    @Test
    public void testClearPropertyAndRemove_removesKeyAndMaintainsOrder() {
        props.setProperty("key1", "val1");
        props.setProperty("key2", "val2");

        Object removedVal = props.remove("key1");
        assertEquals("val1", removedVal);
        assertNull(props.getProperty("key1"));
        assertFalse(props.containsKey("key1"));

        props.clearProperty("key2");
        assertNull(props.getProperty("key2"));
        assertFalse(props.getKeys().hasNext());
    }

    // Tests interpolation with variable substitution
    @Test
    public void testInterpolate_substitutesVariablesCorrectly() {
        props.setProperty("base.dir", "/opt/app");
        props.setProperty("log.dir", "${base.dir}/logs");
        props.setProperty("archive.dir", "${log.dir}/archive");

        assertEquals("/opt/app/logs", props.getString("log.dir"));
        assertEquals("/opt/app/logs/archive", props.getString("archive.dir"));
    }

    // Tests loop detection in interpolation throwing IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testInterpolate_circularReference_throwsIllegalStateException() {
        props.setProperty("keyA", "${keyB}");
        props.setProperty("keyB", "${keyA}");
        props.getString("keyA");
    }

    // Tests subset extraction with given prefix
    @Test
    public void testSubset_matchingPrefix_returnsSubProperties() {
        props.setProperty("db.host", "localhost");
        props.setProperty("db.port", "3306");
        props.setProperty("other.prop", "val");

        ExtendedProperties sub = props.subset("db");
        assertNotNull(sub);
        assertEquals("localhost", sub.getString("host"));
        assertEquals("3306", sub.getString("port"));
        assertNull(sub.getString("other.prop"));

        ExtendedProperties emptySub = props.subset("nonexistent");
        assertNull(emptySub);
    }

    // Tests converting java.util.Properties including parent default properties
    @Test
    public void testConvertProperties_withDefaults_convertsAllEntries() {
        Properties parentProps = new Properties();
        parentProps.setProperty("defaultKey", "defaultValue");

        Properties childProps = new Properties(parentProps);
        childProps.setProperty("childKey", "childValue");

        ExtendedProperties converted = ExtendedProperties.convertProperties(childProps);
        assertEquals("childValue", converted.getString("childKey"));
        assertEquals("defaultValue", converted.getString("defaultKey"));
    }

    // Tests boolean conversions with various valid string representations
    @Test
    public void testGetBoolean_variousFormats_parsesProperly() {
        props.setProperty("b.true", "true");
        props.setProperty("b.on", "on");
        props.setProperty("b.yes", "yes");
        props.setProperty("b.false", "false");
        props.setProperty("b.off", "off");
        props.setProperty("b.no", "no");

        assertTrue(props.getBoolean("b.true"));
        assertTrue(props.getBoolean("b.on"));
        assertTrue(props.getBoolean("b.yes"));
        assertFalse(props.getBoolean("b.false"));
        assertFalse(props.getBoolean("b.off"));
        assertFalse(props.getBoolean("b.no"));
        assertTrue(props.getBoolean("b.missing", true));
    }

    // Tests boolean retrieval throwing exception when missing and no default provided
    @Test(expected = NoSuchElementException.class)
    public void testGetBoolean_missingKey_throwsNoSuchElementException() {
        props.getBoolean("missing.boolean");
    }

    // Tests numeric primitive type getters (byte, short, int, long, float, double)
    @Test
    public void testGetNumericTypes_validValuesAndDefaults_returnsExpected() {
        props.setProperty("num.byte", "8");
        props.setProperty("num.short", "16");
        props.setProperty("num.int", "32");
        props.setProperty("num.long", "64");
        props.setProperty("num.float", "1.25");
        props.setProperty("num.double", "2.5");

        assertEquals((byte) 8, props.getByte("num.byte"));
        assertEquals((short) 16, props.getShort("num.short"));
        assertEquals(32, props.getInt("num.int"));
        assertEquals(32, props.getInteger("num.int", 0));
        assertEquals(64L, props.getLong("num.long"));
        assertEquals(1.25f, props.getFloat("num.float"), 0.0001f);
        assertEquals(2.5d, props.getDouble("num.double"), 0.0001d);

        // Test fallback defaults on missing keys
        assertEquals((byte) 1, props.getByte("absent", (byte) 1));
        assertEquals((short) 2, props.getShort("absent", (short) 2));
        assertEquals(3, props.getInt("absent", 3));
        assertEquals(4L, props.getLong("absent", 4L));
        assertEquals(5.5f, props.getFloat("absent", 5.5f), 0.0001f);
        assertEquals(6.6d, props.getDouble("absent", 6.6d), 0.0001d);
    }

    // Tests getList and getVector copying behavior and defaults
    @Test
    public void testGetVectorAndGetList_returnsIndependentCopies() {
        props.addProperty("items", "item1, item2");
        Vector vec = props.getVector("items");
        List lst = props.getList("items");

        assertEquals(2, vec.size());
        assertEquals(2, lst.size());

        // Modifying copies should not mutate underlying properties
        vec.clear();
        lst.clear();
        assertEquals(2, props.getList("items").size());

        // Default handling
        Vector defaultVec = new Vector();
        defaultVec.add("def");
        assertEquals(1, props.getVector("nonexistent", defaultVec).size());
    }

    // Tests getProperties parsing key=value tokens
    @Test
    public void testGetProperties_subProperties_returnsPropertiesObject() {
        props.addProperty("subprops", "k1=v1, k2=v2");
        Properties parsed = props.getProperties("subprops");
        assertEquals("v1", parsed.getProperty("k1"));
        assertEquals("v2", parsed.getProperty("k2"));
    }

    // Tests getProperties malformed token throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testGetProperties_malformedToken_throwsIllegalArgumentException() {
        props.addProperty("badprops", "invalid_no_equal");
        props.getProperties("badprops");
    }

    // Tests load and save methods with round-trip stream
    @Test
    public void testLoadAndSave_streamRoundTrip_restoresProperties() throws IOException {
        String configData = "# Comment line\n"
                + "app.name = TestApp\n"
                + "app.version = 1.0.0\n"
                + "multiline.val = first line \\\n"
                + "                second line\n";

        InputStream in = new ByteArrayInputStream(configData.getBytes("ISO-8859-1"));
        props.load(in);
        assertTrue(props.isInitialized());
        assertEquals("TestApp", props.getString("app.name"));
        assertEquals("1.0.0", props.getString("app.version"));
        assertEquals("first linesecond line", props.getString("multiline.val"));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        props.save(out, "Header Comment");
        String saved = out.toString("ISO-8859-1");
        assertTrue(saved.contains("Header Comment"));
        assertTrue(saved.contains("app.name=TestApp"));
    }

    // Tests put and putAll method implementations
    @Test
    public void testPutAndPutAll_mapsCorrectly() {
        props.put("p1", "v1");
        assertEquals("v1", props.getString("p1"));

        Map map = new HashMap();
        map.put("p2", "v2");
        map.put("p3", "v3");
        props.putAll(map);

        assertEquals("v2", props.getString("p2"));
        assertEquals("v3", props.getString("p3"));

        ExtendedProperties other = new ExtendedProperties();
        other.setProperty("p4", "v4");
        props.putAll(other);
        assertEquals("v4", props.getString("p4"));
    }

    // Tests combine method merging another ExtendedProperties
    @Test
    public void testCombine_overwritesAndMerges() {
        props.setProperty("k1", "orig1");
        props.setProperty("k2", "orig2");

        ExtendedProperties other = new ExtendedProperties();
        other.setProperty("k2", "new2");
        other.setProperty("k3", "new3");

        props.combine(other);
        assertEquals("orig1", props.getString("k1"));
        assertEquals("new2", props.getString("k2"));
        assertEquals("new3", props.getString("k3"));
    }

    // Tests getKeys with prefix filter
    @Test
    public void testGetKeys_withPrefix_filtersCorrectly() {
        props.setProperty("prefix.a", "1");
        props.setProperty("prefix.b", "2");
        props.setProperty("other.c", "3");

        Iterator iter = props.getKeys("prefix");
        List matchingKeys = new ArrayList();
        while (iter.hasNext()) {
            matchingKeys.add(iter.next());
        }

        assertEquals(2, matchingKeys.size());
        assertTrue(matchingKeys.contains("prefix.a"));
        assertTrue(matchingKeys.contains("prefix.b"));
        assertFalse(matchingKeys.contains("other.c"));
    }

    // Tests setInclude and getInclude configuration
    @Test
    public void testSetAndGetInclude_updatesIncludeProperty() {
        assertEquals("include", props.getInclude());
        props.setInclude("import");
        assertEquals("import", props.getInclude());
        props.setInclude("");
        assertNull(props.getInclude());
    }
}