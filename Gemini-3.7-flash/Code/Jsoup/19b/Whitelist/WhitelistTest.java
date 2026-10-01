package org.jsoup.safety;

import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class WhitelistTest {

    // Tests none() whitelist only allows no tags by default
    @Test
    public void testNone_defaultState_noTagsAllowed() {
        Whitelist wl = Whitelist.none();
        assertFalse(wl.isSafeTag("p"));
        assertFalse(wl.isSafeTag("b"));
        assertFalse(wl.isSafeTag("a"));
    }

    // Tests simpleText() whitelist allows formatting tags
    @Test
    public void testSimpleText_formattingTags_allowed() {
        Whitelist wl = Whitelist.simpleText();
        assertTrue(wl.isSafeTag("b"));
        assertTrue(wl.isSafeTag("em"));
        assertTrue(wl.isSafeTag("i"));
        assertTrue(wl.isSafeTag("strong"));
        assertTrue(wl.isSafeTag("u"));
        assertFalse(wl.isSafeTag("a"));
        assertFalse(wl.isSafeTag("p"));
    }

    // Tests basic() whitelist tag allowance and enforced attributes
    @Test
    public void testBasic_defaultConfiguration_expectedTagsAndEnforcedAttributes() {
        Whitelist wl = Whitelist.basic();
        assertTrue(wl.isSafeTag("a"));
        assertTrue(wl.isSafeTag("p"));
        assertTrue(wl.isSafeTag("blockquote"));
        assertFalse(wl.isSafeTag("img"));

        Attributes enforced = wl.getEnforcedAttributes("a");
        assertEquals("nofollow", enforced.get("rel"));
    }

    // Tests basicWithImages() allows img tag and attributes
    @Test
    public void testBasicWithImages_imgTag_allowed() {
        Whitelist wl = Whitelist.basicWithImages();
        assertTrue(wl.isSafeTag("a"));
        assertTrue(wl.isSafeTag("img"));

        Element el = new Element(Tag.valueOf("img"), "http://example.com/");
        Attribute attr = new Attribute("src", "http://example.com/test.jpg");
        el.attributes().put(attr);

        assertTrue(wl.isSafeAttribute("img", el, attr));
    }

    // Tests relaxed() whitelist allows structure tags and attributes
    @Test
    public void testRelaxed_structuralTags_allowed() {
        Whitelist wl = Whitelist.relaxed();
        assertTrue(wl.isSafeTag("table"));
        assertTrue(wl.isSafeTag("div"));
        assertTrue(wl.isSafeTag("h1"));

        Element el = new Element(Tag.valueOf("a"), "http://example.com/");
        Attribute attr = new Attribute("href", "http://example.com/index.html");
        el.attributes().put(attr);

        assertTrue(wl.isSafeAttribute("a", el, attr));
    }

    // Tests addTags adds allowed tags correctly
    @Test
    public void testAddTags_validTags_tagsBecomeSafe() {
        Whitelist wl = new Whitelist();
        assertFalse(wl.isSafeTag("custom"));
        wl.addTags("custom", "article");
        assertTrue(wl.isSafeTag("custom"));
        assertTrue(wl.isSafeTag("article"));
    }

    // Tests addTags with null input throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testAddTags_nullTags_throwsException() {
        Whitelist wl = new Whitelist();
        wl.addTags((String[]) null);
    }

    // Tests addTags with empty string tag throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testAddTags_emptyTagName_throwsException() {
        Whitelist wl = new Whitelist();
        wl.addTags("");
    }

    // Tests addAttributes adds attributes to specific tag
    @Test
    public void testAddAttributes_specificTag_attributesAllowed() {
        Whitelist wl = new Whitelist();
        wl.addTags("a");
        wl.addAttributes("a", "href", "title");

        Element el = new Element(Tag.valueOf("a"), "http://example.com/");
        Attribute attrHref = new Attribute("href", "http://example.com/");
        Attribute attrClass = new Attribute("class", "link");
        el.attributes().put(attrHref);
        el.attributes().put(attrClass);

        assertTrue(wl.isSafeAttribute("a", el, attrHref));
        assertFalse(wl.isSafeAttribute("a", el, attrClass));
    }

    // Tests addAttributes with :all pseudo tag applies to any tag
    @Test
    public void testAddAttributes_allPseudoTag_appliesToAllTags() {
        Whitelist wl = new Whitelist();
        wl.addTags("p", "div");
        wl.addAttributes(":all", "class");

        Element elP = new Element(Tag.valueOf("p"), "");
        Attribute attrClass = new Attribute("class", "my-class");
        elP.attributes().put(attrClass);

        assertTrue(wl.isSafeAttribute("p", elP, attrClass));
        assertFalse(wl.isSafeAttribute("p", elP, new Attribute("id", "main")));
    }

    // Tests addEnforcedAttribute enforces attribute value on element
    @Test
    public void testAddEnforcedAttribute_customTag_enforcedCorrectly() {
        Whitelist wl = new Whitelist();
        wl.addTags("a");
        wl.addEnforcedAttribute("a", "target", "_blank");

        Attributes attrs = wl.getEnforcedAttributes("a");
        assertEquals("_blank", attrs.get("target"));
        assertEquals(1, attrs.size());

        Attributes unconfiguredAttrs = wl.getEnforcedAttributes("div");
        assertNotNull(unconfiguredAttrs);
        assertEquals(0, unconfiguredAttrs.size());
    }

    // Tests addProtocols restricts allowed protocols for an attribute
    @Test
    public void testAddProtocols_matchingProtocol_isSafe() {
        Whitelist wl = new Whitelist();
        wl.addTags("a");
        wl.addAttributes("a", "href");
        wl.addProtocols("a", "href", "http", "https");

        Element el = new Element(Tag.valueOf("a"), "http://example.com/");
        Attribute validHttp = new Attribute("href", "http://example.com/test");
        el.attributes().put(validHttp);
        assertTrue(wl.isSafeAttribute("a", el, validHttp));

        Attribute invalidFtp = new Attribute("href", "ftp://example.com/test");
        el.attributes().put(invalidFtp);
        assertFalse(wl.isSafeAttribute("a", el, invalidFtp));

        Attribute javascript = new Attribute("href", "javascript:alert(1)");
        el.attributes().put(javascript);
        assertFalse(wl.isSafeAttribute("a", el, javascript));
    }

    // Tests preserveRelativeLinks set to true preserves relative URL
    @Test
    public void testPreserveRelativeLinks_true_preservesRelativeUrl() {
        Whitelist wl = new Whitelist();
        wl.addTags("a");
        wl.addAttributes("a", "href");
        wl.addProtocols("a", "href", "http", "https");
        wl.preserveRelativeLinks(true);

        Element el = new Element(Tag.valueOf("a"), "http://example.com/path/");
        Attribute attr = new Attribute("href", "sub/page.html");
        el.attributes().put(attr);

        boolean safe = wl.isSafeAttribute("a", el, attr);
        assertTrue(safe);
        assertEquals("sub/page.html", attr.getValue());
    }

    // Tests preserveRelativeLinks false converts relative URL to absolute
    @Test
    public void testPreserveRelativeLinks_false_updatesToAbsoluteUrl() {
        Whitelist wl = new Whitelist();
        wl.addTags("a");
        wl.addAttributes("a", "href");
        wl.addProtocols("a", "href", "http", "https");
        wl.preserveRelativeLinks(false);

        Element el = new Element(Tag.valueOf("a"), "http://example.com/path/");
        Attribute attr = new Attribute("href", "sub/page.html");
        el.attributes().put(attr);

        boolean safe = wl.isSafeAttribute("a", el, attr);
        assertTrue(safe);
        assertEquals("http://example.com/path/sub/page.html", attr.getValue());
    }

    // Tests protocol check when baseUri is empty and relative URL cannot resolve protocol
    @Test
    public void testAddProtocols_relativeUrlWithoutBaseUri_returnsFalse() {
        Whitelist wl = new Whitelist();
        wl.addTags("a");
        wl.addAttributes("a", "href");
        wl.addProtocols("a", "href", "http", "https");

        Element el = new Element(Tag.valueOf("a"), "");
        Attribute attr = new Attribute("href", "page.html");
        el.attributes().put(attr);

        assertFalse(wl.isSafeAttribute("a", el, attr));
    }

    // Tests addAttributes validation on empty tag throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testAddAttributes_emptyTag_throwsException() {
        Whitelist wl = new Whitelist();
        wl.addAttributes("", "href");
    }

    // Tests addProtocols validation on empty protocol throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testAddProtocols_emptyProtocol_throwsException() {
        Whitelist wl = new Whitelist();
        wl.addProtocols("a", "href", "");
    }

    // Tests addEnforcedAttribute validation on empty value throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testAddEnforcedAttribute_emptyValue_throwsException() {
        Whitelist wl = new Whitelist();
        wl.addEnforcedAttribute("a", "rel", "");
    }
}