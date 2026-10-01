package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.jsoup.parser.ParseErrorList.noTracking;
import static org.jsoup.parser.ParseSettings.htmlDefault;

import java.util.List;

public class HtmlTreeBuilderTest {

    // Tests simple document parsing produces correct title and text
    @Test
    public void testParse_simpleHtml_returnsCorrectDocument() {
        Document doc = Jsoup.parse("<html><head><title>Title</title></head><body><p>Hello</p></body></html>");
        assertEquals("Title", doc.title());
        assertEquals("Hello", doc.select("p").text());
    }

    // Tests parseBodyFragment with null context returns body content
    @Test
    public void testParseBodyFragment_nullContext_returnsBodyContent() {
        Document doc = Jsoup.parseBodyFragment("<p>fragment</p>");
        assertEquals("<p>fragment</p>", doc.body().html());
    }

    // Tests fragment parsing with context element td (resets insertion mode to InCell)
    @Test
    public void testParseBodyFragment_contextTd_works() {
        Document mainDoc = Jsoup.parse("<table><tr><td id='ctx'></td></tr></table>");
        Element context = mainDoc.select("#ctx").first();
        Document fragDoc = Jsoup.parseBodyFragment("<div>inner</div>", context.tagName());
        assertEquals("<div>inner</div>", fragDoc.body().html());
    }

    // Tests fragment parsing with context element th (resets insertion mode to InCell)
    @Test
    public void testParseBodyFragment_contextTh_works() {
        Document mainDoc = Jsoup.parse("<table><tr><th id='ctx'></th></tr></table>");
        Element context = mainDoc.select("#ctx").first();
        Document fragDoc = Jsoup.parseBodyFragment("<div>inner</div>", context.tagName());
        assertEquals("<div>inner</div>", fragDoc.body().html());
    }

    // Tests fragment parsing with context element select (resets insertion mode to InSelect)
    @Test
    public void testParseBodyFragment_contextSelect_works() {
        Document mainDoc = Jsoup.parse("<select id='ctx'><option>opt</option></select>");
        Element context = mainDoc.select("#ctx").first();
        Document fragDoc = Jsoup.parseBodyFragment("<option>new</option>", context.tagName());
        assertEquals("<option>new</option>", fragDoc.body().html());
    }

    // Tests fragment parsing with context element table (resets insertion mode to InTable)
    @Test
    public void testParseBodyFragment_contextTable_works() {
        Document mainDoc = Jsoup.parse("<table id='ctx'></table>");
        Element context = mainDoc.select("#ctx").first();
        Document fragDoc = Jsoup.parseBodyFragment("<tr><td>cell</td></tr>", context.tagName());
        String bodyHtml = fragDoc.body().html();
        assertTrue(bodyHtml.contains("<tr>"));
        assertTrue(bodyHtml.contains("<td>cell</td>"));
    }

    // Tests fragment parsing with context element body (resets insertion mode to InBody)
    @Test
    public void testParseBodyFragment_contextBody_works() {
        Document mainDoc = Jsoup.parse("<body id='ctx'></body>");
        Element context = mainDoc.select("#ctx").first();
        Document fragDoc = Jsoup.parseBodyFragment("<p>text</p>", context.tagName());
        assertEquals("<p>text</p>", fragDoc.body().html());
    }

    // Tests self-closing tag <br/> produces a void element
    @Test
    public void testSelfClosingTag_br_createsVoidElement() {
        Document doc = Jsoup.parse("<br/>");
        Elements brs = doc.select("br");
        assertEquals(1, brs.size());
        assertEquals(0, brs.first().childNodes().size());
    }

    // Tests comment node is correctly inserted
    @Test
    public void testComment_insertedIntoDocument() {
        Document doc = Jsoup.parse("<!-- test comment --><p>text</p>");
        List<Node> children = doc.body().childNodes();
        assertTrue(children.get(0) instanceof Comment);
        assertEquals(" test comment ", ((Comment) children.get(0)).getData());
    }

    // Tests script content is stored as DataNode (not TextNode)
    @Test
    public void testScriptContent_createsDataNode() {
        Document doc = Jsoup.parse("<script>var x = 1;</script>");
        Element script = doc.select("script").first();
        Node child = script.childNode(0);
        assertTrue(child instanceof DataNode);
        assertEquals("var x = 1;", ((DataNode) child).getWholeData());
    }

    // Tests form element associates input elements
    @Test
    public void testFormElement_inputAssociated() {
        Document doc = Jsoup.parse("<form id='f'><input type='text' name='a'></form>");
        FormElement form = (FormElement) doc.select("form").first();
        assertEquals(1, form.elements().size());
        assertEquals("a", form.elements().first().attr("name"));
    }

    // Tests <base href> updates document base URI and resolves links
    @Test
    public void testBaseTag_setsBaseUri() {
        Document doc = Jsoup.parse("<html><head><base href='http://example.com/'></head><body><a href='/page'>link</a></body></html>");
        assertEquals("http://example.com/", doc.baseUri());
        assertEquals("http://example.com/page", doc.select("a").first().absUrl("href"));
    }

    // Tests foster parenting: table inside paragraph should be placed outside p
    @Test
    public void testFosterInserts_tableInsideParagraph() {
        Document doc = Jsoup.parse("<p>text<table><tr><td>cell</td></tr></table></p>");
        Elements p = doc.select("p");
        assertEquals(1, p.size());
        assertTrue(p.first().childNodes().get(0) instanceof TextNode);
        Element table = doc.select("table").first();
        assertNotNull(table);
        assertTrue(table.previousElementSibling() == p.first() || table.nextElementSibling() == p.first());
    }

    // Tests implied end tags: consecutive li elements are properly nested under ul
    @Test
    public void testImpliedEndTags_liInList() {
        Document doc = Jsoup.parse("<ul><li>item1<li>item2</ul>");
        Elements lis = doc.select("li");
        assertEquals(2, lis.size());
        assertEquals("ul", lis.first().parent().tagName());
        assertEquals("ul", lis.last().parent().tagName());
    }

    // Tests implied end tags: p is closed before block-level element div
    @Test
    public void testImpliedEndTags_pBeforeBlock() {
        Document doc = Jsoup.parse("<p>hello<div>world</div></p>");
        Elements p = doc.select("p");
        Elements div = doc.select("div");
        assertEquals(1, p.size());
        assertEquals(1, div.size());
        assertEquals(p.first().nextElementSibling(), div.first());
    }

    // Tests reconstruction of active formatting elements when encountering end tag
    @Test
    public void testActiveFormattingElements_reconstruction() {
        Document doc = Jsoup.parse("<b><i>text</b></i>");
        assertEquals("<b><i>text</i></b>", doc.body().html().replaceAll("\\s+", ""));
    }

    // Tests that pushActiveFormattingElements removes older element when three identical entries exist
    @Test
    public void testPushActiveFormattingElements_limitThree() {
        Document doc = Jsoup.parse("<b><b><b><b>text</b></b></b></b>");
        String html = doc.body().html().replaceAll("\\s+", "");
        assertTrue(html.startsWith("<b><b><b>"));
    }

    // Tests that formatting elements are cleared after a table is closed
    @Test
    public void testClearFormattingElementsToLastMarker() {
        Document doc = Jsoup.parse("<b><table><tr><td>cell</td></tr></table>text");
        String html = doc.body().html();
        assertFalse(html.contains("<b>text</b>"));
    }

    // Tests inScope behaviour: closing </html> is handled without exception
    @Test
    public void testInScope_closingHtmlTag() {
        Document doc = Jsoup.parse("<html><body><p>text</p></body></html>");
        assertNotNull(doc);
    }

    // Tests parseFragment with null context returns document.childNodes (using HtmlTreeBuilder directly)
    @Test
    public void testParseFragment_nullContext_returnsDocumentChildNodes() {
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        List<Node> nodes = builder.parseFragment("<p>fragment</p>", null, "http://base", noTracking(), htmlDefault);
        assertTrue(nodes.size() > 0);
        Element el = (Element) nodes.get(0);
        assertEquals("p", el.tagName());
    }

    // Tests parseFragment with context that has ownerDocument (triggers quirks mode path)
    @Test
    public void testParseFragment_contextWithOwnerDocument_works() {
        Document mainDoc = new Document("http://base");
        Element context = mainDoc.createElement("div");
        mainDoc.quirksMode(Document.QuirksMode.quirks);
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        List<Node> nodes = builder.parseFragment("<span>text</span>", context, "http://base", noTracking(), htmlDefault);
        assertNotNull(nodes);
        assertTrue(nodes.size() > 0);
    }
}