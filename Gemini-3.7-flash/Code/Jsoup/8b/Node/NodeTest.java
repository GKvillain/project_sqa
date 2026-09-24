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
            accum.append("<").append(name).append(">");
        }

        @Override
        void outerHtmlTail(StringBuilder accum, int depth, Document.OutputSettings out) {
            accum.append("</").append(name).append(">");
        }
    }

    // Tests attribute get, set, hasAttr and removeAttr
    @Test
    public void testAttr_setGetAndRemove_returnsExpectedValues() {
        Node node = new TestNode("http://example.com/");
        node.attr("key1", "val1");

        assertTrue(node.hasAttr("key1"));
        assertEquals("val1", node.attr("key1"));
        assertNotNull(node.attributes());

        node.removeAttr("key1");
        assertFalse(node.hasAttr("key1"));
        assertEquals("", node.attr("key1"));
    }

    // Tests absUrl resolution with valid baseUri and relative path
    @Test
    public void testAbsUrl_relativeUrlWithValidBaseUri_returnsAbsoluteUrl() {
        Node node = new TestNode("http://example.com/path/index.html");
        node.attr("href", "sub/page.html");

        String absUrl = node.absUrl("href");
        assertEquals("http://example.com/path/sub/page.html", absUrl);
    }

    // Tests abs: attribute key prefix routing to absUrl
    @Test
    public void testAttr_absPrefixKey_resolvesAbsoluteUrl() {
        Node node = new TestNode("http://example.com/dir/");
        node.attr("href", "test.html");

        assertEquals("http://example.com/dir/test.html", node.attr("abs:href"));
        assertEquals("", node.attr("abs:nonexistent"));
    }

    // Tests absUrl with already absolute URL
    @Test
    public void testAbsUrl_alreadyAbsoluteUrl_returnsSameUrl() {
        Node node = new TestNode("http://example.com/");
        node.attr("href", "https://other.org/page");

        assertEquals("https://other.org/page", node.absUrl("href"));
    }

    // Tests absUrl with invalid base URI fallback
    @Test
    public void testAbsUrl_invalidBaseUriWithAbsoluteAttribute_returnsAbsoluteUrl() {
        Node node = new TestNode("invalid-uri");
        node.attr("href", "http://valid.org/test");

        assertEquals("http://valid.org/test", node.absUrl("href"));
    }

    // Tests absUrl with invalid base and invalid attribute
    @Test
    public void testAbsUrl_invalidBaseAndRelativeAttribute_returnsEmptyString() {
        Node node = new TestNode("invalid-uri");
        node.attr("href", "relative/path");

        assertEquals("", node.absUrl("href"));
    }

    // Tests absUrl on missing attribute
    @Test
    public void testAbsUrl_missingAttribute_returnsEmptyString() {
        Node node = new TestNode("http://example.com/");
        assertEquals("", node.absUrl("nonexistent"));
    }

    // Tests baseUri get and set
    @Test
    public void testBaseUri_getAndSet_updatesCorrectly() {
        Node node = new TestNode("  http://example.com/  ");
        assertEquals("http://example.com/", node.baseUri());

        node.setBaseUri("http://example.org/new");
        assertEquals("http://example.org/new", node.baseUri());
    }

    // Tests adding children and retrieving child nodes
    @Test
    public void testAddChildren_multipleChildren_correctHierarchyAndIndexes() {
        Node parent = new TestNode("http://example.com/");
        Node child1 = new TestNode("http://example.com/");
        Node child2 = new TestNode("http://example.com/");

        parent.addChildren(child1, child2);

        assertEquals(2, parent.childNodes().size());
        assertSame(child1, parent.childNode(0));
        assertSame(child2, parent.childNode(1));
        assertSame(parent, child1.parent());
        assertSame(parent, child2.parent());
        assertEquals(0, (int) child1.siblingIndex());
        assertEquals(1, (int) child2.siblingIndex());

        Node[] childArray = parent.childNodesAsArray();
        assertEquals(2, childArray.length);
        assertSame(child1, childArray[0]);
    }

    // Tests adding children at specific index
    @Test
    public void testAddChildren_withIndex_insertsCorrectly() {
        Node parent = new TestNode("http://example.com/");
        Node child1 = new TestNode("http://example.com/");
        Node child2 = new TestNode("http://example.com/");
        Node inserted = new TestNode("http://example.com/");

        parent.addChildren(child1, child2);
        parent.addChildren(1, inserted);

        assertEquals(3, parent.childNodes().size());
        assertSame(child1, parent.childNode(0));
        assertSame(inserted, parent.childNode(1));
        assertSame(child2, parent.childNode(2));
        assertEquals(1, (int) inserted.siblingIndex());
        assertEquals(2, (int) child2.siblingIndex());
    }

    // Tests previous and next sibling navigation
    @Test
    public void testSiblingNodes_navigation_returnsCorrectSiblings() {
        Node parent = new TestNode("http://example.com/");
        Node child1 = new TestNode("http://example.com/");
        Node child2 = new TestNode("http://example.com/");
        Node child3 = new TestNode("http://example.com/");

        parent.addChildren(child1, child2, child3);

        List<Node> siblings = child2.siblingNodes();
        assertEquals(3, siblings.size());

        assertNull(child1.previousSibling());
        assertSame(child2, child1.nextSibling());

        assertSame(child1, child2.previousSibling());
        assertSame(child3, child2.nextSibling());

        assertSame(child2, child3.previousSibling());
        assertNull(child3.nextSibling());
    }

    // Tests sibling navigation for orphan root node
    @Test
    public void testSiblingNodes_orphanNode_returnsNull() {
        Node orphan = new TestNode("http://example.com/");
        assertNull(orphan.nextSibling());
        assertNull(orphan.previousSibling());
        assertNull(orphan.parent());
    }

    // Tests removing a child node from its parent
    @Test
    public void testRemove_attachedChild_removesFromParentAndUpdatesIndexes() {
        Node parent = new TestNode("http://example.com/");
        Node child1 = new TestNode("http://example.com/");
        Node child2 = new TestNode("http://example.com/");

        parent.addChildren(child1, child2);
        child1.remove();

        assertEquals(1, parent.childNodes().size());
        assertSame(child2, parent.childNode(0));
        assertNull(child1.parent());
        assertEquals(0, (int) child2.siblingIndex());
    }

    // Tests replacing a child node with another
    @Test
    public void testReplaceWith_validReplacement_replacesInParent() {
        Node parent = new TestNode("http://example.com/");
        Node child1 = new TestNode("http://example.com/");
        Node child2 = new TestNode("http://example.com/");
        Node replacement = new TestNode("http://example.com/");

        parent.addChildren(child1, child2);
        child1.replaceWith(replacement);

        assertEquals(2, parent.childNodes().size());
        assertSame(replacement, parent.childNode(0));
        assertNull(child1.parent());
        assertSame(parent, replacement.parent());
        assertEquals(0, (int) replacement.siblingIndex());
    }

    // Tests ownerDocument on attached and orphan nodes
    @Test
    public void testOwnerDocument_attachedAndOrphan_returnsCorrectDocument() {
        Document doc = new Document("http://example.com/");
        Node child = new TestNode("http://example.com/");
        doc.addChildren(child);

        assertSame(doc, doc.ownerDocument());
        assertSame(doc, child.ownerDocument());

        Node orphan = new TestNode("http://example.com/");
        assertNull(orphan.ownerDocument());
    }

    // Tests outerHtml when node is attached to a Document
    @Test
    public void testOuterHtml_attachedToDocument_generatesHtml() {
        Document doc = new Document("http://example.com/");
        Node child = new TestNode("http://example.com/");
        doc.addChildren(child);

        String html = child.outerHtml();
        assertEquals("<test></test>", html);
        assertEquals("<test></test>", child.toString());
    }

    // Tests outerHtml / toString on an orphan node without an ownerDocument (Defects4J Bug 8b)
    @Test
    public void testOuterHtml_orphanNodeWithoutDocument_handlesOutputSettingsOrRendersHtml() {
        Node orphan = new TestNode("http://example.com/");
        String html = orphan.outerHtml();
        assertEquals("<test></test>", html);
    }

    // Tests equals and hashCode consistency
    @Test
    public void testEqualsAndHashCode_sameInstanceAndDifferentInstance() {
        Node node1 = new TestNode("http://example.com/");
        Node node2 = new TestNode("http://example.com/");

        assertTrue(node1.equals(node1));
        assertFalse(node1.equals(node2));
        assertFalse(node1.equals(null));
        assertFalse(node1.equals("string"));

        assertEquals(node1.hashCode(), node1.hashCode());
    }

    // Tests exception path for null attribute key in attr
    @Test(expected = IllegalArgumentException.class)
    public void testAttr_nullKey_throwsException() {
        Node node = new TestNode("http://example.com/");
        node.attr(null);
    }

    // Tests exception path for remove on orphan node
    @Test(expected = IllegalArgumentException.class)
    public void testRemove_orphanNode_throwsException() {
        Node node = new TestNode("http://example.com/");
        node.remove();
    }

    // Tests exception path for replaceWith on orphan node
    @Test(expected = IllegalArgumentException.class)
    public void testReplaceWith_orphanNode_throwsException() {
        Node node = new TestNode("http://example.com/");
        Node replacement = new TestNode("http://example.com/");
        node.replaceWith(replacement);
    }
}