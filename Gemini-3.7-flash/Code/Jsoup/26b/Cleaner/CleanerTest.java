package org.jsoup.safety;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CleanerTest {
    private Whitelist whitelist;
    private Cleaner cleaner;

    @Before
    public void setUp() {
        whitelist = Whitelist.basic();
        cleaner = new Cleaner(whitelist);
    }

    // Tests null whitelist in constructor throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullWhitelist_throwsException() {
        new Cleaner(null);
    }

    // Tests null document in clean method throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testClean_nullDirtyDocument_throwsException() {
        cleaner.clean(null);
    }

    // Tests null document in isValid method throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testIsValid_nullDirtyDocument_throwsException() {
        cleaner.isValid(null);
    }

    // Tests cleaning valid basic HTML elements
    @Test
    public void testClean_validHtml_returnsSameStructure() {
        Document dirty = Jsoup.parse("<p><a href=\"http://example.com/\">Link</a></p>");
        Document clean = cleaner.clean(dirty);

        assertEquals("<p><a href=\"http://example.com/\" rel=\"nofollow\">Link</a></p>", clean.body().html());
    }

    // Tests cleaning unsafe tags while preserving child safe tags and text
    @Test
    public void testClean_unsafeTags_stripsUnsafeTagsPreservesContent() {
        Document dirty = Jsoup.parse("<script>alert('xss');</script><p>Safe <b>text</b></p>");
        Document clean = cleaner.clean(dirty);

        assertEquals("alert('xss');<p>Safe <b>text</b></p>", clean.body().html());
    }

    // Tests cleaning unsafe attributes on safe tags
    @Test
    public void testClean_unsafeAttributes_removesUnsafeAttributes() {
        Document dirty = Jsoup.parse("<p onclick=\"doEvil()\" class=\"test\">Hello</p>");
        Document clean = cleaner.clean(dirty);

        assertEquals("<p>Hello</p>", clean.body().html());
    }

    // Tests enforced attributes are added by cleaner
    @Test
    public void testClean_enforcedAttributes_addedToTargetElements() {
        Whitelist customWhitelist = Whitelist.none()
                .addTags("a")
                .addAttributes("a", "href")
                .addEnforcedAttribute("a", "rel", "nofollow");
        Cleaner customCleaner = new Cleaner(customWhitelist);

        Document dirty = Jsoup.parse("<a href=\"http://example.com/\">Test</a>");
        Document clean = customCleaner.clean(dirty);

        assertEquals("<a href=\"http://example.com/\" rel=\"nofollow\">Test</a>", clean.body().html());
    }

    // Tests handling of plain text nodes and multiple sibling elements
    @Test
    public void testClean_mixedTextAndElements_preservesOrderAndText() {
        Document dirty = Jsoup.parse("Leading text <p>Paragraph <em>italic</em></p> Trailing text");
        Document clean = cleaner.clean(dirty);

        assertEquals("Leading text <p>Paragraph <em>italic</em></p> Trailing text", clean.body().html());
    }

    // Tests discarding comments and non-element/non-text nodes
    @Test
    public void testClean_htmlComments_removesComments() {
        Document dirty = Jsoup.parse("<p>Hello <!-- This is a comment --> World</p>");
        Document clean = cleaner.clean(dirty);

        assertEquals("<p>Hello  World</p>", clean.body().html());
    }

    // Tests isValid returns true when HTML complies completely with whitelist
    @Test
    public void testIsValid_validDocument_returnsTrue() {
        Document validDoc = Jsoup.parse("<p><a href=\"http://example.com/\" rel=\"nofollow\">Link</a></p>");
        assertTrue(cleaner.isValid(validDoc));
    }

    // Tests isValid returns false when unsafe tags are present
    @Test
    public void testIsValid_unsafeTagPresent_returnsFalse() {
        Document invalidDoc = Jsoup.parse("<div><script>alert(1);</script></div>");
        assertFalse(cleaner.isValid(invalidDoc));
    }

    // Tests isValid returns false when unsafe attribute is present
    @Test
    public void testIsValid_unsafeAttributePresent_returnsFalse() {
        Document invalidDoc = Jsoup.parse("<a href=\"http://example.com/\" onclick=\"stealCookies()\">Link</a>");
        assertFalse(cleaner.isValid(invalidDoc));
    }

    // Tests isValid returns true for empty document body
    @Test
    public void testIsValid_emptyDocument_returnsTrue() {
        Document emptyDoc = Jsoup.parse("");
        assertTrue(cleaner.isValid(emptyDoc));
    }

    // Tests clean on an empty document produces empty body
    @Test
    public void testClean_emptyDocument_producesEmptyBody() {
        Document emptyDoc = Jsoup.parse("");
        Document clean = cleaner.clean(emptyDoc);
        assertEquals("", clean.body().html());
    }

    // Tests deeply nested safe and unsafe elements
    @Test
    public void testClean_deeplyNestedElements_cleansRecursively() {
        Document dirty = Jsoup.parse("<blockquote><div><p><span><b>Deep text</b></span></p></div></blockquote>");
        Document clean = cleaner.clean(dirty);

        assertEquals("<blockquote><p><b>Deep text</b></p></blockquote>", clean.body().html());
    }

    // Tests document where body node is missing/null (Defects4J 26)
    @Test
    public void testClean_documentWithoutBody_cleansWithoutException() {
        Document docWithoutBody = Document.createShell("http://example.com/");
        Element body = docWithoutBody.body();
        if (body != null) {
            body.remove();
        }

        Document clean = cleaner.clean(docWithoutBody);
        assertNotNull(clean);
        assertNotNull(clean.body());
        assertEquals("", clean.body().html());
    }

    // Tests isValid on document where body node is missing/null (Defects4J 26)
    @Test
    public void testIsValid_documentWithoutBody_returnsTrueWithoutException() {
        Document docWithoutBody = Document.createShell("http://example.com/");
        Element body = docWithoutBody.body();
        if (body != null) {
            body.remove();
        }

        assertTrue(cleaner.isValid(docWithoutBody));
    }
}