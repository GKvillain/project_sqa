package org.jsoup.select;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ElementsTest {

    // Tests getting attribute from elements when present and absent
    @Test
    public void testAttr_presentAndAbsent_returnsCorrectValues() {
        Document doc = Jsoup.parse("<p title='foo'>One</p><p title='bar'>Two</p><p>Three</p>");
        Elements ps = doc.select("p");

        assertEquals("foo", ps.attr("title"));
        assertEquals("", ps.attr("nonexistent"));

        Elements empty = new Elements();
        assertEquals("", empty.attr("title"));
    }

    // Tests hasAttr method on elements list
    @Test
    public void testHasAttr_presentAndAbsent_returnsExpectedBoolean() {
        Document doc = Jsoup.parse("<p title='foo'>One</p><p>Two</p>");
        Elements ps = doc.select("p");

        assertTrue(ps.hasAttr("title"));
        assertFalse(ps.hasAttr("href"));

        Elements empty = new Elements();
        assertFalse(empty.hasAttr("title"));
    }

    // Tests setting and removing attributes on all matched elements
    @Test
    public void testAttrSetterAndRemoveAttr_modifiesAllElements() {
        Document doc = Jsoup.parse("<p>One</p><p>Two</p>");
        Elements ps = doc.select("p");

        ps.attr("class", "item");
        assertEquals("item", ps.get(0).attr("class"));
        assertEquals("item", ps.get(1).attr("class"));

        ps.removeAttr("class");
        assertFalse(ps.hasAttr("class"));
    }

    // Tests class manipulation methods: addClass, removeClass, toggleClass, hasClass
    @Test
    public void testClasses_addClassRemoveClassToggleClassHasClass() {
        Document doc = Jsoup.parse("<div><p class='one'>1</p><p>2</p></div>");
        Elements ps = doc.select("p");

        assertTrue(ps.hasClass("one"));
        assertFalse(ps.hasClass("two"));

        ps.addClass("two");
        assertTrue(ps.get(0).hasClass("two"));
        assertTrue(ps.get(1).hasClass("two"));

        ps.toggleClass("two");
        assertFalse(ps.get(0).hasClass("two"));
        assertFalse(ps.get(1).hasClass("two"));

        ps.removeClass("one");
        assertFalse(ps.hasClass("one"));
    }

    // Tests form element value getter and setter
    @Test
    public void testVal_getterAndSetter_returnsAndUpdatesValues() {
        Document doc = Jsoup.parse("<input value='one'/><input value='two'/>");
        Elements inputs = doc.select("input");

        assertEquals("one", inputs.val());

        inputs.val("updated");
        assertEquals("updated", inputs.get(0).val());
        assertEquals("updated", inputs.get(1).val());

        Elements empty = new Elements();
        assertEquals("", empty.val());
    }

    // Tests text and hasText methods
    @Test
    public void testTextAndHasText_multipleElements_returnsJoinedText() {
        Document doc = Jsoup.parse("<div><p>Hello</p><p>World</p><p></p></div>");
        Elements ps = doc.select("p");

        assertEquals("Hello World", ps.text());
        assertTrue(ps.hasText());

        Elements empty = new Elements();
        assertEquals("", empty.text());
        assertFalse(empty.hasText());
    }

    // Tests html, outerHtml and toString methods
    @Test
    public void testHtmlAndOuterHtmlAndToString_returnsCombinedHtml() {
        Document doc = Jsoup.parse("<div><p><span>1</span></p><p><span>2</span></p></div>");
        Elements ps = doc.select("p");

        assertEquals("<span>1</span>\n<span>2</span>", ps.html());
        assertEquals("<p><span>1</span></p>\n<p><span>2</span></p>", ps.outerHtml());
        assertEquals(ps.outerHtml(), ps.toString());
    }

    // Tests tagName update across all elements
    @Test
    public void testTagName_updatesTagNameForAllElements() {
        Document doc = Jsoup.parse("<div><i>1</i><i>2</i></div>");
        Elements is = doc.select("i");

        is.tagName("em");
        assertEquals("<em>1</em>\n<em>2</em>", is.outerHtml());
    }

    // Tests inner HTML manipulation: html(String), prepend(String), append(String)
    @Test
    public void testHtmlPrependAppend_modifiesInnerHtml() {
        Document doc = Jsoup.parse("<div><p>Body</p></div>");
        Elements ps = doc.select("p");

        ps.prepend("<b>Start</b> ");
        ps.append(" <b>End</b>");
        assertEquals("<b>Start</b> Body <b>End</b>", ps.html());

        ps.html("New Content");
        assertEquals("New Content", ps.html());
    }

    // Tests DOM insertion methods: before(String), after(String)
    @Test
    public void testBeforeAndAfter_insertsSiblingHtml() {
        Document doc = Jsoup.parse("<div><p>Middle</p></div>");
        Elements ps = doc.select("p");

        ps.before("<span>Before</span>");
        ps.after("<span>After</span>");
        assertEquals("<span>Before</span><p>Middle</p><span>After</span>", doc.body().children().first().html().replace("\n", ""));
    }

    // Tests wrap and unwrap methods
    @Test
    public void testWrapAndUnwrap_wrapsAndUnwrapsElements() {
        Document doc = Jsoup.parse("<div><b>One</b><b>Two</b></div>");
        Elements bs = doc.select("b");

        bs.wrap("<i></i>");
        assertEquals("<div><i><b>One</b></i><i><b>Two</b></i></div>", doc.body().html().replace("\n", ""));

        bs.unwrap();
        assertEquals("<div><i>One</i><i>Two</i></div>", doc.body().html().replace("\n", ""));
    }

    // Tests wrap with empty string throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testWrap_emptyString_throwsIllegalArgumentException() {
        Document doc = Jsoup.parse("<div><p>Text</p></div>");
        Elements ps = doc.select("p");
        ps.wrap("");
    }

    // Tests empty and remove methods on elements
    @Test
    public void testEmptyAndRemove_modifiesDOM() {
        Document doc = Jsoup.parse("<div><p><span>1</span></p><p><span>2</span></p></div>");
        Elements ps = doc.select("p");

        ps.empty();
        assertEquals("<p></p>\n<p></p>", ps.outerHtml());

        ps.remove();
        assertEquals("<div></div>", doc.body().html().replace("\n", ""));
    }

    // Tests select, not, and is filter methods
    @Test
    public void testSelectAndNotAndIs_filtersElementsCorrectly() {
        Document doc = Jsoup.parse("<div><p class='a'>1</p><p class='b'>2</p><p class='a b'>3</p></div>");
        Elements ps = doc.select("p");

        assertTrue(ps.is(".a"));
        assertFalse(ps.is(".c"));

        Elements filtered = ps.not(".a");
        assertEquals(1, filtered.size());
        assertEquals("2", filtered.first().text());

        Elements selected = ps.select(".b");
        assertEquals(2, selected.size());
    }

    // Tests eq method with valid and invalid indices
    @Test
    public void testEq_validAndInvalidIndex_returnsCorrectElements() {
        Document doc = Jsoup.parse("<p>0</p><p>1</p><p>2</p>");
        Elements ps = doc.select("p");

        Elements eq1 = ps.eq(1);
        assertEquals(1, eq1.size());
        assertEquals("1", eq1.first().text());

        Elements eqOut = ps.eq(5);
        assertTrue(eqOut.isEmpty());
    }

    // Tests parents method returns ancestor elements
    @Test
    public void testParents_returnsUniqueAncestors() {
        Document doc = Jsoup.parse("<div><section><p><span>Text</span></p></section></div>");
        Elements spans = doc.select("span");
        Elements parents = spans.parents();

        assertTrue(parents.contains(doc.select("p").first()));
        assertTrue(parents.contains(doc.select("section").first()));
        assertTrue(parents.contains(doc.select("div").first()));
    }

    // Tests first and last on populated and empty Elements
    @Test
    public void testFirstAndLast_populatedAndEmpty_returnsElementsOrNull() {
        Document doc = Jsoup.parse("<p>First</p><p>Middle</p><p>Last</p>");
        Elements ps = doc.select("p");

        assertNotNull(ps.first());
        assertEquals("First", ps.first().text());
        assertNotNull(ps.last());
        assertEquals("Last", ps.last().text());

        Elements empty = new Elements();
        assertNull(empty.first());
        assertNull(empty.last());
    }

    // Tests clone creates deep copy of elements
    @Test
    public void testClone_createsDeepCopyOfElements() {
        Document doc = Jsoup.parse("<div><p>Original</p></div>");
        Elements ps = doc.select("p");
        Elements cloned = ps.clone();

        assertEquals(ps.size(), cloned.size());
        assertEquals(ps.text(), cloned.text());

        cloned.get(0).text("Modified");
        assertEquals("Original", ps.get(0).text());
        assertEquals("Modified", cloned.get(0).text());
    }

    // Tests traverse depth-first visitor on elements
    @Test
    public void testTraverse_visitsAllElements() {
        Document doc = Jsoup.parse("<div><p>1</p><p>2</p></div>");
        Elements ps = doc.select("p");

        final List<String> visited = new ArrayList<String>();
        ps.traverse(new NodeVisitor() {
            public void head(Node node, int depth) {
                if (node instanceof Element) {
                    visited.add(((Element) node).tagName());
                }
            }

            public void tail(Node node, int depth) {}
        });

        assertEquals(2, visited.size());
        assertEquals("p", visited.get(0));
        assertEquals("p", visited.get(1));
    }

    // Tests traverse with null throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testTraverse_nullVisitor_throwsIllegalArgumentException() {
        Elements elements = new Elements();
        elements.traverse(null);
    }
}