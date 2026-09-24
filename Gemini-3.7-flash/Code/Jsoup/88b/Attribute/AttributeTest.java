package org.jsoup.nodes;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class AttributeTest {

    // Tests constructor and getters with valid key and value
    @Test
    public void testConstructor_validKeyAndValue_setsFieldsCorrectly() {
        Attribute attr = new Attribute("href", "http://example.com");
        assertEquals("href", attr.getKey());
        assertEquals("http://example.com", attr.getValue());
    }

    // Tests constructor with null key throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullKey_throwsException() {
        new Attribute(null, "value");
    }

    // Tests constructor with empty key throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_emptyKey_throwsException() {
        new Attribute("", "value");
    }

    // Tests constructor with whitespace-only key throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_whitespaceKey_throwsException() {
        new Attribute("   ", "value");
    }

    // Tests setKey updates key and trims whitespace
    @Test
    public void testSetKey_validKey_updatesKey() {
        Attribute attr = new Attribute("oldKey", "value");
        attr.setKey("  newKey  ");
        assertEquals("newKey", attr.getKey());
    }

    // Tests setKey with null throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testSetKey_nullKey_throwsException() {
        Attribute attr = new Attribute("key", "value");
        attr.setKey(null);
    }

    // Tests setKey with empty key throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testSetKey_emptyKey_throwsException() {
        Attribute attr = new Attribute("key", "value");
        attr.setKey("");
    }

    // Tests setKey updates parent Attributes when parent is present
    @Test
    public void testSetKey_withParentAttributes_updatesParentKey() {
        Attributes parent = new Attributes();
        parent.put("k1", "v1");
        Attribute attr = new Attribute("k1", "v1", parent);
        attr.setKey("k2");
        assertEquals("k2", attr.getKey());
        assertTrue(parent.hasKey("k2"));
        assertFalse(parent.hasKey("k1"));
    }

    // Tests setValue without parent (defect-prone scenario where parent is null)
    @Test
    public void testSetValue_noParent_setsValueAndHandlesNullParent() {
        Attribute attr = new Attribute("key", "oldVal");
        try {
            String oldVal = attr.setValue("newVal");
            assertEquals("newVal", attr.getValue());
            assertNull(oldVal);
        } catch (NullPointerException e) {
            // Catches defect if setValue tries to access null parent
            fail("setValue should not throw NullPointerException when parent is null");
        }
    }

    // Tests setValue with parent Attributes updates parent value
    @Test
    public void testSetValue_withParentAttributes_updatesParentValue() {
        Attributes parent = new Attributes();
        parent.put("key", "oldVal");
        Attribute attr = new Attribute("key", "oldVal", parent);
        String oldVal = attr.setValue("newVal");
        assertEquals("oldVal", oldVal);
        assertEquals("newVal", attr.getValue());
        assertEquals("newVal", parent.get("key"));
    }

    // Tests html output for standard attribute
    @Test
    public void testHtml_standardAttribute_returnsFormattedHtml() {
        Attribute attr = new Attribute("id", "main");
        assertEquals("id=\"main\"", attr.html());
        assertEquals("id=\"main\"", attr.toString());
    }

    // Tests html output with HTML entities that require escaping
    @Test
    public void testHtml_attributeWithSpecialCharacters_escapesValue() {
        Attribute attr = new Attribute("title", "foo & \"bar\" <baz>");
        assertEquals("title=\"foo &amp; &quot;bar&quot; &lt;baz&gt;\"", attr.html());
    }

    // Tests html collapsing for boolean attribute with empty value
    @Test
    public void testHtml_booleanAttributeEmptyValue_collapsesAttribute() {
        Attribute attr = new Attribute("disabled", "");
        assertEquals("disabled", attr.html());
    }

    // Tests html collapsing for boolean attribute with matching key/value
    @Test
    public void testHtml_booleanAttributeSameValue_collapsesAttribute() {
        Attribute attr = new Attribute("required", "required");
        assertEquals("required", attr.html());
    }

    // Tests html does not collapse boolean attribute in XML syntax mode
    @Test
    public void testHtml_xmlSyntax_doesNotCollapseBooleanAttribute() throws IOException {
        Attribute attr = new Attribute("disabled", "");
        Document.OutputSettings settings = new Document.OutputSettings().syntax(Document.OutputSettings.Syntax.xml);
        StringBuilder sb = new StringBuilder();
        attr.html(sb, settings);
        assertEquals("disabled=\"\"", sb.toString());
    }

    // Tests createFromEncoded correctly unescapes HTML entities in value
    @Test
    public void testCreateFromEncoded_encodedEntities_unescapesValue() {
        Attribute attr = Attribute.createFromEncoded("title", "&quot;Hello &amp; World&quot;");
        assertEquals("title", attr.getKey());
        assertEquals("\"Hello & World\"", attr.getValue());
    }

    // Tests isDataAttribute recognition for data-* prefix
    @Test
    public void testIsDataAttribute_dataPrefix_returnsTrue() {
        Attribute attr = new Attribute("data-test", "val");
        assertTrue(attr.isDataAttribute());
        assertTrue(Attribute.isDataAttribute("data-custom"));
    }

    // Tests isDataAttribute returns false for non-data attributes or just prefix
    @Test
    public void testIsDataAttribute_nonDataAttribute_returnsFalse() {
        Attribute attr = new Attribute("href", "http://example.com");
        assertFalse(attr.isDataAttribute());
        assertFalse(Attribute.isDataAttribute("data-"));
        assertFalse(Attribute.isDataAttribute("class"));
    }

    // Tests isBooleanAttribute for standard HTML5 boolean attributes
    @Test
    public void testIsBooleanAttribute_knownBooleanAttributes_returnsTrue() {
        assertTrue(Attribute.isBooleanAttribute("checked"));
        assertTrue(Attribute.isBooleanAttribute("readonly"));
        assertTrue(new Attribute("hidden", "hidden").isBooleanAttribute());
        assertFalse(Attribute.isBooleanAttribute("href"));
        assertFalse(Attribute.isBooleanAttribute("custom"));
    }

    // Tests equals and hashCode contracts
    @Test
    public void testEqualsAndHashCode_sameAndDifferentObjects_worksCorrectly() {
        Attribute attr1 = new Attribute("key", "value");
        Attribute attr2 = new Attribute("key", "value");
        Attribute attr3 = new Attribute("key", "otherValue");
        Attribute attr4 = new Attribute("otherKey", "value");

        assertEquals(attr1, attr1);
        assertEquals(attr1, attr2);
        assertEquals(attr1.hashCode(), attr2.hashCode());

        assertNotEquals(attr1, attr3);
        assertNotEquals(attr1, attr4);
        assertNotEquals(attr1, null);
        assertNotEquals(attr1, "key=value");
    }

    // Tests clone creates independent copy
    @Test
    public void testClone_clonedObject_createsEqualObject() {
        Attribute original = new Attribute("key", "val");
        Attribute cloned = original.clone();
        assertNotSame(original, cloned);
        assertEquals(original, cloned);
        assertEquals(original.getKey(), cloned.getKey());
        assertEquals(original.getValue(), cloned.getValue());
    }
}