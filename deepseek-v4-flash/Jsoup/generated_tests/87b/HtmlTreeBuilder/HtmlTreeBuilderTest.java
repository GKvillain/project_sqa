package org.jsoup.parser;

import org.junit.Before;
import org.junit.Test;

import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import static org.junit.Assert.*;

public class HtmlTreeBuilderTest {

    private HtmlTreeBuilder builder;
    private Parser parser;

    @Before
    public void setUp() {
        parser = Parser.htmlParser();
        builder = new HtmlTreeBuilder();
    }

    // Helper to set private fields for white-box testing
    private void setField(Object target, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    // Tests normal parsing of simple HTML
    @Test
    public void testParseSimpleHTML_createsDocument() {
        Document doc = Parser.parse("<p>Hello</p>", "http://base");
        Element body = doc.body();
        assertEquals(1, body.children().size());
        assertEquals("p", body.child(0).tagName());
        assertEquals("Hello", body.child(0).text());
    }

    // Tests self-closing tag handling
    @Test
    public void testParseSelfClosingTag_handlesCorrectly() {
        Document doc = Parser.parse("<br/><hr/>", "http://base");
        assertEquals(1, doc.select("br").size());
        assertEquals(1, doc.select("hr").size());
    }

    // Tests auto-closing of <p> when a second <p> is encountered
    @Test
    public void testParseWithMissingClosingTag_autoClosesParagraph() {
        Document doc = Parser.parse("<p>One<p>Two", "http://base");
        Elements ps = doc.select("p");
        assertEquals(2, ps.size());
        assertEquals("One", ps.get(0).text());
        assertEquals("Two", ps.get(1).text());
    }

    // Tests nested formatting elements parsing
    @Test
    public void testParseNestedFormattingElements() {
        Document doc = Parser.parse("<b><i>text</i></b>", "http://base");
        Element b = doc.selectFirst("b");
        assertNotNull(b);
        Element i = b.selectFirst("i");
        assertNotNull(i);
        assertEquals("text", i.text());
    }

    // Tests foster insertion of text before a table
    @Test
    public void testParseTableWithFosterInserts_placesTextBeforeTable() {
        Document doc = Parser.parse("<table>text</table>", "http://base");
        Element body = doc.body();
        assertTrue(body.childNode(0) instanceof TextNode);
        assertEquals("text", ((TextNode) body.childNode(0)).getWholeText());
        assertEquals("table", ((Element) body.childNode(1)).tagName());
    }

    // Tests standard table structure parsing
    @Test
    public void testParseTableStructure() {
        Document doc = Parser.parse("<table><tr><td>Hello</td></tr></table>", "http://base");
        Element table = doc.selectFirst("table");
        assertNotNull(table);
        Element tr = table.selectFirst("tr");
        assertNotNull(tr);
        Element td = tr.selectFirst("td");
        assertNotNull(td);
        assertEquals("Hello", td.text());
    }

    // Tests parseFragment without context element
    @Test
    public void testParseFragmentWithoutContext() {
        List<Node> nodes = parser.parseFragment("<p>para</p>", null, "");
        assertEquals(1, nodes.size());
        assertEquals("p", ((Element) nodes.get(0)).tagName());
    }

    // Tests parseFragment with a <td> context element
    @Test
    public void testParseFragmentWithContext() {
        Document contextDoc = Parser.parse("<table><tr><td></td></tr></table>", "");
        Element context = contextDoc.selectFirst("td");
        List<Node> nodes = parser.parseFragment("<b>bold</b>", context, "");
        assertEquals(1, nodes.size());
        assertEquals("b", ((Element) nodes.get(0)).tagName());
        assertEquals("bold", ((Element) nodes.get(0)).text());
    }

    // White-box: tests resetInsertionMode when context is <td> (bug #87b)
    @Test
    public void testResetInsertionMode_tdContext_returnsInBody() throws Exception {
        builder.initialiseParse(new StringReader(""), "http://base", parser);
        setField(builder, "fragmentParsing", true);
        Element td = new Element(Tag.valueOf("td"), "");
        setField(builder, "contextElement", td);
        ArrayList<Element> stack = builder.getStack();
        stack.clear();
        stack.add(new Element(Tag.valueOf("html"), ""));
        builder.resetInsertionMode();
        assertEquals(HtmlTreeBuilderState.InBody, builder.state());
    }

    // White-box: tests resetInsertionMode when context is <th>
    @Test
    public void testResetInsertionMode_thContext_returnsInBody() throws Exception {
        builder.initialiseParse(new StringReader(""), "http://base", parser);
        setField(builder, "fragmentParsing", true);
        Element th = new Element(Tag.valueOf("th"), "");
        setField(builder, "contextElement", th);
        ArrayList<Element> stack = builder.getStack();
        stack.clear();
        stack.add(new Element(Tag.valueOf("html"), ""));
        builder.resetInsertionMode();
        assertEquals(HtmlTreeBuilderState.InBody, builder.state());
    }

    // White-box: tests resetInsertionMode when <td> is not the last element
    @Test
    public void testResetInsertionMode_tdNonLast_returnsInCell() {
        builder.initialiseParse(new StringReader(""), "http://base", parser);
        ArrayList<Element> stack = builder.getStack();
        stack.clear();
        stack.add(new Element(Tag.valueOf("html"), ""));
        stack.add(new Element(Tag.valueOf("body"), ""));
        stack.add(new Element(Tag.valueOf("table"), ""));
        stack.add(new Element(Tag.valueOf("tr"), ""));
        stack.add(new Element(Tag.valueOf("td"), ""));
        builder.resetInsertionMode();
        assertEquals(HtmlTreeBuilderState.InCell, builder.state());
    }

    // White-box: tests resetInsertionMode when <th> is not the last element
    @Test
    public void testResetInsertionMode_thNonLast_returnsInCell() {
        builder.initialiseParse(new StringReader(""), "http://base", parser);
        ArrayList<Element> stack = builder.getStack();
        stack.clear();
        stack.add(new Element(Tag.valueOf("html"), ""));
        stack.add(new Element(Tag.valueOf("body"), ""));
        stack.add(new Element(Tag.valueOf("table"), ""));
        stack.add(new Element(Tag.valueOf("tr"), ""));
        stack.add(new Element(Tag.valueOf("th"), ""));
        builder.resetInsertionMode();
        assertEquals(HtmlTreeBuilderState.InCell, builder.state());
    }

    // Tests generateImpliedEndTags pops matching tags
    @Test
    public void testGenerateImpliedEndTags_popsMatchingTags() {
        builder.initialiseParse(new StringReader(""), "http://base", parser);
        ArrayList<Element> stack = builder.getStack();
        stack.clear();
        stack.add(new Element(Tag.valueOf("html"), ""));
        stack.add(new Element(Tag.valueOf("li"), ""));
        stack.add(new Element(Tag.valueOf("p"), ""));
        builder.generateImpliedEndTags(null);
        assertEquals(1, stack.size());
        assertEquals("html", stack.get(0).nodeName());
    }

    // Tests generateImpliedEndTags with exclude tag
    @Test
    public void testGenerateImpliedEndTags_excludeTag_preventsPop() {
        builder.initialiseParse(new StringReader(""), "http://base", parser);
        ArrayList<Element> stack = builder.getStack();
        stack.clear();
        stack.add(new Element(Tag.valueOf("html"), ""));
        stack.add(new Element(Tag.valueOf("li"), ""));
        stack.add(new Element(Tag.valueOf("p"), ""));
        builder.generateImpliedEndTags("p");
        assertEquals(3, stack.size());
    }

    // Tests pushActiveFormattingElements removes duplicate after three identical
    @Test
    public void testPushActiveFormattingElements_removesDuplicateAfterThree() {
        builder.initialiseParse(new StringReader(""), "http://base", parser);
        Element b1 = new Element(Tag.valueOf("b"), "");
        Element b2 = new Element(Tag.valueOf("b"), "");
        Element b3 = new Element(Tag.valueOf("b"), "");
        Element b4 = new Element(Tag.valueOf("b"), "");
        builder.pushActiveFormattingElements(b1);
        builder.pushActiveFormattingElements(b2);
        builder.pushActiveFormattingElements(b3);
        builder.pushActiveFormattingElements(b4);
        assertFalse(builder.isInActiveFormattingElements(b1));
        assertTrue(builder.isInActiveFormattingElements(b2));
        assertTrue(builder.isInActiveFormattingElements(b3));
        assertTrue(builder.isInActiveFormattingElements(b4));
    }

    // Tests reconstructFormattingElements creates a new element on stack
    @Test
    public void testReconstructFormattingElements_createsNewElementOnStack() {
        builder.initialiseParse(new StringReader(""), "http://base", parser);
        Element b = new Element(Tag.valueOf("b"), "");
        builder.pushActiveFormattingElements(b);
        builder.reconstructFormattingElements();
        Element top = builder.getStack().get(builder.getStack().size() - 1);
        assertEquals("b", top.nodeName());
        Element lastFormat = builder.lastFormattingElement();
        assertNotNull(lastFormat);
        assertNotSame(b, lastFormat);
        assertTrue(builder.onStack(lastFormat));
    }

    // Tests getActiveFormattingElement returns correct element (with marker)
    @Test
    public void testGetActiveFormattingElement_returnsCorrectElement() {
        builder.initialiseParse(new StringReader(""), "http://base", parser);
        Element b1 = new Element(Tag.valueOf("b"), "");
        Element b2 = new Element(Tag.valueOf("b"), "");
        builder.pushActiveFormattingElements(b1);
        builder.pushActiveFormattingElements(b2);
        assertSame(b2, builder.getActiveFormattingElement("b"));
        builder.insertMarkerToFormattingElements();
        Element b3 = new Element(Tag.valueOf("b"), "");
        builder.pushActiveFormattingElements(b3);
        assertSame(b3, builder.getActiveFormattingElement("b"));
    }

    // Tests inScope returns true when target is in scope
    @Test
    public void testInScope_true() {
        builder.initialiseParse(new StringReader(""), "http://base", parser);
        ArrayList<Element> stack = builder.getStack();
        stack.clear();
        stack.add(new Element(Tag.valueOf("html"), ""));
        stack.add(new Element(Tag.valueOf("div"), ""));
        assertTrue(builder.inScope("div"));
    }

    // Tests inScope returns false when blocked by a table element
    @Test
    public void testInScope_false_blockedByTable() {
        builder.initialiseParse(new StringReader(""), "http://base", parser);
        ArrayList<Element> stack = builder.getStack();
        stack.clear();
        stack.add(new Element(Tag.valueOf("html"), ""));
        stack.add(new Element(Tag.valueOf("table"), ""));
        stack.add(new Element(Tag.valueOf("div"), ""));
        assertFalse(builder.inScope("div"));
    }

    // Tests inTableScope returns true for a table element
    @Test
    public void testInTableScope_true() {
        builder.initialiseParse(new StringReader(""), "http://base", parser);
        ArrayList<Element> stack = builder.getStack();
        stack.clear();
        stack.add(new Element(Tag.valueOf("html"), ""));
        stack.add(new Element(Tag.valueOf("table"), ""));
        assertTrue(builder.inTableScope("table"));
    }

    // Tests inSelectScope returns true for an option element
    @Test
    public void testInSelectScope_true() {
        builder.initialiseParse(new StringReader(""), "http://base", parser);
        ArrayList<Element> stack = builder.getStack();
        stack.clear();
        stack.add(new Element(Tag.valueOf("html"), ""));
        stack.add(new Element(Tag.valueOf("select"), ""));
        stack.add(new Element(Tag.valueOf("option"), ""));
        assertTrue(builder.inSelectScope("option"));
    }

    // Tests inButtonScope false when a button element is encountered
    @Test
    public void testInButtonScope_false_blockedByButton() {
        builder.initialiseParse(new StringReader(""), "http://base", parser);
        ArrayList<Element> stack = builder.getStack();
        stack.clear();
        stack.add(new Element(Tag.valueOf("html"), ""));
        stack.add(new Element(Tag.valueOf("button"), ""));
        stack.add(new Element(Tag.valueOf("div"), ""));
        assertFalse(builder.inButtonScope("div"));
    }

    // Tests isFosterInserts default and setter
    @Test
    public void testIsFosterInserts_defaultFalse() {
        builder.initialiseParse(new StringReader(""), "http://base", parser);
        assertFalse(builder.isFosterInserts());
        builder.setFosterInserts(true);
        assertTrue(builder.isFosterInserts());
    }

    // Tests popStackToClose removes elements until target is encountered
    @Test
    public void testPopStackToClose_removesUntilEncounter() {
        builder.initialiseParse(new StringReader(""), "http://base", parser);
        ArrayList<Element> stack = builder.getStack();
        stack.clear();
        stack.add(new Element(Tag.valueOf("html"), ""));
        stack.add(new Element(Tag.valueOf("body"), ""));
        stack.add(new Element(Tag.valueOf("div"), ""));
        stack.add(new Element(Tag.valueOf("p"), ""));
        builder.popStackToClose("div");
        assertEquals(2, stack.size());
        assertEquals("html", stack.get(0).nodeName());
        assertEquals("body", stack.get(1).nodeName());
    }
}