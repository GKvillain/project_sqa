package org.jsoup.nodes;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import org.jsoup.Jsoup;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;

import java.util.List;
import java.util.Set;

public class ElementTest {

    private Element div;

    @Before
    public void setUp() {
        div = new Element("div");
        div.appendChild(new TextNode("Hello "));
        Element span = new Element("span");
        span.appendChild(new TextNode("World"));
        div.appendChild(span);
        div.appendChild(new TextNode(" !"));
    }

    // Test basic text normalization
    @Test
    public void testText_simple_returnsNormalizedText() {
        assertEquals("Hello World !", div.text());
    }

    // Test text adds space after block element
    @Test
    public void testText_withBlockElements_addsSpace() {
        Element p = new Element("p");
        p.appendChild(new TextNode("One"));
        Element divBlock = new Element("div");
        divBlock.appendChild(new TextNode("Two"));
        p.appendChild(divBlock);
        p.appendChild(new TextNode("Three"));
        assertEquals("One Two Three", p.text());
    }

    // Test text adds space after <br>
    @Test
    public void testText_withBr_addsSpace() {
        Element d = new Element("div");
        d.appendChild(new TextNode("One"));
        Element br = new Element("br");
        d.appendChild(br);
        d.appendChild(new TextNode("Two"));
        assertEquals("One Two", d.text());
    }

    // Test ownText returns only direct text, normalized and trimmed
    @Test
    public void testOwnText_returnsDirectText() {
        assertEquals("Hello !", div.ownText());
    }

    // Test wholeText preserves original whitespace (no normalization)
    @Test
    public void testWholeText_preservesWhitespace() {
        assertEquals("Hello World !", div.wholeText());
    }

    // Test hasClass exact match (case sensitive? actually case insensitive)
    @Test
    public void testHasClass_exactMatch_returnsTrue() {
        Element el = new Element("div");
        el.addClass("test");
        assertTrue(el.hasClass("test"));
        // case insensitive match
        assertTrue(el.hasClass("TEST"));
    }

    // Test hasClass with multiple classes
    @Test
    public void testHasClass_partialMatch_returnsTrue() {
        Element el = new Element("div");
        el.attr("class", "test1 test2");
        assertTrue(el.hasClass("test2"));
        assertTrue(el.hasClass("test1"));
    }

    // Test hasClass when there is no class attribute
    @Test
    public void testHasClass_noClass_returnsFalse() {
        Element el = new Element("div");
        assertFalse(el.hasClass("anything"));
    }

    // Test addClass duplicate is ignored
    @Test
    public void testAddClass_duplicate_ignored() {
        Element el = new Element("div");
        el.addClass("a");
        el.addClass("a");
        Set<String> classes = el.classNames();
        assertEquals(1, classes.size());
    }

    // Test removeClass removes correctly
    @Test
    public void testRemoveClass_removesCorrectly() {
        Element el = new Element("div");
        el.addClass("a b");
        el.removeClass("a");
        assertFalse(el.hasClass("a"));
        assertTrue(el.hasClass("b"));
    }

    // Test toggleClass adds and removes
    @Test
    public void testToggleClass_togglesPresence() {
        Element el = new Element("div");
        el.toggleClass("x");
        assertTrue(el.hasClass("x"));
        el.toggleClass("x");
        assertFalse(el.hasClass("x"));
    }

    // Test empty removes all children
    @Test
    public void testEmpty_removesAllChildren() {
        Element el = new Element("div");
        el.appendChild(new TextNode("test"));
        el.empty();
        assertEquals(0, el.childNodeSize());
    }

    // Test appendChild adds node and updates childNodes
    @Test
    public void testAppendChild_addsNode() {
        Element parent = new Element("div");
        Element child = new Element("span");
        parent.appendChild(child);
        assertEquals(1, parent.childNodeSize());
        assertSame(child, parent.childNode(0));
    }

    // Test prependChild inserts at beginning
    @Test
    public void testPrependChild_insertsAtBeginning() {
        Element parent = new Element("div");
        parent.appendChild(new Element("span"));
        Element first = new Element("b");
        parent.prependChild(first);
        assertSame(first, parent.childNode(0));
    }

    // Test insertChildren at a specific index
    @Test
    public void testInsertChildren_atMiddle_works() {
        Element parent = new Element("div");
        parent.appendChild(new TextNode("1"));
        parent.appendChild(new TextNode("3"));
        TextNode two = new TextNode("2");
        parent.insertChildren(1, two);
        assertEquals("1", ((TextNode)parent.childNode(0)).getWholeText());
        assertEquals("2", ((TextNode)parent.childNode(1)).getWholeText());
        assertEquals("3", ((TextNode)parent.childNode(2)).getWholeText());
    }

    // Test children cache is invalidated after modification
    @Test
    public void testChildren_cachedAfterModification() {
        Element parent = new Element("div");
        parent.appendChild(new Element("a"));
        Elements children = parent.children(); // builds cache
        assertEquals(1, children.size());
        parent.appendChild(new Element("b"));
        children = parent.children(); // should rebuild cache
        assertEquals(2, children.size());
    }

    // Test elementSiblingIndex
    @Test
    public void testElementSiblingIndex_returnsCorrectIndex() {
        Element parent = new Element("ul");
        Element li1 = new Element("li");
        Element li2 = new Element("li");
        Element li3 = new Element("li");
        parent.appendChild(li1);
        parent.appendChild(li2);
        parent.appendChild(li3);
        assertEquals(0, li1.elementSiblingIndex());
        assertEquals(1, li2.elementSiblingIndex());
        assertEquals(2, li3.elementSiblingIndex());
    }

    // Test nextElementSibling and previousElementSibling
    @Test
    public void testNextElementSibling_andPrevious_work() {
        Element parent = new Element("div");
        Element a = new Element("a");
        Element b = new Element("b");
        parent.appendChild(a);
        parent.appendChild(b);
        assertSame(b, a.nextElementSibling());
        assertNull(b.nextElementSibling());
        assertSame(a, b.previousElementSibling());
        assertNull(a.previousElementSibling());
    }

    // Test clone produces independent copy
    @Test
    public void testClone_createsIndependentCopy() {
        Element original = new Element("div");
        original.appendChild(new TextNode("original"));
        Element clone = original.clone();
        clone.text("cloned");
        assertEquals("original", original.text());
        assertEquals("cloned", clone.text());
    }

    // Test tagName with null throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testTagName_nullInput_throwsException() {
        Element el = new Element("div");
        el.tagName(null);
    }

    // Test text() returns empty for element without children
    @Test
    public void testText_empty_returnsEmptyString() {
        Element empty = new Element("div");
        assertEquals("", empty.text());
    }

    // ================== New Test Cases for Uncovered Parts ==================

    // Test attr() get/set/remove and hasAttr()
    @Test
    public void testAttr_getSetRemove_works() {
        Document doc = Jsoup.parse("<div id='container' data-name='test'>content</div>");
        Element el = doc.getElementById("container");

        // get existing attribute
        assertEquals("test", el.attr("data-name"));
        // default empty string for missing attribute
        assertEquals("", el.attr("nonexistent"));

        // set attribute
        el.attr("data-new", "value");
        assertEquals("value", el.attr("data-new"));

        // hasAttr
        assertTrue(el.hasAttr("data-new"));
        assertFalse(el.hasAttr("missing"));

        // remove attribute
        el.removeAttr("data-new");
        assertFalse(el.hasAttr("data-new"));
        assertEquals("", el.attr("data-new"));
    }

    // Test tagName() returns correct tag name
    @Test
    public void testTagName_returnsCurrentTagName() {
        Document doc = Jsoup.parse("<div>text</div>");
        Element div = doc.selectFirst("div");
        assertEquals("div", div.tagName());
    }

    // Test tag() returns Tag object with name
    @Test
    public void testTagObject_returnsInfo() {
        Document doc = Jsoup.parse("<span>text</span>");
        Element span = doc.selectFirst("span");
        assertEquals("span", span.tag().getName());
    }

    // Test id() returns empty string initially and works after setting
    @Test
    public void testId_returnsEmptyStringInitially() {
        Element el = new Element("div");
        assertEquals("", el.id());
        el.id("myId");
        assertEquals("myId", el.id());
    }

    // Test className() returns class attribute value
    @Test
    public void testClassName_returnsClassAttribute() {
        Element el = new Element("div");
        el.attr("class", "first second");
        assertEquals("first second", el.className());
    }

    // Test className(String) sets class attribute
    @Test
    public void testSetClassName_setsClassAttribute() {
        Element el = new Element("div");
        el.className("newClass");
        assertEquals("newClass", el.className());
        assertTrue(el.hasClass("newClass"));
    }

    // Test classNames(Set<String>) setter replaces existing classes
    @Test
    public void testClassNames_setter_addsClasses() {
        Element el = new Element("div");
        el.addClass("a");
        el.addClass("b");
        Set<String> newClasses = new java.util.HashSet<>();
        newClasses.add("x");
        newClasses.add("y");
        el.classNames(newClasses);
        assertEquals(2, el.classNames().size());
        assertTrue(el.hasClass("x"));
        assertTrue(el.hasClass("y"));
        assertFalse(el.hasClass("a"));
    }

    // Test parent() returns correct parent element
    @Test
    public void testParent_returnsParentElement() {
        Document doc = Jsoup.parse("<div><p>text</p></div>");
        Element p = doc.selectFirst("p");
        Element parent = p.parent();
        assertNotNull(parent);
        assertEquals("div", parent.tagName());
    }

    // Test parents() returns all ancestors
    @Test
    public void testParents_returnsAncestors() {
        Document doc = Jsoup.parse("<html><body><div><p>text</p></div></body></html>");
        Element p = doc.selectFirst("p");
        Elements parents = p.parents();
        // includes div, body, html
        assertEquals(3, parents.size());
        String[] expected = {"div", "body", "html"};
        for (int i = 0; i < parents.size(); i++) {
            assertEquals(expected[i], parents.get(i).tagName());
        }
    }

    // Test closest() finds nearest matching ancestor
    @Test
    public void testClosest_findsMatchingAncestor() {
        Document doc = Jsoup.parse("<div class='wrapper'><p>text</p></div>");
        Element p = doc.selectFirst("p");
        Element closest = p.closest(".wrapper");
        assertNotNull(closest);
        assertEquals("div", closest.tagName());
        assertTrue(closest.hasClass("wrapper"));
    }

    // Test select() returns matching descendant elements
    @Test
    public void testSelect_returnsMatchingChildren() {
        Document doc = Jsoup.parse("<div><p>one</p><p>two</p><span>three</span></div>");
        Element div = doc.selectFirst("div");
        Elements pTags = div.select("p");
        assertEquals(2, pTags.size());
        assertEquals("one", pTags.get(0).text());
        assertEquals("two", pTags.get(1).text());
    }

    // Test selectFirst() returns first matching element or null
    @Test
    public void testSelectFirst_returnsFirstMatch() {
        Document doc = Jsoup.parse("<div><p>one</p><p>two</p></div>");
        Element div = doc.selectFirst("div");
        Element first = div.selectFirst("p");
        assertNotNull(first);
        assertEquals("one", first.text());
        assertNull(div.selectFirst("span"));
    }

    // Test getElementsByTag()
    @Test
    public void testGetElementsByTag_returnsElements() {
        Document doc = Jsoup.parse("<div><p>one</p><p>two</p></div>");
        Element div = doc.selectFirst("div");
        Elements pTags = div.getElementsByTag("p");
        assertEquals(2, pTags.size());
    }

    // Test getElementById()
    @Test
    public void testGetElementById_returnsElement() {
        Document doc = Jsoup.parse("<div><p id='unique'>text</p></div>");
        Element found = doc.getElementById("unique");
        assertNotNull(found);
        assertEquals("text", found.text());
        assertNull(doc.getElementById("missing"));
    }

    // Test getElementsByClass()
    @Test
    public void testGetElementsByClass_returnsElements() {
        Document doc = Jsoup.parse("<div><p class='highlight'>a</p><p class='highlight'>b</p><p>c</p></div>");
        Elements highlighted = doc.getElementsByClass("highlight");
        assertEquals(2, highlighted.size());
    }

    // Test getElementsByAttribute()
    @Test
    public void testGetElementsByAttribute_returnsElements() {
        Document doc = Jsoup.parse("<div><p data-x='1'>a</p><p data-y='2'>b</p></div>");
        Elements withDataX = doc.getElementsByAttribute("data-x");
        assertEquals(1, withDataX.size());
        assertEquals("a", withDataX.first().text());
    }

    // Test firstElementSibling() and lastElementSibling()
    @Test
    public void testFirstAndLastElementSibling_work() {
        Document doc = Jsoup.parse("<ul><li>one</li><li>two</li><li>three</li></ul>");
        Elements items = doc.select("li");
        Element first = items.get(0);
        Element middle = items.get(1);
        Element last = items.get(2);

        assertSame(first, middle.firstElementSibling());
        assertSame(last, middle.lastElementSibling());
        assertSame(first, first.firstElementSibling());
        assertSame(last, last.lastElementSibling());
    }

    // Test isBlock() and isInline() for typical elements
    @Test
    public void testIsBlock_andIsInline_forDiv() {
        Element div = new Element("div");
        assertTrue(div.isBlock());
        assertFalse(div.isInline());
    }

    // Test data() returns data from script/style tags
    @Test
    public void testData_returnsDataString() {
        Document doc = Jsoup.parse("<script>var x = 1;</script>");
        Element script = doc.selectFirst("script");
        assertEquals("var x = 1;", script.data());
    }

    // Test html() returns inner HTML
    @Test
    public void testHtml_returnsInnerHtml() {
        Document doc = Jsoup.parse("<div><p>Hello</p> World</div>");
        Element div = doc.selectFirst("div");
        String html = div.html();
        assertEquals("<p>Hello</p> World", html);
    }

    // Test outerHtml() returns full HTML
    @Test
    public void testOuterHtml_returnsFullHtml() {
        Document doc = Jsoup.parse("<div id='d'><p>Hello</p></div>");
        Element div = doc.selectFirst("div");
        String outer = div.outerHtml();
        assertEquals("<div id=\"d\"><p>Hello</p></div>", outer);
    }

    // Test toString() equals outerHtml()
    @Test
    public void testToString_returnsOuterHtml() {
        Document doc = Jsoup.parse("<div><p>Hello</p></div>");
        Element div = doc.selectFirst("div");
        assertEquals(div.outerHtml(), div.toString());
    }

    // Test after() inserts new element immediately after this element
    @Test
    public void testAfter_insertsElementAfter() {
        Document doc = Jsoup.parse("<div><p>original</p></div>");
        Element p = doc.selectFirst("p");
        p.after("<span>new</span>");
        Elements children = doc.selectFirst("div").children();
        assertEquals(2, children.size());
        assertEquals("p", children.get(0).tagName());
        assertEquals("span", children.get(1).tagName());
    }

    // Test before() inserts new element before this element
    @Test
    public void testBefore_insertsElementBefore() {
        Document doc = Jsoup.parse("<div><p>original</p></div>");
        Element p = doc.selectFirst("p");
        p.before("<span>new</span>");
        Elements children = doc.selectFirst("div").children();
        assertEquals(2, children.size());
        assertEquals("span", children.get(0).tagName());
        assertEquals("p", children.get(1).tagName());
    }

    // Test append() appends HTML inside element at end
    @Test
    public void testAppend_appendsHtml() {
        Element el = new Element("div");
        el.append("<span>tail</span>");
        assertEquals(1, el.childNodeSize());
        Element span = el.child(0);
        assertEquals("span", span.tagName());
    }

    // Test prepend() prepends HTML inside element at beginning
    @Test
    public void testPrepend_prependsHtml() {
        Element el = new Element("div");
        el.append("<span>tail</span>");
        el.prepend("<b>head</b>");
        Elements children = el.children();
        assertEquals(2, children.size());
        assertEquals("b", children.get(0).tagName());
        assertEquals("span", children.get(1).tagName());
    }

    // Test wrap() wraps this element with provided HTML
    @Test
    public void testWrap_wrapsElement() {
        Document doc = Jsoup.parse("<div><p>content</p></div>");
        Element p = doc.selectFirst("p");
        p.wrap("<section class='wrapper'></section>");
        Element parent = p.parent();
        assertEquals("section", parent.tagName());
        assertTrue(parent.hasClass("wrapper"));
        // The outer div should contain section
        assertEquals("div", parent.parent().tagName());
    }

    // Test unwrap() removes element but keeps its children
    @Test
    public void testUnwrap_unwrapsElement() {
        Document doc = Jsoup.parse("<div><b>bold</b> and <i>italic</i></div>");
        Element div = doc.selectFirst("div");
        Element bold = div.selectFirst("b");
        bold.unwrap();
        // After unwrap, bold's children ('bold') are direct children of div
        List<Node> nodes = div.childNodes();
        // First child should be TextNode "bold"
        assertTrue(nodes.get(0) instanceof TextNode);
        assertEquals("bold", ((TextNode) nodes.get(0)).getWholeText());
        // Then text " and "
        assertTrue(nodes.get(1) instanceof TextNode);
        // Then italic
        assertTrue(nodes.get(2) instanceof Element);
    }

    // Test siblingElements() returns all sibling elements except this one
    @Test
    public void testSiblingElements_returnsSiblings() {
        Document doc = Jsoup.parse("<div><p>one</p><p>two</p><p>three</p></div>");
        Elements ps = doc.select("p");
        Element middle = ps.get(1);
        Elements siblings = middle.siblingElements();
        assertEquals(2, siblings.size());
        assertSame(ps.get(0), siblings.get(0));
        assertSame(ps.get(2), siblings.get(1));
    }

    // Test childNodes() returns a list of all child nodes (including text)
    @Test
    public void testChildNodesList_returnsList() {
        Element el = new Element("div");
        el.appendChild(new TextNode("Hello"));
        el.appendChild(new Element("br"));
        List<Node> nodes = el.childNodes();
        assertEquals(2, nodes.size());
        assertTrue(nodes.get(0) instanceof TextNode);
        assertTrue(nodes.get(1) instanceof Element);
    }
}