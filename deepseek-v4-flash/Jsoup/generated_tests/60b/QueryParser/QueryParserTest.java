package org.jsoup.select;

import org.junit.Test;
import static org.junit.Assert.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.select.Selector;

public class QueryParserTest {
    private Document doc = Jsoup.parse("<html><body>" +
        "<div id='a' class='b'>" +
            "<p id='p1'>text</p>" +
            "<span id='s1'><a id='a1'><p id='p2'>nested</p></a></span>" +
            "<p id='p3'>more</p>" +
        "</div>" +
        "<p id='p4'>sibling</p>" +
        "<span id='s2'>other</span>" +
    "</body></html>");

    private Elements select(String query) {
        return doc.select(query);
    }

    // Test basic tag selector
    @Test
    public void testParseTag_returnsCorrectElements() {
        Elements els = select("div");
        assertEquals(1, els.size());
        assertEquals("div", els.get(0).tagName());
    }

    // Test id selector
    @Test
    public void testParseId_returnsCorrectElement() {
        Elements els = select("#a");
        assertEquals(1, els.size());
        assertEquals("a", els.get(0).id());
    }

    // Test class selector
    @Test
    public void testParseClass_returnsCorrectElement() {
        Elements els = select(".b");
        assertEquals(1, els.size());
        assertEquals("div", els.get(0).tagName());
    }

    // Test child combinator ( > )
    @Test
    public void testParseChildCombinator_returnsChild() {
        Elements els = select("div > p");
        assertEquals(2, els.size());
        assertEquals("p1", els.get(0).id());
        assertEquals("p3", els.get(1).id());
    }

    // Test descendant combinator (space)
    @Test
    public void testParseDescendantCombinator_returnsDescendant() {
        Elements els = select("div p");
        assertEquals(3, els.size());
    }

    // Test adjacent sibling combinator ( + )
    @Test
    public void testParseAdjacentSibling_returnsSibling() {
        Elements els = select("div + p");
        assertEquals(1, els.size());
        assertEquals("p4", els.get(0).id());
    }

    // Test general sibling combinator ( ~ )
    @Test
    public void testParseGeneralSibling_returnsSibling() {
        Elements els = select("div ~ p");
        assertEquals(1, els.size());
        assertEquals("p4", els.get(0).id());
    }

    // Test OR combinator ( , )
    @Test
    public void testParseOrCombinator_returnsUnion() {
        Elements els = select("div, p");
        assertEquals(5, els.size());
    }

    // Test :has() pseudo-selector
    @Test
    public void testParseHas_returnsHasEvaluator() {
        Elements els = select("div:has(p)");
        assertEquals(1, els.size());
        assertEquals("a", els.get(0).id());
    }

    // Test :has() with descendant combinator inside
    @Test
    public void testParseHasWithDescendant_returnsCorrect() {
        Elements els = select("div:has(p) span");
        assertEquals(1, els.size());
        assertEquals("s1", els.get(0).id());
    }

    // Test :has() combined with OR
    @Test
    public void testParseHasWithOr_returnsUnion() {
        Elements els = select("div:has(p), span");
        assertEquals(3, els.size());
    }

    // Test :not() pseudo-selector
    @Test
    public void testParseNot_returnsNotEvaluator() {
        Elements els = select("p:not(#p4)");
        assertEquals(3, els.size());
    }

    // Test :contains() pseudo-selector
    @Test
    public void testParseContains_returnsContainsEvaluator() {
        Elements els = select("p:contains(text)");
        assertEquals(1, els.size());
        assertEquals("p1", els.get(0).id());
    }

    // Test :matches() pseudo-selector
    @Test
    public void testParseMatches_returnsMatchesEvaluator() {
        Elements els = select("p:matches(te.t)");
        assertEquals(1, els.size());
        assertEquals("p1", els.get(0).id());
    }

    // Test :nth-child() with numeric argument
    @Test
    public void testParseNthChild_returnsNthChild() {
        Elements els = select("p:nth-child(1)");
        assertEquals(1, els.size());
        assertEquals("p1", els.get(0).id());
    }

    // Test :nth-child() with odd keyword
    @Test
    public void testParseNthChildOdd_returnsNthChild() {
        Elements els = select("p:nth-child(odd)");
        assertEquals(2, els.size());
        assertEquals("p1", els.get(0).id());
        assertEquals("p3", els.get(1).id());
    }

    // Test :lt() index pseudo-selector
    @Test
    public void testParseIndexLessThan_returnsCorrect() {
        Elements els = select("p:lt(1)");
        assertEquals(1, els.size());
        assertEquals("p1", els.get(0).id());
    }

    // Test OR combinator followed by descendant combinator (exercises replaceRightMost branch)
    @Test
    public void testParseOrWithDescendantCombinator_returnsCorrect() {
        Elements els = select("div, span > a p");
        assertEquals(2, els.size());
        assertTrue(els.get(0).tagName().equals("div") && els.get(0).id().equals("a"));
        assertTrue(els.get(1).tagName().equals("p") && els.get(1).id().equals("p2"));
    }

    // Test empty id selector throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testParseEmptyId_throwsIllegalArgumentException() {
        QueryParser.parse("#");
    }

    // Test invalid pseudo-selector throws SelectorParseException
    @Test(expected = Selector.SelectorParseException.class)
    public void testParseInvalidPseudo_throwsSelectorParseException() {
        QueryParser.parse(":unknown");
    }

    // Test null query throws NullPointerException
    @Test(expected = NullPointerException.class)
    public void testParseNullQuery_throwsNullPointerException() {
        QueryParser.parse(null);
    }

    // ==================== NEW TEST CASES FOR ADDITIONAL COVERAGE ====================

    // Test :nth-child() with even keyword
    @Test
    public void testParseNthChildEven() {
        Elements els = select("p:nth-child(even)");
        assertEquals(1, els.size());
        assertEquals("p3", els.get(0).id());
    }

    // Test :nth-child() with formula (2n+1)
    @Test
    public void testParseNthChildFormula() {
        Elements els = select("p:nth-child(2n+1)");
        assertEquals(2, els.size());
        assertEquals("p1", els.get(0).id());
        assertEquals("p3", els.get(1).id());
    }

    // Test :nth-last-child() selector
    @Test
    public void testParseNthLastChild() {
        Elements els = select("p:nth-last-child(1)");
        assertEquals(1, els.size());
        assertEquals("p3", els.get(0).id());
    }

    // Test :nth-of-type() selector
    @Test
    public void testParseNthOfType() {
        Elements els = select("p:nth-of-type(2)");
        assertEquals(1, els.size());
        assertEquals("p3", els.get(0).id());
    }

    // Test :gt() index pseudo-selector
    @Test
    public void testParseIndexGreaterThan() {
        Elements els = select("p:gt(1)");
        assertEquals(2, els.size());
        assertEquals("p3", els.get(0).id());
        assertEquals("p4", els.get(1).id());
    }

    // Test :eq() index pseudo-selector
    @Test
    public void testParseIndexEquals() {
        Elements els = select("p:eq(1)");
        assertEquals(1, els.size());
        assertEquals("p3", els.get(0).id());
    }

    // Test :containsOwn() pseudo-selector
    @Test
    public void testParseContainsOwnText() {
        Elements els = select("p:containsOwn(text)");
        assertEquals(1, els.size());
        assertEquals("p1", els.get(0).id());
    }

    // Test :matchesOwn() pseudo-selector
    @Test
    public void testParseMatchesOwnText() {
        Elements els = select("p:matchesOwn(te.t)");
        assertEquals(1, els.size());
        assertEquals("p1", els.get(0).id());
    }

    // Test attribute selector with equals
    @Test
    public void testParseAttributeEquals() {
        Elements els = select("[id=p1]");
        assertEquals(1, els.size());
        assertEquals("p1", els.get(0).id());
    }

    // Test attribute selector with starts with
    @Test
    public void testParseAttributeStartsWith() {
        Elements els = select("[id^=p]");
        assertEquals(4, els.size());
    }

    // Test attribute selector with ends with
    @Test
    public void testParseAttributeEndsWith() {
        Elements els = select("[id$=1]");
        assertEquals(2, els.size());
    }

    // Test attribute selector with contains
    @Test
    public void testParseAttributeContains() {
        Elements els = select("[id*=s]");
        assertEquals(1, els.size());
        assertEquals("s1", els.get(0).id());
    }

    // Test attribute selector with not equals
    @Test
    public void testParseAttributeNotEquals() {
        Elements els = select("[id!=p1]");
        assertEquals(7, els.size());
    }

    // Test multiple attribute selectors
    @Test
    public void testParseMultipleAttributes() {
        Elements els = select("[id][class]");
        assertEquals(1, els.size());
        assertEquals("a", els.get(0).id());
    }

    // Test :contains() case insensitive (if supported)
    @Test
    public void testParseContainsIgnoreCase() {
        Elements els = select("p:contains(TEXT)");
        assertEquals(1, els.size());
        assertEquals("p1", els.get(0).id());
    }

    // Test :has() direct child combinator inside
    @Test
    public void testParseHasWithChildCombinator() {
        Elements els = select("div:has(> p)");
        assertEquals(1, els.size());
        assertEquals("a", els.get(0).id());
    }

    // Test :not() with multiple selectors
    @Test
    public void testParseNotWithMultipleSelectors() {
        Elements els = select("p:not(#p1,#p4)");
        assertEquals(2, els.size());
        assertEquals("p2", els.get(0).id());
        assertEquals("p3", els.get(1).id());
    }

    // Test :has() with adjacent sibling
    @Test
    public void testParseHasWithAdjacentSibling() {
        Elements els = select("div:has(+ p)");
        assertEquals(1, els.size());
        assertEquals("a", els.get(0).id());
    }

    // Test :has() with general sibling
    @Test
    public void testParseHasWithGeneralSibling() {
        Elements els = select("div:has(~ p)");
        assertEquals(1, els.size());
        assertEquals("a", els.get(0).id());
    }

    // Test :first-child pseudo-selector
    @Test
    public void testParseFirstChild() {
        Elements els = select("p:first-child");
        assertEquals(1, els.size());
        assertEquals("p1", els.get(0).id());
    }

    // Test :last-child pseudo-selector
    @Test
    public void testParseLastChild() {
        Elements els = select("p:last-child");
        assertEquals(1, els.size());
        assertEquals("p3", els.get(0).id());
    }

    // Test :first-of-type pseudo-selector
    @Test
    public void testParseFirstOfType() {
        Elements els = select("p:first-of-type");
        assertEquals(2, els.size());
    }

    // Test :last-of-type pseudo-selector
    @Test
    public void testParseLastOfType() {
        Elements els = select("p:last-of-type");
        assertEquals(2, els.size());
    }

    // Test :only-child pseudo-selector
    @Test
    public void testParseOnlyChild() {
        Elements els = select("a:only-child");
        assertEquals(1, els.size());
        assertEquals("a1", els.get(0).id());
    }

    // Test :only-of-type pseudo-selector
    @Test
    public void testParseOnlyOfType() {
        Elements els = select("a:only-of-type");
        assertEquals(1, els.size());
        assertEquals("a1", els.get(0).id());
    }

    // Test :empty pseudo-selector
    @Test
    public void testParseEmpty() {
        Elements els = select(":empty");
        assertEquals(4, els.size());
    }

    // Test :root pseudo-selector
    @Test
    public void testParseRoot() {
        Elements els = select(":root");
        assertEquals(1, els.size());
        assertEquals("html", els.get(0).tagName());
    }

    // Test attribute selector with regex match
    @Test
    public void testParseAttributeMatches() {
        Elements els = select("[id~=p[0-9]]");
        assertEquals(4, els.size());
    }

    // Test OR combinator with multiple levels
    @Test
    public void testParseMultipleOrCombinators() {
        Elements els = select("div, span, p");
        assertEquals(6, els.size());
    }
}