package org.jsoup.select;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Tag;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import static org.junit.Assert.*;

public class ElementsTest {

    // Tests empty constructor and boundary methods on empty elements
    @Test
    public void testEmptyList_noElements_returnsDefaultValues() {
        Elements empty = new Elements();
        assertEquals(0, empty.size());
        assertTrue(empty.isEmpty());
        assertNull(empty.first());
        assertNull(empty.last());
        assertEquals("", empty.attr("class"));
        assertFalse(empty.hasAttr("class"));
        assertEquals("", empty.val());
        assertEquals("", empty.text());
        assertFalse(empty.hasText());
        assertEquals("", empty.html());
        assertEquals("", empty.outerHtml());
        assertEquals("", empty.toString());
        assertEquals(0, empty.parents().size());
        assertEquals(0, empty.eq(0).size());
        assertFalse(empty.is("div"));
    }

    // Tests attribute retrieval, setting, checking, and removal
    @Test
    public void testAttrMethods_variousOperations_modifiesAndReturnsAttributes() {
        Document doc = Jsoup.parse("<div id='1' class='main'></div><div id='2'></div>");
        Elements divs = doc.select("div");

        assertTrue(divs.hasAttr("id"));
        assertFalse(divs.hasAttr("title"));
        assertEquals("1", divs.attr("id"));

        divs.attr("title", "testTitle");
        assertEquals("testTitle", divs.get(0).attr("title"));
        assertEquals("testTitle", divs.get(1).attr("title"));

        divs.removeAttr("title");
        assertFalse(divs.hasAttr("title"));
    }

    // Tests class operations: add, remove, toggle, has
    @Test
    public void testClassMethods_addRemoveToggle_updatesClassesCorrectly() {
        Document doc = Jsoup.parse("<div class='c1'></div><div></div>");
        Elements divs = doc.select("div");

        assertTrue(divs.hasClass("c1"));
        assertFalse(divs.hasClass("c2"));

        divs.addClass("c2");
        assertTrue(divs.get(0).hasClass("c2"));
        assertTrue(divs.get(1).hasClass("c2"));

        divs.removeClass("c1");
        assertFalse(divs.get(0).hasClass("c1"));

        divs.toggleClass("toggle");
        assertTrue(divs.get(0).hasClass("toggle"));
        assertTrue(divs.get(1).hasClass("toggle"));

        divs.toggleClass("toggle");
        assertFalse(divs.get(0).hasClass("toggle"));
        assertFalse(divs.get(1).hasClass("toggle"));
    }

    // Tests form val() get and set
    @Test
    public void testVal_getAndSet_updatesValues() {
        Document doc = Jsoup.parse("<input value='one'/><input value='two'/>");
        Elements inputs = doc.select("input");

        assertEquals("one", inputs.val());

        inputs.val("three");
        assertEquals("three", inputs.get(0).val());
        assertEquals("three", inputs.get(1).val());
    }

    // Tests text() and hasText()
    @Test
    public void testTextAndHasText_withTextContent_returnsCombinedText() {
        Document doc = Jsoup.parse("<p>Hello</p><p>World</p><p></p>");
        Elements ps = doc.select("p");

        assertTrue(ps.hasText());
        assertEquals("Hello World", ps.text());

        Elements emptyPs = doc.select("p:empty");
        assertFalse(emptyPs.hasText());
        assertEquals("", emptyPs.text());
    }

    // Tests html(), outerHtml(), and toString()
    @Test
    public void testHtmlAndOuterHtml_multipleElements_joinsWithNewlines() {
        Document doc = Jsoup.parse("<div><span>1</span></div><div><span>2</span></div>");
        Elements divs = doc.select("div");

        assertEquals("<span>1</span>\n<span>2</span>", divs.html());
        assertEquals("<div>\n <span>1</span>\n</div>\n<div>\n <span>2</span>\n</div>", divs.outerHtml());
        assertEquals(divs.outerHtml(), divs.toString());
    }

    // Tests tagName modification
    @Test
    public void testTagName_changeTag_updatesAllElements() {
        Document doc = Jsoup.parse("<i>1</i><i>2</i>");
        Elements italic = doc.select("i");

        italic.tagName("em");
        assertEquals("<em>1</em>\n<em>2</em>", italic.outerHtml());
    }

    // Tests html(String), prepend, append, before, after DOM manipulations
    @Test
    public void testDomManipulations_contentAndPositioning_updatesDom() {
        Document doc = Jsoup.parse("<div id='d1'>Text</div><div id='d2'>Text</div>");
        Elements divs = doc.select("div");

        divs.html("<b>Inner</b>");
        assertEquals("<b>Inner</b>", divs.get(0).html());

        divs.prepend("<span>Pre</span>");
        assertEquals("<span>Pre</span><b>Inner</b>", divs.get(0).html());

        divs.append("<span>Post</span>");
        assertEquals("<span>Pre</span><b>Inner</b><span>Post</span>", divs.get(0).html());

        divs.before("<p>Before</p>");
        assertEquals(2, doc.select("p").size());

        divs.after("<hr />");
        assertEquals(2, doc.select("hr").size());
    }

    // Tests wrap() and unwrap()
    @Test
    public void testWrapAndUnwrap_elements_wrapsAndUnwrapsCorrectly() {
        Document doc = Jsoup.parse("<p><b>1</b></p><p><b>2</b></p>");
        Elements b = doc.select("b");

        b.wrap("<i class='wrap'></i>");
        assertEquals("<i class=\"wrap\"><b>1</b></i>", doc.select("p").first().html());

        doc.select("b").unwrap();
        assertEquals("<i class=\"wrap\">1</i>", doc.select("p").first().html());
    }

    // Tests wrap with empty html throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testWrap_emptyHtml_throwsException() {
        Document doc = Jsoup.parse("<p>1</p>");
        doc.select("p").wrap("");
    }

    // Tests empty() and remove()
    @Test
    public void testEmptyAndRemove_modifiesDom_clearsOrRemovesElements() {
        Document doc = Jsoup.parse("<div><p>1</p><p>2</p></div>");
        Elements ps = doc.select("p");

        ps.empty();
        assertEquals("<p></p>\n<p></p>", ps.outerHtml());

        ps.remove();
        assertEquals(0, doc.select("p").size());
        assertEquals("<div></div>", doc.body().html());
    }

    // Tests select, not, and is query filters
    @Test
    public void testFilters_selectNotIs_filtersElementsCorrectly() {
        Document doc = Jsoup.parse("<p class='a'>1</p><p class='b'>2</p><p class='a b'>3</p>");
        Elements ps = doc.select("p");

        Elements aOnly = ps.select(".a");
        assertEquals(2, aOnly.size());

        Elements notA = ps.not(".a");
        assertEquals(1, notA.size());
        assertEquals("2", notA.first().text());

        assertTrue(ps.is(".b"));
        assertFalse(ps.is(".nonexistent"));
    }

    // Tests eq(index) within and outside boundary
    @Test
    public void testEq_validAndInvalidIndex_returnsExpectedElements() {
        Document doc = Jsoup.parse("<p>0</p><p>1</p><p>2</p>");
        Elements ps = doc.select("p");

        Elements eq0 = ps.eq(0);
        assertEquals(1, eq0.size());
        assertEquals("0", eq0.first().text());

        Elements eq2 = ps.eq(2);
        assertEquals(1, eq2.size());
        assertEquals("2", eq2.first().text());

        Elements eq3 = ps.eq(3);
        assertEquals(0, eq3.size());

        Elements eqNegative = ps.eq(-1);
        assertEquals(0, eqNegative.size());
    }

    // Tests parents() method collecting ancestor elements
    @Test
    public void testParents_nestedElements_returnsUniqueAncestors() {
        Document doc = Jsoup.parse("<div><span><em>1</em></span><span><em>2</em></span></div>");
        Elements ems = doc.select("em");

        Elements parents = ems.parents();
        assertTrue(parents.contains(doc.select("div").first()));
        assertEquals(2, parents.select("span").size());
    }

    // Tests first() and last()
    @Test
    public void testFirstAndLast_populatedList_returnsFirstAndLastElement() {
        Document doc = Jsoup.parse("<p>1</p><p>2</p><p>3</p>");
        Elements ps = doc.select("p");

        assertEquals("1", ps.first().text());
        assertEquals("3", ps.last().text());
    }

    // Tests clone() creating deep copies
    @Test
    public void testClone_deepCopy_independentOfOriginal() {
        Document doc = Jsoup.parse("<p class='original'>Text</p>");
        Elements original = doc.select("p");
        Elements cloned = original.clone();

        assertEquals(1, cloned.size());
        cloned.attr("class", "cloned");

        assertEquals("original", original.first().attr("class"));
        assertEquals("cloned", cloned.first().attr("class"));
    }

    // Tests traverse() with NodeVisitor
    @Test
    public void testTraverse_nodeVisitor_visitsNodes() {
        Document doc = Jsoup.parse("<div><p>Hello</p></div>");
        Elements divs = doc.select("div");
        final List<String> visited = new ArrayList<String>();

        divs.traverse(new NodeVisitor() {
            public void head(Node node, int depth) {
                visited.add("head:" + node.nodeName());
            }

            public void tail(Node node, int depth) {
                visited.add("tail:" + node.nodeName());
            }
        });

        assertTrue(visited.contains("head:div"));
        assertTrue(visited.contains("head:p"));
        assertTrue(visited.contains("head:#text"));
        assertTrue(visited.contains("tail:div"));
    }

    // Tests traverse with null throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testTraverse_nullVisitor_throwsException() {
        Elements els = new Elements();
        els.traverse(null);
    }

    // Tests constructors and collection delegate methods
    @Test
    public void testConstructorsAndListDelegates_variousOperations_behavesAsList() {
        Element el1 = new Element(Tag.valueOf("div"), "");
        Element el2 = new Element(Tag.valueOf("span"), "");

        Elements varargs = new Elements(el1, el2);
        assertEquals(2, varargs.size());
        assertEquals(el1, varargs.get(0));
        assertEquals(el2, varargs.get(1));
        assertEquals(0, varargs.indexOf(el1));
        assertEquals(1, varargs.lastIndexOf(el2));

        Element el3 = new Element(Tag.valueOf("p"), "");
        varargs.add(el3);
        assertEquals(3, varargs.size());

        varargs.remove(el3);
        assertEquals(2, varargs.size());
        assertFalse(varargs.contains(el3));

        Object[] array = varargs.toArray();
        assertEquals(2, array.length);

        Element[] typedArray = varargs.toArray(new Element[0]);
        assertEquals(2, typedArray.length);

        Elements cloneSubList = new Elements(varargs.subList(0, 1));
        assertEquals(1, cloneSubList.size());
    }

    // Additional coverage tests for remaining constructors and collection delegate methods
    @Test
    public void testInitialCapacityConstructorAndCollectionConstructors_initializesCorrectly() {
        Elements sized = new Elements(10);
        assertEquals(0, sized.size());

        Element el1 = new Element(Tag.valueOf("div"), "");
        Element el2 = new Element(Tag.valueOf("span"), "");
        List<Element> list = Arrays.asList(el1, el2);

        Elements fromList = new Elements(list);
        assertEquals(2, fromList.size());
        assertEquals(el1, fromList.get(0));
        assertEquals(el2, fromList.get(1));
    }

    @Test
    public void testListMutationsAndIterators_supportsStandardListOperations() {
        Element el1 = new Element(Tag.valueOf("div"), "");
        Element el2 = new Element(Tag.valueOf("span"), "");
        Element el3 = new Element(Tag.valueOf("p"), "");
        Element el4 = new Element(Tag.valueOf("b"), "");

        Elements els = new Elements();
        els.add(el1);
        els.add(0, el2);
        assertEquals(el2, els.get(0));
        assertEquals(el1, els.get(1));

        Element previous = els.set(1, el3);
        assertEquals(el1, previous);
        assertEquals(el3, els.get(1));

        Element removed = els.remove(0);
        assertEquals(el2, removed);
        assertEquals(1, els.size());

        List<Element> additions = Arrays.asList(el2, el4);
        els.addAll(additions);
        assertEquals(3, els.size());
        assertTrue(els.containsAll(additions));

        els.addAll(0, Arrays.asList(el1));
        assertEquals(4, els.size());
        assertEquals(el1, els.get(0));

        // Iterator tests
        Iterator<Element> it = els.iterator();
        assertTrue(it.hasNext());
        assertEquals(el1, it.next());

        // ListIterator tests
        ListIterator<Element> listIt = els.listIterator();
        assertTrue(listIt.hasNext());
        assertEquals(el1, listIt.next());

        ListIterator<Element> listItIndex = els.listIterator(1);
        assertTrue(listItIndex.hasNext());
        assertEquals(el3, listItIndex.next());

        // retainAll and removeAll
        els.retainAll(Arrays.asList(el1, el4));
        assertEquals(2, els.size());
        assertTrue(els.contains(el1));
        assertTrue(els.contains(el4));

        els.removeAll(Arrays.asList(el1));
        assertEquals(1, els.size());
        assertFalse(els.contains(el1));

        els.clear();
        assertEquals(0, els.size());
        assertTrue(els.isEmpty());
    }

    @Test
    public void testEqualsAndHashCode_listComparison_worksAsExpected() {
        Element el1 = new Element(Tag.valueOf("div"), "");
        Element el2 = new Element(Tag.valueOf("span"), "");

        Elements els1 = new Elements(el1, el2);
        Elements els2 = new Elements(el1, el2);
        Elements els3 = new Elements(el1);

        assertEquals(els1, els2);
        assertEquals(els1.hashCode(), els2.hashCode());
        assertFalse(els1.equals(els3));
        assertFalse(els1.equals("other"));
        assertFalse(els1.equals(null));
    }
}