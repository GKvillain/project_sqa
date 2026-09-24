package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Test;

import static org.junit.Assert.*;

public class HtmlTreeBuilderStateTest {

    // Tests anyOtherEndTag handling when tag case differs (Defects4J jsoup-62b target)
    @Test
    public void testInBody_caseInsensitiveEndTag_closesElement() {
        Parser parser = Parser.htmlParser().settings(ParseSettings.preserveCase);
        Document doc = parser.parseInput("<r><X>test</x></r>", "");
        assertEquals("<r><X>test</X></r>", doc.body().html());
        assertEquals(1, doc.body().select("X").size());
        assertEquals("test", doc.body().select("X").first().text());
    }

    // Tests initial state with doctype, comments and quirks mode
    @Test
    public void testInitial_doctypeAndComment_transitionsCorrectly() {
        String html = "<!-- comment --><!DOCTYPE html><html><head></head><body></body></html>";
        Document doc = Jsoup.parse(html);
        assertTrue(doc.outerHtml().contains("<!DOCTYPE html>"));
        assertEquals(Document.QuirksMode.noQuirks, doc.quirksMode());
    }

    // Tests BeforeHtml state handling comments, whitespace, and html tag
    @Test
    public void testBeforeHtml_whitespaceAndComment_ignoredAndInserted() {
        String html = "   <!-- before html -->\n<html><head></head><body><p>Test</p></body></html>";
        Document doc = Jsoup.parse(html);
        assertEquals("Test", doc.select("p").text());
        assertEquals(1, doc.childNodeSize()); // html node
    }

    // Tests BeforeHead and InHead states parsing title, meta, link, and scripts
    @Test
    public void testInHead_titleAndStyle_handlesRcDataAndRawtext() {
        String html = "<html><head><title>Sample Title</title><style>body { color: red; }</style></head><body></body></html>";
        Document doc = Jsoup.parse(html);
        assertEquals("Sample Title", doc.title());
        assertEquals(1, doc.head().select("style").size());
        assertEquals("body { color: red; }", doc.head().select("style").first().data());
    }

    // Tests InHead state with void elements like meta and base
    @Test
    public void testInHead_metaAndLink_insertedEmpty() {
        String html = "<html><head><meta charset=\"UTF-8\"><link rel=\"stylesheet\" href=\"style.css\"></head><body></body></html>";
        Document doc = Jsoup.parse(html);
        assertEquals("UTF-8", doc.head().select("meta").attr("charset"));
        assertEquals("style.css", doc.head().select("link").attr("href"));
    }

    // Tests AfterHead transition and automatic body creation
    @Test
    public void testAfterHead_bodyStartTag_transitionsToInBody() {
        String html = "<html><head></head><body class=\"main\"><p>Hello</p></body></html>";
        Document doc = Jsoup.parse(html);
        assertEquals("main", doc.body().className());
        assertEquals("Hello", doc.body().select("p").text());
    }

    // Tests InBody formatting elements reconstruction across paragraphs
    @Test
    public void testInBody_formattingElements_reconstructed() {
        String html = "<b>1<p>2</p>3</b>";
        Document doc = Jsoup.parse(html);
        assertEquals("<b>1</b><p><b>2</b></p><b>3</b>", doc.body().html());
    }

    // Tests Adoption Agency Algorithm in InBody
    @Test
    public void testInBody_adoptionAgencyAlgorithm_reordersElements() {
        String html = "<a>1<p>2</p>3</a>";
        Document doc = Jsoup.parse(html);
        assertEquals("<a>1</a><p><a>2</a></p><a>3</a>", doc.body().html());
    }

    // Tests InTable state for normal table structure
    @Test
    public void testInTable_tableStructure_handlesRowsAndCells() {
        String html = "<table><caption>Title</caption><colgroup><col></colgroup><thead><tr><th>Head</th></tr></thead><tbody><tr><td>Cell</td></tr></tbody></table>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("table").size());
        assertEquals("Title", doc.select("caption").text());
        assertEquals("Head", doc.select("th").text());
        assertEquals("Cell", doc.select("td").text());
    }

    // Tests InTable foster parenting when text appears directly in table
    @Test
    public void testInTable_fosterParenting_movesTextOutOfTable() {
        String html = "<table>foo<tr><td>bar</td></tr>baz</table>";
        Document doc = Jsoup.parse(html);
        assertEquals("foo\nbaz\n<table><tbody><tr><td>bar</td></tr></tbody></table>", doc.body().html());
    }

    // Tests InSelect and InSelectInTable states
    @Test
    public void testInSelect_optionsAndOptgroup_properlyHandled() {
        String html = "<select name=\"test\"><optgroup label=\"group1\"><option value=\"1\">One</option></optgroup><option value=\"2\">Two</option></select>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("select").size());
        assertEquals(1, doc.select("optgroup").size());
        assertEquals(2, doc.select("option").size());
        assertEquals("Two", doc.select("option").get(1).text());
    }

    // Tests InFrameset state
    @Test
    public void testInFrameset_framesetAndFrame_handled() {
        String html = "<html><frameset cols=\"50%,50%\"><frame src=\"frame1.html\"><frame src=\"frame2.html\"></frameset></html>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("frameset").size());
        assertEquals(2, doc.select("frame").size());
        assertEquals("frame1.html", doc.select("frame").first().attr("src"));
    }

    // Tests InBody nested headings and lists auto-closing
    @Test
    public void testInBody_headingsAndLists_implicitCloses() {
        String html = "<h1>Title 1<h2>Title 2</h2></h1><ul><li>Item 1<li>Item 2</ul>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("h1").size());
        assertEquals(1, doc.select("h2").size());
        assertEquals(2, doc.select("li").size());
    }

    // Tests unclosed paragraph tag before block element
    @Test
    public void testInBody_unclosedPWithBlockElement_closesP() {
        String html = "<p>First paragraph<div>Inside div</div>";
        Document doc = Jsoup.parse(html);
        assertEquals("<p>First paragraph</p>\n<div>\n Inside div\n</div>", doc.body().html());
    }

    // Tests form element handling in InBody and nested invalid form rejection
    @Test
    public void testInBody_formHandling_singleFormCreated() {
        String html = "<form id=\"f1\"><input type=\"text\"><form id=\"f2\"><input type=\"password\"></form></form>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("form").size());
        assertEquals("f1", doc.select("form").first().id());
        assertEquals(2, doc.select("input").size());
    }

    // Tests AfterBody and AfterAfterBody transitions with trailing comments
    @Test
    public void testAfterBody_commentsAndHtmlEnd_transitionsCorrectly() {
        String html = "<html><head></head><body>Hello</body><!-- end comment --></html><!-- outer comment -->";
        Document doc = Jsoup.parse(html);
        assertEquals("Hello", doc.body().text());
        assertTrue(doc.outerHtml().contains("end comment"));
        assertTrue(doc.outerHtml().contains("outer comment"));
    }

    // Tests InCell state handling self-closing tags and transitions to InRow
    @Test
    public void testInCell_misnestedCells_autoClosesCells() {
        String html = "<table><tr><td>1<td>2<tr><th>3<th>4</table>";
        Document doc = Jsoup.parse(html);
        assertEquals(2, doc.select("tr").size());
        assertEquals(2, doc.select("td").size());
        assertEquals(2, doc.select("th").size());
    }
}