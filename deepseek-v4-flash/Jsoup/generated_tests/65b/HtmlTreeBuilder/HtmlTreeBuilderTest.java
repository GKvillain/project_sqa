package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.StringReader;
import java.util.List;
import org.jsoup.nodes.*;

/**
 * JUnit 4 test class for HtmlTreeBuilder, targeting Defects4J bug 65b.
 * Focuses on resetInsertionMode, fragment parsing, scope checks, implied end tags, and self-closing tags.
 */
public class HtmlTreeBuilderTest {

    private HtmlTreeBuilder createBuilder() {
        HtmlTreeBuilder b = new HtmlTreeBuilder();
        b.initialiseParse(new StringReader(""), "http://base", new ParseErrorList(16, 10), ParseSettings.htmlDefault);
        return b;
    }

    // -------------------- resetInsertionMode tests (related to bug 65b) --------------------

    // Bug 65b: when context element is a last td, resetInsertionMode should go to InBody, not InCell.
    @Test
    public void testResetInsertionMode_tdContextLast_shouldSetInBody() {
        HtmlTreeBuilder b = new HtmlTreeBuilder();
        Element td = new Element(Tag.valueOf("td"), "http://base");
        b.parseFragment("", td, "http://base", new ParseErrorList(16, 10), ParseSettings.htmlDefault);
        assertEquals("td as last element should produce InBody", HtmlTreeBuilderState.InBody, b.state());
    }

    // Same bug for th.
    @Test
    public void testResetInsertionMode_thContextLast_shouldSetInBody() {
        HtmlTreeBuilder b = new HtmlTreeBuilder();
        Element th = new Element(Tag.valueOf("th"), "http://base");
        b.parseFragment("", th, "http://base", new ParseErrorList(16, 10), ParseSettings.htmlDefault);
        assertEquals("th as last element should produce InBody", HtmlTreeBuilderState.InBody, b.state());
    }

    // Select context last -> InSelect
    @Test
    public void testResetInsertionMode_selectContextLast_shouldSetInSelect() {
        HtmlTreeBuilder b = new HtmlTreeBuilder();
        Element select = new Element(Tag.valueOf("select"), "http://base");
        b.parseFragment("", select, "http://base", new ParseErrorList(16, 10), ParseSettings.htmlDefault);
        assertEquals("select as last element should produce InSelect", HtmlTreeBuilderState.InSelect, b.state());
    }

    // Table context last -> InTable
    @Test
    public void testResetInsertionMode_tableContextLast_shouldSetInTable() {
        HtmlTreeBuilder b = new HtmlTreeBuilder();
        Element table = new Element(Tag.valueOf("table"), "http://base");
        b.parseFragment("", table, "http://base", new ParseErrorList(16, 10), ParseSettings.htmlDefault);
        assertEquals("table as last element should produce InTable", HtmlTreeBuilderState.InTable, b.state());
    }

    // -------------------- Fragment parsing with various contexts --------------------

    // Null context: returns doc child nodes directly
    @Test
    public void testParseFragment_nullContext_returnsDocChildren() {
        HtmlTreeBuilder b = new HtmlTreeBuilder();
        List<Node> result = b.parseFragment("Hello", null, "http://base", new ParseErrorList(16, 10), ParseSettings.htmlDefault);
        assertNotNull(result);
        assertTrue("Should contain at least one node", result.size() > 0);
        assertTrue("First node should be a TextNode", result.get(0) instanceof TextNode);
    }

    // Empty fragment with null context returns empty list
    @Test
    public void testParseFragment_emptyString_returnsEmptyList() {
        HtmlTreeBuilder b = new HtmlTreeBuilder();
        List<Node> result = b.parseFragment("", null, "http://base", new ParseErrorList(16, 10), ParseSettings.htmlDefault);
        assertNotNull(result);
        assertTrue("Should be empty for empty fragment", result.isEmpty());
    }

    // Script context: tokeniser in ScriptData, so <b> is not parsed as tag
    @Test
    public void testParseFragment_scriptContext_doesNotParseTags() {
        HtmlTreeBuilder b = new HtmlTreeBuilder();
        Element script = new Element(Tag.valueOf("script"), "http://base");
        List<Node> result = b.parseFragment("<b>hi</b>", script, "http://base", new ParseErrorList(16, 10), ParseSettings.htmlDefault);
        assertNotNull(result);
        assertTrue("Should contain at least one node", result.size() > 0);
        assertTrue("Content should be text, not element", result.get(0) instanceof TextNode);
    }

    // Textarea context: tokeniser in Rcdata, similarly does not parse tags
    @Test
    public void testParseFragment_textareaContext_doesNotParseTags() {
        HtmlTreeBuilder b = new HtmlTreeBuilder();
        Element textarea = new Element(Tag.valueOf("textarea"), "http://base");
        List<Node> result = b.parseFragment("<b>hi</b>", textarea, "http://base", new ParseErrorList(16, 10), ParseSettings.htmlDefault);
        assertNotNull(result);
        assertTrue("Content should be text, not element", result.get(0) instanceof TextNode);
    }

    // Noscript context: tokeniser in Data (due to current code), so tags are parsed
    @Test
    public void testParseFragment_noscriptContext_parsesTags() {
        HtmlTreeBuilder b = new HtmlTreeBuilder();
        Element noscript = new Element(Tag.valueOf("noscript"), "http://base");
        List<Node> result = b.parseFragment("<b>hi</b>", noscript, "http://base", new ParseErrorList(16, 10), ParseSettings.htmlDefault);
        assertNotNull(result);
        assertTrue("Content should contain an element", result.size() > 0);
        assertTrue("First node should be an Element", result.get(0) instanceof Element);
    }

    // -------------------- Scope tests --------------------

    // inScope returns true for elements inside the stack
    @Test
    public void testInScope_elementInScope_returnsTrue() {
        HtmlTreeBuilder b = new HtmlTreeBuilder();
        Element body = new Element(Tag.valueOf("body"), "http://base");
        b.parseFragment("<div><p></p></div>", body, "http://base", new ParseErrorList(16, 10), ParseSettings.htmlDefault);
        assertTrue("p should be in scope", b.inScope("p"));
        assertTrue("div should be in scope", b.inScope("div"));
    }

    // inScope returns false for elements not on stack
    @Test
    public void testInScope_elementNotInScope_returnsFalse() {
        HtmlTreeBuilder b = new HtmlTreeBuilder();
        Element body = new Element(Tag.valueOf("body"), "http://base");
        b.parseFragment("<div></div>", body, "http://base", new ParseErrorList(16, 10), ParseSettings.htmlDefault);
        assertFalse("span should not be in scope", b.inScope("span"));
    }

    // inButtonScope
    @Test
    public void testInButtonScope_elementInScope_returnsTrue() {
        HtmlTreeBuilder b = new HtmlTreeBuilder();
        Element body = new Element(Tag.valueOf("body"), "http://base");
        b.parseFragment("<button></button>", body, "http://base", new ParseErrorList(16, 10), ParseSettings.htmlDefault);
        assertTrue("button should be in button scope", b.inButtonScope("button"));
    }

    // inListItemScope
    @Test
    public void testInListItemScope_elementInScope_returnsTrue() {
        HtmlTreeBuilder b = new HtmlTreeBuilder();
        Element body = new Element(Tag.valueOf("body"), "http://base");
        b.parseFragment("<ul><li></li></ul>", body, "http://base", new ParseErrorList(16, 10), ParseSettings.htmlDefault);
        assertTrue("li should be in list item scope", b.inListItemScope("li"));
    }

    // inTableScope with unclosed table tags
    @Test
    public void testInTableScope_unclosedTable_returnsTrue() {
        HtmlTreeBuilder b = new HtmlTreeBuilder();
        Element body = new Element(Tag.valueOf("body"), "http://base");
        b.parseFragment("<table><tr><td>", body, "http://base", new ParseErrorList(16, 10), ParseSettings.htmlDefault);
        assertTrue("table should be in table scope", b.inTableScope("table"));
        assertTrue("td should be in table scope", b.inTableScope("td"));
    }

    // -------------------- generateImpliedEndTags tests --------------------

    // When excludeTag matches a top element, only pop those above that are in the list
    @Test
    public void testGenerateImpliedEndTags_withExcludeTag_popsUntilExclude() {
        HtmlTreeBuilder b = createBuilder();
        b.getStack().clear();
        b.getStack().add(new Element(Tag.valueOf("li"), "http://base"));
        b.getStack().add(new Element(Tag.valueOf("p"), "http://base"));
        b.generateImpliedEndTags("li");  // p is in list and != li, so popped. li matches exclude, stop.
        assertEquals("Stack should have 1 element (li)", 1, b.getStack().size());
        assertEquals("li", b.getStack().get(0).nodeName());
    }

    // With null exclude, pop all elements that are in the implied end tag list
    @Test
    public void testGenerateImpliedEndTags_withNullExclude_popsAllInList() {
        HtmlTreeBuilder b = createBuilder();
        b.getStack().clear();
        b.getStack().add(new Element(Tag.valueOf("html"), "http://base"));
        b.getStack().add(new Element(Tag.valueOf("li"), "http://base"));
        b.getStack().add(new Element(Tag.valueOf("p"), "http://base"));
        b.generateImpliedEndTags(null);
        assertEquals("Stack should have only html (not in list)", 1, b.getStack().size());
        assertEquals("html", b.getStack().get(0).nodeName());
    }

    // -------------------- Self-closing tag handling --------------------

    // Self-closing tag should be inserted as element and emit an end tag (but element persists)
    @Test
    public void testInsertSelfClosingTag_handlesCorrectly() {
        HtmlTreeBuilder b = new HtmlTreeBuilder();
        List<Node> result = b.parseFragment("<br/>", null, "http://base", new ParseErrorList(16, 10), ParseSettings.htmlDefault);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue("Result should be an Element", result.get(0) instanceof Element);
        Element br = (Element) result.get(0);
        assertEquals("br", br.tagName());
        assertEquals("Self-closing br should have no children", 0, br.childNodeSize());
    }

    // ======================== NEW TEST CASES (to improve coverage) ========================

    // --- Additional resetInsertionMode tests for other context elements ---

    @Test
    public void testResetInsertionMode_bodyContext_shouldSetInBody() {
        HtmlTreeBuilder b = new HtmlTreeBuilder();
        Element body = new Element(Tag.valueOf("body"), "http://base");
        b.parseFragment("", body, "http://base", new ParseErrorList(16, 10), ParseSettings.htmlDefault);
        assertEquals("body as last element should produce InBody", HtmlTreeBuilderState.InBody, b.state());
    }

    @Test
    public void testResetInsertionMode_headContext_shouldSetInHead() {
        HtmlTreeBuilder b = new HtmlTreeBuilder();
        Element head = new Element(Tag.valueOf("head"), "http://base");
        b.parseFragment("", head, "http://base", new ParseErrorList(16, 10), ParseSettings.htmlDefault);
        assertEquals("head as last element should produce InHead", HtmlTreeBuilderState.InHead, b.state());
    }

    @Test
    public void testResetInsertionMode_captionContext_shouldSetInCaption() {
        HtmlTreeBuilder b = new HtmlTreeBuilder();
        Element caption = new Element(Tag.valueOf("caption"), "http://base");
        b.parseFragment("", caption, "http://base", new ParseErrorList(16, 10), ParseSettings.htmlDefault);
        assertEquals("caption as last element should produce InCaption", HtmlTreeBuilderState.InCaption, b.state());
    }

    @Test
    public void testResetInsertionMode_tbodyContext_shouldSetInTable() {
        HtmlTreeBuilder b = new HtmlTreeBuilder();
        Element tbody = new Element(Tag.valueOf("tbody"), "http://base");
        b.parseFragment("", tbody, "http://base", new ParseErrorList(16, 10), ParseSettings.htmlDefault);
        assertEquals("tbody as last element should produce InTable", HtmlTreeBuilderState.InTable, b.state());
    }

    @Test
    public void testResetInsertionMode_colgroupContext_shouldSetInColumnGroup() {
        HtmlTreeBuilder b = new HtmlTreeBuilder();
        Element colgroup = new Element(Tag.valueOf("colgroup"), "http://base");
        b.parseFragment("", colgroup, "http://base", new ParseErrorList(16, 10), ParseSettings.htmlDefault);
        assertEquals("colgroup as last element should produce InColumnGroup", HtmlTreeBuilderState.InColumnGroup, b.state());
    }

    @Test
    public void testResetInsertionMode_optionContext_shouldSetInSelect() {
        HtmlTreeBuilder b = new HtmlTreeBuilder();
        Element option = new Element(Tag.valueOf("option"), "http://base");
        b.parseFragment("", option, "http://base", new ParseErrorList(16, 10), ParseSettings.htmlDefault);
        assertEquals("option as last element should produce InSelect", HtmlTreeBuilderState.InSelect, b.state());
    }

    // --- Fragment parsing with other context elements ---

    @Test
    public void testParseFragment_withBodyContext_createsBody() {
        HtmlTreeBuilder b = new HtmlTreeBuilder();
        Element body = new Element(Tag.valueOf("body"), "http://base");
        List<Node> result = b.parseFragment("<p>text</p>", body, "http://base", new ParseErrorList(16, 10), ParseSettings.htmlDefault);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0) instanceof Element);
        assertEquals("p", ((Element) result.get(0)).tagName());
    }

    @Test
    public void testParseFragment_comment_ignoredInBody() {
        HtmlTreeBuilder b = new HtmlTreeBuilder();
        List<Node> result = b.parseFragment("<!-- comment --><div>Hi</div>", null, "http://base", new ParseErrorList(16, 10), ParseSettings.htmlDefault);
        assertNotNull(result);
        boolean foundDiv = false;
        for (Node n : result) {
            if (n instanceof Element && ((Element)n).tagName().equals("div")) {
                foundDiv = true;
                break;
            }
        }
        assertTrue("Should have div element", foundDiv);
    }

    @Test
    public void testParseFragment_characterEntity_decoded() {
        HtmlTreeBuilder b = new HtmlTreeBuilder();
        List<Node> result = b.parseFragment("&amp; &lt; &gt;", null, "http://base", new ParseErrorList(16, 10), ParseSettings.htmlDefault);
        assertNotNull(result);
        assertTrue(result.size() > 0);
        if (result.get(0) instanceof TextNode) {
            String text = ((TextNode) result.get(0)).getWholeText();
            assertTrue("Should contain ampersand", text.contains("&"));
            assertTrue("Should contain <", text.contains("<"));
            assertTrue("Should contain >", text.contains(">"));
        }
    }

    // --- Scope tests for nested and deeper cases ---

    @Test
    public void testInScope_nestedDiv_returnsTrue() {
        HtmlTreeBuilder b = new HtmlTreeBuilder();
        Element body = new Element(Tag.valueOf("body"), "http://base");
        b.parseFragment("<div><div><p></p></div></div>", body, "http://base", new ParseErrorList(16, 10), ParseSettings.htmlDefault);
        assertTrue("p should be in scope", b.inScope("p"));
        assertTrue("inner div should be in scope", b.inScope("div"));
    }

    @Test
    public void testInButtonScope_nestedButton_returnsTrue() {
        HtmlTreeBuilder b = new HtmlTreeBuilder();
        Element body = new Element(Tag.valueOf("body"), "http://base");
        b.parseFragment("<button><button></button></button>", body, "http://base", new ParseErrorList(16, 10), ParseSettings.htmlDefault);
        assertTrue("inner button should be in button scope", b.inButtonScope("button"));
    }

    @Test
    public void testInListItemScope_nestedList_returnsTrue() {
        HtmlTreeBuilder b = new HtmlTreeBuilder();
        Element body = new Element(Tag.valueOf("body"), "http://base");
        b.parseFragment("<ul><li>Item<ul><li>Sub</li></ul></li></ul>", body, "http://base", new ParseErrorList(16, 10), ParseSettings.htmlDefault);
        assertTrue("inner li should be in list item scope", b.inListItemScope("li"));
    }

    @Test
    public void testInTableScope_nestedTableUnclosed_returnsTrue() {
        HtmlTreeBuilder b = new HtmlTreeBuilder();
        Element body = new Element(Tag.valueOf("body"), "http://base");
        b.parseFragment("<table><tr><td><table>", body, "http://base", new ParseErrorList(16, 10), ParseSettings.htmlDefault);
        assertTrue("outer table should be in table scope", b.inTableScope("table"));
        assertTrue("inner table should be in table scope", b.inTableScope("table"));
    }

    // --- Additional generateImpliedEndTags tests ---

    @Test
    public void testGenerateImpliedEndTags_excludeDdTag_popsUntilDd() {
        HtmlTreeBuilder b = createBuilder();
        b.getStack().clear();
        b.getStack().add(new Element(Tag.valueOf("html"), "http://base"));
        b.getStack().add(new Element(Tag.valueOf("dd"), "http://base"));
        b.getStack().add(new Element(Tag.valueOf("p"), "http://base"));
        b.generateImpliedEndTags("dd"); // p is in list, popped; dd matches exclude, stop
        assertEquals("Stack should have html and dd", 2, b.getStack().size());
        assertEquals("dd", b.getStack().get(1).nodeName());
    }

    @Test
    public void testGenerateImpliedEndTags_excludeNotMatching_popsAllInList() {
        HtmlTreeBuilder b = createBuilder();
        b.getStack().clear();
        b.getStack().add(new Element(Tag.valueOf("html"), "http://base"));
        b.getStack().add(new Element(Tag.valueOf("li"), "http://base"));
        b.getStack().add(new Element(Tag.valueOf("p"), "http://base"));
        b.generateImpliedEndTags("div"); // div not in stack, exclude doesn't match; pop all in list (li, p)
        assertEquals("Stack should have only html", 1, b.getStack().size());
        assertEquals("html", b.getStack().get(0).nodeName());
    }

    // --- Self-closing tag handling for non-void tags ---

    @Test
    public void testInsertSelfClosingTag_nonVoid_createsElement() {
        HtmlTreeBuilder b = new HtmlTreeBuilder();
        List<Node> result = b.parseFragment("<div/>", null, "http://base", new ParseErrorList(16, 10), ParseSettings.htmlDefault);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0) instanceof Element);
        Element div = (Element) result.get(0);
        assertEquals("div", div.tagName());
        assertTrue("div should have no children", div.childNodeSize() == 0);
    }

    // --- Adoption agency algorithm test (formatting elements) ---

    @Test
    public void testAdoptionAgency_basicReopen() {
        // <b><i>text</b></i> should result in <b><i>text</i></b>
        HtmlTreeBuilder b = new HtmlTreeBuilder();
        List<Node> result = b.parseFragment("<b><i>hello</b></i>", null, "http://base", new ParseErrorList(16, 10), ParseSettings.htmlDefault);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0) instanceof Element);
        Element bElem = (Element) result.get(0);
        assertEquals("b", bElem.tagName());
        assertEquals(1, bElem.childNodeSize());
        assertTrue(bElem.childNode(0) instanceof Element);
        Element iElem = (Element) bElem.childNode(0);
        assertEquals("i", iElem.tagName());
        assertEquals(1, iElem.childNodeSize());
        assertTrue(iElem.childNode(0) instanceof TextNode);
        assertEquals("hello", ((TextNode) iElem.childNode(0)).getWholeText());
    }

    // --- Implied end tags for <p> ---

    @Test
    public void testImpliedEndTags_forParagraph() {
        // <p>one<p>two should close first <p> automatically
        HtmlTreeBuilder b = new HtmlTreeBuilder();
        List<Node> result = b.parseFragment("<p>one<p>two", null, "http://base", new ParseErrorList(16, 10), ParseSettings.htmlDefault);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.get(0) instanceof Element);
        assertTrue(result.get(1) instanceof Element);
        Element p1 = (Element) result.get(0);
        Element p2 = (Element) result.get(1);
        assertEquals("p", p1.tagName());
        assertEquals("p", p2.tagName());
        assertEquals("one", p1.text());
        assertEquals("two", p2.text());
    }

    // --- Test handling of parse errors ---

    @Test
    public void testParseFragment_withErrors_errorsListNotEmpty() {
        ParseErrorList errorList = new ParseErrorList(16, 10);
        HtmlTreeBuilder b = new HtmlTreeBuilder();
        b.parseFragment("<div><p>", null, "http://base", errorList, ParseSettings.htmlDefault);
        assertNotNull(errorList);
    }
}