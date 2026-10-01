package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class HtmlTreeBuilderStateTest {

    @Test
    public void testParseBasicHtml_createsCorrectDocumentTree() {
        Document doc = Jsoup.parse("<html><head><title>Test</title></head><body><p>Hello</p></body></html>");
        assertEquals("html", doc.tagName());
        assertEquals("Test", doc.title());
        Element body = doc.body();
        assertEquals(1, body.children().size());
        assertEquals("p", body.child(0).tagName());
        assertEquals("Hello", body.child(0).text());
    }

    @Test
    public void testParseWithDoctype_setsDoctype() {
        Document doc = Jsoup.parse("<!DOCTYPE html><html><body></body></html>");
        assertNotNull(doc.documentType());
        assertEquals("html", doc.documentType().name());
    }

    @Test
    public void testParseWithComments_ignoresCommentsInOutput() {
        Document doc = Jsoup.parse("<!-- comment --><html><body>text</body></html>");
        assertEquals("text", doc.body().text());
    }

    @Test
    public void testParseWithWhitespace_ignoresLeadingWhitespace() {
        Document doc = Jsoup.parse("   \n\t<html><body>text</body></html>");
        assertEquals("text", doc.body().text());
    }

    @Test
    public void testParseSelfClosingTags_insertsVoidElements() {
        Document doc = Jsoup.parse("<html><body><br><hr><img src='a.png'><input type='text'></body></html>");
        Element body = doc.body();
        assertEquals(4, body.children().size());
        assertEquals("br", body.child(0).tagName());
        assertEquals("hr", body.child(1).tagName());
        assertEquals("img", body.child(2).tagName());
        assertEquals("input", body.child(3).tagName());
    }

    @Test
    public void testParseTableStructure_createsCorrectNesting() {
        String html = "<table><caption>Cap</caption><colgroup><col></colgroup><tbody><tr><td>Cell</td></tr></tbody></table>";
        Document doc = Jsoup.parse(html);
        Element table = doc.select("table").first();
        assertNotNull(table);
        assertEquals(3, table.children().size());
        assertEquals("caption", table.child(0).tagName());
        assertEquals("colgroup", table.child(1).tagName());
        assertEquals("tbody", table.child(2).tagName());
        Element tbody = table.child(2);
        assertEquals("tr", tbody.child(0).tagName());
        assertEquals("td", tbody.child(0).child(0).tagName());
        assertEquals("Cell", tbody.child(0).child(0).text());
    }

    @Test
    public void testParseSelectStructure_createsCorrectNesting() {
        String html = "<select><optgroup label='g'><option>O</option></optgroup></select>";
        Document doc = Jsoup.parse(html);
        Element select = doc.select("select").first();
        assertNotNull(select);
        assertEquals("optgroup", select.child(0).tagName());
        assertEquals("option", select.child(0).child(0).tagName());
        assertEquals("O", select.child(0).child(0).text());
    }

    @Test
    public void testParseScriptStyle_storesRawText() {
        Document doc = Jsoup.parse("<head><script>var x=1;</script><style>body{}</style></head><body></body>");
        Elements scripts = doc.select("script");
        assertEquals(1, scripts.size());
        assertEquals("var x=1;", scripts.first().data());
        Elements styles = doc.select("style");
        assertEquals(1, styles.size());
        assertEquals("body{}", styles.first().data());
    }

    @Test
    public void testParseTextarea_storesRawText() {
        Document doc = Jsoup.parse("<body><textarea>Hello</textarea></body>");
        Element textarea = doc.select("textarea").first();
        assertNotNull(textarea);
        assertEquals("Hello", textarea.text());
    }

    @Test
    public void testParseFormAndInput_createsForm() {
        Document doc = Jsoup.parse("<form action='/'><input type='text' name='q' value=''></form>");
        Element form = doc.select("form").first();
        assertNotNull(form);
        Element input = doc.select("input").first();
        assertNotNull(input);
        assertEquals("text", input.attr("type"));
        assertEquals("q", input.attr("name"));
    }

    @Test
    public void testParseNestedLists_handlesNesting() {
        Document doc = Jsoup.parse("<ul><li>One<ul><li>Two</li></ul></li></ul>");
        Element ul = doc.select("ul").first();
        assertEquals("li", ul.child(0).tagName());
        assertEquals("ul", ul.child(0).child(1).tagName());
        assertEquals("li", ul.child(0).child(1).child(0).tagName());
        assertEquals("Two", ul.child(0).child(1).child(0).text());
    }

    @Test
    public void testParseHeadings_handlesMultipleHeadings() {
        Document doc = Jsoup.parse("<h1>Title</h1><h2>Sub</h2>");
        assertEquals(1, doc.select("h1").size());
        assertEquals(1, doc.select("h2").size());
        assertEquals("Title", doc.select("h1").first().text());
        assertEquals("Sub", doc.select("h2").first().text());
    }

    @Test
    public void testParseButton_handlesNestedButton() {
        Document doc = Jsoup.parse("<button>Click<button>Nested</button></button>");
        Elements buttons = doc.select("button");
        assertEquals(2, buttons.size());
        assertEquals("Click", buttons.get(0).text());
        assertEquals("Nested", buttons.get(1).text());
    }

    @Test
    public void testParseNoScriptInBody_handlesContent() {
        Document doc = Jsoup.parse("<body><noscript><p>Fallback</p></noscript></body>");
        Element noscript = doc.select("noscript").first();
        assertNotNull(noscript);
        Element p = noscript.select("p").first();
        assertNotNull(p);
        assertEquals("Fallback", p.text());
    }

    @Test
    public void testParseImageTag_convertsToImg() {
        Document doc = Jsoup.parse("<body><image src='a.jpg' alt='img'></body>");
        Element img = doc.select("img").first();
        assertNotNull(img);
        assertEquals("a.jpg", img.attr("src"));
    }

    @Test
    public void testParseIsindex_handlesDeprecatedTag() {
        Document doc = Jsoup.parse("<isindex prompt='search'>");
        Element form = doc.select("form").first();
        assertNotNull(form);
        Element input = doc.select("input[name=isindex]").first();
        assertNotNull(input);
        Element label = doc.select("label").first();
        assertNotNull(label);
    }

    @Test
    public void testParseMismatchedTags_handlesErrors() {
        Document doc = Jsoup.parse("<p>Para</i><b>Bold</p></b>");
        Element body = doc.body();
        assertNotNull(body);
    }

    @Test
    public void testParseUnclosedScript_handlesEOF() {
        Document doc = Jsoup.parse("<script>var x =");
        Element script = doc.select("script").first();
        assertNotNull(script);
        assertEquals("var x =", script.data());
    }

    @Test
    public void testParseFrameset_framesetStructure() {
        Document doc = Jsoup.parse("<!DOCTYPE html><html><frameset><frame src='a.html'><noframes>No</noframes></frameset></html>");
        assertNotNull(doc.select("frameset").first());
        assertNotNull(doc.select("frame").first());
    }

    @Test
    public void testParseFormInsideTable_fosterParenting() {
        Document doc = Jsoup.parse("<table><form><tr><td>Cell</td></tr></form></table>");
        Element body = doc.body();
        assertEquals(2, body.children().size());
        assertEquals("form", body.child(0).tagName());
        assertEquals("table", body.child(1).tagName());
    }

    @Test
    public void testParseHtmlInBody_errorRecovery() {
        Document doc = Jsoup.parse("<html><body><html attr='test'><p>Text</p></body>");
        assertEquals(1, doc.select("html").size());
        Element html = doc.select("html").first();
        assertNotNull(html);
    }

    @Test
    public void testParseAdoptionAgency_nestedFormattingElements() {
        Document doc = Jsoup.parse("<div><b><i>text</b></i></div>");
        Element div = doc.select("div").first();
        assertNotNull(div);
        assertTrue(div.text().contains("text"));
    }

    @Test
    public void testParsePlaintext_switchesToPlaintextState() {
        Document doc = Jsoup.parse("<plaintext>Hello");
        Element plaintext = doc.select("plaintext").first();
        assertNotNull(plaintext);
        assertEquals("Hello", plaintext.text());
    }
}