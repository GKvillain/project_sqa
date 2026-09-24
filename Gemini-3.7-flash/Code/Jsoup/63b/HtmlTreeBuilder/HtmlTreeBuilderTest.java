package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.FormElement;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.junit.Before;
import org.junit.Test;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class HtmlTreeBuilderTest {

    private HtmlTreeBuilder treeBuilder;

    @Before
    public void setUp() {
        treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com/", ParseErrorList.tracking(10), ParseSettings.htmlDefault);
    }

    // Tests default settings returned by tree builder
    @Test
    public void testDefaultSettings_normalCall_returnsHtmlDefault() {
        ParseSettings settings = treeBuilder.defaultSettings();
        assertNotNull(settings);
        assertEquals(ParseSettings.htmlDefault, settings);
    }

    // Tests state transition and mark insertion mode tracking
    @Test
    public void testTransitionAndMarkInsertionMode_stateTransitions_tracksOriginalState() {
        treeBuilder.transition(HtmlTreeBuilderState.InBody);
        assertEquals(HtmlTreeBuilderState.InBody, treeBuilder.state());

        treeBuilder.markInsertionMode();
        assertEquals(HtmlTreeBuilderState.InBody, treeBuilder.originalState());

        treeBuilder.transition(HtmlTreeBuilderState.InTable);
        assertEquals(HtmlTreeBuilderState.InTable, treeBuilder.state());
        assertEquals(HtmlTreeBuilderState.InBody, treeBuilder.originalState());
    }

    // Tests framesetOk getter and setter
    @Test
    public void testFramesetOk_settingFlag_updatesCorrectly() {
        assertTrue(treeBuilder.framesetOk());
        treeBuilder.framesetOk(false);
        assertFalse(treeBuilder.framesetOk());
    }

    // Tests base URI resolution and update from base element
    @Test
    public void testMaybeSetBaseUri_validHref_updatesBaseUriOnce() {
        Element base1 = new Element(Tag.valueOf("base", ParseSettings.htmlDefault), "http://example.com/");
        base1.attr("href", "http://example.com/sub/");
        treeBuilder.maybeSetBaseUri(base1);
        assertEquals("http://example.com/sub/", treeBuilder.getBaseUri());

        Element base2 = new Element(Tag.valueOf("base", ParseSettings.htmlDefault), "http://example.com/");
        base2.attr("href", "http://example.com/another/");
        treeBuilder.maybeSetBaseUri(base2);
        assertEquals("http://example.com/sub/", treeBuilder.getBaseUri());
    }

    // Tests inserting start tags and regular elements onto the stack
    @Test
    public void testInsertStartTag_validTagName_pushesElementToStack() {
        Element p = treeBuilder.insertStartTag("p");
        assertNotNull(p);
        assertEquals("p", p.tagName());
        assertTrue(treeBuilder.onStack(p));
        assertEquals(p, treeBuilder.currentElement());
    }

    // Tests insert empty self-closing start tag handling and acknowledge flag
    @Test
    public void testInsertEmpty_selfClosingStartTag_acknowledgesSelfClosing() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.nameAttr("img", new Attributes());
        startTag.selfClosing = true;

        Element el = treeBuilder.insertEmpty(startTag);
        assertNotNull(el);
        assertEquals("img", el.tagName());
        assertFalse(treeBuilder.onStack(el));
    }

    // Tests inserting comment token into document or current element
    @Test
    public void testInsertComment_validCommentToken_appendsCommentNode() {
        Element body = treeBuilder.insertStartTag("body");
        Token.Comment commentToken = new Token.Comment();
        commentToken.data.append("Test comment");

        treeBuilder.insert(commentToken);
        assertEquals(1, body.childNodeSize());
        assertTrue(body.childNode(0) instanceof Comment);
        assertEquals("Test comment", ((Comment) body.childNode(0)).getData());
    }

    // Tests inserting character token as TextNode or DataNode depending on parent tag
    @Test
    public void testInsertCharacter_textAndDataNode_appendsCorrectNodeType() {
        Element body = treeBuilder.insertStartTag("body");
        Token.Character textChar = new Token.Character();
        textChar.data("Hello");
        treeBuilder.insert(textChar);

        assertEquals(1, body.childNodeSize());
        assertTrue(body.childNode(0) instanceof TextNode);
        assertEquals("Hello", ((TextNode) body.childNode(0)).text());

        Element script = treeBuilder.insertStartTag("script");
        Token.Character scriptChar = new Token.Character();
        scriptChar.data("var x = 1;");
        treeBuilder.insert(scriptChar);

        assertEquals(1, script.childNodeSize());
        assertTrue(script.childNode(0) instanceof DataNode);
        assertEquals("var x = 1;", ((DataNode) script.childNode(0)).getWholeData());
    }

    // Tests stack manipulation operations (pop, push, aboveOnStack, removeFromStack)
    @Test
    public void testStackOperations_pushPopAndAbove_maintainsCorrectStackOrder() {
        Element html = treeBuilder.insertStartTag("html");
        Element body = treeBuilder.insertStartTag("body");
        Element div = treeBuilder.insertStartTag("div");

        assertTrue(treeBuilder.onStack(div));
        assertEquals(body, treeBuilder.aboveOnStack(div));
        assertEquals(div, treeBuilder.pop());
        assertFalse(treeBuilder.onStack(div));
        assertEquals(body, treeBuilder.currentElement());

        treeBuilder.push(div);
        assertTrue(treeBuilder.onStack(div));
        assertTrue(treeBuilder.removeFromStack(body));
        assertFalse(treeBuilder.onStack(body));
    }

    // Tests popStackToClose method for closing target elements
    @Test
    public void testPopStackToClose_nestedTags_popsUntilTargetElement() {
        treeBuilder.insertStartTag("html");
        treeBuilder.insertStartTag("body");
        treeBuilder.insertStartTag("p");
        treeBuilder.insertStartTag("span");

        treeBuilder.popStackToClose("p");
        assertNull(treeBuilder.getFromStack("p"));
        assertNull(treeBuilder.getFromStack("span"));
        assertNotNull(treeBuilder.getFromStack("body"));
    }

    // Tests inScope checks across different scopes (scope, list, table, select)
    @Test
    public void testInScope_differentScoping_evaluatesScopeBoundaries() {
        treeBuilder.insertStartTag("html");
        treeBuilder.insertStartTag("body");
        treeBuilder.insertStartTag("table");
        treeBuilder.insertStartTag("tr");
        Element td = treeBuilder.insertStartTag("td");

        assertTrue(treeBuilder.inScope("table"));
        assertTrue(treeBuilder.inScope("td"));
        assertTrue(treeBuilder.inTableScope("table"));
        assertFalse(treeBuilder.inTableScope("body"));

        Element select = treeBuilder.insertStartTag("select");
        Element option = treeBuilder.insertStartTag("option");
        assertTrue(treeBuilder.inSelectScope("option"));
        assertFalse(treeBuilder.inSelectScope("table"));
    }

    // Tests active formatting elements management (push, marker, reconstruct, clear)
    @Test
    public void testActiveFormattingElements_pushAndReconstruct_maintainsFormattingStack() {
        treeBuilder.insertStartTag("html");
        treeBuilder.insertStartTag("body");
        Element b = treeBuilder.insertStartTag("b");
        treeBuilder.pushActiveFormattingElements(b);

        assertEquals(b, treeBuilder.lastFormattingElement());
        assertTrue(treeBuilder.isInActiveFormattingElements(b));

        treeBuilder.insertMarkerToFormattingElements();
        assertNull(treeBuilder.lastFormattingElement());

        Element i = treeBuilder.insertStartTag("i");
        treeBuilder.pushActiveFormattingElements(i);
        assertEquals(i, treeBuilder.lastFormattingElement());

        treeBuilder.clearFormattingElementsToLastMarker();
        assertEquals(b, treeBuilder.lastFormattingElement());
    }

    // Tests foster parenting when fosterInserts flag is true
    @Test
    public void testInsertInFosterParent_fosterInsertsEnabled_insertsBeforeTable() {
        Element html = treeBuilder.insertStartTag("html");
        Element body = treeBuilder.insertStartTag("body");
        Element table = treeBuilder.insertStartTag("table");

        treeBuilder.setFosterInserts(true);
        assertTrue(treeBuilder.isFosterInserts());

        TextNode fosterText = new TextNode("fostered", "http://example.com/");
        treeBuilder.insertInFosterParent(fosterText);

        assertEquals(2, body.childNodeSize());
        assertEquals(fosterText, body.childNode(0));
        assertEquals(table, body.childNode(1));
    }

    // Tests resetInsertionMode across various container contexts
    @Test
    public void testResetInsertionMode_selectAndTableContexts_transitionsToCorrectState() {
        treeBuilder.insertStartTag("html");
        treeBuilder.insertStartTag("body");
        treeBuilder.insertStartTag("table");
        treeBuilder.insertStartTag("tbody");
        treeBuilder.insertStartTag("tr");
        treeBuilder.insertStartTag("td");

        treeBuilder.resetInsertionMode();
        assertEquals(HtmlTreeBuilderState.InCell, treeBuilder.state());

        treeBuilder.pop(); // pop td
        treeBuilder.resetInsertionMode();
        assertEquals(HtmlTreeBuilderState.InRow, treeBuilder.state());

        treeBuilder.pop(); // pop tr
        treeBuilder.resetInsertionMode();
        assertEquals(HtmlTreeBuilderState.InTableBody, treeBuilder.state());
    }

    // Tests parsing HTML fragment with context element
    @Test
    public void testParseFragment_withBodyContext_returnsParsedNodes() {
        Element context = new Element(Tag.valueOf("body", ParseSettings.htmlDefault), "http://example.com/");
        List<Node> nodes = treeBuilder.parseFragment("<div><p>Fragment</p></div>", context, "http://example.com/", ParseErrorList.tracking(10), ParseSettings.htmlDefault);

        assertNotNull(nodes);
        assertEquals(1, nodes.size());
        assertTrue(nodes.get(0) instanceof Element);
        Element div = (Element) nodes.get(0);
        assertEquals("div", div.tagName());
        assertEquals("Fragment", div.select("p").text());
    }

    // Tests parseFragment with null context returning root document children
    @Test
    public void testParseFragment_nullContext_returnsDocumentChildren() {
        List<Node> nodes = treeBuilder.parseFragment("<p>One</p><p>Two</p>", null, "http://example.com/", ParseErrorList.tracking(10), ParseSettings.htmlDefault);

        assertNotNull(nodes);
        assertFalse(nodes.isEmpty());
        Element html = (Element) nodes.get(0);
        assertEquals("html", html.tagName());
    }

    // Tests self-closing tag parse error tracking for Defects4J bug 63
    @Test
    public void testParse_selfClosingTagsTracking_handlesAcknowledgeAndErrors() {
        Parser parser = Parser.htmlParser().setTrackErrors(10);
        Document doc = parser.parseInput("<div /><span />", "http://example.com/");

        assertEquals("<div></div><span></span>", doc.body().html());
        List<ParseError> errors = parser.getErrors();
        assertNotNull(errors);
        assertFalse(errors.isEmpty());
    }
}