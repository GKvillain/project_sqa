package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * JUnit 4 test class for DocumentType, targeting Defects4J bug 16b.
 * The bug is in outerHtmlHead: when publicId is blank and systemId is not blank,
 * the output incorrectly omits the space before systemId and does not include
 * the closing quote. This test suite aims to detect that defect.
 */
public class DocumentTypeTest {

    // Tests normal case with both publicId and systemId blank
    @Test
    public void testOuterHtmlHead_bothBlank_returnsMinimalDoctype() {
        DocumentType doctype = new DocumentType("html", "", "", "http://example.com");
        StringBuilder accum = new StringBuilder();
        doctype.outerHtmlHead(accum, 0, new Document.OutputSettings());
        assertEquals("<!DOCTYPE html>", accum.toString());
    }

    // Tests normal case with publicId set, systemId blank
    @Test
    public void testOuterHtmlHead_publicIdOnly_publicIdIncluded() {
        DocumentType doctype = new DocumentType("html", "-//W3C//DTD XHTML 1.0 Strict//EN", "", "http://example.com");
        StringBuilder accum = new StringBuilder();
        doctype.outerHtmlHead(accum, 0, new Document.OutputSettings());
        assertEquals("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\">", accum.toString());
    }

    // Tests normal case with publicId blank, systemId set
    @Test
    public void testOuterHtmlHead_systemIdOnly_systemIdIncluded() {
        DocumentType doctype = new DocumentType("html", "", "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd", "http://example.com");
        StringBuilder accum = new StringBuilder();
        doctype.outerHtmlHead(accum, 0, new Document.OutputSettings());
        // Expected: " <systemId>" with closing quote, but bug produces " <systemId>\""
        assertEquals("<!DOCTYPE html \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">", accum.toString());
    }

    // Tests normal case with both publicId and systemId set
    @Test
    public void testOuterHtmlHead_bothSet_bothIncluded() {
        DocumentType doctype = new DocumentType("html", "-//W3C//DTD XHTML 1.0 Strict//EN", "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd", "http://example.com");
        StringBuilder accum = new StringBuilder();
        doctype.outerHtmlHead(accum, 0, new Document.OutputSettings());
        assertEquals("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">", accum.toString());
    }

    // Tests edge case: publicId is blank but not empty (e.g., whitespace)
    // This triggers the true branch of the first if and false branch of the second if
    @Test
    public void testOuterHtmlHead_publicIdBlankWhitespace_publicIdNotIncluded() {
        DocumentType doctype = new DocumentType("html", " ", "http://example.com", "http://example.com");
        StringBuilder accum = new StringBuilder();
        doctype.outerHtmlHead(accum, 0, new Document.OutputSettings());
        assertEquals("<!DOCTYPE html \"http://example.com\">", accum.toString());
    }

    // Tests edge case: systemId is blank but not empty (e.g., whitespace)
    @Test
    public void testOuterHtmlHead_systemIdBlankWhitespace_systemIdNotIncluded() {
        DocumentType doctype = new DocumentType("html", "-//W3C//DTD XHTML 1.0 Strict//EN", " ", "http://example.com");
        StringBuilder accum = new StringBuilder();
        doctype.outerHtmlHead(accum, 0, new Document.OutputSettings());
        assertEquals("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\">", accum.toString());
    }

    // Tests null publicId (should be treated as blank by StringUtil.isBlank)
    @Test
    public void testOuterHtmlHead_nullPublicId_publicIdNotIncluded() {
        DocumentType doctype = new DocumentType("html", null, "http://example.com", "http://example.com");
        StringBuilder accum = new StringBuilder();
        doctype.outerHtmlHead(accum, 0, new Document.OutputSettings());
        assertEquals("<!DOCTYPE html \"http://example.com\">", accum.toString());
    }

    // Tests null systemId (should be treated as blank by StringUtil.isBlank)
    @Test
    public void testOuterHtmlHead_nullSystemId_systemIdNotIncluded() {
        DocumentType doctype = new DocumentType("html", "-//W3C//DTD XHTML 1.0 Strict//EN", null, "http://example.com");
        StringBuilder accum = new StringBuilder();
        doctype.outerHtmlHead(accum, 0, new Document.OutputSettings());
        assertEquals("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\">", accum.toString());
    }

    // Tests nodeName method
    @Test
    public void testNodeName_always_returnsDoctype() {
        DocumentType doctype = new DocumentType("html", "", "", "http://example.com");
        assertEquals("#doctype", doctype.nodeName());
    }

    // Tests outerHtmlTail method (should do nothing)
    @Test
    public void testOuterHtmlTail_anyInput_appendsNothing() {
        DocumentType doctype = new DocumentType("html", "", "", "http://example.com");
        StringBuilder accum = new StringBuilder("prefix");
        doctype.outerHtmlTail(accum, 0, new Document.OutputSettings());
        assertEquals("prefix", accum.toString());
    }

    // Tests constructor sets attributes correctly via attr method
    @Test
    public void testConstructor_validInput_setsAttributes() {
        DocumentType doctype = new DocumentType("html", "pub", "sys", "http://example.com");
        assertEquals("html", doctype.attr("name"));
        assertEquals("pub", doctype.attr("publicId"));
        assertEquals("sys", doctype.attr("systemId"));
    }

    // Tests constructor with null name (should not throw exception, just set attr)
    @Test
    public void testConstructor_nullName_setsAttrNull() {
        DocumentType doctype = new DocumentType(null, "", "", "http://example.com");
        assertEquals("null", doctype.attr("name")); // attr converts null to "null" string
    }

    // Tests outerHtmlHead with non-default output settings (ensuring no impact)
    @Test
    public void testOuterHtmlHead_customOutputSettings_doesNotChange() {
        DocumentType doctype = new DocumentType("html", "pub", "sys", "http://example.com");
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.indentAmount(4);
        StringBuilder accum = new StringBuilder();
        doctype.outerHtmlHead(accum, 0, settings);
        assertEquals("<!DOCTYPE html PUBLIC \"pub\" \"sys\">", accum.toString());
    }
}