package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.DocumentType;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.junit.Test;

import static org.junit.Assert.*;

public class HtmlTreeBuilderStateTest {

    // Tests unclosed anchor tag across paragraph elements (Defects4J bug 35)
    @Test
    public void testProcess_unclosedAnchorBeforeParagraph_reconstructsAnchorProperly() {
        String html = "<a href='http://example.com/'>Link<p>Error link</a>";
        Document doc = Jsoup.parse(html);
        assertEquals("<a href=\"http://example.com/\">Link</a>\n<p><a href=\"http://example.com/\">Error link</a></p>", doc.body().html());
    }

    // Tests Initial state handling doctype token
    @Test
    public void testProcess_initialDoctype_setsDocumentTypeAndQuirksMode() {
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

    // Tests Initial and BeforeHtml states handling comments
    @Test
    public void testProcess_beforeHtmlComment_insertsCommentIntoDocument() {
        String html = "<!-- Initial Comment --><html><!-- Head Comment --><head></head><body></body></html>";
        Document doc = Jsoup.parse(html);
        assertTrue(doc.childNode(0) instanceof Comment);
        assertEquals(" Initial Comment ", ((Comment) doc.childNode(0)).getData());
    }

    // Tests InHead state handling title, meta, link, and style elements
    @Test
    public void testProcess_inHeadElements_createsExpectedHeadStructure() {
        String html = "<html><head><title>Test Title</title><meta charset='utf-8'><link rel='stylesheet' href='test.css'></head><body></body></html>";
        Document doc = Jsoup.parse(html);
        assertEquals("Test Title", doc.title());
        assertEquals(1, doc.head().select("meta").size());
        assertEquals(1, doc.head().select("link").size());
    }

    // Tests InHead and InHeadNoscript state handling noscript tags
    @Test
    public void testProcess_inHeadNoscript_parsesNoscriptElements() {
        String html = "<html><head><noscript><link rel='stylesheet' href='fallback.css'></noscript></head><body></body></html>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.head().select("noscript").first());
        assertEquals(1, doc.head().select("noscript > link").size());
    }

    // Tests AfterHead state transition when encountering body tag
    @Test
    public void testProcess_afterHeadBodyTag_transitionsToInBody() {
        String html = "<html><head></head><body class='main'><p>Hello</p></body></html>";
        Document doc = Jsoup.parse(html);
        assertEquals("main", doc.body().className());
        assertEquals("Hello", doc.body().select("p").text());
    }

    // Tests InBody state Adoption Agency Algorithm with formatting tags
    @Test
    public void testProcess_inBodyFormattingTags_appliesAdoptionAgencyAlgorithm() {
        String html = "<p><b>Bold <i>and Italic</b> italic only</i> normal</p>";
        Document doc = Jsoup.parse(html);
        assertEquals("<p><b>Bold <i>and Italic</i></b><i> italic only</i> normal</p>", doc.body().html());
    }

    // Tests InBody state handling list items and implicit closing
    @Test
    public void testProcess_inBodyListItems_autoClosesPreviousListItem() {
        String html = "<ul><li>Item 1<li>Item 2<li>Item 3</ul>";
        Document doc = Jsoup.parse(html);
        assertEquals(3, doc.select("li").size());
        assertEquals("Item 1", doc.select("li").get(0).text());
        assertEquals("Item 2", doc.select("li").get(1).text());
    }

    // Tests InBody state handling definition lists (dd and dt tags)
    @Test
    public void testProcess_inBodyDefinitionList_autoClosesPreviousTerms() {
        String html = "<dl><dt>Term 1<dd>Desc 1<dt>Term 2<dd>Desc 2</dl>";
        Document doc = Jsoup.parse(html);
        assertEquals(2, doc.select("dt").size());
        assertEquals(2, doc.select("dd").size());
    }

    // Tests InTable, InTableBody, InRow, and InCell states with table structures
    @Test
    public void testProcess_inTableStructure_createsCompleteTableHierarchy() {
        String html = "<table><caption>Cap</caption><colgroup><col></colgroup><thead><tr><th>Head</th></tr></thead><tbody><tr><td>Data</td></tr></tbody></table>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.select("table > caption").first());
        assertNotNull(doc.select("table > colgroup > col").first());
        assertEquals("Head", doc.select("thead > tr > th").text());
        assertEquals("Data", doc.select("tbody > tr > td").text());
    }

    // Tests InTable foster parenting when plain text is found inside table
    @Test
    public void testProcess_inTableFosterParenting_fostersLooseTextOutsideTable() {
        String html = "<table>Loose Text<tr><td>Cell</td></tr></table>";
        Document doc = Jsoup.parse(html);
        assertTrue(doc.body().text().startsWith("Loose Text"));
        assertEquals("Cell", doc.select("table td").text());
    }

    // Tests InSelect and InSelectInTable states handling select and option tags
    @Test
    public void testProcess_inSelectOptions_handlesOptgroupAndOptionProperly() {
        String html = "<select><optgroup label='group1'><option>Opt1<option>Opt2</optgroup><option>Opt3</select>";
        Document doc = Jsoup.parse(html);
        Element select = doc.select("select").first();
        assertNotNull(select);
        assertEquals(1, select.select("optgroup").size());
        assertEquals(3, select.select("option").size());
    }

    // Tests InFrameset and AfterFrameset states
    @Test
    public void testProcess_inFrameset_createsFramesetStructure() {
        String html = "<html><frameset rows='50%,50%'><frame src='frame1.html'><frame src='frame2.html'><noframes><p>No frames</p></noframes></frameset></html>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.select("frameset").first());
        assertEquals(2, doc.select("frame").size());
    }

    // Tests InBody state ignoring nested form tags
    @Test
    public void testProcess_inBodyNestedForms_ignoresSecondForm() {
        String html = "<form id='outer'><input name='one'><form id='inner'><input name='two'></form></form>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("form").size());
        assertEquals("outer", doc.select("form").first().id());
        assertEquals(2, doc.select("input").size());
    }

    // Tests InBody state handling headings auto-closing previous headings
    @Test
    public void testProcess_inBodyHeadingTags_autoClosesPreviousHeading() {
        String html = "<h1>Heading 1<h2>Heading 2</h2></h1>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("h1").size());
        assertEquals(1, doc.select("h2").size());
        assertEquals("Heading 1", doc.select("h1").text());
        assertEquals("Heading 2", doc.select("h2").text());
    }

    // Tests InBody state handling button tag scope
    @Test
    public void testProcess_inBodyButtonScope_autoClosesNestedButton() {
        String html = "<button>Btn1<button>Btn2</button>";
        Document doc = Jsoup.parse(html);
        assertEquals(2, doc.select("button").size());
    }

    // Tests Text state parsing script and textarea tags
    @Test
    public void testProcess_textState_preservesRawContentInScriptAndTextarea() {
        String html = "<script>var x = 1 < 2;</script><textarea>alert('test & string');</textarea>";
        Document doc = Jsoup.parse(html);
        assertEquals("var x = 1 < 2;", doc.select("script").first().data());
        assertEquals("alert('test & string');", doc.select("textarea").first().text());
    }

    // Tests AfterBody and AfterAfterBody state handling comments and trailing tokens
    @Test
    public void testProcess_afterBodyContent_handlesTrailingCommentsAndHtml() {
        String html = "<html><head></head><body>Body Text</body><!-- Trailing Comment --></html><!-- Final Comment -->";
        Document doc = Jsoup.parse(html);
        assertEquals("Body Text", doc.body().text());
        assertTrue(doc.outerHtml().contains("<!-- Trailing Comment -->"));
        assertTrue(doc.outerHtml().contains("<!-- Final Comment -->"));
    }
}