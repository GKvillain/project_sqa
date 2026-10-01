package org.jsoup.helper;

import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;

import static org.junit.Assert.*;

public class W3CDomTest {

    private org.jsoup.nodes.Document parseXml(String xml) {
        return Jsoup.parse(xml, "", Parser.xmlParser());
    }

    // Tests basic HTML conversion to a W3C Document.
    @Test
    public void testFromJsoup_basicHtml_createsW3CDocument() {
        org.jsoup.nodes.Document in = Jsoup.parse("<html><head><title>Title</title></head><body><p id='x'>Hello</p></body></html>");
        Document out = new W3CDom().fromJsoup(in);

        assertNotNull(out.getDocumentElement());
        assertEquals("html", out.getDocumentElement().getTagName());
        assertEquals("Title", out.getElementsByTagName("title").item(0).getTextContent());

        Element p = (Element) out.getElementsByTagName("p").item(0);
        assertEquals("Hello", p.getTextContent());
        assertEquals("x", p.getAttribute("id"));
        assertNull(out.getDocumentElement().getNamespaceURI());
    }

    // Tests blank document location does not set W3C document URI.
    @Test
    public void testFromJsoup_blankLocation_doesNotSetDocumentUri() {
        org.jsoup.nodes.Document in = Jsoup.parse("<html></html>", "   ");
        Document out = new W3CDom().fromJsoup(in);

        assertNull(out.getDocumentURI());
    }

    // Tests non-blank document location is copied to W3C document URI.
    @Test
    public void testFromJsoup_nonBlankLocation_setsDocumentUri() {
        org.jsoup.nodes.Document in = Jsoup.parse("<html></html>", "http://example.com/");
        Document out = new W3CDom().fromJsoup(in);

        assertEquals("http://example.com/", out.getDocumentURI());
    }

    // Tests null input validation.
    @Test(expected = IllegalArgumentException.class)
    public void testFromJsoup_nullInput_throwsException() {
        new W3CDom().fromJsoup(null);
    }

    // Tests the public convert method with a namespace-aware W3C document.
    @Test
    public void testConvert_basicHtml_appendsToProvidedDocument() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        Document out = factory.newDocumentBuilder().newDocument();

        org.jsoup.nodes.Document in = Jsoup.parse("<html><body><p>text</p></body></html>");
        new W3CDom().convert(in, out);

        assertEquals("html", out.getDocumentElement().getTagName());
        assertEquals("text", out.getElementsByTagName("p").item(0).getTextContent());
    }

    // Tests comment node conversion.
    @Test
    public void testConvert_commentNode_appendsComment() {
        org.jsoup.nodes.Document in = Jsoup.parse("<html><body><!-- comment --><p>text</p></body></html>");
        Document out = new W3CDom().fromJsoup(in);

        Node body = out.getElementsByTagName("body").item(0);
        NodeList children = body.getChildNodes();
        boolean foundComment = false;

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.COMMENT_NODE) {
                foundComment = true;
                assertTrue(child.getNodeValue().contains("comment"));
            }
        }

        assertTrue(foundComment);
    }

    // Tests DataNode conversion, e.g. script content.
    @Test
    public void testConvert_dataNode_appendsText() {
        org.jsoup.nodes.Document in = Jsoup.parse("<html><body><script>if (a < b) alert('x');</script></body></html>");
        Document out = new W3CDom().fromJsoup(in);

        Element script = (Element) out.getElementsByTagName("script").item(0);
        assertTrue(script.getTextContent().contains("if (a < b) alert('x');"));
    }

    // Tests invalid attribute names are sanitized or ignored.
    @Test
    public void testConvert_attributeNameSanitization_handlesInvalidNames() {
        org.jsoup.nodes.Document in = Jsoup.parse("<html><body><div 1bad='no' bad!attr='y'>x</div></body></html>");
        Document out = new W3CDom().fromJsoup(in);

        Element div = (Element) out.getElementsByTagName("div").item(0);
        assertFalse(div.hasAttribute("1bad"));
        assertTrue(div.hasAttribute("badattr"));
        assertEquals("y", div.getAttribute("badattr"));
    }

    // Tests a prefixed element with a declared namespace.
    @Test
    public void testConvert_elementWithNamespacePrefix_preservesNamespace() {
        org.jsoup.nodes.Document in = parseXml("<html xmlns:svg='http://www.w3.org/2000/svg'><body><svg:svg/></body></html>");
        Document out = new W3CDom().fromJsoup(in);

        NodeList svgNodes = out.getElementsByTagNameNS("http://www.w3.org/2000/svg", "svg");
        assertEquals(1, svgNodes.getLength());

        Element svg = (Element) svgNodes.item(0);
        assertEquals("svg:svg", svg.getTagName());
        assertEquals("http://www.w3.org/2000/svg", svg.getNamespaceURI());
    }

    // Tests default namespace is applied to child elements.
    @Test
    public void testConvert_defaultNamespace_appliesToChildElements() {
        org.jsoup.nodes.Document in = parseXml("<root xmlns='http://example.com/ns'><child>text</child></root>");
        Document out = new W3CDom().fromJsoup(in);

        assertEquals("http://example.com/ns", out.getDocumentElement().getNamespaceURI());

        NodeList children = out.getElementsByTagNameNS("http://example.com/ns", "child");
        assertEquals(1, children.getLength());
        assertEquals("text", children.item(0).getTextContent());
    }

    // Tests namespace declarations do not leak into following siblings.
    @Test
    public void testConvert_childNamespaceDoesNotLeakToSibling() {
        org.jsoup.nodes.Document in = parseXml(
            "<root xmlns:a='urn:outer'>" +
            "<child xmlns:a='urn:inner'><a:item>one</a:item></child>" +
            "<a:item>two</a:item>" +
            "</root>"
        );
        Document out = new W3CDom().fromJsoup(in);

        NodeList items = out.getElementsByTagNameNS("*", "item");
        assertEquals(2, items.getLength());
        assertEquals("urn:inner", items.item(0).getNamespaceURI());
        assertEquals("urn:outer", items.item(1).getNamespaceURI());
    }

    // Tests asString serializes a W3C document.
    @Test
    public void testAsString_serializesDocument_containsContent() {
        Document out = new W3CDom().fromJsoup(Jsoup.parse("<html><body><p>Hello</p></body></html>"));
        String xml = new W3CDom().asString(out);

        assertTrue(xml.contains("<p>"));
        assertTrue(xml.contains("Hello"));
    }

    // Tests asString keeps namespace declarations.
    @Test
    public void testAsString_namespacedDocument_containsNamespaceDeclaration() {
        Document out = new W3CDom().fromJsoup(Jsoup.parse("<html xmlns='http://www.w3.org/1999/xhtml'><body>X</body></html>"));
        String xml = new W3CDom().asString(out);

        assertTrue(xml.contains("http://www.w3.org/1999/xhtml"));
    }
}