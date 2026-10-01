package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

public class NodeTest {

    // Test constructor with valid baseUri and attributes
    @Test
    public void testConstructor_validInputs_createsNode() {
        Attributes attrs = new Attributes();
        attrs.put("id", "test");
        TestNode node = new TestNode("http://example.com", attrs);
        assertEquals("http://example.com", node.baseUri());
        assertNotNull(node.attributes());
        assertTrue(node.hasAttr("id"));
        assertEquals(0, node.childNodes().size());
    }

    // Test attr method returns attribute value for existing key
    @Test
    public void testAttr_existingKey_returnsValue() {
        Attributes attrs = new Attributes();
        attrs.put("class", "main");
        TestNode node = new TestNode("http://example.com", attrs);
        assertEquals("main", node.attr("class"));
    }

    // Test attr method returns empty string for nonexistent key (non-abs)
    @Test
    public void testAttr_nonexistentKey_returnsEmptyString() {
        TestNode node = new TestNode("http://example.com");
        assertEquals("", node.attr("nonexistent"));
    }

    // Test attr method returns absolute URL when key starts with "abs:"
    @Test
    public void testAttr_absPrefixWithValidRelUrl_returnsAbsoluteUrl() {
        Attributes attrs = new Attributes();
        attrs.put("href", "/page");
        TestNode node = new TestNode("http://example.com", attrs);
        String result = node.attr("abs:href");
        assertTrue(result.equals("http://example.com/page"));
    }

    // Test hasAttr returns true when attribute exists
    @Test
    public void testHasAttr_existingKey_returnsTrue() {
        Attributes attrs = new Attributes();
        attrs.put("data-value", "123");
        TestNode node = new TestNode("http://example.com", attrs);
        assertTrue(node.hasAttr("data-value"));
    }

    // Test hasAttr returns false when attribute does not exist
    @Test
    public void testHasAttr_nonexistentKey_returnsFalse() {
        TestNode node = new TestNode("http://example.com");
        assertFalse(node.hasAttr("missing"));
    }

    // Test removeAttr removes existing attribute
    @Test
    public void testRemoveAttr_existingKey_attributeRemoved() {
        Attributes attrs = new Attributes();
        attrs.put("style", "color:red");
        TestNode node = new TestNode("http://example.com", attrs);
        node.removeAttr("style");
        assertFalse(node.hasAttr("style"));
    }

    // Test setBaseUri updates baseUri
    @Test
    public void testSetBaseUri_newUri_returnsUpdatedUri() {
        TestNode node = new TestNode("http://old.com");
        node.setBaseUri("http://new.com");
        assertEquals("http://new.com", node.baseUri());
    }

    // Test absUrl with existing relative URL returns absolute URL
    @Test
    public void testAbsUrl_validRelUrl_returnsAbsoluteUrl() {
        Attributes attrs = new Attributes();
        attrs.put("href", "/path");
        TestNode node = new TestNode("http://example.com", attrs);
        String result = node.absUrl("href");
        assertEquals("http://example.com/path", result);
    }

    // Test absUrl with missing attribute returns empty string
    @Test
    public void testAbsUrl_missingAttribute_returnsEmpty() {
        TestNode node = new TestNode("http://example.com");
        assertEquals("", node.absUrl("href"));
    }

    // Test childNode returns correct child at index
    @Test
    public void testChildNode_validIndex_returnsChild() {
        TestNode parent = new TestNode("http://example.com");
        TestNode child = new TestNode("http://example.com");
        parent.addChildren(child);
        assertSame(child, parent.childNode(0));
    }

    // Test childNodes returns unmodifiable list
    @Test
    public void testChildNodes_returnsUnmodifiableList() {
        TestNode parent = new TestNode("http://example.com");
        try {
            parent.childNodes().add(new TestNode("http://example.com"));
            fail("Should have thrown UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    // Test parent returns parent node
    @Test
    public void testParent_withParent_returnsParent() {
        TestNode parent = new TestNode("http://example.com");
        TestNode child = new TestNode("http://example.com");
        parent.addChildren(child);
        assertSame(parent, child.parent());
    }

    // Test parent returns null for orphan node
    @Test
    public void testParent_noParent_returnsNull() {
        TestNode node = new TestNode("http://example.com");
        assertNull(node.parent());
    }

    // Test remove detaches node from parent
    @Test
    public void testRemove_nodeWithParent_detachesFromParent() {
        TestNode parent = new TestNode("http://example.com");
        TestNode child = new TestNode("http://example.com");
        parent.addChildren(child);
        child.remove();
        assertNull(child.parent());
        assertEquals(0, parent.childNodes().size());
    }

    // Test replaceWith replaces node in parent
    @Test
    public void testReplaceWith_validNode_replacesInParent() {
        TestNode parent = new TestNode("http://example.com");
        TestNode child1 = new TestNode("http://example.com");
        TestNode child2 = new TestNode("http://example.com");
        parent.addChildren(child1);
        child1.replaceWith(child2);
        assertSame(child2, parent.childNode(0));
        assertNull(child1.parent());
    }

    // Test siblingIndex returns correct index
    @Test
    public void testSiblingIndex_firstChild_returnsZero() {
        TestNode parent = new TestNode("http://example.com");
        TestNode child = new TestNode("http://example.com");
        parent.addChildren(child);
        assertEquals(0, (int) child.siblingIndex());
    }

    // Test nextSibling returns next sibling
    @Test
    public void testNextSibling_withNextSibling_returnsSibling() {
        TestNode parent = new TestNode("http://example.com");
        TestNode child1 = new TestNode("http://example.com");
        TestNode child2 = new TestNode("http://example.com");
        parent.addChildren(child1, child2);
        assertSame(child2, child1.nextSibling());
    }

    // Test nextSibling returns null for last child
    @Test
    public void testNextSibling_lastChild_returnsNull() {
        TestNode parent = new TestNode("http://example.com");
        TestNode child = new TestNode("http://example.com");
        parent.addChildren(child);
        assertNull(child.nextSibling());
    }

    // Test previousSibling returns previous sibling
    @Test
    public void testPreviousSibling_withPreviousSibling_returnsSibling() {
        TestNode parent = new TestNode("http://example.com");
        TestNode child1 = new TestNode("http://example.com");
        TestNode child2 = new TestNode("http://example.com");
        parent.addChildren(child1, child2);
        assertSame(child1, child2.previousSibling());
    }

    // Test previousSibling returns null for first child
    @Test
    public void testPreviousSibling_firstChild_returnsNull() {
        TestNode parent = new TestNode("http://example.com");
        TestNode child = new TestNode("http://example.com");
        parent.addChildren(child);
        assertNull(child.previousSibling());
    }

    // Test clone creates deep copy with no parent
    @Test
    public void testClone_orphanNode_createsDeepCopy() {
        TestNode node = new TestNode("http://example.com");
        TestNode clone = (TestNode) node.clone();
        assertNotSame(node, clone);
        assertNull(clone.parent());
        assertEquals(node.baseUri(), clone.baseUri());
    }

    // Test ownerDocument returns null for orphan node
    @Test
    public void testOwnerDocument_orphanNode_returnsNull() {
        TestNode node = new TestNode("http://example.com");
        assertNull(node.ownerDocument());
    }

    // Test outerHtml returns non-null string for simple node
    @Test
    public void testOuterHtml_returnsNonEmptyString() {
        TestNode node = new TestNode("http://example.com");
        assertNotNull(node.outerHtml());
    }

    // Test toString returns outerHtml
    @Test
    public void testToString_equalsOuterHtml() {
        TestNode node = new TestNode("http://example.com");
        assertEquals(node.outerHtml(), node.toString());
    }

    // Concrete implementation of abstract Node for testing
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
            return "#test";
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
}