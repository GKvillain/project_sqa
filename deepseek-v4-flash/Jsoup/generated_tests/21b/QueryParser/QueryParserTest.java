package org.jsoup.select;

import org.junit.Test;
import static org.junit.Assert.*;

public class QueryParserTest {

    // Test single tag selector returns Tag evaluator
    @Test
    public void testParse_singleTag_returnsTagEvaluator() {
        Evaluator eval = QueryParser.parse("div");
        assertTrue(eval instanceof Evaluator.Tag);
    }

    // Test id and class selectors return expected evaluators
    @Test
    public void testParse_idAndClass_returnsExpectedEvaluators() {
        assertTrue(QueryParser.parse("#main") instanceof Evaluator.Id);
        assertTrue(QueryParser.parse(".content") instanceof Evaluator.Class);
    }

    // Test universal selector and attribute presence selector
    @Test
    public void testParse_allAndAttribute_returnsExpectedEvaluators() {
        assertTrue(QueryParser.parse("*") instanceof Evaluator.AllElements);
        assertTrue(QueryParser.parse("[href]") instanceof Evaluator.Attribute);
    }

    // Test all attribute operator branches
    @Test
    public void testParse_attributeOperators_returnsExpectedEvaluators() {
        assertTrue(QueryParser.parse("[href=url]") instanceof Evaluator.AttributeWithValue);
        assertTrue(QueryParser.parse("[href!=url]") instanceof Evaluator.AttributeWithValueNot);
        assertTrue(QueryParser.parse("[href^=http]") instanceof Evaluator.AttributeWithValueStarting);
        assertTrue(QueryParser.parse("[href$=.org]") instanceof Evaluator.AttributeWithValueEnding);
        assertTrue(QueryParser.parse("[href*=example]") instanceof Evaluator.AttributeWithValueContaining);
        assertTrue(QueryParser.parse("[href~=regex]") instanceof Evaluator.AttributeWithValueMatching);
    }

    // Test index pseudo-classes :lt, :gt, :eq
    @Test
    public void testParse_indexPseudos_returnsExpectedEvaluators() {
        assertTrue(QueryParser.parse(":lt(2)") instanceof Evaluator.IndexLessThan);
        assertTrue(QueryParser.parse(":gt(1)") instanceof Evaluator.IndexGreaterThan);
        assertTrue(QueryParser.parse(":eq(0)") instanceof Evaluator.IndexEquals);
    }

    // Test :has and :not pseudo-classes
    @Test
    public void testParse_hasAndNot_returnsExpectedEvaluators() {
        assertTrue(QueryParser.parse(":has(p)") instanceof StructuralEvaluator.Has);
        assertTrue(QueryParser.parse(":not(.foo)") instanceof StructuralEvaluator.Not);
    }

    // Test contains pseudo-classes
    @Test
    public void testParse_contains_returnsExpectedEvaluators() {
        assertTrue(QueryParser.parse(":contains(hello)") instanceof Evaluator.ContainsText);
        assertTrue(QueryParser.parse(":containsOwn(hello)") instanceof Evaluator.ContainsOwnText);
    }

    // Test matches pseudo-classes
    @Test
    public void testParse_matches_returnsExpectedEvaluators() {
        assertTrue(QueryParser.parse(":matches(\\w+)") instanceof Evaluator.Matches);
        assertTrue(QueryParser.parse(":matchesOwn(\\w+)") instanceof Evaluator.MatchesOwn);
    }

    // Test descendant combinator (space)
    @Test
    public void testParse_descendantCombinator_returnsAndEvaluator() {
        assertTrue(QueryParser.parse("div p") instanceof CombiningEvaluator.And);
    }

    // Test child combinator (>)
    @Test
    public void testParse_childCombinator_returnsAndEvaluator() {
        assertTrue(QueryParser.parse("div > p") instanceof CombiningEvaluator.And);
    }

    // Test adjacent sibling combinator (+)
    @Test
    public void testParse_adjacentSibling_returnsAndEvaluator() {
        assertTrue(QueryParser.parse("div + p") instanceof CombiningEvaluator.And);
    }

    // Test general sibling combinator (~)
    @Test
    public void testParse_generalSibling_returnsAndEvaluator() {
        assertTrue(QueryParser.parse("div ~ p") instanceof CombiningEvaluator.And);
    }

    // Test two comma-separated selectors
    @Test
    public void testParse_twoSelectors_returnsOrEvaluator() {
        assertTrue(QueryParser.parse("div, p") instanceof CombiningEvaluator.Or);
    }

    // Test three comma-separated selectors (regression for Defects4J bug)
    @Test
    public void testParse_threeSelectors_returnsOrEvaluator() {
        Evaluator eval = QueryParser.parse("div, p, span");
        assertTrue(eval instanceof CombiningEvaluator.Or);
    }

    // Test unknown pseudo-class throws SelectorParseException
    @Test(expected = Selector.SelectorParseException.class)
    public void testParse_unknownPseudo_throwsSelectorParseException() {
        QueryParser.parse("div:unknown");
    }

    // Test empty :has() throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testParse_emptyHas_throwsIllegalArgumentException() {
        QueryParser.parse(":has()");
    }

    // Test empty :contains() throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testParse_emptyContains_throwsIllegalArgumentException() {
        QueryParser.parse(":contains()");
    }

    // Test non-numeric index in :lt throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testParse_nonNumericIndex_throwsIllegalArgumentException() {
        QueryParser.parse(":lt(abc)");
    }
}