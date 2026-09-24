package org.jsoup.safety;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CleanerTest {
    private Cleaner basicCleaner;
    private Cleaner simpleTextCleaner;
    private Cleaner relaxedCleaner;
    private Cleaner noneCleaner;

    @Before
    public void setUp() {
        basicCleaner = new Cleaner(Whitelist.basic());
        simpleTextCleaner = new Cleaner(Whitelist.simpleText());
        relaxedCleaner = new Cleaner(Whitelist.relaxed());
        noneCleaner = new Cleaner(Whitelist.none());
    }

    // Tests null Whitelist constructor argument throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullWhitelist_throwsException() {
        new Cleaner(null);
    }

    // Tests clean method with null Document throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testClean_nullDocument_throwsException() {
        basicCleaner.clean(null);
    }

    // Tests isValid method with null Document throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testIsValid_nullDocument_throwsException() {
        basicCleaner.isValid(null);
    }

    // Tests clean on safe tags and safe text
    @Test
    public void testClean_safeBasicHtml_preservesSafeContent() {
        Document dirty = Jsoup.parse("<p><a href=\"http://example.com/\">Link</a> <b>bold</b> <i>italic</i></p>");
        Document clean = basicCleaner.clean(dirty);

        assertEquals("<p><a href=\"http://example.com/\" rel=\"nofollow\">Link</a> <b>bold</b> <i>italic</i></p>", clean.body().html());
    }

    // Tests clean strips unsafe script and style tags
    @Test
    public void testClean_unsafeScriptAndStyle_removesUnsafeTags() {
        Document dirty = Jsoup.parse("<div><script>alert('xss');</script><style>body {color:red;}</style><p>Hello</p></div>");
        Document clean = basicCleaner.clean(dirty);

        assertEquals("<p>Hello</p>", clean.body().html());
    }

    // Tests clean strips disallowed attributes while preserving safe tags
    @Test
    public void testClean_disallowedAttributes_removesDisallowedAttributes() {
        Document dirty = Jsoup.parse("<p onclick=\"doEvil()\" style=\"color:red\" class=\"my-class\">Test</p>");
        Document clean = basicCleaner.clean(dirty);

        assertEquals("<p>Test</p>", clean.body().html());
    }

    // Tests clean with Whitelist.none removes all tags but keeps text
    @Test
    public void testClean_noneWhitelist_removesAllTagsRetainsText() {
        Document dirty = Jsoup.parse("<p>Hello <b>World</b> <a href=\"http://test.com\">Link</a></p>");
        Document clean = noneCleaner.clean(dirty);

        assertEquals("Hello World Link", clean.body().html());
    }

    // Tests clean preserves baseUri across clean document and safe nodes
    @Test
    public void testClean_withBaseUri_preservesBaseUri() {
        String baseUri = "http://example.com/path/";
        Document dirty = Jsoup.parse("<p><a href=\"test.html\">Test</a></p>", baseUri);
        Document clean = basicCleaner.clean(dirty);

        assertEquals(baseUri, clean.baseUri());
        assertEquals("http://example.com/path/test.html", clean.body().getElementsByTag("a").first().absUrl("href"));
    }

    // Tests clean handles document with frameset where body might be null
    @Test
    public void testClean_framesetDocument_returnsEmptyBody() {
        Document dirty = Jsoup.parse("<html><head><title>Frameset</title></head><frameset cols=\"50%,50%\"><frame src=\"frame1.html\"><frame src=\"frame2.html\"></frameset></html>");
        Document clean = basicCleaner.clean(dirty);

        assertNotNull(clean.body());
        assertEquals("", clean.body().html());
    }

    // Tests clean with enforced attributes adds required attributes
    @Test
    public void testClean_enforcedAttributes_addsEnforcedRel() {
        Document dirty = Jsoup.parse("<a href=\"http://example.com/\">Example</a>");
        Document clean = basicCleaner.clean(dirty);

        Element link = clean.body().getElementsByTag("a").first();
        assertNotNull(link);
        assertEquals("nofollow", link.attr("rel"));
    }

    // Tests isValid returns true for safe document matching whitelist
    @Test
    public void testIsValid_validBasicDocument_returnsTrue() {
        Document validDoc = Jsoup.parse("<p><a href=\"http://example.com/\" rel=\"nofollow\">Link</a></p>");
        assertTrue(basicCleaner.isValid(validDoc));
    }

    // Tests isValid returns true for simple text with simpleText whitelist
    @Test
    public void testIsValid_simpleText_returnsTrue() {
        Document validDoc = Jsoup.parse("<b>bold text</b> <i>italic text</i>");
        assertTrue(simpleTextCleaner.isValid(validDoc));
    }

    // Tests isValid returns false when unsafe tags are present
    @Test
    public void testIsValid_unsafeScriptTag_returnsFalse() {
        Document dirty = Jsoup.parse("<p>Hello <script>alert(1);</script></p>");
        assertFalse(basicCleaner.isValid(dirty));
    }

    // Tests isValid returns false when disallowed attribute is present
    @Test
    public void testIsValid_disallowedAttribute_returnsFalse() {
        Document dirty = Jsoup.parse("<p onclick=\"evil()\">Hello</p>");
        assertFalse(basicCleaner.isValid(dirty));
    }

    // Tests isValid returns false for invalid protocol in link
    @Test
    public void testIsValid_invalidProtocol_returnsFalse() {
        Document dirty = Jsoup.parse("<a href=\"javascript:alert('xss')\">Click</a>");
        assertFalse(basicCleaner.isValid(dirty));
    }

    // Tests isValid returns false when tags disallowed by simpleText are present
    @Test
    public void testIsValid_tagNotAllowedInSimpleText_returnsFalse() {
        Document dirty = Jsoup.parse("<p><a href=\"http://example.com/\">Link</a></p>");
        assertFalse(simpleTextCleaner.isValid(dirty));
    }

    // Tests isValid returns true on empty document
    @Test
    public void testIsValid_emptyDocument_returnsTrue() {
        Document emptyDoc = Jsoup.parse("");
        assertTrue(basicCleaner.isValid(emptyDoc));
    }

    // Tests nested safe elements cleaning
    @Test
    public void testClean_nestedSafeElements_copiesAllLevels() {
        Document dirty = Jsoup.parse("<blockquote><p>Quote <b>bold <i>italic</i></b></p></blockquote>");
        Document clean = relaxedCleaner.clean(dirty);

        assertEquals("<blockquote>\n <p>Quote <b>bold <i>italic</i></b></p>\n</blockquote>", clean.body().html());
    }

    // Tests clean handles pure text nodes correctly
    @Test
    public void testClean_plainTextNode_copiesTextNode() {
        Document dirty = Jsoup.parse("Just plain text without tags");
        Document clean = basicCleaner.clean(dirty);

        assertEquals("Just plain text without tags", clean.body().html());
        assertEquals(1, clean.body().childNodes().size());
        assertTrue(clean.body().childNodes().get(0) instanceof TextNode);
    }
}