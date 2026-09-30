package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.DocumentType;

public class TokeniserStateTest {

    private String parseBodyText(String html) {
        return Jsoup.parseBodyFragment(html).body().text();
    }

    @Test
    public void testDataState_ampersand_emitsDecodedCharacter() {
        assertEquals("&", parseBodyText("&amp;"));
    }

    @Test
    public void testDataState_lessthan_parsesTag() {
        Document doc = Jsoup.parseBodyFragment("<b>bold</b>");
        Element b = doc.select("b").first();
        assertNotNull(b);
        assertEquals("bold", b.text());
    }

    @Test
    public void testDataState_nullChar_emitsReplacementChar() {
        String result = parseBodyText("a\u0000b");
        assertEquals("a\uFFFDb", result);
    }

    @Test
    public void testDataState_eof_emitsEOF() {
        assertEquals("", parseBodyText(""));
    }

    @Test
    public void testTagOpen_exclamation_markupDeclaration_comment() {
        Document doc = Jsoup.parseBodyFragment("<!-- comment -->");
        boolean found = false;
        for (Node node : doc.body().childNodes()) {
            if (node instanceof Comment) {
                assertEquals(" comment ", ((Comment) node).getData());
                found = true;
            }
        }
        assertTrue("Comment should be present", found);
    }

    @Test
    public void testTagOpen_slash_endTagOpen_parsesEndTag() {
        Document doc = Jsoup.parseBodyFragment("<p>text</p>");
        Element p = doc.select("p").first();
        assertNotNull(p);
        assertEquals("text", p.text());
    }

    @Test
    public void testTagName_whitespace_transitionToBeforeAttributeName() {
        Document doc = Jsoup.parseBodyFragment("<div class='main'>");
        Element div = doc.select("div").first();
        assertNotNull(div);
        assertEquals("main", div.attr("class"));
    }

    @Test
    public void testTagName_slash_selfClosingStartTag() {
        Document doc = Jsoup.parseBodyFragment("<br/>");
        Element br = doc.select("br").first();
        assertNotNull(br);
        assertTrue("Tag should be self-closing", br.tag().isSelfClosing());
    }

    @Test
    public void testBeforeAttributeName_gt_emitsTag() {
        Document doc = Jsoup.parseBodyFragment("<div >");
        assertNotNull(doc.select("div").first());
    }

    @Test
    public void testAttributeName_equals_transitionToBeforeAttributeValue() {
        Document doc = Jsoup.parseBodyFragment("<input type='text'>");
        Element input = doc.select("input").first();
        assertNotNull(input);
        assertEquals("text", input.attr("type"));
    }

    @Test
    public void testAttributeValue_doubleQuoted_consumesUntilQuote() {
        Document doc = Jsoup.parseBodyFragment("<a href=\"http://example.com\">");
        Element a = doc.select("a").first();
        assertEquals("http://example.com", a.attr("href"));
    }

    @Test
    public void testAttributeValue_singleQuoted_consumesUntilApos() {
        Document doc = Jsoup.parseBodyFragment("<a href='http://example.com'>");
        Element a = doc.select("a").first();
        assertEquals("http://example.com", a.attr("href"));
    }

    @Test
    public void testAttributeValue_unquoted_consumesUntilWhitespace() {
        Document doc = Jsoup.parseBodyFragment("<a href=http://example.com>");
        Element a = doc.select("a").first();
        assertEquals("http://example.com", a.attr("href"));
    }

    @Test
    public void testAttributeValue_unquoted_ampersand_consumesCharacterReference() {
        Document doc = Jsoup.parseBodyFragment("<a href=foo&amp;bar>");
        Element a = doc.select("a").first();
        assertEquals("foo&bar", a.attr("href"));
    }

    @Test
    public void testAttributeValue_doubleQuoted_ampersand_consumesCharacterReference() {
        Document doc = Jsoup.parseBodyFragment("<a href=\"foo&amp;bar\">");
        Element a = doc.select("a").first();
        assertEquals("foo&bar", a.attr("href"));
    }

    @Test
    public void testDoctype_transitionThroughStates() {
        Document doc = Jsoup.parse("<!DOCTYPE html>");
        DocumentType doctype = doc.documentType();
        assertNotNull(doctype);
        assertEquals("html", doctype.name());
    }

    @Test
    public void testCdataSection_consumesUntilClose() {
        Document doc = Jsoup.parseBodyFragment("<![CDATA[some data]]>");
        assertEquals("some data", doc.body().text());
    }

    @Test
    public void testScriptData_lessthan_notInterpretedAsTag() {
        Document doc = Jsoup.parseBodyFragment("<script>var x = '<';</script>");
        Element script = doc.select("script").first();
        assertNotNull(script);
        assertEquals("var x = '<';", script.html());
    }

    @Test
    public void testRawtext_lessthan_notInterpretedAsTag() {
        Document doc = Jsoup.parseBodyFragment("<textarea>raw < text</textarea>");
        Element textarea = doc.select("textarea").first();
        assertNotNull(textarea);
        assertEquals("raw < text", textarea.text());
    }

    @Test
    public void testBogusComment_consumesUntilGt() {
        Document doc = Jsoup.parseBodyFragment("<?xml version='1.0'?>");
        boolean found = false;
        for (Node node : doc.body().childNodes()) {
            if (node instanceof Comment) {
                assertEquals("?xml version='1.0'", ((Comment) node).getData());
                found = true;
            }
        }
        assertTrue("Bogus comment should be present", found);
    }
}