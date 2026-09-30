package org.jsoup.parser;

import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.Tag;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class TreeBuilderTest {

    private ConcreteTreeBuilder treeBuilder;

    private static class ConcreteTreeBuilder extends TreeBuilder {
        final List<Token.TokenType> seenTokenTypes = new ArrayList<Token.TokenType>();
        Token lastProcessedToken;
        boolean processReturnValue = true;

        @Override
        protected boolean process(Token token) {
            lastProcessedToken = token;
            seenTokenTypes.add(token.type);
            return processReturnValue;
        }

        @Override
        List<Node> parseFragment(String inputFragment, Element context, String baseUri, ParseErrorList errors) {
            return new ArrayList<Node>();
        }
    }

    @Before
    public void setUp() {
        treeBuilder = new ConcreteTreeBuilder();
    }

    // Tests null input validation in initialiseParse
    @Test(expected = IllegalArgumentException.class)
    public void testInitialiseParse_nullInput_throwsException() {
        treeBuilder.initialiseParse(null, "http://example.com", ParseErrorList.noTracking());
    }

    // Tests null baseUri validation in initialiseParse
    @Test(expected = IllegalArgumentException.class)
    public void testInitialiseParse_nullBaseUri_throwsException() {
        treeBuilder.initialiseParse("<div>test</div>", null, ParseErrorList.noTracking());
    }

    // Tests normal initialization of parse state
    @Test
    public void testInitialiseParse_validInputs_initialisesStateProperly() {
        ParseErrorList errors = ParseErrorList.tracking(5);
        treeBuilder.initialiseParse("<p>Hello</p>", "http://example.com", errors);

        assertNotNull("doc should be initialized", treeBuilder.doc);
        assertEquals("http://example.com", treeBuilder.doc.baseUri());
        assertNotNull("reader should be initialized", treeBuilder.reader);
        assertNotNull("tokeniser should be initialized", treeBuilder.tokeniser);
        assertNotNull("stack should be initialized", treeBuilder.stack);
        assertTrue("stack should initially be empty", treeBuilder.stack.isEmpty());
        assertEquals("http://example.com", treeBuilder.baseUri);
        assertEquals(errors, treeBuilder.errors);
    }

    // Tests parse with input and baseUri only
    @Test
    public void testParse_inputAndBaseUri_returnsDocument() {
        Document doc = treeBuilder.parse("<div>Content</div>", "http://example.com");

        assertNotNull("Returned document should not be null", doc);
        assertEquals("http://example.com", doc.baseUri());
        assertFalse("Tokens should have been processed", treeBuilder.seenTokenTypes.isEmpty());
        assertEquals(Token.TokenType.EOF, treeBuilder.seenTokenTypes.get(treeBuilder.seenTokenTypes.size() - 1));
    }

    // Tests parse with input, baseUri, and custom ParseErrorList
    @Test
    public void testParse_withParseErrorList_tracksErrorsAndReturnsDocument() {
        ParseErrorList errors = ParseErrorList.tracking(10);
        Document doc = treeBuilder.parse("<span>Test</span>", "http://example.com", errors);

        assertNotNull("Returned document should not be null", doc);
        assertEquals(errors, treeBuilder.errors);
        assertTrue("EOF token should be reached", treeBuilder.seenTokenTypes.contains(Token.TokenType.EOF));
    }

    // Tests runParser loop termination upon EOF token
    @Test
    public void testRunParser_emptyInput_stopsAtEof() {
        treeBuilder.initialiseParse("", "http://example.com", ParseErrorList.noTracking());
        treeBuilder.runParser();

        assertEquals(1, treeBuilder.seenTokenTypes.size());
        assertEquals(Token.TokenType.EOF, treeBuilder.seenTokenTypes.get(0));
    }

    // Tests processStartTag with name only
    @Test
    public void testProcessStartTag_nameOnly_processesStartTagToken() {
        treeBuilder.initialiseParse("<div>", "http://example.com", ParseErrorList.noTracking());
        boolean result = treeBuilder.processStartTag("div");

        assertTrue("processStartTag should return true", result);
        assertNotNull("lastProcessedToken should not be null", treeBuilder.lastProcessedToken);
        assertTrue("Token should be StartTag", treeBuilder.lastProcessedToken.isStartTag());
        assertEquals("div", treeBuilder.lastProcessedToken.asStartTag().name());
    }

    // Tests processStartTag with name and attributes
    @Test
    public void testProcessStartTag_nameAndAttributes_processesStartTagWithAttributes() {
        treeBuilder.initialiseParse("<div id='test'>", "http://example.com", ParseErrorList.noTracking());
        Attributes attrs = new Attributes();
        attrs.put("id", "main");
        attrs.put("class", "content");

        boolean result = treeBuilder.processStartTag("div", attrs);

        assertTrue("processStartTag should return true", result);
        assertNotNull("lastProcessedToken should not be null", treeBuilder.lastProcessedToken);
        assertTrue("Token should be StartTag", treeBuilder.lastProcessedToken.isStartTag());
        Token.StartTag startTag = treeBuilder.lastProcessedToken.asStartTag();
        assertEquals("div", startTag.name());
        assertEquals("main", startTag.attributes.get("id"));
        assertEquals("content", startTag.attributes.get("class"));
    }

    // Tests processEndTag with name only
    @Test
    public void testProcessEndTag_nameOnly_processesEndTagToken() {
        treeBuilder.initialiseParse("</div>", "http://example.com", ParseErrorList.noTracking());
        boolean result = treeBuilder.processEndTag("div");

        assertTrue("processEndTag should return true", result);
        assertNotNull("lastProcessedToken should not be null", treeBuilder.lastProcessedToken);
        assertTrue("Token should be EndTag", treeBuilder.lastProcessedToken.isEndTag());
        assertEquals("div", treeBuilder.lastProcessedToken.asEndTag().name());
    }

    // Tests currentElement on empty stack returns null
    @Test
    public void testCurrentElement_emptyStack_returnsNull() {
        treeBuilder.initialiseParse("<p></p>", "http://example.com", ParseErrorList.noTracking());
        assertNull("currentElement should return null when stack is empty", treeBuilder.currentElement());
    }

    // Tests currentElement on single item stack returns that element
    @Test
    public void testCurrentElement_singleElementOnStack_returnsElement() {
        treeBuilder.initialiseParse("<p></p>", "http://example.com", ParseErrorList.noTracking());
        Element el = new Element(Tag.valueOf("p"), "http://example.com");
        treeBuilder.stack.add(el);

        assertSame("currentElement should return the single element on stack", el, treeBuilder.currentElement());
    }

    // Tests currentElement on multi-item stack returns top/last element
    @Test
    public void testCurrentElement_multipleElementsOnStack_returnsTopElement() {
        treeBuilder.initialiseParse("<div><p></p></div>", "http://example.com", ParseErrorList.noTracking());
        Element div = new Element(Tag.valueOf("div"), "http://example.com");
        Element p = new Element(Tag.valueOf("p"), "http://example.com");
        Element span = new Element(Tag.valueOf("span"), "http://example.com");

        treeBuilder.stack.add(div);
        treeBuilder.stack.add(p);
        assertSame("currentElement should return last pushed element", p, treeBuilder.currentElement());

        treeBuilder.stack.add(span);
        assertSame("currentElement should return top of stack after push", span, treeBuilder.currentElement());

        treeBuilder.stack.remove(treeBuilder.stack.size() - 1);
        assertSame("currentElement should return previous element after pop", p, treeBuilder.currentElement());
    }

    // Tests parseFragment implementation
    @Test
    public void testParseFragment() {
        List<Node> nodes = treeBuilder.parseFragment("<p>test</p>", null, "http://example.com", ParseErrorList.noTracking());
        assertNotNull("parseFragment should return a non-null list", nodes);
        assertTrue("parseFragment should return empty list for concrete implementation", nodes.isEmpty());
    }

    // Tests process returning false propagates correctly through processStartTag and processEndTag
    @Test
    public void testProcess_returnsFalse_propagatedCorrectly() {
        treeBuilder.initialiseParse("<div></div>", "http://example.com", ParseErrorList.noTracking());
        treeBuilder.processReturnValue = false;

        assertFalse("processStartTag should return false when process returns false", treeBuilder.processStartTag("div"));
        assertFalse("processStartTag with attrs should return false when process returns false", treeBuilder.processStartTag("div", new Attributes()));
        assertFalse("processEndTag should return false when process returns false", treeBuilder.processEndTag("div"));
    }
}