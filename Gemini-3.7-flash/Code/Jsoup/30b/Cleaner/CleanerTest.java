package org.jsoup.safety;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CleanerTest {
    private Cleaner cleaner;

    @Before
    public void setUp() {
        cleaner = new Cleaner(Whitelist.basic());
    }

    // Tests null whitelist constructor exception path
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullWhitelist_throwsException() {
        new Cleaner(null);
    }

    // Tests null dirtyDocument in clean method exception path
    @Test(expected = IllegalArgumentException.class)
    public void testClean_nullDocument_throwsException() {
        cleaner.clean(null);
    }

    // Tests null dirtyDocument in isValid method exception path
    @Test(expected = IllegalArgumentException.class)
    public void testIsValid_nullDocument_throwsException() {
        cleaner.isValid(null);
    }

    // Tests cleaning a document containing only valid tags and text
    @Test
    public void testClean_validDocument_returnsCleanHtml() {
        Document dirty = Jsoup.parse("<p>Hello <b>world</b></p>");
        Document clean = cleaner.clean(dirty);
        assertEquals("<p>Hello <b>world</b></p>", clean.body().html());
    }

    // Tests cleaning a document containing unsafe tags which should be stripped
    @Test
    public void testClean_unsafeTags_stripsDisallowedTags() {
        Document dirty = Jsoup.parse("<p>Safe</p><script>alert('xss');</script>");
        Document clean = cleaner.clean(dirty);
        assertEquals("<p>Safe</p>", clean.body().html());
    }

    // Tests cleaning a document containing unsafe attributes
    @Test
    public void testClean_unsafeAttributes_stripsDisallowedAttributes() {
        Document dirty = Jsoup.parse("<p onclick=\"alert('xss')\" class=\"allowed\">Text</p>");
        Whitelist whitelist = Whitelist.basic().addAttributes("p", "class");
        Cleaner customCleaner = new Cleaner(whitelist);
        Document clean = customCleaner.clean(dirty);
        assertEquals("<p class=\"allowed\">Text</p>", clean.body().html());
    }

    // Tests that enforced attributes configured in whitelist are applied
    @Test
    public void testClean_enforcedAttributes_appliesEnforcedAttributes() {
        Whitelist whitelist = Whitelist.none()
                .addTags("a")
                .addAttributes("a", "href")
                .addEnforcedAttribute("a", "rel", "nofollow");
        Cleaner enforcedCleaner = new Cleaner(whitelist);

        Document dirty = Jsoup.parse("<a href=\"http://example.com\">Link</a>");
        Document clean = enforcedCleaner.clean(dirty);
        assertEquals("<a href=\"http://example.com\" rel=\"nofollow\">Link</a>", clean.body().html());
    }

    // Tests handling a frameset document where dirtyDocument.body() can be null
    @Test
    public void testClean_framesetDocument_handlesNullBodyGracefully() {
        Document dirty = Jsoup.parse("<frameset><frame src=\"frame.html\"></frameset>");
        Document clean = cleaner.clean(dirty);
        assertNotNull(clean.body());
        assertEquals("", clean.body().html());
    }

    // Tests preserving TextNodes and multiple text fragments
    @Test
    public void testClean_textNodes_preservesTextCorrectly() {
        Document dirty = Jsoup.parse("Some plain text before <p>paragraph</p> and after");
        Document clean = cleaner.clean(dirty);
        assertEquals("Some plain text before \n<p>paragraph</p> and after", clean.body().html());
    }

    // Tests nested safe elements structure
    @Test
    public void testClean_nestedSafeElements_preservesHierarchy() {
        Cleaner relaxedCleaner = new Cleaner(Whitelist.relaxed());
        Document dirty = Jsoup.parse("<div><ul><li><span>Item 1</span></li></ul></div>");
        Document clean = relaxedCleaner.clean(dirty);
        assertEquals("<div>\n <ul>\n  <li><span>Item 1</span></li>\n </ul>\n</div>", clean.body().html());
    }

    // Tests isValid returning true for completely valid document
    @Test
    public void testIsValid_validDocument_returnsTrue() {
        Document dirty = Jsoup.parse("<p>Hello <b>world</b></p>");
        assertTrue(cleaner.isValid(dirty));
    }

    // Tests isValid returning false when an unsafe tag is present in body
    @Test
    public void testIsValid_unsafeTagInBody_returnsFalse() {
        Document dirty = Jsoup.parse("<p>Hello</p><script>alert('xss')</script>");
        assertFalse(cleaner.isValid(dirty));
    }

    // Tests isValid returning false when an unsafe attribute is present
    @Test
    public void testIsValid_unsafeAttribute_returnsFalse() {
        Document dirty = Jsoup.parse("<a href=\"javascript:alert('xss')\">Link</a>");
        assertFalse(cleaner.isValid(dirty));
    }

    // Tests isValid returning false when tags or scripts are present in head (Defects4J 30 regression)
    @Test
    public void testIsValid_tagsInHead_returnsFalse() {
        Document dirty = Jsoup.parse("<html><head><script>alert('xss');</script></head><body><p>Hello</p></body></html>");
        assertFalse(cleaner.isValid(dirty));
    }

    // Tests isValid on empty document returning true
    @Test
    public void testIsValid_emptyDocument_returnsTrue() {
        Document dirty = Jsoup.parse("");
        assertTrue(cleaner.isValid(dirty));
    }
}