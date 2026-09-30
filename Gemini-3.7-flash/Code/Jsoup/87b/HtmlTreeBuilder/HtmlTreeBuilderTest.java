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
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class HtmlTreeBuilderTest {

    private HtmlTreeBuilder builder;
    private Parser parser;

    @Before
    public void setUp() {
        builder = new HtmlTreeBuilder();
        parser = new Parser(builder);
    }

    // Tests stack push, pop, and stack query operations
    @Test
    public void testStackOperations_pushAndPop_maintainsCorrectStack() {
        builder.initialiseParse(new StringReader("<div></div>"), "http://example.com", parser);
        Element html = new Element(Tag.valueOf("html"), "http://example.com");
        Element body = new Element(Tag.valueOf("body"), "http://example.com");
        Element div = new Element(Tag.valueOf("div"), "http://example.com");

        builder.push(html);
        builder.push(body);
        builder.push(div);

        assertTrue(builder.onStack(div));
        assertTrue(builder.onStack(body));
        assertEquals(div, builder.currentElement());
        assertEquals(body, builder.aboveOnStack(div));
        assertEquals(div, builder.getFromStack("div"));

        Element popped = builder.pop();
        assertEquals(div, popped);
        assertFalse(builder.onStack(div));
        assertEquals(body, builder.currentElement());

        boolean removed = builder.removeFromStack(body);
        assertTrue(removed);
        assertFalse(builder.onStack(body));
    }

    // Tests popStackToClose with single element name
    @Test
    public void testPopStackToClose_matchingElement_popsCorrectly() {
        builder.initialiseParse(new StringReader(""), "http://example.com", parser);
        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        Element p = new Element(Tag.valueOf("p"), "");
        Element span = new Element(Tag.valueOf("span"), "");

        builder.push(html);
        builder.push(body);
        builder.push(p);
        builder.push(span);

        builder.popStackToClose("p");
        assertFalse(builder.onStack(span));
        assertFalse(builder.onStack(p));
        assertTrue(builder.onStack(body));
        assertEquals(body, builder.currentElement());
    }

    // Tests popStackToBefore element
    @Test
    public void testPopStackToBefore_existingElement_popsUntilTarget() {
        builder.initialiseParse(new StringReader(""), "http://example.com", parser);
        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        Element div = new Element(Tag.valueOf("div"), "");
        Element span = new Element(Tag.valueOf("span"), "");

        builder.push(html);
        builder.push(body);
        builder.push(div);
        builder.push(span);

        builder.popStackToBefore("div");
        assertFalse(builder.onStack(span));
        assertTrue(builder.onStack(div));
        assertEquals(div, builder.currentElement());
    }

    // Tests clearStackToTableContext and variants
    @Test
    public void testClearStackToTableContext_nestedElements_clearsToTable() {
        builder.initialiseParse(new StringReader(""), "http://example.com", parser);
        Element html = new Element(Tag.valueOf("html"), "");
        Element table = new Element(Tag.valueOf("table"), "");
        Element div = new Element(Tag.valueOf("div"), "");

        builder.push(html);
        builder.push(table);
        builder.push(div);

        builder.clearStackToTableContext();
        assertEquals(table, builder.currentElement());
    }

    // Tests clearStackToTableRowContext
    @Test
    public void testClearStackToTableRowContext_nestedInRow_clearsToRow() {
        builder.initialiseParse(new StringReader(""), "http://example.com", parser);
        Element html = new Element(Tag.valueOf("html"), "");
        Element tr = new Element(Tag.valueOf("tr"), "");
        Element span = new Element(Tag.valueOf("span"), "");

        builder.push(html);
        builder.push(tr);
        builder.push(span);

        builder.clearStackToTableRowContext();
        assertEquals(tr, builder.currentElement());
    }

    // Tests insertOnStackAfter and replaceOnStack
    @Test
    public void testInsertOnStackAfterAndReplace_validElements_modifiesStack() {
        builder.initialiseParse(new StringReader(""), "http://example.com", parser);
        Element html = new Element(Tag.valueOf("html"), "");
        Element div = new Element(Tag.valueOf("div"), "");
        Element span = new Element(Tag.valueOf("span"), "");
        Element p = new Element(Tag.valueOf("p"), "");

        builder.push(html);
        builder.push(div);

        builder.insertOnStackAfter(div, span);
        assertEquals(span, builder.currentElement());

        builder.replaceOnStack(span, p);
        assertEquals(p, builder.currentElement());
        assertFalse(builder.onStack(span));
    }

    // Tests inScope resolution
    @Test
    public void testInScope_elementsInAndOutOfScope_returnsExpected() {
        builder.initialiseParse(new StringReader(""), "http://example.com", parser);
        Element html = new Element(Tag.valueOf("html"), "");
        Element table = new Element(Tag.valueOf("table"), "");
        Element td = new Element(Tag.valueOf("td"), "");
        Element p = new Element(Tag.valueOf("p"), "");

        builder.push(html);
        builder.push(table);
        builder.push(td);
        builder.push(p);

        assertTrue(builder.inScope("p"));
        assertTrue(builder.inScope("td"));
        assertTrue(builder.inTableScope("table"));
        assertFalse(builder.inTableScope("p"));
        assertTrue(builder.inListItemScope("p"));
        assertTrue(builder.inButtonScope("p"));
    }

    // Tests inSelectScope
    @Test
    public void testInSelectScope_validSelectStructure_checksCorrectScope() {
        builder.initialiseParse(new StringReader(""), "http://example.com", parser);
        Element html = new Element(Tag.valueOf("html"), "");
        Element select = new Element(Tag.valueOf("select"), "");
        Element option = new Element(Tag.valueOf("option"), "");

        builder.push(html);
        builder.push(select);
        builder.push(option);

        assertTrue(builder.inSelectScope("option"));
        assertFalse(builder.inSelectScope("input"));
    }

    // Tests active formatting elements push, limit of 3 duplicates, and removal
    @Test
    public void testPushActiveFormattingElements_duplicateThreshold_dropsEarliest() {
        builder.initialiseParse(new StringReader(""), "http://example.com", parser);

        Element b1 = new Element(Tag.valueOf("b"), "");
        Element b2 = new Element(Tag.valueOf("b"), "");
        Element b3 = new Element(Tag.valueOf("b"), "");
        Element b4 = new Element(Tag.valueOf("b"), "");

        builder.pushActiveFormattingElements(b1);
        builder.pushActiveFormattingElements(b2);
        builder.pushActiveFormattingElements(b3);
        assertTrue(builder.isInActiveFormattingElements(b1));

        builder.pushActiveFormattingElements(b4);
        assertFalse(builder.isInActiveFormattingElements(b1));
        assertTrue(builder.isInActiveFormattingElements(b2));
        assertTrue(builder.isInActiveFormattingElements(b3));
        assertTrue(builder.isInActiveFormattingElements(b4));

        assertEquals(b4, builder.lastFormattingElement());
        assertEquals(b4, builder.removeLastFormattingElement());
        assertEquals(b3, builder.lastFormattingElement());

        builder.removeFromActiveFormattingElements(b2);
        assertFalse(builder.isInActiveFormattingElements(b2));
    }

    // Tests active formatting element marker insertion and clearing to marker
    @Test
    public void testClearFormattingElementsToLastMarker_withMarker_clearsUntilMarker() {
        builder.initialiseParse(new StringReader(""), "http://example.com", parser);

        Element a = new Element(Tag.valueOf("a"), "");
        Element b = new Element(Tag.valueOf("b"), "");

        builder.pushActiveFormattingElements(a);
        builder.insertMarkerToFormattingElements();
        builder.pushActiveFormattingElements(b);

        assertEquals(b, builder.getActiveFormattingElement("b"));
        builder.clearFormattingElementsToLastMarker();

        assertNull(builder.getActiveFormattingElement("b"));
        assertEquals(a, builder.getActiveFormattingElement("a"));
    }

    // Tests replaceActiveFormattingElement
    @Test
    public void testReplaceActiveFormattingElement_validElement_replacesCorrectly() {
        builder.initialiseParse(new StringReader(""), "http://example.com", parser);

        Element i1 = new Element(Tag.valueOf("i"), "");
        Element i2 = new Element(Tag.valueOf("i"), "");

        builder.pushActiveFormattingElements(i1);
        builder.replaceActiveFormattingElement(i1, i2);

        assertFalse(builder.isInActiveFormattingElements(i1));
        assertTrue(builder.isInActiveFormattingElements(i2));
    }

    // Tests generateImpliedEndTags
    @Test
    public void testGenerateImpliedEndTags_withImpliedTags_popsUntilNonImplied() {
        builder.initialiseParse(new StringReader(""), "http://example.com", parser);
        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        Element p = new Element(Tag.valueOf("p"), "");

        builder.push(html);
        builder.push(body);
        builder.push(p);

        builder.generateImpliedEndTags();
        assertEquals(body, builder.currentElement());

        builder.push(p);
        builder.generateImpliedEndTags("p");
        assertEquals(p, builder.currentElement());
    }

    // Tests isSpecial method for standard HTML special tags
    @Test
    public void testIsSpecial_specialAndNonSpecialTags_returnsExpected() {
        Element p = new Element(Tag.valueOf("p"), "");
        Element div = new Element(Tag.valueOf("div"), "");
        Element span = new Element(Tag.valueOf("span"), "");
        Element custom = new Element(Tag.valueOf("custom-tag"), "");

        assertTrue(builder.isSpecial(p));
        assertTrue(builder.isSpecial(div));
        assertFalse(builder.isSpecial(span));
        assertFalse(builder.isSpecial(custom));
    }

    // Tests maybeSetBaseUri when href attribute is present
    @Test
    public void testMaybeSetBaseUri_validBaseElement_updatesBaseUri() {
        builder.initialiseParse(new StringReader(""), "http://initial.com", parser);
        Element base = new Element(Tag.valueOf("base"), "http://initial.com");
        base.attr("href", "http://example.com/base/");

        builder.maybeSetBaseUri(base);
        assertEquals("http://example.com/base/", builder.getBaseUri());

        Element secondBase = new Element(Tag.valueOf("base"), "http://initial.com");
        secondBase.attr("href", "http://ignored.com/");
        builder.maybeSetBaseUri(secondBase);
        assertEquals("http://example.com/base/", builder.getBaseUri());
    }

    // Tests parseFragment with a standard context element
    @Test
    public void testParseFragment_withContextElement_parsesChildren() {
        Element context = new Element(Tag.valueOf("div"), "http://example.com");
        List<Node> nodes = builder.parseFragment("<p>Hello</p><span>World</span>", context, "http://example.com", parser);

        assertEquals(2, nodes.size());
        assertEquals("p", nodes.get(0).nodeName());
        assertEquals("span", nodes.get(1).nodeName());
    }

    // Tests parseFragment with null context
    @Test
    public void testParseFragment_nullContext_parsesDocumentNodes() {
        List<Node> nodes = builder.parseFragment("<p>Hello</p>", null, "http://example.com", parser);
        assertNotNull(nodes);
        assertFalse(nodes.isEmpty());
    }

    // Tests foster parenting when inserting in table context
    @Test
    public void testInsertInFosterParent_tableWithParent_insertsBeforeTable() {
        builder.initialiseParse(new StringReader(""), "http://example.com", parser);
        Document doc = builder.getDocument();
        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        Element table = new Element(Tag.valueOf("table"), "");
        doc.appendChild(html);
        html.appendChild(body);
        body.appendChild(table);

        builder.push(html);
        builder.push(body);
        builder.push(table);

        TextNode text = new TextNode("fostered");
        builder.insertInFosterParent(text);

        assertEquals(2, body.childNodeSize());
        assertEquals(text, body.childNode(0));
        assertEquals(table, body.childNode(1));
    }

    // Tests resetInsertionMode transition for table contexts
    @Test
    public void testResetInsertionMode_inSelectContext_transitionsToInSelect() {
        builder.initialiseParse(new StringReader(""), "http://example.com", parser);
        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        Element select = new Element(Tag.valueOf("select"), "");

        builder.push(html);
        builder.push(body);
        builder.push(select);

        builder.resetInsertionMode();
        assertEquals(HtmlTreeBuilderState.InSelect, builder.state());
    }

    // Tests insert FormElement and linking form controls
    @Test
    public void testInsertForm_withFormAndInputs_associatesFormControls() {
        builder.initialiseParse(new StringReader(""), "http://example.com", parser);
        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        builder.push(html);
        builder.push(body);

        Token.StartTag formTag = new Token.StartTag();
        formTag.nameAttr("form", new Attributes());
        FormElement form = builder.insertForm(formTag, true);

        assertEquals(form, builder.getFormElement());
        assertTrue(builder.onStack(form));

        Token.StartTag inputTag = new Token.StartTag();
        inputTag.nameAttr("input", new Attributes());
        Element input = builder.insertEmpty(inputTag);

        assertTrue(form.elements().contains(input));
    }

    // Tests comments and text insertion
    @Test
    public void testInsertCommentAndText_standardTokens_appendsToCurrentElement() {
        builder.initialiseParse(new StringReader(""), "http://example.com", parser);
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
}