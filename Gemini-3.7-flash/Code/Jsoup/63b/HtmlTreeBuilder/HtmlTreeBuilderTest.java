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
    }

    // Tests initial state and settings initialization
    @Test
    public void testInitialiseParse_defaultSettings_stateInitialAndDocCreated() {
        ParseErrorList errors = ParseErrorList.tracking(10);
        treeBuilder.initialiseParse(new StringReader("<div>test</div>"), "http://example.com", errors, ParseSettings.htmlDefault);

        assertEquals(HtmlTreeBuilderState.Initial, treeBuilder.state());
        assertNull(treeBuilder.originalState());
        assertNotNull(treeBuilder.getDocument());
        assertEquals("http://example.com", treeBuilder.getBaseUri());
        assertTrue(treeBuilder.framesetOk());
        assertFalse(treeBuilder.isFosterInserts());
        assertFalse(treeBuilder.isFragmentParsing());
    }

    // Tests maybeSetBaseUri with valid href and ignores second base tag
    @Test
    public void testMaybeSetBaseUri_validHref_setsBaseUriOnce() {
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", ParseErrorList.noTracking(), ParseSettings.htmlDefault);

        Element base1 = new Element(Tag.valueOf("base", ParseSettings.htmlDefault), "http://example.com");
        base1.attr("href", "http://example.com/sub/");
        treeBuilder.maybeSetBaseUri(base1);

        assertEquals("http://example.com/sub/", treeBuilder.getBaseUri());
        assertEquals("http://example.com/sub/", treeBuilder.getDocument().baseUri());

        Element base2 = new Element(Tag.valueOf("base", ParseSettings.htmlDefault), "http://example.com");
        base2.attr("href", "http://example.com/other/");
        treeBuilder.maybeSetBaseUri(base2);

        assertEquals("http://example.com/sub/", treeBuilder.getBaseUri());
    }

    // Tests parsing fragment with null context
    @Test
    public void testParseFragment_nullContext_returnsChildNodes() {
        ParseErrorList errors = ParseErrorList.noTracking();
        List<Node> nodes = treeBuilder.parseFragment("<p>One</p><p>Two</p>", null, "http://example.com", errors, ParseSettings.htmlDefault);

        assertTrue(treeBuilder.isFragmentParsing());
        assertFalse(nodes.isEmpty());
    }

    // Tests parsing fragment with specific context elements (title, style, script)
    @Test
    public void testParseFragment_withContextElement_transitionsTokeniser() {
        Element contextTitle = new Element(Tag.valueOf("title", ParseSettings.htmlDefault), "");
        List<Node> titleNodes = treeBuilder.parseFragment("Hello &amp; World", contextTitle, "http://example.com", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        assertNotNull(titleNodes);

        Element contextStyle = new Element(Tag.valueOf("style", ParseSettings.htmlDefault), "");
        List<Node> styleNodes = treeBuilder.parseFragment("body { color: red; }", contextStyle, "http://example.com", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        assertNotNull(styleNodes);

        Element contextScript = new Element(Tag.valueOf("script", ParseSettings.htmlDefault), "");
        List<Node> scriptNodes = treeBuilder.parseFragment("var x = 1;", contextScript, "http://example.com", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        assertNotNull(scriptNodes);
    }

    // Tests parsing fragment inside a form element context to associate form controls
    @Test
    public void testParseFragment_insideForm_associatesFormElement() {
        FormElement form = new FormElement(Tag.valueOf("form", ParseSettings.htmlDefault), "http://example.com", new Attributes());
        List<Node> nodes = treeBuilder.parseFragment("<input type='text' name='q' />", form, "http://example.com", ParseErrorList.noTracking(), ParseSettings.htmlDefault);

        assertEquals(1, nodes.size());
        assertEquals(form, treeBuilder.getFormElement());
    }

    // Tests self-closing tag handling in insert and acknowledge self-closing flag
    @Test
    public void testInsert_selfClosingStartTag_acknowledgesAndEmitsEnd() {
        ParseErrorList errors = ParseErrorList.tracking(10);
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", errors, ParseSettings.htmlDefault);

        Element html = new Element(Tag.valueOf("html", ParseSettings.htmlDefault), "");
        Element body = new Element(Tag.valueOf("body", ParseSettings.htmlDefault), "");
        treeBuilder.getStack().add(html);
        treeBuilder.getStack().add(body);

        Token.StartTag startTag = new Token.StartTag();
        startTag.nameAttr("img", new Attributes());
        startTag.selfClosing = true;

        Element el = treeBuilder.insert(startTag);
        assertEquals("img", el.nodeName());
    }

    // Tests insertEmpty for known vs unknown self-closing tags
    @Test
    public void testInsertEmpty_knownAndCustomSelfClosingTag_acknowledgesFlag() {
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        Element html = new Element(Tag.valueOf("html", ParseSettings.htmlDefault), "");
        treeBuilder.getStack().add(html);

        Token.StartTag brTag = new Token.StartTag();
        brTag.nameAttr("br", new Attributes());
        brTag.selfClosing = true;
        Element brEl = treeBuilder.insertEmpty(brTag);
        assertEquals("br", brEl.nodeName());

        Token.StartTag customTag = new Token.StartTag();
        customTag.nameAttr("custom-tag", new Attributes());
        customTag.selfClosing = true;
        Element customEl = treeBuilder.insertEmpty(customTag);
        assertEquals("custom-tag", customEl.nodeName());
        assertTrue(customEl.tag().isSelfClosing());
    }

    // Tests insertForm method with onStack true and false
    @Test
    public void testInsertForm_onStackTrueAndFalse_setsFormElement() {
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        Element html = new Element(Tag.valueOf("html", ParseSettings.htmlDefault), "");
        treeBuilder.getStack().add(html);

        Token.StartTag formTag = new Token.StartTag();
        formTag.nameAttr("form", new Attributes());

        FormElement form1 = treeBuilder.insertForm(formTag, true);
        assertEquals(form1, treeBuilder.getFormElement());
        assertTrue(treeBuilder.onStack(form1));

        Token.StartTag formTag2 = new Token.StartTag();
        formTag2.nameAttr("form", new Attributes());
        FormElement form2 = treeBuilder.insertForm(formTag2, false);
        assertEquals(form2, treeBuilder.getFormElement());
        assertFalse(treeBuilder.onStack(form2));
    }

    // Tests insert Character token as DataNode for script/style and TextNode for normal elements
    @Test
    public void testInsert_characterToken_scriptCreatesDataNodeAndDivCreatesTextNode() {
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        Element script = new Element(Tag.valueOf("script", ParseSettings.htmlDefault), "");
        treeBuilder.push(script);

        Token.Character charToken = new Token.Character();
        charToken.data("alert('hello');");
        treeBuilder.insert(charToken);
        assertEquals(1, script.childNodeSize());
        assertTrue(script.childNode(0) instanceof DataNode);

        Element div = new Element(Tag.valueOf("div", ParseSettings.htmlDefault), "");
        treeBuilder.push(div);
        Token.Character textChar = new Token.Character();
        textChar.data("Hello world");
        treeBuilder.insert(textChar);
        assertEquals(1, div.childNodeSize());
        assertTrue(div.childNode(0) instanceof TextNode);
    }

    // Tests stack manipulation methods: pop, push, aboveOnStack, getFromStack, removeFromStack
    @Test
    public void testStackOperations_pushPopAboveGetRemove_behaveCorrectly() {
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        Element html = new Element(Tag.valueOf("html", ParseSettings.htmlDefault), "");
        Element body = new Element(Tag.valueOf("body", ParseSettings.htmlDefault), "");
        Element div = new Element(Tag.valueOf("div", ParseSettings.htmlDefault), "");

        treeBuilder.push(html);
        treeBuilder.push(body);
        treeBuilder.push(div);

        assertTrue(treeBuilder.onStack(body));
        assertEquals(div, treeBuilder.currentElement());
        assertEquals(body, treeBuilder.aboveOnStack(div));
        assertEquals(div, treeBuilder.getFromStack("div"));
        assertNull(treeBuilder.getFromStack("span"));

        Element popped = treeBuilder.pop();
        assertEquals(div, popped);
        assertFalse(treeBuilder.onStack(div));

        boolean removed = treeBuilder.removeFromStack(body);
        assertTrue(removed);
        assertFalse(treeBuilder.onStack(body));

        boolean removedAgain = treeBuilder.removeFromStack(body);
        assertFalse(removedAgain);
    }

    // Tests popStackToClose, popStackToBefore, and clearStackToTableContext
    @Test
    public void testStackPoppingToTargets_variousMethods_popCorrectElements() {
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        Element html = new Element(Tag.valueOf("html", ParseSettings.htmlDefault), "");
        Element table = new Element(Tag.valueOf("table", ParseSettings.htmlDefault), "");
        Element tbody = new Element(Tag.valueOf("tbody", ParseSettings.htmlDefault), "");
        Element tr = new Element(Tag.valueOf("tr", ParseSettings.htmlDefault), "");
        Element td = new Element(Tag.valueOf("td", ParseSettings.htmlDefault), "");

        treeBuilder.push(html);
        treeBuilder.push(table);
        treeBuilder.push(tbody);
        treeBuilder.push(tr);
        treeBuilder.push(td);

        treeBuilder.clearStackToTableRowContext();
        assertEquals("tr", treeBuilder.currentElement().nodeName());

        treeBuilder.push(td);
        treeBuilder.clearStackToTableBodyContext();
        assertEquals("tbody", treeBuilder.currentElement().nodeName());

        treeBuilder.push(tr);
        treeBuilder.clearStackToTableContext();
        assertEquals("table", treeBuilder.currentElement().nodeName());

        treeBuilder.push(tbody);
        treeBuilder.popStackToBefore("tbody");
        assertEquals("table", treeBuilder.currentElement().nodeName());

        treeBuilder.push(tbody);
        treeBuilder.popStackToClose("tbody");
        assertEquals("table", treeBuilder.currentElement().nodeName());

        treeBuilder.push(tbody);
        treeBuilder.popStackToClose("tbody", "table");
        assertEquals("table", treeBuilder.currentElement().nodeName());
    }

    // Tests insertOnStackAfter and replaceOnStack
    @Test
    public void testStackInsertAfterAndReplace_validElements_modifiesStack() {
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        Element html = new Element(Tag.valueOf("html", ParseSettings.htmlDefault), "");
        Element body = new Element(Tag.valueOf("body", ParseSettings.htmlDefault), "");
        treeBuilder.push(html);
        treeBuilder.push(body);

        Element div = new Element(Tag.valueOf("div", ParseSettings.htmlDefault), "");
        treeBuilder.insertOnStackAfter(html, div);
        assertEquals(3, treeBuilder.getStack().size());
        assertEquals(div, treeBuilder.getStack().get(1));

        Element p = new Element(Tag.valueOf("p", ParseSettings.htmlDefault), "");
        treeBuilder.replaceOnStack(div, p);
        assertEquals(p, treeBuilder.getStack().get(1));
    }

    // Tests resetInsertionMode across different context tags on the stack
    @Test
    public void testResetInsertionMode_variousStackElements_transitionsCorrectly() {
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", ParseErrorList.noTracking(), ParseSettings.htmlDefault);

        Element html = new Element(Tag.valueOf("html", ParseSettings.htmlDefault), "");
        Element table = new Element(Tag.valueOf("table", ParseSettings.htmlDefault), "");
        Element select = new Element(Tag.valueOf("select", ParseSettings.htmlDefault), "");

        treeBuilder.push(html);
        treeBuilder.push(table);
        treeBuilder.push(select);

        treeBuilder.resetInsertionMode();
        assertEquals(HtmlTreeBuilderState.InSelect, treeBuilder.state());

        treeBuilder.pop(); // remove select
        treeBuilder.resetInsertionMode();
        assertEquals(HtmlTreeBuilderState.InTable, treeBuilder.state());

        Element tbody = new Element(Tag.valueOf("tbody", ParseSettings.htmlDefault), "");
        treeBuilder.push(tbody);
        treeBuilder.resetInsertionMode();
        assertEquals(HtmlTreeBuilderState.InTableBody, treeBuilder.state());

        Element tr = new Element(Tag.valueOf("tr", ParseSettings.htmlDefault), "");
        treeBuilder.push(tr);
        treeBuilder.resetInsertionMode();
        assertEquals(HtmlTreeBuilderState.InRow, treeBuilder.state());

        Element td = new Element(Tag.valueOf("td", ParseSettings.htmlDefault), "");
        treeBuilder.push(td);
        treeBuilder.resetInsertionMode();
        assertEquals(HtmlTreeBuilderState.InCell, treeBuilder.state());
    }

    // Tests scope checking methods (inScope, inListItemScope, inButtonScope, inTableScope, inSelectScope)
    @Test
    public void testScopeMethods_variousElementsInStack_returnsExpectedScope() {
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        Element html = new Element(Tag.valueOf("html", ParseSettings.htmlDefault), "");
        Element table = new Element(Tag.valueOf("table", ParseSettings.htmlDefault), "");
        Element td = new Element(Tag.valueOf("td", ParseSettings.htmlDefault), "");
        Element p = new Element(Tag.valueOf("p", ParseSettings.htmlDefault), "");

        treeBuilder.push(html);
        treeBuilder.push(table);
        treeBuilder.push(td);
        treeBuilder.push(p);

        assertTrue(treeBuilder.inScope("p"));
        assertTrue(treeBuilder.inScope("td"));
        assertTrue(treeBuilder.inTableScope("table"));
        assertFalse(treeBuilder.inScope("nonexistent"));

        Element ul = new Element(Tag.valueOf("ul", ParseSettings.htmlDefault), "");
        Element li = new Element(Tag.valueOf("li", ParseSettings.htmlDefault), "");
        treeBuilder.push(ul);
        treeBuilder.push(li);
        assertTrue(treeBuilder.inListItemScope("li"));

        Element button = new Element(Tag.valueOf("button", ParseSettings.htmlDefault), "");
        treeBuilder.push(button);
        assertTrue(treeBuilder.inButtonScope("button"));
    }

    // Tests active formatting elements list: max 3 duplicate elements rule
    @Test
    public void testPushActiveFormattingElements_maxThreeDuplicates_evictsOldest() {
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", ParseErrorList.noTracking(), ParseSettings.htmlDefault);

        Attributes attrs = new Attributes();
        attrs.put("class", "bold");

        Element b1 = new Element(Tag.valueOf("b", ParseSettings.htmlDefault), "", attrs.clone());
        Element b2 = new Element(Tag.valueOf("b", ParseSettings.htmlDefault), "", attrs.clone());
        Element b3 = new Element(Tag.valueOf("b", ParseSettings.htmlDefault), "", attrs.clone());
        Element b4 = new Element(Tag.valueOf("b", ParseSettings.htmlDefault), "", attrs.clone());

        treeBuilder.pushActiveFormattingElements(b1);
        treeBuilder.pushActiveFormattingElements(b2);
        treeBuilder.pushActiveFormattingElements(b3);
        assertTrue(treeBuilder.isInActiveFormattingElements(b1));

        treeBuilder.pushActiveFormattingElements(b4);
        assertFalse(treeBuilder.isInActiveFormattingElements(b1));
        assertTrue(treeBuilder.isInActiveFormattingElements(b2));
        assertTrue(treeBuilder.isInActiveFormattingElements(b3));
        assertTrue(treeBuilder.isInActiveFormattingElements(b4));
    }

    // Tests markers in active formatting elements and clearFormattingElementsToLastMarker
    @Test
    public void testFormattingElements_markersAndClear_removesUpToMarker() {
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", ParseErrorList.noTracking(), ParseSettings.htmlDefault);

        Element a = new Element(Tag.valueOf("a", ParseSettings.htmlDefault), "");
        treeBuilder.pushActiveFormattingElements(a);
        treeBuilder.insertMarkerToFormattingElements();

        Element b = new Element(Tag.valueOf("b", ParseSettings.htmlDefault), "");
        treeBuilder.pushActiveFormattingElements(b);

        assertEquals(b, treeBuilder.lastFormattingElement());
        treeBuilder.clearFormattingElementsToLastMarker();

        assertEquals(a, treeBuilder.lastFormattingElement());
    }

    // Tests generateImpliedEndTags with and without exclusion
    @Test
    public void testGenerateImpliedEndTags_withAndWithoutExclusion_popsExpectedTags() {
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        Element html = new Element(Tag.valueOf("html", ParseSettings.htmlDefault), "");
        Element body = new Element(Tag.valueOf("body", ParseSettings.htmlDefault), "");
        Element p = new Element(Tag.valueOf("p", ParseSettings.htmlDefault), "");
        Element li = new Element(Tag.valueOf("li", ParseSettings.htmlDefault), "");

        treeBuilder.push(html);
        treeBuilder.push(body);
        treeBuilder.push(p);
        treeBuilder.push(li);

        treeBuilder.generateImpliedEndTags("p");
        assertEquals("p", treeBuilder.currentElement().nodeName());

        treeBuilder.generateImpliedEndTags();
        assertEquals("body", treeBuilder.currentElement().nodeName());
    }

    // Tests isSpecial method for special HTML elements
    @Test
    public void testIsSpecial_specialAndNonSpecialElements_returnsCorrectBoolean() {
        Element div = new Element(Tag.valueOf("div", ParseSettings.htmlDefault), "");
        Element table = new Element(Tag.valueOf("table", ParseSettings.htmlDefault), "");
        Element custom = new Element(Tag.valueOf("custom-tag", ParseSettings.htmlDefault), "");

        assertTrue(treeBuilder.isSpecial(div));
        assertTrue(treeBuilder.isSpecial(table));
        assertFalse(treeBuilder.isSpecial(custom));
    }

    // Tests foster parenting when fosterInserts is true and table has a parent
    @Test
    public void testInsertInFosterParent_tableHasParent_insertsBeforeTable() {
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        Element html = new Element(Tag.valueOf("html", ParseSettings.htmlDefault), "");
        Element body = new Element(Tag.valueOf("body", ParseSettings.htmlDefault), "");
        Element table = new Element(Tag.valueOf("table", ParseSettings.htmlDefault), "");
        body.appendChild(table);

        treeBuilder.push(html);
        treeBuilder.push(body);
        treeBuilder.push(table);
        treeBuilder.setFosterInserts(true);

        Comment comment = new Comment("fostered comment", "http://example.com");
        treeBuilder.insert(new Token.Comment().setData("fostered comment"));

        assertEquals(2, body.childNodeSize());
        assertEquals(comment.getData(), ((Comment) body.childNode(0)).getData());
        assertEquals(table, body.childNode(1));
    }

    // Tests pending table characters list get, set, new
    @Test
    public void testPendingTableCharacters_manipulation_holdsStrings() {
        treeBuilder.newPendingTableCharacters();
        assertNotNull(treeBuilder.getPendingTableCharacters());
        assertTrue(treeBuilder.getPendingTableCharacters().isEmpty());

        List<String> list = new ArrayList<>();
        list.add("test");
        treeBuilder.setPendingTableCharacters(list);
        assertEquals(1, treeBuilder.getPendingTableCharacters().size());
        assertEquals("test", treeBuilder.getPendingTableCharacters().get(0));
    }

    // Tests error logging when ParseErrorList tracks errors
    @Test
    public void testError_trackingErrors_recordsParseError() {
        ParseErrorList errors = ParseErrorList.tracking(10);
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", errors, ParseSettings.htmlDefault);
        treeBuilder.process(new Token.StartTag().nameAttr("invalid", new Attributes()));

        treeBuilder.error(HtmlTreeBuilderState.InBody);
        assertEquals(1, errors.size());
    }
}