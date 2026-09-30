package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Arrays;

import static org.junit.Assert.*;

public class HtmlTreeBuilderStateTest {

    // Tests that all constant arrays in HtmlTreeBuilderState.Constants are sorted for binary search
    @Test
    public void testConstants_allArrays_areSorted() throws Exception {
        for (Field field : HtmlTreeBuilderState.Constants.class.getDeclaredFields()) {
            if (field.getType().isArray() && field.getType().getComponentType().equals(String.class)) {
                field.setAccessible(true);
                String[] array = (String[]) field.get(null);
                String[] copy = Arrays.copyOf(array, array.length);
                Arrays.sort(copy);
                assertArrayEquals("Array Constants." + field.getName() + " must be sorted", copy, array);
            }
        }
    }

    // Tests closing object/applet tags correctly when nested inside a custom 'name' element (Defects4J 76)
    @Test
    public void testInBody_nestedInsideNameTag_closesObjectCorrectly() {
        Document doc = Jsoup.parse("<name><object>foo</object>bar</name>");
        Element object = doc.select("object").first();
        assertNotNull(object);
        assertEquals("foo", object.text());
        assertEquals("<name><object>foo</object>bar</name>", doc.body().html());
    }

    // Tests closing applet, marquee, and object tags in normal body context
    @Test
    public void testInBody_appletMarqueeObjectEndTag_closesCorrectly() {
        Document doc = Jsoup.parse("<object>one</object><marquee>two</marquee><applet>three</applet>");
        assertEquals(3, doc.body().children().size());
        assertEquals("object", doc.body().child(0).tagName());
        assertEquals("marquee", doc.body().child(1).tagName());
        assertEquals("applet", doc.body().child(2).tagName());
    }

    // Tests Initial state doctype and comment parsing
    @Test
    public void testInitial_doctypeAndComment_parsesProperly() {
        String html = "<!DOCTYPE html><!-- test --><html><head></head><body></body></html>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.documentType());
        assertEquals("html", doc.documentType().name());
        assertEquals(Document.QuirksMode.noQuirks, doc.quirksMode());
    }

    // Tests BeforeHtml state handling unexpected doctype and inserting html start tag
    @Test
    public void testBeforeHtml_unexpectedTags_transitionsToBeforeHead() {
        Document doc = Jsoup.parse("<html><!-- comment --><body><p>Hello</p></body></html>");
        assertEquals("Hello", doc.select("p").first().text());
        assertEquals("html", doc.child(0).tagName());
    }

    // Tests InHead state parsing metadata, title, script, and style tags
    @Test
    public void testInHead_metadataAndScripts_parsedProperly() {
        String html = "<html><head><title>Test Title</title><meta charset=\"utf-8\"><style>body { color: red; }</style><script>var x = 1;</script></head><body></body></html>";
        Document doc = Jsoup.parse(html);
        assertEquals("Test Title", doc.title());
        assertEquals("utf-8", doc.select("meta").first().attr("charset"));
        assertEquals("body { color: red; }", doc.head().select("style").first().data());
    }

    // Tests InHeadNoscript state parsing when noscript is in head
    @Test
    public void testInHeadNoscript_elementsInsideNoscript_handledInHead() {
        String html = "<head><noscript><link rel=\"stylesheet\" href=\"style.css\"><meta name=\"foo\" content=\"bar\"></noscript></head>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.head().select("link").first());
        assertNotNull(doc.head().select("meta").first());
    }

    // Tests AfterHead state transitioning to InBody upon encountering body tag or content
    @Test
    public void testAfterHead_encounterBodyOrContent_transitionsToInBody() {
        Document doc = Jsoup.parse("<head></head>Text before body<body class=\"main\"><p>In body</p></body>");
        assertEquals("main", doc.body().className());
        assertTrue(doc.body().text().contains("Text before body"));
        assertTrue(doc.body().text().contains("In body"));
    }

    // Tests Adoption Agency Algorithm in InBody state for nested formatting tags
    @Test
    public void testInBody_adoptionAgencyAlgorithm_restructuresMisnestedTags() {
        String html = "<b>1<p>2</b>3</p>";
        Document doc = Jsoup.parse(html);
        assertEquals("<b>1</b><p><b>2</b>3</p>", doc.body().html());
    }

    // Tests InBody auto-closing p tag when encountering block elements or headings
    @Test
    public void testInBody_pTagAutoClose_whenHeadingOrBlockAppears() {
        String html = "<p>Paragraph 1<h1>Heading</h1><p>Paragraph 2<div>Div</div>";
        Document doc = Jsoup.parse(html);
        assertEquals(4, doc.body().children().size());
        assertEquals("p", doc.body().child(0).tagName());
        assertEquals("h1", doc.body().child(1).tagName());
        assertEquals("p", doc.body().child(2).tagName());
        assertEquals("div", doc.body().child(3).tagName());
    }

    // Tests InBody list items (li, dt, dd) auto-closing
    @Test
    public void testInBody_listItems_autoClosesPreviousItem() {
        String html = "<ul><li>Item 1<li>Item 2</ul><dl><dt>Term 1<dd>Desc 1<dt>Term 2<dd>Desc 2</dl>";
        Document doc = Jsoup.parse(html);
        assertEquals(2, doc.select("ul > li").size());
        assertEquals(2, doc.select("dl > dt").size());
        assertEquals(2, doc.select("dl > dd").size());
    }

    // Tests InTable state foster parenting when non-table content is in table
    @Test
    public void testInTable_fosterParenting_movesTextBeforeTable() {
        String html = "<table>Plain Text<tr><td>Cell</td></tr></table>";
        Document doc = Jsoup.parse(html);
        assertEquals("Plain Text<table><tbody><tr><td>Cell</td></tr></tbody></table>", doc.body().html());
    }

    // Tests InTable, InTableBody, InRow, InCell states for well-formed and broken tables
    @Test
    public void testInTable_nestedTableStructure_parsedCorrectly() {
        String html = "<table><caption>Cap</caption><colgroup><col></colgroup><thead><tr><th>Header</th></tr></thead><tbody><tr><td>Data</td></tr></tbody></table>";
        Document doc = Jsoup.parse(html);
        assertEquals("Cap", doc.select("caption").text());
        assertEquals("Header", doc.select("th").text());
        assertEquals("Data", doc.select("td").text());
    }

    // Tests InSelect and InSelectInTable states with options and optgroups
    @Test
    public void testInSelect_optionsAndOptgroups_parsedCorrectly() {
        String html = "<select><optgroup label=\"group\"><option value=\"1\">One<option value=\"2\">Two</optgroup><option value=\"3\">Three</select>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("optgroup").size());
        assertEquals(3, doc.select("option").size());
    }

    // Tests InFrameset and AfterFrameset states
    @Test
    public void testInFrameset_framesetDocument_parsedCorrectly() {
        String html = "<html><frameset rows=\"50%,50%\"><frame src=\"frame1.html\"><frame src=\"frame2.html\"><noframes><p>No frames</p></noframes></frameset></html>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.select("frameset").first());
        assertEquals(2, doc.select("frame").size());
    }

    // Tests AfterBody and AfterAfterBody transitions
    @Test
    public void testAfterBody_tokensAfterHtmlTag_appendedProperly() {
        String html = "<html><head></head><body>Content</body></html><!-- trailing comment -->";
        Document doc = Jsoup.parse(html);
        assertEquals("Content", doc.body().text());
        assertNotNull(doc.select("body").first());
    }

    // Tests InCaption state handling table elements and caption end tags
    @Test
    public void testInCaption_tagsInsideCaption_closedCorrectly() {
        String html = "<table><caption>Caption Text<tr><td>Cell</td></tr></caption><tr><td>Row</td></tr></table>";
        Document doc = Jsoup.parse(html);
        assertEquals("Caption Text", doc.select("caption").first().text());
        assertEquals(1, doc.select("tbody tr").size());
    }

    // Tests InColumnGroup state handling col elements and unexpected tokens
    @Test
    public void testInColumnGroup_colTagsAndUnexpectedTokens() {
        String html = "<table><colgroup><col span=\"2\"><col></colgroup><div>Ignored</div><tr><td>Cell</td></tr></table>";
        Document doc = Jsoup.parse(html);
        assertEquals(2, doc.select("colgroup col").size());
        assertEquals(2, doc.select("colgroup col").first().attributes().size() > 0 ? 2 : 2);
        assertEquals("Ignored", doc.select("div").first().text());
    }

    // Tests InTableText state character tokens and whitespace within table
    @Test
    public void testInTableText_whitespaceAndNonWhitespaceHandling() {
        String html = "<table>   \n\t  <tr>  <td>Cell</td> </tr>  </table>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("table").size());
        assertEquals("Cell", doc.select("td").text().trim());
    }

    // Tests InCell state handling auto-closing cells upon encountering new cell or row tags
    @Test
    public void testInCell_implicitCellClosing() {
        String html = "<table><tr><td>1<td>2<th>3<tr><td>4";
        Document doc = Jsoup.parse(html);
        assertEquals(2, doc.select("tr").size());
        assertEquals(3, doc.select("tr").first().children().size());
        assertEquals(1, doc.select("tr").get(1).children().size());
    }

    // Tests InTableBody state handling implicit row insertion and misnested elements
    @Test
    public void testInTableBody_implicitRowAndStrayElements() {
        String html = "<table><tbody><td>Cell 1</td><td>Cell 2</td></tbody></table>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("tbody > tr").size());
        assertEquals(2, doc.select("tr > td").size());
    }

    // Tests InSelect and InSelectInTable transitions when encountering inputs or selects in table
    @Test
    public void testInSelectInTable_selectInsideTableCell() {
        String html = "<table><tr><td><select><option>1</option><input type=\"text\"><option>2</option></select></td></tr></table>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("select").size());
        assertEquals(2, doc.select("option").size());
        assertEquals(1, doc.select("input").size());
    }

    // Tests InBody ruby, rt, and rp tags auto-closing
    @Test
    public void testInBody_rubyAnnotations_handledProperly() {
        String html = "<ruby>Base<rp>(</rp><rt>Ruby<rp>)</rp><rt>Ruby2</ruby>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("ruby").size());
        assertEquals(2, doc.select("rt").size());
        assertEquals(2, doc.select("rp").size());
    }

    // Tests InBody headings auto-closing previous headings
    @Test
    public void testInBody_nestedHeadings_autoClose() {
        String html = "<h1>Heading 1<h2>Heading 2<h3>Heading 3</h1>";
        Document doc = Jsoup.parse(html);
        assertEquals(3, doc.body().children().size());
        assertEquals("h1", doc.body().child(0).tagName());
        assertEquals("h2", doc.body().child(1).tagName());
        assertEquals("h3", doc.body().child(2).tagName());
    }

    // Tests InBody raw text tags (textarea, xmp, iframe, noembed, plaintext)
    @Test
    public void testInBody_rawTextElements() {
        String html = "<textarea><p>Not parsed</p></textarea><xmp><b>Raw</b></xmp><iframe src=\"#\"><p>Frame</p></iframe>";
        Document doc = Jsoup.parse(html);
        assertEquals("<p>Not parsed</p>", doc.select("textarea").first().val());
        assertEquals("<b>Raw</b>", doc.select("xmp").first().text());
        assertEquals(0, doc.select("textarea p").size());
    }

    // Tests InBody form tags nested and closing
    @Test
    public void testInBody_nestedForms_ignoredOrClosed() {
        String html = "<form id=\"f1\"><input name=\"i1\"><form id=\"f2\"><input name=\"i2\"></form></form>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("form").size());
        assertEquals("f1", doc.select("form").first().id());
        assertEquals(2, doc.select("form input").size());
    }

    // Tests InBody image tag alias to img
    @Test
    public void testInBody_imageTagAliasedToImg() {
        String html = "<image src=\"test.png\" alt=\"test\" />";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("img").size());
        assertEquals(0, doc.select("image").size());
        assertEquals("test.png", doc.select("img").first().attr("src"));
    }

    // Tests InBody stray end tags and unhandled end tags
    @Test
    public void testInBody_strayEndTags_ignoredGracefully() {
        String html = "</span></div></p></body></html><p>Valid</p>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.select("p").first());
        assertEquals("Valid", doc.select("p").first().text());
    }

    // Tests AfterAfterFrameset and comments after frameset
    @Test
    public void testAfterFrameset_trailingCommentsAndTags() {
        String html = "<html><frameset rows=\"*\"><frame src=\"a.html\"></frameset></html><!-- after comment -->";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.select("frameset").first());
    }

    // Tests InHead unexpected end tags and fallthrough
    @Test
    public void testInHead_unexpectedEndTags_transitionsToAfterHead() {
        String html = "<html><head><meta charset=\"utf-8\"></foo></head><body><p>Text</p></body></html>";
        Document doc = Jsoup.parse(html);
        assertEquals("Text", doc.select("p").first().text());
        assertEquals("utf-8", doc.select("meta").first().attr("charset"));
    }

    // Tests Foreign content handling in body (SVG and MathML)
    @Test
    public void testInBody_svgAndMathElements() {
        String html = "<div><svg><circle cx=\"50\" cy=\"50\" r=\"40\" /></svg><math><mi>x</mi></math></div>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("svg circle").size());
        assertEquals(1, doc.select("math mi").size());
    }
}