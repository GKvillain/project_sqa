package org.jsoup.select;

import org.junit.Test;
import static org.junit.Assert.*;

public class QueryParserTest {

    // Tests simple tag evaluator parsing
    @Test
    public void testParse_simpleTag_returnsTagEvaluator() {
        Evaluator eval = QueryParser.parse("div");
        assertTrue(eval instanceof Evaluator.Tag);
        assertEquals("div", eval.toString());
    }

    // Tests id selector parsing
    @Test
    public void testParse_idSelector_returnsIdEvaluator() {
        Evaluator eval = QueryParser.parse("#main");
        assertTrue(eval instanceof Evaluator.Id);
        assertEquals("#main", eval.toString());
    }

    // Tests class selector parsing
    @Test
    public void testParse_classSelector_returnsClassEvaluator() {
        Evaluator eval = QueryParser.parse(".content");
        assertTrue(eval instanceof Evaluator.Class);
        assertEquals(".content", eval.toString());
    }

    // Tests all elements wildcard selector
    @Test
    public void testParse_allElements_returnsAllElementsEvaluator() {
        Evaluator eval = QueryParser.parse("*");
        assertTrue(eval instanceof Evaluator.AllElements);
        assertEquals("*", eval.toString());
    }

    // Tests various attribute selectors
    @Test
    public void testParse_attributeSelectors_returnsAttributeEvaluators() {
        assertTrue(QueryParser.parse("[href]") instanceof Evaluator.Attribute);
        assertTrue(QueryParser.parse("[^data-]") instanceof Evaluator.AttributeStarting);
        assertTrue(QueryParser.parse("[type=text]") instanceof Evaluator.AttributeWithValue);
        assertTrue(QueryParser.parse("[type!=text]") instanceof Evaluator.AttributeWithValueNot);
        assertTrue(QueryParser.parse("[href^=https]") instanceof Evaluator.AttributeWithValueStarting);
        assertTrue(QueryParser.parse("[href$=.png]") instanceof Evaluator.AttributeWithValueEnding);
        assertTrue(QueryParser.parse("[class*=btn]") instanceof Evaluator.AttributeWithValueContaining);
        assertTrue(QueryParser.parse("[title~=test]") instanceof Evaluator.AttributeWithValueMatching);
    }

    // Tests combinators: descendant, child, adjacent sibling, and general sibling
    @Test
    public void testParse_combinators_returnsCombiningEvaluator() {
        assertTrue(QueryParser.parse("div p") instanceof CombiningEvaluator.And);
        assertTrue(QueryParser.parse("div > p") instanceof CombiningEvaluator.And);
        assertTrue(QueryParser.parse("div + p") instanceof CombiningEvaluator.And);
        assertTrue(QueryParser.parse("div ~ p") instanceof CombiningEvaluator.And);
        assertTrue(QueryParser.parse("div, p") instanceof CombiningEvaluator.Or);
    }

    // Tests index based pseudo-selectors: :lt, :gt, :eq
    @Test
    public void testParse_structuralIndexPseudoSelectors_returnsIndexEvaluators() {
        assertTrue(QueryParser.parse("div:lt(2)") instanceof CombiningEvaluator.And);
        assertTrue(QueryParser.parse("div:gt(2)") instanceof CombiningEvaluator.And);
        assertTrue(QueryParser.parse("div:eq(2)") instanceof CombiningEvaluator.And);
    }

    // Tests structural pseudo-selectors
    @Test
    public void testParse_structuralPseudoSelectors_returnsCorrectEvaluators() {
        assertTrue(QueryParser.parse(":first-child") instanceof Evaluator.IsFirstChild);
        assertTrue(QueryParser.parse(":last-child") instanceof Evaluator.IsLastChild);
        assertTrue(QueryParser.parse(":first-of-type") instanceof Evaluator.IsFirstOfType);
        assertTrue(QueryParser.parse(":last-of-type") instanceof Evaluator.IsLastOfType);
        assertTrue(QueryParser.parse(":only-child") instanceof Evaluator.IsOnlyChild);
        assertTrue(QueryParser.parse(":only-of-type") instanceof Evaluator.IsOnlyOfType);
        assertTrue(QueryParser.parse(":empty") instanceof Evaluator.IsEmpty);
        assertTrue(QueryParser.parse(":root") instanceof Evaluator.IsRoot);
    }

    // Tests nth-child pseudo selector with odd, even, An+B, and B
    @Test
    public void testParse_nthChildPseudoSelectors_returnsNthEvaluators() {
        assertTrue(QueryParser.parse(":nth-child(odd)") instanceof Evaluator.IsNthChild);
        assertTrue(QueryParser.parse(":nth-child(even)") instanceof Evaluator.IsNthChild);
        assertTrue(QueryParser.parse(":nth-child(2n+1)") instanceof Evaluator.IsNthChild);
        assertTrue(QueryParser.parse(":nth-child(3)") instanceof Evaluator.IsNthChild);
        assertTrue(QueryParser.parse(":nth-last-child(1)") instanceof Evaluator.IsNthLastChild);
        assertTrue(QueryParser.parse(":nth-of-type(2)") instanceof Evaluator.IsNthOfType);
        assertTrue(QueryParser.parse(":nth-last-of-type(2)") instanceof Evaluator.IsNthLastOfType);
    }

    // Tests relational and text pseudo-selectors: :has, :contains, :containsOwn, :containsData, :matches, :matchesOwn, :not
    @Test
    public void testParse_relationalAndTextPseudoSelectors_returnsEvaluators() {
        assertTrue(QueryParser.parse(":has(p)") instanceof StructuralEvaluator.Has);
        assertTrue(QueryParser.parse(":contains(text)") instanceof Evaluator.ContainsText);
        assertTrue(QueryParser.parse(":containsOwn(text)") instanceof Evaluator.ContainsOwnText);
        assertTrue(QueryParser.parse(":containsData(data)") instanceof Evaluator.ContainsData);
        assertTrue(QueryParser.parse(":matches(regex)") instanceof Evaluator.Matches);
        assertTrue(QueryParser.parse(":matchesOwn(regex)") instanceof Evaluator.MatchesOwn);
        assertTrue(QueryParser.parse(":not(div)") instanceof StructuralEvaluator.Not);
    }

    // Tests namespace prefix parsing
    @Test
    public void testParse_namespacedTags_returnsTagEvaluators() {
        Evaluator evalWildcard = QueryParser.parse("*|div");
        assertTrue(evalWildcard instanceof CombiningEvaluator.Or);

        Evaluator evalNs = QueryParser.parse("fb|name");
        assertTrue(evalNs instanceof Evaluator.Tag);
        assertEquals("fb:name", evalNs.toString());
    }

    // Tests selector starting with combinator
    @Test
    public void testParse_leadingCombinator_addsRootEvaluator() {
        Evaluator eval = QueryParser.parse("> div");
        assertTrue(eval instanceof CombiningEvaluator.And);
    }

    // Tests defects4j 60b regression: unbalanced attribute query exception handling
    @Test(expected = Selector.SelectorParseException.class)
    public void testParse_unbalancedAttributeQuery_throwsSelectorParseException() {
        QueryParser.parse("div[attr");
    }

    // Tests invalid nth-child format exception
    @Test(expected = Selector.SelectorParseException.class)
    public void testParse_invalidNthChildFormat_throwsSelectorParseException() {
        QueryParser.parse(":nth-child(invalid)");
    }

    // Tests empty :has() query exception
    @Test(expected = IllegalArgumentException.class)
    public void testParse_emptyHasSelector_throwsIllegalArgumentException() {
        QueryParser.parse(":has()");
    }

    // Tests unknown unexpected token exception
    @Test(expected = Selector.SelectorParseException.class)
    public void testParse_unexpectedToken_throwsSelectorParseException() {
        QueryParser.parse("div;p");
    }
}