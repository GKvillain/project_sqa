package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import java.util.List;

public class ElementTest {

    // Tests basic constructor with tag string
    @Test
    public void testConstructor_tagString_createsElement() {
        Element el = new Element("div");
        assertEquals("div", el.tagName());
    }

    // Tests changing tag name
    @Test
    public void testTagName_changeTag_returnsNewName() {
        Element el = new Element("span");
        el.tagName("div");
        assertEquals("div", el.tagName());
    }

    // Tests appending a child element
    @Test
    public void testAppendChild_addsChild() {
        Element parent = new Element("div");
        Element child = new Element("p");
        parent.appendChild(child);
        assertEquals(1, parent.childNodeSize());
        assertSame(child, parent.child(0));
    }

    // Tests prepending a child at the beginning
    @Test
    public void testPrependChild_addsFirstChild() {
        Element parent = new Element("div");
        Element child1 = new Element("p");
        Element child2 = new Element("span");
        parent.appendChild(child2);
        parent.prependChild(child1);
        assertSame(child1, parent.child(0));
        assertSame(child2, parent.child(1));
    }

    // Tests text() for combined text including nested elements
    @Test
    public void testText_returnsCombinedText() {
        Element el = new Element("p");
        el.appendText("Hello ");
        Element b = new Element("b");
        b.appendText("World");
        el.appendChild(b);
        el.appendText(" !");
        assertEquals("Hello World !", el.text());
    }

    // Tests ownText() returns only direct text children
    @Test
    public void testOwnText_returnsOwnTextOnly() {
        Element el = new Element("p");
        el.appendText("Hello ");
        Element b = new Element("b");
        b.appendText("World");
        el.appendChild(b);
        assertEquals("Hello", el.ownText().trim());
    }

    // Tests hasText true and false scenarios
    @Test
    public void testHasText_returnsCorrect() {
        Element el = new Element("p");
        el.appendText("text");
        assertTrue(el.hasText());
        el.empty();
        el.appendText("   ");
        assertFalse(el.hasText());
    }

    // Tests hasClass exact match and case insensitive
    @Test
    public void testHasClass_exactMatch() {
        Element el = new Element("div").attr("class", "myclass");
        assertTrue(el.hasClass("myclass"));
        assertTrue(el.hasClass("MYCLASS"));
        assertFalse(el.hasClass("other"));
        assertFalse(el.hasClass("myclas")); // different length
    }

    // Tests hasClass with multiple classes
    @Test
    public void testHasClass_partialMatch() {
        Element el = new Element("div").attr("class", "class1 class2 class3");
        assertTrue(el.hasClass("class2"));
        assertTrue(el.hasClass("class1"));
        assertTrue(el.hasClass("class3"));
        assertFalse(el.hasClass("class4"));
    }

    // Tests hasClass with empty class attribute
    @Test
    public void testHasClass_emptyAttribute() {
        Element el = new Element("div").attr("class", "");
        assertFalse(el.hasClass("any"));
    }

    // Tests hasClass with leading/trailing whitespace
    @Test
    public void testHasClass_leadingTrailingSpaces() {
        Element el = new Element("div").attr("class", "  class1  class2  ");
        assertTrue(el.hasClass("class1"));
        assertTrue(el.hasClass("class2"));
        assertFalse(el.hasClass("class3"));
    }

    // Tests hasClass with only whitespace
    @Test
    public void testHasClass_onlyWhitespace() {
        Element el = new Element("div").attr("class", "   ");
        assertFalse(el.hasClass("any"));
    }

    // Tests siblingElements excludes self
    @Test
    public void testSiblingElements_returnsSiblingsExcludingSelf() {
        Element parent = new Element("div");
        Element a = new Element("a");
        Element b = new Element("b");
        Element c = new Element("c");
        parent.appendChild(a);
        parent.appendChild(b);
        parent.appendChild(c);
        Elements siblings = b.siblingElements();
        assertEquals(2, siblings.size());
        assertTrue(siblings.contains(a));
        assertTrue(siblings.contains(c));
        assertFalse(siblings.contains(b));
    }

    // Tests elementSiblingIndex correct position
    @Test
    public void testElementSiblingIndex_returnsCorrectIndex() {
        Element parent = new Element("div");
        Element a = new Element("a");
        Element b = new Element("b");
        parent.appendChild(a);
        parent.appendChild(b);
        assertEquals(0, a.elementSiblingIndex());
        assertEquals(1, b.elementSiblingIndex());
    }

    // Tests cssSelector returns #id when id present
    @Test
    public void testCssSelector_withId_returnsHashId() {
        Element el = new Element("div").attr("id", "myid");
        assertEquals("#myid", el.cssSelector());
    }

    // Tests cssSelector for root element returns tag name
    @Test
    public void testCssSelector_rootElement_returnsTagName() {
        Element root = new Element("html");
        assertEquals("html", root.cssSelector());
    }

    // Tests empty() clears all children
    @Test
    public void testEmpty_clearsChildren() {
        Element parent = new Element("div");
        parent.appendChild(new Element("p"));
        parent.appendChild(new Element("span"));
        assertEquals(2, parent.childNodeSize());
        parent.empty();
        assertEquals(0, parent.childNodeSize());
    }

    // Tests val() for textarea returns text content
    @Test
    public void testVal_textarea_returnsText() {
        Element textarea = new Element("textarea");
        textarea.text("content");
        assertEquals("content", textarea.val());
    }

    // Tests val() for input returns value attribute
    @Test
    public void testVal_input_returnsValueAttr() {
        Element input = new Element("input").attr("value", "test");
        assertEquals("test", input.val());
    }

    // Tests html() with pretty print off returns raw HTML
    @Test
    public void testHtml_prettyPrintOff_returnsRawHtml() {
        Document doc = new Document("");
        doc.outputSettings().prettyPrint(false);
        Element div = new Element("div");
        doc.appendChild(div);
        Element p = new Element("p");
        div.appendChild(p);
        p.appendText("Hello");
        assertEquals("<div><p>Hello</p></div>", div.html());
    }

    // Tests clone creates independent copy
    @Test
    public void testClone_createsDeepCopy() {
        Element parent = new Element("div");
        parent.appendText("text");
        Element clone = parent.clone();
        assertNotSame(parent, clone);
        assertEquals("text", clone.text());
        clone.text("changed");
        assertNotEquals(parent.text(), clone.text());
    }

    // Tests null input to appendChild throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testAppendChild_null_throwsException() {
        Element el = new Element("div");
        el.appendChild(null);
    }

    // Tests empty tag name throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testTagName_empty_throwsException() {
        Element el = new Element("div");
        el.tagName("");
    }

    // ========== New test cases for uncovered parts ==========

    @Test
    public void testId() {
        Element el = new Element("div");
        assertNull(el.id());
        el.attr("id", "myId");
        assertEquals("myId", el.id());
    }

    @Test
    public void testClassName() {
        Element el = new Element("div");
        assertEquals("", el.className());
        el.addClass("c1");
        assertEquals("c1", el.className());
        el.addClass("c2");
        assertTrue(el.className().contains("c1"));
        assertTrue(el.className().contains("c2"));
    }

    @Test
    public void testGetElementById() {
        Element root = new Element("div");
        Element child = new Element("span").attr("id", "target");
        root.appendChild(child);
        assertSame(child, root.getElementById("target"));
        assertNull(root.getElementById("nonexistent"));
    }

    @Test
    public void testGetElementsByTag() {
        Element root = new Element("div");
        root.appendChild(new Element("p"));
        root.appendChild(new Element("span"));
        root.appendChild(new Element("p"));
        assertEquals(2, root.getElementsByTag("p").size());
        assertEquals(1, root.getElementsByTag("span").size());
        assertEquals(0, root.getElementsByTag("b").size());
    }

    @Test
    public void testGetElementsByClass() {
        Element root = new Element("div");
        Element a = new Element("p").addClass("foo");
        Element b = new Element("span").addClass("foo bar");
        Element c = new Element("div").addClass("bar");
        root.appendChild(a);
        root.appendChild(b);
        root.appendChild(c);
        assertEquals(2, root.getElementsByClass("foo").size());
        assertEquals(2, root.getElementsByClass("bar").size());
        assertEquals(0, root.getElementsByClass("baz").size());
    }

    @Test
    public void testAppendTextNode() {
        Element el = new Element("p");
        el.appendChild(new TextNode("Hello"));
        assertEquals("Hello", el.text());
    }

    @Test
    public void testHtml_defaultPrettyPrint() {
        Document doc = new Document("");
        Element div = new Element("div");
        doc.appendChild(div);
        Element p = new Element("p");
        div.appendChild(p);
        p.appendText("Hello");
        String html = div.html();
        assertNotNull(html);
        assertTrue(html.contains("<p>"));
        assertTrue(html.contains("Hello"));
    }

    @Test
    public void testData() {
        Element script = new Element("script");
        script.appendText("alert('hello');");
        assertEquals("alert('hello');", script.data());
        Element style = new Element("style");
        style.appendText("body { margin: 0; }");
        assertEquals("body { margin: 0; }", style.data());
    }

    @Test
    public void testAttrSetGet() {
        Element el = new Element("div");
        el.attr("data-test", "value");
        assertEquals("value", el.attr("data-test"));
        assertTrue(el.hasAttr("data-test"));
        assertFalse(el.hasAttr("nonexistent"));
    }

    @Test
    public void testRemoveAttr() {
        Element el = new Element("div").attr("class", "myclass").attr("id", "myid");
        el.removeAttr("class");
        assertFalse(el.hasAttr("class"));
        assertTrue(el.hasAttr("id"));
    }

    @Test
    public void testAddClass() {
        Element el = new Element("div");
        el.addClass("c1");
        assertTrue(el.hasClass("c1"));
        el.addClass("c2");
        assertTrue(el.hasClass("c1") && el.hasClass("c2"));
        el.addClass("c1");
        assertTrue(el.hasClass("c1"));
        assertEquals("c1 c2", el.attr("class").trim());
    }

    @Test
    public void testRemoveClass() {
        Element el = new Element("div").addClass("a b c");
        el.removeClass("b");
        assertFalse(el.hasClass("b"));
        assertTrue(el.hasClass("a") && el.hasClass("c"));
        el.removeClass("a");
        assertFalse(el.hasClass("a"));
        assertTrue(el.hasClass("c"));
        el.removeClass("d");
        assertEquals("c", el.className().trim());
    }

    @Test
    public void testToggleClass() {
        Element el = new Element("div").addClass("a");
        el.toggleClass("a");
        assertFalse(el.hasClass("a"));
        el.toggleClass("a");
        assertTrue(el.hasClass("a"));
        el.toggleClass("b");
        assertTrue(el.hasClass("b"));
    }

    @Test
    public void testSelect() {
        Document doc = new Document("");
        Element root = new Element("div");
        doc.appendChild(root);
        Element p1 = new Element("p").addClass("foo");
        Element p2 = new Element("p").addClass("bar");
        root.appendChild(p1);
        root.appendChild(p2);
        Elements result = root.select("p.foo");
        assertEquals(1, result.size());
        assertSame(p1, result.get(0));
    }

    @Test
    public void testAppendElement() {
        Element parent = new Element("div");
        Element child = parent.appendElement("span");
        assertNotNull(child);
        assertEquals("span", child.tagName());
        assertSame(child, parent.child(0));
    }

    @Test
    public void testPrependElement() {
        Element parent = new Element("div");
        parent.appendChild(new Element("p"));
        Element child = parent.prependElement("span");
        assertSame(child, parent.child(0));
        assertEquals("span", child.tagName());
    }

    @Test
    public void testParent() {
        Element parent = new Element("div");
        Element child = new Element("p");
        parent.appendChild(child);
        assertSame(parent, child.parent());
        assertNull(parent.parent());
    }

    @Test
    public void testNextPreviousElementSibling() {
        Element parent = new Element("div");
        Element a = new Element("a");
        Element b = new Element("b");
        Element c = new Element("c");
        parent.appendChild(a);
        parent.appendChild(b);
        parent.appendChild(c);
        assertSame(b, a.nextElementSibling());
        assertSame(c, b.nextElementSibling());
        assertNull(c.nextElementSibling());
        assertSame(b, c.previousElementSibling());
        assertSame(a, b.previousElementSibling());
        assertNull(a.previousElementSibling());
    }

    @Test
    public void testFirstLastElementChild() {
        Element parent = new Element("div");
        Element a = new Element("a");
        Element b = new Element("b");
        parent.appendChild(a);
        parent.appendChild(b);
        assertSame(a, parent.firstElementChild());
        assertSame(b, parent.lastElementChild());
        Element empty = new Element("div");
        assertNull(empty.firstElementChild());
        assertNull(empty.lastElementChild());
    }

    @Test
    public void testChildren() {
        Element parent = new Element("div");
        parent.appendChild(new Element("a"));
        parent.appendChild(new TextNode("text"));
        parent.appendChild(new Element("b"));
        Elements children = parent.children();
        assertEquals(2, children.size());
        assertEquals("a", children.get(0).tagName());
        assertEquals("b", children.get(1).tagName());
    }

    @Test
    public void testTextNodes() {
        Element parent = new Element("div");
        parent.appendChild(new TextNode("Hello"));
        parent.appendChild(new Element("b"));
        parent.appendChild(new TextNode("World"));
        List<TextNode> textNodes = parent.textNodes();
        assertEquals(2, textNodes.size());
        assertEquals("Hello", textNodes.get(0).text());
        assertEquals("World", textNodes.get(1).text());
    }

    @Test
    public void testIsBlock() {
        Element div = new Element("div");
        Element span = new Element("span");
        Element p = new Element("p");
        assertTrue(div.isBlock());
        assertFalse(span.isBlock());
        assertTrue(p.isBlock());
    }
}