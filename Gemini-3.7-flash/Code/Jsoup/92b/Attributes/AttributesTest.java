package org.jsoup.nodes;

import org.jsoup.parser.ParseSettings;
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

    // Tests adding and retrieving a standard key-value attribute
    @Test
    public void testPutAndGet_standardKeyValue_returnsValue() {
        attributes.put("href", "https://example.com");
        assertEquals("https://example.com", attributes.get("href"));
        assertTrue(attributes.hasKey("href"));
        assertEquals(1, attributes.size());
    }

    // Tests retrieving a non-existent key
    @Test
    public void testGet_nonExistentKey_returnsEmptyString() {
        assertEquals("", attributes.get("nonexistent"));
        assertFalse(attributes.hasKey("nonexistent"));
    }

    // Tests overwriting an existing attribute with the same key
    @Test
    public void testPut_existingKey_overwritesValue() {
        attributes.put("class", "btn");
        attributes.put("class", "btn-primary");

        assertEquals("btn-primary", attributes.get("class"));
        assertEquals(1, attributes.size());
    }

    // Tests case-insensitive get and hasKey
    @Test
    public void testGetAndHasKeyIgnoreCase_differentCase_returnsTrueAndValue() {
        attributes.put("TITLE", "Test Title");

        assertTrue(attributes.hasKeyIgnoreCase("title"));
        assertFalse(attributes.hasKey("title"));
        assertEquals("Test Title", attributes.getIgnoreCase("title"));
        assertEquals("", attributes.get("title"));
    }

    // Tests putIgnoreCase updating the key and value when case changes
    @Test
    public void testPutIgnoreCase_caseChanged_updatesKeyAndValue() {
        attributes.putIgnoreCase("title", "val1");
        attributes.putIgnoreCase("TITLE", "val2");

        assertEquals(1, attributes.size());
        assertEquals("val2", attributes.get("TITLE"));
        assertEquals("", attributes.get("title"));
    }

    // Tests put with boolean true creates a boolean attribute (null value representation)
    @Test
    public void testPut_booleanTrue_setsBooleanAttribute() {
        attributes.put("required", true);

        assertTrue(attributes.hasKey("required"));
        assertEquals("", attributes.get("required"));
        assertEquals(" required", attributes.html());
    }

    // Tests put with boolean false removes the attribute
    @Test
    public void testPut_booleanFalse_removesAttribute() {
        attributes.put("disabled", true);
        assertTrue(attributes.hasKey("disabled"));

        attributes.put("disabled", false);
        assertFalse(attributes.hasKey("disabled"));
        assertEquals(0, attributes.size());
    }

    // Tests put with Attribute object
    @Test
    public void testPut_attributeObject_addsAndSetsParent() {
        Attribute attr = new Attribute("id", "main");
        attributes.put(attr);

        assertEquals("main", attributes.get("id"));
        assertEquals(attributes, attr.parent);
    }

    // Tests removing an attribute by key (case-sensitive)
    @Test
    public void testRemove_caseSensitive_removesOnlyExactMatch() {
        attributes.put("key", "val1");
        attributes.put("KEY", "val2");

        attributes.remove("key");

        assertFalse(attributes.hasKey("key"));
        assertTrue(attributes.hasKey("KEY"));
        assertEquals(1, attributes.size());
    }

    // Tests removing an attribute by key (case-insensitive)
    @Test
    public void testRemoveIgnoreCase_differentCase_removesAttribute() {
        attributes.put("KEY", "val");
        attributes.removeIgnoreCase("key");

        assertFalse(attributes.hasKeyIgnoreCase("KEY"));
        assertEquals(0, attributes.size());
    }

    // Tests addAll with another Attributes collection
    @Test
    public void testAddAll_multipleAttributes_combinesAttributes() {
        attributes.put("a", "1");
        attributes.put("b", "2");

        Attributes extra = new Attributes();
        extra.put("b", "3");
        extra.put("c", "4");

        attributes.addAll(extra);

        assertEquals(3, attributes.size());
        assertEquals("1", attributes.get("a"));
        assertEquals("3", attributes.get("b"));
        assertEquals("4", attributes.get("c"));
    }

    // Tests addAll with empty source does nothing
    @Test
    public void testAddAll_emptyAttributes_noChange() {
        attributes.put("a", "1");
        attributes.addAll(new Attributes());

        assertEquals(1, attributes.size());
    }

    // Tests iterator traversal and remove
    @Test
    public void testIterator_iterateAndRemove_removesElementsCorrectly() {
        attributes.put("a", "1");
        attributes.put("b", "2");
        attributes.put("c", "3");

        Iterator<Attribute> iter = attributes.iterator();
        assertTrue(iter.hasNext());
        Attribute first = iter.next();
        assertEquals("a", first.getKey());
        iter.remove();

        assertEquals(2, attributes.size());
        assertFalse(attributes.hasKey("a"));
        assertTrue(attributes.hasKey("b"));
    }

    // Tests asList view of attributes including boolean attributes
    @Test
    public void testAsList_mixedAttributes_returnsImmutableList() {
        attributes.put("key", "val");
        attributes.put("disabled", true);

        List<Attribute> list = attributes.asList();
        assertEquals(2, list.size());
        assertEquals("key", list.get(0).getKey());
        assertEquals("disabled", list.get(1).getKey());
    }

    // Tests dataset view operations for data-* attributes
    @Test
    public void testDataset_putAndGet_operatesOnPrefixedAttributes() {
        Map<String, String> dataset = attributes.dataset();
        dataset.put("custom-id", "12345");

        assertTrue(attributes.hasKey("data-custom-id"));
        assertEquals("12345", attributes.get("data-custom-id"));
        assertEquals(1, dataset.size());

        String oldVal = dataset.put("custom-id", "67890");
        assertEquals("12345", oldVal);
        assertEquals("67890", dataset.get("custom-id"));
    }

    // Tests dataset iterator only iterates over data-* attributes
    @Test
    public void testDataset_iterator_filtersOnlyDataAttributes() {
        attributes.put("class", "main");
        attributes.put("data-first", "1");
        attributes.put("id", "container");
        attributes.put("data-second", "2");

        Map<String, String> dataset = attributes.dataset();
        assertEquals(2, dataset.size());

        Iterator<Map.Entry<String, String>> it = dataset.entrySet().iterator();
        assertTrue(it.hasNext());
        Map.Entry<String, String> entry1 = it.next();
        assertEquals("first", entry1.getKey());
        assertEquals("1", entry1.getValue());
    }

    // Tests normalize lowercases all attribute keys
    @Test
    public void testNormalize_mixedCaseKeys_convertsToLowercase() {
        attributes.put("SRC", "img.png");
        attributes.put("Alt", "An Image");

        attributes.normalize();

        assertTrue(attributes.hasKey("src"));
        assertTrue(attributes.hasKey("alt"));
        assertFalse(attributes.hasKey("SRC"));
        assertEquals("img.png", attributes.get("src"));
        assertEquals("An Image", attributes.get("alt"));
    }

    // Tests html serialization and toString
    @Test
    public void testHtml_standardAndSpecialCharacters_escapesCorrectly() {
        attributes.put("href", "http://example.com?a=1&b=2");
        attributes.put("required", true);

        String html = attributes.html();
        assertEquals(" href=\"http://example.com?a=1&amp;b=2\" required", html);
        assertEquals(html, attributes.toString());
    }

    // Tests equals and hashCode contracts
    @Test
    public void testEqualsAndHashCode_equivalentAndDifferentAttributes_satisfyContract() {
        attributes.put("a", "1");
        attributes.put("b", "2");

        Attributes clone = attributes.clone();

        assertEquals(attributes, clone);
        assertEquals(attributes.hashCode(), clone.hashCode());

        clone.put("c", "3");
        assertNotEquals(attributes, clone);
        assertNotEquals(attributes, null);
        assertNotEquals(attributes, "string");
    }

    // Tests clone creates a deep, independent copy
    @Test
    public void testClone_modifyingClone_doesNotAffectOriginal() {
        attributes.put("key", "original");
        Attributes clone = attributes.clone();

        clone.put("key", "modified");
        clone.put("extra", "value");

        assertEquals("original", attributes.get("key"));
        assertFalse(attributes.hasKey("extra"));
        assertEquals(1, attributes.size());
        assertEquals(2, clone.size());
    }

    // Tests exception path for null key on indexOfKey / get
    @Test(expected = IllegalArgumentException.class)
    public void testGet_nullKey_throwsException() {
        attributes.get(null);
    }

    // --- New Tests Added Below ---

    // Tests isEmpty method on empty and populated attributes
    @Test
    public void testIsEmpty_emptyAndPopulated_returnsCorrectStatus() {
        assertTrue(attributes.isEmpty());
        attributes.put("key", "val");
        assertFalse(attributes.isEmpty());
    }

    // Tests add method which allows duplicate keys
    @Test
    public void testAdd_duplicateKeys_appendsDirectly() {
        attributes.add("class", "one");
        attributes.add("class", "two");

        assertEquals(2, attributes.size());
        assertEquals("one", attributes.get("class"));
    }

    // Tests hasDeclaredValueForKey and hasDeclaredValueForKeyIgnoreCase
    @Test
    public void testHasDeclaredValueForKey_booleanAndValued_returnsExpected() {
        attributes.put("required", true);
        attributes.put("TITLE", "MyTitle");

        assertFalse(attributes.hasDeclaredValueForKey("required"));
        assertFalse(attributes.hasDeclaredValueForKeyIgnoreCase("required"));

        assertTrue(attributes.hasDeclaredValueForKey("TITLE"));
        assertTrue(attributes.hasDeclaredValueForKeyIgnoreCase("title"));
        assertFalse(attributes.hasDeclaredValueForKey("title"));

        assertFalse(attributes.hasDeclaredValueForKey("nonexistent"));
        assertFalse(attributes.hasDeclaredValueForKeyIgnoreCase("nonexistent"));
    }

    // Tests capacity expansion by adding attributes exceeding initial size
    @Test
    public void testCapacityExpansion_addingManyAttributes_growsCorrectly() {
        for (int i = 0; i < 20; i++) {
            attributes.put("key" + i, "val" + i);
        }

        assertEquals(20, attributes.size());
        for (int i = 0; i < 20; i++) {
            assertEquals("val" + i, attributes.get("key" + i));
        }
    }

    // Tests deduplication of keys according to ParseSettings
    @Test
    public void testDeduplicate_casePreserveAndCaseInsensitive_removesDuplicates() {
        attributes.add("key", "1");
        attributes.add("KEY", "2");
        attributes.add("key", "3");

        int dropped = attributes.deduplicate(new ParseSettings(true, true));
        assertEquals(1, dropped);
        assertEquals(2, attributes.size());

        dropped = attributes.deduplicate(new ParseSettings(false, false));
        assertEquals(1, dropped);
        assertEquals(1, attributes.size());
        assertEquals("1", attributes.getIgnoreCase("key"));
    }

    // Tests html serialization in XML syntax output mode
    @Test
    public void testHtml_xmlSyntax_formatsBooleanAttributesAsEmptyValues() throws IOException {
        attributes.put("checked", true);
        attributes.put("id", "chk1");

        Document.OutputSettings xmlSettings = new Document.OutputSettings().syntax(Document.OutputSettings.Syntax.xml);
        StringBuilder accum = new StringBuilder();
        attributes.html(accum, xmlSettings);

        assertEquals(" checked=\"\" id=\"chk1\"", accum.toString());
    }

    // Tests dataset entry removal via dataset and dataset iterator
    @Test
    public void testDataset_removeAndIteratorRemove_removesFromAttributes() {
        attributes.put("data-a", "1");
        attributes.put("data-b", "2");
        attributes.put("other", "3");

        Map<String, String> dataset = attributes.dataset();
        dataset.remove("a");

        assertFalse(attributes.hasKey("data-a"));
        assertEquals(2, attributes.size());

        Iterator<Map.Entry<String, String>> it = dataset.entrySet().iterator();
        assertTrue(it.hasNext());
        Map.Entry<String, String> entry = it.next();
        assertEquals("b", entry.getKey());
        it.remove();

        assertFalse(attributes.hasKey("data-b"));
        assertTrue(attributes.hasKey("other"));
        assertEquals(1, attributes.size());
    }

    // Tests setting value on Attribute object updates parent Attributes
    @Test
    public void testAttributeSetValue_updatesParentAttributes() {
        attributes.put("name", "initial");
        Attribute attr = attributes.asList().get(0);
        attr.setValue("updated");

        assertEquals("updated", attributes.get("name"));
    }

    // Tests remove and removeIgnoreCase with non-existent keys
    @Test
    public void testRemove_nonExistentKeys_doesNothing() {
        attributes.put("key", "val");
        attributes.remove("other");
        attributes.removeIgnoreCase("other");

        assertEquals(1, attributes.size());
        assertTrue(attributes.hasKey("key"));
    }
}