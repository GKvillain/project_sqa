package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.DocumentType;
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

    // Tests parsing simple XML with root and child elements
    @Test
    public void testParse_simpleXml_createsCorrectStructure() {
        String xml = "<root><child>Hello</child></root>";
        Document doc = treeBuilder.parse(xml, "http://example.com/");

        assertNotNull(doc);
        assertEquals(1, doc.children().size());
        Element root = doc.child(0);
        assertEquals("root", root.tagName());
        assertEquals(1, root.children().size());
        Element child = root.child(0);
        assertEquals("child", child.tagName());
        assertEquals("Hello", child.text());
    }

    // Tests self-closing tag handling for unknown XML tags
    @Test
    public void testParse_selfClosingUnknownTag_treatedAsSelfClosing() {
        String xml = "<root><custom id=\"1\"/><other>content</other></root>";
        Document doc = treeBuilder.parse(xml, "http://example.com/");

        Element root = doc.child(0);
        assertEquals(2, root.children().size());
        Element custom = root.child(0);
        assertEquals("custom", custom.tagName());
        assertEquals("1", custom.attr("id"));
        assertEquals(0, custom.children().size());
        Element other = root.child(1);
        assertEquals("other", other.tagName());
        assertEquals("content", other.text());
    }

    // Tests self-closing tag handling for known HTML tags in XML mode
    @Test
    public void testParse_selfClosingKnownTag_preservedCorrectly() {
        String xml = "<root><img src=\"test.jpg\"/><p>Text</p></root>";
        Document doc = treeBuilder.parse(xml, "http://example.com/");

        Element root = doc.child(0);
        assertEquals(2, root.children().size());
        Element img = root.child(0);
        assertEquals("img", img.tagName());
        assertEquals("test.jpg", img.attr("src"));
    }

    // Tests comment insertion in XML tree
    @Test
    public void testParse_xmlComment_insertsCommentNode() {
        String xml = "<root><!-- This is a comment --><child/></root>";
        Document doc = treeBuilder.parse(xml, "http://example.com/");

        Element root = doc.child(0);
        List<Node> childNodes = root.childNodes();
        assertTrue(childNodes.size() >= 2);
        assertTrue(childNodes.get(0) instanceof Comment);
        Comment comment = (Comment) childNodes.get(0);
        assertEquals(" This is a comment ", comment.getData());
    }

    // Tests doctype insertion in XML tree
    @Test
    public void testParse_doctypeDeclaration_insertsDocTypeNode() {
        String xml = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\"><root/>";
        Document doc = treeBuilder.parse(xml, "http://example.com/");

        assertNotNull(doc);
        assertTrue(doc.childNode(0) instanceof DocumentType);
        DocumentType doctype = (DocumentType) doc.childNode(0);
        assertEquals("html", doctype.attr("name"));
        assertEquals("-//W3C//DTD XHTML 1.0 Strict//EN", doctype.attr("publicId"));
        assertEquals("http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd", doctype.attr("systemId"));
    }

    // Tests popStackToClose when encountering out-of-order/nested closing tags
    @Test
    public void testPopStackToClose_nestedUnclosedElements_popsUpToMatchingTag() {
        String xml = "<a><b><c>test</a>";
        Document doc = treeBuilder.parse(xml, "http://example.com/");

        Element rootA = doc.child(0);
        assertEquals("a", rootA.tagName());
        Element b = rootA.child(0);
        assertEquals("b", b.tagName());
        Element c = b.child(0);
        assertEquals("c", c.tagName());
        assertEquals("test", c.text());
    }

    // Tests popStackToClose when encountering an end tag that does not exist in the stack
    @Test
    public void testPopStackToClose_nonExistentEndTag_safelyIgnored() {
        String xml = "<root><child>text</unknown></child></root>";
        Document doc = treeBuilder.parse(xml, "http://example.com/");

        Element root = doc.child(0);
        assertEquals("root", root.tagName());
        Element child = root.child(0);
        assertEquals("child", child.tagName());
        assertEquals("text", child.text());
    }

    // Tests case-preservation and case-sensitivity of XML element names
    @Test
    public void testParse_caseSensitiveElements_treatedAsDistinctTags() {
        String xml = "<Root><item>1</item><Item>2</Item><ITEM>3</ITEM></Root>";
        Document doc = treeBuilder.parse(xml, "http://example.com/");

        Element root = doc.child(0);
        assertEquals("Root", root.tagName());
        assertEquals(3, root.children().size());
        assertEquals("item", root.child(0).tagName());
        assertEquals("Item", root.child(1).tagName());
        assertEquals("ITEM", root.child(2).tagName());
    }

    // Tests parsing empty XML string
    @Test
    public void testParse_emptyString_createsEmptyDocument() {
        Document doc = treeBuilder.parse("", "http://example.com/");

        assertNotNull(doc);
        assertEquals(0, doc.children().size());
    }

    // Tests parsing XML with character data and whitespace nodes
    @Test
    public void testParse_textAndWhitespace_preservesTextNodes() {
        String xml = "<root> First text <mid>Middle text</mid> Last text </root>";
        Document doc = treeBuilder.parse(xml, "http://example.com/");

        Element root = doc.child(0);
        List<TextNode> textNodes = root.textNodes();
        assertTrue(textNodes.size() >= 2);
        assertTrue(root.text().contains("First text"));
        assertTrue(root.text().contains("Middle text"));
        assertTrue(root.text().contains("Last text"));
    }

    // Tests parsing XML declaration handling
    @Test
    public void testParse_xmlDeclaration_handledProperly() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><data>value</data></root>";
        Document doc = treeBuilder.parse(xml, "http://example.com/");

        Element root = doc.select("root").first();
        assertNotNull(root);
        assertEquals("value", root.select("data").first().text());
    }

    // Tests parseFragment with XML input
    @Test
    public void testParseFragment_validXmlFragment_returnsNodeList() {
        String fragment = "<item id=\"1\">One</item><item id=\"2\">Two</item>";
        List<Node> nodes = treeBuilder.parseFragment(fragment, "http://example.com/", ParseErrorList.noTracking());

        assertNotNull(nodes);
        assertEquals(2, nodes.size());
        assertTrue(nodes.get(0) instanceof Element);
        Element item1 = (Element) nodes.get(0);
        assertEquals("item", item1.tagName());
        assertEquals("1", item1.attr("id"));
        assertEquals("One", item1.text());

        assertTrue(nodes.get(1) instanceof Element);
        Element item2 = (Element) nodes.get(1);
        assertEquals("item", item2.tagName());
        assertEquals("2", item2.attr("id"));
        assertEquals("Two", item2.text());
    }

    // Tests processing EOF token
    @Test
    public void testProcess_eofToken_returnsTrue() {
        treeBuilder.initialiseParse("<root>", "http://example.com/", ParseErrorList.noTracking());
        boolean processed = treeBuilder.process(new Token.EOF());
        assertTrue(processed);
    }

    // Tests handling bogus comment representing an XML declaration starting with !
    @Test
    public void testParse_bogusCommentExclamation_createsXmlDeclaration() {
        String xml = "<!DECL test><root/>";
        Document doc = treeBuilder.parse(xml, "http://example.com/");

        assertNotNull(doc);
        assertTrue(doc.childNode(0) instanceof XmlDeclaration);
        XmlDeclaration decl = (XmlDeclaration) doc.childNode(0);
        assertEquals("DECL test", decl.name());
    }
}