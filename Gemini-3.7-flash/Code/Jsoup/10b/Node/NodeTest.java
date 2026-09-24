package org.jsoup.nodes;

import org.junit.Test;
import org.junit.Before;

import java.util.List;

import static org.junit.Assert.*;

public class NodeTest {

    private static class TestNode extends Node {
        TestNode(String baseUri, Attributes attributes) {
            super(baseUri, attributes);
        }

        TestNode(String baseUri) {
            super(baseUri);
        }

        TestNode() {
            super();
        }

        @Override
        public String nodeName() {
            return "testNode";
        }

        @Override
        void outerHtmlHead(StringBuilder accum, int depth, Document.OutputSettings out) {
            accum.append("<testNode>");
        }

        @Override
        void outerHtmlTail(StringBuilder accum, int depth, Document.OutputSettings out) {
            accum.append("</testNode>");
        }
    }

    private TestNode node;

    @Before
    public void setUp() {
        node = new TestNode("http://example.com/dir/page.html");
    }

    // Tests absUrl with query relative URL (Defects4J bug 10)
    @Test
    public void testAbsUrl_queryRelativeUrl_returnsCorrectAbsoluteUrl() {
        node.attr("href", "?query=val");
        assertEquals("http://example.com/dir/page.html?query=val", node.absUrl("href"));
    }

    // Tests absUrl with normal relative path
    @Test
    public void testAbsUrl_relativeUrl_returnsAbsoluteUrl() {
        node.attr("href", "sub/target.html");
        assertEquals("http://example.com/dir/sub/target.html", node.absUrl("href"));
    }

    // Tests absUrl with already absolute URL
    @Test
    public void testAbsUrl_absoluteUrl_returnsSameUrl() {
        node.attr("href", "http://other.com/path");
        assertEquals("http://other.com/path", node.absUrl("href"));
    }

    // Tests absUrl when attribute is not present
    @Test
    public void testAbsUrl_attributeNotPresent_returnsEmptyString() {
        assertEquals("", node.absUrl("nonexistent"));
    }

    // Tests absUrl with invalid base URL but valid absolute attribute URL
    @Test
    public void testAbsUrl_invalidBaseUriWithAbsoluteAttribute_returnsAttributeUrl() {
        TestNode invalidBaseNode = new TestNode("not_a_valid_url");
        invalidBaseNode.attr("href", "http://example.com/index.html");
        assertEquals("http://example.com/index.html", invalidBaseNode.absUrl("href"));
    }

    // Tests absUrl with invalid base URL and relative attribute URL
    @Test
    public void testAbsUrl_invalidBaseUriWithRelativeAttribute_returnsEmptyString() {
        TestNode invalidBaseNode = new TestNode("not_a_valid_url");
        invalidBaseNode.attr("href", "relative.html");
        assertEquals("", invalidBaseNode.absUrl("href"));
    }

    // Tests abs: attribute prefix shortcut
    @Test
    public void testAttr_absPrefix_returnsAbsoluteUrl() {
        node.attr("href", "sub.html");
        assertEquals("http://example.com/dir/sub.html", node.attr("abs:href"));
    }

    // Tests attr operations: get, set, has, remove
    @Test
    public void testAttr_getSetHasRemove_behavesCorrectly() {
        node.attr("key", "value");
        assertTrue(node.hasAttr("key"));
        assertEquals("value", node.attr("key"));

        node.removeAttr("key");
        assertFalse(node.hasAttr("key"));
        assertEquals("", node.attr("key"));
    }

    // Tests baseUri getter and setter
    @Test
    public void testSetBaseUri_validUri_updatesBaseUri() {
        node.setBaseUri("http://example.org/");
        assertEquals("http://example.org/", node.baseUri());
    }

    // Tests child addition, retrieval and siblings navigation
    @Test
    public void testChildNodes_addAndNavigate_returnsExpectedSiblings() {
        TestNode child1 = new TestNode("http://example.com");
        TestNode child2 = new TestNode("http://example.com");
        TestNode child3 = new TestNode("http://example.com");

        node.addChildren(child1, child2);
        node.addChildren(1, child3); // order: child1, child3, child2

        assertEquals(3, node.childNodes().size());
        assertEquals(child1, node.childNode(0));
        assertEquals(child3, node.childNode(1));
        assertEquals(child2, node.childNode(2));

        assertEquals(child3, child1.nextSibling());
        assertNull(child1.previousSibling());
        assertEquals(child2, child3.nextSibling());
        assertEquals(child1, child3.previousSibling());
        assertNull(child2.nextSibling());

        assertEquals(Integer.valueOf(1), child3.siblingIndex());
        assertEquals(3, child1.siblingNodes().size());
    }

    // Tests removing a node from its parent
    @Test
    public void testRemove_childNode_removesFromParent() {
        TestNode child1 = new TestNode("http://example.com");
        TestNode child2 = new TestNode("http://example.com");
        node.addChildren(child1, child2);

        child1.remove();

        assertEquals(1, node.childNodes().size());
        assertEquals(child2, node.childNode(0));
        assertEquals(Integer.valueOf(0), child2.siblingIndex());
        assertNull(child1.parent());
    }

    // Tests replacing a child node
    @Test
    public void testReplaceWith_existingChild_replacesCorrectly() {
        TestNode child1 = new TestNode("http://example.com");
        TestNode child2 = new TestNode("http://example.com");
        TestNode replacement = new TestNode("http://example.com");
        node.addChildren(child1, child2);

        child1.replaceWith(replacement);

        assertEquals(2, node.childNodes().size());
        assertEquals(replacement, node.childNode(0));
        assertNull(child1.parent());
        assertEquals(node, replacement.parent());
    }

    // Tests ownerDocument traversal
    @Test
    public void testOwnerDocument_nodeInDocument_returnsDocument() {
        Document doc = new Document("http://example.com");
        TestNode child = new TestNode("http://example.com");
        doc.addChildren(child);

        assertEquals(doc, child.ownerDocument());
        assertNull(node.ownerDocument());
    }

    // Tests outerHtml rendering
    @Test
    public void testOuterHtml_testNode_rendersTags() {
        TestNode child = new TestNode("http://example.com");
        node.addChildren(child);

        assertEquals("<testNode><testNode></testNode></testNode>", node.outerHtml());
        assertEquals("<testNode><testNode></testNode></testNode>", node.toString());
    }

    // Tests clone creates independent deep copy
    @Test
    public void testClone_nodeWithChildren_createsDeepCopy() {
        node.attr("key", "value");
        TestNode child = new TestNode("http://example.com");
        child.attr("childKey", "childVal");
        node.addChildren(child);

        TestNode cloned = (TestNode) node.clone();

        assertNull(cloned.parent());
        assertEquals("value", cloned.attr("key"));
        assertEquals(1, cloned.childNodes().size());
        assertNotSame(node.childNode(0), cloned.childNode(0));
        assertEquals("childVal", cloned.childNode(0).attr("childKey"));

        cloned.attr("key", "newValue");
        assertEquals("value", node.attr("key"));
    }

    // Tests equals and hashCode
    @Test
    public void testEqualsAndHashCode_basicCheck() {
        assertTrue(node.equals(node));
        assertFalse(node.equals(new TestNode("http://example.com")));
        assertNotNull(node.hashCode());
    }

    // Tests null attribute key throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testAttr_nullKey_throwsException() {
        node.attr(null);
    }

    // Tests null base URI in constructor throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullBaseUri_throwsException() {
        new TestNode(null);
    }
}