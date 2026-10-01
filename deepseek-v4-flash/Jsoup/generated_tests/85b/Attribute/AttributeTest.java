package org.jsoup.nodes;

import static org.junit.Assert.*;
import org.junit.Test;

public class AttributeTest {

    // Tests that constructor throws IllegalArgumentException for null key
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullKey_throwsException() {
        new Attribute(null, "value");
    }

    // Tests that constructor throws IllegalArgumentException for key that becomes empty after trimming
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_emptyKeyAfterTrim_throwsException() {
        new Attribute("   ", "value");
    }

    // Tests constructor with valid key and value
    @Test
    public void testConstructor_valid_createsAttribute() {
        Attribute attr = new Attribute("key", "value");
        assertEquals("key", attr.getKey());
        assertEquals("value", attr.getValue());
    }

    // Tests getKey and setKey normal operations
    @Test
    public void testGetSetKey_normal() {
        Attribute attr = new Attribute("old", "val");
        attr.setKey("new");
        assertEquals("new", attr.getKey());
        // trimming
        attr.setKey("  trimmed  ");
        assertEquals("trimmed", attr.getKey());
    }

    // Tests setKey with null throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testSetKey_null_throwsException() {
        new Attribute("k", "v").setKey(null);
    }

    // Tests setKey with key that becomes empty after trim throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testSetKey_emptyAfterTrim_throwsException() {
        new Attribute("k", "v").setKey("   ");
    }

    // Tests setKey when parent is not null but attribute not added to parent
    @Test
    public void testSetKey_withParent_notFound() {
        Attributes parent = new Attributes();
        Attribute attr = new Attribute("old", "val", parent);
        attr.setKey("new");
        assertEquals("new", attr.getKey());
    }

    // Tests setValue: verifies correct old value is returned and value is updated
    // This test is designed to detect the bug where setValue throws NullPointerException
    // when parent is null, and returns wrong old value when parent is not null but attribute absent.
    @Test
    public void testSetValue_returnsOldValueAndUpdates() {
        // Case: parent null, initial val non-null
        Attribute attr = new Attribute("k", "old");
        String old = attr.setValue("new");
        assertEquals("old", old);
        assertEquals("new", attr.getValue());

        // Case: parent null, initial val null
        attr = new Attribute("k", null);
        old = attr.setValue("val");
        assertNull(old);
        assertEquals("val", attr.getValue());

        // Case: parent not null, attribute not added to parent
        Attributes parent = new Attributes();
        attr = new Attribute("k", "old", parent);
        old = attr.setValue("new");
        // Buggy version returns parent.get("k") which is null, so this will fail
        assertEquals("old", old);
        assertEquals("new", attr.getValue());
    }

    // Tests html() output for a normal attribute
    @Test
    public void testHtml_normalAttribute() {
        Attribute attr = new Attribute("href", "http://example.com");
        assertEquals("href=\"http://example.com\"", attr.html());
    }

    // Tests html() for boolean attributes that should collapse
    @Test
    public void testHtml_booleanAttributeCollapsed() {
        // null value
        assertEquals("checked", new Attribute("checked", null).html());
        // empty value
        assertEquals("checked", new Attribute("checked", "").html());
        // value equals key (case-insensitive)
        assertEquals("checked", new Attribute("checked", "checked").html());
        assertEquals("checked", new Attribute("checked", "CHECKED").html());
    }

    // Tests html() for boolean attributes that should not collapse
    @Test
    public void testHtml_booleanAttributeNotCollapsed() {
        // different value
        assertEquals("checked=\"yes\"", new Attribute("checked", "yes").html());
        // non-boolean attribute
        assertEquals("id=\"main\"", new Attribute("id", "main").html());
    }

    // Tests html() escapes special characters in value
    @Test
    public void testHtml_escapedValue() {
        Attribute attr = new Attribute("alt", "a&b");
        String html = attr.html();
        assertTrue("Should contain escaped &", html.contains("&amp;"));
        assertTrue(html.startsWith("alt=\""));
        assertTrue(html.endsWith("\""));
    }

    // Tests that toString() returns html() output
    @Test
    public void testToString() {
        Attribute attr = new Attribute("key", "val");
        assertEquals(attr.html(), attr.toString());
    }

    // Tests static isBooleanAttribute method
    @Test
    public void testIsBooleanAttribute_static() {
        assertTrue(Attribute.isBooleanAttribute("checked"));
        assertTrue(Attribute.isBooleanAttribute("disabled"));
        assertFalse(Attribute.isBooleanAttribute("id"));
        assertFalse(Attribute.isBooleanAttribute("class"));
    }

    // Tests deprecated isBooleanAttribute instance method
    @Test
    public void testIsBooleanAttribute_deprecated() {
        // key in array, any value -> true
        assertTrue(new Attribute("checked", "any").isBooleanAttribute());
        // key not in array, value null -> true
        assertTrue(new Attribute("custom", null).isBooleanAttribute());
        // key not in array, value non-null -> false
        assertFalse(new Attribute("custom", "val").isBooleanAttribute());
        // key in array, value null -> true
        assertTrue(new Attribute("checked", null).isBooleanAttribute());
    }

    // Tests isDataAttribute method (static and instance)
    @Test
    public void testIsDataAttribute() {
        // static
        assertTrue(Attribute.isDataAttribute("data-role"));
        assertFalse(Attribute.isDataAttribute("role"));
        // instance
        assertTrue(new Attribute("data-value", "x").isDataAttribute());
        assertFalse(new Attribute("value", "x").isDataAttribute());
    }

    // Tests shouldCollapseAttribute static method with different syntax
    @Test
    public void testShouldCollapseAttribute_differentSyntax() {
        Document.OutputSettings outHtml = new Document.OutputSettings();
        Document.OutputSettings outXml = new Document.OutputSettings();
        outXml.syntax(Document.OutputSettings.Syntax.xml);

        // Boolean attribute with null val collapses in HTML, not in XML
        assertTrue("HTML: checked null should collapse", Attribute.shouldCollapseAttribute("checked", null, outHtml));
        assertFalse("XML: checked null should not collapse", Attribute.shouldCollapseAttribute("checked", null, outXml));

        // Non-boolean attribute never collapses
        assertFalse("Non-boolean with val", Attribute.shouldCollapseAttribute("id", "val", outHtml));
        assertFalse("Non-boolean with null", Attribute.shouldCollapseAttribute("id", null, outHtml));
    }

    // Tests equals and hashCode
    @Test
    public void testEqualsAndHashCode() {
        Attribute a1 = new Attribute("k", "v");
        Attribute a2 = new Attribute("k", "v");
        assertTrue(a1.equals(a2));
        assertEquals(a1.hashCode(), a2.hashCode());
        assertTrue(a2.equals(a1));

        // different key
        assertFalse(a1.equals(new Attribute("k2", "v")));
        // different value
        assertFalse(a1.equals(new Attribute("k", "v2")));
        // null values
        Attribute a5 = new Attribute("k", null);
        Attribute a6 = new Attribute("k", null);
        assertTrue(a5.equals(a6));
        assertEquals(a5.hashCode(), a6.hashCode());
        // null vs non-null
        assertFalse(a5.equals(new Attribute("k", "v")));
        // same object
        assertTrue(a1.equals(a1));
        // null
        assertFalse(a1.equals(null));
        // different type
        assertFalse(a1.equals("other"));
    }

    // Tests clone method
    @Test
    public void testClone() {
        Attribute attr = new Attribute("key", "val");
        Attribute clone = attr.clone();
        assertNotSame(attr, clone);
        assertEquals(attr.getKey(), clone.getKey());
        assertEquals(attr.getValue(), clone.getValue());
        assertTrue(attr.equals(clone));
        assertEquals(attr.hashCode(), clone.hashCode());

        // Verify clone is independent
        clone.setKey("newKey");
        assertNotEquals("clone change should not affect original", attr.getKey(), clone.getKey());
    }

    // Tests createFromEncoded decodes value
    @Test
    public void testCreateFromEncoded() {
        Attribute attr = Attribute.createFromEncoded("key", "value&amp;");
        assertEquals("key", attr.getKey());
        assertEquals("value&", attr.getValue());
    }

}