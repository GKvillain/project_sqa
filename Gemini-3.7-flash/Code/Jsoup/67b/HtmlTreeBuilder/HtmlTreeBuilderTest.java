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

    private HtmlTreeBuilder builder;

    @Before
    public void setUp() {
        builder = new HtmlTreeBuilder();
        builder.initialiseParse(new StringReader(""), "http://example.com", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
    }

    // Tests initial parse setup and default getters/setters
    @Test
    public void testInitialiseParse_defaultState_initializesCorrectly() {
        assertEquals(HtmlTreeBuilderState.Initial, builder.state());
        assertNull(builder.originalState());
        assertTrue(builder.framesetOk());
        assertNotNull(builder.getDocument());
        assertEquals("http://example.com", builder.getBaseUri());
        assertFalse(builder.isFragmentParsing());
        assertFalse(builder.isFosterInserts());
    }

    // Tests state transitions and marked insertion modes
    @Test
    public void testTransition_validState_updatesStateAndOriginalState() {
        builder.transition(HtmlTreeBuilderState.BeforeHtml);
        assertEquals(HtmlTreeBuilderState.BeforeHtml, builder.state());

        builder.markInsertionMode();
        assertEquals(HtmlTreeBuilderState.BeforeHtml, builder.originalState());

        builder.transition(HtmlTreeBuilderState.InBody);
        assertEquals(HtmlTreeBuilderState.InBody, builder.state());
        assertEquals(HtmlTreeBuilderState.BeforeHtml, builder.originalState());
    }

    // Tests stack push, pop, lookup and removal operations
    @Test
    public void testStackOperations_elementsAdded_maintainsStackOrder() {
        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        Element div = new Element(Tag.valueOf("div"), "");

        builder.push(html);
        builder.push(body);
        builder.push(div);

        assertEquals(3, builder.getStack().size());
        assertTrue(builder.onStack(body));
        assertEquals(div, builder.getFromStack("div"));
        assertEquals(body, builder.aboveOnStack(div));

        Element popped = builder.pop();
        assertEquals(div, popped);
        assertFalse(builder.onStack(div));

        boolean removed = builder.removeFromStack(body);
        assertTrue(removed);
        assertFalse(builder.onStack(body));
        assertEquals(1, builder.getStack().size());
    }

    // Tests popping stack until target element is closed
    @Test
    public void testPopStackToClose_singleAndVarargs_popsCorrectly() {
        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        Element p = new Element(Tag.valueOf("p"), "");
        Element span = new Element(Tag.valueOf("span"), "");

        builder.push(html);
        builder.push(body);
        builder.push(p);
        builder.push(span);

        builder.popStackToClose("p");
        assertFalse(builder.onStack(p));
        assertFalse(builder.onStack(span));
        assertTrue(builder.onStack(body));

        Element table = new Element(Tag.valueOf("table"), "");
        Element tr = new Element(Tag.valueOf("tr"), "");
        builder.push(table);
        builder.push(tr);

        builder.popStackToClose("tr", "table");
        assertFalse(builder.onStack(tr));
        assertTrue(builder.onStack(table));
    }

    // Tests popping stack until just before target element
    @Test
    public void testPopStackToBefore_elementOnStack_removesUntilBeforeTarget() {
        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        Element div = new Element(Tag.valueOf("div"), "");
        Element span = new Element(Tag.valueOf("span"), "");

        builder.push(html);
        builder.push(body);
        builder.push(div);
        builder.push(span);

        builder.popStackToBefore("body");
        assertTrue(builder.onStack(body));
        assertFalse(builder.onStack(div));
        assertFalse(builder.onStack(span));
    }

    // Tests clearing stack to table contexts
    @Test
    public void testClearStackToTableContext_elementsInTable_clearsCorrectly() {
        Element html = new Element(Tag.valueOf("html"), "");
        Element table = new Element(Tag.valueOf("table"), "");
        Element tbody = new Element(Tag.valueOf("tbody"), "");
        Element tr = new Element(Tag.valueOf("tr"), "");
        Element td = new Element(Tag.valueOf("td"), "");

        builder.push(html);
        builder.push(table);
        builder.push(tbody);
        builder.push(tr);
        builder.push(td);

        builder.clearStackToTableRowContext();
        assertEquals("tr", builder.getStack().get(builder.getStack().size() - 1).nodeName());

        builder.push(td);
        builder.clearStackToTableBodyContext();
        assertEquals("tbody", builder.getStack().get(builder.getStack().size() - 1).nodeName());

        builder.clearStackToTableContext();
        assertEquals("table", builder.getStack().get(builder.getStack().size() - 1).nodeName());
    }

    // Tests inserting on stack after element and replacing on stack
    @Test
    public void testInsertOnStackAfterAndReplace_validElements_modifiesStack() {
        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        Element div1 = new Element(Tag.valueOf("div"), "");
        Element div2 = new Element(Tag.valueOf("div"), "");
        Element div3 = new Element(Tag.valueOf("div"), "");

        builder.push(html);
        builder.push(body);
        builder.push(div1);

        builder.insertOnStackAfter(body, div2);
        assertEquals(div2, builder.getStack().get(2));

        builder.replaceOnStack(div1, div3);
        assertFalse(builder.onStack(div1));
        assertTrue(builder.onStack(div3));
    }

    // Tests scope checks for default, list, button, table and select scopes
    @Test
    public void testInScope_differentScopes_returnsExpected() {
        Element html = new Element(Tag.valueOf("html"), "");
        Element table = new Element(Tag.valueOf("table"), "");
        Element td = new Element(Tag.valueOf("td"), "");
        Element p = new Element(Tag.valueOf("p"), "");

        builder.push(html);
        builder.push(table);
        builder.push(td);
        builder.push(p);

        assertTrue(builder.inScope("p"));
        assertTrue(builder.inTableScope("table"));
        assertFalse(builder.inButtonScope("button"));
        assertTrue(builder.inListItemScope("td"));

        Element select = new Element(Tag.valueOf("select"), "");
        Element option = new Element(Tag.valueOf("option"), "");
        builder.push(select);
        builder.push(option);

        assertTrue(builder.inSelectScope("option"));
        assertFalse(builder.inSelectScope("input"));
    }

    // Tests active formatting elements list operations and marker handling
    @Test
    public void testFormattingElements_pushAndRemove_handlesMarkersAndLimits() {
        Element b1 = new Element(Tag.valueOf("b"), "");
        Element b2 = new Element(Tag.valueOf("b"), "");
        Element b3 = new Element(Tag.valueOf("b"), "");
        Element b4 = new Element(Tag.valueOf("b"), "");

        builder.pushActiveFormattingElements(b1);
        builder.pushActiveFormattingElements(b2);
        builder.pushActiveFormattingElements(b3);
        assertTrue(builder.isInActiveFormattingElements(b1));

        // Adding 4th same element removes earliest
        builder.pushActiveFormattingElements(b4);
        assertFalse(builder.isInActiveFormattingElements(b1));
        assertTrue(builder.isInActiveFormattingElements(b4));

        assertEquals(b4, builder.lastFormattingElement());
        assertEquals(b4, builder.getActiveFormattingElement("b"));

        Element i = new Element(Tag.valueOf("i"), "");
        builder.replaceActiveFormattingElement(b4, i);
        assertFalse(builder.isInActiveFormattingElements(b4));
        assertTrue(builder.isInActiveFormattingElements(i));

        builder.insertMarkerToFormattingElements();
        assertNull(builder.getActiveFormattingElement("i"));

        builder.clearFormattingElementsToLastMarker();
        assertTrue(builder.isInActiveFormattingElements(i));

        builder.removeFromActiveFormattingElements(i);
        assertFalse(builder.isInActiveFormattingElements(i));
    }

    // Tests reconstructing formatting elements when closed outside formatting scope
    @Test
    public void testReconstructFormattingElements_unclosedFormattingElements_recreatesElements() {
        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        Element b = new Element(Tag.valueOf("b"), "");

        builder.push(html);
        builder.push(body);
        builder.pushActiveFormattingElements(b);

        builder.reconstructFormattingElements();
        assertEquals(3, builder.getStack().size());
        assertEquals("b", builder.getStack().get(2).nodeName());
    }

    // Tests base URI resolution from base tag
    @Test
    public void testMaybeSetBaseUri_validBaseElement_updatesBaseUri() {
        Attributes attrs = new Attributes();
        attrs.put("href", "http://example.com/sub/");
        Element base = new Element(Tag.valueOf("base"), "http://example.com", attrs);

        builder.maybeSetBaseUri(base);
        assertEquals("http://example.com/sub/", builder.getBaseUri());

        // Subsequent base tag should be ignored
        Attributes attrs2 = new Attributes();
        attrs2.put("href", "http://other.com/");
        Element base2 = new Element(Tag.valueOf("base"), "http://example.com", attrs2);
        builder.maybeSetBaseUri(base2);
        assertEquals("http://example.com/sub/", builder.getBaseUri());
    }

    // Tests insertion of comment and character tokens
    @Test
    public void testInsertCommentAndCharacter_validTokens_insertsIntoCurrentElement() {
        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        builder.push(html);
        builder.push(body);

        Token.Comment commentToken = new Token.Comment();
        commentToken.getData().append("test comment");
        builder.insert(commentToken);

        Token.Character charToken = new Token.Character();
        charToken.data("test text");
        builder.insert(charToken);

        assertEquals(2, body.childNodeSize());
        assertTrue(body.childNode(0) instanceof Comment);
        assertEquals("test comment", ((Comment) body.childNode(0)).getData());
        assertTrue(body.childNode(1) instanceof TextNode);
        assertEquals("test text", ((TextNode) body.childNode(1)).text());
    }

    // Tests foster parenting behavior when fosterInserts is enabled
    @Test
    public void testInsertInFosterParent_fosterEnabled_insertsBeforeTable() {
        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        Element table = new Element(Tag.valueOf("table"), "");
        body.appendChild(table);

        builder.push(html);
        builder.push(body);
        builder.push(table);

        builder.setFosterInserts(true);
        assertTrue(builder.isFosterInserts());

        TextNode textNode = new TextNode("fostered text");
        builder.insertInFosterParent(textNode);

        assertEquals(2, body.childNodeSize());
        assertEquals(textNode, body.childNode(0));
        assertEquals(table, body.childNode(1));
    }

    // Tests resetInsertionMode transition for various stack structures
    @Test
    public void testResetInsertionMode_variousStackElements_transitionsCorrectly() {
        Element html = new Element(Tag.valueOf("html"), "");
        Element select = new Element(Tag.valueOf("select"), "");
        builder.push(html);
        builder.push(select);

        builder.resetInsertionMode();
        assertEquals(HtmlTreeBuilderState.InSelect, builder.state());

        builder.pop();
        Element table = new Element(Tag.valueOf("table"), "");
        builder.push(table);
        builder.resetInsertionMode();
        assertEquals(HtmlTreeBuilderState.InTable, builder.state());

        builder.pop();
        Element body = new Element(Tag.valueOf("body"), "");
        builder.push(body);
        builder.resetInsertionMode();
        assertEquals(HtmlTreeBuilderState.InBody, builder.state());
    }

    // Tests generating implied end tags
    @Test
    public void testGenerateImpliedEndTags_withAndWithoutExclusion_popsImpliedTags() {
        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        Element p = new Element(Tag.valueOf("p"), "");
        Element li = new Element(Tag.valueOf("li"), "");

        builder.push(html);
        builder.push(body);
        builder.push(p);
        builder.push(li);

        builder.generateImpliedEndTags("p");
        assertFalse(builder.onStack(li));
        assertTrue(builder.onStack(p));

        builder.generateImpliedEndTags();
        assertFalse(builder.onStack(p));
        assertTrue(builder.onStack(body));
    }

    // Tests isSpecial check for HTML spec special tags
    @Test
    public void testIsSpecial_knownSpecialAndNormalElements_identifiesCorrectly() {
        assertTrue(builder.isSpecial(new Element(Tag.valueOf("address"), "")));
        assertTrue(builder.isSpecial(new Element(Tag.valueOf("div"), "")));
        assertTrue(builder.isSpecial(new Element(Tag.valueOf("p"), "")));
        assertTrue(builder.isSpecial(new Element(Tag.valueOf("table"), "")));
        assertTrue(builder.isSpecial(new Element(Tag.valueOf("script"), "")));

        assertFalse(builder.isSpecial(new Element(Tag.valueOf("span"), "")));
        assertFalse(builder.isSpecial(new Element(Tag.valueOf("custom-tag"), "")));
    }

    // Tests parsing HTML fragments with context elements
    @Test
    public void testParseFragment_withContextElement_parsesCorrectNodes() {
        Element context = new Element(Tag.valueOf("div"), "http://example.com");
        List<Node> nodes = builder.parseFragment("<p>Fragment text</p>", context, "http://example.com", ParseErrorList.noTracking(), ParseSettings.htmlDefault);

        assertNotNull(nodes);
        assertFalse(nodes.isEmpty());
        Element p = (Element) nodes.get(0);
        assertEquals("p", p.nodeName());
        assertEquals("Fragment text", p.text());
    }

    // Tests form element, head element and pending table characters state
    @Test
    public void testFormHeadAndPendingCharacters_settersAndGetters_workProperly() {
        Element head = new Element(Tag.valueOf("head"), "");
        builder.setHeadElement(head);
        assertEquals(head, builder.getHeadElement());

        FormElement form = new FormElement(Tag.valueOf("form"), "", new Attributes());
        builder.setFormElement(form);
        assertEquals(form, builder.getFormElement());

        builder.newPendingTableCharacters();
        assertNotNull(builder.getPendingTableCharacters());
        assertTrue(builder.getPendingTableCharacters().isEmpty());

        List<String> list = new ArrayList<>();
        list.add("test");
        builder.setPendingTableCharacters(list);
        assertEquals(1, builder.getPendingTableCharacters().size());
        assertEquals("test", builder.getPendingTableCharacters().get(0));
    }

    // Tests toString method representation
    @Test
    public void testToString_initializedBuilder_returnsFormattedString() {
        Element html = new Element(Tag.valueOf("html"), "");
        builder.push(html);
        String result = builder.toString();

        assertNotNull(result);
        assertTrue(result.contains("TreeBuilder"));
        assertTrue(result.contains("state="));
    }
}