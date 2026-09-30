package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

public class TokeniserStateTest {

    // Tests normal plain text in Data state
    @Test
    public void testData_plainText_returnsText() {
        Document doc = Jsoup.parse("Hello world");
        assertEquals("Hello world", doc.body().text());
    }

    // Tests character reference in Data state
    @Test
    public void testData_ampersandCharacterReference_emitsCharacter() {
        Document doc = Jsoup.parse("&amp;");
        assertEquals("&", doc.body().text());
    }

    // Tests null char handling in Data state (should use replacement char U+FFFD)
    // This targets defect 24b which emits raw null char instead of replacement
    @Test
    public void testData_nullChar_emitsReplacementChar() {
        String input = "he\u0000llo";
        Document doc = Jsoup.parse(input);
        assertEquals("he\uFFFDto", doc.body().text());
    }

    // Tests valid start tag via TagOpen
    @Test
    public void testTagOpen_validTag_createsStartTag() {
        Document doc = Jsoup.parse("<div></div>");
        assertNotNull(doc.select("div").first());
    }

    // Tests whitespace in TagName transitions to BeforeAttributeName
    @Test
    public void testTagName_whitespace_transitionsToBeforeAttribute() {
        Document doc = Jsoup.parse("<div class=\"test\"></div>");
        assertEquals("test", doc.select("div").first().attr("class"));
    }

    // Tests double quoted attribute value consumption
    @Test
    public void testAttributeValue_doubleQuoted_consumesValue() {
        Document doc = Jsoup.parse("<a href=\"http://example.com\">link</a>");
        assertEquals("http://example.com", doc.select("a").first().attr("href"));
    }

    // Tests self-closing tag via SelfClosingStartTag
    @Test
    public void testSelfClosingStartTag_gt_emitsSelfClosing() {
        Document doc = Jsoup.parse("<br/>");
        assertNotNull(doc.select("br").first());
    }

    // Tests end tag via EndTagOpen
    @Test
    public void testEndTagOpen_validTagName_createsEndTag() {
        Document doc = Jsoup.parse("<div></div>");
        assertEquals(1, doc.select("div").size());
    }

    // Tests simple comment parsing
    @Test
    public void testComment_simpleComment_emitsComment() {
        Document doc = Jsoup.parse("<!-- test -->");
        assertTrue(doc.toString().contains("<!-- test -->"));
    }

    // Tests DOCTYPE declaration
    @Test
    public void testDoctype_html_emitsDoctype() {
        Document doc = Jsoup.parse("<!DOCTYPE html>");
        assertTrue(doc.toString().contains("<!DOCTYPE html>"));
    }

    // Tests null char in Rcdata (textarea) – should emit replacement char
    @Test
    public void testRcdata_nullChar_emitsReplacementChar() {
        String input = "<textarea>he\u0000llo</textarea>";
        Document doc = Jsoup.parse(input);
        assertEquals("he\uFFFDllo", doc.select("textarea").first().text());
    }

    // Tests null char in Rawtext (style) – should emit replacement char
    @Test
    public void testRawtext_nullChar_emitsReplacementChar() {
        String input = "<style>body{color:red}\u0000</style>";
        Document doc = Jsoup.parse(input);
        assertEquals("body{color:red}\uFFFD", doc.select("style").first().data());
    }

    // Tests null char in ScriptData – should emit replacement char
    @Test
    public void testScriptData_nullChar_emitsReplacementChar() {
        String input = "<script>var x = 'he\u0000llo';</script>";
        Document doc = Jsoup.parse(input);
        assertEquals("var x = 'he\uFFFDto';", doc.select("script").first().data());
    }

    // Tests bogus comment starting with '?'
    @Test
    public void testBogusComment_questionMark_emitsComment() {
        Document doc = Jsoup.parse("<?xml version='1.0'?>");
        assertTrue(doc.toString().contains("<?xml version='1.0'?>"));
    }

    // Tests CDATA section using XML parser (triggers CdataSection state)
    @Test
    public void testCdataSection_insideScript_handlesCorrectly() {
        Document doc = Jsoup.parse("<root><![CDATA[some data]]></root>", "", Parser.xmlParser());
        assertEquals("some data", doc.select("root").first().text());
    }

    // Tests ampersand in unquoted attribute value (character reference in attribute)
    @Test
    public void testAttributeValue_unquoted_ampersand_handlesCharacterReference() {
        Document doc = Jsoup.parse("<a href=test&amp;next>link</a>");
        assertEquals("test&next", doc.select("a").first().attr("href"));
    }

    // Tests slash after quoted attribute value – transition to self-closing
    @Test
    public void testAfterAttributeValue_quoted_slash_transitionsToSelfClosing() {
        Document doc = Jsoup.parse("<div id=\"x\" />");
        Element div = doc.select("div").first();
        assertNotNull(div);
        assertEquals("x", div.attr("id"));
    }

    // Tests slash before attribute name – self-closing tag
    @Test
    public void testBeforeAttributeName_slash_transitionsToSelfClosing() {
        Document doc = Jsoup.parse("<div />");
        assertNotNull(doc.select("div").first());
    }

    // Tests null char in unquoted attribute value – should become replacement char
    @Test
    public void testAttributeValue_unquoted_nullChar_emitsReplacementChar() {
        String input = "<div id=te\u0000st>";
        Document doc = Jsoup.parse(input);
        assertEquals("te\uFFFDst", doc.select("div").first().attr("id"));
    }

    // Tests null char in double quoted attribute value – should become replacement char
    @Test
    public void testAttributeValue_doubleQuoted_nullChar_emitsReplacementChar() {
        String input = "<div id=\"te\u0000st\">";
        Document doc = Jsoup.parse(input);
        assertEquals("te\uFFFDst", doc.select("div").first().attr("id"));
    }
}