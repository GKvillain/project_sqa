package org.jsoup.select;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Before;
import org.junit.Test;

import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class EvaluatorTest {

    private Document doc;
    private Element body;

    @Before
    public void setUp() {
        String html = "<html><head><title>Test</title></head>"
                + "<body>"
                + "<div id='div1' class='main highlight' data-key='value123' title='hello world'><span>Text 1</span><p>Para 1</p></div>"
                + "<div id='div2' class='sub' data-name='test' custom-attr='xyz'><p class='inner'>Para 2</p><span>Text 2</span></div>"
                + "<div id='emptyDiv'><!-- comment --></div>"
                + "<ul id='list'><li>Item 1</li><li>Item 2</li><li>Item 3</li></ul>"
                + "<script>var x = 'script_data';</script>"
                + "</body></html>";
        doc = Jsoup.parse(html);
        body = doc.body();
    }

    // Tests tag name evaluator matching and toString
    @Test
    public void testTag_matchingElement_returnsTrue() {
        Evaluator.Tag eval = new Evaluator.Tag("div");
        Element div1 = doc.getElementById("div1");
        Element span = div1.select("span").first();

        assertTrue(eval.matches(body, div1));
        assertFalse(eval.matches(body, span));
        assertEquals("div", eval.toString());
    }

    // Tests tag ends with evaluator
    @Test
    public void testTagEndsWith_matchingSuffix_returnsTrue() {
        Evaluator.TagEndsWith eval = new Evaluator.TagEndsWith("iv");
        Element div1 = doc.getElementById("div1");
        Element span = div1.select("span").first();

        assertTrue(eval.matches(body, div1));
        assertFalse(eval.matches(body, span));
        assertEquals("iv", eval.toString());
    }

    // Tests ID and Class evaluators
    @Test
    public void testIdAndClass_validElements_matchCorrectly() {
        Evaluator.Id idEval = new Evaluator.Id("div1");
        Evaluator.Class classEval = new Evaluator.Class("highlight");
        Element div1 = doc.getElementById("div1");
        Element div2 = doc.getElementById("div2");

        assertTrue(idEval.matches(body, div1));
        assertFalse(idEval.matches(body, div2));
        assertEquals("#div1", idEval.toString());

        assertTrue(classEval.matches(body, div1));
        assertFalse(classEval.matches(body, div2));
        assertEquals(".highlight", classEval.toString());
    }

    // Tests attribute name existence and attribute starting with prefix
    @Test
    public void testAttributeAndAttributeStarting_presentAttributes_matchCorrectly() {
        Evaluator.Attribute attrEval = new Evaluator.Attribute("custom-attr");
        Evaluator.AttributeStarting attrStartEval = new Evaluator.AttributeStarting("data-");
        Element div1 = doc.getElementById("div1");
        Element div2 = doc.getElementById("div2");

        assertFalse(attrEval.matches(body, div1));
        assertTrue(attrEval.matches(body, div2));
        assertEquals("[custom-attr]", attrEval.toString());

        assertTrue(attrStartEval.matches(body, div1));
        assertTrue(attrStartEval.matches(body, div2));
        assertFalse(attrStartEval.matches(body, doc.getElementById("emptyDiv")));
        assertEquals("[^data-]", attrStartEval.toString());
    }

    // Tests attribute value comparisons (exact, not, prefix, suffix, contains)
    @Test
    public void testAttributeValueVariants_validValues_matchCorrectly() {
        Element div1 = doc.getElementById("div1");

        Evaluator.AttributeWithValue eqEval = new Evaluator.AttributeWithValue("data-key", "value123");
        assertTrue(eqEval.matches(body, div1));
        assertEquals("[data-key=value123]", eqEval.toString());

        Evaluator.AttributeWithValueNot notEval = new Evaluator.AttributeWithValueNot("data-key", "wrong");
        assertTrue(notEval.matches(body, div1));
        assertEquals("[data-key!=wrong]", notEval.toString());

        Evaluator.AttributeWithValueStarting startEval = new Evaluator.AttributeWithValueStarting("data-key", "val");
        assertTrue(startEval.matches(body, div1));
        assertEquals("[data-key^=val]", startEval.toString());

        Evaluator.AttributeWithValueEnding endEval = new Evaluator.AttributeWithValueEnding("data-key", "123");
        assertTrue(endEval.matches(body, div1));
        assertEquals("[data-key$=123]", endEval.toString());

        Evaluator.AttributeWithValueContaining contEval = new Evaluator.AttributeWithValueContaining("data-key", "lue");
        assertTrue(contEval.matches(body, div1));
        assertEquals("[data-key*=lue]", contEval.toString());
    }

    // Tests attribute value matching by regex pattern
    @Test
    public void testAttributeWithValueMatching_regexPattern_matchesCorrectly() {
        Pattern pattern = Pattern.compile("^val.*23$");
        Evaluator.AttributeWithValueMatching eval = new Evaluator.AttributeWithValueMatching("data-key", pattern);
        Element div1 = doc.getElementById("div1");
        Element div2 = doc.getElementById("div2");

        assertTrue(eval.matches(body, div1));
        assertFalse(eval.matches(body, div2));
        assertEquals("[data-key~=^val.*23$]", eval.toString());
    }

    // Tests all elements evaluator
    @Test
    public void testAllElements_anyElement_returnsTrue() {
        Evaluator.AllElements eval = new Evaluator.AllElements();
        assertTrue(eval.matches(body, doc.getElementById("div1")));
        assertTrue(eval.matches(body, body));
        assertEquals("*", eval.toString());
    }

    // Tests index based evaluators: lt, gt, eq
    @Test
    public void testIndexEvaluators_siblingPositions_matchExpected() {
        Element list = doc.getElementById("list");
        Element li0 = list.child(0);
        Element li1 = list.child(1);
        Element li2 = list.child(2);

        Evaluator.IndexLessThan ltEval = new Evaluator.IndexLessThan(1);
        assertTrue(ltEval.matches(list, li0));
        assertFalse(ltEval.matches(list, li1));
        assertEquals(":lt(1)", ltEval.toString());

        Evaluator.IndexGreaterThan gtEval = new Evaluator.IndexGreaterThan(1);
        assertFalse(gtEval.matches(list, li1));
        assertTrue(gtEval.matches(list, li2));
        assertEquals(":gt(1)", gtEval.toString());

        Evaluator.IndexEquals eqEval = new Evaluator.IndexEquals(1);
        assertFalse(eqEval.matches(list, li0));
        assertTrue(eqEval.matches(list, li1));
        assertEquals(":eq(1)", eqEval.toString());
    }

    // Tests structural pseudo evaluators: first-child, last-child, only-child, root
    @Test
    public void testStructuralPseudoClasses_treeNodes_matchExpected() {
        Element list = doc.getElementById("list");
        Element li0 = list.child(0);
        Element li2 = list.child(2);

        Evaluator.IsFirstChild firstChildEval = new Evaluator.IsFirstChild();
        assertTrue(firstChildEval.matches(list, li0));
        assertFalse(firstChildEval.matches(list, li2));
        assertEquals(":first-child", firstChildEval.toString());

        Evaluator.IsLastChild lastChildEval = new Evaluator.IsLastChild();
        assertFalse(lastChildEval.matches(list, li0));
        assertTrue(lastChildEval.matches(list, li2));
        assertEquals(":last-child", lastChildEval.toString());

        Evaluator.IsOnlyChild onlyChildEval = new Evaluator.IsOnlyChild();
        Element innerP = doc.select(".inner").first();
        assertFalse(onlyChildEval.matches(body, li0));
        assertFalse(onlyChildEval.matches(body, innerP));
        assertEquals(":only-child", onlyChildEval.toString());

        Evaluator.IsRoot rootEval = new Evaluator.IsRoot();
        assertTrue(rootEval.matches(doc, doc.child(0)));
        assertFalse(rootEval.matches(doc, body));
        assertEquals(":root", rootEval.toString());
    }

    // Tests nth child pseudo class with formulas
    @Test
    public void testIsNthChild_formulaVariants_matchExpected() {
        Element list = doc.getElementById("list");
        Element li0 = list.child(0);
        Element li1 = list.child(1);
        Element li2 = list.child(2);

        Evaluator.IsNthChild nthChildExact = new Evaluator.IsNthChild(0, 2);
        assertFalse(nthChildExact.matches(list, li0));
        assertTrue(nthChildExact.matches(list, li1));
        assertFalse(nthChildExact.matches(list, li2));
        assertEquals(":nth-child(2)", nthChildExact.toString());

        Evaluator.IsNthChild nthChildOdd = new Evaluator.IsNthChild(2, 1);
        assertTrue(nthChildOdd.matches(list, li0));
        assertFalse(nthChildOdd.matches(list, li1));
        assertTrue(nthChildOdd.matches(list, li2));
        assertEquals(":nth-child(2n+1)", nthChildOdd.toString());

        Evaluator.IsNthLastChild nthLastChild = new Evaluator.IsNthLastChild(0, 1);
        assertFalse(nthLastChild.matches(list, li0));
        assertTrue(nthLastChild.matches(list, li2));
        assertEquals(":nth-last-child(1)", nthLastChild.toString());
    }

    // Tests type-based pseudo classes: first-of-type, last-of-type, only-of-type, nth-of-type
    @Test
    public void testOfTypeEvaluators_tagTypes_matchExpected() {
        Element div1 = doc.getElementById("div1");
        Element spanInDiv1 = div1.select("span").first();
        Element pInDiv1 = div1.select("p").first();

        Evaluator.IsFirstOfType firstOfType = new Evaluator.IsFirstOfType();
        assertTrue(firstOfType.matches(div1, spanInDiv1));
        assertTrue(firstOfType.matches(div1, pInDiv1));
        assertEquals(":first-of-type", firstOfType.toString());

        Evaluator.IsLastOfType lastOfType = new Evaluator.IsLastOfType();
        assertTrue(lastOfType.matches(div1, spanInDiv1));
        assertTrue(lastOfType.matches(div1, pInDiv1));
        assertEquals(":last-of-type", lastOfType.toString());

        Evaluator.IsOnlyOfType onlyOfType = new Evaluator.IsOnlyOfType();
        assertTrue(onlyOfType.matches(div1, spanInDiv1));
        assertEquals(":only-of-type", onlyOfType.toString());
    }

    // Tests empty element evaluator
    @Test
    public void testIsEmpty_elementsWithAndWithoutChildren_matchCorrectly() {
        Evaluator.IsEmpty emptyEval = new Evaluator.IsEmpty();
        Element emptyDiv = doc.getElementById("emptyDiv");
        Element div1 = doc.getElementById("div1");

        assertTrue(emptyEval.matches(body, emptyDiv));
        assertFalse(emptyEval.matches(body, div1));
        assertEquals(":empty", emptyEval.toString());
    }

    // Tests text evaluators: containsText, containsOwnText, containsData
    @Test
    public void testTextEvaluators_textContent_matchCorrectly() {
        Element div1 = doc.getElementById("div1");
        Element script = doc.select("script").first();

        Evaluator.ContainsText containsText = new Evaluator.ContainsText("Text 1");
        assertTrue(containsText.matches(body, div1));
        assertFalse(containsText.matches(body, doc.getElementById("div2")));
        assertEquals(":contains(text 1)", containsText.toString());

        Evaluator.ContainsOwnText containsOwnText = new Evaluator.ContainsOwnText("Text 1");
        assertFalse(containsOwnText.matches(body, div1));
        assertTrue(containsOwnText.matches(body, div1.select("span").first()));
        assertEquals(":containsOwn(text 1)", containsOwnText.toString());

        Evaluator.ContainsData containsData = new Evaluator.ContainsData("script_data");
        assertTrue(containsData.matches(body, script));
        assertFalse(containsData.matches(body, div1));
        assertEquals(":containsData(script_data)", containsData.toString());
    }

    // Tests regex matching evaluators: matches and matchesOwn
    @Test
    public void testRegexEvaluators_patternMatching_matchCorrectly() {
        Pattern p = Pattern.compile("Para \\d");
        Evaluator.Matches matches = new Evaluator.Matches(p);
        Evaluator.MatchesOwn matchesOwn = new Evaluator.MatchesOwn(p);

        Element div1 = doc.getElementById("div1");
        Element p1 = div1.select("p").first();

        assertTrue(matches.matches(body, div1));
        assertTrue(matches.matches(body, p1));
        assertEquals(":matches(" + p.toString() + ")", matches.toString());

        assertFalse(matchesOwn.matches(body, div1));
        assertTrue(matchesOwn.matches(body, p1));
        assertEquals(":matchesOwn(" + p.toString() + ")", matchesOwn.toString());
    }

    // Tests validation exception on empty attribute starting prefix
    @Test(expected = IllegalArgumentException.class)
    public void testAttributeStarting_emptyPrefix_throwsException() {
        new Evaluator.AttributeStarting("");
    }

    // Tests validation exception on empty attribute key in AttributeKeyPair
    @Test(expected = IllegalArgumentException.class)
    public void testAttributeKeyPair_emptyKey_throwsException() {
        new Evaluator.AttributeWithValue("", "value");
    }
}