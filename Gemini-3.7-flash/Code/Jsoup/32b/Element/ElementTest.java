package org.jsoup.nodes;

import org.jsoup.parser.Tag;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class ElementTest {

    // Tests defect 32b: cloning an element must create an independent classNames set
    @Test
    public void testClone_retainsStateAndHasIndependentClassNames() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.addClass("foo");
        el.classNames(); // initializes classNames set in the original element

        Element clone = el.clone();
        clone.removeClass("foo");
        clone.addClass("bar");

        assertTrue(el.hasClass("foo"));
        assertFalse(el.hasClass("bar"));
        assertTrue(clone.hasClass("bar"));
        assertFalse(clone.hasClass("foo"));
    }

    // Tests class attribute manipulations: add, remove, toggle, hasClass
    @Test
    public void testClassNames_manipulations() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.attr("class", "one two");

        Set<String> classes = el.classNames();
        assertEquals(2, classes.size());
        assertTrue(el.hasClass("one"));
        assertTrue(el.hasClass("two"));
        assertFalse(el.hasClass("three"));

        el.addClass("three");
        assertTrue(el.hasClass("three"));

        el.removeClass("one");
        assertFalse(el.hasClass("one"));

        el.toggleClass("four");
        assertTrue(el.hasClass("four"));
        el.toggleClass("four");
        assertFalse(el.hasClass("four"));
    }

    // Tests tag name updates and isBlock check
    @Test
    public void testTagName_validAndBlockStatus() {
        Element el = new Element(Tag.valueOf("span"), "");
        assertEquals("span", el.tagName());
        assertEquals("span", el.nodeName());
        assertFalse(el.isBlock());

        el.tagName("div");
        assertEquals("div", el.tagName());
        assertTrue(el.isBlock());
    }

    // Tests tag name validation exception
    @Test(expected = IllegalArgumentException.class)
    public void testTagName_emptyTag_throwsException() {
        Element el = new Element(Tag.valueOf("span"), "");
        el.tagName("");
    }

    // Tests id attribute and HTML5 dataset
    @Test
    public void testIdAndDataset_values() {
        Element el = new Element(Tag.valueOf("div"), "");
        assertEquals("", el.id());

        el.attr("id", "main-header");
        assertEquals("main-header", el.id());

        el.attr("data-type", "article");
        assertEquals("article", el.dataset().get("type"));
    }

    // Tests parent and child element traversal
    @Test
    public void testChildrenAndParents_navigation() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element child1 = parent.appendElement("p");
        Element child2 = parent.appendElement("span");

        assertEquals(2, parent.children().size());
        assertEquals(child1, parent.child(0));
        assertEquals(child2, parent.child(1));

        assertEquals(parent, child1.parent());
        assertEquals(1, child1.parents().size());

        assertEquals(child2, child1.nextElementSibling());
        assertNull(child2.nextElementSibling());
        assertEquals(child1, child2.previousElementSibling());
        assertNull(child1.previousElementSibling());

        assertEquals(0, (int) child1.elementSiblingIndex());
        assertEquals(1, (int) child2.elementSiblingIndex());
    }

    // Tests sibling elements querying
    @Test
    public void testSiblingElements_returnsOtherChildren() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element child1 = parent.appendElement("p");
        Element child2 = parent.appendElement("span");
        Element child3 = parent.appendElement("a");

        assertEquals(2, child1.siblingElements().size());
        assertTrue(child1.siblingElements().contains(child2));
        assertTrue(child1.siblingElements().contains(child3));
        assertFalse(child1.siblingElements().contains(child1));

        assertEquals(child1, child2.firstElementSibling());
        assertEquals(child3, child2.lastElementSibling());
    }

    // Tests text extraction: text() vs ownText()
    @Test
    public void testTextAndOwnText_hierarchicalText() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.appendText("Hello ");
        Element span = div.appendElement("span");
        span.appendText("World");
        div.appendText(" !");

        assertEquals("Hello World !", div.text());
        assertEquals("Hello !", div.ownText());
        assertTrue(div.hasText());
    }

    // Tests text replacement and empty()
    @Test
    public void testText_replaceAndEmpty() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.appendElement("p").text("Old Text");
        assertEquals("Old Text", div.text());

        div.text("New Text");
        assertEquals("New Text", div.text());
        assertEquals(1, div.textNodes().size());

        div.empty();
        assertEquals(0, div.childNodes().size());
        assertFalse(div.hasText());
        assertEquals("", div.text());
    }

    // Tests form element value getter/setter
    @Test
    public void testVal_inputAndTextarea() {
        Element input = new Element(Tag.valueOf("input"), "");
        input.val("test-val");
        assertEquals("test-val", input.val());

        Element textarea = new Element(Tag.valueOf("textarea"), "");
        textarea.val("content");
        assertEquals("content", textarea.val());
    }

    // Tests inner and outer HTML generation and parsing
    @Test
    public void testHtmlAndOuterHtml_generation() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.html("<p>Text</p>");

        assertEquals("<p>Text</p>", div.html());
        assertEquals("<div>\n <p>Text</p>\n</div>", div.outerHtml());
    }

    // Tests DOM selection and search methods
    @Test
    public void testDomSearchMethods() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.attr("id", "root");
        Element p = div.appendElement("p").attr("class", "lead highlight").attr("title", "sample-title");
        p.text("Sample paragraph text");

        assertEquals(div, div.getElementById("root"));
        assertNull(div.getElementById("non-existent"));

        assertEquals(1, div.getElementsByTag("p").size());
        assertEquals(p, div.getElementsByTag("p").first());

        assertEquals(1, div.getElementsByClass("lead").size());
        assertEquals(p, div.getElementsByClass("lead").first());

        assertEquals(1, div.getElementsByAttribute("title").size());
        assertEquals(1, div.getElementsByAttributeValue("title", "sample-title").size());
        assertEquals(1, div.getElementsByAttributeValueStarting("title", "sample").size());
        assertEquals(1, div.getElementsByAttributeValueEnding("title", "title").size());
        assertEquals(1, div.getElementsByAttributeValueContaining("title", "ple").size());

        assertEquals(1, div.getElementsContainingText("paragraph").size());
        assertEquals(1, div.getElementsContainingOwnText("paragraph").size());

        assertEquals(2, div.getAllElements().size());
    }

    // Tests DataNode extraction
    @Test
    public void testDataAndDataNodes() {
        Element script = new Element(Tag.valueOf("script"), "");
        DataNode data = new DataNode("var x = 1;", "");
        script.appendChild(data);

        assertEquals("var x = 1;", script.data());
        List<DataNode> dataNodes = script.dataNodes();
        assertEquals(1, dataNodes.size());
        assertEquals("var x = 1;", dataNodes.get(0).getWholeData());
    }

    // Tests child insertion at index
    @Test
    public void testInsertChildren_atPosition() {
        Element div = new Element(Tag.valueOf("div"), "");
        Element p2 = new Element(Tag.valueOf("p"), "").text("two");
        div.appendChild(p2);

        Element p1 = new Element(Tag.valueOf("p"), "").text("one");
        div.insertChildren(0, Arrays.asList(p1));

        assertEquals(2, div.children().size());
        assertEquals(p1, div.child(0));
        assertEquals(p2, div.child(1));
    }

    // Tests before and after sibling insertion
    @Test
    public void testBeforeAndAfter_insertion() {
        Element div = new Element(Tag.valueOf("div"), "");
        Element p = div.appendElement("p").text("Middle");

        p.before("<span>Before</span>");
        p.after("<span>After</span>");

        assertEquals(3, div.children().size());
        assertEquals("span", div.child(0).tagName());
        assertEquals("p", div.child(1).tagName());
        assertEquals("span", div.child(2).tagName());
    }
}