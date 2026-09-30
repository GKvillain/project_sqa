package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

public class DocumentTypeTest {

    // Tests constructor and attribute getters with valid inputs
    @Test
    public void testConstructor_validInputs_setsAttributesCorrectly() {
        DocumentType documentType = new DocumentType("html", "publicId", "systemId", "http://example.com");
        assertEquals("html", documentType.attr("name"));
        assertEquals("publicId", documentType.attr("publicId"));
        assertEquals("systemId", documentType.attr("systemId"));
        assertEquals("http://example.com", documentType.baseUri());
    }

    // Tests nodeName returns "#doctype"
    @Test
    public void testNodeName_default_returnsDoctypeNodeName() {
        DocumentType documentType = new DocumentType("html", "", "", "");
        assertEquals("#doctype", documentType.nodeName());
    }

    // Tests outerHtml for standard HTML5 doctype
    @Test
    public void testOuterHtml_nameOnly_generatesHtml5Doctype() {
        DocumentType documentType = new DocumentType("html", "", "", "");
        assertEquals("<!DOCTYPE html>", documentType.outerHtml());
    }

    // Tests outerHtml with both publicId and systemId present
    @Test
    public void testOuterHtml_publicAndSystemDoctype_generatesCorrectString() {
        DocumentType documentType = new DocumentType("html", "-//W3C//DTD HTML 4.01//EN", "http://www.w3.org/TR/html4/strict.dtd", "");
        assertEquals("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">", documentType.outerHtml());
    }

    // Tests outerHtml with publicId present and systemId empty
    @Test
    public void testOuterHtml_publicIdOnly_generatesCorrectString() {
        DocumentType documentType = new DocumentType("html", "-//W3C//DTD HTML 4.01//EN", "", "");
        assertEquals("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\">", documentType.outerHtml());
    }

    // Tests outerHtml with systemId present and publicId empty
    @Test
    public void testOuterHtml_systemIdOnly_generatesCorrectString() {
        DocumentType documentType = new DocumentType("html", "", "http://www.w3.org/TR/html4/strict.dtd", "");
        assertEquals("<!DOCTYPE html \"http://www.w3.org/TR/html4/strict.dtd\">", documentType.outerHtml());
    }

    // Tests outerHtml with whitespace-only publicId and systemId
    @Test
    public void testOuterHtml_blankPublicAndSystemIds_generatesOnlyName() {
        DocumentType documentType = new DocumentType("html", "   ", "   ", "");
        assertEquals("<!DOCTYPE html>", documentType.outerHtml());
    }

    // Tests exception path when name is null
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullName_throwsIllegalArgumentException() {
        new DocumentType(null, "publicId", "systemId", "");
    }

    // Tests exception path when name is empty string
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_emptyName_throwsIllegalArgumentException() {
        new DocumentType("", "publicId", "systemId", "");
    }

    // Tests outerHtml appending to an existing StringBuilder
    @Test
    public void testOuterHtmlHead_existingAccumulator_appendsCorrectly() {
        DocumentType documentType = new DocumentType("html", "", "", "");
        StringBuilder sb = new StringBuilder("prefix_");
        documentType.outerHtmlHead(sb, 0, new Document("").outputSettings());
        assertEquals("prefix_<!DOCTYPE html>", sb.toString());
    }

    // Tests outerHtmlTail does not modify StringBuilder
    @Test
    public void testOuterHtmlTail_noop_doesNotModifyAccumulator() {
        DocumentType documentType = new DocumentType("html", "", "", "");
        StringBuilder sb = new StringBuilder("prefix");
        documentType.outerHtmlTail(sb, 0, new Document("").outputSettings());
        assertEquals("prefix", sb.toString());
    }
}