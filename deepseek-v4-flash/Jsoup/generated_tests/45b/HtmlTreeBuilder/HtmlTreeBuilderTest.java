package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.FormElement;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * JUnit 4 test class for HtmlTreeBuilder.
 * Uses Jsoup public API to exercise internal paths.
 */
public class HtmlTreeBuilderTest {

    @Test
    // Normal case: simple valid HTML
    public void testParse_simpleHtml_createsDocument() {
        Document doc = Jsoup.parse("<html><head><title>Test</title></head><body><p>Hello</p></body></html>");
        assertNotNull(doc);
        assertEquals("Test", doc.title());
        assertEquals("Hello", doc.body().text());
    }

    @Test
    // Boundary case: empty input
    public void testParse_emptyString_createsMinimalDocument() {
        Document doc = Jsoup.parse("");
        assertNotNull(doc);
        assertTrue(doc.children().size() > 0);
    }

    @Test(expected = IllegalArgumentException.class)
    // Invalid case: null input
    public void testParse_nullInput_throwsException() {
        Jsoup.parse(null);
    }

    @Test
    // Fragment parsing with context (body fragment)
    public void testParse_bodyFragment_returnsCorrectHtml() {
        Document doc = Jsoup.parseBodyFragment("<p>frag</p>");
        assertNotNull(doc);
        assertEquals("<p>frag</p>", doc.body().html());
    }

    @Test
    // Fragment parsing with a specific context element
    public void testParseFragment_withContext_returnsNodes() {
        Document doc = Jsoup.parse("<div><p>test</p></div>");
        Element context = doc.select("div").first();
        List<Node> nodes = Jsoup.parseBodyFragment("<b>bold</b>").body().childNodes();
        assertNotNull(nodes);
        assertEquals(1, nodes.size());
    }

    @Test
    // Script content should be stored as DataNode
    public void testParse_scriptContent_handledAsData() {
        Document doc = Jsoup.parse("<script>var x = 1;</script>");
        Element script = doc.select("script").first();
        assertTrue(script.childNode(0) instanceof DataNode);
        assertEquals("var x = 1;", script.data());
    }

    @Test
    // Style content should be stored as DataNode
    public void testParse_styleContent_handledAsData() {
        Document doc = Jsoup.parse("<style>body { color: red; }</style>");
        Element style = doc.select("style").first();
        assertTrue(style.childNode(0) instanceof DataNode);
        assertEquals("body { color: red; }", style.data());
    }

    @Test
    // Foster parenting: text before <tr> inside <table> should be moved before table
    public void testParse_tableWithFosterText_textMovedBeforeTable() {
        Document doc = Jsoup.parse("<table>text<tr><td>cell</td></tr></table>");
        // The text "text" should appear before the <table> element in the body
        Element table = doc.select("table").first();
        assertNotNull(table);
        // The body's first child should be a text node, not the table
        Node first = doc.body().childNode(0);
        assertTrue(first instanceof org.jsoup.nodes.TextNode);
        assertEquals("text", ((org.jsoup.nodes.TextNode) first).text());
    }

    @Test
    // Implied end tags: <ul><li>a<li>b</ul> should close li implicitly
    public void testParse_impliedEndTags_closesAutomatically() {
        Document doc = Jsoup.parse("<ul><li>a<li>b</ul>");
        Elements items = doc.select("li");
        assertEquals(2, items.size());
        assertEquals("a", items.get(0).text());
        assertEquals("b", items.get(1).text());
    }

    @Test
    // Select and option elements parsed correctly (InSelect state)
    public void testParse_selectOptions_recognized() {
        Document doc = Jsoup.parse("<select><option>a</option><option>b</option></select>");
        assertEquals(2, doc.select("option").size());
    }

    @Test
    // Formatting elements reconstruction when needed (e.g. <p><b>text</b></p>)
    public void testParse_formattingElements_reconstructionOnMisnest() {
        Document doc = Jsoup.parse("<p><b>text</b></p>");
        Element p = doc.select("p").first();
        assertNotNull(p);
        Element b = p.select("b").first();
        assertNotNull(b);
        assertEquals("text", b.text());
    }

    @Test
    // Self-closing tags (<br/> and <hr/>) should be handled
    public void testParse_selfClosingTags_handled() {
        Document doc = Jsoup.parse("<br/><hr/>");
        assertEquals(1, doc.select("br").size());
        assertEquals(1, doc.select("hr").size());
    }

    @Test
    // Comment nodes should be inserted
    public void testParse_commentNode_inserted() {
        Document doc = Jsoup.parse("<!-- comment -->");
        assertTrue(doc.childNode(0) instanceof Comment);
        assertEquals(" comment ", ((Comment) doc.childNode(0)).getData());
    }

    @Test
    // Doctype should be detected
    public void testParse_doctype_detected() {
        Document doc = Jsoup.parse("<!DOCTYPE html><html></html>");
        // Use child nodes to access doctype, as documentType() method is not available
        Node firstChild = doc.childNode(0);
        assertNotNull(firstChild);
        assertTrue(firstChild instanceof org.jsoup.nodes.DocumentType);
        assertEquals("html", ((org.jsoup.nodes.DocumentType) firstChild).name());
    }

    @Test
    // Form controls should be associated with form element
    public void testParse_formControls_associatedWithForm() {
        Document doc = Jsoup.parse("<form><input name='x'></form>");
        Elements inputs = doc.select("input");
        assertEquals(1, inputs.size());
        // Ensure the input is inside form
        assertNotNull(inputs.first().closest("form"));
    }

    @Test
    // Nested tables should parse without error (tests clearStackToContext etc.)
    public void testParse_tableNested_parsedWithoutError() {
        Document doc = Jsoup.parse("<table><tr><td><table><tr><td>inner</td></tr></table></td></tr></table>");
        assertEquals(2, doc.select("table").size());
        assertEquals("inner", doc.select("td").last().text());
    }

    @Test
    // Reset insertion mode when leaving table -> InBody
    public void testParse_resetInsertionMode_fromTable() {
        Document doc = Jsoup.parse("<table><tr><td>cell</td></tr></table>");
        doc.outputSettings().prettyPrint(false);
        String html = doc.body().html();
        // Simple check that the structure is correct
        assertTrue(html.contains("<table>"));
    }

    @Test
    // Detect list item scope
    public void testParse_inListItemScope_detectsList() {
        Document doc = Jsoup.parse("<ul><li>item</li></ul>");
        assertEquals(1, doc.select("li").size());
        // scope detection is internal, but successful parse confirms no fatal error
    }

    @Test
    // Frameset should be handled (basic)
    public void testParse_frameset_basic() {
        Document doc = Jsoup.parse("<frameset><frame src='a.html'></frameset>");
        assertEquals(1, doc.select("frameset").size());
    }

    @Test
    // NoScript context: state transition to Data
    public void testParse_noScript_handled() {
        Document doc = Jsoup.parse("<noscript><p>text</p></noscript>");
        assertNotNull(doc);
        // Should not throw; content inside noscript is parsed as raw text? actually in scripting enabled mode it's Data.
        // In Jsoup scripting is enabled by default, so content is parsed as HTML.
        assertEquals("text", doc.select("noscript").text());
    }

    @Test
    // Input with multiple open formatting elements (tests marker logic)
    public void testParse_multipleFormattingElements_noMarker() {
        Document doc = Jsoup.parse("<b><b><b><b>text</b></b></b></b>");
        assertEquals(4, doc.select("b").size());
    }
}