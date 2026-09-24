package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.DocumentType;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.junit.Test;

import static org.junit.Assert.*;

public class HtmlTreeBuilderStateTest {

    // Tests defect 38: image tag inside svg should not be converted to img
    @Test
    public void testProcess_imageInSvg_preservesImageTag() {
        String html = "<svg><image href=\"test.png\"/></svg>";
        Document doc = Jsoup.parse(html);
        Element svg = doc.select("svg").first();
        assertNotNull(svg);
        Element image = svg.select("image").first();
        assertNotNull(image);
        assertEquals("image", image.nodeName());
        assertNull(doc.select("img").first());
    }

    // Tests normal case: image tag outside svg is converted to img in InBody
    @Test
    public void testProcess_imageOutsideSvg_convertsToImg() {
        String html = "<div><image src=\"test.png\" alt=\"photo\"></div>";
        Document doc = Jsoup.parse(html);
        Element img = doc.select("img").first();
        assertNotNull(img);
        assertEquals("img", img.nodeName());
        assertEquals("test.png", img.attr("src"));
        assertNull(doc.select("image").first());
    }

    // Tests Initial state processing doctype and quirks mode
    @Test
    public void testInitial_doctypeDeclaration_setsQuirksAndDoctype() {
        Document doc = Jsoup.parse("<!DOCTYPE html><html><head></head><body></body></html>");
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

    // Tests BeforeHead and InHead states handling metadata and title
    @Test
    public void testInHead_metadataAndTitle_correctlyParsed() {
        String html = "<html><head><title>Test Title</title><meta charset=\"UTF-8\"><link rel=\"stylesheet\" href=\"style.css\"></head><body></body></html>";
        Document doc = Jsoup.parse(html);
        assertEquals("Test Title", doc.title());
        assertEquals("UTF-8", doc.select("meta").first().attr("charset"));
        assertEquals("style.css", doc.select("link").first().attr("href"));
    }

    // Tests InHeadNoscript state and fallback
    @Test
    public void testInHeadNoscript_noscriptElements_processedInHead() {
        String html = "<head><noscript><link rel=\"stylesheet\" href=\"fallback.css\"><style>body{color:red;}</style></noscript></head>";
        Document doc = Jsoup.parse(html);
        Element link = doc.select("head noscript link").first();
        assertNotNull(link);
        assertEquals("fallback.css", link.attr("href"));
    }

    // Tests AfterHead state transitioning to InBody on normal content
    @Test
    public void testAfterHead_implicitBody_createdOnTextToken() {
        String html = "<html><head></head>Hello World</html>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.body());
        assertEquals("Hello World", doc.body().text());
    }

    // Tests InBody heading elements auto-closing previous headings
    @Test
    public void testInBody_nestedHeadings_autoClosePreviousHeading() {
        String html = "<h1>Heading 1<h2>Heading 2</h2></h1>";
        Document doc = Jsoup.parse(html);
        Elements h1 = doc.select("h1");
        Elements h2 = doc.select("h2");
        assertEquals(1, h1.size());
        assertEquals(1, h2.size());
        assertEquals("Heading 1", h1.first().text());
        assertEquals("Heading 2", h2.first().text());
        assertFalse(h1.first().children().contains(h2.first()));
    }

    // Tests InBody list items (li) auto-closing previous li
    @Test
    public void testInBody_nestedListItems_autoClose() {
        String html = "<ul><li>Item 1<li>Item 2<li>Item 3</ul>";
        Document doc = Jsoup.parse(html);
        Elements items = doc.select("ul > li");
        assertEquals(3, items.size());
        assertEquals("Item 1", items.get(0).text());
        assertEquals("Item 2", items.get(1).text());
        assertEquals("Item 3", items.get(2).text());
    }

    // Tests InBody Adoption Agency Algorithm with formatting tags
    @Test
    public void testInBody_adoptionAgencyAlgorithm_reconstructsFormatting() {
        String html = "<b>1<p>2</b>3</p>";
        Document doc = Jsoup.parse(html);
        assertEquals("<b>1</b><p><b>2</b>3</p>", doc.body().html().replaceAll("\\r?\\n", ""));
    }

    // Tests InBody form and input parsing
    @Test
    public void testInBody_formAndInputElements_parsedCorrectly() {
        String html = "<form action=\"/submit\"><input type=\"text\" name=\"user\"/><input type=\"hidden\" name=\"token\" value=\"123\"/></form>";
        Document doc = Jsoup.parse(html);
        Element form = doc.select("form").first();
        assertNotNull(form);
        assertEquals("/submit", form.attr("action"));
        assertEquals(2, form.select("input").size());
    }

    // Tests InBody isindex tag legacy conversion
    @Test
    public void testInBody_isindexTag_convertsToFormWithInput() {
        String html = "<body><isindex action=\"/search\" prompt=\"Search:\"></body>";
        Document doc = Jsoup.parse(html);
        Element form = doc.select("form").first();
        assertNotNull(form);
        assertEquals("/search", form.attr("action"));
        Element input = form.select("input[name=isindex]").first();
        assertNotNull(input);
    }

    // Tests InTable state and foster parenting of non-table elements
    @Test
    public void testInTable_fosterParenting_movesMisplacedTextAndTagsBeforeTable() {
        String html = "<table>Misplaced Text<b>Bold</b><tr><td>Cell</td></tr></table>";
        Document doc = Jsoup.parse(html);
        Element body = doc.body();
        assertTrue(body.text().startsWith("Misplaced TextBold"));
        assertEquals(1, doc.select("table td").size());
        assertEquals("Cell", doc.select("table td").first().text());
    }

    // Tests InTableBody and InRow states handling table cells
    @Test
    public void testInTableBody_implicitRowsAndCells_constructed() {
        String html = "<table><tbody><tr><td>Cell 1<td>Cell 2<tr><th>Header 1</th></tr></tbody></table>";
        Document doc = Jsoup.parse(html);
        Elements rows = doc.select("table tbody tr");
        assertEquals(2, rows.size());
        assertEquals(2, rows.get(0).select("td").size());
        assertEquals(1, rows.get(1).select("th").size());
    }

    // Tests InCaption state handling caption content and transition back to table
    @Test
    public void testInCaption_tableCaption_parsedCorrectly() {
        String html = "<table><caption>Table <b>Caption</b></caption><tr><td>Data</td></tr></table>";
        Document doc = Jsoup.parse(html);
        Element caption = doc.select("table caption").first();
        assertNotNull(caption);
        assertEquals("Table Caption", caption.text());
        assertEquals("Data", doc.select("table td").first().text());
    }

    // Tests InSelect and InSelectInTable states
    @Test
    public void testInSelect_optionsAndOptgroups_structured() {
        String html = "<select name=\"choice\"><optgroup label=\"G1\"><option value=\"1\">One<option value=\"2\">Two</optgroup><option value=\"3\">Three</select>";
        Document doc = Jsoup.parse(html);
        Element select = doc.select("select").first();
        assertNotNull(select);
        assertEquals(1, select.select("optgroup").size());
        assertEquals(3, select.select("option").size());
    }

    // Tests InFrameset and AfterFrameset states
    @Test
    public void testInFrameset_frameAndNoframes_handled() {
        String html = "<html><frameset cols=\"50%,50%\"><frame src=\"frame1.html\"><frame src=\"frame2.html\"><noframes><body>No frames</body></noframes></frameset></html>";
        Document doc = Jsoup.parse(html);
        Element frameset = doc.select("frameset").first();
        assertNotNull(frameset);
        assertEquals(2, frameset.select("frame").size());
        assertEquals("50%,50%", frameset.attr("cols"));
    }

    // Tests Text state parsing raw text elements like script, style, textarea
    @Test
    public void testText_rawTextInScriptAndTextarea_preservedWithoutSubElements() {
        String html = "<script>var a = '<b>not markup</b>';</script><textarea><p>plain text</p></textarea>";
        Document doc = Jsoup.parse(html);
        assertEquals("var a = '<b>not markup</b>';", doc.select("script").first().data());
        assertEquals("<p>plain text</p>", doc.select("textarea").first().text());
    }

    // Tests button scope handling in InBody
    @Test
    public void testInBody_nestedButton_autoClosesPreviousButton() {
        String html = "<button>Button 1<button>Button 2</button>";
        Document doc = Jsoup.parse(html);
        Elements buttons = doc.select("button");
        assertEquals(2, buttons.size());
        assertEquals("Button 1", buttons.get(0).text());
        assertEquals("Button 2", buttons.get(1).text());
    }
}