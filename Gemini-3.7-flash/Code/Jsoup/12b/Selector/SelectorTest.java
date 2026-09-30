package org.jsoup.select;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class SelectorTest {

    private Document doc;

    @Before
    public void setUp() {
        String html = "<div id='main' class='container'>"
                + "<p class='intro' title='first-p'>Hello <span>World</span></p>"
                + "<p id='p2' class='body-text' data-ref='123' data-type='text'>Second paragraph</p>"
                + "<div class='content'>"
                + "<a href='http://example.com/test' rel='nofollow'>Link 1</a>"
                + "<a href='https://example.com/secure'>Link 2</a>"
                + "<img src='image.png' width='500' alt='sample image' />"
                + "<fb:name>Namespace test</fb:name>"
                + "</div>"
                + "<ul id='list'>"
                + "<li class='item'>Item 1</li>"
                + "<li class='item active'>Item 2</li>"
                + "<li class='item'>Item 3</li>"
                + "</ul>"
                + "</div>";
        doc = Jsoup.parse(html);
    }

    // Tests selecting by tag name
    @Test
    public void testSelect_byTag_returnsMatchingElements() {
        Elements els = Selector.select("p", doc);
        assertEquals(2, els.size());
        assertEquals("intro", els.get(0).className());
    }

    // Tests selecting by element ID
    @Test
    public void testSelect_byId_returnsMatchingElement() {
        Elements els = Selector.select("#p2", doc);
        assertEquals(1, els.size());
        assertEquals("p2", els.first().id());
    }

    // Tests selecting by CSS class
    @Test
    public void testSelect_byClass_returnsMatchingElements() {
        Elements els = Selector.select(".item", doc);
        assertEquals(3, els.size());
    }

    // Tests selecting with universal selector
    @Test
    public void testSelect_universalSelector_returnsAllElements() {
        Elements all = Selector.select("*", doc);
        assertTrue(all.size() > 5);
    }

    // Tests selecting with tag and class combination
    @Test
    public void testSelect_tagAndClass_returnsMatchingElements() {
        Elements els = Selector.select("li.active", doc);
        assertEquals(1, els.size());
        assertEquals("Item 2", els.first().text());
    }

    // Tests direct child combinator (>)
    @Test
    public void testSelect_directChildCombinator_returnsMatchingChildren() {
        Elements els = Selector.select("ul#list > li", doc);
        assertEquals(3, els.size());
    }

    // Tests descendant combinator (space)
    @Test
    public void testSelect_descendantCombinator_returnsMatchingDescendants() {
        Elements els = Selector.select("div.container a", doc);
        assertEquals(2, els.size());
    }

    // Tests adjacent sibling combinator (+)
    @Test
    public void testSelect_adjacentSiblingCombinator_returnsNextSibling() {
        Elements els = Selector.select("li.item + li.active", doc);
        assertEquals(1, els.size());
        assertEquals("Item 2", els.first().text());
    }

    // Tests general sibling combinator (~)
    @Test
    public void testSelect_generalSiblingCombinator_returnsSubsequentSiblings() {
        Elements els = Selector.select("p.intro ~ p", doc);
        assertEquals(1, els.size());
        assertEquals("p2", els.first().id());
    }

    // Tests group selector (comma)
    @Test
    public void testSelect_groupSelector_returnsUnionOfElements() {
        Elements els = Selector.select("p.intro, li.active", doc);
        assertEquals(2, els.size());
    }

    // Tests attribute prefix wildcard ([^attrPrefix])
    @Test
    public void testSelect_attributePrefix_returnsMatchingElements() {
        Elements els = Selector.select("[^data-]", doc);
        assertEquals(1, els.size());
        assertEquals("p2", els.first().id());
    }

    // Tests attribute existence and value selectors (=, ^=, $=, *=, !=, ~=)
    @Test
    public void testSelect_attributeValueOperators_returnsMatchingElements() {
        assertEquals(1, Selector.select("a[rel=nofollow]", doc).size());
        assertEquals(1, Selector.select("a[href^=https]", doc).size());
        assertEquals(1, Selector.select("img[src$=.png]", doc).size());
        assertEquals(2, Selector.select("a[href*=/test], a[href*=/secure]", doc).size());
        assertEquals(1, Selector.select("img[alt~=(?i)sample]", doc).size());
        assertTrue(Selector.select("p[title!=other]", doc).size() >= 1);
    }

    // Tests XML namespace selector syntax (ns|tag)
    @Test
    public void testSelect_namespacedTag_returnsMatchingElement() {
        Elements els = Selector.select("fb|name", doc);
        assertEquals(1, els.size());
        assertEquals("Namespace test", els.first().text());
    }

    // Tests pseudo selectors :lt, :gt, :eq
    @Test
    public void testSelect_indexPseudoSelectors_returnsIndexedElements() {
        Elements eq = Selector.select("li:eq(1)", doc);
        assertEquals(1, eq.size());
        assertEquals("Item 2", eq.first().text());

        Elements lt = Selector.select("li:lt(2)", doc);
        assertEquals(2, lt.size());

        Elements gt = Selector.select("li:gt(1)", doc);
        assertEquals(1, gt.size());
        assertEquals("Item 3", gt.first().text());
    }

    // Tests structural pseudo selector :has
    @Test
    public void testSelect_hasPseudo_returnsParentsMatchingDescendantQuery() {
        Elements els = Selector.select("div:has(img[src$=.png])", doc);
        assertTrue(els.size() >= 1);
        assertTrue(els.select("img").size() >= 1);
    }

    // Tests text matching pseudo selectors (:contains, :containsOwn, :matches, :matchesOwn)
    @Test
    public void testSelect_textPseudoSelectors_returnsMatchingElements() {
        Elements contains = Selector.select("p:contains(World)", doc);
        assertEquals(1, contains.size());

        Elements containsOwn = Selector.select("p:containsOwn(Hello)", doc);
        assertEquals(1, containsOwn.size());

        Elements matches = Selector.select("li:matches(^Item \\d+$)", doc);
        assertEquals(3, matches.size());

        Elements matchesOwn = Selector.select("span:matchesOwn(World)", doc);
        assertEquals(1, matchesOwn.size());
    }

    // Tests exclusion pseudo selector :not
    @Test
    public void testSelect_notPseudo_filtersOutMatchingElements() {
        Elements els = Selector.select("li:not(.active)", doc);
        assertEquals(2, els.size());
    }

    // Tests multiple root elements iteration
    @Test
    public void testSelect_multipleRoots_returnsCombinedResults() {
        List<Element> roots = new ArrayList<Element>();
        roots.add(doc.getElementById("main"));
        roots.add(doc.getElementById("list"));

        Elements els = Selector.select("li.item", roots);
        assertEquals(3, els.size());
    }

    // Tests invalid selector syntax throwing SelectorParseException
    @Test(expected = Selector.SelectorParseException.class)
    public void testSelect_invalidQuery_throwsSelectorParseException() {
        Selector.select("div[==]", doc);
    }

    // Tests empty index argument for index selector throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testSelect_nonNumericIndex_throwsException() {
        Selector.select("li:eq(abc)", doc);
    }
}