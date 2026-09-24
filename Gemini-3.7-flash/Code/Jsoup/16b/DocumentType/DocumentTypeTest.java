package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

public class DocumentTypeTest {

    // Tests nodeName implementation
    @Test
    public void testNodeName_always_returnsDoctype() {
        DocumentType docType = new DocumentType("html", "", "", "");
        assertEquals("#doctype", docType.nodeName());
    }

    // Tests constructor setting attributes correctly
    @Test
    public void testConstructor_validParameters_setsAttributesCorrectly() {
        DocumentType docType = new DocumentType("html", "-//W3C//DTD XHTML 1.0 Strict//EN", "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd", "http://example.com");
        assertEquals("html", docType.attr("name"));
        assertEquals("-//W3C//DTD XHTML 1.0 Strict//EN", docType.attr("publicId"));
        assertEquals("http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd", docType.attr("systemId"));
        assertEquals("http://example.com", docType.baseUri());
    }

    // Tests standard HTML5 doctype without public or system IDs
    @Test
    public void testOuterHtml_html5SimpleDoctype_rendersCorrectHtml() {
        DocumentType docType = new DocumentType("html", "", "", "");
        assertEquals("<!DOCTYPE html>", docType.outerHtml());
    }

    // Tests custom doctype name (e.g. XML / SVG / lowercase / uppercase)
    @Test
    public void testOuterHtml_customName_rendersDoctypeWithCustomName() {
        DocumentType docType = new DocumentType("svg", "", "", "");
        assertEquals("<!DOCTYPE svg>", docType.outerHtml());
    }

    // Tests doctype with both publicId and systemId
    @Test
    public void testOuterHtml_publicAndSystemIdentifiers_rendersFullDoctype() {
        DocumentType docType = new DocumentType("html", "-//W3C//DTD HTML 4.01//EN", "http://www.w3.org/TR/html4/strict.dtd", "");
        assertEquals("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">", docType.outerHtml());
    }

    // Tests doctype with publicId only
    @Test
    public void testOuterHtml_publicIdOnly_rendersDoctypeWithPublicId() {
        DocumentType docType = new DocumentType("html", "-//W3C//DTD HTML 4.01//EN", "", "");
        assertEquals("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\">", docType.outerHtml());
    }

    // Tests doctype with systemId only
    @Test
    public void testOuterHtml_systemIdOnly_rendersDoctypeWithSystemId() {
        DocumentType docType = new DocumentType("html", "", "http://www.ibm.com/data/dtd/v11/ibmxhtml1-transitional.dtd", "");
        assertEquals("<!DOCTYPE html SYSTEM \"http://www.ibm.com/data/dtd/v11/ibmxhtml1-transitional.dtd\">", docType.outerHtml());
    }

    // Tests blank/whitespace public and system IDs
    @Test
    public void testOuterHtml_whitespacePublicAndSystemIds_rendersDoctypeWithoutIds() {
        DocumentType docType = new DocumentType("html", "   ", "   ", "");
        assertEquals("<!DOCTYPE html>", docType.outerHtml());
    }

    // Tests outerHtmlHead directly with StringBuilder
    @Test
    public void testOuterHtmlHead_directCall_appendsCorrectDoctype() {
        DocumentType docType = new DocumentType("html", "pub", "sys", "");
        StringBuilder accum = new StringBuilder();
        docType.outerHtmlHead(accum, 0, new Document.OutputSettings());
        assertEquals("<!DOCTYPE html PUBLIC \"pub\" \"sys\">", accum.toString());
    }

    // Tests outerHtmlTail to ensure no characters are appended
    @Test
    public void testOuterHtmlTail_directCall_doesNotModifyAccumulator() {
        DocumentType docType = new DocumentType("html", "", "", "");
        StringBuilder accum = new StringBuilder();
        docType.outerHtmlTail(accum, 0, new Document.OutputSettings());
        assertEquals(0, accum.length());
    }
}