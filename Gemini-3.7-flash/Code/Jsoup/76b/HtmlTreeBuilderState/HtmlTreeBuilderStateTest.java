package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.DocumentType;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class HtmlTreeBuilderStateTest {

    private void assertArraySorted(String[] array) {
        String[] copy = Arrays.copyOf(array, array.length);
        Arrays.sort(copy);
        assertArrayEquals("Constants array must be sorted for inSorted binary search", copy, array);
    }

    // Tests that all Constants arrays used by StringUtil.inSorted are alphabetically sorted
    @Test
    public void testConstants_arraysSorted_matchAlphabeticalOrder() {
        assertArraySorted(HtmlTreeBuilderState.Constants.InBodyStartToHead);
        assertArraySorted(HtmlTreeBuilderState.Constants.InBodyStartPClosers);
        assertArraySorted(HtmlTreeBuilderState.Constants.Headings);
        assertArraySorted(HtmlTreeBuilderState.Constants.InBodyStartPreListing);
        assertArraySorted(HtmlTreeBuilderState.Constants.InBodyStartLiBreakers);
        assertArraySorted(HtmlTreeBuilderState.Constants.DdDt);
        assertArraySorted(HtmlTreeBuilderState.Constants.Formatters);
        assertArraySorted(HtmlTreeBuilderState.Constants.InBodyStartApplets);
        assertArraySorted(HtmlTreeBuilderState.Constants.InBodyStartEmptyFormatters);
        assertArraySorted(HtmlTreeBuilderState.Constants.InBodyStartMedia);
        assertArraySorted(HtmlTreeBuilderState.Constants.InBodyStartInputAttribs);
        assertArraySorted(HtmlTreeBuilderState.Constants.InBodyStartOptions);
        assertArraySorted(HtmlTreeBuilderState.Constants.InBodyStartRuby);
        assertArraySorted(HtmlTreeBuilderState.Constants.InBodyStartDrop);
        assertArraySorted(HtmlTreeBuilderState.Constants.InBodyEndClosers);
        assertArraySorted(HtmlTreeBuilderState.Constants.InBodyEndAdoptionFormatters);
        assertArraySorted(HtmlTreeBuilderState.Constants.InBodyEndTableFosters);
    }

    // Tests Initial and BeforeHtml states with doctype, comments and whitespace
    @Test
    public void testInitialAndBeforeHtml_withDoctypeAndComments_buildsValidDocument() {
        String html = "<!DOCTYPE html><!-- test comment --><html><head></head><body>Hello</body></html>";
        Document doc = Jsoup.parse(html);
        DocumentType doctype = null;
        for (Node n : doc.childNodes()) {
            if (n instanceof DocumentType) {
                doctype = (DocumentType) n;
                break;
            }
        }
        assertNotNull(doctype);
        assertEquals("html", doctype.name());
        assertEquals("Hello", doc.body().text());
    }

    // Tests InHead state with title, meta, link, base and style
    @Test
    public void testInHead_headTags_parsedCorrectly() {
        String html = "<html><head><base href='http://example.com/'><title>Page Title</title><meta charset='utf-8'><link rel='stylesheet' href='style.css'><style>body { color: red; }</style></head><body>Text</body></html>";
        Document doc = Jsoup.parse(html);
        assertEquals("Page Title", doc.title());
        assertEquals("http://example.com/", doc.baseUri());
        assertEquals(1, doc.head().getElementsByTag("meta").size());
        assertEquals(1, doc.head().getElementsByTag("link").size());
        assertEquals(1, doc.head().getElementsByTag("style").size());
    }

    // Tests InHeadNoscript state
    @Test
    public void testInHeadNoscript_noscriptElements_parsedProperly() {
        String html = "<head><noscript><link rel='stylesheet' href='no-script.css'><style>p { color: green; }</style></noscript></head>";
        Document doc = Jsoup.parse(html);
        Element noscript = doc.head().select("noscript").first();
        assertNotNull(noscript);
        assertEquals(1, noscript.children().size());
    }

    // Tests InBody state with headings and auto-closing paragraph tags
    @Test
    public void testInBody_headingsAndParagraph_autoClosesP() {
        String html = "<p>First paragraph<h2>Heading 2</h2><p>Second paragraph<h1>Heading 1</h3><p>Third";
        Document doc = Jsoup.parse(html);
        assertEquals(3, doc.body().getElementsByTag("p").size());
        assertEquals(1, doc.body().getElementsByTag("h2").size());
        assertEquals(1, doc.body().getElementsByTag("h1").size());
    }

    // Tests InBody state with lists and definition lists (li, dd, dt)
    @Test
    public void testInBody_listsAndDefinitions_properlyNested() {
        String html = "<ul><li>Item 1<li>Item 2</ul><dl><dt>Term<dd>Definition</dl>";
        Document doc = Jsoup.parse(html);
        assertEquals(2, doc.select("ul > li").size());
        assertEquals(1, doc.select("dl > dt").size());
        assertEquals(1, doc.select("dl > dd").size());
    }

    // Tests InBody state with pre and listing tags
    @Test
    public void testInBody_preAndListingTags_closesPAndFormatsText() {
        String html = "<p>Text<pre>Preformatted</pre><listing>Listed content</listing>";
        Document doc = Jsoup.parse(html);
        assertEquals("Preformatted", doc.select("pre").first().text());
        assertEquals("Listed content", doc.select("listing").first().text());
        assertFalse(doc.select("p").first().children().contains(doc.select("pre").first()));
    }

    // Tests InBody adoption agency algorithm with nested formatting tags
    @Test
    public void testInBody_adoptionAgencyAlgorithm_reconstructsFormattingElements() {
        String html = "<b>1<p>2</b>3</p>";
        Document doc = Jsoup.parse(html);
        assertEquals("<b>1</b><p><b>2</b>3</p>", doc.body().html().replaceAll("\\r?\\n", ""));
    }

    // Tests InBody state with button, form and input controls
    @Test
    public void testInBody_formsAndButtons_properlyStructured() {
        String html = "<form action='/submit'><button>Click<button>Nested</button><input type='text' name='q'></form>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("form").size());
        assertEquals(2, doc.select("button").size());
        assertEquals(1, doc.select("input").size());
    }

    // Tests InBody formatting and empty formatters
    @Test
    public void testInBody_emptyFormattersAndMedia_insertedEmpty() {
        String html = "<div><img src='test.png'><br><hr><wbr><area><embed><keygen></div>";
        Document doc = Jsoup.parse(html);
        Element div = doc.select("div").first();
        assertNotNull(div);
        assertEquals(7, div.children().size());
    }

    // Tests InTable, InTableBody, InRow, InCell states
    @Test
    public void testInTable_completeTableHierarchy_parsedCorrectly() {
        String html = "<table><caption>Title</caption><colgroup><col></colgroup><thead><tr><th>H1</th></tr></thead><tbody><tr><td>D1</td></tr></tbody><tfoot><tr><td>F1</td></tr></tfoot></table>";
        Document doc = Jsoup.parse(html);
        assertEquals("Title", doc.select("caption").first().text());
        assertEquals("H1", doc.select("th").first().text());
        assertEquals(2, doc.select("td").size());
        assertEquals(1, doc.select("thead").size());
        assertEquals(1, doc.select("tbody").size());
        assertEquals(1, doc.select("tfoot").size());
    }

    // Tests foster parenting when non-table content is placed inside table
    @Test
    public void testInTable_fosterParenting_fostersOutsideTable() {
        String html = "<table>Text before<tr><td>Cell</td></tr>Text after</table>";
        Document doc = Jsoup.parse(html);
        List<Element> tables = doc.select("table");
        assertEquals(1, tables.size());
        assertTrue(doc.body().text().contains("Text before"));
        assertTrue(doc.body().text().contains("Text after"));
    }

    // Tests InSelect and InSelectInTable states
    @Test
    public void testInSelect_optionsAndOptgroups_parsedCorrectly() {
        String html = "<select><optgroup label='G1'><option>1<option>2</optgroup><option>3</select>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("optgroup").size());
        assertEquals(3, doc.select("option").size());
    }

    // Tests InSelect inside a table
    @Test
    public void testInSelectInTable_selectInTable_transitionsCorrectly() {
        String html = "<table><tr><td><select><option>Option 1</option><td>Next cell</td></tr></table>";
        Document doc = Jsoup.parse(html);
        assertEquals(2, doc.select("td").size());
        assertEquals(1, doc.select("select").size());
    }

    // Tests InFrameset and AfterFrameset states
    @Test
    public void testInFrameset_frameElements_parsedCorrectly() {
        String html = "<html><frameset rows='50%,50%'><frame src='top.html'><frame src='bottom.html'><noframes>No frames</noframes></frameset></html>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("frameset").size());
        assertEquals(2, doc.select("frame").size());
        assertEquals(1, doc.select("noframes").size());
    }

    // Tests Text state for raw text and rcdata tags like textarea and script
    @Test
    public void testTextState_textareaAndScript_containsRawContent() {
        String html = "<textarea><p>Raw & text</p></textarea><script>var a = '<p>test</p>';</script>";
        Document doc = Jsoup.parse(html);
        assertEquals("<p>Raw & text</p>", doc.select("textarea").first().text());
        assertTrue(doc.select("script").first().data().contains("var a = '<p>test</p>';"));
    }

    // Tests ForeignContent state fallback
    @Test
    public void testForeignContent_processMethod_returnsTrue() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        Token.Character charToken = new Token.Character().data("content");
        assertTrue(HtmlTreeBuilderState.ForeignContent.process(charToken, tb));
    }

    // Tests AfterBody and AfterAfterBody transitions
    @Test
    public void testAfterBody_tokensAfterBody_handledGracefully() {
        String html = "<html><head></head><body>Content</body><!-- comment after body --></html><!-- comment after html -->";
        Document doc = Jsoup.parse(html);
        assertEquals("Content", doc.body().text());
        assertNotNull(doc);
    }
}