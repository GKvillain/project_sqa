package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

public class AttributeTest {

    // Tests null key rejection
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullKey_throwsException() {
        new Attribute(null, "value");
    }

    // Tests blank key rejection after trimming
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_blankKey_throwsException() {
        new Attribute("   ", "value");
    }

    // Tests key trimming on construction
    @Test
    public void testConstructor_whitespaceKey_trimsKey() {
        Attribute attr = new Attribute("  key  ", "value");
        assertEquals("key", attr.getKey());
        assertEquals("value", attr.getValue());
    }

    // Tests setKey without parent
    @Test
    public void testSetKey_validKey_updatesKey() {
        Attribute attr = new Attribute("old", "value");
        attr.setKey("new");
        assertEquals("new", attr.getKey());
        assertEquals("value", attr.getValue());
    }

    // Tests setKey updates parent's stored key
    @Test
    public void testSetKey_withParent_updatesParentKey() {
        Attributes attrs = new Attributes();
        attrs.put("old", "value");
        Attribute attr = new Attribute("old", attrs.get("old"), attrs);
        attr.setKey("new");
        assertEquals("new", attr.getKey());
        assertEquals("value", attrs.get("new"));
    }

    // Regression test for Defects4J bug: setValue must work without a parent
    @Test
    public void testSetValue_withoutParent_returnsOldValueAndUpdates() {
        Attribute attr = new Attribute("key", "old");
        assertEquals("old", attr.setValue("new"));
        assertEquals("new", attr.getValue());
    }

    // Tests setValue with parent updates parent and returns old value
    @Test
    public void testSetValue_withParent_updatesParentAndReturnsOldValue() {
        Attributes attrs = new Attributes();
        attrs.put("key", "old");
        Attribute attr = new Attribute("key", attrs.get("key"), attrs);
        assertEquals("old", attr.setValue("new"));
        assertEquals("new", attrs.get("key"));
        assertEquals("new", attr.getValue());
    }

    // Tests html output for a normal attribute
    @Test
    public void testHtml_normalValue_returnsQuotedValue() {
        Attribute attr = new Attribute("href", "index.html");
        assertEquals("href=\"index.html\"", attr.html());
        assertEquals("href=\"index.html\"", attr.toString());
    }

    // Tests collapsing boolean attribute with empty value
    @Test
    public void testHtml_booleanEmptyValue_returnsCollapsedKey() {
        Attribute attr = new Attribute("checked", "");
        assertEquals("checked", attr.html());
    }

    // Tests collapsing attribute with null value
    @Test
    public void testHtml_nullValue_returnsCollapsedKey() {
        Attribute attr = new Attribute("href", null);
        assertEquals("href", attr.html());
    }

    // Tests createFromEncoded decodes encoded value
    @Test
    public void testCreateFromEncoded_encodedValue_decodesValue() {
        Attribute attr = Attribute.createFromEncoded("href", "a&amp;b");
        assertEquals("href", attr.getKey());
        assertEquals("a&b", attr.getValue());
    }

    // Tests data attribute detection
    @Test
    public void testIsDataAttribute_detectsDataKeys() {
        assertTrue(new Attribute("data-test", "value").isDataAttribute());
        assertFalse(Attribute.isDataAttribute("data-"));
        assertFalse(Attribute.isDataAttribute("name"));
    }

    // Tests boolean attribute detection
    @Test
    public void testIsBooleanAttribute_detectsKnownBooleans() {
        assertTrue(Attribute.isBooleanAttribute("checked"));
        assertFalse(Attribute.isBooleanAttribute("class"));
    }

    // Tests true branch of shouldCollapseAttribute
    @Test
    public void testShouldCollapseAttribute_booleanEmpty_returnsTrue() {
        Document.OutputSettings out = new Document("").outputSettings();
        assertTrue(Attribute.shouldCollapseAttribute("disabled", "", out));
    }

    // Tests false branch for non-boolean attribute
    @Test
    public void testShouldCollapseAttribute_nonBooleanEmpty_returnsFalse() {
        Document.OutputSettings out = new Document("").outputSettings();
        assertFalse(Attribute.shouldCollapseAttribute("class", "", out));
    }

    // Tests case-insensitive comparison in shouldCollapseAttribute
    @Test
    public void testShouldCollapseAttribute_booleanValueEqualsIgnoreCase_returnsTrue() {
        Document.OutputSettings out = new Document("").outputSettings();
        assertTrue(Attribute.shouldCollapseAttribute("checked", "CHECKED", out));
    }

    // Tests false branch when boolean attribute value does not match
    @Test
    public void testShouldCollapseAttribute_booleanMismatch_returnsFalse() {
        Document.OutputSettings out = new Document("").outputSettings();
        assertFalse(Attribute.shouldCollapseAttribute("disabled", "no", out));
    }

    // Tests equals and hashCode behavior
    @Test
    public void testEquals_sameKeyAndValue_returnsTrue() {
        Attribute a = new Attribute("key", "value");
        Attribute b = new Attribute("key", "value");
        Attribute c = new Attribute("key", "other");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertFalse(a.equals(c));
    }

    // Tests clone returns an equal, distinct copy
    @Test
    public void testClone_returnsEqualCopy() {
        Attribute a = new Attribute("key", "value");
        Attribute b = a.clone();
        assertEquals(a, b);
        assertNotSame(a, b);
    }
}