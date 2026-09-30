package org.jsoup.helper;

import org.jsoup.Jsoup;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import static org.junit.Assert.*;

public class W3CDomTest {

    // Tests null input to fromJsoup
    @Test(expected = IllegalArgumentException.class)
    public void testFromJsoup_nullInput_throwsException() {
        W3CDom w3c = new W3CDom();
        w3c.fromJsoup(null);
    }

    // Tests simple HTML document conversion
    @Test
    public void testFromJsoup_simpleHtml_convertsCorrectly() {
        String html = "<html><head><title>Test Title</title></head><body><p id=\"p1\">Hello World</p></body></html>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html);

        W3CDom w3c = new W3CDom();
        Document w3cDoc = w3c.fromJsoup(jsoupDoc);

        assertNotNull(w3cDoc);
        assertEquals("html", w3cDoc.getDocumentElement().getTagName());
        NodeList paragraphs = w3cDoc.getElementsByTagName("p");
        assertEquals(1, paragraphs.getLength());
        org.w3c.dom.Element p = (org.w3c.dom.Element) paragraphs.item(0);
        assertEquals("p1", p.getAttribute("id"));
        assertEquals("Hello World", p.getTextContent());
    }

    // Tests Document URI propagation when location is present
    @Test
    public void testConvert_withLocation_setsDocumentUri() {
        String html = "<html><head></head><body><p>Text</p></body></html>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html, "http://example.com/page.html");

        W3CDom w3c = new W3CDom();
        Document w3cDoc = w3c.fromJsoup(jsoupDoc);

        assertEquals("http://example.com/page.html", w3cDoc.getDocumentURI());
    }

    // Tests empty document URI when location is blank
    @Test
    public void testConvert_blankLocation_documentUriNotSet() {
        String html = "<html><head></head><body><p>Text</p></body></html>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html);

        W3CDom w3c = new W3CDom();
        Document w3cDoc = w3c.fromJsoup(jsoupDoc);

        assertNull(w3cDoc.getDocumentURI());
    }

    // Tests conversion of comments
    @Test
    public void testFromJsoup_withComment_convertsCommentNode() {
        String html = "<html><head></head><body><!-- This is a comment --><p>Text</p></body></html>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html);

        W3CDom w3c = new W3CDom();
        Document w3cDoc = w3c.fromJsoup(jsoupDoc);

        Node body = w3cDoc.getElementsByTagName("body").item(0);
        Node commentNode = null;
        for (int i = 0; i < body.getChildNodes().getLength(); i++) {
            Node child = body.getChildNodes().item(i);
            if (child.getNodeType() == Node.COMMENT_NODE) {
                commentNode = child;
                break;
            }
        }
        assertNotNull(commentNode);
        assertEquals(" This is a comment ", commentNode.getNodeValue());
    }

    // Tests conversion of DataNode (such as script contents)
    @Test
    public void testFromJsoup_withDataNode_convertsDataNode() {
        String html = "<html><head><script>var x = 10;</script></head><body></body></html>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html);

        W3CDom w3c = new W3CDom();
        Document w3cDoc = w3c.fromJsoup(jsoupDoc);

        Node script = w3cDoc.getElementsByTagName("script").item(0);
        assertNotNull(script);
        assertEquals("var x = 10;", script.getTextContent());
    }

    // Tests default namespace (xmlns="...")
    @Test
    public void testFromJsoup_defaultNamespace_appliedCorrectly() {
        String html = "<html xmlns=\"http://www.w3.org/1999/xhtml\"><head></head><body><p>Test</p></body></html>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html);

        W3CDom w3c = new W3CDom();
        Document w3cDoc = w3c.fromJsoup(jsoupDoc);

        org.w3c.dom.Element htmlEl = w3cDoc.getDocumentElement();
        assertEquals("http://www.w3.org/1999/xhtml", htmlEl.getNamespaceURI());
    }

    // Tests prefixed namespace (xmlns:prefix="...")
    @Test
    public void testFromJsoup_prefixedNamespace_appliedCorrectly() {
        String html = "<html xmlns:custom=\"http://example.com/custom\"><head></head><body><custom:tag>Value</custom:tag></body></html>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html);

        W3CDom w3c = new W3CDom();
        Document w3cDoc = w3c.fromJsoup(jsoupDoc);

        Node customTag = w3cDoc.getElementsByTagName("custom:tag").item(0);
        assertNotNull(customTag);
        assertEquals("http://example.com/custom", customTag.getNamespaceURI());
    }

    // Tests nested element structure and tail traversal
    @Test
    public void testFromJsoup_nestedElements_hierarchyPreserved() {
        String html = "<div><div><span><b>Deep</b></span></div></div>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html);

        W3CDom w3c = new W3CDom();
        Document w3cDoc = w3c.fromJsoup(jsoupDoc);

        NodeList bNodes = w3cDoc.getElementsByTagName("b");
        assertEquals(1, bNodes.getLength());
        Node b = bNodes.item(0);
        assertEquals("Deep", b.getTextContent());
        assertEquals("span", b.getParentNode().getNodeName());
    }

    // Tests attributes with invalid XML characters (Defects4J Bug 54)
    @Test
    public void testFromJsoup_invalidAttributeCharacters_handlesWithoutException() {
        String html = "<html><head></head><body><p <invalid=\"value\" valid=\"ok\">Text</p></body></html>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html);

        W3CDom w3c = new W3CDom();
        Document w3cDoc = w3c.fromJsoup(jsoupDoc);

        NodeList pList = w3cDoc.getElementsByTagName("p");
        assertEquals(1, pList.getLength());
        org.w3c.dom.Element p = (org.w3c.dom.Element) pList.item(0);
        assertEquals("ok", p.getAttribute("valid"));
    }

    // Tests asString serialization to XML string
    @Test
    public void testAsString_validDocument_returnsSerializedString() {
        String html = "<html><head><title>Title</title></head><body><p>Hello</p></body></html>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html);

        W3CDom w3c = new W3CDom();
        Document w3cDoc = w3c.fromJsoup(jsoupDoc);
        String xml = w3c.asString(w3cDoc);

        assertNotNull(xml);
        assertTrue(xml.contains("<title>Title</title>"));
        assertTrue(xml.contains("<p>Hello</p>"));
    }
}