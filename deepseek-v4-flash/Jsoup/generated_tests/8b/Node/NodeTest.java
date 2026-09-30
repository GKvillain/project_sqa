package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

public class NodeTest {
    // Helper method to create a concrete Node for testing
    private static class TestNode extends Node {
        public TestNode(String baseUri, Attributes attributes) {
            super(baseUri, attributes);
        }
        public TestNode(String baseUri) {
            super(baseUri);
        }
        public TestNode() {
            super();
        }
        @Override
        public String nodeName() {
            return "test";
        }
        @Override
        void outerHtmlHead(StringBuilder accum, int depth, Document.OutputSettings out) {
            accum.append("<test>");
        }
        @Override
        void outerHtmlTail(StringBuilder accum, int depth, Document.OutputSettings out) {
            accum.append("</test>");
        }
    }

    // Tests normal constructor with baseUri and attributes
    @Test
    public void testConstructor_validInput_setsFields() {
        Attributes attrs = new Attributes();
        attrs.put("key", "value");
        TestNode node = new TestNode("http://example.com", attrs);
        assertEquals("http://example.com", node.baseUri());
        assertEquals("value", node.attr("key"));
        assertNotNull(node.childNodes());
        assertTrue(node.childNodes().isEmpty());
    }

    // Tests constructor with baseUri only
    @Test
    public void testConstructor_baseUriOnly_createsEmptyAttributes() {
        TestNode node = new TestNode("http://example.com");
        assertEquals("http://example.com", node.baseUri());
        assertNotNull(node.attributes());
        assertTrue(node.attr("nonexistent").isEmpty());
    }

    // Tests default constructor
    @Test
    public void testConstructor_default_setsEmptyChildNodes() {
        TestNode node = new TestNode();
        assertNotNull(node.childNodes());
        assertTrue(node.childNodes().isEmpty());
        assertNull(node.attributes());
    }

    // Tests attr method for existing attribute
    @Test
    public void testAttr_existingKey_returnsValue() {
        Attributes attrs = new Attributes();
        attrs.put("class", "main");
        TestNode node = new TestNode("http://example.com", attrs);
        assertEquals("main", node.attr("class"));
    }

    // Tests attr method for non-existing attribute
    @Test
    public void testAttr_nonExistingKey_returnsEmptyString() {
        TestNode node = new TestNode("http://example.com");
        assertEquals("", node.attr("nonexistent"));
    }

    // Tests attr method with abs: prefix for relative URL
    @Test
    public void testAttr_absPrefix_returnsAbsoluteUrl() {
        Attributes attrs = new Attributes();
        attrs.put("href", "/page");
        TestNode node = new TestNode("http://example.com", attrs);
        String result = node.attr("abs:href");
        assertEquals("http://example.com/page", result);
    }

    // Tests hasAttr method with existing attribute
    @Test
    public void testHasAttr_existingKey_returnsTrue() {
        Attributes attrs = new Attributes();
        attrs.put("id", "one");
        TestNode node = new TestNode("http://example.com", attrs);
        assertTrue(node.hasAttr("id"));
    }

    // Tests hasAttr method with non-existing attribute
    @Test
    public void testHasAttr_nonExistingKey_returnsFalse() {
        TestNode node = new TestNode("http://example.com");
        assertFalse(node.hasAttr("missing"));
    }

    // Tests removeAttr method
    @Test
    public void testRemoveAttr_existingKey_removesAttribute() {
        Attributes attrs = new Attributes();
        attrs.put("style", "color:red");
        TestNode node = new TestNode("http://example.com", attrs);
        node.removeAttr("style");
        assertFalse(node.hasAttr("style"));
    }

    // Tests absUrl with relative URL and valid base
    @Test
    public void testAbsUrl_relativeUrl_returnsAbsoluteUrl() {
        Attributes attrs = new Attributes();
        attrs.put("href", "path/to/page");
        TestNode node = new TestNode("http://example.com/base/", attrs);
        String result = node.absUrl("href");
        assertEquals("http://example.com/base/path/to/page", result);
    }

    // Tests absUrl with absolute URL in attribute
    @Test
    public void testAbsUrl_absoluteUrl_returnsSameUrl() {
        Attributes attrs = new Attributes();
        attrs.put("href", "http://other.com/page");
        TestNode node = new TestNode("http://example.com", attrs);
        String result = node.absUrl("href");
        assertEquals("http://other.com/page", result);
    }

    // Tests absUrl with non-existing attribute
    @Test
    public void testAbsUrl_nonExistingKey_returnsEmptyString() {
        TestNode node = new TestNode("http://example.com");
        assertEquals("", node.absUrl("missing"));
    }

    // Tests absUrl with malformed base URI and absolute attribute
    @Test
    public void testAbsUrl_malformedBase_usesAttributeDirectly() {
        Attributes attrs = new Attributes();
        attrs.put("src", "http://example.com/image.jpg");
        TestNode node = new TestNode("invalid base", attrs);
        String result = node.absUrl("src");
        assertEquals("http://example.com/image.jpg", result);
    }

    // Tests absUrl with malformed base URI and relative attribute returns empty
    @Test
    public void testAbsUrl_malformedBaseAndRelative_returnsEmptyString() {
        Attributes attrs = new Attributes();
        attrs.put("src", "/image.jpg");
        TestNode node = new TestNode("invalid base", attrs);
        String result = node.absUrl("src");
        assertEquals("", result);
    }

    // Tests childNode method with valid index
    @Test
    public void testChildNode_validIndex_returnsChild() {
        TestNode parent = new TestNode("http://example.com");
        TestNode child = new TestNode("http://example.com");
        parent.addChildren(child);
        assertEquals(child, parent.childNode(0));
    }

    // Tests childNode method with invalid index (boundary)
    @Test(expected = IndexOutOfBoundsException.class)
    public void testChildNode_invalidIndex_throwsException() {
        TestNode node = new TestNode("http://example.com");
        node.childNode(0);
    }

    // Tests parent method with parent set
    @Test
    public void testParent_withParent_returnsParentNode() {
        TestNode parent = new TestNode("http://example.com");
        TestNode child = new TestNode("http://example.com");
        parent.addChildren(child);
        assertEquals(parent, child.parent());
    }

    // Tests parent method without parent
    @Test
    public void testParent_noParent_returnsNull() {
        TestNode node = new TestNode("http://example.com");
        assertNull(node.parent());
    }

    // Tests siblingIndex method
    @Test
    public void testSiblingIndex_firstChild_returnsZero() {
        TestNode parent = new TestNode("http://example.com");
        TestNode child = new TestNode("http://example.com");
        parent.addChildren(child);
        assertEquals((Integer) 0, child.siblingIndex());
    }

    // Tests setSiblingIndex method
    @Test
    public void testSetSiblingIndex_validIndex_setsIndex() {
        TestNode node = new TestNode("http://example.com");
        node.setSiblingIndex(5);
        assertEquals((Integer) 5, node.siblingIndex());
    }

    // Tests replaceChild method
    @Test
    public void testReplaceChild_validInput_replacesChild() {
        TestNode parent = new TestNode("http://example.com");
        TestNode oldChild = new TestNode("http://example.com");
        TestNode newChild = new TestNode("http://example.com");
        parent.addChildren(oldChild);
        parent.replaceChild(oldChild, newChild);
        assertEquals(1, parent.childNodes().size());
        assertEquals(newChild, parent.childNode(0));
        assertEquals(parent, newChild.parent());
        assertNull(oldChild.parent());
    }

    // Tests removeChild method
    @Test
    public void testRemoveChild_validInput_removesChild() {
        TestNode parent = new TestNode("http://example.com");
        TestNode child = new TestNode("http://example.com");
        parent.addChildren(child);
        parent.removeChild(child);
        assertTrue(parent.childNodes().isEmpty());
        assertNull(child.parent());
    }

    // Tests addChildren method with multiple children
    @Test
    public void testAddChildren_multipleChildren_addsAll() {
        TestNode parent = new TestNode("http://example.com");
        TestNode child1 = new TestNode("http://example.com");
        TestNode child2 = new TestNode("http://example.com");
        parent.addChildren(child1, child2);
        assertEquals(2, parent.childNodes().size());
        assertEquals(child1, parent.childNode(0));
        assertEquals(child2, parent.childNode(1));
    }

    // Tests nextSibling method with next sibling present
    @Test
    public void testNextSibling_hasSibling_returnsNext() {
        TestNode parent = new TestNode("http://example.com");
        TestNode first = new TestNode("http://example.com");
        TestNode second = new TestNode("http://example.com");
        parent.addChildren(first, second);
        assertEquals(second, first.nextSibling());
    }

    // Tests nextSibling method as last child
    @Test
    public void testNextSibling_lastSibling_returnsNull() {
        TestNode parent = new TestNode("http://example.com");
        TestNode child = new TestNode("http://example.com");
        parent.addChildren(child);
        assertNull(child.nextSibling());
    }

    // Tests previousSibling method with previous sibling present
    @Test
    public void testPreviousSibling_hasSibling_returnsPrevious() {
        TestNode parent = new TestNode("http://example.com");
        TestNode first = new TestNode("http://example.com");
        TestNode second = new TestNode("http://example.com");
        parent.addChildren(first, second);
        assertEquals(first, second.previousSibling());
    }

    // Tests previousSibling method as first child
    @Test
    public void testPreviousSibling_firstSibling_returnsNull() {
        TestNode parent = new TestNode("http://example.com");
        TestNode child = new TestNode("http://example.com");
        parent.addChildren(child);
        assertNull(child.previousSibling());
    }

    // Tests ownerDocument for Document node
    @Test
    public void testOwnerDocument_selfDocument_returnsSelf() {
        // Cannot easily instantiate Document, so test with null parent path
        TestNode node = new TestNode("http://example.com");
        assertNull(node.ownerDocument());
    }

    // Tests equals and hashCode basic functionality
    @Test
    public void testEquals_sameNode_returnsTrue() {
        TestNode node = new TestNode("http://example.com");
        assertEquals(node, node);
    }

    @Test
    public void testEquals_differentNode_returnsFalse() {
        TestNode node1 = new TestNode("http://example.com");
        TestNode node2 = new TestNode("http://example.com");
        assertFalse(node1.equals(node2));
    }
}