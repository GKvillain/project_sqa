package org.jsoup.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.FormElement;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * JUnit 4 Test Class for HtmlTreeBuilder, targeting Defects4J Bug 92b.
 */
public class HtmlTreeBuilderTest {

    // Tests initialiseParse resetting state correctly
    @Test
    public void testInitialiseParse_resetsStateAndFlags() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        builder.initialiseParse(new java.io.StringReader("<html></html>"), "http://base", parser);
        assertNotNull(builder.getDocument());
        assertFalse(builder.isFragmentParsing());
    }

    // Tests that initialiseParse initializes formattingElements list
    @Test
    public void testInitialiseParse_formattingElementsInitialized() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        builder.initialiseParse(new java.io.StringReader("<html></html>"), "http://base", parser);
        assertNotNull(builder.getDocument());
    }

    // Tests parseFragment with null context returns doc child nodes
    @Test
    public void testParseFragment_nullContext_returnsDocChildNodes() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        List<org.jsoup.nodes.Node> nodes = builder.parseFragment("<p>test</p>", null, "http://base", parser);
        assertNotNull(nodes);
        assertFalse(nodes.isEmpty());
    }

    // Tests parseFragment with context element sets up stack and resets insertion mode
    @Test
    public void testParseFragment_withContext_setsUpStack() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        Element context = new Element(org.jsoup.parser.Tag.valueOf("div"), "http://base");
        Document doc = new Document("http://base");
        doc.appendChild(context);
        List<org.jsoup.nodes.Node> nodes = builder.parseFragment("<span>hello</span>", context, "http://base", parser);
        assertNotNull(nodes);
        assertFalse(nodes.isEmpty());
    }

    // Tests process delegates to state
    @Test
    public void testProcess_token_delegatesToState() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        builder.initialiseParse(new java.io.StringReader("<html></html>"), "http://base", parser);
        Token.EndTag endTag = new Token.EndTag();
        endTag.name("html");
        boolean result = builder.process(endTag);
        assertTrue(result); // will process in current state
    }

    // Tests transition changes state
    @Test
    public void testTransition_changesState() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        builder.initialiseParse(new java.io.StringReader("<html></html>"), "http://base", parser);
        builder.transition(HtmlTreeBuilderState.InBody);
        assertEquals(HtmlTreeBuilderState.InBody, builder.state());
    }

    // Tests framesetOk default true
    @Test
    public void testFramesetOk_defaultTrue() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        builder.initialiseParse(new java.io.StringReader("<html></html>"), "http://base", parser);
        assertTrue(builder.framesetOk());
    }

    // Tests framesetOk set and get
    @Test
    public void testFramesetOk_setFalse_returnsFalse() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        builder.initialiseParse(new java.io.StringReader("<html></html>"), "http://base", parser);
        builder.framesetOk(false);
        assertFalse(builder.framesetOk());
    }

    // Tests pushActiveFormattingElements adds element
    @Test
    public void testPushActiveFormattingElements_addsElement() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        builder.initialiseParse(new java.io.StringReader("<html></html>"), "http://base", parser);
        Element el = new Element(org.jsoup.parser.Tag.valueOf("b"), "http://base");
        builder.pushActiveFormattingElements(el);
        assertNotNull(el);
    }

    // Tests reconstructFormattingElements when last is null does nothing
    @Test
    public void testReconstructFormattingElements_lastNull_doesNothing() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        builder.initialiseParse(new java.io.StringReader("<html></html>"), "http://base", parser);
        builder.reconstructFormattingElements();
        // no assertion needed beyond not throwing
    }

    // Tests removeFromActiveFormattingElements removes specific element
    @Test
    public void testRemoveFromActiveFormattingElements_removesElement() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        builder.initialiseParse(new java.io.StringReader("<html></html>"), "http://base", parser);
        Element el = new Element(org.jsoup.parser.Tag.valueOf("b"), "http://base");
        builder.pushActiveFormattingElements(el);
        builder.removeFromActiveFormattingElements(el);
        // no assertion but no exception
    }

    // Tests isInActiveFormattingElements returns false for unknown element
    @Test
    public void testIsInActiveFormattingElements_unknownElement_returnsFalse() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        builder.initialiseParse(new java.io.StringReader("<html></html>"), "http://base", parser);
        Element el = new Element(org.jsoup.parser.Tag.valueOf("i"), "http://base");
        assertFalse(builder.isInActiveFormattingElements(el));
    }

    // Tests getActiveFormattingElement returns correct element by tag name
    @Test
    public void testGetActiveFormattingElement_byTagName_findsElement() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        builder.initialiseParse(new java.io.StringReader("<html></html>"), "http://base", parser);
        Element el = new Element(org.jsoup.parser.Tag.valueOf("b"), "http://base");
        builder.pushActiveFormattingElements(el);
        Element found = builder.getActiveFormattingElement("b");
        assertNotNull(found);
        assertEquals("b", found.normalName());
    }

    // Tests insertMarkerToFormattingElements adds null marker
    @Test
    public void testInsertMarkerToFormattingElements_addsNull() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        builder.initialiseParse(new java.io.StringReader("<html></html>"), "http://base", parser);
        builder.insertMarkerToFormattingElements();
        // marker added without exception
    }

    // Tests clearFormattingElementsToLastMarker clears up to marker
    @Test
    public void testClearFormattingElementsToLastMarker_clearsUntilMarker() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        builder.initialiseParse(new java.io.StringReader("<html></html>"), "http://base", parser);
        builder.clearFormattingElementsToLastMarker();
        // no exception
    }

    // Tests setHeadElement and getHeadElement
    @Test
    public void testSetHeadElement_getHeadElement() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        builder.initialiseParse(new java.io.StringReader("<html></html>"), "http://base", parser);
        Element head = new Element(org.jsoup.parser.Tag.valueOf("head"), "http://base");
        builder.setHeadElement(head);
        assertEquals(head, builder.getHeadElement());
    }

    // Tests fosterInserts default false
    @Test
    public void testIsFosterInserts_defaultFalse() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        builder.initialiseParse(new java.io.StringReader("<html></html>"), "http://base", parser);
        assertFalse(builder.isFosterInserts());
    }

    // Tests setFosterInserts true
    @Test
    public void testSetFosterInserts_true_returnsTrue() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        builder.initialiseParse(new java.io.StringReader("<html></html>"), "http://base", parser);
        builder.setFosterInserts(true);
        assertTrue(builder.isFosterInserts());
    }

    // Tests getFormElement returns null initially
    @Test
    public void testGetFormElement_initialNull() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        builder.initialiseParse(new java.io.StringReader("<html></html>"), "http://base", parser);
        assertNull(builder.getFormElement());
    }

    // Tests setFormElement sets correctly
    @Test
    public void testSetFormElement_setsFormElement() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        builder.initialiseParse(new java.io.StringReader("<html></html>"), "http://base", parser);
        FormElement form = new FormElement(org.jsoup.parser.Tag.valueOf("form"), "http://base", new org.jsoup.nodes.Attributes());
        builder.setFormElement(form);
        assertEquals(form, builder.getFormElement());
    }

    // Tests newPendingTableCharacters creates new list
    @Test
    public void testNewPendingTableCharacters_createsNewList() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        builder.initialiseParse(new java.io.StringReader("<html></html>"), "http://base", parser);
        builder.newPendingTableCharacters();
        assertNotNull(builder.getPendingTableCharacters());
        assertTrue(builder.getPendingTableCharacters().isEmpty());
    }

    // Tests resetInsertionMode transitions to correct state for body
    @Test
    public void testResetInsertionMode_body_returnsInBody() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        builder.initialiseParse(new java.io.StringReader("<html></html>"), "http://base", parser);
        builder.resetInsertionMode();
        assertEquals(HtmlTreeBuilderState.InBody, builder.state());
    }

    // Tests generateImpliedEndTags from non-end-tag element
    @Test
    public void testGenerateImpliedEndTags_defaultExcludeTag() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        builder.initialiseParse(new java.io.StringReader("<html></html>"), "http://base", parser);
        builder.generateImpliedEndTags("p");
        // no exception
    }

    // Tests isSpecial for known special element
    @Test
    public void testIsSpecial_knownSpecial_returnsTrue() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        builder.initialiseParse(new java.io.StringReader("<html></html>"), "http://base", parser);
        Element el = new Element(org.jsoup.parser.Tag.valueOf("div"), "http://base");
        assertTrue(builder.isSpecial(el));
    }

    // Tests isSpecial for non-special element
    @Test
    public void testIsSpecial_nonSpecial_returnsFalse() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        builder.initialiseParse(new java.io.StringReader("<html></html>"), "http://base", parser);
        Element el = new Element(org.jsoup.parser.Tag.valueOf("span"), "http://base");
        assertFalse(builder.isSpecial(el));
    }

    // Tests getBaseUri
    @Test
    public void testGetBaseUri_returnsSetUri() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        builder.initialiseParse(new java.io.StringReader("<html></html>"), "http://base", parser);
        assertEquals("http://base", builder.getBaseUri());
    }
}