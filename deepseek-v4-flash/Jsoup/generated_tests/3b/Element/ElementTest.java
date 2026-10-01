package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import org.jsoup.nodes.TextNode;

public class ElementTest {

    private Element createElement(String tagName) {
        return new Element(Tag.valueOf(tagName), "");
    }

    // Tests classNames when no class attribute is set - should return empty set
    @Test
    public void testClassNames_noClassAttribute_returnsEmptySet() {
        Element el = createElement("div");
        assertTrue("ClassNames should be empty", el.classNames().isEmpty());
    }

    // Tests hasClass with empty string when no class attribute
    @Test
    public void testHasClass_noClassAttribute_returnsFalseForEmptyString() {
        Element el = createElement("div");
        assertFalse("hasClass('') should be false", el.hasClass(""));
    }

    // Tests hasClass with an existing class
    @Test
    public void testHasClass_withExistingClass_returnsTrue() {
        Element el = createElement("div");
        el.attr("class", "test");
        assertTrue(el.hasClass("test"));
    }

    // Tests addClass adds a class and updates classNames
    @Test
    public void testAddClass_addsClass() {
        Element el = createElement("div");
        el.addClass("foo");
        assertTrue(el.hasClass("foo"));
        assertEquals(1, el.classNames().size());
    }

    // Tests removeClass removes an existing class
    @Test
    public void testRemoveClass_removesClass() {
        Element el = createElement("div");
        el.addClass("foo");
        el.removeClass("foo");
        assertFalse(el.hasClass("foo"));
        assertTrue(el.classNames().isEmpty());
    }

    // Tests toggleClass toggles class on and off
    @Test
    public void testToggleClass_toggleOnThenOff_correctState() {
        Element el = createElement("div");
        el.toggleClass("foo");
        assertTrue(el.hasClass("foo"));
        el.toggleClass("foo");
        assertFalse(el.hasClass("foo"));
    }

    // Tests text method with simple text node
    @Test
    public void testText_simpleText_returnsText() {
        Element el = createElement("p");
        el.appendChild(new TextNode("Hello", ""));
        assertEquals("Hello", el.text().trim());
    }

    // Tests text method with block element adding space
    @Test
    public void testText_blockElementAddsSpace() {
        Element div = createElement("div");
        div.appendChild(new TextNode("Hello", ""));
        Element p = createElement("p");
        p.appendChild(new TextNode("World", ""));
        div.appendChild(p);
        assertEquals("Hello World", div.text().trim());
    }

    // Tests children returns only Element children
    @Test
    public void testChildren_returnsChildElements() {
        Element parent = createElement("div");
        parent.appendChild(createElement("p"));
        parent.appendChild(createElement("span"));
        Elements children = parent.children();
        assertEquals(2, children.size());
    }

    // Tests child with valid index returns correct element
    @Test
    public void testChild_validIndex_returnsChild() {
        Element parent = createElement("div");
        Element child = createElement("p");
        parent.appendChild(child);
        assertSame(child, parent.child(0));
    }

    // Tests appendChild adds node as last child
    @Test
    public void testAppendChild_addsChild() {
        Element parent = createElement("div");
        Element child = createElement("p");
        parent.appendChild(child);
        assertEquals(1, parent.children().size());
        assertSame(child, parent.child(0));
    }

    // Tests prependChild adds node as first child
    @Test
    public void testPrependChild_addsChildAtBeginning() {
        Element parent = createElement("div");
        Element child1 = createElement("p");
        Element child2 = createElement("span");
        parent.appendChild(child1);
        parent.prependChild(child2);
        assertSame(child2, parent.child(0));
        assertSame(child1, parent.child(1));
    }

    // Tests append parses HTML and appends nodes
    @Test
    public void testAppend_parsedHtml_appendsChildren() {
        Element parent = createElement("div");
        parent.append("<p>Hello</p>");
        Elements children = parent.children();
        assertEquals(1, children.size());
        assertEquals("p", children.get(0).tagName());
    }

    // Tests empty removes all children
    @Test
    public void testEmpty_removesAllChildren() {
        Element parent = createElement("div");
        parent.appendChild(createElement("p"));
        parent.empty();
        assertEquals(0, parent.children().size());
    }

    // Tests id returns empty string when no id attribute
    @Test
    public void testId_noId_returnsEmpty() {
        Element el = createElement("div");
        assertEquals("", el.id());
    }

    // Tests id returns attribute value when id is set
    @Test
    public void testId_withId_returnsId() {
        Element el = createElement("div");
        el.attr("id", "myid");
        assertEquals("myid", el.id());
    }

    // Tests select returns matching elements
    @Test
    public void testSelect_returnsMatchingElements() {
        Element el = createElement("div");
        el.appendChild(createElement("p"));
        el.appendChild(createElement("span"));
        Elements result = el.select("p");
        assertEquals(1, result.size());
        assertEquals("p", result.get(0).tagName());
    }

    // Tests hasText returns true when text content exists
    @Test
    public void testHasText_withText_returnsTrue() {
        Element el = createElement("div");
        el.appendChild(new TextNode("text", ""));
        assertTrue(el.hasText());
    }

    // Tests hasText returns false when no text content
    @Test
    public void testHasText_noText_returnsFalse() {
        Element el = createElement("div");
        assertFalse(el.hasText());
    }
}