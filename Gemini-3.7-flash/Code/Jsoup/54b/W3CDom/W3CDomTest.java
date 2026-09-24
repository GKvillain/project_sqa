package org.jsoup.helper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.TextNode;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;

import static org.junit.Assert.*;

public class W3CDomTest {

    // Tests basic conversion from Jsoup Document to W3C Document
    @Test
    public void testFromJsoup_validHtml_convertsSuccessfully() {
        String html = "<html><head><title>Test</title></head><body><p id=\"p1\">Hello</p></body></html>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html);

        W3CDom w3cDom = new W3CDom();
        Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);

        assertNotNull(w3cDoc);
        Element root = w3cDoc.getDocumentElement();
        assertEquals("html", root.getTagName());
        NodeList pElements = root.getElementsByTagName("p");
        assertEquals(1, pElements.getLength());
        Element p = (Element) pElements.item(0);
        assertEquals("p1", p.getAttribute("id"));
        assertEquals("Hello", p.getTextContent());
    }

    // Tests null input to fromJsoup throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testFromJsoup_nullDocument_throwsIllegalArgumentException() {
        W3CDom w3cDom = new W3CDom();
        w3cDom.fromJsoup(null);
    }

    // Tests conversion with document location URI set
    @Test
    public void testConvert_withDocumentLocation_setsDocumentUri() {
        String html = "<html><head></head><body><p>Test</p></body></html>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html, "http://example.com/page.html");

        W3CDom w3cDom = new W3CDom();
        Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);

        assertEquals("http://example.com/page.html", w3cDoc.getDocumentURI());
    }

    // Tests conversion without document location URI
    @Test
    public void testConvert_blankDocumentLocation_doesNotSetDocumentUri() {
        String html = "<html><head></head><body><p>Test</p></body></html>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html);

        W3CDom w3cDom = new W3CDom();
        Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);

        assertNull(w3cDoc.getDocumentURI());
    }

    // Tests conversion with default and prefixed XML namespaces
    @Test
    public void testConvert_withNamespaces_handlesPrefixAndDefaultNamespace() {
        String html = "<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:epub=\"http://www.idpf.org/2007/ops\">" +
                "<head><title>NS Test</title></head>" +
                "<body><epub:section>Content</epub:section></body></html>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html);

        W3CDom w3cDom = new W3CDom();
        Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);

        Element root = w3cDoc.getDocumentElement();
        assertEquals("http://www.w3.org/1999/xhtml", root.getNamespaceURI());

        NodeList sections = root.getElementsByTagName("epub:section");
        assertEquals(1, sections.getLength());
        Element section = (Element) sections.item(0);
        assertEquals("http://www.idpf.org/2007/ops", section.getNamespaceURI());
        assertEquals("epub:section", section.getTagName());
    }

    // Tests conversion of comments, text nodes, and data nodes (e.g. script/style)
    @Test
    public void testConvert_textCommentAndDataNodes_convertsAllNodeTypes() {
        String html = "<html><head><script>var x = 10;</script></head><body><!-- A comment --><p>Text</p></body></html>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html);

        W3CDom w3cDom = new W3CDom();
        Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);

        NodeList scripts = w3cDoc.getElementsByTagName("script");
        assertEquals(1, scripts.getLength());
        assertEquals("var x = 10;", scripts.item(0).getTextContent());

        NodeList bodyList = w3cDoc.getElementsByTagName("body");
        assertEquals(1, bodyList.getLength());
        Element body = (Element) bodyList.item(0);

        boolean foundComment = false;
        Node child = body.getFirstChild();
        while (child != null) {
            if (child.getNodeType() == Node.COMMENT_NODE) {
                assertEquals(" A comment ", child.getNodeValue());
                foundComment = true;
            }
            child = child.getNextSibling();
        }
        assertTrue(foundComment);
    }

    // Tests conversion of nested hierarchy ensuring proper tree structure and undescend
    @Test
    public void testConvert_nestedElements_maintainsHierarchy() {
        String html = "<div><ul><li><span>One</span></li><li>Two</li></ul><p>Three</p></div>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html);

        W3CDom w3cDom = new W3CDom();
        Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);

        Element root = w3cDoc.getDocumentElement();
        NodeList spans = root.getElementsByTagName("span");
        assertEquals(1, spans.getLength());
        assertEquals("One", spans.item(0).getTextContent());

        NodeList ps = root.getElementsByTagName("p");
        assertEquals(1, ps.getLength());
        assertEquals("Three", ps.item(0).getTextContent());
    }

    // Tests attribute name sanitization when attribute contains invalid xml characters (Bug 54 regression)
    @Test
    public void testConvert_attributeWithInvalidCharacters_stripsInvalidCharacters() {
        String html = "<div id=\"test\" invalid?attr=\"val\" data-custom=\"123\">Content</div>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html);

        W3CDom w3cDom = new W3CDom();
        Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);

        Element div = (Element) w3cDoc.getElementsByTagName("div").item(0);
        assertEquals("test", div.getAttribute("id"));
        assertEquals("val", div.getAttribute("invalidattr"));
        assertEquals("123", div.getAttribute("data-custom"));
    }

    // Tests asString serialization method
    @Test
    public void testAsString_validW3CDocument_returnsXmlString() {
        String html = "<html><head><title>Title</title></head><body><p>Hello World</p></body></html>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html);

        W3CDom w3cDom = new W3CDom();
        Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        String xml = w3cDom.asString(w3cDoc);

        assertNotNull(xml);
        assertTrue(xml.contains("<title>Title</title>"));
        assertTrue(xml.contains("<p>Hello World</p>"));
    }

    // Tests manual convert method with pre-created Document
    @Test
    public void testConvert_manualDestinationDocument_populatesDocument() throws Exception {
        String html = "<html><body><h1>Header</h1></body></html>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html);

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document out = dbf.newDocumentBuilder().newDocument();

        W3CDom w3cDom = new W3CDom();
        w3cDom.convert(jsoupDoc, out);

        Element root = out.getDocumentElement();
        assertNotNull(root);
        assertEquals("html", root.getTagName());
        assertEquals(1, root.getElementsByTagName("h1").getLength());
        assertEquals("Header", root.getElementsByTagName("h1").item(0).getTextContent());
    }
}