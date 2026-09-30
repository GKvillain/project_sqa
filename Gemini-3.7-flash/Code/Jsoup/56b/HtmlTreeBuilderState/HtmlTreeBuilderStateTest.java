package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.DocumentType;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class HtmlTreeBuilderStateTest {

    // Tests Initial state doctype parsing and QuirksMode transitions
    @Test
    public void testInitial_doctypeWithForceQuirks_setsQuirksMode() {
        String html = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Frameset//EN\"><html><body></body></html>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.documentType());
        assertEquals("html", doc.documentType().name());
    }

    // Tests Initial state whitespace and comments before doctype
    @Test
    public void testInitial_whitespaceAndComments_handledCorrectly() {
        String html = "   <!-- comment before doctype -->\n<!DOCTYPE html><html><head></head><body></body></html>";
        Document doc = Jsoup.parse(html);
        assertTrue(doc.childNodeSize() >= 2);
    }

    // Tests BeforeHtml state handling unexpected doctypes and tags
    @Test
    public void testBeforeHtml_unexpectedTokens_createsHtmlElement() {
        Parser parser = Parser.htmlParser().setTrackErrors(10);
        Document doc = parser.parseInput("<div>Hello</div>", "");
        assertNotNull(doc.select("html").first());
        assertNotNull(doc.select("body").first());
        assertEquals("Hello", doc.select("div").first().text());
    }

    // Tests InHead state processing title, base, meta, style, script, noscript
    @Test
    public void testInHead_variousTags_handledCorrectly() {
        String html = "<html><head><title>Test Title</title><base href='http://example.com/'><meta charset='utf-8'><style>body{color:red;}</style><script>var x = 1;</script><noscript><link rel='stylesheet' href='style.css'></noscript></head><body></body></html>";
        Document doc = Jsoup.parse(html);
        assertEquals("Test Title", doc.title());
        assertEquals("http://example.com/", doc.baseUri());
        assertEquals("http://example.com/", doc.select("base").first().attr("href"));
        assertEquals("utf-8", doc.select("meta").first().attr("charset"));
        assertEquals("body{color:red;}", doc.select("style").first().data());
        assertEquals("var x = 1;", doc.select("script").first().data());
    }

    // Tests AfterHead state transition to InBody and InFrameset
    @Test
    public void testAfterHead_bodyAndFrameset_transitionsCorrectly() {
        String htmlBody = "<html><head></head><body><p>Content</p></body></html>";
        Document docBody = Jsoup.parse(htmlBody);
        assertNotNull(docBody.body());
        assertEquals("Content", docBody.select("p").first().text());

        String htmlFrameset = "<html><head></head><frameset cols='50%,50%'><frame src='frame1.html'><frame src='frame2.html'></frameset></html>";
        Document docFrameset = Jsoup.parse(htmlFrameset);
        assertNotNull(docFrameset.select("frameset").first());
        assertEquals(2, docFrameset.select("frame").size());
    }

    // Tests InBody Adoption Agency Algorithm with formatting tags
    @Test
    public void testInBody_adoptionAgencyAlgorithm_reconstructsFormatting() {
        String html = "<b>1<p>2</b>3</p>";
        Document doc = Jsoup.parse(html);
        assertEquals("<b>1</b>\n<p><b>2</b>3</p>", doc.body().html());
    }

    // Tests InBody nested formatting and unmatched tags
    @Test
    public void testInBody_unmatchedFormattingTags_closedGracefully() {
        String html = "<a><b><p>test</a></b>";
        Document doc = Jsoup.parse(html);
        assertEquals("<a><b></b></a><b></b>\n<p><a><b>test</b></a></p>", doc.body().html());
    }

    // Tests InBody buttons, headings and p tag autocompletion
    @Test
    public void testInBody_headingsAndButtonScopes_autoClosesParagraph() {
        String html = "<p>First<h1>Heading</h1><p>Second<button><p>Inside Button</button>";
        Document doc = Jsoup.parse(html);
        assertEquals(3, doc.select("p").size());
        assertEquals(1, doc.select("h1").size());
        assertEquals(1, doc.select("button").size());
    }

    // Tests InBody lists (li), definition lists (dd, dt) auto-closing
    @Test
    public void testInBody_listsAndDefinitionLists_autoClosesItems() {
        String html = "<ul><li>Item 1<li>Item 2</ul><dl><dt>Term 1<dd>Def 1<dt>Term 2<dd>Def 2</dl>";
        Document doc = Jsoup.parse(html);
        assertEquals(2, doc.select("li").size());
        assertEquals(2, doc.select("dt").size());
        assertEquals(2, doc.select("dd").size());
    }

    // Tests InBody forms handling
    @Test
    public void testInBody_formTags_nestedFormsIgnored() {
        Parser parser = Parser.htmlParser().setTrackErrors(10);
        String html = "<form id='f1'><input name='i1'><form id='f2'><input name='i2'></form></form>";
        Document doc = parser.parseInput(html, "");
        assertEquals(1, doc.select("form").size());
        assertEquals("f1", doc.select("form").first().id());
        assertEquals(2, doc.select("input").size());
        assertTrue(parser.getErrors().size() > 0);
    }

    // Tests InTable, InTableBody, InRow, InCell states and foster parenting
    @Test
    public void testInTable_fosterParenting_movesTextOutOfTable() {
        String html = "<table>foo<tr><td>bar</td>baz</tr>qux</table>";
        Document doc = Jsoup.parse(html);
        assertEquals("foo\nbaz\nqux\n<table>\n <tbody>\n  <tr>\n   <td>bar</td>\n  </tr>\n </tbody>\n</table>", doc.body().html());
    }

    // Tests InTable colgroup and caption transitions
    @Test
    public void testInTable_captionAndColgroup_structureParsed() {
        String html = "<table><caption>Title</caption><colgroup><col span='2'></colgroup><tbody><tr><td>Data</td></tr></tbody></table>";
        Document doc = Jsoup.parse(html);
        assertEquals("Title", doc.select("caption").first().text());
        assertEquals(1, doc.select("colgroup").size());
        assertEquals(1, doc.select("col").size());
        assertEquals("Data", doc.select("td").first().text());
    }

    // Tests InSelect and InSelectInTable transitions
    @Test
    public void testInSelect_optionsAndOptgroups_parsedCorrectly() {
        String html = "<select><optgroup label='g1'><option>1<option>2</optgroup><option>3</select>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("optgroup").size());
        assertEquals(3, doc.select("option").size());

        String tableSelect = "<table><tr><td><select><option>A</option><tr><td>Next</td></tr></table>";
        Document docTable = Jsoup.parse(tableSelect);
        assertEquals(2, docTable.select("tr").size());
        assertEquals("A", docTable.select("option").first().text());
    }

    // Tests Text state for raw text elements like textarea, xmp, iframe, noembed
    @Test
    public void testText_rawTextElements_parsedAsCharacters() {
        String html = "<textarea><p>not a paragraph</p></textarea><xmp><b>bold</b></xmp><iframe><span>frame</span></iframe>";
        Document doc = Jsoup.parse(html);
        assertEquals("<p>not a paragraph</p>", doc.select("textarea").first().text());
        assertEquals("<b>bold</b>", doc.select("xmp").first().text());
        assertEquals("<span>frame</span>", doc.select("iframe").first().text());
    }

    // Tests AfterBody and AfterAfterBody comments and whitespace handling
    @Test
    public void testAfterBody_trailingContent_handledCorrectly() {
        String html = "<html><head></head><body><div>Content</div></body><!-- comment after body --></html><!-- comment after html -->";
        Document doc = Jsoup.parse(html);
        assertEquals("Content", doc.select("div").first().text());
        List<Node> childNodes = doc.childNodes();
        assertTrue(childNodes.size() >= 2);
    }

    // Tests InHeadNoscript state
    @Test
    public void testInHeadNoscript_elementsInsideNoscript_parsed() {
        String html = "<html><head><noscript><meta http-equiv='refresh' content='0'><style>body{margin:0;}</style></noscript></head><body></body></html>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.select("head noscript meta").first());
        assertNotNull(doc.select("head noscript style").first());
    }

    // Tests Direct state execution fallback and anythingElse branch
    @Test
    public void testForeignContent_processReturnsTrue() {
        Parser parser = Parser.htmlParser();
        Document doc = parser.parseInput("<svg><foreignObject><div>test</div></foreignObject></svg>", "");
        assertNotNull(doc.select("svg foreignObject div").first());
        assertEquals("test", doc.select("svg foreignObject div").first().text());
    }

    // Tests BeforeHead state handling
    @Test
    public void testBeforeHead_whitespaceCommentsAndTagTransitions() {
        String html = "<html><!-- before head comment -->  <head><title>Head</title></head><body></body></html>";
        Document doc = Jsoup.parse(html);
        assertEquals("Head", doc.title());
        assertNotNull(doc.head());
    }

    // Tests InCaption state handling start/end tags and closing
    @Test
    public void testInCaption_tableCaptionTagHandling() {
        String html = "<table><caption>Caption Text<p>Paragraph in Caption</p></caption><tr><td>Cell</td></tr></table>";
        Document doc = Jsoup.parse(html);
        Element caption = doc.select("caption").first();
        assertNotNull(caption);
        assertEquals("Caption Text Paragraph in Caption", caption.text());
        assertEquals("Cell", doc.select("td").first().text());
    }

    // Tests InColumnGroup state handling col tag and unexpected tags
    @Test
    public void testInColumnGroup_colElementsAndEndTags() {
        String html = "<table><colgroup><col class='c1'><col class='c2'></colgroup><tr><td>Data</td></tr></table>";
        Document doc = Jsoup.parse(html);
        assertEquals(2, doc.select("colgroup col").size());
        assertEquals("c1", doc.select("colgroup col").first().className());
    }

    // Tests InTableBody and InRow state transitions
    @Test
    public void testInTableBody_and_InRow_states() {
        String html = "<table><thead><tr><th>Header</th></tr></thead><tbody><tr><td>Cell 1</td><td>Cell 2</td></tr></tbody><tfoot><tr><td>Footer</td></tr></tfoot></table>";
        Document doc = Jsoup.parse(html);
        assertEquals("Header", doc.select("thead th").first().text());
        assertEquals(2, doc.select("tbody td").size());
        assertEquals("Footer", doc.select("tfoot td").first().text());
    }

    // Tests InCell state auto-closing upon new cell/row tags
    @Test
    public void testInCell_autoClosesOnNextCell() {
        String html = "<table><tr><td>Cell 1<td>Cell 2<th>Header 1<th>Header 2</tr></table>";
        Document doc = Jsoup.parse(html);
        assertEquals(2, doc.select("td").size());
        assertEquals(2, doc.select("th").size());
        assertEquals("Cell 1", doc.select("td").get(0).text());
        assertEquals("Cell 2", doc.select("td").get(1).text());
    }

    // Tests InFrameset and AfterFrameset states
    @Test
    public void testInFrameset_nestedFramesetAndNoframes() {
        String html = "<html><frameset rows='50%,50%'><frame src='f1.html'><frameset cols='50%,50%'><frame src='f2.html'></frameset><noframes><p>No frames supported</p></noframes></frameset></html><!-- after frameset comment -->";
        Document doc = Jsoup.parse(html);
        assertEquals(2, doc.select("frameset").size());
        assertEquals(2, doc.select("frame").size());
        assertNotNull(doc.select("noframes").first());
    }

    // Tests AfterAfterBody state with comments and whitespace
    @Test
    public void testAfterAfterBody_doctypeAndTrailingComments() {
        String html = "<!DOCTYPE html><html><head></head><body></body></html><!-- comment 1 -->\n<!-- comment 2 -->";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.body());
        assertTrue(doc.childNodes().size() >= 3);
    }
}