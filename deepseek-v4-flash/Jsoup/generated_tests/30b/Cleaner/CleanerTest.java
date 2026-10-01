package org.jsoup.safety;

import org.junit.Test;
import static org.junit.Assert.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.safety.Whitelist;

public class CleanerTest {

    // Tests clean method with a simple safe document (text only)
    @Test
    public void testClean_safeText_returnsSameContent() {
        Whitelist whitelist = Whitelist.none();
        Cleaner cleaner = new Cleaner(whitelist);
        String html = "Hello World";
        Document dirty = Jsoup.parse(html);
        Document clean = cleaner.clean(dirty);
        // With none whitelist, only text nodes are allowed; body should contain the text
        assertEquals("Hello World", clean.body().text());
    }

    // Tests clean method with a safe tag (by whitelist)
    @Test
    public void testClean_safeTag_retainsElement() {
        Whitelist whitelist = Whitelist.simpleText();
        Cleaner cleaner = new Cleaner(whitelist);
        String html = "<b>bold</b>";
        Document dirty = Jsoup.parse(html);
        Document clean = cleaner.clean(dirty);
        assertEquals("<b>bold</b>", clean.body().html());
    }

    // Tests clean method with an unsafe tag (not in whitelist)
    @Test
    public void testClean_unsafeTag_removesElement() {
        Whitelist whitelist = Whitelist.simpleText(); // allows b, i, em, etc.
        Cleaner cleaner = new Cleaner(whitelist);
        String html = "<div>block</div>";
        Document dirty = Jsoup.parse(html);
        Document clean = cleaner.clean(dirty);
        // div is removed, but text inside is kept (since text is allowed)
        assertEquals("block", clean.body().text());
        assertEquals("block", clean.body().html()); // no tags
    }

    // Tests clean method with nested unsafe -> safe elements
    @Test
    public void testClean_nestedUnsafeSafe_preservesSafeChildren() {
        Whitelist whitelist = Whitelist.simpleText();
        Cleaner cleaner = new Cleaner(whitelist);
        String html = "<div><b>bold inside div</b></div>";
        Document dirty = Jsoup.parse(html);
        Document clean = cleaner.clean(dirty);
        // div removed, <b> preserved
        assertEquals("<b>bold inside div</b>", clean.body().html());
    }

    // Tests clean method when element has unsafe attribute
    @Test
    public void testClean_unsafeAttribute_removed() {
        // Use a relaxed whitelist that allows 'a' but not 'style' attribute
        Whitelist whitelist = Whitelist.relaxed(); // allows many tags and attributes, but not all
        // But we want to specifically test attribute removal; better to use base
        // Use a custom whitelist that only allows 'a' tag with 'href' attribute
        Whitelist custom = Whitelist.none();
        custom.addTags("a");
        custom.addAttributes("a", "href");
        Cleaner cleaner = new Cleaner(custom);
        String html = "<a href=\"http://example.com\" style=\"color:red\">link</a>";
        Document dirty = Jsoup.parse(html);
        Document clean = cleaner.clean(dirty);
        // style attribute should be removed
        assertEquals("<a href=\"http://example.com\">link</a>", clean.body().html());
    }

    // Tests clean method with enforced attributes
    @Test
    public void testClean_enforcedAttribute_added() {
        Whitelist whitelist = Whitelist.none();
        whitelist.addTags("a");
        whitelist.addEnforcedAttribute("a", "rel", "nofollow");
        Cleaner cleaner = new Cleaner(whitelist);
        String html = "<a href=\"http://example.com\">link</a>";
        Document dirty = Jsoup.parse(html);
        Document clean = cleaner.clean(dirty);
        // enforced attribute rel="nofollow" should be added
        assertTrue(clean.body().html().contains("rel=\"nofollow\""));
    }

    // Tests isValid method with a valid document
    @Test
    public void testIsValid_validDocument_returnsTrue() {
        Whitelist whitelist = Whitelist.simpleText();
        Cleaner cleaner = new Cleaner(whitelist);
        Document doc = Jsoup.parse("<b>bold</b>");
        assertTrue(cleaner.isValid(doc));
    }

    // Tests isValid method with an invalid document (unsafe tag)
    @Test
    public void testIsValid_invalidTag_returnsFalse() {
        Whitelist whitelist = Whitelist.simpleText();
        Cleaner cleaner = new Cleaner(whitelist);
        Document doc = Jsoup.parse("<div>block</div>");
        assertFalse(cleaner.isValid(doc));
    }

    // Tests isValid method with mixed safe/unsafe elements
    @Test
    public void testIsValid_mixedContent_returnsFalse() {
        Whitelist whitelist = Whitelist.simpleText();
        Cleaner cleaner = new Cleaner(whitelist);
        Document doc = Jsoup.parse("<b>bold</b><script>alert('xss')</script>");
        assertFalse(cleaner.isValid(doc));
    }

    // Tests isValid method with unsafe attribute
    @Test
    public void testIsValid_unsafeAttribute_returnsFalse() {
        Whitelist whitelist = Whitelist.none();
        whitelist.addTags("a");
        whitelist.addAttributes("a", "href");
        Cleaner cleaner = new Cleaner(whitelist);
        Document doc = Jsoup.parse("<a href=\"http://example.com\" style=\"color:red\">link</a>");
        assertFalse(cleaner.isValid(doc));
    }

    // Tests isValid with a frameset document (no body) - should not throw NPE (bug detection)
    @Test
    public void testIsValid_framesetDocument_returnsTrue() {
        Whitelist whitelist = Whitelist.none();
        Cleaner cleaner = new Cleaner(whitelist);
        // Parsing a frameset document produces a document without body
        Document framesetDoc = Jsoup.parse("<frameset><frame src='a.html'></frameset>");
        // The buggy version throws NullPointerException here; test expects no exception and returns true
        assertTrue(cleaner.isValid(framesetDoc));
    }

    // Tests isValid with null document - expects exception
    @Test(expected = IllegalArgumentException.class)
    public void testIsValid_nullDocument_throwsIllegalArgumentException() {
        Whitelist whitelist = Whitelist.none();
        Cleaner cleaner = new Cleaner(whitelist);
        cleaner.isValid(null);
    }

    // Tests clean with null document - expects exception
    @Test(expected = IllegalArgumentException.class)
    public void testClean_nullDocument_throwsIllegalArgumentException() {
        Whitelist whitelist = Whitelist.none();
        Cleaner cleaner = new Cleaner(whitelist);
        cleaner.clean(null);
    }

    // Tests copySafeNodes when source has no children
    @Test
    public void testClean_emptyBody_returnsEmptyBody() {
        Whitelist whitelist = Whitelist.simpleText();
        Cleaner cleaner = new Cleaner(whitelist);
        Document dirty = Jsoup.parse("");
        Document clean = cleaner.clean(dirty);
        assertEquals("", clean.body().text());
    }

    // Tests copySafeNodes with only text nodes
    @Test
    public void testClean_onlyText_preservesText() {
        Whitelist whitelist = Whitelist.none();
        Cleaner cleaner = new Cleaner(whitelist);
        Document dirty = Jsoup.parse("Hello");
        Document clean = cleaner.clean(dirty);
        assertEquals("Hello", clean.body().text());
    }

    // Tests multiple children: safe and unsafe
    @Test
    public void testClean_mixedChildren_keepsSafeAndText() {
        Whitelist whitelist = Whitelist.simpleText();
        Cleaner cleaner = new Cleaner(whitelist);
        String html = "<div>text</div><b>bold</b><i>italic</i><script>alert('xss')</script>";
        Document dirty = Jsoup.parse(html);
        Document clean = cleaner.clean(dirty);
        // Expected: text from div kept, <b>, <i> kept, script removed
        String expected = "text<b>bold</b><i>italic</i>";
        assertEquals(expected, clean.body().html());
    }

    // Tests deep nesting and discarded count (indirectly through isValid)
    @Test
    public void testIsValid_deepNestingUnsafe_returnsFalse() {
        Whitelist whitelist = Whitelist.simpleText();
        Cleaner cleaner = new Cleaner(whitelist);
        String html = "<div><p><span>nested</span></p></div>";
        Document dirty = Jsoup.parse(html);
        assertFalse(cleaner.isValid(dirty));
    }

    // Tests clean with a document that has multiple attributes and enforced attributes
    @Test
    public void testClean_enforcedAndSafeAttributes_combined() {
        Whitelist whitelist = Whitelist.none();
        whitelist.addTags("a");
        whitelist.addAttributes("a", "href");
        whitelist.addEnforcedAttribute("a", "target", "_blank");
        Cleaner cleaner = new Cleaner(whitelist);
        String html = "<a href=\"http://example.com\" class=\"link\">example</a>";
        Document dirty = Jsoup.parse(html);
        Document clean = cleaner.clean(dirty);
        // class attribute should be removed, target="_blank" added
        String expected = "<a href=\"http://example.com\" target=\"_blank\">example</a>";
        assertEquals(expected, clean.body().html());
    }
}