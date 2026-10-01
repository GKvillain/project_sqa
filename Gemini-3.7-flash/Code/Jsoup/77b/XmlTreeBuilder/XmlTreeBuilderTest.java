package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.junit.Before;
import org.junit.Test;

import java.io.StringReader;
import java.util.List;

import static org.junit.Assert.*;

public class XmlTreeBuilderTest {

    private XmlTreeBuilder xmlTreeBuilder;

    @Before
    public void setUp() {
        xmlTreeBuilder = new XmlTreeBuilder();
    }

    // Tests default parser settings
    @Test
    public void testDefaultSettings_default_preservesCase() {
        ParseSettings settings = xmlTreeBuilder.defaultSettings();
        assertTrue(settings.preserveTagCase());
        assertTrue(settings.preserveAttributeCase());
    }

    // Tests basic XML parsing with nested elements and text nodes
    @Test
    public void testParse_simpleXmlString_createsCorrectDocumentTree() {
        String xml = "<root><child id=\"1\">Hello</child></root>";
        Document doc = xmlTreeBuilder.parse(xml, "http://example.com/");

        assertNotNull(doc);
        assertEquals(Document.OutputSettings.Syntax.xml, doc.outputSettings().syntax());
        Element root = doc.child(0);
        assertEquals("root", root.tagName());
        assertEquals(1, root.children().size());

        Element child = root.child(0);
        assertEquals("child", child.tagName());
        assertEquals("1", child.attr("id"));
        assertEquals("Hello", child.text());
    }

    // Tests parsing with Reader input
    @Test
    public void testParse_readerInput_parsesSuccessfully() {
        String xml = "<data><item>value</item></data>";
        Document doc = xmlTreeBuilder.parse(new StringReader(xml), "");

        assertEquals(1, doc.children().size());
        Element data = doc.child(0);
        assertEquals("data", data.tagName());
        assertEquals("value", data.child(0).text());
    }

    // Tests self-closing tag handling
    @Test
    public void testParse_selfClosingTag_createsElementWithoutStacking() {
        String xml = "<root><empty attr=\"val\"/><sibling>text</sibling></root>";
        Document doc = xmlTreeBuilder.parse(xml, "");

        Element root = doc.child(0);
        assertEquals(2, root.children().size());
        assertEquals("empty", root.child(0).tagName());
        assertEquals("val", root.child(0).attr("attr"));
        assertEquals("sibling", root.child(1).tagName());
    }

    // Tests parsing CDATA section
    @Test
    public void testParse_cdataSection_createsCDataNode() {
        String xml = "<root><![CDATA[<unescaped & content>]]></root>";
        Document doc = xmlTreeBuilder.parse(xml, "");

        Element root = doc.child(0);
        assertEquals(1, root.childNodes().size());
        assertTrue(root.childNode(0) instanceof CDataNode);
        CDataNode cdata = (CDataNode) root.childNode(0);
        assertEquals("<unescaped & content>", cdata.text());
    }

    // Tests parsing standard comment
    @Test
    public void testParse_standardComment_createsCommentNode() {
        String xml = "<root><!-- this is a comment --></root>";
        Document doc = xmlTreeBuilder.parse(xml, "");

        Element root = doc.child(0);
        assertEquals(1, root.childNodes().size());
        assertTrue(root.childNode(0) instanceof Comment);
        Comment comment = (Comment) root.childNode(0);
        assertEquals(" this is a comment ", comment.getData());
    }

    // Tests XML declaration parsing (bogus comment with ?)
    @Test
    public void testParse_xmlDeclaration_createsXmlDeclarationNode() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root/>";
        Document doc = xmlTreeBuilder.parse(xml, "");

        assertTrue(doc.childNode(0) instanceof XmlDeclaration);
        XmlDeclaration decl = (XmlDeclaration) doc.childNode(0);
        assertEquals("xml", decl.name());
        assertEquals("1.0", decl.attr("version"));
        assertEquals("UTF-8", decl.attr("encoding"));
    }

    // Tests bogus declaration starting with exclamation
    @Test
    public void testParse_bogusDeclarationWithExclamation_createsXmlDeclarationNode() {
        String xml = "<!foo version=\"1.0\"><root/>";
        Document doc = xmlTreeBuilder.parse(xml, "");

        assertTrue(doc.childNode(0) instanceof XmlDeclaration);
        XmlDeclaration decl = (XmlDeclaration) doc.childNode(0);
        assertEquals("foo", decl.name());
        assertEquals("1.0", decl.attr("version"));
    }

    // Tests DOCTYPE node creation
    @Test
    public void testParse_doctype_createsDocumentTypeNode() {
        String xml = "<!DOCTYPE root PUBLIC \"-//W3C//DTD XML 1.0//EN\" \"http://example.com/dtd\"><root/>";
        Document doc = xmlTreeBuilder.parse(xml, "");

        assertTrue(doc.childNode(0) instanceof DocumentType);
        DocumentType doctype = (DocumentType) doc.childNode(0);
        assertEquals("root", doctype.attr("name"));
        assertEquals("-//W3C//DTD XML 1.0//EN", doctype.attr("publicId"));
        assertEquals("http://example.com/dtd", doctype.attr("systemId"));
    }

    // Tests unmatched closing tag skipping (not on stack)
    @Test
    public void testParse_unmatchedEndTag_ignoresGracefully() {
        String xml = "<root><child>value</child></unmatched></root>";
        Document doc = xmlTreeBuilder.parse(xml, "");

        Element root = doc.child(0);
        assertEquals(1, root.children().size());
        assertEquals("child", root.child(0).tagName());
    }

    // Tests case normalization end-tag matching (Defects4J Bug 77 regression)
    @Test
    public void testParse_normalizedCaseEndTag_popsStackCorrectly() {
        String xml = "<DIV><SPAN>content</SPAN></DIV>";
        Document doc = xmlTreeBuilder.parse(new StringReader(xml), "", ParseErrorList.noTracking(), ParseSettings.htmlDefault);

        Element div = doc.child(0);
        assertEquals("div", div.tagName());
        assertEquals(1, div.children().size());
        assertEquals("span", div.child(0).tagName());
        assertEquals("content", div.child(0).text());
        assertEquals(1, doc.children().size());
    }

    // Tests parseFragment with valid XML fragment
    @Test
    public void testParseFragment_validXmlString_returnsChildNodes() {
        String fragment = "<item id=\"1\">A</item><item id=\"2\">B</item>";
        List<Node> nodes = xmlTreeBuilder.parseFragment(fragment, "", ParseErrorList.noTracking(), ParseSettings.preserveCase);

        assertEquals(2, nodes.size());
        assertTrue(nodes.get(0) instanceof Element);
        assertTrue(nodes.get(1) instanceof Element);

        Element first = (Element) nodes.get(0);
        Element second = (Element) nodes.get(1);

        assertEquals("item", first.tagName());
        assertEquals("1", first.attr("id"));
        assertEquals("A", first.text());

        assertEquals("item", second.tagName());
        assertEquals("2", second.attr("id"));
        assertEquals("B", second.text());
    }

    // Tests preserving tag case sensitivity under default settings
    @Test
    public void testParse_mixedCaseTags_preservesCase() {
        String xml = "<MixedCaseTag><CamelCaseChild>test</CamelCaseChild></MixedCaseTag>";
        Document doc = xmlTreeBuilder.parse(xml, "");

        Element root = doc.child(0);
        assertEquals("MixedCaseTag", root.tagName());
        assertEquals("CamelCaseChild", root.child(0).tagName());
    }

    // Tests out-of-order closing tags popping intermediate stack elements
    @Test
    public void testParse_outOfOrderClosingTag_popsIntermediateElements() {
        String xml = "<root><a><b>text</a></root>";
        Document doc = xmlTreeBuilder.parse(xml, "");

        Element root = doc.child(0);
        assertEquals(1, root.children().size());
        Element a = root.child(0);
        assertEquals("a", a.tagName());
    }

    @Test
    public void testNewInstance_createsDistinctXmlTreeBuilder() {
        TreeBuilder newBuilder = xmlTreeBuilder.newInstance();
        assertNotNull(newBuilder);
        assertTrue(newBuilder instanceof XmlTreeBuilder);
        assertNotSame(xmlTreeBuilder, newBuilder);
    }

    @Test
    public void testParseFragment_withContextElement_returnsNodes() {
        Element context = new Element(Tag.valueOf("context"), "");
        String fragment = "<child>value</child>";
        List<Node> nodes = xmlTreeBuilder.parseFragment(fragment, context, "http://example.com/", ParseErrorList.noTracking(), ParseSettings.preserveCase);

        assertNotNull(nodes);
        assertEquals(1, nodes.size());
        assertTrue(nodes.get(0) instanceof Element);
        Element child = (Element) nodes.get(0);
        assertEquals("child", child.tagName());
        assertEquals("value", child.text());
    }

    @Test
    public void testParse_emptyBogusComment_fallsBackToComment() {
        String xml = "<?> <root/>";
        Document doc = xmlTreeBuilder.parse(xml, "");

        assertTrue(doc.childNode(0) instanceof Comment);
        Comment comment = (Comment) doc.childNode(0);
        assertEquals("?", comment.getData());
    }

    @Test
    public void testParse_doctypeWithoutIds_createsDocumentTypeNode() {
        String xml = "<!DOCTYPE html><html/>";
        Document doc = xmlTreeBuilder.parse(xml, "");

        assertTrue(doc.childNode(0) instanceof DocumentType);
        DocumentType doctype = (DocumentType) doc.childNode(0);
        assertEquals("html", doctype.attr("name"));
        assertEquals("", doctype.attr("publicId"));
        assertEquals("", doctype.attr("systemId"));
    }

    @Test
    public void testParse_textNodeOutsideAndInsideElement_appendsCorrectly() {
        String xml = "TextBefore<root>InsideText</root>TextAfter";
        Document doc = xmlTreeBuilder.parse(xml, "");

        assertEquals(3, doc.childNodeSize());
        assertTrue(doc.childNode(0) instanceof TextNode);
        assertEquals("TextBefore", ((TextNode) doc.childNode(0)).getWholeText());

        assertTrue(doc.childNode(1) instanceof Element);
        Element root = (Element) doc.childNode(1);
        assertEquals("InsideText", root.text());

        assertTrue(doc.childNode(2) instanceof TextNode);
        assertEquals("TextAfter", ((TextNode) doc.childNode(2)).getWholeText());
    }

    @Test
    public void testParse_closingTagOnEmptyStack_ignoresGracefully() {
        String xml = "</closed>";
        Document doc = xmlTreeBuilder.parse(xml, "");

        assertNotNull(doc);
        assertEquals(0, doc.children().size());
    }
}