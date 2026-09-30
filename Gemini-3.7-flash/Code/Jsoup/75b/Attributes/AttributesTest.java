package org.jsoup.nodes;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
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

    // Tests normal put and get by key
    @Test
    public void testPutAndGet_validKeyAndValue_returnsValue() {
        attributes.put("href", "https://example.com");
        assertEquals("https://example.com", attributes.get("href"));
        assertEquals(1, attributes.size());

        // Overwrite existing key
        attributes.put("href", "https://jsoup.org");
        assertEquals("https://jsoup.org", attributes.get("href"));
        assertEquals(1, attributes.size());
    }

    // Tests case-insensitive get and put
    @Test
    public void testGetIgnoreCase_mixedCaseKey_returnsValue() {
        attributes.put("class", "main");
        assertEquals("main", attributes.getIgnoreCase("CLASS"));
        assertEquals("main", attributes.getIgnoreCase("Class"));
        assertEquals("", attributes.getIgnoreCase("id"));
    }

    // Tests boolean attribute put handling
    @Test
    public void testPutBoolean_trueAndFalse_setsAndRemovesAttribute() {
        attributes.put("disabled", true);
        assertTrue(attributes.hasKey("disabled"));
        assertEquals("", attributes.get("disabled"));

        attributes.put("disabled", false);
        assertFalse(attributes.hasKey("disabled"));
        assertEquals(0, attributes.size());
    }

    // Tests put with Attribute object
    @Test
    public void testPut_attributeObject_addsAndSetsParent() {
        Attribute attr = new Attribute("title", "tooltip");
        attributes.put(attr);
        assertEquals("tooltip", attributes.get("title"));
        assertEquals(1, attributes.size());
        assertTrue(attributes.hasKey("title"));
    }

    // Tests removing attribute by exact case key
    @Test
    public void testRemove_existingKey_removesAttribute() {
        attributes.put("id", "header");
        attributes.put("class", "primary");
        attributes.remove("id");

        assertFalse(attributes.hasKey("id"));
        assertTrue(attributes.hasKey("class"));
        assertEquals(1, attributes.size());

        // Remove non-existing key does not throw
        attributes.remove("nonexistent");
        assertEquals(1, attributes.size());
    }

    // Tests removing attribute by case-insensitive key
    @Test
    public void testRemoveIgnoreCase_mixedCaseKey_removesAttribute() {
        attributes.put("data-test", "val");
        attributes.removeIgnoreCase("DATA-TEST");

        assertFalse(attributes.hasKey("data-test"));
        assertEquals(0, attributes.size());
    }

    // Tests key presence check
    @Test
    public void testHasKeyAndHasKeyIgnoreCase_variousKeys_returnsCorrectResult() {
        attributes.put("SRC", "image.png");

        assertTrue(attributes.hasKey("SRC"));
        assertFalse(attributes.hasKey("src"));
        assertTrue(attributes.hasKeyIgnoreCase("src"));
        assertTrue(attributes.hasKeyIgnoreCase("SRC"));
        assertFalse(attributes.hasKeyIgnoreCase("alt"));
    }

    // Tests adding all attributes from another instance
    @Test
    public void testAddAll_multipleAttributes_mergesCorrectly() {
        attributes.put("k1", "v1");
        Attributes incoming = new Attributes();
        incoming.put("k2", "v2");
        incoming.put("k3", "v3");

        attributes.addAll(incoming);
        assertEquals(3, attributes.size());
        assertEquals("v1", attributes.get("k1"));
        assertEquals("v2", attributes.get("k2"));
        assertEquals("v3", attributes.get("k3"));

        // Empty incoming attributes
        attributes.addAll(new Attributes());
        assertEquals(3, attributes.size());
    }

    // Tests iterator traversal and removal
    @Test
    public void testIterator_iterateAndRemove_modifiesAttributes() {
        attributes.put("a", "1");
        attributes.put("b", "2");
        attributes.put("c", "3");

        Iterator<Attribute> it = attributes.iterator();
        assertTrue(it.hasNext());
        Attribute first = it.next();
        assertEquals("a", first.getKey());
        it.remove();

        assertEquals(2, attributes.size());
        assertFalse(attributes.hasKey("a"));
        assertTrue(attributes.hasKey("b"));
        assertTrue(attributes.hasKey("c"));
    }

    // Tests asList view of attributes
    @Test
    public void testAsList_returnsUnmodifiableAttributeList() {
        attributes.put("key1", "val1");
        attributes.put("disabled", true);

        List<Attribute> list = attributes.asList();
        assertEquals(2, list.size());
        assertEquals("key1", list.get(0).getKey());
        assertEquals("val1", list.get(0).getValue());
        assertEquals("disabled", list.get(1).getKey());
    }

    // Tests HTML5 dataset view
    @Test
    public void testDataset_customDataAttributes_managesDataPrefix() {
        attributes.put("data-id", "123");
        attributes.put("data-name", "jsoup");
        attributes.put("class", "bold");

        Map<String, String> dataset = attributes.dataset();
        assertEquals(2, dataset.size());
        assertEquals("123", dataset.get("id"));
        assertEquals("jsoup", dataset.get("name"));

        dataset.put("name", "updated");
        assertEquals("updated", attributes.get("data-name"));
    }

    // Tests HTML serialization for boolean and value attributes
    @Test
    public void testHtml_booleanAndValuedAttributes_generatesCorrectHtml() {
        attributes.put("id", "main");
        attributes.put("disabled", true);
        attributes.put("checked", "checked");

        String html = attributes.html();
        assertEquals(" id=\"main\" disabled checked", html);
    }

    // Tests HTML serialization under XML syntax
    @Test
    public void testHtml_xmlSyntax_doesNotCollapseBooleanAttributes() throws IOException {
        attributes.put("disabled", true);
        attributes.put("id", "main");

        Document.OutputSettings settings = new Document("").outputSettings().syntax(Document.OutputSettings.Syntax.xml);
        StringBuilder accum = new StringBuilder();
        attributes.html(accum, settings);

        assertEquals(" disabled=\"\" id=\"main\"", accum.toString());
    }

    // Tests normalize method to lowercase all keys
    @Test
    public void testNormalize_mixedCaseKeys_lowercasesAllKeys() {
        attributes.put("SRC", "img.jpg");
        attributes.put("HREF", "link.html");
        attributes.normalize();

        assertTrue(attributes.hasKey("src"));
        assertTrue(attributes.hasKey("href"));
        assertFalse(attributes.hasKey("SRC"));
        assertFalse(attributes.hasKey("HREF"));
    }

    // Tests equals and hashCode contract
    @Test
    public void testEqualsAndHashCode_sameAndDifferentAttributes_behavesCorrectly() {
        attributes.put("a", "1");
        attributes.put("b", "2");

        Attributes same = new Attributes();
        same.put("a", "1");
        same.put("b", "2");

        Attributes diff = new Attributes();
        diff.put("a", "1");

        assertEquals(attributes, attributes);
        assertEquals(attributes, same);
        assertEquals(attributes.hashCode(), same.hashCode());
        assertNotEquals(attributes, diff);
        assertNotEquals(attributes, null);
        assertNotEquals(attributes, "string");
    }

    // Tests clone functionality creates a deep independent copy
    @Test
    public void testClone_clonedAttributes_isIndependent() {
        attributes.put("key", "val");
        Attributes cloned = attributes.clone();

        assertEquals(attributes, cloned);
        cloned.put("key", "newVal");

        assertEquals("val", attributes.get("key"));
        assertEquals("newVal", cloned.get("key"));
    }

    // Tests capacity expansion when adding many attributes
    @Test
    public void testCheckCapacity_manyAttributes_expandsProperly() {
        for (int i = 0; i < 20; i++) {
            attributes.put("key" + i, "val" + i);
        }
        assertEquals(20, attributes.size());
        for (int i = 0; i < 20; i++) {
            assertEquals("val" + i, attributes.get("key" + i));
        }
    }

    // Tests exception on null key
    @Test(expected = IllegalArgumentException.class)
    public void testIndexOfKey_nullKey_throwsIllegalArgumentException() {
        attributes.get(null);
    }
}