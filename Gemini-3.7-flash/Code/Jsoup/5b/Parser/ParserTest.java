package org.jsoup.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.junit.Test;

import static org.junit.Assert.*;

public class ParserTest {

    // Tests normal HTML document parsing with standard tags
    @Test
    public void testParse_standardHtml_returnsPopulatedDocument() {
        String html = "<html><head><title>Test Page</title></head><body><p id=\"p1\">Hello World</p></body></html>";
        Document doc = Parser.parse(html, "http://example.com/");

        assertNotNull(doc);
        assertEquals("Test Page", doc.title());
        Element p = doc.getElementById("p1");
        assertNotNull(p);
        assertEquals("Hello World", p.text());
        assertEquals("p", p.tagName());
    }

    // Tests null HTML input throwing exception
    @Test(expected = IllegalArgumentException.class)
    public void testParse_nullHtml_throwsException() {
        Parser.parse(null, "http://example.com/");
    }

    // Tests null baseUri input throwing exception
    @Test(expected = IllegalArgumentException.class)
    public void testParse_nullBaseUri_throwsException() {
        Parser.parse("<p>Hello</p>", null);
    }

    // Tests parsing body fragment into body
    @Test
    public void testParseBodyFragment_fragmentHtml_createsDocumentShellWithBodyContent() {
        String fragment = "<div><span>Fragment text</span></div>";
        Document doc = Parser.parseBodyFragment(fragment, "http://example.com/");

        assertNotNull(doc);
        assertNotNull(doc.body());
        Elements divs = doc.body().getElementsByTag("div");
        assertEquals(1, divs.size());
        assertEquals("Fragment text", divs.get(0).text());
    }

    // Tests relaxed body fragment parsing
    @Test
    public void testParseBodyFragmentRelaxed_unbalancedFragment_parsesWithoutImplicitCreation() {
        String fragment = "<div><span>Text</span>";
        Document doc = Parser.parseBodyFragmentRelaxed(fragment, "http://example.com/");

        assertNotNull(doc);
        assertEquals("Text", doc.body().text());
    }

    // Tests parsing HTML comments (both standard and trailing dash forms)
    @Test
    public void testParse_commentNodes_createsCommentInTree() {
        String html = "<div><!-- This is a comment --><span>Content</span><!-- Another comment -></div>";
        Document doc = Parser.parse(html, "http://example.com/");

        Element div = doc.getElementsByTag("div").first();
        assertNotNull(div);
        assertEquals(3, div.childNodes().size());
        assertTrue(div.childNode(0) instanceof org.jsoup.nodes.Comment);
        org.jsoup.nodes.Comment comment = (org.jsoup.nodes.Comment) div.childNode(0);
        assertEquals(" This is a comment ", comment.getData());
    }

    // Tests parsing XML declaration and DOCTYPE tags
    @Test
    public void testParse_xmlDeclarationAndDoctype_createsXmlDeclarationNodes() {
        String html = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><!DOCTYPE html><html><body>Test</body></html>";
        Document doc = Parser.parse(html, "http://example.com/");

        assertNotNull(doc);
        assertTrue(doc.childNodes().size() > 0);
        assertTrue(doc.childNode(0) instanceof org.jsoup.nodes.XmlDeclaration);
        org.jsoup.nodes.XmlDeclaration decl = (org.jsoup.nodes.XmlDeclaration) doc.childNode(0);
        assertEquals("xml version=\"1.0\" encoding=\"UTF-8\"", decl.getWholeDeclaration());
    }

    // Tests parsing CDATA section
    @Test
    public void testParse_cdataSection_parsedAsTextNode() {
        String html = "<p><![CDATA[Some <raw> CDATA text & content]]></p>";
        Document doc = Parser.parse(html, "http://example.com/");

        Element p = doc.getElementsByTag("p").first();
        assertNotNull(p);
        assertEquals("Some <raw> CDATA text & content", p.text());
    }

    // Tests parsing data tags such as script and style
    @Test
    public void testParse_scriptAndStyleTags_preservesRawContent() {
        String html = "<script>var a = 1 < 2; var b = 'test';</script><style>body > div { color: red; }</style>";
        Document doc = Parser.parse(html, "http://example.com/");

        Element script = doc.getElementsByTag("script").first();
        assertNotNull(script);
        assertEquals("var a = 1 < 2; var b = 'test';", script.data());

        Element style = doc.getElementsByTag("style").first();
        assertNotNull(style);
        assertEquals("body > div { color: red; }", style.data());
    }

    // Tests parsing textarea data tag as text
    @Test
    public void testParse_textareaTag_containsTextContent() {
        String html = "<textarea>Sample <tag> inside textarea</textarea>";
        Document doc = Parser.parse(html, "http://example.com/");

        Element textarea = doc.getElementsByTag("textarea").first();
        assertNotNull(textarea);
        assertEquals("Sample <tag> inside textarea", textarea.text());
    }

    // Tests various attribute formats (single quote, double quote, unquoted, empty value)
    @Test
    public void testParse_variousAttributeFormats_parsedCorrectly() {
        String html = "<div id='single' class=\"double\" data-unquoted=unquotedVal disabled></div>";
        Document doc = Parser.parse(html, "http://example.com/");

        Element div = doc.getElementsByTag("div").first();
        assertNotNull(div);
        assertEquals("single", div.attr("id"));
        assertEquals("double", div.attr("class"));
        assertEquals("unquotedVal", div.attr("data-unquoted"));
        assertTrue(div.hasAttr("disabled"));
    }

    // Tests malformed attributes without valid key
    @Test
    public void testParse_malformedAttributeWithoutKey_handlesGracefully() {
        String html = "<a =\"foo\" href=\"http://example.com\" \"\" =bar>Link</a>";
        Document doc = Parser.parse(html, "http://example.com/");

        Element a = doc.getElementsByTag("a").first();
        assertNotNull(a);
        assertEquals("http://example.com", a.attr("href"));
        assertEquals("Link", a.text());
    }

    // Tests base tag updating the base URI
    @Test
    public void testParse_baseTag_updatesDocumentBaseUri() {
        String html = "<html><head><base href=\"http://jsoup.org/path/\"></head><body><a href=\"sub\">Link</a></body></html>";
        Document doc = Parser.parse(html, "http://initial.com/");

        Element a = doc.getElementsByTag("a").first();
        assertNotNull(a);
        assertEquals("http://jsoup.org/path/sub", a.absUrl("href"));
    }

    // Tests self-closing and empty elements (known tag and unknown tag)
    @Test
    public void testParse_selfClosingTags_handledProperly() {
        String html = "<div><img src=\"image.png\" /><custom-tag id=\"c1\" /><br></div>";
        Document doc = Parser.parse(html, "http://example.com/");

        Element div = doc.getElementsByTag("div").first();
        assertNotNull(div);
        assertEquals(3, div.children().size());
        assertEquals("image.png", div.select("img").first().attr("src"));
        assertEquals("c1", div.select("custom-tag").first().attr("id"));
    }

    // Tests text node containing standalone '<' character
    @Test
    public void testParse_textNodeWithLessThanCharacter_parsesBothTextParts() {
        String html = "<p>5 < 10 and 10 > 5</p>";
        Document doc = Parser.parse(html, "http://example.com/");

        Element p = doc.getElementsByTag("p").first();
        assertNotNull(p);
        assertEquals("5 < 10 and 10 > 5", p.text());
    }

    // Tests implicit parent tag generation (e.g., table cells creating implicit table structure)
    @Test
    public void testParse_elementsRequiringParent_createsImplicitStructure() {
        String html = "<td>Cell 1</td><td>Cell 2</td>";
        Document doc = Parser.parse(html, "http://example.com/");

        Elements cells = doc.getElementsByTag("td");
        assertEquals(2, cells.size());
        assertEquals("Cell 1", cells.get(0).text());
        assertEquals("Cell 2", cells.get(1).text());
        assertNotNull(doc.getElementsByTag("table").first());
    }

    // Tests closing tags out of order and popStackToClose boundary conditions
    @Test
    public void testParse_mismatchedAndExcessEndTags_handledGracefully() {
        String html = "<div><p>Paragraph</span></div></p></body></html>";
        Document doc = Parser.parse(html, "http://example.com/");

        Element div = doc.getElementsByTag("div").first();
        assertNotNull(div);
        Element p = div.getElementsByTag("p").first();
        assertNotNull(p);
        assertEquals("Paragraph", p.text());
    }
}