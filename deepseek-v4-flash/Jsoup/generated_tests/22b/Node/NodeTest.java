package org.jsoup.nodes;

import org.jsoup.parser.Tag;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * JUnit 4 test class for org.jsoup.nodes.Node (Defects4J bug 22b).
 */
public class NodeTest {
    private Document doc;
    private Element div;
    private Element span;

    @Before
    public void setUp() {
        doc = new Document("http://example.com");
        div = new Element(Tag.valueOf("div"), doc.baseUri());
        span = new Element(Tag.valueOf("span"), doc.baseUri());
        doc.appendChild(div);
        div.appendChild(span);
        doc.setBaseUri("http://example.com");
    }

    @Test
    public void testAttr_existingKey_returnsValue() {
        div.attr("class", "container");
        assertEquals("container", div.attr("class"));
    }

    @Test
    public void testAttr_missingKey_returnsEmpty() {
        assertEquals("", div.attr("nonexistent"));
    }

    @Test
    public void testAttr_absPrefix_validRelativeUrl_returnsAbsoluteUrl() {
        div.attr("href", "page.html");
        assertEquals("http://example.com/page.html", div.attr("abs:href"));
    }

    @Test
    public void testHasAttr_existingKey_true() {
        div.attr("id", "main");
        assertTrue(div.hasAttr("id"));
    }

    @Test
    public void testHasAttr_missingKey_false() {
        assertFalse(div.hasAttr("missing"));
    }

    @Test
    public void testHasAttr_absPrefix_keyExistsAndUrlValid_true() {
        div.attr("href", "page.html");
        assertTrue(div.hasAttr("abs:href"));
    }

    @Test
    public void testHasAttr_absPrefix_keyExistsButUrlInvalid_false() {
        Document doc2 = new Document("invalid:");
        Element div2 = new Element(Tag.valueOf("div"), doc2.baseUri());
        doc2.appendChild(div2);
        doc2.setBaseUri("invalid:");
        div2.attr("href", "page.html");
        assertFalse(div2.hasAttr("abs:href"));
    }

    @Test
    public void testAbsUrl_normal_relUrl_returnsAbsoluteUrl() {
        div.attr("href", "page.html");
        assertEquals("http://example.com/page.html", div.absUrl("href"));
    }

    @Test
    public void testAbsUrl_baseUriMalformed_relUrlAbsolute_returnsAbsolute() {
        Document doc2 = new Document("invalid:");
        Element div2 = new Element(Tag.valueOf("div"), doc2.baseUri());
        doc2.appendChild(div2);
        doc2.setBaseUri("invalid:");
        div2.attr("href", "http://other.com/page");
        assertEquals("http://other.com/page", div2.absUrl("href"));
    }

    @Test
    public void testAbsUrl_baseUriMalformed_relUrlRelative_returnsEmpty() {
        Document doc2 = new Document("invalid:");
        Element div2 = new Element(Tag.valueOf("div"), doc2.baseUri());
        doc2.appendChild(div2);
        doc2.setBaseUri("invalid:");
        div2.attr("href", "page.html");
        assertEquals("", div2.absUrl("href"));
    }

    @Test
    public void testSetBaseUri_updatesNodeAndChildren() {
        doc.setBaseUri("http://newbase.com");
        assertEquals("http://newbase.com", div.baseUri());
        assertEquals("http://newbase.com", span.baseUri());
    }

    @Test
    public void testRemove_removesNodeFromParent() {
        span.remove();
        assertTrue(div.childNodes().isEmpty());
    }

    @Test
    public void testBeforeNode_insertsBeforeSibling() {
        Element i = new Element(Tag.valueOf("i"), doc.baseUri());
        span.before(i);
        assertEquals("i", div.childNode(0).nodeName());
        assertEquals("span", div.childNode(1).nodeName());
    }

    @Test
    public void testAfterNode_insertsAfterSibling() {
        Element b = new Element(Tag.valueOf("b"), doc.baseUri());
        span.after(b);
        assertEquals("span", div.childNode(0).nodeName());
        assertEquals("b", div.childNode(1).nodeName());
    }

    @Test
    public void testWrap_wrapsInElement() {
        span.wrap("<div class='wrapper'></div>");
        Node parent = span.parent();
        assertEquals("div", parent.nodeName());
        assertEquals("div", div.childNode(0).nodeName());
    }

    @Test
    public void testClone_createsDeepCopy() {
        Node clone = div.clone();
        assertNotSame(div, clone);
        assertEquals(div.childNodes().size(), clone.childNodes().size());
        assertEquals(div.attr("class"), clone.attr("class"));
        assertNull(clone.parent());
    }

    @Test
    public void testNextSibling_lastChild_null() {
        Element b = new Element(Tag.valueOf("b"), doc.baseUri());
        div.appendChild(b);
        assertNull(b.nextSibling());
        assertNotNull(span.nextSibling());
    }

    @Test
    public void testPreviousSibling_firstChild_null() {
        Element b = new Element(Tag.valueOf("b"), doc.baseUri());
        div.appendChild(b);
        assertNull(span.previousSibling());
        assertNotNull(b.previousSibling());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAttr_nullKey_throwsIllegalArgumentExtion() {
        div.attr(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAbsUrl_emptyKey_throwsIllegalArgumentExtion() {
        div.absUrl("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBefore_nullNode_throwsIllegalArgumentExtion() {
        div.before((Node) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAfter_nullNode_throwsIllegalArgumentExtion() {
        div.after((Node) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveChild_wrongParent_throwsIllegalArgumentExtion() {
        // span's parent is div, not doc; attempting doc.removeChild(span) should fail
        doc.removeChild(span);
    }
}