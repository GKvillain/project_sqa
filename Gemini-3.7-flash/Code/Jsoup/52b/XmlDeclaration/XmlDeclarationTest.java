package org.jsoup.nodes;

import org.junit.Test;

import static org.junit.Assert.*;

public class XmlDeclarationTest {

    // Tests null name passed to constructor throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullName_throwsException() {
        new XmlDeclaration(null, "http://example.com", false);
    }

    // Tests nodeName returns "#declaration"
    @Test
    public void testNodeName_default_returnsDeclarationNodeName() {
        XmlDeclaration decl = new XmlDeclaration("xml", "http://example.com", false);
        assertEquals("#declaration", decl.nodeName());
    }

    // Tests name getter returns the declaration name
    @Test
    public void testName_validName_returnsCorrectName() {
        XmlDeclaration decl = new XmlDeclaration("xml", "http://example.com", false);
        assertEquals("xml", decl.name());
    }

    // Tests getWholeDeclaration when name is not "xml"
    @Test
    public void testGetWholeDeclaration_nonXmlName_returnsName() {
        XmlDeclaration decl = new XmlDeclaration("somethingElse", "http://example.com", false);
        assertEquals("somethingElse", decl.getWholeDeclaration());
    }

    // Tests getWholeDeclaration when name is "xml" without attributes
    @Test
    public void testGetWholeDeclaration_xmlWithoutAttributes_returnsXml() {
        XmlDeclaration decl = new XmlDeclaration("xml", "http://example.com", false);
        assertEquals("xml", decl.getWholeDeclaration());
    }

    // Tests getWholeDeclaration with version attribute only
    @Test
    public void testGetWholeDeclaration_xmlWithVersionOnly_returnsFormattedDeclaration() {
        XmlDeclaration decl = new XmlDeclaration("xml", "http://example.com", false);
        decl.attr("version", "1.0");
        assertEquals("xml version=\"1.0\"", decl.getWholeDeclaration());
    }

    // Tests getWholeDeclaration with encoding attribute only
    @Test
    public void testGetWholeDeclaration_xmlWithEncodingOnly_returnsFormattedDeclaration() {
        XmlDeclaration decl = new XmlDeclaration("xml", "http://example.com", false);
        decl.attr("encoding", "UTF-8");
        assertEquals("xml encoding=\"UTF-8\"", decl.getWholeDeclaration());
    }

    // Tests getWholeDeclaration with both version and encoding attributes
    @Test
    public void testGetWholeDeclaration_xmlWithVersionAndEncoding_returnsFormattedDeclaration() {
        XmlDeclaration decl = new XmlDeclaration("xml", "http://example.com", false);
        decl.attr("version", "1.0");
        decl.attr("encoding", "UTF-8");
        assertEquals("xml version=\"1.0\" encoding=\"UTF-8\"", decl.getWholeDeclaration());
    }

    // Tests outerHtml for standard XML declaration (isProcessingInstruction = false)
    @Test
    public void testOuterHtml_declarationNotProcessingInstruction_formattedWithQuestionMark() {
        XmlDeclaration decl = new XmlDeclaration("xml", "http://example.com", false);
        assertEquals("<?xml>", decl.outerHtml());
    }

    // Tests outerHtml for processing instruction (isProcessingInstruction = true)
    @Test
    public void testOuterHtml_processingInstruction_formattedWithExclamationMark() {
        XmlDeclaration decl = new XmlDeclaration("DOCTYPE html", "http://example.com", true);
        assertEquals("<!DOCTYPE html>", decl.outerHtml());
    }

    // Tests outerHtml when declaration has attributes
    @Test
    public void testOuterHtml_xmlWithAttributes_rendersCorrectXmlTag() {
        XmlDeclaration decl = new XmlDeclaration("xml", "http://example.com", false);
        decl.attr("version", "1.0");
        decl.attr("encoding", "UTF-8");
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\">", decl.outerHtml());
    }

    // Tests toString returns the same result as outerHtml
    @Test
    public void testToString_validDeclaration_equalsOuterHtml() {
        XmlDeclaration decl = new XmlDeclaration("xml", "http://example.com", false);
        decl.attr("version", "1.0");
        assertEquals(decl.outerHtml(), decl.toString());
    }

    // Tests baseUri getter returns the correct base URI
    @Test
    public void testBaseUri_validBaseUri_returnsCorrectBaseUri() {
        XmlDeclaration decl = new XmlDeclaration("xml", "http://example.com/test", false);
        assertEquals("http://example.com/test", decl.baseUri());
    }

    // Tests outerHtml for processing instruction with attributes
    @Test
    public void testOuterHtml_processingInstructionWithAttributes_rendersExclamationMark() {
        XmlDeclaration decl = new XmlDeclaration("xml", "http://example.com", true);
        decl.attr("version", "1.0");
        assertEquals("<!xml version=\"1.0\">", decl.outerHtml());
    }

    // Tests outerHtml for non-xml declaration
    @Test
    public void testOuterHtml_nonXmlDeclaration_rendersCorrectTag() {
        XmlDeclaration decl = new XmlDeclaration("custom-decl", "http://example.com", false);
        assertEquals("<?custom-decl>", decl.outerHtml());
    }

    // Tests cloning creates an independent copy with identical attributes and output
    @Test
    public void testClone_clonedDeclaration_hasIdenticalProperties() {
        XmlDeclaration decl = new XmlDeclaration("xml", "http://example.com", false);
        decl.attr("version", "1.0");
        decl.attr("encoding", "UTF-8");

        XmlDeclaration clone = (XmlDeclaration) decl.clone();
        assertNotSame(decl, clone);
        assertEquals(decl.name(), clone.name());
        assertEquals(decl.baseUri(), clone.baseUri());
        assertEquals(decl.getWholeDeclaration(), clone.getWholeDeclaration());
        assertEquals(decl.outerHtml(), clone.outerHtml());
    }

    // Tests getWholeDeclaration with custom attributes on non-xml declaration
    @Test
    public void testGetWholeDeclaration_nonXmlWithAttributes_returnsName() {
        XmlDeclaration decl = new XmlDeclaration("custom", "http://example.com", false);
        decl.attr("key", "value");
        assertEquals("custom", decl.getWholeDeclaration());
    }
}