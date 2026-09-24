package org.jsoup.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class ParserTest {

    // Tests static htmlParser factory method
    @Test
    public void testHtmlParser_createsInstanceWithHtmlTreeBuilder() {
        Parser parser = Parser.htmlParser();
        assertNotNull(parser);
        assertTrue(parser.getTreeBuilder() instanceof HtmlTreeBuilder);
        assertFalse(parser.isTrackErrors());
    }

    // Tests static xmlParser factory method
    @Test
    public void testXmlParser_createsInstanceWithXmlTreeBuilder() {
        Parser parser = Parser.xmlParser();
        assertNotNull(parser);
        assertTrue(parser.getTreeBuilder() instanceof XmlTreeBuilder);
        assertFalse(parser.isTrackErrors());
    }

    // Tests static parse method for HTML
    @Test
    public void testParse_validHtml_returnsParsedDocument() {
        String html = "<html><head><title>Test</title></head><body><p>Hello</p></body></html>";
        Document doc = Parser.parse(html, "http://example.com/");
        assertNotNull(doc);
        assertEquals("Test", doc.title());
        assertEquals("Hello", doc.select("p").first().text());
        assertEquals("http://example.com/", doc.baseUri());
    }

    // Tests parseInput with error tracking disabled (default)
    @Test
    public void testParseInput_errorTrackingDisabled_noErrorsRecorded() {
        Parser parser = Parser.htmlParser();
        parser.setTrackErrors(0);
        assertFalse(parser.isTrackErrors());

        Document doc = parser.parseInput("<html><p>Foo</b>", "http://example.com/");
        assertNotNull(doc);
        assertNull(parser.getErrors());
    }

    // Tests parseInput with error tracking enabled
    @Test
    public void testParseInput_errorTrackingEnabled_recordsErrors() {
        Parser parser = Parser.htmlParser();
        parser.setTrackErrors(10);
        assertTrue(parser.isTrackErrors());

        Document doc = parser.parseInput("<html><p>Foo</b>", "http://example.com/");
        assertNotNull(doc);
        List<ParseError> errors = parser.getErrors();
        assertNotNull(errors);
        assertFalse(errors.isEmpty());
    }

    // Tests isTrackErrors boundary with negative, zero, and positive values
    @Test
    public void testSetTrackErrors_variousLimits_correctTrackErrorsState() {
        Parser parser = Parser.htmlParser();
        
        parser.setTrackErrors(-1);
        assertFalse(parser.isTrackErrors());

        parser.setTrackErrors(0);
        assertFalse(parser.isTrackErrors());

        parser.setTrackErrors(1);
        assertTrue(parser.isTrackErrors());

        parser.setTrackErrors(100);
        assertTrue(parser.isTrackErrors());
    }

    // Tests getter and setter for TreeBuilder
    @Test
    public void testSetTreeBuilder_customTreeBuilder_updatesSuccessfully() {
        Parser parser = new Parser(new HtmlTreeBuilder());
        XmlTreeBuilder xmlTreeBuilder = new XmlTreeBuilder();
        Parser returnedParser = parser.setTreeBuilder(xmlTreeBuilder);

        assertSame(parser, returnedParser);
        assertSame(xmlTreeBuilder, parser.getTreeBuilder());
    }

    // Tests settings getter and setter
    @Test
    public void testSettings_customSettings_updatesAndReturnsCorrectly() {
        Parser parser = Parser.htmlParser();
        ParseSettings customSettings = new ParseSettings(true, true);
        Parser returned = parser.settings(customSettings);

        assertSame(parser, returned);
        assertSame(customSettings, parser.settings());
    }

    // Tests parseFragment with HTML context
    @Test
    public void testParseFragment_htmlContext_returnsParsedNodes() {
        Element context = new Element(Tag.valueOf("div"), "");
        List<Node> nodes = Parser.parseFragment("<p>One</p><p>Two</p>", context, "http://example.com/");

        assertNotNull(nodes);
        assertEquals(2, nodes.size());
        assertTrue(nodes.get(0) instanceof Element);
        assertEquals("p", ((Element) nodes.get(0)).tagName());
        assertEquals("One", ((Element) nodes.get(0)).text());
        assertEquals("Two", ((Element) nodes.get(1)).text());
    }

    // Tests parseXmlFragment
    @Test
    public void testParseXmlFragment_validXml_returnsXmlNodes() {
        String xml = "<item id=\"1\">Value</item><item id=\"2\">Value 2</item>";
        List<Node> nodes = Parser.parseXmlFragment(xml, "http://example.com/");

        assertNotNull(nodes);
        assertEquals(2, nodes.size());
        assertTrue(nodes.get(0) instanceof Element);
        Element item1 = (Element) nodes.get(0);
        assertEquals("item", item1.tagName());
        assertEquals("1", item1.attr("id"));
        assertEquals("Value", item1.text());
    }

    // Tests parseBodyFragment with single node
    @Test
    public void testParseBodyFragment_singleElement_attachedToBody() {
        Document doc = Parser.parseBodyFragment("<p>Single Element</p>", "http://example.com/");
        assertNotNull(doc);
        assertNotNull(doc.body());
        assertEquals(1, doc.body().children().size());
        assertEquals("Single Element", doc.body().select("p").text());
    }

    // Tests parseBodyFragment with multiple sibling elements
    @Test
    public void testParseBodyFragment_multipleElements_retainsAllInBody() {
        String html = "<div>First</div><p>Second</p><span>Third</span>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com/");

        assertNotNull(doc);
        assertEquals(3, doc.body().children().size());
        assertEquals("First", doc.body().child(0).text());
        assertEquals("Second", doc.body().child(1).text());
        assertEquals("Third", doc.body().child(2).text());
    }

    // Tests parseBodyFragmentRelaxed deprecated method
    @Test
    @SuppressWarnings("deprecation")
    public void testParseBodyFragmentRelaxed_validHtml_returnsDocument() {
        String html = "<div><p>Relaxed Test</p></div>";
        Document doc = Parser.parseBodyFragmentRelaxed(html, "http://example.com/");

        assertNotNull(doc);
        assertEquals("Relaxed Test", doc.select("p").text());
    }

    // Tests unescapeEntities with inAttribute = false
    @Test
    public void testUnescapeEntities_notInAttribute_unescapesHtmlEntities() {
        String escaped = "&lt;div class=&quot;test&quot;&gt;&amp;&lt;/div&gt;";
        String unescaped = Parser.unescapeEntities(escaped, false);
        assertEquals("<div class=\"test\">&</div>", unescaped);
    }

    // Tests unescapeEntities with inAttribute = true
    @Test
    public void testUnescapeEntities_inAttribute_unescapesAttributeEntities() {
        String escaped = "foo &amp; bar &quot; &lt;";
        String unescaped = Parser.unescapeEntities(escaped, true);
        assertEquals("foo & bar \" <", unescaped);
    }
}