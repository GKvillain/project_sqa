package org.jsoup.parser;

import org.junit.Test;
import org.jsoup.nodes.Element;

import java.io.StringReader;
import java.util.Arrays;

import static org.junit.Assert.*;

public class HtmlTreeBuilderTest {

    private HtmlTreeBuilder newBuilder() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse(new StringReader(""), "http://example.com/",
                ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        return tb;
    }

    private Element el(String tag) {
        return new Element(Tag.valueOf(tag, ParseSettings.htmlDefault), "http://example.com/");
    }

    private HtmlTreeBuilder builderWith(String... tags) {
        HtmlTreeBuilder tb = newBuilder();
        for (String tag : tags) {
            tb.push(el(tag));
        }
        return tb;
    }

    // Tests that initialiseParse resets key parser state.
    @Test
    public void testInitialiseParse_resetsStateAndFlags() {
        HtmlTreeBuilder tb = newBuilder();

        assertEquals(HtmlTreeBuilderState.Initial, tb.state());
        assertTrue(tb.framesetOk());
        assertFalse(tb.isFosterInserts());
        assertFalse(tb.isFragmentParsing());
        assertNull(tb.getFormElement());
        assertNull(tb.getHeadElement());
        assertEquals("http://example.com/", tb.getBaseUri());
        assertNotNull(tb.getDocument());
    }

    // Tests the defect: null excludeTag must generate implied end tags.
    @Test
    public void testGenerateImpliedEndTags_nullExclude_popsAllImpliedTags() {
        HtmlTreeBuilder tb = builderWith("html", "li", "p");

        tb.generateImpliedEndTags();

        assertEquals(1, tb.getStack().size());
        assertEquals("html", tb.getStack().get(0).nodeName());
    }

    // Tests that a non-null excludeTag prevents that tag from being popped.
    @Test
    public void testGenerateImpliedEndTags_withExcludeTag_popsUntilExcludedTag() {
        HtmlTreeBuilder tb = builderWith("html", "li", "p");

        tb.generateImpliedEndTags("li");

        assertEquals(2, tb.getStack().size());
        assertEquals("li", tb.getStack().get(1).nodeName());
    }

    // Tests popStackToClose with a single element name.
    @Test
    public void testPopStackToClose_singleName_popsUntilMatch() {
        HtmlTreeBuilder tb = builderWith("html", "div", "p", "li");

        tb.popStackToClose("div");

        assertEquals(1, tb.getStack().size());
        assertEquals("html", tb.getStack().get(0).nodeName());
    }

    // Tests popStackToClose with multiple, sorted element names.
    @Test
    public void testPopStackToClose_varargs_popsUntilAnyMatch() {
        HtmlTreeBuilder tb = builderWith("html", "div", "p", "li");

        tb.popStackToClose("li", "p");

        assertEquals("p", tb.getStack().get(tb.getStack().size() - 1).nodeName());
    }

    // Tests popStackToBefore removes only nodes above the target.
    @Test
    public void testPopStackToBefore_removesNodesAboveTarget() {
        HtmlTreeBuilder tb = builderWith("html", "div", "p", "li");

        tb.popStackToBefore("div");

        assertEquals(2, tb.getStack().size());
        assertEquals("div", tb.getStack().get(1).nodeName());
    }

    // Tests table-context stack clearing.
    @Test
    public void testClearStackToTableContexts_removesUntilBoundary() {
        HtmlTreeBuilder tb = builderWith("html", "table", "tbody", "tr", "td");

        tb.clearStackToTableBodyContext();
        assertEquals("tbody", tb.getStack().get(tb.getStack().size() - 1).nodeName());

        tb = builderWith("html", "table", "tr", "td");
        tb.clearStackToTableRowContext();
        assertEquals("tr", tb.getStack().get(tb.getStack().size() - 1).nodeName());

        tb = builderWith("html", "table", "tbody", "tr", "td");
        tb.clearStackToTableContext();
        assertEquals("table", tb.getStack().get(tb.getStack().size() - 1).nodeName());
    }

    // Tests onStack, getFromStack, and removeFromStack behavior.
    @Test
    public void testStackQueries_onStackGetFromStackRemoveFromStack() {
        HtmlTreeBuilder tb = builderWith("html");
        Element body1 = el("body");
        body1.attr("id", "first");
        Element body2 = el("body");
        body2.attr("id", "second");

        assertFalse(tb.onStack(body1));

        tb.push(body1);
        tb.push(body2);

        assertTrue(tb.onStack(body2));
        assertSame(body2, tb.getFromStack("body"));
        assertNull(tb.getFromStack("span"));

        assertTrue(tb.removeFromStack(body1));
        assertFalse(tb.onStack(body1));
        assertFalse(tb.removeFromStack(body1));
    }

    // Tests stack insertion, replacement, and aboveOnStack.
    @Test
    public void testStackUpdates_insertOnStackAfterReplaceOnStackAboveOnStack() {
        HtmlTreeBuilder tb = builderWith("html", "body");
        Element body = tb.getStack().get(1);
        Element p = el("p");

        tb.insertOnStackAfter(body, p);
        assertSame(p, tb.getStack().get(2));

        Element div = el("div");
        tb.replaceOnStack(p, div);
        assertSame(div, tb.getStack().get(2));

        assertEquals("body", tb.aboveOnStack(div).nodeName());
    }

    // Tests regular scope search with and without a table boundary.
    @Test
    public void testInScope_honorsScopeBoundary() {
        HtmlTreeBuilder tb = builderWith("html", "body", "div");
        assertTrue(tb.inScope("div"));

        tb = builderWith("html", "table", "tr");
        assertFalse(tb.inScope("div"));
    }

    // Tests table scope detection.
    @Test
    public void testInTableScope_detectsTargetAndBoundary() {
        HtmlTreeBuilder tb = builderWith("html", "table", "tr");
        assertTrue(tb.inTableScope("tr"));
        assertFalse(tb.inTableScope("div"));
    }

    // Tests framesetOk default and update.
    @Test
    public void testFramesetOk_defaultTrueAndCanBeUpdated() {
        HtmlTreeBuilder tb = newBuilder();
        assertTrue(tb.framesetOk());

        tb.framesetOk(false);
        assertFalse(tb.framesetOk());
    }

    // Tests insertion mode transition and original state marking.
    @Test
    public void testTransitionAndOriginalState_keepMarkedState() {
        HtmlTreeBuilder tb = newBuilder();
        tb.transition(HtmlTreeBuilderState.InBody);
        tb.markInsertionMode();
        assertSame(HtmlTreeBuilderState.InBody, tb.originalState());

        tb.transition(HtmlTreeBuilderState.InTable);
        assertSame(HtmlTreeBuilderState.InTable, tb.state());
        assertSame(HtmlTreeBuilderState.InBody, tb.originalState());
    }

    // Tests pending table character list.
    @Test
    public void testPendingTableCharacters_areSettable() {
        HtmlTreeBuilder tb = newBuilder();

        tb.newPendingTableCharacters();
        assertTrue(tb.getPendingTableCharacters().isEmpty());

        tb.setPendingTableCharacters(Arrays.asList("a"));
        assertEquals(Arrays.asList("a"), tb.getPendingTableCharacters());
    }

    // Tests base URI update only from the first base element.
    @Test
    public void testMaybeSetBaseUri_usesFirstBaseHrefOnly() {
        HtmlTreeBuilder tb = newBuilder();
        Element base1 = el("base");
        base1.attr("href", "http://example.org/base");

        tb.maybeSetBaseUri(base1);
        assertEquals("http://example.org/base", tb.getBaseUri());

        Element base2 = el("base");
        base2.attr("href", "http://example.net/another");
        tb.maybeSetBaseUri(base2);
        assertEquals("http://example.org/base", tb.getBaseUri());
    }

    // Tests insert(Element) appends to the current node and the stack.
    @Test
    public void testInsertElement_appendsToCurrentAndStack() {
        HtmlTreeBuilder tb = builderWith("html");
        Element div = el("div");

        tb.insert(div);

        assertSame(div, tb.getStack().get(1));
        assertSame(div, tb.getStack().get(0).childNodes().get(0));
    }

    // Tests insertStartTag creates a new element and pushes it.
    @Test
    public void testInsertStartTag_createsAndPushesElement() {
        HtmlTreeBuilder tb = builderWith("html");

        Element p = tb.insertStartTag("p");

        assertEquals("p", p.nodeName());
        assertEquals(2, tb.getStack().size());
        assertSame(p, tb.getStack().get(1));
    }

    // Tests active formatting elements de-duplication at the third duplicate.
    @Test
    public void testPushActiveFormattingElements_removesThirdDuplicate() {
        HtmlTreeBuilder tb = newBuilder();
        Element first = el("b");
        Element second = el("b");
        Element third = el("b");
        Element fourth = el("b");

        tb.pushActiveFormattingElements(first);
        tb.pushActiveFormattingElements(second);
        tb.pushActiveFormattingElements(third);
        tb.pushActiveFormattingElements(fourth);

        assertFalse(tb.isInActiveFormattingElements(first));
        assertTrue(tb.isInActiveFormattingElements(second));
        assertTrue(tb.isInActiveFormattingElements(third));
        assertTrue(tb.isInActiveFormattingElements(fourth));
    }

    // Tests that clearing to the last marker keeps elements below the marker.
    @Test
    public void testClearFormattingElementsToLastMarker_keepsBelowMarker() {
        HtmlTreeBuilder tb = newBuilder();
        Element b = el("b");
        tb.pushActiveFormattingElements(b);
        tb.insertMarkerToFormattingElements();

        Element i = el("i");
        tb.pushActiveFormattingElements(i);

        tb.clearFormattingElementsToLastMarker();

        assertSame(b, tb.lastFormattingElement());
        assertSame(b, tb.getActiveFormattingElement("b"));
        assertNull(tb.getActiveFormattingElement("i"));
        assertTrue(tb.isInActiveFormattingElements(b));
    }

    // ========== New test cases for uncovered coverage ==========

    @Test
    public void testInListItemScope_detectsTargetAndBoundary() {
        HtmlTreeBuilder tb = builderWith("html", "body", "li");
        assertTrue(tb.inListItemScope("li"));
        assertFalse(tb.inListItemScope("div"));

        // Table element breaks scope
        tb = builderWith("html", "table", "li");
        assertFalse(tb.inListItemScope("li"));
    }

    @Test
    public void testInButtonScope_detectsTargetAndBoundary() {
        HtmlTreeBuilder tb = builderWith("html", "body", "button");
        assertTrue(tb.inButtonScope("button"));
        assertFalse(tb.inButtonScope("div"));

        // Table element breaks scope
        tb = builderWith("html", "table", "button");
        assertFalse(tb.inButtonScope("button"));
    }

    @Test
    public void testResetInsertionMode_switchesStateBasedOnStack() {
        // Without table/select context → InBody
        HtmlTreeBuilder tb = builderWith("html", "body", "div");
        tb.transition(HtmlTreeBuilderState.InTable);
        tb.resetInsertionMode();
        assertEquals(HtmlTreeBuilderState.InBody, tb.state());

        // With table > tbody > tr > td → InCell
        tb = builderWith("html", "table", "tbody", "tr", "td");
        tb.transition(HtmlTreeBuilderState.InBody);
        tb.resetInsertionMode();
        assertEquals(HtmlTreeBuilderState.InCell, tb.state());

        // With select → InSelect
        tb = builderWith("html", "select");
        tb.transition(HtmlTreeBuilderState.InBody);
        tb.resetInsertionMode();
        assertEquals(HtmlTreeBuilderState.InSelect, tb.state());
    }

    @Test
    public void testIsSpecial_detectsSpecialElements() {
        HtmlTreeBuilder tb = newBuilder();
        assertTrue(tb.isSpecial(el("ul")));
        assertTrue(tb.isSpecial(el("table")));
        assertTrue(tb.isSpecial(el("div")));
        assertFalse(tb.isSpecial(el("span")));
        assertFalse(tb.isSpecial(el("b")));
    }

    @Test
    public void testSetFormElement_andGetFormElement() {
        HtmlTreeBuilder tb = newBuilder();
        assertNull(tb.getFormElement());

        Element form = el("form");
        tb.setFormElement(form);
        assertSame(form, tb.getFormElement());

        tb.setFormElement(null);
        assertNull(tb.getFormElement());
    }

    @Test
    public void testSetHeadElement_andGetHeadElement() {
        HtmlTreeBuilder tb = newBuilder();
        assertNull(tb.getHeadElement());

        Element head = el("head");
        tb.setHeadElement(head);
        assertSame(head, tb.getHeadElement());
    }

    @Test
    public void testMaybeSetBaseUri_withNoHref_doesNotChangeBaseUri() {
        HtmlTreeBuilder tb = newBuilder();
        tb.setBaseUri("http://initial.example.com/");
        Element base = el("base");
        // no href attribute
        tb.maybeSetBaseUri(base);
        assertEquals("http://initial.example.com/", tb.getBaseUri());
    }

    @Test
    public void testPopStackToClose_withNoMatch_doesNotPop() {
        HtmlTreeBuilder tb = builderWith("html", "div", "p");
        tb.popStackToClose("span");
        assertEquals(3, tb.getStack().size()); // unchanged
    }

    @Test
    public void testClearStackToTableContexts_withoutRelevantElement_doesNothing() {
        // clearStackToTableBodyContext when no tbody/thead/tfoot
        HtmlTreeBuilder tb = builderWith("html", "body", "div");
        tb.clearStackToTableBodyContext();
        assertEquals(3, tb.getStack().size());

        // clearStackToTableRowContext when no tr
        tb = builderWith("html", "body", "div");
        tb.clearStackToTableRowContext();
        assertEquals(3, tb.getStack().size());

        // clearStackToTableContext when no table
        tb = builderWith("html", "body", "div");
        tb.clearStackToTableContext();
        assertEquals(3, tb.getStack().size());
    }

    @Test
    public void testActiveFormattingElements_queriesForNonExistent() {
        HtmlTreeBuilder tb = newBuilder();
        Element b = el("b");
        assertFalse(tb.isInActiveFormattingElements(b));
        assertNull(tb.getActiveFormattingElement("b"));
    }
}