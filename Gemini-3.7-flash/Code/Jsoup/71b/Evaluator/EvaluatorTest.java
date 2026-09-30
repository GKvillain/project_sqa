package org.jsoup.select;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Before;
import org.junit.Test;

import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EvaluatorTest {

    private Document doc;

    @Before
    public void setUp() {
        String html = "<div id='root'>"
                + "<div id='d1' class='test one' title='heading' data-val='xyz' custom-attr='abc'>"
                + "<p id='p1' class='text'>First paragraph</p>"
                + "<p id='p2' class='text special'>Second paragraph with <b>bold</b> text</p>"
                + "<span id='s1'>Span text</span>"
                + "<div id='empty-div'><!-- comment --></div>"
                + "</div>"
                + "</div>";
        doc = Jsoup.parse(html);
    }

    // Tests Tag evaluator matching and toString
    @Test
    public void testTag_matchesAndToString_returnsExpected() {
        Evaluator.Tag eval = new Evaluator.Tag("p");
        Element p1 = doc.getElementById("p1");
        Element s1 = doc.getElementById("s1");

        assertTrue(eval.matches(doc, p1));
        assertFalse(eval.matches(doc, s1));
        assertEquals("p", eval.toString());
    }

    // Tests TagEndsWith evaluator
    @Test
    public void testTagEndsWith_matchingTagSuffix_returnsTrue() {
        Evaluator.TagEndsWith eval = new Evaluator.TagEndsWith("v");
        Element d1 = doc.getElementById("d1");
        Element p1 = doc.getElementById("p1");

        assertTrue(eval.matches(doc, d1));
        assertFalse(eval.matches(doc, p1));
        assertEquals("v", eval.toString());
    }

    // Tests Id evaluator matching and toString
    @Test
    public void testId_matchesAndToString_returnsExpected() {
        Evaluator.Id eval = new Evaluator.Id("d1");
        Element d1 = doc.getElementById("d1");
        Element p1 = doc.getElementById("p1");

        assertTrue(eval.matches(doc, d1));
        assertFalse(eval.matches(doc, p1));
        assertEquals("#d1", eval.toString());
    }

    // Tests Class evaluator matching and toString
    @Test
    public void testClass_matchesAndToString_returnsExpected() {
        Evaluator.Class eval = new Evaluator.Class("special");
        Element p1 = doc.getElementById("p1");
        Element p2 = doc.getElementById("p2");

        assertFalse(eval.matches(doc, p1));
        assertTrue(eval.matches(doc, p2));
        assertEquals(".special", eval.toString());
    }

    // Tests Attribute evaluator
    @Test
    public void testAttribute_hasAttribute_returnsTrue() {
        Evaluator.Attribute eval = new Evaluator.Attribute("title");
        Element d1 = doc.getElementById("d1");
        Element p1 = doc.getElementById("p1");

        assertTrue(eval.matches(doc, d1));
        assertFalse(eval.matches(doc, p1));
        assertEquals("[title]", eval.toString());
    }

    // Tests AttributeStarting evaluator with prefix
    @Test
    public void testAttributeStarting_prefixMatch_returnsTrue() {
        Evaluator.AttributeStarting eval = new Evaluator.AttributeStarting("data-");
        Element d1 = doc.getElementById("d1");
        Element p1 = doc.getElementById("p1");

        assertTrue(eval.matches(doc, d1));
        assertFalse(eval.matches(doc, p1));
        assertEquals("[^data-]", eval.toString());
    }

    // Tests AttributeWithValue matching quoted and unquoted values
    @Test
    public void testAttributeWithValue_matchingValue_returnsExpected() {
        Evaluator.AttributeWithValue eval = new Evaluator.AttributeWithValue("title", "\"heading\"");
        Element d1 = doc.getElementById("d1");
        Element p1 = doc.getElementById("p1");

        assertTrue(eval.matches(doc, d1));
        assertFalse(eval.matches(doc, p1));
        assertEquals("[title=heading]", eval.toString());
    }

    // Tests AttributeWithValueNot, Starting, Ending, and Containing
    @Test
    public void testAttributeValueVariations_variousMatchings_returnsCorrectBoolean() {
        Element d1 = doc.getElementById("d1");

        Evaluator.AttributeWithValueNot notEval = new Evaluator.AttributeWithValueNot("title", "other");
        assertTrue(notEval.matches(doc, d1));
        assertEquals("[title!=other]", notEval.toString());

        Evaluator.AttributeWithValueStarting startEval = new Evaluator.AttributeWithValueStarting("title", "head");
        assertTrue(startEval.matches(doc, d1));
        assertEquals("[title^=head]", startEval.toString());

        Evaluator.AttributeWithValueEnding endEval = new Evaluator.AttributeWithValueEnding("title", "ing");
        assertTrue(endEval.matches(doc, d1));
        assertEquals("[title$=ing]", endEval.toString());

        Evaluator.AttributeWithValueContaining containEval = new Evaluator.AttributeWithValueContaining("title", "ead");
        assertTrue(containEval.matches(doc, d1));
        assertEquals("[title*=ead]", containEval.toString());
    }

    // Tests AttributeWithValueMatching using regex
    @Test
    public void testAttributeWithValueMatching_regexPattern_matchesCorrectly() {
        Evaluator.AttributeWithValueMatching eval = new Evaluator.AttributeWithValueMatching("title", Pattern.compile("^head.*"));
        Element d1 = doc.getElementById("d1");

        assertTrue(eval.matches(doc, d1));
        assertEquals("[title~=^head.*]", eval.toString());
    }

    // Tests AllElements evaluator
    @Test
    public void testAllElements_matchesAlways_returnsTrue() {
        Evaluator.AllElements eval = new Evaluator.AllElements();
        Element d1 = doc.getElementById("d1");

        assertTrue(eval.matches(doc, d1));
        assertEquals("*", eval.toString());
    }

    // Tests IndexLessThan, IndexGreaterThan, and IndexEquals evaluators
    @Test
    public void testIndexEvaluators_siblingPositions_returnsExpected() {
        Element p1 = doc.getElementById("p1"); // index 0
        Element p2 = doc.getElementById("p2"); // index 1

        Evaluator.IndexLessThan lt = new Evaluator.IndexLessThan(1);
        assertTrue(lt.matches(doc, p1));
        assertFalse(lt.matches(doc, p2));
        assertFalse(lt.matches(p1, p1)); // root == element returns false
        assertEquals(":lt(1)", lt.toString());

        Evaluator.IndexGreaterThan gt = new Evaluator.IndexGreaterThan(0);
        assertFalse(gt.matches(doc, p1));
        assertTrue(gt.matches(doc, p2));
        assertEquals(":gt(0)", gt.toString());

        Evaluator.IndexEquals eq = new Evaluator.IndexEquals(1);
        assertFalse(eq.matches(doc, p1));
        assertTrue(eq.matches(doc, p2));
        assertEquals(":eq(1)", eq.toString());
    }

    // Tests structural pseudo-classes: IsFirstChild, IsLastChild, IsOnlyChild
    @Test
    public void testStructuralChildEvaluators_positions_evaluatesCorrectly() {
        Element p1 = doc.getElementById("p1");
        Element emptyDiv = doc.getElementById("empty-div");
        Element d1 = doc.getElementById("d1");

        Evaluator.IsFirstChild firstChild = new Evaluator.IsFirstChild();
        assertTrue(firstChild.matches(doc, p1));
        assertFalse(firstChild.matches(doc, emptyDiv));
        assertEquals(":first-child", firstChild.toString());

        Evaluator.IsLastChild lastChild = new Evaluator.IsLastChild();
        assertFalse(lastChild.matches(doc, p1));
        assertTrue(lastChild.matches(doc, emptyDiv));
        assertEquals(":last-child", lastChild.toString());

        Evaluator.IsOnlyChild onlyChild = new Evaluator.IsOnlyChild();
        assertFalse(onlyChild.matches(doc, p1));
        assertTrue(onlyChild.matches(doc.getElementById("root"), d1));
        assertEquals(":only-child", onlyChild.toString());
    }

    // Tests IsRoot evaluator with Document and Element roots
    @Test
    public void testIsRoot_matchingRoot_returnsExpected() {
        Evaluator.IsRoot rootEval = new Evaluator.IsRoot();
        Element htmlTag = doc.child(0);
        Element d1 = doc.getElementById("d1");

        assertTrue(rootEval.matches(doc, htmlTag));
        assertFalse(rootEval.matches(doc, d1));
        assertTrue(rootEval.matches(d1, d1));
        assertEquals(":root", rootEval.toString());
    }

    // Tests Nth-child and Nth-last-child evaluators including toString formats
    @Test
    public void testNthChildEvaluators_nthPositions_matchesCorrectly() {
        Element p1 = doc.getElementById("p1"); // pos 1
        Element p2 = doc.getElementById("p2"); // pos 2

        Evaluator.IsNthChild nthChild1 = new Evaluator.IsNthChild(0, 1);
        assertTrue(nthChild1.matches(doc, p1));
        assertFalse(nthChild1.matches(doc, p2));
        assertEquals(":nth-child(1)", nthChild1.toString());

        Evaluator.IsNthChild nthChildFormula = new Evaluator.IsNthChild(2, 1);
        assertTrue(nthChildFormula.matches(doc, p1));
        assertFalse(nthChildFormula.matches(doc, p2));
        assertEquals(":nth-child(2n+1)", nthChildFormula.toString());

        Evaluator.IsNthLastChild nthLastChild = new Evaluator.IsNthLastChild(0, 4);
        assertTrue(nthLastChild.matches(doc, p1));
        assertEquals(":nth-last-child(4)", nthLastChild.toString());
    }

    // Tests IsNthOfType, IsNthLastOfType, IsFirstOfType, IsLastOfType, and IsOnlyOfType
    @Test
    public void testNthOfTypeEvaluators_typePositions_evaluatesCorrectly() {
        Element p1 = doc.getElementById("p1"); // first p
        Element p2 = doc.getElementById("p2"); // second p
        Element s1 = doc.getElementById("s1"); // only span

        Evaluator.IsFirstOfType firstOfType = new Evaluator.IsFirstOfType();
        assertTrue(firstOfType.matches(doc, p1));
        assertFalse(firstOfType.matches(doc, p2));
        assertEquals(":first-of-type", firstOfType.toString());

        Evaluator.IsLastOfType lastOfType = new Evaluator.IsLastOfType();
        assertFalse(lastOfType.matches(doc, p1));
        assertTrue(lastOfType.matches(doc, p2));
        assertEquals(":last-of-type", lastOfType.toString());

        Evaluator.IsOnlyOfType onlyOfType = new Evaluator.IsOnlyOfType();
        assertFalse(onlyOfType.matches(doc, p1));
        assertTrue(onlyOfType.matches(doc, s1));
        assertEquals(":only-of-type", onlyOfType.toString());
    }

    // Tests IsEmpty evaluator
    @Test
    public void testIsEmpty_emptyAndNonEmptyElements_returnsExpected() {
        Evaluator.IsEmpty emptyEval = new Evaluator.IsEmpty();
        Element p1 = doc.getElementById("p1");
        Element emptyDiv = doc.getElementById("empty-div");

        assertFalse(emptyEval.matches(doc, p1));
        assertTrue(emptyEval.matches(doc, emptyDiv));
        assertEquals(":empty", emptyEval.toString());
    }

    // Tests text matching evaluators: ContainsText, ContainsOwnText, and ContainsData
    @Test
    public void testTextEvaluators_textContent_matchesProperly() {
        Element p2 = doc.getElementById("p2");

        Evaluator.ContainsText containsText = new Evaluator.ContainsText("paragraph");
        assertTrue(containsText.matches(doc, p2));
        assertEquals(":contains(paragraph)", containsText.toString());

        Evaluator.ContainsOwnText containsOwn = new Evaluator.ContainsOwnText("second paragraph");
        assertTrue(containsOwn.matches(doc, p2));
        assertFalse(new Evaluator.ContainsOwnText("bold").matches(doc, p2));
        assertEquals(":containsOwn(second paragraph)", containsOwn.toString());

        Document scriptDoc = Jsoup.parse("<script>var x = 10;</script>");
        Element script = scriptDoc.selectFirst("script");
        Evaluator.ContainsData containsData = new Evaluator.ContainsData("var x");
        assertTrue(containsData.matches(scriptDoc, script));
        assertEquals(":containsData(var x)", containsData.toString());
    }

    // Tests regex text matching evaluators: Matches and MatchesOwn
    @Test
    public void testRegexMatchesEvaluators_regexOnText_evaluatesCorrectly() {
        Element p2 = doc.getElementById("p2");

        Evaluator.Matches matches = new Evaluator.Matches(Pattern.compile("Second.*text"));
        assertTrue(matches.matches(doc, p2));
        assertEquals(":matches(Second.*text)", matches.toString());

        Evaluator.MatchesOwn matchesOwn = new Evaluator.MatchesOwn(Pattern.compile("Second.*with"));
        assertTrue(matchesOwn.matches(doc, p2));
        assertFalse(new Evaluator.MatchesOwn(Pattern.compile("^bold$")).matches(doc, p2));
        assertEquals(":matchesOwn(Second.*with)", matchesOwn.toString());
    }

    // Tests validation exception when key or value is empty
    @Test(expected = IllegalArgumentException.class)
    public void testAttributeStarting_emptyPrefix_throwsException() {
        new Evaluator.AttributeStarting("");
    }

    // Tests nth evaluators formula representations (b=0, b<0, IsNthOfType, IsNthLastOfType)
    @Test
    public void testCssNthEvaluator_formulaToStringVariants_formatsCorrectly() {
        Evaluator.IsNthChild nthChildBZero = new Evaluator.IsNthChild(2, 0);
        assertEquals(":nth-child(2n)", nthChildBZero.toString());

        Evaluator.IsNthChild nthChildBNegative = new Evaluator.IsNthChild(2, -1);
        assertEquals(":nth-child(2n-1)", nthChildBNegative.toString());

        Evaluator.IsNthOfType nthOfTypeFormula = new Evaluator.IsNthOfType(2, 1);
        assertEquals(":nth-of-type(2n+1)", nthOfTypeFormula.toString());

        Evaluator.IsNthOfType nthOfTypeSingle = new Evaluator.IsNthOfType(0, 2);
        assertEquals(":nth-of-type(2)", nthOfTypeSingle.toString());

        Evaluator.IsNthLastOfType nthLastOfTypeFormula = new Evaluator.IsNthLastOfType(2, 1);
        assertEquals(":nth-last-of-type(2n+1)", nthLastOfTypeFormula.toString());

        Evaluator.IsNthLastOfType nthLastOfTypeSingle = new Evaluator.IsNthLastOfType(0, 1);
        assertEquals(":nth-last-of-type(1)", nthLastOfTypeSingle.toString());

        Element p1 = doc.getElementById("p1");
        Element p2 = doc.getElementById("p2");
        assertTrue(nthOfTypeSingle.matches(doc, p2));
        assertFalse(nthOfTypeSingle.matches(doc, p1));
        assertTrue(nthLastOfTypeSingle.matches(doc, p2));
        assertFalse(nthLastOfTypeSingle.matches(doc, p1));
    }

    // Tests structural evaluators on orphaned elements with no parent
    @Test
    public void testStructuralEvaluators_orphanElement_returnsFalse() {
        Element orphan = new Element("p");

        assertFalse(new Evaluator.IsFirstChild().matches(doc, orphan));
        assertFalse(new Evaluator.IsLastChild().matches(doc, orphan));
        assertFalse(new Evaluator.IsOnlyChild().matches(doc, orphan));
        assertFalse(new Evaluator.IsFirstOfType().matches(doc, orphan));
        assertFalse(new Evaluator.IsLastOfType().matches(doc, orphan));
        assertFalse(new Evaluator.IsOnlyOfType().matches(doc, orphan));
        assertFalse(new Evaluator.IsNthChild(0, 1).matches(doc, orphan));
        assertFalse(new Evaluator.IsNthOfType(0, 1).matches(doc, orphan));
    }

    // Tests AttributeWithValue with single quotes and AttributeWithValueNot when attribute is missing
    @Test
    public void testAttributeEvaluators_additionalCases_returnsExpected() {
        Evaluator.AttributeWithValue singleQuoted = new Evaluator.AttributeWithValue("title", "'heading'");
        Element d1 = doc.getElementById("d1");
        Element p1 = doc.getElementById("p1");

        assertTrue(singleQuoted.matches(doc, d1));

        Evaluator.AttributeWithValueNot notEval = new Evaluator.AttributeWithValueNot("title", "heading");
        assertFalse(notEval.matches(doc, d1)); // has attribute with matching value -> false
        assertTrue(notEval.matches(doc, p1));  // does not have attribute -> true
    }

    // Tests IsEmpty with various node types like blank text nodes, non-blank text nodes, and whitespace
    @Test
    public void testIsEmpty_variousChildNodes_evaluatesCorrectly() {
        Document parsed = Jsoup.parse("<div><span id='blank-text'>   </span><span id='non-blank'> a </span></div>");
        Element blankTextSpan = parsed.getElementById("blank-text");
        Element nonBlankSpan = parsed.getElementById("non-blank");

        assertTrue(new Evaluator.IsEmpty().matches(parsed, blankTextSpan));
        assertFalse(new Evaluator.IsEmpty().matches(parsed, nonBlankSpan));
    }
}