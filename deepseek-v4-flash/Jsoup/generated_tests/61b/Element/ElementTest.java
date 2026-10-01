package org.jsoup.nodes;

import org.junit.Test;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class ElementTest {

    // Tests exact class match
    @Test
    public void testHasClass_exactMatch_returnsTrue() {
        Element el = new Element("div");
        el.attr("class", "test");
        assertTrue(el.hasClass("test"));
    }

    // Tests partial class match (multiple classes)
    @Test
    public void testHasClass_partialMatch_returnsTrue() {
        Element el = new Element("div");
        el.attr("class", "test foo");
        assertTrue(el.hasClass("foo"));
    }

    // Tests no match
    @Test
    public void testHasClass_noMatch_returnsFalse() {
        Element el = new Element("div");
        el.attr("class", "test");
        assertFalse(el.hasClass("other"));
    }

    // Tests empty class attribute returns false
    @Test
    public void testHasClass_emptyClassAttr_returnsFalse() {
        Element el = new Element("div");
        assertFalse(el.hasClass("test"));
    }

    // Tests class with surrounding whitespace
    @Test
    public void testHasClass_whitespaceSurround_returnsTrue() {
        Element el = new Element("div");
        el.attr("class", "  test  ");
        assertTrue(el.hasClass("test"));
    }

    // Tests class name exactly the length of attribute
    @Test
    public void testHasClass_equalLength_returnsTrue() {
        Element el = new Element("div");
        el.attr("class", "test");
        assertTrue(el.hasClass("test"));
    }

    // Tests class name shorter than attribute length with trailing space
    @Test
    public void testHasClass_trailingSpace_returnsTrue() {
        Element el = new Element("div");
        el.attr("class", "test  ");
        assertTrue(el.hasClass("test"));
    }

    // Tests text normalization (multiple spaces and newlines)
    @Test
    public void testText_simpleText_returnsNormalized() {
        Element p = new Element("p");
        p.appendChild(new TextNode("Hello  there", ""));
        assertEquals("Hello there", p.text());
    }

    // Tests block elements add space between text
    @Test
    public void testText_blockElementAddsSpace() {
        Element div = new Element("div");
        div.appendChild(new TextNode("Hello", ""));
        Element span = new Element("span");
        span.appendChild(new TextNode("World", ""));
        div.appendChild(span);
        assertEquals("Hello World", div.text());
    }

    // Tests ownText returns only direct text nodes
    @Test
    public void testOwnText_onlyDirectText_returnsOwnText() {
        Element p = new Element("p");
        p.appendChild(new TextNode("Hello ", ""));
        Element b = new Element("b");
        b.appendChild(new TextNode("there", ""));
        p.appendChild(b);
        p.appendChild(new TextNode(" now!", ""));
        assertEquals("Hello now!", p.ownText());
    }

    // Tests cssSelector with id attribute
    @Test
    public void testCssSelector_withId_returnsId() {
        Element el = new Element("div");
        el.attr("id", "myid");
        assertEquals("#myid", el.cssSelector());
    }

    // Tests cssSelector produces parent > child path
    @Test
    public void testCssSelector_withParent_returnsPath() {
        Element parent = new Element("div");
        Element child = new Element("span");
        parent.appendChild(child);
        String selector = child.cssSelector();
        assertTrue(selector.contains("div > span"));
    }

    // Tests classNames returns set of class names
    @Test
    public void testClassNames_multipleClasses_returnsSet() {
        Element el = new Element("div");
        el.attr("class", "a b c");
        Set<String> classes = el.classNames();
        assertEquals(3, classes.size());
        assertTrue(classes.contains("a"));
        assertTrue(classes.contains("b"));
        assertTrue(classes.contains("c"));
    }

    // Tests classNames returns empty set when no class attribute
    @Test
    public void testClassNames_emptyClass_returnsEmptySet() {
        Element el = new Element("div");
        assertTrue(el.classNames().isEmpty());
    }

    // Tests addClass adds new class
    @Test
    public void testAddClass_newClass_added() {
        Element el = new Element("div");
        el.addClass("test");
        assertTrue(el.hasClass("test"));
    }

    // Tests removeClass removes existing class
    @Test
    public void testRemoveClass_existingClass_removed() {
        Element el = new Element("div");
        el.attr("class", "test foo");
        el.removeClass("test");
        assertFalse(el.hasClass("test"));
        assertTrue(el.hasClass("foo"));
    }

    // Tests toggleClass adds class when absent
    @Test
    public void testToggleClass_addWhenAbsent() {
        Element el = new Element("div");
        el.toggleClass("test");
        assertTrue(el.hasClass("test"));
    }

    // Tests toggleClass removes class when present
    @Test
    public void testToggleClass_removeWhenPresent() {
        Element el = new Element("div");
        el.attr("class", "test");
        el.toggleClass("test");
        assertFalse(el.hasClass("test"));
    }

    // Tests val with textarea returns its text content
    @Test
    public void testVal_textarea_returnsText() {
        Element textarea = new Element("textarea");
        textarea.appendChild(new TextNode("content", ""));
        assertEquals("content", textarea.val());
    }

    // Tests val with input returns value attribute
    @Test
    public void testVal_input_returnsAttributeValue() {
        Element input = new Element("input");
        input.attr("value", "test");
        assertEquals("test", input.val());
    }

    // Tests appendChild adds child correctly
    @Test
    public void testAppendChild_addsChild() {
        Element parent = new Element("div");
        Element child = new Element("span");
        parent.appendChild(child);
        assertEquals(1, parent.children().size());
        assertSame(child, parent.child(0));
    }

    // Tests insertChildren with invalid negative index throws
    @Test(expected = IllegalArgumentException.class)
    public void testInsertChildren_invalidNegativeIndex_throwsException() {
        Element parent = new Element("div");
        parent.appendChild(new Element("span"));
        List<Node> empty = new ArrayList<>();
        parent.insertChildren(-5, empty);
    }

    // Tests insertChildren with index greater than size throws
    @Test(expected = IllegalArgumentException.class)
    public void testInsertChildren_invalidTooHighIndex_throwsException() {
        Element parent = new Element("div");
        parent.appendChild(new Element("span"));
        List<Node> empty = new ArrayList<>();
        parent.insertChildren(5, empty);
    }

    // Tests data method returns combined data from DataNode children
    @Test
    public void testData_returnsDataNodes() {
        Element script = new Element("script");
        script.appendChild(new DataNode("alert('hi');", ""));
        assertEquals("alert('hi');", script.data());
    }

    // Tests hasText returns true with non-blank text
    @Test
    public void testHasText_withText_returnsTrue() {
        Element p = new Element("p");
        p.appendChild(new TextNode("hello", ""));
        assertTrue(p.hasText());
    }

    // Tests hasText returns false with only whitespace
    @Test
    public void testHasText_blankText_returnsFalse() {
        Element p = new Element("p");
        p.appendChild(new TextNode("   ", ""));
        assertFalse(p.hasText());
    }

    // Tests elementSiblingIndex returns correct index
    @Test
    public void testElementSiblingIndex_singleChild_returnsZero() {
        Element parent = new Element("div");
        Element child = new Element("span");
        parent.appendChild(child);
        assertEquals(0, (int) child.elementSiblingIndex());
    }

    // Tests parents returns ancestors in correct order
    @Test
    public void testParents_returnsAncestors() {
        Element grandparent = new Element("div");
        Element parent = new Element("div");
        Element child = new Element("span");
        grandparent.appendChild(parent);
        parent.appendChild(child);
        Elements parents = child.parents();
        assertEquals(2, parents.size());
        assertSame(parent, parents.get(0));
        assertSame(grandparent, parents.get(1));
    }

    // ================== New tests for uncovered coverage ==================

    @Test
    public void testId() {
        Element el = new Element("div");
        el.attr("id", "myId");
        assertEquals("myId", el.id());
    }

    @Test
    public void testTagName() {
        Element el = new Element("div");
        assertEquals("div", el.tagName());
    }

    @Test
    public void testNodeName() {
        Element el = new Element("div");
        assertEquals("div", el.nodeName());
    }

    @Test
    public void testClassName() {
        Element el = new Element("div");
        el.attr("class", "testClass");
        assertEquals("testClass", el.className());
    }

    @Test
    public void testAttrGetSet() {
        Element el = new Element("div");
        el.attr("data-custom", "value");
        assertEquals("value", el.attr("data-custom"));
    }

    @Test
    public void testHasAttr_true() {
        Element el = new Element("div");
        el.attr("id", "x");
        assertTrue(el.hasAttr("id"));
    }

    @Test
    public void testHasAttr_false() {
        Element el = new Element("div");
        assertFalse(el.hasAttr("nonexistent"));
    }

    @Test
    public void testChildNodeSize() {
        Element parent = new Element("div");
        parent.appendChild(new Element("span"));
        parent.appendChild(new Element("p"));
        assertEquals(2, parent.childNodeSize());
    }

    @Test
    public void testChildren() {
        Element parent = new Element("div");
        Element child1 = new Element("span");
        Element child2 = new Element("p");
        parent.appendChild(child1);
        parent.appendChild(child2);
        Elements children = parent.children();
        assertEquals(2, children.size());
        assertSame(child1, children.get(0));
        assertSame(child2, children.get(1));
    }

    @Test
    public void testChild() {
        Element parent = new Element("div");
        Element child = new Element("span");
        parent.appendChild(child);
        assertSame(child, parent.child(0));
    }

    @Test
    public void testFirstElementSibling() {
        Element parent = new Element("div");
        Element first = new Element("span");
        Element second = new Element("p");
        parent.appendChild(first);
        parent.appendChild(second);
        assertSame(first, second.firstElementSibling());
    }

    @Test
    public void testLastElementSibling() {
        Element parent = new Element("div");
        Element first = new Element("span");
        Element last = new Element("p");
        parent.appendChild(first);
        parent.appendChild(last);
        assertSame(last, first.lastElementSibling());
    }

    @Test
    public void testNextElementSibling() {
        Element parent = new Element("div");
        Element first = new Element("span");
        Element second = new Element("p");
        parent.appendChild(first);
        parent.appendChild(second);
        assertSame(second, first.nextElementSibling());
    }

    @Test
    public void testPreviousElementSibling() {
        Element parent = new Element("div");
        Element first = new Element("span");
        Element second = new Element("p");
        parent.appendChild(first);
        parent.appendChild(second);
        assertSame(first, second.previousElementSibling());
    }

    @Test
    public void testSiblingElements() {
        Element parent = new Element("div");
        Element first = new Element("span");
        Element second = new Element("p");
        parent.appendChild(first);
        parent.appendChild(second);
        Elements siblings = first.siblingElements();
        assertEquals(1, siblings.size());
        assertSame(second, siblings.get(0));
    }

    @Test
    public void testParent() {
        Element parent = new Element("div");
        Element child = new Element("span");
        parent.appendChild(child);
        assertSame(parent, child.parent());
    }

    @Test
    public void testRoot() {
        Element root = new Element("html");
        Element body = new Element("body");
        root.appendChild(body);
        Element div = new Element("div");
        body.appendChild(div);
        assertSame(root, div.root());
    }

    @Test
    public void testGetAllElements() {
        Element root = new Element("div");
        Element span = new Element("span");
        Element p = new Element("p");
        root.appendChild(span);
        span.appendChild(p);
        Elements all = root.getAllElements();
        assertEquals(3, all.size());
    }

    @Test
    public void testGetElementsByTag() {
        Element root = new Element("div");
        Element span1 = new Element("span");
        Element span2 = new Element("span");
        root.appendChild(span1);
        root.appendChild(span2);
        Elements spans = root.getElementsByTag("span");
        assertEquals(2, spans.size());
    }

    @Test
    public void testGetElementsByClass() {
        Element root = new Element("div");
        Element el1 = new Element("p");
        el1.attr("class", "test");
        Element el2 = new Element("span");
        el2.attr("class", "test");
        root.appendChild(el1);
        root.appendChild(el2);
        Elements tests = root.getElementsByClass("test");
        assertEquals(2, tests.size());
    }

    @Test
    public void testGetElementById() {
        Element root = new Element("div");
        Element target = new Element("span");
        target.attr("id", "targetId");
        root.appendChild(target);
        Element found = root.getElementById("targetId");
        assertSame(target, found);
    }

    @Test
    public void testGetElementById_notFound_returnsNull() {
        Element root = new Element("div");
        Element target = new Element("span");
        target.attr("id", "otherId");
        root.appendChild(target);
        assertNull(root.getElementById("nonexistent"));
    }

    @Test
    public void testSelect() {
        Element root = new Element("div");
        Element p = new Element("p");
        p.appendChild(new TextNode("text", ""));
        root.appendChild(p);
        Elements selected = root.select("p");
        assertEquals(1, selected.size());
        assertSame(p, selected.get(0));
    }

    @Test
    public void testAppendText() {
        Element el = new Element("p");
        el.appendText("Hello");
        assertEquals("Hello", el.text());
    }

    @Test
    public void testPrependChild() {
        Element parent = new Element("div");
        Element child = new Element("span");
        parent.appendChild(child);
        Element prepended = new Element("p");
        parent.prependChild(prepended);
        assertSame(prepended, parent.child(0));
        assertSame(child, parent.child(1));
    }

    @Test
    public void testPrependText() {
        Element el = new Element("p");
        el.appendChild(new TextNode("World", ""));
        el.prependText("Hello ");
        assertEquals("Hello World", el.text());
    }

    @Test
    public void testAppendElement() {
        Element parent = new Element("div");
        Element child = parent.appendElement("span");
        assertNotNull(child);
        assertEquals("span", child.tagName());
        assertEquals(1, parent.children().size());
        assertSame(child, parent.child(0));
    }

    @Test
    public void testPrependElement() {
        Element parent = new Element("div");
        Element child1 = parent.appendElement("span");
        Element child2 = parent.prependElement("p");
        assertSame(child2, parent.child(0));
        assertSame(child1, parent.child(1));
    }

    @Test
    public void testEmpty() {
        Element parent = new Element("div");
        parent.appendChild(new Element("span"));
        parent.appendChild(new TextNode("text", ""));
        parent.empty();
        assertEquals(0, parent.childNodeSize());
    }

    @Test
    public void testRemove() {
        Element parent = new Element("div");
        Element child = new Element("span");
        parent.appendChild(child);
        child.remove();
        assertEquals(0, parent.children().size());
    }

    @Test
    public void testHtml() {
        Element div = new Element("div");
        div.appendChild(new Element("span"));
        assertEquals("<span></span>", div.html());
    }

    @Test
    public void testOuterHtml() {
        Element div = new Element("div");
        div.appendChild(new Element("span"));
        assertEquals("<div><span></span></div>", div.outerHtml());
    }

    @Test
    public void testTextNodes() {
        Element p = new Element("p");
        p.appendChild(new TextNode("Hello ", ""));
        p.appendChild(new Element("b"));
        p.appendChild(new TextNode("World", ""));
        List<TextNode> textNodes = p.textNodes();
        assertEquals(2, textNodes.size());
        assertEquals("Hello ", textNodes.get(0).getWholeText());
        assertEquals("World", textNodes.get(1).getWholeText());
    }

    @Test
    public void testDataNodes() {
        Element script = new Element("script");
        script.appendChild(new DataNode("data1", ""));
        script.appendChild(new DataNode("data2", ""));
        List<DataNode> dataNodes = script.dataNodes();
        assertEquals(2, dataNodes.size());
        assertEquals("data1", dataNodes.get(0).getWholeData());
        assertEquals("data2", dataNodes.get(1).getWholeData());
    }

    @Test
    public void testHasAttributes_withAttributes_returnsTrue() {
        Element el = new Element("div");
        el.attr("id", "x");
        assertTrue(el.hasAttributes());
    }

    @Test
    public void testHasAttributes_noAttributes_returnsFalse() {
        Element el = new Element("div");
        assertFalse(el.hasAttributes());
    }

    @Test
    public void testAttributes_size() {
        Element el = new Element("div");
        el.attr("id", "x");
        el.attr("class", "y");
        assertEquals(2, el.attributes().size());
    }

    @Test
    public void testTag() {
        Element el = new Element("div");
        assertEquals("div", el.tag().getName());
    }

    @Test
    public void testCssSelector_class() {
        Element el = new Element("div");
        el.attr("class", "myclass");
        assertEquals("div.myclass", el.cssSelector());
    }

    @Test
    public void testCssSelector_tagOnly() {
        Element el = new Element("span");
        assertEquals("span", el.cssSelector());
    }
}