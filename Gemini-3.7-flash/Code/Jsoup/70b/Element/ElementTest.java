package org.jsoup.nodes;

import org.jsoup.Jsoup;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class ElementTest {

    // Tests defect 70: preserveWhitespace should check ancestors beyond immediate parent
    @Test
    public void testText_deeplyNestedInsidePre_preservesWhitespace() {
        Document doc = Jsoup.parse("<pre><code><span>  foo \n  bar  </span></code></pre>");
        Element span = doc.select("span").first();
        assertNotNull(span);
        assertEquals("  foo \n  bar  ", span.text());
    }

    // Tests element instantiation, tag name and isBlock properties
    @Test
    public void testConstructorAndTag_validTag_initializedCorrectly() {
        Element div = new Element("div");
        assertEquals("div", div.tagName());
        assertEquals("div", div.nodeName());
        assertTrue(div.isBlock());

        Element span = new Element(Tag.valueOf("span"), "");
        assertEquals("span", span.tagName());
        assertFalse(span.isBlock());

        span.tagName("p");
        assertEquals("p", span.tagName());
        assertTrue(span.isBlock());
    }

    // Tests exception when changing tag name to empty string
    @Test(expected = IllegalArgumentException.class)
    public void testTagName_emptyString_throwsException() {
        Element el = new Element("div");
        el.tagName("");
    }

    // Tests setting and getting attributes and HTML5 dataset
    @Test
    public void testAttrAndDataset_validAttributes_returnsExpectedValues() {
        Element el = new Element("div");
        el.attr("id", "main");
        el.attr("data-custom", "value1");
        el.attr("disabled", true);

        assertEquals("main", el.id());
        assertEquals("value1", el.dataset().get("custom"));
        assertTrue(el.hasAttr("disabled"));

        el.attr("disabled", false);
        assertFalse(el.hasAttr("disabled"));
    }

    // Tests class name manipulation: addClass, removeClass, toggleClass, and classNames set
    @Test
    public void testClassNames_manipulateClasses_returnsUpdatedClasses() {
        Element el = new Element("div");
        el.addClass("header");
        el.addClass("active");

        assertEquals("header active", el.className());
        assertTrue(el.hasClass("header"));
        assertTrue(el.hasClass("ACTIVE")); // case insensitive

        el.removeClass("header");
        assertFalse(el.hasClass("header"));
        assertTrue(el.hasClass("active"));

        el.toggleClass("active");
        assertFalse(el.hasClass("active"));

        el.toggleClass("active");
        assertTrue(el.hasClass("active"));

        Set<String> newClasses = new LinkedHashSet<>();
        newClasses.add("one");
        newClasses.add("two");
        el.classNames(newClasses);
        assertEquals("one two", el.className());
        assertEquals(2, el.classNames().size());

        el.classNames(Collections.<String>emptySet());
        assertEquals("", el.className());
    }

    // Tests hasClass with empty class attribute and boundary checks
    @Test
    public void testHasClass_boundaryAndEmptyConditions_returnsFalse() {
        Element el = new Element("div");
        assertFalse(el.hasClass("test"));

        el.attr("class", "te");
        assertFalse(el.hasClass("test"));

        el.attr("class", "testing");
        assertFalse(el.hasClass("test"));

        el.attr("class", "test");
        assertTrue(el.hasClass("test"));

        el.attr("class", "foo test bar");
        assertTrue(el.hasClass("test"));
        assertFalse(el.hasClass("baz"));
    }

    // Tests child element retrieval and text node filtering
    @Test
    public void testChildAndChildren_validIndexAndNodes_returnsExpectedElements() {
        Document doc = Jsoup.parse("<div><p>Paragraph 1</p>Text<span>Span 1</span></div>");
        Element div = doc.select("div").first();

        assertEquals(2, div.children().size());
        assertEquals("p", div.child(0).tagName());
        assertEquals("span", div.child(1).tagName());

        List<TextNode> textNodes = div.textNodes();
        assertEquals(1, textNodes.size());
        assertEquals("Text", textNodes.get(0).text());
    }

    // Tests text() and ownText() normalization with nested elements and br tags
    @Test
    public void testTextAndOwnText_nestedElements_returnsCorrectText() {
        Document doc = Jsoup.parse("<div>Hello <span>world</span>!<br>How are you?</div>");
        Element div = doc.select("div").first();

        assertEquals("Hello world! How are you?", div.text());
        assertEquals("Hello ! How are you?", div.ownText());
        assertTrue(div.hasText());

        Element empty = new Element("div");
        assertFalse(empty.hasText());
        assertEquals("", empty.text());
        assertEquals("", empty.ownText());

        empty.text("New text");
        assertEquals("New text", empty.text());
    }

    // Tests data() extraction on script and comment nodes
    @Test
    public void testDataAndDataNodes_scriptElement_returnsCorrectData() {
        Document doc = Jsoup.parse("<script type=\"text/javascript\">var x = 1;</script>");
        Element script = doc.select("script").first();

        assertEquals("var x = 1;", script.data());
        assertEquals(1, script.dataNodes().size());
        assertEquals("var x = 1;", script.dataNodes().get(0).getWholeData());
    }

    // Tests sibling navigation methods: next, previous, first, last, index
    @Test
    public void testSiblingElements_multipleSiblings_returnsCorrectSiblingsAndIndices() {
        Document doc = Jsoup.parse("<div><p id='p1'>1</p><p id='p2'>2</p><p id='p3'>3</p></div>");
        Element p1 = doc.getElementById("p1");
        Element p2 = doc.getElementById("p2");
        Element p3 = doc.getElementById("p3");

        assertEquals(2, p1.siblingElements().size());
        assertEquals(0, p1.elementSiblingIndex());
        assertEquals(1, p2.elementSiblingIndex());
        assertEquals(2, p3.elementSiblingIndex());

        assertEquals(p2, p1.nextElementSibling());
        assertEquals(p3, p2.nextElementSibling());
        assertNull(p3.nextElementSibling());

        assertNull(p1.previousElementSibling());
        assertEquals(p1, p2.previousElementSibling());
        assertEquals(p2, p3.previousElementSibling());

        assertEquals(p1, p2.firstElementSibling());
        assertEquals(p3, p2.lastElementSibling());

        Element orphan = new Element("span");
        assertNull(orphan.nextElementSibling());
        assertNull(orphan.previousElementSibling());
        assertEquals(0, orphan.elementSiblingIndex());
        assertEquals(0, orphan.siblingElements().size());
    }

    // Tests insertChildren with normal and negative indices
    @Test
    public void testInsertChildren_validAndNegativeIndex_insertsNodesCorrectly() {
        Element div = new Element("div");
        Element p1 = new Element("p").text("One");
        Element p2 = new Element("p").text("Two");
        Element p3 = new Element("p").text("Three");

        div.appendChild(p1);
        div.appendChild(p3);

        div.insertChildren(1, p2);
        assertEquals(3, div.children().size());
        assertEquals("Two", div.child(1).text());

        Element p4 = new Element("p").text("Four");
        div.insertChildren(-1, p4); // roll-around end insertion
        assertEquals("Four", div.child(3).text());
    }

    // Tests insertChildren with out of bounds index
    @Test(expected = IllegalArgumentException.class)
    public void testInsertChildren_outOfBoundsIndex_throwsException() {
        Element div = new Element("div");
        div.insertChildren(5, new Element("p"));
    }

    // Tests append, prepend, appendText, prependText, appendElement, prependElement
    @Test
    public void testAppendAndPrepend_htmlAndElements_modifiesTreeCorrectly() {
        Element div = new Element("div");
        div.appendElement("span").text("Middle");
        div.prependElement("b").text("Start");
        div.append("<i>End</i>");
        div.prepend("<a href='#'>Link</a>");

        assertEquals(4, div.children().size());
        assertEquals("a", div.child(0).tagName());
        assertEquals("b", div.child(1).tagName());
        assertEquals("span", div.child(2).tagName());
        assertEquals("i", div.child(3).tagName());

        div.appendText(" trailing");
        div.prependText("leading ");
        assertTrue(div.text().startsWith("leading"));
        assertTrue(div.text().endsWith("trailing"));
    }

    // Tests select, selectFirst, is with CSS selectors
    @Test
    public void testSelectAndSelectFirst_cssQueries_returnsMatchingElements() {
        Document doc = Jsoup.parse("<div id='container'><p class='highlight'>First</p><p>Second</p></div>");
        Element container = doc.getElementById("container");

        Elements paragraphs = container.select("p");
        assertEquals(2, paragraphs.size());

        Element firstP = container.selectFirst("p.highlight");
        assertNotNull(firstP);
        assertEquals("First", firstP.text());

        assertTrue(firstP.is(".highlight"));
        assertFalse(firstP.is("span"));
    }

    // Tests cssSelector generation for ID and nth-child scenarios
    @Test
    public void testCssSelector_elementWithAndWithoutId_returnsUniqueSelector() {
        Document doc = Jsoup.parse("<html><body><div id='content'><p>First</p><p>Second</p></div></body></html>");
        Element div = doc.getElementById("content");
        Element p2 = div.child(1);

        assertEquals("#content", div.cssSelector());
        assertEquals("#content > p:nth-child(2)", p2.cssSelector());
    }

    // Tests val() on input and textarea elements
    @Test
    public void testVal_inputAndTextarea_getsAndSetsValues() {
        Element input = new Element(Tag.valueOf("input"), "");
        input.attr("value", "foo");
        assertEquals("foo", input.val());
        input.val("bar");
        assertEquals("bar", input.attr("value"));

        Element textarea = new Element(Tag.valueOf("textarea"), "");
        textarea.text("default text");
        assertEquals("default text", textarea.val());
        textarea.val("updated text");
        assertEquals("updated text", textarea.text());
    }

    // Tests outerHtml, inner html, and empty()
    @Test
    public void testHtmlAndOuterHtml_elementTree_generatesCorrectMarkup() {
        Element div = new Element("div");
        div.html("<p>Hello</p>");

        assertEquals("<p>Hello</p>", div.html());
        assertEquals("<div>\n <p>Hello</p>\n</div>", div.outerHtml());

        div.empty();
        assertEquals("", div.html());
        assertEquals(0, div.childNodeSize());
    }

    // Tests deep clone and shallowClone
    @Test
    public void testCloneAndShallowClone_elementTree_createsCorrectCopies() {
        Element original = new Element(Tag.valueOf("div"), "http://example.com");
        original.attr("id", "orig");
        original.appendElement("span").text("child");

        Element deepClone = original.clone();
        assertEquals("orig", deepClone.id());
        assertEquals(1, deepClone.children().size());
        assertEquals("child", deepClone.child(0).text());
        assertNotSame(original, deepClone);
        assertNotSame(original.child(0), deepClone.child(0));

        Element shallow = original.shallowClone();
        assertEquals("orig", shallow.id());
        assertEquals(0, shallow.children().size());
    }

    // Tests DOM-type search methods (getElementsByTag, getElementsByClass, getElementsByAttribute)
    @Test
    public void testGetElementsByMethods_matchingNodes_returnsExpectedElements() {
        Document doc = Jsoup.parse("<div><span class='test' data-name='a'>One</span><span class='test' data-name='b'>Two</span></div>");
        Element div = doc.select("div").first();

        assertEquals(2, div.getElementsByTag("span").size());
        assertEquals(2, div.getElementsByClass("test").size());
        assertEquals(2, div.getElementsByAttribute("data-name").size());
        assertEquals(1, div.getElementsByAttributeValue("data-name", "a").size());
        assertEquals(2, div.getElementsByAttributeStarting("data-").size());
        assertEquals(1, div.getElementsContainingText("One").size());
        assertEquals(1, div.getElementsContainingOwnText("Two").size());
    }
}