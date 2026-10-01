package org.jsoup.nodes;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class AttributeTest {

    // Tests constructor with valid key and value
    @Test
    public void testConstructor_validKeyValue_createsAttribute() {
        Attribute attr = new Attribute("href", "http://example.com");
        assertEquals("href", attr.getKey());
        assertEquals("http://example.com", attr.getValue());
    }

    // Tests constructor with null key throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullKey_throwsIllegalArgumentException() {
        new Attribute(null, "value");
    }

    // Tests constructor with empty key throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_emptyKey_throwsIllegalArgumentException() {
        new Attribute("", "value");
    }

    // Tests constructor with whitespace-only key throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_whitespaceKey_throwsIllegalArgumentException() {
        new Attribute("   ", "value");
    }

    // Tests setKey with valid key and trimming
    @Test
    public void testSetKey_validKey_updatesKey() {
        Attribute attr = new Attribute("key", "val");
        attr.setKey("  newKey  ");
        assertEquals("newKey", attr.getKey());
    }

    // Tests setKey when parent Attributes exists
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

    // Tests setValue without parent attached
    @Test
    public void testSetValue_withoutParent_updatesValue() {
        Attribute attr = new Attribute("key", "val");
        String oldVal = attr.setValue("newVal");
        assertEquals("newVal", attr.getValue());
    }

    // Tests setValue with parent attached
    @Test
    public void testSetValue_withParent_updatesValueAndParent() {
        Attributes parent = new Attributes();
        parent.put("key", "oldVal");
        Attribute attr = new Attribute("key", "oldVal", parent);
        String oldVal = attr.setValue("newVal");
        assertEquals("oldVal", oldVal);
        assertEquals("newVal", attr.getValue());
        assertEquals("newVal", parent.get("key"));
    }

    // Tests createFromEncoded unescapes HTML entities in value
    @Test
    public void testCreateFromEncoded_encodedEntities_unescapesValue() {
        Attribute attr = Attribute.createFromEncoded("title", "&lt;Hello &amp; World&gt;");
        assertEquals("title", attr.getKey());
        assertEquals("<Hello & World>", attr.getValue());
    }

    // Tests isDataAttribute logic
    @Test
    public void testIsDataAttribute_dataAndNonDataKeys_returnsExpected() {
        Attribute dataAttr = new Attribute("data-name", "value");
        Attribute nonDataAttr = new Attribute("name", "value");
        Attribute emptyDataAttr = new Attribute("data-", "value");

        assertTrue(dataAttr.isDataAttribute());
        assertFalse(nonDataAttr.isDataAttribute());
        assertFalse(emptyDataAttr.isDataAttribute());
        assertTrue(Attribute.isDataAttribute("data-custom"));
        assertFalse(Attribute.isDataAttribute("data-"));
    }

    // Tests isBooleanAttribute detection
    @Test
    public void testIsBooleanAttribute_booleanAndNonBoolean_returnsExpected() {
        Attribute boolAttr = new Attribute("required", "");
        Attribute nonBoolAttr = new Attribute("class", "btn");

        assertTrue(boolAttr.isBooleanAttribute());
        assertTrue(Attribute.isBooleanAttribute("disabled"));
        assertTrue(Attribute.isBooleanAttribute("checked"));
        assertFalse(nonBoolAttr.isBooleanAttribute());
        assertFalse(Attribute.isBooleanAttribute("href"));
    }

    // Tests shouldCollapseAttribute under HTML and XML syntax
    @Test
    public void testShouldCollapseAttribute_htmlAndXmlSyntax_collapsesCorrectly() {
        Document.OutputSettings htmlOut = new Document.OutputSettings().syntax(Document.OutputSettings.Syntax.html);
        Document.OutputSettings xmlOut = new Document.OutputSettings().syntax(Document.OutputSettings.Syntax.xml);

        Attribute boolAttr = new Attribute("required", "");
        assertTrue(boolAttr.shouldCollapseAttribute(htmlOut));
        assertFalse(boolAttr.shouldCollapseAttribute(xmlOut));

        Attribute boolSameVal = new Attribute("required", "required");
        assertTrue(boolSameVal.shouldCollapseAttribute(htmlOut));

        Attribute normalAttr = new Attribute("href", "index.html");
        assertFalse(normalAttr.shouldCollapseAttribute(htmlOut));
    }

    // Tests html and toString output formatting
    @Test
    public void testHtml_standardAttribute_formatsCorrectly() {
        Attribute attr = new Attribute("class", "my-class");
        assertEquals("class=\"my-class\"", attr.html());
        assertEquals("class=\"my-class\"", attr.toString());
    }

    // Tests html output for boolean attributes
    @Test
    public void testHtml_booleanAttribute_formatsWithoutValue() throws IOException {
        Attribute attr = new Attribute("disabled", "");
        StringBuilder sb = new StringBuilder();
        Document.OutputSettings settings = new Document.OutputSettings();
        attr.html(sb, settings);
        assertEquals("disabled", sb.toString());
    }

    // Tests equals and hashCode contracts
    @Test
    public void testEqualsAndHashCode_sameAndDifferentAttributes_returnsExpected() {
        Attribute attr1 = new Attribute("key", "val");
        Attribute attr2 = new Attribute("key", "val");
        Attribute attr3 = new Attribute("key", "different");
        Attribute attr4 = new Attribute("different", "val");

        assertEquals(attr1, attr2);
        assertEquals(attr1.hashCode(), attr2.hashCode());
        assertNotEquals(attr1, attr3);
        assertNotEquals(attr1, attr4);
        assertNotEquals(attr1, null);
        assertNotEquals(attr1, "string-object");
    }

    // Tests clone creates independent copy
    @Test
    public void testClone_clonedAttribute_isEqualButIndependent() {
        Attribute original = new Attribute("key", "val");
        Attribute clone = original.clone();

        assertNotSame(original, clone);
        assertEquals(original, clone);

        clone.setKey("newKey");
        assertNotEquals(original.getKey(), clone.getKey());
    }
}