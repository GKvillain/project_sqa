package org.jsoup.parser;

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
        treeBuilder.initialiseParse(
                new StringReader(""),
                "http://example.com/",
                ParseErrorList.noTracking(),
                ParseSettings.htmlDefault
        );
    }

    // Tests initial parse configuration and default settings
    @Test
    public void testInitialiseParse_defaultInitialization_setsDefaultValues() {
        assertEquals(HtmlTreeBuilderState.Initial, treeBuilder.state());
        assertNull(treeBuilder.originalState());
        assertNull(treeBuilder.getHeadElement());
        assertNull(treeBuilder.getFormElement());
        assertTrue(treeBuilder.framesetOk());
        assertFalse(treeBuilder.isFosterInserts());
        assertFalse(treeBuilder.isFragmentParsing());
        assertNotNull(treeBuilder.getDocument());
        assertEquals("http://example.com/", treeBuilder.getBaseUri());
    }

    // Tests fragment parsing with context element
    @Test
    public void testParseFragment_withContextElement_returnsParsedNodes() {
        Element context = new Element(Tag.valueOf("div"), "http://example.com/");
        List<Node> nodes = treeBuilder.parseFragment(
                "<p>Hello</p>",
                context,
                "http://example.com/",
                ParseErrorList.noTracking(),
                ParseSettings.htmlDefault
        );

        assertNotNull(nodes);
        assertFalse(nodes.isEmpty());
        assertEquals("p", nodes.get(0).nodeName());
    }

    // Tests fragment parsing with null context element
    @Test
    public void testParseFragment_nullContext_returnsDocumentChildNodes() {
        List<Node> nodes = treeBuilder.parseFragment(
                "<div><p>World</p></div>",
                null,
                "http://example.com/",
                ParseErrorList.noTracking(),
                ParseSettings.htmlDefault
        );

        assertNotNull(nodes);
        assertFalse(nodes.isEmpty());
    }

    // Tests setting base URI from <base href="...">
    @Test
    public void testMaybeSetBaseUri_validBaseHref_updatesBaseUri() {
        Element base = new Element(Tag.valueOf("base"), "http://example.com/");
        base.attr("href", "http://example.com/subpath/");

        treeBuilder.maybeSetBaseUri(base);
        assertEquals("http://example.com/subpath/", treeBuilder.getBaseUri());

        // Subsequent <base> should be ignored
        Element base2 = new Element(Tag.valueOf("base"), "http://example.com/");
        base2.attr("href", "http://example.com/another/");
        treeBuilder.maybeSetBaseUri(base2);
        assertEquals("http://example.com/subpath/", treeBuilder.getBaseUri());
    }

    // Tests inserting start tags and elements onto the stack
    @Test
    public void testInsertStartTag_standardTag_addsElementToStackAndDoc() {
        Element el = treeBuilder.insertStartTag("span");
        assertNotNull(el);
        assertEquals("span", el.nodeName());
        assertTrue(treeBuilder.onStack(el));
        assertEquals(el, treeBuilder.currentElement());
    }

    // Tests inserting comment and text tokens
    @Test
    public void testInsert_commentAndCharacterTokens_appendsCorrectNodes() {
        Element div = treeBuilder.insertStartTag("div");

        Token.Comment commentToken = new Token.Comment();
        commentToken.getData().append("test comment");
        treeBuilder.insert(commentToken);

        Token.Character charToken = new Token.Character();
        charToken.data("test text");
        treeBuilder.insert(charToken);

        assertEquals(2, div.childNodeSize());
        assertTrue(div.childNode(0) instanceof Comment);
        assertEquals("test comment", ((Comment) div.childNode(0)).getData());
        assertTrue(div.childNode(1) instanceof TextNode);
        assertEquals("test text", ((TextNode) div.childNode(1)).getWholeText());
    }

    // Tests inserting character token inside script tag creates DataNode
    @Test
    public void testInsert_characterInScript_appendsDataNode() {
        Element script = treeBuilder.insertStartTag("script");

        Token.Character charToken = new Token.Character();
        charToken.data("var x = 1;");
        treeBuilder.insert(charToken);

        assertEquals(1, script.childNodeSize());
        assertTrue(script.childNode(0) instanceof DataNode);
        assertEquals("var x = 1;", ((DataNode) script.childNode(0)).getWholeData());
    }

    // Tests basic stack operations: push, pop, aboveOnStack, removeFromStack
    @Test
    public void testStackOperations_manipulatingStack_returnsExpectedElements() {
        Element html = treeBuilder.insertStartTag("html");
        Element body = treeBuilder.insertStartTag("body");
        Element div = treeBuilder.insertStartTag("div");

        assertTrue(treeBuilder.onStack(div));
        assertEquals(body, treeBuilder.aboveOnStack(div));
        assertEquals(div, treeBuilder.getFromStack("div"));

        Element popped = treeBuilder.pop();
        assertEquals(div, popped);
        assertFalse(treeBuilder.onStack(div));

        boolean removed = treeBuilder.removeFromStack(body);
        assertTrue(removed);
        assertFalse(treeBuilder.onStack(body));
        assertEquals(html, treeBuilder.currentElement());
    }

    // Tests popStackToClose method
    @Test
    public void testPopStackToClose_targetInStack_popsElementsUpToTarget() {
        treeBuilder.insertStartTag("html");
        treeBuilder.insertStartTag("body");
        treeBuilder.insertStartTag("div");
        treeBuilder.insertStartTag("p");
        treeBuilder.insertStartTag("span");

        treeBuilder.popStackToClose("div");
        assertFalse(treeBuilder.onStack(treeBuilder.getFromStack("span")));
        assertFalse(treeBuilder.onStack(treeBuilder.getFromStack("p")));
        assertFalse(treeBuilder.onStack(treeBuilder.getFromStack("div")));
        assertEquals("body", treeBuilder.currentElement().nodeName());
    }

    // Tests popStackToBefore method
    @Test
    public void testPopStackToBefore_targetInStack_popsElementsAboveTarget() {
        treeBuilder.insertStartTag("html");
        treeBuilder.insertStartTag("body");
        treeBuilder.insertStartTag("div");
        Element p = treeBuilder.insertStartTag("p");
        treeBuilder.insertStartTag("span");

        treeBuilder.popStackToBefore("p");
        assertEquals(p, treeBuilder.currentElement());
    }

    // Tests clearStackToTableContext, clearStackToTableBodyContext, and clearStackToTableRowContext
    @Test
    public void testClearStackToContexts_tableElements_clearsToTargetElement() {
        treeBuilder.insertStartTag("html");
        treeBuilder.insertStartTag("body");
        treeBuilder.insertStartTag("table");
        treeBuilder.insertStartTag("tbody");
        treeBuilder.insertStartTag("tr");
        treeBuilder.insertStartTag("td");

        treeBuilder.clearStackToTableRowContext();
        assertEquals("tr", treeBuilder.currentElement().nodeName());

        treeBuilder.clearStackToTableBodyContext();
        assertEquals("tbody", treeBuilder.currentElement().nodeName());

        treeBuilder.clearStackToTableContext();
        assertEquals("table", treeBuilder.currentElement().nodeName());
    }

    // Tests resetInsertionMode with various stack elements
    @Test
    public void testResetInsertionMode_selectAndTableContexts_transitionsState() {
        treeBuilder.insertStartTag("html");
        treeBuilder.insertStartTag("body");
        treeBuilder.insertStartTag("table");
        treeBuilder.resetInsertionMode();
        assertEquals(HtmlTreeBuilderState.InTable, treeBuilder.state());

        treeBuilder.insertStartTag("tbody");
        treeBuilder.resetInsertionMode();
        assertEquals(HtmlTreeBuilderState.InTableBody, treeBuilder.state());

        treeBuilder.insertStartTag("tr");
        treeBuilder.resetInsertionMode();
        assertEquals(HtmlTreeBuilderState.InRow, treeBuilder.state());

        treeBuilder.insertStartTag("td");
        treeBuilder.resetInsertionMode();
        assertEquals(HtmlTreeBuilderState.InCell, treeBuilder.state());

        treeBuilder.insertStartTag("select");
        treeBuilder.resetInsertionMode();
        assertEquals(HtmlTreeBuilderState.InSelect, treeBuilder.state());
    }

    // Tests inScope checks for generic scope, list item scope, and button scope
    @Test
    public void testInScope_differentScopes_returnsCorrectBoolean() {
        treeBuilder.insertStartTag("html");
        treeBuilder.insertStartTag("body");
        treeBuilder.insertStartTag("p");

        assertTrue(treeBuilder.inScope("p"));
        assertTrue(treeBuilder.inButtonScope("p"));

        treeBuilder.insertStartTag("table");
        treeBuilder.insertStartTag("tr");
        treeBuilder.insertStartTag("td");

        assertTrue(treeBuilder.inTableScope("table"));
        assertFalse(treeBuilder.inScope("p"));

        treeBuilder.insertStartTag("ol");
        treeBuilder.insertStartTag("li");
        assertTrue(treeBuilder.inListItemScope("li"));
    }

    // Tests inSelectScope
    @Test
    public void testInSelectScope_validAndInvalidElements_returnsExpected() {
        treeBuilder.insertStartTag("html");
        treeBuilder.insertStartTag("body");
        treeBuilder.insertStartTag("select");
        treeBuilder.insertStartTag("option");

        assertTrue(treeBuilder.inSelectScope("option"));
        assertFalse(treeBuilder.inSelectScope("div"));
    }

    // Tests generateImpliedEndTags with and without exclusion
    @Test
    public void testGenerateImpliedEndTags_withImpliedTags_popsExpectedTags() {
        treeBuilder.insertStartTag("html");
        treeBuilder.insertStartTag("body");
        treeBuilder.insertStartTag("p");

        treeBuilder.generateImpliedEndTags("p");
        assertEquals("p", treeBuilder.currentElement().nodeName());

        treeBuilder.generateImpliedEndTags();
        assertEquals("body", treeBuilder.currentElement().nodeName());
    }

    // Tests active formatting elements queue operations (push, duplicate removal limit, markers)
    @Test
    public void testActiveFormattingElements_pushAndMarker_maintainsQueueCorrectly() {
        Element b1 = new Element(Tag.valueOf("b"), "http://example.com/");
        Element b2 = new Element(Tag.valueOf("b"), "http://example.com/");
        Element b3 = new Element(Tag.valueOf("b"), "http://example.com/");
        Element b4 = new Element(Tag.valueOf("b"), "http://example.com/");

        treeBuilder.pushActiveFormattingElements(b1);
        treeBuilder.pushActiveFormattingElements(b2);
        treeBuilder.pushActiveFormattingElements(b3);
        treeBuilder.pushActiveFormattingElements(b4); // Should evict b1 due to 3-element limit

        assertEquals(b4, treeBuilder.lastFormattingElement());
        assertTrue(treeBuilder.isInActiveFormattingElements(b4));
        assertEquals(b4, treeBuilder.getActiveFormattingElement("b"));

        treeBuilder.insertMarkerToFormattingElements();
        treeBuilder.pushActiveFormattingElements(new Element(Tag.valueOf("i"), "http://example.com/"));
        treeBuilder.clearFormattingElementsToLastMarker();
        assertEquals(b4, treeBuilder.lastFormattingElement());

        treeBuilder.removeFromActiveFormattingElements(b4);
        assertFalse(treeBuilder.isInActiveFormattingElements(b4));
    }

    // Tests reconstructFormattingElements when elements are not on stack
    @Test
    public void testReconstructFormattingElements_unopenedFormattingElements_addsToStack() {
        treeBuilder.insertStartTag("html");
        treeBuilder.insertStartTag("body");

        Element b = new Element(Tag.valueOf("b"), "http://example.com/");
        treeBuilder.pushActiveFormattingElements(b);

        assertFalse(treeBuilder.onStack(b));
        treeBuilder.reconstructFormattingElements();
        assertEquals("b", treeBuilder.currentElement().nodeName());
    }

    // Tests foster parenting when inserting nodes in foster mode
    @Test
    public void testInsertInFosterParent_tableHasParent_insertsBeforeTable() {
        treeBuilder.insertStartTag("html");
        Element body = treeBuilder.insertStartTag("body");
        Element table = treeBuilder.insertStartTag("table");
        treeBuilder.insertStartTag("tr");

        treeBuilder.setFosterInserts(true);
        TextNode textNode = new TextNode("fostered text");
        treeBuilder.insertInFosterParent(textNode);

        assertEquals(table, body.child(0));
        assertEquals(textNode, body.childNode(0));
    }

    // Tests isSpecial method for known special and custom tags
    @Test
    public void testIsSpecial_specialAndNonSpecialTags_returnsExpected() {
        Element p = new Element(Tag.valueOf("p"), "http://example.com/");
        Element div = new Element(Tag.valueOf("div"), "http://example.com/");
        Element custom = new Element(Tag.valueOf("custom-tag"), "http://example.com/");

        assertTrue(treeBuilder.isSpecial(p));
        assertTrue(treeBuilder.isSpecial(div));
        assertFalse(treeBuilder.isSpecial(custom));
    }

    // Tests state transition and markInsertionMode
    @Test
    public void testStateAndMarkInsertionMode_transitions_storesOriginalState() {
        treeBuilder.transition(HtmlTreeBuilderState.InBody);
        treeBuilder.markInsertionMode();
        assertEquals(HtmlTreeBuilderState.InBody, treeBuilder.originalState());

        treeBuilder.transition(HtmlTreeBuilderState.InTable);
        assertEquals(HtmlTreeBuilderState.InTable, treeBuilder.state());
        assertEquals(HtmlTreeBuilderState.InBody, treeBuilder.originalState());
    }
}