package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Test;

import static org.junit.Assert.*;

public class HtmlTreeBuilderStateTest {

    // Tests parsing document with initial doctype and html structure
    @Test
    public void testInitial_validDoctype_createsDocumentWithDoctype() {
        String html = "<!DOCTYPE html><html><head><title>Test</title></head><body><p>Hello</p></body></html>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.doctype());
        assertEquals("html", doc.doctype().name());
        assertEquals("Hello", doc.select("p").first().text());
    }

    // Tests comment before html element
    @Test
    public void testBeforeHtml_commentBeforeHtml_commentPresentInDocument() {
        String html = "<!-- Comment --><html lang=\"en\"><head></head><body></body></html>";
        Document doc = Jsoup.parse(html);
        assertEquals("en", doc.select("html").first().attr("lang"));
        assertEquals("#comment", doc.childNode(0).nodeName());
    }

    // Tests parsing with missing html and head tags
    @Test
    public void testBeforeHead_implicitTags_generatesHtmlAndHead() {
        String html = "<title>Implicit Head</title><p>Body text</p>";
        Document doc = Jsoup.parse(html);
        assertEquals("Implicit Head", doc.title());
        assertNotNull(doc.head());
        assertNotNull(doc.body());
        assertEquals("Body text", doc.select("p").text());
    }

    // Tests head elements: base, meta, link, script, style, noscript, title
    @Test
    public void testInHead_variousHeadElements_elementsParsedIntoHead() {
        String html = "<html><head>" +
                "<base href=\"http://example.com/\" />" +
                "<meta charset=\"utf-8\" />" +
                "<link rel=\"stylesheet\" href=\"style.css\" />" +
                "<title>Page Title</title>" +
                "<style>body { color: black; }</style>" +
                "<script>var x = 1;</script>" +
                "<noscript><meta http-equiv=\"refresh\" content=\"0\"></noscript>" +
                "</head><body></body></html>";
        Document doc = Jsoup.parse(html);
        assertEquals("http://example.com/", doc.baseUri());
        assertEquals("Page Title", doc.title());
        assertEquals(1, doc.head().select("link").size());
        assertEquals(1, doc.head().select("style").size());
        assertEquals(1, doc.head().select("script").size());
        assertEquals(1, doc.head().select("noscript").size());
    }

    // Tests rawtext and rcdata handling in head
    @Test
    public void testInHead_noframesAndStyle_handledAsRawtext() {
        String html = "<html><head><noframes>&lt;p&gt;No frames&lt;/p&gt;</noframes><style>div { display: none; }</style></head><body><p>Content</p></body></html>";
        Document doc = Jsoup.parse(html);
        assertEquals("Content", doc.select("p").first().text());
        assertNotNull(doc.head().select("style").first());
    }

    // Tests body transition and heading elements auto-closing
    @Test
    public void testInBody_nestedHeadings_autoClosesPreviousHeading() {
        String html = "<h1>Heading 1<h2>Heading 2</h2></h1>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("h1").size());
        assertEquals(1, doc.select("h2").size());
        assertEquals(0, doc.select("h1 h2").size());
    }

    // Tests formatting elements and adoption agency algorithm
    @Test
    public void testInBody_misnestedFormattingTags_reconstructsFormatting() {
        String html = "<p><b>Bold <i>Bold-Italic</b> Italic</i></p>";
        Document doc = Jsoup.parse(html);
        assertEquals("<p><b>Bold <i>Bold-Italic</i></b><i> Italic</i></p>", doc.body().html());
    }

    // Tests list item scope and implicit closure
    @Test
    public void testInBody_nestedListItems_implicitlyClosesPreviousLi() {
        String html = "<ul><li>Item 1<li>Item 2<li>Item 3</ul>";
        Document doc = Jsoup.parse(html);
        assertEquals(3, doc.select("li").size());
        assertEquals(0, doc.select("li > li").size());
    }

    // Tests paragraph auto-closing by block elements
    @Test
    public void testInBody_blockClosesParagraph_paragraphClosedBeforeBlock() {
        String html = "<p>Paragraph 1<div>Div block</div><p>Paragraph 2</p>";
        Document doc = Jsoup.parse(html);
        assertEquals(2, doc.select("p").size());
        assertEquals(1, doc.select("div").size());
        assertEquals(0, doc.select("p > div").size());
    }

    // Tests form handling and nested form prevention
    @Test
    public void testInBody_nestedForms_ignoresInnerForm() {
        String html = "<form id=\"outer\"><input name=\"q1\"/><form id=\"inner\"><input name=\"q2\"/></form></form>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("form").size());
        assertEquals("outer", doc.select("form").first().id());
        assertEquals(2, doc.select("input").size());
    }

    // Tests table structure and foster parenting
    @Test
    public void testInTable_fosterParenting_fostersNonTableContent() {
        String html = "<table>Text outside cell<tr><td>Cell</td></tr></table>";
        Document doc = Jsoup.parse(html);
        assertTrue(doc.body().text().contains("Text outside cell"));
        assertEquals("Cell", doc.select("td").first().text());
    }

    // Tests table tags implicit creation (colgroup, tbody, tr)
    @Test
    public void testInTable_implicitTbodyAndTr_buildsCompleteTableTree() {
        String html = "<table><caption>Caption</caption><colgroup><col /></colgroup><td>Single Cell</td></table>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.select("table > caption").first());
        assertNotNull(doc.select("table > colgroup > col").first());
        assertNotNull(doc.select("table > tbody > tr > td").first());
        assertEquals("Single Cell", doc.select("td").first().text());
    }

    // Tests select and optgroup handling
    @Test
    public void testInSelect_optgroupsAndOptions_parsedProperly() {
        String html = "<select><optgroup label=\"group\"><option>1<option>2</optgroup><option>3</select>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("optgroup").size());
        assertEquals(3, doc.select("option").size());
        assertEquals(2, doc.select("optgroup > option").size());
    }

    // Tests textarea and plaintext elements
    @Test
    public void testInBody_textareaAndPlaintext_retainsTextContent() {
        String html = "<textarea><b>Not bold</b></textarea>";
        Document doc = Jsoup.parse(html);
        assertEquals(0, doc.select("textarea b").size());
        assertEquals("<b>Not bold</b>", doc.select("textarea").first().val());
    }

    // Tests frameset document structure
    @Test
    public void testInFrameset_framesetHtml_createsFramesetElements() {
        String html = "<html><frameset rows=\"50%,50%\"><frame src=\"frame1.html\"><frame src=\"frame2.html\"><noframes><p>No frames</p></noframes></frameset></html>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("frameset").size());
        assertEquals(2, doc.select("frame").size());
        assertEquals(1, doc.select("noframes").size());
    }

    // Tests after body and trailing whitespace/comments
    @Test
    public void testAfterBody_commentsAfterHtml_retainedInDocument() {
        String html = "<html><body><p>Hello</p></body></html><!-- Trailing comment -->";
        Document doc = Jsoup.parse(html);
        assertEquals("Hello", doc.select("p").first().text());
        assertTrue(doc.outerHtml().contains("<!-- Trailing comment -->"));
    }

    // Tests foreign content tags (svg and math)
    @Test
    public void testInBody_svgAndMathElements_properlyInserted() {
        String html = "<div><svg><g><circle cx=\"5\" cy=\"5\" r=\"5\"/></g></svg><math><mi>x</mi></math></div>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("svg circle").size());
        assertEquals(1, doc.select("math mi").size());
    }

    // Tests empty formatter tags like hr, br, img, input
    @Test
    public void testInBody_emptyFormatters_properlyInserted() {
        String html = "<div><hr><br><img src=\"test.jpg\"><input type=\"text\"></div>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("hr").size());
        assertEquals(1, doc.select("br").size());
        assertEquals(1, doc.select("img").size());
        assertEquals(1, doc.select("input").size());
    }

    // Tests enum values and process methods
    @Test
    public void testHtmlTreeBuilderState_enumValues_existAndAreNonNull() {
        for (HtmlTreeBuilderState state : HtmlTreeBuilderState.values()) {
            assertNotNull(state);
        }
        assertEquals(HtmlTreeBuilderState.Initial, HtmlTreeBuilderState.valueOf("Initial"));
        assertEquals(HtmlTreeBuilderState.InBody, HtmlTreeBuilderState.valueOf("InBody"));
        assertEquals(HtmlTreeBuilderState.InTable, HtmlTreeBuilderState.valueOf("InTable"));
    }

    // Additional tests covering branches across all tree builder states

    @Test
    public void testInHeadNoscript_headNoscriptElements_parsedCorrectly() {
        String html = "<html><head><noscript><style>body { color: red; }</style><link rel=\"stylesheet\" href=\"style.css\"></noscript></head><body><p>Content</p></body></html>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.head().select("noscript").size());
        assertEquals(1, doc.head().select("noscript link").size());
        assertEquals(1, doc.head().select("noscript style").size());
        assertEquals("Content", doc.select("p").text());
    }

    @Test
    public void testAfterHead_bodyTagWithAttributes_appliesAttributesToBody() {
        String html = "<html><head><title>Test</title></head><body id=\"main-body\" class=\"container\"><p>Hello</p></body></html>";
        Document doc = Jsoup.parse(html);
        assertEquals("main-body", doc.body().id());
        assertEquals("container", doc.body().className());
    }

    @Test
    public void testAfterHead_unexpectedStartTag_transitionsToInBody() {
        String html = "<html><head><title>Test</title></head><div>Direct Div</div></html>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.body());
        assertEquals("Direct Div", doc.select("body > div").text());
    }

    @Test
    public void testInBody_dlDtDd_nestedDefinitionListsHandled() {
        String html = "<dl><dt>Term 1<dd>Def 1<dt>Term 2<dd>Def 2</dl>";
        Document doc = Jsoup.parse(html);
        assertEquals(2, doc.select("dt").size());
        assertEquals(2, doc.select("dd").size());
        assertEquals(0, doc.select("dt > dd").size());
        assertEquals(0, doc.select("dd > dt").size());
    }

    @Test
    public void testInBody_nestedAnchorTags_closesPreviousAnchor() {
        String html = "<a>Anchor 1 <a>Anchor 2</a></a>";
        Document doc = Jsoup.parse(html);
        assertEquals(2, doc.select("a").size());
        assertEquals(0, doc.select("a > a").size());
    }

    @Test
    public void testInBody_buttonWithNestedElements_buttonScopedProperly() {
        String html = "<button><p>Paragraph inside button</p></button>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("button").size());
        assertEquals(1, doc.select("button > p").size());
    }

    @Test
    public void testInBody_plaintextTag_consumesRemainingInputAsText() {
        String html = "<div>Before</div><plaintext><b>Not bold</b><div>Not a div</div>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("div").size());
        assertEquals("Before", doc.select("div").first().text());
        assertNotNull(doc.select("plaintext").first());
        assertTrue(doc.select("plaintext").first().text().contains("<b>Not bold</b>"));
    }

    @Test
    public void testInTable_theadTbodyTfoot_orderedCorrectly() {
        String html = "<table><tfoot><tr><td>Foot</td></tr></tfoot><thead><tr><th>Head</th></tr></thead><tbody><tr><td>Body</td></tr></tbody></table>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.select("table > thead").first());
        assertNotNull(doc.select("table > tbody").first());
        assertNotNull(doc.select("table > tfoot").first());
        assertEquals("Head", doc.select("thead th").text());
        assertEquals("Body", doc.select("tbody td").text());
        assertEquals("Foot", doc.select("tfoot td").text());
    }

    @Test
    public void testInRow_unexpectedTagsInRow_fosteredOrHandled() {
        String html = "<table><tr>Text in tr<td>Cell</td></tr></table>";
        Document doc = Jsoup.parse(html);
        assertTrue(doc.body().text().contains("Text in tr"));
        assertEquals("Cell", doc.select("td").text());
    }

    @Test
    public void testInCell_nestedBlocksInCell_stayWithinCell() {
        String html = "<table><tr><td><div>Div in cell</div><p>P in cell</p></td></tr></table>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("td > div").size());
        assertEquals(1, doc.select("td > p").size());
    }

    @Test
    public void testInSelectInTable_selectInsideTable_parsedProperly() {
        String html = "<table><tr><td><select><option value=\"1\">One</option><option value=\"2\">Two</option></select></td></tr></table>";
        Document doc = Jsoup.parse(html);
        assertEquals(2, doc.select("select option").size());
        assertEquals("One", doc.select("option").first().text());
    }

    @Test
    public void testInTemplate_templateTag_parsedIntoTemplateContent() {
        String html = "<div><template id=\"my-template\"><p>Template Paragraph</p></template></div>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.select("template#my-template").first());
        assertEquals(1, doc.select("template").size());
    }

    @Test
    public void testAfterAfterBody_contentAfterClosingHtml_fosteredOrAppended() {
        String html = "<!DOCTYPE html><html><head></head><body><p>Body</p></body></html><!-- Trailing Comment --><p>After HTML</p>";
        Document doc = Jsoup.parse(html);
        assertEquals(2, doc.select("p").size());
        assertTrue(doc.outerHtml().contains("<!-- Trailing Comment -->"));
    }

    @Test
    public void testAfterFrameset_trailingCommentAndNoframes_handledCorrectly() {
        String html = "<html><frameset rows=\"*\"><frame src=\"frame.html\"></frameset></html><!-- Trailing -->";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("frameset").size());
        assertEquals(1, doc.select("frame").size());
        assertTrue(doc.outerHtml().contains("<!-- Trailing -->"));
    }

    @Test
    public void testInBody_iframeAndNoembed_parsedCorrectly() {
        String html = "<div><iframe src=\"page.html\"><p>Fallback iframe</p></iframe><noembed><p>Fallback noembed</p></noembed></div>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("iframe").size());
        assertEquals(1, doc.select("noembed").size());
    }

    @Test
    public void testInBody_preAndListingTags_retainWhitespace() {
        String html = "<pre>\nLine 1\nLine 2</pre><listing>\nListing 1\nListing 2</listing>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("pre").size());
        assertEquals(1, doc.select("listing").size());
    }
}