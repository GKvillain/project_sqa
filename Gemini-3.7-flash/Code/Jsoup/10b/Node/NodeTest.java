package org.jsoup.nodes;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class NodeTest {

    private static class ConcreteNode extends Node {
        ConcreteNode(String baseUri, Attributes attributes) {
            super(baseUri, attributes);
        }

        ConcreteNode(String baseUri) {
            super(baseUri);
        }

        ConcreteNode() {
            super();
        }

        @Override
        public String nodeName() {
            return "concrete";
        }

        @Override
        void outerHtmlHead(StringBuilder accum, int depth, Document.OutputSettings out) {
            accum.append("<concrete>");
        }

        @Override
        void outerHtmlTail(StringBuilder accum, int depth, Document.OutputSettings out) {
            accum.append("</concrete>");
        }
    }

    // Tests resolving relative URL with query string relative to path
    @Test
    public void testAbsUrl_queryStringRelUrl_returnsCorrectAbsUrl() {
        ConcreteNode node = new ConcreteNode("http://example.com/path/file.html");
        node.attr("href", "?query=1");
        assertEquals("http://example.com/path/file.html?query=1", node.absUrl("href"));
    }

    // Tests resolving standard relative path URL
    @Test
    public void testAbsUrl_relativePath_returnsAbsoluteUrl() {
        ConcreteNode node = new ConcreteNode("http://example.com/path/file.html");
        node.attr("href", "sub/page.html");
        assertEquals("http://example.com/path/sub/page.html", node.absUrl("href"));
    }

    // Tests absUrl with already absolute URL
    @Test
    public void testAbsUrl_alreadyAbsoluteUrl_returnsSameUrl() {
        ConcreteNode node = new ConcreteNode("http://example.com/");
        node.attr("href", "https://other.com/index.html");
        assertEquals("https://other.com/index.html", node.absUrl("href"));
    }

    // Tests absUrl when attribute does not exist
    @Test
    public void testAbsUrl_missingAttribute_returnsEmptyString() {
        ConcreteNode node = new ConcreteNode("http://example.com/");
        assertEquals("", node.absUrl("nonexistent"));
    }

    // Tests absUrl with invalid base URL but valid absolute attribute URL
    @Test
    public void testAbsUrl_invalidBaseUrlWithAbsoluteAttr_returnsAbsoluteAttr() {
        ConcreteNode node = new ConcreteNode("not_a_valid_url");
        node.attr("href", "http://example.com/test");
        assertEquals("http://example.com/test", node.absUrl("href"));
    }

    // Tests absUrl with invalid base URL and relative attribute URL
    @Test
    public void testAbsUrl_invalidBaseUrlWithRelativeAttr_returnsEmptyString() {
        ConcreteNode node = new ConcreteNode("not_a_valid_url");
        node.attr("href", "/path/to/page");
        assertEquals("", node.absUrl("href"));
    }

    // Tests attr() using abs: prefix
    @Test
    public void testAttr_absPrefix_returnsAbsoluteUrl() {
        ConcreteNode node = new ConcreteNode("http://example.com/path/index.html");
        node.attr("href", "page.html");
        assertEquals("http://example.com/path/page.html", node.attr("abs:href"));
    }

    // Tests attribute get, set, has, and remove operations
    @Test
    public void testAttrAndRemoveAttr_validAttribute_managesAttributesCorrectly() {
        ConcreteNode node = new ConcreteNode("http://example.com/");
        assertFalse(node.hasAttr("key"));
        assertEquals("", node.attr("key"));

        node.attr("key", "value");
        assertTrue(node.hasAttr("key"));
        assertEquals("value", node.attr("key"));
        assertNotNull(node.attributes());

        node.removeAttr("key");
        assertFalse(node.hasAttr("key"));
    }

    // Tests setting and getting base URI
    @Test
    public void testBaseUri_updateBaseUri_returnsUpdatedBaseUri() {
        ConcreteNode node = new ConcreteNode("http://example.com/old");
        assertEquals("http://example.com/old", node.baseUri());

        node.setBaseUri("http://example.com/new");
        assertEquals("http://example.com/new", node.baseUri());
    }

    // Tests child and sibling navigation
    @Test
    public void testChildNodesAndSiblings_treeStructure_navigatesCorrectly() {
        ConcreteNode parent = new ConcreteNode("http://example.com/");
        ConcreteNode child1 = new ConcreteNode("http://example.com/");
        ConcreteNode child2 = new ConcreteNode("http://example.com/");
        ConcreteNode child3 = new ConcreteNode("http://example.com/");

        parent.addChildren(child1, child2);
        parent.addChildren(1, child3); // parent has: child1, child3, child2

        assertEquals(3, parent.childNodes().size());
        assertEquals(child1, parent.childNode(0));
        assertEquals(child3, parent.childNode(1));
        assertEquals(child2, parent.childNode(2));
        assertEquals(3, parent.childNodesAsArray().length);

        assertEquals(parent, child1.parent());
        assertEquals(0, child1.siblingIndex().intValue());
        assertEquals(1, child3.siblingIndex().intValue());
        assertEquals(2, child2.siblingIndex().intValue());

        assertEquals(child3, child1.nextSibling());
        assertNull(child1.previousSibling());
        assertEquals(child2, child3.nextSibling());
        assertEquals(child1, child3.previousSibling());
        assertNull(child2.nextSibling());

        List<Node> siblings = child1.siblingNodes();
        assertEquals(3, siblings.size());
    }

    // Tests removing a node from its parent
    @Test
    public void testRemove_attachedChild_removesFromParent() {
        ConcreteNode parent = new ConcreteNode("http://example.com/");
        ConcreteNode child1 = new ConcreteNode("http://example.com/");
        ConcreteNode child2 = new ConcreteNode("http://example.com/");
        parent.addChildren(child1, child2);

        child1.remove();
        assertEquals(1, parent.childNodes().size());
        assertEquals(child2, parent.childNode(0));
        assertNull(child1.parent());
        assertEquals(0, child2.siblingIndex().intValue());
    }

    // Tests replacing a child node
    @Test
    public void testReplaceWith_attachedChild_replacesSuccessfully() {
        ConcreteNode parent = new ConcreteNode("http://example.com/");
        ConcreteNode child1 = new ConcreteNode("http://example.com/");
        ConcreteNode child2 = new ConcreteNode("http://example.com/");
        ConcreteNode replacement = new ConcreteNode("http://example.com/");

        parent.addChildren(child1, child2);
        child1.replaceWith(replacement);

        assertEquals(2, parent.childNodes().size());
        assertEquals(replacement, parent.childNode(0));
        assertEquals(parent, replacement.parent());
        assertNull(child1.parent());
    }

    // Tests ownerDocument on tree with and without Document root
    @Test
    public void testOwnerDocument_standaloneAndAttached_returnsExpectedDocument() {
        ConcreteNode standalone = new ConcreteNode("http://example.com/");
        assertNull(standalone.ownerDocument());

        Document doc = new Document("http://example.com/");
        ConcreteNode child = new ConcreteNode("http://example.com/");
        doc.addChildren(child);

        assertEquals(doc, doc.ownerDocument());
        assertEquals(doc, child.ownerDocument());
    }

    // Tests cloning a node hierarchy
    @Test
    public void testClone_nodeWithChildren_createsDeepCopy() {
        ConcreteNode parent = new ConcreteNode("http://example.com/");
        parent.attr("key", "val");
        ConcreteNode child = new ConcreteNode("http://example.com/");
        parent.addChildren(child);

        Node clone = parent.clone();
        assertNotSame(parent, clone);
        assertNull(clone.parent());
        assertEquals("val", clone.attr("key"));
        assertEquals(1, clone.childNodes().size());
        assertNotSame(child, clone.childNode(0));
        assertEquals(clone, clone.childNode(0).parent());
    }

    // Tests outerHtml and toString generation
    @Test
    public void testOuterHtml_concreteNode_generatesHtml() {
        ConcreteNode node = new ConcreteNode("http://example.com/");
        assertEquals("<concrete></concrete>", node.outerHtml());
        assertEquals("<concrete></concrete>", node.toString());
    }

    // Tests equals and hashCode
    @Test
    public void testEqualsAndHashCode_sameAndDifferentInstances_expectedBehavior() {
        ConcreteNode node1 = new ConcreteNode("http://example.com/");
        ConcreteNode node2 = new ConcreteNode("http://example.com/");

        assertTrue(node1.equals(node1));
        assertFalse(node1.equals(node2));
        assertFalse(node1.equals(null));
        assertFalse(node1.equals("string"));
        assertTrue(node1.hashCode() != 0);
    }

    // Tests default constructor
    @Test
    public void testDefaultConstructor_instantiation_hasEmptyChildrenAndNullAttributes() {
        ConcreteNode node = new ConcreteNode();
        assertNull(node.baseUri());
        assertNull(node.attributes());
        assertEquals(0, node.childNodes().size());
        assertNull(node.parent());
        assertNull(node.nextSibling());
    }

    // Tests exception on null attribute key
    @Test(expected = IllegalArgumentException.class)
    public void testAttr_nullKey_throwsException() {
        ConcreteNode node = new ConcreteNode("http://example.com/");
        node.attr(null);
    }

    // Tests exception on empty attribute key for absUrl
    @Test(expected = IllegalArgumentException.class)
    public void testAbsUrl_emptyKey_throwsException() {
        ConcreteNode node = new ConcreteNode("http://example.com/");
        node.absUrl("");
    }

    // Tests exception when removing unparented node
    @Test(expected = IllegalArgumentException.class)
    public void testRemove_orphanNode_throwsException() {
        ConcreteNode node = new ConcreteNode("http://example.com/");
        node.remove();
    }
}