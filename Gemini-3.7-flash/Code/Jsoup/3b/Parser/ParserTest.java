package org.jsoup.parser;

import org.jsoup.nodes.Comment;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.nodes.XmlDeclaration;
import org.junit.Test;

import static org.junit.Assert.*;

public class ParserTest {

    // Tests normal HTML document parsing
    @Test
    public void testParse_standardHtml_returnsConstructedDocument() {
        String html = "<html><head><title>Test Title</title></head><body><p class=\"intro\">Hello World</p></body></html>";
        Document doc = Parser.parse(html, "http://example.com");

        assertNotNull(doc);
        assertEquals("Test Title", doc.title());
        assertEquals("Hello World", doc.select("p").text());
        assertEquals("intro", doc.select("p").attr("class"));
    }

    // Tests body fragment parsing
    @Test
    public void testParseBodyFragment_validFragment_parsedInsideBody() {
        String html = "<div><span>Fragment text</span></div>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com");

        assertNotNull(doc);
        assertNotNull(doc.body());
        assertEquals("Fragment text", doc.body().select("span").text());
    }

    // Tests null html parameter throwing exception
    @Test(expected = IllegalArgumentException.class)
    public void testParse_nullHtml_throwsException() {
        Parser.parse(null, "http://example.com");
    }

    // Tests null baseUri parameter throwing exception
    @Test(expected = IllegalArgumentException.class)
    public void testParse_nullBaseUri_throwsException() {
        Parser.parse("<p>Hello</p>", null);
    }

    // Tests parsing HTML comments
    @Test
    public void testParse_htmlComment_parsedCorrectly() {
        String html = "<div><!-- This is a comment -->Content</div>";
        Document doc = Parser.parse(html, "http://example.com");

        Element div = doc.select("div").first();
        assertNotNull(div);
        assertTrue(div.childNode(0) instanceof Comment);
        assertEquals(" This is a comment ", ((Comment) div.childNode(0)).getData());
    }

    // Tests parsing CDATA section
    @Test
    public void testParse_cdataSection_parsedAsTextNode() {
        String html = "<p><![CDATA[Some <raw> & unescaped text]]></p>";
        Document doc = Parser.parse(html, "http://example.com");

        Element p = doc.select("p").first();
        assertNotNull(p);
        assertTrue(p.childNode(0) instanceof TextNode);
        assertEquals("Some <raw> & unescaped text", ((TextNode) p.childNode(0)).getWholeText());
    }

    // Tests parsing XML declaration and doctype
    @Test
    public void testParse_xmlDeclarationAndDoctype_parsedCorrectly() {
        String html = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><!DOCTYPE html><html><body></body></html>";
        Document doc = Parser.parse(html, "http://example.com");

        assertNotNull(doc);
        boolean hasXmlDecl = false;
        for (int i = 0; i < doc.childNodes().size(); i++) {
            if (doc.childNode(i) instanceof XmlDeclaration) {
                hasXmlDecl = true;
                break;
            }
        }
        assertTrue(hasXmlDecl);
    }

    // Tests various attribute quote styles (single, double, unquoted)
    @Test
    public void testParse_variousAttributeStyles_attributesExtractedCorrectly() {
        String html = "<a href='single.html' target=\"_blank\" rel=nofollow>Link</a>";
        Document doc = Parser.parse(html, "http://example.com");

        Element a = doc.select("a").first();
        assertNotNull(a);
        assertEquals("single.html", a.attr("href"));
        assertEquals("_blank", a.attr("target"));
        assertEquals("nofollow", a.attr("rel"));
    }

    // Tests data tags (script and textarea) handling
    @Test
    public void testParse_dataTags_parsedAsDataAndTextNodes() {
        String html = "<script>var x = \"<test>\";</script><textarea>Some <encoded> text</textarea>";
        Document doc = Parser.parse(html, "http://example.com");

        Element script = doc.select("script").first();
        assertNotNull(script);
        assertTrue(script.childNode(0) instanceof DataNode);
        assertEquals("var x = \"<test>\";", ((DataNode) script.childNode(0)).getWholeData());

        Element textarea = doc.select("textarea").first();
        assertNotNull(textarea);
        assertTrue(textarea.childNode(0) instanceof TextNode);
    }

    // Tests base href updating document baseUri
    @Test
    public void testParse_baseTag_updatesDocumentBaseUri() {
        String html = "<html><head><base href=\"http://jsoup.org/path/\"></head><body><a href=\"sub\">Link</a></body></html>";
        Document doc = Parser.parse(html, "http://example.com");

        assertEquals("http://jsoup.org/path/", doc.baseUri());
        Element a = doc.select("a").first();
        assertNotNull(a);
        assertEquals("http://jsoup.org/path/sub", a.absUrl("href"));
    }

    // Tests invalid start tag tokenized as plain text
    @Test
    public void testParse_invalidStartTag_handledAsText() {
        String html = "< notatag> text";
        Document doc = Parser.parse(html, "http://example.com");

        assertEquals("< notatag> text", doc.body().text());
    }

    // Tests self-closing and empty elements
    @Test
    public void testParse_selfClosingAndEmptyTags_parsedCorrectly() {
        String html = "<div><img src=\"pic.jpg\" /><hr><br/></div>";
        Document doc = Parser.parse(html, "http://example.com");

        Element div = doc.select("div").first();
        assertNotNull(div);
        assertNotNull(div.select("img").first());
        assertNotNull(div.select("hr").first());
        assertNotNull(div.select("br").first());
    }

    // Tests implicit ancestor tags creation
    @Test
    public void testParse_missingStructuralTags_implicitParentsCreated() {
        String html = "<td>Orphan cell</td>";
        Document doc = Parser.parse(html, "http://example.com");

        assertNotNull(doc.select("table").first());
        assertNotNull(doc.select("tr").first());
        assertNotNull(doc.select("td").first());
        assertEquals("Orphan cell", doc.select("td").text());
    }

    // Tests unclosed tags auto-closing on stack
    @Test
    public void testParse_unclosedTags_closedProperly() {
        String html = "<p>First paragraph<p>Second paragraph";
        Document doc = Parser.parse(html, "http://example.com");

        assertEquals(2, doc.select("p").size());
        assertEquals("First paragraph", doc.select("p").get(0).text());
        assertEquals("Second paragraph", doc.select("p").get(1).text());
    }

    // Tests parsing empty HTML string
    @Test
    public void testParse_emptyString_returnsEmptyDocument() {
        Document doc = Parser.parse("", "http://example.com");

        assertNotNull(doc);
        assertEquals("", doc.body().text());
    }
}