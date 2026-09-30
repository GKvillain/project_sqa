package org.jsoup.nodes;

import org.jsoup.Jsoup;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class CommentTest {

    // Tests nodeName returns correct comment identifier
    @Test
    public void testNodeName_default_returnsCommentIdentifier() {
        Comment comment = new Comment("This is a comment");
        assertEquals("#comment", comment.nodeName());
    }

    // Tests getData returns correct content
    @Test
    public void testGetData_standardString_returnsCorrectData() {
        String text = "Sample comment text";
        Comment comment = new Comment(text);
        assertEquals(text, comment.getData());
    }

    // Tests deprecated constructor with baseUri
    @Test
    public void testConstructor_withBaseUri_setsDataCorrectly() {
        Comment comment = new Comment("Data with base URI", "http://example.com");
        assertEquals("Data with base URI", comment.getData());
        assertEquals("#comment", comment.nodeName());
    }

    // Tests outerHtml output formatting
    @Test
    public void testOuterHtml_prettyPrintEnabled_formatsWithCommentTags() {
        Comment comment = new Comment("hello world");
        assertEquals("<!--hello world-->", comment.outerHtml());
        assertEquals("<!--hello world-->", comment.toString());
    }

    // Tests outerHtml with prettyPrint disabled
    @Test
    public void testOuterHtml_prettyPrintDisabled_formatsWithCommentTags() {
        Comment comment = new Comment("test comment");
        Document.OutputSettings settings = new Document.OutputSettings().prettyPrint(false);
        assertEquals("<!--test comment-->", comment.outerHtml());
    }

    // Tests isXmlDeclaration with standard non-declaration text
    @Test
    public void testIsXmlDeclaration_regularComment_returnsFalse() {
        Comment comment = new Comment("just a regular comment");
        assertFalse(comment.isXmlDeclaration());
    }

    // Tests isXmlDeclaration with empty content
    @Test
    public void testIsXmlDeclaration_emptyString_returnsFalse() {
        Comment comment = new Comment("");
        assertFalse(comment.isXmlDeclaration());
    }

    // Tests isXmlDeclaration with single character boundary cases
    @Test
    public void testIsXmlDeclaration_singleCharExclamation_returnsFalse() {
        Comment comment = new Comment("!");
        assertFalse(comment.isXmlDeclaration());
    }

    // Tests isXmlDeclaration with single character question mark boundary
    @Test
    public void testIsXmlDeclaration_singleCharQuestionMark_returnsFalse() {
        Comment comment = new Comment("?");
        assertFalse(comment.isXmlDeclaration());
    }

    // Tests isXmlDeclaration starting with question mark
    @Test
    public void testIsXmlDeclaration_startingWithQuestionMark_returnsTrue() {
        Comment comment = new Comment("?xml version=\"1.0\" encoding=\"utf-8\"?");
        assertTrue(comment.isXmlDeclaration());
    }

    // Tests isXmlDeclaration starting with exclamation mark
    @Test
    public void testIsXmlDeclaration_startingWithExclamation_returnsTrue() {
        Comment comment = new Comment("!DOCTYPE html");
        assertTrue(comment.isXmlDeclaration());
    }

    // Tests asXmlDeclaration on standard XML declaration
    @Test
    public void testAsXmlDeclaration_validXmlDeclaration_returnsParsedXmlDeclaration() {
        Comment comment = new Comment("?xml version=\"1.0\" encoding=\"utf-8\"?");
        XmlDeclaration decl = comment.asXmlDeclaration();

        assertNotNull(decl);
        assertEquals("xml", decl.name());
        assertEquals("1.0", decl.attr("version"));
        assertEquals("utf-8", decl.attr("encoding"));
        assertFalse(decl.isProcessingInstruction());
    }

    // Tests asXmlDeclaration on DOCTYPE declaration
    @Test
    public void testAsXmlDeclaration_doctypeDeclaration_returnsParsedXmlDeclarationWithExclamation() {
        Comment comment = new Comment("!DOCTYPE html");
        XmlDeclaration decl = comment.asXmlDeclaration();

        assertNotNull(decl);
        assertEquals("DOCTYPE", decl.name());
        assertTrue(decl.attributes().hasKey("html"));
        assertTrue(decl.isProcessingInstruction());
    }

    // Tests asXmlDeclaration when parsed content has no element child
    @Test
    public void testAsXmlDeclaration_commentProducingNoElementChild_handlesGracefully() {
        Comment comment = new Comment("?eval bogus?");
        XmlDeclaration decl = comment.asXmlDeclaration();
        assertNotNull(decl);
        assertEquals("eval", decl.name());
    }

    // Tests outerHtmlHead with Appendable
    @Test
    public void testOuterHtmlHead_customAppendable_appendsCorrectly() throws IOException {
        Comment comment = new Comment("append test");
        StringBuilder sb = new StringBuilder();
        Document.OutputSettings settings = new Document.OutputSettings();
        comment.outerHtmlHead(sb, 0, settings);
        comment.outerHtmlTail(sb, 0, settings);
        assertEquals("<!--append test-->", sb.toString());
    }

    // Tests setData updates the comment content and returns the comment instance
    @Test
    public void testSetData_updatesDataAndReturnsThis() {
        Comment comment = new Comment("initial data");
        Comment returned = comment.setData("updated data");
        assertEquals("updated data", comment.getData());
        assertEquals(comment, returned);
    }

    // Tests clone creates an independent copy
    @Test
    public void testClone_createsIndependentCopy() {
        Comment original = new Comment("cloneable text");
        Comment clone = original.clone();
        assertNotSame(original, clone);
        assertEquals(original.getData(), clone.getData());

        clone.setData("changed text");
        assertEquals("cloneable text", original.getData());
        assertEquals("changed text", clone.getData());
    }

    // Tests asXmlDeclaration returns null when parsed XML produces no children
    @Test
    public void testAsXmlDeclaration_emptyParsedStructure_returnsNull() {
        Comment comment = new Comment("? ?");
        XmlDeclaration decl = comment.asXmlDeclaration();
        assertNull(decl);
    }

    // Tests outerHtml indentation when outline mode is enabled
    @Test
    public void testOuterHtmlHead_outlineModeEnabled_indentsOutput() throws IOException {
        Comment comment = new Comment("outline test");
        StringBuilder sb = new StringBuilder();
        Document.OutputSettings settings = new Document.OutputSettings().outline(true).indentAmount(2);
        comment.outerHtmlHead(sb, 1, settings);
        comment.outerHtmlTail(sb, 1, settings);
        assertEquals("  <!--outline test-->", sb.toString());
    }

    // Tests outerHtml indentation when comment is first child of a block format element
    @Test
    public void testOuterHtml_firstChildOfBlockElement_indentsCorrectly() {
        Element div = new Element("div");
        Comment comment = new Comment("block child comment");
        div.appendChild(comment);

        Document doc = Jsoup.parse("<div><!--block child comment--></div>");
        doc.outputSettings().prettyPrint(true).indentAmount(2);
        assertTrue(doc.body().html().contains("  <!--block child comment-->"));
    }
}