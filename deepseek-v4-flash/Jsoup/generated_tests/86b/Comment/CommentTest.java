package org.jsoup.nodes;

import org.junit.Test;
import java.io.IOException;
import static org.junit.Assert.*;

public class CommentTest {

    // Tests constructor with data and getData
    @Test
    public void testConstructor_withData_returnsCorrectData() {
        Comment comment = new Comment("test data");
        assertEquals("test data", comment.getData());
    }

    // Tests deprecated constructor with baseUri
    @Test
    public void testConstructor_withBaseUri_deprecated_works() {
        Comment comment = new Comment("data", "uri");
        assertEquals("data", comment.getData());
    }

    // Tests nodeName
    @Test
    public void testNodeName_returnsComment() {
        Comment comment = new Comment("test");
        assertEquals("#comment", comment.nodeName());
    }

    // Tests toString returns outerHtml
    @Test
    public void testToString_returnsOuterHtml() {
        Comment comment = new Comment("test");
        assertEquals(comment.outerHtml(), comment.toString());
    }

    // Tests outerHtmlHead with prettyPrint false (no indent)
    @Test
    public void testOuterHtmlHead_prettyPrintFalse_returnsCommentNoIndent() throws IOException {
        Comment comment = new Comment("data");
        Document.OutputSettings out = new Document.OutputSettings();
        out.prettyPrint(false);
        StringBuilder accum = new StringBuilder();
        comment.outerHtmlHead(accum, 0, out);
        assertEquals("<!--data-->", accum.toString());
    }

    // Tests outerHtmlHead with prettyPrint true and depth 0 (newline only)
    @Test
    public void testOuterHtmlHead_prettyPrintTrue_depth0_containsComment() throws IOException {
        Comment comment = new Comment("data");
        Document.OutputSettings out = new Document.OutputSettings();
        out.prettyPrint(true);
        StringBuilder accum = new StringBuilder();
        comment.outerHtmlHead(accum, 0, out);
        assertTrue(accum.toString().contains("<!--data-->"));
    }

    // Tests outerHtmlHead with prettyPrint true and depth 1 (indent with two spaces)
    @Test
    public void testOuterHtmlHead_prettyPrintTrue_depth1_containsComment() throws IOException {
        Comment comment = new Comment("data");
        Document.OutputSettings out = new Document.OutputSettings();
        out.prettyPrint(true);
        StringBuilder accum = new StringBuilder();
        comment.outerHtmlHead(accum, 1, out);
        assertTrue(accum.toString().contains("<!--data-->"));
    }

    // Tests outerHtmlTail does nothing
    @Test
    public void testOuterHtmlTail_doesNothing() throws IOException {
        Comment comment = new Comment("data");
        StringBuilder accum = new StringBuilder("x");
        Document.OutputSettings out = new Document.OutputSettings();
        comment.outerHtmlTail(accum, 0, out);
        assertEquals("x", accum.toString());
    }

    // Tests isXmlDeclaration with '?' prefix and length > 1 returns true
    @Test
    public void testIsXmlDeclaration_startsWithQuestionMarkLengthGt1_returnsTrue() {
        Comment comment = new Comment("?xml version=\"1.0\"?");
        assertTrue(comment.isXmlDeclaration());
    }

    // Tests isXmlDeclaration with '!' prefix and length > 1 returns true
    @Test
    public void testIsXmlDeclaration_startsWithExclamationMarkLengthGt1_returnsTrue() {
        Comment comment = new Comment("!DOCTYPE");
        assertTrue(comment.isXmlDeclaration());
    }

    // Tests isXmlDeclaration with neither prefix returns false
    @Test
    public void testIsXmlDeclaration_startsWithNeither_returnsFalse() {
        Comment comment = new Comment("hello");
        assertFalse(comment.isXmlDeclaration());
    }

    // Tests isXmlDeclaration with length 1 returns false
    @Test
    public void testIsXmlDeclaration_lengthOne_returnsFalse() {
        Comment comment = new Comment("!");
        assertFalse(comment.isXmlDeclaration());
    }

    // Tests isXmlDeclaration with empty string returns false
    @Test
    public void testIsXmlDeclaration_lengthZero_returnsFalse() {
        Comment comment = new Comment("");
        assertFalse(comment.isXmlDeclaration());
    }

    // Tests isXmlDeclaration with null data throws NullPointerException
    @Test(expected = NullPointerException.class)
    public void testIsXmlDeclaration_nullData_throwsNullPointerException() {
        Comment comment = new Comment(null);
        comment.isXmlDeclaration();
    }

    // Tests asXmlDeclaration with valid '?' declaration returns non-null
    @Test
    public void testAsXmlDeclaration_validQuestionMarkDeclaration_returnsNotNull() {
        Comment comment = new Comment("?xml version=\"1.0\"?");
        XmlDeclaration decl = comment.asXmlDeclaration();
        assertNotNull(decl);
        assertTrue(decl instanceof XmlDeclaration);
    }

    // Tests asXmlDeclaration with valid '!' declaration returns non-null
    @Test
    public void testAsXmlDeclaration_validExclamationDeclaration_returnsNotNull() {
        Comment comment = new Comment("!DOCTYPE html");
        XmlDeclaration decl = comment.asXmlDeclaration();
        assertNotNull(decl);
        assertTrue(decl instanceof XmlDeclaration);
    }

    // Tests asXmlDeclaration with data length 3 ('?ab') returns non-null (branch childNodeSize > 0)
    @Test
    public void testAsXmlDeclaration_dataLength3Question_returnsNotNull() {
        Comment comment = new Comment("?ab");
        XmlDeclaration decl = comment.asXmlDeclaration();
        assertNotNull(decl);
        assertTrue(decl instanceof XmlDeclaration);
    }

    // Tests asXmlDeclaration with data length 3 ('!ab') returns non-null
    @Test
    public void testAsXmlDeclaration_dataLength3Exclamation_returnsNotNull() {
        Comment comment = new Comment("!ab");
        XmlDeclaration decl = comment.asXmlDeclaration();
        assertNotNull(decl);
        assertTrue(decl instanceof XmlDeclaration);
    }

    // Tests asXmlDeclaration with data length 2 ('?x') returns null (branch childNodeSize == 0)
    @Test
    public void testAsXmlDeclaration_dataLength2Question_returnsNull() {
        Comment comment = new Comment("?x");
        XmlDeclaration decl = comment.asXmlDeclaration();
        assertNull(decl);
    }

    // Tests asXmlDeclaration with data length 2 ('!y') returns null
    @Test
    public void testAsXmlDeclaration_dataLength2Exclamation_returnsNull() {
        Comment comment = new Comment("!y");
        XmlDeclaration decl = comment.asXmlDeclaration();
        assertNull(decl);
    }

    // Tests asXmlDeclaration with null data throws NullPointerException
    @Test(expected = NullPointerException.class)
    public void testAsXmlDeclaration_nullData_throwsNullPointerException() {
        Comment comment = new Comment(null);
        comment.asXmlDeclaration();
    }
}