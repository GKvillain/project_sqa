package org.jsoup.nodes;

import org.junit.Test;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import static org.junit.Assert.*;

/**
 * JUnit 4 test class for org.jsoup.nodes.Attributes.
 * Designed to detect the Defects4J bug (75b) and achieve reasonable coverage.
 */
public class AttributesTest {

    // Basic put/get
    @Test
    public void testPutAndGet_normalCase_returnsValue() {
        Attributes attrs = new Attributes();
        attrs.put("key", "value");
        assertEquals("value", attrs.get("key"));
    }

    // Get non-existent key returns empty string
    @Test
    public void testGet_nonExistentKey_returnsEmptyString() {
        Attributes attrs = new Attributes();
        assertEquals("", attrs.get("nonexistent"));
    }

    // GetIgnoreCase
    @Test
    public void testGetIgnoreCase_differentCase_returnsValue() {
        Attributes attrs = new Attributes();
        attrs.put("Key", "value");
        assertEquals("value", attrs.getIgnoreCase("key"));
    }

    // Put overwrites existing key
    @Test
    public void testPut_existingKey_overwritesValue() {
        Attributes attrs = new Attributes();
        attrs.put("key", "old");
        attrs.put("key", "new");
        assertEquals("new", attrs.get("key"));
    }

    // PutIgnoreCase with different case updates key name
    @Test
    public void testPutIgnoreCase_differentKeyCase_updatesKey() {
        Attributes attrs = new Attributes();
        attrs.put("key", "value");
        attrs.putIgnoreCase("KEY", "newvalue");
        assertEquals("newvalue", attrs.get("KEY"));
        assertTrue(attrs.hasKey("KEY"));
        assertFalse(attrs.hasKey("key"));
    }

    // Put with boolean true adds attribute with null value
    @Test
    public void testPutBooleanTrue_addsBooleanAttribute() {
        Attributes attrs = new Attributes();
        attrs.put("disabled", true);
        assertTrue(attrs.hasKey("disabled"));
        // null value treated as empty string via checkNotNull
        assertEquals("", attrs.get("disabled"));
    }

    // Put with boolean false removes attribute
    @Test
    public void testPutBooleanFalse_removesAttribute() {
        Attributes attrs = new Attributes();
        attrs.put("disabled", "true");
        attrs.put("disabled", false);
        assertFalse(attrs.hasKey("disabled"));
    }

    // HasKeyIgnoreCase
    @Test
    public void testHasKeyIgnoreCase_differentCase_returnsTrue() {
        Attributes attrs = new Attributes();
        attrs.put("Key", "value");
        assertTrue(attrs.hasKeyIgnoreCase("key"));
        assertFalse(attrs.hasKey("key"));
    }

    // Remove
    @Test
    public void testRemove_existingKey_removesAttribute() {
        Attributes attrs = new Attributes();
        attrs.put("key", "value");
        attrs.remove("key");
        assertFalse(attrs.hasKey("key"));
        assertEquals("", attrs.get("key"));
    }

    // RemoveIgnoreCase
    @Test
    public void testRemoveIgnoreCase_differentCase_removesAttribute() {
        Attributes attrs = new Attributes();
        attrs.put("Key", "value");
        attrs.removeIgnoreCase("key");
        assertFalse(attrs.hasKeyIgnoreCase("key"));
    }

    // Size
    @Test
    public void testSize_afterMultiplePuts_correctCount() {
        Attributes attrs = new Attributes();
        assertEquals(0, attrs.size());
        attrs.put("a", "1");
        attrs.put("b", "2");
        assertEquals(2, attrs.size());
        attrs.remove("a");
        assertEquals(1, attrs.size());
    }

    // Iterator - basic iteration
    @Test
    public void testIterator_iterateOverAttributes_returnsCorrectKeys() {
        Attributes attrs = new Attributes();
        attrs.put("key1", "val1");
        attrs.put("key2", "val2");
        Iterator<Attribute> it = attrs.iterator();
        assertTrue(it.hasNext());
        Attribute a1 = it.next();
        assertEquals("key1", a1.getKey());
        assertTrue(it.hasNext());
        Attribute a2 = it.next();
        assertEquals("key2", a2.getKey());
        assertFalse(it.hasNext());
    }

    // Iterator remove
    @Test
    public void testIteratorRemove_removesCurrentAttribute() {
        Attributes attrs = new Attributes();
        attrs.put("key1", "val1");
        attrs.put("key2", "val2");
        Iterator<Attribute> it = attrs.iterator();
        it.next(); // key1
        it.remove();
        assertEquals(1, attrs.size());
        assertFalse(attrs.hasKey("key1"));
        assertTrue(attrs.hasKey("key2"));
    }

    // asList returns unmodifiable list with correct types
    @Test
    public void testAsList_containsBooleanAndRegular() {
        Attributes attrs = new Attributes();
        attrs.put("regular", "val");
        attrs.put("checked", true); // null value -> BooleanAttribute
        List<Attribute> list = attrs.asList();
        assertEquals(2, list.size());
        // first is regular Attribute
        assertTrue(list.get(0) instanceof Attribute);
        assertFalse(list.get(0) instanceof BooleanAttribute);
        // second is BooleanAttribute (deprecated but still used)
        assertTrue(list.get(1) instanceof BooleanAttribute);
    }

    // addAll should not modify source attributes' parent (potential defect)
    @Test
    public void testAddAll_sourceAttributesParentNotModified() {
        Attributes source = new Attributes();
        source.put("src", "value");
        // source attributes have parent = this (the source object) after put(Attribute) internally?
        // Actually put(String,String) does not set parent. So initially parent is null.
        // We need to add via put(Attribute) to set parent.
        // Let's create an Attribute directly and add it to source.
        Attribute attr = new Attribute("src", "value");
        source.put(attr); // sets parent = source
        // Now source's internal attribute has parent = source
        // Now create a new Attributes and addAll from source
        Attributes dest = new Attributes();
        dest.addAll(source);
        // After addAll, the original attr's parent should remain source, not dest
        // But the bug (Jsoup-75) is that addAll modifies the source attribute's parent to dest.
        // We can check by iterating source and checking parent of each attribute.
        for (Attribute a : source) {
            assertEquals(source, a.parent);
        }
    }

    // Equals and hashCode
    @Test
    public void testEquals_sameAttributes_returnsTrue() {
        Attributes a1 = new Attributes();
        a1.put("key", "val");
        Attributes a2 = new Attributes();
        a2.put("key", "val");
        assertEquals(a1, a2);
        assertEquals(a1.hashCode(), a2.hashCode());
    }

    @Test
    public void testEquals_differentAttributes_returnsFalse() {
        Attributes a1 = new Attributes();
        a1.put("key", "val");
        Attributes a2 = new Attributes();
        a2.put("key", "other");
        assertNotEquals(a1, a2);
    }

    // Clone produces deep copy
    @Test
    public void testClone_deepCopy_independentModification() {
        Attributes original = new Attributes();
        original.put("key", "val");
        Attributes clone = original.clone();
        clone.put("key", "new");
        assertEquals("val", original.get("key"));
        assertEquals("new", clone.get("key"));
    }

    // Normalize lowercases keys
    @Test
    public void testNormalize_lowercasesKeys() {
        Attributes attrs = new Attributes();
        attrs.put("KEY", "VALUE");
        attrs.normalize();
        assertEquals("VALUE", attrs.get("key"));
        assertFalse(attrs.hasKey("KEY"));
    }

    // Dataset basic operations
    @Test
    public void testDataset_putAndGet() {
        Attributes attrs = new Attributes();
        attrs.put("data-name", "value1");
        Map<String, String> ds = attrs.dataset();
        assertEquals(1, ds.size());
        assertEquals("value1", ds.get("name"));
        ds.put("age", "25");
        assertTrue(attrs.hasKey("data-age"));
        assertEquals("25", attrs.get("data-age"));
    }

    // Html - simple output
    @Test
    public void testHtml_simpleAttributes_containsKeyAndValue() {
        Attributes attrs = new Attributes();
        attrs.put("class", "main");
        String html = attrs.html();
        assertTrue(html.contains("class=\"main\""));
    }

    // Html - boolean attribute (null value) with HTML syntax
    @Test
    public void testHtml_booleanAttribute_noValue() {
        Attributes attrs = new Attributes();
        attrs.put("disabled", true);
        String html = attrs.html();
        assertTrue(html.contains("disabled"));
        assertFalse(html.contains("=\""));
    }

    // Put null key throws exception (Validate.notNull)
    @Test(expected = IllegalArgumentException.class)
    public void testPut_nullKey_throwsException() {
        Attributes attrs = new Attributes();
        attrs.put(null, "value");
    }

    // Put null attribute throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testPut_nullAttribute_throwsException() {
        Attributes attrs = new Attributes();
        attrs.put((Attribute) null);
    }

    // Growth beyond initial capacity
    @Test
    public void testGrowth_moreThanInitialCapacity_works() {
        Attributes attrs = new Attributes();
        for (int i = 0; i < 10; i++) {
            attrs.put("key" + i, "val" + i);
        }
        assertEquals(10, attrs.size());
        assertEquals("val5", attrs.get("key5"));
    }

    // IndexOfKey with null key throws
    @Test(expected = IllegalArgumentException.class)
    public void testGet_nullKey_throwsException() {
        Attributes attrs = new Attributes();
        attrs.get(null);
    }
}