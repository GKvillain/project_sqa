package org.jsoup.nodes;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;

public class LeafNodeTest {
    private TextNode textNode;

    @Before
    public void setUp() {
        textNode = new TextNode("test");
    }

    @Test
    public void testHasAttributes_noAttributes_returnsFalse() {
        assertFalse(textNode.hasAttributes());
    }

    @Test
    public void testHasAttributes_afterSettingNonNodeNameAttr_returnsTrue() {
        textNode.attr("class", "test");
        assertTrue(textNode.hasAttributes());
    }

    @Test
    public void testAttributes_noAttributes_returnsAttributesWithNodeName() {
        Attributes attrs = textNode.attributes();
        assertNotNull(attrs);
        assertEquals("test", attrs.get("#text"));
        assertTrue(textNode.hasAttributes());
    }

    @Test
    public void testAttr_keyEqualsNodeName_noAttributes_returnsValue() {
        assertEquals("test", textNode.attr("#text"));
    }

    @Test
    public void testAttr_keyNotEqualsNodeName_noAttributes_returnsEmptyString() {
        assertEquals("", textNode.attr("foo"));
    }

    @Test
    public void testAttr_setKeyEqualsNodeName_noAttributes_setsValue() {
        textNode.attr("#text", "new");
        assertEquals("new", textNode.coreValue());
        assertFalse(textNode.hasAttributes());
    }

    @Test
    public void testAttr_setKeyNotEqualsNodeName_noAttributes_createsAttributes() {
        textNode.attr("class", "test");
        assertTrue(textNode.hasAttributes());
        assertEquals("test", textNode.attr("class"));
    }

    @Test
    public void testAttr_keyNotEqualsNodeName_withAttributes_returnsAttributeValue() {
        textNode.attr("class", "test");
        assertEquals("test", textNode.attr("class"));
    }

    @Test
    public void testAttr_keyEqualsNodeName_withAttributes_returnsNodeNameAttrValue() {
        textNode.attr("class", "test");
        textNode.attr("#text", "updated");
        assertEquals("updated", textNode.attr("#text"));
        assertEquals("updated", textNode.coreValue());
    }

    @Test
    public void testHasAttr_keyNotPresent_returnsFalse() {
        assertFalse(textNode.hasAttr("missing"));
        assertTrue(textNode.hasAttributes());
    }

    @Test
    public void testHasAttr_keyPresent_returnsTrue() {
        textNode.attr("class", "test");
        assertTrue(textNode.hasAttr("class"));
    }

    @Test
    public void testRemoveAttr_keyPresent_removes() {
        textNode.attr("class", "test");
        assertTrue(textNode.hasAttr("class"));
        textNode.removeAttr("class");
        assertFalse(textNode.hasAttr("class"));
    }

    @Test
    public void testAbsUrl_ensureAttributes_notNull() {
        String absUrl = textNode.absUrl("someKey");
        assertNotNull(absUrl);
    }

    @Test
    public void testBaseUri_noParent_returnsEmpty() {
        assertEquals("", textNode.baseUri());
    }

    @Test
    public void testChildNodeSize_returnsZero() {
        assertEquals(0, textNode.childNodeSize());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testEnsureChildNodes_throwsUnsupportedOperationException() {
        textNode.ensureChildNodes();
    }

    @Test
    public void testCoreValue_returnsNodeNameAttrValue() {
        assertEquals("test", textNode.coreValue());
    }

    @Test
    public void testCoreValue_setValue_updates() {
        textNode.coreValue("newValue");
        assertEquals("newValue", textNode.coreValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAttr_nullKey_throwsIllegalArgumentException() {
        textNode.attr(null);
    }

    @Test(expected = NullPointerException.class)
    public void testAttr_twoArgs_nullKey_throwsNullPointerException() {
        textNode.attr(null, "value");
    }
}