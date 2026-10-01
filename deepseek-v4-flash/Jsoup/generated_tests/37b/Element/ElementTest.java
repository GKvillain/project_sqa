package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;

import java.util.*;

public class ElementTest {

    // Tests constructor, tag name, tag, block status, id, baseUri
    @Test
    public void testBasics() {
        Element div = new Element(Tag.valueOf("div"), "http://example.com");
        assertEquals("div", div.tagName());
        assertEquals(Tag.valueOf("div"), div.tag());
        assertTrue(div.isBlock());
        assertEquals("", div.id());
        assertEquals("http://example.com", div.baseUri());

        // change tag name
        div.tagName("span");
        assertEquals("span", div.tagName());
        assertFalse(div.isBlock()); // span is inline
    }

    // Tests normal parent and children operations
    @Test
    public void testParentChildren() {
        Element ul = new Element(Tag.valueOf("ul"), "");
        Element li1 = new Element(Tag.valueOf("li"), "");
        Element li2 = new Element(Tag.valueOf("li"), "");
        ul.appendChild(li1);
        ul.appendChild(li2);

        assertEquals(ul, li1.parent());
        assertEquals(ul, li2.parent());
        Elements parents = li1.parents();
        assertTrue(parents.contains(ul));

        Elements children = ul.children();
        assertEquals(2, children.size());
        assertEquals(li1, children.get(0));
        assertEquals(li2, children.get(1));
        assertEquals(li1, ul.child(0));
        assertEquals(li2, ul.child(1));
    }

    // Tests child(index) out of bounds
    @Test(expected = IndexOutOfBoundsException.class)
    public void testChildIndexOutOfBounds() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.child(0); // no children
    }

    // Tests text nodes and data nodes
    @Test
    public void testTextNodesAndDataNodes() {
        Element p = new Element(Tag.valueOf("p"), "");
        p.appendChild(new TextNode("Hello", ""));
        p.appendChild(new TextNode(" World", ""));
        List<TextNode> textNodes = p.textNodes();
        assertEquals(2, textNodes.size());
        assertEquals("Hello", textNodes.get(0).getWholeText());
        assertEquals(" World", textNodes.get(1).getWholeText());

        Element script = new Element(Tag.valueOf("script"), "");
        script.appendChild(new DataNode("var x = 1;", ""));
        List<DataNode> dataNodes = script.dataNodes();
        assertEquals(1, dataNodes.size());
        assertEquals("var x = 1;", dataNodes.get(0).getWholeData());
    }

    // Tests appendChild, prependChild, appendElement, prependElement, appendText, prependText, append(String), prepend(String)
    @Test
    public void testAppendPrepend() {
        Element div = new Element(Tag.valueOf("div"), "");

        // appendChild and prependChild
        Element a = new Element(Tag.valueOf("a"), "");
        Element b = new Element(Tag.valueOf("b"), "");
        div.appendChild(a);
        div.prependChild(b);
        assertEquals(b, div.child(0));
        assertEquals(a, div.child(1));
        div.empty();

        // appendElement and prependElement
        Element p = div.appendElement("p");
        assertEquals(p, div.child(0));
        div.prependElement("span");
        assertEquals("span", div.child(0).tagName());
        div.empty();

        // appendText and prependText
        div.appendText("World");
        div.prependText("Hello ");
        assertEquals("Hello World", div.text());
        div.empty();

        // append HTML and prepend HTML
        div.append("<p>One</p><span>Two</span>");
        assertEquals(2, div.children().size());
        assertEquals("p", div.child(0).tagName());
        assertEquals("span", div.child(1).tagName());

        div.prepend("<b>Prepended</b>");
        assertEquals(3, div.children().size());
        assertEquals("b", div.child(0).tagName());
    }

    // Tests empty() method
    @Test
    public void testEmpty() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.appendChild(new TextNode("text", ""));
        div.appendChild(new Element(Tag.valueOf("p"), ""));
        assertEquals(2, div.childNodeSize());
        div.empty();
        assertEquals(0, div.childNodeSize());
        assertTrue(div.children().isEmpty());
    }

    // Tests text(), ownText(), hasText(), and text with <br> and block children, and preserveWhitespace
    @Test
    public void testText() {
        // basic text
        Element p = new Element(Tag.valueOf("p"), "");
        p.appendChild(new TextNode("Hello ", ""));
        Element b = new Element(Tag.valueOf("b"), "");
        b.appendChild(new TextNode("World", ""));
        p.appendChild(b);
        p.appendChild(new TextNode(" !", ""));
        assertEquals("Hello World !", p.text());

        // ownText
        assertEquals("Hello  !", p.ownText());

        // hasText true
        assertTrue(p.hasText());

        // hasText false
        Element empty = new Element(Tag.valueOf("p"), "");
        assertFalse(empty.hasText());

        // hasText with only element child
        Element parent = new Element(Tag.valueOf("div"), "");
        parent.appendChild(new Element(Tag.valueOf("span"), ""));
        assertFalse(parent.hasText());

        // text with <br>
        Element div = new Element(Tag.valueOf("div"), "");
        div.appendChild(new TextNode("Hello", ""));
        div.appendChild(new Element(Tag.valueOf("br"), ""));
        div.appendChild(new TextNode("World", ""));
        assertEquals("Hello World", div.text());
        assertEquals("Hello World", div.ownText()); // ownText also adds space for <br>

        // text with block children (p)
        Element container = new Element(Tag.valueOf("div"), "");
        Element p1 = new Element(Tag.valueOf("p"), "");
        p1.appendChild(new TextNode("First", ""));
        Element p2 = new Element(Tag.valueOf("p"), "");
        p2.appendChild(new TextNode("Second", ""));
        container.appendChild(p1);
        container.appendChild(p2);
        assertEquals("First Second", container.text());

        // preserveWhitespace in <pre>
        Element pre = new Element(Tag.valueOf("pre"), "");
        pre.appendChild(new TextNode("  spaced   text  ", ""));
        assertEquals("  spaced   text  ", pre.text());
    }

    // Tests html(), html(String), outerHtml for inline and self-closing elements
    @Test
    public void testHtml() {
        // inner html
        Element div = new Element(Tag.valueOf("div"), "");
        div.appendChild(new Element(Tag.valueOf("p"), ""));
        assertEquals("<p></p>", div.html());

        // html(String) replacement
        div.html("<span>replaced</span>");
        assertEquals(1, div.children().size());
        assertEquals("span", div.child(0).tagName());

        // outerHtml for inline element
        Element a = new Element(Tag.valueOf("a"), "");
        a.appendChild(new TextNode("link", ""));
        assertEquals("<a>link</a>", a.outerHtml());

        // outerHtml for self-closing element
        Element br = new Element(Tag.valueOf("br"), "");
        assertEquals("<br />", br.outerHtml());
    }

    // Tests class related methods: classNames, hasClass, addClass, removeClass, toggleClass
    @Test
    public void testClassMethods() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.attr("class", "one two three");
        Set<String> classNames = div.classNames();
        assertEquals(3, classNames.size());
        assertTrue(classNames.contains("one"));
        assertTrue(classNames.contains("two"));
        assertTrue(classNames.contains("three"));

        assertTrue(div.hasClass("one"));
        assertTrue(div.hasClass("TWO")); // case insensitive
        assertFalse(div.hasClass("four"));

        div.addClass("four");
        assertTrue(div.hasClass("four"));
        assertEquals("one two three four", div.className());

        div.removeClass("two");
        assertFalse(div.hasClass("two"));
        assertEquals("one three four", div.className());

        div.toggleClass("one");
        assertFalse(div.hasClass("one"));
        div.toggleClass("five");
        assertTrue(div.hasClass("five"));
    }

    // Tests getElementById
    @Test
    public void testGetElementById() {
        Element root = new Element(Tag.valueOf("div"), "");
        Element target = new Element(Tag.valueOf("p"), "");
        target.attr("id", "target");
        root.appendChild(target);
        assertEquals(target, root.getElementById("target"));
        assertNull(root.getElementById("nonexistent"));
    }

    // Tests getElementsByTag
    @Test
    public void testGetElementsByTag() {
        Element root = new Element(Tag.valueOf("div"), "");
        Element p1 = root.appendElement("p");
        Element p2 = root.appendElement("p");
        Element span = root.appendElement("span");
        Elements ps = root.getElementsByTag("p");
        assertEquals(2, ps.size());
        assertTrue(ps.contains(p1));
        assertTrue(ps.contains(p2));
    }

    // Tests sibling-related methods: siblingElements, nextElementSibling, previousElementSibling,
    // firstElementSibling, lastElementSibling, elementSiblingIndex
    @Test
    public void testSiblings() {
        Element ul = new Element(Tag.valueOf("ul"), "");
        Element li1 = ul.appendElement("li");
        Element li2 = ul.appendElement("li");
        Element li3 = ul.appendElement("li");

        assertEquals(li1, li2.previousElementSibling());
        assertEquals(li3, li2.nextElementSibling());
        assertNull(li1.previousElementSibling());
        assertNull(li3.nextElementSibling());

        assertEquals(li1, li2.firstElementSibling());
        assertEquals(li3, li2.lastElementSibling());

        assertEquals(Integer.valueOf(0), li1.elementSiblingIndex());
        assertEquals(Integer.valueOf(1), li2.elementSiblingIndex());
        assertEquals(Integer.valueOf(2), li3.elementSiblingIndex());

        Elements siblings = li2.siblingElements();
        assertEquals(2, siblings.size());
        assertTrue(siblings.contains(li1));
        assertTrue(siblings.contains(li3));
        assertFalse(siblings.contains(li2));
    }

    // Tests clone() method
    @Test
    public void testClone() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.attr("id", "original");
        TextNode text = new TextNode("text", "");
        div.appendChild(text);
        Element clone = div.clone();
        assertNotSame(div, clone);
        assertEquals("original", clone.id());
        assertEquals(div.html(), clone.html());

        // modification of clone should not affect original
        clone.attr("id", "cloned");
        assertEquals("original", div.id());
    }

    // Tests val() for input and textarea
    @Test
    public void testVal() {
        Element input = new Element(Tag.valueOf("input"), "");
        input.attr("value", "test");
        assertEquals("test", input.val());

        Element textarea = new Element(Tag.valueOf("textarea"), "");
        textarea.text("content");
        assertEquals("content", textarea.val());

        textarea.val("updated");
        assertEquals("updated", textarea.val());
    }

    // Tests insertChildren normal and invalid index
    @Test
    public void testInsertChildren() {
        Element div = new Element(Tag.valueOf("div"), "");
        Element a = new Element(Tag.valueOf("a"), "");
        Element b = new Element(Tag.valueOf("b"), "");
        Element c = new Element(Tag.valueOf("c"), "");
        div.appendChild(a);
        div.appendChild(c);
        // insert b at index 1
        div.insertChildren(1, Arrays.asList(b));
        assertEquals(a, div.child(0));
        assertEquals(b, div.child(1));
        assertEquals(c, div.child(2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInsertChildrenInvalidIndex() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.insertChildren(5, Collections.emptyList());
    }

    // Tests data() method
    @Test
    public void testData() {
        Element script = new Element(Tag.valueOf("script"), "");
        script.appendChild(new DataNode("code();", ""));
        assertEquals("code();", script.data());

        Element nested = new Element(Tag.valueOf("div"), "");
        Element innerScript = new Element(Tag.valueOf("script"), "");
        innerScript.appendChild(new DataNode("inner();", ""));
        nested.appendChild(innerScript);
        assertEquals("inner();", nested.data());
    }

    // Tests equals (reference) and hashCode (different objects)
    @Test
    public void testEqualsAndHashCode() {
        Element e1 = new Element(Tag.valueOf("div"), "");
        Element e2 = new Element(Tag.valueOf("div"), "");
        assertTrue(e1.equals(e1));
        assertFalse(e1.equals(e2));
        assertFalse(e1.hashCode() == e2.hashCode()); // typically different
    }
}