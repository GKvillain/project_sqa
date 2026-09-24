package org.jsoup.nodes;

import org.junit.Test;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class AttributesTest {

    // Tests getting and putting case-sensitive attribute values
    @Test
    public void testPutAndGet_caseSensitive_returnsCorrectValue() {
        Attributes attrs = new Attributes();
        attrs.put("key", "value");
        attrs.put("KEY", "VALUE");

        assertEquals(2, attrs.size());
        assertEquals("value", attrs.get("key"));
        assertEquals("VALUE", attrs.get("KEY"));
        assertEquals("", attrs.get("nonexistent"));
    }

    // Tests getIgnoreCase and putIgnoreCase methods
    @Test
    public void testPutAndGetIgnoreCase_caseInsensitive_updatesAndRetrieves() {
        Attributes attrs = new Attributes();
        attrs.put("key", "val1");
        assertEquals("val1", attrs.getIgnoreCase("KEY"));

        attrs.putIgnoreCase("KEY", "val2");
        assertEquals(1, attrs.size());
        assertEquals("val2", attrs.get("KEY"));
        assertEquals("val2", attrs.getIgnoreCase("key"));
    }

    // Tests put boolean value for setting and removing attributes
    @Test
    public void testPut_booleanValue_setsOrRemoves() {
        Attributes attrs = new Attributes();
        attrs.put("disabled", true);
        assertTrue(attrs.hasKeyIgnoreCase("disabled"));
        assertEquals("", attrs.get("disabled"));

        attrs.put("disabled", false);
        assertFalse(attrs.hasKeyIgnoreCase("disabled"));
        assertEquals(0, attrs.size());
    }

    // Tests put with Attribute object instance
    @Test
    public void testPut_attributeObject_storesAndSetsParent() {
        Attributes attrs = new Attributes();
        Attribute attr = new Attribute("id", "main");
        attrs.put(attr);

        assertEquals(1, attrs.size());
        assertEquals("main", attrs.get("id"));
        assertSame(attrs, attr.parent);
    }

    // Tests removing attribute case-sensitively
    @Test
    public void testRemove_caseSensitive_removesOnlyExactMatch() {
        Attributes attrs = new Attributes();
        attrs.put("key", "value");
        attrs.put("KEY", "VALUE");

        attrs.remove("key");
        assertEquals(1, attrs.size());
        assertFalse(attrs.hasKey("key"));
        assertTrue(attrs.hasKey("KEY"));

        attrs.remove("nonexistent");
        assertEquals(1, attrs.size());
    }

    // Tests removing attribute case-insensitively
    @Test
    public void testRemoveIgnoreCase_caseInsensitive_removesMatching() {
        Attributes attrs = new Attributes();
        attrs.put("KEY", "VALUE");

        attrs.removeIgnoreCase("key");
        assertEquals(0, attrs.size());
        assertFalse(attrs.hasKey("KEY"));
    }

    // Tests hasKey and hasKeyIgnoreCase
    @Test
    public void testHasKey_caseSensitiveAndInsensitive_returnsCorrectBoolean() {
        Attributes attrs = new Attributes();
        attrs.put("href", "http://example.com");

        assertTrue(attrs.hasKey("href"));
        assertFalse(attrs.hasKey("HREF"));
        assertTrue(attrs.hasKeyIgnoreCase("HREF"));
        assertFalse(attrs.hasKey("src"));
        assertFalse(attrs.hasKeyIgnoreCase("src"));
    }

    // Tests capacity expansion when adding beyond initial capacity
    @Test
    public void testCheckCapacity_addingMultipleItems_growsArrayCorrectly() {
        Attributes attrs = new Attributes();
        for (int i = 0; i < 10; i++) {
            attrs.put("key" + i, "val" + i);
        }
        assertEquals(10, attrs.size());
        for (int i = 0; i < 10; i++) {
            assertEquals("val" + i, attrs.get("key" + i));
        }
    }

    // Tests addAll with empty and non-empty incoming attributes
    @Test
    public void testAddAll_validAndEmptyAttributes_mergesCorrectly() {
        Attributes attrs1 = new Attributes();
        attrs1.put("k1", "v1");

        Attributes empty = new Attributes();
        attrs1.addAll(empty);
        assertEquals(1, attrs1.size());

        Attributes attrs2 = new Attributes();
        attrs2.put("k2", "v2");
        attrs2.put("k1", "v1-updated");

        attrs1.addAll(attrs2);
        assertEquals(2, attrs1.size());
        assertEquals("v1-updated", attrs1.get("k1"));
        assertEquals("v2", attrs1.get("k2"));
    }

    // Tests iterator and its remove method
    @Test
    public void testIterator_traversalAndRemove_removesElementCorrectly() {
        Attributes attrs = new Attributes();
        attrs.put("k1", "v1");
        attrs.put("k2", "v2");

        Iterator<Attribute> it = attrs.iterator();
        assertTrue(it.hasNext());
        Attribute first = it.next();
        assertEquals("k1", first.getKey());
        assertEquals("v1", first.getValue());

        it.remove();
        assertEquals(1, attrs.size());
        assertFalse(attrs.hasKey("k1"));
        assertTrue(attrs.hasKey("k2"));

        assertTrue(it.hasNext());
        Attribute second = it.next();
        assertEquals("k2", second.getKey());
        assertFalse(it.hasNext());
    }

    // Tests asList representation of attributes
    @Test
    public void testAsList_withStandardAndBooleanAttributes_returnsList() {
        Attributes attrs = new Attributes();
        attrs.put("checked", true);
        attrs.put("class", "btn");

        List<Attribute> list = attrs.asList();
        assertEquals(2, list.size());
        assertEquals("checked", list.get(0).getKey());
        assertEquals("class", list.get(1).getKey());
        assertEquals("btn", list.get(1).getValue());
    }

    // Tests dataset view for custom data- attributes
    @Test
    public void testDataset_customDataAttributes_getsAndSetsCorrectly() {
        Attributes attrs = new Attributes();
        attrs.put("data-name", "Jsoup");
        attrs.put("id", "123");

        Map<String, String> dataset = attrs.dataset();
        assertEquals(1, dataset.size());
        assertEquals("Jsoup", dataset.get("name"));

        String oldVal = dataset.put("name", "JsoupNew");
        assertEquals("Jsoup", oldVal);
        assertEquals("JsoupNew", attrs.get("data-name"));

        dataset.put("version", "1.0");
        assertEquals("1.0", attrs.get("data-version"));
        assertEquals(2, dataset.size());
    }

    // Tests html generation for boolean attribute rendering
    @Test
    public void testHtml_booleanAttribute_rendersCorrectly() {
        Attributes attrs = new Attributes();
        attrs.put("async", true);
        assertEquals(" async", attrs.html());
    }

    // Tests html generation for boolean attribute with empty string or name value
    @Test
    public void testHtml_booleanAttributeWithValue_rendersKeyOnly() {
        Attributes attrs = new Attributes();
        attrs.put("async", "");
        attrs.put("checked", "checked");
        assertEquals(" async checked", attrs.html());
    }

    // Tests html generation for standard attributes with special characters
    @Test
    public void testHtml_standardAttribute_escapesValuesCorrectly() {
        Attributes attrs = new Attributes();
        attrs.put("href", "http://example.com?a=1&b=2");
        attrs.put("title", "Hello \"World\"");

        assertEquals(" href=\"http://example.com?a=1&amp;b=2\" title=\"Hello &quot;World&quot;\"", attrs.html());
        assertEquals(attrs.html(), attrs.toString());
    }

    // Tests normalize method to lowercase all keys
    @Test
    public void testNormalize_mixedCaseKeys_lowercasesAllKeys() {
        Attributes attrs = new Attributes();
        attrs.put("TITLE", "test");
        attrs.put("Href", "url");

        attrs.normalize();
        assertTrue(attrs.hasKey("title"));
        assertTrue(attrs.hasKey("href"));
        assertFalse(attrs.hasKey("TITLE"));
    }

    // Tests clone, equals, and hashCode methods
    @Test
    public void testCloneAndEqualsAndHashCode_equalObjects_returnsExpectedResults() {
        Attributes attrs = new Attributes();
        attrs.put("key", "value");

        Attributes clone = attrs.clone();
        assertEquals(attrs, clone);
        assertEquals(attrs.hashCode(), clone.hashCode());

        clone.put("key2", "value2");
        assertNotEquals(attrs, clone);
        assertNotEquals(attrs.hashCode(), clone.hashCode());

        assertNotEquals(attrs, null);
        assertNotEquals(attrs, "string");
        assertEquals(attrs, attrs);
    }

    // Tests exception on null key passed to indexOfKey
    @Test(expected = IllegalArgumentException.class)
    public void testIndexOfKey_nullKey_throwsIllegalArgumentException() {
        Attributes attrs = new Attributes();
        attrs.get(null);
    }
}