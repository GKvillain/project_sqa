package org.jsoup.select;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class SelectorTest {

    // Tests basic selection by tag, id, class, and universal selector
    @Test
    public void testSelect_basicSelectors_returnsMatchingElements() {
        String html = "<div id='d1' class='main highlight'><p id='p1' class='highlight'>Text</p><span id='s1'>Span</span></div>";
        Document doc = Jsoup.parse(html);

        Elements byTag = Selector.select("p", doc);
        assertEquals(1, byTag.size());
        assertEquals("p1", byTag.first().id());

        Elements byId = Selector.select("#d1", doc);
        assertEquals(1, byId.size());
        assertEquals("div", byId.first().tagName());

        Elements byClass = Selector.select(".highlight", doc);
        assertEquals(2, byClass.size());

        Elements all = Selector.select("*", doc);
        assertTrue(all.size() >= 4);
    }

    // Tests tag combined with class and id
    @Test
    public void testSelect_combinedTagClassAndId_returnsMatchingElement() {
        String html = "<div id='d1' class='box active'></div><div id='d2' class='box'></div>";
        Document doc = Jsoup.parse(html);

        Elements elements = Selector.select("div.box#d1", doc);
        assertEquals(1, elements.size());
        assertEquals("d1", elements.first().id());
    }

    // Tests all attribute selector variants
    @Test
    public void testSelect_attributeSelectors_returnsMatchingElements() {
        String html = "<a href='http://example.com' title='Example Title' data-val='123' rel='nofollow'></a>" +
                      "<a href='https://jsoup.org' title='Jsoup Java Library' data-val='456' rel='tag'></a>" +
                      "<img src='test.png' /><img src='test.jpg' />";
        Document doc = Jsoup.parse(html);

        assertEquals(2, Selector.select("[href]", doc).size());
        assertEquals(2, Selector.select("[^data-]", doc).size());
        assertEquals(1, Selector.select("[href=http://example.com]", doc).size());
        assertEquals(1, Selector.select("[rel!=nofollow]", doc).size());
        assertEquals(1, Selector.select("[href^=https]", doc).size());
        assertEquals(1, Selector.select("[title$=Library]", doc).size());
        assertEquals(1, Selector.select("[title*=Java]", doc).size());
        assertEquals(1, Selector.select("img[src~=(?i)\\.png]", doc).size());
    }

    // Tests combinators: descendant, child, adjacent sibling, general sibling, and comma
    @Test
    public void testSelect_combinators_returnsMatchingElements() {
        String html = "<div id='root'>" +
                      "  <p class='first'>P1</p>" +
                      "  <p class='second'>P2</p>" +
                      "  <span>Span1</span>" +
                      "  <div class='child'><p>Nested P</p></div>" +
                      "</div>";
        Document doc = Jsoup.parse(html);

        // Descendant (space)
        assertEquals(3, Selector.select("div p", doc).size());

        // Direct child (>)
        assertEquals(2, Selector.select("#root > p", doc).size());

        // Adjacent sibling (+)
        Elements adjacent = Selector.select("p.first + p", doc);
        assertEquals(1, adjacent.size());
        assertEquals("second", adjacent.first().className());

        // General sibling (~)
        Elements siblings = Selector.select("p.first ~ span", doc);
        assertEquals(1, siblings.size());
        assertEquals("Span1", siblings.first().text());

        // Group / comma (,)
        Elements group = Selector.select("span, p.first", doc);
        assertEquals(2, group.size());
    }

    // Tests pseudo index selectors: :lt(), :gt(), :eq()
    @Test
    public void testSelect_indexSelectors_returnsMatchingElements() {
        String html = "<ul><li>0</li><li>1</li><li>2</li><li>3</li><li>4</li></ul>";
        Document doc = Jsoup.parse(html);

        Elements eq = Selector.select("li:eq(2)", doc);
        assertEquals(1, eq.size());
        assertEquals("2", eq.first().text());

        Elements lt = Selector.select("li:lt(2)", doc);
        assertEquals(2, lt.size());

        Elements gt = Selector.select("li:gt(2)", doc);
        assertEquals(2, gt.size());
    }

    // Tests structural pseudo selector :has()
    @Test
    public void testSelect_hasPseudoSelector_returnsMatchingParents() {
        String html = "<div id='d1'><p><span>deep</span></p></div><div id='d2'><span>shallow</span></div>";
        Document doc = Jsoup.parse(html);

        Elements divsWithP = Selector.select("div:has(p)", doc);
        assertEquals(1, divsWithP.size());
        assertEquals("d1", divsWithP.first().id());

        Elements startsWithHas = Selector.select(":has(p)", doc);
        assertTrue(startsWithHas.contains(doc.getElementById("d1")));
    }

    // Tests :not() pseudo selector
    @Test
    public void testSelect_notPseudoSelector_returnsFilteredElements() {
        String html = "<div class='item'>1</div><div class='item skip'>2</div><div class='item'>3</div>";
        Document doc = Jsoup.parse(html);

        Elements notSkip = Selector.select("div.item:not(.skip)", doc);
        assertEquals(2, notSkip.size());
        assertEquals("1", notSkip.get(0).text());
        assertEquals("3", notSkip.get(1).text());
    }

    // Tests text matching pseudo selectors: :contains, :containsOwn, :matches, :matchesOwn
    @Test
    public void testSelect_textAndRegexPseudoSelectors_returnsMatchingElements() {
        String html = "<div id='p'>Hello <span>World</span> 123</div><div id='other'>Goodbye</div>";
        Document doc = Jsoup.parse(html);

        Elements contains = Selector.select(":contains(world)", doc);
        assertTrue(contains.contains(doc.getElementById("p")));

        Elements containsOwn = Selector.select("div:containsOwn(Hello)", doc);
        assertEquals(1, containsOwn.size());
        assertEquals("p", containsOwn.first().id());

        Elements matches = Selector.select(":matches(\\d+)", doc);
        assertTrue(matches.contains(doc.getElementById("p")));

        Elements matchesOwn = Selector.select("div:matchesOwn(Hello.*123)", doc);
        assertEquals(1, matchesOwn.size());
        assertEquals("p", matchesOwn.first().id());
    }

    // Tests selecting with namespace prefix
    @Test
    public void testSelect_namespacedTag_returnsMatchingElements() {
        String html = "<fb:name id='fb1'>Facebook Name</fb:name><other id='o1'>Other</other>";
        Document doc = Jsoup.parse(html);

        Elements namespaced = Selector.select("fb|name", doc);
        assertEquals(1, namespaced.size());
        assertEquals("fb1", namespaced.first().id());
    }

    // Tests select method with Iterable<Element> roots
    @Test
    public void testSelect_iterableRoots_returnsCombinedMatches() {
        String html = "<div id='d1'><p class='t'>One</p></div><div id='d2'><p class='t'>Two</p></div>";
        Document doc = Jsoup.parse(html);

        List<Element> roots = Arrays.asList(doc.getElementById("d1"), doc.getElementById("d2"));
        Elements result = Selector.select("p.t", roots);
        assertEquals(2, result.size());
    }

    // Tests root element starting with a combinator
    @Test
    public void testSelect_combinatorAtStart_queriesFromRoot() {
        String html = "<div id='parent'><p>1</p><p>2</p></div>";
        Element parent = Jsoup.parse(html).getElementById("parent");

        Elements children = Selector.select("> p", parent);
        assertEquals(2, children.size());
    }

    // Tests invalid query syntax throwing SelectorParseException
    @Test(expected = Selector.SelectorParseException.class)
    public void testSelect_unexpectedToken_throwsSelectorParseException() {
        Document doc = Jsoup.parse("<div></div>");
        Selector.select("div[invalid~?val]", doc);
    }

    // Tests invalid index in :eq() throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testSelect_nonNumericIndex_throwsException() {
        Document doc = Jsoup.parse("<div><p>A</p></div>");
        Selector.select("p:eq(abc)", doc);
    }

    // Tests null query throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testSelect_nullQuery_throwsException() {
        Document doc = Jsoup.parse("<div></div>");
        Selector.select(null, doc);
    }

    // Tests empty query throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testSelect_emptyQuery_throwsException() {
        Document doc = Jsoup.parse("<div></div>");
        Selector.select("   ", doc);
    }

    // Tests null root throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testSelect_nullRoot_throwsException() {
        Selector.select("div", (Element) null);
    }
}