package org.jsoup.nodes;

import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class ElementTest {
    private Element div;

    @Before
    public void setUp() {
        div = new Element(Tag.valueOf("div"), "http://example.com/");
    }

    // Tests constructor with null tag throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullTag_throwsException() {
        new Element(null, "http://example.com/");
    }

    // Tests basic node and tag name properties
    @Test
    public void testNodeNameAndTagName_standardTag_returnsCorrectName() {
        assertEquals("div", div.nodeName());
        assertEquals("div", div.tagName());
        assertEquals(Tag.valueOf("div"), div.tag());
        assertTrue(div.isBlock());
    }

    // Tests id attribute retrieval when present and absent
    @Test
    public void testId_presentAndAbsent_returnsCorrectId() {
        assertEquals("", div.id());
        div.attr("id", "main-content");
        assertEquals("main-content", div.id());
    }

    // Tests parent and parents hierarchy accumulation
    @Test
    public void testParents_nestedElements_returnsCorrectAncestors() {
        Element child = div.appendElement("p");
        Element span = child.appendElement("span");

        assertEquals(div, child.parent());
        assertEquals(child, span.parent());

        Elements parents = span.parents();
        assertEquals(2, parents.size());
        assertEquals(child, parents.get(0));
        assertEquals(div, parents.get(1));
    }

    // Tests children and child index methods
    @Test
    public void testChildrenAndChild_multipleElements_returnsChildrenCorrectly() {
        Element p1 = div.appendElement("p");
        Element p2 = div.appendElement("p");
        div.appendText("some text");

        Elements children = div.children();
        assertEquals(2, children.size());
        assertEquals(p1, div.child(0));
        assertEquals(p2, div.child(1));
    }

    // Tests append, prepend, and empty operations on child nodes
    @Test
    public void testAppendPrependAndEmpty_htmlContent_updatesChildrenCorrectly() {
        div.append("<p>One</p><p>Two</p>");
        assertEquals(2, div.children().size());
        assertEquals("One", div.child(0).text());
        assertEquals("Two", div.child(1).text());

        div.prepend("<h1>Title</h1>");
        assertEquals(3, div.children().size());
        assertEquals("h1", div.child(0).tagName());
        assertEquals("Title", div.child(0).text());

        div.empty();
        assertEquals(0, div.children().size());
        assertEquals(0, div.childNodes().size());
    }

    // Tests append with null argument throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testAppend_nullHtml_throwsException() {
        div.append(null);
    }

    // Tests prepend with null argument throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testPrepend_nullHtml_throwsException() {
        div.prepend(null);
    }

    // Tests wrap HTML around an element
    @Test
    public void testWrap_validHtml_wrapsElementCorrectly() {
        Element parent = new Element(Tag.valueOf("div"), "http://example.com/");
        Element span = parent.appendElement("span");
        span.text("hello");

        span.wrap("<div class='wrapper'><i></i></div>");

        assertEquals("<div class=\"wrapper\"><i><span>hello</span></i></div>", parent.html());
    }

    // Tests sibling navigation methods
    @Test
    public void testSiblingNavigation_multipleSiblings_navigatesCorrectly() {
        Element p1 = div.appendElement("p");
        Element p2 = div.appendElement("span");
        Element p3 = div.appendElement("div");

        assertEquals(3, p2.siblingElements().size());
        assertEquals(p1, p2.previousElementSibling());
        assertNull(p1.previousElementSibling());

        assertEquals(p3, p2.nextElementSibling());
        assertNull(p3.nextElementSibling());

        assertEquals(p1, p2.firstElementSibling());
        assertEquals(p3, p2.lastElementSibling());

        assertEquals(Integer.valueOf(1), p2.elementSiblingIndex());
        assertEquals(Integer.valueOf(0), div.elementSiblingIndex());
    }

    // Tests DOM query methods by tag, ID, class, and attributes
    @Test
    public void testGetElements_byVariousSelectors_returnsMatchingElements() {
        div.append("<p id='first' class='lead active' title='para1'>Text 1</p>");
        div.append("<p class='lead' title='para2'>Text 2</p>");
        div.append("<span class='active'>Span Text</span>");

        assertEquals(2, div.getElementsByTag("p").size());
        assertNotNull(div.getElementById("first"));
        assertEquals("Text 1", div.getElementById("first").text());
        assertNull(div.getElementById("non-existent"));

        assertEquals(2, div.getElementsByClass("lead").size());
        assertEquals(2, div.getElementsByClass("active").size());
        assertEquals(2, div.getElementsByAttribute("title").size());
        assertEquals(1, div.getElementsByAttributeValue("title", "para1").size());
        assertEquals(1, div.getElementsByAttributeValueStarting("title", "para").size());
        assertEquals(1, div.getElementsByAttributeValueEnding("title", "2").size());
        assertEquals(2, div.getElementsByAttributeValueContaining("title", "para").size());
        assertTrue(div.getElementsByAttributeValueNot("title", "para1").size() >= 1);

        assertEquals(1, div.getElementsByIndexLessThan(1).size());
        assertEquals(2, div.getElementsByIndexGreaterThan(0).size());
        assertEquals(1, div.getElementsByIndexEquals(1).size());
        assertEquals(4, div.getAllElements().size());
    }

    // Tests select method using CSS query
    @Test
    public void testSelect_validQuery_returnsMatchedElements() {
        div.append("<a href='http://example.com/one'>One</a><a href='http://other.com/two'>Two</a>");
        Elements links = div.select("a[href^=http://example.com]");
        assertEquals(1, links.size());
        assertEquals("One", links.get(0).text());
    }

    // Tests text extraction, text setting, and hasText behavior
    @Test
    public void testTextAndHasText_textAndNestedElements_handlesCorrectly() {
        assertFalse(div.hasText());
        assertEquals("", div.text());

        div.text("Hello World");
        assertTrue(div.hasText());
        assertEquals("Hello World", div.text());

        div.empty();
        div.append("<p>First</p><p>Second</p>");
        assertTrue(div.hasText());
        assertEquals("First Second", div.text());
    }

    // Tests whitespace preservation in preformatted tags
    @Test
    public void testPreserveWhitespace_preTag_preservesWhitespace() {
        Element pre = new Element(Tag.valueOf("pre"), "http://example.com/");
        pre.appendText("   line1\n   line2   ");
        assertTrue(pre.preserveWhitespace());
        assertEquals("line1\n   line2", pre.text());
    }

    // Tests data method for DataNode extraction
    @Test
    public void testData_scriptTag_extractsData() {
        Element script = div.appendElement("script");
        script.appendChild(new DataNode("var x = 1;", "http://example.com/"));
        assertEquals("var x = 1;", script.data());
    }

    // Tests class manipulation methods: classNames, addClass, removeClass, toggleClass, hasClass
    @Test
    public void testClassManipulations_classOperations_updatesClassesCorrectly() {
        assertEquals("", div.className());
        assertTrue(div.classNames().isEmpty());
        assertFalse(div.hasClass("test"));

        div.addClass("primary");
        assertTrue(div.hasClass("primary"));
        assertEquals("primary", div.className());

        div.addClass("active");
        assertTrue(div.hasClass("active"));
        assertEquals("primary active", div.className());

        div.removeClass("primary");
        assertFalse(div.hasClass("primary"));
        assertTrue(div.hasClass("active"));

        div.toggleClass("active");
        assertFalse(div.hasClass("active"));

        div.toggleClass("active");
        assertTrue(div.hasClass("active"));

        Set<String> newClasses = new HashSet<String>();
        newClasses.add("one");
        newClasses.add("two");
        div.classNames(newClasses);
        assertTrue(div.hasClass("one"));
        assertTrue(div.hasClass("two"));
        assertFalse(div.hasClass("active"));
    }

    // Tests val method on input and textarea elements
    @Test
    public void testVal_inputAndTextarea_getsAndSetsValueCorrectly() {
        Element input = new Element(Tag.valueOf("input"), "http://example.com/");
        input.val("test-value");
        assertEquals("test-value", input.val());

        Element textarea = new Element(Tag.valueOf("textarea"), "http://example.com/");
        textarea.val("text content");
        assertEquals("text content", textarea.val());
    }

    // Tests outerHtml, html, and toString output
    @Test
    public void testHtmlAndOuterHtml_elementTree_formatsHtmlCorrectly() {
        div.attr("id", "main");
        div.appendElement("p").text("Hello");

        assertEquals("<p>Hello</p>", div.html());
        assertEquals("<div id=\"main\">\n <p>Hello</p>\n</div>", div.outerHtml());
        assertEquals(div.outerHtml(), div.toString());

        Element img = new Element(Tag.valueOf("img"), "http://example.com/");
        assertEquals("<img />", img.outerHtml());
    }

    // Tests equals and hashCode consistency
    @Test
    public void testEqualsAndHashCode_sameAndDifferentObjects_worksCorrectly() {
        Element div1 = new Element(Tag.valueOf("div"), "http://example.com/");
        Element div2 = new Element(Tag.valueOf("div"), "http://example.com/");
        Element span = new Element(Tag.valueOf("span"), "http://example.com/");

        assertEquals(div1, div1);
        assertEquals(div1, div2);
        assertEquals(div1.hashCode(), div2.hashCode());

        assertFalse(div1.equals(span));
        assertFalse(div1.equals(null));
        assertFalse(div1.equals("string"));
    }
}