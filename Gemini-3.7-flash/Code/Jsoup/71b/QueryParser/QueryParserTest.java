package org.jsoup.select;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class QueryParserTest {

    // Tests simple tag, id, and class selectors
    @Test
    public void testParse_basicTagIdAndClass_returnsCombinedEvaluator() {
        Evaluator eval = QueryParser.parse("div#main.content");
        assertNotNull(eval);
        assertTrue(eval instanceof CombiningEvaluator.And);
    }

    // Tests namespaced tag selector handling
    @Test
    public void testParse_namespaceTag_returnsCorrectEvaluator() {
        Evaluator wildcardNs = QueryParser.parse("*|div");
        assertTrue(wildcardNs instanceof CombiningEvaluator.Or);

        Evaluator namedNs = QueryParser.parse("fb|like");
        assertTrue(namedNs instanceof Evaluator.Tag);
        assertEquals("fb:like", namedNs.toString());
    }

    // Tests attribute selector variations
    @Test
    public void testParse_attributeSelectors_returnsAttributeEvaluators() {
        assertTrue(QueryParser.parse("[href]") instanceof Evaluator.Attribute);
        assertTrue(QueryParser.parse("[^data-]") instanceof Evaluator.AttributeStarting);
        assertTrue(QueryParser.parse("[title=foo]") instanceof Evaluator.AttributeWithValue);
        assertTrue(QueryParser.parse("[title!=foo]") instanceof Evaluator.AttributeWithValueNot);
        assertTrue(QueryParser.parse("[title^=foo]") instanceof Evaluator.AttributeWithValueStarting);
        assertTrue(QueryParser.parse("[title$=foo]") instanceof Evaluator.AttributeWithValueEnding);
        assertTrue(QueryParser.parse("[title*=foo]") instanceof Evaluator.AttributeWithValueContaining);
        assertTrue(QueryParser.parse("[title~=foo]") instanceof Evaluator.AttributeWithValueMatching);
    }

    // Tests combinators: child, descendant, adjacent sibling, and general sibling
    @Test
    public void testParse_combinators_returnsStructuralEvaluators() {
        Evaluator child = QueryParser.parse("div > p");
        assertTrue(child instanceof CombiningEvaluator.And);

        Evaluator descendant = QueryParser.parse("div p");
        assertTrue(descendant instanceof CombiningEvaluator.And);

        Evaluator adjacent = QueryParser.parse("div + p");
        assertTrue(adjacent instanceof CombiningEvaluator.And);

        Evaluator sibling = QueryParser.parse("div ~ p");
        assertTrue(sibling instanceof CombiningEvaluator.And);
    }

    // Tests query starting with a combinator (leading combinator uses root)
    @Test
    public void testParse_leadingCombinator_addsRootEvaluator() {
        Evaluator eval = QueryParser.parse("> span");
        assertNotNull(eval);
        assertTrue(eval instanceof CombiningEvaluator.And);
    }

    // Tests group selector (comma / OR combinator) and precedence
    @Test
    public void testParse_orCombinator_returnsOrEvaluator() {
        Evaluator eval = QueryParser.parse("div, p, span");
        assertTrue(eval instanceof CombiningEvaluator.Or);

        Evaluator mixed = QueryParser.parse("div, p > span");
        assertTrue(mixed instanceof CombiningEvaluator.Or);
    }

    // Tests wildcard all elements selector
    @Test
    public void testParse_allElements_returnsAllElementsEvaluator() {
        Evaluator eval = QueryParser.parse("*");
        assertTrue(eval instanceof Evaluator.AllElements);
    }

    // Tests index-based pseudo-selectors: :lt, :gt, :eq
    @Test
    public void testParse_indexPseudoSelectors_returnsIndexEvaluators() {
        assertTrue(QueryParser.parse(":lt(3)") instanceof Evaluator.IndexLessThan);
        assertTrue(QueryParser.parse(":gt(1)") instanceof Evaluator.IndexGreaterThan);
        assertTrue(QueryParser.parse(":eq(2)") instanceof Evaluator.IndexEquals);
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

    // Tests :nth-child and variants with odd, even, formula, and digit
    @Test
    public void testParse_nthChildVariants_returnsNthEvaluators() {
        assertTrue(QueryParser.parse(":nth-child(odd)") instanceof Evaluator.IsNthChild);
        assertTrue(QueryParser.parse(":nth-child(even)") instanceof Evaluator.IsNthChild);
        assertTrue(QueryParser.parse(":nth-child(2n+1)") instanceof Evaluator.IsNthChild);
        assertTrue(QueryParser.parse(":nth-child(3)") instanceof Evaluator.IsNthChild);
        assertTrue(QueryParser.parse(":nth-last-child(2)") instanceof Evaluator.IsNthLastChild);
        assertTrue(QueryParser.parse(":nth-of-type(2n)") instanceof Evaluator.IsNthOfType);
        assertTrue(QueryParser.parse(":nth-last-of-type(1)") instanceof Evaluator.IsNthLastOfType);
    }

    // Tests relational and content pseudo-selectors (:has, :contains, :containsOwn, :containsData)
    @Test
    public void testParse_relationalAndContentPseudos_returnsCorrectEvaluators() {
        assertTrue(QueryParser.parse(":has(p)") instanceof StructuralEvaluator.Has);
        assertTrue(QueryParser.parse(":contains(text)") instanceof Evaluator.ContainsText);
        assertTrue(QueryParser.parse(":containsOwn(text)") instanceof Evaluator.ContainsOwnText);
        assertTrue(QueryParser.parse(":containsData(data)") instanceof Evaluator.ContainsData);
    }

    // Tests pattern matching and negation pseudo-selectors (:matches, :matchesOwn, :not)
    @Test
    public void testParse_regexAndNotPseudos_returnsCorrectEvaluators() {
        assertTrue(QueryParser.parse(":matches([a-z]+)") instanceof Evaluator.Matches);
        assertTrue(QueryParser.parse(":matchesOwn([a-z]+)") instanceof Evaluator.MatchesOwn);
        assertTrue(QueryParser.parse(":not(div.hide)") instanceof StructuralEvaluator.Not);
    }

    // Tests exception on unexpected unhandled pseudo token
    @Test(expected = Selector.SelectorParseException.class)
    public void testParse_unknownPseudo_throwsException() {
        QueryParser.parse(":unknown()");
    }

    // Tests exception on invalid nth-child expression
    @Test(expected = Selector.SelectorParseException.class)
    public void testParse_invalidNthChild_throwsException() {
        QueryParser.parse(":nth-child(abc)");
    }

    // Tests exception on non-numeric index pseudo parameter
    @Test(expected = Selector.SelectorParseException.class)
    public void testParse_nonNumericIndex_throwsException() {
        QueryParser.parse(":eq(abc)");
    }

    // Tests exception on empty subselect within :has
    @Test(expected = Selector.SelectorParseException.class)
    public void testParse_emptyHasPseudo_throwsException() {
        QueryParser.parse(":has()");
    }
}