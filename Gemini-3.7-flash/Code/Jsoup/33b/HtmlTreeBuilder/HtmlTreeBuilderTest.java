package org.jsoup.parser;

import org.junit.Before;
import org.junit.Test;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.FormElement;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class HtmlTreeBuilderTest {

    private HtmlTreeBuilder treeBuilder;

    @Before
    public void setUp() {
        treeBuilder = new HtmlTreeBuilder();
    }

    // Tests standard document parsing
    @Test
    public void testParse_standardHtml_returnsConstructedDocument() {
        String html = "<html><head><title>Test</title></head><body><p>Hello</p></body></html>";
        Document doc = treeBuilder.parse(html, "http://example.com/", ParseErrorList.noTracking());

        assertNotNull(doc);
        assertEquals("Test", doc.title());
        assertEquals("Hello", doc.select("p").text());
        assertEquals("http://example.com/", treeBuilder.getBaseUri());
    }

    // Tests self closing tag insertion behavior for known and unknown elements
    @Test
    public void testInsert_selfClosingTags_handledCorrectly() {
        String html = "<div><img src='test.png'/><custom id='c1'/><p>Text</p></div>";
        Document doc = treeBuilder.parse(html, "http://example.com/", ParseErrorList.tracking(10));

        assertNotNull(doc);
        assertNotNull(doc.select("img").first());
        assertNotNull(doc.select("custom#c1").first());
        assertEquals("Text", doc.select("p").text());
    }

    // Tests fragment parsing with a specified context element
    @Test
    public void testParseFragment_withContextElement_returnsParsedNodes() {
        Element context = new Element(Tag.valueOf("div"), "http://example.com/");
        List<Node> nodes = treeBuilder.parseFragment("<span>One</span><span>Two</span>", context, "http://example.com/", ParseErrorList.noTracking());

        assertEquals(2, nodes.size());
        assertEquals("span", nodes.get(0).nodeName());
        assertEquals("span", nodes.get(1).nodeName());
    }

    // Tests fragment parsing when context element is null
    @Test
    public void testParseFragment_nullContext_returnsDocumentChildren() {
        List<Node> nodes = treeBuilder.parseFragment("<div>Body content</div>", null, "http://example.com/", ParseErrorList.noTracking());

        assertFalse(nodes.isEmpty());
        assertTrue(treeBuilder.isFragmentParsing());
    }

    // Tests stack manipulation operations: push, pop, onStack, getFromStack, and removeFromStack
    @Test
    public void testStackOperations_pushPopAndSearch_operatesCorrectly() {
        treeBuilder.parse("<html><head></head><body></body></html>", "http://example.com/", ParseErrorList.noTracking());
        Element div = new Element(Tag.valueOf("div"), "http://example.com/");
        Element span = new Element(Tag.valueOf("span"), "http://example.com/");

        treeBuilder.push(div);
        treeBuilder.push(span);

        assertTrue(treeBuilder.onStack(div));
        assertTrue(treeBuilder.onStack(span));
        assertSame(span, treeBuilder.getFromStack("span"));
        assertSame(div, treeBuilder.aboveOnStack(span));

        Element popped = treeBuilder.pop();
        assertSame(span, popped);
        assertFalse(treeBuilder.onStack(span));

        assertTrue(treeBuilder.removeFromStack(div));
        assertFalse(treeBuilder.onStack(div));
    }

    // Tests popping the stack until a given element is closed
    @Test
    public void testPopStackToClose_targetElement_removesTargetAndDescendants() {
        treeBuilder.parse("<html><head></head><body></body></html>", "http://example.com/", ParseErrorList.noTracking());
        Element div = new Element(Tag.valueOf("div"), "http://example.com/");
        Element p = new Element(Tag.valueOf("p"), "http://example.com/");
        Element span = new Element(Tag.valueOf("span"), "http://example.com/");

        treeBuilder.push(div);
        treeBuilder.push(p);
        treeBuilder.push(span);

        treeBuilder.popStackToClose("p");

        assertTrue(treeBuilder.onStack(div));
        assertFalse(treeBuilder.onStack(p));
        assertFalse(treeBuilder.onStack(span));
    }

    // Tests clearing the stack to table context
    @Test
    public void testClearStackToTableContext_clearsUntilTableElement() {
        treeBuilder.parse("<html><head></head><body></body></html>", "http://example.com/", ParseErrorList.noTracking());
        Element table = new Element(Tag.valueOf("table"), "http://example.com/");
        Element div = new Element(Tag.valueOf("div"), "http://example.com/");
        Element span = new Element(Tag.valueOf("span"), "http://example.com/");

        treeBuilder.push(table);
        treeBuilder.push(div);
        treeBuilder.push(span);

        treeBuilder.clearStackToTableContext();

        assertTrue(treeBuilder.onStack(table));
        assertFalse(treeBuilder.onStack(div));
        assertFalse(treeBuilder.onStack(span));
    }

    // Tests element scope checking methods
    @Test
    public void testInScope_variousScopes_returnsExpectedBoolean() {
        treeBuilder.parse("<html><head></head><body></body></html>", "http://example.com/", ParseErrorList.noTracking());
        Element div = new Element(Tag.valueOf("div"), "http://example.com/");
        Element button = new Element(Tag.valueOf("button"), "http://example.com/");
        Element p = new Element(Tag.valueOf("p"), "http://example.com/");

        treeBuilder.push(div);
        treeBuilder.push(button);
        treeBuilder.push(p);

        assertTrue(treeBuilder.inScope("p"));
        assertTrue(treeBuilder.inButtonScope("p"));
        assertFalse(treeBuilder.inButtonScope("div"));
        assertTrue(treeBuilder.inTableScope("html"));
    }

    // Tests active formatting elements list with 3-element limit rule and markers
    @Test
    public void testActiveFormattingElements_pushLimitAndMarker_managesCorrectly() {
        treeBuilder.parse("<html><head></head><body></body></html>", "http://example.com/", ParseErrorList.noTracking());

        Element b1 = new Element(Tag.valueOf("b"), "http://example.com/");
        Element b2 = new Element(Tag.valueOf("b"), "http://example.com/");
        Element b3 = new Element(Tag.valueOf("b"), "http://example.com/");
        Element b4 = new Element(Tag.valueOf("b"), "http://example.com/");

        treeBuilder.pushActiveFormattingElements(b1);
        treeBuilder.pushActiveFormattingElements(b2);
        treeBuilder.pushActiveFormattingElements(b3);
        treeBuilder.pushActiveFormattingElements(b4);

        // First duplicate b1 should be removed when 4th duplicate is pushed (max 3)
        assertFalse(treeBuilder.isInActiveFormattingElements(b1));
        assertTrue(treeBuilder.isInActiveFormattingElements(b4));
        assertSame(b4, treeBuilder.getActiveFormattingElement("b"));

        treeBuilder.insertMarkerToFormattingElements();
        assertNull(treeBuilder.getActiveFormattingElement("b"));

        treeBuilder.clearFormattingElementsToLastMarker();
        assertTrue(treeBuilder.isInActiveFormattingElements(b4));
    }

    // Tests foster parenting when inserts are fostered
    @Test
    public void testFosterInserts_fosterEnabled_insertsIntoFosterParent() {
        Document doc = treeBuilder.parse("<table><tr><td>Cell</td></tr>Plain text outside</table>", "http://example.com/", ParseErrorList.noTracking());
        assertNotNull(doc);
        // Foster parenting moves plain text before table or inside body
        assertTrue(doc.body().text().contains("Plain text outside"));
    }

    // Tests setting base URI from <base href> tag
    @Test
    public void testMaybeSetBaseUri_validBaseElement_setsDocumentBaseUri() {
        treeBuilder.parse("<html><head></head><body></body></html>", "http://example.com/", ParseErrorList.noTracking());

        Attributes attrs = new Attributes();
        attrs.put("href", "http://newbase.com/");
        Element base = new Element(Tag.valueOf("base"), "http://example.com/", attrs);

        treeBuilder.maybeSetBaseUri(base);
        assertEquals("http://newbase.com/", treeBuilder.getBaseUri());

        // Subsequent base tags should be ignored
        Attributes attrs2 = new Attributes();
        attrs2.put("href", "http://anotherbase.com/");
        Element base2 = new Element(Tag.valueOf("base"), "http://newbase.com/", attrs2);

        treeBuilder.maybeSetBaseUri(base2);
        assertEquals("http://newbase.com/", treeBuilder.getBaseUri());
    }

    // Tests character insertion in script/style tags vs regular elements
    @Test
    public void testInsertCharacter_scriptAndRegular_createsCorrectNodeTypes() {
        treeBuilder.parse("<html><head></head><body></body></html>", "http://example.com/", ParseErrorList.noTracking());

        Element script = new Element(Tag.valueOf("script"), "http://example.com/");
        treeBuilder.push(script);

        Token.Character charToken = new Token.Character();
        charToken.data("var x = 1;");
        treeBuilder.insert(charToken);
        assertEquals(1, script.childNodeSize());
        assertTrue(script.childNode(0) instanceof DataNode);

        treeBuilder.pop();

        Element div = new Element(Tag.valueOf("div"), "http://example.com/");
        treeBuilder.push(div);

        Token.Character textToken = new Token.Character();
        textToken.data("Regular text");
        treeBuilder.insert(textToken);
        assertEquals(1, div.childNodeSize());
        assertTrue(div.childNode(0) instanceof TextNode);
    }

    // Tests generating implied end tags
    @Test
    public void testGenerateImpliedEndTags_withExcludeTag_popsCorrectly() {
        treeBuilder.parse("<html><head></head><body></body></html>", "http://example.com/", ParseErrorList.noTracking());

        Element p = new Element(Tag.valueOf("p"), "http://example.com/");
        Element li = new Element(Tag.valueOf("li"), "http://example.com/");
        treeBuilder.push(p);
        treeBuilder.push(li);

        treeBuilder.generateImpliedEndTags("p");

        assertFalse(treeBuilder.onStack(li));
        assertTrue(treeBuilder.onStack(p));
    }

    // Tests checking if elements are special tags
    @Test
    public void testIsSpecial_variousTags_returnsExpectedResult() {
        assertTrue(treeBuilder.isSpecial(new Element(Tag.valueOf("div"), "")));
        assertTrue(treeBuilder.isSpecial(new Element(Tag.valueOf("table"), "")));
        assertTrue(treeBuilder.isSpecial(new Element(Tag.valueOf("script"), "")));
        assertTrue(treeBuilder.isSpecial(new Element(Tag.valueOf("p"), "")));
        assertFalse(treeBuilder.isSpecial(new Element(Tag.valueOf("custom"), "")));
        assertFalse(treeBuilder.isSpecial(new Element(Tag.valueOf("span"), "")));
    }

    // Tests resetting insertion mode according to current stack
    @Test
    public void testResetInsertionMode_selectOnStack_transitionsToInSelect() {
        treeBuilder.parse("<html><head></head><body></body></html>", "http://example.com/", ParseErrorList.noTracking());
        Element select = new Element(Tag.valueOf("select"), "http://example.com/");
        treeBuilder.push(select);

        treeBuilder.resetInsertionMode();
        assertEquals(HtmlTreeBuilderState.InSelect, treeBuilder.state());
    }

    // Tests form element insertion and association with form controls
    @Test
    public void testInsertForm_withOnStack_registersFormElement() {
        treeBuilder.parse("<html><head></head><body></body></html>", "http://example.com/", ParseErrorList.noTracking());

        Token.StartTag formStart = new Token.StartTag();
        formStart.name("form");
        FormElement formEl = treeBuilder.insertForm(formStart, true);

        assertNotNull(formEl);
        assertSame(formEl, treeBuilder.getFormElement());
        assertTrue(treeBuilder.onStack(formEl));

        Token.StartTag inputStart = new Token.StartTag();
        inputStart.name("input");
        Element inputEl = treeBuilder.insertEmpty(inputStart);

        assertTrue(formEl.elements().contains(inputEl));
    }

    // Tests comment insertion
    @Test
    public void testInsertComment_createsCommentNodeInCurrentElement() {
        treeBuilder.parse("<html><head></head><body></body></html>", "http://example.com/", ParseErrorList.noTracking());

        Element div = new Element(Tag.valueOf("div"), "http://example.com/");
        treeBuilder.push(div);

        Token.Comment commentToken = new Token.Comment();
        commentToken.getData().append("Test comment");
        treeBuilder.insert(commentToken);

        assertEquals(1, div.childNodeSize());
        assertTrue(div.childNode(0) instanceof Comment);
        assertEquals("Test comment", ((Comment) div.childNode(0)).getData());
    }

    @Test
    public void testClearStackToTableBodyContext_and_TableRowContext() {
        treeBuilder.parse("<html><head></head><body></body></html>", "http://example.com/", ParseErrorList.noTracking());

        Element tbody = new Element(Tag.valueOf("tbody"), "http://example.com/");
        Element div1 = new Element(Tag.valueOf("div"), "http://example.com/");
        treeBuilder.push(tbody);
        treeBuilder.push(div1);
        treeBuilder.clearStackToTableBodyContext();
        assertTrue(treeBuilder.onStack(tbody));
        assertFalse(treeBuilder.onStack(div1));

        Element tr = new Element(Tag.valueOf("tr"), "http://example.com/");
        Element div2 = new Element(Tag.valueOf("div"), "http://example.com/");
        treeBuilder.push(tr);
        treeBuilder.push(div2);
        treeBuilder.clearStackToTableRowContext();
        assertTrue(treeBuilder.onStack(tr));
        assertFalse(treeBuilder.onStack(div2));
    }

    @Test
    public void testInListItemScope_and_InSelectScope() {
        treeBuilder.parse("<html><head></head><body></body></html>", "http://example.com/", ParseErrorList.noTracking());

        Element ol = new Element(Tag.valueOf("ol"), "http://example.com/");
        Element li = new Element(Tag.valueOf("li"), "http://example.com/");
        treeBuilder.push(ol);
        treeBuilder.push(li);

        assertTrue(treeBuilder.inListItemScope("li"));
        assertFalse(treeBuilder.inListItemScope("body"));

        Element optgroup = new Element(Tag.valueOf("optgroup"), "http://example.com/");
        Element option = new Element(Tag.valueOf("option"), "http://example.com/");
        treeBuilder.push(optgroup);
        treeBuilder.push(option);

        assertTrue(treeBuilder.inSelectScope("option"));
        assertTrue(treeBuilder.inSelectScope("optgroup"));
    }

    @Test
    public void testPopStackToBefore_and_PopStackToCloseArray() {
        treeBuilder.parse("<html><head></head><body></body></html>", "http://example.com/", ParseErrorList.noTracking());

        Element div = new Element(Tag.valueOf("div"), "http://example.com/");
        Element p = new Element(Tag.valueOf("p"), "http://example.com/");
        Element span = new Element(Tag.valueOf("span"), "http://example.com/");

        treeBuilder.push(div);
        treeBuilder.push(p);
        treeBuilder.push(span);

        treeBuilder.popStackToBefore("p");
        assertTrue(treeBuilder.onStack(div));
        assertTrue(treeBuilder.onStack(p));
        assertFalse(treeBuilder.onStack(span));

        treeBuilder.push(span);
        treeBuilder.popStackToClose(new String[]{"div", "span"});
        assertFalse(treeBuilder.onStack(span));
    }

    @Test
    public void testFormattingElements_replaceAndRemoveOperations() {
        treeBuilder.parse("<html><head></head><body></body></html>", "http://example.com/", ParseErrorList.noTracking());

        Element b = new Element(Tag.valueOf("b"), "http://example.com/");
        Element i = new Element(Tag.valueOf("i"), "http://example.com/");

        treeBuilder.pushActiveFormattingElements(b);
        assertTrue(treeBuilder.isInActiveFormattingElements(b));

        treeBuilder.replaceActiveFormattingElement(b, i);
        assertFalse(treeBuilder.isInActiveFormattingElements(b));
        assertTrue(treeBuilder.isInActiveFormattingElements(i));

        treeBuilder.removeFromActiveFormattingElements(i);
        assertFalse(treeBuilder.isInActiveFormattingElements(i));
    }

    @Test
    public void testPendingTableCharacters_and_HeadBodyGettersSetters() {
        treeBuilder.parse("<html><head></head><body></body></html>", "http://example.com/", ParseErrorList.noTracking());

        List<String> pending = new ArrayList<String>();
        pending.add("text1");
        treeBuilder.setPendingTableCharacters(pending);
        assertEquals(pending, treeBuilder.getPendingTableCharacters());

        Element head = new Element(Tag.valueOf("head"), "http://example.com/");
        Element body = new Element(Tag.valueOf("body"), "http://example.com/");
        treeBuilder.setHeadElement(head);
        treeBuilder.setBodyElement(body);
        assertSame(head, treeBuilder.getHeadElement());
        assertSame(body, treeBuilder.getBodyElement());

        treeBuilder.setFosterInserts(true);
        assertTrue(treeBuilder.isFosterInserts());
        treeBuilder.setFosterInserts(false);
        assertFalse(treeBuilder.isFosterInserts());
    }

    @Test
    public void testReconstructFormattingElements_withUnclosedFormattingElements() {
        treeBuilder.parse("<html><head></head><body></body></html>", "http://example.com/", ParseErrorList.noTracking());

        Element b = new Element(Tag.valueOf("b"), "http://example.com/");
        treeBuilder.pushActiveFormattingElements(b);

        treeBuilder.reconstructFormattingElements();
        assertEquals("b", treeBuilder.currentElement().nodeName());
    }
}