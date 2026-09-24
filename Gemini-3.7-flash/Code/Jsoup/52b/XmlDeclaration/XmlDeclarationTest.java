package org.jsoup.nodes;

import org.junit.Test;

import static org.junit.Assert.*;

public class XmlDeclarationTest {

    // Tests constructor with null name throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullName_throwsException() {
        new XmlDeclaration(null, "http://example.com", false);
    }

    // Tests nodeName returns correct constant value
    @Test
    public void testNodeName_normal_returnsDeclarationString() {
        XmlDeclaration decl = new XmlDeclaration("xml", "http://example.com", false);
        assertEquals("#declaration", decl.nodeName());
    }

    // Tests name getter returns the name initialized with
    @Test
    public void testName_validName_returnsName() {
        XmlDeclaration decl = new XmlDeclaration("custom-decl", "http://example.com", false);
        assertEquals("custom-decl", decl.name());
    }

    // Tests getWholeDeclaration when name is not "xml"
    @Test
    public void testGetWholeDeclaration_nonXmlName_returnsNameOnly() {
        XmlDeclaration decl = new XmlDeclaration("custom", "http://example.com", false);
        decl.attr("version", "1.0");
        decl.attr("encoding", "UTF-8");
        assertEquals("custom", decl.getWholeDeclaration());
    }

    // Tests getWholeDeclaration when name is "xml" but has no attributes
    @Test
    public void testGetWholeDeclaration_xmlWithNoAttributes_returnsXml() {
        XmlDeclaration decl = new XmlDeclaration("xml", "http://example.com", false);
        assertEquals("xml", decl.getWholeDeclaration());
    }

    // Tests getWholeDeclaration with version and encoding attributes
    @Test
    public void testGetWholeDeclaration_xmlWithVersionAndEncoding_returnsFormattedDeclaration() {
        XmlDeclaration decl = new XmlDeclaration("xml", "http://example.com", false);
        decl.attr("version", "1.0");
        decl.attr("encoding", "UTF-8");
        assertEquals("xml version=\"1.0\" encoding=\"UTF-8\"", decl.getWholeDeclaration());
    }

    // Tests getWholeDeclaration with only one attribute (version)
    @Test
    public void testGetWholeDeclaration_xmlWithVersionOnly_returnsFormattedDeclaration() {
        XmlDeclaration decl = new XmlDeclaration("xml", "http://example.com", false);
        decl.attr("version", "1.0");
        // Tests condition where attributes size is 1
        String whole = decl.getWholeDeclaration();
        assertNotNull(whole);
    }

    // Tests getWholeDeclaration with only one attribute (encoding)
    @Test
    public void testGetWholeDeclaration_xmlWithEncodingOnly_returnsFormattedDeclaration() {
        XmlDeclaration decl = new XmlDeclaration("xml", "http://example.com", false);
        decl.attr("encoding", "UTF-8");
        String whole = decl.getWholeDeclaration();
        assertNotNull(whole);
    }

    // Tests getWholeDeclaration with encoding and other attributes without version
    @Test
    public void testGetWholeDeclaration_xmlWithEncodingAndOtherAttr_returnsFormattedDeclaration() {
        XmlDeclaration decl = new XmlDeclaration("xml", "http://example.com", false);
        decl.attr("encoding", "UTF-8");
        decl.attr("standalone", "yes");
        assertEquals("xml encoding=\"UTF-8\"", decl.getWholeDeclaration());
    }

    // Tests outerHtml for XML declaration (!isProcessingInstruction -> <?...>)
    @Test
    public void testOuterHtml_isDeclaration_rendersQuestionMark() {
        XmlDeclaration decl = new XmlDeclaration("xml", "http://example.com", false);
        decl.attr("version", "1.0");
        decl.attr("encoding", "UTF-8");
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\">", decl.outerHtml());
    }

    // Tests outerHtml for processing instruction (isProcessingInstruction -> <! ... >)
    @Test
    public void testOuterHtml_isProcessingInstruction_rendersExclamationMark() {
        XmlDeclaration decl = new XmlDeclaration("xml", "http://example.com", true);
        decl.attr("version", "1.0");
        decl.attr("encoding", "UTF-8");
        assertEquals("<!xml version=\"1.0\" encoding=\"UTF-8\">", decl.outerHtml());
    }

    // Tests toString returns same output as outerHtml
    @Test
    public void testToString_validDeclaration_returnsSameAsOuterHtml() {
        XmlDeclaration decl = new XmlDeclaration("xml", "http://example.com", false);
        assertEquals(decl.outerHtml(), decl.toString());
    }
}