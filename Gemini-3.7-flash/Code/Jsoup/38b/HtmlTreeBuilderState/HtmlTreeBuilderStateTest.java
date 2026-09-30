package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;

import static org.junit.Assert.*;

public class HtmlTreeBuilderStateTest {

    // Tests defect where image tag inside svg should not be alias-converted to img
    @Test
    public void testInBody_svgImage_retainsImageTag() {
        String html = "<svg><image href=\"foo.png\" /></svg>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.select("svg image").first());
        assertEquals(0, doc.select("img").size());
    }

    // Tests normal case where image tag outside svg is converted to img tag
    @Test
    public void testInBody_imageOutsideSvg_convertsToImgTag() {
        String html = "<p><image src=\"foo.png\"></p>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.select("img").first());
        assertEquals("foo.png", doc.select("img").first().attr("src"));
        assertEquals(0, doc.select("image").size());
    }

    // Tests Initial state doctype handling and quirks mode
    @Test
    public void testInitial_doctype_setsDoctypeAndQuirksMode() {
        String html = "<!DOCTYPE html><html><head></head><body></body></html>";
        Document doc = Jsoup.parse(html);
        assertEquals(Document.QuirksMode.noQuirks, doc.quirksMode());
        assertEquals("html", doc.childNode(0).nodeName());
    }

    // Tests BeforeHtml and BeforeHead states with comments and whitespace
    @Test
    public void testBeforeHtml_commentsAndWhitespace_preserved() {
        String html = "  <!-- comment before html --><html><head><!-- comment in head --></head><body></body></html>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.head());
        assertNotNull(doc.body());
        assertTrue(doc.html().contains("comment before html"));
        assertTrue(doc.html().contains("comment in head"));
    }

    // Tests InHead state handling title, base, and style
    @Test
    public void testInHead_titleAndMetaAndStyle_handledCorrectly() {
        String html = "<html><head><title>Test Title</title><meta charset=\"utf-8\"><style>body { color: red; }</style></head><body></body></html>";
        Document doc = Jsoup.parse(html);
        assertEquals("Test Title", doc.title());
        assertEquals("utf-8", doc.select("meta").first().attr("charset"));
        assertEquals("body { color: red; }", doc.select("style").first().data());
    }

    // Tests InHeadNoscript state transitions
    @Test
    public void testInHeadNoscript_nestedElements_parsedCorrectly() {
        String html = "<head><noscript><link rel=\"stylesheet\" href=\"style.css\"></noscript></head><body>Hello</body>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.select("head noscript link").first());
        assertEquals("Hello", doc.body().text());
    }

    // Tests AfterHead state transitioning to InBody on normal body tag
    @Test
    public void testAfterHead_bodyTag_transitionsToInBody() {
        String html = "<head></head><body class=\"main\"><p>Text</p></body>";
        Document doc = Jsoup.parse(html);
        assertEquals("main", doc.body().className());
        assertEquals("Text", doc.select("p").first().text());
    }

    // Tests InBody state p closers and heading tags
    @Test
    public void testInBody_headingsAndParagraphClosers_closesP() {
        String html = "<body><p>Para 1<h1>Heading 1</h1><p>Para 2<div>Div content</div></body>";
        Document doc = Jsoup.parse(html);
        assertEquals(2, doc.select("p").size());
        assertEquals("Para 1", doc.select("p").get(0).text());
        assertEquals("Heading 1", doc.select("h1").first().text());
        assertEquals("Para 2", doc.select("p").get(1).text());
        assertEquals("Div content", doc.select("div").first().text());
    }

    // Tests InBody Adoption Agency Algorithm with nested formatting tags
    @Test
    public void testInBody_adoptionAgencyAlgorithm_formatsProperly() {
        String html = "<p><b>Bold <i>and italic</b> only italic</i></p>";
        Document doc = Jsoup.parse(html);
        Elements bTags = doc.select("b");
        Elements iTags = doc.select("i");
        assertEquals(1, bTags.size());
        assertEquals("Bold and italic", bTags.text());
        assertEquals(2, iTags.size());
    }

    // Tests InBody isindex tag conversion into form and input elements
    @Test
    public void testInBody_isindex_transformsToFormAndInput() {
        String html = "<body><isindex action=\"/search\" prompt=\"Search: \"></body>";
        Document doc = Jsoup.parse(html);
        Element form = doc.select("form").first();
        assertNotNull(form);
        assertEquals("/search", form.attr("action"));
        Element input = doc.select("input").first();
        assertNotNull(input);
        assertEquals("isindex", input.attr("name"));
    }

    // Tests InTable state foster parenting when non-table content is in table
    @Test
    public void testInTable_fosterParenting_insertsContentBeforeTable() {
        String html = "<table>Text Outside<tr><td>Cell</td></tr></table>";
        Document doc = Jsoup.parse(html);
        assertEquals("Text Outside", doc.body().textNodes().get(0).text());
        assertEquals("Cell", doc.select("td").first().text());
    }

    // Tests InTableText state character handling
    @Test
    public void testInTableText_whitespaceAndData_handlesAppropriately() {
        String html = "<table>   <tr><td>Content</td></tr></table>";
        Document doc = Jsoup.parse(html);
        assertEquals("Content", doc.select("td").first().text());
    }

    // Tests InSelect state option and optgroup parsing
    @Test
    public void testInSelect_optionsAndOptgroup_parsedCorrectly() {
        String html = "<select><optgroup label=\"group\"><option>1<option>2</optgroup><option>3</select>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("optgroup").size());
        assertEquals(3, doc.select("option").size());
        assertEquals("1", doc.select("option").get(0).text());
    }

    // Tests InSelectInTable state when select is enclosed inside table cell
    @Test
    public void testInSelectInTable_selectInsideTable_transitionsCorrectly() {
        String html = "<table><tr><td><select><option>Option 1</option></td><td>Next Cell</td></tr></table>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("select").size());
        assertEquals(2, doc.select("td").size());
        assertEquals("Next Cell", doc.select("td").get(1).text());
    }

    // Tests InFrameset and AfterFrameset state parsing
    @Test
    public void testInFrameset_framesetWithFrames_parsedCorrectly() {
        String html = "<html><frameset rows=\"50%,50%\"><frame src=\"top.html\"><frame src=\"bottom.html\"><noframes><p>No frames</p></noframes></frameset></html>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("frameset").size());
        assertEquals(2, doc.select("frame").size());
        assertEquals("top.html", doc.select("frame").first().attr("src"));
    }

    // Tests Text state parsing raw text elements (textarea, xmp, iframe)
    @Test
    public void testText_rawtextAndRcdata_retainsRawData() {
        String html = "<textarea><b>Not Bold</b></textarea><xmp><i>Raw</i></xmp>";
        Document doc = Jsoup.parse(html);
        assertEquals("<b>Not Bold</b>", doc.select("textarea").first().text());
        assertEquals("<i>Raw</i>", doc.select("xmp").first().text());
    }

    // Tests InCaption, InColumnGroup, InTableBody, InRow, and InCell states
    @Test
    public void testTableSubStates_captionColgroupTheadTbodyTfoot() {
        String html = "<table>" +
                "<caption>Table Caption</caption>" +
                "<colgroup><col class=\"col1\"><col class=\"col2\"></colgroup>" +
                "<thead><tr><th>H1</th><th>H2</th></tr></thead>" +
                "<tbody><tr><td>D1</td><td>D2</td></tr></tbody>" +
                "<tfoot><tr><td>F1</td><td>F2</td></tr></tfoot>" +
                "</table>";
        Document doc = Jsoup.parse(html);
        assertEquals("Table Caption", doc.select("caption").text());
        assertEquals(2, doc.select("col").size());
        assertEquals("H1", doc.select("th").first().text());
        assertEquals("D1", doc.select("tbody td").first().text());
        assertEquals("F1", doc.select("tfoot td").first().text());
    }

    // Tests InTable hidden input and form handling
    @Test
    public void testInTable_hiddenInputAndForm() {
        String html = "<table><input type=\"hidden\" name=\"token\" value=\"123\">" +
                "<form action=\"/submit\"><tr><td>Cell</td></tr></form></table>";
        Document doc = Jsoup.parse(html);
        Element input = doc.select("table > input").first();
        assertNotNull(input);
        assertEquals("123", input.val());
        assertNotNull(doc.select("form").first());
    }

    // Tests InTable foster parenting with pending characters in InTableText
    @Test
    public void testInTableText_mixedCharacters_fosterParented() {
        String html = "<table>   foo <tr><td>Bar</td></tr></table>";
        Document doc = Jsoup.parse(html);
        assertTrue(doc.body().text().startsWith("foo"));
        assertEquals("Bar", doc.select("td").first().text());
    }

    // Tests InBody list elements auto-closing (li, dt, dd)
    @Test
    public void testInBody_listElements_autoClose() {
        String html = "<ul><li>Item 1<li>Item 2</ul><dl><dt>Term 1<dd>Desc 1<dt>Term 2<dd>Desc 2</dl>";
        Document doc = Jsoup.parse(html);
        assertEquals(2, doc.select("li").size());
        assertEquals("Item 1", doc.select("li").get(0).text());
        assertEquals("Item 2", doc.select("li").get(1).text());
        assertEquals(2, doc.select("dt").size());
        assertEquals(2, doc.select("dd").size());
    }

    // Tests nested anchor tags self-closing behavior in InBody
    @Test
    public void testInBody_nestedAnchors_closedProperly() {
        String html = "<a href=\"1\">Link 1 <a href=\"2\">Link 2</a></a>";
        Document doc = Jsoup.parse(html);
        Elements links = doc.select("a");
        assertEquals(2, links.size());
        assertEquals("Link 1 ", links.get(0).text());
        assertEquals("Link 2", links.get(1).text());
    }

    // Tests InBody buttons closing paragraph tags
    @Test
    public void testInBody_buttonWithP_handledCorrectly() {
        String html = "<p>Paragraph<button>Click</button>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("p").size());
        assertEquals("Paragraph", doc.select("p").first().text());
        assertEquals("Click", doc.select("button").first().text());
    }

    // Tests AfterBody and AfterAfterBody comments and content handling
    @Test
    public void testAfterBody_commentsAndTrailingContent() {
        String html = "<html><head></head><body>Content</body></html><!-- comment after html -->";
        Document doc = Jsoup.parse(html);
        assertEquals("Content", doc.body().text());
        assertTrue(doc.outerHtml().contains("comment after html"));
    }

    // Tests quirks mode detection for legacy doctypes
    @Test
    public void testInitial_quirksModeDoctypes() {
        String html = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Frameset//\"><html><body>Quirks</body></html>";
        Document doc = Jsoup.parse(html);
        assertEquals(Document.QuirksMode.quirks, doc.quirksMode());
    }

    // Tests InForeignContent MathML elements parsing
    @Test
    public void testInForeignContent_mathML() {
        String html = "<math><mi>x</mi><mo>+</mo><mn>1</mn></math>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.select("math").first());
        assertEquals("x", doc.select("mi").first().text());
        assertEquals("+", doc.select("mo").first().text());
        assertEquals("1", doc.select("mn").first().text());
    }

    // Tests nested frameset in InFrameset and AfterFrameset
    @Test
    public void testInFrameset_nestedFramesets() {
        String html = "<html><frameset rows=\"*\"><frameset cols=\"50,50\"><frame src=\"1.html\"><frame src=\"2.html\"></frameset></frameset></html>";
        Document doc = Jsoup.parse(html);
        assertEquals(2, doc.select("frameset").size());
        assertEquals(2, doc.select("frame").size());
    }

    // Tests InHead script, base, and noframes handling
    @Test
    public void testInHead_baseAndScript() {
        String html = "<html><head><base href=\"http://example.com/\"><script>var x = 1;</script><noframes>No frames</noframes></head><body></body></html>";
        Document doc = Jsoup.parse(html);
        assertEquals("http://example.com/", doc.baseUri());
        assertEquals("var x = 1;", doc.select("script").first().data());
    }
}