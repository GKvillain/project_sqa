package org.jsoup.helper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Test;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class W3CDomTest {

    // Tests null input throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testFromJsoup_nullDocument_throwsIllegalArgumentException() {
        W3CDom w3cDom = new W3CDom();
        w3cDom.fromJsoup(null);
    }

    // Tests basic conversion from Jsoup Document to W3C Document
    @Test
    public void testFromJsoup_simpleHtml_convertsCorrectly() {
        String html = "<html><head><title>Simple</title></head><body><p>Hello World</p></body></html>";
        Document jsoupDoc = Jsoup.parse(html);

        W3CDom w3cDom = new W3CDom();
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);

        assertNotNull(w3cDoc);
        assertNotNull(w3cDoc.getDocumentElement());
        assertEquals("html", w3cDoc.getDocumentElement().getTagName());

        NodeList pNodes = w3cDoc.getElementsByTagName("p");
        assertEquals(1, pNodes.getLength());
        assertEquals("Hello World", pNodes.item(0).getTextContent());
    }

    // Tests conversion with document location URI set
    @Test
    public void testFromJsoup_withLocation_setsDocumentUri() {
        String html = "<html><head></head><body><p>Test</p></body></html>";
        Document jsoupDoc = Jsoup.parse(html, "http://example.com/page.html");

        W3CDom w3cDom = new W3CDom();
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);

        assertEquals("http://example.com/page.html", w3cDoc.getDocumentURI());
    }

    // Tests conversion without document location URI
    @Test
    public void testFromJsoup_blankLocation_documentUriNotSet() {
        String html = "<html><head></head><body><p>Test</p></body></html>";
        Document jsoupDoc = Jsoup.parse(html);

        W3CDom w3cDom = new W3CDom();
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);

        assertNull(w3cDoc.getDocumentURI());
    }

    // Tests conversion of comments and script data nodes
    @Test
    public void testFromJsoup_withCommentsAndDataNodes_convertsNodeTypes() {
        String html = "<html><head><script>var x = 10;</script></head><body><!-- comment text --><p>Content</p></body></html>";
        Document jsoupDoc = Jsoup.parse(html);

        W3CDom w3cDom = new W3CDom();
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);

        NodeList scriptList = w3cDoc.getElementsByTagName("script");
        assertEquals(1, scriptList.getLength());
        assertEquals("var x = 10;", scriptList.item(0).getTextContent());

        Node bodyNode = w3cDoc.getElementsByTagName("body").item(0);
        Node firstChild = bodyNode.getFirstChild();
        assertEquals(Node.COMMENT_NODE, firstChild.getNodeType());
        assertEquals(" comment text ", firstChild.getNodeValue());
    }

    // Tests copying and filtering of valid and invalid attribute names
    @Test
    public void testFromJsoup_withAttributes_copiesAndSanitizesAttributeNames() {
        String html = "<html><body><div id=\"main\" class=\"container\" valid_attr=\"val\">Text</div></body></html>";
        Document jsoupDoc = Jsoup.parse(html);

        W3CDom w3cDom = new W3CDom();
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);

        org.w3c.dom.Element div = (org.w3c.dom.Element) w3cDoc.getElementsByTagName("div").item(0);
        assertEquals("main", div.getAttribute("id"));
        assertEquals("container", div.getAttribute("class"));
        assertEquals("val", div.getAttribute("valid_attr"));
    }

    // Tests default namespace declaration handling
    @Test
    public void testFromJsoup_withDefaultNamespace_setsElementNamespace() {
        String xml = "<root xmlns=\"http://example.com/ns\"><child>Item</child></root>";
        Document jsoupDoc = Jsoup.parse(xml, "", org.jsoup.parser.Parser.xmlParser());

        W3CDom w3cDom = new W3CDom();
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);

        org.w3c.dom.Element root = w3cDoc.getDocumentElement();
        assertEquals("http://example.com/ns", root.getNamespaceURI());
        assertEquals("root", root.getTagName());

        org.w3c.dom.Element child = (org.w3c.dom.Element) root.getElementsByTagName("child").item(0);
        assertEquals("http://example.com/ns", child.getNamespaceURI());
    }

    // Tests prefixed namespace declaration handling
    @Test
    public void testFromJsoup_withPrefixedNamespace_setsElementNamespace() {
        String xml = "<root xmlns:ns=\"http://example.com/ns\"><ns:item id=\"1\">Text</ns:item></root>";
        Document jsoupDoc = Jsoup.parse(xml, "", org.jsoup.parser.Parser.xmlParser());

        W3CDom w3cDom = new W3CDom();
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);

        org.w3c.dom.Element root = w3cDoc.getDocumentElement();
        NodeList items = root.getElementsByTagNameNS("http://example.com/ns", "item");
        assertEquals(1, items.getLength());

        org.w3c.dom.Element item = (org.w3c.dom.Element) items.item(0);
        assertEquals("http://example.com/ns", item.getNamespaceURI());
        assertEquals("ns:item", item.getTagName());
        assertEquals("1", item.getAttribute("id"));
    }

    // Tests nested elements with multiple namespace prefixes across hierarchy
    @Test
    public void testFromJsoup_nestedNamespaces_retainsNamespaceContext() {
        String xml = "<root xmlns:a=\"http://example.com/a\">" +
                "<a:sub><child xmlns:b=\"http://example.com/b\"><b:leaf>Value</b:leaf></child></a:sub>" +
                "</root>";
        Document jsoupDoc = Jsoup.parse(xml, "", org.jsoup.parser.Parser.xmlParser());

        W3CDom w3cDom = new W3CDom();
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);

        NodeList leaves = w3cDoc.getElementsByTagName("b:leaf");
        assertEquals(1, leaves.getLength());
        assertEquals("http://example.com/b", leaves.item(0).getNamespaceURI());
    }

    // Tests serialize W3C document to String using asString
    @Test
    public void testAsString_validDocument_serializesToString() {
        String html = "<html><head><title>Title</title></head><body><p>Hello</p></body></html>";
        Document jsoupDoc = Jsoup.parse(html);

        W3CDom w3cDom = new W3CDom();
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);

        String result = w3cDom.asString(w3cDoc);
        assertNotNull(result);
        assertTrue(result.contains("<title>Title</title>"));
        assertTrue(result.contains("<p>Hello</p>"));
    }

    // Tests converting using an existing W3C Document instance via convert method
    @Test
    public void testConvert_customW3cDocument_populatesCorrectly() throws Exception {
        String html = "<html><body><section><span>Content</span></section></body></html>";
        Document jsoupDoc = Jsoup.parse(html);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        org.w3c.dom.Document customDoc = factory.newDocumentBuilder().newDocument();

        W3CDom w3cDom = new W3CDom();
        w3cDom.convert(jsoupDoc, customDoc);

        assertNotNull(customDoc.getDocumentElement());
        assertEquals("html", customDoc.getDocumentElement().getTagName());
        NodeList spans = customDoc.getElementsByTagName("span");
        assertEquals(1, spans.getLength());
        assertEquals("Content", spans.item(0).getTextContent());
    }

    // Tests traversal undescending via tail correctly restores tree depth
    @Test
    public void testFromJsoup_siblingElementsStructure_restoresParentDepth() {
        String html = "<html><body><div><p>First</p><p>Second</p></div><div><span>Third</span></div></body></html>";
        Document jsoupDoc = Jsoup.parse(html);

        W3CDom w3cDom = new W3CDom();
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);

        NodeList divs = w3cDoc.getElementsByTagName("div");
        assertEquals(2, divs.getLength());

        org.w3c.dom.Element firstDiv = (org.w3c.dom.Element) divs.item(0);
        NodeList firstDivChildren = firstDiv.getElementsByTagName("p");
        assertEquals(2, firstDivChildren.getLength());

        org.w3c.dom.Element secondDiv = (org.w3c.dom.Element) divs.item(1);
        NodeList secondDivChildren = secondDiv.getElementsByTagName("span");
        assertEquals(1, secondDivChildren.getLength());
    }
}