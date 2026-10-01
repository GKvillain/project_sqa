package org.jsoup.safety;

import org.junit.Test;
import static org.junit.Assert.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.TextNode;
import org.jsoup.nodes.Node;
import java.util.List;

import org.jsoup.helper.Validate;

public class CleanerTest {

    // Tests constructor with null whitelist, expects NullPointerException
    @Test(expected = NullPointerException.class)
    public void testConstructor_nullWhitelist_throwsNullPointerException() {
        new Cleaner(null);
    }

    // Tests constructor with valid whitelist, ensures no exception
    @Test
    public void testConstructor_validWhitelist_createsCleaner() {
        Whitelist whitelist = Whitelist.relaxed();
        Cleaner cleaner = new Cleaner(whitelist);
        assertNotNull(cleaner);
    }

    // Tests clean method with null document, expects NullPointerException
    @Test(expected = NullPointerException.class)
    public void testClean_nullDocument_throwsNullPointerException() {
        Whitelist whitelist = Whitelist.relaxed();
        Cleaner cleaner = new Cleaner(whitelist);
        cleaner.clean(null);
    }

    // Tests clean with a simple valid HTML fragment
    @Test
    public void testClean_simpleValidHtml_returnsCleanDocument() {
        Whitelist whitelist = Whitelist.relaxed();
        Cleaner cleaner = new Cleaner(whitelist);
        Document dirty = Jsoup.parse("<p>Hello</p>");
        Document clean = cleaner.clean(dirty);
        assertNotNull(clean);
        assertEquals("<p>Hello</p>", clean.body().html());
    }

    // Tests clean with tags not in whitelist (should be discarded)
    @Test
    public void testClean_unallowedTag_removesTag() {
        Whitelist whitelist = Whitelist.simpleText();
        Cleaner cleaner = new Cleaner(whitelist);
        Document dirty = Jsoup.parse("<script>alert('xss')</script>");
        Document clean = cleaner.clean(dirty);
        assertNotNull(clean);
        assertEquals("", clean.body().html());
    }

    // Tests clean with unallowed attribute on an allowed tag
    @Test
    public void testClean_unallowedAttribute_removesAttribute() {
        Whitelist whitelist = Whitelist.simpleText();
        Cleaner cleaner = new Cleaner(whitelist);
        Document dirty = Jsoup.parse("<a href='http://evil.com' onclick='alert(1)'>link</a>");
        Document clean = cleaner.clean(dirty);
        assertNotNull(clean);
        // href is allowed for simpleText? Actually simpleText only allows: b, em, i, strong, u
        // So <a> tag itself should be removed
        assertEquals("", clean.body().html());
    }

    // Tests clean with nested allowed tags
    @Test
    public void testClean_nestedAllowedTags_preservesStructure() {
        Whitelist whitelist = Whitelist.relaxed();
        Cleaner cleaner = new Cleaner(whitelist);
        Document dirty = Jsoup.parse("<div><p>text</p></div>");
        Document clean = cleaner.clean(dirty);
        assertNotNull(clean);
        assertEquals("<div>\n <p>text</p>\n</div>", clean.body().html());
    }

    // Tests isValid method with null document, expects NullPointerException
    @Test(expected = NullPointerException.class)
    public void testIsValid_nullDocument_throwsNullPointerException() {
        Whitelist whitelist = Whitelist.relaxed();
        Cleaner cleaner = new Cleaner(whitelist);
        cleaner.isValid(null);
    }

    // Tests isValid with a valid document, returns true
    @Test
    public void testIsValid_validDocument_returnsTrue() {
        Whitelist whitelist = Whitelist.relaxed();
        Cleaner cleaner = new Cleaner(whitelist);
        Document dirty = Jsoup.parse("<p>Hello</p>");
        assertTrue(cleaner.isValid(dirty));
    }

    // Tests isValid with an invalid document (contains script tag)
    @Test
    public void testIsValid_invalidDocument_returnsFalse() {
        Whitelist whitelist = Whitelist.simpleText();
        Cleaner cleaner = new Cleaner(whitelist);
        Document dirty = Jsoup.parse("<script>alert('xss')</script>");
        assertFalse(cleaner.isValid(dirty));
    }

    // Tests isValid with unallowed attribute on allowed tag
    @Test
    public void testIsValid_unallowedAttribute_returnsFalse() {
        Whitelist whitelist = Whitelist.relaxed();
        Cleaner cleaner = new Cleaner(whitelist);
        // relaxed whitelist allows <a> tag, but not onclick attribute
        Document dirty = Jsoup.parse("<a href='http://example.com' onclick='alert(1)'>link</a>");
        // The tag itself is safe, but the attribute is not? Actually relaxed allows "onclick"? 
        // According to jsoup source, relaxed whitelist does NOT allow event attributes.
        // So isValid should return false because unsafe attribute is discarded.
        // But we need to check the exact behavior: isValid returns false if any attribute is discarded.
        // The clean copy will have the attribute removed.
        assertFalse(cleaner.isValid(dirty));
    }

    // Tests copySafeNodes with empty source element (should return 0)
    @Test
    public void testCopySafeNodes_emptySource_returnsZero() throws Exception {
        Whitelist whitelist = Whitelist.relaxed();
        Cleaner cleaner = new Cleaner(whitelist);
        Document cleanShell = Document.createShell("");
        Element source = new Element(Tag.valueOf("div"), "");
        Element dest = cleanShell.body();
        java.lang.reflect.Method method = Cleaner.class.getDeclaredMethod("copySafeNodes", Element.class, Element.class);
        method.setAccessible(true);
        int result = (Integer) method.invoke(cleaner, source, dest);
        assertEquals(0, result);
    }

    // Tests copySafeNodes with a text node only
    @Test
    public void testCopySafeNodes_textNodeOnly_copiesText() throws Exception {
        Whitelist whitelist = Whitelist.relaxed();
        Cleaner cleaner = new Cleaner(whitelist);
        Document cleanShell = Document.createShell("");
        Element source = new Element(Tag.valueOf("div"), "");
        TextNode textNode = new TextNode("hello", "");
        source.appendChild(textNode);
        Element dest = cleanShell.body();
        java.lang.reflect.Method method = Cleaner.class.getDeclaredMethod("copySafeNodes", Element.class, Element.class);
        method.setAccessible(true);
        int result = (Integer) method.invoke(cleaner, source, dest);
        assertEquals(0, result);
        assertEquals("hello", dest.text());
    }

    // Tests copySafeNodes with unallowed tag having safe children
    @Test
    public void testCopySafeNodes_unallowedTagWithSafeChildren_recurses() throws Exception {
        Whitelist whitelist = Whitelist.simpleText();
        Cleaner cleaner = new Cleaner(whitelist);
        Document cleanShell = Document.createShell("");
        Element source = new Element(Tag.valueOf("div"), "");
        Element child = new Element(Tag.valueOf("b"), "");
        child.appendChild(new TextNode("bold", ""));
        source.appendChild(child);
        Element dest = cleanShell.body();
        java.lang.reflect.Method method = Cleaner.class.getDeclaredMethod("copySafeNodes", Element.class, Element.class);
        method.setAccessible(true);
        int result = (Integer) method.invoke(cleaner, source, dest);
        // <div> is not safe, but <b> is safe, so one discarded + its children safe
        assertEquals(1, result);
        // The <b> tag and its text should be copied into dest
        assertEquals("<b>bold</b>", dest.html());
    }

    // Tests createSafeElement with safe tag and safe attributes
    @Test
    public void testCreateSafeElement_safeTagAndAttributes_noDiscarded() throws Exception {
        Whitelist whitelist = Whitelist.relaxed();
        Cleaner cleaner = new Cleaner(whitelist);
        Element sourceEl = new Element(Tag.valueOf("a"), "");
        sourceEl.attr("href", "http://example.com");
        java.lang.reflect.Method method = Cleaner.class.getDeclaredMethod("createSafeElement", Element.class);
        method.setAccessible(true);
        Object meta = method.invoke(cleaner, sourceEl);
        Element el = (Element) meta.getClass().getDeclaredField("el").get(meta);
        int numDiscarded = (Integer) meta.getClass().getDeclaredField("numAttribsDiscarded").get(meta);
        assertEquals(0, numDiscarded);
        assertEquals("a", el.tagName());
        assertTrue(el.hasAttr("href"));
    }

    // Tests createSafeElement with unsafe attribute
    @Test
    public void testCreateSafeElement_unsafeAttribute_discardsAttribute() throws Exception {
        Whitelist whitelist = Whitelist.relaxed();
        Cleaner cleaner = new Cleaner(whitelist);
        Element sourceEl = new Element(Tag.valueOf("a"), "");
        sourceEl.attr("onclick", "alert(1)");
        java.lang.reflect.Method method = Cleaner.class.getDeclaredMethod("createSafeElement", Element.class);
        method.setAccessible(true);
        Object meta = method.invoke(cleaner, sourceEl);
        int numDiscarded = (Integer) meta.getClass().getDeclaredField("numAttribsDiscarded").get(meta);
        assertEquals(1, numDiscarded);
    }

    // Tests clean with comment nodes, should be ignored
    @Test
    public void testClean_commentNode_ignoresComment() {
        Whitelist whitelist = Whitelist.relaxed();
        Cleaner cleaner = new Cleaner(whitelist);
        Document dirty = Jsoup.parse("<div><!-- comment --><p>text</p></div>");
        Document clean = cleaner.clean(dirty);
        assertNotNull(clean);
        assertEquals("<div>\n <p>text</p>\n</div>", clean.body().html());
    }
}