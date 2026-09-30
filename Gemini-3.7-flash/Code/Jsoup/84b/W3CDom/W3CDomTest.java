package org.jsoup.helper;

import org.jsoup.Jsoup;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
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

    // Tests null input to fromJsoup throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testFromJsoup_nullDocument_throwsException() {
        w3cDom.fromJsoup(null);
    }

    // Tests simple HTML document conversion and W3C Document structure
    @Test
    public void testFromJsoup_simpleHtml_convertsStructure() {
        String html = "<html><head><title>W3C Test</title></head><body><p id=\"test-p\">Hello World</p></body></html>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html);

        Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        assertNotNull(w3cDoc);

        NodeList pNodes = w3cDoc.getElementsByTagName("p");
        assertEquals(1, pNodes.getLength());
        Element pElem = (Element) pNodes.item(0);
        assertEquals("Hello World", pElem.getTextContent());
        assertEquals("test-p", pElem.getAttribute("id"));
    }

    // Tests document URI propagation when location is present
    @Test
    public void testConvert_withLocation_setsDocumentURI() {
        String html = "<html><body><p>Location Test</p></body></html>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html, "https://example.com/page.html");

        Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        assertEquals("https://example.com/page.html", w3cDoc.getDocumentURI());
    }

    // Tests document URI when location is blank
    @Test
    public void testConvert_blankLocation_doesNotSetDocumentURI() {
        String html = "<html><body><p>No Location</p></body></html>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html);

        Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        assertEquals(null, w3cDoc.getDocumentURI());
    }

    // Tests conversion of comments and text nodes
    @Test
    public void testFromJsoup_withCommentAndText_convertsNodes() {
        String html = "<div><!-- A sample comment -->Sample Text</div>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parseBodyFragment(html);

        Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        NodeList bodyChildren = w3cDoc.getElementsByTagName("div").item(0).getChildNodes();

        boolean foundComment = false;
        boolean foundText = false;
        for (int i = 0; i < bodyChildren.getLength(); i++) {
            Node child = bodyChildren.item(i);
            if (child.getNodeType() == Node.COMMENT_NODE && " A sample comment ".equals(child.getNodeValue())) {
                foundComment = true;
            }
            if (child.getNodeType() == Node.TEXT_NODE && "Sample Text".equals(child.getNodeValue())) {
                foundText = true;
            }
        }
        assertTrue(foundComment);
        assertTrue(foundText);
    }

    // Tests conversion of DataNode (e.g., inside script elements)
    @Test
    public void testFromJsoup_withDataNode_convertsDataNode() {
        String html = "<script>var x = 10;</script>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parseBodyFragment(html);

        Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        NodeList scriptNodes = w3cDoc.getElementsByTagName("script");
        assertEquals(1, scriptNodes.getLength());
        assertEquals("var x = 10;", scriptNodes.item(0).getTextContent());
    }

    // Tests default and prefixed XML namespace declarations
    @Test
    public void testFromJsoup_withNamespaces_handlesNamespaceScope() {
        String xml = "<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:epub=\"http://www.idpf.org/2007/ops\">" +
                "<head><title>NS Test</title></head>" +
                "<body><epub:section epub:type=\"chapter\"><p>Chapter 1</p></epub:section></body></html>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(xml, "", org.jsoup.parser.Parser.xmlParser());

        Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        Element root = w3cDoc.getDocumentElement();
        assertEquals("http://www.w3.org/1999/xhtml", root.getNamespaceURI());

        NodeList sectionNodes = w3cDoc.getElementsByTagName("epub:section");
        assertEquals(1, sectionNodes.getLength());
        Element sectionElem = (Element) sectionNodes.item(0);
        assertEquals("http://www.idpf.org/2007/ops", sectionElem.getNamespaceURI());
        assertEquals("chapter", sectionElem.getAttribute("epub:type"));
    }

    // Tests handling of undeclared namespace prefix (Defects4J bug 84)
    @Test
    public void testFromJsoup_undeclaredPrefix_handlesWithoutCrashing() {
        String html = "<html><body><fb:like href=\"http://example.com\"></fb:like></body></html>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html);

        Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        assertNotNull(w3cDoc);
        NodeList fbNodes = w3cDoc.getElementsByTagName("fb:like");
        assertEquals(1, fbNodes.getLength());
        Element fbElem = (Element) fbNodes.item(0);
        assertEquals("http://example.com", fbElem.getAttribute("href"));
    }

    // Tests sanitization of invalid XML attribute names
    @Test
    public void testFromJsoup_invalidAttributeNames_filtersInvalidChars() {
        String html = "<div valid_name=\"ok\" invalid^name=\"filtered\" 123start=\"startsNum\">Content</div>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parseBodyFragment(html);

        Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        NodeList divList = w3cDoc.getElementsByTagName("div");
        assertEquals(1, divList.getLength());
        Element div = (Element) divList.item(0);

        assertEquals("ok", div.getAttribute("valid_name"));
        assertEquals("filtered", div.getAttribute("invalidname"));
    }

    // Tests serialization of W3C Document to String
    @Test
    public void testAsString_validDocument_serializesToString() {
        String html = "<html><head><title>String Test</title></head><body><p>Hello W3C</p></body></html>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html);

        Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        String xmlString = w3cDom.asString(w3cDoc);

        assertNotNull(xmlString);
        assertTrue(xmlString.contains("<title>String Test</title>"));
        assertTrue(xmlString.contains("<p>Hello W3C</p>"));
    }

    // Tests deep nesting and stack unwinding during traversal
    @Test
    public void testConvert_nestedElements_preservesHierarchy() {
        String html = "<div id=\"outer\"><div id=\"middle\"><div id=\"inner\">Deep</div></div></div>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parseBodyFragment(html);

        Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        Element outer = (Element) w3cDoc.getElementsByTagName("div").item(0);
        assertEquals("outer", outer.getAttribute("id"));

        Element middle = (Element) outer.getElementsByTagName("div").item(0);
        assertEquals("middle", middle.getAttribute("id"));

        Element inner = (Element) middle.getElementsByTagName("div").item(0);
        assertEquals("inner", inner.getAttribute("id"));
        assertEquals("Deep", inner.getTextContent());
    }

    // Tests explicit convert method with pre-created Document
    @Test
    public void testConvert_customW3CDocument_populatesDocument() throws Exception {
        String html = "<html><body><h1>Direct Convert</h1></body></html>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html);

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document customDoc = dbf.newDocumentBuilder().newDocument();

        w3cDom.convert(jsoupDoc, customDoc);

        NodeList h1Nodes = customDoc.getElementsByTagName("h1");
        assertEquals(1, h1Nodes.getLength());
        assertEquals("Direct Convert", h1Nodes.item(0).getTextContent());
    }
}