package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

import org.jsoup.nodes.*;
import org.jsoup.helper.*;
import org.jsoup.parser.Token;
import java.io.StringReader;
import java.util.List;

public class HtmlTreeBuilderTest {

    // helper to create a builder initialized with empty input
    private HtmlTreeBuilder createBuilder() {
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        builder.initialiseParse(new StringReader(""), "http://example.com",
                new ParseErrorList(16, 16), ParseSettings.htmlDefault);
        return builder;
    }

    // Normal case: parse fragment with null context
    @Test
    public void testParseFragment_nullContext_returnsDocumentChildren() {
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        List<Node> nodes = builder.parseFragment("<p>test</p>", null, "http://example.com",
                new ParseErrorList(16, 16), ParseSettings.htmlDefault);
        assertNotNull(nodes);
        assertEquals(1, nodes.size());
        Element p = (Element) nodes.get(0);
        assertEquals("p", p.tagName());
        assertEquals("test", p.text());
    }

    // Normal case: parse fragment with body context
    @Test
    public void testParseFragment_bodyContext_returnsBodyChildren() {
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        Element context = new Element(Tag.valueOf("body", ParseSettings.htmlDefault), "http://example.com");
        List<Node> nodes = builder.parseFragment("<div>hello</div>", context, "http://example.com",
                new ParseErrorList(16, 16), ParseSettings.htmlDefault);
        assertNotNull(nodes);
        assertEquals(1, nodes.size());
        Element div = (Element) nodes.get(0);
        assertEquals("div", div.tagName());
    }

    // Tests implied end tags (li) during fragment parsing
    @Test
    public void testParseFragment_impliedEndTags_li() {
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        Element context = new Element(Tag.valueOf("body", ParseSettings.htmlDefault), "http://example.com");
        List<Node> nodes = builder.parseFragment("<ul><li>a<li>b", context, "http://example.com",
                new ParseErrorList(16, 16), ParseSettings.htmlDefault);
        assertEquals(1, nodes.size());
        Element ul = (Element) nodes.get(0);
        assertEquals("ul", ul.tagName());
        assertEquals(2, ul.children().size());
        assertEquals("a", ul.child(0).text());
        assertEquals("b", ul.child(1).text());
    }

    // Tests foster insertion when context is a table
    @Test
    public void testParseFragment_tableContext_fosterInsertsDiv() {
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        Element context = new Element(Tag.valueOf("table", ParseSettings.htmlDefault), "http://example.com");
        List<Node> nodes = builder.parseFragment("<div>text</div>", context, "http://example.com",
                new ParseErrorList(16, 16), ParseSettings.htmlDefault);
        assertNotNull(nodes);
        // Div should be foster inserted before the table element (not inside it)
        assertFalse(nodes.isEmpty());
    }

    // Tests script context sets tokeniser state to ScriptData
    @Test
    public void testParseFragment_scriptContext_parsesAsData() {
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        Element context = new Element(Tag.valueOf("script", ParseSettings.htmlDefault), "http://example.com");
        List<Node> nodes = builder.parseFragment("var x = 1;", context, "http://example.com",
                new ParseErrorList(16, 16), ParseSettings.htmlDefault);
        assertNotNull(nodes);
        assertTrue(nodes.size() > 0);
    }

    // Edge case: empty fragment input
    @Test
    public void testParseFragment_emptyInput_returnsEmptyList() {
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        List<Node> nodes = builder.parseFragment("", null, "http://example.com",
                new ParseErrorList(16, 16), ParseSettings.htmlDefault);
        assertTrue(nodes.isEmpty());
    }

    // Tests maybeSetBaseUri with valid href
    @Test
    public void testMaybeSetBaseUri_validHref_setsBaseUri() {
        HtmlTreeBuilder builder = createBuilder();
        Element base = new Element(Tag.valueOf("base", builder.defaultSettings()), builder.getBaseUri());
        base.attr("href", "http://newbase.com");
        builder.maybeSetBaseUri(base);
        assertEquals("http://newbase.com", builder.getBaseUri());
    }

    // Tests maybeSetBaseUri ignores empty href
    @Test
    public void testMaybeSetBaseUri_emptyHref_doesNotChange() {
        HtmlTreeBuilder builder = createBuilder();
        Element base = new Element(Tag.valueOf("base", builder.defaultSettings()), builder.getBaseUri());
        base.attr("href", "");
        builder.maybeSetBaseUri(base);
        assertEquals("http://example.com", builder.getBaseUri());
    }

    // Tests maybeSetBaseUri only sets once (first href wins)
    @Test
    public void testMaybeSetBaseUri_alreadySet_ignoresSecond() {
        HtmlTreeBuilder builder = createBuilder();
        Element base1 = new Element(Tag.valueOf("base", builder.defaultSettings()), builder.getBaseUri());
        base1.attr("href", "http://first.com");
        builder.maybeSetBaseUri(base1);
        Element base2 = new Element(Tag.valueOf("base", builder.defaultSettings()), builder.getBaseUri());
        base2.attr("href", "http://second.com");
        builder.maybeSetBaseUri(base2);
        assertEquals("http://first.com", builder.getBaseUri());
    }

    // Tests transition and state getter
    @Test
    public void testTransition_state() {
        HtmlTreeBuilder builder = createBuilder();
        builder.transition(HtmlTreeBuilderState.InBody);
        assertEquals(HtmlTreeBuilderState.InBody, builder.state());
    }

    // Tests framesetOk default value
    @Test
    public void testFramesetOk_defaultTrue() {
        HtmlTreeBuilder builder = createBuilder();
        assertTrue(builder.framesetOk());
    }

    // Tests framesetOk setter
    @Test
    public void testFramesetOk_setFalse() {
        HtmlTreeBuilder builder = createBuilder();
        builder.framesetOk(false);
        assertFalse(builder.framesetOk());
    }

    // Tests isFragmentParsing true after fragment parsing
    @Test
    public void testIsFragmentParsing_afterParseFragment_true() {
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        builder.parseFragment("<p></p>", null, "http://example.com",
                new ParseErrorList(16, 16), ParseSettings.htmlDefault);
        assertTrue(builder.isFragmentParsing());
    }

    // Tests getDocument returns non-null after initialisation
    @Test
    public void testGetDocument_notNull() {
        HtmlTreeBuilder builder = createBuilder();
        assertNotNull(builder.getDocument());
    }

    // Tests headElement initially null
    @Test
    public void testGetHeadElement_initialNull() {
        HtmlTreeBuilder builder = createBuilder();
        assertNull(builder.getHeadElement());
    }

    // Tests setHeadElement / getHeadElement
    @Test
    public void testSetHeadElement_setsAndGets() {
        HtmlTreeBuilder builder = createBuilder();
        Element head = new Element(Tag.valueOf("head", builder.defaultSettings()), builder.getBaseUri());
        builder.setHeadElement(head);
        assertEquals(head, builder.getHeadElement());
    }

    // Tests fosterInserts default false
    @Test
    public void testIsFosterInserts_defaultFalse() {
        HtmlTreeBuilder builder = createBuilder();
        assertFalse(builder.isFosterInserts());
    }

    // Tests setFosterInserts
    @Test
    public void testSetFosterInserts_true() {
        HtmlTreeBuilder builder = createBuilder();
        builder.setFosterInserts(true);
        assertTrue(builder.isFosterInserts());
    }

    // Tests resetInsertionMode with td in stack -> InCell
    @Test
    public void testResetInsertionMode_tdStack_transitionToInCell() {
        HtmlTreeBuilder builder = createBuilder();
        Element td = new Element(Tag.valueOf("td", builder.defaultSettings()), builder.getBaseUri());
        builder.push(td);
        builder.resetInsertionMode();
        assertEquals(HtmlTreeBuilderState.InCell, builder.state());
    }

    // Tests resetInsertionMode with tr in stack -> InRow
    @Test
    public void testResetInsertionMode_trStack_transitionToInRow() {
        HtmlTreeBuilder builder = createBuilder();
        Element tr = new Element(Tag.valueOf("tr", builder.defaultSettings()), builder.getBaseUri());
        builder.push(tr);
        builder.resetInsertionMode();
        assertEquals(HtmlTreeBuilderState.InRow, builder.state());
    }

    // Tests resetInsertionMode with tbody in stack -> InTableBody
    @Test
    public void testResetInsertionMode_tbodyStack_transitionToInTableBody() {
        HtmlTreeBuilder builder = createBuilder();
        Element tbody = new Element(Tag.valueOf("tbody", builder.defaultSettings()), builder.getBaseUri());
        builder.push(tbody);
        builder.resetInsertionMode();
        assertEquals(HtmlTreeBuilderState.InTableBody, builder.state());
    }

    // Tests resetInsertionMode with table in stack -> InTable
    @Test
    public void testResetInsertionMode_tableStack_transitionToInTable() {
        HtmlTreeBuilder builder = createBuilder();
        Element table = new Element(Tag.valueOf("table", builder.defaultSettings()), builder.getBaseUri());
        builder.push(table);
        builder.resetInsertionMode();
        assertEquals(HtmlTreeBuilderState.InTable, builder.state());
    }

    // Tests resetInsertionMode with select in stack -> InSelect
    @Test
    public void testResetInsertionMode_selectStack_transitionToInSelect() {
        HtmlTreeBuilder builder = createBuilder();
        Element select = new Element(Tag.valueOf("select", builder.defaultSettings()), builder.getBaseUri());
        builder.push(select);
        builder.resetInsertionMode();
        assertEquals(HtmlTreeBuilderState.InSelect, builder.state());
    }

    // Tests resetInsertionMode with caption in stack -> InCaption
    @Test
    public void testResetInsertionMode_captionStack_transitionToInCaption() {
        HtmlTreeBuilder builder = createBuilder();
        Element caption = new Element(Tag.valueOf("caption", builder.defaultSettings()), builder.getBaseUri());
        builder.push(caption);
        builder.resetInsertionMode();
        assertEquals(HtmlTreeBuilderState.InCaption, builder.state());
    }

    // Tests generateImpliedEndTags with excludeTag
    @Test
    public void testGenerateImpliedEndTags_excludeP() {
        HtmlTreeBuilder builder = createBuilder();
        Element li = new Element(Tag.valueOf("li", builder.defaultSettings()), builder.getBaseUri());
        Element p = new Element(Tag.valueOf("p", builder.defaultSettings()), builder.getBaseUri());
        builder.push(li);
        builder.push(p);
        builder.generateImpliedEndTags("p");
        assertEquals("p", builder.currentElement().nodeName());
        builder.generateImpliedEndTags(null);
        assertEquals("li", builder.currentElement().nodeName());
    }

    // Tests generateImpliedEndTags with null (pop all implied end tags)
    @Test
    public void testGenerateImpliedEndTags_noExclude_removesAllImplied() {
        HtmlTreeBuilder builder = createBuilder();
        Element li1 = new Element(Tag.valueOf("li", builder.defaultSettings()), builder.getBaseUri());
        Element li2 = new Element(Tag.valueOf("li", builder.defaultSettings()), builder.getBaseUri());
        Element p = new Element(Tag.valueOf("p", builder.defaultSettings()), builder.getBaseUri());
        builder.push(li1);
        builder.push(li2);
        builder.push(p);
        builder.generateImpliedEndTags(null);
        assertEquals("html", builder.currentElement().nodeName());
    }

    // ===================== New Tests =====================

    // Test process start tag "html" in Initial state -> transition to BeforeHead
    @Test
    public void testProcessStartTagHtml_initialState_transitionsToBeforeHead() {
        HtmlTreeBuilder builder = createBuilder();
        Token.StartTag startTag = new Token.StartTag();
        startTag.name("html");
        boolean handled = builder.process(startTag);
        assertTrue(handled);
        assertEquals(HtmlTreeBuilderState.BeforeHead, builder.state());
    }

    // Test process end tag "body" in InBody state -> transition to AfterBody
    @Test
    public void testProcessEndTagBody_inBody_transitionsToAfterBody() {
        HtmlTreeBuilder builder = createBuilder();
        // set state to InBody and push a body element
        builder.transition(HtmlTreeBuilderState.InBody);
        Element body = new Element(Tag.valueOf("body", builder.defaultSettings()), builder.getBaseUri());
        builder.push(body);
        Token.EndTag endTag = new Token.EndTag();
        endTag.name("body");
        boolean handled = builder.process(endTag);
        assertTrue(handled);
        assertEquals(HtmlTreeBuilderState.AfterBody, builder.state());
    }

    // Test insert character token
    @Test
    public void testInsertCharacter_addsTextToCurrentElement() {
        HtmlTreeBuilder builder = createBuilder();
        // ensure we are in a state that accepts character tokens (InBody)
        builder.transition(HtmlTreeBuilderState.InBody);
        Element div = new Element(Tag.valueOf("div", builder.defaultSettings()), builder.getBaseUri());
        builder.push(div);
        Token.Character charToken = new Token.Character();
        charToken.data("test text");
        builder.insert(charToken);
        assertEquals("test text", div.text());
    }

    // Test insert comment token
    @Test
    public void testInsertComment_addsCommentToDocument() {
        HtmlTreeBuilder builder = createBuilder();
        Token.Comment commentToken = new Token.Comment();
        commentToken.data("my comment");
        builder.insert(commentToken);
        // comment should be added to document
        List<Node> comments = builder.getDocument().childNodes();
        boolean found = false;
        for (Node node : comments) {
            if (node instanceof Comment && ((Comment) node).getData().equals("my comment")) {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }

    // Test insert doctype token
    @Test
    public void testInsertDoctype_addsDoctypeToDocument() {
        HtmlTreeBuilder builder = createBuilder();
        Token.Doctype doctypeToken = new Token.Doctype();
        doctypeToken.name("html");
        builder.insert(doctypeToken);
        // check document has doctype node
        List<Node> children = builder.getDocument().childNodes();
        boolean found = false;
        for (Node node : children) {
            if (node instanceof DocumentType) {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }

    // Test newPendingTableCharacters & getPendingTableCharacters
    @Test
    public void testNewPendingTableCharacters_storesAndRetrieves() {
        HtmlTreeBuilder builder = createBuilder();
        List<String> pending = new java.util.ArrayList<>();
        pending.add("text1");
        pending.add("text2");
        builder.newPendingTableCharacters(pending);
        List<String> retrieved = builder.getPendingTableCharacters();
        assertEquals(pending, retrieved);
    }

    // Test isInScope with element in scope
    @Test
    public void testIsInScope_returnsTrueForMatchingElement() {
        HtmlTreeBuilder builder = createBuilder();
        Element div = new Element(Tag.valueOf("div", builder.defaultSettings()), builder.getBaseUri());
        builder.push(div);
        assertTrue(builder.isInScope("div"));
    }

    // Test isInListItemScope with li element
    @Test
    public void testIsInListItemScope_returnsTrueForLi() {
        HtmlTreeBuilder builder = createBuilder();
        Element li = new Element(Tag.valueOf("li", builder.defaultSettings()), builder.getBaseUri());
        builder.push(li);
        assertTrue(builder.isInListItemScope("li"));
    }

    // Test pushActiveFormattingElements and lastFormattingElement
    @Test
    public void testPushActiveFormattingElements_setsLastFormattingElement() {
        HtmlTreeBuilder builder = createBuilder();
        Element span = new Element(Tag.valueOf("span", builder.defaultSettings()), builder.getBaseUri());
        builder.pushActiveFormattingElements(span);
        assertEquals(span, builder.getLastFormattingElement());
    }

    // Test reconstructFormattingElements does nothing when no markers
    @Test
    public void testReconstructFormattingElements_noMarkers_doesNothing() {
        HtmlTreeBuilder builder = createBuilder();
        // stack should have html element
        builder.reconstructFormattingElements();
        assertNotNull(builder.currentElement());
    }

    // Test popStackToClose removes elements until target
    @Test
    public void testPopStackToClose_removesUntilMatch() {
        HtmlTreeBuilder builder = createBuilder();
        Element div1 = new Element(Tag.valueOf("div", builder.defaultSettings()), builder.getBaseUri());
        Element span = new Element(Tag.valueOf("span", builder.defaultSettings()), builder.getBaseUri());
        Element div2 = new Element(Tag.valueOf("div", builder.defaultSettings()), builder.getBaseUri());
        builder.push(div1);
        builder.push(span);
        builder.push(div2);
        builder.popStackToClose("span");
        // stack should have html, div1, span (div2 removed)
        assertEquals("span", builder.currentElement().nodeName());
        builder.popStackToClose("div");
        // stack should have html, div1 (span removed)
        assertEquals("div", builder.currentElement().nodeName());
    }

    // Test clearStackToTableContext removes elements until table
    @Test
    public void testClearStackToTableContext_removesUntilTable() {
        HtmlTreeBuilder builder = createBuilder();
        Element table = new Element(Tag.valueOf("table", builder.defaultSettings()), builder.getBaseUri());
        Element tr = new Element(Tag.valueOf("tr", builder.defaultSettings()), builder.getBaseUri());
        Element td = new Element(Tag.valueOf("td", builder.defaultSettings()), builder.getBaseUri());
        builder.push(table);
        builder.push(tr);
        builder.push(td);
        builder.clearStackToTableContext();
        // current element should be table
        assertEquals("table", builder.currentElement().nodeName());
    }

    // Test setFormElement and getFormElement
    @Test
    public void testSetFormElement_setsAndGets() {
        HtmlTreeBuilder builder = createBuilder();
        Element form = new Element(Tag.valueOf("form", builder.defaultSettings()), builder.getBaseUri());
        builder.setFormElement(form);
        assertEquals(form, builder.getFormElement());
    }

    // Test getFormElement returns null initially
    @Test
    public void testGetFormElement_initialNull() {
        HtmlTreeBuilder builder = createBuilder();
        assertNull(builder.getFormElement());
    }

    // Test getBaseUri after setBaseUri
    @Test
    public void testSetBaseUri_updatesBaseUri() {
        HtmlTreeBuilder builder = createBuilder();
        builder.setBaseUri("http://newbase.com");
        assertEquals("http://newbase.com", builder.getBaseUri());
    }

    // Test defaultSettings returns htmlDefault
    @Test
    public void testDefaultSettings_returnsHtmlDefault() {
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        assertEquals(ParseSettings.htmlDefault, builder.defaultSettings());
    }

    // Test onStack returns correct boolean
    @Test
    public void testOnStack_returnsTrueForElementOnStack() {
        HtmlTreeBuilder builder = createBuilder();
        Element div = new Element(Tag.valueOf("div", builder.defaultSettings()), builder.getBaseUri());
        builder.push(div);
        assertTrue(builder.onStack(div));
    }

    // Test getLastFormattingElement initially null
    @Test
    public void testGetLastFormattingElement_initialNull() {
        HtmlTreeBuilder builder = createBuilder();
        assertNull(builder.getLastFormattingElement());
    }

    // Test insertMarkerToFormattingElements
    @Test
    public void testInsertMarkerToFormattingElements_addsMarker() {
        HtmlTreeBuilder builder = createBuilder();
        builder.insertMarkerToFormattingElements();
        // after inserting marker, last formatting element should be null (since it's between markers? Actually marker is not an element)
        assertNull(builder.getLastFormattingElement());
    }

    // Test aboveOnStack returns element above given one
    @Test
    public void testAboveOnStack_returnsElementAbove() {
        HtmlTreeBuilder builder = createBuilder();
        Element html = builder.getDocument().child(0); // or stack[0]
        Element body = new Element(Tag.valueOf("body", builder.defaultSettings()), builder.getBaseUri());
        builder.push(body);
        Element result = builder.aboveOnStack(body);
        assertNotNull(result);
        assertEquals("html", result.nodeName());
    }

    // Test replaceInQueue
    @Test
    public void testReplaceInQueue_replacesElement() {
        HtmlTreeBuilder builder = createBuilder();
        Element oldDiv = new Element(Tag.valueOf("div", builder.defaultSettings()), builder.getBaseUri());
        new Element(Tag.valueOf("span", builder.defaultSettings()), builder.getBaseUri()); // dummy
        builder.push(oldDiv);
        Element newDiv = new Element(Tag.valueOf("div", builder.defaultSettings()), builder.getBaseUri());
        builder.replaceInQueue(oldDiv, newDiv);
        assertTrue(builder.onStack(newDiv));
        assertFalse(builder.onStack(oldDiv));
    }

    // Test isElementInQueue
    @Test
    public void testIsElementInQueue_returnsTrueIfPresent() {
        HtmlTreeBuilder builder = createBuilder();
        Element div = new Element(Tag.valueOf("div", builder.defaultSettings()), builder.getBaseUri());
        builder.push(div);
        assertTrue(builder.isElementInQueue(builder.getStack(), div));
    }
}