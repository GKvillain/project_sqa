package org.jsoup.parser;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.util.ArrayList;
import java.util.List;

public class TreeBuilderTest {

    private TestTreeBuilder builder;

    @Before
    public void setUp() {
        builder = new TestTreeBuilder();
    }

    private static class TestTreeBuilder extends TreeBuilder {
        List<Token> processedTokens = new ArrayList<Token>();

        @Override
        protected boolean process(Token token) {
            processedTokens.add(token);
            return true;
        }
    }

    // Tests null input
    @Test(expected = IllegalArgumentException.class)
    public void testInitialiseParse_nullInput_throwsException() {
        builder.initialiseParse(null, "http://base", ParseErrorList.noTracking());
    }

    // Tests null baseUri
    @Test(expected = IllegalArgumentException.class)
    public void testInitialiseParse_nullBaseUri_throwsException() {
        builder.initialiseParse("html", null, ParseErrorList.noTracking());
    }

    // Tests normal initialisation
    @Test
    public void testInitialiseParse_validInput_setsFields() {
        ParseErrorList errors = ParseErrorList.noTracking();
        builder.initialiseParse("<html>", "http://base", errors);

        assertNotNull(builder.reader);
        assertNotNull(builder.tokeniser);
        assertNotNull(builder.doc);
        assertNotNull(builder.stack);
        assertEquals(0, builder.stack.size());
        assertEquals("http://base", builder.baseUri);
        assertSame(errors, builder.errors);
        assertEquals("http://base", builder.doc.baseUri());
    }

    // Tests parse with normal HTML input
    @Test(timeout = 5000)
    public void testParse_validHtml_returnsDocumentWithBaseUri() {
        Document doc = builder.parse("<html><body></body></html>", "http://example.com");
        assertNotNull(doc);
        assertEquals("http://example.com", doc.baseUri());
        assertFalse(builder.processedTokens.isEmpty());
    }

    // Tests parse with empty input
    @Test(timeout = 5000)
    public void testParse_emptyInput_returnsDocument() {
        Document doc = builder.parse("", "http://base");
        assertNotNull(doc);
        assertEquals("http://base", doc.baseUri());
    }

    // Tests processStartTag with simple name
    @Test
    public void testProcessStartTag_simpleName_passesStartTagToken() {
        assertTrue(builder.processStartTag("div"));
        assertEquals(1, builder.processedTokens.size());
        Token token = builder.processedTokens.get(0);
        assertEquals(Token.TokenType.StartTag, token.type);
        assertEquals("div", ((Token.StartTag) token).name());
    }

    // Tests processStartTag with attributes
    @Test
    public void testProcessStartTag_withAttributes_passesStartTagToken() {
        Attributes attrs = new Attributes();
        attrs.put("class", "test");
        assertTrue(builder.processStartTag("span", attrs));
        assertEquals(1, builder.processedTokens.size());
        Token token = builder.processedTokens.get(0);
        assertEquals(Token.TokenType.StartTag, token.type);
        assertEquals("span", ((Token.StartTag) token).name());
    }

    // Tests processEndTag with simple name
    @Test
    public void testProcessEndTag_name_passesEndTagToken() {
        assertTrue(builder.processEndTag("p"));
        assertEquals(1, builder.processedTokens.size());
        Token token = builder.processedTokens.get(0);
        assertEquals(Token.TokenType.EndTag, token.type);
        assertEquals("p", ((Token.EndTag) token).name());
    }

    // Tests empty stack branch
    @Test
    public void testCurrentElement_emptyStack_returnsNull() {
        builder.initialiseParse("", "http://base", ParseErrorList.noTracking());
        assertNull(builder.currentElement());
    }

    // Tests single element on stack
    @Test
    public void testCurrentElement_singleElement_returnsThatElement() {
        builder.initialiseParse("", "http://base", ParseErrorList.noTracking());
        Element element = builder.doc.createElement("div");
        builder.stack.add(element);
        assertSame(element, builder.currentElement());
    }

    // Tests multiple elements on stack
    @Test
    public void testCurrentElement_multipleElements_returnsTopmost() {
        builder.initialiseParse("", "http://base", ParseErrorList.noTracking());
        Element first = builder.doc.createElement("div");
        Element second = builder.doc.createElement("span");
        builder.stack.add(first);
        builder.stack.add(second);
        assertSame(second, builder.currentElement());
    }

    // Tests that runParser processes at least one token (implicitly)
    @Test(timeout = 5000)
    public void testParse_multipleTokens_returnsDocument() {
        builder.parse("<html><head></head><body><p>text</p></body></html>", "http://base");
        assertFalse(builder.processedTokens.isEmpty());
    }
}