package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Test;

import static org.junit.Assert.*;

public class TreeBuilderStateTest {

    // Tests parsing character zero does not drop token
    @Test
    public void testInBody_characterZero_parsedAsText() {
        Document doc = Jsoup.parse("<span>0</span>");
        Element span = doc.select("span").first();
        assertNotNull(span);
        assertEquals("0", span.text());
    }

    // Tests parsing character zero within various block elements
    @Test
    public void testInBody_zeroInParagraph_retainsZeroText() {
        Document doc = Jsoup.parse("<p>0</p><div>0</div>");
        assertEquals("0", doc.select("p").first().text());
        assertEquals("0", doc.select("div").first().text());
    }

    // Tests initial doctype and transition to BeforeHtml and BeforeHead
    @Test
    public void testInitial_doctypeHtml_createsDocument() {
        Document doc = Jsoup.parse("<!DOCTYPE html><html><head><title>Test</title></head><body>Hello</body></html>");
        assertEquals("Test", doc.title());
        assertEquals("Hello", doc.body().text());
    }

    // Tests comments in head and body states
    @Test
    public void testInHead_comments_preservedInTree() {
        Document doc = Jsoup.parse("<html><head><!-- head comment --><title>Test</title></head><body><!-- body comment --></body></html>");
        assertEquals("Test", doc.title());
        assertTrue(doc.head().html().contains("<!--head comment-->"));
    }

    // Tests InHead state with meta, base, and style tags
    @Test
    public void testInHead_headTags_parsedCorrectly() {
        Document doc = Jsoup.parse("<html><head><meta charset=\"utf-8\"><base href=\"http://example.com/\"><style>body{color:red;}</style></head><body></body></html>");
        assertEquals("http://example.com/", doc.baseUri());
        assertEquals(1, doc.select("meta").size());
        assertEquals(1, doc.select("style").size());
    }

    // Tests InHeadNoscript state transitions
    @Test
    public void testInHeadNoscript_noscriptTag_parsedInHead() {
        Document doc = Jsoup.parse("<html><head><noscript><link rel=\"stylesheet\" href=\"style.css\"></noscript></head><body></body></html>");
        assertNotNull(doc.select("noscript").first());
        assertEquals(1, doc.select("link").size());
    }

    // Tests InBody formatting adoption agency algorithm
    @Test
    public void testInBody_unclosedFormattingElements_reconstructed() {
        Document doc = Jsoup.parse("<p><b><i>One</b>Two</i></p>");
        assertEquals("<p><b><i>One</i></b><i>Two</i></p>", doc.body().html());
    }

    // Tests InBody button scope handling
    @Test
    public void testInBody_buttonScope_closesCorrectly() {
        Document doc = Jsoup.parse("<button><p>Inside</p></button>");
        assertNotNull(doc.select("button p").first());
    }

    // Tests InBody list item auto-closing
    @Test
    public void testInBody_listItems_autoClose() {
        Document doc = Jsoup.parse("<ul><li>One<li>Two<li>Three</ul>");
        assertEquals(3, doc.select("li").size());
    }

    // Tests InBody definition list dt and dd tags
    @Test
    public void testInBody_definitionList_autoClosesDtDd() {
        Document doc = Jsoup.parse("<dl><dt>Term<dd>Definition<dt>Term 2<dd>Definition 2</dl>");
        assertEquals(2, doc.select("dt").size());
        assertEquals(2, doc.select("dd").size());
    }

    // Tests InBody form element handling
    @Test
    public void testInBody_formTags_nestedFormsIgnored() {
        Document doc = Jsoup.parse("<form id=1><input name=a><form id=2><input name=b></form></form>");
        assertEquals(1, doc.select("form").size());
        assertEquals(2, doc.select("input").size());
    }

    // Tests InTable, InTableBody, InRow, InCell state transitions
    @Test
    public void testInTable_tableStructure_parsedCorrectly() {
        Document doc = Jsoup.parse("<table><caption>Cap</caption><colgroup><col></colgroup><thead><tr><th>H</th></tr></thead><tbody><tr><td>D</td></tr></tbody></table>");
        assertEquals("Cap", doc.select("caption").first().text());
        assertEquals("H", doc.select("th").first().text());
        assertEquals("D", doc.select("td").first().text());
    }

    // Tests foster parenting when text or elements appear misplaced in table
    @Test
    public void testInTable_fosterParenting_misplacedContentMovedOutsideTable() {
        Document doc = Jsoup.parse("<table>Foo<b>Bar</b><tr><td>Cell</td></tr></table>");
        assertTrue(doc.body().html().startsWith("Foo<b>Bar</b>"));
        assertNotNull(doc.select("table td").first());
    }

    // Tests InSelect and InSelectInTable states
    @Test
    public void testInSelect_optionsAndOptgroups_parsedCorrectly() {
        Document doc = Jsoup.parse("<select><optgroup label=\"g1\"><option>1<option>2</optgroup><option>3</select>");
        assertEquals(1, doc.select("optgroup").size());
        assertEquals(3, doc.select("option").size());
    }

    // Tests InFrameset, AfterFrameset, and AfterAfterFrameset states
    @Test
    public void testInFrameset_frameAndNoframes_parsedCorrectly() {
        Document doc = Jsoup.parse("<html><frameset><frame src=\"frame.html\"><noframes><body>No frames</body></noframes></frameset></html>");
        assertEquals(1, doc.select("frameset").size());
        assertEquals(1, doc.select("frame").size());
    }

    // Tests Text state for script and textarea data
    @Test
    public void testText_scriptAndTextarea_dataHandledAsRaw() {
        Document doc = Jsoup.parse("<textarea><p>NotAParagraph</p></textarea><script>var a = '<p>';</script>");
        assertEquals("<p>NotAParagraph</p>", doc.select("textarea").first().text());
        assertTrue(doc.select("script").first().data().contains("var a = '<p>';"));
    }

    // Tests AfterBody and AfterAfterBody transitions with trailing characters and comments
    @Test
    public void testAfterBody_trailingContent_handledGracefully() {
        Document doc = Jsoup.parse("<html><head></head><body>Content</body></html><!-- trailing comment -->");
        assertEquals("Content", doc.body().text());
        assertTrue(doc.html().contains("<!--trailing comment-->"));
    }
}