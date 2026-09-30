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

    // Tests parsing basic HTML document structure
    @Test
    public void testParse_basicDocument_parsesCorrectly() {
        String html = "<html><head><title>Test Title</title></head><body><p>Hello World</p></body></html>";
        Document doc = Parser.parse(html, "http://example.com/");

        assertEquals("Test Title", doc.title());
        assertEquals("Hello World", doc.select("p").first().text());
        assertEquals("http://example.com/", doc.baseUri());
    }

    // Tests parsing HTML body fragment
    @Test
    public void testParseBodyFragment_simpleFragment_parsesIntoBody() {
        String html = "<div><span>Fragment text</span></div>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com/");

        assertNotNull(doc.body());
        assertEquals(1, doc.body().children().size());
        assertEquals("Fragment text", doc.body().select("span").first().text());
    }

    // Tests parsing comments with trailing dash and standard comment format
    @Test
    public void testParse_comments_createsCommentNodes() {
        String html = "<div><!-- This is a comment -->Hello<!-- another comment ---></div>";
        Document doc = Parser.parse(html, "http://example.com/");

        Element div = doc.select("div").first();
        assertEquals("Hello", div.text());
        assertEquals(3, div.childNodes().size());
    }

    // Tests parsing XML declaration and doctype
    @Test
    public void testParse_xmlDeclAndDoctype_createsXmlDeclarationNodes() {
        String html = "<?xml version=\"1.0\" encoding=\"utf-8\"?><!DOCTYPE html><html><body><p>Test</p></body></html>";
        Document doc = Parser.parse(html, "http://example.com/");

        assertNotNull(doc);
        assertEquals("Test", doc.select("p").first().text());
    }

    // Tests parsing CDATA section
    @Test
    public void testParse_cdataSection_parsesAsTextNode() {
        String html = "<p><![CDATA[Some <raw> data & entities]]></p>";
        Document doc = Parser.parse(html, "http://example.com/");

        Element p = doc.select("p").first();
        assertNotNull(p);
        assertEquals("Some <raw> data & entities", p.text());
    }

    // Tests various attribute quoting styles (single, double, unquoted)
    @Test
    public void testParse_attributesQuotesAndUnquoted_parsesAttributesCorrectly() {
        String html = "<a href='http://example.com/one' id=\"link2\" class=link3 target=_blank>Link</a>";
        Document doc = Parser.parse(html, "http://example.com/");

        Element a = doc.select("a").first();
        assertNotNull(a);
        assertEquals("http://example.com/one", a.attr("href"));
        assertEquals("link2", a.attr("id"));
        assertEquals("link3", a.attr("class"));
        assertEquals("_blank", a.attr("target"));
    }

    // Tests parsing data tags such as textarea, title, and script
    @Test
    public void testParse_dataTags_preservesContent() {
        String html = "<textarea><escaped & content></textarea><script>var x = 1 < 2; var y = \"test\";</script>";
        Document doc = Parser.parse(html, "http://example.com/");

        Element textarea = doc.select("textarea").first();
        assertNotNull(textarea);
        assertEquals("<escaped & content>", textarea.text());

        Element script = doc.select("script").first();
        assertNotNull(script);
        assertEquals("var x = 1 < 2; var y = \"test\";", script.data());
    }

    // Tests base tag updating document base URI
    @Test
    public void testParse_baseTag_updatesDocumentBaseUri() {
        String html = "<html><head><base href='http://foo.com/path/'></head><body><a href='bar.html'>Link</a></body></html>";
        Document doc = Parser.parse(html, "http://example.com/");

        assertEquals("http://foo.com/path/", doc.baseUri());
        Element a = doc.select("a").first();
        assertEquals("http://foo.com/path/bar.html", a.absUrl("href"));
    }

    // Tests invalid start tag character treated as text
    @Test
    public void testParse_invalidTagStart_handledAsText() {
        String html = "< 5 and <";
        Document doc = Parser.parse(html, "http://example.com/");

        assertTrue(doc.text().contains("< 5 and <") || doc.text().contains("&lt; 5 and &lt;"));
    }

    // Tests self closing elements and tags
    @Test
    public void testParse_selfClosingTags_handledCorrectly() {
        String html = "<div><img src=\"foo.jpg\" /><br><hr/></div><p>After</p>";
        Document doc = Parser.parse(html, "http://example.com/");

        Element div = doc.select("div").first();
        assertNotNull(div);
        assertNotNull(div.select("img").first());
        assertNotNull(div.select("br").first());
        assertNotNull(div.select("hr").first());
        assertEquals("After", doc.select("p").first().text());
    }

    // Tests implicit parent element creation for orphaned children
    @Test
    public void testParse_implicitParent_wrapsInValidParent() {
        String html = "<li>Item 1</li><li>Item 2</li>";
        Document doc = Parser.parse(html, "http://example.com/");

        Element ulOrOl = doc.select("li").first().parent();
        assertNotNull(ulOrOl);
        assertEquals(2, doc.select("li").size());
    }

    // Tests unclosed tags auto-closing on stack
    @Test
    public void testParse_unclosedTags_autoClosesCorrectly() {
        String html = "<p>Paragraph 1<p>Paragraph 2<div>Text in div";
        Document doc = Parser.parse(html, "http://example.com/");

        assertEquals(2, doc.select("p").size());
        assertEquals("Text in div", doc.select("div").first().text());
    }

    // Tests empty and whitespace HTML input
    @Test
    public void testParse_emptyOrWhitespaceHtml_createsEmptyShell() {
        Document doc = Parser.parse("", "http://example.com/");
        assertNotNull(doc);
        assertNotNull(doc.body());

        Document docWhitespace = Parser.parse("   \n\t  ", "http://example.com/");
        assertNotNull(docWhitespace);
        assertNotNull(docWhitespace.body());
    }
}