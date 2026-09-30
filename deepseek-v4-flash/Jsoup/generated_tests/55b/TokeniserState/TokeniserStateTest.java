package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class TokeniserStateTest {

    @Test
    public void testSimpleHtml_shouldCreateDocumentStructure() {
        Document doc = Jsoup.parse("<p>Hello</p>");
        assertNotNull(doc);
        assertNotNull(doc.body());
        assertEquals("Hello", doc.body().text());
    }

    @Test
    public void testTagWithAttributes_shouldParseAttributes() {
        Document doc = Jsoup.parse("<a href=\"http://example.com\" class=\"link\">Link</a>");
        assertTrue(doc.select("a").size() > 0);
        Element a = doc.select("a").get(0);
        assertEquals("http://example.com", a.attr("href"));
        assertEquals("link", a.attr("class"));
    }

    @Test
    public void testSelfClosingTag_shouldBeVoidElement() {
        Document doc = Jsoup.parse("<br/>");
        assertTrue(doc.select("br").size() > 0);
        Element br = doc.select("br").get(0);
        assertEquals("br", br.tagName());
    }

    @Test
    public void testComment_shouldNotAppearInOutput() {
        Document doc = Jsoup.parse("text <!-- comment --> more");
        assertFalse(doc.body().text().contains("comment"));
    }

    @Test
    public void testDoctypeWithPublicAndSystemIds_shouldParse() {
        Document doc = Jsoup.parse("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
        assertNotNull(doc);
    }

    @Test
    public void testDoctypeWithSystemIdAndInvalidChar_shouldForceQuirks() {
        Document doc = Jsoup.parse("<!DOCTYPE html SYSTEM \"http://example.com\" invalid>");
        assertNotNull(doc);
        assertEquals(Document.QuirksMode.quirks, doc.quirksMode());
    }

    @Test
    public void testScriptTag_shouldPreserveContent() {
        Document doc = Jsoup.parse("<script>if (x < y) { alert('test'); }</script>");
        assertTrue(doc.body().text().contains("if (x < y) { alert('test'); }"));
    }

    @Test
    public void testTextareaTag_shouldDecodeEntities() {
        Document doc = Jsoup.parse("<textarea>Test &amp; more</textarea>");
        assertTrue(doc.select("textarea").size() > 0);
        Element textarea = doc.select("textarea").get(0);
        assertEquals("Test & more", textarea.text());
    }

    @Test
    public void testCharacterReferenceInData_shouldDecode() {
        Document doc = Jsoup.parse("&amp;");
        assertEquals("&", doc.body().text());
    }

    @Test
    public void testEofAfterOpeningAngle_shouldEmitAngleBracketAsText() {
        Document doc = Jsoup.parse("<");
        assertEquals("<", doc.body().text().trim());
        assertEquals(0, doc.body().children().size());
    }

    @Test
    public void testNullCharacterInData_shouldBeReplaced() {
        Document doc = Jsoup.parse("text\u0000more");
        String text = doc.body().text();
        assertTrue(text.contains("\uFFFD"));
        assertFalse(text.contains("\u0000"));
    }

    @Test
    public void testNullCharacterInAttributeValue_shouldBeReplaced() {
        Document doc = Jsoup.parse("<div class=\"test\u0000value\"></div>");
        assertTrue(doc.select("div").size() > 0);
        Element div = doc.select("div").get(0);
        assertEquals("test\uFFFDvalue", div.attr("class"));
    }

    @Test
    public void testInvalidTagStart_shouldEmitAngleBracketAndText() {
        Document doc = Jsoup.parse("<1div>");
        assertEquals("<1div>", doc.body().text().trim());
        assertEquals(0, doc.body().children().size());
    }

    @Test
    public void testBogusComment_shouldBeTreatedAsComment() {
        Document doc = Jsoup.parse("<?xml test?>");
        assertFalse(doc.body().text().contains("<?xml"));
    }

    @Test
    public void testCdataSection_shouldPreserveContent() {
        Document doc = Jsoup.parse("<![CDATA[test content]]>");
        assertEquals("test content", doc.body().text().trim());
    }

    @Test
    public void testBooleanAttribute_shouldBeParsed() {
        Document doc = Jsoup.parse("<input disabled>");
        assertTrue(doc.select("input").size() > 0);
        Element input = doc.select("input").get(0);
        assertTrue(input.hasAttr("disabled"));
        assertEquals("", input.attr("disabled"));
    }

    @Test
    public void testStyleTag_shouldPreserveContent() {
        Document doc = Jsoup.parse("<style>body { color: red; }</style>");
        assertTrue(doc.body().text().contains("body { color: red; }"));
    }

    @Test
    public void testUnquotedAttributeValue_shouldParse() {
        Document doc = Jsoup.parse("<div class=test></div>");
        assertTrue(doc.select("div").size() > 0);
        Element div = doc.select("div").get(0);
        assertEquals("test", div.attr("class"));
    }

    @Test
    public void testTextareaWithInvalidEndTag_shouldNotClose() {
        Document doc = Jsoup.parse("<textarea>test</div> more</textarea>");
        assertTrue(doc.select("textarea").size() > 0);
        Element textarea = doc.select("textarea").get(0);
        assertEquals("test</div> more", textarea.text());
    }

    @Test
    public void testScriptWithDoubleEscape_shouldHandle() {
        Document doc = Jsoup.parse("<script><!-- var x = 0; --></script>");
        assertTrue(doc.body().text().contains("<!--"));
    }

    // ====== Additional test cases to improve coverage ======

    @Test
    public void testEndTagWithExtraWhitespace() {
        Document doc = Jsoup.parse("<div></div >");
        assertNotNull(doc);
        assertTrue(doc.select("div").size() > 0);
        Element div = doc.select("div").first();
        assertNotNull(div);
    }

    @Test
    public void testAttributeValueSingleQuoted() {
        Document doc = Jsoup.parse("<div class='test'>");
        Element div = doc.select("div").first();
        assertEquals("test", div.attr("class"));
    }

    @Test
    public void testAttributeValueEntityReference() {
        Document doc = Jsoup.parse("<a href=\"http://example.com?param&amp;extra\">Link</a>");
        Element a = doc.select("a").first();
        assertEquals("http://example.com?param&extra", a.attr("href"));
    }

    @Test
    public void testAttributeWithoutValue() {
        Document doc = Jsoup.parse("<a href>Text</a>");
        Element a = doc.select("a").first();
        assertTrue(a.hasAttr("href"));
        assertEquals("", a.attr("href"));
    }

    @Test
    public void testSelfClosingNonVoidTag() {
        Document doc = Jsoup.parse("<div/>");
        Elements divs = doc.select("div");
        assertEquals(1, divs.size());
        Element div = divs.first();
        assertEquals("div", div.tagName());
        // self-closing on non-void elements is ignored; div has no children
        assertEquals(0, div.children().size());
    }

    @Test
    public void testNestedTags() {
        Document doc = Jsoup.parse("<div><p>text</p></div>");
        Elements divs = doc.select("div");
        assertEquals(1, divs.size());
        Elements ps = doc.select("p");
        assertEquals(1, ps.size());
        assertEquals("text", ps.first().text());
    }

    @Test
    public void testMultipleAttributesWithWhitespace() {
        Document doc = Jsoup.parse("<div class = \"test\" id = 'x' >");
        Element div = doc.select("div").first();
        assertEquals("test", div.attr("class"));
        assertEquals("x", div.attr("id"));
    }

    @Test
    public void testEmptyComment() {
        Document doc = Jsoup.parse("<!---->");
        assertNotNull(doc);
        // comment should not appear in output
        assertTrue(doc.body().text().isEmpty());
    }

    @Test
    public void testCharacterReferenceInDataWithMissingSemicolon() {
        // &amp without semicolon, followed by space
        Document doc = Jsoup.parse("&amp test");
        String text = doc.body().text();
        // According to HTML spec, the reference is not matched because it is not followed by ';'
        // Expect the literal text "&amp test"
        assertEquals("&amp test", text);
    }

    @Test
    public void testNumericCharacterReference() {
        Document doc = Jsoup.parse("&#60;&#x3C;");
        assertEquals("<<", doc.body().text());
    }

    @Test
    public void testInvalidCharacterInTagName() {
        // '@' is not valid in tag name; parser should treat it as part of attribute or ignore
        Document doc = Jsoup.parse("<div@test>");
        // At minimum, should not throw exception and should produce a div element
        assertTrue(doc.select("div").size() > 0);
    }

    @Test
    public void testWhitespaceBeforeClosingAngle() {
        Document doc = Jsoup.parse("<div >");
        assertTrue(doc.select("div").size() > 0);
        Element div = doc.select("div").first();
        // no attributes
        assertTrue(div.attributes().isEmpty());
    }

    @Test
    public void testUnclosedDoubleQuotedAttributeValue() {
        Document doc = Jsoup.parse("<div class=\"test");
        Element div = doc.select("div").first();
        assertEquals("test", div.attr("class"));
    }

    @Test
    public void testNullCharacterInAttributeName() {
        Document doc = Jsoup.parse("<div cla\u0000ss=\"value\"></div>");
        // Should parse without crash; attribute name with null may be discarded
        Element div = doc.select("div").first();
        assertNotNull(div);
    }

    @Test
    public void testCarriageReturnInData() {
        String input = "text\rmore";
        Document doc = Jsoup.parse(input);
        String text = doc.body().text();
        // Carriage return should be normalized or replaced; it should not appear as '\r'
        assertFalse(text.contains("\r"));
    }
}