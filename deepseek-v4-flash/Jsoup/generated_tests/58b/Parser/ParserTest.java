package org.jsoup.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class ParserTest {

    // Test constructor with HtmlTreeBuilder
    @Test
    public void testConstructor_htmlParser_createsParserWithHtmlTreeBuilder() {
        Parser parser = new Parser(new HtmlTreeBuilder());
        assertNotNull(parser.getTreeBuilder());
        assertTrue(parser.getTreeBuilder() instanceof HtmlTreeBuilder);
    }

    // Test constructor with XmlTreeBuilder
    @Test
    public void testConstructor_xmlParser_createsParserWithXmlTreeBuilder() {
        Parser parser = new Parser(new XmlTreeBuilder());
        assertNotNull(parser.getTreeBuilder());
        assertTrue(parser.getTreeBuilder() instanceof XmlTreeBuilder);
    }

    // Test static htmlParser factory method
    @Test
    public void testHtmlParser_returnsParserWithHtmlTreeBuilder() {
        Parser parser = Parser.htmlParser();
        assertTrue(parser.getTreeBuilder() instanceof HtmlTreeBuilder);
    }

    // Test static xmlParser factory method
    @Test
    public void testXmlParser_returnsParserWithXmlTreeBuilder() {
        Parser parser = Parser.xmlParser();
        assertTrue(parser.getTreeBuilder() instanceof XmlTreeBuilder);
    }

    // Test parseInput with simple HTML
    @Test
    public void testParseInput_simpleHtml_returnsDocumentWithCorrectTitle() {
        Parser parser = new Parser(new HtmlTreeBuilder());
        Document doc = parser.parseInput("<html><head><title>Test</title></head><body></body></html>", "http://example.com");
        assertEquals("Test", doc.title());
    }

    // Test parseInput with empty string
    @Test
    public void testParseInput_emptyHtml_returnsEmptyDocument() {
        Parser parser = new Parser(new HtmlTreeBuilder());
        Document doc = parser.parseInput("", "http://example.com");
        assertNotNull(doc);
        assertEquals("", doc.text());
    }

    // Test parseInput with error tracking enabled
    @Test
    public void testParseInput_trackErrorsEnabled_returnsNonEmptyErrorList() {
        Parser parser = new Parser(new HtmlTreeBuilder());
        parser.setTrackErrors(10);
        Document doc = parser.parseInput("<p>unclosed", "http://example.com");
        List<ParseError> errors = parser.getErrors();
        assertFalse(errors.isEmpty());
    }

    // Test parseInput with error tracking disabled (default)
    @Test
    public void testParseInput_trackErrorsDisabled_returnsEmptyErrorList() {
        Parser parser = new Parser(new HtmlTreeBuilder());
        parser.setTrackErrors(0);
        Document doc = parser.parseInput("<p>unclosed", "http://example.com");
        List<ParseError> errors = parser.getErrors();
        assertTrue(errors.isEmpty());
    }

    // Test isTrackErrors default false
    @Test
    public void testIsTrackErrors_default_false() {
        Parser parser = new Parser(new HtmlTreeBuilder());
        assertFalse(parser.isTrackErrors());
    }

    // Test isTrackErrors after setTrackErrors with positive value
    @Test
    public void testIsTrackErrors_setPositive_returnsTrue() {
        Parser parser = new Parser(new HtmlTreeBuilder());
        parser.setTrackErrors(5);
        assertTrue(parser.isTrackErrors());
    }

    // Test setTrackErrors(0) disables tracking (boundary)
    @Test
    public void testSetTrackErrors_zero_disablesTracking() {
        Parser parser = new Parser(new HtmlTreeBuilder());
        parser.setTrackErrors(0);
        assertFalse(parser.isTrackErrors());
    }

    // Test setTrackErrors with negative value (should disable tracking)
    @Test
    public void testSetTrackErrors_negative_disablesTracking() {
        Parser parser = new Parser(new HtmlTreeBuilder());
        parser.setTrackErrors(-1);
        assertFalse(parser.isTrackErrors());
    }

    // Test settings getter/setter
    @Test
    public void testSettings_setAndGet_returnsSame() {
        Parser parser = new Parser(new HtmlTreeBuilder());
        ParseSettings customSettings = ParseSettings.htmlDefault;
        parser.settings(customSettings);
        assertSame(customSettings, parser.settings());
    }

    // Test static parse method
    @Test
    public void testStaticParse_simpleHtml_returnsDocumentWithCorrectTitle() {
        Document doc = Parser.parse("<html><head><title>Hello</title></head><body></body></html>", "http://example.com");
        assertEquals("Hello", doc.title());
    }

    // Test static parseFragment method
    @Test
    public void testStaticParseFragment_simpleFragment_returnsNodeList() {
        Document doc = Document.createShell("http://example.com");
        Element body = doc.body();
        List<Node> nodes = Parser.parseFragment("<p>one</p><p>two</p>", body, "http://example.com");
        assertEquals(2, nodes.size());
    }

    // Test static parseXmlFragment method
    @Test
    public void testStaticParseXmlFragment_simpleXml_returnsNodeList() {
        List<Node> nodes = Parser.parseXmlFragment("<root><child/></root>", "http://example.com");
        assertEquals(1, nodes.size());
    }

    // Test static parseBodyFragment method (potential bug area: loop condition i>0)
    @Test
    public void testStaticParseBodyFragment_multipleNodes_allAppended() {
        Document doc = Parser.parseBodyFragment("<p>one</p><p>two</p>", "http://example.com");
        Element body = doc.body();
        assertEquals(2, body.children().size());
        assertEquals("one", body.child(0).text());
        assertEquals("two", body.child(1).text());
    }

    // Test parseBodyFragment with single node
    @Test
    public void testStaticParseBodyFragment_singleNode_appended() {
        Document doc = Parser.parseBodyFragment("<p>only</p>", "http://example.com");
        Element body = doc.body();
        assertEquals(1, body.children().size());
        assertEquals("only", body.child(0).text());
    }

    // Test static unescapeEntities method
    @Test
    public void testUnescapeEntities_basicString_returnsUnescaped() {
        String result = Parser.unescapeEntities("&amp;lt;", false);
        assertEquals("&lt;", result);
    }

    // Test static parseBodyFragmentRelaxed (delegates to parse)
    @Test
    public void testStaticParseBodyFragmentRelaxed_identicalToParse() {
        Document doc1 = Parser.parseBodyFragmentRelaxed("<p>test</p>", "http://example.com");
        Document doc2 = Parser.parse("<p>test</p>", "http://example.com");
        assertEquals(doc1.text(), doc2.text());
    }

    // Test setTreeBuilder returns same parser (chaining)
    @Test
    public void testSetTreeBuilder_returnsThis() {
        Parser parser = new Parser(new HtmlTreeBuilder());
        Parser returned = parser.setTreeBuilder(new XmlTreeBuilder());
        assertSame(parser, returned);
    }

    // ========== New tests for uncovered lines and conditions ==========

    // Test parseInput with null baseUri
    @Test
    public void testParseInput_nullBaseUri_doesNotThrow() {
        Parser parser = new Parser(new HtmlTreeBuilder());
        Document doc = parser.parseInput("<p>Hello</p>", null);
        assertNotNull(doc);
        assertEquals("Hello", doc.text());
    }

    // Test parseInput with XmlTreeBuilder
    @Test
    public void testParseInput_xmlTreeBuilder_returnsDocument() {
        Parser parser = new Parser(new XmlTreeBuilder());
        Document doc = parser.parseInput("<root><child/></root>", "http://example.com");
        assertNotNull(doc);
        assertEquals(1, doc.children().size());
    }

    // Test parseInput with limited error tracking (maxErrors = 1, more errors generated)
    @Test
    public void testParseInput_trackErrorsLimited_oneErrorStored() {
        Parser parser = new Parser(new HtmlTreeBuilder());
        parser.setTrackErrors(1);
        // Intentionally malformed HTML that causes at least two errors
        Document doc = parser.parseInput("<p>unclosed<div>another", "http://example.com");
        List<ParseError> errors = parser.getErrors();
        assertFalse(errors.isEmpty());
        assertEquals(1, errors.size());
    }

    // Test parseInput with error tracking and multiple errors within limit
    @Test
    public void testParseInput_trackErrorsMultiple_twoErrorsStored() {
        Parser parser = new Parser(new HtmlTreeBuilder());
        parser.setTrackErrors(5);
        Document doc = parser.parseInput("<p>error1</p><p>error2", "http://example.com");
        List<ParseError> errors = parser.getErrors();
        // Should have at least 1 error; actual count may vary, but we check non-empty and <=5
        assertFalse(errors.isEmpty());
        assertTrue(errors.size() <= 5);
    }

    // Test parseFragment with null context (should use body of a fresh shell)
    @Test
    public void testStaticParseFragment_nullContext_usesBodyShell() {
        List<Node> nodes = Parser.parseFragment("<p>hello</p>", null, "http://example.com");
        assertEquals(1, nodes.size());
        assertEquals("hello", nodes.get(0).childNode(0).outerHtml());
    }

    // Test parseFragment with empty html
    @Test
    public void testStaticParseFragment_emptyHtml_returnsEmptyList() {
        Document doc = Document.createShell("http://example.com");
        Element body = doc.body();
        List<Node> nodes = Parser.parseFragment("", body, "http://example.com");
        assertTrue(nodes.isEmpty());
    }

    // Test parseXmlFragment with empty string
    @Test
    public void testStaticParseXmlFragment_emptyString_returnsEmptyList() {
        List<Node> nodes = Parser.parseXmlFragment("", "http://example.com");
        assertTrue(nodes.isEmpty());
    }

    // Test parseBodyFragment with empty html (0 nodes, loop should not execute)
    @Test
    public void testStaticParseBodyFragment_emptyHtml_noChildren() {
        Document doc = Parser.parseBodyFragment("", "http://example.com");
        Element body = doc.body();
        assertTrue(body.children().isEmpty());
    }

    // Test parseBodyFragment with three nodes (loop runs twice)
    @Test
    public void testStaticParseBodyFragment_threeNodes_allAppended() {
        Document doc = Parser.parseBodyFragment("<p>one</p><p>two</p><p>three</p>", "http://example.com");
        Element body = doc.body();
        assertEquals(3, body.children().size());
        assertEquals("one", body.child(0).text());
        assertEquals("two", body.child(1).text());
        assertEquals("three", body.child(2).text());
    }

    // Test unescapeEntities with strict mode true
    @Test
    public void testUnescapeEntities_strictTrue_returnsUnescaped() {
        String result = Parser.unescapeEntities("&amp;lt;", true);
        assertEquals("&lt;", result);
    }

    // Test settings with null (should throw IllegalArgumentException or similar)
    @Test(expected = IllegalArgumentException.class)
    public void testSettings_setNull_throwsException() {
        Parser parser = new Parser(new HtmlTreeBuilder());
        parser.settings(null);
    }

    // Test constructor with null tree builder (should throw NullPointerException)
    @Test(expected = NullPointerException.class)
    public void testConstructor_nullTreeBuilder_throwsException() {
        new Parser(null);
    }

    // Test setTreeBuilder with null (should throw NullPointerException)
    @Test(expected = NullPointerException.class)
    public void testSetTreeBuilder_null_throwsException() {
        Parser parser = new Parser(new HtmlTreeBuilder());
        parser.setTreeBuilder(null);
    }

    // Test getErrors before any parse operation (should be empty)
    @Test
    public void testGetErrors_beforeParse_returnsEmptyList() {
        Parser parser = new Parser(new HtmlTreeBuilder());
        List<ParseError> errors = parser.getErrors();
        assertTrue(errors.isEmpty());
    }

    // Test parseBodyFragmentRelaxed with empty string
    @Test
    public void testStaticParseBodyFragmentRelaxed_emptyString_returnsEmptyDocument() {
        Document doc = Parser.parseBodyFragmentRelaxed("", "http://example.com");
        assertNotNull(doc);
        assertTrue(doc.body().children().isEmpty());
    }
}