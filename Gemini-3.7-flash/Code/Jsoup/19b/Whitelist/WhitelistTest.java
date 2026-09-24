package org.jsoup.safety;

import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.junit.Test;

import static org.junit.Assert.*;

public class WhitelistTest {

    // Tests none whitelist allows no tags
    @Test
    public void testNone_createsEmptyWhitelist_noTagsAllowed() {
        Whitelist wl = Whitelist.none();
        assertFalse(wl.isSafeTag("p"));
        assertFalse(wl.isSafeTag("a"));
    }

    // Tests simpleText whitelist allowed and disallowed tags
    @Test
    public void testSimpleText_defaultTags_allowsOnlySimpleFormatting() {
        Whitelist wl = Whitelist.simpleText();
        assertTrue(wl.isSafeTag("b"));
        assertTrue(wl.isSafeTag("em"));
        assertTrue(wl.isSafeTag("i"));
        assertTrue(wl.isSafeTag("strong"));
        assertTrue(wl.isSafeTag("u"));
        assertFalse(wl.isSafeTag("p"));
        assertFalse(wl.isSafeTag("a"));
    }

    // Tests basic whitelist tags, attributes, and enforced attributes
    @Test
    public void testBasic_defaultConfiguration_validatesTagsAndEnforcedAttributes() {
        Whitelist wl = Whitelist.basic();
        assertTrue(wl.isSafeTag("a"));
        assertTrue(wl.isSafeTag("blockquote"));
        assertFalse(wl.isSafeTag("img"));

        Attributes enforced = wl.getEnforcedAttributes("a");
        assertEquals("nofollow", enforced.get("rel"));

        Attributes noEnforced = wl.getEnforcedAttributes("blockquote");
        assertEquals(0, noEnforced.size());
    }

    // Tests basicWithImages whitelist allows img tag
    @Test
    public void testBasicWithImages_defaultConfiguration_allowsImgTag() {
        Whitelist wl = Whitelist.basicWithImages();
        assertTrue(wl.isSafeTag("a"));
        assertTrue(wl.isSafeTag("img"));
    }

    // Tests relaxed whitelist allows structural tags and attributes
    @Test
    public void testRelaxed_defaultConfiguration_allowsTablesAndDivs() {
        Whitelist wl = Whitelist.relaxed();
        assertTrue(wl.isSafeTag("table"));
        assertTrue(wl.isSafeTag("div"));
        assertTrue(wl.isSafeTag("h1"));
        assertEquals(0, wl.getEnforcedAttributes("a").size());
    }

    // Tests addTags adds allowed tags correctly
    @Test
    public void testAddTags_customTags_allowsAddedTags() {
        Whitelist wl = new Whitelist();
        wl.addTags("custom", "article");
        assertTrue(wl.isSafeTag("custom"));
        assertTrue(wl.isSafeTag("article"));
        assertFalse(wl.isSafeTag("div"));
    }

    // Tests addAttributes on specific tag and pseudo tag :all
    @Test
    public void testIsSafeAttribute_specificTagAndAllPseudoTag_returnsTrue() {
        Whitelist wl = new Whitelist();
        wl.addTags("p", "div");
        wl.addAttributes("p", "class");
        wl.addAttributes(":all", "id");

        Element pEl = new Element(Tag.valueOf("p"), "");
        pEl.attr("class", "my-class");
        Attribute classAttr = new Attribute("class", "my-class");
        assertTrue(wl.isSafeAttribute("p", pEl, classAttr));

        Element divEl = new Element(Tag.valueOf("div"), "");
        divEl.attr("id", "my-id");
        Attribute idAttr = new Attribute("id", "my-id");
        assertTrue(wl.isSafeAttribute("div", divEl, idAttr));

        Attribute styleAttr = new Attribute("style", "color:red");
        assertFalse(wl.isSafeAttribute("p", pEl, styleAttr));
        assertFalse(wl.isSafeAttribute("div", divEl, styleAttr));
    }

    // Tests addEnforcedAttribute sets and overrides enforced attribute values
    @Test
    public void testAddEnforcedAttribute_customEnforcedAttribute_setsExpectedAttributes() {
        Whitelist wl = new Whitelist();
        wl.addEnforcedAttribute("a", "target", "_blank");
        wl.addEnforcedAttribute("a", "target", "_self");

        Attributes attrs = wl.getEnforcedAttributes("a");
        assertEquals("_self", attrs.get("target"));
    }

    // Tests valid protocol matching and attribute value update
    @Test
    public void testIsSafeAttribute_allowedProtocol_returnsTrueAndConvertsAbsolute() {
        Whitelist wl = new Whitelist();
        wl.addTags("a");
        wl.addAttributes("a", "href");
        wl.addProtocols("a", "href", "http", "https");

        Element el = new Element(Tag.valueOf("a"), "http://example.com");
        el.attr("href", "/index.html");
        Attribute attr = new Attribute("href", "/index.html");

        assertTrue(wl.isSafeAttribute("a", el, attr));
        assertEquals("http://example.com/index.html", attr.getValue());
    }

    // Tests disallowed protocol returns false
    @Test
    public void testIsSafeAttribute_disallowedProtocol_returnsFalse() {
        Whitelist wl = new Whitelist();
        wl.addTags("a");
        wl.addAttributes("a", "href");
        wl.addProtocols("a", "href", "http", "https");

        Element el = new Element(Tag.valueOf("a"), "http://example.com");
        el.attr("href", "javascript:alert(1)");
        Attribute attr = new Attribute("href", "javascript:alert(1)");

        assertFalse(wl.isSafeAttribute("a", el, attr));
    }

    // Tests preserveRelativeLinks option
    @Test
    public void testPreserveRelativeLinks_setPreserve_preservesOriginalAttributeValue() {
        Whitelist wl = new Whitelist();
        wl.addTags("a");
        wl.addAttributes("a", "href");
        wl.addProtocols("a", "href", "http", "https");
        wl.preserveRelativeLinks(true);

        Element el = new Element(Tag.valueOf("a"), "http://example.com");
        el.attr("href", "/about.html");
        Attribute attr = new Attribute("href", "/about.html");

        assertTrue(wl.isSafeAttribute("a", el, attr));
        assertEquals("/about.html", attr.getValue());
    }

    // Tests custom protocols on attributes
    @Test
    public void testAddProtocols_customProtocols_validatesCorrectly() {
        Whitelist wl = new Whitelist();
        wl.addTags("img");
        wl.addAttributes("img", "src");
        wl.addProtocols("img", "src", "data", "cid");

        Element el = new Element(Tag.valueOf("img"), "");
        el.attr("src", "data:image/png;base64,1234");
        Attribute attr = new Attribute("src", "data:image/png;base64,1234");

        assertTrue(wl.isSafeAttribute("img", el, attr));
    }

    // Tests validation on null or empty tag input
    @Test(expected = IllegalArgumentException.class)
    public void testAddTags_nullTags_throwsException() {
        new Whitelist().addTags((String[]) null);
    }

    // Tests validation on empty tag string
    @Test(expected = IllegalArgumentException.class)
    public void testAddTags_emptyTagName_throwsException() {
        new Whitelist().addTags("");
    }

    // Tests validation on empty attribute key in addAttributes
    @Test(expected = IllegalArgumentException.class)
    public void testAddAttributes_emptyKey_throwsException() {
        new Whitelist().addAttributes("a", "");
    }

    // Tests validation on empty protocol in addProtocols
    @Test(expected = IllegalArgumentException.class)
    public void testAddProtocols_emptyProtocol_throwsException() {
        new Whitelist().addProtocols("a", "href", "");
    }
}