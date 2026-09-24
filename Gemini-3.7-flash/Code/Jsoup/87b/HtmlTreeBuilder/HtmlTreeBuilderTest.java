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
import java.util.List;

import static org.junit.Assert.*;

public class HtmlTreeBuilderTest {

    private HtmlTreeBuilder treeBuilder;
    private Parser parser;

    @Before
    public void setUp() {
        treeBuilder = new HtmlTreeBuilder();
        parser = new Parser(treeBuilder);
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", parser);
    }

    // Tests implied end tags with null excludeTag pops eligible elements like <p> (Defects4J bug 87)
    @Test
    public void testGenerateImpliedEndTags_nullExcludeTag_popsImpliedTags() {
        Element html = treeBuilder.insertStartTag("html");
        Element body = treeBuilder.insertStartTag("body");
        Element p = treeBuilder.insertStartTag("p");

        assertEquals(p, treeBuilder.currentElement());
        treeBuilder.generateImpliedEndTags();
        assertEquals(body, treeBuilder.currentElement());
    }

    // Tests implied end tags when excludeTag matches current element
    @Test
    public void testGenerateImpliedEndTags_withMatchingExcludeTag_doesNotPopExcludedTag() {
        treeBuilder.insertStartTag("html");
        treeBuilder.insertStartTag("body");
        Element p = treeBuilder.insertStartTag("p");

        assertEquals(p, treeBuilder.currentElement());
        treeBuilder.generateImpliedEndTags("p");
        assertEquals(p, treeBuilder.currentElement());
    }

    // Tests implied end tags when excludeTag is different from current tag
    @Test
    public void testGenerateImpliedEndTags_withDifferentExcludeTag_popsTargetTag() {
        treeBuilder.insertStartTag("html");
        Element body = treeBuilder.insertStartTag("body");
        Element li = treeBuilder.insertStartTag("li");

        assertEquals(li, treeBuilder.currentElement());
        treeBuilder.generateImpliedEndTags("p");
        assertEquals(body, treeBuilder.currentElement());
    }

    // Tests normal HTML parsing with implied end tags for paragraphs
    @Test
    public void testParse_consecutiveParagraphs_createsSiblingsNotNested() {
        Document doc = Jsoup.parse("<p>One<p>Two");
        assertEquals(2, doc.body().children().size());
        assertEquals("One", doc.body().child(0).text());
        assertEquals("Two", doc.body().child(1).text());
    }

    // Tests parsing an HTML fragment with context element
    @Test
    public void testParseFragment_withContextElement_returnsParsedNodes() {
        Element context = new Element(Tag.valueOf("div"), "");
        List<Node> nodes = treeBuilder.parseFragment("<span>text</span>", context, "http://example.com", parser);

        assertNotNull(nodes);
        assertFalse(nodes.isEmpty());
        assertEquals("span", ((Element) nodes.get(0)).tagName());
    }

    // Tests parsing an HTML fragment with null context element
    @Test
    public void testParseFragment_nullContext_returnsNodesFromDoc() {
        List<Node> nodes = treeBuilder.parseFragment("<div>content</div>", null, "http://example.com", parser);
        assertNotNull(nodes);
        assertFalse(nodes.isEmpty());
    }

    // Tests push, pop, onStack and getFromStack functionality
    @Test
    public void testStackOperations_pushAndPop_maintainsCorrectStackState() {
        Element el1 = new Element(Tag.valueOf("div"), "");
        Element el2 = new Element(Tag.valueOf("span"), "");

        treeBuilder.push(el1);
        treeBuilder.push(el2);

        assertTrue(treeBuilder.onStack(el1));
        assertTrue(treeBuilder.onStack(el2));
        assertEquals(el2, treeBuilder.getFromStack("span"));
        assertEquals(el2, treeBuilder.currentElement());

        Element popped = treeBuilder.pop();
        assertEquals(el2, popped);
        assertFalse(treeBuilder.onStack(el2));
        assertEquals(el1, treeBuilder.currentElement());
    }

    // Tests removeFromStack with existing and non-existing element
    @Test
    public void testRemoveFromStack_validAndInvalidElements_removesOnlyWhenPresent() {
        Element div = new Element(Tag.valueOf("div"), "");
        Element span = new Element(Tag.valueOf("span"), "");
        Element p = new Element(Tag.valueOf("p"), "");

        treeBuilder.push(div);
        treeBuilder.push(span);

        assertTrue(treeBuilder.removeFromStack(span));
        assertFalse(treeBuilder.onStack(span));
        assertFalse(treeBuilder.removeFromStack(p));
    }

    // Tests popStackToClose with single element name
    @Test
    public void testPopStackToClose_singleTagName_popsUpToAndIncludingTarget() {
        Element html = treeBuilder.insertStartTag("html");
        treeBuilder.insertStartTag("body");
        treeBuilder.insertStartTag("div");
        treeBuilder.insertStartTag("span");

        treeBuilder.popStackToClose("body");
        assertEquals(html, treeBuilder.currentElement());
        assertFalse(treeBuilder.onStack(treeBuilder.getFromStack("div")));
    }

    // Tests popStackToClose with multiple target names
    @Test
    public void testPopStackToClose_multipleTagNames_popsUpToMatchingTag() {
        Element html = treeBuilder.insertStartTag("html");
        Element body = treeBuilder.insertStartTag("body");
        treeBuilder.insertStartTag("ul");
        treeBuilder.insertStartTag("li");

        treeBuilder.popStackToClose(HtmlTreeBuilder.TagSearchList);
        assertEquals(body, treeBuilder.currentElement());
    }

    // Tests popStackToBefore target name
    @Test
    public void testPopStackToBefore_targetTagName_popsElementsAboveTarget() {
        treeBuilder.insertStartTag("html");
        Element body = treeBuilder.insertStartTag("body");
        treeBuilder.insertStartTag("div");
        treeBuilder.insertStartTag("p");

        treeBuilder.popStackToBefore("body");
        assertEquals(body, treeBuilder.currentElement());
    }

    // Tests inScope checks for base scope elements
    @Test
    public void testInScope_elementsInAndOutOfScope_returnsExpectedBoolean() {
        treeBuilder.insertStartTag("html");
        treeBuilder.insertStartTag("body");
        treeBuilder.insertStartTag("table");
        treeBuilder.insertStartTag("tr");
        treeBuilder.insertStartTag("td");
        treeBuilder.insertStartTag("div");

        assertTrue(treeBuilder.inScope("div"));
        assertTrue(treeBuilder.inScope("td"));
        assertTrue(treeBuilder.inScope("table"));
        assertFalse(treeBuilder.inScope("span"));
    }

    // Tests inListItemScope, inButtonScope, inTableScope and inSelectScope
    @Test
    public void testSpecialScopes_targetElements_correctScopeEvaluation() {
        treeBuilder.insertStartTag("html");
        treeBuilder.insertStartTag("body");
        treeBuilder.insertStartTag("ol");
        treeBuilder.insertStartTag("li");

        assertTrue(treeBuilder.inListItemScope("li"));
        assertFalse(treeBuilder.inListItemScope("ol"));

        treeBuilder.insertStartTag("button");
        assertTrue(treeBuilder.inButtonScope("button"));

        treeBuilder.insertStartTag("table");
        assertTrue(treeBuilder.inTableScope("table"));

        Element select = treeBuilder.insertStartTag("select");
        treeBuilder.insertStartTag("option");
        assertTrue(treeBuilder.inSelectScope("option"));
        assertFalse(treeBuilder.inSelectScope("input"));
    }

    // Tests active formatting elements push, limit to 3 duplicates, and pop
    @Test
    public void testPushActiveFormattingElements_maxThreeDuplicates_dropsOldestDuplicate() {
        Attributes attr = new Attributes();
        attr.put("class", "bold");

        Element b1 = new Element(Tag.valueOf("b"), "", attr.clone());
        Element b2 = new Element(Tag.valueOf("b"), "", attr.clone());
        Element b3 = new Element(Tag.valueOf("b"), "", attr.clone());
        Element b4 = new Element(Tag.valueOf("b"), "", attr.clone());

        treeBuilder.pushActiveFormattingElements(b1);
        treeBuilder.pushActiveFormattingElements(b2);
        treeBuilder.pushActiveFormattingElements(b3);
        assertTrue(treeBuilder.isInActiveFormattingElements(b1));

        treeBuilder.pushActiveFormattingElements(b4);
        assertFalse(treeBuilder.isInActiveFormattingElements(b1));
        assertTrue(treeBuilder.isInActiveFormattingElements(b4));
    }

    // Tests formatting element marker and clearing to last marker
    @Test
    public void testClearFormattingElementsToLastMarker_withMarker_clearsOnlyAfterMarker() {
        Element b = new Element(Tag.valueOf("b"), "");
        Element i = new Element(Tag.valueOf("i"), "");

        treeBuilder.pushActiveFormattingElements(b);
        treeBuilder.insertMarkerToFormattingElements();
        treeBuilder.pushActiveFormattingElements(i);

        assertEquals(i, treeBuilder.lastFormattingElement());
        treeBuilder.clearFormattingElementsToLastMarker();
        assertEquals(b, treeBuilder.lastFormattingElement());
    }

    // Tests insert(Token.Character), insert(Token.Comment), and insertForm
    @Test
    public void testInsertNodes_differentTokenTypes_createsProperNodes() {
        treeBuilder.insertStartTag("html");
        treeBuilder.insertStartTag("body");

        Token.Comment commentToken = new Token.Comment();
        commentToken.data.append("test comment");
        treeBuilder.insert(commentToken);

        Token.Character charToken = new Token.Character();
        charToken.data("sample text");
        treeBuilder.insert(charToken);

        Token.StartTag formStart = new Token.StartTag();
        formStart.nameAttr("form", new Attributes());
        FormElement form = treeBuilder.insertForm(formStart, true);

        assertNotNull(form);
        assertEquals(form, treeBuilder.getFormElement());
        assertTrue(treeBuilder.onStack(form));
    }

    // Tests foster parenting when fosterInserts flag is set
    @Test
    public void testInsertInFosterParent_fosterInsertsEnabled_insertsBeforeTable() {
        treeBuilder.insertStartTag("html");
        Element body = treeBuilder.insertStartTag("body");
        Element table = treeBuilder.insertStartTag("table");
        treeBuilder.setFosterInserts(true);

        TextNode textNode = new TextNode("fostered text");
        treeBuilder.insertInFosterParent(textNode);

        assertEquals(body, textNode.parent());
        assertEquals(table, textNode.nextSibling());
    }

    // Tests maybeSetBaseUri only updates baseUri once
    @Test
    public void testMaybeSetBaseUri_multipleBaseTags_onlySetsFirstUri() {
        Element base1 = new Element(Tag.valueOf("base"), "http://example.com");
        base1.attr("href", "http://example.com/first/");

        Element base2 = new Element(Tag.valueOf("base"), "http://example.com");
        base2.attr("href", "http://example.com/second/");

        treeBuilder.maybeSetBaseUri(base1);
        assertEquals("http://example.com/first/", treeBuilder.getBaseUri());

        treeBuilder.maybeSetBaseUri(base2);
        assertEquals("http://example.com/first/", treeBuilder.getBaseUri());
    }

    // Tests isSpecial method for known special tags vs custom tags
    @Test
    public void testIsSpecial_standardAndCustomTags_identifiesCorrectly() {
        Element div = new Element(Tag.valueOf("div"), "");
        Element custom = new Element(Tag.valueOf("custom-tag"), "");

        assertTrue(treeBuilder.isSpecial(div));
        assertFalse(treeBuilder.isSpecial(custom));
    }
}