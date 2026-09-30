package org.jsoup.helper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Test;
import org.w3c.dom.NodeList;

import static org.junit.Assert.*;

/**
 * JUnit 4 test class for W3CDom (Defects4J bug 54b).
 */
public class W3CDomTest {

    // Test normal HTML to W3C conversion
    @Test
    public void testFromJsoup_normalHtml_returnsW3cDocument() {
        Document jsoupDoc = Jsoup.parse("<html><head></head><body>Hello</body></html>");
        W3CDom w3cDom = new W3CDom();
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        assertNotNull(w3cDoc);
        assertEquals("html", w3cDoc.getDocumentElement().getTagName());
        assertEquals("Hello", w3cDoc.getDocumentElement().getElementsByTagName("body").item(0).getTextContent());
    }

    // Test document with namespace declaration (xmlns)
    @Test
    public void testConvert_withNamespaceDeclaration_namespacePreserved() {
        Document jsoupDoc = Jsoup.parse("<html xmlns='http://www.w3.org/1999/xhtml'><body>Text</body></html>");
        W3CDom w3cDom = new W3CDom();
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        assertEquals("http://www.w3.org/1999/xhtml", w3cDoc.getDocumentElement().getNamespaceURI());
    }

    // Test document with prefixed namespace (xmlns:prefix)
    @Test
    public void testConvert_withPrefixedNamespace_prefixMapped() {
        Document jsoupDoc = Jsoup.parse("<html xmlns:svg='http://www.w3.org/2000/svg'><svg:svg/></html>");
        W3CDom w3cDom = new W3CDom();
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        org.w3c.dom.Element svgEl = (org.w3c.dom.Element) w3cDoc.getDocumentElement().getElementsByTagName("svg:svg").item(0);
        assertEquals("http://www.w3.org/2000/svg", svgEl.getNamespaceURI());
    }

    // Test attribute with invalid XML character (should be cleaned by copyAttributes)
    @Test
    public void testConvert_attributeWithInvalidChars_keySanitized() {
        Document jsoupDoc = Jsoup.parse("<div test@attr=\"value\">Text</div>");
        W3CDom w3cDom = new W3CDom();
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        org.w3c.dom.Element div = (org.w3c.dom.Element) w3cDoc.getDocumentElement().getElementsByTagName("div").item(0);
        assertNotNull(div.getAttribute("testattr")); // '@' removed
        assertEquals("value", div.getAttribute("testattr"));
    }

    // Test attribute with colon (valid XML name) should be preserved
    @Test
    public void testConvert_attributeWithColon_keyPreserved() {
        Document jsoupDoc = Jsoup.parse("<div xml:lang=\"en\">Text</div>");
        W3CDom w3cDom = new W3CDom();
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        org.w3c.dom.Element div = (org.w3c.dom.Element) w3cDoc.getDocumentElement().getElementsByTagName("div").item(0);
        assertEquals("en", div.getAttribute("xml:lang"));
    }

    // Test document location (URI) is set
    @Test
    public void testConvert_documentWithLocation_uriSet() {
        Document jsoupDoc = Jsoup.parse("<html></html>");
        jsoupDoc.setBaseUri("http://example.com");  // Fix: use setBaseUri instead of setLocation
        W3CDom w3cDom = new W3CDom();
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        assertEquals("http://example.com", w3cDoc.getDocumentURI());
    }

    // Test null input to fromJsoup throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testFromJsoup_nullInput_throwsIllegalArgumentException() {
        W3CDom w3cDom = new W3CDom();
        w3cDom.fromJsoup(null);
    }

    // Test asString produces non-empty string
    @Test
    public void testAsString_w3cDocument_returnsString() {
        Document jsoupDoc = Jsoup.parse("<html><body>Test</body></html>");
        W3CDom w3cDom = new W3CDom();
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        String result = w3cDom.asString(w3cDoc);
        assertNotNull(result);
        assertTrue(result.contains("Test"));
    }

    // Test conversion of comment
    @Test
    public void testConvert_commentNode_createsComment() {
        Document jsoupDoc = Jsoup.parse("<html><!-- comment --></html>");
        W3CDom w3cDom = new W3CDom();
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        NodeList comments = w3cDoc.getElementsByTagName("html").item(0).getChildNodes();
        boolean foundComment = false;
        for (int i = 0; i < comments.getLength(); i++) {
            if (comments.item(i).getNodeType() == org.w3c.dom.Node.COMMENT_NODE) {
                foundComment = true;
                assertEquals(" comment ", comments.item(i).getNodeValue());
                break;
            }
        }
        assertTrue("Comment should be present", foundComment);
    }

    // Test conversion of text node
    @Test
    public void testConvert_textNode_createsTextNode() {
        Document jsoupDoc = Jsoup.parse("<p>Hello</p>");
        W3CDom w3cDom = new W3CDom();
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        org.w3c.dom.Element p = (org.w3c.dom.Element) w3cDoc.getDocumentElement().getElementsByTagName("p").item(0);
        assertEquals("Hello", p.getTextContent());
    }

    // Test conversion of data node (e.g., <script>)
    @Test
    public void testConvert_dataNode_createsTextNode() {
        Document jsoupDoc = Jsoup.parse("<script>var x = 1;</script>");
        W3CDom w3cDom = new W3CDom();
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        org.w3c.dom.Element script = (org.w3c.dom.Element) w3cDoc.getDocumentElement().getElementsByTagName("script").item(0);
        assertEquals("var x = 1;", script.getTextContent());
    }

    // Test nested elements and proper tail handling (undescend)
    @Test
    public void testConvert_nestedElements_structurePreserved() {
        Document jsoupDoc = Jsoup.parse("<ul><li>A</li><li>B</li></ul>");
        W3CDom w3cDom = new W3CDom();
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        org.w3c.dom.Element ul = (org.w3c.dom.Element) w3cDoc.getDocumentElement().getElementsByTagName("ul").item(0);
        assertEquals(2, ul.getElementsByTagName("li").getLength());
    }

    // Test element with empty namespace (no xmlns) -> namespace should be null
    @Test
    public void testConvert_elementWithoutNamespace_namespaceNull() {
        Document jsoupDoc = Jsoup.parse("<div/>");
        W3CDom w3cDom = new W3CDom();
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        org.w3c.dom.Element div = (org.w3c.dom.Element) w3cDoc.getDocumentElement();
        assertNull(div.getNamespaceURI());
    }

    // Test element with prefix but no namespace declaration (prefix should be empty)
    @Test
    public void testConvert_prefixedElementWithoutDeclaration_namespaceNull() {
        Document jsoupDoc = Jsoup.parse("<html:body/>");
        W3CDom w3cDom = new W3CDom();
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        org.w3c.dom.Element body = (org.w3c.dom.Element) w3cDoc.getDocumentElement();
        // tagName will be "html:body", but namespace not defined -> null
        assertNull(body.getNamespaceURI());
    }

    // Test multiple namespace declarations (xmlns and xmlns:prefix)
    @Test
    public void testConvert_multipleNamespaces_allMapped() {
        Document jsoupDoc = Jsoup.parse("<html xmlns='http://www.w3.org/1999/xhtml' xmlns:svg='http://www.w3.org/2000/svg'>" +
                "<body><svg:svg/></body></html>");
        W3CDom w3cDom = new W3CDom();
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        org.w3c.dom.Element svg = (org.w3c.dom.Element) w3cDoc.getDocumentElement().getElementsByTagName("svg:svg").item(0);
        assertEquals("http://www.w3.org/2000/svg", svg.getNamespaceURI());
    }

    // Test asString with empty document
    @Test
    public void testAsString_emptyDocument_returnsXmlDeclaration() {
        Document jsoupDoc = Jsoup.parse("<html></html>");
        W3CDom w3cDom = new W3CDom();
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        String result = w3cDom.asString(w3cDoc);
        assertTrue(result.contains("<?xml"));
        assertTrue(result.contains("<html"));
    }

    // Test that copyAttributes does not fail on empty attributes
    @Test
    public void testConvert_elementWithNoAttributes_noException() {
        Document jsoupDoc = Jsoup.parse("<div></div>");
        W3CDom w3cDom = new W3CDom();
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        org.w3c.dom.Element div = (org.w3c.dom.Element) w3cDoc.getDocumentElement();
        assertEquals(0, div.getAttributes().getLength());
    }
}