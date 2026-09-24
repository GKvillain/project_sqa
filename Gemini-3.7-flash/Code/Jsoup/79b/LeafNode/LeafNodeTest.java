package org.jsoup.nodes;

import org.junit.Test;

import static org.junit.Assert.*;

public class LeafNodeTest {

    private static class TestLeafNode extends LeafNode {
        private final String name;

        TestLeafNode(String name, String value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public String nodeName() {
            return name;
        }

        @Override
        void outerHtmlHead(Appendable accum, int depth, Document.OutputSettings out) {
        }

        @Override
        void outerHtmlTail(Appendable accum, int depth, Document.OutputSettings out) {
        }
    }

    // Tests reading attribute value matching the nodeName before attributes map is initialized
    @Test
    public void testAttr_matchingNodeName_returnsCoreValue() {
        LeafNode node = new TestLeafNode("testNode", "coreText");
        assertEquals("coreText", node.attr("testNode"));
        assertFalse(node.hasAttributes());
    }

    // Tests reading non-matching attribute key when attributes map is not initialized
    @Test
    public void testAttr_nonMatchingKey_returnsEmptyString() {
        LeafNode node = new TestLeafNode("testNode", "coreText");
        assertEquals("", node.attr("otherKey"));
        assertFalse(node.hasAttributes());
    }

    // Tests null key validation in attr method
    @Test(expected = IllegalArgumentException.class)
    public void testAttr_nullKey_throwsIllegalArgumentException() {
        LeafNode node = new TestLeafNode("testNode", "coreText");
        node.attr(null);
    }

    // Tests updating attribute with matching nodeName modifies value field without creating Attributes object
    @Test
    public void testAttr_updateMatchingKey_updatesValueDirectly() {
        LeafNode node = new TestLeafNode("testNode", "initialValue");
        node.attr("testNode", "updatedValue");
        assertEquals("updatedValue", node.attr("testNode"));
        assertEquals("updatedValue", node.coreValue());
        assertFalse(node.hasAttributes());
    }

    // Tests setting a different attribute key creates Attributes object and stores both attributes
    @Test
    public void testAttr_setDifferentKey_expandsToAttributes() {
        LeafNode node = new TestLeafNode("testNode", "coreText");
        node.attr("customAttr", "customValue");
        assertTrue(node.hasAttributes());
        assertEquals("coreText", node.attr("testNode"));
        assertEquals("customValue", node.attr("customAttr"));
    }

    // Tests attributes() ensures Attributes object is created with coreValue populated
    @Test
    public void testAttributes_whenCalled_populatesAttributesObject() {
        LeafNode node = new TestLeafNode("testNode", "coreText");
        Attributes attributes = node.attributes();
        assertNotNull(attributes);
        assertTrue(node.hasAttributes());
        assertEquals("coreText", attributes.get("testNode"));
    }

    // Tests attributes() when value field is initially null
    @Test
    public void testAttributes_whenValueIsNull_createsEmptyAttributes() {
        LeafNode node = new TestLeafNode("testNode", null);
        Attributes attributes = node.attributes();
        assertNotNull(attributes);
        assertTrue(node.hasAttributes());
        assertEquals("", attributes.get("testNode"));
    }

    // Tests coreValue getter and setter
    @Test
    public void testCoreValue_getAndSet_updatesCorrectly() {
        LeafNode node = new TestLeafNode("testNode", "val1");
        assertEquals("val1", node.coreValue());
        node.coreValue("val2");
        assertEquals("val2", node.coreValue());
    }

    // Tests hasAttr for both existing and non-existing attributes
    @Test
    public void testHasAttr_existingAndNonExisting_returnsExpectedBoolean() {
        LeafNode node = new TestLeafNode("testNode", "val");
        assertTrue(node.hasAttr("testNode"));
        assertFalse(node.hasAttr("nonExistent"));
    }

    // Tests removing an attribute through removeAttr
    @Test
    public void testRemoveAttr_existingKey_removesAttribute() {
        LeafNode node = new TestLeafNode("testNode", "val");
        node.removeAttr("testNode");
        assertFalse(node.hasAttr("testNode"));
        assertEquals("", node.attr("testNode"));
    }

    // Tests absUrl behavior on LeafNode
    @Test
    public void testAbsUrl_noBaseUri_returnsEmptyString() {
        LeafNode node = new TestLeafNode("testNode", "http://example.com");
        assertEquals("", node.absUrl("testNode"));
    }

    // Tests baseUri when node has no parent
    @Test
    public void testBaseUri_withoutParent_returnsEmptyString() {
        LeafNode node = new TestLeafNode("testNode", "val");
        assertEquals("", node.baseUri());
    }

    // Tests baseUri when node has a parent with baseUri
    @Test
    public void testBaseUri_withParent_returnsParentBaseUri() {
        Element parent = new Element("div");
        parent.setBaseUri("http://example.com/");
        LeafNode node = new TestLeafNode("testNode", "val");
        parent.appendChild(node);
        assertEquals("http://example.com/", node.baseUri());
    }

    // Tests doSetBaseUri is a no-op
    @Test
    public void testDoSetBaseUri_called_doesNotChangeBaseUri() {
        LeafNode node = new TestLeafNode("testNode", "val");
        node.doSetBaseUri("http://example.com/");
        assertEquals("", node.baseUri());
    }

    // Tests childNodeSize returns 0 for LeafNode
    @Test
    public void testChildNodeSize_whenCalled_returnsZero() {
        LeafNode node = new TestLeafNode("testNode", "val");
        assertEquals(0, node.childNodeSize());
    }

    // Tests ensureChildNodes throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testEnsureChildNodes_whenCalled_throwsUnsupportedOperationException() {
        LeafNode node = new TestLeafNode("testNode", "val");
        node.ensureChildNodes();
    }
}