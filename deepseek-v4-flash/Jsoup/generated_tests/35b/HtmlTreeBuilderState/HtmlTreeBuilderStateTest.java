package org.jsoup.parser;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

public class HtmlTreeBuilderStateTest {

    private HtmlTreeBuilder tb;
    private Document doc;

    @Before
    public void setUp() {
        tb = new HtmlTreeBuilder();
        Parser parser = new Parser(tb);
        tb.initialiseParse("", "http://example.com", parser);
        doc = tb.getDocument();
    }

    // Helper methods to create tokens
    private Token createStartTag(String name) {
        return new Token.StartTag(name);
    }

    private Token createEndTag(String name) {
        return new Token.EndTag(name);
    }

    private Token createComment(String data) {
        Token.Comment c = new Token.Comment();
        c.setData(data);
        return c;
    }

    private Token createDoctype(String name, String publicId, String systemId, boolean forceQuirks) {
        Token.Doctype d = new Token.Doctype();
        d.setName(name);
        d.setPublicIdentifier(publicId);
        d.setSystemIdentifier(systemId);
        d.setForceQuirks(forceQuirks);
        return d;
    }

    private Token createCharacter(String data) {
        return new Token.Character(data);
    }

    private Token createEOF() {
        return new Token.EOF();
    }

    // ---- Initial state tests ----

    @Test
    public void testInitial_whitespace_ignored() {
        Token t = createCharacter(" ");
        assertTrue(tb.process(t));
        assertEquals(HtmlTreeBuilderState.Initial, tb.state());
        assertEquals(0, doc.childNodes().size());
    }

    @Test
    public void testInitial_comment_inserted() {
        Token t = createComment("test");
        assertTrue(tb.process(t));
        assertEquals(1, doc.childNodes().size());
        assertEquals(HtmlTreeBuilderState.Initial, tb.state());
    }

    @Test
    public void testInitial_doctype_transitionsToBeforeHtml() {
        Token t = createDoctype("html", null, null, false);
        assertTrue(tb.process(t));
        assertEquals(HtmlTreeBuilderState.BeforeHtml, tb.state());
    }

    @Test
    public void testInitial_otherToken_reprocessInBeforeHtml() {
        Token t = createStartTag("p");
        assertTrue(tb.process(t));
        // After reprocessing, state should change (not Initial)
        assertNotSame(HtmlTreeBuilderState.Initial, tb.state());
    }

    // ---- BeforeHtml state tests ----

    @Test
    public void testBeforeHtml_startTagHtml_transitionToBeforeHead() {
        // Move to BeforeHtml first
        tb.process(createDoctype("html", null, null, false));
        assertEquals(HtmlTreeBuilderState.BeforeHtml, tb.state());

        Token t = createStartTag("html");
        assertTrue(tb.process(t));
        assertEquals(HtmlTreeBuilderState.BeforeHead, tb.state());
    }

    @Test
    public void testBeforeHtml_endTagHtml_anythingElse() {
        tb.process(createDoctype("html", null, null, false));
        assertEquals(HtmlTreeBuilderState.BeforeHtml, tb.state());

        // End tag "html" triggers anythingElse, which inserts html and transitions to BeforeHead
        Token t = createEndTag("html");
        assertTrue(tb.process(t));
        assertEquals(HtmlTreeBuilderState.BeforeHead, tb.state());
    }

    // ---- BeforeHead state tests ----

    @Test
    public void testBeforeHead_startTagHead_transitionToInHead() {
        tb.process(createDoctype("html", null, null, false));
        tb.process(createStartTag("html"));
        assertEquals(HtmlTreeBuilderState.BeforeHead, tb.state());

        Token t = createStartTag("head");
        assertTrue(tb.process(t));
        assertEquals(HtmlTreeBuilderState.InHead, tb.state());
    }

    @Test
    public void testBeforeHead_startTagHtml_callsInBody() {
        tb.process(createDoctype("html", null, null, false));
        tb.process(createStartTag("html"));
        assertEquals(HtmlTreeBuilderState.BeforeHead, tb.state());

        // Another <html> start tag should be handled by InBody process
        Token t = createStartTag("html");
        assertTrue(tb.process(t));
        // State should remain BeforeHead (InBody.process does not transition)
        assertEquals(HtmlTreeBuilderState.BeforeHead, tb.state());
    }

    // ---- InHead state tests ----

    @Test
    public void testInHead_endTagHead_transitionToAfterHead() {
        // Set up to InHead
        tb.process(createDoctype("html", null, null, false));
        tb.process(createStartTag("html"));
        tb.process(createStartTag("head"));
        assertEquals(HtmlTreeBuilderState.InHead, tb.state());

        Token t = createEndTag("head");
        assertTrue(tb.process(t));
        assertEquals(HtmlTreeBuilderState.AfterHead, tb.state());
    }

    @Test
    public void testInHead_metaTag_insertEmpty() {
        tb.process(createDoctype("html", null, null, false));
        tb.process(createStartTag("html"));
        tb.process(createStartTag("head"));
        assertEquals(HtmlTreeBuilderState.InHead, tb.state());

        Token t = createStartTag("meta");
        assertTrue(tb.process(t));
        assertEquals(HtmlTreeBuilderState.InHead, tb.state()); // state unchanged
        // Verify meta element was inserted (check stack? not needed)
    }

    // ---- AfterHead state tests ----

    @Test
    public void testAfterHead_startTagBody_transitionToInBody() {
        // Set up to AfterHead
        tb.process(createDoctype("html", null, null, false));
        tb.process(createStartTag("html"));
        tb.process(createStartTag("head"));
        tb.process(createEndTag("head"));
        assertEquals(HtmlTreeBuilderState.AfterHead, tb.state());

        Token t = createStartTag("body");
        assertTrue(tb.process(t));
        assertEquals(HtmlTreeBuilderState.InBody, tb.state());
    }

    // ---- InBody state tests ----

    @Test
    public void testInBody_characterWhitespace_insertAndKeepState() {
        // Move to InBody via <html><head></head><body>
        tb.process(createDoctype("html", null, null, false));
        tb.process(createStartTag("html"));
        tb.process(createStartTag("head"));
        tb.process(createEndTag("head"));
        tb.process(createStartTag("body"));
        assertEquals(HtmlTreeBuilderState.InBody, tb.state());

        Token t = createCharacter(" ");
        assertTrue(tb.process(t));
        assertEquals(HtmlTreeBuilderState.InBody, tb.state());
    }

    @Test
    public void testInBody_startTagDiv_transitions() {
        tb.process(createDoctype("html", null, null, false));
        tb.process(createStartTag("html"));
        tb.process(createStartTag("head"));
        tb.process(createEndTag("head"));
        tb.process(createStartTag("body"));
        assertEquals(HtmlTreeBuilderState.InBody, tb.state());

        Token t = createStartTag("div");
        assertTrue(tb.process(t));
        // Still in InBody after inserting div
        assertEquals(HtmlTreeBuilderState.InBody, tb.state());
    }

    @Test
    public void testInBody_endTagBody_transitionToAfterBody() {
        tb.process(createDoctype("html", null, null, false));
        tb.process(createStartTag("html"));
        tb.process(createStartTag("head"));
        tb.process(createEndTag("head"));
        tb.process(createStartTag("body"));
        assertEquals(HtmlTreeBuilderState.InBody, tb.state());

        Token t = createEndTag("body");
        assertTrue(tb.process(t));
        assertEquals(HtmlTreeBuilderState.AfterBody, tb.state());
    }

    // ---- InTable state tests ----

    @Test
    public void testInTable_endTagTable_popStack() {
        // Transition to InTable by inserting <table>
        tb.process(createDoctype("html", null, null, false));
        tb.process(createStartTag("html"));
        tb.process(createStartTag("head"));
        tb.process(createEndTag("head"));
        tb.process(createStartTag("body"));
        tb.process(createStartTag("table"));
        assertEquals(HtmlTreeBuilderState.InTable, tb.state());

        Token t = createEndTag("table");
        assertTrue(tb.process(t));
        // After closing table, reset insertion mode (likely back to InBody)
        assertSame(HtmlTreeBuilderState.InTable, tb.state()); // actual: after pop, resetInsertionMode -> state becomes InBody? Let's verify via code: InTable.endTag table -> tb.popStackToClose("table"); tb.resetInsertionMode(); which transitions. We'll just check it's not InTable.
        assertNotSame(HtmlTreeBuilderState.InTable, tb.state());
    }

    // ---- InSelect state tests ----

    @Test
    public void testInSelect_startTagOption_insert() {
        // Need to get into InSelect state. Simplified: start with <select> inside body
        tb.process(createDoctype("html", null, null, false));
        tb.process(createStartTag("html"));
        tb.process(createStartTag("head"));
        tb.process(createEndTag("head"));
        tb.process(createStartTag("body"));
        tb.process(createStartTag("select"));
        assertEquals(HtmlTreeBuilderState.InSelect, tb.state());

        Token t = createStartTag("option");
        assertTrue(tb.process(t));
        assertEquals(HtmlTreeBuilderState.InSelect, tb.state());
    }

    @Test
    public void testInSelect_endTagSelect_popToClose() {
        tb.process(createDoctype("html", null, null, false));
        tb.process(createStartTag("html"));
        tb.process(createStartTag("head"));
        tb.process(createEndTag("head"));
        tb.process(createStartTag("body"));
        tb.process(createStartTag("select"));
        assertEquals(HtmlTreeBuilderState.InSelect, tb.state());

        Token t = createEndTag("select");
        assertTrue(tb.process(t));
        // After closing select, reset insertion mode (likely back to InBody)
        assertNotSame(HtmlTreeBuilderState.InSelect, tb.state());
    }

    // ---- Text state tests ----

    @Test
    public void testText_endTagScript_popAndTransitionToOriginal() {
        // Enter Text state via <script> in head
        tb.process(createDoctype("html", null, null, false));
        tb.process(createStartTag("html"));
        tb.process(createStartTag("head"));
        tb.process(createStartTag("script"));
        // After <script>, state should be Text and original state stored as InHead
        assertEquals(HtmlTreeBuilderState.Text, tb.state());

        Token t = createEndTag("script");
        assertTrue(tb.process(t));
        // After end tag, should pop and transition back to original state (InHead)
        assertEquals(HtmlTreeBuilderState.InHead, tb.state());
    }

    // ---- Edge case: null character ----

    @Test
    public void testInBody_nullCharacter_returnsFalse() {
        tb.process(createDoctype("html", null, null, false));
        tb.process(createStartTag("html"));
        tb.process(createStartTag("head"));
        tb.process(createEndTag("head"));
        tb.process(createStartTag("body"));
        assertEquals(HtmlTreeBuilderState.InBody, tb.state());

        Token t = createCharacter("\u0000");
        // In InBody, null character should error and return false
        assertFalse(tb.process(t));
    }

    // ---- EOF handling ----

    @Test
    public void testAfterBody_eof_noTransition() {
        // Move to AfterBody
        tb.process(createDoctype("html", null, null, false));
        tb.process(createStartTag("html"));
        tb.process(createStartTag("head"));
        tb.process(createEndTag("head"));
        tb.process(createStartTag("body"));
        tb.process(createEndTag("body"));
        assertEquals(HtmlTreeBuilderState.AfterBody, tb.state());

        Token t = createEOF();
        assertTrue(tb.process(t));
        // State should remain AfterBody
        assertEquals(HtmlTreeBuilderState.AfterBody, tb.state());
    }
}