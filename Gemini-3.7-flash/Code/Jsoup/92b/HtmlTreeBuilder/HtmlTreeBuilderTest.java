package org.jsoup.parser;

import org.jsoup.Jsoup;
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
    private Parser parser;

    @Before
    public void setUp() {
        treeBuilder = new HtmlTreeBuilder();
        parser = new Parser(treeBuilder);
    }

    // Tests initial state and default parse settings
    @Test
    public void testInitialiseParse_defaultState_initializesCorrectly() {
        treeBuilder.initialiseParse(new StringReader("<div></div>"), "http://example.com/", parser);
        assertEquals(HtmlTreeBuilderState.Initial, treeBuilder.state());
        assertNull(treeBuilder.originalState());
        assertEquals("http://example.com/", treeBuilder.getBaseUri());
        assertTrue(treeBuilder.framesetOk());
        assertFalse(treeBuilder.isFosterInserts());
        assertFalse(treeBuilder.isFragmentParsing());
        assertNotNull(treeBuilder.getDocument());
    }

    // Tests normal HTML fragment parsing
    @Test
    public void testParseFragment_divContext_returnsNodes() {
        Element context = new Element(Tag.valueOf("div"), "");
        List<Node> nodes = treeBuilder.parseFragment("<p>One</p><p>Two</p>", context, "http://example.com/", parser);
        assertEquals(2, nodes.size());
        assertEquals("p", ((Element) nodes.get(0)).tagName());
        assertEquals("One", ((Element) nodes.get(0)).text());
        assertEquals("Two", ((Element) nodes.get(1)).text());
    }

    // Tests fragment parsing with null context
    @Test
    public void testParseFragment_nullContext_returnsNodes() {
        List<Node> nodes = treeBuilder.parseFragment("<div>Hello</div>", null, "http://example.com/", parser);
        assertTrue(nodes.size() > 0);
        Element html = (Element) nodes.get(0);
        assertEquals("html", html.tagName());
    }

    // Tests fragment parsing with form context to associate form controls
    @Test
    public void testParseFragment_formContext_associatesFormControl() {
        FormElement form = new FormElement(Tag.valueOf("form"), "", new Attributes());
        List<Node> nodes = treeBuilder.parseFragment("<input type='text' name='q' />", form, "http://example.com/", parser);
        assertEquals(1, nodes.size());
        assertEquals(1, form.elements().size());
        assertEquals("input", form.elements().get(0).tagName());
    }

    // Tests state transition and markInsertionMode
    @Test
    public void testTransitionAndMarkInsertionMode_validTransitions_storesStates() {
        treeBuilder.initialiseParse(new StringReader(""), "", parser);
        treeBuilder.transition(HtmlTreeBuilderState.InBody);
        assertEquals(HtmlTreeBuilderState.InBody, treeBuilder.state());

        treeBuilder.markInsertionMode();
        assertEquals(HtmlTreeBuilderState.InBody, treeBuilder.originalState());

        treeBuilder.transition(HtmlTreeBuilderState.InTable);
        assertEquals(HtmlTreeBuilderState.InTable, treeBuilder.state());
        assertEquals(HtmlTreeBuilderState.InBody, treeBuilder.originalState());
    }

    // Tests maybeSetBaseUri sets base URI on document only once
    @Test
    public void testMaybeSetBaseUri_firstAndSubsequentBaseTags_setsOnlyFirst() {
        treeBuilder.initialiseParse(new StringReader(""), "http://initial.com/", parser);

        Element base1 = new Element(Tag.valueOf("base"), "http://initial.com/");
        base1.attr("href", "http://first.com/");
        treeBuilder.maybeSetBaseUri(base1);
        assertEquals("http://first.com/", treeBuilder.getBaseUri());

        Element base2 = new Element(Tag.valueOf("base"), "http://first.com/");
        base2.attr("href", "http://second.com/");
        treeBuilder.maybeSetBaseUri(base2);
        assertEquals("http://first.com/", treeBuilder.getBaseUri());
    }

    // Tests stack manipulation operations (push, pop, onStack, getFromStack, removeFromStack)
    @Test
    public void testStackOperations_manipulatingStack_returnsCorrectElements() {
        treeBuilder.initialiseParse(new StringReader(""), "", parser);
        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        Element div = new Element(Tag.valueOf("div"), "");

        treeBuilder.push(html);
        treeBuilder.push(body);
        treeBuilder.push(div);

        assertEquals(3, treeBuilder.getStack().size());
        assertTrue(treeBuilder.onStack(body));
        assertEquals(div, treeBuilder.currentElement());

        Element popped = treeBuilder.pop();
        assertEquals(div, popped);
        assertFalse(treeBuilder.onStack(div));

        assertEquals(body, treeBuilder.getFromStack("body"));
        assertNull(treeBuilder.getFromStack("div"));

        Element above = treeBuilder.aboveOnStack(body);
        assertEquals(html, above);

        assertTrue(treeBuilder.removeFromStack(body));
        assertFalse(treeBuilder.removeFromStack(body));
        assertEquals(1, treeBuilder.getStack().size());
    }

    // Tests popStackToClose with single element name
    @Test
    public void testPopStackToClose_elementInStack_popsElementsUpToAndIncludingTarget() {
        treeBuilder.initialiseParse(new StringReader(""), "", parser);
        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        Element div = new Element(Tag.valueOf("div"), "");
        Element span = new Element(Tag.valueOf("span"), "");

        treeBuilder.push(html);
        treeBuilder.push(body);
        treeBuilder.push(div);
        treeBuilder.push(span);

        treeBuilder.popStackToClose("div");
        assertEquals(2, treeBuilder.getStack().size());
        assertEquals(body, treeBuilder.currentElement());
    }

    // Tests popStackToBefore element name
    @Test
    public void testPopStackToBefore_elementInStack_popsElementsAboveTarget() {
        treeBuilder.initialiseParse(new StringReader(""), "", parser);
        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        Element p = new Element(Tag.valueOf("p"), "");
        Element span = new Element(Tag.valueOf("span"), "");

        treeBuilder.push(html);
        treeBuilder.push(body);
        treeBuilder.push(p);
        treeBuilder.push(span);

        treeBuilder.popStackToBefore("p");
        assertEquals(3, treeBuilder.getStack().size());
        assertEquals(p, treeBuilder.currentElement());
    }

    // Tests insert start tag, empty tag, form, and comment
    @Test
    public void testInsertNodes_differentTokenTypes_insertsCorrectly() {
        treeBuilder.initialiseParse(new StringReader(""), "", parser);
        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        treeBuilder.push(html);
        treeBuilder.push(body);

        Token.StartTag startTag = new Token.StartTag();
        startTag.nameAttr("div", new Attributes());
        Element div = treeBuilder.insert(startTag);
        assertEquals("div", div.tagName());
        assertEquals(div, treeBuilder.currentElement());

        Token.Comment commentToken = new Token.Comment();
        commentToken.getData().append("test comment");
        treeBuilder.insert(commentToken);
        assertEquals(1, div.childNodeSize());
        assertTrue(div.childNode(0) instanceof Comment);

        Token.Character charToken = new Token.Character();
        charToken.data("Hello text");
        treeBuilder.insert(charToken);
        assertEquals(2, div.childNodeSize());
        assertTrue(div.childNode(1) instanceof TextNode);

        Token.StartTag formTag = new Token.StartTag();
        formTag.nameAttr("form", new Attributes());
        FormElement formEl = treeBuilder.insertForm(formTag, true);
        assertNotNull(formEl);
        assertEquals(formEl, treeBuilder.getFormElement());
        assertEquals(formEl, treeBuilder.currentElement());
    }

    // Tests active formatting elements push, marker, remove, and replace
    @Test
    public void testFormattingElements_manipulation_managesActiveFormattingCorrectly() {
        treeBuilder.initialiseParse(new StringReader(""), "", parser);

        Element b1 = new Element(Tag.valueOf("b"), "");
        Element b2 = new Element(Tag.valueOf("b"), "");
        Element b3 = new Element(Tag.valueOf("b"), "");
        Element b4 = new Element(Tag.valueOf("b"), "");

        treeBuilder.pushActiveFormattingElements(b1);
        treeBuilder.pushActiveFormattingElements(b2);
        treeBuilder.pushActiveFormattingElements(b3);
        // Pushing 4th equivalent element should remove the oldest one (Noah's Ark condition)
        treeBuilder.pushActiveFormattingElements(b4);

        assertTrue(treeBuilder.isInActiveFormattingElements(b4));
        assertFalse(treeBuilder.isInActiveFormattingElements(b1));

        treeBuilder.insertMarkerToFormattingElements();
        assertNull(treeBuilder.lastFormattingElement());

        Element i = new Element(Tag.valueOf("i"), "");
        treeBuilder.pushActiveFormattingElements(i);
        assertEquals(i, treeBuilder.getActiveFormattingElement("i"));

        treeBuilder.clearFormattingElementsToLastMarker();
        assertNull(treeBuilder.getActiveFormattingElement("i"));

        treeBuilder.removeFromActiveFormattingElements(b4);
        assertFalse(treeBuilder.isInActiveFormattingElements(b4));
    }

    // Tests scope checking (inScope, inListItemScope, inButtonScope, inTableScope, inSelectScope)
    @Test
    public void testInScope_differentScopes_returnsExpectedBoolean() {
        treeBuilder.initialiseParse(new StringReader(""), "", parser);
        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        Element table = new Element(Tag.valueOf("table"), "");
        Element tr = new Element(Tag.valueOf("tr"), "");
        Element td = new Element(Tag.valueOf("td"), "");
        Element p = new Element(Tag.valueOf("p"), "");

        treeBuilder.push(html);
        treeBuilder.push(body);
        treeBuilder.push(p);

        assertTrue(treeBuilder.inScope("p"));
        assertFalse(treeBuilder.inTableScope("p"));

        treeBuilder.push(table);
        treeBuilder.push(tr);
        treeBuilder.push(td);

        assertTrue(treeBuilder.inTableScope("table"));
        assertTrue(treeBuilder.inScope("td"));
    }

    // Tests generateImpliedEndTags with and without exclusion
    @Test
    public void testGenerateImpliedEndTags_withImpliedTags_popsAppropriateElements() {
        treeBuilder.initialiseParse(new StringReader(""), "", parser);
        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        Element p = new Element(Tag.valueOf("p"), "");
        Element li = new Element(Tag.valueOf("li"), "");

        treeBuilder.push(html);
        treeBuilder.push(body);
        treeBuilder.push(p);
        treeBuilder.push(li);

        treeBuilder.generateImpliedEndTags("li");
        assertEquals(li, treeBuilder.currentElement());

        treeBuilder.generateImpliedEndTags();
        assertEquals(body, treeBuilder.currentElement());
    }

    // Tests isSpecial element classification
    @Test
    public void testIsSpecial_specialAndNonSpecialTags_classifiesCorrectly() {
        Element p = new Element(Tag.valueOf("p"), "");
        Element div = new Element(Tag.valueOf("div"), "");
        Element span = new Element(Tag.valueOf("span"), "");
        Element custom = new Element(Tag.valueOf("customtag"), "");

        assertTrue(treeBuilder.isSpecial(p));
        assertTrue(treeBuilder.isSpecial(div));
        assertFalse(treeBuilder.isSpecial(span));
        assertFalse(treeBuilder.isSpecial(custom));
    }

    // Tests insertInFosterParent when table is in stack
    @Test
    public void testInsertInFosterParent_tableInDoc_fostersBeforeTable() {
        treeBuilder.initialiseParse(new StringReader(""), "", parser);
        Document doc = treeBuilder.getDocument();
        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        Element table = new Element(Tag.valueOf("table"), "");

        doc.appendChild(html);
        html.appendChild(body);
        body.appendChild(table);

        treeBuilder.push(html);
        treeBuilder.push(body);
        treeBuilder.push(table);

        TextNode textNode = new TextNode("Fostered text");
        treeBuilder.insertInFosterParent(textNode);

        assertEquals(2, body.childNodeSize());
        assertEquals(textNode, body.childNode(0));
        assertEquals(table, body.childNode(1));
    }

    // Tests resetInsertionMode with various stack contexts
    @Test
    public void testResetInsertionMode_selectContext_transitionsToInSelect() {
        treeBuilder.initialiseParse(new StringReader(""), "", parser);
        Element html = new Element(Tag.valueOf("html"), "");
        Element select = new Element(Tag.valueOf("select"), "");

        treeBuilder.push(html);
        treeBuilder.push(select);
        treeBuilder.resetInsertionMode();

        assertEquals(HtmlTreeBuilderState.InSelect, treeBuilder.state());
    }

    // Tests duplicate attributes normalization and parsing
    @Test
    public void testDuplicateAttributes_parsingHtml_normalizesDuplicateAttributes() {
        String html = "<div class='first' class='second' ID='main' id='secondary'>Content</div>";
        Document doc = Jsoup.parse(html);
        Element div = doc.selectFirst("div");
        assertNotNull(div);
        assertEquals("first", div.attr("class"));
        assertEquals("main", div.attr("id"));
    }

    // Tests toString produces informative representation
    @Test
    public void testToString_initializedTreeBuilder_containsStateInfo() {
        treeBuilder.initialiseParse(new StringReader(""), "", parser);
        String str = treeBuilder.toString();
        assertTrue(str.contains("TreeBuilder"));
        assertTrue(str.contains("state="));
    }
}