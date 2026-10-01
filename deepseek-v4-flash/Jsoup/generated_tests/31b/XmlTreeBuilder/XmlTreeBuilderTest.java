package org.jsoup.parser;

import org.junit.Test;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.DocumentType;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.parser.Token;
import org.jsoup.parser.TreeBuilder;
import org.jsoup.parser.Parser;
import org.jsoup.parser.ParseErrorList;
import org.jsoup.select.Elements;

import static org.junit.Assert.*;

public class XmlTreeBuilderTest {

    // Tests normal parsing of simple XML
    @Test
    public void testParse_simpleXml_documentCreated() {
        String xml = "<root><child>text</child></root>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
        assertNotNull(doc);
        Element root = doc.child(0);
        assertEquals("root", root.tagName());
        Element child = root.child(0);
        assertEquals("child", child.tagName());
        assertEquals("text", child.text());
    }

    // Tests parsing of self-closing tag (branch: isSelfClosing() = true, known tag false)
    @Test
    public void testInsert_selfClosingTag_tagSetSelfClosing() {
        String xml = "<root><br/></root>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
        Element br = doc.select("br").first();
        assertNotNull(br);
        assertTrue(br.tag().isSelfClosing());
    }

    // Tests parsing of end tag that closes matching element (popStackToClose finds element)
    @Test
    public void testPopStackToClose_endTagFound_closesCorrectly() {
        String xml = "<outer><inner></inner></outer>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
        Elements outers = doc.getElementsByTag("outer");
        assertEquals(1, outers.size());
        Elements inners = doc.getElementsByTag("inner");
        assertEquals(1, inners.size());
        assertEquals(0, inners.first().siblingSize());
    }

    // Tests parsing of end tag when no matching start tag (popStackToClose not found)
    @Test
    public void testPopStackToClose_endTagNotFound_ignored() {
        String xml = "<root></missing></root>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
        assertNotNull(doc);
        Elements roots = doc.getElementsByTag("root");
        assertEquals(1, roots.size());
    }

    // Tests insert of comment token
    @Test
    public void testInsert_commentToken_commentNodeAdded() {
        String xml = "<root><!-- comment --></root>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
        Elements comments = doc.select("root comment");
        // In Jsoup, comments are not part of element tree but are child nodes
        Node child = doc.child(0).childNode(0);
        assertTrue(child instanceof Comment);
        assertEquals(" comment ", ((Comment) child).getData());
    }

    // Tests insert of character token (text)
    @Test
    public void testInsert_characterToken_textNodeAdded() {
        String xml = "<root>text</root>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
        Node child = doc.child(0).childNode(0);
        assertTrue(child instanceof TextNode);
        assertEquals("text", ((TextNode) child).text());
    }

    // Tests insert of doctype token
    @Test
    public void testInsert_doctypeToken_doctypeNodeAdded() {
        String xml = "<!DOCTYPE root><root></root>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
        // Doctype is stored in document node
        DocumentType doctype = doc.documentType();
        assertNotNull(doctype);
        assertEquals("root", doctype.name());
    }

    // Tests parsing of empty string
    @Test
    public void testParse_emptyString_emptyDocument() {
        String xml = "";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
        assertNotNull(doc);
        assertEquals(0, doc.children().size());
    }

    // Tests parsing of null input (should handle gracefully)
    @Test(expected = Exception.class)
    public void testParse_nullInput_throwsException() {
        Jsoup.parse(null, "", Parser.xmlParser());
    }

    // Tests parsing of string with only whitespace
    @Test
    public void testParse_whitespaceInput_emptyDocument() {
        String xml = "   ";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
        assertNotNull(doc);
        assertEquals(0, doc.children().size());
    }

    // Tests parsing of deeply nested structure (boundary for loop)
    @Test
    public void testParse_deeplyNested_parsesCorrectly() {
        StringBuilder sb = new StringBuilder();
        sb.append("<root>");
        for (int i = 0; i < 100; i++) {
            sb.append("<level").append(i).append(">");
        }
        for (int i = 99; i >= 0; i--) {
            sb.append("</level").append(i).append(">");
        }
        sb.append("</root>");
        Document doc = Jsoup.parse(sb.toString(), "", Parser.xmlParser());
        assertNotNull(doc);
        Element root = doc.child(0);
        assertNotNull(root);
        assertEquals("root", root.tagName());
    }

    // Tests parsing of multiple sibling elements
    @Test
    public void testParse_multipleSiblings_allPresent() {
        String xml = "<root><a/><b/><c/></root>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
        Elements siblings = doc.child(0).children();
        assertEquals(3, siblings.size());
        assertEquals("a", siblings.get(0).tagName());
        assertEquals("b", siblings.get(1).tagName());
        assertEquals("c", siblings.get(2).tagName());
    }

    // Tests parsing of element with attributes
    @Test
    public void testParse_elementWithAttributes_attributesPreserved() {
        String xml = "<root attr1=\"val1\" attr2=\"val2\"></root>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
        Element root = doc.child(0);
        assertEquals("val1", root.attr("attr1"));
        assertEquals("val2", root.attr("attr2"));
    }

    // Tests branch where startTag is selfClosing and tag is known (not applicable in XML, but logic exists)
    // This tests the path where tag is known (e.g., <br> in HTML) - not directly testable in XML parser,
    // but we can check that self-closing flag is acknowledged
    @Test
    public void testInsert_selfClosingKnownTag_acknowledged() {
        String xml = "<root><br/></root>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
        Element br = doc.select("br").first();
        assertNotNull(br);
        // In XML parser, all tags are unknown, so the self-closing flag is set via setSelfClosing()
        assertTrue(br.tag().isSelfClosing());
    }

    // Tests that popStackToClose removes elements up to and including the matching one
    @Test
    public void testPopStackToClose_multipleElements_removesCorrectly() {
        String xml = "<a><b><c></c></b></a>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
        Element a = doc.child(0);
        assertEquals("a", a.tagName());
        assertEquals(1, a.children().size());
        assertEquals("b", a.child(0).tagName());
        assertEquals(1, a.child(0).children().size());
        assertEquals("c", a.child(0).child(0).tagName());
    }

    // Tests that doc is placed on stack during initialiseParse
    @Test
    public void testInitialiseParse_docOnStack_documentAccessible() {
        String xml = "<root></root>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
        assertNotNull(doc);
        // After parse, the document should be valid
        assertEquals("#root", doc.child(0).tagName());
    }

    // Tests processing of EOF token (no exception expected)
    @Test
    public void testProcess_eofToken_noException() {
        String xml = "<root></root>";
        // Just ensure parsing completes without error
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
        assertNotNull(doc);
    }
}