package org.jsoup.parser;

import org.junit.Before;
import org.junit.Test;
import org.jsoup.nodes.*;

import java.util.List;

import static org.junit.Assert.*;

public class XmlTreeBuilderTest {

    private XmlTreeBuilder builder;

    @Before
    public void setUp() {
        builder = new XmlTreeBuilder();
    }

    // Tests empty input returns a document with no children and XML syntax
    @Test
    public void testParse_emptyInput_returnsEmptyDocument() {
        Document doc = builder.parse("", "http://example.com");
        assertEquals(0, doc.children().size());
        assertEquals(Document.OutputSettings.Syntax.xml, doc.outputSettings().syntax());
    }

    // Tests single start/end tag pair creates a root element
    @Test
    public void testParse_simpleTag_createsElement() {
        Document doc = builder.parse("<root></root>", "");
        assertEquals(1, doc.children().size());
        Element root = doc.child(0);
        assertEquals("root", root.tagName());
        assertEquals(0, root.children().size());
    }

    // Tests self-closing unknown tag sets selfClosing flag
    @Test
    public void testParse_selfClosingUnknownTag_setsSelfClosing() {
        Document doc = builder.parse("<foo/>", "");
        Element foo = doc.child(0);
        assertEquals("foo", foo.tagName());
        assertTrue(foo.tag().isSelfClosing());
    }

    // Tests self-closing known HTML tag does NOT set selfClosing flag
    @Test
    public void testParse_selfClosingKnownTag_doesNotSetSelfClosing() {
        Document doc = builder.parse("<div/>", "");
        Element div = doc.child(0);
        assertEquals("div", div.tagName());
        assertFalse(div.tag().isSelfClosing());
    }

    // Tests nested tags produce proper hierarchy
    @Test
    public void testParse_nestedTags_createsHierarchy() {
        Document doc = builder.parse("<a><b></b></a>", "");
        Element a = doc.child(0);
        assertEquals("a", a.tagName());
        assertEquals(1, a.children().size());
        assertEquals("b", a.child(0).tagName());
    }

    // Tests mismatched close tag is skipped (not found)
    @Test
    public void testParse_mismatchedCloseTag_skips() {
        Document doc = builder.parse("<a><b></c></a>", "");
        Element a = doc.child(0);
        assertEquals("a", a.tagName());
        assertEquals(1, a.children().size());
        assertEquals("b", a.child(0).tagName());
    }

    // Tests popStackToClose removes multiple elements from stack
    @Test
    public void testParse_deepClose_removesMultipleElements() {
        Document doc = builder.parse("<a><b><c></b></a>", "");
        Element a = doc.child(0);
        assertEquals("a", a.tagName());
        assertEquals(1, a.children().size());
        Element b = a.child(0);
        assertEquals("b", b.tagName());
        assertEquals(1, b.children().size());
        Element c = b.child(0);
        assertEquals("c", c.tagName());
        assertEquals(0, c.children().size());
    }

    // Tests character data creates TextNode
    @Test
    public void testParse_characterData_createsTextNode() {
        Document doc = builder.parse("<root>text</root>", "");
        Element root = doc.child(0);
        assertEquals(1, root.childNodes().size());
        assertTrue(root.childNode(0) instanceof TextNode);
        assertEquals("text", ((TextNode) root.childNode(0)).getWholeText());
    }

    // Tests CData section creates CDataNode
    @Test
    public void testParse_cdata_createsCDataNode() {
        Document doc = builder.parse("<root><![CDATA[<cdata>]]></root>", "");
        Element root = doc.child(0);
        assertEquals(1, root.childNodes().size());
        assertTrue(root.childNode(0) instanceof CDataNode);
        assertEquals("<cdata>", ((CDataNode) root.childNode(0)).getWholeText());
    }

    // Tests normal comment creates Comment node
    @Test
    public void testParse_comment_createsCommentNode() {
        Document doc = builder.parse("<!--comment-->", "");
        assertEquals(1, doc.childNodes().size());
        assertTrue(doc.childNode(0) instanceof Comment);
        assertEquals("comment", ((Comment) doc.childNode(0)).getData());
    }

    // Tests bogus comment that is an XML declaration creates XmlDeclaration
    @Test
    public void testParse_bogusComment_xmlDeclaration_createsXmlDeclaration() {
        Document doc = builder.parse("<?xml version='1.0'?>", "");
        assertEquals(1, doc.childNodes().size());
        assertTrue(doc.childNode(0) instanceof XmlDeclaration);
        XmlDeclaration decl = (XmlDeclaration) doc.childNode(0);
        assertEquals("1.0", decl.attr("version"));
    }

    // Tests bogus comment that is not a valid XML declaration creates Comment
    @Test
    public void testParse_bogusComment_notXmlDeclaration_createsComment() {
        Document doc = builder.parse("<?random stuff?>", "");
        assertEquals(1, doc.childNodes().size());
        assertTrue(doc.childNode(0) instanceof Comment);
    }

    // Tests DOCTYPE creates DocumentType node
    @Test
    public void testParse_doctype_createsDocumentType() {
        Document doc = builder.parse("<!DOCTYPE html>", "");
        assertEquals(1, doc.childNodes().size());
        assertTrue(doc.childNode(0) instanceof DocumentType);
        DocumentType dt = (DocumentType) doc.childNode(0);
        assertEquals("<!DOCTYPE html>", dt.outerHtml());
    }

    // Tests null input throws NullPointerException
    @Test(expected = NullPointerException.class)
    public void testParse_nullInput_throwsNullPointerException() {
        builder.parse((String) null, "");
    }

    // Tests defaultSettings returns preserveCase
    @Test
    public void testDefaultSettings_returnsPreserveCase() {
        assertEquals(ParseSettings.preserveCase, builder.defaultSettings());
    }

    // Tests parseFragment returns a list of nodes
    @Test
    public void testParseFragment_returnsChildNodes() {
        Parser parser = new Parser(builder);
        List<Node> nodes = builder.parseFragment("<a>text</a>", "", parser);
        assertEquals(1, nodes.size());
        assertTrue(nodes.get(0) instanceof Element);
        Element a = (Element) nodes.get(0);
        assertEquals("a", a.tagName());
        assertEquals(1, a.childNodes().size());
        assertEquals("text", ((TextNode) a.childNode(0)).getWholeText());
    }

    // Tests base URI is propagated to the document
    @Test
    public void testParse_withBaseUri_setsBaseUri() {
        Document doc = builder.parse("<root/>", "http://base/");
        assertEquals("http://base/", doc.baseUri());
    }

    // Tests self-closing tag with attributes retains attributes
    @Test
    public void testParse_selfClosingWithAttributes_createsElementWithAttributes() {
        Document doc = builder.parse("<img src='image.png' alt='pic'/>", "");
        Element img = doc.child(0);
        assertEquals("img", img.tagName());
        assertEquals("image.png", img.attr("src"));
        assertEquals("pic", img.attr("alt"));
    }

    // Tests multiple root elements are allowed in XML
    @Test
    public void testParse_multipleRootElements_createsMultipleChildren() {
        Document doc = builder.parse("<a/><b/>", "");
        assertEquals(2, doc.children().size());
        assertEquals("a", doc.child(0).tagName());
        assertEquals("b", doc.child(1).tagName());
    }
}