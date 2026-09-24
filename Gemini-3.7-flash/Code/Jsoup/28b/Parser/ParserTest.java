package org.jsoup.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class ParserTest {

    // Tests creation of htmlParser builder
    @Test
    public void testHtmlParser_initialization_returnsParserWithHtmlTreeBuilder() {
        Parser parser = Parser.htmlParser();
        assertNotNull(parser);
        assertTrue(parser.getTreeBuilder() instanceof HtmlTreeBuilder);
        assertFalse(parser.isTrackErrors());
    }

    // Tests creation of xmlParser builder
    @Test
    public void testXmlParser_initialization_returnsParserWithXmlTreeBuilder() {
        Parser parser = Parser.xmlParser();
        assertNotNull(parser);
        assertTrue(parser.getTreeBuilder() instanceof XmlTreeBuilder);
        assertFalse(parser.isTrackErrors());
    }

    // Tests getter and setter for TreeBuilder
    @Test
    public void testSetTreeBuilder_customTreeBuilder_updatesTreeBuilder() {
        Parser parser = Parser.htmlParser();
        TreeBuilder xmlTreeBuilder = new XmlTreeBuilder();
        Parser returnedParser = parser.setTreeBuilder(xmlTreeBuilder);

        assertSame(parser, returnedParser);
        assertSame(xmlTreeBuilder, parser.getTreeBuilder());
    }

    // Tests default state of error tracking
    @Test
    public void testIsTrackErrors_defaultState_returnsFalse() {
        Parser parser = Parser.htmlParser();
        assertFalse(parser.isTrackErrors());
        assertNull(parser.getErrors());
    }

    // Tests enabling error tracking with positive limit
    @Test
    public void testSetTrackErrors_positiveValue_enablesErrorTracking() {
        Parser parser = Parser.htmlParser();
        Parser returned = parser.setTrackErrors(50);

        assertSame(parser, returned);
        assertTrue(parser.isTrackErrors());
    }

    // Tests disabling error tracking with zero value
    @Test
    public void testSetTrackErrors_zeroValue_disablesErrorTracking() {
        Parser parser = Parser.htmlParser();
        parser.setTrackErrors(10);
        assertTrue(parser.isTrackErrors());

        parser.setTrackErrors(0);
        assertFalse(parser.isTrackErrors());
    }

    // Tests error tracking behavior with negative value
    @Test
    public void testSetTrackErrors_negativeValue_disablesErrorTracking() {
        Parser parser = Parser.htmlParser();
        parser.setTrackErrors(-1);
        assertFalse(parser.isTrackErrors());
    }

    // Tests parseInput with error tracking enabled for malformed HTML
    @Test
    public void testParseInput_withErrorTracking_recordsErrors() {
        Parser parser = Parser.htmlParser().setTrackErrors(10);
        String malformedHtml = "<html><head><title>Test</head><body><p>Unclosed paragraph";
        Document doc = parser.parseInput(malformedHtml, "http://example.com");

        assertNotNull(doc);
        assertNotNull(parser.getErrors());
        assertFalse(parser.getErrors().isEmpty());
    }

    // Tests parseInput with error tracking disabled
    @Test
    public void testParseInput_withoutErrorTracking_errorsListIsEmpty() {
        Parser parser = Parser.htmlParser().setTrackErrors(0);
        String html = "<p>Valid HTML</p>";
        Document doc = parser.parseInput(html, "http://example.com");

        assertNotNull(doc);
        assertNotNull(parser.getErrors());
        assertEquals(0, parser.getErrors().size());
    }

    // Tests static parse method
    @Test
    public void testParse_standardHtml_returnsPopulatedDocument() {
        String html = "<html><head><title>Sample</title></head><body><p>Hello World</p></body></html>";
        Document doc = Parser.parse(html, "http://example.com");

        assertNotNull(doc);
        assertEquals("Sample", doc.title());
        assertEquals("Hello World", doc.select("p").first().text());
        assertEquals("http://example.com", doc.baseUri());
    }

    // Tests static parseFragment with context element
    @Test
    public void testParseFragment_withContextElement_returnsNodeList() {
        Element context = new Element(Tag.valueOf("div"), "");
        String fragmentHtml = "<p>Inner text</p><span>More</span>";
        List<Node> nodes = Parser.parseFragment(fragmentHtml, context, "http://example.com");

        assertNotNull(nodes);
        assertEquals(2, nodes.size());
        assertTrue(nodes.get(0) instanceof Element);
        assertEquals("p", ((Element) nodes.get(0)).tagName());
        assertEquals("span", ((Element) nodes.get(1)).tagName());
    }

    // Tests static parseBodyFragment method
    @Test
    public void testParseBodyFragment_validFragment_appendsNodesToBody() {
        String bodyHtml = "<div><p>Fragment Paragraph</p></div>";
        Document doc = Parser.parseBodyFragment(bodyHtml, "http://example.com");

        assertNotNull(doc);
        assertNotNull(doc.body());
        assertEquals(1, doc.body().children().size());
        assertEquals("div", doc.body().child(0).tagName());
        assertEquals("Fragment Paragraph", doc.body().select("p").first().text());
    }

    // Tests deprecated parseBodyFragmentRelaxed method
    @Test
    @SuppressWarnings("deprecation")
    public void testParseBodyFragmentRelaxed_validHtml_returnsDocument() {
        String html = "<p>Relaxed</p>";
        Document doc = Parser.parseBodyFragmentRelaxed(html, "http://example.com");

        assertNotNull(doc);
        assertEquals("Relaxed", doc.select("p").first().text());
    }

    // Tests XML parsing mode via xmlParser
    @Test
    public void testParseInput_xmlParser_preservesXmlTags() {
        Parser parser = Parser.xmlParser();
        String xml = "<custom-tag id=\"1\"><nested>Value</nested></custom-tag>";
        Document doc = parser.parseInput(xml, "http://example.com");

        assertNotNull(doc);
        assertEquals("custom-tag", doc.child(0).tagName());
        assertEquals("Value", doc.select("nested").first().text());
    }

    // Tests static parseXmlFragment method
    @Test
    public void testParseXmlFragment_validXmlFragment_returnsNodeList() {
        String xmlFragment = "<entry id=\"1\">first</entry><entry id=\"2\">second</entry>";
        List<Node> nodes = Parser.parseXmlFragment(xmlFragment, "http://example.com");

        assertNotNull(nodes);
        assertEquals(2, nodes.size());
        assertTrue(nodes.get(0) instanceof Element);
        assertEquals("entry", ((Element) nodes.get(0)).tagName());
        assertEquals("first", ((Element) nodes.get(0)).text());
        assertEquals("entry", ((Element) nodes.get(1)).tagName());
        assertEquals("second", ((Element) nodes.get(1)).text());
    }

    // Tests static unescapeEntities method with inAttribute flag
    @Test
    public void testUnescapeEntities_inAttributeAndNotInAttribute() {
        String text = "&lt;hello &amp; &quot;world&quot;&gt;";
        String unescaped = Parser.unescapeEntities(text, false);
        assertEquals("<hello & \"world\">", unescaped);

        String attrText = "&lt;value&quot;&amp;&apos;&gt;";
        String unescapedAttr = Parser.unescapeEntities(attrText, true);
        assertEquals("<value\"&'>", unescapedAttr);
    }

    // Tests instance parseFragmentInput method directly
    @Test
    public void testParseFragmentInput_htmlTreeBuilder_parsesWithinContext() {
        Parser parser = Parser.htmlParser();
        Element context = new Element(Tag.valueOf("tbody"), "");
        List<Node> nodes = parser.parseFragmentInput("<tr><td>cell</td></tr>", context, "http://example.com");

        assertNotNull(nodes);
        assertFalse(nodes.isEmpty());
        assertEquals("tr", ((Element) nodes.get(0)).tagName());
    }
}