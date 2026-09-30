package org.jsoup.select;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.junit.Test;
import static org.junit.Assert.*;

public class SelectorTest {

    // Tests basic tag selector
    @Test
    public void testSelect_basicTagSelector_returnsMatchingElements() {
        String html = "<div><p>Hello</p><span>World</span></div>";
        Element root = Jsoup.parse(html);
        Elements result = Selector.select("p", root);
        assertEquals(1, result.size());
        assertEquals("p", result.get(0).tagName());
    }

    // Tests id selector
    @Test
    public void testSelect_idSelector_returnsElementWithId() {
        String html = "<div id='content'>Text</div>";
        Element root = Jsoup.parse(html);
        Elements result = Selector.select("#content", root);
        assertEquals(1, result.size());
        assertEquals("content", result.get(0).id());
    }

    // Tests class selector
    @Test
    public void testSelect_classSelector_returnsMatchingElements() {
        String html = "<div class='highlight'>A</div><p class='highlight'>B</p>";
        Element root = Jsoup.parse(html);
        Elements result = Selector.select(".highlight", root);
        assertEquals(2, result.size());
    }

    // Tests combinator child selector '>'
    @Test
    public void testSelect_childCombinator_returnsDirectChildren() {
        String html = "<div><p>Child</p><span><p>Grandchild</p></span></div>";
        Element root = Jsoup.parse(html);
        Elements result = Selector.select("div > p", root);
        assertEquals(1, result.size());
        assertEquals("Child", result.get(0).text());
    }

    // Tests combinator descendant selector ' '
    @Test
    public void testSelect_descendantCombinator_returnsAllDescendants() {
        String html = "<div><p>Child</p><span><p>Grandchild</p></span></div>";
        Element root = Jsoup.parse(html);
        Elements result = Selector.select("div p", root);
        assertEquals(2, result.size());
    }

    // Tests combinator adjacent sibling selector '+'
    @Test
    public void testSelect_adjacentSiblingCombinator_returnsImmediateSibling() {
        String html = "<ul><li>One</li><li>Two</li><li>Three</li></ul>";
        Element root = Jsoup.parse(html);
        Elements result = Selector.select("li + li", root);
        assertEquals(2, result.size());
        assertEquals("Two", result.get(0).text());
        assertEquals("Three", result.get(1).text());
    }

    // Tests combinator general sibling selector '~'
    @Test
    public void testSelect_generalSiblingCombinator_returnsAllFollowingSiblings() {
        String html = "<div><p>First</p><span>Second</span><span>Third</span></div>";
        Element root = Jsoup.parse(html);
        Elements result = Selector.select("p ~ span", root);
        assertEquals(2, result.size());
    }

    // Tests :has pseudo selector
    @Test
    public void testSelect_hasPseudoSelector_returnsElementsContainingSelector() {
        String html = "<div><p>Text</p></div><span></span>";
        Element root = Jsoup.parse(html);
        Elements result = Selector.select("div:has(p)", root);
        assertEquals(1, result.size());
        assertEquals("div", result.get(0).tagName());
    }

    // Tests :not pseudo selector
    @Test
    public void testSelect_notPseudoSelector_excludesMatchingElements() {
        String html = "<div class='a'>A</div><div class='b'>B</div>";
        Element root = Jsoup.parse(html);
        Elements result = Selector.select("div:not(.a)", root);
        assertEquals(1, result.size());
        assertEquals("b", result.get(0).className());
    }

    // Tests :contains pseudo selector
    @Test
    public void testSelect_containsPseudoSelector_returnsElementsWithText() {
        String html = "<p>Hello World</p><p>Goodbye</p>";
        Element root = Jsoup.parse(html);
        Elements result = Selector.select("p:contains(Hello)", root);
        assertEquals(1, result.size());
    }

    // Tests :lt pseudo selector
    @Test
    public void testSelect_ltPseudoSelector_returnsElementsWithIndexLessThan() {
        String html = "<ul><li>One</li><li>Two</li><li>Three</li></ul>";
        Element root = Jsoup.parse(html);
        Elements result = Selector.select("li:lt(2)", root);
        assertEquals(2, result.size());
    }

    // Tests :gt pseudo selector
    @Test
    public void testSelect_gtPseudoSelector_returnsElementsWithIndexGreaterThan() {
        String html = "<ul><li>One</li><li>Two</li><li>Three</li></ul>";
        Element root = Jsoup.parse(html);
        Elements result = Selector.select("li:gt(0)", root);
        assertEquals(2, result.size());
    }

    // Tests :eq pseudo selector
    @Test
    public void testSelect_eqPseudoSelector_returnsElementAtExactIndex() {
        String html = "<ul><li>One</li><li>Two</li><li>Three</li></ul>";
        Element root = Jsoup.parse(html);
        Elements result = Selector.select("li:eq(1)", root);
        assertEquals(1, result.size());
        assertEquals("Two", result.get(0).text());
    }

    // Tests attribute selector [attr]
    @Test
    public void testSelect_attributeSelector_returnsElementsWithAttribute() {
        String html = "<a href='link.html'>Link</a><a>No link</a>";
        Element root = Jsoup.parse(html);
        Elements result = Selector.select("a[href]", root);
        assertEquals(1, result.size());
    }

    // Tests multiple elements selection via comma
    @Test
    public void testSelect_multipleSelectors_returnsUnion() {
        String html = "<div>Div</div><p>Paragraph</p><span>Span</span>";
        Element root = Jsoup.parse(html);
        Elements result = Selector.select("div, p", root);
        assertEquals(2, result.size());
    }

    // Tests empty query throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testSelect_emptyQuery_throwsException() {
        Element root = Jsoup.parse("<p></p>");
        Selector.select("", root);
    }

    // Tests whitespace preserved leading combinator
    @Test
    public void testSelect_leadingCombinator_usesRootAsElements() {
        String html = "<div><span>Child</span></div>";
        Element root = Jsoup.parse(html);
        Elements result = Selector.select("> span", root);
        assertEquals(1, result.size());
        assertEquals("span", result.get(0).tagName());
    }

    // Tests :containsOwn pseudo selector
    @Test
    public void testSelect_containsOwnPseudoSelector_returnsElementsWithOwnText() {
        String html = "<p>Hello <span>World</span></p>";
        Element root = Jsoup.parse(html);
        Elements result = Selector.select("p:containsOwn(Hello)", root);
        assertEquals(1, result.size());
    }

    // Tests :matches pseudo selector
    @Test
    public void testSelect_matchesPseudoSelector_returnsElementsMatchingRegex() {
        String html = "<p>abc123</p><p>def456</p>";
        Element root = Jsoup.parse(html);
        Elements result = Selector.select("p:matches(\\d+)", root);
        assertEquals(2, result.size());
    }

    // Tests attribute selector with !=
    @Test
    public void testSelect_attributeNotEqual_returnsElementsWithAttributeDifferentValue() {
        String html = "<div class='a'>A</div><div class='b'>B</div>";
        Element root = Jsoup.parse(html);
        Elements result = Selector.select("div[class!=a]", root);
        assertEquals(1, result.size());
    }

    // Tests attribute selector with ^=
    @Test
    public void testSelect_attributeStartsWith_returnsElements() {
        String html = "<a href='http://example.com'>Link</a><a href='https://secure.com'>Secure</a>";
        Element root = Jsoup.parse(html);
        Elements result = Selector.select("a[href^=http://]", root);
        assertEquals(1, result.size());
    }
}