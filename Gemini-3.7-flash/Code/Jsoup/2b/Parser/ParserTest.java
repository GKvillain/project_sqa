package org.jsoup.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.junit.Test;

import static org.junit.Assert.*;

public class ParserTest {

    // Tests null html input throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testParse_nullHtml_throwsException() {
        Parser.parse(null, "http://example.com/");
    }

    // Tests null baseUri input throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testParse_nullBaseUri_throwsException() {
        Parser.parse("<p>Hello</p>", null);
    }

    // Tests simple valid HTML document parsing
    @Test
    public void testParse_simpleHtml_returnsParsedDocument() {
        String html = "<html><head><title>Test</title></head><body><p>Hello World</p></body></html>";
        Document doc = Parser.parse(html, "http://example.com/");

        assertEquals("Test", doc.title());
        assertEquals("Hello World", doc.select("p").first().text());
        assertEquals("http://example.com/", doc.baseUri());
    }

    // Tests parsing HTML body fragment
    @Test
    public void testParseBodyFragment_validFragment_returnsBodyWithNodes() {
        String html = "<p>Fragment test</p><span>Span text</span>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com/");

        assertEquals(1, doc.select("p").size());
        assertEquals("Fragment test", doc.select("p").first().text());
        assertEquals(1, doc.select("span").size());
        assertEquals("Span text", doc.select("span").first().text());
    }

    // Tests parsing comments in HTML
    @Test
    public void testParse_htmlWithComment_preservesCommentNode() {
        String html = "<div><!-- This is a comment -->Content</div>";
        Document doc = Parser.parse(html, "http://example.com/");

        Element div = doc.select("div").first();
        assertNotNull(div);
        assertEquals("Content", div.text());
        assertTrue(div.childNode(0) instanceof org.jsoup.nodes.Comment);
    }

    // Tests parsing CDATA section
    @Test
    public void testParse_cdataSection_parsesAsTextNode() {
        String html = "<p><![CDATA[Some <raw> data & chars]]></p>";
        Document doc = Parser.parse(html, "http://example.com/");

        Element p = doc.select("p").first();
        assertNotNull(p);
        assertEquals("Some <raw> data & chars", p.text());
    }

    // Tests parsing XML declaration and DOCTYPE
    @Test
    public void testParse_xmlDeclarationAndDocType_createsDeclNodes() {
        String html = "<?xml version=\"1.0\" encoding=\"utf-8\"?><!DOCTYPE html><html><body><p>Test</p></body></html>";
        Document doc = Parser.parse(html, "http://example.com/");

        assertNotNull(doc.select("p").first());
        assertEquals("Test", doc.select("p").first().text());
    }

    // Tests various attribute formats: single quote, double quote, and unquoted
    @Test
    public void testParse_variousAttributeQuotes_parsesCorrectly() {
        String html = "<a href='http://a.com' id=\"link1\" class=myClass>Link</a>";
        Document doc = Parser.parse(html, "http://example.com/");

        Element a = doc.select("a").first();
        assertNotNull(a);
        assertEquals("http://a.com", a.attr("href"));
        assertEquals("link1", a.id());
        assertEquals("myClass", a.className());
    }

    // Tests boolean / valueless attribute parsing
    @Test
    public void testParse_booleanAttribute_parsesCorrectly() {
        String html = "<input type=\"checkbox\" checked disabled>";
        Document doc = Parser.parse(html, "http://example.com/");

        Element input = doc.select("input").first();
        assertNotNull(input);
        assertEquals("checkbox", input.attr("type"));
        assertTrue(input.hasAttr("checked"));
        assertTrue(input.hasAttr("disabled"));
    }

    // Tests parsing data tags such as script and style
    @Test
    public void testParse_scriptDataTag_preservesRawContent() {
        String html = "<script type=\"text/javascript\">var x = \"<test>\"; alert(x);</script>";
        Document doc = Parser.parse(html, "http://example.com/");

        Element script = doc.select("script").first();
        assertNotNull(script);
        assertEquals("var x = \"<test>\"; alert(x);", script.data());
    }

    // Tests parsing textarea data tag as text
    @Test
    public void testParse_textareaTag_containsTextNode() {
        String html = "<textarea><b>Not Bold</b> &amp; text</textarea>";
        Document doc = Parser.parse(html, "http://example.com/");

        Element textarea = doc.select("textarea").first();
        assertNotNull(textarea);
        assertEquals("<b>Not Bold</b> & text", textarea.text());
    }

    // Tests parsing self-closing tags and empty tags
    @Test
    public void testParse_selfClosingAndEmptyTags_parsedCorrectly() {
        String html = "<div><img src=\"image.png\" /><hr><br/></div>";
        Document doc = Parser.parse(html, "http://example.com/");

        Element div = doc.select("div").first();
        assertNotNull(div);
        assertEquals(3, div.children().size());
        assertEquals("img", div.child(0).tagName());
        assertEquals("image.png", div.child(0).attr("src"));
        assertEquals("hr", div.child(1).tagName());
        assertEquals("br", div.child(2).tagName());
    }

    // Tests base tag updating baseUri for subsequent relative URLs
    @Test
    public void testParse_baseTag_updatesBaseUri() {
        String html = "<html><head><base href=\"http://jsoup.org/path/\"></head><body><a href=\"sub/page.html\">Link</a></body></html>";
        Document doc = Parser.parse(html, "http://example.com/");

        assertEquals("http://jsoup.org/path/", doc.baseUri());
        Element a = doc.select("a").first();
        assertNotNull(a);
        assertEquals("http://jsoup.org/path/sub/page.html", a.absUrl("href"));
    }

    // Tests implicitly created parents when nesting is omitted
    @Test
    public void testParse_implicitParents_createsNecessaryHierarchy() {
        String html = "<td>Orphan cell</td>";
        Document doc = Parser.parse(html, "http://example.com/");

        assertNotNull(doc.select("table tr td").first());
        assertEquals("Orphan cell", doc.select("td").first().text());
    }

    // Tests nested structure and closing tags popping stack correctly
    @Test
    public void testParse_nestedElements_popsStackCorrectly() {
        String html = "<div id=\"outer\"><div id=\"inner\"><p>Text</p></div><span>After inner</span></div>";
        Document doc = Parser.parse(html, "http://example.com/");

        Element outer = doc.select("#outer").first();
        assertNotNull(outer);
        assertEquals(2, outer.children().size());
        assertEquals("inner", outer.child(0).id());
        assertEquals("span", outer.child(1).tagName());
    }

    // Tests unclosed tags auto-closing at appropriate parent boundaries
    @Test
    public void testParse_unclosedTags_autoClosesCorrectly() {
        String html = "<p>First paragraph<p>Second paragraph";
        Document doc = Parser.parse(html, "http://example.com/");

        assertEquals(2, doc.select("p").size());
        assertEquals("First paragraph", doc.select("p").get(0).text());
        assertEquals("Second paragraph", doc.select("p").get(1).text());
    }

    // Tests standalone text node parsing
    @Test
    public void testParse_bareText_parsedIntoBody() {
        String html = "Just plain text without tags";
        Document doc = Parser.parse(html, "http://example.com/");

        assertEquals("Just plain text without tags", doc.body().text());
    }

    // Tests invalid start tag character handling fallback to text
    @Test
    public void testParse_invalidStartTag_handlesAsText() {
        String html = "< 5 is less than 10";
        Document doc = Parser.parse(html, "http://example.com/");

        assertTrue(doc.body().text().contains("< 5 is less than 10") || doc.body().text().contains("&lt; 5"));
    }
}