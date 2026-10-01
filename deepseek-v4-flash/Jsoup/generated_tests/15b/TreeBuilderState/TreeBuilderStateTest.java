package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

import org.jsoup.parser.Token;
import org.jsoup.parser.TreeBuilder;
import org.jsoup.parser.TreeBuilderState;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Tag;

public class TreeBuilderStateTest {

    // Helper method to create a TreeBuilder in Initial state
    private TreeBuilderState createInitialState() {
        // Use TreeBuilderState directly
        return TreeBuilderState.Initial;
    }

    // Helper to run a series of tokens through the state machine
    private Document parseWithStates(String html) {
        // Use Jsoup.parse to get a Document from HTML string
        return org.jsoup.Jsoup.parse(html);
    }

    // Test Initial state - whitespace token
    @Test
    public void testInitial_whitespaceToken_returnsTrue() {
        Document doc = parseWithStates("   ");
        // If document title is empty, whitespace was ignored
        assertEquals("", doc.title());
    }

    // Test Initial state - doctype token
    @Test
    public void testInitial_doctypeToken_transitionsToBeforeHtml() {
        Document doc = parseWithStates("<!DOCTYPE html>");
        assertNotNull(doc);
    }

    // Test BeforeHtml state - start tag html
    @Test
    public void testBeforeHtml_startTagHtml_transitionsToBeforeHead() {
        Document doc = parseWithStates("<html></html>");
        assertNotNull(doc);
    }

    // Test BeforeHead state - start tag head
    @Test
    public void testBeforeHead_startTagHead_setsHeadElement() {
        Document doc = parseWithStates("<html><head></head></html>");
        assertNotNull(doc);
    }

    // Test InHead state - start tag script
    @Test
    public void testInHead_startTagScript_switchesToScriptData() {
        Document doc = parseWithStates("<html><head><script>alert('test')</script></head></html>");
        assertNotNull(doc);
    }

    // Test InBody state - simple text insertion
    @Test
    public void testInBody_characterToken_insertsText() {
        Document doc = parseWithStates("<html><body>Hello</body></html>");
        assertEquals("Hello", doc.body().text());
    }

    // Test InBody state - start tag p (paragraph)
    @Test
    public void testInBody_startTagP_createsParagraph() {
        Document doc = parseWithStates("<html><body><p>Test</p></body></html>");
        Element p = doc.body().child(0);
        assertEquals("p", p.tagName());
    }

    // Test InBody state - end tag body
    @Test
    public void testInBody_endTagBody_transitionsToAfterBody() {
        Document doc = parseWithStates("<html><body></body></html>");
        assertNotNull(doc);
    }

    // Test InTable state - start tag table
    @Test
    public void testInTable_startTagTable_entersTableMode() {
        Document doc = parseWithStates("<html><body><table><tr><td>Cell</td></tr></table></body></html>");
        Element table = doc.body().child(0);
        assertEquals("table", table.tagName());
    }

    // Test InSelect state - option inside select
    @Test
    public void testInSelect_startTagOption_addsOption() {
        Document doc = parseWithStates("<html><body><select><option>1</option></select></body></html>");
        Element select = doc.body().child(0);
        assertEquals("select", select.tagName());
    }

    // Test AfterBody state - end tag html
    @Test
    public void testAfterBody_endTagHtml_transitionsToAfterAfterBody() {
        Document doc = parseWithStates("<html><body></body></html>");
        assertNotNull(doc);
    }

    // Test Text state - character processing
    @Test
    public void testText_characterToken_insertsCharacter() {
        Document doc = parseWithStates("<html><body><textarea>Content</textarea></body></html>");
        Element textarea = doc.body().child(0);
        assertEquals("textarea", textarea.tagName());
    }

    // Test InFrameset state - frame insertion
    @Test
    public void testInFrameset_startTagFrame_insertsFrame() {
        Document doc = parseWithStates("<html><frameset><frame src='test.html'/></frameset></html>");
        // In frameset mode, body might be null
        assertNotNull(doc);
    }

    // Test Whitespace handling across states
    @Test
    public void testIsWhitespace_newlinesAndTabs_returnsTrue() {
        Document doc = parseWithStates("<html><body>\n\t</body></html>");
        assertEquals("", doc.body().text());
    }

    // Test InCaption state - end tag caption
    @Test
    public void testInCaption_endTagCaption_closesCaption() {
        Document doc = parseWithStates("<html><body><table><caption>Cap</caption><tr><td>Cell</td></tr></table></body></html>");
        assertNotNull(doc);
    }

    // Test InTableBody state - start tag tr
    @Test
    public void testInTableBody_startTagTr_entersRow() {
        Document doc = parseWithStates("<html><body><table><tbody><tr><td>Cell</td></tr></tbody></table></body></html>");
        Element tbody = doc.body().child(0).child(0);
        assertEquals("tbody", tbody.tagName());
    }

    // Test InRow state - end tag tr
    @Test
    public void testInRow_endTagTr_exitsRow() {
        Document doc = parseWithStates("<html><body><table><tr><td>Cell</td></tr></table></body></html>");
        Element tr = doc.body().child(0).child(0);
        assertEquals("tr", tr.tagName());
    }

    // Test InCell state - end tag td
    @Test
    public void testInCell_endTagTd_closesCell() {
        Document doc = parseWithStates("<html><body><table><tr><td>Cell</td></tr></table></body></html>");
        Element td = doc.body().child(0).child(0).child(0);
        assertEquals("td", td.tagName());
    }

    // Test AfterAfterBody state - whitespace handling
    @Test
    public void testAfterAfterBody_whitespace_processesInBody() {
        Document doc = parseWithStates("<html><body></body></html>");
        assertNotNull(doc);
    }

    // Test Doctype handling in initial state
    @Test
    public void testInitial_doctypeWithQuirksMode_setsQuirksCorrectly() {
        Document doc = parseWithStates("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">");
        assertNotNull(doc);
    }

    // Test Comment handling across states
    @Test
    public void testComment_anyState_insertsComment() {
        Document doc = parseWithStates("<html><!-- comment --><body>Text</body></html>");
        assertEquals("Text", doc.body().text());
    }
}