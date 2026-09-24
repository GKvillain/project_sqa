package org.jsoup.nodes;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class AttributeTest {

    // Tests normal attribute creation and getter methods
    @Test
    public void testConstructor_validKeyAndValue_createsAttribute() {
        Attribute attr = new Attribute("href", "http://example.com");
        assertEquals("href", attr.getKey());
        assertEquals("http://example.com", attr.getValue());
    }

    // Tests trimming of key in constructor
    @Test
    public void testConstructor_keyWithSurroundingWhitespace_trimsKey() {
        Attribute attr = new Attribute("  key  ", "val");
        assertEquals("key", attr.getKey());
    }

    // Tests null key in constructor throwing exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullKey_throwsException() {
        new Attribute(null, "val");
    }

    // Tests empty string key in constructor throwing exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_emptyKey_throwsException() {
        new Attribute("", "val");
    }

    // Tests whitespace-only key in constructor throwing exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_whitespaceOnlyKey_throwsException() {
        new Attribute("   ", "val");
    }

    // Tests setKey with valid string and trimming
    @Test
    public void testSetKey_validKey_updatesKey() {
        Attribute attr = new Attribute("key", "val");
        attr.setKey("  newKey  ");
        assertEquals("newKey", attr.getKey());
    }

    // Tests setKey with whitespace-only string throwing exception
    @Test(expected = IllegalArgumentException.class)
    public void testSetKey_whitespaceOnlyKey_throwsException() {
        Attribute attr = new Attribute("key", "val");
        attr.setKey("   ");
    }

    // Tests setKey updates key in parent Attributes collection
    @Test
    public void testSetKey_withParentAttributes_updatesParentKey() {
        Attributes parent = new Attributes();
        parent.put("oldKey", "val");
        Attribute attr = new Attribute("oldKey", "val", parent);
        attr.setKey("newKey");

        assertEquals("newKey", attr.getKey());
        assertTrue(parent.hasKey("newKey"));
        assertFalse(parent.hasKey("oldKey"));
    }

    // Tests setValue updates value and returns previous value
    @Test
    public void testSetValue_withParentAttributes_updatesParentAndReturnsOldValue() {
        Attributes parent = new Attributes();
        parent.put("key", "oldVal");
        Attribute attr = new Attribute("key", "oldVal", parent);

        String oldVal = attr.setValue("newVal");
        assertEquals("oldVal", oldVal);
        assertEquals("newVal", attr.getValue());
        assertEquals("newVal", parent.get("key"));
    }

    // Tests HTML rendering of standard attribute
    @Test
    public void testHtml_standardAttribute_returnsFormattedHtml() {
        Attribute attr = new Attribute("class", "btn btn-primary");
        assertEquals("class=\"btn btn-primary\"", attr.html());
        assertEquals("class=\"btn btn-primary\"", attr.toString());
    }

    // Tests HTML rendering and collapsing of boolean attribute in HTML mode
    @Test
    public void testHtml_booleanAttributeHtmlSyntax_collapsesValue() {
        Attribute attr = new Attribute("disabled", "");
        assertEquals("disabled", attr.html());

        Attribute attrSameVal = new Attribute("disabled", "disabled");
        assertEquals("disabled", attrSameVal.html());
    }

    // Tests HTML rendering of boolean attribute in XML mode without collapsing
    @Test
    public void testHtml_booleanAttributeXmlSyntax_doesNotCollapse() throws IOException {
        Attribute attr = new Attribute("disabled", "");
        StringBuilder sb = new StringBuilder();
        Document.OutputSettings settings = new Document.OutputSettings().syntax(Document.OutputSettings.Syntax.xml);
        attr.html(sb, settings);

        assertEquals("disabled=\"\"", sb.toString());
    }

    // Tests createFromEncoded unescapes HTML entities in attribute value
    @Test
    public void testCreateFromEncoded_encodedEntities_unescapesValue() {
        Attribute attr = Attribute.createFromEncoded("title", "&lt;Hello &amp; World&gt;");
        assertEquals("title", attr.getKey());
        assertEquals("<Hello & World>", attr.getValue());
    }

    // Tests isDataAttribute recognition for data-* prefix
    @Test
    public void testIsDataAttribute_dataPrefix_returnsCorrectBoolean() {
        Attribute dataAttr = new Attribute("data-id", "123");
        assertTrue(dataAttr.isDataAttribute());

        Attribute notDataAttr = new Attribute("custom-data", "123");
        assertFalse(notDataAttr.isDataAttribute());

        Attribute onlyPrefix = new Attribute("data-", "123");
        assertFalse(onlyPrefix.isDataAttribute());
    }

    // Tests isBooleanAttribute detection
    @Test
    public void testIsBooleanAttribute_standardBooleanKeys_returnsTrue() {
        assertTrue(Attribute.isBooleanAttribute("checked"));
        assertTrue(Attribute.isBooleanAttribute("disabled"));
        assertFalse(Attribute.isBooleanAttribute("href"));
    }

    // Tests equality and hashcode contracts
    @Test
    public void testEqualsAndHashCode_sameAndDifferentAttributes_behavesCorrectly() {
        Attribute attr1 = new Attribute("key", "value");
        Attribute attr2 = new Attribute("key", "value");
        Attribute attr3 = new Attribute("key", "different");
        Attribute attr4 = new Attribute("other", "value");

        assertEquals(attr1, attr1);
        assertEquals(attr1, attr2);
        assertEquals(attr1.hashCode(), attr2.hashCode());

        assertNotEquals(attr1, attr3);
        assertNotEquals(attr1, attr4);
        assertNotEquals(attr1, null);
        assertNotEquals(attr1, "key=\"value\"");
    }

    // Tests cloning creates an independent copy
    @Test
    public void testClone_validAttribute_createsExactCopy() {
        Attribute original = new Attribute("key", "val");
        Attribute cloned = original.clone();

        assertNotSame(original, cloned);
        assertEquals(original, cloned);
        assertEquals(original.getKey(), cloned.getKey());
        assertEquals(original.getValue(), cloned.getValue());
    }
}