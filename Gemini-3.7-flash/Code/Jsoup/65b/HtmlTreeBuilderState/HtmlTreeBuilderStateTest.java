package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.DocumentType;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.junit.Test;

import java.io.StringReader;

import static org.junit.Assert.*;

public class HtmlTreeBuilderStateTest {

    // Tests initial state doctype parsing and force quirks handling
    @Test
    public void testInitial_doctype_createsDocumentType() {
        Document doc = Jsoup.parse("<!DOCTYPE html><html><head></head><body></body></html>");
        DocumentType doctype = null;
        for (Node node : doc.childNodes()) {
            if (node instanceof DocumentType) {
                doctype = (DocumentType) node;
                break;
            }
        }
        assertNotNull(doctype);
        assertEquals("html", doctype.attr("name"));
        assertEquals(Document.QuirksMode.noQuirks, doc.quirksMode());
    }

    // Tests beforeHtml state handling missing html tag and comments
    @Test
    public void testBeforeHtml_missingHtmlTag_autoInsertsHtml() {
        Document doc = Jsoup.parse("<!-- comment --><div>Hello</div>");
        assertEquals("html", doc.child(0).nodeName());
        assertEquals("Hello", doc.body().text());
        assertEquals(1, doc.head().children().size() + doc.body().children().size());
    }

    // Tests beforeHead state automatically creating head when missing
    @Test
    public void testBeforeHead_missingHeadTag_autoInsertsHead() {
        Document doc = Jsoup.parse("<html><body>Content</body></html>");
        assertNotNull(doc.head());
        assertNotNull(doc.body());
        assertEquals("Content", doc.body().text());
    }

    // Tests inHead state for meta, base, title and style tags
    @Test
    public void testInHead_headTags_parsedIntoHead() {
        Document doc = Jsoup.parse("<html><head><title>Test Title</title><meta charset=\"utf-8\"><base href=\"http://example.com/\"><style>body{color:red;}</style></head><body></body></html>");
        assertEquals("Test Title", doc.title());
        assertEquals("http://example.com/", doc.baseUri());
        assertEquals(1, doc.head().getElementsByTag("meta").size());
        assertEquals(1, doc.head().getElementsByTag("style").size());
    }

    // Tests inHead state noscript tag handling
    @Test
    public void testInHead_noscriptTag_parsedCorrectly() {
        Document doc = Jsoup.parse("<html><head><noscript><link rel=\"stylesheet\" href=\"style.css\"></noscript></head><body></body></html>");
        Element noscript = doc.head().getElementsByTag("noscript").first();
        assertNotNull(noscript);
        assertEquals(1, noscript.children().size());
    }

    // Tests afterHead state encountering body or frameset
    @Test
    public void testAfterHead_bodyTag_transitionsToInBody() {
        Document doc = Jsoup.parse("<html><head></head><body class=\"main\"><p>Text</p></body></html>");
        assertEquals("main", doc.body().className());
        assertEquals("Text", doc.body().getElementsByTag("p").text());
    }

    // Tests inBody state adoption agency algorithm for unclosed formatting tags
    @Test
    public void testInBody_formattingAdoptionAgency_restructuresCorrectly() {
        Document doc = Jsoup.parse("<b>1<p>2</b>3</p>");
        assertEquals("<b>1</b><p><b>2</b>3</p>", doc.body().html().replaceAll("\\r?\\n", ""));
    }

    // Tests inBody state auto-closing li tags
    @Test
    public void testInBody_nestedLi_autoClosesPreviousLi() {
        Document doc = Jsoup.parse("<ul><li>Item 1<li>Item 2</ul>");
        assertEquals(2, doc.body().getElementsByTag("li").size());
        assertEquals("Item 1", doc.body().getElementsByTag("li").get(0).text());
        assertEquals("Item 2", doc.body().getElementsByTag("li").get(1).text());
    }

    // Tests inBody state ignoring nested form tags
    @Test
    public void testInBody_nestedForm_ignoresInnerForm() {
        Document doc = Jsoup.parse("<form id=\"outer\"><input name=\"one\"><form id=\"inner\"><input name=\"two\"></form></form>");
        assertEquals(1, doc.body().getElementsByTag("form").size());
        assertEquals("outer", doc.body().getElementsByTag("form").first().id());
        assertEquals(2, doc.body().getElementsByTag("input").size());
    }

    // Tests inBody state converting image tag to img
    @Test
    public void testInBody_imageTag_convertsToImg() {
        Document doc = Jsoup.parse("<image src=\"test.png\">");
        assertEquals(1, doc.body().getElementsByTag("img").size());
        assertEquals(0, doc.body().getElementsByTag("image").size());
    }

    // Tests inTable state foster parenting character data and misplaced tags
    @Test
    public void testInTable_fosterParenting_movesContentOutOfTable() {
        Document doc = Jsoup.parse("<table>Misplaced Text<tr><td>Cell</td></tr></table>");
        assertEquals("Misplaced Text", doc.body().textNodes().get(0).text().trim());
        assertEquals(1, doc.body().getElementsByTag("table").size());
        assertEquals("Cell", doc.body().getElementsByTag("td").text());
    }

    // Tests inTableBody state auto-inserting tr for th/td and exitTableBody handling
    @Test
    public void testInTableBody_missingTr_autoInsertsTr() {
        Document doc = Jsoup.parse("<table><tbody><td>Cell 1</td><td>Cell 2</td></tbody></table>");
        Element tbody = doc.body().getElementsByTag("tbody").first();
        assertNotNull(tbody);
        assertEquals(1, tbody.getElementsByTag("tr").size());
        assertEquals(2, tbody.getElementsByTag("td").size());
    }

    // Tests inRow state and inCell state closing tags properly
    @Test
    public void testInRow_tableRowAndCells_properlyClosed() {
        Document doc = Jsoup.parse("<table><tr><th>Header 1<th>Header 2<tr><td>Data 1<td>Data 2</table>");
        assertEquals(2, doc.body().getElementsByTag("tr").size());
        assertEquals(2, doc.body().getElementsByTag("th").size());
        assertEquals(2, doc.body().getElementsByTag("td").size());
    }

    // Tests inTable caption, colgroup and col parsing
    @Test
    public void testInTable_captionAndColgroup_parsedCorrectly() {
        Document doc = Jsoup.parse("<table><caption>Table Caption</caption><colgroup><col></colgroup><tbody><tr><td>Content</td></tr></tbody></table>");
        assertEquals(1, doc.body().getElementsByTag("caption").size());
        assertEquals("Table Caption", doc.body().getElementsByTag("caption").text());
        assertEquals(1, doc.body().getElementsByTag("colgroup").size());
        assertEquals(1, doc.body().getElementsByTag("col").size());
    }

    // Tests inSelect state with optgroup and options
    @Test
    public void testInSelect_optgroupAndOptions_structuredCorrectly() {
        Document doc = Jsoup.parse("<select><optgroup label=\"G1\"><option>O1<option>O2</optgroup><option>O3</select>");
        Element select = doc.body().getElementsByTag("select").first();
        assertNotNull(select);
        assertEquals(1, select.getElementsByTag("optgroup").size());
        assertEquals(3, select.getElementsByTag("option").size());
    }

    // Tests inSelectInTable state when table tags appear inside select
    @Test
    public void testInSelectInTable_tableTagInsideSelect_closesSelect() {
        Document doc = Jsoup.parse("<table><tr><td><select><option>1</option></td><td>2</td></tr></table>");
        assertEquals(1, doc.body().getElementsByTag("select").size());
        assertEquals(2, doc.body().getElementsByTag("td").size());
    }

    // Tests inFrameset, frame and noframes parsing
    @Test
    public void testInFrameset_framesetAndFrame_parsedCorrectly() {
        Document doc = Jsoup.parse("<html><frameset rows=\"50%,50%\"><frame src=\"frame1.html\"><frame src=\"frame2.html\"><noframes><p>No frames</p></noframes></frameset></html>");
        assertEquals(1, doc.getElementsByTag("frameset").size());
        assertEquals(2, doc.getElementsByTag("frame").size());
        assertEquals(1, doc.getElementsByTag("noframes").size());
    }

    // Tests afterBody state and trailing content handling
    @Test
    public void testAfterBody_trailingContent_appendsToBody() {
        Document doc = Jsoup.parse("<html><head></head><body>Content</body><!-- after body --><div>Trailing</div></html>");
        assertEquals("Content Trailing", doc.body().text());
        assertEquals(1, doc.body().getElementsByTag("div").size());
    }

    // Tests foreignContent state processing
    @Test
    public void testForeignContent_directProcess_returnsTrue() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse(new StringReader("<div></div>"), "", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        boolean result = HtmlTreeBuilderState.ForeignContent.process(new Token.Character().data("test"), tb);
        assertTrue(result);
    }
}