package org.jsoup.nodes;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class CommentTest {

    // Tests constructor and nodeName
    @Test
    public void testNodeName_normalComment_returnsCommentNodeName() {
        Comment comment = new Comment("This is a comment");
        assertEquals("#comment", comment.nodeName());
    }

    // Tests getData method
    @Test
    public void testGetData_normalComment_returnsContent() {
        Comment comment = new Comment("Sample data");
        assertEquals("Sample data", comment.getData());
    }

    // Tests setData method
    @Test
    public void testSetData_updatesCommentContent() {
        Comment comment = new Comment("Initial");
        comment.setData("Updated");
        assertEquals("Updated", comment.getData());
        assertEquals("<!--Updated-->", comment.toString());
    }

    // Tests deprecated constructor with baseUri
    @Test
    public void testConstructor_withBaseUri_setsDataCorrectly() {
        Comment comment = new Comment("Data with baseUri", "http://example.com");
        assertEquals("Data with baseUri", comment.getData());
        assertEquals("#comment", comment.nodeName());
    }

    // Tests toString and outerHtml formatting
    @Test
    public void testToString_normalComment_returnsHtmlComment() {
        Comment comment = new Comment("Hello World");
        assertEquals("<!--Hello World-->", comment.toString());
    }

    // Tests outerHtmlHead with prettyPrint enabled and disabled
    @Test
    public void testOuterHtml_prettyPrintSettings_formatsCorrectly() throws IOException {
        Comment comment = new Comment("test comment");
        Document.OutputSettings settings = new Document.OutputSettings();
        
        StringBuilder accum = new StringBuilder();
        settings.prettyPrint(true);
        comment.outerHtmlHead(accum, 0, settings);
        comment.outerHtmlTail(accum, 0, settings);
        assertEquals("<!--test comment-->", accum.toString());

        StringBuilder accum2 = new StringBuilder();
        settings.prettyPrint(false);
        comment.outerHtmlHead(accum2, 0, settings);
        comment.outerHtmlTail(accum2, 0, settings);
        assertEquals("<!--test comment-->", accum2.toString());
    }

    // Tests isXmlDeclaration with standard XML declaration prefix '?'
    @Test
    public void testIsXmlDeclaration_questionMarkPrefix_returnsTrue() {
        Comment comment = new Comment("?xml version=\"1.0\" encoding=\"utf-8\"?");
        assertTrue(comment.isXmlDeclaration());
    }

    // Tests isXmlDeclaration with exclamation prefix '!'
    @Test
    public void testIsXmlDeclaration_exclamationPrefix_returnsTrue() {
        Comment comment = new Comment("!DOCTYPE html");
        assertTrue(comment.isXmlDeclaration());
    }

    // Tests isXmlDeclaration with normal comment data
    @Test
    public void testIsXmlDeclaration_normalComment_returnsFalse() {
        Comment comment = new Comment("just a normal comment");
        assertFalse(comment.isXmlDeclaration());
    }

    // Tests isXmlDeclaration with empty data
    @Test
    public void testIsXmlDeclaration_emptyData_returnsFalse() {
        Comment comment = new Comment("");
        assertFalse(comment.isXmlDeclaration());
    }

    // Tests isXmlDeclaration boundary with single character prefix
    @Test
    public void testIsXmlDeclaration_singleCharPrefix_returnsFalse() {
        Comment comment1 = new Comment("?");
        assertFalse(comment1.isXmlDeclaration());

        Comment comment2 = new Comment("!");
        assertFalse(comment2.isXmlDeclaration());
    }

    // Tests asXmlDeclaration parsing valid XML declaration with '?'
    @Test
    public void testAsXmlDeclaration_validXmlDeclaration_returnsDeclarationNode() {
        Comment comment = new Comment("?xml version=\"1.0\" encoding=\"utf-8\"?");
        XmlDeclaration decl = comment.asXmlDeclaration();
        assertNotNull(decl);
        assertEquals("xml", decl.name());
        assertEquals("1.0", decl.attr("version"));
        assertEquals("utf-8", decl.attr("encoding"));
        assertEquals("#declaration", decl.nodeName());
    }

    // Tests asXmlDeclaration parsing declaration with '!'
    @Test
    public void testAsXmlDeclaration_exclamationDeclaration_returnsDeclarationNode() {
        Comment comment = new Comment("!ELEMENT foo EMPTY");
        XmlDeclaration decl = comment.asXmlDeclaration();
        assertNotNull(decl);
        assertEquals("ELEMENT", decl.name());
        assertEquals("#declaration", decl.nodeName());
    }

    // Tests asXmlDeclaration when comment content is a plain comment
    @Test
    public void testAsXmlDeclaration_plainComment_returnsNull() {
        Comment comment = new Comment("plain comment");
        assertNull(comment.asXmlDeclaration());
    }

    // Tests asXmlDeclaration when comment content does not produce child elements (Defects4J 86 regression)
    @Test
    public void testAsXmlDeclaration_nonElementChild_handlesGracefully() {
        Comment comment = new Comment("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        XmlDeclaration decl = comment.asXmlDeclaration();
        assertNull(decl);
    }

    // Tests asXmlDeclaration with malformed or non-element content
    @Test
    public void testAsXmlDeclaration_notAnXmlDeclaration_returnsNull() {
        Comment comment = new Comment("?!");
        XmlDeclaration decl = comment.asXmlDeclaration();
        assertNull(decl);
    }

    // Tests clone method
    @Test
    public void testClone_createsIndependentCopy() {
        Comment comment = new Comment("Clone me");
        Comment clone = comment.clone();
        assertEquals(comment.getData(), clone.getData());
        assertEquals(comment.outerHtml(), clone.outerHtml());
        clone.setData("Changed");
        assertEquals("Clone me", comment.getData());
        assertEquals("Changed", clone.getData());
    }
}