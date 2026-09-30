package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.parser.Parser;
import org.junit.Test;
import static org.junit.Assert.*;

public class XmlTreeBuilderTest {

    // Tests normal XML with nested elements
    @Test
    public void testParse_simpleXml_createsCorrectStructure() {
        String xml = "<root><child/></root>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
        Element root = doc.child(0);
        assertEquals("root", root.tagName());
        assertEquals(1, root.childNodes().size());
        assertEquals("child", root.child(0).tagName());
    }

    // Tests self-closing tag (e.g., <br/>) does not push to stack
    @Test
    public void testParse_selfClosingTag_notInStack() {
        String xml = "<root><br/></root>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
        Element root = doc.child(0);
        assertEquals(1, root.childNodes().size());
        Element br = root.child(0);
        assertTrue(br.tag().isSelfClosing());
    }

    // Tests nested tags create proper hierarchy
    @Test
    public void testParse_nestedTags_correctHierarchy() {
        String xml = "<a><b><c/></b></a>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
        Element a = doc.child(0);
        Element b = a.child(0);
        Element c = b.child(0);
        assertEquals("a", a.tagName());
        assertEquals("b", b.tagName());
        assertEquals("c", c.tagName());
    }

    // Tests attributes are preserved
    @Test
    public void testParse_attributes_preserved() {
        String xml = "<div id='x' class='y'/>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
        Element div = doc.child(0);
        assertEquals("x", div.attr("id"));
        assertEquals("y", div.attr("class"));
    }

    // Tests end tag closes matching start tag
    @Test
    public void testParse_endTag_closesMatchingStartTag() {
        String xml = "<a><b></b></a>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
        Element a = doc.child(0);
        assertEquals(1, a.childNodes().size());
        Element b = a.child(0);
        assertEquals("b", b.tagName());
    }

    // Tests end tag not matching (e.g., </c> but no <c>) is ignored
    @Test
    public void testParse_endTagNotMatching_skipsPop() {
        String xml = "<a><b></c></a>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
        Element a = doc.child(0);
        // <b> remains as child of <a> because </c> did not match
        assertEquals(1, a.childNodes().size());
        assertEquals("b", a.child(0).tagName());
    }

    // Tests comment node creation
    @Test
    public void testParse_comment_createsCommentNode() {
        String xml = "<!-- comment -->";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
        Node child = doc.childNode(0);
        assertTrue(child instanceof Comment);
        assertEquals(" comment ", ((Comment) child).getData());
    }

    // Tests XML declaration (<?...?>) becomes XmlDeclaration
    @Test
    public void testParse_xmlDeclaration_createsXmlDeclaration() {
        String xml = "<?xml version=\"1.0\"?><root/>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
        Node first = doc.childNode(0);
        assertTrue(first instanceof XmlDeclaration);
        assertFalse(first instanceof Comment); // ensure it's not a comment
    }

    // Tests bogus comment starting with '!' becomes XmlDeclaration
    @Test
    public void testParse_bogusCommentExclamation_createsXmlDeclaration() {
        String xml = "<!myDirective><root/>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
        Node first = doc.childNode(0);
        assertTrue(first instanceof XmlDeclaration);
    }

    // Tests text node creation
    @Test
    public void testParse_textNode_createsTextNode() {
        String xml = "<tag>text</tag>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
        Element tag = doc.child(0);
        Node child = tag.childNode(0);
        assertTrue(child instanceof TextNode);
        assertEquals("text", ((TextNode) child).getWholeText());
    }

    // Tests doctype node creation
    @Test
    public void testParse_doctype_createsDocumentType() {
        String xml = "<!DOCTYPE root><root/>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
        Node first = doc.childNode(0);
        assertTrue(first instanceof DocumentType);
        assertEquals("root", ((DocumentType) first).name());
    }

    // Tests empty input results in empty document
    @Test
    public void testParse_emptyInput_emptyDocument() {
        Document doc = Jsoup.parse("", "", Parser.xmlParser());
        assertEquals(0, doc.childNodes().size());
    }

    // Tests whitespace only input creates text node
    @Test
    public void testParse_whitespaceOnly_createsTextNode() {
        Document doc = Jsoup.parse("   ", "", Parser.xmlParser());
        Node child = doc.childNode(0);
        assertTrue(child instanceof TextNode);
        assertEquals("   ", ((TextNode) child).getWholeText());
    }

    // Tests multiple self-closing tags are siblings
    @Test
    public void testParse_multipleSelfClosingTags_siblings() {
        String xml = "<root><br/><br/><hr/></root>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
        Element root = doc.child(0);
        assertEquals(3, root.childNodes().size());
    }

    // Tests unknown custom tag self-closing flag is set
    @Test
    public void testParse_unknownSelfClosingTag_setsSelfClosingFlag() {
        String xml = "<foo/>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
        Element foo = doc.child(0);
        assertTrue(foo.tag().isSelfClosing());
    }

    // Tests end tag closes deeply nested element correctly
    @Test
    public void testParse_endTagDeepNested_closesCorrectly() {
        String xml = "<a><b><c></c></b></a>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
        Element a = doc.child(0);
        Element b = a.child(0);
        Element c = b.child(0);
        assertEquals("c", c.tagName());
    }

    // Tests sibling elements created by end tags
    @Test
    public void testParse_endTagSiblingMultipleTags() {
        String xml = "<a><b></b><c></c></a>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
        Element a = doc.child(0);
        assertEquals(2, a.childNodes().size());
    }
}