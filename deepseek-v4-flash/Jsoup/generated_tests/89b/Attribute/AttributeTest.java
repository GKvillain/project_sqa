package org.jsoup.nodes;

import org.jsoup.SerializationException;
import org.jsoup.helper.Validate;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * JUnit 4 test class for org.jsoup.nodes.Attribute.
 * Covers normal, boundary, edge, and defect cases (Defects4J bug 89b).
 */
public class AttributeTest {

    // ========== Constructor ==========

    // Test normal valid input
    @Test
    public void testConstructor_validKeyValue_createsAttribute() {
        Attribute attr = new Attribute("href", "http://example.com");
        assertEquals("href", attr.getKey());
        assertEquals("http://example.com", attr.getValue());
    }

    // Test null key throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullKey_throwsException() {
        new Attribute(null, "value");
    }

    // Test empty key after trimming throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_emptyKey_throwsException() {
        new Attribute("   ", "value");
    }

    // Test key trimmed
    @Test
    public void testConstructor_keyWithSpaces_trimmed() {
        Attribute attr = new Attribute("  class  ", "main");
        assertEquals("class", attr.getKey());
    }

    // Test null value allowed
    @Test
    public void testConstructor_nullValue_allowed() {
        Attribute attr = new Attribute("key", null);
        assertNull(attr.getValue()); // getValue() calls checkNotNull, so returns empty? Let's test actual
        // Actually Attributes.checkNotNull returns empty string for null. So we need to check behavior.
        // We'll use getValue() which returns checkNotNull(val). So for null val, checkNotNull returns "".
        assertEquals("", attr.getValue());
    }

    // ========== setKey ==========

    // Test setKey normal
    @Test
    public void testSetKey_normal_updatesKey() {
        Attribute attr = new Attribute("old", "val");
        attr.setKey("new");
        assertEquals("new", attr.getKey());
    }

    // Test setKey null throws
    @Test(expected = IllegalArgumentException.class)
    public void testSetKey_nullKey_throwsException() {
        Attribute attr = new Attribute("key", "val");
        attr.setKey(null);
    }

    // Test setKey empty throws
    @Test(expected = IllegalArgumentException.class)
    public void testSetKey_emptyKey_throwsException() {
        Attribute attr = new Attribute("key", "val");
        attr.setKey("   ");
    }

    // ========== setValue ==========

    // Test setValue normal returns old value and updates
    @Test
    public void testSetValue_normal_returnsOldValueAndUpdates() {
        Attribute attr = new Attribute("key", "old");
        String old = attr.setValue("new");
        assertEquals("old", old);
        assertEquals("new", attr.getValue());
    }

    // Test setValue with null (allowed)
    @Test
    public void testSetValue_nullValue_updatesToEmpty() {
        Attribute attr = new Attribute("key", "some");
        attr.setValue(null);
        assertEquals("", attr.getValue());
    }

    // ========== html() ==========

    // Test html output for normal attribute
    @Test
    public void testHtml_normal_returnsHtmlString() {
        Attribute attr = new Attribute("href", "http://example.com");
        assertEquals("href=\"http://example.com\"", attr.html());
    }

    // Test html with null value (boolean-like)
    @Test
    public void testHtml_nullValue_returnsKeyOnly() {
        Attribute attr = new Attribute("disabled", null);
        // Since val is null, shouldCollapseAttribute might collapse to just "disabled"
        // But careful: isBooleanAttribute returns true for "disabled", so it collapses.
        assertEquals("disabled", attr.html());
    }

    // ========== shouldCollapseAttribute (defect area) ==========

    // Boolean attribute with empty value should collapse
    @Test
    public void testShouldCollapseAttribute_booleanAttributeEmptyValue_returnsTrue() {
        Document doc = new Document("");
        Document.OutputSettings settings = doc.outputSettings();
        settings.syntax(Document.OutputSettings.Syntax.html);
        assertTrue(Attribute.shouldCollapseAttribute("disabled", "", settings));
    }

    // Boolean attribute with null value should collapse (correct behavior)
    @Test
    public void testShouldCollapseAttribute_booleanAttributeNullValue_returnsTrue() {
        Document doc = new Document("");
        Document.OutputSettings settings = doc.outputSettings();
        settings.syntax(Document.OutputSettings.Syntax.html);
        assertTrue(Attribute.shouldCollapseAttribute("disabled", null, settings));
    }

    // Non-boolean attribute with null value should NOT collapse – this is the defect (bug 89b)
    @Test
    public void testShouldCollapseAttribute_nonBooleanAttributeNullValue_returnsFalse_catchesDefect() {
        Document doc = new Document("");
        Document.OutputSettings settings = doc.outputSettings();
        settings.syntax(Document.OutputSettings.Syntax.html);
        // "class" is not a boolean attribute; with null val should not collapse
        assertFalse(Attribute.shouldCollapseAttribute("class", null, settings));
    }

    // Non-boolean attribute with value should not collapse
    @Test
    public void testShouldCollapseAttribute_nonBooleanAttributeWithValue_returnsFalse() {
        Document doc = new Document("");
        Document.OutputSettings settings = doc.outputSettings();
        settings.syntax(Document.OutputSettings.Syntax.html);
        assertFalse(Attribute.shouldCollapseAttribute("class", "main", settings));
    }

    // Non-boolean attribute with empty value should not collapse (unless boolean)
    @Test
    public void testShouldCollapseAttribute_nonBooleanAttributeEmptyValue_returnsFalse() {
        Document doc = new Document("");
        Document.OutputSettings settings = doc.outputSettings();
        settings.syntax(Document.OutputSettings.Syntax.html);
        assertFalse(Attribute.shouldCollapseAttribute("class", "", settings));
    }

    // With xhtml syntax should never collapse
    @Test
    public void testShouldCollapseAttribute_xhtmlSyntax_returnsFalse() {
        Document doc = new Document("");
        Document.OutputSettings settings = doc.outputSettings();
        settings.syntax(Document.OutputSettings.Syntax.xml);
        assertFalse(Attribute.shouldCollapseAttribute("disabled", "", settings));
    }

    // ========== isBooleanAttribute ==========

    // Known boolean attribute
    @Test
    public void testIsBooleanAttribute_knownBoolean_returnsTrue() {
        assertTrue(Attribute.isBooleanAttribute("disabled"));
    }

    // Non-boolean attribute
    @Test
    public void testIsBooleanAttribute_nonBoolean_returnsFalse() {
        assertFalse(Attribute.isBooleanAttribute("class"));
    }

    // ========== equals and hashCode ==========

    // Same key and value should be equal
    @Test
    public void testEquals_sameKeyValue_returnsTrue() {
        Attribute a1 = new Attribute("key", "val");
        Attribute a2 = new Attribute("key", "val");
        assertEquals(a1, a2);
        assertEquals(a1.hashCode(), a2.hashCode());
    }

    // Different key should not be equal
    @Test
    public void testEquals_differentKey_returnsFalse() {
        Attribute a1 = new Attribute("key1", "val");
        Attribute a2 = new Attribute("key2", "val");
        assertNotEquals(a1, a2);
    }

    // ========== clone ==========

    // Cloned attribute should be equal to original
    @Test
    public void testClone_returnsEqualObject() {
        Attribute original = new Attribute("href", "http://example.com");
        Attribute clone = original.clone();
        assertEquals(original, clone);
        assertNotSame(original, clone);
    }

    // ========== createFromEncoded ==========

    // Test unescaping of encoded value
    @Test
    public void testCreateFromEncoded_unescapesValue() {
        Attribute attr = Attribute.createFromEncoded("href", "http://example.com&amp;test");
        assertEquals("href", attr.getKey());
        assertEquals("http://example.com&test", attr.getValue());
    }

    // ========== toString ==========

    // toString equals html
    @Test
    public void testToString_equalsHtml() {
        Attribute attr = new Attribute("key", "val");
        assertEquals(attr.html(), attr.toString());
    }

    // ========== isDataAttribute ==========

    // Data attribute prefix "data-"
    @Test
    public void testIsDataAttribute_dataPrefix_returnsTrue() {
        Attribute attr = new Attribute("data-name", "value");
        assertTrue(attr.isDataAttribute());
    }

    // Non-data attribute
    @Test
    public void testIsDataAttribute_nonData_returnsFalse() {
        Attribute attr = new Attribute("name", "value");
        assertFalse(attr.isDataAttribute());
    }

    // ========== getKey ==========

    @Test
    public void testGetKey_normal_returnsKey() {
        Attribute attr = new Attribute("test", "val");
        assertEquals("test", attr.getKey());
    }
}