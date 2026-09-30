package org.jsoup.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.junit.Test;

import static org.junit.Assert.*;

public class ParserTest {

    // Tests normal HTML parsing with full structure
    @Test
    public void testParse_standardHtml_returnsPopulatedDocument() {
        String html = "<html><head><title>Test Title</title></head><body><p id=\"p1\">Hello World</p></body></html>";
        Document doc = Parser.parse(html, "http://example.com");

        assertNotNull(doc);
        assertEquals("Test Title", doc.title());
        Element p = doc.getElementById("p1");
        assertNotNull(p);
        assertEquals("Hello World", p.text());
    }

    // Tests attribute parsing with double quotes, single quotes, and unquoted values
    @Test
    public void testParse_variousAttributeStyles_parsedCorrectly() {
        String html = "<a href=\"http://example.com/1\" class='link-class' target=_blank>Link</a>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com");

        Element a = doc.select("a").first();
        assertNotNull(a);
        assertEquals("http://example.com/1", a.attr("href"));
        assertEquals("link-class", a.attr("class"));
        assertEquals("_blank", a.attr("target"));
    }

    // Tests handling of empty or invalid attribute characters (Defects4J bug 5 regression)
    @Test
    public void testParse_invalidAttributeSyntax_parsesWithoutException() {
        String html = "<a =badKey href='http://example.com' \"emptyKey\">Test</a>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com");

        Element a = doc.select("a").first();
        assertNotNull(a);
        assertEquals("http://example.com", a.attr("href"));
    }

    // Tests parsing HTML comments including dash handling
    @Test
    public void testParse_htmlComments_parsedAsCommentNode() {
        String html = "<div><!-- This is a regular comment --></div><div><!-- Comment with trailing dash - --></div>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com");

        assertEquals(2, doc.body().children().size());
        assertEquals(1, doc.body().child(0).childNodes().size());
        assertEquals(1, doc.body().child(1).childNodes().size());
    }

    // Tests parsing XML Declarations and processing instructions
    @Test
    public void testParse_xmlDeclarationAndDoctype_parsedCorrectly() {
        String html = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><!DOCTYPE html><html><body><p>Text</p></body></html>";
        Document doc = Parser.parse(html, "http://example.com");

        assertNotNull(doc);
        assertEquals("Text", doc.select("p").first().text());
    }

    // Tests CDATA section parsing
    @Test
    public void testParse_cdataSection_parsedAsTextNode() {
        String html = "<p><![CDATA[some raw <unescaped> data]]></p>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com");

        Element p = doc.select("p").first();
        assertNotNull(p);
        assertEquals("some raw <unescaped> data", p.text());
    }

    // Tests data-only tags: script, title, textarea
    @Test
    public void testParse_dataTags_preservesContent() {
        String html = "<title>Page &amp; Title</title><script>var x = \"<b>test</b>\";</script><textarea>Line 1\nLine 2</textarea>";
        Document doc = Parser.parse(html, "http://example.com");

        assertEquals("Page & Title", doc.title());
        Element script = doc.head().getElementsByTag("script").first();
        assertNotNull(script);
        assertEquals("var x = \"<b>test</b>\";", script.data());

        Element textarea = doc.body().getElementsByTag("textarea").first();
        assertNotNull(textarea);
        assertEquals("Line 1\nLine 2", textarea.text());
    }

    // Tests base href handling updates document baseUri
    @Test
    public void testParse_baseHrefTag_updatesBaseUri() {
        String html = "<head><base href=\"http://jsoup.org/path/\"><link href=\"style.css\"></head>";
        Document doc = Parser.parse(html, "http://example.com");

        assertEquals("http://jsoup.org/path/", doc.baseUri());
        Element link = doc.head().getElementsByTag("link").first();
        assertNotNull(link);
        assertEquals("http://jsoup.org/path/style.css", link.absUrl("href"));
    }

    // Tests self closing and empty tags
    @Test
    public void testParse_selfClosingTags_handledCorrectly() {
        String html = "<div><img src=\"test.jpg\" /><custom-tag id=\"custom\" /><span>After</span></div>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com");

        Element div = doc.select("div").first();
        assertNotNull(div);
        assertEquals(3, div.children().size());
        assertEquals("img", div.child(0).tagName());
        assertEquals("custom-tag", div.child(1).tagName());
        assertEquals("span", div.child(2).tagName());
    }

    // Tests text node containing isolated '<' character
    @Test
    public void testParse_textNodeWithIsolatedLessThan_parsedCorrectly() {
        String html = "<p>5 < 10 and 10 > 5</p>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com");

        Element p = doc.select("p").first();
        assertNotNull(p);
        assertEquals("5 < 10 and 10 > 5", p.text());
    }

    // Tests implicit parent tag generation when tags are out of order
    @Test
    public void testParse_implicitParentTags_automaticallyCreated() {
        String html = "<tr><td>Cell 1</td></tr>";
        Document doc = Parser.parse(html, "http://example.com");

        Element table = doc.body().getElementsByTag("table").first();
        assertNotNull(table);
        Element td = table.select("td").first();
        assertNotNull(td);
        assertEquals("Cell 1", td.text());
    }

    // Tests relaxed body fragment parsing without implicit parent creation
    @Test
    public void testParseBodyFragmentRelaxed_doesNotCreateImplicitParents() {
        String html = "<td>Cell Without Table</td>";
        Document doc = Parser.parseBodyFragmentRelaxed(html, "http://example.com");

        assertNotNull(doc.body());
        Element td = doc.body().select("td").first();
        assertNotNull(td);
        assertEquals("Cell Without Table", td.text());
        assertEquals(0, doc.body().getElementsByTag("table").size());
    }

    // Tests unmatched and unclosed end tags
    @Test
    public void testParse_unmatchedEndTags_ignoredGracefully() {
        String html = "</div></p><p>Valid Paragraph</p></span>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com");

        Element p = doc.select("p").first();
        assertNotNull(p);
        assertEquals("Valid Paragraph", p.text());
    }

    // Tests null html parameter throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testParse_nullHtml_throwsException() {
        Parser.parse(null, "http://example.com");
    }

    // Tests null baseUri parameter throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testParse_nullBaseUri_throwsException() {
        Parser.parse("<div>test</div>", null);
    }
}