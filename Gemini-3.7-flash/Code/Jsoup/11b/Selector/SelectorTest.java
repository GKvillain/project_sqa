package org.jsoup.select;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SelectorTest {

    private Document doc;

    @Before
    public void setUp() {
        String html = "<div id='main' class='content wrap'>"
                + "<h1 title='Header' data-topic='news'>Headline</h1>"
                + "<p id='p1' class='text' data-ref='123'>First <span>inner</span> paragraph.</p>"
                + "<p id='p2' class='text lead' custom='abc'>Second paragraph 123.</p>"
                + "<a href='http://example.com/test.png' rel='nofollow'>Link 1</a>"
                + "<a href='https://example.org/search/item' rel='external'>Link 2</a>"
                + "<ol id='list'>"
                + "<li class='item' val='one'>Item 1</li>"
                + "<li class='item' val='two'>Item 2</li>"
                + "<li class='item' val='three'>Item 3</li>"
                + "</ol>"
                + "<fb:name>Facebook Tag</fb:name>"
                + "</div>";
        doc = Jsoup.parse(html);
    }

    // Tests tag name and namespaced tag selection
    @Test
    public void testSelect_byTag_returnsMatchingElements() {
        Elements ps = Selector.select("p", doc);
        assertEquals(2, ps.size());
        assertEquals("p1", ps.get(0).id());
        assertEquals("p2", ps.get(1).id());

        Elements fb = Selector.select("fb|name", doc);
        assertEquals(1, fb.size());
        assertEquals("Facebook Tag", fb.first().text());
    }

    // Tests ID and class name selector
    @Test
    public void testSelect_byIdAndClass_returnsMatchingElements() {
        Elements byId = Selector.select("#p1", doc);
        assertEquals(1, byId.size());
        assertEquals("p1", byId.first().id());

        Elements byClass = Selector.select(".text", doc);
        assertEquals(2, byClass.size());

        Elements chained = Selector.select("p.text.lead#p2", doc);
        assertEquals(1, chained.size());
        assertEquals("p2", chained.first().id());
    }

    // Tests universal selector (*)
    @Test
    public void testSelect_universalSelector_returnsAllDescendants() {
        Elements all = Selector.select("*", doc);
        assertTrue(all.size() > 5);
        Elements divAll = Selector.select("div#main *", doc);
        assertTrue(divAll.size() >= 7);
    }

    // Tests attribute presence and prefix matching ([attr], [^attrPrefix])
    @Test
    public void testSelect_byAttributePresenceAndPrefix_returnsMatchingElements() {
        Elements titled = Selector.select("[title]", doc);
        assertEquals(1, titled.size());
        assertEquals("h1", titled.first().tagName());

        Elements dataAttrs = Selector.select("[^data-]", doc);
        assertEquals(2, dataAttrs.size());
    }

    // Tests attribute value comparison operators (=, !=, ^=, $=, *=, ~=)
    @Test
    public void testSelect_byAttributeValueOperators_returnsMatchingElements() {
        Elements eq = Selector.select("[rel=nofollow]", doc);
        assertEquals(1, eq.size());

        Elements notEq = Selector.select("a[rel!=nofollow]", doc);
        assertEquals(1, notEq.size());
        assertEquals("Link 2", notEq.first().text());

        Elements prefix = Selector.select("[href^=http:]", doc);
        assertEquals(1, prefix.size());

        Elements suffix = Selector.select("[href$=.png]", doc);
        assertEquals(1, suffix.size());

        Elements contains = Selector.select("[href*=/search/]", doc);
        assertEquals(1, contains.size());

        Elements regex = Selector.select("[href~=(?i)\\.png]", doc);
        assertEquals(1, regex.size());
    }

    // Tests combinators: child (>), descendant ( ), adjacent sibling (+), general sibling (~)
    @Test
    public void testSelect_combinators_returnsMatchingElements() {
        Elements child = Selector.select("ol#list > li", doc);
        assertEquals(3, child.size());

        Elements descendant = Selector.select("div.content span", doc);
        assertEquals(1, descendant.size());
        assertEquals("inner", descendant.first().text());

        Elements adjacent = Selector.select("h1 + p", doc);
        assertEquals(1, adjacent.size());
        assertEquals("p1", adjacent.first().id());

        Elements sibling = Selector.select("h1 ~ ol", doc);
        assertEquals(1, sibling.size());
        assertEquals("list", sibling.first().id());
    }

    // Tests comma-separated union query
    @Test
    public void testSelect_unionQuery_returnsMergedElements() {
        Elements union = Selector.select("h1, ol#list", doc);
        assertEquals(2, union.size());
        assertEquals("h1", union.get(0).tagName());
        assertEquals("ol", union.get(1).tagName());
    }

    // Tests index-based pseudo selectors (:lt, :gt, :eq)
    @Test
    public void testSelect_indexPseudoSelectors_returnsFilteredByIndex() {
        Elements lt = Selector.select("li:lt(2)", doc);
        assertEquals(2, lt.size());
        assertEquals("Item 1", lt.get(0).text());
        assertEquals("Item 2", lt.get(1).text());

        Elements gt = Selector.select("li:gt(1)", doc);
        assertEquals(1, gt.size());
        assertEquals("Item 3", gt.get(0).text());

        Elements eq = Selector.select("li:eq(1)", doc);
        assertEquals(1, eq.size());
        assertEquals("Item 2", eq.first().text());
    }

    // Tests :has(selector) pseudo selector
    @Test
    public void testSelect_hasPseudoSelector_returnsParentsOfMatchingDescendants() {
        Elements hasSpan = Selector.select("p:has(span)", doc);
        assertEquals(1, hasSpan.size());
        assertEquals("p1", hasSpan.first().id());

        Elements hasLi = Selector.select("div:has(ol > li)", doc);
        assertEquals(1, hasLi.size());
        assertEquals("main", hasLi.first().id());
    }

    // Tests :contains and :containsOwn pseudo selectors
    @Test
    public void testSelect_containsPseudoSelectors_matchesText() {
        Elements contains = Selector.select("p:contains(inner)", doc);
        assertEquals(1, contains.size());
        assertEquals("p1", contains.first().id());

        Elements containsOwn = Selector.select("p:containsOwn(inner)", doc);
        assertEquals(0, containsOwn.size());

        Elements containsOwnDirect = Selector.select("p:containsOwn(First)", doc);
        assertEquals(1, containsOwnDirect.size());
        assertEquals("p1", containsOwnDirect.first().id());
    }

    // Tests :matches and :matchesOwn pseudo selectors
    @Test
    public void testSelect_matchesPseudoSelectors_matchesRegex() {
        Elements matches = Selector.select("p:matches(\\d+)", doc);
        assertEquals(1, matches.size());
        assertEquals("p2", matches.first().id());

        Elements matchesOwn = Selector.select("p:matchesOwn(^Second.*\\d+\\.$)", doc);
        assertEquals(1, matchesOwn.size());
        assertEquals("p2", matchesOwn.first().id());
    }

    // Tests :not(selector) pseudo selector (related to Jsoup-11 defect)
    @Test
    public void testSelect_notPseudoSelector_returnsElementsNotMatching() {
        Elements notLead = Selector.select("p:not(.lead)", doc);
        assertEquals(1, notLead.size());
        assertEquals("p1", notLead.first().id());

        Elements notDiv = Selector.select("div#main > :not(p)", doc);
        assertTrue(notDiv.size() > 0);
    }

    // Tests select over multiple root elements
    @Test
    public void testSelect_multipleRoots_returnsElementsFromAllRoots() {
        List<Element> roots = new ArrayList<Element>();
        roots.add(doc.getElementById("p1"));
        roots.add(doc.getElementById("p2"));

        Elements found = Selector.select("span", roots);
        assertEquals(1, found.size());
        assertEquals("inner", found.first().text());
    }

    // Tests leading combinator starting from root
    @Test
    public void testSelect_queryStartingWithCombinator_usesRoot() {
        Element list = doc.getElementById("list");
        Elements items = Selector.select("> li", list);
        assertEquals(3, items.size());
    }

    // Tests invalid query syntax throwing SelectorParseException
    @Test(expected = Selector.SelectorParseException.class)
    public void testSelect_invalidQuery_throwsSelectorParseException() {
        Selector.select("div[[title]]", doc);
    }

    // Tests unhandled token throwing SelectorParseException
    @Test(expected = Selector.SelectorParseException.class)
    public void testSelect_unhandledToken_throwsSelectorParseException() {
        Selector.select("???", doc);
    }

    // Tests invalid index format throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testSelect_nonNumericIndex_throwsIllegalArgumentException() {
        Selector.select("li:eq(abc)", doc);
    }

    // Tests null query throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testSelect_nullQuery_throwsIllegalArgumentException() {
        Selector.select(null, doc);
    }

    // Tests empty query throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testSelect_emptyQuery_throwsIllegalArgumentException() {
        Selector.select("   ", doc);
    }

    // Tests null root throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testSelect_nullRoot_throwsIllegalArgumentException() {
        Selector.select("div", (Element) null);
    }
}