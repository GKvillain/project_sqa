package org.jsoup.nodes;

import org.jsoup.Jsoup;
import org.jsoup.parser.Tag;
import org.jsoup.select.NodeVisitor;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class NodeTest {

    // Tests siblingNodes does not include the node itself (Bug 22b)
    @Test
    public void testSiblingNodes_hasSiblings_returnsOtherSiblingsExcludingSelf() {
        Document doc = Jsoup.parse("<div><p>One</p><p>Two</p><p>Three</p></div>");
        Element div = doc.select("div").first();
        Node p1 = div.childNode(0);
        Node p2 = div.childNode(1);
        Node p3 = div.childNode(2);

        List<Node> siblings = p2.siblingNodes();
        assertEquals(2, siblings.size());
        assertTrue(siblings.contains(p1));
        assertTrue(siblings.contains(p3));
        assertFalse(siblings.contains(p2));
    }

    // Tests siblingNodes when node has no parent
    @Test
    public void testSiblingNodes_orphanNode_returnsEmptyList() {
        Element el = new Element(Tag.valueOf("p"), "");
        List<Node> siblings = el.siblingNodes();
        assertNotNull(siblings);
        assertEquals(0, siblings.size());
    }

    // Tests nextSibling when next sibling exists
    @Test
    public void testNextSibling_hasFollowingSibling_returnsNextNode() {
        Document doc = Jsoup.parse("<p>One</p><p>Two</p>");
        Node p1 = doc.body().childNode(0);
        Node p2 = doc.body().childNode(1);

        assertEquals(p2, p1.nextSibling());
    }

    // Tests nextSibling when node is last child
    @Test
    public void testNextSibling_lastChild_returnsNull() {
        Document doc = Jsoup.parse("<p>One</p><p>Two</p>");
        Node p2 = doc.body().childNode(1);

        assertNull(p2.nextSibling());
    }

    // Tests nextSibling when node has no parent
    @Test
    public void testNextSibling_noParent_returnsNull() {
        Element el = new Element(Tag.valueOf("p"), "");
        assertNull(el.nextSibling());
    }

    // Tests previousSibling when previous sibling exists
    @Test
    public void testPreviousSibling_hasPrecedingSibling_returnsPreviousNode() {
        Document doc = Jsoup.parse("<p>One</p><p>Two</p>");
        Node p1 = doc.body().childNode(0);
        Node p2 = doc.body().childNode(1);

        assertEquals(p1, p2.previousSibling());
    }

    // Tests previousSibling when node is first child
    @Test
    public void testPreviousSibling_firstChild_returnsNull() {
        Document doc = Jsoup.parse("<p>One</p><p>Two</p>");
        Node p1 = doc.body().childNode(0);

        assertNull(p1.previousSibling());
    }

    // Tests previousSibling when node has no parent
    @Test
    public void testPreviousSibling_noParent_returnsNull() {
        Element el = new Element(Tag.valueOf("p"), "");
        assertNull(el.previousSibling());
    }

    // Tests attr with standard key and abs: prefix
    @Test
    public void testAttr_standardAndAbsolute_returnsValues() {
        Document doc = Jsoup.parse("<a href=\"/path/page.html\" title=\"test\">Link</a>", "http://example.com/dir/");
        Node link = doc.select("a").first();

        assertEquals("test", link.attr("title"));
        assertEquals("/path/page.html", link.attr("href"));
        assertEquals("http://example.com/path/page.html", link.attr("abs:href"));
        assertEquals("", link.attr("abs:nonexistent"));
        assertEquals("", link.attr("nonexistent"));
    }

    // Tests hasAttr with standard key and abs: prefix
    @Test
    public void testHasAttr_standardAndAbsolute_returnsExpected() {
        Document doc = Jsoup.parse("<a href=\"/path/page.html\">Link</a>", "http://example.com/");
        Node link = doc.select("a").first();

        assertTrue(link.hasAttr("href"));
        assertTrue(link.hasAttr("abs:href"));
        assertFalse(link.hasAttr("title"));
        assertFalse(link.hasAttr("abs:title"));
    }

    // Tests absUrl with invalid base URL fallback
    @Test
    public void testAbsUrl_invalidBaseUrl_returnsDirectAbsUrl() {
        Element el = new Element(Tag.valueOf("a"), "invalid_url");
        el.attr("href", "http://example.com/test");

        assertEquals("http://example.com/test", el.absUrl("href"));
    }

    // Tests absUrl with query relative path
    @Test
    public void testAbsUrl_queryRelative_resolvesCorrectly() {
        Document doc = Jsoup.parse("<a href=\"?query=1\">Link</a>", "http://example.com/path/index.html");
        Node link = doc.select("a").first();

        assertEquals("http://example.com/path/index.html?query=1", link.absUrl("href"));
    }

    // Tests absUrl on missing attribute
    @Test
    public void testAbsUrl_missingAttr_returnsEmptyString() {
        Document doc = Jsoup.parse("<a>Link</a>", "http://example.com/");
        Node link = doc.select("a").first();

        assertEquals("", link.absUrl("href"));
    }

    // Tests removeAttr removes the attribute
    @Test
    public void testRemoveAttr_existingAttr_removesAttribute() {
        Element el = new Element(Tag.valueOf("a"), "");
        el.attr("key", "value");
        assertTrue(el.hasAttr("key"));

        el.removeAttr("key");
        assertFalse(el.hasAttr("key"));
    }

    // Tests remove node from DOM tree
    @Test
    public void testRemove_childNode_removesFromParent() {
        Document doc = Jsoup.parse("<div><p>1</p><p>2</p></div>");
        Element div = doc.select("div").first();
        Node p1 = div.childNode(0);

        p1.remove();
        assertEquals(1, div.childNodes().size());
        assertEquals("2", ((Element) div.childNode(0)).text());
        assertNull(p1.parent());
    }

    // Tests replaceWith replacing a node
    @Test
    public void testReplaceWith_validReplacement_replacesInParent() {
        Document doc = Jsoup.parse("<div><p>1</p></div>");
        Element div = doc.select("div").first();
        Node p = div.childNode(0);
        Element span = new Element(Tag.valueOf("span"), "");

        p.replaceWith(span);
        assertEquals(1, div.childNodes().size());
        assertEquals(span, div.childNode(0));
        assertNull(p.parent());
    }

    // Tests before and after with HTML
    @Test
    public void testBeforeAndAfter_stringHtml_insertsSiblings() {
        Document doc = Jsoup.parse("<div><p id=\"mid\">Middle</p></div>");
        Element mid = doc.select("#mid").first();

        mid.before("<b>Before</b>");
        mid.after("<i>After</i>");

        assertEquals("<div><b>Before</b><p id=\"mid\">Middle</p><i>After</i></div>", doc.body().html());
    }

    // Tests before and after with Node
    @Test
    public void testBeforeAndAfter_nodeInstance_insertsSiblings() {
        Document doc = Jsoup.parse("<div><p id=\"mid\">Middle</p></div>");
        Element mid = doc.select("#mid").first();

        Element beforeNode = new Element(Tag.valueOf("b"), "");
        Element afterNode = new Element(Tag.valueOf("i"), "");

        mid.before(beforeNode);
        mid.after(afterNode);

        assertEquals(3, doc.select("div").first().childNodes().size());
        assertEquals(beforeNode, mid.previousSibling());
        assertEquals(afterNode, mid.nextSibling());
    }

    // Tests wrap method wrapping an element
    @Test
    public void testWrap_validHtml_wrapsNode() {
        Document doc = Jsoup.parse("<div><p>Text</p></div>");
        Element p = doc.select("p").first();

        p.wrap("<div class=\"wrapper\"><span class=\"inner\"></span></div>");
        assertEquals("<div><div class=\"wrapper\"><span class=\"inner\"><p>Text</p></span></div></div>", doc.body().html());
    }

    // Tests unwrap method removing node but keeping children
    @Test
    public void testUnwrap_nodeWithChildren_movesChildrenToParent() {
        Document doc = Jsoup.parse("<div><span>Two <b>Three</b></span></div>");
        Element span = doc.select("span").first();

        Node firstChild = span.unwrap();
        assertNotNull(firstChild);
        assertEquals("<div>Two <b>Three</b></div>", doc.body().html());
    }

    // Tests setBaseUri updating descendants
    @Test
    public void testSetBaseUri_descendantNodes_updatesAllBaseUris() {
        Document doc = Jsoup.parse("<div><p><a href=\"test\">Link</a></p></div>", "http://example.com/");
        doc.setBaseUri("http://example.org/new/");

        assertEquals("http://example.org/new/", doc.baseUri());
        assertEquals("http://example.org/new/", doc.select("a").first().baseUri());
    }

    // Tests traverse depth-first traversal
    @Test
    public void testTraverse_nodeVisitor_visitsNodes() {
        Document doc = Jsoup.parse("<div><p>Text</p></div>");
        final StringBuilder visited = new StringBuilder();

        doc.body().traverse(new NodeVisitor() {
            public void head(Node node, int depth) {
                visited.append("<").append(node.nodeName()).append(">");
            }

            public void tail(Node node, int depth) {
                visited.append("</").append(node.nodeName()).append(">");
            }
        });

        assertEquals("<body><div><p><#text></#text></p></div></body>", visited.toString());
    }

    // Tests clone creating deep copy and orphan node
    @Test
    public void testClone_standaloneNode_createsDeepCopy() {
        Document doc = Jsoup.parse("<div><p id=\"p1\">Text</p></div>");
        Element div = doc.select("div").first();
        Node clone = div.clone();

        assertNull(clone.parent());
        assertEquals(0, clone.siblingIndex());
        assertEquals(1, clone.childNodes().size());
        assertEquals(clone, clone.childNode(0).parent());
        assertNotSame(div, clone);
        assertNotSame(div.childNode(0), clone.childNode(0));
    }

    // Tests ownerDocument on root document and child nodes
    @Test
    public void testOwnerDocument_documentAndChild_returnsDocumentOrNull() {
        Document doc = Jsoup.parse("<div><p>Text</p></div>");
        Element div = doc.select("div").first();

        assertEquals(doc, doc.ownerDocument());
        assertEquals(doc, div.ownerDocument());

        Element orphan = new Element(Tag.valueOf("p"), "");
        assertNull(orphan.ownerDocument());
    }
}