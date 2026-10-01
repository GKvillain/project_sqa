package org.jsoup.nodes;

import org.junit.Test;

import static org.junit.Assert.*;

public class LeafNodeTest {

    private static class ConcreteLeafNode extends LeafNode {
        ConcreteLeafNode() {
        }

        ConcreteLeafNode(String value) {
            this.value = value;
        }

        @Override
        public String nodeName() {
            return "leaf";
        }

        @Override
        void outerHtmlHead(Appendable accum, int depth, Document.OutputSettings out) {
        }

        @Override
        void outerHtmlTail(Appendable accum, int depth, Document.OutputSettings out) {
        }
    }

    // Tests that childNodeSize always returns 0 for a leaf node
    @Test
    public void testChildNodeSize_always_returnsZero() {
        ConcreteLeafNode leaf = new ConcreteLeafNode("text");
        assertEquals(0, leaf.childNodeSize());
    }

    // Tests that ensureChildNodes throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testEnsureChildNodes_onLeafNode_throwsException() {
        ConcreteLeafNode leaf = new ConcreteLeafNode("text");
        leaf.ensureChildNodes();
    }

    // Tests getting core value when set via constructor
    @Test
    public void testCoreValue_initialValue_returnsValue() {
        ConcreteLeafNode leaf = new ConcreteLeafNode("sampleValue");
        assertEquals("sampleValue", leaf.coreValue());
    }

    // Tests setting and getting core value
    @Test
    public void testCoreValue_setValue_updatesCoreValue() {
        ConcreteLeafNode leaf = new ConcreteLeafNode("initial");
        leaf.coreValue("updated");
        assertEquals("updated", leaf.coreValue());
    }

    // Tests attr with matching nodeName returns core value without creating attributes
    @Test
    public void testAttr_matchingNodeNameWithoutAttributes_returnsValue() {
        ConcreteLeafNode leaf = new ConcreteLeafNode("myValue");
        assertEquals("myValue", leaf.attr("leaf"));
        assertFalse(leaf.hasAttributes());
    }

    // Tests attr with non-matching key returns empty string without creating attributes
    @Test
    public void testAttr_nonMatchingKeyWithoutAttributes_returnsEmptyString() {
        ConcreteLeafNode leaf = new ConcreteLeafNode("myValue");
        assertEquals("", leaf.attr("otherKey"));
        assertFalse(leaf.hasAttributes());
    }

    // Tests attr with null key throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testAttr_nullKey_throwsException() {
        ConcreteLeafNode leaf = new ConcreteLeafNode("myValue");
        leaf.attr(null);
    }

    // Tests setting attr for nodeName when attributes are not initialized updates core value directly
    @Test
    public void testAttr_setMatchingNodeNameWithoutAttributes_updatesValueWithoutAttributes() {
        ConcreteLeafNode leaf = new ConcreteLeafNode("initial");
        leaf.attr("leaf", "modified");
        assertEquals("modified", leaf.coreValue());
        assertFalse(leaf.hasAttributes());
    }

    // Tests setting an attribute other than nodeName transitions leaf to use Attributes object
    @Test
    public void testAttr_setDifferentKey_createsAttributesAndSetsValue() {
        ConcreteLeafNode leaf = new ConcreteLeafNode("core");
        leaf.attr("class", "bold");

        assertTrue(leaf.hasAttributes());
        assertEquals("bold", leaf.attr("class"));
        assertEquals("core", leaf.attr("leaf"));
    }

    // Tests hasAttr transitions to attributes and returns true for existing attribute
    @Test
    public void testHasAttr_existingCoreValue_returnsTrue() {
        ConcreteLeafNode leaf = new ConcreteLeafNode("test");
        assertTrue(leaf.hasAttr("leaf"));
        assertTrue(leaf.hasAttributes());
    }

    // Tests hasAttr returns false for non-existing attribute
    @Test
    public void testHasAttr_nonExistingKey_returnsFalse() {
        ConcreteLeafNode leaf = new ConcreteLeafNode("test");
        assertFalse(leaf.hasAttr("href"));
    }

    // Tests removeAttr removes the attribute
    @Test
    public void testRemoveAttr_existingAttribute_removesAttribute() {
        ConcreteLeafNode leaf = new ConcreteLeafNode("test");
        leaf.removeAttr("leaf");
        assertFalse(leaf.hasAttr("leaf"));
        assertEquals("", leaf.attr("leaf"));
    }

    // Tests absUrl on leaf node without base URI returns empty string
    @Test
    public void testAbsUrl_noBaseUri_returnsEmptyString() {
        ConcreteLeafNode leaf = new ConcreteLeafNode("test");
        assertEquals("", leaf.absUrl("href"));
    }

    // Tests baseUri returns empty string when leaf node has no parent
    @Test
    public void testBaseUri_noParent_returnsEmptyString() {
        ConcreteLeafNode leaf = new ConcreteLeafNode("test");
        assertEquals("", leaf.baseUri());
    }

    // Tests baseUri returns parent baseUri when parent is present
    @Test
    public void testBaseUri_withParent_returnsParentBaseUri() {
        Element parent = new Element("div");
        parent.setBaseUri("https://example.com/");
        ConcreteLeafNode leaf = new ConcreteLeafNode("test");
        parent.appendChild(leaf);

        assertEquals("https://example.com/", leaf.baseUri());
    }

    // Tests doSetBaseUri is a no-op
    @Test
    public void testDoSetBaseUri_called_isNoOp() {
        ConcreteLeafNode leaf = new ConcreteLeafNode("test");
        leaf.doSetBaseUri("https://example.com/");
        assertEquals("", leaf.baseUri());
    }

    // Tests attributes() ensures and returns non-null Attributes instance
    @Test
    public void testAttributes_called_returnsNonNullAttributes() {
        ConcreteLeafNode leaf = new ConcreteLeafNode("test");
        Attributes attrs = leaf.attributes();
        assertNotNull(attrs);
        assertTrue(leaf.hasAttributes());
        assertEquals("test", attrs.get("leaf"));
    }

    // Tests attributes() on leaf node with null initial value
    @Test
    public void testAttributes_nullInitialValue_returnsEmptyAttributes() {
        ConcreteLeafNode leaf = new ConcreteLeafNode(null);
        Attributes attrs = leaf.attributes();
        assertNotNull(attrs);
        assertTrue(leaf.hasAttributes());
        assertEquals(0, attrs.size());
    }
}