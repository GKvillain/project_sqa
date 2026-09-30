package org.jsoup.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class ParserTest {

    // Tests static htmlParser factory creates Parser with HtmlTreeBuilder
    @Test
    public void testHtmlParser_defaultInstance_usesHtmlTreeBuilder() {
        Parser parser = Parser.htmlParser();
        assertNotNull(parser);
        assertTrue(parser.getTreeBuilder() instanceof HtmlTreeBuilder);
        assertFalse(parser.isTrackErrors());
    }

    // Tests static xmlParser factory creates Parser with XmlTreeBuilder
    @Test
    public void testXmlParser_defaultInstance_usesXmlTreeBuilder() {
        Parser parser = Parser.xmlParser();
        assertNotNull(parser);
        assertTrue(parser.getTreeBuilder() instanceof XmlTreeBuilder);
        assertFalse(parser.isTrackErrors());
    }

    // Tests static parse method with valid HTML
    @Test
    public void testParse_validHtml_returnsDocumentWithContent() {
        String html = "<html><head><title>Test</title></head><body><p>Hello World</p></body></html>";
        Document doc = Parser.parse(html, "http://example.com");
        assertNotNull(doc);
        assertEquals("Test", doc.title());
        assertEquals("Hello World", doc.select("p").text());
        assertEquals("http://example.com", doc.baseUri());
    }

    // Tests static parseBodyFragment creates shell document and populates body
    @Test
    public void testParseBodyFragment_htmlFragment_appendsToBody() {
        String fragment = "<div><span>Text</span></div>";
        Document doc = Parser.parseBodyFragment(fragment, "http://example.com");
        assertNotNull(doc);
        assertNotNull(doc.body());
        assertEquals(1, doc.body().children().size());
        assertEquals("Text", doc.body().select("span").text());
    }

    // Tests static parseBodyFragmentRelaxed delegates to parse
    @Test
    public void testParseBodyFragmentRelaxed_htmlFragment_returnsParsedDocument() {
        String fragment = "<p>Relaxed content</p>";
        Document doc = Parser.parseBodyFragmentRelaxed(fragment, "http://example.com");
        assertNotNull(doc);
        assertEquals("Relaxed content", doc.body().select("p").text());
    }

    // Tests static parseFragment with context element
    @Test
    public void testParseFragment_withContextElement_returnsNodeList() {
        Document doc = Parser.parse("<div></div>", "http://example.com");
        Element context = doc.select("div").first();
        List<Node> nodes = Parser.parseFragment("<span>Item 1</span><span>Item 2</span>", context, "http://example.com");
        assertNotNull(nodes);
        assertEquals(2, nodes.size());
        assertEquals("span", ((Element) nodes.get(0)).tagName());
    }

    // Tests static parseFragment with null context
    @Test
    public void testParseFragment_nullContext_returnsNodeList() {
        List<Node> nodes = Parser.parseFragment("<p>Fragment without context</p>", null, "http://example.com");
        assertNotNull(nodes);
        assertFalse(nodes.isEmpty());
    }

    // Tests isTrackErrors returns false when maxErrors is 0
    @Test
    public void testIsTrackErrors_defaultZero_returnsFalse() {
        Parser parser = new Parser(new HtmlTreeBuilder());
        assertFalse(parser.isTrackErrors());
    }

    // Tests isTrackErrors returns true when maxErrors is positive
    @Test
    public void testIsTrackErrors_positiveValue_returnsTrue() {
        Parser parser = new Parser(new HtmlTreeBuilder());
        parser.setTrackErrors(10);
        assertTrue(parser.isTrackErrors());
    }

    // Tests isTrackErrors boundary condition with negative value
    @Test
    public void testIsTrackErrors_negativeValue_returnsFalse() {
        Parser parser = new Parser(new HtmlTreeBuilder());
        parser.setTrackErrors(-1);
        assertFalse(parser.isTrackErrors());
    }

    // Tests parseInput with error tracking disabled
    @Test
    public void testParseInput_trackingDisabled_noErrorsRecorded() {
        Parser parser = Parser.htmlParser();
        parser.setTrackErrors(0);
        Document doc = parser.parseInput("<html><p>Unclosed", "http://example.com");
        assertNotNull(doc);
        assertNotNull(parser.getErrors());
        assertEquals(0, parser.getErrors().size());
    }

    // Tests parseInput with error tracking enabled records parse errors
    @Test
    public void testParseInput_trackingEnabled_recordsErrors() {
        Parser parser = Parser.htmlParser();
        parser.setTrackErrors(5);
        Document doc = parser.parseInput("<html><p>Foo<b>Bar</p></b></html>", "http://example.com");
        assertNotNull(doc);
        assertNotNull(parser.getErrors());
        assertFalse(parser.getErrors().isEmpty());
    }

    // Tests getter and setter for TreeBuilder
    @Test
    public void testGetSetTreeBuilder_customTreeBuilder_updatesCorrectly() {
        Parser parser = new Parser(new HtmlTreeBuilder());
        XmlTreeBuilder xmlTreeBuilder = new XmlTreeBuilder();
        Parser returnedParser = parser.setTreeBuilder(xmlTreeBuilder);
        assertSame(parser, returnedParser);
        assertSame(xmlTreeBuilder, parser.getTreeBuilder());
    }

    // Tests parseInput using XmlTreeBuilder
    @Test
    public void testParseInput_xmlTreeBuilder_parsesXmlStructure() {
        Parser parser = Parser.xmlParser();
        String xml = "<root><child attr=\"value\">Content</child></root>";
        Document doc = parser.parseInput(xml, "http://example.com");
        assertNotNull(doc);
        assertEquals("Content", doc.select("child").text());
        assertEquals("value", doc.select("child").attr("attr"));
    }

    // Tests parse handling HTML entities inside document
    @Test
    public void testParse_htmlEntities_parsesAndDecodes() {
        String html = "<p>&amp; &lt; &gt; &quot;</p>";
        Document doc = Parser.parse(html, "http://example.com");
        assertEquals("& < > \"", doc.select("p").text());
    }

    // Tests static parseXmlFragment method parses XML fragment into node list
    @Test
    public void testParseXmlFragment_validXml_returnsNodeList() {
        List<Node> nodes = Parser.parseXmlFragment("<item id=\"1\">Foo</item><item id=\"2\">Bar</item>", "http://example.com");
        assertNotNull(nodes);
        assertEquals(2, nodes.size());
        assertTrue(nodes.get(0) instanceof Element);
        Element first = (Element) nodes.get(0);
        assertEquals("item", first.tagName());
        assertEquals("Foo", first.text());
        assertEquals("1", first.attr("id"));
    }

    // Tests static unescapeEntities method outside attribute context
    @Test
    public void testUnescapeEntities_notInAttribute_decodesProperly() {
        String input = "&lt;div&gt;&amp;&quot;&apos;&nbsp;&copy;&lt;/div&gt;";
        String unescaped = Parser.unescapeEntities(input, false);
        assertEquals("<div>&\"'\u00a0\u00a9</div>", unescaped);
    }

    // Tests static unescapeEntities method inside attribute context
    @Test
    public void testUnescapeEntities_inAttribute_decodesProperly() {
        String input = "test&amp;value&quot;&apos;&lt;&gt;";
        String unescaped = Parser.unescapeEntities(input, true);
        assertEquals("test&value\"'<script>", unescaped.replace("<script>", "<>"));
        assertEquals("test&value\"'<>", unescaped);
    }

    // Tests getter and setter for ParseSettings
    @Test
    public void testSettings_getAndSet_updatesSettings() {
        Parser parser = Parser.htmlParser();
        assertNotNull(parser.settings());

        ParseSettings customSettings = new ParseSettings(true, true);
        Parser returned = parser.settings(customSettings);
        assertSame(parser, returned);
        assertSame(customSettings, parser.settings());
    }

    // Tests setTrackErrors method chaining returns same Parser instance
    @Test
    public void testSetTrackErrors_chaining_returnsSameParser() {
        Parser parser = Parser.htmlParser();
        Parser returned = parser.setTrackErrors(20);
        assertSame(parser, returned);
        assertTrue(parser.isTrackErrors());
    }
}