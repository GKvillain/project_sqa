package org.jsoup.nodes;

import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class AttributesTest {

    private Attributes attributes;

    @Before
    public void setUp() {
        attributes = new Attributes();
    }

    // Tests get and put with standard key-value pairs
    @Test
    public void testGet_existingKey_returnsValue() {
        attributes.put("key1", "val1");
        assertEquals("val1", attributes.get("key1"));
        assertEquals("", attributes.get("nonExistent"));
    }

    // Tests getIgnoreCase returns value regardless of key casing
    @Test
    public void testGetIgnoreCase_differentCaseKey_returnsValue() {
        attributes.put("TestKey", "val1");
        assertEquals("val1", attributes.getIgnoreCase("testkey"));
        assertEquals("val1", attributes.getIgnoreCase("TESTKEY"));
        assertEquals("", attributes.getIgnoreCase("nonExistent"));
    }

    // Tests get and getIgnoreCase when attributes map is empty/uninitialized
    @Test
    public void testGet_emptyAttributes_returnsEmptyString() {
        assertEquals("", attributes.get("key"));
        assertEquals("", attributes.getIgnoreCase("key"));
    }

    // Tests put with boolean value true sets boolean attribute and false removes it
    @Test
    public void testPut_booleanValue_setsAndRemovesAttribute() {
        attributes.put("disabled", true);
        assertTrue(attributes.hasKey("disabled"));

        attributes.put("disabled", false);
        assertFalse(attributes.hasKey("disabled"));
    }

    // Tests put with Attribute object
    @Test
    public void testPut_attributeObject_addsAttribute() {
        Attribute attr = new Attribute("href", "http://example.com");
        attributes.put(attr);
        assertEquals("http://example.com", attributes.get("href"));
    }

    // Tests remove removes key case-sensitively
    @Test
    public void testRemove_caseSensitiveKey_removesAttribute() {
        attributes.put("Key", "val");
        attributes.remove("key");
        assertTrue(attributes.hasKey("Key"));

        attributes.remove("Key");
        assertFalse(attributes.hasKey("Key"));
    }

    // Tests removeIgnoreCase removes key case-insensitively across multiple attributes
    @Test
    public void testRemoveIgnoreCase_multipleAttributes_removesKeyWithoutException() {
        attributes.put("One", "1");
        attributes.put("Two", "2");
        attributes.put("Three", "3");

        attributes.removeIgnoreCase("two");

        assertFalse(attributes.hasKeyIgnoreCase("two"));
        assertEquals(2, attributes.size());
        assertEquals("1", attributes.get("One"));
        assertEquals("3", attributes.get("Three"));
    }

    // Tests remove and removeIgnoreCase on uninitialized attributes
    @Test
    public void testRemove_emptyAttributes_noException() {
        attributes.remove("key");
        attributes.removeIgnoreCase("key");
        assertEquals(0, attributes.size());
    }

    // Tests hasKey and hasKeyIgnoreCase
    @Test
    public void testHasKey_caseSensitiveAndInsensitive_returnsCorrectBoolean() {
        assertFalse(attributes.hasKey("title"));
        assertFalse(attributes.hasKeyIgnoreCase("title"));

        attributes.put("Title", "Test");
        assertFalse(attributes.hasKey("title"));
        assertTrue(attributes.hasKey("Title"));
        assertTrue(attributes.hasKeyIgnoreCase("title"));
        assertTrue(attributes.hasKeyIgnoreCase("TITLE"));
    }

    // Tests size and addAll from another Attributes object
    @Test
    public void testAddAll_validIncomingAttributes_mergesAllAttributes() {
        assertEquals(0, attributes.size());

        Attributes other = new Attributes();
        other.put("k1", "v1");
        other.put("k2", "v2");

        attributes.addAll(other);
        assertEquals(2, attributes.size());
        assertEquals("v1", attributes.get("k1"));
        assertEquals("v2", attributes.get("k2"));

        attributes.addAll(new Attributes());
        assertEquals(2, attributes.size());
    }

    // Tests iterator on empty and populated attributes
    @Test
    public void testIterator_populatedAttributes_iteratesAll() {
        Iterator<Attribute> emptyIt = attributes.iterator();
        assertFalse(emptyIt.hasNext());

        attributes.put("k1", "v1");
        attributes.put("k2", "v2");

        Iterator<Attribute> it = attributes.iterator();
        assertTrue(it.hasNext());
        assertEquals("k1", it.next().getKey());
        assertTrue(it.hasNext());
        assertEquals("k2", it.next().getKey());
        assertFalse(it.hasNext());
    }

    // Tests asList representation of attributes
    @Test
    public void testAsList_emptyAndPopulated_returnsUnmodifiableList() {
        assertTrue(attributes.asList().isEmpty());

        attributes.put("k1", "v1");
        List<Attribute> list = attributes.asList();
        assertEquals(1, list.size());
        assertEquals("k1", list.get(0).getKey());
    }

    // Tests dataset view for custom HTML5 data-* attributes
    @Test
    public void testDataset_dataAttributes_managesDataPrefix() {
        attributes.put("data-name", "value");
        attributes.put("class", "button");

        Map<String, String> dataset = attributes.dataset();
        assertEquals(1, dataset.size());
        assertEquals("value", dataset.get("name"));

        dataset.put("custom", "val2");
        assertEquals("val2", attributes.get("data-custom"));
    }

    // Tests html and toString representations
    @Test
    public void testHtml_attributesPresent_formatsHtmlString() {
        assertEquals("", attributes.html());
        assertEquals("", attributes.toString());

        attributes.put("id", "main");
        attributes.put("class", "container");

        assertEquals(" id=\"main\" class=\"container\"", attributes.html());
        assertEquals(" id=\"main\" class=\"container\"", attributes.toString());
    }

    // Tests equals and hashCode contracts
    @Test
    public void testEqualsAndHashCode_equivalentAndDifferentObjects_conformsToContract() {
        assertTrue(attributes.equals(attributes));
        assertFalse(attributes.equals(null));
        assertFalse(attributes.equals("not an Attributes"));

        Attributes other = new Attributes();
        assertTrue(attributes.equals(other));
        assertEquals(attributes.hashCode(), other.hashCode());

        attributes.put("k1", "v1");
        assertFalse(attributes.equals(other));

        other.put("k1", "v1");
        assertTrue(attributes.equals(other));
        assertEquals(attributes.hashCode(), other.hashCode());
    }

    // Tests deep clone independence
    @Test
    public void testClone_clonedInstance_isIndependentCopy() {
        Attributes emptyClone = attributes.clone();
        assertEquals(0, emptyClone.size());

        attributes.put("key", "val");
        Attributes clone = attributes.clone();

        assertEquals(1, clone.size());
        assertEquals("val", clone.get("key"));

        clone.put("key", "newVal");
        assertEquals("val", attributes.get("key"));
        assertEquals("newVal", clone.get("key"));
    }

    // Tests exception path for empty or null key in get
    @Test(expected = IllegalArgumentException.class)
    public void testGet_emptyKey_throwsException() {
        attributes.get("");
    }

    // Tests exception path for null Attribute in put
    @Test(expected = IllegalArgumentException.class)
    public void testPut_nullAttribute_throwsException() {
        attributes.put((Attribute) null);
    }

    // Tests exception path for null key in remove
    @Test(expected = IllegalArgumentException.class)
    public void testRemove_nullKey_throwsException() {
        attributes.remove(null);
    }
}