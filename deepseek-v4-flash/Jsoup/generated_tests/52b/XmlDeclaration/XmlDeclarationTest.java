package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.IOException;

public class XmlDeclarationTest {

    // Test constructor with valid input
    @Test
    public void testConstructor_validInput_createsObject() {
        XmlDeclaration decl = new XmlDeclaration("xml", "http://example.com", true);
        assertNotNull(decl);
        assertEquals("xml", decl.name());
    }

    // Test constructor with null name throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullName_throwsException() {
        new XmlDeclaration(null, "http://example.com", false);
    }

    // Test nodeName method
    @Test
    public void testNodeName_alwaysReturnsDeclaration() {
        XmlDeclaration decl = new XmlDeclaration("test", "http://example.com", false);
        assertEquals("#declaration", decl.nodeName());
    }

    // Test name method
    @Test
    public void testName_returnsCorrectName() {
        XmlDeclaration decl = new XmlDeclaration("xml", "http://example.com", true);
        assertEquals("xml", decl.name());
        XmlDeclaration decl2 = new XmlDeclaration("custom", "http://example.com", false);
        assertEquals("custom", decl2.name());
    }

    // Test getWholeDeclaration when name is not "xml" (false branch of condition)
    @Test
    public void testGetWholeDeclaration_nonXmlName_returnsName() {
        XmlDeclaration decl = new XmlDeclaration("custom", "http://example.com", true);
        assertEquals("custom", decl.getWholeDeclaration());
    }

    // Test getWholeDeclaration when name is "xml" but attributes.size() <= 1 (false branch)
    @Test
    public void testGetWholeDeclaration_xmlNoAttributes_returnsName() {
        XmlDeclaration decl = new XmlDeclaration("xml", "http://example.com", true);
        assertEquals("xml", decl.getWholeDeclaration());
    }

    // Test getWholeDeclaration when name is "xml" and exactly one attribute (size = 1) (false branch)
    @Test
    public void testGetWholeDeclaration_xmlOneAttribute_returnsName() {
        XmlDeclaration decl = new XmlDeclaration("xml", "http://example.com", true);
        decl.attributes().put("version", "1.0");
        assertEquals("xml", decl.getWholeDeclaration());
    }

    // Test getWholeDeclaration when name is "xml" and attributes.size() > 1 with version and encoding (true branch)
    @Test
    public void testGetWholeDeclaration_xmlWithVersionAndEncoding_returnsFullDeclaration() {
        XmlDeclaration decl = new XmlDeclaration("xml", "http://example.com", true);
        decl.attributes().put("version", "1.0");
        decl.attributes().put("encoding", "UTF-8");
        String expected = "xml version=\"1.0\" encoding=\"UTF-8\"";
        assertEquals(expected, decl.getWholeDeclaration());
    }

    // Test getWholeDeclaration when only version is present (true branch, encoding null)
    @Test
    public void testGetWholeDeclaration_xmlWithVersionOnly_returnsPartialDeclaration() {
        XmlDeclaration decl = new XmlDeclaration("xml", "http://example.com", true);
        decl.attributes().put("version", "1.1");
        decl.attributes().put("custom", "value"); // ensure size > 1
        String expected = "xml version=\"1.1\"";
        assertEquals(expected, decl.getWholeDeclaration());
    }

    // Test getWholeDeclaration when only encoding is present (true branch, version null)
    @Test
    public void testGetWholeDeclaration_xmlWithEncodingOnly_returnsPartialDeclaration() {
        XmlDeclaration decl = new XmlDeclaration("xml", "http://example.com", true);
        decl.attributes().put("encoding", "ISO-8859-1");
        decl.attributes().put("other", "something");
        String expected = "xml encoding=\"ISO-8859-1\"";
        assertEquals(expected, decl.getWholeDeclaration());
    }

    // Test getWholeDeclaration when attributes.size() > 1 but both version and encoding are null (true branch, both null)
    @Test
    public void testGetWholeDeclaration_xmlMultipleAttributesWithoutVersionOrEncoding_returnsName() {
        XmlDeclaration decl = new XmlDeclaration("xml", "http://example.com", true);
        decl.attributes().put("attr1", "val1");
        decl.attributes().put("attr2", "val2");
        assertEquals("xml", decl.getWholeDeclaration());
    }

    // Test outerHtmlHead with isProcessingInstruction = true (uses '!')
    @Test
    public void testOuterHtmlHead_processingInstructionTrue_usesBang() throws IOException {
        XmlDeclaration decl = new XmlDeclaration("xml", "http://example.com", true);
        decl.attributes().put("version", "1.0");
        StringBuilder sb = new StringBuilder();
        decl.outerHtmlHead(sb, 0, new Document.OutputSettings());
        String expected = "<!xml version=\"1.0\">";
        assertEquals(expected, sb.toString());
    }

    // Test outerHtmlHead with isProcessingInstruction = false (uses '?')
    @Test
    public void testOuterHtmlHead_processingInstructionFalse_usesQuestionMark() throws IOException {
        XmlDeclaration decl = new XmlDeclaration("xml", "http://example.com", false);
        decl.attributes().put("version", "1.0");
        StringBuilder sb = new StringBuilder();
        decl.outerHtmlHead(sb, 0, new Document.OutputSettings());
        String expected = "<?xml version=\"1.0\"?>";
        assertEquals(expected, sb.toString());
    }

    // Test outerHtmlTail does not append anything
    @Test
    public void testOuterHtmlTail_doesNothing() throws IOException {
        XmlDeclaration decl = new XmlDeclaration("test", "http://example.com", false);
        StringBuilder sb = new StringBuilder("prefix");
        decl.outerHtmlTail(sb, 0, new Document.OutputSettings());
        assertEquals("prefix", sb.toString());
    }

    // Test toString returns outerHtml for processing instruction false
    @Test
    public void testToString_processingInstructionFalse_returnsOuterHtml() {
        XmlDeclaration decl = new XmlDeclaration("test", "http://example.com", false);
        assertEquals("<?test?>", decl.toString());
    }

    // Test toString returns outerHtml for processing instruction true
    @Test
    public void testToString_processingInstructionTrue_returnsOuterHtml() {
        XmlDeclaration decl = new XmlDeclaration("test", "http://example.com", true);
        assertEquals("<!test>", decl.toString());
    }

    // ========== New tests for uncovered coverage ==========

    @Test
    public void testConstructorNullBaseUri_throws() {
        try {
            new XmlDeclaration("xml", null, true);
            fail("Should throw exception");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testConstructorEmptyName_allowed() {
        XmlDeclaration decl = new XmlDeclaration("", "http://example.com", false);
        assertEquals("", decl.name());
    }

    @Test
    public void testBaseUri() {
        XmlDeclaration decl = new XmlDeclaration("test", "http://example.com", false);
        assertEquals("http://example.com", decl.baseUri());
    }

    @Test
    public void testAttributesInitiallyEmpty() {
        XmlDeclaration decl = new XmlDeclaration("test", "http://example.com", false);
        assertEquals(0, decl.attributes().size());
    }

    @Test
    public void testSetAndGetAttr() {
        XmlDeclaration decl = new XmlDeclaration("xml", "http://example.com", false);
        decl.attr("version", "1.0");
        assertEquals("1.0", decl.attr("version"));
    }

    @Test
    public void testHasAttr() {
        XmlDeclaration decl = new XmlDeclaration("xml", "http://example.com", false);
        decl.attr("version", "1.0");
        assertTrue(decl.hasAttr("version"));
        assertFalse(decl.hasAttr("encoding"));
    }

    @Test
    public void testRemoveAttr() {
        XmlDeclaration decl = new XmlDeclaration("xml", "http://example.com", false);
        decl.attr("version", "1.0");
        decl.removeAttr("version");
        assertFalse(decl.hasAttr("version"));
    }

    @Test
    public void testClone() {
        XmlDeclaration decl = new XmlDeclaration("xml", "http://example.com", true);
        decl.attr("version", "1.0");
        XmlDeclaration cloned = decl.clone();
        assertNotSame(decl, cloned);
        assertEquals(decl.name(), cloned.name());
        assertEquals(decl.toString(), cloned.toString());
    }

    @Test
    public void testChildNodeSizeZero() {
        XmlDeclaration decl = new XmlDeclaration("test", "http://example.com", false);
        assertEquals(0, decl.childNodeSize());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testChildNodeOutOfBounds() {
        XmlDeclaration decl = new XmlDeclaration("test", "http://example.com", false);
        decl.childNode(0);
    }

    @Test
    public void testToStringNonXmlNameWithAttributes() {
        XmlDeclaration decl = new XmlDeclaration("custom", "http://example.com", false);
        decl.attr("attr1", "val1");
        assertEquals("<?custom?>", decl.toString());
    }

    @Test
    public void testOuterHtmlHeadXmlNoAttributesProcessingInstructionFalse() throws IOException {
        XmlDeclaration decl = new XmlDeclaration("xml", "http://example.com", false);
        StringBuilder sb = new StringBuilder();
        decl.outerHtmlHead(sb, 0, new Document.OutputSettings());
        assertEquals("<?xml?>", sb.toString());
    }

    @Test
    public void testOuterHtmlHeadXmlNoAttributesProcessingInstructionTrue() throws IOException {
        XmlDeclaration decl = new XmlDeclaration("xml", "http://example.com", true);
        StringBuilder sb = new StringBuilder();
        decl.outerHtmlHead(sb, 0, new Document.OutputSettings());
        assertEquals("<!xml>", sb.toString());
    }

    @Test
    public void testOuterHtmlHeadXmlWithVersionAndEncodingProcessingInstructionFalse() throws IOException {
        XmlDeclaration decl = new XmlDeclaration("xml", "http://example.com", false);
        decl.attr("version", "1.0");
        decl.attr("encoding", "UTF-8");
        StringBuilder sb = new StringBuilder();
        decl.outerHtmlHead(sb, 0, new Document.OutputSettings());
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", sb.toString());
    }

    @Test
    public void testOuterHtmlHeadXmlWithVersionAndEncodingProcessingInstructionTrue() throws IOException {
        XmlDeclaration decl = new XmlDeclaration("xml", "http://example.com", true);
        decl.attr("version", "1.0");
        decl.attr("encoding", "UTF-8");
        StringBuilder sb = new StringBuilder();
        decl.outerHtmlHead(sb, 0, new Document.OutputSettings());
        assertEquals("<!xml version=\"1.0\" encoding=\"UTF-8\">", sb.toString());
    }
}