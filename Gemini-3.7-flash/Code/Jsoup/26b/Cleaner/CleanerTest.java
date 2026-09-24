package org.jsoup.safety;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CleanerTest {
    private Cleaner cleaner;

    @Before
    public void setUp() {
        cleaner = new Cleaner(Whitelist.basic());
    }

    // Tests null whitelist in constructor
    @Test(expected = IllegalArgumentException.class)
    public void testCleaner_nullWhitelist_throwsException() {
        new Cleaner(null);
    }

    // Tests null dirty document in clean method
    @Test(expected = IllegalArgumentException.class)
    public void testClean_nullDocument_throwsException() {
        cleaner.clean(null);
    }

    // Tests null dirty document in isValid method
    @Test(expected = IllegalArgumentException.class)
    public void testIsValid_nullDocument_throwsException() {
        cleaner.isValid(null);
    }

    // Tests cleaning simple safe HTML content
    @Test
    public void testClean_safeContent_returnsCleanDocument() {
        Document dirty = Jsoup.parse("<p><a href=\"http://example.com/\" rel=\"nofollow\">Link</a></p>");
        Document clean = cleaner.clean(dirty);

        assertEquals("<p><a href=\"http://example.com/\" rel=\"nofollow\">Link</a></p>", clean.body().html());
    }

    // Tests cleaning unsafe tags by dropping them while keeping safe children
    @Test
    public void testClean_unsafeTags_removesUnsafeTags() {
        Document dirty = Jsoup.parse("<script>alert('xss');</script><p>Text</p><unknown>Inside</unknown>");
        Document clean = cleaner.clean(dirty);

        assertEquals("<p>Text</p>Inside", clean.body().html());
    }

    // Tests cleaning unsafe attributes from safe tags
    @Test
    public void testClean_unsafeAttributes_removesUnsafeAttributes() {
        Document dirty = Jsoup.parse("<p onclick=\"evil()\" class=\"normal\">Hello</p>");
        Document clean = cleaner.clean(dirty);

        assertEquals("<p>Hello</p>", clean.body().html());
    }

    // Tests cleaning document containing comments
    @Test
    public void testClean_comments_dropsComments() {
        Document dirty = Jsoup.parse("<p><!-- this is a comment -->Safe text</p>");
        Document clean = cleaner.clean(dirty);

        assertEquals("<p>Safe text</p>", clean.body().html());
    }

    // Tests cleaning with enforced attributes
    @Test
    public void testClean_enforcedAttributes_appliesEnforcedAttributes() {
        Document dirty = Jsoup.parse("<a href=\"http://example.com/\">Link</a>");
        Document clean = cleaner.clean(dirty);

        assertEquals("<a href=\"http://example.com/\" rel=\"nofollow\">Link</a>", clean.body().html());
    }

    // Tests preserving base URI on cleaned document
    @Test
    public void testClean_preservesBaseUri_setsBaseUriOnCleanDocument() {
        Document dirty = Jsoup.parse("<p><a href=\"test.html\">Link</a></p>", "http://example.com/");
        Document clean = cleaner.clean(dirty);

        assertEquals("http://example.com/", clean.baseUri());
        assertEquals("http://example.com/test.html", clean.body().getElementsByTag("a").first().absUrl("href"));
    }

    // Tests cleaning document with frameset (where body is null)
    @Test
    public void testClean_framesetDocumentWithoutBody_handlesNullBody() {
        Document dirty = Jsoup.parse("<html><head><script></script><noscript></noscript></head><frameset><frame src=\"foo\" /><frame src=\"foo\" /></frameset></html>");
        Document clean = cleaner.clean(dirty);

        assertNotNull(clean);
        assertEquals("", clean.body().html());
    }

    // Tests isValid returning true for safe content
    @Test
    public void testIsValid_safeHtml_returnsTrue() {
        Document valid = Jsoup.parse("<p><a href=\"http://example.com/\" rel=\"nofollow\">Link</a></p>");
        assertTrue(cleaner.isValid(valid));
    }

    // Tests isValid returning false when unsafe tag is present
    @Test
    public void testIsValid_unsafeTag_returnsFalse() {
        Document invalid = Jsoup.parse("<p>Safe</p><script>evil()</script>");
        assertFalse(cleaner.isValid(invalid));
    }

    // Tests isValid returning false when unsafe attribute is present
    @Test
    public void testIsValid_unsafeAttribute_returnsFalse() {
        Document invalid = Jsoup.parse("<p onclick=\"evil()\">Hello</p>");
        assertFalse(cleaner.isValid(invalid));
    }

    // Tests isValid returning false for document with frameset (null body)
    @Test
    public void testIsValid_framesetDocumentWithoutBody_returnsFalse() {
        Document dirty = Jsoup.parse("<html><head><script></script><noscript></noscript></head><frameset><frame src=\"foo\" /><frame src=\"foo\" /></frameset></html>");
        assertFalse(cleaner.isValid(dirty));
    }

    // Tests cleaning empty document
    @Test
    public void testClean_emptyDocument_returnsEmptyBody() {
        Document dirty = Jsoup.parse("");
        Document clean = cleaner.clean(dirty);

        assertEquals("", clean.body().html());
    }

    // Tests isValid on empty document
    @Test
    public void testIsValid_emptyDocument_returnsTrue() {
        Document valid = Jsoup.parse("");
        assertTrue(cleaner.isValid(valid));
    }
}