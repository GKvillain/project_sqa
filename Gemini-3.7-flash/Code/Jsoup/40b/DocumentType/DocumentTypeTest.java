package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

public class DocumentTypeTest {

    // Tests nodeName method returns correct value
    @Test
    public void testNodeName_default_returnsDoctypeNodeName() {
        DocumentType documentType = new DocumentType("html", "", "", "");
        assertEquals("#doctype", documentType.nodeName());
    }

    // Tests doctype creation with only name
    @Test
    public void testOuterHtml_onlyName_returnsSimpleDoctype() {
        DocumentType documentType = new DocumentType("html", "", "", "");
        assertEquals("<!DOCTYPE html>", documentType.outerHtml());
    }

    // Tests doctype with name, publicId, and systemId
    @Test
    public void testOuterHtml_withPublicAndSystemId_returnsFullDoctype() {
        DocumentType documentType = new DocumentType(
                "html",
                "-//W3C//DTD XHTML 1.0 Strict//EN",
                "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd",
                ""
        );
        assertEquals(
                "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">",
                documentType.outerHtml()
        );
    }

    // Tests doctype with publicId but blank systemId
    @Test
    public void testOuterHtml_withPublicIdOnly_returnsPublicDoctype() {
        DocumentType documentType = new DocumentType("html", "-//W3C//DTD HTML 4.01//EN", "", "");
        assertEquals("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\">", documentType.outerHtml());
    }

    // Tests doctype with systemId but blank publicId
    @Test
    public void testOuterHtml_withSystemIdOnly_returnsSystemDoctype() {
        DocumentType documentType = new DocumentType("html", "", "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd", "");
        assertEquals("<!DOCTYPE html \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">", documentType.outerHtml());
    }

    // Tests attribute storage and retrieval
    @Test
    public void testAttr_validValues_returnsStoredAttributes() {
        DocumentType documentType = new DocumentType("html", "pubId", "sysId", "http://example.com");
        assertEquals("html", documentType.attr("name"));
        assertEquals("pubId", documentType.attr("publicId"));
        assertEquals("sysId", documentType.attr("systemId"));
        assertEquals("http://example.com", documentType.baseUri());
    }

    // Tests outerHtml output with base URI
    @Test
    public void testOuterHtml_withBaseUri_ignoresBaseUriInDoctypeOutput() {
        DocumentType documentType = new DocumentType("html", "", "", "http://example.com");
        assertEquals("<!DOCTYPE html>", documentType.outerHtml());
    }

    // Tests exception when name is null
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullName_throwsException() {
        new DocumentType(null, "public", "system", "");
    }

    // Tests exception when name is empty
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_emptyName_throwsException() {
        new DocumentType("", "public", "system", "");
    }

    // Tests outerHtml with blank attributes containing whitespace
    @Test
    public void testOuterHtml_blankWhitespaceIds_ignoresBlankIds() {
        DocumentType documentType = new DocumentType("html", "   ", "   ", "");
        assertEquals("<!DOCTYPE html>", documentType.outerHtml());
    }
}