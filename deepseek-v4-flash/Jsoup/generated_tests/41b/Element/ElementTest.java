package org.jsoup.nodes;

import org.junit.Test;
import org.jsoup.parser.Tag;
import static org.junit.Assert.*;

public class ElementTest {

    // Tests constructor with tag and baseUri
    @Test
    public void testConstructor_tagAndBaseUri_createsElement() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        assertEquals("div", el.tagName());
    }

    // Tests tagName with new tag name
    @Test
    public void testTagName_newTagName_updatesTagName() {
        Element el = new Element(Tag.valueOf("span"), "http://example.com");
        el.tagName("div");
        assertEquals("div", el.tagName());
    }

    // Tests id with attribute set
    @Test
    public void testId_attributeSet_returnsId() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.attr("id", "myId");
        assertEquals("myId", el.id());
    }

    // Tests id without attribute
    @Test
    public void testId_noAttribute_returnsEmptyString() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        assertEquals("", el.id());
    }

    // Tests text() with nested text and elements
    @Test
    public void testText_nestedTextAndElements_returnsCombinedText() {
        Element el = new Element(Tag.valueOf("p"), "http://example.com");
        el.appendChild(new TextNode("Hello ", "http://example.com"));
        Element span = new Element(Tag.valueOf("span"), "http://example.com");
        span.appendChild(new TextNode("there", "http://example.com"));
        el.appendChild(span);
        el.appendChild(new TextNode(" now!", "http://example.com"));
        assertEquals("Hello there now!", el.text());
    }

    // Tests ownText() for direct text only
    @Test
    public void testOwnText_directTextOnly_returnsOwnText() {
        Element el = new Element(Tag.valueOf("p"), "http://example.com");
        el.appendChild(new TextNode("Hello ", "http://example.com"));
        Element span = new Element(Tag.valueOf("span"), "http://example.com");
        span.appendChild(new TextNode("there", "http://example.com"));
        el.appendChild(span);
        el.appendChild(new TextNode(" now!", "http://example.com"));
        assertEquals("Hello  now!", el.ownText());
    }

    // Tests hasText with non-blank text
    @Test
    public void testHasText_nonBlankText_returnsTrue() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.appendChild(new TextNode("hello", "http://example.com"));
        assertTrue(el.hasText());
    }

    // Tests hasText with blank text
    @Test
    public void testHasText_blankText_returnsFalse() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.appendChild(new TextNode("   ", "http://example.com"));
        assertFalse(el.hasText());
    }

    // Tests hasText with empty element
    @Test
    public void testHasText_emptyElement_returnsFalse() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        assertFalse(el.hasText());
    }

    // Tests appendChild adds child
    @Test
    public void testAppendChild_addChild_childAdded() {
        Element parent = new Element(Tag.valueOf("div"), "http://example.com");
        Element child = new Element(Tag.valueOf("span"), "http://example.com");
        parent.appendChild(child);
        assertEquals(1, parent.children().size());
        assertEquals(child, parent.child(0));
    }

    // Tests empty removes children
    @Test
    public void testEmpty_removesChildren_childrenCleared() {
        Element parent = new Element(Tag.valueOf("div"), "http://example.com");
        parent.appendChild(new Element(Tag.valueOf("span"), "http://example.com"));
        parent.empty();
        assertEquals(0, parent.children().size());
    }

    // Tests children() with mixed nodes
    @Test
    public void testChildren_mixedNodes_returnsOnlyElements() {
        Element parent = new Element(Tag.valueOf("div"), "http://example.com");
        parent.appendChild(new TextNode("text", "http://example.com"));
        Element child = new Element(Tag.valueOf("span"), "http://example.com");
        parent.appendChild(child);
        assertEquals(1, parent.children().size());
        assertEquals(child, parent.child(0));
    }

    // Tests textNodes()
    @Test
    public void testTextNodes_mixedNodes_returnsOnlyTextNodes() {
        Element parent = new Element(Tag.valueOf("div"), "http://example.com");
        TextNode tn = new TextNode("hello", "http://example.com");
        parent.appendChild(tn);
        parent.appendChild(new Element(Tag.valueOf("span"), "http://example.com"));
        assertEquals(1, parent.textNodes().size());
        assertEquals(tn, parent.textNodes().get(0));
    }

    // Tests classNames() with multiple classes
    @Test
    public void testClassNames_multipleClasses_returnsSet() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.attr("class", "header gray");
        assertEquals(2, el.classNames().size());
        assertTrue(el.classNames().contains("header"));
        assertTrue(el.classNames().contains("gray"));
    }

    // Tests hasClass with matching class
    @Test
    public void testHasClass_matchingClass_returnsTrue() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.attr("class", "header gray");
        assertTrue(el.hasClass("HEADER"));
        assertTrue(el.hasClass("gray"));
    }

    // Tests hasClass with non-matching class
    @Test
    public void testHasClass_nonMatchingClass_returnsFalse() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.attr("class", "header");
        assertFalse(el.hasClass("footer"));
    }

    // Tests val() for non-textarea element
    @Test
    public void testVal_nonTextarea_returnsValueAttribute() {
        Element el = new Element(Tag.valueOf("input"), "http://example.com");
        el.attr("value", "testVal");
        assertEquals("testVal", el.val());
    }

    // Tests val() for textarea element
    @Test
    public void testVal_textarea_returnsText() {
        Element el = new Element(Tag.valueOf("textarea"), "http://example.com");
        el.text("textarea content");
        assertEquals("textarea content", el.val());
    }

    // Tests cssSelector with id
    @Test
    public void testCssSelector_elementWithId_returnsIdSelector() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.attr("id", "myId");
        assertEquals("#myId", el.cssSelector());
    }

    // Tests text() with br element
    @Test
    public void testText_brElement_appendsSpace() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.appendChild(new TextNode("Hello", "http://example.com"));
        el.appendChild(new Element(Tag.valueOf("br"), "http://example.com"));
        el.appendChild(new TextNode("World", "http://example.com"));
        assertEquals("Hello World", el.text());
    }
}