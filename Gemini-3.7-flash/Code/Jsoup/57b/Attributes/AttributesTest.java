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
        attributes.put("href", "http://example.com");
        assertEquals("http://example.com", attributes.get("href"));
        assertEquals("", attributes.get("nonexistent"));
    }

    // Tests get with empty attributes map
    @Test
    public void testGet_emptyAttributes_returnsEmptyString() {
        assertEquals("", attributes.get("key"));
    }

    // Tests case-insensitive get
    @Test
    public void testGetIgnoreCase_mixedCaseKey_returnsValue() {
        attributes.put("HREF", "http://example.com");
        assertEquals("http://example.com", attributes.getIgnoreCase("href"));
        assertEquals("http://example.com", attributes.getIgnoreCase("HREF"));
        assertEquals("", attributes.getIgnoreCase("src"));
    }

    // Tests boolean attribute addition and removal via put
    @Test
    public void testPut_booleanAttribute_addsOrRemoves() {
        attributes.put("disabled", true);
        assertTrue(attributes.hasKey("disabled"));

        attributes.put("disabled", false);
        assertFalse(attributes.hasKey("disabled"));
    }

    // Tests case-sensitive remove
    @Test
    public void testRemove_caseSensitive_removesExactMatchOnly() {
        attributes.put("class", "main");
        attributes.put("Class", "header");

        attributes.remove("class");
        assertFalse(attributes.hasKey("class"));
        assertTrue(attributes.hasKey("Class"));
    }

    // Tests case-insensitive remove with multiple elements (regression test for ConcurrentModificationException)
    @Test
    public void testRemoveIgnoreCase_multipleAttributes_removesMatchingKeys() {
        attributes.put("one", "1");
        attributes.put("Two", "2");
        attributes.put("three", "3");

        attributes.removeIgnoreCase("two");
        assertFalse(attributes.hasKeyIgnoreCase("two"));
        assertEquals(2, attributes.size());
        assertEquals("1", attributes.get("one"));
        assertEquals("3", attributes.get("three"));
    }

    // Tests hasKey and hasKeyIgnoreCase behavior
    @Test
    public void testHasKey_andHasKeyIgnoreCase_validatesExistence() {
        assertFalse(attributes.hasKey("title"));
        assertFalse(attributes.hasKeyIgnoreCase("title"));

        attributes.put("Title", "Test");
        assertFalse(attributes.hasKey("title"));
        assertTrue(attributes.hasKey("Title"));
        assertTrue(attributes.hasKeyIgnoreCase("title"));
        assertTrue(attributes.hasKeyIgnoreCase("TITLE"));
    }

    // Tests size of attributes
    @Test
    public void testSize_emptyAndPopulated_returnsCorrectCount() {
        assertEquals(0, attributes.size());
        attributes.put("key1", "val1");
        attributes.put("key2", "val2");
        assertEquals(2, attributes.size());
    }

    // Tests addAll merging attributes from another instance
    @Test
    public void testAddAll_validIncomingAttributes_mergesCorrectly() {
        attributes.put("a", "1");

        Attributes incoming = new Attributes();
        incoming.put("b", "2");
        incoming.put("a", "override");

        attributes.addAll(incoming);
        assertEquals(2, attributes.size());
        assertEquals("override", attributes.get("a"));
        assertEquals("2", attributes.get("b"));
    }

    // Tests iteration over attributes
    @Test
    public void testIterator_iteratesAllAttributes() {
        attributes.put("a", "1");
        attributes.put("b", "2");

        int count = 0;
        for (Attribute attr : attributes) {
            assertNotNull(attr);
            count++;
        }
        assertEquals(2, count);
    }

    // Tests asList representation
    @Test
    public void testAsList_returnsUnmodifiableList() {
        assertTrue(attributes.asList().isEmpty());

        attributes.put("key", "value");
        List<Attribute> list = attributes.asList();
        assertEquals(1, list.size());
        assertEquals("key", list.get(0).getKey());
        assertEquals("value", list.get(0).getValue());
    }

    // Tests HTML dataset functionality
    @Test
    public void testDataset_customDataAttributes_managesDataPrefix() {
        attributes.put("data-name", "jsoup");
        attributes.put("class", "test");

        Map<String, String> dataset = attributes.dataset();
        assertEquals(1, dataset.size());
        assertEquals("jsoup", dataset.get("name"));

        dataset.put("version", "1.0");
        assertEquals("1.0", attributes.get("data-version"));
    }

    // Tests HTML generation
    @Test
    public void testHtml_generatesExpectedHtmlString() {
        assertEquals("", attributes.html());

        attributes.put("id", "main");
        attributes.put("class", "container");
        assertEquals(" id=\"main\" class=\"container\"", attributes.html());
        assertEquals(attributes.html(), attributes.toString());
    }

    // Tests equals and hashCode contracts
    @Test
    public void testEqualsAndHashCode_sameAndDifferentContent_evaluatesCorrectly() {
        Attributes attr1 = new Attributes();
        Attributes attr2 = new Attributes();

        assertEquals(attr1, attr2);
        assertEquals(attr1.hashCode(), attr2.hashCode());

        attr1.put("k", "v");
        assertNotEquals(attr1, attr2);

        attr2.put("k", "v");
        assertEquals(attr1, attr2);
        assertEquals(attr1.hashCode(), attr2.hashCode());

        assertNotEquals(attr1, null);
        assertNotEquals(attr1, "different_type");
    }

    // Tests clone functionality creates an independent deep copy
    @Test
    public void testClone_createsIndependentCopy() {
        attributes.put("key", "original");
        Attributes cloned = attributes.clone();

        assertEquals(attributes, cloned);
        cloned.put("key", "modified");
        assertEquals("original", attributes.get("key"));
        assertEquals("modified", cloned.get("key"));
    }

    // Tests exception on empty key get
    @Test(expected = IllegalArgumentException.class)
    public void testGet_emptyKey_throwsException() {
        attributes.get("");
    }

    // Tests exception on null attribute put
    @Test(expected = IllegalArgumentException.class)
    public void testPut_nullAttribute_throwsException() {
        attributes.put((Attribute) null);
    }
}