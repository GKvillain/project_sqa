package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.DocumentType;

public class TokeniserStateTest {

    private Document parse(String html) {
        return Jsoup.parse(html);
    }

    // Tests Data state with null char - should emit replacement character
    @Test
    public void testData_nullChar_replacedWithReplacementChar() {
        Document doc = parse("<div>a\u0000b</div>");
        Element div = doc.select("div").first();
        assertEquals("a\ufffdb", div.text());
    }

    // Tests Data state with character references
    @Test
    public void testData_characterReference_emitsCorrespondingChar() {
        Document doc = parse("<p>&amp;&#65;</p>");
        assertEquals("&A", doc.select("p").first().text());
    }

    // Tests Data state with unknown entity - should emit literal text
    @Test
    public void testData_unknownCharacterReference_emitsLiteral() {
        Document doc = parse("<p>&foo;</p>");
        assertEquals("&foo;", doc.select("p").first().text());
    }

    // Tests TagOpen with non-letter - emits '<' as text and continues
    @Test
    public void testTagOpen_nonLetter_emitsLessThanAsText() {
        Document doc = parse("<div>a < b</div>");
        assertEquals("a < b", doc.select("div").first().text());
    }

    // Tests TagOpen with '?' - creates bogus comment
    @Test
    public void testTagOpen_questionMark_createsBogusComment() {
        Document doc = parse("<div><?xml version=\"1.0\"?></div>");
        Element div = doc.select("div").first();
        assertEquals(1, div.childNodeSize());
        assertTrue(div.childNode(0) instanceof Comment);
        assertEquals("?xml version=\"1.0\"?", ((Comment) div.childNode(0)).getData());
    }

    // Tests TagName with quoted attributes
    @Test
    public void testTagName_withAttributes_parsesQuotedAttribute() {
        Document doc = parse("<div id='foo' class=\"bar\">text</div>");
        Element div = doc.select("div").first();
        assertEquals("foo", div.attr("id"));
        assertEquals("bar", div.attr("class"));
        assertEquals("text", div.text());
    }

    // Tests AttributeValue unquoted with entity
    @Test
    public void testAttributeValue_unquotedWithEntity_parsesValue() {
        Document doc = parse("<div data-a=1&amp;2>text</div>");
        Element div = doc.select("div").first();
        assertEquals("1&2", div.attr("data-a"));
    }

    // Tests AttributeValue single quoted with entity
    @Test
    public void testAttributeValue_singleQuotedWithEntity_parsesValue() {
        Document doc = parse("<div data-a='&lt;b&gt;'>x</div>");
        Element div = doc.select("div").first();
        assertEquals("<b>", div.attr("data-a"));
    }

    // Tests Comment parsing
    @Test
    public void testComment_withText_parsesComment() {
        Document doc = parse("<div><!-- comment --></div>");
        Element div = doc.select("div").first();
        assertTrue(div.childNode(0) instanceof Comment);
        assertEquals(" comment ", ((Comment) div.childNode(0)).getData());
    }

    // Tests Comment with null char - replacement character
    @Test
    public void testComment_nullChar_replacedWithReplacementChar() {
        Document doc = parse("<div><!-- a\u0000b --></div>");
        Element div = doc.select("div").first();
        assertTrue(div.childNode(0) instanceof Comment);
        assertEquals(" a\ufffdb ", ((Comment) div.childNode(0)).getData());
    }

    // Tests Comment unclosed at EOF
    @Test
    public void testComment_unclosedAtEOF_emitsComment() {
        Document doc = parse("<div><!-- unclosed");
        Element div = doc.select("div").first();
        assertTrue(div.childNode(0) instanceof Comment);
        String data = ((Comment) div.childNode(0)).getData();
        assertTrue(data.contains("unclosed"));
    }

    // Tests CommentEndBang with !> sequence
    @Test
    public void testComment_endBang_withExclamationAndGt_parses() {
        Document doc = parse("<div><!-- foo --!></div>");
        Element div = doc.select("div").first();
        assertTrue(div.childNode(0) instanceof Comment);
        String data = ((Comment) div.childNode(0)).getData();
        assertTrue(data.contains("foo"));
    }

    // Tests Doctype basic name
    @Test
    public void testDoctype_basic_parsesName() {
        Document doc = parse("<!DOCTYPE html><html></html>");
        DocumentType dt = doc.doctype();
        assertNotNull(dt);
        assertEquals("html", dt.name());
    }

    // Tests Doctype with public and system identifiers
    @Test
    public void testDoctype_publicAndSystemIdentifiers_parses() {
        Document doc = parse("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\"><html></html>");
        DocumentType dt = doc.doctype();
        assertNotNull(dt);
        assertEquals("-//W3C//DTD XHTML 1.0 Strict//EN", dt.publicId());
        assertEquals("http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd", dt.systemId());
    }

    // Tests CDATA section in HTML
    @Test
    public void testCdataSection_parsesContentAsText() {
        Document doc = parse("<div><![CDATA[<b>raw & text</b>]]></div>");
        Element div = doc.select("div").first();
        assertTrue(div.text().contains("raw & text"));
    }

    // Tests Rcdata (title) with markup characters - should not parse tags
    @Test
    public void testRcdata_title_withMarkupChars_keepsText() {
        Document doc = parse("<title>a < b</title>");
        assertEquals("a < b", doc.title());
    }

    // Tests Rawtext (style) with markup characters
    @Test
    public void testRawtext_style_withMarkupChars_keepsContent() {
        Document doc = parse("<style>a < b { color:red; }</style>");
        Element style = doc.select("style").first();
        assertNotNull(style);
        assertEquals("a < b { color:red; }", style.data());
    }

    // Tests ScriptData with markup characters
    @Test
    public void testScriptData_withMarkupChars_keepsContent() {
        Document doc = parse("<script>var a = 1 < 2;</script>");
        Element script = doc.select("script").first();
        assertNotNull(script);
        assertEquals("var a = 1 < 2;", script.data());
    }

    // Tests AfterAttributeValue quoted followed by another attribute
    @Test
    public void testAfterAttributeValue_quoted_thenNextAttribute_parsesBoth() {
        Document doc = parse("<div a=\"1\" b=\"2\">text</div>");
        Element div = doc.select("div").first();
        assertEquals("1", div.attr("a"));
        assertEquals("2", div.attr("b"));
    }

    // Tests SelfClosingStartTag on div
    @Test
    public void testSelfClosingStartTag_onDiv_parsesAsEmptyElement() {
        Document doc = parse("<div/>");
        Element div = doc.select("div").first();
        assertNotNull(div);
        assertEquals("", div.text());
    }
}