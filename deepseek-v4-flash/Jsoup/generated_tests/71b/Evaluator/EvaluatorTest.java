package org.jsoup.select;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Test;
import java.util.regex.Pattern;
import static org.junit.Assert.*;

public class EvaluatorTest {

    // Tests Tag.matches method with matching tag name
    @Test
    public void testTagMatches_correctTagName_returnsTrue() {
        Evaluator.Tag eval = new Evaluator.Tag("div");
        Element div = Jsoup.parse("<div></div>").selectFirst("div");
        assertTrue(eval.matches(div, div));
    }

    // Tests Tag.matches method with non-matching tag name
    @Test
    public void testTagMatches_incorrectTagName_returnsFalse() {
        Evaluator.Tag eval = new Evaluator.Tag("span");
        Element div = Jsoup.parse("<div></div>").selectFirst("div");
        assertFalse(eval.matches(div, div));
    }

    // Tests Id.matches with matching id
    @Test
    public void testIdMatches_matchingId_returnsTrue() {
        Evaluator.Id eval = new Evaluator.Id("testId");
        Element el = Jsoup.parse("<div id='testId'></div>").selectFirst("div");
        assertTrue(eval.matches(el, el));
    }

    // Tests Id.matches with non-matching id
    @Test
    public void testIdMatches_nonMatchingId_returnsFalse() {
        Evaluator.Id eval = new Evaluator.Id("otherId");
        Element el = Jsoup.parse("<div id='testId'></div>").selectFirst("div");
        assertFalse(eval.matches(el, el));
    }

    // Tests Class.matches with element having the class
    @Test
    public void testClassMatches_elementHasClass_returnsTrue() {
        Evaluator.Class eval = new Evaluator.Class("myClass");
        Element el = Jsoup.parse("<div class='myClass'></div>").selectFirst("div");
        assertTrue(eval.matches(el, el));
    }

    // Tests Class.matches with element not having the class
    @Test
    public void testClassMatches_elementDoesNotHaveClass_returnsFalse() {
        Evaluator.Class eval = new Evaluator.Class("myClass");
        Element el = Jsoup.parse("<div></div>").selectFirst("div");
        assertFalse(eval.matches(el, el));
    }

    // Tests AllElements.matches always returns true
    @Test
    public void testAllElementsMatches_anyElement_returnsTrue() {
        Evaluator.AllElements eval = new Evaluator.AllElements();
        Element el = Jsoup.parse("<div></div>").selectFirst("div");
        assertTrue(eval.matches(el, el));
    }

    // Tests IsFirstChild.matches when element is first child
    @Test
    public void testIsFirstChildMatches_isFirstChild_returnsTrue() {
        Evaluator.IsFirstChild eval = new Evaluator.IsFirstChild();
        Document doc = Jsoup.parse("<ul><li>first</li><li>second</li></ul>");
        Element firstLi = doc.selectFirst("li");
        assertTrue(eval.matches(firstLi, firstLi));
    }

    // Tests IsFirstChild.matches when element is not first child
    @Test
    public void testIsFirstChildMatches_notFirstChild_returnsFalse() {
        Evaluator.IsFirstChild eval = new Evaluator.IsFirstChild();
        Document doc = Jsoup.parse("<ul><li>first</li><li>second</li></ul>");
        Element secondLi = doc.select("li").get(1);
        assertFalse(eval.matches(secondLi, secondLi));
    }

    // Tests IsLastChild.matches when element is last child
    @Test
    public void testIsLastChildMatches_isLastChild_returnsTrue() {
        Evaluator.IsLastChild eval = new Evaluator.IsLastChild();
        Document doc = Jsoup.parse("<ul><li>first</li><li>second</li></ul>");
        Element secondLi = doc.select("li").get(1);
        assertTrue(eval.matches(secondLi, secondLi));
    }

    // Tests IsEmpty.matches on empty element (no child nodes except comments/data)
    @Test
    public void testIsEmptyMatches_emptyElement_returnsTrue() {
        Evaluator.IsEmpty eval = new Evaluator.IsEmpty();
        Element el = Jsoup.parse("<div></div>").selectFirst("div");
        assertTrue(eval.matches(el, el));
    }

    // Tests IsEmpty.matches on non-empty element
    @Test
    public void testIsEmptyMatches_nonEmptyElement_returnsFalse() {
        Evaluator.IsEmpty eval = new Evaluator.IsEmpty();
        Element el = Jsoup.parse("<div><p>text</p></div>").selectFirst("div");
        assertFalse(eval.matches(el, el));
    }

    // Tests ContainsText.matches with element containing search text
    @Test
    public void testContainsTextMatches_textExists_returnsTrue() {
        Evaluator.ContainsText eval = new Evaluator.ContainsText("hello");
        Element el = Jsoup.parse("<div>hello world</div>").selectFirst("div");
        assertTrue(eval.matches(el, el));
    }

    // Tests ContainsText.matches with element not containing search text
    @Test
    public void testContainsTextMatches_textDoesNotExist_returnsFalse() {
        Evaluator.ContainsText eval = new Evaluator.ContainsText("bye");
        Element el = Jsoup.parse("<div>hello world</div>").selectFirst("div");
        assertFalse(eval.matches(el, el));
    }

    // Tests AttributeWithValueMatching.matches with matching regex
    @Test
    public void testAttributeWithValueMatching_matchingPattern_returnsTrue() {
        Evaluator.AttributeWithValueMatching eval = new Evaluator.AttributeWithValueMatching("data-value", Pattern.compile("\\d+"));
        Element el = Jsoup.parse("<div data-value='123'></div>").selectFirst("div");
        assertTrue(eval.matches(el, el));
    }

    // Tests AttributeWithValueMatching.matches with non-matching regex
    @Test
    public void testAttributeWithValueMatching_nonMatchingPattern_returnsFalse() {
        Evaluator.AttributeWithValueMatching eval = new Evaluator.AttributeWithValueMatching("data-value", Pattern.compile("[a-z]+"));
        Element el = Jsoup.parse("<div data-value='123'></div>").selectFirst("div");
        assertFalse(eval.matches(el, el));
    }

    // Tests IndexLessThan.matches with index less than bound
    @Test
    public void testIndexLessThanMatches_siblingIndexLessThanBound_returnsTrue() {
        Evaluator.IndexLessThan eval = new Evaluator.IndexLessThan(2);
        Document doc = Jsoup.parse("<ul><li>first</li><li>second</li><li>third</li></ul>");
        Element firstLi = doc.selectFirst("li");
        assertTrue(eval.matches(firstLi, firstLi));
    }

    // Tests IndexLessThan.matches with index greater than or equal to bound
    @Test
    public void testIndexLessThanMatches_siblingIndexNotLessThanBound_returnsFalse() {
        Evaluator.IndexLessThan eval = new Evaluator.IndexLessThan(1);
        Document doc = Jsoup.parse("<ul><li>first</li><li>second</li></ul>");
        Element secondLi = doc.select("li").get(1);
        assertFalse(eval.matches(secondLi, secondLi));
    }

    // Tests IsRoot.matches when element is the root
    @Test
    public void testIsRootMatches_rootElement_returnsTrue() {
        Evaluator.IsRoot eval = new Evaluator.IsRoot();
        Document doc = Jsoup.parse("<html><body></body></html>");
        Element html = doc.selectFirst("html");
        assertTrue(eval.matches(html, html));
    }

    // Tests IsRoot.matches when element is not root
    @Test
    public void testIsRootMatches_nonRootElement_returnsFalse() {
        Evaluator.IsRoot eval = new Evaluator.IsRoot();
        Document doc = Jsoup.parse("<html><body><p></p></body></html>");
        Element p = doc.selectFirst("p");
        assertFalse(eval.matches(p, p));
    }

    // Tests IsOnlyChild.matches with no siblings
    @Test
    public void testIsOnlyChildMatches_noSiblings_returnsTrue() {
        Evaluator.IsOnlyChild eval = new Evaluator.IsOnlyChild();
        Document doc = Jsoup.parse("<ul><li>only</li></ul>");
        Element li = doc.selectFirst("li");
        assertTrue(eval.matches(li, li));
    }

    // Tests IsOnlyChild.matches with siblings
    @Test
    public void testIsOnlyChildMatches_hasSiblings_returnsFalse() {
        Evaluator.IsOnlyChild eval = new Evaluator.IsOnlyChild();
        Document doc = Jsoup.parse("<ul><li>first</li><li>second</li></ul>");
        Element li = doc.selectFirst("li");
        assertFalse(eval.matches(li, li));
    }

    // ===================== New tests to improve coverage =====================

    // IsLastChild false case
    @Test
    public void testIsLastChildMatches_notLastChild_returnsFalse() {
        Evaluator.IsLastChild eval = new Evaluator.IsLastChild();
        Document doc = Jsoup.parse("<ul><li>first</li><li>second</li></ul>");
        Element firstLi = doc.selectFirst("li");
        assertFalse(eval.matches(firstLi, firstLi));
    }

    // IndexGreaterThan
    @Test
    public void testIndexGreaterThanMatches_siblingIndexGreaterThanBound_returnsTrue() {
        Evaluator.IndexGreaterThan eval = new Evaluator.IndexGreaterThan(0);
        Document doc = Jsoup.parse("<ul><li>first</li><li>second</li></ul>");
        Element secondLi = doc.select("li").get(1);
        assertTrue(eval.matches(secondLi, secondLi));
    }

    @Test
    public void testIndexGreaterThanMatches_siblingIndexNotGreaterThanBound_returnsFalse() {
        Evaluator.IndexGreaterThan eval = new Evaluator.IndexGreaterThan(1);
        Document doc = Jsoup.parse("<ul><li>first</li><li>second</li></ul>");
        Element firstLi = doc.selectFirst("li");
        assertFalse(eval.matches(firstLi, firstLi));
    }

    // IndexEquals
    @Test
    public void testIndexEqualsMatches_equalIndex_returnsTrue() {
        Evaluator.IndexEquals eval = new Evaluator.IndexEquals(1);
        Document doc = Jsoup.parse("<ul><li>first</li><li>second</li></ul>");
        Element secondLi = doc.select("li").get(1);
        assertTrue(eval.matches(secondLi, secondLi));
    }

    @Test
    public void testIndexEqualsMatches_notEqualIndex_returnsFalse() {
        Evaluator.IndexEquals eval = new Evaluator.IndexEquals(2);
        Document doc = Jsoup.parse("<ul><li>first</li><li>second</li></ul>");
        Element firstLi = doc.selectFirst("li");
        assertFalse(eval.matches(firstLi, firstLi));
    }

    // IsNthChild (2n - even)
    @Test
    public void testIsNthChildMatches_nthChildTrue_returnsTrue() {
        Evaluator.IsNthChild eval = new Evaluator.IsNthChild(2, 0);
        Document doc = Jsoup.parse("<ul><li>0</li><li>1</li><li>2</li><li>3</li></ul>");
        Element secondLi = doc.select("li").get(1); // index 1 -> 2nd child -> even
        assertTrue(eval.matches(secondLi, secondLi));
    }

    @Test
    public void testIsNthChildMatches_notNthChild_returnsFalse() {
        Evaluator.IsNthChild eval = new Evaluator.IsNthChild(2, 0);
        Document doc = Jsoup.parse("<ul><li>0</li><li>1</li><li>2</li></ul>");
        Element firstLi = doc.selectFirst("li"); // index 0 -> 1st child -> odd
        assertFalse(eval.matches(firstLi, firstLi));
    }

    // IsNthLastChild (even from end)
    @Test
    public void testIsNthLastChildMatches_nthLastChildTrue_returnsTrue() {
        Evaluator.IsNthLastChild eval = new Evaluator.IsNthLastChild(2, 0);
        Document doc = Jsoup.parse("<ul><li>0</li><li>1</li><li>2</li><li>3</li></ul>");
        Element thirdLi = doc.select("li").get(2); // index 2 -> from end position 2 -> even
        assertTrue(eval.matches(thirdLi, thirdLi));
    }

    @Test
    public void testIsNthLastChildMatches_notNthLastChild_returnsFalse() {
        Evaluator.IsNthLastChild eval = new Evaluator.IsNthLastChild(2, 0);
        Document doc = Jsoup.parse("<ul><li>0</li><li>1</li><li>2</li><li>3</li></ul>");
        Element secondLi = doc.select("li").get(1); // index 1 -> from end position 3 -> odd
        assertFalse(eval.matches(secondLi, secondLi));
    }

    // IsFirstOfType
    @Test
    public void testIsFirstOfTypeMatches_firstOfType_returnsTrue() {
        Evaluator.IsFirstOfType eval = new Evaluator.IsFirstOfType();
        Document doc = Jsoup.parse("<div><p>first</p><p>second</p></div>");
        Element firstP = doc.selectFirst("p");
        assertTrue(eval.matches(firstP, firstP));
    }

    @Test
    public void testIsFirstOfTypeMatches_notFirstOfType_returnsFalse() {
        Evaluator.IsFirstOfType eval = new Evaluator.IsFirstOfType();
        Document doc = Jsoup.parse("<div><p>first</p><p>second</p></div>");
        Element secondP = doc.select("p").get(1);
        assertFalse(eval.matches(secondP, secondP));
    }

    // IsLastOfType
    @Test
    public void testIsLastOfTypeMatches_lastOfType_returnsTrue() {
        Evaluator.IsLastOfType eval = new Evaluator.IsLastOfType();
        Document doc = Jsoup.parse("<div><p>first</p><p>second</p></div>");
        Element secondP = doc.select("p").get(1);
        assertTrue(eval.matches(secondP, secondP));
    }

    @Test
    public void testIsLastOfTypeMatches_notLastOfType_returnsFalse() {
        Evaluator.IsLastOfType eval = new Evaluator.IsLastOfType();
        Document doc = Jsoup.parse("<div><p>first</p><p>second</p></div>");
        Element firstP = doc.selectFirst("p");
        assertFalse(eval.matches(firstP, firstP));
    }

    // IsOnlyOfType
    @Test
    public void testIsOnlyOfTypeMatches_onlyOfType_returnsTrue() {
        Evaluator.IsOnlyOfType eval = new Evaluator.IsOnlyOfType();
        Document doc = Jsoup.parse("<div><p>only</p></div>");
        Element p = doc.selectFirst("p");
        assertTrue(eval.matches(p, p));
    }

    @Test
    public void testIsOnlyOfTypeMatches_notOnlyOfType_returnsFalse() {
        Evaluator.IsOnlyOfType eval = new Evaluator.IsOnlyOfType();
        Document doc = Jsoup.parse("<div><p>first</p><p>second</p></div>");
        Element firstP = doc.selectFirst("p");
        assertFalse(eval.matches(firstP, firstP));
    }

    // ContainsOwnText
    @Test
    public void testContainsOwnTextMatches_textExists_returnsTrue() {
        Evaluator.ContainsOwnText eval = new Evaluator.ContainsOwnText("hello");
        Element el = Jsoup.parse("<div>hello world <span>nested</span></div>").selectFirst("div");
        assertTrue(eval.matches(el, el));
    }

    @Test
    public void testContainsOwnTextMatches_textDoesNotExist_returnsFalse() {
        Evaluator.ContainsOwnText eval = new Evaluator.ContainsOwnText("nested");
        Element el = Jsoup.parse("<div>hello world <span>nested</span></div>").selectFirst("div");
        assertFalse(eval.matches(el, el));
    }

    // MatchesOwn
    @Test
    public void testMatchesOwnMatches_matchingPattern_returnsTrue() {
        Evaluator.MatchesOwn eval = new Evaluator.MatchesOwn(Pattern.compile("hello"));
        Element el = Jsoup.parse("<div>hello world</div>").selectFirst("div");
        assertTrue(eval.matches(el, el));
    }

    @Test
    public void testMatchesOwnMatches_nonMatchingPattern_returnsFalse() {
        Evaluator.MatchesOwn eval = new Evaluator.MatchesOwn(Pattern.compile("bye"));
        Element el = Jsoup.parse("<div>hello world</div>").selectFirst("div");
        assertFalse(eval.matches(el, el));
    }

    // Matches (global)
    @Test
    public void testMatchesMatches_matchingPattern_returnsTrue() {
        Evaluator.Matches eval = new Evaluator.Matches(Pattern.compile("hello"));
        Element el = Jsoup.parse("<div>hello <span>world</span></div>").selectFirst("div");
        assertTrue(eval.matches(el, el));
    }

    @Test
    public void testMatchesMatches_nonMatchingPattern_returnsFalse() {
        Evaluator.Matches eval = new Evaluator.Matches(Pattern.compile("bye"));
        Element el = Jsoup.parse("<div>hello world</div>").selectFirst("div");
        assertFalse(eval.matches(el, el));
    }

    // ContainsWholeText
    @Test
    public void testContainsWholeTextMatches_textExists_returnsTrue() {
        Evaluator.ContainsWholeText eval = new Evaluator.ContainsWholeText("hello world");
        Element el = Jsoup.parse("<div>hello world</div>").selectFirst("div");
        assertTrue(eval.matches(el, el));
    }

    @Test
    public void testContainsWholeTextMatches_textDoesNotExist_returnsFalse() {
        Evaluator.ContainsWholeText eval = new Evaluator.ContainsWholeText("bye");
        Element el = Jsoup.parse("<div>hello world</div>").selectFirst("div");
        assertFalse(eval.matches(el, el));
    }

    // MatchesWholeText
    @Test
    public void testMatchesWholeTextMatches_matchingPattern_returnsTrue() {
        Evaluator.MatchesWholeText eval = new Evaluator.MatchesWholeText(Pattern.compile("hello world"));
        Element el = Jsoup.parse("<div>hello world</div>").selectFirst("div");
        assertTrue(eval.matches(el, el));
    }

    @Test
    public void testMatchesWholeTextMatches_nonMatchingPattern_returnsFalse() {
        Evaluator.MatchesWholeText eval = new Evaluator.MatchesWholeText(Pattern.compile("bye"));
        Element el = Jsoup.parse("<div>hello world</div>").selectFirst("div");
        assertFalse(eval.matches(el, el));
    }

    // AttributeWithValue
    @Test
    public void testAttributeWithValueMatches_exactMatch_returnsTrue() {
        Evaluator.AttributeWithValue eval = new Evaluator.AttributeWithValue("data-val", "test");
        Element el = Jsoup.parse("<div data-val='test'></div>").selectFirst("div");
        assertTrue(eval.matches(el, el));
    }

    @Test
    public void testAttributeWithValueMatches_notExactMatch_returnsFalse() {
        Evaluator.AttributeWithValue eval = new Evaluator.AttributeWithValue("data-val", "other");
        Element el = Jsoup.parse("<div data-val='test'></div>").selectFirst("div");
        assertFalse(eval.matches(el, el));
    }

    // AttributeWithValueNot
    @Test
    public void testAttributeWithValueNotMatches_notEqual_returnsTrue() {
        Evaluator.AttributeWithValueNot eval = new Evaluator.AttributeWithValueNot("data-val", "test");
        Element el = Jsoup.parse("<div data-val='other'></div>").selectFirst("div");
        assertTrue(eval.matches(el, el));
    }

    @Test
    public void testAttributeWithValueNotMatches_equal_returnsFalse() {
        Evaluator.AttributeWithValueNot eval = new Evaluator.AttributeWithValueNot("data-val", "test");
        Element el = Jsoup.parse("<div data-val='test'></div>").selectFirst("div");
        assertFalse(eval.matches(el, el));
    }

    // AttributeWithValueStarting
    @Test
    public void testAttributeWithValueStartingMatches_startsWith_returnsTrue() {
        Evaluator.AttributeWithValueStarting eval = new Evaluator.AttributeWithValueStarting("data-val", "pre");
        Element el = Jsoup.parse("<div data-val='prefix-value'></div>").selectFirst("div");
        assertTrue(eval.matches(el, el));
    }

    @Test
    public void testAttributeWithValueStartingMatches_notStartsWith_returnsFalse() {
        Evaluator.AttributeWithValueStarting eval = new Evaluator.AttributeWithValueStarting("data-val", "other");
        Element el = Jsoup.parse("<div data-val='prefix-value'></div>").selectFirst("div");
        assertFalse(eval.matches(el, el));
    }

    // AttributeWithValueEnding
    @Test
    public void testAttributeWithValueEndingMatches_endsWith_returnsTrue() {
        Evaluator.AttributeWithValueEnding eval = new Evaluator.AttributeWithValueEnding("data-val", "suffix");
        Element el = Jsoup.parse("<div data-val='value-suffix'></div>").selectFirst("div");
        assertTrue(eval.matches(el, el));
    }

    @Test
    public void testAttributeWithValueEndingMatches_notEndsWith_returnsFalse() {
        Evaluator.AttributeWithValueEnding eval = new Evaluator.AttributeWithValueEnding("data-val", "other");
        Element el = Jsoup.parse("<div data-val='value-suffix'></div>").selectFirst("div");
        assertFalse(eval.matches(el, el));
    }

    // AttributeWithValueContaining
    @Test
    public void testAttributeWithValueContainingMatches_contains_returnsTrue() {
        Evaluator.AttributeWithValueContaining eval = new Evaluator.AttributeWithValueContaining("data-val", "mid");
        Element el = Jsoup.parse("<div data-val='prefix-mid-suffix'></div>").selectFirst("div");
        assertTrue(eval.matches(el, el));
    }

    @Test
    public void testAttributeWithValueContainingMatches_notContains_returnsFalse() {
        Evaluator.AttributeWithValueContaining eval = new Evaluator.AttributeWithValueContaining("data-val", "other");
        Element el = Jsoup.parse("<div data-val='prefix-mid-suffix'></div>").selectFirst("div");
        assertFalse(eval.matches(el, el));
    }

    // And evaluator
    @Test
    public void testAndMatches_bothConditionsTrue_returnsTrue() {
        Evaluator.Tag tagEval = new Evaluator.Tag("div");
        Evaluator.Id idEval = new Evaluator.Id("test");
        Evaluator.And andEval = new Evaluator.And(tagEval, idEval);
        Element el = Jsoup.parse("<div id='test'></div>").selectFirst("div");
        assertTrue(andEval.matches(el, el));
    }

    @Test
    public void testAndMatches_oneConditionFalse_returnsFalse() {
        Evaluator.Tag tagEval = new Evaluator.Tag("div");
        Evaluator.Id idEval = new Evaluator.Id("other");
        Evaluator.And andEval = new Evaluator.And(tagEval, idEval);
        Element el = Jsoup.parse("<div id='test'></div>").selectFirst("div");
        assertFalse(andEval.matches(el, el));
    }

    // Or evaluator
    @Test
    public void testOrMatches_oneConditionTrue_returnsTrue() {
        Evaluator.Tag tagEval = new Evaluator.Tag("span");
        Evaluator.Id idEval = new Evaluator.Id("test");
        Evaluator.Or orEval = new Evaluator.Or(tagEval, idEval);
        Element el = Jsoup.parse("<div id='test'></div>").selectFirst("div");
        assertTrue(orEval.matches(el, el));
    }

    @Test
    public void testOrMatches_bothFalse_returnsFalse() {
        Evaluator.Tag tagEval = new Evaluator.Tag("span");
        Evaluator.Id idEval = new Evaluator.Id("other");
        Evaluator.Or orEval = new Evaluator.Or(tagEval, idEval);
        Element el = Jsoup.parse("<div id='test'></div>").selectFirst("div");
        assertFalse(orEval.matches(el, el));
    }

    // Not evaluator
    @Test
    public void testNotMatches_innerConditionFalse_returnsTrue() {
        Evaluator.Tag tagEval = new Evaluator.Tag("span");
        Evaluator.Not notEval = new Evaluator.Not(tagEval);
        Element el = Jsoup.parse("<div></div>").selectFirst("div");
        assertTrue(notEval.matches(el, el));
    }

    @Test
    public void testNotMatches_innerConditionTrue_returnsFalse() {
        Evaluator.Tag tagEval = new Evaluator.Tag("div");
        Evaluator.Not notEval = new Evaluator.Not(tagEval);
        Element el = Jsoup.parse("<div></div>").selectFirst("div");
        assertFalse(notEval.matches(el, el));
    }

    // Has evaluator
    @Test
    public void testHasMatches_hasDescendant_returnsTrue() {
        Evaluator.Tag innerEval = new Evaluator.Tag("p");
        Evaluator.Has hasEval = new Evaluator.Has(innerEval);
        Element el = Jsoup.parse("<div><p>text</p></div>").selectFirst("div");
        assertTrue(hasEval.matches(el, el));
    }

    @Test
    public void testHasMatches_noDescendant_returnsFalse() {
        Evaluator.Tag innerEval = new Evaluator.Tag("span");
        Evaluator.Has hasEval = new Evaluator.Has(innerEval);
        Element el = Jsoup.parse("<div><p>text</p></div>").selectFirst("div");
        assertFalse(hasEval.matches(el, el));
    }
}