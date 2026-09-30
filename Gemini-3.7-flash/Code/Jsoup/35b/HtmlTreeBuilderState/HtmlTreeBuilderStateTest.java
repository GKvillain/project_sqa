package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;

import static org.junit.Assert.*;

public class HtmlTreeBuilderStateTest {

    // Tests unclosed anchor tags across block boundaries (Defects4J bug 35)
    @Test
    public void testProcess_unclosedAnchorsInSeparateParagraphs_correctlyConstructsTree() {
        String html = "<p><a href=\"#\">Something to spend on</a></p>\n" +
                "<p><a href=\"#\">Deep inner <i>something</i> </a></p>";
        Document doc = Jsoup.parse(html);
        Elements anchors = doc.select("a");
        assertEquals(2, anchors.size());
        assertEquals("Something to spend on", anchors.get(0).text());
        assertEquals("Deep inner something", anchors.get(1).text());
    }

    // Tests unclosed anchor followed immediately by another anchor tag
    @Test
    public void testProcess_adjacentUnclosedAnchors_properlyClosed() {
        String html = "<a href='1'>First <a href='2'>Second</a>";
        Document doc = Jsoup.parse(html);
        Elements anchors = doc.select("a");
        assertEquals(2, anchors.size());
        assertEquals("First ", anchors.get(0).text());
        assertEquals("Second", anchors.get(1).text());
        assertEquals("1", anchors.get(0).attr("href"));
        assertEquals("2", anchors.get(1).attr("href"));
    }

    // Tests Initial, BeforeHtml, BeforeHead, and InHead states parsing
    @Test
    public void testProcess_headAndMetadataTags_parsedIntoHead() {
        String html = "<!DOCTYPE html><!-- comment --><html><head>" +
                "<title>Test Title</title>" +
                "<meta charset=\"utf-8\">" +
                "<base href=\"http://example.com/\">" +
                "<link rel=\"stylesheet\" href=\"style.css\">" +
                "<style>body { background: red; }</style>" +
                "<script>var x = 1;</script>" +
                "</head><body>Content</body></html>";
        Document doc = Jsoup.parse(html);
        assertEquals("Test Title", doc.title());
        assertNotNull(doc.head().select("meta").first());
        assertEquals("http://example.com/", doc.baseUri());
        assertEquals("Content", doc.body().text());
    }

    // Tests Adoption Agency Algorithm with misnested inline formatting elements in InBody
    @Test
    public void testProcess_misnestedFormattingElements_reconstructedCorrectly() {
        String html = "<p><b>Bold <i>and italic</b> italic only</i></p>";
        Document doc = Jsoup.parse(html);
        Element p = doc.select("p").first();
        assertNotNull(p);
        assertEquals("<b>Bold <i>and italic</i></b><i> italic only</i>", p.html().replace("\r", ""));
    }

    // Tests heading elements auto-closing earlier headings in InBody
    @Test
    public void testProcess_consecutiveHeadingTags_autoClosesPrecedingHeading() {
        String html = "<h1>Heading 1<h2>Heading 2<h3>Heading 3</h3>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("h1").size());
        assertEquals(1, doc.select("h2").size());
        assertEquals(1, doc.select("h3").size());
        assertEquals("Heading 1", doc.select("h1").first().text());
        assertEquals("Heading 2", doc.select("h2").first().text());
        assertEquals("Heading 3", doc.select("h3").first().text());
    }

    // Tests list items (li, dt, dd) auto-closing in InBody
    @Test
    public void testProcess_listItemsWithoutClosingTags_autoClosesPrecedingItem() {
        String html = "<ul><li>One<li>Two<li>Three</ul><dl><dt>Term<dd>Definition</dl>";
        Document doc = Jsoup.parse(html);
        assertEquals(3, doc.select("li").size());
        assertEquals(1, doc.select("dt").size());
        assertEquals(1, doc.select("dd").size());
        assertEquals("One", doc.select("li").get(0).text());
        assertEquals("Two", doc.select("li").get(1).text());
        assertEquals("Three", doc.select("li").get(2).text());
    }

    // Tests table states (InTable, InTableBody, InRow, InCell, InCaption, InColumnGroup)
    @Test
    public void testProcess_tableStructure_parsesTableHierarchyProperly() {
        String html = "<table>" +
                "<caption>Table Caption</caption>" +
                "<colgroup><col></colgroup>" +
                "<thead><tr><th>Header</th></tr></thead>" +
                "<tbody><tr><td>Data 1</td><td>Data 2</td></tr></tbody>" +
                "<tfoot><tr><td>Footer</td></tr></tfoot>" +
                "</table>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("table").size());
        assertEquals(1, doc.select("caption").size());
        assertEquals("Table Caption", doc.select("caption").text());
        assertEquals(1, doc.select("colgroup").size());
        assertEquals(1, doc.select("th").size());
        assertEquals(3, doc.select("td").size());
        assertEquals("Data 1", doc.select("td").get(0).text());
    }

    // Tests InTableText fostering non-whitespace characters outside the table
    @Test
    public void testProcess_tableTextFostering_fostersCharactersBeforeTable() {
        String html = "<table>Fostered Text<tr><td>Cell</td></tr></table>";
        Document doc = Jsoup.parse(html);
        Element body = doc.body();
        assertTrue(body.html().contains("Fostered Text"));
        assertEquals("Cell", doc.select("td").first().text());
    }

    // Tests InSelect and InSelectInTable states
    @Test
    public void testProcess_selectWithinAndOutsideTable_parsesOptionsCorrectly() {
        String html = "<select><optgroup label='g1'><option>1<option>2</optgroup></select>" +
                "<table><tr><td><select><option>T1<option>T2</select></td></tr></table>";
        Document doc = Jsoup.parse(html);
        Elements selects = doc.select("select");
        assertEquals(2, selects.size());
        assertEquals(1, doc.select("optgroup").size());
        assertEquals(4, doc.select("option").size());
        assertEquals("1", doc.select("optgroup option").first().text());
    }

    // Tests InFrameset, AfterFrameset, and AfterAfterFrameset states
    @Test
    public void testProcess_framesetDocument_parsesFramesetHierarchy() {
        String html = "<html><head><title>Frameset</title></head>" +
                "<frameset rows='50%,50%'>" +
                "<frame src='frame1.html'>" +
                "<frame src='frame2.html'>" +
                "<noframes><p>No frames supported</p></noframes>" +
                "</frameset></html>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("frameset").size());
        assertEquals(2, doc.select("frame").size());
        assertEquals(1, doc.select("noframes").size());
        assertEquals("Frameset", doc.title());
    }

    // Tests form element handling and nested input controls in InBody
    @Test
    public void testProcess_formAndInputElements_associatedProperly() {
        String html = "<form id='f1'><input type='text' name='q' value='val'><textarea>text content</textarea></form>";
        Document doc = Jsoup.parse(html);
        Element form = doc.select("form").first();
        assertNotNull(form);
        assertEquals(1, form.select("input").size());
        assertEquals(1, form.select("textarea").size());
        assertEquals("text content", form.select("textarea").text());
    }

    // Tests nested button tags auto-closing previous button in InBody
    @Test
    public void testProcess_nestedButtons_closesOuterButton() {
        String html = "<button>Button 1<button>Button 2</button></button>";
        Document doc = Jsoup.parse(html);
        Elements buttons = doc.select("button");
        assertEquals(2, buttons.size());
        assertEquals("Button 1", buttons.get(0).text());
        assertEquals("Button 2", buttons.get(1).text());
    }

    // Tests InHeadNoscript fallback when noscript tag is encountered in head
    @Test
    public void testProcess_noscriptInHead_processedProperly() {
        String html = "<head><noscript><link rel='stylesheet' href='fallback.css'></noscript></head>";
        Document doc = Jsoup.parse(html);
        Element link = doc.head().select("link").first();
        assertNotNull(link);
        assertEquals("fallback.css", link.attr("href"));
    }

    // Tests AfterBody and AfterAfterBody transitions with trailing comments and content
    @Test
    public void testProcess_afterBodyContent_transitionsBackToBody() {
        String html = "<html><head></head><body>Main Content</body><!-- comment --></html><!-- after html --><div>Extra</div>";
        Document doc = Jsoup.parse(html);
        assertTrue(doc.body().text().contains("Main Content"));
        assertTrue(doc.body().text().contains("Extra"));
        assertEquals(1, doc.select("div").size());
    }

    // Tests implicit body and head creation when starting directly with body elements
    @Test
    public void testProcess_implicitHtmlAndBodyCreation_createsStructure() {
        String html = "<p>Paragraph without explicit html, head, or body</p>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.head());
        assertNotNull(doc.body());
        assertEquals(1, doc.select("p").size());
        assertEquals("Paragraph without explicit html, head, or body", doc.select("p").text());
    }

    // Tests InBody special tag conversions and handling: image alias to img, standalone </br>, and leading newlines
    @Test
    public void testProcess_inBodyTagConversions_handlesImageAndClosingBrAndPreNewlines() {
        String html = "<image src='pic.png'></br><pre>\nFirst line\nSecond line</pre><listing>\nListed</listing>";
        Document doc = Jsoup.parse(html);
        Element img = doc.select("img").first();
        assertNotNull(img);
        assertEquals("pic.png", img.attr("src"));
        assertEquals(1, doc.select("br").size());
        assertEquals("First line\nSecond line", doc.select("pre").first().text());
        assertEquals("Listed", doc.select("listing").first().text());
    }

    // Tests InBody plaintext tag consuming subsequent markup as plain text
    @Test
    public void testProcess_plaintextTag_consumesRemainingInputAsText() {
        String html = "<p>Before</p><plaintext><b>Not bold</b><div>Content</div>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("p").size());
        assertEquals(0, doc.select("b").size());
        assertEquals(0, doc.select("div").size());
        Element plaintext = doc.select("plaintext").first();
        assertNotNull(plaintext);
        assertTrue(plaintext.text().contains("<b>Not bold</b><div>Content</div>"));
    }

    // Tests nested form handling in InBody (nested form tags should be ignored)
    @Test
    public void testProcess_nestedFormTags_ignoresInnerForm() {
        String html = "<form id='outer'><input name='a'><form id='inner'><input name='b'></form></form>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("form").size());
        assertEquals("outer", doc.select("form").first().id());
        assertEquals(2, doc.select("form input").size());
    }

    // Tests InTable input hidden element handling and table within table
    @Test
    public void testProcess_inTableHiddenInputAndNestedTable_correctlyNested() {
        String html = "<table><input type='hidden' name='token' value='123'><tr><td><table><tr><td>Nested</td></tr></table></td></tr></table>";
        Document doc = Jsoup.parse(html);
        Element hiddenInput = doc.select("input[type=hidden]").first();
        assertNotNull(hiddenInput);
        assertEquals("123", hiddenInput.attr("value"));
        assertEquals(2, doc.select("table").size());
        assertEquals("Nested", doc.select("table table td").first().text());
    }

    // Tests InSelect tag interruption by non-select start tags like input and textarea
    @Test
    public void testProcess_inSelectInterruptedByInput_closesSelectTag() {
        String html = "<select><option>Opt 1<input type='text' value='interrupted'><option>Opt 2</select>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("select").size());
        assertEquals(1, doc.select("select option").size());
        assertEquals(1, doc.select("input").size());
    }

    // Tests InSelectInTable state transitions when table tags are encountered inside select
    @Test
    public void testProcess_inSelectInTable_closesSelectOnTableTags() {
        String html = "<table><tr><td><select><option>1</td><td>Next Cell</td></tr></table>";
        Document doc = Jsoup.parse(html);
        assertEquals(2, doc.select("td").size());
        assertEquals("Next Cell", doc.select("td").get(1).text());
        assertEquals(1, doc.select("select").size());
        assertEquals(1, doc.select("select option").size());
    }

    // Tests InBody dangling paragraph closing tag when no paragraph is in scope
    @Test
    public void testProcess_danglingCloseParagraphTag_createsParagraphElement() {
        String html = "<div>Content</div></p>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("div").size());
        assertEquals(1, doc.select("p").size());
    }

    // Tests InColumnGroup handling when encountering col tags and invalid tags
    @Test
    public void testProcess_inColumnGroup_handlesColSpanAndAutoClosing() {
        String html = "<table><colgroup><col span='2'><col width='100'></colgroup><tr><td>Cell</td></tr></table>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("colgroup").size());
        assertEquals(2, doc.select("colgroup col").size());
        assertEquals("2", doc.select("col").first().attr("span"));
        assertEquals("Cell", doc.select("td").first().text());
    }

    // Tests raw text block tags in InBody (xmp, iframe, noembed)
    @Test
    public void testProcess_rawTextElements_parsesInnerContentAsRawText() {
        String html = "<xmp><b>Raw bold</b></xmp><iframe><span>Frame text</span></iframe><noembed><p>No embed</p></noembed>";
        Document doc = Jsoup.parse(html);
        assertEquals(0, doc.select("b").size());
        assertEquals(0, doc.select("span").size());
        assertEquals(1, doc.select("xmp").size());
        assertTrue(doc.select("xmp").first().text().contains("<b>Raw bold</b>"));
    }

    // Tests redundant head tag and unexpected tags in BeforeHead / AfterHead
    @Test
    public void testProcess_redundantHeadTags_ignoredInHeadState() {
        String html = "<html><head><title>Title</title><head></head><body><head>Body text</body></html>";
        Document doc = Jsoup.parse(html);
        assertEquals("Title", doc.title());
        assertEquals("Body text", doc.body().text());
        assertEquals(1, doc.select("head").size());
    }
}