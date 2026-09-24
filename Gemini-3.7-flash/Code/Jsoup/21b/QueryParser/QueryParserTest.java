package org.jsoup.select;

import org.junit.Test;
import static org.junit.Assert.*;

public class QueryParserTest {

    // Tests tag name parsing
    @Test
    public void testParse_tag_returnsTagEvaluator() {
        Evaluator eval = QueryParser.parse("div");
        assertTrue(eval instanceof Evaluator.Tag);
        assertEquals("div", eval.toString());
    }

    // Tests tag name with namespace parsing
    @Test
    public void testParse_tagWithNamespace_returnsTagEvaluatorWithColon() {
        Evaluator eval = QueryParser.parse("ns|div");
        assertTrue(eval instanceof Evaluator.Tag);
        assertEquals("ns:div", eval.toString());
    }

    // Tests ID selector parsing
    @Test
    public void testParse_id_returnsIdEvaluator() {
        Evaluator eval = QueryParser.parse("#main");
        assertTrue(eval instanceof Evaluator.Id);
        assertEquals("#main", eval.toString());
    }

    // Tests class selector parsing
    @Test
    public void testParse_class_returnsClassEvaluator() {
        Evaluator eval = QueryParser.parse(".highlight");
        assertTrue(eval instanceof Evaluator.Class);
        assertEquals(".highlight", eval.toString());
    }

    // Tests wildcard all elements selector parsing
    @Test
    public void testParse_allElements_returnsAllElementsEvaluator() {
        Evaluator eval = QueryParser.parse("*");
        assertTrue(eval instanceof Evaluator.AllElements);
        assertEquals("*", eval.toString());
    }

    // Tests attribute exists selector parsing
    @Test
    public void testParse_attribute_returnsAttributeEvaluator() {
        Evaluator eval = QueryParser.parse("[href]");
        assertTrue(eval instanceof Evaluator.Attribute);
        assertEquals("[href]", eval.toString());
    }

    // Tests attribute prefix selector parsing
    @Test
    public void testParse_attributeStarting_returnsAttributeStartingEvaluator() {
        Evaluator eval = QueryParser.parse("[^data-]");
        assertTrue(eval instanceof Evaluator.AttributeStarting);
        assertEquals("[^data-]", eval.toString());
    }

    // Tests attribute operators (=, !=, ^=, $=, *=, ~=)
    @Test
    public void testParse_attributeOperators_returnsCorrectEvaluators() {
        assertTrue(QueryParser.parse("[href=val]") instanceof Evaluator.AttributeWithValue);
        assertTrue(QueryParser.parse("[href!=val]") instanceof Evaluator.AttributeWithValueNot);
        assertTrue(QueryParser.parse("[href^=val]") instanceof Evaluator.AttributeWithValueStarting);
        assertTrue(QueryParser.parse("[href$=val]") instanceof Evaluator.AttributeWithValueEnding);
        assertTrue(QueryParser.parse("[href*=val]") instanceof Evaluator.AttributeWithValueContaining);
        assertTrue(QueryParser.parse("[href~=val]") instanceof Evaluator.AttributeWithValueMatching);
    }

    // Tests index pseudo-selectors (:lt, :gt, :eq)
    @Test
    public void testParse_indexSelectors_returnsIndexEvaluators() {
        assertTrue(QueryParser.parse(":lt(3)") instanceof Evaluator.IndexLessThan);
        assertTrue(QueryParser.parse(":gt(1)") instanceof Evaluator.IndexGreaterThan);
        assertTrue(QueryParser.parse(":eq(2)") instanceof Evaluator.IndexEquals);
    }

    // Tests non-numeric index pseudo-selector exception path
    @Test(expected = IllegalArgumentException.class)
    public void testParse_nonNumericIndex_throwsException() {
        QueryParser.parse(":eq(abc)");
    }

    // Tests structural pseudo-selectors (:has, :not)
    @Test
    public void testParse_structuralPseudo_returnsStructuralEvaluators() {
        assertTrue(QueryParser.parse(":has(p)") instanceof StructuralEvaluator.Has);
        assertTrue(QueryParser.parse(":not(div)") instanceof StructuralEvaluator.Not);
    }

    // Tests text matching pseudo-selectors (:contains, :containsOwn, :matches, :matchesOwn)
    @Test
    public void testParse_textPseudo_returnsTextEvaluators() {
        assertTrue(QueryParser.parse(":contains(test)") instanceof Evaluator.ContainsText);
        assertTrue(QueryParser.parse(":containsOwn(test)") instanceof Evaluator.ContainsOwnText);
        assertTrue(QueryParser.parse(":matches(test.*)") instanceof Evaluator.Matches);
        assertTrue(QueryParser.parse(":matchesOwn(test.*)") instanceof Evaluator.MatchesOwn);
    }

    // Tests combinators (>, +, ~, space)
    @Test
    public void testParse_combinators_returnsCombiningEvaluators() {
        assertTrue(QueryParser.parse("div > p") instanceof CombiningEvaluator.And);
        assertTrue(QueryParser.parse("div + p") instanceof CombiningEvaluator.And);
        assertTrue(QueryParser.parse("div ~ p") instanceof CombiningEvaluator.And);
        assertTrue(QueryParser.parse("div p") instanceof CombiningEvaluator.And);
    }

    // Tests query starting with a combinator (Root element evaluation)
    @Test
    public void testParse_startsWithCombinator_usesRoot() {
        Evaluator eval = QueryParser.parse("> p");
        assertTrue(eval instanceof CombiningEvaluator.And);
    }

    // Tests OR combinator (comma separated)
    @Test
    public void testParse_commaOrGroup_returnsOrEvaluator() {
        Evaluator eval = QueryParser.parse("div, p, span");
        assertTrue(eval instanceof CombiningEvaluator.Or);
    }

    // Tests pseudo-selector containing comma to detect parser split issues
    @Test
    public void testParse_pseudoWithComma_handlesBalancedTokensCorrectly() {
        Evaluator eval = QueryParser.parse("p:matches(a, b)");
        assertNotNull(eval);
    }

    // Tests multiple chained selectors without combinators (E.class#id[attr])
    @Test
    public void testParse_chainedSelectors_returnsAndEvaluator() {
        Evaluator eval = QueryParser.parse("div#main.active[hidden]");
        assertTrue(eval instanceof CombiningEvaluator.And);
    }

    // Tests invalid query exception handling
    @Test(expected = Selector.SelectorParseException.class)
    public void testParse_invalidToken_throwsParseException() {
        QueryParser.parse("div;;;");
    }

    // Tests invalid attribute syntax exception handling
    @Test(expected = Selector.SelectorParseException.class)
    public void testParse_invalidAttributeSyntax_throwsParseException() {
        QueryParser.parse("[href?val]");
    }

    // Tests structural position pseudo-selectors (:empty, :root, :first-child, etc.)
    @Test
    public void testParse_structuralPositionPseudos() {
        assertTrue(QueryParser.parse(":empty") instanceof Evaluator.IsEmpty);
        assertTrue(QueryParser.parse(":root") instanceof Evaluator.IsRoot);
        assertTrue(QueryParser.parse(":first-child") instanceof Evaluator.IsFirstChild);
        assertTrue(QueryParser.parse(":last-child") instanceof Evaluator.IsLastChild);
        assertTrue(QueryParser.parse(":first-of-type") instanceof Evaluator.IsFirstOfType);
        assertTrue(QueryParser.parse(":last-of-type") instanceof Evaluator.IsLastOfType);
        assertTrue(QueryParser.parse(":only-child") instanceof Evaluator.IsOnlyChild);
        assertTrue(QueryParser.parse(":only-of-type") instanceof Evaluator.IsOnlyOfType);
    }

    // Tests nth-child pseudo-selectors variations
    @Test
    public void testParse_nthChildVariations() {
        assertTrue(QueryParser.parse(":nth-child(2)") instanceof Evaluator.IsNthChild);
        assertTrue(QueryParser.parse(":nth-last-child(2)") instanceof Evaluator.IsNthLastChild);
        assertTrue(QueryParser.parse(":nth-of-type(2)") instanceof Evaluator.IsNthOfType);
        assertTrue(QueryParser.parse(":nth-last-of-type(2)") instanceof Evaluator.IsNthLastOfType);

        assertTrue(QueryParser.parse(":nth-child(odd)") instanceof Evaluator.IsNthChild);
        assertTrue(QueryParser.parse(":nth-child(even)") instanceof Evaluator.IsNthChild);
        assertTrue(QueryParser.parse(":nth-child(2n+1)") instanceof Evaluator.IsNthChild);
        assertTrue(QueryParser.parse(":nth-child(-2n+1)") instanceof Evaluator.IsNthChild);
        assertTrue(QueryParser.parse(":nth-child(n)") instanceof Evaluator.IsNthChild);
        assertTrue(QueryParser.parse(":nth-child(-n)") instanceof Evaluator.IsNthChild);
    }

    // Tests containsData pseudo-selector
    @Test
    public void testParse_containsDataPseudo() {
        assertTrue(QueryParser.parse(":containsData(scriptData)") instanceof Evaluator.ContainsData);
    }

    // Tests attribute selectors with quoted values
    @Test
    public void testParse_quotedAttributes() {
        assertTrue(QueryParser.parse("[href='http://example.com']") instanceof Evaluator.AttributeWithValue);
        assertTrue(QueryParser.parse("[href=\"http://example.com\"]") instanceof Evaluator.AttributeWithValue);
        assertTrue(QueryParser.parse("[href^='http://']") instanceof Evaluator.AttributeWithValueStarting);
    }

    // Tests leading sibling combinators (+, ~)
    @Test
    public void testParse_leadingSiblingCombinators() {
        assertTrue(QueryParser.parse("+ p") instanceof CombiningEvaluator.And);
        assertTrue(QueryParser.parse("~ p") instanceof CombiningEvaluator.And);
    }
}