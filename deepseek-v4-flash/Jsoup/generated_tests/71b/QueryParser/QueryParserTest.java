package org.jsoup.select;

import org.junit.Test;
import static org.junit.Assert.*;

public class QueryParserTest {

    @Test
    public void testParse_simpleTag_returnsTagEvaluator() {
        Evaluator eval = QueryParser.parse("div");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.Tag);
    }

    @Test
    public void testParse_simpleId_returnsIdEvaluator() {
        Evaluator eval = QueryParser.parse("#myId");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.Id);
    }

    @Test
    public void testParse_simpleClass_returnsClassEvaluator() {
        Evaluator eval = QueryParser.parse(".myClass");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.Class);
    }

    @Test
    public void testParse_attributeKeyOnly_returnsAttributeEvaluator() {
        Evaluator eval = QueryParser.parse("[href]");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.Attribute);
    }

    @Test
    public void testParse_attributeEq_returnsAttributeWithValueEvaluator() {
        Evaluator eval = QueryParser.parse("[href=test]");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.AttributeWithValue);
    }

    @Test
    public void testParse_attributeNotEq_returnsAttributeWithValueNotEvaluator() {
        Evaluator eval = QueryParser.parse("[href!=test]");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.AttributeWithValueNot);
    }

    @Test
    public void testParse_attributeStartsWith_returnsAttributeWithValueStartingEvaluator() {
        Evaluator eval = QueryParser.parse("[href^=http]");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.AttributeWithValueStarting);
    }

    @Test
    public void testParse_childCombinator_returnsCombiningAnd() {
        Evaluator eval = QueryParser.parse("div > p");
        assertNotNull(eval);
        assertTrue(eval instanceof CombiningEvaluator.And);
    }

    @Test
    public void testParse_descendantCombinator_returnsCombiningAnd() {
        Evaluator eval = QueryParser.parse("div p");
        assertNotNull(eval);
        assertTrue(eval instanceof CombiningEvaluator.And);
    }

    @Test
    public void testParse_nextSiblingCombinator_returnsCombiningAnd() {
        Evaluator eval = QueryParser.parse("div + p");
        assertNotNull(eval);
        assertTrue(eval instanceof CombiningEvaluator.And);
    }

    @Test
    public void testParse_subsequentSiblingCombinator_returnsCombiningAnd() {
        Evaluator eval = QueryParser.parse("div ~ p");
        assertNotNull(eval);
        assertTrue(eval instanceof CombiningEvaluator.And);
    }

    @Test
    public void testParse_commaOr_returnsOr() {
        Evaluator eval = QueryParser.parse("div, p");
        assertNotNull(eval);
        assertTrue(eval instanceof CombiningEvaluator.Or);
    }

    @Test
    public void testParse_pseudoHas_returnsHasEvaluator() {
        Evaluator eval = QueryParser.parse(":has(div)");
        assertNotNull(eval);
        assertTrue(eval instanceof StructuralEvaluator.Has);
    }

    @Test
    public void testParse_pseudoNot_returnsNotEvaluator() {
        Evaluator eval = QueryParser.parse(":not(.class)");
        assertNotNull(eval);
        assertTrue(eval instanceof StructuralEvaluator.Not);
    }

    @Test
    public void testParse_pseudoHasWithCombinator_returnsHasEvaluator() {
        Evaluator eval = QueryParser.parse(":has(div > p)");
        assertNotNull(eval);
        assertTrue(eval instanceof StructuralEvaluator.Has);
    }

    @Test
    public void testParse_pseudoContains_returnsContainsTextEvaluator() {
        Evaluator eval = QueryParser.parse(":contains(text)");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.ContainsText);
    }

    @Test
    public void testParse_pseudoContainsOwn_returnsContainsOwnTextEvaluator() {
        Evaluator eval = QueryParser.parse(":containsOwn(own text)");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.ContainsOwnText);
    }

    @Test
    public void testParse_pseudoMatches_returnsMatchesEvaluator() {
        Evaluator eval = QueryParser.parse(":matches(\\d+)");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.Matches);
    }

    @Test
    public void testParse_pseudoMatchesOwn_returnsMatchesOwnEvaluator() {
        Evaluator eval = QueryParser.parse(":matchesOwn(\\w+)");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.MatchesOwn);
    }

    @Test
    public void testParse_pseudoFirstChild_returnsIsFirstChildEvaluator() {
        Evaluator eval = QueryParser.parse(":first-child");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.IsFirstChild);
    }

    @Test
    public void testParse_pseudoLastChild_returnsIsLastChildEvaluator() {
        Evaluator eval = QueryParser.parse(":last-child");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.IsLastChild);
    }

    @Test
    public void testParse_pseudoNthChildOdd_returnsIsNthChildEvaluator() {
        Evaluator eval = QueryParser.parse(":nth-child(odd)");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.IsNthChild);
    }

    @Test
    public void testParse_pseudoNthOfTypeOdd_returnsIsNthOfTypeEvaluator() {
        Evaluator eval = QueryParser.parse(":nth-of-type(odd)");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.IsNthOfType);
    }

    @Test
    public void testParse_pseudoEmpty_returnsIsEmptyEvaluator() {
        Evaluator eval = QueryParser.parse(":empty");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.IsEmpty);
    }

    @Test
    public void testParse_pseudoRoot_returnsIsRootEvaluator() {
        Evaluator eval = QueryParser.parse(":root");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.IsRoot);
    }

    @Test
    public void testParse_allElements_returnsAllElementsEvaluator() {
        Evaluator eval = QueryParser.parse("*");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.AllElements);
    }

    @Test
    public void testParse_complexSelector_returnsCombiningAnd() {
        Evaluator eval = QueryParser.parse("div.class#id");
        assertNotNull(eval);
        assertTrue(eval instanceof CombiningEvaluator.And);
    }

    @Test(expected = Selector.SelectorParseException.class)
    public void testParse_invalidQuery_throwsSelectorParseException() {
        QueryParser.parse("div[");
    }

    @Test(expected = Selector.SelectorParseException.class)
    public void testParse_emptyQuery_throwsSelectorParseException() {
        QueryParser.parse("");
    }

    @Test(expected = Selector.SelectorParseException.class)
    public void testParse_whitespaceQuery_throwsSelectorParseException() {
        QueryParser.parse("   ");
    }

    @Test(expected = Selector.SelectorParseException.class)
    public void testParse_nullQuery_throwsSelectorParseException() {
        QueryParser.parse(null);
    }

    // ========== New test cases for uncovered coverage ==========

    // Attribute selectors additional types
    @Test
    public void testParse_attributeContainsWord_returnsAttributeWithValueContainingWordEvaluator() {
        Evaluator eval = QueryParser.parse("[class~=word]");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.AttributeWithValueContainingWord);
    }

    @Test
    public void testParse_attributeStartsWithPrefix_returnsAttributeWithValuePrefixEvaluator() {
        Evaluator eval = QueryParser.parse("[lang|=en]");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.AttributeWithValuePrefix);
    }

    @Test
    public void testParse_attributeEndsWith_returnsAttributeWithValueEndingEvaluator() {
        Evaluator eval = QueryParser.parse("[href$=.com]");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.AttributeWithValueEnding);
    }

    @Test
    public void testParse_attributeContains_returnsAttributeWithValueContainingEvaluator() {
        Evaluator eval = QueryParser.parse("[href*=example]");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.AttributeWithValueContaining);
    }

    // Pseudo-classes :eq, :gt, :lt
    @Test
    public void testParse_pseudoEq_returnsIndexEqualsEvaluator() {
        Evaluator eval = QueryParser.parse(":eq(0)");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.IndexEquals);
    }

    @Test
    public void testParse_pseudoGt_returnsIndexGreaterThanEvaluator() {
        Evaluator eval = QueryParser.parse(":gt(2)");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.IndexGreaterThan);
    }

    @Test
    public void testParse_pseudoLt_returnsIndexLessThanEvaluator() {
        Evaluator eval = QueryParser.parse(":lt(5)");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.IndexLessThan);
    }

    // Pseudo-classes :first-of-type, :last-of-type, :only-child, :only-of-type
    @Test
    public void testParse_pseudoFirstOfType_returnsIsFirstOfTypeEvaluator() {
        Evaluator eval = QueryParser.parse(":first-of-type");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.IsFirstOfType);
    }

    @Test
    public void testParse_pseudoLastOfType_returnsIsLastOfTypeEvaluator() {
        Evaluator eval = QueryParser.parse(":last-of-type");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.IsLastOfType);
    }

    @Test
    public void testParse_pseudoOnlyChild_returnsIsOnlyChildEvaluator() {
        Evaluator eval = QueryParser.parse(":only-child");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.IsOnlyChild);
    }

    @Test
    public void testParse_pseudoOnlyOfType_returnsIsOnlyOfTypeEvaluator() {
        Evaluator eval = QueryParser.parse(":only-of-type");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.IsOnlyOfType);
    }

    // Nth-child and Nth-of-type with even and formula
    @Test
    public void testParse_pseudoNthChildEven_returnsIsNthChildEvaluator() {
        Evaluator eval = QueryParser.parse(":nth-child(even)");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.IsNthChild);
    }

    @Test
    public void testParse_pseudoNthChildFormula_returnsIsNthChildEvaluator() {
        Evaluator eval = QueryParser.parse(":nth-child(2n+1)");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.IsNthChild);
    }

    @Test
    public void testParse_pseudoNthOfTypeEven_returnsIsNthOfTypeEvaluator() {
        Evaluator eval = QueryParser.parse(":nth-of-type(even)");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.IsNthOfType);
    }

    @Test
    public void testParse_pseudoNthOfTypeFormula_returnsIsNthOfTypeEvaluator() {
        Evaluator eval = QueryParser.parse(":nth-of-type(3n)");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.IsNthOfType);
    }

    // Nth-last-child and Nth-last-of-type
    @Test
    public void testParse_pseudoNthLastChild_returnsIsNthLastChildEvaluator() {
        Evaluator eval = QueryParser.parse(":nth-last-child(odd)");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.IsNthLastChild);
    }

    @Test
    public void testParse_pseudoNthLastOfType_returnsIsNthLastOfTypeEvaluator() {
        Evaluator eval = QueryParser.parse(":nth-last-of-type(odd)");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.IsNthLastOfType);
    }

    // Form-related pseudo-classes
    @Test
    public void testParse_pseudoChecked_returnsCheckedEvaluator() {
        Evaluator eval = QueryParser.parse(":checked");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.Checked);
    }

    @Test
    public void testParse_pseudoDisabled_returnsDisabledEvaluator() {
        Evaluator eval = QueryParser.parse(":disabled");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.Disabled);
    }

    @Test
    public void testParse_pseudoEnabled_returnsEnabledEvaluator() {
        Evaluator eval = QueryParser.parse(":enabled");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.Enabled);
    }

    @Test
    public void testParse_pseudoSelected_returnsSelectedEvaluator() {
        Evaluator eval = QueryParser.parse(":selected");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.Selected);
    }

    @Test
    public void testParse_pseudoInput_returnsInputEvaluator() {
        Evaluator eval = QueryParser.parse(":input");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.Input);
    }

    @Test
    public void testParse_pseudoButton_returnsButtonEvaluator() {
        Evaluator eval = QueryParser.parse(":button");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.Button);
    }

    @Test
    public void testParse_pseudoText_returnsTextEvaluator() {
        Evaluator eval = QueryParser.parse(":text");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.Text);
    }

    @Test
    public void testParse_pseudoRadio_returnsRadioEvaluator() {
        Evaluator eval = QueryParser.parse(":radio");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.Radio);
    }

    @Test
    public void testParse_pseudoCheckbox_returnsCheckboxEvaluator() {
        Evaluator eval = QueryParser.parse(":checkbox");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.Checkbox);
    }

    @Test
    public void testParse_pseudoFile_returnsFileEvaluator() {
        Evaluator eval = QueryParser.parse(":file");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.File);
    }

    @Test
    public void testParse_pseudoImage_returnsImageEvaluator() {
        Evaluator eval = QueryParser.parse(":image");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.Image);
    }

    @Test
    public void testParse_pseudoReset_returnsResetEvaluator() {
        Evaluator eval = QueryParser.parse(":reset");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.Reset);
    }

    @Test
    public void testParse_pseudoSubmit_returnsSubmitEvaluator() {
        Evaluator eval = QueryParser.parse(":submit");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.Submit);
    }

    // Complex combinators
    @Test
    public void testParse_multipleCombinator_returnsCombiningAnd() {
        Evaluator eval = QueryParser.parse("div > p + span");
        assertNotNull(eval);
        assertTrue(eval instanceof CombiningEvaluator.And);
    }

    @Test
    public void testParse_multipleComma_returnsOr() {
        Evaluator eval = QueryParser.parse("div, p, span");
        assertNotNull(eval);
        assertTrue(eval instanceof CombiningEvaluator.Or);
    }

    @Test
    public void testParse_childCombinatorNoSpace_returnsCombiningAnd() {
        Evaluator eval = QueryParser.parse("div>p");
        assertNotNull(eval);
        assertTrue(eval instanceof CombiningEvaluator.And);
    }

    // Complex selectors with multiple attributes
    @Test
    public void testParse_multipleAttributes_returnsCombiningAnd() {
        Evaluator eval = QueryParser.parse("div[class][id]");
        assertNotNull(eval);
        assertTrue(eval instanceof CombiningEvaluator.And);
    }

    @Test
    public void testParse_attributeCombinedWithClass_returnsCombiningAnd() {
        Evaluator eval = QueryParser.parse("div.myClass[attr=val]");
        assertNotNull(eval);
        assertTrue(eval instanceof CombiningEvaluator.And);
    }

    // Pseudo-class with complex not
    @Test
    public void testParse_pseudoNotWithCombinator_returnsNotEvaluator() {
        Evaluator eval = QueryParser.parse(":not(div > p)");
        assertNotNull(eval);
        assertTrue(eval instanceof StructuralEvaluator.Not);
    }

    // Pseudo-class with argument containing spaces
    @Test
    public void testParse_pseudoContainsWithSpaces_returnsContainsTextEvaluator() {
        Evaluator eval = QueryParser.parse(":contains(hello world)");
        assertNotNull(eval);
        assertTrue(eval instanceof Evaluator.ContainsText);
    }

    // Error cases for unclosed or invalid syntax
    @Test(expected = Selector.SelectorParseException.class)
    public void testParse_unclosedPseudo_throwsSelectorParseException() {
        QueryParser.parse(":has(div");
    }

    @Test(expected = Selector.SelectorParseException.class)
    public void testParse_unclosedAttribute_throwsSelectorParseException() {
        QueryParser.parse("[href=test");
    }

    @Test(expected = Selector.SelectorParseException.class)
    public void testParse_invalidPseudo_throwsSelectorParseException() {
        QueryParser.parse(":unknown");
    }

    @Test(expected = Selector.SelectorParseException.class)
    public void testParse_invalidAttributeValue_throwsSelectorParseException() {
        QueryParser.parse("[href=]");
    }

    @Test(expected = Selector.SelectorParseException.class)
    public void testParse_invalidCombinator_throwsSelectorParseException() {
        QueryParser.parse("div >>> p");
    }

    @Test(expected = Selector.SelectorParseException.class)
    public void testParse_emptyAttributeKey_throwsSelectorParseException() {
        QueryParser.parse("[]");
    }
}