package org.jsoup.safety;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CleanerTest {
    private Whitelist basicWhitelist;
    private Cleaner cleaner;

    @Before
    public void setUp() {
        basicWhitelist = Whitelist.basic();
        cleaner = new Cleaner(basicWhitelist);
    }

    // Tests constructor with null whitelist throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullWhitelist_throwsException() {
        new Cleaner(null);
    }

    // Tests clean method with null document throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testClean_nullDocument_throwsException() {
        cleaner.clean(null);
    }

    // Tests isValid method with null document throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testIsValid_nullDocument_throwsException() {
        cleaner.isValid(null);
    }

    // Tests clean method with safe HTML tags and text content
    @Test
    public void testClean_safeHtml_preservesSafeElements() {
        String html = "<p><a href=\"http://example.com/\">Link</a></p>";
        Document dirty = Jsoup.parse(html);
        Document clean = cleaner.clean(dirty);

        assertEquals("<p><a href=\"http://example.com/\" rel=\"nofollow\">Link</a></p>", clean.body().html());
    }

    // Tests clean method removing unsafe script tags and dangerous attributes
    @Test
    public void testClean_unsafeElementsAndAttributes_removesDisallowed() {
        String html = "<p onclick=\"steal()\">Hello <script>alert(1);</script><b onclick=\"evil()\">world</b></p>";
        Document dirty = Jsoup.parse(html);
        Document clean = cleaner.clean(dirty);

        assertEquals("<p>Hello <b>world</b></p>", clean.body().html());
    }

    // Tests clean method with enforced attributes added by whitelist
    @Test
    public void testClean_whitelistWithEnforcedAttributes_addsAttributes() {
        Whitelist whitelist = Whitelist.none().addTags("a").addAttributes("a", "href").addEnforcedAttribute("a", "rel", "nofollow");
        Cleaner customCleaner = new Cleaner(whitelist);
        Document dirty = Jsoup.parse("<a href=\"http://example.com\">Link</a>");
        Document clean = customCleaner.clean(dirty);

        assertEquals("<a href=\"http://example.com\" rel=\"nofollow\">Link</a>", clean.body().html());
    }

    // Tests clean method handling text nodes correctly
    @Test
    public void testClean_plainTextNodes_preservesTextNodes() {
        Document dirty = Jsoup.parse("Plain text without html");
        Document clean = cleaner.clean(dirty);

        assertEquals("Plain text without html", clean.body().html());
    }

    // Tests clean method with DataNode inside a safe tag
    @Test
    public void testClean_dataNodeInSafeTag_preservesDataNode() {
        Whitelist whitelist = Whitelist.relaxed().addTags("script");
        Cleaner customCleaner = new Cleaner(whitelist);

        Document dirty = Document.createShell("");
        Element script = dirty.body().appendElement("script");
        script.appendChild(new DataNode("var x = 1;", ""));

        Document clean = customCleaner.clean(dirty);
        assertTrue(clean.body().html().contains("var x = 1;"));
    }

    // Tests clean method handling comments (should be discarded)
    @Test
    public void testClean_htmlWithComments_discardsComments() {
        String html = "<p>Text<!-- comment --></p>";
        Document dirty = Jsoup.parse(html);
        Document clean = cleaner.clean(dirty);

        assertEquals("<p>Text</p>", clean.body().html());
    }

    // Tests clean method with frameset document (body is null)
    @Test
    public void testClean_documentWithoutBody_returnsEmptyBody() {
        Document framesetDoc = Document.createShell("");
        framesetDoc.body().remove(); // remove body to simulate frameset

        Document clean = cleaner.clean(framesetDoc);
        assertNotNull(clean.body());
        assertEquals("", clean.body().html());
    }

    // Tests isValid returning true for completely safe document body
    @Test
    public void testIsValid_safeDocument_returnsTrue() {
        Document dirty = Jsoup.parse("<p><a href=\"http://example.com/\" rel=\"nofollow\">Safe link</a></p>");
        boolean valid = cleaner.isValid(dirty);

        assertTrue(valid);
    }

    // Tests isValid returning false when unsafe elements exist
    @Test
    public void testIsValid_unsafeTags_returnsFalse() {
        Document dirty = Jsoup.parse("<p>Text <script>alert(1);</script></p>");
        boolean valid = cleaner.isValid(dirty);

        assertFalse(valid);
    }

    // Tests isValid returning false when unsafe attributes exist
    @Test
    public void testIsValid_unsafeAttributes_returnsFalse() {
        Document dirty = Jsoup.parse("<p onclick=\"exploit()\">Click me</p>");
        boolean valid = cleaner.isValid(dirty);

        assertFalse(valid);
    }

    // Tests isValid returning false when comments exist in body
    @Test
    public void testIsValid_documentWithComments_returnsFalse() {
        Document dirty = Jsoup.parse("<p>Hello <!-- comment --></p>");
        boolean valid = cleaner.isValid(dirty);

        assertFalse(valid);
    }

    // Tests isValid returning false when head contains content (Defects4J bug 58)
    @Test
    public void testIsValid_documentWithHeadContent_returnsFalse() {
        Document dirty = Jsoup.parse("<head><script src=\"evil.js\"></script></head><body><p>Hello</p></body>");
        boolean valid = cleaner.isValid(dirty);

        assertFalse(valid);
    }

    // Tests isValid returning false when custom invalid tag is present in nested structure
    @Test
    public void testIsValid_nestedUnsafeTags_returnsFalse() {
        Document dirty = Jsoup.parse("<div><blockquote><unknown>Bad</unknown></blockquote></div>");
        boolean valid = cleaner.isValid(dirty);

        assertFalse(valid);
    }

    // Tests clean method preserving base URI
    @Test
    public void testClean_documentWithBaseUri_preservesBaseUri() {
        Document dirty = Document.createShell("http://example.com/base/");
        dirty.body().appendElement("p").text("Hello");

        Document clean = cleaner.clean(dirty);
        assertEquals("http://example.com/base/", clean.baseUri());
    }

    // Tests isValidBodyHtml with safe body HTML string
    @Test
    public void testIsValidBodyHtml_safeHtml_returnsTrue() {
        assertTrue(cleaner.isValidBodyHtml("<p><a href=\"http://example.com/\" rel=\"nofollow\">Safe link</a></p>"));
        assertTrue(cleaner.isValidBodyHtml("Plain text"));
    }

    // Tests isValidBodyHtml with unsafe HTML content
    @Test
    public void testIsValidBodyHtml_unsafeHtml_returnsFalse() {
        assertFalse(cleaner.isValidBodyHtml("<script>alert(1);</script>"));
        assertFalse(cleaner.isValidBodyHtml("<p onclick=\"exploit()\">Click me</p>"));
    }

    // Tests isValidBodyHtml with null throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testIsValidBodyHtml_nullBodyHtml_throwsException() {
        cleaner.isValidBodyHtml(null);
    }

    // Tests isValid on document without body returns true when head is empty
    @Test
    public void testIsValid_documentWithoutBody_returnsTrue() {
        Document framesetDoc = Document.createShell("");
        framesetDoc.body().remove();
        assertTrue(cleaner.isValid(framesetDoc));
    }

    // Tests clean method unwraps safe elements inside an unsafe container tag
    @Test
    public void testClean_unsafeContainerWithSafeChildren_unwrapsContent() {
        Document dirty = Jsoup.parse("<custom-tag><p>Safe paragraph</p></custom-tag>");
        Document clean = cleaner.clean(dirty);

        assertEquals("<p>Safe paragraph</p>", clean.body().html());
    }

    // Tests isValid returning false when enforced attribute does not match whitelist value
    @Test
    public void testIsValid_enforcedAttributeMismatch_returnsFalse() {
        Whitelist whitelist = Whitelist.none().addTags("a").addAttributes("a", "href").addEnforcedAttribute("a", "rel", "nofollow");
        Cleaner customCleaner = new Cleaner(whitelist);

        Document docWithWrongRel = Jsoup.parse("<a href=\"http://example.com\" rel=\"dofollow\">Link</a>");
        assertFalse(customCleaner.isValid(docWithWrongRel));

        Document docWithoutRel = Jsoup.parse("<a href=\"http://example.com\">Link</a>");
        assertFalse(customCleaner.isValid(docWithoutRel));
    }
}