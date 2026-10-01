package org.jsoup.nodes;

import org.jsoup.Jsoup;
import org.jsoup.select.NodeVisitor;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class NodeTest {

    // Tests that siblingNodes does not include the node itself (Defects4J 22 defect)
    @Test
    public void testSiblingNodes_nodeWithSiblings_doesNotContainSelf() {
        Document doc = Jsoup.parse("<div><p>One</p><p>Two</p><p>Three</p></div>");
        Element p1 = doc.select("p").get(0);
        Element p2 = doc.select("p").get(1);
        Element p3 = doc.select("p").get(2);

        List<Node> siblings = p2.siblingNodes();
        assertEquals(2, siblings.size());
        assertTrue(siblings.contains(p1));
        assertTrue(siblings.contains(p3));
        assertFalse(siblings.contains(p2));
    }

    // Tests siblingNodes on a root/orphan node without parent
    @Test
    public void testSiblingNodes_orphanNode_returnsEmptyList() {
        Element p = new Element(org.jsoup.parser.Tag.valueOf("p"), "");
        List<Node> siblings = p.siblingNodes();
        assertNotNull(siblings);
        assertTrue(siblings.isEmpty());
    }

    // Tests retrieving and setting attributes
    @Test
    public void testAttr_existingAndNewAttributes_setsAndGetsCorrectly() {
        Document doc = Jsoup.parse("<a href='http://example.com' title='test'>Link</a>");
        Node link = doc.select("a").first();

        assertEquals("http://example.com", link.attr("href"));
        assertEquals("test", link.attr("title"));
        assertEquals("", link.attr("nonexistent"));

        link.attr("rel", "nofollow");
        assertEquals("nofollow", link.attr("rel"));
    }

    // Tests hasAttr including case sensitivity and abs: prefix
    @Test
    public void testHasAttr_normalAndAbsPrefix_returnsExpectedBoolean() {
        Document doc = Jsoup.parse("<a href='/path'>Link</a>", "http://example.com");
        Node link = doc.select("a").first();

        assertTrue(link.hasAttr("href"));
        assertFalse(link.hasAttr("title"));
        assertTrue(link.hasAttr("abs:href"));
        assertFalse(link.hasAttr("abs:title"));
    }

    // Tests removing an attribute
    @Test
    public void testRemoveAttr_existingAttribute_removesSuccessfully() {
        Document doc = Jsoup.parse("<a href='http://example.com' class='link'>Link</a>");
        Node link = doc.select("a").first();

        assertTrue(link.hasAttr("class"));
        link.removeAttr("class");
        assertFalse(link.hasAttr("class"));
    }

    // Tests absUrl resolution with base URI and query params
    @Test
    public void testAbsUrl_relativeAndAbsolute_resolvesCorrectUrl() {
        Document doc = Jsoup.parse("<a href='/path'>1</a><a href='?query=1'>2</a><a href='http://other.com'>3</a>", "http://example.com/dir/file");
        List<Element> links = doc.select("a");

        assertEquals("http://example.com/path", links.get(0).absUrl("href"));
        assertEquals("http://example.com/dir/file?query=1", links.get(1).absUrl("href"));
        assertEquals("http://other.com", links.get(2).absUrl("href"));
        assertEquals("", links.get(0).absUrl("missing"));
    }

    // Tests updating base URI recursively on descendants
    @Test
    public void testSetBaseUri_descendantNodes_updatesAllDescendants() {
        Document doc = Jsoup.parse("<div><p><span>Text</span></p></div>", "http://old.com");
        doc.setBaseUri("http://new.com");

        assertEquals("http://new.com", doc.baseUri());
        assertEquals("http://new.com", doc.select("div").first().baseUri());
        assertEquals("http://new.com", doc.select("span").first().baseUri());
    }

    // Tests ownerDocument retrieval for attached and detached nodes
    @Test
    public void testOwnerDocument_attachedAndDetached_returnsDocumentOrNull() {
        Document doc = Jsoup.parse("<div><p>Text</p></div>");
        Node p = doc.select("p").first();
        assertEquals(doc, p.ownerDocument());

        Element orphan = new Element(org.jsoup.parser.Tag.valueOf("div"), "");
        assertNull(orphan.ownerDocument());
    }

    // Tests before and after insertions using Node and HTML strings
    @Test
    public void testBeforeAndAfter_nodeAndHtml_insertsInCorrectPosition() {
        Document doc = Jsoup.parse("<div><p id='target'>Target</p></div>");
        Element target = doc.select("#target").first();

        target.before("<span>BeforeHtml</span>");
        target.after("<span>AfterHtml</span>");
        target.before(new Element(org.jsoup.parser.Tag.valueOf("b"), "").text("BeforeNode"));
        target.after(new Element(org.jsoup.parser.Tag.valueOf("i"), "").text("AfterNode"));

        assertEquals("<div><span>BeforeHtml</span><b>BeforeNode</b><p id=\"target\">Target</p><i>AfterNode</i><span>AfterHtml</span></div>",
                doc.body().html().replaceAll("\\s*\n\\s*", ""));
    }

    // Tests wrap and unwrap methods
    @Test
    public void testWrapAndUnwrap_elementNode_wrapsAndUnwrapsCorrectly() {
        Document doc = Jsoup.parse("<div>One <span>Two <b>Three</b></span></div>");
        Element span = doc.select("span").first();

        span.wrap("<div class='wrapper'></div>");
        assertEquals("<div class=\"wrapper\"><span>Two <b>Three</b></span></div>", doc.select(".wrapper").outerHtml());

        Node firstChild = span.unwrap();
        assertNotNull(firstChild);
        assertTrue(firstChild instanceof TextNode);
        assertEquals("Two ", ((TextNode) firstChild).text());
        assertEquals("<div>One Two <b>Three</b></div>", doc.body().html().replaceAll("\\s*\n\\s*", ""));
    }

    // Tests replaceWith
    @Test
    public void testReplaceWith_validNode_replacesTargetNode() {
        Document doc = Jsoup.parse("<div><p>Original</p></div>");
        Element p = doc.select("p").first();
        Element span = new Element(org.jsoup.parser.Tag.valueOf("span"), "").text("Replaced");

        p.replaceWith(span);
        assertEquals("<div><span>Replaced</span></div>", doc.body().html().replaceAll("\\s*\n\\s*", ""));
        assertNull(p.parent());
    }

    // Tests nextSibling, previousSibling, and siblingIndex
    @Test
    public void testNextSiblingAndPreviousSibling_variousIndices_returnsCorrectSiblings() {
        Document doc = Jsoup.parse("<div><p id='1'></p><p id='2'></p><p id='3'></p></div>");
        Element p1 = doc.select("#1").first();
        Element p2 = doc.select("#2").first();
        Element p3 = doc.select("#3").first();

        assertEquals(0, p1.siblingIndex());
        assertEquals(1, p2.siblingIndex());
        assertEquals(2, p3.siblingIndex());

        assertNull(p1.previousSibling());
        assertEquals(p2, p1.nextSibling());

        assertEquals(p1, p2.previousSibling());
        assertEquals(p3, p2.nextSibling());

        assertEquals(p2, p3.previousSibling());
        assertNull(p3.nextSibling());
    }

    // Tests traverse with NodeVisitor
    @Test
    public void testTraverse_customVisitor_visitsAllNodesInOrder() {
        Document doc = Jsoup.parse("<div><p>One</p></div>");
        final List<String> visited = new ArrayList<String>();

        doc.body().traverse(new NodeVisitor() {
            public void head(Node node, int depth) {
                visited.add("head:" + node.nodeName());
            }

            public void tail(Node node, int depth) {
                visited.add("tail:" + node.nodeName());
            }
        });

        assertTrue(visited.contains("head:body"));
        assertTrue(visited.contains("head:div"));
        assertTrue(visited.contains("head:p"));
        assertTrue(visited.contains("tail:p"));
        assertTrue(visited.contains("tail:div"));
        assertTrue(visited.contains("tail:body"));
    }

    // Tests clone creates an independent deep copy
    @Test
    public void testClone_deepCopy_createsIndependentClone() {
        Document doc = Jsoup.parse("<div id='d'><p class='c'>Text</p></div>");
        Element div = doc.select("#d").first();
        Node clone = div.clone();

        assertNull(clone.parent());
        assertEquals(0, clone.siblingIndex());
        assertEquals(div.outerHtml(), clone.outerHtml());

        clone.attr("id", "new-id");
        assertEquals("d", div.attr("id"));
        assertEquals("new-id", clone.attr("id"));
    }

    // Tests remove removes node and its children from parent
    @Test
    public void testRemove_nodeWithChildren_removesFromParent() {
        Document doc = Jsoup.parse("<div><p>Text</p><span>Other</span></div>");
        Element p = doc.select("p").first();

        p.remove();
        assertNull(p.parent());
        assertEquals(1, doc.select("div").first().children().size());
        assertEquals("Other", doc.select("div").first().child(0).text());
    }

    // Tests null attribute key throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testAttr_nullKey_throwsException() {
        Document doc = Jsoup.parse("<div></div>");
        doc.attr(null);
    }

    // Tests remove on root node without parent throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testRemove_orphanNode_throwsException() {
        Element p = new Element(org.jsoup.parser.Tag.valueOf("p"), "");
        p.remove();
    }

    // Tests childNode, childNodes, childNodesCopy, and childNodeSize
    @Test
    public void testChildNodes_retrievalAndCopy_returnsExpectedNodes() {
        Document doc = Jsoup.parse("<div><p>1</p><p>2</p></div>");
        Element div = doc.select("div").first();

        assertEquals(2, div.childNodeSize());
        assertEquals("p", div.childNode(0).nodeName());
        assertEquals("p", div.childNode(1).nodeName());

        List<Node> childList = div.childNodes();
        assertEquals(2, childList.size());

        List<Node> copyList = div.childNodesCopy();
        assertEquals(2, copyList.size());
        assertNotSame(childList.get(0), copyList.get(0));
    }

    // Tests attributes getter
    @Test
    public void testAttributes_retrieval_returnsAttributesObject() {
        Document doc = Jsoup.parse("<div id='test' class='sample'></div>");
        Element div = doc.select("div").first();
        Attributes attrs = div.attributes();
        assertNotNull(attrs);
        assertEquals("test", attrs.get("id"));
        assertEquals("sample", attrs.get("class"));
    }

    // Tests equals, hashCode, and toString
    @Test
    public void testEqualsHashCodeAndToString() {
        Document doc = Jsoup.parse("<div id='1'>Text</div>");
        Element div = doc.select("div").first();

        assertEquals(div, div);
        assertNotEquals(div, null);
        assertNotEquals(div, new Object());
        assertEquals(div.hashCode(), div.hashCode());
        assertEquals(div.outerHtml(), div.toString());
    }

    // Tests nextSibling and previousSibling on orphan node returns null
    @Test
    public void testNextAndPreviousSibling_orphanNode_returnsNull() {
        Element orphan = new Element(org.jsoup.parser.Tag.valueOf("p"), "");
        assertNull(orphan.nextSibling());
        assertNull(orphan.previousSibling());
        assertEquals(0, orphan.siblingIndex());
    }

    // Tests unwrap on empty element returns null
    @Test
    public void testUnwrap_emptyElement_returnsNull() {
        Document doc = Jsoup.parse("<div><p></p></div>");
        Element p = doc.select("p").first();
        Node result = p.unwrap();
        assertNull(result);
        assertEquals("<div></div>", doc.body().html().replaceAll("\\s*\n\\s*", ""));
    }

    // Tests before on orphan node throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testBefore_orphanNode_throwsException() {
        Element orphan = new Element(org.jsoup.parser.Tag.valueOf("p"), "");
        orphan.before("<span>test</span>");
    }

    // Tests after on orphan node throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testAfter_orphanNode_throwsException() {
        Element orphan = new Element(org.jsoup.parser.Tag.valueOf("p"), "");
        orphan.after("<span>test</span>");
    }

    // Tests replaceWith on orphan node throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testReplaceWith_orphanNode_throwsException() {
        Element orphan = new Element(org.jsoup.parser.Tag.valueOf("p"), "");
        orphan.replaceWith(new Element(org.jsoup.parser.Tag.valueOf("span"), ""));
    }

    // Tests absUrl with protocol relative URL
    @Test
    public void testAbsUrl_protocolRelativeUrl_resolvesCorrectly() {
        Document doc = Jsoup.parse("<a href='//example.com/test'>Link</a>", "http://base.com/path");
        Element link = doc.select("a").first();
        assertEquals("http://example.com/test", link.absUrl("href"));
    }

    // Tests absUrl with empty baseUri returns empty string
    @Test
    public void testAbsUrl_emptyBaseUri_returnsEmptyString() {
        Document doc = Jsoup.parse("<a href='/test'>Link</a>", "");
        Element link = doc.select("a").first();
        assertEquals("", link.absUrl("href"));
    }
}