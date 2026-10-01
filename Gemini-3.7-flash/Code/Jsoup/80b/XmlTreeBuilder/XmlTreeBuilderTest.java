package org.jsoup.parser;

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

    // Tests default settings preserve case
    @Test
    public void testDefaultSettings_preservesCase() {
        ParseSettings settings = treeBuilder.defaultSettings();
        assertEquals("TestTag", settings.normalizeTag("TestTag"));
        assertEquals("TestAttr", settings.normalizeAttribute("TestAttr"));
    }

    // Tests parsing simple XML with String input
    @Test
    public void testParse_simpleXmlString_returnsParsedDocument() {
        String xml = "<root><child id=\"1\">Text</child></root>";
        Document doc = treeBuilder.parse(xml, "http://example.com/");
        
        assertNotNull(doc);
        assertEquals(Document.OutputSettings.Syntax.xml, doc.outputSettings().syntax());
        assertEquals(1, doc.children().size());
        Element root = doc.child(0);
        assertEquals("root", root.tagName());
        assertEquals("Text", root.child(0).text());
        assertEquals("1", root.child(0).attr("id"));
    }

    // Tests parsing simple XML with Reader input
    @Test
    public void testParse_simpleXmlReader_returnsParsedDocument() {
        String xml = "<doc><val>Data</val></doc>";
        Document doc = treeBuilder.parse(new StringReader(xml), "http://example.com/");
        
        assertNotNull(doc);
        assertEquals("doc", doc.child(0).tagName());
        assertEquals("Data", doc.child(0).child(0).text());
    }

    // Tests self-closing tag handling
    @Test
    public void testParse_selfClosingTags_parsedCorrectly() {
        String xml = "<root><selfClosed/><customSelf /></root>";
        Document doc = treeBuilder.parse(xml, "");
        
        Element root = doc.child(0);
        assertEquals(2, root.children().size());
        assertEquals("selfClosed", root.child(0).tagName());
        assertEquals("customSelf", root.child(1).tagName());
    }

    // Tests XML declaration parsing
    @Test
    public void testParse_xmlDeclaration_createsXmlDeclarationNode() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root/>";
        Document doc = treeBuilder.parse(xml, "");
        
        List<Node> nodes = doc.childNodes();
        assertTrue(nodes.size() >= 2);
        assertTrue(nodes.get(0) instanceof XmlDeclaration);
        XmlDeclaration decl = (XmlDeclaration) nodes.get(0);
        assertEquals("xml", decl.name());
        assertEquals("1.0", decl.attr("version"));
        assertEquals("UTF-8", decl.attr("encoding"));
    }

    // Tests bogus comment edge cases and potential IndexOutOfBounds bug
    @Test
    public void testParse_bogusCommentDeclaration_handledGracefully() {
        String xml = "<??><html></html>";
        Document doc = treeBuilder.parse(xml, "");
        assertNotNull(doc);
        assertEquals("html", doc.selectFirst("html").tagName());
    }

    // Tests standard XML comment
    @Test
    public void testParse_standardComment_createsCommentNode() {
        String xml = "<root><!-- this is a comment --><child/></root>";
        Document doc = treeBuilder.parse(xml, "");
        
        Element root = doc.child(0);
        assertEquals(2, root.childNodeSize());
        assertTrue(root.childNode(0) instanceof Comment);
        Comment comment = (Comment) root.childNode(0);
        assertEquals(" this is a comment ", comment.getData());
    }

    // Tests CDATA section parsing
    @Test
    public void testParse_cdataSection_createsCDataNode() {
        String xml = "<root><![CDATA[Some <raw> data & entities]]></root>";
        Document doc = treeBuilder.parse(xml, "");
        
        Element root = doc.child(0);
        assertEquals(1, root.childNodeSize());
        assertTrue(root.childNode(0) instanceof CDataNode);
        CDataNode cdata = (CDataNode) root.childNode(0);
        assertEquals("Some <raw> data & entities", cdata.text());
    }

    // Tests Doctype parsing and preservation
    @Test
    public void testParse_doctype_createsDocumentTypeNode() {
        String xml = "<!DOCTYPE html SYSTEM \"about:legacy-compat\"><root/>";
        Document doc = treeBuilder.parse(xml, "");
        
        List<Node> nodes = doc.childNodes();
        assertTrue(nodes.get(0) instanceof DocumentType);
        DocumentType doctype = (DocumentType) nodes.get(0);
        assertEquals("html", doctype.name());
        assertEquals("about:legacy-compat", doctype.attr("systemId"));
    }

    // Tests unmatched closing tags handling in stack
    @Test
    public void testParse_unmatchedClosingTag_ignoresOrClosesProperly() {
        String xml = "<root><child>text</unmatched></child></root>";
        Document doc = treeBuilder.parse(xml, "");
        
        Element root = doc.child(0);
        assertEquals(1, root.children().size());
        assertEquals("child", root.child(0).tagName());
        assertEquals("text", root.child(0).text());
    }

    // Tests case preservation in tags and attributes
    @Test
    public void testParse_preservesTagAndAttributeCase() {
        String xml = "<camelCase CamelAttr=\"valUE\"><InnerTag/></camelCase>";
        Document doc = treeBuilder.parse(xml, "");
        
        Element root = doc.child(0);
        assertEquals("camelCase", root.tagName());
        assertTrue(root.hasAttr("CamelAttr"));
        assertEquals("valUE", root.attr("CamelAttr"));
        assertEquals("InnerTag", root.child(0).tagName());
    }

    // Tests parsing XML fragment
    @Test
    public void testParseFragment_validFragment_returnsNodeList() {
        String fragment = "<one/><two>text</two>";
        List<Node> nodes = treeBuilder.parseFragment(fragment, "", Parser.xmlParser());
        
        assertEquals(2, nodes.size());
        assertTrue(nodes.get(0) instanceof Element);
        assertEquals("one", ((Element) nodes.get(0)).tagName());
        assertTrue(nodes.get(1) instanceof Element);
        assertEquals("two", ((Element) nodes.get(1)).tagName());
    }

    // Tests nested element hierarchy
    @Test
    public void testParse_nestedElements_buildsCorrectHierarchy() {
        String xml = "<a><b><c><d>deep</d></c></b></a>";
        Document doc = treeBuilder.parse(xml, "");
        
        Element el = doc.selectFirst("d");
        assertNotNull(el);
        assertEquals("deep", el.text());
        assertEquals("c", el.parent().nodeName());
        assertEquals("b", el.parent().parent().nodeName());
        assertEquals("a", el.parent().parent().parent().nodeName());
    }

    // Tests newInstance method
    @Test
    public void testNewInstance_createsDistinctXmlTreeBuilder() {
        XmlTreeBuilder newInstance = treeBuilder.newInstance();
        assertNotNull(newInstance);
        assertNotSame(treeBuilder, newInstance);
    }

    // Tests parseFragment with context element
    @Test
    public void testParseFragment_withContextElement_returnsNodes() {
        Element context = new Element("context");
        String fragment = "<child>content</child>";
        List<Node> nodes = treeBuilder.parseFragment(fragment, context, "http://example.com/", Parser.xmlParser());

        assertNotNull(nodes);
        assertEquals(1, nodes.size());
        assertTrue(nodes.get(0) instanceof Element);
        assertEquals("child", ((Element) nodes.get(0)).tagName());
        assertEquals("content", ((Element) nodes.get(0)).text());
    }

    // Tests closing tag popping multiple elements from stack
    @Test
    public void testPopStackToClose_ancestorClosed_popsDescendants() {
        String xml = "<a><b><c>text</a>";
        Document doc = treeBuilder.parse(xml, "");

        assertNotNull(doc);
        Element a = doc.selectFirst("a");
        assertNotNull(a);
        assertNotNull(a.selectFirst("b"));
        assertNotNull(a.selectFirst("b > c"));
    }

    // Tests bogus comment with exclamation mark converting to declaration
    @Test
    public void testParse_bogusCommentWithExclamation_createsXmlDeclaration() {
        String xml = "<!CUSTOM-DECL param=\"value\"><root/></!CUSTOM-DECL>";
        Document doc = treeBuilder.parse(xml, "");

        assertNotNull(doc);
        List<Node> nodes = doc.childNodes();
        assertTrue(nodes.size() >= 2);
        assertTrue(nodes.get(0) instanceof XmlDeclaration);
        XmlDeclaration decl = (XmlDeclaration) nodes.get(0);
        assertEquals("CUSTOM-DECL", decl.name());
        assertEquals("value", decl.attr("param"));
    }

    // Tests completely non-matching end tag on root level
    @Test
    public void testParse_nonMatchingEndTagAtRootLevel_ignored() {
        String xml = "</ignored><root>content</root></extra>";
        Document doc = treeBuilder.parse(xml, "");

        assertNotNull(doc);
        assertEquals(1, doc.children().size());
        assertEquals("root", doc.child(0).tagName());
        assertEquals("content", doc.child(0).text());
    }

    // Tests text nodes parsing
    @Test
    public void testParse_textNodeInsideElement() {
        String xml = "<root>Hello <b>World</b>!</root>";
        Document doc = treeBuilder.parse(xml, "");

        Element root = doc.child(0);
        assertEquals(3, root.childNodeSize());
        assertTrue(root.childNode(0) instanceof TextNode);
        assertEquals("Hello ", ((TextNode) root.childNode(0)).getWholeText());
        assertTrue(root.childNode(1) instanceof Element);
        assertTrue(root.childNode(2) instanceof TextNode);
        assertEquals("!", ((TextNode) root.childNode(2)).getWholeText());
    }
}