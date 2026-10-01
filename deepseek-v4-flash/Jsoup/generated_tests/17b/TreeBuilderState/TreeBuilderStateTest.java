package org.jsoup.parser;

import org.jsoup.nodes.*;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * JUnit 4 test class for TreeBuilderState, focusing on the Defects4J bug 17b.
 * The bug is related to the handling of the "isindex" tag in the InBody state,
 * specifically the processing of its attributes and the generation of the form,
 * input, and related elements.
 */
public class TreeBuilderStateTest {

    /**
     * Helper method to create a TreeBuilder instance for testing.
     * Uses HtmlTreeBuilderStateTest's approach of creating a parser and parsing a fragment.
     */
    private TreeBuilder createTreeBuilder() {
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        // Initialize the builder with a base URI and parse a dummy fragment to set up the state
        builder.initialiseParse("", "http://example.com", new Parser(builder));
        return builder;
    }

    /**
     * Helper to parse a fragment and return the document.
     */
    private Document parseFragment(String html) {
        Parser parser = Parser.htmlParser();
        return parser.parseFragment(html, "http://example.com").get(0).ownerDocument();
    }

    /**
     * Tests the normal parsing of a simple HTML document to ensure the basic flow works.
     */
    @Test
    public void testProcess_normalHtml_parsesSuccessfully() {
        Document doc = parseFragment("<html><head><title>Test</title></head><body><p>Hello</p></body></html>");
        assertNotNull(doc);
        Element body = doc.body();
        assertNotNull(body);
        assertEquals(1, body.children().size());
        assertEquals("p", body.child(0).tagName());
    }

    /**
     * Tests the parsing of whitespace in the Initial state.
     */
    @Test
    public void testProcess_initialStateWhitespace_ignoresWhitespace() {
        Document doc = parseFragment("   <html></html>");
        assertNotNull(doc);
    }

    /**
     * Tests the parsing of a comment in the Initial state.
     */
    @Test
    public void testProcess_initialStateComment_insertsComment() {
        Document doc = parseFragment("<!-- comment --><html></html>");
        assertNotNull(doc);
        // The comment should be inserted before the html tag
    }

    /**
     * Tests the parsing of a doctype in the Initial state.
     */
    @Test
    public void testProcess_initialStateDoctype_transitionsToBeforeHtml() {
        Document doc = parseFragment("<!DOCTYPE html><html></html>");
        assertNotNull(doc);
        assertEquals("#doctype", doc.childNode(0).nodeName());
    }

    /**
     * Tests the parsing of a start tag "html" in BeforeHtml state.
     */
    @Test
    public void testProcess_beforeHtmlStartTagHtml_transitionsToBeforeHead() {
        Document doc = parseFragment("<html><head></head><body></body></html>");
        assertNotNull(doc);
        assertEquals("html", doc.child(0).tagName());
    }

    /**
     * Tests the parsing of a start tag "head" in BeforeHead state.
     */
    @Test
    public void testProcess_beforeHeadStartTagHead_transitionsToInHead() {
        Document doc = parseFragment("<html><head><title>a</title></head><body></body></html>");
        assertNotNull(doc);
        Element head = doc.head();
        assertNotNull(head);
        assertEquals("head", head.tagName());
    }

    /**
     * Tests the parsing of a start tag "body" in AfterHead state.
     */
    @Test
    public void testProcess_afterHeadStartTagBody_transitionsToInBody() {
        Document doc = parseFragment("<html><head></head><body><p>text</p></body></html>");
        assertNotNull(doc);
        Element body = doc.body();
        assertNotNull(body);
        assertEquals("body", body.tagName());
    }

    /**
     * Tests the parsing of the "isindex" tag in InBody state.
     * This is related to the Defects4J bug 17b. The bug involved incorrect handling
     * of attributes, where the "action" attribute from the isindex tag was not
     * properly applied to the form element, and the "prompt" attribute was not used.
     * This test verifies the basic isindex behavior.
     */
    @Test
    public void testProcess_inBodyIsindex_generatesFormInput() {
        // Basic isindex without attributes
        Document doc = parseFragment("<html><body><isindex></body></html>");
        assertNotNull(doc);
        Element body = doc.body();
        // The isindex should generate form, hr, label, input, hr, /form
        assertNotNull(body);
        // Should have at least form element
        assertTrue(body.children().size() > 0);
        // The first child should be a form
        Element form = body.child(0);
        assertEquals("form", form.tagName());
    }

    /**
     * Tests the "isindex" tag with an "action" attribute.
     * This tests a specific scenario related to the Defects4J bug 17b.
     */
    @Test
    public void testProcess_inBodyIsindexWithAction_setsFormAction() {
        Document doc = parseFragment("<html><body><isindex action=\"/search\"></body></html>");
        assertNotNull(doc);
        Element body = doc.body();
        assertNotNull(body);
        Element form = body.child(0);
        assertEquals("form", form.tagName());
        // The form should have the action attribute
        assertEquals("/search", form.attr("action"));
    }

    /**
     * Tests the "isindex" tag with a "prompt" attribute.
     * This tests a specific scenario related to the Defects4J bug 17b.
     */
    @Test
    public void testProcess_inBodyIsindexWithPrompt_usesPromptText() {
        Document doc = parseFragment("<html><body><isindex prompt=\"Enter keywords:\"></body></html>");
        assertNotNull(doc);
        Element body = doc.body();
        assertNotNull(body);
        // The prompt should be inserted as text within a label
        Element label = body.select("label").first();
        assertNotNull(label);
        assertEquals("Enter keywords:", label.text());
    }

    /**
     * Tests the parsing of an end tag "body" in InBody state.
     */
    @Test
    public void testProcess_inBodyEndTagBody_transitionsToAfterBody() {
        Document doc = parseFragment("<html><body><p>text</p></body></html>");
        assertNotNull(doc);
        assertEquals("html", doc.tagName());
    }

    /**
     * Tests the parsing of a table element in InBody state.
     */
    @Test
    public void testProcess_inBodyStartTagTable_transitionsToInTable() {
        Document doc = parseFragment("<html><body><table><tr><td>cell</td></tr></table></body></html>");
        assertNotNull(doc);
        Element table = doc.select("table").first();
        assertNotNull(table);
        assertEquals("table", table.tagName());
    }

    /**
     * Tests the parsing of a script tag in InHead state.
     */
    @Test
    public void testProcess_inHeadStartTagScript_transitionsToText() {
        Document doc = parseFragment("<html><head><script>alert('hi');</script></head><body></body></html>");
        assertNotNull(doc);
        Element script = doc.select("script").first();
        assertNotNull(script);
        assertEquals("alert('hi');", script.data());
    }

    /**
     * Tests the parsing of a "select" tag inside a table (InTable state).
     */
    @Test
    public void testProcess_inTableStartTagSelect_transitionsToInSelectInTable() {
        Document doc = parseFragment("<html><body><table><tr><td><select><option>a</option></select></td></tr></table></body></html>");
        assertNotNull(doc);
        Element select = doc.select("select").first();
        assertNotNull(select);
        assertEquals("select", select.tagName());
    }

    /**
     * Tests the parsing of a character token in InTableText state that is not whitespace.
     */
    @Test
    public void testProcess_inTableTextNonWhitespace_processesInBody() {
        Document doc = parseFragment("<html><body><table>text</table></body></html>");
        assertNotNull(doc);
        // The text should be inserted before the table (foster parenting)
        Element body = doc.body();
        assertNotNull(body);
        // body should have text and table as children
        assertEquals(2, body.children().size());
        // First child should be the text node (foster parented)
        assertEquals("text", body.child(0).toString().trim());
    }

    /**
     * Tests the parsing of a "p" end tag in InBody state when not in scope.
     */
    @Test
    public void testProcess_inBodyEndTagPNotInScope_createsEmptyP() {
        Document doc = parseFragment("<html><body></p></body></html>");
        assertNotNull(doc);
        // The parser should create an empty <p></p> before closing it
        Element body = doc.body();
        assertNotNull(body);
        // There should be a p element (empty)
        Element p = body.select("p").first();
        assertNotNull(p);
        assertEquals("p", p.tagName());
    }

    /**
     * Tests the parsing of a "br" end tag in InBody state.
     */
    @Test
    public void testProcess_inBodyEndTagBr_processesAsStartTag() {
        Document doc = parseFragment("<html><body>text</br>more</body></html>");
        assertNotNull(doc);
        Element body = doc.body();
        assertNotNull(body);
        // The <br> should be inserted as a self-closing tag
        // The </br> should be treated as <br>
        assertEquals(3, body.children().size()); // text, br, text
        assertEquals("br", body.child(1).tagName());
    }

    /**
     * Tests the parsing of an end tag "noscript" in InHeadNoscript state.
     */
    @Test
    public void testProcess_inHeadNoscriptEndTagNoscript_transitionsToInHead() {
        Document doc = parseFragment("<html><head><noscript><meta charset=\"utf-8\"></noscript></head><body></body></html>");
        assertNotNull(doc);
        // The noscript tag should be handled
        Element head = doc.head();
        assertNotNull(head);
        // The noscript element should be present
        assertTrue(head.children().size() > 0);
    }

    /**
     * Tests the parsing of a "frame" tag in InFrameset state.
     */
    @Test
    public void testProcess_inFramesetStartTagFrame_insertsEmptyFrame() {
        Document doc = parseFragment("<html><frameset><frame src=\"a.html\"></frameset></html>");
        assertNotNull(doc);
        Element frame = doc.select("frame").first();
        assertNotNull(frame);
        assertEquals("frame", frame.tagName());
        assertEquals("a.html", frame.attr("src"));
    }

    /**
     * Tests the parsing of a comment in AfterBody state.
     */
    @Test
    public void testProcess_afterBodyComment_insertsComment() {
        Document doc = parseFragment("<html><body></body><!-- after body comment --></html>");
        assertNotNull(doc);
        // The comment should be inserted into the html element
        assertEquals(1, doc.childNodes().size());
        // In jsoup, the comment after body goes into the html element
        assertTrue(doc.childNode(0) instanceof Element);
    }
}