package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

public class AttributesTest {

    // Tests basic put and get operations
    @Test
    public void testPutAndGet_keyValue_returnsCorrectValue() {
        Attributes attrs = new Attributes();
        attrs.put("key1", "value1");
        assertEquals("value1", attrs.get("key1"));
    }

    // Tests get with non-existent key returns empty string
    @Test
    public void testGet_nonExistentKey_returnsEmptyString() {
        Attributes attrs = new Attributes();
        assertEquals("", attrs.get("nonexistent"));
    }

    // Tests hasKey returns true for existing key
    @Test
    public void testHasKey_existingKey_returnsTrue() {
        Attributes attrs = new Attributes();
        attrs.put("key", "val");
        assertTrue(attrs.hasKey("key"));
    }

    // Tests hasKey returns false for non-existing key
    @Test
    public void testHasKey_nonExistentKey_returnsFalse() {
        Attributes attrs = new Attributes();
        assertFalse(attrs.hasKey("key"));
    }

    // Tests remove removes attribute by key
    @Test
    public void testRemove_existingKey_attributeRemoved() {
        Attributes attrs = new Attributes();
        attrs.put("key", "val");
        attrs.remove("key");
        assertFalse(attrs.hasKey("key"));
        assertEquals("", attrs.get("key"));
    }

    // Tests size returns correct count
    @Test
    public void testSize_afterAddingAttributes_returnsCorrectCount() {
        Attributes attrs = new Attributes();
        assertEquals(0, attrs.size());
        attrs.put("a", "1");
        assertEquals(1, attrs.size());
        attrs.put("b", "2");
        assertEquals(2, attrs.size());
    }

    // Tests put with boolean true adds attribute with null value
    @Test
    public void testPut_booleanTrue_attributeAdded() {
        Attributes attrs = new Attributes();
        attrs.put("checked", true);
        assertTrue(attrs.hasKey("checked"));
        assertEquals("", attrs.get("checked"));
    }

    // Tests put with boolean false removes attribute
    @Test
    public void testPut_booleanFalse_attributeRemoved() {
        Attributes attrs = new Attributes();
        attrs.put("checked", "true");
        attrs.put("checked", false);
        assertFalse(attrs.hasKey("checked"));
    }

    // Tests addAll copies attributes from another Attributes
    @Test
    public void testAddAll_otherAttributes_attributesCopied() {
        Attributes attrs = new Attributes();
        attrs.put("key1", "val1");
        Attributes other = new Attributes();
        other.put("key2", "val2");
        attrs.addAll(other);
        assertEquals(2, attrs.size());
        assertEquals("val1", attrs.get("key1"));
        assertEquals("val2", attrs.get("key2"));
    }

    // Tests getIgnoreCase returns value for case-insensitive key
    @Test
    public void testGetIgnoreCase_mixedCaseKey_returnsValue() {
        Attributes attrs = new Attributes();
        attrs.put("Key", "Value");
        assertEquals("Value", attrs.getIgnoreCase("key"));
        assertEquals("Value", attrs.getIgnoreCase("KEY"));
    }

    // Tests indexOfKey returns correct index
    @Test
    public void testIndexOfKey_existingKey_returnsIndex() {
        Attributes attrs = new Attributes();
        attrs.put("a", "1");
        attrs.put("b", "2");
        int index = attrs.indexOfKey("b");
        assertTrue(index >= 0);
    }

    // Tests indexOfKey returns -1 for non-existing key
    @Test
    public void testIndexOfKey_nonExistentKey_returnsNotFound() {
        Attributes attrs = new Attributes();
        attrs.put("a", "1");
        assertEquals(Attributes.NotFound, attrs.indexOfKey("b"));
    }

    // Tests removeIgnoreCase removes attribute by case-insensitive key
    @Test
    public void testRemoveIgnoreCase_mixedCaseKey_attributeRemoved() {
        Attributes attrs = new Attributes();
        attrs.put("Key", "val");
        attrs.removeIgnoreCase("key");
        assertFalse(attrs.hasKey("Key"));
    }

    // Tests hasKeyIgnoreCase returns true for case-insensitive match
    @Test
    public void testHasKeyIgnoreCase_mixedCaseKey_returnsTrue() {
        Attributes attrs = new Attributes();
        attrs.put("Key", "val");
        assertTrue(attrs.hasKeyIgnoreCase("key"));
    }

    // Tests dataset returns map view
    @Test
    public void testDataset_putDataAttribute_returnsOldValue() {
        Attributes attrs = new Attributes();
        attrs.put("data-test", "123");
        attrs.put("data-other", "abc");
        assertEquals(2, attrs.dataset().size());
        assertEquals("123", attrs.dataset().get("test"));
    }

    // Tests asList returns unmodifiable list
    @Test
    public void testAsList_multipleAttributes_returnsListView() {
        Attributes attrs = new Attributes();
        attrs.put("a", "1");
        attrs.put("b", null);
        assertEquals(2, attrs.asList().size());
    }

    // Tests equals and hashCode
    @Test
    public void testEquals_sameAttributes_returnsTrue() {
        Attributes attrs1 = new Attributes();
        attrs1.put("key", "val");
        Attributes attrs2 = new Attributes();
        attrs2.put("key", "val");
        assertEquals(attrs1, attrs2);
        assertEquals(attrs1.hashCode(), attrs2.hashCode());
    }

    // Tests clone produces independent copy
    @Test
    public void testClone_attributes_clonedIndependently() {
        Attributes attrs = new Attributes();
        attrs.put("key", "val");
        Attributes clone = attrs.clone();
        assertEquals("val", clone.get("key"));
        clone.put("new", "value");
        assertFalse(attrs.hasKey("new"));
    }

    // Tests normalize lowercases keys
    @Test
    public void testNormalize_mixedCaseKeys_keysLowercased() {
        Attributes attrs = new Attributes();
        attrs.put("Key1", "val1");
        attrs.put("KEY2", "val2");
        attrs.normalize();
        assertTrue(attrs.hasKey("key1"));
        assertTrue(attrs.hasKey("key2"));
        assertFalse(attrs.hasKey("Key1"));
        assertFalse(attrs.hasKey("KEY2"));
    }

    // Tests putIgnoreCase updates value for case-insensitive key
    @Test
    public void testPutIgnoreCase_existingKeyIgnoringCase_updatesValue() {
        Attributes attrs = new Attributes();
        attrs.put("Key", "old");
        attrs.putIgnoreCase("key", "new");
        assertEquals("new", attrs.get("Key"));
    }
}