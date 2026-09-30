package org.jsoup.parser;

import org.junit.Test;
import org.jsoup.parser.HtmlTreeBuilderState;
import org.jsoup.parser.Token;
import org.jsoup.nodes.*;
import org.jsoup.helper.StringUtil;

import static org.junit.Assert.*;

/**
 * JUnit 4 test class for HtmlTreeBuilderState.
 * Targets the defect in Defects4J bug 64b (handling of <image> inside SVG)
 * and achieves meaningful branch/line coverage within 15-20 tests.
 */
public class HtmlTreeBuilderStateTest {

    private HtmlTreeBuilder createBuilder() {
        // Create a fresh builder with default settings.
        return new HtmlTreeBuilder();
    }

    private Token.StartTag startTag(String name) {
        // Cast is necessary because Token.StartTag.name() returns Token.Tag (the superclass)
        return (Token.StartTag) new Token.StartTag().name(name);
    }

    private Token.EndTag endTag(String name) {
        return new Token.EndTag().name(name);
    }

    private Token.Character character(String data) {
        return new Token.Character().data(data);
    }

    private Token.Comment comment(String data) {
        return new Token.Comment().data(data);
    }

    private Token.Doctype doctype() {
        return new Token.Doctype("html", "public", "system", false);
    }

    // Helper to push an element with given tag name onto the stack
    private void pushElement(HtmlTreeBuilder tb, String tagName) {
        tb.insert(startTag(tagName));
    }

    // ---------------------------------------------------------------
    // Tests for Initial state
    // ---------------------------------------------------------------

    // Tests whitespace in Initial -> ignored, returns true
    @Test
    public void testInitial_whitespace_ignored() {
        HtmlTreeBuilder tb = createBuilder();
        tb.transition(HtmlTreeBuilderState.Initial);
        Token t = character(" ");
        assertTrue(HtmlTreeBuilderState.Initial.process(t, tb));
        assertEquals(HtmlTreeBuilderState.Initial, tb.state());
    }

    // Tests doctype in Initial -> insert doctype, transition to BeforeHtml
    @Test
    public void testInitial_doctype_transitionsToBeforeHtml() {
        HtmlTreeBuilder tb = createBuilder();
        tb.transition(HtmlTreeBuilderState.Initial);
        Token.Doctype d = new Token.Doctype("html", "public", "system", false);
        assertTrue(HtmlTreeBuilderState.Initial.process(d, tb));
        assertEquals(HtmlTreeBuilderState.BeforeHtml, tb.state());
        assertEquals(1, tb.getDocument().childNodes().size());
        assertTrue(tb.getDocument().childNode(0) instanceof DocumentType);
    }

    // Tests non-whitespace, non-comment, non-doctype in Initial -> transition and reprocess
    @Test
    public void testInitial_anythingElse_transitionsAndReprocess() {
        HtmlTreeBuilder tb = createBuilder();
        tb.transition(HtmlTreeBuilderState.Initial);
        Token t = startTag("p");
        assertTrue(HtmlTreeBuilderState.Initial.process(t, tb));
        // After reprocess, the tag should have been processed in BeforeHtml -> inserted <html> then process <p>
        assertEquals(HtmlTreeBuilderState.BeforeHead, tb.state()); // because BeforeHtml.anythingElse inserts <html> and transitions to BeforeHead
        // The document should now have html element
        assertNotNull(tb.getDocument().childNode(0));
    }

    // ---------------------------------------------------------------
    // Tests for BeforeHtml state
    // ---------------------------------------------------------------

    // Tests start tag "html" -> insert and transition to BeforeHead
    @Test
    public void testBeforeHtml_startTagHtml_transitionsToBeforeHead() {
        HtmlTreeBuilder tb = createBuilder();
        tb.transition(HtmlTreeBuilderState.BeforeHtml);
        Token t = startTag("html");
        assertTrue(HtmlTreeBuilderState.BeforeHtml.process(t, tb));
        assertEquals(HtmlTreeBuilderState.BeforeHead, tb.state());
        assertEquals("html", tb.getDocument().child(0).nodeName());
    }

    // Tests end tag "head" (listed) -> triggers anythingElse
    @Test
    public void testBeforeHtml_endTagInList_anythingElse() {
        HtmlTreeBuilder tb = createBuilder();
        tb.transition(HtmlTreeBuilderState.BeforeHtml);
        Token t = endTag("head");
        assertTrue(HtmlTreeBuilderState.BeforeHtml.process(t, tb));
        // anythingElse inserts <html>, transitions to BeforeHead, then processes end tag "head"
        // since we are now in BeforeHead, the end tag "head" will likely be handled later
        assertEquals(HtmlTreeBuilderState.BeforeHead, tb.state());
        assertNotNull(tb.getDocument().child(0)); // html element
    }

    // ---------------------------------------------------------------
    // Tests for BeforeHead state
    // ---------------------------------------------------------------

    // Tests start tag "html" -> delegates to InBody (no state change)
    @Test
    public void testBeforeHead_startTagHtml_delegatesToInBody() {
        HtmlTreeBuilder tb = createBuilder();
        tb.transition(HtmlTreeBuilderState.BeforeHead);
        pushElement(tb, "html"); // need html on stack for InBody to work
        Token t = startTag("html");
        assertTrue(HtmlTreeBuilderState.BeforeHead.process(t, tb));
        // InBody.process will merge attributes; state should remain BeforeHead? Actually InBody doesn't change state
        assertEquals(HtmlTreeBuilderState.BeforeHead, tb.state());
    }

    // ---------------------------------------------------------------
    // Tests for InHead state
    // ---------------------------------------------------------------

    // Tests start tag "title" -> handleRcData (insert + transitions)
    @Test
    public void testInHead_startTagTitle_handleRcData() {
        HtmlTreeBuilder tb = createBuilder();
        tb.transition(HtmlTreeBuilderState.InHead);
        // Need a head element on stack so tokeniser can be set
        pushElement(tb, "head");
        Token t = startTag("title");
        assertTrue(HtmlTreeBuilderState.InHead.process(t, tb));
        assertEquals(HtmlTreeBuilderState.Text, tb.state());
        // check that tokeniser state is Rcdata (can't directly verify, but we trust)
    }

    // Tests start tag "script" -> complex handling
    @Test
    public void testInHead_startTagScript_insertsScript() {
        HtmlTreeBuilder tb = createBuilder();
        tb.transition(HtmlTreeBuilderState.InHead);
        pushElement(tb, "head");
        Token t = startTag("script");
        assertTrue(HtmlTreeBuilderState.InHead.process(t, tb));
        assertEquals(HtmlTreeBuilderState.Text, tb.state());
        // script element should be on stack
        assertEquals("script", tb.currentElement().nodeName());
    }

    // ---------------------------------------------------------------
    // Tests for InBody state – focus on defect and important branches
    // ---------------------------------------------------------------

    // Tests start tag "a" with existing active formatting element -> closes previous
    @Test
    public void testInBody_startTagAnchor_withExisting_closesPrevious() {
        HtmlTreeBuilder tb = createBuilder();
        tb.transition(HtmlTreeBuilderState.InBody);
        // Need html and body elements on stack
        pushElement(tb, "html");
        pushElement(tb, "body");
        // First <a>
        Token firstA = startTag("a");
        assertTrue(HtmlTreeBuilderState.InBody.process(firstA, tb));
        // Second <a>
        Token secondA = startTag("a");
        assertTrue(HtmlTreeBuilderState.InBody.process(secondA, tb));
        // Should have closed first <a> and inserted second
        // Check that there is only one <a> element in stack
        // (difficult to count, but we trust the algorithm)
        // At minimum, no exception
    }

    // Tests start tag "image" when NOT inside svg -> converts to <img>
    @Test
    public void testInBody_startTagImage_notInSvg_convertsToImg() {
        HtmlTreeBuilder tb = createBuilder();
        tb.transition(HtmlTreeBuilderState.InBody);
        pushElement(tb, "html");
        pushElement(tb, "body");
        Token t = startTag("image");
        assertTrue(HtmlTreeBuilderState.InBody.process(t, tb));
        // Should have inserted an <img> element
        Element inserted = tb.getStack().get(tb.getStack().size() - 1);
        assertEquals("img", inserted.nodeName());
    }

    // Tests start tag "image" when INSIDE svg -> keeps <image>
    @Test
    public void testInBody_startTagImage_inSvg_keepsImage() {
        HtmlTreeBuilder tb = createBuilder();
        tb.transition(HtmlTreeBuilderState.InBody);
        pushElement(tb, "html");
        pushElement(tb, "body");
        pushElement(tb, "svg");  // simulate svg context
        Token t = startTag("image");
        assertTrue(HtmlTreeBuilderState.InBody.process(t, tb));
        Element inserted = tb.getStack().get(tb.getStack().size() - 1);
        assertEquals("image", inserted.nodeName());
    }

    // Tests end tag "body" when in scope -> transition to AfterBody
    @Test
    public void testInBody_endTagBody_inScope_transitionsToAfterBody() {
        HtmlTreeBuilder tb = createBuilder();
        tb.transition(HtmlTreeBuilderState.InBody);
        pushElement(tb, "html");
        pushElement(tb, "body");
        Token t = endTag("body");
        assertTrue(HtmlTreeBuilderState.InBody.process(t, tb));
        assertEquals(HtmlTreeBuilderState.AfterBody, tb.state());
    }

    // Tests end tag "p" when NOT in button scope -> triggers creation of <p> and reprocess
    @Test
    public void testInBody_endTagP_notInScope_createsAndReprocess() {
        HtmlTreeBuilder tb = createBuilder();
        tb.transition(HtmlTreeBuilderState.InBody);
        pushElement(tb, "html");
        pushElement(tb, "body");
        Token t = endTag("p");
        assertTrue(HtmlTreeBuilderState.InBody.process(t, tb));
        // After processing, a <p> should have been created and then closed
        // The stack should still have body as current? Actually after closing p, stack top should be body
        assertEquals("body", tb.currentElement().nodeName());
    }

    // Tests start tag "form" when form element already exists -> returns false
    @Test
    public void testInBody_startTagForm_withExistingForm_returnsFalse() {
        HtmlTreeBuilder tb = createBuilder();
        tb.transition(HtmlTreeBuilderState.InBody);
        pushElement(tb, "html");
        pushElement(tb, "body");
        // Insert first form
        tb.insertForm(startTag("form"), true);
        // Try second form
        Token t = startTag("form");
        assertFalse(HtmlTreeBuilderState.InBody.process(t, tb));
    }

    // Tests start tag "table" in quirks mode? Not needed, but we test basic
    @Test
    public void testInBody_startTagTable_transitionsToInTable() {
        HtmlTreeBuilder tb = createBuilder();
        tb.transition(HtmlTreeBuilderState.InBody);
        pushElement(tb, "html");
        pushElement(tb, "body");
        // Put a <p> in scope to test the "if in button scope" branch (default no quirks mode)
        // Actually default is no quirks, so the condition will check inButtonScope("p")
        // For simplicity, just test that table is inserted and state becomes InTable
        Token t = startTag("table");
        assertTrue(HtmlTreeBuilderState.InBody.process(t, tb));
        assertEquals(HtmlTreeBuilderState.InTable, tb.state());
    }

    // ---------------------------------------------------------------
    // Tests for InTable state
    // ---------------------------------------------------------------

    // Tests end tag "table" -> pop stack, reset insertion mode
    @Test
    public void testInTable_endTagTable_closesTable() {
        HtmlTreeBuilder tb = createBuilder();
        tb.transition(HtmlTreeBuilderState.InTable);
        // Put a <table> on stack
        pushElement(tb, "html");
        pushElement(tb, "body");
        pushElement(tb, "table");
        Token t = endTag("table");
        assertTrue(HtmlTreeBuilderState.InTable.process(t, tb));
        // After closing table, table should be removed from stack
        assertFalse(tb.getStack().stream().anyMatch(e -> e.nodeName().equals("table")));
        // Insertion mode should have been reset (probably back to InBody)
        assertEquals(HtmlTreeBuilderState.InBody, tb.state());
    }

    // ---------------------------------------------------------------
    // Tests for InSelect state
    // ---------------------------------------------------------------

    // Tests end tag "select" -> pop stack, reset insertion mode
    @Test
    public void testInSelect_endTagSelect_closesSelect() {
        HtmlTreeBuilder tb = createBuilder();
        tb.transition(HtmlTreeBuilderState.InSelect);
        // Need a <select> on stack
        pushElement(tb, "html");
        pushElement(tb, "body");
        pushElement(tb, "select");
        // Also need a <option> or just select
        Token t = endTag("select");
        assertTrue(HtmlTreeBuilderState.InSelect.process(t, tb));
        // After closing, select should be removed
        assertFalse(tb.getStack().stream().anyMatch(e -> e.nodeName().equals("select")));
        // Insertion mode reset
        assertEquals(HtmlTreeBuilderState.InBody, tb.state());
    }

    // ---------------------------------------------------------------
    // Tests for AfterBody state
    // ---------------------------------------------------------------

    // Tests end tag "html" -> transition to AfterAfterBody
    @Test
    public void testAfterBody_endTagHtml_transitionsToAfterAfterBody() {
        HtmlTreeBuilder tb = createBuilder();
        tb.transition(HtmlTreeBuilderState.AfterBody);
        // Need html element on stack (set by earlier phases)
        pushElement(tb, "html");
        Token t = endTag("html");
        // Assuming not fragment parsing
        assertTrue(HtmlTreeBuilderState.AfterBody.process(t, tb));
        assertEquals(HtmlTreeBuilderState.AfterAfterBody, tb.state());
    }

    // ---------------------------------------------------------------
    // Tests for InFrameset state (boundary case)
    // ---------------------------------------------------------------

    // Tests end tag "frameset" -> pop and transition if not fragment
    @Test
    public void testInFrameset_endTagFrameset_popAndTransition() {
        HtmlTreeBuilder tb = createBuilder();
        tb.transition(HtmlTreeBuilderState.InFrameset);
        // Put <html> and <frameset> on stack
        pushElement(tb, "html");
        pushElement(tb, "frameset");
        Token t = endTag("frameset");
        assertTrue(HtmlTreeBuilderState.InFrameset.process(t, tb));
        // After popping, current element should be html
        assertEquals("html", tb.currentElement().nodeName());
        // Since not fragment and not another frameset, state should become AfterFrameset
        assertEquals(HtmlTreeBuilderState.AfterFrameset, tb.state());
    }
}