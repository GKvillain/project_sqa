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

    // Tests default settings preserve case
    @Test
    public void testDefaultSettings_returnsPreserveCase() {
        ParseSettings settings = treeBuilder.defaultSettings();
        assertNotNull(settings);
        assertEquals(ParseSettings.preserveCase, settings);
    }

    // Tests parsing simple XML element hierarchy
    @Test
    public void testParse_simpleXml_createsCorrectDocument() {
        String xml = "<root><child id=\"1\">Hello</child></root>";
        Document doc = treeBuilder.parse(xml, "http://example.com/");

        assertEquals(Document.OutputSettings.Syntax.xml, doc.outputSettings().syntax());
        Element root = doc.child(0);
        assertEquals("root", root.tagName());
        assertEquals(1, root.children().size());

        Element child = root.child(0);
        assertEquals("child", child.tagName());
        assertEquals("1", child.attr("id"));
        assertEquals("Hello", child.text());
        assertEquals("http://example.com/", child.baseUri());
    }

    // Tests preserving tag and attribute case
    @Test
    public void testParse_casePreservation_preservesTagAndAttributeCase() {
        String xml = "<CamelCase TagAttribute=\"ValUE\">Text</CamelCase>";
        Document doc = treeBuilder.parse(xml, "");

        Element el = doc.child(0);
        assertEquals("CamelCase", el.tagName());
        assertTrue(el.hasAttr("TagAttribute"));
        assertEquals("ValUE", el.attr("TagAttribute"));
    }

    // Tests self-closing tag handling for unknown XML tags
    @Test
    public void testParse_selfClosingTag_handledCorrectly() {
        String xml = "<root><selfClosing attr=\"value\"/><sibling>content</sibling></root>";
        Document doc = treeBuilder.parse(xml, "");

        Element root = doc.child(0);
        assertEquals(2, root.children().size());
        assertEquals("selfClosing", root.child(0).tagName());
        assertEquals("value", root.child(0).attr("attr"));
        assertEquals("sibling", root.child(1).tagName());
        assertEquals("content", root.child(1).text());
    }

    // Tests XML declaration parsing from bogus comment starting with '?'
    @Test
    public void testParse_xmlDeclarationWithQuestionMark_createsXmlDeclarationNode() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root/>";
        Document doc = treeBuilder.parse(xml, "http://example.com/");

        List<Node> nodes = doc.childNodes();
        assertTrue(nodes.size() >= 2);
        assertTrue(nodes.get(0) instanceof XmlDeclaration);

        XmlDeclaration decl = (XmlDeclaration) nodes.get(0);
        assertEquals("xml", decl.name());
        assertEquals("1.0", decl.attr("version"));
        assertEquals("UTF-8", decl.attr("encoding"));
        assertFalse(decl.isProcessingInstruction());
    }

    // Tests XML declaration / bogus comment starting with '!'
    @Test
    public void testParse_xmlDeclarationWithExclamation_createsProcessingInstruction() {
        String xml = "<!xml version=\"1.0\"?><root/>";
        Document doc = treeBuilder.parse(xml, "");

        List<Node> nodes = doc.childNodes();
        assertTrue(nodes.size() >= 2);
        assertTrue(nodes.get(0) instanceof XmlDeclaration);

        XmlDeclaration decl = (XmlDeclaration) nodes.get(0);
        assertEquals("xml", decl.name());
        assertTrue(decl.isProcessingInstruction());
    }

    // Tests regular comment parsing
    @Test
    public void testParse_regularComment_createsCommentNode() {
        String xml = "<root><!-- This is a comment -->text</root>";
        Document doc = treeBuilder.parse(xml, "");

        Element root = doc.child(0);
        assertEquals(2, root.childNodeSize());
        assertTrue(root.childNode(0) instanceof Comment);

        Comment comment = (Comment) root.childNode(0);
        assertEquals(" This is a comment ", comment.getData());
    }

    // Tests short bogus comment boundary (length <= 1)
    @Test
    public void testParse_shortBogusComment_createsCommentNode() {
        String xml = "<?><root/>";
        Document doc = treeBuilder.parse(xml, "");

        assertTrue(doc.childNode(0) instanceof Comment);
    }

    // Tests DocumentType token parsing with name, publicId, and systemId
    @Test
    public void testParse_doctype_createsDocumentTypeNode() {
        String xml = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\"><html/>";
        Document doc = treeBuilder.parse(xml, "http://example.com/");

        List<Node> nodes = doc.childNodes();
        assertTrue(nodes.get(0) instanceof DocumentType);

        DocumentType doctype = (DocumentType) nodes.get(0);
        assertEquals("html", doctype.attr("name"));
        assertEquals("-//W3C//DTD XHTML 1.0 Strict//EN", doctype.attr("publicId"));
        assertEquals("http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd", doctype.attr("systemId"));
    }

    // Tests popStackToClose when closing tag matches an element deeper in stack
    @Test
    public void testParse_nestedUnclosedTags_popStackClosesCorrectly() {
        String xml = "<a><b><c>test</a>";
        Document doc = treeBuilder.parse(xml, "");

        assertEquals(1, doc.children().size());
        Element a = doc.child(0);
        assertEquals("a", a.tagName());
        assertTrue(a.children().size() > 0);
    }

    // Tests popStackToClose when closing tag is not found in stack
    @Test
    public void testParse_unmatchedClosingTag_ignoresSilently() {
        String xml = "<root>text</unmatched></root>";
        Document doc = treeBuilder.parse(xml, "");

        Element root = doc.child(0);
        assertEquals("root", root.tagName());
        assertEquals("text", root.text());
    }

    // Tests parsing fragment into a list of nodes
    @Test
    public void testParseFragment_validXmlFragment_returnsNodeList() {
        String fragment = "<item>1</item><item>2</item>";
        List<Node> nodes = treeBuilder.parseFragment(fragment, "http://example.com/", ParseErrorList.noTracking(), ParseSettings.preserveCase);

        assertEquals(2, nodes.size());
        assertTrue(nodes.get(0) instanceof Element);
        assertTrue(nodes.get(1) instanceof Element);

        assertEquals("item", ((Element) nodes.get(0)).tagName());
        assertEquals("1", ((Element) nodes.get(0)).text());
        assertEquals("item", ((Element) nodes.get(1)).tagName());
        assertEquals("2", ((Element) nodes.get(1)).text());
    }

    // Tests empty XML input parsing
    @Test
    public void testParse_emptyInput_returnsEmptyDocumentWithXmlSyntax() {
        Document doc = treeBuilder.parse("", "");
        assertNotNull(doc);
        assertEquals(0, doc.children().size());
        assertEquals(Document.OutputSettings.Syntax.xml, doc.outputSettings().syntax());
    }

    // Tests process method handling EOF token
    @Test
    public void testProcess_eofToken_returnsTrue() {
        treeBuilder.parse(new StringReader("<root/>"), "");
        Token.EOF eof = new Token.EOF();
        boolean result = treeBuilder.process(eof);
        assertTrue(result);
    }

    // Tests Jsoup.parse integration with xmlParser
    @Test
    public void testJsoupParse_withXmlParser_usesXmlTreeBuilder() {
        String xml = "<xml><child>value</child></xml>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());

        assertEquals(Document.OutputSettings.Syntax.xml, doc.outputSettings().syntax());
        assertEquals("xml", doc.child(0).tagName());
        assertEquals("value", doc.child(0).child(0).text());
    }

    // Tests parsing from Reader input stream
    @Test
    public void testParse_readerInput_parsesCorrectly() {
        StringReader reader = new StringReader("<root><child>value</child></root>");
        Document doc = treeBuilder.parse(reader, "http://example.com/");

        assertNotNull(doc);
        assertEquals("root", doc.child(0).tagName());
        assertEquals("value", doc.child(0).child(0).text());
    }

    // Tests CDATA section parsing
    @Test
    public void testParse_cdataSection_parsesCorrectly() {
        String xml = "<root><![CDATA[section <b>with markup</b> & entities]]></root>";
        Document doc = treeBuilder.parse(xml, "");

        Element root = doc.child(0);
        assertEquals(1, root.childNodeSize());
        Node child = root.childNode(0);
        assertTrue(child instanceof CDataNode || child instanceof TextNode);
        assertEquals("section <b>with markup</b> & entities", root.text());
    }

    // Tests Doctype declaration with only name (no publicId or systemId)
    @Test
    public void testParse_doctypeWithoutIds_parsesCorrectly() {
        String xml = "<!DOCTYPE html><html/>";
        Document doc = treeBuilder.parse(xml, "");

        List<Node> nodes = doc.childNodes();
        assertTrue(nodes.get(0) instanceof DocumentType);
        DocumentType doctype = (DocumentType) nodes.get(0);
        assertEquals("html", doctype.attr("name"));
        assertEquals("", doctype.attr("publicId"));
        assertEquals("", doctype.attr("systemId"));
    }

    // Tests Doctype declaration with SYSTEM identifier only
    @Test
    public void testParse_doctypeWithSystemIdOnly() {
        String xml = "<!DOCTYPE html SYSTEM \"about:legacy-compat\"><html/>";
        Document doc = treeBuilder.parse(xml, "");

        List<Node> nodes = doc.childNodes();
        assertTrue(nodes.get(0) instanceof DocumentType);
        DocumentType doctype = (DocumentType) nodes.get(0);
        assertEquals("html", doctype.attr("name"));
        assertEquals("", doctype.attr("publicId"));
        assertEquals("about:legacy-compat", doctype.attr("systemId"));
    }

    // Tests XML processing instruction with declaration name only
    @Test
    public void testParse_processingInstructionWithNoAttributes() {
        String xml = "<?xml?><root/>";
        Document doc = treeBuilder.parse(xml, "");

        List<Node> nodes = doc.childNodes();
        assertTrue(nodes.get(0) instanceof XmlDeclaration);
        XmlDeclaration decl = (XmlDeclaration) nodes.get(0);
        assertEquals("xml", decl.name());
    }

    // Tests parseFragment with Parser parameter
    @Test
    public void testParseFragment_withParserParameter() {
        Parser parser = Parser.xmlParser();
        List<Node> nodes = treeBuilder.parseFragment("<entry key=\"k\">val</entry>", "http://example.com/", parser);

        assertEquals(1, nodes.size());
        assertTrue(nodes.get(0) instanceof Element);
        Element entry = (Element) nodes.get(0);
        assertEquals("entry", entry.tagName());
        assertEquals("k", entry.attr("key"));
        assertEquals("val", entry.text());
    }

    // Tests parseFragment with context Element and Parser parameter
    @Test
    public void testParseFragment_withContextAndParser() {
        Parser parser = Parser.xmlParser();
        Element context = new Element(Tag.valueOf("root", ParseSettings.preserveCase), "");
        List<Node> nodes = treeBuilder.parseFragment("<node>item</node>", context, "http://example.com/", parser);

        assertEquals(1, nodes.size());
        assertTrue(nodes.get(0) instanceof Element);
        Element node = (Element) nodes.get(0);
        assertEquals("node", node.tagName());
        assertEquals("item", node.text());
    }

    // Tests multiple root elements parsed in XML mode
    @Test
    public void testParse_multipleRootElements() {
        String xml = "<first>1</first><second>2</second>";
        Document doc = treeBuilder.parse(xml, "");

        assertEquals(2, doc.children().size());
        assertEquals("first", doc.child(0).tagName());
        assertEquals("second", doc.child(1).tagName());
    }

    // Tests self-closing tag without attributes
    @Test
    public void testParse_selfClosingTagWithoutAttributes() {
        String xml = "<root><empty/><other>text</other></root>";
        Document doc = treeBuilder.parse(xml, "");

        Element root = doc.child(0);
        assertEquals(2, root.children().size());
        assertEquals("empty", root.child(0).tagName());
        assertEquals(0, root.child(0).children().size());
        assertEquals("other", root.child(1).tagName());
    }
}