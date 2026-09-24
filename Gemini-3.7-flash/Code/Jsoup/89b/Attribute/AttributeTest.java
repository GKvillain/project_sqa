package org.jsoup.nodes;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AttributeTest {

    // Tests normal attribute creation and getters
    @Test
    public void testConstructor_validKeyAndValue_createsInstance() {
        Attribute attr = new Attribute("href", "http://example.com");
        assertEquals("href", attr.getKey());
        assertEquals("http://example.com", attr.getValue());
    }

    // Tests null key rejection in constructor
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullKey_throwsException() {
        new Attribute(null, "val");
    }

    // Tests empty key rejection in constructor
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_emptyKey_throwsException() {
        new Attribute("", "val");
    }

    // Tests whitespace-only key rejection after trim
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_whitespaceKey_throwsException() {
        new Attribute("   ", "val");
    }

    // Tests setValue when parent is null (Defects4J 89b regression)
    @Test
    public void testSetValue_nullParent_returnsOldValueAndUpdates() {
        Attribute attr = new Attribute("key", "oldVal");
        String oldVal = attr.setValue("newVal");
        assertEquals("oldVal", oldVal);
        assertEquals("newVal", attr.getValue());
    }

    // Tests setValue when parent is present and updates parent
    @Test
    public void testSetValue_withParent_updatesParentAndReturnsOldValue() {
        Attributes parent = new Attributes();
        parent.put("key", "oldVal");
        Attribute attr = new Attribute("key", "oldVal", parent);
        String oldVal = attr.setValue("newVal");
        assertEquals("oldVal", oldVal);
        assertEquals("newVal", attr.getValue());
        assertEquals("newVal", parent.get("key"));
    }

    // Tests setKey to valid key without parent
    @Test
    public void testSetKey_validKey_updatesKey() {
        Attribute attr = new Attribute("oldKey", "val");
        attr.setKey("newKey");
        assertEquals("newKey", attr.getKey());
    }

    // Tests setKey with parent updates key in parent
    @Test
    public void testSetKey_withParent_updatesParentKey() {
        Attributes parent = new Attributes();
        parent.put("oldKey", "val");
        Attribute attr = new Attribute("oldKey", "val", parent);
        attr.setKey("newKey");
        assertEquals("newKey", attr.getKey());
        assertTrue(parent.hasKey("newKey"));
        assertFalse(parent.hasKey("oldKey"));
    }

    // Tests setKey with null key throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testSetKey_nullKey_throwsException() {
        Attribute attr = new Attribute("key", "val");
        attr.setKey(null);
    }

    // Tests html generation for standard key-value attribute
    @Test
    public void testHtml_standardAttribute_rendersCorrectly() {
        Attribute attr = new Attribute("class", "btn btn-primary");
        assertEquals("class=\"btn btn-primary\"", attr.html());
        assertEquals("class=\"btn btn-primary\"", attr.toString());
    }

    // Tests html generation for boolean attribute with empty value
    @Test
    public void testHtml_booleanAttributeEmptyValue_collapsesAttribute() {
        Attribute attr = new Attribute("disabled", "");
        assertEquals("disabled", attr.html());
    }

    // Tests html generation for boolean attribute matching name
    @Test
    public void testHtml_booleanAttributeMatchingValue_collapsesAttribute() {
        Attribute attr = new Attribute("checked", "checked");
        assertEquals("checked", attr.html());
    }

    // Tests shouldCollapseAttribute in XML syntax does not collapse
    @Test
    public void testShouldCollapseAttribute_xmlSyntax_doesNotCollapse() {
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.syntax(Document.OutputSettings.Syntax.xml);
        assertFalse(Attribute.shouldCollapseAttribute("disabled", "", settings));
    }

    // Tests createFromEncoded unescapes HTML entities
    @Test
    public void testCreateFromEncoded_htmlEntities_unescapesCorrectly() {
        Attribute attr = Attribute.createFromEncoded("title", "&lt;Hello &amp; World&gt;");
        assertEquals("title", attr.getKey());
        assertEquals("<Hello & World>", attr.getValue());
    }

    // Tests isDataAttribute with data prefix
    @Test
    public void testIsDataAttribute_dataPrefix_returnsTrue() {
        Attribute attr = new Attribute("data-name", "value");
        assertTrue(attr.isDataAttribute());
        assertTrue(Attribute.isDataAttribute("data-custom"));
    }

    // Tests isDataAttribute with non-data prefix or exact prefix without name
    @Test
    public void testIsDataAttribute_nonDataPrefix_returnsFalse() {
        Attribute attr = new Attribute("name", "value");
        assertFalse(attr.isDataAttribute());
        assertFalse(Attribute.isDataAttribute("data-"));
    }

    // Tests isBooleanAttribute detection
    @Test
    public void testIsBooleanAttribute_booleanAndNonBoolean_returnsExpected() {
        assertTrue(Attribute.isBooleanAttribute("required"));
        assertTrue(Attribute.isBooleanAttribute("readonly"));
        assertFalse(Attribute.isBooleanAttribute("custom"));
    }

    // Tests equals and hashCode contract
    @Test
    public void testEqualsAndHashCode_sameAndDifferentAttributes_behavesCorrectly() {
        Attribute attr1 = new Attribute("key", "val");
        Attribute attr2 = new Attribute("key", "val");
        Attribute attr3 = new Attribute("key", "other");
        Attribute attr4 = new Attribute("otherKey", "val");

        assertEquals(attr1, attr2);
        assertEquals(attr1.hashCode(), attr2.hashCode());
        assertNotEquals(attr1, attr3);
        assertNotEquals(attr1, attr4);
        assertNotEquals(attr1, null);
        assertNotEquals(attr1, "notAnAttribute");
    }

    // Tests clone returns identical but independent copy
    @Test
    public void testClone_clonedInstance_equalsOriginal() {
        Attribute original = new Attribute("key", "value");
        Attribute cloned = original.clone();
        assertNotNull(cloned);
        assertEquals(original, cloned);
        cloned.setValue("newValue");
        assertEquals("value", original.getValue());
        assertEquals("newValue", cloned.getValue());
    }
}