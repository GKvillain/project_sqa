package org.jsoup.parser;

import org.jsoup.nodes.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class HtmlTreeBuilderStateTest {

    private HtmlTreeBuilder tb;

    @org.junit.Before
    public void setUp() {
        tb = new HtmlTreeBuilder();
    }

    // Initial state: whitespace token is ignored
    @Test
    public void testInitial_processWhitespace_ignores() {
        Token.Character whitespace = new Token.Character().data("   ");
        boolean result = tb.process(whitespace);
        assertTrue(result);
        assertTrue(tb.getDocument().childNodes().isEmpty());
    }

    // Initial state: comment token is inserted
    @Test
    public void testInitial_processComment_insertsComment() {
        Token.Comment comment = new Token.Comment();
        comment.data("test");
        tb.process(comment);
        assertEquals(1, tb.getDocument().childNodeSize());
        assertTrue(tb.getDocument().childNode(0) instanceof Comment);
    }

    // Initial state: doctype token transitions to BeforeHtml
    @Test
    public void testInitial_processDoctype_transitionsToBeforeHtml() {
        Token.Doctype doctype = new Token.Doctype();
        doctype.setName("html");
        tb.process(doctype);
        assertEquals(HtmlTreeBuilderState.BeforeHtml, tb.state());
        assertEquals(1, tb.getDocument().childNodeSize());
        assertTrue(tb.getDocument().childNode(0) instanceof DocumentType);
    }

    // BeforeHtml state: start tag "html" transitions to BeforeHead
    @Test
    public void testBeforeHtml_processStartTagHtml_transitionsToBeforeHead() {
        tb.process(new Token.Doctype()); // go to BeforeHtml
        Token.StartTag html = new Token.StartTag();
        html.name("html");
        boolean result = tb.process(html);
        assertTrue(result);
        assertEquals(HtmlTreeBuilderState.BeforeHead, tb.state());
    }

    // BeforeHead state: start tag "head" transitions to InHead
    @Test
    public void testBeforeHead_processStartTagHead_transitionsToInHead() {
        tb.process(new Token.Doctype());
        tb.process(new Token.StartTag().name("html"));
        Token.StartTag head = new Token.StartTag();
        head.name("head");
        boolean result = tb.process(head);
        assertTrue(result);
        assertEquals(HtmlTreeBuilderState.InHead, tb.state());
        assertEquals("head", tb.currentElement().nodeName());
    }

    // InHead state: start tag "title" transitions to Text
    @Test
    public void testInHead_processStartTagTitle_transitionsToText() {
        enterInHead();
        Token.StartTag title = new Token.StartTag();
        title.name("title");
        boolean result = tb.process(title);
        assertTrue(result);
        assertEquals(HtmlTreeBuilderState.Text, tb.state());
        assertEquals("title", tb.currentElement().nodeName());
    }

    // Helper: move to InHead state
    private void enterInHead() {
        tb.process(new Token.Doctype());
        tb.process(new Token.StartTag().name("html"));
        tb.process(new Token.StartTag().name("head"));
    }

    // InBody state: start tag "a" with existing active formatting element
    @Test
    public void testInBody_processStartTagA_withExistingActiveFormattingElement_closesAndReinserts() {
        enterInBody();
        tb.process(new Token.StartTag().name("a"));
        tb.process(new Token.StartTag().name("a"));
        assertEquals("a", tb.currentElement().nodeName());
    }

    // InBody state: start tag "form" when form already exists returns false
    @Test
    public void testInBody_processStartTagForm_whenFormExists_returnsFalse() {
        enterInBody();
        tb.process(new Token.StartTag().name("form"));
        assertNotNull(tb.getFormElement());
        boolean result = tb.process(new Token.StartTag().name("form"));
        assertFalse(result);
    }

    // InBody state: start tag "table" transitions to InTable
    @Test
    public void testInBody_processStartTagTable_transitionsToInTable() {
        enterInBody();
        tb.process(new Token.StartTag().name("table"));
        assertEquals(HtmlTreeBuilderState.InTable, tb.state());
    }

    // InBody state: end tag "body" transitions to AfterBody
    @Test
    public void testInBody_processEndTagBody_transitionsToAfterBody() {
        enterInBody();
        tb.process(new Token.EndTag().name("body"));
        assertEquals(HtmlTreeBuilderState.AfterBody, tb.state());
    }

    // InBody state: end tag "p" when not in scope creates a <p>
    @Test
    public void testInBody_processEndTagP_whenNotInButtonScope_insertsP() {
        enterInBody();
        tb.process(new Token.EndTag().name("p"));
        assertEquals("p", tb.currentElement().nodeName());
    }

    // InTable state: start tag "caption" transitions to InCaption
    @Test
    public void testInTable_processStartTagCaption_transitionsToInCaption() {
        enterInBody();
        tb.process(new Token.StartTag().name("table"));
        Token.StartTag caption = new Token.StartTag();
        caption.name("caption");
        tb.process(caption);
        assertEquals(HtmlTreeBuilderState.InCaption, tb.state());
    }

    // InTable state: end tag "table" pops to close and returns to InBody
    @Test
    public void testInTable_processEndTagTable_popsToClose() {
        enterInBody();
        tb.process(new Token.StartTag().name("table"));
        tb.process(new Token.EndTag().name("table"));
        assertEquals(HtmlTreeBuilderState.InBody, tb.state());
    }

    // InSelect state: start tag "option" inserts option
    @Test
    public void testInSelect_processStartTagOption_insertsOption() {
        enterInBody();
        tb.process(new Token.StartTag().name("select"));
        assertEquals(HtmlTreeBuilderState.InSelect, tb.state());
        Token.StartTag option = new Token.StartTag();
        option.name("option");
        tb.process(option);
        assertEquals("option", tb.currentElement().nodeName());
    }

    // AfterBody state: end tag "html" transitions to AfterAfterBody
    @Test
    public void testAfterBody_processEndTagHtml_transitionsToAfterAfterBody() {
        enterInBody();
        tb.process(new Token.EndTag().name("body"));
        tb.process(new Token.EndTag().name("html"));
        assertEquals(HtmlTreeBuilderState.AfterAfterBody, tb.state());
    }

    // Helper: move to InBody state
    private void enterInBody() {
        tb.process(new Token.Doctype());
        tb.process(new Token.StartTag().name("html"));
        tb.process(new Token.StartTag().name("head"));
        tb.process(new Token.StartTag().name("body"));
    }
}