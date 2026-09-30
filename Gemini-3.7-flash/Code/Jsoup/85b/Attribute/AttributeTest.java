package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

public class AttributeTest {

    // Tests constructor with null key throwing exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullKey_throwsException() {
        new Attribute(null, "value");
    }

    // Tests constructor with empty key throwing exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_emptyKey_throwsException() {
        new Attribute("", "value");
    }

    // Tests constructor with whitespace-only key throwing exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_whitespaceOnlyKey_throwsException() {
        new Attribute("   ", "value");
    }

    // Tests normal constructor and getters
    @Test
    public void testConstructor_validInput_setsKeyAndValue() {
        Attribute attr = new Attribute("  key  ", "val");
        assertEquals("key", attr.getKey());
        assertEquals("val", attr.getValue());
    }

    // Tests setKey with valid string
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

    // Tests setKey when attached to parent Attributes
    @Test
    public void testSetKey_withParentAttributes_updatesParent() {
        Attributes parent = new Attributes();
        parent.put("oldKey", "val");
        Attribute attr = new Attribute("oldKey", "val", parent);
        attr.setKey("newKey");
        assertEquals("newKey", attr.getKey());
        assertTrue(parent.hasKey("newKey"));
        assertFalse(parent.hasKey("oldKey"));
    }

    // Tests setValue with parent Attributes
    @Test
    public void testSetValue_withParentAttributes_updatesParentAndReturnsOld() {
        Attributes parent = new Attributes();
        parent.put("key", "oldVal");
        Attribute attr = new Attribute("key", "oldVal", parent);
        String oldVal = attr.setValue("newVal");
        assertEquals("oldVal", oldVal);
        assertEquals("newVal", attr.getValue());
        assertEquals("newVal", parent.get("key"));
    }

    // Tests setValue without parent
    @Test
    public void testSetValue_withoutParent_updatesValue() {
        Attribute attr = new Attribute("key", "oldVal");
        String oldVal = attr.setValue("newVal");
        assertNull(oldVal);
        assertEquals("newVal", attr.getValue());
    }

    // Tests html serialization for standard attribute
    @Test
    public void testHtml_standardAttribute_rendersKeyAndValue() {
        Attribute attr = new Attribute("href", "http://example.com");
        assertEquals("href=\"http://example.com\"", attr.html());
        assertEquals("href=\"http://example.com\"", attr.toString());
    }

    // Tests html serialization for boolean attribute collapsing
    @Test
    public void testHtml_booleanAttribute_collapsesValue() {
        Attribute attr = new Attribute("required", "");
        assertEquals("required", attr.html());

        Attribute attrSame = new Attribute("required", "required");
        assertEquals("required", attrSame.html());

        Attribute attrNull = new Attribute("required", null);
        assertEquals("required", attrNull.html());
    }

    // Tests html serialization with XML syntax output
    @Test
    public void testHtml_xmlSyntax_doesNotCollapseBoolean() throws Exception {
        Document doc = new Document("");
        doc.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
        StringBuilder sb = new StringBuilder();
        Attribute attr = new Attribute("required", "");
        attr.html(sb, doc.outputSettings());
        assertEquals("required=\"\"", sb.toString());
    }

    // Tests createFromEncoded factory method
    @Test
    public void testCreateFromEncoded_encodedValue_unescapesCorrectly() {
        Attribute attr = Attribute.createFromEncoded("title", "&lt;Hello&gt;");
        assertEquals("title", attr.getKey());
        assertEquals("<Hello>", attr.getValue());
    }

    // Tests isDataAttribute method
    @Test
    public void testIsDataAttribute_variousKeys_identifiesCorrectly() {
        Attribute dataAttr = new Attribute("data-name", "value");
        assertTrue(dataAttr.isDataAttribute());

        Attribute nonDataAttr = new Attribute("class", "value");
        assertFalse(nonDataAttr.isDataAttribute());

        Attribute onlyPrefix = new Attribute("data-", "value");
        assertFalse(onlyPrefix.isDataAttribute());
    }

    // Tests isBooleanAttribute method
    @Test
    public void testIsBooleanAttribute_knownAndUnknownKeys_returnsExpected() {
        assertTrue(Attribute.isBooleanAttribute("checked"));
        assertTrue(Attribute.isBooleanAttribute("disabled"));
        assertFalse(Attribute.isBooleanAttribute("href"));
        assertFalse(Attribute.isBooleanAttribute("unknown"));
    }

    // Tests equals and hashCode methods
    @Test
    public void testEqualsAndHashCode_sameAndDifferent_respectsContract() {
        Attribute a1 = new Attribute("key", "val");
        Attribute a2 = new Attribute("key", "val");
        Attribute a3 = new Attribute("other", "val");
        Attribute a4 = new Attribute("key", "other");

        assertEquals(a1, a1);
        assertEquals(a1, a2);
        assertEquals(a1.hashCode(), a2.hashCode());

        assertNotEquals(a1, a3);
        assertNotEquals(a1, a4);
        assertNotEquals(a1, null);
        assertNotEquals(a1, "string");
    }

    // Tests clone method
    @Test
    public void testClone_clonedInstance_equalsOriginal() {
        Attribute attr = new Attribute("key", "val");
        Attribute cloned = attr.clone();
        assertNotSame(attr, cloned);
        assertEquals(attr, cloned);
        assertEquals(attr.getKey(), cloned.getKey());
        assertEquals(attr.getValue(), cloned.getValue());
    }
}