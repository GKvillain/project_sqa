package org.jsoup.helper;

import org.junit.Test;
import static org.junit.Assert.*;

import org.jsoup.helper.W3CDom;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.nodes.Comment;

import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class W3CDomTest {

    private org.w3c.dom.Document convert(org.jsoup.nodes.Document in) {
        return new W3CDom().fromJsoup(in);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromJsoup_nullDocument_throwsIllegalArgumentException() {
        new W3CDom().fromJsoup(null);
    }

    @Test
    public void testFromJsoup_simpleDocument_createsW3CDoc() {
        Document jsoupDoc = new Document("");
        Element html = new Element("html");
        jsoupDoc.appendChild(html);
        org.w3c.dom.Document w3cDoc = convert(jsoupDoc);
        org.w3c.dom.Element rootEl = (org.w3c.dom.Element) w3cDoc.getFirstChild();
        assertEquals("html", rootEl.getTagName());
    }

    @Test
    public void testFromJsoup_elementWithAttributes_attributesCopied() {
        Document jsoupDoc = new Document("");
        Element div = new Element("div");
        div.attr("id", "myid");
        div.attr("class", "myclass");
        jsoupDoc.appendChild(div);
        org.w3c.dom.Document w3cDoc = convert(jsoupDoc);
        org.w3c.dom.Element rootEl = (org.w3c.dom.Element) w3cDoc.getFirstChild();
        assertEquals("myid", rootEl.getAttribute("id"));
        assertEquals("myclass", rootEl.getAttribute("class"));
    }

    @Test
    public void testFromJsoup_elementWithDefaultXmlns_namespaceApplied() {
        Document jsoupDoc = new Document("");
        Element el = new Element("div");
        el.attr("xmlns", "http://example.com");
        jsoupDoc.appendChild(el);
        org.w3c.dom.Document w3cDoc = convert(jsoupDoc);
        org.w3c.dom.Element rootEl = (org.w3c.dom.Element) w3cDoc.getFirstChild();
        assertEquals("http://example.com", rootEl.getNamespaceURI());
        assertNull(rootEl.getPrefix());
    }

    @Test
    public void testFromJsoup_elementWithPrefixedNamespace_namespaceAndPrefixSet() {
        Document jsoupDoc = new Document("");
        Element el = new Element("foo:bar");
        el.attr("xmlns:foo", "http://foo.com");
        jsoupDoc.appendChild(el);
        org.w3c.dom.Document w3cDoc = convert(jsoupDoc);
        org.w3c.dom.Element rootEl = (org.w3c.dom.Element) w3cDoc.getFirstChild();
        assertEquals("http://foo.com", rootEl.getNamespaceURI());
        assertEquals("foo", rootEl.getPrefix());
        assertEquals("bar", rootEl.getLocalName());
        assertEquals("foo:bar", rootEl.getTagName());
    }

    @Test
    public void testFromJsoup_nestedElements_structurePreserved() {
        Document jsoupDoc = new Document("");
        Element parent = new Element("parent");
        jsoupDoc.appendChild(parent);
        Element child = new Element("child");
        parent.appendChild(child);
        org.w3c.dom.Document w3cDoc = convert(jsoupDoc);
        org.w3c.dom.Element w3cParent = (org.w3c.dom.Element) w3cDoc.getFirstChild();
        assertEquals("parent", w3cParent.getTagName());
        org.w3c.dom.Node w3cChild = w3cParent.getFirstChild();
        assertEquals("child", w3cChild.getNodeName());
    }

    @Test
    public void testFromJsoup_textNode_textContentPreserved() {
        Document jsoupDoc = new Document("");
        Element div = new Element("div");
        div.appendChild(new TextNode("Hello World"));
        jsoupDoc.appendChild(div);
        org.w3c.dom.Document w3cDoc = convert(jsoupDoc);
        org.w3c.dom.Element rootEl = (org.w3c.dom.Element) w3cDoc.getFirstChild();
        org.w3c.dom.Node textNode = rootEl.getFirstChild();
        assertEquals(Node.TEXT_NODE, textNode.getNodeType());
        assertEquals("Hello World", textNode.getTextContent());
    }

    @Test
    public void testFromJsoup_commentNode_commentPreserved() {
        Document jsoupDoc = new Document("");
        Element div = new Element("div");
        div.appendChild(new Comment("my comment"));
        jsoupDoc.appendChild(div);
        org.w3c.dom.Document w3cDoc = convert(jsoupDoc);
        org.w3c.dom.Element rootEl = (org.w3c.dom.Element) w3cDoc.getFirstChild();
        org.w3c.dom.Node commentNode = rootEl.getFirstChild();
        assertEquals(Node.COMMENT_NODE, commentNode.getNodeType());
        assertEquals("my comment", commentNode.getTextContent());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testFromJsoup_emptyDocumentNoChild_throwsException() {
        Document jsoupDoc = new Document("");
        new W3CDom().fromJsoup(jsoupDoc);
    }

    @Test
    public void testFromJsoup_documentWithLocation_uriSet() {
        Document jsoupDoc = new Document("");
        jsoupDoc.setBaseUri("http://example.com");
        Element html = new Element("html");
        jsoupDoc.appendChild(html);
        org.w3c.dom.Document w3cDoc = convert(jsoupDoc);
        assertEquals("http://example.com", w3cDoc.getDocumentURI());
    }

    @Test
    public void testFromJsoup_documentWithEmptyLocation_uriNotSet() {
        Document jsoupDoc = new Document("");
        jsoupDoc.setBaseUri("");
        Element html = new Element("html");
        jsoupDoc.appendChild(html);
        org.w3c.dom.Document w3cDoc = convert(jsoupDoc);
        assertNull(w3cDoc.getDocumentURI());
    }

    @Test
    public void testConvert_givenDocument_convertsIntoDocument() throws Exception {
        W3CDom w3c = new W3CDom();
        Document jsoupDoc = new Document("");
        Element el = new Element("test");
        jsoupDoc.appendChild(el);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        org.w3c.dom.Document w3cOut = db.newDocument();
        w3c.convert(jsoupDoc, w3cOut);
        org.w3c.dom.Element rootEl = (org.w3c.dom.Element) w3cOut.getFirstChild();
        assertEquals("test", rootEl.getTagName());
    }

    @Test
    public void testAsString_afterConversion_returnsString() {
        Document jsoupDoc = new Document("");
        Element root = new Element("root");
        Element child = new Element("child");
        child.appendChild(new TextNode("text"));
        root.appendChild(child);
        jsoupDoc.appendChild(root);
        org.w3c.dom.Document w3cDoc = convert(jsoupDoc);
        String xml = new W3CDom().asString(w3cDoc);
        assertNotNull(xml);
        assertTrue(xml.contains("root"));
        assertTrue(xml.contains("child"));
        assertTrue(xml.contains("text"));
    }

    @Test
    public void testFromJsoup_attributeNameWithInvalidChars_keySanitized() {
        Document jsoupDoc = new Document("");
        Element el = new Element("div");
        el.attr("bad@key", "value");
        jsoupDoc.appendChild(el);
        org.w3c.dom.Document w3cDoc = convert(jsoupDoc);
        org.w3c.dom.Element rootEl = (org.w3c.dom.Element) w3cDoc.getFirstChild();
        assertEquals("value", rootEl.getAttribute("badkey"));
        assertEquals("", rootEl.getAttribute("bad@key"));
    }

    @Test
    public void testFromJsoup_attributeNameBecomesEmpty_skipped() {
        Document jsoupDoc = new Document("");
        Element el = new Element("div");
        el.attr("$$$", "value");
        jsoupDoc.appendChild(el);
        org.w3c.dom.Document w3cDoc = convert(jsoupDoc);
        org.w3c.dom.Element rootEl = (org.w3c.dom.Element) w3cDoc.getFirstChild();
        assertEquals(0, rootEl.getAttributes().getLength());
    }

    @Test
    public void testFromJsoup_namespaceInheritedFromParent_defaultNamespaceInherited() {
        Document jsoupDoc = new Document("");
        Element parent = new Element("parent");
        parent.attr("xmlns", "http://parent");
        Element child = new Element("child");
        parent.appendChild(child);
        jsoupDoc.appendChild(parent);
        org.w3c.dom.Document w3cDoc = convert(jsoupDoc);
        org.w3c.dom.Element w3cParent = (org.w3c.dom.Element) w3cDoc.getFirstChild();
        org.w3c.dom.Element w3cChild = (org.w3c.dom.Element) w3cParent.getFirstChild();
        assertEquals("http://parent", w3cChild.getNamespaceURI());
    }

    @Test
    public void testFromJsoup_namespaceInheritedFromParent_prefixedNamespaceInherited() {
        Document jsoupDoc = new Document("");
        Element parent = new Element("parent");
        parent.attr("xmlns:foo", "http://foo");
        Element child = new Element("foo:child");
        parent.appendChild(child);
        jsoupDoc.appendChild(parent);
        org.w3c.dom.Document w3cDoc = convert(jsoupDoc);
        org.w3c.dom.Element w3cParent = (org.w3c.dom.Element) w3cDoc.getFirstChild();
        org.w3c.dom.Element w3cChild = (org.w3c.dom.Element) w3cParent.getFirstChild();
        assertEquals("http://foo", w3cChild.getNamespaceURI());
        assertEquals("foo", w3cChild.getPrefix());
        assertEquals("child", w3cChild.getLocalName());
    }

    @Test
    public void testFromJsoup_multipleXmlnsDeclarations_lastDeclarationWins() {
        Document jsoupDoc = new Document("");
        Element el = new Element("foo:bar");
        el.attr("xmlns:foo", "first");
        el.attr("xmlns:foo", "second");
        jsoupDoc.appendChild(el);
        org.w3c.dom.Document w3cDoc = convert(jsoupDoc);
        org.w3c.dom.Element rootEl = (org.w3c.dom.Element) w3cDoc.getFirstChild();
        assertEquals("second", rootEl.getNamespaceURI());
    }
}