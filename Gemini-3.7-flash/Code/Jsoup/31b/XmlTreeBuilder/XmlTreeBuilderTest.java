package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.DocumentType;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.nodes.XmlDeclaration;
import org.junit.Test;

import java.io.StringReader;
import java.util.List;

import static org.junit.Assert.*;

public class XmlTreeBuilderTest {

    // Tests parsing simple XML with various node types
    @Test
    public void testParse_simpleXml_createsCorrectDocumentStructure() {
        String xml = "<doc id=\"2\" href=\"foo\">Foo <br /><!-- comment -->Text</doc>";
        XmlTreeBuilder tb = new XmlTreeBuilder();
        Document doc = tb.parse(xml, "http://example.com/");

        assertNotNull(doc);
        Element el = doc.child(0);
        assertEquals("doc", el.tagName());
        assertEquals("2", el.attr("id"));
        assertEquals("foo", el.attr("href"));
        assertEquals(4, el.childNodeSize());
        assertEquals("Foo ", ((TextNode) el.childNode(0)).getWholeText());
        assertEquals("br", el.child(0).tagName());
        assertEquals(" comment ", ((Comment) el.childNode(2)).getData());
        assertEquals("Text", ((TextNode) el.childNode(3)).getWholeText());
    }

    // Tests XML declaration parsing
    @Test
    public void testParse_xmlDeclaration_parsedAsDeclarationOrComment() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root>val</root>";
        XmlTreeBuilder tb = new XmlTreeBuilder();
        Document doc = tb.parse(xml, "http://example.com/");

        assertNotNull(doc);
        assertEquals("root", doc.select("root").first().tagName());
        assertEquals("val", doc.select("root").first().text());
    }

    // Tests case preservation in tags and attributes
    @Test
    public void testParse_caseSensitiveTags_preservesCase() {
        String xml = "<CHECK><TEST Id=\"1\">Val</TEST></CHECK>";
        XmlTreeBuilder tb = new XmlTreeBuilder();
        Document doc = tb.parse(xml, "http://example.com/");

        Element check = doc.child(0);
        assertEquals("CHECK", check.tagName());
        Element test = check.child(0);
        assertEquals("TEST", test.tagName());
        assertEquals("Val", test.text());
    }

    // Tests self closing tags
    @Test
    public void testParse_selfClosingTag_preservesSelfClosing() {
        String xml = "<root><img src=\"test.png\"/><custom-tag/></root>";
        XmlTreeBuilder tb = new XmlTreeBuilder();
        Document doc = tb.parse(xml, "http://example.com/");

        Element root = doc.child(0);
        assertEquals(2, root.children().size());
        assertEquals("img", root.child(0).tagName());
        assertEquals("custom-tag", root.child(1).tagName());
    }

    // Tests unclosed tags and nested hierarchy
    @Test
    public void testParse_nestedUnclosedTags_handlesGracefully() {
        String xml = "<doc><val>One<val>Two</val></val></doc>";
        XmlTreeBuilder tb = new XmlTreeBuilder();
        Document doc = tb.parse(xml, "http://example.com/");

        Element docEl = doc.child(0);
        assertEquals("doc", docEl.tagName());
        assertEquals(1, docEl.children().size());
        assertEquals("val", docEl.child(0).tagName());
    }

    // Tests pop stack when closing non-existent element
    @Test
    public void testParse_mismatchedEndTag_skipsNonExistentTag() {
        String xml = "<root><child>text</nonexistent></child></root>";
        XmlTreeBuilder tb = new XmlTreeBuilder();
        Document doc = tb.parse(xml, "http://example.com/");

        Element root = doc.child(0);
        assertEquals("root", root.tagName());
        assertEquals("child", root.child(0).tagName());
        assertEquals("text", root.child(0).text());
    }

    // Tests doctype parsing
    @Test
    public void testParse_doctype_insertsDocumentTypeNode() {
        String xml = "<!DOCTYPE html SYSTEM \"about:legacy-compat\"><root/>";
        XmlTreeBuilder tb = new XmlTreeBuilder();
        Document doc = tb.parse(xml, "http://example.com/");

        List<Node> nodes = doc.childNodes();
        assertTrue(nodes.size() >= 2);
        assertTrue(nodes.get(0) instanceof DocumentType);
        DocumentType doctype = (DocumentType) nodes.get(0);
        assertEquals("html", doctype.attr("name"));
    }

    // Tests CDATA section parsing
    @Test
    public void testParse_cdataSection_parsesAsText() {
        String xml = "<data><![CDATA[foo <> & bar]]></data>";
        XmlTreeBuilder tb = new XmlTreeBuilder();
        Document doc = tb.parse(xml, "http://example.com/");

        Element data = doc.child(0);
        assertEquals("foo <> & bar", data.text());
    }

    // Tests multiple root elements in document
    @Test
    public void testParse_multipleRoots_parsedIntoDocument() {
        String xml = "<one>First</one><two>Second</two>";
        XmlTreeBuilder tb = new XmlTreeBuilder();
        Document doc = tb.parse(xml, "http://example.com/");

        assertEquals(2, doc.children().size());
        assertEquals("one", doc.child(0).tagName());
        assertEquals("two", doc.child(1).tagName());
    }

    // Tests empty input
    @Test
    public void testParse_emptyInput_createsEmptyDocument() {
        String xml = "";
        XmlTreeBuilder tb = new XmlTreeBuilder();
        Document doc = tb.parse(xml, "http://example.com/");

        assertNotNull(doc);
        assertEquals(0, doc.children().size());
    }

    // Tests deep nesting and popStackToClose branch
    @Test
    public void testParse_deeplyNestedClosing_popsStackCorrectly() {
        String xml = "<a><b><c><d>deep</d></c></b></a>";
        XmlTreeBuilder tb = new XmlTreeBuilder();
        Document doc = tb.parse(xml, "http://example.com/");

        Element a = doc.child(0);
        assertEquals("a", a.tagName());
        Element b = a.child(0);
        assertEquals("b", b.tagName());
        Element c = b.child(0);
        assertEquals("c", c.tagName());
        Element d = c.child(0);
        assertEquals("d", d.tagName());
        assertEquals("deep", d.text());
    }

    // Tests parsing via Parser helper with XML Parser configuration
    @Test
    public void testParse_viaParserXmlParser_preservesCaseAndSelfClosing() {
        String xml = "<CustomTag key=\"VAL\"><NestedTag/></CustomTag>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());

        assertEquals("<CustomTag key=\"VAL\">\n <NestedTag />\n</CustomTag>", doc.html().trim());
    }

    // Tests parsing using Reader input
    @Test
    public void testParse_readerInput_parsesCorrectly() {
        StringReader reader = new StringReader("<root><item>value</item></root>");
        XmlTreeBuilder tb = new XmlTreeBuilder();
        Document doc = tb.parse(reader, "http://example.com/");

        assertNotNull(doc);
        assertEquals("root", doc.child(0).tagName());
        assertEquals("item", doc.child(0).child(0).tagName());
        assertEquals("value", doc.child(0).child(0).text());
    }

    // Tests parseFragment via Parser helper
    @Test
    public void testParseFragment_validFragment_returnsListOfNodes() {
        String fragment = "<one>First</one><two>Second</two>";
        List<Node> nodes = Parser.parseXmlFragment(fragment, "http://example.com/");

        assertEquals(2, nodes.size());
        assertTrue(nodes.get(0) instanceof Element);
        assertEquals("one", ((Element) nodes.get(0)).tagName());
        assertEquals("First", ((Element) nodes.get(0)).text());
        assertTrue(nodes.get(1) instanceof Element);
        assertEquals("two", ((Element) nodes.get(1)).tagName());
        assertEquals("Second", ((Element) nodes.get(1)).text());
    }

    // Tests parseFragment via XmlTreeBuilder directly
    @Test
    public void testParseFragment_viaXmlTreeBuilder_returnsNodes() {
        XmlTreeBuilder tb = new XmlTreeBuilder();
        List<Node> nodes = tb.parseFragment("<item id=\"1\"/><item id=\"2\"/>", "http://example.com/", Parser.xmlParser());

        assertEquals(2, nodes.size());
        assertEquals("item", ((Element) nodes.get(0)).tagName());
        assertEquals("1", nodes.get(0).attr("id"));
        assertEquals("2", nodes.get(1).attr("id"));
    }

    // Tests XML declaration node creation
    @Test
    public void testParse_xmlDeclarationNode_createsXmlDeclarationInstance() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><root/>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());

        List<Node> childNodes = doc.childNodes();
        assertTrue(childNodes.size() >= 2);
        assertTrue(childNodes.get(0) instanceof XmlDeclaration);
        XmlDeclaration decl = (XmlDeclaration) childNodes.get(0);
        assertEquals("xml", decl.name());
        assertEquals("1.0", decl.attr("version"));
        assertEquals("UTF-8", decl.attr("encoding"));
        assertEquals("yes", decl.attr("standalone"));
    }

    // Tests processing instruction style declaration
    @Test
    public void testParse_processingInstruction_createsXmlDeclarationInstance() {
        String xml = "<?xml-stylesheet type=\"text/xsl\" href=\"style.xsl\"?><root/>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());

        assertTrue(doc.childNode(0) instanceof XmlDeclaration);
        XmlDeclaration decl = (XmlDeclaration) doc.childNode(0);
        assertEquals("xml-stylesheet", decl.name());
        assertEquals("text/xsl", decl.attr("type"));
        assertEquals("style.xsl", decl.attr("href"));
    }

    // Tests premature ancestor closing pops unclosed inner children
    @Test
    public void testParse_ancestorClosedEarly_popsInnerElements() {
        String xml = "<parent><child><grandchild></parent><sibling/>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());

        assertEquals(2, doc.children().size());
        Element parent = doc.child(0);
        assertEquals("parent", parent.tagName());
        assertEquals(1, parent.children().size());
        assertEquals("child", parent.child(0).tagName());
        assertEquals("grandchild", parent.child(0).child(0).tagName());

        Element sibling = doc.child(1);
        assertEquals("sibling", sibling.tagName());
    }

    // Tests closing tag on empty stack / root level without open match
    @Test
    public void testParse_orphanClosingTag_ignoredSafely() {
        String xml = "</orphan><root>content</root></extra>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());

        assertEquals(1, doc.children().size());
        assertEquals("root", doc.child(0).tagName());
        assertEquals("content", doc.child(0).text());
    }

    // Tests defaultSettings returns preserveCase settings
    @Test
    public void testDefaultSettings_preservesCase() {
        XmlTreeBuilder tb = new XmlTreeBuilder();
        ParseSettings settings = tb.defaultSettings();

        assertNotNull(settings);
        assertEquals("CamelCaseTag", settings.normalizeTag("CamelCaseTag"));
        assertEquals("AttributeName", settings.normalizeAttribute("AttributeName"));
    }
}