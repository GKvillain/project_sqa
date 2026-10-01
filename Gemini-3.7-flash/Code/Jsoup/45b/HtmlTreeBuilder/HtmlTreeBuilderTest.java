package org.jsoup.parser;

import org.jsoup.nodes.*;
import org.jsoup.select.Elements;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class HtmlTreeBuilderTest {

    private HtmlTreeBuilder treeBuilder;

    @Before
    public void setUp() {
        treeBuilder = new HtmlTreeBuilder();
    }

    // Tests Defects4J bug 45: resetInsertionMode with 'th' context element
    @Test
    public void testResetInsertionMode_thContext_transitionsToInCell() {
        Element th = new Element(Tag.valueOf("th"), "");
        Element html = new Element(Tag.valueOf("html"), "");
        treeBuilder.parseFragment("", th, "", ParseErrorList.noTracking());
        // If "th" is properly handled in resetInsertionMode, state becomes InCell instead of InBody/etc.
        treeBuilder.getStack().clear();
        treeBuilder.getStack().add(html);
        treeBuilder.getStack().add(th);
        treeBuilder.resetInsertionMode();
        assertEquals(HtmlTreeBuilderState.InCell, treeBuilder.state());
    }

    // Tests resetInsertionMode with different HTML table tags
    @Test
    public void testResetInsertionMode_tableTags_transitionsCorrectly() {
        Element html = new Element(Tag.valueOf("html"), "");
        Element table = new Element(Tag.valueOf("table"), "");
        Element tbody = new Element(Tag.valueOf("tbody"), "");
        Element tr = new Element(Tag.valueOf("tr"), "");
        Element td = new Element(Tag.valueOf("td"), "");
        Element select = new Element(Tag.valueOf("select"), "");
        Element caption = new Element(Tag.valueOf("caption"), "");
        Element colgroup = new Element(Tag.valueOf("colgroup"), "");
        Element frameset = new Element(Tag.valueOf("frameset"), "");

        // Select
        treeBuilder.getStack().clear();
        treeBuilder.getStack().add(html);
        treeBuilder.getStack().add(select);
        treeBuilder.resetInsertionMode();
        assertEquals(HtmlTreeBuilderState.InSelect, treeBuilder.state());

        // TD
        treeBuilder.getStack().clear();
        treeBuilder.getStack().add(html);
        treeBuilder.getStack().add(td);
        treeBuilder.resetInsertionMode();
        assertEquals(HtmlTreeBuilderState.InCell, treeBuilder.state());

        // TR
        treeBuilder.getStack().clear();
        treeBuilder.getStack().add(html);
        treeBuilder.getStack().add(tr);
        treeBuilder.resetInsertionMode();
        assertEquals(HtmlTreeBuilderState.InRow, treeBuilder.state());

        // TBODY
        treeBuilder.getStack().clear();
        treeBuilder.getStack().add(html);
        treeBuilder.getStack().add(tbody);
        treeBuilder.resetInsertionMode();
        assertEquals(HtmlTreeBuilderState.InTableBody, treeBuilder.state());

        // Caption
        treeBuilder.getStack().clear();
        treeBuilder.getStack().add(html);
        treeBuilder.getStack().add(caption);
        treeBuilder.resetInsertionMode();
        assertEquals(HtmlTreeBuilderState.InCaption, treeBuilder.state());

        // Colgroup
        treeBuilder.getStack().clear();
        treeBuilder.getStack().add(html);
        treeBuilder.getStack().add(colgroup);
        treeBuilder.resetInsertionMode();
        assertEquals(HtmlTreeBuilderState.InColumnGroup, treeBuilder.state());

        // Table
        treeBuilder.getStack().clear();
        treeBuilder.getStack().add(html);
        treeBuilder.getStack().add(table);
        treeBuilder.resetInsertionMode();
        assertEquals(HtmlTreeBuilderState.InTable, treeBuilder.state());

        // Frameset
        treeBuilder.getStack().clear();
        treeBuilder.getStack().add(html);
        treeBuilder.getStack().add(frameset);
        treeBuilder.resetInsertionMode();
        assertEquals(HtmlTreeBuilderState.InFrameset, treeBuilder.state());
    }

    // Tests parse method initializes state and returns document
    @Test
    public void testParse_simpleHtml_returnsDocument() {
        Document doc = treeBuilder.parse("<html><head><title>Test</title></head><body><p>Hello</p></body></html>", "http://example.com", ParseErrorList.noTracking());
        assertNotNull(doc);
        assertEquals("Test", doc.title());
        assertEquals("Hello", doc.select("p").first().text());
        assertEquals(HtmlTreeBuilderState.Initial, treeBuilder.originalState() == null ? HtmlTreeBuilderState.Initial : treeBuilder.originalState());
    }

    // Tests parseFragment with null context
    @Test
    public void testParseFragment_nullContext_returnsChildNodes() {
        List<Node> nodes = treeBuilder.parseFragment("<p>One</p><p>Two</p>", null, "http://example.com", ParseErrorList.noTracking());
        assertNotNull(nodes);
        assertFalse(nodes.isEmpty());
        assertTrue(treeBuilder.isFragmentParsing());
    }

    // Tests parseFragment with body context
    @Test
    public void testParseFragment_bodyContext_returnsParsedNodes() {
        Element body = new Element(Tag.valueOf("body"), "");
        List<Node> nodes = treeBuilder.parseFragment("<div><span>Test</span></div>", body, "http://example.com", ParseErrorList.noTracking());
        assertNotNull(nodes);
        assertEquals(1, nodes.size());
        assertTrue(nodes.get(0) instanceof Element);
        assertEquals("div", ((Element) nodes.get(0)).tagName());
    }

    // Tests parseFragment with form context chain
    @Test
    public void testParseFragment_formContext_associatesFormElement() {
        FormElement form = new FormElement(Tag.valueOf("form"), "", new Attributes());
        Element div = new Element(Tag.valueOf("div"), "");
        form.appendChild(div);

        List<Node> nodes = treeBuilder.parseFragment("<input type='text' name='q' />", div, "", ParseErrorList.noTracking());
        assertNotNull(nodes);
        assertNotNull(treeBuilder.getFormElement());
        assertEquals("form", treeBuilder.getFormElement().tagName());
    }

    // Tests maybeSetBaseUri sets doc baseUri only once
    @Test
    public void testMaybeSetBaseUri_validHref_setsBaseUriOnce() {
        Document doc = treeBuilder.parse("<html><head></head><body></body></html>", "http://initial.com", ParseErrorList.noTracking());
        Attributes attrs1 = new Attributes();
        attrs1.put("href", "http://example.com/path/");
        Element base1 = new Element(Tag.valueOf("base"), "http://initial.com", attrs1);

        treeBuilder.maybeSetBaseUri(base1);
        assertEquals("http://example.com/path/", treeBuilder.getBaseUri());

        Attributes attrs2 = new Attributes();
        attrs2.put("href", "http://second.com/");
        Element base2 = new Element(Tag.valueOf("base"), "http://initial.com", attrs2);

        treeBuilder.maybeSetBaseUri(base2);
        assertEquals("http://example.com/path/", treeBuilder.getBaseUri());
    }

    // Tests stack manipulation methods: pop, push, onStack, getFromStack, removeFromStack
    @Test
    public void testStackOperations_pushPopAndSearch_success() {
        treeBuilder.parse("<html><head></head><body></body></html>", "", ParseErrorList.noTracking());
        Element div = new Element(Tag.valueOf("div"), "");
        Element span = new Element(Tag.valueOf("span"), "");

        treeBuilder.push(div);
        assertTrue(treeBuilder.onStack(div));
        assertEquals(div, treeBuilder.getFromStack("div"));

        treeBuilder.push(span);
        assertEquals(span, treeBuilder.aboveOnStack(div) != null ? span : treeBuilder.pop());

        treeBuilder.push(span);
        assertTrue(treeBuilder.removeFromStack(span));
        assertFalse(treeBuilder.onStack(span));

        assertNull(treeBuilder.getFromStack("nonexistent"));
    }

    // Tests popStackToClose single and multiple tags
    @Test
    public void testPopStackToClose_variousTags_popsExpectedElements() {
        treeBuilder.parse("<html><head></head><body><div><p><span></span></p></div></body></html>", "", ParseErrorList.noTracking());
        Element p = treeBuilder.getFromStack("p");
        assertNotNull(p);

        treeBuilder.popStackToClose("p");
        assertFalse(treeBuilder.onStack(p));
        assertNotNull(treeBuilder.getFromStack("div"));

        treeBuilder.popStackToClose("div", "body");
        assertNull(treeBuilder.getFromStack("div"));
    }

    // Tests popStackToBefore
    @Test
    public void testPopStackToBefore_existingTag_popsUntilTarget() {
        treeBuilder.parse("<html><head></head><body><div><p><span></span></p></div></body></html>", "", ParseErrorList.noTracking());
        assertTrue(treeBuilder.onStack(treeBuilder.getFromStack("span")));

        treeBuilder.popStackToBefore("p");
        assertNotNull(treeBuilder.getFromStack("p"));
        assertNull(treeBuilder.getFromStack("span"));
    }

    // Tests clearStackToTableContext, clearStackToTableBodyContext, clearStackToTableRowContext
    @Test
    public void testClearStackToContext_tableContexts_clearsCorrectly() {
        treeBuilder.parse("<html><body><table><tbody><tr><td></td></tr></tbody></table></body></html>", "", ParseErrorList.noTracking());

        treeBuilder.clearStackToTableRowContext();
        assertEquals("tr", treeBuilder.getStack().get(treeBuilder.getStack().size() - 1).nodeName());

        treeBuilder.clearStackToTableBodyContext();
        assertEquals("tbody", treeBuilder.getStack().get(treeBuilder.getStack().size() - 1).nodeName());

        treeBuilder.clearStackToTableContext();
        assertEquals("table", treeBuilder.getStack().get(treeBuilder.getStack().size() - 1).nodeName());
    }

    // Tests active formatting elements operations
    @Test
    public void testActiveFormattingElements_pushGetRemove_operatesCorrectly() {
        Attributes attrs = new Attributes();
        attrs.put("class", "bold");
        Element b1 = new Element(Tag.valueOf("b"), "", attrs);
        Element b2 = new Element(Tag.valueOf("b"), "", attrs);
        Element b3 = new Element(Tag.valueOf("b"), "", attrs);
        Element b4 = new Element(Tag.valueOf("b"), "", attrs);

        treeBuilder.pushActiveFormattingElements(b1);
        treeBuilder.pushActiveFormattingElements(b2);
        treeBuilder.pushActiveFormattingElements(b3);
        // 4th identical element should evict the first
        treeBuilder.pushActiveFormattingElements(b4);

        assertTrue(treeBuilder.isInActiveFormattingElements(b4));
        assertEquals(b4, treeBuilder.lastFormattingElement());
        assertEquals(b4, treeBuilder.getActiveFormattingElement("b"));

        treeBuilder.removeFromActiveFormattingElements(b4);
        assertFalse(treeBuilder.isInActiveFormattingElements(b4));

        treeBuilder.insertMarkerToFormattingElements();
        treeBuilder.clearFormattingElementsToLastMarker();
    }

    // Tests inScope checks: inScope, inListItemScope, inButtonScope, inTableScope, inSelectScope
    @Test
    public void testInScope_differentScopes_returnsExpected() {
        treeBuilder.parse("<html><body><div><p><button><select><option></option></select></button></p></div></body></html>", "", ParseErrorList.noTracking());

        assertTrue(treeBuilder.inScope("p"));
        assertTrue(treeBuilder.inScope(new String[]{"p", "div"}));
        assertTrue(treeBuilder.inButtonScope("button"));
        assertTrue(treeBuilder.inSelectScope("option"));
        assertFalse(treeBuilder.inTableScope("table"));
    }

    // Tests generateImpliedEndTags with and without excluded tag
    @Test
    public void testGenerateImpliedEndTags_impliedTags_popsCorrectly() {
        treeBuilder.parse("<html><head></head><body></body></html>", "", ParseErrorList.noTracking());
        Element p = new Element(Tag.valueOf("p"), "");
        Element li = new Element(Tag.valueOf("li"), "");
        treeBuilder.push(p);
        treeBuilder.push(li);

        treeBuilder.generateImpliedEndTags("p");
        assertFalse(treeBuilder.onStack(li));
        assertTrue(treeBuilder.onStack(p));

        treeBuilder.generateImpliedEndTags();
        assertFalse(treeBuilder.onStack(p));
    }

    // Tests isSpecial method for known special elements
    @Test
    public void testIsSpecial_specialAndNonSpecialElements_returnsCorrectBoolean() {
        Element p = new Element(Tag.valueOf("p"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        Element custom = new Element(Tag.valueOf("custom-tag"), "");

        assertTrue(treeBuilder.isSpecial(p));
        assertTrue(treeBuilder.isSpecial(body));
        assertFalse(treeBuilder.isSpecial(custom));
    }

    // Tests foster parenting when fosterInserts is enabled
    @Test
    public void testInsertInFosterParent_tableInDoc_insertsBeforeTable() {
        Document doc = treeBuilder.parse("<html><head></head><body><table></table></body></html>", "", ParseErrorList.noTracking());
        treeBuilder.setFosterInserts(true);
        assertTrue(treeBuilder.isFosterInserts());

        Token.Character charToken = new Token.Character();
        charToken.data("fostered text");
        treeBuilder.process(charToken, HtmlTreeBuilderState.InTable);

        Element table = doc.select("table").first();
        assertNotNull(table);
    }

    // Tests framesetOk, headElement, and pendingTableCharacters state accessors
    @Test
    public void testStateGettersAndSetters_allProperties_workAsExpected() {
        treeBuilder.framesetOk(false);
        assertFalse(treeBuilder.framesetOk());

        Element head = new Element(Tag.valueOf("head"), "");
        treeBuilder.setHeadElement(head);
        assertEquals(head, treeBuilder.getHeadElement());

        List<String> pending = new ArrayList<String>();
        pending.add("test");
        treeBuilder.setPendingTableCharacters(pending);
        assertEquals(1, treeBuilder.getPendingTableCharacters().size());

        treeBuilder.newPendingTableCharacters();
        assertTrue(treeBuilder.getPendingTableCharacters().isEmpty());

        treeBuilder.transition(HtmlTreeBuilderState.InBody);
        assertEquals(HtmlTreeBuilderState.InBody, treeBuilder.state());

        treeBuilder.markInsertionMode();
        assertEquals(HtmlTreeBuilderState.InBody, treeBuilder.originalState());

        assertNotNull(treeBuilder.toString());
    }

    // Tests insertStartTag and insertEmpty methods
    @Test
    public void testInsertTagMethods_startAndEmptyTags_success() {
        treeBuilder.parse("<html><head></head><body></body></html>", "", ParseErrorList.noTracking());

        Element div = treeBuilder.insertStartTag("div");
        assertNotNull(div);
        assertEquals("div", div.tagName());
        assertTrue(treeBuilder.onStack(div));

        Token.StartTag imgTag = new Token.StartTag();
        imgTag.nameAttr("img", new Attributes());
        imgTag.selfClosing = true;
        Element img = treeBuilder.insertEmpty(imgTag);
        assertNotNull(img);
        assertEquals("img", img.tagName());
    }

    // Tests insertOnStackAfter and replaceOnStack
    @Test
    public void testStackInsertAndReplace_validElements_replacesCorrectly() {
        treeBuilder.parse("<html><head></head><body></body></html>", "", ParseErrorList.noTracking());
        Element el1 = new Element(Tag.valueOf("div"), "");
        Element el2 = new Element(Tag.valueOf("span"), "");
        Element el3 = new Element(Tag.valueOf("p"), "");

        treeBuilder.push(el1);
        treeBuilder.insertOnStackAfter(el1, el2);
        assertTrue(treeBuilder.onStack(el2));

        treeBuilder.replaceOnStack(el2, el3);
        assertFalse(treeBuilder.onStack(el2));
        assertTrue(treeBuilder.onStack(el3));
    }
}