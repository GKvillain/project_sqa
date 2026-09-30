package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

public class DocumentTypeTest {

    // Tests nodeName returns "#doctype"
    @Test
    public void testNodeName_default_returnsDoctypeHash() {
        DocumentType documentType = new DocumentType("html", "", "", "");
        assertEquals("#doctype", documentType.nodeName());
    }

    // Tests constructor and attribute getters
    @Test
    public void testConstructor_validParameters_setsAttributesCorrectly() {
        DocumentType documentType = new DocumentType("html", "publicIdVal", "systemIdVal", "http://example.com");
        assertEquals("html", documentType.attr("name"));
        assertEquals("publicIdVal", documentType.attr("publicId"));
        assertEquals("systemIdVal", documentType.attr("systemId"));
        assertEquals("http://example.com", documentType.baseUri());
    }

    // Tests outerHtml for standard HTML5 doctype
    @Test
    public void testOuterHtml_simpleHtml5_returnsExpectedDoctype() {
        DocumentType documentType = new DocumentType("html", "", "", "");
        assertEquals("<!DOCTYPE html>", documentType.outerHtml());
    }

    // Tests outerHtml when name is not "html"
    @Test
    public void testOuterHtml_customName_returnsExpectedDoctypeWithName() {
        DocumentType documentType = new DocumentType("svg", "", "", "");
        assertEquals("<!DOCTYPE svg>", documentType.outerHtml());
    }

    // Tests outerHtml with publicId and systemId
    @Test
    public void testOuterHtml_publicAndSystemId_returnsFormattedDoctype() {
        DocumentType documentType = new DocumentType(
                "html",
                "-//W3C//DTD HTML 4.01//EN",
                "http://www.w3.org/TR/html4/strict.dtd",
                ""
        );
        assertEquals(
                "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">",
                documentType.outerHtml()
        );
    }

    // Tests outerHtml with only publicId (systemId is empty)
    @Test
    public void testOuterHtml_publicIdOnly_returnsFormattedDoctype() {
        DocumentType documentType = new DocumentType("html", "-//W3C//DTD HTML 4.01//EN", "", "");
        assertEquals("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\">", documentType.outerHtml());
    }

    // Tests outerHtml with only systemId (publicId is empty)
    @Test
    public void testOuterHtml_systemIdOnly_returnsFormattedDoctype() {
        DocumentType documentType = new DocumentType("html", "", "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd", "");
        assertEquals("<!DOCTYPE html \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">", documentType.outerHtml());
    }

    // Tests outerHtml with whitespace-only publicId and systemId
    @Test
    public void testOuterHtml_blankPublicAndSystemId_ignoresBlankIds() {
        DocumentType documentType = new DocumentType("html", "   ", "   ", "");
        assertEquals("<!DOCTYPE html>", documentType.outerHtml());
    }

    // Tests outerHtmlHead directly with StringBuilder
    @Test
    public void testOuterHtmlHead_accumulatesDoctypeString() {
        DocumentType documentType = new DocumentType("html", "pub", "sys", "");
        StringBuilder sb = new StringBuilder();
        documentType.outerHtmlHead(sb, 0, new Document("").outputSettings());
        assertEquals("<!DOCTYPE html PUBLIC \"pub\" \"sys\">", sb.toString());
    }

    // Tests outerHtmlTail does not append content
    @Test
    public void testOuterHtmlTail_doesNotAppendAnyContent() {
        DocumentType documentType = new DocumentType("html", "", "", "");
        StringBuilder sb = new StringBuilder("content");
        documentType.outerHtmlTail(sb, 0, new Document("").outputSettings());
        assertEquals("content", sb.toString());
    }
}