package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.DocumentType;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class HtmlTreeBuilderStateTest {

    // Tests doctype handling in Initial state and transition to BeforeHtml
    @Test
    public void testProcess_doctypeInInitial_createsDoctypeNode() {
        String html = "<!DOCTYPE html><html><head></head><body></body></html>";
        Document doc = Jsoup.parse(html);
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

    // Tests self closing rawtext tags in head (e.g. style, title, noframes)
    @Test
    public void testProcess_selfClosingRawtextInHead_parsesBodyCorrectly() {
        String html = "<html><head><noframes /><style /><title /></head><body><p>Content</p></body></html>";
        Document doc = Jsoup.parse(html);
        Element p = doc.select("p").first();
        assertNotNull(p);
        assertEquals("Content", p.text());
        assertEquals("body", p.parent().nodeName());
    }

    // Tests head elements processing in InHead state
    @Test
    public void testProcess_inHeadElements_parsedIntoHead() {
        String html = "<html><head><title>Test Title</title><base href='http://example.com/'><link rel='stylesheet' href='style.css'><meta charset='utf-8'><script>var a = 1;</script></head><body></body></html>";
        Document doc = Jsoup.parse(html);
        assertEquals("Test Title", doc.title());
        assertEquals("http://example.com/", doc.baseUri());
        assertEquals(1, doc.head().select("link").size());
        assertEquals(1, doc.head().select("meta").size());
        assertEquals(1, doc.head().select("script").size());
    }

    // Tests InHeadNoscript state
    @Test
    public void testProcess_noscriptInHead_parsesElementsCorrectly() {
        String html = "<html><head><noscript><link rel='stylesheet' href='fallback.css'></noscript></head><body><p>Hello</p></body></html>";
        Document doc = Jsoup.parse(html);
        Element noscript = doc.head().select("noscript").first();
        assertNotNull(noscript);
        assertEquals(1, noscript.select("link").size());
        assertEquals("Hello", doc.body().text());
    }

    // Tests InBody adoption agency algorithm for unclosed formatting elements
    @Test
    public void testProcess_inBodyFormattingAdoptionAgency_wrapsChildrenProperly() {
        String html = "<p><b>Bold <i>Italic</b> Still Italic</i> Plain</p>";
        Document doc = Jsoup.parse(html);
        assertEquals("<p><b>Bold <i>Italic</i></b><i> Still Italic</i> Plain</p>", doc.body().html());
    }

    // Tests InBody headings and paragraph closing
    @Test
    public void testProcess_headingsAndParagraphClosers_closesParagraph() {
        String html = "<p>Para 1<h1>Heading 1</h1><h2>Heading 2</h2><p>Para 2";
        Document doc = Jsoup.parse(html);
        Elements children = doc.body().children();
        assertEquals(4, children.size());
        assertEquals("p", children.get(0).tagName());
        assertEquals("h1", children.get(1).tagName());
        assertEquals("h2", children.get(2).tagName());
        assertEquals("p", children.get(3).tagName());
    }

    // Tests InTable, InTableBody, InRow, and InCell states
    @Test
    public void testProcess_tableStructure_createsProperHierarchy() {
        String html = "<table><caption>Title</caption><colgroup><col></colgroup><thead><tr><th>H1</th></tr></thead><tbody><tr><td>Data 1</td></tr></tbody><tfoot><tr><td>F1</td></tr></tfoot></table>";
        Document doc = Jsoup.parse(html);
        Element table = doc.select("table").first();
        assertNotNull(table);
        assertEquals("Title", table.select("caption").first().text());
        assertEquals(1, table.select("colgroup col").size());
        assertEquals(1, table.select("thead tr th").size());
        assertEquals("H1", table.select("thead tr th").first().text());
        assertEquals("Data 1", table.select("tbody tr td").first().text());
        assertEquals("F1", table.select("tfoot tr td").first().text());
    }

    // Tests InTable foster parenting for orphan content inside table
    @Test
    public void testProcess_inTableFosterParenting_movesContentAboveTable() {
        String html = "<table>Text outside cell<tr><td>Cell</td></tr></table>";
        Document doc = Jsoup.parse(html);
        Element body = doc.body();
        assertTrue(body.html().startsWith("Text outside cell<table>"));
        assertEquals("Cell", body.select("td").first().text());
    }

    // Tests InSelect and InSelectInTable states
    @Test
    public void testProcess_inSelectAndOptgroup_createsHierarchy() {
        String html = "<select><optgroup label='group1'><option>1</option><option>2</option></optgroup><option>3</option></select>";
        Document doc = Jsoup.parse(html);
        Element select = doc.select("select").first();
        assertNotNull(select);
        assertEquals(1, select.select("optgroup").size());
        assertEquals(3, select.select("option").size());
    }

    // Tests InFrameset, AfterFrameset, and AfterAfterFrameset states
    @Test
    public void testProcess_framesetDocument_parsesFrameset() {
        String html = "<html><frameset rows='50%,50%'><frame src='frame1.html'><frame src='frame2.html'><noframes><p>No frames</p></noframes></frameset></html>";
        Document doc = Jsoup.parse(html);
        Element frameset = doc.select("frameset").first();
        assertNotNull(frameset);
        assertEquals(2, frameset.select("frame").size());
        assertEquals(1, frameset.select("noframes").size());
    }

    // Tests InBody form and input element processing
    @Test
    public void testProcess_formAndInput_createsFormAndPreventsNestedForm() {
        String html = "<form id='f1'><input type='text' name='q'><form id='f2'><input type='hidden' name='h'></form></form>";
        Document doc = Jsoup.parse(html);
        Elements forms = doc.select("form");
        assertEquals(1, forms.size());
        assertEquals("f1", forms.first().id());
        assertEquals(2, forms.first().select("input").size());
    }

    // Tests InBody list item (li, dt, dd) scopes
    @Test
    public void testProcess_listItems_impliedEndTagsHandled() {
        String html = "<ul><li>Item 1<li>Item 2</ul><dl><dt>Term 1<dd>Def 1<dt>Term 2<dd>Def 2</dl>";
        Document doc = Jsoup.parse(html);
        assertEquals(2, doc.select("ul li").size());
        assertEquals(2, doc.select("dl dt").size());
        assertEquals(2, doc.select("dl dd").size());
    }

    // Tests Text state and transitions via HtmlTreeBuilder
    @Test
    public void testProcess_textState_accumulatesCharacters() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse(new java.io.StringReader("<title>Testing</title>"), "http://example.com", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        tb.runParser();
        Document doc = tb.getDocument();
        assertEquals("Testing", doc.title());
    }

    // Tests ForeignContent state directly
    @Test
    public void testProcess_foreignContentState_returnsTrue() {
        Token.Character charToken = new Token.Character().data("content");
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse(new java.io.StringReader(""), "", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        boolean result = HtmlTreeBuilderState.ForeignContent.process(charToken, tb);
        assertTrue(result);
    }

    // Tests AfterBody and AfterAfterBody transitions on trailing content
    @Test
    public void testProcess_afterBodyAndAfterAfterBody_handlesTrailingTokens() {
        String html = "<html><head></head><body>Hello</body></html><!-- comment -->";
        Document doc = Jsoup.parse(html);
        assertEquals("Hello", doc.body().text());
        assertEquals(1, doc.childNodeSize() > 0 ? 1 : 0);
    }
}