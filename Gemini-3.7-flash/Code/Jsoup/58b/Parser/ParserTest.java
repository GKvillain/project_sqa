package org.jsoup.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class ParserTest {

    // Tests static htmlParser factory method creates instance with HtmlTreeBuilder
    @Test
    public void testHtmlParser_creation_returnsHtmlParserInstance() {
        Parser parser = Parser.htmlParser();
        assertNotNull(parser);
        assertTrue(parser.getTreeBuilder() instanceof HtmlTreeBuilder);
        assertFalse(parser.isTrackErrors());
        assertEquals(ParseSettings.htmlDefault, parser.settings());
    }

    // Tests static xmlParser factory method creates instance with XmlTreeBuilder
    @Test
    public void testXmlParser_creation_returnsXmlParserInstance() {
        Parser parser = Parser.xmlParser();
        assertNotNull(parser);
        assertTrue(parser.getTreeBuilder() instanceof XmlTreeBuilder);
        assertFalse(parser.isTrackErrors());
        assertEquals(ParseSettings.preserveCase, parser.settings());
    }

    // Tests direct Parser constructor
    @Test
    public void testParserConstructor_withTreeBuilder_initializesProperly() {
        TreeBuilder treeBuilder = new HtmlTreeBuilder();
        Parser parser = new Parser(treeBuilder);
        assertSame(treeBuilder, parser.getTreeBuilder());
        assertFalse(parser.isTrackErrors());
        assertEquals(ParseSettings.htmlDefault, parser.settings());
    }

    // Tests parseInput with error tracking disabled (default false branch of isTrackErrors)
    @Test
    public void testParseInput_errorTrackingDisabled_parsesSuccessfullyWithoutErrors() {
        Parser parser = Parser.htmlParser();
        Document doc = parser.parseInput("<p>Hello</p>", "http://example.com");
        assertNotNull(doc);
        assertEquals("Hello", doc.select("p").text());
        assertNotNull(parser.getErrors());
        assertEquals(0, parser.getErrors().size());
    }

    // Tests parseInput with error tracking enabled (true branch of isTrackErrors)
    @Test
    public void testParseInput_errorTrackingEnabled_tracksParseErrors() {
        Parser parser = Parser.htmlParser().setTrackErrors(10);
        assertTrue(parser.isTrackErrors());
        Document doc = parser.parseInput("<p>One<p>Two", "http://example.com");
        assertNotNull(doc);
        assertNotNull(parser.getErrors());
        assertFalse(parser.getErrors().isEmpty());
    }

    // Tests instance parseFragmentInput method with error tracking disabled
    @Test
    public void testParseFragmentInput_errorTrackingDisabled_returnsNodes() {
        Parser parser = Parser.htmlParser();
        Element context = new Element("div");
        List<Node> nodes = parser.parseFragmentInput("<p>Fragment Text</p>", context, "http://example.com/");
        assertEquals(1, nodes.size());
        assertTrue(nodes.get(0) instanceof Element);
        assertEquals("Fragment Text", ((Element) nodes.get(0)).text());
        assertNotNull(parser.getErrors());
        assertEquals(0, parser.getErrors().size());
    }

    // Tests instance parseFragmentInput method with error tracking enabled
    @Test
    public void testParseFragmentInput_errorTrackingEnabled_tracksErrors() {
        Parser parser = Parser.htmlParser().setTrackErrors(10);
        Element context = new Element("div");
        List<Node> nodes = parser.parseFragmentInput("<p>One<p>Two", context, "http://example.com/");
        assertFalse(nodes.isEmpty());
        assertNotNull(parser.getErrors());
        assertFalse(parser.getErrors().isEmpty());
    }

    // Tests setTrackErrors boundary condition when set to 0
    @Test
    public void testSetTrackErrors_zero_disablesTracking() {
        Parser parser = Parser.htmlParser();
        parser.setTrackErrors(5);
        assertTrue(parser.isTrackErrors());
        parser.setTrackErrors(0);
        assertFalse(parser.isTrackErrors());
    }

    // Tests setTrackErrors boundary condition when set to negative value
    @Test
    public void testSetTrackErrors_negative_disablesTracking() {
        Parser parser = Parser.htmlParser();
        parser.setTrackErrors(-1);
        assertFalse(parser.isTrackErrors());
    }

    // Tests treeBuilder getter and setter
    @Test
    public void testSetTreeBuilder_customTreeBuilder_updatesTreeBuilder() {
        Parser parser = Parser.htmlParser();
        TreeBuilder xmlTreeBuilder = new XmlTreeBuilder();
        Parser returnedParser = parser.setTreeBuilder(xmlTreeBuilder);
        assertSame(parser, returnedParser);
        assertSame(xmlTreeBuilder, parser.getTreeBuilder());
    }

    // Tests settings getter and setter
    @Test
    public void testSettings_customSettings_updatesSettings() {
        Parser parser = Parser.htmlParser();
        ParseSettings customSettings = new ParseSettings(true, true);
        Parser returnedParser = parser.settings(customSettings);
        assertSame(parser, returnedParser);
        assertSame(customSettings, parser.settings());
    }

    // Tests static parse method with valid HTML
    @Test
    public void testParse_validHtml_returnsParsedDocument() {
        Document doc = Parser.parse("<div id='test'>Content</div>", "http://example.com/");
        assertNotNull(doc);
        assertEquals("Content", doc.getElementById("test").text());
        assertEquals("http://example.com/", doc.baseUri());
    }

    // Tests static parseFragment method with context element
    @Test
    public void testParseFragment_htmlWithContext_returnsNodesList() {
        Element context = new Element("div");
        List<Node> nodes = Parser.parseFragment("<span>Span1</span><span>Span2</span>", context, "http://example.com/");
        assertEquals(2, nodes.size());
        assertTrue(nodes.get(0) instanceof Element);
        assertEquals("span", ((Element) nodes.get(0)).tagName());
        assertEquals("Span1", ((Element) nodes.get(0)).text());
    }

    // Tests static parseXmlFragment method
    @Test
    public void testParseXmlFragment_validXml_returnsXmlNodes() {
        List<Node> nodes = Parser.parseXmlFragment("<custom id='1'>Text</custom><custom id='2'/>", "http://example.com/");
        assertEquals(2, nodes.size());
        assertTrue(nodes.get(0) instanceof Element);
        Element el = (Element) nodes.get(0);
        assertEquals("custom", el.tagName());
        assertEquals("1", el.attr("id"));
        assertEquals("Text", el.text());
    }

    // Tests static parseBodyFragment method with multiple elements
    @Test
    public void testParseBodyFragment_multipleNodes_parsedIntoBody() {
        Document doc = Parser.parseBodyFragment("<p>Paragraph 1</p><p>Paragraph 2</p>", "http://example.com/");
        assertNotNull(doc.body());
        assertEquals(2, doc.body().children().size());
        assertEquals("Paragraph 1", doc.body().child(0).text());
        assertEquals("Paragraph 2", doc.body().child(1).text());
    }

    // Tests static parseBodyFragment method with single text node
    @Test
    public void testParseBodyFragment_singleTextNode_parsedIntoBody() {
        Document doc = Parser.parseBodyFragment("Just text", "http://example.com/");
        assertNotNull(doc.body());
        assertEquals(1, doc.body().childNodeSize());
        assertTrue(doc.body().childNode(0) instanceof TextNode);
        assertEquals("Just text", ((TextNode) doc.body().childNode(0)).text());
    }

    // Tests static parseBodyFragmentRelaxed deprecated method
    @Test
    public void testParseBodyFragmentRelaxed_validHtml_returnsDocument() {
        Document doc = Parser.parseBodyFragmentRelaxed("<div>Relaxed</div>", "http://example.com/");
        assertNotNull(doc);
        assertEquals("Relaxed", doc.select("div").text());
    }

    // Tests static unescapeEntities outside attribute mode
    @Test
    public void testUnescapeEntities_notInAttribute_unescapesCorrectly() {
        String unescaped = Parser.unescapeEntities("&lt;tag&gt; &amp; &quot;", false);
        assertEquals("<tag> & \"", unescaped);
    }

    // Tests static unescapeEntities in attribute mode
    @Test
    public void testUnescapeEntities_inAttribute_unescapesCorrectly() {
        String unescaped = Parser.unescapeEntities("&quot;test&amp;&quot;", true);
        assertEquals("\"test&\"", unescaped);
    }
}