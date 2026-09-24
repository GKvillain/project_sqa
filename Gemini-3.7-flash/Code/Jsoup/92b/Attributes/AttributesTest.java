package org.jsoup.nodes;

import org.jsoup.parser.ParseSettings;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ConcurrentModificationException;
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

    // Tests adding and retrieving attributes by key
    @Test
    public void testPutAndGet_validKeyAndValue_returnsCorrectValue() {
        attributes.put("key1", "val1");
        attributes.put("Key2", "val2");

        assertEquals("val1", attributes.get("key1"));
        assertEquals("val2", attributes.get("Key2"));
        assertEquals("", attributes.get("nonexistent"));
    }

    // Tests case-insensitive get and put operations
    @Test
    public void testGetAndPutIgnoreCase_mixedCaseKeys_retrievesAndUpdatesCorrectly() {
        attributes.put("KEY", "val");

        assertEquals("val", attributes.getIgnoreCase("key"));
        assertTrue(attributes.hasKeyIgnoreCase("key"));
        assertFalse(attributes.hasKey("key"));

        attributes.putIgnoreCase("key", "newVal");
        assertEquals("newVal", attributes.get("key"));
        assertEquals(1, attributes.size());
    }

    // Tests boolean attribute addition and removal
    @Test
    public void testPut_booleanValue_addsAndRemovesCorrectly() {
        attributes.put("checked", true);
        assertTrue(attributes.hasKey("checked"));
        assertEquals("", attributes.get("checked"));

        attributes.put("checked", false);
        assertFalse(attributes.hasKey("checked"));
        assertEquals(0, attributes.size());
    }

    // Tests put with Attribute object
    @Test
    public void testPut_attributeObject_storesAttributeAndSetsParent() {
        Attribute attr = new Attribute("href", "http://example.com");
        attributes.put(attr);

        assertEquals("http://example.com", attributes.get("href"));
        assertEquals(1, attributes.size());
    }

    // Tests removing attributes with case-sensitive and case-insensitive keys
    @Test
    public void testRemoveAndRemoveIgnoreCase_existingKeys_removesCorrectly() {
        attributes.put("TestKey", "val");
        attributes.remove("testkey");
        assertEquals(1, attributes.size());

        attributes.removeIgnoreCase("testkey");
        assertEquals(0, attributes.size());
        assertFalse(attributes.hasKey("TestKey"));
    }

    // Tests capacity expansion when adding more elements than initial capacity
    @Test
    public void testCheckCapacity_moreThanInitialCapacity_growsCorrectly() {
        for (int i = 0; i < 10; i++) {
            attributes.put("key" + i, "val" + i);
        }

        assertEquals(10, attributes.size());
        for (int i = 0; i < 10; i++) {
            assertEquals("val" + i, attributes.get("key" + i));
        }
    }

    // Tests addAll with empty and non-empty incoming attributes
    @Test
    public void testAddAll_validIncomingAttributes_combinesCorrectly() {
        attributes.put("k1", "v1");

        Attributes incoming = new Attributes();
        attributes.addAll(incoming);
        assertEquals(1, attributes.size());

        incoming.put("k2", "v2");
        incoming.put("k1", "v1_updated");
        attributes.addAll(incoming);

        assertEquals(2, attributes.size());
        assertEquals("v1_updated", attributes.get("k1"));
        assertEquals("v2", attributes.get("k2"));
    }

    // Tests iterator and its remove method
    @Test
    public void testIterator_iterateAndRemove_removesElementsCorrectly() {
        attributes.put("k1", "v1");
        attributes.put("k2", "v2");

        Iterator<Attribute> it = attributes.iterator();
        assertTrue(it.hasNext());
        Attribute first = it.next();
        assertEquals("k1", first.getKey());
        assertEquals("v1", first.getValue());

        it.remove();
        assertEquals(1, attributes.size());
        assertFalse(attributes.hasKey("k1"));
        assertTrue(attributes.hasKey("k2"));
    }

    // Tests asList view conversion
    @Test
    public void testAsList_attributesSet_returnsUnmodifiableList() {
        attributes.put("k1", "v1");
        attributes.put("boolAttr", true);

        List<Attribute> list = attributes.asList();
        assertEquals(2, list.size());
        assertEquals("k1", list.get(0).getKey());
        assertEquals("boolAttr", list.get(1).getKey());
    }

    // Tests dataset view operations
    @Test
    public void testDataset_customDataAttributes_mapsCorrectly() {
        attributes.put("data-name", "Jsoup");
        attributes.put("other", "val");

        Map<String, String> dataset = attributes.dataset();
        assertEquals(1, dataset.size());
        assertEquals("Jsoup", dataset.get("name"));

        dataset.put("id", "123");
        assertEquals("123", attributes.get("data-id"));
    }

    // Tests normalize method to convert keys to lowercase
    @Test
    public void testNormalize_mixedCaseKeys_convertsToLowerCase() {
        attributes.put("TEST_KEY", "val");
        attributes.normalize();

        assertTrue(attributes.hasKey("test_key"));
        assertFalse(attributes.hasKey("TEST_KEY"));
        assertEquals("val", attributes.get("test_key"));
    }

    // Tests html generation and toString
    @Test
    public void testHtmlAndToString_attributesPresent_generatesHtml() {
        attributes.put("href", "http://example.com");
        attributes.put("required", true);

        String html = attributes.html();
        assertEquals(" href=\"http://example.com\" required", html);
        assertEquals(html, attributes.toString());
    }

    // Tests equals and hashCode methods
    @Test
    public void testEqualsAndHashCode_sameAndDifferentAttributes_returnsExpectedResults() {
        attributes.put("key", "val");

        Attributes same = new Attributes();
        same.put("key", "val");

        Attributes different = new Attributes();
        different.put("key", "val2");

        assertEquals(attributes, attributes);
        assertEquals(attributes, same);
        assertEquals(attributes.hashCode(), same.hashCode());

        assertNotEquals(attributes, different);
        assertNotEquals(attributes, null);
        assertNotEquals(attributes, "otherType");
    }

    // Tests clone functionality for deep copy
    @Test
    public void testClone_clonedAttributes_isIndependentCopy() {
        attributes.put("k1", "v1");
        Attributes clone = attributes.clone();

        assertEquals(attributes, clone);
        clone.put("k2", "v2");

        assertFalse(attributes.hasKey("k2"));
        assertTrue(clone.hasKey("k2"));
        assertNotEquals(attributes.size(), clone.size());
    }

    // Tests null key check throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testIndexOfKey_nullKey_throwsException() {
        attributes.hasKey(null);
    }

    // Tests add method directly
    @Test
    public void testAdd_newAttribute_addsCorrectly() {
        attributes.add("key1", "val1");
        attributes.add("key2", "val2");

        assertEquals(2, attributes.size());
        assertEquals("val1", attributes.get("key1"));
        assertEquals("val2", attributes.get("key2"));
    }

    // Tests hasDeclaredValueForKey and hasDeclaredValueForKeyIgnoreCase
    @Test
    public void testHasDeclaredValueForKey_variousKeys_returnsExpectedBoolean() {
        attributes.put("key1", "val1");
        attributes.put("checked", true);
        attributes.put("KEY2", "val2");

        assertTrue(attributes.hasDeclaredValueForKey("key1"));
        assertFalse(attributes.hasDeclaredValueForKey("checked"));
        assertFalse(attributes.hasDeclaredValueForKey("nonexistent"));
        assertFalse(attributes.hasDeclaredValueForKey("key2"));

        assertTrue(attributes.hasDeclaredValueForKeyIgnoreCase("key1"));
        assertTrue(attributes.hasDeclaredValueForKeyIgnoreCase("key2"));
        assertFalse(attributes.hasDeclaredValueForKeyIgnoreCase("checked"));
        assertFalse(attributes.hasDeclaredValueForKeyIgnoreCase("nonexistent"));
    }

    // Tests deduplicate method with ParseSettings preserve case and html lowercase
    @Test
    public void testDeduplicate_duplicateKeys_removesDuplicatesAndReturnsCount() {
        attributes.add("KEY", "val1");
        attributes.add("key", "val2");
        attributes.add("other", "val3");

        int dupes = attributes.deduplicate(ParseSettings.htmlDefault);
        assertEquals(1, dupes);
        assertEquals(2, attributes.size());
        assertTrue(attributes.hasKey("key"));

        Attributes preserveAttrs = new Attributes();
        preserveAttrs.add("KEY", "val1");
        preserveAttrs.add("key", "val2");
        int preserveDupes = preserveAttrs.deduplicate(ParseSettings.preserveCase);
        assertEquals(0, preserveDupes);
        assertEquals(2, preserveAttrs.size());
    }

    // Tests dataset entrySet iterator and setValue / remove
    @Test
    public void testDataset_entrySetIteratorAndOperations() {
        attributes.put("data-a", "1");
        attributes.put("data-b", "2");

        Map<String, String> dataset = attributes.dataset();
        Iterator<Map.Entry<String, String>> it = dataset.entrySet().iterator();
        assertTrue(it.hasNext());
        Map.Entry<String, String> entry = it.next();
        assertEquals("a", entry.getKey());
        assertEquals("1", entry.getValue());

        entry.setValue("100");
        assertEquals("100", attributes.get("data-a"));

        it.remove();
        assertFalse(attributes.hasKey("data-a"));
        assertEquals(1, attributes.size());
    }

    // Tests html rendering with XML syntax
    @Test
    public void testHtml_xmlSyntax_rendersEmptyValueForBoolean() throws IOException {
        attributes.put("disabled", true);
        Document.OutputSettings settings = new Document.OutputSettings().syntax(Document.OutputSettings.Syntax.xml);
        StringBuilder sb = new StringBuilder();
        attributes.html(sb, settings);

        assertEquals(" disabled=\"\"", sb.toString());
    }

    // Tests empty attributes behavior
    @Test
    public void testEmptyAttributes_operationsWorkWithoutException() {
        assertTrue(attributes.isEmpty());
        assertEquals(0, attributes.size());
        assertEquals("", attributes.html());
        assertEquals("", attributes.getIgnoreCase("key"));
        assertFalse(attributes.hasKeyIgnoreCase("key"));
        assertFalse(attributes.hasDeclaredValueForKey("key"));
        assertFalse(attributes.hasDeclaredValueForKeyIgnoreCase("key"));
        assertTrue(attributes.asList().isEmpty());

        Attributes clone = attributes.clone();
        assertEquals(attributes, clone);
        assertEquals(attributes.hashCode(), clone.hashCode());
    }

    // Tests put existing Attribute object replacing previous value
    @Test
    public void testPut_existingAttributeObject_replacesValue() {
        attributes.put("k", "v1");
        attributes.put(new Attribute("k", "v2"));

        assertEquals(1, attributes.size());
        assertEquals("v2", attributes.get("k"));
    }

    // Tests putIgnoreCase when key does not exist
    @Test
    public void testPutIgnoreCase_keyDoesNotExist_addsKey() {
        attributes.putIgnoreCase("NEW_KEY", "value");

        assertEquals(1, attributes.size());
        assertEquals("value", attributes.get("NEW_KEY"));
        assertEquals("value", attributes.getIgnoreCase("new_key"));
    }

    // Tests equals with different sizes and keys
    @Test
    public void testEquals_differentSizesAndKeys_returnsFalse() {
        attributes.put("k1", "v1");

        Attributes other = new Attributes();
        other.put("k1", "v1");
        other.put("k2", "v2");
        assertNotEquals(attributes, other);

        Attributes diffKey = new Attributes();
        diffKey.put("diff", "v1");
        assertNotEquals(attributes, diffKey);
    }
}