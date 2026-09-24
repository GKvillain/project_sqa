package org.jsoup.parser;

import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class TreeBuilderTest {

    private static class ConcreteTreeBuilder extends TreeBuilder {
        private final List<Token.TokenType> receivedTokenTypes = new ArrayList<Token.TokenType>();
        private Token lastProcessedToken;
        private String lastStartTagName;
        private Attributes lastStartTagAttributes;
        private String lastEndTagName;

        @Override
        protected boolean process(Token token) {
            receivedTokenTypes.add(token.type);
            lastProcessedToken = token;
            if (token.isStartTag()) {
                Token.StartTag startTag = (Token.StartTag) token;
                lastStartTagName = startTag.name();
                lastStartTagAttributes = startTag.attributes != null ? startTag.attributes.clone() : null;
            } else if (token.isEndTag()) {
                Token.EndTag endTag = (Token.EndTag) token;
                lastEndTagName = endTag.name();
            }
            return true;
        }
    }

    private ConcreteTreeBuilder treeBuilder;

    @Before
    public void setUp() {
        treeBuilder = new ConcreteTreeBuilder();
    }

    // Tests null input string throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testInitialiseParse_nullInput_throwsException() {
        treeBuilder.initialiseParse(null, "http://example.com", ParseErrorList.noTracking());
    }

    // Tests null baseUri throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testInitialiseParse_nullBaseUri_throwsException() {
        treeBuilder.initialiseParse("<p>Test</p>", null, ParseErrorList.noTracking());
    }

    // Tests initialisation of parser state and fields
    @Test
    public void testInitialiseParse_validInputs_initializesFieldsProperly() {
        ParseErrorList errors = ParseErrorList.tracking(10);
        treeBuilder.initialiseParse("<div>Content</div>", "http://example.com/base", errors);

        assertNotNull(treeBuilder.doc);
        assertEquals("http://example.com/base", treeBuilder.doc.baseUri());
        assertEquals("http://example.com/base", treeBuilder.baseUri);
        assertNotNull(treeBuilder.reader);
        assertNotNull(treeBuilder.tokeniser);
        assertNotNull(treeBuilder.stack);
        assertTrue(treeBuilder.stack.isEmpty());
        assertSame(errors, treeBuilder.errors);
    }

    // Tests currentElement when the element stack is empty
    @Test
    public void testCurrentElement_emptyStack_returnsNull() {
        treeBuilder.initialiseParse("<p>Test</p>", "http://example.com", ParseErrorList.noTracking());
        assertNull(treeBuilder.currentElement());
    }

    // Tests currentElement returns the top of the element stack
    @Test
    public void testCurrentElement_nonEmptyStack_returnsTopElement() {
        treeBuilder.initialiseParse("<p>Test</p>", "http://example.com", ParseErrorList.noTracking());

        Element first = new Element(Tag.valueOf("html"), "http://example.com");
        Element second = new Element(Tag.valueOf("body"), "http://example.com");

        treeBuilder.stack.add(first);
        assertSame(first, treeBuilder.currentElement());

        treeBuilder.stack.add(second);
        assertSame(second, treeBuilder.currentElement());

        treeBuilder.stack.remove(treeBuilder.stack.size() - 1);
        assertSame(first, treeBuilder.currentElement());
    }

    // Tests processStartTag with tag name only
    @Test
    public void testProcessStartTag_nameOnly_processesStartTagCorrectly() {
        treeBuilder.initialiseParse("", "http://example.com", ParseErrorList.noTracking());
        boolean result = treeBuilder.processStartTag("div");

        assertTrue(result);
        assertEquals("div", treeBuilder.lastStartTagName);
        assertNotNull(treeBuilder.lastProcessedToken);
        assertTrue(treeBuilder.lastProcessedToken.isStartTag());
    }

    // Tests processStartTag with tag name and attributes
    @Test
    public void testProcessStartTag_nameAndAttributes_processesStartTagWithAttributes() {
        treeBuilder.initialiseParse("", "http://example.com", ParseErrorList.noTracking());
        Attributes attrs = new Attributes();
        attrs.put("id", "main");
        attrs.put("class", "container");

        boolean result = treeBuilder.processStartTag("section", attrs);

        assertTrue(result);
        assertEquals("section", treeBuilder.lastStartTagName);
        assertNotNull(treeBuilder.lastStartTagAttributes);
        assertEquals("main", treeBuilder.lastStartTagAttributes.get("id"));
        assertEquals("container", treeBuilder.lastStartTagAttributes.get("class"));
    }

    // Tests processEndTag with tag name
    @Test
    public void testProcessEndTag_name_processesEndTagCorrectly() {
        treeBuilder.initialiseParse("", "http://example.com", ParseErrorList.noTracking());
        boolean result = treeBuilder.processEndTag("div");

        assertTrue(result);
        assertEquals("div", treeBuilder.lastEndTagName);
        assertNotNull(treeBuilder.lastProcessedToken);
        assertTrue(treeBuilder.lastProcessedToken.isEndTag());
    }

    // Tests parse method without error tracking
    @Test
    public void testParse_withoutErrorTracking_returnsDocumentAndProcessesTokens() {
        Document doc = treeBuilder.parse("<div>Hello</div>", "http://example.com");

        assertNotNull(doc);
        assertEquals("http://example.com", doc.baseUri());
        assertTrue(treeBuilder.receivedTokenTypes.contains(Token.TokenType.StartTag));
        assertTrue(treeBuilder.receivedTokenTypes.contains(Token.TokenType.Character));
        assertTrue(treeBuilder.receivedTokenTypes.contains(Token.TokenType.EndTag));
        assertTrue(treeBuilder.receivedTokenTypes.contains(Token.TokenType.EOF));
    }

    // Tests parse method with error tracking
    @Test
    public void testParse_withErrorTracking_tracksErrorsAndReturnsDocument() {
        ParseErrorList errors = ParseErrorList.tracking(10);
        Document doc = treeBuilder.parse("<p>Test</p", "http://example.com", errors);

        assertNotNull(doc);
        assertSame(errors, treeBuilder.errors);
        assertTrue(treeBuilder.receivedTokenTypes.contains(Token.TokenType.EOF));
    }

    // Tests runParser with empty input processes EOF token
    @Test
    public void testRunParser_emptyInput_terminatesAtEof() {
        Document doc = treeBuilder.parse("", "http://example.com");

        assertNotNull(doc);
        assertEquals(1, treeBuilder.receivedTokenTypes.size());
        assertEquals(Token.TokenType.EOF, treeBuilder.receivedTokenTypes.get(0));
    }
}