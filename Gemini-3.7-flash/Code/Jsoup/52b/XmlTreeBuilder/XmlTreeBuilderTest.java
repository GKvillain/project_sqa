package org.jsoup.parser;

import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.DocumentType;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.nodes.XmlDeclaration;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class XmlTreeBuilderTest {

    // Tests normal XML parsing creating elements and text nodes
    @Test
    public void testParse_simpleXml_createsCorrectDocumentTree() {
        XmlTreeBuilder tb = new XmlTreeBuilder();
        Document doc = tb.parse("<root><child attr=\"val\">hello</child></root>", "http://example.com/");

        assertNotNull(doc);
        assertEquals(1, doc.children().size());
        Element root = doc.child(0);
        assertEquals("root", root.nodeName());
        assertEquals(1, root.children().size());
        Element child = root.child(0);
        assertEquals("child", child.nodeName());
        assertEquals("val", child.attr("attr"));
        assertEquals("hello", child.text());
        assertEquals("http://example.com/", child.baseUri());
    }

    // Tests XML syntax setting on document outputSettings
    @Test
    public void testInitialiseParse_configuresXmlOutputSyntax() {
        XmlTreeBuilder tb = new XmlTreeBuilder();
        Document doc = tb.parse("<root/>", "");

        assertEquals(Document.OutputSettings.Syntax.xml, doc.outputSettings().syntax());
    }

    // Tests self-closing tag handling for unknown XML tags
    @Test
    public void testParse_selfClosingTags_correctlyParsed() {
        XmlTreeBuilder tb = new XmlTreeBuilder();
        Document doc = tb.parse("<root><customTag id=\"1\"/><sibling>text</sibling></root>", "");

        Element root = doc.child(0);
        assertEquals(2, root.children().size());
        assertEquals("customTag", root.child(0).nodeName());
        assertEquals("1", root.child(0).attr("id"));
        assertEquals("sibling", root.child(1).nodeName());
        assertEquals("text", root.child(1).text());
    }

    // Tests comment node insertion
    @Test
    public void testParse_standardComment_insertsCommentNode() {
        XmlTreeBuilder tb = new XmlTreeBuilder();
        Document doc = tb.parse("<root><!-- this is a comment --></root>", "");

        Element root = doc.child(0);
        assertEquals(1, root.childNodeSize());
        assertTrue(root.childNode(0) instanceof Comment);
        Comment comment = (Comment) root.childNode(0);
        assertEquals(" this is a comment ", comment.getData());
        assertFalse(comment.getData().startsWith("?"));
    }

    // Tests XML declaration (bogus comment starting with ?)
    @Test
    public void testParse_xmlDeclarationBogusComment_createsXmlDeclarationNode() {
        XmlTreeBuilder tb = new XmlTreeBuilder();
        Document doc = tb.parse("<?xml version=\"1.0\" encoding=\"UTF-8\"?><root/>", "http://example.com/");

        assertTrue(doc.childNode(0) instanceof XmlDeclaration);
        XmlDeclaration decl = (XmlDeclaration) doc.childNode(0);
        assertEquals("xml version=\"1.0\" encoding=\"UTF-8\"", decl.name());
        assertEquals("http://example.com/", decl.baseUri());
    }

    // Tests declaration starting with ! (bogus comment starting with !)
    @Test
    public void testParse_exclamationBogusComment_createsXmlDeclaration() {
        XmlTreeBuilder tb = new XmlTreeBuilder();
        Document doc = tb.parse("<![CDATA[raw text]]><root/>", "http://example.com/");

        assertTrue(doc.childNode(0) instanceof XmlDeclaration);
        XmlDeclaration decl = (XmlDeclaration) doc.childNode(0);
        assertEquals("[CDATA[raw text]]", decl.name());
    }

    // Tests DocumentType token parsing
    @Test
    public void testParse_docType_insertsDocumentTypeNode() {
        XmlTreeBuilder tb = new XmlTreeBuilder();
        Document doc = tb.parse("<!DOCTYPE html SYSTEM \"about:legacy-compat\"><root/>", "http://example.com/");

        assertTrue(doc.childNode(0) instanceof DocumentType);
        DocumentType doctype = (DocumentType) doc.childNode(0);
        assertEquals("html", doctype.attr("name"));
        assertEquals("about:legacy-compat", doctype.attr("systemId"));
        assertEquals("http://example.com/", doctype.baseUri());
    }

    // Tests character token insertion
    @Test
    public void testParse_characterData_insertsTextNode() {
        XmlTreeBuilder tb = new XmlTreeBuilder();
        Document doc = tb.parse("<root>plain text</root>", "http://example.com/");

        Element root = doc.child(0);
        assertEquals(1, root.childNodeSize());
        assertTrue(root.childNode(0) instanceof TextNode);
        TextNode text = (TextNode) root.childNode(0);
        assertEquals("plain text", text.text());
        assertEquals("http://example.com/", text.baseUri());
    }

    // Tests closing tag popping stack correctly
    @Test
    public void testParse_closingTag_popsStackToMatchingElement() {
        XmlTreeBuilder tb = new XmlTreeBuilder();
        Document doc = tb.parse("<root><a><b>content</b></a><c>after</c></root>", "");

        Element root = doc.child(0);
        assertEquals(2, root.children().size());
        assertEquals("a", root.child(0).nodeName());
        assertEquals("c", root.child(1).nodeName());
        assertEquals("content", root.child(0).child(0).text());
        assertEquals("after", root.child(1).text());
    }

    // Tests unmatched end tag does not pop stack
    @Test
    public void testParse_unmatchedEndTag_isSafelyIgnored() {
        XmlTreeBuilder tb = new XmlTreeBuilder();
        Document doc = tb.parse("<root><p>text</nonexistent></p></root>", "");

        Element root = doc.child(0);
        assertEquals(1, root.children().size());
        Element p = root.child(0);
        assertEquals("p", p.nodeName());
        assertEquals("text", p.text());
    }

    // Tests out-of-order end tag closing parent and intermediate elements
    @Test
    public void testParse_outOfOrderEndTag_popsStackUpToMatchedElement() {
        XmlTreeBuilder tb = new XmlTreeBuilder();
        Document doc = tb.parse("<root><a><b><c>text</a></root>", "");

        Element root = doc.child(0);
        assertEquals(1, root.children().size());
        assertEquals("a", root.child(0).nodeName());
        Element a = root.child(0);
        assertEquals("b", a.child(0).nodeName());
        assertEquals("c", a.child(0).child(0).nodeName());
    }

    // Tests unclosed tags at EOF
    @Test
    public void testParse_unclosedTags_preservesStructureAtEof() {
        XmlTreeBuilder tb = new XmlTreeBuilder();
        Document doc = tb.parse("<root><unclosed>text", "");

        Element root = doc.child(0);
        assertEquals(1, root.children().size());
        Element unclosed = root.child(0);
        assertEquals("unclosed", unclosed.nodeName());
        assertEquals("text", unclosed.text());
    }

    // Tests parseFragment method
    @Test
    public void testParseFragment_validXmlFragment_returnsListOfNodes() {
        XmlTreeBuilder tb = new XmlTreeBuilder();
        List<Node> nodes = tb.parseFragment("<one>1</one><two>2</two>", "http://example.com/", ParseErrorList.noTracking());

        assertNotNull(nodes);
        assertEquals(2, nodes.size());
        assertTrue(nodes.get(0) instanceof Element);
        assertTrue(nodes.get(1) instanceof Element);
        assertEquals("one", nodes.get(0).nodeName());
        assertEquals("two", nodes.get(1).nodeName());
        assertEquals("1", ((Element) nodes.get(0)).text());
        assertEquals("2", ((Element) nodes.get(1)).text());
    }

    // Tests empty string parseFragment
    @Test
    public void testParseFragment_emptyString_returnsEmptyList() {
        XmlTreeBuilder tb = new XmlTreeBuilder();
        List<Node> nodes = tb.parseFragment("", "http://example.com/", ParseErrorList.noTracking());

        assertNotNull(nodes);
        assertEquals(0, nodes.size());
    }

    // Tests process EOF token directly
    @Test
    public void testProcess_eofToken_returnsTrue() {
        XmlTreeBuilder tb = new XmlTreeBuilder();
        tb.initialiseParse("<root/>", "", ParseErrorList.noTracking());
        boolean result = tb.process(new Token.EOF());

        assertTrue(result);
    }
}