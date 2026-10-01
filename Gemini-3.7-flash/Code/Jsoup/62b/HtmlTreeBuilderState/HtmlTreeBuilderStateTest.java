package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;

import static org.junit.Assert.*;

public class HtmlTreeBuilderStateTest {

    // Tests defect 62b: anyOtherEndTag with preserved case tags in InBody
    @Test
    public void testInBodyAnyOtherEndTag_casePreservedTag_closesCorrectly() {
        String html = "<div><rAgE>test</rAgE><p>after</p></div>";
        Parser parser = Parser.htmlParser().settings(ParseSettings.preserveCase);
        Document doc = parser.parseInput(html, "");
        Element rage = doc.select("rAgE").first();
        assertNotNull(rage);
        assertEquals("test", rage.text());
        Element p = doc.select("p").first();
        assertNotNull(p);
        assertEquals("after", p.text());
        assertEquals("div", p.parent().nodeName());
    }

    // Tests Initial state doctype and comment parsing
    @Test
    public void testInitial_doctypeAndComment_parsedProperly() {
        String html = "<!-- comment --><!DOCTYPE html><html><head></head><body></body></html>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.doctype());
        assertEquals("html", doc.doctype().name());
        assertEquals(Document.QuirksMode.noQuirks, doc.quirksMode());
    }

    // Tests BeforeHtml state transitions and comments before html
    @Test
    public void testBeforeHtml_whitespaceAndComments_handledCorrectly() {
        String html = "   <!-- before html -->\n<html><head></head><body></body></html>";
        Document doc = Jsoup.parse(html);
        assertEquals("html", doc.child(0).nodeName());
    }

    // Tests InHead state with meta, base, link, style, title, script
    @Test
    public void testInHead_variousHeadElements_insertedCorrectly() {
        String html = "<html><head><title>Test Title</title><base href=\"http://example.com/\"><link rel=\"stylesheet\" href=\"style.css\"><style>body { color: red; }</style><script>var x = 1;</script><noscript>No script</noscript><meta charset=\"UTF-8\"></head><body></body></html>";
        Document doc = Jsoup.parse(html);
        assertEquals("Test Title", doc.title());
        assertEquals("http://example.com/", doc.baseUri());
        assertEquals(1, doc.head().select("link").size());
        assertEquals(1, doc.head().select("style").size());
        assertEquals(1, doc.head().select("script").size());
        assertEquals(1, doc.head().select("meta").size());
    }

    // Tests AfterHead state transition and auto body creation
    @Test
    public void testAfterHead_textOrTag_transitionsToInBody() {
        String html = "<html><head></head>Hello World<div>content</div></html>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.body());
        assertTrue(doc.body().text().contains("Hello World"));
        assertEquals(1, doc.select("div").size());
    }

    // Tests InBody formatting adoption agency algorithm
    @Test
    public void testInBody_adoptionAgencyAlgorithm_reconstructsTree() {
        String html = "<p><b>bold <i>italic</b> normal</i></p>";
        Document doc = Jsoup.parse(html);
        Elements bTags = doc.select("b");
        Elements iTags = doc.select("i");
        assertFalse(bTags.isEmpty());
        assertFalse(iTags.isEmpty());
    }

    // Tests InBody paragraph closing when block elements are encountered
    @Test
    public void testInBody_blockElementsCloseP_properStructure() {
        String html = "<p>para1<div>div content</div><p>para2</p>";
        Document doc = Jsoup.parse(html);
        assertEquals(2, doc.select("p").size());
        assertEquals(1, doc.select("div").size());
        assertEquals("div content", doc.select("div").first().text());
    }

    // Tests InBody list item (li) auto-closing
    @Test
    public void testInBody_listItems_autoClosed() {
        String html = "<ul><li>Item 1<li>Item 2<li>Item 3</ul>";
        Document doc = Jsoup.parse(html);
        Elements lis = doc.select("li");
        assertEquals(3, lis.size());
        assertEquals("Item 1", lis.get(0).text());
        assertEquals("Item 2", lis.get(1).text());
        assertEquals("Item 3", lis.get(2).text());
    }

    // Tests InBody nested headings auto-closing
    @Test
    public void testInBody_headings_autoClosed() {
        String html = "<h1>Heading 1<h2>Heading 2</h2></h1>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("h1").size());
        assertEquals(1, doc.select("h2").size());
        assertEquals("Heading 1", doc.select("h1").first().text());
    }

    // Tests InBody form, input, and textarea elements
    @Test
    public void testInBody_formsAndInputs_handledProperly() {
        String html = "<form action=\"/submit\"><input type=\"text\" name=\"user\"><textarea>text content</textarea><button type=\"submit\">Send</button></form>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("form").size());
        assertEquals(1, doc.select("input").size());
        assertEquals(1, doc.select("textarea").size());
        assertEquals("text content", doc.select("textarea").first().text());
        assertEquals(1, doc.select("button").size());
    }

    // Tests InTable parsing with caption, colgroup, thead, tbody, tr, and td
    @Test
    public void testInTable_completeTableStructure_parsedCorrectly() {
        String html = "<table><caption>Cap</caption><colgroup><col></colgroup><thead><tr><th>H1</th></tr></thead><tbody><tr><td>D1</td></tr></tbody><tfoot><tr><td>F1</td></tr></tfoot></table>";
        Document doc = Jsoup.parse(html);
        assertEquals("Cap", doc.select("caption").text());
        assertEquals(1, doc.select("col").size());
        assertEquals("H1", doc.select("th").text());
        assertEquals(2, doc.select("td").size());
    }

    // Tests InTable foster parenting for misplaced text and tags
    @Test
    public void testInTable_misplacedContent_fosterParented() {
        String html = "<table>misplaced text<b>bold misplaced</b><tr><td>valid cell</td></tr></table>";
        Document doc = Jsoup.parse(html);
        assertTrue(doc.body().text().contains("misplaced text"));
        assertEquals("valid cell", doc.select("table td").text());
    }

    // Tests InSelect and InSelectInTable states
    @Test
    public void testInSelect_optionsAndOptgroups_parsedCorrectly() {
        String html = "<select><optgroup label=\"Group 1\"><option value=\"1\">One</option></optgroup><option value=\"2\" selected>Two</option></select>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("select").size());
        assertEquals(1, doc.select("optgroup").size());
        assertEquals(2, doc.select("option").size());
        assertEquals("Two", doc.select("option[selected]").text());
    }

    // Tests InFrameset, frame, and noframes handling
    @Test
    public void testInFrameset_framesetDocument_handledProperly() {
        String html = "<html><frameset rows=\"50%,50%\"><frame src=\"frame1.html\"><frame src=\"frame2.html\"><noframes><p>No frames</p></noframes></frameset></html>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("frameset").size());
        assertEquals(2, doc.select("frame").size());
        assertEquals(1, doc.select("noframes").size());
    }

    // Tests span, nobr, and formatting tags short circuits
    @Test
    public void testInBody_spanAndNobr_handledProperly() {
        String html = "<div><span>span text</span><nobr>nobr text</nobr></div>";
        Document doc = Jsoup.parse(html);
        assertEquals("span text", doc.select("span").text());
        assertEquals("nobr text", doc.select("nobr").text());
    }

    // Tests SVG and MathML foreign content elements
    @Test
    public void testInBody_svgAndMath_insertedCorrectly() {
        String html = "<div><svg><circle cx=\"50\" cy=\"50\" r=\"40\" /></svg><math><mi>x</mi></math></div>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("svg").size());
        assertEquals(1, doc.select("circle").size());
        assertEquals(1, doc.select("math").size());
        assertEquals(1, doc.select("mi").size());
    }

    // Tests HtmlTreeBuilderState values and valueOf enum methods
    @Test
    public void testHtmlTreeBuilderState_enumValues_nonEmpty() {
        HtmlTreeBuilderState[] states = HtmlTreeBuilderState.values();
        assertTrue(states.length > 0);
        assertEquals(HtmlTreeBuilderState.Initial, HtmlTreeBuilderState.valueOf("Initial"));
        assertEquals(HtmlTreeBuilderState.InBody, HtmlTreeBuilderState.valueOf("InBody"));
    }
}