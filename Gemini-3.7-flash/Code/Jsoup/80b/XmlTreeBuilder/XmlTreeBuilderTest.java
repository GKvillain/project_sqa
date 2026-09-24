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

    // Tests standard XML parsing with elements and text content
    @Test
    public void testParse_standardXml_createsCorrectDocument() {
        String xml = "<root><child id=\"1\">Hello</child></root>";
        Document doc = treeBuilder.parse(xml, "http://example.com");

        assertNotNull(doc);
        assertEquals(1, doc.children().size());
        Element root = doc.child(0);
        assertEquals("root", root.tagName());
        assertEquals("child", root.child(0).tagName());
        assertEquals("1", root.child(0).attr("id"));
        assertEquals("Hello", root.child(0).text());
        assertEquals(Document.OutputSettings.Syntax.xml, doc.outputSettings().syntax());
    }

    // Tests parsing via Reader input
    @Test
    public void testParse_readerInput_createsCorrectDocument() {
        String xml = "<item>Value</item>";
        Document doc = treeBuilder.parse(new StringReader(xml), "http://example.com");

        assertNotNull(doc);
        assertEquals("item", doc.child(0).tagName());
        assertEquals("Value", doc.child(0).text());
    }

    // Tests defaultSettings returns preserveCase
    @Test
    public void testDefaultSettings_normalCall_preservesCase() {
        ParseSettings settings = treeBuilder.defaultSettings();
        assertNotNull(settings);
        assertEquals(ParseSettings.preserveCase, settings);
    }

    // Tests XML declaration (bogus comment with ?)
    @Test
    public void testParse_xmlDeclaration_createsXmlDeclarationNode() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root/>";
        Document doc = treeBuilder.parse(xml, "http://example.com");

        Node firstNode = doc.childNode(0);
        assertTrue(firstNode instanceof XmlDeclaration);
        XmlDeclaration decl = (XmlDeclaration) firstNode;
        assertEquals("xml", decl.name());
        assertEquals("1.0", decl.attr("version"));
        assertEquals("UTF-8", decl.attr("encoding"));
    }

    // Tests processing of bogus comment with minimal characters (defect regression test for bug 80b)
    @Test
    public void testParse_bogusCommentEmpty_handlesWithoutException() {
        String xml = "<?>text</?>";
        Document doc = treeBuilder.parse(xml, "http://example.com");
        assertNotNull(doc);

        String xmlExclamation = "<!>text</!>";
        Document doc2 = treeBuilder.parse(xmlExclamation, "http://example.com");
        assertNotNull(doc2);
    }

    // Tests processing of standard comment
    @Test
    public void testParse_standardComment_createsCommentNode() {
        String xml = "<root><!-- This is a comment --></root>";
        Document doc = treeBuilder.parse(xml, "http://example.com");

        Element root = doc.child(0);
        assertEquals(1, root.childNodeSize());
        assertTrue(root.childNode(0) instanceof Comment);
        Comment comment = (Comment) root.childNode(0);
        assertEquals(" This is a comment ", comment.getData());
    }

    // Tests processing of CDATA section
    @Test
    public void testParse_cdataSection_createsCDataNode() {
        String xml = "<root><![CDATA[<unescaped & content>]]></root>";
        Document doc = treeBuilder.parse(xml, "http://example.com");

        Element root = doc.child(0);
        assertEquals(1, root.childNodeSize());
        assertTrue(root.childNode(0) instanceof CDataNode);
        CDataNode cdata = (CDataNode) root.childNode(0);
        assertEquals("<unescaped & content>", cdata.text());
    }

    // Tests Doctype node insertion
    @Test
    public void testParse_doctype_createsDocumentTypeNode() {
        String xml = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\"><html/>";
        Document doc = treeBuilder.parse(xml, "http://example.com");

        assertTrue(doc.childNode(0) instanceof DocumentType);
        DocumentType doctype = (DocumentType) doc.childNode(0);
        assertEquals("html", doctype.attr("name"));
        assertEquals("-//W3C//DTD XHTML 1.0 Strict//EN", doctype.attr("publicId"));
        assertEquals("http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd", doctype.attr("systemId"));
    }

    // Tests self-closing tag handling
    @Test
    public void testParse_selfClosingTag_doesNotAddToStack() {
        String xml = "<root><selfClosing attr=\"val\"/><sibling>content</sibling></root>";
        Document doc = treeBuilder.parse(xml, "http://example.com");

        Element root = doc.child(0);
        assertEquals(2, root.children().size());
        assertEquals("selfClosing", root.child(0).tagName());
        assertEquals("val", root.child(0).attr("attr"));
        assertEquals("sibling", root.child(1).tagName());
    }

    // Tests tag case preservation
    @Test
    public void testParse_mixedCaseTags_preservesCase() {
        String xml = "<CamelCaseTag attrName=\"AttrValue\">Text</CamelCaseTag>";
        Document doc = treeBuilder.parse(xml, "http://example.com");

        Element element = doc.child(0);
        assertEquals("CamelCaseTag", element.tagName());
        assertEquals("AttrValue", element.attr("attrName"));
    }

    // Tests parseFragment with valid XML fragment
    @Test
    public void testParseFragment_validFragment_returnsNodeList() {
        String fragment = "<one>First</one><two>Second</two>";
        List<Node> nodes = treeBuilder.parseFragment(fragment, "http://example.com", ParseErrorList.noTracking(), ParseSettings.preserveCase);

        assertNotNull(nodes);
        assertEquals(2, nodes.size());
        assertTrue(nodes.get(0) instanceof Element);
        assertEquals("one", ((Element) nodes.get(0)).tagName());
        assertTrue(nodes.get(1) instanceof Element);
        assertEquals("two", ((Element) nodes.get(1)).tagName());
    }

    // Tests popStackToClose with unmatched / out-of-order closing tags
    @Test
    public void testParse_unmatchedClosingTag_skipsGracefully() {
        String xml = "<root><child>text</unmatched></root>";
        Document doc = treeBuilder.parse(xml, "http://example.com");

        Element root = doc.child(0);
        assertEquals("root", root.tagName());
        assertEquals(1, root.children().size());
        assertEquals("child", root.child(0).tagName());
    }

    // Tests popStackToClose when closing parent element directly
    @Test
    public void testParse_nestedUnclosedTags_closesUpToMatchedTag() {
        String xml = "<root><a><b><c>text</root>";
        Document doc = treeBuilder.parse(xml, "http://example.com");

        assertNotNull(doc);
        assertEquals("root", doc.child(0).tagName());
    }
}