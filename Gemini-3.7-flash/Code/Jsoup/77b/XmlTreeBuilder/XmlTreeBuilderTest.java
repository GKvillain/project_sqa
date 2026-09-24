package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.junit.Before;
import org.junit.Test;

import java.io.StringReader;
import java.util.List;

import static org.junit.Assert.*;

public class XmlTreeBuilderTest {

    private XmlTreeBuilder treeBuilder;

    @Before
    public void setUp() {
        treeBuilder = new XmlTreeBuilder();
    }

    // Tests default settings preserving case
    @Test
    public void testDefaultSettings_returnsPreserveCase() {
        ParseSettings settings = treeBuilder.defaultSettings();
        assertTrue(settings.preserveTagCase());
        assertTrue(settings.preserveAttributeCase());
    }

    // Tests parsing valid XML from string with nested elements
    @Test
    public void testParse_simpleXmlString_createsCorrectTree() {
        String xml = "<root><child id=\"1\">Hello</child></root>";
        Document doc = treeBuilder.parse(xml, "http://example.com");

        assertEquals(Document.OutputSettings.Syntax.xml, doc.outputSettings().syntax());
        assertEquals("root", doc.child(0).tagName());
        assertEquals("child", doc.child(0).child(0).tagName());
        assertEquals("1", doc.child(0).child(0).attr("id"));
        assertEquals("Hello", doc.child(0).child(0).text());
    }

    // Tests parsing XML from Reader
    @Test
    public void testParse_readerInput_createsCorrectTree() {
        String xml = "<item><name>Test</name></item>";
        Document doc = treeBuilder.parse(new StringReader(xml), "http://example.com");

        assertNotNull(doc);
        assertEquals("item", doc.child(0).tagName());
        assertEquals("Test", doc.child(0).child(0).text());
    }

    // Tests self-closing tag for unknown XML tag
    @Test
    public void testInsert_selfClosingUnknownTag_setsSelfClosing() {
        String xml = "<root><customSelfClosing attr=\"val\"/></root>";
        Document doc = treeBuilder.parse(xml, "http://example.com");

        Element customEl = doc.child(0).child(0);
        assertEquals("customSelfClosing", customEl.tagName());
        assertTrue(customEl.tag().isSelfClosing());
    }

    // Tests self-closing tag for known HTML tag in XML mode
    @Test
    public void testInsert_selfClosingKnownTag_parsedCorrectly() {
        String xml = "<root><br/><img src=\"pic.png\"/></root>";
        Document doc = treeBuilder.parse(xml, "http://example.com");

        Element root = doc.child(0);
        assertEquals("br", root.child(0).tagName());
        assertEquals("img", root.child(1).tagName());
    }

    // Tests normal comment insertion
    @Test
    public void testInsert_normalComment_createsCommentNode() {
        String xml = "<root><!-- This is a comment --></root>";
        Document doc = treeBuilder.parse(xml, "http://example.com");

        Node commentNode = doc.child(0).childNode(0);
        assertTrue(commentNode instanceof Comment);
        assertEquals(" This is a comment ", ((Comment) commentNode).getData());
    }

    // Tests bogus comment containing XML declaration with '?'
    @Test
    public void testInsert_xmlDeclarationQuestionMark_createsXmlDeclaration() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root/>";
        Document doc = treeBuilder.parse(xml, "http://example.com");

        Node declNode = doc.childNode(0);
        assertTrue(declNode instanceof XmlDeclaration);
        XmlDeclaration decl = (XmlDeclaration) declNode;
        assertEquals("xml", decl.name());
        assertEquals("1.0", decl.attr("version"));
        assertEquals("UTF-8", decl.attr("encoding"));
        assertTrue(decl.outerHtml().startsWith("<?"));
    }

    // Tests bogus comment containing XML declaration with '!'
    @Test
    public void testInsert_xmlDeclarationExclamationMark_createsProcessingInstruction() {
        String xml = "<!DOCTYPE html><root/>";
        Document doc = treeBuilder.parse(xml, "http://example.com");

        Node docTypeNode = doc.childNode(0);
        assertTrue(docTypeNode instanceof DocumentType);
        DocumentType docType = (DocumentType) docTypeNode;
        assertEquals("html", docType.attr("name"));
    }

    // Tests bogus comment with '!' syntax parsed as declaration
    @Test
    public void testInsert_customDeclarationExclamation_createsXmlDeclaration() {
        String xml = "<!CUSTOM val=\"123\"><root/>";
        Document doc = treeBuilder.parse(xml, "http://example.com");

        Node declNode = doc.childNode(0);
        assertTrue(declNode instanceof XmlDeclaration);
        XmlDeclaration decl = (XmlDeclaration) declNode;
        assertTrue(decl.outerHtml().startsWith("<!"));
        assertEquals("CUSTOM", decl.name());
        assertEquals("123", decl.attr("val"));
    }

    // Tests character data including CDATA node
    @Test
    public void testInsert_cdataSection_createsCDataNode() {
        String xml = "<root><![CDATA[some <raw> & data]]></root>";
        Document doc = treeBuilder.parse(xml, "http://example.com");

        Element root = doc.child(0);
        assertEquals(1, root.childNodeSize());
        assertTrue(root.childNode(0) instanceof CDataNode);
        assertEquals("some <raw> & data", ((CDataNode) root.childNode(0)).text());
    }

    // Tests plain text node
    @Test
    public void testInsert_textNode_createsTextNode() {
        String xml = "<root>Simple Text</root>";
        Document doc = treeBuilder.parse(xml, "http://example.com");

        Element root = doc.child(0);
        assertTrue(root.childNode(0) instanceof TextNode);
        assertEquals("Simple Text", ((TextNode) root.childNode(0)).getWholeText());
    }

    // Tests popStackToClose with matching end tag closing nested hierarchy
    @Test
    public void testPopStackToClose_nestedTags_popsCorrectly() {
        String xml = "<root><a><b><c>text</c></b></a></root>";
        Document doc = treeBuilder.parse(xml, "http://example.com");

        Element root = doc.child(0);
        assertEquals("a", root.child(0).tagName());
        assertEquals("b", root.child(0).child(0).tagName());
        assertEquals("c", root.child(0).child(0).child(0).tagName());
        assertEquals("text", root.child(0).child(0).child(0).text());
    }

    // Tests popStackToClose with unmatched end tag (skipped)
    @Test
    public void testPopStackToClose_unmatchedEndTag_skippedGracefully() {
        String xml = "<root></unmatched><child>content</child></root>";
        Document doc = treeBuilder.parse(xml, "http://example.com");

        Element root = doc.child(0);
        assertEquals(1, root.children().size());
        assertEquals("child", root.child(0).tagName());
        assertEquals("content", root.child(0).text());
    }

    // Tests popStackToClose when closing tag is out of order
    @Test
    public void testPopStackToClose_outOfOrderEndTag_popsStackUpToMatchedElement() {
        String xml = "<root><first><second></first></root>";
        Document doc = treeBuilder.parse(xml, "http://example.com");

        Element root = doc.child(0);
        assertEquals(1, root.children().size());
        assertEquals("first", root.child(0).tagName());
        assertEquals("second", root.child(0).child(0).tagName());
    }

    // Tests case-insensitive / normalized end tag handling (Defects4J 77 regression)
    @Test
    public void testPopStackToClose_caseInsensitiveSettings_closesCorrectly() {
        String xml = "<DIV><p>Hello</DIV>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser().settings(ParseSettings.htmlDefault));

        assertEquals(1, doc.childNodeSize());
        assertEquals("div", doc.child(0).tagName());
        assertEquals("p", doc.child(0).child(0).tagName());
    }

    // Tests parsing XML fragment
    @Test
    public void testParseFragment_validFragment_returnsNodeList() {
        String fragment = "<one>First</one><two>Second</two>";
        List<Node> nodes = treeBuilder.parseFragment(fragment, "http://example.com", ParseErrorList.noTracking(), ParseSettings.preserveCase);

        assertEquals(2, nodes.size());
        assertTrue(nodes.get(0) instanceof Element);
        assertEquals("one", ((Element) nodes.get(0)).tagName());
        assertEquals("First", ((Element) nodes.get(0)).text());
        assertTrue(nodes.get(1) instanceof Element);
        assertEquals("two", ((Element) nodes.get(1)).tagName());
        assertEquals("Second", ((Element) nodes.get(1)).text());
    }

    // Tests Doctype node insertion with public and system identifier
    @Test
    public void testInsert_doctypeWithIdentifiers_setsIdentifiersCorrectly() {
        String xml = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\"><html/>";
        Document doc = treeBuilder.parse(xml, "http://example.com");

        DocumentType docType = (DocumentType) doc.childNode(0);
        assertEquals("html", docType.attr("name"));
        assertEquals("-//W3C//DTD XHTML 1.0 Strict//EN", docType.attr("publicId"));
        assertEquals("http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd", docType.attr("systemId"));
    }

    // Tests empty XML input
    @Test
    public void testParse_emptyString_returnsEmptyDoc() {
        Document doc = treeBuilder.parse("", "http://example.com");
        assertNotNull(doc);
        assertEquals(0, doc.children().size());
    }
}