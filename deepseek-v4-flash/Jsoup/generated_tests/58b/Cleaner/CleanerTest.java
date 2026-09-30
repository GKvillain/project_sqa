package org.jsoup.safety;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Comment;
import org.jsoup.parser.Tag;
import org.junit.Test;
import static org.junit.Assert.*;

public class CleanerTest {

    @Test(expected = NullPointerException.class)
    public void testConstructor_nullWhitelist_throwsNullPointerException() {
        new Cleaner(null);
    }

    @Test(expected = NullPointerException.class)
    public void testClean_nullDocument_throwsNullPointerException() {
        Cleaner cleaner = new Cleaner(Whitelist.basic());
        cleaner.clean(null);
    }

    @Test(expected = NullPointerException.class)
    public void testIsValid_nullDocument_throwsNullPointerException() {
        Cleaner cleaner = new Cleaner(Whitelist.basic());
        cleaner.isValid(null);
    }

    @Test
    public void testClean_bodyNull_returnsDocumentWithEmptyBody() {
        Document dirty = new Document("");
        assertNull(dirty.body());
        Cleaner cleaner = new Cleaner(Whitelist.basic());
        Document clean = cleaner.clean(dirty);
        assertNotNull(clean);
        assertNotNull(clean.body());
        assertEquals(0, clean.body().childNodeSize());
    }

    @Test(expected = NullPointerException.class)
    public void testIsValid_bodyNull_throwsNullPointerException() {
        Document dirty = new Document("");
        assertNull(dirty.body());
        Cleaner cleaner = new Cleaner(Whitelist.basic());
        cleaner.isValid(dirty);
    }

    @Test
    public void testClean_allowedTag_keepsTag() {
        Cleaner cleaner = new Cleaner(Whitelist.basic());
        Document dirty = Document.createShell("");
        Element b = new Element(Tag.valueOf("b"), "");
        b.appendChild(new TextNode("bold", ""));
        dirty.body().appendChild(b);

        Document clean = cleaner.clean(dirty);
        assertEquals(1, clean.body().childNodeSize());
        Element cleanB = (Element) clean.body().childNode(0);
        assertEquals("b", cleanB.tagName());
        assertEquals("bold", cleanB.text());
    }

    @Test
    public void testClean_disallowedTag_removesTag() {
        Cleaner cleaner = new Cleaner(Whitelist.basic());
        Document dirty = Document.createShell("");
        Element script = new Element(Tag.valueOf("script"), "");
        script.appendChild(new TextNode("xss", ""));
        dirty.body().appendChild(script);

        Document clean = cleaner.clean(dirty);
        assertEquals(0, clean.body().childNodeSize());
    }

    @Test
    public void testClean_safeAttribute_kept_unsafeRemoved() {
        Cleaner cleaner = new Cleaner(Whitelist.basic());
        Document dirty = Document.createShell("");
        Element a = new Element(Tag.valueOf("a"), "");
        a.attr("href", "http://example.com");
        a.attr("title", "example title");
        dirty.body().appendChild(a);

        Document clean = cleaner.clean(dirty);
        Element cleanA = (Element) clean.body().childNode(0);
        assertEquals("http://example.com", cleanA.attr("href"));
        assertFalse(cleanA.hasAttr("title"));
    }

    @Test
    public void testClean_textNode_kept() {
        Cleaner cleaner = new Cleaner(Whitelist.basic());
        Document dirty = Document.createShell("");
        dirty.body().appendChild(new TextNode("some text", ""));

        Document clean = cleaner.clean(dirty);
        assertEquals(1, clean.body().childNodeSize());
        assertEquals("some text", clean.body().text());
    }

    @Test
    public void testClean_dataNodeInSafeTag_kept() {
        Whitelist relaxed = Whitelist.relaxed();
        Cleaner cleaner = new Cleaner(relaxed);
        Document dirty = Document.createShell("");
        Element style = new Element(Tag.valueOf("style"), "");
        style.appendChild(new DataNode("body { color: red; }", ""));
        dirty.body().appendChild(style);

        Document clean = cleaner.clean(dirty);
        assertEquals(1, clean.body().childNodeSize());
        Element cleanStyle = (Element) clean.body().childNode(0);
        assertEquals(1, cleanStyle.childNodeSize());
        assertTrue(cleanStyle.childNode(0) instanceof DataNode);
        assertEquals("body { color: red; }", ((DataNode) cleanStyle.childNode(0)).getWholeData());
    }

    @Test
    public void testClean_dataNodeInUnsafeTag_discarded() {
        Cleaner cleaner = new Cleaner(Whitelist.basic());
        Document dirty = Document.createShell("");
        Element style = new Element(Tag.valueOf("style"), "");
        style.appendChild(new DataNode("dummy", ""));
        dirty.body().appendChild(style);

        Document clean = cleaner.clean(dirty);
        assertEquals(0, clean.body().childNodeSize());
    }

    @Test
    public void testIsValid_validBody_returnsTrue() {
        Cleaner cleaner = new Cleaner(Whitelist.basic());
        Document dirty = Document.createShell("");
        Element b = new Element(Tag.valueOf("b"), "");
        b.appendChild(new TextNode("bold", ""));
        dirty.body().appendChild(b);

        assertTrue(cleaner.isValid(dirty));
    }

    @Test
    public void testIsValid_invalidBody_returnsFalse() {
        Cleaner cleaner = new Cleaner(Whitelist.basic());
        Document dirty = Document.createShell("");
        Element script = new Element(Tag.valueOf("script"), "");
        script.appendChild(new TextNode("xss", ""));
        dirty.body().appendChild(script);

        assertFalse(cleaner.isValid(dirty));
    }

    @Test
    public void testClean_enforcedAttribute_added() {
        Whitelist w = new Whitelist();
        w.addTags("a");
        w.addAttributes("a", "href");
        w.addEnforcedAttribute("a", "rel", "nofollow");
        Cleaner cleaner = new Cleaner(w);
        Document dirty = Document.createShell("");
        Element a = new Element(Tag.valueOf("a"), "");
        a.attr("href", "http://example.com");
        dirty.body().appendChild(a);

        Document clean = cleaner.clean(dirty);
        Element cleanA = (Element) clean.body().childNode(0);
        assertEquals("http://example.com", cleanA.attr("href"));
        assertEquals("nofollow", cleanA.attr("rel"));
    }

    @Test
    public void testClean_nestedAllowedTags_preservesStructure() {
        Cleaner cleaner = new Cleaner(Whitelist.basic());
        Document dirty = Document.createShell("");
        Element p = new Element(Tag.valueOf("p"), "");
        Element b = new Element(Tag.valueOf("b"), "");
        b.appendChild(new TextNode("bold in p", ""));
        p.appendChild(b);
        dirty.body().appendChild(p);

        Document clean = cleaner.clean(dirty);
        assertEquals(1, clean.body().childNodeSize());
        Element cleanP = (Element) clean.body().childNode(0);
        assertEquals("p", cleanP.tagName());
        assertEquals(1, cleanP.childNodeSize());
        Element cleanB = (Element) cleanP.childNode(0);
        assertEquals("b", cleanB.tagName());
        assertEquals("bold in p", cleanB.text());
    }

    @Test
    public void testClean_disallowedNestedElement_survivesChildren() {
        Cleaner cleaner = new Cleaner(Whitelist.basic());
        Document dirty = Document.createShell("");
        Element div = new Element(Tag.valueOf("div"), "");
        Element b = new Element(Tag.valueOf("b"), "");
        b.appendChild(new TextNode("bold", ""));
        div.appendChild(b);
        dirty.body().appendChild(div);

        Document clean = cleaner.clean(dirty);
        assertEquals(1, clean.body().childNodeSize());
        Element cleanB = (Element) clean.body().childNode(0);
        assertEquals("b", cleanB.tagName());
        assertEquals("bold", cleanB.text());
    }

    @Test
    public void testClean_rootElementDisallowed_doesNotDiscardRoot() {
        Cleaner cleaner = new Cleaner(Whitelist.basic());
        Document dirty = Document.createShell("");
        Element p = new Element(Tag.valueOf("p"), "");
        p.appendChild(new TextNode("para", ""));
        dirty.body().appendChild(p);

        Document clean = cleaner.clean(dirty);
        assertEquals("body", clean.body().tagName());
        assertEquals(1, clean.body().childNodeSize());
        Element cleanP = (Element) clean.body().childNode(0);
        assertEquals("p", cleanP.tagName());
        assertEquals("para", cleanP.text());
    }

    // ===== New test cases to improve coverage =====

    @Test
    public void testClean_headContentIsDiscarded() {
        // Ensure that only body content is cleaned; head elements are dropped
        Document dirty = Document.createShell("");
        Element title = new Element(Tag.valueOf("title"), "");
        title.appendChild(new TextNode("My Page", ""));
        dirty.head().appendChild(title);

        // Also add something in body
        Element p = new Element(Tag.valueOf("p"), "");
        p.appendChild(new TextNode("body text", ""));
        dirty.body().appendChild(p);

        Cleaner cleaner = new Cleaner(Whitelist.relaxed());
        Document clean = cleaner.clean(dirty);

        // head should exist but empty (or no head element at all in output? In jsoup, clean creates a new Document with empty head)
        assertNotNull(clean.head());
        assertEquals(0, clean.head().childNodeSize());
        // body should have the paragraph
        assertEquals(1, clean.body().childNodeSize());
    }

    @Test
    public void testClean_hrefWithUnsafeProtocol_removesHref() {
        // href with javascript: protocol should be removed as unsafe
        Whitelist w = Whitelist.basic();
        w.addProtocols("a", "href", "http", "https", "ftp"); // ftp allowed
        Cleaner cleaner = new Cleaner(w);
        Document dirty = Document.createShell("");
        Element a = new Element(Tag.valueOf("a"), "");
        a.attr("href", "javascript:alert(1)");
        dirty.body().appendChild(a);

        Document clean = cleaner.clean(dirty);
        Element cleanA = (Element) clean.body().childNode(0);
        // href attribute should be removed because protocol not allowed
        assertFalse(cleanA.hasAttr("href"));
    }

    @Test
    public void testClean_commentNodeIsRemoved() {
        // Comment nodes should be stripped (not copied)
        Cleaner cleaner = new Cleaner(Whitelist.basic());
        Document dirty = Document.createShell("");
        dirty.body().appendChild(new Comment("<!-- comment -->", ""));
        // also add a valid text to ensure body is not empty
        dirty.body().appendChild(new TextNode("visible", ""));

        Document clean = cleaner.clean(dirty);
        // Only text node should remain
        assertEquals(1, clean.body().childNodeSize());
        assertTrue(clean.body().childNode(0) instanceof TextNode);
        assertEquals("visible", ((TextNode) clean.body().childNode(0)).text());
    }

    @Test
    public void testClean_whitelistNone_removesAllTags() {
        // Whitelist.none() allows no tags, so everything should be removed except text nodes?
        // Actually text nodes are kept even with none whitelist? Let's check behavior:
        // jsoup's Whitelist.none() includes no tags, but text nodes are always kept (unless inside disallowed tag that is removed, but text becomes orphan and is kept? In code, if tag is disallowed, it is removed but its children are promoted. So text nodes survive)
        Cleaner cleaner = new Cleaner(Whitelist.none());
        Document dirty = Document.createShell("");
        Element b = new Element(Tag.valueOf("b"), "");
        b.appendChild(new TextNode("bold", ""));
        dirty.body().appendChild(b);
        // also add plain text
        dirty.body().appendChild(new TextNode(" plain", ""));

        Document clean = cleaner.clean(dirty);
        // <b> is disallowed, so its children (text) get promoted. So we expect two text nodes in body
        assertEquals(2, clean.body().childNodeSize());
        assertEquals("bold", ((TextNode) clean.body().childNode(0)).text());
        assertEquals(" plain", ((TextNode) clean.body().childNode(1)).text());
    }

    @Test
    public void testClean_whitelistSimpleText_allowsOnlyBasicInline() {
        // Whitelist.simpleText() allows b, i, em, strong, etc.
        Cleaner cleaner = new Cleaner(Whitelist.simpleText());
        Document dirty = Document.createShell("");
        Element b = new Element(Tag.valueOf("b"), "");
        b.appendChild(new TextNode("bold", ""));
        Element div = new Element(Tag.valueOf("div"), "");
        div.appendChild(new TextNode("div text", ""));
        dirty.body().appendChild(b);
        dirty.body().appendChild(div);

        Document clean = cleaner.clean(dirty);
        // b is allowed, div is not
        assertEquals(2, clean.body().childNodeSize());
        assertTrue(clean.body().childNode(0) instanceof Element);
        assertEquals("b", ((Element) clean.body().childNode(0)).tagName());
        // div's children (text) should be promoted
        assertTrue(clean.body().childNode(1) instanceof TextNode);
        assertEquals("div text", ((TextNode) clean.body().childNode(1)).text());
    }

    @Test
    public void testIsValid_bodyWithExtraAttributes_returnsFalse() {
        // isValid compares the dirty body with the cleaned body; if dirty has attribute that would be removed, isValid returns false
        Whitelist w = Whitelist.basic();
        w.addAttributes("a", "href"); // only href allowed for a
        Cleaner cleaner = new Cleaner(w);
        Document dirty = Document.createShell("");
        Element a = new Element(Tag.valueOf("a"), "");
        a.attr("href", "http://example.com");
        a.attr("title", "extra"); // title not allowed
        dirty.body().appendChild(a);

        assertFalse(cleaner.isValid(dirty));
    }

    @Test
    public void testIsValid_bodyWithProtocolViolation_returnsFalse() {
        // If dirty body contains an attribute with protocol that is not in whitelist, isValid should return false
        Whitelist w = Whitelist.basic();
        w.addProtocols("a", "href", "http", "https");
        Cleaner cleaner = new Cleaner(w);
        Document dirty = Document.createShell("");
        Element a = new Element(Tag.valueOf("a"), "");
        a.attr("href", "ftp://example.com"); // ftp not allowed
        dirty.body().appendChild(a);

        assertFalse(cleaner.isValid(dirty));
    }
}