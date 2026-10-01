package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.parser.*;
import java.util.List;

public class XmlTreeBuilderTest {

    // Helper to parse XML using the XmlTreeBuilder
    private Document parseXml(String xml) {
        return Jsoup.parse(xml, "", Parser.xmlParser());
    }

    // Tests normal parsing of simple XML
    @Test
    public void testParseSimpleXml_returnsDocumentWithRootElement() {
        Document doc = parseXml("<root></root>");
        Element root = doc.child(0);
        assertEquals("root", root.tagName());
        assertEquals(1, doc.childNodeSize());
    }

    // Tests self-closing unknown tag branch in insert(StartTag)
    @Test
    public void testParseSelfClosingTag_elementSelfClosed() {
        Document doc = parseXml("<br/>");
        Element br = doc.child(0);
        assertEquals("br", br.tagName());
        assertEquals(0, br.childrenSize());
        String outer = doc.outerHtml();
        assertTrue("Expected self-closing tag", outer.contains("<br/>") || outer.contains("<br />"));
    }

    // Tests nested elements (normal case for start tag, end tag, text)
    @Test
    public void testParseNestedElements_correctStructure() {
        Document doc = parseXml("<parent><child>text</child></parent>");
        Element parent = doc.child(0);
        assertEquals("parent", parent.tagName());
        assertEquals(1, parent.childNodeSize());
        Element child = parent.child(0);
        assertEquals("child", child.tagName());
        assertEquals("text", child.text());
    }

    // Tests normal comment insertion (false branch of bogus comment condition)
    @Test
    public void testParseComment_commentInserted() {
        Document doc = parseXml("<root><!-- comment --></root>");
        Element root = doc.child(0);
        Node comment = root.childNode(0);
        assertTrue(comment instanceof Comment);
        assertEquals(" comment ", ((Comment) comment).getData());
    }

    // Tests XML declaration (true branch of bogus comment condition)
    @Test
    public void testParseXmlDeclaration_createsXmlDeclaration() {
        Document doc = parseXml("<?xml version=\"1.0\"?>");
        List<Node> children = doc.childNodes();
        assertEquals(1, children.size());
        Node first = children.get(0);
        assertTrue(first instanceof XmlDeclaration);
        XmlDeclaration decl = (XmlDeclaration) first;
        assertEquals("xml", decl.tagName());
        assertEquals("1.0", decl.attr("version"));
    }

    // Tests CDATA section (character token with isCData true)
    @Test
    public void testParseCData_cdataNodeInserted() {
        Document doc = parseXml("<root><![CDATA[some data]]></root>");
        Element root = doc.child(0);
        List<Node> children = root.childNodesCopy();
        boolean foundCData = false;
        for (Node node : children) {
            if (node instanceof CDataNode) {
                foundCData = true;
                assertEquals("some data", ((CDataNode) node).getWholeText());
                break;
            }
        }
        assertTrue("CDataNode not found", foundCData);
    }

    // Tests Doctype insertion
    @Test
    public void testParseDoctype_doctypeNodeInserted() {
        Document doc = parseXml("<!DOCTYPE html>");
        List<Node> children = doc.childNodes();
        boolean foundDocType = false;
        for (Node node : children) {
            if (node instanceof DocumentType) {
                foundDocType = true;
                assertEquals("html", ((DocumentType) node).name());
                break;
            }
        }
        assertTrue("DocumentType not found", foundDocType);
    }

    // Tests parseFragment method
    @Test
    public void testParseFragment_returnsChildNodes() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        String fragment = "<item>value</item>";
        List<Node> nodes = builder.parseFragment(fragment, "http://base", ParseErrorList.noTracking(), ParseSettings.preserveCase);
        assertEquals(1, nodes.size());
        Node node = nodes.get(0);
        assertTrue(node instanceof Element);
        Element element = (Element) node;
        assertEquals("item", element.tagName());
        assertEquals("value", element.text());
    }

    // Tests non-matching end tag (popStackToClose not found branch)
    @Test
    public void testParseNonMatchingEndTag_skips() {
        Document doc = parseXml("<root><child></wrong></child></root>");
        Element root = doc.child(0);
        assertEquals(1, root.childNodeSize());
        assertEquals("child", root.child(0).tagName());
    }

    // Tests EOF token (empty input)
    @Test
    public void testParseEmptyInput_returnsEmptyDocument() {
        Document doc = parseXml("");
        assertEquals(0, doc.childNodeSize());
        assertEquals(0, doc.childNodes().size());
    }

    // Tests comment with data length <= 1 (false branch of length > 1 condition)
    @Test
    public void testParseShortComment_doesNotCreateDeclaration() {
        Document doc = parseXml("<root><!--x--></root>");
        Element root = doc.child(0);
        Node comment = root.childNode(0);
        assertTrue(comment instanceof Comment);
        assertEquals("x", ((Comment) comment).getData());
    }

    // Tests self-closing tag with attributes
    @Test
    public void testParseSelfClosingWithAttributes() {
        Document doc = parseXml("<img src='a.png'/>");
        Element img = doc.child(0);
        assertEquals("img", img.tagName());
        assertEquals("a.png", img.attr("src"));
        String outer = doc.outerHtml();
        assertTrue(outer.contains("/>"));
    }

    // Tests non-self-closing tag (stack add branch)
    @Test
    public void testParseNonSelfClosingTag_addsToStack() {
        Document doc = parseXml("<div><span></span></div>");
        Element div = doc.child(0);
        assertEquals("div", div.tagName());
        assertEquals(1, div.childNodeSize());
        assertEquals("span", div.child(0).tagName());
    }
}