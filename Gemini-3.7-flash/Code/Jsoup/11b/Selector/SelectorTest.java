package org.jsoup.select;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class SelectorTest {

    private Document doc;

    @Before
    public void setUp() {
        String html = "<div id='main' class='content'>"
                + "<p class='intro' title='greeting'>Hello <span>World</span></p>"
                + "<p class='body' title='text' data-info='p2'>Jsoup test 123</p>"
                + "<ul id='list'>"
                + "<li class='item' data-val='a'>One</li>"
                + "<li class='item active' data-val='b'>Two</li>"
                + "<li class='item' data-val='c'>Three</li>"
                + "</ul>"
                + "<div><a href='http://example.com/search/1'>Link 1</a><a href='https://example.org/img.png'>Link 2</a></div>"
                + "</div>";
        doc = Jsoup.parse(html);
    }

    // Tests selection by tag name
    @Test
    public void testSelect_byTag_returnsMatchingElements() {
        Elements ps = Selector.select("p", doc);
        assertEquals(2, ps.size());
        assertEquals("intro", ps.get(0).className());
        assertEquals("body", ps.get(1).className());
    }

    // Tests selection by ID
    @Test
    public void testSelect_byId_returnsMatchingElement() {
        Elements main = Selector.select("#main", doc);
        assertEquals(1, main.size());
        assertEquals("main", main.get(0).id());

        Elements notFound = Selector.select("#nonexistent", doc);
        assertTrue(notFound.isEmpty());
    }

    // Tests selection by CSS class
    @Test
    public void testSelect_byClass_returnsMatchingElements() {
        Elements items = Selector.select(".item", doc);
        assertEquals(3, items.size());
        assertEquals("One", items.get(0).text());
        assertEquals("Two", items.get(1).text());
        assertEquals("Three", items.get(2).text());
    }

    // Tests selection using universal wildcard selector
    @Test
    public void testSelect_universalSelector_returnsAllDescendants() {
        Element list = doc.getElementById("list");
        Elements allInList = Selector.select("*", list);
        assertEquals(4, allInList.size()); // list itself + 3 li items
    }

    // Tests attribute presence and attribute prefix matching
    @Test
    public void testSelect_byAttributePresenceAndPrefix_returnsMatching() {
        Elements withTitle = Selector.select("[title]", doc);
        assertEquals(2, withTitle.size());

        Elements withDataPrefix = Selector.select("[^data-]", doc);
        assertEquals(4, withDataPrefix.size()); // 1 p + 3 li
    }

    // Tests attribute value operators: =, !=, ^=, $=, *=, ~=
    @Test
    public void testSelect_byAttributeValueOperators_returnsMatching() {
        Elements eq = Selector.select("[data-val=b]", doc);
        assertEquals(1, eq.size());
        assertEquals("Two", eq.get(0).text());

        Elements notEq = Selector.select("li[data-val!=b]", doc);
        assertEquals(2, notEq.size());

        Elements startsWith = Selector.select("a[href^=http:]", doc);
        assertEquals(1, startsWith.size());
        assertEquals("Link 1", startsWith.get(0).text());

        Elements endsWith = Selector.select("a[href$=.png]", doc);
        assertEquals(1, endsWith.size());
        assertEquals("Link 2", endsWith.get(0).text());

        Elements contains = Selector.select("a[href*=/search/]", doc);
        assertEquals(1, contains.size());

        Elements regex = Selector.select("a[href~=(?i)\\.png]", doc);
        assertEquals(1, regex.size());
    }

    // Tests child combinator (E > F)
    @Test
    public void testSelect_childCombinator_returnsDirectChildren() {
        Elements directLis = Selector.select("ul#list > li", doc);
        assertEquals(3, directLis.size());

        Elements noDirect = Selector.select("div#main > li", doc);
        assertTrue(noDirect.isEmpty());
    }

    // Tests descendant combinator (E F)
    @Test
    public void testSelect_descendantCombinator_returnsDescendants() {
        Elements spans = Selector.select("div#main p.intro span", doc);
        assertEquals(1, spans.size());
        assertEquals("World", spans.get(0).text());
    }

    // Tests adjacent sibling combinator (E + F)
    @Test
    public void testSelect_adjacentSiblingCombinator_returnsImmediateSibling() {
        Elements nextLi = Selector.select("li.active + li", doc);
        assertEquals(1, nextLi.size());
        assertEquals("Three", nextLi.get(0).text());
    }

    // Tests general sibling combinator (E ~ F)
    @Test
    public void testSelect_generalSiblingCombinator_returnsAllFollowingSiblings() {
        Elements followingSiblings = Selector.select("li:eq(0) ~ li", doc);
        assertEquals(2, followingSiblings.size());
        assertEquals("Two", followingSiblings.get(0).text());
        assertEquals("Three", followingSiblings.get(1).text());
    }

    // Tests comma (OR) combinator (E, F)
    @Test
    public void testSelect_commaCombinator_returnsUnionOfElements() {
        Elements result = Selector.select("p.intro, li.active", doc);
        assertEquals(2, result.size());
        assertEquals("p", result.get(0).tagName());
        assertEquals("li", result.get(1).tagName());
    }

    // Tests structural index filters: :lt, :gt, :eq
    @Test
    public void testSelect_indexFilters_returnsIndexedElements() {
        Elements firstTwo = Selector.select("li:lt(2)", doc);
        assertEquals(2, firstTwo.size());
        assertEquals("One", firstTwo.get(0).text());
        assertEquals("Two", firstTwo.get(1).text());

        Elements afterFirst = Selector.select("li:gt(1)", doc);
        assertEquals(1, afterFirst.size());
        assertEquals("Three", afterFirst.get(0).text());

        Elements second = Selector.select("li:eq(1)", doc);
        assertEquals(1, second.size());
        assertEquals("Two", second.get(0).text());
    }

    // Tests text content selectors: :contains, :containsOwn, :matches, :matchesOwn
    @Test
    public void testSelect_textFilters_returnsMatchingElements() {
        Elements contains = Selector.select("p:contains(World)", doc);
        assertEquals(1, contains.size());
        assertEquals("intro", contains.get(0).className());

        Elements containsOwn = Selector.select("p:containsOwn(World)", doc);
        assertTrue(containsOwn.isEmpty());

        Elements matches = Selector.select("p:matches(\\d+)", doc);
        assertEquals(1, matches.size());
        assertEquals("body", matches.get(0).className());

        Elements matchesOwn = Selector.select("p:matchesOwn(^Jsoup)", doc);
        assertEquals(1, matchesOwn.size());
        assertEquals("body", matchesOwn.get(0).className());
    }

    // Tests :has selector attached to an element
    @Test
    public void testSelect_hasPseudoSelector_returnsParentElements() {
        Elements divs = Selector.select("div:has(p)", doc);
        assertEquals(1, divs.size());
        assertEquals("main", divs.get(0).id());
    }

    // Tests :has selector standing alone at the root
    @Test
    public void testSelect_hasPseudoSelectorAlone_returnsMatchingElements() {
        Elements elementsWithSpan = Selector.select(":has(span)", doc);
        assertFalse(elementsWithSpan.isEmpty());
        assertTrue(elementsWithSpan.contains(doc.select("p.intro").first()));
    }

    // Tests :not pseudo selector
    @Test
    public void testSelect_notPseudoSelector_returnsFilteredElements() {
        Elements notActive = Selector.select("li:not(.active)", doc);
        assertEquals(2, notActive.size());
        assertEquals("One", notActive.get(0).text());
        assertEquals("Three", notActive.get(1).text());
    }

    // Tests selecting over an Iterable of root elements
    @Test
    public void testSelect_iterableRoots_returnsCombinedElements() {
        List<Element> roots = Arrays.asList(doc.getElementById("list"), doc.getElementById("main"));
        Elements lis = Selector.select("li", roots);
        assertEquals(3, lis.size());
    }

    // Tests exception on unexpected query syntax
    @Test(expected = Selector.SelectorParseException.class)
    public void testSelect_invalidQueryToken_throwsSelectorParseException() {
        Selector.select("div % invalid", doc);
    }

    // Tests exception on non-numeric index filter
    @Test(expected = IllegalArgumentException.class)
    public void testSelect_nonNumericIndex_throwsIllegalArgumentException() {
        Selector.select("li:lt(abc)", doc);
    }

    // Tests exception on null or empty query
    @Test(expected = IllegalArgumentException.class)
    public void testSelect_emptyQuery_throwsIllegalArgumentException() {
        Selector.select("   ", doc);
    }
}