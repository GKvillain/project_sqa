package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.DocumentType;
import org.jsoup.nodes.Element;
import org.junit.Test;

import static org.junit.Assert.*;

public class TreeBuilderStateTest {

    // Tests handling of literal '0' character in body to catch 0x0000 int vs char bug
    @Test
    public void testInBody_zeroCharacter_retainedInOutput() {
        Document doc = Jsoup.parse("0");
        assertEquals("0", doc.body().text());
    }

    // Tests handling of literal '0' character in table context
    @Test
    public void testInTableText_zeroCharacter_retainedInOutput() {
        Document doc = Jsoup.parse("<table>0<tr><td>1</td></tr></table>");
        assertTrue(doc.body().text().contains("0"));
    }

    // Tests handling of literal '0' character inside select element
    @Test
    public void testInSelect_zeroCharacter_retainedInOutput() {
        Document doc = Jsoup.parse("<select>0<option>1</option></select>");
        assertEquals("1", doc.select("option").text());
    }

    // Tests initial state doctype parsing and quirks mode handling
    @Test
    public void testInitial_validDoctype_parsesDocumentType() {
        Document doc = Jsoup.parse("<!DOCTYPE html><html><head></head><body><p>Test</p></body></html>");
        assertTrue(doc.childNode(0) instanceof DocumentType);
        DocumentType doctype = (DocumentType) doc.childNode(0);
        assertEquals("html", doctype.attr("name"));
    }

    // Tests before html state with comments and leading whitespace
    @Test
    public void testBeforeHtml_leadingCommentsAndWhitespace_parsedCorrectly() {
        Document doc = Jsoup.parse("<!-- comment -->\n\n<html><head></head><body>Hello</body></html>");
        assertEquals("Hello", doc.body().text());
    }

    // Tests in head state with metadata, style, script, and noscript elements
    @Test
    public void testInHead_metadataElements_insertedIntoHead() {
        String html = "<html><head><title>Title</title><meta name='desc' content='text'><style>body{}</style><script>var a=1;</script><noscript>NoScript</noscript></head><body></body></html>";
        Document doc = Jsoup.parse(html);
        assertEquals("Title", doc.title());
        assertEquals(1, doc.select("meta").size());
        assertEquals(1, doc.select("style").size());
        assertEquals(1, doc.select("script").size());
    }

    // Tests in head base URI update
    @Test
    public void testInHead_baseElement_updatesBaseUri() {
        String html = "<html><head><base href='http://example.com/'></head><body><a href='test.html'>Link</a></body></html>";
        Document doc = Jsoup.parse(html, "http://default.com/");
        Element link = doc.select("a").first();
        assertNotNull(link);
        assertEquals("http://example.com/test.html", link.absUrl("href"));
    }

    // Tests implicit head and body creation when tags are omitted
    @Test
    public void testBeforeHead_missingHeadTag_createsHeadAndBodyImplicitly() {
        Document doc = Jsoup.parse("<title>Implicit Head</title><p>Implicit Body</p>");
        assertEquals("Implicit Head", doc.title());
        assertEquals("Implicit Body", doc.select("p").text());
        assertNotNull(doc.head());
        assertNotNull(doc.body());
    }

    // Tests adoption agency algorithm with mismatched formatting tags
    @Test
    public void testInBody_misnestedFormattingTags_adoptionAgencyAlgorithmApplied() {
        Document doc = Jsoup.parse("<b>1<i>2</b>3</i>");
        assertEquals("<b>1<i>2</i></b><i>3</i>", doc.body().html());
    }

    // Tests list items closing behavior in body
    @Test
    public void testInBody_listItems_autoClosesPrecedingItems() {
        Document doc = Jsoup.parse("<ul><li>Item 1<li>Item 2</ul><dl><dt>Term<dd>Definition</dl>");
        assertEquals(2, doc.select("li").size());
        assertEquals(1, doc.select("dt").size());
        assertEquals(1, doc.select("dd").size());
    }

    // Tests header tags auto-closing behavior
    @Test
    public void testInBody_headings_autoClosePreviousHeading() {
        Document doc = Jsoup.parse("<h1>Heading 1<h2>Heading 2</h2></h1>");
        assertEquals(1, doc.select("h1").size());
        assertEquals(1, doc.select("h2").size());
    }

    // Tests form handling and nested form prevention
    @Test
    public void testInBody_formTags_nestedFormsIgnored() {
        Document doc = Jsoup.parse("<form id='f1'><input name='i1'><form id='f2'><input name='i2'></form></form>");
        assertEquals(1, doc.select("form").size());
        assertEquals(2, doc.select("form input").size());
    }

    // Tests isindex tag expansion into form and input
    @Test
    public void testInBody_isindexTag_expandsToFormAndInput() {
        Document doc = Jsoup.parse("<isindex prompt='search' action='/search'>");
        assertEquals(1, doc.select("form").size());
        assertEquals(1, doc.select("input[name=isindex]").size());
    }

    // Tests table structure states including caption, colgroup, thead, tbody, tfoot, tr, and td
    @Test
    public void testInTable_completeTableStructure_parsesCorrectly() {
        String html = "<table><caption>Cap</caption><colgroup><col></colgroup><thead><tr><th>H</th></tr></thead><tbody><tr><td>D</td></tr></tbody><tfoot><tr><td>F</td></tr></tfoot></table>";
        Document doc = Jsoup.parse(html);
        assertEquals("Cap", doc.select("caption").text());
        assertEquals(1, doc.select("col").size());
        assertEquals("H", doc.select("th").text());
        assertEquals(2, doc.select("td").size());
    }

    // Tests foster parenting of text nodes inside table
    @Test
    public void testInTable_fosterParenting_textMovedBeforeTable() {
        Document doc = Jsoup.parse("<table><tr><td>Cell</td></tr>FosterText</table>");
        assertTrue(doc.body().text().contains("FosterText"));
        assertEquals("FosterText", doc.body().ownText().trim());
    }

    // Tests select options and optgroup parsing
    @Test
    public void testInSelect_optgroupsAndOptions_parsedCorrectly() {
        String html = "<select><optgroup label='G1'><option>O1<option>O2</optgroup><option>O3</select>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("optgroup").size());
        assertEquals(3, doc.select("option").size());
    }

    // Tests frameset and noframes handling
    @Test
    public void testInFrameset_framesetStructure_parsedCorrectly() {
        String html = "<html><frameset rows='50%,50%'><frame src='f1.html'><frame src='f2.html'><noframes>NoFrames</noframes></frameset></html>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("frameset").size());
        assertEquals(2, doc.select("frame").size());
    }

    // Tests after body and after after body comments and whitespace
    @Test
    public void testAfterBody_trailingComments_appendedToHtml() {
        Document doc = Jsoup.parse("<html><head></head><body>Content</body></html><!-- Trailing Comment -->");
        assertEquals("Content", doc.body().text());
    }

    // Tests rawtext and rcdata handling in textarea and xmp
    @Test
    public void testInBody_textareaAndXmp_rawTextHandled() {
        Document doc = Jsoup.parse("<textarea><b>not bold</b></textarea><xmp><i>not italic</i></xmp>");
        assertEquals("<b>not bold</b>", doc.select("textarea").first().text());
        assertEquals("<i>not italic</i>", doc.select("xmp").first().text());
    }
}