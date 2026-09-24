package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.junit.Before;
import org.junit.Test;

import java.io.StringReader;
import java.util.List;

import static org.junit.Assert.*;

public class XmlTreeBuilderTest {

    private XmlTreeBuilder treeBuilder;

    @Before
    public void setUp() {
        treeBuilder = new XmlTreeBuilder();
    }

    // Tests default settings preserving case
    @Test
    public void testDefaultSettings_returnsPreserveCase() {
        ParseSettings settings = treeBuilder.defaultSettings();
        assertNotNull(settings);
        assertEquals(ParseSettings.preserveCase, settings);
    }

    // Tests simple element parsing
    @Test
    public void testParse_simpleXml_createsDocumentTree() {
        String xml = "<root><child id=\"1\">Text</child></root>";
        Document doc = treeBuilder.parse(xml, "http://example.com/");
        
        assertEquals(Document.OutputSettings.Syntax.xml, doc.outputSettings().syntax());
        Element root = doc.child(0);
        assertEquals("root", root.tagName());
        assertEquals(1, root.children().size());
        
        Element child = root.child(0);
        assertEquals("child", child.tagName());
        assertEquals("1", child.attr("id"));
        assertEquals("Text", child.text());
    }

    // Tests self-closing tag handling
    @Test
    public void testParse_selfClosingTags_parsedCorrectly() {
        String xml = "<root><item val=\"a\"/><item val=\"b\"/></root>";
        Document doc = treeBuilder.parse(xml, "http://example.com/");
        
        Element root = doc.child(0);
        assertEquals(2, root.children().size());
        assertEquals("item", root.child(0).tagName());
        assertEquals("a", root.child(0).attr("val"));
        assertEquals("item", root.child(1).tagName());
        assertEquals("b", root.child(1).attr("val"));
    }

    // Tests standard XML comment
    @Test
    public void testParse_standardComment_insertsCommentNode() {
        String xml = "<root><!-- this is a comment --></root>";
        Document doc = treeBuilder.parse(xml, "http://example.com/");
        
        Element root = doc.child(0);
        assertEquals(1, root.childNodeSize());
        assertTrue(root.childNode(0) instanceof Comment);
        Comment comment = (Comment) root.childNode(0);
        assertEquals(" this is a comment ", comment.getData());
    }

    // Tests XML declaration (bogus comment starting with ?)
    @Test
    public void testParse_xmlDeclaration_insertsXmlDeclarationNode() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root/>";
        Document doc = treeBuilder.parse(xml, "http://example.com/");
        
        assertTrue(doc.childNode(0) instanceof XmlDeclaration);
        XmlDeclaration decl = (XmlDeclaration) doc.childNode(0);
        assertEquals("xml", decl.name());
        assertEquals("1.0", decl.attr("version"));
        assertEquals("UTF-8", decl.attr("encoding"));
    }

    // Tests processing instruction (bogus comment starting with !)
    @Test
    public void testParse_processingInstructionDeclaration_insertsXmlDeclarationNode() {
        String xml = "<!foo bar=\"baz\"><root/>";
        Document doc = treeBuilder.parse(xml, "http://example.com/");
        
        assertTrue(doc.childNode(0) instanceof XmlDeclaration);
        XmlDeclaration decl = (XmlDeclaration) doc.childNode(0);
        assertEquals("foo", decl.name());
        assertEquals("baz", decl.attr("bar"));
    }

    // Tests bogus comment with short/boundary data length <= 1
    @Test
    public void testProcess_bogusCommentShortData_insertsCommentNode() {
        Token.Comment token = new Token.Comment();
        token.bogus = true;
        token.data.append("?");

        treeBuilder.initialiseParse("<root></root>", "http://example.com/", ParseErrorList.noTracking(), ParseSettings.preserveCase);
        treeBuilder.process(token);

        List<Node> nodes = treeBuilder.doc.childNodes();
        boolean foundComment = false;
        for (Node node : nodes) {
            if (node instanceof Comment && !(node instanceof XmlDeclaration)) {
                foundComment = true;
                assertEquals("?", ((Comment) node).getData());
            }
        }
        assertTrue(foundComment);
    }

    // Tests Doctype parsing and insertion
    @Test
    public void testParse_doctype_insertsDocumentTypeNode() {
        String xml = "<!DOCTYPE html SYSTEM \"about:legacy-compat\"><root/>";
        Document doc = treeBuilder.parse(xml, "http://example.com/");
        
        assertTrue(doc.childNode(0) instanceof DocumentType);
        DocumentType doctype = (DocumentType) doc.childNode(0);
        assertEquals("html", doctype.attr("name"));
        assertEquals("about:legacy-compat", doctype.attr("systemId"));
    }

    // Tests unclosed and nested tags to verify popStackToClose behavior
    @Test
    public void testParse_unclosedNestedTags_popsStackCorrectly() {
        String xml = "<a><b><c>test</a>";
        Document doc = treeBuilder.parse(xml, "http://example.com/");
        
        assertEquals(1, doc.children().size());
        Element a = doc.child(0);
        assertEquals("a", a.tagName());
        Element b = a.child(0);
        assertEquals("b", b.tagName());
        Element c = b.child(0);
        assertEquals("c", c.tagName());
        assertEquals("test", c.text());
    }

    // Tests closing tag that does not exist in stack
    @Test
    public void testParse_unexpectedClosingTag_skippedWithoutError() {
        String xml = "<root></nonexistent><child>content</child></root>";
        Document doc = treeBuilder.parse(xml, "http://example.com/");
        
        Element root = doc.child(0);
        assertEquals(1, root.children().size());
        assertEquals("child", root.child(0).tagName());
        assertEquals("content", root.child(0).text());
    }

    // Tests parseFragment method
    @Test
    public void testParseFragment_validXmlFragment_returnsNodesList() {
        String fragment = "<item id=\"1\">A</item><item id=\"2\">B</item>";
        List<Node> nodes = treeBuilder.parseFragment(fragment, "http://example.com/", ParseErrorList.noTracking(), ParseSettings.preserveCase);
        
        assertEquals(2, nodes.size());
        assertTrue(nodes.get(0) instanceof Element);
        assertTrue(nodes.get(1) instanceof Element);
        assertEquals("item", ((Element) nodes.get(0)).tagName());
        assertEquals("1", nodes.get(0).attr("id"));
        assertEquals("item", ((Element) nodes.get(1)).tagName());
        assertEquals("2", nodes.get(1).attr("id"));
    }

    // Tests case preservation in tags and attributes
    @Test
    public void testParse_casePreserved_maintainsTagAndAttributeCasing() {
        String xml = "<XmlRoot MyAttr=\"Value\"><ChildElement/></XmlRoot>";
        Document doc = treeBuilder.parse(xml, "http://example.com/");
        
        Element root = doc.child(0);
        assertEquals("XmlRoot", root.tagName());
        assertTrue(root.hasAttr("MyAttr"));
        assertEquals("Value", root.attr("MyAttr"));
        assertEquals("ChildElement", root.child(0).tagName());
    }

    // Tests EOF token handling in process
    @Test
    public void testProcess_eofToken_returnsTrue() {
        treeBuilder.initialiseParse("<root/>", "http://example.com/", ParseErrorList.noTracking(), ParseSettings.preserveCase);
        Token.EOF eof = new Token.EOF();
        boolean result = treeBuilder.process(eof);
        assertTrue(result);
    }

    // Tests empty string parsing
    @Test
    public void testParse_emptyString_createsEmptyDocument() {
        Document doc = treeBuilder.parse("", "http://example.com/");
        assertNotNull(doc);
        assertEquals(0, doc.children().size());
    }

    // Tests parsing via Reader
    @Test
    public void testParse_readerInput_createsDocumentTree() {
        String xml = "<root><child>Reader Content</child></root>";
        Document doc = treeBuilder.parse(new StringReader(xml), "http://example.com/");

        Element root = doc.child(0);
        assertEquals("root", root.tagName());
        assertEquals("Reader Content", root.child(0).text());
    }

    // Tests CDATA section parsing
    @Test
    public void testParse_cdataSection_insertsTextNode() {
        String xml = "<root><![CDATA[cdata <content> & more]]></root>";
        Document doc = treeBuilder.parse(xml, "http://example.com/");

        Element root = doc.child(0);
        assertEquals("cdata <content> & more", root.text());
    }
}