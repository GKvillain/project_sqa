package org.jsoup.nodes;

import org.jsoup.Jsoup;
import org.jsoup.parser.Tag;
import org.jsoup.select.NodeVisitor;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class NodeTest {

    // Tests attribute operations including abs: prefix
    @Test
    public void testAttr_variousKeys_returnsExpectedValues() {
        Document doc = Jsoup.parse("<a href=\"http://example.com/foo\" title=\"link\">Text</a>", "http://example.com/");
        Element link = doc.select("a").first();

        assertEquals("http://example.com/foo", link.attr("href"));
        assertEquals("link", link.attr("title"));
        assertEquals("", link.attr("nonexistent"));
        assertEquals("http://example.com/foo", link.attr("abs:href"));
    }

    // Tests hasAttr and removeAttr with standard and abs: attributes
    @Test
    public void testHasAttrAndRemoveAttr_existingAndMissing_returnsExpectedBooleans() {
        Document doc = Jsoup.parse("<a href=\"/relative\" id=\"my-link\">Link</a>", "http://example.com/");
        Element link = doc.select("a").first();

        assertTrue(link.hasAttr("href"));
        assertTrue(link.hasAttr("id"));
        assertTrue(link.hasAttr("abs:href"));
        assertFalse(link.hasAttr("class"));
        assertFalse(link.hasAttr("abs:class"));

        link.removeAttr("id");
        assertFalse(link.hasAttr("id"));
        assertEquals("", link.attr("id"));
    }

    // Tests absUrl resolution with relative and absolute base URIs
    @Test
    public void testAbsUrl_relativeAndAbsolute_resolvesCorrectly() {
        Document doc = Jsoup.parse("<a href=\"sub/page.html\">Link</a>", "http://example.com/dir/");
        Element link = doc.select("a").first();

        assertEquals("http://example.com/dir/sub/page.html", link.absUrl("href"));
        assertEquals("", link.absUrl("nonexistent"));
    }

    // Tests baseUri updating across descendants
    @Test
    public void testSetBaseUri_descendantsUpdated_allDescendantsHaveNewBaseUri() {
        Document doc = Jsoup.parse("<div><p><span>Hello</span></p></div>", "http://example.com/");
        doc.setBaseUri("http://newhost.com/path/");

        assertEquals("http://newhost.com/path/", doc.baseUri());
        assertEquals("http://newhost.com/path/", doc.select("div").first().baseUri());
        assertEquals("http://newhost.com/path/", doc.select("span").first().baseUri());
    }

    // Tests child node retrieval, indexing and copying
    @Test
    public void testChildNodes_traversalAndCopy_returnsCorrectChildren() {
        Document doc = Jsoup.parse("<div><p>1</p><p>2</p><p>3</p></div>");
        Element div = doc.select("div").first();

        assertEquals(3, div.childNodeSize());
        assertEquals("p", div.childNode(0).nodeName());
        assertEquals("p", div.childNode(1).nodeName());
        assertEquals(3, div.childNodes().size());

        List<Node> copy = div.childNodesCopy();
        assertEquals(3, copy.size());
        assertNotSame(div.childNode(0), copy.get(0));
    }

    // Tests inserting nodes at specific indices (including defect jsoup-49 scenario)
    @Test
    public void testAddChildrenAtIndex_insertInOrder_correctSequence() {
        Document doc = Jsoup.parse("<div><p id=\"1\">One</p><p id=\"2\">Two</p><p id=\"3\">Three</p></div>");
        Element div = doc.select("div").first();

        Element p4 = new Element(Tag.valueOf("p"), "").attr("id", "4");
        Element p5 = new Element(Tag.valueOf("p"), "").attr("id", "5");

        div.addChildren(1, p4, p5);

        assertEquals(5, div.childNodeSize());
        assertEquals("1", div.childNode(0).attr("id"));
        assertEquals("4", div.childNode(1).attr("id"));
        assertEquals("5", div.childNode(2).attr("id"));
        assertEquals("2", div.childNode(3).attr("id"));
        assertEquals("3", div.childNode(4).attr("id"));

        for (int i = 0; i < div.childNodeSize(); i++) {
            assertEquals(i, div.childNode(i).siblingIndex());
        }
    }

    // Tests moving children within the same parent via addChildren with index
    @Test
    public void testAddChildren_movingExistingChildren_preservesIndexIntegrity() {
        Document doc = Jsoup.parse("<div><p id=\"1\">1</p><p id=\"2\">2</p><p id=\"3\">3</p></div>");
        Element div = doc.select("div").first();
        Node p3 = div.childNode(2);

        div.addChildren(0, p3);

        assertEquals(3, div.childNodeSize());
        assertEquals("3", div.childNode(0).attr("id"));
        assertEquals("1", div.childNode(1).attr("id"));
        assertEquals("2", div.childNode(2).attr("id"));
    }

    // Tests before and after HTML insertion
    @Test
    public void testBeforeAndAfter_htmlStrings_insertedInCorrectPositions() {
        Document doc = Jsoup.parse("<div><span id=\"target\">Target</span></div>");
        Element span = doc.select("#target").first();

        span.before("<i>Before</i>");
        span.after("<b>After</b>");

        Element div = doc.select("div").first();
        assertEquals(3, div.children().size());
        assertEquals("i", div.childNode(0).nodeName());
        assertEquals("span", div.childNode(1).nodeName());
        assertEquals("b", div.childNode(2).nodeName());
    }

    // Tests before and after Node insertion
    @Test
    public void testBeforeAndAfter_nodeObjects_insertedInCorrectPositions() {
        Document doc = Jsoup.parse("<div><span id=\"target\">Target</span></div>");
        Element span = doc.select("#target").first();

        Element beforeNode = new Element(Tag.valueOf("em"), "");
        Element afterNode = new Element(Tag.valueOf("strong"), "");

        span.before(beforeNode);
        span.after(afterNode);

        Element div = doc.select("div").first();
        assertEquals(beforeNode, span.previousSibling());
        assertEquals(afterNode, span.nextSibling());
    }

    // Tests sibling retrieval and sibling index properties
    @Test
    public void testSiblingNavigation_firstMiddleLast_returnsSiblingsCorrectly() {
        Document doc = Jsoup.parse("<div><p id=\"1\">1</p><p id=\"2\">2</p><p id=\"3\">3</p></div>");
        Element div = doc.select("div").first();
        Node p1 = div.childNode(0);
        Node p2 = div.childNode(1);
        Node p3 = div.childNode(2);

        assertNull(p1.previousSibling());
        assertEquals(p2, p1.nextSibling());
        assertEquals(p1, p2.previousSibling());
        assertEquals(p3, p2.nextSibling());
        assertEquals(p2, p3.previousSibling());
        assertNull(p3.nextSibling());

        List<Node> p2Siblings = p2.siblingNodes();
        assertEquals(2, p2Siblings.size());
        assertTrue(p2Siblings.contains(p1));
        assertTrue(p2Siblings.contains(p3));
        assertFalse(p2Siblings.contains(p2));
    }

    // Tests wrap and unwrap methods
    @Test
    public void testWrapAndUnwrap_elementWrapping_structureUpdatedProperly() {
        Document doc = Jsoup.parse("<div><span id=\"target\">Inner</span></div>");
        Element span = doc.select("#target").first();

        span.wrap("<div class=\"wrapper\"><p></p></div>");
        assertEquals("div", span.parent().parent().nodeName());
        assertEquals("wrapper", span.parent().parent().attr("class"));

        Element p = span.parent();
        Node firstChild = p.unwrap();

        assertEquals(span, firstChild);
        assertEquals("wrapper", span.parent().attr("class"));
    }

    // Tests replaceWith and remove methods
    @Test
    public void testReplaceWithAndRemove_modifiesParentChildren() {
        Document doc = Jsoup.parse("<div><span id=\"target\">Target</span></div>");
        Element span = doc.select("#target").first();
        Element replacement = new Element(Tag.valueOf("b"), "");

        span.replaceWith(replacement);
        assertNull(span.parent());
        assertEquals(replacement, doc.select("div").first().childNode(0));

        replacement.remove();
        assertNull(replacement.parent());
        assertEquals(0, doc.select("div").first().childNodeSize());
    }

    // Tests ownerDocument retrieval for attached and detached nodes
    @Test
    public void testOwnerDocument_attachedAndDetached_returnsExpectedDocument() {
        Document doc = Jsoup.parse("<div><span>Text</span></div>");
        Element span = doc.select("span").first();
        assertSame(doc, span.ownerDocument());

        Element orphan = new Element(Tag.valueOf("p"), "");
        assertNull(orphan.ownerDocument());
    }

    // Tests traverse depth-first visitor
    @Test
    public void testTraverse_nodeVisitor_visitsAllNodesInOrder() {
        Document doc = Jsoup.parse("<div><p>Text</p></div>");
        final StringBuilder visited = new StringBuilder();

        doc.body().traverse(new NodeVisitor() {
            public void head(Node node, int depth) {
                visited.append(node.nodeName()).append(":head ");
            }

            public void tail(Node node, int depth) {
                visited.append(node.nodeName()).append(":tail ");
            }
        });

        assertTrue(visited.toString().contains("body:head"));
        assertTrue(visited.toString().contains("div:head"));
        assertTrue(visited.toString().contains("p:head"));
        assertTrue(visited.toString().contains("p:tail"));
    }

    // Tests clone creates independent deep copy
    @Test
    public void testClone_deepCopy_isIndependentOfOriginal() {
        Document doc = Jsoup.parse("<div id=\"main\"><p class=\"text\">Hello</p></div>");
        Element div = doc.select("#main").first();

        Node clone = div.clone();
        assertNull(clone.parent());
        assertEquals(div.childNodeSize(), clone.childNodeSize());
        assertEquals(div.outerHtml(), clone.outerHtml());

        // Modify clone and ensure original is unaffected
        clone.attr("id", "modified");
        assertEquals("main", div.attr("id"));
        assertEquals("modified", clone.attr("id"));
    }

    // Tests equals and hashCode contract
    @Test
    public void testEqualsAndHashCode_sameContentAndDifferent_obeysContract() {
        Element el1 = new Element(Tag.valueOf("p"), "").attr("class", "lead");
        Element el2 = new Element(Tag.valueOf("p"), "").attr("class", "lead");
        Element el3 = new Element(Tag.valueOf("p"), "").attr("class", "other");

        assertEquals(el1, el2);
        assertEquals(el1.hashCode(), el2.hashCode());
        assertFalse(el1.equals(el3));
        assertFalse(el1.equals(null));
    }

    // Tests exception on null attribute key
    @Test(expected = IllegalArgumentException.class)
    public void testAttr_nullAttributeKey_throwsException() {
        Element el = new Element(Tag.valueOf("p"), "");
        el.attr(null);
    }

    // Tests exception on removing node without parent
    @Test(expected = IllegalArgumentException.class)
    public void testRemove_orphanNode_throwsException() {
        Element orphan = new Element(Tag.valueOf("p"), "");
        orphan.remove();
    }
}