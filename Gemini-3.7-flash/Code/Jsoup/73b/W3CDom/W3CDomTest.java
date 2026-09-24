package org.jsoup.helper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;

import static org.junit.Assert.*;

public class W3CDomTest {

    // Tests null input throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testFromJsoup_nullInput_throwsException() {
        W3CDom w3c = new W3CDom();
        w3c.fromJsoup(null);
    }

    // Tests normal HTML conversion to W3C Document structure
    @Test
    public void testFromJsoup_standardHtml_convertsStructure() {
        String html = "<html><head><title>Test Title</title></head><body><p class=\"intro\">Hello <b>world</b></p></body></html>";
        Document jsoupDoc = Jsoup.parse(html);
        W3CDom w3c = new W3CDom();
        org.w3c.dom.Document w3cDoc = w3c.fromJsoup(jsoupDoc);

        assertNotNull(w3cDoc);
        assertEquals("html", w3cDoc.getDocumentElement().getTagName());
        NodeList pElements = w3cDoc.getElementsByTagName("p");
        assertEquals(1, pElements.getLength());
        org.w3c.dom.Element p = (org.w3c.dom.Element) pElements.item(0);
        assertEquals("intro", p.getAttribute("class"));
        assertEquals("Hello world", p.getTextContent());
    }

    // Tests document URI conversion when location is present
    @Test
    public void testFromJsoup_withLocation_setsDocumentUri() {
        String html = "<html><body><p>URI Test</p></body></html>";
        Document jsoupDoc = Jsoup.parse(html, "http://example.com/page.html");
        W3CDom w3c = new W3CDom();
        org.w3c.dom.Document w3cDoc = w3c.fromJsoup(jsoupDoc);

        assertEquals("http://example.com/page.html", w3cDoc.getDocumentURI());
    }

    // Tests document URI when location is blank
    @Test
    public void testFromJsoup_blankLocation_documentUriNull() {
        String html = "<html><body><p>No URI</p></body></html>";
        Document jsoupDoc = Jsoup.parse(html);
        W3CDom w3c = new W3CDom();
        org.w3c.dom.Document w3cDoc = w3c.fromJsoup(jsoupDoc);

        assertNull(w3cDoc.getDocumentURI());
    }

    // Tests Comment and DataNode conversion
    @Test
    public void testFromJsoup_commentAndDataNodes_convertsCorrectly() {
        String html = "<html><head><script>var x = 10;</script></head><body><!-- test comment --><p>Body</p></body></html>";
        Document jsoupDoc = Jsoup.parse(html);
        W3CDom w3c = new W3CDom();
        org.w3c.dom.Document w3cDoc = w3c.fromJsoup(jsoupDoc);

        org.w3c.dom.Element script = (org.w3c.dom.Element) w3cDoc.getElementsByTagName("script").item(0);
        assertEquals("var x = 10;", script.getTextContent());

        org.w3c.dom.Element body = (org.w3c.dom.Element) w3cDoc.getElementsByTagName("body").item(0);
        Node firstChild = body.getFirstChild();
        assertEquals(Node.COMMENT_NODE, firstChild.getNodeType());
        assertEquals(" test comment ", firstChild.getNodeValue());
    }

    // Tests default namespace handling
    @Test
    public void testFromJsoup_defaultNamespace_setsNamespaceUri() {
        String html = "<html xmlns=\"http://www.w3.org/1999/xhtml\"><head><title>Title</title></head><body><p>Text</p></body></html>";
        Document jsoupDoc = Jsoup.parse(html);
        W3CDom w3c = new W3CDom();
        org.w3c.dom.Document w3cDoc = w3c.fromJsoup(jsoupDoc);

        assertEquals("http://www.w3.org/1999/xhtml", w3cDoc.getDocumentElement().getNamespaceURI());
    }

    // Tests prefixed namespace mapping
    @Test
    public void testFromJsoup_prefixedNamespace_setsNamespaceUriOnPrefixedElement() {
        String html = "<root xmlns:custom=\"http://example.com/custom\"><custom:element id=\"1\">Value</custom:element></root>";
        Document jsoupDoc = Jsoup.parse(html);
        W3CDom w3c = new W3CDom();
        org.w3c.dom.Document w3cDoc = w3c.fromJsoup(jsoupDoc);

        NodeList elements = w3cDoc.getElementsByTagName("custom:element");
        assertEquals(1, elements.getLength());
        org.w3c.dom.Element customEl = (org.w3c.dom.Element) elements.item(0);
        assertEquals("http://example.com/custom", customEl.getNamespaceURI());
        assertEquals("1", customEl.getAttribute("id"));
    }

    // Tests nested elements with multiple namespace prefixes
    @Test
    public void testFromJsoup_nestedNamespaces_resolvesPrefixesAcrossHierarchy() {
        String html = "<root xmlns:a=\"urn:ns:a\"><parent xmlns:b=\"urn:ns:b\"><a:child/><b:child/></parent><a:child/></root>";
        Document jsoupDoc = Jsoup.parse(html);
        W3CDom w3c = new W3CDom();
        org.w3c.dom.Document w3cDoc = w3c.fromJsoup(jsoupDoc);

        NodeList aChildren = w3cDoc.getElementsByTagName("a:child");
        assertEquals(2, aChildren.getLength());
        assertEquals("urn:ns:a", aChildren.item(0).getNamespaceURI());
        assertEquals("urn:ns:a", aChildren.item(1).getNamespaceURI());

        NodeList bChildren = w3cDoc.getElementsByTagName("b:child");
        assertEquals(1, bChildren.getLength());
        assertEquals("urn:ns:b", bChildren.item(0).getNamespaceURI());
    }

    // Tests attribute name sanitization for invalid characters
    @Test
    public void testFromJsoup_invalidAttributeCharacters_sanitizesKey() {
        String html = "<div valid-key=\"ok\" data-value=\"123\">Content</div>";
        Document jsoupDoc = Jsoup.parse(html);
        W3CDom w3c = new W3CDom();
        org.w3c.dom.Document w3cDoc = w3c.fromJsoup(jsoupDoc);

        org.w3c.dom.Element div = (org.w3c.dom.Element) w3cDoc.getElementsByTagName("div").item(0);
        assertEquals("ok", div.getAttribute("valid-key"));
        assertEquals("123", div.getAttribute("data-value"));
    }

    // Tests explicit convert method with pre-created Document
    @Test
    public void testConvert_customDocument_populatesExistingDocument() throws Exception {
        String html = "<html><head></head><body><span id=\"s1\">Hello</span></body></html>";
        Document jsoupDoc = Jsoup.parse(html);
        W3CDom w3c = new W3CDom();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        org.w3c.dom.Document customDoc = factory.newDocumentBuilder().newDocument();

        w3c.convert(jsoupDoc, customDoc);

        assertEquals("html", customDoc.getDocumentElement().getTagName());
        NodeList spans = customDoc.getElementsByTagName("span");
        assertEquals(1, spans.getLength());
        assertEquals("s1", ((org.w3c.dom.Element) spans.item(0)).getAttribute("id"));
    }

    // Tests asString serializing W3C Document to XML string
    @Test
    public void testAsString_validDocument_returnsSerializedXml() {
        String html = "<html><head><title>Doc</title></head><body><p>Content</p></body></html>";
        Document jsoupDoc = Jsoup.parse(html);
        W3CDom w3c = new W3CDom();
        org.w3c.dom.Document w3cDoc = w3c.fromJsoup(jsoupDoc);

        String result = w3c.asString(w3cDoc);

        assertNotNull(result);
        assertTrue(result.contains("<title>Doc</title>"));
        assertTrue(result.contains("<p>Content</p>"));
    }
}