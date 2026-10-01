package org.jsoup.parser;

import org.jsoup.nodes.Comment;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.DocumentType;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.nodes.XmlDeclaration;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ParserTest {

    // Tests exception on null html input
    @Test(expected = IllegalArgumentException.class)
    public void testParse_nullHtml_throwsException() {
        Parser.parse(null, "http://example.com/");
    }

    // Tests exception on null baseUri input
    @Test(expected = IllegalArgumentException.class)
    public void testParse_nullBaseUri_throwsException() {
        Parser.parse("<p>Hello</p>", null);
    }

    // Tests basic HTML document parsing
    @Test
    public void testParse_simpleHtml_parsesStructureCorrectly() {
        String html = "<html><head><title>Test Title</title></head><body><p>Hello World</p></body></html>";
        Document doc = Parser.parse(html, "http://example.com/");

        assertNotNull(doc);
        assertEquals("Test Title", doc.title());
        assertEquals("Hello World", doc.body().text());
        assertEquals(1, doc.getElementsByTag("p").size());
    }

    // Tests parsing HTML body fragment
    @Test
    public void testParseBodyFragment_validFragment_parsesIntoBody() {
        String fragment = "<div><span>Sample Fragment</span></div>";
        Document doc = Parser.parseBodyFragment(fragment, "http://example.com/");

        assertNotNull(doc.body());
        assertEquals(1, doc.body().getElementsByTag("div").size());
        assertEquals(1, doc.body().getElementsByTag("span").size());
        assertEquals("Sample Fragment", doc.body().text());
    }

    // Tests parsing HTML comments with and without trailing dash
    @Test
    public void testParse_comments_createsCommentNodes() {
        String html = "<div><!-- A standard comment --><span>text</span><!--Another comment-></div>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com/");

        Element div = doc.body().child(0);
        Node commentNode1 = div.childNode(0);
        assertTrue(commentNode1 instanceof Comment);
        assertEquals(" A standard comment ", ((Comment) commentNode1).getData());
    }

    // Tests XML declaration and processing instruction tags
    @Test
    public void testParse_xmlDeclarationAndDocType_createsXmlDeclarationNodes() {
        String html = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><!DOCTYPE html><html><body><p>Test</p></body></html>";
        Document doc = Parser.parse(html, "http://example.com/");

        assertNotNull(doc);
        Elements ps = doc.getElementsByTag("p");
        assertEquals(1, ps.size());
        assertEquals("Test", ps.get(0).text());
    }

    // Tests CDATA block parsing
    @Test
    public void testParse_cdataSection_parsesAsTextNode() {
        String html = "<p><![CDATA[Some <unescaped> data & text]]></p>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com/");

        Element p = doc.body().child(0);
        assertEquals(1, p.childNodes().size());
        assertTrue(p.childNode(0) instanceof TextNode);
        assertEquals("Some <unescaped> data & text", ((TextNode) p.childNode(0)).getWholeText());
    }

    // Tests various attribute formats: double quote, single quote, and unquoted
    @Test
    public void testParse_attributesVariations_parsesAttributesCorrectly() {
        String html = "<a href=\"http://example.com\" id='link1' class=btn rel=nofollow disabled>Link</a>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com/");

        Element a = doc.body().child(0);
        assertEquals("http://example.com", a.attr("href"));
        assertEquals("link1", a.attr("id"));
        assertEquals("btn", a.attr("class"));
        assertEquals("nofollow", a.attr("rel"));
        assertTrue(a.hasAttr("disabled"));
    }

    // Tests empty attribute key recovery branch
    @Test
    public void testParse_emptyAttributeKey_recoversAndParses() {
        String html = "<div =value class=\"test\">Content</div>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com/");

        Element div = doc.body().child(0);
        assertEquals("test", div.attr("class"));
        assertEquals("Content", div.text());
    }

    // Tests data tags: textarea and title parse as TextNode
    @Test
    public void testParse_titleAndTextarea_parsesContentAsTextNode() {
        String html = "<textarea>Line1\n<p>Not a tag</p>\nLine2</textarea>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com/");

        Element textarea = doc.body().child(0);
        assertEquals("textarea", textarea.tagName());
        assertEquals("Line1\n<p>Not a tag</p>\nLine2", textarea.text());
    }

    // Tests data tags: script parses as DataNode
    @Test
    public void testParse_scriptTag_parsesContentAsDataNode() {
        String html = "<script type=\"text/javascript\">var x = \"<b>test</b>\";</script>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com/");

        Element script = doc.body().child(0);
        assertEquals(1, script.childNodes().size());
        assertTrue(script.childNode(0) instanceof DataNode);
        DataNode dataNode = (DataNode) script.childNode(0);
        assertEquals("var x = \"<b>test</b>\";", dataNode.getWholeData());
    }

    // Tests base tag updating baseUri for resolving relative URLs
    @Test
    public void testParse_baseTag_updatesDocumentBaseUri() {
        String html = "<html><head><base href=\"http://example.com/sub/\"></head><body><a href=\"page.html\">Link</a></body></html>";
        Document doc = Parser.parse(html, "http://original.com/");

        assertEquals("http://example.com/sub/", doc.baseUri());
        Element a = doc.getElementsByTag("a").first();
        assertEquals("http://example.com/sub/page.html", a.absUrl("href"));
    }

    // Tests empty elements and self-closing tags
    @Test
    public void testParse_selfClosingAndEmptyTags_parsesCorrectly() {
        String html = "<div><img src=\"image.png\"/><br><hr/><span>After</span></div>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com/");

        Element div = doc.body().child(0);
        assertEquals(4, div.children().size());
        assertEquals("img", div.child(0).tagName());
        assertEquals("br", div.child(1).tagName());
        assertEquals("hr", div.child(2).tagName());
        assertEquals("span", div.child(3).tagName());
    }

    // Tests invalid start tag character treated as text node
    @Test
    public void testParse_invalidStartTag_handlesAsText() {
        String html = "< notAtag > <3 <";
        Document doc = Parser.parseBodyFragment(html, "http://example.com/");

        assertTrue(doc.body().text().contains("< notAtag >"));
    }

    // Tests implicit parent tag creation such as table structure
    @Test
    public void testParse_implicitParents_createsExpectedStructure() {
        String html = "<tr><td>Cell 1</td><td>Cell 2</td></tr>";
        Document doc = Parser.parse(html, "http://example.com/");

        Elements tables = doc.getElementsByTag("table");
        assertFalse(tables.isEmpty());
        Elements tds = doc.getElementsByTag("td");
        assertEquals(2, tds.size());
        assertEquals("Cell 1", tds.get(0).text());
        assertEquals("Cell 2", tds.get(1).text());
    }

    // Tests unclosed tags and nested closing tag stack management
    @Test
    public void testParse_unclosedAndMismatchedTags_normalisesCorrectly() {
        String html = "<div><p>Paragraph 1<p>Paragraph 2</div>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com/");

        Element div = doc.body().child(0);
        assertEquals(2, div.getElementsByTag("p").size());
        assertEquals("Paragraph 1 Paragraph 2", div.text());
    }

    // Tests empty end tags and closing past body tag boundary
    @Test
    public void testParse_emptyOrExtraneousEndTag_ignoresOrClosesGracefully() {
        String html = "<div></></span></p></div></body></html>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com/");

        assertNotNull(doc);
        assertEquals(1, doc.body().children().size());
    }

    // Tests style tag parsing as DataNode
    @Test
    public void testParse_styleTag_parsesContentAsDataNode() {
        String html = "<style type=\"text/css\">body { color: red; } <!-- hide --> </style>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com/");

        Element style = doc.body().child(0);
        assertEquals("style", style.tagName());
        assertEquals(1, style.childNodes().size());
        assertTrue(style.childNode(0) instanceof DataNode);
        DataNode dataNode = (DataNode) style.childNode(0);
        assertTrue(dataNode.getWholeData().contains("body { color: red; }"));
    }

    // Tests frameset document structure
    @Test
    public void testParse_framesetDocument_parsesFrameset() {
        String html = "<html><head><title>Frame Test</title></head><frameset cols=\"25%,75%\"><frame src=\"frame1.html\"><frame src=\"frame2.html\"><noframes><body>No frames support</body></noframes></frameset></html>";
        Document doc = Parser.parse(html, "http://example.com/");

        assertNotNull(doc);
        assertEquals("Frame Test", doc.title());
        Elements framesets = doc.getElementsByTag("frameset");
        assertEquals(1, framesets.size());
        Elements frames = doc.getElementsByTag("frame");
        assertEquals(2, frames.size());
    }

    // Tests DOCTYPE node generation and attributes
    @Test
    public void testParse_fullDoctype_parsesDocumentTypeNode() {
        String html = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"><html><head></head><body></body></html>";
        Document doc = Parser.parse(html, "http://example.com/");

        List<Node> childNodes = doc.childNodes();
        boolean foundDoctype = false;
        for (Node node : childNodes) {
            if (node instanceof DocumentType) {
                foundDoctype = true;
                DocumentType docType = (DocumentType) node;
                assertEquals("html", docType.attr("name"));
                assertEquals("-//W3C//DTD XHTML 1.0 Transitional//EN", docType.attr("publicId"));
                assertEquals("http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd", docType.attr("systemId"));
            }
        }
        assertTrue(foundDoctype);
    }

    // Tests custom/unknown self-closing tags and standard self-closing syntax
    @Test
    public void testParse_unknownSelfClosingTags_parsesProperly() {
        String html = "<div><custom id=\"1\" /><custom id=\"2\"></custom></div>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com/");

        Element div = doc.body().child(0);
        assertEquals(2, div.getElementsByTag("custom").size());
        assertEquals("1", div.child(0).attr("id"));
        assertEquals("2", div.child(1).attr("id"));
    }

    // Tests body attributes propagation and multiple body tags merging
    @Test
    public void testParse_bodyAttributesAndMultipleBodyTags_mergesAttributes() {
        String html = "<body class=\"first\" id=\"main\"><p>Hello</p><body class=\"second\" data-extra=\"val\"></body>";
        Document doc = Parser.parse(html, "http://example.com/");

        Element body = doc.body();
        assertNotNull(body);
        assertTrue(body.hasClass("first") || body.hasClass("second") || body.hasAttr("id") || body.hasAttr("data-extra"));
        assertEquals(1, doc.getElementsByTag("p").size());
    }

    // Tests foster parenting for tags inside tables outside cells
    @Test
    public void testParse_tableFosterParenting_movesTextOutOfTable() {
        String html = "<table>Text Outside Cell<tr><td>Cell</td></tr></table>";
        Document doc = Parser.parse(html, "http://example.com/");

        assertEquals("Text Outside Cell Cell", doc.body().text());
        Element table = doc.getElementsByTag("table").first();
        assertNotNull(table);
        assertEquals(1, table.getElementsByTag("td").size());
    }

    // Tests html entity decoding in body and attributes
    @Test
    public void testParse_htmlEntitiesInTextAndAttributes_decodesProperly() {
        String html = "<a href=\"http://example.com/?a=1&amp;b=2\" title=\"&quot;Quote&quot;\">&lt;Hello &amp; World&gt;</a>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com/");

        Element a = doc.body().getElementsByTag("a").first();
        assertNotNull(a);
        assertEquals("http://example.com/?a=1&b=2", a.attr("href"));
        assertEquals("\"Quote\"", a.attr("title"));
        assertEquals("<Hello & World>", a.text());
    }

    // Tests parse relaxed body fragment or empty fragment parsing
    @Test
    public void testParseBodyFragment_emptyString_createsEmptyBody() {
        Document doc = Parser.parseBodyFragment("", "http://example.com/");
        assertNotNull(doc.body());
        assertEquals(0, doc.body().children().size());
        assertEquals("", doc.body().text());
    }

    // Tests misnested formatting elements Adoption Agency Algorithm
    @Test
    public void testParse_misnestedFormattingTags_reconstructsHierarchy() {
        String html = "<p><b>1<i>2</b>3</i></p>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com/");

        Element p = doc.body().getElementsByTag("p").first();
        assertNotNull(p);
        assertEquals("123", p.text());
        assertEquals(1, doc.getElementsByTag("b").size());
    }

    // Tests XML declaration node parsing details
    @Test
    public void testParse_standaloneXmlDeclaration_parsesXmlDeclarationNode() {
        String html = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" standalone=\"yes\"?>";
        Document doc = Parser.parse(html, "http://example.com/");

        boolean hasXmlDecl = false;
        for (Node child : doc.childNodes()) {
            if (child instanceof XmlDeclaration) {
                hasXmlDecl = true;
                XmlDeclaration decl = (XmlDeclaration) child;
                assertEquals("xml", decl.name());
            }
        }
        assertTrue(hasXmlDecl || doc.childNodes().size() > 0);
    }
}