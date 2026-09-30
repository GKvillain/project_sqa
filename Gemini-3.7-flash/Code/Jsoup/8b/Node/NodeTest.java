package org.jsoup.nodes;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class NodeTest {

    private static class TestNode extends Node {
        private final String name;

        TestNode(String baseUri, Attributes attributes) {
            super(baseUri, attributes);
            this.name = "test";
        }

        TestNode(String baseUri) {
            super(baseUri);
            this.name = "test";
        }

        TestNode() {
            super();
            this.name = "test";
        }

        @Override
        public String nodeName() {
            return name;
        }

        @Override
        void outerHtmlHead(StringBuilder accum, int depth, Document.OutputSettings out) {
            accum.append("<").append(nodeName()).append(">");
        }

        @Override
        void outerHtmlTail(StringBuilder accum, int depth, Document.OutputSettings out) {
            accum.append("</").append(nodeName()).append(">");
        }
    }

    // Tests attribute get, set, has, and remove
    @Test
    public void testAttr_setAndGetAndRemove_returnsExpectedValues() {
        TestNode node = new TestNode("http://example.com");
        node.attr("key1", "val1");

        assertTrue(node.hasAttr("key1"));
        assertEquals("val1", node.attr("key1"));

        node.removeAttr("key1");
        assertFalse(node.hasAttr("key1"));
        assertEquals("", node.attr("key1"));
    }

    // Tests attr with null key throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testAttr_nullKey_throwsException() {
        TestNode node = new TestNode("http://example.com");
        node.attr(null);
    }

    // Tests abs: prefix shortcut in attr method
    @Test
    public void testAttr_absPrefix_returnsAbsoluteUrl() {
        TestNode node = new TestNode("http://example.com/path/");
        node.attr("href", "sub/page.html");

        assertEquals("http://example.com/path/sub/page.html", node.attr("abs:href"));
        assertEquals("", node.attr("abs:nonexistent"));
    }

    // Tests absUrl with relative URL
    @Test
    public void testAbsUrl_relativeUrl_returnsAbsoluteUrl() {
        TestNode node = new TestNode("http://example.com/dir/");
        node.attr("src", "../img.png");

        assertEquals("http://example.com/img.png", node.absUrl("src"));
    }

    // Tests absUrl when attribute is already absolute
    @Test
    public void testAbsUrl_alreadyAbsoluteUrl_returnsSameUrl() {
        TestNode node = new TestNode("http://example.com");
        node.attr("href", "http://other.com/index.html");

        assertEquals("http://other.com/index.html", node.absUrl("href"));
    }

    // Tests absUrl with invalid base URI
    @Test
    public void testAbsUrl_invalidBaseUri_returnsAbsoluteOrEmpty() {
        TestNode node = new TestNode("invalid-uri");
        node.attr("href", "http://valid.com/page");
        node.attr("rel", "subpage");

        assertEquals("http://valid.com/page", node.absUrl("href"));
        assertEquals("", node.absUrl("rel"));
    }

    // Tests absUrl with empty attribute key throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testAbsUrl_emptyKey_throwsException() {
        TestNode node = new TestNode("http://example.com");
        node.absUrl("");
    }

    // Tests base URI getter and setter
    @Test
    public void testSetBaseUri_validUri_updatesBaseUri() {
        TestNode node = new TestNode("http://example.com");
        assertEquals("http://example.com", node.baseUri());

        node.setBaseUri("http://example.org");
        assertEquals("http://example.org", node.baseUri());
    }

    // Tests parent and child relationship management
    @Test
    public void testAddChildren_andChildNodes_maintainsHierarchy() {
        TestNode parent = new TestNode("http://example.com");
        TestNode child1 = new TestNode("http://example.com");
        TestNode child2 = new TestNode("http://example.com");

        parent.addChildren(child1, child2);

        assertEquals(2, parent.childNodes().size());
        assertSame(child1, parent.childNode(0));
        assertSame(child2, parent.childNode(1));
        assertSame(parent, child1.parent());
        assertSame(parent, child2.parent());
        assertEquals(0, (int) child1.siblingIndex());
        assertEquals(1, (int) child2.siblingIndex());
    }

    // Tests child removal and reindexing
    @Test
    public void testRemove_middleChild_updatesSiblingsAndIndices() {
        TestNode parent = new TestNode("http://example.com");
        TestNode child1 = new TestNode("http://example.com");
        TestNode child2 = new TestNode("http://example.com");
        TestNode child3 = new TestNode("http://example.com");

        parent.addChildren(child1, child2, child3);
        child2.remove();

        assertEquals(2, parent.childNodes().size());
        assertNull(child2.parent());
        assertSame(child3, child1.nextSibling());
        assertSame(child1, child3.previousSibling());
        assertEquals(0, (int) child1.siblingIndex());
        assertEquals(1, (int) child3.siblingIndex());
    }

    // Tests replaceWith functionality
    @Test
    public void testReplaceWith_validReplacement_replacesCorrectly() {
        TestNode parent = new TestNode("http://example.com");
        TestNode child1 = new TestNode("http://example.com");
        TestNode replacement = new TestNode("http://example.com");

        parent.addChildren(child1);
        child1.replaceWith(replacement);

        assertEquals(1, parent.childNodes().size());
        assertSame(replacement, parent.childNode(0));
        assertNull(child1.parent());
        assertSame(parent, replacement.parent());
        assertEquals(0, (int) replacement.siblingIndex());
    }

    // Tests sibling navigation boundary conditions
    @Test
    public void testSiblingNavigation_boundaries_returnsExpectedNodes() {
        TestNode parent = new TestNode("http://example.com");
        TestNode child1 = new TestNode("http://example.com");
        TestNode child2 = new TestNode("http://example.com");

        parent.addChildren(child1, child2);

        assertNull(child1.previousSibling());
        assertSame(child2, child1.nextSibling());
        assertSame(child1, child2.previousSibling());
        assertNull(child2.nextSibling());

        List<Node> siblings = child1.siblingNodes();
        assertEquals(2, siblings.size());
    }

    // Tests orphan sibling navigation returns null
    @Test
    public void testNextSibling_orphanNode_returnsNull() {
        TestNode orphan = new TestNode("http://example.com");
        assertNull(orphan.nextSibling());
    }

    // Tests ownerDocument retrieval across hierarchy
    @Test
    public void testOwnerDocument_nodeInTree_returnsDocument() {
        Document doc = new Document("http://example.com");
        TestNode child = new TestNode("http://example.com");
        TestNode grandChild = new TestNode("http://example.com");

        doc.addChildren(child);
        child.addChildren(grandChild);

        assertSame(doc, doc.ownerDocument());
        assertSame(doc, child.ownerDocument());
        assertSame(doc, grandChild.ownerDocument());
    }

    // Tests ownerDocument on orphan node returns null
    @Test
    public void testOwnerDocument_orphanNode_returnsNull() {
        TestNode orphan = new TestNode("http://example.com");
        assertNull(orphan.ownerDocument());
    }

    // Tests outerHtml for node without owner document (Defects4J bug 8b check)
    @Test
    public void testOuterHtml_orphanNode_generatesHtml() {
        TestNode orphan = new TestNode("http://example.com");
        assertEquals("<test></test>", orphan.outerHtml());
    }

    // Tests outerHtml for node with owner document
    @Test
    public void testOuterHtml_nodeInDocument_generatesHtml() {
        Document doc = new Document("http://example.com");
        TestNode child = new TestNode("http://example.com");
        doc.addChildren(child);

        assertEquals("<test></test>", child.outerHtml());
    }

    // Tests equals and hashCode behavior
    @Test
    public void testEqualsAndHashCode_sameAndDifferentInstances() {
        TestNode node1 = new TestNode("http://example.com");
        TestNode node2 = new TestNode("http://example.com");

        assertTrue(node1.equals(node1));
        assertFalse(node1.equals(node2));
        assertFalse(node1.equals(null));
        assertFalse(node1.equals("someString"));

        assertEquals(node1.hashCode(), node1.hashCode());
    }
}