package org.jsoup.select;

import org.junit.Test;
import static org.junit.Assert.*;

public class QueryParserTest {

    // Tests simple tag evaluator parsing
    @Test
    public void testParse_simpleTag_returnsTagEvaluator() {
        Evaluator eval = QueryParser.parse("div");
        assertTrue(eval instanceof Evaluator.Tag);
    }

    // Tests ID selector parsing
    @Test
    public void testParse_byId_returnsIdEvaluator() {
        Evaluator eval = QueryParser.parse("#main");
        assertTrue(eval instanceof Evaluator.Id);
    }

    // Tests Class selector parsing
    @Test
    public void testParse_byClass_returnsClassEvaluator() {
        Evaluator eval = QueryParser.parse(".content");
        assertTrue(eval instanceof Evaluator.Class);
    }

    // Tests wildcard / all elements selector
    @Test
    public void testParse_allElements_returnsAllElementsEvaluator() {
        Evaluator eval = QueryParser.parse("*");
        assertTrue(eval instanceof Evaluator.AllElements);
    }

    // Tests namespace prefix handling in tag selector
    @Test
    public void testParse_namespaceTag_returnsEvaluator() {
        Evaluator eval = QueryParser.parse("ns|div");
        assertTrue(eval instanceof Evaluator.Tag);

        Evaluator wildcardEval = QueryParser.parse("*|div");
        assertTrue(wildcardEval instanceof CombiningEvaluator.Or);
    }

    // Tests simple attribute selector
    @Test
    public void testParse_attributeKeyOnly_returnsAttributeEvaluator() {
        Evaluator eval = QueryParser.parse("[href]");
        assertTrue(eval instanceof Evaluator.Attribute);
    }

    // Tests attribute prefix matching (^attr)
    @Test
    public void testParse_attributeStarting_returnsAttributeStartingEvaluator() {
        Evaluator eval = QueryParser.parse("[^data-]");
        assertTrue(eval instanceof Evaluator.AttributeStarting);
    }

    // Tests attribute value comparison operators
    @Test
    public void testParse_attributeValueOperators_returnsRespectiveEvaluators() {
        assertTrue(QueryParser.parse("[title=test]") instanceof Evaluator.AttributeWithValue);
        assertTrue(QueryParser.parse("[title!=test]") instanceof Evaluator.AttributeWithValueNot);
        assertTrue(QueryParser.parse("[title^=test]") instanceof Evaluator.AttributeWithValueStarting);
        assertTrue(QueryParser.parse("[title$=test]") instanceof Evaluator.AttributeWithValueEnding);
        assertTrue(QueryParser.parse("[title*=test]") instanceof Evaluator.AttributeWithValueContaining);
        assertTrue(QueryParser.parse("[title~=test]") instanceof Evaluator.AttributeWithValueMatching);
    }

    // Tests structural combinators (parent, child, siblings)
    @Test
    public void testParse_combinators_returnsCombiningEvaluators() {
        assertTrue(QueryParser.parse("div p") instanceof CombiningEvaluator.And);
        assertTrue(QueryParser.parse("div > p") instanceof CombiningEvaluator.And);
        assertTrue(QueryParser.parse("div + p") instanceof CombiningEvaluator.And);
        assertTrue(QueryParser.parse("div ~ p") instanceof CombiningEvaluator.And);
        assertTrue(QueryParser.parse("div, p") instanceof CombiningEvaluator.Or);
    }

    // Tests starting query with combinator (root element handling)
    @Test
    public void testParse_startsWithCombinator_returnsRootCombiningEvaluator() {
        Evaluator eval = QueryParser.parse("> p");
        assertTrue(eval instanceof CombiningEvaluator.And);
    }

    // Tests pseudo structural selectors
    @Test
    public void testParse_pseudoSelectors_returnsExpectedEvaluators() {
        assertTrue(QueryParser.parse(":first-child") instanceof Evaluator.IsFirstChild);
        assertTrue(QueryParser.parse(":last-child") instanceof Evaluator.IsLastChild);
        assertTrue(QueryParser.parse(":first-of-type") instanceof Evaluator.IsFirstOfType);
        assertTrue(QueryParser.parse(":last-of-type") instanceof Evaluator.IsLastOfType);
        assertTrue(QueryParser.parse(":only-child") instanceof Evaluator.IsOnlyChild);
        assertTrue(QueryParser.parse(":only-of-type") instanceof Evaluator.IsOnlyOfType);
        assertTrue(QueryParser.parse(":empty") instanceof Evaluator.IsEmpty);
        assertTrue(QueryParser.parse(":root") instanceof Evaluator.IsRoot);
    }

    // Tests index-based pseudo selectors (:lt, :gt, :eq)
    @Test
    public void testParse_indexPseudos_returnsIndexEvaluators() {
        assertTrue(QueryParser.parse(":lt(2)") instanceof Evaluator.IndexLessThan);
        assertTrue(QueryParser.parse(":gt(2)") instanceof Evaluator.IndexGreaterThan);
        assertTrue(QueryParser.parse(":eq(2)") instanceof Evaluator.IndexEquals);
    }

    // Tests nth-child variants with an+b, odd, even syntax
    @Test
    public void testParse_nthChildVariants_returnsNthEvaluators() {
        assertTrue(QueryParser.parse(":nth-child(odd)") instanceof Evaluator.IsNthChild);
        assertTrue(QueryParser.parse(":nth-child(even)") instanceof Evaluator.IsNthChild);
        assertTrue(QueryParser.parse(":nth-child(2n+1)") instanceof Evaluator.IsNthChild);
        assertTrue(QueryParser.parse(":nth-last-child(2)") instanceof Evaluator.IsNthLastChild);
        assertTrue(QueryParser.parse(":nth-of-type(1)") instanceof Evaluator.IsNthOfType);
        assertTrue(QueryParser.parse(":nth-last-of-type(1)") instanceof Evaluator.IsNthLastOfType);
    }

    // Tests functional pseudo selectors (:has, :not, :contains, :matches)
    @Test
    public void testParse_functionalPseudos_returnsExpectedEvaluators() {
        assertTrue(QueryParser.parse(":has(p)") instanceof StructuralEvaluator.Has);
        assertTrue(QueryParser.parse(":not(p)") instanceof StructuralEvaluator.Not);
        assertTrue(QueryParser.parse(":contains(text)") instanceof Evaluator.ContainsText);
        assertTrue(QueryParser.parse(":containsOwn(text)") instanceof Evaluator.ContainsOwnText);
        assertTrue(QueryParser.parse(":containsData(data)") instanceof Evaluator.ContainsData);
        assertTrue(QueryParser.parse(":matches(regex)") instanceof Evaluator.Matches);
        assertTrue(QueryParser.parse(":matchesOwn(regex)") instanceof Evaluator.MatchesOwn);
    }

    // Tests unexpected token throwing SelectorParseException
    @Test(expected = Selector.SelectorParseException.class)
    public void testParse_unexpectedToken_throwsSelectorParseException() {
        QueryParser.parse("div / p");
    }

    // Tests empty attribute key throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testParse_emptyAttribute_throwsException() {
        QueryParser.parse("[]");
    }

    // Tests invalid nth-child format throwing SelectorParseException
    @Test(expected = Selector.SelectorParseException.class)
    public void testParse_invalidNthChildFormat_throwsSelectorParseException() {
        QueryParser.parse(":nth-child(invalid)");
    }

    // Tests unclosed bracket / pseudo selector throwing SelectorParseException (Defects4J 60 bug target)
    @Test(expected = Selector.SelectorParseException.class)
    public void testParse_unclosedAttributeOrPseudo_throwsSelectorParseException() {
        QueryParser.parse("div[attr=");
    }
}