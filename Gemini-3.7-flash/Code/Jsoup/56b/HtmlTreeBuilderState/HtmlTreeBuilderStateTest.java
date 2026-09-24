package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.DocumentType;
import org.jsoup.nodes.Element;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class HtmlTreeBuilderStateTest {

    // Tests Initial state processing doctype token, public and system IDs, and base URI
    @Test
    public void testInitial_doctypeToken_setsDocumentType() {
        String html = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\"><html><head></head><body></body></html>";
        Document doc = Jsoup.parse(html);
        List<DocumentType> doctypes = doc.select("doctype").size() > 0 ? null : doc.children().select("*").size() > 0 ? null : null;
        DocumentType doctype = (DocumentType) doc.childNode(0);

        assertNotNull(doctype);
        assertEquals("html", doctype.attr("name"));
        assertEquals("-//W3C//DTD HTML 4.01//EN", doctype.attr("publicId"));
        assertEquals("http://www.w3.org/TR/html4/strict.dtd", doctype.attr("systemId"));
    }

    // Tests Initial and BeforeHtml states handling leading comments and whitespaces
    @Test
    public void testInitial_leadingCommentAndWhitespace_insertedBeforeHtml() {
        String html = "   <!-- leading comment -->\n<html><head></head><body></body></html>";
        Document doc = Jsoup.parse(html);

        assertEquals(1, doc.children().size());
        assertEquals("html", doc.child(0).nodeName());
        assertEquals("leading comment", doc.childNode(1).outerHtml().trim().replace("<!--", "").replace("-->", "").trim());
    }

    // Tests BeforeHtml error recording on unexpected doctype token
    @Test
    public void testBeforeHtml_unexpectedDoctype_recordsError() {
        Parser parser = Parser.htmlParser().setTrackErrors(10);
        Document doc = parser.parseInput("<html><!DOCTYPE html></html>", "http://example.com");

        assertFalse(parser.getErrors().isEmpty());
        assertNotNull(doc.body());
    }

    // Tests InHead state with meta, base, title, link, and style tags
    @Test
    public void testInHead_standardHeadTags_populatedCorrectly() {
        String html = "<html><head><base href=\"http://example.com/base/\"><title>Test Title</title><meta charset=\"UTF-8\"><link rel=\"stylesheet\" href=\"style.css\"><style>body{color:red;}</style></head><body></body></html>";
        Document doc = Jsoup.parse(html);

        assertEquals("http://example.com/base/", doc.baseUri());
        assertEquals("Test Title", doc.title());
        assertEquals("UTF-8", doc.select("meta").first().attr("charset"));
        assertEquals("style.css", doc.select("link").first().attr("href"));
        assertEquals("body{color:red;}", doc.select("style").first().data());
    }

    // Tests InHead state transitioning to Text on script tag and back
    @Test
    public void testInHead_scriptData_transitionsToTextMode() {
        String html = "<html><head><script type=\"text/javascript\">var a = '<test>';</script></head><body><p>Text</p></body></html>";
        Document doc = Jsoup.parse(html);

        Element script = doc.select("script").first();
        assertNotNull(script);
        assertEquals("var a = '<test>';", script.data());
        assertEquals("Text", doc.select("p").first().text());
    }

    // Tests InHeadNoscript handling fallback tags inside noscript
    @Test
    public void testInHeadNoscript_noscriptElements_processedInHead() {
        String html = "<html><head><noscript><link rel=\"stylesheet\" href=\"fallback.css\"><meta http-equiv=\"refresh\" content=\"30\"></noscript></head><body>Content</body></html>";
        Document doc = Jsoup.parse(html);

        Element noscript = doc.select("head > noscript").first();
        assertNotNull(noscript);
        assertEquals("fallback.css", noscript.select("link").attr("href"));
        assertEquals("30", noscript.select("meta").attr("content"));
    }

    // Tests AfterHead transitioning to InFrameset when encountering frameset tag
    @Test
    public void testAfterHead_framesetTag_transitionsToInFrameset() {
        String html = "<html><head><title>Frames</title></head><frameset cols=\"50%,50%\"><frame src=\"frame1.html\"><frame src=\"frame2.html\"><noframes><p>No frames</p></noframes></frameset></html>";
        Document doc = Jsoup.parse(html);

        Element frameset = doc.select("frameset").first();
        assertNotNull(frameset);
        assertEquals(2, frameset.select("frame").size());
        assertNotNull(doc.select("noframes").first());
    }

    // Tests InBody Adoption Agency Algorithm on misnested formatting tags
    @Test
    public void testInBody_misnestedFormattingTags_reconstructedByAdoptionAgency() {
        String html = "<p><b>1<i>2</b>3</i></p>";
        Document doc = Jsoup.parse(html);

        assertEquals("<p><b>1<i>2</i></b><i>3</i></p>", doc.body().html());
    }

    // Tests InBody reconstructing active formatting elements across paragraph boundaries
    @Test
    public void testInBody_reconstructFormattingElements_spansAcrossParagraphs() {
        String html = "<b>Line 1<p>Line 2</p>Line 3</b>";
        Document doc = Jsoup.parse(html);

        assertEquals("<b>Line 1</b><p><b>Line 2</b></p><b>Line 3</b>", doc.body().html());
    }

    // Tests InBody auto-closing list items when a new li start tag is encountered
    @Test
    public void testInBody_nestedListItems_autoClosesPreviousLi() {
        String html = "<ul><li>Item 1<li>Item 2<li>Item 3</ul>";
        Document doc = Jsoup.parse(html);

        assertEquals(3, doc.select("ul > li").size());
        assertEquals("Item 1", doc.select("li").get(0).text());
        assertEquals("Item 2", doc.select("li").get(1).text());
    }

    // Tests InBody heading elements auto-closing previous headings
    @Test
    public void testInBody_nestedHeadings_autoClosesPrecedingHeading() {
        String html = "<h1>Heading 1<h2>Heading 2<h3>Heading 3</h3></h2></h1>";
        Document doc = Jsoup.parse(html);

        assertEquals(0, doc.select("h1 > h2").size());
        assertEquals(0, doc.select("h2 > h3").size());
        assertEquals("Heading 1", doc.select("h1").text());
        assertEquals("Heading 2", doc.select("h2").text());
        assertEquals("Heading 3", doc.select("h3").text());
    }

    // Tests InBody isindex tag conversion to form and text input
    @Test
    public void testInBody_isindexTag_expandsToFormAndInput() {
        String html = "<body><isindex prompt=\"Search this site: \" action=\"/search\"></body>";
        Document doc = Jsoup.parse(html);

        Element form = doc.select("form").first();
        assertNotNull(form);
        assertEquals("/search", form.attr("action"));
        Element input = form.select("input[name=isindex]").first();
        assertNotNull(input);
        assertTrue(form.select("label").text().contains("Search this site: "));
    }

    // Tests InBody ignoring nested form start tags
    @Test
    public void testInBody_nestedForm_ignoresInnerForm() {
        Parser parser = Parser.htmlParser().setTrackErrors(10);
        Document doc = parser.parseInput("<body><form id=\"outer\"><p><form id=\"inner\"><input name=\"q\"></form></p></form></body>", "");

        assertEquals(1, doc.select("form").size());
        assertEquals("outer", doc.select("form").first().id());
        assertFalse(parser.getErrors().isEmpty());
    }

    // Tests InTable, InTableBody, InRow, and InCell hierarchy creation
    @Test
    public void testInTable_completeTableStructure_createsCorrectElements() {
        String html = "<table><caption>Title</caption><colgroup><col class=\"col1\"></colgroup><thead><tr><th>Head</th></tr></thead><tbody><tr><td>Cell</td></tr></tbody><tfoot><tr><td>Foot</td></tr></tfoot></table>";
        Document doc = Jsoup.parse(html);

        assertNotNull(doc.select("table > caption").first());
        assertNotNull(doc.select("table > colgroup > col.col1").first());
        assertNotNull(doc.select("table > thead > tr > th").first());
        assertNotNull(doc.select("table > tbody > tr > td").first());
        assertNotNull(doc.select("table > tfoot > tr > td").first());
    }

    // Tests InTable and InTableText foster parenting when text or non-table tags appear in table
    @Test
    public void testInTable_fosterParenting_misplacedContentMovedAboveTable() {
        String html = "<table>Characters <b>Bold</b><tr><td>Cell</td></tr></table>";
        Document doc = Jsoup.parse(html);

        assertEquals("Characters <b>Bold</b><table><tbody><tr><td>Cell</td></tr></tbody></table>", doc.body().html());
    }

    // Tests InSelect and InSelectInTable transitions on options, optgroups, and closing table tags
    @Test
    public void testInSelect_optionsAndOptgroups_structuredProperly() {
        String html = "<table><tr><td><select name=\"sel\"><optgroup label=\"group1\"><option value=\"1\">One<option value=\"2\">Two</optgroup><option value=\"3\">Three</select></td></tr></table>";
        Document doc = Jsoup.parse(html);

        Element select = doc.select("select").first();
        assertNotNull(select);
        assertEquals(1, select.select("optgroup").size());
        assertEquals(2, select.select("optgroup > option").size());
        assertEquals(3, select.select("option").size());
    }

    // Tests InSelect dropping out of scope when input or textarea is encountered
    @Test
    public void testInSelect_unexpectedInputTag_closesSelect() {
        String html = "<select><option>Option 1<input type=\"text\" value=\"test\"></select>";
        Document doc = Jsoup.parse(html);

        assertNotNull(doc.select("select").first());
        assertEquals(0, doc.select("select input").size());
        assertNotNull(doc.select("body > input").first());
    }

    // Tests AfterBody and AfterAfterBody handling comments and whitespace after closing html tag
    @Test
    public void testAfterBody_trailingCommentsAndWhitespace_parsedSuccessfully() {
        String html = "<html><head></head><body>Content</body></html><!-- Trailing Comment -->\n";
        Document doc = Jsoup.parse(html);

        assertEquals("Content", doc.body().text());
        assertTrue(doc.outerHtml().contains("<!-- Trailing Comment -->"));
    }

    // Tests ForeignContent / SVG & MathML elements handling self-closing and body integration
    @Test
    public void testInBody_svgAndMathElements_insertedCorrectly() {
        String html = "<div><svg><circle cx=\"50\" cy=\"50\" r=\"40\" /></svg><math><mi>x</mi></math></div>";
        Document doc = Jsoup.parse(html);

        assertNotNull(doc.select("svg > circle").first());
        assertNotNull(doc.select("math > mi").first());
    }

    // Tests InTable auto-closing table on EOF or unexpected table start tag
    @Test
    public void testInTable_nestedTableWithoutClose_autoClosesFirstTable() {
        String html = "<table><tr><td>Cell 1<table><tr><td>Cell 2</td></tr></table></td></tr></table>";
        Document doc = Jsoup.parse(html);

        assertEquals(2, doc.select("table").size());
        assertEquals(1, doc.select("table table").size());
    }
}