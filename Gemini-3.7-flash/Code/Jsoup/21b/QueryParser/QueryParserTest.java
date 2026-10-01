package org.jsoup.select;

import org.jsoup.select.CombiningEvaluator;
import org.jsoup.select.Evaluator;
import org.jsoup.select.QueryParser;
import org.jsoup.select.Selector;
import org.jsoup.select.StructuralEvaluator;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class QueryParserTest {

    // Tests simple tag selector parsing
    @Test
    public void testParse_simpleTag_returnsTagEvaluator() {
        Evaluator eval = QueryParser.parse("div");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.Tag);
        assertEquals("div", ((Evaluator.Tag) eval).toString());
    }

    // Tests ID selector parsing
    @Test
    public void testParse_idSelector_returnsIdEvaluator() {
        Evaluator eval = QueryParser.parse("#main");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.Id);
    }

    // Tests class selector parsing
    @Test
    public void testParse_classSelector_returnsClassEvaluator() {
        Evaluator eval = QueryParser.parse(".content");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.Class);
    }

    // Tests wildcard all elements selector parsing
    @Test
    public void testParse_allElements_returnsAllElementsEvaluator() {
        Evaluator eval = QueryParser.parse("*");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.AllElements);
    }

    // Tests attribute presence and attribute starting selector parsing
    @Test
    public void testParse_attributeSelectors_returnsAttributeEvaluators() {
        Evaluator evalAttr = QueryParser.parse("[href]");
        assertTrue(evalAttr instanceof Evaluator.Attribute);

        Evaluator evalStarting = QueryParser.parse("[^data-]");
        assertTrue(evalStarting instanceof Evaluator.AttributeStarting);
    }

    // Tests attribute value comparison operators
    @Test
    public void testParse_attributeWithValues_returnsCorrectEvaluators() {
        assertTrue(QueryParser.parse("[title=hello]") instanceof Evaluator.AttributeWithValue);
        assertTrue(QueryParser.parse("[title!=hello]") instanceof Evaluator.AttributeWithValueNot);
        assertTrue(QueryParser.parse("[title^=hel]") instanceof Evaluator.AttributeWithValueStarting);
        assertTrue(QueryParser.parse("[title$=lo]") instanceof Evaluator.AttributeWithValueEnding);
        assertTrue(QueryParser.parse("[title*=ell]") instanceof Evaluator.AttributeWithValueContaining);
        assertTrue(QueryParser.parse("[title~=hel.*]") instanceof Evaluator.AttributeWithValueMatching);
    }

    // Tests child combinator (>) parsing
    @Test
    public void testParse_childCombinator_returnsImmediateParentEvaluator() {
        Evaluator eval = QueryParser.parse("div > p");
        assertNotNull(eval);
        assertTrue(eval instanceof CombiningEvaluator.And);
    }

    // Tests descendant combinator (space) parsing
    @Test
    public void testParse_descendantCombinator_returnsParentEvaluator() {
        Evaluator eval = QueryParser.parse("div p");
        assertNotNull(eval);
        assertTrue(eval instanceof CombiningEvaluator.And);
    }

    // Tests adjacent sibling (+) and general sibling (~) combinators
    @Test
    public void testParse_siblingCombinators_returnsSiblingEvaluators() {
        Evaluator evalAdj = QueryParser.parse("h1 + p");
        assertTrue(evalAdj instanceof CombiningEvaluator.And);

        Evaluator evalGen = QueryParser.parse("h1 ~ p");
        assertTrue(evalGen instanceof CombiningEvaluator.And);
    }

    // Tests leading combinator rooted query parsing
    @Test
    public void testParse_leadingCombinator_returnsRootEvaluator() {
        Evaluator eval = QueryParser.parse("> p");
        assertNotNull(eval);
        assertTrue(eval instanceof CombiningEvaluator.And);
    }

    // Tests comma OR group parsing
    @Test
    public void testParse_orCombinator_returnsOrEvaluator() {
        Evaluator eval = QueryParser.parse("div, p, span");
        assertNotNull(eval);
        assertTrue(eval instanceof CombiningEvaluator.Or);
    }

    // Tests pseudo index selectors: lt, gt, eq
    @Test
    public void testParse_pseudoIndexSelectors_returnsIndexEvaluators() {
        assertTrue(QueryParser.parse(":lt(3)") instanceof Evaluator.IndexLessThan);
        assertTrue(QueryParser.parse(":gt(1)") instanceof Evaluator.IndexGreaterThan);
        assertTrue(QueryParser.parse(":eq(2)") instanceof Evaluator.IndexEquals);
    }

    // Tests pseudo selectors: :has(), :not()
    @Test
    public void testParse_structuralPseudoSelectors_returnsStructuralEvaluators() {
        assertTrue(QueryParser.parse(":has(p)") instanceof StructuralEvaluator.Has);
        assertTrue(QueryParser.parse(":not(span)") instanceof StructuralEvaluator.Not);
    }

    // Tests text match pseudo selectors
    @Test
    public void testParse_textPseudoSelectors_returnsTextEvaluators() {
        assertTrue(QueryParser.parse(":contains(text)") instanceof Evaluator.ContainsText);
        assertTrue(QueryParser.parse(":containsOwn(text)") instanceof Evaluator.ContainsOwnText);
        assertTrue(QueryParser.parse(":matches(regex)") instanceof Evaluator.Matches);
        assertTrue(QueryParser.parse(":matchesOwn(regex)") instanceof Evaluator.MatchesOwn);
    }

    // Tests namespace tag selector syntax flip
    @Test
    public void testParse_namespaceTag_flipsPipeToColon() {
        Evaluator eval = QueryParser.parse("ns|div");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.Tag);
        assertEquals("ns:div", ((Evaluator.Tag) eval).toString());
    }

    // Tests exception path for invalid/unknown selector syntax
    @Test(expected = Selector.SelectorParseException.class)
    public void testParse_unknownToken_throwsSelectorParseException() {
        QueryParser.parse("div??invalid");
    }

    // Tests exception path for non-numeric index pseudo selector
    @Test(expected = IllegalArgumentException.class)
    public void testParse_nonNumericIndex_throwsIllegalArgumentException() {
        QueryParser.parse(":eq(abc)");
    }

    // Tests exception path for empty :not subselect
    @Test(expected = IllegalArgumentException.class)
    public void testParse_emptyNotSubselect_throwsIllegalArgumentException() {
        QueryParser.parse(":not()");
    }
}