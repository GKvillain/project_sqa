package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.DocumentType;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import static org.junit.Assert.*;

public class HtmlTreeBuilderStateTest {

    private DocumentType getDocumentType(Document doc) {
        for (Node node : doc.childNodes()) {
            if (node instanceof DocumentType) {
                return (DocumentType) node;
            }
        }
        return null;
    }

    // Tests that all constant string arrays used for binary search are strictly sorted
    @Test
    public void testConstants_arraysOrder_allArraysAreSorted() throws IllegalAccessException {
        Field[] fields = HtmlTreeBuilderState.Constants.class.getDeclaredFields();
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers()) && field.getType().isArray()
                    && field.getType().getComponentType().equals(String.class)) {
                field.setAccessible(true);
                String[] array = (String[]) field.get(null);
                String[] copy = Arrays.copyOf(array, array.length);
                Arrays.sort(copy);
                assertArrayEquals("Array " + field.getName() + " must be sorted for inSorted binary search", copy, array);
            }
        }
    }

    // Tests initial doctype parsing and transition to BeforeHtml and BeforeHead
    @Test
    public void testInitial_doctypeDeclaration_transitionsAndSetsDocumentType() {
        String html = "<!DOCTYPE html><html><head></head><body></body></html>";
        Document doc = Jsoup.parse(html);
        DocumentType doctype = getDocumentType(doc);
        assertNotNull(doctype);
        assertEquals("html", doctype.attr("name"));
        assertEquals(Document.QuirksMode.noQuirks, doc.quirksMode());
    }

    // Tests doctype quirks mode triggering force quirks
    @Test
    public void testInitial_forceQuirksDoctype_setsQuirksMode() {
        String html = "<!DOCTYPE html SYSTEM \"about:legacy-compat\">";
        Document doc = Jsoup.parse(html);
        DocumentType doctype = getDocumentType(doc);
        assertNotNull(doctype);
    }

    // Tests InHead state with metadata, title, style, link and script tags
    @Test
    public void testInHead_headTags_createsHeadElements() {
        String html = "<html><head><title>Test Title</title><meta charset=\"utf-8\"><link rel=\"stylesheet\" href=\"style.css\"><style>body { color: red; }</style><script>var x = 1;</script></head><body></body></html>";
        Document doc = Jsoup.parse(html);
        Element head = doc.head();
        assertEquals("Test Title", doc.title());
        assertEquals("utf-8", head.select("meta").first().attr("charset"));
        assertEquals("style.css", head.select("link").first().attr("href"));
        assertEquals("body { color: red; }", head.select("style").first().data());
        assertEquals("var x = 1;", head.select("script").first().data());
    }

    // Tests InHead base element href setting base URI
    @Test
    public void testInHead_baseElement_setsBaseUri() {
        String html = "<html><head><base href=\"https://example.com/base/\"></head><body><a href=\"sub\">Link</a></body></html>";
        Document doc = Jsoup.parse(html, "https://initial.com/");
        Element link = doc.select("a").first();
        assertEquals("https://example.com/base/sub", link.absUrl("href"));
    }

    // Tests InHeadNoscript state when parsing noscript element
    @Test
    public void testInHeadNoscript_noscriptInsideHead_createsNoscriptElement() {
        String html = "<html><head><noscript><link rel=\"stylesheet\" href=\"noscript.css\"></noscript></head><body></body></html>";
        Document doc = Jsoup.parse(html);
        Element noscript = doc.head().select("noscript").first();
        assertNotNull(noscript);
        assertEquals("noscript.css", noscript.select("link").attr("href"));
    }

    // Tests InBody adoption agency algorithm with mismatched formatting tags
    @Test
    public void testInBody_adoptionAgencyAlgorithm_correctsNesting() {
        String html = "<a>1<b>2<p>3</a>4</b>5</p>";
        Document doc = Jsoup.parse(html);
        assertEquals("<a>1<b>2</b></a><p><a><b>3</b></a><b>4</b>5</p>", doc.body().html().replaceAll("\\s+", ""));
    }

    // Tests InBody heading elements auto-closing previous headings
    @Test
    public void testInBody_nestedHeadings_autoClosesPrecedingHeading() {
        String html = "<h1>Heading 1<h2>Heading 2</h2></h1>";
        Document doc = Jsoup.parse(html);
        assertEquals(0, doc.select("h1 h2").size());
        assertEquals(1, doc.select("h1").size());
        assertEquals(1, doc.select("h2").size());
    }

    // Tests InBody list items auto-closing previous li elements
    @Test
    public void testInBody_consecutiveLiTags_autoClosesPreviousLi() {
        String html = "<ul><li>Item 1<li>Item 2<li>Item 3</ul>";
        Document doc = Jsoup.parse(html);
        Elements lis = doc.select("li");
        assertEquals(3, lis.size());
        assertEquals(0, doc.select("li > li").size());
    }

    // Tests InBody definition list dt and dd tags auto-closing
    @Test
    public void testInBody_dtAndDdTags_autoClosesPreviousDefinition() {
        String html = "<dl><dt>Term 1<dd>Desc 1<dt>Term 2<dd>Desc 2</dl>";
        Document doc = Jsoup.parse(html);
        assertEquals(2, doc.select("dt").size());
        assertEquals(2, doc.select("dd").size());
        assertEquals(0, doc.select("dt > dd").size());
        assertEquals(0, doc.select("dd > dt").size());
    }

    // Tests InBody auto closing p tags before block elements
    @Test
    public void testInBody_blockElementAfterP_autoClosesParagraph() {
        String html = "<p>Paragraph 1<div>Div Content</div>Paragraph 2</p>";
        Document doc = Jsoup.parse(html);
        assertEquals(0, doc.select("p div").size());
        assertEquals(2, doc.select("p").size());
    }

    // Tests InBody active formatting reconstruction with nobr
    @Test
    public void testInBody_nobrInScope_reconstructsFormatting() {
        String html = "<nobr>One<nobr>Two</nobr>Three</nobr>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.body());
        assertTrue(doc.body().text().contains("OneTwoThree"));
    }

    // Tests InTable state for standard table structure
    @Test
    public void testInTable_validTable_createsProperHierarchy() {
        String html = "<table><caption>Title</caption><colgroup><col></colgroup><thead><tr><th>Head</th></tr></thead><tbody><tr><td>Cell</td></tr></tbody></table>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("table caption").size());
        assertEquals(1, doc.select("table colgroup col").size());
        assertEquals(1, doc.select("table thead tr th").size());
        assertEquals(1, doc.select("table tbody tr td").size());
    }

    // Tests InTable foster parenting for orphan text and tags
    @Test
    public void testInTable_orphanContentInsideTable_fosterParentedBeforeTable() {
        String html = "<table>Foo<b>Bar</b><tr><td>Cell</td></tr></table>";
        Document doc = Jsoup.parse(html);
        assertEquals("Foo<b>Bar</b><table><tbody><tr><td>Cell</td></tr></tbody></table>", doc.body().html().replaceAll("\n", ""));
    }

    // Tests InTable auto generation of tbody on direct tr/td
    @Test
    public void testInTable_trWithoutTbody_autoGeneratesTbody() {
        String html = "<table><tr><td>Data</td></tr></table>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("table > tbody > tr > td").size());
    }

    // Tests InSelect state with optgroup and options
    @Test
    public void testInSelect_optionsAndOptgroups_parsedCorrectly() {
        String html = "<select><optgroup label=\"g1\"><option>1<option>2</optgroup><option>3</select>";
        Document doc = Jsoup.parse(html);
        Element select = doc.select("select").first();
        assertNotNull(select);
        assertEquals(1, select.select("optgroup").size());
        assertEquals(3, select.select("option").size());
        assertEquals(2, select.select("optgroup > option").size());
    }

    // Tests InSelect in table context
    @Test
    public void testInSelectInTable_tableTagInsideSelect_closesSelect() {
        String html = "<table><tr><td><select><option>1<td>2</table>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("select").size());
        assertEquals(2, doc.select("td").size());
    }

    // Tests InFrameset state with frame tags
    @Test
    public void testInFrameset_framesetDocument_createsFrameTree() {
        String html = "<html><frameset rows=\"50%,50%\"><frame src=\"frame1.html\"><frame src=\"frame2.html\"><noframes><p>No frames</p></noframes></frameset></html>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("frameset").size());
        assertEquals(2, doc.select("frame").size());
        assertEquals(1, doc.select("noframes").size());
    }

    // Tests InBody custom tag closing preserving case or normalized case
    @Test
    public void testInBody_customElements_parsedAndClosedCorrectly() {
        String html = "<div><custom-tag>Content</custom-tag></div>";
        Document doc = Jsoup.parse(html);
        Element custom = doc.select("custom-tag").first();
        assertNotNull(custom);
        assertEquals("Content", custom.text());
    }

    // Tests Parser settings case preserving with custom tag matching
    @Test
    public void testInBody_casePreserveSettings_correctlyClosesTags() {
        Parser parser = Parser.htmlParser().settings(ParseSettings.preserveCase);
        Document doc = parser.parseInput("<div><CUSTOM_TAG>Content</CUSTOM_TAG></div>", "");
        Element custom = doc.select("CUSTOM_TAG").first();
        assertNotNull(custom);
        assertEquals("Content", custom.text());
    }

    // Tests isindex tag handling inside InBody
    @Test
    public void testInBody_isindexTag_createsFormAndInput() {
        String html = "<isindex prompt=\"Search: \">";
        Document doc = Jsoup.parse(html);
        Element form = doc.select("form").first();
        assertNotNull(form);
        Element input = form.select("input[name=isindex]").first();
        assertNotNull(input);
    }

    // Tests textarea switching to Rcdata state and pre/listing LF consuming
    @Test
    public void testInBody_textareaAndPre_handlesRawDataCorrectly() {
        String html = "<textarea>\nFirst line\nSecond line</textarea><pre>\nPreformatted</pre>";
        Document doc = Jsoup.parse(html);
        Element textarea = doc.select("textarea").first();
        assertNotNull(textarea);
        assertEquals("\nFirst line\nSecond line", textarea.text());
        Element pre = doc.select("pre").first();
        assertNotNull(pre);
        assertEquals("Preformatted", pre.text());
    }
}