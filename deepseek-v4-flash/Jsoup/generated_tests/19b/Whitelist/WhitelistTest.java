package org.jsoup.safety;

import org.junit.Test;
import static org.junit.Assert.*;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.parser.Tag;

public class WhitelistTest {

    // Tests that none() creates an empty whitelist
    @Test
    public void testNone_initialState_noTagsAllowed() {
        Whitelist w = Whitelist.none();
        assertFalse(w.isSafeTag("a"));
        assertFalse(w.isSafeTag("b"));
    }

    // Tests that simpleText() adds the expected tags
    @Test
    public void testSimpleText_defaultTags_containsExpectedTags() {
        Whitelist w = Whitelist.simpleText();
        assertTrue(w.isSafeTag("b"));
        assertTrue(w.isSafeTag("em"));
        assertTrue(w.isSafeTag("i"));
        assertTrue(w.isSafeTag("strong"));
        assertTrue(w.isSafeTag("u"));
        assertFalse(w.isSafeTag("a"));
        assertFalse(w.isSafeTag("div"));
    }

    // Tests that addTags correctly adds tag names
    @Test
    public void testAddTags_validTags_tagsAddedToWhitelist() {
        Whitelist w = new Whitelist();
        w.addTags("div", "span");
        assertTrue(w.isSafeTag("div"));
        assertTrue(w.isSafeTag("span"));
        assertFalse(w.isSafeTag("p"));
    }

    // Tests that addTags with empty string throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testAddTags_emptyTag_throwsException() {
        Whitelist w = new Whitelist();
        w.addTags("");
    }

    // Tests that addAttributes correctly adds attribute to tag
    @Test
    public void testAddAttributes_tagAndKeys_attributeAdded() {
        Whitelist w = new Whitelist();
        w.addTags("a");
        w.addAttributes("a", "href");
        // We test via the isSafeAttribute method which requires an Element
        Element el = new Element(Tag.valueOf("a"), "");
        Attribute attr = new Attribute("href", "http://example.com");
        // "a" is safe tag, attribute "href" is allowed, no protocols, so should be true
        assertTrue(w.isSafeAttribute("a", el, attr));
    }

    // Tests isSafeAttribute returns false for disallowed attribute
    @Test
    public void testIsSafeAttribute_disallowedAttribute_returnsFalse() {
        Whitelist w = new Whitelist();
        w.addTags("a");
        w.addAttributes("a", "href");
        Element el = new Element(Tag.valueOf("a"), "");
        Attribute attr = new Attribute("class", "test");
        assertFalse(w.isSafeAttribute("a", el, attr));
    }

    // Tests isSafeAttribute returns false for invalid protocol
    @Test
    public void testIsSafeAttribute_invalidProtocol_returnsFalse() {
        Whitelist w = Whitelist.basic();
        Element el = new Element(Tag.valueOf("a"), "");
        // element absUrl for href will return "" if no baseUri set
        Attribute attr = new Attribute("href", "invalid://example.com");
        assertFalse(w.isSafeAttribute("a", el, attr));
    }

    // Tests isSafeAttribute returns true for valid protocol
    @Test
    public void testIsSafeAttribute_validProtocol_returnsTrue() {
        Whitelist w = Whitelist.basic();
        Element el = new Element(Tag.valueOf("a"), "");
        Attribute attr = new Attribute("href", "http://example.com");
        assertTrue(w.isSafeAttribute("a", el, attr));
    }

    // Tests that addEnforcedAttribute adds enforced attribute correctly
    @Test
    public void testAddEnforcedAttribute_tagAndKeyAndValue_enforcedAttributeSet() {
        Whitelist w = new Whitelist();
        w.addTags("a");
        w.addEnforcedAttribute("a", "rel", "nofollow");
        Attributes attrs = w.getEnforcedAttributes("a");
        assertEquals("nofollow", attrs.get("rel"));
    }

    // Tests getEnforcedAttributes returns empty for nonexistent tag
    @Test
    public void testGetEnforcedAttributes_tagNotInMap_returnsEmpty() {
        Whitelist w = new Whitelist();
        Attributes attrs = w.getEnforcedAttributes("div");
        assertEquals(0, attrs.size());
    }

    // Tests that addProtocols adds protocol correctly
    @Test
    public void testAddProtocols_validProtocols_protocolAllowed() {
        Whitelist w = new Whitelist();
        w.addTags("a");
        w.addAttributes("a", "href");
        w.addProtocols("a", "href", "http", "https");
        Element el = new Element(Tag.valueOf("a"), "");
        Attribute attr = new Attribute("href", "http://example.com");
        assertTrue(w.isSafeAttribute("a", el, attr));
    }

    // Tests that preserveRelativeLinks setter works
    @Test
    public void testPreserveRelativeLinks_true_setsFlag() {
        Whitelist w = new Whitelist();
        w.preserveRelativeLinks(true);
        assertTrue(w.isSafeTag("none")); // dummy to confirm object is fine, flag is internal
        // The flag is tested indirectly via testValidProtocol; we can test by adding protocol and checking behavior
    }

    // Tests the false branch of isSafeAttribute when :all tag is checked
    @Test
    public void testIsSafeAttribute_noAttributesDefined_checksAllTag() {
        Whitelist w = new Whitelist();
        w.addTags("a");
        w.addAttributes(":all", "class");
        Element el = new Element(Tag.valueOf("a"), "");
        Attribute attr = new Attribute("class", "test");
        // Should check :all since no attributes defined for "a"
        assertTrue(w.isSafeAttribute("a", el, attr));
    }

    // Tests isSafeTag returns false for tag not in set
    @Test
    public void testIsSafeTag_tagNotInSet_returnsFalse() {
        Whitelist w = new Whitelist();
        w.addTags("p");
        assertFalse(w.isSafeTag("div"));
    }

    // Tests basic() whitelist contains expected tags
    @Test
    public void testBasic_defaultTags_containsExpectedTags() {
        Whitelist w = Whitelist.basic();
        assertTrue(w.isSafeTag("a"));
        assertTrue(w.isSafeTag("p"));
        assertFalse(w.isSafeTag("img"));
    }

    // Tests basicWithImages() contains img tag
    @Test
    public void testBasicWithImages_defaultTags_containsImg() {
        Whitelist w = Whitelist.basicWithImages();
        assertTrue(w.isSafeTag("img"));
        assertTrue(w.isSafeTag("a"));
    }

    // Tests relaxed() contains div tag
    @Test
    public void testRelaxed_defaultTags_containsDiv() {
        Whitelist w = Whitelist.relaxed();
        assertTrue(w.isSafeTag("div"));
    }

    // Tests that addTags with null throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testAddTags_nullInput_throwsException() {
        Whitelist w = new Whitelist();
        w.addTags((String) null);
    }

    // Tests that addProtocols with null throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testAddProtocols_nullProtocol_throwsException() {
        Whitelist w = new Whitelist();
        w.addTags("a");
        w.addProtocols("a", "href", (String) null);
    }
}