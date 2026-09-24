package org.jsoup.safety;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CleanerTest {

    private Whitelist whitelist;
    private Cleaner cleaner;

    @Before
    public void setUp() {
        whitelist = Whitelist.basic();
        cleaner = new Cleaner(whitelist);
    }

    // Tests constructor with null whitelist throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullWhitelist_throwsException() {
        new Cleaner(null);
    }

    // Tests clean method with null document throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testClean_nullDocument_throwsException() {
        cleaner.clean(null);
    }

    // Tests isValid method with null document throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testIsValid_nullDocument_throwsException() {
        cleaner.isValid(null);
    }

    // Tests cleaning a simple valid html fragment
    @Test
    public void testClean_validHtml_returnsCleanedDocument() {
        Document dirty = Jsoup.parse("<p><a href=\"http://example.com/\">Link</a></p>");
        Document clean = cleaner.clean(dirty);

        assertNotNull(clean);
        assertEquals("<p><a href=\"http://example.com/\" rel=\"nofollow\">Link</a></p>", clean.body().html());
    }

    // Tests cleaning drops unsafe tags such as script
    @Test
    public void testClean_unsafeScriptTag_removesUnsafeTag() {
        Document dirty = Jsoup.parse("<p>Text<script>alert('xss');</script></p>");
        Document clean = cleaner.clean(dirty);

        assertEquals("<p>Text</p>", clean.body().html());
    }

    // Tests cleaning drops unsafe attributes on safe tags
    @Test
    public void testClean_unsafeAttribute_removesAttribute() {
        Document dirty = Jsoup.parse("<p onclick=\"alert('xss')\">Safe text</p>");
        Document clean = cleaner.clean(dirty);

        assertEquals("<p>Safe text</p>", clean.body().html());
    }

    // Tests cleaning drops unsafe protocol in attributes
    @Test
    public void testClean_unsafeProtocol_removesAttribute() {
        Document dirty = Jsoup.parse("<a href=\"javascript:alert('xss')\">Click</a>");
        Document clean = cleaner.clean(dirty);

        assertEquals("<a rel=\"nofollow\">Click</a>", clean.body().html());
    }

    // Tests isValid returns true for valid content
    @Test
    public void testIsValid_validDocument_returnsTrue() {
        Document validDoc = Jsoup.parse("<p><a href=\"http://example.com/\" rel=\"nofollow\">Link</a></p>");
        assertTrue(cleaner.isValid(validDoc));
    }

    // Tests isValid returns false when unsafe tag is present
    @Test
    public void testIsValid_unsafeTag_returnsFalse() {
        Document dirty = Jsoup.parse("<script>alert('xss')</script><p>Text</p>");
        assertFalse(cleaner.isValid(dirty));
    }

    // Tests isValid returns false when unsafe attribute is present
    @Test
    public void testIsValid_unsafeAttribute_returnsFalse() {
        Document dirty = Jsoup.parse("<p style=\"color:red\">Text</p>");
        assertFalse(cleaner.isValid(dirty));
    }

    // Tests isValid returns false when document contains content in head
    @Test
    public void testIsValid_documentWithHeadContent_returnsFalse() {
        Document dirty = Jsoup.parse("<html><head><script>alert('xss')</script></head><body><p>Text</p></body></html>");
        assertFalse(cleaner.isValid(dirty));
    }

    // Tests cleaning document with comments discards comments
    @Test
    public void testClean_withComments_discardsComments() {
        Document dirty = Jsoup.parse("<p>Hello <!-- comment --> World</p>");
        Document clean = cleaner.clean(dirty);

        assertEquals("<p>Hello  World</p>", clean.body().html());
    }

    // Tests isValid returns false when body has comments (discarded nodes)
    @Test
    public void testIsValid_documentWithComments_returnsFalse() {
        Document dirty = Jsoup.parse("<p>Hello <!-- comment --> World</p>");
        assertFalse(cleaner.isValid(dirty));
    }

    // Tests cleaning with custom relaxed whitelist preserving formatting
    @Test
    public void testClean_relaxedWhitelist_preservesAllowedStructure() {
        Cleaner relaxedCleaner = new Cleaner(Whitelist.relaxed());
        Document dirty = Jsoup.parse("<div><h1>Title</h1><p>Body <b>bold</b></p></div>");
        Document clean = relaxedCleaner.clean(dirty);

        assertEquals("<div>\n <h1>Title</h1>\n <p>Body <b>bold</b></p>\n</div>", clean.body().html());
    }

    // Tests cleaning empty document returns empty body shell
    @Test
    public void testClean_emptyDocument_returnsEmptyBody() {
        Document dirty = Jsoup.parse("");
        Document clean = cleaner.clean(dirty);

        assertEquals("", clean.body().html());
    }
}