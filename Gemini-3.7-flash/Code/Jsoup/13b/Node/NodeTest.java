package org.jsoup.nodes;

import org.jsoup.Jsoup;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class NodeTest {

    // Tests absUrl with relative path
    @Test
    public void testAbsUrl_relativeUrl_returnsAbsoluteUrl() {
        Document doc = Jsoup.parse("<a href='path/page.html'>Link</a>", "http://example.com/dir/");
        Element link = doc.select("a").first();
        assertEquals("http://example.com/dir/path/page.html", link.absUrl("href"));
    }

    // Tests absUrl with query parameter starting with ?
    @Test
    public void testAbsUrl_queryRelativeUrl_resolvesCorrectly() {
        Document doc = Jsoup.parse("<a href='?foo=bar'>Link</a>", "http://example.com/dir/page.html");
        Element link = doc.select("a").first();
        assertEquals("http://example.com/dir/page.html?foo=bar", link.absUrl("href"));
    }

    // Tests absUrl with invalid base URL but valid absolute attribute URL
    @Test
    public void testAbsUrl_invalidBaseUriWithAbsoluteAttribute_returnsAttributeUrl() {
        Document doc = Jsoup.parse("<a href='http://target.com/'>Link</a>", "invalid-uri");
        Element link = doc.select("a").first();
        assertEquals("http://target.com/", link.absUrl("href"));
    }

    // Tests absUrl with non-existent attribute
    @Test
    public void testAbsUrl_attributeDoesNotExist_returnsEmptyString() {
        Document doc = Jsoup.parse("<a>Link</a>", "http://example.com/");
        Element link = doc.select("a").first();
        assertEquals("", link.absUrl("href"));
    }

    // Tests hasAttr and attr with abs: prefix shortcut (Defects4J 13b target)
    @Test
    public void testHasAttr_absPrefixAttribute_returnsCorrectValue() {
        Document doc = Jsoup.parse("<a href='/foo'>Link</a>", "http://example.com/");
        Element link = doc.select("a").first();
        assertTrue(link.hasAttr("abs:href"));
        assertEquals("http://example.com/foo", link.attr("abs:href"));

        assertFalse(link.hasAttr("abs:nonexistent"));
        assertEquals("", link.attr("abs:nonexistent"));
    }

    // Tests attr method for direct attributes and case variations
    @Test
    public void testAttr_existingAndMissingAttributes_returnsExpected() {
        Document doc = Jsoup.parse("<div id='test' title='sample'></div>");
        Element div = doc.select("div").first();
        assertEquals("test", div.attr("id"));
        assertEquals("sample", div.attr("title"));
        assertEquals("", div.attr("class"));

        div.removeAttr("title");
        assertFalse(div.hasAttr("title"));
    }

    // Tests setBaseUri updates baseUri correctly
    @Test
    public void testSetBaseUri_validUri_updatesBaseUri() {
        Document doc = Jsoup.parse("<a href='test.html'>Link</a>", "http://example.com/");
        Element link = doc.select("a").first();
        link.setBaseUri("http://other.com/dir/");
        assertEquals("http://other.com/dir/", link.baseUri());
        assertEquals("http://other.com/dir/test.html", link.absUrl("href"));
    }

    // Tests sibling navigation methods
    @Test
    public void testSiblings_navigationMethods_returnsCorrectSiblings() {
        Document doc = Jsoup.parse("<div><p id='1'></p><p id='2'></p><p id='3'></p></div>");
        Element p1 = doc.getElementById("1");
        Element p2 = doc.getElementById("2");
        Element p3 = doc.getElementById("3");

        assertNull(p1.previousSibling());
        assertEquals(p2, p1.nextSibling());
        assertEquals(p1, p2.previousSibling());
        assertEquals(p3, p2.nextSibling());
        assertNull(p3.nextSibling());

        List<Node> siblings = p2.siblingNodes();
        assertEquals(3, siblings.size());
        assertEquals(1, p2.siblingIndex());
    }

    // Tests before and after with HTML and Node
    @Test
    public void testBeforeAndAfter_insertingNodesAndHtml_insertsCorrectly() {
        Document doc = Jsoup.parse("<div id='root'><p id='target'>Middle</p></div>");
        Element target = doc.getElementById("target");

        target.before("<span id='before-span'>Before</span>");
        target.after("<span id='after-span'>After</span>");

        Element root = doc.getElementById("root");
        assertEquals(3, root.children().size());
        assertEquals("before-span", root.child(0).id());
        assertEquals("target", root.child(1).id());
        assertEquals("after-span", root.child(2).id());

        Element nodeBefore = new Element(org.jsoup.parser.Tag.valueOf("b"), "");
        nodeBefore.attr("id", "b-node");
        target.before(nodeBefore);
        assertEquals(4, root.children().size());
        assertEquals("b-node", root.child(1).id());

        Element nodeAfter = new Element(org.jsoup.parser.Tag.valueOf("i"), "");
        nodeAfter.attr("id", "i-node");
        target.after(nodeAfter);
        assertEquals(5, root.children().size());
        assertEquals("i-node", root.child(3).id());
    }

    // Tests wrap and replaceWith methods
    @Test
    public void testWrapAndReplaceWith_domManipulation_updatesTreeCorrectly() {
        Document doc = Jsoup.parse("<div><p id='target'>Hello</p></div>");
        Element target = doc.getElementById("target");

        target.wrap("<div class='wrapper'><div class='inner'></div></div>");
        assertEquals("<div><div class=\"wrapper\"><div class=\"inner\"><p id=\"target\">Hello</p></div></div></div>",
                doc.body().outerHtml().replaceAll("\\s+", " ").trim());

        Element replacement = new Element(org.jsoup.parser.Tag.valueOf("span"), "");
        replacement.text("Replaced");
        target.replaceWith(replacement);
        assertNull(target.parent());
        assertEquals("Replaced", doc.select("div.inner span").first().text());
    }

    // Tests remove method
    @Test
    public void testRemove_existingNode_removesFromParent() {
        Document doc = Jsoup.parse("<div><p id='1'></p><p id='2'></p></div>");
        Element p1 = doc.getElementById("1");
        Element p2 = doc.getElementById("2");

        p1.remove();
        assertNull(p1.parent());
        assertEquals(1, doc.select("div p").size());
        assertEquals(0, p2.siblingIndex());
    }

    // Tests ownerDocument traversal
    @Test
    public void testOwnerDocument_documentAssociation_returnsOwnerDocument() {
        Document doc = Jsoup.parse("<div><p>Text</p></div>");
        Element p = doc.select("p").first();
        assertEquals(doc, p.ownerDocument());
        assertEquals(doc, doc.ownerDocument());

        Element orphan = new Element(org.jsoup.parser.Tag.valueOf("p"), "");
        assertNull(orphan.ownerDocument());
    }

    // Tests clone method deep copying
    @Test
    public void testClone_createsDeepIndependentCopy() {
        Document doc = Jsoup.parse("<div id='1' class='main'><p>Child</p></div>", "http://example.com/");
        Element div = doc.getElementById("1");
        Node clone = div.clone();

        assertNull(clone.parent());
        assertEquals(1, clone.childNodes().size());
        assertEquals("main", clone.attr("class"));
        assertEquals("http://example.com/", clone.baseUri());

        clone.attr("class", "modified");
        assertEquals("main", div.attr("class"));
        assertEquals("modified", clone.attr("class"));
    }

    // Tests exception path for null attribute key
    @Test(expected = IllegalArgumentException.class)
    public void testAttr_nullKey_throwsException() {
        Document doc = Jsoup.parse("<div></div>");
        doc.select("div").first().attr(null);
    }

    // Tests exception path for removing non-parented node
    @Test(expected = IllegalArgumentException.class)
    public void testRemove_orphanNode_throwsException() {
        Element orphan = new Element(org.jsoup.parser.Tag.valueOf("p"), "");
        orphan.remove();
    }
}