package org.jsoup.nodes;

import org.junit.Test;
import java.io.IOException;
import static org.junit.Assert.*;

public class AttributeTest {

    // Tests normal constructor and getters
    @Test
    public void testConstructor_validKeyAndVal_createsAttribute() {
        Attribute attr = new Attribute("  href  ", "http://example.com");
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

    // Tests setKey on standalone attribute
    @Test
    public void testSetKey_validKey_updatesKey() {
        Attribute attr = new Attribute("name", "value");
        attr.setKey("  new-name  ");
        assertEquals("new-name", attr.getKey());
    }

    // Tests setKey when parent Attributes object is attached
    @Test
    public void testSetKey_withParent_updatesKeyInParent() {
        Attributes parent = new Attributes();
        parent.put("oldKey", "val");
        Attribute attr = new Attribute("oldKey", "val", parent);
        attr.setKey("newKey");
        assertEquals("newKey", attr.getKey());
        assertTrue(parent.hasKey("newKey"));
        assertFalse(parent.hasKey("oldKey"));
    }

    // Tests setValue on standalone attribute (defect 89b regression test)
    @Test
    public void testSetValue_standaloneAttribute_updatesValueAndReturnsOld() {
        Attribute attr = new Attribute("key", "oldVal");
        String oldVal = attr.setValue("newVal");
        assertEquals("oldVal", oldVal);
        assertEquals("newVal", attr.getValue());
    }

    // Tests setValue when parent Attributes object is attached
    @Test
    public void testSetValue_withParent_updatesValueInParent() {
        Attributes parent = new Attributes();
        parent.put("key", "oldVal");
        Attribute attr = new Attribute("key", "oldVal", parent);
        String oldVal = attr.setValue("newVal");
        assertEquals("oldVal", oldVal);
        assertEquals("newVal", attr.getValue());
        assertEquals("newVal", parent.get("key"));
    }

    // Tests html rendering of a standard key-value attribute
    @Test
    public void testHtml_standardAttribute_returnsValidHtml() {
        Attribute attr = new Attribute("class", "my \"class\"");
        assertEquals("class=\"my &quot;class&quot;\"", attr.html());
        assertEquals("class=\"my &quot;class&quot;\"", attr.toString());
    }

    // Tests html rendering of boolean attributes that collapse
    @Test
    public void testHtml_booleanAttribute_collapsesWhenAppropriate() {
        Attribute attr = new Attribute("disabled", "");
        assertEquals("disabled", attr.html());

        Attribute attr2 = new Attribute("checked", "checked");
        assertEquals("checked", attr2.html());

        Attribute attr3 = new Attribute("checked", "other");
        assertEquals("checked=\"other\"", attr3.html());
    }

    // Tests shouldCollapseAttribute in XML mode does not collapse boolean attributes
    @Test
    public void testHtml_xmlSyntax_doesNotCollapse() throws IOException {
        Attribute attr = new Attribute("disabled", "");
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.syntax(Document.OutputSettings.Syntax.xml);

        StringBuilder accum = new StringBuilder();
        attr.html(accum, settings);
        assertEquals("disabled=\"\"", accum.toString());
    }

    // Tests createFromEncoded creates unescaped attribute value
    @Test
    public void testCreateFromEncoded_encodedEntities_unescapesCorrectly() {
        Attribute attr = Attribute.createFromEncoded("href", "http://example.com/search?q=1&amp;amp=2");
        assertEquals("href", attr.getKey());
        assertEquals("http://example.com/search?q=1&amp=2", attr.getValue());
    }

    // Tests data- prefix recognition for isDataAttribute
    @Test
    public void testIsDataAttribute_dataPrefix_identifiesCorrectly() {
        Attribute dataAttr = new Attribute("data-name", "Jsoup");
        assertTrue(dataAttr.isDataAttribute());

        Attribute prefixOnly = new Attribute("data-", "Jsoup");
        assertFalse(prefixOnly.isDataAttribute());

        Attribute normalAttr = new Attribute("name", "Jsoup");
        assertFalse(normalAttr.isDataAttribute());
    }

    // Tests isBooleanAttribute helper method
    @Test
    public void testIsBooleanAttribute_booleanKeys_identifiesCorrectly() {
        assertTrue(Attribute.isBooleanAttribute("allowfullscreen"));
        assertTrue(Attribute.isBooleanAttribute("required"));
        assertFalse(Attribute.isBooleanAttribute("href"));
        assertFalse(Attribute.isBooleanAttribute("custom-bool"));
    }

    // Tests equals and hashCode consistency
    @Test
    public void testEqualsAndHashCode_sameAndDifferentAttributes_behavesCorrectly() {
        Attribute a1 = new Attribute("key", "val");
        Attribute a2 = new Attribute("key", "val");
        Attribute a3 = new Attribute("key", "other");
        Attribute a4 = new Attribute("different", "val");

        assertEquals(a1, a1);
        assertEquals(a1, a2);
        assertEquals(a1.hashCode(), a2.hashCode());

        assertNotEquals(a1, a3);
        assertNotEquals(a1, a4);
        assertNotEquals(a1, null);
        assertNotEquals(a1, "some string");
    }

    // Tests clone returns an independent copy
    @Test
    public void testClone_clonesAttribute_createsIndependentCopy() {
        Attribute original = new Attribute("key", "val");
        Attribute copy = original.clone();

        assertEquals(original, copy);
        assertNotSame(original, copy);

        copy.setKey("newKey");
        assertEquals("key", original.getKey());
        assertEquals("newKey", copy.getKey());
    }
}