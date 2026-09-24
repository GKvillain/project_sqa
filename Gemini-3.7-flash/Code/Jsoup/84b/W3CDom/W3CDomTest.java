package org.jsoup.helper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class W3CDomTest {

    private W3CDom w3cDom;

    @Before
    public void setUp() {
        w3cDom = new W3CDom();
    }

    // Tests null input throwing exception
    @Test(expected = IllegalArgumentException.class)
    public void testFromJsoup_nullDocument_throwsException() {
        w3cDom.fromJsoup(null);
    }

    // Tests normal conversion of basic HTML document
    @Test
    public void testFromJsoup_simpleHtml_convertsCorrectly() {
        String html = "<html><head><title>Test Title</title></head><body><p class=\"lead\">Hello World</p></body></html>";
        Document jsoupDoc = Jsoup.parse(html);
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);

        assertNotNull(w3cDoc);
        assertEquals("html", w3cDoc.getDocumentElement().getTagName());
        NodeList titleNodes = w3cDoc.getElementsByTagName("title");
        assertEquals(1, titleNodes.getLength());
        assertEquals("Test Title", titleNodes.item(0).getTextContent());

        NodeList pNodes = w3cDoc.getElementsByTagName("p");
        assertEquals(1, pNodes.getLength());
        org.w3c.dom.Element pElem = (org.w3c.dom.Element) pNodes.item(0);
        assertEquals("Hello World", pElem.getTextContent());
        assertEquals("lead", pElem.getAttribute("class"));
    }

    // Tests DocumentURI propagation from Jsoup Document location
    @Test
    public void testConvert_withDocumentLocation_setsDocumentUri() {
        String html = "<html><head></head><body></body></html>";
        Document jsoupDoc = Jsoup.parse(html, "https://example.com/test.html");
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);

        assertEquals("https://example.com/test.html", w3cDoc.getDocumentURI());
    }

    // Tests comment node conversion
    @Test
    public void testFromJsoup_withCommentNode_convertsComment() {
        String html = "<html><body><!-- This is a comment --><p>Text</p></body></html>";
        Document jsoupDoc = Jsoup.parse(html);
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);

        Node body = w3cDoc.getElementsByTagName("body").item(0);
        Node firstChild = body.getFirstChild();
        assertEquals(Node.COMMENT_NODE, firstChild.getNodeType());
        assertEquals(" This is a comment ", firstChild.getNodeValue());
    }

    // Tests data node (script/style) conversion
    @Test
    public void testFromJsoup_withDataNode_convertsDataNode() {
        String html = "<html><head><script>var a = 1 & 2;</script></head><body></body></html>";
        Document jsoupDoc = Jsoup.parse(html);
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);

        NodeList scriptNodes = w3cDoc.getElementsByTagName("script");
        assertEquals(1, scriptNodes.getLength());
        assertEquals("var a = 1 & 2;", scriptNodes.item(0).getTextContent());
    }

    // Tests serialization of W3C document to String
    @Test
    public void testAsString_validDocument_returnsXmlString() {
        String html = "<html><head><title>Sample</title></head><body><p>Test</p></body></html>";
        Document jsoupDoc = Jsoup.parse(html);
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        String xml = w3cDom.asString(w3cDoc);

        assertNotNull(xml);
        assertTrue(xml.contains("<title>Sample</title>"));
        assertTrue(xml.contains("<p>Test</p>"));
    }

    // Tests namespace handling on elements and prefixes
    @Test
    public void testFromJsoup_withNamespaces_resolvesNamespaceUri() {
        String html = "<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:epub=\"http://www.idpf.org/2007/ops\">" +
                "<head><title>EPUB</title></head>" +
                "<body><epub:section>Content</epub:section></body></html>";
        Document jsoupDoc = Jsoup.parse(html);
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);

        assertEquals("http://www.w3.org/1999/xhtml", w3cDoc.getDocumentElement().getNamespaceURI());

        NodeList sectionNodes = w3cDoc.getElementsByTagNameNS("http://www.idpf.org/2007/ops", "section");
        assertEquals(1, sectionNodes.getLength());
        assertEquals("Content", sectionNodes.item(0).getTextContent());
    }

    // Tests undeclared namespace prefix (defect check for Jsoup-84)
    @Test
    public void testFromJsoup_undeclaredNamespacePrefix_convertsWithoutException() {
        String html = "<html><body><fb:like>Like</fb:like></body></html>";
        Document jsoupDoc = Jsoup.parse(html);
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);

        assertNotNull(w3cDoc);
        NodeList fbNodes = w3cDoc.getElementsByTagName("fb:like");
        assertEquals(1, fbNodes.getLength());
        assertEquals("Like", fbNodes.item(0).getTextContent());
    }

    // Tests invalid characters in attribute names being filtered
    @Test
    public void testFromJsoup_invalidAttributeCharacters_filtersAttribute() {
        String html = "<html><body><p valid-name=\"true\" inv@lid=\"false\">Text</p></body></html>";
        Document jsoupDoc = Jsoup.parse(html);
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);

        org.w3c.dom.Element p = (org.w3c.dom.Element) w3cDoc.getElementsByTagName("p").item(0);
        assertEquals("true", p.getAttribute("valid-name"));
        assertEquals("false", p.getAttribute("invlid"));
    }

    // Tests convert method with existing custom W3C Document
    @Test
    public void testConvert_customW3cDocument_populatesDocument() throws Exception {
        Document jsoupDoc = Jsoup.parse("<html><body><div>Nested <span>Text</span></div></body></html>");
        org.w3c.dom.Document customDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

        w3cDom.convert(jsoupDoc, customDoc);

        assertNotNull(customDoc.getDocumentElement());
        assertEquals("html", customDoc.getDocumentElement().getTagName());
        NodeList spanNodes = customDoc.getElementsByTagName("span");
        assertEquals(1, spanNodes.getLength());
        assertEquals("Text", spanNodes.item(0).getTextContent());
    }

    // Tests namespace prefix shadowing in nested elements
    @Test
    public void testFromJsoup_nestedNamespaceShadowing_updatesNamespaceCorrectly() {
        String html = "<html xmlns:ns=\"urn:outer\"><body ns:attr=\"1\"><div xmlns:ns=\"urn:inner\"><ns:el>Inner</ns:el></div></body></html>";
        Document jsoupDoc = Jsoup.parse(html);
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);

        NodeList innerList = w3cDoc.getElementsByTagNameNS("urn:inner", "el");
        assertEquals(1, innerList.getLength());
        assertEquals("Inner", innerList.item(0).getTextContent());
    }
}