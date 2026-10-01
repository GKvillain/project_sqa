package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.DocumentType;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.nodes.XmlDeclaration;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class XmlTreeBuilderTest {
    private XmlTreeBuilder treeBuilder;

    @Before
    public void setUp() {
        treeBuilder = new XmlTreeBuilder();
    }

    // Tests normal XML parsing with XML syntax output
    @Test
    public void testParse_simpleXml_createsXmlDocumentStructure() {
        String xml = "<root><child id=\"1\">Text</child></root>";
        Document doc = treeBuilder.parse(xml, "http://example.com");

        assertNotNull(doc);
        assertEquals(Document.OutputSettings.Syntax.xml, doc.outputSettings().syntax());
        Element root = doc.select("root").first();
        assertNotNull(root);
        Element child = root.select("child").first();
        assertNotNull(child);
        assertEquals("1", child.attr("id"));
        assertEquals("Text", child.text());
    }

    // Tests parsing standard XML declaration starting with '?'
    @Test
    public void testParse_xmlDeclarationWithQuestionMark_createsXmlDeclarationNode() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root/>";
        Document doc = treeBuilder.parse(xml, "http://example.com");

        List<Node> nodes = doc.childNodes();
        assertTrue(nodes.size() >= 2);
        assertTrue(nodes.get(0) instanceof XmlDeclaration);
        XmlDeclaration decl = (XmlDeclaration) nodes.get(0);
        assertEquals("xml version=\"1.0\" encoding=\"UTF-8\"", decl.getWholeDeclaration());
        assertFalse(decl.name().startsWith("!"));
    }

    // Tests parsing bogus comment starting with '!' as XML declaration
    @Test
    public void testParse_xmlDeclarationWithExclamationMark_createsXmlDeclarationNode() {
        String xml = "<!DECL test=\"val\"><root/>";
        Document doc = treeBuilder.parse(xml, "http://example.com");

        List<Node> nodes = doc.childNodes();
        assertTrue(nodes.size() >= 2);
        assertTrue(nodes.get(0) instanceof XmlDeclaration);
        XmlDeclaration decl = (XmlDeclaration) nodes.get(0);
        assertEquals("DECL test=\"val\"", decl.getWholeDeclaration());
    }

    // Tests parsing DOCTYPE token branch
    @Test
    public void testParse_doctypeToken_createsDocumentTypeNode() {
        String xml = "<!DOCTYPE html SYSTEM \"about:legacy-compat\"><root/>";
        Document doc = treeBuilder.parse(xml, "http://example.com");

        List<Node> nodes = doc.childNodes();
        assertTrue(nodes.size() >= 2);
        assertTrue(nodes.get(0) instanceof DocumentType);
        DocumentType doctype = (DocumentType) nodes.get(0);
        assertEquals("html", doctype.attr("name"));
        assertEquals("about:legacy-compat", doctype.attr("systemId"));
    }

    // Tests parsing normal comment token branch
    @Test
    public void testParse_commentToken_createsCommentNode() {
        String xml = "<root><!-- this is a comment --></root>";
        Document doc = treeBuilder.parse(xml, "http://example.com");

        Element root = doc.select("root").first();
        assertNotNull(root);
        List<Node> childNodes = root.childNodes();
        assertEquals(1, childNodes.size());
        assertTrue(childNodes.get(0) instanceof Comment);
        Comment comment = (Comment) childNodes.get(0);
        assertEquals(" this is a comment ", comment.getData());
    }

    // Tests self-closing tag handling for unknown tags
    @Test
    public void testInsert_selfClosingUnknownTag_remembersSelfClosing() {
        String xml = "<root><custom-tag attr=\"val\"/></root>";
        Document doc = treeBuilder.parse(xml, "http://example.com");

        Element root = doc.select("root").first();
        assertNotNull(root);
        Element customTag = root.select("custom-tag").first();
        assertNotNull(customTag);
        assertEquals("val", customTag.attr("attr"));
        assertTrue(customTag.tag().isSelfClosing());
    }

    // Tests non-self-closing tag pushed and popped from stack
    @Test
    public void testParse_nestedElements_correctHierarchy() {
        String xml = "<a><b><c>data</c></b></a>";
        Document doc = treeBuilder.parse(xml, "http://example.com");

        Element a = doc.select("a").first();
        assertNotNull(a);
        Element b = a.select("b").first();
        assertNotNull(b);
        Element c = b.select("c").first();
        assertNotNull(c);
        assertEquals("data", c.text());
    }

    // Tests popStackToClose when closing tag is matched across multiple unclosed elements
    @Test
    public void testPopStackToClose_unclosedNestedTags_popsInterveningElements() {
        String xml = "<root><outer><inner>text</outer></root>";
        Document doc = treeBuilder.parse(xml, "http://example.com");

        Element root = doc.select("root").first();
        assertNotNull(root);
        Element outer = root.select("outer").first();
        assertNotNull(outer);
        Element inner = outer.select("inner").first();
        assertNotNull(inner);
        assertEquals("text", inner.text());
    }

    // Tests popStackToClose when end tag does not exist on stack
    @Test
    public void testPopStackToClose_nonExistentEndTag_skipsWithoutError() {
        String xml = "<root><child>text</unknown></child></root>";
        Document doc = treeBuilder.parse(xml, "http://example.com");

        Element child = doc.select("child").first();
        assertNotNull(child);
        assertEquals("text", child.text());
    }

    // Tests character token insertion
    @Test
    public void testInsert_characterToken_appendsTextNodes() {
        String xml = "<root>First<!-- comment -->Second</root>";
        Document doc = treeBuilder.parse(xml, "http://example.com");

        Element root = doc.select("root").first();
        assertNotNull(root);
        List<Node> nodes = root.childNodes();
        assertEquals(3, nodes.size());
        assertTrue(nodes.get(0) instanceof TextNode);
        assertEquals("First", ((TextNode) nodes.get(0)).getWholeText());
        assertTrue(nodes.get(1) instanceof Comment);
        assertTrue(nodes.get(2) instanceof TextNode);
        assertEquals("Second", ((TextNode) nodes.get(2)).getWholeText());
    }

    // Tests parseFragment method returns child nodes of parsed fragment
    @Test
    public void testParseFragment_validFragment_returnsListOfNodes() {
        String fragment = "<one/><two>text</two>";
        List<Node> nodes = treeBuilder.parseFragment(fragment, "http://example.com", ParseErrorList.noTracking());

        assertNotNull(nodes);
        assertEquals(2, nodes.size());
        assertTrue(nodes.get(0) instanceof Element);
        assertEquals("one", nodes.get(0).nodeName());
        assertTrue(nodes.get(1) instanceof Element);
        assertEquals("two", nodes.get(1).nodeName());
        assertEquals("text", ((Element) nodes.get(1)).text());
    }

    // Tests parseFragment with empty string returns empty node list
    @Test
    public void testParseFragment_emptyString_returnsEmptyList() {
        List<Node> nodes = treeBuilder.parseFragment("", "http://example.com", ParseErrorList.noTracking());

        assertNotNull(nodes);
        assertEquals(0, nodes.size());
    }

    // Tests case preservation in XML parsing
    @Test
    public void testParse_caseSensitiveTags_preservesCase() {
        String xml = "<CaseSensitiveTag><ChildNode/></CaseSensitiveTag>";
        Document doc = treeBuilder.parse(xml, "http://example.com");

        Element el = doc.select("CaseSensitiveTag").first();
        assertNotNull(el);
        assertEquals("CaseSensitiveTag", el.tagName());
        Element child = el.select("ChildNode").first();
        assertNotNull(child);
        assertEquals("ChildNode", child.tagName());
    }

    // Tests EOF token handling during process
    @Test
    public void testProcess_eofToken_completesParsingSuccessfully() {
        Token.EOF eofToken = new Token.EOF();
        treeBuilder.initialiseParse("<root>", "http://example.com", ParseErrorList.noTracking());
        boolean processed = treeBuilder.process(eofToken);

        assertTrue(processed);
    }

    // Tests bogus comment without '!' or '?' prefix remains standard Comment
    @Test
    public void testInsert_bogusCommentWithoutDeclarationPrefix_treatedAsComment() {
        Token.Comment commentToken = new Token.Comment();
        commentToken.bogus = true;
        commentToken.getData().append("plain bogus data");

        treeBuilder.initialiseParse("<root/>", "http://example.com", ParseErrorList.noTracking());
        treeBuilder.insert(commentToken);

        Element root = treeBuilder.doc;
        List<Node> nodes = root.childNodes();
        assertTrue(nodes.get(nodes.size() - 1) instanceof Comment);
        assertFalse(nodes.get(nodes.size() - 1) instanceof XmlDeclaration);
    }
}