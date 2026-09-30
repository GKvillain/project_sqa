package org.jsoup.parser;

import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Comment;
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
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
    }

    // Tests initial parse setup and default state
    @Test
    public void testInitialiseParse_defaultInitialization_setsDefaultValues() {
        assertEquals(HtmlTreeBuilderState.Initial, treeBuilder.state());
        assertNull(treeBuilder.originalState());
        assertNull(treeBuilder.getHeadElement());
        assertNull(treeBuilder.getFormElement());
        assertTrue(treeBuilder.framesetOk());
        assertFalse(treeBuilder.isFosterInserts());
        assertFalse(treeBuilder.isFragmentParsing());
        assertEquals("http://example.com", treeBuilder.getBaseUri());
        assertNotNull(treeBuilder.getDocument());
    }

    // Tests stack push, pop, retrieval, and presence checking
    @Test
    public void testStackOperations_pushAndPop_maintainsStackState() {
        Element html = new Element(Tag.valueOf("html"), "http://example.com");
        Element body = new Element(Tag.valueOf("body"), "http://example.com");
        Element div = new Element(Tag.valueOf("div"), "http://example.com");

        treeBuilder.push(html);
        treeBuilder.push(body);
        treeBuilder.push(div);

        assertEquals(3, treeBuilder.getStack().size());
        assertTrue(treeBuilder.onStack(body));
        assertEquals(div, treeBuilder.getFromStack("div"));
        assertNull(treeBuilder.getFromStack("span"));

        Element popped = treeBuilder.pop();
        assertEquals(div, popped);
        assertFalse(treeBuilder.onStack(div));

        Element above = treeBuilder.aboveOnStack(body);
        assertEquals(html, above);

        boolean removed = treeBuilder.removeFromStack(body);
        assertTrue(removed);
        assertFalse(treeBuilder.onStack(body));
        assertFalse(treeBuilder.removeFromStack(body));
    }

    // Tests stack pop to close specified tag name
    @Test
    public void testPopStackToClose_singleTagName_popsCorrectElements() {
        Element html = new Element(Tag.valueOf("html"), "http://example.com");
        Element body = new Element(Tag.valueOf("body"), "http://example.com");
        Element p = new Element(Tag.valueOf("p"), "http://example.com");
        Element span = new Element(Tag.valueOf("span"), "http://example.com");

        treeBuilder.push(html);
        treeBuilder.push(body);
        treeBuilder.push(p);
        treeBuilder.push(span);

        treeBuilder.popStackToClose("p");

        assertEquals(2, treeBuilder.getStack().size());
        assertFalse(treeBuilder.onStack(span));
        assertFalse(treeBuilder.onStack(p));
        assertTrue(treeBuilder.onStack(body));
    }

    // Tests stack pop to close using an array of tag names
    @Test
    public void testPopStackToClose_multipleTagNames_popsToFirstMatching() {
        Element html = new Element(Tag.valueOf("html"), "http://example.com");
        Element body = new Element(Tag.valueOf("body"), "http://example.com");
        Element ul = new Element(Tag.valueOf("ul"), "http://example.com");
        Element li = new Element(Tag.valueOf("li"), "http://example.com");

        treeBuilder.push(html);
        treeBuilder.push(body);
        treeBuilder.push(ul);
        treeBuilder.push(li);

        treeBuilder.popStackToClose(HtmlTreeBuilder.TagSearchList);

        assertEquals(2, treeBuilder.getStack().size());
        assertFalse(treeBuilder.onStack(li));
        assertFalse(treeBuilder.onStack(ul));
    }

    // Tests stack pop until before specified tag name
    @Test
    public void testPopStackToBefore_existingElement_keepsTargetElementOnStack() {
        Element html = new Element(Tag.valueOf("html"), "http://example.com");
        Element table = new Element(Tag.valueOf("table"), "http://example.com");
        Element tr = new Element(Tag.valueOf("tr"), "http://example.com");

        treeBuilder.push(html);
        treeBuilder.push(table);
        treeBuilder.push(tr);

        treeBuilder.popStackToBefore("table");

        assertEquals(2, treeBuilder.getStack().size());
        assertTrue(treeBuilder.onStack(table));
        assertFalse(treeBuilder.onStack(tr));
    }

    // Tests insertOnStackAfter and replaceOnStack operations
    @Test
    public void testInsertOnStackAfterAndReplaceOnStack_validElements_replacesAndInserts() {
        Element html = new Element(Tag.valueOf("html"), "http://example.com");
        Element div1 = new Element(Tag.valueOf("div"), "http://example.com");
        Element div2 = new Element(Tag.valueOf("div"), "http://example.com");
        Element span = new Element(Tag.valueOf("span"), "http://example.com");

        treeBuilder.push(html);
        treeBuilder.push(div1);

        treeBuilder.insertOnStackAfter(div1, div2);
        assertEquals(3, treeBuilder.getStack().size());
        assertEquals(div2, treeBuilder.getStack().get(2));

        treeBuilder.replaceOnStack(div2, span);
        assertEquals(3, treeBuilder.getStack().size());
        assertEquals(span, treeBuilder.getStack().get(2));
        assertFalse(treeBuilder.onStack(div2));
    }

    // Tests inScope checks across regular scope, list item scope, button scope, and table scope
    @Test
    public void testInScopeMethods_variousScopes_returnsExpectedResults() {
        Element html = new Element(Tag.valueOf("html"), "http://example.com");
        Element table = new Element(Tag.valueOf("table"), "http://example.com");
        Element p = new Element(Tag.valueOf("p"), "http://example.com");

        treeBuilder.push(html);
        treeBuilder.push(table);
        treeBuilder.push(p);

        assertTrue(treeBuilder.inScope("p"));
        assertTrue(treeBuilder.inTableScope("table"));
        assertFalse(treeBuilder.inTableScope("p"));
        assertFalse(treeBuilder.inListItemScope("li"));
        assertFalse(treeBuilder.inButtonScope("button"));
    }

    // Tests inSpecificScope depth search boundary when stack depth exceeds MaxScopeSearchDepth
    @Test
    public void testInScope_deepStackExceedingMaxScopeSearchDepth_handlesDepthSafely() {
        Element html = new Element(Tag.valueOf("html"), "http://example.com");
        treeBuilder.push(html);

        for (int i = 0; i < HtmlTreeBuilder.MaxScopeSearchDepth + 20; i++) {
            Element div = new Element(Tag.valueOf("div"), "http://example.com");
            treeBuilder.push(div);
        }

        Element p = new Element(Tag.valueOf("p"), "http://example.com");
        treeBuilder.push(p);

        assertTrue(treeBuilder.inScope("p"));
        assertFalse(treeBuilder.inScope("nonexistent"));
    }

    // Tests inSelectScope behavior with valid and invalid options
    @Test
    public void testInSelectScope_optionsInScope_returnsCorrectStatus() {
        Element html = new Element(Tag.valueOf("html"), "http://example.com");
        Element select = new Element(Tag.valueOf("select"), "http://example.com");
        Element optgroup = new Element(Tag.valueOf("optgroup"), "http://example.com");
        Element option = new Element(Tag.valueOf("option"), "http://example.com");

        treeBuilder.push(html);
        treeBuilder.push(select);
        treeBuilder.push(optgroup);
        treeBuilder.push(option);

        assertTrue(treeBuilder.inSelectScope("option"));
        assertTrue(treeBuilder.inSelectScope("optgroup"));
        assertFalse(treeBuilder.inSelectScope("div"));
    }

    // Tests active formatting elements push, limit to 3 duplicate entries, and pop
    @Test
    public void testActiveFormattingElements_pushDuplicates_limitsToThreeEntries() {
        Element b1 = new Element(Tag.valueOf("b"), "http://example.com");
        Element b2 = new Element(Tag.valueOf("b"), "http://example.com");
        Element b3 = new Element(Tag.valueOf("b"), "http://example.com");
        Element b4 = new Element(Tag.valueOf("b"), "http://example.com");

        treeBuilder.pushActiveFormattingElements(b1);
        treeBuilder.pushActiveFormattingElements(b2);
        treeBuilder.pushActiveFormattingElements(b3);
        treeBuilder.pushActiveFormattingElements(b4);

        assertFalse(treeBuilder.isInActiveFormattingElements(b1));
        assertTrue(treeBuilder.isInActiveFormattingElements(b2));
        assertTrue(treeBuilder.isInActiveFormattingElements(b3));
        assertTrue(treeBuilder.isInActiveFormattingElements(b4));
        assertEquals(b4, treeBuilder.lastFormattingElement());

        Element removed = treeBuilder.removeLastFormattingElement();
        assertEquals(b4, removed);
    }

    // Tests clearing formatting elements up to the last marker
    @Test
    public void testClearFormattingElementsToLastMarker_withMarker_clearsOnlyAfterMarker() {
        Element a = new Element(Tag.valueOf("a"), "http://example.com");
        Element b = new Element(Tag.valueOf("b"), "http://example.com");

        treeBuilder.pushActiveFormattingElements(a);
        treeBuilder.insertMarkerToFormattingElements();
        treeBuilder.pushActiveFormattingElements(b);

        assertEquals(b, treeBuilder.getActiveFormattingElement("b"));

        treeBuilder.clearFormattingElementsToLastMarker();

        assertNull(treeBuilder.getActiveFormattingElement("b"));
        assertEquals(a, treeBuilder.getActiveFormattingElement("a"));
    }

    // Tests reconstruction of active formatting elements
    @Test
    public void testReconstructFormattingElements_formattingElementNotOnStack_reconstructsOnStack() {
        Element html = new Element(Tag.valueOf("html"), "http://example.com");
        Element body = new Element(Tag.valueOf("body"), "http://example.com");
        treeBuilder.push(html);
        treeBuilder.push(body);

        Element b = new Element(Tag.valueOf("b"), "http://example.com");
        treeBuilder.pushActiveFormattingElements(b);

        treeBuilder.reconstructFormattingElements();

        assertTrue(treeBuilder.onStack(treeBuilder.getStack().get(treeBuilder.getStack().size() - 1)));
        assertEquals("b", treeBuilder.getStack().get(treeBuilder.getStack().size() - 1).nodeName());
    }

    // Tests generateImpliedEndTags with and without exclusions
    @Test
    public void testGenerateImpliedEndTags_withImpliedTags_popsExpectedElements() {
        Element html = new Element(Tag.valueOf("html"), "http://example.com");
        Element body = new Element(Tag.valueOf("body"), "http://example.com");
        Element p = new Element(Tag.valueOf("p"), "http://example.com");
        Element li = new Element(Tag.valueOf("li"), "http://example.com");

        treeBuilder.push(html);
        treeBuilder.push(body);
        treeBuilder.push(p);
        treeBuilder.push(li);

        treeBuilder.generateImpliedEndTags("p");
        assertFalse(treeBuilder.onStack(li));
        assertTrue(treeBuilder.onStack(p));

        treeBuilder.generateImpliedEndTags();
        assertFalse(treeBuilder.onStack(p));
    }

    // Tests isSpecial method for standard HTML tags
    @Test
    public void testIsSpecial_specialAndNonSpecialTags_returnsExpectedBoolean() {
        Element p = new Element(Tag.valueOf("p"), "http://example.com");
        Element div = new Element(Tag.valueOf("div"), "http://example.com");
        Element span = new Element(Tag.valueOf("span"), "http://example.com");
        Element custom = new Element(Tag.valueOf("custom-tag"), "http://example.com");

        assertTrue(treeBuilder.isSpecial(p));
        assertTrue(treeBuilder.isSpecial(div));
        assertFalse(treeBuilder.isSpecial(span));
        assertFalse(treeBuilder.isSpecial(custom));
    }

    // Tests resetInsertionMode transition based on context stack
    @Test
    public void testResetInsertionMode_variousStackElements_transitionsToCorrectState() {
        Element html = new Element(Tag.valueOf("html"), "http://example.com");
        Element select = new Element(Tag.valueOf("select"), "http://example.com");

        treeBuilder.push(html);
        treeBuilder.push(select);
        treeBuilder.resetInsertionMode();
        assertEquals(HtmlTreeBuilderState.InSelect, treeBuilder.state());

        treeBuilder.pop();
        Element table = new Element(Tag.valueOf("table"), "http://example.com");
        treeBuilder.push(table);
        treeBuilder.resetInsertionMode();
        assertEquals(HtmlTreeBuilderState.InTable, treeBuilder.state());

        Element tr = new Element(Tag.valueOf("tr"), "http://example.com");
        treeBuilder.push(tr);
        treeBuilder.resetInsertionMode();
        assertEquals(HtmlTreeBuilderState.InRow, treeBuilder.state());

        Element td = new Element(Tag.valueOf("td"), "http://example.com");
        treeBuilder.push(td);
        treeBuilder.resetInsertionMode();
        assertEquals(HtmlTreeBuilderState.InCell, treeBuilder.state());
    }

    // Tests maybeSetBaseUri when valid href attribute is present
    @Test
    public void testMaybeSetBaseUri_validHref_updatesBaseUri() {
        Attributes attrs = new Attributes();
        attrs.put("href", "http://example.com/updated/");
        Element base = new Element(Tag.valueOf("base"), "http://example.com", attrs);

        treeBuilder.maybeSetBaseUri(base);

        assertEquals("http://example.com/updated/", treeBuilder.getBaseUri());
        assertEquals("http://example.com/updated/", treeBuilder.getDocument().baseUri());

        // Second base element should be ignored
        Attributes attrs2 = new Attributes();
        attrs2.put("href", "http://example.com/ignored/");
        Element base2 = new Element(Tag.valueOf("base"), "http://example.com", attrs2);
        treeBuilder.maybeSetBaseUri(base2);
        assertEquals("http://example.com/updated/", treeBuilder.getBaseUri());
    }

    // Tests fragment parsing with context element
    @Test
    public void testParseFragment_divContext_returnsParsedNodes() {
        Element context = new Element(Tag.valueOf("div"), "http://example.com");
        List<Node> nodes = treeBuilder.parseFragment("<p>Fragment content</p>", context, "http://example.com", ParseErrorList.noTracking(), ParseSettings.htmlDefault);

        assertNotNull(nodes);
        assertFalse(nodes.isEmpty());
        Element p = (Element) nodes.get(0);
        assertEquals("p", p.tagName());
        assertEquals("Fragment content", p.text());
    }

    // Tests foster insertion before table element
    @Test
    public void testInsertInFosterParent_withTableInStack_insertsBeforeTable() {
        Element html = new Element(Tag.valueOf("html"), "http://example.com");
        Element body = new Element(Tag.valueOf("body"), "http://example.com");
        Element table = new Element(Tag.valueOf("table"), "http://example.com");

        treeBuilder.getDocument().appendChild(html);
        html.appendChild(body);
        body.appendChild(table);

        treeBuilder.push(html);
        treeBuilder.push(body);
        treeBuilder.push(table);

        treeBuilder.setFosterInserts(true);
        TextNode textNode = new TextNode("fostered text");
        treeBuilder.insertInFosterParent(textNode);

        assertEquals(textNode, table.previousSibling());
    }

    // Tests pending table characters and transition states
    @Test
    public void testPendingTableCharactersAndTransitions_stateManagement() {
        treeBuilder.newPendingTableCharacters();
        List<String> chars = new ArrayList<>();
        chars.add("a");
        treeBuilder.setPendingTableCharacters(chars);
        assertEquals(1, treeBuilder.getPendingTableCharacters().size());

        treeBuilder.transition(HtmlTreeBuilderState.InBody);
        treeBuilder.markInsertionMode();
        treeBuilder.transition(HtmlTreeBuilderState.InTable);

        assertEquals(HtmlTreeBuilderState.InTable, treeBuilder.state());
        assertEquals(HtmlTreeBuilderState.InBody, treeBuilder.originalState());

        treeBuilder.framesetOk(false);
        assertFalse(treeBuilder.framesetOk());
    }

    // Tests toString method representation
    @Test
    public void testToString_validTreeBuilder_returnsFormattedString() {
        Element html = new Element(Tag.valueOf("html"), "http://example.com");
        treeBuilder.push(html);

        String str = treeBuilder.toString();
        assertNotNull(str);
        assertTrue(str.contains("TreeBuilder"));
        assertTrue(str.contains("state="));
    }
}