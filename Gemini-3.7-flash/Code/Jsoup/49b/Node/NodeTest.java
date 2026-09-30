package org.jsoup.nodes;

import org.jsoup.Jsoup;
import org.jsoup.parser.Tag;
import org.jsoup.select.NodeVisitor;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class NodeTest {

    // Tests inserting nodes at index and updating sibling index (Defects4J 49)
    @Test
    public void testAddChildren_insertMultipleAtStart_reindexesCorrectly() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Node c1 = new Element(Tag.valueOf("p"), "").text("1");
        Node c2 = new Element(Tag.valueOf("p"), "").text("2");
        Node c3 = new Element(Tag.valueOf("p"), "").text("3");
        parent.addChildren(c1, c2, c3);

        Node new1 = new Element(Tag.valueOf("span"), "").text("A");
        Node new2 = new Element(Tag.valueOf("span"), "").text("B");
        parent.addChildren(0, new1, new2);

        assertEquals(5, parent.childNodeSize());
        assertSame(new1, parent.childNode(0));
        assertSame(new2, parent.childNode(1));
        assertSame(c1, parent.childNode(2));
        assertSame(c2, parent.childNode(3));
        assertSame(c3, parent.childNode(4));

        assertEquals(0, new1.siblingIndex());
        assertEquals(1, new2.siblingIndex());
        assertEquals(2, c1.siblingIndex());
        assertEquals(3, c2.siblingIndex());
        assertEquals(4, c3.siblingIndex());
    }

    // Tests moving existing children within the same parent
    @Test
    public void testAddChildren_moveChildrenWithinParent_preservesOrder() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Node c1 = new Element(Tag.valueOf("p"), "").text("1");
        Node c2 = new Element(Tag.valueOf("p"), "").text("2");
        Node c3 = new Element(Tag.valueOf("p"), "").text("3");
        Node c4 = new Element(Tag.valueOf("p"), "").text("4");
        parent.addChildren(c1, c2, c3, c4);

        parent.addChildren(1, c4);

        assertEquals(4, parent.childNodeSize());
        assertSame(c1, parent.childNode(0));
        assertSame(c4, parent.childNode(1));
        assertSame(c2, parent.childNode(2));
        assertSame(c3, parent.childNode(3));

        for (int i = 0; i < parent.childNodeSize(); i++) {
            assertEquals(i, parent.childNode(i).siblingIndex());
        }
    }

    // Tests getting attribute with and without abs: prefix
    @Test
    public void testAttr_absPrefix_resolvesAbsoluteUrl() {
        Element link = new Element(Tag.valueOf("a"), "http://example.com/foo/");
        link.attr("href", "bar.html");
        link.attr("title", "Test Link");

        assertEquals("bar.html", link.attr("href"));
        assertEquals("http://example.com/foo/bar.html", link.attr("abs:href"));
        assertEquals("Test Link", link.attr("title"));
        assertEquals("", link.attr("nonexistent"));
        assertEquals("", link.attr("abs:nonexistent"));
    }

    // Tests hasAttr with regular attribute and abs: prefix
    @Test
    public void testHasAttr_absAndRegular_returnsExpected() {
        Element link = new Element(Tag.valueOf("a"), "http://example.com/");
        link.attr("href", "index.html");

        assertTrue(link.hasAttr("href"));
        assertTrue(link.hasAttr("abs:href"));
        assertFalse(link.hasAttr("rel"));
        assertFalse(link.hasAttr("abs:rel"));
    }

    // Tests removeAttr removes the specified attribute
    @Test
    public void testRemoveAttr_existingKey_removesAttribute() {
        Element element = new Element(Tag.valueOf("div"), "");
        element.attr("class", "container");
        assertTrue(element.hasAttr("class"));

        element.removeAttr("class");
        assertFalse(element.hasAttr("class"));
        assertEquals("", element.attr("class"));
    }

    // Tests updating baseUri across descendants
    @Test
    public void testSetBaseUri_descendantTree_updatesAllNodes() {
        Document doc = Jsoup.parse("<div><p><a href='/doc'>Link</a></p></div>", "http://example.com/");
        Element div = doc.select("div").first();
        Element link = doc.select("a").first();

        div.setBaseUri("http://example.org/");

        assertEquals("http://example.org/", div.baseUri());
        assertEquals("http://example.org/", link.baseUri());
        assertEquals("http://example.org/doc", link.absUrl("href"));
    }

    // Tests childNodesCopy returns a deep cloned copy of children
    @Test
    public void testChildNodesCopy_modifications_doNotAffectOriginal() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element child = new Element(Tag.valueOf("p"), "");
        child.attr("class", "original");
        parent.appendChild(child);

        List<Node> copy = parent.childNodesCopy();
        assertEquals(1, copy.size());

        copy.get(0).attr("class", "modified");
        assertEquals("original", child.attr("class"));
        assertEquals("modified", copy.get(0).attr("class"));
    }

    // Tests ownerDocument retrieval for attached and detached nodes
    @Test
    public void testOwnerDocument_attachedAndDetached_returnsCorrectDoc() {
        Document doc = Jsoup.parse("<div><p>Hello</p></div>");
        Element p = doc.select("p").first();
        assertSame(doc, p.ownerDocument());
        assertSame(doc, doc.ownerDocument());

        Element detached = new Element(Tag.valueOf("span"), "");
        assertNull(detached.ownerDocument());
    }

    // Tests before and after insertions using Node
    @Test
    public void testBeforeAndAfter_nodeSibling_insertsAtCorrectPositions() {
        Document doc = Jsoup.parse("<div><p id='mid'>Middle</p></div>");
        Element mid = doc.select("#mid").first();

        Element before = new Element(Tag.valueOf("p"), "").attr("id", "before");
        Element after = new Element(Tag.valueOf("p"), "").attr("id", "after");

        mid.before(before);
        mid.after(after);

        Element div = doc.select("div").first();
        assertEquals(3, div.childNodeSize());
        assertSame(before, div.childNode(0));
        assertSame(mid, div.childNode(1));
        assertSame(after, div.childNode(2));
    }

    // Tests before and after insertions using HTML string
    @Test
    public void testBeforeAndAfter_htmlString_insertsParsedNodes() {
        Document doc = Jsoup.parse("<div><p id='mid'>Middle</p></div>");
        Element mid = doc.select("#mid").first();

        mid.before("<span id='before'>Before</span>");
        mid.after("<span id='after'>After</span>");

        Element div = doc.select("div").first();
        assertEquals(3, div.children().size());
        assertEquals("before", div.child(0).id());
        assertEquals("mid", div.child(1).id());
        assertEquals("after", div.child(2).id());
    }

    // Tests wrap and unwrap methods
    @Test
    public void testWrapAndUnwrap_nestedStructure_wrapsAndUnwrapsCorrectly() {
        Document doc = Jsoup.parse("<div><p>Text</p></div>");
        Element p = doc.select("p").first();

        p.wrap("<section class='outer'><div class='inner'></div></section>");
        Element div = doc.select("div").first();
        assertEquals("section", div.childNode(0).nodeName());

        Element inner = doc.select(".inner").first();
        assertSame(p, inner.childNode(0));

        p.unwrap();
        assertEquals("Text", inner.text());
        assertFalse(inner.children().contains(p));
    }

    // Tests replaceWith replacing a node in parent
    @Test
    public void testReplaceWith_validNode_replacesCorrectly() {
        Document doc = Jsoup.parse("<div><p id='old'>Old</p></div>");
        Element oldP = doc.select("#old").first();
        Element newSpan = new Element(Tag.valueOf("span"), "").attr("id", "new");

        oldP.replaceWith(newSpan);

        Element div = doc.select("div").first();
        assertNull(oldP.parent());
        assertSame(newSpan, div.childNode(0));
        assertEquals(0, newSpan.siblingIndex());
    }

    // Tests sibling navigation methods
    @Test
    public void testSiblings_navigation_returnsExpectedSiblings() {
        Document doc = Jsoup.parse("<div><p id='1'></p><p id='2'></p><p id='3'></p></div>");
        Element p1 = doc.select("#1").first();
        Element p2 = doc.select("#2").first();
        Element p3 = doc.select("#3").first();

        assertNull(p1.previousSibling());
        assertSame(p2, p1.nextSibling());
        assertSame(p1, p2.previousSibling());
        assertSame(p3, p2.nextSibling());
        assertNull(p3.nextSibling());

        List<Node> siblings = p2.siblingNodes();
        assertEquals(2, siblings.size());
        assertTrue(siblings.contains(p1));
        assertTrue(siblings.contains(p3));
        assertFalse(siblings.contains(p2));
    }

    // Tests sibling navigation when node has no parent
    @Test
    public void testSiblings_orphanNode_returnsNullAndEmpty() {
        Element orphan = new Element(Tag.valueOf("p"), "");
        assertNull(orphan.parent());
        assertNull(orphan.previousSibling());
        assertNull(orphan.nextSibling());
        assertTrue(orphan.siblingNodes().isEmpty());
    }

    // Tests traverse depth-first visitor callback
    @Test
    public void testTraverse_visitor_visitsHeadAndTail() {
        Document doc = Jsoup.parse("<div><p>Text</p></div>");
        final StringBuilder visited = new StringBuilder();

        doc.body().traverse(new NodeVisitor() {
            public void head(Node node, int depth) {
                visited.append("H:").append(node.nodeName()).append(";");
            }

            public void tail(Node node, int depth) {
                visited.append("T:").append(node.nodeName()).append(";");
            }
        });

        assertTrue(visited.toString().startsWith("H:body;H:div;H:p;H:#text;"));
        assertTrue(visited.toString().contains("T:p;T:div;T:body;"));
    }

    // Tests clone creates an independent deep copy
    @Test
    public void testClone_deepCopy_isIndependent() {
        Document doc = Jsoup.parse("<div id='main'><p class='one'>Hello</p></div>");
        Element div = doc.select("#main").first();
        Node clone = div.clone();

        assertNotSame(div, clone);
        assertNull(clone.parent());
        assertEquals(div.outerHtml(), clone.outerHtml());

        Element clonedP = ((Element) clone).select("p").first();
        clonedP.attr("class", "two");

        assertEquals("one", div.select("p").first().attr("class"));
        assertEquals("two", clonedP.attr("class"));
    }

    // Tests equals and hashCode contract
    @Test
    public void testEqualsAndHashCode_sameAndDifferentContent() {
        Document doc1 = Jsoup.parse("<p class='test'>Hello</p>");
        Document doc2 = Jsoup.parse("<p class='test'>Hello</p>");
        Document doc3 = Jsoup.parse("<p class='other'>Hello</p>");

        Element p1 = doc1.select("p").first();
        Element p2 = doc2.select("p").first();
        Element p3 = doc3.select("p").first();

        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
        assertNotEquals(p1, p3);
        assertNotEquals(p1, null);
        assertNotEquals(p1, "not a node");
    }

    // Tests remove node throws exception when parent is null
    @Test(expected = IllegalArgumentException.class)
    public void testRemove_orphanNode_throwsException() {
        Element orphan = new Element(Tag.valueOf("p"), "");
        orphan.remove();
    }
}