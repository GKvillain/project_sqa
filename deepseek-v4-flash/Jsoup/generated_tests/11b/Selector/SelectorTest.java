package org.jsoup.select;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Test;

import static org.junit.Assert.*;

public class SelectorTest {

    // Tests basic tag selector
    @Test
    public void testSelect_simpleTag_returnsMatchingElements() {
        Document doc = Jsoup.parse("<div><p>Hello</p><span>World</span></div>");
        Elements result = Selector.select("p", doc);
        assertEquals(1, result.size());
        assertEquals("p", result.get(0).tagName());
    }

    // Tests selector with id
    @Test
    public void testSelect_byId_returnsSingleElement() {
        Document doc = Jsoup.parse("<div id='content'>Text</div>");
        Elements result = Selector.select("#content", doc);
        assertEquals(1, result.size());
        assertEquals("content", result.get(0).id());
    }

    // Tests selector with class
    @Test
    public void testSelect_byClass_returnsMatchingElements() {
        Document doc = Jsoup.parse("<div class='test'>A</div><span class='test'>B</span>");
        Elements result = Selector.select(".test", doc);
        assertEquals(2, result.size());
    }

    // Tests selector with attribute
    @Test
    public void testSelect_byAttribute_returnsMatchingElements() {
        Document doc = Jsoup.parse("<a href='http://example.com'>Link</a>");
        Elements result = Selector.select("[href]", doc);
        assertEquals(1, result.size());
        assertEquals("http://example.com", result.get(0).attr("href"));
    }

    // Tests selector with descendant combinator (space)
    @Test
    public void testSelect_descendantCombinator_returnsDescendantElements() {
        Document doc = Jsoup.parse("<div><p><span>Deep</span></p></div>");
        Elements result = Selector.select("div span", doc);
        assertEquals(1, result.size());
        assertEquals("span", result.get(0).tagName());
    }

    // Tests selector with child combinator (>)
    @Test
    public void testSelect_childCombinator_returnsDirectChildren() {
        Document doc = Jsoup.parse("<div><p>Direct</p><span><p>Nested</p></span></div>");
        Elements result = Selector.select("div > p", doc);
        assertEquals(1, result.size());
        assertEquals("Direct", result.get(0).text());
    }

    // Tests selector with adjacent sibling combinator (+)
    @Test
    public void testSelect_adjacentSibling_returnsImmediateSibling() {
        Document doc = Jsoup.parse("<div><p>First</p><p>Second</p><span>Third</span></div>");
        Elements result = Selector.select("p + p", doc);
        assertEquals(1, result.size());
        assertEquals("Second", result.get(0).text());
    }

    // Tests selector with general sibling combinator (~)
    @Test
    public void testSelect_generalSibling_returnsFollowingSiblings() {
        Document doc = Jsoup.parse("<div><p>First</p><span>Second</span><p>Third</p></div>");
        Elements result = Selector.select("p ~ p", doc);
        assertEquals(1, result.size());
        assertEquals("Third", result.get(0).text());
    }

    // Tests selector with comma (group or)
    @Test
    public void testSelect_commaSeparated_returnsUnionOfSelectors() {
        Document doc = Jsoup.parse("<div><h1>Title</h1><p>Para</p></div>");
        Elements result = Selector.select("h1, p", doc);
        assertEquals(2, result.size());
    }

    // Tests pseudo selector :lt(n)
    @Test
    public void testSelect_ltSelector_returnsElementsWithIndexLessThanN() {
        Document doc = Jsoup.parse("<ul><li>1</li><li>2</li><li>3</li></ul>");
        Elements result = Selector.select("li:lt(2)", doc);
        assertEquals(2, result.size());
    }

    // Tests pseudo selector :gt(n)
    @Test
    public void testSelect_gtSelector_returnsElementsWithIndexGreaterThanN() {
        Document doc = Jsoup.parse("<ul><li>1</li><li>2</li><li>3</li></ul>");
        Elements result = Selector.select("li:gt(0)", doc);
        assertEquals(2, result.size());
    }

    // Tests pseudo selector :eq(n)
    @Test
    public void testSelect_eqSelector_returnsElementAtExactIndex() {
        Document doc = Jsoup.parse("<ul><li>1</li><li>2</li><li>3</li></ul>");
        Elements result = Selector.select("li:eq(1)", doc);
        assertEquals(1, result.size());
        assertEquals("2", result.get(0).text());
    }

    // Tests pseudo selector :contains(text)
    @Test
    public void testSelect_containsText_returnsElementsContainingText() {
        Document doc = Jsoup.parse("<p>Hello World</p><p>Goodbye</p>");
        Elements result = Selector.select("p:contains(Hello)", doc);
        assertEquals(1, result.size());
        assertEquals("Hello World", result.get(0).text());
    }

    // Tests universal selector
    @Test
    public void testSelect_universalSelector_returnsAllElements() {
        Document doc = Jsoup.parse("<div><p>Text</p></div>");
        Elements result = Selector.select("*", doc);
        assertTrue(result.size() >= 2);
    }

    // Tests empty query throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testSelect_emptyQuery_throwsException() {
        Document doc = Jsoup.parse("<div></div>");
        Selector.select("", doc);
    }

    // Tests null query throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testSelect_nullQuery_throwsException() {
        Document doc = Jsoup.parse("<div></div>");
        Selector.select(null, doc);
    }

    // Tests invalid query throws exception
    @Test(expected = Selector.SelectorParseException.class)
    public void testSelect_invalidQuery_throwsParseException() {
        Document doc = Jsoup.parse("<div></div>");
        Selector.select(":nonexistent()", doc);
    }

    // Tests multiple roots via Iterable
    @Test
    public void testSelect_multipleRoots_returnsCombinedResults() {
        Document doc1 = Jsoup.parse("<div><p>A</p></div>");
        Document doc2 = Jsoup.parse("<div><p>B</p></div>");
        Elements result = Selector.select("p", java.util.Arrays.asList(doc1, doc2));
        assertEquals(2, result.size());
    }
}