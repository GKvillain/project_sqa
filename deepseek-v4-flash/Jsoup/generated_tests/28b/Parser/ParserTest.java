package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import java.util.List;

public class ParserTest {

    // Normal case: basic HTML parse returns Document with body text
    @Test
    public void testParseInput_basicHtml_returnsDocument() {
        Parser parser = new Parser(new HtmlTreeBuilder());
        Document doc = parser.parseInput("<html><body><p>Hello</p></body></html>", "http://example.com");
        assertNotNull(doc);
        assertEquals("Hello", doc.body().text());
    }

    // Normal case: empty HTML returns Document with empty body
    @Test
    public void testParseInput_emptyHtml_returnsDocumentWithEmptyBody() {
        Parser parser = new Parser(new HtmlTreeBuilder());
        Document doc = parser.parseInput("", "http://example.com");
        assertNotNull(doc);
        assertNotNull(doc.body());
        assertEquals(0, doc.body().children().size());
    }

    // Branch false: track errors off (maxErrors = 0) -> errors list is noTracking
    @Test
    public void testParseInput_trackErrorsOff_errorsListIsNoTracking() {
        Parser parser = new Parser(new HtmlTreeBuilder());
        parser.setTrackErrors(0);
        parser.parseInput("<div>", "http://base");
        List<ParseError> errors = parser.getErrors();
        assertNotNull(errors);
        assertTrue(errors.isEmpty());
    }

    // Branch true: track errors on (maxErrors > 0) -> errors list is tracking
    @Test
    public void testParseInput_trackErrorsOn_errorsListIsTracking() {
        Parser parser = new Parser(new HtmlTreeBuilder());
        parser.setTrackErrors(5);
        parser.parseInput("<div>", "http://base");
        List<ParseError> errors = parser.getErrors();
        assertNotNull(errors);
    }

    // Boundary: setTrackErrors(0) -> isTrackErrors false
    @Test
    public void testIsTrackErrors_zero_returnsFalse() {
        Parser parser = new Parser(new HtmlTreeBuilder());
        parser.setTrackErrors(0);
        assertFalse(parser.isTrackErrors());
    }

    // Boundary: setTrackErrors(1) -> isTrackErrors true (positive min)
    @Test
    public void testIsTrackErrors_positive_returnsTrue() {
        Parser parser = new Parser(new HtmlTreeBuilder());
        parser.setTrackErrors(1);
        assertTrue(parser.isTrackErrors());
    }

    // Edge: negative value -> isTrackErrors false
    @Test
    public void testIsTrackErrors_negative_returnsFalse() {
        Parser parser = new Parser(new HtmlTreeBuilder());
        parser.setTrackErrors(-5);
        assertFalse(parser.isTrackErrors());
    }

    // Edge: maximum int value
    @Test
    public void testSetTrackErrors_maxValue_setsCorrectly() {
        Parser parser = new Parser(new HtmlTreeBuilder());
        parser.setTrackErrors(Integer.MAX_VALUE);
        assertTrue(parser.isTrackErrors());
    }

    // Chaining: setTreeBuilder returns this
    @Test
    public void testSetTreeBuilder_returnsParser() {
        Parser parser = new Parser(new HtmlTreeBuilder());
        Parser returned = parser.setTreeBuilder(new XmlTreeBuilder());
        assertSame(parser, returned);
    }

    // Normal case: getTreeBuilder returns initial builder
    @Test
    public void testGetTreeBuilder_initialHtml_returnsHtmlTreeBuilder() {
        Parser parser = new Parser(new HtmlTreeBuilder());
        assertTrue(parser.getTreeBuilder() instanceof HtmlTreeBuilder);
    }

    // Static factory: htmlParser
    @Test
    public void testHtmlParser_static_createsParserWithHtmlTreeBuilder() {
        Parser parser = Parser.htmlParser();
        assertTrue(parser.getTreeBuilder() instanceof HtmlTreeBuilder);
    }

    // Static factory: xmlParser
    @Test
    public void testXmlParser_static_createsParserWithXmlTreeBuilder() {
        Parser parser = Parser.xmlParser();
        assertTrue(parser.getTreeBuilder() instanceof XmlTreeBuilder);
    }

    // Static parse method
    @Test
    public void testParse_static_returnsDocument() {
        Document doc = Parser.parse("<p>Hello</p>", "http://base");
        assertNotNull(doc);
        assertEquals("Hello", doc.body().text());
    }

    // Static parseFragment
    @Test
    public void testParseFragment_static_returnsNodeList() {
        Document doc = Document.createShell("http://base");
        Element body = doc.body();
        List<Node> nodes = Parser.parseFragment("<b>bold</b>", body, "http://base");
        assertNotNull(nodes);
        assertEquals(1, nodes.size());
        Element b = (Element) nodes.get(0);
        assertEquals("b", b.tagName());
        assertEquals("bold", b.text());
    }

    // Static parseBodyFragment
    @Test
    public void testParseBodyFragment_static_returnsDocumentWithBodyContent() {
        Document doc = Parser.parseBodyFragment("<p>content</p>", "http://base");
        assertNotNull(doc);
        Element body = doc.body();
        assertEquals(1, body.children().size());
        assertEquals("p", body.child(0).tagName());
        assertEquals("content", body.child(0).text());
    }

    // Deprecated parseBodyFragmentRelaxed
    @Test
    public void testParseBodyFragmentRelaxed_static_returnsDocument() {
        Document doc = Parser.parseBodyFragmentRelaxed("<div>test</div>", "http://base");
        assertNotNull(doc);
        assertNotNull(doc.body());
    }

    // Regression: getErrors returns new list after each parse
    @Test
    public void testGetErrors_afterMultipleParses_reflectsLastParse() {
        Parser parser = new Parser(new HtmlTreeBuilder());
        parser.setTrackErrors(3);
        parser.parseInput("<a>", "base1");
        List<ParseError> errors1 = parser.getErrors();
        parser.parseInput("<b>", "base2");
        List<ParseError> errors2 = parser.getErrors();
        assertNotSame(errors1, errors2);
    }

    // New tests for uncovered parts

    // Test setTreeBuilder with null
    @Test
    public void testSetTreeBuilder_null_throwsException() {
        Parser parser = new Parser(new HtmlTreeBuilder());
        try {
            parser.setTreeBuilder(null);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    // Test parseInput with null baseUri
    @Test
    public void testParseInput_nullBaseUri_returnsDocument() {
        Parser parser = new Parser(new HtmlTreeBuilder());
        Document doc = parser.parseInput("<p>test</p>", null);
        assertNotNull(doc);
        assertEquals("test", doc.body().text());
    }

    // Test parseFragment with empty input
    @Test
    public void testParseFragment_emptyInput_returnsEmptyList() {
        Document doc = Document.createShell("http://base");
        Element body = doc.body();
        List<Node> nodes = Parser.parseFragment("", body, "http://base");
        assertNotNull(nodes);
        assertTrue(nodes.isEmpty());
    }

    // Test parseBodyFragment with empty input
    @Test
    public void testParseBodyFragment_emptyInput_returnsEmptyBody() {
        Document doc = Parser.parseBodyFragment("", "http://base");
        assertNotNull(doc);
        assertNotNull(doc.body());
        assertEquals(0, doc.body().children().size());
    }

    // Test parseBodyFragmentRelaxed with empty input
    @Test
    public void testParseBodyFragmentRelaxed_emptyInput_returnsDocument() {
        Document doc = Parser.parseBodyFragmentRelaxed("", "http://base");
        assertNotNull(doc);
        assertNotNull(doc.body());
        assertEquals(0, doc.body().children().size());
    }

    // Test getTreeBuilder after setTreeBuilder
    @Test
    public void testGetTreeBuilder_afterSet_returnsUpdatedBuilder() {
        Parser parser = new Parser(new HtmlTreeBuilder());
        XmlTreeBuilder xmlBuilder = new XmlTreeBuilder();
        parser.setTreeBuilder(xmlBuilder);
        assertSame(xmlBuilder, parser.getTreeBuilder());
    }

    // Test static parse with null baseUri
    @Test
    public void testParse_static_nullBaseUri_returnsDocument() {
        Document doc = Parser.parse("<p>Hello</p>", null);
        assertNotNull(doc);
        assertEquals("Hello", doc.body().text());
    }

    // Test static parseFragment with null baseUri
    @Test
    public void testParseFragment_static_nullBaseUri_returnsNodeList() {
        Document doc = Document.createShell("http://base");
        Element body = doc.body();
        List<Node> nodes = Parser.parseFragment("<b>bold</b>", body, null);
        assertNotNull(nodes);
        assertEquals(1, nodes.size());
        Element b = (Element) nodes.get(0);
        assertEquals("b", b.tagName());
    }

    // Test parseBodyFragment with null baseUri
    @Test
    public void testParseBodyFragment_static_nullBaseUri_returnsDocument() {
        Document doc = Parser.parseBodyFragment("<p>content</p>", null);
        assertNotNull(doc);
        Element body = doc.body();
        assertEquals(1, body.children().size());
        assertEquals("p", body.child(0).tagName());
    }

    // Test parseInput with malformed HTML still returns document
    @Test
    public void testParseInput_malformedHtml_returnsDocument() {
        Parser parser = new Parser(new HtmlTreeBuilder());
        Document doc = parser.parseInput("<div><span>unclosed", "http://base");
        assertNotNull(doc);
        assertNotNull(doc.body());
    }

    // Test getErrors with trackErrors enabled and malformed HTML
    @Test
    public void testGetErrors_trackErrorsWithMalformedHtml_containsErrors() {
        Parser parser = new Parser(new HtmlTreeBuilder());
        parser.setTrackErrors(10);
        parser.parseInput("<div><span></div>", "http://base");
        List<ParseError> errors = parser.getErrors();
        assertNotNull(errors);
        assertFalse(errors.isEmpty());
    }

    // Test setTrackErrors with negative value and check errors after parse
    @Test
    public void testSetTrackErrors_negative_errorsListEmptyAfterParse() {
        Parser parser = new Parser(new HtmlTreeBuilder());
        parser.setTrackErrors(-1);
        parser.parseInput("<div>", "http://base");
        List<ParseError> errors = parser.getErrors();
        assertNotNull(errors);
        assertTrue(errors.isEmpty());
    }

    // Test isTrackErrors after setting negative
    @Test
    public void testIsTrackErrors_afterNegativeSet_returnsFalse() {
        Parser parser = new Parser(new HtmlTreeBuilder());
        parser.setTrackErrors(-10);
        assertFalse(parser.isTrackErrors());
    }
}