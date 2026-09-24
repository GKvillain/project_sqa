package org.jsoup.select;

import org.junit.Test;

import static org.junit.Assert.*;

public class QueryParserTest {

    // Tests parsing simple tag selector
    @Test
    public void testParse_simpleTag_returnsTagEvaluator() {
        Evaluator eval = QueryParser.parse("div");
        assertTrue(eval instanceof Evaluator.Tag);
        assertEquals("div", eval.toString());
    }

    // Tests parsing id and class selectors
    @Test
    public void testParse_idAndClass_returnsCombinedEvaluator() {
        Evaluator eval = QueryParser.parse("#main.content");
        assertTrue(eval instanceof CombiningEvaluator.And);
    }

    // Tests parsing universal selector
    @Test
    public void testParse_allElements_returnsAllElementsEvaluator() {
        Evaluator eval = QueryParser.parse("*");
        assertTrue(eval instanceof Evaluator.AllElements);
    }

    // Tests parsing namespace selector
    @Test
    public void testParse_namespaceTags_returnsEvaluator() {
        Evaluator wildcardNs = QueryParser.parse("*|div");
        assertTrue(wildcardNs instanceof CombiningEvaluator.Or);

        Evaluator namedNs = QueryParser.parse("fb|like");
        assertTrue(namedNs instanceof Evaluator.Tag);
    }

    // Tests parsing various attribute selectors
    @Test
    public void testParse_attributeOperators_returnsAttributeEvaluators() {
        assertTrue(QueryParser.parse("[href]") instanceof Evaluator.Attribute);
        assertTrue(QueryParser.parse("[^data-]") instanceof Evaluator.AttributeStarting);
        assertTrue(QueryParser.parse("[title=test]") instanceof Evaluator.AttributeWithValue);
        assertTrue(QueryParser.parse("[title!=test]") instanceof Evaluator.AttributeWithValueNot);
        assertTrue(QueryParser.parse("[title^=test]") instanceof Evaluator.AttributeWithValueStarting);
        assertTrue(QueryParser.parse("[title$=test]") instanceof Evaluator.AttributeWithValueEnding);
        assertTrue(QueryParser.parse("[title*=test]") instanceof Evaluator.AttributeWithValueContaining);
        assertTrue(QueryParser.parse("[title~=test.*]") instanceof Evaluator.AttributeWithValueMatching);
    }

    // Tests parsing structural combinators
    @Test
    public void testParse_combinators_returnsCombinedEvaluators() {
        assertTrue(QueryParser.parse("div > p") instanceof CombiningEvaluator.And);
        assertTrue(QueryParser.parse("div p") instanceof CombiningEvaluator.And);
        assertTrue(QueryParser.parse("div + p") instanceof CombiningEvaluator.And);
        assertTrue(QueryParser.parse("div ~ p") instanceof CombiningEvaluator.And);
        assertTrue(QueryParser.parse("div, p") instanceof CombiningEvaluator.Or);
    }

    // Tests leading combinator with root
    @Test
    public void testParse_leadingCombinator_returnsRootEvaluator() {
        Evaluator eval = QueryParser.parse("> p");
        assertTrue(eval instanceof CombiningEvaluator.And);
    }

    // Tests combinator precedence with comma (OR)
    @Test
    public void testParse_orPrecedenceWithCombinators_constructsValidTree() {
        Evaluator eval = QueryParser.parse("div, p > span");
        assertTrue(eval instanceof CombiningEvaluator.Or);
    }

    // Tests pseudo index selectors
    @Test
    public void testParse_indexPseudoSelectors_returnsIndexEvaluators() {
        assertTrue(QueryParser.parse(":lt(2)") instanceof Evaluator.IndexLessThan);
        assertTrue(QueryParser.parse(":gt(2)") instanceof Evaluator.IndexGreaterThan);
        assertTrue(QueryParser.parse(":eq(2)") instanceof Evaluator.IndexEquals);
    }

    // Tests nth-child variants and expressions
    @Test
    public void testParse_nthChildSelectors_returnsNthEvaluators() {
        assertTrue(QueryParser.parse(":nth-child(2n+1)") instanceof Evaluator.IsNthChild);
        assertTrue(QueryParser.parse(":nth-child(odd)") instanceof Evaluator.IsNthChild);
        assertTrue(QueryParser.parse(":nth-child(even)") instanceof Evaluator.IsNthChild);
        assertTrue(QueryParser.parse(":nth-child(3)") instanceof Evaluator.IsNthChild);
        assertTrue(QueryParser.parse(":nth-last-child(1)") instanceof Evaluator.IsNthLastChild);
        assertTrue(QueryParser.parse(":nth-of-type(2n)") instanceof Evaluator.IsNthOfType);
        assertTrue(QueryParser.parse(":nth-last-of-type(2)") instanceof Evaluator.IsNthLastOfType);
    }

    // Tests structural pseudo-class selectors
    @Test
    public void testParse_structuralPseudoClasses_returnsCorrespondingEvaluators() {
        assertTrue(QueryParser.parse(":first-child") instanceof Evaluator.IsFirstChild);
        assertTrue(QueryParser.parse(":last-child") instanceof Evaluator.IsLastChild);
        assertTrue(QueryParser.parse(":first-of-type") instanceof Evaluator.IsFirstOfType);
        assertTrue(QueryParser.parse(":last-of-type") instanceof Evaluator.IsLastOfType);
        assertTrue(QueryParser.parse(":only-child") instanceof Evaluator.IsOnlyChild);
        assertTrue(QueryParser.parse(":only-of-type") instanceof Evaluator.IsOnlyOfType);
        assertTrue(QueryParser.parse(":empty") instanceof Evaluator.IsEmpty);
        assertTrue(QueryParser.parse(":root") instanceof Evaluator.IsRoot);
    }

    // Tests text matching pseudo selectors
    @Test
    public void testParse_textAndRegexSelectors_returnsEvaluators() {
        assertTrue(QueryParser.parse(":contains(text)") instanceof Evaluator.ContainsText);
        assertTrue(QueryParser.parse(":containsOwn(text)") instanceof Evaluator.ContainsOwnText);
        assertTrue(QueryParser.parse(":containsData(data)") instanceof Evaluator.ContainsData);
        assertTrue(QueryParser.parse(":matches(\\d+)") instanceof Evaluator.Matches);
        assertTrue(QueryParser.parse(":matchesOwn(\\d+)") instanceof Evaluator.MatchesOwn);
    }

    // Tests :has and :not sub-queries
    @Test
    public void testParse_hasAndNotSubQueries_returnsStructuralEvaluators() {
        assertTrue(QueryParser.parse(":has(p.highlight)") instanceof StructuralEvaluator.Has);
        assertTrue(QueryParser.parse(":not(div.ignore)") instanceof StructuralEvaluator.Not);
    }

    // Tests invalid query token throws SelectorParseException
    @Test(expected = Selector.SelectorParseException.class)
    public void testParse_invalidToken_throwsSelectorParseException() {
        QueryParser.parse(":unknownPseudo");
    }

    // Tests empty subselect in :has throws SelectorParseException
    @Test(expected = Selector.SelectorParseException.class)
    public void testParse_emptyHasSubQuery_throwsSelectorParseException() {
        QueryParser.parse(":has()");
    }

    // Tests non-numeric index throws SelectorParseException
    @Test(expected = Selector.SelectorParseException.class)
    public void testParse_nonNumericIndex_throwsSelectorParseException() {
        QueryParser.parse(":eq(abc)");
    }

    // Tests malformed nth-child expression throws SelectorParseException
    @Test(expected = Selector.SelectorParseException.class)
    public void testParse_invalidNthChildFormat_throwsSelectorParseException() {
        QueryParser.parse(":nth-child(foo)");
    }
}