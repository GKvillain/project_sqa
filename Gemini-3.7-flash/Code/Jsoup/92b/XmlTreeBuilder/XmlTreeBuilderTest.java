package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.CDataNode;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.DocumentType;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.nodes.XmlDeclaration;
import org.junit.Before;
import org.junit.Test;

import java.io.StringReader;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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

    // Tests parsing simple XML with String input
    @Test
    public void testParse_simpleXmlString_createsCorrectDocument() {
        String xml = "<root><child id=\"1\">Hello</child></root>";
        Document doc = treeBuilder.parse(xml, "http://example.com/");

        assertEquals(Document.OutputSettings.Syntax.xml, doc.outputSettings().syntax());
        Element root = doc.child(0);
        assertEquals("root", root.tagName());
        Element child = root.child(0);
        assertEquals("child", child.tagName());
        assertEquals("1", child.attr("id"));
        assertEquals("Hello", child.text());
    }

    // Tests parsing with Reader input
    @Test
    public void testParse_readerInput_createsCorrectDocument() {
        String xml = "<item>Value</item>";
        Document doc = treeBuilder.parse(new StringReader(xml), "http://example.com/");

        assertNotNull(doc);
        assertEquals("item", doc.child(0).tagName());
        assertEquals("Value", doc.child(0).text());
    }

    // Tests preserving case in tags and attributes
    @Test
    public void testParse_preserveCaseInTagsAndAttributes_preservesExactCase() {
        String xml = "<MixedCase TagAttr=\"Value\" ALLCAPS=\"1\" />";
        Document doc = treeBuilder.parse(xml, "");

        Element el = doc.child(0);
        assertEquals("MixedCase", el.tagName());
        assertTrue(el.hasAttr("TagAttr"));
        assertEquals("Value", el.attr("TagAttr"));
        assertTrue(el.hasAttr("ALLCAPS"));
        assertEquals("1", el.attr("ALLCAPS"));
    }

    // Tests duplicate attributes normalization and retention in XML
    @Test
    public void testParse_duplicateAttributes_dropsOrNormalizesProperly() {
        String xml = "<tag attr=\"one\" ATTR=\"two\" />";
        Document doc = treeBuilder.parse(xml, "");

        Element el = doc.child(0);
        assertEquals("tag", el.tagName());
        assertTrue(el.hasAttr("attr"));
        assertTrue(el.hasAttr("ATTR"));
    }

    // Tests self-closing tag handling
    @Test
    public void testParse_selfClosingTag_doesNotRemainOnStack() {
        String xml = "<root><selfClosing id=\"1\" /><following>Text</following></root>";
        Document doc = treeBuilder.parse(xml, "");

        Element root = doc.child(0);
        assertEquals(2, root.children().size());
        assertEquals("selfClosing", root.child(0).tagName());
        assertEquals("following", root.child(1).tagName());
    }

    // Tests XML declaration parsing
    @Test
    public void testParse_xmlDeclaration_insertsXmlDeclarationNode() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root />";
        Document doc = treeBuilder.parse(xml, "");

        List<Node> nodes = doc.childNodes();
        assertTrue(nodes.size() >= 2);
        assertTrue(nodes.get(0) instanceof XmlDeclaration);
        XmlDeclaration decl = (XmlDeclaration) nodes.get(0);
        assertEquals("xml", decl.name());
        assertEquals("1.0", decl.attr("version"));
        assertEquals("UTF-8", decl.attr("encoding"));
    }

    // Tests standard comment parsing
    @Test
    public void testParse_comment_insertsCommentNode() {
        String xml = "<root><!-- this is a comment --></root>";
        Document doc = treeBuilder.parse(xml, "");

        Element root = doc.child(0);
        assertEquals(1, root.childNodeSize());
        assertTrue(root.childNode(0) instanceof Comment);
        Comment comment = (Comment) root.childNode(0);
        assertEquals(" this is a comment ", comment.getData());
    }

    // Tests CDATA section parsing
    @Test
    public void testParse_cdataSection_insertsCDataNode() {
        String xml = "<root><![CDATA[<unescaped & content>]]></root>";
        Document doc = treeBuilder.parse(xml, "");

        Element root = doc.child(0);
        assertEquals(1, root.childNodeSize());
        assertTrue(root.childNode(0) instanceof CDataNode);
        CDataNode cdata = (CDataNode) root.childNode(0);
        assertEquals("<unescaped & content>", cdata.text());
    }

    // Tests Doctype parsing
    @Test
    public void testParse_doctype_insertsDocumentTypeNode() {
        String xml = "<!DOCTYPE html SYSTEM \"about:legacy-compat\"><html />";
        Document doc = treeBuilder.parse(xml, "");

        List<Node> nodes = doc.childNodes();
        assertTrue(nodes.size() >= 2);
        assertTrue(nodes.get(0) instanceof DocumentType);
        DocumentType doctype = (DocumentType) nodes.get(0);
        assertEquals("html", doctype.attr("name"));
        assertEquals("about:legacy-compat", doctype.attr("systemId"));
    }

    // Tests nested element closing and stack unwinding
    @Test
    public void testParse_nestedElements_correctlyUnwindsStack() {
        String xml = "<a><b><c>test</c></b></a>";
        Document doc = treeBuilder.parse(xml, "");

        Element a = doc.child(0);
        assertEquals("a", a.tagName());
        Element b = a.child(0);
        assertEquals("b", b.tagName());
        Element c = b.child(0);
        assertEquals("c", c.tagName());
        assertEquals("test", c.text());
    }

    // Tests closing an out-of-order tag (premature close)
    @Test
    public void testParse_outOfOrderClosingTag_closesInterveningElements() {
        String xml = "<a><b><c></a>";
        Document doc = treeBuilder.parse(xml, "");

        Element a = doc.child(0);
        assertEquals("a", a.tagName());
        Element b = a.child(0);
        assertEquals("b", b.tagName());
        Element c = b.child(0);
        assertEquals("c", c.tagName());
    }

    // Tests end tag that does not exist in stack (should be ignored)
    @Test
    public void testParse_nonExistentClosingTag_isIgnored() {
        String xml = "<root></nonexistent><child>data</child></root>";
        Document doc = treeBuilder.parse(xml, "");

        Element root = doc.child(0);
        assertEquals(1, root.children().size());
        assertEquals("child", root.child(0).tagName());
        assertEquals("data", root.child(0).text());
    }

    // Tests parseFragment without context
    @Test
    public void testParseFragment_validFragment_returnsListOfNodes() {
        Parser parser = new Parser(treeBuilder);
        List<Node> nodes = treeBuilder.parseFragment("<item1/><item2>Text</item2>", "http://example.com/", parser);

        assertEquals(2, nodes.size());
        assertTrue(nodes.get(0) instanceof Element);
        assertEquals("item1", ((Element) nodes.get(0)).tagName());
        assertTrue(nodes.get(1) instanceof Element);
        assertEquals("item2", ((Element) nodes.get(1)).tagName());
    }

    // Tests parseFragment with context element
    @Test
    public void testParseFragment_withContextElement_returnsListOfNodes() {
        Parser parser = new Parser(treeBuilder);
        Element context = new Element(Tag.valueOf("context"), "");
        List<Node> nodes = treeBuilder.parseFragment("<child>Content</child>", context, "http://example.com/", parser);

        assertEquals(1, nodes.size());
        assertTrue(nodes.get(0) instanceof Element);
        Element child = (Element) nodes.get(0);
        assertEquals("child", child.tagName());
        assertEquals("Content", child.text());
    }

    // Tests empty XML input handling
    @Test
    public void testParse_emptyString_returnsEmptyDocument() {
        Document doc = treeBuilder.parse("", "");
        assertEquals(0, doc.children().size());
    }

    // Tests Character token inserting plain text node
    @Test
    public void testParse_plainText_insertsTextNode() {
        String xml = "<root>Simple Text</root>";
        Document doc = treeBuilder.parse(xml, "");

        Element root = doc.child(0);
        assertEquals(1, root.childNodeSize());
        assertTrue(root.childNode(0) instanceof TextNode);
        assertEquals("Simple Text", ((TextNode) root.childNode(0)).getWholeText());
    }

    // Tests newInstance method
    @Test
    public void testNewInstance_returnsNewXmlTreeBuilder() {
        XmlTreeBuilder newInstance = treeBuilder.newInstance();
        assertNotNull(newInstance);
        assertTrue(newInstance instanceof XmlTreeBuilder);
    }

    // Tests processing instruction / bogus comment converted to XmlDeclaration
    @Test
    public void testParse_processingInstruction_convertsToXmlDeclaration() {
        String xml = "<?xml-stylesheet href=\"common.css\" type=\"text/css\"?><root/>";
        Document doc = treeBuilder.parse(xml, "");

        List<Node> nodes = doc.childNodes();
        assertTrue(nodes.get(0) instanceof XmlDeclaration);
        XmlDeclaration decl = (XmlDeclaration) nodes.get(0);
        assertEquals("xml-stylesheet", decl.name());
        assertEquals("common.css", decl.attr("href"));
        assertEquals("text/css", decl.attr("type"));
    }

    // Tests invalid bogus comment handling
    @Test
    public void testParse_invalidXmlDeclarationBogusComment_fallsBackToCommentNode() {
        String xml = "<?!invalid?><root/>";
        Document doc = treeBuilder.parse(xml, "");

        List<Node> nodes = doc.childNodes();
        assertTrue(nodes.get(0) instanceof Comment);
        Comment comment = (Comment) nodes.get(0);
        assertTrue(comment.getData().contains("invalid"));
    }
}